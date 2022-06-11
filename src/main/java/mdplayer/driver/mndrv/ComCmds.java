package mdplayer.driver.mndrv;

import mdplayer.driver.mxdrv.XMemory;


//
//	part of common commands
//
public class ComCmds {
    public Reg reg;
    public XMemory mm;
    public MnDrv mndrv;
    public ComLfo comlfo;
    public Ab ab;

    //	タイ
    //		[$81]
    public void _COM_81() {
        if ((mm.readByte(reg.a5 + W.flag3) & 0x40) != 0) return;
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) & 0xbf));
    }

    //	スラー
    //		[$83]
    public void _COM_83() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x40));
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) | 0x40));
    }

    //	同期信号送信
    //		[$86] + [track] b
    public void _COM_86() {
        reg.D0_L = 0;
        reg.a0 = Dw.TRACKWORKADR + reg.a6;

        reg.setD0_B(mm.readByte(reg.a1++));
        while (true) {
            reg.setD0_W(reg.getD0_W() - 1);
            if (reg.getD0_W() == 0) break;

            reg.a0 = W._track_work_size + reg.a0;
        }

        int a = mm.readByte(reg.a0 + W.flag4) & 0x80;
        mm.write(reg.a0 + W.flag4, (byte) (mm.readByte(reg.a0 + W.flag4) & 0x7f));
        if (a == 0) {
            mm.write(reg.a0 + W.flag4, (byte) (mm.readByte(reg.a0 + W.flag4) | 0x40));
        }
    }

    //	同期信号待ち
    //		[$87]
    public void _COM_87() {
        int a = mm.readByte(reg.a5 + W.flag4) & 0x40;
        mm.write(reg.a5 + W.flag4, (byte) (mm.readByte(reg.a5 + W.flag4) & 0xbf));
        if (a != 0) return;

        mm.write(reg.a5 + W.dataptr, reg.a1);

        mm.write(reg.a5 + W.flag4, (byte) (mm.readByte(reg.a5 + W.flag4) | 0x80));
        mm.write(reg.a5 + W.len, (byte) 1);
    }

    //	q 設定
    // [$90] + [DATA]b
    //				$1 ～ $10 まで[16段階]
    public void _COM_90() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.q, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) & 0xbf));
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) | 0x20));

        if (mm.readByte(reg.a6 + Dw.MND_VER) < 8) {
            reg.a0 = Ab.dummyAddress;// _atq_old;
            mm.write(reg.a5 + W.qtjob, reg.a0);//_atq_old = 0 とする
            if (ab.hlw_qtjob.containsKey(reg.a5)) ab.hlw_qtjob.remove(reg.a5);
            ab.hlw_qtjob.put(reg.a5, this::_atq_old);
            return;
        }

        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
