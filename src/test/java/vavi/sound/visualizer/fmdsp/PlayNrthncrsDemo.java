package vavi.sound.visualizer.fmdsp;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.fmp.FmpFmDspSource;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;

public class PlayNrthncrsDemo {

    public static void main(String[] args) throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");

        Path file = Path.of("/Users/nsano/Public/np2/FMPData/MusicData/NRTHNCRS.OZI");
        if (!Files.exists(file)) {
            System.err.println("File not found: " + file);
            return;
        }

        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Files.newInputStream(file), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();

        FmpFmDspSource source = new FmpFmDspSource();
        source.setFilename(file.getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        driver.addViewListener(source::update);

        SwingUtilities.invokeLater(() -> {
            FmDspVisualizer vis = new FmDspVisualizer(60);
            vis.setDataSource(source);

            JFrame frame = new JFrame("FMDSP - NRTHNCRS.OZI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(vis, BorderLayout.CENTER);
            frame.pack();
            frame.setLocation(0, 0);
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);

            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int code = e.getKeyCode();
                    if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F10) {
                        vis.setPaletteIndex(code - KeyEvent.VK_F1);
                    } else if (code == KeyEvent.VK_ESCAPE) {
                        frame.dispose();
                        System.exit(0);
                    }
                }
            });
            vis.start();

            new Thread(() -> {
                try {
                    short[] buffer = new short[2 * 1024];
                    while (true) {
                        int ret = driver.render(buffer, 0, 1024);
                        if (ret < 0) break;
                        Thread.sleep(1024 * 1000 / Common.VGMProcSampleRate);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
    }
}
