/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdplayer.driver.psf2.Psf2Driver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Plays a psf and a psf2 the way the player does, through the format, the plugin and the driver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class PsfDriverTest {

    @BeforeAll
    static void setupAll() {
        System.setProperty("mdplayer.variant.ymf262", "0");
    }

    @Test
    void psf1PlaysThroughTheDriver() throws Exception {
        BaseDriver driver = play("tmp/psf/pe.psf", PsfDriver.class);

        // 1:57 of song and 10 seconds of fade, at the output rate
        long expected = (117 + 10) * (long) Setting.getInstance().getOutputDevice().getSampleRate();
        assertEquals(expected, driver.totalCounter);
    }

    @Test
    void psf2PlaysThroughTheDriver() throws Exception {
        play("tmp/psf/01.psf2", Psf2Driver.class);
    }

    private BaseDriver play(String filename, Class<? extends BaseDriver> expected) throws Exception {
        // the samples live under tmp/, which is not in the repository - see driver/readme.md
        assumeTrue(Files.exists(Path.of(filename)), filename + " is missing, see mdplayer/driver/readme.md");

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(filename);
        format.load(new java.io.BufferedInputStream(Files.newInputStream(Path.of(filename))), null);

        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", filename));
        plugin.prepare();

        BaseDriver driver = plugin.getDriver();
        assertInstanceOf(expected, driver);
        assertNotNull(driver.metaData);

        // six seconds of audio, past the emulation latency the driver skips at the start
        int sampleRate = setting.getOutputDevice().getSampleRate();
        short[] buffer = new short[2048];
        int peak = 0;
        int rendered = 0;
        while (rendered < sampleRate * 6) {
            driver.render(buffer, 0, buffer.length);
            for (short s : buffer) {
                peak = Math.max(peak, Math.abs(s));
            }
            rendered += buffer.length / 2;
        }

        assertTrue(peak > 1000, filename + " rendered near silence, peak " + peak);
        assertTrue(driver.counter > 0, "the driver clock should have advanced");

        plugin.stop();
        plugin.close();

        return driver;
    }
}
