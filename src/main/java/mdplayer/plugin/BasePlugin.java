package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.MPcmChip;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.RealChipPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdsound.MDSound;
import mdsound.MDSound.Chip;
import vavi.util.compat.Tuple;

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

    public interface Compilable {
        void compile();
    }

    /** for spi, 0 origin */
    public interface HasSongNo {
        void setSongNo(int songNo);
    }

    public final MDSound mds;

    public final ChipRegister chipRegister;

    // TODO driver should be one, instruments should be separated virtual and real
    public T driverVirtual = null;

    public T driverReal = null;

    protected byte[] dataBuf = null;
    protected double speed;

    public boolean oneTimeReset = false;

    protected FileFormat fileFormat;
    public FileFormat playingFileFormat;

    public int procTimePer1Frame = 0;
    public int stepCounter = 0;

    public String playingFileName;
    public String playingArcFileName;
    protected int songNo = 0;
    protected List<Tuple<String, byte[]>> extendFiles = null;

    public boolean flgReinit = false;
    public boolean stopped = false;
    public boolean paused = false;

    public boolean fadeout;
    public double fadeoutCounter;
    public double fadeoutCounterV;

    // TODO variable?
    public static final int BUFFER_SIZE = 1024;

    public byte[] getData() {
        return dataBuf;
    }

    /** used chips */
    protected final Map<Class<? extends mdplayer.Chip>, List<Chip>> chips = new HashMap<>();

    public final Set<Class<? extends mdplayer.Chip>> getChips() {
        return chips.keySet();
    }

    /** put used chips */
    protected void put(Class<? extends mdplayer.Chip> chip, Chip info) {
        if (chips.containsKey(chip)) {
            chips.get(chip).add(info);
        } else {
            chips.put(chip, new ArrayList<>(List.of(info)));
        }

        // view
        getDriver().fireEventHappened(chipRegister.chip(chip), "led.set", info.id);
    }

    /** check a chip existence */
    public boolean contains(Class<? extends mdplayer.Chip> chip) {
        return chips.get(chip) != null;
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
        mds = new MDSound();

        chipRegister = new ChipRegister();

        init();
    }

    @Override
    public void init() {
        oneTimeReset = false;

        chipRegister.init(this);
        // midi out released
        chipRegister.plugin(MidiPlugin.class).releaseAll();

        this.paused = false;
        this.stopped = true;
        this.fadeout = false;
logger.log(Level.TRACE, "stop: " + this.stopped + ", " + this.hashCode());
    }

    public int getLatency() {
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_AsioOut) {
            return setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getLatency() / 1000;
        }
        return 0; // naudioWrap.getAsioLatency(); TODO
    }

    /** {@code super#prepare()} must be called inside inherited this method */
    public void prepare() {
        // The chip and sub-plugin instances are shared singletons (loaded once via
        // ServiceLoader and reused by every format plugin's ChipRegister), so their
        // owning `context` is only pointed at THIS plugin in the constructor. When a
        // previously-constructed plugin is replayed after another one has played, the
        // shared context is left pointing at that other plugin, breaking chip
        // registration/MIDI routing. Re-point it to the active plugin each song.
        chipRegister.init(this);

        this.fadeout = false;
        this.fadeoutCounter = 1.0;
        this.fadeoutCounterV = 0.00001;
        this.speed = 1;
        chipRegister.plugin(RealChipPlugin.class).realFadeoutVol = 0;
        chipRegister.plugin(RealChipPlugin.class).realFadeoutVolWait = 4;

        chips.clear();
        resetFadeOutParam();

        chipRegister.reset();
        chipRegister.clearFadeoutVolume();
        masterVolume = setting.getBalance().getMasterVolume();

        getDriver().fireEventHappened(this, "led.reset");
    }

    @Override
    public void stop() {
logger.log(Level.TRACE, "stop enter: " + this.stopped);
        if (!this.stopped) {
            this.stopped = true;
logger.log(Level.INFO, "stop: " + this.stopped);
        }
    }

    protected void resetFadeOutParam() {
        this.fadeout = false;
        this.fadeoutCounter = 1.0;
        this.fadeoutCounterV = 0.00001;
        this.speed = 1;
        chipRegister.plugin(RealChipPlugin.class).realFadeoutVol = 0;
        chipRegister.plugin(RealChipPlugin.class).realFadeoutVolWait = 4;

        chipRegister.clearFadeoutVolume();

        chipRegister.reset();
    }

    /** @param params tags: fileName, arcFileName, midiMode, songNo */
    public void setParams(FileFormat format, Map<String, Object> params) {
        this.fileFormat = format;
        this.playingFileFormat = format;
        this.dataBuf = format.getData();
        this.playingFileName = params.get("fileName") != null ? (String) params.get("fileName") : format.getCompiledFilename();
        this.playingArcFileName = (String) params.get("arcFileName");
        chipRegister.plugin(MidiPlugin.class).midiMode = (int) params.getOrDefault("midiMode", 0);
        this.songNo = (int) params.getOrDefault("songNo", 0);
        chipRegister.plugin(MidiPlugin.class).setFileName(playingFileName); // for ExportMIDI
        extendFiles = format.getExtendFiles(); // Additional files
        Common.playingFilePath = Path.of(playingFileName).getParent(); // TODO gross

        // the YM2612 panel draws XGM songs differently; the chip carries the format for the view
        chipRegister.chip(mdplayer.chips.Ym2612Chip.class).fileFormat = format;
    }

    @Override
    public void ff() {
        if (driverVirtual == null) return;
        speed(speed == 1 ? 4 : 1);
    }

    public void slow() {
        speed(speed == 1 ? 0.25 : 1);
    }

    /** Plays at this rate, 1 being the rate the music was written at. */
    public void speed(double value) {
        if (driverVirtual == null) return;

        speed = value;
        driverVirtual.speed = speed;
        if (driverReal != null) driverReal.speed = speed;
    }

    public void resetSlow() {
        speed = 1;
        driverVirtual.speed = speed;
        if (driverReal != null) driverReal.speed = speed;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public boolean isFadeOut() {
        return this.fadeout;
    }

    public boolean isSlow() {
        return !isStopped() && (speed < 1.0);
    }

    public boolean isFF() {
        return !isStopped() && (speed > 1.0);
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
            cnt = driverVirtual.curLoop;
        }
        if (driverReal != null) {
            cnt = Math.min(driverReal.curLoop, cnt);
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
        return driverVirtual.frameCounter;
    }

    public long getRealFrameCounter() {
        if (driverReal == null) return -1;
        return driverReal.frameCounter;
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
            mdplayer.Chip chip = chipRegister.chip(c);
            if (chip != null) {
                mds.setVolume(tag, chip.inst(0), v); // TODO vavi
            }
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
