/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.pmd;

import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.ChipFmDspSource;
import mdplayer.driver.pmd.PmdDriver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Verifies that the PMD driver and ChipFmDspSource report all 4 PCM slots (PPC, PPZ1, PPZ2, PPS).
 */
class PmdPcmLoadTest {

    static final String testFile = "/Users/nsano/Public/np2/PMD/SC3/POP_TM.MZ";

    static boolean testFileExists() {
        return Files.exists(Path.of(testFile));
    }

    @Test
    void testPmdDriverPcmTypes() {
        PmdDriver driver = new PmdDriver();
        assertEquals("PPC", driver.pcmType(0));
        assertEquals("PPZ1", driver.pcmType(1));
        assertEquals("PPZ2", driver.pcmType(2));
        assertEquals("PPS", driver.pcmType(3));
    }

    @Test
    void testGenericSourcePmdPcmTypes() {
        PmdDriver driver = new PmdDriver();
        ChipFmDspSource genericSource = new ChipFmDspSource();
        genericSource.bind(null, () -> driver);

        assertEquals("PPC", genericSource.pcmType(0));
        assertEquals("PPZ1", genericSource.pcmType(1));
        assertEquals("PPZ2", genericSource.pcmType(2));
        assertEquals("PPS", genericSource.pcmType(3));
    }
}
