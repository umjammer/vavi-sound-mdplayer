package vavi.sound.visualizer.fmdsp;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.fmp.FmpFmDspSource;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class CaptureCyanKeyTest {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void captureNrthncrs() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");

        Path file = Path.of("/Users/nsano/Public/np2/FMPData/MusicData/NRTHNCRS.OZI");
        if (!Files.exists(file)) return;

        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Files.newInputStream(file), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();

        FmpFmDspSource source = new FmpFmDspSource();
        source.setFilename(file.getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        driver.addViewListener(source::update);

        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setDataSource(source);
        vis.setSize(640, 400);

        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        short[] buffer = new short[2 * 1024];

        // Render up to 12.8 seconds (where FM_4 pitch LFO/portamento is active)
        int targetFrame = (int) (Common.VGMProcSampleRate * 12.8 / 1024);
        for (int i = 0; i < targetFrame; i++) {
            driver.render(buffer, 0, 1024);
            vis.paint(g);
        }

        // Fade palette fully to 100%
        for (int i = 0; i < 25; i++) {
            vis.paint(g);
        }
        g.dispose();

        File outDir = new File("/Users/nsano/.gemini/antigravity-cli/brain/2b650081-d14e-4daf-bb0b-2745016b0cd4");
        if (!outDir.exists()) outDir.mkdirs();

        File outFile = new File(outDir, "cyan_key_nrthncrs.png");
        ImageIO.write(image, "png", outFile);
        System.out.println("Saved NRTHNCRS.OZI frame capture to: " + outFile.getAbsolutePath());
    }
}
