/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.smaf;

import java.io.ByteArrayOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jdos.api.StdioSink;

import static java.lang.System.getLogger;


/**
 * What the emulated player says about the song it is playing, and how that is put in step with
 * the sound of it.
 * <p>
 * mmftool prints two things (its {@code telemetry.c}): the whole score, once, before it starts -
 * every note with the length it is held for, and the controls that decide how loud it is and
 * where - and then, while it plays, nothing but the position it has reached. The first is the
 * only account of the music there is, the Yamaha dll inside the emulated PC sequencing the file
 * itself and answering nothing. The second is what this class is really for.
 * <p>
 * <b>Why a position at all.</b> The emulated PC runs seconds ahead of what is being heard - the
 * queue between them is deliberately deep, so that a hard passage is taken out of the cushion
 * rather than out of the music - so what it says is happening now will not be heard for seconds.
 * Every line it prints is therefore stamped, on its way out of the emulator, with how much audio
 * the guest had produced when it wrote it. A position and a stamp together say "song time P is
 * output frame F", and one subtraction turns that into the frame the song starts at. After that
 * the audio keeps the time by itself: the frames the player has handed over are the clock, and it
 * is the same clock the listener is on.
 * <p>
 * The offset is measured rather than assumed because it cannot be worked out from the sound. The
 * silence a player writes while it loads and the silence a song opens on look the same from this
 * end, and both jdosbox and {@link MmfToolPlayer} drop what they take for the first kind - a song
 * whose first note is two seconds in would otherwise start two seconds early on the display.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-11 nsano initial version <br>
 */
public class SmafTelemetry implements StdioSink {

    private static final Logger logger = getLogger(SmafTelemetry.class.getName());

    /** every line of this is telemetry; everything else the player prints is a message */
    private static final String MARK = "#MT";

    /**
     * How many beacons the offset is taken over. Each is one reading of a clock the guest keeps
     * against one this counts, and neither is exact to the sample: the player's own position is
     * only asked for every so often, and the audio it has produced sits in buffers of its own. A
     * few seconds of them, taken at the middle, is steadier than any one of them.
     */
    private static final int BEACONS = 64;

    /** a line's worth of what the guest has written, until the newline that ends it */
    private final ByteArrayOutputStream line = new ByteArrayOutputStream(256);

    private final SmafScore score = new SmafScore();

    /** offsets one beacon each: the output frame each said the song began at */
    private final List<Long> anchors = new ArrayList<>();

    /** where the song begins in the player's own output, in frames; unset while empty */
    private volatile double anchorFrames = Double.NaN;

    /** what the audio is being produced at, which turns a position in ms into frames */
    private volatile int sampleRate = 48000;

    /** frames the player dropped as the silence in front of the song, which are in no stream */
    private volatile long droppedFrames;

    /** set when the whole score has been read */
    private volatile boolean ready;

    /** set when the player said it had started, and what it had produced by then */
    private volatile long startedAt = -1;

    /** set when the player said the song was over */
    private volatile boolean finished;

    public SmafScore getScore() {
        return score;
    }

    /** has the score arrived? Nothing can be shown until it has */
    public boolean isReady() {
        return ready;
    }

    /** did the player say the song had finished? */
    public boolean isFinished() {
        return finished;
    }

    /** the rate the emulated player produces at, as its device said when it opened */
    public void setSampleRate(int sampleRate) {
        if (sampleRate > 0) {
            this.sampleRate = sampleRate;
        }
    }

    /** how much silence the player has dropped, which the guest's stamps do not know about */
    public void setDroppedFrames(long droppedFrames) {
        this.droppedFrames = droppedFrames;
    }

    /** is there enough to say where in the song a frame falls? */
    public boolean isSynchronized() {
        return ready && !Double.isNaN(anchorFrames);
    }

    /**
     * Where in the song a frame of the player's output falls [ms], or {@code NaN} while that is
     * not yet known. Frames are counted the way {@link MmfToolPlayer} hands them over, from the
     * first one it did.
     */
    public double songMillis(long frame) {
        double anchor = anchorFrames;
        if (Double.isNaN(anchor)) {
            return Double.NaN;
        }
        return (frame - anchor) * 1000.0 / sampleRate;
    }

    @Override
    public void write(byte[] data, int offset, int length, long frames) {
        for (int i = offset; i < offset + length; i++) {
            if (data[i] == '\n') {
                flush(frames);
            } else if (data[i] != '\r') {
                line.write(data[i]);
            }
        }
    }

