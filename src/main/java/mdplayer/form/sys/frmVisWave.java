package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.Timer;

import dotnet4j.util.compat.Tuple;
import mdplayer.Audio;
import mdplayer.form.frmBase;
import mdplayer.properties.Resources;
import vavi.util.SplitRadixFft;


public class frmVisWave extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;

    private short[][] buf = new short[][] {new short[2048], new short[2048]};
    private Graphics2D g;
    private BufferedImage bmp;
    private int dispType = 1;
    private double dispHeight = 1.0;
    private boolean fft = false;

    static Preferences prefs = Preferences.userNodeForPackage(frmVisWave.class);

    public frmVisWave(frmMain frm) {
        parent = frm;
        initializeComponent();
        bmp = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
    }

    private void timer1_Tick(ActionEvent ev) {
        Audio.copyWaveBuffer(buf);

        g.setColor(Color.black);
        g.fillRect(0, 0, bmp.getWidth(), bmp.getHeight());

        if (fft) {
            float[] a = convertTo(buf[0]);
            processFFT(a);
            buf[0] = convertTo(a);

            a = convertTo(buf[1]);
            processFFT(a);
            buf[1] = convertTo2(a);
        }

        for (int ch = 0; ch < 2; ch++) {
            if (dispType <= 1) {
                int hPos = bmp.getHeight() / 4 + ch * bmp.getHeight() / 2;
                int ox = 0;
                int oy = hPos;

                for (int i = 0; i < 2048; i++) {
                    int x = (i * bmp.getWidth()) / 2048;
                    int y = (int) (buf[ch][i] * dispHeight) * bmp.getHeight() / 65536 + hPos;
                    g.setColor(new Color(0x46, 0x82, 0xb4)); // SteelBlue
                    g.drawLine(ox, oy, x, y);
                    ox = x;
                    oy = y;
                }
            } else {
                int hPos = bmp.getHeight() / 4 + ch * bmp.getHeight() / 2;
                for (int i = 0; i < 2048; i++) {
                    int x = (i * bmp.getWidth()) / 2048;
                    int y = (int) (buf[ch][i] * dispHeight) * bmp.getHeight() / 65536 + hPos;
                    g.setColor(new Color(0xF0, 0xE6, 0x8C)); // Khaki
                    g.drawLine(x, hPos, x, y);
                }
            }
        }

        pictureBox1 = bmp;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().setPosVisWave(getLocation());
            } else {
                parent.setting.getLocation().setPosVisWave(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
            }
            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent e) {
            g = (Graphics2D) bmp.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//        pictureBox1.SizeMode = BufferedImageSizeMode.StretchImage;
        }

        @Override
        public void windowActivated(WindowEvent e) {
            setLocation(new Point(x, y));
        }
    };

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private void tsbDispType2_Click(ActionEvent ev) {
        dispType = 2;
    }

    private void tsbDispType1_Click(ActionEvent ev) {
        dispType = 1;
    }

    private void tsbHeight3_Click(ActionEvent ev) {
        dispHeight = 3;
    }

    private void tsbHeight2_Click(ActionEvent ev) {
        dispHeight = 1.0;
    }

    private void tsbHeight1_Click(ActionEvent ev) {
        dispHeight = 0.3;
    }

    public float[] convertTo(short[] src) {
        for (int i = 0; i < src.length; i++) {
            destF[i] = src[i] / 32768.0f;
        }
        return destF;
    }

    public short[] convertTo(float[] src) {
        for (int i = 0; i < src.length / 2; i++) {
            destS[i * 2] = (short) (Math.min(Math.max(-src[i] * 150.0f * 32768.0f * 0.6, Short.MIN_VALUE), Short.MAX_VALUE));
            destS[i * 2 + 1] = (short) (Math.min(Math.max(-src[i] * 150.0f * 32768.0f * 0.6, Short.MIN_VALUE), Short.MAX_VALUE));
        }
        return destS;
    }

    public short[] convertTo2(float[] src) {
        for (int i = 0; i < src.length / 2; i++) {
            destS2[i * 2] = (short) (Math.min(Math.max(-src[i] * 150.0f * 32768.0f * 0.6, Short.MIN_VALUE), Short.MAX_VALUE));
            destS2[i * 2 + 1] = (short) (Math.min(Math.max(-src[i] * 150.0f * 32768.0f * 0.6, Short.MIN_VALUE), Short.MAX_VALUE));
        }
        return destS2;
    }

    private float[] destF = new float[2048];
    private short[] destS = new short[2048];
    private short[] destS2 = new short[2048];
    private Tuple<Float, Float>[] fftsample = new Tuple[2048]; // Complex

    public void processFFT(float[] sdata) {
        FFT fft = new FFT();

        for (int i = 0; i < sdata.length; i++) {
            fftsample[i].setItem1((float) (sdata[i] * fft.win(i, sdata.length)));
            fftsample[i].setItem2(0f);
        }

        fft.fft(true, (int) (Math.log(sdata.length) / Math.log(2)), fftsample);

        for (int i = 0; i < sdata.length; i++) {
            sdata[i] = (float) Math.sqrt(fftsample[i].getItem1() * fftsample[i].getItem1() + fftsample[i].getItem2() * fftsample[i].getItem2()); // パワースペクトル
        }
    }

    static class FFT {
        private static final int M = 15;

        private double[] fact = new double[M + 1];

        private double aa = 96;

        private double iza;

        private double alpha(double a) {
            if (a <= 21) {
                return 0;
            }
            if (a <= 50) {
                return 0.5842 * Math.pow(a - 21, 0.4) + 0.07886 * (a - 21);
            }
            return 0.1102 * (a - 8.7);
        }

        private double win(double n, int N) {
            return izero(alpha(aa) * Math.sqrt(1 - 4 * n * n / ((N - 1) * (N - 1)))) / iza;
        }

        private double izero(double x) {
            double ret = 1;

            for (int m = 1; m <= M; m++) {
                double t = Math.pow(x / 2, m) / fact[m];
                ret += t * t;
            }

            return ret;
        }

        private void rfft(int n, int isign, double[] x) {
            int ipsize = 0, wsize = 0;
            int[] ip = null;
            double[] w = null;
            int newipsize, newwsize;

            if (n == 0) {
                ip = null;
                ipsize = 0;
                w = null;
                wsize = 0;
                return;
            }

            newipsize = (int) (2 + Math.sqrt(n / 2));
            if (newipsize > ipsize) {
                ipsize = newipsize;
                ip = new int[ipsize];
                ip[0] = 0;
            }

            newwsize = n / 2;
            if (newwsize > wsize) {
                wsize = newwsize;
                w = new double[wsize];
            }

            SplitRadixFft.rdft(n, isign, x, ip, w);
        }

        void fft(boolean b, int i, Tuple<Float, Float>[] fftsample) {
            // TODO
        }
    }

    private void tsbFFT_Click(ActionEvent ev) {
        fft = tsbFFT.isSelected();
    }

    private void initializeComponent() {
//            this.components = new System.ComponentModel.Container();
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmVisWave));
        this.pictureBox1 = new BufferedImage(224, 176, BufferedImage.TYPE_INT_ARGB);
        this.timer1 = new Timer(10, this::timer1_Tick);
        this.toolStripContainer1 = new JLabel();
        this.toolStrip1 = new JPopupMenu();
        this.tsbHeight1 = new JButton();
        this.tsbHeight2 = new JButton();
        this.tsbHeight3 = new JButton();
        this.toolStripSeparator1 = new JSeparator();
        this.tsbDispType1 = new JButton();
        this.tsbDispType2 = new JButton();
        this.tsbFFT = new JRadioButton();
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
//        this.toolStripContainer1.ContentPanel.SuspendLayout();
//        this.toolStripContainer1.TopToolStripPanel.SuspendLayout();
//        this.toolStripContainer1.SuspendLayout();
//        this.toolStrip1.SuspendLayout();

        //
        // pictureBox1
        //
