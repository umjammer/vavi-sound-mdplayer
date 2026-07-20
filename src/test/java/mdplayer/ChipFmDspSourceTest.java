/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import mdplayer.Common.EnmModel;
import mdplayer.chips.SegaPcmChip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import musicDriverInterface.MetaData;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.LevelDataSource.Pan;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackInfo;
import vavi.sound.visualizer.fmdsp.TrackStatus;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * {@link ChipFmDspSource}, on synthetic chip register states - no audio, no driver.
 * <p>
 * The chips are shared singletons served by {@link ChipRegister}, so each test rebuilds the
 * caches it reads from scratch.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-19 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class ChipFmDspSourceTest {

    EmulatedPlugin plugin;
    ChipRegister chipRegister;
    Ym2608Chip opna;
    Ym2151Chip opm;
    Ym2203Chip opn;
    SegaPcmChip segaPcm;
    ChipFmDspSource source;
    final TrackStatus status = new TrackStatus();

    @BeforeEach
    void setup() {
        // the C352 and the SAA1099 keep no registers of their own: their state has to be written
        // to a real emulator and read back, so the chips need a context whose writes land somewhere
        plugin = EmulatedPlugin.of(new mdsound.instrument.C352Inst(), new mdsound.instrument.Saa1099Inst(),
                new mdsound.instrument.SegaPcmInst(), new mdsound.instrument.C140Inst(), new mdsound.instrument.Sn76489Inst(), new mdsound.instrument.NukedYmF262Inst(),
                new mdsound.instrument.Ym2608Inst(), new mdsound.instrument.Ym2203Inst(), new mdsound.instrument.Ym2151Inst());
        chipRegister = plugin.chipRegister;
        // a chip only forwards writes to its emulator when the settings say to use one, which a
        // played song arranges and a test has to say for itself
        useEmulator(mdplayer.Setting.getInstance().getYM2608Type());
        useEmulator(mdplayer.Setting.getInstance().getYM2203Type());
        useEmulator(mdplayer.Setting.getInstance().getYM2151Type());
        // the OPL3 has five implementations and the plugin below registers only the nuked one
        useEmulator(mdplayer.Setting.getInstance().getYMF262Type(), 2);
        opna = chipRegister.chip(Ym2608Chip.class);
        opm = chipRegister.chip(Ym2151Chip.class);
        opn = chipRegister.chip(Ym2203Chip.class);
        segaPcm = chipRegister.chip(SegaPcmChip.class);

        // rebuild the caches the source polls, without going through Chip#init (it needs a
        // plugin). The chips are JVM-wide singletons, so every cache a reader looks at has to be
        // scrubbed or another test's leftovers would claim rows.
        // key-off everything a played song may have left in the other chips' caches
        chipRegister.chip(mdplayer.chips.Ym2612Chip.class).register[0] = new int[][] {new int[0x100], new int[0x100]};
        chipRegister.chip(mdplayer.chips.Ym2612Chip.class).keyOn[0] = new int[6];
        chipRegister.chip(mdplayer.chips.Ym2610Chip.class).register[0] = new int[][] {new int[0x100], new int[0x100]};
        chipRegister.chip(mdplayer.chips.Ym2610Chip.class).keyOn[0] = new int[6];
        chipRegister.chip(mdplayer.chips.YmF278BChip.class).register[0] =
                new int[][] {new int[0x100], new int[0x100], new int[0x100]};

        source = new ChipFmDspSource();
        source.bind(chipRegister, null);
    }

    /** OPNA o4 c: F-number 0x26a at block 4 */
    void opnaKeyOnFm1() {
        // the chip powers up with every level at 127, which is silence - a driver writes them,
        // and the old register shadow only looked open because it was zero filled
        for (int op = 0; op < 4; op++) {
            opna.write(0, 0, 0x40 + op * 4, 0, EnmModel.VirtualModel);
        }
        opna.write(0, 0, 0xa4, (4 << 3) | (0x26a >> 8), EnmModel.VirtualModel);
        opna.write(0, 0, 0xa0, 0x26a & 0xff, EnmModel.VirtualModel);
        opna.write(0, 0, 0xb4, 0xc0, EnmModel.VirtualModel); // pan L+R
        opna.write(0, 0, 0x28, 0xf0, EnmModel.VirtualModel); // key on all four slots of ch1
    }

    @Test
    @DisplayName("fm key from the f-number registers")
    void testFmKey() {
        opnaKeyOnFm1();
        source.snapshot();

        source.readStatus(TrackId.FM_1, status);
        assertTrue(status.playing);
        assertEquals(0x40, status.key); // o4 c
        assertEquals(0x40, status.actualKey);
        assertEquals(127, status.volume); // all TL registers 0
        assertTrue(source.level(0) > 0);
        assertEquals(Pan.CENTER, source.pan(0));

        // untouched rows stay inert
        source.readStatus(TrackId.FM_2, status);
        assertFalse(status.playing);
        assertEquals(0xff, status.key);
    }

    @Test
    @DisplayName("fm key off releases the key and the meter")
    void testFmKeyOff() {
        opnaKeyOnFm1();
        source.snapshot();
        int attack = source.level(0);

        opna.write(0, 0, 0x28, 0x00, EnmModel.VirtualModel); // key off ch1
        source.snapshot();

        source.readStatus(TrackId.FM_1, status);
        assertTrue(status.playing); // the row stays lit once used
        assertEquals(0xff, status.key); // but rests
        assertTrue(source.level(0) < attack);
    }

    @Test
    @DisplayName("fm carrier total level scales the meter")
    void testFmLevel() {
        opnaKeyOnFm1();
        source.snapshot();
        int loud = source.level(0);

        setup(); // fresh caches and source
        opnaKeyOnFm1();
        opna.write(0, 0, 0xb0, 7, EnmModel.VirtualModel); // algorithm 7: every slot is a carrier
        opna.write(0, 0, 0x40, 24, EnmModel.VirtualModel); // -18 dB on the softest one
        opna.write(0, 0, 0x44, 40, EnmModel.VirtualModel);
        opna.write(0, 0, 0x48, 40, EnmModel.VirtualModel);
        opna.write(0, 0, 0x4c, 40, EnmModel.VirtualModel);
        source.snapshot();

        assertEquals(127 - 24, sourceStatus(TrackId.FM_1).volume);
        assertTrue(source.level(0) < loud);
    }

    @Test
    @DisplayName("ssg key from the tone period, mixer bits shown")
    void testSsg() {
        int period = 284; // ~A4 at the OPNA's 1.9968 MHz SSG clock
        opna.write(0, 0, 0x00, period & 0xff, EnmModel.VirtualModel);
        opna.write(0, 0, 0x01, period >> 8, EnmModel.VirtualModel);
        opna.write(0, 0, 0x08, 15, EnmModel.VirtualModel);
        opna.write(0, 0, 0x07, 0x3e, EnmModel.VirtualModel); // tone on channel 1 only, noise off
        source.snapshot();

        source.readStatus(TrackId.SSG_1, status);
        assertTrue(status.playing);
        assertEquals(TrackInfo.SSG, status.info);
        assertEquals(0x49, status.key); // o4 a
        assertTrue(status.ssgTone);
        assertFalse(status.ssgNoise);
        assertEquals(15, status.volume);
        assertTrue(source.level(6) > 0);

        // silence it: level 0 releases the meter
        opna.write(0, 0, 0x08, 0, EnmModel.VirtualModel);
        source.snapshot();
        source.readStatus(TrackId.SSG_1, status);
        assertEquals(0xff, status.key);
    }

    @Test
    @DisplayName("adpcm from the control, delta-n and level registers")
    void testAdpcm() {
        opna.write(0, 1, 0x00, 0xa0, EnmModel.VirtualModel); // start | repeat
        opna.write(0, 1, 0x01, 0xc0, EnmModel.VirtualModel); // pan L+R
        opna.write(0, 1, 0x09, 0xba, EnmModel.VirtualModel);
        opna.write(0, 1, 0x0a, 0x49, EnmModel.VirtualModel); // delta-N 0x49ba: rate ratio 1.0
        opna.write(0, 1, 0x0b, 200, EnmModel.VirtualModel);
        source.snapshot();

        source.readStatus(TrackId.ADPCM, status);
        assertTrue(status.playing);
        assertEquals(0x40, status.key); // ratio 1.0 shows as o4 c
        assertEquals(200, status.volume);
        assertTrue(source.level(10) > 0);
        assertEquals(Pan.CENTER, source.pan(10));
    }

    @Test
    @DisplayName("rhythm key-on hits the drum meter")
    void testRhythm() {
        assertEquals(0, source.level(9));
        opna.write(0, 0, 0x10, 0x01, EnmModel.VirtualModel); // bass drum
        source.snapshot();
        int hit = source.level(9);
        assertTrue(hit > 0);

        source.snapshot(); // no new hit: the meter decays
        assertTrue(source.level(9) < hit);
    }

    @Test
    @DisplayName("opm takes the fm rows over when it keys on first")
    void testOpm() {
        for (int op = 0; op < 4; op++) {
            opm.write(0, 0, 0x60 + op * 8, 0, EnmModel.VirtualModel, 0, 0); // open the levels
        }
        opm.write(0, 0, 0x20, 0xc0 | 7, EnmModel.VirtualModel, 0, 0); // pan L+R, algorithm 7
        opm.write(0, 0, 0x28, 0x4a, EnmModel.VirtualModel, 0, 0); // o4 a
        opm.write(0, 0, 0x08, 0x78, EnmModel.VirtualModel, 0, 0); // key on all four slots of ch1
        source.snapshot();

        source.readStatus(TrackId.FM_1, status);
        assertTrue(status.playing);
        assertEquals(0x49, status.key); // o4 a
        assertTrue(source.level(0) > 0);
        assertEquals(Pan.CENTER, source.pan(0));
    }

    @Test
    @DisplayName("opna wins the fm rows over a silent opm")
    void testArbitration() {
        opnaKeyOnFm1();
        opm.register[0][0x28] = 0x4a; // a key code alone is not activity
        source.snapshot();

        source.readStatus(TrackId.FM_1, status);
        assertEquals(0x40, status.key); // the OPNA note, not the OPM one
    }

    @Test
    @DisplayName("reset clears the rows for the next song")
    void testReset() {
        opnaKeyOnFm1();
        source.snapshot();
        assertTrue(sourceStatus(TrackId.FM_1).playing);

        source.reset();
        assertFalse(sourceStatus(TrackId.FM_1).playing);
        assertEquals(0, source.level(0));
    }

    @Test
    @DisplayName("opn (ym2203) takes the fm and ssg rows, mono pans")
    void testOpn2203() {
        for (int op = 0; op < 4; op++) {
            opn.write(0, 0x40 + op * 4, 0, EnmModel.VirtualModel); // open the levels
        }
        opn.write(0, 0xa4, (4 << 3) | (0x26a >> 8), EnmModel.VirtualModel);
        opn.write(0, 0xa0, 0x26a & 0xff, EnmModel.VirtualModel);
        opn.write(0, 0x28, 0xf0, EnmModel.VirtualModel); // key on all four slots of ch1
        source.snapshot();

        source.readStatus(TrackId.FM_1, status);
        assertTrue(status.playing);
        assertEquals(0x40, status.key); // o4 c, the same F-number table as the OPNA
        assertEquals(Pan.CENTER, source.pan(0)); // the chip is mono
        assertTrue(source.level(0) > 0);

        // its SSG section claims the SSG rows
        opn.write(0, 0x00, 284 & 0xff, EnmModel.VirtualModel);
        opn.write(0, 0x01, 284 >> 8, EnmModel.VirtualModel);
        opn.write(0, 0x08, 15, EnmModel.VirtualModel);
        opn.write(0, 0x07, 0x3e, EnmModel.VirtualModel);
        source.snapshot();
        source.readStatus(TrackId.SSG_1, status);
        assertEquals(TrackInfo.SSG, status.info);
        assertEquals(0x49, status.key); // o4 a
    }

    @Test
    @DisplayName("segapcm rows go to the channels that sound, wherever they sit")
    void testSegaPcm() {
        int ch = 9; // beyond the nine visible rows, the slot map has to bring it forward
        segaPcm.write(0, ch * 8 + 2, 100, EnmModel.VirtualModel); // left volume
        segaPcm.write(0, ch * 8 + 3, 100, EnmModel.VirtualModel); // right volume
        segaPcm.write(0, ch * 8 + 7, 0x40, EnmModel.VirtualModel); // ratio 0.25, two octaves down
        segaPcm.write(0, ch * 8 + 0x86, 0x00, EnmModel.VirtualModel); // bit 0 clear: keyed on
        source.snapshot();

        source.readStatus(TrackId.ADPCM, status); // the first PCM row
        assertTrue(status.playing);
        assertEquals(0x20, status.key); // o2 c
        assertEquals(ch + 1, status.ppz8Ch);
        assertTrue(source.level(10) > 0);
        assertEquals(Pan.CENTER, source.pan(10));
    }

    @Test
    @DisplayName("timer falls back to a synthesized tempo when no chip programs timerb")
    void testTimerFallback() {
        BaseDriver driver = new BaseDriver(null) {
            @Override public void init(Common.EnmModel model, int latency, int waitTime, Object... args) {}
            @Override public void processOneFrame() {}
            @Override public MetaData getMetaData(byte[] buf, Object... args) { return null; }
        };
        source.bind(chipRegister, () -> driver);

        opnaKeyOnFm1(); // the OPNA claims the fm rows but its timerb register is 0
        driver.counter = Common.VGMProcSampleRate; // one second in
        source.snapshot();

        assertEquals(200, source.timerB());
        assertTrue(source.timerBCount() > 0); // the clock and the circle animation move
    }

    @Test
    @DisplayName("row titles, numbers and meter labels follow the claimed chips")
    void testDynamicLabels() {
        opm.write(0, 0, 0x28, 0x4a, EnmModel.VirtualModel, 0, 0);
        opm.write(0, 0, 0x08, 0x78, EnmModel.VirtualModel, 0, 0); // key on ch1
        source.snapshot();

        // rows and meters are both labelled by section, as the original strip is
        assertEquals("FM", source.trackTypeName(TrackId.FM_1));
        assertEquals(1, source.trackNumber(TrackId.FM_1));
        assertEquals("FM1", source.label(0));
        // an untouched row keeps the built-in label
        assertEquals(-1, source.trackNumber(TrackId.SSG_1));

        // the auto view shows the rows in use, nothing else yet
        TrackId[] disp = source.displayTracks();
        assertEquals(1, disp.length);
        assertEquals(TrackId.FM_1, disp[0]);
    }

    @Test
    @DisplayName("auto view collects used rows across groups, in group order")
    void testDisplayTracks() {
        assertEquals(null, source.displayTracks()); // nothing sounded yet: default layout

        opnaKeyOnFm1();
        opna.write(0, 1, 0x00, 0xa0, EnmModel.VirtualModel); // and the ADPCM part
        opna.write(0, 1, 0x09, 0xba, EnmModel.VirtualModel);
        opna.write(0, 1, 0x0a, 0x49, EnmModel.VirtualModel);
        opna.write(0, 1, 0x0b, 200, EnmModel.VirtualModel);
        source.snapshot();
        source.snapshot(); // adpcm keyOn edge needs the ctrl change seen once

        TrackId[] disp = source.displayTracks();
        assertEquals(TrackId.FM_1, disp[0]);
        assertEquals(TrackId.ADPCM, disp[disp.length - 1]);
    }

    @Test
    @DisplayName("a 9 channel opl3 gets 9 rows and 9 meters, not the opna's 6")
    void testOpl3Meters() {
        var opl3 = chipRegister.chip(mdplayer.chips.YmF262Chip.class);
        for (int ch = 0; ch < 9; ch++) {
            opl3.write(0, 0, 0xa0 + ch, 0x40 + ch, EnmModel.VirtualModel); // an f-number each
            opl3.write(0, 0, 0xb0 + ch, 0x20 | (4 << 2), EnmModel.VirtualModel); // key on, block 4
            opl3.write(0, 0, 0xc0 + ch, 0x30, EnmModel.VirtualModel); // both outputs
        }
        plugin.settle();
        source.snapshot();

        // every one of the nine channels has its own row ...
        TrackId[] rows = {TrackId.FM_1, TrackId.FM_2, TrackId.FM_3, TrackId.FM_4, TrackId.FM_5,
                TrackId.FM_6, TrackId.FM_3_EX_1, TrackId.FM_3_EX_2, TrackId.FM_3_EX_3};
        for (int ch = 0; ch < rows.length; ch++) {
            // every row is an FM channel, numbered 1..9 - not nine repeats of the chip name
            assertEquals("FM", source.trackTypeName(rows[ch]), "row " + ch);
            assertEquals(ch + 1, source.trackNumber(rows[ch]), "row " + ch);
            assertTrue(sourceStatus(rows[ch]).playing, "row " + ch);
        }
        // ... and its own meter, where the OPNA's extension rows share FM3's
        for (int c = 0; c < 9; c++) {
            assertTrue(source.level(c) > 0, "meter " + c);
        }
        // a nine wide FM span is numbered every third column, the way the original marks FM1/FM4
        assertEquals("FM1", source.label(0));
        assertEquals("FM4", source.label(3));
        assertEquals("FM7", source.label(6));
        assertEquals(9, source.displayTracks().length);

        // the key/pan readout under each meter must follow the same channel, not the PC-98
        // assignment that puts SSG under columns 6..8
        for (int c = 0; c < 9; c++) {
            assertEquals(rows[c], source.track(c), "meter " + c + " readout");
        }
    }

    @Test
    @DisplayName("the opna keeps the classic meter columns")
    void testOpnaMeterLayout() {
        opnaKeyOnFm1();
        opna.write(0, 0, 0x00, 284 & 0xff, EnmModel.VirtualModel); // and an SSG note
        opna.write(0, 0, 0x01, 284 >> 8, EnmModel.VirtualModel);
        opna.write(0, 0, 0x08, 15, EnmModel.VirtualModel);
        opna.write(0, 0, 0x07, 0x3e, EnmModel.VirtualModel);
        opna.write(0, 1, 0x00, 0xa0, EnmModel.VirtualModel); // and the ADPCM part
        opna.write(0, 1, 0x09, 0xba, EnmModel.VirtualModel);
        opna.write(0, 1, 0x0a, 0x49, EnmModel.VirtualModel);
        opna.write(0, 1, 0x0b, 200, EnmModel.VirtualModel);
        source.snapshot();

        // the original strip's own labels: FM1, FM4, SSG, ADP
        assertEquals("FM1", source.label(0));
        assertEquals("FM4", source.label(3));
        assertEquals("SSG", source.label(6));
        assertEquals("ADP", source.label(10)); // a one column span keeps to 3 characters
        assertEquals("FM", source.trackTypeName(TrackId.FM_1));
        assertEquals("SSG", source.trackTypeName(TrackId.SSG_1));
        assertTrue(source.level(0) > 0);
        assertTrue(source.level(6) > 0);

        // and the readouts sit where the original puts them
        assertEquals(TrackId.FM_1, source.track(0));
        assertEquals(TrackId.SSG_1, source.track(6));
        assertEquals(TrackId.ADPCM, source.track(10));
    }

    @Test
    @DisplayName("c352 channels take the pcm rows, wherever they sit")
    void testC352() {
        var c352 = chipRegister.chip(mdplayer.chips.C352Chip.class);
        int ch = 5;
        c352.write(0, ch * 8, (200 << 8) | 200, EnmModel.VirtualModel); // front volume, l and r
        c352.write(0, ch * 8 + 2, 0x8000, EnmModel.VirtualModel); // half the rate, an octave down
        c352.write(0, ch * 8 + 3, 0x4000, EnmModel.VirtualModel); // ask for a key on
        c352.write(0, ch * 8 + 4, 4, EnmModel.VirtualModel); // wave bank
        c352.write(0, 0x202, 0, EnmModel.VirtualModel); // execute it, raising the chip's busy bit
        source.snapshot();

        source.readStatus(TrackId.ADPCM, status); // the first PCM row
        assertTrue(status.playing);
        assertEquals(0x30, status.key); // ratio 0.5 is an octave under o4 c
        assertEquals(ch + 1, status.ppz8Ch);
        assertEquals("PCM", source.trackTypeName(TrackId.ADPCM));
        assertTrue(source.level(10) > 0);
        assertEquals(Pan.CENTER, source.pan(10));
        assertEquals(4, status.toneNum); // the wave bank, in the PROG column
        // the renderer blanks the key and pan of a PPZ8 or PDZF row: the C352 is neither
        assertNotEquals(TrackInfo.PPZ8, status.info);
        assertNotEquals(TrackInfo.PDZF, status.info);
    }

    @Test
    @DisplayName("opl4 wave channels are read from the slot registers, which start at 8")
    void testYmF278BWave() {
        int[] wave = chipRegister.chip(mdplayer.chips.YmF278BChip.class).register[0][2];
        int ch = 3;
        // group n of 24 starts at 8 + n * 24: F-number 0 and octave 0 is the unshifted rate
        wave[0x08 + ch] = 0x05; // wave number, low eight bits
        wave[0x20 + ch] = 0x01; // its ninth bit in bit 0; F-number low is bits 1-7, so 0
        wave[0x38 + ch] = 0; // F-number high and octave
        wave[0x50 + ch] = 0; // total level 0, full volume
        wave[0x68 + ch] = 0x80; // key on, pan centre
        source.snapshot();

        source.readStatus(TrackId.ADPCM, status);
        assertTrue(status.playing);
        assertEquals(0x40, status.key); // the unshifted rate shows as o4 c
        assertEquals(127, status.volume);
        assertEquals(0x105, status.toneNum); // the nine bit wave number, in the PROG column
        assertTrue(source.level(10) > 0);
        assertEquals(Pan.CENTER, source.pan(10));
        // the renderer blanks the key and pan of a PPZ8 or PDZF row: the OPL4 wave part is neither
        assertNotEquals(TrackInfo.PPZ8, status.info);
        assertNotEquals(TrackInfo.PDZF, status.info);

        // bit 4 is the DO1 pin, which a MoonSound leaves unwired
        wave[0x68 + ch] = 0x90;
        source.snapshot();
        assertEquals(Pan.NONE, source.pan(10));
    }

    @Test
    @DisplayName("saa1099's six channels take the wider fm rows, labelled for what they are")
    void testSaa1099() {
        var saa = chipRegister.chip(mdplayer.chips.Saa1099Chip.class);
        saa.write(0, 0x1c, 0x01, EnmModel.VirtualModel); // the chip enabled at all
        saa.write(0, 0x14, 0x3f, EnmModel.VirtualModel); // tone on every channel
        saa.write(0, 0x00, 0xa0, EnmModel.VirtualModel); // channel 0: right only
        saa.write(0, 0x08, 5, EnmModel.VirtualModel); // its frequency
        saa.write(0, 0x10, 0x54, EnmModel.VirtualModel); // octave 4 for ch 0, 5 for ch 1 - shared
        saa.write(0, 0x01, 0x0b, EnmModel.VirtualModel); // channel 1: left only
        saa.write(0, 0x09, 5, EnmModel.VirtualModel);
        source.snapshot();

        // 8 MHz / 256 * 2^4 / (511 - 5) = 988.1 Hz, which is B5
        source.readStatus(TrackId.FM_1, status);
        assertTrue(status.playing);
        assertEquals(0x5b, status.key); // o5 b
        assertEquals(TrackInfo.SSG, status.info);
        assertTrue(status.ssgTone);
        assertEquals("SSG", source.trackTypeName(TrackId.FM_1));
        assertEquals(Pan.RIGHT, source.pan(0));

        // the octave nibble of the odd channel is the high one: an octave up
        source.readStatus(TrackId.FM_2, status);
        assertEquals(0x6b, status.key); // o6 b
        assertEquals(Pan.LEFT, source.pan(1));
    }

    @Test
    @DisplayName("c140 channels take the pcm rows, an octave apart for double the pitch")
    void testC140() {
        var c140 = chipRegister.chip(mdplayer.chips.C140Chip.class);
        int ch = 2;
        c140.write(0, ch * 16, 0x40, EnmModel.VirtualModel); // right volume
        c140.write(0, ch * 16 + 1, 0x40, EnmModel.VirtualModel); // left volume
        c140.write(0, ch * 16 + 2, 0x05, EnmModel.VirtualModel); // pitch, high byte first
        c140.write(0, ch * 16 + 3, 0x74, EnmModel.VirtualModel);
        c140.write(0, ch * 16 + 4, 7, EnmModel.VirtualModel); // wave bank
        c140.write(0, ch * 16 + 5, 0x93, EnmModel.VirtualModel); // key on in bit 7
        source.snapshot();

        source.readStatus(TrackId.ADPCM, status); // the first PCM row
        assertTrue(status.playing);
        assertEquals(ch + 1, status.ppz8Ch);
        assertEquals(7, status.toneNum); // the bank, in the PROG column
        assertEquals("PCM", source.trackTypeName(TrackId.ADPCM));
        assertEquals(Pan.CENTER, source.pan(10));
        int key = status.key;
        assertTrue(key != 0xff, "the channel should sound");

        // twice the pitch register is exactly an octave up
        setup();
        c140 = chipRegister.chip(mdplayer.chips.C140Chip.class);
        c140.write(0, ch * 16, 0x40, EnmModel.VirtualModel);
        c140.write(0, ch * 16 + 1, 0x40, EnmModel.VirtualModel);
        c140.write(0, ch * 16 + 2, 0x0a, EnmModel.VirtualModel); // 0x0ae8, double 0x0574
        c140.write(0, ch * 16 + 3, 0xe8, EnmModel.VirtualModel);
        c140.write(0, ch * 16 + 5, 0x93, EnmModel.VirtualModel);
        source.snapshot();

        source.readStatus(TrackId.ADPCM, status);
        int octaveUp = status.key;
        assertEquals(12, (((octaveUp >> 4) & 0xf) * 12 + (octaveUp & 0xf))
                - (((key >> 4) & 0xf) * 12 + (key & 0xf)), "double the pitch is one octave");
    }

    @Test
    @DisplayName("readers that poll the emulator claim nothing when no song loaded one")
    void testEmulatorBackedReadersAreNullSafe() {
        // the NES, the NSF machine's NES and the HuC6280 keep no registers of their own: they ask
        // the emulator, which has nothing to give until a song loads the chip. Polling must stay
        // quiet rather than throw.
        assertDoesNotThrow(source::snapshot);
        assertDoesNotThrow(source::snapshot);

        // and nothing of theirs is on screen
        assertEquals(null, source.displayTracks());
        for (int c = 0; c < LevelDataSource.COUNT; c++) {
            assertEquals(0, source.level(c), "meter " + c);
        }
    }

    TrackStatus sourceStatus(TrackId t) {
        source.readStatus(t, status);
        return status;
    }

    static void useEmulator(mdplayer.Setting.ChipType2[] types) {
        useEmulator(types, 0);
    }

    /**
     * Pins the emulator variant as well, for a chip with more than one implementation: the
     * variant a user's {@code local.properties} happens to select is not necessarily one this
     * test registered, and an unregistered one swallows every write.
     *
     * @param variant index into the chip's {@code implementations()}
     */
    static void useEmulator(mdplayer.Setting.ChipType2[] types, int variant) {
        for (mdplayer.Setting.ChipType2 type : types) {
            boolean[] useEmu = new boolean[variant + 2];
            useEmu[variant] = true;
            type.setUseEmu(useEmu);
            type.setUseReal(new boolean[] {false, false});
        }
    }
}
