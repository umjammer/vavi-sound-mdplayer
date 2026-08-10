/*
 * scratch: dump the fmdsp MML columns and the keyboard keys over time, for FMP
 */

package mdplayer.driver.fmp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


class StatusProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/FMP/FMPDDISK/FF5_GILG.OVI");
        int seconds = Integer.getInteger("probe.seconds", 25);
        boolean verbose = Boolean.getBoolean("probe.verbose");

        for (Path p : list(file)) {
            try {
                one(p, seconds, verbose);
            } catch (Exception e) {
                System.err.printf("%-40s FAILED %s%n", p.getFileName(), e);
            }
        }
    }

    static List<Path> list(String file) throws Exception {
        Path p = Path.of(file);
        if (!Files.isDirectory(p)) return List.of(p);
        try (var s = Files.walk(p)) {
            return s.filter(x -> {
                String n = x.getFileName().toString().toUpperCase();
                return n.endsWith(".OPI") || n.endsWith(".OVI") || n.endsWith(".OZI");
            }).toList();
        }
    }

    static void one(Path file, int seconds, boolean verbose) throws Exception {
        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Files.newInputStream(file), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();

        FmpFmDspSource source = new FmpFmDspSource();
        source.setFilename(file.getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        driver.addViewListener(source::update);

        TrackStatus status = new TrackStatus();
        short[] buffer = new short[2 * 1024];
        int bends = 0, maxBend = 0, wild = 0, hw = 0;
        for (int i = 0; i < Common.VGMProcSampleRate * seconds / 1024; i++) {
            driver.render(buffer, 0, 1024);
            if (i % 4 != 0) continue;
            StringBuilder b = new StringBuilder();
            boolean bent = false;
            for (TrackId t : TrackId.values()) {
                source.readStatus(t, status);
                if (!status.playing) continue;
                if (status.status.length() > 6 && status.status.charAt(6) == 'H') hw++;
                if (status.actualKey != status.key && status.key != 0xff && status.actualKey != 0xff) {
                    bent = true;
                    int d = Math.abs(note(status.actualKey) - note(status.key));
                    maxBend = Math.max(maxBend, d);
                    if (d > 12) wild++;
                }
                b.append("  %-9s k=%02x a=%02x gt=%3d dt=%4d m=%s%n".formatted(
                        t, status.key, status.actualKey, status.gate, status.detune, status.status));
            }
            if (bent) bends++;
            if (verbose && bent && bends < 20) {
                System.err.printf("--- %.1f s (bent)%n%s", i * 1024.0 / Common.VGMProcSampleRate, b);
            }
        }
        System.err.printf("%-40s bent=%-6d maxBend=%-3d wild=%-3d hwLfo=%d%n", file.getFileName(), bends, maxBend, wild, hw);
    }

    static int note(int key) {
        return (key >> 4) * 12 + (key & 0xf);
    }
}
