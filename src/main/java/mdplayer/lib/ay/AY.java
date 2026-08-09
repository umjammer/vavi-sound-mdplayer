package mdplayer.lib.ay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.enums.MemoryAccessMode;
import konamiman.z80.impls.PlainMemory;
import vavi.util.ByteUtil;


public class AY {

    public static class Information {

        int fileVersion;
        int playerVersion;
        int pSpecialPlayer;
        public String pAuthor;
        public String pMisc;
        public int numOfSongs;
        int firstSong;
        public List<SongsStructure> songsStructures;
    }

    public static class SongsStructure {

        public String pSongName;
        public SongData songData;
    }

    public static class SongData {

        int aChan;
        int bChan;
        int cChan;
        int noise;
        public int songLength;
        public int fadeLength;
        int hiReg;
        int loReg;
        Points points;
        List<Block> addresses;
    }

    static class Points {

        int stack;
        int init;
        int inter;
    }

    static class Block {

        int address;
        int length;
        int offset;
    }

    private byte[] buf;
    public Information information;
    private Z80Processor z80;
    private int song = 0;
    /** 50Hz interrupts run since {@link #setup}: the unit the header states a song's length in */
    public long frames = 0;
    private static final int zxClock = 3_546_900; // 3.54690MHz
    private static final int cpcClock = 4_000_000; // 4.000000MHz
    private static final double PAL = 50.0;
    private double clkElp = 0.0;
    private double palElp = 0.0;
    private int clock = zxClock;
    private int sampleRate;

    public void run(byte[] buf) {
        this.buf = buf;
        getInformation(buf);
    }

    public void setCpcClock() {
        clock = cpcClock;
    }

