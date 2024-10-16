package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


/**
 * part of LFO
 */
public class ComLfo {
    public Reg reg;
    public XMemory mm;
    public DevOpm devopm;

    private int sp1 = 0;

    //
    public void _init_lfo2() {
        sp1 = reg.D1_L;
        if ((mm.readByte(reg.a5 + W.ch) & 0xff) < 0x80) {
            initLfo2Exit();
            return;
        }

        if ((mm.readByte(reg.a5 + W.ch) & 0xff) >= 0x88) {
            initLfo2Exit();
            return;
        }

        mm.write(reg.a5 + W.addkeycode, (short) 0);
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        devopm._set_kckf();
        initLfo2Exit();
    }

    public void initLfo2Exit() {
        reg.D1_L = sp1;
    }

    public void initLfo() {
        if ((mm.readByte(reg.a5 + W.effect) & 0x20) != 0) {
            reg.a4 = reg.a5 + W.ww_pattern1;
            if (mm.readByte(reg.a4 + W_Ww.sync) != 0) {

                reg.setD0_B(mm.readByte(reg.a4 + W_Ww.speed));
                mm.write(reg.a4 + W_Ww.rate_work, mm.readByte(reg.a4 + W_Ww.rate));
                reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_Ww.delay));
                mm.write(reg.a4 + W_Ww.delay_work, (byte) reg.getD0_B());
                mm.write(reg.a4 + W_Ww.depth_work, mm.readByte(reg.a4 + W_Ww.depth));
                mm.write(reg.a4 + W_Ww.work, (byte) 0x00);
            }
        }

        reg.D5_L = 0xe;
        reg.setD5_B(reg.getD5_B() & mm.readByte(reg.a5 + W.lfo));
        if (reg.getD5_B() != 0) {
            switch (reg.getD5_B()) {
            case 2:
                initLfo1();
                break;
            case 4:
                initLfo2();
                break;
            case 6:
                initLfo3();
                break;
            case 8:
                initLfo4();
                break;
            case 10:
                initLfo5();
                break;
            case 12:
                initLfo6();
                break;
            case 14:
                initLfo7();
                break;
            }
        }
        reg.a4 = reg.a5 + W.p_pattern4;
        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xfc));
    }

    //

    public void initLfo1() {
        reg.a4 = reg.a5 + W.p_pattern1;
        initLfoCommon();
    }

    public void initLfo2() {
        reg.a4 = reg.a5 + W.p_pattern2;
        initLfoCommon();
    }

    public void initLfo3() {
        reg.a4 = reg.a5 + W.p_pattern1;
        initLfoCommon();
        reg.a4 = reg.a5 + W.p_pattern2;
        initLfoCommon();
    }

    public void initLfo4() {
        reg.a4 = reg.a5 + W.p_pattern3;
        initLfoCommon();
    }

    public void initLfo5() {
        reg.a4 = reg.a5 + W.p_pattern1;
        initLfoCommon();
        reg.a4 = reg.a5 + W.p_pattern3;
        initLfoCommon();
    }

    public void initLfo6() {
        reg.a4 = reg.a5 + W.p_pattern2;
        initLfoCommon();
        reg.a4 = reg.a5 + W.p_pattern3;
        initLfoCommon();
    }

    public void initLfo7() {
        reg.a4 = reg.a5 + W.p_pattern1;
        initLfoCommon();
        reg.a4 = reg.a5 + W.p_pattern2;
        initLfoCommon();
        reg.a4 = reg.a5 + W.p_pattern3;
        initLfoCommon();
    }

    //

    public void initLfoCommon() {
        if ((mm.readByte(reg.a4 + W_L.flag) & 0x40) != 0) {
            return;
        }

        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a4 + W_L.flag, (short) (mm.readShort(reg.a4 + W_L.flag) & 0xdfff));

        reg.setD0_B(mm.readByte(reg.a6 + Dw.DRV_FLAG2));
        if ((byte) reg.getD0_B() < 0) {
            initLfoTruetie();
            return;
        }
        if ((reg.D0_L & 0x40) != 0) {
            initLfoFmptie();
            return;
        }

        reg.setD0_B(mm.readByte(reg.a4 + W_L.lfo_sp));
        if ((mm.readByte(reg.a5 + W.flag) & 0x01) == 0) {
            mm.write(reg.a4 + W_L.henka_work, mm.readShort(reg.a4 + W_L.henka));
            reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        }

        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
        reg.setD0_B(mm.readByte(reg.a4 + W_L.count));
        reg.setD0_B(reg.getD0_B() >> 1);
        mm.write(reg.a4 + W_L.count_work, (byte) reg.getD0_B());
    }

    public void initLfoTruetie() {
        reg.setD0_B(mm.readByte(reg.a5 + W.flag));
        if ((reg.getD0_B() & 0x1) != 0) {
            reg.setD0_B(mm.readByte(reg.a4 + W_L.count));
            reg.setD0_B(reg.getD0_B() >> 1);
            mm.write(reg.a4 + W_L.count_work, (byte) reg.getD0_B());
            return;
        }
        if ((reg.D0_L & 0x40) != 0) return;
        initLfoFmptie();
    }

    public void initLfoFmptie() {
        reg.setD0_B(mm.readByte(reg.a4 + W_L.lfo_sp));
        mm.write(reg.a4 + W_L.henka_work, mm.readShort(reg.a4 + W_L.henka));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
        reg.setD0_B(mm.readByte(reg.a4 + W_L.count));
        reg.setD0_B(reg.getD0_B() >> 1);
        mm.write(reg.a4 + W_L.count_work, (byte) reg.getD0_B());
    }

    public void initLfoCommonA() {
        if ((mm.readByte(reg.a4 + W_L.flag) & 0x40) != 0) {
            return;
        }

        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a4 + W_L.flag, (short) (mm.readShort(reg.a4 + W_L.flag) & 0xdfff));

        reg.setD0_B(mm.readByte(reg.a6 + Dw.DRV_FLAG2));
        if ((byte) reg.getD0_B() < 0) {
            _init_lfo_truetie_a();
            return;
        }
        if ((reg.D0_L & 0x40) != 0) {
            initLfoFmptieA();
            return;
        }

        reg.setD0_B(mm.readByte(reg.a4 + W_L.lfo_sp));
        if ((mm.readByte(reg.a5 + W.flag) & 0x01) == 0) {
            mm.write(reg.a4 + W_L.henka_work, mm.readShort(reg.a4 + W_L.henka));
            reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        }

        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
    }

    public void _init_lfo_truetie_a() {
        reg.setD0_B(mm.readByte(reg.a5 + W.flag));
        if ((reg.getD0_B() & 0x1) != 0) {
            mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
            return;
        }

        if ((reg.D0_L & 0x40) != 0) return;
        initLfoFmptieA();
    }

    public void initLfoFmptieA() {
        reg.setD0_B(mm.readByte(reg.a4 + W_L.lfo_sp));
        mm.write(reg.a4 + W_L.henka_work, mm.readShort(reg.a4 + W_L.henka));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
    }

    /**
     * LFO 鋸波
     */
    public void comLfoSaw() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) {

            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
            mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + reg.getD1_W()));

            mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
            if (mm.readByte(reg.a4 + W_L.count_work) == 0) {

                mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
                reg.setD1_W(mm.readShort(reg.a4 + W_L.bendwork));
                reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
                mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) - reg.getD1_W()));
            }
        }
        reg.setD1_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    /**
     * LFO portament
     */
    public void comLfoPortament() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_lfo_portament_end;

            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
            mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        }
