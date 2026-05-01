package mdplayer.driver.hes;

import java.util.function.BiFunction;

import vavi.util.compat.TriConsumer;


public class Km6280 {

    public interface ReadHandler extends BiFunction<M_Hes.HESHES, Integer, Integer> {
    }

    public interface WriterHandler extends TriConsumer<M_Hes.HESHES, Integer, Integer> {
    }

    /** Accumulator */
    public int a;
    /** Status register */
    public int p;
    /** x register */
    public int x;
    /** y register */
    public int y;
    /** Stack pointer */
    public int s;
    /** Program Counter */
    public int pc;

    /** Interrupt request */
    public int iRequest;
    /** Interrupt mask */
    public int iMask;
    /** (incremental)cycle counter */
    public int clock;
    public int lastCode;
//    /** pointer to user area */
//    public Km6280 user;
    /** pointer to user area */
    public M_Hes.HESHES user;

    public int lowClockMode;

    public ReadHandler readByte;
    public WriterHandler writeByte;

    public ReadHandler readMPR;
    public WriterHandler writeMPR;
    public WriterHandler write6270;

    public enum Flags {
        C(0x01),
        Z(0x02),
        I(0x04),
        D(0x08),
        B(0x10),
        T(0x20),
        V(0x40),
        N(0x80);
        final int v;

        Flags(int v) {
            this.v = v;
        }
    }

    public enum IRQ {
        INIT(1),
        RESET(2),
        NMI(4),
        BRK(8),
        TIMER(16),
        INT1(32),
        INT2(64);
        final int v;

        IRQ(int v) {
            this.v = v;
        }
    }

    public static final int C_FLAG = Flags.C.v;
    public static final int Z_FLAG = Flags.Z.v;
    public static final int I_FLAG = Flags.I.v;
    public static final int D_FLAG = Flags.D.v;
    public static final int B_FLAG = Flags.B.v;
    public static final int T_FLAG = Flags.T.v;
    public static final int V_FLAG = Flags.V.v;
    public static final int N_FLAG = Flags.N.v;
    public static final int R_FLAG = 0;

    public static final int BASE_OF_ZERO = 0x2000;

    public static final int VEC_RESET = 0xfffe;
    public static final int VEC_NMI = 0xfffc;
    public static final int VEC_TIMER = 0xfffa;
    public static final int VEC_INT1 = 0xfff8;
    public static final int VEC_INT = 0xfff6;

    public static final int VEC_BRK = VEC_INT;

    public static final int IRQ_INIT = IRQ.INIT.v;
    public static final int IRQ_RESET = IRQ.RESET.v;
    public static final int IRQ_NMI = IRQ.NMI.v;
    public static final int IRQ_BRK = IRQ.BRK.v;
    public static final int IRQ_TIMER = IRQ.TIMER.v;
    public static final int IRQ_INT1 = IRQ.INT1.v;
    public static final int IRQ_INT = IRQ.INT2.v;

    int readK(int adr) {
        return this.readByte.apply(this.user, adr);
    }

    void writeK(int adr, int value) {
        this.writeByte.accept(this.user, adr, value);
    }

    int readMPRK(int adr) {
        return this.readMPR.apply(this.user, adr);
    }

    void writeMPRK(int adr, int value) {
        this.writeMPR.accept(this.user, adr, value);
    }

    void write6270K(int adr, int value) {
        this.write6270.accept(this.user, adr, value);
    }

    public static final byte[] fl_table = {
            0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

            (byte) (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,

            0x03, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,

            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
    };

    public int FLAG_NZ(int w) {
        return fl_table[w & 0xff];
    }

    public int FLAG_NZC(int w) {
        return (fl_table[w & 0x01ff]);
    }

    public void KI_ADDCLOCK(int cycle) {
        if (this.lowClockMode != 0) {
            cycle += cycle + cycle; //
            cycle += cycle; // x6
        }
        this.clock += cycle;
    }

    public int KI_READWORD(int adr) {
        int ret = readK(adr);
        int i = (ret + (readK((adr + 1) & 0xffff) << 8)) & 0xffff;
        return i;
    }

    public int KI_READWORDZP(int adr) {
        int ret = readK(BASE_OF_ZERO + adr);
        return (ret + (readK(BASE_OF_ZERO + ((adr + 1) & 0xff)) << 8)) & 0xffff;
    }

    public int KAI_IMM() {
        int ret = this.pc;
        this.pc = (this.pc + 1) & 0xffff;
        return ret;
    }

    public int KAI_IMM16() {
        int ret = this.pc;
        this.pc = (this.pc + 2) & 0xffff;
        return ret;
    }

    public int KAI_ABS() {
        return KI_READWORD(KAI_IMM16());
    }

    public int KAI_ABSX() {
        return (KAI_ABS() + this.x) & 0xffff;
    }

    public int KAI_ABSY() {
        return (KAI_ABS() + this.y) & 0xffff;
    }

    public int KAI_ZP() {
        return readK(KAI_IMM());
    }

    public int KAI_ZPX() {
        return (KAI_ZP() + this.x) & 0xff;
    }

    public int KAI_INDY() {
        return (KI_READWORDZP(KAI_ZP()) + this.y) & 0xffff;
    }

    public int KA_IMM() {
        int ret = this.pc;
        this.pc = (this.pc + 1) & 0xffff;
        return ret;
    }

    public int KA_IMM16() {
        int ret = this.pc;
        this.pc = (this.pc + 2) & 0xffff;
        return ret;
    }

    public int KA_ABS() {
        return KI_READWORD(KAI_IMM16());
    }

    public int KA_ABSX() {
        return (KAI_ABS() + this.x) & 0xffff;
    }

    public int KA_ABSY() {
        return (KAI_ABS() + this.y) & 0xffff;
    }

    public int KA_ZP() {
        return BASE_OF_ZERO + readK(KAI_IMM());
    }

    public int KA_ZPX() {
        return BASE_OF_ZERO + ((KAI_ZP() + this.x) & 0xff);
    }

    public int KA_ZPY() {
        return BASE_OF_ZERO + ((KAI_ZP() + this.y) & 0xff);
    }

    public int KA_INDX() {
        return KI_READWORDZP(KAI_ZPX());
    }

    public int KA_INDY() {
        return (KI_READWORDZP(KAI_ZP()) + this.y) & 0xffff;
    }

    public int KA_IND() {
        return KI_READWORDZP(KAI_ZP());
    }

    public void KM_ALUADDER(int src) {
        int w = this.a + src + (this.p & C_FLAG);
        this.p &= ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG | T_FLAG);
        this.p += FLAG_NZC(w) + ((((~this.a ^ src) & (this.a ^ w)) >> 1) & V_FLAG);
        this.p &= 0xff;
        this.a = w & 0xff;
    }

    public void KM_ALUADDER_D(int src) {
        int wl = (this.a & 0x0F) + (src & 0x0F) + (this.p & C_FLAG);
        int w = this.a + src + (this.p & C_FLAG);
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        if (wl > 0x9) w += 0x6;
        if (w > 0x9F) {
            this.p += C_FLAG;
            w += 0x60;
        }
        this.p += FLAG_NZ(w);
        this.p &= 0xff;
        this.a = w & 0xff;
        KI_ADDCLOCK(1);
    }

    public void KMI_ADC(int src) {
        KM_ALUADDER(src);
    }

    public void KMI_ADC_D(int src) {
        KM_ALUADDER_D(src);
    }

    public void KMI_SBC(int src) {
        KM_ALUADDER(src ^ 0xff);
    }

    public void KMI_SBC_D(int src) {
        KM_ALUADDER_D(((src ^ 0xff) + (0x100 - 0x66)) & 0xff);
    }

    public void KM_CMP(int src) {
        int w = this.a + (src ^ 0xff) + 1;
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZC(w);
        this.p &= 0xff;
    }

    public void KM_CPX(int src) {
        int w = this.x + (src ^ 0xff) + 1;
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZC(w);
        this.p &= 0xff;
    }

    public void KM_CPY(int src) {
        int w = this.y + (src ^ 0xff) + 1;
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZC(w);
        this.p &= 0xff;
    }

