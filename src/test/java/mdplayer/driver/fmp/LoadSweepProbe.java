/*
 * scratch: how long every FMP song under a directory takes to start
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Prints the slowest starters under {@code -Dprobe.root}, which is how a start-up regression
 * gets noticed - a song with a big .PVI is the one that shows it. {@link FmpLoadSpeedTest}
 * guards the worst of them; this walks the lot.
 */
class LoadSweepProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void sweep() throws Exception {
        Path root = Path.of(System.getProperty("probe.root", "/Users/nsano/Public/np2/FMP"));
        if (!Files.exists(root)) { System.out.println("skip " + root); return; }

        List<Path> songs;
        try (Stream<Path> s = Files.walk(root)) {
            songs = s.filter(Files::isRegularFile)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return n.endsWith(".ovi") || n.endsWith(".ozi") || n.endsWith(".opi");
                    })
                    .sorted().toList();
        }
        System.out.println("songs: " + songs.size());

        record R(String name, double sec, long adpcm) {}
        List<R> results = new ArrayList<>();

        for (Path f : songs) {
            try {
                byte[] data = Files.readAllBytes(f);
                FMP fmp = new FMP();
                fmp.setSearchPath("/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3;" + f.getParent());
                fmp.sampleRate = 55467;
                fmp.charset = Charset.forName("MS932");
                fmp.dir = System.getProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
                fmp.playingFileName = f.toString();
                fmp.ft = new FileTemp();
                AtomicLong adpcm = new AtomicLong();
                fmp.setPPZ8PCMFilename = (mode, fn) -> {};
                fmp.setPPZ8PCMData = (bank, mode, pcmData) -> {};
                fmp.setPPZ8Data = (port, adr, d) -> {};
                fmp.opnaWrite = (p, a, d) -> { if ((p & 0xff) == 0x8e && a == 0x08) adpcm.incrementAndGet(); };
                fmp.blockWrite = b -> {};

                long t0 = System.nanoTime();
                fmp.run(data);
                long t1 = System.nanoTime();
                results.add(new R(root.relativize(f).toString(), (t1 - t0) / 1e9, adpcm.get()));
            } catch (Throwable t) {
                System.out.printf("FAIL %s: %s%n", root.relativize(f), t);
            }
        }

        results.sort(Comparator.comparingDouble(R::sec).reversed());
        System.out.println("--- slowest 20 of " + results.size());
        results.stream().limit(20).forEach(r -> System.out.printf("%7.3f s  adpcm=%7d  %s%n", r.sec(), r.adpcm(), r.name()));
        System.out.printf("total %.1f s%n", results.stream().mapToDouble(R::sec).sum());
    }
}
