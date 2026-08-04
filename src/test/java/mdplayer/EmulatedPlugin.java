/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import mdplayer.ChipRegister;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdsound.Instrument;
import mdsound.MDSound;


/**
 * A plugin holding real emulator instruments, for the chips that keep no registers of their own.
 * <p>
 * Those chips decode what is written to them and throw the registers away, so a test cannot set
 * their state by poking a cache - it has to write to the emulator the same way a driver does and
 * read the state back out. Binding a {@link mdplayer.Chip} to one of these gives it somewhere for
 * its writes to land.
 * <p>
 * The chips are JVM-wide singletons sharing one context, so a test that builds one of these
 * re-points every chip at it. Build a fresh one per test and take {@link #chipRegister} from it
 * rather than making your own.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
public class EmulatedPlugin extends BasePlugin<BaseDriver> {

    private static final int samplingRate = 44100;

    /**
     * @param instruments the instruments to register, each started at its own default clock. The
     *                    same instrument passed twice is started twice, as the second chip of its
     *                    kind - the way a VGM declaring two of one chip has it
     */
    public static EmulatedPlugin of(Instrument... instruments) {
        EmulatedPlugin plugin = new EmulatedPlugin();
        List<MDSound.Chip> chips = new ArrayList<>();
        Map<Instrument, Integer> ids = new IdentityHashMap<>();
        for (Instrument instrument : instruments) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.instrument = instrument;
            chip.id = ids.merge(instrument, 1, Integer::sum) - 1;
            chip.samplingRate = samplingRate;
            chip.clock = clockOf(instrument);
            chip.option = optionOf(instrument);
            chips.add(chip);
            // the played chips, as a song's plugin records them: how many of a chip there are is
            // read back from here (BaseChip#instances), and a second reader of a chip waits on it.
            // Straight into the map rather than through put(), which also tells a driver's view -
            // and there is no driver here
            Class<? extends mdplayer.Chip> owner = ownerOf(plugin.chipRegister, instrument);
            if (owner != null) {
                plugin.getChipInstances().computeIfAbsent(owner, c -> new ArrayList<>()).add(chip);
            }
        }
        plugin.mds.init(samplingRate, 1024, chips);
        return plugin;
    }

    /** the chip wrapper an emulator instrument belongs to, null when no wrapper claims it */
    private static Class<? extends mdplayer.Chip> ownerOf(ChipRegister chipRegister, Instrument instrument) {
        for (Class<? extends mdplayer.Chip> clazz : chipRegister.chips()) {
            for (Class<? extends Instrument> implementation : chipRegister.chip(clazz).implementations()) {
                if (implementation == instrument.getClass()) return clazz;
            }
        }
        return null;
    }

    /** what each of these wants of {@code Instrument#start} beyond a clock */
    private static Object[] optionOf(Instrument instrument) {
        return switch (instrument.getShortName()) {
            // the SegaPCM's bank interface, which decides how its addresses are split
            case "SPCM" -> new Object[] {0x70};
            // the C140's variant: 0 is the System 2, which is what the reader assumes
            case "C140" -> new Object[] {0x00};
            default -> null;
        };
    }

    /** the clock the reader for each of these assumes, so the emulator and the view agree */
    private static int clockOf(Instrument instrument) {
        return switch (instrument.getShortName()) {
            case "SAA" -> mdsound.instrument.Saa1099Inst.DefaultClockValue; // 8 MHz
            case "C352" -> 24576000;
            case "SPCM" -> 4000000;
            case "C140" -> 44100 * 576; // the chip divides this by 576
            default -> 8000000;
        };
    }

    private EmulatedPlugin() {
    }

    /**
     * Renders a little audio so the emulators take in what has been written to them.
     * <p>
     * Not every core applies a register write when it arrives - the Nuked OPL3 queues them and
     * only takes them up as it generates, which is invisible during playback but leaves a test
     * that writes and reads straight back looking at an empty chip.
     */
    public void settle() {
        // enough to drain a queue: the Nuked core stamps each write with a time and takes it up
        // only when generation reaches it
        short[] buffer = new short[2048];
        mds.update(buffer, 0, buffer.length, () -> {});
    }

    @Override
    protected void initChips() {
    }
}
