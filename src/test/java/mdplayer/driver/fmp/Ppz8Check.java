/*
 * scratch: check the PPZ8 rows, which only exist once the plugin's chips are up
 */

package mdplayer.driver.fmp;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


public class Ppz8Check {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void check() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/fmp_ume3/NBZTITLE.OZI");
        String out = System.getProperty("probe.out", "/tmp/ppz8.png");

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Files.newInputStream(Path.of(file)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();

        FmpFmDspSource source = new FmpFmDspSource();
        source.setFilename(Path.of(file).getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        driver.addViewListener(source::update);

        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);

        // render 25 s of song through the driver, exactly as the audio thread would
        short[] buffer = new short[2 * 1024];
        for (int i = 0; i < Common.VGMProcSampleRate * 25 / 1024; i++) {
            driver.render(buffer, 0, 1024);
            for (int j = 0; j + 1 < buffer.length; j += 2) {
                // the "master" event the audio path would fire
            }
        }

        TrackStatus status = new TrackStatus();
        System.err.println("  row     playing key   tone vol  ppz8ch");
        for (TrackId t : TrackId.values()) {
            source.readStatus(t, status);
            System.err.printf("  %-9s %-6s %02x   %3d  %3d  %d%n",
                    t, status.playing, status.key, status.toneNum, status.volume, status.ppz8Ch);
        }
        System.err.print("  levels:");
        for (int c = 0; c < 19; c++) System.err.printf(" %d:%d", c, source.level(c));
        System.err.println();

        visualizer.setSize(visualizer.getPreferredSize());
        visualizer.doLayout();
        for (vavi.sound.visualizer.fmdsp.LeftMode mode : vavi.sound.visualizer.fmdsp.LeftMode.values()) {
            visualizer.setLeftMode(mode);
            visualizer.setRightMode(vavi.sound.visualizer.fmdsp.RightMode.TRACK_INFO);
            BufferedImage image = new BufferedImage(
                    visualizer.getWidth(), visualizer.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < 120; i++) {
                visualizer.paint(image.getGraphics());
            }
            String name = out.replace(".png", "-" + mode + ".png");
            ImageIO.write(image, "png", Path.of(name).toFile());
            System.err.printf("wrote %s%n", name);
        }
    }
}
