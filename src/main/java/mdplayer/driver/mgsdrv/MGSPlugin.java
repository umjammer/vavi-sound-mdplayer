package mdplayer.driver.mgsdrv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.BasePlugin.Compilable;
import mdplayer.lib.mgsc.MgscCompiler;
import mdplayer.lib.mgsdrv.MgsDrv;
import mdsound.MDSound;
import mdsound.instrument.MameAy8910Inst;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MGSDRV (MSX) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class MGSPlugin extends BasePlugin<MgsDriver> implements Compilable {

    private static final Logger logger = getLogger(MGSPlugin.class.getName());

    /**
     * Turns a ".mus" into the ".mgs" the driver plays, when that is what was opened.
     * <p>
     * Everything below here - the chip selection in {@link #initChips}, the driver, the tags -
     * reads the compiled song, so this has to have happened before any of it looks.
     */
    @Override
    public void compile() {
        if (!fileFormat.isMml()) {
            return;
        }
        try {
            dataBuf = new MgscCompiler().compile(dataBuf);
            playingFileName = fileFormat.getCompiledFilename();
        } catch (IOException e) {
            throw new UncheckedIOException("mgsc: " + fileFormat.getCompiledFilename(), e);
        }
    }

    @Override
    public void prepare() {
        compile();

        driverVirtual = new MgsDriver(this);

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
            driverReal = new MgsDriver(this);
        }

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        int i = 0;
        while (dataBuf.length > 1 && i < dataBuf.length - 1 && (dataBuf[i] != 0x1a || dataBuf[i + 1] != 0x00)) {
            i++;
        }
        i += 7;
        int[] trkOffsets = new int[18];
        for (int t = 0; t < trkOffsets.length; t++) {
            trkOffsets[t] = (dataBuf[i + t * 2] & 0xff) + (dataBuf[i + t * 2 + 1] & 0xff) * 0x100;
        }
        boolean useAY = (trkOffsets[0] + trkOffsets[1] + trkOffsets[2] != 0);
        boolean useSCC = (trkOffsets[3] + trkOffsets[4] + trkOffsets[5] + trkOffsets[6] + trkOffsets[7] != 0);
        boolean useOPLL = (trkOffsets[8] + trkOffsets[9] + trkOffsets[10] +
                trkOffsets[11] + trkOffsets[12] + trkOffsets[13] +
                trkOffsets[14] + trkOffsets[15] + trkOffsets[16] +
                trkOffsets[17]
                != 0);
logger.log(Level.INFO, "MGSDRV: AY: %b, SCC: %b, OPLL: %b".formatted(useAY, useSCC, useOPLL));

        if (useAY) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(Ay8910Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ay8910Chip.class);
            chip.clock = MgsDrv.baseClockAY8910 / 2;
            chip.option = null;
            if (chip.instrument instanceof MameAy8910Inst) {
                chip.option = new Object[] {
                        (setting.getAY8910Type()[0].getYM2149mode() ? 0x10 : 0x00), // chip_type 0x10: YM2149, 0x00: AY
                        0x00 // chip_flag
                };
            }
            put(Ay8910Chip.class, chip);
        }

        if (useOPLL) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(Ym2413Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2413Chip.class);
            chip.clock = MgsDrv.baseClockYM2413;
            chip.option = null;
            put(Ym2413Chip.class, chip);
        }

        if (useSCC) {
            MDSound.Chip chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = chipRegister.chip(K051649Chip.class).instrument(0);
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getVolume(MAIN_TAG, K051649Chip.class);
            chip.clock = MgsDrv.baseClockK051649;
            chip.option = null;
            put(K051649Chip.class, chip);
        }

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

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
