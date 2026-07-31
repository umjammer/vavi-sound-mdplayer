package mdplayer.lib.mndrv;

import mdplayer.emu.nise68.XMemory;
import mdplayer.lib.mndrv.MnWork.Dw;
import mdplayer.lib.mndrv.MnWork.W;


//
// part of track analyze
//
public class ComAnalyze {

    public Reg reg;
    public XMemory mm;
    public Ab ab;

    public void _track_ana_quit() {
        // rts
    }

    public void _track_analyze() {

        if (mm.readByte(reg.a5 + W.flag4) < 0) {
            return;
        }

        if (mm.readByte(reg.a5 + W.flag) >= 0) {
            return;
        }

        if (mm.readByte(reg.a5 + W.reverb) >= 0) {
            _track_ana_normal();
            return;
        }

        // ----
        reg.setD4_B(mm.readByte(reg.a5 + W.len) & 0xff);
        reg.setD4_B(reg.getD4_B() - 1);

        if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) {
            _track_echo_next();
            return;
        }

        reg.D0_L = 3;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a5 + W.effect) & 0xff);
        if (reg.getD0_B() - 2 != 0) {
            _track_ana_echo_atq();
            return;
        }

        if (reg.getD4_B() - (mm.readByte(reg.a5 + W.rct) & 0xff) != 0) {
            _track_ana_echo_atq();
            return;
        }
        reg.a0 = mm.readInt(reg.a5 + W.rrcut_adrs);
        ab.hlw_rrcut_adrs.get(reg.a5).run();
        _track_ana_echo_atq();
    }

    public void _track_ana_echo_atq() {
        reg.setD0_B(mm.readByte(reg.a5 + W.at_q_work) & 0xff);
        boolean gotoL1 = false;
        if (reg.getD0_B() != 0) { // break L1;
            reg.setD0_B(reg.getD0_B() - 1);
            if (reg.getD0_B() == 0) gotoL1 = true;
        } else { // if (Reg.getD0_B() != 0) break L2;
            gotoL1 = true;
        }

        if (gotoL1) {
//L1:
            reg.a0 = mm.readInt(reg.a5 + W.echo_adrs);
            ab.hlw_echo_adrs.get(reg.a5).run();
            reg.D0_L = 0xffff_ffff;
        }
//L2:
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
        _track_echo_next();
    }

    public void _track_echo_next() {
        reg.setD0_B(mm.readByte(reg.a5 + W.reverb_time_work) & 0xff);
        if (reg.getD0_B() != 0) {
            reg.setD0_B(reg.getD0_B() - 1);
            if (reg.getD0_B() == 0) {
                reg.a0 = mm.readInt(reg.a5 + W.keyoff_adrs2);
                ab.hlw_keyoff_adrs2.get(reg.a5).run();
                reg.D0_L = 0;
            }
            mm.write(reg.a5 + W.reverb_time_work, (byte) reg.getD0_B());
        }

        mm.write(reg.a5 + W.len, (byte) reg.getD4_B());
        if (reg.getD4_B() != 0) {
            return;
        }

        if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) {
            _track_ana_fetch();
            return;
        }

        reg.a0 = mm.readInt(reg.a5 + W.echo_adrs);
        ab.hlw_echo_adrs.get(reg.a5).run();
        _track_ana_fetch();
    }

    // ----
    public void _track_ana_normal() {
        reg.setD4_B(mm.readByte(reg.a5 + W.len) & 0xff);
        reg.setD4_B(reg.getD4_B() - 1);
        if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) {
            _track_ana_next();
            return;
        }
        reg.D0_L = 3;
        reg.setD0_B(reg.getD0_B() & (mm.readByte(reg.a5 + W.effect) & 0xff));
        if (reg.getD0_B() - 2 != 0) {
            _track_ana_normal_atq();
            return;
        }
        if (reg.getD4_B() - (mm.readByte(reg.a5 + W.rct) & 0xff) != 0) {
            _track_ana_normal_atq();
            return;
        }
        reg.a0 = mm.readInt(reg.a5 + W.rrcut_adrs);
        ab.hlw_rrcut_adrs.get(reg.a5).run();
        _track_ana_normal_atq();
    }

    public void _track_ana_normal_atq() {
        reg.setD0_B(mm.readByte(reg.a5 + W.at_q_work) & 0xff);
        boolean gotoL1 = false;
        if (reg.getD0_B() != 0) { // break L1;
            reg.setD0_B(reg.getD0_B() - 1);
            if (reg.getD0_B() == 0) gotoL1 = true;
        } else { // if (Reg.getD0_B() != 0) break L2;
            gotoL1 = true;
        }

        if (gotoL1) {
//L1:
            reg.a0 = mm.readInt(reg.a5 + W.keyoff_adrs);
            ab.hlw_keyoff_adrs.get(reg.a5).run();
            reg.D0_L = 0xffff_ffff;
        }
//L2:
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
        _track_ana_next();
    }

    public void _track_ana_next() {
        mm.write(reg.a5 + W.len, (byte) reg.getD4_B());
        if (reg.getD4_B() != 0) {
            _track_ana_quit();
            return;
        }
        if ((mm.readByte(reg.a5 + W.flag3) & 0x40) != 0) { // break L1;
            reg.D0_L = 6;
            mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) & (~(1 << reg.getD0_B()))));
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & (~(1 << reg.getD0_B()))));
            reg.a0 = mm.readInt(reg.a5 + W.keyoff_adrs);
            ab.hlw_keyoff_adrs.get(reg.a5).run();
            _track_ana_fetch();
            return;
        }
