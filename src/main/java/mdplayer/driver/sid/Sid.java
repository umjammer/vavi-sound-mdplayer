package mdplayer.driver.sid;

import java.nio.charset.StandardCharsets;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.sid.libsidplayfp.SidEmu;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;
import mdsound.VisWaveBuffer;


public class Sid extends BaseDriver {

    public static final int FCC_PSID = 0x44495350;
    public static final int FCC_RSID = 0x44495352;
    public int songs;
    public int song;

    private playSidFp engine;
    private boolean initial = false;

    public SidConfig cfg;
    public SidTuneInfo tuneInfo;

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        if (Common.getLE32(buf, 0) != FCC_PSID && Common.getLE32(buf, 0) != FCC_RSID)
            return null;

        songs = Common.getBE16(buf, 0x0e);

        Vgm.Gd3 gd3 = new Vgm.Gd3();
        try {
            gd3.trackName = new String(buf, 0x16, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gd3.trackName = gd3.trackName.substring(0, gd3.trackName.indexOf((char) 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gd3.composer = new String(buf, 0x36, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gd3.composer = gd3.composer.substring(0, gd3.composer.indexOf((char) 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gd3.notes = new String(buf, 0x56, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            gd3.notes = gd3.notes.substring(0, gd3.notes.indexOf((char) 0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        if (model == EnmModel.RealModel) {
            stopped = true;
            vgmCurLoop = 9999;
            return true;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        gd3 = getGD3Info(vgmBuf);

        initSid(vgmBuf);
        initial = true;

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;
        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    counter++;
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !isPlaying();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int render(short[] b, int length) {
        if (!initial) {
            return length;
        }
        if (vgmFrameCounter < 0) {
            vgmFrameCounter += length / 2;
            return length;
        }

        chipRegister.SID = this;
        engine.fastForward(100);
        engine.play(b, length);
        for (int i = 0; i < length / 2; i++) {
            processOneFrame();
            visWB.enq(b[i * 2 + 0], b[i * 2 + 1]);
        }

        return length;
    }

    private void initSid(byte[] vgmBuf) {
        SidEmu.Output.outputBufferSize = setting.getSid().outputBufferSize;

        byte[] aryKernal = null;
        byte[] aryBasic = null;
        byte[] aryCharacter = null;
        if (File.exists(setting.getSid().romKernalPath))
            try (FileStream fs = new FileStream(setting.getSid().romKernalPath, FileMode.Open, FileAccess.Read)) {
                aryKernal = new byte[(int) fs.getLength()];
                fs.read(aryKernal, 0, aryKernal.length);
            }
        if (File.exists(setting.getSid().romBasicPath))
            try (FileStream fs = new FileStream(setting.getSid().romBasicPath, FileMode.Open, FileAccess.Read)) {
                aryBasic = new byte[(int) fs.getLength()];
                fs.read(aryBasic, 0, aryBasic.length);
            }
        if (File.exists(setting.getSid().romCharacterPath))
            try (FileStream fs = new FileStream(setting.getSid().romCharacterPath, FileMode.Open, FileAccess.Read)) {
                aryCharacter = new byte[(int) fs.getLength()];
                fs.read(aryCharacter, 0, aryCharacter.length);
            }

        engine = new playSidFp(setting);
        engine.debug(false, null);
        engine.setRoms(aryKernal, aryBasic, aryCharacter);

        ReSidBuilder rs = new ReSidBuilder("ReSid", setting);

        int maxSids = (engine.info()).maxsids();
        rs.create(maxSids);

        SidTune tune = new SidTune(vgmBuf, vgmBuf.length);
        tune.selectSong(song);

        if (!engine.load(tune)) {
            System.err.println("Error: " + engine.error());
            return;
        }

        // Get tune details
        tuneInfo = tune.getInfo();
        //if (!m_track.single)
        //    m_track.songs = (short)tuneInfo.songs();
        //if (!createOutput(m_driver.Output, tuneInfo))
        //    return false;
        //if (!createSidEmu(m_driver.Sid))
        //    return false;

        cfg = new SidConfig(setting);
        cfg.frequency = setting.getOutputDevice().getSampleRate();
        cfg.samplingMethod = (setting.getSid().quality & 2) == 0 ? SidConfig.SamplingMethod.INTERPOLATE : SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
        cfg.fastSampling = (setting.getSid().quality & 1) == 0;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.defaultC64Model = setting.getSid().c64model == 0 ? SidConfig.C64Model.PAL : (
                setting.getSid().c64model == 1 ? SidConfig.C64Model.NTSC : (
                        setting.getSid().c64model == 2 ? SidConfig.C64Model.OLD_NTSC : (
                                setting.getSid().c64model == 3 ? SidConfig.C64Model.DREAN : SidConfig.C64Model.PAL)));
        cfg.defaultSidModel = setting.getSid().sidModel == 0 ? SidConfig.SidModel.MOS6581 : (
                setting.getSid().sidModel == 1 ? SidConfig.SidModel.MOS8580 : SidConfig.SidModel.MOS6581);
        cfg.forceC64Model = setting.getSid().c64modelForce; // 強制的に defaultC64Model を使用するか
        cfg.forceSidModel = setting.getSid().sidmodelForce; // 強制的に defaultSidModel を使用するか

        cfg.sidEmulation = rs;

        if (!engine.config(cfg)) {
            System.err.println("Error: " + engine.error());
        }
    }

    public void visWaveBufferCopy(short[][] dest) {
        visWB.copy(dest);
    }

    VisWaveBuffer visWB = new VisWaveBuffer();

    public Integer[][] GetRegisterFromSid() {
        if (engine == null) return null;
        return engine.getSidRegister();
    }

    public playSidFp GetCurrentEngineContext() {
        if (engine == null) return null;
        return engine;
    }
}
