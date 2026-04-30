package mdplayer.driver.musica;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
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

    public MusicaK4Driver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.musicaK4 = new MuSICA_K4();
        musicaK4.k051649Write = (i, a, d) -> plugin.chipRegister.chip(K051649Chip.class).write(i, a, d, model);
        musicaK4.ay8910Write = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, model);
        musicaK4.ym2413Write = (a, d) -> plugin.chipRegister.chip(Ym2413Chip.class).write(0, a, d, model);
        musicaK4.dir = System.getProperty("mdplayer.musica.dir", System.getProperty("user.dir"));
    }

    public MusicaK4Driver() {
        this(null); // gross
    }

    public byte[] getBgmBin() {
        return musicaK4.getBgmBin();
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        throw new UnsupportedOperationException();
    }

    public MetaData getMetaData(byte[] buf, byte[] vcdBuf) {
        MetaData ret = new MetaData();
        if (buf != null && buf.length > 8) {
            try {
                musicaK4.run(buf, vcdBuf);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                return null;
            }
            if (musicaK4.getBgmBin() == null) return null;
            MetaData md = new MusicaDriver().getMetaData(musicaK4.getBgmBin());
            ret.set(Tag.Title, md.getFirst(Tag.Title));
            ret.set(Tag.TitleJ, md.getFirst(Tag.TitleJ));
            ret.set(Tag.Note, md.getFirst(Tag.Note));
        }

        return ret;
    }

    public void compile(byte[] vgmBuf, byte[] vcdBuf) {
        logger.log(Level.INFO, vgmBuf.length + " bytes\n" + StringUtil.getDump(vgmBuf, 32));
        try {
            musicaK4.run(vgmBuf, vcdBuf);
        } catch (IllegalStateException e) {
            throw e;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
    }

    @Override
    public void processOneFrame() {
        throw new UnsupportedOperationException();
    }
}