    public void KM_BIT(int src) {
        int w = this.a & src;
        this.p &= ~(N_FLAG | V_FLAG | Z_FLAG);
        this.p += (src & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
        this.p &= 0xff;
    }

    public void KM_AND(int src) {
        this.a &= src;
        this.p &= ~(N_FLAG | Z_FLAG | T_FLAG);
        this.p += FLAG_NZ(this.a);
        this.p &= 0xff;
    }

    public void KM_ORA(int src) {
        this.a |= src;
        this.p &= ~(N_FLAG | Z_FLAG | T_FLAG);
        this.p += FLAG_NZ(this.a);
        this.p &= 0xff;
    }

    public void KM_EOR(int src) {
        this.a ^= src;
        this.p &= ~(N_FLAG | Z_FLAG | T_FLAG);
        this.p += FLAG_NZ(this.a);
        this.p &= 0xff;
    }

    public int KM_DEC(int des) {
        int w = des - 1;
        this.p &= ~(N_FLAG | Z_FLAG);
        this.p += FLAG_NZ(w);
        this.p &= 0xff;
        return w & 0xff;
    }

    public int KM_INC(int des) {
        int w = des + 1;
        this.p &= ~(N_FLAG | Z_FLAG);
        this.p += FLAG_NZ(w);
        this.p &= 0xff;
        return w & 0xff;
    }

    public int KM_ASL(int des) {
        int w = des << 1;
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZ(w) + ((des >> 7) /* & C_FLAG*/);
        this.p &= 0xff;
        return w & 0xff;
    }

    public int KM_LSR(int des) {
        int w = des >> 1;
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZ(w) + (des & C_FLAG);
        return w;
    }

    public int KM_LD(int src) {
        this.p &= ~(N_FLAG | Z_FLAG);
        this.p += FLAG_NZ(src);
        this.p &= 0xff;
        return src;
    }

    public int KM_ROL(int des) {
        int w = (des << 1) + (this.p & C_FLAG);
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZ(w) + ((des >> 7) /* & C_FLAG */);
        this.p &= 0xff;
        return (w) & 0xff;
    }

    public int KM_ROR(int des) {
        int w = (des >> 1) + ((this.p & C_FLAG) << 7);
        this.p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        this.p += FLAG_NZ(w) + (des & C_FLAG);
        this.p &= 0xff;
        return w & 0xff;
    }

    public void KM_BRA(int rel) {
        this.pc = (this.pc + (rel ^ 0x80) - 0x80) & 0xffff;
        KI_ADDCLOCK(2);
    }

    public void KM_PUSH(int src) {
        writeK(BASE_OF_ZERO + 0x100 + this.s, src);
        this.s = (this.s - 1) & 0xff;
    }

    public int KM_POP() {
        this.s = (this.s + 1) & 0xff;
        return readK(BASE_OF_ZERO + 0x100 + this.s);
    }

    public int KM_TSB(int mem) {
        int w = this.a | mem;
        this.p &= ~(N_FLAG | V_FLAG | Z_FLAG);
        this.p += (mem & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
        return w;
    }

    public int KM_TRB(int mem) {
        int w = (this.a ^ 0xff) & mem;
        this.p &= ~(N_FLAG | V_FLAG | Z_FLAG);
        this.p += (mem & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
        return w;
    }

    public int KMI_PRET() {
        int saveA = this.a;
        this.a = readK(BASE_OF_ZERO + this.x);
        return saveA;
    }

    public void KMI_POSTT(int saveA) {
        writeK(BASE_OF_ZERO + this.x, this.a);
        this.a = saveA;
        KI_ADDCLOCK(3);
    }

    public void KM_TST(int imm, int mem) {
        int w = imm & mem;
        this.p &= ~(N_FLAG | V_FLAG | Z_FLAG);
        this.p += (mem & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
    }

    // ADC

    public void opcode61() {
        KMI_ADC(readK(KA_INDX()));
    }

    public void opcode65() {
        KMI_ADC(readK(KA_ZP()));
    }

    public void opcode69() {
        KMI_ADC(readK(KA_IMM()));
    }

    public void opcode6D() {
        KMI_ADC(readK(KA_ABS()));
    }

    public void opcode71() {
        KMI_ADC(readK(KA_INDY()));
    }

    public void opcode75() {
        KMI_ADC(readK(KA_ZPX()));
    }

    public void opcode79() {
        KMI_ADC(readK(KA_ABSY()));
    }

    public void opcode7D() {
        KMI_ADC(readK(KA_ABSX()));
    }

    public void opcode72() {
        KMI_ADC(readK(KA_IND()));
    }

    public void D_Opco61() {
        KMI_ADC_D(readK(KA_INDX()));
    }

    public void D_Opco65() {
        KMI_ADC_D(readK(KA_ZP()));
    }

    public void D_Opco69() {
        KMI_ADC_D(readK(KA_IMM()));
    }

    public void D_Opco6D() {
        KMI_ADC_D(readK(KA_ABS()));
    }

    public void D_Opco71() {
        KMI_ADC_D(readK(KA_INDY()));
    }

    public void D_Opco75() {
        KMI_ADC_D(readK(KA_ZPX()));
    }

    public void D_Opco79() {
        KMI_ADC_D(readK(KA_ABSY()));
    }

    public void D_Opco7D() {
        KMI_ADC_D(readK(KA_ABSX()));
    }

    public void D_Opco72() {
        KMI_ADC_D(readK(KA_IND()));
    }

    public void T_opco61() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_INDX()));
        KMI_POSTT(saveA);
    }

    public void T_opco65() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_ZP()));
        KMI_POSTT(saveA);
    }

    public void T_opco69() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_IMM()));
        KMI_POSTT(saveA);
    }

    public void T_opco6D() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_ABS()));
        KMI_POSTT(saveA);
    }

    public void T_opco71() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_INDY()));
        KMI_POSTT(saveA);
    }

    public void T_opco75() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_ZPX()));
        KMI_POSTT(saveA);
    }

    public void T_opco79() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_ABSY()));
        KMI_POSTT(saveA);
    }

    public void T_opco7D() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_ABSX()));
        KMI_POSTT(saveA);
    }

    public void T_opco72() {
        int saveA = KMI_PRET();
        KMI_ADC(readK(KA_IND()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc61() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_INDX()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc65() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_ZP()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc69() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_IMM()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc6D() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_ABS()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc71() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_INDY()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc75() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_ZPX()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc79() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_ABSY()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc7D() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_ABSX()));
        KMI_POSTT(saveA);
    }

    public void TD_Opc72() {
        int saveA = KMI_PRET();
        KMI_ADC_D(readK(KA_IND()));
        KMI_POSTT(saveA);
    }

    // AND

    public void opcode21() {
        KM_AND(readK(KA_INDX()));
    }

    public void opcode25() {
        KM_AND(readK(KA_ZP()));
    }

    public void opcode29() {
        KM_AND(readK(KA_IMM()));
    }

    public void opcode2D() {
        KM_AND(readK(KA_ABS()));
    }

    public void opcode31() {
        KM_AND(readK(KA_INDY()));
    }

    public void opcode35() {
        KM_AND(readK(KA_ZPX()));
    }

    public void opcode39() {
        KM_AND(readK(KA_ABSY()));
    }

    public void opcode3D() {
        KM_AND(readK(KA_ABSX()));
    }

    public void opcode32() {
        KM_AND(readK(KA_IND()));
    }

    public void T_opco21() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_INDX()));
        KMI_POSTT(saveA);
    }

    public void T_opco25() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_ZP()));
        KMI_POSTT(saveA);
    }

    public void T_opco29() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_IMM()));
        KMI_POSTT(saveA);
    }

    public void T_opco2D() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_ABS()));
        KMI_POSTT(saveA);
    }

    public void T_opco31() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_INDY()));
        KMI_POSTT(saveA);
    }

    public void T_opco35() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_ZPX()));
        KMI_POSTT(saveA);
    }

    public void T_opco39() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_ABSY()));
        KMI_POSTT(saveA);
    }

    public void T_opco3D() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_ABSX()));
        KMI_POSTT(saveA);
    }

    public void T_opco32() {
        int saveA = KMI_PRET();
        KM_AND(readK(KA_IND()));
        KMI_POSTT(saveA);
    }

    // ASL

    public void opcode06() {
        int adr = KA_ZP();
        writeK(adr, KM_ASL(readK(adr)));
    }

    public void opcode0E() {
        int adr = KA_ABS();
        writeK(adr, KM_ASL(readK(adr)));
    }

    public void opcode16() {
        int adr = KA_ZPX();
        writeK(adr, KM_ASL(readK(adr)));
    }

    public void opcode1E() {
        int adr = KA_ABSX();
        writeK(adr, KM_ASL(readK(adr)));
    }

    public void opcode0A() {
        this.a = KM_ASL(this.a);
    }

    // BBRi

    public void opcode0F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 0)) == 0) KM_BRA(rel);
    }

    public void opcode1F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 1)) == 0) KM_BRA(rel);
    }

    public void opcode2F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 2)) == 0) KM_BRA(rel);
    }

    public void opcode3F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 3)) == 0) KM_BRA(rel);
    }

    public void opcode4F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 4)) == 0) KM_BRA(rel);
    }

    public void opcode5F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 5)) == 0) KM_BRA(rel);
    }

    public void opcode6F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 6)) == 0) KM_BRA(rel);
    }

    public void opcode7F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 7)) == 0) KM_BRA(rel);
    }

    // BBSi

    public void opcode8F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 0)) != 0) KM_BRA(rel);
    }

    public void opcode9F() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 1)) != 0) KM_BRA(rel);
    }

    public void opcodeAF() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 2)) != 0) KM_BRA(rel);
    }

    public void opcodeBF() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 3)) != 0) KM_BRA(rel);
    }

    public void opcodeCF() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 4)) != 0) KM_BRA(rel);
    }

    public void opcodeDF() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 5)) != 0) KM_BRA(rel);
    }

    public void opcodeEF() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 6)) != 0) KM_BRA(rel);
    }

    public void opcodeFF() {
        int adr = KA_ZP();
        int rel = readK(KA_IMM());
        if ((readK(adr) & (1 << 7)) != 0) KM_BRA(rel);
    }

    // BIT

    public void opcode24() {
        KM_BIT(readK(KA_ZP()));
    }

    public void opcode2C() {
        KM_BIT(readK(KA_ABS()));
    }

    public void opcode34() {
        KM_BIT(readK(KA_ZPX()));
    }

    public void opcode3C() {
        KM_BIT(readK(KA_ABSX()));
    }

    public void opcode89() {
        KM_BIT(readK(KA_IMM()));
    }

    // Bcc

    public void opcode10() {
        int rel = readK(KA_IMM());
        if ((this.p & N_FLAG) == 0) KM_BRA(rel);
    }

    public void opcode30() {
        int rel = readK(KA_IMM());
        if ((this.p & N_FLAG) != 0) KM_BRA(rel);
    }

    public void opcode50() {
        int rel = readK(KA_IMM());
        if ((this.p & V_FLAG) == 0) KM_BRA(rel);
    }

    public void opcode70() {
        int rel = readK(KA_IMM());
        if ((this.p & V_FLAG) != 0) KM_BRA(rel);
    }

    public void opcode90() {
        int rel = readK(KA_IMM());
        if ((this.p & C_FLAG) == 0) KM_BRA(rel);
    }

    public void opcodeB0() {
        int rel = readK(KA_IMM());
        if ((this.p & C_FLAG) != 0) KM_BRA(rel);
    }

    public void opcodeD0() {
        int rel = readK(KA_IMM());
        if ((this.p & Z_FLAG) == 0) KM_BRA(rel);
    }

    public void opcodeF0() {
        int rel = readK(KA_IMM());
        if ((this.p & Z_FLAG) != 0) KM_BRA(rel);
    }

    public void opcode80() {
        int rel = readK(KA_IMM());
        if (true) KM_BRA(rel);
    }

    // BRK

    public void opcode00() {
        this.pc = (this.pc + 1) & 0xffff;
        this.iRequest |= IRQ_BRK; // 00 - BRK
    }

    // BSR

    public void opcode44() { // 44 - BSR */
        KM_PUSH((this.pc >> 8) & 0xff); // !!! pc = NEXT - 1; !!! */
        KM_PUSH((this.pc) & 0xff);
        KM_BRA(readK(KA_IMM()));
    }

    /** CLA */
    public void opcode62() { // 62 - CLA
        this.a = 0;
    }

    /** CLX */
    public void opcode82() { // 82 - CLX
        this.x = 0;
    }

    /** CLY */
    public void opcodeC2() { // C2 - CLY
        this.y = 0;
    }

    /** CLC */
    public void opcode18() { // 18 - CLC
        this.p &= ~C_FLAG;
    }

    /** CLD */
    public void opcodeD8() { // D8 - CLD
        this.p &= ~D_FLAG;
    }

    /** CLI */
    public void opcode58() { // 58 - CLI
        this.p &= ~I_FLAG;
    }

    /** CLV */
    public void opcodeB8() { // B8 - CLV
        this.p &= ~V_FLAG;
    }

    // CMP

    public void opcodeC1() {
        KM_CMP(readK(KA_INDX()));
    }

    public void opcodeC5() {
        KM_CMP(readK(KA_ZP()));
    }

    public void opcodeC9() {
        KM_CMP(readK(KA_IMM()));
    }

    public void opcodeCD() {
        KM_CMP(readK(KA_ABS()));
    }

    public void opcodeD1() {
        KM_CMP(readK(KA_INDY()));
    }

    public void opcodeD5() {
        KM_CMP(readK(KA_ZPX()));
    }

    public void opcodeD9() {
        KM_CMP(readK(KA_ABSY()));
    }

    public void opcodeDD() {
        KM_CMP(readK(KA_ABSX()));
    }

    public void opcodeD2() {
        KM_CMP(readK(KA_IND()));
    }

    // CPX

    public void opcodeE0() {
        KM_CPX(readK(KA_IMM()));
    }

    public void opcodeE4() {
        KM_CPX(readK(KA_ZP()));
    }

    public void opcodeEC() {
        KM_CPX(readK(KA_ABS()));
    }

    // CPY

    public void opcodeC0() {
        KM_CPY(readK(KA_IMM()));
    }

    public void opcodeC4() {
        KM_CPY(readK(KA_ZP()));
    }

    public void opcodeCC() {
        KM_CPY(readK(KA_ABS()));
    }

    // DEC

    public void opcodeC6() {
        int adr = KA_ZP();
        writeK(adr, KM_DEC(readK(adr)));
    }

    public void opcodeCE() {
        int adr = KA_ABS();
        writeK(adr, KM_DEC(readK(adr)));
    }

    public void opcodeD6() {
        int adr = KA_ZPX();
        writeK(adr, KM_DEC(readK(adr)));
    }

    public void opcodeDE() {
        int adr = KA_ABSX();
        writeK(adr, KM_DEC(readK(adr)));
    }

    public void opcode3A() { // 3A - DEA
        this.a = KM_DEC(this.a);
    }

    public void opcodeCA() { // CA - DEX
        this.x = KM_DEC(this.x);
    }

    public void opcode88() { // 88 - DEY
        this.y = KM_DEC(this.y);
    }

    // EOR

    public void opcode41() {
        KM_EOR(readK(KA_INDX()));
    }

    public void opcode45() {
        KM_EOR(readK(KA_ZP()));
    }

    public void opcode49() {
        KM_EOR(readK(KA_IMM()));
    }

    public void opcode4D() {
        KM_EOR(readK(KA_ABS()));
    }

    public void opcode51() {
        KM_EOR(readK(KA_INDY()));
    }

    public void opcode55() {
        KM_EOR(readK(KA_ZPX()));
    }

    public void opcode59() {
        KM_EOR(readK(KA_ABSY()));
    }

    public void opcode5D() {
        KM_EOR(readK(KA_ABSX()));
    }

    public void opcode52() {
        KM_EOR(readK(KA_IND()));
    }

    public void T_opco41() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_INDX()));
        KMI_POSTT(saveA);
    }

    public void T_opco45() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_ZP()));
        KMI_POSTT(saveA);
    }

    public void T_opco49() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_IMM()));
        KMI_POSTT(saveA);
    }

    public void T_opco4D() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_ABS()));
        KMI_POSTT(saveA);
    }

    public void T_opco51() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_INDY()));
        KMI_POSTT(saveA);
    }

    public void T_opco55() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_ZPX()));
        KMI_POSTT(saveA);
    }

    public void T_opco59() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_ABSY()));
        KMI_POSTT(saveA);
    }

    public void T_opco5D() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_ABSX()));
        KMI_POSTT(saveA);
    }

    public void T_opco52() {
        int saveA = KMI_PRET();
        KM_EOR(readK(KA_IND()));
        KMI_POSTT(saveA);
    }

    // INC

    public void opcodeE6() {
        int adr = KA_ZP();
        writeK(adr, KM_INC(readK(adr)));
    }

    public void opcodeEE() {
        int adr = KA_ABS();
        writeK(adr, KM_INC(readK(adr)));
    }

    public void opcodeF6() {
        int adr = KA_ZPX();
        writeK(adr, KM_INC(readK(adr)));
    }

    public void opcodeFE() {
        int adr = KA_ABSX();
        writeK(adr, KM_INC(readK(adr)));
    }

    public void opcode1A() { // 1A - INA
        this.a = KM_INC(this.a);
    }

    public void opcodeE8() { // E8 - INX
        this.x = KM_INC(this.x);
    }

    public void opcodeC8() { // C8 - INY
        this.y = KM_INC(this.y);
    }

    // JMP

    public void opcode4C() {
        this.pc = KI_READWORD(KA_IMM16());
    }

    public void opcode6C() {
        this.pc = KI_READWORD(KA_ABS());
    }

    public void opcode7C() {
        this.pc = KI_READWORD(KA_ABSX());
    }

    /** JSR */
    public void opcode20() { // 20 - JSR
        int adr = KA_IMM();
        KM_PUSH((this.pc >> 8) & 0xff);   /* !!! pc = NEXT - 1; !!! */
        KM_PUSH((this.pc) & 0xff);
        this.pc = KI_READWORD(adr);
    }

    // LDA

    public void opcodeA1() {
        this.a = KM_LD(readK(KA_INDX()));
    }

    public void opcodeA5() {
        this.a = KM_LD(readK(KA_ZP()));
    }

    public void opcodeA9() {
        this.a = KM_LD(readK(KA_IMM()));
    }

    public void opcodeAD() {
        this.a = KM_LD(readK(KA_ABS()));
    }

    public void opcodeB1() {
        this.a = KM_LD(readK(KA_INDY()));
    }

    public void opcodeB5() {
        this.a = KM_LD(readK(KA_ZPX()));
    }

    public void opcodeB9() {
        this.a = KM_LD(readK(KA_ABSY()));
    }

    public void opcodeBD() {
        this.a = KM_LD(readK(KA_ABSX()));
    }

    public void opcodeB2() {
        this.a = KM_LD(readK(KA_IND()));
    }

    // LDX

    public void opcodeA2() {
        this.x = KM_LD(readK(KA_IMM()));
    }

    public void opcodeA6() {
        this.x = KM_LD(readK(KA_ZP()));
    }

    public void opcodeAE() {
        this.x = KM_LD(readK(KA_ABS()));
    }

    public void opcodeB6() {
        this.x = KM_LD(readK(KA_ZPY()));
    }

    public void opcodeBE() {
        this.x = KM_LD(readK(KA_ABSY()));
    }

    // LDY

    public void opcodeA0() {
        this.y = KM_LD(readK(KA_IMM()));
    }

    public void opcodeA4() {
        this.y = KM_LD(readK(KA_ZP()));
    }

    public void opcodeAC() {
        this.y = KM_LD(readK(KA_ABS()));
    }

    public void opcodeB4() {
        this.y = KM_LD(readK(KA_ZPX()));
    }

    public void opcodeBC() {
        this.y = KM_LD(readK(KA_ABSX()));
    }

    // LSR

    public void opcode46() {
        int adr = KA_ZP();
        writeK(adr, KM_LSR(readK(adr)));
    }

    public void opcode4E() {
        int adr = KA_ABS();
        writeK(adr, KM_LSR(readK(adr)));
    }

    public void opcode56() {
        int adr = KA_ZPX();
        writeK(adr, KM_LSR(readK(adr)));
    }

    public void opcode5E() {
        int adr = KA_ABSX();
        writeK(adr, KM_LSR(readK(adr)));
    }

    public void opcode4A() { // 4A - LSR - Accumulator
        this.a = KM_LSR(this.a);
    }

    /** NOP */
    public void opcodeEA() { // EA - NOP
    }

    // ORA

    public void opcode01() {
        KM_ORA(readK(KA_INDX()));
    }

    public void opcode05() {
        KM_ORA(readK(KA_ZP()));
    }

    public void opcode09() {
        KM_ORA(readK(KA_IMM()));
    }

    public void opcode0D() {
        KM_ORA(readK(KA_ABS()));
    }

    public void opcode11() {
        KM_ORA(readK(KA_INDY()));
    }

    public void opcode15() {
        KM_ORA(readK(KA_ZPX()));
    }

    public void opcode19() {
        KM_ORA(readK(KA_ABSY()));
    }

    public void opcode1D() {
        KM_ORA(readK(KA_ABSX()));
    }

    public void opcode12() {
        KM_ORA(readK(KA_IND()));
    }

    public void T_opco01() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_INDX()));
        KMI_POSTT(saveA);
    }

    public void T_opco05() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_ZP()));
        KMI_POSTT(saveA);
    }

    public void T_opco09() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_IMM()));
        KMI_POSTT(saveA);
    }

    public void T_opco0D() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_ABS()));
        KMI_POSTT(saveA);
    }

    public void T_opco11() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_INDY()));
        KMI_POSTT(saveA);
    }

    public void T_opco15() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_ZPX()));
        KMI_POSTT(saveA);
    }

    public void T_opco19() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_ABSY()));
        KMI_POSTT(saveA);
    }

    public void T_opco1D() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_ABSX()));
        KMI_POSTT(saveA);
    }

    public void T_opco12() {
        int saveA = KMI_PRET();
        KM_ORA(readK(KA_IND()));
        KMI_POSTT(saveA);
    }

    // PHr PLr

    public void opcode48() { // 48 - PHA
        KM_PUSH(this.a);
    }

    public void opcode08() { // 08 - PHP
        KM_PUSH((this.p | B_FLAG | R_FLAG) & ~T_FLAG);
    }

    public void opcode68() { // 68 - PLA
        this.a = KM_LD(KM_POP());
    }

    public void opcode28() { // 28 - PLP
        this.p = KM_POP() & ~T_FLAG;
    }

    public void opcodeDA() { // DA - PHX
        KM_PUSH(this.x);
    }

    public void opcode5A() { // 5A - PHY
        KM_PUSH(this.y);
    }

    public void opcodeFA() { // FA - PLX
        this.x = KM_LD(KM_POP());
    }

    public void opcode7A() { // 7A - PLY
        this.y = KM_LD(KM_POP());
    }

    /* RMBi */

    public void opcode07() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 0)));
    }

    public void opcode17() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 1)));
    }

    public void opcode27() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 2)));
    }

    public void opcode37() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 3)));
    }

    public void opcode47() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 4)));
    }

    public void opcode57() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 5)));
    }

    public void opcode67() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 6)));
    }

    public void opcode77() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) & (~(1 << 7)));
    }

    /* SMBi */

    public void opcode87() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 0));
    }

    public void opcode97() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 1));
    }

    public void opcodeA7() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 2));
    }

    public void opcodeB7() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 3));
    }

    public void opcodeC7() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 4));
    }

    public void opcodeD7() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 5));
    }

    public void opcodeE7() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 6));
    }

    public void opcodeF7() {
        int adr = KA_ZP();
        writeK(adr, readK(adr) | (1 << 7));
    }

    // ROL

    public void opcode26() {
        int adr = KA_ZP();
        writeK(adr, KM_ROL(readK(adr)));
    }

    public void opcode2E() {
        int adr = KA_ABS();
        writeK(adr, KM_ROL(readK(adr)));
    }

    public void opcode36() {
        int adr = KA_ZPX();
        writeK(adr, KM_ROL(readK(adr)));
    }

    public void opcode3E() {
        int adr = KA_ABSX();
        writeK(adr, KM_ROL(readK(adr)));
    }

    public void opcode2A() { // 2A - ROL - Accumulator
        this.a = KM_ROL(this.a);
    }

    // ROR

    public void opcode66() {
        int adr = KA_ZP();
        writeK(adr, KM_ROR(readK(adr)));
    }

    public void opcode6E() {
        int adr = KA_ABS();
        writeK(adr, KM_ROR(readK(adr)));
    }

    public void opcode76() {
        int adr = KA_ZPX();
        writeK(adr, KM_ROR(readK(adr)));
    }

    public void opcode7E() {
        int adr = KA_ABSX();
        writeK(adr, KM_ROR(readK(adr)));
    }

    public void opcode6A() { // 6A - ROR - Accumulator
        this.a = KM_ROR(this.a);
    }

    public void opcode40() { // 40 - RTI

        this.p = KM_POP();
        this.pc = KM_POP();
        this.pc += KM_POP() << 8;
    }

    public void opcode60() { // 60 - RTS
        this.pc = KM_POP();
        this.pc += KM_POP() << 8;
        this.pc = (this.pc + 1) & 0xffff;
    }

    public void opcode22() { // 22 - SAX
        int temp = this.a;
        this.a = this.x;
        this.x = temp;
    }

    public void opcode42() { // 42 - SAY
        int temp = this.a;
        this.a = this.y;
        this.y = temp;
    }

    public void opcode02() { // 02 - SXY
        int temp = this.y;
        this.y = this.x;
        this.x = temp;
    }

    /* SBC  */

    public void opcodeE1() {
        KMI_SBC(readK(KA_INDX()));
    }

    public void opcodeE5() {
        KMI_SBC(readK(KA_ZP()));
    }

    public void opcodeE9() {
        KMI_SBC(readK(KA_IMM()));
    }

    public void opcodeED() {
        KMI_SBC(readK(KA_ABS()));
    }

    public void opcodeF1() {
        KMI_SBC(readK(KA_INDY()));
    }

    public void opcodeF5() {
        KMI_SBC(readK(KA_ZPX()));
    }

    public void opcodeF9() {
        KMI_SBC(readK(KA_ABSY()));
    }

    public void opcodeFD() {
        KMI_SBC(readK(KA_ABSX()));
    }

    public void opcodeF2() {
        KMI_SBC(readK(KA_IND()));
    }

    public void D_OpcoE1() {
        KMI_SBC_D(readK(KA_INDX()));
    }

    public void D_OpcoE5() {
        KMI_SBC_D(readK(KA_ZP()));
    }

    public void D_OpcoE9() {
        KMI_SBC_D(readK(KA_IMM()));
    }

    public void D_OpcoED() {
        KMI_SBC_D(readK(KA_ABS()));
    }

    public void D_OpcoF1() {
        KMI_SBC_D(readK(KA_INDY()));
    }

    public void D_OpcoF5() {
        KMI_SBC_D(readK(KA_ZPX()));
    }

    public void D_OpcoF9() {
        KMI_SBC_D(readK(KA_ABSY()));
    }

    public void D_OpcoFD() {
        KMI_SBC_D(readK(KA_ABSX()));
    }

    public void D_OpcoF2() {
        KMI_SBC_D(readK(KA_IND()));
    }

    /* SEC */
    public void opcode38() { // 38 - SEC
        this.p |= C_FLAG;
    }

    /* SED */
    public void opcodeF8() { // F8 - SED
        this.p |= D_FLAG;
    }

    /* SEI */
    public void opcode78() { // 78 - SEI
        this.p |= I_FLAG;
    }

    /* SET */
    public void opcodeF4() { // F4 - SET
        this.p |= T_FLAG;
    }

    public void opcode03() { // 03 - ST0
        write6270K(0, readK(KA_IMM()));
    }

    public void opcode13() { // 13 - ST1
        write6270K(2, readK(KA_IMM()));
    }

    public void opcode23() { // 23 - ST2
        write6270K(3, readK(KA_IMM()));
    }

    /* STA */

    public void opcode81() {
        writeK(KA_INDX(), this.a);
    }

    public void opcode85() {
        writeK(KA_ZP(), this.a);
    }

    public void opcode8D() {
        writeK(KA_ABS(), this.a);
    }

    public void opcode91() {
        writeK(KA_INDY(), this.a);
    }

    public void opcode95() {
        writeK(KA_ZPX(), this.a);
    }

    public void opcode99() {
        writeK(KA_ABSY(), this.a);
    }

    public void opcode9D() {
        writeK(KA_ABSX(), this.a);
    }

    public void opcode92() {
        writeK(KA_IND(), this.a);
    }

    /* STX  */

    public void opcode86() {
        writeK(KA_ZP(), this.x);
    }

    public void opcode8E() {
        writeK(KA_ABS(), this.x);
    }

    public void opcode96() {
        writeK(KA_ZPY(), this.x);
    }

    /* STY  */

    public void opcode84() {
        writeK(KA_ZP(), this.y);
    }

    public void opcode8C() {
        writeK(KA_ABS(), this.y);
    }

    public void opcode94() {
        writeK(KA_ZPX(), this.y);
    }

    /* STZ  */

    public void opcode64() {
        writeK(KA_ZP(), 0);
    }

    public void opcode9C() {
        writeK(KA_ABS(), 0);
    }

    public void opcode74() {
        writeK(KA_ZPX(), 0);
    }

    public void opcode9E() {
        writeK(KA_ABSX(), 0);
    }

    /** TAMi  */
    public void opcode53() { // 53 - TAMi
        writeMPRK(readK(KA_IMM()), this.a);
    }

    /** TMAi */
    public void opcode43() { // 43 - TMAi
        this.a = readMPRK(readK(KA_IMM()));
    }

    // TRB

    public void opcode14() {
        int adr = KA_ZP();
        writeK(adr, KM_TRB(readK(adr)));
    }

    public void opcode1C() {
        int adr = KA_ABS();
        writeK(adr, KM_TRB(readK(adr)));
    }

    // TSB

    public void opcode04() {
        int adr = KA_ZP();
        writeK(adr, KM_TSB(readK(adr)));
    }

    public void opcode0C() {
        int adr = KA_ABS();
        writeK(adr, KM_TSB(readK(adr)));
    }

    // TST

    public void opcode83() {
        int imm = readK(KA_IMM());
        KM_TST(imm, readK(KA_ZP()));
    }

    public void opcode93() {
        int imm = readK(KA_IMM());
        KM_TST(imm, readK(KA_ABS()));
    }

    public void opcodeA3() {
        int imm = readK(KA_IMM());
        KM_TST(imm, readK(KA_ZPX()));
    }

    public void opcodeB3() {
        int imm = readK(KA_IMM());
        KM_TST(imm, readK(KA_ABSX()));
    }

