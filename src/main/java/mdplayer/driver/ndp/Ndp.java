package mdplayer.driver.ndp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import dotnet4j.util.compat.TriConsumer;
import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import mdplayer.driver.mgsdrv.Mapper;
import mdplayer.driver.mgsdrv.MapperRamCartridge;
import mdplayer.driver.mgsdrv.MsxMemory;
import mdplayer.driver.mgsdrv.MsxPort;
import mdplayer.driver.mgsdrv.Z80Opcode;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


// MSX NDP
public class Ndp {

    private static final Logger logger = getLogger(Ndp.class.getName());

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

    public int interrupt(Runnable stop) {
        byte playFG = 0;
        try {
            z80.getRegisters().setPC((short) 0xc009);
            z80.getRegisters().setSP((short) 0xf380);
            z80.continue_();

            z80.getRegisters().setPC((short) 0xc033);
            z80.getRegisters().setSP((short) 0xf380);
            z80.continue_();
            playFG = (byte) (z80.getRegisters().getA() & 0xf);
            if (playFG == 0x0) stop.run();

            z80.getRegisters().setPC((short) 0xc036);
            z80.getRegisters().setSP((short) 0xf380);
            z80.continue_();
            playFG = (byte) (z80.getRegisters().getA() & 0xf);
            playFG = (byte) ((((z80.getMemory().get(0x4000) & 0xff) | (z80.getMemory().get(0x4001) & 0xff)) == 0 ? 1 : (playFG & 1)) |
                    (((z80.getMemory().get(0x4002) & 0xff) | (z80.getMemory().get(0x4003) & 0xff)) == 0 ? 2 : (playFG & 2)) |
                    (((z80.getMemory().get(0x4004) & 0xff) | (z80.getMemory().get(0x4005) & 0xff)) == 0 ? 4 : (playFG & 4)) |
                    (((z80.getMemory().get(0x4006) & 0xff) | (z80.getMemory().get(0x4007) & 0xff)) == 0 ? 8 : (playFG & 8))
            );
            if (playFG == 0xf) stop.run();

            z80.getRegisters().setPC((short) 0xc039);
            z80.getRegisters().setSP((short) 0xf380);
            z80.continue_();
            int a = z80.getRegisters().getA() & 0xff;
            return (a == 255) ? 0 : a;
        } catch (RuntimeException ex) {
            stop.run();
            throw ex;
        }
    }

    void run(byte[] vgmBuf) throws IOException, URISyntaxException {
        Path fileName = Path.of(Ndp.class.getResource("NDP.BIN").toURI());

        z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setMemory(new MsxMemory(k051649Write));
        z80.setPortsSpace(new MsxPort(((MsxMemory) z80.getMemory()).slot, null, ay8910Write, ym2413Write));
        z80.beforeInstructionFetch().addListener(this::z80OnBeforeInstructionFetch);

        mapper = new Mapper((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1], (MsxMemory) z80.getMemory());

        //StopWatch sw = new StopWatch();
        //sw.start();

        z80.reset();

        // Loading a program and setting it in memory
        program = Files.readAllBytes(fileName);
        z80.getMemory().setContents(0xc000 - 7, program, 0, null); // -7 Binary file header information
        z80.getMemory().setContents(0x4000 - 7, vgmBuf, 0, null);

        // Initialize HTIMI (timer interrupt hook) at 0xFD9F with RET (0xC9)
        // The NDP driver copies the old hook content and calls it during interrupt processing
        // If this contains NOPs (0x00), the code will fall through and crash
        z80.getMemory().set(0xFD9F, (byte) 0xC9); // RET

        logger.log(Level.TRACE, "NDPINI(C000H)");
        z80.getRegisters().setPC((short) 0xc000);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();

        // Start playback (MSTART C003) - This sets STATS=1 enabling the driver
        // MSTART internally uses BGMADR which defaults to 0x4000
        logger.log(Level.TRACE, "MSTART(C003H)");
        z80.getRegisters().setPC((short) 0xc003);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();

        // IMAIN direct call (C051) - process one frame
        logger.log(Level.TRACE, "IMAIN(C051H)");
        z80.getRegisters().setPC((short) 0xc051);
        z80.getRegisters().setSP((short) 0xf380);
        z80.continue_();
        //if (z80.getRegisters().getCF().intValue() == 0x01) {
        //    debugRegisters(z80);
        //    throw new Exception("MPLAY2 Fail");
        //}

        //int index;
        ////logger.log(Level.TRACE, "_GETPAR(6047H)");
        //z80.getRegisters().setPC((short) 0x6047);
        //z80.getRegisters().setSP((short) 0xf380);
        //z80.getRegisters().setHL((short) dataAdr);
        //z80.getRegisters().setC((byte) 2); // title
        //z80.continue_();
        //if (z80.getRegisters().getCF().intValue() == 0) {
        //    index = z80.getRegisters().getHL() & 0xffff;
        //    metaData.trackName = Common.getNRDString(z80.getMemory(), /* ref */ index);
        //    metaData.trackNameJ = metaData.trackName;
        //}

        ////logger.log(Level.TRACE, "_GETPAR(6047H)");
        //z80.getRegisters().setPC((short) 0x6047);
        //z80.getRegisters().setSP((short) 0xf380);
        //z80.getRegisters().setHL((short) dataAdr);
        //z80.getRegisters().setC((byte) 3); // memo
        //z80.continue_();
        //if (z80.getRegisters().getCF().intValue() == 0) {
        //    index = z80.getRegisters().getHL() & 0xffff;
        //    metaData.notes = Common.getNRDString(z80.getMemory(), /* ref */ index);
        //}
    }

