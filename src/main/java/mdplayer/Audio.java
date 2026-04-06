package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.LineEvent;

import mdplayer.Common.EnmModel;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdplayer.plugin.SampledPlugin;

import static java.lang.System.getLogger;
import static mdplayer.plugin.BasePlugin.BUFFER_SIZE;


public class Audio {

    private static final Logger logger = getLogger(Audio.class.getName());

    private final Setting setting = Setting.getInstance();

    private static final Audio instance = new Audio();

    public final VisVolume visVolume = new VisVolume();

    protected final long stwh = System.currentTimeMillis();

    private Thread trd;

    public BasePlugin<? extends BaseDriver> plugin;

    public boolean getEmuOnly() {
        return false;
    }

    public void init(BasePlugin<? extends BaseDriver> plugin) {
//        audio._fatalError = false;
        this.plugin = plugin;
        this.waveWriter = new WaveWriter();

        this.naudioWrap.start();
    }

    public int update(short[] buffer, int offset, int sampleCount) {
//logger.log(Level.TRACE, ": " + sampleCount);
        //return nAaudioRead(buffer, offset, sampleCount);

        if (plugin instanceof SampledPlugin sampledPlugin) {
            if (sampledPlugin.naudioFileReader != null) {
                if (trdClosed) {
                    _trdStopped = true;
                    //vgmFadeout = false;
                    //Stopped = true;
                }
                return sampledPlugin.nAudioRead(buffer, offset, sampleCount);
            }
        }

        int cnt = trdVgmVirtualMainFunction(buffer, offset, sampleCount);

        if (setting.getMidiKbd().getUseMIDIKeyboard()) {
            plugin.chipRegister.plugin(MidiPlugin.class).keyboard(buffer, offset, sampleCount);
        }

        return cnt;
    }

    protected int trdVgmVirtualMainFunction(short[] buffer, int offset, int sampleCount) {
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

//            stwh.reset();
//            stwh.start();
            int cnt = plugin.driverVirtual.render(buffer, offset, sampleCount);
//logger.log(Level.TRACE, "sampleCount: " + sampleCount);

            // VST
            plugin.chipRegister.plugin(VstPlugin.class).update(buffer, offset, sampleCount);

            for (int i = 0; i < sampleCount; i++) {
                int mul = (int) (16384.0 * Math.pow(10.0, plugin.masterVolume / 40.0));
                buffer[offset + i] = (short) Math.clamp((buffer[offset + i] * mul) >> 13, -0x8000, 0x7fff);

                if (!plugin.vgmFadeout) continue;

                // Fade-out Processing
                buffer[offset + i] = (short) (buffer[offset + i] * plugin.vgmFadeoutCounter);

                plugin.vgmFadeoutCounter -= plugin.vgmFadeoutCounterV;
                if (plugin.vgmFadeoutCounterV >= 0.004 && plugin.vgmFadeoutCounterV != 0.1) {
                    plugin.vgmFadeoutCounterV = 0.004;
                }

                if (plugin.vgmFadeoutCounter < 0.0) {
                    plugin.vgmFadeoutCounter = 0.0;
                }

                // After the fade out is complete, the music stops playing completely.
                if (plugin.vgmFadeoutCounter == 0.0) {
                    plugin.chipRegister.softReset(EnmModel.VirtualModel);
                    plugin.chipRegister.softReset(EnmModel.RealModel);

                    waveWriter.write(buffer, offset, i + 1);

                    waveWriter.close();

                    plugin.mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);

                    plugin.chipRegister.close();

                    //Thread.sleep(500); // Noise countermeasures

                    plugin.stopped = true;
logger.log(Level.DEBUG, "stop: " + plugin.stopped);

                    // Processing time per frame
//                    procTimePer1Frame = (int) ((double) stwh.ElapsedMilliseconds / (i + 1) * 1000000.0);
                    return i + 1;
                }
            }

            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                updateVisualVolume(buffer, offset);
            }

            waveWriter.write(buffer, offset, sampleCount);

            // Processing time per frame
//            procTimePer1Frame = (int) ((double) stwh.ElapsedMilliseconds / sampleCount * 1000000.0);
            return cnt;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
