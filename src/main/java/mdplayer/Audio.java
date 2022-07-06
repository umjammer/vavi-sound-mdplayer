package mdplayer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.UnsupportedAudioFileException;

import dotnet4j.io.Path;
import dotnet4j.io.Stream;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Common.EnmRealChipType;
import mdplayer.Common.FileFormat;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.driver.Xgm;
import mdplayer.driver.hes.Hes;
import mdplayer.driver.mgsdrv.MGSDRV;
import mdplayer.driver.mid.MID;
import mdplayer.driver.mndrv.MnDrv;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.driver.moonDriver.MoonDriverDotNET;
import mdplayer.driver.mucom.MucomDotNET;
import mdplayer.driver.mxdrv.MXDRV;
import mdplayer.driver.nrtdrv.NRTDRV;
import mdplayer.driver.nsf.Nsf;
import mdplayer.driver.pmd.PMDDotNET;
import mdplayer.driver.rcp.RCP;
import mdplayer.driver.s98.S98;
import mdplayer.driver.sid.Sid;
import mdplayer.driver.zgm.Zgm;
import mdplayer.form.sys.frmMain;
import mdsound.*;
import mdsound.Ym3438Const.Type;
import mdsound.np.chip.DeviceInfo;
import mdsound.x68sound.SoundIocs;
import mdsound.x68sound.X68Sound;
import vavi.util.ByteUtil;
import vavi.util.Debug;
import vavi.util.archive.Archive;
import vavi.util.archive.Entry;


public class Audio {
    public static frmMain frmMain = null;
//    public static final VstMng vstMng = new VstMng();
    public static Setting setting = null;

    public static int clockAY8910 = 1789750;
    public static final int clockS5B = 1789772;
    public static int clockK051649 = 1500000;
    public static final int clockC140 = 21390;
    public static final int clockPPZ8 = 44100;
    public static int clockC352 = 24192000;
    public static int clockFDS = 0;
    public static int clockHuC6280 = 0;
    public static int clockRF5C164 = 0;
    public static int clockMMC5 = 0;
    public static int clockNESDMC = 0;
    public static int clockOKIM6258 = 0;
    public static int clockOKIM6295 = 0;
    public static int clockSegaPCM = 0;
    public static int clockSN76489 = 0;
    public static int clockYM2151 = 0;
    public static int clockYM2203 = 0;
    public static int clockYM2413 = 0;
    public static int clockYM2608 = 0;
    public static int clockYM2610 = 0;
    public static int clockYM2612 = 0;
    public static int clockYMF278B = 0;

    private static final Object lockObj = new Object();
    private static boolean _fatalError = false;

    public static boolean getFatalError() {
        synchronized (lockObj) {
            return _fatalError;
        }
    }

    public static void setFatalError(boolean value) {
        synchronized (lockObj) {
            _fatalError = value;
        }
    }

    private static final int samplingBuffer = 1024;
    private static mdsound.MDSound mds = null;
    public static mdsound.MDSound mdsMIDI = null;
    private static NAudioWrap naudioWrap;
    private static WaveWriter waveWriter = null;

    private static ChipRegister chipRegister = null;
    public static final Set<EnmChip> useChip = new HashSet<>();


    private static Thread trdMain = null;
    public static boolean trdClosed = false;
    private static boolean _trdStopped = true;

    public static boolean getTrdStopped() {
        synchronized (lockObj) {
            return _trdStopped;
        }
    }

    void setTrdStopped(boolean value) {
        synchronized (lockObj) {
            _trdStopped = value;
        }
    }

    public static Object getSIDRegister(int chipID) {
        return chipRegister.getSIDRegister(chipID);
    }

    public static Sid getCurrentSIDContext() {
        return chipRegister.SID;
    }

    public static OkiM6295.OkiM6295State.ChannelInfo getOKIM6295Info(int chipID) {
        return chipRegister.getOKIM6295Info(chipID);
    }

    private static long sw = System.currentTimeMillis();
    private static final double swFreq = 1000d / 44100;

    private static byte[] vgmBuf = null;
    private static double vgmSpeed;
    private static boolean vgmFadeout;
    private static double vgmFadeoutCounter;
    private static double vgmFadeoutCounterV;
    private static int vgmRealFadeoutVol = 0;
    private static int vgmRealFadeoutVolWait = 4;

    private static boolean paused = false;
    public static boolean stopped = false;
    private static int stepCounter = 0;

    public static BaseDriver driverVirtual = null;
    public static BaseDriver driverReal = null;

    private static boolean oneTimeReset = false;
    private static int hiyorimiEven = 0;
    private static boolean hiyorimiNecessary = false;

    public static ChipLEDs chipLED = new ChipLEDs();
    public static final VisVolume visVolume = new VisVolume();

    private static int masterVolume = 0;
    private static final byte[] chips = new byte[256];
    private static String playingFileName;
    private static String playingArcFileName;
    private static int midiMode = 0;
    private static int songNo = 0;
    private static List<Tuple<String, byte[]>> extendFile = null;
    private static FileFormat playingFileFormat;

    private static final long stwh = System.currentTimeMillis();
    public static int ProcTimePer1Frame = 0;

    private static final List<Receiver> midiOuts = new ArrayList<>();
    private static final List<Integer> midiOutsType = new ArrayList<>();
    public static String errMsg = "";
    public static boolean flgReinit = false;

    public static boolean getEmuOnly() {
        return false;
    }

//    public static List<VstMng.VstInfo2> getVSTInfos() {
//        return vstMng.getVSTInfos();
//    }
//
//    public static VstInfo getVSTInfo(String filename) {
//        return vstMng.getVSTInfo(filename);
//    }
//
//    public static boolean addVSTeffect(String fileName) {
//        return vstMng.addVSTeffect(fileName);
//    }
//
//    public static boolean delVSTeffect(String key) {
//        return vstMng.delVSTeffect(key);
//    }

    public static void copyWaveBuffer(short[][] dest) {
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

    public static List<PlayList.Music> getMusic(String file, byte[] buf, String zipFile, Archive archive/* = null*/, Entry entry/* = null*/) {
        FileFormat ff = FileFormat.checkExt(file);
        return ff.getMusic(setting, file, buf, zipFile, archive, entry);
    }

    public static void realChipClose() {
//        if (SoundChip.realChip != null) {
//            SoundChip.realChip.close();
//        }
    }

    public static List<PlayList.Music> getMusic(PlayList.Music ms, byte[] buf, String zipFile/* = null*/) {
        List<PlayList.Music> musics = new ArrayList<>();
        PlayList.Music music = new PlayList.Music();

        music.format = FileFormat.unknown;
        music.fileName = ms.fileName;
        music.arcFileName = zipFile;
        music.title = "unknown";
        music.game = "unknown";
        music.type = "-";

        if (ms.fileName.toLowerCase().lastIndexOf(".nrd") != -1) {

            music.format = FileFormat.NRT;
            int index = 42;
            Gd3 gd3 = (new NRTDRV(setting)).getGD3Info(buf, index);
            music.title = gd3.trackName;
            music.titleJ = gd3.trackNameJ;
            music.game = gd3.gameName;
            music.gameJ = gd3.gameNameJ;
            music.composer = gd3.composer;
            music.composerJ = gd3.composerJ;
            music.vgmby = gd3.vgmBy;

            music.converted = gd3.converted;
            music.notes = gd3.notes;

        } else if (ms.fileName.toLowerCase().lastIndexOf(".mgs") != -1) {

            music.format = FileFormat.MGS;
            int index = 8;
            Gd3 gd3 = (new MGSDRV()).getGD3Info(buf, index);
            music.title = gd3.trackName;
            music.titleJ = gd3.trackNameJ;
            music.game = "";
            music.gameJ = "";
            music.composer = "";
            music.composerJ = "";
            music.vgmby = "";

            music.converted = "";
            music.notes = "";

        } else if (ms.fileName.toLowerCase().lastIndexOf(".xgm") != -1) {
            music.format = FileFormat.XGM;
            Gd3 gd3 = new Xgm(setting).getGD3Info(buf);
            music.title = gd3.trackName;
            music.titleJ = gd3.trackNameJ;
            music.game = gd3.gameName;
            music.gameJ = gd3.gameNameJ;
            music.composer = gd3.composer;
            music.composerJ = gd3.composerJ;
            music.vgmby = gd3.vgmBy;

            music.converted = gd3.converted;
            music.notes = gd3.notes;

            if (music.title.isEmpty() && music.titleJ.isEmpty() && music.game.isEmpty() && music.gameJ.isEmpty() && music.composer.isEmpty() && music.composerJ.isEmpty()) {
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }
        } else if (ms.fileName.toLowerCase().lastIndexOf(".s98") != -1) {
            music.format = FileFormat.S98;
            Vgm.Gd3 gd3 = new S98(setting).getGD3Info(buf);
            if (gd3 != null) {
                music.title = gd3.trackName;
                music.titleJ = gd3.trackNameJ;
                music.game = gd3.gameName;
                music.gameJ = gd3.gameNameJ;
                music.composer = gd3.composer;
                music.composerJ = gd3.composerJ;
                music.vgmby = gd3.vgmBy;

                music.converted = gd3.converted;
                music.notes = gd3.notes;
            } else {
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }

        } else if (ms.fileName.toLowerCase().lastIndexOf(".nsf") != -1) {
            Nsf nsf = new Nsf(setting);
            Gd3 gd3 = nsf.getGD3Info(buf);

            if (gd3 != null) {
                if (ms.songNo == -1) {
                    for (int s = 0; s < nsf.songs; s++) {
                        music = new PlayList.Music();
                        music.format = FileFormat.NSF;
                        music.fileName = ms.fileName;
                        music.arcFileName = zipFile;
                        music.title = String.format("%s - Trk %d", gd3.gameName, s);
                        music.titleJ = String.format("%s - Trk %d", gd3.gameNameJ, s);
                        music.game = gd3.gameName;
                        music.gameJ = gd3.gameNameJ;
                        music.composer = gd3.composer;
                        music.composerJ = gd3.composerJ;
                        music.vgmby = gd3.vgmBy;
                        music.converted = gd3.converted;
                        music.notes = gd3.notes;
                        music.songNo = s;

                        musics.add(music);
                    }

                    return musics;

                } else {
                    music.format = FileFormat.NSF;
                    music.fileName = ms.fileName;
                    music.arcFileName = zipFile;
                    music.title = ms.title;
                    music.titleJ = ms.titleJ;
                    music.game = gd3.gameName;
                    music.gameJ = gd3.gameNameJ;
                    music.composer = gd3.composer;
                    music.composerJ = gd3.composerJ;
                    music.vgmby = gd3.vgmBy;
                    music.converted = gd3.converted;
                    music.notes = gd3.notes;
                    music.songNo = ms.songNo;
                }
            } else {
                music.format = FileFormat.NSF;
                music.fileName = ms.fileName;
                music.arcFileName = zipFile;
                music.game = "unknown";
                music.type = "-";
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }

        } else if (ms.fileName.toLowerCase().lastIndexOf(".mid") != -1) {
            music.format = FileFormat.MID;
            Gd3 gd3 = new MID().getGD3Info(buf);
            if (gd3 != null) {
                music.title = gd3.trackName;
                music.titleJ = gd3.trackNameJ;
                music.game = gd3.gameName;
                music.gameJ = gd3.gameNameJ;
                music.composer = gd3.composer;
                music.composerJ = gd3.composerJ;
                music.vgmby = gd3.vgmBy;

                music.converted = gd3.converted;
                music.notes = gd3.notes;
            } else {
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }

            if (music.title.isEmpty() && music.titleJ.isEmpty()) {
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }

        } else if (ms.fileName.toLowerCase().lastIndexOf(".rcp") != -1) {
            music.format = FileFormat.RCP;
            Vgm.Gd3 gd3 = new RCP().getGD3Info(buf);
            if (gd3 != null) {
                music.title = gd3.trackName;
                music.titleJ = gd3.trackNameJ;
                music.game = gd3.gameName;
                music.gameJ = gd3.gameNameJ;
                music.composer = gd3.composer;
                music.composerJ = gd3.composerJ;
                music.vgmby = gd3.vgmBy;

                music.converted = gd3.converted;
                music.notes = gd3.notes;
            } else {
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }

            if (music.title.isEmpty() && music.titleJ.isEmpty()) {
                music.title = String.format("(%s)", Path.getFileName(ms.fileName));
            }

        } else {
            if (buf.length < 0x40) {
                musics.add(music);
                return musics;
            }
            if (Common.getLE32(buf, 0x00) != Vgm.FCC_VGM) {
                musics.add(music);
                return musics;
            }

            music.format = FileFormat.VGM;
            int version = Common.getLE32(buf, 0x08);
            String _version = String.format("%d.%d%d", (version & 0xf00) / 0x100, (version & 0xf0) / 0x10, (version & 0xf));

            int vgmGd3 = Common.getLE32(buf, 0x14);
            Gd3 gd3 = new Gd3();
            if (vgmGd3 != 0) {
                int vgmGd3Id = Common.getLE32(buf, vgmGd3 + 0x14);
                if (vgmGd3Id != Vgm.FCC_GD3) {
                    musics.add(music);
                    return musics;
                }
                gd3 = (new Vgm(setting)).getGD3Info(buf, vgmGd3);
            }

            int totalCounter = Common.getLE32(buf, 0x18);
            int vgmLoopOffset = Common.getLE32(buf, 0x1c);
            int loopCounter = Common.getLE32(buf, 0x20);

            music.title = gd3.trackName;
            music.titleJ = gd3.trackNameJ;
            music.game = gd3.gameName;
            music.gameJ = gd3.gameNameJ;
            music.composer = gd3.composer;
            music.composerJ = gd3.composerJ;
            music.vgmby = gd3.vgmBy;

            music.converted = gd3.converted;
            music.notes = gd3.notes;

            double sec = (double) totalCounter / (double) setting.getOutputDevice().getSampleRate();
            int tcMminutes = (int) (sec / 60);
            sec -= tcMminutes * 60;
            int tcSecond = (int) sec;
            sec -= tcSecond;
            int tcMillisecond = (int) (sec * 100.0);
            music.duration = String.format("%2d:%2d:%2d", tcMminutes, tcSecond, tcMillisecond);
        }

        musics.add(music);
        return musics;
    }

    public static List<Setting.ChipType2> getRealChipList(EnmRealChipType scciType) {
//        if (SoundChip.realChip == null) return null;
//        return SoundChip.realChip.GetRealChipList(scciType);
        return null;
    }

    private static String getNRDString(byte[] buf, int index) {
        if (buf == null || buf.length < 1 || index < 0 || index >= buf.length) return "";

        try {
            List<Byte> lst = new ArrayList<>();
            for (; buf[index] != 0; index++) {
                lst.add(buf[index]);
            }

            String n = new String(mdsound.Common.toByteArray(lst), Charset.forName("MS932"));
            index++;

            return n;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void init(Setting setting) {
        Debug.println(Level.SEVERE, "Audio:Init:Begin");

        Thread trd = new Thread(Audio::trdIF);
        trd.setPriority(Thread.NORM_PRIORITY);
        trd.start();

        Debug.println(Level.SEVERE, "Audio:Init:STEP 01");

        naudioWrap = new NAudioWrap(setting.getOutputDevice().getSampleRate(), Audio::trdVgmVirtualFunction);
        naudioWrap.playbackStopped = Audio::naudioWrapPlaybackStopped;

        Debug.println(Level.SEVERE, "Audio:Init:STEP 02");

        Audio.setting = setting;
//        vstMng.setting = setting;

        waveWriter = new WaveWriter(setting);

        Debug.println(Level.SEVERE, "Audio:Init:STEP 03");

        if (Audio.setting.getAY8910Type() == null || Audio.setting.getAY8910Type().length < 2) {
            Audio.setting.setAY8910Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getAY8910Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getAY8910Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getAY8910Type()[i].getUseEmu()[0] = true;
                Audio.setting.getAY8910Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getK051649Type() == null || Audio.setting.getK051649Type().length < 2) {
            Audio.setting.setK051649Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getK051649Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getK051649Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getK051649Type()[i].getUseEmu()[0] = true;
                Audio.setting.getK051649Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getC140Type() == null || Audio.setting.getC140Type().length < 2) {
            Audio.setting.setC140Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getC140Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getC140Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getC140Type()[i].getUseEmu()[0] = true;
                Audio.setting.getC140Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getHuC6280Type() == null || Audio.setting.getHuC6280Type().length < 2) {
            Audio.setting.setHuC6280Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getHuC6280Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getHuC6280Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getHuC6280Type()[i].getUseEmu()[0] = true;
                Audio.setting.getHuC6280Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getSEGAPCMType() == null || Audio.setting.getSEGAPCMType().length < 2) {
            Audio.setting.setSEGAPCMType(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getSEGAPCMType()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getSEGAPCMType()[i].setUseEmu(new boolean[1]);
                Audio.setting.getSEGAPCMType()[i].getUseEmu()[0] = true;
                Audio.setting.getSEGAPCMType()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getSN76489Type() == null || Audio.setting.getSN76489Type().length < 2) {
            Audio.setting.setSN76489Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getSN76489Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getSN76489Type()[i].setUseEmu(new boolean[2]);
                Audio.setting.getSN76489Type()[i].getUseEmu()[0] = true;
                Audio.setting.getSN76489Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getY8950Type() == null || Audio.setting.getY8950Type().length < 2) {
            Audio.setting.setY8950Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getY8950Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getY8950Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getY8950Type()[i].getUseEmu()[0] = true;
                Audio.setting.getY8950Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYM2151Type() == null || Audio.setting.getYM2151Type().length < 2) {
            Audio.setting.setYM2151Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM2151Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM2151Type()[i].setUseEmu(new boolean[3]);
                Audio.setting.getYM2151Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM2151Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYM2203Type() == null || Audio.setting.getYM2203Type().length < 2) {
            Audio.setting.setYM2203Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM2203Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM2203Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYM2203Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM2203Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYM2413Type() == null || Audio.setting.getYM2413Type().length < 2) {
            Audio.setting.setYM2413Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM2413Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM2413Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYM2413Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM2413Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYM2608Type() == null || Audio.setting.getYM2608Type().length < 2) {
            Audio.setting.setYM2608Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM2608Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM2608Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYM2608Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM2608Type()[i].setUseReal(new boolean[1]);
            }
        }

        if (Audio.setting.getYM2610Type() == null
                || Audio.setting.getYM2610Type().length < 2
                || Audio.setting.getYM2610Type()[0].getUseReal() == null
                || Audio.setting.getYM2610Type()[0].getUseReal().length < 3
                || Audio.setting.getYM2610Type()[1].getUseReal() == null
                || Audio.setting.getYM2610Type()[1].getUseReal().length < 3
        ) {
            Audio.setting.setYM2610Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM2610Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo(), new Setting.ChipType2.RealChipInfo(), new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM2610Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYM2610Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM2610Type()[i].setUseReal(new boolean[3]);
            }
        }

        if (Audio.setting.getYM2612Type() == null || Audio.setting.getYM2612Type().length < 2) {
            Audio.setting.setYM2612Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM2612Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM2612Type()[i].setUseEmu(new boolean[3]);
                Audio.setting.getYM2612Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM2612Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYM3526Type() == null || Audio.setting.getYM3526Type().length < 2) {
            Audio.setting.setYM3526Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM3526Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM3526Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYM3526Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM3526Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYM3812Type() == null || Audio.setting.getYM3812Type().length < 2) {
            Audio.setting.setYM3812Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYM3812Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYM3812Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYM3812Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYM3812Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYMF262Type() == null || Audio.setting.getYMF262Type().length < 2) {
            Audio.setting.setYMF262Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYMF262Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYMF262Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYMF262Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYMF262Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYMF271Type() == null || Audio.setting.getYMF271Type().length < 2) {
            Audio.setting.setYMF271Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYMF271Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYMF271Type()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYMF271Type()[i].getUseEmu()[0] = true;
                Audio.setting.getYMF271Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYMF278BType() == null || Audio.setting.getYMF278BType().length < 2) {
            Audio.setting.setYMF278BType(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYMF278BType()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYMF278BType()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYMF278BType()[i].getUseEmu()[0] = true;
                Audio.setting.getYMF278BType()[i].setUseReal(new boolean[1]);
            }
        }
        if (Audio.setting.getYMZ280BType() == null || Audio.setting.getYMZ280BType().length < 2) {
            Audio.setting.setYMZ280BType(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                Audio.setting.getYMZ280BType()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                Audio.setting.getYMZ280BType()[i].setUseEmu(new boolean[1]);
                Audio.setting.getYMZ280BType()[i].getUseEmu()[0] = true;
                Audio.setting.getYMZ280BType()[i].setUseReal(new boolean[1]);
            }
        }

        if (mds == null)
            mds = new MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, null);
        else
            mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, null);

        List<MDSound.Chip> lstChips = new ArrayList<>();
        MDSound.Chip chip;

        Ym2612 ym2612 = new Ym2612();
        chip = new MDSound.Chip();
        chip.type = MDSound.InstrumentType.YM2612;
        chip.id = (byte) 0;
        chip.instrument = ym2612;
        chip.update = ym2612::update;
        chip.start = ym2612::start;
        chip.stop = ym2612::stop;
        chip.reset = ym2612::reset;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getYM2612Volume();
        chip.clock = 7670454;
        chip.option = null;
        chipLED.PriOPN2 = 1;
        lstChips.add(chip);

        Sn76489 sn76489 = new Sn76489();
        chip = new MDSound.Chip();
        chip.type = MDSound.InstrumentType.SN76489;
        chip.id = (byte) 0;
        chip.instrument = sn76489;
        chip.update = sn76489::update;
        chip.start = sn76489::start;
        chip.stop = sn76489::stop;
        chip.reset = sn76489::reset;
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getSN76489Volume();
        chip.clock = 3579545;
        chip.option = null;
        chipLED.PriDCSG = 1;
        lstChips.add(chip);

        if (mdsMIDI == null)
            mdsMIDI = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
        else
            mdsMIDI.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

//        if (SoundChip.realChip == null && !getemuOnly()) {
//            Log.forcedWrite("Audio:Init:STEP 04");
//            SoundChip.realChip = new RealChip(!setting.getUnuseRealChip());
//        }
//
//        if (SoundChip.realChip != null) {
//            for (int i = 0; i < 2; i++) {
//                SoundChip.scYM2612[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2612Type()[i], 0);
//                if (SoundChip.scYM2612[i] != null) SoundChip.scYM2612[i].init();
//                SoundChip.scSN76489[i] = SoundChip.realChip.GetRealChip(Audio.setting.getSN76489Type()[i], 0);
//                if (SoundChip.scSN76489[i] != null) SoundChip.scSN76489[i].init();
//                SoundChip.scYM2608[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2608Type()[i], 0);
//                if (SoundChip.scYM2608[i] != null) SoundChip.scYM2608[i].init();
//                SoundChip.scYM2151[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2151Type()[i], 0);
//                if (SoundChip.scYM2151[i] != null) SoundChip.scYM2151[i].init();
//                SoundChip.scYM2203[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2203Type()[i], 0);
//                if (SoundChip.scYM2203[i] != null) SoundChip.scYM2203[i].init();
//                SoundChip.scAY8910[i] = SoundChip.realChip.GetRealChip(Audio.setting.getAY8910Type()[i], 0);
//                if (SoundChip.scAY8910[i] != null) SoundChip.scAY8910[i].init();
//                SoundChip.scK051649[i] = SoundChip.realChip.GetRealChip(Audio.setting.getK051649Type()[i], 0);
//                if (SoundChip.scK051649[i] != null) SoundChip.scK051649[i].init();
//                SoundChip.scYM2413[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2413Type()[i], 0);
//                if (SoundChip.scYM2413[i] != null) SoundChip.scYM2413[i].init();
//                SoundChip.scYM3526[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM3526Type()[i], 0);
//                if (SoundChip.scYM3526[i] != null) SoundChip.scYM3526[i].init();
//                SoundChip.scYM3812[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM3812Type()[i], 0);
//                if (SoundChip.scYM3812[i] != null) SoundChip.scYM3812[i].init();
//                SoundChip.scYMF262[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYMF262Type()[i], 0);
//                if (SoundChip.scYMF262[i] != null) SoundChip.scYMF262[i].init();
//                SoundChip.scYM2610[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 0);
//                if (SoundChip.scYM2610[i] != null) SoundChip.scYM2610[i].init();
//                SoundChip.scYM2610EA[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 1);
//                if (SoundChip.scYM2610EA[i] != null) SoundChip.scYM2610EA[i].init();
//                SoundChip.scYM2610EB[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 2);
//                if (SoundChip.scYM2610EB[i] != null) SoundChip.scYM2610EB[i].init();
//                SoundChip.scSEGAPCM[i] = SoundChip.realChip.GetRealChip(Audio.setting.getSEGAPCMType()[i], 0);
//                if (SoundChip.scSEGAPCM[i] != null) SoundChip.scSEGAPCM[i].init();
//                SoundChip.scC140[i] = SoundChip.realChip.GetRealChip(Audio.setting.getC140Type()[i], 0);
//                if (SoundChip.scC140[i] != null) SoundChip.scC140[i].init();
//            }
//        }

//        chipRegister = new ChipRegister(
//                setting
//                , mds
//                , SoundChip.realChip
////                , vstMng
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
//        );
        chipRegister.initChipRegister(null);

        Debug.println(Level.SEVERE, "Audio:Init:STEP 05");

        paused = false;
        stopped = true;
        _fatalError = false;
        oneTimeReset = false;

        Debug.println(Level.SEVERE, "Audio:Init:STEP 06");

//        Log.forcedWrite("Audio:Init:VST:STEP 01");
//
//        vstMng.vstparse();
//
//        Log.forcedWrite("Audio:Init:VST:STEP 02"); // Load VST instrument
//
//        // 複数のmidioutの設定から必要なVSTを絞り込む
//        Map<String, Integer> dicVst = new HashMap<>();
//        if (setting.getMidiOut().getMidiOutInfos() != null) {
//            for (MidiOutInfo[] aryMoi : setting.getMidiOut().getMidiOutInfos()) {
//                if (aryMoi == null) continue;
//                Map<String, Integer> dicVst2 = new HashMap<>();
//                for (MidiOutInfo moi : aryMoi) {
//                    if (!moi.isVST) continue;
//                    if (dicVst2.containsKey(moi.fileName)) {
//                        dicVst2.put(moi.fileName, dicVst2.get(moi.fileName + 1));
//                        continue;
//                    }
//                    dicVst2.put(moi.fileName, 1);
//                }
//
//                for (Map.Entry<String, Integer> kv : dicVst2.entrySet()) {
//                    if (dicVst.containsKey(kv.getKey())) {
//                        if (dicVst.get(kv.getKey()) < kv.getValue()) {
//                            dicVst.put(kv.getKey(), kv.getValue());
//                        }
//                        continue;
//                    }
//                    dicVst.put(kv.getKey(), kv.getValue());
//                }
//            }
//        }
//
//        for (Map.Entry<String, Integer> kv : dicVst.entrySet()) {
//            for (int i = 0; i < kv.getValue(); i++)
//                vstMng.SetUpVstInstrument(kv);
//        }
//
//
//        if (setting.getVst() != null && setting.getVst().getVSTInfo() != null) {
//            Log.forcedWrite("Audio:Init:VST:STEP 03"); // Load VST Effect
//            vstMng.SetUpVstEffect();
//        }

        Debug.println(Level.SEVERE, "Audio:Init:STEP 07");

        // midi outをリリース
        releaseAllMIDIout();

        Debug.println(Level.SEVERE, "Audio:Init:STEP 08");

        // midi  のインスタンスを作成
        makeMIDIout(setting, 1);
        chipRegister.resetAllMIDIout();

        Debug.println(Level.SEVERE, "Audio:Init:STEP 09");

        // 各外部dllの動的読み込み

        Debug.println(Level.SEVERE, "Audio:Init:STEP 10");

        naudioWrap.Start(Audio.setting);

        Debug.println(Level.SEVERE, "Audio:Init:Complete");
    }

