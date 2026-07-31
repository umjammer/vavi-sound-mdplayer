package mdplayer.driver.sid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import mdplayer.lib.sid.Ptr;


class PtrTest {

    // --- strchr ---

    @Test
    void strchr_foundAtStart() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strchr(src, (byte) 'h');
        assertNotNull(result);
        assertEquals("hello", new String(sliceArray(result)));
    }

    @Test
    void strchr_foundInMiddle() {
        ByteBuffer src = ByteBuffer.wrap("hello world".getBytes());
        ByteBuffer result = Ptr.strchr(src, (byte) 'o');
        assertNotNull(result);
        assertEquals("o world", new String(sliceArray(result)));
    }

    @Test
    void strchr_foundAtAEnd() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strchr(src, (byte) 'o');
        assertNotNull(result);
        assertEquals("o", new String(sliceArray(result)));
    }

    @Test
    void strchr_notFound() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strchr(src, (byte) 'z');
        assertNull(result);
    }

    @Test
    void strchr_nullByte() {
        ByteBuffer src = ByteBuffer.wrap("hello\0world".getBytes());
        ByteBuffer result = Ptr.strchr(src, (byte) 0);
        assertNotNull(result);
        assertEquals(0, result.get(0));
    }

    // --- strrchr ---

    @Test
    void strrchr_foundLast() {
        ByteBuffer src = ByteBuffer.wrap("hello worldlol".getBytes());
        ByteBuffer result = Ptr.strrchr(src, (byte) 'l');
        assertNotNull(result);
        assertEquals("l", new String(sliceArray(result)));
    }

    @Test
    void strrchr_foundOnce() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strrchr(src, (byte) 'e');
        assertNotNull(result);
        assertEquals("ello", new String(sliceArray(result)));
    }

    @Test
    void strrchr_notFound() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strrchr(src, (byte) 'z');
        assertNull(result);
    }

    @Test
    void strrchr_allSameChar() {
        ByteBuffer src = ByteBuffer.wrap("aaa".getBytes());
        ByteBuffer result = Ptr.strrchr(src, (byte) 'a');
        assertNotNull(result);
        assertEquals("a", new String(sliceArray(result)));
    }

    @Test
    void strrchr_foundAtEnd() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strrchr(src, (byte) 'o');
        assertNotNull(result);
        assertEquals("o", new String(sliceArray(result)));
    }

    // --- strstr ---

    @Test
    void strstr_foundAtStart() {
        ByteBuffer src = ByteBuffer.wrap("hello world".getBytes());
        ByteBuffer result = Ptr.strstr(src, "hello");
        assertNotNull(result);
        assertEquals("hello world", new String(sliceArray(result)));
    }

    @Test
    void strstr_foundInMiddle() {
        ByteBuffer src = ByteBuffer.wrap("hello world".getBytes());
        ByteBuffer result = Ptr.strstr(src, "world");
        assertNotNull(result);
        assertEquals("world", new String(sliceArray(result)));
    }

    @Test
    void strstr_notFound() {
        ByteBuffer src = ByteBuffer.wrap("hello world".getBytes());
        ByteBuffer result = Ptr.strstr(src, "xyz");
        assertNull(result);
    }

    @Test
    void strstr_emptySearch() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes());
        ByteBuffer result = Ptr.strstr(src, "");
        assertNotNull(result);
        assertEquals("hello", new String(sliceArray(result)));
    }

    @Test
    void strstr_singleChar() {
        ByteBuffer src = ByteBuffer.wrap("abcabc".getBytes());
        ByteBuffer result = Ptr.strstr(src, "b");
        assertNotNull(result);
        assertEquals("bcabc", new String(sliceArray(result)));
    }

    @Test
    void strstr_foundAtEnd() {
        ByteBuffer src = ByteBuffer.wrap("hello world".getBytes());
        ByteBuffer result = Ptr.strstr(src, "world");
        assertNotNull(result);
        assertEquals("world", new String(sliceArray(result)));
    }

    @Test
    void strstr_longPattern() {
        ByteBuffer src = ByteBuffer.wrap("abcdef".getBytes());
        ByteBuffer result = Ptr.strstr(src, "cde");
        assertNotNull(result);
        assertEquals("cdef", new String(sliceArray(result)));
    }

    private static byte[] sliceArray(ByteBuffer buf) {
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        return arr;
    }
}
