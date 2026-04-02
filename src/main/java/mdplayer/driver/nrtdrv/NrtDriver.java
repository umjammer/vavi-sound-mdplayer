package mdplayer.driver.nrtdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;

import dotnet4j.util.compat.Tuple3;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * NRTDRV
 *
 * @author kumatan
 */
public class NrtDriver extends BaseDriver {

    private static final Logger logger = getLogger(NrtDriver.class.getName());

    private final NRTDRV nrtdrv;

    public NrtDriver() {
        this.nrtdrv = new NRTDRV();
        nrtdrv.ctcStep = 4000000.0f / setting.getOutputDevice().getSampleRate();
        nrtdrv.ctc1Step = 4000000.0f / setting.getOutputDevice().getSampleRate();
        nrtdrv.ym2151WriteV = (i, a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(i, 0, a, d, EnmModel.VirtualModel, 0, 0);
        nrtdrv.ym2151WriteR = (i, a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(i, 0, a, d, EnmModel.RealModel, plugin.chipRegister.chip(Ym2151Chip.class).ym2151Hosei[0], 0);
        nrtdrv.ay8910WriteV = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, EnmModel.VirtualModel);
        nrtdrv.loop = l -> vgmCurLoop = l;
        nrtdrv.isRealModel = model == EnmModel.RealModel;
    }

    public int checkUseChip(byte[] vgmBuf) {
        return nrtdrv.checkUseChip(vgmBuf);
    }

    public void call(int cmdNo) {
        nrtdrv.call(cmdNo);
    }

    @Override
    public void init(byte[] nrdFileData, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = nrdFileData;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        gd3 = getGD3Info(nrdFileData, 42);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

        try {
            nrtdrv.ram = new byte[65536];
            Arrays.fill(nrtdrv.ram, (byte) 0);

            System.arraycopy(vgmBuf, 0, nrtdrv.ram, 0x4000, Math.min(vgmBuf.length, 0xfeff - 0x4000));
        } catch (Exception ex) {
            throw new IllegalStateException("Driver initialization failed.", ex);
        }

        plugin.chipRegister.chip(Ym2151Chip.class).setYm2151Hosei(model, 4000000);

        // Initializing the Driver
        nrtdrv.call(0);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2151Chip.class).sendData((byte) 0, model);
            plugin.chipRegister.chip(Ym2151Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2151Chip.class).sendData((byte) 1, model);
            plugin.chipRegister.chip(Ym2151Chip.class).setSyncWait((byte) 1, 1);
        }
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Vgm.Gd3 gd3 = new Vgm.Gd3();
        gd3.trackName = Common.getNRDString(buf, vgmGd3);
        gd3.trackNameJ = Common.getNRDString(buf, vgmGd3);
        gd3.composer = Common.getNRDString(buf, vgmGd3);
        gd3.composerJ = gd3.composer;
        gd3.vgmBy = Common.getNRDString(buf, vgmGd3);
        gd3.notes = Common.getNRDString(buf, vgmGd3);

        if ((buf[2] & 0x08) != 0) {
            gd3.lyrics = new ArrayList<>();
            int adr = vgmGd3[0];
            while (buf[adr] != (byte) 0xff || buf[adr + 1] != (byte) 0xff) {
                int cnt = (buf[adr] & 0xff) + (buf[adr + 1] & 0xff) * 0x100;
                int[] sAdr = new int[] {(buf[adr + 2] & 0xff) + (buf[adr + 3] & 0xff) * 0x100};
                String msg = Common.getNRDString(buf, sAdr);
                gd3.lyrics.add(new Tuple3<>(cnt, sAdr[0], msg));
                adr += 4;
            }
        }

        if ((((buf[2] & (byte) 0x80) != 0) && buf[41] != 2) || (buf[2] & 0x80) == 0) {
            gd3.notes = "!!Warning!! This data version instanceof older/newer.";
        }

        int r = nrtdrv.checkUseChip(buf);

        switch (r) {
            case 0:
                gd3.usedChips = "";
                break;
            case 1:
            case 2:
                gd3.usedChips = "YM2151";
                break;
            case 3:
                gd3.usedChips = "YM2151x2";
                break;
            case 4:
                gd3.usedChips = "AY8910";
                break;
            case 5:
            case 6:
                gd3.usedChips = "YM2151 , AY8910";
                break;
            case 7:
                gd3.usedChips = "YM2151x2 , AY8910";
                break;
        }

        return gd3;
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    counter++;
                    vgmFrameCounter++;

                    nrtdrv.oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }
            stopped = !nrtdrv.isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public long getDriverCounter() {
        return nrtdrv.work.totalCount;
    }

    @Override
    public long whichCounter(long real, long virtual) {
        return Math.max(virtual, real);
    }
}
