/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.boids;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

import vavi.sound.visualizer.boids.BoidsParams.Group;
import vavi.sound.visualizer.boids.BoidsParams.Param;


/**
 * A slider for every {@link Param}, so that the flock can be tuned while it is flying.
 * <p>
 * Nothing here knows the name of a single parameter: the panel is built by walking
 * {@link Param#values()} and grouping them by {@link Group}, which is what keeps adding a knob to
 * {@link BoidsParams} a one line change. A preset can be picked, and a set that turned out well can
 * be written to a properties file and read back.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
class BoidsControlPanel extends JPanel {

    /** the resolution a slider carries a fractional value at */
    private static final int STEPS = 1000;

    private final BoidsParams params;

    private final Map<Param, JSlider> sliders = new EnumMap<>(Param.class);
    private final Map<Param, JLabel> readouts = new EnumMap<>(Param.class);

    /** true while the panel is writing the sliders itself, so that it does not answer its own echo */
    private boolean adjusting;

    private Path file;

    public BoidsControlPanel(BoidsParams params) {
        this.params = params;
        setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        for (Group group : Group.values()) {
            JPanel section = section(group);
            if (section != null) body.add(section);
        }
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        add(buttons(), BorderLayout.SOUTH);

        setPreferredSize(new Dimension(300, 560));

        params.addListener(this::refresh);
        refresh();
    }

    /** The sliders of one group, or null if the group has none. */
    private JPanel section(Group group) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(group.name().toLowerCase()));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 4, 1, 4);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;
        for (Param p : Param.values()) {
            if (p.group != group) continue;

            JLabel name = new JLabel(p.label);
            name.setFont(name.getFont().deriveFont(Font.PLAIN, 11f));
            c.gridx = 0;
            c.gridy = row;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(name, c);

            JSlider slider = new JSlider(0, STEPS, 0);
            slider.setToolTipText(p.name() + "  (" + format(p, p.min) + " .. " + format(p, p.max)
                    + ", default " + format(p, p.def) + ")");
            slider.setPreferredSize(new Dimension(140, 18));
            slider.addChangeListener(e -> {
                if (adjusting) return;
                params.set(p, p.min + (p.max - p.min) * slider.getValue() / (double) STEPS);
            });
            c.gridx = 1;
            c.gridy = row;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(slider, c);

            JLabel readout = new JLabel();
            readout.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            readout.setPreferredSize(new Dimension(46, 16));
            c.gridx = 2;
            c.gridy = row;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(readout, c);

            sliders.put(p, slider);
            readouts.put(p, readout);
            row++;
        }
        return row == 0 ? null : panel;
    }

    private JPanel buttons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));

        JComboBox<String> presets = new JComboBox<>(BoidsParams.presetNames().toArray(new String[0]));
        presets.setToolTipText("a starting point, not a menu");
        presets.addActionListener(e -> {
            Object name = presets.getSelectedItem();
            if (name != null) params.applyPreset(name.toString());
        });
        panel.add(presets);

        JButton reset = new JButton("reset");
        reset.addActionListener(e -> params.reset());
        panel.add(reset);

        JButton save = new JButton("save...");
        save.addActionListener(e -> {
            Path path = choose(true);
            if (path == null) return;
            try {
                params.save(path);
                file = path;
            } catch (IOException x) {
                error(x);
            }
        });
        panel.add(save);

        JButton load = new JButton("load...");
        load.addActionListener(e -> {
            Path path = choose(false);
            if (path == null) return;
            try {
                params.load(path);
                file = path;
            } catch (IOException x) {
                error(x);
            }
        });
        panel.add(load);

        return panel;
    }

    private Path choose(boolean save) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("boids parameters (*.properties)", "properties"));
        if (file != null) chooser.setSelectedFile(file.toFile());
        int result = save ? chooser.showSaveDialog(this) : chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return null;
        Path path = chooser.getSelectedFile().toPath();
        if (save && !path.toString().contains(".")) path = Path.of(path + ".properties");
        return path;
    }

    private void error(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "boids", JOptionPane.ERROR_MESSAGE);
    }

    /** Writes the parameter set into the sliders; called whenever anything changes it. */
    private void refresh() {
        adjusting = true;
        try {
            for (Map.Entry<Param, JSlider> entry : sliders.entrySet()) {
                Param p = entry.getKey();
                double v = params.get(p);
                entry.getValue().setValue((int) Math.round((v - p.min) / (p.max - p.min) * STEPS));
                readouts.get(p).setText(format(p, v));
            }
        } finally {
            adjusting = false;
        }
    }

    private static String format(Param p, double v) {
        return p.isIntegral() ? Integer.toString((int) Math.round(v))
                : ("%." + p.decimals + "f").formatted(v);
    }
}
