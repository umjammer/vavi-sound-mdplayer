package mdplayer.driver.sid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo.Model;
import mdplayer.plugin.BasePlugin;
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
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        if (ByteUtil.readLeInt(buf, 0) != Sid.FCC_PSID && ByteUtil.readLeInt(buf, 0) != Sid.FCC_RSID) {
            return null;
        }

        sid.songs = Common.getBE16(buf, 0x0e);

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
    public void init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        if (model == EnmModel.RealModel) {
            stopped = true;
            vgmCurLoop = 9999;
            return;
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

        sid.init(vgmBuf, setting);
        sid.initial = true;
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
        if (!sid.initial) {
            return length;
        }
        if (vgmFrameCounter < 0) {
            vgmFrameCounter += length / 2;
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
            if (i * 2 + 1 < length) sid.visWB.enq(b[offset + i * 2], b[offset + i * 2 + 1]);
        }

        return length;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        sid.visWB.copy(dest);
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

    @Override
    public void setSong(int songNo) {
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
}
