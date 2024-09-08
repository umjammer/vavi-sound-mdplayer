package mdplayer.plugin;

import java.util.List;
import java.util.logging.Level;

import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.OpeManager;
import mdplayer.Request;
import mdplayer.Setting;
import mdplayer.WaveWriter;
import mdplayer.driver.Vgm;
import mdplayer.format.AIFFFileFormat;
import mdplayer.format.FileFormat;
import mdplayer.format.MP3FileFormat;
import mdplayer.format.WAVFileFormat;
import mdsound.MDSound;
import vavi.util.Debug;

import static mdplayer.chips.RealChipPlugin.realChipClose;


/**
 * BasePlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public abstract class BasePlugin implements Plugin {

    protected Setting setting = Setting.getInstance();

    public Audio audio = Audio.getInstance();


    protected byte[] vgmBuf = null;
    protected double vgmSpeed;


    protected boolean oneTimeReset = false;

    protected String playingFileName;
    protected String playingArcFileName;
    protected int midiMode = 0;
    protected int songNo = 0;
    protected List<Tuple<String, byte[]>> extendFile = null;

    protected final long stwh = System.currentTimeMillis();

    public boolean flgReinit = false;

    public boolean getEmuOnly() {
        return false;
    }

    protected BasePlugin() {
        init();
    }

    @Override
    public void init() {
        Debug.println("Audio:Init:Begin");

        Thread trd = new Thread(this::trdIF);
        trd.setPriority(Thread.NORM_PRIORITY);
        trd.start();

        Debug.println("Audio:Init:STEP 02");

//        setting = Setting.getInstance();
//        vstMng.setting = setting;

        audio.waveWriter = new WaveWriter(setting);

        Debug.println("Audio:Init:STEP 03");

        setting.init();

        Debug.println("Audio:Init:STEP 05");

        audio.paused = false;
        audio.stopped = true;
Debug.println("stop: " + audio.stopped + ", " + audio.hashCode());
        audio._fatalError = false;
        oneTimeReset = false;

        Debug.println("Audio:Init:STEP 06");


        Debug.println("Audio:Init:STEP 07");

        // midi outをリリース
        audio.releaseAllMIDIout();

        Debug.println("Audio:Init:STEP 08");


        Debug.println("Audio:Init:STEP 09");

        // 各外部dllの動的読み込み

        Debug.println("Audio:Init:STEP 10");

        audio.naudioWrap.start(setting);

        Debug.println("Audio:Init:Complete");
    }

    protected void trdIF() {
        while (true) {
            Request req = OpeManager.getRequestToAudio();
            if (req == null) {
                Thread.yield();
                continue;
            }

            switch (req.request) {
            case Die: // 自殺してください
                seqDie();
                req.setEnd(true);
                return;
            case Stop:
                stop();
                req.setEnd(true);
                OpeManager.completeRequestToAudio(req);
                break;
            }
        }
    }

    protected void trdVgmRealFunction() {

        if (audio.driverReal == null) {
            audio.trdClosed = true;
            audio._trdStopped = true;
            return;
        }

        double o = System.currentTimeMillis() / audio.swFreq;
        double step = 1 / (double) setting.getOutputDevice().getSampleRate();
        audio._trdStopped = false;
new Exception(audio.driverReal.toString()).printStackTrace();
        try {
            while (!audio.trdClosed) {
                Thread.sleep(0);

                double el1 = System.currentTimeMillis() / audio.swFreq;
                if (el1 - o < step) continue;
                if (el1 - o >= step * setting.getOutputDevice().getSampleRate() / 100.0) { // 閾値10ms
                    do {
                        o += step;
                    } while (el1 - o >= step);
                } else {
                    o += step;
                }

                if (audio.stopped || audio.paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (audio.hiyorimiNecessary && audio.driverVirtual.isDataBlock) {
                    continue;
                }

                if (audio.vgmFadeout) {
                    if (audio.vgmRealFadeoutVol != 1000) audio.vgmRealFadeoutVolWait--;
                    if (audio.vgmRealFadeoutVolWait == 0) {
                        if (audio.useChip.contains(Common.EnmChip.YM2151)) audio.chipRegister.setFadeoutVolYM2151(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM2203)) audio.chipRegister.setFadeoutVolYM2203(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.AY8910)) audio.chipRegister.setFadeoutVolAY8910(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM2413)) audio.chipRegister.setFadeoutVolYM2413(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM2608)) audio.chipRegister.setFadeoutVolYM2608(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM2610)) audio.chipRegister.setFadeoutVolYM2610(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM2612)) audio.chipRegister.setFadeoutVolYM2612(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM3526)) audio.chipRegister.setFadeoutVolYM3526(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YM3812)) audio.chipRegister.setFadeoutVolYM3812(0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.SN76489))
                            audio.chipRegister.setFadeoutVolSN76489((byte) 0, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.YMF262)) audio.chipRegister.setFadeoutVolYMF262(0, audio.vgmRealFadeoutVol);

                        if (audio.useChip.contains(Common.EnmChip.S_YM2151)) audio.chipRegister.setFadeoutVolYM2151(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM2203)) audio.chipRegister.setFadeoutVolYM2203(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_AY8910)) audio.chipRegister.setFadeoutVolAY8910(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM2413)) audio.chipRegister.setFadeoutVolYM2413(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM2608)) audio.chipRegister.setFadeoutVolYM2608(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM2610)) audio.chipRegister.setFadeoutVolYM2610(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM2612)) audio.chipRegister.setFadeoutVolYM2612(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM3526)) audio.chipRegister.setFadeoutVolYM3526(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YM3812)) audio.chipRegister.setFadeoutVolYM3812(1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_SN76489))
                            audio.chipRegister.setFadeoutVolSN76489((byte) 1, audio.vgmRealFadeoutVol);
                        if (audio.useChip.contains(Common.EnmChip.S_YMF262)) audio.chipRegister.setFadeoutVolYMF262(1, audio.vgmRealFadeoutVol);

                        audio.vgmRealFadeoutVol++;

                        audio.vgmRealFadeoutVol = Math.min(127, audio.vgmRealFadeoutVol);
                        if (audio.vgmRealFadeoutVol == 127) {
//                            if (SoundChip.realChip != null) {
//                                softReset(EnmModel.RealModel);
//                            }
                            audio.vgmRealFadeoutVolWait = 1000;
                            audio.chipRegister.resetAllMIDIout();
                        } else {
                            audio.vgmRealFadeoutVolWait = 700 - audio.vgmRealFadeoutVol * 2;
                        }
                    }
                }

                if (audio.hiyorimiNecessary) {
                    //long v = driverReal.vgmFrameCounter - audio.driverVirtual.vgmFrameCounter;
                    //long d = setting.getoutputDevice().getSampleRate() * (setting.LatencySCCI - setting.getoutputDevice().getSampleRate() * setting.LatencyEmulation) / 1000;
                    //long l = getLatency() / 4;
                    //int m = 0;
                    //if (d >= 0) {
                    //    if (v >= d - l && v <= d + l) m = 0;
                    //    else m = (v + d > l) ? 1 : 2;
                    //} else {
                    //    d = Math.abs(setting.getoutputDevice().getSampleRate() * ((int)setting.LatencyEmulation - (int)setting.LatencySCCI) / 1000);
                    //    if (v >= d - l && v <= d + l) m = 0;
                    //    else m = (v - d > l) ? 1 : 2;
                    //}

                    double dEMU = setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000.0;
                    double dSCCI = setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000.0;
                    double abs = Math.abs((audio.driverReal.vgmFrameCounter - dSCCI) - (audio.driverVirtual.vgmFrameCounter - dEMU));
                    int m = 0;
                    long l = getLatency() / 10;
                    if (abs >= l) {
                        m = ((audio.driverReal.vgmFrameCounter - dSCCI) > (audio.driverVirtual.vgmFrameCounter - dEMU)) ? 1 : 2;
                    }

                    switch (m) {
                    case 0: // x1
                        audio.driverReal.processOneFrame();
                        break;
                    case 1: // x1/2
                        audio.hiyorimiEven++;
                        if (audio.hiyorimiEven > 1) {
                            audio.driverReal.processOneFrame();
                            audio.hiyorimiEven = 0;
                        }
                        break;
                    case 2: // x2
                        audio.driverReal.processOneFrame();
                        audio.driverReal.processOneFrame();
                        break;
                    }
                } else {
                    audio.driverReal.processOneFrame();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        audio._trdStopped = true;
    }

    public int getLatency() {
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_AsioOut) {
            return setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getLatency() / 1000;
        }
        return 0; // naudioWrap.getAsioLatency(); TODO
    }

    public boolean play() {
//Debug.println("@@@@@@@@@@@@@@@ HERE: " + audio.stopped + ", " + audio.hashCode());
        audio.errMsg = "";

        stop();

        go();

        try {
            audio.waveWriter.open(playingFileName);
        } catch (Exception e) {
            e.printStackTrace();
            audio.errMsg = "wave file open error.";
            return false;
        }

//        Plugin plugin = audio.playingFileFormat.getPlugin();
//        boolean r = plugin.play(playingFileName, audio.playingFileFormat);

        while (true) {
//Debug.println("loop HERE");
            short[] buffer = new short[4];

            audio.trdVgmVirtualFunction(buffer, 0, buffer.length);
            audio.naudioWrap.write(buffer, 0, buffer.length);
            Thread.yield();
        }
    }


    public void startTrdVgmReal() {
        if (setting.getOutputDevice().getDeviceType() == Common.DEV_Null) {
            return;
        }

        audio.trdClosed = false;
        audio.trdMain = new Thread(this::trdVgmRealFunction);
        audio.trdMain.setPriority(Thread.MAX_PRIORITY);
        audio.trdMain.setDaemon(true);
        audio.trdMain.setName("trdVgmReal");
        audio.trdMain.start();
    }

    public void changeChipSampleRate(MDSound.Chip chip, int newSmplRate) {
        MDSound.Chip caa = chip;

        if (caa.samplingRate == newSmplRate)
            return;

        // quick and dirty hack to make sample rate changes work
        caa.samplingRate = newSmplRate;
        if (caa.samplingRate < setting.getOutputDevice().getSampleRate())
            caa.resampler = 0x01;
        else if (caa.samplingRate == setting.getOutputDevice().getSampleRate())
            caa.resampler = 0x02;
        else if (caa.samplingRate > setting.getOutputDevice().getSampleRate())
            caa.resampler = 0x03;
        caa.smpP = 1;
        caa.smpNext -= caa.smpLast;
        caa.smpLast = 0x00;
    }

    public void go() {
        audio.stopped = false;
//Debug.println("stopped: " + audio.stopped + ", " + audio.hashCode());
    }

    @Override
    public void stop() {
        audio.stop();
    }

    protected void resetFadeOutParam() {
        audio.vgmFadeout = false;
        audio.vgmFadeoutCounter = 1.0;
        audio.vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        audio.vgmRealFadeoutVol = 0;
        audio.vgmRealFadeoutVolWait = 4;

        audio.clearFadeoutVolume();

        audio.chipRegister.resetChips();
    }

    protected void seqDie() {
        close();
        realChipClose();
    }

    public void setVGMBuffer(FileFormat format, byte[] srcBuf, String playingFileName, String playingArcFileName, int midiMode, int songNo, List<Tuple<String, byte[]>> extFile) {
        //stop();
        audio.playingFileFormat = format;
        vgmBuf = srcBuf;
        this.playingFileName = playingFileName; // WaveWriter向け
        this.playingArcFileName = playingArcFileName;
        this.midiMode = midiMode;
        this.songNo = songNo;
        audio.chipRegister.setFileName(playingFileName); // ExportMIDI向け
        extendFile = extFile; // 追加ファイル
        Common.playingFilePath = Path.getDirectoryName(playingFileName);

        if (audio.naudioFileReader != null) {
            audio.nAudioStop();
        }

        if (format instanceof WAVFileFormat || format instanceof MP3FileFormat || format instanceof AIFFFileFormat) {
            audio.naudioFileName = playingFileName;
        } else {
            audio.naudioFileName = null;
        }
    }

    @Override
    public void ff() {
        if (audio.driverVirtual == null) return;
        vgmSpeed = (vgmSpeed == 1) ? 4 : 1;
        audio.driverVirtual.vgmSpeed = vgmSpeed;
        if (audio.driverReal != null) audio.driverReal.vgmSpeed = vgmSpeed;
    }

    public void slow() {
        vgmSpeed = (vgmSpeed == 1) ? 0.25 : 1;
        audio.driverVirtual.vgmSpeed = vgmSpeed;
        if (audio.driverReal != null) audio.driverReal.vgmSpeed = vgmSpeed;
    }

    public void resetSlow() {
        vgmSpeed = 1;
        audio.driverVirtual.vgmSpeed = vgmSpeed;
        if (audio.driverReal != null) audio.driverReal.vgmSpeed = vgmSpeed;
    }

    public boolean isStopped() {
        return audio.stopped;
    }

    public boolean isFadeOut() {
        return audio.vgmFadeout;
    }

    public boolean isSlow() {
        return !isStopped() && (vgmSpeed < 1.0);
    }

    public boolean isFF() {
        return !isStopped() && (vgmSpeed > 1.0);
    }

    public void stepPlay(int Step) {
        audio.stepCounter = Step;
    }

    public void closeWaveWriter() {
        audio.waveWriter.close();
    }

    @Override
    public void close() {
        stop();
        audio.close();
    }

    public void resetTimeCounter() {
        if (audio.driverVirtual == null && audio.driverReal == null) return;
        if (audio.driverVirtual != null) {
            audio.driverVirtual.counter = 0;
            audio.driverVirtual.totalCounter = 0;
            audio.driverVirtual.loopCounter = 0;
        }

        if (audio.driverReal != null) {
            audio.driverReal.counter = 0;
            audio.driverReal.totalCounter = 0;
            audio.driverReal.loopCounter = 0;
        }
    }

    public long getCounter() {
        if (audio.driverVirtual == null && audio.driverReal == null) return -1;

        if (audio.driverVirtual == null) return audio.driverReal.counter;
        if (audio.driverReal == null) return audio.driverVirtual.counter;

        return Math.max(audio.driverVirtual.counter, audio.driverReal.counter);
    }

    public long getTotalCounter() {
        if (audio.driverVirtual == null) return -1;

        return audio.driverVirtual.totalCounter;
    }

    public long getLoopCounter() {
        if (audio.driverVirtual == null) return -1;

        return audio.driverVirtual.loopCounter;
    }

    public void updateVol() {
        audio.chipRegister.updateVol();
    }

    public int getVgmCurLoopCounter() {
        int cnt = 0;

        if (audio.driverVirtual != null) {
            cnt = audio.driverVirtual.vgmCurLoop;
        }
        if (audio.driverReal != null) {
            cnt = Math.min(audio.driverReal.vgmCurLoop, cnt);
        }

        return cnt;
    }

    public boolean getVGMStopped() {
        boolean v;
        boolean r;

        v = audio.driverVirtual == null || audio.driverVirtual.stopped;
        r = audio.driverReal == null || audio.driverReal.stopped;
        return v && r;
    }

    public boolean getIsDataBlock(Common.EnmModel model) {

        if (model == Common.EnmModel.VirtualModel) {
            if (audio.driverVirtual == null) return false;
            return audio.driverVirtual.isDataBlock;
        } else {
            if (audio.driverReal == null) return false;
            return audio.driverReal.isDataBlock;
        }
    }

    public boolean getIsPcmRAMWrite(Common.EnmModel model) {
        if (model == Common.EnmModel.VirtualModel) {
            if (audio.driverVirtual == null) return false;
            if (!(audio.driverVirtual instanceof Vgm)) return false;
            return ((Vgm) audio.driverVirtual).isPcmRAMWrite;
        } else {
            if (audio.driverReal == null) return false;
            if (!(audio.driverReal instanceof Vgm)) return false;
            return ((Vgm) audio.driverReal).isPcmRAMWrite;
        }
    }

    public long getVirtualFrameCounter() {
        if (audio.driverVirtual == null) return -1;
        return audio.driverVirtual.vgmFrameCounter;
    }

    public long getRealFrameCounter() {
        if (audio.driverReal == null) return -1;
        return audio.driverReal.vgmFrameCounter;
    }
}
