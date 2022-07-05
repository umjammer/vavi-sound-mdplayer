package mdplayer.driver.mgsdrv;

import konamiman.z80.Z80Processor;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import konamiman.z80.utils.Bit;
import mdplayer.Log;
import vavi.util.Debug;


public class Mapper {
    private byte freeSegment;
    public int tableAddress = 0xf200; // Nextor をまねた
    public int jumpAddress = 0xecb2; // Nextor をまねた
    private MapperRAMCartridge crt;

    public Mapper(MapperRAMCartridge crt, MsxMemory memory) {
        this.crt = crt;
        crt.clearUseFlag();
        //freeSegment = (byte)crt.segmentSize;

        for (int i = 0; i < 4; i++) crt.setSegmentToPage(i, i);
        freeSegment = 4;

        for (int i = 0; i < 16; i++) {
            memory.set(tableAddress + i * 3 + 0, (byte) 0xc3); // JP
            memory.set(tableAddress + i * 3 + 1, (byte) (jumpAddress + i)); // 連番で設定
            memory.set(tableAddress + i * 3 + 2, (byte) ((jumpAddress + i) >> 8)); //
        }
    }

    public void CallMapperProc(BeforeInstructionFetchEvent args, Z80Processor z80, int typ) {
        switch (typ) {
        case 0: // adr
            //Debug.printf(" MAPPER PROC ALL_SEG Reg.a=%02x Reg.B=%02x", z80.getRegisters().getA(), z80.getRegisters().getB());
            if (z80.getRegisters().getB() != 0) throw new UnsupportedOperationException();
            if (freeSegment == 0) {
                z80.getRegisters().setCF(Bit.ON);
                return;
            }
            z80.getRegisters().setA(freeSegment++); // Segment Number 1c 1b
            //Debug.printf("   Allocate Reg.a=%02x ", z80.getRegisters().getA() );
            z80.getRegisters().setB((byte) 0x00); // Slot number
            z80.getRegisters().setCF(Bit.OFF); // 割り当て失敗時に1
            break;
        case 10: // adr:0x1e
            //Debug.printf(" MAPPER PROC PUT_P1 Reg.a=%02x", z80.getRegisters().getA());
            crt.setSegmentToPage(z80.getRegisters().getA(), 1);
            break;
        case 11: // adr:0x21
            //Debug.printf(" MAPPER PROC GET_P1 P1:%02x", crt.GetSegmentNumberFromPageNumber(1));
            z80.getRegisters().setA((byte) crt.getSegmentNumberFromPageNumber(1));
            break;
        case 12: // adr:0x24
            Debug.printf(String.format(" MAPPER PROC PUT_P2 Reg.a=%02x", z80.getRegisters().getA()));
            crt.setSegmentToPage(z80.getRegisters().getA(), 2);
            break;
        case 13: // adr:0x27
            Debug.printf(String.format(" MAPPER PROC GET_P1 P2:%02x", crt.getSegmentNumberFromPageNumber(2)));
            z80.getRegisters().setA((byte) crt.getSegmentNumberFromPageNumber(2));
            break;
        default:
            Debug.printf(" MAPPER PROC Unknown type");
            throw new UnsupportedOperationException();
        }

        z80.executeRet();
    }
}
