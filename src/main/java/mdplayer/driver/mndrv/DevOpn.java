package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


//
// part of YM2608 - FM
//
public class DevOpn {

    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public ComAnalyze comanalyze;
    public ComCmds comcmds;
    public ComLfo comlfo;
    public ComWave comwave;

    /** */
    public void _fm_exit() {
        // rts
    }

    public void _fm_note_set() {
        mm.write(reg.a5 + W.key, (byte) reg.getD0_B());

        reg.setD1_W(0x800);
        reg.D2_L = 0;
        reg.D3_L = 12;

        while (reg.getD0_B() >= reg.getD3_B()) {
            reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD3_B());
            reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        }

        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD2_W(reg.getD2_W() | _fnum_table[reg.getD0_W() / 2]); // mm.Readshort(_fnum_table + reg.getD0_W());
        // pea _fm_keyon(pc)
        _set_fnum();
        comlfo.initLfo();
        _init_lfo_fm();
        _fm_keyon();
    }

    /** */
    public static final short[] _fnum_table = new short[] {
            0x026A, 0x028F, 0x02B6, 0x02DF, 0x030B, 0x0339,
            0x036A, 0x039E, 0x03D5, 0x0410, 0x044E, 0x048F
    };

    /**
     * SET F-Number
     */
    public void _set_fnum() {
        reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD2_W() < 0) {
            reg.D2_L = 0;
        }
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());

        if (mm.readByte(reg.a5 + W.ch3mode) != 0) {
            _set_fnum_ch3();
            return;
        }

        if (reg.getD2_W() - mm.readShort(reg.a5 + W.tune) == 0) return;

        mm.write(reg.a5 + W.tune, (short) reg.getD2_W());

        reg.setD0_B((byte) (reg.getD2_W() >> 8));
        reg.D1_L = 0xa4;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        mndrv._OPN_WRITE2();
        reg.setD0_B(reg.getD2_B());
        reg.setD1_W(reg.getD1_W() - 4);
        mndrv._OPN_WRITE2();
    }

    public void _set_fnum_ch3() {
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3));
        int f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD3_W(reg.getD2_W());
            reg.setD3_W(reg.getD3_W() + mm.readShort(reg.a5 + W.sdetune4));
            if ((short) reg.getD3_W() < 0) {
                reg.D3_L = 0;
            }

            mm.write(reg.a5 + W.keycode_s4, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode3_s4, (short) reg.getD3_W());

            reg.setD0_B((byte) (reg.getD3_W() >> 8));
            reg.D1_L = 0xa6;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD3_B());
            reg.D1_L = 0xa2;
            mndrv._OPN_WRITE3();
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD3_W(reg.getD2_W());
            reg.setD3_W(reg.getD3_W() + mm.readShort(reg.a5 + W.sdetune3));
            if ((short) reg.getD3_W() < 0) {
                reg.D3_L = 0;
            }

            mm.write(reg.a5 + W.keycode_s3, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode3_s3, (short) reg.getD3_W());

            reg.setD0_B((byte) (reg.getD3_W() >> 8));
            reg.D1_L = 0xac;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD3_B());
            reg.D1_L = 0xa8;
            mndrv._OPN_WRITE3();
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD3_W(reg.getD2_W());
            reg.setD3_W(reg.getD3_W() + mm.readShort(reg.a5 + W.sdetune2));
            if ((short) reg.getD3_W() < 0) {
                reg.D3_L = 0;
            }

            mm.write(reg.a5 + W.keycode_s2, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode3_s2, (short) reg.getD3_W());

            reg.setD0_B((byte) (reg.getD3_W() >> 8));
            reg.D1_L = 0xae;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD3_B());
            reg.D1_L = 0xaa;
            mndrv._OPN_WRITE3();
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD3_W(reg.getD2_W());
            reg.setD3_W(reg.getD3_W() + mm.readShort(reg.a5 + W.sdetune1));
            if ((short) reg.getD3_W() < 0) {
                reg.D3_L = 0;
            }

            mm.write(reg.a5 + W.keycode_s1, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD3_W());
            mm.write(reg.a5 + W.keycode3_s1, (short) reg.getD3_W());

            reg.setD0_B((byte) (reg.getD3_W() >> 8));
            reg.D1_L = 0xad;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD3_B());
            reg.D1_L = 0xa9;
            mndrv._OPN_WRITE3();
        }
    }

    /**
     * SET F-Number
     */
    public void _set_fnum2() {
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());

        if (mm.readByte(reg.a5 + W.ch3mode) != 0) {
            _set_fnum2_ch3();
            return;
        }

        mm.write(reg.a5 + W.tune, (short) reg.getD2_W());

        reg.setD0_B((byte) (reg.getD2_W() >> 8));
        reg.D1_L = 0xa4;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        mndrv._OPN_WRITE2();
        reg.setD0_B(reg.getD2_B());
        reg.setD1_W(reg.getD1_W() - 4);
        mndrv._OPN_WRITE2();
    }

    public void _set_fnum2_ch3() {
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3));

        int f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode2_s4));

            reg.setD0_B((byte) (reg.getD2_W() >> 8));
            reg.D1_L = 0xa6;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD2_B());
            reg.D1_L = 0xa2;
            mndrv._OPN_WRITE3();
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode2_s3));

            reg.setD0_B((byte) (reg.getD2_W() >> 8));
            reg.D1_L = 0xac;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD2_B());
            reg.D1_L = 0xa8;
            mndrv._OPN_WRITE3();
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode2_s2));

            reg.setD0_B((byte) (reg.getD2_W() >> 8));
            reg.D1_L = 0xae;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD2_B());
            reg.D1_L = 0xaa;
            mndrv._OPN_WRITE3();
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode2_s1));

            reg.setD0_B((byte) (reg.getD2_W() >> 8));
            reg.D1_L = 0xad;
            mndrv._OPN_WRITE3();
            reg.setD0_B(reg.getD2_B());
            reg.D1_L = 0xa9;
            mndrv._OPN_WRITE3();
        }
    }

    /**
     * KEY ON
     */
    public void _fm_keyon() {
        reg.D0_L = 3;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.effect));
        if (reg.getD0_B() - 3 == 0) {
            _FM_RR_cut();
        }
        comwave._wave_init_kon();

        if (mm.readByte(reg.a5 + W.flag2) < 0) {
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a5 + W.ch));
            if (mm.readByte(reg.a5 + W.ch3mode) != 0) {
                _fm_keyoff_ch3();
                return;
            }
            reg.a0 = Ab.dummyAddress; // _keyon_table;
            reg.setD0_B(_keyon_table[reg.getD0_W()]); // mm.readByte(Reg.a0 + reg.getD0_W());
            reg.D1_L = 0x28;
            mndrv._OPN_WRITE3();
            return;
        }

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x20));
        if ((mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) return;
        }

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xfb));
        if (mm.readByte(reg.a5 + W.reverb) < 0) {
            _FM_echo_ret();
        }

        mm.write(reg.a5 + W.e_p, (byte) 5);
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.ch));
        if (mm.readByte(reg.a5 + W.ch3mode) != 0) {
            _fm_keyon_ch3();
            return;
        }

        reg.setD0_B(_keyon_table[reg.getD0_W()]); // mm.readByte(_keyon_table + reg.getD0_W());
        reg.setD0_B(reg.getD0_B() | mm.readByte(reg.a5 + W.smask));
        reg.D1_L = 0x28;
        mndrv._OPN_WRITE3();

        reg.D0_L = 3;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.effect));
        if (reg.getD0_B() != 0) {
            _FM_RR_ret();
        }
    }

    public void _fm_keyon_ch3() {
        reg.setD3_B(mm.readByte(reg.a5 + W.ch3));
        reg.setD2_B(mm.readByte(reg.a6 + Dw.CH3KOM));
        if (reg.getD0_B() >= 6) {
            reg.setD2_B(mm.readByte(reg.a6 + Dw.CH3KOS));
        }

        reg.setD2_B(reg.getD2_B() | reg.getD3_B());
        if (reg.getD0_B() < 6) {
            mm.write(reg.a6 + Dw.CH3KOM, (byte) reg.getD2_B());
        } else {
            mm.write(reg.a6 + Dw.CH3KOS, (byte) reg.getD2_B());
        }

        reg.setD0_B(_keyon_table[reg.getD0_W()]);
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        reg.D1_L = 0x28;
        mndrv._OPN_WRITE3();

        reg.D0_L = 3;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.effect));
        if (reg.getD0_B() != 0) {
            _FM_RR_ret();
        }
    }

    /** */
    public byte[] _keyon_table = new byte[] {
            0x00, 0x01, 0x02, 0x04, 0x05, 0x06
            , 0x00, 0x01, 0x02, 0x04, 0x05, 0x06
    };

    /**
     * KEY OFF
     */
    public void _fm_keyoff() {
        comwave._wave_init_kof();

        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
        reg.setD0_B(mm.readByte(reg.a5 + W.effect));
        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            f = reg.getD0_B() & 1;
            reg.setD0_B(reg.getD0_B() >> 1);
            if (f == 0) {
                _FM_RR_cut();
            }
        }
        if (mm.readByte(reg.a5 + W.kom) != 0) {
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
            return;
        }
        _fm_keyoff2();
    }

    public void _fm_keyoff2() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
        _fm_keyoff_direct();
    }

    public void _fm_keyoff_direct() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.ch));
        mm.write(reg.a5 + W.e_p, 4);
        if (mm.readByte(reg.a5 + W.ch3mode) != 0) {
            _fm_keyoff_ch3();
            return;
        }

        if (reg.getD0_B() - 2 == 0) {
            if (mm.readByte(reg.a6 + Dw.CH3MODEM) != 0) return;
        } else {
            if (reg.getD0_B() - 8 == 0) {
                if (mm.readByte(reg.a6 + Dw.CH3MODES) != 0) return;
            }
        }
        reg.setD0_B(_keyon_table[reg.getD0_W()]); // mm.readByte(_keyon_table + reg.getD0_W());
        reg.D1_L = 0x28;
        mndrv._OPN_WRITE3();
    }

    public void _fm_keyoff_ch3() {
        reg.setD3_B(mm.readByte(reg.a5 + W.ch3));
        reg.setD2_B(mm.readByte(reg.a6 + Dw.CH3KOM));
        if (reg.getD0_B() >= 6) {
            reg.setD2_B(mm.readByte(reg.a6 + Dw.CH3KOS));
        }
        reg.setD3_B(~reg.getD3_B());
        reg.setD3_B(reg.getD3_B() & reg.getD2_B());
        if (reg.getD0_B() < 6) {
            mm.write(reg.a6 + Dw.CH3KOM, (byte) reg.getD3_B());
        } else {
            mm.write(reg.a6 + Dw.CH3KOS, (byte) reg.getD3_B());
        }
        reg.a0 = Ab.dummyAddress; // _keyon_table;
        reg.setD0_B(_keyon_table[reg.getD0_W()]); // mm.readByte(Reg.a0 + reg.getD0_W());
        reg.setD0_B(reg.getD0_B() | reg.getD3_B());
        reg.D1_L = 0x28;
        mndrv._OPN_WRITE3();
    }

    /**
     */
    public void _FM_RR_cut() {
        reg.a2 = reg.a5 + W.tone_rr;
        reg.D2_L = 0xf;
        reg.setD3_B(mm.readByte(reg.a5 + W.ch3tl));
        if (reg.getD3_B() != 0) {
            _FM_RR_loop_CH3();
            return;
        }
        reg.D1_L = 0x80;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        reg.setD0_B(mm.readByte(reg.a2++));
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        mndrv._OPN_WRITE2();
        reg.setD1_B(reg.getD1_B() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        mndrv._OPN_WRITE2();
        reg.setD1_B(reg.getD1_B() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        mndrv._OPN_WRITE2();
        reg.setD1_B(reg.getD1_B() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        mndrv._OPN_WRITE2();
    }

    public void _FM_RR_loop_CH3() {
        reg.setD0_B(mm.readByte(reg.a2++));
        int f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.setD0_B(reg.getD0_B() | reg.getD2_B());
            reg.D1_L = 0x82;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.setD0_B(reg.getD0_B() | reg.getD2_B());
            reg.D1_L = 0x86;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.setD0_B(reg.getD0_B() | reg.getD2_B());
            reg.D1_L = 0x8a;
            mndrv._OPN_WRITE2();
        }
        f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.setD0_B(mm.readByte(reg.a2++));
            reg.setD0_B(reg.getD0_B() | reg.getD2_B());
            reg.D1_L = 0x8e;
            mndrv._OPN_WRITE2();
        }
    }

    /**
     */
    public void _FM_RR_ret() {
        reg.a2 = reg.a5 + W.tone_rr;
        reg.setD3_B(mm.readByte(reg.a5 + W.ch3tl));
        if (reg.getD3_B() != 0) {
            _FM_RR_ret_CH3();
            return;
        }

        reg.D1_L = 0x80;

        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_B(reg.getD1_B() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_B(reg.getD1_B() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_B(reg.getD1_B() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
    }

    public void _FM_RR_ret_CH3() {
        reg.setD0_B(mm.readByte(reg.a2++));
        int f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x82;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x86;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x8a;
            mndrv._OPN_WRITE2();
        }
        f = reg.getD3_B() & 1;
        reg.setD3_B(reg.getD3_B() >> 1);
        if (f != 0) {
            reg.setD0_B(mm.readByte(reg.a2++));
            reg.D1_L = 0x8e;
            mndrv._OPN_WRITE2();
        }
    }

    /**
     */
    public void _FM_echo() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;

        int sp = reg.getD4_W();

        mm.write(reg.a5 + W.revexec, 0xff);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
        mm.write(reg.a5 + W.reverb_time_work, mm.readByte(reg.a5 + W.reverb_time));

        reg.D5_L = 7;
        reg.setD5_B(reg.getD5_B() & mm.readByte(reg.a5 + W.reverb));
        reg.setD5_W(reg.getD5_W() + 1);
        reg.setD5_W(reg.getD5_W() + (int) (short) reg.getD5_W());

         // _fm_echo_table:
        switch (reg.getD5_W() / 2) {
        case 1:
            _fm_echo_volume();
            break;
        case 2:
            _fm_echo_volume_pan();
            break;
        case 3:
            _fm_echo_volume_tone();
            break;
        case 4:
            _fm_echo_volume_pan_tone();
            break;
        case 5:
            _fm_echo_volume_();
            break;
        }

        reg.setD4_W(sp);
    }

    /** */
    public void _fm_echo_volume() {
        if ((mm.readByte(reg.a5 + W.reverb) & 0x10) != 0) {
            _fm_echo_common_atv();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _fm_echo_common_atv();
            return;
        }
        _fm_echo_common_v();
    }

    public void _fm_echo_volume_pan_tone() {
        _fm_echo_tone();
        _fm_echo_volume_pan();
    }

    public void _fm_echo_volume_pan() {
        _fm_echo_pan();
        _fm_echo_volume();
    }

    public void _fm_echo_volume_tone() {
        _fm_echo_tone();
        _fm_echo_volume();
    }

    public void _fm_echo_volume_() {
        if ((mm.readByte(reg.a5 + W.reverb) & 0x10) != 0) {
            _fm_echo_volume_atv();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _fm_echo_volume_atv();
            return;
        }
        _fm_echo_volume_v();
    }

    /** */
    public void _fm_echo_pan() {
        reg.D0_L = 0x3f;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.pan_ampm));
        reg.setD0_B(reg.getD0_B() | mm.readByte(reg.a5 + W.reverb_pan));
        reg.D1_L = 0xb4;
        mndrv._OPN_WRITE4();
    }

    /** */
    public void _fm_echo_tone() {
        reg.D5_L = 0;
        reg.setD5_B(mm.readByte(reg.a5 + W.reverb_tone));
        _fm_echo_tone_change();
    }

    /**
     * v通常
     */
    public void _fm_echo_common_v() {
        if ((mm.readByte(reg.a5 + W.reverb) & 0x08) != 0) {
            _fm_echo_direct_v();
            return;
        }

        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));

        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD0_B() >= 0) {
            _fm_echo_plus_v();
            return;
        }

        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _FM_F2_softenv();
    }

    public void _fm_echo_plus_v() {
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0x7f;
        }
        if (reg.getD4_B() >= mm.readByte(reg.a5 + W.volcount)) {
            reg.setD4_B(mm.readByte(reg.a5 + W.volcount));
            reg.setD4_B(reg.getD4_B() - 1);
        }
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _FM_F2_softenv();
    }

    /**
     * @v通常
     */
    public void _fm_echo_common_atv() {
        if ((mm.readByte(reg.a5 + W.reverb) & 0x08) != 0) {
            _fm_echo_direct_atv();
            return;
        }

        reg.setD4_B(mm.readByte(reg.a5 + W.vol));

        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD0_B() >= 0) {
            _fm_echo_plus();
            return;
        }

        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() >= 0) {
            _FM_F2_softenv();
        }
        reg.D4_L = 0;
        _FM_F2_softenv();
    }

    public void _fm_echo_plus() {
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() >= 0) {
            _FM_F2_softenv();
        }
        reg.D4_L = 0x7f;
        _FM_F2_softenv();
    }

    /**
     * v微調整
     */
    public void _fm_echo_volume_v() {
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));
        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD0_B() < 0) {
            _fm_echo_vol_v_plus();
            return;
        }

        reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        reg.setD4_B(reg.getD4_B() >> 1);
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _FM_F2_softenv();
    }

    public void _fm_echo_vol_v_plus() {
        reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
        reg.setD4_B(reg.getD4_B() >> 1);
        reg.setD4_W(reg.getD4_W() & 0x7f);
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _FM_F2_softenv();
    }

    /**
     * @v微調整
     */
    public void _fm_echo_volume_atv() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        reg.D0_L = 127;
        reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD4_B());
        reg.setD0_B(reg.getD0_B() >> 1);
        reg.D4_L = 127;
        reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
        _FM_F2_softenv();
    }

    /**
     * v直接
     */
    public void _fm_echo_direct_v() {
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.reverb_vol));
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _FM_F2_softenv();
    }

    /**
     * @v 直接
     */
    public void _fm_echo_direct_atv() {
        reg.setD4_B(mm.readByte(reg.a5 + W.reverb_vol));
        _FM_F2_softenv();
    }

    /**
     */
    public void _FM_echo_ret() {
        int sp = reg.getD4_W();

        mm.write(reg.a5 + W.revexec, 0);
        mm.write(reg.a5 + W.reverb_time_work, 0);

        _fm_keyoff_direct();
        if ((mm.readByte(reg.a5 + W.reverb) & 0x02) != 0) {
            reg.setD5_B(mm.readByte(reg.a5 + W.program2));
            _fm_echo_tone_change();
            reg.setD4_W(sp);
            return;
        }
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) != 0) {
            reg.D4_L = 0;
            reg.setD4_B(mm.readByte(reg.a5 + W.volume));
            reg.a0 = reg.a5 + W.voltable;
            reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        }

        _FM_F2_softenv();
        reg.setD5_B(mm.readByte(reg.a5 + W.reverb));
        int f = reg.getD5_B() & 1;
        reg.setD5_B(reg.getD5_B() >> 1);
        if (f != 0) {
            reg.setD0_B(mm.readByte(reg.a5 + W.pan_ampm));
            reg.D1_L = 0xb4;
            mndrv._OPN_WRITE4();
        }
        reg.setD4_W(sp);
    }

    /** */
    public void _init_hlfo() {
        if ((mm.readByte(reg.a5 + W.lfo) & 1) != 0) {
            reg.D0_L = 0xc;
            reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.flag2));
            if (reg.getD0_B() != 0) {
                reg.a4 = reg.a5 + W.v_pattern4;
                mm.write(reg.a4 + W_L.henka_work, (byte) 0);
                reg.setD0_B(mm.readByte(reg.a4 + W_L.lfo_sp));
                reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
                mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
                reg.D0_L = 0xc0;
                reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.pan_ampm));
                reg.D1_L = 0xb4;
                mndrv._OPN_WRITE4();
            }
        }
    }

    public void _init_lfo_fm() {
        if (mm.readByte(reg.a6 + Dw.FADEFLAG) == 0) {
            if ((mm.readByte(reg.a5 + W.effect) & 0x20) != 0) return;

            reg.D5_L = 0x70;
            reg.setD5_B(reg.getD5_B() & mm.readByte(reg.a5 + W.lfo));
            if (reg.getD5_B() == 0) return;

            reg.setD4_B(mm.readByte(reg.a5 + W.vol));
            _FM_F2_init();
        }

        reg.setD5_B(mm.readByte(reg.a5 + W.lfo));
        int f = reg.getD5_B() & 0x40; //lsl.b //#2
        reg.setD5_B(reg.getD5_B() << 2);
        if (f != 0) {
            reg.a4 = reg.a5 + W.v_pattern3;
            comlfo.initLfoCommonA();
        }
        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.a4 = reg.a5 + W.v_pattern2;
            comlfo.initLfoCommonA();
        }
        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.a4 = reg.a5 + W.v_pattern1;
            comlfo.initLfoCommonA();
        }
    }

    /**
     * MML コマンド処理 (FM 部)
     */
    public void _fm_command() {
        // HACK: (MNDRV) MML コマンド処理(OPN)
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        // _fmc:
        switch (reg.getD0_W() / 2) {
        // case 0x00: 0
        case 0x01:
            comcmds._COM_81();
            break; // 81
        case 0x02:
            _FM_82();
            break; // 82 key off
        case 0x03:
            comcmds._COM_83();
            break; // 83 すらー
        case 0x04:
            _FM_NOP();
            break; // 84
        case 0x05:
            _FM_NOP();
            break; // 85
        case 0x06:
            comcmds._COM_86();
            break; // 86 同期信号送信
        case 0x07:
            comcmds._COM_87();
            break; // 87 同期信号待ち
        case 0x08:
            _FM_88();
            break; // 88 ぴっちべんど
        case 0x09:
            _FM_89();
            break; // 89 ぽるためんと
        case 0x0a:
            _FM_NOP();
            break; // 8A
        case 0x0b:
            _FM_NOP();
            break; // 8B
        case 0x0c:
            _FM_NOP();
            break; // 8C
        case 0x0d:
            _FM_NOP();
            break; // 8D
        case 0x0e:
            _FM_NOP();
            break; // 8E
        case 0x0f:
            _FM_NOP();
            break; // 8F

        case 0x10:
            comcmds._COM_90();
            break; // 90 q
        case 0x11:
            comcmds._COM_91();
            break; // 91 @q
        case 0x12:
            _FM_92();
            break; // 92 keyoff rr cut switch
        case 0x13:
            comcmds._COM_93();
            break; // 93 neg @q
        case 0x14:
            comcmds._COM_94();
            break; // 94 keyoff mode
        case 0x15:
            _FM_NOP();
            break; // 95
        case 0x16:
            _FM_NOP();
            break; // 96
        case 0x17:
            _FM_NOP();
            break; // 97
        case 0x18:
            _FM_98();
            break; // 98 擬似リバーブ
        case 0x19:
            _FM_99();
            break; // 99 擬似エコー
        case 0x1a:
            comcmds._COM_9A();
            break; // 9A 擬似step time
        case 0x1b:
            _FM_NOP();
            break; // 9B
        case 0x1c:
            _FM_NOP();
            break; // 9C
        case 0x1d:
            _FM_NOP();
            break; // 9D
        case 0x1e:
            _FM_NOP();
            break; // 9E
        case 0x1f:
            _FM_NOP();
            break; // 9F

        case 0x20:
            _FM_F0();
            break; // A0 音色切り替え
        case 0x21:
            _FM_A1();
            break; // A1 バンク&音色切り替え
        case 0x22:
            _FM_NOP();
            break; // A2
        case 0x23:
            _FM_A3();
            break; // A3 音量テーブル切り替え
        case 0x24:
            _FM_F2();
            break; // A4 音量
        case 0x25:
            _FM_F5();
            break; // A5
        case 0x26:
            _FM_F6();
            break; // A6
        case 0x27:
            _FM_NOP();
            break; // A7
        case 0x28:
            comcmds._COM_A8();
            break; // A8 相対音量モード
        case 0x29:
            _FM_NOP();
            break; // A9
        case 0x2a:
            _FM_NOP();
            break; // AA
        case 0x2b:
            _FM_NOP();
            break; // AB
        case 0x2c:
            _FM_NOP();
            break; // AC
        case 0x2d:
            _FM_NOP();
            break; // AD
        case 0x2e:
            _FM_NOP();
            break; // AE
        case 0x2f:
            _FM_NOP();
            break; // AF

        case 0x30:
            comcmds._COM_B0();
            break; // B0
        case 0x31:
            _FM_NOP();
            break; // B1
        case 0x32:
            _FM_NOP();
            break; // B2
        case 0x33:
            _FM_NOP();
            break; // B3
        case 0x34:
            _FM_NOP();
            break; // B4
        case 0x35:
            _FM_NOP();
            break; // B5
        case 0x36:
            _FM_NOP();
            break; // B6
        case 0x37:
            _FM_NOP();
            break; // B7
        case 0x38:
            _FM_NOP();
            break; // B8
        case 0x39:
            _FM_NOP();
            break; // B9
        case 0x3a:
            _FM_NOP();
            break; // BA
        case 0x3b:
            _FM_NOP();
            break; // BB
        case 0x3c:
            _FM_NOP();
            break; // BC
        case 0x3d:
            _FM_NOP();
            break; // BD
        case 0x3e:
            comcmds._COM_BE();
            break; // BE ジャンプ
        case 0x3f:
            comcmds._COM_BF();
            break; // BF

        // Psg 系
        case 0x40:
            comcmds._COM_C0();
            break; // C0 ソフトウェアエンベロープ 1
        case 0x41:
            comcmds._COM_C1();
            break; // C1 ソフトウェアエンベロープ 2
        case 0x42:
            _FM_NOP();
            break; // C2
        case 0x43:
            comcmds._COM_C3();
            break; // C3 switch
        case 0x44:
            comcmds._COM_C4();
            break; // C4 env (num)
        case 0x45:
            comcmds._COM_C5();
            break; // C5 env (bank + num)
        case 0x46:
            _FM_NOP();
            break; // C6
        case 0x47:
            _FM_NOP();
            break; // C7
        case 0x48:
            _FM_NOP();
            break; // C8
        case 0x49:
            _FM_NOP();
            break; // C9
        case 0x4a:
            _FM_NOP();
            break; // CA
        case 0x4b:
            _FM_NOP();
            break; // CB
        case 0x4c:
            _FM_NOP();
            break; // CC
        case 0x4d:
            _FM_NOP();
            break; // CD
        case 0x4e:
            _FM_NOP();
            break; // CE
        case 0x4f:
            _FM_NOP();
            break; // CF

        // KEY 系
        case 0x50:
            comcmds._COM_D0();
            break; // D0 キートランスポーズ
        case 0x51:
            comcmds._COM_D1();
            break; // D1 相対キートランスポーズ
        case 0x52:
            _FM_NOP();
            break; // D2
        case 0x53:
            _FM_NOP();
            break; // D3
        case 0x54:
            _FM_NOP();
            break; // D4
        case 0x55:
            _FM_NOP();
            break; // D5
        case 0x56:
            _FM_NOP();
            break; // D6
        case 0x57:
            _FM_NOP();
            break; // D7
        case 0x58:
            comcmds._COM_D8();
            break; // D8 ディチューン
        case 0x59:
            comcmds._COM_D9();
            break; // D9 相対ディチューン
        case 0x5a:
            _FM_DA();
            break; // DA スロットディチューン
        case 0x5b:
            _FM_DB();
            break; // DB 相対スロットディチューン
        case 0x5c:
            _FM_NOP();
            break; // DC
        case 0x5d:
            _FM_NOP();
            break; // DD
        case 0x5e:
            _FM_NOP();
            break; // DE
        case 0x5f:
            _FM_NOP();
            break; // DF

        // LFO 系
        case 0x60:
            _FM_E0();
            break; // E0 hardware LFO
        case 0x61:
            _FM_E1();
            break; // E1 hardware LFO switch
        case 0x62:
            comcmds._COM_E2();
            break; // E2 pitch LFO
        case 0x63:
            comcmds._COM_E3();
            break; // E3 pitch LFO switch
        case 0x64:
            comcmds._COM_E4();
            break; // E4 pitch LFO delay
        case 0x65:
            _FM_NOP();
            break; // E5
        case 0x66:
            _FM_NOP();
            break; // E6
        case 0x67:
            comcmds._COM_E7();
            break; // E7 amp LFO
        case 0x68:
            _FM_E8();
            break; // E8 amp LFO switch
        case 0x69:
            comcmds._COM_E9();
            break; // E9 amp LFO delay
        case 0x6a:
            comcmds._COM_EA();
            break; // EA amp switch 2
        case 0x6b:
            comcmds._COM_EB();
            break; // EB
        case 0x6c:
            _FM_NOP();
            break; // EC
        case 0x6d:
            comcmds._COM_ED();
            break; // ED
        case 0x6e:
            _FM_EE();
            break; // EE LW type LFO
        case 0x6f:
            comcmds._COM_EF();
            break; // EF hardware LFO delay

        // システムコントール系
        case 0x70:
            _FM_F0();
            break; // F0 @
        case 0x71:
            comcmds._COM_D8();
            break; // F1
        case 0x72:
            _FM_F2();
            break; // F2 volume
        case 0x73:
            comcmds._COM_91();
            break; // F3
        case 0x74:
            _FM_F4();
            break; // F4 pan
        case 0x75:
            _FM_F5();
            break; // F5 ) volup
        case 0x76:
            _FM_F6();
            break; // F6 ( voldown
        case 0x77:
            _FM_F7();
            break; // F7 効果音モード切り替え
        case 0x78:
            _FM_F8();
            break; // F8 スロットマスク変更
        case 0x79:
            comcmds._COM_F9();
            break; // F9 永久ループポイントマーク
        case 0x7a:
            _FM_FA();
            break; // FA y command
        case 0x7b:
            comcmds._COM_FB();
            break; // FB リピート抜け出し
        case 0x7c:
            comcmds._COM_FC();
            break; // FC リピート開始
        case 0x7d:
            comcmds._COM_FD();
            break; // FD リピート終端
        case 0x7e:
            comcmds._COM_FE();
            break; // FE tempo
        case 0x7f:
            _FM_FF();
            break; // FF end of data
        }
    }

    /**
     */
    public void _FM_NOP() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
        _fm_keyoff2();
    }

    /**
     */
    public void _FM_82() {
        _fm_keyoff2();
    }

    /**
     * ピッチベンド
     * [$88] + [目標音程]b + [delay]b + [speed]b + [rate]W
     */
    public void _FM_88() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x80));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfd));
        reg.a4 = reg.a5 + W.p_pattern4;

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.key_trans));
        mm.write(reg.a5 + W.key2, (byte) reg.getD0_B());
        _get_fnum();
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a4 + W_L.mokuhyou, (short) reg.getD0_W());

        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD1_B());
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());

        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a4 + W_L.henka, (short) reg.getD0_W());
    }

    /**
    // ポルタメント
    //  [$89] + [switch]b + [先note]b + [元note]b + [step]b
     */
    public void _FM_89() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x80));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x02));
        reg.a4 = reg.a5 + W.p_pattern4;

        reg.setD0_B(mm.readByte(reg.a1++));

        //_FM_89_normal:
        reg.D0_L = 0;
        reg.D1_L = reg.D0_L;
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.key_trans));
        mm.write(reg.a5 + W.key2, (byte) reg.getD0_B());
        _get_fnum();
        reg.setD1_W(reg.getD0_W());

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.key_trans));
        _get_fnum();

        reg.D5_L = 0;
        reg.setD5_B((byte) (reg.getD1_W() >> 8));
        reg.setD5_B(reg.getD5_B() >> 3);
        reg.setD2_B((byte) (reg.getD0_W() >> 8));
        reg.setD2_B(reg.getD2_B() >> 3);
        reg.setD5_B(reg.getD5_B() - (int) (byte) reg.getD2_B());
        if (reg.getD5_B() != 0) {
            reg.setD5_W((short) (byte) reg.getD5_B());
            reg.D5_L = (short) reg.getD5_W() * 0x26a;
        }
        //_non_oct:
        reg.setD4_W(0x7ff);
        reg.setD1_W(reg.getD1_W() & reg.getD4_W());
        reg.setD0_W(reg.getD0_W() & reg.getD4_W());
        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.setD5_W(reg.getD5_W() + (int) (short) reg.getD1_W());

        reg.D2_L = 0;
        reg.setD2_B(mm.readByte(reg.a1 + 1));
        mm.write(reg.a4 + W_L.count, (byte) reg.getD2_B());

        reg.D5_L = (short) reg.getD5_W();
        reg.D5_L = (short) (reg.D5_L / (short) reg.getD2_W()) | (((short) (reg.D5_L % (short) reg.getD2_W())) << 16);
        mm.write(reg.a4 + W_L.henka, (short) reg.getD5_W());
        reg.D5_L = (reg.D5_L >> 16) | (reg.D5_L << 16);
        mm.write(reg.a4 + W_L.henka_work, (short) reg.getD5_W());
    }

    /**
     */
    public void _get_fnum() {
        Reg spReg = new Reg();
        spReg.D1_L = reg.D1_L;
        spReg.D2_L = reg.D2_L;
        spReg.D3_L = reg.D3_L;
        spReg.a0 = reg.a0;

        reg.setD1_W(0x800);
        reg.D2_L = 0;
        reg.D3_L = 12;
        while (reg.getD0_B() >= reg.getD3_B()) {
            reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD3_B());
            reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        }
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.a0 = Ab.dummyAddress; // _fnum_table;
        reg.setD2_W(reg.getD2_W() | _fnum_table[reg.getD0_W() / 2]);
        reg.setD0_W(reg.getD2_W());

        reg.D1_L = spReg.D1_L;
        reg.D2_L = spReg.D2_L;
        reg.D3_L = spReg.D3_L;
        reg.a0 = spReg.a0;
    }

    /**
    // RR cut設定
    //   [$92] + [switch]b
     */
    public void _FM_92() {
        mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) | 0x3));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() != 0) {
            if ((byte) reg.getD0_B() >= 0) {
                if (reg.getD0_B() - 2 == 0) return;
                mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) & 0xfd));
                return;
            }
            mm.write(reg.a5 + W.rct, mm.readByte(reg.a1++));
            mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) & 0xfe));
            return;
        }
        mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) & 0xfc));
        _FM_RR_ret();
    }

    /**
    // 擬似リバーブ
    //  switch = $80 = ON
    //    $81 = OFF
    //    $00 = + [volume]b
    //    $01 = + [volume]b + [pan]b
     */
    public void _FM_98() {
        comcmds._COM_98();

        if ((mm.readByte(reg.a5 + W.reverb) & 0x80) == 0) {
            _fm_keyoff();
        }
    }

    /**
    // 擬似エコー
     */
    public void _FM_99() {
        comcmds._COM_99();
    }

    /**
    // バンク&音色切り替え
     */
    public void _FM_A1() {
        mm.write(reg.a5 + W.bank, mm.readByte(reg.a1++));
        _FM_F0();
    }

    /**
    // 音量テーブル
     */
    public void _FM_A3() {
        comcmds._COM_A3();

        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) return;
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD4_W()));
        _FM_F2_v();
    }

    /**
    // slot detune
    //  [$DA] + [num]b + [slot1]W + [slot2]W + [slot3]W + [slot4]W
     */
    public void _FM_DA() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.setD1_B(mm.readByte(reg.a1++));

        int f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune1, (short) reg.getD0_W());
        }

        f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune2, (short) reg.getD0_W());
        }

        f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune3, (short) reg.getD0_W());
        }

        f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune4, (short) reg.getD0_W());
        }
    }

    /**
    // slot detune
    //  [$DB] + [num]b + [slot1]W + [slot2]W + [slot3]W + [slot4]W
     */
    public void _FM_DB() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.setD1_B(mm.readByte(reg.a1++));

        int f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune1, (short) (mm.readShort(reg.a5 + W.sdetune1) + (short) reg.getD0_W()));
        }

        f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune2, (short) (mm.readShort(reg.a5 + W.sdetune2) + (short) reg.getD0_W()));
        }

        f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune3, (short) (mm.readShort(reg.a5 + W.sdetune3) + (short) reg.getD0_W()));
        }

        f = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (f != 0) {
            reg.setD0_W(mm.readShort(reg.a1));
            reg.a1 += 2;
            mm.write(reg.a5 + W.sdetune4, (short) (mm.readShort(reg.a5 + W.sdetune4) + (short) reg.getD0_W()));
        }

    }

    /**
    // hardware LFO
    //
    // $E0,freq,ams,pms
     */
    public void _FM_E0() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x01));
        reg.setD1_B(mm.readByte(reg.a1++));

        reg.setD0_B(reg.getD0_B() + 8);
        reg.D1_L = 0x22;
        mndrv._OPN_WRITE3();

        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() << 4);
        reg.setD0_B(reg.getD0_B() | mm.readByte(reg.a1++));
        reg.D1_L = 0xc0;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a5 + W.pan_ampm));
        reg.setD0_B(reg.getD0_B() | reg.getD1_B());
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.reverb_pan_work, (byte) reg.getD0_B());
        reg.D1_L = 0xb4;
        mndrv._OPN_WRITE4();
    }

    /**
    // hardware LFO ON / OFF
    //
    // $E1,[on / off]
     */
    public void _FM_E1() {
        reg.a4 = reg.a5 + W.v_pattern4;
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _FM_E1_off();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _FM_E1_normal();
            return;
        }

        if (reg.getD0_B() - 0xff == 0) {
            _FM_E1_time();
            return;
        }
        if (reg.getD0_B() - 0x82 == 0) {
            mm.write(reg.a4 + W_L.flag, (short) 0);
            return;
        }

        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            mm.write(reg.a4 + W_L.flag, (short) 0x8002);
            _FM_E1_normal();
            return;
        }
        mm.write(reg.a4 + W_L.flag, (short) 0x8001);
        _FM_E1_normal();
    }

    public void _FM_E1_normal() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x01));
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));

        if ((mm.readByte(reg.a5 + W.flag2) & 0x08) != 0) {
            _FM_E1_LW();
            return;
        }
        reg.setD0_B(mm.readByte(reg.a5 + W.pan_ampm));
        reg.D1_L = 0xb4;
        mndrv._OPN_WRITE4();
    }

    public void _FM_E1_off() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xfe));
        _FM_E1_LW();
    }

    public void _FM_E1_LW() {
        reg.D0_L = 0xc0;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.pan_ampm));
        reg.D1_L = 0xb4;
        mndrv._OPN_WRITE4();
    }

    public void _FM_E1_time() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xf7));
            return;
        }

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x01));
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) | 0x08));

        reg.a4 = reg.a5 + W.v_pattern4;
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD0_B());
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
    }

    /**
    // 音量 LFO on /off
    //
    // $E8,num,switch
     */
    public void _FM_E8() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        // pea _COM_E8
        _FM_F2_init();
        comcmds._COM_E8();
    }

    /**
    // LW type LFO
    //  [$EE] + [freq]b + [speed]b
     */
    public void _FM_EE() {
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x08));
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x01));

        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + 8);
        reg.D1_L = 0x22;
        mndrv._OPN_WRITE3();
        reg.D0_L = 0xc0;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.pan_ampm));
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD0_B());
        reg.D1_L = 0xB4;
        mndrv._OPN_WRITE4();

        reg.a4 = reg.a5 + W.v_pattern4;
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD0_B());
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
    }

    /**
    // 音色設定
     */
    public void _FM_F0() {
        if (mm.readByte(reg.a5 + W.reverb) < 0) {
            _fm_keyoff();
        }
        reg.setD5_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.program2, (byte) reg.getD5_B());

        _fm_echo_tone_change();
    }

    public void _fm_echo_tone_change() {
        mm.write(reg.a5 + W.program, (byte) reg.getD5_B());
        reg.setD3_B(mm.readByte(reg.a5 + W.dev));
        reg.D0_L = mm.readInt(reg.a6 + Dw.TONE_PTR);
        if (reg.D0_L == 0) return;
        reg.a2 = reg.D0_L;
        reg.a2 += 6;
        reg.setD4_W(mm.readShort(reg.a6 + Dw.VOICENUM));
        reg.setD1_B(mm.readByte(reg.a5 + W.bank));
        _voice_ana_loop();
    }

    public void _voice_ana_loop() {
// L:
        while (true) {
            if (reg.getD1_B() - mm.readByte(reg.a2 + 2) == 0) {
                if (reg.getD5_B() - mm.readByte(reg.a2 + 3) == 0) {
                    _voice_ana_set();
                    return;
                }
            }
            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() != 0) {
                reg.setD0_W(mm.readShort(reg.a2));
                reg.a2 = reg.a2 + (int) (short) reg.getD0_W();
                continue; // break L; // _voice_ana_loop();
            }
            break;
        }
    }

    public void _voice_ana_set() {
        if (mm.readByte(reg.a5 + W.ch3) != 0) {
            _voice_ana_set_ch3();
            return;
        }

        reg.D1_L = 0x8c;
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD3_B());
        reg.D0_L = 0xff;
        reg.D2_L = 4 - 1;
        do {
            mndrv._OPN_WRITE2();
            reg.setD1_B(reg.getD1_B() - 4);
        } while (reg.decAfterD2_W() != 0);

        reg.D1_L = 0x4c;
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD3_B());
        reg.D0_L = 0x7f;
        reg.D2_L = 4 - 1;
        do {
            mndrv._OPN_WRITE2();
            reg.setD1_B(reg.getD1_B() - 4);
        } while (reg.decAfterD2_W() != 0);

        reg.a0 = reg.a2 + 0x1a;
        reg.a4 = reg.a5 + W.tone_rr;
        mm.write(reg.a4++, mm.readByte(reg.a0++));
        mm.write(reg.a4++, mm.readByte(reg.a0++));
        mm.write(reg.a4++, mm.readByte(reg.a0++));
        mm.write(reg.a4++, mm.readByte(reg.a0++));

        reg.setD2_W(mm.readShort(reg.a2));

        reg.setD0_B(mm.readByte(reg.a2 + 4));
        mm.write(reg.a5 + W.fbcon, (byte) reg.getD0_B());
        reg.D1_L = 0xb0;
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD3_B());
        mndrv._OPN_WRITE2();
        reg.setD0_W(reg.getD0_W() & 7);
        reg.a0 = Ab.dummyAddress; // _fm_vol_con_pat;
        reg.setD5_B(_fm_vol_con_pat[reg.getD0_W()]); // mm.readByte(Reg.a0 + reg.getD0_W());

        if ((mm.readByte(reg.a5 + W.flag2) & 0x20) == 0) {
            reg.setD0_B(mm.readByte(reg.a2 + 5));
            reg.setD0_W(reg.getD0_W() << 4);
            mm.write(reg.a5 + W.smask, (byte) reg.getD0_B());
        }

        reg.D1_L = 0x30;
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD3_B());
        reg.a2 += 6;
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_W(reg.getD1_W() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_W(reg.getD1_W() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_W(reg.getD1_W() + 4);
        reg.setD0_B(mm.readByte(reg.a2++));
        mndrv._OPN_WRITE2();
        reg.setD1_W(reg.getD1_W() + 4);

        mm.write(reg.a5 + W.voiceptr, reg.a2);
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD0_B() < 0) {
            reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0x7f;
            }
        } else {
            reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0x00;
            }
        }
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a6 + Dw.MASTER_VOL_FM));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0x7f;
        }

        int sp = reg.getD2_W();

        mm.write(reg.a5 + W.vol2, (byte) reg.getD4_B());
        reg.D2_L = 4 - 1;
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD5_B() & 1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f != 0) {
                reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                if ((byte) reg.getD0_B() < 0) {
                    reg.D0_L = 0x7f;
                }
            }
            mndrv._OPN_WRITE2();
            reg.setD1_B(reg.getD1_B() + 4);
        } while (reg.decAfterD2_W() != 0);

        reg.D2_L = 16 - 1;
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            mndrv._OPN_WRITE2();
            reg.setD1_B(reg.getD1_B() + 4);
        } while (reg.decAfterD2_W() != 0); // D2_B--

        reg.setD2_W(sp);

        if (reg.getD2_W() >= 0x21) {
            reg.setD1_W(reg.getD1_W() + 4);
            reg.setD0_B(mm.readByte(reg.a2++));
            mm.write(reg.a4++, (byte) reg.getD0_B());
            mndrv._OPN_WRITE2();
            reg.setD1_W(reg.getD1_W() + 4);
            reg.setD0_B(mm.readByte(reg.a2++));
            mm.write(reg.a4++, (byte) reg.getD0_B());
            mndrv._OPN_WRITE2();
            reg.setD1_W(reg.getD1_W() + 4);
            reg.setD0_B(mm.readByte(reg.a2++));
            mm.write(reg.a4++, (byte) reg.getD0_B());
            mndrv._OPN_WRITE2();
            reg.setD1_W(reg.getD1_W() + 4);
            reg.setD0_B(mm.readByte(reg.a2++));
            mm.write(reg.a4++, (byte) reg.getD0_B());
            mndrv._OPN_WRITE2();
        }
    }

    public void _voice_ana_set_ch3() {
        reg.setD4_B(mm.readByte(reg.a5 + W.ch3tl));
        reg.setD5_B(reg.getD4_B());
        reg.D1_L = 0x82;
        reg.D0_L = 0xff;
        reg.D2_L = 4 - 1;
        int f;
        do {
            f = reg.getD5_B() & 1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f != 0) {
                mndrv._OPN_WRITE2();
            }
            reg.setD1_B(reg.getD1_B() + 4);
        } while (reg.decAfterD2_W() != 0);

        reg.setD5_B(reg.getD4_B());
        reg.D1_L = 0x42;
        reg.D0_L = 0x7f;
        reg.D2_L = 4 - 1;
        do {
            f = reg.getD5_B() & 1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f != 0) {
                mndrv._OPN_WRITE2();
            }
            reg.setD1_B(reg.getD1_B() + 4);
        } while (reg.decAfterD2_W() != 0);

        reg.a0 = reg.a2 + 0x1a;
        reg.a4 = reg.a5 + W.tone_rr;
        mm.write(reg.a4++, mm.readByte(reg.a0++));
        mm.write(reg.a4++, mm.readByte(reg.a0++));
        mm.write(reg.a4++, mm.readByte(reg.a0++));
        mm.write(reg.a4++, mm.readByte(reg.a0++));

        reg.setD3_W(mm.readShort(reg.a2));

        reg.setD0_B(mm.readByte(reg.a2 + 4));
        mm.write(reg.a5 + W.fbcon, (byte) reg.getD0_B());
        reg.D1_L = 0xb2;
        mndrv._OPN_WRITE2();
        reg.setD0_W(reg.getD0_W() & 7);
        reg.a0 = Ab.dummyAddress; // _fm_vol_con_pat;
        reg.setD5_B(_fm_vol_con_pat[reg.getD0_W()]); // mm.readByte(Reg.a0 + reg.getD0_W());

        reg.a2 += 6;
        reg.setD2_B(mm.readByte(reg.a5 + W.ch3tl));

        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x32;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x36;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x3a;
            mndrv._OPN_WRITE2();
        }
        reg.setD0_B(mm.readByte(reg.a2++));
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) {
            reg.D1_L = 0x3e;
            mndrv._OPN_WRITE2();
        }

        mm.write(reg.a5 + W.voiceptr, reg.a2);

        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD0_B() < 0) {
            reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0x7f;
            }
        } else {
            reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0x00;
            }
        }
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a6 + Dw.MASTER_VOL_FM));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0x7f;
        }

        int sp = reg.getD3_W();

        mm.write(reg.a5 + W.vol2, (byte) reg.getD4_B());
        reg.D1_L = 0x42;
        reg.D2_L = 4 - 1;
        reg.setD3_B(mm.readByte(reg.a5 + W.ch3tl));
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            f = reg.getD3_B() & 1;
            reg.setD3_B(reg.getD3_B() >> 1);
            if (f == 0) {
                reg.setD5_B(reg.getD5_B() >> 1);
            } else {
                f = reg.getD5_B() & 1;
                reg.setD5_B(reg.getD5_B() >> 1);
                if (f != 0) {
                    reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                    if ((byte) reg.getD0_B() < 0) {
                        reg.D0_L = 0x7f;
                    }
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_B(reg.getD1_B() + 4);
        } while (reg.decAfterD2_W() != 0);

        reg.setD3_W(sp);

        reg.D5_L = 4 - 1;
        if (reg.getD3_W() >= 0x21) {
            reg.D5_L = 5 - 1;
        }
        reg.setD3_B(mm.readByte(reg.a5 + W.ch3tl));
        do {
            reg.setD4_B(reg.getD3_B());
            reg.D2_L = 4 - 1;
            do {
                reg.setD0_B(mm.readByte(reg.a2++));
                f = reg.getD4_B() & 1;
                reg.setD4_B(reg.getD4_B() >> 1);
                if (f != 0) {
                    mndrv._OPN_WRITE2();
                }
                reg.setD1_B(reg.getD1_B() + 4);
            } while (reg.decAfterD2_W() != 0);
        } while (reg.decAfterD5_W() != 0);
    }

    public static final byte[] _fm_vol_con_pat = new byte[] {
            0x08, 0x08, 0x08, 0x08, 0x0C, 0x0E, 0x0E, 0x0F
    };

    /**
    // volume 設定
    //
    // FM COMMAND
    // $F2 [ volume ]
    //   [$F2] + [$00～$15]b    vコマンド
    //   [$F2] + [$80～$FF]b    @vコマンド（ビット7無効）
     */
    public void _FM_F2() {
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) & 0xef));
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() < 0) {
            reg.setD4_B(reg.getD4_B() & 0x7f);
            _FM_F2_v();
            return;
        }
        if (mm.readByte(reg.a5 + W.volmode) != 0) {
            mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) | 0x10));
        }
        if (reg.getD4_B() >= mm.readByte(reg.a5 + W.volcount)) {
            reg.setD4_B(mm.readByte(reg.a5 + W.volcount));
            reg.setD4_B(reg.getD4_B() - 1);
        }
        mm.write(reg.a5 + W.volume, (byte) reg.getD4_B());
        reg.a2 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD4_W()));
        _FM_F2_v();
    }

    public void _FM_F2_v() {
        mm.write(reg.a5 + W.vol, (byte) reg.getD4_B());
        _FM_F2_softenv();
    }

    public void _FM_F2_softenv() {
        reg.setD0_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD0_B() < 0) {
            reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0x7f;
            }
        } else {
            reg.setD4_B(reg.getD4_B() - (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0;
            }
        }
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a6 + Dw.MASTER_VOL_FM));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0x7f;
        }
        _FM_F2_set();
    }

    public void _FM_F2_set() {
        reg.D0_L = 0;

        mm.write(reg.a5 + W.vol2, (byte) reg.getD4_B());
        reg.D1_L = mm.readInt(reg.a5 + W.voiceptr);
        if (reg.D1_L == 0) return;
        reg.a2 = reg.D1_L;

        reg.D0_L = 7;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.fbcon));
        reg.setD3_B(_fm_vol_con_pat[reg.getD0_W()]); // mm.readByte(_fm_vol_con_pat);

        reg.D1_L = 0x40;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));

        reg.D2_L = 4 - 1;
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD5_B() != 0) {
            _FM_F2_SL_CH3();
            return;
        }
        //_FM_F2_set_loop:
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD3_B() & 1;
            reg.setD3_B(reg.getD3_B() >> 1);
            if (f != 0) {
                reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                if ((byte) reg.getD0_B() < 0) {
                    reg.D0_L = 0x7f;
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    public void _FM_F2_SL_CH3() {
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3tl));
        //_FM_F2_SL_loop:
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD5_B() & 1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f == 0) {
                reg.setD3_B(reg.getD3_B() >> 1);
            } else {
                f = reg.getD3_B() & 1;
                reg.setD3_B(reg.getD3_B() >> 1);
                if (f != 0) {
                    reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                    if ((byte) reg.getD0_B() < 0) {
                        reg.D0_L = 0x7f;
                    }
                    mndrv._OPN_WRITE2();
                }
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    /**
    // pan 設定
    //   [$F4] + [DATA]b
     */
    public void _FM_F4() {
        reg.D0_L = 0x3f;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.pan_ampm));
        reg.setD0_B(reg.getD0_B() | mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.reverb_pan_work, (byte) reg.getD0_B());
        reg.D1_L = 0xb4;
        mndrv._OPN_WRITE4();
    }

    /**
    // volup
    //   [$F5] + [DATA]b
     */
    public void _FM_F5() {
        if (mm.readByte(reg.a5 + W.volmode) == 0) {
            _FM_F5_normal();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _FM_F5_normal();
            return;
        }

        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a1++));
        if (reg.getD4_B() >= mm.readByte(reg.a5 + W.volcount)) {
            reg.setD4_B(mm.readByte(reg.a5 + W.volcount));
            reg.setD4_B(reg.getD4_B() - 1);
        }
        mm.write(reg.a5 + W.volume, (byte) reg.getD4_B());
        reg.a2 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD4_W()));
        _FM_F2_v();
    }

    public void _FM_F5_normal() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD4_B(reg.getD4_B() - mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        _FM_F2_v();
    }

    /**
    // voldown
    //   [$F6] + [DATA]b
     */
    public void _FM_F6() {
        if (mm.readByte(reg.a5 + W.volmode) == 0) {
            _FM_F6_normal();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _FM_F6_normal();
            return;
        }

        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));
        reg.setD4_B(reg.getD4_B() - mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        mm.write(reg.a5 + W.volume, (byte) reg.getD4_B());
        reg.a2 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD4_W()));
        _FM_F2_v();
    }

    public void _FM_F6_normal() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0x7f;
        }
        _FM_F2_v();
    }

    /**
    // 効果音モード設定
    //   [$F7] + [switch]b
     */
    public void _FM_F7() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() - (-1) == 0) {
            _FM_F7_OFF();
            return;
        }

        mm.write(reg.a5 + W.ch3, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.ch3mode, 0xff);

        reg.D1_L = 0;
        int f = reg.getD0_B() & 0x80;
        reg.setD0_B(reg.getD0_B() << 1);
        if (f != 0) {
            reg.setD1_W(reg.getD1_W() + 8);
        }
        f = reg.getD0_B() & 0x80;
        reg.setD0_B(reg.getD0_B() << 1);
        if (f != 0) {
            reg.setD1_W(reg.getD1_W() + 2);
        }
        f = reg.getD0_B() & 0x80;
        reg.setD0_B(reg.getD0_B() << 1);
        if (f != 0) {
            reg.setD1_W(reg.getD1_W() + 4);
        }
        f = reg.getD0_B() & 0x80;
        reg.setD0_B(reg.getD0_B() << 1);
        if (f != 0) {
            reg.setD1_W(reg.getD1_W() + 1);
        }
        mm.write(reg.a5 + W.ch3tl, (byte) reg.getD1_B());

        reg.D1_L = 0x27;
        if (mm.readByte(reg.a5 + W.ch) - 2 != 0) {
            _FM_F7_on_slave();
            return;
        }
        mm.write(reg.a6 + Dw.CH3MODEM, 0x40);
        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x20) != 0) {
            _FM_F7_OPMTIMER();
            return;
        }
        reg.D0_L = 0x4f;
        mndrv._OPN_WRITE3();
    }

    public void _FM_F7_on_slave() {
        mm.write(reg.a6 + Dw.CH3MODES, 0x40);
        _FM_F7_OPMTIMER();
    }

    public void _FM_F7_OPMTIMER() {
        reg.D0_L = 0x40;
        mndrv._OPN_WRITE3();
    }

    public void _FM_F7_OFF() {
        mm.write(reg.a5 + W.ch3mode, 0x00);
        mm.write(reg.a5 + W.ch3tl, (byte) reg.getD0_B());
        reg.D1_L = 0x27;
        if (mm.readByte(reg.a5 + W.ch) - 2 != 0) {
            _FM_F7_off_slave();
            return;
        }
        mm.write(reg.a6 + Dw.CH3MODEM, 0);
        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x20) == 0) {
            _FM_F7_exit();
            return;
        }
        reg.D0_L = 0xf;
        mndrv._OPN_WRITE3();
    }

    public void _FM_F7_off_slave() {
        mm.write(reg.a6 + Dw.CH3MODES, 0);
        _FM_F7_exit();
    }

    public void _FM_F7_exit() {
        reg.D0_L = 0;
        mndrv._OPN_WRITE3();
    }

    /**
    // スロットマスク設定
    //   [$F8] + [data]b
     */
    public void _FM_F8() {
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x20));
        mm.write(reg.a5 + W.smask, mm.readByte(reg.a1++));
    }

    /**
    // y command
    //   [$FA] + [REG] + [DAT]a
     */
    public void _FM_FA() {
        reg.D2_L = 0x26;
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD1_B() - reg.getD2_B() != 0) {
            mndrv._OPN_WRITE2();
            return;
        }

        mm.write(reg.a6 + Dw.TEMPO, (byte) reg.getD0_B());
        mndrv._OPN_WRITE2();
    }

    /**
     */
    public void _FM_FF() {
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));

        reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a0 = reg.a6 + Dw.TRACKWORKADR;

