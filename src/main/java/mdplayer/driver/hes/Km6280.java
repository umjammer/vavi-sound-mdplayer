package mdplayer.driver.hes;

import java.util.function.BiFunction;

import mdsound.Common.TriConsumer;


public class Km6280 {

    public static class K6280Context {

        public interface dlgReadHandler extends BiFunction<M_Hes.HESHES, Integer, Integer> {
        }

        public interface dlgWriterHandler extends TriConsumer<M_Hes.HESHES, Integer, Integer> {
        }

        public int A; /** Accumulator */
        public int P; /** Status register */
        public int X; /** X register */
        public int Y; /** Y register */
        public int S; /** Stack pointer */
        public int PC; /** Program Counter */

        public int iRequest; /** Interrupt request */
        public int iMask; /** Interrupt mask */
        public int clock; /** (incremental)cycle counter */
        public int lastcode;
        //public Km6280 user; /** pointer to user area */
        public M_Hes.HESHES user; /** pointer to user area */

        public int lowClockMode;

        public dlgReadHandler ReadByte;
        public dlgWriterHandler WriteByte;

        public dlgReadHandler ReadMPR;
        public dlgWriterHandler WriteMPR;
        public dlgWriterHandler Write6270;

        public enum K6280_FLAGS {
            K6280_C_FLAG(0x01),
            K6280_Z_FLAG(0x02),
            K6280_I_FLAG(0x04),
            K6280_D_FLAG(0x08),
            K6280_B_FLAG(0x10),
            K6280_T_FLAG(0x20),
            K6280_V_FLAG(0x40),
            K6280_N_FLAG(0x80);
            final int v;

            K6280_FLAGS(int v) {
                this.v = v;
            }
        }

        public enum K6280_IRQ {
            K6280_INIT(1),
            K6280_RESET(2),
            K6280_NMI(4),
            K6280_BRK(8),
            K6280_TIMER(16),
            K6280_INT1(32),
            K6280_INT2(64);
            final int v;

            K6280_IRQ(int v) {
                this.v = v;
            }
        }

        public static final int C_FLAG = K6280_FLAGS.K6280_C_FLAG.v;
        public static final int Z_FLAG = K6280_FLAGS.K6280_Z_FLAG.v;
        public static final int I_FLAG = K6280_FLAGS.K6280_I_FLAG.v;
        public static final int D_FLAG = K6280_FLAGS.K6280_D_FLAG.v;
        public static final int B_FLAG = K6280_FLAGS.K6280_B_FLAG.v;
        public static final int T_FLAG = K6280_FLAGS.K6280_T_FLAG.v;
        public static final int V_FLAG = K6280_FLAGS.K6280_V_FLAG.v;
        public static final int N_FLAG = K6280_FLAGS.K6280_N_FLAG.v;
        public static final int R_FLAG = 0;

        public static final int BASE_OF_ZERO = 0x2000;

        public static final int VEC_RESET = 0xFFFE;
        public static final int VEC_NMI = 0xFFFC;
        public static final int VEC_TIMER = 0xFFFA;
        public static final int VEC_INT1 = 0xFFF8;
        public static final int VEC_INT = 0xFFF6;

        public static final int VEC_BRK = VEC_INT;

        public static final int IRQ_INIT = K6280_IRQ.K6280_INIT.v;
        public static final int IRQ_RESET = K6280_IRQ.K6280_RESET.v;
        public static final int IRQ_NMI = K6280_IRQ.K6280_NMI.v;
        public static final int IRQ_BRK = K6280_IRQ.K6280_BRK.v;
        public static final int IRQ_TIMER = K6280_IRQ.K6280_TIMER.v;
        public static final int IRQ_INT1 = K6280_IRQ.K6280_INT1.v;
        public static final int IRQ_INT = K6280_IRQ.K6280_INT2.v;

        int K_READ(int adr) {
            return this.ReadByte.apply(this.user, adr);
        }

        void K_WRITE(int adr, int value) {
            this.WriteByte.accept(this.user, adr, value);
        }

        int K_READMPR(int adr) {
            return this.ReadMPR.apply(this.user, adr);
        }

        void K_WRITEMPR(int adr, int value) {
            this.WriteMPR.accept(this.user, adr, value);
        }

        void K_WRITE6270(int adr, int value) {
            this.Write6270.accept(this.user, adr, value);
        }

        public static final byte[] fl_table = new byte[] {
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
            //#if BUILD_HUC6280
            if (this.lowClockMode != 0) {
                //#if 0
                //cycle += (cycle << 2);	/* x5 */
                //#else
                cycle += cycle + cycle; //    */
                cycle += cycle; // x6 */
                //#endif
            }
            this.clock += cycle;
            //#else
            //__THIS__.clock += cycle;
            //#endif
        }

        public int KI_READWORD(int adr) {
            int ret = K_READ(adr);
            int i = ret + (K_READ((adr + 1) & 0xffff) << 8);
            return i;
        }

        public int KI_READWORDZP(int adr) {
            int ret = K_READ(BASE_OF_ZERO + adr);
            return ret + (K_READ(BASE_OF_ZERO + ((adr + 1) & 0xff)) << 8);
        }

        public int KAI_IMM() {
            int ret = this.PC;
            this.PC = (this.PC + 1) & 0xffff;
            return ret;
        }

        public int KAI_IMM16() {
            int ret = this.PC;
            this.PC = (this.PC + 2) & 0xffff;
            return ret;
        }

        public int KAI_ABS() {
            return KI_READWORD(KAI_IMM16());
        }

        public int KAI_ABSX() {
            return (KAI_ABS() + this.X) & 0xffff;
        }

        public int KAI_ABSY() {
            return (KAI_ABS() + this.Y) & 0xffff;
        }

        public int KAI_ZP() {
            return K_READ(KAI_IMM());
        }

        public int KAI_ZPX() {
            return (KAI_ZP() + this.X) & 0xff;
        }

        public int KAI_INDY() {
            return (KI_READWORDZP(KAI_ZP()) + this.Y) & 0xffff;
        }

        public int KA_IMM() {
            int ret = this.PC;
            this.PC = (this.PC + 1) & 0xffff;
            return ret;
        }

        public int KA_IMM16() {
            int ret = this.PC;
            this.PC = (this.PC + 2) & 0xffff;
            return ret;
        }

        public int KA_ABS() {
            return KI_READWORD(KAI_IMM16());
        }

        public int KA_ABSX() {
            return (KAI_ABS() + this.X) & 0xffff;
        }

        public int KA_ABSY() {
            return (KAI_ABS() + this.Y) & 0xffff;
        }

        public int KA_ZP() {
            return BASE_OF_ZERO + K_READ(KAI_IMM());
        }

        public int KA_ZPX() {
            return BASE_OF_ZERO + ((KAI_ZP() + this.X) & 0xff);
        }

        public int KA_ZPY() {
            return BASE_OF_ZERO + ((KAI_ZP() + this.Y) & 0xff);
        }

        public int KA_INDX() {
            return KI_READWORDZP(KAI_ZPX());
        }

        public int KA_INDY() {
            return (KI_READWORDZP(KAI_ZP()) + this.Y) & 0xffff;
        }

        public int KA_IND() {
            return KI_READWORDZP(KAI_ZP());
        }

        public void KM_ALUADDER(int src) {
            int w = this.A + src + (this.P & C_FLAG);
            this.P &= ~(int) (N_FLAG | V_FLAG | Z_FLAG | C_FLAG | T_FLAG);
            this.P += FLAG_NZC(w)
                    + ((((~this.A ^ src) & (this.A ^ w)) >> 1) & V_FLAG);
            this.A = w & 0xff;
        }

        public void KM_ALUADDER_D(int src) {
            int wl = (this.A & 0x0F) + (src & 0x0F) + (this.P & C_FLAG);
            int w = this.A + src + (this.P & C_FLAG);
            //#if BUILD_HUC6280 || BUILD_M65C02
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            //#else
            //this.P &= (int)(~(int)C_FLAG);
            //#endif
            if (wl > 0x9) w += 0x6;
            if (w > 0x9F) {
                this.P += C_FLAG;
                w += 0x60;
            }
            //#if BUILD_HUC6280 || BUILD_M65C02
            this.P += FLAG_NZ(w);
            //#endif
            this.A = w & 0xff;
            KI_ADDCLOCK(1);
        }

        public void KMI_ADC(int src) {
            KM_ALUADDER(src);
        }

        public void KMI_ADC_D(int src) {
            KM_ALUADDER_D(src);
        }

        public void KMI_SBC(int src) {
            KM_ALUADDER(src ^ 0xFF);
        }

        public void KMI_SBC_D(int src) {
            KM_ALUADDER_D(((src ^ 0xFF) + (0x100 - 0x66)) & 0xff);
        }

        public void KM_CMP(int src) {
            int w = this.A + (src ^ 0xFF) + 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZC(w);
        }

