package mdplayer.emu.nise68;

public class Register68 {

    private int[] d = new int[8];

    public int[] getD() {
        return d;
    }

    public void setD(int[] value) {
        d = value;
    }

    private A a = new A();

    public A getA() {
        return a;
    }

    public void setA(A value) {
        a = value;
    }

    public int pc;

    public int getUSP() {
        return a.usp;
    }

    public void setUSP(int value) {
        a.usp = value;
    }

    public int getSSP() {
        return a.ssp;
    }

    public void setSSP(int value) {
        a.ssp = value;
    }

    public short getSR() {
        return a.sr;
    }

    public void setSR(short value) {
        a.sr = value;
    }

    public short getSRbk() {
        return a.srBk;
    }

    public void setSRbk(short value) {
        a.srBk = value;
    }

    public byte getCCR() {
        return (byte) a.sr;
    }

    public void setCCR(byte value) {
        a.sr = (short) ((a.sr & 0xffe0) | (value & 0x1f));
    }

    public boolean getX() {
        return (a.sr & 0x0010) != 0;
    }

    public void setX(boolean value) {
        a.sr = (short) ((a.sr & 0xffef) | (value ? 0x0010 : 0x00));
    }

    public boolean getN() {
        return (a.sr & 0x0008) != 0;
    }

    public void setN(boolean value) {
        a.sr = (short) ((a.sr & 0xfff7) | (value ? 0x0008 : 0x00));
    }

    public boolean getZ() {
        return (a.sr & 0x0004) != 0;
    }

    public void setZ(boolean value) {
        a.sr = (short) ((a.sr & 0xfffb) | (value ? 0x0004 : 0x00));
    }

    public boolean getV() {
        return (a.sr & 0x0002) != 0;
    }

    public void setV(boolean value) {
        a.sr = (short) ((a.sr & 0xfffd) | (value ? 0x0002 : 0x00));
    }

    public boolean getC() {
        return (a.sr & 0x0001) != 0;
    }

    public void setC(boolean value) {
        a.sr = (short) ((a.sr & 0xfffe) | (value ? 0x0001 : 0x00));
    }

    public boolean getS() {
        return (a.sr & 0x2000) != 0;
    }

    public void setS(boolean value) {
        a.sr = (short) ((a.sr & 0xdfff) | (value ? 0x2000 : 0x00));
    }

    public boolean getT() {
        return (a.sr & 0x8000) != 0;
    }

    public void setT(boolean value) {
        a.sr = (short) ((a.sr & 0x7fff) | (value ? 0x8000 : 0x00));
    }

