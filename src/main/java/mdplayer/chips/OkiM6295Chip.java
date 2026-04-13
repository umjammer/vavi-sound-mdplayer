/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.chips.OkiM6295;
import mdsound.instrument.OkiM6295Inst;


/**
 * Oki6295Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class OkiM6295Chip extends BaseChip {

    private final boolean[][] mask = {
            {false, false, false, false},
            {false, false, false, false}
    };

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {OkiM6295Inst.class};
    }

    public void setMask(int chipId, int ch, boolean mask) {
        this.mask[chipId][ch] = mask;
        if (mask)
            context.mds.inst(inst(chipId), 0).setMask(chipId, 1 << ch);
        else
            context.mds.inst(inst(chipId), 0).resetMask(chipId, 1 << ch);
    }

    public OkiM6295.ChannelInfo read(int chipId) {
        return context.mds.inst(OkiM6295Inst.class, 0).getChInfo(chipId);
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOKI9", 2);
        else
            context.chipLED.put("SecOKI9", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(OkiM6295Inst.class).writePcm(chipId, buf, offset, length, srcOffset, romSize);

        dumpData(model, "OKIM6295_PCMData", srcOffset, buf, length);
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOKI9", 2);
        else
            context.chipLED.put("SecOKI9", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
//logger.log(Level.TRACE, "chipId=%d Port=%x data=%x".formatted(chipId, port, data));
        }
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }
}
