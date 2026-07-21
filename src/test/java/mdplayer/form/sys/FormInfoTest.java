package mdplayer.form.sys;

import javax.swing.JFrame;

import mdplayer.Audio;
import mdplayer.driver.VgmDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
public class FormInfoTest {

    @Test
    public void testFormInfo() throws Exception {
        Audio audio = Audio.getInstance();
        BasePlugin<VgmDriver> plugin = new mdplayer.plugin.VGMPlugin();
        plugin.driverVirtual = new VgmDriver();
        plugin.driverVirtual.metaData = new musicDriverInterface.MetaData();
        audio.plugin = plugin;
        
        MetaData metaData = audio.plugin.driverVirtual.metaData;
        metaData.set(Tag.Title, "Mock Title");
        metaData.set(Tag.TitleJ, "Mock Title (J)");
        metaData.set(Tag.GameTitle, "Mock Game Title");
        metaData.set(Tag.GameTitleJ, "Mock Game Title (J)");
        metaData.set(Tag.GameSystem, "Mock System");
        metaData.set(Tag.GameSystemJ, "Mock System (J)");
        metaData.set(Tag.Composer, "Mock Composer");
        metaData.set(Tag.ComposerJ, "Mock Composer (J)");
        metaData.set(Tag.Converter, "Mock Release");
        metaData.set(Tag.Maker, "Mock VGM By");
        metaData.set(Tag.Note, "Mock Notes");
        metaData.set(Tag.SongObjVersion, "Mock Version");
        metaData.set(Tag.Chip, "Mock Used Chips");

        FormInfo frmInfo = new FormInfo(null);
        assertNotNull(frmInfo);
        frmInfo.rtbLyrics.setText("Mock Lyrics Line 1 (Karaoke Test)");

        // Display the form briefly for visual validation and paint it to an image
        frmInfo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmInfo.setLocationRelativeTo(null);
        frmInfo.setVisible(true);

        Thread.sleep(1000);

        // Capture screen/component to verify layout and save to the artifacts directory
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(frmInfo.getWidth(), frmInfo.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics g = img.getGraphics();
        frmInfo.paint(g);
        g.dispose();
        
        java.io.File outputDir = new java.io.File("/Users/nsano/.gemini/antigravity-cli/brain/28703abf-3d83-43d0-87f2-9e7a31cc595d");
        if (!outputDir.exists()) outputDir.mkdirs();
        javax.imageio.ImageIO.write(img, "PNG", new java.io.File(outputDir, "info_screenshot.png"));
        System.out.println("Screenshot saved to: " + new java.io.File(outputDir, "info_screenshot.png").getAbsolutePath());

        Thread.sleep(2000);

        frmInfo.setVisible(false);
        frmInfo.dispose();
    }
}
