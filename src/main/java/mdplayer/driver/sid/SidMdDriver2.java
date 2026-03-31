/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.sid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * SidMdDriver2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-03-31 nsano initial version <br>
 */
public class SidMdDriver2 extends BaseDriver implements SidDriver {

    private static final Logger logger = getLogger(SidMdDriver2.class.getName());

    private final Sid2 sid;

    public SidMdDriver2() {
        this.sid = new Sid2();
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        if (ByteUtil.readLeInt(buf, 0) != Sid2.FCC_PSID && ByteUtil.readLeInt(buf, 0) != Sid2.FCC_RSID) {
            return null;
        }

        sid.songs = Common.getBE16(buf, 0x0e);

        Vgm.Gd3 gd3 = new Vgm.Gd3();
        try {
            gd3.trackName = new String(buf, 0x16, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.trackName.indexOf((char) 0);
            if (idx != -1) gd3.trackName = gd3.trackName.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            gd3.composer = new String(buf, 0x36, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.composer.indexOf((char) 0);
            if (idx != -1) gd3.composer = gd3.composer.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            gd3.notes = new String(buf, 0x56, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.notes.indexOf((char) 0);
            if (idx != -1) gd3.notes = gd3.notes.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return gd3;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

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

        sid.init(vgmBuf, setting);

        plugin.chipRegister.chip(SidChip.class).setDriver(this);
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;
        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    counter++;
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public Integer[][] getRegisterFromSid() {
        return null;
    }

    @Override
    public Map<String, Object> getInfo() {
        return Map.of();
    }

    @Override
    public void setSong(int songNo) {
        sid.song = songNo;
    }

    @Override
    public int getSongs() {
        return sid.songs;
    }

int CC;
static final int INTERVAL = 1024;

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        if (vgmFrameCounter < 0) {
            vgmFrameCounter += sampleCount / 2;
            return sampleCount;
        }

        int c = 0;

        try {
            for (int i = 0; i < sampleCount / 2 && !sid.deque.isEmpty(); i++) {
                processOneFrame();
                buffer[c + 0] = sid.deque.take();
                buffer[c + 1] = sid.deque.take();
if (CC++ % INTERVAL == 0) {
 logger.log(Level.DEBUG, "SID: %d, %d".formatted(buffer[c + 0], buffer[c + 1]));
}
                sid.visWB.enq(buffer[c + 0], buffer[c + 1]);
                c += 2;
            }
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return c;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        sid.visWB.copy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }
}
