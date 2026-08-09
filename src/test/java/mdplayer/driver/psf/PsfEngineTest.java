/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.lib.psf.PsfEngine;
import mdplayer.lib.psf.PsfFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Checks the PSF1 emulation against audio rendered by aosdk, which is what it was ported from.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class PsfEngineTest {

    static final Path psf = Path.of("tmp/psf/pe.psf");
    static final Path reference = Path.of("tmp/psf/pe.ref.wav");

    /** one video frame of samples, which is the granularity aosdk ticks the hardware at */
    static final int SAMPLES_PER_FRAME = 44100 / 60;

    /** the samples live under tmp/, which is not in the repository - see driver/readme.md */
    @BeforeEach
    void checkSamples() {
        assumeTrue(Files.exists(psf), "tmp/psf/pe.psf is missing, see mdplayer/driver/readme.md");
    }

    @Test
    void tags() throws Exception {
        PsfFile file = PsfFile.decode(Files.readAllBytes(psf));
        assertEquals(1, file.version);
        assertNotNull(file.tag("title"));
System.err.println("title: " + file.tag("title") + ", game: " + file.tag("game")
        + ", length: " + file.tag("length") + ", fade: " + file.tag("fade"));
    }

    @Test
    void matchesAosdk() throws Exception {
        int frames = 300;

        short[] mine = render(frames);
        short[] theirs = readWav(reference, frames * SAMPLES_PER_FRAME * 2);

        int diff = 0;
        int worst = 0;
        for (int i = 0; i < theirs.length; i++) {
            int d = Math.abs(mine[i] - theirs[i]);
            if (d != 0) diff++;
            worst = Math.max(worst, d);
        }
System.err.println("samples: %d, differing: %d, worst: %d".formatted(theirs.length, diff, worst));

        int peak = 0;
        for (short s : mine) peak = Math.max(peak, Math.abs(s));
        assertTrue(peak > 1000, "rendered audio is silent, peak " + peak);

        assertEquals(0, diff, "the port should be sample exact against aosdk");
    }

    static short[] render(int frames) throws IOException {
        PsfEngine engine = new PsfEngine();
        engine.start(PsfFile.load(Files.readAllBytes(psf), name -> null));

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

    static short[] readWav(Path path, int count) throws IOException {
        byte[] b = Files.readAllBytes(path);
        int data = 44; // the header this file was written with
        short[] out = new short[count];
        for (int i = 0; i < count; i++) {
            out[i] = (short) ((b[data + i * 2] & 0xff) | (b[data + i * 2 + 1] << 8));
        }
        return out;
    }
}
