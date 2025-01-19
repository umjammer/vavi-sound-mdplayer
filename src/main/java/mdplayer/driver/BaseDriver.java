
package mdplayer.driver;

import mdplayer.Audio;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.Ym2151Chip;


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

    public Vgm.Gd3 gd3 = new Vgm.Gd3();

    public String version = "";

    public String usedChips = "";

    public int vstDelta = 0;

    public boolean isDataBlock = false;

    public final int[] ym2151Hosei = new int[] {
        0, 0
    };

    protected byte[] vgmBuf = null;

    protected ChipRegister chipRegister = null;

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
                                 ChipRegister chipRegister,
                                 EnmModel model,
                                 EnmChip[] useChip,
                                 int latency,
                                 int waitTime);

    public abstract boolean init(byte[] vgmBuf,
                                 int fileType,
                                 ChipRegister chipRegister,
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
                int clock = chipRegister.chip(Ym2151Chip.class).getYM2151Clock(chipId);
                if (clock != -1) {
                    ym2151Hosei[chipId] = Common.getYM2151Hosei(ym2151ClockValue, clock);
                }
            }
        }
    }

    public int render(Audio audio, short[] buffer, int offset, int sampleCount) {
        if (audio.hiyorimiNecessary && audio.driverReal != null && audio.driverReal.isDataBlock)
            return audio.mds.update(buffer, offset, sampleCount, null);

        if (audio.stepCounter > 0) {
            audio.stepCounter -= sampleCount;
            if (audio.stepCounter <= 0) {
                audio.paused = true;
                audio.stepCounter = 0;
                return audio.mds.update(buffer, offset, sampleCount, null);
            }
        }

//                driverVirtual.vstDelta = 0;
//                stwh.reset();
//                stwh.start();
//logger.log(Level.TRACE, "driver: " + driverVirtual.getClass().getSimpleName());
        int cnt = audio.mds.update(buffer, offset, sampleCount, audio.driverVirtual::processOneFrame);
        audio.procTimePer1Frame = (int) ((double) System.currentTimeMillis() / (sampleCount + 1) * 1000000.0);
        return cnt;
    }
}
