/*
 * ST-Sound ( YM files player library )
 *
 * Copyright (C) 1995-1999 Arnaud Carre ( http://leonard.oxg.free.fr )
*
 * This file is part of ST-Sound
 *
 * ST-Sound is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ST-Sound is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ST-Sound; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mdplayer.driver.ym;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import jp.gr.java_conf.dangan.util.lha.LzssInputStream;
import jp.gr.java_conf.dangan.util.lha.PreLh5Decoder;

import static mdplayer.driver.ym.DigiDrum.MAX_DIGIDRUM;
import static mdplayer.driver.ym.DigiDrum.sampleAddress;
import static mdplayer.driver.ym.DigiDrum.sampleLen;


/** YM Music Driver */
public class YmMusic {

    public static class YmMusicInfo {

        public String pSongName;
        public String pSongAuthor;
        public String pSongComment;
        public String pSongType;
        public String pSongPlayer;
        public int musicTimeInSec;
        public int musicTimeInMs;
    }

    public interface Ym2149Ex {

        void setClock(int clock);

        void reset();

        void writeRegister(int reg, int value);

        int readRegister(int reg);

        void update(short[] buffer, int length);

        void sidStart(int voice, int freq, int volume);

        void sidSinStart(int voice, int freq, int volume);

        void sidStop(int voice);

        void drumStart(int voice, byte[] data, int size, int freq);

        void syncBuzzerStart(int freq, int volume);

        void syncBuzzerStop();
    }

    //
    // Constants from YmMusic.h / YmTypes.h
    //
    public static final int YMTPREC = 16;
    public static final int MAX_VOICE = 8;
    public static final int PC_DAC_FREQ = 44100;
    public static final int YMTNBSRATE = PC_DAC_FREQ / 50;

    public enum YmFileType {
        YM_V2(0), YM_V3(1), YM_V4(2), YM_V5(3), YM_V6(4), YM_VMAX(5),
        YM_TRACKER1(32), YM_TRACKER2(33), YM_TRACKERMAX(34),
        YM_MIX1(64), YM_MIX2(65), YM_MIXMAX(66);
        final int v;

        YmFileType(int v) {
            this.v = v;
        }
    }

    public static final int A_STREAMINTERLEAVED = 1;
    public static final int A_DRUMSIGNED = 2;
    public static final int A_DRUM4BITS = 4;
    public static final int A_TIMECONTROL = 8;
    public static final int A_LOOPMODE = 16;

    private static final int[] MFP_PREDIV = {0, 4, 10, 16, 50, 64, 100, 200};

    private static final int[] YM_VOLUME_TABLE = {
            62, 161, 265, 377, 580, 774, 1155, 1575,
            2260, 3088, 4570, 6233, 9330, 13187, 21220, 32767
    };

    private static final int ATARI_CLOCK = 2000000;
    private static final int MFP_CLOCK = 2457600;

    //
    // Nested structs (previously C structs)
    //
    public static class MixBlock {

        public int sampleStart;
        public int sampleLength;
        public int nbRepeat;
        public int replayFreq;
    }

    public static class DigiDrum {

        public int size;
        public byte[] pData;
        public int repLen;
    }

    public static class YmTrackerLine {

        public int noteOn;
        public int volume;
        public int freqHigh;
        public int freqLow;
    }

    public static class YmTrackerVoice {

        public byte[] pSample;
        public int sampleSize;
        public int samplePos;
        public int repLen;
        public int sampleVolume;
        public int sampleFreq;
        public boolean bLoop;
        public boolean bRunning;
    }

    //
    // Fields
    //
    public Ym2149Ex ymChip;
    private YmFileType songType;
    private int nbFrame;
    private int loopFrame;
    private int currentFrame;
    private int nbDrum;
    private DigiDrum[] drumTab;
    private int musicTime;
    private byte[] bigMalloc;
    private int dataStreamOffset;
    private boolean loop;
    private int fileSize;
    private int playerRate;
    private int attrib;
    private volatile boolean musicOk;
    private volatile boolean pause;
    private int streamInc;
    private int innerSamplePos;
    private final int replayRate;

    private String songName;
    private String songAuthor;
    private String songComment;
    private String songTypeStr;
    private String songPlayer;

    // MIX1
    private int nbRepeat;
    private int nbMixBlock;
    private MixBlock[] mixBlock;
    private int mixPos;
    private byte[] bigSampleBuffer;
    private byte[] currentMixSample;
    private int currentMixSampleOffset;
    private long currentSampleLength;
    private long currentPente;
    private long currentPos;

    // Tracker
    private int nbVoice;
    private final YmTrackerVoice[] trackerVoice = new YmTrackerVoice[MAX_VOICE];
    private int trackerNbSampleBefore;
    private final short[] trackerVolumeTable = new short[256 * 64];
    private int trackerFreqShift;

    public boolean bMusicOver;

    //
    // Construction / destruction
    //
    public YmMusic() {
        this(44100);
    }

