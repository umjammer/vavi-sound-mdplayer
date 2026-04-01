package mdplayer.driver.zms.nise68;

import java.util.function.BiFunction;
import java.util.function.Function;


public class MemHook {

    public final int startAdr;
    public final int endAdr;
    public final Function<Integer, Integer> read;
    public final BiFunction<Integer, Byte, Boolean> write;

    public MemHook(int startAdr, int endAdr, Function<Integer, Integer> read, BiFunction<Integer, Byte, Boolean> write) {
        this.startAdr = startAdr;
        this.endAdr = endAdr;
        this.read = read;
        this.write = write;
    }

    public byte readB(int adr) {
        return (byte) (int) read.apply(adr);
    }

    public short readW(int adr) {
        return (short) (int) read.apply(adr);
    }

    public int readL(int adr) {
        return (int) read.apply(adr);
    }

    public boolean writeB(int adr, byte dat) {
        return write.apply(adr, dat);
    }
}