// L1:
        boolean L3 = false;
        do {
            if ((mm.readByte(reg.a0 + W.flag2) & 0x01) != 0) { // break L3;
                L3 = true;
                break;
            }
            reg.a0 = reg.a0 + W._track_work_size;
            reg.setD0_W(reg.getD0_W() - 1);
        } while (reg.getD0_W() != 0); // break L1;

        if (!L3) {
            mm.write(reg.a6 + Dw.LOOP_COUNTER, (short) (mm.readShort(reg.a6 + Dw.LOOP_COUNTER) + 1));
            reg.setD1_W(mm.readShort(reg.a6 + Dw.LOOP_COUNTER));
            if (reg.getD1_W() - 0xffff == 0) {
                reg.D1_L = 0;
                mm.write(reg.a6 + Dw.LOOP_COUNTER, (short) reg.getD1_W());
            }
            reg.D0_L = 1;
            mndrv.SUBEVENT();

            reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
            reg.a0 = reg.a6 + Dw.TRACKWORKADR;
            do {
                if (mm.readByte(reg.a0 + W.flag) < 0) {
                    mm.write(reg.a0 + W.flag2, (byte) (mm.readByte(reg.a0 + W.flag2) | 0x01));
                }
                reg.a0 = reg.a0 + W._track_work_size;
                reg.setD0_W(reg.getD0_W() - 1);
            } while (reg.getD0_W() != 0);
        }
