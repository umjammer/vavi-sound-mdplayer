/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Chip;
import mdplayer.Common.EnmModel;
import mdplayer.chips.NesChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import mdsound.MDSound;
import vavi.util.ByteUtil;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;

import static java.lang.System.getLogger;


/**
 * Nsf2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public class Nsf2 extends BaseDriver implements NsfDriver {

    private static final Logger logger = getLogger(Nsf2.class.getName());

    public Nsf2() {
        sampleRate = setting.getOutputDevice().getSampleRate();
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (ByteUtil.readLeInt(buf, 0) != FCC_NSF) {
            // NSFe is not supported for now
logger.log(Level.WARNING, "NSFe not supported.");
            return null;
        }

        if (buf.length < 0x80) { // no header?
logger.log(Level.WARNING, "no header?");
            return null;
        }

        version = buf[0x05] & 0xff;
        songs = buf[0x06] & 0xff;
        start = buf[0x07] & 0xff;
        load_address = (buf[0x08] & 0xff) | ((buf[0x09] & 0xff) << 8);
        initAddress = (buf[0x0a] & 0xff) | ((buf[0x0B] & 0xff) << 8);
        playAddress = (buf[0x0c] & 0xff) | ((buf[0x0D] & 0xff) << 8);

        List<Byte> strLst = new ArrayList<>();
        int tagAdr = 0x0e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        title_nsf = new String(ByteUtil.toByteArray(strLst), Charset.forName("MS932"));
        title = title_nsf;

        strLst.clear();
        tagAdr = 0x2e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        artist_nsf = new String(ByteUtil.toByteArray(strLst), Charset.forName("MS932"));
        artist = artist_nsf;

        //memcpy(copyright_nsf, image + 0x4e, 32);
        //copyright_nsf[31] = '\0';
        strLst.clear();
        tagAdr = 0x4e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        copyrightNsf = new String(ByteUtil.toByteArray(strLst), Charset.forName("MS932"));
        copyright = copyrightNsf;

        ripper = ""; // NSFe only
        text = ""; // NSFe only
        text_len = 0; // NSFe only
        speedNtsc = (buf[0x6e] & 0xff) | ((buf[0x6f] & 0xff) << 8);
        System.arraycopy(buf, 112, bankSwitch, 0, 8);
        speedPal = (buf[0x78] & 0xff) | ((buf[0x79] & 0xff) << 8);
        palNtsc = buf[0x7a] & 0xff;

        if (speedPal == 0)
            speedPal = 0x4e20;
        if (speedNtsc == 0)
            speedNtsc = 0x411A;

        soundChip = buf[0x7b] & 0xff;

        useVrc6 = (soundChip & 1) != 0;
        useVrc7 = (soundChip & 2) != 0;
        useFds = (soundChip & 4) != 0;
        useMmc5 = (soundChip & 8) != 0;
        useN106 = (soundChip & 16) != 0;
        useFme7 = (soundChip & 32) != 0;
logger.log(Level.INFO, "%s%s%s%s%s%s".formatted(useVrc6 ? "6" : "_", useVrc7 ? "7" : "_", useFds ? "F" : "_", useMmc5 ? "M" : "_", useN106 ? "N" : "_", useFme7 ? "F" : "_"));

        System.arraycopy(buf, 124, extra, 0, 4);

        //delete[] body;
        //body = new UINT8[size - 0x80];
        body = new byte[buf.length - 0x80];
        System.arraycopy(buf, 128, body, 0, buf.length - 0x80);

        bodySize = buf.length - 0x80;

        //song = start - 1;

        Gd3 gd3 = new Gd3();
        gd3.gameName = title;
        gd3.gameNameJ = title;
        gd3.composer = artist;
        gd3.composerJ = artist;
        gd3.trackName = title;
        gd3.trackNameJ = title;
        gd3.systemName = copyright;
        gd3.systemNameJ = copyright;

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

        this.chip = plugin.audio.chipRegister.chip(NesChip.class);

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

        _init(vgmBuf);

        return true;
    }

    private static final int FCC_NSF = 0x4d53454e; // "NESM"

    private int version;
    public int songs;
    private int start;
    private int load_address;
    private int initAddress;
    private int playAddress;
    private String filename;
    // margin 64 chars.
    private String printTitle;
    private String title_nsf;
    private String artist_nsf;
    private String copyrightNsf;
    private String title;
    private String artist;
    private String copyright;
    // NSFe only
    private String ripper;
    // NSFe only
    private String text;
    // NSFe only
    private int text_len;
    private int speedNtsc;
    private byte[] bankSwitch = new byte[8];
    private int speedPal;
    private int palNtsc;
    private int soundChip;
    public boolean useVrc7;
    public boolean useVrc6;
    public boolean useFds;
    public boolean useFme7;
    public boolean useMmc5;
    public boolean useN106;
    private byte[] extra = new byte[4];
    private byte[] body;
    private int bodySize;
    private byte[] nsfeImage;
    public int[] nsfePlst;
    public int nsfePlstSize;
    private static final int NSFE_ENTRIES = 256;
    NsfAudioFactory factory;
    NsfAudio nsf;
    NsfRenderer renderer;
    byte[] frames = new byte[1600];
    int frameSize;
    int framePointer;

    @Override public void setSong(int songNo) {
        song = songNo;
    }

    @Override public boolean useFds() {
        return useFds;
    }

    @Override public boolean useFme7() {
        return useFme7;
    }

    @Override public boolean useMmc5() {
        return useMmc5;
    }

    @Override public boolean useN106() {
        return useN106;
    }

    @Override public boolean useVrc6() {
        return useVrc6;
    }

    @Override public boolean useVrc7() {
        return useVrc7;
    }

    @Override public void setApu(MDSound.Chip chip) {
        cAPU = chip;
    }

    @Override public void setDmc(MDSound.Chip chip) {
        cDMC = chip;
    }

    @Override public void setFds(MDSound.Chip chip) {
        cFDS = chip;
    }

    @Override public void setMmc5(MDSound.Chip chip) {
        cMMC5 = chip;
    }

    @Override public void setN160(MDSound.Chip chip) {
        cN160 = chip;
    }

    @Override public void setVrc6(MDSound.Chip chip) {
        cVRC6 = chip;
    }

    @Override public void setVrc7(MDSound.Chip chip) {
        cVRC7 = chip;
    }

    @Override public void setFme7(MDSound.Chip chip) {
        cFME7 = chip;
    }

    /**
     * Currently selected track number
     */
    public int song;

    private mdsound.MDSound.Chip cAPU = null;
    private mdsound.MDSound.Chip cDMC = null;
    private mdsound.MDSound.Chip cFDS = null;
    private mdsound.MDSound.Chip cMMC5 = null;
    private mdsound.MDSound.Chip cN160 = null;
    private mdsound.MDSound.Chip cVRC6 = null;
    private mdsound.MDSound.Chip cVRC7 = null;
    private mdsound.MDSound.Chip cFME7 = null;

    private NesChip chip;

    private final int sampleRate;

    private void _init(byte[] buf) {
        factory = new NsfAudioFactory();
        nsf = factory.create(buf);

        renderer = new NsfRenderer();
        renderer.ready(nsf, song);
    }

    public void visWaveBufferCopy(short[][] dest) {
        visWB.copy(dest);
    }

    private final mdsound.VisWaveBuffer visWB = new mdsound.VisWaveBuffer();

    // TODO separate from implementation

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }

    @Override
    public void processOneFrame() {
    }

int CC;
static final int INTERVAL = 1024;

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        // TODO loop, silent detector
        short[] b = new short[sampleCount / 2];
        int r = renderer.render(b, offset, sampleCount / 2);
        for (int i = 0; i < r; i++) {
            buffer[i * 2 + 0] = b[i];
            buffer[i * 2 + 1] = b[i];
if (CC++ % INTERVAL == 0) { logger.log(Level.DEBUG, "NSF: %d, %d".formatted(b[i], b[i])); }
        }
        return r * 2;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        visWaveBufferCopy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }
}