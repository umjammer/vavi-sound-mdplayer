/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import javax.swing.JFrame;

import mdplayer.Audio;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import mdplayer.plugin.BasePlugin.Compilable;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.LeftMode;
import vavi.sound.visualizer.fmdsp.RightMode;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * FMP.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-25 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
public class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String fmp;

    @Property(name = "mdplayer.fmp.dir")
    String fmpDir;
    @Property(name = "mdplayer.fmp.pvi")
    String fmpPvi;

    @Property(name= "fmp.dir")
    String fmpCompileDir;

    @Property
    String dir;

    /** PC-98 font rom, the comment lines have no Japanese glyphs without it */
    @Property(name = "fmdsp.fontRom")
    String fontRom;

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 1000 * 1000 : 10 * 1000;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);

            // fmp
            System.setProperty("mdplayer.fmp.dir", fmpDir);
            System.setProperty("mdplayer.fmp.pvi", fmpPvi);
        }

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
Debug.println("volume: " + volume + ", player.volume: " + System.getProperty("mdplayer.volume") + ", cwd: " + System.getProperty("user.dir") + ", time: " + time);
Debug.println("settings\n" +
 "mdplayer.fmp.dir: " + System.getProperty("mdplayer.fmp.dir") + "\n" +
 "mdplayer.fmp.pvi: " + System.getProperty("mdplayer.fmp.pvi"));
    }

    private final Audio audio = Audio.getInstance();

    @Test
    @DisplayName("compile mml")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test() throws Exception {
Debug.println("filename: " + fmp);
        FileFormat format = FileFormat.getFileFormat(fmp);
        format.load(Files.newInputStream(Path.of(fmp)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", fmp));
        audio.init(plugin);
        audio.play();
    }

    @Test
    @DisplayName("play fmp w/ fmdsp visualizer")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
Debug.println("filename: " + fmp);
        FileFormat format = FileFormat.getFileFormat(fmp);
        format.load(Files.newInputStream(Path.of(fmp)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", fmp));

        FmpFmDspSource source = new FmpFmDspSource();
        source.setFilename(Path.of(fmp).getFileName().toString());

        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);
        if (fontRom != null && Files.exists(Path.of(fontRom))) {
            visualizer.setFontRom(Files.readAllBytes(Path.of(fontRom)));
        }

        JFrame frame = new JFrame();
        frame.setTitle(Path.of(fmp).getFileName() + " - FMP");
        frame.setLayout(new BorderLayout());
        frame.add(visualizer, BorderLayout.CENTER);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F10) {
                    visualizer.setPaletteIndex(code - KeyEvent.VK_F1);
                } else if (code == KeyEvent.VK_F11) {
                    if (e.isShiftDown()) {
                        RightMode[] r = RightMode.values();
                        visualizer.setRightMode(r[(visualizer.getRightMode().ordinal() + 1) % r.length]);
                    } else {
                        LeftMode[] l = LeftMode.values();
                        visualizer.setLeftMode(l[(visualizer.getLeftMode().ordinal() + 1) % l.length]);
                    }
                } else if (code == KeyEvent.VK_SPACE) {
                    audio.pause();
                    source.setPaused(audio.isPaused());
                }
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocusInWindow();

        audio.init(plugin);
        audio.addGenericListener(source::update);
        visualizer.start();
        audio.play();
        audio.close();
        visualizer.stop();
        frame.setVisible(false);
        frame.dispose();
    }

    @Test
    @DisplayName("play multi fmp w/ fmdsp visualizer")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        Random random = new Random(System.currentTimeMillis());

        FmpFmDspSource source = new FmpFmDspSource();

        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);
        if (fontRom != null && Files.exists(Path.of(fontRom))) {
            visualizer.setFontRom(Files.readAllBytes(Path.of(fontRom)));
        }

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(visualizer, BorderLayout.CENTER);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F10) {
                    visualizer.setPaletteIndex(code - KeyEvent.VK_F1);
                } else if (code == KeyEvent.VK_F11) {
                    if (e.isShiftDown()) {
                        RightMode[] r = RightMode.values();
                        visualizer.setRightMode(r[(visualizer.getRightMode().ordinal() + 1) % r.length]);
                    } else {
                        LeftMode[] l = LeftMode.values();
                        visualizer.setLeftMode(l[(visualizer.getLeftMode().ordinal() + 1) % l.length]);
                    }
                } else if (code == KeyEvent.VK_SPACE) {
                    audio.pause();
                    source.setPaused(audio.isPaused());
                }
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocusInWindow();

        audio.addGenericListener(source::update);
        visualizer.start();

        List<Path> list = listFilesUnderDirFilteredByExt(dir, ".OVI,.OPI,.OZI,.MVI,.MPI,.MZI");

        int c = list.size();
        while (c > 0) {
            Path p = list.get(random.nextInt(list.size()));
Debug.println("filename: " + p);
            frame.setTitle(p.getFileName() + " - FMP");
            source.reset(); // each song comes with a new driver, whose counters start at 0 again
            source.setFilename(p.getFileName().toString());

            FileFormat format = FileFormat.getFileFormat(p.toString());
            format.load(Files.newInputStream(p), null);
            var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
            plugin.setParams(format, Map.of("fileName", p.toString()));

            audio.init(plugin);
            audio.play();
            audio.close();

            c--;
        };

        visualizer.stop();
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * @param dir separated by ';'
     * @param ext separated by ','
     */
    static List<Path> listFilesUnderDirFilteredByExt(String dir, String ext) {
Debug.println("dir: " + dir);
Debug.println("ext: " + ext);
        Predicate<Path> x = p -> Arrays.stream(ext.split(",")).anyMatch(e -> p.getFileName().toString().toUpperCase().endsWith(e));
        return Arrays.stream(dir.split(File.pathSeparator)).flatMap(d -> {
            try {
                return Files.walk(Paths.get(d)).filter(x);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).toList();
    }

    @Test
    @Disabled("under construction")
    @DisplayName("compile dir")
    void test1() throws Exception {
        listFilesUnderDirFilteredByExt(fmpCompileDir, ".MVI,.MPI,.MZI").forEach(p -> {
            try {
                FileFormat format = FileFormat.getFileFormat(p.toString());
                format.load(Files.newInputStream(p), null);
                var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
                plugin.setParams(format, Map.of("fileName", p.toString()));
                ((Compilable) plugin).compile();
            } catch (Exception e) {
                Debug.println(e.toString());
            }
        });
    }
}
