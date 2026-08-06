package mdplayer.driver.vgm;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** temporary probe */
public class VgmProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        String file = "../simplevgm/tmp/Out_Run_(Arcade)/01 Magical Sound Shower.vgz";

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "midiMode", 0, "songNo", 0));
        plugin.prepare();

        VgmDriver driver = (VgmDriver) plugin.getDriver();
        System.err.printf("XXX vgmEof: 0x%x, loopOffsetRaw: 0x%x, loopSamples: %d, totalSamples: %d, dataLen: 0x%x%n", driver.vgm.vgmEof, driver.vgm.vgmLoopOffset, driver.loopCounter, driver.totalCounter, plugin.getData().length);

        short[] buf = new short[4];
        long samples = 0;
        int lastLoop = -1;
        long lastLoopSample = 0;
        int sampleRate = mdplayer.Setting.getInstance().getOutputDevice().getSampleRate();
        while (samples < (long) sampleRate * 60 * 10) { // 10 min max
            int r = driver.render(buf, 0, buf.length);
            if (r < 0) { System.err.println("XXX render < 0"); break; }
            samples += r / 2;
            if (driver.curLoop != lastLoop) {
                System.err.printf("XXX loop %d at %.2f sec (delta %.2f), vgmAdr 0x%x, stopped=%s%n", driver.curLoop, samples / (double) sampleRate, (samples - lastLoopSample) / (double) sampleRate, driver.vgm.vgmAdr, driver.stopped);
                lastLoop = driver.curLoop;
                lastLoopSample = samples;
            }
            if (driver.stopped) { System.err.println("XXX stopped at " + samples / (double) sampleRate); break; }
            if (driver.curLoop > 3) { System.err.println("XXX 4 loops done"); break; }
        }
        System.err.printf("XXX end: samples=%d (%.2f sec) curLoop=%d%n", samples, samples / (double) sampleRate, driver.curLoop);
    }
}
