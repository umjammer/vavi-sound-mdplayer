/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.smaf;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import jdos.api.AudioSink;
import jdos.api.JDosBox;

import static java.lang.System.getLogger;


/**
 * Plays a SMAF file by running mmftoolc.exe - a real MA-2/3/5 player, built for Windows - on an
 * emulated PC, and taking what it writes to its {@code waveOut} device.
 * <p>
 * <b>What paces it.</b> The emulated player keeps time the way it would on real hardware: it
 * hands the sound card a buffer, and cannot hand it the next one until the card has played the
 * last. So the emulated card has to take its samples at the rate they are meant to be heard at,
 * no faster - which is exactly what happens when {@link #write} blocks until {@link #read} has
 * made room. Let it take them as fast as the emulator can make them and the player's own sense
 * of time goes with it: the song comes out at the wrong speed and ends in the wrong place.
 * <p>
 * The queue between the two is what makes that safe to rely on. The emulated player fills it
 * during the seconds it spends loading its instrument tables and then stays that far ahead, so a
 * moment where the emulation cannot keep up is taken out of the cushion rather than heard.
 * <p>
 * Those loading seconds are digital silence, dropped rather than queued - jdosbox drops them at
 * the emulated device now, and this drops whatever still gets through - so they cost nothing to
 * sit through. What is queued is real audio, which is the point: the cushion has to be built out
 * of the song, not out of the silence in front of it.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-10 nsano initial version <br>
 */
public class MmfToolPlayer {

    private static final Logger logger = getLogger(MmfToolPlayer.class.getName());

    /** where mmftoolc.exe and its M5_Emu*.dll live */
    public static final String MMFTOOL_PATH_KEY = "mdplayer.smaf.mmftool";

    /** how much audio the queue holds - the cushion the emulator keeps ahead of the listener */
    private static final double QUEUE_SECONDS =
            Double.parseDouble(System.getProperty("mdplayer.smaf.queue", "20"));

    /** how long the emulated player is given to produce a sound before we give up on it */
    private static final long SILENCE_LIMIT_SECONDS = 120;

    /**
     * How much of the cushion has to be there before the first sample is handed over.
     * <p>
     * A song is not evenly hard to synthesize - a heavy passage half way through will drain a
     * cushion that the opening bars said was plenty - and whatever the emulated PC cannot make
     * up as it goes it has to have in hand before it starts. So the cushion is not a fixed size:
     * while the player is loading, the rate it produces audio at is measured, and what it would
     * be short by over a song's length is what gets built up. A machine that keeps up - which is
     * the normal case since jdosbox's dispatch loop was cleaned out, an MA-5 song running at
     * about 1.14 of real time on openjdk - waits only this floor; one that does not waits in
     * silence rather than stuttering its way through the song. Both wait about the same length
     * of time in total, since the emulator is what everything is waiting for - the difference is
     * only whether that time is spent before the music or during it.
     * <p>
     * Set {@code mdplayer.smaf.prime} to a number of seconds to fix it instead.
     */
    private static final double MIN_PRIME_SECONDS = 2;

    /** the length of song the cushion is built to cover, when the emulator is running short */
    private static final double ASSUMED_SONG_SECONDS =
            Double.parseDouble(System.getProperty("mdplayer.smaf.song", "60"));

    /** a fixed cushion in seconds, or 0 to work it out from how the emulator is doing */
    private static final double FIXED_PRIME_SECONDS =
            Double.parseDouble(System.getProperty("mdplayer.smaf.prime", "0"));

    /** how long to watch the emulator before believing what it says about its own speed */
    private static final long RATE_WINDOW_MILLIS = 1500;

    /**
     * How far ahead of real time the player has to be running before its cushion is allowed to
     * be a small one. A song is not evenly hard to synthesize - a heavy passage half way through
     * will drain a cushion that the opening bars said was plenty - so merely keeping up is not
     * enough to start on. The margin is small because a machine that is ahead at all recovers
     * whatever a heavy passage takes out of it; the measured worst case is a fifth of a second
     * lost per ten seconds of hard synthesis.
     */
    private static final double COMFORTABLE_RATE = 1.02;

    /**
     * How fast the emulated PC is. The emulated player has to synthesize its audio faster than
     * it is heard or the cushion never fills, and "max" is what DOSBox calls giving it as much
     * of the host as it will take.
     */
    private static final String CYCLES = System.getProperty("mdplayer.smaf.cycles", "max");

    /**
     * The emulated player's own master volume, 0 to 127, which it reads from its environment.
     * Its top setting clips on loud songs, so this leaves a little room.
     */
    private static final String VOLUME = System.getProperty("mdplayer.smaf.volume", "100");

