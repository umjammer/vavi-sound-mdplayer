/*
 * scratch: sanity check FmpWork against the chip writes
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.Setting;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;
import mdplayer.lib.fmp.FmpWork;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


public class WorkCheck {

    /** what the chip was last told, to check the work area against */
    final int[][] fnum = new int[6][2];
    final boolean[] keyOn = new boolean[6];

    void write(int p, int a, int d) {
        int port = (p & 0xff) == 0x8a ? 0 : 1;
        if (port == 0 && a == 0x28) {
            int c = d & 0x07;
            int ch = c < 3 ? c : (c >= 4 && c <= 6 ? c - 1 : -1);
            if (ch >= 0) keyOn[ch] = (d & 0xf0) != 0;
        } else if (a >= 0xa0 && a <= 0xa2) {
            fnum[(a - 0xa0) + port * 3][0] = d;
        } else if (a >= 0xa4 && a <= 0xa6) {
            fnum[(a - 0xa4) + port * 3][1] = d;
        }
    }

    static final String[] names = {
            "FM1", "FM2", "FM3", "FM4", "FM5", "FM6", "ADPCM", "EX1", "EX2", "EX3", "SSG1", "SSG2", "SSG3"
    };

    static int part(int n) {
        return n < 10 ? n : n + 9; // 0-5 FM, 6 ADPCM, 7-9 FM3EX, 19-21 SSG
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void check() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/FMP/FMPDDISK/FF5_GILG.OVI");
        String dir = "/Users/nsano/Public/np2/FMP/FMPDDISK";

        FMP fmp = new FMP();
        fmp.ft = new FileTemp();
        fmp.ft.saveCompiledFile = Setting.getInstance().getOther().getSaveCompiledFile();
        fmp.setSearchPath(dir + ";" + Path.of(file).getParent() + ";/Users/nsano/Public/np2/PVI");
        fmp.sampleRate = 44100;
        fmp.charset = Charset.forName("Shift_JIS");
        fmp.dir = dir;
        fmp.blockWrite = b -> {};
        fmp.setPPZ8PCMData = (a, b, c) -> {};
        fmp.setPPZ8Data = (a, b, c) -> {};
        fmp.opnaWrite = this::write;
        fmp.playingFileName = file;
        fmp.run(Files.readAllBytes(Path.of(file)));

        FmpWork work = fmp.getWork();
        int tick = 0;
        for (int i = 0; i < 44100 * 25; i++) {
            fmp.nise98.runTimer();
            if (!fmp.nise98.intTimer()) continue;
            fmp.processOneFrame(() -> {});
            work.ticks++;
            if (++tick % 512 != 0) continue;

            System.err.printf("%n== tick %d  loop=%d%n", tick, work.loopCount());
            System.err.println("  part   play keyOn  key   note tone vol gate left detune fnum   chip fnum/keyon");
            for (int n = 0; n < names.length; n++) {
                int p = part(n);
                int note = work.note(p);
                String key = note < 0 ? "--" : "o%d%s".formatted(note / 12, new String[] {
                        "C ", "C+", "D ", "D+", "E ", "F ", "F+", "G ", "G+", "A ", "A+", "B "}[note % 12]);
                String chip = n < 6
                        ? "%04x %s".formatted((fnum[n][1] & 0x3f) << 8 | fnum[n][0] & 0xff, keyOn[n] ? "on" : "  ")
                        : "";
                System.err.printf("  %-6s %-4s %-5s %-5s %3d %4d %3d %4d %4d %6d  %04x  %s%n",
                        names[n], work.playing(p) ? "yes" : "no", work.keyOn(p) ? "on" : "", key,
                        note, work.toneNum(p), work.volume(p), work.gate(p, work.ticksLeft(p)), work.ticksLeft(p),
                        work.detune(p), work.fnum(p), chip);
            }
        }
    }
}
