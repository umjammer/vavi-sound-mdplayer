package mdplayer.driver.mid;

import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BasePlugin;


/**
 * MIDPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MIDPlugin extends BasePlugin<MidiDriver> {

    @Override
    public void prepare() {
        driverVirtual = new MidiDriver(this);

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            driverReal = new MidiDriver(this);
        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        getDriver().fireEventHappened(chipRegister.plugin(MidiPlugin.class), "led.set", 0);
        getDriver().fireEventHappened(chipRegister.plugin(MidiPlugin.class), "led.set", 1);

        chipRegister.plugin(MidiPlugin.class).releaseAll();
        chipRegister.plugin(MidiPlugin.class).make();
        chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(chipRegister.plugin(MidiPlugin.class).midiMode));

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
