package mdplayer.driver.musica;

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
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


/**
 * MuSICA
 *
 * @author kumatan
 */
public class MusicaDriver extends BaseDriver {

    private static final Logger logger = getLogger(MusicaDriver.class.getName());

    private final MuSICA musica;

    public MusicaDriver() {
        this.musica = new MuSICA();
        musica.k051649Write = (i, a, d) -> plugin.chipRegister.chip(K051649Chip.class).write(i, a, d, model);
        musica.ay8910Write = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, model);
        musica.ym2413Write = (a, d) -> plugin.chipRegister.chip(Ym2413Chip.class).write(0, a, d, model);
        musica.updateTrackName = this::updateTrackName;
        musica.updateNote = this::updateNote;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Vgm.Gd3 ret = new Vgm.Gd3();
        if (buf != null && buf.length > 8) {
            try {
                musica.run(buf);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                return null;
            }
            ret.trackName = gd3.trackName;
            ret.trackNameJ = gd3.trackNameJ;
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
            musica.run(vgmBuf);
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
                int playFg = musica.interrupt();
                if (playFg == 0) stopped = true;
                vgmCurLoop = musica.getHL();
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void updateTrackName(String value) {
        gd3.trackName = value;
        gd3.trackNameJ = gd3.trackName;
    }

    private void updateNote(String value) {
        gd3.notes = value;
logger.log(Level.INFO, gd3);
    }
}
