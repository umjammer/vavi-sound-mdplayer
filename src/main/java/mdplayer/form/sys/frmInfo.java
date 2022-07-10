package mdplayer.form.sys;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import dotnet4j.util.compat.Tuple3;
import mdplayer.Audio;
import mdplayer.Setting;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;
import mdplayer.properties.Resources;
import vavi.util.Debug;


public class frmInfo extends JFrame {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public frmMain parent;
    public Setting setting = null;
    public List<Tuple3<Integer, Integer, String>> lyrics = null;
    public int lyricsIndex = 0;
    private Color culColor = new Color(192, 192, 255);

    static Preferences prefs = Preferences.userNodeForPackage(frmInfo.class);

    public frmInfo(frmMain frm) {
        parent = frm;
        initializeComponent();
        rtbLyrics.addFocusListener(RichTextBox1_GotFocus);
        update();
    }

    private FocusListener RichTextBox1_GotFocus = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            lblComposer.requestFocus();
        }
    };

    public void update() {

        lblTitle.setText("");
        lblTitleJ.setText("");
        lblGame.setText("");
        lblGameJ.setText("");
        lblSystem.setText("");
        lblSystemJ.setText("");
        lblComposer.setText("");
        lblComposerJ.setText("");
        lblRelease.setText("");
        lblVGMBy.setText("");
        lblNotes.setText("");
        lblVersion.setText("");
        lblUsedChips.setText("");
        rtbLyrics.setText(null);

        Vgm.Gd3 gd3 = Audio.getInstance().getGd3();
        if (gd3 == null) return;

        lblTitle.setText(gd3.trackName);
        lblTitleJ.setText(gd3.trackNameJ);
        lblGame.setText(gd3.gameName);
        lblGameJ.setText(gd3.gameNameJ);
        lblSystem.setText(gd3.systemName);
        lblSystemJ.setText(gd3.systemNameJ);
        lblComposer.setText(gd3.composer);
        lblComposerJ.setText(gd3.composerJ);
        lblRelease.setText(gd3.converted);
        lblVGMBy.setText(gd3.vgmBy);
        lblNotes.setText(gd3.notes);
        lblVersion.setText(gd3.version);
        lblUsedChips.setText(gd3.usedChips);

        if (gd3.lyrics == null) {
            timer.stop();
        } else {
            lyrics = gd3.lyrics;
            timer.start();
        }
    }

    public void screenInit() {
        lyricsIndex = 0;
        culColor = new Color(192, 192, 255);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().setPInfo(getLocation());
            } else {
                parent.setting.getLocation().setPInfo(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
            }

            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent e) {
            setLocation(new Point(x, y));
        }
    };

