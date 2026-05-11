/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 */

package mdplayer.driver.sid;

import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid.Sid;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid.Voice;


/**
 * Verify that writing each SID register hits the expected voice's field
 * and only that voice.
 */
public class RegisterDispatchTest {

    public static void main(String[] args) throws Exception {
        Sid sid = new Sid(44100);
        Voice[] voices = getVoices(sid);

        // Snapshot before
        int[] freqBefore = {voices[0].wave.freq, voices[1].wave.freq, voices[2].wave.freq};
        int[] pwBefore   = {voices[0].wave.pw,   voices[1].wave.pw,   voices[2].wave.pw};

        // Write distinct values to each voice's FREQ_LO
        sid.write(0x00, 0xaa); // voice 0 FREQ_LO
        sid.write(0x07, 0xbb); // voice 1 FREQ_LO
        sid.write(0x0e, 0xcc); // voice 2 FREQ_LO

        System.out.println("After FREQ_LO writes (0xaa, 0xbb, 0xcc):");
        System.out.printf("  v0.freq = 0x%06x  (expect low byte 0xaa)%n", voices[0].wave.freq);
        System.out.printf("  v1.freq = 0x%06x  (expect low byte 0xbb)%n", voices[1].wave.freq);
        System.out.printf("  v2.freq = 0x%06x  (expect low byte 0xcc)%n", voices[2].wave.freq);

        // Write FREQ_HI
        sid.write(0x01, 0x12); // voice 0 FREQ_HI
        sid.write(0x08, 0x34); // voice 1 FREQ_HI
        sid.write(0x0f, 0x56); // voice 2 FREQ_HI

        System.out.println("After FREQ_HI writes (0x12, 0x34, 0x56):");
        System.out.printf("  v0.freq = 0x%06x  (expect 0x12aa)%n", voices[0].wave.freq);
        System.out.printf("  v1.freq = 0x%06x  (expect 0x34bb)%n", voices[1].wave.freq);
        System.out.printf("  v2.freq = 0x%06x  (expect 0x56cc)%n", voices[2].wave.freq);

        // Write PW
        sid.write(0x02, 0x11); // voice 0 PW_LO
        sid.write(0x03, 0x02); // voice 0 PW_HI
        sid.write(0x09, 0x22); // voice 1 PW_LO
        sid.write(0x0a, 0x03); // voice 1 PW_HI
        sid.write(0x10, 0x33); // voice 2 PW_LO
        sid.write(0x11, 0x04); // voice 2 PW_HI

        System.out.println("After PW writes:");
        System.out.printf("  v0.pw = 0x%03x  (expect 0x211)%n", voices[0].wave.pw);
        System.out.printf("  v1.pw = 0x%03x  (expect 0x322)%n", voices[1].wave.pw);
        System.out.printf("  v2.pw = 0x%03x  (expect 0x433)%n", voices[2].wave.pw);
    }

    private static Voice[] getVoices(Sid sid) throws Exception {
        java.lang.reflect.Field f = Sid.class.getDeclaredField("voice");
        f.setAccessible(true);
        return (Voice[]) f.get(sid);
    }
}