    private String playingFileName;

    public String getPlayingFileName() {
        return playingFileName;
    }

    private void z80OnBeforeInstructionFetch(BeforeInstructionFetchEvent args) {
        // Absolutely minimum implementation of CP/M for ZEXALL and ZEXDOC to work

        var z80 = (Z80Processor) args.getSource();

        if (z80.getRegisters().getPC() == 0) { // 0:JP WBOOT
            args.getExecutionStopper().stop(false);
            return;
        } else if (z80.getRegisters().getPC() == 0x0005) {
            //logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
            callBIOS(args, z80);
        } else if (z80.getRegisters().getPC() == 0x000c) {
            //logger.log(Level.TRACE, "Call RDSLT(0x000c) Reg.A=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA() & 0xff, z80.getRegisters().getHL() & 0xffff));

            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            z80.getRegisters().setA(((MsxMemory) z80.getMemory()).readSlotMemoryAdr(
                    slot & 0x03,
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
            //logger.log(Level.TRACE, "Call ENASLT(0x0024) Reg.A=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA() & 0xff, z80.getRegisters().getHL() & 0xffff));
            int page = (z80.getRegisters().getH() & 0xc0) >> 6;
            if (page == 3) {
                // NDP.BIN seems to fail detecting its own slot (3-1) and tries to map Slot 0 to Page 3 (Code)
                // We force Slot 3-1 (0x87) if target is Page 3.
                if ((z80.getRegisters().getA() & 0xff) != 0x87) {
                    z80.getRegisters().setA((byte) 0x87);
                }
            }
            int slot = (z80.getRegisters().getA() & 0xff) & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.getMemory()).changePage(
                    slot & 0x03,
                    (slot & 0x0c) >> 2,
                    page
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
            logger.log(Level.TRACE, "JP NEWSTT(0x4601) Reg.HL=%04x".formatted(z80.getRegisters().getHL() & 0xffff));
            String msg = getAsciiZ(z80, z80.getRegisters().getHL());
            logger.log(Level.TRACE, "(HL)=%s".formatted(msg));
            if (msg.equals(":_SYSTEM")) {
                args.getExecutionStopper().stop(false);
            }
        } else if ((z80.getRegisters().getPC() & 0xffff) >= mapper.jumpAddress && (z80.getRegisters().getPC() & 0xffff) < mapper.jumpAddress + 16) {
            //logger.log(Level.TRACE, "Call MAPPER PROC(0x%04x~) PC-%04x:%04x".formatted(mapper.jumpAddress, (z80.getRegisters().getPC() & 0xffff) - mapper.jumpAddress));
            mapper.callMapperProc(args, z80, (z80.getRegisters().getPC() & 0xffff) - mapper.jumpAddress);
        } else if ((z80.getRegisters().getPC() & 0xffff) == 0xffca) {
            //logger.log(Level.TRACE, "Call EXTBIO(0xffca) Reg.DE=%04x".formatted(z80.getRegisters().getDE() & 0xffff));
            callExtBio(args, z80);
        }

        //debugRegisters(z80);
    }

    private static void debugRegisters(Z80Processor z80) {
        String nimo = Z80Opcode.getNimo(z80.getMemory().get(z80.getRegisters().getPC() & 0xffff), z80.getMemory().get((z80.getRegisters().getPC() & 0xffff) + 1), z80.getMemory().get((z80.getRegisters().getPC() & 0xffff) + 2));
        logger.log(Level.TRACE, "nimo:%7s Reg PC:%04x AF:%04x BC:%04x DE:%04x HL:%04x IX:%04x IY:%04x".formatted(nimo,
                z80.getRegisters().getPC() & 0xffff,
                z80.getRegisters().getAF() & 0xffff, z80.getRegisters().getBC() & 0xffff, z80.getRegisters().getDE() & 0xffff, z80.getRegisters().getHL() & 0xffff,
                z80.getRegisters().getIX() & 0xffff, z80.getRegisters().getIY() & 0xffff));
    }

    private void callExtBio(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte funcType = z80.getRegisters().getD();
        byte function = z80.getRegisters().getE();

        switch (funcType & 0xff) {
            case 0x00:
                //logger.log(Level.TRACE, " EXTBIO broadcast");
                switch (function) {
                    case 0:
                        break;
                    default:
                        logger.log(Level.TRACE, " EXTBIO broadcast Unknown function: %02x".formatted(function & 0xff) );
                        break;
                }
                break;
            case 0x04:
                //logger.log(Level.TRACE, " EXTBIO MemoryMapper");
                extBio_MemoryMapper(args, z80, function);
                break;
            case 0xf0:
                // Function call for MGSDRV
                z80.getRegisters().setA((byte) 0); // Non-stationed
                break;
            default:
                logger.log(Level.TRACE, " EXTBIO Unknown type: %02x".formatted(funcType & 0xff));
                break;
        }
        z80.executeRet();
    }

    private void extBio_MemoryMapper(BeforeInstructionFetchEvent args, Z80Processor z80, byte function) {
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

        if (function == 0) {
            // _TERM0 - Terminate program (no error code)
            args.getExecutionStopper().stop(false);
            return;
        } else if (function == 9) {
            var messageAddress = z80.getRegisters().getDE() & 0xffff;
            var bytesToPrint = new ArrayList<Byte>();
            byte byteToPrint;
            while ((byteToPrint = z80.getMemory().get(messageAddress & 0xffff)) != DollarCode) {
                bytesToPrint.add(byteToPrint);
                messageAddress = (short) ((messageAddress + 1) & 0xffff);
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
            //logger.log(Level.TRACE, "_GENV HL:%04x DE:%04x B:%02x".formatted(z80.getRegisters().getHL() & 0xffff, z80.getRegisters().getDE() & 0xffff, z80.getRegisters().getB() & 0xff));
            String msg = getAsciiZ(z80, z80.getRegisters().getHL());
            //logger.log(Level.TRACE, "(HL)=%s".formatted(msg));

            if (msg.equals("PARAMETERS")) {
                byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
                for (int i = 0; i < option.length; i++)
                    z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
                z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.length, (byte) 0);
            } else if (msg.equals("SHELL")) {
                //byte[] option = "c:\\dummy".getBytes(StandardCharsets.US_ASCII);
                //for (int i = 0; i < option.length; i++) z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
                //z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.length, 0);
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
            logger.log(Level.WARNING, "unknown 0x%02x".formatted(function));
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
        return new String(ByteUtil.toByteArray(bytesToPrint), StandardCharsets.US_ASCII);
    }
}