//L1:
        if ((mm.readByte(reg.a5 + W.flag) & 0x40) != 0) {
            _track_ana_fetch();
            return;
        }
        reg.a0 = mm.readInt(reg.a5 + W.keyoff_adrs);
        ab.hlw_keyoff_adrs.get(reg.a5).run();
        _track_ana_fetch();
    }

    /** HACK: (MNDRV) Track Fetch */
    public void _track_ana_fetch() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x7f));
        reg.a1 = mm.readInt(reg.a5 + W.dataptr);
        // break _track_ana_fetch_L1;
        boolean dmyFlg = true;

//_track_loop:
        boolean _track_ana_rest_exit = false;
        while (true) {
            do {
                if (!dmyFlg) {
                    if (mm.readByte(reg.a5 + W.flag4) < 0) {
                        return;
                    }
                    if (mm.readByte(reg.a5 + W.flag) >= 0) {
                        return;
                    }
                }

//L1:
                dmyFlg = false;
                reg.D0_L = 0;
                reg.setD0_B(mm.readByte(reg.a1++) & 0xff);
                if ((byte) reg.getD0_B() >= 0) break; // _track_ana_mml;

                reg.setD0_B(reg.getD0_B() - 0x80);
                if (reg.getD0_B() == 0) { // break _track_ana_rest_exit;
                    _track_ana_rest_exit = true;
                    break;
                } else {
                    reg.a0 = mm.readInt(reg.a5 + W.subcmd_adrs);
                    ab.hlw_subcmd_adrs.get(reg.a5).run();
                }
            } while (true);
            //_track_loop();

            if (!_track_ana_rest_exit) {
//_track_ana_mml:

                if ((byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x8) == 0) { // break _track_ana_exit_jump;

                    reg.setD1_B(mm.readByte(reg.a5 + W.key_trans) & 0xff);
                    if ((byte) reg.getD0_B() < 0) { // break _track_ana_mml_plus;

                        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
                        if ((byte) reg.getD0_B() < 0) { //break _track_ana_mml_;
                            reg.D0_L = 0;
//                        break _track_ana_mml_;
                        }
                    } else {
//_track_ana_mml_plus:

                        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
                        if ((byte) reg.getD0_B() < 0) { // break _track_ana_mml_;
                            reg.D0_L = 0x7f;
                        }
                    }
//_track_ana_mml_:
                    boolean _track_ana_exit = false;
                    if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) { // break _track_ana_mml1;
                        reg.setD1_B(mm.readByte(reg.a5 + W.flag) & 0xff);
                        if ((reg.D1_L & 0x40) != 0) { // break _track_ana_mml1;
                            if ((reg.D1_L & 0x02) == 0) { // break _track_ana_mml1;
                                if (reg.getD0_B() - (mm.readByte(reg.a5 + W.key) & 0xff) == 0) { // break _track_ana_exit;
                                    _track_ana_exit = true;
                                } else {
                                    mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x1));
                                }
                            }
                        }
                    }
//_track_ana_mml1:
                    if (!_track_ana_exit) {
                        reg.a0 = mm.readInt(reg.a5 + W.setnote_adrs);
                        ab.hlw_setnote_adrs.get(reg.a5).run();
                    }

