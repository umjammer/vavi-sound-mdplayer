package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


public class DevMPcm {
    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public ComAnalyze comanalyze;
    public ComCmds comcmds;
    public ComLfo comlfo;
    public ComWave comwave;
    public DevOpm devopm;

    //
    //	part of MPCM
    //
    //─────────────────────────────────────
    public void _mpcm_note_set() {
        mm.write(reg.a5 + W.key, (byte) reg.getD0_B());
        reg.D2_L = 0;
        reg.setD2_B(reg.getD0_B());

        if (mm.readByte(reg.a5 + W.pcm_tone) == 0) {
            _mpcm_note_keyon();
            return;
        }

        reg.setD2_W(reg.getD2_W() << 6);
        reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD2_W() < 0) {
            reg.D2_L = 0;
        }

        mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());

        // pea _mpcm_keyon(pc)
        _mpcm_freq();
        comlfo._init_lfo();
        _init_lfo_mpcm();
        _mpcm_keyon();
    }

    //─────────────────────────────────────
    //
    public void _init_lfo_mpcm() {
        if (mm.readByte(reg.a6 + Dw.FADEFLAG) == 0) {
            reg.D5_L = 0x70;
            reg.setD5_B(reg.getD5_B() & mm.readByte(reg.a5 + W.lfo));
            if (reg.getD5_B() == 0) return;

            reg.setD4_B(mm.readByte(reg.a5 + W.vol));
            _MPCM_F2_lfo();
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

    //─────────────────────────────────────
    //
    public void _mpcm_freq() {
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
        reg.setD1_W(reg.getD2_W());

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
            reg.setD0_W(0x400);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
    }

    //─────────────────────────────────────
    //	NOTE KEY ON
    //
    public void _mpcm_note_keyon_pdx() {
        reg.D5_L = mm.readInt(reg.a6 + Dw.PCMBUFADR);
        if (reg.D5_L == 0) {
            _mpcm_note_keyon_set_noload();
            return;
        }
        reg.a2 = reg.D5_L;
        reg.D4_L = 0;
        reg.D5_L = 0;
        reg.setD5_B(mm.readByte(reg.a5 + W.bank));
        reg.setD0_B(reg.getD5_B());
        reg.setD0_W(reg.getD0_W() << 8);
        reg.setD0_B(reg.getD2_B());

        if (reg.getD0_W() - mm.readShort(reg.a5 + W.banktone) != 0) {
            mm.write(reg.a5 + W.banktone, (short) reg.getD0_W());
            reg.setD4_B(reg.getD2_B());
            reg.setD5_W(reg.getD5_W() << 5);
            reg.D4_L += reg.D5_L;
            reg.D5_L += reg.D5_L;
            reg.D4_L += reg.D5_L;
            reg.D4_L <<= 3;
            reg.D0_L = mm.readInt(reg.a2 + reg.D4_L);
            reg.D1_L = mm.readInt(reg.a2 + reg.D4_L + 4);
            reg.D0_L += reg.a2;
            if (reg.D0_L < reg.a2) {
                _mpcm_note_keyon_set_noload();
                return;
            }
            if (reg.D0_L >= mm.readInt(reg.a6 + Dw.PCMBUF_ENDADR)) {
                _mpcm_note_keyon_set_noload();
                return;
            }

            reg.a2 = mm.readInt(reg.a6 + Dw.MPCMWORKADR);
            mm.write(reg.a2 + P.SEL, mm.readByte(reg.a5 + W.pcmmode));
            mm.write(reg.a2 + P.ADDRESS, reg.D0_L);
            mm.write(reg.a2 + P.LENGTH, reg.D1_L);
            mm.write(reg.a2 + P.LOOP_END, reg.D1_L);
            reg.a2 += 2;
            int s = reg.a1;
            reg.a1 = reg.a2;
            reg.a2 = s;
            reg.setD0_W(0x200);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            reg.D1_L = 0;
            mndrv.trap(1);
            reg.a1 = reg.a2;

            reg.setD2_W(reg.getD2_W() << 6);
            mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
            mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
        }
        _mpcm_pdx_keyon();
    }

    public void _mpcm_pdx_keyon() {
        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) == 0) {
            _mpcm_keyon();
            return;
        }

        reg.setD1_B(mm.readByte(reg.a5 + W.pan_ampm));
        reg.setD0_W(0x600);
        reg.setD0_B(mm.readByte(reg.a5 + W.dev));
        mndrv.trap(1);
        _mpcm_keyon();
    }

    //─────────────────────────────────────
    //	NOTE KEY ON
    //
    public void _mpcm_note_keyon() {
        reg.setD5_B(mm.readByte(reg.a6 + Dw.DRV_FLAG));
        if ((reg.D5_L & 0x2) == 0) {
            _mpcm_note_keyon_set_noload();
            return;
        }

        if ((reg.D5_L & 0x10) != 0) {
            _mpcm_note_keyon_pdx();
            return;
        }

        reg.D5_L = 0;
        reg.setD5_B(mm.readByte(reg.a5 + W.bank));
        reg.setD5_W(reg.getD5_W() << 7);
        reg.setD5_B(reg.getD5_B() | reg.getD2_B());

        if (reg.getD5_W() - mm.readShort(reg.a5 + W.banktone) == 0) {
            _mpcm_keyon();
            return;
        }

        reg.a2 = mm.readInt(reg.a6 + Dw.MPCMWORKADR);
        reg.D0_L = mm.readInt(reg.a6 + Dw.ZPDCOUNT);
        if (reg.D0_L == 0) {
            _mpcm_note_keyon_set_noload();
            return;
        }

        do {
            if (reg.getD5_W() - mm.readShort(reg.a2) == 0) {
                reg.a2 += 2;
                mm.write(reg.a5 + W.banktone, (short) reg.getD5_W());
                _mpcm_note_keyon_set();
                return;
            }

            reg.a2 = reg.a2 + P._pcm_work_size;
            reg.D0_L--;
        } while (reg.D0_L != 0);
    }

    public void _mpcm_note_keyon_set() {
        int s = reg.a1;
        reg.a1 = reg.a2;
        reg.a2 = s;
        reg.setD0_W(0x200);
        reg.setD0_B(mm.readByte(reg.a5 + W.dev));
        reg.D1_L = 0;
        mndrv.trap(1);
        reg.a1 = reg.a2;

        reg.setD2_W(reg.getD2_W() << 6);
        mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
        _mpcm_keyon();
    }

    // key on へ
    //─────────────────────────────────────
    //	KEY ON
    //
    public void _mpcm_keyon() {
        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
            comwave._wave_init_kon();
            mm.write(reg.a5 + W.revexec, 0x00);
            if ((byte) mm.readByte(reg.a5 + W.flag2) >= 0) {
                _mpcm_keyon_nomask();
                return;
            }

            reg.setD0_W(0x100);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
    }

    public void _mpcm_keyon_nomask() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x20));
        if ((mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) return;
        }

        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xfb));

        if ((byte) mm.readByte(reg.a5 + W.reverb) < 0) {
            _mpcm_echo_ret();
        }
        mm.write(reg.a5 + W.e_p, 5);

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
    }

    //─────────────────────────────────────
    public void _mpcm_note_keyon_set_noload() {
        reg.setD2_W(reg.getD2_W() << 6);
        mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
    }

    //─────────────────────────────────────
    //	KEY OFF
    //
    public void _mpcm_keyoff() {
        comwave._wave_init_kof();
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));

        if (mm.readByte(reg.a5 + W.kom) != 0) {
            mm.write(reg.a5 + W.e_p, 4);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
            return;
        }
        _mpcm_keyoff2();
    }

    public void _mpcm_keyoff2() {
        if ((byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
            reg.setD0_W(0x100);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
        mm.write(reg.a5 + W.e_p, 4);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));
    }

    //─────────────────────────────────────
    //
    public void _mpcm_echo() {
        if ((byte) (mm.readByte(reg.a5 + W.flag) & 0x20) == 0) return;

        int sp = reg.getD4_W();

        mm.write(reg.a5 + W.revexec, 0xff);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x98));

        mm.write(reg.a5 + W.reverb_time_work, mm.readByte(reg.a5 + W.reverb_time));

        reg.D5_L = 7;
        reg.setD5_B(reg.getD5_B() & mm.readByte(reg.a5 + W.reverb));
        reg.setD5_W(reg.getD5_W() + 1);
        reg.setD5_W(reg.getD5_W() + (int) (short) reg.getD5_W());

        //_mpcm_echo_table:
        switch (reg.getD5_W() / 2) {
        case 1:
            _mpcm_echo_volume();
            break;
        case 2:
            _mpcm_echo_volume_pan();
            break;
        case 3:
            _mpcm_echo_volume_tone();
            break;
        case 4:
            _mpcm_echo_volume_pan_tone();
            break;
        case 5:
            _mpcm_echo_volume_();
            break;
        }

        reg.setD4_W(sp);
    }

    //─────────────────────────────────────
    public void _mpcm_echo_volume() {
        if ((byte) (mm.readByte(reg.a5 + W.reverb) & 0x10) != 0) {
            _mpcm_echo_common_atv();
            return;
        }
        if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _mpcm_echo_common_atv();
            return;
        }
        _mpcm_echo_common_v();
    }

    public void _mpcm_echo_volume_pan_tone() {
        _mpcm_echo_tone();
        _mpcm_echo_volume_pan();
    }

    public void _mpcm_echo_volume_pan() {
        _mpcm_echo_pan();
        _mpcm_echo_volume();
    }

    public void _mpcm_echo_volume_tone() {
        _mpcm_echo_tone();
        _mpcm_echo_volume();
    }

    public void _mpcm_echo_volume_() {
        if ((byte) (mm.readByte(reg.a5 + W.reverb) & 0x10) != 0) {
            _mpcm_echo_volume_atv();
            return;
        }
        if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _mpcm_echo_volume_atv();
            return;
        }
        _mpcm_echo_volume_v();
    }

    //─────────────────────────────────────
    public void _mpcm_echo_pan() {
        if ((byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) == 0) return;
        reg.setD1_B(mm.readByte(reg.a5 + W.reverb_pan));
        reg.setD0_W(0x600);
        reg.setD0_B(mm.readByte(reg.a5 + W.dev));
        mndrv.trap(1);
    }

    //─────────────────────────────────────
    public void _mpcm_echo_tone() {
        reg.D5_L = 0;
        reg.setD5_B(mm.readByte(reg.a5 + W.reverb_tone));
        _mpcm_echo_tone_change();
    }

    //─────────────────────────────────────
    // v通常
    //
    public void _mpcm_echo_common_v() {
        if ((byte) (mm.readByte(reg.a5 + W.reverb) & 0x08) != 0) {
            _mpcm_echo_direct_v();
            return;
        }

        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));

        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD0_B() >= 0) {
            _mpcm_echo_plus_v();
            return;
        }

        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _MPCM_F2_softenv();
    }

    public void _mpcm_echo_plus_v() {
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0x7f;
        }
        if (reg.getD4_B() >= mm.readByte(reg.a5 + W.volcount)) {
            reg.setD4_B(mm.readByte(reg.a5 + W.volcount));
            reg.setD4_B(reg.getD4_B() - 1);
        }

        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    // @v通常
    //
    public void _mpcm_echo_common_atv() {
        if ((byte) (mm.readByte(reg.a5 + W.reverb) & 0x08) != 0) {
            _mpcm_echo_direct_atv();
            return;
        }

        reg.setD4_B(mm.readByte(reg.a5 + W.vol));

        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD0_B() >= 0) {
            _mpcm_echo_plus();
            return;
        }

        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() >= 0) {
            _MPCM_F2_softenv();
            return;
        }
        reg.D4_L = 0;
        _MPCM_F2_softenv();
    }

    public void _mpcm_echo_plus() {
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() >= 0) {
            _MPCM_F2_softenv();
            return;
        }
        reg.D4_L = 0x7f;
        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    // v微調整
    //
    public void _mpcm_echo_volume_v() {
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));
        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        if ((byte) reg.getD0_B() >= 0) {
            _mpcm_echo_vol_plus();
            return;
        }

        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        reg.setD4_B(reg.getD4_B() >> 1);
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _MPCM_F2_softenv();
    }

    public void _mpcm_echo_vol_plus() {
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        reg.setD4_B(reg.getD4_B() >> 1);
        reg.setD4_W(reg.getD4_W() & 0x7f);
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    // @v微調整
    //
    public void _mpcm_echo_volume_atv() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_vol));
        reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
        reg.setD4_B(reg.getD4_B() >> 1);
        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    // v直接
    //
    public void _mpcm_echo_direct_v() {
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.reverb_vol));
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    // @v直接
    //
    public void _mpcm_echo_direct_atv() {
        reg.setD4_B(mm.readByte(reg.a5 + W.reverb_vol));
        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    public void _mpcm_echo_ret() {
        int sp = reg.getD4_W();

        mm.write(reg.a5 + W.revexec, 0);
        mm.write(reg.a5 + W.reverb_time_work, 0);

        if ((mm.readByte(reg.a5 + W.reverb) & 0x02) != 0) {
            reg.setD5_B(mm.readByte(reg.a5 + W.program2));
            _mpcm_echo_tone_change();
        }
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) != 0) { // break _mpcm_echo_ret_atv;
            reg.D4_L = 0;
            reg.setD4_B(mm.readByte(reg.a5 + W.volume));
            reg.a0 = reg.a5 + W.voltable;
            reg.setD4_B(mm.readByte(reg.a0 + (int) (short) reg.getD4_W()));
        }
