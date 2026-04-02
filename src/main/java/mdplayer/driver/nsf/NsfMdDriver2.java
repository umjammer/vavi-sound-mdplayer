/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

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
 * NsfMdDriver2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-01 nsano initial version <br>
 */
public class NsfMdDriver2 extends BaseDriver implements NsfDriver {

    private static final Logger logger = getLogger(NsfMdDriver2.class.getName());

    private final Nsf2 nsf;

    public NsfMdDriver2() {
        this.nsf = new Nsf2();
        nsf.sampleRate = setting.getOutputDevice().getSampleRate();
    }

    @Override
    public int getSongs() {
        return nsf.songs;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (ByteUtil.readLeInt(buf, 0) != Nsf2.FCC_NSF) {
            // NSFe is not supported for now
            logger.log(Level.WARNING, "NSFe not supported.");
            return null;
        }

        if (buf.length < 0x80) { // no header?
            logger.log(Level.WARNING, "no header?");
            return null;
        }

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

        nsf.init(vgmBuf);
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
    public void processOneFrame() {
    }

    int CC;
    static final int INTERVAL = 1024;

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        // TODO loop, silent detector
        short[] b = new short[sampleCount / 2];
        int r = nsf.renderer.render(b, offset, sampleCount / 2);
        for (int i = 0; i < r; i++) {
            buffer[i * 2 + 0] = b[i];
            buffer[i * 2 + 1] = b[i];
            if (CC++ % INTERVAL == 0) {
                logger.log(Level.DEBUG, "NSF: %d, %d".formatted(b[i], b[i]));
            }
        }
        return r * 2;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        nsf.visWaveBufferCopy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }
}
