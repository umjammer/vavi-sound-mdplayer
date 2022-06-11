package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


//
// part of YM2608 - RHYTHM
//
public class DevRhy {

    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public ComCmds comcmds;
    public DevOpn devopn;

    /** */
    public void _rhythm_quit() {
    }

    public void _ch_rhythm() {
        if (mm.readByte(reg.a5 + W.flag4) < 0) return;
        if (mm.readByte(reg.a5 + W.flag) >= 0) return;

        reg.setD4_B(mm.readByte(reg.a5 + W.len));
        reg.setD4_B(reg.getD4_B() - 1);


        if ((mm.readByte(reg.a5 + W.flag) & 0x40) == 0) { // break _rhythm_next;

            if (reg.getD4_B() - mm.readByte(reg.a5 + W.at_q) != 0) {
                reg.D1_L = 0x98;
                mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & reg.getD1_B()));
            }

            if ((mm.readByte(reg.a5 + W.flag3) & 0x20) != 0) { // break _rhythm_next;
                reg.setD0_B(mm.readByte(reg.a5 + W.at_q_work));
                if (reg.getD0_B() != 0) { // break _rhythm_next;

                    reg.setD0_B(reg.getD0_B() - 1);
                    if (reg.getD0_B() == 0) {
                        reg.D1_L = 0x98;
                        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & reg.getD1_B()));
                    }

                    mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
                }
            }
        }
// _rhythm_next:

        mm.write(reg.a5 + W.len, (byte) reg.getD4_B());
        if (reg.getD4_B() != 0) return;

        if ((mm.readByte(reg.a5 + W.flag) & 0x40) == 0) { // break _rhythm_fetch;

            reg.D1_L = 0x98;
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & reg.getD1_B()));
        }
// _rhythm_fetch:
        reg.a1 = mm.readInt(reg.a5 + W.dataptr);
        boolean L1 = true;
//        break L1;

// _rhythm_loop:
        while (true) {
            if (!L1) {
                if (mm.readByte(reg.a5 + W.flag4) < 0) return;
                if (mm.readByte(reg.a5 + W.flag) >= 0) return;
                L1 = false;
            }
// L1:
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a1++));
            if ((byte) reg.getD0_B() < 0) { // break _rhythm_mml;
                reg.setD0_B(reg.getD0_B() - 0x80);
                if (reg.getD0_B() != 0) { // break _rhythm_exit;
                    //   pea _rhythm_loop(pc)
                    _rhythm_command();
                    continue; // break _rhythm_loop;
                }
            } else {
// _rhythm_mml:
                mm.write(reg.a5 + W.key, (byte) reg.getD0_B());
                if (mm.readByte(reg.a5 + W.flag2) >= 0) { // break _rhythm_exit;
                    mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x20));
                    mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xfb));
                    mm.write(reg.a5 + W.at_q_work, mm.readByte(reg.a5 + W.at_q));
                    if ((mm.readByte(reg.a5 + W.ch) - 0x41) != 0) { // break _slave_rhythm;
                        mm.write(reg.a6 + Dw.RHY_DAT, (byte) (mm.readByte(reg.a6 + Dw.RHY_DAT) | reg.getD0_B()));

                        mm.write(reg.a5 + W.len, mm.readByte(reg.a1++));
                        if (mm.readByte(reg.a5 + W.len) == 0) continue; // break _rhythm_loop;
                        mm.write(reg.a5 + W.dataptr, reg.a1);
                        return;
                    }
// _slave_rhythm:
                    mm.write(reg.a6 + Dw.RHY_DAT2, (byte) (mm.readByte(reg.a6 + Dw.RHY_DAT2) | reg.getD0_B()));
                }
            }
