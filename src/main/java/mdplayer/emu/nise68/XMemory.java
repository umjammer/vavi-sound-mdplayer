package mdplayer.emu.nise68;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;


public class XMemory {

    private static final Logger logger = System.getLogger(XMemory.class.getName());

    public byte[] mm;
    private int mask;

    private void updateMask(int size) {
        int len = Integer.toHexString(size).length();
        mask = Integer.parseInt("f".repeat(len), 16);
    }

    public void alloc(int size) {
        mm = new byte[size];
        updateMask(size);
    }

    public void realloc(int size) {
        byte[] m = new byte[size];
        updateMask(size);
        if (mm != null && mm.length > 0) {
            int s = (Math.min(mm.length, size));
            System.arraycopy(mm, 0, m, 0, s);
            mm = m;
        }
    }

    public void write(int v1, byte v2) {
//logger.log(Level.DEBUG, "%08x:%02x".formatted(v1, v2));
if ((v1 & mask) >= mm.length) {
 logger.log(Level.WARNING, "index is out of bounds: %d, %d".formatted(v1 & mask, mm.length));
 return;
}
        mm[v1 & mask] = v2;
    }

    public void write(int v1, short v2) {
        write(v1, (byte) ((v2 & 0xff00) >> 8));
        write(v1 + 1, (byte) ((v2 & 0xff) >> 0));
    }

    public void write(int v1, int v2) {
        write(v1, (byte) ((v2 & 0xff00_0000) >> 24));
        write(v1 + 1, (byte) ((v2 & 0xff_0000) >> 16));
        write(v1 + 2, (byte) ((v2 & 0xff00) >> 8));
        write(v1 + 3, (byte) ((v2 & 0xff) >> 0));
    }

    public byte readByte(int v1) {
if ((v1 & mask) >= mm.length) {
 logger.log(Level.WARNING, "index is out of bounds: %d, %d".formatted(v1 & mask, mm.length));
 return 0;
}
        return mm[v1 & mask];
    }

    public short readShort(int v1) {
        return (short) (((readByte(v1) & 0xff) << 8) + ((readByte(v1 + 1) & 0xff) << 0));
    }

    public int readInt(int v1) {
        return ((readByte(v1) & 0xff) << 24) +
                ((readByte(v1 + 1) & 0xff) << 16) +
                ((readByte(v1 + 2) & 0xff) << 8) +
                ((readByte(v1 + 3) & 0xff) << 0);
    }
}
