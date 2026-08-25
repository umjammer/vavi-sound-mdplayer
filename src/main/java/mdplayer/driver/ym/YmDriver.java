/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ym;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.ym.Ym2149Ex;
import mdplayer.lib.ym.YmMusic;
import mdplayer.lib.ym.YmMusic.YmMusicInfo;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * YmDriver.
 * <p>
 * YM (Atari ST, stsound) has its own renderer, so this driver overrides
 * {@link #render} like the sid driver does.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-03 nsano initial version <br>
 */
public class YmDriver extends BaseDriver {

    private static final Logger logger = getLogger(YmDriver.class.getName());

    /** Atari ST YM2149 master clock */
    private static final long ATARI_CLOCK = 2_000_000;

    private YmMusic music;
    private int lastPos;

    public YmDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public YmDriver() {
        this(null); // gross
    }

    private static YmMusic createYmMusic(int rate) {
        YmMusic music = new YmMusic(rate);
        Ym2149Ex chip = new Ym2149Ex(ATARI_CLOCK, 1, rate);
        music.ymChip = new YmMusic.Ym2149Ex() {
            @Override
            public void setClock(int clock) {
                chip.setClock(clock);
            }

            @Override
            public void reset() {
                chip.reset();
            }

            @Override
            public void writeRegister(int reg, int value) {
                chip.writeRegister(reg, value);
            }

            @Override
            public int readRegister(int reg) {
                return chip.readRegister(reg);
            }

            @Override
            public void update(short[] buffer, int length) {
                chip.update(buffer, length);
            }

            @Override
            public void sidStart(int voice, int freq, int volume) {
                chip.sidStart(voice, freq, volume);
            }

            @Override
            public void sidSinStart(int voice, int freq, int volume) {
                chip.sidSinStart(voice, freq, volume);
            }

            @Override
            public void sidStop(int voice) {
                chip.sidStop(voice);
            }

            @Override
            public void drumStart(int voice, byte[] data, int size, int freq) {
                chip.drumStart(voice, data, size, freq);
            }

            @Override
            public void syncBuzzerStart(int freq, int volume) {
                chip.syncBuzzerStart(freq, volume);
            }

            @Override
            public void syncBuzzerStop() {
                chip.syncBuzzerStop();
            }
        };
        return music;
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        if (buf == null || buf.length < 4) return null;

        YmMusic m = createYmMusic(setting.getOutputDevice().getSampleRate());
        try {
            m.loadMemory(buf, buf.length);
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.getMessage(), e);
            return null;
        }

        YmMusicInfo info = new YmMusicInfo();
        m.getMusicInfo(info);
        m.unLoad();

        MetaData md = new MetaData();
        md.set(Tag.Title, info.pSongName);
        md.set(Tag.Composer, info.pSongAuthor);
        md.set(Tag.Note, info.pSongComment);
        md.set(Tag.NumberOfSongs, "1");
        return md;
    }

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

        metaData = retrieveMetaData(dataBuf);

        music = createYmMusic(setting.getOutputDevice().getSampleRate());
        try {
            music.loadMemory(dataBuf, dataBuf.length);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        music.setLoopMode(music.isLoopable());
        music.stop();
        music.play();
        lastPos = 0;

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

    private final short[] internalBuffer = new short[1024];
    private int internalProduced = 0;
    private int internalConsumed = 0;

    @Override
    public int render(short[] b, int offset, int length) {
        if (music == null) {
            return length;
        }
        if (frameCounter < 0) {
            frameCounter += length / 2;
            return length;
        }

        int written = 0;
        while (written < length - 1) {
            if (internalConsumed >= internalProduced) {
                boolean active = music.update(internalBuffer, internalBuffer.length);
                if (!active || music.bMusicOver) {
                    stopped = true;
                }
                internalProduced = internalBuffer.length;
                internalConsumed = 0;
                int pos = music.getPos();
                if (pos < lastPos) curLoop++;
                lastPos = pos;
            }

            int frames = Math.min((length - written) / 2, internalProduced - internalConsumed);
            for (int i = 0; i < frames; i++) {
                short sample = internalBuffer[internalConsumed + i];
                // mono to stereo
                b[offset + written + i * 2] = sample;
                b[offset + written + i * 2 + 1] = sample;

                processOneFrame();
                fireEventHappened(this, "wave.buffer", sample, sample);
            }

            written += frames * 2;
            internalConsumed += frames;
        }

        return written;
    }

    /**
     * The YM2149's registers as the chip has them, for the visualizer. The Atari's music is
     * rendered here rather than through a {@link mdplayer.ChipRegister} chip, so this is the only
     * way to see what its three channels are doing.
     *
     * @return null before the song has started
     */
    public int[] getPsgRegisters() {
        if (music == null || music.ymChip == null) return null;
        int[] regs = new int[16];
        for (int i = 0; i < regs.length; i++) {
            regs[i] = music.ymChip.readRegister(i) & 0xff;
        }
        return regs;
    }
}
