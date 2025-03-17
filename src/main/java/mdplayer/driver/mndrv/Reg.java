package mdplayer.driver.mndrv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        D0_L = (D0_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD1_B() {
        return (byte) D1_L;
    }

    void setD1_B(int value) {
        D1_L = (D1_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD2_B() {
        return (byte) D2_L;
    }

    void setD2_B(int value) {
        D2_L = (D2_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD3_B() {
        return (byte) D3_L;
    }

    void setD3_B(int value) {
        D3_L = (D3_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD4_B() {
        return (byte) D4_L;
    }

    void setD4_B(int value) {
        D4_L = (D4_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD5_B() {
        return (byte) D5_L;
    }

    void setD5_B(int value) {
        D5_L = (D5_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD6_B() {
        return (byte) D6_L;
    }

    void setD6_B(int value) {
        D6_L = (D6_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD7_B() {
        return (byte) D7_L;
    }

    void setD7_B(int value) {
        D7_L = (D7_L & 0xffff_ff00) | (value & 0xff);
    }

    public int getD0_W() {
        return D0_L;
    }

    void setD0_W(int value) {
        D0_L = (D0_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD1_W() {
        return D1_L;
    }

    void setD1_W(int value) {
        D1_L = (D1_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD2_W() {
        return D2_L;
    }

    void setD2_W(int value) {
        D2_L = (D2_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD3_W() {
        return D3_L;
    }

    void setD3_W(int value) {
        D3_L = (D3_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD4_W() {
        return D4_L;
    }

    void setD4_W(int value) {
        D4_L = (D4_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD5_W() {
        return D5_L;
    }

    void setD5_W(int value) {
        D5_L = (D5_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD6_W() {
        return D6_L;
    }

    void setD6_W(int value) {
        D6_L = (D6_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getD7_W() {
        return D7_L;
    }

    void setD7_W(int value) {
        D7_L = (D7_L & 0xffff_0000) | (value & 0xffff);
    }

    public int getSR_W() {
        return sr;
    }

    void setSR_W(int value) {
        sr = (sr & 0xffff_0000) | (value & 0xffff);
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
        return (long) a + (long) b > (long) 0xffff_ffff;
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
