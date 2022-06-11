package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


//
// part of INTERRUPT MAIN
//
public class Interrupt {
    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public DevPsg devpsg;
    public DevOpn devopn;
    public DevOpm devopm;
    public DevMPcm devmpcm;
    public Ab ab;
    public FMTimer timerOPM;
    public FMTimer timerOPN;

    /** */
    public static final byte[] _opn_irq = new byte[] {
            0x30, 0x1F, 0x2F, 0x3F
    };

    public static final short[] _opn_intmask = new short[] {
            0x200, 0x200, 0x200, 0x300, 0x400, 0x500, 0x600, 0x700
    };

    /**
     * OPN timer
     */
    public void _opn_entry() {
        reg.sr |= 0x700;

        int spA6 = reg.a6;

        reg.a6 = MnDrv._work_top;
        byte f = (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) & 1);
        mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) | 1));
        if (f != 0) {
            // _opn_entry_short_exit
            reg.a6 = spA6;
            return;
        }

        Reg spReg = new Reg();
        spReg.D0_L = reg.D0_L;
        spReg.D1_L = reg.D1_L;
        spReg.D2_L = reg.D2_L;
        spReg.D3_L = reg.D3_L;
        spReg.D4_L = reg.D4_L;
        spReg.D5_L = reg.D5_L;
        spReg.D6_L = reg.D6_L;
        spReg.D7_L = reg.D7_L;
        spReg.a0 = reg.a0;
        spReg.a1 = reg.a1;
        spReg.a2 = reg.a2;
        spReg.a3 = reg.a3;
        spReg.a4 = reg.a4;
        spReg.a5 = reg.a5;

        reg.a0 = 0xecc0c1;
        reg.D0_L = 3;
        do {
            reg.setD3_B((byte) timerOPN.readStatus());
        } while ((byte) reg.getD3_B() < 0);

        reg.setD3_W(reg.getD3_W() & reg.getD0_W());

        reg.D0_L = 7;
        reg.D0_L &= 0; // Reg.Arg[0]; // 4 * 15(sp) -> 割り込みレベルの調整(下げている)を行っている(再現不要箇所)
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD4_W(reg.getSR_W());
        reg.setD4_W(reg.getD4_W() & 0xf8ff);
        reg.setD4_W(reg.getD4_W() | _opn_intmask[reg.getD0_W() / 2]);

_opn_recall:
        while (true) {
            reg.D7_L = 0;
            reg.D1_L = 0x27;
            reg.setD0_B(_opn_irq[reg.getD3_W()]);
            reg.setD0_B(reg.getD0_B() | mm.readByte(reg.a6 + Dw.CH3MODEM));
            mndrv._OPN_WRITE();
            reg.setSR_W(reg.getD4_W());

            int sp = reg.getD4_W();
            reg.setD3_W(reg.getD3_W() + (int) (short) reg.getD3_W());
            switch (reg.getD3_W()) {
            case 0:
                break _opn_recall; // _opn_entry_exit;
            case 2:
                _timer_a_job();
                break;
            case 4:
                _timer_b_job();
                break;
            case 6:
                _timer_ab_job();
                break;
            }
            reg.setD4_W(sp);

            reg.setD0_B(mm.readByte(reg.a6 + Dw.TEMPO3));
            if (mm.readByte(reg.a6 + Dw.TEMPO2) - reg.getD0_B() != 0) {
                mm.write(reg.a6 + Dw.TEMPO2, (byte) reg.getD0_B());
                reg.D7_L = 0;
                reg.D1_L = 0x26;
                mndrv._OPN_WRITE();
            }

            reg.sr |= 0x700;
            reg.a0 = 0xecc0c1;
            reg.D0_L = 3;
            do {
                reg.setD3_B((byte) timerOPN.readStatus());
            } while ((byte) reg.getD3_B() < 0);
            reg.setD3_W(reg.getD3_W() & reg.getD0_W());
            if (reg.getD3_W() != 0) continue; // break _opn_recall;

            mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0xfe));
            reg.sr = reg.getD4_W();

            reg.setD7_W(mm.readShort(reg.a6 + Dw.INTEXECNUM));
            if (reg.getD7_W() != 0) {
                reg.a0 = reg.a6 + Dw.INTEXECBUF;
                mm.write(reg.a6 + Dw.INTEXECNUM, (short) 0);
                reg.setD7_W(reg.getD7_W() - 1);
                do {
                    reg.a1 = mm.readInt(reg.a0);
                    reg.a0 += 4;
                    Reg spReg2 = new Reg();
                    spReg2.D7_L = reg.D7_L;
                    spReg2.a0 = reg.a0;
                    spReg2.a6 = reg.a6;
                    ab.hlINTEXECBUF.get(reg.a1).run();
                    reg.D7_L = spReg2.D7_L;
                    reg.a0 = spReg2.a0;
                    reg.a6 = spReg2.a6;
                } while (reg.decAfterD7_W() != 0);
            }
            break;
        }
