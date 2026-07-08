package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import mdplayer.Common.EnmModel;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdplayer.plugin.SampledPlugin;
import vavi.sound.SoundUtil;
import vavi.util.event.GenericListener;

import static java.lang.System.getLogger;
import static mdplayer.plugin.BasePlugin.BUFFER_SIZE;
import static vavi.sound.SoundUtil.volume;


/** virtual device player */
public final class Audio {

    private static final Logger logger = getLogger(Audio.class.getName());

    private final Setting setting = Setting.getInstance();

    private static final Audio instance = new Audio();

    public BasePlugin<? extends BaseDriver> plugin;

    private SourceDataLine line;

    /** while true the render loop of {@link #play()} keeps running */
    private volatile boolean rendering = false;

    /** true when the render loop of {@link #play()} has exited */
    private volatile boolean renderStopped = true;

    private Audio() {
    }

    /** singleton */
    public static Audio getInstance() {
        return instance;
    }

    /** binds a plugin */
    public void init(BasePlugin<? extends BaseDriver> plugin) {
        this.plugin = plugin;

        try {
            int sampleRate = setting.getOutputDevice().getSampleRate();
            AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);
            // for hijack datasource, we need to retrieve target SourceDataLine by name
            line = SoundUtil.getLine("#Default Audio Device", SourceDataLine.class);
logger.log(Level.DEBUG, format);
            line.addLineListener(Audio::lineListener);
            line.open(format);
            volume(line, Double.parseDouble(System.getProperty("mdplayer.volume", "0.2")));
            line.start();
        } catch (LineUnavailableException e) {
            throw new IllegalStateException(e);
        }
    }

    /** line listener */
    private static void lineListener(LineEvent e) {
logger.log(Level.DEBUG, "line: " + e.getType());
    }

    /** start blocking rendering */
    public boolean play() {
        plugin.chipRegister.plugin(RealChipPlugin.class).startThread();

        plugin.prepare();
//logger.log(Level.TRACE, "play: " + audio.stopped + ", " + audio.hashCode());
        listeners.forEach(l -> plugin.getDriver().addViewListener(l)); // TODO consider more

        stop();

        try { Thread.sleep(500); } catch (InterruptedException ignore) {}

        plugin.paused = false;
        plugin.stopped = false;

        plugin.oneTimeReset = false;

//logger.log(Level.TRACE, "stopped: " + audio.stopped + ", " + audio.hashCode());

        if (plugin instanceof SampledPlugin sampledPlugin) {
            if (sampledPlugin.naudioFileReader != null) {
                sampledPlugin.stopAudio();
            }
        }

        if (plugin instanceof SampledPlugin sampledPlugin) {
            sampledPlugin.naudioFileName = plugin.playingFileName;
        }

        logger.log(Level.DEBUG, "driver: " + plugin.driverVirtual.getClass().getSimpleName());

        rendering = true;
        renderStopped = false;
        try {
            while (rendering) {
//logger.log(Level.TRACE, "loop HERE");
                short[] buffer = new short[4];

                int r;
                if (plugin instanceof SampledPlugin sampledPlugin) {
                    r = sampledPlugin.read(buffer, 0, buffer.length);
                } else {
                    r = render(buffer, 0, buffer.length);
                    if (setting.getMidiKbd().getUseMIDIKeyboard()) {
                        plugin.chipRegister.plugin(MidiPlugin.class).keyboard(buffer, 0, buffer.length);
                    }

                    // detect the end of an emulated-chip song: once it has looped
                    // enough times or the driver reached the end of its sequence,
                    // start fading out. (The GUI drives this from its screen loop;
                    // headless callers such as tests have no such loop, so play()
                    // would otherwise render silence forever and never return.)
                    if ((setting.getOther().getUseLoopTimes() && plugin.getVgmCurLoopCounter() > setting.getOther().getLoopTimes() - 1)
                            || plugin.getVGMStopped()) {
                        plugin.fadeout = true;
                    }
                }
                if (r == -1) break;

                this.write(buffer, 0, buffer.length);

                // the fade-out has finished (render() marks the plugin stopped):
                // leave the loop so play() returns.
                if (plugin.stopped) break;

                Thread.yield();
            }
        } finally {
            renderStopped = true;
        }

        return false;
    }

    /** write to line */
    private int write(short[] buffer, int offset, int count) {
        ByteBuffer bb = ByteBuffer.allocate(count * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(buffer, offset, count);
        sb.rewind();
        return line.write(bb.array(), 0, count * Short.BYTES);
    }

    /** */
    public void stop() {
        logger.log(Level.TRACE, "stop enter");
        try {
            if (plugin.paused) pause();

            if (plugin.stopped) {
                plugin.chipRegister.plugin(RealChipPlugin.class).setThreadClosed(true);
                while (!plugin.chipRegister.plugin(RealChipPlugin.class).isThreadStopped()) { // TODO if realChip is not null, _trdStopped is false
                    Thread.sleep(1);
                }

                if (plugin instanceof SampledPlugin sampledPlugin) {
                    if (sampledPlugin.naudioFileReader != null) {
                        sampledPlugin.stopAudio();
                    }
                }

                return;
            }

            if (!plugin.paused) {
                if (!line.isRunning()) {
                    plugin.fadeoutCounterV = 0.1;
                    plugin.fadeout = true;
                    int cnt = 0;
                    while (!plugin.stopped && cnt < 100) {
                        Thread.yield();
                        cnt++;
                    }
                }
            }
            plugin.chipRegister.plugin(RealChipPlugin.class).setThreadClosed(true);

            if (plugin instanceof SampledPlugin sampledPlugin) {
                if (sampledPlugin.naudioFileReader != null) {
                    return;
                }
            }

            try {
                plugin.chipRegister.softReset(EnmModel.VirtualModel);
                plugin.chipRegister.softReset(EnmModel.RealModel);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.toString()); // usually chip 1 is null
            }

            int timeout = 5000;
            while (!plugin.chipRegister.plugin(RealChipPlugin.class).isThreadStopped()) {
                Thread.yield();
                timeout--;
                if (timeout < 1) break;
            }
            while (!plugin.stopped) {
                Thread.yield();
                timeout--;
                if (timeout < 1) break;
            }
            plugin.stopped = true;
logger.log(Level.INFO, "stop: " + plugin.stopped + ", " + hashCode());

            try {
                plugin.chipRegister.softReset(EnmModel.VirtualModel);
                plugin.chipRegister.softReset(EnmModel.RealModel);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.toString()); // usually chip 1 is null
            }

            //plugin.chipRegister.plugin(MidiPlugin.class).close();

            // DEBUG
            //plugin.chipRegister.plugin(VstPlugin.class).parse();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    /** */
    public void close() {
        logger.log(Level.INFO, "close enter");

        // stop the render loop of play() and wait for it to exit, so the
        // previous track's thread can never render into the line reopened by
        // the next init() (shared singleton line/plugin fields).
        rendering = false;
        int timeout = 1000;
        while (!renderStopped && timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
        }

        plugin.close();

        try {
            plugin.chipRegister.plugin(MidiPlugin.class).midiClose();
            plugin.chipRegister.plugin(RealChipPlugin.class).close();

            if (line.available() > 0)
                line.drain();
            line.stop();
            line.close();

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    /** */
    public void stepPlay(int step) {
        plugin.stepCounter = step;
    }

    /** */
    private int render(short[] buffer, int offset, int sampleCount) {
        if (buffer == null || buffer.length < 1 || sampleCount == 0) return 0;
        if (plugin.driverVirtual == null) return sampleCount;

        try {
            //stwh.reset(); stwh.Start();

//logger.log(Level.TRACE, "stop: " + stopped + ", " + hashCode());
            if (plugin.stopped || plugin.paused) {
                if (plugin.driverVirtual.isNotRenderingOnPause()) {
                    for (int d = offset; d < offset + sampleCount; d++) buffer[d] = 0;
                    return sampleCount;
                } else {
                    int ret = plugin.mds.update(buffer, offset, sampleCount, null);
                    return ret;
                }
            }

            int cnt = plugin.driverVirtual.render(buffer, offset, sampleCount);
//logger.log(Level.TRACE, "sampleCount: " + sampleCount);

            // VST
            plugin.chipRegister.plugin(VstPlugin.class).update(buffer, offset, sampleCount);

            for (int i = 0; i < sampleCount; i++) {
                int mul = (int) (16384.0 * Math.pow(10.0, plugin.masterVolume / 40.0));
                buffer[offset + i] = (short) Math.clamp((buffer[offset + i] * mul) >> 13, -0x8000, 0x7fff);

                if (!plugin.fadeout) continue;

                // Fade-out Processing
                buffer[offset + i] = (short) (buffer[offset + i] * plugin.fadeoutCounter);

                plugin.fadeoutCounter -= plugin.fadeoutCounterV;
                if (plugin.fadeoutCounterV >= 0.004 && plugin.fadeoutCounterV != 0.1) {
                    plugin.fadeoutCounterV = 0.004;
                }

                if (plugin.fadeoutCounter < 0.0) {
                    plugin.fadeoutCounter = 0.0;
                }

                // After the fade out is complete, the music stops playing completely.
                if (plugin.fadeoutCounter == 0.0) {
                    plugin.chipRegister.softReset(EnmModel.VirtualModel);
                    plugin.chipRegister.softReset(EnmModel.RealModel);

//                    plugin.mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);

                    plugin.chipRegister.close();

                    //Thread.sleep(500); // Noise countermeasures

                    plugin.stopped = true;
logger.log(Level.DEBUG, "stop: " + plugin.stopped);

                    return i + 1;
                }
            }

            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                updateVisualVolume(buffer, offset);
            }

            return cnt;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            plugin.stopped = true;
        }

        return -1;
    }

    /** */
    private void updateVisualVolume(short[] buffer, int offset) {
        plugin.getDriver().fireEventHappened(this, "master", buffer, offset);

        for (var i : plugin.mds.getFirstInstruments()) {
            var vs = i.getView("volume", null);
        }
    }

    /** */
    public void fadeout() {
        if (isPaused()) {
            pause();
        }

        plugin.fadeout = true;
    }

    /** */
    public void pause() {
        plugin.paused = !plugin.paused;
    }

    /** */
    public boolean isPaused() {
        return plugin.paused;
    }

    // for gui
    public void processRequest() {
        while (true) {
            Request req = OpeManager.getRequestToAudio();
            if (req == null) {
                Thread.yield(); // TODO this should not be a thread, use event system or blocking queue
                continue;
            }

            switch (req.request) {
                case Die: // Please kill yourself
                    plugin.close();
                    plugin.chipRegister.plugin(RealChipPlugin.class).realChipClose();
                    req.setEnd(true);
                    return;
                case Stop:
                    stop();
                    req.setEnd(true);
                    OpeManager.completeRequestToAudio(req);
                    break;
            }
        }
    }

    boolean emuOnly;

    public boolean isEmuOnly() {
        return emuOnly;
    }

    /**
     * add to plugin before start playing
     * TODO consider more
     */
    private final List<GenericListener> listeners = new ArrayList<>();

    /**
     * view listeners (such as visualizer)
     * TODO consider more
     */
    public void addGenericListener(GenericListener l) {
        listeners.add(l);
    }
}
