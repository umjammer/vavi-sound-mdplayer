package mdplayer.driver.sid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import mdplayer.lib.sid.Mem;


class MemTest {

    // --- memset(byte[] des, byte val, int length) ---

    @Test
    void memset_byteArray_zeroLength() {
        byte[] src = {1, 2, 3, 4, 5};
        byte[] des = Arrays.copyOf(src, src.length);
        Mem.memset(des, (byte) 0, 0);
        assertArrayEquals(src, des);
    }

    @Test
    void memset_byteArray_all() {
        byte[] des = new byte[5];
        Mem.memset(des, (byte) 0xAA, 5);
        for (byte b : des) {
            assertEquals(-86, b);
        }
    }

    @Test
    void memset_byteArray_partial() {
        byte[] des = {1, 2, 3, 4, 5};
        Mem.memset(des, (byte) 0, 3);
        assertEquals(0, des[0]);
        assertEquals(0, des[1]);
        assertEquals(0, des[2]);
        assertEquals(4, des[3]);
        assertEquals(5, des[4]);
    }

    // --- memset(ByteBuffer des, byte val, int length) ---

    @Test
    void memset_buffer_zeroLength() {
        ByteBuffer des = ByteBuffer.allocate(5);
        des.put((byte) 1).put((byte) 2).put((byte) 3).put((byte) 4).put((byte) 5);
        des.position(0);
        Mem.memset(des, (byte) 0, 0);
        des.position(0);
        assertEquals(1, des.get(0));
    }

    @Test
    void memset_buffer_all() {
        ByteBuffer des = ByteBuffer.allocate(5);
        Mem.memset(des, (byte) 0x55, 5);
        des.position(0);
        for (int i = 0; i < 5; i++) {
            assertEquals(0x55, des.get(i) & 0xFF);
        }
    }

