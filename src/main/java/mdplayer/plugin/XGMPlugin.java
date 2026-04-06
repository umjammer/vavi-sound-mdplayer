package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Common;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.Xgm2Driver;
import mdplayer.driver.XgmDriver;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.Ym3438Const;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.instrument.Ym3438Inst;

import static java.lang.System.getLogger;
import static mdplayer.driver.Xgm2.checkXGM2;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * XGMPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class XGMPlugin extends BasePlugin<XgmDriver> {

    private static final Logger logger = getLogger(XGMPlugin.class.getName());

    @Override
    public void prepare() {
        if (!checkXGM2(vgmBuf))
            driverVirtual = new XgmDriver();
        else
            driverVirtual = new Xgm2Driver();

        driverReal = null;
//        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new Xgm();
//        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(chipRegister.chip(Ym2612Chip.class).inst(0));
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
        chipRegister.chip(Ym2612Chip.class).clock = 7670454;
        chipLED.put("PriOPN2", 1);
        put(Ym2612Chip.class, chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Sn76489Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class);
        chip.clock = 3579545;
        chip.option = null;
        chipLED.put("PriDCSG", 1);
        put(Sn76489Chip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        setVolume(MAIN_TAG, Ym2612Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class));
        setVolume(MAIN_TAG, Sn76489Chip.class, true, setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class));
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
//        chipRegister.chip(Ym2203Chip.class).setSsgVolume(1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);

        driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ym2612Chip.class, Sn76489Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ym2612Chip.class, Sn76489Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }
}
