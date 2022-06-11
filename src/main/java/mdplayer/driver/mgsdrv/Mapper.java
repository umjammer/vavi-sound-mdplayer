package mdplayer.driver.mgsdrv;

import mdplayer.Log;


public class Mapper {
    private byte freeSegment;
    public int TableAddress = 0xf200;//Nextor をまねた
    public int JumpAddress = 0xecb2;//Nextor をまねた
    private MapperRAMCartridge crt;

    public Mapper(MapperRAMCartridge crt, MsxMemory memory) {
        this.crt = crt;
        crt.clearUseFlag();
        //freeSegment = (byte)crt.segmentSize;

        for (int i = 0; i < 4; i++) crt.setSegmentToPage(i, i);
        freeSegment = 4;

        for (int i = 0; i < 16; i++) {
            memory.set(TableAddress + i * 3 + 0, (byte) 0xc3);//JP
            memory.set(TableAddress + i * 3 + 1, (byte) (JumpAddress + i));//連番で設定
            memory.set(TableAddress + i * 3 + 2, (byte) ((JumpAddress + i) >> 8));//
        }
    }

    public void CallMapperProc(BeforeInstructionFetchEventArgs args, IZ80Processor z80, int typ) {
        switch (typ) {
        case 0://adr
            //Log.Write(" MAPPER PROC ALL_SEG Reg.A={0:x02} Reg.B={1:x02}", z80.Registers.A, z80.Registers.B);
            if (z80.Registers.B != 0) throw new UnsupportedOperationException();
            if (freeSegment == 0) {
                z80.Registers.CF = 1;
                return;
            }
            z80.Registers.A = freeSegment++;//Segment Number 1c 1b
            //Log.Write("   Allocate Reg.A={0:x02} ", z80.Registers.A );
            z80.Registers.B = 0x00;//Slot number
            z80.Registers.CF = 0;//割り当て失敗時に1
            break;
        case 10://adr:0x1e
            //Log.Write(" MAPPER PROC PUT_P1 Reg.A={0:x02}", z80.Registers.A);
            crt.setSegmentToPage(z80.Registers.A, 1);
            break;
        case 11://adr:0x21
            //Log.Write(" MAPPER PROC GET_P1 P1:{0:x02}", crt.GetSegmentNumberFromPageNumber(1));
            z80.Registers.A = (byte) crt.getSegmentNumberFromPageNumber(1);
            break;
        case 12://adr:0x24
            Log.write(String.format(" MAPPER PROC PUT_P2 Reg.A={0:x02}", z80.Registers.A));
            crt.setSegmentToPage(z80.Registers.A, 2);
            break;
        case 13://adr:0x27
            Log.write(String.format(" MAPPER PROC GET_P1 P2:{0:x02}", crt.getSegmentNumberFromPageNumber(2)));
            z80.Registers.A = (byte) crt.getSegmentNumberFromPageNumber(2);
            break;
        default:
            Log.write(" MAPPER PROC Unknown type");
            throw new UnsupportedOperationException();
        }

        z80.ExecuteRet();
    }
}
