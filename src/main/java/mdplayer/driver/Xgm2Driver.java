package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * XGM2
 *
 * @author kumatan
 */
public class Xgm2Driver extends BaseDriver {

    private static final Logger logger = getLogger(Xgm2Driver.class.getName());

    private final Xgm2 xgm2;

    public Xgm2Driver() {
        this.xgm2 = new Xgm2();
        xgm2.pcmStep = setting.getOutputDevice().getSampleRate() / 13300.0;
        xgm2.version = v -> version = v;
        xgm2.tag = this::tag;
        xgm2.stop = () -> stopped = true;
        xgm2.loop = l -> vgmCurLoop = l;
        xgm2.ym2612Write = (p, a, d) -> plugin.chipRegister.chip(Ym2612Chip.class).write(0, p, a, d, model, vgmFrameCounter);
        xgm2.sn76489Write = v -> plugin.chipRegister.chip(Sn76489Chip.class).write(0, v, model /*, vgmFrameCounter */);
    }

    private void tag(boolean existGD3, int gd3DataBlockAddr) {
        if (!existGD3) gd3 = new Gd3();
        else {
            gd3 = mdplayer.Common.getGD3Info(vgmBuf, gd3DataBlockAddr + 12);
            gd3.usedChips = usedChips;
        }
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        xgm2.getXGM2Info(buf);
        return gd3;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = vgmBuf;
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

        xgm2.getXGM2Info(vgmBuf);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(1, 1);
        }

        // initialize Driver
        xgm2.init();

        xgm2.vgmBuf = vgmBuf;
    }

    @Override
    public void processOneFrame() {
        try {
            xgm2.clockVi();

            vgmSpeedCounter += (double) mdplayer.Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    counter++;
                    vgmFrameCounter++;

                    xgm2.oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }

            xgm2.clock(stopped);
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
