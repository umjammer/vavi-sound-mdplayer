/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.hvl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.hvl.HVL.Tune;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * HvlDriver.
 * <p>
 * HVL (HivelyTracker, also plays AHX) has its own renderer, so this driver
 * overrides {@link #render} like the sid driver does.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-03 nsano initial version <br>
 */
public class HvlDriver extends BaseDriver {

    private static final Logger logger = getLogger(HvlDriver.class.getName());

    /** HVL plays at 50Hz (PAL) */
    private static final int HZ = 50;

    private Tune tune;

    public HvlDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public HvlDriver() {
        this(null); // gross
    }

    private static boolean isSupportedMagic(byte[] buf) {
        if (buf == null || buf.length < 16) return false;
        if (buf[0] == 'H' && buf[1] == 'V' && buf[2] == 'L' && buf[3] <= 1) return true;
        return buf[0] == 'T' && buf[1] == 'H' && buf[2] == 'X' && buf[3] < 3;
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (!isSupportedMagic(buf)) return null;

        HVL.hvl_InitReplayer();
        Tune ht;
        try {
            ht = HVL.hvl_reset(buf, buf.length, 0, setting.getOutputDevice().getSampleRate(), false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
        if (ht == null) return null;

        MetaData md = new MetaData();
        md.set(Tag.Title, ht.ht_Name);
        // subsong 0 is the main song
        md.set(Tag.NumberOfSongs, String.valueOf(ht.ht_SubsongNr + 1));
        return md;
    }

    /**
     * @param args 0: songNo (0 origin, 0 is the main song)
     */
    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
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

        metaData = getMetaData(dataBuf);

        int songNo = args.length > 0 ? (int) args[0] : 0;

        HVL.hvl_InitReplayer();
        tune = HVL.hvl_reset(dataBuf, dataBuf.length, 0, setting.getOutputDevice().getSampleRate(), false);
        if (tune == null) {
            throw new IllegalStateException("not a hvl/ahx file");
        }
        tune.hvl_InitSubsong(Math.min(songNo, tune.ht_SubsongNr));

        // one decoded frame: 1/50 sec, stereo interleaved
        internalBuffer = new short[tune.ht_Frequency / HZ * 2];
        internalProduced = 0;
        internalConsumed = 0;
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
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private short[] internalBuffer;
    private int internalProduced = 0;
    private int internalConsumed = 0;

    @Override
    public int render(short[] b, int offset, int length) {
        if (tune == null) {
            return length;
        }
        if (frameCounter < 0) {
            frameCounter += length / 2;
            return length;
        }

        int written = 0;
        while (written < length - 1) {
            if (internalConsumed >= internalProduced) {
                // decode interleaved: left at even, right at odd indexes
                tune.hvl_DecodeFrame(internalBuffer, 0, internalBuffer, 1, 4);
                internalProduced = internalBuffer.length;
                internalConsumed = 0;
                if (tune.ht_SongEndReached != 0) {
                    tune.ht_SongEndReached = 0;
                    curLoop++;
                }
            }

            int frames = Math.min((length - written) / 2, (internalProduced - internalConsumed) / 2);
            for (int i = 0; i < frames; i++) {
                short l = internalBuffer[internalConsumed + i * 2];
                short r = internalBuffer[internalConsumed + i * 2 + 1];
                b[offset + written + i * 2] = l;
                b[offset + written + i * 2 + 1] = r;

                processOneFrame();
                fireEventHappened(this, "wave.buffer", l, r);
            }

            written += frames * 2;
            internalConsumed += frames * 2;
        }

        return written;
    }
}
