
package mdplayer;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.Common.EnmInstFormat;
import mdplayer.properties.Resources;
import vavi.util.Debug;
import vavi.util.serdes.Serdes;


public class Setting implements Serializable {

    public static class ChipType2 implements Serializable {
        private boolean[] useEmu = null;

        public boolean[] getUseEmu() {
            return useEmu;
        }

        public void setUseEmu(boolean[] value) {
            useEmu = value;
        }

        private boolean[] useReal = null;

        public boolean[] getUseReal() {
            return useReal;
        }

        public void setUseReal(boolean[] value) {
            useReal = value;
        }

        private RealChipInfo[] realChipInfos = null;

        public RealChipInfo[] getRealChipInfo() {
            return realChipInfos;
        }

        public void setRealChipInfo(RealChipInfo[] value) {
            realChipInfos = value;
        }

        public static class RealChipInfo {

            /** Chip共通 識別情報 */
            private int interfaceType = -1;

            public int getInterfaceType() {
                return interfaceType;
            }

            void setInterfaceType(int value) {
                interfaceType = value;
            }

            private int soundLocation = -1;

            public int getSoundLocation() {
                return soundLocation;
            }

            public void setSoundLocation(int value) {
                soundLocation = value;
            }

            private int busID = -1;

            public int getBusID() {
                return busID;
            }

            public void setBusID(int value) {
                busID = value;
            }

            private int soundChip = -1;

            public int getSoundChip() {
                return soundChip;
            }

            public void setSoundChip(int value) {
                soundChip = value;
            }

            private int chipType = 0;

            public int getChipType() {
                return chipType;
            }

            void setChipType(int value) {
                chipType = value;
            }

            private String interfaceName = "";

            public String getInterfaceName() {
                return interfaceName;
            }

            public void setInterfaceName(String value) {
                interfaceName = value;
            }

            private String chipName = "";

            public String getChipName() {
                return chipName;
            }

            void setChipName(String value) {
                chipName = value;
            }

            // Chip固有の追加設定

            /** ウエイトコマンドをSCCIに送るか */
            private boolean useWait = true;

            public boolean getUseWait() {
                return useWait;
            }

            public void setUseWait(boolean value) {
                useWait = value;
            }

            /** ウエイトコマンドを2倍にするか */
            private boolean useWaitBoost = false;

            public boolean getUseWaitBoost() {
                return useWaitBoost;
            }

            public void setUseWaitBoost(boolean value) {
                useWaitBoost = value;
            }

            /** PCMのみエミュレーションするか */
            private boolean onlyPCMEmulation = false;

            public boolean getOnlyPCMEmulation() {
                return onlyPCMEmulation;
            }

            public void setOnlyPCMEmulation(boolean value) {
                onlyPCMEmulation = value;
            }

            public RealChipInfo copy() {
                RealChipInfo ret = new RealChipInfo();

                ret.interfaceType = this.interfaceType;
                ret.soundLocation = this.soundLocation;
                ret.busID = this.busID;
                ret.soundChip = this.soundChip;
                ret.chipType = this.chipType;
                ret.interfaceName = this.interfaceName;
                ret.chipName = this.chipName;

                ret.useWait = this.useWait;
                ret.useWaitBoost = this.useWaitBoost;
                ret.onlyPCMEmulation = this.onlyPCMEmulation;

                return ret;
            }
        }

        /** Emulation時の遅延時間 */
        private int latencyForEmulation = 0;

        public int getLatencyForEmulation() {
            return latencyForEmulation;
        }

        void setLatencyForEmulation(int value) {
            latencyForEmulation = value;
        }

        private int latencyForReal = 0;

        public int getLatencyForReal() {
            return latencyForReal;
        }

        void setLatencyForReal(int value) {
            latencyForReal = value;
        }

        public ChipType2 copy() {
            ChipType2 ct = new ChipType2();

            ct.useEmu = null;
            if (this.useEmu != null) {
                ct.useEmu = new boolean[this.useEmu.length];
                System.arraycopy(this.useEmu, 0, ct.useEmu, 0, this.useEmu.length);
            }

            ct.useReal = null;
            if (this.useReal != null) {
                ct.useReal = new boolean[this.useReal.length];
                System.arraycopy(this.useReal, 0, ct.useReal, 0, this.useReal.length);
            }

            ct.realChipInfos = null;
            if (this.realChipInfos != null) {
                ct.realChipInfos = new RealChipInfo[this.realChipInfos.length];
                for (int i = 0; i < this.realChipInfos.length; i++)
                    if (this.realChipInfos[i] != null)
                        ct.realChipInfos[i] = this.realChipInfos[i].copy();
            }

            ct.latencyForEmulation = this.latencyForEmulation;
            ct.latencyForReal = this.latencyForReal;

            return ct;
        }
    }

    public static class SID implements Serializable {
        public String romKernalPath = "";
        public String romBasicPath = "";
        public String romCharacterPath = "";
        public int quality = 1;
        public int outputBufferSize = 5000;
        public int c64model = 0;
        public boolean c64modelForce = false;
        public int sidModel = 0;
        public boolean sidmodelForce = false;

        public SID copy() {
            SID sid = new SID();

            sid.romKernalPath = this.romKernalPath;
            sid.romBasicPath = this.romBasicPath;
            sid.romCharacterPath = this.romCharacterPath;
            sid.quality = this.quality;
            sid.outputBufferSize = this.outputBufferSize;
            sid.c64model = this.c64model;
            sid.c64modelForce = this.c64modelForce;
            sid.sidModel = this.sidModel;
            sid.sidmodelForce = this.sidmodelForce;

            return sid;
        }
    }

    public static class NukedOPN2 implements Serializable {
        public int emuType = 0;

        // ごめんGensのオプションもここ。。。

        public boolean gensDACHPF = true;
        public boolean gensSSGEG = true;

        public NukedOPN2 Copy() {
            NukedOPN2 no = new NukedOPN2();
            no.emuType = this.emuType;
            no.gensDACHPF = this.gensDACHPF;
            no.gensSSGEG = this.gensSSGEG;

            return no;
        }
    }

    public static class AutoBalance implements Serializable {
        private boolean useThis = false;
        private boolean loadSongBalance = false;
        private boolean loadDriverBalance = false;
        private boolean saveSongBalance = false;
        private boolean samePositionAsSongData = false;

        public boolean getUseThis() {
            return useThis;
        }

        public void setUseThis(boolean value) {
            useThis = value;
        }

        public boolean getLoadSongBalance() {
            return loadSongBalance;
        }

        public void setLoadSongBalance(boolean value) {
            loadSongBalance = value;
        }

        public boolean getLoadDriverBalance() {
            return loadDriverBalance;
        }

        public void setLoadDriverBalance(boolean value) {
            loadDriverBalance = value;
        }

        public boolean getSaveSongBalance() {
            return saveSongBalance;
        }

        public void setSaveSongBalance(boolean value) {
            saveSongBalance = value;
        }

        public boolean getSamePositionAsSongData() {
            return samePositionAsSongData;
        }

        public void setSamePositionAsSongData(boolean value) {
            samePositionAsSongData = value;
        }

        public AutoBalance copy() {
            AutoBalance AutoBalance = new AutoBalance();
            AutoBalance.useThis = this.useThis;
            AutoBalance.loadSongBalance = this.loadSongBalance;
            AutoBalance.loadDriverBalance = this.loadDriverBalance;
            AutoBalance.saveSongBalance = this.saveSongBalance;
            AutoBalance.samePositionAsSongData = this.samePositionAsSongData;

            return AutoBalance;
        }
    }

    public static class PMDDotNET implements Serializable {
        public String compilerArguments = "/v /C";
        public boolean isAuto = true;
        public int soundBoard = 1;
        public boolean usePPSDRV = true;
        public boolean usePPZ8 = true;
        public String driverArguments = "";
        public boolean setManualVolume = false;
        public boolean usePPSDRVUseInterfaceDefaultFreq = true;
        public int ppsDrvManualFreq = 2000;
        public int ppsDrvManualWait = 1;
        public int volumeFM = 0;
        public int volumeSSG = 0;
        public int volumeRhythm = 0;
        public int volumeAdpcm = 0;
        public int volumeGIMICSSG = 31;

        public PMDDotNET copy() {
            PMDDotNET p = new PMDDotNET();
            p.compilerArguments = this.compilerArguments;
            p.isAuto = this.isAuto;
            p.soundBoard = this.soundBoard;
            p.usePPSDRV = this.usePPSDRV;
            p.usePPZ8 = this.usePPZ8;
            p.driverArguments = this.driverArguments;
            p.setManualVolume = this.setManualVolume;
            p.usePPSDRVUseInterfaceDefaultFreq = this.usePPSDRVUseInterfaceDefaultFreq;
            p.ppsDrvManualFreq = this.ppsDrvManualFreq;
            p.ppsDrvManualWait = this.ppsDrvManualWait;
            p.volumeFM = this.volumeFM;
            p.volumeSSG = this.volumeSSG;
            p.volumeRhythm = this.volumeRhythm;
            p.volumeAdpcm = this.volumeAdpcm;
            p.volumeGIMICSSG = this.volumeGIMICSSG;

            return p;
        }
    }

    public static class MidiExport implements Serializable {

        private boolean useMIDIExport = false;

        public boolean getUseMIDIExport() {
            return useMIDIExport;
        }

        public void setUseMIDIExport(boolean value) {
            useMIDIExport = value;
        }

        private boolean useYM2151Export = false;

        public boolean getUseYM2151Export() {
            return useYM2151Export;
        }

        public void setUseYM2151Export(boolean value) {
            useYM2151Export = value;
        }

        private boolean useYM2612Export = true;

        public boolean getUseYM2612Export() {
            return useYM2612Export;
        }

        public void setUseYM2612Export(boolean value) {
            useYM2612Export = value;
        }

        private String exportPath = "";

        public String getExportPath() {
            return exportPath;
        }

        public void setExportPath(String value) {
            exportPath = value;
        }

        private boolean useVOPMex = false;

        public boolean getUseVOPMex() {
            return useVOPMex;
        }

        public void setUseVOPMex(boolean value) {
            useVOPMex = value;
        }

        private boolean keyOnFnum = false;

        public boolean getKeyOnFnum() {
            return keyOnFnum;
        }

        public void setKeyOnFnum(boolean value) {
            keyOnFnum = value;
        }

        public MidiExport copy() {
            MidiExport midiExport = new MidiExport();

            midiExport.useMIDIExport = this.useMIDIExport;
            midiExport.useYM2151Export = this.useYM2151Export;
            midiExport.useYM2612Export = this.useYM2612Export;
            midiExport.exportPath = this.exportPath;
            midiExport.useVOPMex = this.useVOPMex;
            midiExport.keyOnFnum = this.keyOnFnum;

            return midiExport;
        }
    }

    public static class MidiKbd implements Serializable {

        private boolean useMIDIKeyboard = false;
        public boolean getUseMIDIKeyboard() {
            return useMIDIKeyboard;
        }
        public void setUseMIDIKeyboard(boolean value) {
            useMIDIKeyboard = value;
        }
        private String midiInDeviceName = "";
        public String getMidiInDeviceName() {
            return midiInDeviceName;
        }
        public void setMidiInDeviceName(String value) {
            midiInDeviceName = value;
        }
        private boolean isMono = true;
        public boolean isMono() {
            return isMono;
        }
        public void setMono(boolean value) {
            isMono = value;
        }
        private int useFormat = 0;
        public int getUseFormat() {
            return useFormat;
        }
        public void setUseFormat(int value) {
            useFormat = value;
        }
        private int useMonoChannel = 0;
        public int getUseMonoChannel() {
            return useMonoChannel;
        }
        public void setUseMonoChannel(int value) {
            useMonoChannel = value;
        }
        private boolean[] useChannel = new boolean[9];
        public boolean[] getUseChannel() {
            return useChannel;
        }
        void setUseChannel(boolean[] value) {
            useChannel = value;
        }
        private Tone[] tones = new Tone[6];
        public Tone[] getTones() {
            return tones;
        }
        void setTones(Tone[] value) {
            tones = value;
        }
        private int midiCtrlCopyToneFromYM2612Ch1 = 97;
        public int getMidiCtrl_CopyToneFromYM2612Ch1() {
            return midiCtrlCopyToneFromYM2612Ch1;
        }
        public void setMidiCtrl_CopyToneFromYM2612Ch1(int value) {
            midiCtrlCopyToneFromYM2612Ch1 = value;
        }
        private int midiCtrlDelOneLog = 96;
        public int getMidiCtrl_DelOneLog() {
            return midiCtrlDelOneLog;
        }
        public void setMidiCtrl_DelOneLog(int value) {
            midiCtrlDelOneLog = value;
        }
        private int midiCtrlCopySelecttingLogToClipbrd = 66;
        public int getMidiCtrl_CopySelecttingLogToClipbrd() {
            return midiCtrlCopySelecttingLogToClipbrd;
        }
        public void setMidiCtrl_CopySelecttingLogToClipbrd(int value) {
            midiCtrlCopySelecttingLogToClipbrd = value;
        }
        private int midiCtrlStop = -1;
        public int getMidiCtrl_Stop() {
            return midiCtrlStop;
        }
        public void setMidiCtrl_Stop(int value) {
            midiCtrlStop = value;
        }
        private int midiCtrlPause = -1;
        public int getMidiCtrl_Pause() {
            return midiCtrlPause;
        }
        public void setMidiCtrl_Pause(int value) {
            midiCtrlPause = value;
        }
        private int midiCtrlFadeout = -1;
        public int getMidiCtrl_Fadeout() {
            return midiCtrlFadeout;
        }
        public void setMidiCtrl_Fadeout(int value) {
            midiCtrlFadeout = value;
        }
        private int midiCtrlPrevious = -1;
        public int getMidiCtrl_Previous() {
            return midiCtrlPrevious;
        }
        public void setMidiCtrl_Previous(int value) {
            midiCtrlPrevious = value;
        }
        private int midiCtrlSlow = -1;
        public int getMidiCtrlSlow() {
            return midiCtrlSlow;
        }
        public void setMidiCtrlSlow(int value) {
            midiCtrlSlow = value;
        }
        private int midiCtrlPlay = -1;
        public int getMidiCtrl_Play() {
            return midiCtrlPlay;
        }
        public void setMidiCtrl_Play(int value) {
            midiCtrlPlay = value;
        }
        private int midiCtrlFast = -1;
        public int getMidiCtrl_Fast() {
            return midiCtrlFast;
        }
        public void setMidiCtrl_Fast(int value) {
            midiCtrlFast = value;
        }
        private int midiCtrlNext = -1;
        public int getMidiCtrl_Next() {
            return midiCtrlNext;
        }
        public void setMidiCtrl_Next(int value) {
            midiCtrlNext = value;
        }

