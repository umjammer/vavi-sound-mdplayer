/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * A song whose PCM lives in a .PVI hands FMP.COM a quarter of a million bytes to feed to the
 * OPNA one OUT instruction at a time, and all of that runs on the emulated 286 before a note is
 * heard. Formatting the emulator's per instruction TRACE lines used to cost more than executing
 * the instructions they describe, so starting such a song took over five seconds; it is now well
 * under one. This holds that line - see the note on {@code Nise286.tracing}.
 */
class FmpLoadSpeedTest {

    /** 240 KB of ADPCM in FMPVI_B.PVI, the worst case in the FMP sample disks */
    static final String testFile = "/Users/nsano/Public/np2/FMP/FMPD1/76THSTAR.OZI";

    static boolean testFileExists() {
        return Files.exists(Path.of(testFile));
    }

    @Test
    @EnabledIf("testFileExists")
    void testPviSongLoadsQuickly() throws Exception {
        byte[] data = Files.readAllBytes(Path.of(testFile));

        FMP fmp = new FMP();
        fmp.setSearchPath(Path.of(testFile).getParent().toString());
        fmp.sampleRate = 55467;
        fmp.charset = Charset.forName("MS932");
        fmp.dir = System.getProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        fmp.playingFileName = testFile;
        fmp.ft = new FileTemp();

        AtomicLong adpcmWrites = new AtomicLong();
        fmp.setPPZ8PCMFilename = (mode, fn) -> {};
        fmp.setPPZ8PCMData = (bank, mode, pcmData) -> {};
        fmp.setPPZ8Data = (port, adr, d) -> {};
        fmp.opnaWrite = (p, a, d) -> {
            if ((p & 0xff) == 0x8e && a == 0x08) adpcmWrites.incrementAndGet();
        };
        fmp.blockWrite = b -> {};

        long t0 = System.nanoTime();
        fmp.run(data);
        double sec = (System.nanoTime() - t0) / 1e9;
        System.out.printf("loaded in %.3f s, %d ADPCM bytes%n", sec, adpcmWrites.get());

        // the PVI really was fed to the chip - without that the timing below would prove nothing
        assertEquals(245079, adpcmWrites.get(), "ADPCM bytes written while loading");
        // generous next to the 0.2 s it now takes, so a loaded machine does not fail the build,
        // but far below the 5.5 s the eager TRACE formatting used to cost
        assertTrue(sec < 2.0, "PVI song load took %.3f s".formatted(sec));
    }
}
