/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.fmp7;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import jdos.api.AudioSink;
import jdos.api.JDosBox;
import jdos.win.api.SharedMemory;
import mdplayer.lib.PcmQueue;

import static java.lang.System.getLogger;


/**
 * Plays an FMP7 song by running FMP7.exe - the real thing, built for Windows - on an emulated PC,
 * and taking what it writes to its DirectSound buffer.
 * <p>
 * <b>What paces it.</b> The emulated player keeps time the way it would on real hardware: it
 * writes into a sound buffer ahead of the play cursor and cannot get further ahead than the
 * buffer is long. So the emulated card has to take its samples at the rate they are meant to be
 * heard at, no faster - which is what happens when {@link PcmQueue#write} blocks until
 * {@link #read} has made room. The cushion between the two is therefore not ours to choose: it
 * is FMP7's own sound buffer, which on this machine is 400ms. There is room for it - FMP7
 * synthesizes about four times faster than it plays here - and the queue below only has to be
 * deep enough not to be the thing in the way.
 * <p>
 * <b>What it says it is playing.</b> FMP7 is closed source, but its api is published and it
 * keeps a shared memory - {@link Fmp7Work} - up to date as it plays. On a real PC another
 * process reads that; here the other process is this jvm, which is what
 * {@link SharedMemory} is. A reading is taken every time samples are handed over and kept with
 * the position in the audio they were handed over at, so that what the visualizer shows is what
 * was playing when the sound it is drawn against was made - see {@link #workAt}.
 * <p>
 * <b>Room.</b> FMP7 needs a machine with 64MB in it. On the default 16 it starts, plays, and
 * quietly never publishes its work: the allocation in front of that fails and it carries on
 * without it.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 * @see <a href="http://fmpdoc.fmp.jp/fmp7-api/">FMP7 公開API仕様</a>
 */
public class Fmp7Player {

    private static final Logger logger = getLogger(Fmp7Player.class.getName());

    /** where FMP7.exe and its dlls live */
    public static final String FMP7_PATH_KEY = "mdplayer.fmp7.path";

    /** how much memory the emulated PC gets, in megabytes - see the class comment */
    private static final String MEMORY = System.getProperty("mdplayer.fmp7.memory", "64");

    /** how much audio the queue can hold, which is the cushion the emulator gets to build */
    private static final double QUEUE_SECONDS = 3;

    /** how long FMP7 is given to produce a sound before we give up on it */
    private static final long SILENCE_LIMIT_SECONDS = 60;

    /**
     * How many readings of the work are kept.
     * <p>
     * They have to reach back at least as far as the queue does: a reading is chosen by where
     * the listener has got to, and the listener is a whole queue behind what the emulator has
     * made. Too few and the oldest one still held is newer than the one wanted, which is a
     * display running ahead of its own music by however much is missing.
     * <p>
     * One is taken per piece the emulated sound card is given, which is 2048 bytes - 512 frames,
     * or about 11ms.
     */
    private static final int SNAPSHOTS = (int) ((QUEUE_SECONDS + 1.5) * 48000 / 512);

    /**
     * How far ahead of the sound FMP7's work runs, at most [ms].
     * <p>
     * It is however much of its sound buffer FMP7 has filled but the card has not played, and it
     * is measured rather than assumed - see {@link #leadMillis}. The cap is what keeps a song
     * that opens with a silent bar from being measured as a lead of that whole bar.
     */
    private static final double MAX_LEAD_MILLIS = 500;

