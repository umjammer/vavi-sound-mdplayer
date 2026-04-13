package mdplayer.driver.gbs;


import mdplayer.Common;


public class GbsInfo {

    public byte version;
    public byte nums;
    public byte firstSong;
    public int loadAddress;
    public int initAddress;
    public int playAddress;
    public int sp;
    public byte timerModulo;
    public byte timerControl;
    public String title;
    public String author;
    public String copyright;
    public byte[][] mem;

    /** @throws IllegalArgumentException data is not gbs */
    static GbsInfo factory(byte[] b) {
        // IdentifierCheck
        if (b[0] != 'G' || b[1] != 'B' || b[2] != 'S') throw new IllegalArgumentException("Unknown format");

        GbsInfo info = new GbsInfo();
        info.version = b[3];
        info.nums = b[4];
        info.firstSong = b[5];
        info.loadAddress = (b[6] & 0xFF) + ((b[7] & 0xFF) << 8);
        info.initAddress = (b[8] & 0xFF) + ((b[9] & 0xFF) << 8);
        info.playAddress = (b[10] & 0xFF) + ((b[11] & 0xFF) << 8);
        info.sp = (b[12] & 0xFF) + ((b[13] & 0xFF) << 8);
        info.timerModulo = b[14];
        info.timerControl = b[15];

        info.title = new String(b, 0x10, 32, Common.charset).replace("\0", "");
        info.author = new String(b, 0x30, 32, Common.charset).replace("\0", "");
        info.copyright = new String(b, 0x50, 32, Common.charset).replace("\0", "");

        int dataSize = b.length - 0x70;
        int maxBank = (info.loadAddress + dataSize + 0x3FFF) / 0x4000;
        info.mem = new byte[maxBank][];
        for (int i = 0; i < maxBank; i++) {
            info.mem[i] = new byte[0x4000];
        }

        int ptr = info.loadAddress % 0x4000;
        int cptr = 0x70;
        int bank = info.loadAddress / 0x4000;
        while (bank < maxBank && cptr < b.length) {
            info.mem[bank][ptr] = b[cptr];
            ptr++;
            if (ptr == 0x4000) {
                ptr = 0;
                bank++;
            }
            cptr++;
        }

        return info;
    }
}