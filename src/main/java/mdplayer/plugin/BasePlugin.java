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
import mdplayer.ChipLEDs;
import mdplayer.ChipRegister;
import mdplayer.Common;
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
import mdplayer.driver.BaseDriver;
import mdplayer.driver.VgmDriver;
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
public abstract class BasePlugin<T extends BaseDriver> implements Plugin {

    private static final Logger logger = getLogger(BasePlugin.class.getName());

    protected final Setting setting = Setting.getInstance();

    public final Audio audio = Audio.getInstance();

    public final MDSound mds;

    public final ChipRegister chipRegister;

    // TODO driver should be one, instruments should be separated virtual and real
    public T driverVirtual = null;

    public T driverReal = null;

    // view
    public final ChipLEDs chipLED = new ChipLEDs();

    protected byte[] vgmBuf = null;
    protected double vgmSpeed;

    protected boolean oneTimeReset = false;

    protected FileFormat fileFormat;
    protected String playingFileName;
    protected String playingArcFileName;
    protected int midiMode = 0;
    protected int songNo = 0;
    protected List<Tuple<String, byte[]>> extendFiles = null;

    public boolean flgReinit = false;

    protected int vgmRealFadeoutVol = 0;
    protected int vgmRealFadeoutVolWait = 4;

    protected int hiyorimiEven = 0;
    public boolean hiyorimiNecessary = false;

    // TODO variable?
    public static final int BUFFER_SIZE = 1024;

    protected static void sleep(int i) {
        try { Thread.sleep(i); } catch (InterruptedException ignore) {}
    }

    /** used chips */
    protected final Map<Class<? extends mdplayer.Chip>, List<Chip>> chips = new HashMap<>();

    /** put used chips */
    protected void put(Class<? extends mdplayer.Chip> chip, Chip info) {
        if (chips.containsKey(chip)) {
            chips.get(chip).add(info);
        } else {
            chips.put(chip, new ArrayList<>(List.of(info)));
        }
    }

    /** check a chip existence */
    protected boolean contains(Class<? extends mdplayer.Chip> chip, int index) {
        var infos = chips.get(chip);
        return infos != null && index < infos.size();
    }

    /** list for MDSound */
    protected List<Chip> flatten() {
        return chips.values().stream().flatMap(Collection::stream).toList();
    }

    public boolean getEmuOnly() {
        return false;
    }

    protected BasePlugin() {
        mds = new MDSound(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);

        chipRegister = new ChipRegister();
        chipRegister.init(this);

        init();
    }

    @Override
    public void init() {
        audio.plugin = this;
        audio.waveWriter = new WaveWriter();

        audio.paused = false;
        audio.stopped = true;
logger.log(Level.TRACE, "stop: " + audio.stopped + ", " + audio.hashCode());
//        audio._fatalError = false;
        oneTimeReset = false;

        // midi out released
        chipRegister.plugin(MidiPlugin.class).releaseAll();

        audio.naudioWrap.start();
    }

