package mdplayer.driver.mgsdrv;

import java.io.IOException;
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
 * MSX MgsDrv
 *
 * system property
 * <li>{@code mdplayer.mgs.dir} ... mgsdrv.com location, default {@code $HOME}</li>
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
        mgs.dir = System.getProperty("mdplayer.mgs.dir", System.getProperty("user.dir"));
    }

    public void setPlayingFileName(String value) {
        mgs.playingFileName = value;
    }

    /**
     * @param args 0: index
     */
    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();
        if (buf != null && buf.length > 8) {
            int[] index = {(int) args[0]};
            md.set(Tag.Title, Common.getNRDString(buf, index));
            md.set(Tag.TitleJ, md.getFirst(Tag.Title));
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
            mgs.run(dataBuf);
        } catch (IOException e) {
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
                int playFg = mgs.interrupt();
                if (playFg == 0) stopped = true;
                curLoop = mgs.getD();
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
