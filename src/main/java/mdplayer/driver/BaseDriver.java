
package mdplayer.driver;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.Ym2151Chip;
import mdplayer.plugin.BasePlugin;


public abstract class BaseDriver {

    protected static final Setting setting = Setting.getInstance();

    public double vgmSpeed = 1;

    protected double vgmSpeedCounter;

    public long counter = 0;

    public long totalCounter = 0;

    public long loopCounter = 0;

    public int vgmCurLoop = 0;

    public boolean stopped = false;

    public int vgmFrameCounter;

    // TODO generalize, e.g. MusicTag
    public Vgm.Gd3 gd3 = new Vgm.Gd3();

    protected String version = "";

    protected String usedChips = "";

    protected int vstDelta = 0;

    public boolean isDataBlock = false;

    protected byte[] vgmBuf = null;

    protected BasePlugin plugin;

    protected EnmModel model = EnmModel.VirtualModel;

    protected Class<? extends Chip>[] useChip = new Class[] {
        null,
    };

    protected int latency = 1000;

    protected int waitTime = 0;

    public abstract void init(byte[] vgmBuf,
                              BasePlugin plugin,
                              EnmModel model,
                              Class<? extends Chip>[] useChip,
                              int latency,
                              int waitTime,
                              Object... args);

    public abstract void processOneFrame();

    public Vgm.Gd3 getGD3Info(byte[] buf) {
        return getGD3Info(buf, new int[1]);
    }

    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        return getGD3Info(buf, new int[] {vgmGd3});
    }

    public abstract Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3);

    public int render(short[] buffer, int offset, int sampleCount) {
        if (plugin.hiyorimiNecessary && plugin.driverReal != null && plugin.driverReal.isDataBlock)
            return plugin.mds.update(buffer, offset, sampleCount, null);

        if (plugin.audio.stepCounter > 0) {
            plugin.audio.stepCounter -= sampleCount;
            if (plugin.audio.stepCounter <= 0) {
                plugin.audio.paused = true;
                plugin.audio.stepCounter = 0;
                return plugin.mds.update(buffer, offset, sampleCount, null);
            }
        }

//        driverVirtual.vstDelta = 0;
//        stwh.reset();
//        stwh.start();
//logger.log(Level.TRACE, "driver: " + driverVirtual.getClass().getSimpleName());
        int cnt = plugin.mds.update(buffer, offset, sampleCount, plugin.driverVirtual::processOneFrame);
        plugin.audio.procTimePer1Frame = (int) ((double) System.currentTimeMillis() / (sampleCount + 1) * 1000000.0);
        return cnt;
    }

    public long getDriverCounter() {
        return 0;
    }

    public void copyWaveBuffer(short[][] dest) {
        plugin.mds.visWaveBuffer.copy(dest);
    }

    public long whichCounter(long real, long virtual) {
        return 0;
    }

    public boolean isNotRenderingOnPause() {
        return setting.getOther().getNonRenderingForPause();
    }
}
