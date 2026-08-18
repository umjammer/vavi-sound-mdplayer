/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp7;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.lib.fmp7.Fmp7File;
import mdplayer.lib.fmp7.Fmp7Player;
import mdplayer.lib.fmp7.Fmp7Work;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * FMP7 (".owi") driver.
 * <p>
 * There is no emulator here to write OPNA registers to: FMP7 is a Windows program that
 * synthesizes its own sound, so this drives the real thing on an emulated PC and takes what
 * comes out - see {@link Fmp7Player}. Like the hvl, sid and psf drivers it therefore overrides
 * {@link #render} rather than feeding a chip.
 * <p>
 * FMP7 spends its first seconds loading before it makes a sound, and those seconds are spent at
 * whatever speed the host can manage - the emulation is only held to real time once there is
 * audio to hold it to - so a song starts a moment after it is asked for.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 */
public class Fmp7Driver extends BaseDriver {

    private static final Logger logger = getLogger(Fmp7Driver.class.getName());

    /**
     * How long to wait for the emulator to come up with the next samples before playing silence.
     * <p>
     * Waiting rather than filling in silence is what makes this work at both speeds it is asked
     * to run at: playing, the output device paces the render loop and the emulator - which runs
     * ahead of real time - is never actually waited on; rendering to a file, the loop takes
     * samples as fast as it can and this is what holds it to the emulator's pace instead of
     * writing out a file full of holes.
     */
    private static final long READ_TIMEOUT_MILLIS = 500;

    private Fmp7Player player;

    /** what FMP7 has produced and this driver has not yet used up */
    private final byte[] source = new byte[8192];
    private int sourcePos;
    private int sourceLen;

    /** the number of bytes one frame of FMP7's output takes */
    private int frameSize = 4;

    // FMP7's rate to the output rate resampler, as in PsfDriver
    private double step = 1;
    private double frac;
    private int curL, curR, prevL, prevR;

    /** how many output frames were silence because the emulator had not caught up */
    private long underruns;

    /** the emulator had nothing this call; asking it again per frame would stall the render */
    private boolean starved;

    /** what FMP7 said it was playing when the frame about to be rendered was made */
    private final Fmp7Work work = new Fmp7Work(Fmp7Work.GLOBAL_SIZE, Fmp7Work.PART_SIZE);

    /** whether {@link #work} has ever been filled in */
    private boolean hasWork;

    public Fmp7Driver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public Fmp7Driver() {
        this(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        Fmp7File fmp7;
        try {
            fmp7 = Fmp7File.decode(buf);
        } catch (IOException e) {
logger.log(Level.DEBUG, "not an fmp7 song: " + e.getMessage());
            return null;
        }

        MetaData md = new MetaData();
        String title = fmp7.getTitle() != null ? fmp7.getTitle() : "";
        md.set(Tag.Title, title);
        md.set(Tag.TitleJ, title);
        set(md, Tag.Composer, fmp7.getComposer());
        set(md, Tag.ComposerJ, fmp7.getComposer());
        set(md, Tag.Maker, fmp7.getCreator());
        set(md, Tag.Note, fmp7.getComment());
        md.set(Tag.NumberOfSongs, "1");
        // the driver registers no chip, so this is what names it on the fmdsp header
        md.set(Tag.Chip, "FMP7");

        this.metaData = md;
        return md;
    }

    private static void set(MetaData md, Tag tag, String value) {
        if (value != null && !value.isEmpty()) {
            md.set(tag, value);
        }
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
        // FMP7's own startup is the latency here, and it is far longer than anything the setting
        // would ask for; there is nothing to prime
        frameCounter = 0;
        speed = 1;
        speedCounter = 0;
        underruns = 0;
        hasWork = false;

        metaData = getMetaData(dataBuf);

        stopPlayer();
        // the song's own directory comes with it: a song that uses PCM names its sample bank
        // there, and without it the PCM parts play silent - see Fmp7Player#copySongData
        player = new Fmp7Player(dataBuf, plugin != null ? plugin.playingFilePath : null);
        try {
            player.start();
        } catch (IOException e) {
            player = null;
            throw new UncheckedIOException(e);
        }

        sourcePos = 0;
        sourceLen = 0;
        frac = 0;
        curL = curR = prevL = prevR = 0;
        // the rate FMP7 picks is not known until it opens its device, and it is always 48000;
        // step is recomputed once the first samples turn up
        step = (double) player.getSampleRate() / setting.getOutputDevice().getSampleRate();
    }

    /** the emulated player, for a test or a probe that wants to see what it has been up to */
    public Fmp7Player getPlayer() {
        return player;
    }

    /**
     * What FMP7 said it was playing when the sound now being rendered was made, or null while it
     * has not said anything yet. This is what the visualizer is shown against - see
     * {@link mdplayer.fmdsp.Fmp7Reader}.
     */
    public Fmp7Work getWork() {
        return hasWork ? work : null;
    }

    /** shuts the emulated machine down, which is also what frees the one machine slot in the JVM */
    public void stopPlayer() {
        if (player != null) {
logger.log(Level.DEBUG, "fmp7: stopping, " + underruns + " underruns");
            player.stop();
            player = null;
        }
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;

        speedCounter += speed;
        while (speedCounter >= 1.0 && !stopped) {
            speedCounter -= 1.0;
            if (frameCounter > -1) {
                counter++;
            } else {
                frameCounter++;
            }
        }
    }

    /**
     * Takes FMP7's next frame.
     *
     * @return false when there was nothing to take, which is either the end of the song or the
     *         emulator failing to keep up
     */
    private boolean nextSourceSample() {
        if (sourcePos + frameSize > sourceLen) {
            if (starved) {
                // already waited once this call and got nothing; waiting again for every frame
                // of the buffer would turn one render into minutes of them
                return false;
            }
            sourceLen = player.read(source, 0, source.length - source.length % frameSize, READ_TIMEOUT_MILLIS);
            sourcePos = 0;
            if (sourceLen < frameSize) {
                sourceLen = 0;
                starved = true;
                return false;
            }
            frameSize = player.getChannels() * 2;
            step = (double) player.getSampleRate() / setting.getOutputDevice().getSampleRate();
        }
        int l = (short) ((source[sourcePos] & 0xff) | (source[sourcePos + 1] << 8));
        int r = frameSize >= 4 ? (short) ((source[sourcePos + 2] & 0xff) | (source[sourcePos + 3] << 8)) : l;
        sourcePos += frameSize;
        curL = l;
        curR = r;
        return true;
    }

    /** advances FMP7 by one of its own samples, or notes that it had none */
    private void emulateOneSample() {
        if (!nextSourceSample()) {
            curL = 0;
            curR = 0;
            if (player.isFinished() || player.isSongOver()) {
                // the machine has gone, or FMP7 has stopped playing and everything it made has
                // been heard - which is how a song that does not loop ends
                if (!stopped) {
                    if (player.isFinished() && !player.isSongOver()) {
                        // the machine went while the song was still going, which is not the song
                        // ending - it is the emulated PC dying under it, and saying so is better
                        // than a track that quietly stops early and looks like a short song
logger.log(Level.WARNING, "fmp7: the emulated PC went while the song was still playing, %.1fs in"
        .formatted(getSongMillis() / 1000), player.getFailure());
                    } else {
logger.log(Level.DEBUG, "fmp7: the song ended after %.1fs".formatted(getSongMillis() / 1000));
                    }
                }
                stopped = true;
            } else if (player.isSounding()) {
                underruns++;
            }
        }
    }

    @Override
    public int render(short[] b, int offset, int length) {
        if (player == null) {
            return length;
        }
        starved = false;

        for (int i = 0; i < length - 1; i += 2) {
            int l, r;
            if (step == 1.0) {
                emulateOneSample();
                l = curL;
                r = curR;
            } else {
                frac += step;
                while (frac >= 1.0) {
                    frac -= 1.0;
                    prevL = curL;
                    prevR = curR;
                    emulateOneSample();
                }
                l = (int) (prevL + (curL - prevL) * frac);
                r = (int) (prevR + (curR - prevR) * frac);
            }

            b[offset + i] = (short) l;
            b[offset + i + 1] = (short) r;

            processOneFrame();
            if (isWatched()) {
                // the call boxes both samples and allocates an array for them, forty thousand
                // times a second, whether anything is listening or not
                fireEventHappened(this, "wave.buffer", (short) l, (short) r);
            }
        }

        readWork();
        return length;
    }

    /**
     * Catches the work up to the sound just rendered, once a buffer rather than once a frame:
     * the tail of what has been taken from the player is sound nobody has heard yet, so it is
     * stepped back over.
     * <p>
     * The loop count comes from here too, which is why it is read against the sound rather than
     * against what the emulator has run ahead to: a song is not over until the last time round
     * has been heard.
     */
    private void readWork() {
        int pending = frameSize > 0 ? (sourceLen - sourcePos) / frameSize : 0;
        if (!player.workAt(pending, work)) {
            return;
        }
        hasWork = true;
        curLoop = work.loop();
    }

    /**
     * Where in the song the frame about to be rendered falls [ms], or {@code NaN} while FMP7 has
     * not said enough for that to be known.
     */
    public double getSongMillis() {
        return hasWork ? work.playMillis() : Double.NaN;
    }
}
