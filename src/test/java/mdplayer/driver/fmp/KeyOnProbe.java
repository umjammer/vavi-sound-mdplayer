/*
 * scratch: find the bit in the part work that says "the key is down"
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.Setting;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


public class KeyOnProbe {

    static final int STRIDE = 0x6f;

    /** the chip's own key on state, which is the truth we match against */
    final boolean[] keyOn = new boolean[6];

    void write(int p, int a, int d) {
        int port = (p & 0xff) == 0x8a ? 0 : 1;
        if (port == 0 && a == 0x28) {
            int c = d & 0x07;
            int ch = c < 3 ? c : (c >= 4 && c <= 6 ? c - 1 : -1);
            if (ch >= 0) keyOn[ch] = (d & 0xf0) != 0;
        }
    }

    /** hit[r][bit], miss[r][bit] over all ticks and channels */
    final int[][] hit = new int[0x80][9];
    final int[][] total = new int[0x80][9];

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/FMP/FMPDDISK/FF5_GILG.OVI");
        String dir = "/Users/nsano/Public/np2/FMP/FMPDDISK";

        FMP fmp = new FMP();
        fmp.ft = new FileTemp();
        fmp.ft.saveCompiledFile = Setting.getInstance().getOther().getSaveCompiledFile();
        fmp.setSearchPath(dir);
        fmp.sampleRate = 44100;
        fmp.charset = Charset.forName("Shift_JIS");
        fmp.dir = dir;
        fmp.blockWrite = b -> {};
        fmp.setPPZ8PCMData = (a, b, c) -> {};
        fmp.setPPZ8Data = (a, b, c) -> {};
        fmp.opnaWrite = this::write;
        fmp.playingFileName = file;
        fmp.run(Files.readAllBytes(Path.of(file)));

        var mem = fmp.nise98.getMem();
        for (int i = 0; i < 44100 * 25; i++) {
            fmp.nise98.runTimer();
            if (!fmp.nise98.intTimer()) continue;
            fmp.processOneFrame(() -> {});
            int key0 = fmp.workPtr + 0x3b;

            for (int ch = 0; ch < 6; ch++) {
                for (int r = 0; r < 0x80; r++) {
                    int v = mem.peekB(key0 + STRIDE * ch + r - 0x20) & 0xff;
                    for (int bit = 0; bit < 8; bit++) {
                        boolean set = (v & (1 << bit)) != 0;
                        total[r][bit]++;
                        if (set == keyOn[ch]) hit[r][bit]++;
                    }
                    // and the whole byte being nonzero
                    total[r][8]++;
                    if ((v != 0) == keyOn[ch]) hit[r][8]++;
                }
            }
        }

        System.err.println("-- predicates matching the chip's key on state --");
        for (int r = 0; r < 0x80; r++) {
            for (int bit = 0; bit < 9; bit++) {
                double acc = (double) hit[r][bit] / total[r][bit];
                if (acc > 0.9) {
                    System.err.printf("  key%+d %s : %.3f%n", r - 0x20,
                            bit == 8 ? "!= 0    " : "bit %d   ".formatted(bit), acc);
                }
            }
        }
    }
}
