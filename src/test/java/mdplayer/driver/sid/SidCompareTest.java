/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 */

package mdplayer.driver.sid;

import java.nio.file.Files;
import java.nio.file.Paths;

import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;

import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Compare audio output produced via the high-level {@link Sid} init path
 * (used by {@link SidMdDriver}) against the same setup used by
 * {@link SidTestProgram}. Both should produce identical audio.
 */
@Disabled("ai iteration")
public class SidCompareTest {

    private static final int SamplingRate = 44100;

    static void main(String[] args) throws Exception {
        String filename = args.length > 0
                ? args[0]
                : "/Users/nsano/src/java/JSIDPlay2/tmp/Formula_1_Simulator.sid";
        int song = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int buffers = args.length > 2 ? Integer.parseInt(args[2]) : 30;

        byte[] buf = Files.readAllBytes(Paths.get(filename));

        System.out.println("=== Path A: SidTestProgram-style ===");
        runA(buf, song, buffers, "/tmp/cmp_A.raw");

        System.out.println("=== Path B: Sid.init (used by SidMdDriver) ===");
        runB(buf, song, buffers, "/tmp/cmp_B.raw");

        diffWavs("/tmp/cmp_A.raw", "/tmp/cmp_B.raw");
    }

    /** Mirror of {@link SidTestProgram#init}. */
    static void runA(byte[] buf, int song, int buffers, String out) throws Exception {
        Setting setting = mock(Setting.class, Mockito.RETURNS_DEEP_STUBS);
        when(setting.getOutputDevice().getSampleRate()).thenReturn(SamplingRate);

        playSidFp engine = new playSidFp(SamplingRate);
        engine.debug(false, null);
        engine.setRoms(null, null, null);
        ReSidBuilder rs = new ReSidBuilder("ReSid", setting.getOutputDevice().getSampleRate());
        rs.create(3);
        SidTune tune = new SidTune(buf, buf.length);
        tune.selectSong(song);
        if (!engine.load(tune)) {
            throw new IllegalStateException(engine.error());
        }

        SidConfig cfg = new SidConfig(SamplingRate);
        cfg.frequency = SamplingRate;
        // Match Sid.init defaults (quality=1) to isolate the sampling method as the difference
        cfg.samplingMethod = SidConfig.SamplingMethod.INTERPOLATE;
        cfg.fastSampling = false;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.sidEmulation = rs;
        if (!engine.config(cfg)) {
            throw new IllegalStateException(engine.error());
        }

        renderToFile(engine, buffers, out, "A");
    }

    /** Mirror of {@link Sid#init} as used by {@link SidMdDriver}. */
    static void runB(byte[] buf, int song, int buffers, String out) throws Exception {
        Sid sid = new Sid();
        sid.song = song;
        sid.init(buf, null, null, null, 5000, SamplingRate, 1, 0, 0, false, false);
        renderToFile(sid.engine, buffers, out, "B");
    }

    static void renderToFile(playSidFp engine, int buffers, String out, String tag) throws Exception {
        // Audio.java calls render(buffer, 0, 4) repeatedly. Mimic that.
        int chunk = 4096; // Use larger chunk
        int totalShorts = buffers * 4096;
        short[] tmp = new short[chunk];
        try (java.io.DataOutputStream wav = new java.io.DataOutputStream(
                new java.io.BufferedOutputStream(new java.io.FileOutputStream(out)))) {
            int written = 0;
            int iters = 0;
            while (written < totalShorts) {
                if ("B".equals(tag)) {
                    engine.fastForward(100);
                }
                int produced = engine.play(tmp, chunk);
                for (int i = 0; i < produced; i++) {
                    short s = tmp[i];
                    wav.writeByte(s & 0xff);
                    wav.writeByte((s >> 8) & 0xff);
                }
                written += produced;
                iters++;
                if (iters % 5000 == 0) System.out.printf("  %s iter=%d written=%d%n", tag, iters, written);
            }
            System.out.printf("  %s total iters=%d written=%d%n", tag, iters, written);
        }
    }

    static void diffWavs(String a, String b) throws Exception {
        byte[] da = Files.readAllBytes(Paths.get(a));
        byte[] db = Files.readAllBytes(Paths.get(b));
        System.out.println();
        System.out.printf("A size=%d, B size=%d%n", da.length, db.length);
        int diffCount = 0;
        int firstDiff = -1;
        int n = Math.min(da.length, db.length);
        for (int i = 0; i < n; i++) {
            if (da[i] != db[i]) {
                diffCount++;
                if (firstDiff < 0) firstDiff = i;
            }
        }
        System.out.printf("byte diffs=%d/%d, first diff at byte %d%n", diffCount, n, firstDiff);
    }
}
