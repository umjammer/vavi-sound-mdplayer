package mdplayer.driver.ay;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import konamiman.z80.interfaces.Memory;
import konamiman.z80.interfaces.Z80Registers;
import mdplayer.Audio;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.ZxBeepChip;

import static java.lang.System.getLogger;


public class Port implements Memory {

    private static final Logger logger = getLogger(Port.class.getName());

    Z80Registers registers;
    Audio audio;
    EnmModel model;
    private byte ayReg = 0;
    private byte ayDat = 0;
    private final byte[] ayRegMap = new byte[255];

    private int bn = 0;
    private int bp = 0;

    @Override
    public byte get(int address) {
        return inPort(address);
    }

    @Override
    public void set(int address, byte value) {
        outPort(address, value);
    }

    @Override
    public int getSize() {
        return 256;
    }

    @Override
    public byte[] getContents(int startAddress, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContents(int startAddress, byte[] contents, int startIndex /* = 0 */, Integer length /* = null */) {
        throw new UnsupportedOperationException();
    }

    private void outPort(int address, byte value) {
        address = registers.getB() * 0x100 | (byte) address;

        if ((address & 0xc002) == 0xc000) {
            ayReg = value;
        } else if ((address & 0xc002) == 0x8000) {
            ayDat = value;
            audio.chipRegister.chip(Ay8910Chip.class).write(0, ayReg & 0xff, ayDat & 0xff, model);
            ayRegMap[ayReg & 0xff] = ayDat;
            //logger.log(Level.TRACE, "AY Reg:%02x Dat:%02x".formatted(ayReg, ayDat));
        } else if ((address & 0x0001) == 0) {
            if ((value & 16) != 0)
                bn = 10;
            else
                bn = 0;
            if (bn != bp) {
                audio.chipRegister.chip(ZxBeepChip.class).write(0, -1, -1, -1, model);
                bp = bn;
            }
        } else {
            logger.log(Level.DEBUG, "Out Port Adr:%04x Dat:%02x".formatted(address, value));
        }
    }

    private byte inPort(int address) {
        byte ret = (byte) 255;
        address = registers.getB() * 0x100 | (byte) address;
        if ((address & 0xc002) == (0xfffd & 0xc002)) {
            if (ayReg < 14) ret = ayRegMap[ayReg];
        }

//logger.log(Level.TRACE, "In Port Adr: %04x".formatted(address));
        return ret;
    }
}
