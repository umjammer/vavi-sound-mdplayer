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
import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo.Model;
import mdplayer.plugin.BasePlugin;
import mdsound.VisWaveBuffer;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * SID
 *
 * @author kumatan
 */
public class SidMdDriver extends BaseDriver implements SidDriver {

    private static final Logger logger = getLogger(SidMdDriver.class.getName());

    private final Sid sid;

    public SidMdDriver() {
        this.sid = new Sid();
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (buf == null) return null;

        if (ByteUtil.readLeInt(buf, 0) != Sid.FCC_PSID && ByteUtil.readLeInt(buf, 0) != Sid.FCC_RSID) {
            return null;
        }

        sid.songs = ByteUtil.readBeShort(buf, 0x0e) & 0xffff;

        MetaData md = new MetaData();
        try {
            md.set(Tag.Title, new String(buf, 0x16, 32, StandardCharsets.US_ASCII).trim());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = md.getFirst(Tag.Title).indexOf((char) 0);
            if (idx != -1) md.set(Tag.Title, md.getFirst(Tag.Title).substring(0, idx));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            md.set(Tag.Composer, new String(buf, 0x36, 32, StandardCharsets.US_ASCII).trim());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = md.getFirst(Tag.Composer).indexOf((char) 0);
            if (idx != -1) md.set(Tag.Composer, md.getFirst(Tag.Composer).substring(0, idx));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            md.set(Tag.Note, new String(buf, 0x56, 32, StandardCharsets.US_ASCII).trim());
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        try {
            int idx = md.getFirst(Tag.Note).indexOf((char) 0);
            if (idx != -1) md.set(Tag.Note, md.getFirst(Tag.Note).substring(0, idx));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return md;
    }

    /**
     * @param args 0: songNo
     */
    @Override
    public void init(byte[] dataBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.dataBuf = dataBuf;
        this.plugin = plugin;
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        if (model == EnmModel.RealModel) {
            stopped = true;
            curLoop = 9999;
            return;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;
        speedCounter = 0;

        metaData = getMetaData(dataBuf);

        setSong((int) args[0]);

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

        sid.init(dataBuf,
                aryKernel, aryBasic, aryCharacter,
                setting.getSid().outputBufferSize,
                setting.getOutputDevice().getSampleRate(),
                setting.getSid().quality,
                setting.getSid().c64model,
                setting.getSid().sidModel,
                setting.getSid().c64modelForce,
                setting.getSid().sidmodelForce
                );
        sid.initial = true;
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;
        try {
            speedCounter += speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                } else {
                    frameCounter++;
                }
            }
            //Stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public int render(short[] b, int offset, int length) {
        if (!sid.initial) {
            return length;
        }
        if (frameCounter < 0) {
            frameCounter += length / 2;
            return length;
        }
//        vstDelta = 0;

        plugin.chipRegister.chip(SidChip.class).sid = this;
        sid.engine.fastForward(100);

        if (sid.blockBuffer == null || sid.blockBuffer.length < length) {
            sid.blockBuffer = new short[length];
        }

        int produced = Math.max(0, sid.engine.play(sid.blockBuffer, length));
        int toCopy = Math.min(length, produced);
        if (toCopy > 0) {
            System.arraycopy(sid.blockBuffer, 0, b, offset, toCopy);
        }
        for (int i = toCopy; i < length; i++) {
            b[offset + i] = 0;
        }
        for (int i = 0; i < length / 2; i++) {
            processOneFrame();
            if (i * 2 + 1 < length) this.visWB.enq(b[offset + i * 2], b[offset + i * 2 + 1]);
        }

        return length;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        this.visWB.copy(dest);
    }

    @Override
    public Integer[][] getRegisterFromSid() {
        if (sid.engine == null) return null;
        return sid.engine.getSidRegister();
    }

    @Override
    public Map<String, Object> getInfo() {
        if (sid.tuneInfo == null) return Map.of();
        Function<Integer, Model> f = i -> sid.tuneInfo.sidModel(i);
        return Map.of(
                "LoadAddr", sid.tuneInfo.loadAddr(),
                "InitAddress", sid.tuneInfo.initAddr(),
                "PlayAddress", sid.tuneInfo.playAddr(),
                "sidModel", f,
                "ClockSpeed", sid.tuneInfo.clockSpeed(),
                "defaultSidModel", sid.cfg.defaultSidModel,
                "SpeedString", sid.engine.info().getSpeedString()
        );
    }

    private void setSong(int songNo) {
        sid.song = songNo;
        if (sid.tune != null) {
            sid.tune.selectSong(sid.song);
            sid.tuneInfo = sid.tune.getInfo();
            if (sid.engine != null) {
                sid.engine.load(sid.tune);
            }
        }
    }

    @Override
    public int getSongs() {
        return sid.songs;
    }

    final VisWaveBuffer visWB = new VisWaveBuffer();
}
