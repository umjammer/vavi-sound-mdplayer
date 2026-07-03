/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ahx;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.ahx.AHX.AHXOutput;
import mdplayer.driver.ahx.AHX.AHXPlayer;
import mdplayer.plugin.BasePlugin;
import mdsound.VisWaveBuffer;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * AhxDriver.
 * <p>
 * AHX has its own renderer, so this driver overrides {@link #render}
 * like the sid driver does.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-03 nsano initial version <br>
 */
public class AhxDriver extends BaseDriver {

    private static final Logger logger = getLogger(AhxDriver.class.getName());

    /** AHX plays at 50Hz (PAL) */
    private static final int HZ = 50;

    private final AHXPlayer player;
    private AHXOutput output;
    private boolean initialized;

    public AhxDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.player = new AHXPlayer();
    }

    public AhxDriver() {
        this(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (buf == null || buf.length < 14) return null;
        if (buf[0] != 'T' || buf[1] != 'H' || buf[2] != 'X') return null;

        AHXPlayer p = new AHXPlayer();
        try {
            p.loadSong(buf, buf.length);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }

        MetaData md = new MetaData();
        md.set(Tag.Title, p.song.name);
        // subsong 0 is the main song
        md.set(Tag.NumberOfSongs, String.valueOf(p.song.subsongNr + 1));
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

        try {
            player.init();
            player.loadSong(dataBuf, dataBuf.length);
            player.initSubsong(Math.min(songNo, player.song.subsongNr));

            output = new AHXOutput();
            output.player = player;
            output.init(setting.getOutputDevice().getSampleRate(), 16, 1, 256.0f, HZ);

            internalProduced = 0;
            internalConsumed = 0;
            initialized = true;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new IllegalStateException(e);
        }
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

    private int internalProduced = 0;
    private int internalConsumed = 0;

    @Override
    public int render(short[] b, int offset, int length) {
        if (!initialized) {
            return length;
        }
        if (frameCounter < 0) {
            frameCounter += length / 2;
            return length;
        }

        int written = 0;
        while (written < length - 1) {
            if (internalConsumed >= internalProduced) {
                output.mixBuffer();
                internalProduced = output.mixingBuffer.length;
                internalConsumed = 0;
                if (player.songEndReached != 0) {
                    player.songEndReached = 0;
                    curLoop++;
                }
            }

            int frames = Math.min((length - written) / 2, internalProduced - internalConsumed);
            for (int i = 0; i < frames; i++) {
                int sample = output.mixingBuffer[internalConsumed + i];
                if (sample > Short.MAX_VALUE) sample = Short.MAX_VALUE;
                else if (sample < Short.MIN_VALUE) sample = Short.MIN_VALUE;
                // mono to stereo
                b[offset + written + i * 2] = (short) sample;
                b[offset + written + i * 2 + 1] = (short) sample;

                processOneFrame();
                this.visWB.enq((short) sample, (short) sample);
            }

            written += frames * 2;
            internalConsumed += frames;
        }

        return written;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        this.visWB.copy(dest);
    }

    final VisWaveBuffer visWB = new VisWaveBuffer();
}
