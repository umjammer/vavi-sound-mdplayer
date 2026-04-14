package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.IntConsumer;

import dotnet4j.util.compat.TriConsumer;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * MegaDrive SGDK XGM
 */
public class Xgm {

    private static final Logger logger = getLogger(Xgm.class.getName());

    public static final int FCC_XGM = 0x204d4758; // "XGM "
    public static final int FCC_GD3 = 0x20336447; // "Gd3 "

    public static class XGMSampleID {
        public int addr = 0;
        public int size = 0;
    }

    private final XGMSampleID[] sampleID = new XGMSampleID[63];
    private int sampleDataBlockSize = 0;
    private int sampleDataBlockAddr = 0;
    private int musicDataBlockSize = 0;
    private int musicDataBlockAddr = 0;
    private int versionInformation = 0;
    private int dataInformation = 0;
    private boolean isNTSC = false;
    boolean existGD3 = false;
    private boolean multiTrackFile = false;
    int gd3InfoStartAddr = 0;

    byte[] xgmBuf;
    Runnable stop;
    Runnable tag;
    Runnable loop;
    TriConsumer<Integer, Integer, Integer> ym2612Write;
    IntConsumer sn76489Write;
    int sampleRate;

    void init() {
        musicPtr = musicDataBlockAddr;
        xgmPcm = new XgmPcm[] {new XgmPcm(), new XgmPcm(), new XgmPcm(), new XgmPcm()};
        DACEnable = 0;
        this.musicStep = sampleRate / 60.0; // setting.getoutputDevice().SampleRate / 60.0;
    }

    void clock(boolean stopped) {
        pcmSpeedCounter++; // = (double) sampleRate / setting.getoutputDevice().SampleRate * speed;
        while (pcmSpeedCounter >= 1.0 && !stopped) {
            pcmSpeedCounter -= 1.0;
            onePCMFrameMain();
        }
    }