// _mpcm_echo_ret_atv:

        _MPCM_F2_softenv();

        reg.setD5_B(mm.readByte(reg.a5 + W.reverb));
        int f = reg.getD5_B() & 1;
        reg.setD5_B(reg.getD5_B() >> 1);
        if (f != 0) {
            if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
                reg.setD1_B(mm.readByte(reg.a5 + W.pan_ampm));
                reg.setD0_W(0x600);
                reg.setD0_B(mm.readByte(reg.a5 + W.dev));
                mndrv.trap(1);
            }
        }
        reg.setD4_W(sp);
    }

    //─────────────────────────────────────
    //	MML コマンド処理 ( PCM 部 )
    //
    public void _mpcm_command() {
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());

        //_mpcmc:
        switch (reg.getD0_W() / 2) {
        case 0x00:
            break;//
        case 0x01:
            comcmds._COM_81();
            break;// 81
        case 0x02:
            _MPCM_82();
            break;// 82	key off
        case 0x03:
            comcmds._COM_83();
            break;// 83	すらー
        case 0x04:
            _MPCM_NOP();
            break;// 84
        case 0x05:
            _MPCM_NOP();
            break;// 85
        case 0x06:
            comcmds._COM_86();
            break;// 86	同期信号送信
        case 0x07:
            comcmds._COM_87();
            break;// 87	同期信号待ち
        case 0x08:
            devopm._OPM_88();
            break;// 88	ぴっちべんど
        case 0x09:
            devopm._OPM_89();
            break;// 89	ぽるためんと
        case 0x0a:
            devopm._OPM_8A();
            break;// 8A	ぽるためんと係数変更
        case 0x0b:
            _MPCM_NOP();
            break;// 8B
        case 0x0c:
            _MPCM_NOP();
            break;// 8C
        case 0x0d:
            _MPCM_NOP();
            break;// 8D
        case 0x0e:
            _MPCM_NOP();
            break;// 8E
        case 0x0f:
            _MPCM_NOP();
            break;// 8F

        case 0x10:
            comcmds._COM_90();
            break;// 90	q
        case 0x11:
            comcmds._COM_91();
            break;// 91	@q
        case 0x12:
            comcmds._COM_94();
            break;// 92	ノートオフモード
        case 0x13:
            comcmds._COM_93();
            break;// 93	negative @q
        case 0x14:
            comcmds._COM_94();
            break;// 94	keyoff mode
        case 0x15:
            _MPCM_NOP();
            break;// 95
        case 0x16:
            _MPCM_NOP();
            break;// 96
        case 0x17:
            _MPCM_NOP();
            break;// 97
        case 0x18:
            _MPCM_98();
            break;// 98	擬似リバーブ
        case 0x19:
            _MPCM_99();
            break;// 99
        case 0x1a:
            comcmds._COM_9A();
            break;// 9A	擬似動作 step time
        case 0x1b:
            _MPCM_NOP();
            break;// 9B
        case 0x1c:
            _MPCM_NOP();
            break;// 9C
        case 0x1d:
            _MPCM_NOP();
            break;// 9D
        case 0x1e:
            _MPCM_NOP();
            break;// 9E
        case 0x1f:
            _MPCM_NOP();
            break;// 9F

        case 0x20:
            _MPCM_F0();
            break;// A0	音色切り替え
        case 0x21:
            _MPCM_A1();
            break;// A1	バンク&音色切り替え
        case 0x22:
            _MPCM_A2();
            break;// A2	モード切り替え
        case 0x23:
            _MPCM_A3();
            break;// A3	音量テーブル
        case 0x24:
            _MPCM_F2();
            break;// A4	音量
        case 0x25:
            _MPCM_F5();
            break;// A5
        case 0x26:
            _MPCM_F6();
            break;// A6
        case 0x27:
            _MPCM_A7();
            break;// A7	127段階音量テーブル切り替え
        case 0x28:
            comcmds._COM_A8();
            break;// A8	相対音量モード
        case 0x29:
            _MPCM_NOP();
            break;// A9
        case 0x2a:
            _MPCM_NOP();
            break;// AA
        case 0x2b:
            _MPCM_NOP();
            break;// AB
        case 0x2c:
            _MPCM_NOP();
            break;// AC
        case 0x2d:
            _MPCM_NOP();
            break;// AD
        case 0x2e:
            _MPCM_NOP();
            break;// AE
        case 0x2f:
            _MPCM_NOP();
            break;// AF

        case 0x30:
            comcmds._COM_B0();
            break;// B0
        case 0x31:
            _MPCM_NOP();
            break;// B1
        case 0x32:
            _MPCM_NOP();
            break;// B2
        case 0x33:
            _MPCM_NOP();
            break;// B3
        case 0x34:
            _MPCM_NOP();
            break;// B4
        case 0x35:
            _MPCM_NOP();
            break;// B5
        case 0x36:
            _MPCM_NOP();
            break;// B6
        case 0x37:
            _MPCM_NOP();
            break;// B7
        case 0x38:
            _MPCM_NOP();
            break;// B8
        case 0x39:
            _MPCM_NOP();
            break;// B9
        case 0x3a:
            _MPCM_NOP();
            break;// BA
        case 0x3b:
            _MPCM_NOP();
            break;// BB
        case 0x3c:
            _MPCM_NOP();
            break;// BC
        case 0x3d:
            _MPCM_NOP();
            break;// BD
        case 0x3e:
            comcmds._COM_BE();
            break;// BE	ジャンプ
        case 0x3f:
            comcmds._COM_BF();
            break;// BF

        // PSG 系
        case 0x40:
            comcmds._COM_C0();
            break;// C0	ソフトウェアエンベロープ 1
        case 0x41:
            comcmds._COM_C1();
            break;// C1	ソフトウェアエンベロープ 2
        case 0x42:
            _MPCM_NOP();
            break;// C2
        case 0x43:
            comcmds._COM_C3();
            break;// C3	switch
        case 0x44:
            comcmds._COM_C4();
            break;// C4	env (num)
        case 0x45:
            comcmds._COM_C5();
            break;// C5	env (bank + num)
        case 0x46:
            _MPCM_NOP();
            break;// C6
        case 0x47:
            _MPCM_NOP();
            break;// C7
        case 0x48:
            _MPCM_NOP();
            break;// C8
        case 0x49:
            _MPCM_NOP();
            break;// C9
        case 0x4a:
            _MPCM_NOP();
            break;// CA
        case 0x4b:
            _MPCM_NOP();
            break;// CB
        case 0x4c:
            _MPCM_NOP();
            break;// CC
        case 0x4d:
            _MPCM_NOP();
            break;// CD
        case 0x4e:
            _MPCM_NOP();
            break;// CE
        case 0x4f:
            _MPCM_NOP();
            break;// CF

        // KEY 系
        case 0x50:
            comcmds._COM_D0();
            break;// D0	キートランスポーズ
        case 0x51:
            comcmds._COM_D1();
            break;// D1	相対キートランスポーズ
        case 0x52:
            _MPCM_NOP();
            break;// D2
        case 0x53:
            _MPCM_NOP();
            break;// D3
        case 0x54:
            _MPCM_NOP();
            break;// D4
        case 0x55:
            _MPCM_NOP();
            break;// D5
        case 0x56:
            _MPCM_NOP();
            break;// D6
        case 0x57:
            _MPCM_NOP();
            break;// D7
        case 0x58:
            comcmds._COM_D8();
            break;// D8	ディチューン
        case 0x59:
            comcmds._COM_D9();
            break;// D9	相対ディチューン
        case 0x5a:
            _MPCM_NOP();
            break;// DA
        case 0x5b:
            _MPCM_NOP();
            break;// DB
        case 0x5c:
            _MPCM_NOP();
            break;// DC
        case 0x5d:
            _MPCM_NOP();
            break;// DD
        case 0x5e:
            _MPCM_NOP();
            break;// DE
        case 0x5f:
            _MPCM_NOP();
            break;// DF

        // LFO 系
        case 0x60:
            _MPCM_NOP();
            break;// E0
        case 0x61:
            _MPCM_NOP();
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
            _MPCM_NOP();
            break;// E5
        case 0x66:
            _MPCM_NOP();
            break;// E6
        case 0x67:
            comcmds._COM_E7();
            break;// E7	amp LFO
        case 0x68:
            _MPCM_E8();
            break;// E8	amp LFO switch
        case 0x69:
            comcmds._COM_E9();
            break;// E9	amp LFO delay
        case 0x6a:
            _MPCM_NOP();
            break;// EA
        case 0x6b:
            _MPCM_NOP();
            break;// EB
        case 0x6c:
            _MPCM_NOP();
            break;// EC
        case 0x6d:
            comcmds._COM_ED();
            break;// ED
        case 0x6e:
            _MPCM_NOP();
            break;// EE
        case 0x6f:
            _MPCM_NOP();
            break;// EF

        // システムコントール系
        case 0x70:
            _MPCM_F0();
            break;// F0	@
        case 0x71:
            _MPCM_NOP();
            break;// F1
        case 0x72:
            _MPCM_F2();
            break;// F2	volume
        case 0x73:
            _MPCM_F3();
            break;// F3	F
        case 0x74:
            _MPCM_F4();
            break;// F4	pan
        case 0x75:
            _MPCM_F5();
            break;// F5	)	くれ
        case 0x76:
            _MPCM_F6();
            break;// F6	(	でくれ
        case 0x77:
            _MPCM_NOP();
            break;// F7
        case 0x78:
            _MPCM_NOP();
            break;// F8
        case 0x79:
            comcmds._COM_F9();
            break;// F9	永久ループポイントマーク
        case 0x7a:
            devopm._OPM_FA();
            break;// FA	Y COMMAND
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
            _MPCM_FF();
            break;// FF	end of data
        }
    }

    //─────────────────────────────────────
    //
    public void _MPCM_NOP() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
        _mpcm_keyoff2();
    }

    //─────────────────────────────────────
    //	強制キーオフ
    //
    public void _MPCM_82() {
        _mpcm_keyoff2();
    }

    //─────────────────────────────────────
    //	擬似リバーブ
    //		switch = $80 = ON
    //			 $81 = OFF
    //			 $00 = + [volume]b
    //			 $01 = + [volume]b + [pan]b
    //			 $02 = + [volume]b + [tone]b
    //			 $03 = + [volume]b + [panpot]b + [tone]b
    //	work
    //		bit1 1:tone change
    //		bit0 1:panpot change
    //
    public void _MPCM_98() {
        comcmds._COM_98();
        if ((mm.readByte(reg.a5 + W.reverb) & 0x80) == 0) {
            _mpcm_keyoff2();
        }
    }

    //─────────────────────────────────────
    //	擬似エコー
    //
    public void _MPCM_99() {
        comcmds._COM_99();
    }

    //─────────────────────────────────────
    //	bank & tone set
    //		[$A1] + [bank]b + [tone]b
    public void _MPCM_A1() {
        mm.write(reg.a5 + W.bank, mm.readByte(reg.a1++));
        _MPCM_F0();
    }

    //─────────────────────────────────────
    //	TONE / TIMBRE
    //		[$A2] + [switch]b
    public void _MPCM_A2() {
        mm.write(reg.a5 + W.bank, 0);
        mm.write(reg.a5 + W.program, 0);
        mm.write(reg.a5 + W.program2, 0);
        mm.write(reg.a5 + W.pcm_tone, 0);
        mm.write(reg.a5 + W.banktone, 0xff);
        mm.write(reg.a5 + W.pcmmode, 0xff);

        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            _MPCM_A2_mx();
            return;
        }
        mm.write(reg.a5 + W.pcm_tone, (byte) ((reg.getD0_B() != 0) ? 0xff : 0x00));
    }

    public void _MPCM_A2_mx() {
        reg.setD0_B(reg.getD0_B() + 1);
        if (reg.getD0_B() != 0) { // break _MPCM_A2_8;
            reg.setD0_B(reg.getD0_B() + 1);
            if (reg.getD0_B() == 0) { // break _MPCM_A2_16;
// _MPCM_A2_16:
                mm.write(reg.a5 + W.pcmmode, 1);
            }
        } else {
// _MPCM_A2_8:
            mm.write(reg.a5 + W.pcmmode, 2);
        }
    }

    //─────────────────────────────────────
    //	音量テーブル
    //
    public void _MPCM_A3() {
        comcmds._COM_A3();

        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) return;
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a5 + W.volume));
        reg.a0 = reg.a5 + W.voltable;
        reg.setD4_B(mm.readByte(reg.a2 + (int) (short) reg.getD4_W()));
        _MPCM_F2_v();
    }

    //─────────────────────────────────────
    //	127段階音量テーブル切り替え
    //
    public void _MPCM_A7() {
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD5_B(mm.readByte(reg.a1++));

        reg.D0_L = mm.readInt(reg.a6 + Dw.VOL_PTR);
        if (reg.D0_L == 0) return;

        reg.a2 = reg.D0_L;
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) | (reg.D0_L << 16);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D2_L = reg.D0_L;
        if (reg.D0_L == 0) return;

        reg.setD4_W(mm.readShort(reg.a2));
        reg.a2 += 2;

