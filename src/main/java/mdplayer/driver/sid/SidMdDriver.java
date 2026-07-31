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
import mdsound.np.LoopDetector;
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

        ld.reset();
        timeInMs = 0;
        playtimeDetected = false;
        silentLength = 0;
        lastOut = 0;
        writes = 0;
        lastWrites = 0;
        lastWriteMs = 0;

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
        sid.engine.setSidWriteListener((addr, data) -> ld.write(addr, data, 0));
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

    /**
     * What tells a song that came around again from one that goes on: a Sid tune has no end mark
     * and its play routine is simply called forever, so the only thing left to watch is the
     * register writes repeating, the way the NSF and HES drivers do it.
     * <p>
     * 2^20 writes is about 10 minutes of a tune writing its registers 30 times a frame - long
     * enough that a signature of a loop still sits in the buffer when the loop comes back around.
     */
    private final LoopDetector.BasicDetector ld = new LoopDetector.BasicDetector(20) {
        @Override
        public boolean write(int adr, int val, int id) {
            // 0x19-0x1c are the read-only ones (paddles, osc3/env3) and 0x1d-0x1f are unused:
            // a tune has no reason to write them, and letting them in would only be noise
            if (adr > 0x18) return false;
            writes++;
            return super.write(adr, val, id);
        }
    };

    /**
     * A loop this short is the play routine ticking over on itself - a tune whose music has run
     * out but that keeps rewriting the same registers every frame - not a musical loop.
     */
    private static final int minLoopMs = 5_000;
    /** how long the chip may go unwritten before the tune counts as over */
    private static final int idleMs = 10_000;
    /** how long the output may stay unchanged before the tune counts as over */
    private static final int silenceMs = 10_000;

    /** how far into the song the rendering has got, what {@link #ld} times its writes by */
    private double timeInMs;
    /** whether the end is known - either the loop was found or the song is already over */
    private boolean playtimeDetected;
    /** how many samples in a row came out the same, which is how a song that just stops is noticed */
    private int silentLength;
    private int lastOut;
    /** how many writes the tune has made, and where it stood the last time that was looked at */
    private int writes;
    private int lastWrites;
    private double lastWriteMs;

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
                short l = b[offset + written + i * 2], r = b[offset + written + i * 2 + 1];
                fireEventHappened(this, "wave.buffer", l, r);

                int m = l + r;
                if (m == lastOut) silentLength++;
                else silentLength = 0;
                lastOut = m;
            }

            written += toCopy;
            internalConsumed += toCopy;
        }

        detectEnd(written / 2);

        return written;
    }

    /**
     * Works out how far through the song the playback is, which nothing but the rendering itself
     * can tell for a Sid: {@link #curLoop} is what the player watches to fade a song out, and it
     * only means something once the loop has been detected and its length is known.
     *
     * @param samples stereo samples rendered by this call
     */
    private void detectEnd(int samples) {
        int sampleRate = setting.getOutputDevice().getSampleRate();
        timeInMs += 1000.0 * samples / sampleRate * speed;

        if (playtimeDetected) {
            if (totalCounter != 0) curLoop = (int) (counter / totalCounter);
            return;
        }
        curLoop = 0;

        // a tune whose music has run out stops writing the chip altogether - a dead tune, one the
        // emulation never got going, looks the same and is just as over
        if (writes != lastWrites) {
            lastWrites = writes;
            lastWriteMs = timeInMs;
        } else if (timeInMs - lastWriteMs > idleMs) {
logger.log(Level.DEBUG, "end: nothing written for %dms".formatted(idleMs));
            over();
            return;
        }

        // ... and one that keeps writing but has nothing left to say goes flat. this is deliberately
        // long: tunes take rests, and Sid output sits at an exact value while they do
        if (silentLength > (long) sampleRate * silenceMs / 1000) {
logger.log(Level.DEBUG, "end: silent for %dms".formatted(silenceMs));
            over();
            return;
        }

        if (ld.isLooped((int) timeInMs, 30000, 5000)) {
            int start = ld.getLoopStart(), end = ld.getLoopEnd();
logger.log(Level.DEBUG, "loop: %d - %d ms".formatted(start, end));
            if (end - start < minLoopMs) {
                // the play routine repeating itself frame by frame: the song is over, not looping
                over();
                return;
            }
            playtimeDetected = true;
            totalCounter = (long) end * sampleRate / 1000L;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = ((long) end - (long) start) * sampleRate / 1000L;
            return;
        }

        // nothing above can tell when a tune that never repeats itself exactly is done
        int maxPlayTime = setting.getSid().maxPlayTime;
        if (maxPlayTime > 0 && timeInMs > maxPlayTime * 1000L) {
logger.log(Level.DEBUG, "end: gave up after %ds".formatted(maxPlayTime));
            over();
        }
    }

    /** Ends the song: the player fades it out from here and moves on to the next one. */
    private void over() {
        playtimeDetected = true;
        loopCounter = 0;
        stopped = true;
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