        public void KM_CPX(int src) {
            int w = this.X + (src ^ 0xFF) + 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZC(w);
        }

        public void KM_CPY(int src) {
            int w = this.Y + (src ^ 0xFF) + 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZC(w);
        }

        public void KM_BIT(int src) {
            int w = this.A & src;
            this.P &= ~(int) (N_FLAG | V_FLAG | Z_FLAG);
            this.P += (src & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
        }

        public void KM_AND(int src) {
            this.A &= src;
            this.P &= ~(int) (N_FLAG | Z_FLAG | T_FLAG);
            this.P += FLAG_NZ(this.A);
        }

        public void KM_ORA(int src) {
            this.A |= src;
            this.P &= ~(int) (N_FLAG | Z_FLAG | T_FLAG);
            this.P += FLAG_NZ(this.A);
        }

        public void KM_EOR(int src) {
            this.A ^= src;
            this.P &= ~(int) (N_FLAG | Z_FLAG | T_FLAG);
            this.P += FLAG_NZ(this.A);
        }

        public int KM_DEC(int des) {
            int w = des - 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG);
            this.P += FLAG_NZ(w);
            return w & 0xff;
        }

        public int KM_INC(int des) {
            int w = des + 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG);
            this.P += FLAG_NZ(w);
            return w & 0xff;
        }

        public int KM_ASL(int des) {
            int w = des << 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZ(w) + ((des >> 7)/* & C_FLAG*/);
            return w & 0xff;
        }

        public int KM_LSR(int des) {
            int w = des >> 1;
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZ(w) + (des & C_FLAG);
            return w;
        }

        public int KM_LD(int src) {
            this.P &= ~(int) (N_FLAG | Z_FLAG);
            this.P += FLAG_NZ(src);
            return src;
        }

        public int KM_ROL(int des) {
            int w = (des << 1) + (this.P & C_FLAG);
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZ(w) + ((des >> 7)/* & C_FLAG*/);
            return (w) & 0xff;
        }

        public int KM_ROR(int des) {
            int w = (des >> 1) + ((this.P & C_FLAG) << 7);
            this.P &= ~(int) (N_FLAG | Z_FLAG | C_FLAG);
            this.P += FLAG_NZ(w) + (des & C_FLAG);
            return (w) & 0xff;
        }

        public void KM_BRA(int rel) {
            //#if BUILD_HUC6280
            this.PC = (this.PC + (rel ^ 0x80) - 0x80) & 0xffff;
            KI_ADDCLOCK(2);
            //#else
            //int oldPage = this.PC & 0xFF00;
            //this.PC = (int)((this.PC + (rel ^ 0x80) - 0x80) & 0xffff);
            //KI_ADDCLOCK((int)(1 + ((oldPage != (this.PC & 0xFF00)) ? 1 : 0)));
            //#endif
        }

        public void KM_PUSH(int src) {
            K_WRITE(BASE_OF_ZERO + 0x100 + this.S, src);
            this.S = (this.S - 1) & 0xff;
        }

        public int KM_POP() {
            this.S = (this.S + 1) & 0xff;
            return K_READ(BASE_OF_ZERO + 0x100 + this.S);
        }

        //#if BUILD_HUC6280 || BUILD_M65C02
        public int KM_TSB(int mem) {
            int w = this.A | mem;
            this.P &= ~(int) (N_FLAG | V_FLAG | Z_FLAG);
            this.P += (mem & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
            return w;
        }

        public int KM_TRB(int mem) {
            int w = (this.A ^ 0xFF) & mem;
            this.P &= ~(int) (N_FLAG | V_FLAG | Z_FLAG);
            this.P += (mem & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
            return w;
        }

        //#endif
        //#if BUILD_HUC6280
        public int KMI_PRET() {
            int saveA = this.A;
            this.A = K_READ(BASE_OF_ZERO + this.X);
            return saveA;
        }

        public void KMI_POSTT(int saveA) {
            K_WRITE(BASE_OF_ZERO + this.X, this.A);
            this.A = saveA;
            KI_ADDCLOCK(3);
        }

        public void KM_TST(int imm, int mem) {
            int w = imm & mem;
            this.P &= ~(int) (N_FLAG | V_FLAG | Z_FLAG);
            this.P += (mem & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
        }

        /* --- ADC ---  */

        public void Opcode61() {
            KMI_ADC(K_READ(KA_INDX()));
        }

        public void Opcode65() {
            KMI_ADC(K_READ(KA_ZP()));
        }

        public void Opcode69() {
            KMI_ADC(K_READ(KA_IMM()));
        }

        public void Opcode6D() {
            KMI_ADC(K_READ(KA_ABS()));
        }

        public void Opcode71() {
            KMI_ADC(K_READ(KA_INDY()));
        }

        public void Opcode75() {
            KMI_ADC(K_READ(KA_ZPX()));
        }

        public void Opcode79() {
            KMI_ADC(K_READ(KA_ABSY()));
        }

        public void Opcode7D() {
            KMI_ADC(K_READ(KA_ABSX()));
        }

        public void Opcode72() {
            KMI_ADC(K_READ(KA_IND()));
        }

        public void D_Opco61() {
            KMI_ADC_D(K_READ(KA_INDX()));
        }

        public void D_Opco65() {
            KMI_ADC_D(K_READ(KA_ZP()));
        }

        public void D_Opco69() {
            KMI_ADC_D(K_READ(KA_IMM()));
        }

        public void D_Opco6D() {
            KMI_ADC_D(K_READ(KA_ABS()));
        }

        public void D_Opco71() {
            KMI_ADC_D(K_READ(KA_INDY()));
        }

        public void D_Opco75() {
            KMI_ADC_D(K_READ(KA_ZPX()));
        }

        public void D_Opco79() {
            KMI_ADC_D(K_READ(KA_ABSY()));
        }

        public void D_Opco7D() {
            KMI_ADC_D(K_READ(KA_ABSX()));
        }

        public void D_Opco72() {
            KMI_ADC_D(K_READ(KA_IND()));
        }

        public void T_Opco61() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_INDX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco65() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_ZP()));
            KMI_POSTT(saveA);
        }

