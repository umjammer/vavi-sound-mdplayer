package mdplayer.emu.nise68;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.System.getLogger;


public class Nise68 {

    private static final Logger logger = getLogger(Nise68.class.getName());

    /**
     * The step loops below run once per emulated instruction and {@link #trap} once per frame,
     * so the log levels are resolved once and folded away by the JIT while logging is off.
     */
    private static final boolean TRACE = logger.isLoggable(Level.TRACE);

    private static final boolean DEBUG = logger.isLoggable(Level.DEBUG);

    public NiseHuman hmn = null;
    public Memory68 mem = null;
    public Register68 reg = null;
    private NiseM68 cpu = null;
    private NiseIOCS iocs = null;
    private MidiBoard[] midiBoard = null;
    private FileMng fileMng = null;
    private BiFunction<Integer, Byte, Integer> scc;
    private SccA scc_A = null;

    private int step = 0;
    private int run = 0;
    private int cmd = 0;
    private int ocmd = -1;

    private static final int IOCSCallAddress = 0xfe_0000;
    private byte hkVsyncVal = 0;
    private byte hkOPMAdr;
    private byte hkOPMDat;

    public Function<Integer, Integer> mpcm = null;
    public BiFunction<Integer, Integer, Integer> opm = null;
    public BiFunction<Integer, Byte, Integer> midi = null;

    public void init(List<String> envZPDs, boolean isVer2, FileMng fm, Charset charset) {
        mem = new Memory68(16 * 1024 * 1024);
        reg = new Register68();
        hmn = new NiseHuman(mem, reg, envZPDs, fm);
        hmn.charset = charset;
        fileMng = fm;
        cpu = new NiseM68(mem, reg);
        cpu.hmn = hmn;
        iocs = new NiseIOCS(mem, reg);

        // IOCS address
        mem.pokeL(0xbc, IOCSCallAddress);
        mem.pokeL(0x238, IOCSCallAddress);

        mem.pokeL(0x228, IOCSCallAddress);
        mem.pokeL(0x230, IOCSCallAddress);
        mem.pokeL(0x2a8, IOCSCallAddress);
        mem.pokeL(0x2b0, IOCSCallAddress);

        if (isVer2) {
            // for ZMUSIC v2
            mem.pokeL(0x10c, 0xfe_0004);
            mem.pokeL(0x7c0, 0xfe_0004);
            mem.pokeL(0x1a8, 0xfe_0004);
            mem.pokeL(0x088, NiseHuman.mpcmPtr);
            mem.pokeL(NiseHuman.mpcmPtr - 0x08, 0x5043_4d00);
        } else {
            // MPCM related
            mem.pokeL(0x84, NiseHuman.mpcmPtr); // Does it say where the traps are located?
            mem.pokeL(NiseHuman.mpcmPtr - 0x8, 0x4d50_434d); // 'MPCM'
        }

        mem.hookList.add(new MemHook(0xe8_8001, 0xe8_8001, this::hkVsync, null)); // vsync?
        mem.hookList.add(new MemHook(0xe9_0001, 0xe9_0003, Nise68::hkOPMr, this::hkOPMw)); // opm
        mem.hookList.add(new MemHook(0xea_fa00, 0xea_fa1f, this::hkCZ6BM1fr, this::hkCZ6BM1fw)); // 1st/2nd CZ-6BM1(MIDI)
        //mem.hookList.add(new memhook(0xe9a001, 0xe9a001, hkDummy, null)); // for midiwait
        mem.hookList.add(new MemHook(0xe9_8005, 0xe9_8007, this::hkSCC_Ar, this::hkSCC_Aw)); // SCC(Serial Communication Controller) ChA(RS-232C)
        mem.refreshHookBounds();

        step = 0;
        run = 0;
    }

    public int loadRun(String filename, String option, int startAddress,
                       boolean dispReg /* = false */,
                       boolean useStepCounter /* = false */,
                       boolean dispStepCounter /* = false */,
                       long MaxStepCounter /* = 100_000_000 */,
                       long StartStepCounterForDispStep /* = 0 */) {
        hmn.loadAndExecuteFile(filename, option, startAddress);
        if (dispReg) dispRegs(reg);

        int waitClock = 0;
        hmn.programTerminate = false;
        step = 0;
        run++;

        while (((useStepCounter && step < MaxStepCounter) || !useStepCounter) && !hmn.programTerminate) {
            waitClock += stepExecute();

            if (useStepCounter) {
                step++;
                if (step < StartStepCounterForDispStep) continue;
            }

//#if DEBUG
            if (TRACE) {
                if (dispReg) {
                    dispRegs(reg);
                }

                //if (dispStepCounter) logger.log(Level.TRACE, "STEP:%s totalCycle:%s\r\n", step, waitClock);

                if (run > 0 && step == 500) {
                    //logger.log(Level.TRACE, "");
                }

                if (reg.pc == 0x0002_2968) {
                }
                if (reg.pc == 0x000_0002_2982) { // Comments loaded
                }
                //if (reg.pc == 0x000_0002_22e2) { // Processing commands beginning with '('
                //}

                //if ((reg.pc & 0xffff_fff0) == reg.pc) {
                //    dumpMemory(reg.pc - 0x80, reg.pc + 0x80);
                //}
            }
//#endif
        }

        logger.log(Level.DEBUG, "Terminate program. return code=$%02x".formatted(hmn.returnCode));
        logger.log(Level.DEBUG, "  alloc count =%d".formatted(hmn.memMng.getAllocCount()));
        logger.log(Level.DEBUG, "");

        return hmn.returnCode;
    }

