/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 */

package mdplayer.driver.sid;

import mdplayer.lib.sid.libsidplayfp.builders.resid_builder.resid.Sid;
import mdplayer.lib.sid.libsidplayfp.builders.resid_builder.resid.Voice;


/**
 * Compare three voices given identical inputs. If voice 0 sounds different
 * from voice 1/2 at the bit level, the bug is voice-0-specific (sync, ring,
 * or per-voice init). Otherwise the bug is upstream.
 */
public class VoiceCompareTest {

    static void main(String[] args) throws Exception {
        Sid sid = new Sid(44100);

        // Configure all three voices identically: triangle waveform, gate on, freq=0x1000
        // Voice 0: regs 0x00..0x06   Voice 1: 0x07..0x0d   Voice 2: 0x0e..0x14
        for (int v = 0; v < 3; v++) {
            int base = v * 7;
            sid.write(base + 0, 0x00); // FREQ_LO
            sid.write(base + 1, 0x10); // FREQ_HI -> freq=0x1000
            sid.write(base + 2, 0x00); // PW_LO
            sid.write(base + 3, 0x08); // PW_HI -> pw=0x800
            sid.write(base + 5, 0x00); // ATK=0, DECAY=0
            sid.write(base + 6, 0xf0); // SUS=15, REL=0  (max sustain, fast release)
            sid.write(base + 4, 0x11); // triangle (bit 4) + gate (bit 0)
        }
        sid.write(0x18, 0x0f); // volume max, no filter

        Voice[] voices = getVoices(sid);

        // Run the SID for some cycles to let envelope attack.
        // Sample every N cycles and dump per-voice wave output.
        System.out.println("# After init, run cycles and dump every N");
        System.out.println("cycle | v0_acc v0_wout | v1_acc v1_wout | v2_acc v2_wout | v0_synSrc v1_synSrc v2_synSrc");
        for (int outer = 0; outer < 20; outer++) {
            for (int inner = 0; inner < 100; inner++) {
                sid.clock();
            }
            int cycle = (outer + 1) * 100;
            short v0 = voices[0].wave.output();
            short v1 = voices[1].wave.output();
            short v2 = voices[2].wave.output();
            // Check sync sources
            int v0Src = identityHash(getSyncSource(voices[0]));
            int v1Src = identityHash(getSyncSource(voices[1]));
            int v2Src = identityHash(getSyncSource(voices[2]));
            int v0Self = identityHash(voices[0].wave);
            int v1Self = identityHash(voices[1].wave);
            int v2Self = identityHash(voices[2].wave);
            System.out.printf(
                    "%5d | acc=%06x w=%04x | acc=%06x w=%04x | acc=%06x w=%04x | v0src=%s v1src=%s v2src=%s%n",
                    cycle,
                    voices[0].wave.accumulator, v0 & 0xffff,
                    voices[1].wave.accumulator, v1 & 0xffff,
                    voices[2].wave.accumulator, v2 & 0xffff,
                    v0Src == v2Self ? "v2" : v0Src == v1Self ? "v1" : v0Src == v0Self ? "v0(self)" : "?",
                    v1Src == v0Self ? "v0" : v1Src == v2Self ? "v2" : v1Src == v1Self ? "v1(self)" : "?",
                    v2Src == v1Self ? "v1" : v2Src == v0Self ? "v0" : v2Src == v2Self ? "v2(self)" : "?");
        }
    }

    private static int identityHash(Object o) {
        return System.identityHashCode(o);
    }

    private static Voice[] getVoices(Sid sid) throws Exception {
        java.lang.reflect.Field f = Sid.class.getDeclaredField("voice");
        f.setAccessible(true);
        return (Voice[]) f.get(sid);
    }

    private static Object getSyncSource(Voice v) throws Exception {
        java.lang.reflect.Field f = v.wave.getClass().getDeclaredField("syncSource");
        f.setAccessible(true);
        return f.get(v.wave);
    }
}