// L3:
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        if (reg.getD0_W() != 0) {
            if (reg.getD0_W() - 0xffff != 0) {
                reg.a1 = reg.a1 + (int) (short) reg.getD0_W();
                return;
            }
        } else {
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));
            mm.write(reg.a5 + W.lfo, 0x00);
            mm.write(reg.a5 + W.weffect, 0x00);
            _fm_keyoff();
            comcmds._all_end_check();
            return;
        }
        reg.a1 = mm.readInt(reg.a5 + W.loop);
    }

    /**
     */
    public void _ch_fm_lfo_job() {
        comwave._ch_effect();
        //_ch_fm_lfo:
        if ((mm.readByte(reg.a5 + W.effect) & 0x20) != 0) {
            reg.a4 = reg.a5 + W.ww_pattern1;
            _ch_fm_ww();
        }
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if (reg.getD0_B() == 0) return;
        mm.write(reg.a5 + W.addkeycode, (short) 0);
        mm.write(reg.a5 + W.addvolume, (short) 0);

        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            _ch_fm_HLFO();
        }
        //_ch_fm_p1:
        if (mm.readByte(reg.a6 + Dw.LFO_FLAG) < 0) {
            _ch_fm_lfo_extend();
            return;
        }

        reg.D1_L = 7;
        reg.setD1_B(reg.getD1_B() & reg.getD0_B());
        if (reg.getD1_B() != 0) {
            int sp = reg.getD0_W();
            reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
            switch (reg.getD1_W() / 2) {
            case 1:
                _ch_fm_plfo_1();
                break;
            case 2:
                _ch_fm_plfo_2();
                break;
            case 3:
                _ch_fm_plfo_3();
                break;
            case 4:
                _ch_fm_plfo_4();
                break;
            case 5:
                _ch_fm_plfo_5();
                break;
            case 6:
                _ch_fm_plfo_6();
                break;
            case 7:
                _ch_fm_plfo_7();
                break;
            }
            reg.setD0_W(sp);
        }

        reg.setD0_B(reg.getD0_B() >> 3);
        reg.setD0_W(reg.getD0_W() & 7);
        if (reg.getD0_W() != 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            switch (reg.getD0_W()) {
            case 1:
                _ch_fm_alfo_1();
                break;
            case 2:
                _ch_fm_alfo_2();
                break;
            case 3:
                _ch_fm_alfo_3();
                break;
            case 4:
                _ch_fm_alfo_4();
                break;
            case 5:
                _ch_fm_alfo_5();
                break;
            case 6:
                _ch_fm_alfo_6();
                break;
            case 7:
                _ch_fm_alfo_7();
                break;
            }
        }

        //_ch_fm_lfo_end:
        reg.setD0_W(mm.readShort(reg.a5 + W.addvolume));
        if (reg.getD0_W() - mm.readShort(reg.a5 + W.addvolume2) != 0) {
            mm.write(reg.a5 + W.addvolume2, (short) reg.getD0_W());
            _ch_fm_v_calc();
        }
    }

    /** */
    public void _ch_fm_plfo_1() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_p_common();
    }

    public void _ch_fm_plfo_2() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_p_common();
    }

    public void _ch_fm_plfo_3() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_p_common();
    }

    public void _ch_fm_plfo_4() {
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_p_common();
    }

    public void _ch_fm_plfo_5() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_p_common();
    }

    public void _ch_fm_plfo_6() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_p_common();
    }

    public void _ch_fm_plfo_7() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_p_common();
    }

    /** */
    public void _ch_fm_p_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            _ch_fm_p_wavememory();
            return;
        }

        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _ch_fm_p_com_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_fm_p_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_fm_p_com_exec();
        }
    }

    public void _ch_fm_p_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_fm_p_com_exec();
        }
    }

    public void _ch_fm_p_com_exec() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_W() / 2) {
        case 1:
            _ch_fm_p_0();
            break;
        case 2:
            _ch_fm_p_1();
            break;
        case 3:
            _ch_fm_p_2();
            break;
        case 4:
            _ch_fm_p_3();
            break;
        case 5:
            _ch_fm_p_4();
            break;
        case 6:
            _ch_fm_p_5();
            break;
        case 7:
            _ch_fm_p_6();
            break;
        case 8:
            _ch_fm_p_7();
            break;
        }
    }

    /** */
    public void _ch_fm_alfo_1() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_fm_a_common();
    }

    public void _ch_fm_alfo_2() {
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_fm_a_common();
    }

    public void _ch_fm_alfo_3() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_fm_a_common();
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_fm_a_common();
    }

    public void _ch_fm_alfo_4() {
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_fm_a_common();
    }

    public void _ch_fm_alfo_5() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_fm_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_fm_a_common();
    }

    public void _ch_fm_alfo_6() {
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_fm_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_fm_a_common();
    }

    public void _ch_fm_alfo_7() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_fm_a_common();
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_fm_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_fm_a_common();
    }

    /** */
    public void _ch_fm_a_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            comwave._com_wavememory();
            mm.write(reg.a5 + W.addvolume, (short) reg.getD0_W());
            return;
        }
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _ch_fm_v_com_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_fm_v_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_fm_v_com_exec();
        }
    }

    public void _ch_fm_v_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_fm_v_com_exec();
        }
    }

    public void _ch_fm_v_com_exec() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_W() / 2) {
        case 1:
            comlfo.comLfoSaw();
            break;
        case 2:
            comlfo.comLfoPortament();
            break;
        case 3:
            comlfo.comLfoTriangle();
            break;
        }
        mm.write(reg.a5 + W.addvolume, (short) reg.getD1_W());
    }

    /** */
    public void _ch_fm_v_calc() {
        reg.setD3_B(mm.readByte(reg.a3 + W_W.slot));
        if ((byte) reg.getD3_B() < 0) {
            _fm_v_calc_slot();
            return;
        }

        if ((short) reg.getD0_W() < 0) {
            _ch_fm_v_minus();
            return;
        }

        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD4_W(reg.getD4_W() + (int) (short) reg.getD0_W());
        if ((short) reg.getD4_W() < 0) {
            reg.D4_L = 0x7f;
        }
        _FM_F2_lfo();
    }

    public void _ch_fm_v_minus() {
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD4_W(reg.getD4_W() + (int) (short) reg.getD0_W());
        if ((short) reg.getD4_W() < 0) {
            reg.D4_L = 0x00;
        }
        _FM_F2_lfo();
    }

    /** */
    public void _FM_F2_lfo() {
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a6 + Dw.MASTER_VOL_FM));
        if ((byte) reg.getD4_B() >= 0) {
            _FM_F2_set();
            return;
        }
        reg.D4_L = 0x7f;
        mm.write(reg.a5 + W.vol2, (byte) reg.getD4_B());
        reg.D0_L = mm.readInt(reg.a5 + W.voiceptr);
        if (reg.D0_L == 0) return;

        reg.setD3_B(mm.readByte(reg.a3 + W_W.slot));
        if ((byte) reg.getD3_B() >= 0) {
            reg.D3_L = 7;
            reg.setD3_B(reg.getD3_B() & mm.readByte(reg.a5 + W.fbcon));
            reg.a2 = Ab.dummyAddress; // _fm_vol_con_pat;
            reg.setD3_B(_fm_vol_con_pat[reg.getD3_W()]); // mm.readByte(Reg.a2 + reg.getD3_W());
        }

        reg.a2 = reg.D0_L;

        reg.D1_L = 0x40;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        reg.D2_L = 4 - 1;
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD5_B() != 0) {
            _FM_F2_lfo_loop_ch3();
            return;
        }
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD3_B() & 1;
            reg.setD3_B(reg.getD3_B() >> 1);
            if (f != 0) {
                reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                if ((byte) reg.getD0_B() < 0) {
                    reg.D0_L = 0x7f;
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    public void _FM_F2_lfo_loop_ch3() {
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD5_B() & 1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f == 0) {
                reg.setD3_B(reg.getD3_B() >> 1);
            } else {
                f = reg.getD3_B() & 1;
                reg.setD3_B(reg.getD3_B() >> 1);
                if (f != 0) {
                    reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                    if ((byte) reg.getD0_B() < 0) {
                        reg.D0_L = 0x7f;
                    }
                    mndrv._OPN_WRITE2();
                }
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    /** */
    public void _fm_v_calc_slot() {
        reg.setD4_W(reg.getD0_W());
        reg.D0_L = mm.readInt(reg.a5 + W.voiceptr);
        if (reg.D0_L == 0) return;

        reg.a2 = reg.D0_L;
        reg.D1_L = 0x40;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        reg.D2_L = 4 - 1;
        reg.D0_L = 0;
        if ((short) reg.getD4_W() < 0) {
            _fm_v_calc_slot_minus();
            return;
        }
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD3_B() & 1;
            reg.setD3_B(reg.getD3_B() >> 1);
            if (f != 0) {
                reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD4_W());
                if ((short) reg.getD0_W() < 0) {
                    reg.D0_L = 0x7f;
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    public void _fm_v_calc_slot_minus() {
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD3_B() & 1;
            reg.setD3_B(reg.getD3_B() >> 1);
            if (f != 0) {
                reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD4_W());
                if ((short) reg.getD0_W() < 0) {
                    reg.D0_L = 0x0;
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    /**
    // extended LFO
     */
    public void _ch_fm_lfo_extend() {
        reg.D1_L = 7;
        reg.setD1_B(reg.getD1_B() & reg.getD0_B());
        if (reg.getD1_B() != 0) {
            int sp = reg.getD0_W();
            reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
            switch (reg.getD1_W() / 2) {
            case 1:
                _ch_fm_explfo_1();
                break;
            case 2:
                _ch_fm_explfo_2();
                break;
            case 3:
                _ch_fm_explfo_3();
                break;
            case 4:
                _ch_fm_explfo_4();
                break;
            case 5:
                _ch_fm_explfo_5();
                break;
            case 6:
                _ch_fm_explfo_6();
                break;
            case 7:
                _ch_fm_explfo_7();
                break;
            }
            reg.setD0_W(sp);
        }

        reg.setD0_B(reg.getD0_B() >> 3);
        reg.setD0_W(reg.getD0_W() & 7);
        if (reg.getD0_W() != 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            reg.a4 = Ab.dummyAddress; // _ch_fm_alfo_table;
            reg.setD0_W(mm.readShort(reg.a4 + (short) reg.getD0_W()));
            switch (reg.getD0_W() / 2) {
            case 1:
                _ch_fm_alfo_1();
                break;
            case 2:
                _ch_fm_alfo_2();
                break;
            case 3:
                _ch_fm_alfo_3();
                break;
            case 4:
                _ch_fm_alfo_4();
                break;
            case 5:
                _ch_fm_alfo_5();
                break;
            case 6:
                _ch_fm_alfo_6();
                break;
            case 7:
                _ch_fm_alfo_7();
                break;
            }
        }
        reg.setD0_W(mm.readShort(reg.a5 + W.addvolume));
        if (reg.getD0_W() - mm.readShort(reg.a5 + W.addvolume2) != 0) {
            mm.write(reg.a5 + W.addvolume2, (short) reg.getD0_W());
            _ch_fm_v_calc();
        }
        reg.setD2_W(mm.readShort(reg.a5 + W.addkeycode));
        if (reg.getD2_W() - mm.readShort(reg.a5 + W.addkeycode2) == 0) return;
        mm.write(reg.a5 + W.addkeycode2, (short) reg.getD2_W());

        reg.setD4_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD4_B() != 0) {
            _ch_fm_lfo_ex_slot4();
            return;
        }
        if (mm.readByte(reg.a5 + W.ch3mode) != 0) return;

        // pea _set_fnum2(pc)
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        _ex_slot_calc();
        _set_fnum2();
    }

    public void _ch_fm_lfo_ex_slot4() {
        int f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s4));
            reg.setD2_W(reg.getD2_W() - mm.readShort(reg.a5 + W.sdetune4));
            _ex_slot_calc();
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.sdetune4));
            mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
            reg.setD2_W(reg.getD2_W() - mm.readShort(reg.a5 + W.sdetune3));
            _ex_slot_calc();
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.sdetune3));
            mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
            reg.setD2_W(reg.getD2_W() - mm.readShort(reg.a5 + W.sdetune2));
            _ex_slot_calc();
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.sdetune2));
            mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
            reg.setD2_W(reg.getD2_W() - mm.readShort(reg.a5 + W.sdetune1));
            _ex_slot_calc();
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.sdetune1));
            mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());
        }

        _set_fnum2();
    }

    //_ch_fm_lfo_exit:
    // rts

    public void _ex_slot_calc() {
        if ((mm.readByte(reg.a6 + Dw.LFO_FLAG) & 0x40) != 0) {
            _ex_slot_calc2();
            return;
        }

        reg.setD1_W(mm.readShort(reg.a5 + W.addkeycode));
        if ((short) reg.getD1_W() < 0) {
            _ex_slot_calc_minus();
            return;
        }
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if (reg.getD2_W() >= 0x4000) {
            reg.setD2_W(0x3fff);
        }
    }

    public void _ex_slot_calc_minus() {
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if ((short) reg.getD2_W() < 0) {
            reg.setD2_W(0x26a);
        }
    }

    /** */
    public void _ch_fm_explfo_1() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_exp_common();
    }

    public void _ch_fm_explfo_2() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_exp_common();
    }

    public void _ch_fm_explfo_3() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_exp_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_exp_common();
    }

    public void _ch_fm_explfo_4() {
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_exp_common();
    }

    public void _ch_fm_explfo_5() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_exp_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_exp_common();
    }

    public void _ch_fm_explfo_6() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_exp_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_exp_common();
    }

    public void _ch_fm_explfo_7() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fm_exp_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fm_exp_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fm_exp_common();
    }

    /** */
    public void _ex_slot_calc2() {
        int sp = reg.D4_L;
        try {
            reg.setD1_W(mm.readShort(reg.a5 + W.addkeycode));
            if ((short) reg.getD1_W() >= 0) { // break _ex_slot_calc2_minus;

                reg.setD3_W(reg.getD2_W());
                reg.setD3_W(reg.getD3_W() & 0x3800);
                reg.setD2_W(reg.getD2_W() & 0x7ff);
                reg.setD2_W(reg.getD2_W() - mm.readShort(reg.a5 + W.detune));
                reg.setD4_W(0x26a);
                reg.setD2_W(reg.getD2_W() - (int) (short) reg.getD4_W());
                reg.setD5_W(0x800);
                reg.setD0_W(0x4000);

                L1a:
                if (reg.getD1_W() >= reg.getD4_W()) {
                    reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD4_W());
                    reg.setD3_W(reg.getD3_W() + (int) (short) reg.getD5_W());
                    if (reg.getD3_W() < reg.getD0_W()) break L1a; // TODO
                    reg.setD2_W(0x3fff);
                    return; // break _ex_slot_calc2_exit;
                }
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
                if (reg.getD2_W() >= reg.getD4_W()) {
                    reg.setD2_W(reg.getD2_W() - (int) (short) reg.getD4_W());
                    reg.setD3_W(reg.getD3_W() + (int) (short) reg.getD5_W());
                    if (reg.getD3_W() >= reg.getD0_W()) {
                        reg.setD2_W(0x3fff);
                        return; // break _ex_slot_calc2_exit;
                    }
                }
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD4_W());
                reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.detune));
                reg.setD2_W(reg.getD2_W() | reg.getD3_W());
                return; // break _ex_slot_calc2_exit;
                //9:
                //reg.getD2_W() = 0x3fff;
                //break _ex_slot_calc2_exit;
            }
