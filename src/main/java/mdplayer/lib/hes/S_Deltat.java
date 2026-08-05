package mdplayer.lib.hes;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import vavi.util.compat.TriConsumer;


class KmifSoundDevice {

    interface dlgRelease extends Runnable {
    }

    interface dlgReset extends BiConsumer<Integer, Integer> {
    }

    interface dlgSynth extends Consumer<int[]> {
    }

    interface dlgVolume extends Consumer<Integer> {
    }

    interface dlgWrite extends BiConsumer<Integer, Integer> {
    }

    interface dlgRead extends Function<Integer, Integer> {
    }

    interface dlgSetinst extends TriConsumer<Integer, byte[], Integer> {
    }

    public Object ctx;
    public dlgRelease release;
    public dlgReset reset;
    public dlgSynth synth;
    public dlgVolume volume;
    public dlgWrite write;
    public dlgRead read;
    public dlgSetinst setInst;
}

public class S_Deltat extends KmifSoundDevice {

    public static class Kmif_LogTable {

        public static final int LOG_BITS = 12;
        public static final int LIN_BITS = 7;
        public static final int LOG_LIN_BITS = 30;

        public static Object ctx;

        public interface dlgRelease extends Consumer<Object> {
        }

        public static dlgRelease release;
        public final int[] linearTbl = new int[(1 << LIN_BITS) + 1];
        public final int[] logTbl = new int[1 << LOG_BITS];

        public static final Object log_tables_mutex = new Object();
        public static int log_tables_refcount = 0;
        public static Kmif_LogTable log_tables = null;

        public static void logTableRelease(Object ctx) {
            synchronized (log_tables_mutex) {
//                while (log_tables_mutex != 1) {
//                    XSLEEP(0);
//                }
                log_tables_refcount--;
                if (log_tables_refcount == 0) {
//                    XFREE(ctx);
                    log_tables = null;
                }
            }
        }

        private void LogTableCalc() {
            int i;
            double a;
            for (i = 0; i < (1 << LOG_BITS); i++) {
                a = (1 << LOG_LIN_BITS) / Math.pow(2, i / (double) (1 << LOG_BITS));
                this.logTbl[i] = (int) a;
            }
            this.linearTbl[0] = LOG_LIN_BITS << LOG_BITS;
            for (i = 1; i < (1 << LIN_BITS) + 1; i++) {
                int ua;
                a = i << (LOG_LIN_BITS - LIN_BITS);
                ua = (int) ((LOG_LIN_BITS - (Math.log(a) / Math.log(2))) * (1 << LOG_BITS));
                this.linearTbl[i] = ua << 1;
            }
        }

        public static Kmif_LogTable logTableAddRef() {
            synchronized (log_tables_mutex) {
                if (log_tables_refcount == 0) {
                    log_tables = new Kmif_LogTable();
                    ctx = log_tables;
                    release = Kmif_LogTable::logTableRelease;
                    log_tables.LogTableCalc();
                }
                if (log_tables != null) log_tables_refcount++;
            }
            return log_tables;
        }

        public int logToLin(int l, int sft) {
            int ret;
            int ofs;
            ofs = l + (sft << (LOG_BITS + 1));
            sft = ofs >> (LOG_BITS + 1);
            if (sft >= LOG_LIN_BITS) return 0;
            ofs = (ofs >> 1) & ((1 << LOG_BITS) - 1);
            ret = logTbl[ofs] >> sft;
            return (l & 1) != 0 ? -ret : ret;
        }
    }

    public static class YmDeltaTPcmSound_ {

        public final byte[] chMask = {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        };

        // For channel mask
        public enum enmChMask { // If you change the order, it'll be horrible.
            DEV_2A03_SQ1,
            DEV_2A03_SQ2,
            DEV_2A03_TR,
            DEV_2A03_NOISE,
            DEV_2A03_DPCM,

            DEV_FDS_CH1,

            DEV_MMC5_SQ1,
            DEV_MMC5_SQ2,
            DEV_MMC5_DA,

            DEV_VRC6_SQ1,
            DEV_VRC6_SQ2,
            DEV_VRC6_SAW,

