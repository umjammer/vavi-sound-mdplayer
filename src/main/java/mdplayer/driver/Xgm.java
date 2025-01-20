package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.plugin.BasePlugin;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class Xgm extends BaseDriver {

    private static final Logger logger = getLogger(Xgm.class.getName());

    public Xgm() {
        this.setting = Setting.getInstance();
        musicStep = Common.VGMProcSampleRate / 60.0; // setting.getoutputDevice().SampleRate / 60.0;
        pcmStep = setting.getOutputDevice().getSampleRate() / 14000.0;
    }

    public static final int FCC_XGM = 0x204d4758; // "XGM "
    public static final int FCC_GD3 = 0x20336447; // "Gd3 "

    private static class XGMSampleID {
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
    private boolean existGD3 = false;
    private boolean multiTrackFile = false;
    private int gd3InfoStartAddr = 0;

    @Override
    public boolean init(byte[] xgmBuf, BasePlugin plugin, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        this.vgmBuf = xgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        if (!getXGMInfo(vgmBuf)) return false;

        if (model == EnmModel.RealModel) {
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setYM2612SyncWait((byte) 0, 1);
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setYM2612SyncWait((byte) 1, 1);
        }

        // Initializing the Driver
        musicPtr = musicDataBlockAddr;
        xgmpcm = new XgmPcm[] {new XgmPcm(), new XgmPcm(), new XgmPcm(), new XgmPcm()};
        DACEnable = 0;

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }

            pcmSpeedCounter++; // = (double)Common.VGMProcSampleRate / setting.getoutputDevice().SampleRate * vgmSpeed;
            while (pcmSpeedCounter >= 1.0 && !stopped) {
                pcmSpeedCounter -= 1.0;
                onePCMFrameMain();
            }

            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        getXGMInfo(buf);
        return gd3;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] vgmBuf) {

        if (!existGD3) return new Vgm.Gd3();

        Vgm.Gd3 gd3 = Common.getGD3Info(vgmBuf, gd3InfoStartAddr + 12);
        gd3.usedChips = usedChips;

        return gd3;
    }

    private boolean getXGMInfo(byte[] vgmBuf) {
        if (vgmBuf == null) return false;

        try {
            if (ByteUtil.readLeInt(vgmBuf, 0) != FCC_XGM) return false;

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

            gd3 = getGD3Info(vgmBuf);

            if (musicDataBlockSize == 0) {
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.DEBUG, "An exception occurred while getting XGM information: " + e.getMessage(), e);
            return false;
        }

        return true;
    }

    public boolean isPlaying() {
        return true;
    }


    private double musicStep;// setting.getoutputDevice().SampleRate / 60.0;
    private final double pcmStep;// setting.getoutputDevice().SampleRate / 14000.0;
    private double musicDownCounter = 0.0;
    private double pcmDownCounter = 0.0;
    private int musicPtr = 0;
    private int DACEnable = 0;

    private void oneFrameMain() {
        try {
            //if (model == EnmModel.RealModel) return;

            counter++;
            vgmFrameCounter++;

            musicStep = Common.VGMProcSampleRate / (isNTSC ? 60.0 : 50.0);

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

            int cmd = vgmBuf[musicPtr++] & 0xff;

            // wait
            if (cmd == 0) break;

            // loop command
            if (cmd == 0x7e) {
                musicPtr = musicDataBlockAddr + ByteUtil.readLe24(vgmBuf, musicPtr);
                vgmCurLoop++;
                continue;
            }

            // end command
            if (cmd == 0x7f) {
                stopped = true;
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
            int data = vgmBuf[musicPtr++] & 0xff;
            plugin.audio.chipRegister.chip(Sn76489Chip.class).setSN76489Register(0, data, model);
        }
    }

    private void writeYM2612P0(int X) {
        for (int i = 0; i < X + 1; i++) {
            int adr = vgmBuf[musicPtr++] & 0xff;
            int val = vgmBuf[musicPtr++] & 0xff;
            if (adr == 0x2b) DACEnable = val & 0x80;
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setYM2612Register(0, 0, adr, val, model, vgmFrameCounter);
        }
    }

    private void writeYM2612P1(int X) {
        for (int i = 0; i < X + 1; i++) {
            int adr = vgmBuf[musicPtr++] & 0xff;
            int val = vgmBuf[musicPtr++] & 0xff;
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setYM2612Register(0, 1, adr, val, model, vgmFrameCounter);
        }
    }

    private void writeYM2612Key(int X) {
        for (int i = 0; i < X + 1; i++) {
            int val = vgmBuf[musicPtr++] & 0xff;
            plugin.audio.chipRegister.chip(Ym2612Chip.class).setYM2612Register(0, 0, 0x28, val, model, vgmFrameCounter);
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

    public XgmPcm[] xgmpcm = null;
    private double pcmSpeedCounter;

    private void playPCM(int X) {
        int priority = X & 0xc;
        int channel = X & 0x3;
        int id = vgmBuf[musicPtr++] & 0xff;

        // Can only be played if priority is high or if muted
        if (xgmpcm[channel].priority <= priority || !xgmpcm[channel].isPlaying) {
            if (id == 0 || sampleID[id - 1].size == 0) {
                // If the ID is 0 or an undefined ID is specified, the sound will stop.
                xgmpcm[channel].priority = 0;
//                xgmPcm[channel].startAddr = 0;
//                xgmPcm[channel].endAddr = 0;
//                xgmPcm[channel].addr = 0;
//                xgmPcm[channel].inst = id;
                xgmpcm[channel].isPlaying = false;
            } else {
                xgmpcm[channel].priority = priority;
                xgmpcm[channel].startAddr = sampleDataBlockAddr + sampleID[id - 1].addr;
                xgmpcm[channel].endAddr = sampleDataBlockAddr + sampleID[id - 1].addr + sampleID[id - 1].size;
                xgmpcm[channel].addr = sampleDataBlockAddr + sampleID[id - 1].addr;
                xgmpcm[channel].inst = id;
                xgmpcm[channel].isPlaying = true;
            }
        }
    }

    private void oneFramePCM() {
        if (DACEnable == 0) return;

        int o = 0;

        for (int i = 0; i < 4; i++) {
            if (!xgmpcm[i].isPlaying) continue;
            byte d = vgmBuf[xgmpcm[i].addr++]; // signed
            o += d;
            xgmpcm[i].data = Math.abs(d);
            if (xgmpcm[i].addr >= xgmpcm[i].endAddr) {
                xgmpcm[i].isPlaying = false;
                xgmpcm[i].data = 0;
            }
        }
        o = (short) Math.min(Math.max(o, Byte.MIN_VALUE + 1), Byte.MAX_VALUE);
        o += 0x80;

        plugin.audio.chipRegister.chip(Ym2612Chip.class).setYM2612Register(0, 0, 0x2a, o, model, vgmFrameCounter);
    }
}
