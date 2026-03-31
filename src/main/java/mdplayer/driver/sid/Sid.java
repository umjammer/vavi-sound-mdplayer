package mdplayer.driver.sid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.SidEmu;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;
import mdsound.VisWaveBuffer;

import static java.lang.System.getLogger;


// TODO eliminate mdplayer packages
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

    void init(byte[] vgmBuf, Setting setting) {
        SidEmu.Output.outputBufferSize = setting.getSid().outputBufferSize;

        byte[] aryKernel = null;
        byte[] aryBasic = null;
        byte[] aryCharacter = null;
        if (File.exists(setting.getSid().romKernalPath)) {
            try (FileStream fs = new FileStream(setting.getSid().romKernalPath, FileMode.Open, FileAccess.Read)) {
                aryKernel = new byte[(int) fs.getLength()];
                fs.read(aryKernel, 0, aryKernel.length);
            }
        }
        if (File.exists(setting.getSid().romBasicPath)) {
            try (FileStream fs = new FileStream(setting.getSid().romBasicPath, FileMode.Open, FileAccess.Read)) {
                aryBasic = new byte[(int) fs.getLength()];
                fs.read(aryBasic, 0, aryBasic.length);
            }
        }
        if (File.exists(setting.getSid().romCharacterPath)) {
            try (FileStream fs = new FileStream(setting.getSid().romCharacterPath, FileMode.Open, FileAccess.Read)) {
                aryCharacter = new byte[(int) fs.getLength()];
                fs.read(aryCharacter, 0, aryCharacter.length);
            }
        }

        int sampleRate = setting.getOutputDevice().getSampleRate();
        engine = new playSidFp(sampleRate);
        engine.debug(false, null);
        engine.setRoms(aryKernel, aryBasic, aryCharacter);

        ReSidBuilder rs = new ReSidBuilder("ReSid", setting);
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
        cfg.samplingMethod = (setting.getSid().quality & 2) == 0
                ? SidConfig.SamplingMethod.INTERPOLATE
                : SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
        cfg.fastSampling = (setting.getSid().quality & 1) == 0;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.defaultC64Model = switch (setting.getSid().c64model) {
            case 1 -> SidConfig.C64Model.NTSC;
            case 2 -> SidConfig.C64Model.OLD_NTSC;
            case 3 -> SidConfig.C64Model.DREAN;
            default -> SidConfig.C64Model.PAL;
        };
        cfg.defaultSidModel = setting.getSid().sidModel == 1
                ? SidConfig.SidModel.MOS8580
                : SidConfig.SidModel.MOS6581;
        cfg.forceC64Model = setting.getSid().c64modelForce;
        cfg.forceSidModel = setting.getSid().sidmodelForce;

        cfg.sidEmulation = rs;

        if (!engine.config(cfg)) {
            logger.log(Level.TRACE, "Error: " + engine.error());
        }

        blockBuffer = null;
    }

    VisWaveBuffer visWB = new VisWaveBuffer();
}
