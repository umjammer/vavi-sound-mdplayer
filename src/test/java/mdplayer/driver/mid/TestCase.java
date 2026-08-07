/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.mid;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import mdplayer.lib.mid.MID;
import musicDriverInterface.MetaData;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Standard MIDI file.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-08 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String midi;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    /** a variable length quantity, the way a standard midi file spells one */
    static void putDelta(ByteArrayOutputStream out, int value) {
        List<Integer> bytes = new ArrayList<>();
        bytes.add(value & 0x7f);
        value >>= 7;
        while (value > 0) {
            bytes.add((value & 0x7f) | 0x80);
            value >>= 7;
        }
        for (int i = bytes.size() - 1; i >= 0; i--) out.write(bytes.get(i));
    }

    @Test
    @DisplayName("a variable length quantity says how long it is")
    void testDeltaLength() {
        // one byte per 7 bits, so the pointer has to step over as many as were written - reading
        // the value alone and leaving the pointer where it was put every following event out of
        // step and eventually ran off the end of the file
        int[] values = {0, 0x7f, 0x80, 0x3fff, 0x4000, 0x1fffff, 0x200000, 0x0fffffff};
        int[] lengths = {1, 1, 2, 2, 3, 3, 4, 4};
        for (int i = 0; i < values.length; i++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            putDelta(out, values[i]);
            byte[] buf = out.toByteArray();
            assertEquals(lengths[i], buf.length, "spelling of " + values[i]);
            assertEquals(values[i], MID.getDelta(0, buf), "value of " + values[i]);
            assertEquals(lengths[i], MID.deltaLength(0, buf), "length of " + values[i]);
        }
    }

    /**
     * A one track file whose notes are a multi byte delta apart, which is where the pointer used
     * to lose its place: three note on/note off pairs and nothing else.
     */
    static byte[] makeMid(int delta) {
        ByteArrayOutputStream trk = new ByteArrayOutputStream();
        for (int i = 0; i < 3; i++) {
            putDelta(trk, delta);
            trk.write(0x90); trk.write(60 + i); trk.write(100); // note on
            putDelta(trk, delta);
            trk.write(0x80); trk.write(60 + i); trk.write(0); // note off
        }
        putDelta(trk, 0);
        trk.write(0xff); trk.write(0x2f); trk.write(0x00); // end of track

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[] {'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 0, 0, 1, 0x01, (byte) 0xe0});
        out.writeBytes(new byte[] {'M', 'T', 'r', 'k'});
        int len = trk.size();
        out.writeBytes(new byte[] {(byte) (len >> 24), (byte) (len >> 16), (byte) (len >> 8), (byte) len});
        out.writeBytes(trk.toByteArray());
        return out.toByteArray();
    }

    /** drives the event parser with no tempo and no audio, so a mis-parse is an exception here */
    static int play(byte[] buf, List<int[]> sent) throws Exception {
        MID mid = new MID();
        mid.charset = Charset.forName("windows-31j");
        mid.sampleRate = 44100;
        mid.send3 = (n, c, d1, d2) -> sent.add(new int[] {c & 0xff, d1 & 0xff, d2 & 0xff});
        mid.send2 = (n, c, d1) -> sent.add(new int[] {c & 0xff, d1 & 0xff});
        mid.send0 = (n, d) -> {};
        mid.lyric = (n, l) -> {};
        boolean[] stopped = {false};
        mid.stop = () -> stopped[0] = true;
        mid.counter = () -> {};
        mid.getInformationHeader(buf);

        Method oneFrameMID = MID.class.getDeclaredMethod("oneFrameMID", byte[].class);
        oneFrameMID.setAccessible(true);
        int steps = 0;
        while (!stopped[0] && steps < 8_000_000) {
            oneFrameMID.invoke(mid, buf);
            steps++;
        }
        assertTrue(stopped[0], "the track never ended");
        return steps;
    }

    @Test
    @DisplayName("multi byte deltas keep the events in step")
    void testMultiByteDelta() throws Exception {
        for (int delta : new int[] {0x40, 0x80, 0x3fff, 0x4000, 0x40000}) {
            List<int[]> sent = new ArrayList<>();
            play(makeMid(delta), sent);
            assertEquals(6, sent.size(), "events at delta " + delta);
            for (int i = 0; i < 3; i++) {
                assertArrayEquals3(new int[] {0x90, 60 + i, 100}, sent.get(i * 2), "note on " + i);
                assertArrayEquals3(new int[] {0x80, 60 + i, 0}, sent.get(i * 2 + 1), "note off " + i);
            }
        }
    }

    static void assertArrayEquals3(int[] expected, int[] actual, String what) {
        assertEquals(expected.length, actual.length, what);
        for (int i = 0; i < expected.length; i++) assertEquals(expected[i], actual[i], what + " [" + i + "]");
    }

    @Test
    @DisplayName("a real file parses to its end, with as many note offs as note ons")
    @EnabledIf("localPropertiesExists")
    void testRealFile() throws Exception {
        Path p = Path.of(midi);
        if (!Files.exists(p)) return;

        byte[] buf = Files.readAllBytes(p);
        List<int[]> sent = new ArrayList<>();
        play(buf, sent);

        long on = sent.stream().filter(m -> (m[0] & 0xf0) == 0x90 && m.length > 2 && m[2] > 0).count();
        long off = sent.stream().filter(m -> (m[0] & 0xf0) == 0x80
                || ((m[0] & 0xf0) == 0x90 && m.length > 2 && m[2] == 0)).count();
        assertTrue(on > 0, "no notes were played");
        assertEquals(on, off, "every note that started has to stop");

        MetaData md = new MidiDriver().getMetaData(buf);
        assertTrue(!md.getFirst(MetaData.Tag.Title).isEmpty(), "no title");
    }
}
