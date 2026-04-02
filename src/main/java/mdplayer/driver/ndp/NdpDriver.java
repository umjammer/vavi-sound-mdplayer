package mdplayer.driver.ndp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * MSX NDP
 *
 * @author kumatan
 */
public class NdpDriver extends BaseDriver {

    private static final Logger logger = getLogger(NdpDriver.class.getName());

    private final Ndp ndp;

    public NdpDriver() {
        this.ndp = new Ndp();
        ndp.k051649Write = (i, a, d) -> plugin.chipRegister.chip(K051649Chip.class).write(i, a, d, model);
        ndp.ay8910Write = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, model);
        ndp.ym2413Write = (a, d) -> plugin.chipRegister.chip(Ym2413Chip.class).write(0, a, d, model);
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Gd3 ret = new Gd3();
        if (buf != null && buf.length > 8) {
            if (buf.length > 7 + 0x0b && (buf[7 + 0x0b] & 2) != 0) {
                int[] index = new int[] {7 + 0xe};
                String TITLE = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                gd3.trackName = gd3.trackNameJ = TITLE;
                String COMPOSER = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                gd3.composer = gd3.composerJ = COMPOSER;
                String ARRANGER = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                gd3.systemName = ARRANGER;
                String PROGRAMMER = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                gd3.converted = PROGRAMMER;
                String MEMO = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                gd3.notes = MEMO;
            }
            ret.trackName = gd3.trackName;
            ret.trackNameJ = gd3.trackNameJ;
            ret.composer = gd3.composer;
            ret.composerJ = gd3.composerJ;
            ret.systemName = gd3.systemName;
            ret.converted = gd3.converted;
            ret.notes = gd3.notes;
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
            ndp.run(vgmBuf);
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
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameMain() {
        try {
            counter++;
            vgmFrameCounter++;

            if (vgmFrameCounter % (Common.VGMProcSampleRate / 60) == 0) {
                vgmCurLoop = ndp.interrupt(() -> stopped = true);
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Exception in interrupt: " + ex.getMessage(), ex);
        }
    }
}
