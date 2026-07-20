/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Plays every sample named in {@code local.properties} for a moment, without audio and without the
 * player, and checks the visualizer has something to show for each one.
 * <p>
 * A driver whose key/volume slots stay blank is silently broken: it plays, so nothing complains,
 * but the meters read empty. That has happened one driver at a time (ZMS, MDX, RCP), which is what
 * this sweep is here to catch - it asserts that a song which produced audio also lit at least one
 * meter.
 * <p>
 * Needs the local sample collection, so it only runs with {@code -Dvavi.test=local}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
class VisualizerSweepTest {

    /** how long to give a song to get going, in render blocks of ~1 s - some have long lead-ins */
    private static final int BLOCKS = 16;

    @BeforeEach
    void setup() throws Exception {
        LocalProperties.bind();
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "local")
    void sweep() throws Exception {
        List<String> silent = new ArrayList<>();
        List<String> blank = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (Path path : LocalProperties.listFiles()) {
            try {
                Result result = play(path);
                System.err.printf("%-9s %-6s %s%n", result.missing().isEmpty() ? "ok" : "BLANK "
                        + result.missing(), result.driver, path);
                if (result.silent) silent.add(path.toString());
                else if (!result.missing().isEmpty()) {
                    blank.add(path + " (" + result.driver + " " + result.missing() + ")");
                }
            } catch (Exception | StackOverflowError e) {
                System.err.printf("%-9s %-6s %s%n", "FAILED", "-", path);
                failed.add(path + " (" + e + ")");
            }
        }

        System.err.println("silent (nothing rendered, not judged): " + silent);
        System.err.println("failed to play: " + failed);
        assertTrue(blank.isEmpty(), "meters stayed blank while playing: " + blank);
    }

    /**
     * What the meters of a song showed. Each of the three is a separate way for a row to look
     * broken, and they fail one at a time: MDX showed keys with no volume, a Konami VGM showed
     * keys and volume with no note length bar.
     */
    private record Result(String driver, boolean silent, boolean key, boolean volume, boolean bar) {

        /** the parts of a meter that never showed anything, empty when all of them did */
        List<String> missing() {
            if (silent) return List.of();
            List<String> missing = new ArrayList<>();
            if (!key) missing.add("key");
            if (!volume) missing.add("vol");
            if (!bar) missing.add("bar");
            return missing;
        }
    }

    /** renders a moment of {@code path} and reports what its meters showed */
    private static Result play(Path path) throws Exception {
        FileFormat format = FileFormat.getFileFormat(path.toString());
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(path))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", path.toString()));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);

        short[] buffer = new short[1024];
        TrackStatus status = new TrackStatus();
        // a MIDI driver renders no audio here - its notes go to the synthesizer - so "it played"
        // has to count the messages it sent as well as the samples it produced
        long midiBefore = plugin.chipRegister.plugin(MidiPlugin.class).sentMessages();
        boolean sounded = false;
        boolean key = false;
        boolean volume = false;
        boolean bar = false;
        for (int block = 0; block < BLOCKS && !(key && volume && bar); block++) {
            for (int i = 0; i < 44100 / buffer.length; i++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
                for (short s : buffer) {
                    if (s != 0) { sounded = true; break; }
                }
                source.snapshot();
                // read the meters as often as they are written: a row that only shows a note for
                // a moment between two blocks is still a row that works
                for (TrackId track : TrackId.values()) {
                    source.readStatus(track, status);
                    if (!status.playing) continue;
                    // exactly what the renderer draws: a key, the volume bar, the length bar
                    if ((status.key & 0xf) < 12) key = true;
                    if (status.volume > 0) volume = true;
                    if (status.ticks > 3) bar = true; // under 4 ticks the bar has no column to fill
                }
            }
        }
        sounded |= plugin.chipRegister.plugin(MidiPlugin.class).sentMessages() > midiBefore;
        try {
            // stop first: a MIDI driver's notes are held at the synthesizer and only stopping
            // silences them, so without this the sweep leaves each song sustaining over the next.
            // stop() no-ops on a song that already finished by itself, hence the direct call too
            plugin.stop();
            plugin.chipRegister.plugin(MidiPlugin.class).allSoundOff();
            plugin.close();
        } catch (Exception ignore) {
            // a driver that will not shut down cleanly is not this test's business
        }
        return new Result(String.valueOf(source.driverName()), !sounded, key, volume, bar);
    }
}
