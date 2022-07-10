package mdplayer.form.kb.opl;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;
import javax.swing.JPanel;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.DrawBuff;
import mdplayer.FrameBuffer;
import mdplayer.MDChipParams;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmYMF278B extends frmBase {
    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    private int frameSizeW = 0;
    private int frameSizeH = 0;
    private int chipID = 0;
    private int zoom = 1;

    private MDChipParams.YMF278B newParam = null;
    private MDChipParams.YMF278B oldParam = new MDChipParams.YMF278B();
    private FrameBuffer frameBuffer = new FrameBuffer();

    static Preferences prefs = Preferences.userNodeForPackage(frmYMF278B.class);

    public frmYMF278B(frmMain frm, int chipID, int zoom, MDChipParams.YMF278B newParam) {
        super(frm);
        this.chipID = chipID;
        this.zoom = zoom;
        initializeComponent();

        this.newParam = newParam;
        frameBuffer.Add(pbScreen, Resources.getPlaneYMF278B(), null, zoom);
        boolean ymF278BType = (chipID == 0)
                ? parent.setting.getYMF278BType()[0].getUseReal()[0]
                : parent.setting.getYMF278BType()[1].getUseReal()[0];
        int ymF278BSoundLocation = (chipID == 0)
                ? parent.setting.getYMF278BType()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYMF278BType()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = ymF278BType ? 1 : 0;
        DrawBuff.screenInitYMF278B(frameBuffer, tp);
        update();
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
                parent.setting.getLocation().getPosYmf278b()[chipID] = getLocation();
            } else {
                parent.setting.getLocation().getPosYmf278b()[chipID] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
            }
            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent e) {
            setLocation(new Point(x, y));

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + Resources.getPlaneYMF278B().getWidth() * zoom, frameSizeH + Resources.getPlaneYMF278B().getHeight() * zoom));
        this.setMinimumSize(new Dimension(frameSizeW + Resources.getPlaneYMF278B().getWidth() * zoom, frameSizeH + Resources.getPlaneYMF278B().getHeight() * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + Resources.getPlaneYMF278B().getWidth() * zoom, frameSizeH + Resources.getPlaneYMF278B().getHeight() * zoom));
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

    private static final int[] slot1Tbl = new int[] {0, 6, 1, 7, 2, 8, 12, 13, 14, 18, 24, 19, 25, 20, 26, 30, 31, 32};
    private static final int[] slot2Tbl = new int[] {3, 9, 4, 10, 5, 11, 15, 16, 17, 21, 27, 22, 28, 23, 29, 33, 34, 35};
    private static final int[] chTbl = new int[] {0, 3, 1, 4, 2, 5, 6, 7, 8};

    public void screenInit() {
        for (int c = 0; c < newParam.channels.length; c++) {
            newParam.channels[c].note = -1;
        }
    }

    public void screenChangeParams() {
        int[][] ymf278bRegister = audio.getYMF278BRegister(chipID);
        MDChipParams.Channel nyc;
        int slot = 0;
        int slotP = 0;

        //FM
        for (int c = 0; c < 18; c++) {
            nyc = newParam.channels[c];
            for (int i = 0; i < 2; i++) {

                if (i == 0) {
                    slot = slot1Tbl[c] % 18;
                    slotP = slot1Tbl[c] / 18;
                } else {
                    slot = slot2Tbl[c] % 18;
                    slotP = slot2Tbl[c] / 18;
                }
                slot = (slot % 6) + 8 * (slot / 6);

                //AR
                nyc.inst[0 + i * 17] = ymf278bRegister[slotP][0x60 + slot] >> 4;
                //DR
                nyc.inst[1 + i * 17] = ymf278bRegister[slotP][0x60 + slot] & 0xf;
                //SL
                nyc.inst[2 + i * 17] = ymf278bRegister[slotP][0x80 + slot] >> 4;
                //RR
                nyc.inst[3 + i * 17] = ymf278bRegister[slotP][0x80 + slot] & 0xf;
                //KL
                nyc.inst[4 + i * 17] = ymf278bRegister[slotP][0x40 + slot] >> 6;
                //TL
                nyc.inst[5 + i * 17] = ymf278bRegister[slotP][0x40 + slot] & 0x3f;
                //MT
                nyc.inst[6 + i * 17] = ymf278bRegister[slotP][0x20 + slot] & 0xf;
                //AM
                nyc.inst[7 + i * 17] = ymf278bRegister[slotP][0x20 + slot] >> 7;
                //VB
                nyc.inst[8 + i * 17] = (ymf278bRegister[slotP][0x20 + slot] >> 6) & 1;
                //EG
                nyc.inst[9 + i * 17] = (ymf278bRegister[slotP][0x20 + slot] >> 5) & 1;
                //KR
                nyc.inst[10 + i * 17] = (ymf278bRegister[slotP][0x20 + slot] >> 4) & 1;
                //WS
                nyc.inst[13 + i * 17] = (ymf278bRegister[slotP][0xe0 + slot] & 7);
            }
        }

        newParam.channels[18].dda = ((ymf278bRegister[1][0xbd] >> 7) & 0x01) != 0;//DA
        newParam.channels[19].dda = ((ymf278bRegister[1][0xbd] >> 6) & 0x01) != 0;//DV
        newParam.channels[20].freq = ymf278bRegister[2][0xf8] & 0x7;//FM MIX_L
        newParam.channels[21].freq = ymf278bRegister[2][0xf8] >> 3;//FM MIX_R
        newParam.channels[22].freq = ymf278bRegister[2][0xf9] & 0x7;//PCM MIX_L
        newParam.channels[23].freq = ymf278bRegister[2][0xf9] >> 3;//PCM MIX_R

        //ConnectSelect
        for (int c = 0; c < 6; c++) {
            newParam.channels[c].dda = (ymf278bRegister[1][0x04] & (0x1 << c)) != 0;
            newParam.channels[c].inst[34] = newParam.channels[c].dda ? 1 : 0;
            newParam.channels[c].inst[35] = newParam.channels[c].dda ? 2 : 0;
            if (newParam.channels[c].dda) {
                //OP4 mode
                int ch = (c < 3) ? c * 2 : ((c - 3) * 2 + 9);
                //cnt=14
                int a = newParam.channels[ch].inst[14] * 2 + newParam.channels[ch].inst[14 + 17];
                //mod=16
                switch (a) {
                case 0:
                    newParam.channels[ch].inst[16] = 0;
                    newParam.channels[ch].inst[16 + 17] = 0;
                    newParam.channels[ch + 1].inst[16] = 0;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                case 1:
                    newParam.channels[ch].inst[16] = 0;
                    newParam.channels[ch].inst[16 + 17] = 1;
                    newParam.channels[ch + 1].inst[16] = 0;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                case 2:
                    newParam.channels[ch].inst[16] = 1;
                    newParam.channels[ch].inst[16 + 17] = 0;
                    newParam.channels[ch + 1].inst[16] = 0;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                case 3:
                    newParam.channels[ch].inst[16] = 1;
                    newParam.channels[ch].inst[16 + 17] = 0;
                    newParam.channels[ch + 1].inst[16] = 1;
                    newParam.channels[ch + 1].inst[16 + 17] = 1;
                    break;
                }
            }
        }

        int ko = audio.getYMF278BFMKeyON(chipID);

        for (int c = 0; c < 18; c++) {
            nyc = newParam.channels[c];

            int p = c / 9;
            int cadr = c % 9;

            int adr = chTbl[cadr];

            //BL
            nyc.inst[11] = (ymf278bRegister[p][0xb0 + adr] >> 2) & 7;
            //FNUM
            nyc.inst[12] = ymf278bRegister[p][0xa0 + adr]
                    + ((ymf278bRegister[p][0xb0 + adr] & 3) << 8);

            //FB
            nyc.inst[15] = (ymf278bRegister[p][0xc0 + adr] >> 1) & 7;
            //CN
            nyc.inst[14] = (ymf278bRegister[p][0xc0 + adr] & 1);
            //PAN
            nyc.inst[36] = ymf278bRegister[p][0xc0 + adr] & 0x30;
            nyc.inst[36] = ((nyc.inst[36] >> 5) & 1) | ((nyc.inst[36] >> 3) & 2); //00RL0000 -> 000000LR
            //modFlg
            int n = ymf278bRegister[p][0xc0 + adr] & 1;
            nyc.inst[16] = n == 0 ? 0 : 1;
            nyc.inst[33] = 1;

            int nt = Common.searchSegaPCMNote(nyc.inst[12] / 344.0) + (nyc.inst[11] - 4) * 12;

            boolean fouropChannel = cadr < 6;
            boolean fouropControl = fouropChannel && cadr % 2 == 0;

            // 4opのフラグはキーボードの並びと異なる
            MDChipParams.Channel ccnt = fouropChannel ? newParam.channels[(p * 3) + (cadr / 2)] : null;
            MDChipParams.Channel csub = fouropControl ? newParam.channels[c + 1] : null;
            boolean fouropMode = ccnt != null && ccnt.dda;

            int cnt2 = fouropControl ? ymf278bRegister[p][0xc3 + adr] & 1 : 0;

            boolean chmask = fouropMode && !fouropControl;

            if ((ko & (1 << (adr + p * 9))) != 0) {
                if (nyc.note != nt && !chmask) {
                    nyc.note = nt;

                    if (fouropMode) {
                        int tl1 = nyc.inst[5 + 0 * 17];
                        int tl2 = nyc.inst[5 + 1 * 17];
                        int tl3 = csub.inst[5 + 0 * 17];
                        int tl4 = csub.inst[5 + 1 * 17];

                        // cnt == 0はTL4
                        int tl = tl4;

                        int cnt = (n << 1) + cnt2;
                        switch (cnt) {
                        case 1:
                            tl = Math.min(tl2, tl4);
                            break;
                        case 2:
                            tl = Math.min(tl1, tl4);
                            break;
                        case 3:
                            tl = Math.min(tl1, Math.min(tl3, tl4));
                            break;
                        }

                        nyc.volumeL = (nyc.inst[36] & 2) != 0 ? (19 * (64 - tl) / 64) : 0;
                        nyc.volumeR = (nyc.inst[36] & 1) != 0 ? (19 * (64 - tl) / 64) : 0;
                    } else {
                        int tl1 = nyc.inst[5 + 0 * 17];
                        int tl2 = nyc.inst[5 + 1 * 17];
                        int tl = tl2;
                        if (n != 0) {
                            tl = Math.min(tl1, tl2);
                        }
                        nyc.volumeL = (nyc.inst[36] & 2) != 0 ? (19 * (64 - tl) / 64) : 0;
                        nyc.volumeR = (nyc.inst[36] & 1) != 0 ? (19 * (64 - tl) / 64) : 0;
                    }
                } else {
                    nyc.volumeL--;
                    if (nyc.volumeL < 0) nyc.volumeL = 0;
                    nyc.volumeR--;
                    if (nyc.volumeR < 0) nyc.volumeR = 0;
                }
            } else {
                nyc.note = -1;
                nyc.volumeL--;
                if (nyc.volumeL < 0) {
                    nyc.volumeL = 0;
                }
                nyc.volumeR--;
                if (nyc.volumeR < 0) {
                    nyc.volumeR = 0;
                }
            }


        }

        //Audio.resetYMF278BFMKeyON(chipID);

        int r = audio.getYMF278BRyhthmKeyON(chipID);

        //slot14 TL 0x51 HH
        //slot15 TL 0x52 TOM
        //slot16 TL 0x53 BD
        //slot17 TL 0x54 SD
        //slot18 TL 0x55 CYM

        //BD
        if ((r & 0x10) != 0) {
            newParam.channels[18].volume = 19 - ((ymf278bRegister[0][0x53] & 0x3f) >> 2);
        } else {
            newParam.channels[18].volume--;
            if (newParam.channels[18].volume < 0) newParam.channels[18].volume = 0;
        }

        //SD
        if ((r & 0x08) != 0) {
            newParam.channels[19].volume = 19 - ((ymf278bRegister[0][0x54] & 0x3f) >> 2);
        } else {
            newParam.channels[19].volume--;
            if (newParam.channels[19].volume < 0) newParam.channels[19].volume = 0;
        }

        //TOM
        if ((r & 0x04) != 0) {
            newParam.channels[20].volume = 19 - ((ymf278bRegister[0][0x52] & 0x3f) >> 2);
        } else {
            newParam.channels[20].volume--;
            if (newParam.channels[20].volume < 0) newParam.channels[20].volume = 0;
        }

        //CYM
        if ((r & 0x02) != 0) {
            newParam.channels[21].volume = 19 - ((ymf278bRegister[0][0x55] & 0x3f) >> 2);
        } else {
            newParam.channels[21].volume--;
            if (newParam.channels[21].volume < 0) newParam.channels[21].volume = 0;
        }

        //HH
        if ((r & 0x01) != 0) {
            newParam.channels[22].volume = 19 - ((ymf278bRegister[0][0x51] & 0x3f) >> 2);
        } else {
            newParam.channels[22].volume--;
            if (newParam.channels[22].volume < 0) newParam.channels[22].volume = 0;
        }

        audio.resetYMF278BRyhthmKeyON(chipID);

        //PCM
        int[] pcmKey = audio.getYMF278BPCMKeyON(chipID);
        int[] mdPCMKey = audio.getMoonDriverPCMKeyOn();
        for (int c = 23; c < 23 + 24; c++) {
            nyc = newParam.channels[c];
            //Pan
            nyc.pan = (ymf278bRegister[2][0x68 + (c - 23)] & 0xf);
            nyc.pan = (nyc.pan == 8 ? 0 :
                    (
                            (nyc.pan < 8 ? (15 - nyc.pan * 2) : 15)
                                    + ((nyc.pan > 8 ? (nyc.pan * 2 - 18) : 15) << 4)
                    ));
            //Oct
            nyc.inst[13] = (ymf278bRegister[2][0x38 + (c - 23)] >> 4);
            //F-Num
            nyc.inst[14] = (ymf278bRegister[2][0x20 + (c - 23)] >> 1) + ((ymf278bRegister[2][0x38 + (c - 23)] & 0x7) << 7);
            if (mdPCMKey == null) {
                //moonDriver以外
                //Volume
                if (pcmKey[c - 23] == 1) {
                    //note
                    nyc.note = ((nyc.inst[13] + 7) & 0xf) * 12 + Common.searchPCMNote(nyc.inst[14], 1) - 5;
                    //System.err.println("%x %x", nyc.inst[13], nyc.inst[14]);
                    nyc.volumeL = (127 - (ymf278bRegister[2][0x50 + (c - 23)] >> 1)) * (nyc.pan & 0xf) / 16 / 6;
                    nyc.volumeR = (127 - (ymf278bRegister[2][0x50 + (c - 23)] >> 1)) * (nyc.pan >> 4) / 16 / 6;
                } else {
                    if (pcmKey[c - 23] == 2) {
                        nyc.note = -1;
                    }
                    nyc.volumeL--;
                    if (nyc.volumeL < 0) nyc.volumeL = 0;
                    nyc.volumeR--;
                    if (nyc.volumeR < 0) nyc.volumeR = 0;
                }
            } else {
                //moonDriverの場合
                if (mdPCMKey[c - 23] > -1) {
                    //note
                    nyc.note = mdPCMKey[c - 23];
                    //System.err.println("%x %x", nyc.inst[13], nyc.inst[14]);
                    nyc.volumeL = (127 - (ymf278bRegister[2][0x50 + (c - 23)] >> 1)) * (nyc.pan & 0xf) / 16 / 6;
                    nyc.volumeR = (127 - (ymf278bRegister[2][0x50 + (c - 23)] >> 1)) * (nyc.pan >> 4) / 16 / 6;
                } else {
                    if (mdPCMKey[c - 23] == -1) nyc.note = -1;
                    nyc.volumeL--;
                    if (nyc.volumeL < 0) nyc.volumeL = 0;
                    nyc.volumeR--;
                    if (nyc.volumeR < 0) nyc.volumeR = 0;
                }
            }
            //AR
            nyc.inst[0] = (ymf278bRegister[2][0x98 + (c - 23)] >> 4);
            //D1
            nyc.inst[1] = (ymf278bRegister[2][0x98 + (c - 23)]) & 0xf;
            //DL
            nyc.inst[2] = (ymf278bRegister[2][0xb0 + (c - 23)] >> 4);
            //D2
            nyc.inst[3] = (ymf278bRegister[2][0xb0 + (c - 23)]) & 0xf;
            //RC
            nyc.inst[4] = (ymf278bRegister[2][0xc8 + (c - 23)] >> 4);
            //RR
            nyc.inst[5] = (ymf278bRegister[2][0xc8 + (c - 23)]) & 0xf;
            //AM
            nyc.inst[6] = (ymf278bRegister[2][0xe0 + (c - 23)]) & 0x7;
            //Vib
            nyc.inst[7] = (ymf278bRegister[2][0x80 + (c - 23)]) & 0x7;
            //Lfo
            nyc.inst[8] = (ymf278bRegister[2][0x80 + (c - 23)] >> 3) & 0x7;
            //Reverb
            nyc.inst[9] = (ymf278bRegister[2][0x38 + (c - 23)] >> 3) & 0x1;
            //LD
            nyc.inst[10] = (ymf278bRegister[2][0x50 + (c - 23)] & 0x1);
            //TL
            nyc.inst[11] = (ymf278bRegister[2][0x50 + (c - 23)] >> 1);
            //Wav
            nyc.inst[12] = (ymf278bRegister[2][0x08 + (c - 23)]) + ((ymf278bRegister[2][0x20 + (c - 23)] & 0x1) << 8);
        }
        audio.resetYMF278BPCMKeyON(chipID);
    }

    public void screenDrawParams() {
        boolean YMF278BType = (chipID == 0)
                ? parent.setting.getYMF278BType()[0].getUseReal()[0]
                : parent.setting.getYMF278BType()[1].getUseReal()[0];
        int YMF278BSoundLocation = (chipID == 0)
                ? parent.setting.getYMF278BType()[0].getRealChipInfo()[0].getSoundLocation()
                : parent.setting.getYMF278BType()[1].getRealChipInfo()[0].getSoundLocation();
        int tp = YMF278BType ? 1 : 0;

        MDChipParams.Channel oyc;
        MDChipParams.Channel nyc;

        //FM
        for (int c = 0; c < 18; c++) {

            oyc = oldParam.channels[c];
            nyc = newParam.channels[c];

            for (int i = 0; i < 2; i++) {
                DrawBuff.susFlag(frameBuffer, 81 + i * 34, c * 2 + 2, 1, oyc.inst[16 + i * 17], nyc.inst[16 + i * 17]);
                DrawBuff.font4Int2(frameBuffer, 336 + 4 + i * 136, c * 8 + 8, 0, 0, oyc.inst[0 + i * 17], nyc.inst[0 + i * 17]);//AR
                DrawBuff.font4Int2(frameBuffer, 336 + 12 + i * 136, c * 8 + 8, 0, 0, oyc.inst[1 + i * 17], nyc.inst[1 + i * 17]);//DR
                DrawBuff.font4Int2(frameBuffer, 336 + 20 + i * 136, c * 8 + 8, 0, 0, oyc.inst[2 + i * 17], nyc.inst[2 + i * 17]);//SL
                DrawBuff.font4Int2(frameBuffer, 336 + 28 + i * 136, c * 8 + 8, 0, 0, oyc.inst[3 + i * 17], nyc.inst[3 + i * 17]);//RR

                DrawBuff.font4Int2(frameBuffer, 336 + 40 + i * 136, c * 8 + 8, 0, 0, oyc.inst[4 + i * 17], nyc.inst[4 + i * 17]);//KL
                DrawBuff.font4Int2(frameBuffer, 336 + 48 + i * 136, c * 8 + 8, 0, 0, oyc.inst[5 + i * 17], nyc.inst[5 + i * 17]);//TL

                DrawBuff.font4Int2(frameBuffer, 336 + 60 + i * 136, c * 8 + 8, 0, 0, oyc.inst[6 + i * 17], nyc.inst[6 + i * 17]);//MT

                DrawBuff.font4Int2(frameBuffer, 336 + 72 + i * 136, c * 8 + 8, 0, 0, oyc.inst[7 + i * 17], nyc.inst[7 + i * 17]);//AM
                DrawBuff.font4Int2(frameBuffer, 336 + 80 + i * 136, c * 8 + 8, 0, 0, oyc.inst[8 + i * 17], nyc.inst[8 + i * 17]);//VB
                DrawBuff.font4Int2(frameBuffer, 336 + 88 + i * 136, c * 8 + 8, 0, 0, oyc.inst[9 + i * 17], nyc.inst[9 + i * 17]);//EG
                DrawBuff.font4Int2(frameBuffer, 336 + 96 + i * 136, c * 8 + 8, 0, 0, oyc.inst[10 + i * 17], nyc.inst[10 + i * 17]);//KR
                DrawBuff.font4Int2(frameBuffer, 336 + 108 + i * 136, c * 8 + 8, 0, 0, oyc.inst[13 + i * 17], nyc.inst[13 + i * 17]);//WS
            }

            DrawBuff.font4Int2(frameBuffer, 336 + 4 * 65, c * 8 + 8, 0, 0, oyc.inst[11], nyc.inst[11]);//BL
            DrawBuff.font4Hex12Bit(frameBuffer, 336 + 4 * 69, c * 8 + 8, 0, oyc.inst[12], nyc.inst[12]);//F-Num
            DrawBuff.font4Int2(frameBuffer, 336 + 4 * 73, c * 8 + 8, 0, 0, oyc.inst[14], nyc.inst[14]);//CN
            DrawBuff.font4Int2(frameBuffer, 336 + 4 * 76, c * 8 + 8, 0, 0, oyc.inst[15], nyc.inst[15]);//FB
            int dmy = 99;
            DrawBuff.Pan(frameBuffer, 24, 8 + c * 8, oyc.inst[36], nyc.inst[36], dmy, 0);
            DrawBuff.keyBoard(frameBuffer, c, oyc.note, nyc.note, tp);
            DrawBuff.VolumeXY(frameBuffer, 64, c * 2 + 2, 1, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.VolumeXY(frameBuffer, 64, c * 2 + 3, 1, oyc.volumeR, nyc.volumeR, tp);
            DrawBuff.ChYMF278B(frameBuffer, c, oyc.mask, nyc.mask, tp);

            //DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 37, (c / 3) * 2 + 24,oyc.inst[0], nyc.inst[0]);
            //DrawBuff.SUSFlag(frameBuffer, (c % 3) * 16 + 41, (c / 3) * 2 + 24,oyc.inst[1], nyc.inst[1]);
            //DrawBuff.SUSFlag(frameBuffer, (c % 3) * 16 + 44, (c / 3) * 2 + 24,oyc.inst[2], nyc.inst[2]);
            //DrawBuff.drawInstNumber(frameBuffer, (c % 3) * 16 + 46, (c / 3) * 2 + 24,oyc.inst[3], nyc.inst[3]);

        }

        for (int c = 0; c < 6; c++) {
            //CS
            DrawBuff.drawNESSw(frameBuffer, 79 * 4 + c * 4, 19 * 8, oldParam.channels[c].dda, newParam.channels[c].dda);
            int ch = (c < 3) ? c * 2 : ((c - 3) * 2 + 9);
            DrawBuff.Kakko(frameBuffer, 4 * 80, (c < 3 ? 0 : 24) + c * 16 + 8, 0, oldParam.channels[c].inst[34], newParam.channels[c].inst[34]);
            DrawBuff.Kakko(frameBuffer, 4 * 162, (c < 3 ? 0 : 24) + c * 16 + 8, 0, oldParam.channels[c].inst[35], newParam.channels[c].inst[35]);
        }
        DrawBuff.drawNESSw(frameBuffer, 88 * 4, 19 * 8, oldParam.channels[18].dda, newParam.channels[18].dda);//DA
        DrawBuff.drawNESSw(frameBuffer, 92 * 4, 19 * 8, oldParam.channels[19].dda, newParam.channels[19].dda);//DV
        DrawBuff.font4Int1(frameBuffer, 109 * 4, 19 * 8, 0, oldParam.channels[20].freq, newParam.channels[20].freq);//FM MIX_L
        DrawBuff.font4Int1(frameBuffer, 111 * 4, 19 * 8, 0, oldParam.channels[21].freq, newParam.channels[21].freq);//FM_MI_R
        DrawBuff.font4Int1(frameBuffer, 100 * 4, 19 * 8, 0, oldParam.channels[22].freq, newParam.channels[22].freq);//PCM MIX_L
        DrawBuff.font4Int1(frameBuffer, 102 * 4, 19 * 8, 0, oldParam.channels[23].freq, newParam.channels[23].freq);//PCM_MI_R

        for (int c = 18; c < 23; c++) {
            DrawBuff.ChYMF278B(frameBuffer, c, oldParam.channels[c].mask, newParam.channels[c].mask, tp);
            DrawBuff.VolumeXY(frameBuffer, 12 + (c - 18) * 13, 19 * 2, 0, oldParam.channels[c].volume, newParam.channels[c].volume, tp);
        }

        //PCM
        for (int c = 23; c < 23 + 24; c++) {
            oyc = oldParam.channels[c];
            nyc = newParam.channels[c];
            DrawBuff.PanType2(frameBuffer, c - 4, oyc.pan, nyc.pan, 0);
            DrawBuff.font4Int2(frameBuffer, 516 + 0, (c - 3) * 8, 0, 0, oyc.inst[0], nyc.inst[0]);//AR
            DrawBuff.font4Int2(frameBuffer, 516 + 8, (c - 3) * 8, 0, 0, oyc.inst[1], nyc.inst[1]);//D1
            DrawBuff.font4Int2(frameBuffer, 516 + 16, (c - 3) * 8, 0, 0, oyc.inst[2], nyc.inst[2]);//DL
            DrawBuff.font4Int2(frameBuffer, 516 + 24, (c - 3) * 8, 0, 0, oyc.inst[3], nyc.inst[3]);//D2
            DrawBuff.font4Int2(frameBuffer, 516 + 32, (c - 3) * 8, 0, 0, oyc.inst[4], nyc.inst[4]);//RC
            DrawBuff.font4Int2(frameBuffer, 516 + 40, (c - 3) * 8, 0, 0, oyc.inst[5], nyc.inst[5]);//RR
            DrawBuff.font4Int2(frameBuffer, 516 + 48, (c - 3) * 8, 0, 0, oyc.inst[6], nyc.inst[6]);//AM
            DrawBuff.font4Int2(frameBuffer, 516 + 56, (c - 3) * 8, 0, 0, oyc.inst[7], nyc.inst[7]);//VB
            DrawBuff.font4Int2(frameBuffer, 516 + 64, (c - 3) * 8, 0, 0, oyc.inst[8], nyc.inst[8]);//Lfo
            DrawBuff.font4Int2(frameBuffer, 516 + 72, (c - 3) * 8, 0, 0, oyc.inst[9], nyc.inst[9]);//RV
            DrawBuff.font4Int2(frameBuffer, 516 + 80, (c - 3) * 8, 0, 0, oyc.inst[10], nyc.inst[10]);//LD
            DrawBuff.font4Int2(frameBuffer, 516 + 88, (c - 3) * 8, 0, 3, oyc.inst[11], nyc.inst[11]);//TL
            DrawBuff.font4Int2(frameBuffer, 516 + 100, (c - 3) * 8, 0, 3, oyc.inst[12], nyc.inst[12]);//WV
            DrawBuff.font4Int2(frameBuffer, 516 + 112, (c - 3) * 8, 0, 0, oyc.inst[13], nyc.inst[13]);//Oct
            DrawBuff.font4Hex12Bit(frameBuffer, 516 + 128, (c - 3) * 8, 0, oyc.inst[14], nyc.inst[14]);//F-Num

            DrawBuff.ChYMF278B(frameBuffer, c, oyc.mask, nyc.mask, tp);
            DrawBuff.VolumeXY(frameBuffer, 113, (c - 3) * 2 + 0, 1, oyc.volumeL, nyc.volumeL, tp);
            DrawBuff.VolumeXY(frameBuffer, 113, (c - 3) * 2 + 1, 1, oyc.volumeR, nyc.volumeR, tp);
            DrawBuff.KeyBoardToYMF278BPCM(frameBuffer, c - 4, oyc.note, nyc.note, tp);
        }

    }

    private MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            int px = ev.getX() / zoom;
            int py = ev.getY() / zoom;
            int ch;
            //上部のラベル行の場合は何もしない
            if (py < 1 * 8) {
                //但しchをクリックした場合はマスク反転
                if (px < 8) {
                    for (ch = 0; ch < 47; ch++) {
                        if (newParam.channels[ch].mask)
                            parent.resetChannelMask(EnmChip.YMF278B, chipID, ch);
                        else
                            parent.setChannelMask(EnmChip.YMF278B, chipID, ch);
                    }
                }
                return;
            }

            //鍵盤 FM & RHM
            ch = (py / 8) - 1;
            if (ch < 0) return;

            if (ch == 18) {
                int x = (px / 4 - 10);
                if (x < 0) return;
                x /= 13;
                if (x > 4) return;
                ch += x;
            } else if (ch > 18) {
                ch += 4;
            }

            if (ev.getButton() == MouseEvent.BUTTON1) {
                if (ch < 18) {
                    parent.getInstCh(EnmChip.YMF278B, ch, chipID);
                }

                //マスク
                parent.setChannelMask(EnmChip.YMF278B, chipID, ch);
                return;
            }

            //マスク解除
            for (ch = 0; ch < 47; ch++) parent.resetChannelMask(EnmChip.YMF278B, chipID, ch);
        }
    };

    private void initializeComponent() {
//            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(frmYMF278B));
        this.pbScreen = new JPanel();
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).BeginInit();

        //
        // pbScreen
        //
        this.image = mdplayer.properties.Resources.getPlaneYMF278B();
        this.pbScreen.setLocation(new Point(0, 0));
        this.pbScreen.setName("pbScreen");
        this.pbScreen.setPreferredSize(new Dimension(656, 352));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // frmYMF278B
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(656, 352));
        this.getContentPane().add(this.pbScreen);
//        this.FormBorderStyle = JFormBorderStyle.FixedSingle;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
//        this.MaximizeBox = false;
        this.setName("frmYMF278B");
        this.setTitle("YMF278B");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
//            this.ResumeLayout(false);
    }

    BufferedImage image;
    public JPanel pbScreen;
}
