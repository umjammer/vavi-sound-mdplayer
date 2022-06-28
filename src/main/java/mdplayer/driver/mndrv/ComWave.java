package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


//
//	part of wavememory
//
public class ComWave {

    public Reg reg;
    public Ab ab;
    public XMemory mm;
    public ComLfo comlfo;

    private int cf = 0;
    private byte val = 0;

    //─────────────────────────────────────
    //
    public void _wave_init_kon() {
        _weffect_init_kon();

        reg.D0_L = 0x7e;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.lfo));
        if (reg.getD0_B() == 0) {
            return;
        }

        cf = reg.getD0_B() & 2;
        reg.setD0_B(reg.getD0_B() >> 2);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wp_pattern1;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kon_common(val);
            }
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wp_pattern2;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kon_common(val);
            }
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wp_pattern3;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kon_common(val);
            }
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wv_pattern1;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kon_common_a(val);
            }
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wv_pattern2;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kon_common_a(val);
            }
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf == 0) {
            return;
        }

        reg.a3 = reg.a5 + W.wv_pattern3;
        val = mm.readByte(reg.a3 + W_W.use_flag);
        if (val != 0) {
            _wave_init_kon_common_a(val);
        }
    }

    public void _wave_init_kon_common_a(byte val) {
        if (val >= 0) {
            _wave_init_compatible_a();
            return;
        }

        mm.write(reg.a3 + W_W.exec_flag, (byte) 0);
        mm.write(reg.a3 + W_W.loop_flag, (byte) 0);

        mm.write(reg.a3 + W_W.adrs_work, mm.readInt(reg.a3 + W_W.start));
        mm.write(reg.a3 + W_W.start_adrs_work, mm.readInt(reg.a3 + W_W.loop_start));
        mm.write(reg.a3 + W_W.end_adrs_work, mm.readInt(reg.a3 + W_W.loop_end));
        mm.write(reg.a3 + W_W.lp_cnt_work, mm.readInt(reg.a3 + W_W.loop_count));
        if (mm.readInt(reg.a3 + W_W.loop_count) == 0) {
            return;
        }
        mm.write(reg.a3 + W_W.loop_flag, 0xff);
    }

    public void _wave_init_compatible_a() {
        if ((mm.readByte(reg.a6 + W.flag3) & 0x40) == 0) {
            if ((mm.readByte(reg.a6 + W.flag) & 0x40) != 0) return;
        }
        mm.write(reg.a3 + W_W.ko_start, mm.readShort(reg.a3 + W_W.start));
        mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.loop_start));
        mm.write(reg.a3 + W_W.ko_loop_end, mm.readShort(reg.a3 + W_W.loop_end));

    }

    public void _wave_init_kon_common(byte val) {
        if (val >= 0) {
            _wave_init_compatible();
            return;
        }

        mm.write(reg.a3 + W_W.exec_flag, (byte) 0);
        mm.write(reg.a3 + W_W.loop_flag, (byte) 0);

        mm.write(reg.a3 + W_W.adrs_work, mm.readInt(reg.a3 + W_W.start));
        mm.write(reg.a3 + W_W.start_adrs_work, mm.readInt(reg.a3 + W_W.loop_start));
        mm.write(reg.a3 + W_W.end_adrs_work, mm.readInt(reg.a3 + W_W.loop_end));
        mm.write(reg.a3 + W_W.lp_cnt_work, mm.readInt(reg.a3 + W_W.loop_count));
        if (mm.readInt(reg.a3 + W_W.loop_count) == 0) {
            return;
        }
        mm.write(reg.a3 + W_W.loop_flag, (byte) 0xff);
    }

    public void _wave_init_compatible() {
        if ((mm.readByte(reg.a6 + W.flag3) & 0x40) == 0) {
            if ((mm.readByte(reg.a6 + W.flag) & 0x40) != 0) return;
        }
        mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.loop_start));
        mm.write(reg.a3 + W_W.ko_loop_end, mm.readInt(reg.a3 + W_W.loop_end));
        mm.write(reg.a3 + W_W.ko_loop_count, mm.readInt(reg.a3 + W_W.loop_count));

    }

    //─────────────────────────────────────
    //
    public void _wave_init_kof() {
        _weffect_init_kof();

        reg.D0_L = 0x7e;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.lfo));
        if (reg.getD0_B() == 0) return;

        cf = reg.getD0_B() & 0x2;
        reg.setD0_B(reg.getD0_B() >> 2);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wp_pattern1;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kof_common(val);
            }
        }

        cf = reg.getD0_B() & 0x1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wp_pattern2;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kof_common(val);
            }
        }

        cf = reg.getD0_B() & 0x1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wp_pattern3;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kof_common(val);
            }
        }

        cf = reg.getD0_B() & 0x1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wv_pattern1;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kof_common(val);
            }
        }

        cf = reg.getD0_B() & 0x1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.wv_pattern2;
            val = mm.readByte(reg.a3 + W_W.use_flag);
            if (val != 0) {
                _wave_init_kof_common(val);
            }
        }

        cf = reg.getD0_B() & 0x1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf == 0) {
            return;
        }

        reg.a3 = reg.a5 + W.wv_pattern3;
        val = mm.readByte(reg.a3 + W_W.use_flag);
        if (val == 0) return;// break L9;

        _wave_init_kof_common(val);
    }

    public void _wave_init_kof_common(byte val) {
        if (val >= 0) {
            return;
        }

        if (mm.readByte(reg.a3 + W_W.ko_flag) == 0) {
            return;
        }

        mm.write(reg.a3 + W_W.exec_flag, (byte) 0);
        mm.write(reg.a3 + W_W.loop_flag, (byte) 0);

        mm.write(reg.a3 + W_W.adrs_work, mm.readInt(reg.a3 + W_W.ko_start));
        mm.write(reg.a3 + W_W.start_adrs_work, mm.readInt(reg.a3 + W_W.ko_loop_start));
        mm.write(reg.a3 + W_W.end_adrs_work, mm.readInt(reg.a3 + W_W.ko_loop_end));
        mm.write(reg.a3 + W_W.lp_cnt_work, mm.readInt(reg.a3 + W_W.ko_loop_count));
        if (mm.readInt(reg.a3 + W_W.ko_loop_count) != 0) {
            mm.write(reg.a3 + W_W.loop_flag, (byte) 0xff);
        }
    }


    //─────────────────────────────────────
    //
    public void _weffect_init_kon() {
        reg.setD0_B(mm.readByte(reg.a5 + W.weffect));
        if ((byte) reg.getD0_B() >= 0) return;

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern1;
            _weffect_init_kon_common();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern2;
            _weffect_init_kon_common();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern3;
            _weffect_init_kon_common();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern4;
            _weffect_init_kon_common();
        }
    }

    public void _weffect_init_kon_common() {
        reg.setD1_B(mm.readByte(reg.a3 + W_We.mode));
        if ((byte) reg.getD1_B() < 0) return;

        cf = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (cf != 0) {
            _effect_exec();
            return;
        }

        mm.write(reg.a3 + W_We.exec_flag, (byte) 0);
        mm.write(reg.a3 + W_We.loop_flag, (byte) 0);

        mm.write(reg.a3 + W_We.adrs_work, mm.readInt(reg.a3 + W_We.start));
        mm.write(reg.a3 + W_We.start_adrs_work, mm.readInt(reg.a3 + W_We.loop_start));
        mm.write(reg.a3 + W_We.end_adrs_work, mm.readInt(reg.a3 + W_We.loop_end));
        mm.write(reg.a3 + W_We.lp_cnt_work, mm.readInt(reg.a3 + W_We.loop_count));
        if (mm.readInt(reg.a3 + W_We.loop_count) != 0) {
            mm.write(reg.a3 + W_We.loop_flag, (byte) 0xff);
        }
    }

    //─────────────────────────────────────
    //
    public void _weffect_init_kof() {
        reg.setD0_B(mm.readByte(reg.a5 + W.weffect));
        if ((byte) reg.getD0_B() >= 0) return;

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern1;
            _weffect_init_kof_common();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern2;
            _weffect_init_kof_common();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern3;
            _weffect_init_kof_common();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf == 0) {
            return;
        }

        reg.a3 = reg.a5 + W.we_pattern4;
        _weffect_init_kof_common();
    }

    public void _weffect_init_kof_common() {
        reg.setD1_B(mm.readByte(reg.a3 + W_We.mode));
        if ((byte) reg.getD1_B() < 0) return;

        cf = reg.getD1_B() & 1;
        reg.setD1_B(reg.getD1_B() >> 1);
        if (cf != 0) {
            return;
        }

        mm.write(reg.a3 + W_We.exec_flag, (byte) 0);
        mm.write(reg.a3 + W_We.loop_flag, (byte) 0);

        mm.write(reg.a3 + W_We.adrs_work, mm.readInt(reg.a3 + W_We.ko_start));
        mm.write(reg.a3 + W_We.start_adrs_work, mm.readInt(reg.a3 + W_We.ko_loop_start));
        mm.write(reg.a3 + W_We.end_adrs_work, mm.readInt(reg.a3 + W_We.ko_loop_end));
        mm.write(reg.a3 + W_We.lp_cnt_work, mm.readInt(reg.a3 + W_We.ko_loop_count));
        if (mm.readInt(reg.a3 + W_We.ko_loop_count) != 0) {
            mm.write(reg.a3 + W_We.loop_flag, (byte) 0xff);
        }
    }

    //─────────────────────────────────────
    //─────────────────────────────────────
    //	wavememory effect
    //
    public void _ch_effect() {
        reg.setD0_B(mm.readByte(reg.a5 + W.weffect));
        if ((byte) reg.getD0_B() >= 0) return;

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern1;
            _ch_effect_exec();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern2;
            _ch_effect_exec();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern3;
            _ch_effect_exec();
        }

        cf = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (cf != 0) {
            reg.a3 = reg.a5 + W.we_pattern4;
            _ch_effect_exec();
        }

    }

    public void _ch_effect_exec() {
        reg.setD4_B(mm.readByte(reg.a3 + W_We.mode));
        if ((byte) reg.getD4_B() >= 0) {
            cf = reg.getD4_B() & 1;
            reg.setD4_B(reg.getD4_B() >> 1);
            if (cf != 0) {
                return;
            }
        }

        reg.setD4_B(mm.readByte(reg.a3 + W_We.exec));
        if ((byte) reg.getD4_B() >= 0) {
            _effect_exec();
            return;
        }
        cf = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (cf == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
                _effect_exec();
            }
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _effect_exec();
        }
    }

    //─────────────────────────────────────
    public void _effect_exec() {
        short sp = (short) reg.getD0_W();

        mm.write(reg.a3 + W_We.delay_work, (byte) (mm.readByte(reg.a3 + W_We.delay_work) - 1));
        if (mm.readByte(reg.a3 + W_We.delay_work) == 0) { // break _weffect_exit;
            mm.write(reg.a3 + W_We.delay_work, mm.readByte(reg.a3 + W_We.speed));

            if (mm.readByte(reg.a3 + W_We.exec_flag) == 0) { // break _weffect_exit;

                reg.a0 = mm.readInt(reg.a3 + W_We.adrs_work);
                reg.setD0_W(mm.readShort(reg.a0));
                reg.a0 += 2;
                if (mm.readInt(reg.a3 + W_We.end_adrs_work) - reg.a0 == 0) { // break _weffect_10;

                    if (mm.readByte(reg.a3 + W_We.count) == 0) {
                        mm.write(reg.a3 + W_We.exec_flag, 0xff);
//                        break _weffect_10;
                    } else {

                        reg.a0 = mm.readInt(reg.a3 + W_We.start_adrs_work);

                        if (mm.readByte(reg.a3 + W_We.loop_flag) != 0) { // break _weffect_10;

                            mm.write(reg.a3 + W_We.lp_cnt_work, mm.readInt(reg.a3 + W_We.lp_cnt_work) - 1);
                            if (mm.readInt(reg.a3 + W_We.lp_cnt_work) == 0) {
                                mm.write(reg.a3 + W_We.exec_flag, (byte) 0xff);
                            }
                        }
                    }
                }
// _weffect_10:
                mm.write(reg.a3 + W_We.adrs_work, reg.a0);
                reg.a0 = mm.readInt(reg.a3 + W_We.exec_adrs);
                ab.hlw_we_exec_adrs.get(reg.a0).run();
            }
        }
// _weffect_exit:
        reg.setD0_W(sp);
    }

    //─────────────────────────────────────
    //─────────────────────────────────────
    //	wavememory
    //
    public void _com_wavememory() {
        reg.setD4_W(mm.readShort(reg.a4 + W_L.flag));
        if ((short) reg.getD4_W() >= 0) {
            _com_wave_exec();
            return;
        }

        cf = reg.getD4_B() & 1;
        reg.setD4_B(reg.getD4_B() >> 1);
        if (cf == 0) {
            if ((mm.readByte(reg.a5 + W.flag) & 0x20) == 0) {
                _com_wave_exec();
            }
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag) & 0x20) != 0) {
            _com_wave_exec();
        }
    }

    //─────────────────────────────────────
    public void _com_wave_exec() {
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a3 + W_W.type));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_W()) {
        case 2:
            _com_wave_normal();     // $00 repeat
            break;
        case 4:
            _com_wave_normal();        // $01 1shot
            break;
        case 6:
            _com_wave_nop();        // $02
            break;
        case 8:
            _com_wave_nop();        // $03
            break;
        case 10:
            _com_wave_nop();        // $04
            break;
        case 12:
            _com_wave_nop();        // $05
            break;
        case 14:
            _com_wave_nop();        // $06
            break;
        case 16:
            _com_wave_nop();        // $07
            break;
        case 18:
            _com_wave_nop();        // $08
            break;
        case 20:
            _com_wave_nop();        // $09
            break;
        case 22:
            _com_wave_nop();        // $0A
            break;
        case 24:
            _com_wave_nop();        // $0B
            break;
        case 26:
            _com_wave_nop();        // $0C
            break;
        case 28:
            _com_wave_nop();        // $0D
            break;
        case 30:
            _com_wave_nop();        // $0E
            break;
        case 32:
            _com_wave_lw();        // $0F
            break;

        case 34:
            _com_wave_saw();        // $10
            break;
        case 36:
            _com_wave_square();    // $11
            break;
        case 38:
            _com_wave_triangle();    // $12
            break;
        case 40:
            _com_wave_randome();    // $13
            break;
        case 42:
            _com_wave_saw_a();        // $14
            break;
        case 44:
            _com_wave_square_a();    // $15
            break;
        case 46:
            _com_wave_triangle_a();    // $16
            break;
        case 48:
            _com_wave_randome_a();    // $17
            break;
        case 50:
            _com_wave_nop();        // $18
            break;
        case 52:
            _com_wave_nop();        // $19
            break;
        case 54:
            _com_wave_nop();        // $1A
            break;
        case 56:
            _com_wave_nop();        // $1B
            break;
        case 58:
            _com_wave_nop();        // $1C
            break;
        case 60:
            _com_wave_nop();        // $1D
            break;
        case 62:
            _com_wave_nop();        // $1E
            break;
        case 64:
            _com_wave_nop();        // $1F
            break;
        }
    }

    //─────────────────────────────────────
    public void _com_wave_nop() {
        reg.D0_L = 0;
    }

    //─────────────────────────────────────
    public void _com_wave_normal() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            if (mm.readByte(reg.a3 + W_W.exec_flag) == 0) { // break _com_wave_exit;

                reg.a0 = mm.readInt(reg.a3 + W_W.adrs_work);
                reg.D0_L = 0;
                reg.setD0_B(mm.readByte(reg.a3 + W_W.depth));
                reg.D0_L = (short) (mm.readShort(reg.a0) * (short) reg.getD0_W()); // 68020未満のCPUはw*W=lのみ?

                if (reg.a0 - mm.readInt(reg.a3 + W_W.end_adrs_work) == 0) { // break _com_w10;

                    if (mm.readByte(reg.a3 + W_W.type) == 0) {
                        mm.write(reg.a3 + W_W.exec_flag, 0xff);
//                        break _com_w10;
                    } else {
                        reg.a0 = mm.readInt(reg.a3 + W_W.start_adrs_work);
                        if (mm.readByte(reg.a3 + W_W.loop_flag) != 0) { // break _com_w10;

                            mm.write(reg.a3 + W_W.lp_cnt_work, mm.readInt(reg.a3 + W_W.lp_cnt_work) - 1);
                            if (mm.readInt(reg.a3 + W_W.lp_cnt_work) == 0) {
                                mm.write(reg.a3 + W_W.exec_flag, (byte) 0xff);
                            }
                        }
                    }
                }
// _com_w10:
                mm.write(reg.a3 + W_W.adrs_work, reg.a0);
                mm.write(reg.a4 + W_L.bendwork, (short) reg.getD0_W());
                return;
            }
        }