// _opn_entry_exit:

        reg.D0_L = spReg.D0_L;
        reg.D1_L = spReg.D1_L;
        reg.D2_L = spReg.D2_L;
        reg.D3_L = spReg.D3_L;
        reg.D4_L = spReg.D4_L;
        reg.D5_L = spReg.D5_L;
        reg.D6_L = spReg.D6_L;
        reg.D7_L = spReg.D7_L;
        reg.a0 = spReg.a0;
        reg.a1 = spReg.a1;
        reg.a2 = spReg.a2;
        reg.a3 = spReg.a3;
        reg.a4 = spReg.a4;
        reg.a5 = spReg.a5;
    }

    //

    public static final short[] _intmask = new short[] {
            0x200, 0x200, 0x200, 0x300, 0x400, 0x500, 0x600, 0x700
    };

    public static final byte[] _dev_irq = new byte[] {
            0x30, 0x1F, 0x2F, 0x3F
    };

    /**
     * OPM timer
     */
    public void _opm_entry() {
        reg.sr |= 0x700;
        int spA6 = reg.a6;
        reg.a6 = MnDrv._work_top;
        byte f = (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) & 1);
        mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) | 1));
        if (f != 0) {
            // _opm_entry_short_exit
            reg.a6 = spA6;
            return;
        }

        Reg spReg = new Reg();
        spReg.D0_L = reg.D0_L;
        spReg.D1_L = reg.D1_L;
        spReg.D2_L = reg.D2_L;
        spReg.D3_L = reg.D3_L;
        spReg.D4_L = reg.D4_L;
        spReg.D5_L = reg.D5_L;
        spReg.D6_L = reg.D6_L;
        spReg.D7_L = reg.D7_L;
        spReg.a0 = reg.a0;
        spReg.a1 = reg.a1;
        spReg.a2 = reg.a2;
        spReg.a3 = reg.a3;
        spReg.a4 = reg.a4;
        spReg.a5 = reg.a5;

        reg.a0 = 0xe90003;
        reg.D0_L = 3;
        do {
            reg.setD3_B((byte) timerOPM.readStatus());
        } while ((byte) reg.getD3_B() < 0);
        reg.setD3_W(reg.getD3_W() & reg.getD0_W());

        reg.D0_L = 7;
        reg.D0_L &= 0; // Reg.Arg[0]; // 4 * 15(sp) -> 割り込みレベルの調整(下げている)を行っている(再現不要箇所)
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD4_W((short) reg.getSR_W());
        reg.setD4_W(reg.getD4_W() & 0xf8ff);
        reg.setD4_W(reg.getD4_W() | _intmask[reg.getD0_W() / 2]);

