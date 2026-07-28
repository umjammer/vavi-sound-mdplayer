/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.visualizer.fmdsp;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.LeftMode;
import vavi.sound.visualizer.fmdsp.RightMode;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Plays a MIDI file with nothing but the Java MIDI SPI and shows it on {@link FmDspVisualizer}
 * through {@link MidiFmDspSource}.
 * <pre>{@code
 *  mvn -o -Dtest.class=vavi.sound.midi.visualizer.fmdsp.MidiFmDspDemo \
 *      -Dtest.args=/path/to/song.mid ... # or simply run this main from the IDE
 * }</pre>
 * The sequencer is opened without its own device so the stream goes only where it is pointed:
 * through the source, which passes it on to the software synthesizer. Note that
 * {@code MidiSystem.getReceiver()} is a hardware port that is silent on most machines - the
 * synthesizer's own receiver is what sounds.
 * <p>
 * F1..F10 palette, F11 / Shift+F11 the left / right layout, SPACE pause, ESC quit.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-28 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public final class MidiFmDspDemo {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    float volume = 0.2f;

    @Property
    String midi;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @DisplayName("midi fmdsp visualizer")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
Debug.print(midi);
        main(new String[] {midi});

        CountDownLatch cdl = new CountDownLatch(1);
        cdl.await();
    }

    private MidiFmDspDemo() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: MidiFmDspDemo <midi file>");
            return;
        }
        File file = Path.of(args[0]).toFile();
        Sequence sequence = MidiSystem.getSequence(file);

        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
        Sequencer sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.setSequence(sequence);

        MidiFmDspSource source = new MidiFmDspSource(synthesizer.getReceiver());
        source.setFilename(file.getName());
        sequencer.getTransmitter().setReceiver(source);
        // the sequencer keeps its meta events to itself, so the end of the song comes from here
        sequencer.addMetaEventListener(meta -> {
            if (meta.getType() == 0x2f) source.stop();
        });

        SwingUtilities.invokeLater(() -> launch(source, sequencer, synthesizer));
        sequencer.start();
    }

    private static void launch(MidiFmDspSource source, Sequencer sequencer, Synthesizer synthesizer) {
        FmDspVisualizer vis = new FmDspVisualizer(60);
        vis.setDataSource(source);
        vis.setLeftMode(LeftMode.AUTO);

        JFrame frame = new JFrame("FMDSP / plain Java MIDI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(vis, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F10) {
                    vis.setPaletteIndex(code - KeyEvent.VK_F1);
                } else if (code == KeyEvent.VK_F11) {
                    if (e.isShiftDown()) {
                        RightMode[] r = RightMode.values();
                        vis.setRightMode(r[(vis.getRightMode().ordinal() + 1) % r.length]);
                    } else {
                        LeftMode[] l = LeftMode.values();
                        vis.setLeftMode(l[(vis.getLeftMode().ordinal() + 1) % l.length]);
                    }
                } else if (code == KeyEvent.VK_SPACE) {
                    if (sequencer.isRunning()) sequencer.stop(); else sequencer.start();
                    source.setPaused(!sequencer.isRunning());
                } else if (code == KeyEvent.VK_ESCAPE) {
                    sequencer.close();
                    synthesizer.close();
                    frame.dispose();
                }
            }
        });
        frame.requestFocusInWindow();

        vis.start();
    }
}
