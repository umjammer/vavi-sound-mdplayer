package mdplayer.driver.musica;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import mdplayer.Common;
import mdplayer.emu.msx.Mapper;
import mdplayer.emu.msx.MapperRamCartridge;
import mdplayer.emu.msx.MsxMemory;
import mdplayer.emu.msx.MsxPort;
import vavi.util.ByteUtil;
import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;


/**
 * MuSICA BGM (
 *
 * @author kumatan
 */
public class MuSICA {

    private static final Logger logger = getLogger(MuSICA.class.getName());

    private static byte[] program = null;
    private static final byte DollarCode = '$';
    private Z80Processor z80;
    private Mapper mapper;
    public static final int baseClockAY8910 = 1789773;
    public static final int baseClockYM2413 = 3579545;
    public static final int baseClockK051649 = 1789773;

    TriConsumer<Integer, Integer, Integer> k051649Write;
    BiConsumer<Integer, Integer> ay8910Write;
    BiConsumer<Integer, Integer> ym2413Write;
    Consumer<String> updateTrackName;
    Consumer<String> updateNote;

    /** KINROU5.DRV location */
    String dir;

    public int interrupt() {
        //logger.log(Level.TRACE, "\r\n_INTER(001FH)");
        z80.getRegisters().setPC((short) 0x6029);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();
        //debugRegisters(z80);

        z80.getRegisters().setPC((short) 0x6032);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();

        return z80.getRegisters().getA() & 0x1;
    }

    public int getHL() {
        return z80.getRegisters().getHL() & 0xffff;
    }

    void run(byte[] vgmBuf) throws IOException {
        Path fileName = Path.of(dir, "KINROU5.DRV");

        z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setMemory(new MsxMemory(k051649Write));
        z80.setPortsSpace(new MsxPort(((MsxMemory) z80.getMemory()).slot, null, ay8910Write, ym2413Write));
        z80.beforeInstructionFetch().addListener(this::z80OnBeforeInstructionFetch);

        mapper = new Mapper((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1], (MsxMemory) z80.getMemory());

        //Stopwatch sw = new Stopwatch();
        //sw.Start();

        z80.reset();

        // Loading a program and setting it in memory
        program = Files.readAllBytes(fileName);
        z80.getMemory().setContents(0x6000 - 7, program, 0, null);
        z80.getRegisters().setPC((short) 0x6000);

        logger.log(Level.TRACE, "_INITAL(6020H)");
        z80.getRegisters().setPC((short) 0x6020);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();

        logger.log(Level.TRACE, "MSX-MUSIC slot %02x".formatted(z80.getMemory().get(0x6010) & 0xff));
        logger.log(Level.TRACE, "SCC       slot %02x".formatted(z80.getMemory().get(0x6011) & 0xff));

        byte[] mgsdata = vgmBuf;
        int dataAdr = (vgmBuf[1] & 0xff) + (vgmBuf[2] & 0xff) * 0x100;
        z80.getMemory().setContents(dataAdr - 7, vgmBuf, 0, null);

        logger.log(Level.TRACE, "_MPLAY2(6026H)");
        z80.getRegisters().setPC((short) 0x6026);
        z80.getRegisters().setHL((short) dataAdr);
        z80.getRegisters().setDE((short) dataAdr);
        z80.getRegisters().setA((byte) 0); // Repeat count (0:infinity)
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();
        logger.log(Level.TRACE, "MPLAY2 IsSuccess? RegC=%02x".formatted(z80.getRegisters().getC() & 0xff));
        if (z80.getRegisters().getCF().intValue() == 0x01) {
            debugRegisters(z80);
            throw new IllegalStateException("MPLAY2 Fail");
        }

        int[] index = new int[1];
        //logger.log(Level.TRACE, "_GETPAR(6047H)");
        z80.getRegisters().setPC((short) 0x6047);
        z80.getRegisters().setSP((short) 0xf380);
        z80.getRegisters().setHL((short) dataAdr);
        z80.getRegisters().setC((byte) 2); // title
        z80.continue_();
        if (z80.getRegisters().getCF().intValue() == 0) {
            index[0] = z80.getRegisters().getHL() & 0xffff;
            updateTrackName.accept(Common.getNRDString(z80.getMemory().getContents(0, z80.getMemory().getSize()), /* ref */ index));
        }

        //logger.log(Level.TRACE, "_GETPAR(6047H)");
        z80.getRegisters().setPC((short) 0x6047);
        z80.getRegisters().setSP((short) 0xf380);
        z80.getRegisters().setHL((short) dataAdr);
        z80.getRegisters().setC((byte) 3); // memo
        z80.continue_();
        if (z80.getRegisters().getCF().intValue() == 0) {
            index[0] = z80.getRegisters().getHL() & 0xffff;
            updateNote.accept(Common.getNRDString(z80.getMemory().getContents(0, z80.getMemory().getSize()), /* ref */ index));
        }
    }