    private static void trdIF() {
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

    private static void seqDie() {
        close();
        realChipClose();
    }

    private static void makeMIDIout(Setting setting, int m) {
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

    private static void releaseAllMIDIout() {
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

    public static MDSound.Chip getMDSChipInfo(MDSound.InstrumentType typ) {
        return chipRegister.GetChipInfo(typ);
    }

    public static int getLatency() {
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_AsioOut) {
            return setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getLatency() / 1000;
        }
        return 0; // naudioWrap.getAsioLatency(); TODO
    }

    public static void setVGMBuffer(FileFormat format, byte[] srcBuf, String playingFileName, String playingArcFileName, int midiMode, int songNo, List<Tuple<String, byte[]>> extFile) {
         // Stop();
        playingFileFormat = format;
        vgmBuf = srcBuf;
        Audio.playingFileName = playingFileName; // WaveWriter向け
        Audio.playingArcFileName = playingArcFileName;
        Audio.midiMode = midiMode;
        Audio.songNo = songNo;
        chipRegister.SetFileName(playingFileName); // ExportMIDI向け
        extendFile = extFile; // 追加ファイル
        Common.playingFilePath = Path.getDirectoryName(playingFileName);

        if (naudioFileReader != null) {
            nAudioStop();
        }

        if (format == FileFormat.WAV || format == FileFormat.MP3 || format == FileFormat.AIFF) {
            naudioFileName = playingFileName;
        } else {
            naudioFileName = null;
        }
    }

    public static void getPlayingFileName(String playingFileName, String playingArcFileName) {
        playingFileName = Audio.playingFileName;
        playingArcFileName = Audio.playingArcFileName;
    }

    public static boolean play(Setting setting) {
        errMsg = "";

        stop();

        try {
            waveWriter.open(playingFileName);
        } catch (Exception e) {
            e.printStackTrace();
            errMsg = "wave file open error.";
            return false;
        }

        MDSound.np_nes_apu_volume = 0;
        MDSound.np_nes_dmc_volume = 0;
        MDSound.np_nes_fds_volume = 0;
        MDSound.np_nes_fme7_volume = 0;
        MDSound.np_nes_mmc5_volume = 0;
        MDSound.np_nes_n106_volume = 0;
        MDSound.np_nes_vrc6_volume = 0;
        MDSound.np_nes_vrc7_volume = 0;


        if (playingFileFormat == FileFormat.MGS) {
            driverVirtual = new MGSDRV();
            driverVirtual.setting = setting;
            ((MGSDRV) driverVirtual).setPlayingFileName(playingFileName);
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new MGSDRV();
                driverReal.setting = setting;
                ((MGSDRV) driverReal).setPlayingFileName(playingFileName);
            }
            return mgsPlay_mgsdrv(setting);
        }

        if (playingFileFormat == FileFormat.MUB) {
            driverVirtual = new MucomDotNET();
            driverVirtual.setting = setting;
            ((MucomDotNET) driverVirtual).setPlayingFileName(playingFileName);
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
                driverReal = new MucomDotNET();
                driverReal.setting = setting;
                ((MucomDotNET) driverReal).setPlayingFileName(playingFileName);
            }
            return mucPlay_mucomDotNET(setting, MucomDotNET.MUCOMFileType.MUB);
        }

        if (playingFileFormat == FileFormat.MUC) {
            driverVirtual = new MucomDotNET();
            driverVirtual.setting = setting;
            ((MucomDotNET) driverVirtual).setPlayingFileName(playingFileName);
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
                driverReal = new MucomDotNET();
                driverReal.setting = setting;
                ((MucomDotNET) driverReal).setPlayingFileName(playingFileName);
            }

            return mucPlay_mucomDotNET(setting, MucomDotNET.MUCOMFileType.MUC);
        }

