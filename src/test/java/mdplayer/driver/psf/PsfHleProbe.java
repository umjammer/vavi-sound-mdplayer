package mdplayer.driver.psf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import mdplayer.lib.psf.PsfEngine;
import mdplayer.lib.psf.PsfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * scratch: does a psf actually make a sound, and if not, where is its cpu stuck?
 * <p>
 * Run with {@code -Dvavi.test=ai} and either {@code -Dprobe.dir=<album>} for a peak level per
 * file, or {@code -Dprobe.file=<psf>} for one file's pc profile. A song that plays silence
 * usually sits on one address: a bare one is a wait loop in the driver, a BIOS vector
 * (0xa0/0xb0/0xc0) is a kernel call the HLE is not returning from.
 */
class PsfHleProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void peakPerFile() throws Exception {
        String dir = System.getProperty("probe.dir");
        if (dir == null) return;
        int seconds = Integer.getInteger("probe.seconds", 5);

        try (var files = Files.list(Path.of(dir))) {
            for (Path p : files.sorted().toList()) {
                String n = p.getFileName().toString().toLowerCase();
                if (!n.endsWith(".psf") && !n.endsWith(".minipsf")) continue;
                try {
                    PsfEngine engine = new PsfEngine();
                    engine.start(PsfFile.load(Files.readAllBytes(p), name -> null));
                    int peak = 0;
                    for (int f = 0; f < 60 * seconds; f++) {
                        for (int s = 0; s < 735; s++) {
                            engine.sample();
                            peak = Math.max(peak, Math.abs(engine.getLeft()));
                        }
                        engine.frame();
                    }
System.err.println("%6d  %s".formatted(peak, p.getFileName()));
                } catch (Exception e) {
System.err.println("  FAIL  %s: %s".formatted(p.getFileName(), e));
                }
            }
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void whereItSits() throws Exception {
        String file = System.getProperty("probe.file");
        if (file == null) return;
        int seconds = Integer.getInteger("probe.seconds", 5);

        PsfEngine engine = new PsfEngine();
        engine.start(PsfFile.load(Files.readAllBytes(Path.of(file)), name -> null));

        Map<Integer, Integer> pcs = new HashMap<>();
        int peak = 0;
        for (int f = 0; f < 60 * seconds; f++) {
            for (int s = 0; s < 735; s++) {
                engine.sample();
                peak = Math.max(peak, Math.abs(engine.getLeft()));
                if ((s & 63) == 0) {
                    int pc = engine.hw.cpu.pc;
                    // at a BIOS vector the address alone says nothing; the subcall is in t1
                    int key = pc == 0xa0 || pc == 0xb0 || pc == 0xc0
                            ? (pc << 16) | (engine.hw.cpu.r[9] & 0xff) : pc;
                    pcs.merge(key, 1, Integer::sum);
                }
            }
            engine.frame();
        }

        int voices = 0;
        for (int v = 0; v < engine.spu.voiceCount(); v++) {
            if (engine.spu.keyOnCount(v) > 0) voices++;
        }
System.err.println("peak=%d voices keyed=%d".formatted(peak, voices));
        pcs.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(8)
                .forEach(e -> System.err.println("  %08x  %d".formatted(e.getKey(), e.getValue())));
    }
}
