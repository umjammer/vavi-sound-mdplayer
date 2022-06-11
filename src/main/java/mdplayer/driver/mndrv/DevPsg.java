package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


//
// part of YM2608 - PSG
//
public class DevPsg {

    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public ComAnalyze comanalyze;
    public ComCmds comcmds;
    public ComLfo comlfo;
    public ComWave comwave;
    public DevOpn devopn;
    public Ab ab;

    /** */
    public void _psg_note_set() {
        mm.write(reg.a5 + W.key, (byte) reg.getD0_B());
        //    pea _psg_env_keyon(pc)
        _psg_freq();
        comlfo._init_lfo();
        _init_lfo_psg();
        _psg_env_keyon();
    }

    /** */
    public void _psg_freq() {
        reg.D1_L = 0;
        reg.D2_L = 12;

        while (reg.getD0_B() >= reg.getD2_B()) {
            reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD2_B());
            reg.setD1_B(reg.getD1_B() + 1);
        }

        mm.write(reg.a5 + W.octave, (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD0_W(_psg_table[reg.getD0_W() / 2]);
        mm.write(reg.a5 + W.makotune, (short) reg.getD0_W());
        reg.setD2_B(mm.readByte(reg.a6 + Dw.DRV_FLAG2));
        int f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f == 0) {
            _set_psg_();
            return;
        }
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f == 0) {
            _set_psg_mako();
            return;
        }

        mm.write(reg.a5 + W.freqbase, (short) reg.getD0_W());
        mm.write(reg.a5 + W.freqwork, (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() >> (int) reg.getD1_W());
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }

        mm.write(reg.a5 + W.keycode2, (short) reg.getD0_W());
        _set_psg_bend();
    }

    public void _set_psg_mako() {
        mm.write(reg.a5 + W.freqbase, (short) reg.getD0_W());
        mm.write(reg.a5 + W.freqwork, (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() >> (int) reg.getD1_W());
        reg.setD1_W(mm.readShort(reg.a5 + W.detune));
        reg.setD1_W((short) (-(short) reg.getD1_W()));

        reg.setD1_W((short) ((short) reg.getD1_W() >> 2));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD1_W());
        mm.write(reg.a5 + W.keycode2, (short) reg.getD0_W());
        _set_psg_bend();
    }

    public void _set_psg_() {
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }

        mm.write(reg.a5 + W.freqbase, (short) reg.getD0_W());
        mm.write(reg.a5 + W.freqwork, (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() >> (int) reg.getD1_W());
        mm.write(reg.a5 + W.keycode2, (short) reg.getD0_W());
        _set_psg_bend();
    }

    public void _set_psg_bend() {
        mm.write(reg.a5 + W.keycode, (short) reg.getD0_W());
        reg.setD1_B(mm.readByte(reg.a5 + W.dev));
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD1_B());

        if (reg.getD0_W() - mm.readShort(reg.a5 + W.tune) == 0) return;

        mm.write(reg.a5 + W.tune, (short) reg.getD0_W());
        short sp = (short) reg.getD0_W();
        mndrv._OPN_WRITE2();
        reg.setD0_B((byte) (sp >> 8));
        reg.setD1_B(reg.getD1_B() + 1);
        mndrv._OPN_WRITE2();
    }

    /** */
    public static final short[] _psg_table = new short[] {
            0x0EE8, 0x0E12, 0x0D48, 0x0C89
            , 0x0BD5, 0x0B2B, 0x0A8A, 0x09F3
            , 0x0964, 0x08DD, 0x085E, 0x07E6
    };

    /** */
    public void _psg_env_keyon() {
        if (mm.readByte(reg.a5 + W.flag2) < 0) return;
        comwave._wave_init_kon();

        mm.write(reg.a5 + W.reverb_time_work, 0);
        mm.write(reg.a5 + W.revexec, 0);

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x20));
        if ((mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) return;
        }

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xfb));

        reg.setD0_B(mm.readByte(reg.a5 + W.e_sw));
        if ((byte) reg.getD0_B() < 0) {
            _psg_env_keyon_();
            return;
        }

        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD1_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD1_B() >= 0) {
            _psg_env_keyon1();
            return;
        }
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        if ((byte) reg.getD0_B() >= 0) {
            _psg_env_keyon2();
            return;
        }
        reg.D0_L = 0;
        _psg_env_keyon2();
    }

    public void _psg_env_keyon1() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        if (reg.getD0_B() >= 16) {
            reg.D0_L = 15;
        }
        _psg_env_keyon2();
    }

    public void _psg_env_keyon2() {
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }

        reg.setD0_W(reg.getD0_W() & 0xf);
        reg.a2 = reg.a5 + W.voltable;
        reg.setD0_B(mm.readByte(reg.a2 + (int) (short) reg.getD0_W()));
        mm.write(reg.a5 + W.vol2, (byte) reg.getD0_B());
        reg.D1_L = 8;
        mndrv._OPN_WRITE4();
    }

    public void _psg_env_keyon_() {
        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            mm.write(reg.a5 + W.e_p, 5);
            return;
        }

        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        reg.setD0_B(reg.getD0_B() & 0xf0);
        mm.write(reg.a5 + W.e_p, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_dl, mm.readByte(reg.a0 + (int) (short) reg.getD0_W()));
        reg.D1_L = 0x7f;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 1));
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        mm.write(reg.a5 + W.e_lm, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 2));
        mm.write(reg.a5 + W.e_ini, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 3));
    }

    /** */
    public void _psg_env_keyoff() {
        if (mm.readByte(reg.a5 + W.rct) == 0) {
            _psg_keyoff();
            return;
        }

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
        comwave._wave_init_kof();
    }

    public void _psg_keyoff() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
        comwave._wave_init_kof();
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));

        reg.setD0_B(mm.readByte(reg.a5 + W.e_sw));
        if ((byte) reg.getD0_B() >= 0) {
            reg.D0_L = 0;
            reg.D1_L = 8;
            mndrv._OPN_WRITE4();
            return;
        }

        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            mm.write(reg.a5 + W.e_p, 4);
            return;
        }

        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        reg.setD0_B(reg.getD0_B() & 0xf0);
        reg.setD0_B(reg.getD0_B() + 0xc);
        mm.write(reg.a5 + W.e_p, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_dl, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 0));
        reg.setD1_B(mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 1));
        reg.setD1_B(reg.getD1_B() & 0x7f);
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        mm.write(reg.a5 + W.e_lm, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 2));
    }

    /**
     */
    public void _psg_echo() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;

        mm.write(reg.a5 + W.revexec, 0xff);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
        mm.write(reg.a5 + W.reverb_time_work, (byte) (mm.readByte(reg.a5 + W.reverb_time)));

        reg.D5_L = 7;
        reg.setD5_B(reg.getD5_B() & mm.readByte(reg.a5 + W.reverb));
        reg.setD5_W(reg.getD5_W() + 1);
        reg.setD5_W(reg.getD5_W() + (int) (short) reg.getD5_W());
        switch (reg.getD5_W()) {
        case 2:
        case 4:
        case 6:
        case 8:
            _psg_echo_volume();
            //_psg_echo_volume_pan();
            //_psg_echo_volume_tone();
            //_psg_echo_volume_pan_tone();
            break;
        case 10:
            _psg_echo_volume_();
            break;
        }
    }

    /** */
    public void _psg_echo_volume() {
        //_psg_echo_volume_pan
        //_psg_echo_volume_tone
        //_psg_echo_volume_pan_tone
        _psg_echo_common_v();
    }

    public void _psg_echo_volume_() {
        _psg_echo_volume_v();
    }

    /**
     * v通常
     */
    public void _psg_echo_common_v() {
        if ((mm.readByte(reg.a5 + W.reverb) & 0x08) != 0) {
            _psg_echo_direct_v();
            return;
        }

        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD1_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD1_B() >= 0) {
            _psg_echo_plus();
            return;
        }

        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        _psg_f2_softenv();
    }

    public void _psg_echo_plus() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        if (reg.getD0_B() >= 0xf) {
            reg.D0_L = 0xf;
        }
        _psg_f2_softenv();
    }

    /**
     * v微調整
     */
    public void _psg_echo_volume_v() {
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD1_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD1_B() < 0) {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if ((byte) reg.getD0_B() < 0) {
                reg.D0_L = 0;
            }
        } else {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if (reg.getD0_B() >= 16) {
                reg.D0_L = 15;
            }
        }

        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }

        reg.setD1_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD1_B() < 0) {
            _psg_echo_vol_plus();
            return;
        }
        reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD1_B());
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        _psg_echo_vol_1();
    }

    public void _psg_echo_vol_plus() {
        reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD1_B());
        if (reg.getD0_B() >= 0xf) {
            reg.D0_L = 0xf;
        }
        _psg_echo_vol_1();
    }

    public void _psg_echo_vol_1() {
        reg.setD0_B(reg.getD0_B() >> 1);
        _psg_lfo();
    }

    /**
     * v直接
     */
    public void _psg_echo_direct_v() {
        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        _psg_f2_softenv();
    }

    /** */
    public void _psg_env() {
        if ((byte) mm.readByte(reg.a5 + W.e_sw) >= 0) return;
        mm.write(reg.a5 + W.e_sp, (byte) (mm.readByte(reg.a5 + W.e_sp) - 1));
        if (mm.readByte(reg.a5 + W.e_sp) == 0) {
            _psg_env_next();
        }
    }

    public void _psg_env_next() {
        reg.a4 = mm.readInt(reg.a5 + W.psgenv_adrs);

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        reg.setD1_B(mm.readByte(reg.a4 + (int) (short) reg.getD0_W() + 1));
        if ((byte) reg.getD1_B() < 0) {
            _psg_env_minus();
            return;
        }

        reg.setD1_B(reg.getD1_B() & 0x7f);
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        reg.setD0_B(mm.readByte(reg.a5 + W.e_ini));
        Boolean f = reg.cryADD((byte) reg.getD0_B(), mm.readByte(reg.a5 + W.e_dl));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.e_dl));
        if (f) {
            _psg_env_common();
            return;
        }
        if (reg.getD0_B() >= mm.readByte(reg.a5 + W.e_lm)) {
            _psg_env_common();
            return;
        }
        _psg_volume_set();
    }

    public void _psg_env_minus() {
        reg.setD1_B(reg.getD1_B() & 0x7f);
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        reg.setD0_B(mm.readByte(reg.a5 + W.e_ini));
        boolean cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_dl);
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_dl));
        if (cf) {
            _psg_env_common();
            return;
        }
        if (reg.getD0_B() <= mm.readByte(reg.a5 + W.e_lm)) {
            _psg_env_common();
            return;
        }
        _psg_volume_set();
    }

    public void _psg_env_common() {
        reg.setD0_B(mm.readByte(reg.a5 + W.e_lm));
        _psg_volume_set();
        if (mm.readByte(reg.a5 + W.e_ini) == 0) {
            _psg_volume_0();
            return;
        }

        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        if ((reg.D0_L & 0x8) != 0) {
            _psg_volume_0();
            return;
        }
        reg.setD0_B(reg.getD0_B() + 4);
        mm.write(reg.a5 + W.e_p, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_dl, mm.readByte(reg.a4 + (int) (short) reg.getD0_W() + 0));
        reg.D1_L = 0x7f;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a4 + (int) (short) reg.getD0_W() + 1));
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        mm.write(reg.a5 + W.e_lm, mm.readByte(reg.a4 + (int) (short) reg.getD0_W() + 2));
    }

    public void _psg_volume_0() {
        reg.D0_L = 0;
        _psg_volume_set();
    }

    public void _psg_volume_set() {
        mm.write(reg.a5 + W.e_ini, (byte) reg.getD0_B());
        _psg_volume_set2();
    }

    public void _psg_volume_set2() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.vol));
        reg.setD2_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD2_B() < 0) {
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD2_B());
            if ((byte) reg.getD1_B() < 0) {
                reg.D1_L = 0;
            }
        } else {
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD2_B());
            if (reg.getD1_B() >= 16) {
                reg.D1_L = 15;
            }
        }
        reg.setD1_B(reg.getD1_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        if ((byte) reg.getD1_B() < 0) {
            reg.D1_L = 0;
        }
        reg.setD1_B(reg.getD1_B() + 1);
        reg.setD1_W(reg.getD1_W() * reg.getD0_W());
        short sp = (short) reg.getD1_W();
        reg.D0_L = 0;
        reg.setD0_B((byte) (sp >> 8));
        _psg_volume_set3();
    }

    public void _psg_volume_set3() {
        reg.a2 = reg.a5 + W.voltable;
        reg.setD0_B(mm.readByte(reg.a2 + (int) (short) reg.getD0_W()));

        if (reg.getD0_B() - mm.readByte(reg.a5 + W.vol2) != 0) {
            mm.write(reg.a5 + W.vol2, (byte) reg.getD0_B());
            reg.D1_L = 8;
            mndrv._OPN_WRITE4();
        }
    }

    /**
     */
    public void _init_lfo_psg() {
        if (mm.readByte(reg.a5 + W.e_sw) >= 0) return;
        if (mm.readByte(reg.a6 + Dw.FADEFLAG) == 0) {
            reg.setD0_B(mm.readByte(reg.a5 + W.vol));
            _psg_lfo();
        }

        reg.setD5_B(mm.readByte(reg.a5 + W.lfo));
        int f = reg.getD5_B() & 0x40;
        reg.setD5_B(reg.getD5_B() << 2);
        if (f != 0) {
            reg.a4 = reg.a5 + W.v_pattern3;
            comlfo._init_lfo_common_a();
        }
        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.a4 = reg.a5 + W.v_pattern2;
            comlfo._init_lfo_common_a();
        }
        f = reg.getD5_B() & 0x80;
        reg.setD5_B(reg.getD5_B() << 1);
        if (f != 0) {
            reg.a4 = reg.a5 + W.v_pattern1;
            comlfo._init_lfo_common_a();
        }
    }

    /**
     * MML コマンド処理 ( PSG 部 )
     */
    public void _psg_command() {
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        //_psgc:
        switch (reg.getD0_W() / 2) {

        case 0x00:
            break;
        case 0x01:
            comcmds._COM_81();
            break;// 81
        case 0x02:
            _PSG_82();
            break;// 82
        case 0x03:
            comcmds._COM_83();
            break;// 83	すらー
        case 0x04:
            _PSG_NOP();
            break;// 84
        case 0x05:
            _PSG_NOP();
            break;// 85
        case 0x06:
            comcmds._COM_86();
            break;// 86	同期信号送信
        case 0x07:
            comcmds._COM_87();
            break;// 87	同期待ち
        case 0x08:
            _PSG_88();
            break;// 88	ぴっちべんど
        case 0x09:
            _PSG_89();
            break;// 89	ぽるためんと
        case 0x0a:
            _PSG_NOP();
            break;// 8A
        case 0x0b:
            _PSG_NOP();
            break;// 8B
        case 0x0c:
            _PSG_NOP();
            break;// 8C
        case 0x0d:
            _PSG_NOP();
            break;// 8D
        case 0x0e:
            _PSG_NOP();
            break;// 8E
        case 0x0f:
            _PSG_NOP();
            break;// 8F

        case 0x10:
            comcmds._COM_90();
            break;// 90	q
        case 0x11:
            comcmds._COM_91();
            break;// 91	@q
        case 0x12:
            _PSG_NOP();
            break;// 92
        case 0x13:
            comcmds._COM_93();
            break;// 93	neg @q
        case 0x14:
            comcmds._COM_94();
            break;// 94	keyoff mode
        case 0x15:
            _PSG_NOP();
            break;// 95
        case 0x16:
            _PSG_NOP();
            break;// 96
        case 0x17:
            _PSG_NOP();
            break;// 97
        case 0x18:
            _PSG_98();
            break;// 98	擬似リバーブ
        case 0x19:
            _PSG_NOP();
            break;// 99
        case 0x1a:
            comcmds._COM_9A();
            break;// 9A	擬似 step time
        case 0x1b:
            _PSG_NOP();
            break;// 9B
        case 0x1c:
            _PSG_NOP();
            break;// 9C
        case 0x1d:
            _PSG_NOP();
            break;// 9D
        case 0x1e:
            _PSG_NOP();
            break;// 9E
        case 0x1f:
            _PSG_NOP();
            break;// 9F

        case 0x20:
            _PSG_A0();
            break;// A0	wavenum
        case 0x21:
            _PSG_A1();
            break;// A1	bank + wavenum
        case 0x22:
            _PSG_A2();
            break;// A2	書き換え & 再定義
        case 0x23:
            _PSG_A3();
            break;// A3	音量テーブル
        case 0x24:
            _PSG_F2();
            break;// A4
        case 0x25:
            _PSG_F5();
            break;// A5
        case 0x26:
            _PSG_F6();
            break;// A6
        case 0x27:
            _PSG_NOP();
            break;// A7
        case 0x28:
            _PSG_NOP();
            break;// A8
        case 0x29:
            _PSG_NOP();
            break;// A9
        case 0x2a:
            _PSG_NOP();
            break;// AA
        case 0x2b:
            _PSG_NOP();
            break;// AB
        case 0x2c:
            _PSG_NOP();
            break;// AC
        case 0x2d:
            _PSG_NOP();
            break;// AD
        case 0x2e:
            _PSG_NOP();
            break;// AE
        case 0x2f:
            _PSG_NOP();
            break;// AF

        case 0x30:
            comcmds._COM_B0();
            break;// B0
        case 0x31:
            _PSG_NOP();
            break;// B1
        case 0x32:
            _PSG_NOP();
            break;// B2
        case 0x33:
            _PSG_NOP();
            break;// B3
        case 0x34:
            _PSG_NOP();
            break;// B4
        case 0x35:
            _PSG_NOP();
            break;// B5
        case 0x36:
            _PSG_NOP();
            break;// B6
        case 0x37:
            _PSG_NOP();
            break;// B7
        case 0x38:
            _PSG_NOP();
            break;// B8
        case 0x39:
            _PSG_NOP();
            break;// B9
        case 0x3a:
            _PSG_NOP();
            break;// BA
        case 0x3b:
            _PSG_NOP();
            break;// BB
        case 0x3c:
            _PSG_NOP();
            break;// BC
        case 0x3d:
            _PSG_NOP();
            break;// BD
        case 0x3e:
            comcmds._COM_BE();
            break;// BE	ジャンプ
        case 0x3f:
            comcmds._COM_BF();
            break;// BF

        case 0x40:
            _PSG_C0();
            break;// C0	ソフトウェアエンベロープ
        case 0x41:
            _PSG_C1();
            break;// C1	ソフトウェアエンベロープ
        case 0x42:
            _PSG_C2();
            break;// C2	キーオフボリューム
        case 0x43:
            _PSG_C3();
            break;// C3	ソフトウェアエンベロープスイッチ
        case 0x44:
            _PSG_A0();
            break;// C4	エンベロープ切り替え (@e)
        case 0x45:
            _PSG_A1();
            break;// C5	エンベロープ切り替え (@e bank)
        case 0x46:
            _PSG_NOP();
            break;// C6
        case 0x47:
            _PSG_NOP();
            break;// C7
        case 0x48:
            _PSG_C8();
            break;// C8	ノイズ周波数
        case 0x49:
            _PSG_C9();
            break;// C9	ミキサー
        case 0x4a:
            _PSG_NOP();
            break;// CA
        case 0x4b:
            _PSG_NOP();
            break;// CB
        case 0x4c:
            _PSG_NOP();
            break;// CC
        case 0x4d:
            _PSG_NOP();
            break;// CD
        case 0x4e:
            _PSG_NOP();
            break;// CE
        case 0x4f:
            _PSG_CF();
            break;// CF	エンベロープ2

        case 0x50:
            comcmds._COM_D0();
            break;// D0	キートランスポーズ
        case 0x51:
            comcmds._COM_D1();
            break;// D1	相対キートランスポーズ
        case 0x52:
            _PSG_NOP();
            break;// D2
        case 0x53:
            _PSG_NOP();
            break;// D3
        case 0x54:
            _PSG_NOP();
            break;// D4
        case 0x55:
            _PSG_NOP();
            break;// D5
        case 0x56:
            _PSG_NOP();
            break;// D6
        case 0x57:
            _PSG_NOP();
            break;// D7
        case 0x58:
            comcmds._COM_D8();
            break;// D8	ディチューン
        case 0x59:
            comcmds._COM_D9();
            break;// D9	相対ディチューン
        case 0x5a:
            _PSG_NOP();
            break;// DA
        case 0x5b:
            _PSG_NOP();
            break;// DB
        case 0x5c:
            _PSG_NOP();
            break;// DC
        case 0x5d:
            _PSG_NOP();
            break;// DD
        case 0x5e:
            _PSG_NOP();
            break;// DE
        case 0x5f:
            _PSG_NOP();
            break;// DF

        case 0x60:
            _PSG_NOP();
            break;// E0
        case 0x61:
            _PSG_NOP();
            break;// E1
        case 0x62:
            comcmds._COM_E2();
            break;// E2	pitch LFO
        case 0x63:
            comcmds._COM_E3();
            break;// E3	pitch LFO switch
        case 0x64:
            comcmds._COM_E4();
            break;// E4	pitch LFO delay
        case 0x65:
            _PSG_NOP();
            break;// E5
        case 0x66:
            _PSG_NOP();
            break;// E6
        case 0x67:
            _PSG_E7();
            break;// E7	amp LFO
        case 0x68:
            _PSG_E8();
            break;// E8	amp LFO switch
        case 0x69:
            _PSG_E9();
            break;// E9	amp LFO delay
        case 0x6a:
            _PSG_NOP();
            break;// EA
        case 0x6b:
            _PSG_NOP();
            break;// EB
        case 0x6c:
            _PSG_NOP();
            break;// EC
        case 0x6d:
            comcmds._COM_ED();
            break;// ED
        case 0x6e:
            _PSG_C8();
            break;// EE
        case 0x6f:
            _PSG_C9();
            break;// EF

        case 0x70:
            _PSG_NOP();
            break;// F0
        case 0x71:
            comcmds._COM_D8();
            break;// F1
        case 0x72:
            _PSG_F2();
            break;// F2	volume
        case 0x73:
            comcmds._COM_91();
            break;// F3	@q
        case 0x74:
            _PSG_NOP();
            break;// F4
        case 0x75:
            _PSG_F5();
            break;// F5	)
        case 0x76:
            _PSG_F6();
            break;// F6	(
        case 0x77:
            _PSG_NOP();
            break;// F7
        case 0x78:
            _PSG_NOP();
            break;// F8
        case 0x79:
            comcmds._COM_F9();
            break;// F9	永久ループポイントマーク
        case 0x7a:
            devopn._FM_FA();
            break;// FA	Y command
        case 0x7b:
            comcmds._COM_FB();
            break;// FB	リピート抜け出し
        case 0x7c:
            comcmds._COM_FC();
            break;// FC	リピート開始
        case 0x7d:
            comcmds._COM_FD();
            break;// FD	リピート終端
        case 0x7e:
            comcmds._COM_FE();
            break;// FE	tempo
        case 0x7f:
            _PSG_FF();
            break;// FF	end of data
        }
    }

    /**
     */
    public void _PSG_NOP() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
        _psg_keyoff();
    }

    /**
     */
    public void _PSG_82() {
        _psg_keyoff();
    }

    /**
     * ピッチベンド
     * 	[$88] + [目標音程]b + [delay]b + [speed]b + [rate]W
     */
    public void _PSG_88() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x80));
        reg.a4 = reg.a5 + W.p_pattern4;

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.key_trans));
        mm.write(reg.a5 + W.key2, (byte) reg.getD0_B());
        _get_freq();
        mm.write(reg.a4 + W_L.mokuhyou, (short) reg.getD0_W());

        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD1_B());

        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a4 + W_L.henka, (short) reg.getD0_W());
    }

    /**
     * ポルタメント
     * 	[$89] + [switch]b + [先note]b + [元note]b + [step]b
     */
    public void _PSG_89() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x80));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x02));
        reg.a4 = reg.a5 + W.p_pattern4;
        reg.setD0_B(mm.readByte(reg.a1++));

        _PSG_89_normal();
    }

    public void _PSG_89_normal() {
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.key_trans));
        _get_freq();
        reg.setD1_W(reg.getD0_W());

        reg.setD0_B(mm.readByte(reg.a1));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.key_trans));
        _get_freq();

        reg.D2_L = 0;
        reg.setD2_B(mm.readByte(reg.a1 + 1));
        mm.write(reg.a4 + W_L.count, (byte) reg.getD2_B());

        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.D1_L = (short) reg.getD1_W();
        reg.D1_L = (short) ((int) reg.D1_L / (short) reg.getD2_W()) | (int) (((short) ((int) reg.D1_L % (short) reg.getD2_W())) << 16);
        mm.write(reg.a4 + W_L.henka, (short) reg.getD1_W());
        reg.D1_L = (reg.D1_L << 16) | (reg.D1_L >> 16);
        mm.write(reg.a4 + W_L.henka_work, (short) reg.getD1_W());
    }

    /**
     * frequency
     */
    public void _get_freq() {
        Reg spReg = new Reg();
        spReg.D1_L = reg.D1_L;
        spReg.D2_L = reg.D2_L;
        spReg.a0 = reg.a0;

        reg.setD0_W(reg.getD0_W() & 0xff);
        reg.D1_L = 0;
        reg.D2_L = 0xc;
        while (reg.getD0_B() >= reg.getD2_B()) {
            reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD2_B());
            reg.setD1_B(reg.getD1_B() + 1);
        }
        //reg.getD0_W() += (int)(short)reg.getD0_W();
        //Reg.a0 = _psg_table;
        reg.setD0_W(_psg_table[reg.getD0_W()]);
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }
        reg.setD0_W(reg.getD0_W() >> reg.getD1_W());

        reg.D1_L = spReg.D1_L;
        reg.D2_L = spReg.D2_L;
        reg.a0 = spReg.a0;
    }

    /**
     * 擬似リバーブ
     * 	switch = $80 = ON
     * 		 $81 = OFF
     * 		 $00 = + [volume]b
     * 		 $01 = + [volume]b + [pan]b
     * 		 $02 = + [volume]b + [tone]b
     * 		 $03 = + [volume]b + [panpot]b + [tone]b
     * 		 $04 = + [volume]b
     */
    public void _PSG_98() {
        comcmds._COM_98();

        if ((mm.readByte(reg.a5 + W.reverb) & 0x80) == 0) {
            _psg_keyoff();
        }
    }

    /**
     * ソフトウェアエンベロープ
     * 	[$A0] + [NUM]b
     */
    public void _PSG_A0() {
        reg.setD5_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.program, (byte) reg.getD5_B());

        reg.D0_L = mm.readInt(reg.a6 + Dw.ENV_PTR);
        if (reg.D0_L == 0) return;

        reg.a2 = reg.D0_L;
        reg.a2 += 6;
        reg.setD4_W(mm.readShort(reg.a6 + Dw.ENVNUM));
        if (reg.getD4_W() == 0) return;

        reg.setD1_B(mm.readByte(reg.a5 + W.bank));
