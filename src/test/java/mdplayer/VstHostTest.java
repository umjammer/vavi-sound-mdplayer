/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.sound.midi.ShortMessage;

import mdplayer.vst.VstFileChooser;
import mdplayer.vst.VstMng;
import mdplayer.vst.VstMng.VstInfo2;
import mdplayer.vst.VstReceiver;
import org.urish.jnavst.AEffect;
import org.urish.jnavst.VstPlugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Hosting a VST plug-in, against whatever is actually installed on this machine.
 * <p>
 * There is nothing to check the host against but a real plug-in - the whole of it is a native
 * calling convention - so these look through the places plug-ins are installed and use what they
 * find. Anything that will not load is passed over rather than failed on: a VST 2 plug-in built
 * for another architecture is a library this JVM cannot open, and most of the ones still around
 * were built before the machine they are being tried on existed. Nor does every instrument that
 * loads have anything to play - a sampler whose content was never installed renders silence - so
 * the instrument tests ask that <em>one</em> of them sounds, and name the ones that did not.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-08 nsano initial version <br>
 */
class VstHostTest {

    /** {@code -Dvst.file} plays a particular plug-in instead of hunting for one */
    private static final String FILE = System.getProperty("vst.file", "");

    /** enough blocks for an instrument's attack to be well underway */
    static final int BLOCKS = 40;

    static final int FRAMES = 512;

    /** anything above this is a sound rather than a rounding error */
    static final float AUDIBLE = 0.001f;

    /** every plug-in installed here, in a stable order */
    static List<File> installed() {
        if (!FILE.isEmpty()) return List.of(new File(FILE));

        List<File> found = new ArrayList<>();
        for (String path : VstFileChooser.searchPaths()) {
            File[] files = new File(path).listFiles(f -> f.getName().toLowerCase().endsWith(VstFileChooser.extension()));
            if (files == null) continue;
            found.addAll(Arrays.asList(files));
        }
        found.sort(Comparator.comparing(File::getName));
        return found;
    }

    /**
     * Loads a plug-in.
     *
     * @return null when this one cannot be loaded here - the wrong architecture, a shell, or not a
     *         VST 2 plug-in at all
     */
    static VstPlugin load(File file) {
        try {
            VstPlugin plugin = new VstPlugin(file);
            plugin.open();
            return plugin;
        } catch (Throwable t) {
            return null;
        }
    }

    @Test
    @DisplayName("a plugin loads and says what it is")
    void load() {
        int loaded = 0;
        for (File file : installed()) {
            VstPlugin plugin = load(file);
            if (plugin == null) continue;
            loaded++;
            try {
                assertEquals(AEffect.K_EFFECT_MAGIC, plugin.getEffect().magic, file + ": the AEffect is not one");
                assertEquals(192, plugin.getEffect().size(), file + ": the AEffect layout is not the 64 bit one");
                assertTrue(plugin.getVstVersion() >= 2, file + ": not a VST 2 plugin, " + plugin.getVstVersion());
                assertTrue(plugin.canReplacing(), file + ": cannot processReplacing, which every 2.4 plugin can");
                assertNotNull(plugin.getName());
                assertFalse(plugin.getName().isEmpty(), file + ": the plugin did not say its name");
                assertTrue(plugin.getNumOutputs() > 0, file + ": no audio output");
            } finally {
                plugin.close();
            }
        }
        assumeTrue(loaded > 0, "no loadable VST 2 plugin is installed");
    }

    @Test
    @DisplayName("an instrument makes a sound when it is sent a note")
    void instrument() {
        List<String> silent = new ArrayList<>();
        String sounded = null;

        for (File file : installed()) {
            VstPlugin plugin = load(file);
            if (plugin == null) continue;
            try {
                if (!plugin.isSynth()) continue;

                plugin.setSampleRate(44100);
                plugin.setBlockSize(FRAMES);
                plugin.resume();

                float[][] out = new float[2][FRAMES];
                assertEquals(0, peak(plugin, out, 2), 0,
                        plugin.getName() + " made a sound before it was played");

                plugin.getEventQueue().add(new byte[] {(byte) ShortMessage.NOTE_ON, 60, 100}, 0);
                if (peak(plugin, out, BLOCKS) > AUDIBLE) {
                    sounded = plugin.getName();
                    break;
                }
                silent.add(plugin.getName());
            } finally {
                plugin.close();
            }
        }

        assumeTrue(sounded != null || !silent.isEmpty(), "no loadable VST 2 instrument is installed");
        assertNotNull(sounded, "no instrument played anything for a note on, tried " + silent);
    }

    /** the loudest sample over some blocks */
    static float peak(VstPlugin plugin, float[][] out, int blocks) {
        float peak = 0;
        for (int i = 0; i < blocks; i++) {
            plugin.processReplacing(new float[0][], out, FRAMES);
            for (float[] channel : out) {
                for (float v : channel) peak = Math.max(peak, Math.abs(v));
            }
        }
        return peak;
    }

    @Test
    @DisplayName("a song's mix picks up an instrument played as a midi out")
    void midiOut() throws Exception {
        List<String> silent = new ArrayList<>();
        int loudest = 0;

        for (File file : installed()) {
            VstPlugin probe = load(file);
            if (probe == null) continue;
            boolean synth = probe.isSynth();
            probe.close();
            if (!synth) continue;

            VstMng vstMng = new VstMng();
            try {
                VstInfo2 instrument = vstMng.acquireInstrument(file.getAbsolutePath());
                assertNotNull(instrument, file + ": the instrument could not be acquired");

                VstReceiver receiver = new VstReceiver(instrument);
                receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), -1);

                // what the mixer hands over: stereo, interleaved, and silent to start with
                short[] buffer = new short[FRAMES * 2];
                for (int i = 0; i < BLOCKS; i++) {
                    Arrays.fill(buffer, (short) 0);
                    vstMng.update(buffer, 0, buffer.length);
                    for (short sample : buffer) loudest = Math.max(loudest, Math.abs(sample));
                }
                if (loudest > 32) return;
                silent.add(file.getName());
            } finally {
                vstMng.close();
            }
        }

        assumeTrue(!silent.isEmpty(), "no loadable VST 2 instrument is installed");
        assertTrue(loudest > 32, "no instrument was mixed in, tried " + silent);
    }
}
