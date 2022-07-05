
package mdplayer.driver.sid;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/** @deprecated use {@link java.nio.ByteBuffer} */
@Deprecated
public class Ptr {
//    public ByteBuffer buf;
//
//    public int ptr;
//
//    public Ptr(int size) {
//        buf = ByteBuffer.allocate(size);
//        ptr = 0;
//    }
//
//    public Ptr(ByteBuffer buf, int ptr) {
//        this.ptr = ptr;
//    }
//
//    public Ptr(byte[] buf, int ptr) {
//        this.buf = ByteBuffer.wrap(buf);
//        this.ptr = ptr;
//    }
//
//    public void set(int i, T value) {
//        buf[ptr + i] = value;
//    }
//
//    public T get(int i) {
//        return buf[ptr + i];
//    }
//
//    public void AddPtr(int a) {
//        ptr += a;
//    }
//
//    public String toString(int len) {
//        String ret = "";
//        for (int i = 0; i < len; i++) {
//            if (ptr + i < buf.length)
//                ret += buf[ptr + i];
//        }
//
//        return ret;
//    }

    public static ByteBuffer strchr(ByteBuffer src, byte c) {
        int ptr = 0;
        for (int i = 0; i < src.array().length; i++) {
            if (src.get(i) == c) {
                ptr = i;
                ByteBuffer r = src.duplicate();
                r.position(ptr);
                return r.slice();
            }
        }
        return null;
    }

    public static ByteBuffer strrchr(ByteBuffer src, byte c) {
        int ptr = 0;
        for (int i = src.capacity() - 1; i >= src.position(); i--) {
            if (src.get(i) == c) {
                ptr = i;
                ByteBuffer r = src.duplicate();
                r.position(ptr);
                return r.slice();
            }
        }
        return null;
    }

    public static ByteBuffer strstr(ByteBuffer src, String s) {
        String str = new String(src.array(), src.arrayOffset(), src.capacity() - src.position(), StandardCharsets.US_ASCII);
        int ind = str.indexOf(s);
        if (ind == -1)
            return null;

        ByteBuffer r = src.duplicate();
        r.position(ind);
        return r.slice();
    }

//    public int getCount() {
//        return buf.length - ptr;
//    }
}