// _psg_a0_ana_loop:
        while (true) {
            if (reg.getD1_B() - mm.readByte(reg.a2 + 2) == 0) {
                if (reg.getD5_B() - mm.readByte(reg.a2 + 3) == 0) break; // _psg_a0_set;
            }
            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() == 0) return;
            reg.setD0_W(mm.readShort(reg.a2));
            reg.a2 = reg.a2 + (int) (short) reg.getD0_W();
//            break _psg_a0_ana_loop;
        }
// _psg_a0_set:
        reg.setD5_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.a2 += 2;

        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);

        reg.D0_L = 16 - 1;
        do {
            mm.write(reg.a0, mm.readByte(reg.a2));
            reg.a2++;
            reg.a0++;
        } while (reg.decAfterD0_W() != 0);

        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));

        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a2++));
        if ((byte) reg.getD1_B() >= 0) {
            _psg_c9_();
        }
        reg.setD0_B(mm.readByte(reg.a2));
        reg.a2++;
        if ((byte) reg.getD0_B() >= 0) {
            _psg_c8_();
        }

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG2) & 0x4) == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;

            reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
            mm.write(reg.a5 + W.e_dl, mm.readByte(reg.a0 + (int) (short) reg.getD0_W()));
            reg.D1_L = 0x7f;
            reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 1));
            mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());

            mm.write(reg.a5 + W.e_lm, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 2));

            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            mm.write(reg.a5 + W.e_p, 0);
            mm.write(reg.a5 + W.e_sub, 0);
        }
        mm.write(reg.a5 + W.e_sw, 0x81);
        short sp = (short) reg.getSR_W();
        reg.setSR_W(reg.getSR_W() | 0x700);
        // reg.a3 = _ex_soft4;
        reg.a3 = Ab.dummyAddress;
        mm.write(reg.a5 + W.softenv_adrs, reg.a3);
        ab.hlw_softenv_adrs.put(reg.a5, this::_ex_soft4);
        reg.setSR_W(sp);

        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
        mm.write(reg.a5 + W.e_sv, mm.readByte(reg.a0 + 3));
        mm.write(reg.a5 + W.e_ar, mm.readByte(reg.a0 + 0));
        mm.write(reg.a5 + W.e_dr, mm.readByte(reg.a0 + 4));
        mm.write(reg.a5 + W.e_sl, mm.readByte(reg.a0 + 6));
        mm.write(reg.a5 + W.e_sr, mm.readByte(reg.a0 + 8));
        mm.write(reg.a5 + W.e_rr, mm.readByte(reg.a0 + 12));
    }

    /**
     *
     */
    public void _PSG_A1() {
        mm.write(reg.a5 + W.bank, mm.readByte(reg.a1++));
        _PSG_A0();
    }

    /**
     *
     */
    public void _PSG_A2() {
        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.bank, (byte) reg.getD1_B());
        reg.setD5_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.program, (byte) reg.getD5_B());

        reg.D0_L = mm.readInt(reg.a6 + Dw.ENV_PTR);
        if (reg.D0_L == 0) return;

        reg.a2 = reg.D0_L;
        reg.a2 += 6;
        reg.setD4_W(mm.readShort(reg.a6 + Dw.ENVNUM));
        if (reg.getD4_W() == 0) return;