//_track_ana_exit:

                    reg.a0 = mm.readInt(reg.a5 + W.inithlfo_adrs);
                    ab.hlw_inithlfo_adrs.get(reg.a5).run();
                }
//_track_ana_exit_jump:

                reg.D0_L = 0;
                reg.setD0_B(mm.readByte(reg.a1++) & 0xff);
                if (reg.getD0_B() == 0) {
                    continue; // break _track_loop;
                }
                mm.write(reg.a5 + W.len, (byte) reg.getD0_B());
                if ((byte) (mm.readByte(reg.a5 + W.flag2) & 0x40) != 0) {
                    _track_ana_exit_atq();
                    return;
                }

                reg.a0 = mm.readInt(reg.a5 + W.qtjob);
                ab.hlw_qtjob.get(reg.a5).run();
                _track_ana_exit_();

                return;
            }
//_track_ana_rest_exit:

            reg.setD0_B(mm.readByte(reg.a6 + Dw.DRV_FLAG2) & 0xff);
            if ((byte) reg.getD0_B() >= 0) { // break L1;
                if ((reg.D0_L & 0x40) == 0) { // break L1;
                    reg.a0 = mm.readInt(reg.a5 + W.keyoff_adrs);
                    ab.hlw_keyoff_adrs.get(reg.a5).run();
                }
            }
//L1:
            reg.setD0_B(mm.readByte(reg.a1++) & 0xff);
            if (reg.getD0_B() == 0) continue; // break _track_loop;
            mm.write(reg.a5 + W.len, (byte) reg.getD0_B());
            reg.a0 = mm.readInt(reg.a6 + Dw.TRKANA_RESTADR);
            ab.hlTRKANA_RESTADR.get(reg.a6).run(); // Execute the Runnable at position a6
            return;
        }
    }

    public void _track_ana_exit_atq() {
        if ((byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG2) & 0x10) != 0) {
            _track_ana_exit_atq_new();
            return;
        }
        reg.setD0_B(reg.getD0_B() - (mm.readByte(reg.a5 + W.at_q) & 0xff));
        if (reg.getD0_B() == 0) {
            reg.D0_L = 0xffffffff;
        }
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
        _track_ana_exit_atq_final();
    }

    public void _track_ana_exit_atq_new() {
        reg.D1_L = 0;
        reg.setD1_B(mm.readByte(reg.a5 + W.at_q) & 0xff);
        reg.setD0_W(reg.getD0_W() - (int) (short) reg.getD1_W());
        if (/* signed */ (short) reg.getD0_W() > 0) {
            mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
        } else {
            mm.write(reg.a5 + W.at_q_work, (byte) 1);
        }
        _track_ana_exit_atq_final();
    }

    public void _track_ana_exit_atq_final() {
        if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x20) != 0) {
            mm.write(reg.a5 + W.at_q_work, mm.readByte(reg.a5 + W.at_q));
        }
        _track_ana_exit_();
    }

    public void _track_ana_exit_() {
        if ((mm.readByte(reg.a1) & 0xff) - 0x81 != 0) {
            if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) {
                mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xbf));
            }
            mm.write(reg.a5 + W.dataptr, reg.a1);
            return;
        }
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x40));
        reg.a1 += 1;
        mm.write(reg.a5 + W.dataptr, reg.a1);
    }

    public void _track_ana_rest_old() {
        if ((byte) (mm.readByte(reg.a5 + W.flag2) & 0x40) == 0) {
            mm.write(reg.a5 + W.at_q, (byte) 0);
        }
        if ((mm.readByte(reg.a1) & 0xff) - 0x81 != 0) {
            if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) {
                mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xbf));
            }
            mm.write(reg.a5 + W.dataptr, reg.a1);
            return;
        }
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x40));
        reg.a1 += 1;
        mm.write(reg.a5 + W.dataptr, reg.a1);
    }

    public void _track_ana_rest_new() {
        if ((byte) (mm.readByte(reg.a5 + W.flag2) & 0x40) == 0) {
            mm.write(reg.a5 + W.at_q, (byte) reg.getD0_B());
            mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
        }
        if ((mm.readByte(reg.a1) & 0xff) - 0x81 != 0) {
            if ((byte) (mm.readByte(reg.a5 + W.flag3) & 0x40) == 0) {
                mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xbf));
            }
            mm.write(reg.a5 + W.dataptr, reg.a1);
            return;
        }
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x40));
        reg.a1 += 1;
        mm.write(reg.a5 + W.dataptr, reg.a1);
    }
}