    public void setZxClock() {
        clock = zxClock;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void getInformation(byte[] buf) {
        // FileID 'ZXAY'
        if (ByteUtil.readLeInt(buf, 0) != 0x5941_585a) {
            throw new UnsupportedOperationException();
        }
        // TypeID 'EMUL'
        if (ByteUtil.readLeInt(buf, 4) != 0x4c55_4d45) {
            throw new UnsupportedOperationException();
        }

        information = new Information();
        information.fileVersion = buf[8] & 0xff;
        information.playerVersion = buf[9] & 0xff;
        information.pSpecialPlayer = ByteUtil.readBeShort(buf, 10) & 0xffff;
        information.pAuthor = getString(buf, 12);
        information.pMisc = getString(buf, 14);
        information.numOfSongs = (buf[16] & 0xff) + 1;
        information.firstSong = buf[17] & 0xff;
        int ptr = (ByteUtil.readBeShort(buf, 18) & 0xffff) + 18;
        information.songsStructures = new ArrayList<>();
        for (int i = 0; i < information.numOfSongs; i++) {
            SongsStructure ss = new SongsStructure();
            ss.pSongName = getString(buf, ptr);
            ptr += 2;
            int sptr = (ByteUtil.readBeShort(buf, ptr) & 0xffff) + ptr;
            ptr += 2;

            ss.songData = new SongData();
            ss.songData.aChan = buf[sptr++] & 0xff;
            ss.songData.bChan = buf[sptr++] & 0xff;
            ss.songData.cChan = buf[sptr++] & 0xff;
            ss.songData.noise = buf[sptr++] & 0xff;
            ss.songData.songLength = ByteUtil.readBeShort(buf, sptr) & 0xffff;
            sptr += 2;
            ss.songData.fadeLength = ByteUtil.readBeShort(buf, sptr) & 0xffff;
            sptr += 2;
            ss.songData.hiReg = buf[sptr++] & 0xff;
            ss.songData.loReg = buf[sptr++] & 0xff;
            int pptr = (ByteUtil.readBeShort(buf, sptr) & 0xffff) + sptr;
            sptr += 2;
            int bptr = (ByteUtil.readBeShort(buf, sptr) & 0xffff) + sptr;
            sptr += 2;

            ss.songData.points = new Points();
            ss.songData.points.stack = ByteUtil.readBeShort(buf, pptr) & 0xffff;
            pptr += 2;
            ss.songData.points.init = ByteUtil.readBeShort(buf, pptr) & 0xffff;
            pptr += 2;
            ss.songData.points.inter = ByteUtil.readBeShort(buf, pptr) & 0xffff;
            pptr += 2;

            ss.songData.addresses = new ArrayList<>();
            while (true) {
                Block b = new Block();
                b.address = ByteUtil.readBeShort(buf, bptr) & 0xffff;
                bptr += 2;
                if (b.address == 0) break;
                b.length = ByteUtil.readBeShort(buf, bptr) & 0xffff;
                bptr += 2;
                b.offset = (ByteUtil.readBeShort(buf, bptr) & 0xffff) + bptr;
                bptr += 2;
                ss.songData.addresses.add(b);
            }

            information.songsStructures.add(ss);
        }
    }

    private static String getString(byte[] buf, int ptr) {
        int ofs = (ByteUtil.readBeShort(buf, ptr) & 0xffff) + ptr;
        List<Byte> dat = new ArrayList<>();
        while (buf[ofs] != 0) {
            dat.add(buf[ofs++]);
        }

        return new String(ByteUtil.toByteArray(dat));
    }

    /** The song a number selects, which is the first one when the number names no song of this file. */
    public int songIndex(int songNum) {
        return songNum >= 0 && songNum < information.songsStructures.size() ? songNum : 0;
    }

    /** @param songNum out of range selects the first song, as {@link #setup} does */
    public SongData songData(int songNum) {
        return information.songsStructures.get(songIndex(songNum)).songData;
    }

    public void setup(int songNum, BiConsumer<Integer, Integer> ayWrite, Runnable zxWrite) {
        Port port = new Port();
        z80 = new Z80ProcessorImpl();
        z80.setPortsSpace(port);
        z80.setPortsSpaceAccessMode((byte) 0, port.getSize(), MemoryAccessMode.ReadAndWrite);
        z80.setClockFrequencyInMHz(4);
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setAutoStopOnDiPlusHalt(false);
        z80.setMemory(new PlainMemory(0x1_0000));
        port.registers = z80.getRegisters();
        port.ayWrite = ayWrite;
        port.zxWrite = zxWrite;
        port.cpu = this;

        // a) Fill #0000-#00FF range with #C9 value
        for (int i = 0x0000; i < 0x0100; i++) z80.getMemory().set(i, (byte) 0xc9);
        // b) Fill #0100-#3FFF range with #FF value
        for (int i = 0x0100; i < 0x4000; i++) z80.getMemory().set(i, (byte) 0xff);
        // c) Fill #4000-#FFFF range with #00 value
        for (int i = 0x4000; i < 0x10000; i++) z80.getMemory().set(i, (byte) 0x00);
        // d) Place to #0038 address #FB value
        z80.getMemory().set(0x0038, (byte) 0xfb);

        // e) if INIT equal to ZERO then place to first CALL instruction address of first AY file block instead of INIT (see next f) and g) steps)
        // f) if INTERRUPT equal to ZERO then place at ZERO address next player:
        // g) if INTERRUPT not equal to ZERO then place at ZERO address next player:
        songNum = songIndex(songNum);
        song = songNum;
        frames = 0;
        clkElp = 0;
        palElp = 0;
        int init = information.songsStructures.get(songNum).songData.points.init;
        if (init == 0) {
            init = information.songsStructures.get(songNum).songData.addresses.getFirst().address;
        }
        int inter = information.songsStructures.get(songNum).songData.points.inter;
        if (inter == 0) {
            byte[] player = {(byte) 0xf3, (byte) 0xcd, 0x00, 0x00, (byte) 0xed, 0x5e, (byte) 0xfb, 0x76, 0x18, (byte) 0xfa};
            for (int i = 0; i < player.length; i++) z80.getMemory().set(i, player[i]);
        } else {
            byte[] player = {(byte) 0xf3, (byte) 0xcd, 0x00, 0x00, (byte) 0xed, 0x56, (byte) 0xfb, 0x76, (byte) 0xcd, 0x00, 0x00, 0x18, (byte) 0xf7};
            for (int i = 0; i < player.length; i++) z80.getMemory().set(i, player[i]);
            z80.getMemory().set(9, (byte) inter);
            z80.getMemory().set(10, (byte) (inter >> 8));
        }
        z80.getMemory().set(2, (byte) init);
        z80.getMemory().set(3, (byte) (init >> 8));

        // h) Load all blocks for this song
        for (int i = 0; i < information.songsStructures.get(songNum).songData.addresses.size(); i++) {
            Block block = information.songsStructures.get(songNum).songData.addresses.get(i);
            // a block whose length runs past the end of the file is loaded as far as the file
            // goes - one in ten of them does, and the memory is zero filled there anyway
            for (int j = 0; j < block.length && block.offset + j < buf.length; j++) {
                if (block.address + j >= 0x1_0000) continue;
                z80.getMemory().set(block.address + j, buf[block.offset + j]);
            }
        }

        // i) Load all common lower registers with loReg value (including AF register)
        byte lr = (byte) information.songsStructures.get(songNum).songData.loReg;
        z80.getRegisters().setF(lr);
        z80.getRegisters().getAlternate().setF(lr);
        z80.getRegisters().setL(lr);
        z80.getRegisters().getAlternate().setL(lr);
        z80.getRegisters().setE(lr);
        z80.getRegisters().getAlternate().setE(lr);
        z80.getRegisters().setC(lr);
        z80.getRegisters().getAlternate().setC(lr);
        z80.getRegisters().setIXL(lr);
        z80.getRegisters().setIYL(lr);

        // j) Load all common higher registers with hiReg value
        byte hr = (byte) information.songsStructures.get(songNum).songData.hiReg;
        z80.getRegisters().setA(hr);
        z80.getRegisters().getAlternate().setA(hr);
        z80.getRegisters().setH(hr);
        z80.getRegisters().getAlternate().setH(hr);
        z80.getRegisters().setD(hr);
        z80.getRegisters().getAlternate().setD(hr);
        z80.getRegisters().setB(hr);
        z80.getRegisters().getAlternate().setB(hr);
        z80.getRegisters().setIXH(hr);
        z80.getRegisters().setIYH(hr);

        // k) Load into I register 3 (this player version)
        z80.getRegisters().setIR((short) 0x0300);

        // l) load to SP stack value from points data of this song
        z80.getRegisters().setSP((short) information.songsStructures.get(songNum).songData.points.stack);

        // m) Load to PC ZERO value
        z80.getRegisters().setPC((short) 0);

        // n) Disable Z80 interrupts and set IM0 mode
        z80.setInterruptMode((byte) 0);

        // o) Emulate resetting of AY chip
        // p) Start Z80 emulation
    }

    public void oneFrame() {
        double old = z80.getTStatesElapsedSinceReset();
        boolean brk = false;

        while (!brk) {
            z80.executeNextInstruction();
            double step = z80.getTStatesElapsedSinceReset() - old;
            old = z80.getTStatesElapsedSinceReset();

            clkElp += step;
            if ((double) clock / sampleRate <= clkElp) {
                clkElp -= (double) clock / sampleRate;
                brk = true;
            }

            palElp += step;
            if (clock / PAL <= palElp) {
                palElp -= (clock / PAL);
                frames++;

                if (z80.isHalted()) {
                    short pc = z80.getRegisters().getPC();
                    short sp = z80.getRegisters().getSP();
                    short af = z80.getRegisters().getAF();
                    z80.reset();
                    z80.getRegisters().setPC(pc);
                    z80.getRegisters().setSP(sp);
                    z80.getRegisters().setAF(af);
                    brk = true; // The loop is exited only when HALT is reached. (The Z80 needs to rest until the next processing time.)
                }

                // If you set brk=true here, it will not cause any problems,
                // but you will not be able to reproduce the frame drops.
            }
        }

//        if (ps && z80.isHalted()) {
//            int pc = z80.getRegisters().getPC();
//            int sp = z80.getRegisters().getSP();
//            int af = z80.getRegisters().getAF();
//            z80.reset();
//            z80.getRegisters().setPC((short) pc);
//            z80.getRegisters().setSP((short) sp);
//            z80.getRegisters().setAF((short) af);
////            logger.log(Level.TRACE, "PC:%04x".formatted(z80.getRegisters().getPC()));
//        }
    }
}