    public YmMusic(int _replayRate) {
        this.replayRate = _replayRate;
        this.innerSamplePos = 0;
        this.nbDrum = 0;
        this.drumTab = null;
        setLoopMode(false);
        for (int i = 0; i < MAX_VOICE; i++) {
            trackerVoice[i] = new YmTrackerVoice();
        }
    }

    public void unLoad() {
        musicOk = false;
        pause = true;
        bMusicOver = false;
        songName = null;
        songAuthor = null;
        songComment = null;
        songTypeStr = null;
        songPlayer = null;
        bigMalloc = null;
        dataStreamOffset = 0;
        if (nbDrum > 0) {
            for (int i = 0; i < nbDrum; i++) {
                drumTab[i].pData = null;
            }
            nbDrum = 0;
            drumTab = null;
        }
        bigSampleBuffer = null;
        mixBlock = null;
    }

    public void stop() {
        pause = true;
        currentFrame = 0;
        mixPos = -1;
    }

    public void play() {
        pause = false;
    }

    public void pause() {
        pause = true;
    }

    //
    // Helpers
    //

    private static void signSample(byte[] data, int offset, int size) {
        for (int i = 0; i < size; i++) {
            data[offset + i] ^= (byte) 0x80;
        }
    }

    private static void bufferClear(short[] buffer, int nbSample) {
        Arrays.fill(buffer, 0, nbSample, (short) 0);
    }

    private static int readBigEndian32(byte[] data, int offset) {
        if (data.length < offset + 4) return 0;
        return Math.toIntExact(((data[offset] & 0xFFL) << 24)
                | ((data[offset + 1] & 0xFFL) << 16)
                | ((data[offset + 2] & 0xFFL) << 8)
                | (data[offset + 3] & 0xFFL));
    }

    private static int readLittleEndian32(byte[] data, int offset) {
        if (data.length < offset + 4) return 0;
        return Math.toIntExact(((data[offset] & 0xFFL) << 0)
                | ((data[offset + 1] & 0xFFL) << 8)
                | ((data[offset + 2] & 0xFFL) << 16)
                | ((data[offset + 3] & 0xFFL) << 24));
    }

    //
    // Parsing helpers (replacement for readMotorolaDword/Word/NtString)
    //
    private static class ParseContext {

        byte[] data;
        int offset;
        int remaining;

        ParseContext(byte[] data, int offset, int remaining) {
            this.data = data;
            this.offset = offset;
            this.remaining = remaining;
        }

        int readMotorolaDword() {
            if (remaining < 4) {
                remaining -= 4;
                return 0;
            }
            long v = ((data[offset] & 0xFFL) << 24)
                    | ((data[offset + 1] & 0xFFL) << 16)
                    | ((data[offset + 2] & 0xFFL) << 8)
                    | (data[offset + 3] & 0xFFL);
            offset += 4;
            remaining -= 4;
            return (int) v;
        }

        int readMotorolaWord() {
            if (remaining < 2) {
                remaining -= 2;
                return 0;
            }
            int v = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
            offset += 2;
            remaining -= 2;
            return v;
        }

        String readNtString() {
            if (remaining <= 0) {
                remaining--;
                return "";
            }
            int start = offset;
            int len = 0;
            while (remaining > 0 && data[offset] != 0) {
                offset++;
                remaining--;
                len++;
            }
            if (remaining < 0) {
                return "";
            }
            String s = new String(data, start, len, StandardCharsets.ISO_8859_1);
            if (remaining > 0) {
                offset++; // skip null
                remaining--;
            }
            return s;
        }

        void skip(int n) {
            offset += n;
            remaining -= n;
        }
    }

    //
    // File loading & depacking
    //

    public void load(String fileName) throws IOException {
        stop();
        unLoad();

        byte[] raw = Files.readAllBytes(Paths.get(fileName));

        fileSize = raw.length;
        bigMalloc = raw;

        bigMalloc = depackFile();

        ymDecode();

        ymChip.reset();
        musicOk = true;
        pause = false;
    }

    public void loadMemory(byte[] pBlock, int size) throws IOException {
        stop();
        unLoad();

        fileSize = size;
        bigMalloc = new byte[size];
        System.arraycopy(pBlock, 0, bigMalloc, 0, size);

        bigMalloc = depackFile();

        ymDecode();

        ymChip.reset();
        musicOk = true;
        pause = false;
    }

    private byte[] depackFile() throws IOException {
        if (fileSize < 22) {
            return bigMalloc;
        }

        int size = bigMalloc[0] & 0xFF;
        String id = new String(bigMalloc, 2, 5, StandardCharsets.US_ASCII);
        int level = bigMalloc[20] & 0xFF;
        int nameLength = bigMalloc[21] & 0xFF;

        if (size == 0 || !id.equals("-lh5-")) {
            return bigMalloc; // not compressed
        }

        if (level != 0) {
            throw new IllegalArgumentException("LHARC Header must be 0 !");
        }

        // lzhHeader_t: size(0) sum(1) id[5](2) packed(7) original(11) reserved[5](15) level(20) name_lenght(21)
        int packedSize = readLittleEndian32(bigMalloc, 7);
        int originalSize = readLittleEndian32(bigMalloc, 11);

        int srcOffset = 22 + nameLength + 2; // header + name + CRC16
        int ptrLeft = fileSize - srcOffset;

        if (packedSize > ptrLeft) {
            throw new IllegalArgumentException("File too small");
        }

        byte[] pNew = new byte[originalSize];

        // LZH depacking stub — replace with real depacker
        lzhUnpack(bigMalloc, srcOffset, packedSize, pNew, originalSize);

        fileSize = originalSize;
        return pNew;
    }

