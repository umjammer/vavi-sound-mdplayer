
package mdplayer.driver.mgsdrv;

import konamiman.z80.interfaces.Memory;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;


public class MsxMemory implements Memory {

    private mdplayer.ChipRegister chipRegister;
    private EnmModel model;
    public MSXSlot slot;

    public MsxMemory(ChipRegister chipRegister, EnmModel model) {
        this.chipRegister = chipRegister;
        this.model = model;
        this.slot = new MSXSlot(chipRegister, model);
    }

    @Override
    public byte get(int address) {
        int page = address / Cartridge.PAGE_SIZE;
        return slot.pages[page].get(address);
    }

    @Override
    public void set(int address, byte value) {
        int page = address / Cartridge.PAGE_SIZE;
        slot.pages[page].set(address, value);
    }

    public int size = 65536;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public byte[] getContents(int startAddress, int length) {
        if (startAddress >= this.size)
            throw new IndexOutOfBoundsException("startAddress cannot go beyond memory size");

        if (startAddress + length > this.size)
            throw new IndexOutOfBoundsException("startAddress + length cannot go beyond memory size");

        if (startAddress < 0)
            throw new IndexOutOfBoundsException("startAddress cannot be negative");

        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++)
            ret[i] = get(startAddress + i);
        return ret;
    }

    @Override
    public void setContents(int startAddress,
                            byte[] contents,
                            int startIndex /* = 0 */,
                            Integer length /* = null */) {
        if (contents == null)
            throw new NullPointerException("contents");

        if (length == null)
            length = contents.length;

        if ((startIndex + length) > contents.length)
            throw new IndexOutOfBoundsException("startIndex + length cannot be greater than contents.length");

        if (startIndex < 0)
            throw new IndexOutOfBoundsException("startIndex cannot be negative");

        if (startAddress + length > size)
            throw new IndexOutOfBoundsException("startAddress + length cannot go beyond the memory size");

        for (int i = 0; i < length; i++)
            set(startAddress + i, contents[startIndex + i]);
    }

    public void changePage(int basicSlot, int extendSlot, int toMemoryPage) {
        slot.setPageFromSlot(toMemoryPage, basicSlot, extendSlot);
    }

    public byte readSlotMemoryAdr(int slot, int exSlot, int address) {
        return this.slot.slots[slot][exSlot].get(address);
    }
}