_opm_recall:
        while (true) {
            reg.D1_L = 0x14;
            reg.setD0_B(_dev_irq[reg.getD3_W()]);
            mndrv._OPM_WRITE();
            reg.setSR_W(reg.getD4_W());

            int sp = reg.getD4_W();
            reg.setD3_W(reg.getD3_W() + reg.getD3_W());
            switch (reg.getD3_W()) {
            case 0:
                break _opm_recall; // _opm_entry_exit;
            case 2:
                _timer_a_job();
                break;
            case 4:
                _timer_b_job();
                break;
            case 6:
                _timer_ab_job();
                break;
            }
            reg.setD4_W(sp);

            reg.setD0_B(mm.readByte(reg.a6 + Dw.TEMPO3));
            if (mm.readByte(reg.a6 + Dw.TEMPO2) - reg.getD0_B() != 0) {
                mm.write(reg.a6 + Dw.TEMPO2, (byte) reg.getD0_B());
                reg.D1_L = 0x12;
                mndrv._OPM_WRITE();
            }

            reg.sr |= 0x700;
            reg.a0 = 0xe90003;
            reg.D0_L = 3;
            do {
                //reg.getD3_B() = mm.ReadByte(Reg.a0);
                reg.setD3_B((byte) timerOPM.readStatus());
            } while ((byte) reg.getD3_B() < 0);
            reg.setD3_W(reg.getD3_W() & reg.getD0_W());
            if (reg.getD3_W() != 0) continue; // break _opm_recall;

            mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0xfe));
            reg.sr = reg.getD4_W();

            reg.setD7_W(mm.readShort(reg.a6 + Dw.INTEXECNUM));
            if (reg.getD7_W() != 0) {
                reg.a0 = reg.a6 + Dw.INTEXECBUF;
                mm.write(reg.a6 + Dw.INTEXECNUM, (short) 0);
                reg.setD7_W(reg.getD7_W() - 1);
                do {
                    reg.a1 = mm.readInt(reg.a0);
                    reg.a0 += 4;
                    Reg spReg2 = new Reg();
                    spReg2.D7_L = reg.D7_L;
                    spReg2.a0 = reg.a0;
                    spReg2.a6 = reg.a6;
                    ab.hlINTEXECBUF.get(reg.a1).run();
                    reg.D7_L = spReg2.D7_L;
                    reg.a0 = spReg2.a0;
                    reg.a6 = spReg2.a6;
                } while (reg.decAfterD7_W() != 0);
            }
        }
// _opm_entry_exit:

        reg.D0_L = spReg.D0_L;
        reg.D1_L = spReg.D1_L;
        reg.D2_L = spReg.D2_L;
        reg.D3_L = spReg.D3_L;
        reg.D4_L = spReg.D4_L;
        reg.D5_L = spReg.D5_L;
        reg.D6_L = spReg.D6_L;
        reg.D7_L = spReg.D7_L;
        reg.a0 = spReg.a0;
        reg.a1 = spReg.a1;
        reg.a2 = spReg.a2;
        reg.a3 = spReg.a3;
        reg.a4 = spReg.a4;
        reg.a5 = spReg.a5;
    }

    //

    /**
     * TIMER-A/B
     */
    private void _timer_ab_job() {
        _timer_b_job();
        _timer_a_job();
    }

    /**
     * TIMER-A JOB
     */
    private void _timer_a_job() {
        _fade_entry();

        reg.setD1_B(mm.readByte(reg.a6 + Dw.DRV_STATUS));
        if ((byte) reg.getD1_B() < 0) {
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD1_B());
            if ((byte) reg.getD1_B() >= 0) {
                if ((mm.readByte(reg.a6 + Dw.DRV_FLAG3) & 0x40) != 0) {
                    _ch_ana_tma_env();
                }
                if (mm.readByte(reg.a6 + Dw.DRV_FLAG3) < 0) {
                    _ch_ana_tma_lfo();
                }
            }
        }
    }

    /**
    //	TIMER-B JOB
     */
    private void _timer_b_job() {
        reg.setD1_B(mm.readByte(reg.a6 + Dw.DRV_STATUS));
        if ((byte) reg.getD1_B() < 0) { // break _timer_b_job_pause;
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD1_B());
            if ((byte) reg.getD1_B() >= 0) { // break _timer_b_job_pause;
                do {
                    _ch_ana();
                } while ((mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x08) != 0);
            }
        }
