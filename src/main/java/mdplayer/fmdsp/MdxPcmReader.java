/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.fmdsp;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import mdplayer.ChipRegister;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.driver.mxdrv.MxDriver;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;


/**
 * The PCM parts of an MDX, which are MXDRV's eight and not any chip's.
 * <p>
 * An MDX is sixteen parts: eight FM ones, which the {@link Ym2151Reader OPM} shows because they
 * are written to it as registers, and eight PCM ones which are not a chip's at all. They are
 * played by handing PCM8 a sample address, a length and a mode word - nothing that can be read
 * back, and nothing that stays anywhere once the sample is running - so the only place they exist
 * is MXDRV's own work area, which is where this reads them from.
 * <p>
 * A PCM part's note picks a PDX sample rather than a pitch: o0c is sample 0 and o7b is sample 95,
 * so the keyboard shows the note the MML wrote and the tone number shows which sample it reached.
 * Until the song runs the PCM8 command there is only the one part, played on the X68000's ADPCM,
 * and only the ADPCM row appears.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-25 nsano initial version <br>
 * @see mdplayer.driver.mxdrv.MXDRV#getPcmPart
 */
public class MdxPcmReader implements FmDspChipReader {

    private Supplier<BaseDriver> driver;

    private final MXDRV.PcmPart part = new MXDRV.PcmPart();

    private final boolean[] prevKeyOns = new boolean[MXDRV.PCM_PARTS];
    private final int[] prevNotes = new int[MXDRV.PCM_PARTS];
    private final int[] prevLengths = new int[MXDRV.PCM_PARTS];
    private boolean active;

    @Override
    public String chipName() {
        return "PCM8";
    }

    @Override
    public void bind(ChipRegister chipRegister) {
    }

    @Override
    public void bind(Supplier<BaseDriver> driver) {
        this.driver = driver;
    }

    @Override
    public void reset() {
        Arrays.fill(prevKeyOns, false);
        Arrays.fill(prevNotes, -1);
        Arrays.fill(prevLengths, 0);
        active = false;
    }

    @Override
    public Set<Group> groups() {
        return EnumSet.of(Group.PCM);
    }

    @Override
    public int priority() {
        return 45;
    }

    @Override
    public boolean ready() {
        return mxdrv() != null;
    }

    /** the playing driver, when it is an MDX one that has been started */
    private MXDRV mxdrv() {
        BaseDriver d = driver == null ? null : driver.get();
        return d instanceof MxDriver mx ? mx.getMxdrv() : null;
    }

    @Override
    public boolean active(Group group) {
        if (!active) {
            for (int ch = 0; ch < channels(group) && !active; ch++) {
                active = partOf(ch) && part.keyOn;
            }
        }
        return active;
    }

    /**
     * One part before the PCM8 command, eight after it: an MDX that never sends it plays its ninth
     * part on the ADPCM alone, and seven empty rows would say it had a PCM section it has not.
     */
    @Override
    public int channels(Group group) {
        MXDRV mxdrv = mxdrv();
        return mxdrv != null && mxdrv.isPcm8Mode() ? MXDRV.PCM_PARTS : 1;
    }

    /** reads part {@code ch} into {@link #part}, the way every one of these starts */
    private boolean partOf(int ch) {
        MXDRV mxdrv = mxdrv();
        return mxdrv != null && mxdrv.getPcmPart(ch, part);
    }

    @Override
    public void read(Group group, int ch, FmDspChannel out) {
        MXDRV mxdrv = mxdrv();
        if (mxdrv == null || !mxdrv.getPcmPart(ch, part)) return;

        boolean pcm8 = mxdrv.isPcm8Mode();
        out.name = pcm8 ? "PCM" : "ADPCM";
        out.num = ch + 9; // the MDX part, which is the ninth one onwards
        out.sounding = part.keyOn;
        // the key going down between two snapshots is the usual edge, but a part playing the same
        // sample twice over has come and gone inside one - it is the note length reloading that
        // says so, see PcmPart#length
        out.keyOn = part.keyOn && (!prevKeyOns[ch] || part.note != prevNotes[ch]
                || part.length > prevLengths[ch]);
        prevKeyOns[ch] = part.keyOn;
        prevNotes[ch] = part.note;
        prevLengths[ch] = part.length;

        // the note is a sample number, so it is exact and there is nothing for it to be detuned by
        out.note = part.note < keys ? part.note : -1;
        out.toneNum = part.sample;
        out.volume = part.volume;
        out.pan = panOf(part.pan);
        out.amplitude = amplitude(pcm8, part);
    }

    /** the notes the keyboard has, which is also how many samples a PDX bank holds */
    private static final int keys = 96;

    /**
     * The gain PCM8 gives each of its sixteen levels, as sixteenths - a copy of the emulator's
     * {@code x68sound.Global.PCM8VOLTBL}, which is not visible from here. It is a table rather than
     * a scale, which is why the meter reads it instead of putting a dB curve over the level number.
     */
    private static final int[] levels = {2, 3, 4, 5, 6, 8, 10, 12, 16, 20, 24, 32, 40, 48, 64, 80};

    /**
     * How loud the part is, against the loudest PCM8 will play one. In ADPCM mode there is no level
     * at all - the volume never leaves the work area, see {@code MXDRV.L000e7e} - so a part which
     * is sounding is shown at full height.
     */
    private static double amplitude(boolean pcm8, MXDRV.PcmPart part) {
        if (!part.keyOn || part.pan == 0) return 0;
        if (!pcm8) return 1;
        if (part.level < 0) return 0; // faded out
        return levels[Math.min(part.level, levels.length - 1)] / (double) levels[levels.length - 1];
    }

    /** PCM8's mode word pans by which speakers it adds the part to: bit 0 left, bit 1 right */
    private static Pan panOf(int pan) {
        return switch (pan & 3) {
            case 1 -> Pan.LEFT;
            case 2 -> Pan.RIGHT;
            case 3 -> Pan.CENTER;
            default -> Pan.NONE;
        };
    }
}
