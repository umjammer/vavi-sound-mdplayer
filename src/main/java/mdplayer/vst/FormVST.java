/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.vst;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.Timer;

import mdplayer.Common;
import org.urish.jnavst.ERect;
import org.urish.jnavst.VstPlugin;

import static java.lang.System.getLogger;


/**
 * The window a plug-in's editor lives in.
 * <p>
 * A VST editor is drawn by the plug-in itself into a window the host owns, which means handing it
 * a native window handle. Whether that handle can be got at from Java depends on the platform, so
 * when it cannot be - and for the plug-ins that have no editor of their own at all - the
 * parameters are shown as plain sliders instead. Either way the plug-in is playable; only the
 * looks differ.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 * @version 0.01 2026-08-08 nsano the editor actually opens <br>
 */
public class FormVST extends JFrame {

    private static final Logger logger = getLogger(FormVST.class.getName());

    /** how many parameters the fallback editor will draw; some plug-ins have hundreds */
    private static final int MAX_PARAMETERS = 64;

    private VstPlugin plugin;

    /** what the plug-in draws into, while it is drawing itself */
    private Canvas canvas;

    /** ticks while the plug-in owns the window: its editor stops repainting without this */
    private final Timer timer;

    public FormVST(Frame owner) {
        this.timer = new Timer(20, this::tick);
        initializeComponent();
    }

    public VstPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(VstPlugin plugin) {
        this.plugin = plugin;
    }

    /** kept for the code that was written against the old name */
    public VstPlugin getPluginCommandStub() {
        return plugin;
    }

    public void setPluginCommandStub(VstPlugin plugin) {
        setPlugin(plugin);
    }

    /**
     * Opens the editor for a plug-in, where it was last left.
     */
    public void show(VstMng.VstInfo2 vi) {
        setTitle(vi.effectName == null || vi.effectName.isEmpty() ? plugin.getName() : vi.effectName);

        if (vi.location != null && (vi.location.x != 0 || vi.location.y != 0)) {
            setLocation(new Point(vi.location.x, vi.location.y));
        }

        // the canvas has to be on screen before the plug-in is given it: what it wants is a
        // native window, and a component that has not been realized does not have one yet
        setVisible(true);

        if (!openNative()) {
            openFallback();
        }
    }

    /** hands the window to the plug-in; false when this platform cannot say what the handle is */
    private boolean openNative() {
        if (plugin == null || !plugin.hasEditor()) return false;

        if (!plugin.editOpen(canvas)) return false;

        ERect rect = plugin.getEditRect();
        if (rect != null) {
            Dimension size = new Dimension(rect.right - rect.left, rect.bottom - rect.top);
            canvas.setPreferredSize(size);
            pack();
        }
        timer.start();
        return true;
    }

    /** sliders, for a plug-in whose own editor cannot be shown here */
    private void openFallback() {
        remove(canvas);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        int count = Math.min(plugin.getNumParams(), MAX_PARAMETERS);
        for (int i = 0; i < count; i++) {
            int index = i;
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            panel.add(new JLabel(plugin.getParamName(index)), c);

            JSlider slider = new JSlider(0, 1000, (int) (plugin.getParameter(index) * 1000));
            JLabel value = new JLabel(plugin.getParamDisplay(index) + " " + plugin.getParamLabel(index));
            slider.addChangeListener(e -> {
                plugin.setParameter(index, slider.getValue() / 1000f);
                value.setText(plugin.getParamDisplay(index) + " " + plugin.getParamLabel(index));
            });
            c.gridx = 1;
            c.weightx = 1;
            panel.add(slider, c);
            c.gridx = 2;
            c.weightx = 0;
            panel.add(value, c);
        }

        if (count == 0) {
            panel.add(new JLabel("this plugin has nothing to show"), c);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        getContentPane().add(scroll, BorderLayout.CENTER);
        setSize(new Dimension(480, Math.min(600, 40 + count * 28)));
        validate();
    }

    /** closes the editor, leaving the plug-in itself alone */
    public void close() {
        timer.stop();
        try {
            if (plugin != null) plugin.editClose();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "closing the editor failed", t);
        }
        setVisible(false);
        dispose();
    }

    private void tick(ActionEvent ev) {
        try {
            if (plugin == null || !plugin.isEditorOpen()) {
                timer.stop();
                return;
            }
            plugin.editIdle();
        } catch (Throwable t) {
            timer.stop();
            logger.log(Level.ERROR, "the editor of " + getTitle() + " stopped being updated", t);
        }
    }

    private void initializeComponent() {
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(new Dimension(284, 261));

        this.setLayout(new BorderLayout());
        this.getContentPane().add(this.canvas, BorderLayout.CENTER);
        this.setIconImage(Common.getImage("Feli128"));
        this.setName("frmVST");
        this.setTitle("VST");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                close();
            }
        });
        this.pack();
    }
}
