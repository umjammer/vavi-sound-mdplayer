package mdplayer.lib.nrtdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;


public class NRTDRV {

    private static final Logger logger = getLogger(NRTDRV.class.getName());

    public byte[] ram;
    public final Work work = new Work();

    private static final byte[] KTABLE = {
            // C     C+    D     D +   E     F     F+    G     G+    a     a+    B
            0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x04, 0x05, 0x06, 0x08, 0x09, 0x0a, // o0
            0x0c, 0x0d, 0x0e, 0x10, 0x11, 0x12, 0x14, 0x15, 0x16, 0x18, 0x19, 0x1a, // o1
            0x1c, 0x1d, 0x1e, 0x20, 0x21, 0x22, 0x24, 0x25, 0x26, 0x28, 0x29, 0x2a, // o2
            0x2c, 0x2d, 0x2e, 0x30, 0x31, 0x32, 0x34, 0x35, 0x36, 0x38, 0x39, 0x3a, // o3
            0x3c, 0x3d, 0x3e, 0x40, 0x41, 0x42, 0x44, 0x45, 0x46, 0x48, 0x49, 0x4a, // o4
            0x4c, 0x4d, 0x4e, 0x50, 0x51, 0x52, 0x54, 0x55, 0x56, 0x58, 0x59, 0x5a, // o5
            0x5c, 0x5d, 0x5e, 0x60, 0x61, 0x62, 0x64, 0x65, 0x66, 0x68, 0x69, 0x6a, // o6
            0x6c, 0x6d, 0x6e, 0x70, 0x71, 0x72, 0x74, 0x75, 0x76, 0x78, 0x79, 0x7a, // o7
            0x7c, 0x7d, 0x7e, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, // o8
            0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f  // o9
    };

    private static final int[] PTABLE = {
            // C    C+   D    D+   E    F    F+   G    G+   a    a+   B
            4095, 4036, 3980, 3978, 3924, 3894, 3868, 3812, 3756, 3700, 3644, 3588, // o0
            3532, 3476, 3420, 3228, 3047, 2876, 2715, 2562, 2419, 2283, 2155, 2034, // o1
            1920, 1812, 1711, 1614, 1524, 1438, 1358, 1281, 1210, 1142, 1078, 1017, // o2
            960, 906, 855, 807, 762, 719, 679, 641, 605, 571, 539, 509,             // o3
            480, 453, 428, 404, 381, 360, 339, 320, 302, 285, 269, 254,             // o4
            240, 227, 214, 202, 190, 180, 170, 160, 151, 143, 135, 127,             // o5
            120, 113, 107, 101, 95, 90, 85, 80, 76, 71, 67, 64,                     // o6
            60, 57, 53, 50, 48, 45, 42, 40, 38, 36, 34, 32,                         // o7
            30, 28, 27, 25, 24, 22, 21, 20, 19, 18, 17, 16,                         // o8
            15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4                                // o9
    };

    private static final byte[] PMTBL = {
            0x3f, 0x09, // Ch.1/P0
            0x3e, 0x08, // Ch.1/P1
            0x37, 0x01, // Ch.1/P2
            0x36, 0x00, // Ch.1/P3

            0x3f, 0x12, // Ch.2/P0
            0x3d, 0x10, // Ch.2/P1
            0x2f, 0x02, // Ch.2/P2
            0x2d, 0x00, // Ch.2/P3

            0x3f, 0x24, // Ch.3/P0
            0x3b, 0x20, // Ch.3/P1
            0x1f, 0x04, // Ch.3/P2
            0x1b, 0x00  // Ch.3/P3
    };

    private static final byte[] TTONE = {
            (byte) 162, 1, 0,                      // +3
            75, 0, 74, 0, 74, 0, 74, 0,            // +8
            74, 0, 74, 0, 74, 0, 74, 0,            // +8
            126, 0, 74, 0, 74, 0, 74, 0,           // +8
            74, 0, 74, 0, 74, 0, 74, 0,            // +8
            (byte) 175, 0, 74, 0, 74, 0,           // +6
            15, (byte) 255, 2,                     // +3 PVX
            0x3c,                                  // +1 FVX
            0x18,                                  // +1
            0x02, 0x00, 0x01, 0x00,                // +4
            0x1c, 0x7f, 0x02, (byte) 0x81,         // +4
            0x1e, 0x00, 0x1f, 0x00,                // +4
            0x00, 0x00, 0x00, 0x00,                // +4
            0x00, 0x00, 0x00, 0x00,                // +4
            0x0f, (byte) 0xff, 0x0f, (byte) 0xff,  // +4
            0x1c, 0x7f, 0x00, 0x7f,                // +4
            126,                                   // +1 TR0
            3, 125, 14,                            // +3 TR1
            44, 0,                                 // +2
            (byte) 176, 24, (byte) 178, 24, (byte) 180, 24, (byte) 181, 24, (byte) 183, 24, (byte) 185, 24, (byte) 187, 24, (byte) 188, 24, 0, (byte) 255, (byte) 129, 0, 48, // +21
            (byte) 188, 24, (byte) 187, 24, 0, 96, (byte) 178, 24, (byte) 176, 24, 0, 48, (byte) 176, (byte) 192, 0, 48, 25, 6, // +18
            (byte) 188, 24, 0, 72, 127,            // +5 TR1L
            119, 0,                                // +2
            3, 125, 14,                            // +3 TR2
            44, 0,                                 // +2
            0, (byte) 255, (byte) 129, (byte) 176, 24, (byte) 178, 24, (byte) 180, 24, (byte) 181, 24, (byte) 183, 24, (byte) 185, 24, (byte) 187, 24, (byte) 188, 24, 0, 48, // +21
            0, 96, (byte) 181, 24, (byte) 180, 24, 0, (byte) 192, (byte) 183, 96, 0, 48, 25, 6, // +14
            0, 48, (byte) 188, 24, 0, 24, 127,     // +7 TR2L
            (byte) 166, 0,                         // +2
            14,                                    // +1 TR3
            41, 0,                                 // +2
            19, 112, 0, (byte) 192, (byte) 176, 24, (byte) 178, 24, (byte) 180, 24, (byte) 181, 24, (byte) 183, 24, (byte) 185, 24, (byte) 187, 24, (byte) 188, 24, 0, (byte) 192, // +22
            0, 96, (byte) 185, 24, (byte) 183, 24, 0, (byte) 192, (byte) 180, (byte) 144, 0, 48, 25, 6, // +14
            0, 24, (byte) 188, 24, 0, 48, 127,    // +7 TR3L
            (byte) 214, 0                         // +2
    };

    public boolean isRealModel;
    public IntConsumer loop;

    public TriConsumer<Integer, Integer, Integer> ym2151WriteV;
    public TriConsumer<Integer, Integer, Integer> ym2151WriteR;
    public BiConsumer<Integer, Integer> ay8910WriteV;

    public int checkUseChip(byte[] buf) {
        int trkPtr = 3;

        boolean flg = false;
        for (int i = 0; i < 8; i++) {
            int l = buf[trkPtr] & 0xff;
            int h = buf[trkPtr + 1] & 0xff;
            int hl = (h << 8) + l;
            int cmd = buf[hl] & 0xff;
            while (cmd != 127) {
                if ((cmd & 0x80) != 0) {
                    flg = true;
                } else {
                    switch (cmd) {
                    case 0x00:
                        hl++;
                        while (buf[hl] == (byte) 255) hl++;
                        break;
                    case 0x0e:
                        hl += 2;
                        break;
                    case 0x0f:
                    case 0x13:
                    case 0x14:
                    case 0x15:
                        hl++;
                        break;
                    case 0x11:
                    case 0x12:
                    case 0x16:
                    case 0x17:
                    case 0x7d:
                    case 0x7e:
                        break;
                    default:
                        flg = true;
                        break;
                    }
                }

                if (flg) break;

                hl++;
                cmd = buf[hl] & 0xff;
            }

            if (flg) break;
            trkPtr += 2;
        }
        boolean useOPM1 = flg;

        trkPtr = 3 + 2 * 8;
        flg = false;
        for (int i = 0; i < 8; i++) {
            int l = buf[trkPtr] & 0xff;
            int h = buf[trkPtr + 1] & 0xff;
            int hl = (h << 8) + l;
            int cmd = buf[hl] & 0xff;
            while (cmd != 127) {
                if ((cmd & 0x80) != 0) {
                    flg = true;
                } else {
                    switch (cmd) {
                    case 0x00:
                        hl++;
                        while (buf[hl] == (byte) 255) hl++;
                        break;
                    case 0x0e:
                        hl += 2;
                        break;
                    case 0x0f:
                    case 0x13:
                    case 0x14:
                    case 0x15:
                        hl++;
                        break;
                    case 0x11:
                    case 0x12:
                    case 0x16:
                    case 0x17:
                    case 0x7d:
                    case 0x7e:
                        break;
                    default:
                        flg = true;
                        break;
                    }
                }

                if (flg) break;

                hl++;
                cmd = buf[hl] & 0xff;
            }

            if (flg) break;
            trkPtr += 2;
        }
        boolean useOPM2 = flg;

        trkPtr = 3 + 2 * 16;
        flg = false;
        for (int i = 0; i < 3; i++) {
            int l = buf[trkPtr] & 0xff;
            int h = buf[trkPtr + 1] & 0xff;
            int hl = (h << 8) + l;
            int cmd = buf[hl] & 0xff;
            while (cmd != 127) {
                if ((cmd & 0x80) != 0) {
                    flg = true;
                } else {
                    switch (cmd) {
                    case 0x00:
                        hl++;
                        while (buf[hl] == (byte) 255) hl++;
                        break;
                    case 0x0e:
                        hl += 2;
                        break;
                    case 0x0f:
                    case 0x13:
                    case 0x14:
                    case 0x15:
                        hl++;
                        break;
                    case 0x11:
                    case 0x12:
                    case 0x16:
                    case 0x17:
                    case 0x7d:
                    case 0x7e:
                        break;
                    default:
                        flg = true;
                        break;
                    }
                }

                if (flg) break;

                hl++;
                cmd = buf[hl] & 0xff;
            }

            if (flg) break;
            trkPtr += 2;
        }
        boolean usePSG = flg;

        return (usePSG ? 4 : 0) | (useOPM2 ? 2 : 0) | (useOPM1 ? 1 : 0);
    }

    public void call(int cmdNo) {
        switch (cmdNo) {
        case 0:
        case 4:
            drvini();
            break;
        case 1:
            mplay();
            break;
        case 2:
            mstop();
            break;
        case 3:
            mfade();
            break;
        case 5:
            mtest();
            break;
        case 6:
            mplayd();
            break;
        }
    }

