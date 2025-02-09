
package mdplayer.driver.mxdrv;

public class XMemory {
    public byte[] mm;

    public void alloc(int size) {
        mm = new byte[size];
    }

    public void realloc(int size) {
        byte[] m = new byte[size];
        if (mm != null && mm.length > 0) {
            int s = (Math.min(mm.length, size));
            System.arraycopy(mm, 0, m, 0, s);
            mm = m;
        }
    }

    public void write(int v1, byte v2) {
// logger.log(Level.TRACE, "%08x:%02x".formatted(v1, v2));
        mm[v1] = v2;
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
        return mm[v1];
    }

    public short readShort(int v1) {
        return (short) (((mm[v1] & 0xff) << 8) + ((mm[v1 + 1] & 0xff) << 0));
    }

    public int readInt(int v1) {
        return ((mm[v1] & 0xff) << 24) + ((mm[v1 + 1] & 0xff) << 16) + ((mm[v1 + 2] & 0xff) << 8) + ((mm[v1 + 3] & 0xff) << 0);
    }
}