    protected void trdVgmRealFunction() {

        if (driverReal == null) { // first time, driverReal must be null
            audio.trdClosed = true;
            audio.setTrdStopped(true);
            return;
        }

        double o = System.currentTimeMillis() / Audio.swFreq;
        double step = 1 / (double) setting.getOutputDevice().getSampleRate();
        audio.setTrdStopped(false);
        try {
            while (!audio.trdClosed) {
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

                if (audio.stopped || audio.paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (hiyorimiNecessary && driverVirtual.isDataBlock) {
                    continue;
                }

                if (audio.vgmFadeout) {
                    if (vgmRealFadeoutVol != 1000) vgmRealFadeoutVolWait--;
                    if (vgmRealFadeoutVolWait == 0) {
                        if (contains(Ym2151Chip.class, 0)) chipRegister.chip(Ym2151Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2203Chip.class, 0)) chipRegister.chip(Ym2203Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ay8910Chip.class, 0)) chipRegister.chip(Ay8910Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2413Chip.class, 0)) chipRegister.chip(Ym2413Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2608Chip.class, 0)) chipRegister.chip(Ym2608Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2610Chip.class, 0)) chipRegister.chip(Ym2610Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym2612Chip.class, 0)) chipRegister.chip(Ym2612Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym3526Chip.class, 0)) chipRegister.chip(Ym3526Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Ym3812Chip.class, 0)) chipRegister.chip(Ym3812Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(Sn76489Chip.class, 0)) chipRegister.chip(Sn76489Chip.class).setFadeout(0, vgmRealFadeoutVol);
                        if (contains(YmF262Chip.class, 0)) chipRegister.chip(YmF262Chip.class).setFadeout(0, vgmRealFadeoutVol);

                        if (contains(Ym2151Chip.class, 1)) chipRegister.chip(Ym2151Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2203Chip.class, 1)) chipRegister.chip(Ym2203Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ay8910Chip.class, 1)) chipRegister.chip(Ay8910Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2413Chip.class, 1)) chipRegister.chip(Ym2413Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2608Chip.class, 1)) chipRegister.chip(Ym2608Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2610Chip.class, 1)) chipRegister.chip(Ym2610Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym2612Chip.class, 1)) chipRegister.chip(Ym2612Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym3526Chip.class, 1)) chipRegister.chip(Ym3526Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Ym3812Chip.class, 1)) chipRegister.chip(Ym3812Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(Sn76489Chip.class, 1)) chipRegister.chip(Sn76489Chip.class).setFadeout(1, vgmRealFadeoutVol);
                        if (contains(YmF262Chip.class, 1)) chipRegister.chip(YmF262Chip.class).setFadeout(1, vgmRealFadeoutVol);

                        vgmRealFadeoutVol++;

                        vgmRealFadeoutVol = Math.min(127, vgmRealFadeoutVol);
                        if (vgmRealFadeoutVol == 127) {
//                            if (SoundChip.realChip != null) {
//                                softReset(EnmModel.RealModel);
//                            }
                            vgmRealFadeoutVolWait = 1000;
                            chipRegister.plugin(MidiPlugin.class).resetAll();
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
                    double abs = Math.abs((driverReal.vgmFrameCounter - dSCCI) - (driverVirtual.vgmFrameCounter - dEMU));
                    int m = 0;
                    long l = getLatency() / 10;
                    if (abs >= l) {
                        m = ((driverReal.vgmFrameCounter - dSCCI) > (driverVirtual.vgmFrameCounter - dEMU)) ? 1 : 2;
                    }

                    switch (m) {
                    case 0: // x1
                        driverReal.processOneFrame();
                        break;
                    case 1: // x1/2
                        hiyorimiEven++;
                        if (hiyorimiEven > 1) {
                            driverReal.processOneFrame();
                            hiyorimiEven = 0;
                        }
                        break;
                    case 2: // x2
                        driverReal.processOneFrame();
                        driverReal.processOneFrame();
                        break;
                    }
                } else {
                    driverReal.processOneFrame();
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

    public void prepare() {
        audio.vgmFadeout = false;
        audio.vgmFadeoutCounter = 1.0;
        audio.vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        vgmRealFadeoutVol = 0;
        vgmRealFadeoutVolWait = 4;

        chips.clear();
        hiyorimiNecessary = setting.getHiyorimiMode();
        resetFadeOutParam();

        chipRegister.reset();
        chipRegister.clearFadeoutVolume();
        chipLED.clear();
        masterVolume = setting.getBalance().getMasterVolume();
    }

    /** */
    public boolean play() {
        prepare();
//logger.log(Level.TRACE, "play: " + audio.stopped + ", " + audio.hashCode());
        audio.errMsg = "";

        stop();

        sleep(500);

        audio.paused = false;
        audio.stopped = false;

        oneTimeReset = false;

//        if (trd == null) {
//            trd = new Thread(this::trdIF);
//            trd.setPriority(Thread.NORM_PRIORITY);
//            trd.start();
//        }

        go();

        try {
            audio.waveWriter.open(playingFileName);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            audio.errMsg = "wave file open error.";
            return false;
        }

logger.log(Level.DEBUG, "driver: " + driverVirtual.getClass().getSimpleName());
        while (true) {
//logger.log(Level.TRACE, "loop HERE");
            short[] buffer = new short[4];

            int r = audio.update(buffer, 0, buffer.length);
            if (r == -1) break;
            audio.naudioWrap.write(buffer, 0, buffer.length);
            Thread.yield();
        }

        return false;
    }

    public void startTrdVgmReal() {
        if (setting.getOutputDevice().getDeviceType() == Common.DEV_Null) {
logger.log(Level.INFO, "dev null: " + getClass().getName());
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
logger.log(Level.INFO, "stop enter: " + audio.stopped);
        if (!audio.stopped) {
            audio.stop();
        }
    }

    protected void resetFadeOutParam() {
        audio.vgmFadeout = false;
        audio.vgmFadeoutCounter = 1.0;
        audio.vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        vgmRealFadeoutVol = 0;
        vgmRealFadeoutVolWait = 4;

        chipRegister.clearFadeoutVolume();

        chipRegister.reset();
    }

    public void seqDie() {
        close();
        realChipClose();
    }

    public void setBuffer(FileFormat format, byte[] srcBuf, String playingFileName, String playingArcFileName, int midiMode, int songNo, List<Tuple<String, byte[]>> extFile) {
        //stop();
        this.fileFormat = format;
        audio.playingFileFormat = format;
        vgmBuf = srcBuf;
        this.playingFileName = playingFileName; // for WaveWriter
        this.playingArcFileName = playingArcFileName;
        this.midiMode = midiMode;
        this.songNo = songNo;
        chipRegister.plugin(MidiPlugin.class).setFileName(playingFileName); // for ExportMIDI
        extendFiles = extFile; // Additional files
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
        if (driverVirtual == null) return;
        vgmSpeed = (vgmSpeed == 1) ? 4 : 1;
        driverVirtual.vgmSpeed = vgmSpeed;
        if (driverReal != null) driverReal.vgmSpeed = vgmSpeed;
    }

    public void slow() {
        vgmSpeed = (vgmSpeed == 1) ? 0.25 : 1;
        driverVirtual.vgmSpeed = vgmSpeed;
        if (driverReal != null) driverReal.vgmSpeed = vgmSpeed;
    }

    public void resetSlow() {
        vgmSpeed = 1;
        driverVirtual.vgmSpeed = vgmSpeed;
        if (driverReal != null) driverReal.vgmSpeed = vgmSpeed;
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
logger.log(Level.INFO, "close enter");
        stop();
        audio.close();
    }

    public void resetTimeCounter() {
        if (driverVirtual == null && driverReal == null) return;
        if (driverVirtual != null) {
            driverVirtual.counter = 0;
            driverVirtual.totalCounter = 0;
            driverVirtual.loopCounter = 0;
        }

        if (driverReal != null) {
            driverReal.counter = 0;
            driverReal.totalCounter = 0;
            driverReal.loopCounter = 0;
        }
    }

    public long getCounter() {
        if (driverVirtual == null && driverReal == null) return -1;

        if (driverVirtual == null) return driverReal.counter;
        if (driverReal == null) return driverVirtual.counter;

        return Math.max(driverVirtual.counter, driverReal.counter);
    }

    public long getTotalCounter() {
        if (driverVirtual == null) return -1;

        return driverVirtual.totalCounter;
    }

    public long getLoopCounter() {
        if (driverVirtual == null) return -1;

        return driverVirtual.loopCounter;
    }

    public void updateVol() {
        chipRegister.updateVol();
    }

    public int getVgmCurLoopCounter() {
        int cnt = 0;

        if (driverVirtual != null) {
            cnt = driverVirtual.vgmCurLoop;
        }
        if (driverReal != null) {
            cnt = Math.min(driverReal.vgmCurLoop, cnt);
        }

        return cnt;
    }

    public boolean getVGMStopped() {
        boolean v;
        boolean r;

        v = driverVirtual == null || driverVirtual.stopped;
        r = driverReal == null || driverReal.stopped;
        return v && r;
    }

    public boolean getIsDataBlock(Common.EnmModel model) {

        if (model == Common.EnmModel.VirtualModel) {
            if (driverVirtual == null) return false;
            return driverVirtual.isDataBlock;
        } else {
            if (driverReal == null) return false;
            return driverReal.isDataBlock;
        }
    }

    public boolean getIsPcmRAMWrite(Common.EnmModel model) {
        if (model == Common.EnmModel.VirtualModel) {
            if (driverVirtual == null) return false;
            if (!(driverVirtual instanceof VgmDriver)) return false;
            return ((VgmDriver) driverVirtual).isPcmRAMWrite();
        } else {
            if (driverReal == null) return false;
            if (!(driverReal instanceof VgmDriver)) return false;
            return ((VgmDriver) driverReal).isPcmRAMWrite();
        }
    }

    public long getVirtualFrameCounter() {
        if (driverVirtual == null) return -1;
        return driverVirtual.vgmFrameCounter;
    }

    public long getRealFrameCounter() {
        if (driverReal == null) return -1;
        return driverReal.vgmFrameCounter;
    }

    @Override
    public BaseDriver getDriver() {
        return driverVirtual;
    }

    protected abstract void initChips();

    public long getDriverCounter() {
        if (driverVirtual == null && driverReal == null) return -1;

        if (driverVirtual == null) {
            return driverReal.getDriverCounter();
        }
        if (driverReal == null) {
            return driverVirtual.getDriverCounter();
        }

        return driverVirtual.whichCounter(driverReal.getDriverCounter(), driverVirtual.getDriverCounter());
    }

    public void setVolume(String tag, Class<? extends mdplayer.Chip> c, boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getVolume(tag, c)) + volume, -192, 20);
            mds.setVolume(tag, chipRegister.chip(c).inst(0), v); // TODO vavi
            setting.getBalance().setVolume(tag, c, v);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public int masterVolume = 0;

    public void setMasterVolume(boolean isAbs, int volume) {
        masterVolume = Common.range((isAbs ? 0 : setting.getBalance().getMasterVolume()) + volume, -192, 20);
        setting.getBalance().setMasterVolume(masterVolume);
    }
}
