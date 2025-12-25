package mdplayer.driver.fmp.nise98;

import java.util.Stack;


// TODO c# int16 is signed
public class Register286 {

    // eRegs     sRegs
    // 0 ... AX  0 ... ES
    // 1 ... CX  1 ... CS
    // 2 ... DX  2 ... SS
    // 3 ... BX  3 ... DS
    // 4 ... SP
    // 5 ... BP
    // 6 ... SI
    // 7 ... DI
    public short[] eRegs = new short[8];
    public short[] sRegs = new short[4];
    public short ip = 0;
    public short flag = (short) 0x8000;

    // es: 0
    public short getES() {
        return sRegs[0];
    }

    public void setES(short value) {
        sRegs[0] = value;
    }

    // cs: 1
    public short getCS() {
        return sRegs[1];
    }

    public void setCS(short value) {
        sRegs[1] = value;
    }

    // ss: 2
    public short getSS() {
        return sRegs[2];
    }

    public void setSS(short value) {
        sRegs[2] = value;
    }

    // ----

    // ds: 3
    public short getDS() {
        return sRegs[3];
    }

    public void setDS(short value) {
        sRegs[3] = value;
    }

    // ax: 0
    public short getAX() {
        return eRegs[0];
    }

    public void setAX(short value) {
        eRegs[0] = value;
    }

    // cx: 1
    public short getCX() {
        return eRegs[1];
    }

    public void decCX() {
        eRegs[1]--;
    }

    public void setCX(short value) {
        eRegs[1] = value;
    }

    // dx: 2
    public short getDX() {
        return eRegs[2];
    }

    public void setDX(short value) {
        eRegs[2] = value;
    }

    // bx: 3
    public short getBX() {
        return eRegs[3];
    }

    public void setBX(short value) {
        eRegs[3] = value;
    }

    // sp: 4
    public short getSP() {
        return eRegs[4];
    }

    public void addSP(int value) {
        eRegs[4] = (short) (eRegs[4] + value);
    }

    public void subSP(int value) {
        eRegs[4] -= value;
    }

    public void setSP(short value) {
        eRegs[4] = value;
    }

    // bp: 5
    public short getBP() {
        return eRegs[5];
    }

    public void setBP(short value) {
        eRegs[5] = value;
    }

    // si: 6
    public short getSI() {
        return eRegs[6];
    }

    public void addSI(int value) {
        eRegs[6] += value;
    }

    public void setSI(short value) {
        eRegs[6] = value;
    }

    // di: 7
    public short getDI() {
        return eRegs[7];
    }

    public void addDI(int value) {
        eRegs[7] += value;
    }

    public void setDI(short value) {
        eRegs[7] = value;
    }

    // ----

    //
    public byte getAL() {
        return (byte) eRegs[0];
    }

    public void setAL(byte value) {
        eRegs[0] &= (short) 0xff00;
        eRegs[0] |= (value & 0xff);
    }

    public byte getAH() {
        return (byte) (eRegs[0] >> 8);
    }

    public void setAH(byte value) {
        eRegs[0] &= (short) 0x00ff;
        eRegs[0] |= (short) ((value & 0x00ff) << 8);
    }

    //
    public byte getCL() {
        return (byte) eRegs[1];
    }

    public void setCL(byte value) {
        eRegs[1] &= (short) 0xff00;
        eRegs[1] |= (value & 0xff);
    }

    public byte getCH() {
        return (byte) (eRegs[1] >> 8);
    }

    public void setCH(byte value) {
        eRegs[1] &= (short) 0x00ff;
        eRegs[1] |= (short) ((value & 0xff) << 8);
    }

    //
    public byte getDL() {
        return (byte) eRegs[2];
    }

    public void setDL(byte value) {
        eRegs[2] &= (short) 0xff00;
        eRegs[2] |= (short) (value & 0xff);
    }

    public byte getDH() {
        return (byte) (eRegs[2] >> 8);
    }

    public void setDH(byte value) {
        eRegs[2] &= (short) 0x00ff;
        eRegs[2] |= (short) ((value & 0xff) << 8);
    }

    //
    public byte getBL() {
        return (byte) eRegs[3];
    }

    public void setBL(byte value) {
        eRegs[3] &= (short) 0xff00;
        eRegs[3] |= (short) (value & 0xff);
    }

    public byte getBH() {
        return (byte) (eRegs[3] >> 8);
    }

    public void setBH(byte value) {
        eRegs[3] &= (short) 0x00ff;
        eRegs[3] |= (short) ((value & 0xff) << 8);
    }

    // ----

    public int getCS_IP() {
        return ((getCS() & 0xffff) << 4) + (ip & 0xffff);
    }

    public int getDS_DX() {
        return ((getDS() & 0xffff) << 4) + (getDX() & 0xffff);
    }

    public int getDS_SI() {
        return ((getDS() & 0xffff) << 4) + (getSI() & 0xffff);
    }

    public int getDS_DI() {
        return ((getDS() & 0xffff) << 4) + (getDI() & 0xffff);
    }

    public int getES_DI() {
        return ((getES() & 0xffff) << 4) + (getDI() & 0xffff);
    }

    public int getSS_SP() {
        return ((getSS() & 0xffff) << 4) + (getSP() & 0xffff);
    }

    // ----

    // CF bit0
    public boolean isCF() {
        return (flag & (1 << 0)) != 0;
    }

    public void setCF(boolean value) {
        flag &= ~(1 << 0);
        flag |= (short) (value ? (1 << 0) : 0);
    }

    // PF bit2
    public boolean isPF() {
        return (flag & (1 << 2)) != 0;
    }

    public void setPF(boolean value) {
        flag &= ~(1 << 2);
        flag |= (short) (value ? (1 << 2) : 0);
    }