    public void trap(int num) {
        trap(num, false, false, false, 100_000_000, 0);
    }

    public void trap(int num, boolean dispReg /* = false */,
                     boolean useStepCounter /* = false */,
                     boolean dispStepCounter /* = false */,
                     long MaxStepCounter /* = 100_000_000 */,
                     long StartStepCounterForDispStep /* = 0 */) {
        if (dispReg) dispRegs(reg);

        int waitClock = 0;
        hmn.programTerminate = false;
        step = 0;
        run++;

        reg.setSSP(NiseHuman.defSSP);
        reg.setUSP(NiseHuman.defUSP);
        cpu.ctrap2((short) num);

        while (((useStepCounter && step < MaxStepCounter) || !useStepCounter) && !hmn.programTerminate) {
            waitClock += stepExecute();

            if (useStepCounter) {
                step++;
                if (step < StartStepCounterForDispStep) continue;
            }

//#if DEBUG
            if (TRACE) {
                if (dispReg) {
                    dispRegs(reg);
                }

                if (dispStepCounter) logger.log(Level.TRACE, "STEP:%s totalCycle:%s", step, waitClock);

                if (run > 8 && step == 146) {
                    //logger.log(Level.TRACE, "");
                }

                //if (run > 0 && step == 249) {
                //    logger.log(Level.TRACE, "");
                //}

                if (run > 0 && (reg.pc == 0x0002_e9fe)) {
                }

                //// For command-by-command debugging
                //if (run > 0 && reg.pc == 0x0003_07ba) { // D7 -> Command number
                //    cmd++;
                //}
                //if (cmd >= 14 && cmd != ocmd) { // D7 -> Command number
                //    ;
                //    ocmd = cmd;
                //}
            }
//#endif
        }

//#if DEBUG
        if (DEBUG) logger.log(Level.DEBUG, "Terminate program. return code=$%02x runs=%s".formatted(hmn.returnCode, run));
        // logger.log(Level.DEBUG, "  alloc count ={0:d}", hmn.memMng.allocCount);
        // logger.log(Level.DEBUG, "");
//#endif
    }

    public void trapOPM() {
        trapOPM(false, false, false, 100_000_000, 0);
    }

    public void trapOPM(boolean dispReg /* = false */,
                        boolean useStepCounter /* = false */,
                        boolean dispStepCounter /* = false */,
                        long MaxStepCounter /* = 100_000_000 */,
                        long StartStepCounterForDispStep /* = 0 */) {
        if (dispReg) dispRegs(reg);

        int waitClock = 0;
        hmn.programTerminate = false;
        step = 0;
        run++;

        reg.setSSP(NiseHuman.defSSP);
        reg.setUSP(NiseHuman.defUSP);
        cpu.ctrapPtr(iocs.interruptOPM);

        while (((useStepCounter && step < MaxStepCounter) || !useStepCounter) && !hmn.programTerminate) {
            waitClock += stepExecute();

            if (useStepCounter) {
                step++;
                if (step < StartStepCounterForDispStep) continue;
            }

//#if DEBUG
            if (TRACE) {
                if (dispReg) {
                    dispRegs(reg);
                }

                if (dispStepCounter) logger.log(Level.TRACE, "STEP:%s totalCycle:%s\r\n", step, waitClock);

                //if (run > 3082 && step == 1827) {
                //    //logger.log(Level.TRACE, "");
                //}

                //if (run > 0 && step == 249) {
                //    //logger.log(Level.TRACE, "");
                //}

                //if (run > 0 && (reg.PC == 0x0002_f350)) {
                //    ;
                //}

                //// For command-by-command debugging
                // if (run > 0 && reg.PC == 0x0003_07ba) { D7 -> Command number
                //    cmd++;
                //}
                //if (cmd >= 14 && cmd != ocmd) { D7 -> Command number
                //    ;
                //    ocmd = cmd;
                //}
            }
//#endif
        }

//#if DEBUG
        if (DEBUG) logger.log(Level.DEBUG, "Terminate program. return code=$%02x runs=%s".formatted(hmn.returnCode, run));
        //logger.log(Level.DEBUG, "  alloc count =%d".formatted(hmn.memMng.allocCount));
        //logger.log(Level.DEBUG, "");
//#endif
    }

