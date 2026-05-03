package mdplayer.plugin;

import java.io.InputStream;
import java.lang.System.Logger;
import java.util.function.Function;

import mdplayer.Chip;
import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.driver.mucom.MucomDriver;
import mdplayer.format.MUCFileFormat;
import mdplayer.plugin.BasePlugin.Compilable;
import mdsound.MDSound;
import mdsound.instrument.Ym2608Inst;
import mdsound.instrument.Ym2610Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * Mucom88 (PC-8801) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MucomPlugin extends BasePlugin<MucomDriver> implements Compilable {

    private static final Logger logger = getLogger(MucomPlugin.class.getName());

    @Override
    public void compile() {

    }

    @Override
    public void prepare() {
        driverVirtual = new MucomDriver(this);

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0] && !setting.getYM2608Type()[0].getUseEmu()[1]) {
            driverReal = new MucomDriver(this);
        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        if (fileFormat instanceof MUCFileFormat) {
            dataBuf = driverVirtual.compile(dataBuf);
        }
        Class<? extends Chip>[] useChipFromMub = MucomDriver.useChipsFromMub(dataBuf);

        Function<String, InputStream> fn = Ym2608Chip::getOPNARyhthmStream;

        if (useChipFromMub[0] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(Ym2608Chip.class).instrument(0);
            if (chip.instrument instanceof Ym2608Inst ym2608) {
                chip.setVolumes.put("FM", ym2608::setVolume);
                chip.setVolumes.put("SSG", ym2608::setVolume);
                chip.setVolumes.put("RHYTHM", ym2608::setVolume);
                chip.setVolumes.put("ADPCM", ym2608::setVolume);
            }
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
            chip.clock = MucomDriver.opnaBaseClock;
            chip.option = new Object[] {fn};
            put(Ym2608Chip.class, chip);

            chipLED.put("PriOPNA", 1);
            chipRegister.chip(Ym2608Chip.class).clock = MucomDriver.opnaBaseClock;
        }

        if (useChipFromMub[1] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 1;
            chip.instrument = chipRegister.chip(Ym2608Chip.class).instrument(0);
            if (chip.instrument instanceof Ym2608Inst ym2608) {
                chip.setVolumes.put("FM", ym2608::setVolume);
                chip.setVolumes.put("SSG", ym2608::setVolume);
                chip.setVolumes.put("RHYTHM", ym2608::setVolume);
                chip.setVolumes.put("ADPCM", ym2608::setVolume);
            }
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class);
            chip.clock = MucomDriver.opnaBaseClock;
            chip.option = new Object[] {fn};
            put(Ym2608Chip.class, chip);

            chipLED.put("SecOPNA", 1);
        }

        if (useChipFromMub[2] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriOPNB", 1);
            chip.instrument = chipRegister.chip(Ym2610Chip.class).instrument(0);
            chip.samplingRate = 55467;
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Chip.class);
            chip.clock = MucomDriver.opnbBaseClock;
            if (chip.instrument instanceof Ym2610Inst ym2610) {
                chip.setVolumes.put("FM", ym2610::setVolume);
                chip.setVolumes.put("SSG", ym2610::setVolume);
                chip.setVolumes.put("ADPCMA", ym2610::setVolume);
                chip.setVolumes.put("ADPCMB", ym2610::setVolume);
            }
            chip.option = null;
            put(Ym2610Chip.class, chip);
            chipRegister.chip(Ym2610Chip.class).clock = MucomDriver.opnbBaseClock;
        }

        if (useChipFromMub[3] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 1;
            chipLED.put("SecOPNB", 1);
            chip.instrument = chipRegister.chip(Ym2610Chip.class).instrument(1);
            chip.samplingRate = 55467; // (int) setting.getoutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2610Chip.class);
            chip.clock = MucomDriver.opnbBaseClock;
            if (chip.instrument instanceof Ym2610Inst ym2610) {
                chip.setVolumes.put("FM", ym2610::setVolume);
                chip.setVolumes.put("PSG", ym2610::setVolume);
                chip.setVolumes.put("ADPCMA", ym2610::setVolume);
                chip.setVolumes.put("ADPCMB", ym2610::setVolume);
            }
            chip.option = null;
            put(Ym2610Chip.class, chip);
        }

        if (useChipFromMub[4] != Unused.class) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chipLED.put("PriOPM", 1);
            chip.instrument = chipRegister.chip(Ym2151Chip.class).instrument(0);
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
            chip.clock = MucomDriver.opmBaseClock;
            chip.samplingRate = chip.clock / 64;
            chip.option = null;
            put(Ym2151Chip.class, chip);
        }

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        setVolume(MAIN_TAG, Ym2608Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2608Chip.class));
        setVolume("FM", Ym2608Chip.class, true, setting.getBalance().getVolume("FM", Ym2608Chip.class));
        setVolume("SSG", Ym2608Chip.class, true, setting.getBalance().getVolume("SSG", Ym2608Chip.class));
        setVolume("RHYTHM", Ym2608Chip.class, true, setting.getBalance().getVolume("RHYTHM", Ym2608Chip.class));
        setVolume("ADPCM", Ym2608Chip.class, true, setting.getBalance().getVolume("ADPCM", Ym2608Chip.class));

        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x2d, 0x00, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(1, 0, 0x29, 0x82, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.VirtualModel); // Reset with Psg TONE
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x07, 0x38, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x08, 0x00, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x09, 0x00, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, Common.EnmModel.VirtualModel);
        chipRegister.chip(Ym2608Chip.class).write(0, 0, 0x0a, 0x00, Common.EnmModel.RealModel);

        chipRegister.chip(Ym2608Chip.class).writeClock((byte) 0, MucomDriver.opnaBaseClock, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).writeClock((byte) 1, MucomDriver.opnaBaseClock, Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);
        chipRegister.chip(Ym2608Chip.class).setSsgVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), Common.EnmModel.RealModel);


        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }

//        if (driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//            SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
//        }
    }
}