// _rhythm_exit:
            mm.write(reg.a5 + W.len, mm.readByte(reg.a1++));
            if (mm.readByte(reg.a5 + W.len) == 0) continue; // break _rhythm_loop;

            mm.write(reg.a5 + W.dataptr, reg.a1);
            return;
        }
    }

    /**
     * MML コマンド処理(RHYTHM 部)
     */
    public void _rhythm_command() {
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());

        //_rhyc:
        switch (reg.getD0_W() / 2) {
        case 0x00:
            break;
        case 0x01:
            _RHY_81();
            break; // 81
        case 0x02:
            _RHY_82();
            break; // 82
        case 0x03:
            comcmds._COM_83();
            break; // 83 すらー
        case 0x04:
            _RHY_NOP();
            break; // 84
        case 0x05:
            _RHY_NOP();
            break; // 85
        case 0x06:
            comcmds._COM_86();
            break; // 86 同期信号送信
        case 0x07:
            comcmds._COM_87();
            break; // 87 同期信号待ち
        case 0x08:
            _RHY_NOP();
            break; // 88
        case 0x09:
            _RHY_NOP();
            break; // 89
        case 0x0a:
            _RHY_NOP();
            break; // 8A
        case 0x0b:
            _RHY_NOP();
            break; // 8B
        case 0x0c:
            _RHY_NOP();
            break; // 8C
        case 0x0d:
            _RHY_NOP();
            break; // 8D
        case 0x0e:
            _RHY_NOP();
            break; // 8E
        case 0x0f:
            _RHY_NOP();
            break; // 8F

        case 0x10:
            _RHY_NOP();
            break; // 90
        case 0x11:
            _RHY_NOP();
            break; // 91
        case 0x12:
            _RHY_NOP();
            break; // 92
        case 0x13:
            comcmds._COM_93();
            break; // 93 neg @q
        case 0x14:
            _RHY_NOP();
            break; // 94
        case 0x15:
            _RHY_NOP();
            break; // 95
        case 0x16:
            _RHY_NOP();
            break; // 96
        case 0x17:
            _RHY_NOP();
            break; // 97
        case 0x18:
            _RHY_NOP();
            break; // 98
        case 0x19:
            _RHY_NOP();
            break; // 99
        case 0x1a:
            _RHY_NOP();
            break; // 9A
        case 0x1b:
            _RHY_NOP();
            break; // 9B
        case 0x1c:
            _RHY_NOP();
            break; // 9C
        case 0x1d:
            _RHY_NOP();
            break; // 9D
        case 0x1e:
            _RHY_NOP();
            break; // 9E
        case 0x1f:
            _RHY_NOP();
            break; // 9F

        case 0x20:
            _RHY_NOP();
            break; // A0
        case 0x21:
            _RHY_NOP();
            break; // A1
        case 0x22:
            _RHY_NOP();
            break; // A2
        case 0x23:
            _RHY_NOP();
            break; // A3
        case 0x24:
            _RHY_F2();
            break; // A4
        case 0x25:
            _RHY_F5();
            break; // A5
        case 0x26:
            _RHY_F6();
            break; // A6
        case 0x27:
            _RHY_NOP();
            break; // A7
        case 0x28:
            _RHY_NOP();
            break; // A8
        case 0x29:
            _RHY_NOP();
            break; // A9
        case 0x2a:
            _RHY_NOP();
            break; // AA
        case 0x2b:
            _RHY_NOP();
            break; // AB
        case 0x2c:
            _RHY_NOP();
            break; // AC
        case 0x2d:
            _RHY_NOP();
            break; // AD
        case 0x2e:
            _RHY_NOP();
            break; // AE
        case 0x2f:
            _RHY_NOP();
            break; // AF

        case 0x30:
            comcmds._COM_B0();
            break; // B0
        case 0x31:
            _RHY_NOP();
            break; // B1
        case 0x32:
            _RHY_NOP();
            break; // B2
        case 0x33:
            _RHY_NOP();
            break; // B3
        case 0x34:
            _RHY_NOP();
            break; // B4
        case 0x35:
            _RHY_NOP();
            break; // B5
        case 0x36:
            _RHY_NOP();
            break; // B6
        case 0x37:
            _RHY_NOP();
            break; // B7
        case 0x38:
            _RHY_NOP();
            break; // B8
        case 0x39:
            _RHY_NOP();
            break; // B9
        case 0x3a:
            _RHY_NOP();
            break; // BA
        case 0x3b:
            _RHY_NOP();
            break; // BB
        case 0x3c:
            _RHY_NOP();
            break; // BC
        case 0x3d:
            _RHY_NOP();
            break; // BD
        case 0x3e:
            comcmds._COM_BE();
            break; // BE ジャンプ
        case 0x3f:
            comcmds._COM_BF();
            break; // BF

        case 0x40:
            _RHY_NOP();
            break; // C0
        case 0x41:
            _RHY_NOP();
            break; // C1
        case 0x42:
            _RHY_NOP();
            break; // C2
        case 0x43:
            _RHY_NOP();
            break; // C3
        case 0x44:
            _RHY_NOP();
            break; // C4
        case 0x45:
            _RHY_NOP();
            break; // C5
        case 0x46:
            _RHY_NOP();
            break; // C6
        case 0x47:
            _RHY_NOP();
            break; // C7
        case 0x48:
            _RHY_NOP();
            break; // C8
        case 0x49:
            _RHY_NOP();
            break; // C9
        case 0x4a:
            _RHY_NOP();
            break; // CA
        case 0x4b:
            _RHY_NOP();
            break; // CB
        case 0x4c:
            _RHY_NOP();
            break; // CC
        case 0x4d:
            _RHY_NOP();
            break; // CD
        case 0x4e:
            _RHY_NOP();
            break; // CE
        case 0x4f:
            _RHY_NOP();
            break; // CF

        case 0x50:
            _RHY_NOP();
            break; // D0
        case 0x51:
            _RHY_NOP();
            break; // D1
        case 0x52:
            _RHY_NOP();
            break; // D2
        case 0x53:
            _RHY_NOP();
            break; // D3
        case 0x54:
            _RHY_NOP();
            break; // D4
        case 0x55:
            _RHY_NOP();
            break; // D5
        case 0x56:
            _RHY_NOP();
            break; // D6
        case 0x57:
            _RHY_NOP();
            break; // D7
        case 0x58:
            _RHY_NOP();
            break; // D8
        case 0x59:
            _RHY_NOP();
            break; // D9
        case 0x5a:
            _RHY_NOP();
            break; // DA
        case 0x5b:
            _RHY_NOP();
            break; // DB
        case 0x5c:
            _RHY_NOP();
            break; // DC
        case 0x5d:
            _RHY_NOP();
            break; // DD
        case 0x5e:
            _RHY_NOP();
            break; // DE
        case 0x5f:
            _RHY_NOP();
            break; // DF

        case 0x60:
            _RHY_NOP();
            break; // E0
        case 0x61:
            _RHY_NOP();
            break; // E1
        case 0x62:
            _RHY_NOP();
            break; // E2
        case 0x63:
            _RHY_NOP();
            break; // E3
        case 0x64:
            _RHY_NOP();
            break; // E4
        case 0x65:
            _RHY_NOP();
            break; // E5
        case 0x66:
            _RHY_NOP();
            break; // E6
        case 0x67:
            _RHY_NOP();
            break; // E7
        case 0x68:
            _RHY_NOP();
            break; // E8
        case 0x69:
            _RHY_NOP();
            break; // E9
        case 0x6a:
            _RHY_NOP();
            break; // EA
        case 0x6b:
            _RHY_NOP();
            break; // EB
        case 0x6c:
            _RHY_NOP();
            break; // EC
        case 0x6d:
            comcmds._COM_ED();
            break; // ED
        case 0x6e:
            _RHY_NOP();
            break; // EE
        case 0x6f:
            _RHY_NOP();
            break; // EF

        case 0x70:
            _RHY_F0();
            break; // F0 rhythm total volume
        case 0x71:
            _RHY_F1();
            break; // F1
        case 0x72:
            _RHY_F2();
            break; // F2 volume
        case 0x73:
            _RHY_NOP();
            break; // F3
        case 0x74:
            _RHY_F4();
            break; // F4 pan
        case 0x75:
            _RHY_F5();
            break; // F5	)	くれ
        case 0x76:
            _RHY_F6();
            break; // F6(でくれ
        case 0x77:
            _RHY_NOP();
            break; // F7
        case 0x78:
            _RHY_NOP();
            break; // F8
        case 0x79:
            comcmds._COM_F9();
            break; // F9 永久ループポイントマーク
        case 0x7a:
            devopn._FM_FA();
            break; // FA Y COMMAND
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
            _RHY_FF();
            break; // FF end of data
        }
    }

    /**
     */
    public void _RHY_NOP() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x7f));
    }

    /**
     */
    public void _RHY_81() {
    }

    /**
     * 強制ダンプ
     */
    public void _RHY_82() {
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() | 0x80);
        reg.D1_L = 0x11;
        mndrv._OPN_WRITE2();
    }

    /**
     * Rhythm Total Volume
     */
    public void _RHY_F0() {
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a6 + Dw.RHY_TV, (byte) reg.getD0_B());

        reg.setD1_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD1_B() < 0) {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if ((byte) reg.getD0_B() < 0) { // break L2;
                reg.D0_L = 0;
//                break L2;
            }
        } else {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if (reg.getD0_B() >= 0x40) {
                reg.D0_L = 0x3f;
            }
        }
