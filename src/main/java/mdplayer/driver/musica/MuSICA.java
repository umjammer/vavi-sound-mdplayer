package mdplayer.driver.musica;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.mgsdrv.Mapper;
import mdplayer.driver.mgsdrv.MapperRamCartridge;
import mdplayer.driver.mgsdrv.MsxMemory;
import mdplayer.driver.mgsdrv.MsxPort;
import mdplayer.plugin.BasePlugin;
import moonDriver.common.GD3;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class MuSICA extends BaseDriver {

    private static final Logger logger = getLogger(MuSICA.class.getName());

    private final GD3 gd3 = new GD3();

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Vgm.Gd3 ret = new Vgm.Gd3();
        if (buf != null && buf.length > 8) {
            run(buf);
            ret.trackName = gd3.TrackName;
            ret.trackNameJ = gd3.TrackNameJ;
            ret.notes = gd3.Notes;
        }

        return ret;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;

        try {
            run(vgmBuf);
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processOneFrame() {
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
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void interrupt() {
        //logger.log(Level.TRACE, "\r\n_INTER(001FH)");
        z80.getRegisters().setPC((short) 0x6029);
        z80.getRegisters().setPC((short) 0xf380);
        z80.continue_();
        //debugRegisters(z80);

        z80.getRegisters().setPC((short) 0x6032);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();

        byte playFg = (byte) (z80.getRegisters().getA() & 0x1);
        if (playFg == 0) stopped = true;
        vgmCurLoop = z80.getRegisters().getHL() & 0xffff;
    }

    private static byte[] program = null;
    private static byte DollarCode;
    private Z80Processor z80;
    private Mapper mapper;
    static int baseClockAY8910 = 1789773;
    static int baseClockYM2413 = 3579545;
    static int baseClockK051649 = 1789773;

    private void run(byte[] vgmBuf) {
        Path crntDir = Path.of(System.getProperty("user.dir"));
        Path fileName = crntDir.resolve("KINROU5.DRV");
        DollarCode = '$';

        z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setMemory(new MsxMemory(plugin.audio.chipRegister, model));
        z80.setPortsSpace(new MsxPort(((MsxMemory) z80.getMemory()).slot, plugin.audio.chipRegister, null, model));
        z80.beforeInstructionFetch().addListener(this::Z80OnBeforeInstructionFetch);

        mapper = new Mapper((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1], (MsxMemory) z80.getMemory());

        //Stopwatch sw = new Stopwatch();
        //sw.Start();

        z80.reset();

        // Loading a program and setting it in memory
        try {
            program = Files.readAllBytes(fileName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        z80.getMemory().setContents(0x6000 - 7, program, 0, null);
        z80.getRegisters().setPC((short) 0x6000);

        logger.log(Level.TRACE, "_INITAL(6020H)");
        z80.getRegisters().setPC((short) 0x6020);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();

        logger.log(Level.TRACE, "MSX-MUSIC slot %02x", z80.getMemory().get(0x6010));
        logger.log(Level.TRACE, "SCC       slot %02x", z80.getMemory().get(0x6011));

        byte[] mgsdata = vgmBuf;
        short dataAdr = (short) ((vgmBuf[1] & 0xff) + (vgmBuf[2] & 0xff) * 0x100);
        z80.getMemory().setContents(dataAdr - 7, vgmBuf, 0, null);

        logger.log(Level.TRACE, "_MPLAY2(6026H)");
        z80.getRegisters().setPC((short) 0x6026);
        z80.getRegisters().setHL(dataAdr);
        z80.getRegisters().setDE(dataAdr);
        z80.getRegisters().setA((byte) 0); // Repeat count (0:infinity)
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();
        logger.log(Level.TRACE, "MPLAY2 IsSuccess? RegC=%02x", z80.getRegisters().getC());
        if (z80.getRegisters().getCF().intValue() == 0x01) {
            debugRegisters(z80);
            throw new IllegalStateException("MPLAY2 Fail");
        }

        int[] index = new int[1];
        //logger.log(Level.TRACE, "_GETPAR(6047H)");
        z80.getRegisters().setPC((short) 0x6047);
        z80.getRegisters().setSP((short) 0xf380);
        z80.getRegisters().setHL(dataAdr);
        z80.getRegisters().setC((byte) 2); // title
        z80.continue_();
        if (z80.getRegisters().getCF().intValue() == 0) {
            index[0] = z80.getRegisters().getHL() & 0xffff;
            gd3.TrackName = Common.getNRDString(z80.getMemory().getContents(0, z80.getMemory().getSize()), /* ref */ index);
            gd3.TrackNameJ = gd3.TrackName;
        }

        //logger.log(Level.TRACE, "_GETPAR(6047H)");
        z80.getRegisters().setPC((short) 0x6047);
        z80.getRegisters().setSP((short) 0xf380);
        z80.getRegisters().setHL(dataAdr);
        z80.getRegisters().setC((byte) 3); // memo
        z80.continue_();
        if (z80.getRegisters().getCF().intValue() == 0) {
            index[0] = z80.getRegisters().getHL() & 0xffff;
            gd3.Notes = Common.getNRDString(z80.getMemory().getContents(0, z80.getMemory().getSize()), /* ref */ index);
        }
    }

    private String PlayingFileName;

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    private void Z80OnBeforeInstructionFetch(BeforeInstructionFetchEvent args) {
        //Absolutely minimum implementation of CP/M for ZEXALL and ZEXDOC to work

        var z80 = (Z80Processor) args.getSource();

        if (z80.getRegisters().getPC() == 0) {//0:JP WBOOT
            args.getExecutionStopper().stop(false);
        } else if (z80.getRegisters().getPC() == 0x0005) {
            //logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x", z80.getRegisters().C);
            callBIOS(args, z80);
        } else if (z80.getRegisters().getPC() == 0x000c) {
            //logger.log(Level.TRACE, "Call RDSLT(0x000c) Reg.A=%02x Reg.HL={1:x04}", z80.getRegisters().A, z80.getRegisters().HL);

            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            z80.getRegisters().setA(((MsxMemory) z80.getMemory()).readSlotMemoryAdr(
                    (slot & 0x03),
                    (slot & 0x0c) >> 2,
                    z80.getRegisters().getHL()
            ));
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0014) {
            logger.log(Level.TRACE, "Call WRSLT(0x0014) Reg.A=%02x Reg.HL={1:x04} Reg.E=%02x", z80.getRegisters().getA(), z80.getRegisters().getHL(), z80.getRegisters().getE());
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x001c) {
            logger.log(Level.TRACE, "Call CALSLT(0x001c) Reg.IY=%04x Reg.IX={1:x04}", z80.getRegisters().getIY(), z80.getRegisters().getIX());
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0024) {
            //logger.log(Level.TRACE, "Call ENASLT(0x0024) Reg.A=%02x Reg.HL={1:x04}", z80.getRegisters().A, z80.getRegisters().HL);
            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.getMemory()).changePage(
                    (slot & 0x03)
                    , ((slot & 0x0c) >> 2)
                    , ((z80.getRegisters().getH() & 0xc0) >> 6)
            );
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0030) {
            logger.log(Level.TRACE, "Call CALLF(0x0030)");
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0090) {
            logger.log(Level.TRACE, "Call GICINI (0090H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0093) {
            logger.log(Level.TRACE, "Call WRTPSG (0093H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0096) {
            logger.log(Level.TRACE, "Call RDPSG (0096H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0138 || z80.getRegisters().getPC() == 0x013B || z80.getRegisters().getPC() == 0x015C || z80.getRegisters().getPC() == 0x015f) {
            logger.log(Level.TRACE, "Call InterSlot");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x4601) {
            logger.log(Level.TRACE, "JP NEWSTT(0x4601) Reg.HL=%04x", z80.getRegisters().getHL());
            String msg = getAsciiz(z80, (short) z80.getRegisters().getHL());
            logger.log(Level.TRACE, "(HL)={0}", msg);
            if (msg.equals(":_SYSTEM")) {
                args.getExecutionStopper().stop(false);
            }
        } else if (z80.getRegisters().getPC() >= mapper.jumpAddress && z80.getRegisters().getPC() < mapper.jumpAddress + 16) {
            //logger.log(Level.TRACE, "Call MAPPER PROC(0x%04x～) PC-%04x:{1:x04}", mapper.JumpAddress, z80.getRegisters().PC - mapper.JumpAddress);
            mapper.CallMapperProc(args, z80, z80.getRegisters().getPC() - mapper.jumpAddress);
        } else if ((z80.getRegisters().getPC() & 0xffff) == 0xffca) {
            //logger.log(Level.TRACE, "Call EXTBIO(0xffca) Reg.DE=%04x", z80.getRegisters().DE);
            callEXTBIO(args, z80);
        }

        //debugRegisters(z80);
    }

    private static void debugRegisters(Z80Processor z80) {
        logger.log(Level.TRACE, "Reg PC:%04x AF:%04x BC:%04x DE:%04x HL:%04x IX:%04x IY:%04x"
                , z80.getRegisters().getPC()
                , z80.getRegisters().getAF(), z80.getRegisters().getBC(), z80.getRegisters().getDE(), z80.getRegisters().getHL()
                , z80.getRegisters().getIX(), z80.getRegisters().getIY());
    }

    private void callEXTBIO(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte funcType = z80.getRegisters().getD();
        byte function = z80.getRegisters().getE();

        switch (funcType & 0xff) {
            case 0x04:
                // logger.log(Level.TRACE, " EXTBIO MemoryMapper");
                EXTBIO_MemoryMapper(args, z80, function);
                break;
            case 0xf0:
                // MGSDRV向けファンクションコール
                z80.getRegisters().setA((byte) 0); // 非常駐時
                break;
            default:
                logger.log(Level.TRACE, " EXTBIO Unknown type");
                break;
        }

        z80.executeRet();
    }

    private void EXTBIO_MemoryMapper(BeforeInstructionFetchEvent args, Z80Processor z80, byte function) {
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
            var bytesToPrint = new ArrayList<Byte>();
            byte byteToPrint;
            while ((byteToPrint = z80.getMemory().get(messageAddress)) != DollarCode) {
                bytesToPrint.add(byteToPrint);
                messageAddress++;
            }

            var stringToPrint = new String(ByteUtil.toByteArray(bytesToPrint));
            System.out.print(stringToPrint);
        } else if (function == 2) {
            var byteToPrint = z80.getRegisters().getE();
            System.out.print((char) byteToPrint);
        } else if (function == 0x62) {
            //_TERM
            logger.log(Level.TRACE, "_TERM ErrorCode:%02x".formatted(z80.getRegisters().getB()));
            args.getExecutionStopper().stop(false);
            return;

        } else if (function == 0x6b) {
            //_GENV
            //logger.log(Level.TRACE, "_GENV HL:%04x DE:%04x B:%02x", z80.getRegisters().HL, z80.getRegisters().DE, z80.getRegisters().B);
            String msg = getAsciiz(z80, z80.getRegisters().getHL());
            //logger.log(Level.TRACE, "(HL)={0}", msg);

            if (msg.equals("PARAMETERS")) {
                byte[] option = "/z".getBytes();
                for (int i = 0; i < option.length; i++)
                    z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
                z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.length, (byte) 0);
            } else if (msg.equals("SHELL")) {
                //byte[] option = Encoding.ASCII.GetBytes("c:\\dummy");
                //for (int i = 0; i < option.Length; i++) z80.Memory[z80.getRegisters().DE + i] = option[i];
                //z80.Memory[z80.getRegisters().DE + option.Length] = 0;
                z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
            } else {
                z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
            }

            z80.getRegisters().setA((byte) 0x00); // Error number
            z80.getRegisters().setDE((short) 0x00); // value

        } else if (function == 0x6c) {
            //_SENV
            //logger.log(Level.TRACE, "_SENV HL:%04x DE:%04x", z80.getRegisters().HL, z80.getRegisters().DE);
            //String msg = GetASCIIZ(z80, (short)z80.getRegisters().HL);
            //logger.log(Level.TRACE, "(HL)={0}", msg);
            //msg = GetASCIIZ(z80, (short)z80.getRegisters().DE);
            //logger.log(Level.TRACE, "(DE)={0}", msg);
            z80.getRegisters().setA((byte) 0x00);//Error number
        } else if (function == 0x6f) {
            //_DOSVER
            z80.getRegisters().setBC((short) 0x0231); // ROM version
            z80.getRegisters().setDE((short) 0x0210); // DISK version
            //logger.log(Level.TRACE, "_DOSVER ret BC(ROMVer):%04x DE(DISKVer):{1:x04}", z80.getRegisters().BC, z80.getRegisters().DE);
        } else {
            logger.log(Level.ERROR, "unknown 0x%02x".formatted(function));
            debugRegisters(z80);
        }

        z80.executeRet();
    }

    private static String getAsciiz(Z80Processor z80, short reg) {
        var messageAddress = reg;
        var bytesToPrint = new ArrayList<Byte>();
        byte byteToPrint;
        while ((byteToPrint = z80.getMemory().get(messageAddress)) != 0) {
            bytesToPrint.add(byteToPrint);
            messageAddress++;
        }
        return new String(ByteUtil.toByteArray(bytesToPrint));
    }
}