    private void flush(long frames) {
        String text = line.toString(StandardCharsets.ISO_8859_1);
        line.reset();
        if (text.isEmpty()) {
            return;
        }
        if (!text.startsWith(MARK)) {
            // the player's own messages, which used to go to the host's console and would
            // otherwise be lost now that this has taken the channel
logger.log(Level.DEBUG, "smaf: " + text);
            return;
        }
        try {
            parse(text, frames);
        } catch (RuntimeException e) {
logger.log(Level.DEBUG, "smaf: cannot read telemetry \"" + text + "\": " + e);
        }
    }

    private void parse(String text, long frames) {
        String[] f = text.split(" ");
        switch (f[0]) {
            case MARK + "1" -> { // the score starts here
                score.clear();
                for (int i = 1; i < f.length; i++) {
                    if (f[i].startsWith("fmt=")) {
                        score.setFormat(Integer.parseInt(f[i].substring(4)));
                    } else if (f[i].startsWith("start=")) {
                        // the player begins here and counts from here, so the score is held
                        // that way too - see SmafScore#startMillis
                        score.setStartMillis(Integer.parseInt(f[i].substring(6)));
                    }
                }
            }
            case MARK + "n" -> score.add(new SmafScore.Note(
                    Integer.parseInt(f[1]), Integer.parseInt(f[2]),
                    Integer.parseInt(f[3]), Integer.parseInt(f[4]), Integer.parseInt(f[5])));
            case MARK + "c" -> score.add(new SmafScore.Control(
                    Integer.parseInt(f[1]), Integer.parseInt(f[2]),
                    Integer.parseInt(f[3]), Integer.parseInt(f[4])));
            case MARK + "r" -> score.add(new SmafScore.Control(
                    Integer.parseInt(f[1]), Integer.parseInt(f[2]),
                    SmafScore.PROGRAM, Integer.parseInt(f[3])));
            case MARK + "b" -> score.add(new SmafScore.Control(
                    Integer.parseInt(f[1]), Integer.parseInt(f[2]),
                    SmafScore.BEND, Integer.parseInt(f[3])));
            case MARK + "z" -> { // the score ends here
                ready = true;
logger.log(Level.DEBUG, "smaf: score of %d notes, %d controls, %.1fs%s".formatted(
                        score.noteCount(), score.controlCount(), score.lengthMillis() / 1000.0,
                        score.getStartMillis() == 0 ? ""
                                : ", starting %.1fs in".formatted(score.getStartMillis() / 1000.0)));
            }
            case MARK + "s" -> startedAt = frames; // the song is playing from here
            case MARK + "p" -> beacon(Integer.parseInt(f[1]), frames);
            case MARK + "e" -> finished = true;
            default -> logger.log(Level.DEBUG, "smaf: telemetry not understood: " + text);
        }
    }

    /**
     * One reading of the player's own clock against the audio it has produced, which between them
     * say where the song begins.
     * <p>
     * Only once there is audio: before the first sample everything before the song has been
     * dropped, so the count stands at nothing while the player's position runs on, and a reading
     * taken there says the song began further and further back the longer it waits.
     */
    private void beacon(int positionMillis, long frames) {
        if (frames <= 0 || positionMillis < 0) {
            return;
        }
        synchronized (anchors) {
            if (anchors.size() >= BEACONS) {
                return;
            }
            anchors.add(frames - droppedFrames - Math.round(positionMillis / 1000.0 * sampleRate));
            List<Long> sorted = new ArrayList<>(anchors);
            Collections.sort(sorted);
            anchorFrames = sorted.get(sorted.size() / 2);
            if (anchors.size() == 1) {
logger.log(Level.DEBUG, "smaf: song starts at frame %d, %.2fs into the output".formatted(
                        Math.round(anchorFrames), anchorFrames / (double) sampleRate));
            }
        }
    }

    /** how much silence the player has dropped, which the guest's stamps do not know about */
    public long getDroppedFrames() {
        return droppedFrames;
    }

    /** what the emulated player said it had produced when it started the song, for a log line */
    public long getStartedAt() {
        return startedAt;
    }

    /** where the song begins in the output [frames], or NaN while that is not known */
    public double getAnchorFrames() {
        return anchorFrames;
    }
}