    private final byte[] mmf;

    private Path work;

    private JDosBox dosbox;

    private PcmQueue queue;

    /** when the emulated player first made a sound, from which its rate is measured */
    private long soundingAtMillis;

    /** frames it had produced by then */
    private long soundingAtFrames;

    private volatile int sampleRate = 44100;
    private volatile int channels = 2;
    private volatile int sampleSizeInBits = 16;

    /** set once the emulated player has produced a sample that is not silence */
    private volatile boolean sounding;

    /** set when the emulated player was given long enough to make a sound and never did */
    private volatile boolean gaveUp;

    /** set once the cushion has been built and the song may start */
    private volatile boolean primed;

    /** every frame the emulated player has produced, silence and all */
    private volatile long producedFrames;

    /** how much digital silence has been dropped waiting for {@link #sounding} */
    private long droppedBytes;

    public MmfToolPlayer(byte[] mmf) {
        this.mmf = mmf;
    }

    /** where mmftoolc.exe is, as the system property says */
    public static File toolDirectory() {
        return new File(System.getProperty(MMFTOOL_PATH_KEY, "/usr/local/src/mmftool"));
    }

    /** what the emulated player needs on its drive: itself, its dlls and its voice table */
    private static boolean isToolFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return Files.isRegularFile(path)
                && (name.equals("mmftoolc.exe") || name.endsWith(".dll") || name.endsWith(".inf") || name.endsWith(".vm3"));
    }

    /** is there an mmftool to run? */
    public static boolean isAvailable() {
        return new File(toolDirectory(), "mmftoolc.exe").exists();
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    /** has the emulated player made a sound yet? */
    public boolean isSounding() {
        return sounding;
    }

    /**
     * The rate the emulated player is producing audio at, against real time. Below 1 it is not
     * keeping up, and the difference has to come out of the cushion.
     *
     * @return 1 until there has been long enough to tell
     */
    private double producedRate() {
        if (soundingAtMillis == 0) {
            return 1;
        }
        long elapsed = System.currentTimeMillis() - soundingAtMillis;
        if (elapsed < RATE_WINDOW_MILLIS) {
            return 1;
        }
        double seconds = (producedFrames - soundingAtFrames) / (double) sampleRate;
        return seconds / (elapsed / 1000.0);
    }

    /** how much audio has to be in hand before the song starts */
    private int primeBytes() {
        int frameSize = channels * (sampleSizeInBits / 8);
        double seconds;
        if (FIXED_PRIME_SECONDS > 0) {
            seconds = FIXED_PRIME_SECONDS;
        } else {
            // what the player will fall behind by over a song, plus a little to be going on with
            seconds = MIN_PRIME_SECONDS
                    + Math.max(0, COMFORTABLE_RATE - producedRate()) * ASSUMED_SONG_SECONDS;
        }
        seconds = Math.min(seconds, QUEUE_SECONDS * 0.9);
        return (int) (seconds * sampleRate * frameSize);
    }

    /** how much audio is queued ahead of the listener - the cushion, in seconds */
    public double getCushionSeconds() {
        if (queue == null) {
            return 0;
        }
        return queue.available() / (double) (sampleRate * channels * (sampleSizeInBits / 8));
    }

    /** what the emulated player has been up to, for a log line or a test */
    public String getStatistics() {
        int frameSize = channels * (sampleSizeInBits / 8);
        return "%.1fs produced, %.1fs of it the silence it starts with".formatted(
                producedFrames / (double) sampleRate,
                droppedBytes / (double) (sampleRate * frameSize));
    }

    /** Boots the machine and returns; the audio turns up at {@link #read} in its own time. */
    public void start() throws IOException {
        if (!isAvailable()) {
            throw new IOException("no mmftoolc.exe under " + toolDirectory()
                    + "; set -D" + MMFTOOL_PATH_KEY + "=<dir>");
        }

        // the win32 layer only ever knows one drive (see Win#run, which mounts C and nothing
        // else), so the tool and the song have to sit on it together; the tool's own directory
        // is copied rather than mounted so that nothing this writes lands in it
        work = Files.createTempDirectory("mdplayer-smaf");
        for (Path f : Files.newDirectoryStream(toolDirectory().toPath(), MmfToolPlayer::isToolFile)) {
            Files.copy(f, work.resolve(f.getFileName().toString()));
        }
        Files.write(work.resolve("song.mmf"), mmf);

        queue = new PcmQueue((int) (48000 * 2 * 2 * QUEUE_SECONDS));

        dosbox = new JDosBox()
                .mount('c', work.toFile())
                .command("c:")
                .command("set MMFTOOL_MASTER_VOLUME=" + VOLUME)
                .command("mmftoolc.exe song.mmf")
                // nothing on the machine's own mixer is wanted, and not opening a line for it
                // keeps it from fighting the player for the host's audio device
                .set("mixer", "nosound", "true")
                // the emulated PC is barely fast enough to synthesize MA-5 in real time, so it
                // is not going to spend any of itself emulating sound cards nothing will use
                .set("sblaster", "sbtype", "none")
                .set("gus", "gus", "false")
                .set("speaker", "pcspeaker", "false")
                .set("speaker", "tandy", "off")
                .set("speaker", "disney", "false")
                .set("joystick", "joysticktype", "none")
                .set("compiler", "threshold", System.getProperty("mdplayer.smaf.threshold", "1000"))
                .set("cpu", "cycles", CYCLES)
                // there is no screen to draw, so this is emulation nobody is going to look at
                .set("render", "frameskip", "10")
                .exitWhenProgramFinishes(true)
                .waveOutSink(new Sink());
        dosbox.start();
    }

    /** what the emulated player writes to its waveOut device */
    private class Sink implements AudioSink {

        @Override
        public void open(int sampleRate, int sampleSizeInBits, int channels) {
            MmfToolPlayer.this.sampleRate = sampleRate;
            MmfToolPlayer.this.sampleSizeInBits = sampleSizeInBits;
            MmfToolPlayer.this.channels = channels;
logger.log(Level.DEBUG, "smaf: waveOut " + sampleRate + "Hz " + sampleSizeInBits + "bit " + channels + "ch");
        }

        @Override
        public void write(byte[] data, int offset, int length) {
            producedFrames += length / (channels * (sampleSizeInBits / 8));

            if (!sounding) {
                int start = firstNonZero(data, offset, length);
                if (start < 0) {
                    // dropped, not queued: silence would otherwise fill the cushion that is
                    // meant to carry the start of the song. jdosbox drops it first these days,
                    // so little reaches here - but a sink is not promised trimmed audio, and
                    // this is also what notices a player that never sounds at all
                    droppedBytes += length;
                    if (droppedBytes > (long) sampleRate * channels * (sampleSizeInBits / 8) * SILENCE_LIMIT_SECONDS) {
logger.log(Level.WARNING, "smaf: nothing but silence after " + SILENCE_LIMIT_SECONDS + "s, giving up");
                        gaveUp = true;
                        queue.finish();
                    }
                    return;
                }
                // align to a frame so the channels do not swap
                int frame = channels * (sampleSizeInBits / 8);
                start -= (start - offset) % frame;
                sounding = true;
                soundingAtMillis = System.currentTimeMillis();
                soundingAtFrames = producedFrames;
logger.log(Level.DEBUG, "smaf: sound starts, " + droppedBytes + " bytes of silence dropped");
                length -= start - offset;
                offset = start;
            }

            // blocking here is the point: it is the emulated sound card taking its samples at
            // the rate they are heard, which is what holds the player to the song's own tempo
            queue.write(data, offset, length);
        }

        @Override
        public void close() {
            // just this waveOut device closing - the player opens one to warm up and another to
            // play, so this is not the end of the song. What ends it is the machine finishing.
logger.log(Level.DEBUG, "smaf: waveOut closed");
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
     * Takes the next samples the emulated player has made.
     *
     * @param timeoutMillis how long to wait for them before giving up and returning short
     * @return bytes read; 0 means either the song is over ({@link #isFinished}) or the emulator
     *         did not keep up
     */
    public int read(byte[] b, int offset, int length, long timeoutMillis) {
        if (queue == null) {
            return 0;
        }
        if (!primed) {
            // nothing is handed over until the cushion is built, or until it becomes clear that
            // there will never be that much - a song shorter than the cushion, or a player that
            // has finished or failed
            if (!queue.awaitAtLeast(primeBytes(), timeoutMillis)
                    && !queue.isDrained() && (dosbox == null || dosbox.isRunning())) {
                return 0;
            }
            primed = true;
logger.log(Level.DEBUG, "smaf: primed with %.1fs, the player is running at %.2f of real time"
        .formatted(getCushionSeconds(), producedRate()));
        }
        int n = queue.read(b, offset, length, timeoutMillis);
        if (n == 0 && dosbox != null && !dosbox.isRunning()) {
            queue.finish();
        }
        return n;
    }

    /** is the song over - the machine gone and the queue dry, or never a sound out of it? */
    public boolean isFinished() {
        return dosbox == null || gaveUp || (!dosbox.isRunning() && (queue == null || queue.available() == 0));
    }

    /** Shuts the machine down and takes the song's scratch directory with it. */
    public void stop() {
logger.log(Level.DEBUG, "smaf: " + getStatistics());
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
