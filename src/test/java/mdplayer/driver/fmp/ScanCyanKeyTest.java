package mdplayer.driver.fmp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.pmd.PmdFmDspSource;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.sound.visualizer.fmdsp.TrackStatusSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class ScanCyanKeyTest {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void scanSustainedBends() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");

        List<Path> files = new ArrayList<>();
        Path dir = Path.of("/Users/nsano/Public/np2/FMPPMD");
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> {
                    String s = p.toString().toUpperCase();
                    return s.endsWith(".OPI") || s.endsWith(".OVI") || s.endsWith(".OZI") || s.endsWith(".M2") || s.endsWith(".MZ") || s.endsWith(".M");
                }).limit(50).forEach(files::add);
            }
        }

        for (Path file : files) {
            scanFile(file);
        }
    }

    private void scanFile(Path file) {
        try {
            FileFormat format = FileFormat.getFileFormat(file.toString());
            format.load(Files.newInputStream(file), null);
            var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
            plugin.setParams(format, Map.of("fileName", file.toString()));
            plugin.prepare();

            TrackStatusSource source;
            if (file.toString().toUpperCase().endsWith(".MZ") || file.toString().toUpperCase().endsWith(".M2") || file.toString().toUpperCase().endsWith(".M")) {
                PmdFmDspSource pmdSource = new PmdFmDspSource();
                pmdSource.setFilename(file.getFileName().toString());
                plugin.getDriver().addViewListener(pmdSource::update);
                source = pmdSource;
            } else {
                FmpFmDspSource fmpSource = new FmpFmDspSource();
                fmpSource.setFilename(file.getFileName().toString());
                plugin.getDriver().addViewListener(fmpSource::update);
                source = fmpSource;
            }

            BaseDriver driver = plugin.getDriver();
            TrackStatus status = new TrackStatus();
            short[] buffer = new short[2 * 1024];

            int currentConsecutiveBends = 0;
            int maxConsecutiveBends = 0;
            double maxBendStartSec = 0;
            TrackId maxBendTrack = null;

            for (int i = 0; i < Common.VGMProcSampleRate * 60 / 1024; i++) {
                driver.render(buffer, 0, 1024);
                if (i % 4 != 0) continue;
                double timestampSec = i * 1024.0 / Common.VGMProcSampleRate;

                boolean bent = false;
                for (TrackId t : TrackId.values()) {
                    source.readStatus(t, status);
                    if (!status.playing) continue;

                    if (status.actualKey != status.key && status.key != 0xff && status.actualKey != 0xff) {
                        bent = true;
                        currentConsecutiveBends++;
                        if (currentConsecutiveBends > maxConsecutiveBends) {
                            maxConsecutiveBends = currentConsecutiveBends;
                            maxBendStartSec = timestampSec;
                            maxBendTrack = t;
                        }
                    }
                }
                if (!bent) {
                    currentConsecutiveBends = 0;
                }
            }

            double durationSec = maxConsecutiveBends * (4096.0 / Common.VGMProcSampleRate);
            if (durationSec > 0.3) {
                System.out.printf("FOUND SUSTAINED BEND: %s [%s] at %.2fs for duration %.2fs (%d frames)%n",
                        file.getFileName(), maxBendTrack, maxBendStartSec, durationSec, maxConsecutiveBends);
            }
        } catch (Exception ignored) {
        }
    }
}
