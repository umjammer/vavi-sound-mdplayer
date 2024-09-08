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
import dotnet4j.util.compat.Tuple;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.PlayList;
import mdplayer.Setting;
import mdplayer.form.kb.wf.frmHuC6280;
import mdplayer.properties.Resources;
import mdsound.Instrument;
import mdsound.instrument.*;


public class frmMixer2 extends JFrame {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    public frmMain parent;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int zoom;
    private int chipn = -1;

    private MDChipParams.Mixer newParam;
    private MDChipParams.Mixer oldParam = new MDChipParams.Mixer();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmHuC6280.class);
    Audio audio = Audio.getInstance();

    public frmMixer2(frmMain frm, int zoom, MDChipParams.Mixer newParam) {
        parent = frm;
        this.zoom = zoom;

        initializeComponent();
        pbScreen.addMouseWheelListener(this.pbScreen_MouseWheel);

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneMixer(), null, zoom);
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
            setVolume(chipn, false, delta);
        }
    };

    private void setVolume(int i, boolean isAbs, int delta) {
        if (i == 0) {
            audio.setMasterVolume(isAbs, delta);
        } else if (i == setVolume.length) {
            audio.setGimicOPNVolume(false, delta);
        } else if (i == setVolume.length + 1) {
            audio.setGimicOPNAVolume(false, delta);
        } else if (i > 0 && i < setVolume.length) {
            var t = setVolume[chipn];
            audio.setVolume(t.getItem1(), t.getItem2(), isAbs, delta);
        }
    }

    public void update() {
        frameBuffer.Refresh(null);
    }

