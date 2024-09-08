
package mdplayer.driver.mgsdrv;

import java.util.Arrays;


public class MapperRAMCartridge implements Cartridge, IMapper {

    private final byte[][] physicalMemory;
    private final boolean[] useFlag;
    private final byte[][] visibleMemory;
    private static int[] visibleMemorySegmentNumber;
    private int segmentSize;

    public int getSegmentSize() {
        return segmentSize;
    }

    public MapperRAMCartridge(int segmentSize) {
        this.segmentSize = segmentSize;
        physicalMemory = new byte[segmentSize][];
        useFlag = new boolean[segmentSize];
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

    @Override
    public int getSegmentNumberFromPageNumber(int pageNumber) {
        return visibleMemorySegmentNumber[pageNumber % 4];
    }

    @Override
    public void setSegmentToPage(int segmentNumber, int pageNumber) {
        visibleMemory[pageNumber % 4] = physicalMemory[segmentNumber % physicalMemory.length];
        visibleMemorySegmentNumber[pageNumber % 4] = segmentNumber % physicalMemory.length;
        use(segmentNumber % physicalMemory.length);
    }

    @Override
    public void clearUseFlag() {
        Arrays.fill(useFlag, false);
    }

    @Override
    public boolean use(int segmentNumber) {
        return useFlag[segmentNumber % physicalMemory.length];
    }
}
