package mdplayer.lib.mndrv;

import java.util.HashMap;
import java.util.Map;

import mdplayer.emu.nise68.IRegister;


public class Reg implements IRegister {

    public int D0_L;
    public int D1_L;
    public int D2_L;
    public int D3_L;
    public int D4_L;
    public int D5_L;
    int D6_L;
    int D7_L;
    public int a0;
    public int a1;
    public int a2;
    public int a3;
    public int a4;
    public int a5;
    public int a6;
    private int a7;

    public int sr;

    public int getD0_B() {
        return D0_L & 0xff;
    }

    void setD0_B(int value) {
        D0_L = (D0_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD1_B() {
        return D1_L & 0xff;
    }

    void setD1_B(int value) {
        D1_L = (D1_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD2_B() {
        return D2_L & 0xff;
    }

    void setD2_B(int value) {
        D2_L = (D2_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD3_B() {
        return D3_L & 0xff;
    }

    void setD3_B(int value) {
        D3_L = (D3_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD4_B() {
        return D4_L & 0xff;
    }

    void setD4_B(int value) {
        D4_L = (D4_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD5_B() {
        return D5_L & 0xff;
    }

    void setD5_B(int value) {
        D5_L = (D5_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD6_B() {
        return D6_L & 0xff;
    }

    void setD6_B(int value) {
        D6_L = (D6_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD7_B() {
        return D7_L & 0xff;
    }

    void setD7_B(int value) {
        D7_L = (D7_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD0_W() {
        return D0_L & 0xffff;
    }

    void setD0_W(int value) {
        D0_L = (D0_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD1_W() {
        return D1_L & 0xffff;
    }

    void setD1_W(int value) {
        D1_L = (D1_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD2_W() {
        return D2_L & 0xffff;
    }

    void setD2_W(int value) {
        D2_L = (D2_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD3_W() {
        return D3_L & 0xffff;
    }

    void setD3_W(int value) {
        D3_L = (D3_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD4_W() {
        return D4_L & 0xffff;
    }

    void setD4_W(int value) {
        D4_L = (D4_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD5_W() {
        return D5_L & 0xffff;
    }

    void setD5_W(int value) {
        D5_L = (D5_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD6_W() {
        return D6_L & 0xffff;
    }

    void setD6_W(int value) {
        D6_L = (D6_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD7_W() {
        return D7_L & 0xffff;
    }

    void setD7_W(int value) {
        D7_L = (D7_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getSR_W() {
        return sr & 0xffff;
    }

    void setSR_W(int value) {
        sr = (sr & 0xffff_0000) | (value & 0xffff);
    }

    public int getAndDecD0_W() {
        int v = D0_L & 0xffff;
        try {
            return v;
        } finally {
            D0_L = (D0_L & 0xffff0000) | ((v - 1) & 0xffff);
        }
    }

    public int getAndDecD1_W() {
        int v = D1_L & 0xffff;
        try {
            return v;
        } finally {
            D1_L = (D1_L & 0xffff0000) | ((v - 1) & 0xffff);
        }
    }

    public int getAndDecD2_W() {
        int v = D2_L & 0xffff;
        try {
            return v;
        } finally {
            D2_L = (D2_L & 0xffff0000) | ((v - 1) & 0xffff);
        }
    }

    public int getAndDecD4_W() {
        int v = D4_L & 0xffff;
        try {
            return v;
        } finally {
            D4_L = (D4_L & 0xffff0000) | ((v - 1) & 0xffff);
        }
    }
    public int getAndDecD5_W() {
        int v = D5_L & 0xffff;
        try {
            return v;
        } finally {
            D5_L = (D5_L & 0xffff0000) | ((v - 1) & 0xffff);
        }
    }

    public int getAndDecD7_W() {
        int v = D7_L & 0xffff;
        try {
            return v;
        } finally {
            D7_L = (D7_L & 0xffff0000) | ((v - 1) & 0xffff);
        }
    }

    public void setD0_L(int v) {
        D0_L = v;
    }

    public void setD1_L(int v) {
        D1_L = v;
    }

    public void setD2_L(int v) {
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
        return (a & 0xffff_ffffL) + (b & 0xffff_ffffL) > 0xffff_ffffL;
    }

    // TODO check
    @Override
    public int getAl(int index) {
        return switch (index) {
            case 0 -> a0;
            case 1 -> a1; // correct
            case 2 -> a2;
            case 3 -> a3;
            case 4 -> a4;
            case 5 -> a5;
            case 6 -> a6;
            case 7 -> a7;
            default -> throw new IllegalArgumentException(String.valueOf(index));
        };
    }
}

class Ab {
    static final int dummyAddress = 0xffff_ffff;

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
