package mdplayer.plugin;

import java.lang.System.Logger;

import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.OkiM6258Chip;
import mdplayer.driver.rcp.RcsDriver;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.x68sound.SoundIocs;

import static java.lang.System.getLogger;


/**
 * RCSPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-12 nsano initial version <br>
 */
public class RCSPlugin extends BasePlugin<RcsDriver> {

    private static final Logger logger = getLogger(RCSPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new RcsDriver(this);
        driverVirtual.setExtendFile(extendFiles);

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            driverReal = new RcsDriver(this);
            driverReal.setExtendFile(extendFiles);
        }

        super.prepare();
        initChips();
    }

    private final String[] supportFile = null; // TODO

    @Override
    protected void initChips() {
        if (setting.getRcs().pcm8type == 0) {
            X68kYm2151Inst opmPCM = Instrument.getInstrument(X68kYm2151Inst.class);
            opmPCM.soundIocs[0] = new SoundIocs(opmPCM.chips[0]);
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = opmPCM;
            chip.volume = 0;
            chip.clock = 4_000_000;
            chip.samplingRate = 4_000_000 / 64;
            chip.option = new Object[] { 0, 1, 0 };
            put(OkiM6258Chip.class, chip); // not use mds, via driver direct
        } else {
            Pcm8PPInst pcm8pp = Instrument.getInstrument(Pcm8PPInst.class);
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = pcm8pp;
            chip.volume = 0;
            chip.clock = 4_000_000;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.option = new Object[] {setting.getZMusic().pcm8ppsOption};
            put(OkiM6258Chip.class, chip); // not use mds, via driver direct
        }

        getDriver().fireEventHappened(chipRegister.plugin(MidiPlugin.class), "led.set", 0);
        getDriver().fireEventHappened(chipRegister.plugin(MidiPlugin.class), "led.set", 1);

        chipRegister.plugin(MidiPlugin.class).releaseAll();
        chipRegister.plugin(MidiPlugin.class).make();
        chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(chipRegister.plugin(MidiPlugin.class).midiMode));

        driverVirtual.setSupportFileName((supportFile == null || supportFile.length < 1) ? null : supportFile[0]);
        driverReal.setSupportFileName((supportFile == null || supportFile.length < 1) ? null : supportFile[0]);

        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(Common.EnmModel.RealModel,
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }
}
