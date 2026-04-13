package mdplayer.emu.msx;


public class Slot {

    public Cartridge[][] slots;

    public final Cartridge[] pages = new Cartridge[4];
    public final SlotPos[] pagesSlotPos = new SlotPos[] {new SlotPos(), new SlotPos(), new SlotPos(), new SlotPos()};
    public final int[] currentExtSlotPos = new int[] {0, 0, 0, 0};

    public void setPageFromSlot(int page, int basic) {
        pages[page] = slots[basic][currentExtSlotPos[basic]];
        pagesSlotPos[page].basic = basic;
        pagesSlotPos[page].extend = currentExtSlotPos[basic];
    }

    public void setPageFromSlot(int page, int basic, int extend) {
        pages[page] = slots[basic][extend];
        pagesSlotPos[page].basic = basic;
        pagesSlotPos[page].extend = extend;
        currentExtSlotPos[basic] = extend;
    }

    static class SlotPos {
        public int basic;
        public int extend;
    }
}
