package mdplayer.driver.fmp.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import mdplayer.driver.mndrv.FMTimer;
import musicDriverInterface.ChipDatum;

import static java.lang.System.getLogger;


public class Nise98 {

    private static final Logger logger = getLogger(Nise98.class.getName());

    private Function<String, Object[]> msgWrite = null;
    private Consumer<ChipDatum> opnaWrite;
    private Register286 regs = null;
    private Memory98 mem = null;
    private Nise286 cpu = null;
    private NiseInt08Timer int08Timer = null;
    private NiseDos dos = null;
    private NisePpz8 ppz8 = null;
    private FileTemp fileTemp = null;

    private fmStatus fmReg088 = null;
    private fmStatus fmReg188 = null;
    private fmStatus fmReg288 = null;
    private fmStatus fmReg388 = null;
    private int V_SYNC_cnt = 2;
    private byte V_SYNC = 0;
    private short mojiCode;
    private byte lineCount;
    private byte mojiPattern;
    private byte pA460h;
    private byte Mute86PCM = 0;

    private int step = 0;
    private int functionCallTimes = 0;

    public enum OngenBoardType {
        None,
        PC9801_26K,
        SpeakBoard,
        PC9801_86B
    }

    public static class fmStatus {

        //bit76:11 int 5(IRQ12)(factory)
        //bit76:10 int 4(IRQ10)
        //bit76:01 int 6(IRQ13)
        //bit76:00 int 0(IRQ03)
        public byte Int = (byte) 0b1100_0000;
        public byte p88lastAdr = 0;
        public byte p8clastAdr = 0;
        public boolean IsBusy = false;
        public boolean IsTimerBOverFlow = true;
        public boolean IsTimerAOverFlow = false;
        public byte[] regs;
        public byte[] AdpcmMem;
        public byte AdpcmPtr = 0;
        public boolean AdpcmReadMode = false;
        public FMTimer timer = null;

        //ongenBoardType
        public OngenBoardType ongen = OngenBoardType.SpeakBoard;

        public fmStatus(OngenBoardType ongen) {
            this.ongen = ongen;
            if (ongen == OngenBoardType.PC9801_26K) {
                regs = new byte[256 * 1];
            } else if (ongen == OngenBoardType.PC9801_86B) {
                regs = new byte[256 * 2];
                AdpcmMem = null;
            } else if (ongen == OngenBoardType.SpeakBoard) {
                regs = new byte[256 * 2];
                AdpcmMem = new byte[256];
            } else {
                regs = null;
                AdpcmMem = null;
            }

            timer = new FMTimer(false, null, 7987200); // OPNATimer(55467, 7987200);
        }
    }

    public void Init(Function<String, Object[]> msgWrite, Consumer<ChipDatum> opnaWrite, FileTemp fileTemp, OngenBoardType ongen /* = enmOngenBoardType.PC9801_86B */) {
        logger.log(Level.DEBUG, "<Nise98>Init");

        this.opnaWrite = opnaWrite;
        this.fileTemp = fileTemp;
        regs = new Register286();
        mem = new Memory98(16 * 64 * 1024);
        dos = new NiseDos(regs, mem, fileTemp);
        cpu = new Nise286(this);
        int08Timer = new NiseInt08Timer(cpu, 8);
        ppz8 = new NisePpz8(this);

        if (ongen == OngenBoardType.None) {
            fmReg088 = new fmStatus(OngenBoardType.None);
            fmReg188 = new fmStatus(OngenBoardType.None);
            fmReg288 = new fmStatus(OngenBoardType.None);
            fmReg388 = new fmStatus(OngenBoardType.None);
            pA460h = (byte) 0xfc;
        } else if (ongen == OngenBoardType.PC9801_26K) {
            fmReg088 = new fmStatus(OngenBoardType.None);
            fmReg188 = new fmStatus(OngenBoardType.PC9801_26K);
            fmReg288 = new fmStatus(OngenBoardType.None);
            fmReg388 = new fmStatus(OngenBoardType.None);
            pA460h = (byte) 0xfc;
        } else if (ongen == OngenBoardType.PC9801_86B) {
            fmReg088 = new fmStatus(OngenBoardType.None);
            fmReg188 = new fmStatus(OngenBoardType.PC9801_86B);
            fmReg288 = new fmStatus(OngenBoardType.None);
            fmReg388 = new fmStatus(OngenBoardType.None);
            pA460h = 0b0100_0001;
        } else if (ongen == OngenBoardType.SpeakBoard) {
            fmReg088 = new fmStatus(OngenBoardType.SpeakBoard);
            fmReg188 = new fmStatus(OngenBoardType.None);
            fmReg288 = new fmStatus(OngenBoardType.None);
            fmReg388 = new fmStatus(OngenBoardType.None);
            pA460h = (byte) 0xfc;
        }
    }

