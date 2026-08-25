/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.smaf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.lib.smaf.MmfToolPlayer;
import mdplayer.lib.smaf.SmafFile;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * SMAF (".mmf") driver.
 * <p>
 * There is no MA-2/3/5 emulator to write chip registers to, so this drives the real thing:
 * mmftoolc.exe, running on an emulated PC, whose {@code waveOut} samples arrive here through
 * {@link MmfToolPlayer}. Like the hvl, sid and psf drivers it therefore overrides
 * {@link #render} rather than feeding a chip.
 * <p>
 * The emulated player takes several seconds to load its instrument tables before it makes a
 * sound. Those seconds are spent at whatever speed the host can manage - the emulation is only
 * held to real time once there is audio to hold it to - so a song starts a few seconds after it
 * is asked for.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-10 nsano initial version <br>
 */
public class SmafDriver extends BaseDriver {

    private static final Logger logger = getLogger(SmafDriver.class.getName());

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

    private MmfToolPlayer player;

    /** what the emulated player has produced and this driver has not yet used up */
    private final byte[] source = new byte[8192];
    private int sourcePos;
    private int sourceLen;

    /** the number of bytes one frame of the emulated player's output takes */
    private int frameSize = 4;

    // the emulated player's rate to the output rate resampler, as in PsfDriver
    private double step = 1;
    private double frac;
    private int curL, curR, prevL, prevR;

    /** how many output frames were silence because the emulator had not caught up */
    private long underruns;

    /** the emulator had nothing this call; asking it again per frame would stall the render */
    private boolean starved;

    public SmafDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public SmafDriver() {
        this(null); // gross
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        SmafFile smaf;
        try {
            smaf = SmafFile.decode(buf);
        } catch (IOException e) {
logger.log(Level.DEBUG, "not a smaf: " + e.getMessage());
            return null;
        }

        MetaData md = new MetaData();
        String title = smaf.getTitle() != null ? smaf.getTitle() : "";
        md.set(Tag.Title, title);
        md.set(Tag.TitleJ, title);
        set(md, Tag.Composer, smaf.getComposer());
        set(md, Tag.ComposerJ, smaf.getComposer());
        set(md, Tag.Maker, smaf.getCopyright());
        set(md, Tag.Note, smaf.getComment());
        md.set(Tag.NumberOfSongs, "1");
        // the driver registers no chip, so this is what names it on the fmdsp header
        md.set(Tag.Chip, smaf.getFormat().label);

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
        // the emulated player's own startup is the latency here, and it is far longer than
        // anything the setting would ask for; there is nothing to prime
        frameCounter = 0;
        speed = 1;
        speedCounter = 0;
        underruns = 0;

        metaData = retrieveMetaData(dataBuf);

        stopPlayer();
        player = new MmfToolPlayer(dataBuf);
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
        // the rate the emulated player picks is not known until it opens its device, and it is
        // always 48000; step is recomputed once the first samples turn up
        step = (double) player.getSampleRate() / setting.getOutputDevice().getSampleRate();
    }

    /** the emulated player, for a test or a probe that wants to see what it has been up to */
    public MmfToolPlayer getPlayer() {
        return player;
    }

    /**
     * Where in the song the frame about to be rendered falls [ms], or {@code NaN} while the
     * emulated player has not said enough for that to be known. This is what the visualizer is
     * shown against - see {@link mdplayer.fmdsp.SmafReader}.
     * <p>
     * Measured from what has been rendered rather than from what has been taken: this reads the
     * player a buffer at a time, and the tail of that buffer is sound nobody has heard yet.
     */
    public double getSongMillis() {
        if (player == null) {
            return Double.NaN;
        }
        int pending = frameSize > 0 ? (sourceLen - sourcePos) / frameSize : 0;
        return player.getSongMillis(pending);
    }

    /** shuts the emulated machine down, which is also what frees the one machine slot in the JVM */
    public void stopPlayer() {
        if (player != null) {
logger.log(Level.DEBUG, "smaf: stopping, " + underruns + " underruns");
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
     * Takes the emulated player's next frame.
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
            // once the song is over nothing more is coming, and waiting the timeout out for it
            // would hang the player: it renders two frames at a time, so a fade-out of a hundred
            // thousand samples would take twenty five thousand of these waits - hours of them.
            // That is what "the song freezes at its end" was.
            long timeout = stopped ? 0 : READ_TIMEOUT_MILLIS;
            sourceLen = player.read(source, 0, source.length - source.length % frameSize, timeout);
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

    /** advances the emulated player by one of its own samples, or notes that it had none */
    private void emulateOneSample() {
        if (!nextSourceSample()) {
            curL = 0;
            curR = 0;
            if (player.isFinished()) {
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

        return length;
    }
}
