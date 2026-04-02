package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.chips.NesChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import mdsound.MDSound;
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
        nsf.isRealModel = model != EnmModel.RealModel;
        nsf.sampleRate = setting.getOutputDevice().getSampleRate();
        nsf.updateAtMiddle = this::updateAtMiddle;
        nsf.updateAtTrail = this::updateAtTrail;
        nsf.speed = () -> vgmSpeed;
        nsf.incCounter = l -> vgmFrameCounter += l;
        nsf.getCounter = () -> vgmFrameCounter;
        nsf.updateAtDetectLoop = this::updateAtDetectLoop;
        nsf.updateAtDetectSilent = this::updateAtDetectSilent;
    }

    @Override
    public int getSongs() {
        return nsf.songs;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (ByteUtil.readLeInt(buf, 0) != Nsf.FCC_NSF) {
            // NSFe is not supported for now
            logger.log(Level.WARNING, "NSFe not supported.");
            return null;
        }

        if (buf.length < 0x80) { // no header?
            logger.log(Level.WARNING, "no header?");
            return null;
        }

        nsf.init(buf);

        Gd3 gd3 = new Gd3();
        gd3.gameName = nsf.title;
        gd3.gameNameJ = nsf.title;
        gd3.composer = nsf.artist;
        gd3.composerJ = nsf.artist;
        gd3.trackName = nsf.title;
        gd3.trackNameJ = nsf.title;
        gd3.systemName = nsf.copyright;
        gd3.systemNameJ = nsf.copyright;

        return gd3;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        nsf.chip = plugin.chipRegister.chip(NesChip.class);

        if (model == EnmModel.RealModel) {
            stopped = true;
            vgmCurLoop = 9999;
            return;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        gd3 = getGD3Info(vgmBuf);

        nsf.init(setting);
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;

        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    //oneFrameMain();
                } else {
                    vgmFrameCounter++;
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
    public void setApu(MDSound.Chip chip) {
        nsf.cAPU = chip;
    }

    @Override
    public void setDmc(MDSound.Chip chip) {
        nsf.cDMC = chip;
    }

    @Override
    public void setFds(MDSound.Chip chip) {
        nsf.cFDS = chip;
    }

    @Override
    public void setMmc5(MDSound.Chip chip) {
        nsf.cMMC5 = chip;
    }

    @Override
    public void setN160(MDSound.Chip chip) {
        nsf.cN160 = chip;
    }

    @Override
    public void setVrc6(MDSound.Chip chip) {
        nsf.cVRC6 = chip;
    }

    @Override
    public void setVrc7(MDSound.Chip chip) {
        nsf.cVRC7 = chip;
    }

    @Override
    public void setFme7(MDSound.Chip chip) {
        nsf.cFME7 = chip;
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
        vgmSpeedCounter += vgmSpeed;
        counter = (int) vgmSpeedCounter;
        vgmFrameCounter++;
    }

    private void updateAtTrail(boolean playtime_detected) {
        if (!playtime_detected) vgmCurLoop = 0;
        else {
            if (totalCounter != 0) vgmCurLoop = (int) (counter / totalCounter);
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