    /**
     * What FMP7 is told about itself before it starts, in the registry file jdosbox reads from
     * beside the program.
     * <p>
     * Every line of it buys back emulated time at the price of some quality: without it FMP7
     * needs nearly all of the machine and the sound breaks up, with it about three quarters of
     * what it has. A jdosbox.reg sitting in the user's own FMP7 directory is used instead, so
     * this is only the default.
     */
    private static final String REGISTRY = """
            Windows Registry Editor Version 5.00

            [HKEY_CURRENT_USER\\SOFTWARE\\Guu\\FMP7]
            ; the interpolation used when a pcm sample is resampled - 0 is the cheapest
            "ReSamplePower"=dword:00000000
            ; the rate it is resampled to: 0=44.1kHz 1=48kHz 2=88.2kHz 3=96kHz 4=176.4kHz 5=192kHz
            "ReSampleRate"=dword:00000001

            ; the emulation of each chip, oversampled or not. Off is where most of the time comes back.
            [HKEY_CURRENT_USER\\SOFTWARE\\Guu\\FMP7\\AddOn\\FMP7]
            "OverSampling"=dword:00000000

            [HKEY_CURRENT_USER\\SOFTWARE\\Guu\\FMP7\\AddOn\\FMP7\\PSG]
            "OverSampling"=dword:00000000

            [HKEY_CURRENT_USER\\SOFTWARE\\Guu\\FMP7\\AddOn\\FMP7\\PCM]
            "OverSampling"=dword:00000000
            """;

    private final byte[] song;

    private Path work;

    private JDosBox dosbox;

    private PcmQueue queue;

    private volatile int sampleRate = 48000;
    private volatile int channels = 2;
    private volatile int sampleSizeInBits = 16;

    /** set once FMP7 has produced a sample that is not silence */
    private volatile boolean sounding;

    /** set when FMP7 was given long enough to make a sound and never did */
    private volatile boolean gaveUp;

    /** frames handed to the queue, which is the numbering the readings are filed under */
    private volatile long producedFrames;

    /** how much digital silence has been dropped waiting for {@link #sounding} */
    private long droppedBytes;

    /** frames handed to the caller, which is the clock the display is shown against */
    private volatile long consumedFrames;

    /** readings of the work, oldest first once it has wrapped */
    private final Fmp7Work[] snapshots = new Fmp7Work[SNAPSHOTS];

    /** how many readings have been taken; the newest is at {@code (taken - 1) % SNAPSHOTS} */
    private volatile long taken;

    /** how far ahead of the sound the work was when the song started to sound [ms]; -1 until then */
    private volatile double leadMillis = -1;

    public Fmp7Player(byte[] song) {
        this.song = song;
    }

    /** where FMP7 is, as the system property says */
    public static File playerDirectory() {
        return new File(System.getProperty(FMP7_PATH_KEY, "/usr/local/src/FMP7"));
    }

