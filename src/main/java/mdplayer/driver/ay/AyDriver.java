package mdplayer.driver.ay;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.ZxBeepChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * AY
 *
 * @author kumatan
 */
public class AyDriver extends BaseDriver {

    private static final Logger logger = getLogger(AyDriver.class.getName());

    private AY ay;

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;

        int songNo = (int) args[0];

        ay = new AY();
        ay.setZxClock();
        ay.setSampleRate(setting.getOutputDevice().getSampleRate());

        try {
            ay.run(vgmBuf);
            ay.setup(songNo,
                    (r, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, r, d, model),
                    () -> plugin.chipRegister.chip(ZxBeepChip.class).write(0, -1, -1, -1, model)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Driver initialization failed.", e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    ay.oneFrame();
                    counter++;
                } else {
                    vgmFrameCounter++;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        ay.getInformation(buf);
        Vgm.Gd3 ret = new Vgm.Gd3();
        ret.trackName = ay.information.songsStructures.getFirst().pSongName;
        ret.trackNameJ = ay.information.songsStructures.getFirst().pSongName;
        return ret;
    }
}