    private void z80OnBeforeInstructionFetch(BeforeInstructionFetchEvent args) {
        // Absolutely minimum implementation of CP/M for ZEXALL and ZEXDOC to work

        var z80 = (Z80Processor) args.getSource();

        if (z80.getRegisters().getPC() == 0) { // 0:JP WBOOT
            args.getExecutionStopper().stop(false);
        } else if (z80.getRegisters().getPC() == 0x0005) {
            //logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
            callBIOS(args, z80);
        } else if (z80.getRegisters().getPC() == 0x000c) {
            //logger.log(Level.TRACE, "Call RDSLT(0x000c) Reg.A=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA() & 0xff, z80.getRegisters().getHL() & 0xffff));

            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            z80.getRegisters().setA(((MsxMemory) z80.getMemory()).readSlotMemoryAdr(
                    (slot & 0x03),
                    (slot & 0x0c) >> 2,
                    z80.getRegisters().getHL() & 0xffff
            ));
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0014) {
            logger.log(Level.TRACE, "Call WRSLT(0x0014) Reg.A=%02x Reg.HL=%04x Reg.E=%02x".formatted(z80.getRegisters().getA() & 0xff, z80.getRegisters().getHL() & 0xffff, z80.getRegisters().getE() & 0xff));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x001c) {
            logger.log(Level.TRACE, "Call CALSLT(0x001c) Reg.IY=%04x Reg.IX=%04x".formatted(z80.getRegisters().getIY() & 0xffff, z80.getRegisters().getIX() & 0xffff));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0024) {
            // logger.log(Level.TRACE, "Call ENASLT(0x0024) Reg.A=%02x Reg.HL=%04x".formatted(z80.getRegisters().A & 0xff, z80.getRegisters().HL & 0xffff));
            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.getMemory()).changePage(
                    (slot & 0x03),
                    ((slot & 0x0c) >> 2),
                    ((z80.getRegisters().getH() & 0xc0) >> 6)
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
        } else if (z80.getRegisters().getPC() == 0x0138 || z80.getRegisters().getPC() == 0x013b || z80.getRegisters().getPC() == 0x015c || z80.getRegisters().getPC() == 0x015f) {
            logger.log(Level.TRACE, "Call InterSlot");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x4601) {
            logger.log(Level.TRACE, "JP NEWSTT(0x4601) Reg.HL=%04x", z80.getRegisters().getHL() & 0xffff);
            String msg = getAsciiZ(z80, z80.getRegisters().getHL());
            logger.log(Level.TRACE, "(HL)=%s".formatted(msg));
            if (msg.equals(":_SYSTEM")) {
                args.getExecutionStopper().stop(false);
            }
        } else if ((z80.getRegisters().getPC() & 0xffff) >= Mapper.jumpAddress && (z80.getRegisters().getPC() & 0xffff) < Mapper.jumpAddress + 16) {
            //logger.log(Level.TRACE, "Call MAPPER PROC(0x%04x～) PC-%04x:%04x".formatted(mapper.JumpAddress, z80.getRegisters().PC - mapper.JumpAddress));
            mapper.callMapperProc(args, z80, (z80.getRegisters().getPC() & 0xffff) - Mapper.jumpAddress);
        } else if ((z80.getRegisters().getPC() & 0xffff) == 0xffca) {
            //logger.log(Level.TRACE, "Call EXTBIO(0xffca) Reg.DE=%04x".formatted(z80.getRegisters().DE));
            callEXTBIO(args, z80);
        }

        //debugRegisters(z80);
    }

    private static void debugRegisters(Z80Processor z80) {
        logger.log(Level.TRACE, "Reg PC:%04x AF:%04x BC:%04x DE:%04x HL:%04x IX:%04x IY:%04x".formatted(
                z80.getRegisters().getPC() & 0xffff,
                z80.getRegisters().getAF() & 0xffff, z80.getRegisters().getBC() & 0xffff, z80.getRegisters().getDE() & 0xffff, z80.getRegisters().getHL() & 0xffff,
                z80.getRegisters().getIX() & 0xffff, z80.getRegisters().getIY() & 0xffff));
    }

