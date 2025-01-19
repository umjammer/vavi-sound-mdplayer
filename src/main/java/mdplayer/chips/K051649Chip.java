/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdsound.Instrument;
import mdsound.instrument.K051649Inst;


/**
 * K051649Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class K051649Chip implements Chip {

    private final Setting.ChipType2[] ctK051649 = new Setting.ChipType2[] {
            setting.getK051649Type()[0], setting.getK051649Type()[1]
    };

    private final RSoundChip[] scK051649 = {null, null};

    public K051649Inst scc_k051649 = Instrument.getInstrument(K051649Inst.class);

    private int sccR_port;

    private int sccR_offset;

    private int sccR_dat;

    public byte[] K051649tKeyOnOff = {
            0, 0
    };

    public boolean[][] maskChK051649 = {
            {false, false, false, false, false},
            {false, false, false, false, false}
    };

    private ChipRegister context;

    @Override
    public void init(ChipRegister context) {
        this.context = context;
        scc_k051649.start((byte) 0, 100, 200);
        scc_k051649.start((byte) 1, 100, 200);
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {

    }

    public void setK051649Mask(int chipId, int ch) {
        maskChK051649[chipId][ch] = true;
        writeK051649(chipId, (3 << 1) | 1, K051649tKeyOnOff[chipId], EnmModel.VirtualModel);
    }

    public void resetK051649Mask(int chipId, int ch) {
        maskChK051649[chipId][ch] = false;
        writeK051649(chipId, (3 << 1) | 1, K051649tKeyOnOff[chipId], EnmModel.VirtualModel);
    }


    public void writeK051649(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            context.chipLED.put("PriK051649", 2);
        else
            context.chipLED.put("SecK051649", 2);

        if ((adr & 1) != 0) {
            if ((adr >> 1) == 3) { // keyonoff
                K051649tKeyOnOff[chipId] = (byte) data;
                data &= maskChK051649[chipId][0] ? 0xfe : 0xff;
                data &= maskChK051649[chipId][1] ? 0xfd : 0xff;
                data &= maskChK051649[chipId][2] ? 0xfb : 0xff;
                data &= maskChK051649[chipId][3] ? 0xf7 : 0xff;
                data &= maskChK051649[chipId][4] ? 0xef : 0xff;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctK051649[chipId].getUseReal()[0]) {
                context.mds.write(K051649Inst.class, chipId, 0, adr, data);

                // Save register data
                scc_k051649.write(chipId, 0, adr, data);
            }
        } else {
            if (scK051649[chipId] == null)
                return;

            // Save register data
            scc_k051649.write(chipId, 0, adr, data);

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

                scK051649[chipId].setRegister(setting.getDebug_SCCbaseAddress() | sccR_offset, sccR_dat);
            }
        }
    }

    public void softResetK051649(int chipId, EnmModel model) {
        // All channel volume zero
        for (int i = 0; i < 5; i++) {
            writeK051649(chipId, (0x00 << 1) + 0, i, model);
            writeK051649(chipId, (0x02 << 1) + 1, 0x00, model);
            writeK051649(chipId, (0x00 << 1) + 0, i, model);
            writeK051649(chipId, (0x03 << 1) + 1, 0x00, model);
        }
    }
}
