package mdplayer.driver.fmp.nise98;

import java.util.function.Consumer;
import java.util.function.Function;


public class Memory98 {

    private final byte[] mem;

    public Memory98(int size) {
        mem = new byte[size];
    }

    public void pokeB(int ptr, byte dat) {
        mem[ptr % mem.length] = dat;
    }

    public void pokeW(int ptr, short dat) {
        mem[ptr % mem.length] = (byte) dat;
        mem[(ptr + 1) % mem.length] = (byte) ((dat & 0xff00) >>> 8);
    }

    public byte peekB(int ptr) {
        return mem[ptr % mem.length];
    }

    public short peekW(int ptr) {
        return (short) ((mem[ptr % mem.length] & 0xff) + ((mem[(ptr + 1) % mem.length] & 0xff) << 8));
    }

    public void setHookAddress(int startAdr, int endAdr, Function<Integer, Byte> write, Consumer<Integer> read) {
    }
}