    @Test
    void memset_buffer_partial() {
        ByteBuffer des = ByteBuffer.allocate(10);
        for (int i = 0; i < 10; i++) {
            des.put((byte) i);
        }
        des.position(0);
        Mem.memset(des, (byte) 0, 5);
        des.position(0);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, des.get(i));
        }
        for (int i = 5; i < 10; i++) {
            assertEquals(i, des.get(i));
        }
    }

    // --- memcpy(byte[] des, byte[] src, int length) ---

    @Test
    void memcpy_byteArray_array_zeroLength() {
        byte[] des = {1, 2, 3};
        byte[] src = {4, 5, 6};
        Mem.memcpy(des, src, 0);
        assertArrayEquals(new byte[]{1, 2, 3}, des);
    }

    @Test
    void memcpy_byteArray_array_all() {
        byte[] src = {1, 2, 3, 4, 5};
        byte[] des = new byte[5];
        Mem.memcpy(des, src, 5);
        assertArrayEquals(src, des);
    }

    @Test
    void memcpy_byteArray_array_partial() {
        byte[] src = {10, 20, 30, 40, 50};
        byte[] des = {0, 0, 0, 0, 0};
        Mem.memcpy(des, src, 3);
        assertEquals(10, des[0]);
        assertEquals(20, des[1]);
        assertEquals(30, des[2]);
        assertEquals(0, des[3]);
        assertEquals(0, des[4]);
    }

    // --- memcpy(byte[] des, ByteBuffer src, int length) ---

    @Test
    void memcpy_byteArray_buffer_all() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{100, -56, 77, 88, 99});
        byte[] des = new byte[5];
        Mem.memcpy(des, src, 5);
        assertEquals(100, des[0]);
        assertEquals(-56, des[1]);
        assertEquals(77, des[2]);
        assertEquals(88, des[3]);
        assertEquals(99, des[4]);
    }

    @Test
    void memcpy_byteArray_buffer_partial() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        byte[] des = new byte[5];
        Mem.memcpy(des, src, 3);
        assertEquals(1, des[0]);
        assertEquals(2, des[1]);
        assertEquals(3, des[2]);
        assertEquals(0, des[3]);
        assertEquals(0, des[4]);
    }

    // --- memcpy(ByteBuffer des, byte[] src, int length) ---

    @Test
    void memcpy_buffer_byteArray_all() {
        byte[] src = {12, 34, 56, 78, 90};
        ByteBuffer des = ByteBuffer.allocate(5);
        Mem.memcpy(des, src, 5);
        des.position(0);
        for (int i = 0; i < 5; i++) {
            assertEquals(src[i], des.get(i));
        }
    }

    @Test
    void memcpy_buffer_byteArray_partial() {
        byte[] src = {1, 2, 3, 4, 5};
        ByteBuffer des = ByteBuffer.allocate(5);
        Mem.memcpy(des, src, 3);
        des.position(0);
        assertEquals(1, des.get(0));
        assertEquals(2, des.get(1));
        assertEquals(3, des.get(2));
        assertEquals(0, des.get(3));
        assertEquals(0, des.get(4));
    }

    // --- memcpy(ByteBuffer des, ByteBuffer src, int length) ---

    @Test
    void memcpy_buffer_buffer_all() {
        byte[] data = {10, 20, 30, 40, 50};
        ByteBuffer src = ByteBuffer.wrap(data);
        ByteBuffer des = ByteBuffer.allocate(5);
        Mem.memcpy(des, src, 5);
        des.position(0);
        for (int i = 0; i < 5; i++) {
            assertEquals(data[i], des.get(i));
        }
    }

    @Test
    void memcpy_buffer_buffer_offsets() {
        ByteBuffer src = ByteBuffer.wrap(new byte[]{0, 0, 100, -56, -56, 0});
        src.position(2);
        ByteBuffer des = ByteBuffer.allocate(6);
        des.position(1);
        Mem.memcpy(des, src, 3);
        des.position(0);
        assertEquals(0, des.get(0));
        assertEquals(100, des.get(1));
        assertEquals(-56, des.get(2));
        assertEquals(-56, des.get(3));
        assertEquals(0, des.get(4));
        assertEquals(0, des.get(5));
    }

    // --- memcmp(byte[] srcA, byte[] srcB, int len) ---

    @Test
    void memcmp_byteArray_equal() {
        byte[] a = {1, 2, 3, 4, 5};
        byte[] b = {1, 2, 3, 4, 5};
        assertEquals(0, Mem.memcmp(a, b, 5));
    }

    @Test
    void memcmp_byteArray_less() {
        byte[] a = {1, 2, 3};
        byte[] b = {1, 2, 4};
        assertTrue(Mem.memcmp(a, b, 3) < 0);
    }

    @Test
    void memcmp_byteArray_greater() {
        byte[] a = {1, 3, 3};
        byte[] b = {1, 2, 3};
        assertTrue(Mem.memcmp(a, b, 3) > 0);
    }

    @Test
    void memcmp_byteArray_partial() {
        byte[] a = {10, 20, 30, 40, 50};
        byte[] b = {10, 99, 30, 40, 50};
        assertEquals(20 - 99, Mem.memcmp(a, b, 2));
    }

    @Test
    void memcmp_byteArray_zeroLen() {
        byte[] a = {1, 2, 3};
        byte[] b = {4, 5, 6};
        assertEquals(0, Mem.memcmp(a, b, 0));
    }

    // --- memcmp(byte[] srcA, int indA, byte[] srcB, int indB, int len) ---

    @Test
    void memcmp_byteArray_offsets_equal() {
        byte[] a = {0, 0, 10, 20, 30, 0, 0};
        byte[] b = {0, 0, 10, 20, 30, 0, 0};
        assertEquals(0, Mem.memcmp(a, 2, b, 2, 3));
    }

    @Test
    void memcmp_byteArray_offsets_less() {
        byte[] a = {0, 0, 5, 20, 30, 0, 0};
        byte[] b = {0, 0, 10, 20, 30, 0, 0};
        assertTrue(Mem.memcmp(a, 2, b, 2, 3) < 0);
    }

    @Test
    void memcmp_byteArray_offsets_greater() {
        byte[] a = {0, 0, 15, 20, 30, 0, 0};
        byte[] b = {0, 0, 10, 20, 30, 0, 0};
        assertTrue(Mem.memcmp(a, 2, b, 2, 3) > 0);
    }

    @Test
    void memcmp_byteArray_offsets_both() {
        byte[] a = {0, 0, 10, 20, 30};
        byte[] b = {99, 99, 10, 20, 30};
        assertEquals(0, Mem.memcmp(a, 2, b, 2, 3));
    }
}
