
package mdplayer.driver.sid;

import java.nio.ByteBuffer;


public class mem {
    public static void memset(byte[] des, byte val, int length) {
        for (int i = 0; i < length; i++)
            des[i] = val;
    }

    public static void memset(ByteBuffer des, byte val, int length) {
        for (int i = 0; i < length; i++)
            des.put(i,  val);
    }

    public static void memcpy(byte[] des, byte[] src, int length) {
        if (length >= 0) System.arraycopy(src, 0, des, 0, length);
    }

    public static void memcpy(byte[] des, ByteBuffer src, int length) {
        for (int i = 0; i < length; i++)
            des[i] = src.get(i);
    }

    public static void memcpy(ByteBuffer des, byte[] src, int length) {
        for (int i = 0; i < length; i++)
            des.put(i, src[i]);
    }

    public static void memcpy(ByteBuffer des, ByteBuffer src, int length) {
        for (int i = 0; i < length; i++)
            des.put(i, src.get(i));
    }

    public static int memcmp(byte[] srcA, byte[] srcB, int len) {
        for (int i = 0; i < len; i++) {
            int n = srcA[i] - srcB[i];
            if (n != 0) {
                return n;
            }
        }

        return 0;
    }

    public static int memcmp(byte[] srcA, int indA, byte[] srcB, int indB, int len) {
        for (int i = 0; i < len; i++) {
            int n = srcA[i + indA] - srcB[i + indB];
            if (n != 0) {
                return n;
            }
        }

        return 0;
    }
}
