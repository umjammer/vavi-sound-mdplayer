package mdplayer.driver.sid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.sid.libsidplayfp.SidEmu;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo.Model;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;
import mdplayer.plugin.BasePlugin;
import mdsound.VisWaveBuffer;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class Sid extends BaseDriver implements SidDriver {

    private static final Logger logger = getLogger(Sid.class.getName());

    private static final int FCC_PSID = 0x44495350;
    private static final int FCC_RSID = 0x44495352;
    public int songs;
    private int song = 1;

    private playSidFp engine;
    private SidTune tune;
    private short[] blockBuffer;
    private boolean initial = false;

    private SidConfig cfg;
    private SidTuneInfo tuneInfo;

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        if (ByteUtil.readLeInt(buf, 0) != FCC_PSID && ByteUtil.readLeInt(buf, 0) != FCC_RSID) {
            return null;
        }

        songs = Common.getBE16(buf, 0x0e);

        Vgm.Gd3 gd3 = new Vgm.Gd3();
        try {
            gd3.trackName = new String(buf, 0x16, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.trackName.indexOf((char) 0);
            if (idx != -1) gd3.trackName = gd3.trackName.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            gd3.composer = new String(buf, 0x36, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.composer.indexOf((char) 0);
            if (idx != -1) gd3.composer = gd3.composer.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            gd3.notes = new String(buf, 0x56, 32, StandardCharsets.US_ASCII).trim();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = gd3.notes.indexOf((char) 0);
            if (idx != -1) gd3.notes = gd3.notes.substring(0, idx);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
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

        init(vgmBuf);
        initial = true;

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public int render(short[] b, int offset, int length) {
        if (!initial) {
            return length;
        }
        if (vgmFrameCounter < 0) {
            vgmFrameCounter += length / 2;
            return length;
        }
//        vstDelta = 0;

        plugin.audio.chipRegister.chip(SidChip.class).sid = this;
        engine.fastForward(100);

        if (blockBuffer == null || blockBuffer.length < length) {
            blockBuffer = new short[length];
        }

        int produced = Math.max(0, engine.play(blockBuffer, length));
        int toCopy = Math.min(length, produced);
        if (toCopy > 0) {
            System.arraycopy(blockBuffer, 0, b, offset, toCopy);
        }
        for (int i = toCopy; i < length; i++) {
            b[offset + i] = 0;
        }
        for (int i = 0; i < length / 2; i++) {
            processOneFrame();
            if (i * 2 + 1 < length) visWB.enq(b[offset + i * 2], b[offset + i * 2 + 1]);
        }

        return length;
    }

    private void init(byte[] vgmBuf) {
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

    @Override
    public Integer[][] getRegisterFromSid() {
        if (engine == null) return null;
        return engine.getSidRegister();
    }

    @Override
    public Map<String, Object> getInfo() {
        if (tuneInfo == null) return Map.of();
        Function<Integer, Model> f = i -> tuneInfo.sidModel(i);
        return Map.of(
            "LoadAddr", tuneInfo.loadAddr(),
            "InitAddress", tuneInfo.initAddr(),
            "PlayAddress", tuneInfo.playAddr(),
            "sidModel", f,
            "ClockSpeed", tuneInfo.clockSpeed(),
            "defaultSidModel", cfg.defaultSidModel,
            "SpeedString", engine.info().getSpeedString()
        );
    }

    @Override
    public void setSong(int songNo) {
        this.song = songNo;
        if (tune != null) {
            tune.selectSong(song);
            tuneInfo = tune.getInfo();
            if (engine != null) {
                engine.load(tune);
            }
        }
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        visWB.copy(dest);
    }
}
