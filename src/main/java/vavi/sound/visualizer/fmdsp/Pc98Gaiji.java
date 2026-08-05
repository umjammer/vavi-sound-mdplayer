/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * The JIS X 0208 rows the standard leaves to the maker, which no Shift_JIS decoder has a mapping
 * for. A decoder parks them in the Unicode private use area with {@link #puaOf}, and
 * {@link FmDspVisualizer} draws them from the PC-98 font ROM, which addresses its glyphs by JIS
 * code, with {@link #jisOf}.
 * <p>
 * Rows 9 to 12 hold half width glyphs - the ROM carries them in the left half of a full width
 * cell, and FMP's editor writes its ASCII, its katakana and its rules there. Rows 85 to 94 are the
 * gaiji a user defines, which live in the machine's RAM and so are in no ROM dump: nothing can be
 * drawn for them, and the caller is expected to fall back to the geta mark.
 * <p>
 * A JIS code does not fit in the private use area as it is - it runs to 0x7e7e, which is 32382
 * where the area has 6400 - so the two blocks are packed, 94 cells to a row.
 */
public final class Pc98Gaiji {

    private Pc98Gaiji() {
    }

    /** cells in a JIS X 0208 row */
    private static final int CELLS = 94;

    /** rows 9 to 12, the half width ones */
    private static final int HALF_FIRST = 9, HALF_LAST = 12, HALF_BASE = 0xe000;

    /** rows 85 to 94, the user defined ones */
    private static final int GAIJI_FIRST = 85, GAIJI_LAST = 94;
    private static final int GAIJI_BASE = HALF_BASE + (HALF_LAST - HALF_FIRST + 1) * CELLS;

    /**
     * The private use character carrying {@code jis}.
     *
     * @param jis a JIS X 0208 code, its row in the high byte
     * @return 0 when the code is in none of the maker's rows
     */
    public static char puaOf(int jis) {
        int row = (jis >> 8) - 0x20;
        int cell = (jis & 0xff) - 0x20;
        if (cell < 1 || cell > CELLS) {
            return 0;
        }
        if (row >= HALF_FIRST && row <= HALF_LAST) {
            return (char) (HALF_BASE + (row - HALF_FIRST) * CELLS + cell - 1);
        }
        if (row >= GAIJI_FIRST && row <= GAIJI_LAST) {
            return (char) (GAIJI_BASE + (row - GAIJI_FIRST) * CELLS + cell - 1);
        }
        return 0;
    }

    /**
     * The JIS X 0208 code {@code c} carries.
     *
     * @return 0 when {@code c} is not one of {@link #puaOf}'s characters
     */
    public static int jisOf(char c) {
        int row, cell;
        if (c >= HALF_BASE && c < GAIJI_BASE) {
            int i = c - HALF_BASE;
            row = HALF_FIRST + i / CELLS;
            cell = i % CELLS + 1;
        } else if (c >= GAIJI_BASE && c < GAIJI_BASE + (GAIJI_LAST - GAIJI_FIRST + 1) * CELLS) {
            int i = c - GAIJI_BASE;
            row = GAIJI_FIRST + i / CELLS;
            cell = i % CELLS + 1;
        } else {
            return 0;
        }
        return (row + 0x20) << 8 | (cell + 0x20);
    }

    /** whether {@code c} is one of the half width rows, whose glyph is the left half of its cell */
    public static boolean isHalfWidth(char c) {
        return c >= HALF_BASE && c < GAIJI_BASE;
    }
}