        if (playingFileFormat == FileFormat.MML || playingFileFormat == FileFormat.M) {
            driverVirtual = new PMDDotNET();
            driverVirtual.setting = setting;
            ((PMDDotNET) driverVirtual).setPlayingFileName(playingFileName);
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
                driverReal = new PMDDotNET();
                driverReal.setting = setting;
                ((PMDDotNET) driverReal).setPlayingFileName(playingFileName);
            }
            return mmlPlay_PMDDotNET(setting, playingFileFormat == FileFormat.MML ? 0 : 1);
        }

        if (playingFileFormat == FileFormat.NRT) {
            driverVirtual = new NRTDRV(setting);
            driverVirtual.setting = setting;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new NRTDRV(setting);
                driverReal.setting = setting;
            }
            return nrdPlay(setting);
        }

        if (playingFileFormat == FileFormat.MDR) {
            driverVirtual = new MoonDriver();
            driverVirtual.setting = setting;
            ((MoonDriver) driverVirtual).ExtendFile = (extendFile != null && extendFile.size() > 0) ? extendFile.get(0) : null;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new MoonDriver();
                driverReal.setting = setting;
                ((MoonDriver) driverReal).ExtendFile = (extendFile != null && extendFile.size() > 0) ? extendFile.get(0) : null;
            }
            return mdrPlay(setting);
        }

        if (playingFileFormat == FileFormat.MDL) {
            driverVirtual = new MoonDriverDotNET();
            driverVirtual.setting = setting;
            ((MoonDriverDotNET) driverVirtual).setPlayingFileName(playingFileName);
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null && !setting.getYM2608Type()[0].getUseEmu()[0]) {
                driverReal = new MoonDriverDotNET();
                driverReal.setting = setting;
                ((MoonDriverDotNET) driverReal).setPlayingFileName(playingFileName);
            }

            return mdlPlay_moonDriverDotNET(setting, MoonDriverDotNET.enmMoonDriverFileType.MDL);
        }

        if (playingFileFormat == FileFormat.MDX) {
            driverVirtual = new MXDRV();
            driverVirtual.setting = setting;
            ((MXDRV) driverVirtual).extendFile = (extendFile != null && extendFile.size() > 0) ? extendFile.get(0) : null;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new MXDRV();
                driverReal.setting = setting;
                ((MXDRV) driverReal).extendFile = (extendFile != null && extendFile.size() > 0) ? extendFile.get(0) : null;
            }
            return mdxPlay(setting);
        }

        if (playingFileFormat == FileFormat.MND) {
            driverVirtual = new MnDrv();
            driverVirtual.setting = setting;

            ((MnDrv) driverVirtual).extendFile = extendFile;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new MnDrv();
                driverReal.setting = setting;
                ((MnDrv) driverReal).extendFile = extendFile;
            }
            return mndPlay(setting);
        }

        if (playingFileFormat == FileFormat.XGM) {
            driverVirtual = new Xgm(setting);
            driverVirtual.setting = setting;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new Xgm(setting);
                driverReal.setting = setting;
            }

            return xgmPlay(setting);
        }

        if (playingFileFormat == FileFormat.ZGM) {
            driverVirtual = new Zgm();
            driverVirtual.setting = setting;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new Zgm();
                driverReal.setting = setting;
            }

            return zgmPlay(setting);
        }

        if (playingFileFormat == FileFormat.S98) {
            driverVirtual = new S98(setting);
            driverVirtual.setting = setting;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new S98(setting);
                driverReal.setting = setting;
            }

            return s98Play(setting);
        }

        if (playingFileFormat == FileFormat.MID) {
            driverVirtual = new MID();
            driverVirtual.setting = setting;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new MID();
                driverReal.setting = setting;
            }
            return midPlay(setting);
        }

        if (playingFileFormat == FileFormat.RCP) {
            driverVirtual = new RCP();
            driverVirtual.setting = setting;
            ((RCP) driverVirtual).ExtendFile = extendFile;
            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new RCP();
                driverReal.setting = setting;
                ((RCP) driverReal).ExtendFile = extendFile;
            }
            return rcpPlay(setting);
        }

        if (playingFileFormat == FileFormat.NSF) {
            driverVirtual = new Nsf(setting);
            driverVirtual.setting = setting;
            driverReal = null;
            //if (setting.getoutputDevice().DeviceType != Common.DEV_Null) {
            //    driverReal = new Nsf();
            //    driverReal.setting = setting;
            //}
            return nsfPlay(setting);
        }

        if (playingFileFormat == FileFormat.HES) {
            driverVirtual = new Hes();
            driverVirtual.setting = setting;

            driverReal = null;
            //if (setting.getoutputDevice().DeviceType != Common.DEV_Null) {
            //    driverReal = new Hes();
            //    driverReal.setting = setting;
            //}
            return hesPlay(setting);
        }

        if (playingFileFormat == FileFormat.SID) {
            driverVirtual = new Sid();
            driverVirtual.setting = setting;

            driverReal = null;
            //if (setting.getoutputDevice().DeviceType != Common.DEV_Null) {
            //    driverReal = new Sid.Sid();
            //    driverReal.setting = setting;
            //}
            return sidPlay(setting);
        }

        if (playingFileFormat == FileFormat.VGM) {
            driverVirtual = new Vgm(setting);
            driverVirtual.setting = setting;
            ((Vgm) driverVirtual).dacControl.chipRegister = chipRegister;
            ((Vgm) driverVirtual).dacControl.model = EnmModel.VirtualModel;


            driverReal = null;
            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                driverReal = new Vgm(setting);
                driverReal.setting = setting;
                ((Vgm) driverReal).dacControl.chipRegister = chipRegister;
                ((Vgm) driverReal).dacControl.model = EnmModel.RealModel;
            }
            return vgmPlay(setting);
        }

        try {
            if (playingFileFormat == FileFormat.WAV
                    || playingFileFormat == FileFormat.MP3
                    || playingFileFormat == FileFormat.AIFF) {
                naudioFileReader = AudioSystem.getAudioInputStream(new java.io.File(naudioFileName));
                return true;
            }
        } catch (UnsupportedAudioFileException | java.io.IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean mgsPlay_mgsdrv(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            int i = 0;
            while (vgmBuf.length > 1 && i < vgmBuf.length - 1 && (vgmBuf[i] != 0x1a || vgmBuf[i + 1] != 0x00)) {
                i++;
            }
            i += 7;
            int[] trkOffsets = new int[18];
            for (int t = 0; t < trkOffsets.length; t++) {
                trkOffsets[t] = vgmBuf[i + t * 2] + vgmBuf[i + t * 2 + 1] * 0x100;
            }
            boolean useAY = (trkOffsets[0] + trkOffsets[1] + trkOffsets[2] != 0);
            boolean useSCC = (trkOffsets[3] + trkOffsets[4] + trkOffsets[5] + trkOffsets[6] + trkOffsets[7] != 0);
            boolean useOPLL = (trkOffsets[8] + trkOffsets[9] + trkOffsets[10]
                    + trkOffsets[11] + trkOffsets[12] + trkOffsets[13]
                    + trkOffsets[14] + trkOffsets[15] + trkOffsets[16]
                    + trkOffsets[17]
                    != 0);

            chipRegister.resetChips();
            resetFadeOutParam();
            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            masterVolume = setting.getBalance().getMasterVolume();

            if (useAY) {
                Ay8910 ay8910;
                chip = new MDSound.Chip();
                ay8910 = new Ay8910();
                chip.id = 0;
                chipLED.PriAY10 = 1;
                chip.type = MDSound.InstrumentType.AY8910;
                chip.instrument = ay8910;
                chip.update = ay8910::update;
                chip.start = ay8910::start;
                chip.stop = ay8910::stop;
                chip.reset = ay8910::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getAY8910Volume();
                chip.clock = MGSDRV.baseclockAY8910 / 2;
                chip.option = null;
                lstChips.add(chip);
                useChip.add(EnmChip.AY8910);
                clockAY8910 = MGSDRV.baseclockAY8910;
            }

            if (useOPLL) {
                Ym2413 ym2413;
                chip = new MDSound.Chip();
                ym2413 = new Ym2413();
                chip.id = 0;
                chipLED.PriOPLL = 1;
                chip.type = MDSound.InstrumentType.YM2413;
                chip.instrument = ym2413;
                chip.update = ym2413::update;
                chip.start = ym2413::start;
                chip.stop = ym2413::stop;
                chip.reset = ym2413::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2413Volume();
                chip.clock = MGSDRV.baseclockYM2413;
                chip.option = null;
                lstChips.add(chip);
                useChip.add(EnmChip.YM2413);
                clockYM2413 = MGSDRV.baseclockYM2413;
            }

            if (useSCC) {
                K051649 K051649;
                chip = new MDSound.Chip();
                K051649 = new K051649();
                chip.id = 0;
                chipLED.PriK051649 = 1;
                chip.type = MDSound.InstrumentType.K051649;
                chip.instrument = K051649;
                chip.update = K051649::update;
                chip.start = K051649::start;
                chip.stop = K051649::stop;
                chip.reset = K051649::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getK051649Volume();
                chip.clock = MGSDRV.baseclockK051649;
                chip.option = null;
                lstChips.add(chip);
                useChip.add(EnmChip.K051649);
                clockK051649 = MGSDRV.baseclockK051649;
            }

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.AY8910, EnmChip.YM2413, EnmChip.K051649}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.AY8910}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            paused = false;
            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean mucPlay_mucomDotNET(Setting setting, MucomDotNET.MUCOMFileType fileType) {

        try {

            if (vgmBuf == null || setting == null) return false;

            if (fileType == MucomDotNET.MUCOMFileType.MUC) {
                vgmBuf = ((MucomDotNET) driverVirtual).compile(vgmBuf);
            }
            EnmChip[] useChipFromMub = ((MucomDotNET) driverVirtual).useChipsFromMub(vgmBuf);

            //Stop();
            chipRegister.resetChips();
            resetFadeOutParam();
            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            masterVolume = setting.getBalance().getMasterVolume();

            Ym2608 ym2608;
            ym2608 = new Ym2608();
            Ym2610 ym2610;
            ym2610 = new Ym2610();
            Ym2151 ym2151;
            ym2151 = new Ym2151();
            Function<String, Stream> fn = Common::getOPNARyhthmStream;

            if (useChipFromMub[0] != EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chipLED.PriOPNA = 1;
                chip.type = MDSound.InstrumentType.YM2608;
                chip.instrument = ym2608;
                chip.update = ym2608::update;
                chip.start = ym2608::start;
                chip.stop = ym2608::stop;
                chip.reset = ym2608::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2608Volume();
                chip.clock = MucomDotNET.opnaBaseClock;
                chip.option = new Object[] {fn};
                lstChips.add(chip);
                useChip.add(EnmChip.YM2608);
                clockYM2608 = MucomDotNET.opnaBaseClock;
            }

            if (useChipFromMub[1] != EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 1;
                chipLED.SecOPNA = 1;
                chip.type = MDSound.InstrumentType.YM2608;
                chip.instrument = ym2608;
                chip.update = ym2608::update;
                chip.start = ym2608::start;
                chip.stop = ym2608::stop;
                chip.reset = ym2608::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2608Volume();
                chip.clock = MucomDotNET.opnaBaseClock;
                chip.option = new Object[] {fn};
                lstChips.add(chip);
                useChip.add(EnmChip.S_YM2608);
            }

            if (useChipFromMub[2] != EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chipLED.PriOPNB = 1;
                chip.type = MDSound.InstrumentType.YM2610;
                chip.instrument = ym2610;
                chip.update = ym2610::update;
                chip.start = ym2610::start;
                chip.stop = ym2610::stop;
                chip.reset = ym2610::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2610Volume();
                chip.clock = MucomDotNET.opnbBaseClock;
                chip.option = null;
                lstChips.add(chip);
                useChip.add(EnmChip.YM2610);
                clockYM2610 = MucomDotNET.opnbBaseClock;
            }

            if (useChipFromMub[3] != EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 1;
                chipLED.SecOPNB = 1;
                chip.type = MDSound.InstrumentType.YM2610;
                chip.instrument = ym2610;
                chip.update = ym2610::update;
                chip.start = ym2610::start;
                chip.stop = ym2610::stop;
                chip.reset = ym2610::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2610Volume();
                chip.clock = MucomDotNET.opnbBaseClock;
                chip.option = null;
                lstChips.add(chip);
                useChip.add(EnmChip.S_YM2610);
            }

            if (useChipFromMub[4] != EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chipLED.PriOPM = 1;
                chip.type = MDSound.InstrumentType.YM2151;
                chip.instrument = ym2151;
                chip.update = ym2151::update;
                chip.start = ym2151::start;
                chip.stop = ym2151::stop;
                chip.reset = ym2151::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = MucomDotNET.opmBaseClock;
                chip.option = null;
                lstChips.add(chip);
                useChip.add(EnmChip.YM2151);
            }

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            setYM2608Volume(true, setting.getBalance().getYM2608Volume());
            setYM2608FMVolume(true, setting.getBalance().getYM2608FMVolume());
            setYM2608PSGVolume(true, setting.getBalance().getYM2608PSGVolume());
            setYM2608RhythmVolume(true, setting.getBalance().getYM2608RhythmVolume());
            setYM2608AdpcmVolume(true, setting.getBalance().getYM2608AdpcmVolume());

            chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
            chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
            chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.RealModel);

            chipRegister.writeYM2608Clock((byte) 0, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
            chipRegister.writeYM2608Clock((byte) 1, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
            chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);


            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            paused = false;

            if (driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//                SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
            }

            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean mmlPlay_PMDDotNET(Setting setting, int fileType) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();
            resetFadeOutParam();
            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            masterVolume = setting.getBalance().getMasterVolume();

            Ym2608 ym2608;
            chip = new MDSound.Chip();
            ym2608 = new Ym2608();
            chip.id = 0;
            chipLED.PriOPNA = 1;
            chip.type = MDSound.InstrumentType.YM2608;
            chip.instrument = ym2608;
            chip.update = ym2608::update;
            chip.start = ym2608::start;
            chip.stop = ym2608::stop;
            chip.reset = ym2608::reset;
            chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getYM2608Volume();
            chip.clock = PMDDotNET.baseclock;
            Function<String, Stream> fn = Common::getOPNARyhthmStream;
            chip.option = new Object[] {fn};
            lstChips.add(chip);
            useChip.add(EnmChip.YM2608);
            clockYM2608 = PMDDotNET.baseclock;

            mdsound.PPZ8 ppz8;
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            ppz8 = new mdsound.PPZ8();
            chip.type = MDSound.InstrumentType.PPZ8;
            chip.instrument = ppz8;
            chip.update = ppz8::update;
            chip.start = ppz8::start;
            chip.stop = ppz8::stop;
            chip.reset = ppz8::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getPPZ8Volume();
            chip.clock = PMDDotNET.baseclock;
            chip.option = null;
            chipLED.PriPPZ8 = 1;
            lstChips.add(chip);
            useChip.add(EnmChip.PPZ8);


            mdsound.PPSDRV ppsdrv;
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            ppsdrv = new PPSDRV();
            chip.type = MDSound.InstrumentType.PPSDRV;
            chip.instrument = ppsdrv;
            chip.update = ppsdrv::update;
            chip.start = ppsdrv::start;
            chip.stop = ppsdrv::stop;
            chip.reset = ppsdrv::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = 0;// setting.getbalance().getPPZ8Volume;
            chip.clock = PMDDotNET.baseclock;
            chip.option = null;
            chipLED.PriPPSDRV = 1;
            lstChips.add(chip);
            useChip.add(EnmChip.PPSDRV);


            mdsound.P86 P86;
            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            P86 = new mdsound.P86();
            chip.type = MDSound.InstrumentType.P86;
            chip.instrument = P86;
            chip.update = P86::update;
            chip.start = P86::start;
            chip.stop = P86::stop;
            chip.reset = P86::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = 0;// setting.getbalance().getP86Volume;
            chip.clock = PMDDotNET.baseclock;
            chip.option = null;
            chipLED.PriP86 = 1;
            lstChips.add(chip);
            useChip.add(EnmChip.P86);


            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            setYM2608Volume(true, setting.getBalance().getYM2608Volume());
            setYM2608FMVolume(true, setting.getBalance().getYM2608FMVolume());
            setYM2608PSGVolume(true, setting.getBalance().getYM2608PSGVolume());
            setYM2608RhythmVolume(true, setting.getBalance().getYM2608RhythmVolume());
            setYM2608AdpcmVolume(true, setting.getBalance().getYM2608AdpcmVolume());

            chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
            chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
            chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
            chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
            chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.RealModel);

            chipRegister.writeYM2608Clock((byte) 0, PMDDotNET.baseclock, EnmModel.RealModel);
            chipRegister.writeYM2608Clock((byte) 1, PMDDotNET.baseclock, EnmModel.RealModel);
            chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);


            if (!driverVirtual.init(vgmBuf, fileType, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, fileType, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            paused = false;

            if (driverReal != null && setting.getYM2608Type()[0].getUseReal()[0]) {
//                SoundChip.realChip.WaitOPNADPCMData(setting.getYM2608Type()[0].getRealChipInfo()[0].getSoundLocation() == -1);
            }

            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean mdlPlay_moonDriverDotNET(Setting setting, MoonDriverDotNET.enmMoonDriverFileType fileType) {

        try {

            if (vgmBuf == null || setting == null) return false;

            if (fileType == MoonDriverDotNET.enmMoonDriverFileType.MDL) {
                vgmBuf = ((MoonDriverDotNET) driverVirtual).Compile(vgmBuf);
            }
            EnmChip[] useChipFromMdr = new EnmChip[] {EnmChip.YMF278B};

            //Stop();
            chipRegister.resetChips();
            resetFadeOutParam();
            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            masterVolume = setting.getBalance().getMasterVolume();

            YmF278b ymf278b = new YmF278b();
            //Func<String, Stream> fn = Common.GetOPNARyhthmStream;

            if (useChipFromMdr[0] != EnmChip.Unuse) {
                chip = new MDSound.Chip();
                chip.id = 0;
                chipLED.PriOPL4 = 1;
                chip.type = MDSound.InstrumentType.YMF278B;
                chip.instrument = ymf278b;
                chip.update = ymf278b::update;
                chip.start = ymf278b::start;
                chip.stop = ymf278b::stop;
                chip.reset = ymf278b::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYMF278BVolume();
                chip.clock = 33868800;
                chip.option = null;// new Object[] { fn };
                lstChips.add(chip);
                useChip.add(EnmChip.YMF278B);
                //clockYM2608 = MucomDotNET.opnaBaseClock;
            }

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            //SetYM2608Volume(true, setting.getbalance().getYM2608Volume);
            //SetYM2608FMVolume(true, setting.getbalance().getYM2608FMVolume);
            //SetYM2608PSGVolume(true, setting.getbalance().getYM2608PSGVolume);
            //SetYM2608RhythmVolume(true, setting.getbalance().getYM2608RhythmVolume);
            //SetYM2608AdpcmVolume(true, setting.getbalance().getYM2608AdpcmVolume);

            //chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
            //chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
            //chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x08, 0x00, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x09, 0x00, EnmModel.RealModel);
            //chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.VirtualModel);
            //chipRegister.setYM2608Register(0, 0, 0x0a, 0x00, EnmModel.RealModel);

            //chipRegister.writeYM2608Clock(0, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
            //chipRegister.writeYM2608Clock(1, MucomDotNET.opnaBaseClock, EnmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, EnmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, EnmModel.RealModel);


            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YMF278B}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YMF278B}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            //Play

            paused = false;

            //if (driverReal != null && setting.getYMF278BType()[0].getUseReal()[0])
            //{
            //    realChip.WaitOPL4PCMData(setting.getYMF278BType()[0].getrealChipInfo()[0].getSoundLocation() == -1);
            //}

            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public static boolean nrdPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            int r = ((NRTDRV) driverVirtual).checkUseChip(vgmBuf);

            chipRegister.setFadeoutVolYM2151(0, 0);
            chipRegister.setFadeoutVolYM2151(1, 0);

            chipRegister.resetChips();

            useChip.clear();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;
            clearFadeoutVolume();
            chipRegister.resetChips();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            mdsound.Ym2151 ym2151 = null;
            mdsound.Ym2151Mame ym2151_mame = null;
            mdsound.Ym2151X68Sound ym2151_x68sound = null;
            for (int i = 0; i < 2; i++) {
                if ((i == 0 && (r & 0x3) != 0) || (i == 1 && (r & 0x2) != 0)) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;

                    if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[0]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[0])) {
                        if (ym2151 == null) ym2151 = new mdsound.Ym2151();
                        chip.type = MDSound.InstrumentType.YM2151;
                        chip.instrument = ym2151;
                        chip.update = ym2151::update;
                        chip.start = ym2151::start;
                        chip.stop = ym2151::stop;
                        chip.reset = ym2151::reset;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                        if (ym2151_mame == null) ym2151_mame = new mdsound.Ym2151Mame();
                        chip.type = MDSound.InstrumentType.YM2151mame;
                        chip.instrument = ym2151_mame;
                        chip.update = ym2151_mame::update;
                        chip.start = ym2151_mame::start;
                        chip.stop = ym2151_mame::stop;
                        chip.reset = ym2151_mame::reset;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                        if (ym2151_x68sound == null) ym2151_x68sound = new mdsound.Ym2151X68Sound();
                        chip.type = MDSound.InstrumentType.YM2151x68sound;
                        chip.instrument = ym2151_x68sound;
                        chip.update = ym2151_x68sound::update;
                        chip.start = ym2151_x68sound::start;
                        chip.stop = ym2151_x68sound::stop;
                        chip.reset = ym2151_x68sound::reset;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2151Volume();
                    chip.clock = 4000000;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPM = 1;
                    else chipLED.SecOPM = 1;

                    if (chip.start != null) {
                        lstChips.add(chip);
                        useChip.add(i == 0 ? EnmChip.YM2151 : EnmChip.S_YM2151);
                    }
                }
            }

            if ((r & 0x4) != 0) {
                mdsound.Ay8910 ay8910 = new mdsound.Ay8910();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.AY8910;
                chip.id = (byte) 0;
                chip.instrument = ay8910;
                chip.update = ay8910::update;
                chip.start = ay8910::start;
                chip.stop = ay8910::stop;
                chip.reset = ay8910::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getAY8910Volume();
                chip.clock = 2000000 / 2;
                clockAY8910 = chip.clock;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x1;
                chipLED.PriAY10 = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.AY8910);
            }

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (useChip.contains(EnmChip.YM2151) || useChip.contains(EnmChip.S_YM2151))
                setYM2151Volume(true, setting.getBalance().getYM2151Volume());
            if (useChip.contains(EnmChip.AY8910))
                setAY8910Volume(true, setting.getBalance().getAY8910Volume());

            if (useChip.contains(EnmChip.YM2151))
                chipRegister.writeYM2151Clock((byte) 0, 4000000, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2151))
                chipRegister.writeYM2151Clock((byte) 1, 4000000, EnmModel.RealModel);

            if (driverVirtual != null) driverVirtual.setYm2151Hosei(4000000);
            if (driverReal != null) driverReal.setYm2151Hosei(4000000);
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);


            if (driverVirtual != null) {
                driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YM2151, EnmChip.AY8910}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
                ((NRTDRV) driverVirtual).call(0);//
            }

            if (driverReal != null) {
                driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YM2151, EnmChip.AY8910}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
                ((NRTDRV) driverReal).call(0);//
            }


            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            ((NRTDRV) driverVirtual).call(1); // MPLAY

            if (driverReal != null) {
                ((NRTDRV) driverReal).call(1); // MPLAY
            }


            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    private static void clearFadeoutVolume() {
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

    public static boolean mdrPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            //int r = ((NRTDRV)driverVirtual).checkUseChip(vgmBuf);

            chipRegister.setFadeoutVolYM2151(0, 0);
            chipRegister.setFadeoutVolYM2151(1, 0);

            chipRegister.resetChips();
            useChip.clear();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            byte sg = vgmBuf[7];

            boolean isOPL3 = (sg & 2) != 0;

            if (isOPL3) {
                YmF262 ymf262 = new YmF262();

                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YMF262;
                chip.id = 0;
                chip.instrument = ymf262;
                chip.update = ymf262::update;
                chip.start = ymf262::start;
                chip.stop = ymf262::stop;
                chip.reset = ymf262::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYMF262Volume();
                chip.clock = 14318180;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                chipLED.PriOPL3 = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.YMF262);
            } else {
                YmF278b ymf278b = new YmF278b();

                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YMF278B;
                chip.id = 0;
                chip.instrument = ymf278b;
                chip.update = ymf278b::update;
                chip.start = ymf278b::start;
                chip.stop = ymf278b::stop;
                chip.reset = ymf278b::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYMF278BVolume();
                chip.clock = 33868800;
                chip.option = new Object[] {Common.getApplicationFolder()};

                hiyorimiDeviceFlag |= 0x2;

                chipLED.PriOPL4 = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.YMF278B);
            }

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (isOPL3) setYMF262Volume(true, setting.getBalance().getYMF262Volume());
            else setYMF278BVolume(true, setting.getBalance().getYMF278BVolume());
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            ((MoonDriver) driverVirtual).isOPL3 = isOPL3;
            ((MoonDriver) driverReal).isOPL3 = isOPL3;

            driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            if (driverReal != null) {
                driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
            }

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean mdxPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;
            if (setting.getOutputDevice().getSampleRate() != 44100) {
                return false;
            }
            //Stop();

            chipRegister.resetChips();
            useChip.clear();
            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            startTrdVgmReal();

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 3;

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip = null;

            if (setting.getYM2151Type()[0].getUseEmu()[0]) {
                mdsound.Ym2151 ym2151 = new mdsound.Ym2151();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2151;
                chip.id = (byte) 0;
                chip.instrument = ym2151;
                chip.update = ym2151::update;
                chip.start = ym2151::start;
                chip.stop = ym2151::stop;
                chip.reset = ym2151::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[1]) {
                mdsound.Ym2151Mame ym2151mame = new mdsound.Ym2151Mame();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2151mame;
                chip.id = (byte) 0;
                chip.instrument = ym2151mame;
                chip.update = ym2151mame::update;
                chip.start = ym2151mame::start;
                chip.stop = ym2151mame::stop;
                chip.reset = ym2151mame::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[2]) {
                mdsound.Ym2151X68Sound mdxOPM = new mdsound.Ym2151X68Sound();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2151x68sound;
                chip.id = (byte) 0;
                chip.instrument = mdxOPM;
                chip.update = mdxOPM::update;
                chip.start = mdxOPM::start;
                chip.stop = mdxOPM::stop;
                chip.reset = mdxOPM::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = 4000000;
                chip.option = new Object[] {1, 0, 0};
            }
            if (chip != null) {
                lstChips.add(chip);
            }
            useChip.add(EnmChip.YM2151);

            mdsound.Ym2151X68Sound mdxPCM_V = new mdsound.Ym2151X68Sound();
            mdxPCM_V.x68sound[0] = new X68Sound();
            mdxPCM_V.sound_Iocs[0] = new SoundIocs(mdxPCM_V.x68sound[0]);
            mdsound.Ym2151X68Sound mdxPCM_R = new mdsound.Ym2151X68Sound();
            mdxPCM_R.x68sound[0] = new X68Sound();
            mdxPCM_R.sound_Iocs[0] = new SoundIocs(mdxPCM_R.x68sound[0]);
            useChip.add(EnmChip.OKIM6258);

            chipLED.PriOPM = 1;
            chipLED.PriOKI5 = 1;


            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            setYM2151Volume(true, setting.getBalance().getYM2151Volume());

            if (useChip.contains(EnmChip.YM2151))
                chipRegister.writeYM2151Clock((byte) 0, 4000000, EnmModel.RealModel);
            //chipRegister.writeYM2151Clock(1, 4000000, enmModel.RealModel);

            driverVirtual.setYm2151Hosei(4000000);
            if (driverReal != null) driverReal.setYm2151Hosei(4000000);
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            boolean retV = ((mdplayer.driver.mxdrv.MXDRV) driverVirtual).init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
                    , mdxPCM_V);
            boolean retR = true;
            if (driverReal != null) {
                retR = ((mdplayer.driver.mxdrv.MXDRV) driverReal).init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
                        , mdxPCM_R);
            }

            if (!retV || !retR) {
                errMsg = !driverVirtual.errMsg.isEmpty() ? driverVirtual.errMsg : (driverReal != null ? driverReal.errMsg : "");
                return false;
            }

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean mndPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 3;

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            List<MDSound.Chip> lstChips = new ArrayList<>();
            MDSound.Chip chip = null;

            if (setting.getYM2151Type()[0].getUseEmu()[0]) {
                mdsound.Ym2151 ym2151 = new mdsound.Ym2151();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2151;
                chip.id = (byte) 0;
                chip.instrument = ym2151;
                chip.update = ym2151::update;
                chip.start = ym2151::start;
                chip.stop = ym2151::stop;
                chip.reset = ym2151::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[1]) {
                mdsound.Ym2151Mame ym2151mame = new mdsound.Ym2151Mame();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2151mame;
                chip.id = (byte) 0;
                chip.instrument = ym2151mame;
                chip.update = ym2151mame::update;
                chip.start = ym2151mame::start;
                chip.stop = ym2151mame::stop;
                chip.reset = ym2151mame::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = 4000000;
                chip.option = null;
            } else if (setting.getYM2151Type()[0].getUseEmu()[2]) {
                mdsound.Ym2151X68Sound mdxOPM = new mdsound.Ym2151X68Sound();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2151x68sound;
                chip.id = (byte) 0;
                chip.instrument = mdxOPM;
                chip.update = mdxOPM::update;
                chip.start = mdxOPM::start;
                chip.stop = mdxOPM::stop;
                chip.reset = mdxOPM::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2151Volume();
                chip.clock = 4000000;
                chip.option = new Object[] {1, 0, 0};
            }
            if (chip != null) {
                lstChips.add(chip);
            }
            useChip.add(EnmChip.YM2151);

            mdsound.Ym2608 opna = new Ym2608();
            if (setting.getYM2608Type()[0].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2608;
                chip.id = (byte) 0;
                chip.instrument = opna;
                chip.update = opna::update;
                chip.start = opna::start;
                chip.stop = opna::stop;
                chip.reset = opna::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2608Volume();
                chip.clock = 8000000;// 7987200;
                Function<String, Stream> fn = Common::getOPNARyhthmStream;
                chip.option = new Object[] {fn};
                lstChips.add(chip);
                clockYM2608 = 8000000;
            }
            useChip.add(EnmChip.YM2608);

            if (setting.getYM2608Type()[1].getUseEmu()[0]) {
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.YM2608;
                chip.id = (byte) 1;
                chip.instrument = opna;
                chip.update = opna::update;
                chip.start = opna::start;
                chip.stop = opna::stop;
                chip.reset = opna::reset;
                chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getYM2608Volume();
                chip.clock = 8000000;// 7987200;
                chip.option = new Object[] {Common.getApplicationFolder()};
                lstChips.add(chip);
                clockYM2608 = 8000000;
            }
            useChip.add(EnmChip.S_YM2608);

            mdsound.MPcmX68k mpcm = new MPcmX68k();
            chip = new MDSound.Chip();
            chip.type = MDSound.InstrumentType.mpcmX68k;
            chip.id = (byte) 0;
            chip.instrument = mpcm;
            chip.update = mpcm::update;
            chip.start = mpcm::start;
            chip.stop = mpcm::stop;
            chip.reset = mpcm::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getOKIM6258Volume();
            chip.clock = 15600;
            chip.option = new Object[] {Common.getApplicationFolder()};
            lstChips.add(chip);
            useChip.add(EnmChip.OKIM6258);

            chipLED.PriOPM = 1;
            chipLED.PriOPNA = 1;
            chipLED.SecOPNA = 1;
            chipLED.PriOKI5 = 1;

            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (useChip.contains(EnmChip.YM2151) || useChip.contains(EnmChip.S_YM2151))
                setYM2151Volume(true, setting.getBalance().getYM2151Volume());

            if (useChip.contains(EnmChip.YM2608) || useChip.contains(EnmChip.S_YM2608)) {
                setYM2608Volume(true, setting.getBalance().getYM2608Volume());
                setYM2608FMVolume(true, setting.getBalance().getYM2608FMVolume());
                setYM2608PSGVolume(true, setting.getBalance().getYM2608PSGVolume());
                setYM2608RhythmVolume(true, setting.getBalance().getYM2608RhythmVolume());
                setYM2608AdpcmVolume(true, setting.getBalance().getYM2608AdpcmVolume());
            }

            Thread.sleep(500);

            if (useChip.contains(EnmChip.YM2608)) {
                chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.VirtualModel);
                chipRegister.setYM2608Register(0, 0, 0x2d, 0x00, EnmModel.RealModel);
                chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
                chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
                chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
                chipRegister.setYM2608Register(0, 0, 0x07, 0x38, EnmModel.RealModel);
                chipRegister.writeYM2608Clock((byte) 0, 8000000, EnmModel.RealModel);
                chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            }

            if (useChip.contains(EnmChip.S_YM2608)) {
                chipRegister.setYM2608Register(1, 0, 0x2d, 0x00, EnmModel.VirtualModel);
                chipRegister.setYM2608Register(1, 0, 0x2d, 0x00, EnmModel.RealModel);
                chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
                chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
                chipRegister.setYM2608Register(1, 0, 0x07, 0x38, EnmModel.VirtualModel); // Psg TONE でリセット
                chipRegister.setYM2608Register(1, 0, 0x07, 0x38, EnmModel.RealModel);
                chipRegister.writeYM2608Clock((byte) 1, 8000000, EnmModel.RealModel);
                chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            }

            if (useChip.contains(EnmChip.YM2151))
                chipRegister.writeYM2151Clock((byte) 0, 4000000, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2151))
                chipRegister.writeYM2151Clock((byte) 1, 4000000, EnmModel.RealModel);

            driverVirtual.setYm2151Hosei(4000000);
            if (driverReal != null) driverReal.setYm2151Hosei(4000000);

            if (useChip.contains(EnmChip.YM2203))
                chipRegister.setYM2203SSGVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2203))
                chipRegister.setYM2203SSGVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);

            boolean retV = driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YM2151, EnmChip.YM2608}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
            );
            boolean retR = true;
            if (driverReal != null) {
                retR = driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YM2151, EnmChip.YM2608}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000
                );
            }

            if (!retV || !retR) {
                errMsg = !driverVirtual.errMsg.isEmpty() ? driverVirtual.errMsg : (driverReal != null ? driverReal.errMsg : "");
                return false;
            }

            ((MnDrv) driverVirtual).mpcm = mpcm;

            paused = false;
            oneTimeReset = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean xgmPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            chip = new MDSound.Chip();
            chip.id = (byte) 0;
            chip.option = null;
            mdsound.Ym2612 ym2612;
            mdsound.Ym3438 ym3438;
            mdsound.Ym2612Mame ym2612mame;

            if (setting.getYM2612Type()[0].getUseEmu()[0]) {
                ym2612 = new Ym2612();
                chip.type = MDSound.InstrumentType.YM2612;
                chip.instrument = ym2612;
                chip.update = ym2612::update;
                chip.start = ym2612::start;
                chip.stop = ym2612::stop;
                chip.reset = ym2612::reset;
                chip.option = new Object[] {
                                (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00)
                                        | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                        };
            } else if (setting.getYM2612Type()[0].getUseEmu()[1]) {
                ym3438 = new Ym3438();
                chip.type = MDSound.InstrumentType.YM3438;
                chip.instrument = ym3438;
                chip.update = ym3438::update;
                chip.start = ym3438::start;
                chip.stop = ym3438::stop;
                chip.reset = ym3438::reset;
                switch (setting.getNukedOPN2().emuType) {
                case 0:
                    ym3438.setChipType(Type.discrete);
                    break;
                case 1:
                    ym3438.setChipType(Type.asic);
                    break;
                case 2:
                    ym3438.setChipType(Type.ym2612);
                    break;
                case 3:
                    ym3438.setChipType(Type.ym2612_u);
                    break;
                case 4:
                    ym3438.setChipType(Type.asic_lp);
                    break;
                }
            } else if (setting.getYM2612Type()[0].getUseEmu()[2]) {
                ym2612mame = new Ym2612Mame();
                chip.type = MDSound.InstrumentType.YM2612mame;
                chip.instrument = ym2612mame;
                chip.update = ym2612mame::update;
                chip.start = ym2612mame::start;
                chip.stop = ym2612mame::stop;
                chip.reset = ym2612mame::reset;
            }

            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getYM2612Volume();
            chip.clock = 7670454;
            clockYM2612 = 7670454;
            chipLED.PriOPN2 = 1;
            lstChips.add(chip);
            useChip.add(EnmChip.YM2612);

            Sn76489 sn76489 = new Sn76489();
            chip = new MDSound.Chip();
            chip.type = MDSound.InstrumentType.SN76489;
            chip.id = (byte) 0;
            chip.instrument = sn76489;
            chip.update = sn76489::update;
            chip.start = sn76489::start;
            chip.stop = sn76489::stop;
            chip.reset = sn76489::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getSN76489Volume();
            chip.clock = 3579545;
            chip.option = null;
            chipLED.PriDCSG = 1;
            lstChips.add(chip);
            useChip.add(EnmChip.SN76489);

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            setYM2612Volume(true, setting.getBalance().getYM2612Volume());
            setSN76489Volume(true, setting.getBalance().getSN76489Volume());
            //chipRegister.setYM2203SSGVolume(0, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2203SSGVolume(1, setting.getbalance().getGimicOPNVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(0, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);
            //chipRegister.setYM2608SSGVolume(1, setting.getbalance().getGimicOPNAVolume, enmModel.RealModel);

            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YM2612, EnmChip.SN76489}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YM2612, EnmChip.SN76489}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }
            // Play

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean zgmPlay(Setting setting) {
        if (vgmBuf == null || setting == null) return false;

        try {
            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            // MIDIに対応するまで封印
            // startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            if (!driverVirtual.init(vgmBuf
                    , chipRegister
                    , EnmModel.VirtualModel
                    , new EnmChip[] {EnmChip.YM2203}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            // MIDIに対応するまで封印
            //if (driverReal != null && !driverReal.init(vgmBuf
            //    , chipRegister
            //    , EnmModel.RealModel
            //    , new EnmChip[] { EnmChip.YM2203 }
            //    , (int)(setting.getoutputDevice().getSampleRate() * setting.LatencySCCI / 1000)
            //    , (int)(setting.getoutputDevice().getSampleRate() * setting.getoutputDevice().getWaitTime() / 1000)))
            //    return false;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            //
            //chips initialization
            //


            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean s98Play(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.YM2203}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.YM2203}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            List<S98.S98DevInfo> s98DInfo = ((S98) driverVirtual).s98Info.DeviceInfos;

            Ay8910 ym2149 = null;
            Ym2203 ym2203 = null;
            Ym2612 ym2612 = null;
            Ym3438 ym3438 = null;
            Ym2612Mame ym2612mame = null;
            Ym2608 ym2608 = null;
            Ym2151 ym2151 = null;
            Ym2151Mame ym2151mame = null;
            Ym2151X68Sound ym2151_x68sound = null;
            Ym2413 ym2413 = null;
            Ym3526 ym3526 = null;
            Ym3812 ym3812 = null;
            YmF262 ymf262 = null;
            Ay8910 ay8910 = null;

            int YM2151ClockValue = 4000000;
            int YM2203ClockValue = 4000000;
            int YM2608ClockValue = 8000000;
            int YMF262ClockValue = 14318180;
            useChip.clear();

            for (S98.S98DevInfo dInfo : s98DInfo) {
                switch (dInfo.DeviceType) {
                case 1:
                    chip = new MDSound.Chip();
                    if (ym2149 == null) {
                        ym2149 = new Ay8910();
                        chip.id = 0;
                        chipLED.PriAY10 = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecAY10 = 1;
                    }
                    chip.type = MDSound.InstrumentType.AY8910;
                    chip.instrument = ym2149;
                    chip.update = ym2149::update;
                    chip.start = ym2149::start;
                    chip.stop = ym2149::stop;
                    chip.reset = ym2149::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getAY8910Volume();
                    chip.clock = dInfo.clock / 4;
                    clockAY8910 = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.AY8910 : EnmChip.S_AY8910);
                    break;
                case 2:
                    chip = new MDSound.Chip();
                    if (ym2203 == null) {
                        ym2203 = new Ym2203();
                        chip.id = 0;
                        chipLED.PriOPN = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPN = 1;
                    }
                    chip.type = MDSound.InstrumentType.YM2203;
                    chip.instrument = ym2203;
                    chip.update = ym2203::update;
                    chip.start = ym2203::start;
                    chip.stop = ym2203::stop;
                    chip.reset = ym2203::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2203Volume();
                    chip.clock = dInfo.clock;
                    YM2203ClockValue = chip.clock;
                    chip.option = null;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM2203 : EnmChip.S_YM2203);

                    break;
                case 3:
                    chip = new MDSound.Chip();
                    chip.option = null;
                    if (ym2612 == null) {
                        ym2612 = new Ym2612();
                        ym3438 = new Ym3438();
                        ym2612mame = new Ym2612Mame();
                        chip.id = 0;
                        chipLED.PriOPN2 = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPN2 = 1;
                    }

                    if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[0]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[0])) {
                        chip.type = MDSound.InstrumentType.YM2612;
                        chip.instrument = ym2612;
                        chip.update = ym2612::update;
                        chip.start = ym2612::start;
                        chip.stop = ym2612::stop;
                        chip.reset = ym2612::reset;
                        chip.option = new Object[] {
                                        (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00)
                                                | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                                };
                    } else if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[1]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[1])) {
                        chip.type = MDSound.InstrumentType.YM3438;
                        chip.instrument = ym3438;
                        chip.update = ym3438::update;
                        chip.start = ym3438::start;
                        chip.stop = ym3438::stop;
                        chip.reset = ym3438::reset;
                        switch (setting.getNukedOPN2().emuType) {
                        case 0:
                            ym3438.setChipType(Type.discrete);
                            break;
                        case 1:
                            ym3438.setChipType(Type.asic);
                            break;
                        case 2:
                            ym3438.setChipType(Type.ym2612);
                            break;
                        case 3:
                            ym3438.setChipType(Type.ym2612_u);
                            break;
                        case 4:
                            ym3438.setChipType(Type.asic_lp);
                            break;
                        }
                    } else if ((chip.id == 0 && setting.getYM2612Type()[0].getUseEmu()[2]) || (chip.id == 1 && setting.getYM2612Type()[1].getUseEmu()[2])) {
                        chip.type = MDSound.InstrumentType.YM2612mame;
                        chip.instrument = ym2612mame;
                        chip.update = ym2612mame::update;
                        chip.start = ym2612mame::start;
                        chip.stop = ym2612mame::stop;
                        chip.reset = ym2612mame::reset;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2612Volume();
                    chip.clock = dInfo.clock;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM2612 : EnmChip.S_YM2612);

                    break;
                case 4:
                    chip = new MDSound.Chip();
                    if (ym2608 == null) {
                        ym2608 = new Ym2608();
                        chip.id = 0;
                        chipLED.PriOPNA = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPNA = 1;
                    }
                    chip.type = MDSound.InstrumentType.YM2608;
                    chip.instrument = ym2608;
                    chip.update = ym2608::update;
                    chip.start = ym2608::start;
                    chip.stop = ym2608::stop;
                    chip.reset = ym2608::reset;
                    chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2608Volume();
                    chip.clock = dInfo.clock;
                    YM2608ClockValue = chip.clock;
                    Function<String, Stream> fn = Common::getOPNARyhthmStream;
                    chip.option = new Object[] {fn};
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM2608 : EnmChip.S_YM2608);

                    break;
                case 5:
                    chip = new MDSound.Chip();
                    if (ym2151 == null && ym2151mame == null) {
                        chip.id = 0;
                        chipLED.PriOPM = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPM = 1;
                    }

                    if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[0]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[0])) {
                        if (ym2151 == null) ym2151 = new mdsound.Ym2151();
                        chip.type = MDSound.InstrumentType.YM2151;
                        chip.instrument = ym2151;
                        chip.update = ym2151::update;
                        chip.start = ym2151::start;
                        chip.stop = ym2151::stop;
                        chip.reset = ym2151::reset;
                    } else if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                        if (ym2151mame == null) ym2151mame = new mdsound.Ym2151Mame();
                        chip.type = MDSound.InstrumentType.YM2151mame;
                        chip.instrument = ym2151mame;
                        chip.update = ym2151mame::update;
                        chip.start = ym2151mame::start;
                        chip.stop = ym2151mame::stop;
                        chip.reset = ym2151mame::reset;
                    } else if ((chip.id == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (chip.id == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                        if (ym2151_x68sound == null) ym2151_x68sound = new mdsound.Ym2151X68Sound();
                        chip.type = MDSound.InstrumentType.YM2151x68sound;
                        chip.instrument = ym2151_x68sound;
                        chip.update = ym2151_x68sound::update;
                        chip.start = ym2151_x68sound::start;
                        chip.stop = ym2151_x68sound::stop;
                        chip.reset = ym2151_x68sound::reset;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2151Volume();
                    chip.clock = dInfo.clock;
                    YM2151ClockValue = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    if (chip.start != null)
                        lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM2151 : EnmChip.S_YM2151);

                    break;
                case 6:
                    chip = new MDSound.Chip();
                    if (ym2413 == null) {
                        ym2413 = new Ym2413();
                        chip.id = 0;
                        chipLED.PriOPLL = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPLL = 1;
                    }
                    chip.type = MDSound.InstrumentType.YM2413;
                    chip.instrument = ym2413;
                    chip.update = ym2413::update;
                    chip.start = ym2413::start;
                    chip.stop = ym2413::stop;
                    chip.reset = ym2413::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2413Volume();
                    chip.clock = dInfo.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM2413 : EnmChip.S_YM2413);

                    break;
                case 7:
                    chip = new MDSound.Chip();
                    if (ym3526 == null) {
                        ym3526 = new Ym3526();
                        chip.id = 0;
                        chipLED.PriOPL = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPL = 1;
                    }
                    chip.type = MDSound.InstrumentType.YM3526;
                    chip.instrument = ym3526;
                    chip.update = ym3526::update;
                    chip.start = ym3526::start;
                    chip.stop = ym3526::stop;
                    chip.reset = ym3526::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM3526Volume();
                    chip.clock = dInfo.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM3526 : EnmChip.S_YM3526);

                    break;
                case 8:
                    chip = new MDSound.Chip();
                    if (ym3812 == null) {
                        ym3812 = new Ym3812();
                        chip.id = 0;
                        chipLED.PriOPL2 = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPL2 = 1;
                    }
                    chip.type = MDSound.InstrumentType.YM3812;
                    chip.instrument = ym3812;
                    chip.update = ym3812::update;
                    chip.start = ym3812::start;
                    chip.stop = ym3812::stop;
                    chip.reset = ym3812::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM3812Volume();
                    chip.clock = dInfo.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YM3812 : EnmChip.S_YM3812);

                    break;
                case 9:
                    chip = new MDSound.Chip();
                    if (ymf262 == null) {
                        ymf262 = new YmF262();
                        chip.id = 0;
                        chipLED.PriOPL3 = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecOPL3 = 1;
                    }
                    chip.type = MDSound.InstrumentType.YMF262;
                    chip.instrument = ymf262;
                    chip.update = ymf262::update;
                    chip.start = ymf262::start;
                    chip.stop = ymf262::stop;
                    chip.reset = ymf262::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYMF262Volume();
                    chip.clock = dInfo.clock;
                    YMF262ClockValue = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.YMF262 : EnmChip.S_YMF262);

                    break;
                case 15:
                    chip = new MDSound.Chip();
                    if (ay8910 == null) {
                        ay8910 = new Ay8910();
                        chip.id = 0;
                        chipLED.PriAY10 = 1;
                    } else {
                        chip.id = 1;
                        chipLED.SecAY10 = 1;
                    }
                    chip.type = MDSound.InstrumentType.AY8910;
                    chip.instrument = ay8910;
                    chip.update = ay8910::update;
                    chip.start = ay8910::start;
                    chip.stop = ay8910::stop;
                    chip.reset = ay8910::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getAY8910Volume();
                    chip.clock = dInfo.clock;
                    clockAY8910 = chip.clock;
                    chip.option = null;
                    //hiyorimiDeviceFlag |= 0x2;
                    lstChips.add(chip);
                    useChip.add(chip.id == 0 ? EnmChip.AY8910 : EnmChip.S_AY8910);

                    break;
                }
            }

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            if (useChip.contains(EnmChip.YM2203) || useChip.contains(EnmChip.S_YM2203)) {
                setYM2203Volume(true, setting.getBalance().getYM2203Volume());
                setYM2203FMVolume(true, setting.getBalance().getYM2203FMVolume());
                setYM2203PSGVolume(true, setting.getBalance().getYM2203PSGVolume());
            }

            if (useChip.contains(EnmChip.YM2612) || useChip.contains(EnmChip.S_YM2612))
                setYM2612Volume(true, setting.getBalance().getYM2612Volume());

            if (useChip.contains(EnmChip.YM2608) || useChip.contains(EnmChip.S_YM2608)) {
                setYM2608Volume(true, setting.getBalance().getYM2608Volume());
                setYM2608FMVolume(true, setting.getBalance().getYM2608FMVolume());
                setYM2608PSGVolume(true, setting.getBalance().getYM2608PSGVolume());
                setYM2608RhythmVolume(true, setting.getBalance().getYM2608RhythmVolume());
                setYM2608AdpcmVolume(true, setting.getBalance().getYM2608AdpcmVolume());
            }

            if (useChip.contains(EnmChip.YM2608)) {
                chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.VirtualModel);
                chipRegister.setYM2608Register(0, 0, 0x29, 0x82, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YM2608)) {
                chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.VirtualModel);
                chipRegister.setYM2608Register(1, 0, 0x29, 0x82, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.YM2151) || useChip.contains(EnmChip.S_YM2151))
                setYM2151Volume(true, setting.getBalance().getYM2151Volume());
            if (useChip.contains(EnmChip.YM2413) || useChip.contains(EnmChip.S_YM2413))
                setYM2413Volume(true, setting.getBalance().getYM2413Volume());
            if (useChip.contains(EnmChip.YM3526) || useChip.contains(EnmChip.S_YM3526))
                setYM3526Volume(true, setting.getBalance().getYM3526Volume());
            if (useChip.contains(EnmChip.AY8910) || useChip.contains(EnmChip.S_AY8910))
                setAY8910Volume(true, setting.getBalance().getAY8910Volume());

            if (useChip.contains(EnmChip.AY8910))
                chipRegister.writeAY8910Clock((byte) 0, clockAY8910, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_AY8910))
                chipRegister.writeAY8910Clock((byte) 1, clockAY8910, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2151))
                chipRegister.writeYM2151Clock((byte) 0, YM2151ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2151))
                chipRegister.writeYM2151Clock((byte) 1, YM2151ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2203))
                chipRegister.writeYM2203Clock((byte) 0, YM2203ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2203))
                chipRegister.writeYM2203Clock((byte) 1, YM2203ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2608))
                chipRegister.writeYM2608Clock((byte) 0, YM2608ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2608))
                chipRegister.writeYM2608Clock((byte) 1, YM2608ClockValue, EnmModel.RealModel);

            if (useChip.contains(EnmChip.YMF262)) {
                chipRegister.setYMF262Register(0, 1, 5, 1, EnmModel.RealModel); // opl3mode
                chipRegister.writeYMF262Clock((byte) 0, YMF262ClockValue, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YMF262)) {
                chipRegister.setYMF262Register(1, 1, 5, 1, EnmModel.RealModel); // opl3mode
                chipRegister.writeYMF262Clock((byte) 1, YMF262ClockValue, EnmModel.RealModel);
            }

            driverVirtual.setYm2151Hosei(YM2151ClockValue);
            if (driverReal != null) driverReal.setYm2151Hosei(YM2151ClockValue);

            if (driverReal == null || ((S98) driverReal).SSGVolumeFromTAG == -1) {
                if (useChip.contains(EnmChip.YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
                if (useChip.contains(EnmChip.YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            } else {
                if (useChip.contains(EnmChip.YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 0, ((S98) driverReal).SSGVolumeFromTAG, EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 1, ((S98) driverReal).SSGVolumeFromTAG, EnmModel.RealModel);
                if (useChip.contains(EnmChip.YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 0, ((S98) driverReal).SSGVolumeFromTAG, EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 1, ((S98) driverReal).SSGVolumeFromTAG, EnmModel.RealModel);
            }
            //Play

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean midPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            chipLED.PriMID = 1;
            chipLED.SecMID = 1;

            masterVolume = setting.getBalance().getMasterVolume();

            chipRegister.initChipRegister(null);
            releaseAllMIDIout();
            makeMIDIout(setting, midiMode);
            chipRegister.setMIDIout(setting.getMidiOut().getMidiOutInfos().get(midiMode), midiOuts, midiOutsType);

            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            hiyorimiNecessary = hiyorimiNecessary;


            //Play

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            stopped = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean rcpPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            chipLED.PriMID = 1;
            chipLED.SecMID = 1;

            masterVolume = setting.getBalance().getMasterVolume();

            chipRegister.initChipRegister(null);
            releaseAllMIDIout();
            makeMIDIout(setting, midiMode);
            chipRegister.setMIDIout(setting.getMidiOut().getMidiOutInfos().get(midiMode), midiOuts, midiOutsType);

            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            hiyorimiNecessary = hiyorimiNecessary;

            //Play

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean nsfPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();


            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            chipLED.PriNES = 1;
            chipLED.PriDMC = 1;

            masterVolume = setting.getBalance().getMasterVolume();

            ((Nsf) driverVirtual).song = songNo;
            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                ((Nsf) driverReal).song = songNo;
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            if (((Nsf) driverVirtual).useFds) chipLED.PriFDS = 1;
            if (((Nsf) driverVirtual).useFme7) chipLED.PriFME7 = 1;
            if (((Nsf) driverVirtual).useMmc5) chipLED.PriMMC5 = 1;
            if (((Nsf) driverVirtual).useN106) chipLED.PriN106 = 1;
            if (((Nsf) driverVirtual).useVrc6) chipLED.PriVRC6 = 1;
            if (((Nsf) driverVirtual).useVrc7) chipLED.PriVRC7 = 1;

            //nes_intf nes = new nes_intf();
            MDSound.Chip chip;
            NesIntF nes = new NesIntF();

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.Nes;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getAPUVolume();
            chip.clock = 0;
            chip.option = null;
            lstChips.add(chip);
            ((Nsf) driverVirtual).cAPU = chip;
            useChip.add(EnmChip.NES);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.DMC;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getDMCVolume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cDMC = chip;
            useChip.add(EnmChip.DMC);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.FDS;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getFDSVolume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cFDS = chip;
            useChip.add(EnmChip.FDS);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.MMC5;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getMMC5Volume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cMMC5 = chip;
            useChip.add(EnmChip.MMC5);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.N160;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getN160Volume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cN160 = chip;
            useChip.add(EnmChip.N163);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.VRC6;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVRC6Volume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cVRC6 = chip;
            useChip.add(EnmChip.VRC6);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.VRC7;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getVRC7Volume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cVRC7 = chip;
            useChip.add(EnmChip.VRC7);

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.FME7;
            chip.instrument = nes;
            chip.update = nes::update;
            chip.start = nes::start;
            chip.stop = nes::stop;
            chip.reset = nes::reset;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 0;
            chip.option = null;
            chip.volume = setting.getBalance().getFME7Volume();
            lstChips.add(chip);
            ((Nsf) driverVirtual).cFME7 = chip;
            useChip.add(EnmChip.FME7);

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegisterNSF(lstChips.toArray(new MDSound.Chip[0]));

            //Play

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static void go() {
        stopped = false;
    }

    public static boolean hesPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            chipLED.PriHuC = 1;

            masterVolume = setting.getBalance().getMasterVolume();

            //((Hes)driverVirtual).song = (byte)SongNo;
            //((Hes)driverReal).song = (byte)SongNo;
            //if (!driverVirtual.init(vgmBuf, chipRegister, enmModel.VirtualModel, new enmUseChip[] { enmUseChip.Unuse }, 0)) return false;
            //if (!driverReal.init(vgmBuf, chipRegister, enmModel.RealModel, new enmUseChip[] { enmUseChip.Unuse }, 0)) return false;

            MDSound.Chip chip;
            mdsound.OotakePsg huc = new OotakePsg();

            chip = new MDSound.Chip();
            chip.id = 0;
            chip.type = MDSound.InstrumentType.HuC6280;
            chip.instrument = huc;
            chip.update = huc::update;
            chip.start = huc::start;
            chip.stop = huc::stop;
            chip.reset = huc::reset;
            chip.additionalUpdate = ((Hes) driverVirtual)::additionalUpdate;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.volume = setting.getBalance().getHuC6280Volume();
            chip.clock = 3579545;
            chip.option = null;
            lstChips.add(chip);
            ((Hes) driverVirtual).c6280 = chip;
            useChip.add(EnmChip.HuC6280);

            hiyorimiNecessary = hiyorimiNecessary;

            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));

            ((Hes) driverVirtual).song = (byte) songNo;
            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                ((Hes) driverReal).song = (byte) songNo;
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }
            //Play

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean sidPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            chipRegister.initChipRegister(null);

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();
            chipLED.priSID = 1;

            masterVolume = setting.getBalance().getMasterVolume();

            ((Sid) driverVirtual).song = (byte) songNo + 1;
            if (!driverVirtual.init(vgmBuf, chipRegister, EnmModel.VirtualModel, new EnmChip[] {EnmChip.Unuse}
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;
            if (driverReal != null) {
                ((Sid) driverReal).song = (byte) songNo + 1;
                if (!driverReal.init(vgmBuf, chipRegister, EnmModel.RealModel, new EnmChip[] {EnmChip.Unuse}
                        , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                        , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                    return false;
            }

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean vgmPlay(Setting setting) {

        try {

            if (vgmBuf == null || setting == null) return false;

            //Stop();

            chipRegister.resetChips();

            vgmFadeout = false;
            vgmFadeoutCounter = 1.0;
            vgmFadeoutCounterV = 0.00001;
            vgmSpeed = 1;
            vgmRealFadeoutVol = 0;
            vgmRealFadeoutVolWait = 4;

            clearFadeoutVolume();

            chipRegister.resetChips();

            useChip.clear();

            startTrdVgmReal();

            List<MDSound.Chip> lstChips = new ArrayList<>();

            MDSound.Chip chip;

            hiyorimiNecessary = setting.getHiyorimiMode();

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            if (!driverVirtual.init(vgmBuf
                    , chipRegister
                    , EnmModel.VirtualModel
                    , new EnmChip[] {EnmChip.YM2203}// usechip.toArray(new MDSound.Chip[0])
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            if (driverReal != null && !driverReal.init(vgmBuf
                    , chipRegister
                    , EnmModel.RealModel
                    , new EnmChip[] {EnmChip.YM2203}// usechip.toArray(new MDSound.Chip[0])
                    , setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000
                    , setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
                return false;

            hiyorimiNecessary = setting.getHiyorimiMode();
            int hiyorimiDeviceFlag = 0;

            chipLED = new ChipLEDs();

            masterVolume = setting.getBalance().getMasterVolume();

            if (((Vgm) driverVirtual).sn76489ClockValue != 0) {
                mdsound.Sn76489 sn76489 = null;
                mdsound.Sn76496 sn76496 = null;

                for (int i = 0; i < (((Vgm) driverVirtual).sn76489DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.option = null;

                    if ((i == 0 && setting.getSN76489Type()[0].getUseEmu()[0])
                            || (i == 1 && setting.getSN76489Type()[1].getUseEmu()[0])) {
                        if (sn76489 == null) sn76489 = new Sn76489();
                        chip.type = MDSound.InstrumentType.SN76489;
                        chip.instrument = sn76489;
                        chip.update = sn76489::update;
                        chip.start = sn76489::start;
                        chip.stop = sn76489::stop;
                        chip.reset = sn76489::reset;
                    } else if ((i == 0 && setting.getSN76489Type()[0].getUseEmu()[1])
                            || (i == 1 && setting.getSN76489Type()[1].getUseEmu()[1])) {
                        if (sn76496 == null) sn76496 = new Sn76496();
                        chip.type = MDSound.InstrumentType.SN76496;
                        chip.instrument = sn76496;
                        chip.update = sn76496::update;
                        chip.start = sn76496::start;
                        chip.stop = sn76496::stop;
                        chip.reset = sn76496::reset;
                        chip.option = ((Vgm) driverVirtual).sn76489Option;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getSN76489Volume();
                    chip.clock = ((Vgm) driverVirtual).sn76489ClockValue
                            | (((Vgm) driverVirtual).sn76489NGPFlag ? 0x80000000 : 0);
                    clockSN76489 = chip.clock & 0x7fff_ffff;
                    if (i == 0) chipLED.PriDCSG = 1;
                    else chipLED.SecDCSG = 1;

                    hiyorimiDeviceFlag |= (setting.getSN76489Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                    sn76489NGPFlag = ((Vgm) driverVirtual).sn76489NGPFlag;

                    if (chip.start != null) lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.SN76489 : EnmChip.S_SN76489);
                }
            }

            if (((Vgm) driverVirtual).ym2612ClockValue != 0) {
                mdsound.Ym2612 ym2612 = null;
                mdsound.Ym3438 ym3438 = null;
                mdsound.Ym2612Mame ym2612mame = null;

                for (int i = 0; i < (((Vgm) driverVirtual).ym2612DualChipFlag ? 2 : 1); i++) {
                    //mdsound.ym2612 ym2612 = new mdsound.ym2612();
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;
                    chip.option = null;

                    if ((i == 0 && ((setting.getYM2612Type()[0].getUseEmu()[0] ||
                                     setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) ||
                                     setting.getYM2612Type()[0].getUseReal()[0])
                            ) ||
                        (i == 1 && (setting.getYM2612Type()[1].getUseEmu()[0] ||
                                    setting.getYM2612Type()[1].getRealChipInfo()[0].getOnlyPCMEmulation()) ||
                                    setting.getYM2612Type()[1].getUseReal()[0])
                    ) {
                        if (ym2612 == null) ym2612 = new Ym2612();
                        chip.type = MDSound.InstrumentType.YM2612;
                        chip.instrument = ym2612;
                        chip.update = ym2612::update;
                        chip.start = ym2612::start;
                        chip.stop = ym2612::stop;
                        chip.reset = ym2612::reset;
                        chip.option = new Object[] {
                            (setting.getNukedOPN2().gensDACHPF ? 0x01 : 0x00) | (setting.getNukedOPN2().gensSSGEG ? 0x02 : 0x00)
                        };
                    } else if ((i == 0 && setting.getYM2612Type()[0].getUseEmu()[1]) || (i == 1 && setting.getYM2612Type()[1].getUseEmu()[1])) {
                        if (ym3438 == null) ym3438 = new Ym3438();
                        chip.type = MDSound.InstrumentType.YM3438;
                        chip.instrument = ym3438;
                        chip.update = ym3438::update;
                        chip.start = ym3438::start;
                        chip.stop = ym3438::stop;
                        chip.reset = ym3438::reset;
                        switch (setting.getNukedOPN2().emuType) {
                        case 0:
                            ym3438.setChipType(Type.discrete);
                            break;
                        case 1:
                            ym3438.setChipType(Type.asic);
                            break;
                        case 2:
                            ym3438.setChipType(Type.ym2612);
                            break;
                        case 3:
                            ym3438.setChipType(Type.ym2612_u);
                            break;
                        case 4:
                            ym3438.setChipType(Type.asic_lp);
                            break;
                        }
                    } else if ((i == 0 && setting.getYM2612Type()[0].getUseEmu()[2]) || (i == 1 && setting.getYM2612Type()[0].getUseEmu()[2])) {
                        if (ym2612mame == null) ym2612mame = new Ym2612Mame();
                        chip.type = MDSound.InstrumentType.YM2612mame;
                        chip.instrument = ym2612mame;
                        chip.update = ym2612mame::update;
                        chip.start = ym2612mame::start;
                        chip.stop = ym2612mame::stop;
                        chip.reset = ym2612mame::reset;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2612Volume();
                    chip.clock = ((Vgm) driverVirtual).ym2612ClockValue;
                    clockYM2612 = ((Vgm) driverVirtual).ym2612ClockValue;

                    hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]) ? 0x1 : 0x2;
                    hiyorimiDeviceFlag |= (setting.getYM2612Type()[0].getUseReal()[0]
                            && setting.getYM2612Type()[0].getRealChipInfo()[0].getOnlyPCMEmulation()) ? 0x2 : 0x0;

                    if (i == 0) chipLED.PriOPN2 = 1;
                    else chipLED.SecOPN2 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2612 : EnmChip.S_YM2612);
                }
            }

            if (((Vgm) driverVirtual).rf5C68ClockValue != 0) {
                mdsound.Rf5c68 rf5c68 = new mdsound.Rf5c68();

                for (int i = 0; i < (((Vgm) driverVirtual).rf5C68DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.RF5C68;
                    chip.id = (byte) i;
                    chip.instrument = rf5c68;
                    chip.update = rf5c68::update;
                    chip.start = rf5c68::start;
                    chip.stop = rf5c68::stop;
                    chip.reset = rf5c68::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getRF5C68Volume();
                    chip.clock = ((Vgm) driverVirtual).rf5C68ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriRF5C68 = 1;
                    else chipLED.SecRF5C68 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.RF5C68 : EnmChip.S_RF5C68);
                }
            }

            if (((Vgm) driverVirtual).rf5C164ClockValue != 0) {
                mdsound.ScdPcm rf5c164 = new mdsound.ScdPcm();

                for (int i = 0; i < (((Vgm) driverVirtual).rf5C164DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.RF5C164;
                    chip.id = (byte) i;
                    chip.instrument = rf5c164;
                    chip.update = rf5c164::update;
                    chip.start = rf5c164::start;
                    chip.stop = rf5c164::stop;
                    chip.reset = rf5c164::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getRF5C164Volume();
                    chip.clock = ((Vgm) driverVirtual).rf5C164ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriRF5C = 1;
                    else chipLED.SecRF5C = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.RF5C164 : EnmChip.S_RF5C164);
                }
            }

            if (((Vgm) driverVirtual).pwmClockValue != 0) {
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.PWM;
                chip.id = 0;
                mdsound.Pwm pwm = new mdsound.Pwm();
                chip.instrument = pwm;
                chip.update = pwm::update;
                chip.start = pwm::start;
                chip.stop = pwm::stop;
                chip.reset = pwm::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getPWMVolume();
                chip.clock = ((Vgm) driverVirtual).pwmClockValue;
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                chipLED.PriPWM = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.PWM);
            }

            if (((Vgm) driverVirtual).c140ClockValue != 0) {
                mdsound.C140 c140 = new mdsound.C140();
                for (int i = 0; i < (((Vgm) driverVirtual).c140DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.C140;
                    chip.id = (byte) i;
                    chip.instrument = c140;
                    chip.update = c140::update;
                    chip.start = c140::start;
                    chip.stop = c140::stop;
                    chip.reset = c140::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getC140Volume();
                    chip.clock = ((Vgm) driverVirtual).c140ClockValue;
                    chip.option = new Object[] {((Vgm) driverVirtual).C140Type};

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriC140 = 1;
                    else chipLED.SecC140 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.C140 : EnmChip.S_C140);
                }
            }

            if (((Vgm) driverVirtual).multiPCMClockValue != 0) {
                mdsound.MultiPcm multipcm = new mdsound.MultiPcm();
                for (int i = 0; i < (((Vgm) driverVirtual).multiPCMDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.MultiPCM;
                    chip.id = (byte) i;
                    chip.instrument = multipcm;
                    chip.update = multipcm::update;
                    chip.start = multipcm::start;
                    chip.stop = multipcm::stop;
                    chip.reset = multipcm::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getMultiPCMVolume();
                    chip.clock = ((Vgm) driverVirtual).multiPCMClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriMPCM = 1;
                    else chipLED.SecMPCM = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.MultiPCM : EnmChip.S_MultiPCM);
                }
            }

            if (((Vgm) driverVirtual).okiM6258ClockValue != 0) {
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.OKIM6258;
                chip.id = 0;
                mdsound.OkiM6258 okim6258 = new mdsound.OkiM6258();
                chip.instrument = okim6258;
                chip.update = okim6258::update;
                chip.start = okim6258::start;
                chip.stop = okim6258::stop;
                chip.reset = okim6258::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getOKIM6258Volume();
                chip.clock = ((Vgm) driverVirtual).okiM6258ClockValue;
                chip.option = new Object[] {(int) ((Vgm) driverVirtual).okiM6258Type};
                //chip.option = new Object[1] { 6 };
                okim6258.okim6258_set_srchg_cb((byte) 0, Audio::changeChipSampleRate, chip);

                hiyorimiDeviceFlag |= 0x2;

                chipLED.PriOKI5 = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.OKIM6258);
            }

            if (((Vgm) driverVirtual).okiM6295ClockValue != 0) {
                mdsound.OkiM6295 okim6295 = new mdsound.OkiM6295();
                for (byte i = 0; i < (((Vgm) driverVirtual).okiM6295DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.OKIM6295;
                    chip.id = i;
                    chip.instrument = okim6295;
                    chip.update = okim6295::update;
                    chip.start = okim6295::start;
                    chip.stop = okim6295::stop;
                    chip.reset = okim6295::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getOKIM6295Volume();
                    chip.clock = ((Vgm) driverVirtual).okiM6295ClockValue;
                    chip.option = null;
                    okim6295.okim6295_set_srchg_cb(i, Audio::changeChipSampleRate, chip);

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOKI9 = 1;
                    else chipLED.SecOKI9 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.OKIM6295 : EnmChip.S_OKIM6295);
                }
            }

            if (((Vgm) driverVirtual).segaPCMClockValue != 0) {
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.SEGAPCM;
                chip.id = 0;
                mdsound.SegaPcm segapcm = new mdsound.SegaPcm();
                chip.instrument = segapcm;
                chip.update = segapcm::update;
                chip.start = segapcm::start;
                chip.stop = segapcm::stop;
                chip.reset = segapcm::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getSEGAPCMVolume();
                chip.clock = ((Vgm) driverVirtual).segaPCMClockValue;
                chip.option = new Object[] {((Vgm) driverVirtual).segaPCMInterface};

                hiyorimiDeviceFlag |= 0x2;

                chipLED.PriSPCM = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.SEGAPCM);
            }

            if (((Vgm) driverVirtual).yn2608ClockValue != 0) {
                mdsound.Ym2608 ym2608 = new mdsound.Ym2608();
                for (int i = 0; i < (((Vgm) driverVirtual).ym2608DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YM2608;
                    chip.id = (byte) i;
                    chip.instrument = ym2608;
                    chip.update = ym2608::update;
                    chip.start = ym2608::start;
                    chip.stop = ym2608::stop;
                    chip.reset = ym2608::reset;
                    chip.samplingRate = 55467;// (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2608Volume();
                    chip.clock = ((Vgm) driverVirtual).yn2608ClockValue;
                    Function<String, Stream> fn = Common::getOPNARyhthmStream;
                    chip.option = new Object[] {fn};
                    hiyorimiDeviceFlag |= 0x2;
                    clockYM2608 = ((Vgm) driverVirtual).yn2608ClockValue;

                    if (i == 0) chipLED.PriOPNA = 1;
                    else chipLED.SecOPNA = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2608 : EnmChip.S_YM2608);
                }
            }

            if (((Vgm) driverVirtual).yn2151ClockValue != 0) {
                mdsound.Ym2151 ym2151 = null;
                mdsound.Ym2151Mame ym2151_mame = null;
                mdsound.Ym2151X68Sound ym2151_x68sound = null;
                for (int i = 0; i < (((Vgm) driverVirtual).ym2151DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.id = (byte) i;

                    if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[0]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[0])) {
                        if (ym2151 == null) ym2151 = new mdsound.Ym2151();
                        chip.type = MDSound.InstrumentType.YM2151;
                        chip.instrument = ym2151;
                        chip.update = ym2151::update;
                        chip.start = ym2151::start;
                        chip.stop = ym2151::stop;
                        chip.reset = ym2151::reset;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[1]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[1])) {
                        if (ym2151_mame == null) ym2151_mame = new mdsound.Ym2151Mame();
                        chip.type = MDSound.InstrumentType.YM2151mame;
                        chip.instrument = ym2151_mame;
                        chip.update = ym2151_mame::update;
                        chip.start = ym2151_mame::start;
                        chip.stop = ym2151_mame::stop;
                        chip.reset = ym2151_mame::reset;
                    } else if ((i == 0 && setting.getYM2151Type()[0].getUseEmu()[2]) || (i == 1 && setting.getYM2151Type()[1].getUseEmu()[2])) {
                        if (ym2151_x68sound == null) ym2151_x68sound = new mdsound.Ym2151X68Sound();
                        chip.type = MDSound.InstrumentType.YM2151x68sound;
                        chip.instrument = ym2151_x68sound;
                        chip.update = ym2151_x68sound::update;
                        chip.start = ym2151_x68sound::start;
                        chip.stop = ym2151_x68sound::stop;
                        chip.reset = ym2151_x68sound::reset;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2151Volume()
                    ;
                    chip.clock = ((Vgm) driverVirtual).yn2151ClockValue;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPM = 1;
                    else chipLED.SecOPM = 1;

                    if (chip.start != null)
                        lstChips.add(chip);

                    useChip.add(i == 0 ? EnmChip.YM2151 : EnmChip.S_YM2151);
                }
            }

            if (((Vgm) driverVirtual).ym2203ClockValue != 0) {
                mdsound.Ym2203 ym2203 = new mdsound.Ym2203();
                for (int i = 0; i < (((Vgm) driverVirtual).ym2203DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YM2203;
                    chip.id = (byte) i;
                    chip.instrument = ym2203;
                    chip.update = ym2203::update;
                    chip.start = ym2203::start;
                    chip.stop = ym2203::stop;
                    chip.reset = ym2203::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2203Volume();
                    chip.clock = ((Vgm) driverVirtual).ym2203ClockValue;
                    chip.option = null;

                    clockYM2203 = ((Vgm) driverVirtual).ym2203ClockValue;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPN = 1;
                    else chipLED.SecOPN = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2203 : EnmChip.S_YM2203);
                }
            }

            if (((Vgm) driverVirtual).ym2610ClockValue != 0) {
                mdsound.Ym2610 ym2610 = new mdsound.Ym2610();
                for (int i = 0; i < (((Vgm) driverVirtual).ym2610DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YM2610;
                    chip.id = (byte) i;
                    chip.instrument = ym2610;
                    chip.update = ym2610::update;
                    chip.start = ym2610::start;
                    chip.stop = ym2610::stop;
                    chip.reset = ym2610::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2610Volume();
                    chip.clock = ((Vgm) driverVirtual).ym2610ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPNB = 1;
                    else chipLED.SecOPNB = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2610 : EnmChip.S_YM2610);
                }
            }

            if (((Vgm) driverVirtual).ym3812ClockValue != 0) {
                mdsound.Ym3812 ym3812 = new mdsound.Ym3812();
                for (int i = 0; i < (((Vgm) driverVirtual).ym3812DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YM3812;
                    chip.id = (byte) i;
                    chip.instrument = ym3812;
                    chip.update = ym3812::update;
                    chip.start = ym3812::start;
                    chip.stop = ym3812::stop;
                    chip.reset = ym3812::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM3812Volume()
                    ;
                    chip.clock = ((Vgm) driverVirtual).ym3812ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPL2 = 1;
                    else chipLED.SecOPL2 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM3812 : EnmChip.S_YM3812);
                }
            }

            if (((Vgm) driverVirtual).ymF262ClockValue != 0) {
                YmF262 ymf262 = new YmF262();
                for (int i = 0; i < (((Vgm) driverVirtual).ymF262DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YMF262;
                    chip.id = (byte) i;
                    chip.instrument = ymf262;
                    chip.update = ymf262::update;
                    chip.start = ymf262::start;
                    chip.stop = ymf262::stop;
                    chip.reset = ymf262::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYMF262Volume();
                    chip.clock = ((Vgm) driverVirtual).ymF262ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPL3 = 1;
                    else chipLED.SecOPL3 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMF262 : EnmChip.S_YMF262);
                }
            }

            if (((Vgm) driverVirtual).ymF271ClockValue != 0) {
                mdsound.Ymf271 ymf271 = new mdsound.Ymf271();
                for (int i = 0; i < (((Vgm) driverVirtual).ymF271DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YMF271;
                    chip.id = (byte) i;
                    chip.instrument = ymf271;
                    chip.update = ymf271::update;
                    chip.start = ymf271::start;
                    chip.stop = ymf271::stop;
                    chip.reset = ymf271::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYMF271Volume();
                    chip.clock = ((Vgm) driverVirtual).ymF271ClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPX = 1;
                    else chipLED.SecOPX = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMF271 : EnmChip.S_YMF271);
                }
            }

            if (((Vgm) driverVirtual).ymF278BClockValue != 0) {
                YmF278b ymf278b = new YmF278b();
                for (int i = 0; i < (((Vgm) driverVirtual).ymF278BDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YMF278B;
                    chip.id = (byte) i;
                    chip.instrument = ymf278b;
                    chip.update = ymf278b::update;
                    chip.start = ymf278b::start;
                    chip.stop = ymf278b::stop;
                    chip.reset = ymf278b::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYMF278BVolume();
                    chip.clock = ((Vgm) driverVirtual).ymF278BClockValue & 0x7fffffff;
                    chip.option = new Object[] {Common.getApplicationFolder()};

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPL4 = 1;
                    else chipLED.SecOPL4 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMF278B : EnmChip.S_YMF278B);
                }
            }

            if (((Vgm) driverVirtual).ymZ280BClockValue != 0) {
                YmZ280b ymz280b = new YmZ280b();
                for (int i = 0; i < (((Vgm) driverVirtual).ymZ280BDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YMZ280B;
                    chip.id = (byte) i;
                    chip.instrument = ymz280b;
                    chip.update = ymz280b::update;
                    chip.start = ymz280b::start;
                    chip.stop = ymz280b::stop;
                    chip.reset = ymz280b::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYMZ280BVolume();
                    chip.clock = ((Vgm) driverVirtual).ymZ280BClockValue & 0x7fffffff;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriYMZ = 1;
                    else chipLED.SecYMZ = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YMZ280B : EnmChip.S_YMZ280B);
                }
            }

            if (((Vgm) driverVirtual).ay8910ClockValue != 0) {
                mdsound.Ay8910 ay8910 = null;
                mdsound.Ay8910Mame ay8910mame = null;

                for (int i = 0; i < (((Vgm) driverVirtual).ay8910DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.AY8910;
                    chip.id = (byte) i;

                    if ((i == 0 && setting.getAY8910Type()[0].getUseEmu()[0])
                            || (i == 1 && setting.getAY8910Type()[1].getUseEmu()[0])) {
                        if (ay8910 == null) ay8910 = new Ay8910();
                        chip.type = MDSound.InstrumentType.AY8910;
                        chip.instrument = ay8910;
                        chip.update = ay8910::update;
                        chip.start = ay8910::start;
                        chip.stop = ay8910::stop;
                        chip.reset = ay8910::reset;
                    } else if ((i == 0 && setting.getAY8910Type()[0].getUseEmu()[1])
                            || (i == 1 && setting.getAY8910Type()[1].getUseEmu()[1])) {
                        if (ay8910mame == null) ay8910mame = new Ay8910Mame();
                        chip.type = MDSound.InstrumentType.AY8910mame;
                        chip.instrument = ay8910mame;
                        chip.update = ay8910mame::update;
                        chip.start = ay8910mame::start;
                        chip.stop = ay8910mame::stop;
                        chip.reset = ay8910mame::reset;
                    }

                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getAY8910Volume();
                    chip.clock = (((Vgm) driverVirtual).ay8910ClockValue & 0x7fffffff) / 2;
                    clockAY8910 = chip.clock;
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriAY10 = 1;
                    else chipLED.SecAY10 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.AY8910 : EnmChip.S_AY8910);
                }
            }

            if (((Vgm) driverVirtual).ym2413ClockValue != 0) {
                Instrument opll;
                if (!((Vgm) driverVirtual).ym2413VRC7Flag) {
                    opll = new mdsound.Ym2413();
                } else {
                    opll = new VRC7();
                }

                for (int i = 0; i < (((Vgm) driverVirtual).ym2413DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YM2413;
                    chip.id = (byte) i;
                    chip.instrument = opll;
                    chip.update = opll::update;
                    chip.start = opll::start;
                    chip.stop = opll::stop;
                    chip.reset = opll::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM2413Volume();
                    chip.clock = (((Vgm) driverVirtual).ym2413ClockValue & 0x7fffffff);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriOPLL = 1;
                    else chipLED.SecOPLL = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM2413 : EnmChip.S_YM2413);
                }
            }

            if (((Vgm) driverVirtual).huC6280ClockValue != 0) {
                mdsound.OotakePsg huc6280 = new mdsound.OotakePsg();
                for (int i = 0; i < (((Vgm) driverVirtual).huC6280DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.HuC6280;
                    chip.id = (byte) i;
                    chip.instrument = huc6280;
                    chip.update = huc6280::update;
                    chip.start = huc6280::start;
                    chip.stop = huc6280::stop;
                    chip.reset = huc6280::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getHuC6280Volume();
                    chip.clock = (((Vgm) driverVirtual).huC6280ClockValue & 0x7fffffff);
                    chip.option = null;

                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriHuC = 1;
                    else chipLED.SecHuC = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.HuC6280 : EnmChip.S_HuC6280);
                }
            }

            if (((Vgm) driverVirtual).qSoundClockValue != 0) {
                mdsound.QSoundCtr qsound = new mdsound.QSoundCtr();
                chip = new MDSound.Chip();
                chip.type = MDSound.InstrumentType.QSoundCtr;
                chip.id = (byte) 0;
                chip.instrument = qsound;
                chip.update = qsound::update;
                chip.start = qsound::start;
                chip.stop = qsound::stop;
                chip.reset = qsound::reset;
                chip.samplingRate = setting.getOutputDevice().getSampleRate();
                chip.volume = setting.getBalance().getQSoundVolume();
                chip.clock = (((Vgm) driverVirtual).qSoundClockValue);// & 0x7fffffff);
                chip.option = null;

                hiyorimiDeviceFlag |= 0x2;

                //if (i == 0) chipLED.PriHuC = 1;
                //else chipLED.SecHuC = 1;
                chipLED.PriQsnd = 1;

                lstChips.add(chip);
                useChip.add(EnmChip.QSound);
            }

            if (((Vgm) driverVirtual).saa1099ClockValue != 0) {
                mdsound.Saa1099 saa1099 = new Saa1099();
                for (int i = 0; i < (((Vgm) driverVirtual).saA1099DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.SAA1099;
                    chip.id = (byte) i;
                    chip.instrument = saa1099;
                    chip.update = saa1099::update;
                    chip.start = saa1099::start;
                    chip.stop = saa1099::stop;
                    chip.reset = saa1099::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getSAA1099Volume();
                    chip.clock = (((Vgm) driverVirtual).saa1099ClockValue & 0x3fffffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriSAA = 1;
                    else chipLED.SecSAA = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.SAA1099 : EnmChip.S_SAA1099);
                }
            }

            if (((Vgm) driverVirtual).wSwanClockValue != 0) {
                mdsound.WsAudio WSwan = new WsAudio();
                for (int i = 0; i < (((Vgm) driverVirtual).wSwanDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.WSwan;
                    chip.id = (byte) i;
                    chip.instrument = WSwan;
                    chip.update = WSwan::update;
                    chip.start = WSwan::start;
                    chip.stop = WSwan::stop;
                    chip.reset = WSwan::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getWSwanVolume();
                    chip.clock = (((Vgm) driverVirtual).wSwanClockValue & 0x3fffffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriWSW = 1;
                    else chipLED.SecWSW = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.WSwan : EnmChip.S_WSwan);
                }
            }

            if (((Vgm) driverVirtual).pokeyClockValue != 0) {
                mdsound.Pokey pokey = new Pokey();
                for (int i = 0; i < (((Vgm) driverVirtual).pokeyDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.POKEY;
                    chip.id = (byte) i;
                    chip.instrument = pokey;
                    chip.update = pokey::update;
                    chip.start = pokey::start;
                    chip.stop = pokey::stop;
                    chip.reset = pokey::reset;
                    chip.samplingRate = (((Vgm) driverVirtual).pokeyClockValue & 0x3fffffff);// (int)setting.getoutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getPOKEYVolume();
                    chip.clock = (((Vgm) driverVirtual).pokeyClockValue & 0x3fffffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriPOK = 1;
                    else chipLED.SecPOK = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.POKEY : EnmChip.S_POKEY);
                }
            }

            if (((Vgm) driverVirtual).x1_010ClockValue != 0) {
                mdsound.X1_010 X1_010 = new X1_010();
                for (int i = 0; i < (((Vgm) driverVirtual).x1_010DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.X1_010;
                    chip.id = (byte) i;
                    chip.instrument = X1_010;
                    chip.update = X1_010::update;
                    chip.start = X1_010::start;
                    chip.stop = X1_010::stop;
                    chip.reset = X1_010::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getX1_010Volume();
                    chip.clock = (((Vgm) driverVirtual).x1_010ClockValue & 0x3fffffff);
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriX1010 = 1;
                    else chipLED.SecX1010 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.X1_010 : EnmChip.S_X1_010);
                }
            }

            if (((Vgm) driverVirtual).c352ClockValue != 0) {
                mdsound.C352 c352 = new C352();
                for (int i = 0; i < (((Vgm) driverVirtual).c352DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.C352;
                    chip.id = (byte) i;
                    chip.instrument = c352;
                    chip.update = c352::update;
                    chip.start = c352::start;
                    chip.stop = c352::stop;
                    chip.reset = c352::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getC352Volume();
                    chip.clock = (((Vgm) driverVirtual).c352ClockValue & 0x7fffffff);
                    chip.option = new Object[] {(((Vgm) driverVirtual).c352ClockDivider)};
                    int divider = (((Vgm) driverVirtual).c352ClockDivider) != 0 ? (((Vgm) driverVirtual).c352ClockDivider) : 288;
                    clockC352 = chip.clock / divider;
                    c352.c352_set_options((byte) (((Vgm) driverVirtual).c352ClockValue >> 31));
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriC352 = 1;
                    else chipLED.SecC352 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.C352 : EnmChip.S_C352);
                }
            }

            if (((Vgm) driverVirtual).ga20ClockValue != 0) {
                mdsound.Iremga20 ga20 = new Iremga20();
                for (int i = 0; i < (((Vgm) driverVirtual).ga20DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.GA20;
                    chip.id = (byte) i;
                    chip.instrument = ga20;
                    chip.update = ga20::update;
                    chip.start = ga20::start;
                    chip.stop = ga20::stop;
                    chip.reset = ga20::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getGA20Volume();
                    chip.clock = (((Vgm) driverVirtual).ga20ClockValue & 0x7fffffff);
                    chip.option = null;
                    hiyorimiDeviceFlag |= 0x2;

                    if (i == 0) chipLED.PriGA20 = 1;
                    else chipLED.SecGA20 = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.GA20 : EnmChip.S_GA20);
                }
            }

            if (((Vgm) driverVirtual).k053260ClockValue != 0) {
                mdsound.K053260 k053260 = new mdsound.K053260();

                for (int i = 0; i < (((Vgm) driverVirtual).k053260DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.K053260;
                    chip.id = (byte) i;
                    chip.instrument = k053260;
                    chip.update = k053260::update;
                    chip.start = k053260::start;
                    chip.stop = k053260::stop;
                    chip.reset = k053260::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getK053260Volume();
                    chip.clock = ((Vgm) driverVirtual).k053260ClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriK053260 = 1;
                    else chipLED.SecK053260 = 1;

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.K053260 : EnmChip.S_K053260);
                }
            }

            if (((Vgm) driverVirtual).k054539ClockValue != 0) {
                mdsound.K054539 k054539 = new mdsound.K054539();

                for (int i = 0; i < (((Vgm) driverVirtual).k054539DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.K054539;
                    chip.id = (byte) i;
                    chip.instrument = k054539;
                    chip.update = k054539::update;
                    chip.start = k054539::start;
                    chip.stop = k054539::stop;
                    chip.reset = k054539::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getK054539Volume();
                    chip.clock = ((Vgm) driverVirtual).k054539ClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriK054539 = 1;
                    else chipLED.SecK054539 = 1;

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.K054539 : EnmChip.S_K054539);
                }
            }

            if (((Vgm) driverVirtual).k051649ClockValue != 0) {
                mdsound.K051649 k051649 = new mdsound.K051649();

                for (int i = 0; i < (((Vgm) driverVirtual).k051649DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.K051649;
                    chip.id = (byte) i;
                    chip.instrument = k051649;
                    chip.update = k051649::update;
                    chip.start = k051649::start;
                    chip.stop = k051649::stop;
                    chip.reset = k051649::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getK051649Volume();
                    chip.clock = ((Vgm) driverVirtual).k051649ClockValue;
                    clockK051649 = chip.clock;
                    chip.option = null;
                    if (i == 0) chipLED.PriK051649 = 1;
                    else chipLED.SecK051649 = 1;

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.K051649 : EnmChip.S_K051649);
                }
            }

            if (((Vgm) driverVirtual).ym3526ClockValue != 0) {
                mdsound.Ym3526 ym3526 = new mdsound.Ym3526();

                for (int i = 0; i < (((Vgm) driverVirtual).ym3526DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.YM3526;
                    chip.id = (byte) i;
                    chip.instrument = ym3526;
                    chip.update = ym3526::update;
                    chip.start = ym3526::start;
                    chip.stop = ym3526::stop;
                    chip.reset = ym3526::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getYM3526Volume();
                    chip.clock = ((Vgm) driverVirtual).ym3526ClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriOPL = 1;
                    else chipLED.SecOPL = 1;

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.YM3526 : EnmChip.S_YM3526);
                }
            }

            if (((Vgm) driverVirtual).y8950ClockValue != 0) {
                mdsound.Y8950 y8950 = new mdsound.Y8950();

                for (int i = 0; i < (((Vgm) driverVirtual).y8950DualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.Y8950;
                    chip.id = (byte) i;
                    chip.instrument = y8950;
                    chip.update = y8950::update;
                    chip.start = y8950::start;
                    chip.stop = y8950::stop;
                    chip.reset = y8950::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getY8950Volume();
                    chip.clock = ((Vgm) driverVirtual).y8950ClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriY8950 = 1;
                    else chipLED.SecY8950 = 1;

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.Y8950 : EnmChip.S_Y8950);
                }
            }

            if (((Vgm) driverVirtual).dmgClockValue != 0) {
                mdsound.Gb dmg = new mdsound.Gb();

                for (int i = 0; i < (((Vgm) driverVirtual).dmgDualChipFlag ? 2 : 1); i++) {
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.DMG;
                    chip.id = (byte) i;
                    chip.instrument = dmg;
                    chip.update = dmg::update;
                    chip.start = dmg::start;
                    chip.stop = dmg::stop;
                    chip.reset = dmg::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getDMGVolume();
                    chip.clock = ((Vgm) driverVirtual).dmgClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriDMG = 1;
                    else chipLED.SecDMG = 1;

                    hiyorimiDeviceFlag |= 0x2;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.DMG : EnmChip.S_DMG);
                }
            }

            if (((Vgm) driverVirtual).nesClockValue != 0) {

                for (int i = 0; i < (((Vgm) driverVirtual).nesDualChipFlag ? 2 : 1); i++) {
                    mdsound.NesIntF nes = new mdsound.NesIntF();
                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.Nes;
                    chip.id = (byte) i;
                    chip.instrument = nes;
                    chip.update = nes::update;
                    chip.start = nes::start;
                    chip.stop = nes::stop;
                    chip.reset = nes::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getAPUVolume();
                    chip.clock = ((Vgm) driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriNES = 1;
                    else chipLED.SecNES = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.NES : EnmChip.S_NES);

                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.DMC;
                    chip.id = (byte) i;
                    chip.instrument = nes;
                    //chip.update = nes::update;
                    chip.start = nes::start;
                    chip.stop = nes::stop;
                    chip.reset = nes::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getDMCVolume();
                    chip.clock = ((Vgm) driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriDMC = 1;
                    else chipLED.SecDMC = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.DMC : EnmChip.S_DMC);


                    chip = new MDSound.Chip();
                    chip.type = MDSound.InstrumentType.FDS;
                    chip.id = (byte) i;
                    chip.instrument = nes;
                    //chip.update = nes::update;
                    chip.start = nes::start;
                    chip.stop = nes::stop;
                    chip.reset = nes::reset;
                    chip.samplingRate = setting.getOutputDevice().getSampleRate();
                    chip.volume = setting.getBalance().getFDSVolume();
                    chip.clock = ((Vgm) driverVirtual).nesClockValue;
                    chip.option = null;
                    if (i == 0) chipLED.PriFDS = 1;
                    else chipLED.SecFDS = 1;

                    lstChips.add(chip);
                    useChip.add(i == 0 ? EnmChip.FDS : EnmChip.S_FDS);


                    hiyorimiDeviceFlag |= 0x2;

                }
            }


            hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && hiyorimiNecessary;


            if (mds == null)
                mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));
            else
                mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, lstChips.toArray(new MDSound.Chip[0]));

            chipRegister.initChipRegister(lstChips.toArray(new MDSound.Chip[0]));


            if (useChip.contains(EnmChip.YM2203) || useChip.contains(EnmChip.S_YM2203)) {
                chipRegister.setYM2203Register(0, 0x7, 0x3f, EnmModel.RealModel); // 出力オフ
                chipRegister.setYM2203Register(1, 0x7, 0x3f, EnmModel.RealModel);
                chipRegister.setYM2203Register(0, 0x8, 0x0, EnmModel.RealModel);
                chipRegister.setYM2203Register(1, 0x8, 0x0, EnmModel.RealModel);
                chipRegister.setYM2203Register(0, 0x9, 0x0, EnmModel.RealModel);
                chipRegister.setYM2203Register(1, 0x9, 0x0, EnmModel.RealModel);
                chipRegister.setYM2203Register(0, 0xa, 0x0, EnmModel.RealModel);
                chipRegister.setYM2203Register(1, 0xa, 0x0, EnmModel.RealModel);
                setYM2203FMVolume(true, setting.getBalance().getYM2203FMVolume());
                setYM2203PSGVolume(true, setting.getBalance().getYM2203PSGVolume());
            }

            if (useChip.contains(EnmChip.YM2608) || useChip.contains(EnmChip.S_YM2608)) {
                setYM2608FMVolume(true, setting.getBalance().getYM2608FMVolume());
                setYM2608PSGVolume(true, setting.getBalance().getYM2608PSGVolume());
                setYM2608RhythmVolume(true, setting.getBalance().getYM2608RhythmVolume());
                setYM2608AdpcmVolume(true, setting.getBalance().getYM2608AdpcmVolume());
            }

            if (useChip.contains(EnmChip.YM2610) || useChip.contains(EnmChip.S_YM2610)) {

                setYM2610FMVolume(true, setting.getBalance().getYM2610FMVolume());
                setYM2610PSGVolume(true, setting.getBalance().getYM2610PSGVolume());
                setYM2610AdpcmAVolume(true, setting.getBalance().getYM2610AdpcmAVolume());
                setYM2610AdpcmBVolume(true, setting.getBalance().getYM2610AdpcmBVolume());
            }

            if (useChip.contains(EnmChip.AY8910))
                chipRegister.writeAY8910Clock((byte) 0, ((Vgm) driverVirtual).ay8910ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_AY8910))
                chipRegister.writeAY8910Clock((byte) 1, ((Vgm) driverVirtual).ay8910ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2151))
                chipRegister.writeYM2151Clock((byte) 0, ((Vgm) driverVirtual).yn2151ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2151))
                chipRegister.writeYM2151Clock((byte) 1, ((Vgm) driverVirtual).yn2151ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2203))
                chipRegister.writeYM2203Clock((byte) 0, ((Vgm) driverVirtual).ym2203ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2203))
                chipRegister.writeYM2203Clock((byte) 1, ((Vgm) driverVirtual).ym2203ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM2608))
                chipRegister.writeYM2608Clock((byte) 0, ((Vgm) driverVirtual).yn2608ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.S_YM2608))
                chipRegister.writeYM2608Clock((byte) 1, ((Vgm) driverVirtual).yn2608ClockValue, EnmModel.RealModel);
            if (useChip.contains(EnmChip.YM3526)) {
                chipRegister.setYM3526Register(0, 0xbd, 0, EnmModel.RealModel); // リズムモードオフ
                chipRegister.writeYM3526Clock((byte) 0, ((Vgm) driverVirtual).ym3526ClockValue, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YM3526)) {
                chipRegister.setYM3526Register(1, 0xbd, 0, EnmModel.RealModel); // リズムモードオフ
                chipRegister.writeYM3526Clock((byte) 1, ((Vgm) driverVirtual).ym3526ClockValue, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.YM3812)) {
                chipRegister.setYM3812Register(0, 0xbd, 0, EnmModel.RealModel); // リズムモードオフ
                chipRegister.writeYM3812Clock((byte) 0, ((Vgm) driverVirtual).ym3812ClockValue, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YM3812)) {
                chipRegister.setYM3812Register(1, 0xbd, 0, EnmModel.RealModel); // リズムモードオフ
                chipRegister.writeYM3812Clock((byte) 1, ((Vgm) driverVirtual).ym3812ClockValue, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.YMF262)) {
                chipRegister.setYMF262Register(0, 0, 0xbd, 0, EnmModel.RealModel); // リズムモードオフ
                chipRegister.setYMF262Register(0, 1, 5, 1, EnmModel.RealModel); // opl3mode
                chipRegister.writeYMF262Clock((byte) 0, ((Vgm) driverVirtual).ymF262ClockValue, EnmModel.RealModel);
            }
            if (useChip.contains(EnmChip.S_YMF262)) {
                chipRegister.setYMF262Register(1, 0, 0xbd, 0, EnmModel.RealModel); // リズムモードオフ
                chipRegister.setYMF262Register(1, 1, 5, 1, EnmModel.RealModel); // opl3mode
                chipRegister.writeYMF262Clock((byte) 1, ((Vgm) driverVirtual).ymF262ClockValue, EnmModel.RealModel);
            }
            if (sn76489NGPFlag) {
                chipRegister.setSN76489Register(0, 0xe5, EnmModel.RealModel); // white noise mode
                chipRegister.setSN76489Register(1, 0xe5, EnmModel.RealModel); // white noise mode
                chipRegister.setSN76489Register(0, 0xe5, EnmModel.VirtualModel); // white noise mode
                chipRegister.setSN76489Register(1, 0xe5, EnmModel.VirtualModel); // white noise mode
            }
            if (useChip.contains(EnmChip.YM2610)) {
                 // control2 レジスタのパンをセンターに予め設定
                chipRegister.setYM2610Register(0, 0, 0x11, 0xc0, EnmModel.RealModel);
                chipRegister.setYM2610Register(0, 0, 0x11, 0xc0, EnmModel.VirtualModel);
            }
            if (useChip.contains(EnmChip.S_YM2610)) {
                 // control2 レジスタのパンをセンターに予め設定
                chipRegister.setYM2610Register(1, 0, 0x11, 0xc0, EnmModel.RealModel);
                chipRegister.setYM2610Register(1, 0, 0x11, 0xc0, EnmModel.VirtualModel);
            }
            if (useChip.contains(EnmChip.C140))
                chipRegister.writeC140Type((byte) 0, ((Vgm) driverVirtual).C140Type, EnmModel.RealModel);
            if (useChip.contains(EnmChip.SEGAPCM))
                chipRegister.writeSEGAPCMClock((byte) 0, ((Vgm) driverVirtual).segaPCMClockValue, EnmModel.RealModel);

            int SSGVolumeFromTAG = -1;
            if (driverReal != null) {
                if (driverReal.gd3.systemNameJ.indexOf("9801") > 0) SSGVolumeFromTAG = 31;
                if (driverReal.gd3.systemNameJ.indexOf("8801") > 0) SSGVolumeFromTAG = 63;
                if (driverReal.gd3.systemNameJ.indexOf("pc-88") > 0) SSGVolumeFromTAG = 63;
                if (driverReal.gd3.systemNameJ.indexOf("PC88") > 0) SSGVolumeFromTAG = 63;
                if (driverReal.gd3.systemNameJ.indexOf("pc-98") > 0) SSGVolumeFromTAG = 31;
                if (driverReal.gd3.systemNameJ.indexOf("PC98") > 0) SSGVolumeFromTAG = 31;
                if (driverReal.gd3.systemName.indexOf("9801") > 0) SSGVolumeFromTAG = 31;
                if (driverReal.gd3.systemName.indexOf("8801") > 0) SSGVolumeFromTAG = 63;
                if (driverReal.gd3.systemName.indexOf("pc-88") > 0) SSGVolumeFromTAG = 63;
                if (driverReal.gd3.systemName.indexOf("PC88") > 0) SSGVolumeFromTAG = 63;
                if (driverReal.gd3.systemName.indexOf("pc-98") > 0) SSGVolumeFromTAG = 31;
                if (driverReal.gd3.systemName.indexOf("PC98") > 0) SSGVolumeFromTAG = 31;
            }

            if (SSGVolumeFromTAG == -1) {
                if (useChip.contains(EnmChip.YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 0, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 1, setting.getBalance().getGimicOPNVolume(), EnmModel.RealModel);
                if (useChip.contains(EnmChip.YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 0, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 1, setting.getBalance().getGimicOPNAVolume(), EnmModel.RealModel);
            } else {
                if (useChip.contains(EnmChip.YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 0, SSGVolumeFromTAG, EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2203))
                    chipRegister.setYM2203SSGVolume((byte) 1, SSGVolumeFromTAG, EnmModel.RealModel);
                if (useChip.contains(EnmChip.YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 0, SSGVolumeFromTAG, EnmModel.RealModel);
                if (useChip.contains(EnmChip.S_YM2608))
                    chipRegister.setYM2608SSGVolume((byte) 1, SSGVolumeFromTAG, EnmModel.RealModel);
            }

            driverVirtual.setYm2151Hosei(((Vgm) driverVirtual).yn2151ClockValue);
            if (driverReal != null) driverReal.setYm2151Hosei(((Vgm) driverReal).yn2151ClockValue);


            //frmMain.ForceChannelMask(EnmChip.Ym2612, 0, 0, true);

            paused = false;
            oneTimeReset = false;

            Thread.sleep(500);

            //Stopped = false;

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }


    private static void resetFadeOutParam() {
        vgmFadeout = false;
        vgmFadeoutCounter = 1.0;
        vgmFadeoutCounterV = 0.00001;
        vgmSpeed = 1;
        vgmRealFadeoutVol = 0;
        vgmRealFadeoutVolWait = 4;

        clearFadeoutVolume();

        chipRegister.resetChips();
    }

    public static void changeChipSampleRate(MDSound.Chip chip, int NewSmplRate) {
        MDSound.Chip caa = chip;

        if (caa.samplingRate == NewSmplRate)
            return;

        // quick and dirty hack to make sample rate changes work
        caa.samplingRate = NewSmplRate;
        if (caa.samplingRate < setting.getOutputDevice().getSampleRate()) // SampleRate)
            caa.resampler = 0x01;
        else if (caa.samplingRate == setting.getOutputDevice().getSampleRate()) // SampleRate)
            caa.resampler = 0x02;
        else if (caa.samplingRate > setting.getOutputDevice().getSampleRate()) // SampleRate)
            caa.resampler = 0x03;
        caa.smpP = 1;
        caa.smpNext -= caa.smpLast;
        caa.smpLast = 0x00;

    }

    public static void ff() {
        if (driverVirtual == null) return;
        vgmSpeed = (vgmSpeed == 1) ? 4 : 1;
        driverVirtual.vgmSpeed = vgmSpeed;
        if (driverReal != null) driverReal.vgmSpeed = vgmSpeed;
    }

    public static void slow() {
        vgmSpeed = (vgmSpeed == 1) ? 0.25 : 1;
        driverVirtual.vgmSpeed = vgmSpeed;
        if (driverReal != null) driverReal.vgmSpeed = vgmSpeed;
    }

    public static void resetSlow() {
        vgmSpeed = 1;
        driverVirtual.vgmSpeed = vgmSpeed;
        if (driverReal != null) driverReal.vgmSpeed = vgmSpeed;
    }

    public static void pause() {

        try {
            paused = !paused;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean getIsPaused() {
        return paused;
    }

    public static boolean isStopped() {
        return stopped;
    }

    public static boolean getIsFadeOut() {
        return vgmFadeout;
    }

    public static boolean getIsSlow() {
        return !isStopped() && (vgmSpeed < 1.0);
    }

    public static boolean getIsFF() {
        return !isStopped() && (vgmSpeed > 1.0);
    }

    static boolean sn76489NGPFlag = false;

    public static boolean getSn76489NGPFlag() {
        return sn76489NGPFlag;
    }

    public static void stepPlay(int Step) {
        stepCounter = Step;
    }

    public static void fadeout() {
        vgmFadeout = true;
    }

    public static void closeWaveWriter() {
        waveWriter.close();
    }

    public static void stop() {

        try {
            if (paused) pause();

            if (stopped) {
                trdClosed = true;
                while (!_trdStopped) {
                    Thread.sleep(1);
                }

                if ((playingFileFormat != FileFormat.WAV
                        || playingFileFormat != FileFormat.MP3
                        || playingFileFormat != FileFormat.AIFF)
                        && naudioFileReader != null) {
                    nAudioStop();
                }

                return;
            }

            if (!paused) {
                LineEvent.Type ps = naudioWrap.GetPlaybackState();
                if (ps != null && ps != LineEvent.Type.STOP) {
                    vgmFadeoutCounterV = 0.1;
                    vgmFadeout = true;
                    int cnt = 0;
                    while (!stopped && cnt < 100) {
                        Thread.yield();
//                        JApplication.DoEvents();
                        cnt++;
                    }
                }
            }
            trdClosed = true;

            if (naudioFileReader != null) {
                nAudioStop();
                return;
            }

            softReset(EnmModel.VirtualModel);
            softReset(EnmModel.RealModel);

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

            softReset(EnmModel.VirtualModel);
            softReset(EnmModel.RealModel);

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

    private static void nAudioStop() {
        try {
            AudioInputStream dmy = naudioFileReader;
            naudioFileReader = null;
            dmy.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {

            stop();
            naudioWrap.Stop();

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void resetTimeCounter() {
        if (driverVirtual == null && driverReal == null) return;
        if (driverVirtual != null) {
            driverVirtual.counter = 0;
            driverVirtual.totalCounter = 0;
            driverVirtual.loopCounter = 0;
        }

        if (driverReal != null) {
            driverReal.counter = 0;
            driverReal.totalCounter = 0;
            driverReal.loopCounter = 0;
        }
    }

    public static long getCounter() {
        if (driverVirtual == null && driverReal == null) return -1;

        if (driverVirtual == null) return driverReal.counter;
        if (driverReal == null) return driverVirtual.counter;

        return Math.max(driverVirtual.counter, driverReal.counter);
    }

    public static long getTotalCounter() {
        if (driverVirtual == null) return -1;

        return driverVirtual.totalCounter;
    }

    public static long getDriverCounter() {
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

    public static long getLoopCounter() {
        if (driverVirtual == null) return -1;

        return driverVirtual.loopCounter;
    }

    public static byte[] getChipStatus() {
        chips[0] = chipRegister.chipLED.PriOPN;
        chipRegister.chipLED.PriOPN = chipLED.PriOPN;
        chips[1] = chipRegister.chipLED.PriOPN2;
        chipRegister.chipLED.PriOPN2 = chipLED.PriOPN2;
        chips[2] = chipRegister.chipLED.PriOPNA;
        chipRegister.chipLED.PriOPNA = chipLED.PriOPNA;
        chips[3] = chipRegister.chipLED.PriOPNB;
        chipRegister.chipLED.PriOPNB = chipLED.PriOPNB;

        chips[4] = chipRegister.chipLED.PriOPM;
        chipRegister.chipLED.PriOPM = chipLED.PriOPM;
        chips[5] = chipRegister.chipLED.PriDCSG;
        chipRegister.chipLED.PriDCSG = chipLED.PriDCSG;
        chips[6] = chipRegister.chipLED.PriRF5C;
        chipRegister.chipLED.PriRF5C = chipLED.PriRF5C;
        chips[7] = chipRegister.chipLED.PriPWM;
        chipRegister.chipLED.PriPWM = chipLED.PriPWM;

        chips[8] = chipRegister.chipLED.PriOKI5;
        chipRegister.chipLED.PriOKI5 = chipLED.PriOKI5;
        chips[9] = chipRegister.chipLED.PriOKI9;
        chipRegister.chipLED.PriOKI9 = chipLED.PriOKI9;
        chips[10] = chipRegister.chipLED.PriC140;
        chipRegister.chipLED.PriC140 = chipLED.PriC140;
        chips[11] = chipRegister.chipLED.PriSPCM;
        chipRegister.chipLED.PriSPCM = chipLED.PriSPCM;

        chips[12] = chipRegister.chipLED.PriAY10;
        chipRegister.chipLED.PriAY10 = chipLED.PriAY10;
        chips[13] = chipRegister.chipLED.PriOPLL;
        chipRegister.chipLED.PriOPLL = chipLED.PriOPLL;
        chips[14] = chipRegister.chipLED.PriHuC;
        chipRegister.chipLED.PriHuC = chipLED.PriHuC;
        chips[15] = chipRegister.chipLED.PriC352;
        chipRegister.chipLED.PriC352 = chipLED.PriC352;
        chips[16] = chipRegister.chipLED.PriK054539;
        chipRegister.chipLED.PriK054539 = chipLED.PriK054539;
        chips[17] = chipRegister.chipLED.PriRF5C68;
        chipRegister.chipLED.PriRF5C68 = chipLED.PriRF5C68;


        chips[128 + 0] = chipRegister.chipLED.SecOPN;
        chipRegister.chipLED.SecOPN = chipLED.SecOPN;
        chips[128 + 1] = chipRegister.chipLED.SecOPN2;
        chipRegister.chipLED.SecOPN2 = chipLED.SecOPN2;
        chips[128 + 2] = chipRegister.chipLED.SecOPNA;
        chipRegister.chipLED.SecOPNA = chipLED.SecOPNA;
        chips[128 + 3] = chipRegister.chipLED.SecOPNB;
        chipRegister.chipLED.SecOPNB = chipLED.SecOPNB;

        chips[128 + 4] = chipRegister.chipLED.SecOPM;
        chipRegister.chipLED.SecOPM = chipLED.SecOPM;
        chips[128 + 5] = chipRegister.chipLED.SecDCSG;
        chipRegister.chipLED.SecDCSG = chipLED.SecDCSG;
        chips[128 + 6] = chipRegister.chipLED.SecRF5C;
        chipRegister.chipLED.SecRF5C = chipLED.SecRF5C;
        chips[128 + 7] = chipRegister.chipLED.SecPWM;
        chipRegister.chipLED.SecPWM = chipLED.SecPWM;

        chips[128 + 8] = chipRegister.chipLED.SecOKI5;
        chipRegister.chipLED.SecOKI5 = chipLED.SecOKI5;
        chips[128 + 9] = chipRegister.chipLED.SecOKI9;
        chipRegister.chipLED.SecOKI9 = chipLED.SecOKI9;
        chips[128 + 10] = chipRegister.chipLED.SecC140;
        chipRegister.chipLED.SecC140 = chipLED.SecC140;
        chips[128 + 11] = chipRegister.chipLED.SecSPCM;
        chipRegister.chipLED.SecSPCM = chipLED.SecSPCM;

        chips[128 + 12] = chipRegister.chipLED.SecAY10;
        chipRegister.chipLED.SecAY10 = chipLED.SecAY10;
        chips[128 + 13] = chipRegister.chipLED.SecOPLL;
        chipRegister.chipLED.SecOPLL = chipLED.SecOPLL;
        chips[128 + 14] = chipRegister.chipLED.SecHuC;
        chipRegister.chipLED.SecHuC = chipLED.SecHuC;
        chips[128 + 15] = chipRegister.chipLED.SecC352;
        chipRegister.chipLED.SecC352 = chipLED.SecC352;
        chips[128 + 16] = chipRegister.chipLED.SecK054539;
        chipRegister.chipLED.SecK054539 = chipLED.SecK054539;
        chips[128 + 17] = chipRegister.chipLED.SecRF5C68;
        chipRegister.chipLED.SecRF5C68 = chipLED.SecRF5C68;


        return chips;
    }

    public static void updateVol() {
        chipRegister.updateVol();
    }

    public static int getVgmCurLoopCounter() {
        int cnt = 0;

        if (driverVirtual != null) {
            cnt = driverVirtual.vgmCurLoop;
        }
        if (driverReal != null) {
            cnt = Math.min(driverReal.vgmCurLoop, cnt);
        }

        return cnt;
    }

    public static boolean getVGMStopped() {
        boolean v;
        boolean r;

        v = driverVirtual == null || driverVirtual.stopped;
        r = driverReal == null || driverReal.stopped;
        return v && r;
    }

    public static boolean getIsDataBlock(EnmModel model) {

        if (model == EnmModel.VirtualModel) {
            if (driverVirtual == null) return false;
            return driverVirtual.isDataBlock;
        } else {
            if (driverReal == null) return false;
            return driverReal.isDataBlock;
        }
    }

    public static boolean getIsPcmRAMWrite(EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (driverVirtual == null) return false;
            if (!(driverVirtual instanceof Vgm)) return false;
            return ((Vgm) driverVirtual).isPcmRAMWrite;
        } else {
            if (driverReal == null) return false;
            if (!(driverReal instanceof Vgm)) return false;
            return ((Vgm) driverReal).isPcmRAMWrite;
        }
    }

    private static void naudioWrapPlaybackStopped(LineEvent e) {
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

    private static void startTrdVgmReal() {
        if (setting.getOutputDevice().getDeviceType() == Common.DEV_Null) {
            return;
        }

        trdClosed = false;
        trdMain = new Thread(Audio::trdVgmRealFunction);
        trdMain.setPriority(Thread.MAX_PRIORITY);
        trdMain.setDaemon(true);
        trdMain.setName("trdVgmReal");
        trdMain.start();
    }

    private static void trdVgmRealFunction() {

        if (driverReal == null) {
            trdClosed = true;
            _trdStopped = true;
            return;
        }

        double o = System.currentTimeMillis() / swFreq;
        double step = 1 / (double) setting.getOutputDevice().getSampleRate();
        _trdStopped = false;
        try {
            while (!trdClosed) {
                Thread.sleep(0);

                double el1 = System.currentTimeMillis() / swFreq;
                if (el1 - o < step) continue;
                if (el1 - o >= step * setting.getOutputDevice().getSampleRate() / 100.0) { // 閾値10ms
                    do {
                        o += step;
                    } while (el1 - o >= step);
                } else {
                    o += step;
                }

                if (stopped || paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (hiyorimiNecessary && driverVirtual.isDataBlock) {
                    continue;
                }

                if (vgmFadeout) {
                    if (vgmRealFadeoutVol != 1000) vgmRealFadeoutVolWait--;
                    if (vgmRealFadeoutVolWait == 0) {
                        if (useChip.contains(EnmChip.YM2151)) chipRegister.setFadeoutVolYM2151(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM2203)) chipRegister.setFadeoutVolYM2203(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.AY8910)) chipRegister.setFadeoutVolAY8910(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM2413)) chipRegister.setFadeoutVolYM2413(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM2608)) chipRegister.setFadeoutVolYM2608(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM2610)) chipRegister.setFadeoutVolYM2610(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM2612)) chipRegister.setFadeoutVolYM2612(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM3526)) chipRegister.setFadeoutVolYM3526(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YM3812)) chipRegister.setFadeoutVolYM3812(0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.SN76489))
                            chipRegister.setFadeoutVolSN76489((byte) 0, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.YMF262)) chipRegister.setFadeoutVolYMF262(0, vgmRealFadeoutVol);

                        if (useChip.contains(EnmChip.S_YM2151)) chipRegister.setFadeoutVolYM2151(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM2203)) chipRegister.setFadeoutVolYM2203(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_AY8910)) chipRegister.setFadeoutVolAY8910(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM2413)) chipRegister.setFadeoutVolYM2413(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM2608)) chipRegister.setFadeoutVolYM2608(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM2610)) chipRegister.setFadeoutVolYM2610(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM2612)) chipRegister.setFadeoutVolYM2612(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM3526)) chipRegister.setFadeoutVolYM3526(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YM3812)) chipRegister.setFadeoutVolYM3812(1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_SN76489))
                            chipRegister.setFadeoutVolSN76489((byte) 1, vgmRealFadeoutVol);
                        if (useChip.contains(EnmChip.S_YMF262)) chipRegister.setFadeoutVolYMF262(1, vgmRealFadeoutVol);

                        vgmRealFadeoutVol++;

                        vgmRealFadeoutVol = Math.min(127, vgmRealFadeoutVol);
                        if (vgmRealFadeoutVol == 127) {
//                            if (SoundChip.realChip != null) {
//                                softReset(EnmModel.RealModel);
//                            }
                            vgmRealFadeoutVolWait = 1000;
                            chipRegister.resetAllMIDIout();
                        } else {
                            vgmRealFadeoutVolWait = 700 - vgmRealFadeoutVol * 2;
                        }
                    }
                }

                if (hiyorimiNecessary) {
                    //long v = driverReal.vgmFrameCounter - driverVirtual.vgmFrameCounter;
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
                    double abs = Math.abs((driverReal.vgmFrameCounter - dSCCI) - (driverVirtual.vgmFrameCounter - dEMU));
                    int m = 0;
                    long l = getLatency() / 10;
                    if (abs >= l) {
                        m = ((driverReal.vgmFrameCounter - dSCCI) > (driverVirtual.vgmFrameCounter - dEMU)) ? 1 : 2;
                    }

                    switch (m) {
                    case 0: // x1
                        driverReal.processOneFrame();
                        break;
                    case 1: // x1/2
                        hiyorimiEven++;
                        if (hiyorimiEven > 1) {
                            driverReal.processOneFrame();
                            hiyorimiEven = 0;
                        }
                        break;
                    case 2: // x2
                        driverReal.processOneFrame();
                        driverReal.processOneFrame();
                        break;
                    }
                } else {
                    driverReal.processOneFrame();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        _trdStopped = true;
    }

    private static void softReset(EnmModel model) {
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

    private static short[] bufVirtualFunction_MIDIKeyboard = null;

    private static int trdVgmVirtualFunction(short[] buffer, int offset, int sampleCount) {
        //return NaudioRead(buffer, offset, sampleCount);

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

    private static int trdVgmVirtualMainFunction(short[] buffer, int offset, int sampleCount) {
        if (buffer == null || buffer.length < 1 || sampleCount == 0) return 0;
        if (driverVirtual == null) return sampleCount;

        try {
            //stwh.Reset(); stwh.Start();

            int i;
            int cnt;

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
                cnt = ((Nsf) driverVirtual).Render(buffer, sampleCount / 2, offset) * 2;
            } else if (driverVirtual instanceof Sid) {
//                driverVirtual.vstDelta = 0;
                cnt = ((Sid) driverVirtual).render(buffer, sampleCount);
            } else if (driverVirtual instanceof MXDRV) {
                mds.setIncFlag();
//                driverVirtual.vstDelta = 0;
                for (i = 0; i < sampleCount; i += 2) {
                    cnt = ((MXDRV) driverVirtual).Render(buffer, offset + i, 2);
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
                cnt = mds.update(buffer, offset, sampleCount, driverVirtual::processOneFrame);
                ProcTimePer1Frame = (int) ((double) System.currentTimeMillis() / (sampleCount + 1) * 1000000.0);
            }

            // VST
//            vstMng.VST_Update(buffer, offset, sampleCount);

            for (i = 0; i < sampleCount; i++) {
                int mul = (int) (16384.0 * Math.pow(10.0, masterVolume / 40.0));
                buffer[offset + i] = (short) Limit((buffer[offset + i] * mul) >> 13, 0x7fff, -0x8000);

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
                    softReset(EnmModel.VirtualModel);
                    softReset(EnmModel.RealModel);

                    waveWriter.write(buffer, offset, i + 1);

                    waveWriter.close();

                    if (mds == null)
                        mds = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), samplingBuffer, null);
                    else
                        mds.init(setting.getOutputDevice().getSampleRate(), samplingBuffer, null);


                    chipRegister.Close();

                    //Thread.sleep(500); // noise対策

                    stopped = true;

                     // 1frame当たりの処理時間
                    //ProcTimePer1Frame = (int)((double)stwh.ElapsedMilliseconds / (i + 1) * 1000000.0);
                    return i + 1;
                }

            }

            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                updateVisualVolume(buffer, offset);
            }

            waveWriter.write(buffer, offset, sampleCount);

             // //1frame当たりの処理時間
            //ProcTimePer1Frame = (int)((double)stwh.ElapsedMilliseconds / sampleCount * 1000000.0);
            return cnt;

        } catch (Exception ex) {
            ex.printStackTrace();
            _fatalError = true;
            stopped = true;
        }

        return -1;
    }

    private static String naudioFileName = null;
    private static AudioInputStream naudioFileReader = null;
