package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * VGM
 *
 * @author kumatan
 */
public class VgmDriver extends BaseDriver {

    private static final Logger logger = getLogger(VgmDriver.class.getName());

    public final Vgm vgm;

    public VgmDriver() {
        this.vgm = new Vgm();
        vgm.frameCounter = () -> vgmFrameCounter;
        vgm.dataBlock = b -> isDataBlock = b;
        vgm.getTotalCounter = () -> totalCounter;
        vgm.setTotalCounter = v -> totalCounter = v;
        vgm.setLoopCounter = v -> loopCounter = v;
        vgm.loop = () -> vgmCurLoop;
        vgm.setUsedChips = s -> usedChips = s;
        vgm.getUsedChips = () -> usedChips;
        vgm.setVersion = s -> version = s;
        vgm.getVersion = () -> version;
        vgm.getGD3Info = this::getGD3Info;
    }

    public boolean isPcmRAMWrite() {
        return vgm.isPcmRAMWrite;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        vgmFrameCounter = -latency - waitTime;
        vgmCurLoop = 0;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        stopped = false;
        isDataBlock = false;

        vgm.vgmBuf = vgmBuf;
        vgm.model = model;
        vgm.chipRegister = plugin.chipRegister;
        vgm.setting = setting;
        vgm.ym2151Hosei = plugin.chipRegister.chip(Ym2151Chip.class).ym2151Hosei;

        vgm.init();

        vgm.useChipYM2612Ch6 = false;
        for (Class<? extends Chip> uc : useChip) {
            if (uc == Ym2612Chip.class && false) { // TODO Ym2612Ch6
                vgm.useChipYM2612Ch6 = true;
                break;
            }
        }

    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameVGMMain();
                } else {
                    vgmFrameCounter++;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameVGMMain() {
        if (vgm.vgmWait > 0) {
            //if (model == enmModel.VirtualModel)
            vgm.oneFrameVGMStream();
            vgm.vgmWait--;
            counter++;
            vgmFrameCounter++;
//logger.log(Level.TRACE, "ret: wait: " + vgmWait + ".formatted(fc: " + vgmFrameCounter));
            return;
        }

        vgm.oneFrameVGMStream();

        if (!vgm.vgmAnalyze) {
            //if (model == enmModel.VirtualModel)
            //    oneFrameVGMStream();
            stopped = true;
            logger.log(Level.DEBUG, "ret: not analyze");
            return;
        }

        int countNum = 0;
        while (vgm.vgmWait <= 0) {
            if (vgm.vgmAdr >= vgmBuf.length || (vgm.vgmEof != 0 && vgm.vgmAdr >= vgm.vgmEof)) {
                if (loopCounter != 0) {
                    vgm.vgmAdr = vgm.vgmLoopOffset + 0x1c;
                    vgmCurLoop++;
                    counter = 0;
                } else {
                    vgm.vgmAnalyze = false;
                    logger.log(Level.DEBUG, "ret: not analyze 2");
                    return;
                }
            }

            int cmd = vgmBuf[vgm.vgmAdr] & 0xff;
//if (!List.of(0xc0).contains(cmd)) {
            logger.log(Level.DEBUG, "[%s]: adr: 0x%x, cmd: 0x%x".formatted(model, vgm.vgmAdr, cmd)); // ok
//}
            if (vgm.vgmCmdTbl[cmd] != null) {
                //if (model == EnmModel.VirtualModel) logger.log(Level.DEBUG, "%05x : %02x ".formatted(vgmAdr, vgmBuf[vgmAdr]));
                vgm.vgmCmdTbl[cmd].run();
            } else {
                // Unknown command
                logger.log(Level.WARNING, "[%s]:unknown command: adr: 0x%x cmd: 0x%x".formatted(model, vgm.vgmAdr, vgmBuf[vgm.vgmAdr]));
                vgm.vgmAdr++;
            }
            countNum++;
            if (countNum > 100) {
                if (model == EnmModel.RealModel && countNum % 100 == 0) {
                    isDataBlock = true;
                    plugin.chipRegister.chip(Ym2608Chip.class).sendData(0, model);
                    plugin.chipRegister.chip(Ym2608Chip.class).setSyncWait(0, 1);
                    plugin.chipRegister.chip(Ym2151Chip.class).sendData(0, model);
                    plugin.chipRegister.chip(Ym2151Chip.class).setSyncWait(0, 1);

                    plugin.chipRegister.chip(Ym2608Chip.class).sendData(1, model);
                    plugin.chipRegister.chip(Ym2608Chip.class).setSyncWait(1, 1);
                    plugin.chipRegister.chip(Ym2151Chip.class).sendData(1, model);
                    plugin.chipRegister.chip(Ym2151Chip.class).setSyncWait(1, 1);
                }
            }
        }

        if (model == EnmModel.RealModel && isDataBlock) {
            isDataBlock = false;
            //logger.log(Level.TRACE, "%s countNum:%d".formatted(model, countNum));
            countNum = 0;
        }

        // Send wait
        if (model == EnmModel.RealModel) {
            if (vgmSpeed == 1) { // Apply weight only when speed is constant
                if (vgm.useChipYM2612Ch6)
                    plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(0, vgm.vgmWait);
//                if ((useChip & enmUseChip.SN76489) == enmUseChip.SN76489)
//                    plugin.audio.chipRegister.setSN76489SyncWait(vgmWait);
//                plugin.audio.chipRegister.setYM2608SyncWait(vgmWait);
//                plugin.audio.chipRegister.setYM2151SyncWait(vgmWait);
            }
        }

//        if (model == enmModel.VirtualModel)
//           oneFrameVGMStream();

        vgm.vgmWait--;
        counter++;
        vgmFrameCounter++;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {

        int adr = vgmGd3[0] + 12 + 0x14;
        gd3 = Common.getGD3Info(buf, adr);
        gd3.usedChips = usedChips;

        return gd3;
    }

    @Override
    public long getDriverCounter() {
        return vgmFrameCounter;
    }

    @Override
    public long whichCounter(long real, long virtual) {
        return Math.min(real, virtual);
    }
}
