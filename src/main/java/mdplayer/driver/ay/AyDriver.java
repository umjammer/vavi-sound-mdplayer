package mdplayer.driver.ay;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.ZxBeepChip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.ay.AY;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * AY
 *
 * @author kumatan
 */
public class AyDriver extends BaseDriver {

    private static final Logger logger = getLogger(AyDriver.class.getName());

    private AY ay;

    public AyDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    /** @param args 0: songNo */
    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        loopCounter = 0;
        curLoop = 0;
        this.model = model;
        frameCounter = -latency - waitTime;

        int songNo = (int) args[0];

        ay = new AY();
        ay.setZxClock();
        ay.setSampleRate(setting.getOutputDevice().getSampleRate());

        try {
            ay.run(dataBuf);
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
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    ay.oneFrame();
                    counter++;
                } else {
                    frameCounter++;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        ay.getInformation(buf);
        MetaData md = new MetaData();
        md.set(Tag.Title, ay.information.songsStructures.getFirst().pSongName);
        md.set(Tag.TitleJ, ay.information.songsStructures.getFirst().pSongName);
        return md;
    }
}
