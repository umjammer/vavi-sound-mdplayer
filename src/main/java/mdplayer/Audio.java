package mdplayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.driver.nrtdrv.NRTDRV;
import mdplayer.driver.nsf.Nsf;
import mdplayer.driver.sid.Sid;
import mdplayer.form.sys.frmMain;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.chips.GbSound;
import mdsound.chips.OotakeHuC6280;
import mdsound.chips.K051649;
import mdsound.chips.MultiPCM;
import mdsound.chips.OkiM6258;
import mdsound.chips.OkiM6295;
import mdsound.chips.PPZ8Status;
import mdsound.chips.PcmChip;
import mdsound.chips.Rf5c68;
import mdsound.chips.SegaPcm;
import mdsound.chips.YmF271;
import mdsound.instrument.*;
import mdsound.np.chip.DeviceInfo;
import vavi.util.ByteUtil;
import vavi.util.Debug;


public class Audio {

    private final Setting setting = Setting.getInstance();

    private static final Audio instance = new Audio();

    public BaseDriver driverVirtual = null;

    public mdsound.MDSound mds = null;

    public Set<Common.EnmChip> useChip = new HashSet<>();

    public ChipRegister chipRegister;

    public ChipLEDs chipLED = new ChipLEDs();
    public final VisVolume visVolume = new VisVolume();

    public static final int SamplingBuffer = 1024;

    public int clockAY8910 = 1789750;
    public static final int clockS5B = 1789772;
    public int clockK051649 = 1500000;
    public static final int clockC140 = 21390;
    public static final int clockPPZ8 = 44100;
    public int clockC352 = 24192000;
    public int clockFDS = 0;
    public int clockHuC6280 = 0;
    public int clockRF5C164 = 0;
    public int clockMMC5 = 0;
    public int clockNESDMC = 0;
    public int clockOKIM6258 = 0;
    public int clockOKIM6295 = 0;
    public int clockSegaPCM = 0;
    public int clockSN76489 = 0;
    public int clockYM2151 = 0;
    public int clockYM2203 = 0;
    public int clockYM2413 = 0;
    public int clockYM2608 = 0;
    public int clockYM2610 = 0;
    public int clockYM2612 = 0;
    public int clockYMF278B = 0;

    public String errMsg = "";

    protected short[] bufVirtualFunction_MIDIKeyboard = null;

    public FileFormat playingFileFormat;

    public String naudioFileName = null;
    public AudioInputStream naudioFileReader = null;
//    protected NWave.SampleProviders.SampleToWaveProvider16 naudioWs = null;
    protected byte[] naudioSrcbuffer = null;
    public int procTimePer1Frame = 0;
    public int stepCounter = 0;
    public double vgmFadeoutCounter;
    public double vgmFadeoutCounterV;

    public byte[] ensure(byte[] buffer, int bytesRequired) {
        if (buffer == null || buffer.length < bytesRequired) {
            buffer = new byte[bytesRequired];
        }
        return buffer;
    }

    protected void convert2ByteToShort(short[] destBuffer, int offset, byte[] source, int shortCount) {
        int samplesRead = shortCount;
        for (int n = 0; n < samplesRead; n++) {
            destBuffer[n] = ByteUtil.readLeShort(source, offset + n * Short.BYTES); // volume;
        }
    }