// _com_wave_exit:
        reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    //─────────────────────────────────────
    public void _com_wave_lw() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) != 0) { // break _com_wave_k_exit;
            reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
            return;
        }

        reg.a0 = mm.readInt(reg.a3 + W_W.adrs_work);
// _com_wave_k_loop:
        while (true) {
            reg.setD0_B(mm.readByte(reg.a0++));
            if (reg.getD0_B() - 0x80 == 0) continue; // break _com_wave_k_loop;
            if (reg.getD0_B() < 0xf0) { // break _com_wave_k_command;

// _com_wave_k1:
                mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));
                reg.setD1_B(reg.getD0_B());
                reg.setD0_W(reg.getD0_W() & 0x7f);
                reg.D2_L = 0;
                reg.setD2_B(mm.readByte(reg.a3 + W_W.depth));
                reg.D0_L = reg.getD2_W() * reg.getD0_W();
                if ((byte) reg.getD1_B() < 0) {
                    reg.setD0_W((short) (-(short) reg.getD0_W()));
                }
                mm.write(reg.a3 + W_W.adrs_work, reg.a0);
                mm.write(reg.a4 + W_L.bendwork, (short) reg.getD0_W());
                return;

// _com_wave_k_exit:
//                reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
//                return;
            }
// _com_wave_k_command:
            if (reg.getD0_B() - 0xff == 0) return; // break _com_wave_k_command_end;

            reg.setD0_B(reg.getD0_B() & 0xf);
            if (reg.getD0_B() != 0) { // break _com_wave_k_command_go_loop;

                mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD0_B());
                continue; // break _com_wave_k_loop;

