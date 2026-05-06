/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * Identifier of a logical track row. The ordinal mirrors the C enum
 * {@code fmdriver_tracknum}.
 */
public enum TrackId {
    FM_1,
    FM_2,
    FM_3,
    FM_3_EX_1,
    FM_3_EX_2,
    FM_3_EX_3,
    FM_4,
    FM_5,
    FM_6,
    SSG_1,
    SSG_2,
    SSG_3,
    ADPCM,
    PPZ8_1,
    PPZ8_2,
    PPZ8_3,
    PPZ8_4,
    PPZ8_5,
    PPZ8_6,
    PPZ8_7,
    PPZ8_8;

    public static final int COUNT = values().length;
}
