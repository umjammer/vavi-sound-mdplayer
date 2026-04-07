package mdplayer.driver.mgsdrv;

import dotnet4j.util.compat.TriConsumer;


public class SCCCartridge implements Cartridge {

    private boolean readOnly = true;
    private final byte[] mem = new byte[65536];
    private final TriConsumer<Integer, Integer, Integer> chipWrite;

    public SCCCartridge(TriConsumer<Integer, Integer, Integer> chipWrite) {
        this.chipWrite = chipWrite;
    }

    @Override
    public byte get(int address) {
        return read(address);
    }

    @Override
    public void set(int address, byte value) {
        write(address, value);
    }

    private void write(int address, byte data) {
        //logger.log(Level.TRACE, "SCC Write : adr:%04x Dat:%02x".formatted(address, data)));
        if (address == 0x9000) {
            if (data == 0) readOnly = true;
            else if (data == 0x3f) readOnly = false;
        }

        if ((address & 0xff00) == 0x9800) {
            if (address >= 0x9800 && address < 0x9880) {
                int scc1Port = 0; // dataBuf[vgmAdr + 1] & 0x7f;
                byte scc1Offset = (byte) address;
                byte rDat = data;
                int scc1ChipId = 0;
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset & 0xff);
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x01, rDat & 0xff);
            } else if (address < 0x988a) {
                int scc1Port = 1;
                byte scc1Offset = (byte) (address - 0x9880);
                byte rDat = data;
                int scc1ChipId = 0;
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset & 0xff);
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x01, rDat & 0xff);
            } else if (address < 0x988f) {
                int scc1Port = 2;
                byte scc1Offset = (byte) (address - 0x988a);
                byte rDat = data;
                int scc1ChipId = 0;
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset & 0xff);
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x01, rDat & 0xff);
            } else if (address == 0x988f) {
                int scc1Port = 3;
                byte scc1Offset = (byte) (address - 0x988f);
                byte rDat = data;
                int scc1ChipId = 0;
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset & 0xff);
                chipWrite.accept(scc1ChipId, (scc1Port << 1) | 0x01, rDat & 0xff);
            }
        }

        if (readOnly) return;
        mem[address] = data;
    }

    private byte read(int address) {
        //logger.log(Level.TRACE, "SCC Read : adr:%04x Dat:%02x".formatted(address, 0));
        return mem[address];
    }
}
