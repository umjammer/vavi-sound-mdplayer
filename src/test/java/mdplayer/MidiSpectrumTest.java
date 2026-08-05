/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * The analyzer bars of a song mdplayer renders no audio for.
 * <p>
 * A ZMS is played by sending MIDI to a synthesizer, which mixes its own sound: mdplayer's mixer
 * stays silent, so the {@link vavi.sound.visualizer.fmdsp.FftAnalyzer} has nothing to measure and
 * the spectrum stood empty for the whole song while every other part of the display worked. The
 * bars come from the notes instead - see {@link mdplayer.fmdsp.MidiReader#readFft} - and this
 * plays a real one headlessly to check they do.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-31 nsano initial version <br>
 */
class MidiSpectrumTest {

    /** the sample from {@code AGENTS.md}, whose bars were blank; {@code -Dmidi.file} overrides */
    static final String FILE = System.getProperty("midi.file", "tmp/zms/S_Wind.ZMS");

    @BeforeEach
    void setup() throws Exception {
        LocalProperties.bind();
    }

    @Test
    @DisplayName("a midi song's spectrum is drawn from its notes")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void spectrum() throws Exception {
        Path path = Path.of(FILE);
        assumeTrue(Files.exists(path), FILE + " is not there");

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

        MidiPlugin midi = plugin.chipRegister.plugin(MidiPlugin.class);
        int[] bars = new int[FftDataSource.LENGTH];
        int peak = 0;
        boolean rendered = false;
        try {
            short[] buffer = new short[1024];
            // a few seconds of the song, given it snapshot by snapshot the way the player does
            for (int block = 0; block < 4 * 43 && peak == 0; block++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
                for (short s : buffer) {
                    if (s != 0) { rendered = true; break; }
                }
                source.snapshot();
                // the bars are read on the drawing thread and their ballistics are timed off the
                // wall clock, so they have to be given frames as well as notes
                source.fft().readFft(bars);
                Thread.sleep(1);
                for (int bar : bars) peak = Math.max(peak, bar);
            }
            assumeTrue(midi.sentMessages() > 0, FILE + " sent no midi: nothing to draw from");
        } finally {
            try {
                plugin.stop();
                midi.allSoundOff();
                plugin.close();
            } catch (Exception ignore) {
                // a driver that will not shut down cleanly is not this test's business
            }
        }
        System.err.printf("%s: midi messages=%d rendered=%b peak bar=%d%n",
                FILE, midi.sentMessages(), rendered, peak);
        assertTrue(peak > 0, "the spectrum stayed blank");
    }
}
