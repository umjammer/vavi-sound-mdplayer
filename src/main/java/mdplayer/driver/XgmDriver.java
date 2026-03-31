package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.Xgm.XgmPcm;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * XGM2
 *
 * @author kumatan
 */
public class XgmDriver extends BaseDriver {

    private static final Logger logger = getLogger(XgmDriver.class.getName());

    private final Xgm xgm;

    public XgmDriver() {
        this.xgm = new Xgm();
        xgm.pcmStep = setting.getOutputDevice().getSampleRate() / 14000.0;
        xgm.stop = () -> stopped = true;
        xgm.loop = () -> vgmCurLoop++;
        xgm.tag = () -> gd3 = getGD3Info(vgmBuf);
        xgm.ym2612Write = (p, a, d) -> plugin.chipRegister.chip(Ym2612Chip.class).write(0, p, a, d, model, vgmFrameCounter);
        xgm.sn76489Write = v -> plugin.chipRegister.chip(Sn76489Chip.class).write(0, v, model);
    }

    public XgmPcm[] getXgmPcm() {
        return xgm.xgmpcm;
    }

    @Override
    public void init(byte[] xgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = xgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        xgm.getXGMInfo(vgmBuf);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 1, 1);
        }

        // Initializing the Driver
        xgm.init();

        xgm.vgmBuf = vgmBuf;
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    counter++;
                    vgmFrameCounter++;

                    xgm.oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }

            xgm.clock(stopped);
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        xgm.getXGMInfo(buf);
        return gd3;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] vgmBuf) {

        if (!xgm.existGD3) return new Vgm.Gd3();

        Vgm.Gd3 gd3 = Common.getGD3Info(vgmBuf, xgm.gd3InfoStartAddr + 12);
        gd3.usedChips = usedChips;

        return gd3;
    }
}
