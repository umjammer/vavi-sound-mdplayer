package mdplayer.driver.mgsdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import konamiman.z80.Z80Processor;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import konamiman.z80.utils.Bit;

import static java.lang.System.getLogger;


public class Mapper {

    private static final Logger logger = getLogger(Mapper.class.getName());

    private byte freeSegment;
    public static final int tableAddress = 0xf200; // Copycat Nextor
    public static final int jumpAddress = 0xecb2; // Copycat Nextor
    private final MapperRamCartridge crt;

    public Mapper(MapperRamCartridge crt, MsxMemory memory) {
        this.crt = crt;
        crt.clearUseFlag();
        //freeSegment = (byte)crt.segmentSize;

        for (int i = 0; i < 4; i++) crt.setSegmentToPage(i, i);
        freeSegment = 4;

        for (int i = 0; i < 16; i++) {
            memory.set(tableAddress + i * 3 + 0, (byte) 0xc3); // JP
            memory.set(tableAddress + i * 3 + 1, (byte) (jumpAddress + i)); // Set in sequential order
            memory.set(tableAddress + i * 3 + 2, (byte) ((jumpAddress + i) >> 8)); //
        }
    }

    public void callMapperProc(BeforeInstructionFetchEvent args, Z80Processor z80, int typ) {
        switch (typ) {
        case 0: // adr
//logger.log(Level.TRACE, " MAPPER PROC ALL_SEG Reg.a=%02x Reg.B=%02x".formatted(z80.getRegisters().getA(), z80.getRegisters().getB()));
            if (z80.getRegisters().getB() != 0) { z80.getRegisters().setCF(Bit.ON); z80.executeRet(); return; }
            if (freeSegment == 0) {
                z80.getRegisters().setCF(Bit.ON);
                z80.executeRet();
                return;
            }
            z80.getRegisters().setA(freeSegment++); // Segment Number 1c 1b
//logger.log(Level.TRACE, "   Allocate Reg.a=%02x ".formatted(z80.getRegisters().getA()));
            z80.getRegisters().setB((byte) 0x00); // Slot number
            z80.getRegisters().setCF(Bit.OFF); // 1 on allocation failure
            break;
        case 1:
            z80.getRegisters().setCF(Bit.OFF); // 1 on allocation failure
            break;
        case 10: // adr:0x1e
//logger.log(Level.TRACE, " MAPPER PROC PUT_P1 Reg.a=%02x".formatted(z80.getRegisters().getA()));
            crt.setSegmentToPage(z80.getRegisters().getA() & 0xff, 1);
            break;
        case 11: // adr:0x21
//logger.log(Level.TRACE, " MAPPER PROC GET_P1 P1:%02x".formatted(crt.GetSegmentNumberFromPageNumber(1)));
            z80.getRegisters().setA((byte) crt.getSegmentNumberFromPageNumber(1));
            break;
        case 12: // adr:0x24
logger.log(Level.DEBUG, " MAPPER PROC PUT_P2 Reg.a=%02x".formatted(z80.getRegisters().getA()));
            crt.setSegmentToPage(z80.getRegisters().getA() & 0xff, 2);
            break;
        case 13: // adr:0x27
logger.log(Level.DEBUG, " MAPPER PROC GET_P1 P2:%02x".formatted(crt.getSegmentNumberFromPageNumber(2)));
            z80.getRegisters().setA((byte) crt.getSegmentNumberFromPageNumber(2));
            break;
        default:
logger.log(Level.DEBUG, " MAPPER PROC Unknown type: " + typ);
            // Don't throw - just return cleanly for unhandled calls
            break;
        }

        z80.executeRet();
    }
}