//        reg.getD0_W(mm.Readshort(_com_90_table));
//        reg.a0 = _com_90_table;
        reg.a0 = Ab.dummyAddress;
        mm.write(reg.a5 + W.qtjob, reg.a0); // d0(q:0x01-0x10)
        if (ab.hlw_qtjob.containsKey(reg.a5)) ab.hlw_qtjob.remove(reg.a5);
        switch (reg.getD0_W() / 2) {
        case 1:
            ab.hlw_qtjob.put(reg.a5, this::_atq_01);
            break;
        case 2:
            ab.hlw_qtjob.put(reg.a5, this::_atq_02);
            break;
        case 3:
            ab.hlw_qtjob.put(reg.a5, this::_atq_03);
            break;
        case 4:
            ab.hlw_qtjob.put(reg.a5, this::_atq_04);
            break;
        case 5:
            ab.hlw_qtjob.put(reg.a5, this::_atq_05);
            break;
        case 6:
            ab.hlw_qtjob.put(reg.a5, this::_atq_06);
            break;
        case 7:
            ab.hlw_qtjob.put(reg.a5, this::_atq_07);
            break;
        case 8:
            ab.hlw_qtjob.put(reg.a5, this::_atq_08);
            break;
        case 9:
            ab.hlw_qtjob.put(reg.a5, this::_atq_09);
            break;
        case 10:
            ab.hlw_qtjob.put(reg.a5, this::_atq_10);
            break;
        case 11:
            ab.hlw_qtjob.put(reg.a5, this::_atq_11);
            break;
        case 12:
            ab.hlw_qtjob.put(reg.a5, this::_atq_12);
            break;
        case 13:
            ab.hlw_qtjob.put(reg.a5, this::_atq_13);
            break;
        case 14:
            ab.hlw_qtjob.put(reg.a5, this::_atq_14);
            break;
        case 15:
            ab.hlw_qtjob.put(reg.a5, this::_atq_15);
            break;
        case 16:
            ab.hlw_qtjob.put(reg.a5, this::_atq_16);
            break;
        }
    }

    public void _atq_01() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_02() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 3);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_03() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_04() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 2);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_05() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_06() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 3);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_07() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() << 3);
        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_08() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 1);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_09() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() << 3);
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_10() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 3);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_11() {
        reg.setD1_W(reg.getD0_W());
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(reg.getD1_W());
        reg.setD1_W(reg.getD1_W() << 4);
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_12() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() + (int) (short) reg.getD1_W());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD1_W());
        reg.setD1_W(reg.getD1_W() >> 2);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_13() {
        reg.setD1_W(reg.getD0_W());
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(reg.getD1_W());
        reg.setD1_W(reg.getD1_W() << 4);
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_14() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() << 3);
        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 3);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_15() {
        reg.setD1_W(reg.getD0_W());
        reg.setD1_W(reg.getD1_W() << 4);
        reg.setD1_W(reg.getD1_W() - (int) (short) reg.getD0_W());
        reg.setD1_W(reg.getD1_W() >> 4);
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD1_B());
    }

    public void _atq_16() {
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
    }

    public void _atq_old() {
        reg.setD3_B(reg.getD0_B());
        reg.D1_L = 0x10;
        reg.D2_L = 0;
        reg.setD1_B(reg.getD1_B() - mm.readByte(reg.a5 + W.q));
        if (reg.getD1_B() != 0) {
            reg.setD3_B(reg.getD3_B() >> 4);
            do {
                reg.setD2_B(reg.getD2_B() + (int) (byte) reg.getD3_B());
            } while (reg.decAfterD1_W() != 0);
        }
        reg.setD0_B(reg.getD0_B() - (int) (byte) reg.getD2_B());
        mm.write(reg.a5 + W.at_q, (byte) reg.getD0_B());
        mm.write(reg.a5 + W.at_q_work, (byte) reg.getD0_B());
    }

    //	@q 設定
    //			[$91] + [DATA]b
    public void _COM_91() {
        mm.write(reg.a5 + W.at_q, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x40));
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) & 0xdf));
    }

    //	ネガティブ @q 設定
    //			[$93] + [DATA]b
    public void _COM_93() {
        mm.write(reg.a5 + W.at_q, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x40));
        mm.write(reg.a5 + W.flag3, (byte) (mm.readByte(reg.a5 + W.flag3) | 0x20));
    }

    //	キーオフモード
    //			[$94] + [switch]b
    public void _COM_94() {
        mm.write(reg.a5 + W.kom, 0);
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() != 0) mm.write(reg.a5 + W.kom, (byte) 0xff);
    }

    //	擬似リバーブ
    //		switch = $80 = ON
    //			 $81 = OFF
    //			 $82 = volume を直接指定にする
    //			 $83 = volume を相対指定にする
    //			 $84 = リバーブ動作は相対音量モードに依存する
    //			 $85 = リバーブ動作は常に @v 単位
    //
    //			 $00 = + [volume]b
    //			 $01 = + [volume]b + [pan]b
    //			 $02 = + [volume]b + [tone]b
    //			 $03 = + [volume]b + [panpot]b + [tone]b
    //			 $04 = + [volume]b ( 微調整 )
    //	work
    //		bit4 1:常に @v
    //		bit3 1:@v直接
    //		bit2 1:微調整
    //		bit1 1:音色変更
    //		bit0 1:定位変更
    //
    public void _COM_98() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() >= 0) {
            mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) & 0x10));
            reg.setD0_B(reg.getD0_B() + 1);
            reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD0_B());
            switch (reg.getD0_B()) {
            case 2:
                _COM_98_0();
                break;
            case 4:
                _COM_98_1();
                break;
            case 6:
                _COM_98_2();
                break;
            case 8:
                _COM_98_3();
                break;
            case 10:
                _COM_98_4();
                break;
            }
            return;
        }

        reg.setD0_B(reg.getD0_B() & 0xf);
        reg.setD0_B(reg.getD0_B() + 1);
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD0_B());
        switch (reg.getD0_B()) {
        case 2:
            _COM_98_80();
            break;
        case 4:
            _COM_98_81();
            break;
        case 6:
            _COM_98_82();
            break;
        case 8:
            _COM_98_83();
            break;
        case 10:
            _COM_98_84();
            break;
        case 12:
            _COM_98_85();
            break;
        }
    }

    public void _COM_98_80() {
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x80));
    }

    public void _COM_98_81() {
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) & 0x7f));
    }

    public void _COM_98_82() {
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x08));
    }

    public void _COM_98_83() {
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) & 0xf7));
    }

    public void _COM_98_84() {
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) & 0xef));
    }

    public void _COM_98_85() {
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x10));
    }

    // volume
    public void _COM_98_0() {
        mm.write(reg.a5 + W.reverb_vol, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x80));
    }

    // volume + pan
    public void _COM_98_1() {
        mm.write(reg.a5 + W.reverb_vol, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb_pan, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x81));
    }

    // volume + tone
    public void _COM_98_2() {
        mm.write(reg.a5 + W.reverb_vol, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb_tone, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x82));
    }

    // volume + panpot + tone
    public void _COM_98_3() {
        mm.write(reg.a5 + W.reverb_vol, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb_pan, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb_tone, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x83));
    }

    // volume
    public void _COM_98_4() {
        mm.write(reg.a5 + W.reverb_vol, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.reverb, (byte) (mm.readByte(reg.a5 + W.reverb) | 0x84));
    }

    // 擬似エコー(廃止コマンド？)
    public void _COM_99() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() >= 0) {
            reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
            return;
        }

        if (reg.getD0_B() == 0x80) {
            return;
        }
    }

    //	擬似動作 step time
    public void _COM_9A() {
        mm.write(reg.a5 + W.reverb_time, mm.readByte(reg.a1++));
    }

    //	音量テーブル切り替え
    public void _COM_A3() {
        reg.setD1_B(mm.readByte(reg.a1++));
        reg.setD5_B(mm.readByte(reg.a1++));

        reg.D0_L = mm.readInt(reg.a6 + Dw.VOL_PTR);
        if (reg.D0_L == 0) return;

        reg.a2 = reg.D0_L;
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) | (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D2_L = reg.D0_L;
        if (reg.D2_L == 0) return;

        reg.setD4_W(mm.readShort(reg.a2));
        reg.a2 += 2;

        //_com_a3_ana_loop:
        while (true) {
            if (mm.readByte(reg.a2 + 2) == reg.getD1_B()) {
                if (mm.readByte(reg.a2 + 3) == reg.getD5_B()) {
                    _com_a3_set();
                    return;
                }
            }
            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() == 0) {
                return;
            }
            reg.setD0_W(mm.readShort(reg.a2)); //Reg.a2 += 1;
            reg.a2 = (reg.a2 + (int) (short) reg.getD0_W()) & 0x00ffffff;
        }
    }

    public void _com_a3_set() {
        reg.a2 += 4;
        reg.D0_L = 0x7f;
        reg.setD0_B(reg.getD0_B() & mm.readByte(reg.a2));
        mm.write(reg.a5 + W.volcount, (byte) reg.getD0_B());
        reg.a2 += 2;// (Reg.a2 & 0xffff0000) + (int)((int)Reg.a2 + 2);

        reg.a0 = reg.a5 + W.voltable;
        do {
            mm.write(reg.a0, mm.readByte(reg.a2));
            reg.a0++;
            reg.a2++;
        } while (reg.decAfterD0_W() != 0);
    }

    //	相対音量モード
    public void _COM_A8() {
        mm.write(reg.a5 + W.volmode, mm.readByte(reg.a1++));
    }

    //	ドライバ動作モード変更
    public void _COM_B0() {
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _COM_B0_0();
            break;
        case 4:
            _COM_B0_1();
            break;
        case 6:
            _COM_B0_2();
            break;
        case 8:
            _COM_B0_3();
            break;
        }
        return;
    }

    public void _COM_B0_0() {
        mm.write(reg.a6 + Dw.DRV_FLAG2, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG2) & 0x7f));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) return;
        mm.write(reg.a6 + Dw.DRV_FLAG2, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG2) | 0x80));
    }

    public void _COM_B0_1() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            mm.write(reg.a6 + Dw.DRV_FLAG3, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG3) & 0x7f));
            return;
        }
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() == 0) {
            mm.write(reg.a6 + Dw.DRV_FLAG3, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG3) | 0x80));
            _COM_B0_start_timer_a();
            return;
        }
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() == 0) {
            mm.write(reg.a6 + Dw.DRV_FLAG3, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG3) & 0xbf));
            return;
        }
        mm.write(reg.a6 + Dw.DRV_FLAG3, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG3) | 0x40));
        _COM_B0_start_timer_a();
    }

    public void _COM_B0_start_timer_a() {
        int sd = reg.D7_L;
        reg.D7_L = 3;
        reg.D1_L = 0x10;
        reg.D0_L = 0x1c;
        mndrv._OPN_WRITE();
        reg.D7_L = sd;
    }

    public void _COM_B0_2() {
        mm.write(reg.a6 + Dw.VOLMODE, 0);
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() != 0) mm.write(reg.a6 + Dw.VOLMODE, (byte) 0xff);
    }

    // PSG LFO MODE
    public void _COM_B0_3() {
        mm.write(reg.a6 + Dw.DRV_FLAG2, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG2) & 0xfc));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) return;
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() == 0) {
            mm.write(reg.a6 + Dw.DRV_FLAG2, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG2) + 1));
            return;
        }
        reg.setD0_B(reg.getD0_B() - 1);
        if (reg.getD0_B() == 0) {
            mm.write(reg.a6 + Dw.DRV_FLAG2, (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG2) + 2));
        }
    }


    //	トラックジャンプ
    public void _COM_BE() {
        mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) ^ 0x08));
    }

    //	フェードアウト
    public void _COM_BF_exit() {
        reg.a1++;
    }

    public void _COM_BF() {
        byte b = (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) & 0x10);
        mm.write(reg.a6 + Dw.DRV_STATUS, (byte) (mm.readByte(reg.a6 + Dw.DRV_STATUS) | 0x10));
        if (b != 0) {
            _COM_BF_exit();
            return;
        }
        mm.write(reg.a6 + Dw.FADESPEED, 1);
        mm.write(reg.a6 + Dw.FADESPEED_WORK, 3);

        b = (byte) (mm.readByte(reg.a6 + Dw.DRV_FLAG) & 0x01);
        if (b != 0) {
            _COM_BF_no_opn();
            return;
        }
        int sd = reg.D7_L;
        reg.D7_L = 3;
        reg.D1_L = 0x10;
        reg.D0_L = 0x1c;
        mndrv._OPN_WRITE();
        reg.D7_L = sd;
        _COM_BF_no_opn();
    }

    public void _COM_BF_no_opn() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_BF_normal();
            return;
        }
        mm.write(reg.a6 + Dw.FADESPEED, (byte) reg.getD0_B());
        mm.write(reg.a6 + Dw.FADESPEED_WORK, (byte) reg.getD0_B());
    }

    public void _COM_BF_normal() {
        mm.write(reg.a6 + Dw.FADESPEED, 7);
        mm.write(reg.a6 + Dw.FADESPEED_WORK, 7);
    }

    //	ソフトウェアエンベロープ
    //		[$C0] + [SV]b + [AR]b + [DR]b + [SL]b + [SR]b + [RR]b
    public void _COM_C0() {
        mm.write(reg.a5 + W.e_sv, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_ar, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_dr, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_sl, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_sr, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_rr, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_sub, (byte) 0);
        mm.write(reg.a5 + W.e_p, (byte) 4);
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));
    }

    //	ソフトウェアエンベロープ 2
    //		[$C1] + [AL]b + [DD]b + [SR]b + [RR]b
    public void _COM_C1() {
        mm.write(reg.a5 + W.e_al, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_dd, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_sr, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_rr, mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x81));
    }

    //	・ソフトウェアエンベロープスイッチ
    //		[$C3] + [switch]
    public void _COM_C3() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) & 0x7f));
            return;
        }
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));
    }

    //	・エンベロープ切り替え
    //		[$C4] + [num]
    public void _COM_C4() {
        reg.setD5_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.envnum, (byte) reg.getD5_B());
        reg.D0_L = mm.readInt(reg.a6 + Dw.ENV_PTR);
        if (reg.D0_L == 0) return;

        reg.a2 = reg.D0_L;
        reg.a2 += 6;
        reg.setD4_W(mm.readShort(reg.a6 + Dw.ENVNUM));
        if (reg.getD4_W() == 0) return;

        reg.setD1_B(mm.readByte(reg.a5 + W.envbank));
        while (true) {
            if (mm.readByte(reg.a2 + 2) == reg.getD1_B()) {
                if (mm.readByte(reg.a2 + 3) == reg.getD5_B()) {
                    _COM_C4_set();
                    return;
                }
            }
            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() == 0) return;

            reg.setD0_W(mm.readShort(reg.a2));
            reg.a2 = (reg.a2 + (int) (short) reg.getD0_W()) & 0x00ffffff;
        }
    }

    public void _COM_C4_set() {
        reg.a2 += 4;
        mm.write(reg.a5 + W.e_sv, mm.readByte(reg.a2 + 3));
        mm.write(reg.a5 + W.e_ar, mm.readByte(reg.a2 + 0));
        mm.write(reg.a5 + W.e_dr, mm.readByte(reg.a2 + 4));
        mm.write(reg.a5 + W.e_sl, mm.readByte(reg.a2 + 6));
        mm.write(reg.a5 + W.e_sr, mm.readByte(reg.a2 + 8));
        mm.write(reg.a5 + W.e_rr, mm.readByte(reg.a2 + 12));
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));

        byte b = (byte) (mm.readByte(reg.a5 + W.flag) & 0x20);
        if (b != 0) return;

        mm.write(reg.a5 + W.e_sub, 0);
        mm.write(reg.a5 + W.e_p, 4);
    }

    //	・バンク&エンベロープ切り替え
    //		[$C5] + [bank] + [num]
    public void _COM_C5() {
        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.envbank, (byte) reg.getD1_B());
        reg.setD5_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.envbank, (byte) reg.getD5_B());
        reg.D0_L = mm.readInt(reg.a6 + Dw.ENV_PTR);
        if (reg.D0_L == 0) return;
        reg.a2 = reg.D0_L;
        reg.a2 += 6;
        reg.setD4_W(mm.readShort(reg.a6 + Dw.ENVNUM));
        if (reg.getD4_W() == 0) return;
        while (true) {
            if (mm.readByte(reg.a2 + 2) == reg.getD1_B()) {
                if (mm.readByte(reg.a2 + 3) == reg.getD5_B()) {
                    _COM_C5_set();
                    return;
                }
            }
            reg.setD4_W(reg.getD4_W() - 1);
            if (reg.getD4_W() == 0) return;
            reg.setD0_W(mm.readShort(reg.a2));
            reg.a2 = (reg.a2 + (int) (short) reg.getD0_W()) & 0x00ffffff;
        }
    }

    public void _COM_C5_set() {
        reg.a2 += 4;
        mm.write(reg.a5 + W.e_sv, mm.readByte(reg.a2 + 3));
        mm.write(reg.a5 + W.e_ar, mm.readByte(reg.a2 + 0));
        mm.write(reg.a5 + W.e_dr, mm.readByte(reg.a2 + 4));
        mm.write(reg.a5 + W.e_sl, mm.readByte(reg.a2 + 6));
        mm.write(reg.a5 + W.e_sr, mm.readByte(reg.a2 + 8));
        mm.write(reg.a5 + W.e_rr, mm.readByte(reg.a2 + 12));
        mm.write(reg.a5 + W.e_sub, 0);
        mm.write(reg.a5 + W.e_p, 4);
        mm.write(reg.a5 + W.e_sw, (byte) (mm.readByte(reg.a5 + W.e_sw) | 0x80));
    }

    //	キートランスポーズ
    public void _COM_D0() {
        mm.write(reg.a5 + W.key_trans, mm.readByte(reg.a1++));
    }

    //	相対キートランスポーズ
    public void _COM_D1() {
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a5 + W.key_trans, (byte) (mm.readByte(reg.a5 + W.key_trans) + (byte) reg.getD0_B()));
    }

    //	detune 設定
    public void _COM_D8() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a5 + W.detune, (short) reg.getD0_W());
    }

    //	detune 設定
    public void _COM_D9() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a5 + W.detune, (short) (mm.readShort(reg.a5 + W.detune) + (short) reg.getD0_W()));
    }

    //	pitch LFO
    //
    //	$E2,num,wave,speed,count,delay,henka_w
    public void _COM_E2() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _COM_E2_0();
            break;
        case 4:
            _COM_E2_1();
            break;
        case 6:
            _COM_E2_2();
            break;
        case 8:
            _COM_E2_3();
            break;
        }
    }

    //	pitch LFO ALL
    public void _COM_E2_0() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0xe));
        reg.a0 = reg.a1;
        reg.a4 = W.p_pattern1 + reg.a5;
        reg.a3 = W.wp_pattern1 + reg.a5;
        _COM_E2_common();
        if (reg.D2_L < 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xfd));
        }
        reg.a1 = reg.a0;
        reg.a4 = W.p_pattern2 + reg.a5;
        reg.a3 = W.wp_pattern2 + reg.a5;
        _COM_E2_common();
        if (reg.D2_L < 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xfb));
        }
        reg.a1 = reg.a0;
        reg.a4 = W.p_pattern3 + reg.a5;
        reg.a3 = W.wp_pattern3 + reg.a5;
        _COM_E2_common();
        if (reg.D2_L < 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xf7));
        }
    }

    //	pitch LFO 1
    public void _COM_E2_1() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x02));

        reg.a4 = W.p_pattern1 + reg.a5;
        reg.a3 = W.wp_pattern1 + reg.a5;
        _COM_E2_common();
        if (reg.D2_L < 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xfd));
        }
    }

    //	pitch LFO 2
    public void _COM_E2_2() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x04));

        reg.a4 = W.p_pattern2 + reg.a5;
        reg.a3 = W.wp_pattern2 + reg.a5;
        _COM_E2_common();
        if (reg.D2_L < 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xfb));
        }
    }

    //	pitch LFO 3
    public void _COM_E2_3() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x08));

        reg.a4 = W.p_pattern3 + reg.a5;
        reg.a3 = W.wp_pattern3 + reg.a5;
        _COM_E2_common();
        if (reg.D2_L < 0) {
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xf7));
        }
    }

    //	LFO SET COMMON
    public void _COM_E2_common() {
        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a3 + W_W.use_flag, (byte) 0);
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.pattern, (byte) reg.getD0_B());
        if ((byte) reg.getD0_B() < 0) {
            _COM_E2_wavememory();
            return;
        }
        if (reg.getD0_B() >= 0x10) {
            _COM_E2_compatible();
            return;
        }

        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD1_B());
        mm.write(reg.a4 + W_L.count, mm.readByte(reg.a1++));
        reg.setD0_B(mm.readByte(reg.a1++));

        if (reg.getD0_B() - 0xff != 0) {
            mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD0_B());
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD1_B());
        }
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;

        mm.write(reg.a4 + W_L.henka, (short) reg.getD0_W());
        mm.write(reg.a4 + W_L.henka_work, (short) reg.getD0_W());
        reg.setD0_B(mm.readByte(reg.a4 + W_L.count));
        reg.setD0_B(reg.getD0_B() >> 1);
        mm.write(reg.a4 + W_L.count_work, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_L.flag, (short) (mm.readShort(reg.a4 + W_L.flag) & 0xdfff));
        reg.D2_L = 0;
    }

    public void _COM_E2_compatible() {
        mm.write(reg.a3 + W_W.use_flag, (byte) 1);
        mm.write(reg.a4 + W_L.pattern, (byte) 0xff);

        reg.setD0_B(reg.getD0_B() & 7);
        reg.setD1_B(reg.getD0_B());
        reg.setD1_B(reg.getD1_B() & 3);
        reg.setD2_B(reg.getD1_B());
        reg.setD1_B(reg.getD1_B() + 0x10);
        mm.write(reg.a3 + W_W.type, (byte) reg.getD1_B());

        mm.write(reg.a4 + W_L.lfo_sp, mm.readByte(reg.a1++));

        reg.setD1_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a3 + W_W.start, (short) reg.getD1_W());//周期1

        if (reg.getD2_B() != 0) {
            reg.setD1_W(reg.getD1_W() >> 1);
        }
        mm.write(reg.a3 + W_W.loop_start, (short) reg.getD1_W());//周期2

        reg.setD1_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        reg.D1_L = (short) reg.getD1_W();
        reg.D1_L = (short) reg.D1_L << 8;
        if (reg.getD0_B() >= 4) {
            reg.D1_L = (short) reg.D1_L << 8;
        }
        mm.write(reg.a3 + W_W.loop_end, reg.D1_L);//増減1
        if (reg.getD2_B() != 2) {
            reg.D1_L = 0;
        }
        mm.write(reg.a3 + W_W.loop_count, reg.D1_L);//増減2
        reg.D2_L = 0;
    }

    public void _COM_E2_wavememory() {
        reg.setD0_W(reg.getD0_W() & 0x7f);
        _get_wave_memory_e2();

        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD1_B());
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a3 + W_W.depth, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_L.count, (byte) reg.getD0_B());
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() != 0xff) {
            mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD0_B());
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD1_B());
        }
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.flag, (short) 0);
    }

    public void _get_wave_memory_e2() {
        reg.D1_L = mm.readInt(reg.a6 + Dw.WAVE_PTR);
        if (reg.D1_L == 0) {
            reg.D2_L = 0xffffffff;//-1;
            return;
        }
        reg.a2 = reg.D1_L;
        reg.D5_L = reg.D1_L;

        reg.setD1_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D1_L = (reg.D1_L << 16) + (reg.D1_L >> 16);
        reg.setD1_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        if (reg.D1_L == 0) {
            reg.D2_L = 0xffffffff;//-1;
            return;
        }

        reg.setD1_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        if (reg.getD1_W() == 0) {
            reg.D2_L = 0xffffffff;//-1;
            return;
        }

//        _com_e2_wm10:
        while (true) {
            reg.setD2_W(mm.readShort(reg.a2));
            reg.D2_L = (reg.D2_L << 16) + (reg.D2_L >> 16);
            reg.setD2_W(mm.readShort(reg.a2 + 2));
            if (mm.readShort(reg.a2 + 4) != 0) {
                reg.a2 = (reg.a2 + reg.D2_L) & 0xffffff;
                if (reg.decAfterD1_W() != 0) continue; // break _com_e2_wm10;
                reg.D2_L = 0xffffffff;//-1;
                return;
            } else {
                break;
            }
        }

        reg.a2 += 6;

        reg.setD0_B(mm.readByte(reg.a2++));
        reg.setD1_B(reg.getD0_B());
        reg.setD1_B(reg.getD1_B() & 0xf);
        mm.write(reg.a3 + W_W.type, (byte) reg.getD1_B());
        reg.setD0_B(reg.getD0_B() >> 4);
        mm.write(reg.a3 + W_W.ko_flag, (byte) reg.getD0_B());

        reg.setD0_B(mm.readByte(reg.a2++));

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_W.start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_W.loop_start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_W.loop_end, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        mm.write(reg.a3 + W_W.loop_count, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_W.ko_start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_W.ko_loop_start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W( mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_W.ko_loop_end, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L << 16) + (reg.D0_L >> 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        mm.write(reg.a3 + W_W.ko_loop_count, reg.D0_L);

        mm.write(reg.a3 + W_W.use_flag, (byte) 0xff);
        reg.D2_L = 0;
    }

    //	pitch LFO on /off
    //
    //	$E3,num,switch
    public void _COM_E3() {
        reg.D0_L = 0;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.D1_L = reg.D0_L;
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 0:
            _COM_E3_0();
            break;
        case 2:
            _COM_E3_1();
            break;
        case 4:
            _COM_E3_2();
            break;
        case 6:
            _COM_E3_3();
            break;
        }
    }

    // bit15		0:enable 1:disable
    // bit1 keyoff	0:enable 1:disable
    // bit0 keyon	0:enable 1:disable
    // $80  = at keyon
    // $81  = at keyoff
    // $82  = always
    // $83  = async
    // $84  = stop & init
    //
    public void _COM_E3_0() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0xf1));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) return;
        if ((byte) reg.getD0_B() >= 0) {
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x0e));
            return;
        }

        if (reg.getD0_B() == 0x82) {
            reg.a4 = W.p_pattern1 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0);
            reg.a4 = W.p_pattern2 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0);
            reg.a4 = W.p_pattern3 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x0e));
            return;
        }
        if (reg.getD0_B() == 0x83) {
            reg.a4 = W.p_pattern1 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            reg.a4 = W.p_pattern2 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            reg.a4 = W.p_pattern3 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x0e));
            return;
        }
        if (reg.getD0_B() == 0x84) {
            comlfo._init_lfo2();
            return;
        }

        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            reg.a4 = W.p_pattern1 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            reg.a4 = W.p_pattern2 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            reg.a4 = W.p_pattern3 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x0e));
            return;
        }

        reg.a4 = W.p_pattern1 + reg.a5;
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        reg.a4 = W.p_pattern2 + reg.a5;
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        reg.a4 = W.p_pattern3 + reg.a5;
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x0e));

    }

    public void _COM_E3_1() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_E3_OFF();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _COM_E3_ON();
            return;
        }
        reg.a4 = W.p_pattern1 + reg.a5;
        _COM_E3_common();
    }

    public void _COM_E3_2() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_E3_OFF();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _COM_E3_ON();
            return;
        }
        reg.a4 = W.p_pattern2 + reg.a5;
        _COM_E3_common();
    }

    public void _COM_E3_3() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_E3_OFF();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _COM_E3_ON();
            return;
        }
        reg.a4 = W.p_pattern3 + reg.a5;
        _COM_E3_common();
    }

    public void _COM_E3_common() {
        if (reg.getD0_B() == 0x82) {
            mm.write(reg.a4 + W_L.flag, (short) 0);
            _COM_E3_ON();
            return;
        }
        if (reg.getD0_B() == 0x83) {
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            _COM_E3_ON();
            return;
        }
        if (reg.getD0_B() == 0x84) {
            comlfo._init_lfo2();
            _COM_E3_OFF();
            return;
        }
        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f != 0) {
            mm.write(reg.a4 + W_L.flag, (short) 0x8002);
            _COM_E3_ON();
            return;
        }

        mm.write(reg.a4 + W_L.flag, (short) 0x8001);
        _COM_E3_ON();
    }


    public void _COM_E3_ON() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        reg.D0_L |= 1 << reg.D1_L;
        mm.write(reg.a5 + W.lfo, (byte) reg.getD0_B());
    }

    public void _COM_E3_OFF() {
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        reg.D0_L &= ~(int) (1 << reg.D1_L);
        mm.write(reg.a5 + W.lfo, (byte) reg.getD0_B());
    }

    //	pitch LFO delay
    //
    //	$E4,num,delay
    public void _COM_E4() {
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _COM_E4_0();
            break;
        case 4:
            _COM_E4_1();
            break;
        case 6:
            _COM_E4_2();
            break;
        case 8:
            _COM_E4_3();
            break;
        }
    }

    //	pitch LFO ALL
    public void _COM_E4_0() {
        reg.a0 = reg.a1;
        reg.a4 = W.p_pattern1 + reg.a5;
        _COM_E49_common();
        reg.a1 = reg.a0;
        reg.a4 = W.p_pattern2 + reg.a5;
        _COM_E49_common();
        reg.a1 = reg.a0;
        reg.a4 = W.p_pattern3 + reg.a5;
        _COM_E49_common();
    }

    //	pitch LFO 1
    public void _COM_E4_1() {
        reg.a4 = W.p_pattern1 + reg.a5;
        _COM_E49_common();
    }

    //	pitch LFO 2
    public void _COM_E4_2() {
        reg.a4 = W.p_pattern2 + reg.a5;
        _COM_E49_common();
    }

    //	pitch LFO 3
    public void _COM_E4_3() {
        reg.a4 = W.p_pattern3 + reg.a5;
        _COM_E49_common();
    }

    public void _COM_E49_common() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0xff) {
            _COM_E49_add();
            return;
        }
        mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD0_B());
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.lfo_sp));
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
    }

    public void _COM_E49_add() {
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.keydelay));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.lfo_sp));
        mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
    }

    //	音量 LFO
    //
    //	$E7,num,wave,delay,count,speed,henka_w
    public void _COM_E7() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _COM_E7_0();
            break;
        case 4:
            _COM_E7_1();
            break;
        case 6:
            _COM_E7_2();
            break;
        case 8:
            _COM_E7_3();
            break;
        }
    }

    //	音量 LFO
    public void _COM_E7_0() {
        reg.a0 = reg.a1;
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x70));
        reg.a4 = W.v_pattern1 + reg.a5;
        reg.a3 = W.wv_pattern1 + reg.a5;
        _COM_E7_common();
        reg.a1 = reg.a0;
        reg.a4 = W.v_pattern2 + reg.a5;
        reg.a3 = W.wv_pattern2 + reg.a5;
        _COM_E7_common();
        reg.a1 = reg.a0;
        reg.a4 = W.v_pattern3 + reg.a5;
        reg.a3 = W.wv_pattern3 + reg.a5;
        _COM_E7_common();
    }

    //	音量 LFO 1
    public void _COM_E7_1() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x10));
        reg.a4 = W.v_pattern1 + reg.a5;
        reg.a3 = W.wv_pattern1 + reg.a5;
        _COM_E7_common();
    }

    //	音量 LFO 2
    public void _COM_E7_2() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x20));
        reg.a4 = W.v_pattern2 + reg.a5;
        reg.a3 = W.wv_pattern2 + reg.a5;
        _COM_E7_common();
    }

    //	音量 LFO 3
    public void _COM_E7_3() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x40));
        reg.a4 = W.v_pattern3 + reg.a5;
        reg.a3 = W.wv_pattern3 + reg.a5;
        _COM_E7_common();
    }

    //	LFO SET COMMON
    public void _COM_E7_common() {
        mm.write(reg.a4 + W_L.bendwork, (short) 0);
        mm.write(reg.a3 + W_W.use_flag, (byte) 0);
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.pattern, (byte) reg.getD0_B());
        if ((byte) reg.getD0_B() < 0) {
            _COM_E2_wavememory();
            return;
        }
        if (reg.getD0_B() >= 0x10) {
            _COM_E7_compatible();
            return;
        }

        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a4 + W_L.lfo_sp, (byte) reg.getD1_B());
        mm.write(reg.a4 + W_L.count, mm.readByte(reg.a1++));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() - 0xff != 0) {
            mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD0_B());
            reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD0_B());
            mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD1_B());
        }
        reg.a1++;
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD0_W((short) reg.getD0_B()); //byte to short cast(signed)
        mm.write(reg.a4 + W_L.henka, (short) reg.getD0_W());
        mm.write(reg.a4 + W_L.henka_work, (short) reg.getD0_W());
    }

    public void _COM_E7_compatible() {
        mm.write(reg.a3 + W_W.use_flag, (byte) 1);
        mm.write(reg.a4 + W_L.pattern, (byte) 0xff);
        reg.setD0_B(reg.getD0_B() & 7);
        reg.setD1_B(reg.getD0_B());
        reg.setD1_B(reg.getD1_B() & 3);
        reg.setD2_B(reg.getD1_B());
        reg.setD1_B(reg.getD1_B() + 0x14);
        mm.write(reg.a3 + W_W.type, (byte) reg.getD1_B());
        mm.write(reg.a4 + W_L.lfo_sp, mm.readByte(reg.a1++));
        reg.setD1_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a3 + W_W.start, (short) reg.getD1_W());//周期
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        mm.write(reg.a3 + W_W.loop_start, (short) reg.getD0_W());//増減

        int f = reg.getD2_B() & 1;
        reg.setD2_B(reg.getD2_B() >> 1);
        if (f == 0) {
            reg.D0_L = (short) reg.getD1_W() * (short) reg.getD0_W();
        }
        reg.setD0_W((short) (-((short) reg.getD0_W())));
        if ((short) reg.getD0_W() < 0) {
            reg.D0_L = 0;
        }
        mm.write(reg.a3 + W_W.loop_end, (short) reg.getD0_W());//最大振幅

        mm.write(reg.a3 + W_W.ko_start, mm.readShort(reg.a3 + W_W.start));
        mm.write(reg.a3 + W_W.ko_loop_start, mm.readShort(reg.a3 + W_W.loop_start));
        mm.write(reg.a3 + W_W.ko_loop_end, mm.readShort(reg.a3 + W_W.loop_end));
        reg.D2_L = 0;
    }

    //	音量 LFO on /off
    //
    //	$E8,num,switch
    public void _COM_E8() {

        reg.D0_L = 0;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.D1_L = reg.D0_L;
        reg.setD1_B(reg.getD1_B() + 3);
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 0:
            _COM_E8_0();
            break;
        case 2:
            _COM_E8_1();
            break;
        case 4:
            _COM_E8_2();
            break;
        case 6:
            _COM_E8_3();
            break;
        }
    }

    public void _COM_E8_0() {
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) & 0x8f));
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x70));
            return;
        }

        if (reg.getD0_B() == 0x82) {
            reg.a4 = W.v_pattern1 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0);
            reg.a4 = W.v_pattern2 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0);
            reg.a4 = W.v_pattern3 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x70));
            return;
        }
        if (reg.getD0_B() == 0x83) {
            reg.a4 = W.v_pattern1 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            reg.a4 = W.v_pattern2 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            reg.a4 = W.v_pattern3 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x70));
            return;
        }

        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f == 0) {
            reg.a4 = W.v_pattern1 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            reg.a4 = W.v_pattern2 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            reg.a4 = W.v_pattern3 + reg.a5;
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
            mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x70));
            return;
        }

        reg.a4 = W.v_pattern1 + reg.a5;
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        reg.a4 = W.v_pattern2 + reg.a5;
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        reg.a4 = W.v_pattern3 + reg.a5;
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        mm.write(reg.a5 + W.lfo, (byte) (mm.readByte(reg.a5 + W.lfo) | 0x70));

    }

    public void _COM_E8_1() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_E8_OFF();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _COM_E8_ON();
            return;
        }
        reg.a4 = W.v_pattern1 + reg.a5;
        _COM_E8_common();
    }

    public void _COM_E8_2() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_E8_OFF();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _COM_E8_ON();
            return;
        }
        reg.a4 = W.v_pattern2 + reg.a5;
        _COM_E8_common();
    }

    public void _COM_E8_3() {
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            _COM_E8_OFF();
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            _COM_E8_ON();
            return;
        }
        reg.a4 = W.v_pattern3 + reg.a5;
        _COM_E8_common();
    }

    public void _COM_E8_common() {
        if (reg.getD0_B() == 0x82) {
            mm.write(reg.a4 + W_L.flag, (short) 0);
            _COM_E8_ON();
            return;
        }
        if (reg.getD0_B() == 0x83) {
            mm.write(reg.a4 + W_L.flag, (short) 0x4000);
            _COM_E8_ON();
            return;
        }
        int f = reg.getD0_B() & 1;
        reg.setD0_B(reg.getD0_B() >> 1);
        if (f == 0) {
            mm.write(reg.a4 + W_L.flag, (short) 0x8001);
            _COM_E8_ON();
            return;
        }
        mm.write(reg.a4 + W_L.flag, (short) 0x8002);
        _COM_E8_ON();
    }

    public void _COM_E8_ON() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        reg.D0_L |= 1 << reg.D1_L;
        mm.write(reg.a5 + W.lfo, (byte) reg.getD0_B());
    }

    public void _COM_E8_OFF() {
        reg.setD0_B(mm.readByte(reg.a5 + W.lfo));
        reg.D0_L &= ~(int) (1 << reg.D1_L);
        mm.write(reg.a5 + W.lfo, (byte) reg.getD0_B());
    }

    //─────────────────────────────────────
    //	音量 LFO delay
    //
    public void _COM_E9() {
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _COM_E9_0();
            break;
        case 4:
            _COM_E9_1();
            break;
        case 6:
            _COM_E9_2();
            break;
        case 8:
            _COM_E9_3();
            break;
        }
    }

    //─────────────────────────────────────
    //	音量 LFO
    //
    public void _COM_E9_0() {
        reg.a0 = reg.a1;
        reg.a4 = W.v_pattern1 + reg.a5;
        _COM_E49_common();
        reg.a1 = reg.a0;
        reg.a4 = W.v_pattern2 + reg.a5;
        _COM_E49_common();
        reg.a1 = reg.a0;
        reg.a4 = W.v_pattern3 + reg.a5;
        _COM_E49_common();
    }

    //─────────────────────────────────────
    //	音量 LFO 1
    public void _COM_E9_1() {
        reg.a4 = W.v_pattern1 + reg.a5;
        _COM_E49_common();
    }

    //─────────────────────────────────────
    //	音量 LFO 2
    public void _COM_E9_2() {
        reg.a4 = W.v_pattern2 + reg.a5;
        _COM_E49_common();
    }

    //─────────────────────────────────────
    //	音量 LFO 3
    public void _COM_E9_3() {
        reg.a4 = W.v_pattern3 + reg.a5;
        _COM_E49_common();
    }

    //─────────────────────────────────────
    //	音量 LFO switch 2
    //
    public void _COM_EA() {
        reg.D0_L = 1;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _COM_EA_0();
            break;
        case 4:
            _COM_EA_1();
            break;
        case 6:
            _COM_EA_2();
            break;
        case 8:
            _COM_EA_3();
            break;
        }
    }

    //─────────────────────────────────────
    //	音量 LFO
    //
    public void _COM_EA_0() {
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() != 0) {
            reg.setD0_B(reg.getD0_B() | 0x80);
        }
        reg.a3 = W.wv_pattern1 + reg.a5;
        mm.write(reg.a3 + W_W.slot, (byte) reg.getD0_B());
        reg.a3 = W.wv_pattern2 + reg.a5;
        mm.write(reg.a3 + W_W.slot, (byte) reg.getD0_B());
        reg.a3 = W.wv_pattern3 + reg.a5;
        mm.write(reg.a3 + W_W.slot, (byte) reg.getD0_B());
    }

    //─────────────────────────────────────
    //	音量 LFO 1
    public void _COM_EA_1() {
        reg.a3 = W.wv_pattern1 + reg.a5;
        _COM_EA_common();
    }

    //─────────────────────────────────────
    //	音量 LFO 2
    public void _COM_EA_2() {
        reg.a3 = W.wv_pattern2 + reg.a5;
        _COM_EA_common();
    }

    //─────────────────────────────────────
    //	音量 LFO 3
    public void _COM_EA_3() {
        reg.a3 = W.wv_pattern3 + reg.a5;
        _COM_EA_common();
    }

    public void _COM_EA_common() {
        reg.D0_L = 0;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        if (reg.getD0_B() != 0) {
            reg.setD0_B(reg.getD0_B() | 0x80);
        }
        mm.write(reg.a3 + W_W.slot, (byte) reg.getD0_B());
    }

    //─────────────────────────────────────
    //	わうわう
    //
    public void _COM_EB() {
        reg.a4 = W.ww_pattern1 + reg.a5;

        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() == 0) {
            mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) & 0xdf));
            return;
        }
        if ((byte) reg.getD0_B() >= 0) {
            mm.write(reg.a4 + W_Ww.delay, mm.readByte(reg.a1++));
            mm.write(reg.a4 + W_Ww.speed, mm.readByte(reg.a1++));
            mm.write(reg.a4 + W_Ww.rate, mm.readByte(reg.a1++));
            mm.write(reg.a4 + W_Ww.depth, mm.readByte(reg.a1++));
            mm.write(reg.a4 + W_Ww.slot, mm.readByte(reg.a1++));
        }
        mm.write(reg.a4 + W_Ww.sync, (byte) 0);
        mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) | 0x20));

        reg.setD0_B(mm.readByte(reg.a4 + W_Ww.speed));
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_Ww.delay));
        mm.write(reg.a4 + W_Ww.delay_work, (byte) reg.getD0_B());
        mm.write(reg.a4 + W_Ww.rate_work, mm.readByte(reg.a4 + W_Ww.rate));
        mm.write(reg.a4 + W_Ww.depth_work, mm.readByte(reg.a4 + W_Ww.depth));
        mm.write(reg.a4 + W_Ww.work, (byte) 0);

        if (mm.readByte(reg.a4 + W_Ww.slot) < 0) {
            mm.write(reg.a4 + W_Ww.sync, (byte) 0xff);
        }
    }

    //─────────────────────────────────────
    //	wavememory effect
    //
    //	[$ED] + [num]b + [switch]b ...
    //	num
    //		$00 ～ $03
    //	switch
    //	minus
    //		$80 = ON
    //		$81 = OFF
    //		$82 = always
    //		$83 = at keyon
    //		$84 = at keyoff
    //	plus = + [switch2]b + [wave]W + [delay]b + [speed]b + [sync]b + [reset]W
    //	$00 = y command
    //		波形データの上位バイトのレジスタに
    //		下位バイトのデータを書き込む
    //	$01 = tone
    //	$02 = panpot
    //
    public void _COM_ED() {
        reg.D0_L = 0;
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a1++));
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _com_ed_1();
            break;
        case 4:
            _com_ed_2();
            break;
        case 6:
            _com_ed_3();
            break;
        case 8:
            _com_ed_4();
            break;
        }
    }

    public void _com_ed_1() {
        reg.a3 = W.we_pattern1 + reg.a5;
        mm.write(reg.a3 + W_We.exec, (byte) 0);
        reg.D1_L = 0;
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            _com_ed_sw_common();
            return;
        }

        if ((byte) _com_ed_common() < 0) {
            mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) & 0xfe));
            return;
        }
        mm.write(reg.a5 + W.effect, (byte) (mm.readByte(reg.a5 + W.effect) | 0x81));
    }

    public void _com_ed_2() {
        reg.a3 = W.we_pattern2 + reg.a5;
        mm.write(reg.a3 + W_We.exec, (byte) 0);
        reg.D1_L = 1;
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            _com_ed_sw_common();
            return;
        }

        _com_ed_common();
        if ((byte) reg.D2_L < 0) {
            mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) & 0xfd));
            return;
        }
        mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) | 0x82));
    }

    public void _com_ed_3() {
        reg.a3 = W.we_pattern3 + reg.a5;
        mm.write(reg.a3 + W_We.exec, (byte) 0);
        reg.D1_L = 2;
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            _com_ed_sw_common();
            return;
        }

        _com_ed_common();
        if ((byte) reg.D2_L < 0) {
            mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) & 0xfb));
            return;
        }
        mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) | 0x84));
    }

    public void _com_ed_4() {
        reg.a3 = W.we_pattern4 + reg.a5;
        mm.write(reg.a3 + W_We.exec, (byte) 0);
        reg.D1_L = 3;
        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        if ((byte) reg.getD0_B() < 0) {
            _com_ed_sw_common();
            return;
        }

        _com_ed_common();
        if ((byte) reg.D2_L < 0) {
            mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) & 0xf7));
            return;
        }
        mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) | 0x88));
    }

    public void _com_ed_sw_common() {
        reg.setD0_W(reg.getD0_W() & 0x7);
        reg.setD0_W(reg.getD0_W() + 1);
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        switch (reg.getD0_B()) {
        case 2:
            _com_ed_80();
            break;
        case 4:
            _com_ed_81();
            break;
        case 6:
            _com_ed_82();
            break;
        case 8:
            _com_ed_83();
            break;
        case 10:
            _com_ed_84();
            break;
        }
    }

    //
    //	ON
    //
    public void _com_ed_80() {
        reg.D0_L = 1;
        reg.D0_L <<= reg.D1_L;
        reg.setD0_B(reg.getD0_B() | 0x80);
        mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) | reg.getD0_B()));
        reg.D0_L = 0;
    }

    //
    //	OFF
    //
    public void _com_ed_81() {
        reg.D0_L = 1;
        reg.D0_L <<= reg.D1_L;
        reg.setD0_B(~reg.getD0_B());
        mm.write(reg.a5 + W.weffect, (byte) (mm.readByte(reg.a5 + W.weffect) & reg.getD0_B()));

        reg.a0 = reg.a5 + W.we_ycom_adrs;
        reg.setD0_W(reg.getD0_W() & 3);
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.a0 = reg.a0 + (int) (short) reg.getD0_W();
        reg.a0 = mm.readInt(reg.a0);
        reg.setD0_W(mm.readShort(reg.a3 + W_We.reset));
        ab.hlw_we_ycom_adrs.get(reg.a0).run();
        reg.D0_L = 0;
    }

    //
    //	always
    //
    public void _com_ed_82() {
        mm.write(reg.a3 + W_We.exec, (byte) 0);
        reg.D0_L = 0;
    }

    //
    //	at keyon
    //
    public void _com_ed_83() {
        mm.write(reg.a3 + W_We.exec, (byte) 0x81);
        reg.D0_L = 0;
    }

    //
    //	at keyoff
    //
    public void _com_ed_84() {
        mm.write(reg.a3 + W_We.exec, (byte) 0x82);
        reg.D0_L = 0;
    }

    public int _com_ed_common() {
        reg.a0 = reg.a5 + W.we_ycom_adrs;
        reg.setD0_W(reg.getD0_W() & 3);
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.setD0_W(reg.getD0_W() + (int) (short) reg.getD0_W());
        reg.a0 = mm.readInt(reg.a0 + (int) (short) reg.getD0_W());
        reg.a0 = mm.readInt(reg.a0);
        mm.write(reg.a3 + W_We.exec_adrs, reg.a0);
        ab.hlw_we_exec_adrs.put(reg.a0, null);

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));

        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        _get_wave_memory_ed();

        mm.write(reg.a3 + W_We.delay, mm.readByte(reg.a1++));
        reg.setD1_B(mm.readByte(reg.a1++));
        mm.write(reg.a3 + W_We.speed, (byte) reg.getD1_B());

        reg.D0_L = 0;
        reg.setD0_B(mm.readByte(reg.a1++));
        mm.write(reg.a3 + W_We.mode, _com_ed_sync_table[reg.getD0_W()]);

        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        reg.setD0_W(mm.readShort(reg.a3 + W_We.reset));

        mm.write(reg.a3 + W_We.exec_flag, (byte) 0x00);
        mm.write(reg.a3 + W_We.loop_flag, (byte) 0x00);
        reg.setD0_B(mm.readByte(reg.a3 + W_We.delay));
        reg.setD0_B(reg.getD0_B() + (int) (byte) reg.getD1_B());
        mm.write(reg.a3 + W_We.delay_work, (byte) reg.getD0_B());
        mm.write(reg.a3 + W_We.adrs_work, mm.readInt(reg.a3 + W_We.start));
        mm.write(reg.a3 + W_We.start_adrs_work, mm.readInt(reg.a3 + W_We.loop_start));
        mm.write(reg.a3 + W_We.end_adrs_work, mm.readInt(reg.a3 + W_We.loop_end));
        mm.write(reg.a3 + W_We.lp_cnt_work, mm.readInt(reg.a3 + W_We.loop_count));
        reg.D0_L = 0;

        return reg.D0_L;
    }

    private static final byte[] _com_ed_sync_table = new byte[] {
                    (byte) 0x80, 0x00, 0x01, 0x02
            };

    public void _get_wave_memory_ed() {
        reg.D0_L = mm.readInt(reg.a6 + Dw.WAVE_PTR);
        if (reg.D0_L == 0) {
            _com_ed_wm_err_exit();
            return;
        }
        reg.a2 = reg.D1_L;
        reg.D5_L = reg.D1_L;

        reg.setD1_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D1_L = (reg.D1_L >> 16) + (reg.D1_L << 16);
        reg.setD1_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        if (reg.D1_L == 0) {
            _com_ed_wm_err_exit();
            return;
        }

        reg.setD1_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        if (reg.D1_L == 0) {
            _com_ed_wm_err_exit();
        }
    }

    public void _com_ed_wm10() {
        do {
            reg.setD2_W(mm.readShort(reg.a2));
            reg.D2_L = (reg.D2_L >> 16) + (reg.D2_L << 16);
            reg.setD2_W(mm.readShort(reg.a2 + 2));
            if (reg.getD0_W() - mm.readShort(reg.a2 + 4) == 0) {
                _com_ed_wm20();
                return;
            }
            reg.a2 = mm.readInt(reg.a2 + reg.D2_L);
        } while (reg.decAfterD1_W() != 0);
        _com_ed_wm_err_exit();
    }

    public void _com_ed_wm20() {
        reg.a2 += 6;

        reg.setD0_B(mm.readByte(reg.a2++));
        reg.setD1_B(reg.getD0_B());
        reg.setD1_B(reg.getD1_B() & 0xf);
        mm.write(reg.a3 + W_We.count, (byte) reg.getD1_B());
        reg.setD0_B(reg.getD1_B() >> 4);
        mm.write(reg.a3 + W_We.ko_flag, (byte) reg.getD1_B());

        reg.setD0_B(mm.readByte(reg.a2++));

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_We.start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_We.loop_start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_We.loop_end, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        mm.write(reg.a3 + W_We.loop_count, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_We.ko_start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_We.ko_loop_start, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L += reg.D5_L;
        mm.write(reg.a3 + W_We.ko_loop_end, reg.D0_L);

        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        reg.D0_L = (reg.D0_L >> 16) + (reg.D0_L << 16);
        reg.setD0_W(mm.readShort(reg.a2));
        reg.a2 += 2;
        mm.write(reg.a3 + W_We.ko_loop_count, reg.D0_L);
    }

    public void _com_ed_wm_err_exit() {
        reg.D2_L = 0xffffffff;// -1;
    }

    //─────────────────────────────────────
    //	hardware LFO delay
    //		[$EF] + [delay]b
    //
    public void _COM_EF() {
        mm.write(reg.a5 + W.flag, (byte) (mm.readByte(reg.a5 + W.flag) | 0x02));
        mm.write(reg.a5 + W.flag2, (byte) (mm.readByte(reg.a5 + W.flag2) | 0x04));
        reg.a4 = reg.a5 + W.v_pattern4;
        reg.setD0_B(mm.readByte(reg.a1++));
        if (reg.getD0_B() - 0xff == 0) {
            _COM_EF_add();
            return;
        }

        mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD0_B());
        reg.setD0_B(reg.getD0_B() + mm.readByte(reg.a4 + W_L.lfo_sp));
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD0_B());
    }

    public void _COM_EF_add() {
        reg.setD0_B(mm.readByte(reg.a1++));
        reg.setD1_B(mm.readByte(reg.a4 + W_L.keydelay));
        reg.setD1_B(reg.getD1_B() + (int) (byte) reg.getD0_B());
        reg.setD1_B(reg.getD1_B() + mm.readByte(reg.a4 + W_L.lfo_sp));
        mm.write(reg.a4 + W_L.keydelay, (byte) reg.getD1_B());
        mm.write(reg.a4 + W_L.delay_work, (byte) reg.getD1_B());
    }

    //─────────────────────────────────────
    //	永久ループポイントマーク
    //			[$F9]
    public void _COM_F9() {
        mm.write(reg.a5 + W.loop, reg.a1);
    }

    //─────────────────────────────────────
    //	リピート抜け出し
    //			[$FB] + [終端コマンドへのオフセット]W
    public void _COM_FB() {
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        reg.a0 = reg.a1 + (int) (short) reg.getD0_W() + 1;
        reg.setD0_W(mm.readShort(reg.a0));
        reg.a0 += 2;
        if ((mm.readByte(reg.a0 + (int) (short) reg.getD0_W() + 2) - 1) != 0) return;
        reg.a1 = reg.a0;
    }

    //─────────────────────────────────────
    //	リピート開始
    //			[$FC] + [リピート回数]b + [$00]b
    public void _COM_FC() {
        mm.write(reg.a1 + 1, mm.readByte(reg.a1));
        reg.a1 += 2;
    }

    //─────────────────────────────────────
    //	リピート終端
    //			[$FD] + [開始コマンドへのオフセット]W
    public void _COM_FD() {
        reg.setD0_W(mm.readShort(reg.a1));
        reg.a1 += 2;
        if (mm.readByte(reg.a1 + (int) (short) reg.getD0_W() + 1) == 0) {
            reg.a1 = reg.a1 + (int) (short) reg.getD0_W() + 3;
            return;
        }
        mm.write(reg.a1 + (int) (short) reg.getD0_W() + 2, (byte) (mm.readByte(reg.a1 + (int) (short) reg.getD0_W() + 2) - 1));
        if (mm.readByte(reg.a1 + (int) (short) reg.getD0_W() + 2) != 0) {
            reg.a1 = reg.a1 + (int) (short) reg.getD0_W() + 3;
        }
    }

    //─────────────────────────────────────
    //	tempo 設定
    //
    public void _COM_FE() {
        mm.write(reg.a6 + Dw.TEMPO, mm.readByte(reg.a1++));
    }

    //─────────────────────────────────────
    public void _all_end_check() {
        reg.setD0_W(mm.readShort(reg.a6 + Dw.USE_TRACK));
        reg.a0 = reg.a6 + Dw.TRACKWORKADR;
        do {
            if (mm.readByte(reg.a0 + W.flag) < 0) {
                return;
            }
            reg.a0 = reg.a0 + W._track_work_size;// Dw._work_size;
            reg.setD0_W(reg.getD0_W() - 1);
        } while (reg.getD0_W() != 0);
        mm.write(reg.a6 + Dw.DRV_STATUS, (byte) 0x20);
        mm.write(reg.a6 + Dw.LOOP_COUNTER, (short) 0xffff);// -1
        reg.D0_L = 2;
        mndrv.SUBEVENT();
    }
}
