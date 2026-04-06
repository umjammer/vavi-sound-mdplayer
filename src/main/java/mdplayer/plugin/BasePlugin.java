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
import mdplayer.ChipLEDs;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.RealChipPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.MDSound.Chip;

import static java.lang.System.getLogger;


/**
 * BasePlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public abstract class BasePlugin<T extends BaseDriver> implements Plugin {

    private static final Logger logger = getLogger(BasePlugin.class.getName());

    protected final Setting setting = Setting.getInstance();

    public final MDSound mds;

    public final ChipRegister chipRegister;

    // TODO driver should be one, instruments should be separated virtual and real
    public T driverVirtual = null;

    public T driverReal = null;

    // view
    public final ChipLEDs chipLED = new ChipLEDs();

    protected byte[] vgmBuf = null;
    protected double vgmSpeed;

    public boolean oneTimeReset = false;

    protected FileFormat fileFormat;
    public FileFormat playingFileFormat;

    public int procTimePer1Frame = 0;
    public int stepCounter = 0;

    public String playingFileName;
    public String playingArcFileName;
    protected int midiMode = 0;
    protected int songNo = 0;
    protected List<Tuple<String, byte[]>> extendFiles = null;

    public boolean flgReinit = false;
    public boolean stopped = false;
    public boolean paused = false;

    public boolean vgmFadeout;
    public double vgmFadeoutCounter;
    public double vgmFadeoutCounterV;
    protected int vgmRealFadeoutVol = 0;
    protected int vgmRealFadeoutVolWait = 4;

    public int hiyorimiEven = 0;
    public boolean hiyorimiNecessary = false;

    // TODO variable?
    public static final int BUFFER_SIZE = 1024;

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

    protected BasePlugin() {
        mds = new MDSound(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);

        chipRegister = new ChipRegister();
        chipRegister.init(this);

        init();
    }

    @Override
    public void init() {
        oneTimeReset = false;

        // midi out released
        chipRegister.plugin(MidiPlugin.class).releaseAll();

        this.paused = false;
        this.stopped = true;
logger.log(Level.TRACE, "stop: " + this.stopped + ", " + this.hashCode());
    }

    public void fadeOut() {
        if (vgmRealFadeoutVol != 1000) vgmRealFadeoutVolWait--;
        if (vgmRealFadeoutVolWait == 0) {
            chips.keySet().forEach(c -> chipRegister.chip(c).setFadeout(0, vgmRealFadeoutVol));
            chips.keySet().forEach(c -> chipRegister.chip(c).setFadeout(1, vgmRealFadeoutVol));

            vgmRealFadeoutVol++;

            vgmRealFadeoutVol = Math.min(127, vgmRealFadeoutVol);
            if (vgmRealFadeoutVol == 127) {
//                if (SoundChip.realChip != null) {
//                    softReset(EnmModel.RealModel);
//                }
                vgmRealFadeoutVolWait = 1000;
                chipRegister.plugin(MidiPlugin.class).resetAll();
            } else {
                vgmRealFadeoutVolWait = 700 - vgmRealFadeoutVol * 2;
            }
        }
    }

    public int getLatency() {
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_AsioOut) {
            return setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getLatency() / 1000;
        }
        return 0; // naudioWrap.getAsioLatency(); TODO
    }

    public void prepare() {
        this.vgmFadeout = false;
        this.vgmFadeoutCounter = 1.0;
        this.vgmFadeoutCounterV = 0.00001;
        this.vgmSpeed = 1;
        this.vgmRealFadeoutVol = 0;
        this.vgmRealFadeoutVolWait = 4;

        chips.clear();
        this.hiyorimiNecessary = setting.getHiyorimiMode();
        resetFadeOutParam();

        chipRegister.reset();
        chipRegister.clearFadeoutVolume();
        chipLED.clear();
        masterVolume = setting.getBalance().getMasterVolume();
    }

    public void changeChipSampleRate(MDSound.Chip chip, int newSmplRate) {

        if (chip.samplingRate == newSmplRate)
            return;

        // quick and dirty hack to make sample rate changes work
        chip.samplingRate = newSmplRate;
        if (chip.samplingRate < setting.getOutputDevice().getSampleRate())
            chip.resampler = 0x01;
        else if (chip.samplingRate == setting.getOutputDevice().getSampleRate())
            chip.resampler = 0x02;
        else if (chip.samplingRate > setting.getOutputDevice().getSampleRate())
            chip.resampler = 0x03;
        chip.smpP = 1;
        chip.smpNext -= chip.smpLast;
        chip.smpLast = 0x00;
    }

    public void go() {
        this.stopped = false;
//logger.log(Level.TRACE, "stopped: " + audio.stopped + ", " + audio.hashCode());
    }

    @Override
    public void stop() {
logger.log(Level.INFO, "stop enter: " + this.stopped);
        if (!this.stopped) {
            this.stopped = true;
        }
    }

    protected void resetFadeOutParam() {
        this.vgmFadeout = false;
        this.vgmFadeoutCounter = 1.0;
        this.vgmFadeoutCounterV = 0.00001;
        this.vgmSpeed = 1;
        this.vgmRealFadeoutVol = 0;
        this.vgmRealFadeoutVolWait = 4;

        chipRegister.clearFadeoutVolume();

        chipRegister.reset();
    }

    public void seqDie() {
        close();
        chipRegister.plugin(RealChipPlugin.class).realChipClose();
    }

    public void setBuffer(FileFormat format, byte[] srcBuf, String playingFileName, String playingArcFileName, int midiMode, int songNo, List<Tuple<String, byte[]>> extFile) {
        //stop();
        this.fileFormat = format;
        this.playingFileFormat = format;
        this.vgmBuf = srcBuf;
        this.playingFileName = playingFileName; // for WaveWriter
        this.playingArcFileName = playingArcFileName;
        this.midiMode = midiMode;
        this.songNo = songNo;
        chipRegister.plugin(MidiPlugin.class).setFileName(playingFileName); // for ExportMIDI
        extendFiles = extFile; // Additional files
        Common.playingFilePath = Path.of(playingFileName).getParent();
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
        return this.stopped;
    }

    public boolean isFadeOut() {
        return this.vgmFadeout;
    }

    public boolean isSlow() {
        return !isStopped() && (vgmSpeed < 1.0);
    }

    public boolean isFF() {
        return !isStopped() && (vgmSpeed > 1.0);
    }

    @Override
    public void close() {
logger.log(Level.INFO, "close enter");
        stop();
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

    public boolean isDataBlock(Common.EnmModel model) {

        if (model == Common.EnmModel.VirtualModel) {
            if (driverVirtual == null) return false;
            return driverVirtual.isDataBlock;
        } else {
            if (driverReal == null) return false;
            return driverReal.isDataBlock;
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