    // AF bit4
    public boolean isAF() {
        return (flag & (1 << 4)) != 0;
    }

    public void setAF(boolean value) {
        flag &= ~(1 << 4);
        flag |= (short) (value ? (1 << 4) : 0);
    }

    // ZF bit6
    public boolean isZF() {
        return (flag & (1 << 6)) != 0;
    }

    public void setZF(boolean value) {
        flag &= ~(1 << 6);
        flag |= (short) (value ? (1 << 6) : 0);
    }

    // SF bit7
    public boolean isSF() {
        return (flag & (1 << 7)) != 0;
    }

    public void setSF(boolean value) {
        flag &= ~(1 << 7);
        flag |= (short) (value ? (1 << 7) : 0);
    }

    // TF bit8
    public boolean isTF() {
        return (flag & (1 << 8)) != 0;
    }

    public void setTF(boolean value) {
        flag &= ~(1 << 8);
        flag |= (short) (value ? (1 << 8) : 0);
    }

    // IF bit9
    public boolean isIF() {
        return (flag & (1 << 9)) != 0;
    }

    public void setIF(boolean value) {
        flag &= ~(1 << 9);
        flag |= (short) (value ? (1 << 9) : 0);
    }

    // DF bit10
    public boolean isDF() {
        return (flag & (1 << 10)) != 0;
    }

    public void setDF(boolean value) {
        flag &= ~(1 << 10);
        flag |= (short) (value ? (1 << 10) : 0);
    }

    // OF bit11
    public boolean isOF() {
        return (flag & (1 << 11)) != 0;
    }

    public void setOF(boolean value) {
        flag &= ~(1 << 11);
        flag |= (short) (value ? (1 << 11) : 0);
    }

    public int auxVal, overVal, signVal, zeroVal, carryVal, dirVal; // 0 or non-0 valued flags
    public byte parityVal;

    public void setSZPFb(byte ans) {
        signVal = ans;
        setSF(signVal < 0);
        zeroVal = ans & 0xff;
        setZF(zeroVal == 0);
        parityVal = ans;
        setPF(parity_table[parityVal & 0xff]);
    }

    public void setSZPFw(short ans) {
        signVal = ans;
        setSF(signVal < 0);
        zeroVal = ans & 0xffff;
        setZF(zeroVal == 0);
        parityVal = (byte) ans;
        setPF(parity_table[parityVal & 0xff]);
    }

    public void setCFb(short a) {
        carryVal = a & 0x100;
        setCF(carryVal != 0);
    }

    public void setCFw(int a) {
        carryVal = a & 0x1_0000;
        setCF(carryVal != 0);
    }

    public void setAF(byte a, byte b, byte ans) {
        auxVal = ((ans & 0xff) ^ ((a & 0xff) ^ (b & 0xff))) & 0x10;
        setAF(auxVal != 0);
    }

    // OF determination when ans = a - b
    // SF determination must be performed in advanceans = a - b
    public void setOFwSub(short a, short b, short ans) {
        // OF = SF
        //    ? ((b > 0 && ans > a) || (b < 0 && ans < a))
        //    : ans > a;

        overVal = ((b & 0xffff) ^ (a & 0xffff)) & ((b & 0xffff) ^ (ans & 0xffff)) & 0x8000;
        setOF(overVal != 0);
    }

    public void setOFbSub(byte a, byte b, byte ans) {
        // OF = SF
        //    ? ((b > 0 && ans > a) || (b < 0 && ans < a))
        //    : ans > a;
        overVal = ((b & 0xff) ^ (a & 0xff)) & ((b & 0xff) ^ (ans & 0xff)) & 0x80;
        setOF(overVal != 0);
    }

    public void setOFwAdd(short a, short b, short ans) {
        // OF = SF
        //    ? ((a >= 0 && ans < b) || (a < 0 && ans > b))
        //    : (ans < a || ans < b);
        overVal = ((ans & 0xffff) ^ (a & 0xffff)) & ((ans & 0xffff) ^ (b & 0xffff)) & 0x8000;
    }

    public void setOFbAdd(byte a, byte b, byte ans) {
        // OF = SF
        //    ? ((a >= 0 && ans < b) || (a < 0 && ans > b))
        //    : (ans < a || ans < b);
        overVal = ((ans & 0xff) ^ (a & 0xff)) & ((ans & 0xff) ^ (b & 0xff)) & 0x80;
    }

    @Override
    public String toString() {
        return """
                %nAX:%04x CX:%04x DX:%04x BX:%04x SP:%04x BP:%04x SI:%04x DI:%04x\s
                ES:%04x CS:%04x SS:%04x DS:%04x IP:%04x FLAG:[%s.%s.%s.%s%s%s%s%s%s....%04x]
                """.formatted(
                getAX(), getCX(), getDX(), getBX(), getSP(), getBP(), getSI(), getDI(),
                getES(), getCS(), getSS(), getDS(), ip,
                (isCF() ? "C" : "-"),
                (isPF() ? "P" : "-"),
                (isAF() ? "A" : "-"),
                (isZF() ? "Z" : "-"),
                (isSF() ? "S" : "-"),
                (isTF() ? "T" : "-"),
                (isIF() ? "I" : "-"),
                (isDF() ? "D" : "-"),
                (isOF() ? "O" : "-"),
                flag & 0xffff
        );
    }

    public Register286() {
        parity_table = new boolean[256];
        for (int i = 0; i < 256; i++) {
            int c = 0;
            for (int j = 0; j < 8; j++) {
                if ((i & (1 << j)) != 0)
                    c++;
            }

            parity_table[i] = (c & 1) == 0;
        }
    }

    private final Stack<Short> regStack = new Stack<>();
    private final boolean[] parity_table;
}
