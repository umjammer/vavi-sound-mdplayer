package mdplayer.driver.mndrv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdplayer.Common;


public class Reg {
    public int D0_L;
    public int D1_L;
    public int D2_L;
    public int D3_L;
    public int D4_L;
    public int D5_L;
    public int D6_L;
    public int D7_L;
    public int a0;
    public int a1;
    public int a2;
    public int a3;
    public int a4;
    public int a5;
    public int a6;
    public int a7;

    public int sr;

    public List<Integer> arg = new ArrayList<>();

    public int getD0_B() {
        return (byte) D0_L;
    }

    void setD0_B(int value) {
        D0_L = (D0_L & 0xffffff00) | (value & 0xff);
    }

    public int getD1_B() {
        return (byte) D1_L;
    }

    void setD1_B(int value) {
        D1_L = (D1_L & 0xffffff00) | (value & 0xff);
    }

    public int getD2_B() {
        return (byte) D2_L;
    }

    void setD2_B(int value) {
        D2_L = (D2_L & 0xffffff00) | (value & 0xff);
    }

    public int getD3_B() {
        return (byte) D3_L;
    }

    void setD3_B(int value) {
        D3_L = (D3_L & 0xffffff00) | (value & 0xff);
    }

    public int getD4_B() {
        return (byte) D4_L;
    }

    void setD4_B(int value) {
        D4_L = (D4_L & 0xffffff00) | (value & 0xff);
    }

    public int getD5_B() {
        return (byte) D5_L;
    }

    void setD5_B(int value) {
        D5_L = (D5_L & 0xffffff00) | (value & 0xff);
    }

    public int getD6_B() {
        return (byte) D6_L;
    }

    void setD6_B(int value) {
        D6_L = (D6_L & 0xffffff00) | (value & 0xff);
    }

    public int getD7_B() {
        return (byte) D7_L;
    }

    void setD7_B(int value) {
        D7_L = (D7_L & 0xffffff00) | (value & 0xff);
    }

    public int getD0_W() {
        return D0_L;
    }

