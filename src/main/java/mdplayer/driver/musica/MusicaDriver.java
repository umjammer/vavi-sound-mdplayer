package mdplayer.driver.musica;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

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
        musica.dir = System.getProperty("mdplayer.musica.dir", System.getProperty("user.dir"));
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();
        if (buf != null && buf.length > 8) {
            try {
                musica.k051649Write = (i, a, d) -> {};
                musica.ay8910Write = (a, d) -> {};
                musica.ym2413Write = (a, d) -> {};
                musica.run(buf);
            } catch (Exception ex) {
                logger.log(Level.ERROR, ex.getMessage(), ex);
                return null;
            }
            md.set(Tag.Title, metaData.getFirst(Tag.Title)); // TODO check
            md.set(Tag.TitleJ, metaData.getFirst(Tag.TitleJ));
            md.set(Tag.Note, metaData.getFirst(Tag.Note));
        }

        return md;
    }

    @Override
    public void init(byte[] dataBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.plugin = plugin;
        loopCounter = 0;
        curLoop = 0;
        this.model = model;
        frameCounter = -latency - waitTime;

        try {
            musica.run(dataBuf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    oneFrameMain();
                } else {
                    frameCounter++;
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
            frameCounter++;

            if (frameCounter % (Common.VGMProcSampleRate / 60) == 0) {
                int playFg = musica.interrupt();
                if (playFg == 0) stopped = true;
                curLoop = musica.getHL();
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void updateTrackName(String value) {
        metaData.set(Tag.Title, value);
        metaData.set(Tag.TitleJ, metaData.getFirst(Tag.Title));
    }

    private void updateNote(String value) {
        metaData.set(Tag.Note, value);
logger.log(Level.INFO, metaData);
    }
}