            DEV_N106_CH1,
            DEV_N106_CH2,
            DEV_N106_CH3,
            DEV_N106_CH4,
            DEV_N106_CH5,
            DEV_N106_CH6,
            DEV_N106_CH7,
            DEV_N106_CH8,

            DEV_DMG_SQ1,
            DEV_DMG_SQ2,
            DEV_DMG_WM,
            DEV_DMG_NOISE,

            DEV_HUC6230_CH1,
            DEV_HUC6230_CH2,
            DEV_HUC6230_CH3,
            DEV_HUC6230_CH4,
            DEV_HUC6230_CH5,
            DEV_HUC6230_CH6,

            DEV_AY8910_CH1,
            DEV_AY8910_CH2,
            DEV_AY8910_CH3,

            DEV_SN76489_SQ1,
            DEV_SN76489_SQ2,
            DEV_SN76489_SQ3,
            DEV_SN76489_NOISE,

            DEV_SCC_CH1,
            DEV_SCC_CH2,
            DEV_SCC_CH3,
            DEV_SCC_CH4,
            DEV_SCC_CH5,

            DEV_YM2413_CH1,
            DEV_YM2413_CH2,
            DEV_YM2413_CH3,
            DEV_YM2413_CH4,
            DEV_YM2413_CH5,
            DEV_YM2413_CH6,
            DEV_YM2413_CH7,
            DEV_YM2413_CH8,
            DEV_YM2413_CH9,
            DEV_YM2413_BD,
            DEV_YM2413_HH,
            DEV_YM2413_SD,
            DEV_YM2413_TOM,
            DEV_YM2413_TCY,

            DEV_ADPCM_CH1,

            DEV_MSX_DA,

            DEV_MAX,
        }

        private static int DivFix(int p1, int p2, int fix) {
            int ret;
            ret = p1 / p2;
            p1 = p1 % p2;// p1 = p1 - p2 * ret;
            while (fix-- != 0) {
                p1 += p1;
                ret += ret;
                if (p1 >= p2) {
                    p1 -= p2;
                    ret++;
                }
            }
            return ret;
        }

        private static final int CPS_SHIFT = 16;
        private static final int PHASE_SHIFT = 16; // 16(fix)

        public KmifSoundDevice kmif;
        public Kmif_LogTable logtbl;

        public static class YmDeltaTpcMsound_Common_Tag {
            public int mastervolume;
            public int step;
            public int output;
            public int cnt;
            public int cps;
            public int phase;
            public int deltan;
            public int scale;
            public int mem;
            public int play;
            public int start;
            public int stop;
            public int level32;
            public int key;
            public int level;
            public int granuality;
            public int pad4_3;
            public final byte[] regs = new byte[0x10];
        }

        public YmDeltaTpcMsound_Common_Tag common = new YmDeltaTpcMsound_Common_Tag();
        public byte[] romRamBuf;
        public int romRamMask;
        public byte[] ramBuf;
        public int ramMask;
        public byte[] romBuf;
        public int romMask;
        public int ymDeltaTPcm_type;
        public int memShift;
        public int ram_size;

        public YmDeltaTPcmSound_ ymDeltaTPcmSound;

        public static final byte[] table_step = {
                1, 3, 5, 7, 9, 11, 13, 15,
                -1, -1, -1, -1, 2, 4, 6, 8
        };

        public static final byte[] table_scale = {
                57, 57, 57, 57, 77, 102, (byte) 128, (byte) 153,
                57, 57, 57, 57, 77, 102, (byte) 128, (byte) 153
        };

