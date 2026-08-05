
package mdplayer.driver;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import musicDriverInterface.MetaData;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;
import vavi.util.event.GenericSupport;


public abstract class BaseDriver {

    protected static final Setting setting = Setting.getInstance();

    public double speed = 1;
    protected double speedCounter;
    public long counter = 0;
    public long totalCounter = 0;
    public long loopCounter = 0;
    public int curLoop = 0;
    public boolean stopped = false;
    public int frameCounter;

    public MetaData metaData = new MetaData();

    protected String version = "";
    protected String usedChips = "";

    public boolean isDataBlock = false;

    protected byte[] dataBuf;

    protected BasePlugin<? extends BaseDriver> plugin;

    protected EnmModel model = EnmModel.VirtualModel;

    protected int latency = 1000;
    protected int waitTime = 0;

    protected BaseDriver(BasePlugin<? extends BaseDriver> plugin) {
        this.plugin = plugin;
        this.dataBuf = plugin != null ? plugin.getData() : null; // gross
    }

    /** */
    public abstract void init(EnmModel model, int latency, int waitTime, Object... args);

    /** advances the clock */
    public abstract void processOneFrame();

    /** gets the metadata */
    public abstract MetaData getMetaData(byte[] buf, Object... args);

    /**
     * The lines the fmdsp comment area is to show, laid out the way the file has them - a memo
     * that is a screen image places its text with spaces, which the metadata is stripped of.
     *
     * @return null when the file has no such memo, leaving the caller to use the metadata
     */
    public String[] comments() {
        return null;
    }

    /**
     * The PCM type label for slot {@code index} (e.g. "PPC", "PPZ1", "PDX", "PVI", etc.).
     * TODO view -> view.plugin
     */
    public String pcmType(int index) {
        String fn = pcmFilename(index);
        if (fn != null) {
            int dot = fn.lastIndexOf('.');
            if (dot >= 0 && dot < fn.length() - 1) {
                return fn.substring(dot + 1).toUpperCase();
            }
        }
        return switch (index) {
            case 0 -> "PCM1";
            case 1 -> "PCM2";
            default -> null;
        };
    }

    /**
     * The PCM filename for slot {@code index}, or null when the driver has nothing for that slot.
     * TODO view -> view.plugin
     */
    public String pcmFilename(int index) {
        if (plugin != null) {
            var ex = plugin.getExtendFiles();
            if (ex != null && index >= 0 && index < ex.size()) {
                return ex.get(index).getItem1();
            }
        }
        return null;
    }

    /**
     * Whether the PCM file for slot {@code index} failed to load. Drivers that load
     * PCM data override this.
     * TODO view -> view.plugin
     */
    public boolean pcmError(int index) {
        return false;
    }

    /** renders the audio */
    public int render(short[] buffer, int offset, int sampleCount) {
        if (plugin.chipRegister.plugin(RealChipPlugin.class).isHiyorimiNecessary() && plugin.driverReal != null && plugin.driverReal.isDataBlock)
            return plugin.mds.update(buffer, offset, sampleCount, null);

        if (plugin.stepCounter > 0) {
            plugin.stepCounter -= sampleCount;
            if (plugin.stepCounter <= 0) {
                plugin.paused = true;
                plugin.stepCounter = 0;
                return plugin.mds.update(buffer, offset, sampleCount, null);
            }
        }

        plugin.chipRegister.plugin(VstPlugin.class).vstDelta = 0;
//logger.log(Level.TRACE, "driver: " + driverVirtual.getClass().getSimpleName());
        int cnt = plugin.mds.update(buffer, offset, sampleCount, plugin.driverVirtual::processOneFrame);
        plugin.procTimePer1Frame = (int) ((double) System.currentTimeMillis() / (sampleCount + 1) * 1000000.0);
        return cnt;
    }

    // default
    public long getDriverCounter() {
        return 0;
    }

    // default
    public long whichCounter(long real, long virtual) {
        return 0;
    }

    public boolean isNotRenderingOnPause() {
        return setting.getOther().getNonRenderingForPause();
    }

    protected final GenericSupport viewSupport = new GenericSupport();

    public void addViewListener(GenericListener listener) {
        viewSupport.addGenericListener(listener);
    }

    /**
     * Fires a view event.
     *
     * @param name
     *        "led.reset" ... none
     *        "led.set" ... none, {@code src} is indicated the led target
     *        "led.on" ... args 0: chip id, {@code src} is indicated the led target
     *        "wave.buffer" ... args 0: left value, 1: right value
     */
    public void fireEventHappened(Object src, String name, Object... args) {
        viewSupport.fireEventHappened(new GenericEvent(src, name, args));
    }

    public String getName() {
        return getClass().getSimpleName().replace("Driver", "").toUpperCase();
    }
}
