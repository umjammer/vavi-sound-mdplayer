/*
 * scratch probe: what the fmdsp rows show for an MDX
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;


public class MdxPcmProbe {

    @Test
    void probe() throws Exception {
        LocalProperties.bind();
        Path path = Path.of(System.getProperty("mdx.file",
                "/Users/nsano/Public/np2/x68000mdx/Arrange/RUNUPC.MDX"));

        FileFormat format = FileFormat.getFileFormat(path.toString());
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(path))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", path.toString()));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);

        short[] buffer = new short[1024];
        TrackStatus status = new TrackStatus();
        for (int block = 0; block < 20; block++) {
            for (int i = 0; i < 44100 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
                source.snapshot();
            }
            StringBuilder sb = new StringBuilder("block " + block + "\n");
            for (TrackId t : TrackId.values()) {
                source.readStatus(t, status);
                String name = source.trackTypeName(t);
                if (!status.playing && name == null) continue;
                sb.append("  %-12s %-6s #%-3d key=%02x vol=%03d tn=%03d ticks=%03d level=%s %s%n"
                        .formatted(t, name, source.trackNumber(t), status.key, status.volume,
                                status.toneNum, status.ticks, levelOf(source, t), status.info));
            }
            var mxdrv = ((mdplayer.driver.mxdrv.MxDriver) plugin.getDriver()).getMxdrv();
            var part = new mdplayer.driver.mxdrv.MXDRV.PcmPart();
            sb.append("  pcm8=").append(mxdrv.isPcm8Mode()).append('\n');
            for (int ch = 0; ch < 8; ch++) {
                mxdrv.getPcmPart(ch, part);
                sb.append("    ch%d note=%3d sample=%3d v=%3d level=%3d pan=%d on=%b len=%d%n"
                        .formatted(ch, part.note, part.sample, part.volume, part.level, part.pan,
                                part.keyOn, part.length));
            }
            for (int c = 0; c < vavi.sound.visualizer.fmdsp.LevelDataSource.COUNT; c++) {
                sb.append("    col%02d %-5s %-10s %5d %s%n".formatted(c, source.label(c),
                        source.track(c), source.level(c), source.pan(c)));
            }
            System.err.print(sb);
        }
        plugin.stop();
        plugin.close();
    }

    private static String levelOf(ChipFmDspSource source, TrackId t) {
        for (int c = 0; c < vavi.sound.visualizer.fmdsp.LevelDataSource.COUNT; c++) {
            if (source.track(c) == t) return "%d(%s,%s)".formatted(source.level(c), source.pan(c), source.label(c));
        }
        return "-";
    }
}
