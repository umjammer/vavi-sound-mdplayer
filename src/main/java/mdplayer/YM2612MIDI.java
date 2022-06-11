package mdplayer;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.swing.JOptionPane;

import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.StreamReader;
import dotnet4j.io.StreamWriter;
import mdplayer.Common.EnmChip;
import mdplayer.form.sys.frmMain;
import mdsound.MDSound;


public class YM2612MIDI {
    private int latestNoteNumberMONO = -1;
    private int[] latestNoteNumber = new int[] {-1, -1, -1, -1, -1, -1};

    private frmMain parent;
    private MDSound mdsMIDI;
    public MDChipParams newParam;

    private int[][] _noteLog = new int[][] {new int[100], new int[100], new int[100], new int[100], new int[100], new int[100]};

    public int[][] getNoteLog() {
        return _noteLog;
    }

    public void setNoteLog(int[][] value) {
        _noteLog = value;
    }

    private int[] _noteLogPtr = new int[6];

    public int[] getNoteLogPtr() {
        return _noteLogPtr;
    }

    public void setNoteLogPtr(int[] value) {
        _noteLogPtr = value;
    }

    public YM2612MIDI(frmMain parent, mdsound.MDSound mdsMIDI, MDChipParams newParam) {
        this.parent = parent;
        this.mdsMIDI = mdsMIDI;
        this.newParam = newParam;

        for (int ch = 0; ch < 6; ch++) {
            for (int n = 0; n < 100; n++) _noteLog[ch][n] = -1;
            _noteLogPtr[ch] = 0;
            if (parent.setting.getMidiKbd().getTones() != null && parent.setting.getMidiKbd().getTones()[ch] != null)
                voiceCopyChFromTone(ch, parent.setting.getMidiKbd().getTones()[ch]);
        }
    }

    public void close() {
        setTonesToSettng();
    }

