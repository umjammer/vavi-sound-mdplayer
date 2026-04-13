package mdplayer.emu.msx;


interface IMapper {

    int getSegmentNumberFromPageNumber(int pageNumber);

    void setSegmentToPage(int segmentNumber, int pageNumber);

    boolean use(int segmentNumber);

    void clearUseFlag();
}