//    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().setPosMixer(getLocation());
            } else {
                parent.setting.getLocation().setPosMixer(new Point(prefs.getInt("x", 0), prefs.getInt("y", 0)));
            }
            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent ev) {
            setLocation(new Point(x, y));

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneMixer().getWidth() * zoom, frameSizeH + Resources.getPlaneMixer().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneMixer().getWidth() * zoom, frameSizeH + Resources.getPlaneMixer().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneMixer().getWidth() * zoom, frameSizeH + Resources.getPlaneMixer().getHeight() * zoom));
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

        newParam.Master.Volume = parent.setting.getBalance().getMasterVolume();
        newParam.YM2151.Volume = parent.setting.getBalance().getVolume("MAIN", Ym2151Inst.class);
        newParam.YM2203.Volume = parent.setting.getBalance().getVolume("MAIN", Ym2203Inst.class);
        newParam.YM2203FM.Volume = parent.setting.getBalance().getVolume("FM", Ym2203Inst.class);
        newParam.YM2203PSG.Volume = parent.setting.getBalance().getVolume("PSG", Ym2203Inst.class);
        newParam.YM2612.Volume = parent.setting.getBalance().getVolume("MAIN", Ym2612Inst.class);
        newParam.YM2608.Volume = parent.setting.getBalance().getVolume("MAIN", Ym2608Inst.class);
        newParam.YM2608FM.Volume = parent.setting.getBalance().getVolume("FM", Ym2608Inst.class);
        newParam.YM2608PSG.Volume = parent.setting.getBalance().getVolume("PSG", Ym2608Inst.class);
        newParam.YM2608Rhythm.Volume = parent.setting.getBalance().getVolume("Rhythm", Ym2608Inst.class);
        newParam.YM2608Adpcm.Volume = parent.setting.getBalance().getVolume("Adpcm", Ym2608Inst.class);
        newParam.YM2610.Volume = parent.setting.getBalance().getVolume("MAIN", Ym2610Inst.class);
        newParam.YM2610FM.Volume = parent.setting.getBalance().getVolume("FM", Ym2610Inst.class);
        newParam.YM2610PSG.Volume = parent.setting.getBalance().getVolume("PSG", Ym2610Inst.class);
        newParam.YM2610AdpcmA.Volume = parent.setting.getBalance().getVolume("AdpcmA", Ym2610Inst.class);
        newParam.YM2610AdpcmB.Volume = parent.setting.getBalance().getVolume("AdpcmB", Ym2610Inst.class);

        newParam.YM2413.Volume = parent.setting.getBalance().getVolume("MAIN", Ym2413Inst.class);
        newParam.YM3526.Volume = parent.setting.getBalance().getVolume("MAIN", Ym3526Inst.class);
        newParam.Y8950.Volume = parent.setting.getBalance().getVolume("MAIN", Y8950Inst.class);
        newParam.YM3812.Volume = parent.setting.getBalance().getVolume("MAIN", Ym3812Inst.class);
        newParam.YMF262.Volume = parent.setting.getBalance().getVolume("MAIN", YmF262Inst.class);
        newParam.YMF278B.Volume = parent.setting.getBalance().getVolume("MAIN", YmF278bInst.class);
        newParam.YMZ280B.Volume = parent.setting.getBalance().getVolume("MAIN", YmZ280bInst.class);
        newParam.YMF271.Volume = parent.setting.getBalance().getVolume("MAIN", YmF271Inst.class);
        newParam.AY8910.Volume = parent.setting.getBalance().getVolume("MAIN", Ay8910Inst.class);
        newParam.SN76489.Volume = parent.setting.getBalance().getVolume("MAIN", Sn76489Inst.class);
        newParam.HuC6280.Volume = parent.setting.getBalance().getVolume("MAIN", HuC6280Inst.class);

        newParam.RF5C164.Volume = parent.setting.getBalance().getVolume("MAIN", ScdPcmInst.class);
        newParam.RF5C68.Volume = parent.setting.getBalance().getVolume("MAIN", Rf5c68Inst.class);
        newParam.PWM.Volume = parent.setting.getBalance().getVolume("MAIN", PwmInst.class);
        newParam.OKIM6258.Volume = parent.setting.getBalance().getVolume("MAIN", OkiM6258Inst.class);
        newParam.OKIM6295.Volume = parent.setting.getBalance().getVolume("MAIN", OkiM6295Inst.class);
        newParam.C140.Volume = parent.setting.getBalance().getVolume("MAIN", C140Inst.class);
        newParam.C352.Volume = parent.setting.getBalance().getVolume("MAIN", C352Inst.class);
        newParam.SAA1099.Volume = parent.setting.getBalance().getVolume("MAIN", Saa1099Inst.class);
        newParam.PPZ8.Volume = parent.setting.getBalance().getVolume("MAIN", Ppz8Inst.class);
        newParam.SEGAPCM.Volume = parent.setting.getBalance().getVolume("MAIN", SegaPcmInst.class);
        newParam.MultiPCM.Volume = parent.setting.getBalance().getVolume("MAIN", MultiPcmInst.class);
        newParam.K051649.Volume = parent.setting.getBalance().getVolume("MAIN", K051649Inst.class);
        newParam.K053260.Volume = parent.setting.getBalance().getVolume("MAIN", K053260Inst.class);
        newParam.K054539.Volume = parent.setting.getBalance().getVolume("MAIN", K054539Inst.class);
        newParam.QSound.Volume = parent.setting.getBalance().getVolume("MAIN", QSoundInst.class);
        newParam.GA20.Volume = parent.setting.getBalance().getVolume("MAIN", Ga20Inst.class);

        newParam.APU.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.class);
        newParam.DMC.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.DMC.class);
        newParam.FDS.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.FDS.class);
        newParam.MMC5.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.MMC5.class);
        newParam.N160.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.N160.class);
        newParam.VRC6.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.VRC6.class);
        newParam.VRC7.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.VRC7.class);
        newParam.FME7.Volume = parent.setting.getBalance().getVolume("MAIN", IntFNesInst.FME7.class);
        newParam.DMG.Volume = parent.setting.getBalance().getVolume("MAIN", DmgInst.class);

        newParam.GimicOPN.Volume = parent.setting.getBalance().getGimicOPNVolume();
        newParam.GimicOPNA.Volume = parent.setting.getBalance().getGimicOPNAVolume();


        newParam.Master.VisVolume1 = Common.range(audio.visVolume.get("master") / 250, 0, 44);
        if (newParam.Master.VisVolume2 <= newParam.Master.VisVolume1) {
            newParam.Master.VisVolume2 = newParam.Master.VisVolume1;
            newParam.Master.VisVol2Cnt = 30;
        }

        newParam.YM2151.VisVolume1 = Common.range(audio.visVolume.get("ym2151") / 200, 0, 44);
        if (newParam.YM2151.VisVolume2 <= newParam.YM2151.VisVolume1) {
            newParam.YM2151.VisVolume2 = newParam.YM2151.VisVolume1;
            newParam.YM2151.VisVol2Cnt = 30;
        }

        newParam.YM2203.VisVolume1 = Common.range(audio.visVolume.get("ym2203") / 200, 0, 44);
        if (newParam.YM2203.VisVolume2 <= newParam.YM2203.VisVolume1) {
            newParam.YM2203.VisVolume2 = newParam.YM2203.VisVolume1;
            newParam.YM2203.VisVol2Cnt = 30;
        }

        newParam.YM2203FM.VisVolume1 = Common.range(audio.visVolume.get("ym2203FM") / 200, 0, 44);
        if (newParam.YM2203FM.VisVolume2 <= newParam.YM2203FM.VisVolume1) {
            newParam.YM2203FM.VisVolume2 = newParam.YM2203FM.VisVolume1;
            newParam.YM2203FM.VisVol2Cnt = 30;
        }

        newParam.YM2203PSG.VisVolume1 = Common.range(audio.visVolume.get("ym2203SSG") / 120, 0, 44);
        if (newParam.YM2203PSG.VisVolume2 <= newParam.YM2203PSG.VisVolume1) {
            newParam.YM2203PSG.VisVolume2 = newParam.YM2203PSG.VisVolume1;
            newParam.YM2203PSG.VisVol2Cnt = 30;
        }

        newParam.YM2612.VisVolume1 = Common.range(audio.visVolume.get("ym2612") / 200, 0, 44);
        if (newParam.YM2612.VisVolume2 <= newParam.YM2612.VisVolume1) {
            newParam.YM2612.VisVolume2 = newParam.YM2612.VisVolume1;
            newParam.YM2612.VisVol2Cnt = 30;
        }

        newParam.YM2608.VisVolume1 = Common.range(audio.visVolume.get("ym2608") / 200, 0, 44);
        if (newParam.YM2608.VisVolume2 <= newParam.YM2608.VisVolume1) {
            newParam.YM2608.VisVolume2 = newParam.YM2608.VisVolume1;
            newParam.YM2608.VisVol2Cnt = 30;
        }

        newParam.YM2608FM.VisVolume1 = Common.range(audio.visVolume.get("ym2608FM") / 200, 0, 44);
        if (newParam.YM2608FM.VisVolume2 <= newParam.YM2608FM.VisVolume1) {
            newParam.YM2608FM.VisVolume2 = newParam.YM2608FM.VisVolume1;
            newParam.YM2608FM.VisVol2Cnt = 30;
        }

        newParam.YM2608PSG.VisVolume1 = Common.range(audio.visVolume.get("ym2608SSG") / 120, 0, 44);
        if (newParam.YM2608PSG.VisVolume2 <= newParam.YM2608PSG.VisVolume1) {
            newParam.YM2608PSG.VisVolume2 = newParam.YM2608PSG.VisVolume1;
            newParam.YM2608PSG.VisVol2Cnt = 30;
        }

        newParam.YM2608Rhythm.VisVolume1 = Common.range(audio.visVolume.get("ym2608Rtm") / 200, 0, 44);
        if (newParam.YM2608Rhythm.VisVolume2 <= newParam.YM2608Rhythm.VisVolume1) {
            newParam.YM2608Rhythm.VisVolume2 = newParam.YM2608Rhythm.VisVolume1;
            newParam.YM2608Rhythm.VisVol2Cnt = 30;
        }

        newParam.YM2608Adpcm.VisVolume1 = Common.range(audio.visVolume.get("ym2608APCM") / 200, 0, 44);
        if (newParam.YM2608Adpcm.VisVolume2 <= newParam.YM2608Adpcm.VisVolume1) {
            newParam.YM2608Adpcm.VisVolume2 = newParam.YM2608Adpcm.VisVolume1;
            newParam.YM2608Adpcm.VisVol2Cnt = 30;
        }

        newParam.YM2610.VisVolume1 = Common.range(audio.visVolume.get("ym2610") / 200, 0, 44);
        if (newParam.YM2610.VisVolume2 <= newParam.YM2610.VisVolume1) {
            newParam.YM2610.VisVolume2 = newParam.YM2610.VisVolume1;
            newParam.YM2610.VisVol2Cnt = 30;
        }

        newParam.YM2610FM.VisVolume1 = Common.range(audio.visVolume.get("ym2610FM") / 200, 0, 44);
        if (newParam.YM2610FM.VisVolume2 <= newParam.YM2610FM.VisVolume1) {
            newParam.YM2610FM.VisVolume2 = newParam.YM2610FM.VisVolume1;
            newParam.YM2610FM.VisVol2Cnt = 30;
        }

        newParam.YM2610PSG.VisVolume1 = Common.range(audio.visVolume.get("ym2610SSG") / 120, 0, 44);
        if (newParam.YM2610PSG.VisVolume2 <= newParam.YM2610PSG.VisVolume1) {
            newParam.YM2610PSG.VisVolume2 = newParam.YM2610PSG.VisVolume1;
            newParam.YM2610PSG.VisVol2Cnt = 30;
        }

        newParam.YM2610AdpcmA.VisVolume1 = Common.range(audio.visVolume.get("ym2610APCMA") / 200, 0, 44);
        if (newParam.YM2610AdpcmA.VisVolume2 <= newParam.YM2610AdpcmA.VisVolume1) {
            newParam.YM2610AdpcmA.VisVolume2 = newParam.YM2610AdpcmA.VisVolume1;
            newParam.YM2610AdpcmA.VisVol2Cnt = 30;
        }

        newParam.YM2610AdpcmB.VisVolume1 = Common.range(audio.visVolume.get("ym2610APCMB") / 200, 0, 44);
        if (newParam.YM2610AdpcmB.VisVolume2 <= newParam.YM2610AdpcmB.VisVolume1) {
            newParam.YM2610AdpcmB.VisVolume2 = newParam.YM2610AdpcmB.VisVolume1;
            newParam.YM2610AdpcmB.VisVol2Cnt = 30;
        }


        newParam.YM2413.VisVolume1 = Common.range(audio.visVolume.get("ym2413") / 200, 0, 44);
        if (newParam.YM2413.VisVolume2 <= newParam.YM2413.VisVolume1) {
            newParam.YM2413.VisVolume2 = newParam.YM2413.VisVolume1;
            newParam.YM2413.VisVol2Cnt = 30;
        }

        newParam.YM3526.VisVolume1 = Common.range(audio.visVolume.get("ym3526") / 200, 0, 44);
        if (newParam.YM3526.VisVolume2 <= newParam.YM3526.VisVolume1) {
            newParam.YM3526.VisVolume2 = newParam.YM3526.VisVolume1;
            newParam.YM3526.VisVol2Cnt = 30;
        }

        newParam.Y8950.VisVolume1 = Common.range(audio.visVolume.get("y8950") / 200, 0, 44);
        if (newParam.Y8950.VisVolume2 <= newParam.Y8950.VisVolume1) {
            newParam.Y8950.VisVolume2 = newParam.Y8950.VisVolume1;
            newParam.Y8950.VisVol2Cnt = 30;
        }

        newParam.YM3812.VisVolume1 = Common.range(audio.visVolume.get("ym3812") / 200, 0, 44);
        if (newParam.YM3812.VisVolume2 <= newParam.YM3812.VisVolume1) {
            newParam.YM3812.VisVolume2 = newParam.YM3812.VisVolume1;
            newParam.YM3812.VisVol2Cnt = 30;
        }

        newParam.YMF262.VisVolume1 = Common.range(audio.visVolume.get("ymf262") / 200, 0, 44);
        if (newParam.YMF262.VisVolume2 <= newParam.YMF262.VisVolume1) {
            newParam.YMF262.VisVolume2 = newParam.YMF262.VisVolume1;
            newParam.YMF262.VisVol2Cnt = 30;
        }

        newParam.YMF278B.VisVolume1 = Common.range(audio.visVolume.get("ymf278b") / 200, 0, 44);
        if (newParam.YMF278B.VisVolume2 <= newParam.YMF278B.VisVolume1) {
            newParam.YMF278B.VisVolume2 = newParam.YMF278B.VisVolume1;
            newParam.YMF278B.VisVol2Cnt = 30;
        }

        newParam.YMZ280B.VisVolume1 = Common.range(audio.visVolume.get("ymz280b") / 200, 0, 44);
        if (newParam.YMZ280B.VisVolume2 <= newParam.YMZ280B.VisVolume1) {
            newParam.YMZ280B.VisVolume2 = newParam.YMZ280B.VisVolume1;
            newParam.YMZ280B.VisVol2Cnt = 30;
        }

        newParam.YMF271.VisVolume1 = Common.range(audio.visVolume.get("ymf271") / 200, 0, 44);
        if (newParam.YMF271.VisVolume2 <= newParam.YMF271.VisVolume1) {
            newParam.YMF271.VisVolume2 = newParam.YMF271.VisVolume1;
            newParam.YMF271.VisVol2Cnt = 30;
        }

        newParam.AY8910.VisVolume1 = Common.range(audio.visVolume.get("ay8910") / 120, 0, 44);
        if (newParam.AY8910.VisVolume2 <= newParam.AY8910.VisVolume1) {
            newParam.AY8910.VisVolume2 = newParam.AY8910.VisVolume1;
            newParam.AY8910.VisVol2Cnt = 30;
        }

        newParam.SN76489.VisVolume1 = Common.range(audio.visVolume.get("sn76489") / 120, 0, 44);
        if (newParam.SN76489.VisVolume2 <= newParam.SN76489.VisVolume1) {
            newParam.SN76489.VisVolume2 = newParam.SN76489.VisVolume1;
            newParam.SN76489.VisVol2Cnt = 30;
        }

        newParam.HuC6280.VisVolume1 = Common.range(audio.visVolume.get("huc6280") / 120, 0, 44);
        if (newParam.HuC6280.VisVolume2 <= newParam.HuC6280.VisVolume1) {
            newParam.HuC6280.VisVolume2 = newParam.HuC6280.VisVolume1;
            newParam.HuC6280.VisVol2Cnt = 30;
        }


        newParam.RF5C164.VisVolume1 = Common.range(audio.visVolume.get("rf5c164") / 200, 0, 44);
        if (newParam.RF5C164.VisVolume2 <= newParam.RF5C164.VisVolume1) {
            newParam.RF5C164.VisVolume2 = newParam.RF5C164.VisVolume1;
            newParam.RF5C164.VisVol2Cnt = 30;
        }

        newParam.RF5C68.VisVolume1 = Common.range(audio.visVolume.get("rf5c68") / 200, 0, 44);
        if (newParam.RF5C68.VisVolume2 <= newParam.RF5C68.VisVolume1) {
            newParam.RF5C68.VisVolume2 = newParam.RF5C68.VisVolume1;
            newParam.RF5C68.VisVol2Cnt = 30;
        }

        newParam.PWM.VisVolume1 = Common.range(audio.visVolume.get("pwm") / 200, 0, 44);
        if (newParam.PWM.VisVolume2 <= newParam.PWM.VisVolume1) {
            newParam.PWM.VisVolume2 = newParam.PWM.VisVolume1;
            newParam.PWM.VisVol2Cnt = 30;
        }

        newParam.OKIM6258.VisVolume1 = Common.range(audio.visVolume.get("okim6258") / 200, 0, 44);
        if (newParam.OKIM6258.VisVolume2 <= newParam.OKIM6258.VisVolume1) {
            newParam.OKIM6258.VisVolume2 = newParam.OKIM6258.VisVolume1;
            newParam.OKIM6258.VisVol2Cnt = 30;
        }

        newParam.OKIM6295.VisVolume1 = Common.range(audio.visVolume.get("okim6295") / 200, 0, 44);
        if (newParam.OKIM6295.VisVolume2 <= newParam.OKIM6295.VisVolume1) {
            newParam.OKIM6295.VisVolume2 = newParam.OKIM6295.VisVolume1;
            newParam.OKIM6295.VisVol2Cnt = 30;
        }

        newParam.C140.VisVolume1 = Common.range(audio.visVolume.get("c140") / 200, 0, 44);
        if (newParam.C140.VisVolume2 <= newParam.C140.VisVolume1) {
            newParam.C140.VisVolume2 = newParam.C140.VisVolume1;
            newParam.C140.VisVol2Cnt = 30;
        }

        newParam.C352.VisVolume1 = Common.range(audio.visVolume.get("c352") / 200, 0, 44);
        if (newParam.C352.VisVolume2 <= newParam.C352.VisVolume1) {
            newParam.C352.VisVolume2 = newParam.C352.VisVolume1;
            newParam.C352.VisVol2Cnt = 30;
        }

        newParam.SAA1099.VisVolume1 = Common.range(audio.visVolume.get("saa1099") / 200, 0, 44);
        if (newParam.SAA1099.VisVolume2 <= newParam.SAA1099.VisVolume1) {
            newParam.SAA1099.VisVolume2 = newParam.SAA1099.VisVolume1;
            newParam.SAA1099.VisVol2Cnt = 30;
        }

        newParam.PPZ8.VisVolume1 = Common.range(audio.visVolume.get("ppz8") / 200, 0, 44);
        if (newParam.PPZ8.VisVolume2 <= newParam.PPZ8.VisVolume1) {
            newParam.PPZ8.VisVolume2 = newParam.PPZ8.VisVolume1;
            newParam.PPZ8.VisVol2Cnt = 30;
        }

        newParam.SEGAPCM.VisVolume1 = Common.range(audio.visVolume.get("segaPCM") / 200, 0, 44);
        if (newParam.SEGAPCM.VisVolume2 <= newParam.SEGAPCM.VisVolume1) {
            newParam.SEGAPCM.VisVolume2 = newParam.SEGAPCM.VisVolume1;
            newParam.SEGAPCM.VisVol2Cnt = 30;
        }

        newParam.MultiPCM.VisVolume1 = Common.range(audio.visVolume.get("multiPCM") / 200, 0, 44);
        if (newParam.MultiPCM.VisVolume2 <= newParam.MultiPCM.VisVolume1) {
            newParam.MultiPCM.VisVolume2 = newParam.MultiPCM.VisVolume1;
            newParam.MultiPCM.VisVol2Cnt = 30;
        }

        newParam.K051649.VisVolume1 = Common.range(audio.visVolume.get("k051649") / 200, 0, 44);
        if (newParam.K051649.VisVolume2 <= newParam.K051649.VisVolume1) {
            newParam.K051649.VisVolume2 = newParam.K051649.VisVolume1;
            newParam.K051649.VisVol2Cnt = 30;
        }

        newParam.K053260.VisVolume1 = Common.range(audio.visVolume.get("k053260") / 200, 0, 44);
        if (newParam.K053260.VisVolume2 <= newParam.K053260.VisVolume1) {
            newParam.K053260.VisVolume2 = newParam.K053260.VisVolume1;
            newParam.K053260.VisVol2Cnt = 30;
        }

        newParam.K054539.VisVolume1 = Common.range(audio.visVolume.get("k054539") / 200, 0, 44);
        if (newParam.K054539.VisVolume2 <= newParam.K054539.VisVolume1) {
            newParam.K054539.VisVolume2 = newParam.K054539.VisVolume1;
            newParam.K054539.VisVol2Cnt = 30;
        }

        newParam.QSound.VisVolume1 = Common.range(audio.visVolume.get("qSound") / 200, 0, 44);
        if (newParam.QSound.VisVolume2 <= newParam.QSound.VisVolume1) {
            newParam.QSound.VisVolume2 = newParam.QSound.VisVolume1;
            newParam.QSound.VisVol2Cnt = 30;
        }

        newParam.GA20.VisVolume1 = Common.range(audio.visVolume.get("ga20") / 200, 0, 44);
        if (newParam.GA20.VisVolume2 <= newParam.GA20.VisVolume1) {
            newParam.GA20.VisVolume2 = newParam.GA20.VisVolume1;
            newParam.GA20.VisVol2Cnt = 30;
        }

        newParam.APU.VisVolume1 = Common.range(audio.visVolume.get("APU") / 200, 0, 44);
        if (newParam.APU.VisVolume2 <= newParam.APU.VisVolume1) {
            newParam.APU.VisVolume2 = newParam.APU.VisVolume1;
            newParam.APU.VisVol2Cnt = 30;
        }

        newParam.DMC.VisVolume1 = Common.range(audio.visVolume.get("DMC") / 350, 0, 44);
        if (newParam.DMC.VisVolume2 <= newParam.DMC.VisVolume1) {
            newParam.DMC.VisVolume2 = newParam.DMC.VisVolume1;
            newParam.DMC.VisVol2Cnt = 30;
        }

        newParam.FDS.VisVolume1 = Common.range(audio.visVolume.get("FDS") / 200, 0, 44);
        if (newParam.FDS.VisVolume2 <= newParam.FDS.VisVolume1) {
            newParam.FDS.VisVolume2 = newParam.FDS.VisVolume1;
            newParam.FDS.VisVol2Cnt = 30;
        }

        newParam.MMC5.VisVolume1 = Common.range(audio.visVolume.get("MMC5") / 50, 0, 44);
        if (newParam.MMC5.VisVolume2 <= newParam.K054539.VisVolume1) {
            newParam.MMC5.VisVolume2 = newParam.MMC5.VisVolume1;
            newParam.MMC5.VisVol2Cnt = 30;
        }

        newParam.N160.VisVolume1 = Common.range(audio.visVolume.get("N160") / 50, 0, 44);
        if (newParam.N160.VisVolume2 <= newParam.N160.VisVolume1) {
            newParam.N160.VisVolume2 = newParam.N160.VisVolume1;
            newParam.N160.VisVol2Cnt = 30;
        }
        newParam.VRC6.VisVolume1 = Common.range(audio.visVolume.get("Vrc6Inst") / 50, 0, 44);
        if (newParam.VRC6.VisVolume2 <= newParam.VRC6.VisVolume1) {
            newParam.VRC6.VisVolume2 = newParam.VRC6.VisVolume1;
            newParam.VRC6.VisVol2Cnt = 30;
        }

        newParam.VRC7.VisVolume1 = Common.range(audio.visVolume.get("VRC7") / 50, 0, 44);
        if (newParam.VRC7.VisVolume2 <= newParam.VRC7.VisVolume1) {
            newParam.VRC7.VisVolume2 = newParam.VRC7.VisVolume1;
            newParam.VRC7.VisVol2Cnt = 30;
        }

        newParam.FME7.VisVolume1 = Common.range(audio.visVolume.get("FME7") / 50, 0, 44);
        if (newParam.FME7.VisVolume2 <= newParam.FME7.VisVolume1) {
            newParam.FME7.VisVolume2 = newParam.FME7.VisVolume1;
            newParam.FME7.VisVol2Cnt = 30;
        }

        newParam.DMG.VisVolume1 = Common.range(audio.visVolume.get("DMG") / 50, 0, 44);
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
        audio.visVolume.put("master", -1);
        audio.visVolume.put("ym2151", -1);
        audio.visVolume.put("ym2203", -1);
        audio.visVolume.put("ym2203FM", -1);
        audio.visVolume.put("ym2203SSG", -1);
        audio.visVolume.put("ym2612", -1);
        audio.visVolume.put("ym2608", -1);
        audio.visVolume.put("ym2608APCM", -1);
        audio.visVolume.put("ym2608FM", -1);
        audio.visVolume.put("ym2608Rtm", -1);
        audio.visVolume.put("ym2608SSG", -1);
        audio.visVolume.put("ym2610", -1);
        audio.visVolume.put("ym2610APCMA", -1);
        audio.visVolume.put("ym2610APCMB", -1);
        audio.visVolume.put("ym2610FM", -1);
        audio.visVolume.put("ym2610SSG", -1);

        audio.visVolume.put("ym2413", -1);
        audio.visVolume.put("ym3526", -1);
        audio.visVolume.put("y8950", -1);
        audio.visVolume.put("ym3812", -1);
        audio.visVolume.put("ymf262", -1);
        audio.visVolume.put("ymf278b", -1);
        audio.visVolume.put("ymz280b", -1);
        audio.visVolume.put("ymf271", -1);
        audio.visVolume.put("ay8910", -1);
        audio.visVolume.put("sn76489", -1);
        audio.visVolume.put("huc6280", -1);

        audio.visVolume.put("rf5c164", -1);
        audio.visVolume.put("rf5c68", -1);
        audio.visVolume.put("pwm", -1);
        audio.visVolume.put("okim6258", -1);
        audio.visVolume.put("okim6295", -1);
        audio.visVolume.put("c140", -1);
        audio.visVolume.put("c352", -1);
        audio.visVolume.put("saa1099", -1);
        audio.visVolume.put("ppz8", -1);
        audio.visVolume.put("segaPCM", -1);
        audio.visVolume.put("multiPCM", -1);
        audio.visVolume.put("k051649", -1);
        audio.visVolume.put("k053260", -1);
        audio.visVolume.put("k054539", -1);
        audio.visVolume.put("qSound", -1);
        audio.visVolume.put("ga20", -1);

        audio.visVolume.put("APU", 0);
        audio.visVolume.put("DMC", 0);
        audio.visVolume.put("FDS", 0);
        audio.visVolume.put("MMC5", 0);
        audio.visVolume.put("N160", 0);
        audio.visVolume.put("Vrc6Inst", 0);
        audio.visVolume.put("VRC7", 0);
        audio.visVolume.put("FME7", 0);
        audio.visVolume.put("DMG", -1);
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
            if (b) setVolume(chipn, true, 0);
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
            int px = ev.getX() / parent.setting.getOther().getZoom();
            int py = ev.getY() / parent.setting.getOther().getZoom();
            py = py % 72;
            int n;
            if (ev.getButton() == MouseEvent.BUTTON1) {
                if (chipn < 62) {
                    if (py < 18) {
                        n = Math.min((18 - py), 8);
                        n = (int) (n * 2.5);
                    } else if (py == 18) {
                        n = 0;
                    } else {
                        n = Math.max((18 - py), -35);
                        n = (int) (n * (192.0 / 35.0));
                    }
                } else {
                    if (py < 0) {
                        n = 127;
                    } else {
                        n = (int) ((72 - py) * (127.0 / 72.0));
                    }
                }

                setVolume(chipn, true, n);
            }
        }

        @Override
        public void mouseEntered(MouseEvent ev) {
            pbScreen.requestFocus();
        }
    };

    private void tsmiLoadDriverBalance_Click(ActionEvent ev) {

    }

    private void tsmiLoadSongBalance_Click(ActionEvent ev) {

    }

    private void tsmiSaveDriverBalance_Click(ActionEvent ev) {
        try {
            String retMsg = parent.SaveDriverBalance(parent.setting.getBalance().copy());
            if (!retMsg.equals("")) {
                JOptionPane.showMessageDialog(null, String.format("ドライバーのミキサーバランス[%s]を設定フォルダーに保存しました。", retMsg), "保存", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, String.format("%s", ex.getMessage()), "保存失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tsmiSaveSongBalance_Click(ActionEvent ev) {
        try {
            Setting.Balance bln = parent.setting.getBalance().copy();
            PlayList.Music ms = parent.GetPlayingMusicInfo();
            if (ms == null) {
                JOptionPane.showMessageDialog(null, "演奏情報が取得できませんでした。\n演奏中又は演奏完了直後に再度お試しください。",
                        "情報取得失敗", JOptionPane.ERROR_MESSAGE);
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
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, String.format("%s", ex.getMessage()), "保存失敗", JOptionPane.ERROR_MESSAGE);
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
        this.image = mdplayer.properties.Resources.getPlaneMixer();
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

    @SuppressWarnings("unchecked")
    private Tuple<String, Class<? extends Instrument>>[] setVolume = Arrays.<Tuple<String, Class<? extends Instrument>>>asList(
            null, // master
            new Tuple<>("MAIN", Ym2151Inst.class),
            new Tuple<>("MAIN", Ym2203Inst.class),
            new Tuple<>("FM", Ym2203Inst.class),
            new Tuple<>("PSG", Ym2203Inst.class),
            new Tuple<>("MAIN", Ym2612Inst.class),
            new Tuple<>("MAIN", Ym2608Inst.class),
            new Tuple<>("FM", Ym2608Inst.class),
            new Tuple<>("PSG", Ym2608Inst.class),
            new Tuple<>("Rhythm", Ym2608Inst.class),
            new Tuple<>("Adpcm", Ym2608Inst.class),
            new Tuple<>("MAIN", Ym2610Inst.class),
            new Tuple<>("FM", Ym2610Inst.class),
            new Tuple<>("PSG", Ym2610Inst.class),
            new Tuple<>("AdpcmA", Ym2610Inst.class),
            new Tuple<>("AdpcmB", Ym2610Inst.class),
            new Tuple<>("MAIN", Ym2413Inst.class),
            new Tuple<>("MAIN", Ym3526Inst.class),
            new Tuple<>("MAIN", Y8950Inst.class),
            new Tuple<>("MAIN", Ym3812Inst.class),
            new Tuple<>("MAIN", YmF262Inst.class),
            new Tuple<>("MAIN", YmF278bInst.class),
            new Tuple<>("MAIN", YmZ280bInst.class),
            new Tuple<>("MAIN", YmF271Inst.class),
            null,
            new Tuple<>("MAIN", Ay8910Inst.class),
            new Tuple<>("MAIN", Sn76489Inst.class),
            new Tuple<>("MAIN", HuC6280Inst.class),
            new Tuple<>("MAIN", Saa1099Inst.class),
            null,
            null,
            null,
            null,
            null,
            new Tuple<>("MAIN", ScdPcmInst.class),
            new Tuple<>("MAIN", Rf5c68Inst.class),
            new Tuple<>("MAIN", PwmInst.class),
            new Tuple<>("MAIN", OkiM6258Inst.class),
            new Tuple<>("MAIN", OkiM6295Inst.class),
            new Tuple<>("MAIN", C140Inst.class),
            new Tuple<>("MAIN", C352Inst.class),
            new Tuple<>("MAIN", SegaPcmInst.class),
            new Tuple<>("MAIN", MultiPcmInst.class),
            new Tuple<>("MAIN", K051649Inst.class),
            new Tuple<>("MAIN", K053260Inst.class),
            new Tuple<>("MAIN", K054539Inst.class),
            new Tuple<>("MAIN", QSoundInst.class),
            new Tuple<>("MAIN", Ga20Inst.class),
            new Tuple<>("MAIN", IntFNesInst.class),
            new Tuple<>("MAIN", IntFNesInst.DMC.class),
            new Tuple<>("MAIN", IntFNesInst.FDS.class),
            new Tuple<>("MAIN", IntFNesInst.MMC5.class),
            new Tuple<>("MAIN", IntFNesInst.N160.class),
            new Tuple<>("MAIN", IntFNesInst.VRC6.class),
            new Tuple<>("MAIN", IntFNesInst.VRC7.class),
            new Tuple<>("MAIN", IntFNesInst.FME7.class),
            new Tuple<>("MAIN", DmgInst.class),
            null,
            null,
            null,
            null,
            new Tuple<>("MAIN", Ppz8Inst.class)
    ).toArray(Tuple[]::new);
}
