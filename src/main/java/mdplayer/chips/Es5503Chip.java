/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdsound.Instrument;
import mdsound.instrument.Es5503Inst;


/**
 * Es5503Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-02 nsano initial version <br>
 */
public class Es5503Chip extends BaseChip {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Es5503Inst.class};
    }

    /**
     * The oscillator registers as they were written, which is the one thing about this chip that
     * has to be kept on the way in.
     * <p>
     * Everything else the display wants - whether an oscillator runs, its level, where it is
     * routed - the emulator answers for itself, and is worth more from there because it keeps
     * moving after the last write. But how big a step the frequency register is depends on the
     * oscillator's resolution and wave table size, and those are only in register {@code 0xc0+osc}
     * on their way past: without them a rate is out by octaves and the key column goes blank.
     *
     * @see mdplayer.fmdsp.Es5503Reader
     */
    public final int[][] register = new int[2][0x100];

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);
        // shared between songs, like the chip itself
        for (int[] regs : register) Arrays.fill(regs, 0);
    }

    public void write(int chipId, int port, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (port >= 0 && port < register[chipId].length) {
            register[chipId][port] = data & 0xff;
        }
        if (model == EnmModel.VirtualModel) {
            context.mds.write(inst(chipId), chipId, 0, port, data);
        }
    }

    public void writePcm(int chipId, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel) {
            context.mds.inst(Es5503Inst.class).writePcm(chipId, Arrays.copyOfRange(buf, srcOffset, srcOffset + length), offset, length);
        } else {
        }

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        Es5503Inst inst = context.mds.inst(Es5503Inst.class);
        return inst == null ? Collections.emptyMap() : inst.getView(chipId, "info");
    }
}
