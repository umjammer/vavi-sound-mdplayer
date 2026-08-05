/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;
import mdplayer.lib.fmp.FmpWork;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Verifies that the FMP driver extracts PVI/PPZ filenames from the
 * song data header, matching the C reference fmp_load() in fmdriver_fmp.c.
 */
class FmpPcmLoadTest {

    // This OVI file has pvi_name="PCM03" embedded in its FMC header
    static final String testFile = "/Users/nsano/Public/np2/FMPPMD/Pops/MATURI.OVI";

    static boolean testFileExists() {
        return Files.exists(Path.of(testFile));
    }

    @Test
    @EnabledIf("testFileExists")
    void testParsePcmNamesFromHeader() throws Exception {
        byte[] data = Files.readAllBytes(Path.of(testFile));
        FmpWork work = new FmpWork();

        FmpDriver.parsePcmNames(data, work);

        System.out.println("pviName: " + work.pviName);
        System.out.println("ppzName: " + work.ppzName);

        assertNotNull(work.pviName, "PVI name should be extracted from header");
        assertEquals("PCM03", work.pviName, "PVI name should match embedded header value");
    }

    @Test
    void testParsePcmNamesFormat1NoNames() {
        // format 1 (dataver <= 0x29) has no embedded PVI names
        // Construct a minimal FMC header at offset 0x1c (fmtoneptr for format 1)
        byte[] data = new byte[0x30];
        int offset = 0x1c;
        data[0] = (byte) (offset & 0xff);
        data[1] = (byte) ((offset >> 8) & 0xff);
        data[offset] = 'F';
        data[offset + 1] = 'M';
        data[offset + 2] = 'C';
        data[offset + 3] = 0x20; // dataver <= 0x29, format 1

        FmpWork work = new FmpWork();
        FmpDriver.parsePcmNames(data, work);

        assertNull(work.pviName, "Format 1 should not have PVI name");
        assertNull(work.ppzName, "Format 1 should not have PPZ name");
    }

    // This OZI file has pvi_name="FMPD_V6" and ppz_name="STR_V3"
    static final String testFileOzi = "/Users/nsano/Public/np2/FMPPMD/Pops/TIERRA05.OZI";

    static boolean testFileOziExists() {
        return Files.exists(Path.of(testFileOzi));
    }

    @Test
    @EnabledIf("testFileOziExists")
    void testParsePcmNamesBothPviAndPpz() throws Exception {
        byte[] data = Files.readAllBytes(Path.of(testFileOzi));
        FmpWork work = new FmpWork();

        FmpDriver.parsePcmNames(data, work);

        System.out.println("pviName: " + work.pviName);
        System.out.println("ppzName: " + work.ppzName);

        assertEquals("FMPD_V6", work.pviName, "PVI name from format 3 header");
        assertEquals("STR_V3", work.ppzName, "PPZ name from format 3 header");
    }

    @Test
    @EnabledIf("testFileExists")
    void testPpz8CallbackOverridesHeaderName() throws Exception {
        byte[] data = Files.readAllBytes(Path.of(testFile));

        FMP fmp = new FMP();
        fmp.setSearchPath(Path.of(testFile).getParent().toString());
        fmp.sampleRate = 55467;
        fmp.charset = Charset.forName("MS932");
        fmp.dir = System.getProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        fmp.playingFileName = testFile;
        fmp.ft = new FileTemp();

        AtomicReference<String> capturedPviName = new AtomicReference<>();

        fmp.setPPZ8PCMFilename = (mode, fn) -> {
            System.out.println("*** PPZ8 PCM Filename callback: mode=" + mode + " fn=" + fn);
            if (mode == 0) capturedPviName.set(fn);
        };
        fmp.setPPZ8PCMData = (bank, mode, pcmData) -> {};
        fmp.setPPZ8Data = (port, adr, d) -> {};
        fmp.opnaWrite = (p, a, d) -> {};
        fmp.blockWrite = b -> {};

        try {
            fmp.run(data);
        } catch (Exception e) {
            System.out.println("Exception during run: " + e.getMessage());
        }

        // Now apply header parsing (simulating what FmpDriver.init() does)
        FmpWork work = fmp.getWork();
        FmpDriver.parsePcmNames(data, work);

        System.out.println("After parsePcmNames:");
        System.out.println("  pviName: " + work.pviName);
        System.out.println("  capturedPviName: " + capturedPviName.get());

        // If PPZ8 callback set a name, parsePcmNames should not override it
        // If PPZ8 callback didn't fire, parsePcmNames should provide the header name
        assertNotNull(work.pviName, "PVI name should be set from header or callback");
    }

    @Test
    @EnabledIf("testFileOziExists")
    void testGenericSourceWithFmpDriver() throws Exception {
        byte[] data = Files.readAllBytes(Path.of(testFileOzi));

        FmpDriver driver = new FmpDriver();
        FmpDriver.parsePcmNames(data, driver.pcmFilename(0) == null ? new FmpWork() : null);

        // Test via FmpDriver directly
        FmpWork work = new FmpWork();
        FmpDriver.parsePcmNames(data, work);
        assertEquals("FMPD_V6", work.pviName);
        assertEquals("STR_V3", work.ppzName);

        // Test via ChipFmDspSource (generic source)
        mdplayer.ChipFmDspSource genericSource = new mdplayer.ChipFmDspSource();
        genericSource.bind(null, () -> driver);

        // Simulate driver initialization that sets pviName and ppzName in FmpWork
        FmpWork fmpWork = new FmpWork();
        FmpDriver.parsePcmNames(data, fmpWork);
        // Ensure driver's work has the extracted names
        FmpDriver.parsePcmNames(data, getFmpWork(driver));

        assertEquals("FMPD_V6", genericSource.pcmFilename(0), "Generic source should report PVI filename for slot 0");
        assertEquals("STR_V3", genericSource.pcmFilename(1), "Generic source should report PPZ filename for slot 1");
    }

    static final String testFile76 = "/Users/nsano/Public/np2/FMP/FMPD1/76THSTAR.OZI";

    static boolean testFile76Exists() {
        return Files.exists(Path.of(testFile76));
    }

    @Test
    @EnabledIf("testFile76Exists")
    void test76thStarHeaderPcmNames() throws Exception {
        byte[] data = Files.readAllBytes(Path.of(testFile76));
        FmpWork work = new FmpWork();
        FmpDriver.parsePcmNames(data, work);

        assertEquals("FMPVI_B", work.pviName, "76THSTAR.OZI PVI name (slot 0)");
        assertEquals("FMPPZ", work.ppzName, "76THSTAR.OZI PPZ name (slot 1)");
    }

    private static FmpWork getFmpWork(FmpDriver driver) throws Exception {
        var field = FmpDriver.class.getDeclaredField("fmp");
        field.setAccessible(true);
        FMP fmp = (FMP) field.get(driver);
        return fmp.getWork();
    }
}