// _timer_b_job_pause:
        reg.D3_L = 0;
        reg.setD0_B(mm.readByte(reg.a6 + Dw.TEMPO));

        if (mm.readByte(reg.a6 + Dw.DRV_FLAG) < 0) {
            _key_ctrl_disable();
            return;
        }

        reg.D2_L = 0xf;
        reg.setD2_B(reg.getD2_B() & mm.readByte(0x80e));
        if (reg.getD2_B() == 0) {
            _key_ctrl_disable();
            return;
        }
        reg.setD2_W(reg.getD2_W() + (int) (short) reg.getD2_W());
        switch (reg.getD2_W()) {
        case 0:
        case 2: // SHIFT
        case 4: // CTRL
        case 6: // SHIFT + CTRL
        case 14: // SHIFT + CTRL + OPT.1
        case 22: // SHIFT + CTRL + OPT.2
        case 24:
        case 26:
        case 28:
        case 30:
            _key_nop();
            break;
        case 8: // OPT.1
            _key_OPT1();
            break;
        case 10: // SHIFT + OPT.1
            _key_SOPT1();
            break;
        case 12: // CTRL + OPT.1
            _key_COPT1();
            break;
        case 16: // OPT.2
            _key_OPT2();
            break;
        case 18: // SHIFT + OPT.2
            _key_SOPT2();
            break;
        case 20: // CTRL + OPT.2
            _key_COPT2();
            break;
        }
        mm.write(reg.a6 + Dw.TEMPO3, (byte) reg.getD0_B());

        reg.setD1_B(mm.readByte(reg.a6 + Dw.DRV_STATUS));
        if ((byte) reg.getD1_B() >= 0) return;
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD1_B());
        if ((byte) reg.getD1_B() < 0) return;

        if (reg.getD3_W() != 0) {
            _ch_ana();
        }
        reg.setD0_B(mm.readByte(reg.a6 + Dw.RHY_DAT));
        if (reg.getD0_B() != 0) {
            mm.write(reg.a6 + Dw.RHY_DAT, 0);
            reg.D7_L = 0;
            reg.D1_L = 0x10;
            mndrv._OPN_WRITE();
        }
        reg.setD0_B(mm.readByte(reg.a6 + Dw.RHY_DAT2));
        if (reg.getD0_B() != 0) {
            mm.write(reg.a6 + Dw.RHY_DAT2, 0);
            reg.D7_L = 6;
            reg.D1_L = 0x10;
            mndrv._OPN_WRITE();
        }
