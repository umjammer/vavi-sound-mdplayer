/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.chips.YmF271;
import mdsound.instrument.YmF271Inst;


/**
 * YmF271Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class YmF271Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getYMF271Type();

    private final RSoundChip[] realChips = {null, null};

    public final int[][][] register = {
            {null, null},
            {null, null}
    };

    private BasePlugin<? extends BaseDriver> context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {YmF271Inst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        this.context = context;

        for (int chipId = 0; chipId < 2; chipId++) {
            register[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                register[chipId][0][i] = 0;
                register[chipId][1][i] = 0;
                register[chipId][2][i] = 0;
                register[chipId][3][i] = 0;
                register[chipId][4][i] = 0;
                register[chipId][5][i] = 0;
                register[chipId][6][i] = 0;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void write(int chipId, int port, int addr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPX", 2);
        else
            context.chipLED.put("SecOPX", 2);

        if (model == EnmModel.VirtualModel)
            register[chipId][port][addr] = data;

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(inst(chipId), chipId, port, addr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;
            realChips[chipId].setRegister(port * 0x100 + addr, data);
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOPX", 2);
        else
            context.chipLED.put("SecOPX", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(YmF271Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);
    }

    public YmF271 read(int chipId) {
        return context.mds.inst(YmF271Inst.class).getChip(chipId);
    }
}