    private static void callEXTBIO(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte funcType = z80.getRegisters().getD();
        byte function = z80.getRegisters().getE();

        switch (funcType & 0xff) {
            case 0x04:
                //logger.log(Level.TRACE, " EXTBIO MemoryMapper");
                EXTBIO_MemoryMapper(args, z80, function);
                break;
            case 0xf0:
                // Function call for MGSDRV
                z80.getRegisters().setA((byte) 0); // When not present
                break;
            default:
                logger.log(Level.TRACE, " EXTBIO Unknown type: %02x".formatted(funcType & 0xff));
                break;
        }

        z80.executeRet();
    }

    private static void EXTBIO_MemoryMapper(BeforeInstructionFetchEvent args, Z80Processor z80, byte function) {
        switch (function) {
            case 0x02:
                z80.getRegisters().setA((byte) 0);
                z80.getRegisters().setBC((short) 0);
                z80.getRegisters().setHL((short) Mapper.tableAddress);
                break;
        }
    }

    private static void callBIOS(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte function = z80.getRegisters().getC();

        if (function == 9) {
            var messageAddress = z80.getRegisters().getDE();
            var bytesToPrint = new ArrayList<Byte>();
            byte byteToPrint;
            while ((byteToPrint = z80.getMemory().get(messageAddress & 0xffff)) != DollarCode) {
                bytesToPrint.add(byteToPrint);
                messageAddress++;
            }

            var stringToPrint = new String(ByteUtil.toByteArray(bytesToPrint));
            System.out.print(stringToPrint);
        } else if (function == 2) {
            var byteToPrint = z80.getRegisters().getE();
            System.out.print((char) byteToPrint);
        } else if (function == 0x62) {
            // _TERM
            logger.log(Level.TRACE, "_TERM ErrorCode:%02x".formatted(z80.getRegisters().getB() & 0xff));
            args.getExecutionStopper().stop(false);
            return;

        } else if (function == 0x6b) {
            // _GENV
            //logger.log(Level.TRACE, "_GENV HL:%04x DE:%04x B:%02x".formatted(z80.getRegisters().HL, z80.getRegisters().DE, z80.getRegisters().B));
            String msg = getAsciiZ(z80, z80.getRegisters().getHL());
            //logger.log(Level.TRACE, "(HL)=%s".formatted(msg));

            if (msg.equals("PARAMETERS")) {
                byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
                for (int i = 0; i < option.length; i++)
                    z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
                z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.length, (byte) 0);
            } else if (msg.equals("SHELL")) {
                //byte[] option = "c:\\dummy".getBytes(StandardCharsets.US_ASCII);
                //for (int i = 0; i < option.Length; i++) z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
                //z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.Length, (byte) 0);
                z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
            } else {
                z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
            }

            z80.getRegisters().setA((byte) 0x00); // Error number
            z80.getRegisters().setDE((short) 0x00); // value

        } else if (function == 0x6c) {
            // _SENV
            //logger.log(Level.TRACE, "_SENV HL:%04x DE:%04x".formatted(z80.getRegisters().getHL() & 0xffff, z80.getRegisters().getDE() & 0xffff));
            //String msg = getAsciiZ(z80, z80.getRegisters().getHL());
            //logger.log(Level.TRACE, "(HL)=%s".formatted(msg));
            //msg = getAsciiZ(z80, z80.getRegisters().getDE());
            //logger.log(Level.TRACE, "(DE)=%s".formatted(msg));
            z80.getRegisters().setA((byte) 0x00); // Error number
        } else if (function == 0x6f) {
            // _DOSVER
            z80.getRegisters().setBC((short) 0x0231); // ROM version
            z80.getRegisters().setDE((short) 0x0210); // DISK version
            //logger.log(Level.TRACE, "_DOSVER ret BC(ROMVer):%04x DE(DISKVer):%04x".formatted(z80.getRegisters().getBC() & 0xffff, z80.getRegisters().getDE() & 0xffff));
        } else {
            logger.log(Level.ERROR, "unknown 0x%02x".formatted(function & 0xff));
            debugRegisters(z80);
        }

        z80.executeRet();
    }

    private static String getAsciiZ(Z80Processor z80, short reg) {
        var messageAddress = reg & 0xffff;
        var bytesToPrint = new ArrayList<Byte>();
        byte byteToPrint;
        while ((byteToPrint = z80.getMemory().get(messageAddress & 0xffff)) != 0) {
            bytesToPrint.add(byteToPrint);
            messageAddress++;
        }
        return new String(ByteUtil.toByteArray(bytesToPrint));
    }
}
