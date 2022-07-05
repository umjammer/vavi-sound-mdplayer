package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


public class DevOpnEmu {
    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public ComAnalyze comanalyze;
    public ComCmds comcmds;
    public ComLfo comlfo;
    public ComWave comwave;
    public DevOpn devopn;
    public DevOpm devopm;


    //
    //	part of YM2608 - FM emulation
    //

    //─────────────────────────────────────
    public void _fmemu_exit() {
        //	rts
    }

    public void _fme_note_set() {
        mm.write(reg.a5 + W.key, (byte) reg.getD0_B());

        reg.setD1_W(0x800);
        reg.D2_L = 0;
        reg.D3_L = 12;

        while (reg.getD0_B() >= reg.getD3_B()) {
            reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD3_B());
            reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        }

        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.a0 = Ab.dummyAddress;// DevOpn._fnum_table;
        reg.setD2_W(reg.getD2_W() | DevOpn._fnum_table[reg.getD0_W() / 2]);// mm.Readshort(Reg.a0 + reg.getD0_W());
        //    pea _opm_keyon(pc)
        _emu_set_fnum();
        comlfo.initLfo();
        devopn._init_lfo_fm();
        devopm._opm_keyon();
    }

    //─────────────────────────────────────
    //	SET F-Number
    //
    public void _emu_set_fnum() {
        reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.detune));
        if ((short) reg.getD2_W() < 0) {
            reg.D2_L = 0;
        }
        mm.write(reg.a5 + W.keycode2, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
        _get_kckf();
    }

    //─────────────────────────────────────
    //	SET F-Number
    //
    public void _emu_set_fnum2() {
        mm.write(reg.a5 + W.keycode, (short) reg.getD2_W());
        _get_kckf();
    }

    //─────────────────────────────────────
    public void _get_kckf() {
        reg.setD1_W(reg.getD2_W());
        reg.setD1_W(reg.getD1_W() & 0x3800);
        reg.setD1_W(reg.getD1_W() >> 7);
        reg.setD2_W(reg.getD2_W() & 0x7ff);
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD2_W());
        reg.a0 = reg.a6 + Dw.FNUM_KC_TABLE;
        reg.setD0_B(mm.readByte(reg.a0 + (int) (short) reg.getD2_W()));
        reg.setD3_B(mm.readByte(reg.a0 + (int) (short) reg.getD2_W() + 1));
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() & 0x7f);
        reg.D1_L = 0x28;
        mndrv._OPM_WRITE4();
        reg.D1_L = 0x30;
        reg.setD0_B(reg.getD3_B());
        mndrv._OPM_WRITE4();
    }

    //─────────────────────────────────────
    //	MML コマンド処理(FM 部)
    //
    public void _fme_command() {
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        //_fmec:
        switch (reg.getD0_W() / 2) {
        case 0x00:
            break;
        case 0x01:
            comcmds._COM_81();
            break;// 81
        case 0x02:
            devopm._OPM_82();
            break;// 82	key off
        case 0x03:
            comcmds._COM_83();
            break;// 83	すらー
        case 0x04:
            _FME_NOP();
            break;// 84
        case 0x05:
            _FME_NOP();
            break;// 85
        case 0x06:
            comcmds._COM_86();
            break;// 86	同期信号送信
        case 0x07:
            comcmds._COM_87();
            break;// 87	同期信号待ち
        case 0x08:
            devopn._FM_88();
            break;// 88	ぴっちべんど
        case 0x09:
            devopn._FM_89();
            break;// 89	ぽるためんと
        case 0x0a:
            _FME_NOP();
            break;// 8A
        case 0x0b:
            _FME_NOP();
            break;// 8B
        case 0x0c:
            _FME_NOP();
            break;// 8C
        case 0x0d:
            _FME_NOP();
            break;// 8D
        case 0x0e:
            _FME_NOP();
            break;// 8E
        case 0x0f:
            _FME_NOP();
            break;// 8F

        case 0x10:
            comcmds._COM_90();
            break;// 90	q
        case 0x11:
            comcmds._COM_91();
            break;// 91	@q
        case 0x12:
            devopm._OPM_92();
            break;// 92	keyoff rr cut switch
        case 0x13:
            comcmds._COM_93();
            break;// 93	neg @q
        case 0x14:
            comcmds._COM_94();
            break;// 94	keyoff mode
        case 0x15:
            _FME_NOP();
            break;// 95
        case 0x16:
            _FME_NOP();
            break;// 96
        case 0x17:
            _FME_NOP();
            break;// 97
        case 0x18:
            devopm._OPM_98();
            break;// 98	擬似リバーブ
        case 0x19:
            devopm._OPM_99();
            break;// 99	擬似エコー
        case 0x1a:
            comcmds._COM_9A();
            break;// 9A 擬似step time
        case 0x1b:
            _FME_NOP();
            break;// 9B
        case 0x1c:
            _FME_NOP();
            break;// 9C
        case 0x1d:
            _FME_NOP();
            break;// 9D
        case 0x1e:
            _FME_NOP();
            break;// 9E
        case 0x1f:
            _FME_NOP();
            break;// 9F

        case 0x20:
            devopm._OPM_F0();
            break;// A0 音色切り替え
        case 0x21:
            devopm._OPM_A1();
            break;// A1 バンク&音色切り替え
        case 0x22:
            _FME_NOP();
            break;// A2
        case 0x23:
            devopm._OPM_A3();
            break;// A3 音量テーブル切り替え
        case 0x24:
            devopm._OPM_F2();
            break;// A4 音量
        case 0x25:
            devopm._OPM_F5();
            break;// A5
        case 0x26:
            devopm._OPM_F6();
            break;// A6
        case 0x27:
            _FME_NOP();
            break;// A7
        case 0x28:
            comcmds._COM_A8();
            break;// A8 相対音量モード
        case 0x29:
            _FME_NOP();
            break;// A9
        case 0x2a:
            _FME_NOP();
            break;// AA
        case 0x2b:
            _FME_NOP();
            break;// AB
        case 0x2c:
            _FME_NOP();
            break;// AC
        case 0x2d:
            _FME_NOP();
            break;// AD
        case 0x2e:
            _FME_NOP();
            break;// AE
        case 0x2f:
            _FME_NOP();
            break;// AF

        case 0x30:
            comcmds._COM_B0();
            break;// B0
        case 0x31:
            _FME_NOP();
            break;// B1
        case 0x32:
            _FME_NOP();
            break;// B2
        case 0x33:
            _FME_NOP();
            break;// B3
        case 0x34:
            _FME_NOP();
            break;// B4
        case 0x35:
            _FME_NOP();
            break;// B5
        case 0x36:
            _FME_NOP();
            break;// B6
        case 0x37:
            _FME_NOP();
            break;// B7
        case 0x38:
            _FME_NOP();
            break;// B8
        case 0x39:
            _FME_NOP();
            break;// B9
        case 0x3a:
            _FME_NOP();
            break;// BA
        case 0x3b:
            _FME_NOP();
            break;// BB
        case 0x3c:
            _FME_NOP();
            break;// BC
        case 0x3d:
            _FME_NOP();
            break;// BD
        case 0x3e:
            comcmds._COM_BE();
            break;// BE ジャンプ
        case 0x3f:
            comcmds._COM_BF();
            break;// BF

        // Psg 系
        case 0x40:
            comcmds._COM_C0();
            break;// C0 ソフトウェアエンベロープ 1
        case 0x41:
            comcmds._COM_C1();
            break;// C1 ソフトウェアエンベロープ 2
        case 0x42:
            _FME_NOP();
            break;// C2
        case 0x43:
            comcmds._COM_C3();
            break;// C3	switch
        case 0x44:
            comcmds._COM_C4();
            break;// C4 env(num)
        case 0x45:
            comcmds._COM_C5();
            break;// C5 env(bank + num)
        case 0x46:
            _FME_NOP();
            break;// C6
        case 0x47:
            _FME_NOP();
            break;// C7
        case 0x48:
            _FME_NOP();
            break;// C8
        case 0x49:
            _FME_NOP();
            break;// C9
        case 0x4a:
            _FME_NOP();
            break;// CA
        case 0x4b:
            _FME_NOP();
            break;// CB
        case 0x4c:
            _FME_NOP();
            break;// CC
        case 0x4d:
            _FME_NOP();
            break;// CD
        case 0x4e:
            _FME_NOP();
            break;// CE
        case 0x4f:
            _FME_NOP();
            break;// CF

        // KEY 系
        case 0x50:
            comcmds._COM_D0();
            break;// D0 キートランスポーズ
        case 0x51:
            comcmds._COM_D1();
            break;// D1 相対キートランスポーズ
        case 0x52:
            _FME_NOP();
            break;// D2
        case 0x53:
            _FME_NOP();
            break;// D3
        case 0x54:
            _FME_NOP();
            break;// D4
        case 0x55:
            _FME_NOP();
            break;// D5
        case 0x56:
            _FME_NOP();
            break;// D6
        case 0x57:
            _FME_NOP();
            break;// D7
        case 0x58:
            comcmds._COM_D8();
            break;// D8 ディチューン
        case 0x59:
            comcmds._COM_D9();
            break;// D9 相対ディチューン
        case 0x5a:
            _FME_NOP();
            break;// DA スロットディチューン
        case 0x5b:
            _FME_NOP();
            break;// DB 相対スロットディチューン
        case 0x5c:
            _FME_NOP();
            break;// DC
        case 0x5d:
            _FME_NOP();
            break;// DD
        case 0x5e:
            _FME_NOP();
            break;// DE
        case 0x5f:
            _FME_NOP();
            break;// DF

        // LFO 系
        case 0x60:
            devopm._OPM_E0();
            break;// E0 hardware LFO
        case 0x61:
            devopm._OPM_E1();
            break;// E1 hardware LFO switch
        case 0x62:
            comcmds._COM_E2();
            break;// E2 pitch LFO
        case 0x63:
            comcmds._COM_E3();
            break;// E3 pitch LFO switch
        case 0x64:
            comcmds._COM_E4();
            break;// E4 pitch LFO delay
        case 0x65:
            _FME_NOP();
            break;// E5
        case 0x66:
            _FME_NOP();
            break;// E6
        case 0x67:
            comcmds._COM_E7();
            break;// E7 amp LFO
        case 0x68:
            devopm._OPM_E8();
            break;// E8 amp LFO switch
        case 0x69:
            comcmds._COM_E9();
            break;// E9 amp LFO delay
        case 0x6a:
            comcmds._COM_EA();
            break;// EA amp switch 2
        case 0x6b:
            comcmds._COM_EB();
            break;// EB
        case 0x6c:
            _FME_NOP();
            break;// EC
        case 0x6d:
            comcmds._COM_ED();
            break;// ED
        case 0x6e:
            devopm._OPM_EE();
            break;// EE LW type LFO
        case 0x6f:
            comcmds._COM_EF();
            break;// EF hardware LFO delay

        // システムコントール系
        case 0x70:
            devopm._OPM_F0();
            break;// F0	@
        case 0x71:
            comcmds._COM_D8();
            break;// F1
        case 0x72:
            devopm._OPM_F2();
            break;// F2 volume
        case 0x73:
            comcmds._COM_91();
            break;// F3
        case 0x74:
            devopm._OPM_F4();
            break;// F4 pan
        case 0x75:
            devopm._OPM_F5();
            break;// F5	) volup
        case 0x76:
            devopm._OPM_F6();
            break;// F6(voldown
        case 0x77:
            _FME_NOP();
            break;// F7 効果音モード切り替え
        case 0x78:
            devopm._OPM_F8();
            break;// F8 スロットマスク変更
        case 0x79:
            comcmds._COM_F9();
            break;// F9 永久ループポイントマーク
        case 0x7a:
            devopm._OPM_FA();
            break;// FA y command
        case 0x7b:
            comcmds._COM_FB();
            break;// FB リピート抜け出し
        case 0x7c:
            comcmds._COM_FC();
            break;// FC リピート開始
        case 0x7d:
            comcmds._COM_FD();
            break;// FD リピート終端
        case 0x7e:
            comcmds._COM_FE();
            break;// FE tempo
        case 0x7f:
            devopm._OPM_FF();
            break;// FF end of data
        }
    }

    //─────────────────────────────────────
    //
    public void _FME_NOP() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
        devopm._opm_keyoff();
    }

    //─────────────────────────────────────
    //
    public void _ch_fme_lfo_job() {
        comwave._ch_effect();
        _ch_fme_lfo();
    }

    public void _ch_fme_lfo() {
        if ((mm.readByte(reg.a5 + W.effect) & 0x20) != 0) {
            reg.a4 = reg.a5 + W.ww_pattern1;
            devopm._ch_opm_ww();
        }
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if (reg.getD0_B() == 0) return;
        mm.write(reg.a5 + W.addkeycode, (short) 0);

        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            devopm._ch_opm_HLFO();
        }
        //_ch_fme_p1:
        if (mm.readByte(reg.a6 + Dw.LFO_FLAG) < 0) {
            _ch_fme_lfo_extend();
            return;
        }

        reg.D1_L = 7;
        reg.setD1_B(reg.getD1_B() & reg.getD0_B());
        if (reg.getD1_B() != 0) {
            int sp = reg.getD0_W();
            reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
            switch (reg.getD1_W() / 2) {
            case 1:
                _ch_fme_plfo_1();
                break;
            case 2:
                _ch_fme_plfo_2();
                break;
            case 3:
                _ch_fme_plfo_3();
                break;
            case 4:
                _ch_fme_plfo_4();
                break;
            case 5:
                _ch_fme_plfo_5();
                break;
            case 6:
                _ch_fme_plfo_6();
                break;
            case 7:
                _ch_fme_plfo_7();
                break;
            }
            reg.setD0_W(sp);
        }
        reg.setD0_B(reg.getD0_B() >> 3);
        reg.setD0_W(reg.getD0_W() & 7);
        if (reg.getD0_W() != 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            switch (reg.getD0_W() / 2) {
            case 1:
                devopm._ch_opm_alfo_1();
                break;
            case 2:
                devopm._ch_opm_alfo_2();
                break;
            case 3:
                devopm._ch_opm_alfo_3();
                break;
            case 4:
                devopm._ch_opm_alfo_4();
                break;
            case 5:
                devopm._ch_opm_alfo_5();
                break;
            case 6:
                devopm._ch_opm_alfo_6();
                break;
            case 7:
                devopm._ch_opm_alfo_7();
                break;
            }
        }
    }

    //─────────────────────────────────────
    public void _ch_fme_plfo_1() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fme_p_common();
    }

    public void _ch_fme_plfo_2() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fme_p_common();
    }

    public void _ch_fme_plfo_3() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fme_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fme_p_common();
    }

    public void _ch_fme_plfo_4() {
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fme_p_common();
    }

    public void _ch_fme_plfo_5() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fme_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fme_p_common();
    }

    public void _ch_fme_plfo_6() {
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fme_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fme_p_common();
    }

    public void _ch_fme_plfo_7() {
        reg.a4 = reg.a5 + W.p_pattern1;
        reg.a3 = reg.a5 + W.wp_pattern1;
        _ch_fme_p_common();
        reg.a4 = reg.a5 + W.p_pattern2;
        reg.a3 = reg.a5 + W.wp_pattern2;
        _ch_fme_p_common();
        reg.a4 = reg.a5 + W.p_pattern3;
        reg.a3 = reg.a5 + W.wp_pattern3;
        _ch_fme_p_common();
    }

    //─────────────────────────────────────
    public void _ch_fme_p_common() {
        reg.D0_L = 1;
        reg.setD1_B(mm.readByte(reg.a4 + W_L.pattern));
        if ((byte) reg.getD1_B() < 0) {
            _ch_fme_p_wavememory();
            return;
        }

        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            devopn._ch_fm_p_com_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f != 0) {
            _ch_fme_p_keyon_only();
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
            _ch_fme_p_com_exec();
        }
    }

    public void _ch_fme_p_keyon_only() {
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _ch_fme_p_com_exec();
        }
    }

    public void _ch_fme_p_com_exec() {
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        //_pe_pattern:
        switch (reg.getD0_W() / 2) {
        case 1:
            _ch_fme_p_0();
            break;
        case 2:
            _ch_fme_p_1();
            break;
        case 3:
            _ch_fme_p_2();
            break;
        case 4:
            _ch_fme_p_3();
            break;
        case 5:
            _ch_fme_p_4();
            break;
        case 6:
            _ch_fme_p_5();
            break;
        case 7:
            _ch_fme_p_6();
            break;
        case 8:
            _ch_fme_p_7();
            break;
        }
    }

    //─────────────────────────────────────
    //	extended LFO
    //
    public void _ch_fme_lfo_extend() {
        reg.D1_L = 7;
        reg.setD1_B(reg.getD1_B() & reg.getD0_B());
        if (reg.getD1_B() != 0) {
            int sp = reg.getD0_W();
            reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
            switch (reg.getD1_W() / 2) {
            case 1:
                devopn._ch_fm_explfo_1();
                break;
            case 2:
                devopn._ch_fm_explfo_2();
                break;
            case 3:
                devopn._ch_fm_explfo_3();
                break;
            case 4:
                devopn._ch_fm_explfo_4();
                break;
            case 5:
                devopn._ch_fm_explfo_5();
                break;
            case 6:
                devopn._ch_fm_explfo_6();
                break;
            case 7:
                devopn._ch_fm_explfo_7();
                break;
            }
            reg.setD0_W(sp);
        }
        reg.setD0_B(reg.getD0_B() >> 3);
        reg.setD0_W(reg.getD0_W() & 7);
        if (reg.getD0_W() != 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            switch (reg.getD0_W() / 2) {
            case 1:
                devopm._ch_opm_alfo_1();
                break;
            case 2:
                devopm._ch_opm_alfo_2();
                break;
            case 3:
                devopm._ch_opm_alfo_3();
                break;
            case 4:
                devopm._ch_opm_alfo_4();
                break;
            case 5:
                devopm._ch_opm_alfo_5();
                break;
            case 6:
                devopm._ch_opm_alfo_6();
                break;
            case 7:
                devopm._ch_opm_alfo_7();
                break;
            }
        }
        //	pea	_emu_set_fnum2(pc)
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        devopn._ex_slot_calc();
        _emu_set_fnum2();
    }

    //─────────────────────────────────────
    public void _ch_fme_mml_job() {
        comanalyze._track_analyze();
        _ch_fme_bend_job();
    }

    public void _ch_fme_bend_job() {
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        if ((byte) reg.getD0_B() >= 0) return;
        if ((mm.readByte(reg.a5 + W.flag2) & 0x02) != 0) {
            _ch_fme_porta();
            return;
        }
        _ch_fme_bend();
    }

    //─────────────────────────────────────
    //	pitch bend
    //
    public void _ch_fme_bend() {
        reg.a4 = reg.a5 + W.p_pattern4;
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) {
            //_ch_fme_bend_no_job();
            return;
        }
        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
        if ((short) reg.getD1_W() >= 0) {
            reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
            devopn._ch_fm_porta_calc();
            if (reg.getD2_W() >= mm.readShort(reg.a4 + W_L.mokuhyou)) {
                _ch_fme_bend_end();
                return;
            }
            mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
            _emu_set_fnum2();
            return;
        }
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        devopn._ch_fm_porta_calc();
        if (reg.getD2_W() < mm.readShort(reg.a4 + W_L.mokuhyou)) {
            _ch_fme_bend_end();
            return;
        }
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _emu_set_fnum2();
    }

    public void _ch_fme_bend_end() {
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
        reg.a4 = Ab.dummyAddress;// _fnum_table;
        reg.setD2_W(DevOpn._fnum_table[reg.getD0_W() / 2]); // mm.Readshort(Reg.a4 + reg.getD0_W());
        reg.setD2_W(reg.getD2_W() | reg.getD1_W());
        _emu_set_fnum();

    }

    //─────────────────────────────────────
    //	portament
    //
    public void _ch_fme_porta() {
        reg.a4 = reg.a5 + W.p_pattern4;

        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka));
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
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
            reg.a4 = Ab.dummyAddress;// _fnum_table;
            reg.setD2_W(DevOpn._fnum_table[reg.getD0_W() / 2]); // mm.Readshort(Reg.a4 + reg.getD0_W());
            reg.setD2_W(reg.getD2_W() | reg.getD1_W());
            mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
            _emu_set_fnum();
            return;
        }
        devopn._ch_fm_porta_calc();
        mm.write(reg.a5 + W.keycode3, (short) reg.getD2_W());
        _emu_set_fnum2();
    }

    //─────────────────────────────────────
    //	pitch LFO 鋸波
    //
    public void _ch_fme_p_0() {
        comlfo.comLfoSaw();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc3();
    }

    //─────────────────────────────────────
    //	pitch LFO portament
    //
    public void _ch_fme_p_1() {
        comlfo.comLfoPortament();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc3();
    }

    //─────────────────────────────────────
    //	pitch LFO delta
    //
    public void _ch_fme_p_2() {
        comlfo.comLfoTriangle();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc3();
    }

    //─────────────────────────────────────
    //	pitch LFO portament2
    //
    public void _ch_fme_p_3() {
        comlfo.comLfoPortament();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc2();
    }

    //─────────────────────────────────────
    //	pitch LFO delta2
    //
    public void _ch_fme_p_4() {
        comlfo.comLfoTriangle();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc2();
    }

    //─────────────────────────────────────
    //	pitch LFO delta 3
    //
    public void _ch_fme_p_5() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) {
            //_ch_fme_p_5_end();
            return;
        }
        mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode));
        reg.setD1_W(mm.readShort(reg.a4 + W_L.henka_work));
        if ((short) reg.getD1_W() >= 0) {
            _ch_fme_p_5_plus();
            return;
        }

        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if ((short) reg.getD2_W() < 0) {
            reg.D2_L = 0;
        }
        _ch_fme_p_5_common();
    }

    public void _ch_fme_p_5_plus() {
        mm.write(reg.a4 + W_L.bendwork, (short) (mm.readShort(reg.a4 + W_L.bendwork) + (short) reg.getD1_W()));
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD1_W());
        if ((short) reg.getD2_W() < 0) {
            reg.setD2_W(0x3fff);
        }
        _ch_fme_p_5_common();
    }

    public void _ch_fme_p_5_common() {
        if (mm.readByte(reg.a5 + W.ch3) != 0) {
            mm.write(reg.a5 + W.keycode_s2, (short) (mm.readShort(reg.a5 + W.keycode_s2) + (short) reg.getD1_W()));
            mm.write(reg.a5 + W.keycode_s3, (short) (mm.readShort(reg.a5 + W.keycode_s3) + (short) reg.getD1_W()));
            mm.write(reg.a5 + W.keycode_s4, (short) (mm.readShort(reg.a5 + W.keycode_s4) + (short) reg.getD1_W()));
        }
        devopn._set_fnum2();
        mm.write(reg.a4 + W_L.count_work, (byte) (mm.readByte(reg.a4 + W_L.count_work) - 1));
        if (mm.readByte(reg.a4 + W_L.count_work) != 0) return;
        mm.write(reg.a4 + W_L.count_work, mm.readByte(reg.a4 + W_L.count));
        //	neg.W	w_l_henka_work(a4)
        mm.write(reg.a4 + W_L.henka_work, (short) (-(short) mm.readShort(reg.a4 + W_L.henka_work)));
    }

    //─────────────────────────────────────
    //	pitch LFO 1shot
    //
    public void _ch_fme_p_6() {
        comlfo.comLfoOneshot();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc3();
    }

    //─────────────────────────────────────
    //	pitch LFO 1shot 2
    //
    public void _ch_fme_p_7() {
        comlfo.comLfoOneshot();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + (short) reg.getD1_W()));
        _ch_fme_p_calc2();
    }

    //─────────────────────────────────────
    //	wavememory pitch
    //
    public void _ch_fme_p_wavememory() {
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _fm_pe_wave_exec();
            return;
        }
        int f = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (f == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
                _fm_pe_wave_exec();
            }
        }
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _fm_pe_wave_exec();
        }
    }

    public void _fm_pe_wave_exec() {
        comwave._com_wave_exec();
        mm.write(reg.a5 + W.addkeycode, (short) (mm.readShort(reg.a5 + W.addkeycode) + reg.getD0_W()));
        _ch_fme_p_calc();
    }

    //─────────────────────────────────────
    public void _ch_fme_p_calc() {
        //	pea	_emu_set_fnum2(pc)
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        devopn._ex_slot_calc();
        _emu_set_fnum2();
    }

    public void _ch_fme_p_calc2() {
        //	pea	_emu_set_fnum2(pc)
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        devopn._ex_slot_calc2();
        _emu_set_fnum2();
    }

    //─────────────────────────────────────
    public void _ch_fme_p_calc3() {
        reg.setD2_W(mm.readShort(reg.a5 + W.keycode3));
        reg.setD2_W(reg.getD2_W() + mm.readShort(reg.a5 + W.addkeycode));
        _emu_set_fnum2();
    }

    //─────────────────────────────────────
    //─────────────────────────────────────
    //	effect execute
    //
    public void _fme_effect_ycommand() {
        reg.setD1_B((byte) (reg.getD0_W() >> 8));
        mndrv._OPN_WRITE2();
    }
}
