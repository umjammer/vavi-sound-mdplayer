
package mdplayer.driver;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;


public abstract class BaseDriver {
    public Setting setting;

    public double vgmSpeed = 1;

    protected double vgmSpeedCounter;

    public long counter = 0;

    public long totalCounter = 0;

    public long loopCounter = 0;

    public int vgmCurLoop = 0;

    public boolean stopped = false;

    public long vgmFrameCounter;

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
        for (int chipID = 0; chipID < 2; chipID++) {
            ym2151Hosei[chipID] = Common.getYM2151Hosei(ym2151ClockValue, 3579545);
            if (model == EnmModel.RealModel) {
                ym2151Hosei[chipID] = 0;
                int clock = chipRegister.getYM2151Clock((byte) chipID);
                if (clock != -1) {
                    ym2151Hosei[chipID] = Common.getYM2151Hosei(ym2151ClockValue, clock);
                }
            }
        }
    }
}
