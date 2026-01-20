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
    private int blockBufferPtr;
    private int blockBufferValid;
    private static final int BLOCK_SIZE = 4096;
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
//        vstDelta = 0;

        plugin.audio.chipRegister.chip(SidChip.class).sid = this;

        int written = 0;
        while (written < length) {
            if (blockBufferPtr >= blockBufferValid) {
                // Refill
                blockBufferValid = Math.max(0, engine.play(blockBuffer, BLOCK_SIZE));
                blockBufferPtr = 0;
                if (blockBufferValid <= 0) break; // EOF or Error
            }

            int available = blockBufferValid - blockBufferPtr;
            int toCopy = Math.min(length - written, available);
            System.arraycopy(blockBuffer, blockBufferPtr, b, offset + written, toCopy);

            blockBufferPtr += toCopy;
            written += toCopy;
        }

        for (int i = written; i < length; i++) {
            b[offset + i] = 0;
        }
        for (int i = 0; i < length / 2; i++) {
            if (i * 2 + 1 < length) visWB.enq(b[offset + i * 2], b[offset + i * 2 + 1]);
        }

        return length;
    }

    private void init(byte[] vgmBuf) {
        int originalSampleRate = setting.getOutputDevice().getSampleRate();
        setting.getOutputDevice().setSampleRate(44100);

        try {
            engine = new playSidFp(44100);
            engine.setRoms(null, null, null);

            ReSidBuilder rs = new ReSidBuilder("ReSid", setting);
            rs.create(1);

            tune = new SidTune(vgmBuf, vgmBuf.length);
            tune.selectSong(song);
            tuneInfo = tune.getInfo();

            if (!engine.load(tune)) {
                logger.log(Level.TRACE, "Error: " + engine.error());
                return;
            }

            cfg = new SidConfig(44100);
            cfg.frequency = 44100;
            cfg.samplingMethod = SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
            cfg.fastSampling = false;
            cfg.playback = SidConfig.Playback.STEREO;

            cfg.sidEmulation = rs;

            if (!engine.config(cfg)) {
                logger.log(Level.TRACE, "Error: " + engine.error());
            }

            blockBuffer = new short[BLOCK_SIZE];
            blockBufferPtr = 0;
            blockBufferValid = 0;

        } finally {
            setting.getOutputDevice().setSampleRate(originalSampleRate);
        }
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