//#endif

    /** TAX */
    public void opcodeAA() { // AA - TAX
        this.x = KM_LD(this.a);
    }

    /** TAY */
    public void opcodeA8() { // A8 - TAY
        this.y = KM_LD(this.a);
    }

    /** TSX */
    public void opcodeBA() { // BA - TSX
        this.x = KM_LD(this.s);
    }

    /** TXA */
    public void opcode8A() { // 8A - TXA
        this.a = KM_LD(this.x);
    }

    /** TXS */
    public void opcode9A() { // 9A - TXS
        this.s = this.x;
    }

    /** TYA */
    public void opcode98() { // 98 - TYA
        this.a = KM_LD(this.y);
    }

//#if BUILD_HUC6280

    public void opcode73() { // 73 - TII
        int src, des, len;
        src = KI_READWORD(KA_IMM16());
        des = KI_READWORD(KA_IMM16());
        len = KI_READWORD(KA_IMM16());
        KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
        do {
            writeK(des, readK(src));
            src = (src + 1) & 0xffff;
            des = (des + 1) & 0xffff;
            len = (len - 1) & 0xffff;
        } while (len != 0);
    }

    public void opcodeC3() { // C3 - TDD
        int src, des, len;
        src = KI_READWORD(KA_IMM16());
        des = KI_READWORD(KA_IMM16());
        len = KI_READWORD(KA_IMM16());
        KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
        do {
            writeK(des, readK(src));
            src = (src - 1) & 0xffff;
            des = (des - 1) & 0xffff;
            len = (len - 1) & 0xffff;
        } while (len != 0);
    }

    public void opcodeD3() { // D3 - TIN
        int src, des, len;
        src = KI_READWORD(KA_IMM16());
        des = KI_READWORD(KA_IMM16());
        len = KI_READWORD(KA_IMM16());
        KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
        do {
            writeK(des, readK(src));
            src = (src + 1) & 0xffff;
            len = (len - 1) & 0xffff;
        } while (len != 0);
    }

    public void opcodeE3() { // E3 - TIA
        int add = 1;
        int src, des, len;
        src = KI_READWORD(KA_IMM16());
        des = KI_READWORD(KA_IMM16());
        len = KI_READWORD(KA_IMM16());
        KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
        do {
            writeK(des, readK(src));
            src = (src + 1) & 0xffff;
            des = (des + add) & 0xffff;
            add = -add;
            len = (len - 1) & 0xffff;
        } while (len != 0);
    }

    public void opcodeF3() { // F3 - TAI
        int add = 1;
        int src, des, len;
        src = KI_READWORD(KA_IMM16());
        des = KI_READWORD(KA_IMM16());
        len = KI_READWORD(KA_IMM16());
        KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
        do {
            writeK(des, readK(src));
            src = (src + add) & 0xffff;
            des = (des + 1) & 0xffff;
            add = -add;
            len = (len - 1) & 0xffff;
        } while (len != 0);
    }

    public void opcode54() { // 54 - CSL
        this.lowClockMode = 1;
    }

    public void opcodeD4() { // D4 - CSH
        this.lowClockMode = 0;
    }

    /**
     * OotakeHuC6280 clock cycle table
     *
     * -0         undefined OP-code
     * BRK(#$00)  +7 by Interrupt
     */
    public final byte[] cl_table = {
            /* L 0  1  2  3  4  5  6  7  8  9  a  B  C  D  E  F     H */
            1, 7, 3, 4, 6, 4, 6, 7, 3, 2, 2, -0, 7, 5, 7, 6, /* 0 */
            2, 7, 7, 4, 6, 4, 6, 7, 2, 5, 2, -0, 7, 5, 7, 6, /* 1 */
            7, 7, 3, 4, 4, 4, 6, 7, 3, 2, 2, -0, 5, 5, 7, 6, /* 2 */
            2, 7, 7, -0, 4, 4, 6, 7, 2, 5, 2, -0, 5, 5, 7, 6, /* 3 */
            7, 7, 3, 4, 8, 4, 6, 7, 3, 2, 2, -0, 4, 5, 7, 6, /* 4 */
            2, 7, 7, 5, 2, 4, 6, 7, 2, 5, 3, -0, -0, 5, 7, 6, /* 5 */
            7, 7, 2, -0, 4, 4, 6, 7, 3, 2, 2, -0, 7, 5, 7, 6, /* 6 */
            2, 7, 7, 17, 4, 4, 6, 7, 2, 5, 3, -0, 7, 5, 7, 6, /* 7 */
            2, 7, 2, 7, 4, 4, 4, 7, 2, 2, 2, -0, 5, 5, 5, 6, /* 8 */
            2, 7, 7, 8, 4, 4, 4, 7, 2, 5, 2, -0, 5, 5, 5, 6, /* 9 */
            2, 7, 2, 7, 4, 4, 4, 7, 2, 2, 2, -0, 5, 5, 5, 6, /* a */
            2, 7, 7, 8, 4, 4, 4, 7, 2, 5, 2, -0, 5, 5, 5, 6, /* B */
            2, 7, 2, 17, 4, 4, 6, 7, 2, 2, 2, -0, 5, 5, 7, 6, /* C */
            2, 7, 7, 17, 2, 4, 6, 7, 2, 5, 3, -0, -0, 5, 7, 6, /* D */
            2, 7, -0, 17, 4, 4, 6, 7, 2, 2, 2, -0, 5, 5, 7, 6, /* E */
            2, 7, 7, 17, 2, 4, 6, 7, 2, 5, 3, -0, -0, 5, 7, 6, /* F */
    };

    public void K_OPEXEC() {
        int opcode = this.lastCode = readK(KAI_IMM());
        KI_ADDCLOCK(cl_table[opcode]);
        switch (opcode) {

        // OP__(00)    OPt_(01)    OPxx(02)    OPxx(04)    OPt_(05)    OP__(06)
        case 0x00:
            opcode00();
            break;
        case 0x01:
            if ((this.p & T_FLAG) != 0) T_opco01();
            else opcode01();
            break;
        case 0x05:
            if ((this.p & T_FLAG) != 0) T_opco05();
            else opcode05();
            break;
        case 0x06:
            opcode06();
            break;

        // OP__(08)    OPt_(09)    OP__(0A)    OPxx(0C)    OPt_(0D)    OP__(0E)
        case 0x08:
            opcode08();
            break;
        case 0x09:
            if ((this.p & T_FLAG) != 0) T_opco09();
            else opcode09();
            break;
        case 0x0A:
            opcode0A();
            break;
        case 0x0D:
            if ((this.p & T_FLAG) != 0) T_opco0D();
            else opcode0D();
            break;
        case 0x0E:
            opcode0E();
            break;

        // OP__(10)    OPt_(11)    OPxx(12)    OPxx(14)    OPt_(15)    OP__(16)
        case 0x10:
            opcode10();
            break;
        case 0x11:
            if ((this.p & T_FLAG) != 0) T_opco11();
            else opcode11();
            break;
        case 0x15:
            if ((this.p & T_FLAG) != 0) T_opco15();
            else opcode15();
            break;
        case 0x16:
            opcode16();
            break;

        // OP__(18)    OPt_(19)    OPxx(1A)    OPxx(1C)    OPt_(1D)    OP__(1E)
        case 0x18:
            opcode18();
            break;
        case 0x19:
            if ((this.p & T_FLAG) != 0) T_opco19();
            else opcode19();
            break;
        case 0x1D:
            if ((this.p & T_FLAG) != 0) T_opco1D();
            else opcode1D();
            break;
        case 0x1E:
            opcode1E();
            break;

        // OP__(20)    OP__(21)    OPxx(22)    OP__(24)    OP__(25)    OP__(26)
        case 0x20:
            opcode20();
            break;
        case 0x21:
            opcode21();
            break;
        case 0x24:
            opcode24();
            break;
        case 0x25:
            opcode25();
            break;
        case 0x26:
            opcode26();
            break;

        // OP__(28)    OP__(29)    OP__(2A)    OP__(2C)    OP__(2D)    OP__(2E)
        case 0x28:
            opcode28();
            break;
        case 0x29:
            opcode29();
            break;
        case 0x2A:
            opcode2A();
            break;
        case 0x2C:
            opcode2C();
            break;
        case 0x2D:
            opcode2D();
            break;
        case 0x2E:
            opcode2E();
            break;

        // OP__(30)    OPt_(31)    OPxx(32)    OPxx(34)    OPt_(35)    OP__(36)
        case 0x30:
            opcode30();
            break;
        case 0x31:
            if ((this.p & T_FLAG) != 0) T_opco31();
            else opcode31();
            break;
        case 0x35:
            if ((this.p & T_FLAG) != 0) T_opco35();
            else opcode35();
            break;
        case 0x36:
            opcode36();
            break;

        // OP__(38)    OPt_(39)    OPxx(3A)    OPxx(3C)    OPt_(3D)    OP__(3E)
        case 0x38:
            opcode38();
            break;
        case 0x39:
            if ((this.p & T_FLAG) != 0) T_opco39();
            else opcode39();
            break;
        case 0x3D:
            if ((this.p & T_FLAG) != 0) T_opco3D();
            else opcode3D();
            break;
        case 0x3E:
            opcode3E();
            break;

        // OP__(40)    OPt_(41)    OPxx(42)    OPxx(44)    OPt_(45)    OP__(46)
        case 0x40:
            opcode40();
            break;
        case 0x41:
            if ((this.p & T_FLAG) != 0) T_opco41();
            else opcode41();
            break;
        case 0x45:
            if ((this.p & T_FLAG) != 0) T_opco45();
            else opcode45();
            break;
        case 0x46:
            opcode46();
            break;

        // OP__(48)    OPt_(49)    OP__(4A)    OP__(4C)    OPt_(4D)    OP__(4E)
        case 0x48:
            opcode48();
            break;
        case 0x49:
            if ((this.p & T_FLAG) != 0) T_opco49();
            else opcode49();
            break;
        case 0x4A:
            opcode4A();
            break;
        case 0x4C:
            opcode4C();
            break;
        case 0x4D:
            if ((this.p & T_FLAG) != 0) T_opco4D();
            else opcode4D();
            break;
        case 0x4E:
            opcode4E();
            break;

        // OP__(50)    OPt_(51)    OPxx(52)    OPxx(54)    OPt_(55)    OP__(56)
        case 0x50:
            opcode50();
            break;
        case 0x51:
            if ((this.p & T_FLAG) != 0) T_opco51();
            else opcode51();
            break;
        case 0x55:
            if ((this.p & T_FLAG) != 0) T_opco55();
            else opcode55();
            break;
        case 0x56:
            opcode56();
            break;

        // OP__(58)    OPt_(59)    OPxx(5A)    OPxx(5C)    OPt_(5D)    OP__(5E)
        case 0x58:
            opcode58();
            break;
        case 0x59:
            if ((this.p & T_FLAG) != 0) T_opco59();
            else opcode59();
            break;
        case 0x5D:
            if ((this.p & T_FLAG) != 0) T_opco5D();
            else opcode5D();
            break;
        case 0x5E:
            opcode5E();
            break;

        // OP__(60)    OPtd(61)    OPxx(62)    OPxx(64)    OPtd(65)    OP__(66)
        case 0x60:
            opcode60();
            break;
        case 0x61:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc61();
            else T_opco61();
            else if ((this.p & D_FLAG) != 0) D_Opco61();
            else opcode61();
            break;
        case 0x65:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc65();
            else T_opco65();
            else if ((this.p & D_FLAG) != 0) D_Opco65();
            else opcode65();
            break;
        case 0x66:
            opcode66();
            break;

        // OP__(68)    OPtd(69)    OP__(6A)    OP__(6C)    OPtd(6D)    OP__(6E)
        case 0x68:
            opcode68();
            break;
        case 0x69:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc69();
            else T_opco69();
            else if ((this.p & D_FLAG) != 0) D_Opco69();
            else opcode69();
            break;
        case 0x6A:
            opcode6A();
            break;
        case 0x6C:
            opcode6C();
            break;
        case 0x6D:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc6D();
            else T_opco6D();
            else if ((this.p & D_FLAG) != 0) D_Opco6D();
            else opcode6D();
            break;
        case 0x6E:
            opcode6E();
            break;

        // OP__(70)    OPtd(71)    OPxx(72)    OPxx(74)    OPtd(75)    OP__(76)
        case 0x70:
            opcode70();
            break;
        case 0x71:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc71();
            else T_opco71();
            else if ((this.p & D_FLAG) != 0) D_Opco71();
            else opcode71();
            break;
        case 0x75:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc75();
            else T_opco75();
            else if ((this.p & D_FLAG) != 0) D_Opco75();
            else opcode75();
            break;
        case 0x76:
            opcode76();
            break;

        // OP__(78)    OPtd(79)    OPxx(7A)    OPxx(7C)    OPtd(7D)    OP__(7E)
        case 0x78:
            opcode78();
            break;
        case 0x79:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc79();
            else T_opco79();
            else if ((this.p & D_FLAG) != 0) D_Opco79();
            else opcode79();
            break;
        case 0x7D:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc7D();
            else T_opco7D();
            else if ((this.p & D_FLAG) != 0) D_Opco7D();
            else opcode7D();
            break;
        case 0x7E:
            opcode7E();
            break;

        // OPxx(80)    OP__(81)    OPxx(82)    OP__(84)    OP__(85)    OP__(86)
        case 0x81:
            opcode81();
            break;
        case 0x84:
            opcode84();
            break;
        case 0x85:
            opcode85();
            break;
        case 0x86:
            opcode86();
            break;

        // OP__(88)    OPxx(89)    OP__(8A)    OP__(8C)    OP__(8D)    OP__(8E)
        case 0x88:
            opcode88();
            break;
        case 0x8A:
            opcode8A();
            break;
        case 0x8C:
            opcode8C();
            break;
        case 0x8D:
            opcode8D();
            break;
        case 0x8E:
            opcode8E();
            break;

        // OP__(90)    OP__(91)    OPxx(92)    OP__(94)    OP__(95)    OP__(96)
        case 0x90:
            opcode90();
            break;
        case 0x91:
            opcode91();
            break;
        case 0x94:
            opcode94();
            break;
        case 0x95:
            opcode95();
            break;
        case 0x96:
            opcode96();
            break;

        // OP__(98)    OP__(99)    OP__(9A)    OPxx(9C)    OP__(9D)    OPxx(9E)
        case 0x98:
            opcode98();
            break;
        case 0x99:
            opcode99();
            break;
        case 0x9A:
            opcode9A();
            break;
        case 0x9D:
            opcode9D();
            break;

        // OP__(A0)    OP__(A1)    OP__(A2)    OP__(A4)    OP__(A5)    OP__(A6)
        case 0xA0:
            opcodeA0();
            break;
        case 0xA1:
            opcodeA1();
            break;
        case 0xA2:
            opcodeA2();
            break;
        case 0xA4:
            opcodeA4();
            break;
        case 0xA5:
            opcodeA5();
            break;
        case 0xA6:
            opcodeA6();
            break;

        // OP__(A8)    OP__(A9)    OP__(AA)    OP__(AC)    OP__(AD)    OP__(AE)
        case 0xA8:
            opcodeA8();
            break;
        case 0xA9:
            opcodeA9();
            break;
        case 0xAA:
            opcodeAA();
            break;
        case 0xAC:
            opcodeAC();
            break;
        case 0xAD:
            opcodeAD();
            break;
        case 0xAE:
            opcodeAE();
            break;

        // OP__(B0)    OP__(B1)    OPxx(B2)    OP__(B4)    OP__(B5)    OP__(B6)
        case 0xB0:
            opcodeB0();
            break;
        case 0xB1:
            opcodeB1();
            break;
        case 0xB4:
            opcodeB4();
            break;
        case 0xB5:
            opcodeB5();
            break;
        case 0xB6:
            opcodeB6();
            break;

        // OP__(B8)    OP__(B9)    OP__(BA)    OP__(BC)    OP__(BD)    OP__(BE)
        case 0xB8:
            opcodeB8();
            break;
        case 0xB9:
            opcodeB9();
            break;
        case 0xBA:
            opcodeBA();
            break;
        case 0xBC:
            opcodeBC();
            break;
        case 0xBD:
            opcodeBD();
            break;
        case 0xBE:
            opcodeBE();
            break;

        // OP__(C0)    OP__(C1)    OPxx(C2)    OP__(C4)    OP__(C5)    OP__(C6)
        case 0xC0:
            opcodeC0();
            break;
        case 0xC1:
            opcodeC1();
            break;
        case 0xC4:
            opcodeC4();
            break;
        case 0xC5:
            opcodeC5();
            break;
        case 0xC6:
            opcodeC6();
            break;

        // OP__(C8)    OP__(C9)    OP__(CA)    OP__(CC)    OP__(CD)    OP__(CE)
        case 0xC8:
            opcodeC8();
            break;
        case 0xC9:
            opcodeC9();
            break;
        case 0xCA:
            opcodeCA();
            break;
        case 0xCC:
            opcodeCC();
            break;
        case 0xCD:
            opcodeCD();
            break;
        case 0xCE:
            opcodeCE();
            break;

        // OP__(D0)    OP__(D1)    OPxx(D2)    OPxx(D4)    OP__(D5)    OP__(D6)
        case 0xD0:
            opcodeD0();
            break;
        case 0xD1:
            opcodeD1();
            break;
        case 0xD5:
            opcodeD5();
            break;
        case 0xD6:
            opcodeD6();
            break;

        // OP__(D8)    OP__(D9)    OPxx(DA)    OPxx(DC)    OP__(DD)    OP__(DE)
        case 0xD8:
            opcodeD8();
            break;
        case 0xD9:
            opcodeD9();
            break;
        case 0xDD:
            opcodeDD();
            break;
        case 0xDE:
            opcodeDE();
            break;

        // OP__(E0)    OP_d(E1)    OPxx(E2)    OP__(E4)    OP_d(E5)    OP__(E6)
        case 0xE0:
            opcodeE0();
            break;
        case 0xE1:
            if ((this.p & D_FLAG) != 0) D_OpcoE1();
            else opcodeE1();
            break;
        case 0xE4:
            opcodeE4();
            break;
        case 0xE5:
            if ((this.p & D_FLAG) != 0) D_OpcoE5();
            else opcodeE5();
            break;
        case 0xE6:
            opcodeE6();
            break;

        // OP__(E8)    OP_d(E9)    OP__(EA)    OP__(EC)    OP_d(ED)    OP__(EE)
        case 0xE8:
            opcodeE8();
            break;
        case 0xE9:
            if ((this.p & D_FLAG) != 0) D_OpcoE9();
            else opcodeE9();
            break;
        case 0xEA:
            opcodeEA();
            break;
        case 0xEC:
            opcodeEC();
            break;
        case 0xED:
            if ((this.p & D_FLAG) != 0) D_OpcoED();
            else opcodeED();
            break;
        case 0xEE:
            opcodeEE();
            break;

        // OP__(F0)    OP_d(F1)    OPxx(F2)    OPxx(F4)    OP_d(F5)    OP__(F6)
        case 0xF0:
            opcodeF0();
            break;
        case 0xF1:
            if ((this.p & D_FLAG) != 0) D_OpcoF1();
            else opcodeF1();
            break;
        case 0xF5:
            if ((this.p & D_FLAG) != 0) D_OpcoF5();
            else opcodeF5();
            break;
        case 0xF6:
            opcodeF6();
            break;

        // OP__(F8)    OP_d(F9)    OPxx(FA)    OPxx(FC)    OP_d(FD)    OP__(FE)
        case 0xF8:
            opcodeF8();
            break;
        case 0xF9:
            if ((this.p & D_FLAG) != 0) D_OpcoF9();
            else opcodeF9();
            break;
        case 0xFD:
            if ((this.p & D_FLAG) != 0) D_OpcoFD();
            else opcodeFD();
            break;
        case 0xFE:
            opcodeFE();
            break;

        // 34 - BIT - Zero Page,x
        // 3C - BIT - Absolute,x
        // 80 - BRA
        // 3A - DEA
        // 1A - INA
        case 0x34:
            opcode34();
            break;
        case 0x3C:
            opcode3C();
            break;
        case 0x80:
            opcode80();
            break;
        case 0x3A:
            opcode3A();
            break;
        case 0x1A:
            opcode1A();
            break;

        // 89 - BIT - Immediate
        case 0x89:
            opcode89();
            break;

        // TSB
        case 0x04:
            opcode04();
            break;
        case 0x0C:
            opcode0C();
            break;

        // TRB
        case 0x14:
            opcode14();
            break;
        case 0x1C:
            opcode1C();
            break;

        // 12 - ORA - (Indirect)
        // 32 - AND - (Indirect)
        // 52 - EOR - (Indirect)
        // 72 - ADC - (Indirect)
        // 92 - STA - (Indirect)
        // B2 - LDA - (Indirect)
        // D2 - CMP - (Indirect)
        // F2 - SBC - (Indirect)
        case 0x12:
            if ((this.p & T_FLAG) != 0) T_opco12();
            else opcode12();
            break;
        case 0x32:
            if ((this.p & T_FLAG) != 0) T_opco32();
            else opcode32();
            break;
        case 0x52:
            if ((this.p & T_FLAG) != 0) T_opco52();
            else opcode52();
            break;
        case 0x72:
            if ((this.p & T_FLAG) != 0) if ((this.p & D_FLAG) != 0) TD_Opc72();
            else T_opco72();
            else if ((this.p & D_FLAG) != 0) D_Opco72();
            else opcode72();
            break;
        case 0x92:
            opcode92();
            break;
        case 0xB2:
            opcodeB2();
            break;
        case 0xD2:
            opcodeD2();
            break;
        case 0xF2:
            if ((this.p & D_FLAG) != 0) D_OpcoF2();
            else opcodeF2();
            break;

        // PHX PHY PLX PLY
        case 0xDA:
            opcodeDA();
            break;
        case 0x5A:
            opcode5A();
            break;
        case 0xFA:
            opcodeFA();
            break;
        case 0x7A:
            opcode7A();
            break;

        // STZ
        case 0x64:
            opcode64();
            break;
        case 0x9C:
            opcode9C();
            break;
        case 0x74:
            opcode74();
            break;
        case 0x9E:
            opcode9E();
            break;

        // 7C - JMP - Absolute,x
        case 0x7C:
            opcode7C();
            break;

        // BBRi
        case 0x0F:
            opcode0F();
            break;
        case 0x1F:
            opcode1F();
            break;
        case 0x2F:
            opcode2F();
            break;
        case 0x3F:
            opcode3F();
            break;

        // OP__(4F)	OP__(5F)	OP__(6F)	OP__(7F)
        case 0x4F:
            opcode4F();
            break;
        case 0x5F:
            opcode5F();
            break;
        case 0x6F:
            opcode6F();
            break;
        case 0x7F:
            opcode7F();
            break;

        // BBSi
        case 0x8F:
            opcode8F();
            break;
        case 0x9F:
            opcode9F();
            break;
        case 0xAF:
            opcodeAF();
            break;
        case 0xBF:
            opcodeBF();
            break;

        // OP__(CF)	OP__(DF)	OP__(EF)	OP__(FF)
        case 0xCF:
            opcodeCF();
            break;
        case 0xDF:
            opcodeDF();
            break;
        case 0xEF:
            opcodeEF();
            break;
        case 0xff:
            opcodeFF();
            break;

        // 44 - BSR
        case 0x44:
            opcode44();
            break;

        // CLA CLX CLY
        case 0x62:
            opcode62();
            break;
        case 0x82:
            opcode82();
            break;
        case 0xC2:
            opcodeC2();
            break;

        // RMBi
        case 0x07:
            opcode07();
            break;
        case 0x17:
            opcode17();
            break;
        case 0x27:
            opcode27();
            break;
        case 0x37:
            opcode37();
            break;

        // OP__(47)	OP__(57)	OP__(67)	OP__(77)
        case 0x47:
            opcode47();
            break;
        case 0x57:
            opcode57();
            break;
        case 0x67:
            opcode67();
            break;
        case 0x77:
            opcode77();
            break;

        // SMBi
        case 0x87:
            opcode87();
            break;
        case 0x97:
            opcode97();
            break;
        case 0xA7:
            opcodeA7();
            break;
        case 0xB7:
            opcodeB7();
            break;

        // OP__(C7)	OP__(D7)	OP__(E7)	OP__(F7)
        case 0xC7:
            opcodeC7();
            break;
        case 0xD7:
            opcodeD7();
            break;
        case 0xE7:
            opcodeE7();
            break;
        case 0xF7:
            opcodeF7();
            break;

        // SXY SAX SAY
        case 0x02:
            opcode02();
            break;
        case 0x22:
            opcode22();
            break;
        case 0x42:
            opcode42();
            break;

        // F4 - SET
        case 0xF4:
            opcodeF4();
            break;

        // ST0 ST1 ST2
        case 0x03:
            opcode03();
            break;
        case 0x13:
            opcode13();
            break;
        case 0x23:
            opcode23();
            break;

        // TMAi TAMi
        case 0x43:
            opcode43();
            break;
        case 0x53:
            opcode53();
            break;

        // TST
        case 0x83:
            opcode83();
            break;
        case 0x93:
            opcode93();
            break;
        case 0xA3:
            opcodeA3();
            break;
        case 0xB3:
            opcodeB3();
            break;

        // block
        case 0x73:
            opcode73();
            break;
        case 0xC3:
            opcodeC3();
            break;
        case 0xD3:
            opcodeD3();
            break;
        case 0xE3:
            opcodeE3();
            break;
        case 0xF3:
            opcodeF3();
            break;

        // OP__(54)	OP__(D4)	// CSL CSH
        case 0x54:
            opcode54();
            break;
        case 0xD4:
            opcodeD4();
            break;
//#endif
        }
    }

    public void K_EXEC() {
        if (this.iRequest != 0) {
            if ((this.iRequest & IRQ_INIT) != 0) {
//#if BUILD_HUC6280
                this.lowClockMode = 1;
//#endif
                this.a = 0;
                this.x = 0;
                this.y = 0;
                this.s = 0xff;
                this.p = Z_FLAG | R_FLAG | I_FLAG;
                this.iRequest = 0;
                this.iMask = 0xffff_ffff; // ~0;
                KI_ADDCLOCK(7);
                return;
            } else if ((this.iRequest & IRQ_RESET) != 0) {
//#if BUILD_HUC6280
                this.lowClockMode = 1;
                writeMPRK(0x80, 0x00); // IPL(TOP OF ROM)
//#endif
                this.a = 0;
                this.x = 0;
                this.y = 0;
                this.s = 0xff;
                this.p = Z_FLAG | R_FLAG | I_FLAG;
                this.pc = KI_READWORD(VEC_RESET);
                this.iRequest = 0;
                this.iMask = 0xffff_ffff; // ~0;
            } else if ((this.iRequest & IRQ_NMI) != 0) {
                KM_PUSH((this.pc >> 8) & 0xff);
                KM_PUSH((this.pc) & 0xff);
                KM_PUSH(this.p | R_FLAG | B_FLAG);
//#if BUILD_M65C02 || BUILD_HUC6280
                this.p = (this.p & ~(D_FLAG | T_FLAG)) | I_FLAG;
                this.iRequest &= ~IRQ_NMI;
//#else
//                this.p = (this.p & ~T_FLAG) | I_FLAG;   // 6502 bug
//                this.iRequest &= ~(IRQ_NMI | IRQ_BRK);
//#endif
                this.pc = KI_READWORD(VEC_NMI);
                KI_ADDCLOCK(7);
            } else if ((this.iRequest & IRQ_BRK) != 0) {
                KM_PUSH((this.pc >> 8) & 0xff);
                KM_PUSH((this.pc) & 0xff);
                KM_PUSH(this.p | R_FLAG | B_FLAG);
//#if BUILD_M65C02 || BUILD_HUC6280
                this.p = (this.p & ~(D_FLAG | T_FLAG)) | I_FLAG;
//#else
//                this.p = (this.p & ~T_FLAG) | I_FLAG;   // 6502 bug
//#endif
                this.iRequest &= ~IRQ_BRK;
                this.pc = KI_READWORD(VEC_BRK);
                KI_ADDCLOCK(7);
            } else if ((this.p & I_FLAG) != 0) {
                // Interrupt disabled
            }
//#if BUILD_HUC6280
            else if ((this.iMask & this.iRequest & IRQ_INT1) != 0) {
                KM_PUSH((this.pc >> 8) & 0xff);
                KM_PUSH((this.pc) & 0xff);
                KM_PUSH(this.p | R_FLAG | B_FLAG);
                this.p = (this.p & ~(D_FLAG | T_FLAG)) | I_FLAG;
                this.pc = KI_READWORD(VEC_INT1);
                KI_ADDCLOCK(7);
            } else if ((this.iMask & this.iRequest & IRQ_TIMER) != 0) {
                KM_PUSH((this.pc >> 8) & 0xff);
                KM_PUSH((this.pc) & 0xff);
                KM_PUSH(this.p | R_FLAG | B_FLAG);
                this.p = (this.p & ~(D_FLAG | T_FLAG)) | I_FLAG;
                this.pc = KI_READWORD(VEC_TIMER);
                KI_ADDCLOCK(7);
            } else if ((this.iMask & this.iRequest & IRQ_INT) != 0) {
                KM_PUSH((this.pc >> 8) & 0xff);
                KM_PUSH((this.pc) & 0xff);
                KM_PUSH((this.p | R_FLAG) & ~B_FLAG);
                this.p = (this.p & ~(D_FLAG | T_FLAG)) | I_FLAG;
                this.pc = KI_READWORD(VEC_INT);
                KI_ADDCLOCK(7);
            }
        }
        K_OPEXEC();
    }
}
