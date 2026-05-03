package mdplayer.driver.sid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.driver.sid.libsidplayfp.SidEmu;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;

import static java.lang.System.getLogger;


public class Sid {

    private static final Logger logger = getLogger(Sid.class.getName());

    static final int FCC_PSID = 0x44495350;
    static final int FCC_RSID = 0x44495352;
    public int songs;
    int song = 1;

    playSidFp engine;
    SidTune tune;
    short[] blockBuffer;
    boolean initial = false;

    SidConfig cfg;
    SidTuneInfo tuneInfo;

    void init(byte[] vgmBuf,
              byte[] aryKernel, byte[] aryBasic, byte[] aryCharacter,
              int outputBufferSize, int sampleRate,
              int quality, int c64model, int sidModel, boolean c64modelForce, boolean sidmodelForce) {
        SidEmu.Output.outputBufferSize = outputBufferSize;

        engine = new playSidFp(sampleRate);
        engine.debug(false, null);
        engine.setRoms(aryKernel, aryBasic, aryCharacter);

        ReSidBuilder rs = new ReSidBuilder("ReSid", sampleRate);
        rs.create(engine.info().maxsids());

        tune = new SidTune(vgmBuf, vgmBuf.length);
        tune.selectSong(song);
        tuneInfo = tune.getInfo();

        if (!engine.load(tune)) {
            logger.log(Level.TRACE, "Error: " + engine.error());
            return;
        }

        cfg = new SidConfig(sampleRate);
        cfg.frequency = sampleRate;
        cfg.samplingMethod = (quality & 2) == 0
                ? SidConfig.SamplingMethod.INTERPOLATE
                : SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
        cfg.fastSampling = (quality & 1) == 0;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.defaultC64Model = switch (c64model) {
            case 1 -> SidConfig.C64Model.NTSC;
            case 2 -> SidConfig.C64Model.OLD_NTSC;
            case 3 -> SidConfig.C64Model.DREAN;
            default -> SidConfig.C64Model.PAL;
        };
        cfg.defaultSidModel = sidModel == 1
                ? SidConfig.SidModel.MOS8580
                : SidConfig.SidModel.MOS6581;
        cfg.forceC64Model = c64modelForce;
        cfg.forceSidModel = sidmodelForce;

        cfg.sidEmulation = rs;

        if (!engine.config(cfg)) {
            logger.log(Level.TRACE, "Error: " + engine.error());
        }

        blockBuffer = null;
    }
}