// _psg_a2_ana_loop:
        while (true) {
            if (reg.getD1_B() - mm.readByte(reg.a2 + 2) == 0) {
                if (reg.getD5_B() - mm.readByte(reg.a2 + 3) == 0)
                    break; // _psg_a2_set;
            }
            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() == 0) return;
            reg.setD0_W(mm.readShort(reg.a2));
            reg.a2 = reg.a2 + (int) (short) reg.getD0_W();
//            break _psg_a2_ana_loop;
        }
// _psg_a2_set:
        reg.setD5_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.a2 += 2;

        reg.a0 = 0;// _psg_env_default;

        _psg_env_default[3] = mm.readByte(reg.a1++);//mm.Write(Reg.a0 + 3, mm.ReadByte(Reg.a1++));
        _psg_env_default[0] = mm.readByte(reg.a1++);//mm.Write(Reg.a0 + 0, mm.ReadByte(Reg.a1++));
        _psg_env_default[4] = mm.readByte(reg.a1++);//mm.Write(Reg.a0 + 4, mm.ReadByte(Reg.a1++));
        _psg_env_default[6] = mm.readByte(reg.a1++);//mm.Write(Reg.a0 + 6, mm.ReadByte(Reg.a1++));
        _psg_env_default[8] = mm.readByte(reg.a1++);//mm.Write(Reg.a0 + 8, mm.ReadByte(Reg.a1++));
        _psg_env_default[12] = mm.readByte(reg.a1++);//mm.Write(Reg.a0 + 12, mm.ReadByte(Reg.a1++));

        reg.D0_L = 16 - 1;
        do {
            mm.write(reg.a2, _psg_env_default[reg.a0]);// mm.ReadByte(Reg.a0));
            reg.a2++;
            reg.a0++;
        } while (reg.decAfterD0_W() != 0);

        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
        reg.a2 = 0;// _psg_env_default;

        reg.D0_L = 16 - 1;
        do {
            mm.write(reg.a0, _psg_env_default[reg.a2]);// mm.ReadByte(Reg.a2));
            reg.a2++;
            reg.a0++;
        } while (reg.decAfterD0_W() != 0);

        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));
        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        mm.write(reg.a5 + W.e_dl, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 0));
        reg.D1_L = 0x7f;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 1));
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        mm.write(reg.a5 + W.e_lm, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 2));
    }

    /**
     * 音量テーブル
     */
    public void _PSG_A3() {
        comcmds._COM_A3();
        _psg_lfo();
    }

    /**
     * ソフトウェアエンベロープ
     * 	[$C0] + [SV]b + [AR]b + [DR]b + [SL]b + [SR]b + [RR]b
     */
    public void _PSG_C0() {
        mm.write(reg.a5 + W.program, 0xff);
        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
        //Reg.a2 = _psg_env_default;
        _psg_env_default[3] = mm.readByte(reg.a1++);//mm.Write(Reg.a2 + 3, mm.ReadByte(Reg.a1++));
        _psg_env_default[0] = mm.readByte(reg.a1++);//mm.Write(Reg.a2 + 0, mm.ReadByte(Reg.a1++));
        _psg_env_default[4] = mm.readByte(reg.a1++);//mm.Write(Reg.a2 + 4, mm.ReadByte(Reg.a1++));
        _psg_env_default[6] = mm.readByte(reg.a1++);//mm.Write(Reg.a2 + 6, mm.ReadByte(Reg.a1++));
        _psg_env_default[8] = mm.readByte(reg.a1++);//mm.Write(Reg.a2 + 8, mm.ReadByte(Reg.a1++));
        _psg_env_default[12] = mm.readByte(reg.a1++);//mm.Write(Reg.a2 + 12, mm.ReadByte(Reg.a1++));
        _psg_env_default[14] = mm.readByte(reg.a5 + W.kov);//mm.Write(Reg.a2 + 14, mm.ReadByte(Reg.a5 + W.kov));
        reg.a2 = 0;

        reg.D0_L = 16 - 1;
        do {
            mm.write(reg.a0, _psg_env_default[reg.a2]);// mm.ReadByte(Reg.a2));
            reg.a2++;
            reg.a0++;
        } while (reg.decAfterD0_W() != 0);

        mm.write(reg.a5 + W.e_sw, 0x80);
        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        mm.write(reg.a5 + W.e_dl, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 0));
        reg.D1_L = 0x7f;
        reg.setD1_B(reg.getD1_B() & mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 1));
        mm.write(reg.a5 + W.e_sp, (byte) reg.getD1_B());
        mm.write(reg.a5 + W.e_lm, mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 2));
    }

    public static final byte[] _psg_env_default = new byte[] {
            0x00, 0x01, (byte) 0xFF, (byte) 0xFF, 0x00, (byte) 0x81, 0x00, 0x00, 0x00, (byte) 0x81, 0x00, 0x00, (byte) 0xFF, (byte) 0x81, 0x00, 0x00
    };

    /**
     * ソフトウェアエンベロープ
     * 	[$C1] + [AL]b + [DD]b + [SR]b + [RR]b
     */
    public void _PSG_C1() {
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_al, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_alw, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_dd, mm.readByte(reg.a1++));
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_sr, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_srw, (byte) reg.getD0_B());
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_rr, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_rrw, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_p, 0);

        mm.write(reg.a5 + W.e_sw, 0x81);
        short sp = (short) reg.getSR_W();
        reg.setSR_W(reg.getSR_W() | 0x700);
        //Reg.a3 = _soft2;
        reg.a3 = Ab.dummyAddress;
        mm.write(reg.a5 + W.softenv_adrs, reg.a3);
        ab.hlw_softenv_adrs.put(reg.a5, this::_soft2);
        reg.setSR_W(sp);
    }

    /**
     * キーオフボリューム
     * 	[$C2] + [KOV]
     */
    public void _PSG_C2() {
        reg.a0 = mm.readInt(reg.a5 + W.psgenv_adrs);
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.kov, (byte) reg.getD0_B());
        mm.write(reg.a0 + 14, (byte) reg.getD0_B());
    }

    /**
     * ソフトウェアエンベロープスイッチ
     * 	[$C3] + [switch]
     */
    public void _PSG_C3() {
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) & 0x7f));
        reg.setD0_B(mm.readByte(reg.a1++));

        if (reg.getD0_B() != 0) {
            mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));
        }
    }

    /**
     * noise 周波数
     */
    public void _PSG_C8() {
        reg.setD0_B(mm.readByte(reg.a1++));
        _psg_c8_();
    }

    public void _psg_c8_() {
        if (mm.readByte(reg.a5 + W.ch) < 0x23) {
            mm.write(reg.a6 + Dw.NOISE_M, (byte) reg.getD0_B());
        } else {
            mm.write(reg.a6 + Dw.NOISE_S, (byte) reg.getD0_B());
        }
        reg.D1_L = 6;
        mndrv._OPN_WRITE2();
    }

    /**
     * ミキサー設定
     */
    public void _PSG_C9() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a1++));
        _psg_c9_();
    }

    public void _psg_c9_() {
        reg.D3_L = 0;
        reg.setD3_B(mm.readByte(reg.a5 + W.ch));
        //Reg.a0 = _ch_table;
        reg.setD6_B(MnDrv._ch_table[reg.getD3_W() + 0]); // mm.ReadByte(Reg.a0 + reg.getD3_W() + 0);
        reg.setD3_B(MnDrv._ch_table[reg.getD3_W() + 8]); // mm.ReadByte(Reg.a0 + reg.getD3_W() + 8);

        if (reg.getD3_B() < 0x30) {
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a6 + Dw.PSGMIX_M));
        } else {
            reg.setD0_B(mm.readByte(reg.a6 + Dw.PSGMIX_S));
        }
        reg.D2_L = 9;
        reg.setD2_W(reg.getD2_W() << reg.getD6_W());
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        reg.D1_L <<= reg.D6_L;
        reg.setD1_B(reg.getD1_B() ^ reg.getD2_B());
        reg.setD0_B(reg.getD0_B() ^ reg.getD1_B());

        if (reg.getD3_B() < 0x30) {
            mm.write(reg.a6 + Dw.PSGMIX_M, (byte) reg.getD0_B());
        } else {
            mm.write(reg.a6 + Dw.PSGMIX_S, (byte) reg.getD0_B());
        }
        reg.D1_L = 7;
        mndrv._OPN_WRITE2();
    }

    /**
     * ソフトウェアエンベロープ
     */
    public void _PSG_CF() {
        reg.D1_L = 0x1f;
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() & reg.getD1_B());
        mm.write(reg.a5 + W.eenv_ar, (byte) reg.getD0_B());

        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() & reg.getD1_B());
        mm.write(reg.a5 + W.eenv_dr, (byte) reg.getD0_B());

        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() & reg.getD1_B());
        mm.write(reg.a5 + W.eenv_sr, (byte) reg.getD0_B());

        reg.setD1_B(mm.readByte(reg.a1++));
        reg.D0_L = 0xf;
        reg.setD0_B(reg.getD0_B() & reg.getD1_B());
        mm.write(reg.a5 + W.eenv_rr, (byte) reg.getD0_B());

        reg.setD1_B(~reg.getD1_B());
        reg.setD1_B(reg.getD1_B() >> 4);
        mm.write(reg.a5 + W.eenv_sl, (byte) reg.getD1_B());

        reg.D0_L = 0xf;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.eenv_al, (byte) reg.getD0_B());

        mm.write(reg.a5 + W.e_sw, 0x81);

        short sp = (short) reg.getSR_W();
        reg.setSR_W(reg.getSR_W() | 0x700);
        //Reg.a3 = _ex_soft2;
        reg.a3 = Ab.dummyAddress;
        mm.write(reg.a5 + W.softenv_adrs, reg.a3);
        ab.hlw_softenv_adrs.put(reg.a5, this::_ex_soft2);
        reg.setSR_W(sp);
    }

    /**
     * 音量LFO
     */
    public void _PSG_E7() {
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) & 0x7f));
        comcmds._COM_E7();
    }

    /**
     * 音量LFO switch
     */
    public void _PSG_E8() {
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) & 0x7f));
        comcmds._COM_E8();
    }

    /**
     * 音量LFO delay
     */
    public void _PSG_E9() {
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) & 0x7f));
        comcmds._COM_E9();
    }

    /**
     * volume
     */
    public void _PSG_F2() {
        reg.D0_L = 0;
        reg.D1_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.vol, (byte) reg.getD0_B());