        public static final int[] scaleTable = {
                2, 6, 10, 14, 18, 22, 26, 30, -2, -6, -10, -14, -18, -22, -26, -30,
                2, 6, 10, 14, 19, 23, 27, 31, -2, -6, -10, -14, -19, -23, -27, -31,
                2, 6, 11, 15, 21, 25, 30, 34, -2, -6, -11, -15, -21, -25, -30, -34,
                2, 7, 12, 17, 23, 28, 33, 38, -2, -7, -12, -17, -23, -28, -33, -38,
                2, 7, 13, 18, 25, 30, 36, 41, -2, -7, -13, -18, -25, -30, -36, -41,
                3, 9, 15, 21, 28, 34, 40, 46, -3, -9, -15, -21, -28, -34, -40, -46,
                3, 10, 17, 24, 31, 38, 45, 52, -3, -10, -17, -24, -31, -38, -45, -52,
                3, 10, 18, 25, 34, 41, 49, 56, -3, -10, -18, -25, -34, -41, -49, -56,
                4, 12, 21, 29, 38, 46, 55, 63, -4, -12, -21, -29, -38, -46, -55, -63,
                4, 13, 22, 31, 41, 50, 59, 68, -4, -13, -22, -31, -41, -50, -59, -68,
                5, 15, 25, 35, 46, 56, 66, 76, -5, -15, -25, -35, -46, -56, -66, -76,
                5, 16, 27, 38, 50, 61, 72, 83, -5, -16, -27, -38, -50, -61, -72, -83,
                6, 18, 31, 43, 56, 68, 81, 93, -6, -18, -31, -43, -56, -68, -81, -93,
                6, 19, 33, 46, 61, 74, 88, 101, -6, -19, -33, -46, -61, -74, -88, -101,
                7, 22, 37, 52, 67, 82, 97, 112, -7, -22, -37, -52, -67, -82, -97, -112,
                8, 24, 41, 57, 74, 90, 107, 123, -8, -24, -41, -57, -74, -90, -107, -123,
                9, 27, 45, 63, 82, 100, 118, 136, -9, -27, -45, -63, -82, -100, -118, -136,
                10, 30, 50, 70, 90, 110, 130, 150, -10, -30, -50, -70, -90, -110, -130, -150,
                11, 33, 55, 77, 99, 121, 143, 165, -11, -33, -55, -77, -99, -121, -143, -165,
                12, 36, 60, 84, 109, 133, 157, 181, -12, -36, -60, -84, -109, -133, -157, -181,
                13, 39, 66, 92, 120, 146, 173, 199, -13, -39, -66, -92, -120, -146, -173, -199,
                14, 43, 73, 102, 132, 161, 191, 220, -14, -43, -73, -102, -132, -161, -191, -220,
                16, 48, 81, 113, 146, 178, 211, 243, -16, -48, -81, -113, -146, -178, -211, -243,
                17, 52, 88, 123, 160, 195, 231, 266, -17, -52, -88, -123, -160, -195, -231, -266,
                19, 58, 97, 136, 176, 215, 254, 293, -19, -58, -97, -136, -176, -215, -254, -293,
                21, 64, 107, 150, 194, 237, 280, 323, -21, -64, -107, -150, -194, -237, -280, -323,
                23, 70, 118, 165, 213, 260, 308, 355, -23, -70, -118, -165, -213, -260, -308, -355,
                26, 78, 130, 182, 235, 287, 339, 391, -26, -78, -130, -182, -235, -287, -339, -391,
                28, 85, 143, 200, 258, 315, 373, 430, -28, -85, -143, -200, -258, -315, -373, -430,
                31, 94, 157, 220, 284, 347, 410, 473, -31, -94, -157, -220, -284, -347, -410, -473,
                34, 103, 173, 242, 313, 382, 452, 521, -34, -103, -173, -242, -313, -382, -452, -521,
                38, 114, 191, 267, 345, 421, 498, 574, -38, -114, -191, -267, -345, -421, -498, -574,
                42, 126, 210, 294, 379, 463, 547, 631, -42, -126, -210, -294, -379, -463, -547, -631,
                46, 138, 231, 323, 417, 509, 602, 694, -46, -138, -231, -323, -417, -509, -602, -694,
                51, 153, 255, 357, 459, 561, 663, 765, -51, -153, -255, -357, -459, -561, -663, -765,
                56, 168, 280, 392, 505, 617, 729, 841, -56, -168, -280, -392, -505, -617, -729, -841,
                61, 184, 308, 431, 555, 678, 802, 925, -61, -184, -308, -431, -555, -678, -802, -925,
                68, 204, 340, 476, 612, 748, 884, 1020, -68, -204, -340, -476, -612, -748, -884, -1020,
                74, 223, 373, 522, 672, 821, 971, 1120, -74, -223, -373, -522, -672, -821, -971, -1120,
                82, 246, 411, 575, 740, 904, 1069, 1233, -82, -246, -411, -575, -740, -904, -1069, -1233,
                90, 271, 452, 633, 814, 995, 1176, 1357, -90, -271, -452, -633, -814, -995, -1176, -1357,
                99, 298, 497, 696, 895, 1094, 1293, 1492, -99, -298, -497, -696, -895, -1094, -1293, -1492,
                109, 328, 547, 766, 985, 1204, 1423, 1642, -109, -328, -547, -766, -985, -1204, -1423, -1642,
                120, 360, 601, 841, 1083, 1323, 1564, 1804, -120, -360, -601, -841, -1083, -1323, -1564, -1804,
                132, 397, 662, 927, 1192, 1457, 1722, 1987, -132, -397, -662, -927, -1192, -1457, -1722, -1987,
                145, 436, 728, 1019, 1311, 1602, 1894, 2185, -145, -436, -728, -1019, -1311, -1602, -1894, -2185,
                160, 480, 801, 1121, 1442, 1762, 2083, 2403, -160, -480, -801, -1121, -1442, -1762, -2083, -2403,
                176, 528, 881, 1233, 1587, 1939, 2292, 2644, -176, -528, -881, -1233, -1587, -1939, -2292, -2644,
                194, 582, 970, 1358, 1746, 2134, 2522, 2910, -194, -582, -970, -1358, -1746, -2134, -2522, -2910
        };