// _ex_slot_calc2_minus:

            reg.setD3_W(reg.getD2_W());
            reg.setD3_W(reg.getD3_W() & 0x3800);
            reg.setD2_W(reg.getD2_W() & 0x7ff);
            reg.setD2_W((short) ((short) reg.getD2_W() - mm.readShort(reg.a5 + W.detune)));
            reg.setD4_W(0x26a);
            reg.setD2_W((short) ((short) reg.getD2_W() - (short) reg.getD4_W()));
            reg.setD5_W(0x800);
            reg.setD1_W((short) (-(short) reg.getD1_W()));

            L1b:
            if (reg.getD1_W() >= reg.getD4_W()) {
                reg.setD1_W((short) ((short) reg.getD1_W() - (short) reg.getD4_W()));
                reg.setD3_W((short) ((short) reg.getD3_W() - (short) reg.getD5_W()));
                if ((short) reg.getD3_W() >= 0) break L1b; // TODO
                reg.setD2_W(reg.getD4_W());
                return; // break _ex_slot_calc2_exit;
            }

            reg.setD2_W((short) ((short) reg.getD2_W() - (short) reg.getD1_W()));
            if ((short) reg.getD2_W() < 0) {
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD4_W());
                reg.setD3_W((short) ((short) reg.getD3_W() - (short) reg.getD5_W()));
                if ((short) reg.getD3_W() < 0) {
                    reg.setD2_W(reg.getD4_W());
                    return; // break _ex_slot_calc2_exit;
                }
            }
            reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD4_W());
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.detune));
            reg.setD2_W(reg.getD2_W() | reg.getD3_W());
        } finally {
// _ex_slot_calc2_exit:
            reg.D4_L = sp;
        }
    }

    /** */
    public void _ch_fm_exp_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            comwave._com_wavememory();
            mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) & reg.getD0_W()));
            return;
        }
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _ch_fm_exp_com_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_fm_exp_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_fm_exp_com_exec();
        }
    }

    public void _ch_fm_exp_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_fm_exp_com_exec();
        }
    }

    public void _ch_fm_exp_com_exec() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.a0 = Ab.dummyAddress; // _pitch_extend;
        switch (reg.getD0_W()) {
        case 2:
            comlfo.comLfoSaw();
            break;
        case 4:
            comlfo.comLfoPortament();
            break;
        case 6:
            comlfo.comLfoTriangle();
            break;
        case 8:
            comlfo.comLfoOneshot();
            break;
        case 10:
            comlfo.comLfoSquare();
            break;
        case 12:
            comlfo.comLfoRandom();
            break;
        }
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));

    }

    /**
     * HARD WARE LFO delay & LW type modulation
     */
    public void _ch_fm_HLFO() {
        reg.D1_L = 0xc;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a5 + W.flag2));
        if (reg.getD1_B() == 0) return;
        reg.a4 = reg.a5 + W.v_pattern4;
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _ch_fm_HLFO_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_fm_h_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_fm_HLFO_exec();
        }
    }

    public void _ch_fm_h_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_fm_HLFO_exec();
        }
    }

    public void _ch_fm_HLFO_exec() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) {
            return;
        }

        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
        if ((reg.D0_L & 8) != 0) {
            _ch_fm_HLFO_lw();
            return;
        }

        reg.setD0_B(mm.readByte(reg.a5 + W.pan_ampm));
        reg.D1_L = 0x84;
        mndrv._OPN_WRITE4();
        //_ch_fm_HLFO_exit();
    }

    public void _ch_fm_HLFO_lw() {
        reg.D0_L = 0xc0;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.pan_ampm));
        reg.setD1_B(mm.readByte(reg.a4 + W_L.henka_work));
        reg.setD1_B(0x11);
        if ((byte) reg.getD1_B() < 0) return;
        mm.write(reg.a4 + W_L.henka_work, (byte) reg.getD1_B());
        reg.setD2_B(reg.getD1_B());
        reg.setD2_B(reg.getD2_B() & 7);
        reg.setD1_B(reg.getD1_B() >> 1);
        reg.setD1_B(reg.getD1_B() & 0x30);
        reg.setD1_B(reg.getD1_B() | reg.getD2_B());
        reg.setD0_B(reg.getD0_B() | reg.getD1_B());
        reg.D1_L = 0xb4;
        mndrv._OPN_WRITE4();
    }

    /** */
    public void _ch_fm_mml_job() {
        comanalyze._track_analyze();

        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if ((byte) reg.getD0_B() >= 0) return;
        if ((mm.readByte(reg.a5 + W.flag2) & 0x2) != 0) {
            _ch_fm_porta();
            return;
        }
        _ch_fm_bend();
    }

    /**
    // pitch bend
     */
    public void _ch_fm_bend() {
        reg.a4 = reg.a5 + W.p_pattern4;
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) return;

        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

        reg.setD5_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD5_B() == 0) {
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
            if ((short) reg.getD1_W() >= 0) {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
                _ch_fm_porta_calc();
                if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
                _set_fnum2();
                return;
            }
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
            _ch_fm_porta_calc();
            if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
                _ch_fm_bend_end();
                return;
            }
            mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
            _set_fnum2();
            return;
        }

        int f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
            if ((short) reg.getD1_W() >= 0) {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s4));
                _ch_fm_porta_calc();
                if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s4, (short) reg.getD2_W());
            } else {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s4));
                _ch_fm_porta_calc();
                if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s4, (short) reg.getD2_W());
            }
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
            if ((short) reg.getD1_W() >= 0) {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
                _ch_fm_porta_calc();
                if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s3, (short) reg.getD2_W());
            } else {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
                _ch_fm_porta_calc();
                if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s3, (short) reg.getD2_W());
            }
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
            if ((short) reg.getD1_W() >= 0) {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
                _ch_fm_porta_calc();
                if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s2, (short) reg.getD2_W());
            } else {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
                _ch_fm_porta_calc();
                if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s2, (short) reg.getD2_W());
            }
        }

        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
            if ((short) reg.getD1_W() >= 0) {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
                _ch_fm_porta_calc();
                if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s1, (short) reg.getD2_W());
            } else {
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
                _ch_fm_porta_calc();
                if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
                    _ch_fm_bend_end();
                    return;
                }
                mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s1, (short) reg.getD2_W());
            }
        }

        _set_fnum2();
        //_ch_fm_bend_end();
    }

    public void _ch_fm_bend_end() {
        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.key2));
        mm.write(reg.a5 + W.key, (byte) reg.getD0_B());

        reg.D1_L = 0;
        reg.setD2_W(0x800);
        reg.D3_L = 12;
        while (reg.getD0_B() >= reg.getD3_B()) {
            reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD3_B());
            reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD2_W());
        }
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.a4 = Ab.dummyAddress; // _fnum_table;
        reg.setD2_W(_fnum_table[reg.getD0_W() / 2]); // mm.Readshort(Reg.a4 + reg.getD0_W());
        reg.setD2_W(reg.getD2_W() | reg.getD1_W());
        _set_fnum();

    }

    /**
     * portament
     */
    public void _ch_fm_porta() {
        reg.a4 = reg.a5 + W.p_pattern4;
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD5_B() != 0) { // break _ch_fm_porta_normal;

            int f = reg.getD5_B() & 0x10;
            reg.setD5_B(reg.getD5_B() >> 5);
            if (f != 0) {
                reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
                _ch_fm_porta_calc();
                mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s1, (short) reg.getD2_W());
            }

            f = reg.getD5_B() & 0x1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f != 0) {
                reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
                _ch_fm_porta_calc();
                mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s2, (short) reg.getD2_W());
            }

            f = reg.getD5_B() & 0x1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f != 0) {
                reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
                _ch_fm_porta_calc();
                mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s3, (short) reg.getD2_W());
            }

            f = reg.getD5_B() & 0x1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f != 0) {
                reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
                reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
                _ch_fm_porta_calc();
                mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
                mm.write(reg.a5 + W.keycode3_s3, (short) reg.getD2_W());
            }