// #if false
        reg.setD1_B(reg.getD0_B());
        reg.setD1_B(reg.getD1_B() + 1);
        reg.setD0_W(reg.getD0_W() << 8);
        reg.setD0_W(reg.getD0_W() / reg.getD1_W());
        reg.D0_L = (short) (reg.D0_L / (short) reg.getD1_W()) | (((short) (reg.D0_L % (short) reg.getD1_W())) << 16);
        mm.write(reg.a5 + W.eenv_limit, reg.getD0_B());
// #endif
    }

    public void _psg_f2_softenv() {
        reg.setD1_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD1_B() < 0) {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if ((byte) reg.getD0_B() < 0) {
                reg.D0_L = 0;
            }
        } else {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if (reg.getD0_B() >= 16) {
                reg.D0_L = 15;
            }
        }
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        _psg_lfo();
    }

    public void _psg_lfo() {
        if ((byte) mm.readByte(reg.a5 + W.reverb) >= 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;
        }
        reg.setD0_W(reg.getD0_W() & 0xf);
        reg.a2 = reg.a5 + W.voltable;
        reg.setD0_B(mm.readByte(reg.a2 + (int) (short) reg.getD0_W()));
        mm.write(reg.a5 + W.vol2, (byte) reg.getD0_B());
        reg.D1_L = 8;
        mndrv._OPN_WRITE4();
    }

    /**
     * volup
     */
    public void _PSG_F5() {
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        if (reg.getD0_B() >= 0xf) {
            reg.D0_L = 0xf;
        }
        mm.write(reg.a5 + W.vol, (byte) reg.getD0_B());
        if ((byte) mm.readByte(reg.a5 + W.e_sw) >= 0) {
            _psg_lfo();
        }
    }

    /**
     * voldown
     */
    public void _PSG_F6() {
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a5 + W.vol, (byte) reg.getD0_B());
        if ((byte) mm.readByte(reg.a5 + W.e_sw) >= 0) {
            _psg_lfo();
        }
    }

    /**
     */
    public void _PSG_FF() {
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));

        reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a0 = reg.a6 + Dw.TRACKWORKADR;
        do {
            if ((mm.readByte(reg.a0 + W.flag2) & 1) != 0) break L3;
            reg.a0 = reg.a0 + W._track_work_size;// Dw._trackworksize;
            reg.setD0_W(reg.getD0_W() -1);
        } while (reg.getD0_W() != 0);

        mm.write(reg.a6 + Dw.LOOP_COUNTER, (short) (mm.readShort(reg.a6 + Dw.LOOP_COUNTER) + 1));
        reg.setD1_W(mm.readShort(reg.a6 + Dw.LOOP_COUNTER));
        if (reg.getD1_W() - (-1) == 0) {
            reg.D1_L = 0;
            mm.write(reg.a6 + Dw.LOOP_COUNTER, (short) reg.getD1_W());
        }
        reg.D0_L = 1;
        mndrv.SUBEVENT();

        reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a0 = reg.a6 + Dw.TRACKWORKADR;

        do {
            if ((byte) mm.readByte(reg.a0 + W.flag) < 0) {
                mm.write(reg.a0 + W.flag2, (byte) (mm.readByte(reg.a0 + W.flag2) | 1));
            }
            reg.a0 = reg.a0 + W._track_work_size;
            reg.setD0_W(reg.getD0_W() - 1);
        } while (reg.getD0_W() != 0);

        L3:
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        if (reg.getD0_W() != 0) {
            if (reg.getD0_W() - 0xffff != 0) {
                reg.a1 = reg.a1 + (int) (short) reg.getD0_W();
                return;
            }
        } else {
            mm.write(reg.a5 + W.e_p, 0);
            mm.write(reg.a5 + W.lfo, 0);
            mm.write(reg.a5 + W.weffect, 0);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));
            // pea	_all_end_check(pc)
            _psg_volume_0();
            comcmds._all_end_check();
        }
        reg.a1 = mm.readInt(reg.a5 + W.loop);
    }

    /**
     */
    public void _ch_psg_lfo_job() {
        comwave._ch_effect();
        //_ch_psg_lfo:
        mm.write(reg.a5 + W.addkeycode, (short) 0);
        mm.write(reg.a5 + W.addvolume, (short) 0);

        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if (reg.getD0_B() == 0) return;
        reg.D1_L = 0xe;
        reg.setD1_B(reg.getD1_B() & reg.getD0_B());
        if (reg.getD1_B() != 0) {
            short sp = (short) reg.getD0_W();
            switch (reg.getD1_W()) {
            case 2:
                _ch_psg_plfo_1();
                break;
            case 4:
                _ch_psg_plfo_2();
                break;
            case 6:
                _ch_psg_plfo_3();
                break;
            case 8:
                _ch_psg_plfo_4();
                break;
            case 10:
                _ch_psg_plfo_5();
                break;
            case 12:
                _ch_psg_plfo_6();
                break;
            case 14:
                _ch_psg_plfo_7();
                break;
            }
            reg.setD0_W(sp);
        }
        reg.setD0_B(reg.getD0_B() >> 4);
        reg.setD0_W(reg.getD0_W() & 0x7);

        if (reg.getD0_W() != 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            switch (reg.getD0_W()) {
            case 2:
                _ch_psg_alfo_1();
                break;
            case 4:
                _ch_psg_alfo_2();
                break;
            case 6:
                _ch_psg_alfo_3();
                break;
            case 8:
                _ch_psg_alfo_4();
                break;
            case 10:
                _ch_psg_alfo_5();
                break;
            case 12:
                _ch_psg_alfo_6();
                break;
            case 14:
                _ch_psg_alfo_7();
                break;
            }
        }
        //_ch_psg_lfo_end:
        reg.setD1_W(mm.readShort(reg.a5 + W.addvolume));
        if (reg.getD1_W() == 0) break _ch_psg_a_exit;
        if ((short) reg.getD1_W() < 0) break _ch_psg_a_minus;

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_W(reg.getD0_W() - (int) (short) reg.getD1_W());
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }
        _psg_lfo();
        break _ch_psg_a_exit;

        _ch_psg_a_minus:
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_W(reg.getD0_W() - (int) (short) reg.getD1_W());
        if ((short) (reg.getD0_W() - 0xf) < 0) {
            reg.D0_L = 0xf;
        }
        _psg_lfo();

        _ch_psg_a_exit:
        reg.setD2_B(mm.readByte(reg.a6 + Dw.DRV_FLAG2));
        int f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) break _ch_psg_lfo_end2;
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) break _ch_psg_lfo_end3;

        reg.setD0_W(mm.readShort(reg.a5 + W.freqbase));
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.addkeycode));
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.octave));
        reg.setD0_W(reg.getD0_W() >> reg.getD1_W());
        _set_psg_bend();
        return;

        _ch_psg_lfo_end2:
        reg.setD0_W(mm.readShort(reg.a5 + W.makotune));
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.octave));
        reg.setD0_W(reg.getD0_W() >> reg.getD1_W());
        reg.setD1_W(mm.readShort(reg.a5 + W.detune));

        //; Wed Mar  8 08:23 JST 2000 (saori)
