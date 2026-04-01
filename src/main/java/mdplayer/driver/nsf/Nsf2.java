/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.chips.NesChip;
import vavi.util.ByteUtil;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * Nsf2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-02-16 nsano initial version <br>
 */
public class Nsf2 {

    private static final Logger logger = getLogger(Nsf2.class.getName());

    static final int FCC_NSF = 0x4d53454e; // "NESM"

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
    String title;
    String artist;
    String copyright;
    // NSFe only
    private String ripper;
    // NSFe only
    private String text;
    // NSFe only
    private int text_len;
    private int speedNtsc;
    private final byte[] bankSwitch = new byte[8];
    private int speedPal;
    private int palNtsc;
    private int soundChip;
    public boolean useVrc7;
    public boolean useVrc6;
    public boolean useFds;
    public boolean useFme7;
    public boolean useMmc5;
    public boolean useN106;
    private final byte[] extra = new byte[4];
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

    /**
     * Currently selected track number
     */
    public int song;

    mdsound.MDSound.Chip cAPU = null;
    mdsound.MDSound.Chip cDMC = null;
    mdsound.MDSound.Chip cFDS = null;
    mdsound.MDSound.Chip cMMC5 = null;
    mdsound.MDSound.Chip cN160 = null;
    mdsound.MDSound.Chip cVRC6 = null;
    mdsound.MDSound.Chip cVRC7 = null;
    mdsound.MDSound.Chip cFME7 = null;

    NesChip chip;

    int sampleRate;

    void initInfo(byte[] buf) {
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
        title_nsf = new String(ByteUtil.toByteArray(strLst), charset);
        title = title_nsf;

        strLst.clear();
        tagAdr = 0x2e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        artist_nsf = new String(ByteUtil.toByteArray(strLst), charset);
        artist = artist_nsf;

        //memcpy(copyright_nsf, image + 0x4e, 32);
        //copyright_nsf[31] = '\0';
        strLst.clear();
        tagAdr = 0x4e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        copyrightNsf = new String(ByteUtil.toByteArray(strLst), charset);
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
    }

    void init(byte[] buf) {
        factory = new NsfAudioFactory();
        nsf = factory.create(buf);

        renderer = new NsfRenderer();
        renderer.ready(nsf, song);
    }

    public void visWaveBufferCopy(short[][] dest) {
        visWB.copy(dest);
    }

    private final mdsound.VisWaveBuffer visWB = new mdsound.VisWaveBuffer();
}