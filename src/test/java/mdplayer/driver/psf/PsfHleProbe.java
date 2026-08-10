package mdplayer.driver.psf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import mdplayer.lib.psf.Psf2Engine;
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

        try (var files = Files.walk(Path.of(dir))) {
            for (Path p : files.sorted().toList()) {
                String n = p.getFileName().toString().toLowerCase();
                boolean two = n.endsWith(".psf2") || n.endsWith(".minipsf2");
                if (!two && !n.endsWith(".psf") && !n.endsWith(".minipsf")) continue;
                try {
                    int peak = two ? peak2(p, seconds) : peak1(p, seconds);
System.err.printf("%6d  %s%n", peak, dir.length() < p.toString().length()
        ? p.toString().substring(dir.length()) : p.getFileName().toString());
                } catch (Throwable e) {
System.err.printf("  FAIL  %s: %s%n", p.getFileName(), e);
                }
            }
        }
    }

    private static int peak1(Path p, int seconds) throws Exception {
        PsfEngine engine = new PsfEngine();
        engine.start(PsfFile.load(Files.readAllBytes(p), name -> resolve(p, name)));
        int peak = 0;
        for (int f = 0; f < 60 * seconds; f++) {
            for (int s = 0; s < 735; s++) {
                engine.sample();
                peak = Math.max(peak, Math.abs(engine.getLeft()));
            }
            engine.frame();
        }
        return peak;
    }

    private static int peak2(Path p, int seconds) throws Exception {
        Psf2Engine engine = new Psf2Engine();
        engine.start(PsfFile.load(Files.readAllBytes(p), name -> resolve(p, name)));
        int peak = 0;
        for (int f = 0; f < 60 * seconds; f++) {
            for (int s = 0; s < 735; s++) {
                engine.sample();
                peak = Math.max(peak, Math.abs(engine.getLeft()));
            }
            engine.frame();
        }
        return peak;
    }

    /** a "_lib" sits beside the file that names it */
    private static byte[] resolve(Path psf, String name) {
        try {
            return Files.readAllBytes(psf.resolveSibling(name));
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void whereItSits() throws Exception {
        String file = System.getProperty("probe.file");
        if (file == null) return;
        int seconds = Integer.getInteger("probe.seconds", 5);

        boolean two = file.toLowerCase().endsWith("2");
        PsfEngine engine = two ? null : new PsfEngine();
        Psf2Engine engine2 = two ? new Psf2Engine() : null;
        Path path = Path.of(file);
        PsfFile[] files = PsfFile.load(Files.readAllBytes(path), name -> resolve(path, name));
        if (two) engine2.start(files); else engine.start(files);
        var hw = two ? engine2.hw : engine.hw;
        var spu = two ? (mdplayer.emu.psx.SpuVoices) engine2.spu : engine.spu;

        Map<Integer, Integer> pcs = new HashMap<>();
        int peak = 0;
        for (int f = 0; f < 60 * seconds; f++) {
            for (int s = 0; s < 735; s++) {
                if (two) engine2.sample(); else engine.sample();
                peak = Math.max(peak, Math.abs(two ? engine2.getLeft() : engine.getLeft()));
                if ((s & 63) == 0) {
                    int pc = hw.cpu.pc;
                    // at a BIOS vector the address alone says nothing; the subcall is in t1
                    int key = pc == 0xa0 || pc == 0xb0 || pc == 0xc0
                            ? (pc << 16) | (hw.cpu.r[9] & 0xff) : pc;
                    pcs.merge(key, 1, Integer::sum);
                }
            }
            if (two) engine2.frame(); else engine.frame();
        }

        int voices = 0;
        for (int v = 0; v < spu.voiceCount(); v++) {
            if (spu.keyOnCount(v) > 0) voices++;
        }
System.err.printf("peak=%d voices keyed=%d songDone=%s%n", peak, voices, hw.songDone);
        pcs.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(8)
                .forEach(e -> System.err.printf("  %08x  %d%n", e.getKey(), e.getValue()));
    }
}
