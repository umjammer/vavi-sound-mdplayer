/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdplayer.driver.fmp7.Fmp7Driver;
import mdplayer.lib.fmp7.Fmp7Work;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.RightMode;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;


/**
 * scratch: plays an .owi and watches the fmdsp rows fill from what FMP7 says it is playing, then
 * renders one frame of it off screen.
 * <p>
 * The thing worth looking at is not only the picture but the line before it: where FMP7 had the
 * song when the mixer first made a sound. Unlike a driver mdplayer runs itself, FMP7's player
 * runs ahead of its own sound card, and how far ahead is the one thing between the display and
 * the audio that has to be measured rather than counted.
 * <p>
 * Run with {@code -Dvavi.test=ai}, optionally {@code -Dprobe.file=...owi},
 * {@code -Dprobe.seconds=20} and {@code -Dprobe.out=...png}.
 */
class Fmp7FmDspProbe {

    /** over this a sample is music rather than the level FMP7 sits at */
    private static final int LOUD = 1000;

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void render() throws Exception {
        String file = System.getProperty("probe.file", "tmp/fmp7/829.owi");
        int seconds = Integer.getInteger("probe.seconds", 20);
        String out = System.getProperty("probe.out", "tmp/fmp7-frame.png");

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);
        int sampleRate = setting.getOutputDevice().getSampleRate();

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
        source.setFilename(Path.of(file).getFileName().toString());

        Fmp7Driver driver = (Fmp7Driver) plugin.getDriver();

        short[] buffer = new short[1024];
        double firstSampleMillis = Double.NaN;
        double firstLoudMillis = Double.NaN;
        int rendered = 0;
        int rows = 0;
        int notes = 0;
        int checks = 0;
        int songStart = 0;
        // rendering flat out is the file rendering case, where the emulator is what holds the
        // loop up; paced is what a sound card does, and the emulator then runs ahead of it
        boolean paced = Boolean.getBoolean("probe.paced");
        long started = System.currentTimeMillis();
        while (rendered < sampleRate * seconds && !driver.stopped) {
            if (paced) {
                long wait = started + rendered * 1000L / sampleRate - System.currentTimeMillis();
                if (wait > 0) Thread.sleep(wait);
            }
            driver.render(buffer, 0, buffer.length);
            rendered += buffer.length / 2;
            source.snapshot();

            Fmp7Work work = driver.getWork();
            double millis = driver.getSongMillis();
            if (work != null && !Double.isNaN(millis)) {
                for (short v : buffer) {
                    if (v != 0 && Double.isNaN(firstSampleMillis)) {
                        firstSampleMillis = millis;
                        // the loop starts before the emulator does, and what it renders until
                        // then is silence that is no part of the song
                        songStart = rendered;
                    }
                    if (Math.abs(v) > LOUD && Double.isNaN(firstLoudMillis)) firstLoudMillis = millis;
                }
                for (int p = 0; p < Fmp7Work.MAX_PART; p++) {
                    if (work.mode(p) != Fmp7Work.MODE_NONE && work.note(p) != Fmp7Work.REST) notes++;
                }
            }
            rows = Math.max(rows, sounding(source));

            // how far the display is from the sound: the song position it reports against the
            // song position of the sample just rendered, once the song is under way
            if (work != null && !Double.isNaN(firstLoudMillis) && rendered / (sampleRate * 2) > checks) {
                checks++;
System.err.printf("sync: %6.2fs of song rendered, work says %6.2fs%n",
        (rendered - songStart) / (double) sampleRate, millis / 1000);
            }
        }

System.err.printf("audio: %.2fs rendered, driver stopped=%b, loop %d%n",
        rendered / (double) sampleRate, driver.stopped, driver.curLoop);
System.err.printf("sync: mixer's first sample at song %.0fms, first one over %d at song %.0fms%n",
        firstSampleMillis, LOUD, firstLoudMillis);
System.err.printf("work: %d part-frames holding a note; rows that sounded: %d%n", notes, rows);

        TrackStatus status = new TrackStatus();
        for (TrackId row : TrackId.values()) {
            source.readStatus(row, status);
            if (!status.playing) continue;
System.err.println(row + " type=" + source.trackTypeName(row) + " num=" + source.trackNumber(row));
        }

        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setSize(640, 400);
        vis.setDataSource(source);
        vis.setRightMode(RightMode.valueOf(System.getProperty("probe.right", "TRACK_INFO")));

        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        // the palette fades in over its first frames, so one paint would come out black
        for (int i = 0; i < 130; i++) vis.paint(g);
        g.dispose();

        Path png = Path.of(out);
        if (png.getParent() != null) Files.createDirectories(png.getParent());
        ImageIO.write(image, "png", png.toFile());
System.err.println("wrote " + png);

        plugin.stop();
        plugin.close();
    }

    private static int sounding(ChipFmDspSource source) {
        TrackStatus status = new TrackStatus();
        int n = 0;
        for (TrackId row : TrackId.values()) {
            source.readStatus(row, status);
            if (status.playing) n++;
        }
        return n;
    }
}
