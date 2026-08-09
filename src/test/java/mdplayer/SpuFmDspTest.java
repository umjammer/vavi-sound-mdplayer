/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import org.junit.jupiter.api.Test;
import vavi.sound.visualizer.fmdsp.LevelDataSource;
import vavi.sound.visualizer.fmdsp.TrackDetail;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * What the fmdsp strip shows for a psf and a psf2: the SPU voices on the PCM rows.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class SpuFmDspTest {

    @Test
    void psf1VoicesReachTheStrip() throws Exception {
        check("tmp/psf/pe.psf", "SPU");
    }

    @Test
    void psf2VoicesReachTheStrip() throws Exception {
        check("tmp/psf/01.psf2", "SPU2");
    }

    private void check(String file, String chipName) throws Exception {
        // the samples live under tmp/, which is not in the repository - see driver/readme.md
        assumeTrue(Files.exists(Path.of(file)), file + " is missing, see mdplayer/driver/readme.md");

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(new BufferedInputStream(Files.newInputStream(Path.of(file))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);

        BaseDriver driver = plugin.getDriver();
        short[] buffer = new short[1024];

        TrackStatus status = new TrackStatus();
        int playingRows = 0;
        int noted = 0;
        int levelled = 0;

        // ten seconds, which is past the driver's latency and past the psf2's bank upload
        for (int i = 0; i < 44100 * 10 / buffer.length; i++) {
            driver.render(buffer, 0, buffer.length);
            source.snapshot();

            for (TrackId row : TrackId.values()) {
                source.readStatus(row, status);
                if (!status.playing) continue;
                if (status.key > 0) noted++;
            }
            for (int c = 0; c < LevelDataSource.COUNT; c++) {
                if (source.level(c) > 0) levelled++;
            }
        }

        StringBuilder shown = new StringBuilder();
        for (TrackId row : TrackId.values()) {
            source.readStatus(row, status);
            if (status.playing) {
                playingRows++;
                assertEquals("PCM", source.trackTypeName(row), row + " should be a PCM row");
                assertTrue(source.trackNumber(row) > 0, row + " should be numbered by its voice");
                shown.append(" %s=%s%d".formatted(row, source.trackTypeName(row), source.trackNumber(row)));
            }
        }

System.err.println("%s: rows %d, keyed snapshots %d, levelled meters %d, chips [%s],%s"
        .formatted(file, playingRows, noted, levelled, source.chips(), shown));

        assertTrue(playingRows > 0, "no SPU voice reached a row");
        assertTrue(noted > 0, "no voice ever showed a key");
        assertTrue(levelled > 0, "no level meter ever moved");
        assertTrue(source.chips().contains(chipName),
                "the strip should name the chip " + chipName + ", got " + source.chips());

        // the track info half draws this, and every claimed row should have something to say
        TrackDetail detail = new TrackDetail();
        boolean detailed = false;
        for (TrackId row : TrackId.values()) {
            source.readStatus(row, status);
            if (!status.playing) continue;
            detail.clear();
            if (source.readDetail(row, detail)) {
                assertNotNull(detail.header);
                assertNotNull(detail.text);
                detailed = true;
            }
        }
        assertTrue(detailed, "no row filled in its track detail");

        plugin.stop();
        plugin.close();
    }
}
