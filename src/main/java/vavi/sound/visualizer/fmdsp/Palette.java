/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;

import java.awt.Color;


/**
 * 10-color palette table lifted verbatim from {@code fmdsp_sprites.h}. Index 0
 * is background; the C drawing code references colors by these slot indices.
 */
public final class Palette {

    /** Number of palette slots. Mirrors {@code FMDSP_PALETTE_COLORS}. */
    public static final int COLORS = 10;

    /** Number of selectable palettes (F1..F10). Mirrors {@code PALETTE_NUM}. */
    public static final int COUNT = 10;

    /** Per-frame fade speed in 8-bit channel steps. Mirrors {@code FADEDELTA}. */
    public static final int FADE_DELTA = 16;

    private static final int LCDWR = 172, LCDWG = 239, LCDWB = 7;
    private static final int LCDBR = 12, LCDBG = 91, LCDBB = 0;

    private static int[] lcd(int c) {
        return new int[] {
            (LCDWR * c + LCDBR * (255 - c)) / 256,
            (LCDWG * c + LCDBG * (255 - c)) / 256,
            (LCDWB * c + LCDBB * (255 - c)) / 256,
        };
    }

    private static int[] lcdb(int c) {
        return new int[] {
            (LCDBR * c + LCDWR * (255 - c)) / 256,
            (LCDBG * c + LCDWG * (255 - c)) / 256,
            (LCDBB * c + LCDWB * (255 - c)) / 256,
        };
    }

    private static final int[][][] TABLE = new int[COUNT][][];

    static {
        TABLE[0] = palette(0, 0, 0, 170, 170, 153, 102, 136, 255, 68, 68, 119, 204, 204, 187, 102, 102, 85, 136, 255, 68, 51, 51, 238, 0, 187, 255, 68, 102, 170);
        TABLE[1] = palette(0, 0, 0, 187, 187, 170, 136, 170, 255, 85, 85, 153, 204, 204, 187, 136, 136, 119, 153, 255, 119, 102, 85, 255, 0, 204, 255, 85, 119, 170);
        TABLE[2] = palette(0, 0, 0, 255, 102, 0, 255, 170, 0, 102, 68, 51, 153, 136, 102, 119, 85, 68, 255, 221, 85, 255, 102, 0, 255, 85, 0, 170, 119, 85);
        TABLE[3] = palette(0, 0, 0, 170, 170, 153, 170, 153, 255, 85, 51, 102, 204, 204, 187, 102, 102, 85, 119, 255, 34, 136, 68, 221, 0, 187, 255, 136, 102, 187);
        TABLE[4] = palette(0, 0, 0, 187, 187, 170, 102, 153, 255, 85, 68, 136, 255, 255, 238, 119, 119, 102, 255, 68, 0, 85, 85, 255, 255, 119, 255, 119, 119, 187);
        TABLE[5] = palette(0, 0, 0, 255, 51, 119, 255, 187, 0, 85, 85, 68, 119, 119, 102, 102, 102, 102, 255, 221, 0, 255, 0, 51, 255, 0, 51, 170, 136, 0);
        TABLE[6] = palette(102, 170, 238, 0, 17, 136, 0, 51, 136, 153, 221, 255, 153, 221, 255, 119, 187, 255, 0, 51, 136, 34, 102, 187, 0, 85, 204, 68, 136, 255);
        TABLE[7] = palette(0, 0, 0, 170, 170, 136, 102, 153, 255, 17, 0, 51, 170, 170, 153, 51, 51, 51, 85, 255, 68, 34, 17, 255, 0, 170, 255, 68, 85, 170);
        TABLE[8] = lcdPalette(true);
        TABLE[9] = lcdPalette(false);
    }

    private static int[][] palette(int... rgb) {
        int[][] result = new int[COLORS][3];
        for (int i = 0; i < COLORS; i++) {
            result[i][0] = rgb[i * 3];
            result[i][1] = rgb[i * 3 + 1];
            result[i][2] = rgb[i * 3 + 2];
        }
        return result;
    }

    private static int[][] lcdPalette(boolean inverted) {
        int[] shades = {255, 72, 0, 182, inverted ? 0 : 145, 145, inverted ? 218 : 0, 109, inverted ? 218 : 0, 0};
        int[][] result = new int[COLORS][];
        for (int i = 0; i < COLORS; i++) {
            result[i] = inverted ? lcdb(shades[i]) : lcd(shades[i]);
        }
        return result;
    }

    private final int[] curr = new int[COLORS * 3];
    private final int[] target = new int[COLORS * 3];

    public Palette() {
        select(0);
        // C code seeds curr to all-zero on init then fades up.
        for (int i = 0; i < curr.length; i++) curr[i] = 0;
    }

    /** Select a palette slot; the current colors fade towards it. */
    public void select(int index) {
        if (index < 0 || index >= COUNT) index = 0;
        int[][] src = TABLE[index];
        for (int i = 0; i < COLORS; i++) {
            target[i * 3] = src[i][0];
            target[i * 3 + 1] = src[i][1];
            target[i * 3 + 2] = src[i][2];
        }
    }

    /**
     * Step the per-channel fade by {@link #FADE_DELTA} towards the target.
     * Returns true if anything changed.
     */
    public boolean tick() {
        boolean changed = false;
        for (int i = 0; i < curr.length; i++) {
            int p = curr[i];
            int t = target[i];
            if (p < t) {
                p = Math.min(p + FADE_DELTA, t);
                changed = true;
            } else if (p > t) {
                p = Math.max(p - FADE_DELTA, t);
                changed = true;
            }
            curr[i] = p;
        }
        return changed;
    }

    /** Color for slot {@code 0..COLORS-1}. */
    public Color color(int slot) {
        int i = slot * 3;
        return new Color(curr[i], curr[i + 1], curr[i + 2]);
    }
}
