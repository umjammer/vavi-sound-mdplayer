package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.mgsdrv.MgsDrv;
import mdplayer.format.FileFormat;
import mdsound.MDSound;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MGSDRV (MSX) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MGSPlugin extends BasePlugin {

    private static final Logger logger = getLogger(MGSPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new MgsDrv();
        ((MgsDrv) audio.driverVirtual).setPlayingFileName(playingFileName);
        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            audio.driverReal = new MgsDrv();
            ((MgsDrv) audio.driverReal).setPlayingFileName(playingFileName);
        }
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
        int i = 0;
        while (vgmBuf.length > 1 && i < vgmBuf.length - 1 && (vgmBuf[i] != 0x1a || vgmBuf[i + 1] != 0x00)) {
            i++;
        }
        i += 7;
        int[] trkOffsets = new int[18];
        for (int t = 0; t < trkOffsets.length; t++) {
            trkOffsets[t] = (vgmBuf[i + t * 2] & 0xff) + (vgmBuf[i + t * 2 + 1] & 0xff) * 0x100;
        }
        boolean useAY = (trkOffsets[0] + trkOffsets[1] + trkOffsets[2] != 0);
        boolean useSCC = (trkOffsets[3] + trkOffsets[4] + trkOffsets[5] + trkOffsets[6] + trkOffsets[7] != 0);
        boolean useOPLL = (trkOffsets[8] + trkOffsets[9] + trkOffsets[10] +
                trkOffsets[11] + trkOffsets[12] + trkOffsets[13] +
                trkOffsets[14] + trkOffsets[15] + trkOffsets[16] +
                trkOffsets[17]
                != 0);

        startTrdVgmReal();

        if (useAY) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriAY10", 1);
            chip.instrument = audio.chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = MgsDrv.baseclockAY8910 / 2;
            chip.option = null;
            put(Ay8910Chip.class, chip);
            audio.chipRegister.chip(Ay8910Chip.class).clock = MgsDrv.baseclockAY8910;
        }

        if (useOPLL) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriOPLL", 1);
            chip.instrument = audio.chipRegister.chip(Ym2413Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
            chip.clock = MgsDrv.baseclockYM2413;
            chip.option = null;
            put(Ym2413Chip.class, chip);
            audio.chipRegister.chip(Ym2413Chip.class).clock = MgsDrv.baseclockYM2413;
        }

        if (useSCC) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            audio.chipLED.put("PriK051649", 1);
            chip.instrument = audio.chipRegister.chip(K051649Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
            chip.clock = MgsDrv.baseclockK051649;
            chip.option = null;
            put(K051649Chip.class, chip);
            audio.chipRegister.chip(K051649Chip.class).clock = MgsDrv.baseclockK051649;
        }

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        if (!audio.driverVirtual.init(vgmBuf, this, Common.EnmModel.VirtualModel,
                new Class[] {Ay8910Chip.class, Ym2413Chip.class, K051649Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, Common.EnmModel.RealModel,
                    new Class[] {Ay8910Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
        }

        return true;
    }
}
