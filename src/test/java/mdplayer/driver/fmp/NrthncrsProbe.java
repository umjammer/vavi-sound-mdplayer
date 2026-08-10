package mdplayer.driver.fmp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.lib.fmp.FmpWork;
import mdplayer.driver.BasePlugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class NrthncrsProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probeFmpWork() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");

        Path file = Path.of("/Users/nsano/Public/np2/FMPData/MusicData/NRTHNCRS.OZI");
        if (!Files.exists(file)) return;

        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Files.newInputStream(file), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();

        BaseDriver driver = plugin.getDriver();
        FmpWork[] workHolder = new FmpWork[1];
        driver.addViewListener(ev -> {
            if ("fmp".equals(ev.getName())) {
                workHolder[0] = (FmpWork) ev.getArguments()[0];
            }
        });

        short[] buffer = new short[2 * 1024];

        System.out.println("=== Direct FmpWork Part 3 (FM4) Probe ===");
        for (int i = 0; i < (int) (Common.VGMProcSampleRate * 15.0 / 1024); i++) {
            driver.render(buffer, 0, 1024);
            double sec = i * 1024.0 / Common.VGMProcSampleRate;
            FmpWork fw = workHolder[0];

            if (fw != null && sec >= 6.5 && sec <= 12.5 && i % 4 == 0) {
                int note = fw.note(3);
                int fnum = fw.fnum(3);
                int outFnum = fw.outFnum(3);
                int detune = fw.detune(3);
                System.out.printf("Time %5.2fs: part3 note=%d (0x%02X), fnum=0x%04X, outFnum=0x%04X, detune=%d%n",
                        sec, note, note, fnum, outFnum, detune);
            }
        }
    }
}