    public void dumpMemory(int startAddress, int endAddress) {
        String str = "$%08x: %02x %02x %02x %02x : %02x %02x %02x %02x : %02x %02x %02x %02x : %02x %02x %02x %02x";
        startAddress &= 0xffff_fff0;
        endAddress = (endAddress & 0xffff_fff0) + 0x10;
        for (int i = startAddress; i < endAddress; i += 16) {
            logger.log(Level.TRACE, str.formatted(
                    i,
                    mem.peekB(i) & 0xff, mem.peekB(i + 1) & 0xff, mem.peekB(i + 2) & 0xff, mem.peekB(i + 3) & 0xff,
                    mem.peekB(i + 4) & 0xff, mem.peekB(i + 5) & 0xff, mem.peekB(i + 6) & 0xff, mem.peekB(i + 7) & 0xff,
                    mem.peekB(i + 8) & 0xff, mem.peekB(i + 9) & 0xff, mem.peekB(i + 10) & 0xff, mem.peekB(i + 11) & 0xff,
                    mem.peekB(i + 12) & 0xff, mem.peekB(i + 13) & 0xff, mem.peekB(i + 14) & 0xff, mem.peekB(i + 15) & 0xff
            ));
        }
    }

    private int stepExecute() {
        if (reg.pc == 0) {
            hmn.programTerminate = true;
            //logger.log(Level.INFO, "PC is Zero. program terminate.");
            return 0;
        } else if (reg.pc == IOCSCallAddress) {
            iocs.call();
            return 0;
        } else if (reg.pc == NiseHuman.mpcmPtr) {
            mPcm();
            return 0;
        }

        int waitClock = cpu.stepExecute();
        return waitClock;
    }

    private void mPcm() {
        logger.log(Level.TRACE, "Call MPCM function ");

        int n = reg.getDw(0) & 0xffff;

        int ret = mpcm.apply(n);
        reg.setDl(0, ret);

        reg.setSR(mem.peekW(reg.getSSP()));
        reg.setSSP(reg.getSSP() + 2);
        reg.pc = mem.peekL(reg.getSSP());
        reg.setSSP(reg.getSSP() + 4);
    }

    private static void dispRegs(Register68 regs) {
        logger.log(Level.TRACE, regs);
    }

    private int hkVsync(int ptr) {
        hkVsyncVal ^= (byte) 0x90;
        return hkVsyncVal & 0xff;
    }

    private static int hkOPMr(int ptr) {
        if (ptr == 0x00e9_0003) {
            return 0;
        }

//#if DEBUG
//        throw new UnsupportedOperationException();
//#else
        return 0;
//#endif
    }

    private boolean hkOPMw(int ptr, byte dat) {
        if (ptr == 0x00e9_0001) {
            hkOPMAdr = dat;
            return false;
        } else if (ptr == 0x00e9_0003) {
            hkOPMDat = dat;
            writeOpm(hkOPMAdr, hkOPMDat);
            return false;
        }

//#if DEBUG
//        throw new UnsupportedOperationException();
//#else
        return false;
//#endif
    }

    private int hkCZ6BM1fr(int ptr) {
        int n = (ptr & 0x10) != 0 ? 1 : 0;
        return midiBoard[n].read((byte) ptr) & 0xff;
    }

    private boolean hkCZ6BM1fw(int ptr, byte dat) {
        int n = (ptr & 0x10) != 0 ? 1 : 0;
        return midiBoard[n].write((byte) ptr, dat);
    }

    private int hkSCC_Ar(int ptr) {
        return scc_A.read(ptr);
    }

    private boolean hkSCC_Aw(int ptr, byte dat) {
        return scc_A.write(ptr, dat);
    }

    private void writeOpm(byte hkOPMAdr, byte hkOPMDat) {
        logger.log(Level.TRACE, "Write OPM Adr:$%02x Dat:$%02x".formatted(hkOPMAdr & 0xff, hkOPMDat & 0xff));
        opm.apply(hkOPMAdr & 0xff, hkOPMDat & 0xff);
    }

    public boolean intTimer() {
        return midiBoard[0].intTimer();
    }

    public void setMPcm(Function<Integer, Integer> mpcm) {
        this.mpcm = mpcm;
    }

    public void setOpm(BiFunction<Integer, Integer, Integer> opm) {
        this.opm = opm;
    }

    public void setMidi(BiFunction<Integer, Byte, Integer> midi, int renderingFreq) {
        this.midi = midi;
        midiBoard = new MidiBoard[] {
                new MidiBoard(0, renderingFreq, midi), new MidiBoard(1, renderingFreq, midi)
        };
    }

    public void setSCC_A(BiFunction<Integer, Byte, Integer> scc, int renderingFreq) {
        this.scc = scc;
        scc_A = new SccA(renderingFreq, this.scc);
    }
}