    @Override
    public String toString() {
        return
                // "D0:%08x D1:%08x D2:%08x D3:%08x D4:%08x D5:%08x D6:%08x D7:%08x\n"
                // + "A0:%08x A1:%08x A2:%08x A3:%08x A4:%08x A5:%08x A6:%08x A7:%08x\n"
                // + "PC:%08x USP:%08x SSP:%08x SR:%04x  X:%s N:%s Z:%s V:%s C:%s %s %s",
                """
                D0-D7=%1$08x,%2$08x,%3$08x,%4$08x,%5$08x,%6$08x,%7$08x,%8$08x
                A0-A7=%9$08x,%10$08x,%11$08x,%12$08x,%13$08x,%14$08x,%15$08x,%16$08x
                  PC=%17$08x   USP=%18$08x   SSP=%19$08x    SR=%20$04x""".formatted(
                        d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7],
                        a.get(0), a.get(1), a.get(2), a.get(3), a.get(4), a.get(5), a.get(6), a.get(7),
                        pc, getUSP(), getSSP(), getSR() & 0xffff,
                        ((getSR() & 0x8000) != 0 ? "[Trace]" : ""),
                        ((getSR() & 0x2000) != 0 ? "[Super]" : "[User]"),
                        getX() ? "*" : ".",
                        getN() ? "*" : ".",
                        getZ() ? "*" : ".",
                        getV() ? "*" : ".",
                        getC() ? "*" : "."
                );
    }

    public void setC(short before, short after) {
        setC((before & 0xff00) != (after & 0xff00));
    }

    public void setC(int before, int after) {
        setC((before & 0xffff_0000) != (after & 0xffff_0000));
    }

    public void setC(long before, long after) {
        setC((before & 0xffff_ffff_0000_0000L) != (after & 0xffff_ffff_0000_0000L));
    }

    public void setV(byte before, byte after) {
        setV((before & 0x80) != (after & 0x80));
    }

    public void setV(short before, short after) {
        setV((before & 0x8000) != (after & 0x8000));
    }

    public void setV(int before, int after) {
        setV((before & 0x8000_0000) != (after & 0x8000_0000));
    }

    public void setZ(byte after) {
        setZ(after == 0);
    }

    public void setZ(short after) {
        setZ(after == 0);
    }

    public void setZ(int after) {
        setZ(after == 0);
    }

    public void setN(byte after) {
        setN((after & 0x80) != 0);
    }

    public void setN(short after) {
        setN((after & 0x8000) != 0);
    }

    public void setN(int after) {
        setN((after & 0x8000_0000) != 0);
    }

    public void setDb(int n, byte val) {
        d[n] = (d[n] & 0xffff_ff00) | (val & 0xff);
    }

    public void setDw(int n, short val) {
        d[n] = (d[n] & 0xffff_0000) | (val & 0xffff);
    }

    public void setDl(int n, int val) {
        d[n] = val;
    }

    public byte getDb(int n) {
        return (byte) d[n];
    }

    public short getDw(int n) {
        return (short) d[n];
    }

    public int getDl(int n) {
        return d[n];
    }

    public void setAb(int n, byte val) {
        a.set(n, val);
    }

    public void setAw(int n, short val) {
        a.set(n, val);
    }

    public void setAl(int n, int val) {
        a.set(n, val);
    }

    public byte getAb(int n) {
        return (byte) a.get(n);
    }

    public short getAw(int n) {
        return (short) a.get(n);
    }

    public int getAl(int n) {
        return a.get(n);
    }

    //
    // @see "run68"
    //

    public void setVadd(byte src, byte dst, byte after) {
        boolean s = ((src & 0x80) != 0);
        boolean d = ((dst & 0x80) != 0);
        boolean a = ((after & 0x80) != 0);
        setV((s && d && !a) || (!s && !d && a));
    }

    public void setVadd(short src, short dst, short after) {
        boolean s = ((src & 0x8000) != 0);
        boolean d = ((dst & 0x8000) != 0);
        boolean a = ((after & 0x8000) != 0);
        setV((s && d && !a) || (!s && !d && a));
    }

    public void setVadd(int src, int dst, int after) {
        boolean s = ((src & 0x8000_0000) != 0);
        boolean d = ((dst & 0x8000_0000) != 0);
        boolean a = ((after & 0x8000_0000) != 0);
        setV((s && d && !a) || (!s && !d && a));
    }

    public void setVcmp(byte src, byte dst, byte after) {
        boolean s = ((src & 0x80) != 0);
        boolean d = ((dst & 0x80) != 0);
        boolean a = ((after & 0x80) != 0);
        setV((!s && d && !a) || (s && !d && a));
    }

    public void setVcmp(short src, short dst, short after) {
        boolean s = ((src & 0x8000) != 0);
        boolean d = ((dst & 0x8000) != 0);
        boolean a = ((after & 0x8000) != 0);
        setV((!s && d && !a) || (s && !d && a));
    }

    public void setVcmp(int src, int dst, int after) {
        boolean s = ((src & 0x8000_0000) != 0);
        boolean d = ((dst & 0x8000_0000) != 0);
        boolean a = ((after & 0x8000_0000) != 0);
        setV((!s && d && !a) || (s && !d && a));
    }

    public void setVneg(byte dst, byte after) {
        boolean d = ((dst & 0x80) != 0);
        boolean a = ((after & 0x80) != 0);
        setV((d && a));
    }

    public void setVneg(short dst, short after) {
        boolean d = ((dst & 0x8000) != 0);
        boolean a = ((after & 0x8000) != 0);
        setV((d && a));
    }

    public void setVneg(int dst, int after) {
        boolean d = ((dst & 0x8000_0000) != 0);
        boolean a = ((after & 0x8000_0000) != 0);
        setV((d && a));
    }


    public void setCadd(byte src, byte dst, byte after) {
        boolean s = ((src & 0x80) != 0);
        boolean d = ((dst & 0x80) != 0);
        boolean a = ((after & 0x80) != 0);
        setC((s && d) || (d && !a) || (s && !a));
    }

    public void setCadd(short src, short dst, short after) {
        boolean s = ((src & 0x8000) != 0);
        boolean d = ((dst & 0x8000) != 0);
        boolean a = ((after & 0x8000) != 0);
        setC((s && d) || (d && !a) || (s && !a));
    }

    public void setCadd(int src, int dst, int after) {
        boolean s = ((src & 0x8000_0000) != 0);
        boolean d = ((dst & 0x8000_0000) != 0);
        boolean a = ((after & 0x8000_0000) != 0);
        setC((s && d) || (d && !a) || (s && !a));
    }

    public void setCcmp(byte src, byte dst, byte after) {
        boolean s = ((src & 0x80) != 0);
        boolean d = ((dst & 0x80) != 0);
        boolean a = ((after & 0x80) != 0);
        setC((s && !d) || (!d && a) || (s && a));
    }

    public void setCcmp(short src, short dst, short after) {
        boolean s = ((src & 0x8000) != 0);
        boolean d = ((dst & 0x8000) != 0);
        boolean a = ((after & 0x8000) != 0);
        setC((s && !d) || (!d && a) || (s && a));
    }

    public void setCcmp(int src, int dst, int after) {
        boolean s = ((src & 0x8000_0000) != 0);
        boolean d = ((dst & 0x8000_0000) != 0);
        boolean a = ((after & 0x8000_0000) != 0);
        setC((s && !d) || (!d && a) || (s && a));
    }

    public void setCneg(byte dst, byte after) {
        boolean d = ((dst & 0x80) != 0);
        boolean a = ((after & 0x80) != 0);
        setV((d || a));
    }

    public void setCneg(short dst, short after) {
        boolean d = ((dst & 0x8000) != 0);
        boolean a = ((after & 0x8000) != 0);
        setV((d || a));
    }

    public void setCneg(int dst, int after) {
        boolean d = ((dst & 0x8000_0000) != 0);
        boolean a = ((after & 0x8000_0000) != 0);
        setV((d || a));
    }

    public static class A {

        /** field for Items property */
        private final int[] items = new int[8];
        public int usp;
        public int ssp;
        public short sr;
        public short srBk;

        public int get(int index) { // indexer
            if (index == 7) {
                if ((sr & 0x2000) != 0) return ssp;
                else return usp;
            }
            return items[index];
        }

        public void set(int index, int value) {
            if (index == 7) {
                if ((sr & 0x2000) != 0) ssp = value;
                else usp = value;
            } else
                items[index] = value;
        }
    }
}