    /** @throws IllegalArgumentException parse error */
    void getXGMInfo(byte[] vgmBuf) {
        if (vgmBuf == null) throw new IllegalArgumentException("null buffer");

        try {
            if (ByteUtil.readLeInt(vgmBuf, 0) != FCC_XGM) throw new IllegalArgumentException("not xgm data");

            for (int i = 0; i < 63; i++) {
                sampleID[i] = new XGMSampleID();
                sampleID[i].addr = ((int) ByteUtil.readLeShort(vgmBuf, i * 4 + 4) * 256);
                sampleID[i].size = ((int) ByteUtil.readLeShort(vgmBuf, i * 4 + 6) * 256);
            }

            sampleDataBlockSize = ByteUtil.readLeShort(vgmBuf, 0x100);
            versionInformation = vgmBuf[0x102] & 0xff;
            dataInformation = vgmBuf[0x103] & 0xff;
            isNTSC = (dataInformation & 0x1) == 0;
            existGD3 = (dataInformation & 0x2) != 0;
            multiTrackFile = (dataInformation & 0x4) != 0;
            sampleDataBlockAddr = 0x104;
            musicDataBlockSize = ByteUtil.readLeInt(vgmBuf, sampleDataBlockAddr + sampleDataBlockSize * 256);
            musicDataBlockAddr = sampleDataBlockAddr + sampleDataBlockSize * 256 + 4;
            gd3InfoStartAddr = musicDataBlockAddr + musicDataBlockSize;

            tag.run();

            if (musicDataBlockSize == 0) {
                throw new IllegalArgumentException("illegal block size");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("An exception occurred while getting XGM information: " + e.getMessage(), e);
        }
    }

    public boolean isPlaying() {
        return true;
    }

    private double musicStep; // setting.getoutputDevice().SampleRate / 60.0;
    double pcmStep; // setting.getoutputDevice().SampleRate / 14000.0;
    private double musicDownCounter = 0.0;
    private double pcmDownCounter = 0.0;
    private int musicPtr = 0;
    private int DACEnable = 0;

    void oneFrameMain() {
        try {
            //if (model == EnmModel.RealModel) return;

            musicStep = sampleRate / (isNTSC ? 60.0 : 50.0);

            if (musicDownCounter <= 0.0) {
                // process xgm
                oneFrameXGM();
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

            //if (pcmDownCounter <= 0.0) {
            //    // process pcm
            //    oneFramePCM();
            //    pcmDownCounter += pcmStep;
            //}
            //pcmDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void onePCMFrameMain() {
        try {
            if (pcmDownCounter <= 0.0) {
                // process pcm
                oneFramePCM();
                pcmDownCounter += pcmStep;
            }
            pcmDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameXGM() {
        while (true) {

            int cmd = xgmBuf[musicPtr++] & 0xff;

            // wait
            if (cmd == 0) break;

            // loop command
            if (cmd == 0x7e) {
                musicPtr = musicDataBlockAddr + ByteUtil.readLe24(xgmBuf, musicPtr);
                loop.run();
                continue;
            }

            // end command
            if (cmd == 0x7f) {
                stop.run();
                break;
            }

            int X = cmd & 0xf;
            cmd &= 0xf0;

            if (cmd == 0x10) {
                // Psg register write:
                writePSG(X);
            } else if (cmd == 0x20) {
                // Ym2612 port 0 register write:
                writeYM2612P0(X);
            } else if (cmd == 0x30) {
                // Ym2612 port 1 register write:
                writeYM2612P1(X);
            } else if (cmd == 0x40) {
                // Ym2612 key off/on ($28) command write:
                writeYM2612Key(X);
            } else if (cmd == 0x50) {
                // PCM play command:
                playPCM(X);
            }
        }
    }

    private void writePSG(int X) {
        for (int i = 0; i < X + 1; i++) {
            int data = xgmBuf[musicPtr++] & 0xff;
            sn76489Write.accept(data);
        }
    }

    private void writeYM2612P0(int X) {
        for (int i = 0; i < X + 1; i++) {
            int adr = xgmBuf[musicPtr++] & 0xff;
            int val = xgmBuf[musicPtr++] & 0xff;
            if (adr == 0x2b) DACEnable = val & 0x80;
            ym2612Write.accept(0, adr, val);
        }
    }

    private void writeYM2612P1(int X) {
        for (int i = 0; i < X + 1; i++) {
            int adr = xgmBuf[musicPtr++] & 0xff;
            int val = xgmBuf[musicPtr++] & 0xff;
            ym2612Write.accept(1, adr, val);
        }
    }

    private void writeYM2612Key(int X) {
        for (int i = 0; i < X + 1; i++) {
            int val = xgmBuf[musicPtr++] & 0xff;
            ym2612Write.accept(0, 0x28, val);
        }
    }

    public static class XgmPcm {
        public int priority = 0;
        public int startAddr = 0;
        public int endAddr = 0;
        public int addr = 0;
        public int inst = 0;
        public boolean isPlaying = false;
        public int data = 0;
    }

    public XgmPcm[] xgmPcm = null;
    private double pcmSpeedCounter;

    private void playPCM(int X) {
        int priority = X & 0xc;
        int channel = X & 0x3;
        int id = xgmBuf[musicPtr++] & 0xff;

        // Can only be played if priority is high or if muted
        if (xgmPcm[channel].priority <= priority || !xgmPcm[channel].isPlaying) {
            if (id == 0 || sampleID[id - 1].size == 0) {
                // If the ID is 0 or an undefined ID is specified, the sound will stop.
                xgmPcm[channel].priority = 0;
//                xgmPcm[channel].startAddr = 0;
//                xgmPcm[channel].endAddr = 0;
//                xgmPcm[channel].addr = 0;
//                xgmPcm[channel].inst = id;
                xgmPcm[channel].isPlaying = false;
            } else {
                xgmPcm[channel].priority = priority;
                xgmPcm[channel].startAddr = sampleDataBlockAddr + sampleID[id - 1].addr;
                xgmPcm[channel].endAddr = sampleDataBlockAddr + sampleID[id - 1].addr + sampleID[id - 1].size;
                xgmPcm[channel].addr = sampleDataBlockAddr + sampleID[id - 1].addr;
                xgmPcm[channel].inst = id;
                xgmPcm[channel].isPlaying = true;
            }
        }
    }

    private void oneFramePCM() {
        if (DACEnable == 0) return;

        int o = 0;

        for (int i = 0; i < 4; i++) {
            if (!xgmPcm[i].isPlaying) continue;
            byte d = xgmBuf[xgmPcm[i].addr++]; // signed
            o += d;
            xgmPcm[i].data = Math.abs(d);
            if (xgmPcm[i].addr >= xgmPcm[i].endAddr) {
                xgmPcm[i].isPlaying = false;
                xgmPcm[i].data = 0;
            }
        }
        o = (short) Math.clamp(o, Byte.MIN_VALUE + 1, Byte.MAX_VALUE);
        o += 0x80;

        ym2612Write.accept(0, 0x2a, o);
    }
}
