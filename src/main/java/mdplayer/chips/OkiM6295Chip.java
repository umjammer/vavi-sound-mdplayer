/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.chips.OkiM6295;
import mdsound.instrument.OkiM6295Inst;


/**
 * Oki6295Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class OkiM6295Chip implements Chip {

    private static final boolean[][] maskOKIM6295 = {
            {false, false, false, false},
            {false, false, false, false}
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

    public void setMaskOKIM6295(int chipId, int ch, boolean mask) {
        maskOKIM6295[chipId][ch] = mask;
        if (mask)
            context.mds.setOkiM6295Mask(0, chipId, 1 << ch);
        else
            context.mds.resetOkiM6295Mask(0, chipId, 1 << ch);
    }

    public OkiM6295.ChannelInfo getOKIM6295Info(int chipId) {
        return context.mds.getOkiM6295Info(0, chipId);
    }

    public void writeOKIM6295PCMData(int chipId,
                                     int romSize,
                                     int dataStart,
                                     int dataLength,
                                     byte[] romData,
                                     int srcStartAdr,
                                     EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOKI9", 2);
        else
            context.chipLED.put("SecOKI9", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteOkiM6295PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeOKIM6295(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriOKI9", 2);
        else
            context.chipLED.put("SecOKI9", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(OkiM6295Inst.class, chipId, 0, port, data);
//logger.log(Level.TRACE, "chipId=%d Port=%x data=%x".formatted(chipId, port, data));
        }
    }

    public void setOKIM6295Mask(int chipId, int ch) {
        setMaskOKIM6295(chipId, ch, true);
    }

    public void resetOKIM6295Mask(int chipId, int ch) {
        setMaskOKIM6295(chipId, ch, false);
    }
}