//    @Override
//    protected void WndProc(Message m) {
//        if (parent != null) {
//            parent.windowsMessage(m);
//        }
//
//        super.WndProc(m);
//    }

    private void timer_Tick(ActionEvent ev) {
        if (lyrics == null || lyrics.size() < 1) return;

        long cnt = Audio.getInstance().getDriverCounter();

        try {
            if (cnt >= lyrics.get(lyricsIndex).getItem1()) {

                 // lblLyrics.setText(lyrics[lyricsIndex].getItem3());
                rtbLyrics.setText(null);

                int ind = 0;
                rtbLyrics.setSelectionColor(culColor);
                while (ind < lyrics.get(lyricsIndex).getItem3().length()) {
                    char c = lyrics.get(lyricsIndex).getItem3().charAt(ind);
                    if (c == '\\') {
                        ind++;
                        c = lyrics.get(lyricsIndex).getItem3().charAt(ind);
                        switch (c) {
                        case '"':
                        case '\\':
                            break;
                        case 'c':
                            ind++;
                            String n = String.valueOf(lyrics.get(lyricsIndex).getItem3().charAt(ind++));
                            int r, g, b;
                            if (n.equals("s")) {
                                r = 192;
                                g = 192;
                                b = 255;  // 192,192,255 system color
                            } else {
                                n += lyrics.get(lyricsIndex).getItem3().charAt(ind++);
                                r = Integer.parseInt(n, 16);
                                n = String.valueOf(lyrics.get(lyricsIndex).getItem3().charAt(ind++));
                                n += lyrics.get(lyricsIndex).getItem3().charAt(ind++);
                                g = Integer.parseInt(n, 16);
                                n = String.valueOf(lyrics.get(lyricsIndex).getItem3().charAt(ind++));
                                n += lyrics.get(lyricsIndex).getItem3().charAt(ind++);
                                b = Integer.parseInt(n, 16);
                            }
                            culColor = new Color(r, g, b);
                            rtbLyrics.setSelectionColor(culColor);
                            continue;
                        }
                    }
                    rtbLyrics.setText(String.valueOf(c));
                    ind++;
                }

                lyricsIndex++;

                if (lyricsIndex == lyrics.size()) {
                    timer.stop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                rtbLyrics.setText(null);
                rtbLyrics.setText("LYLIC PARSE ERROR");
            } catch (Exception ex) {
                e.printStackTrace();
            }
        }
    }

    private void initializeComponent() {
        this.lblTitle = new JLabel();
        this.lblTitleJ = new JLabel();
        this.lblGame = new JLabel();
        this.lblGameJ = new JLabel();
        this.lblSystem = new JLabel();
        this.lblComposer = new JLabel();
        this.lblRelease = new JLabel();
        this.lblVersion = new JLabel();
        this.lblVGMBy = new JLabel();
        this.lblNotes = new JLabel();
        this.lblUsedChips = new JLabel();
        this.lblSystemJ = new JLabel();
        this.lblComposerJ = new JLabel();
        this.timer = new Timer(10, this::timer_Tick);
        this.rtbLyrics = new JTextField();

        //
        // lblTitle
        //
        this.lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblTitle.setVerticalAlignment(SwingConstants.TOP);
        this.lblTitle.setBackground(Color.black);
        this.lblTitle.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblTitle.setForeground(new Color(192, 192, 255));
        this.lblTitle.setLocation(new Point(40, 0));
        this.lblTitle.setName("lblTitle");
        this.lblTitle.setPreferredSize(new Dimension(284, 16));
        // this.lblTitle.TabIndex = 2
        this.lblTitle.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblTitleJ
        //
        this.lblTitleJ.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblTitleJ.setVerticalAlignment(SwingConstants.TOP);
        this.lblTitleJ.setBackground(Color.black);
        this.lblTitleJ.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblTitleJ.setForeground(new Color(192, 192, 255));
        this.lblTitleJ.setLocation(new Point(40, 16));
        this.lblTitleJ.setName("lblTitleJ");
        this.lblTitleJ.setPreferredSize(new Dimension(284, 16));
        // this.lblTitleJ.TabIndex = 3
        this.lblTitleJ.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblGame
        //
        this.lblGame.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblGame.setVerticalAlignment(SwingConstants.TOP);
        this.lblGame.setBackground(Color.black);
        this.lblGame.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblGame.setForeground(new Color(192, 192, 255));
        this.lblGame.setLocation(new Point(40, 32));
        this.lblGame.setName("lblGame");
        this.lblGame.setPreferredSize(new Dimension(284, 16));
        // this.lblGame.TabIndex = 4
        this.lblGame.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblGameJ
        //
        this.lblGameJ.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblGameJ.setVerticalAlignment(SwingConstants.TOP);
        this.lblGameJ.setBackground(Color.black);
        this.lblGameJ.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblGameJ.setForeground(new Color(192, 192, 255));
        this.lblGameJ.setLocation(new Point(40, 48));
        this.lblGameJ.setName("lblGameJ");
        this.lblGameJ.setPreferredSize(new Dimension(284, 16));
        // this.lblGameJ.TabIndex = 5
        this.lblGameJ.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblSystem
        //
        this.lblSystem.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblSystem.setVerticalAlignment(SwingConstants.TOP);
        this.lblSystem.setBackground(Color.black);
        this.lblSystem.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblSystem.setForeground(new Color(192, 192, 255));
        this.lblSystem.setLocation(new Point(40, 64));
        this.lblSystem.setName("lblSystem");
        this.lblSystem.setPreferredSize(new Dimension(284, 16));
        // this.lblSystem.TabIndex = 6
        this.lblSystem.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblComposer
        //
        this.lblComposer.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblComposer.setVerticalAlignment(SwingConstants.TOP);
        this.lblComposer.setBackground(Color.black);
        this.lblComposer.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblComposer.setForeground(new Color(192, 192, 255));
        this.lblComposer.setLocation(new Point(40, 96));
        this.lblComposer.setName("lblComposer");
        this.lblComposer.setPreferredSize(new Dimension(284, 16));
        // this.lblComposer.TabIndex = 7
        this.lblComposer.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblRelease
        //
        this.lblRelease.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblRelease.setVerticalAlignment(SwingConstants.TOP);
        this.lblRelease.setBackground(Color.black);
        this.lblRelease.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblRelease.setForeground(new Color(192, 192, 255));
        this.lblRelease.setLocation(new Point(40, 128));
        this.lblRelease.setName("lblRelease");
        this.lblRelease.setPreferredSize(new Dimension(284, 16));
        // this.lblRelease.TabIndex = 8
        this.lblRelease.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblVersion
        //
        this.lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblVersion.setVerticalAlignment(SwingConstants.TOP);
        this.lblVersion.setBackground(Color.black);
        this.lblVersion.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblVersion.setForeground(new Color(192, 192, 255));
        this.lblVersion.setLocation(new Point(40, 144));
        this.lblVersion.setName("lblVersion");
        this.lblVersion.setPreferredSize(new Dimension(284, 16));
        // this.lblVersion.TabIndex = 9
        this.lblVersion.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblVGMBy
        //
        this.lblVGMBy.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblVGMBy.setVerticalAlignment(SwingConstants.TOP);
        this.lblVGMBy.setBackground(Color.black);
        this.lblVGMBy.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblVGMBy.setForeground(new Color(192, 192, 255));
        this.lblVGMBy.setLocation(new Point(40, 160));
        this.lblVGMBy.setName("lblVGMBy");
        this.lblVGMBy.setPreferredSize(new Dimension(284, 16));
        // this.lblVGMBy.TabIndex = 10
        this.lblVGMBy.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblNotes
        //
        this.lblNotes.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblNotes.setVerticalAlignment(SwingConstants.TOP);
        this.lblNotes.setBackground(Color.black);
        this.lblNotes.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblNotes.setForeground(new Color(192, 192, 255));
        this.lblNotes.setLocation(new Point(40, 176));
        this.lblNotes.setName("lblNotes");
        this.lblNotes.setPreferredSize(new Dimension(284, 16));
        // this.lblNotes.TabIndex = 11
        this.lblNotes.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblUsedChips
        //
        this.lblUsedChips.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblUsedChips.setVerticalAlignment(SwingConstants.TOP);
        this.lblUsedChips.setBackground(Color.black);
        this.lblUsedChips.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblUsedChips.setForeground(new Color(192, 192, 255));
        this.lblUsedChips.setLocation(new Point(40, 192));
        this.lblUsedChips.setName("lblUsedChips");
        this.lblUsedChips.setPreferredSize(new Dimension(284, 16));
        // this.lblUsedChips.TabIndex = 12
        this.lblUsedChips.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblSystemJ
        //
        this.lblSystemJ.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblSystemJ.setVerticalAlignment(SwingConstants.TOP);
        this.lblSystemJ.setBackground(Color.black);
        this.lblSystemJ.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblSystemJ.setForeground(new Color(192, 192, 255));
        this.lblSystemJ.setLocation(new Point(40, 80));
        this.lblSystemJ.setName("lblSystemJ");
        this.lblSystemJ.setPreferredSize(new Dimension(284, 16));
        // this.lblSystemJ.TabIndex = 13
        this.lblSystemJ.setText("01234567890123456789012345678901234567890123456789");
        //
        // lblComposerJ
        //
        this.lblComposerJ.setHorizontalAlignment(SwingConstants.CENTER);
        this.lblComposerJ.setVerticalAlignment(SwingConstants.TOP);
        this.lblComposerJ.setBackground(Color.black);
        this.lblComposerJ.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.lblComposerJ.setForeground(new Color(192, 192, 255));
        this.lblComposerJ.setLocation(new Point(40, 112));
        this.lblComposerJ.setName("lblComposerJ");
        this.lblComposerJ.setPreferredSize(new Dimension(284, 16));
        // this.lblComposerJ.TabIndex = 14
        this.lblComposerJ.setText("01234567890123456789012345678901234567890123456789");
        //
        // timer
        //
//        this.timer.Interval = 10;
//        this.timer.Tick += new System.EventHandler(this.timer_Tick);
        this.timer.start();
        //
        // rtbLyrics
        //
//            this.rtbLyrics.Anchor = ((JAnchorStyles)(((JAnchorStyles.Top | JAnchorStyles.Left) | JAnchorStyles.Right)));
        this.rtbLyrics.setBackground(Color.black);
        this.rtbLyrics.setBorder(null);
//        this.rtbLyrics.Cursor = Cursor.Arrow;
//        this.rtbLyrics.DetectUrls = false;
        this.rtbLyrics.setFont(new Font("メイリオ", Font.BOLD, 9));
        this.rtbLyrics.setForeground(new Color(192, 192, 255));
        this.rtbLyrics.setLocation(new Point(41, 209));
//        this.rtbLyrics.Multiline = false;
        this.rtbLyrics.setName("rtbLyrics");
        this.rtbLyrics.setEditable(false);
//        this.rtbLyrics.ScrollBars = JTextAreaScrollBars.None;
//        this.rtbLyrics.ShortcutsEnabled = false;
        this.rtbLyrics.setPreferredSize(new Dimension(283, 20));
        // this.rtbLyrics.TabIndex = 16
        // this.rtbLyrics.TabStop = false;
        this.rtbLyrics.setText("");
        //
        // frmInfo
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setBackground(Color.black);
        this.image = mdplayer.properties.Resources.getPlaneB();
//        this.BackgroundImageLayout = JImageLayout.None;
        this.setPreferredSize(new Dimension(324, 229));
        this.getContentPane().add(this.rtbLyrics);
        this.getContentPane().add(this.lblComposerJ);
        this.getContentPane().add(this.lblSystemJ);
        this.getContentPane().add(this.lblUsedChips);
        this.getContentPane().add(this.lblNotes);
        this.getContentPane().add(this.lblVGMBy);
        this.getContentPane().add(this.lblVersion);
        this.getContentPane().add(this.lblRelease);
        this.getContentPane().add(this.lblComposer);
        this.getContentPane().add(this.lblSystem);
        this.getContentPane().add(this.lblGameJ);
        this.getContentPane().add(this.lblGame);
        this.getContentPane().add(this.lblTitleJ);
        this.getContentPane().add(this.lblTitle);
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
        this.setMaximumSize(new Dimension(800, 268));
        this.setMinimumSize(new Dimension(252, 268));
        this.setName("frmInfo");
        this.setTitle("Information");
        this.addWindowListener(this.windowListener);
    }

    BufferedImage image;
    private JLabel lblTitle;
    private JLabel lblTitleJ;
    private JLabel lblGame;
    private JLabel lblGameJ;
    private JLabel lblSystem;
    private JLabel lblComposer;
    private JLabel lblRelease;
    private JLabel lblVersion;
    private JLabel lblVGMBy;
    private JLabel lblNotes;
    private JLabel lblUsedChips;
    private JLabel lblSystemJ;
    private JLabel lblComposerJ;
    private Timer timer;
    private JTextField rtbLyrics;
}