//            break L2;
        } else {
// _ch_fm_porta_normal:
            reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        }
// L2:
        mm.write(reg.a4 + W_L.count, (byte) (mm.readByte(reg.a4 + W_L.count) - 1));
        if (mm.readByte(reg.a4 + W_L.count) == 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfd));
            mm.write(reg.a4 + W_L.bendwork, (short) 0);
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a5 + W.key2));
            mm.write(reg.a5 + W.key, (byte) reg.getD0_B());
            reg.D1_L = 0;
            reg.setD2_W(0x800);
            reg.D3_L = 12;
            while (reg.getD0_B() >= reg.getD3_B()) {
                reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD3_B());
                reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD2_W());
            }
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            reg.a4 = Ab.dummyAddress; // _fnum_table;
            reg.setD2_W(_fnum_table[reg.getD0_W() / 2]); // mm.Readshort(Reg.a4 + reg.getD0_W());
            reg.setD2_W(reg.getD2_W() | reg.getD1_W());
            mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
            _set_fnum();
            return;
        }

        _ch_fm_porta_calc();
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _set_fnum();
    }

    public void _ch_fm_porta_calc() {
        reg.setD3_W(reg.getD2_W());
        reg.setD2_W(reg.getD2_W() & 0x7ff);
        reg.setD3_W(reg.getD3_W() & 0x3800);
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if (mm.readShort(reg.a4 + W_L.henka_work) != 0) { // break _ch_fm_porta_c;
            if (mm.readShort(reg.a4 + W_L.henka_work) >= 0) { // break _ch_fm_porta_m;

                mm.write(reg.a4 + W_L.henka_work, (short) (mm.readShort(reg.a4 + W_L.henka_work) - 1));
                mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + 1));
                reg.setD2_W(reg.getD2_W() + 1);
//                break _ch_fm_porta_c;
            } else {
// _ch_fm_porta_m:

                mm.write(reg.a4 + W_L.henka_work, (short) (mm.readShort(reg.a4 + W_L.henka_work) + 1));
                mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) - 1));
                reg.setD2_W(reg.getD2_W() - 1);
            }
        }
