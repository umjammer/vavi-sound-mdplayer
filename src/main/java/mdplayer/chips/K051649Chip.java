/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.Instrument;
import mdsound.chips.K051649;
import mdsound.instrument.K051649Inst;


/**
 * K051649Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K051649Chip implements Chip {

    private final Setting.ChipType2[] chipTypes = setting.getK051649Type();

    private final RSoundChip[] realChips = {null, null};

    private int sccR_port;

    private int sccR_offset;

    private int sccR_dat;

    public final byte[] keyOnOff = {
            0, 0
    };

    public final boolean[][] mask = {
            {false, false, false, false, false},
            {false, false, false, false, false}
    };

    private Audio context;

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {K051649Inst.class};
    }

    @Override
    public void init(Audio context) {
        this.context = context;

        K051649Inst chip = Instrument.getInstrument(K051649Inst.class); // ugly
        chip.start(0, 100, 200);
        chip.start(1, 100, 200);
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {

    }

    public void setMask(int chipId, int ch) {
        mask[chipId][ch] = true;
        write(chipId, (3 << 1) | 1, keyOnOff[chipId], EnmModel.VirtualModel);
    }

    public void resetMask(int chipId, int ch) {
        mask[chipId][ch] = false;
        write(chipId, (3 << 1) | 1, keyOnOff[chipId], EnmModel.VirtualModel);
    }


    public void write(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK051649", 2);
        else
            context.chipLED.put("SecK051649", 2);

        if ((adr & 1) != 0) {
            if ((adr >> 1) == 3) { // keyonoff
                keyOnOff[chipId] = (byte) data;
                data &= mask[chipId][0] ? 0xfe : 0xff;
                data &= mask[chipId][1] ? 0xfd : 0xff;
                data &= mask[chipId][2] ? 0xfb : 0xff;
                data &= mask[chipId][3] ? 0xf7 : 0xff;
                data &= mask[chipId][4] ? 0xef : 0xff;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!chipTypes[chipId].getUseReal()[0]) {
                context.mds.write(inst(chipId), chipId, 0, adr, data);

                // Save register data
                context.mds.inst(inst(chipId)).write(chipId, 0, adr, data);
            }
        } else {
            if (realChips[chipId] == null)
                return;

            // Save register data
            context.mds.inst(inst(chipId)).write(chipId, 0, adr, data);

            if ((adr & 1) == 0) {
                sccR_port = (adr >> 1);
                sccR_offset = data;
            } else {
                sccR_dat = data;

                switch (sccR_port) {
                    case 0x00:
                        sccR_offset += 0x00;
                        break;
                    case 0x01:
                        sccR_offset += 0x80;
                        break;
                    case 0x02:
                        sccR_offset += 0x8a;
                        break;
                    case 0x03:
                        sccR_offset += 0x8f;
                        break;
                }

                realChips[chipId].setRegister(setting.getDebug_SCCbaseAddress() | sccR_offset, sccR_dat);
            }
        }
    }

    public void softReset(int chipId, EnmModel model) {
        // All channel volume zero
        for (int i = 0; i < 5; i++) {
            write(chipId, (0x00 << 1) + 0, i, model);
            write(chipId, (0x02 << 1) + 1, 0x00, model);
            write(chipId, (0x00 << 1) + 0, i, model);
            write(chipId, (0x03 << 1) + 1, 0x00, model);
        }
    }

    public K051649 getChip(int chipId) {
        return context.mds.inst(K051649Inst.class).getChip(chipId);
    }

    @Override
    public void softReset(EnmModel model) {
        softReset(0, model);
        softReset(1, model);
    }
}
