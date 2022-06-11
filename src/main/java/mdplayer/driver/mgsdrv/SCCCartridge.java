package mdplayer.driver.mgsdrv;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;


public class SCCCartridge implements Cartridge {
    private Boolean readOnly = true;
    private byte[] mem = new byte[65536];
    private ChipRegister chipRegister;
    private EnmModel model;

    public SCCCartridge(ChipRegister chipRegister, EnmModel model) {
        this.chipRegister = chipRegister;
        this.model = model;
    }

    public byte get(int address) {
        return read(address);
    }

    public void set(int address, byte value) {
        write(address, value);
    }

    private void write(int address, byte data) {
        //Log.Write(String.format("SCC Write : Adr:{0:x04} Dat:{1:x02}", address, data));
        if (address == 0x9000) {
            if (data == 0) readOnly = true;
            else if (data == 0x3f) readOnly = false;
        }

        if ((address & 0xff00) == 0x9800) {
            if (address >= 0x9800 && address < 0x9880) {
                int scc1_port = 0;// vgmBuf[vgmAdr + 1] & 0x7f;
                byte scc1_offset = (byte) address;
                byte rDat = data;
                byte scc1_chipid = 0;
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x00), scc1_offset, model);
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x01), rDat, model);
            } else if (address < 0x988a) {
                int scc1_port = 1;
                byte scc1_offset = (byte) (address - 0x9880);
                byte rDat = data;
                byte scc1_chipid = 0;
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x00), scc1_offset, model);
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x01), rDat, model);
            } else if (address < 0x988f) {
                int scc1_port = 2;
                byte scc1_offset = (byte) (address - 0x988a);
                byte rDat = data;
                byte scc1_chipid = 0;
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x00), scc1_offset, model);
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x01), rDat, model);
            } else if (address == 0x988f) {
                int scc1_port = 3;
                byte scc1_offset = (byte) (address - 0x988f);
                byte rDat = data;
                byte scc1_chipid = 0;
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x00), scc1_offset, model);
                chipRegister.writeK051649(scc1_chipid, (int) ((scc1_port << 1) | 0x01), rDat, model);
            }

        }

        if (readOnly) return;
        mem[address] = data;
    }

    private byte read(int address) {
        //System.err.println("SCC Read : Adr:{0:x04} Dat:{1:x02}", address, 0);
        return mem[address];
    }
}