    public int nAudioRead(short[] buffer, int offset, int count) {
        try {
            naudioSrcbuffer = ensure(naudioSrcbuffer, count * 2);
//            naudioWs.read(naudioSrcbuffer, 0, count * 2);
            convert2ByteToShort(buffer, offset, naudioSrcbuffer, count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public int trdVgmVirtualFunction(short[] buffer, int offset, int sampleCount) {
//Debug.println(": " + sampleCount);
        //return nAaudioRead(buffer, offset, sampleCount);

        if (naudioFileReader != null) {
            if (trdClosed) {
                _trdStopped = true;
                //vgmFadeout = false;
                //Stopped = true;
            }
            return nAudioRead(buffer, offset, sampleCount);
        }

        int cnt = trdVgmVirtualMainFunction(buffer, offset, sampleCount);

        if (setting.getMidiKbd().getUseMIDIKeyboard()) {
            if (bufVirtualFunction_MIDIKeyboard == null || bufVirtualFunction_MIDIKeyboard.length < sampleCount) {
                bufVirtualFunction_MIDIKeyboard = new short[sampleCount];
            }
            mdsMIDI.update(bufVirtualFunction_MIDIKeyboard, 0, sampleCount, null);
            for (int i = 0; i < sampleCount; i++) {
                buffer[i + offset] += bufVirtualFunction_MIDIKeyboard[i];
            }
        }

        return cnt;
    }

    public int limit(int v, int max, int min) {
        return v > max ? max : Math.max(v, min);
    }

int CC;
    protected int trdVgmVirtualMainFunction(short[] buffer, int offset, int sampleCount) {
        if (buffer == null || buffer.length < 1 || sampleCount == 0) return 0;
        if (driverVirtual == null) return sampleCount;

        try {
            //stwh.Reset(); stwh.Start();

            int i;
            int cnt;
//if (CC++ > 100) { System.exit(1); }
//Debug.println("stop: " + stopped + ", " + hashCode());
            if (stopped || paused) {
                if (setting.getOther().getNonRenderingForPause()
                        || driverVirtual instanceof Nsf
                ) {
                    for (int d = offset; d < offset + sampleCount; d++) buffer[d] = 0;
                    return sampleCount;
                }

                int ret = mds.update(buffer, offset, sampleCount, null);
                return ret;
            }

            if (driverVirtual instanceof Nsf) {
//                driverVirtual.vstDelta = 0;
                cnt = ((Nsf) driverVirtual).render(buffer, sampleCount / 2, offset) * 2;
            } else if (driverVirtual instanceof Sid) {
//                driverVirtual.vstDelta = 0;
                cnt = ((Sid) driverVirtual).render(buffer, sampleCount);
            } else if (driverVirtual instanceof MXDRV) {
                mds.setIncFlag();
//                driverVirtual.vstDelta = 0;
                for (i = 0; i < sampleCount; i += 2) {
                    cnt = ((MXDRV) driverVirtual).render(buffer, offset + i, 2);
                    mds.update(buffer, offset + i, 2, null);
                }
                //cnt = (int)((MXDRV.MXDRV)driverVirtual).Render(buffer, offset , sampleCount);
                //mds.Update(buffer, offset , sampleCount, null);
                cnt = sampleCount;
            } else {
                if (hiyorimiNecessary && driverReal != null && driverReal.isDataBlock)
                    return mds.update(buffer, offset, sampleCount, null);

                if (stepCounter > 0) {
                    stepCounter -= sampleCount;
                    if (stepCounter <= 0) {
                        paused = true;
                        stepCounter = 0;
                        return mds.update(buffer, offset, sampleCount, null);
                    }
                }

//                driverVirtual.vstDelta = 0;
//                stwh.reset();
//                stwh.start();
//Debug.println("driver: " + driverVirtual.getClass().getSimpleName());
                cnt = mds.update(buffer, offset, sampleCount, driverVirtual::processOneFrame);
                procTimePer1Frame = (int) ((double) System.currentTimeMillis() / (sampleCount + 1) * 1000000.0);
            }
//Debug.println("sampleCount: " + sampleCount);

            // VST
//            vstMng.VST_Update(buffer, offset, sampleCount);

            for (i = 0; i < sampleCount; i++) {
                int mul = (int) (16384.0 * Math.pow(10.0, masterVolume / 40.0));
                buffer[offset + i] = (short) limit((buffer[offset + i] * mul) >> 13, 0x7fff, -0x8000);

                if (!vgmFadeout) continue;

                // フェードアウト処理
                buffer[offset + i] = (short) (buffer[offset + i] * vgmFadeoutCounter);

                vgmFadeoutCounter -= vgmFadeoutCounterV;
                if (vgmFadeoutCounterV >= 0.004 && vgmFadeoutCounterV != 0.1) {
                    vgmFadeoutCounterV = 0.004;
                }

                if (vgmFadeoutCounter < 0.0) {
                    vgmFadeoutCounter = 0.0;
                }

                // フェードアウト完了後、演奏を完全停止する
                if (vgmFadeoutCounter == 0.0) {
                    softReset(Common.EnmModel.VirtualModel);
                    softReset(Common.EnmModel.RealModel);

                    waveWriter.write(buffer, offset, i + 1);

                    waveWriter.close();

                    if (mds == null)
                        mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), SamplingBuffer, null);
                    else
                        mds.init(setting.getOutputDevice().getSampleRate(), SamplingBuffer, null);


                    chipRegister.close();

                    //Thread.sleep(500); // noise対策

                    stopped = true;
Debug.println("stop: " + stopped);

                    // 1frame当たりの処理時間
                    //procTimePer1Frame = (int)((double)stwh.ElapsedMilliseconds / (i + 1) * 1000000.0);
                    return i + 1;
                }
            }

            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                updateVisualVolume(buffer, offset);
            }

            waveWriter.write(buffer, offset, sampleCount);

            // //1frame当たりの処理時間
            //procTimePer1Frame = (int)((double)stwh.ElapsedMilliseconds / sampleCount * 1000000.0);
            return cnt;

        } catch (Exception ex) {
            ex.printStackTrace();
            _fatalError = true;
            stopped = true;
        }

        return -1;
    }

    protected void naudioWrapPlaybackStopped(LineEvent e) {
//        if (e.getException != null) {
//            JOptionPane.showMessageDialog(null,
//                    String.format("デバイスが何らかの原因で停止しました。\nメッセージ:\n%s\nスタックトレース:\n%s"
//                            , e.Exception.Message
//                            , e.Exception.StackTrace)
//                    , "エラー"
//                    , JOptionPane.ERROR_MESSAGE);
//            flgReinit = true;
//
//            try {
//                naudioWrap.Stop();
//            } catch (Exception ex) {
//                Log.forcedWrite(ex);
//            }
//
//        } else {
        try {
            stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        }
    }

