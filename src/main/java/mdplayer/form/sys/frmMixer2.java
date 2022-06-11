package mdplayer.form.sys;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileFilter;

import dotnet4j.io.Path;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.Log;
import mdplayer.MDChipParams;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.form.kb.wf.frmHuC6280;
import mdplayer.properties.Resources;


public class frmMixer2 extends JFrame {
    public Boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public frmMain parent;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int zoom = 1;
    private int chipn = -1;

    private MDChipParams.Mixer newParam;
    private MDChipParams.Mixer oldParam = new MDChipParams.Mixer();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmHuC6280.class);

    public frmMixer2(frmMain frm, int zoom, MDChipParams.Mixer newParam) {
        parent = frm;
        this.zoom = zoom;

        initializeComponent();
        pbScreen.addMouseWheelListener(this.pbScreen_MouseWheel);

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getplaneMixer(), null, zoom);
        DrawBuff.screenInitMixer(frameBuffer);
        update();
    }

    private MouseWheelListener pbScreen_MouseWheel = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            chipn = px / 20 + (py / 72) * 16;
            int delta = (int) Math.signum(ev.getWheelRotation());

            if (chipn < 0 || chipn >= setVolume.length) return;
            setVolume[chipn].accept(false, delta);
        }
    };

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected Boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().setPosMixer(getLocation());
            } else {
                parent.setting.getLocation().setPosMixer(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
            }
            isClosed = true;
        }

        public void windowOpened(WindowEvent ev) {
            setLocation(new Point(x, y));

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getplaneMixer().getWidth() * zoom, frameSizeH + Resources.getplaneMixer().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getplaneMixer().getWidth() * zoom, frameSizeH + Resources.getplaneMixer().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getplaneMixer().getWidth() * zoom, frameSizeH + Resources.getplaneMixer().getHeight() * zoom));
        componentListener.componentResized(null);
    }

    private ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentMoved(ComponentEvent e) {
            prefs.putInt("x", e.getComponent().getX());
            prefs.putInt("y", e.getComponent().getY());
        }
        @Override
        public void componentResized(ComponentEvent e) {
        }
    };

