/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mdplayer.chips.MidiPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * A MIDI song played through a VST instrument.
 * <p>
 * A song whose driver only produces MIDI has always left this player's own mixer silent - whatever
 * sound there was came out of a synthesizer somewhere else, which is why such a song shows no
 * spectrum, no wave and no level. Pointing one of its outs at a VST instrument puts the sound back
 * in the mixer, and that is what this measures: the same buffer the sound card is handed, which was
 * flat before and is not now.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-08 nsano initial version <br>
 */
class VstMidiOutTest {

    /** a song the drivers play as pure MIDI; {@code -Dmidi.file} overrides */
    static final String FILE = System.getProperty("midi.file", "tmp/zms/S_Wind.ZMS");

    /** how many of the settings' MIDI out modes to point at the instrument */
    static final int MODES = 2;

    @BeforeEach
    void setup() throws Exception {
        LocalProperties.bind();
    }

    @Test
    @DisplayName("a midi song is heard in the mixer when its out is a VST instrument")
    void midiSongThroughVst() throws Exception {
        Path path = Path.of(FILE);
        assumeTrue(Files.exists(path), FILE + " is not there");

        File instrument = anInstrument();
        assumeTrue(instrument != null, "no loadable VST 2 instrument is installed");

        Setting setting = Setting.getInstance();
        List<MidiOutInfo[]> modes = new ArrayList<>();
        MidiOutInfo out = new MidiOutInfo();
        out.isVST = true;
        out.fileName = instrument.getAbsolutePath();
        out.name = instrument.getName();
        // every mode, because which one a driver ends up playing in depends on the driver
        for (int i = 0; i < MODES; i++) {
            modes.add(new MidiOutInfo[] {out});
        }
        setting.getMidiOut().setMidiOutInfos(modes);

        FileFormat format = FileFormat.getFileFormat(path.toString());
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(path))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", path.toString()));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        MidiPlugin midi = plugin.chipRegister.plugin(MidiPlugin.class);
        VstPlugin vst = plugin.chipRegister.plugin(VstPlugin.class);

        int loudest = 0;
        try {
            short[] buffer = new short[1024];
            // a few seconds of the song, block by block, the way Audio#update runs it
            for (int block = 0; block < 8 * 43 && loudest == 0; block++) {
                plugin.getDriver().render(buffer, 0, buffer.length);
                vst.update(buffer, 0, buffer.length);
                for (short sample : buffer) loudest = Math.max(loudest, Math.abs(sample));
            }
            assumeTrue(midi.sentMessages() > 0, FILE + " sent no midi: there was nothing to play");
        } finally {
            try {
                plugin.stop();
                midi.allSoundOff();
                plugin.close();
                vst.shutdown();
            } catch (Exception ignore) {
                // a driver that will not shut down cleanly is not this test's business
            }
        }

        System.err.printf("%s through %s: midi messages=%d loudest sample=%d%n",
                FILE, instrument.getName(), midi.sentMessages(), loudest);
        assertTrue(loudest > 32, "the mixer stayed silent, so the instrument was never played");
    }

    /** an installed instrument that actually makes a sound, or null when there is none */
    static File anInstrument() {
        for (File file : VstHostTest.installed()) {
            org.urish.jnavst.VstPlugin plugin = VstHostTest.load(file);
            if (plugin == null) continue;
            try {
                if (!plugin.isSynth()) continue;
                plugin.setSampleRate(44100);
                plugin.setBlockSize(VstHostTest.FRAMES);
                plugin.resume();
                plugin.getEventQueue().add(new byte[] {(byte) 0x90, 60, 100}, 0);
                float[][] scratch = new float[2][VstHostTest.FRAMES];
                if (VstHostTest.peak(plugin, scratch, VstHostTest.BLOCKS) > VstHostTest.AUDIBLE) {
                    return file;
                }
            } finally {
                plugin.close();
            }
        }
        return null;
    }
}
