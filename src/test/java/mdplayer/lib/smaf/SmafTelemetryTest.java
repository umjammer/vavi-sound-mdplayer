/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.smaf;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * What the emulated player says, without the emulated player: the telemetry is a few lines of
 * text and a frame count, so everything it decides can be decided here in a millisecond instead
 * of over the twenty seconds it takes to play a song.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-11 nsano initial version <br>
 */
class SmafTelemetryTest {

    static final int RATE = 48000;

    /** feeds text the way the guest writes it, in whatever pieces, with a frame stamp */
    static void feed(SmafTelemetry telemetry, String text, long frames) {
        byte[] b = text.getBytes(StandardCharsets.ISO_8859_1);
        telemetry.write(b, 0, b.length, frames);
    }

    static SmafTelemetry withScore(String... lines) {
        SmafTelemetry telemetry = new SmafTelemetry();
        telemetry.setSampleRate(RATE);
        for (String line : lines) {
            feed(telemetry, line + "\n", 0);
        }
        return telemetry;
    }

    @Test
    void readsTheScore() {
        SmafTelemetry telemetry = withScore(
                "Playing: song.mmf (Press Ctrl+C to stop)",
                "#MT1 fmt=4 dbase=4 gbase=4 start=0 stop=0",
                "#MTr 100 2 37",
                "#MTc 100 2 7 110",
                "#MTn 500 2 60 100 250",
                "#MTz 4");

        SmafScore score = telemetry.getScore();
        assertTrue(telemetry.isReady());
        assertEquals(1, score.noteCount());
        assertEquals(2, score.controlCount());
        assertEquals(4, score.getFormat());
        assertEquals(750, score.lengthMillis());

        score.seek(600);
        SmafScore.Channel channel = score.channel(2);
        assertTrue(channel.sounding());
        assertTrue(channel.keyOn);
        assertNotNull(channel.top());
        assertEquals(60, channel.top().key());
        assertEquals(100, channel.top().velocity());
        assertEquals(37, channel.program);
        assertEquals(110, channel.volume);

        // the note lets go on its own: its length is written next to it, unlike MIDI
        score.clearEdges();
        score.seek(800);
        assertFalse(channel.sounding());
        assertFalse(channel.keyOn);
        assertNull(channel.top());
    }

    /**
     * A file may name a start point, and the player begins there and counts its position from
     * there - so everything in front of it has to move back, or the display runs that much ahead
     * of the music for the whole song.
     */
    @Test
    void movesTheScoreOntoThePlayersClock() {
        SmafTelemetry telemetry = withScore(
                "#MT1 fmt=2 dbase=4 gbase=4 start=2180 stop=0",
                "#MTc 16 0 7 100",
                "#MTn 2180 0 43 127 104",
                "#MTz 3");

        SmafScore score = telemetry.getScore();
        assertEquals(2180, score.getStartMillis());
        assertEquals(0, score.firstNoteMillis());

        // the setup in front of the start point is applied by the first look, and the note the
        // song starts on is struck at nothing rather than two seconds in
        score.seek(0);
        assertEquals(100, score.channel(0).volume);
        assertTrue(score.channel(0).sounding());
        assertEquals(43, score.channel(0).top().key());
    }

    /**
     * The position the player prints and the audio it has produced by then say between them where
     * the song begins in the output - which nothing else can say, the silence in front of a song
     * and the silence a song opens on being the same silence from this end.
     */
    @Test
    void findsWhereTheSongBeginsInTheOutput() {
        SmafTelemetry telemetry = withScore(
                "#MT1 fmt=4 dbase=4 gbase=4 start=0 stop=0",
                "#MTn 0 0 60 100 100",
                "#MTz 2");
        assertFalse(telemetry.isSynchronized());

        // nothing has been produced yet, so this says nothing about anything
        feed(telemetry, "#MTp 500\n", 0);
        assertFalse(telemetry.isSynchronized());

        // a second into the song with a second of audio produced: the song began where the audio
        // did. The stamps run half a second behind the position here, the way a player that has
        // handed its buffers over reports a position it has already passed
        for (int millis = 1000; millis <= 4000; millis += 500) {
            feed(telemetry, "#MTp " + millis + "\n", (long) millis * RATE / 1000);
        }
        assertTrue(telemetry.isSynchronized());
        assertEquals(0, telemetry.getAnchorFrames(), 1);
        assertEquals(2000, telemetry.songMillis(2L * RATE), 1);

        // The frames are counted in the stream the player hands over, which is what is left after
        // it has dropped what it took for silence. A second thrown away is a second of the song
        // that is not in that stream at all, so the song began a second before it starts - and
        // its first frame is a second into the music rather than at the top of it.
        SmafTelemetry dropped = withScore(
                "#MT1 fmt=4 dbase=4 gbase=4 start=0 stop=0",
                "#MTn 0 0 60 100 100",
                "#MTz 2");
        dropped.setDroppedFrames(RATE); // a second of it
        feed(dropped, "#MTp 1000\n", RATE);
        assertEquals(-RATE, dropped.getAnchorFrames(), 1);
        assertEquals(1000, dropped.songMillis(0), 1);
    }

    /** a line can arrive in any number of pieces, and the guest's own messages are not telemetry */
    @Test
    void readsTextHoweverItArrives() {
        SmafTelemetry telemetry = new SmafTelemetry();
        telemetry.setSampleRate(RATE);
        feed(telemetry, "#MT1 fmt=4 dbase=4 gbase=4 start=0 stop=0\n#MTn 0 1 6", 0);
        feed(telemetry, "0 100 100\r\n#MT", 0);
        feed(telemetry, "z 2\n", 0);

        assertTrue(telemetry.isReady());
        assertEquals(1, telemetry.getScore().noteCount());
        telemetry.getScore().seek(0);
        assertEquals(60, telemetry.getScore().channel(1).top().key());
    }

    /** a play head asked for a moment it has gone past starts the song over rather than sulking */
    @Test
    void playsTheSongAgain() {
        SmafTelemetry telemetry = withScore(
                "#MT1 fmt=4 dbase=4 gbase=4 start=0 stop=0",
                "#MTn 100 0 60 100 100",
                "#MTz 2");
        SmafScore score = telemetry.getScore();

        score.seek(150);
        assertTrue(score.channel(0).sounding());
        score.seek(1000);
        assertFalse(score.channel(0).sounding());

        score.seek(150);
        assertTrue(score.channel(0).sounding());
        assertTrue(score.channel(0).keyOn);
    }
}