//    private static NAudio.Wave.SampleProviders.SampleToWaveProvider16 naudioWs = null;
    private static byte[] naudioSrcbuffer = null;

    public static int nAudioRead(short[] buffer, int offset, int count) {
        try {
            naudioSrcbuffer = ensure(naudioSrcbuffer, count * 2);
//            naudioWs.read(naudioSrcbuffer, 0, count * 2);
            convert2ByteToShort(buffer, offset, naudioSrcbuffer, count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public static byte[] ensure(byte[] buffer, int bytesRequired) {
        if (buffer == null || buffer.length < bytesRequired) {
            buffer = new byte[bytesRequired];
        }
        return buffer;
    }

    private static void convert2ByteToShort(short[] destBuffer, int offset, byte[] source, int shortCount) {
        int samplesRead = shortCount;
        for (int n = 0; n < samplesRead; n++) {
            destBuffer[n] = ByteUtil.readLeShort(source, offset + n * Short.BYTES); // volume;
        }
    }

    private static void updateVisualVolume(short[] buffer, int offset) {
        visVolume.master = buffer[offset];

        int[][][] vol = mds.getYM2151VisVolume();
        if (vol != null)
            visVolume.ym2151 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYM2203VisVolume();
        if (vol != null)
            visVolume.ym2203 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (vol != null)
            visVolume.ym2203FM = (short) getMonoVolume(vol[0][1][0], vol[0][1][1], vol[1][1][0], vol[1][1][1]);
        if (vol != null)
            visVolume.ym2203SSG = (short) getMonoVolume(vol[0][2][0], vol[0][2][1], vol[1][2][0], vol[1][2][1]);

        vol = mds.getYM2612VisVolume();
        if (vol != null)
            visVolume.ym2612 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYM2608VisVolume();
        if (vol != null)
            visVolume.ym2608 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (vol != null)
            visVolume.ym2608FM = (short) getMonoVolume(vol[0][1][0], vol[0][1][1], vol[1][1][0], vol[1][1][1]);
        if (vol != null)
            visVolume.ym2608SSG = (short) getMonoVolume(vol[0][2][0], vol[0][2][1], vol[1][2][0], vol[1][2][1]);
        if (vol != null)
            visVolume.ym2608Rtm = (short) getMonoVolume(vol[0][3][0], vol[0][3][1], vol[1][3][0], vol[1][3][1]);
        if (vol != null)
            visVolume.ym2608APCM = (short) getMonoVolume(vol[0][4][0], vol[0][4][1], vol[1][4][0], vol[1][4][1]);

        vol = mds.getYM2610VisVolume();
        if (vol != null)
            visVolume.ym2610 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (vol != null)
            visVolume.ym2610FM = (short) getMonoVolume(vol[0][1][0], vol[0][1][1], vol[1][1][0], vol[1][1][1]);
        if (vol != null)
            visVolume.ym2610SSG = (short) getMonoVolume(vol[0][2][0], vol[0][2][1], vol[1][2][0], vol[1][2][1]);
        if (vol != null)
            visVolume.ym2610APCMA = (short) getMonoVolume(vol[0][3][0], vol[0][3][1], vol[1][3][0], vol[1][3][1]);
        if (vol != null)
            visVolume.ym2610APCMB = (short) getMonoVolume(vol[0][4][0], vol[0][4][1], vol[1][4][0], vol[1][4][1]);


        vol = mds.getYM2413VisVolume();
        if (vol != null)
            visVolume.ym2413 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYM3526VisVolume();
        if (vol != null)
            visVolume.ym3526 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getY8950VisVolume();
        if (vol != null)
            visVolume.y8950 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYM3812VisVolume();
        if (vol != null)
            visVolume.ym3812 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYMF262VisVolume();
        if (vol != null)
            visVolume.ymf262 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYMF278BVisVolume();
        if (vol != null)
            visVolume.ymf278b = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYMF271VisVolume();
        if (vol != null)
            visVolume.ymf271 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getYMZ280BVisVolume();
        if (vol != null)
            visVolume.ymz280b = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getAY8910VisVolume();
        if (vol != null)
            visVolume.ay8910 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getSN76489VisVolume();
        if (vol != null)
            visVolume.sn76489 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getHuC6280VisVolume();
        if (vol != null)
            visVolume.huc6280 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);


        vol = mds.getRF5C164VisVolume();
        if (vol != null)
            visVolume.rf5c164 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getRF5C68VisVolume();
        if (vol != null)
            visVolume.rf5c68 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getPWMVisVolume();
        if (vol != null) visVolume.pwm = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getOKIM6258VisVolume();
        if (vol != null)
            visVolume.okim6258 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getOKIM6295VisVolume();
        if (vol != null)
            visVolume.okim6295 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getC140VisVolume();
        if (vol != null) visVolume.c140 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getC352VisVolume();
        if (vol != null) visVolume.c352 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getSAA1099VisVolume();
        if (vol != null)
            visVolume.saa1099 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getPPZ8VisVolume();
        if (vol != null) visVolume.ppz8 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getSegaPCMVisVolume();
        if (vol != null)
            visVolume.segaPCM = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getMultiPCMVisVolume();
        if (vol != null)
            visVolume.multiPCM = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getK051649VisVolume();
        if (vol != null)
            visVolume.k051649 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getK053260VisVolume();
        if (vol != null)
            visVolume.k053260 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getK054539VisVolume();
        if (vol != null)
            visVolume.k054539 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getQSoundCtrVisVolume();
        if (vol != null)
            visVolume.qSound = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);

        vol = mds.getGA20VisVolume();
        if (vol != null) visVolume.ga20 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);


        vol = mds.getNESVisVolume();
        if (vol != null) visVolume.APU = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        else visVolume.APU = (short) MDSound.np_nes_apu_volume;

        vol = mds.getDMCVisVolume();
        if (vol != null) visVolume.DMC = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        else visVolume.DMC = (short) MDSound.np_nes_dmc_volume;

        vol = mds.getFDSVisVolume();
        if (vol != null) visVolume.FDS = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        else visVolume.FDS = (short) MDSound.np_nes_fds_volume;

        vol = mds.getMMC5VisVolume();
        if (vol != null) visVolume.MMC5 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (visVolume.MMC5 == 0) visVolume.MMC5 = (short) MDSound.np_nes_mmc5_volume;

        vol = mds.getN160VisVolume();
        if (vol != null) visVolume.N160 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (visVolume.N160 == 0) visVolume.N160 = (short) MDSound.np_nes_n106_volume;

        vol = mds.getVRC6VisVolume();
        if (vol != null) visVolume.VRC6 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (visVolume.VRC6 == 0) visVolume.VRC6 = (short) MDSound.np_nes_vrc6_volume;

        vol = mds.getVRC7VisVolume();
        if (vol != null) visVolume.VRC7 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (visVolume.VRC7 == 0) visVolume.VRC7 = (short) MDSound.np_nes_vrc7_volume;

        vol = mds.getFME7VisVolume();
        if (vol != null) visVolume.FME7 = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
        if (visVolume.FME7 == 0) visVolume.FME7 = (short) MDSound.np_nes_fme7_volume;

        vol = mds.getDMGVisVolume();
        if (vol != null) visVolume.DMG = (short) getMonoVolume(vol[0][0][0], vol[0][0][1], vol[1][0][0], vol[1][0][1]);
    }

    public static int getMonoVolume(int pl, int pr, int sl, int sr) {
        int v = pl + pr + sl + sr;
        v >>= 1;
        if (sl + sr != 0) v >>= 1;

        return v;
    }

    public static int Limit(int v, int max, int min) {
        return v > max ? max : Math.max(v, min);
    }

    public static long getVirtualFrameCounter() {
        if (driverVirtual == null) return -1;
        return driverVirtual.vgmFrameCounter;
    }

    public static long getRealFrameCounter() {
        if (driverReal == null) return -1;
        return driverReal.vgmFrameCounter;
    }

    public static Gd3 getGd3() {
        if (driverVirtual != null) return driverVirtual.gd3;
        return null;
    }


    public static int[][] getFMRegister(int chipID) {
        return chipRegister.fmRegisterYM2612[chipID];
    }

    public static int[][] getYM2612MIDIRegister() {
        return mdsMIDI.ReadYM2612Register(0, (byte) 0);
    }

    public static int[] getYM2151Register(int chipID) {
        return chipRegister.fmRegisterYM2151[chipID];
    }

    public static int[] getYm2203Register(int chipID) {
        return chipRegister.fmRegisterYM2203[chipID];
    }

    public static int[] getYM2413Register(int chipID) {
        return chipRegister.fmRegisterYM2413[chipID];
    }

    public static DeviceInfo.TrackInfo[] getVRC6Register(int chipID) {
        return chipRegister.getVRC6Register(chipID);
    }

    public static byte[] getVRC7Register(int chipID) {
        return chipRegister.getVRC7Register(chipID);
    }

    public static DeviceInfo.TrackInfo[] getN106Register(int chipID) {
        return chipRegister.getN106Register(chipID);
    }

    public static int[][] getYM2608Register(int chipID) {
        return chipRegister.fmRegisterYM2608[chipID];
    }

    public static int[][] getYM2610Register(int chipID) {
        return chipRegister.fmRegisterYM2610[chipID];
    }

    public static int[] getYM3526Register(int chipID) {
        return chipRegister.fmRegisterYM3526[chipID];
    }

    public static int[] getY8950Register(int chipID) {
        return chipRegister.fmRegisterY8950[chipID];
    }

    public static int[] getYM3812Register(int chipID) {
        return chipRegister.fmRegisterYM3812[chipID];
    }

    public static int[][] getYMF262Register(int chipID) {
        return chipRegister.fmRegisterYMF262[chipID];
    }

    public static int[][] getYMF278BRegister(int chipID) {
        return chipRegister.fmRegisterYMF278B[chipID];
    }

    public static int[] getMoonDriverPCMKeyOn() {
        if (driverVirtual instanceof MoonDriver) {
            return ((MoonDriver) driverVirtual).GetPCMKeyOn();
        }
        return null;
    }

    public static int[] getPSGRegister(int chipID) {
        return chipRegister.sn76489Register[chipID];
    }

    public static int getPSGRegisterGGPanning(int chipID) {
        return chipRegister.sn76489RegisterGGPan[chipID];
    }

    public static int[] getAY8910Register(int chipID) {
        return chipRegister.psgRegisterAY8910[chipID];
    }

    public static OotakePsg.HuC6280State getHuC6280Register(int chipID) {
        return mds.ReadHuC6280Status(chipID);
    }

    public static K051649.K051649State getK051649Register(int chipID) {
        return chipRegister.scc_k051649.GetK051649_State((byte) chipID);//  mds.readK051649Status(chipID);
    }

    public static MIDIParam getMIDIInfos(int chipID) {
        return chipRegister.midiParams[chipID];
    }

    public static ScdPcm.PcmChip getRf5c164Register(int chipID) {
        return mds.ReadRf5c164Register(chipID);
    }

    public static mdsound.Rf5c68.Rf5c68State getRf5c68Register(int chipID) {
        return mds.ReadRf5c68Register(chipID);
    }

    public static Ymf271.YMF271Chip getYMF271Register(int chipID) {
        return mds.ReadYMF271Register(chipID);
    }


    public static byte[] getC140Register(int chipID) {
        return chipRegister.pcmRegisterC140[chipID];
    }

    public static PPZ8.PPZ8Status.Channel[] getPPZ8Register(int chipID) {
        return chipRegister.GetPPZ8Register(chipID);
    }

    public static boolean[] getC140KeyOn(int chipID) {
        return chipRegister.pcmKeyOnC140[chipID];
    }

    public static int[] getYMZ280BRegister(int chipID) {
        return chipRegister.YMZ280BRegister[chipID];
    }

    public static int[] getC352Register(int chipID) {
        return chipRegister.pcmRegisterC352[chipID];
    }

    public static MultiPcm.MultiPCM getMultiPCMRegister(int chipID) {
        return chipRegister.getMultiPCMRegister(chipID);
    }

    public static int[] getC352KeyOn(int chipID) {
        return chipRegister.readC352((byte) chipID);
    }

    public static int[] getQSoundRegister(int chipID) {
        return chipRegister.getQSoundRegister(chipID);
    }

    public static byte[] getSEGAPCMRegister(int chipID) {
        return chipRegister.pcmRegisterSEGAPCM[chipID];
    }

    public static boolean[] getSEGAPCMKeyOn(int chipID) {
        return chipRegister.pcmKeyOnSEGAPCM[chipID];
    }

    public static OkiM6258.OkiM6258State getOKIM6258Register(int chipID) {
        return mds.ReadOKIM6258Status(chipID);
    }

    public static SegaPcm.SegaPcmState getSegaPCMRegister(int chipID) {
        return mds.ReadSegaPCMStatus(chipID);
    }

    public static byte[] getAPURegister(int chipID) {
        byte[] reg;

         // nsf向け
        if (chipRegister == null) reg = null;
        else if (chipRegister.nes_apu == null) reg = null;
        else if (chipRegister.nes_apu.apu == null) reg = null;
        else if (chipID == 1) reg = null;
        else reg = chipRegister.nes_apu.apu.reg;

         // vgm向け
        if (reg == null) reg = chipRegister.getNESRegisterAPU(chipID, EnmModel.VirtualModel);

        return reg;
    }

    public static byte[] getDMCRegister(int chipID) {
        byte[] reg;
        try {
             // nsf向け
            if (chipRegister == null) reg = null;
            else if (chipRegister.nes_apu == null) reg = null;
            else if (chipRegister.nes_apu.apu == null) reg = null;
            else if (chipID == 1) reg = null;
            else reg = chipRegister.nes_dmc.dmc.reg;

             // vgm向け
            if (reg == null) reg = chipRegister.getNESRegisterDMC(chipID, EnmModel.VirtualModel);

            return reg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static mdsound.np.NpNesFds getFDSRegister(int chipID) {
        mdsound.np.NpNesFds reg;

         // nsf向け
        if (chipRegister == null) reg = null;
        else if (chipRegister.nes_apu == null) reg = null;
        else if (chipRegister.nes_apu.apu == null) reg = null;
        else if (chipID == 1) reg = null;
        else reg = chipRegister.nes_fds.fds;

         // vgm向け
        if (reg == null) reg = chipRegister.getFDSRegister(chipID, EnmModel.VirtualModel);

        return reg;
    }

    private static final byte[] s5bregs = new byte[0x20];

    public static byte[] getS5BRegister(int chipID) {
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

    public static Gb.GbSound getDMGRegister(int chipID) {
        if (mds == null) return null;
        else if (chipID == 1) return null;

        return mds.ReadDMG((byte) chipID);
    }

    private static final byte[] mmc5regs = new byte[10];

    public static byte[] getMMC5Register(int chipID) {
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

    public static int[] getFMKeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2612[chipID];
    }

    public static int[] getYM2151KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2151[chipID];
    }

    public static boolean getOKIM6258KeyOn(int chipID) {
        return chipRegister.okim6258Keyon[chipID];
    }

    public static void resetOKIM6258KeyOn(int chipID) {
        chipRegister.okim6258Keyon[chipID] = false;
    }

    public static int getYM2151PMD(int chipID) {
        return chipRegister.fmPMDYM2151[chipID];
    }

    public static int getYM2151AMD(int chipID) {
        return chipRegister.fmAMDYM2151[chipID];
    }

    public static int[] getYM2608KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2608[chipID];
    }

    public static int[] getYM2610KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2610[chipID];
    }

    public static int[] getYM2203KeyOn(int chipID) {
        return chipRegister.fmKeyOnYM2203[chipID];
    }

    public static mdplayer.ChipRegister.ChipKeyInfo getYM2413KeyInfo(int chipID) {
        return chipRegister.getYM2413KeyInfo(chipID);
    }

    public static mdplayer.ChipRegister.ChipKeyInfo getYM3526KeyInfo(int chipID) {
        return chipRegister.getYM3526KeyInfo(chipID);
    }

    public static mdplayer.ChipRegister.ChipKeyInfo getY8950KeyInfo(int chipID) {
        return chipRegister.getY8950KeyInfo(chipID);
    }

    public static mdplayer.ChipRegister.ChipKeyInfo getYM3812KeyInfo(int chipID) {
        return chipRegister.getYM3812KeyInfo(chipID);
    }

    public static mdplayer.ChipRegister.ChipKeyInfo getVRC7KeyInfo(int chipID) {
        return chipRegister.getVRC7KeyInfo(chipID);
    }

    public static int getYMF262FMKeyON(int chipID) {
        return chipRegister.getYMF262FMKeyON(chipID);
    }

    public static int getYMF262RyhthmKeyON(int chipID) {
        return chipRegister.getYMF262RyhthmKeyON(chipID);
    }

    public static int getYMF278BFMKeyON(int chipID) {
        return chipRegister.getYMF278BFMKeyON(chipID);
    }

    public static void resetYMF278BFMKeyON(int chipID) {
        chipRegister.resetYMF278BFMKeyON(chipID);
    }

    public static int getYMF278BRyhthmKeyON(int chipID) {
        return chipRegister.getYMF278BRyhthmKeyON(chipID);
    }

    public static void resetYMF278BRyhthmKeyON(int chipID) {
        chipRegister.resetYMF278BRyhthmKeyON(chipID);
    }

    public static int[] getYMF278BPCMKeyON(int chipID) {
        return chipRegister.getYMF278BPCMKeyON(chipID);
    }

    public static void resetYMF278BPCMKeyON(int chipID) {
        chipRegister.resetYMF278BPCMKeyON(chipID);
    }


    public static void setMasterVolume(boolean isAbs, int volume) {
        masterVolume = Common.Range((isAbs ? 0 : setting.getBalance().getMasterVolume()) + volume, -192, 20);
        setting.getBalance().setMasterVolume(masterVolume);
    }

    public static void setAY8910Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getAY8910Volume()) + volume, -192, 20);
            mds.setVolumeAY8910(v);
            setting.getBalance().setAY8910Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2151Volume(boolean isAbs, int volume) {
        try {
            int vol = Common.Range((isAbs ? 0 : setting.getBalance().getYM2151Volume()) + volume, -192, 20);
            setting.getBalance().setYM2151Volume(vol);

            mds.setVolumeYM2151(vol);
            mds.setVolumeYM2151Mame(vol);
            mds.SetVolumeYM2151x68sound(vol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2203Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2203Volume()) + volume, -192, 20);
            mds.SetVolumeYM2203(v);
            setting.getBalance().setYM2203Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2203FMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2203FMVolume()) + volume, -192, 20);
            mds.SetVolumeYM2203FM(v);
            setting.getBalance().setYM2203FMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2203PSGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2203PSGVolume()) + volume, -192, 20);
            mds.SetVolumeYM2203PSG(v);
            setting.getBalance().setYM2203PSGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2413Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2413Volume()) + volume, -192, 20);
            mds.SetVolumeYM2413(v);
            setting.getBalance().setYM2413Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setK053260Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getK053260Volume()) + volume, -192, 20);
            mds.SetVolumeK053260(v);
            setting.getBalance().setK053260Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRF5C68Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getRF5C68Volume()) + volume, -192, 20);
            mds.SetVolumeRF5C68(v);
            setting.getBalance().setRF5C68Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM3812Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM3812Volume()) + volume, -192, 20);
            mds.SetVolumeYM3812(v);
            setting.getBalance().setYM3812Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setY8950Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getY8950Volume()) + volume, -192, 20);
            mds.SetVolumeY8950(v);
            setting.getBalance().setY8950Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM3526Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM3526Volume()) + volume, -192, 20);
            mds.SetVolumeYM3526(v);
            setting.getBalance().setYM3526Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2608Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2608Volume()) + volume, -192, 20);
            mds.SetVolumeYM2608(v);
            setting.getBalance().setYM2608Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2608FMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2608FMVolume()) + volume, -192, 20);
            mds.SetVolumeYM2608FM(v);
            setting.getBalance().setYM2608FMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2608PSGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2608PSGVolume()) + volume, -192, 20);
            mds.SetVolumeYM2608PSG(v);
            setting.getBalance().setYM2608PSGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2608RhythmVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2608RhythmVolume()) + volume, -192, 20);
            mds.SetVolumeYM2608Rhythm(v);
            setting.getBalance().setYM2608RhythmVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2608AdpcmVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2608AdpcmVolume()) + volume, -192, 20);
            mds.SetVolumeYM2608Adpcm(v);
            setting.getBalance().setYM2608AdpcmVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2610Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2610Volume()) + volume, -192, 20);
            mds.SetVolumeYM2610(v);
            setting.getBalance().setYM2610Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2610FMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2610FMVolume()) + volume, -192, 20);
            mds.SetVolumeYM2610FM(v);
            setting.getBalance().setYM2610FMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2610PSGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2610PSGVolume()) + volume, -192, 20);
            mds.SetVolumeYM2610PSG(v);
            setting.getBalance().setYM2610PSGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2610AdpcmAVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2610AdpcmAVolume()) + volume, -192, 20);
            mds.SetVolumeYM2610AdpcmA(v);
            setting.getBalance().setYM2610AdpcmAVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2610AdpcmBVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2610AdpcmBVolume()) + volume, -192, 20);
            mds.SetVolumeYM2610AdpcmB(v);
            setting.getBalance().setYM2610AdpcmBVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYM2612Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYM2612Volume()) + volume, -192, 20);
            mds.SetVolumeYM2612(v);
            setting.getBalance().setYM2612Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSN76489Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getSN76489Volume()) + volume, -192, 20);
            mds.SetVolumeSN76489(v);
            setting.getBalance().setSN76489Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setHuC6280Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getHuC6280Volume()) + volume, -192, 20);
            mds.setVolumeHuC6280(v);
            setting.getBalance().setHuC6280Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRF5C164Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getRF5C164Volume()) + volume, -192, 20);
            mds.SetVolumeRF5C164(v);
            setting.getBalance().setRF5C164Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPWMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getPWMVolume()) + volume, -192, 20);
            mds.SetVolumePWM(v);
            setting.getBalance().setPWMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOKIM6258Volume(boolean isAbs, int volume) {
        try {
            int vol = Common.Range((isAbs ? 0 : setting.getBalance().getOKIM6258Volume()) + volume, -192, 20);
            setting.getBalance().setOKIM6258Volume(vol);

            mds.SetVolumeOKIM6258(vol);
            mds.SetVolumeMpcmX68k(vol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOKIM6295Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getOKIM6295Volume()) + volume, -192, 20);
            mds.SetVolumeOKIM6295(v);
            setting.getBalance().setOKIM6295Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setC140Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getC140Volume()) + volume, -192, 20);
            mds.SetVolumeC140(v);
            setting.getBalance().setC140Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSegaPCMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getSEGAPCMVolume()) + volume, -192, 20);
            mds.setVolumeSegaPCM(v);
            setting.getBalance().setSEGAPCMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setC352Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getC352Volume()) + volume, -192, 20);
            mds.SetVolumeC352(v);
            setting.getBalance().setC352Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSA1099Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getSAA1099Volume()) + volume, -192, 20);
            mds.SetVolumeSAA1099(v);
            setting.getBalance().setSAA1099Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPPZ8Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getPPZ8Volume()) + volume, -192, 20);
            mds.SetVolumePPZ8(v);
            setting.getBalance().setPPZ8Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setK051649Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getK051649Volume()) + volume, -192, 20);
            mds.SetVolumeK051649(v);
            setting.getBalance().setK051649Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setK054539Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getK054539Volume()) + volume, -192, 20);
            mds.SetVolumeK054539(v);
            setting.getBalance().setK054539Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setQSoundVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getQSoundVolume()) + volume, -192, 20);
            mds.SetVolumeQSoundCtr(v);
            setting.getBalance().setQSoundVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDMGVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getDMGVolume()) + volume, -192, 20);
            mds.setVolumeDMG(v);
            setting.getBalance().setDMGVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setGA20Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getGA20Volume()) + volume, -192, 20);
            mds.setVolumeGA20(v);
            setting.getBalance().setGA20Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYMZ280BVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYMZ280BVolume()) + volume, -192, 20);
            mds.setVolumeYMZ280B(v);
            setting.getBalance().setYMZ280BVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYMF271Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYMF271Volume()) + volume, -192, 20);
            mds.setVolumeYMF271(v);
            setting.getBalance().setYMF271Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYMF262Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYMF262Volume()) + volume, -192, 20);
            mds.SetVolumeYMF262(v);
            setting.getBalance().setYMF262Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setYMF278BVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getYMF278BVolume()) + volume, -192, 20);
            mds.SetVolumeYMF278B(v);
            setting.getBalance().setYMF278BVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMultiPCMVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getMultiPCMVolume()) + volume, -192, 20);
            mds.setVolumeMultiPCM(v);
            setting.getBalance().setMultiPCMVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setAPUVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getAPUVolume()) + volume, -192, 20);
            mds.SetVolumeNES(v);
            setting.getBalance().setAPUVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDMCVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getDMCVolume()) + volume, -192, 20);
            mds.SetVolumeDMC(v);
            setting.getBalance().setDMCVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFDSVolume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getFDSVolume()) + volume, -192, 20);
            mds.SetVolumeFDS(v);
            setting.getBalance().setFDSVolume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMMC5Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getMMC5Volume()) + volume, -192, 20);
            mds.SetVolumeMMC5(v);
            setting.getBalance().setMMC5Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setN160Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getN160Volume()) + volume, -192, 20);
            mds.SetVolumeN160(v);
            setting.getBalance().setN160Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVRC6Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getVRC6Volume()) + volume, -192, 20);
            mds.SetVolumeVRC6(v);
            setting.getBalance().setVRC6Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVRC7Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getVRC7Volume()) + volume, -192, 20);
            mds.SetVolumeVRC7(v);
            setting.getBalance().setVRC7Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFME7Volume(boolean isAbs, int volume) {
        try {
            int v = Common.Range((isAbs ? 0 : setting.getBalance().getFME7Volume()) + volume, -192, 20);
            mds.SetVolumeFME7(v);
            setting.getBalance().setFME7Volume(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setGimicOPNVolume(boolean isAbs, int volume) {
        setting.getBalance().setGimicOPNVolume(Common.Range((isAbs ? 0 : setting.getBalance().getGimicOPNVolume()) + volume, 0, 127));
    }

    public static void setGimicOPNAVolume(boolean isAbs, int volume) {
        setting.getBalance().setGimicOPNAVolume(Common.Range((isAbs ? 0 : setting.getBalance().getGimicOPNAVolume()) + volume, 0, 127));
    }


    public static int[] getFMVolume(int chipID) {
        return chipRegister.getYM2612Volume(chipID);
    }

    public static int[] getYM2151Volume(int chipID) {
        return chipRegister.getYM2151Volume(chipID);
    }

    public static int[] getYM2608Volume(int chipID) {
        return chipRegister.getYM2608Volume(chipID);
    }

    public static int[][] getYM2608RhythmVolume(int chipID) {
        return chipRegister.getYM2608RhythmVolume(chipID);
    }

    public static int[] getYM2608AdpcmVolume(int chipID) {
        return chipRegister.getYM2608AdpcmVolume(chipID);
    }

    public static int[] getYM2610Volume(int chipID) {
        return chipRegister.getYM2610Volume(chipID);
    }

    public static int[][] getYM2610RhythmVolume(int chipID) {
        return chipRegister.getYM2610RhythmVolume(chipID);
    }

    public static int[] getYM2610AdpcmVolume(int chipID) {
        return chipRegister.getYM2610AdpcmVolume(chipID);
    }

    public static int[] getYM2203Volume(int chipID) {
        return chipRegister.getYM2203Volume(chipID);
    }

    public static int[] getFMCh3SlotVolume(int chipID) {
        return chipRegister.getYM2612Ch3SlotVolume(chipID);
    }

    public static int[] getYM2608Ch3SlotVolume(int chipID) {
        return chipRegister.getYM2608Ch3SlotVolume(chipID);
    }

    public static int[] getYM2610Ch3SlotVolume(int chipID) {
        return chipRegister.getYM2610Ch3SlotVolume(chipID);
    }

    public static int[] getYM2203Ch3SlotVolume(int chipID) {
        return chipRegister.getYM2203Ch3SlotVolume(chipID);
    }

    public static int[][] getPSGVolume(int chipID) {
        return chipRegister.getPSGVolume(chipID);
    }

    public static void setRF5C164Mask(int chipID, int ch) {
        //mds.setRf5c164Mask(chipID, ch);
        chipRegister.setMaskRF5C164(chipID, ch, true);
    }

    public static void setRF5C68Mask(int chipID, int ch) {
        //mds.setRf5c68Mask(chipID, ch);
        chipRegister.setMaskRF5C68(chipID, ch, true);
    }

    public static void setSN76489Mask(int chipID, int ch) {
        //mds.setSN76489Mask(chipID,1 << ch);
        chipRegister.setMaskSN76489(chipID, ch, true);
        sn76489ForcedSendVolume(chipID, ch);
    }

    public static void resetSN76489Mask(int chipID, int ch) {
        try {
            //mds.resetSN76489Mask(chipID, 1 << ch);
            chipRegister.setMaskSN76489(chipID, ch, false);
            sn76489ForcedSendVolume(chipID, ch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sn76489ForcedSendVolume(int chipID, int ch) {
        Setting.ChipType2 ct = setting.getSN76489Type()[chipID];
        chipRegister.setSN76489Register(chipID
                , (byte) (0x90
                        | ((ch & 3) << 5)
                        | (15 - (Math.max(chipRegister.sn76489Vol[chipID][ch][0], chipRegister.sn76489Vol[chipID][ch][1]) & 0xf)))
                , ct.getUseEmu()[0] ? EnmModel.VirtualModel : EnmModel.RealModel);
    }

    public static void setYM2151Mask(int chipID, int ch) {
        //mds.setYM2151Mask(ch);
        chipRegister.setMaskYM2151(chipID, ch, true, false);
    }

    public static void setYM2203Mask(int chipID, int ch) {
        chipRegister.setMaskYM2203(chipID, ch, true, false);
    }

    public static void setYM2413Mask(int chipID, int ch) {
        chipRegister.setMaskYM2413(chipID, ch, true);
    }

    public static void setYM2608Mask(int chipID, int ch) {
        //mds.setYM2608Mask(ch);
        chipRegister.setMaskYM2608(chipID, ch, true, false);
    }

    public static void setYM2610Mask(int chipID, int ch) {
        //mds.setYM2610Mask(ch);
        chipRegister.setMaskYM2610(chipID, ch, true);
    }

    public static void setYM2612Mask(int chipID, int ch) {
        chipRegister.setMaskYM2612(chipID, ch, true);
    }

    public static void setYM3526Mask(int chipID, int ch) {
        chipRegister.setMaskYM3526(chipID, ch, true);
    }

    public static void setY8950Mask(int chipID, int ch) {
        chipRegister.setMaskY8950(chipID, ch, true);
    }

    public static void setYM3812Mask(int chipID, int ch) {
        chipRegister.setMaskYM3812(chipID, ch, true);
    }

    public static void setYMF262Mask(int chipID, int ch) {
        chipRegister.setMaskYMF262(chipID, ch, true);
    }

    public static void setYMF278BMask(int chipID, int ch) {
        chipRegister.setMaskYMF278B(chipID, ch, true);
    }

    public static void setC140Mask(int chipID, int ch) {
        //mds.setC140Mask(chipID, 1 << ch);
        chipRegister.setMaskC140(chipID, ch, true);
    }

    public static void setPPZ8Mask(int chipID, int ch) {
        //mds.setPPZ8Mask(chipID, 1 << ch);
        chipRegister.setMaskPPZ8(chipID, ch, true);
    }

    public static void setC352Mask(int chipID, int ch) {
        chipRegister.setMaskC352(chipID, ch, true);
    }

    public static void setSegaPCMMask(int chipID, int ch) {
        //mds.setSegaPcmMask(chipID, 1 << ch);
        chipRegister.setMaskSegaPCM(chipID, ch, true);
    }

    public static void setQSoundMask(int chipID, int ch) {
        chipRegister.setMaskQSound(chipID, ch, true);
    }

    public static void setAY8910Mask(int chipID, int ch) {
        //mds.setAY8910Mask(chipID, 1 << ch);
        chipRegister.setMaskAY8910(chipID, ch, true);
    }

    public static void setHuC6280Mask(int chipID, int ch) {
        //mds.setHuC6280Mask(chipID, 1 << ch);
        chipRegister.setMaskHuC6280(chipID, ch, true);
    }

    public static void setOKIM6258Mask(int chipID) {
        chipRegister.setMaskOKIM6258(chipID, true);
    }

    public static void setOKIM6295Mask(int chipID, int ch) {
        chipRegister.setMaskOKIM6295(chipID, ch, true);
    }

    public static void resetOKIM6295Mask(int chipID, int ch) {
        chipRegister.setMaskOKIM6295(chipID, ch, false);
    }

    public static void setNESMask(int chipID, int ch) {
        chipRegister.setNESMask(chipID, ch);
    }

    public static void setDMCMask(int chipID, int ch) {
        chipRegister.setNESMask(chipID, ch + 2);
    }

    public static void setFDSMask(int chipID) {
        chipRegister.setFDSMask(chipID);
    }

    public static void setMMC5Mask(int chipID, int ch) {
        chipRegister.setMMC5Mask(chipID, ch);
    }

    public static void setVRC7Mask(int chipID, int ch) {
        chipRegister.setVRC7Mask(chipID, ch);
    }

    public static void setK051649Mask(int chipID, int ch) {
        chipRegister.setK051649Mask(chipID, ch);
    }

    public static void setDMGMask(int chipID, int ch) {
        chipRegister.setDMGMask(chipID, ch);
    }

    public static void setVRC6Mask(int chipID, int ch) {
        chipRegister.setVRC6Mask(chipID, ch);
    }

    public static void setN163Mask(int chipID, int ch) {
        chipRegister.setN163Mask(chipID, ch);
    }


    public static void resetOKIM6258Mask(int chipID) {
        chipRegister.setMaskOKIM6258(chipID, false);
    }

    public static void resetYM2612Mask(int chipID, int ch) {
        try {
            //mds.resetYM2612Mask(chipID, 1 << ch);
            chipRegister.setMaskYM2612(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM2203Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2203(chipID, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM2413Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2413(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetRF5C164Mask(int chipID, int ch) {
        try {
            //mds.resetRf5c164Mask(chipID, ch);
            chipRegister.setMaskRF5C164(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetRF5C68Mask(int chipID, int ch) {
        try {
            //mds.resetRf5c68Mask(chipID, ch);
            chipRegister.setMaskRF5C68(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM2151Mask(int chipID, int ch) {
        try {
            //mds.resetYM2151Mask(ch);
            chipRegister.setMaskYM2151(chipID, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM2608Mask(int chipID, int ch) {
        try {
            //mds.resetYM2608Mask(ch);
            chipRegister.setMaskYM2608(chipID, ch, false, stopped);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM2610Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM2610(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM3526Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM3526(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetY8950Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskY8950(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYM3812Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYM3812(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYMF262Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskYMF262(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetYMF278BMask(int chipID, int ch) {
        try {
            chipRegister.setMaskYMF278B(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetC140Mask(int chipID, int ch) {
        //mds.resetC140Mask(chipID, 1 << ch);
        try {
            chipRegister.setMaskC140(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetPPZ8Mask(int chipID, int ch) {
        //mds.resetPPZ8Mask(chipID, 1 << ch);
        try {
            chipRegister.setMaskPPZ8(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetC352Mask(int chipID, int ch) {
        try {
            chipRegister.setMaskC352(chipID, ch, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetSegaPCMMask(int chipID, int ch) {
        //mds.resetSegaPcmMask(chipID, 1 << ch);
        chipRegister.setMaskSegaPCM(chipID, ch, false);
    }

    public static void resetQSoundMask(int chipID, int ch) {
        chipRegister.setMaskQSound(chipID, ch, false);
    }

    public static void resetAY8910Mask(int chipID, int ch) {
        //mds.resetAY8910Mask(chipID, 1 << ch);
        chipRegister.setMaskAY8910(chipID, ch, false);
    }

    public static void resetHuC6280Mask(int chipID, int ch) {
        //mds.resetHuC6280Mask(chipID, 1 << ch);
        chipRegister.setMaskHuC6280(chipID, ch, false);
    }

    public static void resetNESMask(int chipID, int ch) {
        chipRegister.resetNESMask(chipID, ch);
    }

    public static void resetDMCMask(int chipID, int ch) {
        chipRegister.resetNESMask(chipID, ch + 2);
    }

    public static void resetFDSMask(int chipID) {
        chipRegister.resetFDSMask(chipID);
    }

    public static void resetMMC5Mask(int chipID, int ch) {
        chipRegister.resetMMC5Mask(chipID, ch);
    }

    public static void resetVRC7Mask(int chipID, int ch) {
        chipRegister.resetVRC7Mask(chipID, ch);
    }

    public static void resetK051649Mask(int chipID, int ch) {
        chipRegister.resetK051649Mask(chipID, ch);
    }

    public static void resetDMGMask(int chipID, int ch) {
        chipRegister.resetDMGMask(chipID, ch);
    }

    public static void resetVRC6Mask(int chipID, int ch) {
        chipRegister.resetVRC6Mask(chipID, ch);
    }

    public static void resetN163Mask(int chipID, int ch) {
        chipRegister.resetN163Mask(chipID, ch);
    }
}


