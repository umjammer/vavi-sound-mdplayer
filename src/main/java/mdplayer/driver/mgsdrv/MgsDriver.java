package mdplayer.driver.mgsdrv;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import java.util.ArrayList;
import java.util.List;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.mgsdrv.MgsDrv;
import mdplayer.driver.BasePlugin;
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
    private String[] comments;

    public MgsDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        mgs = new MgsDrv();
        mgs.k051649Write = (i, a, d) -> plugin.chipRegister.chip(K051649Chip.class).write(i, a, d, model);
        mgs.ay8910Write = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, model);
        mgs.ym2413Write = (a, d) -> plugin.chipRegister.chip(Ym2413Chip.class).write(0, a, d, model);
        mgs.dir = System.getProperty("mdplayer.mgs.dir", System.getProperty("user.dir"));
    }

    public MgsDriver() {
        this(null); // gross
    }

    /**
     * @param args 0: index
     */
    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();
        comments = null;
        if (buf != null && buf.length > 8) {
            int start = (args != null && args.length > 0 && args[0] instanceof Integer) ? (int) args[0] : 8;
            for (int i = 0; i < Math.min(buf.length - 1, 16); i++) {
                if (buf[i] == '\r' && buf[i + 1] == '\n') {
                    start = i + 2;
                    break;
                }
            }
            int[] index = {start};
            String text = Common.getNRDString(buf, index);
            if (!text.isEmpty()) {
                String[] lines = text.split("\\r?\\n");
                List<String> validLines = new ArrayList<>();
                for (String l : lines) {
                    if (!l.isEmpty()) {
                        validLines.add(l);
                    }
                }
                if (!validLines.isEmpty()) {
                    md.set(Tag.Title, validLines.getFirst().strip());
                    md.set(Tag.TitleJ, validLines.getFirst().strip());
                }
                if (validLines.size() > 1) {
                    md.set(Tag.Composer, validLines.get(1).strip());
                    md.set(Tag.ComposerJ, validLines.get(1).strip());
                }
                if (validLines.size() > 2) {
                    md.set(Tag.Note, validLines.get(2).strip());
                }
                comments = validLines.subList(0, Math.min(3, validLines.size())).toArray(String[]::new);
            }
        }

        return md;
    }

    @Override
    public String[] comments() {
        return comments;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        loopCounter = 0;
        curLoop = 0;
        this.model = model;
        frameCounter = -latency - waitTime;

        mgs.playingFileName = plugin.playingFileName;

        metaData = retrieveMetaData(dataBuf, 8);

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

    @Override
    public String getName() {
        return "MGSDRV";
    }
}