// _ch_fm_porta_c:

        if ((short) reg.getD1_W() < 0) { // break _ch_fm_porta_calc_plus;
            if (reg.getD2_W() < 0x26a) { // break _ch_fm_porta_calc_common;
                if (reg.getD3_W() == 0) return;
                reg.setD4_W(reg.getD2_W());
                reg.setD4_W(reg.getD4_W() - 0x26a);
                reg.setD3_W(reg.getD3_W() - 0x800);
                reg.setD2_W(0x4d4);
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD4_W());
//                break _ch_fm_porta_calc_common;
            }
        } else {
// _ch_fm_porta_calc_plus:

            if (reg.getD2_W() >= 0x4d4) { // break _ch_fm_porta_calc_common;
                if (reg.getD3_W() >= 0x4000) return;
                reg.setD4_W(reg.getD2_W());
                reg.setD4_W(reg.getD4_W() - 0x4d4);
                reg.setD3_W(reg.getD3_W() + 0x800);
                reg.setD2_W(0x26a);
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD4_W());
            }
        }
// _ch_fm_porta_calc_common:

        reg.setD2_W(reg.getD2_W() | reg.getD3_W());
    }

    /**
    // pitch LFO 鋸波
     */
    public void _ch_fm_p_0() {
        comlfo.comLfoSaw();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc3();
    }

    /**
    // pitch LFO portament
     */
    public void _ch_fm_p_1() {
        comlfo.comLfoPortament();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc3();
    }

    /**
    // pitch LFO delta
     */
    public void _ch_fm_p_2() {
        comlfo.comLfoTriangle();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc3();
    }

    /**
    // pitch LFO portament2
     */
    public void _ch_fm_p_3() {
        comlfo.comLfoPortament();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc2();
    }

    /**
    // pitch LFO delta2
     */
    public void _ch_fm_p_4() {
        comlfo.comLfoTriangle();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc2();
    }

    /**
    // pitch LFO delta 3
     */
    public void _ch_fm_p_5() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) return;

        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode));
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
        if ((short) reg.getD1_W() < 0) { // break _ch_fm_p_5_plus;

            mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + 1));
            reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
            if ((short) reg.getD2_W() < 0) { // break _ch_fm_p_5_common;
                reg.D2_L = 0;
//                break _ch_fm_p_5_common;
            }
        } else {
// _ch_fm_p_5_plus:
            mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
            reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
            if ((short) reg.getD2_W() < 0) { // break _ch_fm_p_5_common;
                reg.setD2_W(0x3fff);
            }
        }
