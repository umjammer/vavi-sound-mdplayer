/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.mdsdrv.MdsDrv;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MdsPlugin (Mega Drive) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-01-08 nsano initial version <br>
 */
public class MdsPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MdsPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MdsDrv();
        ((MdsDrv) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new MdsDrv();
//            ((MdsDrv) audio.driverReal).setPlayingFileName(playingFileName);
//        }
        prepare();
        boolean r = _play();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    /** */
    private boolean _play() {
        startTrdVgmReal();

        MDSound.Chip chip = new MDSound.Chip();
        chip.instrument = Instrument.getInstrument(audio.chipRegister.chip(Ym2612Chip.class).inst(0));
        chip.id = 0;
        audio.chipLED.put("PriOPM", 1);
        if (chip.instrument instanceof Ym2612Inst) {
            chip.option = new Object[] {
                    (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00) |
                            (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
            };
        } else if (chip.instrument instanceof Ym3438Inst ym3438) {
            switch (setting.getNukedOPN2().emuType) {
                case 0 -> ym3438.setChipType(Ym3438Const.Type.discrete);
                case 1 -> ym3438.setChipType(Ym3438Const.Type.asic);
                case 2 -> ym3438.setChipType(Ym3438Const.Type.ym2612);
                case 3 -> ym3438.setChipType(Ym3438Const.Type.ym2612_u);
                case 4 -> ym3438.setChipType(Ym3438Const.Type.asic_lp);
            }
        }
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
        chip.clock = 7670454;
        audio.chipRegister.chip(Ym2612Chip.class).clock = 7670454;
        audio.chipLED.put("PriOPN2", 1);
        put(Ym2612Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Sn76489Chip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class);
        chip.clock = 3579545;
        chip.option = null;
        audio.chipLED.put("PriDCSG", 1);
        put(Sn76489Chip.class, chip);

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.setVolume(MAIN_TAG, Ym2612Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class));
        audio.setVolume(MAIN_TAG, Sn76489Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class));

        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ym2612Chip.class, Sn76489Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ym2612Chip.class, Sn76489Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//            SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
        }

        return true;
    }
}
