package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.Xgm.XgmPcm;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * XGM2
 *
 * @author kumatan
 */
public class XgmDriver extends BaseDriver {

    private static final Logger logger = getLogger(XgmDriver.class.getName());

    private final Xgm xgm;

    public XgmDriver() {
        this.xgm = new Xgm();
        xgm.pcmStep = setting.getOutputDevice().getSampleRate() / 14000.0;
        xgm.stop = () -> stopped = true;
        xgm.loop = () -> curLoop++;
        xgm.tag = () -> metaData = getMetaData(dataBuf);
        xgm.ym2612Write = (p, a, d) -> plugin.chipRegister.chip(Ym2612Chip.class).write(0, p, a, d, model, frameCounter);
        xgm.sn76489Write = v -> plugin.chipRegister.chip(Sn76489Chip.class).write(0, v, model);
    }

    public XgmPcm[] getXgmPcm() {
        return xgm.xgmpcm;
    }

    @Override
    public void init(byte[] xgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.dataBuf = xgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;
        speedCounter = 0;

        xgm.getXGMInfo(dataBuf);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 1, 1);
        }

        // Initializing the Driver
        xgm.init();

        xgm.vgmBuf = dataBuf;
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                    frameCounter++;

                    xgm.oneFrameMain();
                } else {
                    frameCounter++;
                }
            }

            xgm.clock(stopped);
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        xgm.getXGMInfo(buf);
        return metaData;
    }

    private MetaData getMetaData(byte[] vgmBuf) {

        if (!xgm.existGD3) return new MetaData();

        MetaData md = Common.getMetaData(vgmBuf, xgm.gd3InfoStartAddr + 12);
        md.set(Tag.Chip, usedChips);

        return md;
    }
}