        private void writeRam(int v) {
            this.ramBuf[(this.common.mem >> 1) & this.ramMask] = (byte) v;
            this.common.mem += 1 << 1;
        }

        private int readRam() {
            int v;
            v = this.romRamBuf[(this.common.play >> 1) & this.romRamMask];
            if ((this.common.play & 1) != 0)
                v &= 0x0f;
            else
                v >>= 4;
            this.common.play += 1;
            if (this.common.play >= this.common.stop) {
                if ((this.common.regs[0] & 0x10) != 0) {
                    this.common.play = this.common.start;
                    this.common.step = 0;
                    if (this.ymDeltaTPcm_type == 3) {
                        this.common.scale = 0;
                    } else {
                        this.common.scale = 127;
                    }
                } else {
                    this.common.key = 0;
                }
            }
            return v;
        }

        private void stepDeltaT(int data) {
            if (this.ymDeltaTPcm_type == 3) { // MSM5205
                this.common.scale = this.common.scale + scaleTable[(this.common.step << 4) + (data & 0xf)];
                if (this.common.scale > 2047) this.common.scale = 2047;
                if (this.common.scale < -2048) this.common.scale = -2048;

                this.common.step += table_step[(data & 7) + 8];
                if (this.common.step > 48) this.common.step = 48;
                if (this.common.step < 0) this.common.step = 0;
            } else {
                if ((data & 8) != 0)
                    this.common.step -= (table_step[data & 7] * this.common.scale) >> 3;
                else
                    this.common.step += (table_step[data & 7] * this.common.scale) >> 3;
                if (this.common.step > ((1 << 15) - 1)) this.common.step = ((1 << 15) - 1);
                if (this.common.step < -(1 << 15)) this.common.step = -(1 << 15);
                this.common.scale = (this.common.scale * table_scale[data]) >> 6;
                if (this.common.scale > 24576) this.common.scale = 24576;
                if (this.common.scale < 127) this.common.scale = 127;
            }
        }

//#if (((-1) >> 1) == -1)
        public int SSR(int x, int y) {
            return x >> y;
        }
//#else
//        public int SSR(int x, int y) {
//            return x >= 0 ? x >> y : -((-x - 1) >> y) - 1;
//        }
//#endif

