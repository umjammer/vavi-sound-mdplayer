package mdplayer.driver.mgsdrv;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import mdplayer.emu.msx.Mapper;
import mdplayer.emu.msx.MapperRamCartridge;
import mdplayer.emu.msx.MsxMemory;
import mdplayer.emu.msx.MsxPort;
import vavi.util.ByteUtil;
import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;


/**
 * MSX MgsDrv
 *
 * @author kumatan
 */
public class MgsDrv {

    private static final Logger logger = getLogger(MgsDrv.class.getName());

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

    /** msgdrv.com dir */
    String dir;

    public int interrupt() {
        //logger.log(Level.TRACE, "\n_INTER(001FH)");
        z80.getRegisters().setPC((short) 0x601f);
        z80.getRegisters().setSP((short) 0x000a);
        z80.continue_();
        //DebugRegisters(z80);

        return z80.getRegisters().getA() & 0xff;
    }

    public int getD() {
        return z80.getRegisters().getD() & 0xff;
    }

    void run(byte[] vgmBuf) throws IOException {
        Path fileName = Path.of(dir, "MGSDRV.COM");

        z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setMemory(new MsxMemory(k051649Write));
        z80.setPortsSpace(new MsxPort(((MsxMemory) z80.getMemory()).slot, null, ay8910Write, ym2413Write));
        z80.beforeInstructionFetch().addListener(this::Z80OnBeforeInstructionFetch);

        mapper = new Mapper((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1], (MsxMemory) z80.getMemory());

        //StopWatch sw = new StopWatch();
        //sw.start();

        z80.reset();

        // Loading a program and setting it in memory
        if (program == null) program = Files.readAllBytes(fileName);
        z80.getMemory().setContents(0x100, program, 0, null);
        z80.getRegisters().setPC((short) 0x100);

        // A set of command line arguments
        byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
        z80.getMemory().set(0x80, (byte) option.length);
        for (int p = 0; p < option.length; p++) z80.getMemory().set(0x81 + p, option[p]);

        z80.continue_();

        //sw.stop();
        //logger.log(Level.TRACE, "Elapsed time: %d".formatted(sw.elapsed));

        // Switch to the segment where MGSDRV exists
        ((MsxMemory) z80.getMemory()).changePage(3, 1, 1); // slot3-1 to Page1
        ((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1]).setSegmentToPage(4, 1); // Set segment 0x4 to Page1 of slot3-1
        //((MsxMemory) z80.Memory).ChangePage(3, 1, 2);
        //((MapperRAMCartridge) ((MsxMemory) z80.getMemory'().slot.slots[3][1]).setSegmentToPage(0x1a, 2);

        logger.log(Level.DEBUG, "\n_SYSCK(0010H)");
        z80.getRegisters().setPC((short) 0x6010);
        z80.continue_();
        //debugRegisters(z80);

        logger.log(Level.DEBUG, "MSX-MUSIC slot %02x".formatted(z80.getRegisters().getD() & 0xff));
        logger.log(Level.DEBUG, "SCC       slot %02x".formatted(z80.getRegisters().getA() & 0xff));
        logger.log(Level.DEBUG, "MGSDRV Version %04x".formatted(z80.getRegisters().getHL() & 0xffff));

        logger.log(Level.DEBUG, "\n_INITM(0013H)");
        z80.getRegisters().setPC((short) 0x6013);
        z80.continue_();
        //debugRegisters(z80);

        byte[] mgsdata = vgmBuf;
        MapperRamCartridge cart = ((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1]);
        for (int i = 0; i < mgsdata.length; i++) {
            if (i % 0x4000 == 0) cart.setSegmentToPage(5 + (i / 0x4000), 2); // From segment 5 to page 2
            z80.getMemory().set(0x8000 + (i % 0x4000), mgsdata[i]);
        }
        cart.setSegmentToPage(5, 2);

        logger.log(Level.DEBUG, "\n_DATCK(0028H)");
        z80.getRegisters().setPC((short) 0x6028);
        z80.getRegisters().setHL((short) 0x8000);
        z80.continue_();
        //debugRegisters(z80);

        logger.log(Level.DEBUG, "\n_PLYST(0016H)");
        z80.getRegisters().setPC((short) 0x6016);
        z80.getRegisters().setDE((short) 0x8000);
        z80.getRegisters().setHL((short) 0xffff);
        z80.getRegisters().setB((byte) 0xff);
        z80.continue_();
        //debugRegisters(z80);
    }

