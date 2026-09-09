/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.muap;

import java.io.InputStream;
import java.util.function.Function;

import mdplayer.Common.EnmModel;
import mdplayer.chips.Cs4231Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.mucom.MucomDriver;
import mdplayer.driver.BasePlugin.Compilable;
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
public class MuapPlugin extends BasePlugin<MuapDriver> implements Compilable {

    /**
     * Turns a ".mus" into the ".o" the driver plays, when that is what was opened.
     * <p>
     * The compiler hands back the tone table and the label table along with the object, and it
     * leaves them on the driver it ran on - so this has to be the driver that goes on to play,
     * and it has to have happened before {@link #initChips} passes them around.
     */
    @Override
    public void compile() {
        if (!fileFormat.isMml()) {
            return;
        }
        byte[] compiled = driverVirtual.compile(dataBuf);
        if (compiled == null) {
            throw new IllegalStateException("muap: " + playingFileName + ": the song did not compile");
        }
        dataBuf = compiled;
        // a song out of an archive has no name of its own here, and the driver needs one to
        // resolve what the MML includes - so keep the name it came in with
        if (fileFormat.getCompiledFilename() != null) {
            playingFileName = fileFormat.getCompiledFilename();
        }
    }

    @Override
    public void prepare() {
        driverVirtual = new MuapDriver(this);

        compile();

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new MuapDriver(this);
//        }
//        driverPianoRoll = null;
//        if (setting.pianoRoll.usePianoRoll) {
//            driverPianoRoll = new MuapDriver(this);
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {

        // Share tone table
        byte[] toneBuff = driverVirtual.toneBuff;
//        driverReal.toneBuff = toneBuff;
//        driverPianoRoll.toneBuff = toneBuff;
        // Share label table
        int[] labelAdr = driverVirtual.labelAdr;
//        driverReal.labelAdr = labelAdr;
//        driverPianoRoll.labelAdr = labelAdr;

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Ym2608Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
        chip.clock = MucomDriver.opnaBaseClock;
        if (chip.instrument instanceof Ym2608Inst ym2608) {
            chip.setVolumes.put("FM", ym2608::setVolume);
            chip.setVolumes.put("SSG", ym2608::setVolume);
            chip.setVolumes.put("RHYTHM", ym2608::setVolume);
            chip.setVolumes.put("ADPCM", ym2608::setVolume);
        }
        Function<String, InputStream> fn = chipRegister.chip(Ym2608Chip.class)::getOPNARyhthmStream;
        chip.option = new Object[] {fn};
        put(Ym2608Chip.class, chip);

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
            put(Ym2612Chip.class, chip);
        }

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Cs4231Inst.class);
        chip.samplingRate = 55467; // (UInt32) setting.outputDevice.SampleRate;
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Cs4231Chip.class);
        chip.clock = 0;
        chip.option = null;
        put(Cs4231Chip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
        setVolume("SSG", Ym2608Chip.class, true, setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        setVolume("RHYTHM", Ym2608Chip.class, true, setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        setVolume("ADPCM", Ym2608Chip.class, true, setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.VirtualModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, EnmModel.RealModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.VirtualModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, EnmModel.RealModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.VirtualModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, EnmModel.RealModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.VirtualModel /* , 0 */); // reset PSG TONE
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, EnmModel.RealModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, EnmModel.VirtualModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, EnmModel.RealModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, EnmModel.VirtualModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, EnmModel.RealModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, EnmModel.VirtualModel /* , 0 */);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, EnmModel.RealModel /* , 0 */);

        chipRegister.chip(Ym2608Chip.class).writeClock(0, MucomDriver.opnaBaseClock, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).writeClock(1, MucomDriver.opnaBaseClock, EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        driverVirtual.init(EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
//        if (driverPianoRoll != null) {
//            driverPianoRoll.init(dataBuf, this, EnmModel.PianoRollModel, new Class[] {Ym2608Chip.class},
//                    (int) (setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000),
//                    (int) (setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000));
//        }

//        if (driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//           realChip.WaitOPNADPCMData(setting.YM2608Type[0].realChipInfo[0].SoundLocation == -1);
//        }

        oneTimeReset = false;
    }
}
