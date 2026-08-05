/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.boids;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import mdplayer.Audio;
import mdplayer.ChipFmDspSource;
import mdplayer.LocalProperties;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.util.archive.Archives;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Plays the song named by {@code file} in {@code local.properties} and watches it as a flock, with
 * the sliders beside it - which is the only way to find out what the parameters should be.
 * <p>
 * The data comes from {@link ChipFmDspSource}, the same generic chip reader the FMDSP visualizer
 * uses, so this works for every driver that one works for.
 * <p>
 * Runs from the IDE only ({@code -Dvavi.test=ide}); SPACE pauses, P walks the presets, ESC stops.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
class BoidsPlayTest {

    static boolean localPropertiesExists() {
        return LocalProperties.exists();
    }

    @Property
    String file;

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @BeforeEach
    void setup() throws Exception {
        LocalProperties.bind();
        PropsEntity.Util.bind(this);

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
    }

    @Test
    @DisplayName("play a song and watch it as a flock of boids")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void play() throws Exception {
        Audio audio = Audio.getInstance();

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));

        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(plugin);
        source.setFilename(Path.of(file).getFileName().toString());

        BoidsVisualizer visualizer = new BoidsVisualizer(60, 800, 560);
        visualizer.setDataSource(source);
        BoidsControlPanel controls = new BoidsControlPanel(visualizer.getParams());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, visualizer, controls);
        split.setResizeWeight(1);

        JFrame frame = new JFrame(Path.of(file).getFileName() + " - boids");
        frame.setLayout(new BorderLayout());
        frame.add(split, BorderLayout.CENTER);
        frame.addKeyListener(new KeyAdapter() {
            private int preset;

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE -> {
                        audio.pause();
                        source.setPaused(audio.isPaused());
                    }
                    case KeyEvent.VK_P -> {
                        List<String> names = BoidsParams.presetNames();
                        preset = (preset + 1) % names.size();
                        visualizer.getParams().applyPreset(names.get(preset));
                    }
                    case KeyEvent.VK_ESCAPE -> frame.dispose();
                    default -> {}
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
}
