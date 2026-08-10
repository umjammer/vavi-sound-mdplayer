/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.vst;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;

import mdplayer.Setting;
import org.urish.jnavst.VstHost;
import org.urish.jnavst.VstPlugin;

import static java.lang.System.getLogger;


/**
 * The VST plug-ins a song is played through.
 * <p>
 * There are two kinds and they sit at opposite ends of the mixer. An <em>effect</em> is handed the
 * finished stereo mix and replaces it, one after another in the order they were added. An
 * <em>instrument</em> has no input at all: it is sent the MIDI a driver produces and its own sound
 * is added to the mix, which is what lets a MIDI song be heard through something other than the
 * platform synthesizer.
 * <p>
 * Loading and unloading happens on whichever thread the UI or a song change runs on while
 * {@link #update} runs on the audio thread, so the two are held apart by {@link #lock} - a plug-in
 * being destroyed underneath a render call would take the process with it.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 * @version 0.01 2026-08-08 nsano works <br>
 */
public class VstMng {

    private static final Logger logger = getLogger(VstMng.class.getName());

    private final Setting setting = Setting.getInstance();

    /** the effect chain, applied to the mix in this order */
    private final List<VstInfo2> effects = new ArrayList<>();

    /** every instrument that has been loaded, in use or not */
    private final List<VstInfo2> instruments = new ArrayList<>();

    /** held by everything that loads, unloads or renders */
    private final ReentrantLock lock = new ReentrantLock();

    /** what the effect chain was built from, so a song change does not reload an unchanged chain */
    private String effectSignature = null;

    private final Host host = new Host();

    /** de-interleaved scratch, stereo, reallocated only when a bigger block arrives */
    private float[][] in = new float[2][0];
    private float[][] out = new float[2][0];

    private static final float[][] NO_INPUT = new float[0][];

    /** what this host answers a plug-in with */
    private class Host implements VstHost {

        /** the transport position, in frames; only ever moved by the audio thread */
        long samplePosition;

        @Override public float getSampleRate() {
            return setting.getOutputDevice().getSampleRate();
        }

        @Override public int getBlockSize() {
            return BLOCK_SIZE;
        }

        @Override public long getSamplePosition() {
            return samplePosition;
        }
    }

    /**
     * The largest block a plug-in is told to expect.
     * <p>
     * The mixer hands out whatever the sound card asked for, which is not a constant, so this is
     * simply large enough to cover it; a plug-in is allowed to be given fewer frames than this but
     * never more, and {@link VstPlugin#processReplacing} reallocates rather than overrun if one is.
     */
    private static final int BLOCK_SIZE = 8192;

    // loading

    /**
     * Loads a plug-in and gets it as far as being ready to render.
     *
     * @return null when it could not be loaded, which is not fatal - the song plays without it
     */
    private VstPlugin openPlugin(String pluginPath) {
        try {
            VstPlugin plugin = new VstPlugin(new File(pluginPath), host);
            plugin.open();
            plugin.setSampleRate(setting.getOutputDevice().getSampleRate());
            plugin.setBlockSize(BLOCK_SIZE);
            plugin.resume();
            return plugin;
        } catch (Throwable t) {
            // a plug-in built for another architecture fails inside dlopen, i.e. as an Error
            logger.log(Level.WARNING, "cannot load the VST plugin " + pluginPath + ": " + t);
            return null;
        }
    }

    /**
     * What a plug-in says about itself, for a chooser that has just been pointed at a file. The
     * plug-in is loaded and thrown away again.
     *
     * @return null when the file is not a VST plug-in this can load
     */
    public VstInfo getInfo(String fileName) {
        VstPlugin plugin = openPlugin(fileName);
        if (plugin == null) return null;
        try {
            VstInfo info = new VstInfo();
            info.fileName = fileName;
            info.effectName = plugin.getName();
            info.productName = plugin.getProductString();
            info.vendorName = plugin.getVendorString();
            info.programName = plugin.getProgramName();
            info.midiInputChannels = plugin.isSynth() ? 16 : 0;
            info.midiOutputChannels = 0;
            return info;
        } finally {
            plugin.close();
        }
    }

    // the effect chain

