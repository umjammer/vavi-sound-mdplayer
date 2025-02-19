package mdplayer.driver.fmp.nise98;

import java.util.Stack;


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
    public short IP = 0;
    public short FLAG = (short) 0x8000;

    public short getES() {
        return sRegs[0];
    }

    public void setES(short value) {
        sRegs[0] = value;
    }

    public short getCS() {
        return sRegs[1];
    }

    public void setCS(short value) {
        sRegs[1] = value;
    }

    public short getSS() {
        return sRegs[2];
    }

    public void setSS(short value) {
        sRegs[2] = value;
    }

    public short getDS() {
        return sRegs[3];
    }

    public void setDS(short value) {
        sRegs[3] = value;
    }

    public short getAX() {
        return eRegs[0];
    }

    public void setAX(short value) {
        eRegs[0] = value;
    }

    public short getCX() {
        return eRegs[1];
    }

    public void decCX() {
        setCX((short) (getCX() - 1));
    }

    public void setCX(short value) {
        eRegs[1] = value;
    }

    public short getDX() {
        return eRegs[2];
    }

    public void setDX(short value) {
        eRegs[2] = value;
    }

    public short getBX() {
        return eRegs[3];
    }

    public void setBX(short value) {
        eRegs[3] = value;
    }

    public short getSP() {
        return eRegs[4];
    }

    public void addSP(int value) {
        setSP((short) (getSP() + value));
    }

    public void subSP(int value) {
        setSP((short) (getSP() - value));
    }

    public void setSP(short value) {
        eRegs[4] = value;
    }

    public short getBP() {
        return eRegs[5];
    }

    public void setBP(short value) {
        eRegs[5] = value;
    }

    public short getSI() {
        return eRegs[6];
    }

    public void addSI(int value) {
        setSI((short) (getSI() + value));
    }

    public void setSI(short value) {
        eRegs[6] = value;
    }

    public short getDI() {
        return eRegs[7];
    }

    public void addDI(int value) {
        setDI((short) (getDI() + value));
    }

    public void setDI(short value) {
        eRegs[7] = value;
    }

    public byte getAL() {
        return (byte) eRegs[0];
    }

    public void setAL(short value) {
        eRegs[0] &= (short) 0xff00;
        eRegs[0] |= (short) value;
    }

    public short getAH() {
        return (byte) (eRegs[0] >> 8);
    }

    public void setAH(short value) {
        eRegs[0] &= (short) 0x00ff;
        eRegs[0] |= (short) (value << 8);
    }

    public short getCL() {
        return (byte) eRegs[1];
    }

    public void setCL(short value) {
        eRegs[1] &= (short) 0xff00;
        eRegs[1] |= (short) value;
    }

    public Byte getCH() {
        return (byte) (eRegs[1] >> 8);
    }

    public void setCH(Byte value) {
        eRegs[1] &= (short) 0x00ff;
        eRegs[1] |= (short) (value << 8);
    }

    public Byte getDL() {
        return (byte) eRegs[2];
    }

    public void setDL(Byte value) {
        eRegs[2] &= (short) 0xff00;
        eRegs[2] |= (short) value;
    }

    public Byte getDH() {
        return (byte) (eRegs[2] >> 8);
    }

    public void setDH(Byte value) {
        eRegs[2] &= (short) 0x00ff;
        eRegs[2] |= (short) (value << 8);
    }

    public Byte getBL() {
        return (byte) eRegs[3];
    }

    public void setBL(Byte value) {
        eRegs[3] &= (short) 0xff00;
        eRegs[3] |= (short) value;
    }

    public Byte getBH() {
        return (byte) (eRegs[3] >> 8);
    }

    public void setBH(Byte value) {
        eRegs[3] &= (short) 0x00ff;
        eRegs[3] |= (short) (value << 8);
    }

    public int getCS_IP() {
        {
            return ((short) getCS() << 4) + (short) IP;
        }
    }

    public int getDS_DX() {
        {
            return ((short) getDS() << 4) + (short) getDX();
        }
    }

    public int getDS_SI() {
        {
            return ((short) getDS() << 4) + (short) getSI();
        }
    }

    public int getDS_DI() {
        {
            return ((short) getDS() << 4) + (short) getDI();
        }
    }

    public int getES_DI() {
        {
            return ((short) getES() << 4) + (short) getDI();
        }
    }

    public int getSS_SP() {
        {
            return ((short) getSS() << 4) + (short) getSP();
        }
    }

    // CF bit0
    public boolean getCF() {
        return ((FLAG & (1 << 0)) != 0);
    }

    public void setCF(boolean value) {
        FLAG &= ~(1 << 0);
        FLAG |= (short) (value ? (1 << 0) : 0);
    }

    // PF bit2
    public boolean getPF() {
        return ((FLAG & (1 << 2)) != 0);
    }

    public void setPF(boolean value) {
        FLAG &= ~(1 << 2);
        FLAG |= (short) (value ? (1 << 2) : 0);
    }

    // AF bit4
    public boolean getAF() {
        return ((FLAG & (1 << 4)) != 0);
    }

    public void setAF(boolean value) {
        FLAG &= ~(1 << 4);
        FLAG |= (short) (value ? (1 << 4) : 0);
    }

    // ZF bit6
    public boolean getZF() {
        return ((FLAG & (1 << 6)) != 0);
    }

    public void setZF(boolean value) {
        FLAG &= ~(1 << 6);
        FLAG |= (short) (value ? (1 << 6) : 0);
    }

    // SF bit7
    public boolean getSF() {
        return ((FLAG & (1 << 7)) != 0);
    }

    public void setSF(boolean value) {
        FLAG &= ~(1 << 7);
        FLAG |= (short) (value ? (1 << 7) : 0);
    }

    // TF bit8
    public boolean getTF() {
        return ((FLAG & (1 << 8)) != 0);
    }

    public void setTF(boolean value) {
        FLAG &= ~(1 << 8);
        FLAG |= (short) (value ? (1 << 8) : 0);
    }

    // IF bit9
    public boolean getIF() {
        return ((FLAG & (1 << 9)) != 0);
    }

    public void setIF(boolean value) {
        FLAG &= ~(1 << 9);
        FLAG |= (short) (value ? (1 << 9) : 0);
    }

    // DF bit10
    public boolean getDF() {
        return ((FLAG & (1 << 10)) != 0);
    }

    public void setDF(boolean value) {
        FLAG &= ~(1 << 10);
        FLAG |= (short) (value ? (1 << 10) : 0);
    }

    // OF bit11
    public boolean getOF() {
        return ((FLAG & (1 << 11)) != 0);
    }

    public void setOF(boolean value) {
        FLAG &= ~(1 << 11);
        FLAG |= (short) (value ? (1 << 11) : 0);
    }

    public int AuxVal, OverVal, SignVal, ZeroVal, CarryVal, DirVal;      /* 0 or non-0 valued flags */
    public byte ParityVal;

    public void SetSZPFb(byte ans) {
        SignVal = (byte) ans;
        setSF(SignVal < 0);
        ZeroVal = ans;
        setZF(ZeroVal == 0);
        ParityVal = ans;
        setPF(parity_table[ParityVal & 0xff]);
    }

    public void SetSZPFw(short ans) {
        SignVal = (short) ans;
        setSF(SignVal < 0);
        ZeroVal = ans;
        setZF(ZeroVal == 0);
        ParityVal = (byte) ans;
        setPF(parity_table[ParityVal & 0xff]);
    }

    public void SetCFb(short a) {
        CarryVal = (a) & 0x100;
        setCF(CarryVal != 0);
    }

    public void SetCFw(int a) {
        CarryVal = (int) ((a) & 0x10000);
        setCF(CarryVal != 0);
    }

    public void SetAF(byte a, byte b, byte ans) {
        AuxVal = ((ans) ^ ((a) ^ (b))) & 0x10;
        setAF(AuxVal != 0);
    }

    // ans = a - b
    // の時のOF判定
    // 事前にSFの判定を行っておくこと
    public void SetOFwSub(short a, short b, short ans) {
        // OF = SF
        //    ? ((b > 0 && ans > a) || (b < 0 && ans < a))
        //    : ans > a;

        OverVal = ((b ^ a) & (b ^ ans) & 0x8000);
        setOF(OverVal != 0);
    }

    public void SetOFbSub(byte a, byte b, byte ans) {
        // OF = SF
        //    ? ((b > 0 && ans > a) || (b < 0 && ans < a))
        //    : ans > a;
        OverVal = ((b ^ a) & (b ^ ans) & 0x80);
        setOF(OverVal != 0);
    }

    public void SetOFwAdd(short a, short b, short ans) {
        // OF = SF
        //    ? ((a >= 0 && ans < b) || (a < 0 && ans > b))
        //    : (ans < a || ans < b);
        OverVal = (((ans) ^ (a)) & ((ans) ^ (b)) & 0x8000);
    }

    public void SetOFbAdd(byte a, byte b, byte ans) {
        // OF = SF
        //    ? ((a >= 0 && ans < b) || (a < 0 && ans > b))
        //    : (ans < a || ans < b);
        OverVal = (((ans) ^ (a)) & ((ans) ^ (b)) & 0x80);
    }

    @Override
    public String toString() {
        return String.format(
                "AX:{0:X04} CX:{1:X04} DX:{2:X04} BX:{3:X04} SP:{4:X04} BP:{5:X04} SI:{6:X04} DI:{7:X04} \r\n"
                        + "ES:{8:X04} CS:{9:X04} SS:{10:X04} DS:{11:X04} IP:{12:X04} FLAG:{13}",
                getAX(), getCX(), getDX(), getBX(), getSP(), getBP(), getSI(), getDI(),
                getES(), getCS(), getSS(), getDS(), IP, String.format(
                        "{16:X04}[{15}{14}{13}{12}{11}{10}{9}{8}{7}{6}{5}{4}{3}{2}{1}{0}]",
                        getCF() ? "C" : "-",
                        ".",
                        getPF() ? "P" : "-",
                        ".",
                        getAF() ? "A" : "-",
                        ".",
                        getZF() ? "Z" : "-",
                        getSF() ? "S" : "-",
                        getTF() ? "T" : "-",
                        getIF() ? "I" : "-",
                        getDF() ? "D" : "-",
                        getOF() ? "O" : "-",
                        ".",
                        ".",
                        ".",
                        ".",
                        FLAG
                )
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

            parity_table[i] = ((c & 1) == 0);
        }
    }

    private Stack<Short> regStack = new Stack<>();
    private boolean[] parity_table;

}
