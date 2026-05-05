
package mdplayer.driver;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
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

    public void copyWaveBuffer(short[][] dest) {
        plugin.mds.visWaveBuffer.copy(dest);
    }

    // default
    public long whichCounter(long real, long virtual) {
        return 0;
    }

    public boolean isNotRenderingOnPause() {
        return setting.getOther().getNonRenderingForPause();
    }

    protected final GenericSupport genericSupport = new GenericSupport();

    public void addGenericListener(GenericListener listener) {
        genericSupport.addGenericListener(listener);
    }
}