// #if false
        if ((short) reg.getD1_W() < 0) {
            reg.D1_L = 0;
        }
// #endif

        reg.setD1_W(reg.getD1_W() + mm.readShort(reg.a5 + W.addkeycode));
        reg.setD1_W((short) (-(short) reg.getD1_W()));
        reg.setD1_W((short) ((short) reg.getD1_W() >> 2));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD1_W());
        _set_psg_bend();
        return;

        _ch_psg_lfo_end3:
        reg.setD0_W(mm.readShort(reg.a5 + W.makotune));
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.octave));
        reg.setD0_W(reg.getD0_W() >> (int) reg.getD1_W());
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }
        reg.setD0_W(reg.getD0_W() + mm.readShort(reg.a5 + W.addkeycode));
        _set_psg_bend();
    }

    /** */
    public void _ch_psg_plfo_1() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_psg_p_common();
    }

    public void _ch_psg_plfo_2() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_psg_p_common();
    }

    public void _ch_psg_plfo_3() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_psg_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_psg_p_common();
    }

    public void _ch_psg_plfo_4() {
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_psg_p_common();
    }

    public void _ch_psg_plfo_5() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_psg_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_psg_p_common();
    }

    public void _ch_psg_plfo_6() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_psg_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_psg_p_common();
    }

    public void _ch_psg_plfo_7() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_psg_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_psg_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_psg_p_common();
    }

    /** */
    public void _ch_psg_alfo_1() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_psg_a_common();
    }

    public void _ch_psg_alfo_2() {
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_psg_a_common();
    }

    public void _ch_psg_alfo_3() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_psg_a_common();
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_psg_a_common();
    }

    public void _ch_psg_alfo_4() {
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_psg_a_common();
    }

    public void _ch_psg_alfo_5() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_psg_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_psg_a_common();
    }

    public void _ch_psg_alfo_6() {
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_psg_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_psg_a_common();
    }

    public void _ch_psg_alfo_7() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_psg_a_common();
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_psg_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_psg_a_common();
    }

    /** */
    public void _ch_psg_a_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            comwave._com_wavememory();
            mm.write(reg.a5 + W.addvolume, (short) (mm.readShort(reg.a5 + W.addvolume) + (short) reg.getD0_W()));
            return;
        }
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() < 0) {
            int f = reg.getD4_B() & 1;
            reg.setD4_B(reg.getD4_B() >> 1);
            if (f == 0) {
                if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) return;
            } else {
                if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;
            }
        }

        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD0_B());

        //_psg_velocity_pattern:
        switch (reg.getD0_W()) {
        case 2:
            comlfo._com_lfo_saw();
            break;
        case 4:
            comlfo._com_lfo_portament();
            break;
        case 6:
            comlfo._com_lfo_triangle();
            break;
        }

        mm.write(reg.a5 + W.addvolume, (short) (mm.readShort(reg.a5 + W.addvolume) + (short) reg.getD1_W()));
    }

    /** */
    public void _ch_psg_p_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            comwave._com_wavememory();
            mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD0_W()));
            return;
        }
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() < 0) {
            int f = reg.getD4_B() & 1;
            reg.setD4_B(reg.getD4_B() >> 1);
            if (f == 0) {
                if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) return;
            } else {
                if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;
            }
        }

        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD0_B());

        if ((byte) mm.readByte(reg.a6 + Dw.LFO_FLAG) >= 0) {
            //_psg_pitch_pattern:
            switch (reg.getD0_W()) {
            case 2:
                comlfo._com_lfo_saw();
                break;
            case 4:
                comlfo._com_lfo_portament();
                break;
            case 6:
                comlfo._com_lfo_triangle();
                break;
            case 8:
                comlfo._com_lfo_portament();
                break;
            case 10:
                comlfo._com_lfo_triangle();
                break;
            case 12:
                comlfo._com_lfo_triangle();
                break;
            case 14:
                comlfo._com_lfo_oneshot();
                break;
            case 16:
                comlfo._com_lfo_oneshot();
                break;
            }
            mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
            return;
        }

        //_pitch_extend:
        switch (reg.getD0_W()) {
        case 2:
            comlfo._com_lfo_saw();
            break;
        case 4:
            comlfo._com_lfo_portament();
            break;
        case 6:
            comlfo._com_lfo_triangle();
            break;
        case 8:
            comlfo._com_lfo_oneshot();
            break;
        case 10:
            comlfo._com_lfo_square();
            break;
        case 12:
            comlfo._com_lfo_randome();
            break;
        }
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));

    }

    /** */
    public void _ch_psg_mml_job() {
        comanalyze._track_analyze();

        //_ch_psg_bend_job:
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if ((byte) reg.getD0_B() >= 0) return;
        // btst.b	// #1,w_flag2(a5)
        if ((mm.readByte(reg.a5 + W.flag2) & 0x02) != 0) {
            _ch_psg_porta();
            return;
        }
        _ch_psg_bend();
    }

    /**
     * pitch bend
     */
    public void _ch_psg_bend() {
        reg.a4 = reg.a5 + W.p_pattern4;
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) return;

        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
        reg.setD0_W(mm.readShort(reg.a5 + W.freqbase));
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.octave));
        reg.setD2_W(mm.readShort(reg.a4 + W_L.henka));

        if ((short) reg.getD2_W() >= 0) {
            mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) - reg.getD2_W()));
            reg.setD0_W(reg.getD0_W() - (int) (short) reg.getD2_W());
            mm.write(reg.a5 + W.freqbase, (short) reg.getD0_W());
            reg.setD0_W(reg.getD0_W() >> (int) reg.getD1_W());
            if (reg.getD0_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
                _ch_psg_bend_end();
                return;
            }
            _set_psg_bend();
            return;
        }

        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) - reg.getD2_W()));
        reg.setD0_W(reg.getD0_W() - (int) (short) reg.getD2_W());
        mm.write(reg.a5 + W.freqbase, (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() >> (int) reg.getD1_W());
        if (reg.getD0_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
            _ch_psg_bend_end();
            return;
        }
        _set_psg_bend();
    }

    public void _ch_psg_bend_end() {
        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.key2));
        mm.write(reg.a5 + W.key, (byte) reg.getD0_B());
        _psg_freq();
    }

    /**
     * portament
     */
    public void _ch_psg_porta() {
        reg.a4 = reg.a5 + W.p_pattern4;
        mm.write(reg.a4 + W_L.count, (byte) (mm.readByte(reg.a4 + W_L.count) - 1));
        if (mm.readByte(reg.a4 + W_L.count) == 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfd));
            return;
        }
        reg.setD2_W(mm.readShort(reg.a4 + W_L.henka));

        reg.setD0_W(mm.readShort(reg.a5 + W.keycode));
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD2_W()));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD2_W());

        if (mm.readShort(reg.a4 + W_L.henka_work) != 0) { // break _ch_psg_porta_common;
            if (mm.readShort(reg.a4 + W_L.henka_work) >= 0) { // break _ch_psg_porta_minus;

                mm.write(reg.a4 + W_L.henka_work, (short) (mm.readShort(reg.a4 + W_L.henka_work) - 1));
                mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + 1));
                reg.setD0_W(reg.getD0_W() + 1);
