package mdplayer.driver.xgm;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.xgm.Xgm;
import mdplayer.lib.xgm.Xgm.XgmPcm;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * XGM
 *
 * @author kumatan
 */
public class XgmDriver extends BaseDriver {

    private static final Logger logger = getLogger(XgmDriver.class.getName());

    private final Xgm xgm;

    public XgmDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.xgm = new Xgm();
        xgm.sampleRate = Common.VGMProcSampleRate;
        xgm.pcmStep = setting.getOutputDevice().getSampleRate() / 14000.0;
        xgm.stop = () -> stopped = true;
        xgm.loop = () -> curLoop++;
        xgm.updateMetaData = () -> metaData = getMetaData(dataBuf);
        xgm.ym2612Write = (p, a, d) -> plugin.chipRegister.chip(Ym2612Chip.class).write(0, p, a, d, model, frameCounter);
        xgm.sn76489Write = v -> plugin.chipRegister.chip(Sn76489Chip.class).write(0, v, model);
    }

    public XgmDriver() {
        this(null); // gross
    }

    public XgmPcm[] getXgmPcm() {
        return xgm.xgmPcm;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
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

        xgm.getXGMInfo(this.dataBuf);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 1, 1);
        }

        // Initializing the Driver
        xgm.init();

        xgm.xgmBuf = this.dataBuf;
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
        xgm.getXGMInfo(buf); // #getMetaData below is called inside
        return metaData;
    }

    private MetaData getMetaData(byte[] dataBuf) {

        if (!xgm.existGD3) return new MetaData();

        MetaData md = Common.getMetaData(dataBuf, xgm.gd3InfoStartAddr + 12);
        md.set(Tag.Chip, usedChips);

        return md;
    }
}