        public MidiKbd copy() {
            MidiKbd midiKbd = new MidiKbd();

            midiKbd.midiInDeviceName = this.midiInDeviceName;
            midiKbd.useMIDIKeyboard = this.useMIDIKeyboard;
            System.arraycopy(this.useChannel, 0, midiKbd.useChannel, 0, midiKbd.useChannel.length);
            midiKbd.isMono = this.isMono;
            midiKbd.useMonoChannel = this.useMonoChannel;

            midiKbd.midiCtrlCopySelecttingLogToClipbrd = this.midiCtrlCopySelecttingLogToClipbrd;
            midiKbd.midiCtrlCopyToneFromYM2612Ch1 = this.midiCtrlCopyToneFromYM2612Ch1;
            midiKbd.midiCtrlDelOneLog = this.midiCtrlDelOneLog;
            midiKbd.midiCtrlFadeout = this.midiCtrlFadeout;
            midiKbd.midiCtrlFast = this.midiCtrlFast;
            midiKbd.midiCtrlNext = this.midiCtrlNext;
            midiKbd.midiCtrlPause = this.midiCtrlPause;
            midiKbd.midiCtrlPlay = this.midiCtrlPlay;
            midiKbd.midiCtrlPrevious = this.midiCtrlPrevious;
            midiKbd.midiCtrlSlow = this.midiCtrlSlow;
            midiKbd.midiCtrlStop = this.midiCtrlStop;

            return midiKbd;
        }
    }

    public static class KeyBoardHook implements Serializable {
        public static class HookKeyInfo implements Serializable {
            private boolean shift = false;
            private boolean ctrl = false;
            private boolean win = false;
            private boolean alt = false;
            private String key = "(None)";

            public boolean getShift() {
                return shift;
            }
            public void setShift(boolean value) {
                shift = value;
            }
            public boolean getCtrl() {
                return ctrl;
            }
            public void setCtrl(boolean value) {
                ctrl = value;
            }
            public boolean getWin() {
                return win;
            }
            public void setWin(boolean value) {
                win = value;
            }
            public boolean getAlt() {
                return alt;
            }
            public void setAlt(boolean value) {
                alt = value;
            }
            public String getKey() {
                return key;
            }
            public void setKey(String value) {
                key = value;
            }

            public HookKeyInfo copy() {
                HookKeyInfo hookKeyInfo = new HookKeyInfo();
                hookKeyInfo.shift = this.shift;
                hookKeyInfo.ctrl = this.ctrl;
                hookKeyInfo.win = this.win;
                hookKeyInfo.alt = this.alt;
                hookKeyInfo.key = this.key;

                return hookKeyInfo;
            }
        }

        private boolean useKeyBoardHook = false;
        public boolean getUseKeyBoardHook() {
            return useKeyBoardHook;
        }
        public void setUseKeyBoardHook(boolean value) {
            useKeyBoardHook = value;
        }
        public HookKeyInfo getStop() {
            return stop;
        }
        void setStop(HookKeyInfo value) {
            stop = value;
        }
        public HookKeyInfo getPause() {
            return pause;
        }
        void setPause(HookKeyInfo value) {
            pause = value;
        }
        public HookKeyInfo getFadeout() {
            return fadeout;
        }
        void setFadeout(HookKeyInfo value) {
            fadeout = value;
        }
        public HookKeyInfo getPrev() {
            return prev;
        }
        void setPrev(HookKeyInfo value) {
            prev = value;
        }
        public HookKeyInfo getSlow() {
            return slow;
        }
        void setSlow(HookKeyInfo value) {
            slow = value;
        }
        public HookKeyInfo getPlay() {
            return play;
        }
        void setPlay(HookKeyInfo value) {
            play = value;
        }
        public HookKeyInfo getNext() {
            return next;
        }
        void setNext(HookKeyInfo value) {
            next = value;
        }
        public HookKeyInfo getFast() {
            return fast;
        }
        void setFast(HookKeyInfo value) {
            fast = value;
        }

        private HookKeyInfo stop = new HookKeyInfo();
        private HookKeyInfo pause = new HookKeyInfo();
        private HookKeyInfo fadeout = new HookKeyInfo();
        private HookKeyInfo prev = new HookKeyInfo();
        private HookKeyInfo slow = new HookKeyInfo();
        private HookKeyInfo play = new HookKeyInfo();
        private HookKeyInfo next = new HookKeyInfo();
        private HookKeyInfo fast = new HookKeyInfo();

        public KeyBoardHook copy() {
            KeyBoardHook keyBoard = new KeyBoardHook();
            keyBoard.useKeyBoardHook = this.useKeyBoardHook;
            keyBoard.stop = this.stop.copy();
            keyBoard.pause = this.pause.copy();
            keyBoard.fadeout = this.fadeout.copy();
            keyBoard.prev = this.prev.copy();
            keyBoard.slow = this.slow.copy();
            keyBoard.play = this.play.copy();
            keyBoard.next = this.next.copy();
            keyBoard.fast = this.fast.copy();

            return keyBoard;
        }
    }

    public static class Vst implements Serializable {

//        private String defaultPath = "";
//        private String[] vstPluginPath = null;
//        public String[] getVstPluginPath() {
//            return vstPluginPath;
//        }
//        void setVstPluginPath(String[] value) {
//            vstPluginPath = value;
//        }
//        private VstInfo[] vstInfos = null;
//        public VstInfo[] getVSTInfo() {
//            return vstInfos;
//        }
//        public void setVSTInfo(VstInfo[] value) {
//            vstInfos = value;
//        }
//        public String getDefaultPath() {
//            return defaultPath;
//        }
//        public void setDefaultPath(String value) {
//            defaultPath = value;
//        }
//
//        public Vst copy() {
//            Vst vst = new Vst();
//
//            vst.vstInfos = this.vstInfos;
//            vst.defaultPath = this.defaultPath;
//
//            return vst;
//        }
    }

    public static class MidiOut implements Serializable {

        private String gmReset = "30:F0,7E,7F,09,01,F7";
        public String getGMReset() {
            return gmReset;
        }
        public void setGMReset(String value) {
            gmReset = value;
        }
        private String xgReset = "30:F0,43,10,4C,00,00,7E,00,F7";
        public String getXGReset() {
            return xgReset;
        }
        public void setXGReset(String value) {
            xgReset = value;
        }
        private String gsReset = "30:F0,41,10,42,12,40,00,7F,00,41,F7";
        public String getGSReset() {
            return gsReset;
        }
        public void setGSReset(String value) {
            gsReset = value;
        }
        private String custom = "";
        public String getCustom() {
            return custom;
        }
        public void setCustom(String value) {
            custom = value;
        }
        private List<MidiOutInfo[]> midiOutInfos = null;
        public List<MidiOutInfo[]> getMidiOutInfos() {
            return midiOutInfos;
        }
        public void setMidiOutInfos(List<MidiOutInfo[]> value) {
            midiOutInfos = value;
        }

        public MidiOut copy() {
            MidiOut MidiOut = new MidiOut();

            MidiOut.gmReset = this.gmReset;
            MidiOut.xgReset = this.xgReset;
            MidiOut.gsReset = this.gsReset;
            MidiOut.custom = this.custom;
            MidiOut.midiOutInfos = this.midiOutInfos;

            return MidiOut;
        }
    }

    public static final Point EmptyPoint = new Point(0, 0);
    public static final Dimension EmptyDimension = new Dimension(0, 0);

    private static Setting instance = new Setting();

