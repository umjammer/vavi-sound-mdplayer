package mdplayer.driver.fmp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class AnalyzeCyanKeyDuration {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void analyzeDuration() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");

        Path file = Path.of("/Users/nsano/Public/np2/FMPData/MusicData/NRTHNCRS.OZI");
        if (!Files.exists(file)) return;

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

        System.out.println("=== Cyan Key Duration Analysis for NRTHNCRS.OZI (FM_4) ===");

        boolean inCyanState = false;
        double cyanStartSec = 0;
        int cyanFrameCount = 0;
        int cyanEventIndex = 0;

        // Scan 60 seconds of playback
        int totalFrames = (int) (Common.VGMProcSampleRate * 60.0 / 1024);
        for (int i = 0; i < totalFrames; i++) {
            driver.render(buffer, 0, 1024);
            double sec = i * 1024.0 / Common.VGMProcSampleRate;

            source.readStatus(TrackId.FM_4, status);
            boolean isCyan = status.playing && status.key != 0xff && status.actualKey != 0xff && status.actualKey != status.key;

            if (isCyan) {
                if (!inCyanState) {
                    inCyanState = true;
                    cyanStartSec = sec;
                    cyanFrameCount = 1;
                } else {
                    cyanFrameCount++;
                }
            } else {
                if (inCyanState) {
                    inCyanState = false;
                    cyanEventIndex++;
                    double durationMs = cyanFrameCount * (1024.0 / Common.VGMProcSampleRate) * 1000.0;
                    System.out.printf("Cyan Event #%02d at %5.2fs: duration = %6.1f ms (%2d audio buffer frames, ~%2d video frames at 60fps), key=0x%02X, actualKey=0x%02X, status=%s%n",
                            cyanEventIndex, cyanStartSec, durationMs, cyanFrameCount, (int)(durationMs * 60 / 1000), status.key, status.actualKey, status.status);
                }
            }
        }
    }
}