// _com_wave_k_command_end:
//                return;
            }
// _com_wave_k_command_go_loop:
            mm.write(reg.a3 + W_W.lp_cnt_work, mm.readInt(reg.a3 + W_W.lp_cnt_work) & 1);
            if (mm.readInt(reg.a3 + W_W.lp_cnt_work) == 0) {
                mm.write(reg.a3 + W_W.loop_start, reg.a0);
                continue; // break _com_wave_k_loop;
            }
            reg.D0_L = 0;
            reg.setD0_B(mm.readByte(reg.a0++));
            if (reg.D0_L - mm.readInt(reg.a3 + W_W.lp_cnt_work) == 0) {
                mm.write(reg.a3 + W_W.lp_cnt_work, 0);
                if (reg.getD0_B() - 0xff != 0) {
                    reg.D0_L = 0xff;
                    mm.write(reg.a3 + W_W.lp_cnt_work, reg.D0_L);
                }
            }
            reg.a0 = mm.readInt(reg.a3 + W_W.loop_start);
            // break _com_wave_k_loop;
        }
    }

    //─────────────────────────────────────
    //
    public void _com_wave_saw() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_saw_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            reg.D0_L = mm.readInt(reg.a3 + W_W.ko_loop_end);
            mm.write(reg.a3 + W_W.ko_loop_count, mm.readInt(reg.a3 + W_W.ko_loop_count) + reg.D0_L);
            mm.write(reg.a3 + W_W.ko_loop_start, (short) (mm.readShort(reg.a3 + W_W.ko_loop_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_loop_start) == 0) {
                mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.start));
                mm.write(reg.a3 + W_W.ko_loop_count, -(int) mm.readByte(reg.a3 + W_W.ko_loop_count));
            }
            mm.write(reg.a4 + W_L.bendwork, mm.readShort(reg.a3 + W_W.ko_loop_count));
        }
