package mdplayer.driver.musica;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import konamiman.z80.Z80Processor;
import konamiman.z80.Z80ProcessorImpl;
import konamiman.z80.events.BeforeInstructionFetchEvent;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.mgsdrv.Mapper;
import mdplayer.driver.mgsdrv.MapperRamCartridge;
import mdplayer.driver.mgsdrv.MsxMemory;
import mdplayer.driver.mgsdrv.MsxPort;
import mdplayer.driver.mgsdrv.MsxVdp;
import mdplayer.plugin.BasePlugin;
import vavi.util.ByteUtil;
import vavi.util.StringUtil;

import static java.lang.System.getLogger;


// MSD
public class MuSICA_K4 extends BaseDriver {

    private static final Logger logger = getLogger(MuSICA_K4.class.getName());

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        throw new UnsupportedOperationException();
    }

    public Vgm.Gd3 getGD3Info(byte[] buf, byte[] vcdBuf) {
        Vgm.Gd3 ret = new Vgm.Gd3();
        if (buf != null && buf.length > 8) {
            try {
            run(buf, vcdBuf);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                return null;
            }
            if (bgmBin == null) return null;
            Vgm.Gd3 gd3 = (new MuSICA()).getGD3Info(bgmBin, null);
            ret.trackName = gd3.trackName;
            ret.trackNameJ = gd3.trackNameJ;
            ret.notes = gd3.notes;
        }

        return ret;
    }

    public boolean compile(byte[] vgmBuf, byte[] vcdBuf) {
logger.log(Level.INFO, "\n" + StringUtil.getDump(vgmBuf, 128));
        try {
            run(vgmBuf, vcdBuf);
            if (bgmBin == null) {
logger.log(Level.WARNING, "bgmBin is null");
                return false;
            }
            //Files.writeAllBytes(Path.of("/Users/kuma/Desktop/test.bgm", bgmBin));
            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        this.plugin = plugin;
        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processOneFrame() {
        throw new UnsupportedOperationException();
    }

    private static byte[] kinrou4 = null;
    private static final byte DollarCode = '$';
    private Z80Processor z80;
    private Mapper mapper;
    private MsxVdp vdp;
    private static short DTAAddress = 0x0080;
    private static short FCBAddress = 0x0080;
    private byte[] msdBin = null;
    private byte[] vcdBin = null;
    private byte[] bgmBin = null;
    private static final List<Byte> consoleBuf = new ArrayList<>();

    private void run(byte[] msdBin, byte[] vcdBin) throws IOException, URISyntaxException {
        this.msdBin = msdBin;
        this.vcdBin = vcdBin;
        this.bgmBin = null;

        Path fileName = Path.of(MuSICA_K4.class.getResource("KINROU4.COM").toURI());

        vdp = new MsxVdp();
        z80 = new Z80ProcessorImpl();
        z80.setClockSynchronizer(null);
        z80.setAutoStopOnRetWithStackEmpty(true);
        z80.setMemory(new MsxMemory(plugin.audio.chipRegister, model));
        z80.setPortsSpace(new MsxPort(((MsxMemory) z80.getMemory()).slot, plugin.audio.chipRegister, vdp, model));
        z80.beforeInstructionFetch().addListener(this::Z80OnBeforeInstructionFetch);
        mapper = new Mapper((MapperRamCartridge) ((MsxMemory) z80.getMemory()).slot.slots[3][1], (MsxMemory) z80.getMemory());
        z80.reset();

        // Loading a program and setting it in memory
        kinrou4 = Files.readAllBytes(fileName);
        z80.getMemory().setContents(0x100, kinrou4, 0, null);
        z80.getRegisters().setPC((short) 0x100);
        z80.getRegisters().setSP((short) 0xf380);

        //boolean existVCD = false;
        //if (vcdBin != null && vcdBin.length > 0) existVCD = true;

        // A set of command line arguments
        byte[] option = " DUMMY.MSD DUMMY.VCD".getBytes(StandardCharsets.US_ASCII);
        z80.getMemory().set(0x80, (byte) option.length);
        for (int p = 0; p < option.length; p++) z80.getMemory().set(0x81 + p, option[p]);
        option = " DUMMY   MSD     DUMMY   VCD".getBytes(StandardCharsets.US_ASCII);
        for (int p = 0; p < option.length; p++) z80.getMemory().set(0x5c + p, option[p]);
        z80.getMemory().set(0x5c, (byte) 0x00);
        z80.getMemory().set(0x68, (byte) 0x00);
        z80.getMemory().set(0x69, (byte) 0x00);
        z80.getMemory().set(0x6a, (byte) 0x00);
        z80.getMemory().set(0x6b, (byte) 0x00);
        z80.getMemory().set(0x6c, (byte) 0x00);

        z80.getMemory().set(0x06, vdp.m); // V9938 base Address m port0/1   read
        z80.getMemory().set(0x07, vdp.n); // V9938 base Address n port0/1/3 write   port2 read

        z80.continue_();
        conFlash();

        if (bgmBin == null) {
            logger.log(Level.TRACE, "Compile Fail");
            return;
        }

        logger.log(Level.TRACE, "Compile Success. Length = %04x", bgmBin.length);
    }

//    private String playingFileName;
//
//    public void setPlayingFileName(String playingFileName) {
//        this.playingFileName = playingFileName;
//    }

    private void Z80OnBeforeInstructionFetch(BeforeInstructionFetchEvent args) {
        //Absolutely minimum implementation of CP/M for ZEXALL and ZEXDOC to work

        var z80 = (Z80Processor) args.getSource();

        if (z80.getRegisters().getPC() == 0) { // 0:JP WBOOT
            args.getExecutionStopper().stop(false);
        } else if (z80.getRegisters().getPC() == 0x0005) {
            //logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC()));
            callBIOS(args);
        } else if (z80.getRegisters().getPC() == 0x000c) {
            logger.log(Level.TRACE, "Call RDSLT(0x000c) Reg.A=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA() & 0xff, z80.getRegisters().getHL() & 0xffff));

            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            z80.getRegisters().setA(((MsxMemory) z80.getMemory()).readSlotMemoryAdr(
                    slot & 0x03,
                    (slot & 0x0c) >> 2,
                    z80.getRegisters().getHL() & 0xffff));
            z80.executeRet();
        } else if (z80.getRegisters().getPC() == 0x0014) {
            logger.log(Level.TRACE, "Call WRSLT(0x0014) Reg.A=%02x Reg.HL=%04x Reg.E=%02x".formatted(z80.getRegisters().getA(), z80.getRegisters().getHL(), z80.getRegisters().getE()));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x001c) {
            logger.log(Level.TRACE, "Call CALSLT(0x001c) Reg.IY=%04x Reg.IX=%04x".formatted(z80.getRegisters().getIY(), z80.getRegisters().getIX()));
            throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x0024) {
            //logger.log(Level.TRACE, "\r\nCall ENASLT(0x0024) Reg.A=%02x Reg.HL=%04x".formatted(z80.getRegisters().getA() & 0xff, z80.getRegisters().getHL() & 0xffff));
            int slot = z80.getRegisters().getA() & ((z80.getRegisters().getA() & 0x80) != 0 ? 0xf : 0x3);
            ((MsxMemory) z80.getMemory()).changePage(
                    slot & 0x03,
                    (slot & 0x0c) >> 2,
                    (z80.getRegisters().getH() & 0xc0) >> 6);
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
        } else if (z80.getRegisters().getPC() == 0x0138) {
            logger.log(Level.TRACE, "Call RSLREG(0138H/MAIN)");
            z80.getRegisters().setA((byte) (0x80 | 0x03 | 0x04));
        } else if (z80.getRegisters().getPC() == 0x013B) {
            logger.log(Level.TRACE, "Call WSLREG(013BH/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x015C) {
            logger.log(Level.TRACE, "Call SUBROM(015CH/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x015f) {
            logger.log(Level.TRACE, "Call EXTROM(015FH/MAIN)");
            //throw new UnsupportedOperationException();
        } else if (z80.getRegisters().getPC() == 0x4601) {
            logger.log(Level.TRACE, "JP NEWSTT(0x4601) Reg.HL=%04x".formatted(z80.getRegisters().getHL() & 0xffff));
            String msg = getAsciiz(z80, z80.getRegisters().getHL());
            logger.log(Level.TRACE, "(HL)=%s".formatted(msg));
            if (msg.equals(":_SYSTEM")) {
                args.getExecutionStopper().stop(false);
            }
        } else if ((z80.getRegisters().getPC() & 0xffff) >= mapper.jumpAddress && (z80.getRegisters().getPC() & 0xffff) < mapper.jumpAddress + 16) {
            //logger.log(Level.TRACE, "\r\nCall MAPPER PROC(0x%04x～) PC-%04x:%04x".formatted(mapper.JumpAddress, (z80.getRegisters().getPC() & 0xffff) - mapper.JumpAddress));
            mapper.callMapperProc(args, z80, (z80.getRegisters().getPC() & 0xffff) - mapper.jumpAddress);
        } else if ((z80.getRegisters().getPC() & 0xffff) == 0xffca) {
            //logger.log(Level.TRACE, "\r\nCall EXTBIO(0xffca) Reg.DE=%04x".formatted(z80.getRegisters().getDE()));
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

    private void callEXTBIO(BeforeInstructionFetchEvent args, Z80Processor z80) {
        byte funcType = z80.getRegisters().getD();
        byte function = z80.getRegisters().getC();

        switch (funcType & 0xff) {
            case 0x04:
                //logger.log(Level.TRACE, " EXTBIO MemoryMapper");
                EXTBIO_MemoryMapper(args, function);
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

    private void EXTBIO_MemoryMapper(BeforeInstructionFetchEvent args, byte function) {
        Z80Processor z80 = (Z80Processor) args.getSource();

        switch (function) {
            case 0x02:
                z80.getRegisters().setA((byte) 0);
                z80.getRegisters().setBC((short) 0);
                z80.getRegisters().setHL((short) mapper.tableAddress);
                break;
        }
    }

    private void callBIOS(BeforeInstructionFetchEvent args) {
        Z80Processor z80 = (Z80Processor) args.getSource();

        byte function = z80.getRegisters().getC();
        byte byteToPrint;
        String msg;
        switch (function & 0xff) {
            case 2:
                byteToPrint = z80.getRegisters().getE();
                System.out.print((char) byteToPrint);
                break;
            case 9:
                var messageAddress = z80.getRegisters().getDE();
                var bytesToPrint = new ArrayList<Byte>();
                while ((byteToPrint = z80.getMemory().get(messageAddress & 0xffff)) != DollarCode) {
                    bytesToPrint.add(byteToPrint);
                    messageAddress++;
                }
                var StringToPrint = new String(ByteUtil.toByteArray(bytesToPrint));
                conWrite(StringToPrint);
                break;
            case 0x0f:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "File Open FCB Address:%04x".formatted(z80.getRegisters().getDE() & 0xffff));
                z80.getRegisters().setA((byte) 0x00); // success
                FCBAddress = z80.getRegisters().getDE();
                break;
            case 0x10:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "File Close FCB Address:%04x".formatted(z80.getRegisters().getDE() & 0xffff));
                z80.getRegisters().setA((byte) 0x00); // success
                break;
            case 0x16:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "File Create FCB Address:%04x".formatted(z80.getRegisters().getDE() & 0xffff));
                z80.getRegisters().setA((byte) 0x00); // success
                FCBAddress = z80.getRegisters().getDE();
                break;
            case 0x1a:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "DTA Address:%04x".formatted(z80.getRegisters().getDE() & 0xffff));
                DTAAddress = z80.getRegisters().getDE();
                break;
            case 0x26:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "Write random block: FCB Adr(DE):%04x Write Record(HL):%04x".formatted(z80.getRegisters().getDE() & 0xffff, z80.getRegisters().getHL() & 0xffff));
                z80.getRegisters().setA((byte) 0x00); // success
                FCBAddress = z80.getRegisters().getDE();
                var dummy1 = (short) ((z80.getMemory().get((FCBAddress & 0xffff) + 12) & 0xff) + (z80.getMemory().get((FCBAddress & 0xffff) + 13) & 0xff) * 0x100); // currentBlock
                short recordSize = (short) ((z80.getMemory().get((FCBAddress & 0xffff) + 14) & 0xff) + (z80.getMemory().get((FCBAddress & 0xffff) + 15) & 0xff) * 0x100);
                var dummy2 = (short) ((z80.getMemory().get((FCBAddress & 0xffff) + 16) & 0xff) +
                        (z80.getMemory().get((FCBAddress & 0xffff) + 17) & 0xff) * 0x100 +
                        (z80.getMemory().get((FCBAddress & 0xffff) + 18) & 0xff) * 0x1_0000 +
                        (z80.getMemory().get((FCBAddress & 0xffff) + 19) & 0xff) * 0x100_0000
                ); // fileSize
                bgmBin = z80.getMemory().getContents(DTAAddress & 0xffff, (z80.getRegisters().getHL() & 0xffff) * (recordSize & 0xffff));
                break;
            case 0x27:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "Read random block: FCB Adr(DE):%04x Read Record(HL):%04x".formatted(z80.getRegisters().getDE() & 0xffff, z80.getRegisters().getHL() & 0xffff));
                readRandomBlock(z80);
                break;
            case 0x62:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.TRACE, "_TERM ErrorCode:%02x".formatted(z80.getRegisters().getB() & 0xff));
                args.getExecutionStopper().stop(false);
                return;
            case 0x6b:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                //_GENV
                //logger.log(Level.TRACE, "_GENV HL:%04x DE:%04x B:%02x".formatted(z80.getRegisters().getHL() & 0xffff, z80.getRegisters().getDE() & 0xffff, z80.getRegisters().getB() & 0xff));
                msg = getAsciiz(z80, z80.getRegisters().getHL());
                //logger.log(Level.TRACE, "(HL)=%s".formatted(msg));

                if (msg.equals("PARAMETERS")) {
                    byte[] option = "/z".getBytes(StandardCharsets.US_ASCII);
                    for (int i = 0; i < option.length; i++)
                        z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i, option[i]);
                    z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + option.length, (byte) 0);
                } else if (msg.equals("SHELL")) {
                    //byte[] option = Encoding.ASCII.GetBytes("c:\\dummy");
                    //for (int i = 0; i < option.Length; i++) z80.getMemory().set((z80.getRegisters().getDE() & 0xffff) + i) & 0xffff, option[i]);
                    //z80.getMemory().set(((z80.getRegisters().getDE() & 0xffff) + option.length) & 0xffff, (byte) 0);
                    z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
                } else {
                    z80.getMemory().set(z80.getRegisters().getDE() & 0xffff, (byte) 0);
                }

                z80.getRegisters().setA((byte) 0x00); // Error number
                z80.getRegisters().setDE((short) 0x00); // value
                break;
            case 0x6c:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x", z80.getRegisters().getC() & 0xff);
                //_SENV
                //logger.log(Level.TRACE, "_SENV HL:%04x DE:%04x".formatted(z80.getRegisters().getHL() & 0xffff, z80.getRegisters().getDE() & 0xffff));
                //msg = getAsciiz(z80, (short) z80.getRegisters().getHL() & 0xffff);
                //logger.log(Level.TRACE, "(HL)=%s".formatted(msg));
                //msg = getAsciiz(z80, (short) z80.getRegisters().getDE() & 0xffff);
                //logger.log(Level.TRACE, "(DE)=%s".formatted(msg));

                z80.getRegisters().setA((byte) 0x00); // Error number
                break;
            case 0x6f:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x", z80.getRegisters().getC() & 0xff);
                //_DOSVER
                z80.getRegisters().setBC((short) 0x0231); // ROM version
                z80.getRegisters().setDE((short) 0x0210); // DISK version
                //logger.log(Level.TRACE, "_DOSVER ret BC(ROMVer):%04x DE(DISKVer):%04x".formatted(z80.getRegisters().getBC(), z80.getRegisters().getDE()));
                break;
            default:
                logger.log(Level.TRACE, "Call BDOS(0x0005) Reg.C=%02x".formatted(z80.getRegisters().getC() & 0xff));
                logger.log(Level.WARNING, "unknown 0x%02x".formatted(function));
                debugRegisters(z80);
                break;
        }

        z80.executeRet();
    }

    private void readRandomBlock(Z80Processor z80) {
        short recordSize = (short) ((z80.getMemory().get((FCBAddress & 0xffff) + 14) & 0xff) + (z80.getMemory().get((FCBAddress & 0xffff) + 15) & 0xff) * 0x100);
        short randomRecord = (short) ((z80.getMemory().get((FCBAddress & 0xffff) + 33) & 0xff) +
                (z80.getMemory().get((FCBAddress & 0xffff) + 34) & 0xff) * 0x100 +
                (z80.getMemory().get((FCBAddress & 0xffff) + 35) & 0xff) * 0x1_0000 +
                ((recordSize & 0xffff) < 64 ? ((z80.getMemory().get((FCBAddress & 0xffff) + 36) & 0xff) * 0x100_0000) : 0)
        );

        if (DTAAddress == 0x4000) {
            if (msdBin == null) {
                z80.getRegisters().setA((byte) 0xff); // fail
                z80.getRegisters().setHL((short) 0x00); // The number of records that were read
            } else {
                z80.getMemory().setContents(DTAAddress & 0xffff, msdBin, (recordSize & 0xffff) * (randomRecord & 0xffff), msdBin.length - (recordSize & 0xffff) * (randomRecord & 0xffff));
                z80.getRegisters().setA((byte) 0x00); // success
                z80.getRegisters().setHL((short) 0x00); // The number of records that were read
            }
        } else if ((DTAAddress & 0xffff) == 0x8000) {
            if (vcdBin == null) {
                z80.getRegisters().setA((byte) 0xff); // fail
                z80.getRegisters().setHL((short) 0x00); // The number of records that were read
            } else {
                z80.getMemory().setContents(DTAAddress & 0xffff, vcdBin, (recordSize & 0xffff) * (randomRecord & 0xffff), vcdBin.length - (recordSize & 0xffff) * (randomRecord & 0xffff));
                z80.getRegisters().setA((byte) 0x00); // success
                z80.getRegisters().setHL((short) 0x00); // The number of records that were read
            }
        }
    }

    private static void conWrite(String v) {
        for (byte c : v.getBytes()) {
            if (c == (byte) '\n') {
                conFlash();
                continue;
            }
            consoleBuf.add(c);
        }
    }

    private static void conFlash() {
        if (consoleBuf.size() <= 0) return;
        String msg = new String(ByteUtil.toByteArray(consoleBuf));

        System.out.printf("[MuSICA]%s", msg);
        consoleBuf.clear();
    }

    private static String getAsciiz(Z80Processor z80, short reg) {
        var messageAddress = reg & 0xffff;
        var bytesToPrint = new ArrayList<Byte>();
        byte byteToPrint;
        while ((byteToPrint = z80.getMemory().get(messageAddress & 0xffff)) != 0) {
            bytesToPrint.add(byteToPrint);
            messageAddress++;
        }
        return new String(ByteUtil.toByteArray(bytesToPrint));
    }

    public byte[] getBgmBin() {
        return bgmBin;
    }
}