// _mpcm_a7_ana_loop:
        while (true) {
            if (reg.getD1_B() - mm.readByte(reg.a2 + 2) == 0) {
                if (reg.getD5_B() - mm.readByte(reg.a2 + 3) == 0) break; // _mpcm_a7_set;
            }

            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() == 0) return;

            reg.setD0_W(mm.readShort(reg.a2));
            reg.a2 = reg.a2 + (int) (short) reg.getD0_W();
//            break _mpcm_a7_ana_loop;
        }
// _mpcm_a7_set:
        reg.a2 += 4;
        int e = reg.a1;
        reg.a1 = reg.a2;
        reg.a2 = e;
        reg.setD0_W(0x8005);
        reg.D1_L = -1;
        mndrv.trap(1);
        reg.a1 = reg.a2;
    }

    //─────────────────────────────────────
    //	音量 LFO on /off
    //
    //	$E8,num,switch
    public void _MPCM_E8() {
        //	pea	_COM_E8(pc)
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        _MPCM_F2_lfo();
        comcmds._COM_E8();
    }

    //─────────────────────────────────────
    //	tone set
    //		[$F0] + [num]b
    public void _MPCM_F0() {
        if ((byte) mm.readByte(reg.a5 + W.reverb) < 0) {
            _mpcm_keyoff();
        }
        reg.D5_L = 0;
        reg.setD5_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.program, (byte) reg.getD5_B());
        mm.write(reg.a5 + W.program2, (byte) reg.getD5_B());
        mm.write(reg.a5 + W.banktone, (short) (-1));

        if (mm.readByte(reg.a5 + W.pcm_tone) != 0) {
            _mpcm_echo_tone_change();
            return;
        }
        mm.write(reg.a5 + W.bank, (byte) reg.getD5_B());
    }

    public void _mpcm_echo_tone_change() {
        mm.write(reg.a5 + W.program, (byte) reg.getD5_B());

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x2) == 0) {
            return;
        }

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a5 + W.bank));
        reg.setD0_W(reg.getD0_W() << 7);
        reg.setD0_B(reg.getD0_B() | reg.getD5_B());
        reg.setD5_W(reg.getD0_W());
        reg.D5_L = reg.D5_L | 0x8000;

        reg.D0_L = mm.readInt(reg.a6 + Dw.MPCMWORKADR);
        if (reg.D0_L == 0) return;
        reg.a2 = reg.D0_L;
        reg.D0_L = mm.readInt(reg.a6 + Dw.ZPDCOUNT);
        if (reg.D0_L == 0) return;

