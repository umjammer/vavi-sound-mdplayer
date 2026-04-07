package mdplayer.driver.fmp.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.function.Function;

import dotnet4j.util.compat.TriConsumer;
import mdplayer.driver.mndrv.FMTimer;

import static java.lang.System.getLogger;


public class Nise98 {

    private static final Logger logger = getLogger(Nise98.class.getName());

    private Function<String, Object[]> msgWrite = null;
    private TriConsumer<Integer, Integer, Integer> opnaWrite;
    private Register286 regs = null;
    private Memory98 mem = null;
    private Nise286 cpu = null;
    private NiseInt08Timer int08Timer = null;
    private NiseDos dos = null;
    private NisePpz8 ppz8 = null;
    private FileTemp fileTemp = null;

    private FmStatus fmReg088 = null;
    private FmStatus fmReg188 = null;
    private FmStatus fmReg288 = null;
    private FmStatus fmReg388 = null;
    private int vSyncCnt = 2;
    private byte vSync = 0;
    private short mojiCode;
    private byte lineCount;
    private byte mojiPattern;
    private byte pA460h;
    private byte mute86Pcm = 0;

    private int step = 0;
    private int functionCallTimes = 0;

    public enum OngenBoardType {
        None,
        PC9801_26K,
        SpeakBoard,
        PC9801_86B
    }

    public static class FmStatus {

        // bit76:11 int 5(IRQ12)(factory)
        // bit76:10 int 4(IRQ10)
        // bit76:01 int 6(IRQ13)
        // bit76:00 int 0(IRQ03)
        public static final byte int_ = (byte) 0b1100_0000;
        public byte p88lastAdr = 0;
        public byte p8clastAdr = 0;
        public boolean isBusy = false;
        public boolean isTimerBOverFlow = true;
        public boolean isTimerAOverFlow = false;
        public final byte[] regs;
        public byte[] adpcmMem;
        public byte adpcmPtr = 0;
        public boolean adpcmReadMode = false;
        public FMTimer timer = null;

        // ongenBoardType
        public OngenBoardType ongen = OngenBoardType.SpeakBoard;

        public FmStatus(OngenBoardType ongen) {
            this.ongen = ongen;
            if (ongen == OngenBoardType.PC9801_26K) {
                regs = new byte[256 * 1];
            } else if (ongen == OngenBoardType.PC9801_86B) {
                regs = new byte[256 * 2];
                adpcmMem = null;
            } else if (ongen == OngenBoardType.SpeakBoard) {
                regs = new byte[256 * 2];
                adpcmMem = new byte[256];
            } else {
                regs = null;
                adpcmMem = null;
            }

            timer = new FMTimer(false, null, 7987200); // OPNATimer(55467, 7987200);
        }
    }

    public void init(Function<String, Object[]> msgWrite, TriConsumer<Integer, Integer, Integer> opnaWrite, FileTemp fileTemp, OngenBoardType ongen /* = enmOngenBoardType.PC9801_86B */) {
        logger.log(Level.TRACE, "<Nise98>Init");

        this.opnaWrite = opnaWrite;
        this.fileTemp = fileTemp;
        regs = new Register286();
        mem = new Memory98(16 * 64 * 1024);
        dos = new NiseDos(regs, mem, fileTemp);
        cpu = new Nise286(this);
        int08Timer = new NiseInt08Timer(cpu, 8);
        ppz8 = new NisePpz8(this);

        if (ongen == OngenBoardType.None) {
            fmReg088 = new FmStatus(OngenBoardType.None);
            fmReg188 = new FmStatus(OngenBoardType.None);
            fmReg288 = new FmStatus(OngenBoardType.None);
            fmReg388 = new FmStatus(OngenBoardType.None);
            pA460h = (byte) 0xfc;
        } else if (ongen == OngenBoardType.PC9801_26K) {
            fmReg088 = new FmStatus(OngenBoardType.None);
            fmReg188 = new FmStatus(OngenBoardType.PC9801_26K);
            fmReg288 = new FmStatus(OngenBoardType.None);
            fmReg388 = new FmStatus(OngenBoardType.None);
            pA460h = (byte) 0xfc;
        } else if (ongen == OngenBoardType.PC9801_86B) {
            fmReg088 = new FmStatus(OngenBoardType.None);
            fmReg188 = new FmStatus(OngenBoardType.PC9801_86B);
            fmReg288 = new FmStatus(OngenBoardType.None);
            fmReg388 = new FmStatus(OngenBoardType.None);
            pA460h = 0b0100_0001;
        } else if (ongen == OngenBoardType.SpeakBoard) {
            fmReg088 = new FmStatus(OngenBoardType.SpeakBoard);
            fmReg188 = new FmStatus(OngenBoardType.None);
            fmReg288 = new FmStatus(OngenBoardType.None);
            fmReg388 = new FmStatus(OngenBoardType.None);
            pA460h = (byte) 0xfc;
        }
    }

