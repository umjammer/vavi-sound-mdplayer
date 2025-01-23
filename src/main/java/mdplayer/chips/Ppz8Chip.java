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
import mdsound.chips.PPZ8;
import mdsound.instrument.Ppz8Inst;

import static java.lang.System.getLogger;


/**
 * Ppz8Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class Ppz8Chip implements Chip {

    private static final Logger logger = getLogger(Ppz8Chip.class.getName());

    private static final boolean[][] mask = {
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

    public PPZ8.Channel[] read(int chipId) {
        return context.mds.inst(Ppz8Inst.class).readStatus(chipId);
    }

    public void writePcm(int chipId, int bank, int mode, byte[][] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPZ8", 2);
        else
            context.chipLED.put("SecPPZ8", 2);

        context.mds.inst(Ppz8Inst.class).writePcm(chipId, bank, mode, pcmData);
    }

    public void write(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            context.chipLED.put("PriPPZ8", 2);
        else
            context.chipLED.put("SecPPZ8", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        context.mds.inst(Ppz8Inst.class).write(chipId, dPort, dAddr, dData);
    }

    public void setMask(int chipId, int ch, boolean mask) {
        Ppz8Chip.mask[chipId][ch] = mask;
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        try {
            setMask(chipId, ch, false);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