        public void synthSound(int[] p) {
            if (this.common.key != 0) {
                int step;
                this.common.cnt += this.common.cps;
                step = this.common.cnt >> CPS_SHIFT;
                this.common.cnt &= (1 << CPS_SHIFT) - 1;
                this.common.phase += step * this.common.deltan;
                step = this.common.phase >> PHASE_SHIFT;
                this.common.phase &= (1 << PHASE_SHIFT) - 1;
                if (step != 0) {
                    do {
                        stepDeltaT(readRam());
                    } while (--step != 0);
                    if (this.ymDeltaTPcm_type == 3) { // MSM5205
                        this.common.output = this.common.scale * this.common.level32;
                    } else {
                        this.common.output = this.common.step * this.common.level32;
                    }
                    this.common.output = SSR(this.common.output, 8 + 2);
                }
                if (chMask[enmChMask.DEV_ADPCM_CH1.ordinal()] != 0) {
                    p[0] += this.common.output;
                    p[1] += this.common.output;
                }
            }
        }

        public void writeSound(int a, int v) {
            this.common.regs[a] = (byte) v;
            switch (a) {
            // START,REC,MEMDATA,REPEAT,SPOFF,--,--,RESET
            case 0x00: // Control Register 1
                if ((v & 0x80) != 0 && this.common.key == 0) {
                    this.common.key = 1;
                    this.common.play = this.common.start;
                    this.common.step = 0;
                    if (this.ymDeltaTPcm_type == 3) { // MSM5205
                        this.common.scale = 0;
                    } else {
                        this.common.scale = 127;
                    }
                }
                if ((v & 1) != 0) this.common.key = 0;
                break;
            // L,R,-,-,SAMPLE,DA/AD,RAMTYPE,ROM
            case 0x01: // Control Register 2
                // MSX-AUDIO does not have ADPCM ROM, so disable it.
                //sndp.romRamBuf  = (sndp.common.regs[1] & 1) ? sndp.romBuf  : sndp.ramBuf;
                //sndp.romRamMask = (sndp.common.regs[1] & 1) ? sndp.romMask : sndp.ramMask;
                break;
            case 0x02: // Start Address L
            case 0x03: // Start Address H
                this.common.granuality = (v & 2) != 0 ? 1 : 4;
                this.common.start = (((this.common.regs[3] & 0xff) << 8) + (this.common.regs[2] & 0xff)) << (this.memShift + 1);
                this.common.mem = this.common.start;
                break;
            case 0x04: // Stop Address L
            case 0x05: // Stop Address H
                this.common.stop = (((this.common.regs[5] & 0xff) << 8) + (this.common.regs[4] & 0xff)) << (this.memShift + 1);
                break;
            case 0x06: // Prescale L
            case 0x07: // Prescale H
                break;
            case 0x08: // data
                if ((this.common.regs[0] & 0x60) == 0x60) writeRam(v);
                break;
            case 0x09: // Delta-N L
            case 0x0a: // Delta-N H
                this.common.deltan = ((this.common.regs[0xa] & 0xff) << 8) + (this.common.regs[0x9] & 0xff);
                if (this.common.deltan < 0x100) this.common.deltan = 0x100;
                break;
            case 0x0b: // Level Control
                this.common.level = v;
                this.common.level32 = (this.common.level * this.logtbl.logToLin(this.common.mastervolume, Kmif_LogTable.LOG_LIN_BITS - 15)) >> 7;
                if (this.ymDeltaTPcm_type == 3) { // MSM5205
                    this.common.output = this.common.scale * this.common.level32;
                } else {
                    this.common.output = this.common.step * this.common.level32;
                }
                this.common.output = SSR(this.common.output, 8 + 2);
                break;
            }
        }

        public int readSound(int a) {
            return 0;
        }

        public void resetSound(int clock, int freq) {
            //XMEMSET(&sndp.common, 0, sizeof(sndp.common));
            this.common = new YmDeltaTpcMsound_Common_Tag();
            this.common.cps = DivFix(clock, 72 * freq, CPS_SHIFT);
            this.romRamBuf = (this.common.regs[1] & 1) != 0 ? this.romBuf : this.ramBuf;
            this.romRamMask = (this.common.regs[1] & 1) != 0 ? this.romMask : this.ramMask;
            this.common.granuality = 4;
        }