    public void init() {
        if (this.getAY8910Type() == null || this.getAY8910Type().length < 2) {
            this.setAY8910Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getAY8910Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getAY8910Type()[i].setUseEmu(new boolean[1]);
                this.getAY8910Type()[i].getUseEmu()[0] = true;
                this.getAY8910Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getK051649Type() == null || this.getK051649Type().length < 2) {
            this.setK051649Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getK051649Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getK051649Type()[i].setUseEmu(new boolean[1]);
                this.getK051649Type()[i].getUseEmu()[0] = true;
                this.getK051649Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getC140Type() == null || this.getC140Type().length < 2) {
            this.setC140Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getC140Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getC140Type()[i].setUseEmu(new boolean[1]);
                this.getC140Type()[i].getUseEmu()[0] = true;
                this.getC140Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getHuC6280Type() == null || this.getHuC6280Type().length < 2) {
            this.setHuC6280Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getHuC6280Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getHuC6280Type()[i].setUseEmu(new boolean[1]);
                this.getHuC6280Type()[i].getUseEmu()[0] = true;
                this.getHuC6280Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getSEGAPCMType() == null || this.getSEGAPCMType().length < 2) {
            this.setSEGAPCMType(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getSEGAPCMType()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getSEGAPCMType()[i].setUseEmu(new boolean[1]);
                this.getSEGAPCMType()[i].getUseEmu()[0] = true;
                this.getSEGAPCMType()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getSN76489Type() == null || this.getSN76489Type().length < 2) {
            this.setSN76489Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getSN76489Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getSN76489Type()[i].setUseEmu(new boolean[2]);
                this.getSN76489Type()[i].getUseEmu()[0] = true;
                this.getSN76489Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getY8950Type() == null || this.getY8950Type().length < 2) {
            this.setY8950Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getY8950Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getY8950Type()[i].setUseEmu(new boolean[1]);
                this.getY8950Type()[i].getUseEmu()[0] = true;
                this.getY8950Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYM2151Type() == null || this.getYM2151Type().length < 2) {
            this.setYM2151Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM2151Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM2151Type()[i].setUseEmu(new boolean[3]);
                this.getYM2151Type()[i].getUseEmu()[0] = true;
                this.getYM2151Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYM2203Type() == null || this.getYM2203Type().length < 2) {
            this.setYM2203Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM2203Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM2203Type()[i].setUseEmu(new boolean[1]);
                this.getYM2203Type()[i].getUseEmu()[0] = true;
                this.getYM2203Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYM2413Type() == null || this.getYM2413Type().length < 2) {
            this.setYM2413Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM2413Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM2413Type()[i].setUseEmu(new boolean[1]);
                this.getYM2413Type()[i].getUseEmu()[0] = true;
                this.getYM2413Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYM2608Type() == null || this.getYM2608Type().length < 2) {
            this.setYM2608Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM2608Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM2608Type()[i].setUseEmu(new boolean[1]);
                this.getYM2608Type()[i].getUseEmu()[0] = true;
                this.getYM2608Type()[i].setUseReal(new boolean[1]);
            }
        }

        if (this.getYM2610Type() == null
                || this.getYM2610Type().length < 2
                || this.getYM2610Type()[0].getUseReal() == null
                || this.getYM2610Type()[0].getUseReal().length < 3
                || this.getYM2610Type()[1].getUseReal() == null
                || this.getYM2610Type()[1].getUseReal().length < 3
        ) {
            this.setYM2610Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM2610Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo(), new Setting.ChipType2.RealChipInfo(), new Setting.ChipType2.RealChipInfo()});
                this.getYM2610Type()[i].setUseEmu(new boolean[1]);
                this.getYM2610Type()[i].getUseEmu()[0] = true;
                this.getYM2610Type()[i].setUseReal(new boolean[3]);
            }
        }

        if (this.getYM2612Type() == null || this.getYM2612Type().length < 2) {
            this.setYM2612Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM2612Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM2612Type()[i].setUseEmu(new boolean[3]);
                this.getYM2612Type()[i].getUseEmu()[0] = true;
                this.getYM2612Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYM3526Type() == null || this.getYM3526Type().length < 2) {
            this.setYM3526Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM3526Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM3526Type()[i].setUseEmu(new boolean[1]);
                this.getYM3526Type()[i].getUseEmu()[0] = true;
                this.getYM3526Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYM3812Type() == null || this.getYM3812Type().length < 2) {
            this.setYM3812Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYM3812Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYM3812Type()[i].setUseEmu(new boolean[1]);
                this.getYM3812Type()[i].getUseEmu()[0] = true;
                this.getYM3812Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYMF262Type() == null || this.getYMF262Type().length < 2) {
            this.setYMF262Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYMF262Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYMF262Type()[i].setUseEmu(new boolean[1]);
                this.getYMF262Type()[i].getUseEmu()[0] = true;
                this.getYMF262Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYMF271Type() == null || this.getYMF271Type().length < 2) {
            this.setYMF271Type(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYMF271Type()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYMF271Type()[i].setUseEmu(new boolean[1]);
                this.getYMF271Type()[i].getUseEmu()[0] = true;
                this.getYMF271Type()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYMF278BType() == null || this.getYMF278BType().length < 2) {
            this.setYMF278BType(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYMF278BType()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYMF278BType()[i].setUseEmu(new boolean[1]);
                this.getYMF278BType()[i].getUseEmu()[0] = true;
                this.getYMF278BType()[i].setUseReal(new boolean[1]);
            }
        }
        if (this.getYMZ280BType() == null || this.getYMZ280BType().length < 2) {
            this.setYMZ280BType(new Setting.ChipType2[] {new Setting.ChipType2(), new Setting.ChipType2()});
            for (int i = 0; i < 2; i++) {
                this.getYMZ280BType()[i].setRealChipInfo(new Setting.ChipType2.RealChipInfo[] {new Setting.ChipType2.RealChipInfo()});
                this.getYMZ280BType()[i].setUseEmu(new boolean[1]);
                this.getYMZ280BType()[i].getUseEmu()[0] = true;
                this.getYMZ280BType()[i].setUseReal(new boolean[1]);
            }
        }
    }

    public static Setting getInstance() {
        return instance;
    }

    // 多音源対応
    private String fileSearchPathList;
    public String getFileSearchPathList() {
        return fileSearchPathList;
    }
    public void setFileSearchPathList(String value) {
        fileSearchPathList = value;
    }
    private OutputDevice outputDevice = new OutputDevice();
    public OutputDevice getOutputDevice() {
        return outputDevice;
    }
    void setOutputDevice(OutputDevice value) {
        outputDevice = value;
    }
    private ChipType2[] ay8910Type = null;
    public ChipType2[] getAY8910Type() {
        return ay8910Type;
    }
    public void setAY8910Type(ChipType2[] value) {
        ay8910Type = value;
    }

    // private ChipType2[] _AY8910SType = new ChipType2();
    // public ChipType2[] AY8910SType {
    // get() {
    // return _AY8910SType;
    // }
    // set {
    // _AY8910SType = value;
    // }

    private ChipType2[] ym2151Type = null;
    public ChipType2[] getYM2151Type() {
        return ym2151Type;
    }
    public void setYM2151Type(ChipType2[] value) {
        ym2151Type = value;
    }
    private ChipType2[] ym2203Type = null;
    public ChipType2[] getYM2203Type() {
        return ym2203Type;
    }
    public void setYM2203Type(ChipType2[] value) {
        ym2203Type = value;
    }
    private ChipType2[] ym2413Type = null;
    public ChipType2[] getYM2413Type() {
        return ym2413Type;
    }
    public void setYM2413Type(ChipType2[] value) {
        ym2413Type = value;
    }
    private ChipType2[] huC6280Type = null;
    public ChipType2[] getHuC6280Type() {
        return huC6280Type;
    }
    public void setHuC6280Type(ChipType2[] value) {
        huC6280Type = value;
    }
    private ChipType2[] k051649Type = null;
    public ChipType2[] getK051649Type() {
        return k051649Type;
    }
    public void setK051649Type(ChipType2[] value) {
        k051649Type = value;
    }

    // private ChipType2[] _YM2413SType = null;
    // public ChipType2[] YM2413SType
    // get() {
    // return _YM2413SType;
    // }
    // set {
    // _YM2413SType = value;
    // }

    private ChipType2[] ym2608Type = null;
    public ChipType2[] getYM2608Type() {
        return ym2608Type;
    }
    public void setYM2608Type(ChipType2[] value) {
        ym2608Type = value;
    }
    private ChipType2[] ym2610Type = null;
    public ChipType2[] getYM2610Type() {
        return ym2610Type;
    }
    public void setYM2610Type(ChipType2[] value) {
        ym2610Type = value;
    }
    private ChipType2[] ymf262Type = null;
    public ChipType2[] getYMF262Type() {
        return ymf262Type;
    }
    public void setYMF262Type(ChipType2[] value) {
        ymf262Type = value;
    }
    private ChipType2[] ymf271Type = null;
    public ChipType2[] getYMF271Type() {
        return ymf271Type;
    }
    public void setYMF271Type(ChipType2[] value) {
        ymf271Type = value;
    }
    private ChipType2[] _YMF278BType = null;
    public ChipType2[] getYMF278BType() {
        return _YMF278BType;
    }
    public void setYMF278BType(ChipType2[] value) {
        _YMF278BType = value;
    }
    private ChipType2[] _YMZ280BType = null;
    public ChipType2[] getYMZ280BType() {
        return _YMZ280BType;
    }
    public void setYMZ280BType(ChipType2[] value) {
        _YMZ280BType = value;
    }
    private ChipType2[] _YM2612Type = null;
    public ChipType2[] getYM2612Type() {
        return _YM2612Type;
    }
    public void setYM2612Type(ChipType2[] value) {
        _YM2612Type = value;
    }
    private ChipType2[] _SN76489Type = null;
    public ChipType2[] getSN76489Type() {
        return _SN76489Type;
    }
    public void setSN76489Type(ChipType2[] value) {
        _SN76489Type = value;
    }

    // private ChipType2 _YM2151SType = new ChipType2();
    // public ChipType2 YM2151SType
    // get() {
    // return _YM2151SType;
    // }
    // set {
    // _YM2151SType = value;
    // }

    // private ChipType2 _YM2203SType = new ChipType2();
    // public ChipType2 YM2203SType
    // {
    // get()
    // {
    // return _YM2203SType;
    // }

    // set
    // {
    // _YM2203SType = value;
    // }
    // }

    // private ChipType2 _YM2608SType = new ChipType2();
    // public ChipType2 YM2608SType
    // {
    // get()
    // {
    // return _YM2608SType;
    // }

    // set
    // {
    // _YM2608SType = value;
    // }
    // }

    // private ChipType2 _YM2610SType = new ChipType2();
    // public ChipType2 YM2610SType
    // {
    // get()
    // {
    // return _YM2610SType;
    // }

    // set
    // {
    // _YM2610SType = value;
    // }
    // }

    // private ChipType2 _YM2612SType = new ChipType2();
    // public ChipType2 YM2612SType
    // {
    // get()
    // {
    // return _YM2612SType;
    // }

    // set
    // {
    // _YM2612SType = value;
    // }
    // }

    // private ChipType2 _YMF262SType = new ChipType2();
    // public ChipType2 YMF262SType
    // {
    // get()
    // {
    // return _YMF262SType;
    // }

    // set
    // {
    // _YMF262SType = value;
    // }
    // }

    // private ChipType2 _YMF271SType = new ChipType2();
    // public ChipType2 YMF271SType
    // {
    // get()
    // {
    // return _YMF271SType;
    // }

    // set
    // {
    // _YMF271SType = value;
    // }
    // }

    // private ChipType2 _YMF278BSType = new ChipType2();
    // public ChipType2 YMF278BSType
    // {
    // get()
    // {
    // return _YMF278BSType;
    // }

    // set
    // {
    // _YMF278BSType = value;
    // }
    // }

    // private ChipType2 _YMZ280BSType = new ChipType2();
    // public ChipType2 YMZ280BSType {
    // get() {
    // return _YMZ280BSType;
    // }

    // set {
    // _YMZ280BSType = value;
    // }
    // }

    // private ChipType2 _SN76489SType = new ChipType2();
    // public ChipType2 SN76489SType {
    // get() {
    // return _SN76489SType;
    // }

    // set {
    // _SN76489SType = value;
    // }
    // }

    // private ChipType2 _HuC6280SType = new ChipType2();
    // public ChipType2 HuC6280SType {
    // get() {
    // return _HuC6280SType;
    // }

    // set {
    // _HuC6280SType = value;
    // }
    // }

    private ChipType2[] _YM3526Type = null;

    public ChipType2[] getYM3526Type() {
        return _YM3526Type;
    }

    public void setYM3526Type(ChipType2[] value) {
        _YM3526Type = value;
    }

    // private ChipType2 _YM3526SType = new ChipType2();
    // public ChipType2 YM3526SType
    // {
    // get()
    // {
    // return _YM3526SType;
    // }

    // set
    // {
    // _YM3526SType = value;
    // }
    // }

    private ChipType2[] _YM3812Type = null;
    public ChipType2[] getYM3812Type() {
        return _YM3812Type;
    }
    public void setYM3812Type(ChipType2[] value) {
        _YM3812Type = value;
    }

    // private ChipType2 _YM3812SType = new ChipType2();
    // public ChipType2 YM3812SType
    // {
    // get()
    // {
    // return _YM3812SType;
    // }

    // set
    // {
    // _YM3812SType = value;
    // }
    // }

    private ChipType2[] _Y8950Type = null;
    public ChipType2[] getY8950Type() {
        return _Y8950Type;
    }
    public void setY8950Type(ChipType2[] value) {
        _Y8950Type = value;
    }

    // private ChipType2 _Y8950SType = new ChipType2();
    // public ChipType2 Y8950SType
    // {
    // get()
    // {
    // return _Y8950SType;
    // }

    // set
    // {
    // _Y8950SType = value;
    // }
    // }

    private ChipType2[] _C140Type = null;
    public ChipType2[] getC140Type() {
        return _C140Type;
    }
    public void setC140Type(ChipType2[] value) {
        _C140Type = value;
    }

    // private ChipType2 _C140SType = new ChipType2();
    // public ChipType2 C140SType
    // {
    // get()
    // {
    // return _C140SType;
    // }

    // set
    // {
    // _C140SType = value;
    // }
    // }

    private ChipType2[] _SEGAPCMType = null;
    public ChipType2[] getSEGAPCMType() {
        return _SEGAPCMType;
    }
    public void setSEGAPCMType(ChipType2[] value) {
        _SEGAPCMType = value;
    }

    // private ChipType2 _SEGAPCMSType = new ChipType2();
    // public ChipType2 SEGAPCMSType
    // {
    // get()
    // {
    // return _SEGAPCMSType;
    // }

    // set
    // {
    // _SEGAPCMSType = value;
    // }
    // }

    private int latencyEmulation = 0;
    public int getLatencyEmulation() {
        return latencyEmulation;
    }
    public void setLatencyEmulation(int value) {
        latencyEmulation = value;
    }
    private int latencySCCI = 0;
    public int getLatencySCCI() {
        return latencySCCI;
    }
    public void setLatencySCCI(int value) {
        latencySCCI = value;
    }
    private boolean hiyorimiMode = true;
    public boolean getHiyorimiMode() {
        return hiyorimiMode;
    }
    public void setHiyorimiMode(boolean value) {
        hiyorimiMode = value;
    }
    private boolean debugDispFrameCounter = false;
    public boolean getDebug_DispFrameCounter() {
        return debugDispFrameCounter;
    }
    public void setDebug_DispFrameCounter(boolean value) {
        debugDispFrameCounter = value;
    }
    private int debugSCCbaseAddress = 0x9800;
    public int getDebug_SCCbaseAddress() {
        return debugSCCbaseAddress;
    }
    public void setDebug_SCCbaseAddress(int value) {
        debugSCCbaseAddress = value;
    }
    private Other other = new Other();
    public Other getOther() {
        return other;
    }
    void setOther(Other value) {
        other = value;
    }
    private Balance balance = new Balance();
    public Balance getBalance() {
        return balance;
    }
    public void setBalance(Balance value) {
        balance = value;
    }
    private Location location = new Location();
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location value) {
        location = value;
    }
    private MidiExport midiExport = new MidiExport();
    public MidiExport getMidiExport() {
        return midiExport;
    }
    void setMidiExport(MidiExport value) {
        midiExport = value;
    }
    private MidiKbd midiKbd = new MidiKbd();
    public MidiKbd getMidiKbd() {
        return midiKbd;
    }
    void setMidiKbd(MidiKbd value) {
        midiKbd = value;
    }
    private Vst vst = new Vst();
    public Vst getVst() {
        return vst;
    }
    void setVst(Vst value) {
        vst = value;
    }
    private MidiOut midiOut = new MidiOut();
    public MidiOut getMidiOut() {
        return midiOut;
    }
    void setMidiOut(MidiOut value) {
        midiOut = value;
    }
    private NSF nsf = new NSF();
    public NSF getNsf() {
        return nsf;
    }
    void setNsf(NSF value) {
        nsf = value;
    }
    private SID sid = new SID();
    public SID getSid() {
        return sid;
    }
    public void setSid(SID value) {
        sid = value;
    }

    private NukedOPN2 nukedOPN2 = new NukedOPN2();

    public NukedOPN2 getNukedOPN2() {
        return nukedOPN2;
    }

    public void setNukedOPN2(NukedOPN2 value) {
        nukedOPN2 = value;
    }

    private AutoBalance autoBalance = new AutoBalance();

    public AutoBalance getAutoBalance() {
        return autoBalance;
    }

    public void setAutoBalance(AutoBalance value) {
        autoBalance = value;
    }

    private PMDDotNET pmdDotNET = new PMDDotNET();

    public PMDDotNET getPmdDotNET() {
        return pmdDotNET;
    }

    void setPMDDotNET(PMDDotNET value) {
        pmdDotNET = value;
    }

    public KeyBoardHook getKeyBoardHook() {
        return _keyBoardHook;
    }

    void setKeyBoardHook(KeyBoardHook value) {
        _keyBoardHook = value;
    }

    private boolean unuseRealChip;

    public boolean getUnuseRealChip() {
        return unuseRealChip;
    }

    public void setUnuseRealChip(boolean value) {
        unuseRealChip = value;
    }

    private KeyBoardHook _keyBoardHook = new KeyBoardHook();

    public static class OutputDevice implements Serializable {

        private int deviceType = 0;
        public int getDeviceType() {
            return deviceType;
        }
        public void setDeviceType(int value) {
            deviceType = value;
        }
        private int latency = 300;
        public int getLatency() {
            return latency;
        }
        public void setLatency(int value) {
            latency = value;
        }
        private int waitTime = 500;
        public int getWaitTime() {
            return waitTime;
        }
        public void setWaitTime(int value) {
            waitTime = value;
        }
        private String waveOutDeviceName = "";
        public String getWaveOutDeviceName() {
            return waveOutDeviceName;
        }
        public void setWaveOutDeviceName(String value) {
            waveOutDeviceName = value;
        }
        private String directSoundDeviceName = "";
        public String getDirectSoundDeviceName() {
            return directSoundDeviceName;
        }
        public void setDirectSoundDeviceName(String value) {
            directSoundDeviceName = value;
        }
        private String wasapiDeviceName = "";
        public String getWasapiDeviceName() {
            return wasapiDeviceName;
        }
        public void setWasapiDeviceName(String value) {
            wasapiDeviceName = value;
        }
        private boolean wasapiShareMode = true;
        public boolean getWasapiShareMode() {
            return wasapiShareMode;
        }
        public void setWasapiShareMode(boolean value) {
            wasapiShareMode = value;
        }
        private String asioDeviceName = "";
        public String getAsioDeviceName() {
            return asioDeviceName;
        }
        public void setAsioDeviceName(String value) {
            asioDeviceName = value;
        }
        private int sampleRate = 44100;
        public int getSampleRate() {
            return sampleRate;
        }
        public void setSampleRate(int value) {
            sampleRate = value;
        }

        public OutputDevice copy() {
            OutputDevice outputDevice = new OutputDevice();
            outputDevice.deviceType = this.deviceType;
            outputDevice.latency = this.latency;
            outputDevice.waitTime = this.waitTime;
            outputDevice.waveOutDeviceName = this.waveOutDeviceName;
            outputDevice.directSoundDeviceName = this.directSoundDeviceName;
            outputDevice.wasapiDeviceName = this.wasapiDeviceName;
            outputDevice.wasapiShareMode = this.wasapiShareMode;
            outputDevice.asioDeviceName = this.asioDeviceName;
            outputDevice.sampleRate = this.sampleRate;

            return outputDevice;
        }
    }
    // implements Serializable;
    // public class ChipType2
    // {
    // private boolean _UseEmu = true;
    // public boolean UseEmu
    // {
    // get() {
    // return _UseEmu;
    // }

    // set {
    // _UseEmu = value;
    // }
    // }

    // private boolean _UseEmu2 = false;
    // public boolean UseEmu2
    // {
    // get() {
    // return _UseEmu2;
    // }

    // set {
    // _UseEmu2 = value;
    // }
    // }

    // private boolean _UseEmu3 = false;
    // public boolean UseEmu3
    // {
    // get() {
    // return _UseEmu3;
    // }

    // set {
    // _UseEmu3 = value;
    // }
    // }

    // private boolean _UseScci = false;
    // public boolean UseScci
    // {
    // get()
    // {
    // return _UseScci;
    // }

    // set
    // {
    // _UseScci = value;
    // }
    // }

    // private String _InterfaceName = "";
    // public String InterfaceName
    // {
    // get()
    // {
    // return _InterfaceName;
    // }

    // set
    // {
    // _InterfaceName = value;
    // }
    // }

    // private int _Soun.setLocation(-1);
    // public int SoundLocation
    // {
    // get()
    // {
    // return _SoundLocation;
    // }

    // set
    // {
    // _Soun.setLocation(value);
    // }
    // }

    // private int _BusID = -1;
    // public int BusID
    // {
    // get()
    // {
    // return _BusID;
    // }

    // set
    // {
    // _BusID = value;
    // }
    // }

    // private int _SoundChip = -1;
    // public int SoundChip
    // {
    // get()
    // {
    // return _SoundChip;
    // }

    // set
    // {
    // _SoundChip = value;
    // }
    // }

    // private String _ChipName = "";
    // public String ChipName
    // {
    // get()
    // {
    // return _ChipName;
    // }

    // set
    // {
    // _ChipName = value;
    // }
    // }

    // private boolean _UseScci2 = false;
    // public boolean UseScci2
    // {
    // get()
    // {
    // return _UseScci2;
    // }

    // set
    // {
    // _UseScci2 = value;
    // }
    // }

    // private String _InterfaceName2A = "";
    // public String InterfaceName2A
    // {
    // get()
    // {
    // return _InterfaceName2A;
    // }

    // set
    // {
    // _InterfaceName2A = value;
    // }
    // }

    // private int _SoundLocation2A = -1;
    // public int SoundLocation2A
    // {
    // get()
    // {
    // return _SoundLocation2A;
    // }

    // set
    // {
    // _SoundLocation2A = value;
    // }
    // }

    // private int _BusID2A = -1;
    // public int BusID2A
    // {
    // get()
    // {
    // return _BusID2A;
    // }

    // set
    // {
    // _BusID2A = value;
    // }
    // }

    // private int _SoundChip2A = -1;
    // public int SoundChip2A
    // {
    // get()
    // {
    // return _SoundChip2A;
    // }

    // set
    // {
    // _SoundChip2A = value;
    // }
    // }

    // private String _ChipName2A = "";
    // public String ChipName2A
    // {
    // get()
    // {
    // return _ChipName2A;
    // }

    // set
    // {
    // _ChipName2A = value;
    // }
    // }

    // private int _Type = 0;
    // public int Type
    // {
    // get()
    // {
    // return _Type;
    // }

    // set
    // {
    // _Type = value;
    // }
    // }

    // private String _InterfaceName2B = "";
    // public String InterfaceName2B
    // {
    // get()
    // {
    // return _InterfaceName2B;
    // }

    // set
    // {
    // _InterfaceName2B = value;
    // }
    // }

    // private int _SoundLocation2B = -1;
    // public int SoundLocation2B
    // {
    // get()
    // {
    // return _SoundLocation2B;
    // }

    // set
    // {
    // _SoundLocation2B = value;
    // }
    // }

    // private int _BusID2B = -1;
    // public int BusID2B
    // {
    // get()
    // {
    // return _BusID2B;
    // }

    // set
    // {
    // _BusID2B = value;
    // }
    // }

    // private int _SoundChip2B = -1;
    // public int SoundChip2B
    // {
    // get()
    // {
    // return _SoundChip2B;
    // }

    // set
    // {
    // _SoundChip2B = value;
    // }
    // }

    // private String _ChipName2B = "";
    // public String ChipName2B
    // {
    // get()
    // {
    // return _ChipName2B;
    // }

    // set
    // {
    // _ChipName2B = value;
    // }
    // }

    // private boolean _UseWait = true;
    // public boolean UseWait
    // {
    // get()
    // {
    // return _UseWait;
    // }

    // set
    // {
    // _UseWait = value;
    // }
    // }

    // private boolean _UseWaitBoost = false;
    // public boolean UseWaitBoost
    // {
    // get()
    // {
    // return _UseWaitBoost;
    // }

    // set
    // {
    // _UseWaitBoost = value;
    // }
    // }

    // private boolean _OnlyPCMEmulation = false;
    // public boolean OnlyPCMEmulation
    // {
    // get()
    // {
    // return _OnlyPCMEmulation;
    // }

    // set
    // {
    // _OnlyPCMEmulation = value;
    // }
    // }

    // private int _LatencyForEmulation = 0;
    // public int LatencyForEmulation
    // {
    // get()
    // {
    // return _LatencyForEmulation;
    // }

    // set
    // {
    // _LatencyForEmulation = value;
    // }
    // }

    // private int _LatencyForScci = 0;
    // public int LatencyForScci
    // {
    // get()
    // {
    // return _LatencyForScci;
    // }

    // set
    // {
    // _LatencyForScci = value;
    // }
    // }

    // public ChipType2 Copy()
    // {
    // ChipType2 ct = new ChipType2();
    // ct.UseEmu = this.UseEmu;
    // ct.UseEmu2 = this.UseEmu2;
    // ct.UseEmu3 = this.UseEmu3;
    // ct.UseScci = this.UseScci;
    // ct.Soun.setLocation(this.getSoundLocation());

    // ct.BusID = this.BusID;
    // ct.InterfaceName = this.InterfaceName;
    // ct.SoundChip = this.SoundChip;
    // ct.ChipName = this.ChipName;
    // ct.UseScci2 = this.UseScci2;
    // ct.SoundLocation2A = this.SoundLocation2A;

    // ct.InterfaceName2A = this.InterfaceName2A;
    // ct.BusID2A = this.BusID2A;
    // ct.SoundChip2A = this.SoundChip2A;
    // ct.ChipName2A = this.ChipName2A;
    // ct.SoundLocation2B = this.SoundLocation2B;

    // ct.InterfaceName2B = this.InterfaceName2B;
    // ct.BusID2B = this.BusID2B;
    // ct.SoundChip2B = this.SoundChip2B;
    // ct.ChipName2B = this.ChipName2B;

    // ct.UseWait = this.UseWait;
    // ct.UseWaitBoost = this.UseWaitBoost;
    // ct.OnlyPCMEmulation = this.OnlyPCMEmulation;
    // ct.LatencyForEmulation = this.LatencyForEmulation;
    // ct.LatencyForScci = this.LatencyForScci;
    // ct.Type = this.Type;

    // return ct;
    // }
    // }

    public static class Other implements Serializable {
        private boolean useLoopTimes = true;
        public boolean getUseLoopTimes() {
            return useLoopTimes;
        }
        public void setUseLoopTimes(boolean value) {
            useLoopTimes = value;
        }
        private int loopTimes = 2;
        public int getLoopTimes() {
            return loopTimes;
        }
        public void setLoopTimes(int value) {
            loopTimes = value;
        }
        private boolean useGetInst = true;
        public boolean getUseGetInst() {
            return useGetInst;
        }
        public void setUseGetInst(boolean value) {
            useGetInst = value;
        }
        private String defaultDataPath = "";
        public String getDefaultDataPath() {
            return defaultDataPath;
        }
        public void setDefaultDataPath(String value) {
            defaultDataPath = value;
        }
        private EnmInstFormat instFormat = EnmInstFormat.MML2VGM;
        public EnmInstFormat getInstFormat() {
            return instFormat;
        }
        public void setInstFormat(EnmInstFormat value) {
            instFormat = value;
        }
        private int zoom = 1;
        public int getZoom() {
            return zoom;
        }
        public void setZoom(int value) {
            zoom = value;
        }
        private int screenFrameRate = 60;
        public int getScreenFrameRate() {
            return screenFrameRate;
        }
        public void setScreenFrameRate(int value) {
            screenFrameRate = value;
        }
        private boolean autoOpen = false;
        public boolean getAutoOpen() {
            return autoOpen;
        }
        public void setAutoOpen(boolean value) {
            autoOpen = value;
        }
        private boolean dumpSwitch = false;
        public boolean getDumpSwitch() {
            return dumpSwitch;
        }
        public void setDumpSwitch(boolean value) {
            dumpSwitch = value;
        }
        private String dumpPath = "";
        public String getDumpPath() {
            return dumpPath;
        }
        public void setDumpPath(String value) {
            dumpPath = value;
        }
        private boolean wavSwitch = false;
        public boolean getWavSwitch() {
            return wavSwitch;
        }
        public void setWavSwitch(boolean value) {
            wavSwitch = value;
        }
        private String wavPath = "";
        public String getWavPath() {
            return wavPath;
        }
        public void setWavPath(String value) {
            wavPath = value;
        }
        private int filterIndex = 0;
        public int getFilterIndex() {
            return filterIndex;
        }
        public void setFilterIndex(int value) {
            filterIndex = value;
        }
        private String textExt = "txt;doc;hed";
        public String getTextExt() {
            return textExt;
        }
        public void setTextExt(String value) {
            textExt = value;
        }
        private String mmlExt = "mml;gwi;muc;mdl";
        public String getMMLExt() {
            return mmlExt;
        }
        public void setMMLExt(String value) {
            mmlExt = value;
        }
        private String imageExt = "jpg;gif;png;mag";
        public String getImageExt() {
            return imageExt;
        }
        public void setImageExt(String value) {
            imageExt = value;
        }
        private boolean autoOpenText = false;
        public boolean getAutoOpenText() {
            return autoOpenText;
        }
        public void setAutoOpenText(boolean value) {
            autoOpenText = value;
        }
        private boolean autoOpenMML = false;
        public boolean getAutoOpenMML() {
            return autoOpenMML;
        }
        public void setAutoOpenMML(boolean value) {
            autoOpenMML = value;
        }
        private boolean autoOpenImg = false;
        public boolean getAutoOpenImg() {
            return autoOpenImg;
        }
        public void setAutoOpenImg(boolean value) {
            autoOpenImg = value;
        }
        private boolean initAlways = false;
        public boolean getInitAlways() {
            return initAlways;
        }
        public void setInitAlways(boolean value) {
            initAlways = value;
        }
        private boolean emptyPlayList = false;
        public boolean getEmptyPlayList() {
            return emptyPlayList;
        }
        public void setEmptyPlayList(boolean value) {
            emptyPlayList = value;
        }
        boolean exAll = false;
        public boolean getExAll() {
            return exAll;
        }
        public void setExAll(boolean value) {
            exAll = value;
        }
        boolean nonRenderingForPause = false;
        public boolean getNonRenderingForPause() {
            return nonRenderingForPause;
        }
        public void setNonRenderingForPause(boolean value) {
            nonRenderingForPause = value;
        }

        public Other copy() {
            Other other = new Other();
            other.useLoopTimes = this.useLoopTimes;
            other.loopTimes = this.loopTimes;
            other.useGetInst = this.useGetInst;
            other.defaultDataPath = this.defaultDataPath;
            other.instFormat = this.instFormat;
            other.zoom = this.zoom;
            other.screenFrameRate = this.screenFrameRate;
            other.autoOpen = this.autoOpen;
            other.dumpSwitch = this.dumpSwitch;
            other.dumpPath = this.dumpPath;
            other.wavSwitch = this.wavSwitch;
            other.wavPath = this.wavPath;
            other.filterIndex = this.filterIndex;
            other.textExt = this.textExt;
            other.mmlExt = this.mmlExt;
            other.imageExt = this.imageExt;
            other.autoOpenText = this.autoOpenText;
            other.autoOpenMML = this.autoOpenMML;
            other.autoOpenImg = this.autoOpenImg;
            other.initAlways = this.initAlways;
            other.emptyPlayList = this.emptyPlayList;
            other.exAll = this.exAll;
            other.nonRenderingForPause = this.nonRenderingForPause;

            return other;
        }
    }

    public static class Balance implements Serializable {

        private int masterVolume = 0;

        public int getMasterVolume() {
            if (masterVolume > 20 || masterVolume < -192)
                masterVolume = 0;
            return masterVolume;
        }

        public void setMasterVolume(int value) {
            masterVolume = value;
            if (masterVolume > 20 || masterVolume < -192)
                masterVolume = 0;
        }

        private int ym2612Volume = 0;

        public int getYM2612Volume() {
            if (ym2612Volume > 20 || ym2612Volume < -192)
                ym2612Volume = 0;
            return ym2612Volume;
        }

        public void setYM2612Volume(int value) {
            ym2612Volume = value;
            if (ym2612Volume > 20 || ym2612Volume < -192)
                ym2612Volume = 0;
        }

        private int sn76489Volume = 0;

        public int getSN76489Volume() {
            if (sn76489Volume > 20 || sn76489Volume < -192)
                sn76489Volume = 0;
            return sn76489Volume;
        }

        public void setSN76489Volume(int value) {
            sn76489Volume = value;
            if (sn76489Volume > 20 || sn76489Volume < -192)
                sn76489Volume = 0;
        }

        private int rf5C68Volume = 0;

        public int getRF5C68Volume() {
            if (rf5C68Volume > 20 || rf5C68Volume < -192)
                rf5C68Volume = 0;
            return rf5C68Volume;
        }

        public void setRF5C68Volume(int value) {
            rf5C68Volume = value;
            if (rf5C68Volume > 20 || rf5C68Volume < -192)
                rf5C68Volume = 0;
        }

        private int rf5C164Volume = 0;

        public int getRF5C164Volume() {
            if (rf5C164Volume > 20 || rf5C164Volume < -192)
                rf5C164Volume = 0;
            return rf5C164Volume;
        }

        public void setRF5C164Volume(int value) {
            rf5C164Volume = value;
            if (rf5C164Volume > 20 || rf5C164Volume < -192)
                rf5C164Volume = 0;
        }

        private int pwmVolume = 0;

        public int getPWMVolume() {
            if (pwmVolume > 20 || pwmVolume < -192)
                pwmVolume = 0;
            return pwmVolume;
        }

        public void setPWMVolume(int value) {
            pwmVolume = value;
            if (pwmVolume > 20 || pwmVolume < -192)
                pwmVolume = 0;
        }

        private int c140Volume = 0;

        public int getC140Volume() {
            if (c140Volume > 20 || c140Volume < -192)
                c140Volume = 0;
            return c140Volume;
        }

        public void setC140Volume(int value) {
            c140Volume = value;
            if (c140Volume > 20 || c140Volume < -192)
                c140Volume = 0;
        }

        private int OkiM6258Volume = 0;

        public int getOKIM6258Volume() {
            if (OkiM6258Volume > 20 || OkiM6258Volume < -192)
                OkiM6258Volume = 0;
            return OkiM6258Volume;
        }

        public void setOKIM6258Volume(int value) {
            OkiM6258Volume = value;
            if (OkiM6258Volume > 20 || OkiM6258Volume < -192)
                OkiM6258Volume = 0;
        }

        private int _OKIM6295Volume = 0;

        public int getOKIM6295Volume() {
            if (_OKIM6295Volume > 20 || _OKIM6295Volume < -192)
                _OKIM6295Volume = 0;
            return _OKIM6295Volume;
        }

        public void setOKIM6295Volume(int value) {
            _OKIM6295Volume = value;
            if (_OKIM6295Volume > 20 || _OKIM6295Volume < -192)
                _OKIM6295Volume = 0;
        }

        private int _SEGAPCMVolume = 0;

        public int getSEGAPCMVolume() {
            if (_SEGAPCMVolume > 20 || _SEGAPCMVolume < -192)
                _SEGAPCMVolume = 0;
            return _SEGAPCMVolume;
        }

        public void setSEGAPCMVolume(int value) {
            _SEGAPCMVolume = value;
            if (_SEGAPCMVolume > 20 || _SEGAPCMVolume < -192)
                _SEGAPCMVolume = 0;
        }

        private int _AY8910Volume = 0;

        public int getAY8910Volume() {
            if (_AY8910Volume > 20 || _AY8910Volume < -192)
                _AY8910Volume = 0;
            return _AY8910Volume;
        }

        public void setAY8910Volume(int value) {
            _AY8910Volume = value;
            if (_AY8910Volume > 20 || _AY8910Volume < -192)
                _AY8910Volume = 0;
        }

        private int ym2413Volume = 0;

        public int getYM2413Volume() {
            if (ym2413Volume > 20 || ym2413Volume < -192)
                ym2413Volume = 0;
            return ym2413Volume;
        }

        public void setYM2413Volume(int value) {
            ym2413Volume = value;
            if (ym2413Volume > 20 || ym2413Volume < -192)
                ym2413Volume = 0;
        }

        private int _YM3526Volume = 0;

        public int getYM3526Volume() {
            if (_YM3526Volume > 20 || _YM3526Volume < -192)
                _YM3526Volume = 0;
            return _YM3526Volume;
        }

        public void setYM3526Volume(int value) {
            _YM3526Volume = value;
            if (_YM3526Volume > 20 || _YM3526Volume < -192)
                _YM3526Volume = 0;
        }

        private int _Y8950Volume = 0;

        public int getY8950Volume() {
            if (_Y8950Volume > 20 || _Y8950Volume < -192)
                _Y8950Volume = 0;
            return _Y8950Volume;
        }

        public void setY8950Volume(int value) {
            _Y8950Volume = value;
            if (_Y8950Volume > 20 || _Y8950Volume < -192)
                _Y8950Volume = 0;
        }

        private int _HuC6280Volume = 0;

        public int getHuC6280Volume() {
            if (_HuC6280Volume > 20 || _HuC6280Volume < -192)
                _HuC6280Volume = 0;
            return _HuC6280Volume;
        }

        public void setHuC6280Volume(int value) {
            _HuC6280Volume = value;
            if (_HuC6280Volume > 20 || _HuC6280Volume < -192)
                _HuC6280Volume = 0;
        }

        private int ym2151Volume = 0;

        public int getYM2151Volume() {
            if (ym2151Volume > 20 || ym2151Volume < -192)
                ym2151Volume = 0;
            return ym2151Volume;
        }

        public void setYM2151Volume(int value) {
            ym2151Volume = value;
            if (ym2151Volume > 20 || ym2151Volume < -192)
                ym2151Volume = 0;
        }

        private int ym2608Volume = 0;

        public int getYM2608Volume() {
            if (ym2608Volume > 20 || ym2608Volume < -192)
                ym2608Volume = 0;
            return ym2608Volume;
        }

        public void setYM2608Volume(int value) {
            ym2608Volume = value;
            if (ym2608Volume > 20 || ym2608Volume < -192)
                ym2608Volume = 0;
        }

        private int _YM2608FMVolume = 0;

        public int getYM2608FMVolume() {
            if (_YM2608FMVolume > 20 || _YM2608FMVolume < -192)
                _YM2608FMVolume = 0;
            return _YM2608FMVolume;
        }

        public void setYM2608FMVolume(int value) {
            _YM2608FMVolume = value;
            if (_YM2608FMVolume > 20 || _YM2608FMVolume < -192)
                _YM2608FMVolume = 0;
        }

        private int _YM2608PSGVolume = 0;

        public int getYM2608PSGVolume() {
            if (_YM2608PSGVolume > 20 || _YM2608PSGVolume < -192)
                _YM2608PSGVolume = 0;
            return _YM2608PSGVolume;
        }

        public void setYM2608PSGVolume(int value) {
            _YM2608PSGVolume = value;
            if (_YM2608PSGVolume > 20 || _YM2608PSGVolume < -192)
                _YM2608PSGVolume = 0;
        }

        private int _YM2608RhythmVolume = 0;

        public int getYM2608RhythmVolume() {
            if (_YM2608RhythmVolume > 20 || _YM2608RhythmVolume < -192)
                _YM2608RhythmVolume = 0;
            return _YM2608RhythmVolume;
        }

        public void setYM2608RhythmVolume(int value) {
            _YM2608RhythmVolume = value;
            if (_YM2608RhythmVolume > 20 || _YM2608RhythmVolume < -192)
                _YM2608RhythmVolume = 0;
        }

        private int _YM2608AdpcmVolume = 0;

        public int getYM2608AdpcmVolume() {
            if (_YM2608AdpcmVolume > 20 || _YM2608AdpcmVolume < -192)
                _YM2608AdpcmVolume = 0;
            return _YM2608AdpcmVolume;
        }

        public void setYM2608AdpcmVolume(int value) {
            _YM2608AdpcmVolume = value;
            if (_YM2608AdpcmVolume > 20 || _YM2608AdpcmVolume < -192)
                _YM2608AdpcmVolume = 0;
        }

        private int ym2203Volume = 0;

        public int getYM2203Volume() {
            if (ym2203Volume > 20 || ym2203Volume < -192)
                ym2203Volume = 0;
            return ym2203Volume;
        }

        public void setYM2203Volume(int value) {
            ym2203Volume = value;
            if (ym2203Volume > 20 || ym2203Volume < -192)
                ym2203Volume = 0;
        }

        private int ym2203Fmvolume = 0;

        public int getYM2203FMVolume() {
            if (ym2203Fmvolume > 20 || ym2203Fmvolume < -192)
                ym2203Fmvolume = 0;
            return ym2203Fmvolume;
        }

        public void setYM2203FMVolume(int value) {
            ym2203Fmvolume = value;
            if (ym2203Fmvolume > 20 || ym2203Fmvolume < -192)
                ym2203Fmvolume = 0;
        }

        private int ym2203Psgvolume = 0;

        public int getYM2203PSGVolume() {
            if (ym2203Psgvolume > 20 || ym2203Psgvolume < -192)
                ym2203Psgvolume = 0;
            return ym2203Psgvolume;
        }

        public void setYM2203PSGVolume(int value) {
            ym2203Psgvolume = value;
            if (ym2203Psgvolume > 20 || ym2203Psgvolume < -192)
                ym2203Psgvolume = 0;
        }

        private int _YM2610Volume = 0;

        public int getYM2610Volume() {
            if (_YM2610Volume > 20 || _YM2610Volume < -192)
                _YM2610Volume = 0;
            return _YM2610Volume;
        }

        public void setYM2610Volume(int value) {
            _YM2610Volume = value;
            if (_YM2610Volume > 20 || _YM2610Volume < -192)
                _YM2610Volume = 0;
        }

        private int _YM2610FMVolume = 0;

        public int getYM2610FMVolume() {
            if (_YM2610FMVolume > 20 || _YM2610FMVolume < -192)
                _YM2610FMVolume = 0;
            return _YM2610FMVolume;
        }

        public void setYM2610FMVolume(int value) {
            _YM2610FMVolume = value;
            if (_YM2610FMVolume > 20 || _YM2610FMVolume < -192)
                _YM2610FMVolume = 0;
        }

        private int _YM2610PSGVolume = 0;

        public int getYM2610PSGVolume() {
            if (_YM2610PSGVolume > 20 || _YM2610PSGVolume < -192)
                _YM2610PSGVolume = 0;
            return _YM2610PSGVolume;
        }

        public void setYM2610PSGVolume(int value) {
            _YM2610PSGVolume = value;
            if (_YM2610PSGVolume > 20 || _YM2610PSGVolume < -192)
                _YM2610PSGVolume = 0;
        }

        private int _YM2610AdpcmAVolume = 0;

        public int getYM2610AdpcmAVolume() {
            if (_YM2610AdpcmAVolume > 20 || _YM2610AdpcmAVolume < -192)
                _YM2610AdpcmAVolume = 0;
            return _YM2610AdpcmAVolume;
        }

        public void setYM2610AdpcmAVolume(int value) {
            _YM2610AdpcmAVolume = value;
            if (_YM2610AdpcmAVolume > 20 || _YM2610AdpcmAVolume < -192)
                _YM2610AdpcmAVolume = 0;
        }

        private int _YM2610AdpcmBVolume = 0;

        public int getYM2610AdpcmBVolume() {
            if (_YM2610AdpcmBVolume > 20 || _YM2610AdpcmBVolume < -192)
                _YM2610AdpcmBVolume = 0;
            return _YM2610AdpcmBVolume;
        }

        public void setYM2610AdpcmBVolume(int value) {
            _YM2610AdpcmBVolume = value;
            if (_YM2610AdpcmBVolume > 20 || _YM2610AdpcmBVolume < -192)
                _YM2610AdpcmBVolume = 0;
        }

        private int _YM3812Volume = 0;

        public int getYM3812Volume() {
            if (_YM3812Volume > 20 || _YM3812Volume < -192)
                _YM3812Volume = 0;
            return _YM3812Volume;
        }

        public void setYM3812Volume(int value) {
            _YM3812Volume = value;
            if (_YM3812Volume > 20 || _YM3812Volume < -192)
                _YM3812Volume = 0;
        }

        private int _C352Volume = 0;

        public int getC352Volume() {
            if (_C352Volume > 20 || _C352Volume < -192)
                _C352Volume = 0;
            return _C352Volume;
        }

        public void setC352Volume(int value) {
            _C352Volume = value;
            if (_C352Volume > 20 || _C352Volume < -192)
                _C352Volume = 0;
        }

        private int _SAA1099Volume = 0;

        public int getSAA1099Volume() {
            if (_SAA1099Volume > 20 || _SAA1099Volume < -192)
                _SAA1099Volume = 0;
            return _SAA1099Volume;
        }

        public void setSAA1099Volume(int value) {
            _SAA1099Volume = value;
            if (_SAA1099Volume > 20 || _SAA1099Volume < -192)
                _SAA1099Volume = 0;
        }

        private int _WSwanVolume = 0;

        public int getWSwanVolume() {
            if (_WSwanVolume > 20 || _WSwanVolume < -192)
                _WSwanVolume = 0;
            return _WSwanVolume;
        }

        void setWSwanVolume(int value) {
            _WSwanVolume = value;
            if (_WSwanVolume > 20 || _WSwanVolume < -192)
                _WSwanVolume = 0;
        }

        private int _POKEYVolume = 0;

        public int getPOKEYVolume() {
            if (_POKEYVolume > 20 || _POKEYVolume < -192)
                _POKEYVolume = 0;
            return _POKEYVolume;
        }

        void setPOKEYVolume(int value) {
            _POKEYVolume = value;
            if (_POKEYVolume > 20 || _POKEYVolume < -192)
                _POKEYVolume = 0;
        }

        private int _PPZ8Volume = 0;

        public int getPPZ8Volume() {
            if (_PPZ8Volume > 20 || _PPZ8Volume < -192)
                _PPZ8Volume = 0;
            return _PPZ8Volume;
        }

        public void setPPZ8Volume(int value) {
            _PPZ8Volume = value;
            if (_PPZ8Volume > 20 || _PPZ8Volume < -192)
                _PPZ8Volume = 0;
        }

        private int _X1_010Volume = 0;

        public int getX1_010Volume() {
            if (_X1_010Volume > 20 || _X1_010Volume < -192)
                _X1_010Volume = 0;
            return _X1_010Volume;
        }

        void setX1_010Volume(int value) {
            _X1_010Volume = value;
            if (_X1_010Volume > 20 || _X1_010Volume < -192)
                _X1_010Volume = 0;
        }

        private int _K054539Volume = 0;

        public int getK054539Volume() {
            if (_K054539Volume > 20 || _K054539Volume < -192)
                _K054539Volume = 0;
            return _K054539Volume;
        }

        public void setK054539Volume(int value) {
            _K054539Volume = value;
            if (_K054539Volume > 20 || _K054539Volume < -192)
                _K054539Volume = 0;
        }

        private int _APUVolume = 0;

        public int getAPUVolume() {
            if (_APUVolume > 20 || _APUVolume < -192)
                _APUVolume = 0;
            return _APUVolume;
        }

        public void setAPUVolume(int value) {
            _APUVolume = value;
            if (_APUVolume > 20 || _APUVolume < -192)
                _APUVolume = 0;
        }

        private int _DMCVolume = 0;

        public int getDMCVolume() {
            if (_DMCVolume > 20 || _DMCVolume < -192)
                _DMCVolume = 0;
            return _DMCVolume;
        }

        public void setDMCVolume(int value) {
            _DMCVolume = value;
            if (_DMCVolume > 20 || _DMCVolume < -192)
                _DMCVolume = 0;
        }

        private int _FDSVolume = 0;

        public int getFDSVolume() {
            if (_FDSVolume > 20 || _FDSVolume < -192)
                _FDSVolume = 0;
            return _FDSVolume;
        }

        public void setFDSVolume(int value) {
            _FDSVolume = value;
            if (_FDSVolume > 20 || _FDSVolume < -192)
                _FDSVolume = 0;
        }

        private int _MMC5Volume = 0;

        public int getMMC5Volume() {
            if (_MMC5Volume > 20 || _MMC5Volume < -192)
                _MMC5Volume = 0;
            return _MMC5Volume;
        }

        public void setMMC5Volume(int value) {
            _MMC5Volume = value;
            if (_MMC5Volume > 20 || _MMC5Volume < -192)
                _MMC5Volume = 0;
        }

        private int _N160Volume = 0;

        public int getN160Volume() {
            if (_N160Volume > 20 || _N160Volume < -192)
                _N160Volume = 0;
            return _N160Volume;
        }

        public void setN160Volume(int value) {
            _N160Volume = value;
            if (_N160Volume > 20 || _N160Volume < -192)
                _N160Volume = 0;
        }

        private int _VRC6Volume = 0;

        public int getVRC6Volume() {
            if (_VRC6Volume > 20 || _VRC6Volume < -192)
                _VRC6Volume = 0;
            return _VRC6Volume;
        }

        public void setVRC6Volume(int value) {
            _VRC6Volume = value;
            if (_VRC6Volume > 20 || _VRC6Volume < -192)
                _VRC6Volume = 0;
        }

        private int _VRC7Volume = 0;

        public int getVRC7Volume() {
            if (_VRC7Volume > 20 || _VRC7Volume < -192)
                _VRC7Volume = 0;
            return _VRC7Volume;
        }

        public void setVRC7Volume(int value) {
            _VRC7Volume = value;
            if (_VRC7Volume > 20 || _VRC7Volume < -192)
                _VRC7Volume = 0;
        }

        private int _FME7Volume = 0;

        public int getFME7Volume() {
            if (_FME7Volume > 20 || _FME7Volume < -192)
                _FME7Volume = 0;
            return _FME7Volume;
        }

        public void setFME7Volume(int value) {
            _FME7Volume = value;
            if (_FME7Volume > 20 || _FME7Volume < -192)
                _FME7Volume = 0;
        }

        private int _DMGVolume = 0;

        public int getDMGVolume() {
            if (_DMGVolume > 20 || _DMGVolume < -192)
                _DMGVolume = 0;
            return _DMGVolume;
        }

        public void setDMGVolume(int value) {
            _DMGVolume = value;
            if (_DMGVolume > 20 || _DMGVolume < -192)
                _DMGVolume = 0;
        }

        private int _GA20Volume = 0;

        public int getGA20Volume() {
            if (_GA20Volume > 20 || _GA20Volume < -192)
                _GA20Volume = 0;
            return _GA20Volume;
        }

        public void setGA20Volume(int value) {
            _GA20Volume = value;
            if (_GA20Volume > 20 || _GA20Volume < -192)
                _GA20Volume = 0;
        }

        private int _YMZ280BVolume = 0;

        public int getYMZ280BVolume() {
            if (_YMZ280BVolume > 20 || _YMZ280BVolume < -192)
                _YMZ280BVolume = 0;
            return _YMZ280BVolume;
        }

        public void setYMZ280BVolume(int value) {
            _YMZ280BVolume = value;
            if (_YMZ280BVolume > 20 || _YMZ280BVolume < -192)
                _YMZ280BVolume = 0;
        }

        private int _YMF271Volume = 0;

        public int getYMF271Volume() {
            if (_YMF271Volume > 20 || _YMF271Volume < -192)
                _YMF271Volume = 0;
            return _YMF271Volume;
        }

        public void setYMF271Volume(int value) {
            _YMF271Volume = value;
            if (_YMF271Volume > 20 || _YMF271Volume < -192)
                _YMF271Volume = 0;
        }

        private int _YMF262Volume = 0;

        public int getYMF262Volume() {
            if (_YMF262Volume > 20 || _YMF262Volume < -192)
                _YMF262Volume = 0;
            return _YMF262Volume;
        }

        public void setYMF262Volume(int value) {
            _YMF262Volume = value;
            if (_YMF262Volume > 20 || _YMF262Volume < -192)
                _YMF262Volume = 0;
        }

        private int _YMF278BVolume = 0;

        public int getYMF278BVolume() {
            if (_YMF278BVolume > 20 || _YMF278BVolume < -192)
                _YMF278BVolume = 0;
            return _YMF278BVolume;
        }

        public void setYMF278BVolume(int value) {
            _YMF278BVolume = value;
            if (_YMF278BVolume > 20 || _YMF278BVolume < -192)
                _YMF278BVolume = 0;
        }

        private int _MultiPCMVolume = 0;

        public int getMultiPCMVolume() {
            if (_MultiPCMVolume > 20 || _MultiPCMVolume < -192)
                _MultiPCMVolume = 0;
            return _MultiPCMVolume;
        }

        public void setMultiPCMVolume(int value) {
            _MultiPCMVolume = value;
            if (_MultiPCMVolume > 20 || _MultiPCMVolume < -192)
                _MultiPCMVolume = 0;
        }

        private int _QSoundVolume = 0;

        public int getQSoundVolume() {
            if (_QSoundVolume > 20 || _QSoundVolume < -192)
                _QSoundVolume = 0;
            return _QSoundVolume;
        }

        public void setQSoundVolume(int value) {
            _QSoundVolume = value;
            if (_QSoundVolume > 20 || _QSoundVolume < -192)
                _QSoundVolume = 0;
        }

        private int _K051649Volume = 0;

        public int getK051649Volume() {
            if (_K051649Volume > 20 || _K051649Volume < -192)
                _K051649Volume = 0;
            return _K051649Volume;
        }

        public void setK051649Volume(int value) {
            _K051649Volume = value;
            if (_K051649Volume > 20 || _K051649Volume < -192)
                _K051649Volume = 0;
        }

        private int _K053260Volume = 0;

        public int getK053260Volume() {
            if (_K053260Volume > 20 || _K053260Volume < -192)
                _K053260Volume = 0;
            return _K053260Volume;
        }

        public void setK053260Volume(int value) {
            _K053260Volume = value;
            if (_K053260Volume > 20 || _K053260Volume < -192)
                _K053260Volume = 0;
        }

        private int _GimicOPNVolume = 0;

        public int getGimicOPNVolume() {
            if (_GimicOPNVolume > 127 || _GimicOPNVolume < 0)
                _GimicOPNVolume = 30;
            return _GimicOPNVolume;
        }

        public void setGimicOPNVolume(int value) {
            _GimicOPNVolume = value;
            if (_GimicOPNVolume > 127 || _GimicOPNVolume < 0)
                _GimicOPNVolume = 30;
        }

        private int _GimicOPNAVolume = 0;

        public int getGimicOPNAVolume() {
            if (_GimicOPNAVolume > 127 || _GimicOPNAVolume < 0)
                _GimicOPNAVolume = 30;
            return _GimicOPNAVolume;
        }

        public void setGimicOPNAVolume(int value) {
            _GimicOPNAVolume = value;
            if (_GimicOPNAVolume > 127 || _GimicOPNAVolume < 0)
                _GimicOPNAVolume = 30;
        }

        public Balance copy() {
            Balance balance = new Balance();
            balance.masterVolume = this.masterVolume;
            balance.ym2151Volume = this.ym2151Volume;
            balance.ym2203Volume = this.ym2203Volume;
            balance.ym2203Fmvolume = this.ym2203Fmvolume;
            balance.ym2203Psgvolume = this.ym2203Psgvolume;
            balance.ym2413Volume = this.ym2413Volume;
            balance.ym2608Volume = this.ym2608Volume;
            balance._YM2608FMVolume = this._YM2608FMVolume;
            balance._YM2608PSGVolume = this._YM2608PSGVolume;
            balance._YM2608RhythmVolume = this._YM2608RhythmVolume;
            balance._YM2608AdpcmVolume = this._YM2608AdpcmVolume;
            balance._YM2610Volume = this._YM2610Volume;
            balance._YM2610FMVolume = this._YM2610FMVolume;
            balance._YM2610PSGVolume = this._YM2610PSGVolume;
            balance._YM2610AdpcmAVolume = this._YM2610AdpcmAVolume;
            balance._YM2610AdpcmBVolume = this._YM2610AdpcmBVolume;

            balance.ym2612Volume = this.ym2612Volume;
            balance._AY8910Volume = this._AY8910Volume;
            balance.sn76489Volume = this.sn76489Volume;
            balance._HuC6280Volume = this._HuC6280Volume;
            balance._SAA1099Volume = this._SAA1099Volume;

            balance.rf5C164Volume = this.rf5C164Volume;
            balance.rf5C68Volume = this.rf5C68Volume;
            balance.pwmVolume = this.pwmVolume;
            balance.OkiM6258Volume = this.OkiM6258Volume;
            balance._OKIM6295Volume = this._OKIM6295Volume;
            balance.c140Volume = this.c140Volume;
            balance._SEGAPCMVolume = this._SEGAPCMVolume;
            balance._C352Volume = this._C352Volume;
            balance._K051649Volume = this._K051649Volume;
            balance._K053260Volume = this._K053260Volume;
            balance._K054539Volume = this._K054539Volume;
            balance._QSoundVolume = this._QSoundVolume;
            balance._MultiPCMVolume = this._MultiPCMVolume;

            balance._APUVolume = this._APUVolume;
            balance._DMCVolume = this._DMCVolume;
            balance._FDSVolume = this._FDSVolume;
            balance._MMC5Volume = this._MMC5Volume;
            balance._N160Volume = this._N160Volume;
            balance._VRC6Volume = this._VRC6Volume;
            balance._VRC7Volume = this._VRC7Volume;
            balance._FME7Volume = this._FME7Volume;
            balance._DMGVolume = this._DMGVolume;
            balance._GA20Volume = this._GA20Volume;
            balance._YMZ280BVolume = this._YMZ280BVolume;
            balance._YMF271Volume = this._YMF271Volume;
            balance._YMF262Volume = this._YMF262Volume;
            balance._YMF278BVolume = this._YMF278BVolume;
            balance._YM3526Volume = this._YM3526Volume;
            balance._Y8950Volume = this._Y8950Volume;
            balance._YM3812Volume = this._YM3812Volume;

            balance._PPZ8Volume = this._PPZ8Volume;
            balance._GimicOPNVolume = this._GimicOPNVolume;
            balance._GimicOPNAVolume = this._GimicOPNAVolume;

            return balance;
        }

        public void save(String fullPath) {
            try (OutputStream out = Files.newOutputStream(Paths.get(fullPath))) {
                Serdes.Util.serialize(this, out);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public static Balance load(String fullPath) {
            java.nio.file.Path p = Paths.get(fullPath);
            if (!Files.exists(p))
                return null;
            try (InputStream in = Files.newInputStream(p)) {
                return Serdes.Util.deserialize(in, new Balance());
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static class Location implements Serializable {
        private Point _PMain = EmptyPoint;

        public Point getPMain() {
            if (_PMain.x < 0 || _PMain.y < 0) {
                return new Point(0, 0);
            }
            return _PMain;
        }

        public void setPMain(Point value) {
            _PMain = value;
        }

        private Point _PInfo = EmptyPoint;

        public Point getPInfo() {
            if (_PInfo.x < 0 || _PInfo.y < 0) {
                return new Point(0, 0);
            }
            return _PInfo;
        }

        public void setPInfo(Point value) {
            _PInfo = value;
        }

        private boolean _OInfo = false;

        public boolean getOInfo() {
            return _OInfo;
        }

        public void setOInfo(boolean value) {
            _OInfo = value;
        }

        private Point _PPlayList = EmptyPoint;

        public Point getPPlayList() {
            if (_PPlayList.x < 0 || _PPlayList.y < 0) {
                return new Point(0, 0);
            }
            return _PPlayList;
        }

        public void setPPlayList(Point value) {
            _PPlayList = value;
        }

        private boolean _OPlayList = false;

        public boolean getOPlayList() {
            return _OPlayList;
        }

        public void setOPlayList(boolean value) {
            _OPlayList = value;
        }

        private Dimension _PPlayListWH = EmptyDimension;

        public Dimension getPPlayListWH() {
            if (_PPlayListWH.width < 0 || _PPlayListWH.height < 0) {
                return new Dimension(0, 0);
            }
            return _PPlayListWH;
        }

        public void setPPlayListWH(Dimension value) {
            _PPlayListWH = value;
        }

        private Point _PMixer = EmptyPoint;

        public Point getPMixer() {
            if (_PMixer.x < 0 || _PMixer.y < 0) {
                return new Point(0, 0);
            }
            return _PMixer;
        }

        void setPMixer(Point value) {
            _PMixer = value;
        }

        private boolean _OMixer = false;

        public boolean getOMixer() {
            return _OMixer;
        }

        public void setOMixer(boolean value) {
            _OMixer = value;
        }

        private Point _PMixerWH = EmptyPoint;

        public Point getPMixerWH() {
            if (_PMixerWH.x < 0 || _PMixerWH.y < 0) {
                return new Point(0, 0);
            }
            return _PMixerWH;
        }

        void setPMixerWH(Point value) {
            _PMixerWH = value;
        }

        private Point[] _PosRf5c164 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosRf5c164() {
            return _PosRf5c164;
        }

        void setPosRf5c164(Point[] value) {
            _PosRf5c164 = value;
        }

        private boolean[] _OpenRf5c164 = new boolean[] {
                false, false
        };

        public boolean[] getOpenRf5c164() {
            return _OpenRf5c164;
        }

        void setOpenRf5c164(boolean[] value) {
            _OpenRf5c164 = value;
        }

        private Point[] _PosRf5c68 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosRf5c68() {
            return _PosRf5c68;
        }

        void setPosRf5c68(Point[] value) {
            _PosRf5c68 = value;
        }

        private boolean[] _OpenRf5c68 = new boolean[] {
                false, false
        };

        public boolean[] getOpenRf5c68() {
            return _OpenRf5c68;
        }

        void setOpenRf5c68(boolean[] value) {
            _OpenRf5c68 = value;
        }

        private Point[] _PosYMF271 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYMF271() {
            return _PosYMF271;
        }

        void setPosYMF271(Point[] value) {
            _PosYMF271 = value;
        }

        private boolean[] _OpenYMF271 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYMF271() {
            return _OpenYMF271;
        }

        void setOpenYMF271(boolean[] value) {
            _OpenYMF271 = value;
        }

        private Point[] _PosC140 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosC140() {
            return _PosC140;
        }

        void setPosC140(Point[] value) {
            _PosC140 = value;
        }

        private boolean[] openC140 = new boolean[] {
                false, false
        };

        public boolean[] getOpenC140() {
            return openC140;
        }

        void setOpenC140(boolean[] value) {
            openC140 = value;
        }

        private Point[] posS5B = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosS5B() {
            return posS5B;
        }

        void setPosS5B(Point[] value) {
            posS5B = value;
        }

        private boolean[] _OpenS5B = new boolean[] {
                false, false
        };

        public boolean[] getOpenS5B() {
            return _OpenS5B;
        }

        void setOpenS5B(boolean[] value) {
            _OpenS5B = value;
        }

        private Point[] _PosDMG = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosDMG() {
            return _PosDMG;
        }

        void setPosDMG(Point[] value) {
            _PosDMG = value;
        }

        private boolean[] _OpenDMG = new boolean[] {
                false, false
        };

        public boolean[] getOpenDMG() {
            return _OpenDMG;
        }

        void setOpenDMG(boolean[] value) {
            _OpenDMG = value;
        }

        private Point[] _PosPPZ8 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosPPZ8() {
            return _PosPPZ8;
        }

        void setPosPPZ8(Point[] value) {
            _PosPPZ8 = value;
        }

        private boolean[] _OpenPPZ8 = new boolean[] {
                false, false
        };

        public boolean[] getOpenPPZ8() {
            return _OpenPPZ8;
        }

        void setOpenPPZ8(boolean[] value) {
            _OpenPPZ8 = value;
        }

        private Point[] _PosYMZ280B = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYMZ280B() {
            return _PosYMZ280B;
        }

        void setPosYMZ280B(Point[] value) {
            _PosYMZ280B = value;
        }

        private boolean[] _OpenYMZ280B = new boolean[] {
                false, false
        };

        public boolean[] getOpenYMZ280B() {
            return _OpenYMZ280B;
        }

        void setOpenYMZ280B(boolean[] value) {
            _OpenYMZ280B = value;
        }

        private Point[] _PosC352 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosC352() {
            return _PosC352;
        }

        void setPosC352(Point[] value) {
            _PosC352 = value;
        }

        private boolean[] openC352 = new boolean[] {
                false, false
        };

        public boolean[] getOpenC352() {
            return openC352;
        }

        void setOpenC352(boolean[] value) {
            openC352 = value;
        }

        private Point[] _PosMultiPCM = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosMultiPCM() {
            return _PosMultiPCM;
        }

        void setPosMultiPCM(Point[] value) {
            _PosMultiPCM = value;
        }

        private boolean[] _OpenMultiPCM = new boolean[] {
                false, false
        };

        public boolean[] getOpenMultiPCM() {
            return _OpenMultiPCM;
        }

        void setOpenMultiPCM(boolean[] value) {
            _OpenMultiPCM = value;
        }

        private boolean[] _OpenQSound = new boolean[] {
                false, false
        };

        public boolean[] getOpenQSound() {
            return _OpenQSound;
        }

        void setOpenQSound(boolean[] value) {
            _OpenQSound = value;
        }

        private Point[] _PosYm2151 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm2151() {
            return _PosYm2151;
        }

        void setPosYm2151(Point[] value) {
            _PosYm2151 = value;
        }

        private boolean[] _OpenYm2151 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm2151() {
            return _OpenYm2151;
        }

        void setOpenYm2151(boolean[] value) {
            _OpenYm2151 = value;
        }

        private Point[] _PosYm2608 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm2608() {
            return _PosYm2608;
        }

        void setPosYm2608(Point[] value) {
            _PosYm2608 = value;
        }

        private boolean[] _OpenYm2608 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm2608() {
            return _OpenYm2608;
        }

        void setOpenYm2608(boolean[] value) {
            _OpenYm2608 = value;
        }

        private Point[] _PosYm2203 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm2203() {
            return _PosYm2203;
        }

        void setPosYm2203(Point[] value) {
            _PosYm2203 = value;
        }

        private boolean[] _OpenYm2203 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm2203() {
            return _OpenYm2203;
        }

        void setOpenYm2203(boolean[] value) {
            _OpenYm2203 = value;
        }

        private Point[] _PosYm2610 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm2610() {
            return _PosYm2610;
        }

        void setPosYm2610(Point[] value) {
            _PosYm2610 = value;
        }

        private boolean[] _OpenYm2610 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm2610() {
            return _OpenYm2610;
        }

        void setOpenYm2610(boolean[] value) {
            _OpenYm2610 = value;
        }

        private Point[] _PosYm2612 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm2612() {
            return _PosYm2612;
        }

        void setPosYm2612(Point[] value) {
            _PosYm2612 = value;
        }

        private boolean[] _OpenYm2612 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm2612() {
            return _OpenYm2612;
        }

        void setOpenYm2612(boolean[] value) {
            _OpenYm2612 = value;
        }

        private Point[] _PosOKIM6258 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosOKIM6258() {
            return _PosOKIM6258;
        }

        void setPosOKIM6258(Point[] value) {
            _PosOKIM6258 = value;
        }

        private boolean[] _OpenOKIM6258 = new boolean[] {
                false, false
        };

        public boolean[] getOpenOKIM6258() {
            return _OpenOKIM6258;
        }

        void setOpenOKIM6258(boolean[] value) {
            _OpenOKIM6258 = value;
        }

        private Point[] _PosOKIM6295 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosOKIM6295() {
            return _PosOKIM6295;
        }

        void setPosOKIM6295(Point[] value) {
            _PosOKIM6295 = value;
        }

        private boolean[] openOKIM6295 = new boolean[] {
                false, false
        };

        public boolean[] getOpenOKIM6295() {
            return openOKIM6295;
        }

        void setOpenOKIM6295(boolean[] value) {
            openOKIM6295 = value;
        }

        private Point[] _PosSN76489 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosSN76489() {
            return _PosSN76489;
        }

        void setPosSN76489(Point[] value) {
            _PosSN76489 = value;
        }

        private boolean[] _OpenSN76489 = new boolean[] {
                false, false
        };

        public boolean[] getOpenSN76489() {
            return _OpenSN76489;
        }

        void setOpenSN76489(boolean[] value) {
            _OpenSN76489 = value;
        }

        private Point[] _PosMIDI = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosMIDI() {
            return _PosMIDI;
        }

        void setPosMIDI(Point[] value) {
            _PosMIDI = value;
        }

        private boolean[] _OpenMIDI = new boolean[] {
                false, false
        };

        public boolean[] getOpenMIDI() {
            return _OpenMIDI;
        }

        public void setOpenMIDI(boolean[] value) {
            _OpenMIDI = value;
        }

        private Point[] _PosSegaPCM = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosSegaPCM() {
            return _PosSegaPCM;
        }

        void setPosSegaPCM(Point[] value) {
            _PosSegaPCM = value;
        }

        private boolean[] _OpenSegaPCM = new boolean[] {
                false, false
        };

        public boolean[] getOpenSegaPCM() {
            return _OpenSegaPCM;
        }

        void setOpenSegaPCM(boolean[] value) {
            _OpenSegaPCM = value;
        }

        private Point[] _PosAY8910 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosAY8910() {
            return _PosAY8910;
        }

        void setPosAY8910(Point[] value) {
            _PosAY8910 = value;
        }

        private boolean[] _OpenAY8910 = new boolean[] {
                false, false
        };

        public boolean[] getOpenAY8910() {
            return _OpenAY8910;
        }

        void setOpenAY8910(boolean[] value) {
            _OpenAY8910 = value;
        }

        private Point[] _PosHuC6280 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosHuC6280() {
            return _PosHuC6280;
        }

        void setPosHuC6280(Point[] value) {
            _PosHuC6280 = value;
        }

        private boolean[] _OpenHuC6280 = new boolean[] {
                false, false
        };

        public boolean[] getOpenHuC6280() {
            return _OpenHuC6280;
        }

        void setOpenHuC6280(boolean[] value) {
            _OpenHuC6280 = value;
        }

        private Point[] _PosK051649 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosK051649() {
            return _PosK051649;
        }

        void setPosK051649(Point[] value) {
            _PosK051649 = value;
        }

        private boolean[] _OpenK051649 = new boolean[] {
                false, false
        };

        public boolean[] getOpenK051649() {
            return _OpenK051649;
        }

        void setOpenK051649(boolean[] value) {
            _OpenK051649 = value;
        }

        private Point[] _PosYm2413 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm2413() {
            return _PosYm2413;
        }

        void setPosYm2413(Point[] value) {
            _PosYm2413 = value;
        }

        private boolean[] _OpenYm2413 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm2413() {
            return _OpenYm2413;
        }

        void setOpenYm2413(boolean[] value) {
            _OpenYm2413 = value;
        }

        private Point[] _PosYm3526 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm3526() {
            return _PosYm3526;
        }

        void setPosYm3526(Point[] value) {
            _PosYm3526 = value;
        }

        private boolean[] _OpenYm3526 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm3526() {
            return _OpenYm3526;
        }

        void setOpenYm3526(boolean[] value) {
            _OpenYm3526 = value;
        }

        private Point[] _PosY8950 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosY8950() {
            return _PosY8950;
        }

        void setPosY8950(Point[] value) {
            _PosY8950 = value;
        }

        private boolean[] _OpenY8950 = new boolean[] {
                false, false
        };

        public boolean[] getOpenY8950() {
            return _OpenY8950;
        }

        void setOpenY8950(boolean[] value) {
            _OpenY8950 = value;
        }

        private Point[] _PosYm3812 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYm3812() {
            return _PosYm3812;
        }

        void setPosYm3812(Point[] value) {
            _PosYm3812 = value;
        }

        private boolean[] _OpenYm3812 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYm3812() {
            return _OpenYm3812;
        }

        void setOpenYm3812(boolean[] value) {
            _OpenYm3812 = value;
        }

        private Point[] _PosYmf262 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYmf262() {
            return _PosYmf262;
        }

        void setPosYmf262(Point[] value) {
            _PosYmf262 = value;
        }

        private boolean[] _OpenYmf262 = new boolean[] {
                false, false
        };

        public boolean[] getOpenYmf262() {
            return _OpenYmf262;
        }

        void setOpenYmf262(boolean[] value) {
            _OpenYmf262 = value;
        }

        private Point[] _PosYmf278b = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosYmf278b() {
            return _PosYmf278b;
        }

        void setPosYmf278b(Point[] value) {
            _PosYmf278b = value;
        }

        private boolean[] _OpenYmf278b = new boolean[] {
                false, false
        };

        public boolean[] getOpenYmf278b() {
            return _OpenYmf278b;
        }

        void setOpenYmf278b(boolean[] value) {
            _OpenYmf278b = value;
        }

        private Point _PosYm2612MIDI = EmptyPoint;

        public Point getPosYm2612MIDI() {
            return _PosYm2612MIDI;
        }

        public void setPosYm2612MIDI(Point value) {
            _PosYm2612MIDI = value;
        }

        private boolean _OpenYm2612MIDI = false;

        public boolean getOpenYm2612MIDI() {
            return _OpenYm2612MIDI;
        }

        public void setOpenYm2612MIDI(boolean value) {
            _OpenYm2612MIDI = value;
        }

        private Point _PosMixer = EmptyPoint;

        public Point getPosMixer() {
            return _PosMixer;
        }

        public void setPosMixer(Point value) {
            _PosMixer = value;
        }

        private boolean _OpenMixer = false;

        public boolean getOpenMixer() {
            return _OpenMixer;
        }

        void setOpenMixer(boolean value) {
            _OpenMixer = value;
        }

        private Point _PosVSTeffectList = EmptyPoint;

        public Point getPosVSTeffectList() {
            return _PosVSTeffectList;
        }

        public void setPosVSTeffectList(Point value) {
            _PosVSTeffectList = value;
        }

        private boolean _OpenVSTeffectList = false;

        public boolean getOpenVSTeffectList() {
            return _OpenVSTeffectList;
        }

        public void setOpenVSTeffectList(boolean value) {
            _OpenVSTeffectList = value;
        }

        private Point[] _PosNESDMC = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosNESDMC() {
            return _PosNESDMC;
        }

        void setPosNESDMC(Point[] value) {
            _PosNESDMC = value;
        }

        private boolean[] _OpenNESDMC = new boolean[] {
                false, false
        };

        public boolean[] getOpenNESDMC() {
            return _OpenNESDMC;
        }

        void setOpenNESDMC(boolean[] value) {
            _OpenNESDMC = value;
        }

        private Point[] _PosFDS = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosFDS() {
            return _PosFDS;
        }

        void setPosFDS(Point[] value) {
            _PosFDS = value;
        }

        private boolean[] _OpenFDS = new boolean[] {
                false, false
        };

        public boolean[] getOpenFDS() {
            return _OpenFDS;
        }

        void setOpenFDS(boolean[] value) {
            _OpenFDS = value;
        }

        private Point[] _PosMMC5 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosMMC5() {
            return _PosMMC5;
        }

        void setPosMMC5(Point[] value) {
            _PosMMC5 = value;
        }

        private boolean[] _OpenMMC5 = new boolean[] {
                false, false
        };

        public boolean[] getOpenMMC5() {
            return _OpenMMC5;
        }

        void setOpenMMC5(boolean[] value) {
            _OpenMMC5 = value;
        }

        private Point[] _PosVrc6 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosVrc6() {
            return _PosVrc6;
        }

        void setPosVrc6(Point[] value) {
            _PosVrc6 = value;
        }

        private boolean[] _OpenVrc6 = new boolean[] {
                false, false
        };

        public boolean[] getOpenVrc6() {
            return _OpenVrc6;
        }

        void setOpenVrc6(boolean[] value) {
            _OpenVrc6 = value;
        }

        private Point[] _PosVrc7 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosVrc7() {
            return _PosVrc7;
        }

        void setPosVrc7(Point[] value) {
            _PosVrc7 = value;
        }

        private boolean[] _OpenVrc7 = new boolean[] {
                false, false
        };

        public boolean[] getOpenVrc7() {
            return _OpenVrc7;
        }

        void setOpenVrc7(boolean[] value) {
            _OpenVrc7 = value;
        }

        private Point[] _PosN106 = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosN106() {
            return _PosN106;
        }

        void setPosN106(Point[] value) {
            _PosN106 = value;
        }

        private boolean[] _OpenN106 = new boolean[] {
                false, false
        };

        public boolean[] getOpenN106() {
            return _OpenN106;
        }

        void setOpenN106(boolean[] value) {
            _OpenN106 = value;
        }

        private Point[] _PosQSound = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosQSound() {
            return _PosQSound;
        }

        void setPosQSound(Point[] value) {
            _PosQSound = value;
        }

        private Point[] _PosRegTest = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosRegTest() {
            return _PosRegTest;
        }

        void setPosRegTest(Point[] value) {
            _PosRegTest = value;
        }

        private boolean[] _OpenRegTest = new boolean[] {
                false, false
        };

        public boolean[] getOpenRegTest() {
            return _OpenRegTest;
        }

        void setOpenRegTest(boolean[] value) {
            _OpenRegTest = value;
        }

        private Point _PosVisWave = EmptyPoint;

        public Point getPosVisWave() {
            return _PosVisWave;
        }

        public void setPosVisWave(Point value) {
            _PosVisWave = value;
        }

        private boolean _OpenVisWave = false;

        public boolean getOpenVisWave() {
            return _OpenVisWave;
        }

        public void setOpenVisWave(boolean value) {
            _OpenVisWave = value;
        }

        int _ChipSelect;

        public int getChipSelect() {
            return _ChipSelect;
        }

        public void setChipSelect(int value) {
            _ChipSelect = value;
        }

        public Location Copy() {
            Location location = new Location();

            location._PMain = this._PMain;
            location._PInfo = this._PInfo;
            location._OInfo = this._OInfo;
            location._PPlayList = this._PPlayList;
            location._OPlayList = this._OPlayList;
            location._PPlayListWH = this._PPlayListWH;
            location._PMixer = this._PMixer;
            location._OMixer = this._OMixer;
            location._PMixerWH = this._PMixerWH;
            location._PosMixer = this._PosMixer;
            location._OpenMixer = this._OpenMixer;
            location._PosRf5c164 = this._PosRf5c164;
            location._OpenRf5c164 = this._OpenRf5c164;
            location._PosRf5c68 = this._PosRf5c68;
            location._OpenRf5c68 = this._OpenRf5c68;
            location._PosC140 = this._PosC140;
            location.openC140 = this.openC140;
            location._PosPPZ8 = this._PosPPZ8;
            location._OpenPPZ8 = this._OpenPPZ8;
            location.posS5B = this.posS5B;
            location._OpenS5B = this._OpenS5B;
            location._PosDMG = this._PosDMG;
            location._OpenDMG = this._OpenDMG;
            location._PosYMZ280B = this._PosYMZ280B;
            location._OpenYMZ280B = this._OpenYMZ280B;
            location._PosC352 = this._PosC352;
            location.openC352 = this.openC352;
            location._PosQSound = this._PosQSound;
            location._OpenQSound = this._OpenQSound;
            location._PosYm2151 = this._PosYm2151;
            location._OpenYm2151 = this._OpenYm2151;
            location._PosYm2608 = this._PosYm2608;
            location._OpenYm2608 = this._OpenYm2608;
            location._PosYm2203 = this._PosYm2203;
            location._OpenYm2203 = this._OpenYm2203;
            location._PosYm2610 = this._PosYm2610;
            location._OpenYm2610 = this._OpenYm2610;
            location._PosYm2612 = this._PosYm2612;
            location._OpenYm2612 = this._OpenYm2612;
            location._PosYm3526 = this._PosYm3526;
            location._OpenYm3526 = this._OpenYm3526;
            location._PosY8950 = this._PosY8950;
            location._OpenY8950 = this._OpenY8950;
            location._PosYm3812 = this._PosYm3812;
            location._OpenYm3812 = this._OpenYm3812;
            location._PosYmf262 = this._PosYmf262;
            location._OpenYmf262 = this._OpenYmf262;
            location._PosYMF271 = this._PosYMF271;
            location._OpenYMF271 = this._OpenYMF271;
            location._PosYmf278b = this._PosYmf278b;
            location._OpenYmf278b = this._OpenYmf278b;
            location._PosOKIM6258 = this._PosOKIM6258;
            location._OpenOKIM6258 = this._OpenOKIM6258;
            location._PosOKIM6295 = this._PosOKIM6295;
            location.openOKIM6295 = this.openOKIM6295;
            location._PosSN76489 = this._PosSN76489;
            location._OpenSN76489 = this._OpenSN76489;
            location._PosSegaPCM = this._PosSegaPCM;
            location._OpenSegaPCM = this._OpenSegaPCM;
            location._PosAY8910 = this._PosAY8910;
            location._OpenAY8910 = this._OpenAY8910;
            location._PosHuC6280 = this._PosHuC6280;
            location._OpenHuC6280 = this._OpenHuC6280;
            location._PosK051649 = this._PosK051649;
            location._OpenK051649 = this._OpenK051649;
            location._PosYm2612MIDI = this._PosYm2612MIDI;
            location._OpenYm2612MIDI = this._OpenYm2612MIDI;
            location._PosVSTeffectList = this._PosVSTeffectList;
            location._OpenVSTeffectList = this._OpenVSTeffectList;
            location._PosVrc7 = this._PosVrc7;
            location._OpenVrc7 = this._OpenVrc7;
            location._PosMIDI = this._PosMIDI;
            location._OpenMIDI = this._OpenMIDI;
            location._PosRegTest = this._PosRegTest;
            location._OpenRegTest = this._OpenRegTest;
            location._PosVisWave = this._PosVisWave;
            location._OpenVisWave = this._OpenVisWave;
            location._ChipSelect = this._ChipSelect;

            return location;
        }
    }

    public static class NSF implements Serializable {
        private boolean _NESUnmuteOnReset = true;
        private boolean _NESNonLinearMixer = true;
        private boolean _NESPhaseRefresh = true;
        private boolean _NESDutySwap = false;
        private int _FDSLpf = 2000;
        private boolean _FDS4085Reset = false;
        private boolean _FDSWriteDisable8000 = true;
        private boolean _DMCUnmuteOnReset = true;
        private boolean _DMCNonLinearMixer = true;
        private boolean _DMCEnable4011 = true;
        private boolean _DMCEnablePnoise = true;
        private boolean _DMCDPCMAntiClick = false;
        private boolean _DMCRandomizeNoise = true;
        private boolean _DMCTRImute = true;
        private boolean _DMCTRINull = true;
        private boolean _MMC5NonLinearMixer = true;
        private boolean _MMC5PhaseRefresh = true;
        private boolean _N160Serial = false;

        public boolean getNESUnmuteOnReset() {
            return _NESUnmuteOnReset;
        }
        public void setNESUnmuteOnReset(boolean value) {
            _NESUnmuteOnReset = value;
        }
        public boolean getNESNonLinearMixer() {
            return _NESNonLinearMixer;
        }
        public void setNESNonLinearMixer(boolean value) {
            _NESNonLinearMixer = value;
        }
        public boolean getNESPhaseRefresh() {
            return _NESPhaseRefresh;
        }
        public void setNESPhaseRefresh(boolean value) {
            _NESPhaseRefresh = value;
        }
        public boolean getNESDutySwap() {
            return _NESDutySwap;
        }
        public void setNESDutySwap(boolean value) {
            _NESDutySwap = value;
        }
        public int getFDSLpf() {
            return _FDSLpf;
        }
        public void setFDSLpf(int value) {
            _FDSLpf = value;
        }
        public boolean getFDS4085Reset() {
            return _FDS4085Reset;
        }
        public void setFDS4085Reset(boolean value) {
            _FDS4085Reset = value;
        }
        public boolean getFDSWriteDisable8000() {
            return _FDSWriteDisable8000;
        }
        public void setFDSWriteDisable8000(boolean value) {
            _FDSWriteDisable8000 = value;
        }
        public boolean getDMCUnmuteOnReset() {
            return _DMCUnmuteOnReset;
        }
        public void setDMCUnmuteOnReset(boolean value) {
            _DMCUnmuteOnReset = value;
        }
        public boolean getDMCNonLinearMixer() {
            return _DMCNonLinearMixer;
        }
        public void setDMCNonLinearMixer(boolean value) {
            _DMCNonLinearMixer = value;
        }
        public boolean getDMCEnable4011() {
            return _DMCEnable4011;
        }
        public void setDMCEnable4011(boolean value) {
            _DMCEnable4011 = value;
        }
        public boolean getDMCEnablePnoise() {
            return _DMCEnablePnoise;
        }
        public void setDMCEnablePnoise(boolean value) {
            _DMCEnablePnoise = value;
        }
        public boolean getDMCDPCMAntiClick() {
            return _DMCDPCMAntiClick;
        }
        public void setDMCDPCMAntiClick(boolean value) {
            _DMCDPCMAntiClick = value;
        }
        public boolean getDMCRandomizeNoise() {
            return _DMCRandomizeNoise;
        }
        public void setDMCRandomizeNoise(boolean value) {
            _DMCRandomizeNoise = value;
        }
        public boolean getDMCTRImute() {
            return _DMCTRImute;
        }
        public void setDMCTRImute(boolean value) {
            _DMCTRImute = value;
        }
        public boolean getDMCTRINull() {
            return _DMCTRINull;
        }
        void setDMCTRINull(boolean value) {
            _DMCTRINull = value;
        }
        public boolean getMMC5NonLinearMixer() {
            return _MMC5NonLinearMixer;
        }
        public void setMMC5NonLinearMixer(boolean value) {
            _MMC5NonLinearMixer = value;
        }
        public boolean getMMC5PhaseRefresh() {
            return _MMC5PhaseRefresh;
        }
        public void setMMC5PhaseRefresh(boolean value) {
            _MMC5PhaseRefresh = value;
        }
        public boolean getN160Serial() {
            return _N160Serial;
        }
        public void setN160Serial(boolean value) {
            _N160Serial = value;
        }
        private int _HPF = 92;
        public int getHPF() {
            return _HPF;
        }
        public void setHPF(int value) {
            _HPF = value;
        }
        private int _LPF = 112;
        public int getLPF() {
            return _LPF;
        }
        public void setLPF(int value) {
            _LPF = value;
        }
        private boolean _DMCRandomizeTRI = false;
        public boolean getDMCRandomizeTRI() {
            return _DMCRandomizeTRI;
        }
        public void setDMCRandomizeTRI(boolean value) {
            _DMCRandomizeTRI = value;
        }
        private boolean _DMCDPCMReverse = false;
        public boolean getDMCDPCMReverse() {
            return _DMCDPCMReverse;
        }
        public void setDMCDPCMReverse(boolean value) {
            _DMCDPCMReverse = value;
        }

        public NSF copy() {
            NSF nsf = new NSF();

            nsf._NESUnmuteOnReset = this._NESUnmuteOnReset;
            nsf._NESNonLinearMixer = this._NESNonLinearMixer;
            nsf._NESPhaseRefresh = this._NESPhaseRefresh;
            nsf._NESDutySwap = this._NESDutySwap;

            nsf._FDSLpf = this._FDSLpf;
            nsf._FDS4085Reset = this._FDS4085Reset;
            nsf._FDSWriteDisable8000 = this._FDSWriteDisable8000;

            nsf._DMCUnmuteOnReset = this._DMCUnmuteOnReset;
            nsf._DMCNonLinearMixer = this._DMCNonLinearMixer;
            nsf._DMCEnable4011 = this._DMCEnable4011;
            nsf._DMCEnablePnoise = this._DMCEnablePnoise;
            nsf._DMCDPCMAntiClick = this._DMCDPCMAntiClick;
            nsf._DMCRandomizeNoise = this._DMCRandomizeNoise;
            nsf._DMCTRImute = this._DMCTRImute;
            nsf._DMCRandomizeTRI = this._DMCRandomizeTRI;
            nsf._DMCDPCMReverse = this._DMCDPCMReverse;
            // nsf._DMCTRINull = this._DMCTRINull;

            nsf._MMC5NonLinearMixer = this._MMC5NonLinearMixer;
            nsf._MMC5PhaseRefresh = this._MMC5PhaseRefresh;

            nsf._N160Serial = this._N160Serial;

            nsf._HPF = this._HPF;
            nsf._LPF = this._LPF;

            return nsf;
        }
    }

    public Setting copy() {
        Setting setting = new Setting();
        setting.outputDevice = this.outputDevice.copy();

        setting.ay8910Type = null;
        if (this.ay8910Type != null) {
            setting.ay8910Type = new ChipType2[this.ay8910Type.length];
            for (int i = 0; i < this.ay8910Type.length; i++)
                setting.ay8910Type[i] = this.ay8910Type[i].copy();
        }

        setting.k051649Type = null;
        if (this.k051649Type != null) {
            setting.k051649Type = new ChipType2[this.k051649Type.length];
            for (int i = 0; i < this.k051649Type.length; i++)
                setting.k051649Type[i] = this.k051649Type[i].copy();
        }

        setting.ym2151Type = null;
        if (this.ym2151Type != null) {
            setting.ym2151Type = new ChipType2[this.ym2151Type.length];
            for (int i = 0; i < this.ym2151Type.length; i++)
                setting.ym2151Type[i] = this.ym2151Type[i].copy();
        }

        setting.ym2203Type = null;
        if (this.ym2203Type != null) {
            setting.ym2203Type = new ChipType2[this.ym2203Type.length];
            for (int i = 0; i < this.ym2203Type.length; i++)
                setting.ym2203Type[i] = this.ym2203Type[i].copy();
        }

        setting.ym2413Type = null;
        if (this.ym2413Type != null) {
            setting.ym2413Type = new ChipType2[this.ym2413Type.length];
            for (int i = 0; i < this.ym2413Type.length; i++)
                setting.ym2413Type[i] = this.ym2413Type[i].copy();
        }

        setting.ym2608Type = null;
        if (this.ym2608Type != null) {
            setting.ym2608Type = new ChipType2[this.ym2608Type.length];
            for (int i = 0; i < this.ym2608Type.length; i++)
                setting.ym2608Type[i] = this.ym2608Type[i].copy();
        }

        setting.ym2610Type = null;
        if (this.ym2610Type != null) {
            setting.ym2610Type = new ChipType2[this.ym2610Type.length];
            for (int i = 0; i < this.ym2610Type.length; i++)
                setting.ym2610Type[i] = this.ym2610Type[i].copy();
        }

        setting._YM2612Type = null;
        if (this._YM2612Type != null) {
            setting._YM2612Type = new ChipType2[this._YM2612Type.length];
            for (int i = 0; i < this._YM2612Type.length; i++)
                setting._YM2612Type[i] = this._YM2612Type[i].copy();
        }

        setting._YM3526Type = null;
        if (this._YM3526Type != null) {
            setting._YM3526Type = new ChipType2[this._YM3526Type.length];
            for (int i = 0; i < this._YM3526Type.length; i++)
                setting._YM3526Type[i] = this._YM3526Type[i].copy();
        }

        setting._YM3812Type = null;
        if (this._YM3812Type != null) {
            setting._YM3812Type = new ChipType2[this._YM3812Type.length];
            for (int i = 0; i < this._YM3812Type.length; i++)
                setting._YM3812Type[i] = this._YM3812Type[i].copy();
        }

        setting.ymf262Type = null;
        if (this.ymf262Type != null) {
            setting.ymf262Type = new ChipType2[this.ymf262Type.length];
            for (int i = 0; i < this.ymf262Type.length; i++)
                setting.ymf262Type[i] = this.ymf262Type[i].copy();
        }

        setting._SN76489Type = null;
        if (this._SN76489Type != null) {
            setting._SN76489Type = new ChipType2[this._SN76489Type.length];
            for (int i = 0; i < this._SN76489Type.length; i++)
                setting._SN76489Type[i] = this._SN76489Type[i].copy();
        }

        setting._C140Type = null;
        if (this._C140Type != null) {
            setting._C140Type = new ChipType2[this._C140Type.length];
            for (int i = 0; i < this._C140Type.length; i++)
                setting._C140Type[i] = this._C140Type[i].copy();
        }

        setting._SEGAPCMType = null;
        if (this._SEGAPCMType != null) {
            setting._SEGAPCMType = new ChipType2[this._SEGAPCMType.length];
            for (int i = 0; i < this._SEGAPCMType.length; i++)
                setting._SEGAPCMType[i] = this._SEGAPCMType[i].copy();
        }

        setting.unuseRealChip = this.unuseRealChip;
        setting.fileSearchPathList = this.fileSearchPathList;

        //setting._YM2151SType = this._YM2151SType.Copy();
        //setting._YM2203SType = this._YM2203SType.Copy();
        //setting._YM2413SType = this._YM2413SType.Copy();
        //setting._AY8910SType = this._AY8910SType.Copy();
        //setting._YM2608SType = this._YM2608SType.Copy();
        //setting._YM2610SType = this._YM2610SType.Copy();
        //setting._YM2612SType = this._YM2612SType.Copy();
        //setting._YM3526SType = this._YM3526SType.Copy();
        //setting._YM3812SType = this._YM3812SType.Copy();
        //setting._YMF262SType = this._YMF262SType.Copy();
        //setting._SN76489SType = this._SN76489SType.Copy();
        //setting._C140SType = this._C140SType.Copy();
        //setting._SEGAPCMSType = this._SEGAPCMSType.Copy();

        setting.other = this.other.copy();
        setting.balance = this.balance.copy();
        setting.latencyEmulation = this.latencyEmulation;
        setting.latencySCCI = this.latencySCCI;
        setting.debugDispFrameCounter = this.debugDispFrameCounter;
        setting.hiyorimiMode = this.hiyorimiMode;
        setting.location = this.location.Copy();
        setting.midiExport = this.midiExport.copy();
        setting.midiKbd = this.midiKbd.copy();
//        setting.vst = this.vst.copy();
        setting.midiOut = this.midiOut.copy();
        setting.nsf = this.nsf.copy();
        setting.sid = this.sid.copy();
        setting.nukedOPN2 = this.nukedOPN2.Copy();
        setting.autoBalance = this.autoBalance.copy();
        setting.pmdDotNET = this.pmdDotNET.copy();

        setting._keyBoardHook = this._keyBoardHook.copy();

        return setting;
    }

    public void save() {
        String fullPath = Common.settingFilePath;
        fullPath = Path.combine(fullPath, Resources.getCntSettingFileName());

        try (OutputStream sw = Files.newOutputStream(Paths.get(fullPath))) {
            Serdes.Util.serialize(sw, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Setting load() {
        try {
            String fn = Resources.getCntSettingFileName();
            if (File.exists(Path.getDirectoryName(System.getProperty("user.dir")) + fn)) {
                // アプリケーションと同じフォルダに設定ファイルがあるならそちらを使用する
                Common.settingFilePath = Path.getDirectoryName(System.getProperty("user.dir"));
            } else {
                // 上記以外は、アプリケーション向けデータフォルダを使用する
                Common.settingFilePath = Common.getApplicationDataFolder(true);
            }

            String fullPath = Common.settingFilePath;
            fullPath = Path.combine(fullPath, Resources.getCntSettingFileName());

            if (!File.exists(fullPath)) {
                return new Setting();
            }
            try (InputStream sr = Files.newInputStream(Paths.get(fullPath))) {
                return Serdes.Util.deserialize(sr, new Setting());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Setting();
        }
    }
}
