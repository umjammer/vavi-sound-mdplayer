package mdplayer.driver.mgsdrv;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;


public class SCCCartridge implements Cartridge {

    private boolean readOnly = true;
    private byte[] mem = new byte[65536];
    private ChipRegister chipRegister;
    private EnmModel model;

    public SCCCartridge(ChipRegister chipRegister, EnmModel model) {
        this.chipRegister = chipRegister;
        this.model = model;
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
        //Debug.printf(String.format("SCC Write : adr:%04x Dat:%02x", address, data));
        if (address == 0x9000) {
            if (data == 0) readOnly = true;
            else if (data == 0x3f) readOnly = false;
        }

        if ((address & 0xff00) == 0x9800) {
            if (address >= 0x9800 && address < 0x9880) {
                int scc1Port = 0; // vgmBuf[vgmAdr + 1] & 0x7f;
                byte scc1Offset = (byte) address;
                byte rDat = data;
                byte scc1ChipId = 0;
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset, model);
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x01, rDat, model);
            } else if (address < 0x988a) {
                int scc1Port = 1;
                byte scc1Offset = (byte) (address - 0x9880);
                byte rDat = data;
                byte scc1ChipId = 0;
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset, model);
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x01, rDat, model);
            } else if (address < 0x988f) {
                int scc1Port = 2;
                byte scc1Offset = (byte) (address - 0x988a);
                byte rDat = data;
                byte scc1ChipId = 0;
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset, model);
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x01, rDat, model);
            } else if (address == 0x988f) {
                int scc1Port = 3;
                byte scc1Offset = (byte) (address - 0x988f);
                byte rDat = data;
                byte scc1ChipId = 0;
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x00, scc1Offset, model);
                chipRegister.writeK051649(scc1ChipId, (scc1Port << 1) | 0x01, rDat, model);
            }
        }

        if (readOnly) return;
        mem[address] = data;
    }

    private byte read(int address) {
        //System.err.println("SCC Read : adr:%04x Dat:%02x", address, 0);
        return mem[address];
    }
}