// L2:
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_RHY));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        reg.D1_L = 0x11;
        mndrv._OPN_WRITE2();
    }

    /**
     * Rhythm Total Volume
     */
    public void _RHY_F1() {
        reg.setD0_B(mm.readByte(reg.a6 + Dw.RHY_TV));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        } else {
            if (reg.getD0_B() >= 0x40) {
                reg.D0_L = 0x3f;
            }
        }
        mm.write(reg.a6 + Dw.RHY_TV, (byte) reg.getD0_B());

        reg.setD1_B(mm.readByte(reg.a5 + W.track_vol));
        if ((byte) reg.getD1_B() < 0) {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if ((byte) reg.getD0_B() < 0) {
                reg.D0_L = 0;
            }
        } else {
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
            if (reg.getD0_B() >= 0x40) {
                reg.D0_L = 0x3f;
            }
        }
        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_RHY));
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        reg.D1_L = 0x11;
        mndrv._OPN_WRITE2();
    }

    /**
     * volume
     */
    public void _RHY_F2() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD2_B(mm.readByte(reg.a1++));
        reg.a0 = reg.a6 + Dw.M_BD;
        if (mm.readByte(reg.a5 + W.ch) - 0x40 != 0) {
            reg.a0 = reg.a6 + Dw.S_BD;
        }
        reg.a0 = reg.a0 + (int) (short) reg.getD1_W();
        reg.D0_L = 0xc0;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a0));
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        mm.write(reg.a0, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.vol2, (byte) reg.getD2_B());
        reg.setD1_B(reg.getD1_B() + 0x18);
        mndrv._OPN_WRITE2();
    }

    /**
     * panpot
     */
    public void _RHY_F4() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD2_B(mm.readByte(reg.a1++));
        reg.a0 = reg.a6 + Dw.M_BD;
        if (mm.readByte(reg.a5 + W.ch) - 0x40 != 0) {
            reg.a0 = reg.a6 + Dw.S_BD;
        }
        reg.a0 = reg.a0 + (int) (short) reg.getD1_W();
        reg.D0_L = 0x1f;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a0));
        reg.setD0_B(reg.getD0_B() | reg.getD2_B());
        mm.write(reg.a0, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.pan_ampm, (byte) reg.getD0_B());
        reg.setD1_B(reg.getD1_B() + 0x18);
        mndrv._OPN_WRITE2();
    }

    /**
     * くれしぇんど
     */
    public void _RHY_F5() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD2_B(mm.readByte(reg.a1++));
        reg.a0 = reg.a6 + Dw.M_BD;
        if (mm.readByte(reg.a5 + W.ch) - 0x40 != 0) {
            reg.a0 = reg.a6 + Dw.S_BD;
        }
        reg.a0 = reg.a0 + (int) (short) reg.getD1_W();
        reg.setD0_B(mm.readByte(reg.a0));
        reg.setD3_B(reg.getD0_B());
        reg.setD0_B(reg.getD0_B() & 0x1f);
        reg.setD3_B(reg.getD3_B() & 0xc0);
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD2_B());
        if (reg.getD0_B() >= 0x20) {
            reg.D0_L = 0x1f;
        }
        mm.write(reg.a5 + W.vol2, (byte) reg.getD0_B());
        reg.setD0_B(reg.getD0_B() | reg.getD3_B());
        mm.write(reg.a0, (byte) reg.getD0_B());
        reg.setD1_B(reg.getD1_B() + 0x18);
        mndrv._OPN_WRITE2();
    }

    /**
     * でくれしぇんど
     */
    public void _RHY_F6() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD2_B(mm.readByte(reg.a1++));
        reg.a0 = reg.a6 + Dw.M_BD;
        if (mm.readByte(reg.a5 + W.ch) - 0x40 != 0) {
            reg.a0 = reg.a6 + Dw.S_BD;
        }
        reg.a0 = reg.a0 + (int) (short) reg.getD1_W();
        reg.setD0_B(mm.readByte(reg.a0));
        reg.setD3_B(reg.getD0_B());
        reg.setD0_B(reg.getD0_B() & 0x1f);
        reg.setD3_B(reg.getD3_B() &0xc0);
        reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD2_B());
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a5 + W.vol2, (byte) reg.getD0_B());
        reg.setD0_B(reg.getD0_B() | reg.getD3_B());
        mm.write(reg.a0, (byte) reg.getD0_B());
        reg.setD1_B(reg.getD1_B()+0x18);
        mndrv._OPN_WRITE2();
    }

    /**
     */
    public void _RHY_FF() {
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));

        reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a0 = reg.a6 + Dw.TRACKWORKADR;
        boolean L3 = false;
        do {
            if ((mm.readByte(reg.a0 + W.flag2) & 1) != 0) { // break L3;
                L3 = true;
                break;
            }
            reg.a0 = reg.a0 + W._track_work_size;
            reg.setD0_W(reg.getD0_W() - 1);
        } while (reg.getD0_W() != 0);

        if (!L3) {
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
                if (mm.readByte(reg.a0 + W.flag2) <= 0) {
                    mm.write(reg.a0 + W.flag2, (byte) (mm.readByte(reg.a0 + W.flag2) | 1));
                }
                reg.a0 = reg.a0 + W._track_work_size;
                reg.setD0_W(reg.getD0_W() - 1);
            } while (reg.getD0_W() != 0);
        }
// L3:
        reg.setD0_W(mm.readShort(reg.a1)); reg.a1 += 2;
        if (reg.getD0_W() != 0) {
            if (reg.getD0_W() - 0xffff != 0) { // break L2;
                reg.a1 = reg.a1 + (int) (short) reg.getD0_W();
                return;
            }
        } else {
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0x77));
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xfe));
            mm.write(reg.a5 + W.weffect, 0);
            comcmds._all_end_check();
            return;
        }
// L2:
        reg.a1 = mm.readInt(reg.a5 + W.loop);
    }
}
