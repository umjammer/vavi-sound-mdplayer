/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdsound.chips.Rf5c68;
import mdsound.instrument.Rf5c68Inst;

import static java.lang.System.getLogger;


/**
 * Rf5C68Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Rf5C68Chip implements Chip {

    private static final Logger logger = getLogger(Rf5C68Chip.class.getName());

    private final boolean[][] maskChRF5C68 = {
            {false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false}
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

    public void setMaskRF5C68(int chipId, int ch, boolean mask) {
        maskChRF5C68[chipId][ch] = mask;
        if (mask)
            context.mds.setRf5c68Mask(chipId, ch);
        else
            context.mds.resetRf5c68Mask(chipId, ch);
    }

    public void writeRF5C68PCMData(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteRf5c68PCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public void writeRF5C68(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel) {
            context.mds.write(Rf5c68Inst.class, chipId, 0, adr, data);
        }
    }

    public void writeRF5C68MemW(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriRF5C68", 2);
        else
            context.chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            context.mds.WriteRf5c68MemW(chipId, offset, data);
    }

    public Rf5c68 getRf5c68Register(int chipId) {
        return context.mds.ReadRf5c68Register(chipId);
    }

    public void setRF5C68Mask(int chipId, int ch) {
        setMaskRF5C68(chipId, ch, true);
    }

    public void resetRF5C68Mask(int chipId, int ch) {
        try {
            setMaskRF5C68(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