    private int noteONMONO(int noteNumber) {
        int fnum = Tables.FmFNum[(noteNumber % 12) + 36];
        int oct = noteNumber / 12 - 1;
        oct = Math.min(Math.max(oct, 0), 7);

        int ch = parent.setting.getMidiKbd().getUseMONOChannel();
        if (ch < 0 || ch > 5) return -1;

        mdsMIDI.writeYM2612((byte) 0, (byte) (ch / 3), (byte) (0xa4 + (ch % 3)), (byte) (((fnum & 0x700) >> 8) | (oct << 3)));
        mdsMIDI.writeYM2612((byte) 0, (byte) (ch / 3), (byte) (0xa0 + (ch % 3)), (byte) (fnum & 0xff));

        mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0x00 + ch + (ch / 3)));
        mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0xf0 + ch + (ch / 3)));
        latestNoteNumberMONO = noteNumber;

        return ch;
    }

    private void noteOFFMONO(int noteNumber) {
        int ch = parent.setting.getMidiKbd().getUseMONOChannel();
        if (ch < 0 || ch > 5) return;

        if (noteNumber == latestNoteNumberMONO)
            mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0x00 + ch + (ch / 3)));
    }

    private int noteON(int noteNumber) {
        if (parent.setting.getMidiKbd().getIsMONO()) {
            return noteONMONO(noteNumber);
        }

        int fnum = Tables.FmFNum[(noteNumber % 12) + 36];
        int oct = noteNumber / 12 - 1;
        oct = Math.min(Math.max(oct, 0), 7);

        boolean sw = false;
        int ch = 0;
        for (; ch < 6; ch++) {
            if (latestNoteNumber[ch] != -1) continue;
            if (!parent.setting.getMidiKbd().getUseChannel()[ch]) continue;
            sw = true;
            break;
        }
        if (!sw) return -1;

        mdsMIDI.writeYM2612((byte) 0, (byte) (ch / 3), (byte) (0xa4 + (ch % 3)), (byte) (((fnum & 0x700) >> 8) | (oct << 3)));
        mdsMIDI.writeYM2612((byte) 0, (byte) (ch / 3), (byte) (0xa0 + (ch % 3)), (byte) (fnum & 0xff));

        mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0x00 + ch + (ch / 3)));
        mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0xf0 + ch + (ch / 3)));
        latestNoteNumber[ch] = noteNumber;

        return ch;
    }

    private void noteOFF(int noteNumber) {
        if (parent.setting.getMidiKbd().getIsMONO()) {
            noteOFFMONO(noteNumber);
            return;
        }

        boolean sw = false;
        int ch = 0;
        for (; ch < 6; ch++) {
            if (latestNoteNumber[ch] != noteNumber) continue;
            sw = true;
            break;
        }
        if (!sw) return;

        latestNoteNumber[ch] = -1;
        mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0x00 + ch + (ch / 3)));
    }

    private void voiceCopy() {
        int[][] reg = Audio.getFMRegister(0);// chipRegister.fmRegisterYM2612[0];
        if (reg == null) return;

        for (int i = 0; i < 6; i++) {
            voiceCopyCh(0, i, reg);
        }
    }

    private void voiceCopyCh(int src, int des, int[][] reg) {

        for (int i = 0x30; i < 0xa0; i += 0x10) {
            for (int j = 0; j < 4; j++) {
                mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (i + j * 4 + (des % 3)), (byte) reg[src / 3][i + j * 4 + (src % 3)]);
            }
        }

        mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0xb0 + (des % 3)), (byte) reg[src / 3][0xb0 + (src % 3)]);
        mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) ((0xb4 + (des % 3)) | 0xc0), (byte) reg[src / 3][0xb4 + (src % 3)]);

        int alg = (byte) reg[src / 3][0xb0 + (src % 3)] & 0x7;
        byte[] algTl = new byte[] {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};
        int[] tls = new int[4];
        int max = 127;

        for (int j = 0; j < 4; j++) {
            tls[j] = (byte) reg[src / 3][0x40 + j * 4 + (src % 3)];
            if ((algTl[alg] & (1 << j)) != 0)
                max = Math.min(max, tls[j]);
        }

        for (int j = 0; j < 4; j++) {
            if ((algTl[alg] & (1 << j)) != 0)
                mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x40 + j * 4 + (des % 3)), (byte) (tls[j] - max));
        }
    }

    private void voiceCopyChFromOPM(int src, int des, int[] reg) {
        for (int i = 0; i < 4; i++) {
            int opn = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
            int opm = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));

            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x50 + opn + (des % 3)), (byte) (reg[0x80 + opm + src] & 0xdf));//AR + KS
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x60 + opn + (des % 3)), (byte) (reg[0xa0 + opm + src] & 0x9f));//DR + AM
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x70 + opn + (des % 3)), (byte) (reg[0xc0 + opm + src] & 0x1f));//SR
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x80 + opn + (des % 3)), (byte) (reg[0xe0 + opm + src] & 0xff));//RR + SL
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x40 + opn + (des % 3)), (byte) (reg[0x60 + opm + src] & 0x7f));//TL
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x30 + opn + (des % 3)), (byte) (reg[0x40 + opm + src] & 0x7f));//ML + DT
        }

        mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0xb0 + (des % 3)), (byte) (reg[0x20 + src] & 0x3f));//AL + FB

        int alg = (byte) reg[0x20 + src] & 0x7;
        byte[] algTl = new byte[] {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};
        int[] tls = new int[4];
        int max = 127;

        for (int j = 0; j < 4; j++) {
            tls[j] = (byte) (reg[0x60 + j * 8 + src] & 0x7f);
            if ((algTl[alg] & (1 << j)) != 0)
                max = Math.min(max, tls[j]);
        }

        for (int j = 0; j < 4; j++) {
            if ((algTl[alg] & (1 << j)) != 0)
                mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x40 + j * 4 + (des % 3)), (byte) (tls[j] - max));
        }
    }

    private void voiceCopyChFromTone(int des, Tone tone) {
        if (tone == null) return;
        if (des < 0 || des > 5) return;

        for (int i = 0; i < 4; i++) {
            int opn = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));

            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x50 + opn + (des % 3)), (byte) ((tone.ops[i].ar & 0x1f) + ((tone.ops[i].ks & 0x3) << 6)));//AR + KS
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x60 + opn + (des % 3)), (byte) ((tone.ops[i].dr & 0x1f) + ((tone.ops[i].am & 0x1) << 7)));//DR + AM
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x70 + opn + (des % 3)), (byte) ((tone.ops[i].sr & 0x1f)));//SR
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x80 + opn + (des % 3)), (byte) ((tone.ops[i].rr & 0xf) + ((tone.ops[i].sl & 0xf) << 4)));//RR + SL
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x40 + opn + (des % 3)), (byte) ((tone.ops[i].tl & 0x7f)));//TL
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x30 + opn + (des % 3)), (byte) ((tone.ops[i].ml & 0xf) + ((tone.ops[i].dt & 0x7) << 4)));//ML + DT
            mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0x90 + opn + (des % 3)), (byte) ((tone.ops[i].sg & 0xf)));//SG
        }

        mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0xb0 + (des % 3)), (byte) ((tone.al & 0x7) + ((tone.fb & 0x7) << 3)));//AL + FB
        mdsMIDI.writeYM2612((byte) 0, (byte) (des / 3), (byte) (0xb4 + (des % 3)), (byte) (0xc0 + (tone.pms & 0x7) + ((tone.ams & 0x3) << 4)));//PMS + AMS
    }

    private Tone voiceCopyChToTone(int des, String name) {
        Tone tone = new Tone();
        int[][] reg = mdsMIDI.ReadYM2612Register((byte) 0);

        for (int i = 0; i < 4; i++) {
            int opn = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));

            tone.ops[i].ar = reg[des / 3][0x50 + opn + (des % 3)] & 0x1f;//AR
            tone.ops[i].ks = (reg[des / 3][0x50 + opn + (des % 3)] & 0xc0) >> 6;//KS
            tone.ops[i].dr = reg[des / 3][0x60 + opn + (des % 3)] & 0x1f;//DR
            tone.ops[i].am = (reg[des / 3][0x60 + opn + (des % 3)] & 0x80) >> 7;//AM
            tone.ops[i].sr = reg[des / 3][0x70 + opn + (des % 3)] & 0x1f;//SR
            tone.ops[i].rr = reg[des / 3][0x80 + opn + (des % 3)] & 0xf;//RR
            tone.ops[i].sl = (reg[des / 3][0x80 + opn + (des % 3)] & 0xf0) >> 4;//SL
            tone.ops[i].tl = reg[des / 3][0x40 + opn + (des % 3)] & 0x7f;//TL
            tone.ops[i].ml = reg[des / 3][0x30 + opn + (des % 3)] & 0xf;//ML
            tone.ops[i].dt = (reg[des / 3][0x30 + opn + (des % 3)] & 0x70) >> 4;//DT
            tone.ops[i].dt2 = 0;
        }

        tone.al = reg[des / 3][0xb0 + (des % 3)] & 0x7;//AL
        tone.fb = (reg[des / 3][0xb0 + (des % 3)] & 0x38) >> 3;//FB
        tone.ams = 0;
        tone.pms = 0;
        tone.name = name;

        return tone;
    }

    private Tone voiceCopyToneToTone(Tone src, String name) {
        Tone des = new Tone();

        for (int i = 0; i < 4; i++) {
            des.ops[i].ar = src.ops[i].ar;//AR
            des.ops[i].ks = src.ops[i].ks;//KS
            des.ops[i].dr = src.ops[i].dr;//DR
            des.ops[i].am = src.ops[i].am;//AM
            des.ops[i].sr = src.ops[i].sr;//SR
            des.ops[i].rr = src.ops[i].rr;//RR
            des.ops[i].sl = src.ops[i].sl;//SL
            des.ops[i].tl = src.ops[i].tl;//TL
            des.ops[i].ml = src.ops[i].ml;//ML
            des.ops[i].dt = src.ops[i].dt;//DT
            des.ops[i].dt2 = 0;
        }

        des.al = src.al;//AL
        des.fb = src.fb;//FB
        des.ams = 0;
        des.pms = 0;
        des.name = name;

        return des;
    }

    public void setVoiceFromChipRegister(EnmChip chip, int chipID, int ch) {
        if (chip == EnmChip.YM2612 || chip == EnmChip.YM2608 || chip == EnmChip.YM2610 || chip == EnmChip.YM2203) {
            int[][] srcRegs = null;
            if (chip == EnmChip.YM2612) {
                srcRegs = Audio.getFMRegister(chipID);
            } else if (chip == EnmChip.YM2608) {
                srcRegs = Audio.getYM2608Register(chipID);
            } else if (chip == EnmChip.YM2610) {
                srcRegs = Audio.getYM2610Register(chipID);
            } else if (chip == EnmChip.YM2203) {
                int[] sReg = Audio.getym2203Register(chipID);
                srcRegs = new int[][] {sReg, null};
            }
            for (int i = 0; i < 6; i++) {
                if (parent.setting.getMidiKbd().getUseChannel()[i]) {
                    voiceCopyCh(ch, i, srcRegs);
                }
            }
        } else if (chip == EnmChip.YM2151) {
            int[] reg = Audio.getYM2151Register(chipID);
            for (int i = 0; i < 6; i++) {
                if (parent.setting.getMidiKbd().getUseChannel()[i]) {
                    voiceCopyChFromOPM(ch, i, reg);
                }
            }
        }
    }

    public void allNoteOff() {
        for (int ch = 0; ch < 6; ch++) {
            mdsMIDI.writeYM2612((byte) 0, (byte) 0, (byte) 0x28, (byte) (0x00 + ch + (ch / 3)));
        }
    }

    public void setMode(int m) {
        switch (m) {
        case 0:
            //MONO
            for (int ch = 0; ch < 6; ch++) {
                parent.setting.getMidiKbd().getUseChannel()[ch] = false;
                if (ch == parent.setting.getMidiKbd().getUseMONOChannel()) {
                    parent.setting.getMidiKbd().getUseChannel()[ch] = true;
                }
            }
            parent.setting.getMidiKbd().setIsMONO(true);
            break;
        default:
            //POLY
            parent.setting.getMidiKbd().setIsMONO(false);
            break;
        }
    }

    public void selectChannel(int ch) {
        if (parent.setting.getMidiKbd().getIsMONO()) {
            parent.setting.getMidiKbd().setUseMONOChannel(ch);
            setMode(0);
        } else {
            parent.setting.getMidiKbd().getUseChannel()[ch] = !parent.setting.getMidiKbd().getUseChannel()[ch];
        }
    }

    public void log2MML(int ch) {
        final String[] tblNote = {"c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b"};
        int ptr = _noteLogPtr[ch];

        //解析開始位置を調べる
        do {
            ptr--;
            if (ptr < 0) ptr = _noteLog[ch].length - 1;

            if (ptr == _noteLogPtr[ch]) {
                ptr = _noteLogPtr[ch] - 1;
                if (ptr < 0) ptr = _noteLog[ch].length - 1;
                break;
            }
        } while (_noteLog[ch][ptr] != -1);
        ptr++;
        if (ptr == _noteLog[ch].length) ptr = 0;

        //ログが無い場合は処理終了
        if (_noteLog[ch][ptr] == -1) return;

        //解析開始
        StringBuilder mml = new StringBuilder("o");

        //オクターブコマンド
        int oct = _noteLog[ch][ptr] / 12;
        mml.append(oct + 1);

        do {
            int o = _noteLog[ch][ptr] / 12;
            int n = _noteLog[ch][ptr] % 12;

            //相対オクターブコマンドの解析
            int s = oct - o;
            if (s < 0) {
                do {
                    mml.append(">");
                    oct++;
                } while (oct != o);
            } else if (s > 0) {
                do {
                    mml.append("<");
                    oct--;
                } while (oct != o);
            }

            //ノートコマンド
            mml.append(tblNote[n]);

            ptr++;
        } while (ptr != _noteLogPtr[ch]);

        //クリップボードにMMLをセット
        Common.setClipboard(mml.toString());
    }

    public void log2MML66(int ch) {
        final String[] tblNote = {"c", "c+", "d", "d+", "e", "f", "f+", "g", "g+", "a", "a+", "b"};
        int ptr = _noteLogPtr[ch];

        //解析開始位置を調べる
        do {
            ptr--;
            if (ptr < 0) ptr = _noteLog[ch].length - 1;

            if (ptr == _noteLogPtr[ch]) {
                ptr = _noteLogPtr[ch] - 1;
                if (ptr < 0) ptr = _noteLog[ch].length - 1;
                break;
            }
        } while (_noteLog[ch][ptr] != -1);
        ptr++;
        if (ptr == _noteLog[ch].length) ptr = 0;

        if (ptr == _noteLogPtr[ch]) return;

        //解析開始
        StringBuilder mml = new StringBuilder();

        //オクターブのみ取得
        int oct = _noteLog[ch][ptr] / 12;

        do {
            int o = _noteLog[ch][ptr] / 12;
            int n = _noteLog[ch][ptr] % 12;

            //相対オクターブコマンドの解析
            int s = oct - o;
            if (s < 0) {
                do {
                    mml.append(">");
                    oct++;
                } while (oct != o);
            } else if (s > 0) {
                do {
                    mml.append("<");
                    oct--;
                } while (oct != o);
            }

            //ノートコマンド
            mml.append(tblNote[n]);

            ptr++;
            if (ptr == _noteLog[ch].length) ptr = 0;
        } while (ptr != _noteLogPtr[ch]);

        //クリップボードにMMLをセット
        Common.setClipboard(mml.toString());
        Common.sendKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);

        clearNoteLog(ch);
    }

    public void clearNoteLog() {
        for (int ch = 0; ch < 6; ch++) {
            clearNoteLog(ch);
        }
    }

    public void clearNoteLog(int ch) {
        for (int i = 0; i < 10; i++) {
            newParam.ym2612Midi.noteLog[ch][i] = -1;
        }

        for (int i = 0; i < 100; i++) {
            _noteLog[ch][i] = -1;
        }
        _noteLogPtr[ch] = 0;
    }

    public void midiIn_MessageReceived(MidiMessage e) {
        try {

            if (e.getStatus() == ShortMessage.NOTE_ON) {
                ShortMessage noe = (ShortMessage) e;

                if (noe.getData2() == 0) {
                    noteOFF(noe.getData1());
                    return;
                }

                int ch = noteON(noe.getData1());

                if (ch != -1) {
                    int n = (noe.getData1() % 12) + (noe.getData1() / 12 - 1) * 12;
                    n = Math.max(Math.min(n, 12 * 8 - 1), 0);
                    _noteLog[ch][_noteLogPtr[ch]] = n;
                    int p = _noteLogPtr[ch] - 9;
                    if (p < 0) p += 100;
                    for (int i = 0; i < 10; i++) {
                        newParam.ym2612Midi.noteLog[ch][i] = _noteLog[ch][p];
                        p++;
                        if (p == 100) p = 0;
                    }
                    _noteLogPtr[ch]++;
                    if (_noteLogPtr[ch] == 100) _noteLogPtr[ch] = 0;
                }
            } else if (e.getStatus() == ShortMessage.NOTE_OFF) {
                ShortMessage ne = (ShortMessage) e;
                noteOFF(ne.getData1());
            } else if (e.getStatus() == ShortMessage.CONTROL_CHANGE) {
                int cc = (int) (((ShortMessage) e).getData1());
                Setting.MidiKbd mk = parent.setting.getMidiKbd();
                if (cc == mk.getMidiCtrl_CopyToneFromYM2612Ch1()) voiceCopy();
                if (cc == mk.getMidiCtrl_CopySelecttingLogToClipbrd()) {
                    if (parent.setting.getMidiKbd().getIsMONO())
                        log2MML66(parent.setting.getMidiKbd().getUseMONOChannel());
                }
                if (cc == mk.getMidiCtrl_DelOneLog()) {
                    if (parent.setting.getMidiKbd().getIsMONO()) {
                        int ch = parent.setting.getMidiKbd().getUseMONOChannel();
                        int ptr = _noteLogPtr[ch];
                        ptr--;
                        if (ptr < 0) ptr += 100;
                        _noteLog[ch][ptr] = -1;
                        _noteLogPtr[ch] = ptr;
                        int p = _noteLogPtr[ch] - 10;
                        if (p < 0) p += 100;
                        for (int i = 0; i < 10; i++) {
                            newParam.ym2612Midi.noteLog[ch][i] = _noteLog[ch][p];
                            p++;
                            if (p == 100) p = 0;
                        }
                    }
                }
                if (cc == mk.getMidiCtrl_Fadeout()) {
                    parent.fadeout();
                }
                if (cc == mk.getMidiCtrl_Fast()) {
                    parent.ff();
                }
                if (cc == mk.getMidiCtrl_Next()) {
                    parent.next();
                }
                if (cc == mk.getMidiCtrl_Pause()) {
                    parent.pause();
                }
                if (cc == mk.getMidiCtrl_Play()) {
                    parent.play();
                }
                if (cc == mk.getMidiCtrl_Previous()) {
                    parent.prev();
                }
                if (cc == mk.getMidiCtrl_Slow()) {
                    parent.slow();
                }
                if (cc == mk.getMidiCtrl_Stop()) {
                    parent.stop();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(null,
                    String.format("Exception:\n%s\nStackTrace:\n%s\n", ex.getMessage(), ex.getStackTrace())
                    , "ERROR"
                    , JOptionPane.DEFAULT_OPTION
                    , JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void setTonesToSettng() {
        parent.setting.getMidiKbd().setTones(new Tone[6]);
        for (int ch = 0; ch < 6; ch++) {
            parent.setting.getMidiKbd().getTones()[ch] = voiceCopyChToTone(ch, "");
        }
    }

    public void setTonesFromSettng() {
        for (int ch = 0; ch < 6; ch++) {
            voiceCopyChFromTone(ch, parent.setting.getMidiKbd().getTones()[ch]);
        }
    }

    /**
     * @param tp 1 origin
     */
    public void saveTonePallet(String fn, int tp, TonePallet tonePallet) {
        if (tp == 1) {
            tonePallet.save(fn);
            return;
        }

        Charset enc = StandardCharsets.UTF_8;
        if (tp == 2) enc = Charset.defaultCharset();

        try (StreamWriter sw = new StreamWriter(new FileStream(fn, FileMode.CreateNew), enc)) {
            int n = 0;
            int row = 10;
            for (Tone t : tonePallet.getLstTone()) {
                String[] toneText = null;
                switch (tp) {
                case 2:
                    toneText = makeToneTextForMml2vgm(t, n++);
                    break;
                case 3:
                    toneText = makeToneTextForFMP7(t, n++);
                    break;
                case 4:
                    toneText = makeToneTextForNRTDRV(t, n++);
                    break;
                case 5:
                    toneText = makeToneTextForMXDRV(t, n++);
                    break;
                case 6:
                    toneText = makeToneTextForMUSICLALF(t, n++);
                    break;
                }

                if (tp != 6) {
                    for (String text : toneText) {
                        sw.writeLine(text);
                    }
                } else {
                    for (String text : toneText) {
                        sw.writeLine(text.replace("[ROW]", String.valueOf(row)));
                        row += 10;
                    }
                }
            }
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    private String[] makeToneTextForMml2vgm(Tone t, int n) {
        List<String> tt = new ArrayList<>();
        tt.add(String.format("%s", t.name));
        tt.add(String.format("'@ N %03d", n));
        tt.add("   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM  SSG - EG");
        for (Tone.Op op : t.ops) {
            tt.add(String.format("'@ %03d %03d %03d %03d %03d %03d %03d %03d %03d %03d %03d"
                    , op.ar, op.dr, op.sr, op.rr, op.sl, op.tl, op.ks, op.ml, op.dt, op.am, op.sg
            ));
        }
        tt.add("   AL  FB");
        tt.add(String.format("'@ %03d %03d", t.al, t.fb));
        tt.add("");

        return tt.toArray(new String[0]);
    }

    private String[] makeToneTextForFMP7(Tone t, int n) {
        List<String> tt = new ArrayList<>();

        tt.add(String.format("%s", t.name));
        tt.add(String.format("'@ FA %03d", n));
        tt.add("   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM");
        for (Tone.Op op : t.ops) {
            tt.add(String.format("'@ %03d %03d %03d %03d %03d %03d %03d %03d %03d %03d "
                    , op.ar, op.dr, op.sr, op.rr, op.sl, op.tl, op.ks, op.ml, op.dt, op.am
            ));
        }
        tt.add("   AL  FB");
        tt.add(String.format("'@ %03d %03d", t.al, t.fb));
        tt.add("");

        return tt.toArray(new String[0]);
    }

    private String[] makeToneTextForNRTDRV(Tone t, int n) {
        List<String> tt = new ArrayList<>();
        tt.add(String.format("@%d {{ ;%s", n, t.name));
        tt.add(";PAN ALG FB  OP");
        tt.add(String.format(" 003,%03d,%03d,015", t.al, t.fb));
        tt.add("; AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME");
        for (Tone.Op op : t.ops) {
            tt.add(String.format("  %03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d"
                    , op.ar, op.dr, op.sr, op.rr, op.sl, op.tl, op.ks, op.ml, op.dt, op.dt2, op.am
            ));
        }
        tt.add("}");
        tt.add("");

        return tt.toArray(new String[0]);
    }

    private String[] makeToneTextForMXDRV(Tone t, int n) {
        List<String> tt = new ArrayList<>();

        tt.add(String.format("/* %s */", t.name));
        tt.add(String.format("@%d= {{ ", n));
        tt.add("/* AR  D1R D2R RR  D1L TL  KS  MUL DT1 DT2 AME */");
        for (Tone.Op op : t.ops) {
            tt.add(String.format("   %03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d"
                    , op.ar, op.dr, op.sr, op.rr, op.sl, op.tl, op.ks, op.ml, op.dt, op.dt2, op.am
            ));
        }
        tt.add("/* CON FL  OP");
        tt.add(String.format("   %03d,%03d,015", t.al, t.fb));
        tt.add("}");
        tt.add("");

        return tt.toArray(new String[0]);
    }

    private String[] makeToneTextForMUSICLALF(Tone t, int n) {
        List<String> tt = new ArrayList<>();

        tt.add(String.format("[ROW] ' @%d:{{", n));
        tt.add(String.format("[ROW] ' %03d,%03d", t.al, t.fb));

        int o = 0;
        for (Tone.Op op : t.ops) {
            o++;
            if (o != 4) {
                tt.add(String.format("[ROW] ' %03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d"
                        , op.ar, op.dr, op.sr, op.rr, op.sl, op.tl, op.ks, op.ml, op.dt
                ));
            } else {
                tt.add(String.format("[ROW] ' %03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,%03d,\"%s\" }}"
                        , op.ar, op.dr, op.sr, op.rr, op.sl, op.tl, op.ks, op.ml, op.dt, t.name
                ));
            }
        }

        tt.add("[ROW] '");

        return tt.toArray(new String[0]);
    }

    /**
     * @param tp 1 origin
     */
    public void loadTonePallet(String fn, int tp, TonePallet tonePallet) {
        if (tp == 1) {
            TonePallet tP = TonePallet.load(fn);
            tonePallet.setLstTone(tP.getLstTone());
            return;
        }

        List<String> tnt = new ArrayList<>();

        try (StreamReader sr = new StreamReader(new FileStream(fn, FileMode.Open))) {
            String line;
            while ((line = sr.readLine()) != null) {
                tnt.add(line);
            }
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }

        switch (tp) {
        case 2:
            loadTonePalletFromMml2Vgm(tnt.toArray(new String[0]), tonePallet);
            break;
        case 3:
            loadTonePalletFromFMP7(tnt.toArray(new String[0]), tonePallet);
            break;
        case 4:
            loadTonePalletFromNRTDRV(tnt.toArray(new String[0]), tonePallet);
            break;
        case 5:
            loadTonePalletFromMXDRV(tnt.toArray(new String[0]), tonePallet);
            break;
        case 6:
            loadTonePalletFromMUSICLALF(tnt.toArray(new String[0]), tonePallet);
            break;
        }
    }

    public void loadTonePalletFromMml2Vgm(String[] tnt, TonePallet tonePallet) {
        String line;
        int stage = 0;
        List<Integer> toneBuf = new ArrayList<>();
        int index = 0;

        while ((line = index == tnt.length ? null : tnt[index++]) != null) {
            line = line.trim();
            if (line.indexOf("'@") != 0) continue;

            line = line.replace("'@", "").trim();
            String c = String.valueOf(line.charAt(0)).toUpperCase();
            if (stage == 0 && (c.equals("M") || c.equals("N"))) {
                stage = 1;
                if (line.length() < 2) continue;

                line = line.substring(1).trim();
            }

            if (stage > 0) {
                int[] nums = numSplit(line);
                if (nums.length == 0 && line.equals("xx")) {
                    nums = new int[] {0};
                }
                for (int n : nums) {
                    stage++;
                    toneBuf.add(n);
                }
                if (stage > 47) {
                    if (stage == 48) {
                        Tone t = new Tone();
                        t.name = String.format("No.%d(From MML2VGM)", toneBuf.get(0));
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 1);
                            t.ops[i].dr = toneBuf.get(i * 11 + 2);
                            t.ops[i].sr = toneBuf.get(i * 11 + 3);
                            t.ops[i].rr = toneBuf.get(i * 11 + 4);
                            t.ops[i].sl = toneBuf.get(i * 11 + 5);
                            t.ops[i].tl = toneBuf.get(i * 11 + 6);
                            t.ops[i].ks = toneBuf.get(i * 11 + 7);
                            t.ops[i].ml = toneBuf.get(i * 11 + 8);
                            t.ops[i].dt = toneBuf.get(i * 11 + 9);
                            t.ops[i].am = toneBuf.get(i * 11 + 10);
                            t.ops[i].sg = toneBuf.get(i * 11 + 11);
                            t.ops[i].dt2 = 0;
                        }
                        t.al = toneBuf.get(45);
                        t.fb = toneBuf.get(46);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                    }

                    stage = 0;
                    toneBuf.clear();
                }
            }
        }
    }

    public void loadTonePalletFromFMP7(String[] tnt, TonePallet tonePallet) {
        String line;
        int stage = 0;
        List<Integer> toneBuf = new ArrayList<>();
        int m = 0;
        int index = 0;

        while ((line = index == tnt.length ? null : tnt[index++]) != null) {
            line = line.trim();
            if (line.indexOf("'@") != 0) continue;

            line = line.replace("'@", "").trim();
            String c = String.valueOf(line.charAt(0)).toUpperCase();
            if (stage == 0 && c.equals("F")) {
                stage = 1;
                if (line.length() < 2) continue;

                line = line.substring(1).trim();
                c = String.valueOf(line.charAt(0)).toUpperCase();
                m = 0;//互換モード

                if (c.equals("A")) {
                    m = 1;//OPNAモード
                    line = line.substring(1).trim();
                } else if (c.equals("C")) {
                    m = 2;//OPMモード
                    line = line.substring(1).trim();
                }
            }

            if (stage > 0) {
                int[] nums = numSplit(line);
                for (int n : nums) {
                    stage++;
                    toneBuf.add(n);
                }

                if (m == 0 && stage >= 40) {
                    if (stage == 40) {
                        //互換
                        Tone t = new Tone();
                        t.name = String.format("No.%d(From FMP7 compatible)", toneBuf.get(0));
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 9 + 1);
                            t.ops[i].dr = toneBuf.get(i * 9 + 2);
                            t.ops[i].sr = toneBuf.get(i * 9 + 3);
                            t.ops[i].rr = toneBuf.get(i * 9 + 4);
                            t.ops[i].sl = toneBuf.get(i * 9 + 5);
                            t.ops[i].tl = toneBuf.get(i * 9 + 6);
                            t.ops[i].ks = toneBuf.get(i * 9 + 7);
                            t.ops[i].ml = toneBuf.get(i * 9 + 8);
                            t.ops[i].dt = toneBuf.get(i * 9 + 9);
                            t.ops[i].am = 0;
                            t.ops[i].sg = 0;
                            t.ops[i].dt2 = 0;
                        }
                        t.al = toneBuf.get(37);
                        t.fb = toneBuf.get(38);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                    }

                    stage = 0;
                    toneBuf.clear();
                }

                if (m == 1 && stage >= 44) {
                    if (stage == 44) {
                        //OPNA
                        Tone t = new Tone();
                        t.name = String.format("No.%d(From FMP7 OPNA)", toneBuf.get(0));
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 10 + 1);
                            t.ops[i].dr = toneBuf.get(i * 10 + 2);
                            t.ops[i].sr = toneBuf.get(i * 10 + 3);
                            t.ops[i].rr = toneBuf.get(i * 10 + 4);
                            t.ops[i].sl = toneBuf.get(i * 10 + 5);
                            t.ops[i].tl = toneBuf.get(i * 10 + 6);
                            t.ops[i].ks = toneBuf.get(i * 10 + 7);
                            t.ops[i].ml = toneBuf.get(i * 10 + 8);
                            t.ops[i].dt = toneBuf.get(i * 10 + 9);
                            t.ops[i].am = toneBuf.get(i * 10 + 10);
                            t.ops[i].sg = 0;
                            t.ops[i].dt2 = 0;
                        }
                        t.al = toneBuf.get(41);
                        t.fb = toneBuf.get(42);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                    }

                    stage = 0;
                    toneBuf.clear();
                }

                if (m == 2 && stage >= 48) {
                    if (stage == 48) {
                        //OPM
                        Tone t = new Tone();
                        t.name = String.format("No.%d(From FMP7 OPM)", toneBuf.get(0));
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 1);
                            t.ops[i].dr = toneBuf.get(i * 11 + 2);
                            t.ops[i].sr = toneBuf.get(i * 11 + 3);
                            t.ops[i].rr = toneBuf.get(i * 11 + 4);
                            t.ops[i].sl = toneBuf.get(i * 11 + 5);
                            t.ops[i].tl = toneBuf.get(i * 11 + 6);
                            t.ops[i].ks = toneBuf.get(i * 11 + 7);
                            t.ops[i].ml = toneBuf.get(i * 11 + 8);
                            t.ops[i].dt = toneBuf.get(i * 11 + 9);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 10);
                            t.ops[i].am = toneBuf.get(i * 11 + 11);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(45);
                        t.fb = toneBuf.get(46);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                    }

                    stage = 0;
                    toneBuf.clear();
                }
            }
        }
    }

    public void loadTonePalletFromNRTDRV(String[] tnt, TonePallet tonePallet) {
        int voiceMode = 0;

        String line;
        boolean cm = false;
        int ind;
        int index = 0;

        line = index == tnt.length ? null : tnt[index++];
        if (line == null) return;

        do {
            if (cm) {
                // コメント中
                ind = line.indexOf("*/");
                if (ind >= 0) {
                    cm = false;
                    if (line.length() == 2) {
                        line = index == tnt.length ? null : tnt[index++];
                        continue;
                    }
                    line = line.substring(ind + 2);
                    continue;
                }

                line = index == tnt.length ? null : tnt[index++];
                continue;
            }

            ind = line.indexOf(";");
            if (ind >= 0) {
                if (ind == 0) {
                    line = index == tnt.length ? null : tnt[index++];
                    continue;
                }
                line = line.substring(0, ind);
                continue;
            }

            ind = line.indexOf("/*");
            if (ind >= 0) {
                cm = true;
                line = line.substring(0, ind);
            }

            String cmd = line.trim().toUpperCase();
            if (cmd.contains("#VOICE_MODE")) {
                try {
                    voiceMode = Integer.parseInt(cmd.replace("#VOICE_MODE", "").trim());
                } catch (Exception e) {
                    voiceMode = 0;
                }
            }

            line = index == tnt.length ? null : tnt[index++];
        } while (line != null);


        line = "";
        cm = false;
        ind = 0;
        int stage = 0;
        List<Integer> toneBuf = new ArrayList<>();
        index = 0;

        line = index == tnt.length ? null : tnt[index++];
        if (line == null) return;

        do {
            if (cm) {
                //コメント中
                ind = line.indexOf("*/");
                if (ind >= 0) {
                    cm = false;
                    if (line.length() == 2) {
                        line = index == tnt.length ? null : tnt[index++];
                        continue;
                    }
                    line = line.substring(ind + 2);
                    continue;
                }

                line = index == tnt.length ? null : tnt[index++];
                continue;
            }

            ind = line.indexOf(";");
            if (ind >= 0) {
                if (ind == 0) {
                    line = index == tnt.length ? null : tnt[index++];
                    continue;
                }
                line = line.substring(0, ind);
                continue;
            }

            ind = line.indexOf("/*");
            if (ind >= 0) {
                cm = true;
                line = line.substring(0, ind);
            }

            String cmd = line.trim();
            if (cmd.length() == 0) {
                line = index == tnt.length ? null : tnt[index++];
                continue;
            }

            if (stage == 0 && cmd.charAt(0) == '@') {
                stage++;
                line = line.substring(1);

                char c = line.charAt(0);
                Integer n = null;
                while (c >= '0' && c <= '9') {
                    if (n == null) n = 0;

                    n = n * 10 + (c - '0');

                    line = line.substring(1);
                    if (line.length() < 1) break;

                    c = line.charAt(0);
                }
                if (n != null) toneBuf.add(n);

                continue;
            } else if (stage == 1 && cmd.charAt(0) == '{') {
                stage++;
                line = line.substring(1);

                int[] nums = numSplit(line);
                for (int n : nums) {
                    toneBuf.add(n);
                }

                line = index == tnt.length ? null : tnt[index++];
                continue;
            } else if (stage == 2) {

                int[] nums = numSplit(line);
                for (int n : nums) {
                    toneBuf.add(n);
                }

                if (line.indexOf('}') >= 0) {

                    //
                    Tone t = null;
                    t = new Tone();
                    t.name = String.format("No.%d(From NRTDRV)", toneBuf.get(0));

                    switch (voiceMode) {
                    case 0:
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 5);
                            t.ops[i].dr = toneBuf.get(i * 11 + 6);
                            t.ops[i].sr = toneBuf.get(i * 11 + 7);
                            t.ops[i].rr = toneBuf.get(i * 11 + 8);
                            t.ops[i].sl = toneBuf.get(i * 11 + 9);
                            t.ops[i].tl = toneBuf.get(i * 11 + 10);
                            t.ops[i].ks = toneBuf.get(i * 11 + 11);
                            t.ops[i].ml = toneBuf.get(i * 11 + 12);
                            t.ops[i].dt = toneBuf.get(i * 11 + 13);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 14);
                            t.ops[i].am = toneBuf.get(i * 11 + 15);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(2);
                        t.fb = toneBuf.get(3);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                        break;
                    case 1:
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 1);
                            t.ops[i].dr = toneBuf.get(i * 11 + 2);
                            t.ops[i].sr = toneBuf.get(i * 11 + 3);
                            t.ops[i].rr = toneBuf.get(i * 11 + 4);
                            t.ops[i].sl = toneBuf.get(i * 11 + 5);
                            t.ops[i].tl = toneBuf.get(i * 11 + 6);
                            t.ops[i].ks = toneBuf.get(i * 11 + 7);
                            t.ops[i].ml = toneBuf.get(i * 11 + 8);
                            t.ops[i].dt = toneBuf.get(i * 11 + 9);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 10);
                            t.ops[i].am = toneBuf.get(i * 11 + 11);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(46);
                        t.fb = toneBuf.get(47);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                        break;
                    case 2:
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 4);
                            t.ops[i].dr = toneBuf.get(i * 11 + 5);
                            t.ops[i].sr = toneBuf.get(i * 11 + 6);
                            t.ops[i].rr = toneBuf.get(i * 11 + 7);
                            t.ops[i].sl = toneBuf.get(i * 11 + 8);
                            t.ops[i].tl = toneBuf.get(i * 11 + 9);
                            t.ops[i].ks = toneBuf.get(i * 11 + 10);
                            t.ops[i].ml = toneBuf.get(i * 11 + 11);
                            t.ops[i].dt = toneBuf.get(i * 11 + 12);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 13);
                            t.ops[i].am = toneBuf.get(i * 11 + 14);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(1);
                        t.fb = toneBuf.get(2);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                        break;
                    case 3:
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 1);
                            t.ops[i].dr = toneBuf.get(i * 11 + 2);
                            t.ops[i].sr = toneBuf.get(i * 11 + 3);
                            t.ops[i].rr = toneBuf.get(i * 11 + 4);
                            t.ops[i].sl = toneBuf.get(i * 11 + 5);
                            t.ops[i].tl = toneBuf.get(i * 11 + 6);
                            t.ops[i].ks = toneBuf.get(i * 11 + 7);
                            t.ops[i].ml = toneBuf.get(i * 11 + 8);
                            t.ops[i].dt = toneBuf.get(i * 11 + 9);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 10);
                            t.ops[i].am = toneBuf.get(i * 11 + 11);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(45);
                        t.fb = toneBuf.get(46);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                        break;
                    case 4:
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 4);
                            t.ops[i].dr = toneBuf.get(i * 11 + 5);
                            t.ops[i].sr = toneBuf.get(i * 11 + 6);
                            t.ops[i].rr = toneBuf.get(i * 11 + 7);
                            t.ops[i].sl = toneBuf.get(i * 11 + 8);
                            t.ops[i].tl = toneBuf.get(i * 11 + 9);
                            t.ops[i].ks = toneBuf.get(i * 11 + 10);
                            t.ops[i].ml = toneBuf.get(i * 11 + 11);
                            t.ops[i].dt = toneBuf.get(i * 11 + 12);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 13);
                            t.ops[i].am = toneBuf.get(i * 11 + 14);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(1) & 0x7;
                        t.fb = (toneBuf.get(1) & 0x38) >> 3;

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                        break;
                    case 5:
                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 12);
                            t.ops[i].dr = toneBuf.get(i * 11 + 13);
                            t.ops[i].sr = toneBuf.get(i * 11 + 14);
                            t.ops[i].rr = toneBuf.get(i * 11 + 15);
                            t.ops[i].sl = toneBuf.get(i * 11 + 16);
                            t.ops[i].tl = toneBuf.get(i * 11 + 17);
                            t.ops[i].ks = toneBuf.get(i * 11 + 18);
                            t.ops[i].ml = toneBuf.get(i * 11 + 19);
                            t.ops[i].dt = toneBuf.get(i * 11 + 20);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 21);
                            t.ops[i].am = toneBuf.get(i * 11 + 22);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(1) & 0x7;
                        t.fb = (toneBuf.get(1) & 0x38) >> 3;

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                        break;
                    }

                    stage = 0;
                    line = line.substring(1);
                    toneBuf.clear();
                    continue;
                }
            }

            line = index == tnt.length ? null : tnt[index++];
        } while (line != null);


    }

    public void loadTonePalletFromMXDRV(String[] tnt, TonePallet tonePallet) {
        String line;
        int stage = 0;
        List<Integer> toneBuf = new ArrayList<>();
        int index = 0;

        line = index == tnt.length ? null : tnt[index++];
        if (line == null) return;

        do {

            line = line.trim();

            if (line.length() > 1 && line.charAt(0) == '/' && line.charAt(1) == '*') {
                line = index == tnt.length ? null : tnt[index++];
                if (line == null) return;
                continue;
            }

            if (line.isEmpty()) {
                line = index == tnt.length ? null : tnt[index++];
                if (line == null) return;
                continue;
            }

            if (stage == 0 && line.charAt(0) == '@') {
                stage++;
                line = line.substring(1);

                char c = line.charAt(0);
                Integer n = null;
                while (c >= '0' && c <= '9') {
                    if (n == null) n = 0;

                    n = n * 10 + (c - '0');

                    line = line.substring(1);
                    if (line.length() < 1) break;

                    c = line.charAt(0);
                }
                if (n != null) toneBuf.add(n);

                continue;
            } else if (stage == 1 && line.charAt(0) == '=') {
                stage++;

                if (line.length() > 1) {
                    line = line.substring(1);
                } else {
                    line = index == tnt.length ? null : tnt[index++];
                    if (line == null) return;
                }
                continue;
            } else if (stage == 2 && line.charAt(0) == '{') {
                stage++;

                if (line.length() > 1) {
                    line = line.substring(1);
                } else {
                    line = index == tnt.length ? null : tnt[index++];
                    if (line == null) return;
                    continue;
                }

                int[] nums = numSplit(line);
                for (int n : nums) {
                    toneBuf.add(n);
                }

                line = index == tnt.length ? null : tnt[index++];
                if (line == null) return;
                continue;
            } else if (stage == 3) {

                int[] nums = numSplit(line);
                for (int n : nums) {
                    toneBuf.add(n);
                }

                if (line.indexOf('}') >= 0) {

                    if (toneBuf.size() == 48) {
                        Tone t = new Tone();
                        t.name = String.format("No.%d(From MXDRV)", toneBuf.get(0));

                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 11 + 1);
                            t.ops[i].dr = toneBuf.get(i * 11 + 2);
                            t.ops[i].sr = toneBuf.get(i * 11 + 3);
                            t.ops[i].rr = toneBuf.get(i * 11 + 4);
                            t.ops[i].sl = toneBuf.get(i * 11 + 5);
                            t.ops[i].tl = toneBuf.get(i * 11 + 6);
                            t.ops[i].ks = toneBuf.get(i * 11 + 7);
                            t.ops[i].ml = toneBuf.get(i * 11 + 8);
                            t.ops[i].dt = toneBuf.get(i * 11 + 9);
                            t.ops[i].dt2 = toneBuf.get(i * 11 + 10);
                            t.ops[i].am = toneBuf.get(i * 11 + 11);
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(45);
                        t.fb = toneBuf.get(46);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                    }
                    stage = 0;
                    line = line.substring(1);
                    toneBuf.clear();
                    continue;
                }
            }

            line = index == tnt.length ? null : tnt[index++];
        } while (line != null);
    }

    public void loadTonePalletFromMUSICLALF(String[] tnt, TonePallet tonePallet) {
        String line;
        int stage = 0;
        List<Integer> toneBuf = new ArrayList<>();
        String nm = "";
        int index = 0;

        line = index == tnt.length ? null : tnt[index++];
        if (line == null) return;
        line = line.substring(line.indexOf("'") + 1).trim();

        do {

            line = line.trim();

            if (line.length() > 0 && line.charAt(0) == ';') {
                line = index == tnt.length ? null : tnt[index++];
                if (line == null) return;
                line = line.substring(line.indexOf("'") + 1).trim();
                continue;
            }

            if (line.isEmpty()) {
                line = index == tnt.length ? null : tnt[index++];
                if (line == null) return;
                line = line.substring(line.indexOf("'") + 1).trim();
                continue;
            }

            if (stage == 0 && line.charAt(0) == '@') {
                stage++;
                line = line.substring(1);

                char c = line.charAt(0);
                Integer n = null;
                while (c >= '0' && c <= '9') {
                    if (n == null) n = 0;

                    n = n * 10 + (c - '0');

                    line = line.substring(1);
                    if (line.length() < 1) break;

                    c = line.charAt(0);
                }
                if (n != null) toneBuf.add(n);

                continue;
            } else if (stage == 1 && line.charAt(0) == ':') {
                stage++;

                if (line.length() > 1) {
                    line = line.substring(1);
                } else {
                    line = index == tnt.length ? null : tnt[index++];
                    if (line == null) return;
                    line = line.substring(line.indexOf("'") + 1).trim();
                }
                continue;
            } else if (stage == 2 && line.charAt(0) == '{') {
                stage++;

                if (line.length() > 1) {
                    line = line.substring(1);
                } else {
                    line = index == tnt.length ? null : tnt[index++];
                    if (line == null) return;
                    line = line.substring(line.indexOf("'") + 1).trim();
                    continue;
                }

                int[] nums = numSplit(line);
                for (int n : nums) {
                    toneBuf.add(n);
                }

                line = index == tnt.length ? null : tnt[index++];
                if (line == null) return;
                line = line.substring(line.indexOf("'") + 1).trim();
                continue;
            } else if (stage == 3) {

                int[] nums = numSplit(line);
                for (int n : nums) {
                    toneBuf.add(n);
                }

                if (toneBuf.size() >= 39) {
                    stage = 4;
                    continue;
                }
            } else if (stage == 4) {
                if (line.indexOf('"') >= 0) {
                    try {
                        String n = line.substring(line.indexOf('"') + 1);
                        nm = n.substring(0, n.indexOf('"'));
                    } catch (Exception ignored) {
                    }
                }

                if (line.indexOf('}') >= 0) {

                    if (toneBuf.size() == 39) {
                        Tone t = new Tone();
                        t.name = nm.isEmpty() ? String.format("No.%d(From MusicLALF)", toneBuf.get(0)) : nm;

                        t.ops = new Tone.Op[4];
                        for (int i = 0; i < 4; i++) {
                            t.ops[i] = new Tone.Op();
                            t.ops[i].ar = toneBuf.get(i * 9 + 3);
                            t.ops[i].dr = toneBuf.get(i * 9 + 4);
                            t.ops[i].sr = toneBuf.get(i * 9 + 5);
                            t.ops[i].rr = toneBuf.get(i * 9 + 6);
                            t.ops[i].sl = toneBuf.get(i * 9 + 7);
                            t.ops[i].tl = toneBuf.get(i * 9 + 8);
                            t.ops[i].ks = toneBuf.get(i * 9 + 9);
                            t.ops[i].ml = toneBuf.get(i * 9 + 10);
                            t.ops[i].dt = toneBuf.get(i * 9 + 11);
                            t.ops[i].dt2 = 0;
                            t.ops[i].am = 0;
                            t.ops[i].sg = 0;
                        }
                        t.al = toneBuf.get(1);
                        t.fb = toneBuf.get(2);

                        tonePallet.getLstTone().set(toneBuf.get(0), t);
                    }
                    stage = 0;
                    nm = "";
                    line = line.substring(1);
                    toneBuf.clear();
                    continue;
                }
            }

            line = index == tnt.length ? null : tnt[index++];
            if (line == null) return;
            line = line.substring(line.indexOf("'") + 1).trim();
        } while (!line.isEmpty());
    }

    private int[] numSplit(String line) {
        List<Integer> ret = new ArrayList<>();

        line = line.trim();
        while (line.length() > 0) {
            char c = line.charAt(0);
            Integer n = null;
            while (c >= '0' && c <= '9') {
                if (n == null) n = 0;

                n = n * 10 + (c - '0');

                line = line.substring(1);
                if (line.length() < 1) break;

                c = line.charAt(0);
            }

            if (n != null) ret.add(n);

            if (line.length() < 1) break;
            line = line.substring(1);

            line = line.trim();
        }

        return ret.stream().mapToInt(Integer::intValue).toArray();
    }

    public void copyToneToClipboard(int[] chs) {
        if (chs == null || chs.length < 1) return;

        List<String> des = new ArrayList<>();

        for (int ch : chs) {
            String[] tt = null;
            switch (parent.setting.getMidiKbd().getUseFormat()) {
            case 0:
                tt = makeToneTextForMml2vgm(parent.setting.getMidiKbd().getTones()[ch], ch + 1);
                break;
            case 2:
                tt = makeToneTextForFMP7(parent.setting.getMidiKbd().getTones()[ch], ch + 1);
                break;
            case 4:
                tt = makeToneTextForMXDRV(parent.setting.getMidiKbd().getTones()[ch], ch + 1);
                break;
            case 3:
                tt = makeToneTextForMUSICLALF(parent.setting.getMidiKbd().getTones()[ch], ch + 1);
                break;
            case 1:
                tt = makeToneTextForNRTDRV(parent.setting.getMidiKbd().getTones()[ch], ch + 1);
                break;
            }

            if (tt == null || tt.length < 1) return;

            des.addAll(Arrays.asList(tt));
        }

        if (parent.setting.getMidiKbd().getUseFormat() == 3) {
            int row = 10;
            for (int i = 0; i < des.size(); i++) {
                des.set(i, des.get(i).replace("[ROW]", String.valueOf(row)));
                row += 10;
            }
        }

        StringBuilder n = new StringBuilder();
        for (String text : des) {
            n.append(text).append("\n");
        }

        Common.setClipboard(n.toString());
    }

    public void pasteToneFromClipboard(int[] chs) {
        String cbt = Common.getClipboard();
        if (cbt.isEmpty()) return;
        String[] tnt = cbt.split("\n");

        TonePallet tp = new TonePallet();
        tp.setLstTone(new ArrayList<>(256));
        for (int i = 0; i < 256; i++) tp.getLstTone().add(null);

        switch (parent.setting.getMidiKbd().getUseFormat()) {
        case 0:
            loadTonePalletFromMml2Vgm(tnt, tp);
            break;
        case 2:
            loadTonePalletFromFMP7(tnt, tp);
            break;
        case 4:
            loadTonePalletFromMXDRV(tnt, tp);
            break;
        case 3:
            loadTonePalletFromMUSICLALF(tnt, tp);
            break;
        case 1:
            loadTonePalletFromNRTDRV(tnt, tp);
            break;
        }

        int j = 0;
        for (int ch : chs) {
            Tone t = null;
            while (t == null && j < 256) {
                t = tp.getLstTone().get(j);
                j++;
            }

            if (t != null) parent.setting.getMidiKbd().getTones()[ch] = t;
        }

        setTonesFromSettng();
    }

    public void changeSelectedParamValue(int n) {
        int ch = newParam.ym2612Midi.selectCh;
        int p = newParam.ym2612Midi.selectParam;
        if (ch == -1 || p == -1) return;

        if (p >= 44 && p < 48) {
            switch (p) {
            case 44:
                parent.setting.getMidiKbd().getTones()[ch].al += n;
                break;
            case 45:
                parent.setting.getMidiKbd().getTones()[ch].fb += n;
                break;
            case 46:
                parent.setting.getMidiKbd().getTones()[ch].ams += n;
                break;
            case 47:
                parent.setting.getMidiKbd().getTones()[ch].pms += n;
                break;
            }
        } else {
            int op = p / 11;
            switch (p % 11) {
            case 0:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].ar += n;
                break;
            case 1:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].dr += n;
                break;
            case 2:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].sr += n;
                break;
            case 3:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].rr += n;
                break;
            case 4:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].sl += n;
                break;
            case 5:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].tl += n;
                break;
            case 6:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].ks += n;
                break;
            case 7:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].ml += n;
                break;
            case 8:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].dt += n;
                break;
            case 9:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].am += n;
                break;
            case 10:
                parent.setting.getMidiKbd().getTones()[ch].ops[op].sg += n;
                break;
            }
        }
        setTonesFromSettng();
    }
}
