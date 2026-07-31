package mdplayer.driver.sid;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import mdplayer.Common.EnmModel;
import mdplayer.chips.SidChip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.sid.Sid;
import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidTuneInfo.Model;
import mdplayer.driver.BasePlugin;
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

    public SidMdDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

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
        md.set(Tag.NumberOfSongs, String.valueOf(sid.songs));

        return md;
    }

    /**
     * @param args 0: songNo
     */
    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
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

        byte[] kernelRom = null;
        byte[] basicRom = null;
        byte[] characterRom = null;
        try {
            Path p = Path.of(setting.getSid().romKernalPath);
            if (!Files.isDirectory(p) && Files.exists(p)) {
                try (InputStream fs = Files.newInputStream(p)) {
                    kernelRom = fs.readAllBytes();
                }
            }
            p = Path.of(setting.getSid().romBasicPath);
            if (!Files.isDirectory(p) && Files.exists(p)) {
                try (InputStream fs = Files.newInputStream(p)) {
                    basicRom = fs.readAllBytes();
                }
            }
            p = Path.of(setting.getSid().romCharacterPath);
            if (!Files.isDirectory(p) && Files.exists(p)) {
                try (InputStream fs = Files.newInputStream(p)) {
                    characterRom = fs.readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        sid.init(dataBuf,
                kernelRom, basicRom, characterRom,
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
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private final short[] internalBuffer = new short[4096];
    private int internalProduced = 0;
    private int internalConsumed = 0;

    @Override
    public int render(short[] b, int offset, int length) {
        if (!sid.initial) {
            return length;
        }
        if (frameCounter < 0) {
            frameCounter += length / 2;
            return length;
        }

        plugin.chipRegister.chip(SidChip.class).sid = this;
        //sid.engine.fastForward(100);

        int written = 0;
        while (written < length) {
            if (internalConsumed >= internalProduced) {
                internalProduced = sid.engine.play(internalBuffer, internalBuffer.length);
                internalConsumed = 0;
                if (internalProduced <= 0) break;
            }

            int toCopy = Math.min(length - written, internalProduced - internalConsumed);
            System.arraycopy(internalBuffer, internalConsumed, b, offset + written, toCopy);
            
            for (int i = 0; i < toCopy / 2; i++) {
                processOneFrame();
                fireEventHappened(this, "wave.buffer", b[offset + written + i * 2], b[offset + written + i * 2 + 1]);
            }

            written += toCopy;
            internalConsumed += toCopy;
        }

        return written;
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
logger.log(Level.INFO, "songNo: " + sid.song + " / " + sid.songs);
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

    @Override
    public String getName() {
        return "SID";
    }
}