//            _com_lfo_portament_end:
        reg.setD1_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    /**
     * LFO triangle
     */
    public void comLfoTriangle() {
        //if(Reg.a4 == 0x14cf0) Debug.printf(String.format("adr:%x bendwork:%d",Reg.a4, mm.Readshort(Reg.a4 + W_L.bendwork)));

        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_lfo_triangle_end;

            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
            mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));

            mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
            if (mm.readByte(reg.a4 + W_L.count_work) == 0) { // break _com_lfo_triangle_end;
                mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
                mm.write(reg.a4 + W_L.henka_work, (short) (-(short) mm.readShort(reg.a4 + W_L.henka_work)));
            }
        }
//            _com_lfo_triangle_end:
        reg.setD1_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    /**
     * LFO 1shot
     */
    public void comLfoOneshot() {
        reg.setD2_W(mm.readShort(reg.a4 + W_L.flag));
        if (reg.getD2_W() == 0) { // break _com_lfo_oneshot_end;

            mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
            if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_lfo_oneshot_end;

                mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
                reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
                mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));

                mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
                if (mm.readByte(reg.a4 + W_L.count_work) == 0) { // break _com_lfo_oneshot_end;
                    mm.write(reg.a4 + W_L.flag, (short) (mm.readShort(reg.a4 + W_L.flag) | 0x2000));
                }
            }
        }
//        _com_lfo_oneshot_end:
        reg.setD1_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    /**
     * LFO square
     */
    public void comLfoSquare() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_lfo_square_end;

            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
            mm.write(reg.a4 + W_L.bendwork, mm.readShort(reg.a4 + W_L.henka_work));

            mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
            if (mm.readByte(reg.a4 + W_L.count_work) == 0) { // break _com_lfo_square_end;
                mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
                mm.write(reg.a4 + W_L.henka_work, (short) (-(short) mm.readShort(reg.a4 + W_L.henka_work)));
            }
        }
