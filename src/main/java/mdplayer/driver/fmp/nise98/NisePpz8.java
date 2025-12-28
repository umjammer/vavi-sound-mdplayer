package mdplayer.driver.fmp.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.TriConsumer;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


public class NisePpz8 {

    private static final Logger logger = getLogger(NisePpz8.class.getName());

    private final Register286 regs;
    private final Memory98 mem;
    private final NiseDos dos;
    private final Nise286 cpu;
    private final Nise98 nise98;

    private static final byte ppz8Int = 0x7f;
    private static final short ppz8EntryAddressSeg = 0x4000;
    private static final short ppz8EntryAddressOfs = 0x0000;

    private static final short ppz8IDOfs = 0x0005;
    private static final short ppz8VerOfs = 0x000a;

    private static final short ppz8FIFOAddressOfs = 0x0F00; // Offset to FIFO processing
    private static final short ppz8ReleaseOfs = 0x1000;
    private static final short ppz8ReleaseMessageOfs = 0x1001;

//    private int temporarySeg;
//    private int temporarySize;
    private byte emuADPCM;
    private TriConsumer<Integer, Integer, byte[][]> setPPZ8PCMData;
    private TriConsumer<Integer, Integer, Integer> setPPZ8Data;
    private final byte[][] pcmData = new byte[2][];

    public NisePpz8(Nise98 nise98) {
        this.regs = nise98.getRegisters();
        this.mem = nise98.getMem();
        this.dos = nise98.getDos();
        this.cpu = nise98.getCPU();
        this.nise98 = nise98;

        mem.pokeW(ppz8Int * 4 + 0, ppz8EntryAddressOfs); // ofs
        mem.pokeW(ppz8Int * 4 + 2, ppz8EntryAddressSeg); // seg

        // ID
        int ptr = (ppz8EntryAddressSeg << 4) + ppz8IDOfs;
        mem.pokeB(ptr + 0x00, (byte) 'P');
        mem.pokeB(ptr + 0x01, (byte) 'P');
        mem.pokeB(ptr + 0x02, (byte) 'Z');
        mem.pokeB(ptr + 0x03, (byte) '8');
        mem.pokeB(ptr + 0x04, (byte) 0);

        // Version
        ptr = (ppz8EntryAddressSeg << 4) + ppz8VerOfs;
        mem.pokeB(ptr + 0x00, (byte) '1');
        mem.pokeB(ptr + 0x01, (byte) '.');
        mem.pokeB(ptr + 0x02, (byte) '0');
        mem.pokeB(ptr + 0x03, (byte) '7');

        // Residency release message
        byte[] bmsg = "The PPZ8 has been disabled as resident.\r\n$".getBytes(charset);
        ptr = (ppz8EntryAddressSeg << 4) + ppz8ReleaseMessageOfs;
        for (byte ch : bmsg) {
            mem.pokeB(ptr, ch);
            ptr++;
        }

        dos.setHookINT(ppz8Int, this::int7F);
        cpu.setHook(this::hook);
    }

    public void fmpRegisterPPZ8(/* out */ int[] step, /* out */ Register286[] regs) {
        regs[0] = nise98.getRegisters();
        regs[0].setAX((short) 0x0010);
        logger.log(Level.DEBUG, "FMPRegistPPZ8");
        step[0] = 0;
        regs[0].setDS(ppz8EntryAddressSeg); // 'PPZ8''s seg
        regs[0].setSI(ppz8IDOfs); // 'PPZ8''s ofs
        regs[0].setDX(ppz8ReleaseOfs); // Far call when resident is released
        regs[0].setCL((byte) 0x00); // TASK_ASIN
        nise98.callRunFunctionCall((byte) 0xd2, true, true, true, 10_000_000_000L, 0_000);

        logger.log(Level.INFO, "set the fake PPZ8 to the FMP task.");
    }

