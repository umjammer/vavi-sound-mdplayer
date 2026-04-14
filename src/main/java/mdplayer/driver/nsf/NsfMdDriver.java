package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.NesChip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * NSF
 *
 * @author kumatan
 */
public class NsfMdDriver extends BaseDriver implements NsfDriver {

    private static final Logger logger = getLogger(NsfMdDriver.class.getName());

    private final Nsf nsf;

    public NsfMdDriver() {
        this.nsf = new Nsf();
        nsf.getVolume = plugin.chipRegister.chip(NesChip.class)::getVolume;
        nsf.isRealModel = model != EnmModel.RealModel;
        nsf.sampleRate = setting.getOutputDevice().getSampleRate();
        nsf.updateAtMiddle = this::updateAtMiddle;
        nsf.updateAtTrail = this::updateAtTrail;
        nsf.speed = () -> speed;
        nsf.incCounter = l -> frameCounter += l;
        nsf.getCounter = () -> frameCounter;
        nsf.updateAtDetectLoop = this::updateAtDetectLoop;
        nsf.updateAtDetectSilent = this::updateAtDetectSilent;
    }

    @Override
    public int getSongs() {
        return nsf.songs;
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (ByteUtil.readLeInt(buf, 0) != Nsf.FCC_NSF) {
            // NSFe is not supported for now
            logger.log(Level.WARNING, "NSFe not supported.");
            return null;
        }

        if (buf.length < 0x80) { // no header?
            logger.log(Level.WARNING, "no header?");
            return null;
        }

        nsf.initInfo(buf, Common.charset);

        MetaData md = new MetaData();
        md.set(Tag.GameTitle, nsf.title);
        md.set(Tag.GameTitleJ, nsf.title);
        md.set(Tag.Composer, nsf.artist);
        md.set(Tag.ComposerJ, nsf.artist);
        md.set(Tag.Title, nsf.title);
        md.set(Tag.TitleJ, nsf.title);
        md.set(Tag.GameSystem, nsf.copyright);
        md.set(Tag.GameSystemJ, nsf.copyright);

        return md;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.dataBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        nsf.chip = plugin.chipRegister.chip(NesChip.class);

        if (model == EnmModel.RealModel) {
            stopped = true;
            curLoop = 9999;
            return;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;
        speedCounter = 0;

        metaData = getMetaData(vgmBuf);

        nsf.init(setting);
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;

        try {
            speedCounter += speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    //oneFrameMain();
                } else {
                    frameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public void setSong(int songNo) {
        nsf.song = songNo;
    }

    @Override
    public boolean useFds() {
        return nsf.useFds;
    }

    @Override
    public boolean useFme7() {
        return nsf.useFme7;
    }

    @Override
    public boolean useMmc5() {
        return nsf.useMmc5;
    }

    @Override
    public boolean useN106() {
        return nsf.useN106;
    }

    @Override
    public boolean useVrc6() {
        return nsf.useVrc6;
    }

    @Override
    public boolean useVrc7() {
        return nsf.useVrc7;
    }

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
//        vstDelta = 0;
        return nsf.render(buffer, sampleCount / 2, offset) * 2;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        nsf.visWaveBufferCopy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }

    private void updateAtMiddle() {
        speedCounter += speed;
        counter = (int) speedCounter;
        frameCounter++;
    }

    private void updateAtTrail(boolean playtime_detected) {
        if (!playtime_detected) curLoop = 0;
        else {
            if (totalCounter != 0) curLoop = (int) (counter / totalCounter);
            else stopped = true;
        }
    }

    private void updateAtDetectLoop(long totalCounter, long loopCounter) {
        this.totalCounter = totalCounter;
        if (this.totalCounter == 0) this.totalCounter = counter;
        this.loopCounter = loopCounter;
    }

    private void updateAtDetectSilent(long totalCounter) {
        this.totalCounter = totalCounter;
        if (this.totalCounter == 0) this.totalCounter = counter;
        loopCounter = 0;
        stopped = true;
    }
}