    public boolean isPlaying() {
        int loop = Integer.MAX_VALUE;
        boolean flg = false;

        for (Ch c : work.opm1Chs) {
            //if (c.TrackStopFlg == 255)
            if (c.trackStopFlg != 0) {
                continue;
            }
            loop = Math.min(c.loopCounter & 0xff, loop);
            flg = true;
        }
        for (Ch c : work.opm2Chs) {
            //if (c.TrackStopFlg == 255)
            if (c.trackStopFlg != 0) {
                continue;
            }
            loop = Math.min(c.loopCounter & 0xff, loop);
            flg = true;
        }
        for (Ch c : work.psgChs) {
            //if (c.TrackStopFlg == 255)
            if (c.trackStopFlg != 0) {
                continue;
            }
            loop = Math.min(c.loopCounter & 0xff, loop);
            flg = true;
        }

        this.loop.accept(loop);
        return flg;
    }

    private float ctc0DownCounter = 0.0f;
    private float ctc0DownCounterMAX = 0.0f;
    private boolean ctc0Paluse = false;
    private float ctc1DownCounter = 0.0f;
    private float ctc1DownCounterMAX = 0.0f;
    //private boolean ctc1Paluse = false;
    private float ctc3DownCounter = 0.0f;
    private float ctc3DownCounterMAX = 0.0f;
    //private boolean ctc3Paluse = false;
    public float ctcStep; // sampleRate;
    public float ctc1Step; // sampleRate;

