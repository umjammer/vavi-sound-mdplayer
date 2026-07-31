/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.IntStream;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.util.archive.Archives;

import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Headless diagnostic: renders a file without audio and dumps what {@link ChipFmDspSource}
 * sees in the chip caches. Not a regression test.
 * <p>
 * Run with {@code -Dvavi.test=diag -Ddiag.file=<path>}.
 */
@PropsEntity(url = "file:local.properties")
class ChipCacheCheck {

    /**
     * The drivers that boot a real X68000/MSX/PC-98 binary look for it relative to these, and
     * fail to load at all without them - the same set {@code TestCase} binds.
     */
    @Property(name = "mdplayer.fmp.dir") String fmpDir;
    @Property(name = "mdplayer.fmp.pvi") String fmpPvi;
    @Property(name = "mdplayer.zms.dir") String zmsDir;
    @Property(name = "mdplayer.mgs.dir") String mgsDir;
    @Property(name = "mdplayer.ndp.dir") String ndpDir;
    @Property(name = "mdplayer.musica.dir") String musicaDir;
    @Property(name = "muap.dir.dta") String muapDirDta;
    @Property(name = "muap.dir.pcm") String muapDirPcm;

    @Property(name = "mdplayer.variant.pcm8") int variantPcm8;
    @Property(name = "mdplayer.variant.mpcm") int variantMpcm;
    @Property(name = "mdplayer.variant.ym2151") int variantYm2151;
    @Property(name = "mdplayer.variant.ym2413") int variantYm2413;
    @Property(name = "mdplayer.variant.ymf262") int variantYmf262;
    @Property(name = "mdplayer.variant.ay8910") int variantAy8910;

    @BeforeEach
    void setup() throws Exception {
        if (!Files.exists(Path.of("local.properties"))) return;
        PropsEntity.Util.bind(this);
        System.setProperty("mdplayer.fmp.dir", fmpDir);
        System.setProperty("mdplayer.fmp.pvi", fmpPvi);
        System.setProperty("mdplayer.zms.dir", zmsDir);
        System.setProperty("mdplayer.mgs.dir", mgsDir);
        System.setProperty("mdplayer.ndp.dir", ndpDir);
        System.setProperty("mdplayer.musica.dir", musicaDir);
        System.setProperty("muap.dir.dta", muapDirDta);
        System.setProperty("muap.dir.pcm", muapDirPcm);
        System.setProperty("mdplayer.variant.pcm8", String.valueOf(variantPcm8));
        System.setProperty("mdplayer.variant.mpcm", String.valueOf(variantMpcm));
        System.setProperty("mdplayer.variant.ym2151", String.valueOf(variantYm2151));
        System.setProperty("mdplayer.variant.ym2413", String.valueOf(variantYm2413));
        System.setProperty("mdplayer.variant.ymf262", String.valueOf(variantYmf262));
        System.setProperty("mdplayer.variant.ay8910", String.valueOf(variantAy8910));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() throws Exception {
        String file = System.getProperty("diag.file");
        System.err.println("file: " + file);

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);

        short[] buffer = new short[1024];
        TrackStatus status = new TrackStatus();
        for (int block = 0; block < 10; block++) {
            // ~1 s of audio per block, snapshotting at the real cadence
            for (int i = 0; i < 44100 * 2 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
                source.snapshot();
            }
            System.err.println("--- block " + block
                    + " driver=" + source.driverName()
                    + " counter=" + (plugin.getDriver() != null ? plugin.getDriver().counter : -1)
                    + " timerB=" + source.timerB() + " timerBCount=" + source.timerBCount());
            StringBuilder sb = new StringBuilder();
            for (TrackId t : new TrackId[] {TrackId.FM_1, TrackId.FM_2, TrackId.FM_3,
                    TrackId.SSG_1, TrackId.ADPCM, TrackId.PPZ8_1, TrackId.PPZ8_2}) {
                source.readStatus(t, status);
                sb.append(t).append(":playing=").append(status.playing)
                        .append(",key=").append(Integer.toHexString(status.key))
                        .append(",vol=").append(status.volume)
                        .append(",bar=").append(status.ticksLeft).append('/').append(status.ticks)
                        .append("  ");
            }
            System.err.println(sb);
            System.err.println("levels=" + IntStream.range(0, 19).map(source::level).boxed().toList());
            System.err.println("pans=" + IntStream.range(0, 19).mapToObj(source::pan).toList());
            TrackId[] disp = source.displayTracks();
            System.err.println("auto view=" + (disp == null ? null
                    : java.util.Arrays.stream(disp)
                            .map(t -> source.trackTypeName(t) + source.trackNumber(t))
                            .toList()));
            System.err.println("meter labels=" + IntStream.range(0, 19).mapToObj(source::label).toList());
            // exactly what the renderer puts under a meter: it blanks the key and centres the
            // pan for a track that is not "playing", and PPZ8/PDZF tracks never count as playing
            StringBuilder mb = new StringBuilder("meter readout=");
            for (int c = 0; c < 14; c++) {
                TrackId t = source.track(c);
                if (t == null || c == 9) {
                    mb.append(c).append(":-  ");
                    continue;
                }
                source.readStatus(t, status);
                boolean playing = status.playing
                        && status.info != vavi.sound.visualizer.fmdsp.TrackInfo.PPZ8
                        && status.info != vavi.sound.visualizer.fmdsp.TrackInfo.PDZF;
                String key = "---";
                if (playing && (status.key & 0xf) < 12) {
                    key = "%03d".formatted(((status.key >> 4) & 0xf) * 12 + (status.key & 0xf));
                }
                mb.append(c).append(':').append(key)
                        .append('/').append("%03d".formatted(status.toneNum))
                        .append('/').append(playing ? source.pan(c) : "CENTER").append("  ");
            }
            System.err.println(mb);
        }
    }
}
