package mdplayer.driver.mgsdrv;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Log;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;


public class MGSDRV extends BaseDriver {

    @Override
    public Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        Gd3 ret = new Vgm.Gd3();
        if (buf != null && buf.length > 8) {
            vgmGd3 = 8;
            ret.trackName = Common.getNRDString(buf, vgmGd3);
            ret.trackNameJ = ret.trackName;
        }

        return ret;
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        this.chipRegister = chipRegister;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;

        try {
            Run(vgmBuf);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void oneFrameProc() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            Log.forcedWrite(ex);

        }

    }

    private void oneFrameMain() {
        try {
            counter++;
            vgmFrameCounter++;

            if (vgmFrameCounter % (Common.VGMProcSampleRate / 60) == 0) {
                interrupt();
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);

        }
    }

    private void interrupt() {
        //Log.Write("\n_INTER(001FH)");
        z80.Registers.PC = 0x601F;
        z80.Registers.SP = 0x000a;
        z80.Continue();//.ExecuteCall(0x601f);//.Continue();
        //DebugRegisters(z80);

        byte PLAYFG = z80.Registers.A;
        if (PLAYFG == 0) stopped = true;
        vgmCurLoop = z80.Registers.D;
    }

    private static byte[] program = null;
    private static byte DollarCode;
    private Z80Processor z80;
    private Mapper mapper;
    public static int baseclockAY8910 = 1789773;
    public static int baseclockYM2413 = 3579545;
    public static int baseclockK051649 = 1789773;

    private void Run(byte[] vgmBuf) {
        String fileName = "MGSDRV.COM";
        DollarCode = '$';

        z80 = new Z80Processor();
        z80.ClockSynchronizer = null;
        z80.AutoStopOnRetWithStackEmpty = true;
        z80.Memory = new MsxMemory(chipRegister, model);
        z80.PortsSpace = new MsxPort(((MsxMemory) z80.Memory).slot, chipRegister, model);
        z80.BeforeInstructionFetch += Z80OnBeforeInstructionFetch;

        mapper = new Mapper((MapperRAMCartridge) ((MsxMemory) z80.Memory).slot.slots[3][1], (MsxMemory) z80.Memory);

        //Stopwatch sw = new Stopwatch();
        //sw.Start();

        z80.Reset();


        //プログラムの読み込みとメモリへのセット
        if (program == null) program = File.readAllBytes(fileName);
        z80.Memory.setContents(0x100, program);
        z80.Registers.PC = 0x100;

        //コマンドライン引数のセット
        byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
        z80.Memory[0x80] = (byte) option.length;
        for (int p = 0; p < option.length; p++) z80.Memory[0x81 + p] = option[p];

        z80.Continue();

        //sw.Stop();
        //Log.Write("\nElapsed time: {0}\n" , sw.Elapsed);


        //MGSDRVの存在するセグメントに切り替える
        ((MsxMemory) z80.Memory).changePage(3, 1, 1);//slot3-1を Page1に
        ((MapperRAMCartridge) ((MsxMemory) z80.Memory).slot.slots[3][1]).setSegmentToPage(4, 1);//slot3-1のPage1にsegment0x4を設定
        //((MsxMemory)z80.Memory).ChangePage(3, 1, 2);
        //((MapperRAMCartridge)((MsxMemory)z80.Memory).slot.slots[3][1]).SetSegmentToPage(0x1a, 2);

        Log.write("\n_SYSCK(0010H)");
        z80.Registers.PC = 0x6010;
        z80.Continue();
        //DebugRegisters(z80);

        Log.write(String.format("MSX-MUSIC slot {0:x02}", z80.Registers.D));
        Log.write(String.format("SCC       slot {0:x02}", z80.Registers.A));
        Log.write(String.format("MGSDRV Version {0:x04}", z80.Registers.HL));

        Log.write("\n_INITM(0013H)");
        z80.Registers.PC = 0x6013;
        z80.Continue();
        //DebugRegisters(z80);

        byte[] mgsdata = vgmBuf;
        MapperRAMCartridge cart = ((MapperRAMCartridge) ((MsxMemory) z80.Memory).slot.slots[3][1]);
        for (int i = 0; i < mgsdata.length; i++) {
            if (i % 0x4000 == 0) cart.setSegmentToPage(5 + (i / 0x4000), 2);//segment 5以降をpage2へ
            z80.Memory[0x8000 + (i % 0x4000)] = mgsdata[i];
        }
        cart.setSegmentToPage(5, 2);

        Log.write("\n_DATCK(0028H)");
        z80.Registers.PC = 0x6028;
        z80.Registers.HL = unchecked((short) 0x8000);
        z80.Continue();
        //DebugRegisters(z80);

        Log.write("\n_PLYST(0016H)");
        z80.Registers.PC = 0x6016;
        z80.Registers.DE = unchecked((short) 0x8000);
        z80.Registers.HL = unchecked((short) 0xffff);
        z80.Registers.B = 0xff;
        z80.Continue();
        //DebugRegisters(z80);


    }

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    private String PlayingFileName;

    private void Z80OnBeforeInstructionFetch(Object sender, BeforeInstructionFetchEventArgs args) {
        //Absolutely minimum implementation of CP/M for ZEXALL and ZEXDOC to work

        IZ80Processor z80 = (IZ80Processor) sender;

        if (z80.Registers.PC == 0) {//0:JP WBOOT
            args.ExecutionStopper.Stop();
            return;
        } else if (z80.Registers.PC == 0x0005) {
            //Log.Write("Call BDOS(0x0005) Reg.C={0:x02}", z80.Registers.C);
            CallBIOS(args, z80);
            return;
        } else if (z80.Registers.PC == 0x000c) {
            //Log.Write("Call RDSLT(0x000c) Reg.A={0:x02} Reg.HL={1:x04}", z80.Registers.A, z80.Registers.HL);

            int slot = z80.Registers.A & ((z80.Registers.A & 0x80) != 0 ? 0xf : 0x3);
            z80.Registers.A = ((MsxMemory) z80.Memory).readSlotMemoryAdr(
                    (slot & 0x03),
                    (slot & 0x0c) >> 2,
                    (int) z80.Registers.HL
            );
            z80.ExecuteRet();
            return;
        } else if (z80.Registers.PC == 0x0014) {
            Log.write(String.format("Call WRSLT(0x0014) Reg.A={0:x02} Reg.HL={1:x04} Reg.E={2:x02}", z80.Registers.A, z80.Registers.HL, z80.Registers.E));
            throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x001c) {
            Log.write(String.format("Call CALSLT(0x001c) Reg.IY={0:x04} Reg.IX={1:x04}", z80.Registers.IY, z80.Registers.IX));
            throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x0024) {
            //Log.Write("\nCall ENASLT(0x0024) Reg.A={0:x02} Reg.HL={1:x04}", z80.Registers.A, z80.Registers.HL);
            int slot = z80.Registers.A & ((z80.Registers.A & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.Memory).changePage(
                    (slot & 0x03)
                    , ((slot & 0x0c) >> 2)
                    , ((z80.Registers.H & 0xc0) >> 6)
            );
            z80.ExecuteRet();
            return;
        } else if (z80.Registers.PC == 0x0030) {
            Log.write("Call CALLF(0x0030)");
            throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x0090) {
            Log.write("Call GICINI (0090H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x0093) {
            Log.write("Call WRTPSG (0093H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x0096) {
            Log.write("Call RDPSG (0096H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x0138 || z80.Registers.PC == 0x013B || z80.Registers.PC == 0x015C || z80.Registers.PC == 0x015f) {
            Log.write("Call InterSlot");
            //throw new UnsupportedOperationException();
        } else if (z80.Registers.PC == 0x4601) {
            Log.write(String.format("JP NEWSTT(0x4601) Reg.HL={0:x04}", z80.Registers.HL));
            String msg = GetASCIIZ(z80, (int) z80.Registers.HL);
            Log.write(String.format("(HL)={0}", msg));
            if (msg == ":_SYSTEM") {
                args.ExecutionStopper.Stop();
            }
            return;
        } else if (z80.Registers.PC >= mapper.JumpAddress && z80.Registers.PC < mapper.JumpAddress + 16) {
            //Log.Write("\nCall MAPPER PROC(0x{0:x04}～) PC-{0:x04}:{1:x04}", mapper.JumpAddress, z80.Registers.PC - mapper.JumpAddress);
            mapper.CallMapperProc(args, z80, z80.Registers.PC - mapper.JumpAddress);
            return;
        } else if (z80.Registers.PC == 0xffca) {
            //Log.Write("\nCall EXTBIO(0xffca) Reg.DE={0:x04}", z80.Registers.DE);
            CallEXTBIO(args, z80);
            return;
        }

        //DebugRegisters(z80);
        ;
        return;
    }

    private static void DebugRegisters(IZ80Processor z80) {
        Log.write(String.format("Reg PC:{0:x04} AF:{1:x04} BC:{2:x04} DE:{3:x04} HL:{4:x04} IX:{5:x04} IY:{6:x04}"
                , z80.Registers.PC
                , z80.Registers.AF, z80.Registers.BC, z80.Registers.DE, z80.Registers.HL
                , z80.Registers.IX, z80.Registers.IY));
    }

    private void CallEXTBIO(BeforeInstructionFetchEventArgs args, IZ80Processor z80) {
        byte funcType = z80.Registers.D;
        byte function = z80.Registers.E;

        switch (funcType) {
        case 0x04:
            //Log.Write(" EXTBIO MemoryMapper");
            EXTBIO_MemoryMapper(args, z80, function);
            break;
        case 0xf0:
            //MGSDRV向けファンクションコール
            z80.Registers.A = 0;//非常駐時
            break;
        default:
            Log.write(" EXTBIO Unknown type");
            break;
        }

        z80.ExecuteRet();
    }

    private void EXTBIO_MemoryMapper(BeforeInstructionFetchEventArgs args, IZ80Processor z80, byte function) {
        switch (function) {
        case 0x02:
            z80.Registers.A = 0;
            z80.Registers.BC = 0;
            z80.Registers.HL = (short) mapper.TableAddress;
            break;
        }
    }

    private static void CallBIOS(BeforeInstructionFetchEventArgs args, IZ80Processor z80) {
        byte function = z80.Registers.C;

        if (function == 9) {
            String messageAddress = z80.Registers.DE;
            List<Byte> bytesToPrint = new ArrayList<Byte>();
            byte byteToPrint;
            while ((byteToPrint = z80.Memory[messageAddress]) != DollarCode) {
                bytesToPrint.add(byteToPrint);
                messageAddress++;
            }

            String StringToPrint = new String(mdsound.Common.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
            System.err.printf(StringToPrint);
        } else if (function == 2) {
            String byteToPrint = z80.Registers.E;
            String charToPrint = new String(new byte[] {byteToPrint}, StandardCharsets.US_ASCII)[0];
            System.err.printf(charToPrint);
        } else if (function == 0x62) {
            //_TERM
            Log.write(String.format("_TERM ErrorCode:{0:x02}", z80.Registers.B));
            args.ExecutionStopper.Stop();
            return;

        } else if (function == 0x6b) {
            //_GENV
            //Log.Write("_GENV HL:{0:x04} DE:{1:x04} B:{2:x02}", z80.Registers.HL, z80.Registers.DE, z80.Registers.B);
            String msg = GetASCIIZ(z80, (int) z80.Registers.HL);
            //Log.Write("(HL)={0}", msg);

            if (msg == "PARAMETERS") {
                byte[] option = StandardCharsets.US_ASCII.GetBytes("/z");
                for (int i = 0; i < option.length; i++) z80.Memory[z80.Registers.DE + i] = option[i];
                z80.Memory[z80.Registers.DE + option.length] = 0;
            } else if (msg == "SHELL") {
                //byte[] option = StandardCharsets.US_ASCII.GetBytes("c:\\dummy");
                //for (int i = 0; i < option.length; i++) z80.Memory[z80.Registers.DE + i] = option[i];
                //z80.Memory[z80.Registers.DE + option.length] = 0;
                z80.Memory[z80.Registers.DE] = 0;
            } else {
                z80.Memory[z80.Registers.DE] = 0;
            }

            z80.Registers.A = 0x00;//Error number
            z80.Registers.DE = 0x00;//value

        } else if (function == 0x6c) {
            //_SENV
            //Log.Write("_SENV HL:{0:x04} DE:{1:x04}", z80.Registers.HL, z80.Registers.DE);
            String msg = GetASCIIZ(z80, (int) z80.Registers.HL);
            //Log.Write("(HL)={0}", msg);

            msg = GetASCIIZ(z80, (int) z80.Registers.DE);
            //Log.Write("(DE)={0}", msg);

            z80.Registers.A = 0x00;//Error number
        } else if (function == 0x6f) {
            //_DOSVER
            z80.Registers.BC = 0x0231;//ROM version
            z80.Registers.DE = 0x0210;//DISK version
            //Log.Write("_DOSVER ret BC(ROMVer):{0:x04} DE(DISKVer):{1:x04}", z80.Registers.BC, z80.Registers.DE);
        } else {
            Log.write(String.format("unknown 0x{0:x02}", function));
        }

        z80.ExecuteRet();
    }

    private static String GetASCIIZ(IZ80Processor z80, int reg) {
        int messageAddress = reg;
        List<Byte> bytesToPrint = new ArrayList<>();
        byte byteToPrint;
        while ((byteToPrint = z80.Memory[messageAddress]) != 0) {
            bytesToPrint.add(byteToPrint);
            messageAddress++;
        }
        return new String(mdsound.Common.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
    }

}