// _timer_b_job_exit:
    }

    /** */
    private void _key_ctrl_disable() {
        mm.write(reg.a6 + Dw.TEMPO3, (byte) reg.getD0_B());
        mm.write(reg.a6 + Dw.SP_KEY, 0);
        if (mm.readByte(reg.a6 + Dw.MUTE) == 0) {
            if ((mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x40) == 0) {
                _ff_mute_return();
            }
        }
        reg.setD0_B(mm.readByte(reg.a6 + Dw.RHY_DAT));
        if (reg.getD0_B() != 0) {
            mm.write(reg.a6 + Dw.RHY_DAT, 0);
            reg.D7_L = 0;
            reg.D1_L = 0x10;
            mndrv._OPN_WRITE();
        }
        reg.setD0_B(mm.readByte(reg.a6 + Dw.RHY_DAT2));
        if (reg.getD0_B() != 0) {
            mm.write(reg.a6 + Dw.RHY_DAT2, 0);
            reg.D7_L = 6;
            reg.D1_L = 0x10;
            mndrv._OPN_WRITE();
        }
    }

    /** */
    private void _key_nop() {
        mm.write(reg.a6 + Dw.SP_KEY, 0);
        if ((mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x40) == 0) {
            if (mm.readByte(reg.a6 + Dw.MUTE) == 0) {
                _ff_mute_return();
            }
        }
    }

    private void _key_OPT1() {
        reg.setD2_B(mm.readByte(0x80b));
        int f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) {
            _key_OPT1_XF4();
            return;
        }
        f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f != 0) {
            _key_OPT1_XF5();
            return;
        }
        if (mm.readByte(0x80a) < 0) {
            _key_OPT1_XF3();
            return;
        }
        _key_nop();
    }

    private void _key_OPT2() {
        if ((mm.readByte(0x80b) & 0x2) != 0) {
            _key_OPT2_XF5();
            return;
        }
        if ((mm.readByte(0x80b) & 0x2) < 0) {
            _key_OPT2_XF3();
            return;
        }
        _key_nop();
    }

    private void _key_OPT1_XF3() {
        if (mm.readByte(reg.a6 + Dw.SP_KEY) == 0) {
            int f = mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x10;
            mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) | 0x10));
            if (f == 0) {
                mm.write(reg.a6 + Dw.FADEFLAG, 1);
                mm.write(reg.a6 + Dw.FADESPEED, 7);
                mm.write(reg.a6 + Dw.FADESPEED_WORK, 7);
                mm.write(reg.a6 + Dw.FADECOUNT, 3);
                mm.write(reg.a6 + Dw.SP_KEY, 0xff);
                if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 1) == 0) {
                    Reg spReg = new Reg();
                    spReg.D0_L = reg.D0_L;
                    spReg.D1_L = reg.D1_L;
                    spReg.D7_L = reg.D7_L;
                    reg.D7_L = 3;
                    reg.D1_L = 0x10;
                    reg.D0_L = 0x1c;
                    mndrv._OPN_WRITE();
                    reg.D0_L = spReg.D0_L;
                    reg.D1_L = spReg.D1_L;
                    reg.D7_L = spReg.D7_L;
                }
            }
        }
    }

    private void _key_OPT2_XF3() {
        if (mm.readByte(reg.a6 + Dw.SP_KEY) == 0) {
            mm.write(reg.a6 + Dw.FADEFLAG, 0xff);
            mm.write(reg.a6 + Dw.FADESPEED, 5);
            mm.write(reg.a6 + Dw.FADESPEED_WORK, 5);
            mm.write(reg.a6 + Dw.FADECOUNT, 3);
            mm.write(reg.a6 + Dw.SP_KEY, 0xff);
        }
    }

    private void _key_OPT1_XF4() {
        if (mm.readByte(reg.a6 + Dw.SP_KEY) == 0) {
            mm.write(reg.a6 + Dw.SP_KEY, 0xff);
            _get_track_mask();
            mndrv._t_play_music();
            _set_track_mask();
            reg.setD0_B(mm.readByte(reg.a6 + Dw.TEMPO));
            reg.D3_L = 0;
        }
    }

    private void _key_OPT1_XF5() {
        if (mm.readByte(reg.a6 + Dw.SP_KEY) == 0) {
            mm.write(reg.a6 + Dw.SP_KEY, 0xff);
            mndrv._t_play_music();
            reg.setD0_B(mm.readByte(reg.a6 + Dw.TEMPO));
            reg.D3_L = 0;
        }
    }

    private void _key_OPT2_XF5() {
        if (mm.readByte(reg.a6 + Dw.SP_KEY) == 0) {
            mndrv._t_pause();
            reg.D3_L = 0;
            mm.write(reg.a6 + Dw.SP_KEY, 0xff);
        }
    }

    private void _key_COPT1() {
        reg.D0_L = 0;
    }

    private void _key_SOPT1() {
        reg.setD0_B(reg.getD0_B() >> 1);
    }

    private void _key_COPT2() {
        reg.setD3_B(0xff);
        _key_SOPT2();
    }

    private void _key_SOPT2() {
        reg.D0_L = 0xf0;
        _ff_mute();
    }

    private void _get_track_mask() {
        reg.a5 = reg.a6 + Dw.TRACKWORKADR;
        reg.a0 = reg.a6 + Dw.MASKDATA;
        reg.D1_L = 0;
        reg.setD1_W(mm.readShort(reg.a6 + Dw.USE_TRACK));

        do {
            reg.setD0_B(mm.readByte(reg.a5 + W.flag2));
            reg.setD0_B(reg.getD0_B() & 0x80);
            mm.write(reg.a0++, (byte) reg.getD0_B());
            reg.a5 = reg.a5 + W._track_work_size;
            reg.setD1_W(reg.getD1_W() - 1);
        } while (reg.getD1_W() != 0);
    }

    private void _set_track_mask() {
        reg.a5 = reg.a6 + Dw.TRACKWORKADR;
        reg.a0 = reg.a6 + Dw.MASKDATA;

        reg.D1_L = 0;
        reg.setD1_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        do {
            reg.setD0_B(mm.readByte(reg.a0++));
            mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | reg.getD0_B()));
            reg.a5 = reg.a5 + W._track_work_size;
            reg.setD1_W(reg.getD1_W() - 1);
        } while (reg.getD1_W() != 0);
    }

    private void _ff_mute() {
        if ((mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x10) != 0) return;

        mm.write(reg.a6 + Dw.MUTE, 0);

        Reg spReg = new Reg();
        spReg.D0_L = reg.D0_L;
        spReg.D3_L = reg.D3_L;

        mm.write(reg.a6 + Dw.MASTER_VOL_FM, 14);
        mm.write(reg.a6 + Dw.MASTER_VOL_PCM, 40);
        mm.write(reg.a6 + Dw.MASTER_VOL_RHY, 25);
        mm.write(reg.a6 + Dw.MASTER_VOL_PSG, 5);
        _ch_fade();

        reg.D0_L = spReg.D0_L;
        reg.D3_L = spReg.D3_L;
    }

    private void _ff_mute_return() {
        mm.write(reg.a6 + Dw.MUTE, 0xff);
        Reg spReg = new Reg();
        spReg.D0_L = reg.D0_L;
        spReg.D3_L = reg.D3_L;
        reg.D0_L = 0;
        mm.write(reg.a6 + Dw.MASTER_VOL_FM, (byte) reg.getD0_B());
        mm.write(reg.a6 + Dw.MASTER_VOL_PCM, (byte) reg.getD0_B());
        mm.write(reg.a6 + Dw.MASTER_VOL_RHY, (byte) reg.getD0_B());
        mm.write(reg.a6 + Dw.MASTER_VOL_PSG, (byte) reg.getD0_B());
        _ch_fade();
        reg.D0_L = spReg.D0_L;
        reg.D3_L = spReg.D3_L;
    }

    /**
     * FADEIN/OUT
     */
    private void _fade_entry() {
        reg.setD1_B(mm.readByte(reg.a6 + Dw.FADEFLAG));
        if (reg.getD1_B() == 0) return;

        if ((mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x40) != 0) return;
        mm.write(reg.a6 + Dw.FADESPEED_WORK, (byte) (mm.readByte(reg.a6 + Dw.FADESPEED_WORK) - 1));
        if (mm.readByte(reg.a6 + Dw.FADESPEED_WORK) != 0) return;

        mm.write(reg.a6 + Dw.FADESPEED_WORK, mm.readByte(reg.a6 + Dw.FADESPEED));
        //	tst.b	d1
        //	bpl	_fade_out
        if ((byte) reg.getD1_B() >= 0) {
            _fade_out();
            return;
        }

        _ch_fadein_calc();
        _fade_exec();
    }

    private void _fade_out() {
        _ch_fadeout_calc();
        _fade_exec();
    }

    private void _fade_exec() {
        int sp = reg.D7_L;
        _ch_fade();
        reg.D7_L = sp;

        if ((mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x20) == 0) return;

        mm.write(reg.a6 + Dw.FADEFLAG, 0);
        mndrv._d_stop_music();
        reg.D0_L = 5;
        mndrv.SUBEVENT();
        mm.write(reg.a6 + Dw.DRV_STATUS, 0x20);
    }

    /**
     * Mon Aug 14 17:34 JST 2000 (saori)
     * 各 MASTER_VOLはここでは 127以上にはならない為
     * エラーチェックを省いた。
     */
    private void _ch_fadeout_calc() {
        mm.write(reg.a6 + Dw.MASTER_VOL_FM, (byte) (mm.readByte(reg.a6 + Dw.MASTER_VOL_FM) + 1));
        mm.write(reg.a6 + Dw.MASTER_VOL_PCM, (byte) (mm.readByte(reg.a6 + Dw.MASTER_VOL_PCM) + 1));
        mm.write(reg.a6 + Dw.MASTER_VOL_RHY, (byte) (mm.readByte(reg.a6 + Dw.MASTER_VOL_RHY) + 1));

        mm.write(reg.a6 + Dw.FADECOUNT, (byte) (mm.readByte(reg.a6 + Dw.FADECOUNT) - 1));
        if (mm.readByte(reg.a6 + Dw.FADECOUNT) != 0) return;
        mm.write(reg.a6 + Dw.FADECOUNT, 3);

        mm.write(reg.a6 + Dw.MASTER_VOL_PCM, (byte) (mm.readByte(reg.a6 + Dw.MASTER_VOL_PCM) + 2));

        reg.setD0_B(mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        reg.setD0_B(reg.getD0_B() + 1);
        if (reg.getD0_B() >= 25) {
            mm.write(reg.a6 + Dw.DRV_STATUS, 0x20);
        }
        mm.write(reg.a6 + Dw.MASTER_VOL_PSG, (byte) reg.getD0_B());
    }

    /** */
    private void _ch_fadein_calc() {
        reg.setD0_B(mm.readByte(reg.a6 + Dw.MASTER_VOL_FM));
        reg.setD0_B(reg.getD0_B() - 1);
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a6 + Dw.MASTER_VOL_FM, (byte) reg.getD0_B());

        reg.setD0_B(mm.readByte(reg.a6 + Dw.MASTER_VOL_PCM));
        reg.setD0_B(reg.getD0_B() - 2);
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a6 + Dw.MASTER_VOL_PCM, (byte) reg.getD0_B());

        reg.setD0_B(mm.readByte(reg.a6 + Dw.MASTER_VOL_RHY));
        reg.setD0_B(reg.getD0_B() - 1);
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a6 + Dw.MASTER_VOL_RHY, (byte) reg.getD0_B());

        mm.write(reg.a6 + Dw.FADECOUNT, (byte) (mm.readByte(reg.a6 + Dw.FADECOUNT) - 1));
        if (mm.readByte(reg.a6 + Dw.FADECOUNT) != 0) return;

        mm.write(reg.a6 + Dw.FADECOUNT, 3);
        reg.setD0_B(mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
        reg.setD0_B(reg.getD0_B() - 1);
        if ((byte) reg.getD0_B() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a6 + Dw.MASTER_VOL_PSG, (byte) reg.getD0_B());
    }

    /** */
    private void _ch_fade() {
        reg.setD7_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a5 = reg.a6 + Dw.TRACKWORKADR;
// _ch_fade_loop:
        do {
            reg.setD0_B(mm.readByte(reg.a5 + W.ch));
            if ((byte) reg.getD0_B() >= 0) { // break _ch_fade_loop_check;

                if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 1) == 0) { // break _ch_fade_next;

                    if (reg.getD0_B() >= 0x40) { // break _ch_fade_loop_rhythm;
// _ch_fade_loop_rhythm:
                        reg.setD0_B(mm.readByte(reg.a6 + Dw.RHY_TV));
                        reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_RHY));
                        if ((byte) reg.getD0_B() < 0) {
                            reg.D0_L = 0;
                        }
                        reg.D1_L = 0x11;
                        mndrv._OPN_WRITE2();
                        reg.a5 = reg.a5 + W._track_work_size;
                        if (reg.decAfterD7_W() != 0) continue; // break _ch_fade_loop;
                        return;
                    }
                    if (reg.getD0_B() >= 0x20) { // break _ch_fade_loop_psg;
// _ch_fade_loop_psg:
                        if (mm.readByte(reg.a5 + W.e_sw) >= 0) {

                            reg.setD0_B(mm.readByte(reg.a5 + W.vol));
                            reg.setD0_B(reg.getD0_B() - mm.readByte(reg.a6 + Dw.MASTER_VOL_PSG));
                            if ((byte) reg.getD0_B() < 0) {
                                reg.D0_L = 0;
                            }
                            devpsg._psg_lfo();
                        }
                        reg.a5 = reg.a5 + W._track_work_size;
                        if (reg.decAfterD7_W() != 0) continue; // break _ch_fade_loop;
                        return;
                    }

                    reg.setD4_B(mm.readByte(reg.a6 + Dw.MUTE));
                    reg.setD4_B(reg.getD4_B() | mm.readByte(reg.a5 + W.flag));
                    if ((reg.D4_L & 0x20) != 0) {
                        if (mm.readByte(reg.a5 + W.revexec) == 0) {
                            reg.setD4_B(mm.readByte(reg.a5 + W.vol));
//                            break L2;
                        } else {
                            reg.D4_L = 0x7f;
                        }
                    } else {
                        reg.D4_L = 0x7f;
                    }
