/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.plugin;

import java.util.function.Function;

import dotnet4j.io.Stream;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.Cs4231Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.muap.MuapJava;
import mdplayer.driver.mucom.MucomJava;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.Ym3438Const.Type;
import mdsound.instrument.Cs4231Inst;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;

import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MuapPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-29 nsano initial version <br>
 */
public class MuapPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MuapJava();
        ((MuapJava) audio.driverVirtual).playingFileName = playingFileName;

        audio.driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new MuapJava();
//
//            ((MuapJava) audio.driverReal).playingFileName = playingFileName;
//        }
//        audio.driverPianoRoll = null;
//        if (setting.pianoRoll.usePianoRoll) {
//            audio.driverPianoRoll = new MuapJava();
//
//            ((MuapJava) driverPianoRoll).playingFileName = playingFileName;
//        }
        boolean r = _play(setting, audio.playingFileFormat);
        super.play();
        return r;
    }

    /**  */
    public boolean _play(Setting setting, FileFormat fileType) {

        // Share tone table
        byte[] toneBuff = ((MuapJava) audio.driverVirtual).toneBuff;
//        ((MuapJava) audio.driverReal).toneBuff = toneBuff;
//            ((MuapJava) audio.driverPianoRoll).toneBuff = toneBuff;
        // Share label table
        int[] labelAdr = ((MuapJava) audio.driverVirtual).labelAdr;
//        ((MuapJava) audio.driverReal).labelAdr = labelAdr;
//            ((MuapJava) audio.driverPianoRoll).labelAdr = labelAdr;

        startTrdVgmReal();

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Ym2608Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        chip.clock = MucomJava.opnaBaseClock;
        if (chip.instrument instanceof Ym2608Inst ym2608) {
            chip.setVolumes.put("FM", ym2608::setVolume);
            chip.setVolumes.put("SSG", ym2608::setVolume);
            chip.setVolumes.put("RHYTHM", ym2608::setVolume);
            chip.setVolumes.put("ADPCM", ym2608::setVolume);
        }
        Function<String, Stream> fn = Common::getOPNARyhthmStream;
        chip.option = new Object[] {fn};
        audio.chipLED.put("PriOPNA", 1);
        put(Ym2608Chip.class, chip);
        audio.chipRegister.chip(Ym2608Chip.class).clock = 7987200;

        chip = new MDSound.Chip();

        chip.instrument = Instrument.getInstrument(Ym2612Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
        chip.clock = 7987200;
        if (chip.instrument instanceof Ym2612Inst) {
            chip.option = new Object[] {
                    (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00) |
                            (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
            };
        } else if (chip.instrument instanceof Ym3438Inst ym3438) {
            switch (setting.getNukedOPN2().emuType) {
                case 0:
                    ym3438.setChipType(Type.discrete);
                    break;
                case 1:
                    ym3438.setChipType(Type.asic);
                    break;
                case 2:
                    ym3438.setChipType(Type.ym2612);
                    break;
                case 3:
                    ym3438.setChipType(Type.ym2612_u);
                    break;
                case 4:
                    ym3438.setChipType(Type.asic_lp);
                    break;
            }
        }
        if (chip.clock != 0) {
            audio.chipRegister.chip(Ym2612Chip.class).clock = 7670454;
            audio.chipLED.put("PriOPN2", 1);
            put(Ym2612Chip.class, chip);
        }

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Cs4231Inst.class);
        chip.samplingRate = 55467; // (UInt32) setting.outputDevice.SampleRate;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Cs4231Chip.class);
        chip.clock = 0;
        chip.option = null;
        audio.chipLED.put("PriCS4231", 1);
        put(Cs4231Chip.class, chip);

        if (hiyorimiNecessary) hiyorimiNecessary = true;
        else hiyorimiNecessary = false;

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        audio.setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
        audio.setVolume("SSG", Ym2608Chip.class, true, setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        audio.setVolume("RHYTHM", Ym2608Chip.class, true, setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        audio.setVolume("ADPCM", Ym2608Chip.class, true, setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.VirtualModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.RealModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.VirtualModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.RealModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.VirtualModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.RealModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.VirtualModel /* , 0 */); // reset PSG TONE
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.RealModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, EnmModel.VirtualModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, EnmModel.RealModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, EnmModel.VirtualModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, EnmModel.RealModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, EnmModel.VirtualModel /* , 0 */);
        audio.chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, EnmModel.RealModel /* , 0 */);

        audio.chipRegister.chip(Ym2608Chip.class).writeClock(0, MucomJava.opnaBaseClock, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).writeClock(1, MucomJava.opnaBaseClock, EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
        audio.chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        if (!audio.driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel, new Class[] {Ym2608Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, EnmModel.RealModel, new Class[] {Ym2608Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }
//        if (audio.driverPianoRoll != null) {
//            if (!audio.driverPianoRoll.init(vgmBuf, this, EnmModel.PianoRollModel, new Class[] {Ym2608Chip.class},
//                    (int) (setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000),
//                    (int) (setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000)))
//                return false;
//        }

        if (audio.driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//           realChip.WaitOPNADPCMData(setting.YM2608Type[0].realChipInfo[0].SoundLocation == -1);
        }

        oneTimeReset = false;

        return true;
    }
}
