package mdplayer.driver.fmp.nise98;

import java.util.function.Consumer;
import java.util.function.Function;


public class Memory98 {

    private byte[] mem;

    public Memory98(int size) {
        mem = new byte[size];
    }

    public void PokeB(int ptr, byte dat) {
        mem[ptr % mem.length] = dat;
    }

    public void pokeW(int ptr, short dat) {
        mem[(int) ptr % mem.length] = (byte) dat;
        mem[((int) ptr + 1) % mem.length] = (byte) (dat >> 8);
    }

    public byte PeekB(int ptr) {
        return mem[((int) ptr % mem.length)];
    }

    public short peekW(int ptr) {
        return (short) (mem[(int) ptr % mem.length]+ (mem[((int) ptr + 1) % mem.length] << 8));
    }

    public void SetHookAddress(int startAdr, int endAdr, Function<Integer, Byte> write, Consumer<Integer> read) {
    }
}