//                break _ch_psg_porta_common;
            } else {
// _ch_psg_porta_minus:
                mm.write(reg.a4 + W_L.henka_work, (short) (mm.readShort(reg.a4 + W_L.henka_work) + 1));
                mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) - 1));
                reg.setD0_W(reg.getD0_W() - 1);
            }
        }
// _ch_psg_porta_common:
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.octave));
        reg.setD2_W(reg.getD0_W());
        reg.setD2_W(reg.getD2_W() << reg.getD1_W());
        mm.write(reg.a5 + W.freqbase, (short) reg.getD2_W());
        mm.write(reg.a5 + W.freqwork, (short) reg.getD0_W());
        mm.write(reg.a5 + W.makotune, (short) reg.getD2_W());
        _set_psg_bend();
    }

    /**
    //
     */
    public void _soft2() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        if (reg.getD0_B() == 0) return;
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.vol2));
        //_soft2_al:
        reg.setD0_B(reg.getD0_B() - 1);//1
        if (reg.getD0_B() == 0) {
            mm.write(reg.a5 + W.e_alw, (byte) (mm.readByte(reg.a5 + W.e_alw) - 1));
            if (mm.readByte(reg.a5 + W.e_alw) != 0) break _soft2_ok;
            mm.write(reg.a5 + W.e_p, 2);
            break _soft2_ok;
        }
        reg.setD0_B(reg.getD0_B() - 1);//2
        if (reg.getD0_B() == 0) {
            if (mm.readByte(reg.a5 + W.e_dd) == 0) break _soft2_ok;//dr=0 は減衰無し
            mm.write(reg.a5 + W.e_p, 3);
            reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a5 + W.e_dd));
            if ((byte) reg.getD1_B() >= 0) break _soft2_ok;
            reg.D1_L = 0;
            mm.write(reg.a5 + W.e_p, 4);
            break _soft2_ok;
        }
        //_soft2_sr:
        reg.setD0_B(reg.getD0_B() - 1);//3
        if (reg.getD0_B() == 0) {
            mm.write(reg.a5 + W.e_srw, (byte) (mm.readByte(reg.a5 + W.e_srw) - 1));
            if (mm.readByte(reg.a5 + W.e_srw) != 0) break _soft2_ok;
            mm.write(reg.a5 + W.e_srw, mm.readByte(reg.a5 + W.e_sr));
            boolean cf = reg.getD1_B() < 1;
            reg.setD1_B(reg.getD1_B() - 1);
            if (!cf) break _soft2_ok;
            reg.D1_L = 0;
            mm.write(reg.a5 + W.e_p, 4);
            break _soft2_ok;
        }
        //_soft2_rr:
        reg.setD0_B(reg.getD0_B() - 1);//4
        if (reg.getD0_B() == 0) {
            if (mm.readByte(reg.a5 + W.e_rr) != 0)// rr = 0 は消音
            {
                mm.write(reg.a5 + W.e_rrw, (byte) (mm.readByte(reg.a5 + W.e_rrw) - 1));
                if (mm.readByte(reg.a5 + W.e_rrw) != 0) break _soft2_ok;
                mm.write(reg.a5 + W.e_rrw, mm.readByte(reg.a5 + W.e_rr));
                reg.setD1_B(reg.getD1_B() - 1);
                break _soft2_ok;
            }
            reg.D1_L = 0;
            mm.write(reg.a5 + W.e_p, 4);
            break _soft2_ok;
        }
        //_soft2_ko:
        mm.write(reg.a5 + W.e_p, 1);
        mm.write(reg.a5 + W.e_alw, mm.readByte(reg.a5 + W.e_al));
        mm.write(reg.a5 + W.e_srw, mm.readByte(reg.a5 + W.e_sr));
        mm.write(reg.a5 + W.e_rrw, mm.readByte(reg.a5 + W.e_rr));
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        _soft3_0();
        return;

        _soft2_ok:
        reg.setD0_B(reg.getD1_B());
        _soft3_0();
    }

    /**
     * extend software envelop
     */
    public void _ex_soft2() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        if (reg.getD0_B() == 0) return;
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.vol2));
        //_soft3_ar:
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() != 0) break esm_dr_check;
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_arc));
        reg.setD2_B(reg.getD2_B() - 1);
        if ((byte) reg.getD2_B() < 0) break arc_count_check;
        reg.setD2_B(reg.getD2_B() + 1);
        mm.write(reg.a5 + W.eenv_volume, (byte) (mm.readByte(reg.a5 + W.eenv_volume) + (byte) reg.getD2_B()));
        if (mm.readByte(reg.a5 + W.eenv_volume) >= 15) break esm_ar_next;
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_ar));
        reg.setD2_B(reg.getD2_B() - 16);
        mm.write(reg.a5 + W.eenv_arc, (byte) reg.getD2_B());
        break _soft3_ok;

        esm_ar_next:
        mm.write(reg.a5 + W.eenv_volume, 15);
        mm.write(reg.a5 + W.e_p, 2);//DR
        if (mm.readByte(reg.a5 + W.eenv_sl) - 15 != 0) break _soft3_ok;
        mm.write(reg.a5 + W.e_p, 3);//SR
        break _soft3_ok;

        arc_count_check:
        if (mm.readByte(reg.a5 + W.eenv_ar) == 0) break _soft3_ok;
        mm.write(reg.a5 + W.eenv_arc, (byte) (mm.readByte(reg.a5 + W.eenv_arc) + 1));
        break _soft3_ok;

        esm_dr_check:
        reg.setD0_B(reg.getD0_B() - 1);//2
        if (reg.getD0_B() != 0) break esm_sr_check;

        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_drc));
        reg.setD2_B(reg.getD2_B() - 1);
        if ((byte) reg.getD2_B() < 0) break drc_count_check;
        reg.setD2_B(reg.getD2_B() + 1);
        boolean cf = mm.readByte(reg.a5 + W.eenv_volume) < reg.getD2_B();
        mm.write(reg.a5 + W.eenv_volume, (byte) (mm.readByte(reg.a5 + W.eenv_volume) - reg.getD2_B()));
        //int sp = Reg.SR_W;
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_sl));
        //Reg.SR_W = (Reg.SR_W & 0xff00) | (sp & 0xff);
        if (cf) break dr_slset;
        reg.setD3_B(mm.readByte(reg.getD5_B() + W.eenv_volume));
        if (reg.getD3_B() < reg.getD2_B()) break dr_slset;

        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_dr));
        reg.setD2_B(reg.getD2_B() - 16);
        if ((byte) reg.getD2_B() >= 0) break esm_dr_notx;
        reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD2_B());

        esm_dr_notx:
        mm.write(reg.a5 + W.eenv_drc, (byte) reg.getD2_B());
        break _soft3_ok;

        dr_slset:
        mm.write(reg.a5 + W.eenv_volume, (byte) reg.getD2_B());
        mm.write(reg.a5 + W.e_p, 3);
        break _soft3_ok;

        drc_count_check:
        if (mm.readByte(reg.a5 + W.eenv_dr) == 0) break _soft3_ok;
        mm.write(reg.a5 + W.eenv_drc, (byte) (mm.readByte(reg.a5 + W.eenv_drc) + 1));
        break _soft3_ok;

        esm_sr_check:
        reg.setD0_B(reg.getD0_B() - 1);//3
        if (reg.getD0_B() != 0) break esm_rr;

        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_src));
        reg.setD2_B(reg.getD2_B() - 1);
        if ((byte) reg.getD2_B() < 0) break src_count_check;
        reg.setD2_B(reg.getD2_B() + 1);
        cf = mm.readByte(reg.a5 + W.eenv_volume) < reg.getD2_B();
        mm.write(reg.a5 + W.eenv_volume, (byte) (mm.readByte(reg.a5 + W.eenv_volume) - reg.getD2_B()));
        if (!cf) break esm_sr_exit;
        mm.write(reg.a5 + W.eenv_volume, 0);

        esm_sr_exit:
        mm.write(reg.a5 + W.eenv_sr, (byte) reg.getD2_B());
        reg.setD2_B(reg.getD2_B() - 16);
        if ((byte) reg.getD2_B() >= 0) break esm_sr_notx;
        reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD2_B());

        esm_sr_notx:
        mm.write(reg.a5 + W.eenv_src, (byte) reg.getD2_B());
        break _soft3_ok;

        src_count_check:
        if (mm.readByte(reg.a5 + W.eenv_sr) == 0) break _soft3_ok;
        mm.write(reg.a5 + W.eenv_src, (byte) (mm.readByte(reg.a5 + W.eenv_src) + 1));
        break _soft3_ok;

        esm_rr:
        reg.setD0_B(reg.getD0_B() - 1);//4
        if (reg.getD0_B() != 0) break esm_ko;

        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_rrc));
        if (reg.getD2_B() == 0) break rrc_count_check;
        cf = mm.readByte(reg.a5 + W.eenv_volume) < reg.getD2_B();
        mm.write(reg.a5 + W.eenv_volume, (byte) (mm.readByte(reg.a5 + W.eenv_volume) - reg.getD2_B()));
        if (!cf) break esm_rr_exit;
        mm.write(reg.a5 + W.eenv_volume, 0);

        esm_rr_exit:
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_rr));
        reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD2_B());
        reg.setD2_B(reg.getD2_B() - 16);
        mm.write(reg.a5 + W.eenv_rrc, (byte) reg.getD2_B());
        break _soft3_ok;

        rrc_count_check:
        if (mm.readByte(reg.a5 + W.eenv_rr) == 0) break _soft3_ok;
        mm.write(reg.a5 + W.eenv_rrc, (byte) (mm.readByte(reg.a5 + W.eenv_rrc) + 1));
        break _soft3_ok;

        esm_ko:
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() != 0) {
            _psg_volume_0();
            return;
        }

        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_ar));
        reg.setD2_B(reg.getD2_B() - 16);
        mm.write(reg.a5 + W.eenv_arc, (byte) reg.getD2_B());
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_dr));
        reg.setD2_B(reg.getD2_B() - 16);
        if ((byte) reg.getD2_B() < 0) { // break eei_dr_notx;
            reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD2_B());
        }
