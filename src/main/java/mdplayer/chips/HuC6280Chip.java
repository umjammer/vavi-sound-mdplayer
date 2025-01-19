/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdsound.instrument.HuC6280Inst;


/**
 * HuC6280Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class HuC6280Chip implements Chip {

    private final Setting.ChipType2[] ctHuC6280 = new Setting.ChipType2[] {
            setting.getHuC6280Type()[0], setting.getHuC6280Type()[1]
    };

    private final boolean[][] maskChHuC6280 = {
            {false, false, false, false, false, false},
            {false, false, false, false, false, false}
    };

    private final int[] HuC6280CurrentCh = {
            0, 0
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    public void setHuC6280Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriHuC", 2);
        else
            context.chipLED.put("SecHuC", 2);

        if (model == EnmModel.VirtualModel) {
            if (!ctHuC6280[chipId].getUseReal()[0]) {
                if (dAddr == 0) {
                    HuC6280CurrentCh[chipId] = dData & 7;
                }
                if (dAddr == 4) {
                    dData = maskChHuC6280[chipId][HuC6280CurrentCh[chipId]] ? 0 : dData;
                }
//logger.log(Level.TRACE, "chipId:%d adr:%d Dat:%d".formatted(chipId, dAddr, dData));
                context.mds.write(HuC6280Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
//            if (scHuC6280[chipId] == null) return;
        }
    }

    public int readHuC6280Register(int chipId, int adr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriHuC", 2);
        else
            context.chipLED.put("SecHuC", 2);

        if (model == EnmModel.VirtualModel) {
            return context.mds.readOotakePsg(chipId, adr);
        }

        return 0;
    }

    public void setMaskHuC6280(int chipId, int ch, boolean mask) {
        maskChHuC6280[chipId][ch] = mask;
    }
}
