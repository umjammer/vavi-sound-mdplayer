/*
 * scratch: find a song that drives all three SSG channels (and ADPCM)
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import mdplayer.emu.nise98.FileTemp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class PickSong {

    @Test
    @Disabled("for ai iteration")
    void pick() throws Exception {
        String dir = "/Users/nsano/Public/np2/FMP/FMPDDISK"; // FMP.COM lives here
        String songs = System.getProperty("probe.dir", dir);
        for (Path p : Files.list(Path.of(songs)).filter(f -> f.toString().matches(".*\\.(OVI|OPI|OZI)")).sorted().toList()) {
            try {
                Map<Integer, Integer> hist = new TreeMap<>();
                int[] ppz8 = new int[1];
                FMP fmp = new FMP();
                fmp.ft = new FileTemp();
                fmp.setSearchPath(dir + ";" + songs + ";/Users/nsano/Public/np2/PVI");
                fmp.sampleRate = 44100;
                fmp.charset = Charset.forName("Shift_JIS");
                fmp.dir = dir;
                fmp.blockWrite = b -> {};
                fmp.setPPZ8PCMData = (a, b, c) -> {};
                fmp.setPPZ8Data = (a, b, c) -> ppz8[0]++;
                fmp.opnaWrite = (pt, a, d) -> {
                    int port = (pt & 0xff) == 0x8a ? 0 : 1;
                    hist.merge(port * 0x100 + a, 1, Integer::sum);
                };
                fmp.playingFileName = p.toString();
                fmp.run(Files.readAllBytes(p));
                for (int i = 0; i < 44100 * 15; i++) {
                    fmp.nise98.runTimer();
                    if (!fmp.nise98.intTimer()) continue;
                    fmp.processOneFrame(() -> {});
                }
                int ssg1 = hist.getOrDefault(0x00, 0), ssg2 = hist.getOrDefault(0x02, 0), ssg3 = hist.getOrDefault(0x04, 0);
                int fm4 = hist.getOrDefault(0x1a0, 0);
                int adpcm = hist.getOrDefault(0x108, 0), rhythm = hist.getOrDefault(0x10, 0);
                System.out.printf("%-14s ssg=%4d/%4d/%4d fm4-6=%4d adpcm=%3d rhythm=%3d ppz8=%5d%n",
                        p.getFileName(), ssg1, ssg2, ssg3, fm4, adpcm, rhythm, ppz8[0]);
            } catch (Exception e) {
                System.out.printf("%-14s %s%n", p.getFileName(), e);
            }
        }
    }
}
