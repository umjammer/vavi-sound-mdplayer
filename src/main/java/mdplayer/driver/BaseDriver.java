
package mdplayer.driver;

import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.Ym2151Chip;
import mdplayer.plugin.BasePlugin;


public abstract class BaseDriver {

    protected static Setting setting = Setting.getInstance();

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

    public final int[] ym2151Hosei = new int[] {
        0, 0
    };

    protected byte[] vgmBuf = null;

    protected BasePlugin plugin;

    protected EnmModel model = EnmModel.VirtualModel;

    protected EnmChip[] useChip = new EnmChip[] {
        EnmChip.Unuse
    };

    protected int latency = 1000;

    protected int waitTime = 0;

    public String getErrMsg() {
        return errMsg;
    }

    public String errMsg;

    public abstract boolean init(byte[] vgmBuf,
                                 BasePlugin plugin,
                                 EnmModel model,
                                 EnmChip[] useChip,
                                 int latency,
                                 int waitTime);

    public abstract boolean init(byte[] vgmBuf,
                                 int fileType,
                                 BasePlugin plugin,
                                 EnmModel model,
                                 EnmChip[] useChip,
                                 int latency,
                                 int waitTime);

    public abstract void processOneFrame();

    public Vgm.Gd3 getGD3Info(byte[] buf) {
        return getGD3Info(buf, new int[1]);
    }

    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        return getGD3Info(buf, new int[] {vgmGd3});
    }

    public abstract Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3);

    public void setYm2151Hosei(float ym2151ClockValue) {
        for (int chipId = 0; chipId < 2; chipId++) {
            ym2151Hosei[chipId] = Common.getYM2151Hosei(ym2151ClockValue, 3579545);
            if (model == EnmModel.RealModel) {
                ym2151Hosei[chipId] = 0;
                int clock = plugin.audio.chipRegister.chip(Ym2151Chip.class).getYM2151Clock(chipId);
                if (clock != -1) {
                    ym2151Hosei[chipId] = Common.getYM2151Hosei(ym2151ClockValue, clock);
                }
            }
        }
    }

    public int render(short[] buffer, int offset, int sampleCount) {
        if (plugin.hiyorimiNecessary && plugin.audio.driverReal != null && plugin.audio.driverReal.isDataBlock)
            return plugin.audio.mds.update(buffer, offset, sampleCount, null);

        if (plugin.audio.stepCounter > 0) {
            plugin.audio.stepCounter -= sampleCount;
            if (plugin.audio.stepCounter <= 0) {
                plugin.audio.paused = true;
                plugin.audio.stepCounter = 0;
                return plugin.audio.mds.update(buffer, offset, sampleCount, null);
            }
        }

//                driverVirtual.vstDelta = 0;
//                stwh.reset();
//                stwh.start();
//logger.log(Level.TRACE, "driver: " + driverVirtual.getClass().getSimpleName());
        int cnt = plugin.audio.mds.update(buffer, offset, sampleCount, plugin.audio.driverVirtual::processOneFrame);
        plugin.audio.procTimePer1Frame = (int) ((double) System.currentTimeMillis() / (sampleCount + 1) * 1000000.0);
        return cnt;
    }

    public long getDriverCounter() {
        return 0;
    }

    public void copyWaveBuffer(short[][] dest) {
        plugin.audio.chipRegister.mds.visWaveBuffer.copy(dest);
    }

    public long whichCounter(long real, long virtual) {
        return 0;
    }
}
