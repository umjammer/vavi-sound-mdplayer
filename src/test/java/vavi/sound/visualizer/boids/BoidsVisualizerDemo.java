/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.boids;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


/**
 * Stand-alone demo: {@link BoidsVisualizer} driven by a synthetic chord sequence, with the whole
 * parameter set on sliders next to it.
 * <p>
 * ESC closes it, SPACE stops the notes so that the flock can be watched settling, and TAB hides the
 * sliders.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
public final class BoidsVisualizerDemo {

    private BoidsVisualizerDemo() {}

    static void main(String[] args) {
        SwingUtilities.invokeLater(BoidsVisualizerDemo::launch);
    }

    private static void launch() {
        BoidsVisualizer vis = new BoidsVisualizer(60, 800, 560);
        SyntheticSongSource data = new SyntheticSongSource();
        vis.setDataSource(data);

        // the synthetic song has no player to advance it, so it is clocked here
        Timer clock = new Timer(1000 / 120, e -> data.advance(1 / 120d));
        clock.start();

        BoidsControlPanel controls = new BoidsControlPanel(vis.getParams());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, vis, controls);
        split.setResizeWeight(1);

        JFrame frame = new JFrame("boids / mdplayer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(split, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE -> data.setSilent(!data.playing());
                    case KeyEvent.VK_TAB -> {
                        controls.setVisible(!controls.isVisible());
                        split.resetToPreferredSizes();
                    }
                    case KeyEvent.VK_ESCAPE -> frame.dispose();
                    default -> {}
                }
            }
        });
        frame.requestFocusInWindow();

        vis.start();
    }
}