// _mpcm_f0_timbre_ana:
        do {
            if (reg.getD5_W() - mm.readShort(reg.a2) == 0) {
                reg.a2 += 2;
                _mpcm_tone_ana_set();
                return;
            }
            reg.a2 = reg.a2 + P._pcm_work_size;
            reg.D0_L--;
        } while (reg.D0_L != 0); // break _mpcm_f0_timbre_ana;
    }

    public void _mpcm_tone_ana_set() {
        int e = reg.a1;
        reg.a1 = reg.a2;
        reg.a2 = e;
        reg.setD0_W(0x200);
        reg.setD0_B(mm.readByte(reg.a5 + W.dev));
        reg.D1_L = 0;
        mndrv.trap(1);
        reg.a1 = reg.a2;
    }

    //─────────────────────────────────────
    //	volume
    //		[$F2] + [volume]b
    public void _MPCM_F2() {
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) & 0xef));
        reg.D4_L = 0;
        reg.setD4_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() < 0) {
            reg.setD4_B((byte) (-(byte) reg.getD4_B()));
            reg.setD4_B(reg.getD4_B() - 1);
            _MPCM_F2_v();
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
        _MPCM_F2_v();
    }

    public void _MPCM_F2_v() {
        mm.write(reg.a5 + W.vol, (byte) reg.getD4_B());
        _MPCM_F2_softenv();
    }

    public void _MPCM_F2_softenv() {
        reg.setD0_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD0_B() < 0) {
            reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 0;
            }
        } else {
            reg.setD4_B(reg.getD4_B() + (int) (byte) reg.getD0_B());
            if ((byte) reg.getD4_B() < 0) {
                reg.D4_L = 127;
            }
        }
        _MPCM_F2_lfo();
    }

    public void _MPCM_F2_lfo() {
        reg.setD4_B(reg.getD4_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PCM));
        if ((byte) reg.getD4_B() < 0) {
            reg.D4_L = 0;
        }
        mm.write(reg.a5 + W.vol2, (byte) reg.getD4_B());

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x2) != 0) {
            reg.setD0_W(0x500);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            reg.setD1_B(reg.getD4_B());
            mndrv.trap(1);
        }
    }

    //─────────────────────────────────────
    //	frequency
    //		[$F3] + [freq]b
    public void _MPCM_F3() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a1++));

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x2) != 0) {
            reg.setD0_W(0x300);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
    }

    //─────────────────────────────────────
    //	panpot
    //		[$F4] + [pan]b
    public void _MPCM_F4() {
        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD1_B());

        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
            reg.setD0_W(0x600);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
    }

    //─────────────────────────────────────
    //	volup
    //			[$F5] + [DATA]b
    //
    public void _MPCM_F5() {
        if (mm.readByte(reg.a5 + W.volmode) == 0) {
            _MPCM_F5_normal();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _MPCM_F5_normal();
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
        _MPCM_F2_v();
    }

    public void _MPCM_F5_normal() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD4_B(reg.getD4_B() + mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() >= 0) {
            _MPCM_F2_v();
            return;
        }
        reg.D4_L = 0x7f;
        _MPCM_F2_v();
    }

    //─────────────────────────────────────
    //	voldown
    //			[$F6] + [DATA]b
    //
    public void _MPCM_F6() {
        if (mm.readByte(reg.a5 + W.volmode) == 0) {
            _MPCM_F6_normal();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x10) == 0) {
            _MPCM_F6_normal();
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
        _MPCM_F2_v();
    }

    public void _MPCM_F6_normal() {
        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
        reg.setD4_B(reg.getD4_B() - mm.readByte(reg.a1++));
        if ((byte) reg.getD4_B() >= 0) {
            _MPCM_F2_v();
            return;
        }
        reg.D4_L = 0;
        _MPCM_F2_v();
    }

    //─────────────────────────────────────
    //
    public void _MPCM_FF() {
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));

        reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a0 = reg.a6 + Dw.TRACKWORKADR;

        L1:
        if ((mm.readByte(reg.a0 + W.flag2) & 0x01) == 0) {
            reg.a0 = reg.a0 + W._track_work_size;
            reg.setD0_W(reg.getD0_W() - 1);
            if (reg.getD0_W() != 0) break L1;

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
                    mm.write(reg.a0 + W.flag2, (byte) (mm.readByte(reg.a0 + W.flag2) | 0x01));
                }
                reg.a0 = reg.a0 + W._track_work_size;
                reg.setD0_W(reg.getD0_W() - 1);
            } while (reg.getD0_W() != 0);
        }

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
            _mpcm_keyoff2();
            comcmds._all_end_check();
            return;
        }
        reg.a1 = mm.readInt(reg.a5 + W.loop);
    }


    //─────────────────────────────────────
    public void _ch_mpcm_lfo_job() {
        comwave._ch_effect();
        //_ch_mpcm_lfo:
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if (reg.getD0_B() == 0) return;
        mm.write(reg.a5 + W.addkeycode, (short) 0);
        mm.write(reg.a5 + W.addvolume, (short) 0);

        reg.D1_L = 0xe;
        reg.setD1_B(reg.getD1_B() & reg.getD0_B());
        if (reg.getD1_B() != 0) {
            int sp = reg.getD0_W();
            //_ch_mpcm_plfo_table
            switch (reg.getD1_W() / 2) {
            case 1:
                _ch_mpcm_plfo_1();
                break;
            case 2:
                _ch_mpcm_plfo_2();
                break;
            case 3:
                _ch_mpcm_plfo_3();
                break;
            case 4:
                _ch_mpcm_plfo_4();
                break;
            case 5:
                _ch_mpcm_plfo_5();
                break;
            case 6:
                _ch_mpcm_plfo_6();
                break;
            case 7:
                _ch_mpcm_plfo_7();
                break;
            }
            reg.setD0_W(sp);
        }
        reg.setD0_B(reg.getD0_B() >> 4);
        reg.setD0_W(reg.getD0_W() & 7);
        if (reg.getD0_W() != 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            //_ch_mpcm_alfo_table
            switch (reg.getD0_W() / 2) {
            case 1:
                _ch_mpcm_alfo_1();
                break;
            case 2:
                _ch_mpcm_alfo_2();
                break;
            case 3:
                _ch_mpcm_alfo_3();
                break;
            case 4:
                _ch_mpcm_alfo_4();
                break;
            case 5:
                _ch_mpcm_alfo_5();
                break;
            case 6:
                _ch_mpcm_alfo_6();
                break;
            case 7:
                _ch_mpcm_alfo_7();
                break;
            }
        }
        //_ch_mpcm_lfo_end:
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        reg.setD1_W(mm.readShort(reg.a5 + W.addkeycode));
        if (reg.getD1_W() - mm.readShort(reg.a5 + W.addkeycode2) != 0) { // break _ch_mpcm_lfo_a;
            mm.write(reg.a5 + W.addkeycode2, (short) reg.getD1_W());
            if ((short) reg.getD1_W() >= 0) { // break _ch_mpcm_lfo_end_minus;
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
                if (reg.getD2_W() >= 0x2000) { // break _ch_mpcm_lfo_end_common;
                    reg.setD2_W(0x1fff);
                }
//                break _ch_mpcm_lfo_end_common;
            } else {
// _ch_mpcm_lfo_end_minus:
                reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
                if ((short) reg.getD2_W() < 0) { // break _ch_mpcm_lfo_end_common;
                    reg.D2_L = 0;
                }
            }
// _ch_mpcm_lfo_end_common:
            _mpcm_freq();
        }
// _ch_mpcm_lfo_a:
        reg.setD0_W(mm.readShort(reg.a5 + W.addvolume));
        if (reg.getD0_W() - mm.readShort(reg.a5 + W.addvolume2) == 0) return;
        mm.write(reg.a5 + W.addvolume2, (short) reg.getD0_W());
        if ((short) reg.getD0_W() >= 0) { // break _ch_mpcm_lfo_a_minus;

            reg.D4_L = 0;
            reg.setD4_B(mm.readByte(reg.a5 + W.vol));
            reg.setD4_W(reg.getD4_W() + (int) (short) reg.getD0_W());
            if ((short) reg.getD4_W() >= 0) {
                _MPCM_F2_lfo();
            } else {
                reg.D4_L = 0x7f;
                _MPCM_F2_lfo();
            }
        } else {
// _ch_mpcm_lfo_a_minus:
            reg.D4_L = 0;
            reg.setD4_B(mm.readByte(reg.a5 + W.vol));
            reg.setD4_W(reg.getD4_W() + (int) (short) reg.getD0_W());
            if ((short) reg.getD4_W() >= 0) {
                _MPCM_F2_lfo();
            } else {
                reg.D4_L = 0;
                _MPCM_F2_lfo();
            }
        }
    }

    //─────────────────────────────────────
    public void _ch_mpcm_plfo_1() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_mpcm_p_common();
    }

    public void _ch_mpcm_plfo_2() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_mpcm_p_common();
    }

    public void _ch_mpcm_plfo_3() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_mpcm_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_mpcm_p_common();
    }

    public void _ch_mpcm_plfo_4() {
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_mpcm_p_common();
    }

    public void _ch_mpcm_plfo_5() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_mpcm_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_mpcm_p_common();
    }

    public void _ch_mpcm_plfo_6() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_mpcm_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_mpcm_p_common();
    }

    public void _ch_mpcm_plfo_7() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_mpcm_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_mpcm_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_mpcm_p_common();
    }

    //─────────────────────────────────────
    public void _ch_mpcm_alfo_1() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_mpcm_a_common();
    }

    public void _ch_mpcm_alfo_2() {
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_mpcm_a_common();
    }

    public void _ch_mpcm_alfo_3() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_mpcm_a_common();
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_mpcm_a_common();
    }

    public void _ch_mpcm_alfo_4() {
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_mpcm_a_common();
    }

    public void _ch_mpcm_alfo_5() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_mpcm_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_mpcm_a_common();
    }

    public void _ch_mpcm_alfo_6() {
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_mpcm_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_mpcm_a_common();
    }

    public void _ch_mpcm_alfo_7() {
        reg.a4 = reg.a5 + W.v_pattern1;
        reg.a3 = reg.a5 + W.wv_pattern1;
        _ch_mpcm_a_common();
        reg.a4 = reg.a5 + W.v_pattern2;
        reg.a3 = reg.a5 + W.wv_pattern2;
        _ch_mpcm_a_common();
        reg.a4 = reg.a5 + W.v_pattern3;
        reg.a3 = reg.a5 + W.wv_pattern3;
        _ch_mpcm_a_common();
    }

    //─────────────────────────────────────
    public void _ch_mpcm_a_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            comwave._com_wavememory();
            mm.write(reg.a5 + W.addvolume, (short) (mm.readShort(reg.a5 + W.addvolume) + (short) reg.getD0_W()));
            return;
        }

        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _ch_mpcm_v_com_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_mpcm_v_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_mpcm_v_com_exec();
        }
    }

    public void _ch_mpcm_v_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_mpcm_v_com_exec();
        }
    }

    public void _ch_mpcm_v_com_exec() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        //_mpcm_velocity_pattern
        switch (reg.getD0_W() / 2) {
        case 1:
            comlfo._com_lfo_saw();
            break;
        case 2:
            comlfo._com_lfo_portament();
            break;
        case 3:
            comlfo._com_lfo_triangle();
            break;
        }
        mm.write(reg.a5 + W.addvolume, (short) reg.getD1_W());
    }

    //─────────────────────────────────────
    public void _ch_mpcm_p_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            comwave._com_wavememory();
            mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD0_W()));
            return;
        }
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _ch_mpcm_p_com_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_mpcm_p_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_mpcm_p_com_exec();
        }
    }

    public void _ch_mpcm_p_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_mpcm_p_com_exec();
        }
    }

    public void _ch_mpcm_p_com_exec() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());

        if ((byte) mm.readByte(reg.a6 + Dw.LFO_FLAG) >= 0) {
            //_mpcm_pitch_pattern:
            switch (reg.getD0_W() / 2) {
            case 1:
                comlfo._com_lfo_saw();
                break;
            case 2:
                comlfo._com_lfo_portament();
                break;
            case 3:
                comlfo._com_lfo_triangle();
                break;
            case 4:
                comlfo._com_lfo_portament();
                break;
            case 5:
                comlfo._com_lfo_triangle();
                break;
            case 6:
                comlfo._com_lfo_triangle();
                break;
            case 7:
                comlfo._com_lfo_oneshot();
                break;
            case 8:
                comlfo._com_lfo_oneshot();
                break;
            }
            mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
            return;
        }
        reg.a0 = Ab.dummyAddress;// _pitch_extend;
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

    //─────────────────────────────────────
    public void _ch_mpcm_mml_job() {
        comanalyze._track_analyze();

        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if ((byte) reg.getD0_B() >= 0) return;

        if ((mm.readByte(reg.a5 + W.flag2) & 0x2) != 0) {
            _ch_mpcm_porta();
            return;
        }
        _ch_mpcm_bend();
    }

    //─────────────────────────────────────
    //	pitch bend
    //
    public void _ch_mpcm_bend() {
        reg.a4 = reg.a5 + W.p_pattern4;
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) return;

        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
        if ((short) reg.getD1_W() >= 0) {
            _ch_mpcm_bend_plus();
            return;
        }

        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if ((short) reg.getD2_W() < 0) {
            reg.setD2_W(0x1fff);
        }
        if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
            _ch_mpcm_bend_end();
            return;
        }
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _mpcm_freq();
    }

    public void _ch_mpcm_bend_plus() {
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if ((short) reg.getD2_W() < 0) {
            reg.setD2_W(0);
        }
        if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
            _ch_mpcm_bend_end();
            return;
        }
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _mpcm_freq();
    }

    public void _ch_mpcm_bend_end() {
        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));

        reg.D2_L = 0;
        reg.setD2_B(mm.readByte(reg.a5 + W.key2));
        mm.write(reg.a5 + W.key, (byte) reg.getD2_B());
        reg.setD2_W(reg.getD2_W() << 6);
        reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD2_W() < 0) {
            reg.D2_L = 0;
        }
        mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _mpcm_freq();
    }

    //─────────────────────────────────────
    //	portament
    //
    public void _ch_mpcm_porta() {
        reg.a4 = reg.a5 + W.p_pattern4;

        if ((mm.readByte(reg.a5 + W.flag2) & 0x10) != 0) {
            _ch_mpcm_lw_port();
            return;
        }
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if (mm.readShort(reg.a4 + W_L.henka_work) == 0) {
            _ch_mpcm_porta_common();
            return;
        }
        if (mm.readShort(reg.a4 + W_L.henka_work) < 0) {
            _ch_mpcm_porta_minus();
            return;
        }

        mm.write(reg.a4 + W_L.henka_work, (short) (mm.readShort(reg.a4 + W_L.henka_work) - 1));
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + 1));
        reg.setD2_W(reg.getD2_W() + 1);
        _ch_mpcm_porta_common();
    }

    public void _ch_mpcm_porta_minus() {
        mm.write(reg.a4 + W_L.henka_work, (short) (mm.readShort(reg.a4 + W_L.henka_work) + 1));
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + 1));
        reg.setD2_W(reg.getD2_W() - 1);
        _ch_mpcm_porta_common();
    }

    public void _ch_mpcm_porta_common() {
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _mpcm_freq();
    }

    public void _ch_mpcm_lw_port() {
        reg.D1_L = 0;
        reg.D2_L = 0;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.lfo_sp));
        if (reg.getD1_B() == 0) {
            _ch_mpcm_lw_porta_end();
            return;
        }
        reg.setD0_W(mm.readShort(reg.a4 + W_L.mokuhyou));
        Boolean cf = (short) reg.getD0_W() - mm.readShort(reg.a5 + W.keycode) < 0;
        reg.setD0_W((short) ((short) reg.getD0_W() - mm.readShort(reg.a5 + W.keycode)));
        if (reg.getD0_W() == 0) {
            _ch_mpcm_lw_porta_end();
            return;
        }
        if (cf) {
            reg.D2_L = 0xff;
            reg.setD0_W((short) (-(short) reg.getD0_W()));
        }
        reg.setD0_W(reg.getD0_W() * reg.getD1_W());
        int f = reg.D0_L & 0x80;
        reg.D0_L >>= 8;
        if (f == 0) {
            reg.D0_L = 1;
        }
        if (reg.getD2_B() != 0) {
            reg.setD0_W((short) (-(short) reg.getD0_W()));
        }
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode));
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD0_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD0_W());
        _mpcm_freq();
    }

    public void _ch_mpcm_lw_porta_end() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfd));
    }

    //─────────────────────────────────────
    public void _ch_mpcm_softenv_job() {
        comlfo._soft_env();
        _MPCM_F2_softenv();
    }

    //─────────────────────────────────────
    //	effect execute
    //
    public void _mpcm_effect_tone() {
        mm.write(reg.a5 + W.bank, (byte) (reg.getD0_W() >> 8));
        reg.setD5_B(reg.getD0_B());
        _mpcm_echo_tone_change();
    }

    public void _mpcm_effect_pan() {
        reg.setD1_B(reg.getD0_B());
        reg.setD1_B(reg.getD1_B() & 3);
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD1_B());
        if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x02) != 0) {
            reg.setD0_W(0x600);
            reg.setD0_B(mm.readByte(reg.a5 + W.dev));
            mndrv.trap(1);
        }
    }
}