    public void int7F() {
        switch (regs.getAH()) {
            case 0x00: // Initialization
                // Work initialization
                setPPZ8Data.accept(0, 0, 0);
                setPPZ8Data.accept(10, 0, 1);
                for (int i = 0; i < 8; i++) {
                    setPPZ8Data.accept(21, i, 16000);
//                    setPPZ8Data.accept(0x0e + i * 0x100, 0xffff, 0xffff);
                }
                break;
            case 0x01: // KEY ON PCM
                setPPZ8Data.accept(1, regs.getAL() & 0xff, regs.getDX() & 0xffff);
                break;
            case 0x02: // KEY OFF PCM
                setPPZ8Data.accept(2, regs.getAL() & 0xff, 0);
                break;
            case 0x03:
                List<Byte> lstFN = new ArrayList<>();
                int ptr = regs.getDS_DX();
                do {
                    byte c = mem.peekB(ptr++);
                    if (c == 0) break;
                    lstFN.add(c);
                } while (true);
                String fn = new String(ByteUtil.toByteArray(lstFN), charset);
                boolean refEnv = regs.getAL() == 0;
                int pcmBufNum = regs.getCL() & 0xff;
                boolean pcmIsPVI = regs.getCH() == 0;
                pcmData[pcmBufNum] = dos.loadData(fn);
                setPPZ8PCMData.accept(pcmBufNum, pcmIsPVI ? 0 : 1, pcmData);
                regs.setCF(false);
                break;
            case 0x04:
                switch (regs.getAL()) {
                    case 0x09:
                        regs.setES(ppz8EntryAddressSeg);
                        regs.setBX(ppz8FIFOAddressOfs);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                setPPZ8Data.accept(4, regs.getAL() & 0xff, 0);
                break;
            case 0x07: // change volume
                setPPZ8Data.accept(7, regs.getAL() & 0xff, Math.min(regs.getDX() & 0xffff, 15));// / (emuADPCM != 0 ? 16 : 1));
                break;
            case 0x0a: // ADPCM volume adjust
                setPPZ8Data.accept(10, 0, regs.getDX() & 0xffff);
                break;
            case 0x0b: // change PCM FNUM
                setPPZ8Data.accept(11, regs.getAL() & 0xff, ((regs.getDX() & 0xffff) << 16) + (regs.getCX() & 0xffff));
                break;
            case 0x0e: // set loop point
                setPPZ8Data.accept(14 + regs.getAL() * 0x100, ((regs.getDX() & 0xffff) << 16) + (regs.getCX() & 0xffff), ((regs.getDI() & 0xffff) << 16) + (regs.getSI() & 0xffff));
                break;
            case 0x12: // Disable interrupt
                break;
            case 0x13: // change pan
                setPPZ8Data.accept(19, regs.getAL() & 0xff, regs.getDX() & 0xffff);
                break;
            case 0x14: // Playback Rate Settings
                break;
            case 0x15: // Original data frequency setting
                setPPZ8Data.accept(21, regs.getAL() & 0xff, regs.getDX() & 0xffff);
                break;
            case 0x16:
                setPPZ8Data.accept(22, 0, regs.getAL() & 0xff);
                break;
            case 0x17:
//                temporarySeg = (short) regs.ES;
//                temporarySize = (short) regs.DX;
                break;
            case 0x18:
                setPPZ8Data.accept(24, regs.getAL() & 0xff, 0);
                emuADPCM = regs.getAL();
                break;
            case 0x19: // Resident disable permission/prohibition setting
                break;
            case 0x1a: // Changing the FIFO buffer
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public boolean hook() {
        if (regs.getCS() != ppz8EntryAddressSeg) return false;

        boolean cancel = false;
        switch (regs.ip) {
            case ppz8ReleaseOfs:
                regs.setDX(ppz8ReleaseMessageOfs);
                cancel = true;
                break;
            case ppz8FIFOAddressOfs:
                // FIFO Processing
                // The original uses EMS/XMS data transfer processing.
                // TBD
                cancel = true;
                break;
        }

        if (cancel) {
            regs.ip = mem.peekW(regs.getSS_SP());
            regs.addSP(2);
            regs.setCS(mem.peekW(regs.getSS_SP()));
            regs.addSP(2);
            return true;
        }
        logger.log(Level.ERROR, "[NisePPZ8]An unknown address is referenced.ip:%04x".formatted(regs.ip & 0xffff));
        return false;
    }

    public void setCallBack(TriConsumer<Integer, Integer, byte[][]> setPPZ8PCMData, TriConsumer<Integer, Integer, Integer> setPPZ8Data) {
        this.setPPZ8PCMData = setPPZ8PCMData;
        this.setPPZ8Data = setPPZ8Data;
    }
}
