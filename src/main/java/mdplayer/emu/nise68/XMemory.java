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
        reported = 0;
    }

    public void realloc(int size) {
        byte[] m = new byte[size];
        updateMask(size);
        if (mm != null && mm.length > 0) {
            int s = (Math.min(mm.length, size));
            System.arraycopy(mm, 0, m, 0, s);
            mm = m;
        }
        reported = 0;
    }

    /** how many accesses outside the memory are reported before the rest are passed over */
    private static final int reportLimit = 8;

    /** how many have been reported since the memory was (re)allocated */
    private int reported;

    /**
     * Reports an access outside the memory, up to {@link #reportLimit} times.
     * <p>
     * Song data that sends its driver reading at an address nowhere near its work area does so at
     * every interrupt, for as long as it plays: one line per access fills the log thousands of
     * times a second with the same message and buries everything else in it. The access itself is
     * already harmless here - a read gives 0 and a write is dropped - so once it has been said, it
     * is worth saying no more.
     */
    private void outOfBounds(int address) {
        if (reported >= reportLimit) return;
        reported++;
        logger.log(Level.WARNING, "index is out of bounds: %d, %d%s".formatted(address, mm.length,
                reported == reportLimit ? " (further ones are not reported)" : ""));
    }

    public void write(int v1, byte v2) {
//logger.log(Level.DEBUG, "%08x:%02x".formatted(v1, v2));
if ((v1 & mask) >= mm.length) {
 outOfBounds(v1 & mask);
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
 outOfBounds(v1 & mask);
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