//            _com_lfo_square_end:
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
    }

    /**
     * LFO random
     */
    public void comLfoRandom() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_lfo_randome_end;

            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
            getRandom();
            reg.D1_L = 0;
            reg.setD1_B(mm.readByte(reg.a4 + W_L.count));
            reg.D1_L = reg.getD1_W() * mm.readShort(reg.a4 + W_L.henka_work);
            reg.D0_L = (short) reg.getD0_W() * (short) reg.getD1_W();
            reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);

            mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
            if (mm.readByte(reg.a4 + W_L.count_work) == 0) { // break _com_lfo_randome_end;
                mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
                mm.write(reg.a4 + W_L.henka_work, (short) (-(short) mm.readShort(reg.a4 + W_L.henka_work)));
            }
        }
//            _com_lfo_randome_end:
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
    }

    /**
     * 乱数を得る
     * out
     * d0.l	乱数
     */
    public void getRandom() {
        reg.D0_L = mm.readInt(reg.a6 + Dw.RANDOMESEED);
        reg.D1_L = reg.D0_L;
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.D1_L = (reg.D1_L >> 16) + (reg.D1_L << 16);
        reg.setD1_W(0);
        reg.D0_L ^= reg.D1_L;
        reg.D1_L = reg.D0_L;
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(0);
        reg.D1_L = (reg.D1_L >> 16) + (reg.D1_L << 16);
        reg.D1_L += reg.D1_L;
        reg.D0_L ^= reg.D1_L;
        reg.D0_L = ~reg.D0_L;
        mm.write(reg.a6 + Dw.RANDOMESEED, reg.D0_L);
    }

    /**
     * ソフトウェアエンベロープ
     */
    public void softEnv() {
// _soft1:
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        boolean _soft1_volume_set = false;
_soft1_end:
        if (reg.getD0_B() != 0) { // break _soft1_end;
// _soft1_ar:
            reg.setD0_B(reg.getD0_B() - 1);
_soft1_ok:
            if (reg.getD0_B() == 0) { // break _soft1_dr;
                reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
                boolean cf = reg.cryADD((byte) reg.getD0_B(), mm.readByte(reg.a5 + W.e_ar));
                reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.e_ar));
                if (cf) { // break _soft1_ok;
                    reg.setD0_B(0xff);
                    mm.write(reg.a5 + W.e_p, (byte) 2);
                }
//                break _soft1_ok;
            } else {
// _soft1_dr:
                reg.setD0_B(reg.getD0_B() - 1);
                if (reg.getD0_B() == 0) { // break _soft1_sr;
                    reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
                    boolean cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_dr);
                    reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_dr));
                    if (!cf) { // break _soft1_dr2;
                        if (reg.getD0_B() >= mm.readByte(reg.a5 + W.e_sl)) break _soft1_ok;
                    }
// _soft1_dr2:
                    reg.setD0_B(mm.readByte(reg.a5 + W.e_sl));
                    mm.write(reg.a5 + W.e_p, (byte) 3);
//                    break _soft1_ok;
                } else {
// _soft1_sr:
                    reg.setD0_B(reg.getD0_B() - 1);
                    if (reg.getD0_B() == 0) { // break _soft1_rr;
                        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
                        boolean cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_sr);
                        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_sr));
                        if (reg.getD0_B() == 0) break _soft1_end;
                        if (!cf) break _soft1_ok;
                        break _soft1_end;
                    }
// _soft1_rr:
                    reg.setD0_B(reg.getD0_B() - 1);
                    if (reg.getD0_B() == 0) { // break _soft1_ko;
                        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
                        boolean cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_rr);
                        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_rr));
                        if (reg.getD0_B() == 0) break _soft1_end;
                        if (!cf) break _soft1_ok;
                        if (cf) break _soft1_end;
                    }
// _soft1_ko:
                    mm.write(reg.a5 + W.e_p, (byte) 1);
                    reg.setD0_B(mm.readByte(reg.a5 + W.e_sv));
                }
            }
// _soft1_ok:
            mm.write(reg.a5 + W.e_sub, (byte) reg.getD0_B());
            _soft1_volume_set = true; // break _soft1_volume_set;
        }
// _soft1_end:
        if (!_soft1_volume_set) {
            reg.D4_L = 0;
            mm.write(reg.a5 + W.e_sub, (byte) reg.getD4_B());
            mm.write(reg.a5 + W.e_p, (byte) reg.getD4_B());
            reg.a2 = reg.a5 + W.voltable;
            reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD4_W()));
            return;
        }
// _soft1_volume_set:
        reg.D2_L = 0;
        reg.setD2_B(reg.getD0_B());
        if (reg.getD2_B() != 0) { // break _soft1_vol_ok;
            reg.D0_L = 0;
            reg.D1_L = 0;
            reg.setD1_B(mm.readByte(reg.a5 + W.volume));
            if (reg.getD1_B() != 0) { // break _soft1_vol_ok;
                do {
                    reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD2_W());
                } while (reg.decAfterD1_W() != 0);
                reg.setD0_B(reg.getD0_W() >> 8);
            }
        }
// _soft1_vol_ok:
        reg.setD0_W(reg.getD0_W() & 0xff);
        reg.a2 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD0_W()));
    }
}
