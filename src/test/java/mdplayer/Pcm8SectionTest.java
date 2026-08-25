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

import mdplayer.chips.MPcmChip;
import mdplayer.chips.Pcm8Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import vavi.util.archive.Archives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * {@link Pcm8Chip} and {@link MPcmChip} are shared singletons, but which back end they stand for is
 * a per-driver setting: ZMUSIC, MXDRV, RCS and MNDRV each keep their own. The chip used to read
 * ZMUSIC's (PCM8) or MNDRV's (MPCM) section whoever was playing, so it could stand for one
 * instrument while the plugin had registered the other - which is how an MDX came to be played by
 * PCM8PP's no-op OPM and fell silent.
 * <p>
 * Each case below sets the driver's own section one way and every other section the other, so a
 * chip that read the wrong one would be caught by the value alone, and renders to prove the song
 * still sounds either way.
 * <p>
 * Run with: {@code mvn test -Dtest=Pcm8SectionTest -Dvavi.test=ai}
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-26 nsano initial version <br>
 */
class Pcm8SectionTest {

    static final String MDX = "/Users/nsano/Public/np2/MXDRV/Pops/LADY.MDX";
    static final String MND = "tmp/mnd/MND_BRA1/BRAN100.MND";
    /** an FM song: the MIDI-only ones in that directory render silent, see {@code MidiPlugin} */
    static final String ZMS = "tmp/zms/After_T.ZMS";

    @BeforeAll
    static void beforeAll() throws Exception {
        LocalProperties.bind();
    }

    private static BasePlugin<? extends BaseDriver> open(String file) throws Exception {
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "midiMode", 0, "songNo", 0));
        plugin.prepare();
        return plugin;
    }

    /** @return the RMS of {@code seconds} of the song, {@code 0} when it never sounds */
    private static double render(BasePlugin<? extends BaseDriver> plugin, int seconds) {
        int sampleRate = Setting.getInstance().getOutputDevice().getSampleRate();
        short[] buf = new short[(sampleRate / 120) * 2];
        double sum = 0;
        long n = 0;
        for (int i = 0; i < seconds * 120; i++) {
            plugin.getDriver().render(buf, 0, buf.length);
            for (short v : buf) {
                sum += (double) v * v;
                n++;
            }
        }
        return Math.sqrt(sum / n);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void mdxFollowsTheMxDrvSection() throws Exception {
        Setting s = Setting.getInstance();
        for (int v = 0; v <= 1; v++) {
            s.getMxDrv().pcm8Type = v;
            s.getZMusic().pcm8Type = 1 - v; // the section an MDX must NOT follow
            BasePlugin<? extends BaseDriver> plugin = open(MDX);
            int pcm8 = plugin.chipRegister.chip(Pcm8Chip.class).activeIndex(0);
            double rms = render(plugin, 5);
            System.err.printf("  MxDrv=%d ZMusic=%d -> pcm8=%d rms=%.1f%n", v, 1 - v, pcm8, rms);
            assertEquals(v, pcm8, "Pcm8Chip must follow MXDRV's section for an MDX");
            assertTrue(rms > 100, "an MDX should sound with MxDrv.pcm8Type=" + v);
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void zmsFollowsTheZMusicSection() throws Exception {
        Setting s = Setting.getInstance();
        for (int v = 0; v <= 1; v++) {
            s.getZMusic().pcm8Type = v;
            s.getZMusic().mpcmType = v;
            s.getMxDrv().pcm8Type = 1 - v;
            s.getMnDrv().mpcmType = 1 - v;
            BasePlugin<? extends BaseDriver> plugin = open(ZMS);
            int pcm8 = plugin.chipRegister.chip(Pcm8Chip.class).activeIndex(0);
            int mpcm = plugin.chipRegister.chip(MPcmChip.class).activeIndex(0);
            double rms = render(plugin, 5);
            System.err.printf("  ZMusic=%d others=%d -> pcm8=%d mpcm=%d rms=%.1f%n", v, 1 - v, pcm8, mpcm, rms);
            assertEquals(v, pcm8, "Pcm8Chip must follow ZMUSIC's section for a ZMS");
            assertEquals(v, mpcm, "MPcmChip must follow ZMUSIC's section for a ZMS");
            assertTrue(rms > 100, "a ZMS should sound with ZMusic.pcm8Type=" + v);
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void mndFollowsTheMnDrvSection() throws Exception {
        Setting s = Setting.getInstance();
        for (int v = 0; v <= 1; v++) {
            s.getMnDrv().mpcmType = v;
            s.getZMusic().mpcmType = 1 - v;
            BasePlugin<? extends BaseDriver> plugin = open(MND);
            int mpcm = plugin.chipRegister.chip(MPcmChip.class).activeIndex(0);
            double rms = render(plugin, 5);
            System.err.printf("  MnDrv=%d ZMusic=%d -> mpcm=%d rms=%.1f%n", v, 1 - v, mpcm, rms);
            assertEquals(v, mpcm, "MPcmChip must follow MNDRV's section for an MND");
            assertTrue(rms > 100, "an MND should sound with MnDrv.mpcmType=" + v);
        }
    }
}
