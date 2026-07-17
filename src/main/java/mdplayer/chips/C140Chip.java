/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.MDChipParams.VolumeInfo;
import mdplayer.RealChip.RSoundChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.Instrument.PcmEnabledInstrument;
import mdsound.chips.C140;
import mdsound.instrument.C140Inst;
import mdsound.instrument.C219Inst;


/**
 * C140Chip.
 * <p>
 * system property
 * <li>{@code mdplayer.variant.c140} ... active chip index</li>
 * </p>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class C140Chip extends BaseChip {

    private final Setting.ChipType2[] chipTypes = setting.getC140Type();

    private final RSoundChip[] realChips = {null, null};

    // TODO eliminate cache like params, retrieve directly
    @Deprecated
    public final byte[][] pcmRegister = {null, null};

    @Deprecated
    public final boolean[][] pcmKeyOn = {null, null};

    @Deprecated
    private static final boolean[][] mask = {
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false}
    };

    @SuppressWarnings("unchecked")
    private Class<? extends PcmEnabledInstrument> _inst(int chipId) {
        return (Class<? extends PcmEnabledInstrument>) inst(chipId);
    }

    @Override
    public int activeIndex(int chipId) {
        return chipTypes[chipId].getEnabledId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {C140Inst.class, C219Inst.class};
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        super.init(context);

        for (int chipId = 0; chipId < 2; chipId++) {
            pcmRegister[chipId] = new byte[0x200];
            pcmKeyOn[chipId] = new boolean[24];
        }
    }

    public void setMask(int chipId, int ch, boolean mask) {
        C140Chip.mask[chipId][ch] = mask;
    }

    public void write(int chipId, int adr, int data, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if ((model == EnmModel.VirtualModel && (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (realChips != null && realChips[chipId] != null))) {
            pcmRegister[chipId][adr] = (byte) data;
            int ch = adr >> 4;
            switch (adr & 0xf) {
                case 0x05:
                    if ((data & 0x80) != 0) {
                        pcmKeyOn[chipId][ch] = true;
                        data = mask[chipId][ch] ? data & 0x7f : data;
                    }
                    break;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (chipTypes[chipId] == null || !chipTypes[chipId].getUseReal()[0])
                context.mds.write(inst(chipId), chipId, 0, adr, data);
        } else {
            if (realChips != null && realChips[chipId] != null)
                realChips[chipId].setRegister(adr, data);
        }
    }

    public void writePcm(int chipId, int romSize, int offset, int length, byte[] buf, int srcOffset, EnmModel model) {
        fireEventHappened("led.on", chipId);

        if (model == EnmModel.VirtualModel)
            context.mds.inst(_inst(chipId)).writePcm(chipId, buf, offset, length, srcOffset, romSize);
        else {
            if (realChips != null && realChips[chipId] != null) {
                // Start address setting
                realChips[chipId].setRegister(0x1_0000, offset);
                realChips[chipId].setRegister(0x1_0001, offset >> 8);
                realChips[chipId].setRegister(0x1_0002, offset >> 16);
                // Data Transfer
                for (int i = 0; i < length; i++) {
                    realChips[chipId].setRegister(0x1_0004, buf[srcOffset + i]);
                }
//                realChips[chipId].setRegister(0x10006, romSize);

                context.chipRegister.plugin(RealChipPlugin.class).realChip.sendData();
            }
        }

        dumpData(model, "PCMData", srcOffset, buf, length);
    }

    public void writeType(int chipId, int type, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (realChips != null && realChips[chipId] != null) {
                switch (C140.Type.valueOf(type)) {
                    case SYSTEM2:
                        realChips[chipId].setRegister(0x1_0008, 0);
                        break;
                    case SYSTEM21:
                        realChips[chipId].setRegister(0x1_0008, 1);
                        break;
                    case ASIC219:
                        realChips[chipId].setRegister(0x1_0008, 2);
                        break;
                }
            }
        }
    }

    public byte[] read(int chipId) {
        return pcmRegister[chipId];
    }

    @Override
    public Map<String, Object> getInfo(int chipId) {
        return Map.of("keyOn", pcmKeyOn[chipId]);
    }

    public void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    @Deprecated
    public static class Params {

        public final mdplayer.MDChipParams.Channel[] channels = new mdplayer.MDChipParams.Channel[] {new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel(), new mdplayer.MDChipParams.Channel()};
    }

    @Deprecated
    public final Params[] c140 = new Params[] {new Params(), new Params()};
    @Deprecated
    public final Params[] c140_old = new Params[] {new Params(), new Params()};

    @Deprecated
    public final VolumeInfo C140_ = new VolumeInfo();
    @Deprecated
    public final VolumeInfo C140_old = new VolumeInfo();
}
