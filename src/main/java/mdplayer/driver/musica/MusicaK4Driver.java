package mdplayer.driver.musica;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;
import vavi.util.StringUtil;

import static java.lang.System.getLogger;


/**
 * MuSICA
 *
 * @author kumatan
 */
public class MusicaK4Driver extends BaseDriver {

    private static final Logger logger = getLogger(MusicaK4Driver.class.getName());

    private final MuSICA_K4 musicaK4;

    public MusicaK4Driver() {
        this.musicaK4 = new MuSICA_K4();
        musicaK4.k051649Write = (i, a, d) -> plugin.chipRegister.chip(K051649Chip.class).write(i, a, d, model);
        musicaK4.ay8910Write = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, model);
        musicaK4.ym2413Write = (a, d) -> plugin.chipRegister.chip(Ym2413Chip.class).write(0, a, d, model);
    }

    public byte[] getBgmBin() {
        return musicaK4.getBgmBin();
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        throw new UnsupportedOperationException();
    }

    public Vgm.Gd3 getGD3Info(byte[] buf, byte[] vcdBuf) {
        Vgm.Gd3 ret = new Vgm.Gd3();
        if (buf != null && buf.length > 8) {
            try {
                musicaK4.run(buf, vcdBuf);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                return null;
            }
            if (musicaK4.getBgmBin() == null) return null;
            Vgm.Gd3 gd3 = (new MusicaDriver()).getGD3Info(musicaK4.getBgmBin(), null);
            ret.trackName = gd3.trackName;
            ret.trackNameJ = gd3.trackNameJ;
            ret.notes = gd3.notes;
        }

        return ret;
    }

    public void compile(byte[] vgmBuf, byte[] vcdBuf) {
        logger.log(Level.INFO, "\n" + StringUtil.getDump(vgmBuf, 128));
        try {
            musicaK4.run(vgmBuf, vcdBuf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.plugin = plugin;
    }

    @Override
    public void processOneFrame() {
        throw new UnsupportedOperationException();
    }
}