    public NiseDos GetDos() {
        return dos;
    }

    public Register286 getRegisters() {
        return regs;
    }

    public Memory98 GetMem() {
        return mem;
    }

    public NisePpz8 GetPPZ8() {
        return ppz8;
    }

    public Nise286 GetCPU() {
        return cpu;
    }

    public void UserINT(UserInt ui) {
        cpu.AddUserInt(ui);
    }

    public boolean Execute() {
        return false;
    }


    public void Runtimer() {
        fmReg088.timer.timer();
        fmReg188.timer.timer();
        fmReg288.timer.timer();
        fmReg388.timer.timer();
    }

    public boolean IntTimer() {
        return (
                (fmReg088.timer.readStatus() & 3)
                        | (fmReg188.timer.readStatus() & 3)
                        | (fmReg288.timer.readStatus() & 3)
                        | (fmReg388.timer.readStatus() & 3)
        ) != 0;
    }


    public int StepExecute() {
        int08Timer.stepExecute();

        int waitClock = cpu.StepExecute();
        return waitClock;
    }

    public byte INPb(short port) {
        logger.log(Level.DEBUG, "<Nise98>IN  Port:$%04x", port);
        switch (port & 0xffff) {
            case 0x0000://Master interrupt Controler
                return 0;
            case 0x0002://Interrupt controler Master
                return cpu.w_mmsk;
            case 0x0008://Slave  interrupt Controler
                return 0;
            case 0x000a://Interrupt controler slave
                return cpu.w_smsk;
            case 0x00a0://graphics GDC status read
                //bit5:V_SYNC
                V_SYNC_cnt--;
                if (V_SYNC_cnt == 0) {
                    V_SYNC_cnt = 2;
                    V_SYNC ^= 0x20;
                }
                return V_SYNC;

            case 0x088://FM port
            case 0x08a://FM port
            case 0x08c://FM port
            case 0x08e://FM port
                return FMPortInport(fmReg088, port);

            case 0x188://FM port
            case 0x18a://FM port
            case 0x18c://FM port
            case 0x18e://FM port
                return FMPortInport(fmReg188, port);

            case 0x288://FM port
            case 0x28a://FM port
            case 0x28c://FM port
            case 0x28e://FM port
                return FMPortInport(fmReg288, port);

            case 0x388://FM port
            case 0x38a://FM port
            case 0x38c://FM port
            case 0x38e://FM port
                return FMPortInport(fmReg388, port);

            case 0xa460:
                //FMP の場合
                //0b0000_0001　DO+      188h
                //0b0001_0001　73ボード 188h
                //0b0010_0001　73ボード 188h
                //0b0011_0001　73ボード 288h
                //0b0100_0001　86ボード 188h
                //0b0101_0001　86ボード 288h
                //0b0110_0001　YMF288   188h(ADPCM有り:SPB 188h
                //0b0111_0001　YMF288   188h(ADPCM有り:SPB 188h
                //0b1000_0001　SPB      088h
                //0b1001_0001　x ハング (もともとこの値を返すことは無いと思われる)
                //0b1010_0001　以降、YMF288   0x188h 恐らく後続の処理でさらに調べていると思われる
                //       ~~~~ここはFMPは完全無視(但し0xffだった場合は判定処理が終わる.恐らく後続の処理でさらに調べていると思われる)

                if (fmReg188.ongen == OngenBoardType.None) return (byte) 0xff;
                else if (fmReg188.ongen == OngenBoardType.PC9801_26K) return (byte) 0xff;
                else if (fmReg188.ongen == OngenBoardType.PC9801_86B) return (byte) 0b0100_0001;
                else if (fmReg088.ongen == OngenBoardType.SpeakBoard) return (byte) 0b1000_0001;

                return (byte) 0xff;

            case 0xa66e:
                return Mute86PCM;

            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port));
        }
    }

    public short INPw(short port) {
        logger.log(Level.DEBUG, "<Nise98>IN  Port:$%04x".formatted(port));
        switch (port) {
//                case 0xa460:
//                    return IsOPNA ? 0x00 : 0xff;//0xFF:not OPNA
//                case 0x088://FM port
//                    return IsSPB ? 0x01 : 0x00;
//                case 0x188://FM port
//                    return IsSPB ? 0x01 : 0x00;
//                case 0x288://FM port
//                    return IsSPB ? 0x01 : 0x00;
//                case 0x388://FM port
//                    return IsSPB ? 0x01 : 0x00;
            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port));
        }
    }

    public void OUTPb(short port, byte data) {
        switch (port & 0xffff) {
            case 0x00: // Initialize interrupt
                logger.log(Level.DEBUG, "<Nise98>OUT Port:$%02x".formatted(port));
                break;
            case 0x02:
                logger.log(Level.DEBUG, "<Nise98>OUT Port:$%02x".formatted(port));
                cpu.w_mmsk = data;
                break;
            case 0x08: // Slave interrupt Controler
                break;
            case 0x0a:
                logger.log(Level.DEBUG, "<Nise98>OUT Port:$%02x".formatted(port));
                cpu.w_smsk = data;
                break;
            case 0x5f: // WAIT Wait for 0.6 microseconds or more
                break;
            case 0x68: // Mode F/F Register 1 http://www.webtech.co.jp/company/doc/undocumented_mem/io_disp.txt
                logger.log(Level.DEBUG, "<Nise98>OUT Port:$%02x".formatted(port));
                // 0000101nb: KAC Mode Dot Access Mode
                break;
            case 0x71: // TIMER: Counter#0 R/W
                logger.log(Level.DEBUG, "<Nise98>OUT Port:$%02x".formatted(port));
                cpu.interruptTrigger[8] = true;
                int08Timer.start();
                break;
            case 0x77: // TIMER: Set Mode
                logger.log(Level.DEBUG, "<Nise98>OUT Port:$%02x".formatted(port));
                break;
            case 0xa1: // Second byte of character code
                mojiCode = (short) ((mojiCode & 0x00ff) | (data << 8));
                break;
            case 0xa3: // First byte of character code
                mojiCode = (short) ((mojiCode & 0xff00) | data);
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
                FMPortOutport(fmReg088, port, data);
                break;
            case 0x188: // FM port adr
            case 0x18a: // FM port val
            case 0x18c: // FM port val
            case 0x18e: // FM port val
                FMPortOutport(fmReg188, port, data);
                break;
            case 0x288: // FM port adr
            case 0x28a: // FM port adr
            case 0x28c: // FM port adr
            case 0x28e: // FM port adr
                FMPortOutport(fmReg288, port, data);
                break;
            case 0x388: // FM port adr
            case 0x38a: // FM port adr
            case 0x38c: // FM port adr
            case 0x38e: // FM port adr
                FMPortOutport(fmReg388, port, data);
                break;

            case 0xa460: // OPNA control
                logger.log(Level.DEBUG, "<Nise98> --- 0xA460(OPNA control) Val:$%02x".formatted(data));
                // bit 1: 0: unMask 2608, 1: Mask 2608
                // bit 0: 0: Use 2608 as 2203 1: Use 2608 as 2608
                // bit2~ unused
                pA460h = data;
                break;
            case 0xa66e:
                // bit0: 1-> mute
                Mute86PCM = data;
                break;
            default:
                throw new UnsupportedOperationException("<Nise98>Request port:$%04x".formatted(port));
        }
    }

    public void OUTPw(short port, short data) {
        logger.log(Level.DEBUG, "<Nise98>OUT Port:$%04x".formatted(port));

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
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port));
        }
    }

    private byte FMPortInport(fmStatus fs, short port) {
        logger.log(Level.DEBUG, "<Nise98> --- IN  FM Port:$%03x".formatted(port));
        switch (port & 0xff) {
            case 0x88: // FM port
                if (fs.ongen == OngenBoardType.None)
                    return (byte) 0xff;

                byte ret = (byte) (fs.timer.readStatus() |
                        (fs.IsBusy ? 0x80 : 0x00));
                //| (fs.IsTimerBOverFlow ? 0x02 : 00)
                //| (fs.IsTimerAOverFlow ? 0x01 : 0x00)
                //);
                return ret;

            case 0x8a: // FM port
                if (fs.ongen == OngenBoardType.None)
                    return (byte) 0xff;

                if (fs.p88lastAdr == 0x0e)
                    return fs.Int;
                else if (fs.p88lastAdr == (byte) 0xff)
                    return (byte) (fs.ongen == OngenBoardType.PC9801_26K ? 0x00 : 0x01);
                else
                    return (byte) (fs.regs != null ? fs.regs[fs.p88lastAdr] : 0x00);

            case 0x8c: // FM port
                //fs.AdpcmPtr++;
                if (fs.ongen == OngenBoardType.None
                        || fs.ongen == OngenBoardType.PC9801_26K)
                    return (byte) 0xff;
                if (fs.ongen == OngenBoardType.PC9801_86B && (pA460h & 3) == 0)
                    return (byte) 0xff;

                return (byte) (fs.timer.readStatus() |
                        (fs.IsBusy ? 0x80 : 0x00)
                        | 0x08 //bit3:BRDY 
                        //| (fs.IsTimerBOverFlow ? 0x02 : 00)
                        //| (fs.IsTimerAOverFlow ? 0x01 : 0x00)
                );

            case 0x8e: // FM port
                if (fs.ongen == OngenBoardType.None
                        || fs.ongen == OngenBoardType.PC9801_26K)
                    return (byte) 0xff;
                if (fs.ongen == OngenBoardType.PC9801_86B && (pA460h & 3) == 0)
                    return (byte) 0xff;

                if (fs.p8clastAdr == 0x08 && fs.AdpcmMem != null)
                    return fs.AdpcmMem[fs.AdpcmPtr++];

                return 0x00;

            default:
                throw new UnsupportedOperationException("Request port:$%04x".formatted(port));
        }
    }

    private void FMPortOutport(fmStatus fs, short port, byte data) {
        logger.log(Level.DEBUG, "<Nise98> --- OUT FM Port:%03x Dat:$%02x".formatted(port, data));
        if (fs.ongen == OngenBoardType.None) return;

        ChipDatum cd;
        switch (port & 0xff) {
            case 0x088: // FM port adr
                fs.p88lastAdr = data;
                break;
            case 0x08a: // FM port val
                if (fs.regs != null) fs.regs[fs.p88lastAdr] = data;
                fs.timer.WriteReg(fs.p88lastAdr, data);
                cd = new ChipDatum(port, fs.p88lastAdr, data);
                opnaWrite.accept(cd);
                break;
            case 0x08c: // FM port val
                fs.p8clastAdr = data;
                //if (fs.p8clastAdr == 0x08) {
                //}
                break;
            case 0x08e://FM port val
                if (fs.regs != null && fs.regs.length == 512) {
                    fs.regs[256 + fs.p8clastAdr] = data;
                }
                if (fs.p8clastAdr == 0x00) {
                    if (data == 0x20) {
                        fs.AdpcmReadMode = true;
                    }
                }
                if (fs.p8clastAdr == 0x02) {
                    fs.AdpcmPtr = data;
                } else if (fs.p8clastAdr == 0x03) {
                    if (fs.AdpcmReadMode) fs.AdpcmPtr -= 2;
                    //fs.AdpcmPtr = data;
                } else if (fs.p8clastAdr == 0x08) {
                    if (fs.AdpcmMem != null) fs.AdpcmMem[fs.AdpcmPtr++] = data;
                } else if (fs.p8clastAdr == 0x10) {
                    //if (data == 0x13) fs.AdpcmPtr++;
                }
                cd = new ChipDatum(port, fs.p8clastAdr, data);
                opnaWrite.accept(cd);
                break;
            default:
                throw new UnsupportedOperationException("<Nise98>Request port:$%04x".formatted(port));
        }
    }

    public int LoadRun(String filename, String option, int startSegment) {
        return LoadRun(filename, option, startSegment, false, false, false, 100_000_000, 0);
    }

    public int LoadRun(String filename,
                       String option,
                       int startSegment,
                       boolean dispReg /* = false */,
                       boolean useStepCounter /* = false */,
                       boolean dispStepCounter /* = false */,
                       long MaxStepCounter /* = 100_000_000 */,
                       long StartStepCounterForDispStep /* = 0 */) {
        dos.loadAndExecuteFile(filename, option, startSegment);
        Register286 regs = getRegisters();
        if (dispReg) DispRegs(regs);

        while (((useStepCounter && step < MaxStepCounter) || !useStepCounter) && !dos.getProgramTerminate()) {
            int waitClock = StepExecute();

            if (useStepCounter) {
                step++;
                if (step < StartStepCounterForDispStep) continue;
            }

            if (dispReg) {
                regs = getRegisters();
                DispRegs(regs);
            }

            if (dispStepCounter) logger.log(Level.TRACE, "STEP:%d\n".formatted(step));

            if ((regs.IP & 0xffff) == 0xb35d) {
            }
        }

        logger.log(Level.DEBUG, "Terminate program. return code=$%02x".formatted(dos.getReturnCode()));
        logger.log(Level.DEBUG, "");

        return dos.getReturnCode();
    }

    public void CallRunfunctionCall(byte intnumber) {
        CallRunfunctionCall(intnumber, false, false, false, 100_000_000, 0);
    }

    public void CallRunfunctionCall(byte intnumber,
                                    boolean dispReg /* = false */,
                                    boolean useStepCounter /* = false */,
                                    boolean dispStepCounter /* = false */,
                                    long MaxStepCounter /* = 100_000_000 */,
                                    long StartStepCounterForDispStep /* = 0 */) {
        Register286 regs = getRegisters();
        UserInt ui = new UserInt();
        ui.setIntNum(intnumber);
        UserINT(ui);
        //regs.IF=false;
        cpu.w_mmsk = (byte) 0xff;
        cpu.w_smsk = (byte) 0xff;
        dos.setProgramTerminate(false);

        functionCallTimes++;

        do {
            int waitClock = StepExecute();

            if (useStepCounter) {
                step++;
                if (step < StartStepCounterForDispStep) continue;
            }

            if (dispReg) {
                regs = getRegisters();
                DispRegs(regs);
            }

            //if (dispStepCounter) logger.log(Level.TRACE, "functionCalls:{0} STEP:{1}\r\n", functionCallTimes, step);

            //if (regs.IP == 0xb01) {
            //    if(regs.AL==0xf0|| regs.AL == 0xcd)
            //    ;
            //}
            //if (regs.IP == 0x4088)// || regs.IP == 0x19c9 || regs.IP == 0x19d1) {
            //    ;
            //}

        } while (regs.getCS() != 0 || regs.IP != 0);

        //logger.log(Level.DEBUG, "Terminate function call");
        //logger.log(Level.DEBUG, "");
    }

    private void DispRegs(Register286 regs) {
        logger.log(Level.TRACE, Objects.requireNonNull(regs, regs.toString()));
    }
}