//            _fatalError = true;
            plugin.stopped = true;
        }

        return -1;
    }

    protected void naudioWrapPlaybackStopped(LineEvent e) {
//        if (e.getException != null) {
//            JOptionPane.showMessageDialog(null,
//                    "The device has stopped for some reason.\nMessage:\n%s\nStack trace:\n%s".formatted(
//                            e.Exception.Message, e.Exception.StackTrace)
//                    , "Error"
//                    , JOptionPane.ERROR_MESSAGE);
//            flgReinit = true;
//
//            try {
//                naudioWrap.Stop();
//            } catch (Exception ex) {
//                Log.forcedWrite(ex);
//            }
//
//        } else {
        try {
            stop();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
//        }
    }

    public void stop() {
logger.log(Level.INFO, "stop enter");
        try {
            if (plugin.paused) pause();

            if (plugin.stopped) {
                trdClosed = true;
                while (!_trdStopped) { // TODO if realChip is not null, _trdStopped is false
                    Thread.sleep(1);
                }

                if (plugin instanceof SampledPlugin sampledPlugin) {
                    if (sampledPlugin.naudioFileReader != null) {
                        sampledPlugin.nAudioStop();
                    }
                }

                return;
            }

            if (!plugin.paused) {
                LineEvent.Type ps = naudioWrap.getPlaybackState();
                if (ps != null && ps != LineEvent.Type.STOP) {
                    plugin.vgmFadeoutCounterV = 0.1;
                    plugin.vgmFadeout = true;
                    int cnt = 0;
                    while (!plugin.stopped && cnt < 100) {
                        Thread.yield();
                        cnt++;
                    }
                }
            }
            trdClosed = true;

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
            while (!_trdStopped) {
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
//new Exception().printStackTrace();
//logger.log(Level.DEBUG, "stop: " + stopped + ", " + hashCode());

try {
            plugin.chipRegister.softReset(EnmModel.VirtualModel);
            plugin.chipRegister.softReset(EnmModel.RealModel);
} catch (Exception e) {
 logger.log(Level.ERROR, e.toString()); // usually chip 1 is null
}

            //chipRegister.outMIDIData_Close();
            if (setting.getOther().getWavSwitch()) {
                Thread.sleep(500);
                waveWriter.close();
            }

            // DEBUG
            //vstparse();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private Audio() {
        logger.log(Level.DEBUG, "Audio:Init:STEP 01");

        naudioWrap = new NAudioWrap(setting.getOutputDevice().getSampleRate());
        naudioWrap.playbackStopped = this::naudioWrapPlaybackStopped;
    }

    public static Audio getInstance() {
        return instance;
    }

    public final NAudioWrap naudioWrap;
    public WaveWriter waveWriter = null;

    public void stepPlay(int Step) {
        plugin.stepCounter = Step;
    }

    public void closeWaveWriter() {
        this.waveWriter.close();
    }

    public void close() {
logger.log(Level.INFO, "close enter");
        plugin.close();

        try {
            plugin.chipRegister.plugin(MidiPlugin.class).midiClose();
            plugin.chipRegister.plugin(RealChipPlugin.class).close();

            naudioWrap.stop();

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public Thread trdMain = null;
    public boolean trdClosed = false;
    private boolean _trdStopped = true;

    // for gui
    protected void trdIF() {
        while (true) {
            Request req = OpeManager.getRequestToAudio();
            if (req == null) {
                Thread.yield(); // TODO this should not be a thread, use event system or blocking queue
                continue;
            }

            switch (req.request) {
                case Die: // Please kill yourself
                    plugin.seqDie();
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

    protected void trdVgmRealFunction() {

        if (plugin.driverReal == null) { // first time, driverReal must be null
            this.trdClosed = true;
            this.setTrdStopped(true);
            return;
        }

        double o = System.currentTimeMillis() / Audio.swFreq;
        double step = 1 / (double) setting.getOutputDevice().getSampleRate();
        this.setTrdStopped(false);
        try {
            while (!this.trdClosed) {
                Thread.sleep(0);

                double el1 = System.currentTimeMillis() / Audio.swFreq;
                if (el1 - o < step) continue;
                if (el1 - o >= step * setting.getOutputDevice().getSampleRate() / 100.0) { // Threshold 10ms
                    do {
                        o += step;
                    } while (el1 - o >= step);
                } else {
                    o += step;
                }

                if (plugin.stopped || plugin.paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (plugin.hiyorimiNecessary && plugin.driverVirtual.isDataBlock) {
                    continue;
                }

                if (plugin.vgmFadeout) {
                    plugin.fadeOut();
                }

                if (plugin.hiyorimiNecessary) {
//                    long v = driverReal.vgmFrameCounter - audio.driverVirtual.vgmFrameCounter;
//                    long d = setting.getoutputDevice().getSampleRate() * (setting.LatencySCCI - setting.getoutputDevice().getSampleRate() * setting.LatencyEmulation) / 1000;
//                    long l = getLatency() / 4;
//                    int m = 0;
//                    if (d >= 0) {
//                        if (v >= d - l && v <= d + l) m = 0;
//                        else m = (v + d > l) ? 1 : 2;
//                    } else {
//                        d = Math.abs(setting.getoutputDevice().getSampleRate() * ((int) setting.LatencyEmulation - (int) setting.LatencySCCI) / 1000);
//                        if (v >= d - l && v <= d + l) m = 0;
//                        else m = (v - d > l) ? 1 : 2;
//                    }

                    double dEMU = setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000.0;
                    double dSCCI = setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000.0;
                    double abs = Math.abs((plugin.driverReal.vgmFrameCounter - dSCCI) - (plugin.driverVirtual.vgmFrameCounter - dEMU));
                    int m = 0;
                    long l = plugin.getLatency() / 10;
                    if (abs >= l) {
                        m = ((plugin.driverReal.vgmFrameCounter - dSCCI) > (plugin.driverVirtual.vgmFrameCounter - dEMU)) ? 1 : 2;
                    }

                    switch (m) {
                        case 0: // x1
                            plugin.driverReal.processOneFrame();
                            break;
                        case 1: // x1/2
                            plugin.hiyorimiEven++;
                            if (plugin.hiyorimiEven > 1) {
                                plugin.driverReal.processOneFrame();
                                plugin.hiyorimiEven = 0;
                            }
                            break;
                        case 2: // x2
                            plugin.driverReal.processOneFrame();
                            plugin.driverReal.processOneFrame();
                            break;
                    }
                } else {
                    plugin.driverReal.processOneFrame();
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        this.setTrdStopped(true);
    }

    public void startTrdVgmReal() {
        if (setting.getOutputDevice().getDeviceType() == Common.DEV_Null) {
            logger.log(Level.INFO, "dev null: " + getClass().getName());
            return;
        }

        this.trdClosed = false;
        this.trdMain = new Thread(this::trdVgmRealFunction);
        this.trdMain.setPriority(Thread.MAX_PRIORITY);
        this.trdMain.setDaemon(true);
        this.trdMain.setName("trdVgmReal");
        this.trdMain.start();
    }

    /** */
    public boolean play() {
        startTrdVgmReal();

        plugin.prepare();
//logger.log(Level.TRACE, "play: " + audio.stopped + ", " + audio.hashCode());

        stop();

        sleep(500);

        plugin.paused = false;
        plugin.stopped = false;

        plugin.oneTimeReset = false;

//        if (trd == null) {
//            trd = new Thread(this::trdIF);
//            trd.setPriority(Thread.NORM_PRIORITY);
//            trd.start();
//        }

        plugin.go();

        if (plugin instanceof SampledPlugin sampledPlugin) {
            if (sampledPlugin.naudioFileReader != null) {
                sampledPlugin.nAudioStop();
            }
        }

        if (plugin instanceof SampledPlugin sampledPlugin) {
            sampledPlugin.naudioFileName = plugin.playingFileName;
        }

        try {
            this.waveWriter.open(plugin.playingFileName);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new IllegalStateException("wave file open error.", e);
        }

logger.log(Level.DEBUG, "driver: " + plugin.driverVirtual.getClass().getSimpleName());
        while (true) {
//logger.log(Level.TRACE, "loop HERE");
            short[] buffer = new short[4];

            int r = this.update(buffer, 0, buffer.length);
            if (r == -1) break;
            this.naudioWrap.write(buffer, 0, buffer.length);
            Thread.yield();
        }

        return false;
    }

    public synchronized boolean getTrdStopped() {
        return _trdStopped;
    }

    public synchronized void setTrdStopped(boolean value) {
//new Exception("value: " + value).printStackTrace(System.err);
        _trdStopped = value;
    }

    private void updateVisualVolume(short[] buffer, int offset) {
        visVolume.master = buffer[offset];

        for (var i : plugin.mds.getFirstInstruments()) {
            var vs = i.getView("volume", null);
        }
    }

//    protected long sw = System.currentTimeMillis();
    public static final double swFreq = 1000d / 44100;

    public void fadeout() {
        if (isPaused()) {
            pause();
        }

        plugin.vgmFadeout = true;
    }

    public void pause() {
        plugin.paused = !plugin.paused;
    }

    public boolean isPaused() {
        return plugin.paused;
    }

    protected static void sleep(int i) {
        try { Thread.sleep(i); } catch (InterruptedException ignore) {}
    }
}
