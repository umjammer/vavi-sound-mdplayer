
package mdplayer.driver.mgsdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * @author kumatan
 */
public class MgsDriver extends BaseDriver {

    private static final Logger logger = getLogger(MgsDriver.class.getName());

    private final MgsDrv mgs;

    public MgsDriver() {
        mgs = new MgsDrv();
        mgs.k051649Write = (i, a, d) -> plugin.chipRegister.chip(K051649Chip.class).write(i, a, d, model);
        mgs.ay8910Write = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, model);
        mgs.ym2413Write = (a, d) -> plugin.chipRegister.chip(Ym2413Chip.class).write(0, a, d, model);
    }

    public void setPlayingFileName(String value) {
        mgs.playingFileName = value;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Gd3 ret = new Gd3();
        if (buf != null && buf.length > 8) {
            vgmGd3[0] = 8;
            ret.trackName = Common.getNRDString(buf, vgmGd3);
            ret.trackNameJ = ret.trackName;
        }

        return ret;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;

        try {
            mgs.run(vgmBuf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameMain() {
        try {
            counter++;
            vgmFrameCounter++;

            if (vgmFrameCounter % (Common.VGMProcSampleRate / 60) == 0) {
                int PLAYFG = mgs.interrupt();
                if (PLAYFG == 0) stopped = true;
                vgmCurLoop = mgs.getD();
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
