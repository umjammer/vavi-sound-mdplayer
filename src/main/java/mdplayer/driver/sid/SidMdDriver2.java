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

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
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
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (buf == null) return null;

        if (ByteUtil.readLeInt(buf, 0) != Sid2.FCC_PSID && ByteUtil.readLeInt(buf, 0) != Sid2.FCC_RSID) {
            return null;
        }

        sid.songs = Common.getBE16(buf, 0x0e);

        MetaData md = new MetaData();
        try {
            md.set(Tag.Title, new String(buf, 0x16, 32, StandardCharsets.US_ASCII).trim());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = md.getFirst(Tag.Title).indexOf((char) 0);
            if (idx != -1) md.set(Tag.Title, md.getFirst(Tag.Title).substring(0, idx));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            md.set(Tag.Composer, new String(buf, 0x36, 32, StandardCharsets.US_ASCII).trim());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = md.getFirst(Tag.Composer).indexOf((char) 0);
            if (idx != -1) md.set(Tag.Composer, md.getFirst(Tag.Composer).substring(0, idx));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            md.set(Tag.Note, new String(buf, 0x56, 32, StandardCharsets.US_ASCII).trim());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = md.getFirst(Tag.Note).indexOf((char) 0);
            if (idx != -1) md.set(Tag.Note, md.getFirst(Tag.Note).substring(0, idx));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return md;
    }

    /**
     * @param args 0: songNo
     */
    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.dataBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

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

        sid.song = (int) args[0];
        sid.init(vgmBuf, setting);

        plugin.chipRegister.chip(SidChip.class).setDriver(this);
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;
        try {
            speedCounter += speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                } else {
                    frameCounter++;
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
    public int getSongs() {
        return sid.songs;
    }

int CC;
static final int INTERVAL = 1024;

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        if (frameCounter < 0) {
            frameCounter += sampleCount / 2;
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