// _com_wave_saw_exit:
        reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    //─────────────────────────────────────
    //
    public void _com_wave_square() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_square_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            reg.D0_L = mm.readInt(reg.a3 + W_W.ko_loop_end);
            mm.write(reg.a3 + W_W.ko_loop_count, reg.D0_L);
            mm.write(reg.a3 + W_W.ko_loop_start, (short) (mm.readShort(reg.a3 + W_W.ko_loop_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_loop_start) == 0) {
                mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.start));
                mm.write(reg.a3 + W_W.ko_loop_end, -(int) mm.readInt(reg.a3 + W_W.ko_loop_end));
            }
            mm.write(reg.a4 + W_L.bendwork, mm.readShort(reg.a3 + W_W.ko_loop_count));
        }
// _com_wave_square_exit:
        reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    //─────────────────────────────────────
    //
    public void _com_wave_triangle() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_triangle_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            reg.D0_L = mm.readInt(reg.a3 + W_W.ko_loop_end);
            mm.write(reg.a3 + W_W.ko_loop_count, mm.readInt(reg.a3 + W_W.ko_loop_count) + reg.D0_L);
            mm.write(reg.a3 + W_W.ko_loop_start, (short) (mm.readShort(reg.a3 + W_W.ko_loop_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_loop_start) == 0) {
                mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.start));
                mm.write(reg.a3 + W_W.ko_loop_end, -(int) mm.readInt(reg.a3 + W_W.ko_loop_end));
            }
            mm.write(reg.a4 + W_L.bendwork, mm.readShort(reg.a3 + W_W.ko_loop_count));
        }