//    @Override
//    protected void WndProc(Message m) {
//        if (parent != null) {
//            parent.windowsMessage(m);
//        }
//
//        try {
//            super.WndProc(m);
//        } catch (Exception ex) {
//            Log.forcedWrite(ex);
//        }
//    }

    public void screenChangeParams() {

        newParam.Master.Volume = parent.setting.getbalance().getMasterVolume();
        newParam.YM2151.Volume = parent.setting.getbalance().getYM2151Volume();
        newParam.YM2203.Volume = parent.setting.getbalance().getYM2203Volume();
        newParam.YM2203FM.Volume = parent.setting.getbalance().getYM2203FMVolume();
        newParam.YM2203PSG.Volume = parent.setting.getbalance().getYM2203PSGVolume();
        newParam.YM2612.Volume = parent.setting.getbalance().getYM2612Volume();
        newParam.YM2608.Volume = parent.setting.getbalance().getYM2608Volume();
        newParam.YM2608FM.Volume = parent.setting.getbalance().getYM2608FMVolume();
        newParam.YM2608PSG.Volume = parent.setting.getbalance().getYM2608PSGVolume();
        newParam.YM2608Rhythm.Volume = parent.setting.getbalance().getYM2608RhythmVolume();
        newParam.YM2608Adpcm.Volume = parent.setting.getbalance().getYM2608AdpcmVolume();
        newParam.YM2610.Volume = parent.setting.getbalance().getYM2610Volume();
        newParam.YM2610FM.Volume = parent.setting.getbalance().getYM2610FMVolume();
        newParam.YM2610PSG.Volume = parent.setting.getbalance().getYM2610PSGVolume();
        newParam.YM2610AdpcmA.Volume = parent.setting.getbalance().getYM2610AdpcmAVolume();
        newParam.YM2610AdpcmB.Volume = parent.setting.getbalance().getYM2610AdpcmBVolume();

        newParam.YM2413.Volume = parent.setting.getbalance().getYM2413Volume();
        newParam.YM3526.Volume = parent.setting.getbalance().getYM3526Volume();
        newParam.Y8950.Volume = parent.setting.getbalance().getY8950Volume();
        newParam.YM3812.Volume = parent.setting.getbalance().getYM3812Volume();
        newParam.YMF262.Volume = parent.setting.getbalance().getYMF262Volume();
        newParam.YMF278B.Volume = parent.setting.getbalance().getYMF278BVolume();
        newParam.YMZ280B.Volume = parent.setting.getbalance().getYMZ280BVolume();
        newParam.YMF271.Volume = parent.setting.getbalance().getYMF271Volume();
        newParam.AY8910.Volume = parent.setting.getbalance().getAY8910Volume();
        newParam.SN76489.Volume = parent.setting.getbalance().getSN76489Volume();
        newParam.HuC6280.Volume = parent.setting.getbalance().getHuC6280Volume();

        newParam.RF5C164.Volume = parent.setting.getbalance().getRF5C164Volume();
        newParam.RF5C68.Volume = parent.setting.getbalance().getRF5C68Volume();
        newParam.PWM.Volume = parent.setting.getbalance().getPWMVolume();
        newParam.OKIM6258.Volume = parent.setting.getbalance().getOKIM6258Volume();
        newParam.OKIM6295.Volume = parent.setting.getbalance().getOKIM6295Volume();
        newParam.C140.Volume = parent.setting.getbalance().getC140Volume();
        newParam.C352.Volume = parent.setting.getbalance().getC352Volume();
        newParam.SAA1099.Volume = parent.setting.getbalance().getSAA1099Volume();
        newParam.PPZ8.Volume = parent.setting.getbalance().getPPZ8Volume();
        newParam.SEGAPCM.Volume = parent.setting.getbalance().getSEGAPCMVolume();
        newParam.MultiPCM.Volume = parent.setting.getbalance().getMultiPCMVolume();
        newParam.K051649.Volume = parent.setting.getbalance().getK051649Volume();
        newParam.K053260.Volume = parent.setting.getbalance().getK053260Volume();
        newParam.K054539.Volume = parent.setting.getbalance().getK054539Volume();
        newParam.QSound.Volume = parent.setting.getbalance().getQSoundVolume();
        newParam.GA20.Volume = parent.setting.getbalance().getGA20Volume();

        newParam.APU.Volume = parent.setting.getbalance().getAPUVolume();
        newParam.DMC.Volume = parent.setting.getbalance().getDMCVolume();
        newParam.FDS.Volume = parent.setting.getbalance().getFDSVolume();
        newParam.MMC5.Volume = parent.setting.getbalance().getMMC5Volume();
        newParam.N160.Volume = parent.setting.getbalance().getN160Volume();
        newParam.VRC6.Volume = parent.setting.getbalance().getVRC6Volume();
        newParam.VRC7.Volume = parent.setting.getbalance().getVRC7Volume();
        newParam.FME7.Volume = parent.setting.getbalance().getFME7Volume();
        newParam.DMG.Volume = parent.setting.getbalance().getDMGVolume();

        newParam.GimicOPN.Volume = parent.setting.getbalance().getGimicOPNVolume();
        newParam.GimicOPNA.Volume = parent.setting.getbalance().getGimicOPNAVolume();


        newParam.Master.VisVolume1 = Common.Range(Audio.visVolume.master / 250, 0, 44);
        if (newParam.Master.VisVolume2 <= newParam.Master.VisVolume1) {
            newParam.Master.VisVolume2 = newParam.Master.VisVolume1;
            newParam.Master.VisVol2Cnt = 30;
        }

        newParam.YM2151.VisVolume1 = Common.Range(Audio.visVolume.ym2151 / 200, 0, 44);
        if (newParam.YM2151.VisVolume2 <= newParam.YM2151.VisVolume1) {
            newParam.YM2151.VisVolume2 = newParam.YM2151.VisVolume1;
            newParam.YM2151.VisVol2Cnt = 30;
        }

        newParam.YM2203.VisVolume1 = Common.Range(Audio.visVolume.ym2203 / 200, 0, 44);
        if (newParam.YM2203.VisVolume2 <= newParam.YM2203.VisVolume1) {
            newParam.YM2203.VisVolume2 = newParam.YM2203.VisVolume1;
            newParam.YM2203.VisVol2Cnt = 30;
        }

        newParam.YM2203FM.VisVolume1 = Common.Range(Audio.visVolume.ym2203FM / 200, 0, 44);
        if (newParam.YM2203FM.VisVolume2 <= newParam.YM2203FM.VisVolume1) {
            newParam.YM2203FM.VisVolume2 = newParam.YM2203FM.VisVolume1;
            newParam.YM2203FM.VisVol2Cnt = 30;
        }

        newParam.YM2203PSG.VisVolume1 = Common.Range(Audio.visVolume.ym2203SSG / 120, 0, 44);
        if (newParam.YM2203PSG.VisVolume2 <= newParam.YM2203PSG.VisVolume1) {
            newParam.YM2203PSG.VisVolume2 = newParam.YM2203PSG.VisVolume1;
            newParam.YM2203PSG.VisVol2Cnt = 30;
        }

        newParam.YM2612.VisVolume1 = Common.Range(Audio.visVolume.ym2612 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2612.VisVolume2 <= newParam.YM2612.VisVolume1) {
            newParam.YM2612.VisVolume2 = newParam.YM2612.VisVolume1;
            newParam.YM2612.VisVol2Cnt = 30;
        }

        newParam.YM2608.VisVolume1 = Common.Range(Audio.visVolume.ym2608 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2608.VisVolume2 <= newParam.YM2608.VisVolume1) {
            newParam.YM2608.VisVolume2 = newParam.YM2608.VisVolume1;
            newParam.YM2608.VisVol2Cnt = 30;
        }

        newParam.YM2608FM.VisVolume1 = Common.Range(Audio.visVolume.ym2608FM / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2608FM.VisVolume2 <= newParam.YM2608FM.VisVolume1) {
            newParam.YM2608FM.VisVolume2 = newParam.YM2608FM.VisVolume1;
            newParam.YM2608FM.VisVol2Cnt = 30;
        }

        newParam.YM2608PSG.VisVolume1 = Common.Range(Audio.visVolume.ym2608SSG / 120, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2608PSG.VisVolume2 <= newParam.YM2608PSG.VisVolume1) {
            newParam.YM2608PSG.VisVolume2 = newParam.YM2608PSG.VisVolume1;
            newParam.YM2608PSG.VisVol2Cnt = 30;
        }

        newParam.YM2608Rhythm.VisVolume1 = Common.Range(Audio.visVolume.ym2608Rtm / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2608Rhythm.VisVolume2 <= newParam.YM2608Rhythm.VisVolume1) {
            newParam.YM2608Rhythm.VisVolume2 = newParam.YM2608Rhythm.VisVolume1;
            newParam.YM2608Rhythm.VisVol2Cnt = 30;
        }

        newParam.YM2608Adpcm.VisVolume1 = Common.Range(Audio.visVolume.ym2608APCM / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2608Adpcm.VisVolume2 <= newParam.YM2608Adpcm.VisVolume1) {
            newParam.YM2608Adpcm.VisVolume2 = newParam.YM2608Adpcm.VisVolume1;
            newParam.YM2608Adpcm.VisVol2Cnt = 30;
        }

        newParam.YM2610.VisVolume1 = Common.Range(Audio.visVolume.ym2610 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2610.VisVolume2 <= newParam.YM2610.VisVolume1) {
            newParam.YM2610.VisVolume2 = newParam.YM2610.VisVolume1;
            newParam.YM2610.VisVol2Cnt = 30;
        }

        newParam.YM2610FM.VisVolume1 = Common.Range(Audio.visVolume.ym2610FM / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2610FM.VisVolume2 <= newParam.YM2610FM.VisVolume1) {
            newParam.YM2610FM.VisVolume2 = newParam.YM2610FM.VisVolume1;
            newParam.YM2610FM.VisVol2Cnt = 30;
        }

        newParam.YM2610PSG.VisVolume1 = Common.Range(Audio.visVolume.ym2610SSG / 120, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2610PSG.VisVolume2 <= newParam.YM2610PSG.VisVolume1) {
            newParam.YM2610PSG.VisVolume2 = newParam.YM2610PSG.VisVolume1;
            newParam.YM2610PSG.VisVol2Cnt = 30;
        }

        newParam.YM2610AdpcmA.VisVolume1 = Common.Range(Audio.visVolume.ym2610APCMA / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2610AdpcmA.VisVolume2 <= newParam.YM2610AdpcmA.VisVolume1) {
            newParam.YM2610AdpcmA.VisVolume2 = newParam.YM2610AdpcmA.VisVolume1;
            newParam.YM2610AdpcmA.VisVol2Cnt = 30;
        }

        newParam.YM2610AdpcmB.VisVolume1 = Common.Range(Audio.visVolume.ym2610APCMB / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.YM2610AdpcmB.VisVolume2 <= newParam.YM2610AdpcmB.VisVolume1) {
            newParam.YM2610AdpcmB.VisVolume2 = newParam.YM2610AdpcmB.VisVolume1;
            newParam.YM2610AdpcmB.VisVol2Cnt = 30;
        }


        newParam.YM2413.VisVolume1 = Common.Range(Audio.visVolume.ym2413 / 200, 0, 44);
        if (newParam.YM2413.VisVolume2 <= newParam.YM2413.VisVolume1) {
            newParam.YM2413.VisVolume2 = newParam.YM2413.VisVolume1;
            newParam.YM2413.VisVol2Cnt = 30;
        }

        newParam.YM3526.VisVolume1 = Common.Range(Audio.visVolume.ym3526 / 200, 0, 44);
        if (newParam.YM3526.VisVolume2 <= newParam.YM3526.VisVolume1) {
            newParam.YM3526.VisVolume2 = newParam.YM3526.VisVolume1;
            newParam.YM3526.VisVol2Cnt = 30;
        }

        newParam.Y8950.VisVolume1 = Common.Range(Audio.visVolume.y8950 / 200, 0, 44);
        if (newParam.Y8950.VisVolume2 <= newParam.Y8950.VisVolume1) {
            newParam.Y8950.VisVolume2 = newParam.Y8950.VisVolume1;
            newParam.Y8950.VisVol2Cnt = 30;
        }

        newParam.YM3812.VisVolume1 = Common.Range(Audio.visVolume.ym3812 / 200, 0, 44);
        if (newParam.YM3812.VisVolume2 <= newParam.YM3812.VisVolume1) {
            newParam.YM3812.VisVolume2 = newParam.YM3812.VisVolume1;
            newParam.YM3812.VisVol2Cnt = 30;
        }

        newParam.YMF262.VisVolume1 = Common.Range(Audio.visVolume.ymf262 / 200, 0, 44);
        if (newParam.YMF262.VisVolume2 <= newParam.YMF262.VisVolume1) {
            newParam.YMF262.VisVolume2 = newParam.YMF262.VisVolume1;
            newParam.YMF262.VisVol2Cnt = 30;
        }

        newParam.YMF278B.VisVolume1 = Common.Range(Audio.visVolume.ymf278b / 200, 0, 44);
        if (newParam.YMF278B.VisVolume2 <= newParam.YMF278B.VisVolume1) {
            newParam.YMF278B.VisVolume2 = newParam.YMF278B.VisVolume1;
            newParam.YMF278B.VisVol2Cnt = 30;
        }

        newParam.YMZ280B.VisVolume1 = Common.Range(Audio.visVolume.ymz280b / 200, 0, 44);
        if (newParam.YMZ280B.VisVolume2 <= newParam.YMZ280B.VisVolume1) {
            newParam.YMZ280B.VisVolume2 = newParam.YMZ280B.VisVolume1;
            newParam.YMZ280B.VisVol2Cnt = 30;
        }

        newParam.YMF271.VisVolume1 = Common.Range(Audio.visVolume.ymf271 / 200, 0, 44);
        if (newParam.YMF271.VisVolume2 <= newParam.YMF271.VisVolume1) {
            newParam.YMF271.VisVolume2 = newParam.YMF271.VisVolume1;
            newParam.YMF271.VisVol2Cnt = 30;
        }

        newParam.AY8910.VisVolume1 = Common.Range(Audio.visVolume.ay8910 / 120, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.AY8910.VisVolume2 <= newParam.AY8910.VisVolume1) {
            newParam.AY8910.VisVolume2 = newParam.AY8910.VisVolume1;
            newParam.AY8910.VisVol2Cnt = 30;
        }

        newParam.SN76489.VisVolume1 = Common.Range(Audio.visVolume.sn76489 / 120, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.SN76489.VisVolume2 <= newParam.SN76489.VisVolume1) {
            newParam.SN76489.VisVolume2 = newParam.SN76489.VisVolume1;
            newParam.SN76489.VisVol2Cnt = 30;
        }

        newParam.HuC6280.VisVolume1 = Common.Range(Audio.visVolume.huc6280 / 120, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.HuC6280.VisVolume2 <= newParam.HuC6280.VisVolume1) {
            newParam.HuC6280.VisVolume2 = newParam.HuC6280.VisVolume1;
            newParam.HuC6280.VisVol2Cnt = 30;
        }


        newParam.RF5C164.VisVolume1 = Common.Range(Audio.visVolume.rf5c164 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.RF5C164.VisVolume2 <= newParam.RF5C164.VisVolume1) {
            newParam.RF5C164.VisVolume2 = newParam.RF5C164.VisVolume1;
            newParam.RF5C164.VisVol2Cnt = 30;
        }

        newParam.RF5C68.VisVolume1 = Common.Range(Audio.visVolume.rf5c68 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.RF5C68.VisVolume2 <= newParam.RF5C68.VisVolume1) {
            newParam.RF5C68.VisVolume2 = newParam.RF5C68.VisVolume1;
            newParam.RF5C68.VisVol2Cnt = 30;
        }

        newParam.PWM.VisVolume1 = Common.Range(Audio.visVolume.pwm / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.PWM.VisVolume2 <= newParam.PWM.VisVolume1) {
            newParam.PWM.VisVolume2 = newParam.PWM.VisVolume1;
            newParam.PWM.VisVol2Cnt = 30;
        }

        newParam.OKIM6258.VisVolume1 = Common.Range(Audio.visVolume.okim6258 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.OKIM6258.VisVolume2 <= newParam.OKIM6258.VisVolume1) {
            newParam.OKIM6258.VisVolume2 = newParam.OKIM6258.VisVolume1;
            newParam.OKIM6258.VisVol2Cnt = 30;
        }

        newParam.OKIM6295.VisVolume1 = Common.Range(Audio.visVolume.okim6295 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.OKIM6295.VisVolume2 <= newParam.OKIM6295.VisVolume1) {
            newParam.OKIM6295.VisVolume2 = newParam.OKIM6295.VisVolume1;
            newParam.OKIM6295.VisVol2Cnt = 30;
        }

        newParam.C140.VisVolume1 = Common.Range(Audio.visVolume.c140 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.C140.VisVolume2 <= newParam.C140.VisVolume1) {
            newParam.C140.VisVolume2 = newParam.C140.VisVolume1;
            newParam.C140.VisVol2Cnt = 30;
        }

        newParam.C352.VisVolume1 = Common.Range(Audio.visVolume.c352 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.C352.VisVolume2 <= newParam.C352.VisVolume1) {
            newParam.C352.VisVolume2 = newParam.C352.VisVolume1;
            newParam.C352.VisVol2Cnt = 30;
        }

        newParam.SAA1099.VisVolume1 = Common.Range(Audio.visVolume.saa1099 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.SAA1099.VisVolume2 <= newParam.SAA1099.VisVolume1) {
            newParam.SAA1099.VisVolume2 = newParam.SAA1099.VisVolume1;
            newParam.SAA1099.VisVol2Cnt = 30;
        }

        newParam.PPZ8.VisVolume1 = Common.Range(Audio.visVolume.ppz8 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.PPZ8.VisVolume2 <= newParam.PPZ8.VisVolume1) {
            newParam.PPZ8.VisVolume2 = newParam.PPZ8.VisVolume1;
            newParam.PPZ8.VisVol2Cnt = 30;
        }

        newParam.SEGAPCM.VisVolume1 = Common.Range(Audio.visVolume.segaPCM / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.SEGAPCM.VisVolume2 <= newParam.SEGAPCM.VisVolume1) {
            newParam.SEGAPCM.VisVolume2 = newParam.SEGAPCM.VisVolume1;
            newParam.SEGAPCM.VisVol2Cnt = 30;
        }

        newParam.MultiPCM.VisVolume1 = Common.Range(Audio.visVolume.multiPCM / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.MultiPCM.VisVolume2 <= newParam.MultiPCM.VisVolume1) {
            newParam.MultiPCM.VisVolume2 = newParam.MultiPCM.VisVolume1;
            newParam.MultiPCM.VisVol2Cnt = 30;
        }

        newParam.K051649.VisVolume1 = Common.Range(Audio.visVolume.k051649 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.K051649.VisVolume2 <= newParam.K051649.VisVolume1) {
            newParam.K051649.VisVolume2 = newParam.K051649.VisVolume1;
            newParam.K051649.VisVol2Cnt = 30;
        }

        newParam.K053260.VisVolume1 = Common.Range(Audio.visVolume.k053260 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.K053260.VisVolume2 <= newParam.K053260.VisVolume1) {
            newParam.K053260.VisVolume2 = newParam.K053260.VisVolume1;
            newParam.K053260.VisVol2Cnt = 30;
        }

        newParam.K054539.VisVolume1 = Common.Range(Audio.visVolume.k054539 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.K054539.VisVolume2 <= newParam.K054539.VisVolume1) {
            newParam.K054539.VisVolume2 = newParam.K054539.VisVolume1;
            newParam.K054539.VisVol2Cnt = 30;
        }

        newParam.QSound.VisVolume1 = Common.Range(Audio.visVolume.qSound / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.QSound.VisVolume2 <= newParam.QSound.VisVolume1) {
            newParam.QSound.VisVolume2 = newParam.QSound.VisVolume1;
            newParam.QSound.VisVol2Cnt = 30;
        }

        newParam.GA20.VisVolume1 = Common.Range(Audio.visVolume.ga20 / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.GA20.VisVolume2 <= newParam.GA20.VisVolume1) {
            newParam.GA20.VisVolume2 = newParam.GA20.VisVolume1;
            newParam.GA20.VisVol2Cnt = 30;
        }

        newParam.APU.VisVolume1 = Common.Range(Audio.visVolume.APU / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.APU.VisVolume2 <= newParam.APU.VisVolume1) {
            newParam.APU.VisVolume2 = newParam.APU.VisVolume1;
            newParam.APU.VisVol2Cnt = 30;
        }

        newParam.DMC.VisVolume1 = Common.Range(Audio.visVolume.DMC / 350, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.DMC.VisVolume2 <= newParam.DMC.VisVolume1) {
            newParam.DMC.VisVolume2 = newParam.DMC.VisVolume1;
            newParam.DMC.VisVol2Cnt = 30;
        }

        newParam.FDS.VisVolume1 = Common.Range(Audio.visVolume.FDS / 200, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.FDS.VisVolume2 <= newParam.FDS.VisVolume1) {
            newParam.FDS.VisVolume2 = newParam.FDS.VisVolume1;
            newParam.FDS.VisVol2Cnt = 30;
        }

        newParam.MMC5.VisVolume1 = Common.Range(Audio.visVolume.MMC5 / 50, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.MMC5.VisVolume2 <= newParam.K054539.VisVolume1) {
            newParam.MMC5.VisVolume2 = newParam.MMC5.VisVolume1;
            newParam.MMC5.VisVol2Cnt = 30;
        }

        newParam.N160.VisVolume1 = Common.Range(Audio.visVolume.N160 / 50, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.N160.VisVolume2 <= newParam.N160.VisVolume1) {
            newParam.N160.VisVolume2 = newParam.N160.VisVolume1;
            newParam.N160.VisVol2Cnt = 30;
        }
        newParam.VRC6.VisVolume1 = Common.Range(Audio.visVolume.VRC6 / 50, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.VRC6.VisVolume2 <= newParam.VRC6.VisVolume1) {
            newParam.VRC6.VisVolume2 = newParam.VRC6.VisVolume1;
            newParam.VRC6.VisVol2Cnt = 30;
        }

        newParam.VRC7.VisVolume1 = Common.Range(Audio.visVolume.VRC7 / 50, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.VRC7.VisVolume2 <= newParam.VRC7.VisVolume1) {
            newParam.VRC7.VisVolume2 = newParam.VRC7.VisVolume1;
            newParam.VRC7.VisVol2Cnt = 30;
        }

        newParam.FME7.VisVolume1 = Common.Range(Audio.visVolume.FME7 / 50, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.FME7.VisVolume2 <= newParam.FME7.VisVolume1) {
            newParam.FME7.VisVolume2 = newParam.FME7.VisVolume1;
            newParam.FME7.VisVol2Cnt = 30;
        }

        newParam.DMG.VisVolume1 = Common.Range(Audio.visVolume.DMG / 50, 0, 44);//(Short.MAX_VALUE / 44);
        if (newParam.DMG.VisVolume2 <= newParam.DMG.VisVolume1) {
            newParam.DMG.VisVolume2 = newParam.DMG.VisVolume1;
            newParam.DMG.VisVol2Cnt = 30;
        }
    }

    public void screenDrawParams() {
        int num;
        MDChipParams.Mixer.VolumeInfo oVI, nVI;

        num = 0;
        oVI = oldParam.Master;
        nVI = newParam.Master;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2151;
        nVI = newParam.YM2151;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2203;
        nVI = newParam.YM2203;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2203FM;
        nVI = newParam.YM2203FM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2203PSG;
        nVI = newParam.YM2203PSG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2612;
        nVI = newParam.YM2612;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2608;
        nVI = newParam.YM2608;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2608FM;
        nVI = newParam.YM2608FM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2608PSG;
        nVI = newParam.YM2608PSG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2608Rhythm;
        nVI = newParam.YM2608Rhythm;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2608Adpcm;
        nVI = newParam.YM2608Adpcm;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2610;
        nVI = newParam.YM2610;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2610FM;
        nVI = newParam.YM2610FM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2610PSG;
        nVI = newParam.YM2610PSG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2610AdpcmA;
        nVI = newParam.YM2610AdpcmA;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM2610AdpcmB;
        nVI = newParam.YM2610AdpcmB;
        drawVolAndFader(num, oVI, nVI);

        num++;
        oVI = oldParam.YM2413;
        nVI = newParam.YM2413;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM3526;
        nVI = newParam.YM3526;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.Y8950;
        nVI = newParam.Y8950;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YM3812;
        nVI = newParam.YM3812;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YMF262;
        nVI = newParam.YMF262;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YMF278B;
        nVI = newParam.YMF278B;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YMZ280B;
        nVI = newParam.YMZ280B;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.YMF271;
        nVI = newParam.YMF271;
        drawVolAndFader(num, oVI, nVI);
        num++;
        num++;
        oVI = oldParam.AY8910;
        nVI = newParam.AY8910;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.SN76489;
        nVI = newParam.SN76489;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.HuC6280;
        nVI = newParam.HuC6280;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.SAA1099;
        nVI = newParam.SAA1099;
        drawVolAndFader(num, oVI, nVI);
        num++;
        num++;
        num++;

        num++;
        num++;
        num++;
        oVI = oldParam.RF5C164;
        nVI = newParam.RF5C164;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.RF5C68;
        nVI = newParam.RF5C68;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.PWM;
        nVI = newParam.PWM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.OKIM6258;
        nVI = newParam.OKIM6258;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.OKIM6295;
        nVI = newParam.OKIM6295;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.C140;
        nVI = newParam.C140;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.C352;
        nVI = newParam.C352;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.SEGAPCM;
        nVI = newParam.SEGAPCM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.MultiPCM;
        nVI = newParam.MultiPCM;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.K051649;
        nVI = newParam.K051649;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.K053260;
        nVI = newParam.K053260;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.K054539;
        nVI = newParam.K054539;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.QSound;
        nVI = newParam.QSound;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.GA20;
        nVI = newParam.GA20;
        drawVolAndFader(num, oVI, nVI);

        num++;
        oVI = oldParam.APU;
        nVI = newParam.APU;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.DMC;
        nVI = newParam.DMC;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.FDS;
        nVI = newParam.FDS;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.MMC5;
        nVI = newParam.MMC5;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.N160;
        nVI = newParam.N160;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.VRC6;
        nVI = newParam.VRC6;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.VRC7;
        nVI = newParam.VRC7;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.FME7;
        nVI = newParam.FME7;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.DMG;
        nVI = newParam.DMG;
        drawVolAndFader(num, oVI, nVI);
        num++;
        num++;
        num++;
        num++;
        num++;
        oVI = oldParam.PPZ8;
        nVI = newParam.PPZ8;
        drawVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.GimicOPN;
        nVI = newParam.GimicOPN;
        drawGVolAndFader(num, oVI, nVI);
        num++;
        oVI = oldParam.GimicOPNA;
        nVI = newParam.GimicOPNA;
        drawGVolAndFader(num, oVI, nVI);
    }

    private void drawVolAndFader(int num, MDChipParams.Mixer.VolumeInfo oVI, MDChipParams.Mixer.VolumeInfo nVI) {
        DrawBuff.drawFader(
                frameBuffer
                , 5 + (num % 16) * 20
                , 16 + (num / 16) * 8 * 9
                , num == 0 ? 0 : 1
                , oVI.Volume
                , nVI.Volume);
        nVI.VisVol2Cnt--;
        if (nVI.VisVol2Cnt == 0) {
            nVI.VisVol2Cnt = 1;
            if (nVI.VisVolume2 > 0) nVI.VisVolume2--;
        }
        DrawBuff.MixerVolume(
                frameBuffer
                , 2 + (num % 16) * 20
                , 10 + (num / 16) * 8 * 9
                , oVI.VisVolume1
                , nVI.VisVolume1
                , oVI.VisVolume2
                , nVI.VisVolume2);
    }

    private void drawGVolAndFader(int num, MDChipParams.Mixer.VolumeInfo oVI, MDChipParams.Mixer.VolumeInfo nVI) {
        DrawBuff.drawGFader(
                frameBuffer
                , 5 + (num % 16) * 20
                , 16 + (num / 16) * 8 * 9
                , num == 0 ? 0 : 1
                , oVI.Volume
                , nVI.Volume);
        nVI.VisVol2Cnt--;
        if (nVI.VisVol2Cnt == 0) {
            nVI.VisVol2Cnt = 1;
            if (nVI.VisVolume2 > 0) nVI.VisVolume2--;
        }
        DrawBuff.MixerVolume(
                frameBuffer
                , 2 + (num % 16) * 20
                , 10 + (num / 16) * 8 * 9
                , oVI.VisVolume1
                , nVI.VisVolume1
                , oVI.VisVolume2
                , nVI.VisVolume2);
    }

    public void screenInit() {
        Audio.visVolume.master = -1;
        Audio.visVolume.ym2151 = -1;
        Audio.visVolume.ym2203 = -1;
        Audio.visVolume.ym2203FM = -1;
        Audio.visVolume.ym2203SSG = -1;
        Audio.visVolume.ym2612 = -1;
        Audio.visVolume.ym2608 = -1;
        Audio.visVolume.ym2608APCM = -1;
        Audio.visVolume.ym2608FM = -1;
        Audio.visVolume.ym2608Rtm = -1;
        Audio.visVolume.ym2608SSG = -1;
        Audio.visVolume.ym2610 = -1;
        Audio.visVolume.ym2610APCMA = -1;
        Audio.visVolume.ym2610APCMB = -1;
        Audio.visVolume.ym2610FM = -1;
        Audio.visVolume.ym2610SSG = -1;

        Audio.visVolume.ym2413 = -1;
        Audio.visVolume.ym3526 = -1;
        Audio.visVolume.y8950 = -1;
        Audio.visVolume.ym3812 = -1;
        Audio.visVolume.ymf262 = -1;
        Audio.visVolume.ymf278b = -1;
        Audio.visVolume.ymz280b = -1;
        Audio.visVolume.ymf271 = -1;
        Audio.visVolume.ay8910 = -1;
        Audio.visVolume.sn76489 = -1;
        Audio.visVolume.huc6280 = -1;

        Audio.visVolume.rf5c164 = -1;
        Audio.visVolume.rf5c68 = -1;
        Audio.visVolume.pwm = -1;
        Audio.visVolume.okim6258 = -1;
        Audio.visVolume.okim6295 = -1;
        Audio.visVolume.c140 = -1;
        Audio.visVolume.c352 = -1;
        Audio.visVolume.saa1099 = -1;
        Audio.visVolume.ppz8 = -1;
        Audio.visVolume.segaPCM = -1;
        Audio.visVolume.multiPCM = -1;
        Audio.visVolume.k051649 = -1;
        Audio.visVolume.k053260 = -1;
        Audio.visVolume.k054539 = -1;
        Audio.visVolume.qSound = -1;
        Audio.visVolume.ga20 = -1;

        Audio.visVolume.APU = 0;
        Audio.visVolume.DMC = 0;
        Audio.visVolume.FDS = 0;
        Audio.visVolume.MMC5 = 0;
        Audio.visVolume.N160 = 0;
        Audio.visVolume.VRC6 = 0;
        Audio.visVolume.VRC7 = 0;
        Audio.visVolume.FME7 = 0;
        Audio.visVolume.DMG = -1;
    }

    private KeyListener frmMixer2_KeyDown = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
        }
    };

    private MouseListener frmMixer2_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();

            chipn = px / 20 + (py / 72) * 16;
        }
    };

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            chipn = px / 20 + (py / 72) * 16;
            boolean b = ev.getButton() == MouseEvent.BUTTON3;
            if (b) setVolume[chipn].accept(true, 0);
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            py = py % 72;
            int n = 0;
            if (ev.getButton() == MouseEvent.BUTTON1) {
                if (chipn < 62) {
                    if (py < 18) {
                        n = (18 - py) > 8 ? 8 : (18 - py);
                        n = (int) (n * 2.5);
                    } else if (py == 18) {
                        n = 0;
                    } else {
                        n = (18 - py) < -35 ? -35 : (18 - py);
                        n = (int) (n * (192.0 / 35.0));
                    }
                } else {
                    if (py < 0) {
                        n = 127;
                    } else {
                        n = (int) ((72 - py) * (127.0 / 72.0));
                    }
                }

                if (chipn < 0 || chipn >= setVolume.length) return;
                setVolume[chipn].accept(true, n);

            }
        }

        @Override
        public void mouseEntered(MouseEvent ev) {
            pbScreen.requestFocus();
        }
    };

    @SuppressWarnings("unchecked")
    BiConsumer<Boolean, Integer>[] setVolume = Arrays.<BiConsumer<Boolean, Integer>>asList(
            Audio::setMasterVolume, Audio::setYM2151Volume, Audio::setYM2203Volume, Audio::setYM2203FMVolume
            , Audio::setYM2203PSGVolume, Audio::setYM2612Volume, Audio::setYM2608Volume, Audio::setYM2608FMVolume
            , Audio::setYM2608PSGVolume, Audio::setYM2608RhythmVolume, Audio::setYM2608AdpcmVolume, Audio::setYM2610Volume
            , Audio::setYM2610FMVolume, Audio::setYM2610PSGVolume, Audio::setYM2610AdpcmAVolume, Audio::setYM2610AdpcmBVolume

            , Audio::setYM2413Volume, Audio::setYM3526Volume, Audio::setY8950Volume, Audio::setYM3812Volume
            , Audio::setYMF262Volume, Audio::setYMF278BVolume, Audio::setYMZ280BVolume, Audio::setYMF271Volume
            , null, Audio::setAY8910Volume, Audio::setSN76489Volume, Audio::setHuC6280Volume
            , Audio::setSA1099Volume, null, null, null

            , null, null, Audio::setRF5C164Volume, Audio::setRF5C68Volume
            , Audio::setPWMVolume, Audio::setOKIM6258Volume, Audio::setOKIM6295Volume, Audio::setC140Volume
            , Audio::setC352Volume, Audio::setSegaPCMVolume, Audio::setMultiPCMVolume, Audio::setK051649Volume
            , Audio::setK053260Volume, Audio::setK054539Volume, Audio::setQSoundVolume, Audio::setGA20Volume

            , Audio::setAPUVolume, Audio::setDMCVolume, Audio::setFDSVolume, Audio::setMMC5Volume
            , Audio::setN160Volume, Audio::setVRC6Volume, Audio::setVRC7Volume, Audio::setFME7Volume
            , Audio::setDMGVolume, null, null, null
            , null, Audio::setPPZ8Volume, Audio::setGimicOPNVolume, Audio::setGimicOPNAVolume
    ).toArray(new BiConsumer[0]);

    private void tsmiLoadDriverBalance_Click(ActionEvent ev) {

    }

    private void tsmiLoadSongBalance_Click(ActionEvent ev) {

    }

    private void tsmiSaveDriverBalance_Click(ActionEvent ev) {
        try {
            String retMsg = parent.SaveDriverBalance(parent.setting.getbalance().copy());
            if (retMsg != "") {
                JOptionPane.showConfirmDialog(null, String.format("ドライバーのミキサーバランス[%s]を設定フォルダーに保存しました。", retMsg), "保存", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            Log.forcedWrite(ex);
            JOptionPane.showConfirmDialog(null, String.format("%s", ex.getMessage()), "保存失敗", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tsmiSaveSongBalance_Click(ActionEvent ev) {
        try {
            Setting.Balance bln = parent.setting.getbalance().copy();
            PlayList.Music ms = parent.GetPlayingMusicInfo();
            if (ms == null) {
                JOptionPane.showConfirmDialog(null, "演奏情報が取得できませんでした。\n演奏中又は演奏完了直後に再度お試しください。", "情報取得失敗", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser sfd = new JFileChooser();
            sfd.setFileFilter(new FileFilter() {
                @Override public boolean accept(File f) { return f.getName().toLowerCase().endsWith(".mbc"); }
                @Override public String getDescription() { return "ミキサーバランス(*.mbc)"; }
            });
            sfd.setDialogTitle("ミキサーバランスを保存");
            sfd.setCurrentDirectory(new File(Path.getDirectoryName(ms.arcFileName == null || ms.arcFileName.isEmpty() ? ms.fileName : ms.arcFileName)));
            if (!parent.setting.getAutoBalance().getSamePositionAsSongData())
                sfd.setCurrentDirectory(new File(Common.settingFilePath = "MixerBalance"));

//            sfd.RestoreDirectory = false;
            sfd.setSelectedFile(new File(Path.getFileName(ms.arcFileName == null || ms.arcFileName.isEmpty() ? ms.fileName : ms.arcFileName) + ".mbc"));
//            sfd.CheckPathExists = true;

            if (sfd.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            bln.save(sfd.getSelectedFile().getPath());
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            JOptionPane.showConfirmDialog(null, String.format("%s", ex.getMessage()), "保存失敗", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponent() {
//        this.components = new System.ComponentModel.Container();
//        System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmMixer2));
        this.pbScreen = new JPanel();
        this.ctxtMenu = new JPopupMenu();
        this.tsmiLoadDriverBalance = new JMenuItem();
        this.tsmiLoadSongBalance = new JMenuItem();
        this.toolStripSeparator1 = new JSeparator();
        this.tsmiSaveDriverBalance = new JMenuItem();
        this.tsmiSaveSongBalance = new JMenuItem();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();
//            this.ctxtMenu.SuspendLayout();

        //
        // pbScreen
        //
//        this.pbScreen.ContextMenuStrip = this.ctxtMenu;
        this.image = mdplayer.properties.Resources.getplaneMixer();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(320, 288));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick); // TODO
        this.pbScreen.addMouseWheelListener(this.pbScreen_MouseWheel); // TODO
        //
        // ctxtMenu
        //
        this.ctxtMenu.add(this.tsmiLoadDriverBalance);
        this.ctxtMenu.add(this.tsmiLoadSongBalance);
        this.ctxtMenu.add(this.toolStripSeparator1);
        this.ctxtMenu.add(this.tsmiSaveDriverBalance);
        this.ctxtMenu.add(this.tsmiSaveSongBalance);
        this.ctxtMenu.setName("ctxtMenu");
        this.ctxtMenu.setPreferredSize(new Dimension(224, 98));
        //
        // tsmiLoadDriverBalance
        //
        this.tsmiLoadDriverBalance.setEnabled(false);
        this.tsmiLoadDriverBalance.setName("tsmiLoadDriverBalance");
        this.tsmiLoadDriverBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiLoadDriverBalance.setText("読込　ドライバーミキサーバランス");
        this.tsmiLoadDriverBalance.addActionListener(this::tsmiLoadDriverBalance_Click);
        //
        // tsmiLoadSongBalance
        //
        this.tsmiLoadSongBalance.setEnabled(false);
        this.tsmiLoadSongBalance.setName("tsmiLoadSongBalance");
        this.tsmiLoadSongBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiLoadSongBalance.setText("読込　ソングミキサーバランス");
        this.tsmiLoadSongBalance.addActionListener(this::tsmiLoadSongBalance_Click);
        //
        // toolStripSeparator1
        //
        this.toolStripSeparator1.setName("toolStripSeparator1");
        this.toolStripSeparator1.setPreferredSize(new Dimension(220, 6));
        //
        // tsmiSaveDriverBalance
        //
        this.tsmiSaveDriverBalance.setName("tsmiSaveDriverBalance");
        this.tsmiSaveDriverBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiSaveDriverBalance.setText("保存　ドライバーミキサーバランス");
        this.tsmiSaveDriverBalance.addActionListener(this::tsmiSaveDriverBalance_Click);
        //
        // tsmiSaveSongBalance
        //
        this.tsmiSaveSongBalance.setName("tsmiSaveSongBalance");
        this.tsmiSaveSongBalance.setPreferredSize(new Dimension(223, 22));
        this.tsmiSaveSongBalance.setText("保存　ソングミキサーバランス");
        this.tsmiSaveSongBalance.addActionListener(this::tsmiSaveSongBalance_Click);
        //
        // frmMixer2
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        this.setPreferredSize(new Dimension(320, 288));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmMixer2");
        this.setTitle("Mixer");
        this.addWindowListener(this.windowListener);
        this.addKeyListener(this.frmMixer2_KeyDown);
        this.addMouseListener(this.frmMixer2_MouseClick); // TODO
        this.addMouseWheelListener(this.pbScreen_MouseWheel); // TODO
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
        // this.ctxtMenu.ResumeLayout(false);
//            this.ResumeLayout(false);

    }

    BufferedImage image;
    public JPanel pbScreen;
    private JPopupMenu ctxtMenu;
    private JMenuItem tsmiSaveDriverBalance;
    private JMenuItem tsmiSaveSongBalance;
    private JMenuItem tsmiLoadDriverBalance;
    private JMenuItem tsmiLoadSongBalance;
    private JSeparator toolStripSeparator1;
}
