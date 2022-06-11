package mdplayer.driver.mgsdrv;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Log;


public class MsxPort implements IMemory {
    private MSXSlot slot;
    private ChipRegister chipRegister;
    private Common.EnmModel model;
    private byte opllAdr;
    private byte ay8910Adr;

    public MsxPort(MSXSlot slot, ChipRegister chipRegister, Common.EnmModel model) {
        this.slot = slot;
        this.chipRegister = chipRegister;
        this.model = model;
    }

    public byte get(int address) {
        return inPort(address);
    }

    public void set(int address, byte value) {
        outPort(address, value);
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    public byte[] getContents(int startAddress, int length) {
        throw new UnsupportedOperationException();
    }

    public void setContents(int startAddress, byte[] contents, int startIndex/* = 0*/, Integer length/* = null*/) {
        throw new UnsupportedOperationException();
    }

    private void outPort(int address, byte value) {

        switch (address) {
        case 0xa0:
            ay8910Adr = value;
            break;
        case 0xa1:
            chipRegister.setAY8910Register(0, ay8910Adr, value, model);
            break;
        case 0xa2:
            //Log.Write("PSG Port Adr:{0:x04} Dat:{1:x02}", address, value);
            break;
        case 0x7c:
            opllAdr = value;
            break;
        case 0x7d:
            chipRegister.setYM2413Register(0, opllAdr, value, model);
            //Log.write("OPLL Port Adr:%04x Dat:%02x", address, value);
            break;
        case 0xa8:
            //Log.write("ChangeSlot Port Adr:%04x Dat:%02x", address, value);
            changeSlot(value);
            break;
        default:
            Log.write(String.format("Port  Adr:%04x Dat:%02x", address, value));
            break;
        }

    }

    private byte inPort(int address) {

        if (address == 0xa8) {
            //Log.write("ChangeSlot Port :  Adr:{0:x04}", address);
            return readSlot();
        }

        Log.write(String.format("Port :  Adr:%04x", address));
        return 0;
    }

    private void changeSlot(byte value) {
        int bs;

        for (int p = 0; p < 4; p++) {
            bs = (value >> (p * 2)) & 0x3;
            slot.setPageFromSlot(p, bs);
        }
    }

    private byte readSlot() {
        return (byte) (
                ((slot.pagesSlotPos[0].basic & 3) << 0)
                        | ((slot.pagesSlotPos[1].basic & 3) << 2)
                        | ((slot.pagesSlotPos[2].basic & 3) << 4)
                        | ((slot.pagesSlotPos[3].basic & 3) << 6)
        );
    }
}
