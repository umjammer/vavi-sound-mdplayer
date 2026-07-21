/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import mdplayer.driver.BaseDriver;
import mdplayer.fmdsp.FmDspChannel;
import mdplayer.fmdsp.FmDspChipReader;
import mdplayer.fmdsp.FmDspChipReader.Group;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData.Tag;
import vavi.sound.visualizer.fmdsp.FftAnalyzer;
import vavi.sound.visualizer.fmdsp.FftDataSource;
import vavi.sound.visualizer.fmdsp.FmDspDataSource;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackInfo;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.sound.visualizer.fmdsp.TrackStatusSource;
import vavi.sound.visualizer.fmdsp.WorkStateSource;
import vavi.util.event.GenericEvent;


/**
 * Feeds {@link FmDspVisualizer} from the shared chip register caches, independent of the sound
 * driver playing.
 * <p>
 * Every driver funnels its chip writes through the {@link ChipRegister} chip wrappers, which cache
 * the register file and the key-on state. This source polls those caches through the
 * {@link FmDspChipReader}s found by {@link ServiceLoader} - it names no chip and no driver, so it
 * works for any format mdplayer plays. Subscribe {@link #update(GenericEvent)} to the audio event
 * stream: the "master" event carries the rendered PCM, which clocks the ~120 Hz snapshot and feeds
 * the spectrum.
 * <p>
 * The fmdsp rows are grouped ({@linkplain Group#FM FM}, {@linkplain Group#SSG SSG},
 * {@linkplain Group#PCM PCM}, the {@linkplain Group#RHYTHM drum meter}) and each group's rows are
 * assigned to the readers that report activity, in the order they become active - whatever chips a
 * song actually plays claim the screen. Compared to a driver-specific source
 * ({@link mdplayer.driver.pmd.PmdFmDspSource}, {@link mdplayer.driver.fmp.FmpFmDspSource}) three
 * things are approximations:
 * <ul>
 * <li>the MML columns. Ticks, gate and detune live in the driver work area only, so they stay 0;
 * the key is decoded from the chip's frequency registers instead of the driver's note.</li>
 * <li>the level meters, which are synthesized from the level registers and a decay envelope
 * retriggered on every key-on edge, exactly like the driver-specific sources do.</li>
 * <li>key-on edges. The caches are polled, so a channel retriggered with the very same register
 * state between two snapshots is missed.</li>
 * </ul>
 * Pre-timed streams such as VGM never program TimerB; the tick counter - and with it the clock and
 * the circle animation - would freeze, so it falls back to a synthesized {@link #defaultTimerB}
 * tempo.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
public class ChipFmDspSource implements FmDspDataSource, LevelDataSource, TrackStatusSource, WorkStateSource {

    /** how fast a held note's meter sags, per snapshot */
    private static final double levelSustainDecay = 0.995;

    /** how far a held note's meter sags, as a fraction of the register level */
    private static final double levelSustain = 0.6;

    /** how fast a released note's meter falls, per snapshot */
    private static final double levelRelease = 0.95;

    /** the drum meter has no per-voice level, a hit shows at a fixed height */
    private static final double rhythmLevel = 0.75;

    /** one TimerB step [Hz], the OPNA's clock / 72 / 2 - what the fmdsp clock is calibrated to */
    private static final double timerBStepHz = 7987200.0 / 72 / 2;

    /** the tempo the tick counter falls back to when no chip programs TimerB (VGM and friends) */
    private static final int defaultTimerB = 200;

    /**
     * The clock the note length bar is measured against [Hz].
     * <p>
     * Deliberately not the TimerB tick: that runs at ~62 Hz for a stream that programs no TimerB
     * and ~133 Hz for a typical OPM song, so a sixteenth note measured barely one or two of the
     * bar's 64 columns - four counts each - and the bar read as broken. PMD and FMP do not have
     * this problem because they read a length out of the part in the driver's own clock, where a
     * quarter note is 96. This rate reproduces that scale: a quarter note at 120 bpm is 120 counts,
     * a sixteenth is 30, and the bar's 255 count full scale is a bit over a second.
     */
    private static final double noteTickHz = 240;

    /** snapshots per second, the cadence the driver-specific sources use as well */
    private static final int snapshotRate = 120;

    /** rows and level channels of each group; -1 marks a row without a meter */
    private static final Map<Group, int[]> rows = new EnumMap<>(Map.of(
            Group.FM, new int[] {
                    TrackId.FM_1.ordinal(), TrackId.FM_2.ordinal(), TrackId.FM_3.ordinal(),
                    TrackId.FM_4.ordinal(), TrackId.FM_5.ordinal(), TrackId.FM_6.ordinal(),
                    TrackId.FM_3_EX_1.ordinal(), TrackId.FM_3_EX_2.ordinal(), TrackId.FM_3_EX_3.ordinal()},
            Group.SSG, new int[] {
                    TrackId.SSG_1.ordinal(), TrackId.SSG_2.ordinal(), TrackId.SSG_3.ordinal()},
            Group.PCM, new int[] {
                    TrackId.ADPCM.ordinal(),
                    TrackId.PPZ8_1.ordinal(), TrackId.PPZ8_2.ordinal(), TrackId.PPZ8_3.ordinal(),
                    TrackId.PPZ8_4.ordinal(), TrackId.PPZ8_5.ordinal(), TrackId.PPZ8_6.ordinal(),
                    TrackId.PPZ8_7.ordinal(), TrackId.PPZ8_8.ordinal()}));

    /**
     * The order the meter columns are handed out in, which is the order of the original strip:
     * FM, SSG, the drum meter, then PCM.
     */
    private static final Group[] meterOrder = {Group.FM, Group.SSG, Group.RHYTHM, Group.PCM};

    /**
     * Columns each group keeps even when nothing claims it, so the familiar PC-98 strip - FM at
     * 0, SSG at 6, the drums at 9, PCM from 10 - survives a song that happens to use only some
     * of them. A group whose chips need more than this pushes the ones behind it along.
     */
    private static final Map<Group, Integer> meterMinimums = new EnumMap<>(Map.of(
            Group.FM, 6, Group.SSG, 3, Group.RHYTHM, 1, Group.PCM, 1));

    /** reused instances, like the chips themselves */
    private static final ServiceLoader<FmDspChipReader> readerLoader = ServiceLoader.load(FmDspChipReader.class);

    private final List<FmDspChipReader> readers = new ArrayList<>();

    /** latest snapshot, written by the driver thread, read by the EDT */
    private final TrackStatus[] tracks = new TrackStatus[TrackId.COUNT];

    /** a row is shown "playing" once its channel keyed on during this song */
    private final boolean[] used = new boolean[TrackId.COUNT];

    /** display name and number of each row, from the reader that fills it */
    private final String[] rowNames = new String[TrackId.COUNT];

    private final int[] rowNums = new int[TrackId.COUNT];

    /** meter column labels, set at the first column of each claimed span */
    private final String[] levelLabels = new String[LevelDataSource.COUNT];

    /** the row feeding each meter column, so its key/pan readout follows the same chip */
    private final TrackId[] levelTracks = new TrackId[LevelDataSource.COUNT];

    /** first meter column of each claimed reader, parallel to {@link #claims} */
    private final Map<Group, List<Integer>> meterBases = new EnumMap<>(Group.class);

    /** the rows in use, for {@code LeftMode.AUTO}; null until something sounds */
    private volatile TrackId[] dispTracks;

    private final double[] envelopes = new double[LevelDataSource.COUNT];

    /**
     * Tick count of each row's last key on, {@code -1} before the first one. The bar over the
     * keyboard wants the note's remaining length; a register file does not carry one, so the
     * length is measured between key ons instead - see {@link #noteLengths}.
     */
    private final long[] keyOnTicks = new long[TrackId.COUNT];

    /**
     * The measured length of each row's previous note, in {@link #noteTickHz} counts, {@code 0}
     * until the row has played two. A chip snapshot only learns a note's length once the next one
     * starts, so the bar shows the note that is playing counting down against the length of the one
     * before it - right for the steady runs that fill most parts, and re-scaled by the next key on
     * when the part changes. A sequenced driver reads the real length out of the part, which is why
     * {@code PmdFmDspSource} and {@code FmpFmDspSource} do not need any of this.
     */
    private final int[] noteLengths = new int[TrackId.COUNT];

    /** the note each row played at the last snapshot, {@code -1} while it rests */
    private final int[] lastNotes = new int[TrackId.COUNT];

    /** the bar is 64 columns of 4 counts each, see {@code BAR_CNT} */
    private static final int barFullScale = 255;

    /**
     * What the note clock is divided by before it reaches the bar, so that the longest note being
     * played fits the bar's 255 counts instead of pinning it full.
     * <p>
     * The clock itself has to be fine - a sixteenth note is only a fraction of a second, and at a
     * coarse rate it rounds away to no columns at all - but a fine clock alone makes anything over
     * a second sit at full scale. Dividing by a power of two chosen from the notes actually
     * sounding keeps both ends: short notes stay detailed, long ones still count down. It is one
     * divider for every row, so rows remain comparable with each other, and it follows the music
     * because a row that has stopped no longer counts towards it.
     */
    private int barScale = 1;

    private final Pan[] pans = new Pan[LevelDataSource.COUNT];

    /** row assignment of this song, in the order the readers became active */
    private final Map<Group, List<FmDspChipReader>> claims = new EnumMap<>(Group.class);

    /** which reader/channel currently fills a row, for {@link #masked} */
    private final FmDspChipReader[] rowReaders = new FmDspChipReader[TrackId.COUNT];
    private final Group[] rowGroups = new Group[TrackId.COUNT];
    private final int[] rowChannels = new int[TrackId.COUNT];

    private final FmDspChannel channel = new FmDspChannel();

    private ChipRegister chipRegister;

    private Supplier<BaseDriver> driver = () -> null;

    // work state

    private volatile BaseDriver work;
    private volatile boolean paused;
    private volatile long timerBCount;
    private volatile int timerB;
    private volatile long loopTimerBCount;
    private volatile long timerBCountLoop;

    private long lastCounter;
    private double timerBStep;
    /** the note length clock, see {@link #noteTickHz} */
    private long noteTicks;
    private double noteTickStep;
    private long loopStartTimerBCount;
    private int lastLoopCount;

    private String filename;
    private final String[] comments = new String[3];

    private final FftAnalyzer fft = new FftAnalyzer(Common.VGMProcSampleRate);

    /** frames pushed since the last snapshot */
    private int snapshotFrames;

    public ChipFmDspSource() {
        for (FmDspChipReader reader : readerLoader) {
            readers.add(reader);
        }
        readers.sort(Comparator.comparingInt(FmDspChipReader::priority));
        for (Group g : Group.values()) {
            claims.put(g, new ArrayList<>());
            meterBases.put(g, new ArrayList<>());
        }
        Arrays.setAll(tracks, i -> new TrackStatus());
        Arrays.fill(pans, Pan.CENTER);
        reset();
    }

    /** points this source at the playing plugin's chips and driver; call once per plugin */
    public void bind(BasePlugin<? extends BaseDriver> plugin) {
        bind(plugin.chipRegister, plugin::getDriver);
    }

    void bind(ChipRegister chipRegister, Supplier<BaseDriver> driver) {
        this.chipRegister = chipRegister;
        this.driver = driver != null ? driver : () -> null;
        readers.forEach(r -> r.bind(chipRegister));
        readers.forEach(r -> r.bind(this.driver));
    }

    /**
     * Clears everything that belongs to one song. The chips are shared singletons whose caches
     * carry over between songs, and the displayed clock only ever moves forwards - call this
     * before every song of a play list.
     */
    public void reset() {
        readers.forEach(FmDspChipReader::reset);
        claims.values().forEach(List::clear);
        meterBases.values().forEach(List::clear);
        Arrays.fill(rowReaders, null);
        Arrays.stream(tracks).forEach(ChipFmDspSource::clear);
        Arrays.fill(used, false);
        Arrays.fill(rowNames, null);
        Arrays.fill(rowNums, 0);
        Arrays.fill(levelLabels, null);
        Arrays.fill(levelTracks, null);
        dispTracks = null;
        Arrays.fill(envelopes, 0);
        Arrays.fill(keyOnTicks, -1);
        Arrays.fill(noteLengths, 0);
        Arrays.fill(lastNotes, -1);
        barScale = 1;
        Arrays.fill(pans, Pan.CENTER);
        Arrays.fill(comments, null);
        work = null;
        paused = false;
        timerBCount = 0;
        timerB = 0;
        timerBStep = 0;
        noteTicks = 0;
        noteTickStep = 0;
        lastCounter = 0;
        loopTimerBCount = 0;
        loopStartTimerBCount = 0;
        timerBCountLoop = 0;
        lastLoopCount = 0;
        snapshotFrames = 0;
        clock = 0;
        lastClockNanos = 0;
        shownFrames = 0;
    }

    /** the title shown on the file bar */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /** true while the player is paused, the chips don't know about it */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @param event "master" ... args 0: pcm buffer, 1: offset
     */
    public void update(GenericEvent event) {
        if (!"master".equals(event.getName())) return;

        short[] buffer = (short[]) event.getArguments()[0];
        int offset = (int) event.getArguments()[1];
        for (int i = offset; i + 1 < buffer.length; i += 2) {
            fft.push((buffer[i] + buffer[i + 1]) / 2f);
            snapshotFrames++;
        }
        if (snapshotFrames >= Common.VGMProcSampleRate / snapshotRate) {
            snapshotFrames = 0;
            snapshot();
        }
    }

    // ----- snapshot -----

    /** polls the chip caches once; package-private so a test can clock it directly */
    void snapshot() {
        for (FmDspChipReader reader : readers) {
            if (reader.ready()) reader.poll();
        }

        // readers claim a group's rows when they first turn active; the assignment then sticks
        // for the song, so the rows don't jump around
        for (Group g : Group.values()) {
            List<FmDspChipReader> claimed = claims.get(g);
            for (FmDspChipReader reader : readers) {
                if (!claimed.contains(reader) && reader.ready()
                        && reader.groups().contains(g) && reader.active(g)) {
                    claimed.add(reader);
                }
            }
        }

        allocateMeters();
        // before fill: the per-row note length bar measures itself against the tick counter,
        // and its scale comes from the notes the previous snapshot left sounding
        state();
        scaleBar();

        fill(Group.FM);
        fill(Group.SSG);
        fill(Group.PCM);
        rhythm();

        nameCollisions();
        updateDisplayTracks();
    }

    /**
     * Hands the meter columns to the claimed readers, in the strip's own group order and as many
     * as each says it needs - so a nine channel FM chip gets nine meters where the OPNA's six
     * plus its three extension rows get six. Each group keeps at least its
     * {@linkplain #meterMinimums classic span}, so the layout only moves for a chip that really
     * is wider. A song with more voices than the strip has columns loses the tail.
     */
    private void allocateMeters() {
        int cursor = 0;
        for (Group g : meterOrder) {
            List<FmDspChipReader> claimed = claims.get(g);
            List<Integer> bases = meterBases.get(g);
            bases.clear();

            int span = 0;
            for (FmDspChipReader reader : claimed) {
                bases.add(Math.min(cursor + span, LevelDataSource.COUNT));
                span += reader.meters(g);
            }
            cursor = Math.min(cursor + Math.max(span, meterMinimums.get(g)), LevelDataSource.COUNT);
        }
    }

    /**
     * Names a row for its chip where the section alone would be ambiguous.
     * <p>
     * Rows are labelled for what they are - FM, SSG, PCM - which reads better than repeating one
     * chip's name down the whole strip. But two different chips can be the same sort of thing at
     * once: an MSX has the PSG on the SSG rows and the SCC on the wider block, and both of them
     * are square wave channels. Where that happens the section is no use as a name, so those rows
     * take their chip's instead.
     */
    private void nameCollisions() {
        // decide every row against the names as they stand, then rename: doing it in one pass
        // would have a renamed row no longer match the rows it collided with, and the second of
        // any pair would keep the ambiguous name
        boolean[] shared = new boolean[rowNames.length];
        for (int row = 0; row < rowNames.length; row++) {
            if (rowNames[row] == null || rowReaders[row] == null) continue;
            for (int other = 0; other < rowNames.length && !shared[row]; other++) {
                shared[row] = rowNames[row].equals(rowNames[other]) && rowReaders[other] != null
                        && rowReaders[other] != rowReaders[row];
            }
        }
        for (int row = 0; row < rowNames.length; row++) {
            if (shared[row] && rowReaders[row].chipName() != null) {
                rowNames[row] = rowReaders[row].chipName();
            }
        }
    }

    /** the used rows in group order, capped at the 10 the left half fits */
    private void updateDisplayTracks() {
        List<TrackId> list = new ArrayList<>(10);
        for (Group g : new Group[] {Group.FM, Group.SSG, Group.PCM}) {
            for (int row : rows.get(g)) {
                if (used[row] && list.size() < 10) list.add(TrackId.values()[row]);
            }
        }
        dispTracks = list.isEmpty() ? null : list.toArray(new TrackId[0]);
    }

    private void fill(Group g) {
        int[] groupRows = rows.get(g);
        List<FmDspChipReader> claimed = claims.get(g);
        List<Integer> bases = meterBases.get(g);
        int slot = 0;
        for (int r = 0; r < claimed.size(); r++) {
            FmDspChipReader reader = claimed.get(r);
            int base = bases.get(r);
            for (int ch = 0; ch < reader.channels(g) && slot < groupRows.length; ch++, slot++) {
                int row = groupRows[slot];
                int meter = reader.meterOf(g, ch);
                int level = meter < 0 ? -1 : base + meter;
                if (level >= LevelDataSource.COUNT) level = -1;

                channel.clear();
                reader.read(g, ch, channel);

                if (channel.keyOn) used[row] = true;
                rowNames[row] = channel.name;
                rowNums[row] = channel.num > 0 ? channel.num : ch + 1;
                // the strip labels the start of a span, and every third column of a wide FM one
                if (level >= 0 && (meter == 0 || (g == Group.FM && meter % 3 == 0))) {
                    String label = labelOf(g, reader, channel.name, meter);
                    if (label != null) levelLabels[level] = label;
                }
                TrackStatus status = tracks[row];
                status.playing = used[row];
                status.info = channel.info;
                status.key = channel.sounding ? keyOf(channel.note) : 0xff;
                status.actualKey = status.key;
                status.volume = channel.volume;
                status.toneNum = channel.toneNum;
                status.ssgTone = channel.ssgTone;
                status.ssgNoise = channel.ssgNoise;
                status.ppz8Ch = channel.pcmCh;

                // a re-struck note re-attacks the meter. MXDRV keys off and back on inside one
                // snapshot, so its FM rows never show a key-on edge; without this their meters
                // would sit pinned at the sustain floor. A pitch change on a held row is the same
                // event, as it is for the note bar - but not for a streamed sample, whose pitch is
                // a playback rate that drifts every snapshot. Read before noteLength updates it.
                boolean restruck = channel.keyOn
                        || (!channel.sampled && channel.sounding && channel.note >= 0
                            && channel.note != lastNotes[row]);

                if (noteLength(row, status, channel.keyOn, channel.sounding, channel.note, channel.sampled)) {
                    // streaming: the pitch is a playback rate, so the note it lands nearest is an
                    // artefact of the arithmetic and not something the song ever played
                    status.key = 0xff;
                    status.actualKey = 0xff;
                }

                if (level >= 0) {
                    envelope(level, restruck, channel.sounding, channel.amplitude, channel.measured);
                    pans[level] = channel.pan;
                    levelTracks[level] = TrackId.values()[row];
                }
                rowReaders[row] = reader;
                rowGroups[row] = g;
                rowChannels[row] = ch;
            }
            if (slot >= groupRows.length) break;
        }
    }

    private void rhythm() {
        List<FmDspChipReader> claimed = claims.get(Group.RHYTHM);
        List<Integer> bases = meterBases.get(Group.RHYTHM);
        for (int r = 0; r < claimed.size(); r++) {
            int meter = bases.get(r);
            if (meter >= LevelDataSource.COUNT) break;

            channel.clear();
            claimed.get(r).read(Group.RHYTHM, 0, channel);
            levelLabels[meter] = channel.name != null ? channel.name : "RHY";
            if (channel.keyOn) {
                envelopes[meter] = channel.amplitude > 0 ? channel.amplitude : rhythmLevel;
            } else {
                envelopes[meter] *= levelRelease;
            }
            pans[meter] = Pan.CENTER;
        }
    }

    /**
     * The label over a meter column, the way the original strip has them: the section it belongs
     * to - FM1, FM4, SSG, RHY, ADP, PPZ8 - with a wide FM span numbered every third column so a
     * nine channel chip reads FM1, FM4, FM7.
     * <p>
     * The exception is a group two different chips share, where the section alone would not say
     * which is which: there each span carries its {@linkplain FmDspChipReader#chipName chip's
     * name} instead.
     *
     * @param meter the column within the reader's own span
     */
    private String labelOf(Group g, FmDspChipReader reader, String section, int meter) {
        String label;
        if (claims.get(g).size() > 1 && reader.chipName() != null) {
            if (meter != 0) return null; // one name per span, at its start
            label = reader.chipName();
        } else if (g == Group.FM && section != null) {
            label = section + (meter + 1);
        } else {
            label = section;
        }
        // a one column span has no room for the fourth character
        int room = reader.meters(g) > 1 ? 4 : 3;
        return label != null && label.length() > room ? label.substring(0, room) : label;
    }

    /**
     * Drives channel {@code c}'s level meter: it jumps to {@code amplitude} on key on, sags
     * towards the sustain level while the key is held, and releases otherwise. Note that this is
     * an envelope, not a measurement - see the class comment.
     */
    private void envelope(int c, boolean keyOn, boolean sounding, double amplitude, boolean measured) {
        if (measured) {
            // already an envelope, and the real one: hold the peak and let it fall, but do not
            // scale it down to a sustain level the chip is not actually at
            envelopes[c] = Math.max(amplitude, envelopes[c] * levelRelease);
            return;
        }
        if (keyOn) {
            envelopes[c] = amplitude;
        } else if (sounding) {
            envelopes[c] = Math.max(envelopes[c] * levelSustainDecay, amplitude * levelSustain);
        } else {
            envelopes[c] *= levelRelease;
        }
    }

    /**
     * Fills the note length bar of row {@code row}: {@link TrackStatus#ticks} is the length the
     * row's previous note ran for, which the renderer marks as the note's start, and
     * {@link TrackStatus#ticksLeft} is how much of it the note that is playing has left.
     * A note that outlives the measurement holds the bar at empty rather than wrapping around.
     * <p>
     * The lengths are kept at the full resolution of the note clock and only divided by
     * {@link #barScale} on the way out, so lengthening the notes never loses the short ones.
     */
    private boolean noteLength(int row, TrackStatus status, boolean keyOn, boolean sounding, int note,
                               boolean sampled) {
        // A key on is only seen when the key actually goes down between two snapshots. MXDRV keys
        // off and back on inside one, so its rows look permanently held and would never measure a
        // note at all; a pitch change on a held row is the same event as far as the bar cares.
        boolean started = keyOn || (sounding && note >= 0 && note != lastNotes[row]);
        lastNotes[row] = sounding ? note : -1;
        if (started) {
            if (keyOnTicks[row] >= 0) {
                long measured = noteTicks - keyOnTicks[row];
                noteLengths[row] = (int) Math.min(measured, maxNoteLength);
            }
            keyOnTicks[row] = noteTicks;
        }
        if (!sounding && !started) {
            // resting: nothing is counting down
            status.ticks = 0;
            status.ticksLeft = 0;
            return false;
        }
        long elapsed = keyOnTicks[row] < 0 ? 0 : noteTicks - keyOnTicks[row];
        if (sampled && elapsed > streamHold) {
            // A sampled voice that has been sounding this long without being re-struck is not
            // playing a note, it is streaming - a whole part, or a whole track, rendered as one
            // endless sample the way an arcade board does it. There is no note and no length in
            // that, only sound, so both meters stay off and the level meter carries the row.
            status.ticks = 0;
            status.ticksLeft = 0;
            return true;
        }
        if (noteLengths[row] == 0) {
            // Nothing measured yet, so this is the row's first note: it is being held and how long
            // it runs is not known yet. The bar says held, full, and empties when the note does. An
            // empty bar would say the row was silent, which is what every first note used to show.
            status.ticks = barFullScale;
            status.ticksLeft = barFullScale;
            return false;
        }
        status.ticks = noteLengths[row] / barScale;
        status.ticksLeft = (int) Math.max(0, noteLengths[row] - elapsed) / barScale;
        return false;
    }

    /**
     * How long a sampled voice may sound without being re-struck before it counts as streaming
     * rather than playing a note: longer than the bar itself can represent.
     * <p>
     * Deliberately not a number of seconds. The note clock is driven by the driver's own sample
     * counter, which not every driver advances at the same rate, so a threshold in seconds would
     * only be true for some of them. Measured against the bar it is exact whatever the rate - a
     * note too long for the bar to draw is one the bar has nothing to say about.
     * <p>
     * Four bars' worth rather than one, which lands somewhere between five and twenty seconds
     * depending on the driver. A single bar caught a PMD ADPCM part holding a long note and blanked
     * a key that was really being played; nothing sequenced holds one sample this long, while a
     * streamed track holds it for minutes, so the cost of waiting is a few seconds of a held note
     * being drawn before the row goes quiet.
     */
    private static final int streamHold = barFullScale * 4;

    /**
     * Picks {@link #barScale} from the notes that are sounding, before the rows are filled: the
     * smallest power of two that brings the longest of them inside the bar. A row that has stopped
     * does not count, so an opening drone does not coarsen the rest of the song.
     */
    private void scaleBar() {
        int longest = 0;
        for (int row = 0; row < lastNotes.length; row++) {
            if (lastNotes[row] >= 0) longest = Math.max(longest, noteLengths[row]);
        }
        int scale = 1;
        while (longest / scale > barFullScale) scale <<= 1;
        barScale = scale;
    }

    /**
     * The longest note the bar will measure, past which it stops counting down and simply stays
     * full - about a minute even at the coarsest scale, which no note outlives musically.
     */
    private static final int maxNoteLength = (int) (noteTickHz * 60);

    /** MML key: high nibble octave, low nibble note, 0xff while the part rests */
    static int keyOf(int note) {
        return note < 0 ? 0xff : (note / 12) << 4 | note % 12;
    }

    private static void clear(TrackStatus status) {
        status.playing = false;
        status.info = TrackInfo.NORMAL;
        status.ticks = 0;
        status.ticksLeft = 0;
        status.key = 0xff;
        status.actualKey = 0xff;
        status.toneNum = 0;
        status.volume = 0;
        status.gate = 0;
        status.detune = 0;
        status.ppz8Ch = 0;
        status.ssgTone = false;
        status.ssgNoise = false;
        Arrays.fill(status.fmSlotMask, false);
    }

    // ----- work state -----

    /**
     * The tick counter fmdsp wants for its clock, its loop bar and the circle animation, rebuilt
     * the way {@code OPNATimer} does it from the driver's sample counter: one TimerB step is
     * {@code opnaClock / 72 / 2} = 55467 Hz, and TimerB overflows every {@code (256 - timerB) * 16}
     * steps. The period comes from the FM chip that owns the rows; a song that never programs
     * TimerB gets {@link #defaultTimerB}.
     */
    private void state() {
        BaseDriver d = driver.get();
        work = d;
        if (d == null) return;

        // not every driver fills one in - the HES driver leaves it null until a song is loaded
        if (d.metaData != null) {
            if (comments[0] == null) comments[0] = d.metaData.getFirst(Tag.Title);
            if (comments[1] == null) comments[1] = d.metaData.getFirst(Tag.Composer);
            if (comments[2] == null) comments[2] = d.metaData.getFirst(Tag.Arranger);
        }

        int tb = 0;
        for (FmDspChipReader reader : claims.get(Group.FM)) {
            tb = reader.timerB();
            if (tb > 0) break;
        }
        if (tb <= 0) tb = defaultTimerB;
        timerB = tb;

        long counter = d.counter;
        // MXDRV never advances its sample counter - it is clocked by the PCM8 chip, which does not
        // report back - so a note clock hung off the counter alone would stand still and every MDX
        // row would show an empty bar. Fall back to counting snapshots, which arrive at a known
        // rate in the player.
        noteTickStep += counter > lastCounter
                ? (counter - lastCounter) * (noteTickHz / Common.VGMProcSampleRate)
                : noteTickHz / snapshotRate;
        while (noteTickStep >= 1) {
            noteTickStep -= 1;
            noteTicks++;
        }
        timerBStep += (counter - lastCounter) * (timerBStepHz / Common.VGMProcSampleRate);
        lastCounter = counter;
        int overflow = (256 - tb) << 4;
        while (timerBStep >= overflow) {
            timerBStep -= overflow;
            timerBCount++;
        }

        int loop = Math.max(d.curLoop, 0);
        if (loop != lastLoopCount) {
            if (lastLoopCount > 0) loopTimerBCount = timerBCount - loopStartTimerBCount;
            loopStartTimerBCount = timerBCount;
            lastLoopCount = loop;
        }
        timerBCountLoop = timerBCount - loopStartTimerBCount;
    }

    // ----- FmDspDataSource -----

    @Override public FftDataSource fft() { return fft; }

    @Override public LevelDataSource level() { return this; }

    @Override public TrackStatusSource trackStatus() { return this; }

    @Override public WorkStateSource work() { return this; }

    // ----- TrackStatusSource -----

    @Override
    public void readStatus(TrackId track, TrackStatus out) {
        TrackStatus status = tracks[track.ordinal()];
        out.playing = status.playing;
        out.info = status.info;
        out.ticks = status.ticks;
        out.ticksLeft = status.ticksLeft;
        out.key = status.key;
        out.actualKey = status.actualKey;
        out.toneNum = status.toneNum;
        out.volume = status.volume;
        out.gate = status.gate;
        out.detune = status.detune;
        out.status = status.status;
        out.ppz8Ch = status.ppz8Ch;
        out.ssgTone = status.ssgTone;
        out.ssgNoise = status.ssgNoise;
        System.arraycopy(status.fmSlotMask, 0, out.fmSlotMask, 0, out.fmSlotMask.length);
    }

    @Override
    public boolean masked(TrackId track) {
        FmDspChipReader reader = rowReaders[track.ordinal()];
        return reader != null && reader.masked(rowGroups[track.ordinal()], rowChannels[track.ordinal()]);
    }

    @Override
    public String trackTypeName(TrackId track) {
        return rowNames[track.ordinal()];
    }

    @Override
    public int trackNumber(TrackId track) {
        int num = rowNums[track.ordinal()];
        return num > 0 ? num : -1;
    }

    @Override
    public TrackId[] displayTracks() {
        return dispTracks;
    }

    // ----- LevelDataSource -----

    @Override
    public int level(int channel) {
        return (int) (envelopes[channel] * 32767);
    }

    @Override
    public Pan pan(int channel) {
        return pans[channel];
    }

    @Override
    public String label(int channel) {
        return levelLabels[channel];
    }

    /** the row that fed this column this song; null for the drum meter and unclaimed columns */
    @Override
    public TrackId track(int channel) {
        return levelTracks[channel];
    }

    // ----- WorkStateSource -----

    @Override public int sampleRate() { return Common.VGMProcSampleRate; }

    /** time constant [s] with which the displayed clock is pulled onto the driver's own counter */
    private static final double clockSlew = 0.2;

    /**
     * The render loop blocks on the audio line and then renders a whole device period at once, so
     * the driver's sample counter advances in ~10 ms bursts of several hundred samples - reading it
     * straight makes the clock stutter. Playback is real time, so the displayed clock runs off the
     * wall clock instead and is slewed towards the driver's counter, which keeps it smooth without
     * letting it drift. It is frozen while paused and never runs backwards.
     */
    @Override
    public long generatedFrames() {
        BaseDriver d = work;
        if (d == null) return 0;

        long counter = d.counter;
        long now = System.nanoTime();
        if (lastClockNanos == 0) {
            clock = counter;
            lastClockNanos = now;
        }
        double elapsed = (now - lastClockNanos) / 1e9;
        lastClockNanos = now;

        if (!paused && playing()) {
            clock += elapsed * sampleRate();
        }
        // time based, so the result does not depend on how often the renderer asks
        clock += (counter - clock) * (1 - Math.exp(-elapsed / clockSlew));
        // a stall (a long pause, a seek) is not worth slewing through
        if (Math.abs(counter - clock) > sampleRate()) {
            clock = counter;
        }

        shownFrames = Math.max(shownFrames, (long) clock);
        return shownFrames;
    }

    private double clock;
    private long lastClockNanos;
    private long shownFrames;

    @Override public long timerBCount() { return timerBCount; }

    @Override public int timerB() { return timerB; }

    @Override public int loopCount() { BaseDriver d = work; return d != null ? Math.max(d.curLoop, 0) : 0; }

    @Override public long loopTimerBCount() { return loopTimerBCount; }

    @Override public long timerBCountLoop() { return timerBCountLoop; }

    @Override public boolean playing() { BaseDriver d = work; return d != null && !d.stopped; }

    @Override public boolean paused() { return paused; }

    @Override
    public String driverName() {
        BaseDriver d = work;
        return d != null ? d.getClass().getSimpleName().replace("Driver", "").toUpperCase() : null;
    }

    @Override public String filename() { return filename; }

    @Override public String comment(int line) { return comments[line]; }
}