    public void oneFrameMain() {
        try {
            // KUMA: (CTC0 & 0x40)==0 is always true
            //ctc0DownCounterMAX = (work.ctc0TimeConstant == 0 ? 0x100 : work.ctc0TimeConstant & 0xff) * ((work.ctc0 & 0x40) == 0 ? ((work.ctc0 & 0x20) != 0 ? 256.0f : 16.0f) : 1);
            ctc0DownCounterMAX = (work.ctc0TimeConstant == 0 ? 0x100 : work.ctc0TimeConstant & 0xff) * ((work.ctc0 & 0x20) != 0 ? 256.0f : 16.0f);
            // KUMA: (CTC1 & 0x40)==0 is always true
            //ctc1DownCounterMAX = (work.ctc1TimeConstant == 0 ? 0x100 : work.ctc1TimeConstant & 0xff) * ((work.ctc1 & 0x40) == 0 ? ((work.ctc1 & 0x20) != 0 ? 256.0f : 16.0f) : 1);
            ctc1DownCounterMAX = (work.ctc1TimeConstant == 0 ? 0x100 : work.ctc1TimeConstant & 0xff) * ((work.ctc1 & 0x20) != 0 ? 256.0f : 16.0f);
            ctc3DownCounterMAX = (work.ctc3TimeConstant == 0 ? 0x100 : work.ctc3TimeConstant & 0xff) * ((work.ctc3 & 0x40) == 0 ? ((work.ctc3 & 0x20) != 0 ? 256.0f : 16.0f) : 1);
            ctc0Paluse = false;
            //ctc3Paluse = false;

            // KUMA: (CTC0 & 0x40)==0 is always true
            // ctc0
            //if ((work.ctc0 & 0x40) == 0) {
            //    // Timer Mode
            ctc0DownCounter -= ctcStep;
            //} else {
            //    // CounterMode None
            //    ;
            //}

            if (ctc0DownCounter <= 0.0f) {
                ctc0Paluse = true;
                //if ((work.ctc0 & 0x80) != 0) {
                //    parse();
                //}
                ctc0DownCounter += ctc0DownCounterMAX;
            }

            // KUMA: (CTC1 & 0x40)==0 is always true
            // ctc1
            //if ((work.ctc1 & 0x40) == 0) {
            //    // Timer Mode
            ctc1DownCounter -= ctc1Step;
            //} else {
            //    // CounterMode None
            //    ;
            //}

            if (ctc1DownCounter <= 0.0f) {
                // ctc1Paluse = true;
                if ((work.ctc1 & 0x80) != 0) {
                    work.int2();
                }
                ctc1DownCounter += ctc1DownCounterMAX;
            }

            // ctc3
            if ((work.ctc3 & 0x40) == 0) {
                // Timer Mode
                ctc3DownCounter -= 1.0f;
            } else if (ctc0Paluse) {
                // Counter Mode(Counting the pulses of ctc0)
                ctc3DownCounter -= 1.0f;
            }

            if (ctc3DownCounter <= 0.0f) {
                // CTC3Paluse = true;
                if ((work.ctc3 & 0x80) != 0) {
                    work.imain();
                }
                ctc3DownCounter = ctc3DownCounterMAX;
            }

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void drvini() {

        work.ctcFlg = 0;
        work.ctcFlg |= 2; // 2 if OPM2 is used, 0 if not
        work.ctcFlg |= 1; // 1 if OPM1 is used, 0 if not
        work.ctcFlg |= 4; // 4 if turbo spec, 0 if not

        work.ctc3io = 0x1fa3; // Turbo ctc3 IO port number saved. 0x0707 if not saved
    }

    private void mplay() {
        mstop();

        // DI

        work.ctc0 = 0x27; // TimerMode PreScal = 256
        work.ctc0TimeConstant = ram[work.bgmAdr];
        work.ctc1 = (byte) 0xb7;
        work.ctc1TimeConstant = 0;
        work.ctc3 = (byte) 0xc7; // CounterMode PreScal = none
        work.ctc3TimeConstant = ram[work.bgmAdr + 1];
        work.mVol = 0;
        work.ffFlg = 0;
        work.fSpeed = 5;
        work.opmT02_LightMode = true;
        work.opmT14_MVMode = false;
        work.plyFlg &= 0xc0;
        work.plyFlg |= 0x01;
        work.opmKeyONEnable = false;
        work.opmRestEnable = false;
        work.opmT19Enable = false;
        work.psgKeyONEnable = false;
        work.psgRestEnable = false;

        // EI at the end of interrupt routine is invalid

        work.imain();
        if (isRealModel) {
            //plugin.audio.chipRegister.sendDataYM2151(0, model);
            //plugin.audio.chipRegister.setYM2151SyncWait(0, 1);
            //plugin.audio.chipRegister.sendDataYM2151(1, model);
            //plugin.audio.chipRegister.setYM2151SyncWait(1, 1);
        }

        work.opmKeyONEnable = true;
        work.psgKeyONEnable = true;
        work.opmRestEnable = true;
        work.psgRestEnable = true;
        work.opmT19Enable = true;

        // EI valid at the end of interrupt routine

        // EI
    }

    private void mstop() {

        // DI

        int hl = work.bgmAdr;
        hl += 2;
        byte c = ram[hl];

        work.opmFlg = (byte) (ram[hl] & 1); // OPM wait flag storage
        if ((ram[hl] & 2) != 0) {
            work.keyOnFOpmMask = 0;
            work.PENVF_VOL0 = true;
        }

        work.KEYON_LightMode = false;
        work.PKEYON_LightMode = false;
        if ((ram[hl] & 4) != 0) {
            work.KEYON_LightMode = true;
            work.PKEYON_LightMode = true;
        }

        hl++;
        if ((work.ctcFlg & 1) != 0) {
            work.opmIo = 0x701;
            minit(hl);
        }
        hl += 2 * 8;
        if ((work.ctcFlg & 2) != 0) {
            work.opmIo = 0x709;
            minit(hl);
        }

        hl = work.bgmAdr;
        hl += 35;
        pinit(hl);
        ctcrst();

        work.plyFlg &= 0xc0;
        work.count = 0;
        work.totalCount = 0;

        // EI
    }

    private void mfade() {
        if ((work.plyFlg & 0x3) == 0) return;
        work.MFADE1((byte) 5);
    }

    private void mtest() {
        System.arraycopy(TTONE, 0, ram, 16384, TTONE.length);
        work.bgmAdr = 0x4000;
        mplay();
    }

    private void mplayd() {
        work.bgmAdr = 0x4000;
        mplay();
    }

    private void ctcrst() {
        work.ctc3 = 3;
        work.ctc2 = 3;
        work.ctc1 = 3;
        work.ctc0 = 3;
    }

    private final byte[] psrtbl = new byte[] {(byte) 0xe0, 1, (byte) 0xe0, 1, (byte) 0xe0, 1, 0, 0x38, 0, 0, 0, 0, 0x10, 0};

    private void pinit(int hl) {
        Ch[] wChs = work.psgChs;
        for (int b = 0; b < 3; b++) {
            int de = (ram[hl] & 0xff) + (ram[hl + 1] & 0xff) * 0x100;
            hl += 2;
            wChs[b].ptrData = de + work.bgmAdr;
            wChs[b].wreset();
            wChs[b].psgToneStartAdr = 0;
            wChs[b].psgToneAdr = 0;
            wChs[b].psgTone = 480;
            wChs[b].psgHardEnvelopeType = 16;

            byte a = (byte) (work.plyFlg & 0x80);
            if (a != 0) {
                a = (byte) (wChs[b].lfoFlags & 0x80);
            }
            wChs[b].lfoFlags = a;
        }

        work.pFlg = 0x38;
        //byte[] psrtbl = new byte[] { 0xe0, 1, 0xe0, 1, 0xe0, 1, 0, 0x38, 0, 0, 0, 0, 0x10, 0 };
        for (byte d = 0; d < psrtbl.length; d++) {
            wpsg(d, psrtbl[d]);
        }
    }

    private void minit(int hl) {
        lreset();
        wopm((byte) 0xf, (byte) 0); // OPM register 0FH=0 (noise initialization)

        Ch[] wChs = work.opm1Chs;
        if (work.opmIo != 0x701) {
            wChs = work.opm2Chs;
        }
        for (byte b = 0; b < 8; b++) {
            opmrst(b);
            int de = (ram[hl] & 0xff) + (ram[hl + 1] & 0xff) * 0x100;
            hl += 2;
            wChs[b].ptrData = de + work.bgmAdr;
            wChs[b].wreset();
            wChs[b].kf = 0;
            wChs[b].softAMFlagAndDelay = 0;
            wChs[b].noteNumber = 48;
            wChs[b].op1Tl = (byte) 255;
            wChs[b].op2Tl = (byte) 255;
            wChs[b].op3Tl = (byte) 255;
            wChs[b].op4Tl = (byte) 255;

            byte a = 0x78;
            if ((work.plyFlg & 0x80) != 0) {
                a = (byte) (wChs[b].lfoFlags & 0xf8);
            }
            wChs[b].lfoFlags = a;
        }
    }

    private void opmrst(byte b) {
        // KEY OFF
        wopm((byte) 8, b);

        // TL
        wopm((byte) (0x60 + b), (byte) 127);
        wopm((byte) (0x68 + b), (byte) 127);
        wopm((byte) (0x70 + b), (byte) 127);
        wopm((byte) (0x78 + b), (byte) 127);

        // SL/RR
        wopm((byte) (0xe0 + b), (byte) 255);
        wopm((byte) (0xe8 + b), (byte) 255);
        wopm((byte) (0xf0 + b), (byte) 255);
        wopm((byte) (0xf8 + b), (byte) 255);

        // KC
        wopm((byte) (0x28 + b), (byte) 0x3c);
        // KF
        wopm((byte) (0x30 + b), (byte) 0);
        // PMS/AMS
        wopm((byte) (0x38 + b), (byte) 0);
    }

    private void lreset() {
        wopm((byte) 1, (byte) 2);
        wopm((byte) 1, (byte) 0);
    }

    private void wopm(byte d, byte a) {
        if (!isRealModel) {
            if (work.opmIo == 0x701) {
                // Write to a virtual register
                work.opm1VReg[d & 0xff] = a;
                // Write to real register
                ym2151WriteV.accept(0, d & 0xff, a & 0xff);
                //logger.log(Level.TRACE, "OPM1 Reg%02x Dat%02x".formatted(d, a));
            } else {
                // Write to a virtual register
                work.opm2VReg[d & 0xff] = a;
                // Write to real register
                ym2151WriteV.accept(1, d & 0xff, a & 0xff);
                //logger.log(Level.TRACE, "OPM2 Reg%02x Dat%02x".formatted(d, a));
            }
        } else {
            if (work.opmIo == 0x701) {
                // Write to a virtual register
                work.opm1VReg[d & 0xff] = a;
                // Write to real register
                ym2151WriteR.accept(0, d & 0xff, a & 0xff);
                //logger.log(Level.TRACE, "OPM1 Reg%02x Dat%02x".formatted(d, a));
            } else {
                // Write to a virtual register
                work.opm2VReg[d & 0xff] = a;
                // Write to real register
                ym2151WriteR.accept(1, d & 0xff, a & 0xff);
                //logger.log(Level.TRACE, "OPM2 Reg%02x Dat%02x".formatted(d, a));
            }
        }
    }

    private void wpsg(byte d, byte a) {
        if (!isRealModel) {
            //out(0x1c00, d); // Psg register
            //out(0x1b00, a); // Psg data
            ay8910WriteV.accept(d & 0xff, a & 0xff);
//        } else {
        }
    }

    public class Work {
        public int ctcFlg = 0;
        public int ctc3io = 0;
        public byte ctc0 = 0;
        public byte ctc1 = 0;
        public byte ctc2 = 0;
        public byte ctc3 = 0;
        public byte ctc0TimeConstant = 0;
        public byte ctc1TimeConstant = 0;
        public byte ctc3TimeConstant = 0;
        public int bgmAdr = 0x4000; // Default Address
        public byte opmFlg = 0;
        public int opmIo = 0x701;
        public byte plyFlg = 0;
        public byte count = 0;
        public byte zCount = 0;
        public short totalCount = 0;
        public byte ffFlg = 0;
        public byte pFlg = 0x38;
        public byte mVol = 0;
        public byte fCount = 0;
        public byte fSpeed = 5;
        public byte ver = 2;

        public final Ch[] opm1Chs = new Ch[] {new Ch(), new Ch(), new Ch(), new Ch(), new Ch(), new Ch(), new Ch(), new Ch()};
        public final Ch[] opm2Chs = new Ch[] {new Ch(), new Ch(), new Ch(), new Ch(), new Ch(), new Ch(), new Ch(), new Ch()};
        public final Ch[] psgChs = new Ch[] {new Ch(), new Ch(), new Ch()};

        public final byte[] opm1VReg = new byte[256];
        public final byte[] opm2VReg = new byte[256];

        public byte amd1 = 0;
        public byte pmd1 = 0;
        public byte amd2 = 0;
        public byte pmd2 = 0;

        // Flag for countermeasures against direct instruction rewrite processing

        public byte keyOnFOpmMask = 0x78;
        public boolean PENVF_VOL0 = false;
        public boolean KEYON_LightMode = false;
        public boolean PKEYON_LightMode = false;
        public boolean opmT02_LightMode = true;
        public boolean opmT14_MVMode = false;
        public boolean opmKeyONEnable = true;
        public boolean opmRestEnable = true;
        public boolean opmT19Enable = true;
        public boolean psgKeyONEnable = true;
        public boolean psgRestEnable = true;

        private void imain() {
            if ((this.plyFlg & 0x40) != 0) {
                return;
            }

            this.totalCount += 1; // 0 - 65535 (short)

            // OPM1
            if ((this.ctcFlg & 0x1) != 0) {
                this.opmIo = 0x701;
                mmain(this.opm1Chs);
            }

            // OPM2
            if ((this.ctcFlg & 0x2) != 0) {
                this.opmIo = 0x709;
                mmain(this.opm2Chs);
            }

            // Psg
            this.opmIo = 0;
            pmain(this.psgChs);

            // FadeOut
            if ((this.plyFlg & 0x2) == 0) return;

            this.fCount++;

            if ((this.fSpeed & 0xff) >= (this.fCount & 0xff)) return;

            this.fCount = 0;

            this.mVol++;

            if ((this.mVol & 0xff) <= 127) return;

            this.plyFlg &= 0xc0;
            this.plyFlg |= 0x4;

            for (Ch wch : this.opm1Chs) {
                wch.trackStopFlg = (byte) 0xff;
            }

            for (Ch wch : this.opm2Chs) {
                wch.trackStopFlg = (byte) 0xff;
            }

            for (Ch wch : this.psgChs) {
                wch.trackStopFlg = (byte) 0xff;
            }
        }

        private void int2() {
            this.count--;

            // OPM1
            if ((this.ctcFlg & 0x1) != 0) {
                this.opmIo = 0x701;
                efx(this.opm1Chs);
            }
            // OPM2
            if ((this.ctcFlg & 0x2) != 0) {
                this.opmIo = 0x709;
                efx(this.opm2Chs);
            }
            // Psg
            this.opmIo = 0;
            efxp(this.psgChs);
        }

        private void MFADE1(byte a) {
            this.fSpeed = a;
            this.opmT02_LightMode = true;
            this.opmT14_MVMode = true;
            a = this.plyFlg;
            a &= 0xc0;
            a |= 2;
            this.plyFlg = a;
        }
    }

    public class Ch {
        public static class RepBuf {
            public byte count = 0;
            public int startAdr = 0;
            public int endAdr = 0;
        }

        public int ptrData = 0; // (IX,IX+1)
        public byte loopCounter = 0; // IX+2
        public byte counter = 0; // IX+3
        public byte detune = 0; // IX+4
        public int macroReturnAdr = 0; // IX+5,IX+6
        public byte panAlgFb = (byte) 0xc0; // (IX+7)
        public byte op1Tl = 0; // IX+8
        public byte op2Tl = 0; // IX+9
        public byte op3Tl = 0; // IX+10
        public byte op4Tl = 0; // IX+11
        public int psgToneStartAdr = 0; // IX+8,IX+9
        public int psgToneAdr = 0; // IX+10,IX+11
        public byte legartFlg = 0; // IX+12
        public byte volume = 127; // IX+13
        public byte nestCount = 0; // IX+14
        public byte q = 8; // q IX+15
        public byte Q = 0; // Q IX+16
        public byte gatetime = 1; // IX+17
        public byte isCountNext = 0; // IX+18
        public byte lfoFlags = 0; // IX+19
        public byte transpose = 0; // IX+20
        public byte kf = 0; // Key Fraction (IX+21)
        public byte noteNumber = 0; // Note Number (IX+22)
        public int psgTone = 0; // PSG intervals IX+21, IX+22
        public byte portaFlg = 9; // Portamento Flag IX+23
        public int portaTone = 0; // Portamento reaching pitch IX+24, IX+25
        public byte softPMDelay = 0; // Soft PM delay setting value IX+26
        public byte softPMPitch = 0; // Soft PM pitch setting value IX+27
        public byte softPMStep = 0; // Soft PM step setting value IX+28
        public byte softPMDelayCount = 0; // Soft PM Delay Counter IX+29
        public byte softPMStepCount = 0; // Soft PM step counter IX+30
        public byte softPMProcCount = 0; // Soft PM process counter IX+31
        public byte softPMType = 0; // Soft PM step counter IX+32
        public byte softAMFlagAndDelay = 0; // Soft AM flag and delay setting value (0=off) (IX+33)
        public byte psgHardEnvelopeType = 16; // PSG hard envelope shape (16=soft envelope) (IX+33)
        public byte softAMDepth = 0; // Soft AM depth setting value IX+34
        public byte softAMStep = 0; // Soft AM step setting value IX+35
        public byte softAMSelOP = 0; // Soft AM selection OP IX+36
        public byte softAMDelayCount = 0; // Soft AM Delay Counter IX+37
        public byte softAMStepCount = 0; // Soft AM step counter IX+38
        public byte softAMProcCount = 0; // Soft AM process counter IX+39
        public byte op1Tls = (byte) 255; // IX+40
        public byte op2Tls = (byte) 255; // IX+41
        public byte op3Tls = (byte) 255; // IX+42
        public byte op4Tls = (byte) 255; // IX+43
        public byte portaStartFlg = 0; // Portamento operation flag IX+44
        public byte legartDelayFlg = 0; // Legato Delay Flag IX+45
        public byte keyOffFlg = 0; // Key-off flag IX+46
        public byte trackStopFlg = 0; // Truck Stop Flag IX+47
        public byte glideFlg = 0; // Glide Flag IX+48
        public int glide = 0; // Glide value IX+49, IX+50
        public byte workForPlayer = 0; // Player work (note number before processing) IX+51
        public byte psgRr = 0; // IX+52
        public byte psgRrLevel = 15; // IX+53
        public byte psgRrCounter = 0; // IX+54
        public byte psgRrVolOffset = 0; // IX+55

        public final RepBuf[] repBuf = new RepBuf[] {
                new RepBuf(), new RepBuf(), new RepBuf(), new RepBuf(), new RepBuf(), new RepBuf()
        };

        private void wreset() {
            this.panAlgFb = (byte) 0xc0;
            this.volume = 127;
            this.q = 8;
            this.gatetime = 1;
            this.op1Tls = (byte) 255;
            this.op2Tls = (byte) 255;
            this.op3Tls = (byte) 255;
            this.op4Tls = (byte) 255;
            this.psgRrLevel = 15;
            this.loopCounter = 0;
            this.counter = 0;
            this.detune = 0;
            this.macroReturnAdr = 0;
            this.legartFlg = 0;
            this.nestCount = 0;
            this.Q = 0;
            this.isCountNext = 0;
            this.transpose = 0;
            this.portaFlg = 0;
            this.portaTone = 0;
            this.softPMDelay = 0;
            this.softPMPitch = 0;
            this.softPMStep = 0;
            this.softPMDelayCount = 0;
            this.softPMStepCount = 0;
            this.softPMProcCount = 0;
            this.softPMType = 0;
            this.softAMDepth = 0;
            this.softAMStep = 0;
            this.softAMSelOP = 0;
            this.softAMDelayCount = 0;
            this.softAMStepCount = 0;
            this.softAMProcCount = 0;
            this.portaStartFlg = 0;
            this.legartDelayFlg = 0;
            this.keyOffFlg = 0;
            this.trackStopFlg = 0;
            this.glideFlg = 0;
            this.glide = 0;
            this.workForPlayer = 0;
            this.psgRr = 0;
            this.psgRrCounter = 0;
            this.psgRrVolOffset = 0;
        }

        private void comchk(byte e) {
            while (true) {
                byte cmdno = ram[this.ptrData];
                this.ptrData++;

                if ((cmdno & 0x80) != 0) {
                    if (work.opmKeyONEnable) {
                        // KEYON
                        if (work.KEYON_LightMode) {
                            throw new UnsupportedOperationException();
                        } else {
                            keyon(e, cmdno);
                        }
                    } else {
                        this.ptrData--;
                    }
                    return;
                } else if (cmdno == 127) {
                    // Track End
                    trkend();
                } else if ((cmdno & 0xff) < 38) {
                    if (comck0(e, cmdno) == 1) return;
                } else if (cmdno == 125) {
                    // Track Pause
                    this.trackStopFlg = 125;
                    return;
                } else {
                    this.trackStopFlg = (byte) 255;
                    return;
                }
            }
        }

        private int comck0(byte e, byte cmdno) {
            int r = 0;
            switch (cmdno) {
            case 0:
                if (work.opmRestEnable) r = REST(e);
                else {
                    r = 1;
                    this.ptrData--;
                }
                break;
            case 1:
                r = ZCOM(e);
                break;
            case 2:
                if (work.opmT02_LightMode) r = VSETL(e);
                else r = VSET(e);
                break;
            case 3:
                r = VOLWS(e);
                break;
            case 4:
                r = CSMCOM(e);
                break;
            case 5:
                r = STYPE(e);
                break;
            case 6:
                r = SOP(e);
                break;
            case 7:
                r = HLFO(e);
                break;
            case 8:
                r = REST(e);
                break;
            case 9:
                r = REST(e);
                break;
            case 10:
                r = REST(e);
                break;
            case 11:
                r = FFSET(e);
                break;
            case 12:
                r = YCOMP(e);
                break;
            case 13:
                r = REST(e);
                break;
            case 14:
                if (work.opmT14_MVMode) r = VSETMV(e);
                else r = VSET(e);
                break;
            case 15:
                r = DETUNE(e);
                break;
            case 16:
                r = MMACRO(e);
                break;
            case 17:
                r = LEGON(e);
                break;
            case 18:
                r = LEGOFF(e);
                break;
            case 19:
                if (work.opmT19Enable) r = VOLUME(e);
                else {
                    r = 1;
                    this.ptrData--;
                }
                break;
            case 20:
                r = PANPOT(e);
                break;
            case 21:
                r = RSTART(e);
                break;
            case 22:
                r = REND(e);
                break;
            case 23:
                r = RQUIT(e);
                break;
            case 24:
                r = YCOM(e);
                break;
            case 25:
                r = QUANT1(e);
                break;
            case 26:
                r = QUANT2(e);
                break;
            case 27:
                r = LSYNC(e);
                break;
            case 28:
                r = KSHIFT(e);
                break;
            case 29:
                r = PORTA(e);
                break;
            case 30:
                r = TEMPO(e);
                break;
            case 31:
                r = SPM(e);
                break;
            case 32:
                r = SAM(e);
                break;
            case 33:
                r = KKOFF(e);
                break;
            case 34:
                r = REPLAY(e);
                break;
            case 35:
                r = GLIDE(e);
                break;
            case 36:
                r = VOLOP(e);
                break;
            case 37:
                r = FADEC(e);
                break;
            }

            return r;
        }

        private int REST(byte e) {
            byte a = ram[this.ptrData];
            if (a == (byte) 255) {
                this.isCountNext = (byte) 255;
            } else {
                this.isCountNext = 0;
            }
            a = (byte) ((a & 0xff) - 1);
            this.counter = a;
            this.ptrData++;

            return 1;
        }

        private int ZCOM(byte e) {
            byte a = ram[this.ptrData];
            work.zCount = a;
            this.ptrData++;

            do {
                byte d = ram[this.ptrData];
                this.ptrData++;
                a = ram[this.ptrData];
                this.ptrData++;
                wopm(d, a);
                a = work.zCount;
                a = (byte) ((a & 0xff) - 1);
                work.zCount = a;
            } while (a != 0);

            return 0;
        }

        private int VSETL(byte e) {
            byte a = (byte) (0x20 + e);
            byte d = a;
            a = ram[this.ptrData];
            this.panAlgFb = a;
            wopm(d, a);
            this.ptrData++;
            int bc = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
            this.ptrData += 2;
            int hl = work.bgmAdr + bc;

            VSETJ(e, hl);

            return 0;
        }

        private void VSETJ(byte e, int hl) {
            byte d = (byte) (0x40 + (e & 0xff));
            byte a;

            for (int i = 0; i < 24; i++) {
                a = ram[hl];
                hl++;
                wopm(d, a);
                d += (byte) ((d & 0xff) < 248 ? 8 : 0);
            }
            this.op1Tl = ram[hl];
            hl++;
            this.op3Tl = ram[hl];
            hl++;
            this.op2Tl = ram[hl];
            hl++;
            this.op4Tl = ram[hl];
        }

        private int VSET(byte e) {
            int bc = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
            this.ptrData += 2;
            int hl = work.bgmAdr + bc;

            byte a = ram[hl];
            byte c = a;
            a &= 0xc0;
            if (a == 0) {
                a = (byte) (this.panAlgFb & 0xc0);
                a |= c;
                c = a;
            }
            a = 0x20;
            a += e;
            byte d = a;
            a = c;
            this.panAlgFb = a;
            wopm(d, a);
            hl++;
            d = ram[hl];
            a = this.lfoFlags;
            a &= 0x83;
            a |= d;
            this.lfoFlags = a;
            hl++;
            VSETJ(e, hl);
            return 0;
        }

        private int VSETMV(byte e) {
            int bc = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
            this.ptrData += 2;
            int hl = work.bgmAdr + bc;

            byte a = ram[hl];
            byte c = a;
            a &= 0xc0;
            if (a == 0) {
                a = (byte) (this.panAlgFb & 0xc0);
                a &= 0xc0;
                a |= c;
                c = a;
            }
            a = 0x20;
            a += e;
            byte d = a;
            a = c;
            this.panAlgFb = a;
            wopm(d, a);
            hl++;
            d = ram[hl];
            a = this.lfoFlags;
            a &= 0x83;
            a |= d;
            this.lfoFlags = a;
            hl++;

            a = 0x40;
            a += e;
            d = a;
            // DT1/ML
            for (int i = 0; i < 4; i++) {
                a = ram[hl];
                hl++;
                wopm(d, a);
                d += 8;
            }
            // TL
            for (int i = 0; i < 4; i++) {
                a = work.mVol;
                c = ram[hl];
                a += c;
                if ((a & 0xff) > 127) a = 127;
                hl++;
                wopm(d, a);
                d += 8;
            }
            // etc
            for (int i = 0; i < 16; i++) {
                a = ram[hl];
                hl++;
                wopm(d, a);
                d += 8;
            }
            this.op1Tl = ram[hl];
            hl++;
            this.op3Tl = ram[hl];
            hl++;
            this.op2Tl = ram[hl];
            hl++;
            this.op4Tl = ram[hl];

            return 0;
        }

        private int VOLWS(byte e) {
            this.volume = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int CSMCOM(byte e) {
            byte a = 0x40;
            a = (byte) ((a & 0xff) + (e & 0xff));
            byte d = a;
            byte c = 0x8;

            for (int i = 0; i < 4; i++) {
                a = ram[this.ptrData];
                this.ptrData++;
                if ((a & 0x20) == 0) {
                    wopm(d, (byte) (a & 0xf));
                    a &= 0xc0;
                    d |= 0x80;
                    wopm(d, a);
                    d &= 0x7f;
                }
                a = ram[this.ptrData];
                this.ptrData++;
                if ((a & 0xff) < 127) {
                    d |= 0x20;
                    wopm(d, a);
                    d &= 0xdf;
                }
                a = c;
                a += d;
                d = a;
            }

            return 0;
        }

        private int SPM(byte e) {
            this.softPMDelay = ram[this.ptrData];
            this.ptrData++;
            this.softPMPitch = ram[this.ptrData];
            this.ptrData++;
            this.softPMStep = ram[this.ptrData];
            this.ptrData++;

            STYPE(e);

            return 0;
        }

        private int STYPE(byte e) {
            this.softPMType = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int SAM(byte e) {
            this.softAMFlagAndDelay = ram[this.ptrData];
            this.ptrData++;
            this.softAMDepth = ram[this.ptrData];
            this.ptrData++;
            this.softAMStep = ram[this.ptrData];
            this.ptrData++;
            if (this.softAMStep == 0) {
                tlrst(e);
            }
            SOP(e);

            return 0;
        }

        private int SOP(byte e) {
            this.softAMSelOP = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int HLFO(byte e) {
            byte d = 0x18;
            byte a = ram[this.ptrData];
            wopm(d, a);
            this.ptrData++;

            d = 0x19;

            if (work.opmIo != 0) {
                a = ram[this.ptrData];
                work.pmd2 = a;
                wopm(d, a);
                this.ptrData++;

                d = 0x19;
                a = ram[this.ptrData];
                work.amd2 = a;
            } else {
                a = ram[this.ptrData];
                work.pmd1 = a;
                wopm(d, a);
                this.ptrData++;

                d = 0x19;
                a = ram[this.ptrData];
                work.amd1 = a;
            }
            wopm(d, a);
            this.ptrData++;

            d = 0x1b;
            a = ram[this.ptrData];
            wopm(d, a);

            this.ptrData++; // CCRET

            return 0;
        }

        private int FFSET(byte e) {
            work.ffFlg = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int YCOM(byte e) {
            byte d = ram[this.ptrData];
            this.ptrData++;
            byte a = ram[this.ptrData];
            wopm(d, a);
            this.ptrData++; // CCRET
            return 0;
        }

        private int PYCOM(byte e) {
            byte d = ram[this.ptrData];
            this.ptrData++;
            byte a = ram[this.ptrData];
            wpsg(d, a);
            this.ptrData++; // CCRET
            return 0;
        }

        private int YCOMP(byte e) {
            byte a = ram[this.ptrData];
            this.ptrData++;
            if (a == 0) {
                PYCOM(e);
                return 0;
            } else {
                byte b = 7;
                byte c = a;
                byte d = ram[this.ptrData];
                this.ptrData++;
                a = ram[this.ptrData];
                WOPMBC(a, b, c, d);
                this.ptrData++; // CCRET
            }

            return 0;
        }

        private void WOPMBC(byte a, byte b, byte c, byte d) {
            if (c == 1) {
                // OPM1
                work.opm1VReg[d] = a;
                if (work.opmFlg != 0) {
                    // Weight
                }
                ym2151WriteV.accept(0, d & 0xff, a & 0xff);
            } else {
                // OPM2
                work.opm2VReg[d] = a;
                if (work.opmFlg != 0) {
                    // Weight
                }
                ym2151WriteV.accept(1, d & 0xff, a & 0xff);
            }
        }

        private int DETUNE(byte e) {
            this.detune = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int MMACRO(byte e) {
            int bc = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
            this.ptrData += 2;
            this.macroReturnAdr = this.ptrData;
            int hl = work.bgmAdr;
            hl += bc;
            this.ptrData = hl;

            // CCRET1 Not required

            return 0;
        }

        private int LEGON(byte e) {
            this.legartFlg = 1;

            // CCRET1 Not required

            return 0;
        }

        private int LEGOFF(byte e) {
            this.legartFlg = 0;

            // CCRET1 Not required

            return 0;
        }

        private int VOLUME(byte e) {
            this.volume = ram[this.ptrData];
            this.ptrData++;

            volsub(e);

            return 0;
        }

        private int PANPOT(byte e) {
            byte a = 0x20;
            a += e;
            byte d = a;
            a = this.panAlgFb;
            a &= 0x3f;
            byte c = a;
            a = ram[this.ptrData];
            a <<= 6;
            a |= c;
            this.panAlgFb = a;

            wopm(d, a);

            this.ptrData++; // CCRET

            return 0;
        }

        private int RSTART(byte e) {
            this.nestCount++;
            byte d = ram[this.ptrData];
            this.ptrData++;

            // RASRS is probably unnecessary

            this.repBuf[this.nestCount - 1].count = d;
            this.repBuf[this.nestCount - 1].startAdr = this.ptrData;

            // CCRET1 is not required
            return 0;
        }

        private int REND(byte e) {
            // RASRS is probably unnecessary

            this.repBuf[this.nestCount - 1].count--;
            if (this.repBuf[this.nestCount - 1].count == 0) {
                this.nestCount--;
                // CCRET1 is not required
                return 0;
            }

            this.repBuf[this.nestCount - 1].endAdr = this.ptrData;
            this.ptrData = this.repBuf[this.nestCount - 1].startAdr;

            // CCRET1 is not required
            return 0;
        }

        private int RQUIT(byte e) {
            // RASRS is probably unnecessary

            if (this.repBuf[this.nestCount - 1].count != 1) {
                // CCRET1 is not required
                return 0;
            }

            this.repBuf[this.nestCount - 1].count = 0;
            this.ptrData = this.repBuf[this.nestCount - 1].endAdr;
            this.nestCount--;

            // CCRET1 is not required
            return 0;
        }

        private int QUANT1(byte e) {
            this.q = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int QUANT2(byte e) {
            this.Q = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int LSYNC(byte e) {
            byte a = ram[this.ptrData];
            if (a == 0) {
                lreset();
            } else {
                byte d = a;
                a = this.lfoFlags;
                a &= 0xfc;
                a |= d;
                this.lfoFlags = a;
            }

            this.ptrData++; // CCRET

            return 0;
        }

        private int TEMPO(byte e) {

            work.ctc0 = 0x25;
            work.ctc0TimeConstant = ram[this.ptrData];
            work.ctc3 = (byte) 0xc5;
            work.ctc3TimeConstant = ram[this.ptrData + 1];

            work.keyOnFOpmMask = 0x78;
            work.PENVF_VOL0 = false;
            work.ffFlg = 0;

            this.ptrData += 2; // CCRET

            return 0;
        }

        private int KSHIFT(byte e) {
            this.transpose = ram[this.ptrData];
            this.ptrData++; // CCRET

            return 0;
        }

        private int PORTA(byte e) {
            byte a = ram[this.ptrData];
            this.portaFlg = a;
            this.portaStartFlg = a;
            this.glideFlg = 0;

            this.ptrData++; // CCRET

            return 0;
        }

        private int PORTAP(byte e) {
            byte a = ram[this.ptrData];
            this.op1Tls = a;
            a = ram[this.ptrData + 1];
            this.portaFlg = a;
            this.portaStartFlg = a;
            this.op2Tls = 0;
            this.glideFlg = 0;

            this.ptrData++;
            this.ptrData++; // CCRET

            return 0;
        }

        private int KKOFF(byte e) {
            byte d = 8;
            byte a = e;
            wopm(d, a);

            this.legartDelayFlg = 0;
            this.workForPlayer = 0;

            return 0;
        }

        private int REPLAY(byte e) {
            byte b = ram[this.ptrData];
            this.ptrData++;

            Ch c;
            if ((b & 0xff) < 8) c = work.opm1Chs[b];
            else if ((b & 0xff) < 16) c = work.opm2Chs[b - 8];
            else c = work.psgChs[b - 16];

            byte a = c.trackStopFlg;
            if (a != (byte) 255) {
                c.trackStopFlg = 0;
            }

            // CCRET1 is not required
            return 0;
        }

        private int GLIDE(byte e) {
            byte a = ram[this.ptrData];
            this.glideFlg = a;
            this.portaFlg = a;
            this.ptrData++;
            this.glide = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
            this.ptrData += 2; // CCRET
            return 0;
        }

        private int VOLOP(byte e) {
            this.volume = ram[this.ptrData];
            this.ptrData++;
            byte a = ram[this.ptrData];
            this.ptrData++;

            volops(a, e);

            return 0;
        }

        private int FADEC(byte e) {
            byte a = ram[this.ptrData];

            work.MFADE1(a);

            this.ptrData++;
            return 0;
        }

        private void volops(byte a, byte e) {
            if ((a & 1) != 0) {
                volwr((byte) 0x60, (byte) 0x8, e);
            }
            if ((a & 2) != 0) {
                volwr((byte) 0x70, (byte) 0x9, e);
            }
            if ((a & 4) != 0) {
                volwr((byte) 0x68, (byte) 0xa, e);
            }
            if ((a & 8) != 0) {
                volwr((byte) 0x78, (byte) 0xb, e);
            }
        }

        private void trkend() {
            int bc = this.macroReturnAdr;
            if (bc != 0) {
                this.ptrData = bc;
                this.macroReturnAdr = 0;
            } else {
                bc = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
                this.ptrData = work.bgmAdr + bc;
                this.loopCounter++;
            }
            // CCRET1 is not required
        }

        private void mvset(byte e) {
            mvwr(e, 3); // OP4

            byte a = (byte) (this.panAlgFb & 0x7);
            if ((a & 0xff) < 4) return;

            mvwr(e, 2); // OP2

            if (a == 4) return;

            mvwr(e, 1); // OP3

            if (a != 7) return;

            mvwr(e, 0); // OP1
        }

        private void mvwr(byte e, int op) {
            byte d = (byte) (0x60 + (e & 0xff) + op * 8);
            byte a = (byte) (this.volume ^ 127);

            switch (op) {
            case 0:
                a += (byte) ((work.mVol & 0xff) + (this.op1Tl & 0xff));
                a = (byte) (((a & 0xff) > 127) ? 127 : a);
                this.op1Tls = a;
                break;
            case 1:
                a += (byte) ((work.mVol & 0xff) + (this.op3Tl & 0xff));
                a = (byte) (((a & 0xff) > 127) ? 127 : a);
                this.op3Tls = a;
                break;
            case 2:
                a += (byte) ((work.mVol & 0xff) + (this.op2Tl & 0xff));
                a = (byte) (((a & 0xff) > 127) ? 127 : a);
                this.op2Tls = a;
                break;
            case 3:
                a += (byte) (work.mVol + this.op4Tl);
                a = (byte) (((a & 0xff) > 127) ? 127 : a);
                this.op4Tls = a;
                break;
            }

            wopm(d, a);
        }

        private void keyon(byte e, byte cmdno) {
            byte a = (byte) (cmdno & 0x7f);
            byte ks = this.transpose;
            a += ks;
            this.workForPlayer = a;

            a = ram[this.ptrData];

            if (a == (byte) 255) this.isCountNext = a;
            else this.isCountNext = 0;

            a--;
            this.counter = a;

            if (a == 0) {
                if (this.legartFlg == 0) {
                    this.keyOffFlg = 1;
                }
            }

            this.ptrData++;

            int bc = (a & 0xff) * 0x100;
            bc = bc / 8;

            a = this.q;
            if (a == 8) {
                a = 0;
            } else {
                a = (byte) (((8 - (a & 0xff)) * bc) / 0x100);
            }
            a += this.Q;
            a++;
            this.gatetime = a;

            byte b = this.workForPlayer;
            byte d = (byte) (0x30 + (e & 0xff));
            int ia = this.detune & 0xff;
            if (ia >= 128) {
                while (ia < 256) {
                    b--;
                    ia += 64;
                }
            } else {
                while (ia >= 64) {
                    b++;
                    ia -= 64;
                }
            }
            ia = ia * 4;

            byte c = (byte) ia;
            a = this.glideFlg;
boolean KEYONE = false;
            if (a != 0) {
                this.portaFlg = a;
                this.portaTone = (b & 0xff) * 0x100 + (c & 0xff);
                this.portaStartFlg = a;

                int hl = (b & 0xff) * 0x100 + (c & 0xff);
                bc = this.glide;
                hl += bc;
                b = (byte) (hl / 0x100);
                c = (byte) (hl & 0xff);
            } else {
                a = this.portaFlg;
                if (a != 0) {
                    this.portaStartFlg = a;
                    this.portaTone = (b & 0xff) * 0x100 + (c & 0xff);
                    //break
KEYONE = true;
                }
            }
if (!KEYONE) {
            this.noteNumber = b;
            this.kf = c;
            a = c;
            wopm(d, a);
            d -= 8;
            a = KTABLE[this.noteNumber & 0xff];
            wopm(d, a);
}
// KEYONE:
            a = this.legartDelayFlg;
            c = a;
            this.legartDelayFlg = this.legartFlg;
            if (a == 0) {
                a = this.lfoFlags;
                if ((a & 2) != 0) {
                    lreset();
                }
                d = 8;
                a = this.lfoFlags;
                if ((a & 0x80) != 0) {
                    a = 0;
                } else {
                    // a &= 0x78;
                    a &= work.keyOnFOpmMask;
                }
                a |= e;
                wopm(d, a);
            }

            a = this.softPMType;
            if ((a & 0x80) != 0) {
                this.softPMProcCount = (byte) ((a & 0x7f) * 2);
                if (c != 0) {
                    b = 1;
                } else {
                    b = this.softPMDelay;
                }
                this.softPMDelayCount = b;
                this.softPMStepCount = this.softPMStep;
            }
            if ((this.softAMSelOP & 0x80) != 0) {
                this.softAMStepCount = (byte) ((this.softAMStep & 0xff) + 1);
                a = c;
                if (c != 0) {
                    a = 1;
                } else {
                    a = this.softAMFlagAndDelay;
                }
                this.softAMDelayCount = a;
                this.softAMProcCount = 0;
                tlrst(e);
            }
        }

        private void tlrst(byte e) {
            byte a = (byte) (this.panAlgFb & 7);
            byte l = a;
            byte c;

            if (l != 7) {
                c = this.op1Tl;
                a = this.op1Tls;
                if (a != c) {
                    this.op1Tls = c;
                    a = 0x60;
                    a += e;
                    byte d = a;
                    a = c;
                    wopm(d, a);
                }
            }

            if (l < 4) {
                c = this.op2Tl;
                a = this.op2Tls;
                if (a != c) {
                    this.op2Tls = c;
                    a = 0x70;
                    a += e;
                    byte d = a;
                    a = c;
                    wopm(d, a);
                }
            }

            if (l < 5) {
                c = this.op3Tl;
                a = this.op3Tls;
                if (a != c) {
                    this.op3Tls = c;
                    a = 0x68;
                    a += e;
                    byte d = a;
                    a = c;
                    wopm(d, a);
                }
            }

            c = this.op4Tl;
            this.op4Tls = c;

            volsub(e);
        }

        private void volsub(byte e) {
            byte a = 0x78;
            byte c = 11;
            volwr(a, c, e);

            a = (byte) (this.panAlgFb & 7);
            byte l = a;
            if (a >= 4) {
                a = 0x70;
                c = 9;
                volwr(a, c, e);

                if (l != 4) {
                    a = 0x68;
                    c = 10;
                    volwr(a, c, e);

                    if (l == 7) {
                        a = 0x60;
                        c = 8;
                        volwr(a, c, e);
                    }
                }
            }
        }

        private void volwr(byte a, byte c, byte e) {
            a += e;
            byte d = a;

            byte op = c;

            a = (byte) (this.volume ^ 0x7f);
            c = a;
            a = work.mVol;
            byte b = a;

            switch (op) {
            case 8:
                a = this.op1Tl;
                break;
            case 9:
                a = this.op2Tl;
                break;
            case 10:
                a = this.op3Tl;
                break;
            case 11:
                a = this.op4Tl;
                break;
            }
            a += (byte) ((c & 0xff) + (b & 0xff));
            if ((a & 0xff) > 127) a = 127;
            switch (op) {
            case 8:
                this.op1Tls = a;
                break;
            case 9:
                this.op2Tls = a;
                break;
            case 10:
                this.op3Tls = a;
                break;
            case 11:
                this.op4Tls = a;
                break;
            }
            wopm(d, a);
        }

        private void PSGRR() {

            if (this.psgRrCounter != 0) {
                this.psgRrCounter--;
                return;
            }

            this.psgRrCounter = this.psgRr;
            this.psgRrVolOffset++;
            if ((this.psgRrVolOffset & 0xff) < 16) return;

            this.psgToneAdr = 0;
            this.keyOffFlg = 0;
            this.workForPlayer = 0;
        }

        private void RRST(byte e) {
            byte a = this.psgHardEnvelopeType;
            if ((a & 0xff) - 16 < 0) {
                // RRST1:
                this.psgToneAdr = 0;
                this.keyOffFlg = 0;
                this.workForPlayer = 0;
                wpsg((byte) (8 + (e & 0xff)), (byte) 0);
                return;
            }

            this.psgRrVolOffset = this.psgRrLevel;
            this.keyOffFlg = 2;
        }

        private void PCOM(byte e) {
            while (true) {
                byte cmdno = ram[this.ptrData];
                this.ptrData++;

                if ((cmdno & 0x80) != 0) {
                    if (work.psgKeyONEnable) {
                        if (work.PKEYON_LightMode) {
                            throw new UnsupportedOperationException();
                        } else {
                            PKEYON(e, cmdno);
                        }
                    } else {
                        this.ptrData--;
                    }
                    return;
                } else if (cmdno == 127) {
                    // Track End
                    this.trkend();
                    // return;
                } else if ((cmdno & 0xff) < 38) {
                    if (PCOM0(e, cmdno) == 1) return;
                } else {
                    this.trackStopFlg = (byte) 255;
                    return;
                }
            }
        }

        private int PCOM0(byte e, byte cmdno) {
            int r = 0;
            switch (cmdno) {
            case 0:
                if (work.psgRestEnable) r = REST(e);
                else {
                    r = 1;
                    this.ptrData--;
                }
                break;
            case 1:
                r = PZCOM(e);
                break;
            case 2:
                r = REST(e);
                break;
            case 3:
                r = REST(e);
                break;
            case 4:
                r = REST(e);
                break;
            case 5:
                r = STYPE(e);
                break;
            case 6:
                r = REST(e);
                break;
            case 7:
                r = REST(e);
                break;
            case 8:
                r = REST(e);
                break;
            case 9:
                r = REST(e);
                break;
            case 10:
                r = REST(e);
                break;
            case 11:
                r = FFSET(e);
                break;
            case 12:
                r = YCOMP(e);
                break;
            case 13:
                r = RRSET(e);
                break;
            case 14:
                r = PVSET(e);
                break;
            case 15:
                r = DETUNE(e);
                break;
            case 16:
                r = MMACRO(e);
                break;
            case 17:
                r = LEGON(e);
                break;
            case 18:
                r = LEGOFF(e);
                break;
            case 19:
                r = PVOL(e);
                break;
            case 20:
                r = PMODE(e);
                break;
            case 21:
                r = RSTART(e);
                break;
            case 22:
                r = REND(e);
                break;
            case 23:
                r = RQUIT(e);
                break;
            case 24:
                r = PYCOM(e);
                break;
            case 25:
                r = QUANT1(e);
                break;
            case 26:
                r = QUANT2(e);
                break;
            case 27:
                r = NFREQ(e);
                break;
            case 28:
                r = KSHIFT(e);
                break;
            case 29:
                r = PORTAP(e);
                break;
            case 30:
                r = TEMPO(e);
                break;
            case 31:
                r = SPM(e);
                break;
            case 32:
                r = SHAPE(e);
                break;
            case 33:
                r = PKKOFF(e);
                break;
            case 34:
                r = REPLAY(e);
                break;
            case 35:
                r = GLIDE(e);
                break;
            case 36:
                r = PERIOD(e);
                break;
            case 37:
                r = FADEC(e);
                break;
            }

            return r;
        }

        private void PENV(byte e) {
            int hl = this.psgToneAdr;
            byte a;
            byte c;
            while (true) {
                a = ram[hl];
                hl++;
                int bc;
                if (a == (byte) 255) {
                    bc = ram[hl] & 0xff;
                    hl -= bc;
                    a = ram[hl];
                }
                // PENV1:
                if ((a & 0xff) - 16 < 0) {
                    break;
                }
                if ((a & 0xff) - 48 < 0) {
                    wpsg((byte) 6, (byte) ((a & 0xff) - 16));
                    continue;
                }
                a -= 48;
                a *= 2;
                bc = a & 0xff;

                a = work.pFlg;
                c = PMTBL[(e & 0xff) * 8 + bc];
                a &= c;
                c = PMTBL[(e & 0xff) * 8 + bc + 1];
                a |= c;
                work.pFlg = a;
                wpsg((byte) 7, a);
            }
//PENV4:
            this.psgToneAdr = hl;
            a ^= 15;
            c = a;
            byte d = (byte) (8 + (e & 0xff));
            byte b = work.mVol;
//PENVF2:
            if ((this.lfoFlags & 0x80) == 0) {
                if (work.PENVF_VOL0) a = 0;
                else a = this.volume;

                if ((a & 0xff) - (b & 0xff) < 0) {
                    a = 0;
                } else {
                    a -= b;
                    a = (byte) (a >>> 3);
                    b = this.psgRrVolOffset;
                    if ((a & 0xff) - (b & 0xff) < 0) {
                        a = 0;
                    } else {
                        a -= b;
                        if ((a & 0xff) - (c & 0xff) < 0) {
                            a = 0;
                        } else {
                            a -= c;
                        }
                    }
                }
            } else {
                a = 0;
            }
//PENV5:
            wpsg(d, a);
        }

        private void PKEYON(byte e, byte cmdno) {
            byte a = (byte) (cmdno & 0x7f);
            byte ks = this.transpose;
            a += ks;
            this.workForPlayer = a;

            a = ram[this.ptrData];

            if (a == (byte) 255) this.isCountNext = a;
            else this.isCountNext = 0;

            a--;
            this.counter = a;

            if (a == 0) {
                if (this.legartFlg == 0) {
                    this.keyOffFlg = 1;
                }
            } else {
                this.keyOffFlg = 0;
            }

            this.ptrData++;

            int bc = (a & 0xff) * 0x100;
            bc = bc / 8;

            a = this.q;
            if (a == 8) {
                a = 0;
            } else {
                a = (byte) (((8 - (a & 0xff)) * bc) / 0x100);
            }
            a += this.Q;
            a++;
            this.gatetime = a;

            // PKONR
            bc = this.workForPlayer & 0xff; // 2x Not required
            byte d = (byte) (e << 1);

            byte b = 0;
            byte c;
            if (bc < PTABLE.length) { // In versions earlier than 180303, the array may be exceeded. Therefore, a code to check the index has been added.
                c = (byte) PTABLE[bc];
            } else {
                c = 0;
            }
            a = this.detune;
            a = (byte) (~a + 1); // NEG
            if ((a & 0xff) - 129 < 0) {
                // PKON6
                if ((a & 0xff) + (c & 0xff) <= 255) {
                    a += c;
                } else {
                    a += c;
                    b++;
                }
            } else {
                if ((a & 0xff) + (c & 0xff) > 255) {
                    a += c;
                } else {
                    a += c;
                    b--;
                }
            }
            // PKON5
            c = a;
            if (bc < PTABLE.length) { // In versions earlier than 180303, the array may be exceeded. Therefore, a code to check the index has been added.
                a = (byte) (PTABLE[bc] >>> 8);
            } else {
                a = 0;
            }
            b += a;

            a = this.glideFlg;
boolean PKON9 = false;
boolean PKONE = false;
            if (a != 0) {
                this.portaFlg = a;
                this.portaTone = (b & 0xff) * 0x100 + (c & 0xff);
                this.portaStartFlg = a;

                int hl = (b & 0xff) * 0x100 + (c & 0xff);
                bc = this.glide;
                hl += bc;
                b = (byte) (hl / 0x100);
                c = (byte) (hl & 0xff);
//                break PKON8;
            } else {
//PKON11:
                a = this.portaFlg;
                if (a != 0) {
                    this.portaStartFlg = a;
                    this.portaTone = (b & 0xff) * 0x100 + (c & 0xff);
                    //break PKON9;
PKON9 = true;
                }
            }
//PKON8:
if (!PKON9) {
            this.psgTone = (b & 0xff) * 0x100 + (c & 0xff);
            wpsg(d, c);
            d++;
            wpsg(d, b);
}
//PKON9:
            a = this.legartDelayFlg;
            c = a;
            this.legartDelayFlg = this.legartFlg;
            if (a != 0) {
                if (this.psgToneAdr != 0) {
                    //break PKONE;
PKONE = true;
                }
            }
if (!PKONE) {
            // PKON10
            a = this.psgHardEnvelopeType;
            if ((a & 0xff) - 16 < 0) {
                wpsg((byte) 13, a);
                d = (byte) (8 + (e & 0xff));
                a = this.lfoFlags;
                if ((a & 0x80) == 0) {
                    a = 16;
                } else {
                    a = 0;
                }
                wpsg(d, a);
//                break PKONE;
            } else {
//PKSENV:
                a = this.lfoFlags;
                if ((a & 0x80) != 0) {
                    d = 8;
                    d += e;
                    a = 0;
                    wpsg(d, a);
                }
                // PKSENV02
                this.psgRrVolOffset = a;
                this.psgToneAdr = this.psgToneStartAdr;
            }
}
//PKONE:
            a = this.softPMType;
            if ((a & 0x80) != 0) {
                this.softPMProcCount = (byte) (((a & 0x7f) * 2) ^ 2);
                this.softPMStepCount = this.softPMStep;

                if (c != 0) {
                    b = 1;
                } else {
                    b = this.softPMDelay;
                }
                this.softPMDelayCount = b;
            }
        }

        private int PZCOM(byte e) {
            byte a = ram[this.ptrData];
            work.zCount = a;
            this.ptrData++;

            do {
                byte d = ram[this.ptrData];
                this.ptrData++;
                a = ram[this.ptrData];
                this.ptrData++;
                wpsg(d, a);
                a = work.zCount;
                a--;
                work.zCount = a;
            } while (a != 0);

            return 0;
        }

        private int RRSET(byte e) {
            this.psgRr = ram[this.ptrData];
            this.ptrData++;
            this.psgRrLevel = ram[this.ptrData];

            this.ptrData++; // CCRET
            return 0;
        }

        private int PVSET(byte e) {
            int bc = (ram[this.ptrData] & 0xff) + (ram[this.ptrData + 1] & 0xff) * 0x100;
            this.ptrData += 2;
            this.psgToneStartAdr = work.bgmAdr + bc;
            this.psgHardEnvelopeType = 16;

            return 0;
        }

        private int PVOL(byte e) {
            this.volume = ram[this.ptrData];

            this.ptrData++; // CCRET
            return 0;
        }

        private int PMODE(byte e) {
            byte a = ram[this.ptrData];
            a += a;
            int bc = a & 0xff;
            int hl = e & 0xff;
            hl = hl * 8;
            hl += bc;
            a = work.pFlg;
            a &= PMTBL[hl];
            a |= PMTBL[hl + 1];
            work.pFlg = a;

            wpsg((byte) 7, a);

            this.ptrData++; // CCRET
            return 0;
        }

        private int NFREQ(byte e) {
            wpsg((byte) 6, ram[this.ptrData]);

            this.ptrData++; // CCRET
            return 0;
        }

        private int SHAPE(byte e) {
            this.psgHardEnvelopeType = ram[this.ptrData];
            this.psgToneAdr = 0;

            this.ptrData++; // CCRET
            return 0;
        }

        private int PKKOFF(byte e) {
            this.psgToneAdr = 0;
            this.legartDelayFlg = 0;
            this.workForPlayer = 0;
            wpsg((byte) (8 + (e & 0xff)), (byte) 0);
            return 0;
        }

        private int PERIOD(byte e) {
            wpsg((byte) 11, ram[this.ptrData]);
            wpsg((byte) 12, ram[this.ptrData + 1]);

            this.ptrData += 2; // CCRET
            return 0;
        }

        private void EPOR(byte e) {
            if (this.portaStartFlg == 0) return;

            byte l = this.kf;
            byte h = this.noteNumber;
            byte b = (byte) (this.portaTone >>> 8);
            byte c = (byte) (this.portaTone & 0xff);
            byte a = h;

            boolean neg = true;

            if (a == b) {
                a = l;
                if (a == c) {
                    this.portaStartFlg = 0;
                    return;
                } else if ((a & 0xff) < (c & 0xff)) neg = false;
            } else if ((a & 0xff) < (b & 0xff)) neg = false;

            //
            if (neg) {
                // Subtraction
                int p = (this.portaFlg & 0xff) * 4;
                int hl = ((h & 0xff) * 0x100) + (l & 0xff) - p;
                if (hl < 0) {
                    h = b;
                    l = c;
                } else {
                    a = (byte) ((hl >>> 8) - (b & 0xff));
                    if (((hl & 0xff) - (c & 0xff)) < 0) {
                        a--;
                    }
                    h = (byte) (hl >>> 8);
                    l = (byte) (hl & 0xff);
                }
            } else {
                // Addition
                int p = (this.portaFlg & 0xff) * 4;
                int hl = ((h & 0xff) * 0x100) + (l & 0xff) + p;
                h = (byte) (hl >>> 8);
                l = (byte) (hl & 0xff);

//                a = (byte) ((hl >>> 8) - (b & 0xff));
//                if (((hl & 0xff) - (c & 0xff)) < 0) {
//                    a--;
//                    h = (byte) (hl >>> 8);
//                    l = (byte) (hl & 0xff);
//                }
                if (hl > (b & 0xff) * 0x100 + (c & 0xff)) {
                    h = b;
                    l = c;
                }
            }

            this.kf = l;
            this.noteNumber = h;
            wopm((byte) (0x30 + (e & 0xff)), l);
            wopm((byte) (0x28 + (e & 0xff)), KTABLE[h]);
//logger.log(Level.TRACE, "opmout Reg%02x dat%02x".formatted(l, KTABLE[h]));
        }

        private void EPM(byte e) {
            if (this.portaStartFlg != 0) return;

//logger.log(Level.TRACE, "softPMProcCount[%d]".formatted(this.softPMProcCount));
//logger.log(Level.TRACE, "softPMStep[%d]".formatted(this.softPMStep));
//logger.log(Level.TRACE, "softPMStepCount[%d]".formatted(this.softPMStepCount));
//logger.log(Level.TRACE, "softPMPitch[%d]".formatted(this.softPMPitch));
//logger.log(Level.TRACE, "KF[%d]".formatted(this.KF));
//logger.log(Level.TRACE, "NoteNumber[%d]".formatted(this.NoteNumber));

            if (this.softPMDelayCount - 1 != 0) {
                this.softPMDelayCount--;
                return;
            }

            // EPM1:
            byte l = this.kf;
            byte h = this.noteNumber;

            byte a = this.softPMStepCount;
            a--;
            if (a != 0) {
                this.softPMStepCount = a;
                a = this.softPMProcCount;
                if ((a & 0xff) >= 8) return;
                if ((a & 0xff) >= 4) EPMH1(a, h, e);
                else if (a == 0 || a == 3) {
                    EPMM(a, h, l, e);
                } else {
                    EPMP(a, h, l, e);
                }
                return;
            }
//EPMS:
            a = this.softPMStep;
            this.softPMStepCount = a;

            a = this.softPMProcCount;
            if ((a & 0xff) - 8 < 0) {
//EPMS0:
                if ((a & 0xff) >= 4) {
//EPMH:
                    a++;
                    if ((a & 0xff) >= 8) a = 4;
                    this.softPMProcCount = a;
                    EPMH1(a, h, e);
                    return;
                }
                a++;
                a &= 3;
                this.softPMProcCount = a;
//EPMS1:
                if (a == 0 || (a & 0xff) >= 3) {
                    EPMM(a, h, l, e);
                } else {
                    EPMP(a, h, l, e);
                }
                return;
            }
//EPMQ:
            a++;
            if (a == 10 || (a & 0xff) > 12) {
                a--;
                a--;
            }
//EPMQ1:
            this.softPMProcCount = a;
            if (a == 8 || (a & 0xff) >= 11) {
                EPMP(a, h, l, e);
            } else {
                EPMM(a, h, l, e);
            }
        }

        private void EPMH1(byte a, byte h, byte e) {
            byte b = this.softPMPitch;
            byte c = 0;

            if (a == 4 || a == 7) {
                if ((h & 0xff) - (b & 0xff) >= 0) {
                    c = (byte) ((h & 0xff) - (b & 0xff));
                }
            } else {
                c = 97;
                if ((h & 0xff) + (b & 0xff) <= 97) {
                    c = (byte) ((h & 0xff) + (b & 0xff));
                }
            }

//EPM2H:
            this.noteNumber = c;
            wopm((byte) (0x28 + (e & 0xff)), KTABLE[c & 0xff]);
        }

        private void EPMM(byte a, byte h, byte l, byte e) {
            int p = (this.softPMPitch & 0xff) * 4;
            int hl = (h & 0xff) * 0x100 + (l & 0xff);
            hl = Math.max(hl - p, 0);
            h = (byte) (hl >>> 8);
            l = (byte) (hl & 0xff);

            this.kf = l;
            this.noteNumber = h;

            wopm((byte) (0x30 + (e & 0xff)), l);
            wopm((byte) (0x28 + (e & 0xff)), KTABLE[h & 0xff]);
        }

        private void EPMP(byte a, byte h, byte l, byte e) {
            int p = (this.softPMPitch & 0xff) * 4;
            int hl = (h & 0xff) * 0x100 + (l & 0xff);
            if ((((hl + p) >>> 8) & 0xff) >= 120) {
                hl = 0x77fc;
            } else hl = hl + p;
            h = (byte) (hl >>> 8);
            l = (byte) (hl & 0xff);

            this.kf = l;
            this.noteNumber = h;

            wopm((byte) (0x30 + (e & 0xff)), l);
            wopm((byte) (0x28 + (e & 0xff)), KTABLE[h & 0xff]);
        }

        private void EAM(byte e) {

//logger.log(Level.TRACE, "softPMProcCount[%d]".formatted(this.softPMProcCount));
//logger.log(Level.TRACE, "softPMStep[%d]".formatted(this.softPMStep));
//logger.log(Level.TRACE, "softPMStepCount[%d]".formatted(this.softPMStepCount));
//logger.log(Level.TRACE, "softPMPitch[%d]".formatted(this.softPMPitch));

            if (this.softAMDelayCount - 1 != 0) {
                this.softAMDelayCount--;
                return;
            }

//EAM1:
            this.softAMStepCount--;
            if (this.softAMStepCount == 0) {
//EAMS:
                this.softAMStepCount = this.softAMStep;
                this.softAMProcCount ^= 1;
            }

// EAML:
            for (int b = 0; b < 4; b++) {
                if ((byte) (this.softAMSelOP & (1 << b)) == 0) continue;

                switch (b) {
                case 0:
                    if (this.softAMProcCount == 0) this.op1Tls += this.softAMDepth;
                    else this.op1Tls -= this.softAMDepth;
                    wopm((byte) (0x60 + (e & 0xff)), this.op1Tls);
                    break;
                case 1:
                    if (this.softAMProcCount == 0) this.op2Tls += this.softAMDepth;
                    else this.op2Tls -= this.softAMDepth;
                    wopm((byte) (0x70 + (e & 0xff)), this.op2Tls);
                    break;
                case 2:
                    if (this.softAMProcCount == 0) this.op3Tls += this.softAMDepth;
                    else this.op3Tls -= this.softAMDepth;
                    wopm((byte) (0x68 + (e & 0xff)), this.op3Tls);
                    break;
                case 3:
                    if (this.softAMProcCount == 0) this.op4Tls += this.softAMDepth;
                    else this.op4Tls -= this.softAMDepth;
                    wopm((byte) (0x78 + (e & 0xff)), this.op4Tls);
                    break;
                }
            }
        }

        private void EPOP(byte e) {
            if (this.portaStartFlg == 0) return;

            byte b = this.op1Tls;
            byte a = this.op2Tls;
            a += b;
            if ((a & 0xff) - 100 >= 0) {
                a -= 100;
                this.op2Tls = a;
                a = this.portaFlg;
                a &= 0x7f;
                a++;
            } else {
                this.op2Tls = a;
                a = this.portaFlg;
                a &= 0x7f;
            }
            this.op3Tls = a;

            byte l = (byte) this.psgTone;
            byte h = (byte) (this.psgTone >>> 8);
            b = (byte) (this.portaTone >>> 8);
            byte c = (byte) (this.portaTone & 0xff);

            a = h;

            boolean neg = this.psgTone >= this.portaTone;

            if (this.psgTone == this.portaTone) {
                this.portaStartFlg = 0;
                return;
            }

            //
            if (neg) {
                // Subtraction
                int p = this.op3Tls & 0xff;
                int hl = ((h & 0xff) * 0x100) + (l & 0xff) - p;
                h = (byte) (hl >>> 8);
                l = (byte) (hl & 0xff);

                if (hl < (b & 0xff) * 0x100 + (c & 0xff)) {
                    h = b;
                    l = c;
                }
            } else {
                // Addition
                int p = this.op3Tls & 0xff;
                int hl = ((h & 0xff) * 0x100) + (l & 0xff) + p;
                h = (byte) (hl >>> 8);
                l = (byte) (hl & 0xff);

                if (hl > (b & 0xff) * 0x100 + (c & 0xff)) {
                    h = b;
                    l = c;
                }
            }

            this.psgTone = ((h & 0xff) * 0x100) + (l & 0xff);

            wpsg((byte) ((e & 0xff) * 2), l);
            wpsg((byte) ((e & 0xff) * 2 + 1), h);
        }

        private void PEPM(byte e) {
            if (this.portaStartFlg != 0) return;

//logger.log(Level.TRACE, "softPMProcCount[%d]".formatted(this.softPMProcCount));
//logger.log(Level.TRACE, "softPMStep[%d]".formatted(this.softPMStep));
//logger.log(Level.TRACE, "softPMStepCount[%d]".formatted(this.softPMStepCount));
//logger.log(Level.TRACE, "softPMPitch[%d]".formatted(this.softPMPitch));
//logger.log(Level.TRACE, "KF[%d]".formatted(this.KF));
//logger.log(Level.TRACE, "NoteNumber[%d]".formatted(this.NoteNumber));

            if (this.softPMDelayCount - 1 != 0) {
                this.softPMDelayCount--;
                return;
            }

//PEPM1:
            byte l = (byte) (this.psgTone);
            byte h = (byte) (this.psgTone >>> 8);

            byte a = this.softPMStepCount;
            a--;
            if (a != 0) {
                this.softPMStepCount = a;
                a = this.softPMProcCount;
                if ((a & 0xff) >= 8) return;
                if ((a & 0xff) >= 4) PEPMH1(a, h, l, e);
                    // PEPMS1
                else if (a == 0 || a == 3) {
                    PEPMM(a, h, l, e);
                } else {
                    PEPMP(a, h, l, e);
                }
                return;
            }
//PEPMS:
            a = this.softPMStep;
            this.softPMStepCount = a;

            a = this.softPMProcCount;
            if ((a & 0xff) - 8 < 0) {
//PEPMS0:
                if ((a & 0xff) >= 4) {
//PEPMH:
                    a++;
                    if ((a & 0xff) >= 8) a = 4;
                    this.softPMProcCount = a;
                    EPMH1(a, h, e);
                    return;
                }
                a++;
                a &= 3;
                this.softPMProcCount = a;
//EPMS1:
                if (a == 0 || (a & 0xff) >= 3) {
                    PEPMM(a, h, l, e);
                } else {
                    PEPMP(a, h, l, e);
                }
                return;
            }
//PEPMQ:
            a++;
            if (a == 10 || (a & 0xff) > 12) {
                // PEPMQ0
                a--;
                a--;
            }
//PEPMQ1:
            this.softPMProcCount = a;
            if (a == 8 || (a & 0xff) >= 11) {
                PEPMP(a, h, l, e);
            } else {
                PEPMM(a, h, l, e);
            }
        }

        private void PEPMH1(byte a, byte h, byte l, byte e) {
            byte b = e;
            // byte c = 0;
            int hl;

            if (a == 4 || a == 7) {
                // PEPMMH
                hl = (this.softPMPitch & 0xff) * 64;
                int x = ((h & 0xff) * 0x100 + (l & 0xff)) - hl;

                hl = Math.max(x, 0);
            } else {
                // PEPMPH
                hl = (this.softPMPitch & 0xff) * 64;
                int x = ((h & 0xff) * 0x100 + (l & 0xff)) + hl;

                if ((x >> 8) >= 16) {
                    hl = 4095;
                } else {
                    hl = x;
                }
            }

//PEPM2H:
            this.psgTone = hl;
            wpsg((byte) ((e & 0xff) * 2), (byte) hl);
            wpsg((byte) ((e & 0xff) * 2 + 1), (byte) (hl >>> 8));
        }

        private void PEPMM(byte a, byte h, byte l, byte e) {
            int p = this.softPMPitch & 0xff;
            int hl = (h & 0xff) * 0x100 + (l & 0xff);
            hl = Math.max(hl - p, 0);
            h = (byte) (hl >>> 8);
            l = (byte) (hl & 0xff);

            this.psgTone = (h & 0xff) * 0x100 + (l & 0xff);

            wpsg((byte) ((e & 0xff) * 2), l);
            wpsg((byte) ((e & 0xff) * 2 + 1), h);
        }

        private void PEPMP(byte a, byte h, byte l, byte e) {
            int p = this.softPMPitch;
            int hl = (h & 0xff) * 0x100 + (l & 0xff);
            if ((((hl + p) >> 8) & 0xff) >= 16) {
                hl = 4095;
            } else hl = hl + p;
            h = (byte) (hl >>> 8);
            l = (byte) (hl & 0xff);

            this.psgTone = (h & 0xff) * 0x100 + (l & 0xff);

            wpsg((byte) ((e & 0xff) * 2), l);
            wpsg((byte) ((e & 0xff) * 2 + 1), h);
        }

        public void efx(byte e) {
            if (this.portaFlg != 0) this.EPOR(e);

            if (this.softPMStep != 0) this.EPM(e);

            if (this.softAMStep != 0) this.EAM(e);
        }

        public void efpx(byte e) {
            if (this.portaFlg != 0) this.EPOP(e);

            if (this.softPMStep != 0) this.PEPM(e);
        }

        public void mmain(byte e) {
MAINL:
            if (this.trackStopFlg == 0) {
                if (this.keyOffFlg != 0) {
                    this.keyOffFlg = 0;
                    this.workForPlayer = 0;
                    wopm((byte) 8, e); // Key off
                }

                if (this.counter == 0) {
                    if (this.isCountNext == 0) {
                        this.comchk(e);
                        break MAINL;
                    }
                    byte d = ram[this.ptrData];
                    this.ptrData++;
                    if (d != (byte) 0xff) {
                        if (d == 0) {
                            this.comchk(e);
                            break MAINL;
                        }
                        this.isCountNext = 0;
                    }
                    this.counter = d;
                }
                this.counter--;

                if ((this.gatetime & 0xff) > (this.counter & 0xff)) {
                    if (this.legartFlg == 0) {
                        if (this.isCountNext == 0) {
                            this.keyOffFlg = 1;
                        }
                    }
                }
            }
            if ((work.plyFlg & 0x3) > 1) {
                this.mvset(e);
            }
        }

        public void pmain(byte e) {
            if (this.keyOffFlg == 2) {
                this.PSGRR();
            } else if (this.keyOffFlg != 0) {
                this.RRST(e);
            }

//PMAIN2:
            byte a = this.trackStopFlg;
PMAINL:
            if (a == 0) {
//PMAIN3:
                byte c = this.counter;
                if (c == 0) {
                    if (this.isCountNext == 0) {
                        this.PCOM(e);
                        break PMAINL;
                    }
                    a = ram[this.ptrData];
                    this.ptrData++;
                    if (a != (byte) 255) {
                        if (a == 0) {
                            this.PCOM(e);
                            break PMAINL;
                        }
                        this.isCountNext = 0;
                    }
                    this.counter = a;
                }
//PMAIN1:
                this.counter--;

                if ((this.counter & 0xff) - (this.gatetime & 0xff) < 0) {
                    if (this.legartFlg == 0) {
                        if (this.isCountNext == 0) {
                            if (this.keyOffFlg == 0)
                                this.keyOffFlg = 1;
                        }
                    }
                }
            }

            if (this.psgToneAdr != 0) {
                this.PENV(e);
            }
        }
    }

    private void efx(Ch[] chs) {
        for (byte e = 0; e < 8; e++) {
            Ch wch = chs[e];
            wch.efx(e);
        }
    }

    private void efxp(Ch[] chs) {
        for (byte e = 0; e < 3; e++) {
            Ch wch = chs[e];
            wch.efpx(e);
        }
    }

    private void mmain(Ch[] chs) {
        for (byte e = 0; e < 8; e++) {
            Ch wch = chs[e];
            wch.mmain(e);
        }
    }

    private void pmain(Ch[] chs) {
        for (byte e = 0; e < 3; e++) {
            Ch wch = chs[e];
            wch.pmain(e);
        }
    }
}