// _com_wave_triangle_exit:
        reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    //─────────────────────────────────────
    //
    public void _com_wave_randome() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_randome_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            mm.write(reg.a3 + W_W.ko_loop_start, (short) (mm.readShort(reg.a3 + W_W.ko_loop_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_loop_start) == 0) {
                mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.start));
                comlfo.getRandom();
                reg.D1_L = mm.readInt(reg.a3 + W_W.ko_loop_end);
                reg.D0_L = (short) ((short) reg.getD1_W() * (short) reg.getD0_W());
                mm.write(reg.a3 + W_W.ko_loop_count, reg.D0_L);
            }
            mm.write(reg.a4 + W_L.bendwork, mm.readShort(reg.a3 + W_W.ko_loop_count));
        }
// _com_wave_randome_exit:
        reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
    }

    //─────────────────────────────────────
    //
    public void _com_wave_saw_a() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_saw_a_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            reg.setD0_W(mm.readShort(reg.a3 + W_W.ko_loop_start));
            mm.write(reg.a3 + W_W.ko_loop_end, (short) (mm.readShort(reg.a3 + W_W.ko_loop_end) + (short) reg.getD0_W()));
            mm.write(reg.a3 + W_W.ko_start, (short) (mm.readShort(reg.a3 + W_W.ko_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_start) == 0) {
                mm.write(reg.a3 + W_W.ko_start, mm.readShort(reg.a3 + W_W.start));
                mm.write(reg.a3 + W_W.ko_loop_end, mm.readShort(reg.a3 + W_W.loop_end));
            }
            reg.setD0_B(mm.readByte(reg.a3 + W_W.ko_loop_end));
            reg.setD0_W((short) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.bendwork, (short) reg.getD0_W());
        } else {
// _com_wave_saw_a_exit:
            reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
        }
    }

    //─────────────────────────────────────
    //
    public void _com_wave_square_a() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_square_a_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            reg.setD0_W(mm.readShort(reg.a3 + W_W.ko_loop_start));
            mm.write(reg.a3 + W_W.ko_start, (short) (mm.readShort(reg.a3 + W_W.ko_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_start) == 0) {
                mm.write(reg.a3 + W_W.ko_start, mm.readShort(reg.a3 + W_W.start));
                mm.write(reg.a3 + W_W.ko_loop_end, (short) (mm.readShort(reg.a3 + W_W.ko_loop_end) + (short) reg.getD0_W()));
                mm.write(reg.a3 + W_W.ko_loop_start, (short) (-(short) mm.readShort(reg.a3 + W_W.ko_loop_start)));
            }
            reg.setD0_B(mm.readByte(reg.a3 + W_W.ko_loop_end));
            reg.setD0_W((short) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.bendwork, (short) reg.getD0_W());
        } else {
// _com_wave_square_a_exit:
            reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
        }
    }

    //─────────────────────────────────────
    //
    public void _com_wave_triangle_a() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_triangle_a_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            reg.setD0_W(mm.readShort(reg.a3 + W_W.ko_loop_start));
            mm.write(reg.a3 + W_W.ko_loop_end, (short) (mm.readShort(reg.a3 + W_W.ko_loop_end) + (short) reg.getD0_W()));
            mm.write(reg.a3 + W_W.ko_start, (short) (mm.readShort(reg.a3 + W_W.ko_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_start) == 0) {
                mm.write(reg.a3 + W_W.ko_start, mm.readShort(reg.a3 + W_W.start));
                mm.write(reg.a3 + W_W.ko_loop_start, (short) (-(short) mm.readShort(reg.a3 + W_W.ko_loop_start)));
            }
            reg.setD0_B(mm.readByte(reg.a3 + W_W.ko_loop_end));
            reg.setD0_W((short) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.bendwork, (short) reg.getD0_W());
        } else {
// _com_wave_triangle_a_exit:
            reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
        }
    }

    //─────────────────────────────────────
    //
    public void _com_wave_randome_a() {
        mm.write(reg.a4 + W_L.delay_work, (byte) (mm.readByte(reg.a4 + W_L.delay_work) - 1));
        if (mm.readByte(reg.a4 + W_L.delay_work) == 0) { // break _com_wave_randome_a_exit;
            mm.write(reg.a4 + W_L.delay_work, mm.readByte(reg.a4 + W_L.lfo_sp));

            mm.write(reg.a3 + W_W.ko_start, (short) (mm.readShort(reg.a3 + W_W.ko_start) - 1));
            if (mm.readShort(reg.a3 + W_W.ko_start) == 0) {
                mm.write(reg.a3 + W_W.ko_start, mm.readShort(reg.a3 + W_W.start));
                comlfo.getRandom();
                reg.setD1_W(mm.readShort(reg.a3 + W_W.ko_loop_start));
                reg.D0_L = (short) ((short) reg.getD1_W() * (short) reg.getD0_W());
                mm.write(reg.a3 + W_W.ko_loop_end, (short) reg.getD0_W());
            }
            reg.setD0_B(mm.readByte(reg.a3 + W_W.ko_loop_end));
            reg.setD0_W((short) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.bendwork, (short) reg.getD0_W());
        } else {
// _com_wave_randome_a_exit:
            reg.setD0_W(mm.readShort(reg.a4 + W_L.bendwork));
        }
    }
}
