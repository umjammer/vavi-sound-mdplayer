/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ay;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import mdplayer.lib.ay.AY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** Does the AY register stream of a song repeat, and after how long? Not a regression test. */
class AyLoopProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void dump() throws Exception {
        String file = System.getProperty("diag.file");
        int songNo = Integer.parseInt(System.getProperty("diag.song", "0"));
        int seconds = Integer.parseInt(System.getProperty("diag.limit", "180"));

        byte[] buf = Files.readAllBytes(Path.of(file));

        AY ay = new AY();
        ay.setZxClock();
        ay.setSampleRate(44100);
        ay.run(buf);

        int frames = seconds * 50;
        List<List<Integer>> perFrame = new ArrayList<>();
        for (int i = 0; i <= frames; i++) perFrame.add(new ArrayList<>());
        int[] beeps = new int[frames + 1];
        ay.setup(songNo,
                (r, d) -> { int f = (int) Math.min(ay.frames, frames); perFrame.get(f).add((r << 8) | d); },
                () -> { int f = (int) Math.min(ay.frames, frames); beeps[f]++; });

        java.lang.reflect.Field f = AY.class.getDeclaredField("z80");
        f.setAccessible(true);
        konamiman.z80.Z80Processor z80 = (konamiman.z80.Z80Processor) f.get(ay);
        java.util.Map<Integer, Integer> pcs = new java.util.TreeMap<>();
        long next = 0;
        while (ay.frames < frames) {
            ay.oneFrame();
            if (ay.frames >= next) {
                next = ay.frames + 10;
                pcs.merge(z80.getRegisters().getPC() & 0xffff, 1, Integer::sum);
            }
        }
        System.err.printf("state=%s stop=%s halted=%s SP=%04x IM=%d%n",
                z80.getState(), z80.getStopReason(), z80.isHalted(),
                z80.getRegisters().getSP() & 0xffff, z80.getInterruptMode());
        System.err.println("PC seen: " + pcs.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue()).limit(8)
                .map(e -> "%04x:%d".formatted(e.getKey(), e.getValue())).toList());

        int[] hash = new int[frames];
        int writes = 0;
        for (int i = 0; i < frames; i++) {
            int h = beeps[i] * 31;
            for (int w : perFrame.get(i)) h = h * 31 + w;
            hash[i] = h;
            writes += perFrame.get(i).size();
        }
        int beepCount = 0;
        for (int i = 0; i < frames; i++) beepCount += beeps[i];
        System.err.printf("frames=%d writes=%d (%.0f/s) beeps=%d%n", frames, writes, writes / (double) seconds, beepCount);

        // the smallest period the second half of the recording repeats at
        int found = -1;
        for (int p = 1; p < frames / 2 && found < 0; p++) {
            boolean ok = true;
            for (int i = frames / 2; i + p < frames && ok; i++) if (hash[i] != hash[i + p]) ok = false;
            if (ok) found = p;
        }
        System.err.printf("period=%s%n", found < 0 ? "none" : "%d frames (%.1fs)".formatted(found, found / 50.0));

        // where the stream first settles into that period
        if (found > 0) {
            int start = 0;
            for (int i = frames - found - 1; i >= 0; i--) {
                if (hash[i] != hash[i + found]) { start = i + 1; break; }
            }
            System.err.printf("loop starts at frame %d (%.1fs)%n", start, start / 50.0);
        }
    }
}
