/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.pmd;

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
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.LeftMode;
import vavi.sound.visualizer.fmdsp.RightMode;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * PMD.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-13 nsano initial version <br>
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
    String pmd;

    @Property
    String dir;

    /** PC-98 font rom, the comment lines have no Japanese glyphs without it */
    @Property(name = "fmdsp.fontRom")
    String fontRom;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
Debug.println("volume: " + volume + ", player.volume: " + System.getProperty("mdplayer.volume") + ", cwd: " + System.getProperty("user.dir"));

        audio = Audio.getInstance(); // ⚠️ caution settings and system properties race condition
    }

    private Audio audio;

    @Test
    @DisplayName("play pmd w/ fmdsp visualizer")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test() throws Exception {
Debug.println("filename: " + pmd);
        FileFormat format = FileFormat.getFileFormat(pmd);
        format.load(Files.newInputStream(Path.of(pmd)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", pmd));

        PmdFmDspSource source = new PmdFmDspSource();
        source.setFilename(Path.of(pmd).getFileName().toString());

        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);
        if (fontRom != null && Files.exists(Path.of(fontRom))) {
            visualizer.setFontRom(Files.readAllBytes(Path.of(fontRom)));
        }

        JFrame frame = new JFrame();
        frame.setTitle(Path.of(pmd).getFileName() + " - PMD");
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
    @DisplayName("play multi pmd w/ fmdsp visualizer")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        Random random = new Random(System.currentTimeMillis());

        PmdFmDspSource source = new PmdFmDspSource();

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

        List<Path> list = listFilesUnderDirFilteredByExt(dir, ".M,.M2,.MZ");

        int c = list.size();
        while (c > 0) {
            Path p = list.get(random.nextInt(list.size()));
Debug.println("filename: " + p);
            frame.setTitle(p.getFileName() + " - PMD");
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
        }

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
}
