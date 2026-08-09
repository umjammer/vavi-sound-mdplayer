/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.lib.psf.Psf2Engine;
import mdplayer.lib.psf.PsfFile;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.compat.Tuple;

import static java.lang.System.getLogger;


/**
 * PSF2 (Sony PlayStation 2) driver.
 * <p>
 * A PSF2 is a different machine from a PSF1 - an IOP running IRX modules out of a virtual
 * filesystem, with the two core SPU2 rather than the PS1's SPU - so it gets its own driver,
 * sharing only the container format and the R3000 with the PSF1 one.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class Psf2Driver extends BaseDriver {

    private static final Logger logger = getLogger(Psf2Driver.class.getName());

    /** the SPU2 is wired to this rate, whatever the output device wants */
    public static final int PSX_RATE = 44100;

    /** aosdk renders a video frame's worth of samples, then ticks the frame */
    private static final int SAMPLES_PER_FRAME = PSX_RATE / 60;

    private Psf2Engine engine;

    /** where the fade starts and ends, in emulated samples; -1 when the file names no length */
    private long decayBegin = -1;
    private long decayEnd = -1;
    private long emulatedSamples;

    private int frameSamples;

    // the 44100 Hz to output rate resampler
    private double step = 1;
    private double frac;
    private int curL, curR, prevL, prevR;

    public Psf2Driver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public Psf2Driver() {
        this(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        PsfFile psf;
        try {
            psf = PsfFile.decode(buf);
        } catch (IOException e) {
logger.log(Level.DEBUG, "not a psf2: " + e.getMessage());
            return null;
        }

        MetaData md = new MetaData();
        String title = psf.tag("title");
        md.set(Tag.Title, title != null ? title : "");
        md.set(Tag.TitleJ, title != null ? title : "");
        set(md, Tag.GameTitle, psf.tag("game"));
        set(md, Tag.GameTitleJ, psf.tag("game"));
        set(md, Tag.Composer, psf.tag("artist"));
        set(md, Tag.ComposerJ, psf.tag("artist"));
        set(md, Tag.Maker, psf.tag("copyright"));
        set(md, Tag.Converter, psf.tag("psfby"));
        set(md, Tag.Note, psf.tag("comment"));
        md.set(Tag.NumberOfSongs, "1");

        double length = psf.length();
        if (length > 0) {
            int seconds = (int) length;
            md.set(Tag.Duration, "%d:%02d".formatted(seconds / 60, seconds % 60));
        }

        this.metaData = md;
        return md;
    }

    private static void set(MetaData md, Tag tag, String value) {
        if (value != null) {
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
        frameCounter = -latency - waitTime;
        speed = 1;
        speedCounter = 0;

        metaData = getMetaData(dataBuf);

        PsfFile[] files;
        try {
            files = PsfFile.load(dataBuf, this::resolveLib);
            engine = new Psf2Engine();
            engine.start(files);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        double length = files[0].length();
        double fade = files[0].fade();
        if (length == 0) {
            decayBegin = -1;
            decayEnd = -1;
        } else {
            decayBegin = (long) (length * PSX_RATE);
            decayEnd = decayBegin + (long) (fade * PSX_RATE);
            totalCounter = (long) (decayEnd * (double) setting.getOutputDevice().getSampleRate() / PSX_RATE);
        }
        emulatedSamples = 0;
        frameSamples = 0;

        step = (double) PSX_RATE / setting.getOutputDevice().getSampleRate();
        frac = 0;
        curL = curR = prevL = prevR = 0;
    }

    /** the "_lib" tags name files that came along as {@link BasePlugin#getExtendFiles extend files} */
    private byte[] resolveLib(String name) {
        if (plugin == null) {
            return null;
        }
        List<Tuple<String, byte[]>> files = plugin.getExtendFiles();
        if (files == null) {
            return null;
        }
        for (Tuple<String, byte[]> f : files) {
            if (f.getItem1().equalsIgnoreCase(name)) {
                return f.getItem2();
            }
        }
        return null;
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

    /** advances the emulation by one 44100 Hz sample */
    private void emulateOneSample() {
        engine.sample();

        curL = engine.getLeft();
        curR = engine.getRight();

        if (decayBegin >= 0 && emulatedSamples >= decayBegin) {
            if (emulatedSamples >= decayEnd) {
                curL = 0;
                curR = 0;
                stopped = true;
            } else {
                int fader = 256 - (int) (256 * (emulatedSamples - decayBegin) / (decayEnd - decayBegin));
                curL = (curL * fader) >> 8;
                curR = (curR * fader) >> 8;
            }
        }
        // the IOP dropping into its null state is how a PSF2 says it is finished
        if (engine.hw.songDone) {
            stopped = true;
        }

        emulatedSamples++;

        frameSamples++;
        if (frameSamples >= SAMPLES_PER_FRAME) {
            frameSamples = 0;
            engine.frame();
        }
    }

    @Override
    public int render(short[] b, int offset, int length) {
        if (engine == null) {
            return length;
        }
        if (frameCounter < 0) {
            frameCounter += length / 2;
            return length;
        }

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
            fireEventHappened(this, "wave.buffer", (short) l, (short) r);
        }

        return length;
    }
}
