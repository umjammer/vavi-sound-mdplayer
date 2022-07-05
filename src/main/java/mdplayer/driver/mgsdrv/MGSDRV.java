package mdplayer.driver.mgsdrv;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
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
        z80.getRegisters().setPC((short) 0x601F);
        z80.getRegisters().setSP((short) 0x000a);
        z80.continue_();
        //DebugRegisters(z80);

        byte PLAYFG = z80.getRegisters().getA();
        if (PLAYFG == 0) stopped = true;
        vgmCurLoop = z80.getRegisters().getD();
    }

    private static byte[] program = null;
    private static final byte DollarCode = '$';
    private Z80Processor z80;
    private Mapper mapper;
    public static int baseclockAY8910 = 1789773;
    public static int baseclockYM2413 = 3579545;
    public static int baseclockK051649 = 1789773;

    private void Run(byte[] vgmBuf) {
        String fileName = "MGSDRV.COM";

        z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setMemory(new MsxMemory(chipRegister, model));
        z80.setPortsSpace(new MsxPort(((MsxMemory) z80.getMemory()).slot, chipRegister, model));
        z80.beforeInstructionFetch().addListener(this::Z80OnBeforeInstructionFetch);

        mapper = new Mapper((MapperRAMCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1], (MsxMemory) z80.getMemory());

        //Stopwatch sw = new Stopwatch();
        //sw.Start();

        z80.reset();

        //プログラムの読み込みとメモリへのセット
        if (program == null) program = File.readAllBytes(fileName);
        z80.getMemory().setContents(0x100, program, 0, null);
        z80.getRegisters().setPC((short) 0x100);

        //コマンドライン引数のセット
        byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
        z80.getMemory().set(0x80, (byte) option.length);
        for (int p = 0; p < option.length; p++) z80.getMemory().set(0x81 + p, option[p]);

        z80.continue_();

        //sw.Stop();
        //Log.Write("\nElapsed time: %d\n" , sw.Elapsed);

        // MGSDRVの存在するセグメントに切り替える
        ((MsxMemory) z80.getMemory()).changePage(3, 1, 1); // slot3-1を Page1に
        ((MapperRAMCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1]).setSegmentToPage(4, 1); // slot3-1のPage1にsegment0x4を設定

        Log.write("\n_SYSCK(0010H)");
        z80.getRegisters().setPC((short) 0x6010);
        z80.continue_();
        //DebugRegisters(z80);

        Log.write(String.format("MSX-MUSIC slot %02x", z80.getRegisters().getD()));
        Log.write(String.format("SCC       slot %02x", z80.getRegisters().getA()));
        Log.write(String.format("MGSDRV Version %04x", z80.getRegisters().getHL()));

        Log.write("\n_INITM(0013H)");
        z80.getRegisters().setPC((short) 0x6013);
        z80.continue_();
        //DebugRegisters(z80);

        byte[] mgsdata = vgmBuf;
        MapperRAMCartridge cart = ((MapperRAMCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1]);
        for (int i = 0; i < mgsdata.length; i++) {
            if (i % 0x4000 == 0) cart.setSegmentToPage(5 + (i / 0x4000), 2); // segment 5以降をpage2へ
            z80.getMemory().set(0x8000 + (i % 0x4000), mgsdata[i]);
        }
        cart.setSegmentToPage(5, 2);

        Log.write("\n_DATCK(0028H)");
        z80.getRegisters().setPC((short) 0x6028);
        z80.getRegisters().setHL((short) 0x8000);
        z80.continue_();
        //DebugRegisters(z80);

        Log.write("\n_PLYST(0016H)");
        z80.getRegisters().setPC((short) 0x6016);
        z80.getRegisters().setDE((short) 0x8000);
        z80.getRegisters().setHL((short) 0xffff);
        z80.getRegisters().setB((byte) 0xff);
        z80.continue_();
        //DebugRegisters(z80);
    }

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    private String PlayingFileName;

    private void Z80OnBeforeInstructionFetch(BeforeInstructionFetchEvent args) {

        Z80Processor z80 = (Z80Processor) args.getSource();

        if (z80.getRegisters().getPC() == 0) { // 0:JP WBOOT
            args.getExecutionStopper().stop(false);
        } else if (z80.getRegisters().getPC() == 0x0005) {
            //Log.Write("Call BDOS(0x0005) Reg.C=%02x", z80.getRegisters().getC());
            callBIOS(args, z80);
        } else if (z80.getRegisters().getPC() == 0x000c) {
            //Log.Write("Call RDSLT(0x000c) Reg.a=%02x Reg.HL=%04x", z80.getRegisters().getA(), z80.getRegisters().getHL());

            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            z80.getRegisters().setA(((MsxMemory) z80.getMemory()).readSlotMemoryAdr(
                    slot & 0x03,
                    (slot & 0x0c) >> 2,
                    z80.getRegisters().getHL() & 0xffff
            ));
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0014) {
            Log.write(String.format("Call WRSLT(0x0014) Reg.a=%02x Reg.HL=%04x Reg.E=%02x", z80.getRegisters().getA(), z80.getRegisters().getHL(), z80.getRegisters().getE()));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x001c) {
            Log.write(String.format("Call CALSLT(0x001c) Reg.IY=%04x Reg.IX=%04x", z80.getRegisters().getIY(), z80.getRegisters().getIX()));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0024) {
            //Log.Write("\nCall ENASLT(0x0024) Reg.a=%02x Reg.HL=%04x", z80.getRegisters().getA(), z80.getRegisters().getHL());
            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.getMemory()).changePage(
                    (slot & 0x03),
                    ((slot & 0x0c) >> 2),
                    ((z80.getRegisters().getH() & 0xc0) >> 6)
            );
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0030) {
            Log.write("Call CALLF(0x0030)");
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0090) {
            Log.write("Call GICINI (0090H/MAIN)");
        } else if (z80.getRegisters().getPC() == 0x0093) {
            Log.write("Call WRTPSG (0093H/MAIN)");
        } else if (z80.getRegisters().getPC() == 0x0096) {
            Log.write("Call RDPSG (0096H/MAIN)");
        } else if (z80.getRegisters().getPC() == 0x0138 || z80.getRegisters().getPC() == 0x013B || z80.getRegisters().getPC() == 0x015C || z80.getRegisters().getPC() == 0x015f) {
            Log.write("Call InterSlot");
        } else if (z80.getRegisters().getPC() == 0x4601) {
            Log.write(String.format("JP NEWSTT(0x4601) Reg.HL=%04x", z80.getRegisters().getHL()));
            String msg = getASCIIZ(z80, (int) z80.getRegisters().getHL());
            Log.write(String.format("(HL)=%s", msg));
            if (msg.equals(":_SYSTEM")) {
                args.getExecutionStopper().stop(false);
            }
        } else if (z80.getRegisters().getPC() >= mapper.jumpAddress && z80.getRegisters().getPC() < mapper.jumpAddress + 16) {
            //Log.Write("\nCall MAPPER PROC(0x%04x～) pc-%04x:%04x", mapper.JumpAddress, z80.getRegisters().getPC() - mapper.JumpAddress);
            mapper.CallMapperProc(args, z80, z80.getRegisters().getPC() - mapper.jumpAddress);
        } else if ((z80.getRegisters().getPC() & 0xffff) == 0xffca) {
            //Log.Write("\nCall EXTBIO(0xffca) Reg.DE=%04x", z80.getRegisters().getDE());
            callEXTBIO(args, z80);
        }

        //DebugRegisters(z80);
    }

    private static void debugRegisters(Z80Processor z80) {
        Log.write(String.format("Reg pc:%04x AF:%04x BC:%04x DE:%04x HL:%04x IX:%04x IY:%04x"
                , z80.getRegisters().getPC()
                , z80.getRegisters().getAF(), z80.getRegisters().getBC(), z80.getRegisters().getDE(), z80.getRegisters().getHL()
                , z80.getRegisters().getIX(), z80.getRegisters().getIY()));
    }

    private void callEXTBIO(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte funcType = z80.getRegisters().getD();
        byte function = z80.getRegisters().getE();

        switch (funcType & 0xff) {
        case 0x04:
            //Log.Write(" EXTBIO MemoryMapper");
            extbioMemorymapper(args, z80, function);
            break;
        case 0xf0:
            // MGSDRV向けファンクションコール
            z80.getRegisters().setA((byte) 0); // 非常駐時
            break;
        default:
            Log.write(" EXTBIO Unknown type");
            break;
        }

        z80.executeRet();
    }

    private void extbioMemorymapper(BeforeInstructionFetchEvent args, Z80Processor z80, byte function) {
        switch (function) {
        case 0x02:
            z80.getRegisters().setA((byte) 0);
            z80.getRegisters().setBC((short) 0);
            z80.getRegisters().setHL((short) mapper.tableAddress);
            break;
        }
    }

    private static void callBIOS(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte function = z80.getRegisters().getC();

        if (function == 9) {
            var messageAddress = z80.getRegisters().getDE();
            List<Byte> bytesToPrint = new ArrayList<>();
            byte byteToPrint;
            while ((byteToPrint = z80.getMemory().get(messageAddress)) != DollarCode) {
                bytesToPrint.add(byteToPrint);
                messageAddress++;
            }

            String StringToPrint = new String(mdsound.Common.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
            System.err.printf(StringToPrint);
        } else if (function == 2) {
            byte byteToPrint = z80.getRegisters().getE();
            char charToPrint = (char) (byteToPrint & 0xff);
            System.err.print(charToPrint);
        } else if (function == 0x62) {
            // _TERM
            Log.write(String.format("_TERM ErrorCode:%02x", z80.getRegisters().getB()));
            args.getExecutionStopper().stop(false);
            return;

        } else if (function == 0x6b) {
            // _GENV
            //Log.Write("_GENV HL:%04x DE:%04x B:%02x", z80.getRegisters().getHL(), z80.getRegisters().getDE(), z80.getRegisters().getB());
            String msg = getASCIIZ(z80, (int) z80.getRegisters().getHL());
            //Log.Write("(HL)=%d", msg);

            if (msg.equals("PARAMETERS")) {
                byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
                for (int i = 0; i < option.length; i++) z80.getMemory().set(z80.getRegisters().getDE() + i, option[i]);
                z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.length, (byte) 0);
            } else if (msg.equals("SHELL")) {
                //byte[] option = StandardCharsets.US_ASCII.GetBytes("c:\\dummy");
                //for (int i = 0; i < option.length; i++) z80.getMemory()[z80.getRegisters().getDE() + i] = option[i];
                //z80.getMemory()[z80.getRegisters().getDE() + option.length] = 0;
                z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
            } else {
                z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
            }

            z80.getRegisters().setA((byte) 0x00); // Error number
            z80.getRegisters().setDE((short) 0x00); // value

        } else if (function == 0x6c) {
            // _SENV
            //Log.Write("_SENV HL:%04x DE:%04x", z80.getRegisters().getHL(), z80.getRegisters().getDE());
            String msg = getASCIIZ(z80, z80.getRegisters().getHL() & 0xffff);
            //Log.Write("(HL)=%d", msg);

            msg = getASCIIZ(z80, (int) z80.getRegisters().getDE());
            //Log.Write("(DE)=%d", msg);

            z80.getRegisters().setA((byte) 0x00); // Error number
        } else if (function == 0x6f) {
            // _DOSVER
            z80.getRegisters().setBC((short) 0x0231); // ROM version
            z80.getRegisters().setDE((short) 0x0210); // DISK version
            //Log.Write("_DOSVER ret BC(ROMVer):%04x DE(DISKVer):%04x", z80.getRegisters().getBC(), z80.getRegisters().getDE());
        } else {
            Log.write(String.format("unknown 0x%02x", function));
        }

        z80.executeRet();
    }

    private static String getASCIIZ(Z80Processor z80, int reg) {
        int messageAddress = reg;
        List<Byte> bytesToPrint = new ArrayList<>();
        byte byteToPrint;
        while ((byteToPrint = z80.getMemory().get(messageAddress)) != 0) {
            bytesToPrint.add(byteToPrint);
            messageAddress++;
        }
        return new String(mdsound.Common.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
    }
}