// _ch_fm_p_5_common:
        if (mm.readByte(reg.a5 + W.ch3) != 0) {
            mm.write(reg.a5 + W.keycode_s1, (short) (mm.readShort(reg.a5 + W.keycode_s1) + (short) reg.getD1_W()));
            mm.write(reg.a5 + W.keycode_s2, (short) (mm.readShort(reg.a5 + W.keycode_s2) + (short) reg.getD1_W()));
            mm.write(reg.a5 + W.keycode_s3, (short) (mm.readShort(reg.a5 + W.keycode_s3) + (short) reg.getD1_W()));
            mm.write(reg.a5 + W.keycode_s4, (short) (mm.readShort(reg.a5 + W.keycode_s4) + (short) reg.getD1_W()));
        }
        _set_fnum2();
        mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
        if (mm.readByte(reg.a4 + W_L.count_work) == 0) {
            mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
            mm.write(reg.a4 + W_L.henka_work, (short) (-(short) mm.readShort(reg.a4 + W_L.henka_work)));
        }
    }

    /**
    // pitch LFO 1shot
     */
    public void _ch_fm_p_6() {
        comlfo.comLfoOneshot();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc3();
    }

    /**
    // pitch LFO 1shot 2
     */
    public void _ch_fm_p_7() {
        comlfo.comLfoOneshot();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fm_p_calc2();
    }

    /**
    // wavememory pitch
     */
    public void _ch_fm_p_wavememory() {
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _fm_p_wave_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
                _fm_p_wave_exec();
            }
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _fm_p_wave_exec();
        }
    }

    public void _fm_p_wave_exec() {
        comwave._com_wave_exec();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD0_W()));
        _ch_fm_p_calc();
    }

    /** */
    public void _ch_fm_p_calc() {
        reg.setD4_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD4_B() == 0) {
            if (mm.readByte(reg.a5 + W.ch3mode) != 0) return;

            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
            // pea _set_fnum2(pc)
            _ex_slot_calc();
            _set_fnum2();
            return;
        }

        int f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s4));
            _ex_slot_calc();
            mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
            _ex_slot_calc();
            mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
            _ex_slot_calc();
            mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
            _ex_slot_calc();
            mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());
        }

        _set_fnum2();
    }

    /** */
    public void _ch_fm_p_calc2() {
        reg.setD4_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD4_B() == 0) {
            // pea _set_fnum2(pc)
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
            _ex_slot_calc2();
            _set_fnum2();
            return;
        }

        int f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s4));
            _ex_slot_calc2();
            mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
            _ex_slot_calc2();
            mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
            _ex_slot_calc2();
            mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
            _ex_slot_calc2();
            mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());
        }

        _set_fnum2();
    }

    /** */
    public void _ch_fm_p_calc3() {
        reg.setD4_B(mm.readByte(reg.a5 + W.ch3));
        if (reg.getD4_B() == 0) {
            if (mm.readByte(reg.a5 + W.ch3mode) != 0) return;
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.addkeycode));
            _set_fnum2();
            return;
        }

        int f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s4));
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.addkeycode));
            mm.write(reg.a5 + W.keycode2_s4, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s3));
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.addkeycode));
            mm.write(reg.a5 + W.keycode2_s3, (short) reg.getD2_W());
        }

        f = reg.getD4_B() & 0x80;
        reg.setD4_B(reg.getD4_B() << 1);
        if (f != 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s2));
            reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.addkeycode));
            mm.write(reg.a5 + W.keycode2_s2, (short) reg.getD2_W());
        }

        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3_s1));
        reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.addkeycode));
        mm.write(reg.a5 + W.keycode2_s1, (short) reg.getD2_W());

        _set_fnum2();
    }

    /**
    // わうわう
     */
    public void _ch_fm_ww() {
        mm.write(reg.a4 + W_Ww.delay_work, (byte) (mm.readByte(reg.a4 + W_Ww.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_Ww.delay_work) != 0) return;

        mm.write(reg.a4 + W_Ww.delay_work, mm.readByte(reg.a4 + W_Ww.speed));
        reg.setD4_B(mm.readByte(reg.a4 + W_Ww.work));
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a4 + W_Ww.rate_work));
        mm.write(reg.a4 + W_Ww.work, (byte) reg.getD4_B());

        reg.a2 = mm.readInt(reg.a5 + W.voiceptr);
        reg.D1_L = 0x40;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        reg.setD3_B(mm.readByte(reg.a4 + W_Ww.slot));
        reg.D2_L = 4 - 1;
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD3_B() & 1;
            reg.setD3_B(reg.getD3_B() >> 1);
            if (f != 0) {
                reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                if ((byte) reg.getD0_B() < 0) {
                    reg.D0_L = 0x7f;
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);

        mm.write(reg.a4 + W_Ww.depth_work, (byte) (mm.readByte(reg.a4 + W_Ww.depth_work) - 1));
        if (mm.readByte(reg.a4 + W_Ww.depth_work) != 0) return;
        mm.write(reg.a4 + W_Ww.depth_work, mm.readByte(reg.a4 + W_Ww.depth));
        mm.write(reg.a4 + W_Ww.rate_work, (byte) (-(byte) mm.readByte(reg.a4 + W_Ww.rate_work)));
    }

    /** */
    public void _FM_F2_init() {
        reg.D0_L = 0;
        mm.write(reg.a5 + W.vol2, (byte) reg.getD4_B());
        reg.D1_L = mm.readInt(reg.a5 + W.voiceptr);
        if (reg.D1_L == 0) return;
        reg.a2 = reg.D1_L;
        reg.D0_L = 6;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.fbcon));
        reg.a0 = Ab.dummyAddress; // _fm_vol_con_pat;
        reg.setD3_B(_fm_vol_con_pat[reg.getD0_W()]); // mm.readByte(Reg.a0 + reg.getD0_W());

        reg.D1_L = 0x40;
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.dev));
        reg.D2_L = 4 - 1;
        reg.setD5_B(mm.readByte(reg.a5 + W.ch3tl));
        //_FM_F2_init_loop:
        do {
            reg.setD0_B(mm.readByte(reg.a2++));
            int f = reg.getD5_B() & 1;
            reg.setD5_B(reg.getD5_B() >> 1);
            if (f == 0) {
                reg.setD3_B(reg.getD3_B() >> 1);
            } else {
                f = reg.getD3_B() & 1;
                reg.setD3_B(reg.getD3_B() >> 1);
                if (f != 0) {
                    reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD4_B());
                    if ((byte) reg.getD0_B() < 0) {
                        reg.D0_L = 0x7f;
                    }
                }
                mndrv._OPN_WRITE2();
            }
            reg.setD1_W(reg.getD1_W() + 4);
        } while (reg.decAfterD2_W() != 0);
    }

    /** */
    public void _ch_fm_softenv_job() {
        comlfo.softEnv();
        _FM_F2_softenv();
    }

    /**
    // effect execute
     */
    public void _fm_effect_ycommand() {
        reg.setD1_B((byte) (reg.getD0_W() >> 8));
        mndrv._OPN_WRITE2();
    }

    public void _fm_effect_tone() {
        mm.write(reg.a5 + W.bank, (byte) (reg.getD0_W() >> 8));
        reg.setD5_B(reg.getD0_B());
        _fm_echo_tone_change();
    }

    public void _fm_effect_pan() {
        reg.setD0_W(reg.getD0_W() & 3);
        reg.setD0_B(_fm_effect_pan_table[reg.getD0_W()]);
        reg.D1_L = 0x3f;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a5 + W.pan_ampm));
        reg.setD0_B(reg.getD0_B() | reg.getD1_B());
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.reverb_pan_work, (byte) reg.getD0_B());
        reg.D1_L = 0x84;
        mndrv._OPN_WRITE4();
    }

    public static final byte[] _fm_effect_pan_table = new byte[] {
            0x00, (byte) 0x80, 0x40, (byte) 0xC0
    };
}
