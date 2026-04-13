package mdplayer.emu.msx;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;

import konamiman.z80.interfaces.Memory;

import static java.lang.System.getLogger;


public class MsxPort implements Memory {

    private static final Logger logger = getLogger(MsxPort.class.getName());

    private final MsxSlot slot;
    private final BiConsumer<Integer, Integer> ay8910Write;
    private final BiConsumer<Integer, Integer> ym2413Write;
    private final MsxVdp vdp;
    private byte opllAdr;
    private byte ay8910Adr;

    public MsxPort(MsxSlot slot, MsxVdp vdp, BiConsumer<Integer, Integer> ay8910Write, BiConsumer<Integer, Integer> ym2413Write) {
        this.slot = slot;
        this.vdp = vdp;
        this.ay8910Write = ay8910Write;
        this.ym2413Write = ym2413Write;
    }

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

        switch (address) {
        case 0x00:
        case 0x01:
        case 0x02:
        case 0x03:
            if (vdp != null) vdp.write(address, value);
            break;
        case 0xa0:
            ay8910Adr = value;
            break;
        case 0xa1:
            ay8910Write.accept(ay8910Adr & 0xff, value & 0xff);
            break;
        case 0xa2:
            //logger.log(Level.TRACE, "Psg Port adr:%04x Dat:%02x".formatted(address, value));
            break;
        case 0x7c:
            opllAdr = value;
            break;
        case 0x7d:
            ym2413Write.accept(opllAdr & 0xff, value & 0xff);
            //logger.log(Level.TRACE, "Ym2413 Port adr:%04x Dat:%02x".formatted(address, value));
            break;
        case 0xa8:
            //logger.log(Level.TRACE, "ChangeSlot Port adr:%04x Dat:%02x".formatted(address, value));
            changeSlot(value);
            break;
        default:
            logger.log(Level.DEBUG, "Port  adr:%04x Dat:%02x".formatted(address, value & 0xff));
            break;
        }
    }

    private byte inPort(int address) {

        switch (address) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
                if (vdp == null) return 0;
                return vdp.read(address);
            case 0xa8:
                //logger.log(Level.TRACE, "ChangeSlot Port :  adr:%04x".formatted(address));
                return readSlot();
        }

        logger.log(Level.DEBUG, "Port :  adr:%04x".formatted(address));
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
                ((slot.pagesSlotPos[0].basic & 3) << 0) |
                ((slot.pagesSlotPos[1].basic & 3) << 2) |
                ((slot.pagesSlotPos[2].basic & 3) << 4) |
                ((slot.pagesSlotPos[3].basic & 3) << 6)
        );
    }
}