//        this.pictureBox1.Dock = JDockStyle.Fill;
//        this.pictureBox1.setLocation(new Point(0, 0));
//        this.pictureBox1.setName("pictureBox1");
        // this.pictureBox1.TabIndex = 0
        // this.pictureBox1.TabStop = false;
        //
        // timer1
        //
        this.timer1.start();
        //
        // toolStripContainer1
        //
        //
        // toolStripContainer1.ContentPanel
        //
        this.toolStripContainer1.setIcon(new ImageIcon(this.pictureBox1));
        this.toolStripContainer1.setPreferredSize(new Dimension(224, 176));
//        this.toolStripContainer1.Dock = JDockStyle.Fill;
        this.toolStripContainer1.setLocation(new Point(0, 0));
        this.toolStripContainer1.setName("toolStripContainer1");
        this.toolStripContainer1.setPreferredSize(new Dimension(224, 201));
        // this.toolStripContainer1.TabIndex = 1
        this.toolStripContainer1.setToolTipText("toolStripContainer1");
        //
        // toolStripContainer1.TopToolStripPanel
        //
        this.toolStripContainer1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    toolStrip1.show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });
        //
        // toolStrip1
        //
//        this.toolStrip1.Dock = JDockStyle.None;
        this.toolStrip1.add(this.tsbHeight1);
        this.toolStrip1.add(this.tsbHeight2);
        this.toolStrip1.add(this.tsbHeight3);
        this.toolStrip1.add(this.toolStripSeparator1);
        this.toolStrip1.add(this.tsbDispType1);
        this.toolStrip1.add(this.tsbDispType2);
        this.toolStrip1.add(this.tsbFFT);
        this.toolStrip1.setLocation(new Point(3, 0));
        this.toolStrip1.setName("toolStrip1");
        this.toolStrip1.setPreferredSize(new Dimension(187, 25));
        // this.toolStrip1.TabIndex = 0
        //
        // tsbHeight1
        //