    String playingFileName;

    private void Z80OnBeforeInstructionFetch(BeforeInstructionFetchEvent args) {
        // Absolutely minimum implementation of CP/M for ZEXALL and ZEXDOC to work

        Z80Processor z80 = (Z80Processor) args.getSource();

        if (z80.getRegisters().getPC() == 0) { // 0:JP WBOOT
            args.getExecutionStopper().stop(false);
        } else if (z80.getRegisters().getPC() == 0x0005) {
            //logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC()));
            callBIOS(args, z80);
        } else if (z80.getRegisters().getPC() == 0x000c) {
            //logger.log(Level.TRACE, "Call RDSLT(0x000c) Reg.a=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA(), z80.getRegisters().getHL()));

            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            z80.getRegisters().setA(((MsxMemory) z80.getMemory()).readSlotMemoryAdr(
                    slot & 0x03,
                    (slot & 0x0c) >> 2,
                    z80.getRegisters().getHL() & 0xffff
            ));
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0014) {
            logger.log(Level.DEBUG, "Call WRSLT(0x0014) Reg.a=%02x Reg.HL=%04x Reg.E=%02x".formatted(z80.getRegisters().getA(), z80.getRegisters().getHL(), z80.getRegisters().getE()));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x001c) {
            logger.log(Level.DEBUG, "Call CALSLT(0x001c) Reg.IY=%04x Reg.IX=%04x".formatted(z80.getRegisters().getIY(), z80.getRegisters().getIX()));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0024) {
            //logger.log(Level.TRACE, "\nCall ENASLT(0x0024) Reg.a=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA(), z80.getRegisters().getHL()));
            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.getMemory()).changePage(
                    (slot & 0x03),
                    ((slot & 0x0c) >> 2),
                    ((z80.getRegisters().getH() & 0xc0) >> 6)
            );
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0030) {
            logger.log(Level.DEBUG, "Call CALLF(0x0030)");
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0090) {
            logger.log(Level.DEBUG, "Call GICINI (0090H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0093) {
            logger.log(Level.DEBUG, "Call WRTPSG (0093H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0096) {
            logger.log(Level.DEBUG, "Call RDPSG (0096H/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0138 || z80.getRegisters().getPC() == 0x013B || z80.getRegisters().getPC() == 0x015C || z80.getRegisters().getPC() == 0x015f) {
            logger.log(Level.DEBUG, "Call InterSlot");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x4601) {
            logger.log(Level.DEBUG, "JP NEWSTT(0x4601) Reg.HL=%04x".formatted(z80.getRegisters().getHL()));
            String msg = getAsciiZ(z80, z80.getRegisters().getHL() & 0xffff);
            logger.log(Level.DEBUG, "(HL)=%s".formatted(msg));
            if (msg.equals(":_SYSTEM")) {
                args.getExecutionStopper().stop(false);
            }
        } else if ((z80.getRegisters().getPC() & 0xffff) >= Mapper.jumpAddress && (z80.getRegisters().getPC() & 0xffff) < Mapper.jumpAddress + 16) {
            //logger.log(Level.TRACE, "\nCall MAPPER PROC(0x%04x～) pc-%04x:%04x".formatted(mapper.JumpAddress, z80.getRegisters().getPC() - mapper.JumpAddress));
            mapper.callMapperProc(args, z80, (z80.getRegisters().getPC() & 0xffff) - Mapper.jumpAddress);
        } else if ((z80.getRegisters().getPC() & 0xffff) == 0xffca) {
            //logger.log(Level.TRACE, "\nCall EXTBIO(0xffca) Reg.DE=%04x".formatted(z80.getRegisters().getDE()));
            callEXTBIO(args, z80);
        }

        //DebugRegisters(z80);
    }

    private static void debugRegisters(Z80Processor z80) {
        logger.log(Level.DEBUG, "Reg pc:%04x AF:%04x BC:%04x DE:%04x HL:%04x IX:%04x IY:%04x".formatted(
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
            extbioMemorymapper(args, z80, function);
            break;
        case 0xf0:
            // Function call for MGSDRV
            z80.getRegisters().setA((byte) 0); // When not present
            break;
        default:
            logger.log(Level.DEBUG, " EXTBIO Unknown type");
            break;
        }

        z80.executeRet();
    }

    private static void extbioMemorymapper(BeforeInstructionFetchEvent args, Z80Processor z80, byte function) {
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
            List<Byte> bytesToPrint = new ArrayList<>();
            byte byteToPrint;
            while ((byteToPrint = z80.getMemory().get(messageAddress & 0xffff)) != DollarCode) {
                bytesToPrint.add(byteToPrint);
                messageAddress++;
            }

            String stringToPrint = new String(ByteUtil.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
            System.out.print(stringToPrint);
        } else if (function == 2) {
            byte byteToPrint = z80.getRegisters().getE();
            char charToPrint = (char) (byteToPrint & 0xff);
            System.out.print(charToPrint);
        } else if (function == 0x62) {
            // _TERM
            logger.log(Level.DEBUG, "_TERM ErrorCode:%02x".formatted(z80.getRegisters().getB()));
            args.getExecutionStopper().stop(false);
            return;

        } else if (function == 0x6b) {
            // _GENV
            //logger.log(Level.TRACE, "_GENV HL:%04x DE:%04x B:%02x".formatted(z80.getRegisters().getHL(), z80.getRegisters().getDE(), z80.getRegisters().getB()));
            String msg = getAsciiZ(z80, z80.getRegisters().getHL() & 0xffff);
            //logger.log(Level.TRACE, "(HL)=%d".formatted(msg));

            if (msg.equals("PARAMETERS")) {
                byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
                for (int i = 0; i < option.length; i++) z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
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
            //logger.log(Level.TRACE, "_SENV HL:%04x DE:%04x".formatted(z80.getRegisters().getHL(), z80.getRegisters().getDE()));
            String msg = getAsciiZ(z80, z80.getRegisters().getHL() & 0xffff);
            //logger.log(Level.TRACE, "(HL)=%d".formatted(msg));

            msg = getAsciiZ(z80, z80.getRegisters().getDE() & 0xffff);
            //logger.log(Level.TRACE, "(DE)=%d".formatted(msg));

            z80.getRegisters().setA((byte) 0x00); // Error number
        } else if (function == 0x6f) {
            // _DOSVER
            z80.getRegisters().setBC((short) 0x0231); // ROM version
            z80.getRegisters().setDE((short) 0x0210); // DISK version
            //logger.log(Level.TRACE, "_DOSVER ret BC(ROMVer):%04x DE(DISKVer):%04x".formatted(z80.getRegisters().getBC(), z80.getRegisters().getDE()));
        } else {
            logger.log(Level.DEBUG, "unknown 0x%02x".formatted(function));
        }

        z80.executeRet();
    }

    private static String getAsciiZ(Z80Processor z80, int reg) {
        int messageAddress = reg;
        List<Byte> bytesToPrint = new ArrayList<>();
        byte byteToPrint;
        while ((byteToPrint = z80.getMemory().get(messageAddress)) != 0) {
            bytesToPrint.add(byteToPrint);
            messageAddress++;
        }
        return new String(ByteUtil.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
    }
}