// eei_dr_notx:
        mm.write(reg.a5 + W.eenv_drc, (byte) reg.getD2_B());
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_sr));
        reg.setD2_B(reg.getD2_B() - 16);
        if ((byte) reg.getD2_B() < 0) { // break eei_sr_notx;
            reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD2_B());
        }
// eei_sr_notx:
        mm.write(reg.a5 + W.eenv_src, (byte) reg.getD2_B());
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_rr));
        reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD2_B());
        reg.setD2_B(reg.getD2_B() - 16);
        mm.write(reg.a5 + W.eenv_rrc, (byte) reg.getD2_B());
        reg.setD2_B(mm.readByte(reg.a5 + W.eenv_al));
        mm.write(reg.a5 + W.eenv_volume, (byte) reg.getD2_B());
        mm.write(reg.a5 + W.e_p, 1);
        //mm.Write(Reg.a5 + W.eenv_sl, mm.ReadByte(Reg.a5 + W.vol));

        _soft3_ok:
        //
        // 拡張版 音量=dl*(eenv_vol+1)/16
        //
        reg.D0_L = 0;
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.eenv_volume));
        if (reg.getD1_B() == 0) {
            _soft3_0();
            return;
        }
        reg.setD1_B(reg.getD1_B() + 1);
        reg.setD0_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_W(reg.getD0_W() * reg.getD1_W());
        int f = reg.getD0_B() & 0x8;
        reg.setD0_B(reg.getD0_B() >> 4);
        if (f != 0) {
            reg.setD0_B(reg.getD0_B() + 1);
        }

        _soft3_0();
    }

    public void _soft3_0() {
        reg.setD2_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD2_B() < 0) {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD2_B());
            if ((byte) reg.getD0_B() < 0) {
                reg.D0_L = 0;
            }
        } else {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD2_B());
            if (reg.getD0_B() >= 16) {
                reg.D0_L = 15;
            }
        }
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        if ((byte) reg.getD0_B() >= 0) {
            _psg_volume_set3();
            return;
        }
        reg.D0_L = 0;
        _psg_volume_set3();
    }

    /**
     * extend software envelop
     */
    public void _ex_soft4() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_p));
        if (reg.getD0_B() == 0) break _ex_soft4_end;

        //_ex_soft4_ar:				// 1
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() != 0) break _ex_soft4_dr;

        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a5 + W.e_ar));
        if (reg.getD0_B() < mm.readByte(reg.a5 + W.eenv_limit)) break _ex_soft4_ok;
        reg.setD0_B(mm.readByte(reg.a5 + W.eenv_limit));
        mm.write(reg.a5 + W.e_p, 2);
        break _ex_soft4_ok;

        _ex_soft4_dr:
        // 2
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() != 0) break _ex_soft4_sr;

        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
        Boolean cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_dr);
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_dr));
        if (cf) break _ex_soft4_dr2;
        if (reg.getD0_B() >= mm.readByte(reg.a5 + W.e_sl)) break _ex_soft4_ok;

        _ex_soft4_dr2:
        reg.setD0_B(mm.readByte(reg.a5 + W.e_sl));
        mm.write(reg.a5 + W.e_p, 3);
        break _ex_soft4_ok;

        _ex_soft4_sr:
        // 3
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() != 0) break _ex_soft4_rr;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
        cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_sr);
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_sr));
        if ((byte) reg.getD0_B() == 0) break _ex_soft4_end;
        if (!cf) break _ex_soft4_ok;
        break _ex_soft4_end;

        _ex_soft4_rr:
        // 4
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() != 0) break _ex_soft4_ko;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
        cf = reg.getD0_B() < mm.readByte(reg.a5 + W.e_rr);
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a5 + W.e_rr));
        if ((byte) reg.getD0_B() == 0) break _ex_soft4_end;
        if (!cf) break _ex_soft4_ok;
        if (cf) break _ex_soft4_end;

        _ex_soft4_ko:
        // 5
        mm.write(reg.a5 + W.e_p, 1);
        reg.setD0_B(mm.readByte(reg.a5 + W.e_sv));
        if (reg.getD0_B() < mm.readByte(reg.a5 + W.eenv_limit)) break _ex_soft4_ok;
        reg.setD0_B(mm.readByte(reg.a5 + W.eenv_limit));

        _ex_soft4_ok:
        mm.write(reg.a5 + W.e_sub, (byte) reg.getD0_B());
        break _ex_soft4_volume_set;

        _ex_soft4_end:
        reg.D0_L = 0;
        mm.write(reg.a5 + W.e_sub, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.e_p, (byte) reg.getD0_B());

        _ex_soft4_volume_set:
        reg.D0_L = 0;
        reg.D1_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.e_sub));
        if (reg.getD0_B() == 0) {
            _soft3_0();
            return;
        }
        reg.setD1_B(mm.readByte(reg.a5 + W.vol));
        reg.setD1_B(reg.getD1_B() + 1);
        reg.setD0_W(reg.getD0_W() * reg.getD1_W());
        reg.D0_L = reg.getD0_W() >> 8;
        _soft3_0();
    }
}