    /** is there an FMP7 to run? */
    public static boolean isAvailable() {
        return new File(playerDirectory(), "FMP7.exe").exists();
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    /** has FMP7 made a sound yet? */
    public boolean isSounding() {
        return sounding;
    }

    /** Boots the machine and returns; the audio turns up at {@link #read} in its own time. */
    public void start() throws IOException {
        if (!isAvailable()) {
            throw new IOException("no FMP7.exe under " + playerDirectory()
                    + "; set -D" + FMP7_PATH_KEY + "=<dir>");
        }

        // the win32 layer only ever knows one drive (see Win#run, which mounts C and nothing
        // else), so FMP7 and the song have to sit on it together; its own directory is copied
        // rather than mounted so that nothing this writes lands in it
        work = Files.createTempDirectory("mdplayer-fmp7");
        Files.createDirectories(work.resolve("addon"));
        copy(playerDirectory().toPath(), work);
        Path addon = playerDirectory().toPath().resolve("addon");
        if (Files.isDirectory(addon)) {
            copy(addon, work.resolve("addon"));
        }
        if (!Files.exists(work.resolve("jdosbox.reg"))) {
            Files.writeString(work.resolve("jdosbox.reg"), REGISTRY, StandardCharsets.UTF_8);
        }
        Files.write(work.resolve("song.owi"), song);

        queue = new PcmQueue((int) (48000 * 2 * 2 * QUEUE_SECONDS));
        for (int i = 0; i < snapshots.length; i++) {
            snapshots[i] = new Fmp7Work(Fmp7Work.GLOBAL_SIZE, Fmp7Work.PART_SIZE);
        }

        dosbox = new JDosBox()
                .arg("-m", MEMORY)
                .mount('c', work.toFile())
                .command("c:")
                .command("FMP7.exe song.owi")
                // nothing on the machine's own mixer is wanted, and not opening a line for it
                // keeps it from fighting the player for the host's audio device
                .set("mixer", "nosound", "true")
                // FMP7 has a whole OPNA to synthesize in real time, so it is not going to spend
                // any of the machine emulating sound cards nothing will use
                .set("sblaster", "sbtype", "none")
                .set("gus", "gus", "false")
                .set("speaker", "pcspeaker", "false")
                .set("speaker", "tandy", "off")
                .set("speaker", "disney", "false")
                .set("joystick", "joysticktype", "none")
                .set("cpu", "cycles", "max")
                // dosbox's "max" holds the emulated cpu to about 90% of what the host will give
                // it and sleeps a millisecond whenever it finds itself ahead - the right thing
                // for a game, a 10% brake on a machine whose only job is to synthesize audio
                // faster than it is heard. Turbo takes that off; the queue below still paces it.
                .turbo(true)
                // there is no screen to draw, so this is emulation nobody is going to look at
                .set("render", "frameskip", "10")
                .exitWhenProgramFinishes(true)
                .directSoundSink(new Sink());
        dosbox.start();
    }

    /** FMP7's own files: the program, its dlls, its addon drivers and its settings */
    private static void copy(Path from, Path to) throws IOException {
        for (Path f : Files.newDirectoryStream(from)) {
            String name = f.getFileName().toString().toLowerCase();
            if (Files.isRegularFile(f)
                    && (name.endsWith(".exe") || name.endsWith(".dll") || name.equals("jdosbox.reg"))) {
                Files.copy(f, to.resolve(f.getFileName().toString()));
            }
        }
    }

    /** what FMP7 writes to its DirectSound buffer */
    private class Sink implements AudioSink {

        @Override
        public void open(int sampleRate, int sampleSizeInBits, int channels) {
            Fmp7Player.this.sampleRate = sampleRate;
            Fmp7Player.this.sampleSizeInBits = sampleSizeInBits;
            Fmp7Player.this.channels = channels;
logger.log(Level.DEBUG, "fmp7: dsound " + sampleRate + "Hz " + sampleSizeInBits + "bit " + channels + "ch");
        }

        @Override
        public void write(byte[] data, int offset, int length) {
            int frame = channels * (sampleSizeInBits / 8);

            if (!sounding) {
                int start = firstNonZero(data, offset, length);
                if (start < 0) {
                    // dropped, not queued: FMP7 spends its first seconds loading, and what it
                    // writes while it does is digital silence nobody wants to sit through
                    droppedBytes += length;
                    if (droppedBytes > (long) sampleRate * frame * SILENCE_LIMIT_SECONDS) {
logger.log(Level.WARNING, "fmp7: nothing but silence after " + SILENCE_LIMIT_SECONDS + "s, giving up");
                        gaveUp = true;
                        queue.finish();
                    }
                    return;
                }
                // align to a frame so the channels do not swap
                start -= (start - offset) % frame;
                length -= start - offset;
                offset = start;
                sounding = true;
logger.log(Level.DEBUG, "fmp7: sound starts, " + droppedBytes + " bytes of silence dropped");
            }

            // the reading is taken before the samples go in, so that it is the state of the
            // player at the moment it had made this much sound and no more. It is counted in
            // the frames that are queued and not the ones that were made: the silence dropped
            // in front of the song never reaches the caller, so counting it here would file
            // every reading that much too late.
            producedFrames += length / frame;
            snapshot();

            // blocking here is the point: it is the emulated sound card taking its samples at
            // the rate they are heard, which is what holds FMP7 to the song's own tempo
            queue.write(data, offset, length);
        }

        @Override
        public void close() {
logger.log(Level.DEBUG, "fmp7: dsound closed");
        }
    }

    /** Reads the work as it is now and files it under how much sound has been made so far. */
    private void snapshot() {
        Fmp7Work next = snapshots[(int) (taken % SNAPSHOTS)];
        if (SharedMemory.read(Fmp7Work.KEY_MAP, 0, next.buffer(), 0, next.size()) == 0) {
            return; // FMP7 has not published its work yet
        }
        next.atFrame = producedFrames;
        taken++;

        if (leadMillis < 0 && next.playing()) {
            // whatever FMP7 had already made and the card had not played is how far ahead of the
            // sound its work runs, and this is the moment that lead can be seen: the first sound
            // of the song is being handed over, and the player is that much further on
            leadMillis = Math.min(next.playMillis(), MAX_LEAD_MILLIS);
logger.log(Level.DEBUG, "fmp7: the work runs %.0fms ahead of the sound".formatted(leadMillis));
        }
    }

    private static int firstNonZero(byte[] b, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            if (b[i] != 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Takes the next samples FMP7 has made.
     *
     * @param timeoutMillis how long to wait for them before giving up and returning short
     * @return bytes read; 0 means either the song is over ({@link #isFinished}) or the emulator
     *         did not keep up
     */
    public int read(byte[] b, int offset, int length, long timeoutMillis) {
        if (queue == null) {
            return 0;
        }
        int n = queue.read(b, offset, length, timeoutMillis);
        consumedFrames += n / (channels * (sampleSizeInBits / 8));
        if (n == 0 && dosbox != null && !dosbox.isRunning()) {
            queue.finish();
        }
        return n;
    }

    /** frames handed over so far, which is what the listener has heard bar the output buffer */
    public long getConsumedFrames() {
        return consumedFrames;
    }

    /**
     * Copies into {@code out} what FMP7 was playing when the sound {@code framesBack} behind the
     * last frame handed over was made.
     * <p>
     * Two things sit between the two: the queue, which is however far the emulator has run ahead
     * of the listener, and FMP7's own sound buffer, which is how far its player has run ahead of
     * its sound card. The first is exact - every reading is filed under the frame it was taken
     * at - and the second is {@link #leadMillis}.
     *
     * @return false while FMP7 has published nothing to read
     */
    public boolean workAt(long framesBack, Fmp7Work out) {
        long taken = this.taken;
        if (taken == 0) {
            return false;
        }
        double lead = leadMillis > 0 ? leadMillis : 0;
        long want = consumedFrames - framesBack - (long) (lead * sampleRate / 1000);

        // the readings are in order, so the one wanted is the last one taken at or before it
        long first = Math.max(0, taken - SNAPSHOTS);
        Fmp7Work found = snapshots[(int) ((taken - 1) % SNAPSHOTS)];
        for (long i = taken - 1; i >= first; i--) {
            Fmp7Work each = snapshots[(int) (i % SNAPSHOTS)];
            found = each;
            if (each.atFrame <= want) {
                break;
            }
        }
        out.copyFrom(found);
        return true;
    }

    /** what FMP7 has been up to, for a log line or a test */
    public String getStatistics() {
        int frameSize = channels * (sampleSizeInBits / 8);
        return "%.1fs produced, %.1fs of it the silence it starts with, %d readings".formatted(
                producedFrames / (double) sampleRate,
                droppedBytes / (double) (sampleRate * frameSize), taken);
    }

    /** how much audio is queued ahead of the listener - the cushion, in seconds */
    public double getCushionSeconds() {
        if (queue == null) {
            return 0;
        }
        return queue.available() / (double) (sampleRate * channels * (sampleSizeInBits / 8));
    }

    /** what the machine died of, when it did not end on its own terms */
    public Throwable getFailure() {
        return dosbox == null ? null : dosbox.getFailure();
    }

    /** is the song over - the machine gone and the queue dry, or never a sound out of it? */
    public boolean isFinished() {
        return dosbox == null || gaveUp || (!dosbox.isRunning() && (queue == null || queue.available() == 0));
    }

    /** Shuts the machine down and takes the song's scratch directory with it. */
    public void stop() {
logger.log(Level.DEBUG, "fmp7: " + getStatistics());
        if (queue != null) {
            queue.close();
        }
        if (dosbox != null) {
            dosbox.stop();
            dosbox = null;
        }
        if (work != null) {
            try (Stream<Path> walk = Files.walk(work)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
logger.log(Level.DEBUG, "leaving " + p + ": " + e.getMessage());
                    }
                });
            } catch (IOException e) {
logger.log(Level.DEBUG, "leaving " + work + ": " + e.getMessage());
            }
            work = null;
        }
    }
}
