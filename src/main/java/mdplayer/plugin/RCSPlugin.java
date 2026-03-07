package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip.Unused;
import mdplayer.Common;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.OkiM6258Chip;
import mdplayer.driver.rcp.RCP;
import mdplayer.driver.rcp.RCS;
import mdplayer.driver.zms.Zms;
import mdplayer.format.FileFormat;
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
public class RCSPlugin extends BasePlugin {

    private static final Logger logger = getLogger(RCSPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new RCS();
        ((RCP) audio.driverVirtual).extendFile = extendFiles;
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new RCS();
            ((RCP) audio.driverReal).extendFile = extendFiles;
        }
        prepare();
        boolean r = _play();
        if (!r) {
logger.log(Level.WARNING, "cannot start: " + this);
            return false;
        }
        super.play();
        return true;
    }

    private String[] supportFile = null;

    /** */
    private boolean _play() {
        startTrdVgmReal();

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
            ((Zms) audio.driverVirtual).opmPCM = opmPCM;
            ((Zms) audio.driverVirtual).pcm8type = 0;
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
            ((Zms) audio.driverVirtual).pcm8pp = pcm8pp;
            ((Zms) audio.driverVirtual).pcm8type = 1;
        }

        hiyorimiNecessary = setting.getHiyorimiMode();

        audio.chipLED.put("PriMID", 1);
        audio.chipLED.put("SecMID", 1);
        audio.chipLED.put("PriPCM8", 1);

        audio.chipRegister.plugin(MidiPlugin.class).releaseAll();
        audio.chipRegister.plugin(MidiPlugin.class).make(setting, midiMode);
        audio.chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(midiMode));

        ((RCS) audio.driverVirtual).supportFileName = (supportFile == null || supportFile.length < 1) ? null : supportFile[0];
        ((RCS) audio.driverReal).supportFileName = (supportFile == null || supportFile.length < 1) ? null : supportFile[0];

        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel, new Class[] {Unused.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel, new Class[] {Unused.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        return true;
    }
}