    private void lzhUnpack(byte[] bigMalloc, int srcOffset, int packedSize, byte[] pNew, int originalSize) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bigMalloc, srcOffset, packedSize);
        LzssInputStream lzssis = new LzssInputStream(new PreLh5Decoder(bais));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lzssis.transferTo(baos);
        lzssis.close();
        System.arraycopy(baos.toByteArray(), 0, pNew, 0, baos.size());
        assert baos.size() == originalSize;
    }

    //
    // De-interleave
    //

    private boolean deInterleave() {
        if ((attrib & A_STREAMINTERLEAVED) != 0) {
            int bufSize = nbFrame * streamInc;
            byte[] tmpBuff = new byte[bufSize];
            int[] nextPlane = new int[streamInc];
            for (int j = 0; j < streamInc; j++) {
                nextPlane[j] = nbFrame * j;
            }
            int pW = 0;
            for (int j = 0; j < nextPlane[1]; j++) {
                for (int k = 0; k < streamInc; k++) {
                    tmpBuff[pW + k] = bigMalloc[dataStreamOffset + j + nextPlane[k]];
                }
                pW += streamInc;
            }
            bigMalloc = tmpBuff;
            dataStreamOffset = 0;
            attrib &= ~A_STREAMINTERLEAVED;
        }
        return true;
    }

    //
    // YM Decode
    //

    private void ymDecode() {
        if (fileSize < 4) {
            throw new IllegalArgumentException("File too small");
        }

        long id = readBigEndian32(bigMalloc, 0);
        ParseContext ctx = new ParseContext(bigMalloc, 0, fileSize);

        switch ((int) id) {
            case 0x594d3221: // 'YM2!'
                songType = YmFileType.YM_V2;
                nbFrame = (fileSize - 4) / 14;
                if (nbFrame == 0) {
                    throw new IllegalArgumentException("No frames in file");
                }
                loopFrame = 0;
                ymChip.setClock(ATARI_CLOCK);
                setPlayerRate(50);
                dataStreamOffset = 4;
                streamInc = 14;
                nbDrum = 0;
                setAttrib(A_STREAMINTERLEAVED | A_TIMECONTROL);
                songName = "Unknown";
                songAuthor = "Unknown";
                songComment = "Converted by Leonard.";
                songTypeStr = "YM 2";
                songPlayer = "YM-Chip driver.";
                break;

            case 0x594d3321: // 'YM3!'
                songType = YmFileType.YM_V3;
                nbFrame = (fileSize - 4) / 14;
                if (nbFrame == 0) {
                    throw new IllegalArgumentException("No frames in file");
                }
                loopFrame = 0;
                ymChip.setClock(ATARI_CLOCK);
                setPlayerRate(50);
                dataStreamOffset = 4;
                streamInc = 14;
                nbDrum = 0;
                setAttrib(A_STREAMINTERLEAVED | A_TIMECONTROL);
                songName = "Unknown";
                songAuthor = "Unknown";
                songComment = "";
                songTypeStr = "YM 3";
                songPlayer = "YM-Chip driver.";
                break;

            case 0x594d3362: // 'YM3b'
                if (fileSize < 4) {
                    throw new IllegalArgumentException("File too small");
                }
                songType = YmFileType.YM_V3;
                nbFrame = (fileSize - 8) / 14;
                if (nbFrame == 0) {
                    throw new IllegalArgumentException("No frames in file");
                }
                loopFrame = readLittleEndian32(bigMalloc, fileSize - 4);
                ymChip.setClock(ATARI_CLOCK);
                setPlayerRate(50);
                dataStreamOffset = 4;
                streamInc = 14;
                nbDrum = 0;
                setAttrib(A_STREAMINTERLEAVED | A_TIMECONTROL);
                songName = "Unknown";
                songAuthor = "Unknown";
                songComment = "";
                songTypeStr = "YM 3b (loop)";
                songPlayer = "YM-Chip driver.";
                break;

            case 0x594d3421: // 'YM4!'
                throw new IllegalArgumentException("No more YM4! support. Use YM5! format.");

            case 0x594d3521: // 'YM5!'
            case 0x594d3621: // 'YM6!'
                if (fileSize < 12) {
                    throw new IllegalArgumentException("File too small");
                }
                String check = new String(bigMalloc, 4, 8, StandardCharsets.US_ASCII);
                if (!check.equals("LeOnArD!")) {
                    throw new IllegalArgumentException("Not a valid YM format !");
                }
                ctx.offset = 12;
                ctx.remaining = fileSize - 12;
                nbFrame = ctx.readMotorolaDword();
                setAttrib(ctx.readMotorolaDword());
                nbDrum = ctx.readMotorolaWord();
                ymChip.setClock(ctx.readMotorolaDword());
                setPlayerRate(ctx.readMotorolaWord());
                loopFrame = ctx.readMotorolaDword();
                int skip = ctx.readMotorolaWord();
                ctx.skip(skip);
                if (ctx.remaining <= 0) {
                    throw new IllegalArgumentException("File too small");
                }
                if (nbDrum > 0) {
                    drumTab = new YmMusic.DigiDrum[nbDrum];
                    for (int i = 0; i < nbDrum; i++) {
                        drumTab[i] = new YmMusic.DigiDrum();
                        drumTab[i].size = ctx.readMotorolaDword();
                        if (ctx.remaining <= 0) {
                            throw new IllegalArgumentException("File too small");
                        }
                        if (drumTab[i].size != 0) {
                            if ((drumTab[i].size & 0xffff_ffffL) >= 0x8000_0000L) {
                                throw new IllegalArgumentException("To big drumtab");
                            }
                            if (ctx.remaining < drumTab[i].size) {
                                throw new IllegalArgumentException("File too small");
                            }
                            drumTab[i].pData = new byte[drumTab[i].size];
                            System.arraycopy(ctx.data, ctx.offset, drumTab[i].pData, 0, drumTab[i].size);
                            if ((attrib & A_DRUM4BITS) != 0) {
                                byte[] pw = drumTab[i].pData;
                                for (int j = 0; j < drumTab[i].size; j++) {
                                    pw[j] = (byte) (YM_VOLUME_TABLE[pw[j] & 15] >> 7);
                                }
                            }
                            ctx.skip(drumTab[i].size);
                        }
                    }
                    attrib &= ~A_DRUM4BITS;
                }
                songName = ctx.readNtString();
                songAuthor = ctx.readNtString();
                songComment = ctx.readNtString();
                if (ctx.remaining <= 0) {
                    throw new IllegalArgumentException("File too small");
                }
                songType = (id == 0x594d3621) ? YmFileType.YM_V6 : YmFileType.YM_V5;
                songTypeStr = (id == 0x594d3621) ? "YM 6" : "YM 5";
                if (nbFrame >= 0x08000000 || nbFrame < 0) {
                    throw new IllegalArgumentException("Too many frames");
                }
                if (ctx.remaining < (long) nbFrame * 16) {
                    throw new IllegalArgumentException("File too small");
                }
                dataStreamOffset = ctx.offset;
                streamInc = 16;
                setAttrib(A_STREAMINTERLEAVED | A_TIMECONTROL);
                songPlayer = "YM-Chip driver.";
                break;

            case 0x4d495831: // 'MIX1'
                if (fileSize < 12) {
                    throw new IllegalArgumentException("File too small");
                }
                check = new String(bigMalloc, 4, 8, StandardCharsets.US_ASCII);
                if (!check.equals("LeOnArD!")) {
                    throw new IllegalArgumentException("Not a valid YM format !");
                }
                ctx.offset = 12;
                ctx.remaining = fileSize - 12;
                songType = YmFileType.YM_MIX1;
                long tmp = ctx.readMotorolaDword();
                setAttrib(0);
                if ((tmp & 1) != 0) setAttrib(A_DRUMSIGNED);
                int sampleSize = ctx.readMotorolaDword();
                nbMixBlock = ctx.readMotorolaDword();
                if (ctx.remaining <= 0) {
                    throw new IllegalArgumentException("File too small");
                }
                if (sampleSize <= 0) {
                    throw new IllegalArgumentException("Invalid sampleSize");
                }
                if (nbMixBlock <= 0) {
                    throw new IllegalArgumentException("Invalid number of mixblocks");
                }
                mixBlock = new YmMusic.MixBlock[nbMixBlock];
                for (int i = 0; i < nbMixBlock; i++) {
                    mixBlock[i] = new YmMusic.MixBlock();
                    mixBlock[i].sampleStart = ctx.readMotorolaDword();
                    mixBlock[i].sampleLength = ctx.readMotorolaDword();
                    mixBlock[i].nbRepeat = ctx.readMotorolaWord();
                    mixBlock[i].replayFreq = ctx.readMotorolaWord();
                }
                songName = ctx.readNtString();
                songAuthor = ctx.readNtString();
                songComment = ctx.readNtString();

                if ((sampleSize & 0xffff_ffffL) >= 0x8000_0000L) {
                    throw new IllegalArgumentException("Invalid sampleSize");
                }
                if (ctx.remaining < sampleSize) {
                    throw new IllegalArgumentException("File too small");
                }

                bigSampleBuffer = new byte[(int) sampleSize];
                System.arraycopy(ctx.data, ctx.offset, bigSampleBuffer, 0, sampleSize);

                if ((attrib & A_DRUMSIGNED) == 0) {
                    signSample(bigSampleBuffer, 0, sampleSize);
                    setAttrib(A_DRUMSIGNED);
                }

                mixPos = -1;
                songTypeStr = "MIX1";
                songPlayer = "Digi-Mix driver.";
                break;

            case 0x594d5431: // 'YMT1'
            case 0x594d5432: // 'YMT2'
                if (fileSize < 12) {
                    throw new IllegalArgumentException("File too small");
                }
                check = new String(bigMalloc, 4, 8, StandardCharsets.US_ASCII);
                if (!check.equals("LeOnArD!")) {
                    throw new IllegalArgumentException("Not a valid YM format !");
                }
                ctx.offset = 12;
                ctx.remaining = fileSize - 12;
                songType = YmFileType.YM_TRACKER1;
                nbVoice = ctx.readMotorolaWord();
                setPlayerRate(ctx.readMotorolaWord());
                nbFrame = ctx.readMotorolaDword();
                loopFrame = ctx.readMotorolaDword();
                nbDrum = ctx.readMotorolaWord();
                attrib = ctx.readMotorolaDword();
                songName = ctx.readNtString();
                songAuthor = ctx.readNtString();
                songComment = ctx.readNtString();
                if (ctx.remaining < 0) {
                    throw new IllegalArgumentException("File too small");
                }
                if (nbDrum > 0) {
                    drumTab = new DigiDrum[nbDrum];
                    for (int i = 0; i < nbDrum; i++) {
                        drumTab[i] = new DigiDrum();
                        drumTab[i].size = ctx.readMotorolaWord();
                        if (ctx.remaining < 0) {
                            throw new IllegalArgumentException("File too small");
                        }
                        drumTab[i].repLen = drumTab[i].size;
                        if (id == 0x594d5432) { // YMT2
                            drumTab[i].repLen = ctx.readMotorolaWord();
                            ctx.readMotorolaWord(); // flag
                            if (ctx.remaining < 0) {
                                throw new IllegalArgumentException("File too small");
                            }
                        }
                        if (drumTab[i].repLen > drumTab[i].size) {
                            drumTab[i].repLen = drumTab[i].size;
                        }
                        if (drumTab[i].size != 0) {
                            if ((drumTab[i].size & 0xffff_ffffL) >= 0x8000_0000L) {
                                throw new IllegalArgumentException("Drumtab to big");
                            }
                            if (ctx.remaining < drumTab[i].size) {
                                throw new IllegalArgumentException("File too small");
                            }
                            drumTab[i].pData = new byte[drumTab[i].size];
                            System.arraycopy(ctx.data, ctx.offset, drumTab[i].pData, 0, drumTab[i].size);
                            ctx.skip(drumTab[i].size);
                        }
                    }
                }

                trackerFreqShift = 0;
                if (id == 0x594d5432) {
                    trackerFreqShift = (attrib >> 28) & 15;
                    attrib &= 0x0fff_ffff;
                    songTypeStr = "YM-T2";
                } else {
                    songTypeStr = "YM-T1";
                }

                if (nbVoice > MAX_VOICE || nbVoice < 0) {
                    throw new IllegalArgumentException("Too many voices");
                }
                if (nbFrame >= (0x8000_0000L / (MAX_VOICE * 4)) || nbFrame < 0) {
                    throw new IllegalArgumentException("Too many frames");
                }
                if (ctx.remaining < (int) (4L * nbVoice * nbFrame)) {
                    throw new IllegalArgumentException("File too small");
                }

                dataStreamOffset = ctx.offset;
                ymChip.setClock(ATARI_CLOCK);

                ymTrackerInit(100);
                streamInc = 16;
                setTimeControl(true);
                songPlayer = "Universal Tracker";
                break;

            default:
                throw new IllegalArgumentException("Unknown YM format !");
        }

        if (!deInterleave()) {
            throw new IllegalStateException();
        }
    }

    //
    // Playback control
    //

    public void setTimeControl(boolean bTime) {
        if (bTime) attrib |= A_TIMECONTROL;
        else attrib &= ~A_TIMECONTROL;
    }

    public void setLoopMode(boolean bLoopMode) {
        loop = bLoopMode;
    }

    public void setPlayerRate(int rate) {
        playerRate = rate;
    }

    public int getPos() {
        if (!isSeekable()) return 0;
        if (nbFrame > 0 && playerRate > 0) {
            return (currentFrame * 1000) / playerRate;
        }
        return 0;
    }

    public int getMusicTime() {
        if (nbFrame > 0 && playerRate > 0) {
            return (nbFrame * 1000) / playerRate;
        }
        return 0;
    }

    public int setMusicTime(int time) {
        if (!isSeekable()) return 0;
        int newTime = time;
        if (newTime >= getMusicTime()) newTime = 0;
        currentFrame = (newTime * playerRate) / 1000;
        return newTime;
    }

    public void getMusicInfo(YmMusicInfo pInfo) {
        if (pInfo != null) {
            pInfo.pSongName = songName;
            pInfo.pSongAuthor = songAuthor;
            pInfo.pSongComment = songComment;
            pInfo.pSongType = songTypeStr;
            pInfo.pSongPlayer = songPlayer;
            if (playerRate > 0) {
                pInfo.musicTimeInMs = (nbFrame * 1000) / playerRate;
                pInfo.musicTimeInSec = pInfo.musicTimeInMs / 1000;
            } else {
                pInfo.musicTimeInSec = 0;
                pInfo.musicTimeInMs = 0;
            }
        }
    }

    public void setAttrib(int _attrib) {
        attrib = _attrib;
    }

    public int getAttrib() {
        return attrib;
    }

    public boolean isSeekable() {
        return (getAttrib() & A_TIMECONTROL) != 0;
    }

    public int readYmRegister(int reg) {
        return ymChip.readRegister(reg);
    }

    public void setVolume(int volume) {
        // ymChip.setGlobalVolume(volume);
    }

    public int waveCreate(String fName) {
        // Not implemented in original sources
        return -1;
    }

    //
    // Update / render
    //
    public boolean update(short[] sampleBuffer, int nbSample) {
        if (!musicOk || pause || bMusicOver) {
            bufferClear(sampleBuffer, nbSample);
            return !bMusicOver;
        }

        if (songType.v >= YmFileType.YM_MIX1.v
                && songType.v < YmFileType.YM_MIXMAX.v) {
            stDigitMix(sampleBuffer, nbSample);
        } else if (songType.v >= YmFileType.YM_TRACKER1.v
                && songType.v < YmFileType.YM_TRACKERMAX.v) {
            ymTrackerUpdate(sampleBuffer, nbSample);
        } else {
            int outPos = 0;
            int nbs = nbSample;
            int vblNbSample = replayRate / playerRate;
            do {
                int sampleToCompute = vblNbSample - innerSamplePos;
                if (sampleToCompute > nbs) sampleToCompute = nbs;
                innerSamplePos += sampleToCompute;
                if (innerSamplePos >= vblNbSample) {
                    player();
                    innerSamplePos -= vblNbSample;
                }
                if (sampleToCompute > 0) {
                    short[] chunk = new short[sampleToCompute];
                    ymChip.update(chunk, sampleToCompute);
                    System.arraycopy(chunk, 0, sampleBuffer, outPos, sampleToCompute);
                    outPos += sampleToCompute;
                }
                nbs -= sampleToCompute;
            } while (nbs > 0);
        }

        return true;
    }

    //
    // Player tick
    //
    private void player() {
        if (currentFrame < 0) currentFrame = 0;

        if (currentFrame >= nbFrame) {
            if (loop) {
                currentFrame = loopFrame;
            } else {
                bMusicOver = true;
                ymChip.reset();
                return;
            }
        }

        int ptrBase = dataStreamOffset + currentFrame * streamInc;

        for (int i = 0; i <= 10; i++) {
            ymChip.writeRegister(i, bigMalloc[ptrBase + i] & 0xFF);
        }

        ymChip.sidStop(0);
        ymChip.sidStop(1);
        ymChip.sidStop(2);
        ymChip.syncBuzzerStop();

        // Digi-drum handling
        if (songType == YmFileType.YM_V2) {
            if ((bigMalloc[ptrBase + 13] & 0xFF) != 0xff) {
                ymChip.writeRegister(11, bigMalloc[ptrBase + 11] & 0xFF);
                ymChip.writeRegister(12, 0);
                ymChip.writeRegister(13, 10);
            }
            if ((bigMalloc[ptrBase + 10] & 0x80) != 0) {
                int sampleNum = bigMalloc[ptrBase + 10] & 0x7f;
                if ((bigMalloc[ptrBase + 12] & 0xFF) != 0) {
                    if (sampleNum < MAX_DIGIDRUM) {
                        int sampleFrq = MFP_CLOCK / (bigMalloc[ptrBase + 12] & 0xFF);
                        ymChip.drumStart(2, sampleAddress[sampleNum], sampleLen[sampleNum], sampleFrq);
                    }
                }
                ymChip.writeRegister(7, ymChip.readRegister(7) | 0x24);
            }
        } else if (songType.v >= YmFileType.YM_V3.v) {
            ymChip.writeRegister(11, bigMalloc[ptrBase + 11] & 0xFF);
            ymChip.writeRegister(12, bigMalloc[ptrBase + 12] & 0xFF);
            if ((bigMalloc[ptrBase + 13] & 0xFF) != 0xff) {
                ymChip.writeRegister(13, bigMalloc[ptrBase + 13] & 0xFF);
            }

            if (songType.v >= YmFileType.YM_V5.v) {
                if (songType == YmFileType.YM_V6) {
                    readYm6Effect(ptrBase, 1, 6, 14);
                    readYm6Effect(ptrBase, 3, 8, 15);
                } else {
                    // YM5 effect decoding
                    int code = (bigMalloc[ptrBase + 1] >> 4) & 3;
                    if (code != 0) {
                        int voice = code - 1;
                        int prediv = MFP_PREDIV[(bigMalloc[ptrBase + 6] >> 5) & 7];
                        prediv *= (bigMalloc[ptrBase + 14] & 0xFF);
                        int tmpFreq = 0;
                        if (prediv != 0) {
                            tmpFreq = 2457600 / prediv;
                            ymChip.sidStart(voice, tmpFreq, bigMalloc[ptrBase + voice + 8] & 15);
                        }
                    }

                    code = (bigMalloc[ptrBase + 3] >> 4) & 3;
                    if (code != 0) {
                        int voice = code - 1;
                        int ndrum = bigMalloc[ptrBase + 8 + voice] & 31;
                        if (ndrum >= 0 && ndrum < nbDrum) {
                            int prediv = MFP_PREDIV[(bigMalloc[ptrBase + 8] >> 5) & 7];
                            prediv *= (bigMalloc[ptrBase + 15] & 0xFF);
                            if (prediv != 0) {
                                int sampleFrq = MFP_CLOCK / prediv;
                                ymChip.drumStart(voice, drumTab[ndrum].pData, drumTab[ndrum].size, sampleFrq);
                            }
                        }
                    }
                }
            }
        }
        currentFrame++;
    }

    private void readYm6Effect(int ptrBase, int codeReg, int predivReg, int countReg) {
        int code = bigMalloc[ptrBase + codeReg] & 0xf0;
        int prediv = MFP_PREDIV[(bigMalloc[ptrBase + predivReg] >> 5) & 7];
        int count = bigMalloc[ptrBase + countReg] & 0xFF;

        if ((code & 0x30) != 0) {
            int tmpFreq;
            int voice = ((code & 0x30) >> 4) - 1;
            switch (code & 0xc0) {
                case 0x00: // SID
                case 0x80: // Sinus-SID
                    prediv *= count;
                    tmpFreq = 0;
                    if (prediv != 0) {
                        tmpFreq = 2457600 / prediv;
                        if ((code & 0xc0) == 0x00)
                            ymChip.sidStart(voice, tmpFreq, bigMalloc[ptrBase + voice + 8] & 15);
                        else
                            ymChip.sidSinStart(voice, tmpFreq, bigMalloc[ptrBase + voice + 8] & 15);
                    }
                    break;
                case 0x40: // DigiDrum
                    int ndrum = bigMalloc[ptrBase + voice + 8] & 31;
                    if (ndrum >= 0 && ndrum < nbDrum) {
                        prediv *= count;
                        if (prediv > 0) {
                            tmpFreq = 2457600 / prediv;
                            ymChip.drumStart(voice, drumTab[ndrum].pData, drumTab[ndrum].size, tmpFreq);
                        }
                    }
                    break;
                case 0xc0: // Sync-Buzzer
                    prediv *= count;
                    tmpFreq = 0;
                    if (prediv != 0) {
                        tmpFreq = 2457600 / prediv;
                        ymChip.syncBuzzerStart(tmpFreq, bigMalloc[ptrBase + voice + 8] & 15);
                    }
                    break;
            }
        }
    }

    //
    // MIX1 digit mixer
    //
    private void readNextBlockInfo() {
        nbRepeat--;
        if (nbRepeat <= 0) {
            mixPos++;
            if (mixPos >= nbMixBlock) {
                mixPos = 0;
                if (!loop) bMusicOver = true;
            }
            nbRepeat = mixBlock[mixPos].nbRepeat;
        }
        currentMixSample = bigSampleBuffer;
        currentMixSampleOffset = mixBlock[mixPos].sampleStart;
        currentSampleLength = ((long) mixBlock[mixPos].sampleLength) << 12;
        currentPente = (((long) mixBlock[mixPos].replayFreq) << 12) / PC_DAC_FREQ;
        currentPos &= ((1L << 12) - 1);
    }

    private void stDigitMix(short[] pWrite16, int nbs) {
        if (bMusicOver) return;

        if (mixPos == -1) {
            nbRepeat = -1;
            readNextBlockInfo();
        }

        if (nbs > 0) {
            for (int i = 0; i < nbs; i++) {
                int pos = (int) (currentPos >> 12);
                short sa = (short) ((currentMixSample[currentMixSampleOffset + pos] & 0xFF) << 8);
                short sb = sa;
                if (pos < ((currentSampleLength >> 12) - 1)) {
                    sb = (short) ((currentMixSample[currentMixSampleOffset + pos + 1] & 0xFF) << 8);
                }
                int frac = (int) (currentPos & ((1 << 12) - 1));
                int value = sa + (((sb - sa) * frac) >> 12);
                pWrite16[i] = (short) value;

                currentPos += currentPente;
                if (currentPos >= currentSampleLength) {
                    readNextBlockInfo();
                    if (bMusicOver) return;
                }
            }
        }
    }

    //
    // YM-Tracker
    //
    private void ymTrackerDesInterleave() {
        if ((attrib & A_STREAMINTERLEAVED) != 0) {
            int size = 4 * nbVoice * nbFrame;
            byte[] pNewBuffer = new byte[size];
            int step = 4 * nbVoice;
            int n1 = step;
            int a2 = 0;
            int a0 = dataStreamOffset;
            do {
                int n2 = nbFrame;
                int a1 = a2;
                do {
                    pNewBuffer[a1] = bigMalloc[a0++];
                    a1 += step;
                } while (--n2 > 0);
                a2++;
            } while (--n1 > 0);
            System.arraycopy(pNewBuffer, 0, bigMalloc, dataStreamOffset, size);
            attrib &= ~A_STREAMINTERLEAVED;
        }
    }

    private void ymTrackerInit(int volMaxPercent) {
        for (int i = 0; i < MAX_VOICE; i++) {
            trackerVoice[i].bRunning = false;
        }
        trackerNbSampleBefore = 0;

        int scale = (256 * volMaxPercent) / (nbVoice * 100);

        for (int vol = 0; vol < 64; vol++) {
            for (int s = -128; s < 128; s++) {
                trackerVolumeTable[256 * vol + (s + 128)] = (short) ((s * scale * vol) / 64);
            }
        }

        ymTrackerDesInterleave();
    }

    private void ymTrackerPlayer(YmMusic.YmTrackerVoice[] pVoice) {
        int base = dataStreamOffset + (currentFrame * nbVoice * 4);
        for (int i = 0; i < nbVoice; i++) {
            int off = base + i * 4;
            pVoice[i].sampleFreq = ((bigMalloc[off + 2] & 0xFF) << 8) | (bigMalloc[off + 3] & 0xFF);
            if (pVoice[i].sampleFreq != 0) {
                pVoice[i].sampleVolume = bigMalloc[off + 1] & 63;
                pVoice[i].bLoop = (bigMalloc[off + 1] & 0x40) != 0;
                int n = bigMalloc[off] & 0xFF;
                if (n != 0xff) {
                    pVoice[i].bRunning = true;
                    pVoice[i].pSample = drumTab[n].pData;
                    pVoice[i].sampleSize = drumTab[n].size;
                    pVoice[i].repLen = drumTab[n].repLen;
                    pVoice[i].samplePos = 0;
                }
            } else {
                pVoice[i].bRunning = false;
            }
        }

        currentFrame++;
        if (currentFrame >= nbFrame) {
            if (!loop) {
                bMusicOver = true;
            }
            currentFrame = 0;
        }
    }

    private void ymTrackerVoiceAdd(YmMusic.YmTrackerVoice pVoice, short[] pBuffer, int outPos, int nbs) {
        if (!pVoice.bRunning) return;

        short[] pVolumeTab = trackerVolumeTable;
        int volIdx = 256 * (pVoice.sampleVolume & 63);
        byte[] pSample = pVoice.pSample;
        int samplePos = pVoice.samplePos;

        double stepD = pVoice.sampleFreq << YMTPREC;
        stepD *= 1 << trackerFreqShift;
        stepD /= replayRate;
        int sampleInc = (int) stepD;

        int sampleEnd = (pVoice.sampleSize << YMTPREC);
        int repLen = (pVoice.repLen << YMTPREC);

        for (int i = 0; i < nbs; i++) {
            int idx = samplePos >> YMTPREC;
            int va = pVolumeTab[volIdx + (pSample[idx] & 0xFF)];
            int vb = va;
            if (samplePos < (sampleEnd - (1L << YMTPREC))) {
                vb = pVolumeTab[volIdx + (pSample[idx + 1] & 0xFF)];
            }
            int frac = (int) (samplePos & ((1L << YMTPREC) - 1));
            va += (((vb - va) * frac) >> YMTPREC);
            pBuffer[outPos + i] = (short) (pBuffer[outPos + i] + va);

            samplePos += sampleInc;
            if (samplePos >= sampleEnd) {
                if (pVoice.bLoop) {
                    samplePos -= repLen;
                } else {
                    pVoice.bRunning = false;
                    return;
                }
            }
        }
        pVoice.samplePos = samplePos;
    }

    private void ymTrackerUpdate(short[] pBuffer, int nbSample) {
        Arrays.fill(pBuffer, 0, nbSample, (short) 0);
        if (bMusicOver) return;

        int outPos = 0;
        int remaining = nbSample;
        do {
            if (trackerNbSampleBefore == 0) {
                ymTrackerPlayer(trackerVoice);
                if (bMusicOver) return;
                trackerNbSampleBefore = YMTNBSRATE;
            }
            int nbs = trackerNbSampleBefore;
            if (nbs > remaining) nbs = remaining;
            trackerNbSampleBefore -= nbs;
            if (nbs > 0) {
                for (int i = 0; i < nbVoice; i++) {
                    ymTrackerVoiceAdd(trackerVoice[i], pBuffer, outPos, nbs);
                }
                outPos += nbs;
            }
            remaining -= nbs;
        } while (remaining > 0);
    }
}
