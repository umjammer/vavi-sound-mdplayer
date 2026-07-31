package mdplayer.lib.ay;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;

import konamiman.z80.interfaces.Memory;
import konamiman.z80.interfaces.Z80Registers;

import static java.lang.System.getLogger;


class Port implements Memory {

    private static final Logger logger = getLogger(Port.class.getName());

    Z80Registers registers;
    BiConsumer<Integer, Integer> ayWrite;
    Runnable zxWrite;
    public AY cpu;

    private byte ayReg = 0;
    private byte ayDat = 0;
    private final byte[] ayRegMap = new byte[255];
    private byte cpcSw = 0;

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
        // CPC

        if (registers.getB() == (byte) 0xf4 || registers.getB() == (byte) 0xf6) {
            cpu.setCpcClock();
            cpcOutPort(value);
            return;
        }

        // ZX

        cpu.setZxClock();
        address = registers.getB() * 0x100 | (byte) address;

        if ((address & 0xc002) == 0xc000) {
            ayReg = value;
        } else if ((address & 0xc002) == 0x8000) {
            ayDat = value;
            ayWrite.accept(ayReg & 0xff, ayDat & 0xff);
            ayRegMap[ayReg & 0xff] = ayDat;
            //logger.log(Level.TRACE, "AY Reg:%02x Dat:%02x".formatted(ayReg, ayDat));
        } else if ((address & 0x0001) == 0) {
            if ((value & 16) != 0)
                bn = 10;
            else
                bn = 0;
            if (bn != bp) {
                zxWrite.run();
                bp = bn;
            }
        } else {
            logger.log(Level.DEBUG, "Out Port Adr:%04x Dat:%02x".formatted(address, value));
        }
    }

    private void cpcOutPort(byte value) {
        if (registers.getB() == (byte) 0xf4) {
            ayDat = value;
            return;
        }

        if (registers.getB() != (byte) 0xf6) return;

        byte b = (byte) (value & 0xc0);

        if (cpcSw == 0) {
            cpcSw = b;
            return;
        }

        if (b != 0) return;

        if (cpcSw == (byte) 0xc0) {
            ayReg = (byte) (ayDat & 0xf);
            cpcSw = 0;
            return;
        }

        if (cpcSw == (byte) 0x80) {
            if (ayReg < 14) ayWrite.accept(ayReg & 0xff, ayDat & 0xff);
            cpcSw = 0;
        }
    }

    private byte inPort(int address) {
        byte ret = (byte) 255;
        address = (registers.getB() & 0xff) * 0x100 | (address & 0xff);
        if ((address & 0xc002) == (0xfffd & 0xc002)) {
            if (ayReg < 14) ret = ayRegMap[ayReg];
        }

//logger.log(Level.TRACE, "In Port Adr: %04x".formatted(address));
        return ret;
    }
}