    public NiseDos getDos() {
        return dos;
    }

    public Register286 getRegisters() {
        return regs;
    }

    public Memory98 getMem() {
        return mem;
    }

    public NisePpz8 getPPZ8() {
        return ppz8;
    }

    public Nise286 getCPU() {
        return cpu;
    }

    public void userINT(UserInt ui) {
        cpu.addUserInt(ui);
    }

    public boolean execute() {
        return false;
    }

    public void runTimer() {
        fmReg088.timer.timer();
        fmReg188.timer.timer();
        fmReg288.timer.timer();
        fmReg388.timer.timer();
    }

    public boolean intTimer() {
        return ((fmReg088.timer.readStatus() & 3) |
                (fmReg188.timer.readStatus() & 3) |
                (fmReg288.timer.readStatus() & 3) |
                (fmReg388.timer.readStatus() & 3)) != 0;
    }

    public int stepExecute() {
        int08Timer.stepExecute();

        int waitClock = cpu.stepExecute();
        return waitClock;
    }

    public byte inpB(short port) {
        logger.log(Level.TRACE, "<Nise98>IN  Port:$%04x".formatted(port & 0xffff));
        switch (port & 0xffff) {
            case 0x0000: // Master interrupt Controller
                return 0;
            case 0x0002: // Interrupt controller Master
                return cpu.w_mmsk;
            case 0x0008: // Slave interrupt Controller
                return 0;
            case 0x000a: // Interrupt controller slave
                return cpu.w_smsk;
            case 0x00a0: // graphics GDC status read
                 // bit5:vSync
                vSyncCnt--;
                if (vSyncCnt == 0) {
                    vSyncCnt = 2;
                    vSync ^= 0x20;
                }
                return vSync;

            case 0x088: // FM port
            case 0x08a: // FM port
            case 0x08c: // FM port
            case 0x08e: // FM port
                return fmPortInPort(fmReg088, port);

            case 0x188: // FM port
            case 0x18a: // FM port
            case 0x18c: // FM port
            case 0x18e: // FM port
                return fmPortInPort(fmReg188, port);

            case 0x288: // FM port
            case 0x28a: // FM port
            case 0x28c: // FM port
            case 0x28e: // FM port
                return fmPortInPort(fmReg288, port);

            case 0x388: // FM port
            case 0x38a: // FM port
            case 0x38c: // FM port
            case 0x38e: // FM port
                return fmPortInPort(fmReg388, port);

            case 0xa460:
                 // For FMP
                 // 0b0000_0001  DO+      188h
                 // 0b0001_0001  73 board 188h
                 // 0b0010_0001  73 board 188h
                 // 0b0011_0001  73 board 288h
                 // 0b0100_0001  86 board 188h
                 // 0b0101_0001  86 board 288h
                 // 0b0110_0001  YMF288   188h(With ADPCM:SPB 188h
                 // 0b0111_0001  YMF288   188h(With ADPCM:SPB 188h
                 // 0b1000_0001  SPB      088h
                 // 0b1001_0001  x Hang (this value is never expected to be returned)
                 // 0b1010_0001  After that, YMF288 0x188h is probably being investigated further in subsequent processing.
                 //        ~~~~ Here, FMP is completely ignored (however, if it is 0xff, the judgment process ends.
                //              It is likely that further investigation will be carried out in subsequent processes).

logger.log(Level.TRACE, "fmReg188.ongen: " + fmReg188.ongen);
                if (fmReg188.ongen == OngenBoardType.None) return (byte) 0xff;
                else if (fmReg188.ongen == OngenBoardType.PC9801_26K) return (byte) 0xff;
                else if (fmReg188.ongen == OngenBoardType.PC9801_86B) return (byte) 0b0100_0001;
                else if (fmReg088.ongen == OngenBoardType.SpeakBoard) return (byte) 0b1000_0001;

                return (byte) 0xff;

            case 0xa66e:
                return mute86Pcm;

            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port & 0xffff));
        }
    }

    public short inpW(short port) {
        logger.log(Level.TRACE, "<Nise98>IN  Port:$%04x".formatted(port & 0xffff));
        switch (port & 0xffff) {
//            case 0xa460:
//                return IsOPNA ? 0x00 : 0xff; // 0xFF:not OPNA
//            case 0x088: // FM port
//                return IsSPB ? 0x01 : 0x00;
//            case 0x188: // FM port
//                return IsSPB ? 0x01 : 0x00;
//            case 0x288: // FM port
//                return IsSPB ? 0x01 : 0x00;
//            case 0x388: // FM port
//                    return IsSPB ? 0x01 : 0x00;
            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port & 0xffff));
        }
    }

    public void outpB(short port, byte data) {
        switch (port & 0xffff) {
            case 0x00: // Initialize interrupt
                logger.log(Level.TRACE, "<Nise98>OUT Port:$%02x".formatted(port & 0xff));
                break;
            case 0x02:
                logger.log(Level.TRACE, "<Nise98>OUT Port:$%02x".formatted(port & 0xff));
                cpu.w_mmsk = data;
                break;
            case 0x08: // Slave interrupt Controler
                break;
            case 0x0a:
                logger.log(Level.TRACE, "<Nise98>OUT Port:$%02x".formatted(port & 0xff));
                cpu.w_smsk = data;
                break;
            case 0x5f: // WAIT Wait for 0.6 microseconds or more
                break;
            case 0x68: // Mode F/F Register 1 http://www.webtech.co.jp/company/doc/undocumented_mem/io_disp.txt
                logger.log(Level.TRACE, "<Nise98>OUT Port:$%02x".formatted(port & 0xff));
                // 0000101nb: KAC Mode Dot Access Mode
                break;
            case 0x71: // TIMER: Counter#0 R/W
                logger.log(Level.TRACE, "<Nise98>OUT Port:$%02x".formatted(port & 0xff));
                cpu.interruptTrigger[8] = true;
                int08Timer.start();
                break;
            case 0x77: // TIMER: Set Mode
                logger.log(Level.TRACE, "<Nise98>OUT Port:$%02x".formatted(port & 0xff));
                break;
            case 0xa1: // Second byte of character code
                mojiCode = (short) ((mojiCode & 0x00ff) | ((data & 0xff) << 8));
                break;
            case 0xa3: // First byte of character code
                mojiCode = (short) ((mojiCode & 0xff00) | (data & 0xff));
                break;
            case 0xa5: // Line Counter
                lineCount = data;
                break;
            case 0xa9: // Character pattern writing
                mojiPattern = data;
                break;

            case 0x088: // FM port adr
            case 0x08a: // FM port val
            case 0x08c: // FM port val
            case 0x08e: // FM port val
                fmPortOutPort(fmReg088, port, data);
                break;
            case 0x188: // FM port adr
            case 0x18a: // FM port val
            case 0x18c: // FM port val
            case 0x18e: // FM port val
                fmPortOutPort(fmReg188, port, data);
                break;
            case 0x288: // FM port adr
            case 0x28a: // FM port adr
            case 0x28c: // FM port adr
            case 0x28e: // FM port adr
                fmPortOutPort(fmReg288, port, data);
                break;
            case 0x388: // FM port adr
            case 0x38a: // FM port adr
            case 0x38c: // FM port adr
            case 0x38e: // FM port adr
                fmPortOutPort(fmReg388, port, data);
                break;

            case 0xa460: // OPNA control
                logger.log(Level.DEBUG, "<Nise98> --- 0xA460(OPNA control) Val:$%02x".formatted(data & 0xff));
                // bit 1: 0: unMask 2608, 1: Mask 2608
                // bit 0: 0: Use 2608 as 2203 1: Use 2608 as 2608
                // bit2~ unused
                pA460h = data;
                break;
            case 0xa66e:
                // bit0: 1-> mute
                mute86Pcm = data;
                break;
            default:
                throw new UnsupportedOperationException("<Nise98>Request port:$%04x".formatted(port & 0xffff));
        }
    }

    public void outpW(short port, short data) {
        logger.log(Level.DEBUG, "<Nise98>OUT Port:$%04x".formatted(port & 0xffff));

        switch (port & 0xffff) {
            case 0x5f: // WAIT Wait for 0.6 microseconds or more
                break;
            case 0x088: // FM port
                break;
            case 0x188: // FM port
                break;
            case 0x288: // FM port
                break;
            case 0x388: // FM port
                break;
            case 0xa460: // OPNA Info It seems that clearing bits 0 and 1 can suppress the OPNA function.
                break;
            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port & 0xffff));
        }
    }

    private byte fmPortInPort(FmStatus fs, short port) {
        logger.log(Level.TRACE, "<Nise98> --- IN  FM Port:$%03x".formatted(port & 0xfff));
        switch (port & 0xff) { // byte size
            case 0x88: // FM port
                if (fs.ongen == OngenBoardType.None)
                    return (byte) 0xff;

                byte ret = (byte) (fs.timer.readStatus() |
                        (fs.isBusy ? 0x80 : 0x00));
                //| (fs.isTimerBOverFlow ? 0x02 : 00)
                //| (fs.isTimerAOverFlow ? 0x01 : 0x00)
                //);
                return ret;

            case 0x8a: // FM port
                if (fs.ongen == OngenBoardType.None)
                    return (byte) 0xff;

                if (fs.p88lastAdr == 0x0e)
                    return fs.int_;
                else if (fs.p88lastAdr == (byte) 0xff)
                    return (byte) (fs.ongen == OngenBoardType.PC9801_26K ? 0x00 : 0x01);
                else
                    return (byte) (fs.regs != null ? fs.regs[fs.p88lastAdr & 0xff] : 0x00);

            case 0x8c: // FM port
                //fs.adpcmPtr++;
                if (fs.ongen == OngenBoardType.None
                        || fs.ongen == OngenBoardType.PC9801_26K)
                    return (byte) 0xff;
                if (fs.ongen == OngenBoardType.PC9801_86B && (pA460h & 3) == 0)
                    return (byte) 0xff;

                return (byte) (fs.timer.readStatus() |
                        (fs.isBusy ? 0x80 : 0x00) |
                        0x08 // bit3:BRDY
                        //| (fs.isTimerBOverFlow ? 0x02 : 00)
                        //| (fs.isTimerAOverFlow ? 0x01 : 0x00)
                );

            case 0x8e: // FM port
                if (fs.ongen == OngenBoardType.None
                        || fs.ongen == OngenBoardType.PC9801_26K)
                    return (byte) 0xff;
                if (fs.ongen == OngenBoardType.PC9801_86B && (pA460h & 3) == 0)
                    return (byte) 0xff;

                if (fs.p8clastAdr == 0x08 && fs.adpcmMem != null)
                    return fs.adpcmMem[fs.adpcmPtr++ & 0xff];

                return 0x00;

            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port & 0xffff));
        }
    }

    private void fmPortOutPort(FmStatus fs, short port, byte data) {
        logger.log(Level.TRACE, "<Nise98> --- OUT FM Port:%03x Dat:$%02x".formatted(port & 0xfff, data & 0xff));
        if (fs.ongen == OngenBoardType.None) return;

        switch (port & 0xff) { // byte size
            case 0x088: // FM port adr
                fs.p88lastAdr = data;
                break;
            case 0x08a: // FM port val
                if (fs.regs != null) fs.regs[fs.p88lastAdr & 0xff] = data;
                fs.timer.writeReg(fs.p88lastAdr, data);
                opnaWrite.accept(port & 0xffff, fs.p88lastAdr & 0xff, data & 0xff);
                break;
            case 0x08c: // FM port val
                fs.p8clastAdr = data;
                //if (fs.p8clastAdr == 0x08) {
                //}
                break;
            case 0x08e: // FM port val
                if (fs.regs != null && fs.regs.length == 512) {
                    fs.regs[256 + (fs.p8clastAdr & 0xff)] = data;
                }
                if (fs.p8clastAdr == 0x00) {
                    if (data == 0x20) {
                        fs.adpcmReadMode = true;
                    }
                }
                if (fs.p8clastAdr == 0x02) {
                    fs.adpcmPtr = data;
                } else if (fs.p8clastAdr == 0x03) {
                    if (fs.adpcmReadMode) fs.adpcmPtr -= 2;
                    //fs.adpcmPtr = data;
                } else if (fs.p8clastAdr == 0x08) {
                    if (fs.adpcmMem != null) fs.adpcmMem[fs.adpcmPtr++ & 0xff] = data;
                } else if (fs.p8clastAdr == 0x10) {
                    //if (data == 0x13) fs.adpcmPtr++;
                }
                opnaWrite.accept(port & 0xffff, fs.p8clastAdr & 0xff, data & 0xff);
                break;
            default:
                throw new UnsupportedOperationException("<Nise98>Request port:$%04x".formatted(port & 0xffff));
        }
    }

    public int loadRun(String filename, String option, int startSegment) {
        return loadRun(filename, option, startSegment, false, false, false, 100_000_000, 0);
    }

    public int loadRun(String filename,
                       String option,
                       int startSegment,
                       boolean dispReg /* = false */,
                       boolean useStepCounter /* = false */,
                       boolean dispStepCounter /* = false */,
                       long MaxStepCounter /* = 100_000_000 */,
                       long StartStepCounterForDispStep /* = 0 */) {
        dos.loadAndExecuteFile(filename, option, startSegment);
        Register286 regs = getRegisters();
        if (dispReg) dispRegs(regs);

        while (((useStepCounter && step < MaxStepCounter) || !useStepCounter) && !dos.getProgramTerminate()) {
            int waitClock = stepExecute();

            if (useStepCounter) {
                step++;
                if (step < StartStepCounterForDispStep) continue;
            }

            if (dispReg) {
                regs = getRegisters();
                dispRegs(regs);
            }

            if (dispStepCounter) logger.log(Level.TRACE, "STEP:%d".formatted(step));

            if ((regs.ip & 0xffff) == 0xb35d) {
            }
        }

        logger.log(Level.TRACE, "Terminate program. return code=$%02x".formatted(dos.getReturnCode() & 0xff));
//        logger.log(Level.TRACE, "");

        return dos.getReturnCode();
    }

    public void callRunFunctionCall(byte intNumber) {
        callRunFunctionCall(intNumber, false, false, false, 100_000_000, 0);
    }

    public void callRunFunctionCall(byte intNumber,
                                    boolean dispReg /* = false */,
                                    boolean useStepCounter /* = false */,
                                    boolean dispStepCounter /* = false */,
                                    long maxStepCounter /* = 100_000_000 */,
                                    long startStepCounterForDispStep /* = 0 */) {
        Register286 regs = getRegisters();
        UserInt ui = new UserInt();
        ui.setIntNum(intNumber & 0xff);
        userINT(ui);
        //regs.IF=false;
        cpu.w_mmsk = (byte) 0xff;
        cpu.w_smsk = (byte) 0xff;
        dos.setProgramTerminate(false);

        functionCallTimes++;

        do {
            int waitClock = stepExecute();

            if (useStepCounter) {
                step++;
                if (step < startStepCounterForDispStep) continue;
            }

            if (dispReg) {
                regs = getRegisters();
                dispRegs(regs);
            }

//            if (dispStepCounter) logger.log(Level.TRACE, "functionCalls:%d STEP:%d".formatted(functionCallTimes, step));
//
//            if (regs.ip == 0xb01) {
//                if (regs.AL == 0xf0|| regs.AL == 0xcd)
//                    ;
//            }
//            if (regs.ip == 0x4088)// || regs.ip == 0x19c9 || regs.ip == 0x19d1) {
//                ;
//            }

        } while (regs.getCS() != 0 || regs.ip != 0);

        //logger.log(Level.DEBUG, "Terminate function call");
        //logger.log(Level.DEBUG, "");
    }

    private static void dispRegs(Register286 regs) {
        logger.log(Level.TRACE, Objects.requireNonNull(regs, regs.toString()));
    }
}
