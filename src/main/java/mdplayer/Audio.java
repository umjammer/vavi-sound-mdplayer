package mdplayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.driver.nrtdrv.NRTDRV;
import mdplayer.driver.nsf.Nsf;
import mdplayer.driver.sid.Sid;
import mdplayer.form.sys.frmMain;
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
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;
import mdsound.np.chip.DeviceInfo;


public class Audio {

    private Setting setting = Setting.getInstance();

    private static Audio instance = new Audio();

    public BaseDriver driverVirtual = null;

    public mdsound.MDSound mds = null;

    public Set<Common.EnmChip> useChip = new HashSet<>();

    public ChipRegister chipRegister = null;

    public ChipLEDs chipLED = new ChipLEDs();
    public final VisVolume visVolume = new VisVolume();

    public final int SamplingBuffer = 1024;

    public int clockAY8910 = 1789750;
    public final int clockS5B = 1789772;
    public int clockK051649 = 1500000;
    public final int clockC140 = 21390;
    public final int clockPPZ8 = 44100;
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

    private Audio() {
        // midi  のインスタンスを作成
        makeMIDIout(setting, 1);
        chipRegister.resetAllMIDIout();

        //
        List<MDSound.Chip> lstChips = new ArrayList<>();
        MDSound.Chip chip;

        chip = new MDSound.Chip();
        chip.id = (byte) 0;
        chip.instrument = new Ym2612Inst();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getYM2612Volume();
        chip.clock = 7670454;
        chip.option = null;
        chipLED.put("PriOPN2", 1);
        lstChips.add(chip);

        chip = new MDSound.Chip();
        chip.id = (byte) 0;
        chip.instrument = new Sn76489Inst();
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getSN76489Volume();
        chip.clock = 3579545;
        chip.option = null;
        chipLED.put("PriDCSG", 1);
        lstChips.add(chip);

        if (mdsMIDI == null)
            mdsMIDI = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
        else
            mdsMIDI.init(setting.getOutputDevice().getSampleRate(), SamplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
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
                MidiDevice device = null;
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

            naudioWrap.Stop();

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

    public Object getSIDRegister(int chipID) {
        return chipRegister.getSIDRegister(chipID);
    }

    public Sid getCurrentSIDContext() {
        return chipRegister.SID;
    }

    public OkiM6295.ChannelInfo getOKIM6295Info(int chipID) {
        return chipRegister.getOKIM6295Info(chipID);
    }

    public void updateVisualVolume(short[] buffer, int offset) {
        visVolume.master = buffer[offset];

        for (var i : mds.getFirstInstruments()) {
            var vs = i.getVisVolume();
        }
    }

    protected final int[] chips = new int[256];

    public int[] getChipStatus() {
        chips[0] = chipRegister.chipLED.get("PriOPN");
        chipRegister.chipLED.put("PriOPN", chipLED.get("PriOPN"));
        chips[1] = chipRegister.chipLED.get("PriOPN2");
        chipRegister.chipLED.put("PriOPN2", chipLED.get("PriOPN2"));
        chips[2] = chipRegister.chipLED.get("PriOPNA");
        chipRegister.chipLED.put("PriOPNA", chipLED.get("PriOPNA"));
        chips[3] = chipRegister.chipLED.get("PriOPNB");
        chipRegister.chipLED.put("PriOPNB", chipLED.get("PriOPNB"));

        chips[4] = chipRegister.chipLED.get("PriOPM");
        chipRegister.chipLED.put("PriOPM", chipLED.get("PriOPM"));
        chips[5] = chipRegister.chipLED.get("PriDCSG");
        chipRegister.chipLED.put("PriDCSG", chipLED.get("PriDCSG"));
        chips[6] = chipRegister.chipLED.get("PriRF5C");
        chipRegister.chipLED.put("PriRF5C", chipLED.get("PriRF5C"));
        chips[7] = chipRegister.chipLED.get("PriPWM");
        chipRegister.chipLED.put("PriPWM", chipLED.get("PriPWM"));

        chips[8] = chipRegister.chipLED.get("PriOKI5");
        chipRegister.chipLED.put("PriOKI5", chipLED.get("PriOKI5"));
        chips[9] = chipRegister.chipLED.get("PriOKI9");
        chipRegister.chipLED.put("PriOKI9", chipLED.get("PriOKI9"));
        chips[10] = chipRegister.chipLED.get("PriC140");
        chipRegister.chipLED.put("PriC140", chipLED.get("PriC140"));
        chips[11] = chipRegister.chipLED.get("PriSPCM");
        chipRegister.chipLED.put("PriSPCM", chipLED.get("PriSPCM"));

        chips[12] = chipRegister.chipLED.get("PriAY10");
        chipRegister.chipLED.put("PriAY10", chipLED.get("PriAY10"));
        chips[13] = chipRegister.chipLED.get("PriOPLL");
        chipRegister.chipLED.put("PriOPLL", chipLED.get("PriOPLL"));
        chips[14] = chipRegister.chipLED.get("PriHuC");
        chipRegister.chipLED.put("PriHuC", chipLED.get("PriHuC"));
        chips[15] = chipRegister.chipLED.get("PriC352");
        chipRegister.chipLED.put("PriC352", chipLED.get("PriC352"));
        chips[16] = chipRegister.chipLED.get("PriK054539");
        chipRegister.chipLED.put("PriK054539", chipLED.get("PriK054539"));
        chips[17] = chipRegister.chipLED.get("PriRF5C68");
        chipRegister.chipLED.put("PriRF5C68", chipLED.get("PriRF5C68"));


        chips[128 + 0] = chipRegister.chipLED.get("SecOPN");
        chipRegister.chipLED.put("SecOPN", chipLED.get("SecOPN"));
        chips[128 + 1] = chipRegister.chipLED.get("SecOPN2");
        chipRegister.chipLED.put("SecOPN2", chipLED.get("SecOPN2"));
        chips[128 + 2] = chipRegister.chipLED.get("SecOPNA");
        chipRegister.chipLED.put("SecOPNA", chipLED.get("SecOPNA"));
        chips[128 + 3] = chipRegister.chipLED.get("SecOPNB");
        chipRegister.chipLED.put("SecOPNB", chipLED.get("SecOPNB"));

        chips[128 + 4] = chipRegister.chipLED.get("SecOPM");
        chipRegister.chipLED.put("SecOPM", chipLED.get("SecOPM"));
        chips[128 + 5] = chipRegister.chipLED.get("SecDCSG");
        chipRegister.chipLED.put("SecDCSG", chipLED.get("SecDCSG"));
        chips[128 + 6] = chipRegister.chipLED.get("SecRF5C");
        chipRegister.chipLED.put("SecRF5C", chipLED.get("SecRF5C"));
        chips[128 + 7] = chipRegister.chipLED.get("SecPWM");
        chipRegister.chipLED.put("SecPWM", chipLED.get("SecPWM"));

        chips[128 + 8] = chipRegister.chipLED.get("SecOKI5");
        chipRegister.chipLED.put("SecOKI5", chipLED.get("SecOKI5"));
        chips[128 + 9] = chipRegister.chipLED.get("SecOKI9");
        chipRegister.chipLED.put("SecOKI9", chipLED.get("SecOKI9"));
        chips[128 + 10] = chipRegister.chipLED.get("SecC140");
        chipRegister.chipLED.put("SecC140", chipLED.get("SecC140"));
        chips[128 + 11] = chipRegister.chipLED.get("SecSPCM");
        chipRegister.chipLED.put("SecSPCM", chipLED.get("SecSPCM"));

        chips[128 + 12] = chipRegister.chipLED.get("SecAY10");
        chipRegister.chipLED.put("SecAY10", chipLED.get("SecAY10"));
        chips[128 + 13] = chipRegister.chipLED.get("SecOPLL");
        chipRegister.chipLED.put("SecOPLL", chipLED.get("SecOPLL"));
        chips[128 + 14] = chipRegister.chipLED.get("SecHuC");
        chipRegister.chipLED.put("SecHuC", chipLED.get("SecHuC"));
        chips[128 + 15] = chipRegister.chipLED.get("SecC352");
        chipRegister.chipLED.put("SecC352", chipLED.get("SecC352"));
        chips[128 + 16] = chipRegister.chipLED.get("SecK054539");
        chipRegister.chipLED.put("SecK054539", chipLED.get("SecK054539"));
        chips[128 + 17] = chipRegister.chipLED.get("SecRF5C68");
        chipRegister.chipLED.put("SecRF5C68", chipLED.get("SecRF5C68"));

        return chips;
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
        chipRegister.setFadeoutVolSN76489((byte) 0, 0);
        chipRegister.setFadeoutVolSN76489((byte) 1, 0);
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
    public final double swFreq = 1000d / 44100;


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




    public int[][] getFMRegister(int chipID) {
        return chipRegister.fmRegisterYM2612[chipID];
    }

    public int[][] getYM2612MIDIRegister() {
        return mdsMIDI.ReadYm2612Register(0, (byte) 0);
    }

    public int[] getYM2151Register(int chipID) {
        return chipRegister.fmRegisterYM2151[chipID];
    }

    public int[] getYm2203Register(int chipID) {
        return chipRegister.fmRegisterYM2203[chipID];
    }

    public int[] getYM2413Register(int chipID) {
        return chipRegister.fmRegisterYM2413[chipID];
    }

    public DeviceInfo.TrackInfo[] getVRC6Register(int chipID) {
        return chipRegister.getVRC6Register(chipID);
    }

    public byte[] getVRC7Register(int chipID) {
        return chipRegister.getVRC7Register(chipID);
    }

    public DeviceInfo.TrackInfo[] getN106Register(int chipID) {
        return chipRegister.getN106Register(chipID);
    }

    public int[][] getYM2608Register(int chipID) {
        return chipRegister.fmRegisterYM2608[chipID];
    }

    public int[][] getYM2610Register(int chipID) {
        return chipRegister.fmRegisterYM2610[chipID];
    }

    public int[] getYM3526Register(int chipID) {
        return chipRegister.fmRegisterYM3526[chipID];
    }

    public int[] getY8950Register(int chipID) {
        return chipRegister.fmRegisterY8950[chipID];
    }

    public int[] getYM3812Register(int chipID) {
        return chipRegister.fmRegisterYM3812[chipID];
    }

    public int[][] getYMF262Register(int chipID) {
        return chipRegister.fmRegisterYMF262[chipID];
    }

    public int[][] getYMF278BRegister(int chipID) {
        return chipRegister.fmRegisterYMF278B[chipID];
    }

    public int[] getPSGRegister(int chipID) {
        return chipRegister.sn76489Register[chipID];
    }

    public int getPSGRegisterGGPanning(int chipID) {
        return chipRegister.sn76489RegisterGGPan[chipID];
    }

    public int[] getAY8910Register(int chipID) {
        return chipRegister.psgRegisterAY8910[chipID];
    }

    public OotakeHuC6280 getHuC6280Register(int chipID) {
        return mds.ReadOotakePsgStatus(chipID);
    }

    public K051649 getK051649Register(int chipID) {
        return chipRegister.scc_k051649.GetK051649_State((byte) chipID);
    }

    public MIDIParam getMIDIInfos(int chipID) {
        return chipRegister.midiParams[chipID];
    }

    public PcmChip getRf5c164Register(int chipID) {
        return mds.ReadRf5c164Register(chipID);
    }

    public Rf5c68 getRf5c68Register(int chipID) {
        return mds.ReadRf5c68Register(chipID);
    }

    public YmF271 getYMF271Register(int chipID) {
        return mds.ReadYmf271Register(chipID);
    }


    public byte[] getC140Register(int chipID) {
        return chipRegister.pcmRegisterC140[chipID];
    }

    public PPZ8Status.Channel[] getPPZ8Register(int chipID) {
        return chipRegister.GetPPZ8Register(chipID);
    }

    public boolean[] getC140KeyOn(int chipID) {
        return chipRegister.pcmKeyOnC140[chipID];
    }

    public int[] getYMZ280BRegister(int chipID) {
        return chipRegister.YMZ280BRegister[chipID];
    }

    public int[] getC352Register(int chipID) {
        return chipRegister.pcmRegisterC352[chipID];
    }

    public MultiPCM getMultiPCMRegister(int chipID) {
        return chipRegister.getMultiPCMRegister(chipID);
    }

    public int[] getC352KeyOn(int chipID) {
        return chipRegister.readC352((byte) chipID);
    }

    public int[] getQSoundRegister(int chipID) {
        return chipRegister.getQSoundRegister(chipID);
    }

    public byte[] getSEGAPCMRegister(int chipID) {
        return chipRegister.pcmRegisterSEGAPCM[chipID];
    }

    public boolean[] getSEGAPCMKeyOn(int chipID) {
        return chipRegister.pcmKeyOnSEGAPCM[chipID];
    }

    public OkiM6258 getOKIM6258Register(int chipID) {
        return mds.ReadOkiM6258Status(chipID);
    }

    public SegaPcm getSegaPCMRegister(int chipID) {
        return mds.ReadSegaPCMStatus(chipID);
    }

    public byte[] getAPURegister(int chipID) {
        byte[] reg;

        // nsf向け
        if (chipRegister == null) reg = null;
        else if (chipRegister.nes_apu == null) reg = null;
        else if (chipRegister.nes_apu.apu == null) reg = null;
        else if (chipID == 1) reg = null;
        else reg = chipRegister.nes_apu.apu.reg;

        // vgm向け
        if (reg == null) reg = chipRegister.getNESRegisterAPU(chipID, Common.EnmModel.VirtualModel);

        return reg;
    }

    public byte[] getDMCRegister(int chipID) {
        byte[] reg;
        try {
            // nsf向け
            if (chipRegister == null) reg = null;
            else if (chipRegister.nes_apu == null) reg = null;
            else if (chipRegister.nes_apu.apu == null) reg = null;
            else if (chipID == 1) reg = null;
            else reg = chipRegister.nes_dmc.dmc.reg;

            // vgm向け
            if (reg == null) reg = chipRegister.getNESRegisterDMC(chipID, Common.EnmModel.VirtualModel);

            return reg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public mdsound.np.NpNesFds getFDSRegister(int chipID) {
        mdsound.np.NpNesFds reg;

        // nsf向け
        if (chipRegister == null) reg = null;
        else if (chipRegister.nes_apu == null) reg = null;
        else if (chipRegister.nes_apu.apu == null) reg = null;
        else if (chipID == 1) reg = null;
        else reg = chipRegister.nes_fds.fds;

        // vgm向け
        if (reg == null) reg = chipRegister.getFDSRegister(chipID, Common.EnmModel.VirtualModel);

        return reg;
    }

    protected final byte[] s5bregs = new byte[0x20];

    public byte[] getS5BRegister(int chipID) {
        // nsf 向け
        if (chipRegister == null) return null;
        else if (chipRegister.nes_fme7 == null) return null;
        else if (chipID == 1) return null;

        int[] dat = new int[] { 0 };
        for (int adr = 0x00; adr < 0x20; adr++) {
            chipRegister.nes_fme7.read(adr, dat);
            s5bregs[adr] = (byte) dat[0];
        }

        return s5bregs;
    }

    public GbSound getDMGRegister(int chipID) {
        if (mds == null) return null;
        else if (chipID == 1) return null;

        return mds.ReadGb((byte) chipID);
    }

    protected final byte[] mmc5regs = new byte[10];

    public byte[] getMMC5Register(int chipID) {
        // nsf 向け
        if (chipRegister == null) return null;
        else if (chipRegister.nes_mmc5 == null) return null;
        else if (chipID == 1) return null;

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

    public int[] getFMKeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2612[chipID];
    }

    public int[] getYM2151KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2151[chipID];
    }

    public boolean getOKIM6258KeyOn(int chipID) {
        return chipRegister.okim6258Keyon[chipID];
    }

    public void resetOKIM6258KeyOn(int chipID) {
        chipRegister.okim6258Keyon[chipID] = false;
    }

    public int getYM2151PMD(int chipID) {
        return chipRegister.fmPMDYM2151[chipID];
    }

    public int getYM2151AMD(int chipID) {
        return chipRegister.fmAMDYM2151[chipID];
    }

    public int[] getYM2608KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2608[chipID];
    }

    public int[] getYM2610KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2610[chipID];
    }

    public int[] getYM2203KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2203[chipID];
    }

    public mdplayer.ChipRegister.ChipKeyInfo getYM2413KeyInfo(int chipID) {
        return chipRegister.getYM2413KeyInfo(chipID);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getYM3526KeyInfo(int chipID) {
        return chipRegister.getYM3526KeyInfo(chipID);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getY8950KeyInfo(int chipID) {
        return chipRegister.getY8950KeyInfo(chipID);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getYM3812KeyInfo(int chipID) {
        return chipRegister.getYM3812KeyInfo(chipID);
    }

    public mdplayer.ChipRegister.ChipKeyInfo getVRC7KeyInfo(int chipID) {
        return chipRegister.getVRC7KeyInfo(chipID);
    }

    public int getYMF262FMKeyON(int chipID) {
        return chipRegister.getYMF262FMKeyON(chipID);
    }

    public int getYMF262RyhthmKeyON(int chipID) {
        return chipRegister.getYMF262RyhthmKeyON(chipID);
    }

    public int getYMF278BFMKeyON(int chipID) {
        return chipRegister.getYMF278BFMKeyON(chipID);
    }

    public void resetYMF278BFMKeyON(int chipID) {
        chipRegister.resetYMF278BFMKeyON(chipID);
    }

    public int getYMF278BRyhthmKeyON(int chipID) {
        return chipRegister.getYMF278BRyhthmKeyON(chipID);
    }

    public void resetYMF278BRyhthmKeyON(int chipID) {
        chipRegister.resetYMF278BRyhthmKeyON(chipID);
    }

    public int[] getYMF278BPCMKeyON(int chipID) {
        return chipRegister.getYMF278BPCMKeyON(chipID);
    }

    public void resetYMF278BPCMKeyON(int chipID) {
        chipRegister.resetYMF278BPCMKeyON(chipID);
    }

    public int[] getFMVolume(int chipID) {
        return chipRegister.getYM2612Volume(chipID);
    }

    public int[] getYM2151Volume(int chipID) {
        return chipRegister.getYM2151Volume(chipID);
    }

    public int[] getYM2608Volume(int chipID) {
        return chipRegister.getYM2608Volume(chipID);
    }

    public int[][] getYM2608RhythmVolume(int chipID) {
        return chipRegister.getYM2608RhythmVolume(chipID);
    }

    public int[] getYM2608AdpcmVolume(int chipID) {
        return chipRegister.getYM2608AdpcmVolume(chipID);
    }

    public int[] getYM2610Volume(int chipID) {
        return chipRegister.getYM2610Volume(chipID);
    }

    public int[][] getYM2610RhythmVolume(int chipID) {
        return chipRegister.getYM2610RhythmVolume(chipID);
    }

    public int[] getYM2610AdpcmVolume(int chipID) {
        return chipRegister.getYM2610AdpcmVolume(chipID);
    }

    public int[] getYM2203Volume(int chipID) {
        return chipRegister.getYM2203Volume(chipID);
    }

    public int[] getFMCh3SlotVolume(int chipID) {
        return chipRegister.getYM2612Ch3SlotVolume(chipID);
    }

    public int[] getYM2608Ch3SlotVolume(int chipID) {
        return chipRegister.getYM2608Ch3SlotVolume(chipID);
    }

    public int[] getYM2610Ch3SlotVolume(int chipID) {
        return chipRegister.getYM2610Ch3SlotVolume(chipID);
    }

    public int[] getYM2203Ch3SlotVolume(int chipID) {
        return chipRegister.getYM2203Ch3SlotVolume(chipID);
    }

    public int[][] getPSGVolume(int chipID) {
        return chipRegister.getPSGVolume(chipID);
    }

    public void setRF5C164Mask(int chipID, int ch) {
        chipRegister.setMaskRF5C164(chipID, ch, true);
    }

    public void setRF5C68Mask(int chipID, int ch) {
        chipRegister.setMaskRF5C68(chipID, ch, true);
    }

    public void setSN76489Mask(int chipID, int ch) {
        chipRegister.setMaskSN76489(chipID, ch, true);
        sn76489ForcedSendVolume(chipID, ch);
    }

    public void resetSN76489Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskSN76489(chipID, ch, false);
            sn76489ForcedSendVolume(chipID, ch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sn76489ForcedSendVolume(int chipID, int ch) {
        Setting.ChipType2 ct = setting.getSN76489Type()[chipID];
        chipRegister.setSN76489Register(chipID
                , (byte) (0x90
                        | ((ch & 3) << 5)
                        | (15 - (Math.max(chipRegister.sn76489Vol[chipID][ch][0], chipRegister.sn76489Vol[chipID][ch][1]) & 0xf)))
                , ct.getUseEmu()[0] ? Common.EnmModel.VirtualModel : Common.EnmModel.RealModel);
    }

    public void setYM2151Mask(int chipID, int ch) {
        chipRegister.setMaskYM2151(chipID, ch, true, false);
    }

    public void setYM2203Mask(int chipID, int ch) {
        chipRegister.setMaskYM2203(chipID, ch, true, false);
    }

    public void setYM2413Mask(int chipID, int ch) {
        chipRegister.setMaskYM2413(chipID, ch, true);
    }

    public void setYM2608Mask(int chipID, int ch) {
        chipRegister.setMaskYM2608(chipID, ch, true, false);
    }

    public void setYM2610Mask(int chipID, int ch) {
        chipRegister.setMaskYM2610(chipID, ch, true);
    }

    public void setYM2612Mask(int chipID, int ch) {
        chipRegister.setMaskYM2612(chipID, ch, true);
    }

    public void setYM3526Mask(int chipID, int ch) {
        chipRegister.setMaskYM3526(chipID, ch, true);
    }

    public void setY8950Mask(int chipID, int ch) {
        chipRegister.setMaskY8950(chipID, ch, true);
    }

    public void setYM3812Mask(int chipID, int ch) {
        chipRegister.setMaskYM3812(chipID, ch, true);
    }

    public void setYMF262Mask(int chipID, int ch) {
        chipRegister.setMaskYMF262(chipID, ch, true);
    }

    public void setYMF278BMask(int chipID, int ch) {
        chipRegister.setMaskYMF278B(chipID, ch, true);
    }

    public void setC140Mask(int chipID, int ch) {
        chipRegister.setMaskC140(chipID, ch, true);
    }

    public void setPPZ8Mask(int chipID, int ch) {
        chipRegister.setMaskPPZ8(chipID, ch, true);
    }

    public void setC352Mask(int chipID, int ch) {
        chipRegister.setMaskC352(chipID, ch, true);
    }

    public void setSegaPCMMask(int chipID, int ch) {
        chipRegister.setMaskSegaPCM(chipID, ch, true);
    }

    public void setQSoundMask(int chipID, int ch) {
        chipRegister.setMaskQSound(chipID, ch, true);
    }

    public void setAY8910Mask(int chipID, int ch) {
        chipRegister.setMaskAY8910(chipID, ch, true);
    }

    public void setHuC6280Mask(int chipID, int ch) {
        chipRegister.setMaskHuC6280(chipID, ch, true);
    }

    public void setOKIM6258Mask(int chipID) {
        chipRegister.setMaskOKIM6258(chipID, true);
    }

    public void setOKIM6295Mask(int chipID, int ch) {
        chipRegister.setMaskOKIM6295(chipID, ch, true);
    }

    public void resetOKIM6295Mask(int chipID, int ch) {
        chipRegister.setMaskOKIM6295(chipID, ch, false);
    }

    public void setNESMask(int chipID, int ch) {
        chipRegister.setNESMask(chipID, ch);
    }

    public void setDMCMask(int chipID, int ch) {
        chipRegister.setNESMask(chipID, ch + 2);
    }

    public void setFDSMask(int chipID) {
        chipRegister.setFDSMask(chipID);
    }

    public void setMMC5Mask(int chipID, int ch) {
        chipRegister.setMMC5Mask(chipID, ch);
    }

    public void setVRC7Mask(int chipID, int ch) {
        chipRegister.setVRC7Mask(chipID, ch);
    }

    public void setK051649Mask(int chipID, int ch) {
        chipRegister.setK051649Mask(chipID, ch);
    }

    public void setDMGMask(int chipID, int ch) {
        chipRegister.setDMGMask(chipID, ch);
    }

    public void setVRC6Mask(int chipID, int ch) {
        chipRegister.setVRC6Mask(chipID, ch);
    }

    public void setN163Mask(int chipID, int ch) {
        chipRegister.setN163Mask(chipID, ch);
    }


    public void resetOKIM6258Mask(int chipID) {
        chipRegister.setMaskOKIM6258(chipID, false);
    }

    public void resetYM2612Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2612(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2203Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2203(chipID, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2413Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2413(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetRF5C164Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskRF5C164(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetRF5C68Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskRF5C68(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean stopped = false;

    public void resetYM2151Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2151(chipID, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2608Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2608(chipID, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM2610Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2610(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM3526Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM3526(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetY8950Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskY8950(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYM3812Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM3812(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYMF262Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYMF262(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetYMF278BMask(int chipID, int ch) {
        try {
            chipRegister.setMaskYMF278B(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetC140Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskC140(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetPPZ8Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskPPZ8(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetC352Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskC352(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSegaPCMMask(int chipID, int ch) {
        chipRegister.setMaskSegaPCM(chipID, ch, false);
    }

    public void resetQSoundMask(int chipID, int ch) {
        chipRegister.setMaskQSound(chipID, ch, false);
    }

    public void resetAY8910Mask(int chipID, int ch) {
        chipRegister.setMaskAY8910(chipID, ch, false);
    }

    public void resetHuC6280Mask(int chipID, int ch) {
        chipRegister.setMaskHuC6280(chipID, ch, false);
    }

    public void resetNESMask(int chipID, int ch) {
        chipRegister.resetNESMask(chipID, ch);
    }

    public void resetDMCMask(int chipID, int ch) {
        chipRegister.resetNESMask(chipID, ch + 2);
    }

    public void resetFDSMask(int chipID) {
        chipRegister.resetFDSMask(chipID);
    }

    public void resetMMC5Mask(int chipID, int ch) {
        chipRegister.resetMMC5Mask(chipID, ch);
    }

    public void resetVRC7Mask(int chipID, int ch) {
        chipRegister.resetVRC7Mask(chipID, ch);
    }

    public void resetK051649Mask(int chipID, int ch) {
        chipRegister.resetK051649Mask(chipID, ch);
    }

    public void resetDMGMask(int chipID, int ch) {
        chipRegister.resetDMGMask(chipID, ch);
    }

    public void resetVRC6Mask(int chipID, int ch) {
        chipRegister.resetVRC6Mask(chipID, ch);
    }

    public void resetN163Mask(int chipID, int ch) {
        chipRegister.resetN163Mask(chipID, ch);
    }

    public void setAY8910Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getAY8910Volume()) + volume, -192, 20);
            mds.setVolumeAY8910(v);
            setting.getBalance().setAY8910Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2151Volume(boolean isAbs, int volume) {
        try {
            int vol = Common.range((isAbs ? 0 : setting.getBalance().getYM2151Volume()) + volume, -192, 20);
            setting.getBalance().setYM2151Volume(vol);

            mds.setVolumeYm2151(vol);
            mds.setVolumeYm2151Mame(vol);
            mds.SetVolumeYm2151X68Sound(vol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2203Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2203Volume()) + volume, -192, 20);
            mds.SetVolumeYm2203(v);
            setting.getBalance().setYM2203Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2203FMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2203FMVolume()) + volume, -192, 20);
            mds.SetVolumeYm2203FM(v);
            setting.getBalance().setYM2203FMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2203PSGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2203PSGVolume()) + volume, -192, 20);
            mds.SetVolumeYm2203PSG(v);
            setting.getBalance().setYM2203PSGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2413Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2413Volume()) + volume, -192, 20);
            mds.SetVolumeYm2413(v);
            setting.getBalance().setYM2413Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setK053260Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getK053260Volume()) + volume, -192, 20);
            mds.SetVolumeK053260(v);
            setting.getBalance().setK053260Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRF5C68Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getRF5C68Volume()) + volume, -192, 20);
            mds.SetVolumeRf5c68(v);
            setting.getBalance().setRF5C68Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM3812Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM3812Volume()) + volume, -192, 20);
            mds.SetVolumeYm3812(v);
            setting.getBalance().setYM3812Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setY8950Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getY8950Volume()) + volume, -192, 20);
            mds.SetVolumeY8950(v);
            setting.getBalance().setY8950Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM3526Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM3526Volume()) + volume, -192, 20);
            mds.SetVolumeYm3526(v);
            setting.getBalance().setYM3526Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2608Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2608Volume()) + volume, -192, 20);
            mds.SetVolumeYm2608(v);
            setting.getBalance().setYM2608Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2608FMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2608FMVolume()) + volume, -192, 20);
            mds.SetVolumeYm2608FM(v);
            setting.getBalance().setYM2608FMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2608PSGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2608PSGVolume()) + volume, -192, 20);
            mds.SetVolumeYm2608PSG(v);
            setting.getBalance().setYM2608PSGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2608RhythmVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2608RhythmVolume()) + volume, -192, 20);
            mds.SetVolumeYm2608Rhythm(v);
            setting.getBalance().setYM2608RhythmVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2608AdpcmVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2608AdpcmVolume()) + volume, -192, 20);
            mds.SetVolumeYm2608Adpcm(v);
            setting.getBalance().setYM2608AdpcmVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2610Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2610Volume()) + volume, -192, 20);
            mds.SetVolumeYm2610(v);
            setting.getBalance().setYM2610Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2610FMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2610FMVolume()) + volume, -192, 20);
            mds.SetVolumeYm2610FM(v);
            setting.getBalance().setYM2610FMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2610PSGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2610PSGVolume()) + volume, -192, 20);
            mds.SetVolumeYm2610PSG(v);
            setting.getBalance().setYM2610PSGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2610AdpcmAVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2610AdpcmAVolume()) + volume, -192, 20);
            mds.SetVolumeYm2610AdpcmA(v);
            setting.getBalance().setYM2610AdpcmAVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2610AdpcmBVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2610AdpcmBVolume()) + volume, -192, 20);
            mds.SetVolumeYm2610AdpcmB(v);
            setting.getBalance().setYM2610AdpcmBVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYM2612Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYM2612Volume()) + volume, -192, 20);
            mds.SetVolumeYm2612(v);
            setting.getBalance().setYM2612Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSN76489Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getSN76489Volume()) + volume, -192, 20);
            mds.SetVolumeSn76489(v);
            setting.getBalance().setSN76489Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHuC6280Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getHuC6280Volume()) + volume, -192, 20);
            mds.setVolumeOotakePsg(v);
            setting.getBalance().setHuC6280Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRF5C164Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getRF5C164Volume()) + volume, -192, 20);
            mds.SetVolumeScdPcm(v);
            setting.getBalance().setRF5C164Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPWMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getPWMVolume()) + volume, -192, 20);
            mds.SetVolumePwm(v);
            setting.getBalance().setPWMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOKIM6258Volume(boolean isAbs, int volume) {
        try {
            int vol = Common.range((isAbs ? 0 : setting.getBalance().getOKIM6258Volume()) + volume, -192, 20);
            setting.getBalance().setOKIM6258Volume(vol);

            mds.SetVolumeOkiM6258(vol);
            mds.SetVolumeMpcmX68k(vol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOKIM6295Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getOKIM6295Volume()) + volume, -192, 20);
            mds.SetVolumeOkiM6295(v);
            setting.getBalance().setOKIM6295Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setC140Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getC140Volume()) + volume, -192, 20);
            mds.SetVolumeC140(v);
            setting.getBalance().setC140Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSegaPCMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getSEGAPCMVolume()) + volume, -192, 20);
            mds.setVolumeSegaPCM(v);
            setting.getBalance().setSEGAPCMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setC352Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getC352Volume()) + volume, -192, 20);
            mds.SetVolumeC352(v);
            setting.getBalance().setC352Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSA1099Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getSAA1099Volume()) + volume, -192, 20);
            mds.SetVolumeSaa1099(v);
            setting.getBalance().setSAA1099Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPPZ8Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getPPZ8Volume()) + volume, -192, 20);
            mds.SetVolumePPZ8(v);
            setting.getBalance().setPPZ8Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setK051649Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getK051649Volume()) + volume, -192, 20);
            mds.SetVolumeK051649(v);
            setting.getBalance().setK051649Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setK054539Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getK054539Volume()) + volume, -192, 20);
            mds.SetVolumeK054539(v);
            setting.getBalance().setK054539Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setQSoundVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getQSoundVolume()) + volume, -192, 20);
            mds.SetVolumeQSoundCtr(v);
            setting.getBalance().setQSoundVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDMGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getDMGVolume()) + volume, -192, 20);
            mds.setVolumeGb(v);
            setting.getBalance().setDMGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGA20Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getGA20Volume()) + volume, -192, 20);
            mds.setVolumeIremga20(v);
            setting.getBalance().setGA20Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYMZ280BVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYMZ280BVolume()) + volume, -192, 20);
            mds.setVolumeYmZ280b(v);
            setting.getBalance().setYMZ280BVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYMF271Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYMF271Volume()) + volume, -192, 20);
            mds.setVolumeYmf271(v);
            setting.getBalance().setYMF271Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYMF262Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYMF262Volume()) + volume, -192, 20);
            mds.SetVolumeYmF262(v);
            setting.getBalance().setYMF262Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setYMF278BVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getYMF278BVolume()) + volume, -192, 20);
            mds.SetVolumeYmF278b(v);
            setting.getBalance().setYMF278BVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMultiPCMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getMultiPCMVolume()) + volume, -192, 20);
            mds.setVolumeMultiPCM(v);
            setting.getBalance().setMultiPCMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setAPUVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getAPUVolume()) + volume, -192, 20);
            mds.SetVolumeNES(v);
            setting.getBalance().setAPUVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDMCVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getDMCVolume()) + volume, -192, 20);
            mds.SetVolumeDMC(v);
            setting.getBalance().setDMCVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFDSVolume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getFDSVolume()) + volume, -192, 20);
            mds.SetVolumeFDS(v);
            setting.getBalance().setFDSVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMMC5Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getMMC5Volume()) + volume, -192, 20);
            mds.SetVolumeMMC5(v);
            setting.getBalance().setMMC5Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setN160Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getN160Volume()) + volume, -192, 20);
            mds.SetVolumeN160(v);
            setting.getBalance().setN160Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVRC6Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getVRC6Volume()) + volume, -192, 20);
            mds.SetVolumeVRC6(v);
            setting.getBalance().setVRC6Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVRC7Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getVRC7Volume()) + volume, -192, 20);
            mds.SetVolumeVRC7(v);
            setting.getBalance().setVRC7Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFME7Volume(boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getFME7Volume()) + volume, -192, 20);
            mds.SetVolumeFME7(v);
            setting.getBalance().setFME7Volume(v);
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

    @SuppressWarnings("unchecked")
    public BiConsumer<Boolean, Integer>[] setVolume = Arrays.<BiConsumer<Boolean, Integer>>asList(
            this::setMasterVolume, this::setYM2151Volume, this::setYM2203Volume, this::setYM2203FMVolume
            , this::setYM2203PSGVolume, this::setYM2612Volume, this::setYM2608Volume, this::setYM2608FMVolume
            , this::setYM2608PSGVolume, this::setYM2608RhythmVolume, this::setYM2608AdpcmVolume, this::setYM2610Volume
            , this::setYM2610FMVolume, this::setYM2610PSGVolume, this::setYM2610AdpcmAVolume, this::setYM2610AdpcmBVolume

            , this::setYM2413Volume, this::setYM3526Volume, this::setY8950Volume, this::setYM3812Volume
            , this::setYMF262Volume, this::setYMF278BVolume, this::setYMZ280BVolume, this::setYMF271Volume
            , null, this::setAY8910Volume, this::setSN76489Volume, this::setHuC6280Volume
            , this::setSA1099Volume, null, null, null

            , null, null, this::setRF5C164Volume, this::setRF5C68Volume
            , this::setPWMVolume, this::setOKIM6258Volume, this::setOKIM6295Volume, this::setC140Volume
            , this::setC352Volume, this::setSegaPCMVolume, this::setMultiPCMVolume, this::setK051649Volume
            , this::setK053260Volume, this::setK054539Volume, this::setQSoundVolume, this::setGA20Volume

            , this::setAPUVolume, this::setDMCVolume, this::setFDSVolume, this::setMMC5Volume
            , this::setN160Volume, this::setVRC6Volume, this::setVRC7Volume, this::setFME7Volume
            , this::setDMGVolume, null, null, null
            , null, this::setPPZ8Volume, this::setGimicOPNVolume, this::setGimicOPNAVolume
    ).toArray(BiConsumer[]::new);

    public MDSound.Chip getMDSChipInfo(Class<? extends Instrument> typ) {
        return chipRegister.GetChipInfo(typ);
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
            return ((MoonDriver) driverVirtual).GetPCMKeyOn();
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
}
