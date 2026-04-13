package mdplayer.driver.mxdrv;

import java.nio.ByteBuffer;

import mdplayer.emu.nise68.XMemory;


public class Depend {
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int SET = 255;
    public static final int CLR = 0;

    public static short getBWord(XMemory mm, int a) {
        return (short) (((mm.readByte(a + 0) & 0xff) * 0x100) + (mm.readByte(a + 1) & 0xff));
    }

    public static int getBLong(XMemory mm, int a) {
        return ((mm.readByte(a + 0) & 0xff) * 0x100_0000) + ((mm.readByte(a + 1) & 0xff) * 0x1_0000) +
                      ((mm.readByte(a + 2) & 0xff) * 0x100) + (mm.readByte(a + 3) & 0xff);
    }

    public static void putBWord(ByteBuffer a, short b) {
        a.put(0, (byte) ((b & 0xff00) >> 8));
        a.put(1, (byte) ((b & 0xff) >> 0));
    }

    public static void putBLong(XMemory mm, int a, int b) {
        mm.write(a + 0, (byte) ((b & 0xff00_0000) >> 24));
        mm.write(a + 1, (byte) ((b & 0xff_0000) >> 16));
        mm.write(a + 2, (byte) ((b & 0xff00) >> 8));
        mm.write(a + 3, (byte) ((b & 0xff) >> 0));
    }
}

class X68Reg {
    public int d0;
    public int d1;
    public int d2;
    public int d3;
    public int d4;
    public int d5;
    public int d6;
    public int d7;
    public int a0;
    public int a1;
    public int a2;
    public int a3;
    public int a4;
    public int a5;
    public int a6;
    public int a7;
}