    /**
     * Builds the effect chain the settings describe.
     * <p>
     * Called before every song, so it does nothing at all while the settings still name the same
     * plug-ins in the same order: reloading a chain means re-reading megabytes of plug-in for no
     * reason, and it would drop the state the plug-ins hold.
     */
    public void setUpEffects() {
        VstInfo[] infos = setting.getVst().getVSTInfo();
        String signature = signatureOf(infos);
        lock.lock();
        try {
            if (signature.equals(effectSignature)) return;

            closeAll(effects);
            effectSignature = signature;
            if (infos == null) return;

            for (VstInfo info : infos) {
                if (info == null || info.fileName == null || info.fileName.isEmpty()) continue;
                VstPlugin plugin = openPlugin(info.fileName);
                if (plugin == null) continue;

                VstInfo2 vi = new VstInfo2();
                vi.plugin = plugin;
                vi.fileName = info.fileName;
                vi.key = info.key == null || info.key.isEmpty() ? newKey() : info.key;
                vi.effectName = plugin.getName();
                vi.productName = plugin.getProductString();
                vi.vendorName = plugin.getVendorString();
                vi.programName = plugin.getProgramName();
                vi.power = info.power;
                vi.location = info.location;
                vi.param = info.param;
                restoreState(plugin, info);
                // it was loaded switched on, which is what an effect saved as off must not be
                if (!vi.power) plugin.suspend();
                effects.add(vi);

                if (info.editor) openEditor(vi);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Puts a plug-in back the way it was left.
     * <p>
     * A plug-in that keeps its own state hands the host an opaque chunk and ignores the parameter
     * values, so which of the two is restored is the plug-in's choice, not ours.
     */
    private static void restoreState(VstPlugin plugin, VstInfo info) {
        if (plugin.usesChunks()) {
            if (info.chunk != null && !info.chunk.isEmpty()) {
                try {
                    plugin.setChunk(Base64.getDecoder().decode(info.chunk), false);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "the saved state of " + info.fileName + " is unreadable", e);
                }
            }
        } else {
            plugin.setParameters(info.param);
        }
    }

    /** what a plug-in has to say about its own state, or empty when it keeps none */
    private static String stateOf(VstPlugin plugin) {
        byte[] chunk = plugin == null ? null : plugin.getChunk(false);
        return chunk == null ? "" : Base64.getEncoder().encodeToString(chunk);
    }

    /** what the chain is made of, so an unchanged one can be left alone */
    private static String signatureOf(VstInfo[] infos) {
        if (infos == null) return "";
        StringBuilder sb = new StringBuilder();
        for (VstInfo info : infos) {
            if (info == null) continue;
            sb.append(info.fileName).append('\n');
        }
        return sb.toString();
    }

    /** adds a plug-in to the end of the chain and remembers it in the settings */
    public boolean addEffect(String fileName) {
        VstPlugin plugin = openPlugin(fileName);
        if (plugin == null) return false;

        lock.lock();
        try {
            VstInfo2 vi = new VstInfo2();
            vi.plugin = plugin;
            vi.fileName = fileName;
            vi.key = newKey();
            vi.effectName = plugin.getName();
            vi.productName = plugin.getProductString();
            vi.vendorName = plugin.getVendorString();
            vi.programName = plugin.getProgramName();
            vi.power = true;
            effects.add(vi);
            storeEffects();
            if (plugin.hasEditor()) {
                vi.editor = true;
                openEditor(vi);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Takes a plug-in back out of the chain.
     *
     * @param key the plug-in to remove, or empty for all of them
     */
    public boolean removeEffect(String key) {
        lock.lock();
        try {
            if (key == null || key.isEmpty()) {
                closeAll(effects);
            } else {
                for (int i = 0; i < effects.size(); i++) {
                    if (!key.equals(effects.get(i).key)) continue;
                    close(effects.remove(i));
                    break;
                }
            }
            storeEffects();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** switches an effect in or out of the chain without unloading it */
    public void setPower(VstInfo2 vi, boolean power) {
        lock.lock();
        try {
            vi.power = power;
            if (vi.plugin == null) return;
            if (power) vi.plugin.resume(); else vi.plugin.suspend();
            storeEffects();
        } finally {
            lock.unlock();
        }
    }

    public List<VstInfo2> getEffects() {
        return effects;
    }

    // instruments

    /**
     * Hands out an instrument for a MIDI out to play through, loading it the first time it is
     * asked for.
     * <p>
     * A file asked for twice gets two instances, because two MIDI outs pointed at the same plug-in
     * are two independent synthesizers. Instances are kept between songs and handed out again by
     * {@link #releaseInstruments}, so changing song does not reload them.
     *
     * @return null when the plug-in could not be loaded
     */
    public VstInfo2 acquireInstrument(String fileName) {
        lock.lock();
        try {
            for (VstInfo2 vi : instruments) {
                if (!vi.inUse && fileName.equals(vi.fileName)) {
                    vi.inUse = true;
                    if (vi.plugin != null) vi.plugin.resume();
                    return vi;
                }
            }

            VstPlugin plugin = openPlugin(fileName);
            if (plugin == null) return null;

            VstInfo2 vi = new VstInfo2();
            vi.plugin = plugin;
            vi.fileName = fileName;
            vi.key = newKey();
            vi.isInstrument = true;
            vi.inUse = true;
            vi.power = true;
            vi.effectName = plugin.getName();
            vi.productName = plugin.getProductString();
            vi.vendorName = plugin.getVendorString();
            vi.programName = plugin.getProgramName();
            instruments.add(vi);
            return vi;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gives every instrument back, ready to be handed out for the next song.
     * <p>
     * Suspending is what actually silences a plug-in - it drops the voices it is holding and the
     * tail of whatever was sounding, which an all sound off would only ask it to do and which it
     * could not act on anyway with nothing left to render it.
     */
    public void releaseInstruments() {
        lock.lock();
        try {
            for (VstInfo2 vi : instruments) {
                if (vi.plugin != null) vi.plugin.suspend();
                vi.inUse = false;
            }
        } finally {
            lock.unlock();
        }
    }

    // rendering

    /**
     * Mixes the instruments in and runs the mix through the effects.
     * <p>
     * Called from the audio thread once per block, right after the drivers have rendered.
     *
     * @param buffer stereo, interleaved
     * @param sampleCount how much of it to work on, counted in samples rather than frames
     */
    public void update(short[] buffer, int offset, int sampleCount) {
        if (buffer == null || sampleCount < 2) return;
        // read without the lock: nothing is loaded for most songs, and this runs every block. The
        // worst a stale answer costs is one block of a plugin that was added the instant before
        if (effects.isEmpty() && instruments.isEmpty()) return;
        int frames = sampleCount / 2;

        lock.lock();
        try {
            allocate(frames);

            for (VstInfo2 vi : instruments) {
                if (!vi.inUse || !vi.power || !renders(vi)) continue;
                vi.plugin.processReplacing(NO_INPUT, out, frames);
                mix(buffer, offset, frames, vi.plugin.getNumOutputs());
            }

            for (VstInfo2 vi : effects) {
                if (!vi.power || !renders(vi)) continue;
                deinterleave(buffer, offset, frames);
                vi.plugin.processReplacing(in, out, frames);
                replace(buffer, offset, frames, vi.plugin.getNumOutputs());
            }

            host.samplePosition += frames;
        } catch (Throwable t) {
            logger.log(Level.ERROR, "VST rendering failed, the plugins are being switched off", t);
            panic();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Whether there is anything to take from this plug-in.
     * <p>
     * A plug-in with no outputs would leave the scratch buffers holding the last block another
     * plug-in wrote, which would then be mixed in as noise.
     */
    private static boolean renders(VstInfo2 vi) {
        return vi.plugin != null && vi.plugin.getNumOutputs() > 0;
    }

    /** after a plug-in has misbehaved: keep the song playing rather than fail every block */
    private void panic() {
        for (VstInfo2 vi : effects) vi.power = false;
        for (VstInfo2 vi : instruments) vi.power = false;
    }

    private void allocate(int frames) {
        if (in[0].length >= frames) return;
        in = new float[][] {new float[frames], new float[frames]};
        out = new float[][] {new float[frames], new float[frames]};
    }

    private void deinterleave(short[] buffer, int offset, int frames) {
        for (int i = 0; i < frames; i++) {
            in[0][i] = buffer[offset + i * 2] / 32768f;
            in[1][i] = buffer[offset + i * 2 + 1] / 32768f;
        }
    }

    /** the plug-in's output replaces what was there, which is what an effect is for */
    private void replace(short[] buffer, int offset, int frames, int channels) {
        int right = channels > 1 ? 1 : 0;
        for (int i = 0; i < frames; i++) {
            buffer[offset + i * 2] = toShort(out[0][i]);
            buffer[offset + i * 2 + 1] = toShort(out[right][i]);
        }
    }

    /** the plug-in's output is added to what was there, which is what an instrument is for */
    private void mix(short[] buffer, int offset, int frames, int channels) {
        int right = channels > 1 ? 1 : 0;
        for (int i = 0; i < frames; i++) {
            buffer[offset + i * 2] = add(buffer[offset + i * 2], out[0][i]);
            buffer[offset + i * 2 + 1] = add(buffer[offset + i * 2 + 1], out[right][i]);
        }
    }

    private static short toShort(float value) {
        return (short) Math.clamp((int) (value * 32767f), -32768, 32767);
    }

    private static short add(short sample, float value) {
        return (short) Math.clamp(sample + (int) (value * 32767f), -32768, 32767);
    }

    // editors

    /**
     * Shows a plug-in's own editor, which is a window of the plug-in's making inside one of ours.
     * <p>
     * The window is always built on the event thread, because this is also reached from the one
     * that starts a song. Never with {@code invokeAndWait}: the caller may hold {@link #lock},
     * which the event thread takes whenever a row of the effect list is clicked.
     */
    public void openEditor(VstInfo2 vi) {
        if (vi.plugin == null) {
            vi.editor = false;
            return;
        }
        if (vi.form != null && vi.form.isVisible()) return;
        vi.editor = true;
        onEventThread(() -> {
            try {
                FormVST form = new FormVST(null);
                form.setPlugin(vi.plugin);
                form.show(vi);
                vi.form = form;
            } catch (Throwable t) {
                logger.log(Level.WARNING, "cannot open the editor of " + vi.fileName, t);
                vi.editor = false;
            }
        });
    }

    /** closes the editor and keeps where it was, so it opens in the same place next time */
    public void closeEditor(VstInfo2 vi) {
        FormVST form = vi.form;
        if (form == null) {
            vi.editor = false;
            return;
        }
        vi.location = form.getLocation();
        vi.form = null;
        vi.editor = false;
        onEventThread(form::close);
    }

    private static void onEventThread(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    // lifecycle

    /** writes the effect chain back into the settings, which is what is reloaded next time */
    private void storeEffects() {
        List<VstInfo> stored = new ArrayList<>();
        for (VstInfo2 vi : effects) {
            VstInfo info = new VstInfo();
            info.key = vi.key;
            info.fileName = vi.fileName;
            info.power = vi.power;
            info.editor = vi.editor;
            info.effectName = vi.effectName;
            info.productName = vi.productName;
            info.vendorName = vi.vendorName;
            info.programName = vi.programName;
            info.location = vi.form != null ? vi.form.getLocation() : vi.location;
            info.param = vi.plugin != null ? vi.plugin.getParameters() : vi.param;
            info.chunk = stateOf(vi.plugin);
            info.midiInputChannels = vi.midiInputChannels;
            info.midiOutputChannels = vi.midiOutputChannels;
            stored.add(info);
        }
        setting.getVst().setVSTInfo(stored.toArray(VstInfo[]::new));
        effectSignature = signatureOf(setting.getVst().getVSTInfo());
    }

    /**
     * Unloads everything, on the way out of the application. The plug-ins survive a song change -
     * see {@link #setUpEffects} and {@link #releaseInstruments} - so this is the only place they
     * are actually let go of.
     */
    public void close() {
        lock.lock();
        try {
            storeEffects();
            closeAll(effects);
            closeAll(instruments);
            effectSignature = null;
        } finally {
            lock.unlock();
        }
    }

    private void closeAll(List<VstInfo2> list) {
        for (VstInfo2 vi : list) {
            close(vi);
        }
        list.clear();
    }

    private void close(VstInfo2 vi) {
        try {
            closeEditor(vi);
            if (vi.plugin != null) vi.plugin.close();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "closing the VST plugin " + vi.fileName + " failed", t);
        }
        vi.plugin = null;
    }

    /** the identity an effect is found by in the settings and in the list window */
    private static String newKey() {
        return String.valueOf(System.nanoTime());
    }

    /**
     * A loaded plug-in, which is {@link VstInfo} - what the settings keep about it - plus the
     * things that only exist while it is loaded.
     */
    public static class VstInfo2 extends VstInfo {

        public VstPlugin plugin = null;

        /** the window its editor is in, or null while the editor is closed */
        FormVST form = null;

        public boolean isInstrument = false;

        /** whether a MIDI out is currently pointed at this instrument */
        boolean inUse = false;
    }
}
