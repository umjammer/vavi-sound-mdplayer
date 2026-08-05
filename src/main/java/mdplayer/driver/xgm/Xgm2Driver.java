package mdplayer.driver.xgm;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.xgm.Xgm2;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * XGM2
 *
 * @author kumatan
 */
public class Xgm2Driver extends XgmDriver {

    private static final Logger logger = getLogger(Xgm2Driver.class.getName());

    private final Xgm2 xgm2;

    public Xgm2Driver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.xgm2 = new Xgm2();
        xgm2.pcmStep = BaseDriver.setting.getOutputDevice().getSampleRate() / 13300.0;
        xgm2.version = v -> version = v;
        xgm2.tag = this::tag;
        xgm2.stop = () -> stopped = true;
        xgm2.loop = l -> curLoop = l;
        xgm2.ym2612Write = (p, a, d) -> plugin.chipRegister.chip(Ym2612Chip.class).write(0, p, a, d, model, frameCounter);
        xgm2.sn76489Write = v -> plugin.chipRegister.chip(Sn76489Chip.class).write(0, v, model /*, frameCounter */);
    }

    public Xgm2Driver() {
        this(null); // gross
    }

    private void tag(boolean existGD3, int gd3DataBlockAddr) {
        if (!existGD3) metaData = new MetaData();
        else {
            metaData = mdplayer.Common.getMetaData(dataBuf, gd3DataBlockAddr + 12);
            metaData.set(Tag.Chip, usedChips);
        }
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        xgm2.getXGM2Info(buf);
        return metaData;
    }

    @Override
    public void init(EnmModel model,int  latency, int waitTime, Object... args) {
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

        xgm2.getXGM2Info(dataBuf);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(1, 1);
        }

        // initialize Driver
        xgm2.init();

        xgm2.xgmBuf = dataBuf;
    }

    @Override
    public void processOneFrame() {
        try {
            xgm2.clockVi();

            speedCounter += (double) mdplayer.Common.VGMProcSampleRate / BaseDriver.setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                    frameCounter++;

                    xgm2.oneFrameMain();
                } else {
                    frameCounter++;
                }
            }

            xgm2.clock(stopped);
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
