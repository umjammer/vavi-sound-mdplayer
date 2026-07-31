/*
 * scratch: render one visualizer frame from FMP, offscreen
 */

package mdplayer.driver.fmp;

import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import mdplayer.Setting;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;
import mdplayer.lib.fmp.FmpWork;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.util.event.GenericEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


public class RenderCheck {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void render() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/FMP/FMPDDISK/FF5_GILG.OVI");
        String dir = "/Users/nsano/Public/np2/FMP/FMPDDISK";
        String out = System.getProperty("probe.out", "/tmp/fmp.png");
        String fontRom = System.getProperty("probe.font", "");

        FMP fmp = new FMP();
        fmp.ft = new FileTemp();
        fmp.ft.saveCompiledFile = Setting.getInstance().getOther().getSaveCompiledFile();
        fmp.setSearchPath(dir);
        fmp.sampleRate = 44100;
        fmp.charset = Charset.forName("Shift_JIS");
        fmp.dir = dir;
        fmp.blockWrite = b -> {};
        fmp.setPPZ8PCMData = (a, b, c) -> {};
        fmp.setPPZ8Data = (a, b, c) -> {};

        FmpFmDspSource source = new FmpFmDspSource();
        source.setFilename(Path.of(file).getFileName().toString());

        FmpWork work = fmp.getWork();
        fmp.opnaWrite = (p, a, d) -> {
            int port = (p & 0xff) == 0x8a ? 0 : 1;
            if (port == 0 && a == 0x07) work.ssgMixer = d & 0xff;
            if (port == 0 && a == 0x26) work.timerB = d & 0xff;
            if (port == 1 && a == 0x10 && (d & 0x80) == 0) work.rhythmKeyOn |= d & 0x3f;
            if (port == 1 && a == 0x01) work.adpcmPan = d & 0xff;
        };
        fmp.playingFileName = file;
        fmp.run(Files.readAllBytes(Path.of(file)));

        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);
        if (!fontRom.isEmpty() && Files.exists(Path.of(fontRom))) {
            visualizer.setFontRom(Files.readAllBytes(Path.of(fontRom)));
        }

        // 20 s of song, feeding the source the same events the driver would
        int tick = 0;
        for (int i = 0; i < 44100 * 20; i++) {
            fmp.nise98.runTimer();
            if (!fmp.nise98.intTimer()) continue;
            fmp.processOneFrame(() -> {});
            work.ticks++;
            if (++tick % 4 == 0) {
                source.update(new GenericEvent(this, "fmp", work));
            }
        }

        visualizer.setSize(visualizer.getPreferredSize());
        visualizer.doLayout();
        BufferedImage image = new BufferedImage(
                visualizer.getWidth(), visualizer.getHeight(), BufferedImage.TYPE_INT_RGB);
        // the palette fades in one step per paint
        for (int i = 0; i < 120; i++) {
            visualizer.paint(image.getGraphics());
        }
        ImageIO.write(image, "png", Path.of(out).toFile());
        System.err.printf("wrote %s (%dx%d)%n", out, image.getWidth(), image.getHeight());
    }
}