        public void volumeSound(int volume) {
            volume = (volume << (Kmif_LogTable.LOG_BITS - 8)) << 1;
            this.common.mastervolume = volume;
            this.common.level32 = (this.common.level * this.logtbl.logToLin(this.common.mastervolume, Kmif_LogTable.LOG_LIN_BITS - 15)) >> 7;
            this.common.output = this.common.step * this.common.level32;
            this.common.output = SSR(this.common.output, 8 + 2);
        }

        public void releaseSound() {
            if (this != null) {
                YmDeltaTPcmSound_ s = this;
                if (s.logtbl != null) Kmif_LogTable.release.accept(Kmif_LogTable.ctx);
            }
        }

        public void setInst(int n, byte[] p, int l) {
            if (n != 0) return;
            if (p != null) {
                this.romBuf = p;
                this.romMask = l - 1;
                this.romRamBuf = (this.common.regs[1] & 1) != 0 ? this.romBuf : this.ramBuf;
                this.romRamMask = (this.common.regs[1] & 1) != 0 ? this.romMask : this.ramMask;
            } else {
                this.romBuf = null;
                this.romMask = 0;
            }
        }
    }

    // Register viewer settings from here

    private YmDeltaTPcmSound_ sndpr;

    // ioview_ioread_DEV_ADPCM
    private Function<Integer, Integer> ioview_ioread_DEV_ADPCM_;

    // ioview_ioread_DEV_ADPCM2
    private Function<Integer, Integer> ioview_ioread_DEV_ADPCM2_;

    private int ioview_ioread_bf(int a) {
        if (a <= 0xb) return sndpr.common.regs[a];
        else return 0x100;
    }

    private int ioview_ioread_bf2(int a) {
        if (a < sndpr.ram_size) return sndpr.ramBuf[a];
        else return 0x100;
    }

    // Register viewer settings up to here

    private KmifSoundDevice alloc(int ymDeltaTPcmType, byte[] pcmBuf) {
        int ramSize;
        YmDeltaTPcmSound_ sndp;
        ramSize = switch (ymDeltaTPcmType) {
            case 0 -> 32 * 1024;  // YMDELTATPCM_TYPE_Y8950
            case 1 -> 256 * 1024; // YMDELTATPCM_TYPE_YM2608
            case 3 -> 256 * 256;  // MSM5205
            default -> 0;
        };
        sndp = new YmDeltaTPcmSound_();
        sndp.ramBuf = new byte[ramSize];
        if (sndp == null) return null;
        sndp.ram_size = ramSize;
        sndp.ymDeltaTPcm_type = ymDeltaTPcmType;
        sndp.memShift = switch (ymDeltaTPcmType) {
            case 0 -> 2; // YMDELTATPCM_TYPE_Y8950
            case 1 -> 6; // YMDELTATPCM_TYPE_YM2608 // OPNA
            case 2 -> 9; // YMDELTATPCM_TYPE_YM2610
            case 3 -> 0; // MSM5205
            default -> 0;
        };
        sndp.kmif.ctx = sndp;
        sndp.kmif.release = sndp::releaseSound;
        sndp.kmif.synth = sndp::synthSound;
        sndp.kmif.volume = sndp::volumeSound;
        sndp.kmif.reset = sndp::resetSound;
        sndp.kmif.write = sndp::writeSound;
        sndp.kmif.read = sndp::readSound;
        sndp.kmif.setInst = sndp::setInst;
        // RAM
        if (pcmBuf != null)
            sndp.ramBuf = pcmBuf;
        else
            sndp.ramBuf = ramSize != 0 ? sndp.ramBuf : null;
        sndp.ramMask = ramSize != 0 ? (ramSize - 1) : 0;
        // ROM
        sndp.romBuf = null;
        sndp.romMask = 0;
        sndp.logtbl = Kmif_LogTable.logTableAddRef();
        if (sndp.logtbl == null) {
            sndp.releaseSound();
            return null;
        }
        // Register viewer settings from here
        sndpr = sndp;
        if (ioview_ioread_DEV_ADPCM_ == null) ioview_ioread_DEV_ADPCM_ = this::ioview_ioread_bf;
        if (ioview_ioread_DEV_ADPCM2_ == null) ioview_ioread_DEV_ADPCM2_ = this::ioview_ioread_bf2;
        // Register viewer settings up to here
        return sndp.kmif;
    }
}
