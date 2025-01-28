package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dotnet4j.util.compat.Tuple;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.OpeManager;
import mdplayer.Request;
import mdplayer.Setting;
import mdplayer.WaveWriter;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Ym3526Chip;
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.driver.Vgm;
import mdplayer.format.AIFFFileFormat;
import mdplayer.format.FileFormat;
import mdplayer.format.MP3FileFormat;
import mdplayer.format.WAVFileFormat;
import mdsound.MDSound;
import mdsound.MDSound.Chip;

import static java.lang.System.getLogger;
import static mdplayer.chips.RealChipPlugin.realChipClose;


/**
 * BasePlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public abstract class BasePlugin implements Plugin {

    private static final Logger logger = getLogger(BasePlugin.class.getName());

    protected Setting setting = Setting.getInstance();

    public Audio audio = Audio.getInstance();

    protected byte[] vgmBuf = null;
    protected double vgmSpeed;

    protected boolean oneTimeReset = false;

    protected String playingFileName;
    protected String playingArcFileName;
    protected int midiMode = 0;
    protected int songNo = 0;
    protected List<Tuple<String, byte[]>> extendFile = null;

    protected final long stwh = System.currentTimeMillis();

    public boolean flgReinit = false;

    private Thread trd;

    protected int vgmRealFadeoutVol = 0;
    protected int vgmRealFadeoutVolWait = 4;

    protected int hiyorimiEven = 0;
    public boolean hiyorimiNecessary = false;

    protected static void sleep(int i) {
        try { Thread.sleep(i); } catch (InterruptedException ignore) {}
    }

    protected Map<Class<? extends mdplayer.Chip>, List<Chip>> chips = new HashMap<>();

    protected void put(Class<? extends mdplayer.Chip> chip, Chip info) {
        if (chips.containsKey(chip)) {
            chips.get(chip).add(info);
        } else {
            chips.put(chip, new ArrayList<>(List.of(info)));
        }
    }

    protected boolean contains(Class<? extends mdplayer.Chip> chip, int index) {
        var infos = chips.get(chip);
        return infos != null && index < infos.size();
    }

    protected List<Chip> flatten() {
        return chips.values().stream().flatMap(Collection::stream).toList();
    }

    public boolean getEmuOnly() {
        return false;
    }

    protected BasePlugin() {
        init();
    }

    @Override
    public void init() {
        logger.log(Level.DEBUG, "Audio:Init:Begin");

//        logger.log(Level.DEBUG, "Audio:Init:STEP 02");

        audio.waveWriter = new WaveWriter(setting);

//        logger.log(Level.DEBUG, "Audio:Init:STEP 03");

        setting.init();

        logger.log(Level.DEBUG, "Audio:Init:STEP 05");

        audio.paused = false;
        audio.stopped = true;
logger.log(Level.DEBUG, "stop: " + audio.stopped + ", " + audio.hashCode());
//        audio._fatalError = false;
        oneTimeReset = false;

//        logger.log(Level.DEBUG, "Audio:Init:STEP 06");


//        logger.log(Level.DEBUG, "Audio:Init:STEP 07");

        // midi out released
        audio.chipRegister.plugin(MidiPlugin.class).releaseAll();

//        logger.log(Level.DEBUG, "Audio:Init:STEP 08");


//        logger.log(Level.DEBUG, "Audio:Init:STEP 09");

        // Dynamic loading of each external dll

//        logger.log(Level.DEBUG, "Audio:Init:STEP 10");

        audio.naudioWrap.start(setting);

        logger.log(Level.DEBUG, "Audio:Init:Complete");
    }

    protected void trdIF() {
        while (true) {
            Request req = OpeManager.getRequestToAudio();
            if (req == null) {
                Thread.yield(); // TODO this should not be a thread, use event system or blocking queue
                continue;
            }

            switch (req.request) {
            case Die: // Please kill yourself
                seqDie();
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

        if (audio.driverReal == null) { // first time, driverReal must be null
            audio.trdClosed = true;
            audio.setTrdStopped(true);
            return;
        }

        double o = System.currentTimeMillis() / audio.swFreq;
        double step = 1 / (double) setting.getOutputDevice().getSampleRate();
        audio.setTrdStopped(false);
        try {
            while (!audio.trdClosed) {
                Thread.sleep(0);

                double el1 = System.currentTimeMillis() / audio.swFreq;
                if (el1 - o < step) continue;
                if (el1 - o >= step * setting.getOutputDevice().getSampleRate() / 100.0) { // Threshold 10ms
                    do {
                        o += step;
                    } while (el1 - o >= step);
                } else {
                    o += step;
                }

                if (audio.stopped || audio.paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (hiyorimiNecessary && audio.driverVirtual.isDataBlock) {
                    continue;
                }

                if (audio.vgmFadeout) {
                    if (vgmRealFadeoutVol != 1000) vgmRealFadeoutVolWait--;
                    if (vgmRealFadeoutVolWait == 0) {
                        if (contains(Ym2151Chip.class, 0)) audio.chipRegister.chip(Ym2151Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2203Chip.class, 0)) audio.chipRegister.chip(Ym2203Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ay8910Chip.class, 0)) audio.chipRegister.chip(Ay8910Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2413Chip.class, 0)) audio.chipRegister.chip(Ym2413Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2608Chip.class, 0)) audio.chipRegister.chip(Ym2608Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2610Chip.class, 0)) audio.chipRegister.chip(Ym2610Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2612Chip.class, 0)) audio.chipRegister.chip(Ym2612Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym3526Chip.class, 0)) audio.chipRegister.chip(Ym3526Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym3812Chip.class, 0)) audio.chipRegister.chip(Ym3812Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Sn76489Chip.class, 0)) audio.chipRegister.chip(Sn76489Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(YmF262Chip.class, 0)) audio.chipRegister.chip(YmF262Chip.class).setFadeout(0, vgmRealFadeoutVol);

                        if (contains(Ym2151Chip.class, 1)) audio.chipRegister.chip(Ym2151Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2203Chip.class, 1)) audio.chipRegister.chip(Ym2203Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ay8910Chip.class, 1)) audio.chipRegister.chip(Ay8910Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2413Chip.class, 1)) audio.chipRegister.chip(Ym2413Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2608Chip.class, 1)) audio.chipRegister.chip(Ym2608Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2610Chip.class, 1)) audio.chipRegister.chip(Ym2610Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2612Chip.class, 1)) audio.chipRegister.chip(Ym2612Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym3526Chip.class, 1)) audio.chipRegister.chip(Ym3526Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym3812Chip.class, 1)) audio.chipRegister.chip(Ym3812Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Sn76489Chip.class, 1)) audio.chipRegister.chip(Sn76489Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(YmF262Chip.class, 1)) audio.chipRegister.chip(YmF262Chip.class).setFadeout(1, vgmRealFadeoutVol);

                        vgmRealFadeoutVol++;

                        vgmRealFadeoutVol = Math.min(127, vgmRealFadeoutVol);
                        if (vgmRealFadeoutVol == 127) {
//                            if (SoundChip.realChip != null) {
//                                softReset(EnmModel.RealModel);
//                            }
                            vgmRealFadeoutVolWait = 1000;
                            audio.chipRegister.plugin(MidiPlugin.class).resetAll();
                        } else {
                            vgmRealFadeoutVolWait = 700 - vgmRealFadeoutVol * 2;
                        }
                    }
                }

                if (hiyorimiNecessary) {
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
                    double abs = Math.abs((audio.driverReal.vgmFrameCounter - dSCCI) - (audio.driverVirtual.vgmFrameCounter - dEMU));
                    int m = 0;
                    long l = getLatency() / 10;
                    if (abs >= l) {
                        m = ((audio.driverReal.vgmFrameCounter - dSCCI) > (audio.driverVirtual.vgmFrameCounter - dEMU)) ? 1 : 2;
                    }

                    switch (m) {
                    case 0: // x1
                        audio.driverReal.processOneFrame();
                        break;
                    case 1: // x1/2
                        hiyorimiEven++;
                        if (hiyorimiEven > 1) {
                            audio.driverReal.processOneFrame();
                            hiyorimiEven = 0;
                        }
                        break;
                    case 2: // x2
                        audio.driverReal.processOneFrame();
                        audio.driverReal.processOneFrame();
                        break;
                    }
                } else {
                    audio.driverReal.processOneFrame();
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        audio.setTrdStopped(true);
    }

    public int getLatency() {
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_AsioOut) {
            return setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getLatency() / 1000;
        }
        return 0; // naudioWrap.getAsioLatency(); TODO
    }

    public boolean play() {
//logger.log(Level.TRACE, "play: " + audio.stopped + ", " + audio.hashCode());
        audio.errMsg = "";

        if (trd == null) {
            trd = new Thread(this::trdIF);
            trd.setPriority(Thread.NORM_PRIORITY);
            trd.start();
        }

        stop();

        chips.clear();

        go();

        try {
            audio.waveWriter.open(playingFileName);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            audio.errMsg = "wave file open error.";
            return false;
        }

//        Plugin plugin = audio.playingFileFormat.getPlugin();
//        boolean r = plugin.play(playingFileName, audio.playingFileFormat);

        while (true) {
//logger.log(Level.TRACE, "loop HERE");
            short[] buffer = new short[4];

            audio.trdVgmVirtualFunction(buffer, 0, buffer.length);
            audio.naudioWrap.write(buffer, 0, buffer.length);
            Thread.yield();
        }
    }

    public void startTrdVgmReal() {
        if (setting.getOutputDevice().getDeviceType() == Common.DEV_Null) {
logger.log(Level.INFO, "dev null:" + getClass().getName());
            return;
        }

        audio.trdClosed = false;
        audio.trdMain = new Thread(this::trdVgmRealFunction);
        audio.trdMain.setPriority(Thread.MAX_PRIORITY);
        audio.trdMain.setDaemon(true);
        audio.trdMain.setName("trdVgmReal");
        audio.trdMain.start();
    }

    public void changeChipSampleRate(MDSound.Chip chip, int newSmplRate) {
        MDSound.Chip caa = chip;

        if (caa.samplingRate == newSmplRate)
            return;

        // quick and dirty hack to make sample rate changes work
        caa.samplingRate = newSmplRate;
        if (caa.samplingRate < setting.getOutputDevice().getSampleRate())
            caa.resampler = 0x01;
        else if (caa.samplingRate == setting.getOutputDevice().getSampleRate())
            caa.resampler = 0x02;
        else if (caa.samplingRate > setting.getOutputDevice().getSampleRate())
            caa.resampler = 0x03;
        caa.smpP = 1;
        caa.smpNext -= caa.smpLast;
        caa.smpLast = 0x00;
    }

    public void go() {
        audio.stopped = false;
//logger.log(Level.TRACE, "stopped: " + audio.stopped + ", " + audio.hashCode());
    }

    @Override
    public void stop() {
        audio.stop();
    }

    protected void resetFadeOutParam() {
        audio.vgmFadeout = false;
        audio.vgmFadeoutCounter = 1.0;
        audio.vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        vgmRealFadeoutVol = 0;
        vgmRealFadeoutVolWait = 4;

        audio.chipRegister.clearFadeoutVolume();

        audio.chipRegister.reset();
    }

    protected void seqDie() {
        close();
        realChipClose();
    }

    public void setVGMBuffer(FileFormat format, byte[] srcBuf, String playingFileName, String playingArcFileName, int midiMode, int songNo, List<Tuple<String, byte[]>> extFile) {
        //stop();
        audio.playingFileFormat = format;
        vgmBuf = srcBuf;
        this.playingFileName = playingFileName; // for WaveWriter
        this.playingArcFileName = playingArcFileName;
        this.midiMode = midiMode;
        this.songNo = songNo;
        audio.chipRegister.plugin(MidiPlugin.class).setFileName(playingFileName); // for ExportMIDI
        extendFile = extFile; // Additional files
        Common.playingFilePath = Path.of(playingFileName).getParent();

        if (audio.naudioFileReader != null) {
            audio.nAudioStop();
        }

        if (format instanceof WAVFileFormat || format instanceof MP3FileFormat || format instanceof AIFFFileFormat) {
            audio.naudioFileName = playingFileName;
        } else {
            audio.naudioFileName = null;
        }
    }

    @Override
    public void ff() {
        if (audio.driverVirtual == null) return;
        vgmSpeed = (vgmSpeed == 1) ? 4 : 1;
        audio.driverVirtual.vgmSpeed = vgmSpeed;
        if (audio.driverReal != null) audio.driverReal.vgmSpeed = vgmSpeed;
    }

    public void slow() {
        vgmSpeed = (vgmSpeed == 1) ? 0.25 : 1;
        audio.driverVirtual.vgmSpeed = vgmSpeed;
        if (audio.driverReal != null) audio.driverReal.vgmSpeed = vgmSpeed;
    }

    public void resetSlow() {
        vgmSpeed = 1;
        audio.driverVirtual.vgmSpeed = vgmSpeed;
        if (audio.driverReal != null) audio.driverReal.vgmSpeed = vgmSpeed;
    }

    public boolean isStopped() {
        return audio.stopped;
    }

    public boolean isFadeOut() {
        return audio.vgmFadeout;
    }

    public boolean isSlow() {
        return !isStopped() && (vgmSpeed < 1.0);
    }

    public boolean isFF() {
        return !isStopped() && (vgmSpeed > 1.0);
    }

    public void stepPlay(int Step) {
        audio.stepCounter = Step;
    }

    public void closeWaveWriter() {
        audio.waveWriter.close();
    }

    @Override
    public void close() {
        stop();
        audio.close();
    }

    public void resetTimeCounter() {
        if (audio.driverVirtual == null && audio.driverReal == null) return;
        if (audio.driverVirtual != null) {
            audio.driverVirtual.counter = 0;
            audio.driverVirtual.totalCounter = 0;
            audio.driverVirtual.loopCounter = 0;
        }

        if (audio.driverReal != null) {
            audio.driverReal.counter = 0;
            audio.driverReal.totalCounter = 0;
            audio.driverReal.loopCounter = 0;
        }
    }

    public long getCounter() {
        if (audio.driverVirtual == null && audio.driverReal == null) return -1;

        if (audio.driverVirtual == null) return audio.driverReal.counter;
        if (audio.driverReal == null) return audio.driverVirtual.counter;

        return Math.max(audio.driverVirtual.counter, audio.driverReal.counter);
    }

    public long getTotalCounter() {
        if (audio.driverVirtual == null) return -1;

        return audio.driverVirtual.totalCounter;
    }

    public long getLoopCounter() {
        if (audio.driverVirtual == null) return -1;

        return audio.driverVirtual.loopCounter;
    }

    public void updateVol() {
        audio.chipRegister.updateVol();
    }

    public int getVgmCurLoopCounter() {
        int cnt = 0;

        if (audio.driverVirtual != null) {
            cnt = audio.driverVirtual.vgmCurLoop;
        }
        if (audio.driverReal != null) {
            cnt = Math.min(audio.driverReal.vgmCurLoop, cnt);
        }

        return cnt;
    }

    public boolean getVGMStopped() {
        boolean v;
        boolean r;

        v = audio.driverVirtual == null || audio.driverVirtual.stopped;
        r = audio.driverReal == null || audio.driverReal.stopped;
        return v && r;
    }

    public boolean getIsDataBlock(Common.EnmModel model) {

        if (model == Common.EnmModel.VirtualModel) {
            if (audio.driverVirtual == null) return false;
            return audio.driverVirtual.isDataBlock;
        } else {
            if (audio.driverReal == null) return false;
            return audio.driverReal.isDataBlock;
        }
    }

    public boolean getIsPcmRAMWrite(Common.EnmModel model) {
        if (model == Common.EnmModel.VirtualModel) {
            if (audio.driverVirtual == null) return false;
            if (!(audio.driverVirtual instanceof Vgm)) return false;
            return ((Vgm) audio.driverVirtual).isPcmRAMWrite;
        } else {
            if (audio.driverReal == null) return false;
            if (!(audio.driverReal instanceof Vgm)) return false;
            return ((Vgm) audio.driverReal).isPcmRAMWrite;
        }
    }

    public long getVirtualFrameCounter() {
        if (audio.driverVirtual == null) return -1;
        return audio.driverVirtual.vgmFrameCounter;
    }

    public long getRealFrameCounter() {
        if (audio.driverReal == null) return -1;
        return audio.driverReal.vgmFrameCounter;
    }
}