// L2:
                    devopn._FM_F2_softenv();
                }
// _ch_fade_next:
                reg.a5 = reg.a5 + W._track_work_size;
                reg.setD7_W(reg.getD7_W() - 1);
                if (reg.getD7_W() != 0) continue; // break _ch_fade_loop;
                return;
            }
// _ch_fade_loop_check:
            if (reg.getD0_B() < 0xa0) { // break _ch_fade_loop_pcm;

                reg.setD4_B(mm.readByte(reg.a6 + Dw.MUTE));
                reg.setD4_B(reg.getD4_B() | mm.readByte(reg.a5 + W.flag));
                if ((reg.D4_L & 0x20) != 0) {
                    if (mm.readByte(reg.a5 + W.revexec) == 0) {
                        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
//                        break L2b;
                    } else {
                        reg.D4_L = 0x7f;
                    }
                } else {
                    reg.D4_L = 0x7f;
                }
// L2b:
                devopm._OPM_F2_softenv();
                reg.a5 = reg.a5 + W._track_work_size;
                if (reg.decAfterD7_W() != 0) continue; // break _ch_fade_loop;
                return;
            }
// _ch_fade_loop_pcm:
            if ((mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x40) != 0) { // break L9;

                reg.setD4_B(mm.readByte(reg.a6 + Dw.MUTE));
                reg.setD4_B(reg.getD4_B() | mm.readByte(reg.a5 + W.flag));
                if ((reg.D4_L & 0x20) != 0) {
                    if (mm.readByte(reg.a5 + W.revexec) == 0) {
                        reg.setD4_B(mm.readByte(reg.a5 + W.vol));
//                        break L2c;
                    } else {
                        reg.D4_L = 0;
                    }
                } else {
                    reg.D4_L = 0;
                }
// L2c:
                devmpcm._MPCM_F2_softenv();
            }
