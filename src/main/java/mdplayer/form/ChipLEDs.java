
package mdplayer.form;

import mdplayer.Chip;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.C140Chip;
import mdplayer.chips.HuC6280Chip;
import mdplayer.chips.OkiM6258Chip;
import mdplayer.chips.OkiM6295Chip;
import mdplayer.chips.PwmChip;
import mdplayer.chips.Rf5C164Chip;
import mdplayer.chips.SegaPcmChip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;


/**
 * The lamps along the top of the main screen, one for each chip the player can drive.
 * <p>
 * A chip lights its own lamp: it fires a {@code "led.on"} view event whenever it is written to, and
 * the view turns that into an {@link #on} here. Nothing ever turns a lamp off — it {@link #fade}s a
 * little every frame, so a chip that is being played glows and one that has fallen silent goes dark
 * by itself.
 *
 * @see mdplayer.driver.BaseDriver#fireEventHappened
 */
public class ChipLEDs {

    /** how bright a lamp is the moment its chip speaks */
    private static final int LIT = 255;

    /** how much of that a lamp loses each frame */
    private static final int FADE = 16;

    /** a song may have two chips of a kind: a primary and a secondary */
    private static final int CHIPS = 2;

    /**
     * A lamp: the chip that lights it and the column its name is drawn in. The order is the order
     * the name sprites are in.
     */
    public enum Led {
        OPN(Ym2203Chip.class, 14),
        OPN2(Ym2612Chip.class, 18),
        OPNA(Ym2608Chip.class, 23),
        OPNB(Ym2610Chip.class, 28),
        OPM(Ym2151Chip.class, 33),
        DCSG(Sn76489Chip.class, 37),
        RF5C(Rf5C164Chip.class, 42),
        PWM(PwmChip.class, 47),
        OKI5(OkiM6258Chip.class, 51),
        OKI9(OkiM6295Chip.class, 56),
        C140(C140Chip.class, 61),
        SPCM(SegaPcmChip.class, 66),
        AY10(Ay8910Chip.class, 4),
        OPLL(Ym2413Chip.class, 9),
        HuC8(HuC6280Chip.class, 71);

        /** the chip whose writes light this lamp */
        final Class<? extends Chip> chip;

        /** where the chip's name is drawn, in units of 4 pixels */
        final int x;

        Led(Class<? extends Chip> chip, int x) {
            this.chip = chip;
            this.x = x;
        }

        /** The lamp this chip lights, or none if it has none. */
        static Led of(Object chip) {
            for (Led led : values()) {
                if (led.chip.isInstance(chip)) return led;
            }
            return null;
        }
    }

    /** how bright each lamp is now */
    private final int[][] values = new int[CHIPS][Led.values().length];

    /** how bright each lamp was when it was last drawn */
    private final int[][] drawn = new int[CHIPS][Led.values().length];

    /** Lights the lamp of a chip that has just been written to. */
    public void on(Object chip, int chipId) {
        Led led = Led.of(chip);
        if (led == null || chipId < 0 || chipId >= CHIPS) return;

        values[chipId][led.ordinal()] = LIT;
    }

    /** Dims every lamp by one frame's worth. */
    public void fade() {
        for (int[] chip : values) {
            for (int i = 0; i < chip.length; i++) {
                chip[i] = Math.max(chip[i] - FADE, 0);
            }
        }
    }

    public void clear() {
        for (int chipId = 0; chipId < CHIPS; chipId++) {
            for (int i = 0; i < Led.values().length; i++) {
                values[chipId][i] = 0;
                drawn[chipId][i] = 0;
            }
        }
    }

    public byte value(int chipId, Led led) {
        return (byte) values[chipId][led.ordinal()];
    }

    /** What is on screen, so that only a lamp that has changed is redrawn. */
    public byte drawn(int chipId, Led led) {
        return (byte) drawn[chipId][led.ordinal()];
    }

    /** Remembers what has just been drawn. */
    public void drew(int chipId, Led led) {
        drawn[chipId][led.ordinal()] = values[chipId][led.ordinal()];
    }
}
