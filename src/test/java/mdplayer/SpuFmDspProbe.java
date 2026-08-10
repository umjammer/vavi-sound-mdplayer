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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.RightMode;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;


/**
 * scratch: renders one fmdsp frame of a psf or psf2 off screen, so the SPU strip can be looked at.
 * <p>
 * Run with {@code -Dvavi.test=ai -Dprobe.file=tmp/psf/pe.psf}, optionally
 * {@code -Dprobe.seconds=8}, {@code -Dprobe.right=TRACK_INFO} and {@code -Dprobe.out=...png}.
 */
class SpuFmDspProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void render() throws Exception {
        String file = System.getProperty("probe.file", "tmp/psf/pe.psf");
        int seconds = Integer.getInteger("probe.seconds", 8);
        String out = System.getProperty("probe.out", "tmp/psf/frame.png");

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
        source.setFilename(Path.of(file).getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        short[] buffer = new short[1024];
        for (int i = 0; i < 44100 * seconds / buffer.length; i++) {
            driver.render(buffer, 0, buffer.length);
            source.snapshot();
        }

        TrackStatus status = new TrackStatus();
        for (TrackId row : TrackId.values()) {
            source.readStatus(row, status);
            if (!status.playing) continue;
System.err.println(row + " type=" + source.trackTypeName(row) + " num=" + source.trackNumber(row)
        + " masked=" + source.masked(row));
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

        ImageIO.write(image, "png", Path.of(out).toFile());
System.err.println("wrote " + out);

        plugin.stop();
        plugin.close();
    }
}