// L9:
            reg.a5 = reg.a5 + W._track_work_size;
        } while (reg.decAfterD7_W() != 0); // break _ch_fade_loop;
    }

    /** */
    private void _ch_ana() {
        reg.setD7_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a5 = reg.a6 + Dw.TRACKWORKADR;

// _ch_ana_loop:
        do {
            reg.a0 = mm.readInt(reg.a5 + W.mmljob_adrs);
            ab.hlw_mmljob_adrs.get(reg.a5).run();

            if (mm.readByte(reg.a6 + Dw.DRV_FLAG3) >= 0) {
                reg.a0 = mm.readInt(reg.a5 + W.lfojob_adrs);
                ab.hlw_lfojob_adrs.get(reg.a5).run();
            }
            if ((mm.readByte(reg.a6 + Dw.DRV_FLAG3) & 0x40) == 0) {
                if (mm.readByte(reg.a5 + W.revexec) >= 0) {
                    reg.setD0_B(mm.readByte(reg.a5 + W.e_sw));
                    if ((byte) reg.getD0_B() < 0) {
                        reg.a0 = mm.readInt(reg.a5 + W.softenv_adrs);
                        ab.hlw_softenv_adrs.get(reg.a5).run();
                    }
                }
            }
            reg.a5 = reg.a5 + W._track_work_size;// Dw._trackworksize;

            reg.setD7_W(reg.getD7_W() - 1);
        } while (reg.getD7_W() != 0); // break _ch_ana_loop;
    }

    /**
     * TIMER-A lfo
     */
    private void _ch_ana_tma_lfo() {
        reg.setD7_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a5 = reg.a6 + Dw.TRACKWORKADR;

// _ch_ana_tma_lfo_loop:
        do {
            reg.a0 = mm.readInt(reg.a5 + W.lfojob_adrs);
            ab.hlw_lfojob_adrs.get(reg.a5).run();
            reg.a5 = reg.a5 + W._track_work_size;
            reg.setD7_W(reg.getD7_W() - 1);
        } while (reg.getD7_W() != 0); // break _ch_ana_tma_lfo_loop;
    }

    /**
     * TIMER-A software envelope
     */
    private void _ch_ana_tma_env() {
        reg.setD7_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a5 = reg.a6 + Dw.TRACKWORKADR;
// _ch_ana_tma_env_loop:
        do {
            if (mm.readByte(reg.a5 + W.revexec) >= 0) {
                if (mm.readByte(reg.a5 + W.e_sw) < 0) {
                    reg.a0 = mm.readInt(reg.a5 + W.softenv_adrs);
                    ab.hlw_softenv_adrs.get(reg.a5).run();
                }
            }
            reg.a5 = reg.a5 + W._track_work_size;// Dw._trackworksize;
            reg.setD7_W(reg.getD7_W() - 1);
        } while (reg.getD7_W() != 0); // break _ch_ana_tma_env_loop;
    }
}
