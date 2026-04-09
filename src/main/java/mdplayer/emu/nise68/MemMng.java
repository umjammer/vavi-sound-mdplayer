package mdplayer.emu.nise68;

import java.util.HashMap;
import java.util.Map;


public class MemMng {

    public final Map<Integer, Integer> dicMng = new HashMap<>();
    private int startPtr = 0;
    int address = 0x2000;

    public int getAddress() {
        return address;
    }

    public int getAllocCount() {
        return dicMng.size();
    }

    public MemMng(int startAdr) {
        startPtr = startAdr;
        dicMng.clear();
        address = 0x2000;
    }

    final int bl = 4;

    public boolean set(int memPtr, int size) {
        if (dicMng.containsKey(memPtr)) return false;

        dicMng.put(memPtr, size);
        int m = memPtr + size;
        startPtr = Math.max(startPtr, m);
        if (startPtr % bl != 0) startPtr += bl - (startPtr % bl);
        return true;
    }

    public boolean Change(int newPtr, int newlen) {
        if (!dicMng.containsKey(newPtr)) return false;
        dicMng.put(newPtr, newlen);
        int m = newPtr + newlen;
        startPtr = Math.max(startPtr, m);
        if (startPtr % bl != 0) startPtr += bl - (startPtr % bl);
        return true;
    }

    public int malloc(int byteSize) {
        int ret = startPtr;
        dicMng.put(startPtr, byteSize);
        startPtr += byteSize;
        if (startPtr % bl != 0) startPtr += bl - (startPtr % bl);
        return ret;
    }

    public int mfree(int ptr) {
        if (!dicMng.containsKey(ptr))
            return -1;
        //dicMng.remove(ptr);
        return 0;
    }
}
