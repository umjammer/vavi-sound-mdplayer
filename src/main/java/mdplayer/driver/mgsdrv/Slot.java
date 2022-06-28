package mdplayer.driver.mgsdrv;

public class Slot {

    public Cartridge[][] slots;

    public Cartridge[] pages = new Cartridge[4];
    public SlotPos[] pagesSlotPos = new SlotPos[] {new SlotPos(), new SlotPos(), new SlotPos(), new SlotPos()};
    public int[] currentExtSlotPos = new int[] {0, 0, 0, 0};

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
