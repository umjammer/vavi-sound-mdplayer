/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.psx;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Sony CXD8530AQ/BQ/CQ, the custom r3000a the PlayStation and the PS2 IOP run.
 * <p>
 * ported from aosdk eng_psf/psx.c, which is smf's MAME psx cpu.
 * <p>
 * The geometry engine (COP2/GTE) is a register file here and nothing more: a sound driver
 * has no geometry to transform, so the arithmetic is left out and {@link #docop2} only says
 * it was asked for.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class R3000 {

    private static final Logger logger = getLogger(R3000.class.getName());

    /** set true to have every instruction logged, which is what psxcpu_verbose does in aosdk */
    private static final boolean TRACE = false;

    // exceptions
    private static final int EXC_INT = 0;
    private static final int EXC_ADEL = 4;
    private static final int EXC_ADES = 5;
    private static final int EXC_SYS = 8;
    private static final int EXC_BP = 9;
    private static final int EXC_RI = 10;
    private static final int EXC_CPU = 11;
    private static final int EXC_OVF = 12;

    // cop0 registers
    public static final int CP0_RANDOM = 1;
    public static final int CP0_BADVADDR = 8;
    public static final int CP0_SR = 12;
    public static final int CP0_CAUSE = 13;
    public static final int CP0_EPC = 14;
    public static final int CP0_PRID = 15;

    private static final int SR_IEC = 1 << 0;
    private static final int SR_KUC = 1 << 1;
    private static final int SR_ISC = 1 << 16;
    private static final int SR_SWC = 1 << 17;
    private static final int SR_TS = 1 << 21;
    private static final int SR_BEV = 1 << 22;
    private static final int SR_RE = 1 << 25;
    private static final int SR_CU0 = 1 << 28;
    private static final int SR_CU1 = 1 << 29;
    private static final int SR_CU2 = 1 << 30;
    private static final int SR_CU3 = 1 << 31;

    private static final int CAUSE_EXC = 31 << 2;
    private static final int CAUSE_IP = 255 << 8;
    private static final int CAUSE_IP2 = 1 << 10;
    private static final int CAUSE_IP3 = 1 << 11;
    private static final int CAUSE_IP4 = 1 << 12;
    private static final int CAUSE_IP5 = 1 << 13;
    private static final int CAUSE_IP6 = 1 << 14;
    private static final int CAUSE_IP7 = 1 << 15;
    private static final int CAUSE_CE = 3 << 28;
    private static final int CAUSE_CE0 = 0 << 28;
    private static final int CAUSE_CE1 = 1 << 28;
    private static final int CAUSE_CE2 = 2 << 28;
    private static final int CAUSE_BD = 1 << 31;

    // opcodes
    private static final int OP_SPECIAL = 0;
    private static final int OP_REGIMM = 1;
    private static final int OP_J = 2;
    private static final int OP_JAL = 3;
    private static final int OP_BEQ = 4;
    private static final int OP_BNE = 5;
    private static final int OP_BLEZ = 6;
    private static final int OP_BGTZ = 7;
    private static final int OP_ADDI = 8;
    private static final int OP_ADDIU = 9;
    private static final int OP_SLTI = 10;
    private static final int OP_SLTIU = 11;
    private static final int OP_ANDI = 12;
    private static final int OP_ORI = 13;
    private static final int OP_XORI = 14;
    private static final int OP_LUI = 15;
    private static final int OP_COP0 = 16;
    private static final int OP_COP1 = 17;
    private static final int OP_COP2 = 18;
    private static final int OP_LB = 32;
    private static final int OP_LH = 33;
    private static final int OP_LWL = 34;
    private static final int OP_LW = 35;
    private static final int OP_LBU = 36;
    private static final int OP_LHU = 37;
    private static final int OP_LWR = 38;
    private static final int OP_SB = 40;
    private static final int OP_SH = 41;
    private static final int OP_SWL = 42;
    private static final int OP_SW = 43;
    private static final int OP_SWR = 46;
    private static final int OP_LWC1 = 49;
    private static final int OP_LWC2 = 50;
    private static final int OP_SWC1 = 57;
    private static final int OP_SWC2 = 58;

    // OP_SPECIAL
    private static final int FUNCT_SLL = 0;
    private static final int FUNCT_SRL = 2;
    private static final int FUNCT_SRA = 3;
    private static final int FUNCT_SLLV = 4;
    private static final int FUNCT_SRLV = 6;
    private static final int FUNCT_SRAV = 7;
    private static final int FUNCT_JR = 8;
    private static final int FUNCT_JALR = 9;
    /** not an r3000 instruction, aosdk puts it at the exception vector to trap into the HLE BIOS */
    public static final int FUNCT_HLECALL = 11;
    private static final int FUNCT_SYSCALL = 12;
    private static final int FUNCT_BREAK = 13;
    private static final int FUNCT_MFHI = 16;
    private static final int FUNCT_MTHI = 17;
    private static final int FUNCT_MFLO = 18;
    private static final int FUNCT_MTLO = 19;
    private static final int FUNCT_MULT = 24;
    private static final int FUNCT_MULTU = 25;
    private static final int FUNCT_DIV = 26;
    private static final int FUNCT_DIVU = 27;
    private static final int FUNCT_ADD = 32;
    private static final int FUNCT_ADDU = 33;
    private static final int FUNCT_SUB = 34;
    private static final int FUNCT_SUBU = 35;
    private static final int FUNCT_AND = 36;
    private static final int FUNCT_OR = 37;
    private static final int FUNCT_XOR = 38;
    private static final int FUNCT_NOR = 39;
    private static final int FUNCT_SLT = 42;
    private static final int FUNCT_SLTU = 43;

    // OP_REGIMM
    private static final int RT_BLTZ = 0;
    private static final int RT_BGEZ = 1;
    private static final int RT_BLTZAL = 16;
    private static final int RT_BGEZAL = 17;

    // OP_COP0/1/2
    private static final int RS_MFC = 0;
    private static final int RS_CFC = 2;
    private static final int RS_MTC = 4;
    private static final int RS_CTC = 6;
    private static final int RS_BC = 8;

    private static final int RT_BCF = 0;
    private static final int RT_BCT = 1;

    private static final int CF_RFE = 16;

    /** the delayed register slot that means "the pc", i.e. a taken branch */
    private static final int REGPC = 32;

    public static final int MIPS_IRQ0 = 0;
    public static final int MIPS_IRQ1 = 1;
    public static final int MIPS_IRQ2 = 2;
    public static final int MIPS_IRQ3 = 3;
    public static final int MIPS_IRQ4 = 4;
    public static final int MIPS_IRQ5 = 5;

    public static final int CLEAR_LINE = 0;
    public static final int ASSERT_LINE = 1;

    /** what mtc0 is allowed to change, per cop0 register */
    private static final int[] mtc0WriteMask = {
            0xffffffff, // INDEX
            0x00000000, // RANDOM
            0xffffff00, // ENTRYLO
            0x00000000,
            0xffe00000, // CONTEXT
            0x00000000,
            0x00000000,
            0x00000000,
            0x00000000, // BADVADDR
            0x00000000,
            0xffffffc0, // ENTRYHI
            0x00000000,
            0xf27fff3f, // SR
            0x00000300, // CAUSE
            0x00000000, // EPC
            0x00000000, // PRID
            0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x00000000, 0x00000000, 0x00000000, 0x00000000,
    };

    // ---- state ----

    /** the instruction being executed */
    public int op;
    public int pc;
    public int prevpc;
    public int delayv;
    public int delayr;
    public int hi;
    public int lo;
    public final int[] r = new int[32];
    public final int[] cp0r = new int[32];
    public final int[] cp2cr = new int[32];
    public final int[] cp2dr = new int[32];

    private int iCount;

    private final PsxBus bus;

    public R3000(PsxBus bus) {
        this.bus = bus;
    }

    /** aosdk's mips_init, which only registers save state and so has nothing to do here */
    public void init() {
    }

    public void reset() {
        setCp0r(CP0_SR, (cp0r[CP0_SR] & ~(SR_TS | SR_SWC | SR_KUC | SR_IEC)) | SR_BEV);
        setCp0r(CP0_RANDOM, 63);
        setCp0r(CP0_PRID, 0x00000200);
        setPc(0xbfc00000);
        prevpc = 0xffffffff;
    }

    /** ends the current {@link #execute} slice */
    public void shortenFrame() {
        iCount = 0;
    }

    public int getICount() {
        return iCount;
    }

    public void setICount(int count) {
        iCount = count;
    }

    // ---- helpers ----

    private void setCp0r(int reg, int value) {
        cp0r[reg] = value;
        if (reg == CP0_SR || reg == CP0_CAUSE) {
            if ((cp0r[CP0_SR] & SR_IEC) != 0 && (cp0r[CP0_SR] & cp0r[CP0_CAUSE] & CAUSE_IP) != 0) {
                exception(EXC_INT);
            } else if (delayr != REGPC && (pc & (((cp0r[CP0_SR] & SR_KUC) << 30) | 3)) != 0) {
                exception(EXC_ADEL);
                setCp0r(CP0_BADVADDR, pc);
            }
        }
    }

    private void commitDelayedLoad() {
        if (delayr != 0) {
            // REGPC is not a register, it is "a branch is pending". {@link #advancePc} and
            // {@link #delayedLoad} test for it before they get here, but {@link #delayedBranch}
            // does not, so a branch in a branch's delay slot arrives with it set - and the C
            // then writes the pending target one past the end of its register file, into a cop0
            // register nothing reads. The store simply goes nowhere; what matters is that the
            // pending branch is dropped, which is what the caller does next.
            if (delayr != REGPC) {
                r[delayr] = delayv;
            }
            delayr = 0;
            delayv = 0;
        }
    }

    private void delayedBranch(int adr) {
        if ((adr & (((cp0r[CP0_SR] & SR_KUC) << 30) | 3)) != 0) {
            exception(EXC_ADEL);
            setCp0r(CP0_BADVADDR, adr);
        } else {
            commitDelayedLoad();
            delayr = REGPC;
            delayv = adr;
            pc += 4;
        }
    }

    public void setPc(int val) {
        pc = val;
        delayr = 0;
        delayv = 0;
    }

    private void advancePc() {
        if (delayr == REGPC) {
            setPc(delayv);
        } else {
            commitDelayedLoad();
            pc += 4;
        }
    }

    private void load(int n_r, int n_v) {
        advancePc();
        if (n_r != 0) {
            r[n_r] = n_v;
        }
    }

    private void delayedLoad(int n_r, int n_v) {
        if (delayr == REGPC) {
            setPc(delayv);
            delayr = n_r;
            delayv = n_v;
        } else {
            commitDelayedLoad();
            pc += 4;
            if (n_r != 0) {
                r[n_r] = n_v;
            }
        }
    }

    private void exception(int exception) {
        setCp0r(CP0_SR, (cp0r[CP0_SR] & ~0x3f) | ((cp0r[CP0_SR] << 2) & 0x3f));
        if (delayr == REGPC) {
            setCp0r(CP0_EPC, pc - 4);
            setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~CAUSE_EXC) | CAUSE_BD | (exception << 2));
        } else {
            commitDelayedLoad();
            setCp0r(CP0_EPC, pc);
            setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~(CAUSE_EXC | CAUSE_BD)) | (exception << 2));
        }
        if ((cp0r[CP0_SR] & SR_BEV) != 0) {
            setPc(0xbfc00180);
        } else {
            setPc(0x80000080);
        }
    }

    public void setIrqLine(int irqLine, int state) {
        int ip = switch (irqLine) {
            case MIPS_IRQ0 -> CAUSE_IP2;
            case MIPS_IRQ1 -> CAUSE_IP3;
            case MIPS_IRQ2 -> CAUSE_IP4;
            case MIPS_IRQ3 -> CAUSE_IP5;
            case MIPS_IRQ4 -> CAUSE_IP6;
            case MIPS_IRQ5 -> CAUSE_IP7;
            default -> 0;
        };
        if (ip == 0) {
            return;
        }

        switch (state) {
        case CLEAR_LINE -> setCp0r(CP0_CAUSE, cp0r[CP0_CAUSE] & ~ip);
        case ASSERT_LINE -> setCp0r(CP0_CAUSE, cp0r[CP0_CAUSE] |= ip);
        default -> { /* nothing */ }
        }
    }

    // instruction fields

    private static int insOp(int op) { return (op >>> 26) & 63; }
    private static int insRs(int op) { return (op >>> 21) & 31; }
    private static int insRt(int op) { return (op >>> 16) & 31; }
    private static int insImmediate(int op) { return op & 0xffff; }
    private static int insTarget(int op) { return op & 0x3ffffff; }
    private static int insRd(int op) { return (op >>> 11) & 31; }
    private static int insShamt(int op) { return (op >>> 6) & 31; }
    private static int insFunct(int op) { return op & 63; }
    private static int insCo(int op) { return (op >>> 25) & 1; }
    private static int insCofun(int op) { return op & 0x1ffffff; }
    private static int insCf(int op) { return op & 63; }

    /** sign extends the 16 bit immediate */
    private static int wordExtend(int a) { return (short) a; }

    /** sign extends a byte read from memory */
    private static int byteExtend(int a) { return (byte) a; }

    /**
     * Runs at most the given number of cycles.
     *
     * @return the cycles actually run, which may be more than asked for
     */
    public int execute(int cycles) {
        int n_res;

        iCount = cycles;
        do {
            op = bus.read32(pc);

if (TRACE) {
    logger.log(Level.TRACE, "[%08x: %08x] [SP %08x RA %08x V0 %08x V1 %08x A0 %08x S0 %08x S1 %08x]"
            .formatted(pc, op, r[29], r[31], r[2], r[3], r[4], r[16], r[17]));
}

            switch (insOp(op)) {
            case OP_SPECIAL:
                switch (insFunct(op)) {
                case FUNCT_HLECALL:
                    bus.biosHle(pc);
                    break;
                case FUNCT_SLL:
                    load(insRd(op), r[insRt(op)] << insShamt(op));
                    break;
                case FUNCT_SRL:
                    load(insRd(op), r[insRt(op)] >>> insShamt(op));
                    break;
                case FUNCT_SRA:
                    load(insRd(op), r[insRt(op)] >> insShamt(op));
                    break;
                case FUNCT_SLLV:
                    load(insRd(op), r[insRt(op)] << (r[insRs(op)] & 31));
                    break;
                case FUNCT_SRLV:
                    load(insRd(op), r[insRt(op)] >>> (r[insRs(op)] & 31));
                    break;
                case FUNCT_SRAV:
                    load(insRd(op), r[insRt(op)] >> (r[insRs(op)] & 31));
                    break;
                case FUNCT_JR:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        delayedBranch(r[insRs(op)]);
                    }
                    break;
                case FUNCT_JALR:
                    n_res = pc + 8;
                    delayedBranch(r[insRs(op)]);
                    if (insRd(op) != 0) {
                        r[insRd(op)] = n_res;
                    }
                    break;
                case FUNCT_SYSCALL:
                    exception(EXC_SYS);
                    break;
                case FUNCT_BREAK:
                    // aosdk exits the process here, which is no way to end a song
                    logger.log(Level.WARNING, "BREAK at %08x".formatted(pc));
                    advancePc();
                    break;
                case FUNCT_MFHI:
                    load(insRd(op), hi);
                    break;
                case FUNCT_MTHI:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        advancePc();
                        hi = r[insRs(op)];
                    }
                    break;
                case FUNCT_MFLO:
                    load(insRd(op), lo);
                    break;
                case FUNCT_MTLO:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        advancePc();
                        lo = r[insRs(op)];
                    }
                    break;
                case FUNCT_MULT:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        long res64 = (long) r[insRs(op)] * (long) r[insRt(op)];
                        advancePc();
                        lo = (int) res64;
                        hi = (int) (res64 >> 32);
                    }
                    break;
                case FUNCT_MULTU:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        long res64 = (r[insRs(op)] & 0xffffffffL) * (r[insRt(op)] & 0xffffffffL);
                        advancePc();
                        lo = (int) res64;
                        hi = (int) (res64 >>> 32);
                    }
                    break;
                case FUNCT_DIV:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        if (r[insRt(op)] != 0) {
                            // the r3000 has no overflow trap, and java would throw for MIN_VALUE / -1
                            int div, mod;
                            if (r[insRs(op)] == Integer.MIN_VALUE && r[insRt(op)] == -1) {
                                div = Integer.MIN_VALUE;
                                mod = 0;
                            } else {
                                div = r[insRs(op)] / r[insRt(op)];
                                mod = r[insRs(op)] % r[insRt(op)];
                            }
                            advancePc();
                            lo = div;
                            hi = mod;
                        } else {
                            advancePc();
                        }
                    }
                    break;
                case FUNCT_DIVU:
                    if (insRd(op) != 0) {
                        exception(EXC_RI);
                    } else {
                        if (r[insRt(op)] != 0) {
                            int div = Integer.divideUnsigned(r[insRs(op)], r[insRt(op)]);
                            int mod = Integer.remainderUnsigned(r[insRs(op)], r[insRt(op)]);
                            advancePc();
                            lo = div;
                            hi = mod;
                        } else {
                            advancePc();
                        }
                    }
                    break;
                case FUNCT_ADD: {
                    n_res = r[insRs(op)] + r[insRt(op)];
                    if ((~(r[insRs(op)] ^ r[insRt(op)]) & (r[insRs(op)] ^ n_res)) < 0) {
                        exception(EXC_OVF);
                    } else {
                        load(insRd(op), n_res);
                    }
                    break;
                }
                case FUNCT_ADDU:
                    load(insRd(op), r[insRs(op)] + r[insRt(op)]);
                    break;
                case FUNCT_SUB:
                    n_res = r[insRs(op)] - r[insRt(op)];
                    if (((r[insRs(op)] ^ r[insRt(op)]) & (r[insRs(op)] ^ n_res)) < 0) {
                        exception(EXC_OVF);
                    } else {
                        load(insRd(op), n_res);
                    }
                    break;
                case FUNCT_SUBU:
                    load(insRd(op), r[insRs(op)] - r[insRt(op)]);
                    break;
                case FUNCT_AND:
                    load(insRd(op), r[insRs(op)] & r[insRt(op)]);
                    break;
                case FUNCT_OR:
                    load(insRd(op), r[insRs(op)] | r[insRt(op)]);
                    break;
                case FUNCT_XOR:
                    load(insRd(op), r[insRs(op)] ^ r[insRt(op)]);
                    break;
                case FUNCT_NOR:
                    load(insRd(op), ~(r[insRs(op)] | r[insRt(op)]));
                    break;
                case FUNCT_SLT:
                    load(insRd(op), r[insRs(op)] < r[insRt(op)] ? 1 : 0);
                    break;
                case FUNCT_SLTU:
                    load(insRd(op), Integer.compareUnsigned(r[insRs(op)], r[insRt(op)]) < 0 ? 1 : 0);
                    break;
                default:
                    exception(EXC_RI);
                    break;
                }
                break;
            case OP_REGIMM:
                switch (insRt(op)) {
                case RT_BLTZ:
                    if (r[insRs(op)] < 0) {
                        delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                    } else {
                        advancePc();
                    }
                    break;
                case RT_BGEZ:
                    if (r[insRs(op)] >= 0) {
                        delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                    } else {
                        advancePc();
                    }
                    break;
                case RT_BLTZAL:
                    n_res = pc + 8;
                    if (r[insRs(op)] < 0) {
                        delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                    } else {
                        advancePc();
                    }
                    r[31] = n_res;
                    break;
                case RT_BGEZAL:
                    n_res = pc + 8;
                    if (r[insRs(op)] >= 0) {
                        delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                    } else {
                        advancePc();
                    }
                    r[31] = n_res;
                    break;
                default:
                    break;
                }
                break;
            case OP_J:
                delayedBranch(((pc + 4) & 0xf0000000) + (insTarget(op) << 2));
                break;
            case OP_JAL:
                n_res = pc + 8;
                delayedBranch(((pc + 4) & 0xf0000000) + (insTarget(op) << 2));
                r[31] = n_res;
                break;
            case OP_BEQ:
                if (r[insRs(op)] == r[insRt(op)]) {
                    delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                } else {
                    advancePc();
                }
                break;
            case OP_BNE:
                if (r[insRs(op)] != r[insRt(op)]) {
                    delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                } else {
                    advancePc();
                }
                break;
            case OP_BLEZ:
                if (insRt(op) != 0) {
                    exception(EXC_RI);
                } else if (r[insRs(op)] <= 0) {
                    delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                } else {
                    advancePc();
                }
                break;
            case OP_BGTZ:
                if (insRt(op) != 0) {
                    exception(EXC_RI);
                } else if (r[insRs(op)] > 0) {
                    delayedBranch(pc + 4 + (wordExtend(insImmediate(op)) << 2));
                } else {
                    advancePc();
                }
                break;
            case OP_ADDI: {
                int imm = wordExtend(insImmediate(op));
                n_res = r[insRs(op)] + imm;
                if ((~(r[insRs(op)] ^ imm) & (r[insRs(op)] ^ n_res)) < 0) {
                    exception(EXC_OVF);
                } else {
                    load(insRt(op), n_res);
                }
                break;
            }
            case OP_ADDIU:
                if (insRt(op) == 0) {
                    bus.iopCall(pc, insImmediate(op));
                    advancePc();
                } else {
                    load(insRt(op), r[insRs(op)] + wordExtend(insImmediate(op)));
                }
                break;
            case OP_SLTI:
                load(insRt(op), r[insRs(op)] < wordExtend(insImmediate(op)) ? 1 : 0);
                break;
            case OP_SLTIU:
                load(insRt(op), Integer.compareUnsigned(r[insRs(op)], wordExtend(insImmediate(op))) < 0 ? 1 : 0);
                break;
            case OP_ANDI:
                load(insRt(op), r[insRs(op)] & insImmediate(op));
                break;
            case OP_ORI:
                load(insRt(op), r[insRs(op)] | insImmediate(op));
                break;
            case OP_XORI:
                load(insRt(op), r[insRs(op)] ^ insImmediate(op));
                break;
            case OP_LUI:
                load(insRt(op), insImmediate(op) << 16);
                break;
            case OP_COP0:
                if ((cp0r[CP0_SR] & SR_KUC) != 0 && (cp0r[CP0_SR] & SR_CU0) == 0) {
                    exception(EXC_CPU);
                    setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~CAUSE_CE) | CAUSE_CE0);
                } else {
                    switch (insRs(op)) {
                    case RS_MFC:
                        delayedLoad(insRt(op), cp0r[insRd(op)]);
                        break;
                    case RS_CFC:
                        logger.log(Level.DEBUG, "%08x: COP0 CFC not supported".formatted(pc));
                        advancePc();
                        break;
                    case RS_MTC:
                        n_res = (cp0r[insRd(op)] & ~mtc0WriteMask[insRd(op)])
                                | (r[insRt(op)] & mtc0WriteMask[insRd(op)]);
                        advancePc();
                        setCp0r(insRd(op), n_res);
                        break;
                    case RS_CTC:
                        logger.log(Level.DEBUG, "%08x: COP0 CTC not supported".formatted(pc));
                        advancePc();
                        break;
                    case RS_BC:
                        logger.log(Level.DEBUG, "%08x: COP0 BC not supported".formatted(pc));
                        advancePc();
                        break;
                    default:
                        if (insCo(op) == 1 && insCf(op) == CF_RFE) {
                            advancePc();
                            setCp0r(CP0_SR, (cp0r[CP0_SR] & ~0xf) | ((cp0r[CP0_SR] >>> 2) & 0xf));
                        } else {
                            logger.log(Level.DEBUG, "%08x: COP0 unknown command %08x".formatted(pc, op));
                            advancePc();
                        }
                        break;
                    }
                }
                break;
            case OP_COP1:
                if ((cp0r[CP0_SR] & SR_CU1) == 0) {
                    exception(EXC_CPU);
                    setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~CAUSE_CE) | CAUSE_CE1);
                } else {
                    logger.log(Level.DEBUG, "%08x: COP1 unknown command %08x".formatted(pc, op));
                    advancePc();
                }
                break;
            case OP_COP2:
                if ((cp0r[CP0_SR] & SR_CU2) == 0) {
                    exception(EXC_CPU);
                    setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~CAUSE_CE) | CAUSE_CE2);
                } else {
                    switch (insRs(op)) {
                    case RS_MFC:
                        delayedLoad(insRt(op), getcp2dr(insRd(op)));
                        break;
                    case RS_CFC:
                        delayedLoad(insRt(op), getcp2cr(insRd(op)));
                        break;
                    case RS_MTC:
                        setcp2dr(insRd(op), r[insRt(op)]);
                        advancePc();
                        break;
                    case RS_CTC:
                        setcp2cr(insRd(op), r[insRt(op)]);
                        advancePc();
                        break;
                    case RS_BC:
                        logger.log(Level.DEBUG, "%08x: COP2 BC not supported".formatted(pc));
                        advancePc();
                        break;
                    default:
                        if (insCo(op) == 1) {
                            docop2(insCofun(op));
                        } else {
                            logger.log(Level.DEBUG, "%08x: COP2 unknown command %08x".formatted(pc, op));
                        }
                        advancePc();
                        break;
                    }
                }
                break;
            case OP_LB:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LB SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        delayedLoad(insRt(op), byteExtend(bus.read8(reversed ? adr ^ 3 : adr)));
                    }
                }
                break;
            case OP_LH:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LH SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & (((cp0r[CP0_SR] & SR_KUC) << 30) | 1)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        delayedLoad(insRt(op), wordExtend(bus.read16(reversed ? adr ^ 2 : adr)));
                    }
                }
                break;
            case OP_LWL:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LWL SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        if (reversed) {
                            n_res = switch (adr & 3) {
                                case 0 -> (r[insRt(op)] & 0x00ffffff) | (bus.read8(adr + 3) << 24);
                                case 1 -> (r[insRt(op)] & 0x0000ffff) | (bus.read16(adr + 1) << 16);
                                case 2 -> (r[insRt(op)] & 0x000000ff) | (bus.read8(adr - 1) << 8) | (bus.read16(adr) << 16);
                                default -> bus.read32(adr - 3);
                            };
                        } else {
                            n_res = switch (adr & 3) {
                                case 0 -> (r[insRt(op)] & 0x00ffffff) | (bus.read8(adr) << 24);
                                case 1 -> (r[insRt(op)] & 0x0000ffff) | (bus.read16(adr - 1) << 16);
                                case 2 -> (r[insRt(op)] & 0x000000ff) | (bus.read16(adr - 2) << 8) | (bus.read8(adr) << 24);
                                default -> bus.read32(adr - 3);
                            };
                        }
                        delayedLoad(insRt(op), n_res);
                    }
                }
                break;
            case OP_LW:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LW SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    delayedLoad(insRt(op), bus.read32(adr));
                }
                break;
            case OP_LBU:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LBU SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        delayedLoad(insRt(op), bus.read8(reversed ? adr ^ 3 : adr));
                    }
                }
                break;
            case OP_LHU:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LHU SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & (((cp0r[CP0_SR] & SR_KUC) << 30) | 1)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        delayedLoad(insRt(op), bus.read16(reversed ? adr ^ 2 : adr));
                    }
                }
                break;
            case OP_LWR:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LWR SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        if (reversed) {
                            n_res = switch (adr & 3) {
                                case 3 -> (r[insRt(op)] & 0xffffff00) | bus.read8(adr - 3);
                                case 2 -> (r[insRt(op)] & 0xffff0000) | bus.read16(adr - 2);
                                case 1 -> (r[insRt(op)] & 0xff000000) | bus.read16(adr - 1) | (bus.read8(adr + 1) << 16);
                                default -> bus.read32(adr);
                            };
                        } else {
                            n_res = switch (adr & 3) {
                                case 3 -> (r[insRt(op)] & 0xffffff00) | bus.read8(adr);
                                case 2 -> (r[insRt(op)] & 0xffff0000) | bus.read16(adr);
                                case 1 -> (r[insRt(op)] & 0xff000000) | bus.read8(adr) | (bus.read16(adr + 1) << 8);
                                default -> bus.read32(adr);
                            };
                        }
                        delayedLoad(insRt(op), n_res);
                    }
                }
                break;
            case OP_SB:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: SB SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADES);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        bus.write8(reversed ? adr ^ 3 : adr, r[insRt(op)]);
                        advancePc();
                    }
                }
                break;
            case OP_SH:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: SH SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & (((cp0r[CP0_SR] & SR_KUC) << 30) | 1)) != 0) {
                        exception(EXC_ADES);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        bus.write16(reversed ? adr ^ 2 : adr, r[insRt(op)]);
                        advancePc();
                    }
                }
                break;
            case OP_SWL:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: SWL SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADES);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        if (reversed) {
                            switch (adr & 3) {
                            case 0 -> bus.write8(adr + 3, r[insRt(op)] >>> 24);
                            case 1 -> bus.write16(adr + 1, r[insRt(op)] >>> 16);
                            case 2 -> {
                                bus.write8(adr - 1, r[insRt(op)] >>> 8);
                                bus.write16(adr, r[insRt(op)] >>> 16);
                            }
                            case 3 -> bus.write32(adr - 3, r[insRt(op)]);
                            default -> { /* nothing */ }
                            }
                        } else {
                            switch (adr & 3) {
                            case 0 -> bus.write8(adr, r[insRt(op)] >>> 24);
                            case 1 -> bus.write16(adr - 1, r[insRt(op)] >>> 16);
                            case 2 -> {
                                bus.write16(adr - 2, r[insRt(op)] >>> 8);
                                bus.write8(adr, r[insRt(op)] >>> 24);
                            }
                            case 3 -> bus.write32(adr - 3, r[insRt(op)]);
                            default -> { /* nothing */ }
                            }
                        }
                        advancePc();
                    }
                }
                break;
            case OP_SW:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    // the bootstrap uses this, so it is not worth a word
                    advancePc();
                } else {
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    bus.write32(adr, r[insRt(op)]);
                    advancePc();
                }
                break;
            case OP_SWR:
                if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: SWR SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    boolean reversed = (cp0r[CP0_SR] & (SR_RE | SR_KUC)) == (SR_RE | SR_KUC);
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & ((cp0r[CP0_SR] & SR_KUC) << 30)) != 0) {
                        exception(EXC_ADES);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        if (reversed) {
                            switch (adr & 3) {
                            case 0 -> bus.write32(adr, r[insRt(op)]);
                            case 1 -> {
                                bus.write16(adr - 1, r[insRt(op)]);
                                bus.write8(adr + 1, r[insRt(op)] >>> 16);
                            }
                            case 2 -> bus.write16(adr - 2, r[insRt(op)]);
                            case 3 -> bus.write8(adr - 3, r[insRt(op)]);
                            default -> { /* nothing */ }
                            }
                        } else {
                            switch (adr & 3) {
                            case 0 -> bus.write32(adr, r[insRt(op)]);
                            case 1 -> {
                                bus.write8(adr, r[insRt(op)]);
                                bus.write16(adr + 1, r[insRt(op)] >>> 8);
                            }
                            case 2 -> bus.write16(adr, r[insRt(op)]);
                            case 3 -> bus.write8(adr, r[insRt(op)]);
                            default -> { /* nothing */ }
                            }
                        }
                        advancePc();
                    }
                }
                break;
            case OP_LWC1:
                logger.log(Level.DEBUG, "%08x: COP1 LWC not supported".formatted(pc));
                advancePc();
                break;
            case OP_LWC2:
                if ((cp0r[CP0_SR] & SR_CU2) == 0) {
                    exception(EXC_CPU);
                    setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~CAUSE_CE) | CAUSE_CE2);
                } else if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: LWC2 SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & (((cp0r[CP0_SR] & SR_KUC) << 30) | 3)) != 0) {
                        exception(EXC_ADEL);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        setcp2dr(insRt(op), bus.read32(adr));
                        advancePc();
                    }
                }
                break;
            case OP_SWC1:
                logger.log(Level.DEBUG, "%08x: COP1 SWC not supported".formatted(pc));
                advancePc();
                break;
            case OP_SWC2:
                if ((cp0r[CP0_SR] & SR_CU2) == 0) {
                    exception(EXC_CPU);
                    setCp0r(CP0_CAUSE, (cp0r[CP0_CAUSE] & ~CAUSE_CE) | CAUSE_CE2);
                } else if ((cp0r[CP0_SR] & SR_ISC) != 0) {
                    logger.log(Level.DEBUG, "%08x: SWC2 SR_ISC not supported".formatted(pc));
                    advancePc();
                } else {
                    int adr = r[insRs(op)] + wordExtend(insImmediate(op));
                    if ((adr & (((cp0r[CP0_SR] & SR_KUC) << 30) | 3)) != 0) {
                        exception(EXC_ADES);
                        setCp0r(CP0_BADVADDR, adr);
                    } else {
                        bus.write32(adr, getcp2dr(insRt(op)));
                        advancePc();
                    }
                }
                break;
            default:
                // aosdk deliberately does nothing here, not even advance the pc
                break;
            }
            iCount--;
        } while (iCount > 0);

        return cycles - iCount;
    }

    // ---- cop2, a register file only ----

    private int getcp2dr(int reg) {
        return cp2dr[reg];
    }

    private void setcp2dr(int reg, int value) {
        cp2dr[reg] = value;
    }

    private int getcp2cr(int reg) {
        return cp2cr[reg];
    }

    private void setcp2cr(int reg, int value) {
        cp2cr[reg] = value;
    }

    /** the geometry engine, which a sound driver has no use for */
    private void docop2(int gteop) {
        logger.log(Level.DEBUG, "%08x: GTE op %08x ignored".formatted(pc, gteop));
    }
}
