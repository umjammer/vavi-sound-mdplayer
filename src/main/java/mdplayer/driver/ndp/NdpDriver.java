package mdplayer.driver.ndp;

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
 * MSX NDP
 * <p>
 * system property
 * <li>{@code mdplayer.ndp.dir} ... ndp.bin location, default {@code $HOME}</li>
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
        ndp.dir = System.getProperty("mdplayer.ndp.dir", System.getProperty("user.dir"));
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();
        if (buf != null && buf.length > 8) {
            if (buf.length > 7 + 0x0b && (buf[7 + 0x0b] & 2) != 0) {
                int[] index = new int[] {7 + 0xe};
                String TITLE = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                md.set(Tag.Title, TITLE); md.set(Tag.TitleJ, TITLE);
                String COMPOSER = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                md.set(Tag.Composer, COMPOSER); md.set(Tag.ComposerJ, COMPOSER);
                String ARRANGER = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                md.set(Tag.GameSystem, ARRANGER);
                String PROGRAMMER = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                md.set(Tag.Converter, PROGRAMMER);
                String MEMO = Common.getNRDString(buf, /* ref */ index, (byte) 0xff);
                md.set(Tag.Note, MEMO);
            }
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
            ndp.run(dataBuf);
        } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage(), e);
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
                curLoop = ndp.interrupt(() -> stopped = true);
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Exception in interrupt: " + ex.getMessage(), ex);
        }
    }
}
