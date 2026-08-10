/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.lib.psf.Psf2Engine;
import mdplayer.lib.psf.PsfFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Checks the PSF2 emulation against audio rendered by aosdk, which is what it was ported from.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class Psf2EngineTest {

    static final Path psf2 = Path.of("tmp/psf/01.psf2");
    static final Path reference = Path.of("tmp/psf/01.ref.wav");

    static final int SAMPLES_PER_FRAME = 44100 / 60;

    /** the samples live under tmp/, which is not in the repository - see driver/readme.md */
    @BeforeEach
    void checkSamples() {
        assumeTrue(Files.exists(psf2), "tmp/psf/01.psf2 is missing, see mdplayer/driver/readme.md");
    }

    @Test
    void tags() throws Exception {
        PsfFile file = PsfFile.decode(Files.readAllBytes(psf2));
        assertEquals(2, file.version);
System.err.println("title: " + file.tag("title") + ", game: " + file.tag("game")
        + ", length: " + file.tag("length") + ", reserved: " + file.reserved.length
        + ", program: " + file.program.length);
    }

    @Test
    void matchesAosdk() throws Exception {
        int frames = 300;

        short[] mine = render(frames);
        short[] theirs = PsfEngineTest.readWav(reference, frames * SAMPLES_PER_FRAME * 2);

        int diff = 0;
        int worst = 0;
        for (int i = 0; i < theirs.length; i++) {
            int d = Math.abs(mine[i] - theirs[i]);
            if (d != 0) diff++;
            worst = Math.max(worst, d);
        }
System.err.printf("samples: %d, differing: %d, worst: %d%n", theirs.length, diff, worst);

        int peak = 0;
        for (short s : mine) peak = Math.max(peak, Math.abs(s));
        assertTrue(peak > 1000, "rendered audio is silent, peak " + peak);

        assertEquals(0, diff, "the port should be sample exact against aosdk");
    }

    static short[] render(int frames) throws IOException {
        Psf2Engine engine = new Psf2Engine();
        engine.start(PsfFile.load(Files.readAllBytes(psf2), name -> null));

        short[] out = new short[frames * SAMPLES_PER_FRAME * 2];
        int p = 0;
        for (int f = 0; f < frames; f++) {
            for (int s = 0; s < SAMPLES_PER_FRAME; s++) {
                engine.sample();
                out[p++] = (short) engine.getLeft();
                out[p++] = (short) engine.getRight();
            }
            engine.frame();
        }
        return out;
    }
}