    public void stop() {

        try {
            if (paused) pause();

            if (stopped) {
                trdClosed = true;
                while (!_trdStopped) {
                    Thread.sleep(1);
                }

                if (playingFileFormat != null && !playingFileFormat.isSampled()
                        && naudioFileReader != null) {
                    nAudioStop();
                }

                return;
            }

            if (!paused) {
                LineEvent.Type ps = naudioWrap.getPlaybackState();
                if (ps != null && ps != LineEvent.Type.STOP) {
                    vgmFadeoutCounterV = 0.1;
                    vgmFadeout = true;
                    int cnt = 0;
                    while (!stopped && cnt < 100) {
                        Thread.yield();
                        cnt++;
                    }
                }
            }
            trdClosed = true;

            if (naudioFileReader != null) {
                nAudioStop();
                return;
            }

            softReset(Common.EnmModel.VirtualModel);
            softReset(Common.EnmModel.RealModel);

            int timeout = 5000;
            while (!_trdStopped) {
                Thread.yield();
                timeout--;
                if (timeout < 1) break;
            }
            while (!stopped) {
                Thread.yield();
                timeout--;
                if (timeout < 1) break;
            }
            stopped = true;
new Exception().printStackTrace();
Debug.println("stop: " + stopped + ", " + hashCode());

            softReset(Common.EnmModel.VirtualModel);
            softReset(Common.EnmModel.RealModel);

            //chipRegister.outMIDIData_Close();
            if (setting.getOther().getWavSwitch()) {
                Thread.sleep(500);
                waveWriter.close();
            }

            // DEBUG
            //vstparse();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void nAudioStop() {
        try {
            AudioInputStream dmy = naudioFileReader;
            naudioFileReader = null;
            dmy.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Audio() {
        Debug.println("Audio:Init:STEP 01");

        naudioWrap = new NAudioWrap(setting.getOutputDevice().getSampleRate(), this::trdVgmVirtualFunction);
        naudioWrap.playbackStopped = this::naudioWrapPlaybackStopped;

        if (mds == null)
            mds = new MDSound(setting.getOutputDevice().getSampleRate(), SamplingBuffer, null);
        else
            mds.init(setting.getOutputDevice().getSampleRate(), SamplingBuffer, null);

        chipRegister = new ChipRegister(
                mds
//                , SoundChip.realChip
//                , vstMng
//                , SoundChip.scYM2612
//                , SoundChip.scSN76489
//                , SoundChip.scYM2608
//                , SoundChip.scYM2151
//                , SoundChip.scYM2203
//                , SoundChip.scYM2413
//                , SoundChip.scYM2610
//                , SoundChip.scYM2610EA
//                , SoundChip.scYM2610EB
//                , SoundChip.scYM3526
//                , SoundChip.scYM3812
//                , SoundChip.scYMF262
//                , SoundChip.scC140
//                , SoundChip.scSEGAPCM
//                , SoundChip.scAY8910
//                , SoundChip.scK051649
        );
        chipRegister.initChipRegister(null);

        //
        List<MDSound.Chip> lstChips = new ArrayList<>();
        MDSound.Chip chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new Ym2612Inst();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume("MAIN", Ym2612Inst.class);
        chip.clock = 7670454;
        chip.option = null;
        chipLED.put("PriOPN2", 1);
        lstChips.add(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = new Sn76489Inst();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume("MAIN", Sn76489Inst.class);
        chip.clock = 3579545;
        chip.option = null;
        chipLED.put("PriDCSG", 1);
        lstChips.add(chip);

        if (mdsMIDI == null)
            mdsMIDI = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
        else
            mdsMIDI.init(setting.getOutputDevice().getSampleRate(), SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

        // midi  のインスタンスを作成
        makeMIDIout(setting, 1);
        chipRegister.resetAllMIDIout();
    }

    public static Audio getInstance() {
        return instance;
    }

    public frmMain frmMain = null;
//    public static final VstMng vstMng = new VstMng();

//#region MIDI

    public mdsound.MDSound mdsMIDI = null;
    public final List<Receiver> midiOuts = new ArrayList<>();
    public final List<Integer> midiOutsType = new ArrayList<>();

    public void makeMIDIout(Setting setting, int m) {
        if (setting.getMidiOut().getMidiOutInfos() == null || setting.getMidiOut().getMidiOutInfos().size() < 1)
            return;
        if (setting.getMidiOut().getMidiOutInfos().get(m) == null || setting.getMidiOut().getMidiOutInfos().get(m).length < 1)
            return;

        for (int i = 0; i < setting.getMidiOut().getMidiOutInfos().get(m).length; i++) {
            int n = -1;
            int t = 0;
            Receiver mo = null;

            MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
            int j = 0;
            for (var info : midiDeviceInfos) {
                MidiDevice device;
                try {
                    device = MidiSystem.getMidiDevice(info);
                } catch (MidiUnavailableException e) {
                    throw new RuntimeException(e);
                }
                if (device.getMaxReceivers() == 0) {
                    continue;
                }
                if (!setting.getMidiOut().getMidiOutInfos().get(m)[i].name.equals(info.getName()))
                    continue;

                n = j++;
                t = setting.getMidiOut().getMidiOutInfos().get(m)[i].type;
                break;
            }

            if (n != -1) {
                try {
                    mo = MidiSystem.getReceiver();
                } catch (Exception e) {
                    e.printStackTrace();
                    mo = null;
                }
            }

//            if (n == -1) {
//                vstMng.SetupVstMidiOut(setting.getMidiOut().getMidiOutInfos().get(m)[i]);
//            }

            if (mo != null) {
                midiOuts.add(mo);
                midiOutsType.add(t);
            }
        }
    }

    public void releaseAllMIDIout() {
        if (midiOuts.size() > 0) {
            for (int i = 0; i < midiOuts.size(); i++) {
                if (midiOuts.get(i) != null) {
                    midiOuts.get(i).close();
                    midiOuts.set(i, null);
                }
            }
            midiOuts.clear();
            midiOutsType.clear();
        }

//        vstMng.ReleaseAllMIDIout();
    }

    public NAudioWrap naudioWrap;
    public WaveWriter waveWriter = null;

    public void close() {
        try {
            // midi outをリリース
            if (midiOuts.size() > 0) {
                for (int i = 0; i < midiOuts.size(); i++) {
                    if (midiOuts.get(i) != null) {
                        midiOuts.get(i).close();
                        midiOuts.set(i, null);
                    }
                }
                midiOuts.clear();
                midiOutsType.clear();
            }

//            vstMng.ReleaseAllMIDIout();
//            vstMng.Close();

//            SoundChip.realChip = null;

            naudioWrap.stop();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//#endregion

    protected final Object lockObj = new Object();
    public boolean _fatalError = false;

    public boolean getFatalError() {
        synchronized (lockObj) {
            return _fatalError;
        }
    }

    public void setFatalError(boolean value) {
        synchronized (lockObj) {
            _fatalError = value;
        }
    }
    public Thread trdMain = null;
    public boolean trdClosed = false;
    public boolean _trdStopped = true;

    public boolean getTrdStopped() {
        synchronized (lockObj) {
            return _trdStopped;
        }
    }

    void setTrdStopped(boolean value) {
        synchronized (lockObj) {
            _trdStopped = value;
        }
    }

    public Object getSIDRegister(int chipId) {
        return chipRegister.getSIDRegister(chipId);
    }

    public Sid getCurrentSIDContext() {
        return chipRegister.SID;
    }

    public OkiM6295.ChannelInfo getOKIM6295Info(int chipId) {
        return chipRegister.getOKIM6295Info(chipId);
    }

    public void updateVisualVolume(short[] buffer, int offset) {
        visVolume.master = buffer[offset];

        for (var i : mds.getFirstInstruments()) {
            var vs = i.getView("volume", null);
        }
    }

    public void clearFadeoutVolume() {
        chipRegister.setFadeoutVolYM2203(0, 0);
        chipRegister.setFadeoutVolYM2203(1, 0);
        chipRegister.setFadeoutVolAY8910(0, 0);
        chipRegister.setFadeoutVolAY8910(1, 0);
        chipRegister.setFadeoutVolYM2413(0, 0);
        chipRegister.setFadeoutVolYM2413(1, 0);
        chipRegister.setFadeoutVolYM2608(0, 0);
        chipRegister.setFadeoutVolYM2608(1, 0);
        chipRegister.setFadeoutVolYM2151(0, 0);
        chipRegister.setFadeoutVolYM2151(1, 0);
        chipRegister.setFadeoutVolYM2612(0, 0);
        chipRegister.setFadeoutVolYM2612(1, 0);
        chipRegister.setFadeoutVolSN76489( 0, 0);
        chipRegister.setFadeoutVolSN76489( 1, 0);
        chipRegister.setFadeoutVolYM3526(0, 0);
        chipRegister.setFadeoutVolYM3526(1, 0);
        chipRegister.setFadeoutVolYM3812(0, 0);
        chipRegister.setFadeoutVolYM3812(1, 0);
        chipRegister.setFadeoutVolYMF262(0, 0);
        chipRegister.setFadeoutVolYMF262(1, 0);
    }

    public int vgmRealFadeoutVol = 0;
    public int vgmRealFadeoutVolWait = 4;

    public int hiyorimiEven = 0;
    public boolean hiyorimiNecessary = false;

    public BaseDriver driverReal = null;

    protected long sw = System.currentTimeMillis();
    public static final double swFreq = 1000d / 44100;

    public void softReset(Common.EnmModel model) {
        chipRegister.softResetYM2203(0, model);
        chipRegister.softResetYM2203(1, model);
        chipRegister.softResetAY8910(0, model);
        chipRegister.softResetAY8910(1, model);
        chipRegister.softResetYM2413(0, model);
        chipRegister.softResetYM2413(1, model);
        chipRegister.softResetYM2608(0, model);
        chipRegister.softResetYM2608(1, model);
        chipRegister.softResetYM2151(0, model);
        chipRegister.softResetYM2151(1, model);
        chipRegister.softResetYM3526(0, model);
        chipRegister.softResetYM3526(1, model);
        chipRegister.softResetYM3812(0, model);
        chipRegister.softResetYM3812(1, model);
        chipRegister.softResetYMF262(0, model);
        chipRegister.softResetYMF262(1, model);
        chipRegister.softResetK051649(0, model);
        chipRegister.softResetK051649(1, model);
        chipRegister.softResetMIDI(0, model);
        chipRegister.softResetMIDI(1, model);

//        if (model == EnmModel.RealModel && SoundChip.realChip != null) {
//            SoundChip.realChip.SendData();
//        }
    }

//#region register

    public int[][] getFMRegister(int chipId) {
        return chipRegister.fmRegisterYM2612[chipId];
    }

    public int[][] getYM2612MIDIRegister() {
        return mdsMIDI.ReadYm2612Register(0, 0);
    }

    public int[] getYM2151Register(int chipId) {
        return chipRegister.fmRegisterYM2151[chipId];
    }

    public int[] getYm2203Register(int chipId) {
        return chipRegister.fmRegisterYM2203[chipId];
    }

    public int[] getYM2413Register(int chipId) {
        return chipRegister.fmRegisterYM2413[chipId];
    }

    public DeviceInfo.TrackInfo[] getVRC6Register(int chipId) {
        return chipRegister.getVRC6Register(chipId);
    }

    public byte[] getVRC7Register(int chipId) {
        return chipRegister.getVRC7Register(chipId);
    }

    public DeviceInfo.TrackInfo[] getN106Register(int chipId) {
        return chipRegister.getN106Register(chipId);
    }

    public int[][] getYM2608Register(int chipId) {
        return chipRegister.fmRegisterYM2608[chipId];
    }

    public int[][] getYM2610Register(int chipId) {
        return chipRegister.fmRegisterYM2610[chipId];
    }

    public int[] getYM3526Register(int chipId) {
        return chipRegister.fmRegisterYM3526[chipId];
    }

    public int[] getY8950Register(int chipId) {
        return chipRegister.fmRegisterY8950[chipId];
    }

    public int[] getYM3812Register(int chipId) {
        return chipRegister.fmRegisterYM3812[chipId];
    }

    public int[][] getYMF262Register(int chipId) {
        return chipRegister.fmRegisterYMF262[chipId];
    }

    public int[][] getYMF278BRegister(int chipId) {
        return chipRegister.fmRegisterYMF278B[chipId];
    }

    public int[] getPSGRegister(int chipId) {
        return chipRegister.sn76489Register[chipId];
    }

    public int getPSGRegisterGGPanning(int chipId) {
        return chipRegister.sn76489RegisterGGPan[chipId];
    }

    public int[] getAY8910Register(int chipId) {
        return chipRegister.psgRegisterAY8910[chipId];
    }

    public OotakeHuC6280 getHuC6280Register(int chipId) {
        return mds.ReadOotakePsgStatus(chipId);
    }

    public K051649 getK051649Register(int chipId) {
        return chipRegister.scc_k051649.GetK051649_State(chipId);
    }

    public MIDIParam getMIDIInfos(int chipId) {
        return chipRegister.midiParams[chipId];
    }

    public PcmChip getRf5c164Register(int chipId) {
        return mds.ReadRf5c164Register(chipId);
    }

    public Rf5c68 getRf5c68Register(int chipId) {
        return mds.ReadRf5c68Register(chipId);
    }

    public YmF271 getYMF271Register(int chipId) {
        return mds.ReadYmf271Register(chipId);
    }

    public byte[] getC140Register(int chipId) {
        return chipRegister.pcmRegisterC140[chipId];
    }

    public PPZ8Status.Channel[] getPPZ8Register(int chipId) {
        return chipRegister.getPPZ8Register(chipId);
    }

    public boolean[] getC140KeyOn(int chipId) {
        return chipRegister.pcmKeyOnC140[chipId];
    }

    public int[] getYMZ280BRegister(int chipId) {
        return chipRegister.YMZ280BRegister[chipId];
    }

    public int[] getC352Register(int chipId) {
        return chipRegister.pcmRegisterC352[chipId];
    }

    public MultiPCM getMultiPCMRegister(int chipId) {
        return chipRegister.getMultiPCMRegister(chipId);
    }

    public int[] getC352KeyOn(int chipId) {
        return chipRegister.readC352(chipId);
    }

    public int[] getQSoundRegister(int chipId) {
        return chipRegister.getQSoundRegister(chipId);
    }

    public byte[] getSEGAPCMRegister(int chipId) {
        return chipRegister.pcmRegisterSEGAPCM[chipId];
    }

    public boolean[] getSEGAPCMKeyOn(int chipId) {
        return chipRegister.pcmKeyOnSEGAPCM[chipId];
    }

    public OkiM6258 getOKIM6258Register(int chipId) {
        return mds.ReadOkiM6258Status(chipId);
    }

    public SegaPcm getSegaPCMRegister(int chipId) {
        return mds.ReadSegaPCMStatus(chipId);
    }

    public byte[] getAPURegister(int chipId) {
        byte[] reg;

        // nsf向け
        if (chipRegister == null) reg = null;
        else if (chipRegister.nes_apu == null) reg = null;
        else if (chipRegister.nes_apu.apu == null) reg = null;
        else if (chipId == 1) reg = null;
        else reg = chipRegister.nes_apu.apu.reg;

        // vgm向け
        if (reg == null) reg = chipRegister.getNESRegisterAPU(chipId, Common.EnmModel.VirtualModel);

        return reg;
    }

    public byte[] getDMCRegister(int chipId) {
        byte[] reg;
        try {
            // nsf向け
            if (chipRegister == null) reg = null;
            else if (chipRegister.nes_apu == null) reg = null;
            else if (chipRegister.nes_apu.apu == null) reg = null;
            else if (chipId == 1) reg = null;
            else reg = chipRegister.nes_dmc.dmc.reg;

            // vgm向け
            if (reg == null) reg = chipRegister.getNESRegisterDMC(chipId, Common.EnmModel.VirtualModel);

            return reg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public mdsound.np.NpNesFds getFDSRegister(int chipId) {
        mdsound.np.NpNesFds reg;

        // nsf向け
        if (chipRegister == null) reg = null;
        else if (chipRegister.nes_apu == null) reg = null;
        else if (chipRegister.nes_apu.apu == null) reg = null;
        else if (chipId == 1) reg = null;
        else reg = chipRegister.nes_fds.fds;

        // vgm向け
        if (reg == null) reg = chipRegister.getFDSRegister(chipId, Common.EnmModel.VirtualModel);

        return reg;
    }

    protected final byte[] s5bregs = new byte[0x20];

    public byte[] getS5BRegister(int chipId) {
        // nsf 向け
        if (chipRegister == null) return null;
        else if (chipRegister.nes_fme7 == null) return null;
        else if (chipId == 1) return null;

        int[] dat = new int[] { 0 };
        for (int adr = 0x00; adr < 0x20; adr++) {
            chipRegister.nes_fme7.read(adr, dat);
            s5bregs[adr] = (byte) dat[0];
        }

        return s5bregs;
    }

    public GbSound getDMGRegister(int chipId) {
        if (mds == null) return null;
        else if (chipId == 1) return null;

        return mds.ReadGb(chipId);
    }

    protected final byte[] mmc5regs = new byte[10];

    public byte[] getMMC5Register(int chipId) {
        // nsf 向け
        if (chipRegister == null) return null;
        else if (chipRegister.nes_mmc5 == null) return null;
        else if (chipId == 1) return null;

        int[] dat = new int[] { 0 };
        for (int adr = 0x5000; adr < 0x5008; adr++) {
            chipRegister.nes_mmc5.read(adr, dat);
            mmc5regs[adr & 0x7] = (byte) dat[0];
        }

        chipRegister.nes_mmc5.read(0x5010, dat);
        mmc5regs[8] = (byte) (chipRegister.nes_mmc5.pcmMode ? 1 : 0);
        mmc5regs[9] = chipRegister.nes_mmc5.pcm;

        return mmc5regs;
    }

//#endregion

//#region key on

    public int[] getFMKeyOn(int chipId) {
        return chipRegister.fmKeyOnYM2612[chipId];
    }

    public int[] getYM2151KeyOn(int chipId) {
        return chipRegister.fmKeyOnYM2151[chipId];
    }

    public boolean getOKIM6258KeyOn(int chipId) {
        return chipRegister.okim6258Keyon[chipId];
    }

    public void resetOKIM6258KeyOn(int chipId) {
        chipRegister.okim6258Keyon[chipId] = false;
    }

    public int getYM2151PMD(int chipId) {
        return chipRegister.fmPMDYM2151[chipId];
    }

    public int getYM2151AMD(int chipId) {
        return chipRegister.fmAMDYM2151[chipId];
    }

    public int[] getYM2608KeyOn(int chipId) {
        return chipRegister.fmKeyOnYM2608[chipId];
    }

    public int[] getYM2610KeyOn(int chipId) {
        return chipRegister.fmKeyOnYM2610[chipId];
    }

    public int[] getYM2203KeyOn(int chipId) {
        return chipRegister.fmKeyOnYM2203[chipId];
    }

    public int getYMF262FMKeyON(int chipId) {
        return chipRegister.getYMF262FMKeyON(chipId);
    }

    public int getYMF262RyhthmKeyON(int chipId) {
        return chipRegister.getYMF262RyhthmKeyON(chipId);
    }

    public int getYMF278BFMKeyON(int chipId) {
        return chipRegister.getYMF278BFMKeyON(chipId);
    }

    public void resetYMF278BFMKeyON(int chipId) {
        chipRegister.resetYMF278BFMKeyON(chipId);
    }

    public int getYMF278BRyhthmKeyON(int chipId) {
        return chipRegister.getYMF278BRyhthmKeyON(chipId);
    }

    public void resetYMF278BRyhthmKeyON(int chipId) {
        chipRegister.resetYMF278BRyhthmKeyON(chipId);
    }

    public int[] getYMF278BPCMKeyON(int chipId) {
        return chipRegister.getYMF278BPCMKeyON(chipId);
    }

    public void resetYMF278BPCMKeyON(int chipId) {
        chipRegister.resetYMF278BPCMKeyON(chipId);
    }

//#endregion

//#region key info

    public mdplayer.ChipRegister.ChipKeyInfo getYM2413KeyInfo(int chipId) {
        return chipRegister.getYM2413KeyInfo(chipId);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getYM3526KeyInfo(int chipId) {
        return chipRegister.getYM3526KeyInfo(chipId);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getY8950KeyInfo(int chipId) {
        return chipRegister.getY8950KeyInfo(chipId);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getYM3812KeyInfo(int chipId) {
        return chipRegister.getYM3812KeyInfo(chipId);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getVRC7KeyInfo(int chipId) {
        return chipRegister.getVRC7KeyInfo(chipId);
    }

//#endregion

//#region volume

    public int[] getFMVolume(int chipId) {
        return chipRegister.getYM2612Volume(chipId);
    }

    public int[] getYM2151Volume(int chipId) {
        return chipRegister.getYM2151Volume(chipId);
    }

    public int[] getYM2608Volume(int chipId) {
        return chipRegister.getYM2608Volume(chipId);
    }

    public int[][] getYM2608RhythmVolume(int chipId) {
        return chipRegister.getYM2608RhythmVolume(chipId);
    }

    public int[] getYM2608AdpcmVolume(int chipId) {
        return chipRegister.getYM2608AdpcmVolume(chipId);
    }

    public int[] getYM2610Volume(int chipId) {
        return chipRegister.getYM2610Volume(chipId);
    }

    public int[][] getYM2610RhythmVolume(int chipId) {
        return chipRegister.getYM2610RhythmVolume(chipId);
    }

    public int[] getYM2610AdpcmVolume(int chipId) {
        return chipRegister.getYM2610AdpcmVolume(chipId);
    }

    public int[] getYM2203Volume(int chipId) {
        return chipRegister.getYM2203Volume(chipId);
    }

    public int[] getFMCh3SlotVolume(int chipId) {
        return chipRegister.getYM2612Ch3SlotVolume(chipId);
    }

    public int[] getYM2608Ch3SlotVolume(int chipId) {
        return chipRegister.getYM2608Ch3SlotVolume(chipId);
    }

    public int[] getYM2610Ch3SlotVolume(int chipId) {
        return chipRegister.getYM2610Ch3SlotVolume(chipId);
    }

    public int[] getYM2203Ch3SlotVolume(int chipId) {
        return chipRegister.getYM2203Ch3SlotVolume(chipId);
    }

    public int[][] getPSGVolume(int chipId) {
        return chipRegister.getPSGVolume(chipId);
    }

//#endregion

//#region mask

    public void setRF5C164Mask(int chipId, int ch) {
        chipRegister.setMaskRF5C164(chipId, ch, true);
    }

    public void setRF5C68Mask(int chipId, int ch) {
        chipRegister.setMaskRF5C68(chipId, ch, true);
    }

    public void setSN76489Mask(int chipId, int ch) {
        chipRegister.setMaskSN76489(chipId, ch, true);
        sn76489ForcedSendVolume(chipId, ch);
    }

    public void resetSN76489Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskSN76489(chipId, ch, false);
            sn76489ForcedSendVolume(chipId, ch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sn76489ForcedSendVolume(int chipId, int ch) {
        Setting.ChipType2 ct = setting.getSN76489Type()[chipId];
        chipRegister.setSN76489Register(chipId
                , (byte) (0x90
                        | ((ch & 3) << 5)
                        | (15 - (Math.max(chipRegister.sn76489Vol[chipId][ch][0], chipRegister.sn76489Vol[chipId][ch][1]) & 0xf)))
                , ct.getUseEmu()[0] ? Common.EnmModel.VirtualModel : Common.EnmModel.RealModel);
    }

    public void setYM2151Mask(int chipId, int ch) {
        chipRegister.setMaskYM2151(chipId, ch, true, false);
    }

    public void setYM2203Mask(int chipId, int ch) {
        chipRegister.setMaskYM2203(chipId, ch, true, false);
    }

    public void setYM2413Mask(int chipId, int ch) {
        chipRegister.setMaskYM2413(chipId, ch, true);
    }

    public void setYM2608Mask(int chipId, int ch) {
        chipRegister.setMaskYM2608(chipId, ch, true, false);
    }

    public void setYM2610Mask(int chipId, int ch) {
        chipRegister.setMaskYM2610(chipId, ch, true);
    }

    public void setYM2612Mask(int chipId, int ch) {
        chipRegister.setMaskYM2612(chipId, ch, true);
    }

    public void setYM3526Mask(int chipId, int ch) {
        chipRegister.setMaskYM3526(chipId, ch, true);
    }

    public void setY8950Mask(int chipId, int ch) {
        chipRegister.setMaskY8950(chipId, ch, true);
    }

    public void setYM3812Mask(int chipId, int ch) {
        chipRegister.setMaskYM3812(chipId, ch, true);
    }

    public void setYMF262Mask(int chipId, int ch) {
        chipRegister.setMaskYMF262(chipId, ch, true);
    }

    public void setYMF278BMask(int chipId, int ch) {
        chipRegister.setMaskYMF278B(chipId, ch, true);
    }

    public void setC140Mask(int chipId, int ch) {
        chipRegister.setMaskC140(chipId, ch, true);
    }

    public void setPPZ8Mask(int chipId, int ch) {
        chipRegister.setMaskPPZ8(chipId, ch, true);
    }

    public void setC352Mask(int chipId, int ch) {
        chipRegister.setMaskC352(chipId, ch, true);
    }

    public void setSegaPCMMask(int chipId, int ch) {
        chipRegister.setMaskSegaPCM(chipId, ch, true);
    }

    public void setQSoundMask(int chipId, int ch) {
        chipRegister.setMaskQSound(chipId, ch, true);
    }

    public void setAY8910Mask(int chipId, int ch) {
        chipRegister.setMaskAY8910(chipId, ch, true);
    }

    public void setHuC6280Mask(int chipId, int ch) {
        chipRegister.setMaskHuC6280(chipId, ch, true);
    }

    public void setOKIM6258Mask(int chipId) {
        chipRegister.setMaskOKIM6258(chipId, true);
    }

    public void setOKIM6295Mask(int chipId, int ch) {
        chipRegister.setMaskOKIM6295(chipId, ch, true);
    }

    public void resetOKIM6295Mask(int chipId, int ch) {
        chipRegister.setMaskOKIM6295(chipId, ch, false);
    }

    public void setNESMask(int chipId, int ch) {
        chipRegister.setNESMask(chipId, ch);
    }

    public void setDMCMask(int chipId, int ch) {
        chipRegister.setNESMask(chipId, ch + 2);
    }

    public void setFDSMask(int chipId) {
        chipRegister.setFDSMask(chipId);
    }

    public void setMMC5Mask(int chipId, int ch) {
        chipRegister.setMMC5Mask(chipId, ch);
    }

    public void setVRC7Mask(int chipId, int ch) {
        chipRegister.setVRC7Mask(chipId, ch);
    }

    public void setK051649Mask(int chipId, int ch) {
        chipRegister.setK051649Mask(chipId, ch);
    }

    public void setDMGMask(int chipId, int ch) {
        chipRegister.setDMGMask(chipId, ch);
    }

    public void setVRC6Mask(int chipId, int ch) {
        chipRegister.setVRC6Mask(chipId, ch);
    }

    public void setN163Mask(int chipId, int ch) {
        chipRegister.setN163Mask(chipId, ch);
    }


    public void resetOKIM6258Mask(int chipId) {
        chipRegister.setMaskOKIM6258(chipId, false);
    }

    public void resetYM2612Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM2612(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2203Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM2203(chipId, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2413Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM2413(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetRF5C164Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskRF5C164(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetRF5C68Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskRF5C68(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean stopped = false;

    public void resetYM2151Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM2151(chipId, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2608Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM2608(chipId, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2610Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM2610(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM3526Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM3526(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetY8950Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskY8950(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM3812Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYM3812(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYMF262Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskYMF262(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYMF278BMask(int chipId, int ch) {
        try {
            chipRegister.setMaskYMF278B(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetC140Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskC140(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetPPZ8Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskPPZ8(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetC352Mask(int chipId, int ch) {
        try {
            chipRegister.setMaskC352(chipId, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSegaPCMMask(int chipId, int ch) {
        chipRegister.setMaskSegaPCM(chipId, ch, false);
    }

    public void resetQSoundMask(int chipId, int ch) {
        chipRegister.setMaskQSound(chipId, ch, false);
    }

    public void resetAY8910Mask(int chipId, int ch) {
        chipRegister.setMaskAY8910(chipId, ch, false);
    }

    public void resetHuC6280Mask(int chipId, int ch) {
        chipRegister.setMaskHuC6280(chipId, ch, false);
    }

    public void resetNESMask(int chipId, int ch) {
        chipRegister.resetNESMask(chipId, ch);
    }

    public void resetDMCMask(int chipId, int ch) {
        chipRegister.resetNESMask(chipId, ch + 2);
    }

    public void resetFDSMask(int chipId) {
        chipRegister.resetFDSMask(chipId);
    }

    public void resetMMC5Mask(int chipId, int ch) {
        chipRegister.resetMMC5Mask(chipId, ch);
    }

    public void resetVRC7Mask(int chipId, int ch) {
        chipRegister.resetVRC7Mask(chipId, ch);
    }

    public void resetK051649Mask(int chipId, int ch) {
        chipRegister.resetK051649Mask(chipId, ch);
    }

    public void resetDMGMask(int chipId, int ch) {
        chipRegister.resetDMGMask(chipId, ch);
    }

    public void resetVRC6Mask(int chipId, int ch) {
        chipRegister.resetVRC6Mask(chipId, ch);
    }

    public void resetN163Mask(int chipId, int ch) {
        chipRegister.resetN163Mask(chipId, ch);
    }

//#endregion

    public void setVolume(String tag, Class<? extends Instrument> c, boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getVolume(tag, c)) + volume, -192, 20);
            mds.setVolume(tag, c, v);
            setting.getBalance().setVolume(tag, c, v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int masterVolume = 0;

    public void setMasterVolume(boolean isAbs, int volume) {
        masterVolume = Common.range((isAbs ? 0 : setting.getBalance().getMasterVolume()) + volume, -192, 20);
        setting.getBalance().setMasterVolume(masterVolume);
    }


    public void setGimicOPNVolume(boolean isAbs, int volume) {
        setting.getBalance().setGimicOPNVolume(Common.range((isAbs ? 0 : setting.getBalance().getGimicOPNVolume()) + volume, 0, 127));
    }

    public void setGimicOPNAVolume(boolean isAbs, int volume) {
        setting.getBalance().setGimicOPNAVolume(Common.range((isAbs ? 0 : setting.getBalance().getGimicOPNAVolume()) + volume, 0, 127));
    }

    public MDSound.Chip getMDSChipInfo(Class<? extends Instrument> typ) {
        return chipRegister.getChipInfo(typ);
    }

    public boolean sn76489NGPFlag = false;

    public boolean getSn76489NGPFlag() {
        return sn76489NGPFlag;
    }

    public void getPlayingFileName(String playingFileName, String playingArcFileName) {
        playingFileName = playingFileName;
        playingArcFileName = playingArcFileName;
    }

    public int[] getMoonDriverPCMKeyOn() {
        if (driverVirtual instanceof MoonDriver) {
            return ((MoonDriver) driverVirtual).getPCMKeyOn();
        }
        return null;
    }

    public Vgm.Gd3 getGd3() {
        if (driverVirtual != null) return driverVirtual.gd3;
        return null;
    }

    public long getDriverCounter() {
        if (driverVirtual == null && driverReal == null) return -1;


        if (driverVirtual == null) {
            if (driverReal instanceof NRTDRV) return ((NRTDRV) driverReal).work.totalCount;
            else if (driverReal instanceof Vgm) return driverReal.vgmFrameCounter;
            else return 0;
        }
        if (driverReal == null) {
            if (driverVirtual instanceof NRTDRV) return ((NRTDRV) driverVirtual).work.totalCount;
            else if (driverVirtual instanceof Vgm) return driverVirtual.vgmFrameCounter;
            else return 0;
        }

        if (driverVirtual instanceof NRTDRV && driverReal instanceof NRTDRV) {
            return ((NRTDRV) driverVirtual).work.totalCount > ((NRTDRV) driverReal).work.totalCount ? ((NRTDRV) driverVirtual).work.totalCount : ((NRTDRV) driverReal).work.totalCount;
        } else if (driverVirtual instanceof Vgm && driverReal instanceof Vgm) {
            return Math.max(driverVirtual.vgmFrameCounter, driverReal.vgmFrameCounter);
        } else {
            return 0;
        }
    }

    public void copyWaveBuffer(short[][] dest) {
        if (driverVirtual instanceof Nsf) {
            ((Nsf) driverVirtual).visWaveBufferCopy(dest);
            return;
        } else if (driverVirtual instanceof Sid) {
            ((Sid) driverVirtual).visWaveBufferCopy(dest);
            return;
        }

        if (mds == null) return;
        mds.visWaveBuffer.copy(dest);
    }

    public boolean paused = false;
    public boolean vgmFadeout;

    public void fadeoutCommand() {
        if (isPaused()) {
            pause();
        }

        fadeout();
    }

    public void pause() {

        try {
            paused = !paused;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void fadeout() {
        vgmFadeout = true;
    }
}