    void setD0_W(int value) {
        D0_L = (D0_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD1_W() {
        return D1_L;
    }

    void setD1_W(int value) {
        D1_L = (D1_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD2_W() {
        return D2_L;
    }

    void setD2_W(int value) {
        D2_L = (D2_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD3_W() {
        return D3_L;
    }

    void setD3_W(int value) {
        D3_L = (D3_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD4_W() {
        return D4_L;
    }

    void setD4_W(int value) {
        D4_L = (D4_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD5_W() {
        return D5_L;
    }

    void setD5_W(int value) {
        D5_L = (D5_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD6_W() {
        return D6_L;
    }

    void setD6_W(int value) {
        D6_L = (D6_L & 0xffff0000) | (value & 0xffff);
    }

    public int getD7_W() {
        return D7_L;
    }

    void setD7_W(int value) {
        D7_L = (D7_L & 0xffff0000) | (value & 0xffff);
    }

    public int getSR_W() {
        return sr;
    }

    void setSR_W(int value) {
        sr = (sr & 0xffff0000) | (value & 0xffff);
    }

    public int decAfterD0_W() {
        return D0_L--;
    }

    public int decAfterD1_W() {
        return D1_L--;
    }

    public int decAfterD2_W() {
        return D2_L--;
    }

    public int decAfterD4_W() {
        return D4_L--;
    }
    public int decAfterD5_W() {
        return D5_L--;
    }

    public int decAfterD7_W() {
        return D7_L--;
    }

    public void setD0_L(int v) {
        D0_L = v;
    }

    public void setD1_L(int v) {
        D1_L = v;
    }

    public void setD2L(int v) {
        D2_L = v;
    }

    public void setD3_L(int v) {
        D3_L = v;
    }

    public void setD4_L(int v) {
        D4_L = v;
    }

    public void setD5_L(int v) {
        D5_L = v;
    }

    public void setD6_L(int v) {
        D6_L = v;
    }

    public void setD7_L(int v) {
        D7_L = v;
    }

    public void setSR(int v) {
        sr = v;
    }

    public boolean cryADD(byte a, byte b) {
        return (a & 0xff) + (b & 0xff) > 0xff;
    }

    public boolean cryADD(short a, short b) {
        return (a  & 0xffff) + (b  & 0xffff) > 0xffff;
    }

    public boolean cryADD(int a, int b) {
        return (long) a + (long) b > (long) 0xffffffff;
    }
}

class Ab {
    static final int dummyAddress = 0xffffffff;

    final Map<Integer, Runnable> hlTRKANA_RESTADR = new HashMap<>();
    final Map<Integer, Runnable> hlw_qtjob = new HashMap<>();
    final Map<Integer, Runnable> hlw_mmljob_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_lfojob_adrs = new HashMap<>();
//    final Map<Integer, Runnable> hlw_psgenv_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_softenv_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_rrcut_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_echo_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_keyoff_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_keyoff_adrs2 = new HashMap<>();
    final Map<Integer, Runnable> hlw_subcmd_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_setnote_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_inithlfo_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_we_exec_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_we_ycom_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_we_tone_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlw_we_pan_adrs = new HashMap<>();
    final Map<Integer, Runnable> hlINTEXECBUF = new HashMap<>();
}

class FMTimer {
    // タイマーAの上位8ビット
    private int timerAregH;
    // タイマーAの下位2ビット
    private int timerAregL;
    // タイマーAのオーバーフロー設定値
    private int timerA;
    // タイマーAのカウンター値
    private double timerACounter;
    // タイマーBのオーバーフロー設定値
    private int timerB;
    // タイマーBのカウンター値
    private double timerBCounter;
    // タイマー制御レジスタ (下位4ビット+7ビット)
    private int timerReg;
    // ステータスレジスタ (下位2ビット)
    private int statReg;
    private boolean isOPM = false;
    private Runnable csmKeyOn;
    private double step = 0.0;
    private double masterClock = 3579545.0;

    public FMTimer(boolean isOPM, Runnable csmKeyOn, double masterClock) {
        this.isOPM = isOPM;
        this.csmKeyOn = csmKeyOn;
        this.masterClock = masterClock;
        if (isOPM) {
            step = masterClock / 64.0 / 1.0 / (double) Common.VGMProcSampleRate;
        } else {
            step = masterClock / 72.0 / 2.0 / (double) Common.VGMProcSampleRate;
        }
    }

    public void timer() {
        int flag_set = 0;

        if ((timerReg & 0x01) != 0) { // timerA 動作中
            timerACounter += step;
            if (timerACounter >= timerA) {
                flag_set |= ((timerReg >> 2) & 0x01);
                timerACounter -= timerA;
                if ((timerReg & 0x80) != 0) csmKeyOn.run();
            }
        }

        if ((timerReg & 0x02) != 0) { // timerB 動作中
            timerBCounter += step;
            if (timerBCounter >= timerB) {
                flag_set |= ((timerReg >> 2) & 0x02);
                timerBCounter -= timerB;
            }
        }

        statReg |= flag_set;
    }

    public void WriteReg(byte adr, byte data) {
        if (isOPM) WriteRegOPM(adr, data);
        else WriteRegOPN(adr, data);
    }

    private void WriteRegOPM(byte adr, byte data) {
        switch (adr) {
        case 0x10:
        case 0x11:
            // timerA
            if (adr == 0x10) timerAregH = data;
            else timerAregL = data & 3;
            timerA = 1024 - ((timerAregH << 2) + timerAregL);
            break;

        case 0x12:
            // timerB
            timerB = (256 - (int) data) << (10 - 6);
            break;

        case 0x14:
            // タイマー制御レジスタ
            timerReg = data & 0x8F;
            statReg &= 0xFF - ((data >> 4) & 3);
            break;
        }
    }

    private void WriteRegOPN(byte adr, byte data) {
        switch (adr) {
        case 0x24:
        case 0x25:
            // timerA
            if (adr == 0x24) timerAregH = data;
            else timerAregL = data & 3;
            timerA = 1024 - ((timerAregH << 2) + timerAregL);
            break;

        case 0x26:
            // timerB
            timerB = (256 - (int) data) << (10 - 6);
            break;

        case 0x27:
            // タイマー制御レジスタ
            timerReg = data & 0x8F;
            statReg &= 0xFF - ((data >> 4) & 3);
            break;
        }
    }

    public int readStatus() {
        return statReg;
    }
}