//        this.tsbHeight1.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbHeight1.setIcon(new ImageIcon(mdplayer.properties.Resources.getVHeight1()));
        this.tsbHeight1.setBackground(Color.magenta);
        this.tsbHeight1.setName("tsbHeight1");
        this.tsbHeight1.setPreferredSize(new Dimension(23, 22));
        this.tsbHeight1.setText("Height x 0.3");
        this.tsbHeight1.addActionListener(this::tsbHeight1_Click);
        //
        // tsbHeight2
        //
//        this.tsbHeight2.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbHeight2.setIcon(new ImageIcon(mdplayer.properties.Resources.getVHeight2()));
        this.tsbHeight2.setBackground(Color.magenta);
        this.tsbHeight2.setName("tsbHeight2");
        this.tsbHeight2.setPreferredSize(new Dimension(23, 22));
        this.tsbHeight2.setText("Height x 1.0");
        this.tsbHeight2.addActionListener(this::tsbHeight2_Click);
        //
        // tsbHeight3
        //
//        this.tsbHeight3.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbHeight3.setIcon(new ImageIcon(mdplayer.properties.Resources.getVHeight3()));
        this.tsbHeight3.setBackground(Color.magenta);
        this.tsbHeight3.setName("tsbHeight3");
        this.tsbHeight3.setPreferredSize(new Dimension(23, 22));
        this.tsbHeight3.setText("Height x 3.0");
        this.tsbHeight3.addActionListener(this::tsbHeight3_Click);
        //
        // toolStripSeparator1
        //
        this.toolStripSeparator1.setName("toolStripSeparator1");
        this.toolStripSeparator1.setPreferredSize(new Dimension(6, 25));
        //
        // tsbDispType1
        //
//        this.tsbDispType1.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbDispType1.setIcon(new ImageIcon(mdplayer.properties.Resources.getVType1()));
        this.tsbDispType1.setBackground(Color.magenta);
        this.tsbDispType1.setName("tsbDispType1");
        this.tsbDispType1.setPreferredSize(new Dimension(23, 22));
        this.tsbDispType1.setText("type 1");
        this.tsbDispType1.addActionListener(this::tsbDispType1_Click);
        //
        // tsbDispType2
        //
//        this.tsbDispType2.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbDispType2.setIcon(new ImageIcon(mdplayer.properties.Resources.getVType2()));
        this.tsbDispType2.setBackground(Color.magenta);
        this.tsbDispType2.setName("tsbDispType2");
        this.tsbDispType2.setPreferredSize(new Dimension(23, 22));
        this.tsbDispType2.setText("type 2");
        this.tsbDispType2.addActionListener(this::tsbDispType2_Click);
        //
        // tsbFFT
        //
//        this.tsbFFT.CheckOnClick = true;
//        this.tsbFFT.DisplayStyle = JToolStripItemDisplayStyle.Image;
        this.tsbFFT.setIcon(new ImageIcon(mdplayer.properties.Resources.getVType3()));
        this.tsbFFT.setBackground(Color.magenta);
        this.tsbFFT.setName("tsbFFT");
        this.tsbFFT.setPreferredSize(new Dimension(23, 22));
        this.tsbFFT.setText("FFT");
        this.tsbFFT.addActionListener(this::tsbFFT_Click);
        //
        // frmVisWave
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(224, 201));
        this.getContentPane().add(this.toolStripContainer1);
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
        this.setName("frmVisWave");
        this.setOpacity(0.9f);
        this.setTitle("Visualizer");
        this.addWindowListener(this.windowListener);
        //((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
        // this.toolStripContainer1.ContentPanel.ResumeLayout(false);
        // this.toolStripContainer1.TopToolStripPanel.ResumeLayout(false);
        // this.toolStripContainer1.TopToolStripPanel.PerformLayout();
        // this.toolStripContainer1.ResumeLayout(false);
        // this.toolStripContainer1.PerformLayout();
        // this.toolStrip1.ResumeLayout(false);
        // this.toolStrip1.PerformLayout();
//        this.ResumeLayout(false);
    }

    private BufferedImage pictureBox1;
    private Timer timer1;
    private JLabel toolStripContainer1;
    private JPopupMenu toolStrip1;
    private JButton tsbHeight1;
    private JButton tsbHeight2;
    private JButton tsbHeight3;
    private JSeparator toolStripSeparator1;
    private JButton tsbDispType1;
    private JButton tsbDispType2;
    private JRadioButton tsbFFT;
}