        public void T_Opco69() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_IMM()));
            KMI_POSTT(saveA);
        }

        public void T_Opco6D() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_ABS()));
            KMI_POSTT(saveA);
        }

        public void T_Opco71() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_INDY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco75() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_ZPX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco79() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_ABSY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco7D() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_ABSX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco72() {
            int saveA = KMI_PRET();
            KMI_ADC(K_READ(KA_IND()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc61() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_INDX()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc65() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_ZP()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc69() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_IMM()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc6D() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_ABS()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc71() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_INDY()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc75() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_ZPX()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc79() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_ABSY()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc7D() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_ABSX()));
            KMI_POSTT(saveA);
        }

        public void TD_Opc72() {
            int saveA = KMI_PRET();
            KMI_ADC_D(K_READ(KA_IND()));
            KMI_POSTT(saveA);
        }

        /* --- AND ---  */

        public void Opcode21() {
            KM_AND(K_READ(KA_INDX()));
        }

        public void Opcode25() {
            KM_AND(K_READ(KA_ZP()));
        }

        public void Opcode29() {
            KM_AND(K_READ(KA_IMM()));
        }

        public void Opcode2D() {
            KM_AND(K_READ(KA_ABS()));
        }

        public void Opcode31() {
            KM_AND(K_READ(KA_INDY()));
        }

        public void Opcode35() {
            KM_AND(K_READ(KA_ZPX()));
        }

        public void Opcode39() {
            KM_AND(K_READ(KA_ABSY()));
        }

        public void Opcode3D() {
            KM_AND(K_READ(KA_ABSX()));
        }

        public void Opcode32() {
            KM_AND(K_READ(KA_IND()));
        }

        public void T_Opco21() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_INDX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco25() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_ZP()));
            KMI_POSTT(saveA);
        }

        public void T_Opco29() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_IMM()));
            KMI_POSTT(saveA);
        }

        public void T_Opco2D() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_ABS()));
            KMI_POSTT(saveA);
        }

        public void T_Opco31() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_INDY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco35() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_ZPX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco39() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_ABSY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco3D() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_ABSX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco32() {
            int saveA = KMI_PRET();
            KM_AND(K_READ(KA_IND()));
            KMI_POSTT(saveA);
        }

        /* --- ASL ---  */

        public void Opcode06() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_ASL(K_READ(adr)));
        }

        public void Opcode0E() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_ASL(K_READ(adr)));
        }

        public void Opcode16() {
            int adr = KA_ZPX();
            K_WRITE(adr, KM_ASL(K_READ(adr)));
        }

        public void Opcode1E() {
            int adr = KA_ABSX();
            K_WRITE(adr, KM_ASL(K_READ(adr)));
        }

        public void Opcode0A() {
            this.A = KM_ASL(this.A);
        }

        /* --- BBRi --- */

        public void Opcode0F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 0)) == 0) KM_BRA(rel);
        }

        public void Opcode1F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 1)) == 0) KM_BRA(rel);
        }

        public void Opcode2F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 2)) == 0) KM_BRA(rel);
        }

        public void Opcode3F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 3)) == 0) KM_BRA(rel);
        }

        public void Opcode4F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 4)) == 0) KM_BRA(rel);
        }

        public void Opcode5F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 5)) == 0) KM_BRA(rel);
        }

        public void Opcode6F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 6)) == 0) KM_BRA(rel);
        }

        public void Opcode7F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 7)) == 0) KM_BRA(rel);
        }

        /* --- BBSi --- */

        public void Opcode8F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 0)) != 0) KM_BRA(rel);
        }

        public void Opcode9F() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 1)) != 0) KM_BRA(rel);
        }

        public void OpcodeAF() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 2)) != 0) KM_BRA(rel);
        }

        public void OpcodeBF() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 3)) != 0) KM_BRA(rel);
        }

        public void OpcodeCF() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 4)) != 0) KM_BRA(rel);
        }

        public void OpcodeDF() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 5)) != 0) KM_BRA(rel);
        }

        public void OpcodeEF() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 6)) != 0) KM_BRA(rel);
        }

        public void OpcodeFF() {
            int adr = KA_ZP();
            int rel = K_READ(KA_IMM());
            if ((K_READ(adr) & (1 << 7)) != 0) KM_BRA(rel);
        }

        /* --- BIT ---  */

        public void Opcode24() {
            KM_BIT(K_READ(KA_ZP()));
        }

        public void Opcode2C() {
            KM_BIT(K_READ(KA_ABS()));
        }

        public void Opcode34() {
            KM_BIT(K_READ(KA_ZPX()));
        }

        public void Opcode3C() {
            KM_BIT(K_READ(KA_ABSX()));
        }

        public void Opcode89() {
            KM_BIT(K_READ(KA_IMM()));
        }

        /* --- Bcc ---  */

        public void Opcode10() {
            int rel = K_READ(KA_IMM());
            if ((this.P & N_FLAG) == 0) KM_BRA(rel);
        }

        public void Opcode30() {
            int rel = K_READ(KA_IMM());
            if ((this.P & N_FLAG) != 0) KM_BRA(rel);
        }

        public void Opcode50() {
            int rel = K_READ(KA_IMM());
            if ((this.P & V_FLAG) == 0) KM_BRA(rel);
        }

        public void Opcode70() {
            int rel = K_READ(KA_IMM());
            if ((this.P & V_FLAG) != 0) KM_BRA(rel);
        }

        public void Opcode90() {
            int rel = K_READ(KA_IMM());
            if ((this.P & C_FLAG) == 0) KM_BRA(rel);
        }

        public void OpcodeB0() {
            int rel = K_READ(KA_IMM());
            if ((this.P & C_FLAG) != 0) KM_BRA(rel);
        }

        public void OpcodeD0() {
            int rel = K_READ(KA_IMM());
            if ((this.P & Z_FLAG) == 0) KM_BRA(rel);
        }

        public void OpcodeF0() {
            int rel = K_READ(KA_IMM());
            if ((this.P & Z_FLAG) != 0) KM_BRA(rel);
        }

        public void Opcode80() {
            int rel = K_READ(KA_IMM());
            if (true) KM_BRA(rel);
        }

        /* --- BRK --- */

        public void Opcode00() {
            this.PC = (this.PC + 1) & 0xffff;
            this.iRequest |= IRQ_BRK;
        } /* 00 - BRK */

        /* --- BSR --- */

        public void Opcode44() { // 44 - BSR */
            KM_PUSH((this.PC >> 8) & 0xff); // !!! PC = NEXT - 1; !!! */
            KM_PUSH((this.PC) & 0xff);
            KM_BRA(K_READ(KA_IMM()));
        }

        /* --- CLA --- */
        public void Opcode62() { // 62 - CLA
            this.A = 0;
        }

        /* --- CLX --- */
        public void Opcode82() { // 82 - CLX
            this.X = 0;
        }

        /* --- CLY --- */
        public void OpcodeC2() { // C2 - CLY
            this.Y = 0;
        }

        /* --- CLC --- */
        public void Opcode18() { // 18 - CLC
            this.P &= ~(int) C_FLAG;
        }

        /* --- CLD --- */
        public void OpcodeD8() { // D8 - CLD
            this.P &= ~(int) D_FLAG;
        }

        /* --- CLI --- */
        public void Opcode58() { // 58 - CLI
            this.P &= ~(int) I_FLAG;
        }

        /* --- CLV --- */
        public void OpcodeB8() { // B8 - CLV
            this.P &= ~(int) V_FLAG;
        }

        /* --- CMP --- */

        public void OpcodeC1() {
            KM_CMP(K_READ(KA_INDX()));
        }

        public void OpcodeC5() {
            KM_CMP(K_READ(KA_ZP()));
        }

        public void OpcodeC9() {
            KM_CMP(K_READ(KA_IMM()));
        }

        public void OpcodeCD() {
            KM_CMP(K_READ(KA_ABS()));
        }

        public void OpcodeD1() {
            KM_CMP(K_READ(KA_INDY()));
        }

        public void OpcodeD5() {
            KM_CMP(K_READ(KA_ZPX()));
        }

        public void OpcodeD9() {
            KM_CMP(K_READ(KA_ABSY()));
        }

        public void OpcodeDD() {
            KM_CMP(K_READ(KA_ABSX()));
        }

        public void OpcodeD2() {
            KM_CMP(K_READ(KA_IND()));
        }

        /* --- CPX --- */

        public void OpcodeE0() {
            KM_CPX(K_READ(KA_IMM()));
        }

        public void OpcodeE4() {
            KM_CPX(K_READ(KA_ZP()));
        }

        public void OpcodeEC() {
            KM_CPX(K_READ(KA_ABS()));
        }

        /* --- CPY --- */

        public void OpcodeC0() {
            KM_CPY(K_READ(KA_IMM()));
        }

        public void OpcodeC4() {
            KM_CPY(K_READ(KA_ZP()));
        }

        public void OpcodeCC() {
            KM_CPY(K_READ(KA_ABS()));
        }

        /* --- DEC ---  */

        public void OpcodeC6() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_DEC(K_READ(adr)));
        }

        public void OpcodeCE() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_DEC(K_READ(adr)));
        }

        public void OpcodeD6() {
            int adr = KA_ZPX();
            K_WRITE(adr, KM_DEC(K_READ(adr)));
        }

        public void OpcodeDE() {
            int adr = KA_ABSX();
            K_WRITE(adr, KM_DEC(K_READ(adr)));
        }

        public void Opcode3A() { // 3A - DEA
            this.A = KM_DEC(this.A);
        }

        public void OpcodeCA() { // CA - DEX
            this.X = KM_DEC(this.X);
        }

        public void Opcode88() { // 88 - DEY
            this.Y = KM_DEC(this.Y);
        }

        /* --- EOR ---  */

        public void Opcode41() {
            KM_EOR(K_READ(KA_INDX()));
        }

        public void Opcode45() {
            KM_EOR(K_READ(KA_ZP()));
        }

        public void Opcode49() {
            KM_EOR(K_READ(KA_IMM()));
        }

        public void Opcode4D() {
            KM_EOR(K_READ(KA_ABS()));
        }

        public void Opcode51() {
            KM_EOR(K_READ(KA_INDY()));
        }

        public void Opcode55() {
            KM_EOR(K_READ(KA_ZPX()));
        }

        public void Opcode59() {
            KM_EOR(K_READ(KA_ABSY()));
        }

        public void Opcode5D() {
            KM_EOR(K_READ(KA_ABSX()));
        }

        public void Opcode52() {
            KM_EOR(K_READ(KA_IND()));
        }

        public void T_Opco41() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_INDX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco45() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_ZP()));
            KMI_POSTT(saveA);
        }

        public void T_Opco49() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_IMM()));
            KMI_POSTT(saveA);
        }

        public void T_Opco4D() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_ABS()));
            KMI_POSTT(saveA);
        }

        public void T_Opco51() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_INDY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco55() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_ZPX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco59() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_ABSY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco5D() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_ABSX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco52() {
            int saveA = KMI_PRET();
            KM_EOR(K_READ(KA_IND()));
            KMI_POSTT(saveA);
        }

        /* --- INC ---  */

        public void OpcodeE6() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_INC(K_READ(adr)));
        }

        public void OpcodeEE() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_INC(K_READ(adr)));
        }

        public void OpcodeF6() {
            int adr = KA_ZPX();
            K_WRITE(adr, KM_INC(K_READ(adr)));
        }

        public void OpcodeFE() {
            int adr = KA_ABSX();
            K_WRITE(adr, KM_INC(K_READ(adr)));
        }

        public void Opcode1A() { // 1A - INA
            this.A = KM_INC(this.A);
        }

        public void OpcodeE8() { // E8 - INX
            this.X = KM_INC(this.X);
        }

        public void OpcodeC8() { // C8 - INY
            this.Y = KM_INC(this.Y);
        }

        /* --- JMP ---  */

        public void Opcode4C() {
            this.PC = KI_READWORD(KA_IMM16());
        }

        public void Opcode6C() {
            this.PC = KI_READWORD(KA_ABS());
        }

        public void Opcode7C() {
            this.PC = KI_READWORD(KA_ABSX());
        }

        /* --- JSR --- */
        public void Opcode20() { // 20 - JSR
            int adr = KA_IMM();
            KM_PUSH((this.PC >> 8) & 0xff);   /* !!! PC = NEXT - 1; !!! */
            KM_PUSH((this.PC) & 0xff);
            this.PC = KI_READWORD(adr);
        }

        /* --- LDA --- */

        public void OpcodeA1() {
            this.A = KM_LD(K_READ(KA_INDX()));
        }

        public void OpcodeA5() {
            this.A = KM_LD(K_READ(KA_ZP()));
        }

        public void OpcodeA9() {
            this.A = KM_LD(K_READ(KA_IMM()));
        }

        public void OpcodeAD() {
            this.A = KM_LD(K_READ(KA_ABS()));
        }

        public void OpcodeB1() {
            this.A = KM_LD(K_READ(KA_INDY()));
        }

        public void OpcodeB5() {
            this.A = KM_LD(K_READ(KA_ZPX()));
        }

        public void OpcodeB9() {
            this.A = KM_LD(K_READ(KA_ABSY()));
        }

        public void OpcodeBD() {
            this.A = KM_LD(K_READ(KA_ABSX()));
        }

        public void OpcodeB2() {
            this.A = KM_LD(K_READ(KA_IND()));
        }

        /* --- LDX ---  */

        public void OpcodeA2() {
            this.X = KM_LD(K_READ(KA_IMM()));
        }

        public void OpcodeA6() {
            this.X = KM_LD(K_READ(KA_ZP()));
        }

        public void OpcodeAE() {
            this.X = KM_LD(K_READ(KA_ABS()));
        }

        public void OpcodeB6() {
            this.X = KM_LD(K_READ(KA_ZPY()));
        }

        public void OpcodeBE() {
            this.X = KM_LD(K_READ(KA_ABSY()));
        }

        /* --- LDY ---  */

        public void OpcodeA0() {
            this.Y = KM_LD(K_READ(KA_IMM()));
        }

        public void OpcodeA4() {
            this.Y = KM_LD(K_READ(KA_ZP()));
        }

        public void OpcodeAC() {
            this.Y = KM_LD(K_READ(KA_ABS()));
        }

        public void OpcodeB4() {
            this.Y = KM_LD(K_READ(KA_ZPX()));
        }

        public void OpcodeBC() {
            this.Y = KM_LD(K_READ(KA_ABSX()));
        }

        /* --- LSR ---  */

        public void Opcode46() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_LSR(K_READ(adr)));
        }

        public void Opcode4E() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_LSR(K_READ(adr)));
        }

        public void Opcode56() {
            int adr = KA_ZPX();
            K_WRITE(adr, KM_LSR(K_READ(adr)));
        }

        public void Opcode5E() {
            int adr = KA_ABSX();
            K_WRITE(adr, KM_LSR(K_READ(adr)));
        }

        public void Opcode4A() { // 4A - LSR - Accumulator
            this.A = KM_LSR(this.A);
        }

        /* --- NOP ---  */
        public void OpcodeEA() { // EA - NOP
        }

        /* --- ORA ---  */

        public void Opcode01() {
            KM_ORA(K_READ(KA_INDX()));
        }

        public void Opcode05() {
            KM_ORA(K_READ(KA_ZP()));
        }

        public void Opcode09() {
            KM_ORA(K_READ(KA_IMM()));
        }

        public void Opcode0D() {
            KM_ORA(K_READ(KA_ABS()));
        }

        public void Opcode11() {
            KM_ORA(K_READ(KA_INDY()));
        }

        public void Opcode15() {
            KM_ORA(K_READ(KA_ZPX()));
        }

        public void Opcode19() {
            KM_ORA(K_READ(KA_ABSY()));
        }

        public void Opcode1D() {
            KM_ORA(K_READ(KA_ABSX()));
        }

        public void Opcode12() {
            KM_ORA(K_READ(KA_IND()));
        }

        public void T_Opco01() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_INDX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco05() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_ZP()));
            KMI_POSTT(saveA);
        }

        public void T_Opco09() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_IMM()));
            KMI_POSTT(saveA);
        }

        public void T_Opco0D() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_ABS()));
            KMI_POSTT(saveA);
        }

        public void T_Opco11() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_INDY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco15() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_ZPX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco19() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_ABSY()));
            KMI_POSTT(saveA);
        }

        public void T_Opco1D() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_ABSX()));
            KMI_POSTT(saveA);
        }

        public void T_Opco12() {
            int saveA = KMI_PRET();
            KM_ORA(K_READ(KA_IND()));
            KMI_POSTT(saveA);
        }

        /* --- PHr PLr  --- */
        public void Opcode48() { // 48 - PHA
            KM_PUSH(this.A);
        }

        public void Opcode08() { // 08 - PHP
            KM_PUSH((this.P | B_FLAG | R_FLAG) & ~T_FLAG);
        }

        public void Opcode68() { // 68 - PLA
            this.A = KM_LD(KM_POP());
        }

        public void Opcode28() { // 28 - PLP
            this.P = KM_POP() & ~T_FLAG;
        }

        public void OpcodeDA() { // DA - PHX
            KM_PUSH(this.X);
        }

        public void Opcode5A() { // 5A - PHY
            KM_PUSH(this.Y);
        }

        public void OpcodeFA() { // FA - PLX
            this.X = KM_LD(KM_POP());
        }

        public void Opcode7A() { // 7A - PLY
            this.Y = KM_LD(KM_POP());
        }

        /* --- RMBi --- */

        public void Opcode07() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 0)));
        }

        public void Opcode17() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 1)));
        }

        public void Opcode27() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 2)));
        }

        public void Opcode37() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 3)));
        }

        public void Opcode47() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 4)));
        }

        public void Opcode57() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 5)));
        }

        public void Opcode67() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 6)));
        }

        public void Opcode77() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) & (~(1 << 7)));
        }

        /* --- SMBi --- */

        public void Opcode87() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 0));
        }

        public void Opcode97() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 1));
        }

        public void OpcodeA7() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 2));
        }

        public void OpcodeB7() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 3));
        }

        public void OpcodeC7() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 4));
        }

        public void OpcodeD7() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 5));
        }

        public void OpcodeE7() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 6));
        }

        public void OpcodeF7() {
            int adr = KA_ZP();
            K_WRITE(adr, K_READ(adr) | (1 << 7));
        }

        /* --- ROL ---  */

        public void Opcode26() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_ROL(K_READ(adr)));
        }

        public void Opcode2E() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_ROL(K_READ(adr)));
        }

        public void Opcode36() {
            int adr = KA_ZPX();
            K_WRITE(adr, KM_ROL(K_READ(adr)));
        }

        public void Opcode3E() {
            int adr = KA_ABSX();
            K_WRITE(adr, KM_ROL(K_READ(adr)));
        }

        public void Opcode2A() { // 2A - ROL - Accumulator
            this.A = KM_ROL(this.A);
        }

        /* --- ROR ---  */

        public void Opcode66() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_ROR(K_READ(adr)));
        }

        public void Opcode6E() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_ROR(K_READ(adr)));
        }

        public void Opcode76() {
            int adr = KA_ZPX();
            K_WRITE(adr, KM_ROR(K_READ(adr)));
        }

        public void Opcode7E() {
            int adr = KA_ABSX();
            K_WRITE(adr, KM_ROR(K_READ(adr)));
        }

        public void Opcode6A() { // 6A - ROR - Accumulator
            this.A = KM_ROR(this.A);
        }

        public void Opcode40() { // 40 - RTI

            this.P = KM_POP();
            this.PC = KM_POP();
            this.PC += KM_POP() << 8;
        }

        public void Opcode60() { // 60 - RTS
            this.PC = KM_POP();
            this.PC += KM_POP() << 8;
            this.PC = (this.PC + 1) & 0xffff;
        }

        public void Opcode22() { // 22 - SAX
            int temp = this.A;
            this.A = this.X;
            this.X = temp;
        }

        public void Opcode42() { // 42 - SAY
            int temp = this.A;
            this.A = this.Y;
            this.Y = temp;
        }

        public void Opcode02() { // 02 - SXY
            int temp = this.Y;
            this.Y = this.X;
            this.X = temp;
        }

        /* --- SBC ---  */

        public void OpcodeE1() {
            KMI_SBC(K_READ(KA_INDX()));
        }

        public void OpcodeE5() {
            KMI_SBC(K_READ(KA_ZP()));
        }

        public void OpcodeE9() {
            KMI_SBC(K_READ(KA_IMM()));
        }

        public void OpcodeED() {
            KMI_SBC(K_READ(KA_ABS()));
        }

        public void OpcodeF1() {
            KMI_SBC(K_READ(KA_INDY()));
        }

        public void OpcodeF5() {
            KMI_SBC(K_READ(KA_ZPX()));
        }

        public void OpcodeF9() {
            KMI_SBC(K_READ(KA_ABSY()));
        }

        public void OpcodeFD() {
            KMI_SBC(K_READ(KA_ABSX()));
        }

        public void OpcodeF2() {
            KMI_SBC(K_READ(KA_IND()));
        }

        public void D_OpcoE1() {
            KMI_SBC_D(K_READ(KA_INDX()));
        }

        public void D_OpcoE5() {
            KMI_SBC_D(K_READ(KA_ZP()));
        }

        public void D_OpcoE9() {
            KMI_SBC_D(K_READ(KA_IMM()));
        }

        public void D_OpcoED() {
            KMI_SBC_D(K_READ(KA_ABS()));
        }

        public void D_OpcoF1() {
            KMI_SBC_D(K_READ(KA_INDY()));
        }

        public void D_OpcoF5() {
            KMI_SBC_D(K_READ(KA_ZPX()));
        }

        public void D_OpcoF9() {
            KMI_SBC_D(K_READ(KA_ABSY()));
        }

        public void D_OpcoFD() {
            KMI_SBC_D(K_READ(KA_ABSX()));
        }

        public void D_OpcoF2() {
            KMI_SBC_D(K_READ(KA_IND()));
        }

        /* --- SEC --- */
        public void Opcode38() { // 38 - SEC
            this.P |= C_FLAG;
        }

        /* --- SED --- */
        public void OpcodeF8() { // F8 - SED
            this.P |= D_FLAG;
        }

        /* --- SEI --- */
        public void Opcode78() { // 78 - SEI
            this.P |= I_FLAG;
        }

        /* --- SET --- */
        public void OpcodeF4() { // F4 - SET
            this.P |= T_FLAG;
        }

        public void Opcode03() { // 03 - ST0
            K_WRITE6270(0, K_READ(KA_IMM()));
        }

        public void Opcode13() { // 13 - ST1
            K_WRITE6270(2, K_READ(KA_IMM()));
        }

        public void Opcode23() { // 23 - ST2
            K_WRITE6270(3, K_READ(KA_IMM()));
        }

        /* --- STA --- */

        public void Opcode81() {
            K_WRITE(KA_INDX(), this.A);
        }

        public void Opcode85() {
            K_WRITE(KA_ZP(), this.A);
        }

        public void Opcode8D() {
            K_WRITE(KA_ABS(), this.A);
        }

        public void Opcode91() {
            K_WRITE(KA_INDY(), this.A);
        }

        public void Opcode95() {
            K_WRITE(KA_ZPX(), this.A);
        }

        public void Opcode99() {
            K_WRITE(KA_ABSY(), this.A);
        }

        public void Opcode9D() {
            K_WRITE(KA_ABSX(), this.A);
        }

        public void Opcode92() {
            K_WRITE(KA_IND(), this.A);
        }

        /* --- STX ---  */

        public void Opcode86() {
            K_WRITE(KA_ZP(), this.X);
        }

        public void Opcode8E() {
            K_WRITE(KA_ABS(), this.X);
        }

        public void Opcode96() {
            K_WRITE(KA_ZPY(), this.X);
        }

        /* --- STY ---  */

        public void Opcode84() {
            K_WRITE(KA_ZP(), this.Y);
        }

        public void Opcode8C() {
            K_WRITE(KA_ABS(), this.Y);
        }

        public void Opcode94() {
            K_WRITE(KA_ZPX(), this.Y);
        }

        /* --- STZ ---  */

        public void Opcode64() {
            K_WRITE(KA_ZP(), 0);
        }

        public void Opcode9C() {
            K_WRITE(KA_ABS(), 0);
        }

        public void Opcode74() {
            K_WRITE(KA_ZPX(), 0);
        }

        public void Opcode9E() {
            K_WRITE(KA_ABSX(), 0);
        }

        /* --- TAMi ---  */

        public void Opcode53() { // 53 - TAMi
            K_WRITEMPR(K_READ(KA_IMM()), this.A);
        }

        /* --- TMAi ---  */

        public void Opcode43() { // 43 - TMAi
            this.A = K_READMPR(K_READ(KA_IMM()));
        }

        /* --- TRB --- */

        public void Opcode14() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_TRB(K_READ(adr)));
        }

        public void Opcode1C() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_TRB(K_READ(adr)));
        }

        /* --- TSB --- */

        public void Opcode04() {
            int adr = KA_ZP();
            K_WRITE(adr, KM_TSB(K_READ(adr)));
        }

        public void Opcode0C() {
            int adr = KA_ABS();
            K_WRITE(adr, KM_TSB(K_READ(adr)));
        }

        /* --- TST --- */

        public void Opcode83() {
            int imm = K_READ(KA_IMM());
            KM_TST(imm, K_READ(KA_ZP()));
        }

        public void Opcode93() {
            int imm = K_READ(KA_IMM());
            KM_TST(imm, K_READ(KA_ABS()));
        }

        public void OpcodeA3() {
            int imm = K_READ(KA_IMM());
            KM_TST(imm, K_READ(KA_ZPX()));
        }

        public void OpcodeB3() {
            int imm = K_READ(KA_IMM());
            KM_TST(imm, K_READ(KA_ABSX()));
        }
        //#endif

        /* --- TAX ---  */
        public void OpcodeAA() { // AA - TAX
            this.X = KM_LD(this.A);
        }

        /* --- TAY ---  */
        public void OpcodeA8() { // A8 - TAY
            this.Y = KM_LD(this.A);
        }

        /* --- TSX ---  */
        public void OpcodeBA() { // BA - TSX
            this.X = KM_LD(this.S);
        }

        /* --- TXA ---  */
        public void Opcode8A() { // 8A - TXA
            this.A = KM_LD(this.X);
        }

        /* --- TXS ---  */
        public void Opcode9A() { // 9A - TXS
            this.S = this.X;
        }

        /* --- TYA ---  */
        public void Opcode98() { // 98 - TYA
            this.A = KM_LD(this.Y);
        }

        //#if BUILD_HUC6280
        public void Opcode73() { // 73 - TII
            int src, des, len;
            src = KI_READWORD(KA_IMM16());
            des = KI_READWORD(KA_IMM16());
            len = KI_READWORD(KA_IMM16());
            KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
            do {
                K_WRITE(des, K_READ(src));
                src = (src + 1) & 0xffff;
                des = (des + 1) & 0xffff;
                len = (len - 1) & 0xffff;
            } while (len != 0);
        }

        public void OpcodeC3() { // C3 - TDD
            int src, des, len;
            src = KI_READWORD(KA_IMM16());
            des = KI_READWORD(KA_IMM16());
            len = KI_READWORD(KA_IMM16());
            KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
            do {
                K_WRITE(des, K_READ(src));
                src = (src - 1) & 0xffff;
                des = (des - 1) & 0xffff;
                len = (len - 1) & 0xffff;
            } while (len != 0);
        }

        public void OpcodeD3() { // D3 - TIN
            int src, des, len;
            src = KI_READWORD(KA_IMM16());
            des = KI_READWORD(KA_IMM16());
            len = KI_READWORD(KA_IMM16());
            KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
            do {
                K_WRITE(des, K_READ(src));
                src = (src + 1) & 0xffff;
                len = (len - 1) & 0xffff;
            } while (len != 0);
        }

        public void OpcodeE3() { // E3 - TIA
            int add = 1;
            int src, des, len;
            src = KI_READWORD(KA_IMM16());
            des = KI_READWORD(KA_IMM16());
            len = KI_READWORD(KA_IMM16());
            KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
            do {
                K_WRITE(des, K_READ(src));
                src = (src + 1) & 0xffff;
                des = (des + add) & 0xffff;
                add = -add;
                len = (len - 1) & 0xffff;
            } while (len != 0);
        }

        public void OpcodeF3() { // F3 - TAI
            int add = 1;
            int src, des, len;
            src = KI_READWORD(KA_IMM16());
            des = KI_READWORD(KA_IMM16());
            len = KI_READWORD(KA_IMM16());
            KI_ADDCLOCK(len != 0 ? len * 6 : 0x60000);
            do {
                K_WRITE(des, K_READ(src));
                src = (src + add) & 0xffff;
                des = (des + 1) & 0xffff;
                add = -add;
                len = (len - 1) & 0xffff;
            } while (len != 0);
        }

        public void Opcode54() { // 54 - CSL
            this.lowClockMode = 1;
        }

        public void OpcodeD4() { // D4 - CSH
            this.lowClockMode = 0;
        }

        /*
             HuC6280 clock cycle table

             -0         undefined OP-code
             BRK(#$00)  +7 by Interrupt

            */
        public byte[] cl_table = new byte[] {
                /* L 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F     H */
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
                2, 7, 2, 7, 4, 4, 4, 7, 2, 2, 2, -0, 5, 5, 5, 6, /* A */
                2, 7, 7, 8, 4, 4, 4, 7, 2, 5, 2, -0, 5, 5, 5, 6, /* B */
                2, 7, 2, 17, 4, 4, 6, 7, 2, 2, 2, -0, 5, 5, 7, 6, /* C */
                2, 7, 7, 17, 2, 4, 6, 7, 2, 5, 3, -0, -0, 5, 7, 6, /* D */
                2, 7, -0, 17, 4, 4, 6, 7, 2, 2, 2, -0, 5, 5, 7, 6, /* E */
                2, 7, 7, 17, 2, 4, 6, 7, 2, 5, 3, -0, -0, 5, 7, 6, /* F */
        };

        public void K_OPEXEC() {
            int opcode = this.lastcode = K_READ(KAI_IMM());
            KI_ADDCLOCK(cl_table[opcode]);
            switch (opcode) {

            // OP__(00)    OPt_(01)    OPxx(02)    OPxx(04)    OPt_(05)    OP__(06)
            case 0x00:
                Opcode00();
                break;
            case 0x01:
                if ((this.P & T_FLAG) != 0) T_Opco01();
                else Opcode01();
                break;
            case 0x05:
                if ((this.P & T_FLAG) != 0) T_Opco05();
                else Opcode05();
                break;
            case 0x06:
                Opcode06();
                break;

            // OP__(08)    OPt_(09)    OP__(0A)    OPxx(0C)    OPt_(0D)    OP__(0E)
            case 0x08:
                Opcode08();
                break;
            case 0x09:
                if ((this.P & T_FLAG) != 0) T_Opco09();
                else Opcode09();
                break;
            case 0x0A:
                Opcode0A();
                break;
            case 0x0D:
                if ((this.P & T_FLAG) != 0) T_Opco0D();
                else Opcode0D();
                break;
            case 0x0E:
                Opcode0E();
                break;

            // OP__(10)    OPt_(11)    OPxx(12)    OPxx(14)    OPt_(15)    OP__(16)
            case 0x10:
                Opcode10();
                break;
            case 0x11:
                if ((this.P & T_FLAG) != 0) T_Opco11();
                else Opcode11();
                break;
            case 0x15:
                if ((this.P & T_FLAG) != 0) T_Opco15();
                else Opcode15();
                break;
            case 0x16:
                Opcode16();
                break;

            // OP__(18)    OPt_(19)    OPxx(1A)    OPxx(1C)    OPt_(1D)    OP__(1E)
            case 0x18:
                Opcode18();
                break;
            case 0x19:
                if ((this.P & T_FLAG) != 0) T_Opco19();
                else Opcode19();
                break;
            case 0x1D:
                if ((this.P & T_FLAG) != 0) T_Opco1D();
                else Opcode1D();
                break;
            case 0x1E:
                Opcode1E();
                break;

            // OP__(20)    OP__(21)    OPxx(22)    OP__(24)    OP__(25)    OP__(26)
            case 0x20:
                Opcode20();
                break;
            case 0x21:
                Opcode21();
                break;
            case 0x24:
                Opcode24();
                break;
            case 0x25:
                Opcode25();
                break;
            case 0x26:
                Opcode26();
                break;

            // OP__(28)    OP__(29)    OP__(2A)    OP__(2C)    OP__(2D)    OP__(2E)
            case 0x28:
                Opcode28();
                break;
            case 0x29:
                Opcode29();
                break;
            case 0x2A:
                Opcode2A();
                break;
            case 0x2C:
                Opcode2C();
                break;
            case 0x2D:
                Opcode2D();
                break;
            case 0x2E:
                Opcode2E();
                break;

            // OP__(30)    OPt_(31)    OPxx(32)    OPxx(34)    OPt_(35)    OP__(36)
            case 0x30:
                Opcode30();
                break;
            case 0x31:
                if ((this.P & T_FLAG) != 0) T_Opco31();
                else Opcode31();
                break;
            case 0x35:
                if ((this.P & T_FLAG) != 0) T_Opco35();
                else Opcode35();
                break;
            case 0x36:
                Opcode36();
                break;

            // OP__(38)    OPt_(39)    OPxx(3A)    OPxx(3C)    OPt_(3D)    OP__(3E)
            case 0x38:
                Opcode38();
                break;
            case 0x39:
                if ((this.P & T_FLAG) != 0) T_Opco39();
                else Opcode39();
                break;
            case 0x3D:
                if ((this.P & T_FLAG) != 0) T_Opco3D();
                else Opcode3D();
                break;
            case 0x3E:
                Opcode3E();
                break;

            // OP__(40)    OPt_(41)    OPxx(42)    OPxx(44)    OPt_(45)    OP__(46)
            case 0x40:
                Opcode40();
                break;
            case 0x41:
                if ((this.P & T_FLAG) != 0) T_Opco41();
                else Opcode41();
                break;
            case 0x45:
                if ((this.P & T_FLAG) != 0) T_Opco45();
                else Opcode45();
                break;
            case 0x46:
                Opcode46();
                break;

            //OP__(48)    OPt_(49)    OP__(4A)    OP__(4C)    OPt_(4D)    OP__(4E)
            case 0x48:
                Opcode48();
                break;
            case 0x49:
                if ((this.P & T_FLAG) != 0) T_Opco49();
                else Opcode49();
                break;
            case 0x4A:
                Opcode4A();
                break;
            case 0x4C:
                Opcode4C();
                break;
            case 0x4D:
                if ((this.P & T_FLAG) != 0) T_Opco4D();
                else Opcode4D();
                break;
            case 0x4E:
                Opcode4E();
                break;

            //OP__(50)    OPt_(51)    OPxx(52)    OPxx(54)    OPt_(55)    OP__(56)
            case 0x50:
                Opcode50();
                break;
            case 0x51:
                if ((this.P & T_FLAG) != 0) T_Opco51();
                else Opcode51();
                break;
            case 0x55:
                if ((this.P & T_FLAG) != 0) T_Opco55();
                else Opcode55();
                break;
            case 0x56:
                Opcode56();
                break;

            //OP__(58)    OPt_(59)    OPxx(5A)    OPxx(5C)    OPt_(5D)    OP__(5E)
            case 0x58:
                Opcode58();
                break;
            case 0x59:
                if ((this.P & T_FLAG) != 0) T_Opco59();
                else Opcode59();
                break;
            case 0x5D:
                if ((this.P & T_FLAG) != 0) T_Opco5D();
                else Opcode5D();
                break;
            case 0x5E:
                Opcode5E();
                break;

            //OP__(60)    OPtd(61)    OPxx(62)    OPxx(64)    OPtd(65)    OP__(66)
            case 0x60:
                Opcode60();
                break;
            case 0x61:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc61();
                else T_Opco61();
                else if ((this.P & D_FLAG) != 0) D_Opco61();
                else Opcode61();
                break;
            case 0x65:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc65();
                else T_Opco65();
                else if ((this.P & D_FLAG) != 0) D_Opco65();
                else Opcode65();
                break;
            case 0x66:
                Opcode66();
                break;

            //OP__(68)    OPtd(69)    OP__(6A)    OP__(6C)    OPtd(6D)    OP__(6E)
            case 0x68:
                Opcode68();
                break;
            case 0x69:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc69();
                else T_Opco69();
                else if ((this.P & D_FLAG) != 0) D_Opco69();
                else Opcode69();
                break;
            case 0x6A:
                Opcode6A();
                break;
            case 0x6C:
                Opcode6C();
                break;
            case 0x6D:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc6D();
                else T_Opco6D();
                else if ((this.P & D_FLAG) != 0) D_Opco6D();
                else Opcode6D();
                break;
            case 0x6E:
                Opcode6E();
                break;

            //OP__(70)    OPtd(71)    OPxx(72)    OPxx(74)    OPtd(75)    OP__(76)
            case 0x70:
                Opcode70();
                break;
            case 0x71:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc71();
                else T_Opco71();
                else if ((this.P & D_FLAG) != 0) D_Opco71();
                else Opcode71();
                break;
            case 0x75:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc75();
                else T_Opco75();
                else if ((this.P & D_FLAG) != 0) D_Opco75();
                else Opcode75();
                break;
            case 0x76:
                Opcode76();
                break;

            //OP__(78)    OPtd(79)    OPxx(7A)    OPxx(7C)    OPtd(7D)    OP__(7E)
            case 0x78:
                Opcode78();
                break;
            case 0x79:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc79();
                else T_Opco79();
                else if ((this.P & D_FLAG) != 0) D_Opco79();
                else Opcode79();
                break;
            case 0x7D:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc7D();
                else T_Opco7D();
                else if ((this.P & D_FLAG) != 0) D_Opco7D();
                else Opcode7D();
                break;
            case 0x7E:
                Opcode7E();
                break;

            //OPxx(80)    OP__(81)    OPxx(82)    OP__(84)    OP__(85)    OP__(86)
            case 0x81:
                Opcode81();
                break;
            case 0x84:
                Opcode84();
                break;
            case 0x85:
                Opcode85();
                break;
            case 0x86:
                Opcode86();
                break;

            //OP__(88)    OPxx(89)    OP__(8A)    OP__(8C)    OP__(8D)    OP__(8E)
            case 0x88:
                Opcode88();
                break;
            case 0x8A:
                Opcode8A();
                break;
            case 0x8C:
                Opcode8C();
                break;
            case 0x8D:
                Opcode8D();
                break;
            case 0x8E:
                Opcode8E();
                break;

            //OP__(90)    OP__(91)    OPxx(92)    OP__(94)    OP__(95)    OP__(96)
            case 0x90:
                Opcode90();
                break;
            case 0x91:
                Opcode91();
                break;
            case 0x94:
                Opcode94();
                break;
            case 0x95:
                Opcode95();
                break;
            case 0x96:
                Opcode96();
                break;

            //OP__(98)    OP__(99)    OP__(9A)    OPxx(9C)    OP__(9D)    OPxx(9E)
            case 0x98:
                Opcode98();
                break;
            case 0x99:
                Opcode99();
                break;
            case 0x9A:
                Opcode9A();
                break;
            case 0x9D:
                Opcode9D();
                break;

            //OP__(A0)    OP__(A1)    OP__(A2)    OP__(A4)    OP__(A5)    OP__(A6)
            case 0xA0:
                OpcodeA0();
                break;
            case 0xA1:
                OpcodeA1();
                break;
            case 0xA2:
                OpcodeA2();
                break;
            case 0xA4:
                OpcodeA4();
                break;
            case 0xA5:
                OpcodeA5();
                break;
            case 0xA6:
                OpcodeA6();
                break;

            //OP__(A8)    OP__(A9)    OP__(AA)    OP__(AC)    OP__(AD)    OP__(AE)
            case 0xA8:
                OpcodeA8();
                break;
            case 0xA9:
                OpcodeA9();
                break;
            case 0xAA:
                OpcodeAA();
                break;
            case 0xAC:
                OpcodeAC();
                break;
            case 0xAD:
                OpcodeAD();
                break;
            case 0xAE:
                OpcodeAE();
                break;

            //OP__(B0)    OP__(B1)    OPxx(B2)    OP__(B4)    OP__(B5)    OP__(B6)
            case 0xB0:
                OpcodeB0();
                break;
            case 0xB1:
                OpcodeB1();
                break;
            case 0xB4:
                OpcodeB4();
                break;
            case 0xB5:
                OpcodeB5();
                break;
            case 0xB6:
                OpcodeB6();
                break;

            //OP__(B8)    OP__(B9)    OP__(BA)    OP__(BC)    OP__(BD)    OP__(BE)
            case 0xB8:
                OpcodeB8();
                break;
            case 0xB9:
                OpcodeB9();
                break;
            case 0xBA:
                OpcodeBA();
                break;
            case 0xBC:
                OpcodeBC();
                break;
            case 0xBD:
                OpcodeBD();
                break;
            case 0xBE:
                OpcodeBE();
                break;

            //OP__(C0)    OP__(C1)    OPxx(C2)    OP__(C4)    OP__(C5)    OP__(C6)
            case 0xC0:
                OpcodeC0();
                break;
            case 0xC1:
                OpcodeC1();
                break;
            case 0xC4:
                OpcodeC4();
                break;
            case 0xC5:
                OpcodeC5();
                break;
            case 0xC6:
                OpcodeC6();
                break;

            //OP__(C8)    OP__(C9)    OP__(CA)    OP__(CC)    OP__(CD)    OP__(CE)
            case 0xC8:
                OpcodeC8();
                break;
            case 0xC9:
                OpcodeC9();
                break;
            case 0xCA:
                OpcodeCA();
                break;
            case 0xCC:
                OpcodeCC();
                break;
            case 0xCD:
                OpcodeCD();
                break;
            case 0xCE:
                OpcodeCE();
                break;

            //OP__(D0)    OP__(D1)    OPxx(D2)    OPxx(D4)    OP__(D5)    OP__(D6)
            case 0xD0:
                OpcodeD0();
                break;
            case 0xD1:
                OpcodeD1();
                break;
            case 0xD5:
                OpcodeD5();
                break;
            case 0xD6:
                OpcodeD6();
                break;

            //OP__(D8)    OP__(D9)    OPxx(DA)    OPxx(DC)    OP__(DD)    OP__(DE)
            case 0xD8:
                OpcodeD8();
                break;
            case 0xD9:
                OpcodeD9();
                break;
            case 0xDD:
                OpcodeDD();
                break;
            case 0xDE:
                OpcodeDE();
                break;

            //OP__(E0)    OP_d(E1)    OPxx(E2)    OP__(E4)    OP_d(E5)    OP__(E6)
            case 0xE0:
                OpcodeE0();
                break;
            case 0xE1:
                if ((this.P & D_FLAG) != 0) D_OpcoE1();
                else OpcodeE1();
                break;
            case 0xE4:
                OpcodeE4();
                break;
            case 0xE5:
                if ((this.P & D_FLAG) != 0) D_OpcoE5();
                else OpcodeE5();
                break;
            case 0xE6:
                OpcodeE6();
                break;

            //OP__(E8)    OP_d(E9)    OP__(EA)    OP__(EC)    OP_d(ED)    OP__(EE)
            case 0xE8:
                OpcodeE8();
                break;
            case 0xE9:
                if ((this.P & D_FLAG) != 0) D_OpcoE9();
                else OpcodeE9();
                break;
            case 0xEA:
                OpcodeEA();
                break;
            case 0xEC:
                OpcodeEC();
                break;
            case 0xED:
                if ((this.P & D_FLAG) != 0) D_OpcoED();
                else OpcodeED();
                break;
            case 0xEE:
                OpcodeEE();
                break;

            //OP__(F0)    OP_d(F1)    OPxx(F2)    OPxx(F4)    OP_d(F5)    OP__(F6)
            case 0xF0:
                OpcodeF0();
                break;
            case 0xF1:
                if ((this.P & D_FLAG) != 0) D_OpcoF1();
                else OpcodeF1();
                break;
            case 0xF5:
                if ((this.P & D_FLAG) != 0) D_OpcoF5();
                else OpcodeF5();
                break;
            case 0xF6:
                OpcodeF6();
                break;

            //OP__(F8)    OP_d(F9)    OPxx(FA)    OPxx(FC)    OP_d(FD)    OP__(FE)
            case 0xF8:
                OpcodeF8();
                break;
            case 0xF9:
                if ((this.P & D_FLAG) != 0) D_OpcoF9();
                else OpcodeF9();
                break;
            case 0xFD:
                if ((this.P & D_FLAG) != 0) D_OpcoFD();
                else OpcodeFD();
                break;
            case 0xFE:
                OpcodeFE();
                break;

            /* 34 - BIT - Zero Page,X */
            /* 3C - BIT - Absolute,X */
            /* 80 - BRA */
            /* 3A - DEA */
            /* 1A - INA */
            case 0x34:
                Opcode34();
                break;
            case 0x3C:
                Opcode3C();
                break;
            case 0x80:
                Opcode80();
                break;
            case 0x3A:
                Opcode3A();
                break;
            case 0x1A:
                Opcode1A();
                break;

            /* 89 - BIT - Immediate */
            case 0x89:
                Opcode89();
                break;

            /* TSB */
            case 0x04:
                Opcode04();
                break;
            case 0x0C:
                Opcode0C();
                break;

            /* TRB */
            case 0x14:
                Opcode14();
                break;
            case 0x1C:
                Opcode1C();
                break;

            /* 12 - ORA - (Indirect) */
            /* 32 - AND - (Indirect) */
            /* 52 - EOR - (Indirect) */
            /* 72 - ADC - (Indirect) */
            /* 92 - STA - (Indirect) */
            /* B2 - LDA - (Indirect) */
            /* D2 - CMP - (Indirect) */
            /* F2 - SBC - (Indirect) */
            case 0x12:
                if ((this.P & T_FLAG) != 0) T_Opco12();
                else Opcode12();
                break;
            case 0x32:
                if ((this.P & T_FLAG) != 0) T_Opco32();
                else Opcode32();
                break;
            case 0x52:
                if ((this.P & T_FLAG) != 0) T_Opco52();
                else Opcode52();
                break;
            case 0x72:
                if ((this.P & T_FLAG) != 0) if ((this.P & D_FLAG) != 0) TD_Opc72();
                else T_Opco72();
                else if ((this.P & D_FLAG) != 0) D_Opco72();
                else Opcode72();
                break;
            case 0x92:
                Opcode92();
                break;
            case 0xB2:
                OpcodeB2();
                break;
            case 0xD2:
                OpcodeD2();
                break;
            case 0xF2:
                if ((this.P & D_FLAG) != 0) D_OpcoF2();
                else OpcodeF2();
                break;

            /* PHX PHY PLX PLY */
            case 0xDA:
                OpcodeDA();
                break;
            case 0x5A:
                Opcode5A();
                break;
            case 0xFA:
                OpcodeFA();
                break;
            case 0x7A:
                Opcode7A();
                break;

            /* STZ */
            case 0x64:
                Opcode64();
                break;
            case 0x9C:
                Opcode9C();
                break;
            case 0x74:
                Opcode74();
                break;
            case 0x9E:
                Opcode9E();
                break;

            /* 7C - JMP - Absolute,X */
            case 0x7C:
                Opcode7C();
                break;

            /* BBRi */
            case 0x0F:
                Opcode0F();
                break;
            case 0x1F:
                Opcode1F();
                break;
            case 0x2F:
                Opcode2F();
                break;
            case 0x3F:
                Opcode3F();
                break;

            //OP__(4F)	OP__(5F)	OP__(6F)	OP__(7F)
            case 0x4F:
                Opcode4F();
                break;
            case 0x5F:
                Opcode5F();
                break;
            case 0x6F:
                Opcode6F();
                break;
            case 0x7F:
                Opcode7F();
                break;

            /* BBSi*/
            case 0x8F:
                Opcode8F();
                break;
            case 0x9F:
                Opcode9F();
                break;
            case 0xAF:
                OpcodeAF();
                break;
            case 0xBF:
                OpcodeBF();
                break;

            //OP__(CF)	OP__(DF)	OP__(EF)	OP__(FF)
            case 0xCF:
                OpcodeCF();
                break;
            case 0xDF:
                OpcodeDF();
                break;
            case 0xEF:
                OpcodeEF();
                break;
            case 0xFF:
                OpcodeFF();
                break;

            /* 44 - BSR */
            case 0x44:
                Opcode44();
                break;

            /* CLA CLX CLY */
            case 0x62:
                Opcode62();
                break;
            case 0x82:
                Opcode82();
                break;
            case 0xC2:
                OpcodeC2();
                break;

            /* RMBi */
            case 0x07:
                Opcode07();
                break;
            case 0x17:
                Opcode17();
                break;
            case 0x27:
                Opcode27();
                break;
            case 0x37:
                Opcode37();
                break;

            //OP__(47)	OP__(57)	OP__(67)	OP__(77)
            case 0x47:
                Opcode47();
                break;
            case 0x57:
                Opcode57();
                break;
            case 0x67:
                Opcode67();
                break;
            case 0x77:
                Opcode77();
                break;

            /* SMBi */
            case 0x87:
                Opcode87();
                break;
            case 0x97:
                Opcode97();
                break;
            case 0xA7:
                OpcodeA7();
                break;
            case 0xB7:
                OpcodeB7();
                break;

            //OP__(C7)	OP__(D7)	OP__(E7)	OP__(F7)
            case 0xC7:
                OpcodeC7();
                break;
            case 0xD7:
                OpcodeD7();
                break;
            case 0xE7:
                OpcodeE7();
                break;
            case 0xF7:
                OpcodeF7();
                break;

            /* SXY SAX SAY */
            case 0x02:
                Opcode02();
                break;
            case 0x22:
                Opcode22();
                break;
            case 0x42:
                Opcode42();
                break;

            /* F4 - SET */
            case 0xF4:
                OpcodeF4();
                break;

            /* ST0 ST1 ST2 */
            case 0x03:
                Opcode03();
                break;
            case 0x13:
                Opcode13();
                break;
            case 0x23:
                Opcode23();
                break;

            /* TMAi TAMi */
            case 0x43:
                Opcode43();
                break;
            case 0x53:
                Opcode53();
                break;

            /* TST */
            case 0x83:
                Opcode83();
                break;
            case 0x93:
                Opcode93();
                break;
            case 0xA3:
                OpcodeA3();
                break;
            case 0xB3:
                OpcodeB3();
                break;

            /* block */
            case 0x73:
                Opcode73();
                break;
            case 0xC3:
                OpcodeC3();
                break;
            case 0xD3:
                OpcodeD3();
                break;
            case 0xE3:
                OpcodeE3();
                break;
            case 0xF3:
                OpcodeF3();
                break;

            // OP__(54)	OP__(D4)	/* CSL CSH */
            case 0x54:
                Opcode54();
                break;
            case 0xD4:
                OpcodeD4();
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
                    this.A = 0;
                    this.X = 0;
                    this.Y = 0;
                    this.S = 0xFF;
                    this.P = Z_FLAG | R_FLAG | I_FLAG;
                    this.iRequest = 0;
                    this.iMask = 0xffffffff;// ~0;
                    KI_ADDCLOCK(7);
                    return;
                } else if ((this.iRequest & IRQ_RESET) != 0) {
                    //#if BUILD_HUC6280
                    this.lowClockMode = 1;
                    K_WRITEMPR(0x80, 0x00); /* IPL(TOP OF ROM) */
                    //#endif
                    this.A = 0;
                    this.X = 0;
                    this.Y = 0;
                    this.S = 0xFF;
                    this.P = Z_FLAG | R_FLAG | I_FLAG;
                    this.PC = KI_READWORD(VEC_RESET);
                    this.iRequest = 0;
                    this.iMask = 0xffffffff;// ~0;
                } else if ((this.iRequest & IRQ_NMI) != 0) {
                    KM_PUSH((this.PC >> 8) & 0xff);
                    KM_PUSH((this.PC) & 0xff);
                    KM_PUSH(this.P | R_FLAG | B_FLAG);
                    //#if BUILD_M65C02 || BUILD_HUC6280
                    this.P = (this.P & ~(D_FLAG | T_FLAG)) | I_FLAG;
                    this.iRequest &= ~(int) IRQ_NMI;
                    //#else
                    //__THIS__.P = (__THIS__.P & ~T_FLAG) | I_FLAG;   /* 6502 bug */
                    //__THIS__.iRequest &= ~(IRQ_NMI | IRQ_BRK);
                    //#endif
                    this.PC = KI_READWORD(VEC_NMI);
                    KI_ADDCLOCK(7);
                } else if ((this.iRequest & IRQ_BRK) != 0) {
                    KM_PUSH((this.PC >> 8) & 0xff);
                    KM_PUSH((this.PC) & 0xff);
                    KM_PUSH(this.P | R_FLAG | B_FLAG);
                    //#if BUILD_M65C02 || BUILD_HUC6280
                    this.P = (this.P & ~(D_FLAG | T_FLAG)) | I_FLAG;
                    //#else
                    //__THIS__.P = (__THIS__.P & ~T_FLAG) | I_FLAG;   /* 6502 bug */
                    //#endif
                    this.iRequest &= ~(int) IRQ_BRK;
                    this.PC = KI_READWORD(VEC_BRK);
                    KI_ADDCLOCK(7);
                } else if ((this.P & I_FLAG) != 0) {
                    /* Interrupt disabled */
                }
                //#if BUILD_HUC6280
                else if ((this.iMask & this.iRequest & IRQ_INT1) != 0) {
                    KM_PUSH((this.PC >> 8) & 0xff);
                    KM_PUSH((this.PC) & 0xff);
                    KM_PUSH(this.P | R_FLAG | B_FLAG);
                    this.P = (this.P & ~(D_FLAG | T_FLAG)) | I_FLAG;
                    this.PC = KI_READWORD(VEC_INT1);
                    KI_ADDCLOCK(7);
                } else if ((this.iMask & this.iRequest & IRQ_TIMER) != 0) {
                    KM_PUSH((this.PC >> 8) & 0xff);
                    KM_PUSH((this.PC) & 0xff);
                    KM_PUSH(this.P | R_FLAG | B_FLAG);
                    this.P = (this.P & ~(D_FLAG | T_FLAG)) | I_FLAG;
                    this.PC = KI_READWORD(VEC_TIMER);
                    KI_ADDCLOCK(7);
                } else if ((this.iMask & this.iRequest & IRQ_INT) != 0) {
                    KM_PUSH((this.PC >> 8) & 0xff);
                    KM_PUSH((this.PC) & 0xff);
                    KM_PUSH((this.P | R_FLAG) & ~B_FLAG);
                    this.P = (this.P & ~(D_FLAG | T_FLAG)) | I_FLAG;
                    this.PC = KI_READWORD(VEC_INT);
                    KI_ADDCLOCK(7);
                }
            }
            K_OPEXEC();
        }
    }
}
