
package mdplayer.driver.mgsdrv;

public class MapperRAMCartridge implements Cartridge, IMapper {

    private final byte[][] physicalMemory;
    private final Boolean[] useFlag;
    private final byte[][] visibleMemory;
    private static int[] visibleMemorySegmentNumber;
    private int segmentSize;

    public int getSegmentSize() {
        return segmentSize;
    }

    public MapperRAMCartridge(int segmentSize) {
        this.segmentSize = segmentSize;
        physicalMemory = new byte[segmentSize][];
        useFlag = new Boolean[segmentSize];
        visibleMemory = new byte[][] {
            null, null, null, null
        };
        visibleMemorySegmentNumber = new int[4];

        for (int i = 0; i < segmentSize; i++) {
            physicalMemory[i] = new byte[PAGE_SIZE];
            if (i < 4)
                setSegmentToPage(i, i);
        }
    }

    @Override
    public byte get(int address) {
        int page = address / PAGE_SIZE;
        if (page > visibleMemory.length || visibleMemory[page] == null)
            return (byte) 0xff;

        return visibleMemory[page][address % PAGE_SIZE];
    }

    @Override
    public void set(int address, byte value) {
        int page = address / PAGE_SIZE;
        if (page > visibleMemory.length || visibleMemory[page] == null)
            return;

        visibleMemory[page][address % PAGE_SIZE] = value;
    }

    public int getSegmentNumberFromPageNumber(int pageNumber) {
        return visibleMemorySegmentNumber[pageNumber % 4];
    }

    public void setSegmentToPage(int segmentNumber, int pageNumber) {
        visibleMemory[pageNumber % 4] = physicalMemory[segmentNumber % physicalMemory.length];
        visibleMemorySegmentNumber[pageNumber % 4] = segmentNumber % physicalMemory.length;
        use(segmentNumber % physicalMemory.length);
    }

    public void clearUseFlag() {
        for (int i = 0; i < useFlag.length; i++)
            useFlag[i] = false;
    }

    public boolean use(int segmentNumber) {
        return useFlag[segmentNumber % physicalMemory.length];
    }
}
