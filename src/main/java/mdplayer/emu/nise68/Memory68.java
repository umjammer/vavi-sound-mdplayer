package mdplayer.emu.nise68;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public class Memory68 implements IMemory {

    public final byte[] mem;
    public final List<MemHook> hookList;

    /**
     * Bounds of {@link #hookList}, so the common case (plain ram, i.e. almost every access)
     * skips the scan instead of walking every hook only to find none of them match.
     * {@link #refreshHookBounds()} must be called after {@link #hookList} is touched directly.
     */
    private int minHookAdr = Integer.MAX_VALUE;
    private int maxHookAdr = Integer.MIN_VALUE;

    public Memory68(int size /* = 16 * 1024 * 1024 */) {
        mem = new byte[size];
        hookList = new ArrayList<>();
    }

    /** Recomputes the address window covered by {@link #hookList}. */
    public void refreshHookBounds() {
        minHookAdr = Integer.MAX_VALUE;
        maxHookAdr = Integer.MIN_VALUE;
        for (MemHook hook : hookList) {
            if (hook.startAdr < minHookAdr) minHookAdr = hook.startAdr;
            if (hook.endAdr > maxHookAdr) maxHookAdr = hook.endAdr;
        }
    }

    /** @return true when no hook can possibly cover {@code adr} */
    private boolean noHook(int adr) {
        return adr < minHookAdr || adr > maxHookAdr;
    }

    public void pokeB(int ptr, byte dat) {
//#if DEBUG
        //if (ptr >= 0x0003_3D0F && ptr<= 0x0003_3D0F) {
        //    ;
        //}
//#endif
        int adr = (int) ((ptr & 0xffff_ffffL) % mem.length);
        if (checkAndWriteHookAddressByte(adr, dat)) return;
        mem[adr] = dat;
    }

    public void pokeW(int ptr, short dat) { // BE
//#if DEBUG
        //if (ptr >= 0x0003_3D0F && ptr <= 0x0003_3D0F + 2) {
        //    ;
        //}
//#endif
        int adr = (int) ((ptr & 0xffff_ffffL) % mem.length);
        mem[adr] = (byte) (dat >>> 8);
        mem[(adr + 1) % mem.length] = (byte) dat;
    }

    public void pokeL(int ptr, int dat) { // BE
//#if DEBUG
        //if (ptr >= 0x0003_3D0F && ptr <= 0x0003_3D0F + 4) {
        //    ;
        //}
//#endif
        int adr = (int) ((ptr & 0xffff_ffffL) % mem.length);
        mem[adr] = (byte) (dat >>> 24);
        mem[(adr + 1) % mem.length] = (byte) (dat >>> 16);
        mem[(adr + 2) % mem.length] = (byte) (dat >>> 8);
        mem[(adr + 3) % mem.length] = (byte) dat;
    }

    public byte peekB(int ptr) {
        int adr = (int) ((ptr & 0xffff_ffffL) % mem.length);
        byte[] m = new byte[1];
        if (checkAndReadHookAddressByte(adr, /* out */ m)) {
            return m[0];
        }
        return mem[adr];
    }

    public short peekW(int ptr) { // BE
        int adr1 = (int) ((ptr & 0xffff_ffffL) % mem.length);
        int adr2 = (adr1 + 1) % mem.length;
        short[] m = new short[1];
        if (checkAndReadHookAddressWord(adr1, /* out */ m)) {
            return m[0];
        }
        return (short) (((mem[adr1] & 0xff) << 8) + (mem[adr2] & 0xff));
    }

    public int peekL(int ptr) { // BE
        int adr1 = (int) (((ptr + 0) & 0xffff_ffffL) % mem.length);
        int adr2 = (int) (((ptr + 1) & 0xffff_ffffL) % mem.length);
        int adr3 = (int) (((ptr + 2) & 0xffff_ffffL) % mem.length);
        int adr4 = (int) (((ptr + 3) & 0xffff_ffffL) % mem.length);
        int[] m = new int[1];
        if (checkAndReadHookAddressLong(adr1, /* out */ m)) {
            return m[0];
        }
        return ((mem[adr1] & 0xff) << 24) +
                ((mem[adr2] & 0xff) << 16) +
                ((mem[adr3] & 0xff) << 8) +
                (mem[adr4] & 0xff);
    }

    public void setHookAddress(int startAdr, int endAdr, BiFunction<Integer, Byte, Boolean> write, Function<Integer, Integer> read) {
        hookList.add(new MemHook(startAdr, endAdr, read, write));
        refreshHookBounds();
    }

    private boolean checkAndReadHookAddressByte(int adr, /* out */ byte[] retVal) {
        if (noHook(adr)) {
            retVal[0] = 0;
            return false;
        }
        for (var hook : hookList) {
            if (hook.startAdr <= adr && hook.endAdr >= adr) {
                if (hook.read == null) continue;
                retVal[0] = hook.readB(adr);
                return true;
            }
        }

        retVal[0] = 0;
        return false;
    }

    private boolean checkAndReadHookAddressWord(int adr, /* out */ short[] retVal) {
        if (noHook(adr)) {
            retVal[0] = 0;
            return false;
        }
        for (var hook : hookList) {
            if (hook.startAdr <= adr && hook.endAdr >= adr) {
                if (hook.read == null) continue;
                retVal[0] = hook.readW(adr);
                return true;
            }
        }

        retVal[0] = 0;
        return false;
    }

    private boolean checkAndReadHookAddressLong(int adr, /* out */ int[] retVal) {
        if (noHook(adr)) {
            retVal[0] = 0;
            return false;
        }
        for (var hook : hookList) {
            if (hook.startAdr <= adr && hook.endAdr >= adr) {
                if (hook.read == null) continue;
                retVal[0] = hook.readL(adr);
                return true;
            }
        }

        retVal[0] = 0;
        return false;
    }

    private boolean checkAndWriteHookAddressByte(int adr, byte val) {
        if (noHook(adr)) return false;
        for (var hook : hookList) {
            if (hook.startAdr <= adr && hook.endAdr >= adr) {
                if (hook.write == null) continue;
                return hook.write.apply(adr, val);
            }
        }

        return false;
    }

    @Override
    public byte readByte(int address) {
        return peekB(address);
    }

    @Override
    public int readInt(int address) {
        return peekL(address);
    }

    @Override
    public byte[] getMemory() {
        return mem;
    }
}
