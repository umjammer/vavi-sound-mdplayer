
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

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.Common.EnmInstFormat;
import mdplayer.properties.Resources;
import mdplayer.vst.VstInfo;
import vavi.util.serdes.Serdes;


public class Setting implements Serializable {

    public static class ChipType2 implements Serializable {
        private Boolean[] _UseEmu = null;

        public Boolean[] getUseEmu() {
            return _UseEmu;
        }

        public void setUseEmu(Boolean[] value) {
            _UseEmu = value;
        }

        private Boolean[] _UseReal = null;

        public Boolean[] getUseReal() {
            return _UseReal;
        }

        public void setUseReal(Boolean[] value) {
            _UseReal = value;
        }

        private RealChipInfo[] _realChipInfo = null;

        public RealChipInfo[] getRealChipInfo() {
            return _realChipInfo;
        }

        public void setrealChipInfo(RealChipInfo[] value) {
            _realChipInfo = value;
        }

        public static class RealChipInfo {
            // Chip共通 識別情報

            private int _InterfaceType = -1;

            public int getInterfaceType() {
                return _InterfaceType;
            }

            void setInterfaceType(int value) {
                _InterfaceType = value;
            }

            private int _SoundLocation = -1;

            public int getSoundLocation() {
                return _SoundLocation;
            }

            public void setSoundLocation(int value) {
                _SoundLocation = value;
            }

            private int _BusID = -1;

            public int getBusID() {
                return _BusID;
            }

            public void setBusID(int value) {
                _BusID = value;
            }

            private int _SoundChip = -1;

            public int getSoundChip() {
                return _SoundChip;
            }

            public void setSoundChip(int value) {
                _SoundChip = value;
            }

            private int _ChipType = 0;

            public int getChipType() {
                return _ChipType;
            }

            void setChipType(int value) {
                _ChipType = value;
            }

            private String _InterfaceName = "";

            public String getInterfaceName() {
                return _InterfaceName;
            }

            public void setInterfaceName(String value) {
                _InterfaceName = value;
            }

            private String _ChipName = "";

            public String getChipName() {
                return _ChipName;
            }

            void setChipName(String value) {
                _ChipName = value;
            }

            // Chip固有の追加設定

            // ウエイトコマンドをSCCIに送るか
            private Boolean _UseWait = true;

            public Boolean getUseWait() {
                return _UseWait;
            }

            public void setUseWait(Boolean value) {
                _UseWait = value;
            }

            // ウエイトコマンドを2倍にするか
            private Boolean _UseWaitBoost = false;

            public Boolean getUseWaitBoost() {
                return _UseWaitBoost;
            }

            public void setUseWaitBoost(Boolean value) {
                _UseWaitBoost = value;
            }

            // PCMのみエミュレーションするか
            private Boolean _OnlyPCMEmulation = false;

            public Boolean getOnlyPCMEmulation() {
                return _OnlyPCMEmulation;
            }

            public void setOnlyPCMEmulation(Boolean value) {
                _OnlyPCMEmulation = value;
            }

            public RealChipInfo copy() {
                RealChipInfo ret = new RealChipInfo();

                ret._InterfaceType = this._InterfaceType;
                ret._SoundLocation = this._SoundLocation;
                ret._BusID = this._BusID;
                ret._SoundChip = this._SoundChip;
                ret._ChipType = this._ChipType;
                ret._InterfaceName = this._InterfaceName;
                ret._ChipName = this._ChipName;

                ret._UseWait = this._UseWait;
                ret._UseWaitBoost = this._UseWaitBoost;
                ret._OnlyPCMEmulation = this._OnlyPCMEmulation;

                return ret;
            }
        }

        // Emulation時の遅延時間
        private int _LatencyForEmulation = 0;

        public int getLatencyForEmulation() {
            return _LatencyForEmulation;
        }

        void setLatencyForEmulation(int value) {
            _LatencyForEmulation = value;
        }

        private int _LatencyForReal = 0;

        public int getLatencyForReal() {
            return _LatencyForReal;
        }

        void setLatencyForReal(int value) {
            _LatencyForReal = value;
        }

        public ChipType2 copy() {
            ChipType2 ct = new ChipType2();

            ct._UseEmu = null;
            if (this._UseEmu != null) {
                ct._UseEmu = new Boolean[this._UseEmu.length];
                for (int i = 0; i < this._UseEmu.length; i++)
                    ct._UseEmu[i] = this._UseEmu[i];
            }

            ct._UseReal = null;
            if (this._UseReal != null) {
                ct._UseReal = new Boolean[this._UseReal.length];
                for (int i = 0; i < this._UseReal.length; i++)
                    ct._UseReal[i] = this._UseReal[i];
            }

            ct._realChipInfo = null;
            if (this._realChipInfo != null) {
                ct._realChipInfo = new RealChipInfo[this._realChipInfo.length];
                for (int i = 0; i < this._realChipInfo.length; i++)
                    if (this._realChipInfo[i] != null)
                        ct._realChipInfo[i] = this._realChipInfo[i].copy();
            }

            ct._LatencyForEmulation = this._LatencyForEmulation;
            ct._LatencyForReal = this._LatencyForReal;

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
        public Boolean c64modelForce = false;
        public int sidModel = 0;
        public Boolean sidmodelForce = false;

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
        public int EmuType = 0;

        // ごめんGensのオプションもここ。。。
        public Boolean GensDACHPF = true;

        public Boolean GensSSGEG = true;

        public NukedOPN2 Copy() {
            NukedOPN2 no = new NukedOPN2();
            no.EmuType = this.EmuType;
            no.GensDACHPF = this.GensDACHPF;
            no.GensSSGEG = this.GensSSGEG;

            return no;
        }
    }

    public static class AutoBalance implements Serializable {
        private Boolean _UseThis = false;
        private Boolean _LoadSongBalance = false;
        private Boolean _LoadDriverBalance = false;
        private Boolean _SaveSongBalance = false;
        private Boolean _SamePositionAsSongData = false;

        public Boolean getUseThis() {
            return _UseThis;
        }

        public void setUseThis(Boolean value) {
            _UseThis = value;
        }

        public Boolean getLoadSongBalance() {
            return _LoadSongBalance;
        }

        public void setLoadSongBalance(Boolean value) {
            _LoadSongBalance = value;
        }

        public Boolean getLoadDriverBalance() {
            return _LoadDriverBalance;
        }

        public void setLoadDriverBalance(Boolean value) {
            _LoadDriverBalance = value;
        }

        public Boolean getSaveSongBalance() {
            return _SaveSongBalance;
        }

        public void setSaveSongBalance(Boolean value) {
            _SaveSongBalance = value;
        }

        public Boolean getSamePositionAsSongData() {
            return _SamePositionAsSongData;
        }

        public void setSamePositionAsSongData(Boolean value) {
            _SamePositionAsSongData = value;
        }

        public AutoBalance copy() {
            AutoBalance AutoBalance = new AutoBalance();
            AutoBalance._UseThis = this._UseThis;
            AutoBalance._LoadSongBalance = this._LoadSongBalance;
            AutoBalance._LoadDriverBalance = this._LoadDriverBalance;
            AutoBalance._SaveSongBalance = this._SaveSongBalance;
            AutoBalance._SamePositionAsSongData = this._SamePositionAsSongData;

            return AutoBalance;
        }
    }

    public static class PMDDotNET implements Serializable {
        public String compilerArguments = "/v /C";
        public Boolean isAuto = true;
        public int soundBoard = 1;
        public Boolean usePPSDRV = true;
        public Boolean usePPZ8 = true;
        public String driverArguments = "";
        public Boolean setManualVolume = false;
        public Boolean usePPSDRVUseInterfaceDefaultFreq = true;
        public int PPSDRVManualFreq = 2000;
        public int PPSDRVManualWait = 1;
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
            p.PPSDRVManualFreq = this.PPSDRVManualFreq;
            p.PPSDRVManualWait = this.PPSDRVManualWait;
            p.volumeFM = this.volumeFM;
            p.volumeSSG = this.volumeSSG;
            p.volumeRhythm = this.volumeRhythm;
            p.volumeAdpcm = this.volumeAdpcm;
            p.volumeGIMICSSG = this.volumeGIMICSSG;

            return p;
        }
    }

    public static class MidiExport implements Serializable {

        private Boolean _UseMIDIExport = false;

        public Boolean getUseMIDIExport() {
            return _UseMIDIExport;
        }

        public void setUseMIDIExport(Boolean value) {
            _UseMIDIExport = value;
        }

        private Boolean _UseYM2151Export = false;

        public Boolean getUseYM2151Export() {
            return _UseYM2151Export;
        }

        public void setUseYM2151Export(Boolean value) {
            _UseYM2151Export = value;
        }

        private Boolean _UseYM2612Export = true;

        public Boolean getUseYM2612Export() {
            return _UseYM2612Export;
        }

        public void setUseYM2612Export(Boolean value) {
            _UseYM2612Export = value;
        }

        private String _ExportPath = "";

        public String getExportPath() {
            return _ExportPath;
        }

        public void setExportPath(String value) {
            _ExportPath = value;
        }

        private Boolean _UseVOPMex = false;

        public Boolean getUseVOPMex() {
            return _UseVOPMex;
        }

        public void setUseVOPMex(Boolean value) {
            _UseVOPMex = value;
        }

        private Boolean _KeyOnFnum = false;

        public Boolean getKeyOnFnum() {
            return _KeyOnFnum;
        }

        public void setKeyOnFnum(Boolean value) {
            _KeyOnFnum = value;
        }

        public MidiExport copy() {
            MidiExport MidiExport = new MidiExport();

            MidiExport._UseMIDIExport = this._UseMIDIExport;
            MidiExport._UseYM2151Export = this._UseYM2151Export;
            MidiExport._UseYM2612Export = this._UseYM2612Export;
            MidiExport._ExportPath = this._ExportPath;
            MidiExport._UseVOPMex = this._UseVOPMex;
            MidiExport._KeyOnFnum = this._KeyOnFnum;

            return MidiExport;
        }
    }

    public static class MidiKbd implements Serializable {

        private Boolean _UseMIDIKeyboard = false;
        public Boolean getUseMIDIKeyboard() {
            return _UseMIDIKeyboard;
        }
        public void setUseMIDIKeyboard(Boolean value) {
            _UseMIDIKeyboard = value;
        }
        private String _MidiInDeviceName = "";
        public String getMidiInDeviceName() {
            return _MidiInDeviceName;
        }
        public void setMidiInDeviceName(String value) {
            _MidiInDeviceName = value;
        }
        private Boolean _IsMONO = true;
        public Boolean getIsMONO() {
            return _IsMONO;
        }
        public void setIsMONO(Boolean value) {
            _IsMONO = value;
        }
        private int _useFormat = 0;
        public int getUseFormat() {
            return _useFormat;
        }
        public void setUseFormat(int value) {
            _useFormat = value;
        }
        private int _UseMONOChannel = 0;
        public int getUseMONOChannel() {
            return _UseMONOChannel;
        }
        public void setUseMONOChannel(int value) {
            _UseMONOChannel = value;
        }
        private Boolean[] _UseChannel = new Boolean[9];
        public Boolean[] getUseChannel() {
            return _UseChannel;
        }
        void setUseChannel(Boolean[] value) {
            _UseChannel = value;
        }
        private Tone[] _Tones = new Tone[6];
        public Tone[] getTones() {
            return _Tones;
        }
        void setTones(Tone[] value) {
            _Tones = value;
        }
        private int _MidiCtrl_CopyToneFromYM2612Ch1 = 97;
        public int getMidiCtrl_CopyToneFromYM2612Ch1() {
            return _MidiCtrl_CopyToneFromYM2612Ch1;
        }
        public void setMidiCtrl_CopyToneFromYM2612Ch1(int value) {
            _MidiCtrl_CopyToneFromYM2612Ch1 = value;
        }
        private int _MidiCtrl_DelOneLog = 96;
        public int getMidiCtrl_DelOneLog() {
            return _MidiCtrl_DelOneLog;
        }
        public void setMidiCtrl_DelOneLog(int value) {
            _MidiCtrl_DelOneLog = value;
        }
        private int _MidiCtrl_CopySelecttingLogToClipbrd = 66;
        public int getMidiCtrl_CopySelecttingLogToClipbrd() {
            return _MidiCtrl_CopySelecttingLogToClipbrd;
        }
        public void setMidiCtrl_CopySelecttingLogToClipbrd(int value) {
            _MidiCtrl_CopySelecttingLogToClipbrd = value;
        }
        private int _MidiCtrl_Stop = -1;
        public int getMidiCtrl_Stop() {
            return _MidiCtrl_Stop;
        }
        public void setMidiCtrl_Stop(int value) {
            _MidiCtrl_Stop = value;
        }
        private int _MidiCtrl_Pause = -1;
        public int getMidiCtrl_Pause() {
            return _MidiCtrl_Pause;
        }
        public void setMidiCtrl_Pause(int value) {
            _MidiCtrl_Pause = value;
        }
        private int _MidiCtrl_Fadeout = -1;
        public int getMidiCtrl_Fadeout() {
            return _MidiCtrl_Fadeout;
        }
        public void setMidiCtrl_Fadeout(int value) {
            _MidiCtrl_Fadeout = value;
        }
        private int _MidiCtrl_Previous = -1;
        public int getMidiCtrl_Previous() {
            return _MidiCtrl_Previous;
        }
        public void setMidiCtrl_Previous(int value) {
            _MidiCtrl_Previous = value;
        }
        private int _MidiCtrl_Slow = -1;
        public int getMidiCtrl_Slow() {
            return _MidiCtrl_Slow;
        }
        public void setMidiCtrl_Slow(int value) {
            _MidiCtrl_Slow = value;
        }
        private int _MidiCtrl_Play = -1;
        public int getMidiCtrl_Play() {
            return _MidiCtrl_Play;
        }
        public void setMidiCtrl_Play(int value) {
            _MidiCtrl_Play = value;
        }
        private int _MidiCtrl_Fast = -1;
        public int getMidiCtrl_Fast() {
            return _MidiCtrl_Fast;
        }
        public void setMidiCtrl_Fast(int value) {
            _MidiCtrl_Fast = value;
        }
        private int _MidiCtrl_Next = -1;
        public int getMidiCtrl_Next() {
            return _MidiCtrl_Next;
        }
        public void setMidiCtrl_Next(int value) {
            _MidiCtrl_Next = value;
        }

        public MidiKbd copy() {
            MidiKbd midiKbd = new MidiKbd();

            midiKbd._MidiInDeviceName = this._MidiInDeviceName;
            midiKbd._UseMIDIKeyboard = this._UseMIDIKeyboard;
            System.arraycopy(this._UseChannel, 0, midiKbd._UseChannel, 0, midiKbd._UseChannel.length);
            midiKbd._IsMONO = this._IsMONO;
            midiKbd._UseMONOChannel = this._UseMONOChannel;

            midiKbd._MidiCtrl_CopySelecttingLogToClipbrd = this._MidiCtrl_CopySelecttingLogToClipbrd;
            midiKbd._MidiCtrl_CopyToneFromYM2612Ch1 = this._MidiCtrl_CopyToneFromYM2612Ch1;
            midiKbd._MidiCtrl_DelOneLog = this._MidiCtrl_DelOneLog;
            midiKbd._MidiCtrl_Fadeout = this._MidiCtrl_Fadeout;
            midiKbd._MidiCtrl_Fast = this._MidiCtrl_Fast;
            midiKbd._MidiCtrl_Next = this._MidiCtrl_Next;
            midiKbd._MidiCtrl_Pause = this._MidiCtrl_Pause;
            midiKbd._MidiCtrl_Play = this._MidiCtrl_Play;
            midiKbd._MidiCtrl_Previous = this._MidiCtrl_Previous;
            midiKbd._MidiCtrl_Slow = this._MidiCtrl_Slow;
            midiKbd._MidiCtrl_Stop = this._MidiCtrl_Stop;

            return midiKbd;
        }
    }

    public static class KeyBoardHook implements Serializable {
        public static class HookKeyInfo implements Serializable {
            private Boolean _Shift = false;
            private Boolean _Ctrl = false;
            private Boolean _Win = false;
            private Boolean _Alt = false;
            private String _Key = "(None)";

            public Boolean getShift() {
                return _Shift;
            }
            public void setShift(Boolean value) {
                _Shift = value;
            }
            public Boolean getCtrl() {
                return _Ctrl;
            }
            public void setCtrl(Boolean value) {
                _Ctrl = value;
            }
            public Boolean getWin() {
                return _Win;
            }
            public void setWin(Boolean value) {
                _Win = value;
            }
            public Boolean getAlt() {
                return _Alt;
            }
            public void setAlt(Boolean value) {
                _Alt = value;
            }
            public String getKey() {
                return _Key;
            }
            public void setKey(String value) {
                _Key = value;
            }

            public HookKeyInfo copy() {
                HookKeyInfo hookKeyInfo = new HookKeyInfo();
                hookKeyInfo._Shift = this._Shift;
                hookKeyInfo._Ctrl = this._Ctrl;
                hookKeyInfo._Win = this._Win;
                hookKeyInfo._Alt = this._Alt;
                hookKeyInfo._Key = this._Key;

                return hookKeyInfo;
            }
        }

        private Boolean _UseKeyBoardHook = false;
        public Boolean getUseKeyBoardHook() {
            return _UseKeyBoardHook;
        }
        public void setUseKeyBoardHook(Boolean value) {
            _UseKeyBoardHook = value;
        }
        public HookKeyInfo getStop() {
            return _Stop;
        }
        void setStop(HookKeyInfo value) {
            _Stop = value;
        }
        public HookKeyInfo getPause() {
            return _Pause;
        }
        void setPause(HookKeyInfo value) {
            _Pause = value;
        }
        public HookKeyInfo getFadeout() {
            return _Fadeout;
        }
        void setFadeout(HookKeyInfo value) {
            _Fadeout = value;
        }
        public HookKeyInfo getPrev() {
            return _Prev;
        }
        void setPrev(HookKeyInfo value) {
            _Prev = value;
        }
        public HookKeyInfo getSlow() {
            return _Slow;
        }
        void setSlow(HookKeyInfo value) {
            _Slow = value;
        }
        public HookKeyInfo getPlay() {
            return _Play;
        }
        void setPlay(HookKeyInfo value) {
            _Play = value;
        }
        public HookKeyInfo getNext() {
            return _Next;
        }
        void setNext(HookKeyInfo value) {
            _Next = value;
        }
        public HookKeyInfo getFast() {
            return _Fast;
        }
        void setFast(HookKeyInfo value) {
            _Fast = value;
        }

        private HookKeyInfo _Stop = new HookKeyInfo();
        private HookKeyInfo _Pause = new HookKeyInfo();
        private HookKeyInfo _Fadeout = new HookKeyInfo();
        private HookKeyInfo _Prev = new HookKeyInfo();
        private HookKeyInfo _Slow = new HookKeyInfo();
        private HookKeyInfo _Play = new HookKeyInfo();
        private HookKeyInfo _Next = new HookKeyInfo();
        private HookKeyInfo _Fast = new HookKeyInfo();

        public KeyBoardHook copy() {
            KeyBoardHook keyBoard = new KeyBoardHook();
            keyBoard._UseKeyBoardHook = this._UseKeyBoardHook;
            keyBoard._Stop = this._Stop.copy();
            keyBoard._Pause = this._Pause.copy();
            keyBoard._Fadeout = this._Fadeout.copy();
            keyBoard._Prev = this._Prev.copy();
            keyBoard._Slow = this._Slow.copy();
            keyBoard._Play = this._Play.copy();
            keyBoard._Next = this._Next.copy();
            keyBoard._Fast = this._Fast.copy();

            return keyBoard;
        }
    }

    public static class Vst implements Serializable {

        private String _DefaultPath = "";
        private String[] _VSTPluginPath = null;
        public String[] getVSTPluginPath() {
            return _VSTPluginPath;
        }
        void setVSTPluginPath(String[] value) {
            _VSTPluginPath = value;
        }
        private VstInfo[] _VSTInfo = null;
        public VstInfo[] getVSTInfo() {
            return _VSTInfo;
        }
        public void setVSTInfo(VstInfo[] value) {
            _VSTInfo = value;
        }
        public String getDefaultPath() {
            return _DefaultPath;
        }
        public void setDefaultPath(String value) {
            _DefaultPath = value;
        }

        public Vst copy() {
            Vst vst = new Vst();

            vst._VSTInfo = this._VSTInfo;
            vst._DefaultPath = this._DefaultPath;

            return vst;
        }
    }

    public static class MidiOut implements Serializable {

        private String _GMReset = "30:F0,7E,7F,09,01,F7";
        public String getGMReset() {
            return _GMReset;
        }
        public void setGMReset(String value) {
            _GMReset = value;
        }
        private String _XGReset = "30:F0,43,10,4C,00,00,7E,00,F7";
        public String getXGReset() {
            return _XGReset;
        }
        public void setXGReset(String value) {
            _XGReset = value;
        }
        private String _GSReset = "30:F0,41,10,42,12,40,00,7F,00,41,F7";
        public String getGSReset() {
            return _GSReset;
        }
        public void setGSReset(String value) {
            _GSReset = value;
        }
        private String _Custom = "";
        public String getCustom() {
            return _Custom;
        }
        public void setCustom(String value) {
            _Custom = value;
        }
        private List<MidiOutInfo[]> _lstMidiOutInfo = null;
        public List<MidiOutInfo[]> getlstMidiOutInfo() {
            return _lstMidiOutInfo;
        }
        public void setlstMidiOutInfo(List<MidiOutInfo[]> value) {
            _lstMidiOutInfo = value;
        }

        public MidiOut copy() {
            MidiOut MidiOut = new MidiOut();

            MidiOut._GMReset = this._GMReset;
            MidiOut._XGReset = this._XGReset;
            MidiOut._GSReset = this._GSReset;
            MidiOut._Custom = this._Custom;
            MidiOut._lstMidiOutInfo = this._lstMidiOutInfo;

            return MidiOut;
        }
    }

    public static final Point EmptyPoint = new Point(0, 0);
    public static final Dimension EmptyDimension = new Dimension(0, 0);

    public Setting() {
    }

    // 多音源対応
    private String _FileSearchPathList;
    public String getFileSearchPathList() {
        return _FileSearchPathList;
    }
    public void setFileSearchPathList(String value) {
        _FileSearchPathList = value;
    }
    private OutputDevice _outputDevice = new OutputDevice();
    public OutputDevice getOutputDevice() {
        return _outputDevice;
    }
    void setOutputDevice(OutputDevice value) {
        _outputDevice = value;
    }
    private ChipType2[] _AY8910Type = null;
    public ChipType2[] getAY8910Type() {
        return _AY8910Type;
    }
    public void setAY8910Type(ChipType2[] value) {
        _AY8910Type = value;
    }

    // private ChipType2[] _AY8910SType = new ChipType2();
    // public ChipType2[] AY8910SType {
    // get() {
    // return _AY8910SType;
    // }

    // set {
    // _AY8910SType = value;
    // }
    // }

    private ChipType2[] _YM2151Type = null;
    public ChipType2[] getYM2151Type() {
        return _YM2151Type;
    }
    public void setYM2151Type(ChipType2[] value) {
        _YM2151Type = value;
    }
    private ChipType2[] _YM2203Type = null;
    public ChipType2[] getYM2203Type() {
        return _YM2203Type;
    }
    public void setYM2203Type(ChipType2[] value) {
        _YM2203Type = value;
    }
    private ChipType2[] _YM2413Type = null;
    public ChipType2[] getYM2413Type() {
        return _YM2413Type;
    }
    public void setYM2413Type(ChipType2[] value) {
        _YM2413Type = value;
    }
    private ChipType2[] _HuC6280Type = null;
    public ChipType2[] getHuC6280Type() {
        return _HuC6280Type;
    }
    void setHuC6280Type(ChipType2[] value) {
        _HuC6280Type = value;
    }
    private ChipType2[] _K051649Type = null;
    public ChipType2[] getK051649Type() {
        return _K051649Type;
    }
    public void setK051649Type(ChipType2[] value) {
        _K051649Type = value;
    }

    // private ChipType2[] _YM2413SType = null;
    // public ChipType2[] YM2413SType
    // {
    // get()
    // {
    // return _YM2413SType;
    // }

    // set
    // {
    // _YM2413SType = value;
    // }
    // }

    private ChipType2[] _YM2608Type = null;
    public ChipType2[] getYM2608Type() {
        return _YM2608Type;
    }
    public void setYM2608Type(ChipType2[] value) {
        _YM2608Type = value;
    }
    private ChipType2[] _YM2610Type = null;
    public ChipType2[] getYM2610Type() {
        return _YM2610Type;
    }
    public void setYM2610Type(ChipType2[] value) {
        _YM2610Type = value;
    }
    private ChipType2[] _YMF262Type = null;
    public ChipType2[] getYMF262Type() {
        return _YMF262Type;
    }
    public void setYMF262Type(ChipType2[] value) {
        _YMF262Type = value;
    }
    private ChipType2[] _YMF271Type = null;
    public ChipType2[] getYMF271Type() {
        return _YMF271Type;
    }
    void setYMF271Type(ChipType2[] value) {
        _YMF271Type = value;
    }
    private ChipType2[] _YMF278BType = null;
    public ChipType2[] getYMF278BType() {
        return _YMF278BType;
    }
    void setYMF278BType(ChipType2[] value) {
        _YMF278BType = value;
    }
    private ChipType2[] _YMZ280BType = null;
    public ChipType2[] getYMZ280BType() {
        return _YMZ280BType;
    }
    void setYMZ280BType(ChipType2[] value) {
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
    // {
    // get()
    // {
    // return _YM2151SType;
    // }

    // set
    // {
    // _YM2151SType = value;
    // }
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
    void setY8950Type(ChipType2[] value) {
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

    private int _LatencyEmulation = 0;
    public int getLatencyEmulation() {
        return _LatencyEmulation;
    }
    public void setLatencyEmulation(int value) {
        _LatencyEmulation = value;
    }
    private int _LatencySCCI = 0;
    public int getLatencySCCI() {
        return _LatencySCCI;
    }
    public void setLatencySCCI(int value) {
        _LatencySCCI = value;
    }
    private Boolean _HiyorimiMode = true;
    public Boolean getHiyorimiMode() {
        return _HiyorimiMode;
    }
    public void setHiyorimiMode(Boolean value) {
        _HiyorimiMode = value;
    }
    private Boolean _Debug_DispFrameCounter = false;
    public Boolean getDebug_DispFrameCounter() {
        return _Debug_DispFrameCounter;
    }
    public void setDebug_DispFrameCounter(Boolean value) {
        _Debug_DispFrameCounter = value;
    }
    private int _Debug_SCCbaseAddress = 0x9800;
    public int getDebug_SCCbaseAddress() {
        return _Debug_SCCbaseAddress;
    }
    public void setDebug_SCCbaseAddress(int value) {
        _Debug_SCCbaseAddress = value;
    }
    private Other _other = new Other();
    public Other getOther() {
        return _other;
    }
    void setother(Other value) {
        _other = value;
    }
    private Balance _balance = new Balance();
    public Balance getbalance() {
        return _balance;
    }
    void setbalance(Balance value) {
        _balance = value;
    }
    private Location _location = new Location();
    public Location getLocation() {
        return _location;
    }
    public void setlocation(Location value) {
        _location = value;
    }
    private MidiExport _midiExport = new MidiExport();
    public MidiExport getMidiExport() {
        return _midiExport;
    }
    void setmidiExport(MidiExport value) {
        _midiExport = value;
    }
    private MidiKbd _midiKbd = new MidiKbd();
    public MidiKbd getMidiKbd() {
        return _midiKbd;
    }
    void setmidiKbd(MidiKbd value) {
        _midiKbd = value;
    }
    private Vst _vst = new Vst();
    public Vst getvst() {
        return _vst;
    }
    void setvst(Vst value) {
        _vst = value;
    }
    private MidiOut _midiOut = new MidiOut();
    public MidiOut getMidiOut() {
        return _midiOut;
    }
    void setmidiOut(MidiOut value) {
        _midiOut = value;
    }
    private NSF _nsf = new NSF();
    public NSF getNsf() {
        return _nsf;
    }
    void setnsf(NSF value) {
        _nsf = value;
    }
    private SID _sid = new SID();
    public SID getSid() {
        return _sid;
    }
    public void setsid(SID value) {
        _sid = value;
    }

    private NukedOPN2 _NukedOPN2 = new NukedOPN2();

    public NukedOPN2 getNukedOPN2() {
        return _NukedOPN2;
    }

    public void setnukedOPN2(NukedOPN2 value) {
        _NukedOPN2 = value;
    }

    private AutoBalance _autoBalance = new AutoBalance();

    public AutoBalance getAutoBalance() {
        return _autoBalance;
    }

    public void setautoBalance(AutoBalance value) {
        _autoBalance = value;
    }

    private PMDDotNET _PMDDotNET = new PMDDotNET();

    public PMDDotNET getPmdDotNET() {
        return _PMDDotNET;
    }

    void setPMDDotNET(PMDDotNET value) {
        _PMDDotNET = value;
    }

    public KeyBoardHook getkeyBoardHook() {
        return _keyBoardHook;
    }

    void setkeyBoardHook(KeyBoardHook value) {
        _keyBoardHook = value;
    }

    private Boolean _unuseRealChip;

    public Boolean getunuseRealChip() {
        return _unuseRealChip;
    }

    public void setunuseRealChip(Boolean value) {
        _unuseRealChip = value;
    }

    private KeyBoardHook _keyBoardHook = new KeyBoardHook();

    public static class OutputDevice implements Serializable {

        private int _DeviceType = 0;
        public int getDeviceType() {
            return _DeviceType;
        }
        public void setDeviceType(int value) {
            _DeviceType = value;
        }
        private int _Latency = 300;
        public int getLatency() {
            return _Latency;
        }
        public void setLatency(int value) {
            _Latency = value;
        }
        private int _WaitTime = 500;
        public int getWaitTime() {
            return _WaitTime;
        }
        public void setWaitTime(int value) {
            _WaitTime = value;
        }
        private String _WaveOutDeviceName = "";
        public String getWaveOutDeviceName() {
            return _WaveOutDeviceName;
        }
        public void setWaveOutDeviceName(String value) {
            _WaveOutDeviceName = value;
        }
        private String _DirectSoundDeviceName = "";
        public String getDirectSoundDeviceName() {
            return _DirectSoundDeviceName;
        }
        public void setDirectSoundDeviceName(String value) {
            _DirectSoundDeviceName = value;
        }
        private String _WasapiDeviceName = "";
        public String getWasapiDeviceName() {
            return _WasapiDeviceName;
        }
        public void setWasapiDeviceName(String value) {
            _WasapiDeviceName = value;
        }
        private Boolean _WasapiShareMode = true;
        public Boolean getWasapiShareMode() {
            return _WasapiShareMode;
        }
        public void setWasapiShareMode(Boolean value) {
            _WasapiShareMode = value;
        }
        private String _AsioDeviceName = "";
        public String getAsioDeviceName() {
            return _AsioDeviceName;
        }
        public void setAsioDeviceName(String value) {
            _AsioDeviceName = value;
        }
        private int _SampleRate = 44100;
        public int getSampleRate() {
            return _SampleRate;
        }
        public void setSampleRate(int value) {
            _SampleRate = value;
        }

        public OutputDevice copy() {
            OutputDevice outputDevice = new OutputDevice();
            outputDevice._DeviceType = this._DeviceType;
            outputDevice._Latency = this._Latency;
            outputDevice._WaitTime = this._WaitTime;
            outputDevice._WaveOutDeviceName = this._WaveOutDeviceName;
            outputDevice._DirectSoundDeviceName = this._DirectSoundDeviceName;
            outputDevice._WasapiDeviceName = this._WasapiDeviceName;
            outputDevice._WasapiShareMode = this._WasapiShareMode;
            outputDevice._AsioDeviceName = this._AsioDeviceName;
            outputDevice._SampleRate = this._SampleRate;

            return outputDevice;
        }
    }
    // implements Serializable;
    // public class ChipType2
    // {
    // private Boolean _UseEmu = true;
    // public Boolean UseEmu
    // {
    // get()
    // {
    // return _UseEmu;
    // }

    // set
    // {
    // _UseEmu = value;
    // }
    // }

    // private Boolean _UseEmu2 = false;
    // public Boolean UseEmu2
    // {
    // get()
    // {
    // return _UseEmu2;
    // }

    // set
    // {
    // _UseEmu2 = value;
    // }
    // }

    // private Boolean _UseEmu3 = false;
    // public Boolean UseEmu3
    // {
    // get()
    // {
    // return _UseEmu3;
    // }

    // set
    // {
    // _UseEmu3 = value;
    // }
    // }

    // private Boolean _UseScci = false;
    // public Boolean UseScci
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

    // private Boolean _UseScci2 = false;
    // public Boolean UseScci2
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

    // private Boolean _UseWait = true;
    // public Boolean UseWait
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

    // private Boolean _UseWaitBoost = false;
    // public Boolean UseWaitBoost
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

    // private Boolean _OnlyPCMEmulation = false;
    // public Boolean OnlyPCMEmulation
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
        private Boolean _UseLoopTimes = true;
        public Boolean getUseLoopTimes() {
            return _UseLoopTimes;
        }
        public void setUseLoopTimes(Boolean value) {
            _UseLoopTimes = value;
        }
        private int _LoopTimes = 2;
        public int getLoopTimes() {
            return _LoopTimes;
        }
        public void setLoopTimes(int value) {
            _LoopTimes = value;
        }
        private Boolean _UseGetInst = true;
        public Boolean getUseGetInst() {
            return _UseGetInst;
        }
        public void setUseGetInst(Boolean value) {
            _UseGetInst = value;
        }
        private String _DefaultDataPath = "";
        public String getDefaultDataPath() {
            return _DefaultDataPath;
        }
        public void setDefaultDataPath(String value) {
            _DefaultDataPath = value;
        }
        private EnmInstFormat _InstFormat = EnmInstFormat.MML2VGM;
        public EnmInstFormat getInstFormat() {
            return _InstFormat;
        }
        public void setInstFormat(EnmInstFormat value) {
            _InstFormat = value;
        }
        private int _Zoom = 1;
        public int getZoom() {
            return _Zoom;
        }
        public void setZoom(int value) {
            _Zoom = value;
        }
        private int _ScreenFrameRate = 60;
        public int getScreenFrameRate() {
            return _ScreenFrameRate;
        }
        public void setScreenFrameRate(int value) {
            _ScreenFrameRate = value;
        }
        private Boolean _AutoOpen = false;
        public Boolean getAutoOpen() {
            return _AutoOpen;
        }
        public void setAutoOpen(Boolean value) {
            _AutoOpen = value;
        }
        private Boolean _DumpSwitch = false;
        public Boolean getDumpSwitch() {
            return _DumpSwitch;
        }
        public void setDumpSwitch(Boolean value) {
            _DumpSwitch = value;
        }
        private String _DumpPath = "";
        public String getDumpPath() {
            return _DumpPath;
        }
        public void setDumpPath(String value) {
            _DumpPath = value;
        }
        private Boolean _WavSwitch = false;
        public Boolean getWavSwitch() {
            return _WavSwitch;
        }
        public void setWavSwitch(Boolean value) {
            _WavSwitch = value;
        }
        private String _WavPath = "";
        public String getWavPath() {
            return _WavPath;
        }
        public void setWavPath(String value) {
            _WavPath = value;
        }
        private int _FilterIndex = 0;
        public int getFilterIndex() {
            return _FilterIndex;
        }
        public void setFilterIndex(int value) {
            _FilterIndex = value;
        }
        private String _TextExt = "txt;doc;hed";
        public String getTextExt() {
            return _TextExt;
        }
        public void setTextExt(String value) {
            _TextExt = value;
        }
        private String _MMLExt = "mml;gwi;muc;mdl";
        public String getMMLExt() {
            return _MMLExt;
        }
        public void setMMLExt(String value) {
            _MMLExt = value;
        }
        private String _ImageExt = "jpg;gif;png;mag";
        public String getImageExt() {
            return _ImageExt;
        }
        public void setImageExt(String value) {
            _ImageExt = value;
        }
        private Boolean _AutoOpenText = false;
        public Boolean getAutoOpenText() {
            return _AutoOpenText;
        }
        public void setAutoOpenText(Boolean value) {
            _AutoOpenText = value;
        }
        private Boolean _AutoOpenMML = false;
        public Boolean getAutoOpenMML() {
            return _AutoOpenMML;
        }
        public void setAutoOpenMML(Boolean value) {
            _AutoOpenMML = value;
        }
        private Boolean _AutoOpenImg = false;
        public Boolean getAutoOpenImg() {
            return _AutoOpenImg;
        }
        public void setAutoOpenImg(Boolean value) {
            _AutoOpenImg = value;
        }
        private Boolean _InitAlways = false;
        public Boolean getInitAlways() {
            return _InitAlways;
        }
        public void setInitAlways(Boolean value) {
            _InitAlways = value;
        }
        private Boolean _EmptyPlayList = false;
        public Boolean getEmptyPlayList() {
            return _EmptyPlayList;
        }
        public void setEmptyPlayList(Boolean value) {
            _EmptyPlayList = value;
        }
        Boolean _ExAll = false;
        public Boolean getExAll() {
            return _ExAll;
        }
        public void setExAll(Boolean value) {
            _ExAll = value;
        }
        Boolean _NonRenderingForPause = false;
        public Boolean getNonRenderingForPause() {
            return _NonRenderingForPause;
        }
        public void setNonRenderingForPause(Boolean value) {
            _NonRenderingForPause = value;
        }

        public Other copy() {
            Other other = new Other();
            other._UseLoopTimes = this._UseLoopTimes;
            other._LoopTimes = this._LoopTimes;
            other._UseGetInst = this._UseGetInst;
            other._DefaultDataPath = this._DefaultDataPath;
            other._InstFormat = this._InstFormat;
            other._Zoom = this._Zoom;
            other._ScreenFrameRate = this._ScreenFrameRate;
            other._AutoOpen = this._AutoOpen;
            other._DumpSwitch = this._DumpSwitch;
            other._DumpPath = this._DumpPath;
            other._WavSwitch = this._WavSwitch;
            other._WavPath = this._WavPath;
            other._FilterIndex = this._FilterIndex;
            other._TextExt = this._TextExt;
            other._MMLExt = this._MMLExt;
            other._ImageExt = this._ImageExt;
            other._AutoOpenText = this._AutoOpenText;
            other._AutoOpenMML = this._AutoOpenMML;
            other._AutoOpenImg = this._AutoOpenImg;
            other._InitAlways = this._InitAlways;
            other._EmptyPlayList = this._EmptyPlayList;
            other._ExAll = this._ExAll;
            other._NonRenderingForPause = this._NonRenderingForPause;

            return other;
        }
    }

    public static class Balance implements Serializable {

        private int _MasterVolume = 0;

        public int getMasterVolume() {
            if (_MasterVolume > 20 || _MasterVolume < -192)
                _MasterVolume = 0;
            return _MasterVolume;
        }

        void setMasterVolume(int value) {
            _MasterVolume = value;
            if (_MasterVolume > 20 || _MasterVolume < -192)
                _MasterVolume = 0;
        }

        private int _YM2612Volume = 0;

        public int getYM2612Volume() {
            if (_YM2612Volume > 20 || _YM2612Volume < -192)
                _YM2612Volume = 0;
            return _YM2612Volume;
        }

        void setYM2612Volume(int value) {
            _YM2612Volume = value;
            if (_YM2612Volume > 20 || _YM2612Volume < -192)
                _YM2612Volume = 0;
        }

        private int _SN76489Volume = 0;

        public int getSN76489Volume() {
            if (_SN76489Volume > 20 || _SN76489Volume < -192)
                _SN76489Volume = 0;
            return _SN76489Volume;
        }

        void setSN76489Volume(int value) {
            _SN76489Volume = value;
            if (_SN76489Volume > 20 || _SN76489Volume < -192)
                _SN76489Volume = 0;
        }

        private int _RF5C68Volume = 0;

        public int getRF5C68Volume() {
            if (_RF5C68Volume > 20 || _RF5C68Volume < -192)
                _RF5C68Volume = 0;
            return _RF5C68Volume;
        }

        void setRF5C68Volume(int value) {
            _RF5C68Volume = value;
            if (_RF5C68Volume > 20 || _RF5C68Volume < -192)
                _RF5C68Volume = 0;
        }

        private int _RF5C164Volume = 0;

        public int getRF5C164Volume() {
            if (_RF5C164Volume > 20 || _RF5C164Volume < -192)
                _RF5C164Volume = 0;
            return _RF5C164Volume;
        }

        void setRF5C164Volume(int value) {
            _RF5C164Volume = value;
            if (_RF5C164Volume > 20 || _RF5C164Volume < -192)
                _RF5C164Volume = 0;
        }

        private int _PWMVolume = 0;

        public int getPWMVolume() {
            if (_PWMVolume > 20 || _PWMVolume < -192)
                _PWMVolume = 0;
            return _PWMVolume;
        }

        void setPWMVolume(int value) {
            _PWMVolume = value;
            if (_PWMVolume > 20 || _PWMVolume < -192)
                _PWMVolume = 0;
        }

        private int _C140Volume = 0;

        public int getC140Volume() {
            if (_C140Volume > 20 || _C140Volume < -192)
                _C140Volume = 0;
            return _C140Volume;
        }

        void setC140Volume(int value) {
            _C140Volume = value;
            if (_C140Volume > 20 || _C140Volume < -192)
                _C140Volume = 0;
        }

        private int _OKIM6258Volume = 0;

        public int getOKIM6258Volume() {
            if (_OKIM6258Volume > 20 || _OKIM6258Volume < -192)
                _OKIM6258Volume = 0;
            return _OKIM6258Volume;
        }

        void setOKIM6258Volume(int value) {
            _OKIM6258Volume = value;
            if (_OKIM6258Volume > 20 || _OKIM6258Volume < -192)
                _OKIM6258Volume = 0;
        }

        private int _OKIM6295Volume = 0;

        public int getOKIM6295Volume() {
            if (_OKIM6295Volume > 20 || _OKIM6295Volume < -192)
                _OKIM6295Volume = 0;
            return _OKIM6295Volume;
        }

        void setOKIM6295Volume(int value) {
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

        void setSEGAPCMVolume(int value) {
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

        void setAY8910Volume(int value) {
            _AY8910Volume = value;
            if (_AY8910Volume > 20 || _AY8910Volume < -192)
                _AY8910Volume = 0;
        }

        private int _YM2413Volume = 0;

        public int getYM2413Volume() {
            if (_YM2413Volume > 20 || _YM2413Volume < -192)
                _YM2413Volume = 0;
            return _YM2413Volume;
        }

        void setYM2413Volume(int value) {
            _YM2413Volume = value;
            if (_YM2413Volume > 20 || _YM2413Volume < -192)
                _YM2413Volume = 0;
        }

        private int _YM3526Volume = 0;

        public int getYM3526Volume() {
            if (_YM3526Volume > 20 || _YM3526Volume < -192)
                _YM3526Volume = 0;
            return _YM3526Volume;
        }

        void setYM3526Volume(int value) {
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

        void setY8950Volume(int value) {
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

        void setHuC6280Volume(int value) {
            _HuC6280Volume = value;
            if (_HuC6280Volume > 20 || _HuC6280Volume < -192)
                _HuC6280Volume = 0;
        }

        private int _YM2151Volume = 0;

        public int getYM2151Volume() {
            if (_YM2151Volume > 20 || _YM2151Volume < -192)
                _YM2151Volume = 0;
            return _YM2151Volume;
        }

        void setYM2151Volume(int value) {
            _YM2151Volume = value;
            if (_YM2151Volume > 20 || _YM2151Volume < -192)
                _YM2151Volume = 0;
        }

        private int _YM2608Volume = 0;

        public int getYM2608Volume() {
            if (_YM2608Volume > 20 || _YM2608Volume < -192)
                _YM2608Volume = 0;
            return _YM2608Volume;
        }

        void setYM2608Volume(int value) {
            _YM2608Volume = value;
            if (_YM2608Volume > 20 || _YM2608Volume < -192)
                _YM2608Volume = 0;
        }

        private int _YM2608FMVolume = 0;

        public int getYM2608FMVolume() {
            if (_YM2608FMVolume > 20 || _YM2608FMVolume < -192)
                _YM2608FMVolume = 0;
            return _YM2608FMVolume;
        }

        void setYM2608FMVolume(int value) {
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

        void setYM2608PSGVolume(int value) {
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

        void setYM2608RhythmVolume(int value) {
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

        void setYM2608AdpcmVolume(int value) {
            _YM2608AdpcmVolume = value;
            if (_YM2608AdpcmVolume > 20 || _YM2608AdpcmVolume < -192)
                _YM2608AdpcmVolume = 0;
        }

        private int _YM2203Volume = 0;

        public int getYM2203Volume() {
            if (_YM2203Volume > 20 || _YM2203Volume < -192)
                _YM2203Volume = 0;
            return _YM2203Volume;
        }

        void setYM2203Volume(int value) {
            _YM2203Volume = value;
            if (_YM2203Volume > 20 || _YM2203Volume < -192)
                _YM2203Volume = 0;
        }

        private int _YM2203FMVolume = 0;

        public int getYM2203FMVolume() {
            if (_YM2203FMVolume > 20 || _YM2203FMVolume < -192)
                _YM2203FMVolume = 0;
            return _YM2203FMVolume;
        }

        void setYM2203FMVolume(int value) {
            _YM2203FMVolume = value;
            if (_YM2203FMVolume > 20 || _YM2203FMVolume < -192)
                _YM2203FMVolume = 0;
        }

        private int _YM2203PSGVolume = 0;

        public int getYM2203PSGVolume() {
            if (_YM2203PSGVolume > 20 || _YM2203PSGVolume < -192)
                _YM2203PSGVolume = 0;
            return _YM2203PSGVolume;
        }

        void setYM2203PSGVolume(int value) {
            _YM2203PSGVolume = value;
            if (_YM2203PSGVolume > 20 || _YM2203PSGVolume < -192)
                _YM2203PSGVolume = 0;
        }

        private int _YM2610Volume = 0;

        public int getYM2610Volume() {
            if (_YM2610Volume > 20 || _YM2610Volume < -192)
                _YM2610Volume = 0;
            return _YM2610Volume;
        }

        void setYM2610Volume(int value) {
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

        void setYM2610FMVolume(int value) {
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

        void setYM2610PSGVolume(int value) {
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

        void setYM2610AdpcmAVolume(int value) {
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

        void setYM2610AdpcmBVolume(int value) {
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

        void setYM3812Volume(int value) {
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

        void setC352Volume(int value) {
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

        void setSAA1099Volume(int value) {
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

        void setPPZ8Volume(int value) {
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

        void setK054539Volume(int value) {
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

        void setAPUVolume(int value) {
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

        void setDMCVolume(int value) {
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

        void setFDSVolume(int value) {
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

        void setMMC5Volume(int value) {
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

        void setN160Volume(int value) {
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

        void setVRC6Volume(int value) {
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

        void setVRC7Volume(int value) {
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

        void setFME7Volume(int value) {
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

        void setDMGVolume(int value) {
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

        void setGA20Volume(int value) {
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

        void setYMZ280BVolume(int value) {
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

        void setYMF271Volume(int value) {
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

        void setYMF262Volume(int value) {
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

        void setYMF278BVolume(int value) {
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

        void setMultiPCMVolume(int value) {
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

        void setQSoundVolume(int value) {
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

        void setK051649Volume(int value) {
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

        void setK053260Volume(int value) {
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

        void setGimicOPNVolume(int value) {
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

        void setGimicOPNAVolume(int value) {
            _GimicOPNAVolume = value;
            if (_GimicOPNAVolume > 127 || _GimicOPNAVolume < 0)
                _GimicOPNAVolume = 30;
        }

        public Balance copy() {
            Balance balance = new Balance();
            balance._MasterVolume = this._MasterVolume;
            balance._YM2151Volume = this._YM2151Volume;
            balance._YM2203Volume = this._YM2203Volume;
            balance._YM2203FMVolume = this._YM2203FMVolume;
            balance._YM2203PSGVolume = this._YM2203PSGVolume;
            balance._YM2413Volume = this._YM2413Volume;
            balance._YM2608Volume = this._YM2608Volume;
            balance._YM2608FMVolume = this._YM2608FMVolume;
            balance._YM2608PSGVolume = this._YM2608PSGVolume;
            balance._YM2608RhythmVolume = this._YM2608RhythmVolume;
            balance._YM2608AdpcmVolume = this._YM2608AdpcmVolume;
            balance._YM2610Volume = this._YM2610Volume;
            balance._YM2610FMVolume = this._YM2610FMVolume;
            balance._YM2610PSGVolume = this._YM2610PSGVolume;
            balance._YM2610AdpcmAVolume = this._YM2610AdpcmAVolume;
            balance._YM2610AdpcmBVolume = this._YM2610AdpcmBVolume;

            balance._YM2612Volume = this._YM2612Volume;
            balance._AY8910Volume = this._AY8910Volume;
            balance._SN76489Volume = this._SN76489Volume;
            balance._HuC6280Volume = this._HuC6280Volume;
            balance._SAA1099Volume = this._SAA1099Volume;

            balance._RF5C164Volume = this._RF5C164Volume;
            balance._RF5C68Volume = this._RF5C68Volume;
            balance._PWMVolume = this._PWMVolume;
            balance._OKIM6258Volume = this._OKIM6258Volume;
            balance._OKIM6295Volume = this._OKIM6295Volume;
            balance._C140Volume = this._C140Volume;
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
                Log.forcedWrite(ex);
            }
        }

        public static Balance load(String fullPath) {
            java.nio.file.Path p = Paths.get(fullPath);
            if (!Files.exists(p))
                return null;
            try (InputStream in = Files.newInputStream(p)) {
                return Serdes.Util.deserialize(in, new Balance());
            } catch (IOException ex) {
                Log.forcedWrite(ex);
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

        private Boolean _OInfo = false;

        public Boolean getOInfo() {
            return _OInfo;
        }

        public void setOInfo(Boolean value) {
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

        private Boolean _OPlayList = false;

        public Boolean getOPlayList() {
            return _OPlayList;
        }

        public void setOPlayList(Boolean value) {
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

        private Boolean _OMixer = false;

        public Boolean getOMixer() {
            return _OMixer;
        }

        public void setOMixer(Boolean value) {
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

        private Boolean[] _OpenRf5c164 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenRf5c164() {
            return _OpenRf5c164;
        }

        void setOpenRf5c164(Boolean[] value) {
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

        private Boolean[] _OpenRf5c68 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenRf5c68() {
            return _OpenRf5c68;
        }

        void setOpenRf5c68(Boolean[] value) {
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

        private Boolean[] _OpenYMF271 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYMF271() {
            return _OpenYMF271;
        }

        void setOpenYMF271(Boolean[] value) {
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

        private Boolean[] _OpenC140 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenC140() {
            return _OpenC140;
        }

        void setOpenC140(Boolean[] value) {
            _OpenC140 = value;
        }

        private Point[] _PosS5B = new Point[] {
                EmptyPoint, EmptyPoint
        };

        public Point[] getPosS5B() {
            return _PosS5B;
        }

        void setPosS5B(Point[] value) {
            _PosS5B = value;
        }

        private Boolean[] _OpenS5B = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenS5B() {
            return _OpenS5B;
        }

        void setOpenS5B(Boolean[] value) {
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

        private Boolean[] _OpenDMG = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenDMG() {
            return _OpenDMG;
        }

        void setOpenDMG(Boolean[] value) {
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

        private Boolean[] _OpenPPZ8 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenPPZ8() {
            return _OpenPPZ8;
        }

        void setOpenPPZ8(Boolean[] value) {
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

        private Boolean[] _OpenYMZ280B = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYMZ280B() {
            return _OpenYMZ280B;
        }

        void setOpenYMZ280B(Boolean[] value) {
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

        private Boolean[] _OpenC352 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenC352() {
            return _OpenC352;
        }

        void setOpenC352(Boolean[] value) {
            _OpenC352 = value;
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

        private Boolean[] _OpenMultiPCM = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenMultiPCM() {
            return _OpenMultiPCM;
        }

        void setOpenMultiPCM(Boolean[] value) {
            _OpenMultiPCM = value;
        }

        private Boolean[] _OpenQSound = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenQSound() {
            return _OpenQSound;
        }

        void setOpenQSound(Boolean[] value) {
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

        private Boolean[] _OpenYm2151 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm2151() {
            return _OpenYm2151;
        }

        void setOpenYm2151(Boolean[] value) {
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

        private Boolean[] _OpenYm2608 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm2608() {
            return _OpenYm2608;
        }

        void setOpenYm2608(Boolean[] value) {
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

        private Boolean[] _OpenYm2203 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm2203() {
            return _OpenYm2203;
        }

        void setOpenYm2203(Boolean[] value) {
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

        private Boolean[] _OpenYm2610 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm2610() {
            return _OpenYm2610;
        }

        void setOpenYm2610(Boolean[] value) {
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

        private Boolean[] _OpenYm2612 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm2612() {
            return _OpenYm2612;
        }

        void setOpenYm2612(Boolean[] value) {
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

        private Boolean[] _OpenOKIM6258 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenOKIM6258() {
            return _OpenOKIM6258;
        }

        void setOpenOKIM6258(Boolean[] value) {
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

        private Boolean[] _OpenOKIM6295 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenOKIM6295() {
            return _OpenOKIM6295;
        }

        void setOpenOKIM6295(Boolean[] value) {
            _OpenOKIM6295 = value;
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

        private Boolean[] _OpenSN76489 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenSN76489() {
            return _OpenSN76489;
        }

        void setOpenSN76489(Boolean[] value) {
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

        private Boolean[] _OpenMIDI = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenMIDI() {
            return _OpenMIDI;
        }

        public void setOpenMIDI(Boolean[] value) {
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

        private Boolean[] _OpenSegaPCM = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenSegaPCM() {
            return _OpenSegaPCM;
        }

        void setOpenSegaPCM(Boolean[] value) {
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

        private Boolean[] _OpenAY8910 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenAY8910() {
            return _OpenAY8910;
        }

        void setOpenAY8910(Boolean[] value) {
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

        private Boolean[] _OpenHuC6280 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenHuC6280() {
            return _OpenHuC6280;
        }

        void setOpenHuC6280(Boolean[] value) {
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

        private Boolean[] _OpenK051649 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenK051649() {
            return _OpenK051649;
        }

        void setOpenK051649(Boolean[] value) {
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

        private Boolean[] _OpenYm2413 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm2413() {
            return _OpenYm2413;
        }

        void setOpenYm2413(Boolean[] value) {
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

        private Boolean[] _OpenYm3526 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm3526() {
            return _OpenYm3526;
        }

        void setOpenYm3526(Boolean[] value) {
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

        private Boolean[] _OpenY8950 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenY8950() {
            return _OpenY8950;
        }

        void setOpenY8950(Boolean[] value) {
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

        private Boolean[] _OpenYm3812 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYm3812() {
            return _OpenYm3812;
        }

        void setOpenYm3812(Boolean[] value) {
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

        private Boolean[] _OpenYmf262 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYmf262() {
            return _OpenYmf262;
        }

        void setOpenYmf262(Boolean[] value) {
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

        private Boolean[] _OpenYmf278b = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenYmf278b() {
            return _OpenYmf278b;
        }

        void setOpenYmf278b(Boolean[] value) {
            _OpenYmf278b = value;
        }

        private Point _PosYm2612MIDI = EmptyPoint;

        public Point getPosYm2612MIDI() {
            return _PosYm2612MIDI;
        }

        public void setPosYm2612MIDI(Point value) {
            _PosYm2612MIDI = value;
        }

        private Boolean _OpenYm2612MIDI = false;

        public Boolean getOpenYm2612MIDI() {
            return _OpenYm2612MIDI;
        }

        public void setOpenYm2612MIDI(Boolean value) {
            _OpenYm2612MIDI = value;
        }

        private Point _PosMixer = EmptyPoint;

        public Point getPosMixer() {
            return _PosMixer;
        }

        public void setPosMixer(Point value) {
            _PosMixer = value;
        }

        private Boolean _OpenMixer = false;

        public Boolean getOpenMixer() {
            return _OpenMixer;
        }

        void setOpenMixer(Boolean value) {
            _OpenMixer = value;
        }

        private Point _PosVSTeffectList = EmptyPoint;

        public Point getPosVSTeffectList() {
            return _PosVSTeffectList;
        }

        public void setPosVSTeffectList(Point value) {
            _PosVSTeffectList = value;
        }

        private Boolean _OpenVSTeffectList = false;

        public Boolean getOpenVSTeffectList() {
            return _OpenVSTeffectList;
        }

        public void setOpenVSTeffectList(Boolean value) {
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

        private Boolean[] _OpenNESDMC = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenNESDMC() {
            return _OpenNESDMC;
        }

        void setOpenNESDMC(Boolean[] value) {
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

        private Boolean[] _OpenFDS = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenFDS() {
            return _OpenFDS;
        }

        void setOpenFDS(Boolean[] value) {
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

        private Boolean[] _OpenMMC5 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenMMC5() {
            return _OpenMMC5;
        }

        void setOpenMMC5(Boolean[] value) {
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

        private Boolean[] _OpenVrc6 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenVrc6() {
            return _OpenVrc6;
        }

        void setOpenVrc6(Boolean[] value) {
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

        private Boolean[] _OpenVrc7 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenVrc7() {
            return _OpenVrc7;
        }

        void setOpenVrc7(Boolean[] value) {
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

        private Boolean[] _OpenN106 = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenN106() {
            return _OpenN106;
        }

        void setOpenN106(Boolean[] value) {
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

        private Boolean[] _OpenRegTest = new Boolean[] {
                false, false
        };

        public Boolean[] getOpenRegTest() {
            return _OpenRegTest;
        }

        void setOpenRegTest(Boolean[] value) {
            _OpenRegTest = value;
        }

        private Point _PosVisWave = EmptyPoint;

        public Point getPosVisWave() {
            return _PosVisWave;
        }

        public void setPosVisWave(Point value) {
            _PosVisWave = value;
        }

        private Boolean _OpenVisWave = false;

        public Boolean getOpenVisWave() {
            return _OpenVisWave;
        }

        public void setOpenVisWave(Boolean value) {
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
            location._OpenC140 = this._OpenC140;
            location._PosPPZ8 = this._PosPPZ8;
            location._OpenPPZ8 = this._OpenPPZ8;
            location._PosS5B = this._PosS5B;
            location._OpenS5B = this._OpenS5B;
            location._PosDMG = this._PosDMG;
            location._OpenDMG = this._OpenDMG;
            location._PosYMZ280B = this._PosYMZ280B;
            location._OpenYMZ280B = this._OpenYMZ280B;
            location._PosC352 = this._PosC352;
            location._OpenC352 = this._OpenC352;
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
            location._OpenOKIM6295 = this._OpenOKIM6295;
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
        private Boolean _NESUnmuteOnReset = true;
        private Boolean _NESNonLinearMixer = true;
        private Boolean _NESPhaseRefresh = true;
        private Boolean _NESDutySwap = false;
        private int _FDSLpf = 2000;
        private Boolean _FDS4085Reset = false;
        private Boolean _FDSWriteDisable8000 = true;
        private Boolean _DMCUnmuteOnReset = true;
        private Boolean _DMCNonLinearMixer = true;
        private Boolean _DMCEnable4011 = true;
        private Boolean _DMCEnablePnoise = true;
        private Boolean _DMCDPCMAntiClick = false;
        private Boolean _DMCRandomizeNoise = true;
        private Boolean _DMCTRImute = true;
        private Boolean _DMCTRINull = true;
        private Boolean _MMC5NonLinearMixer = true;
        private Boolean _MMC5PhaseRefresh = true;
        private Boolean _N160Serial = false;

        public Boolean getNESUnmuteOnReset() {
            return _NESUnmuteOnReset;
        }
        public void setNESUnmuteOnReset(Boolean value) {
            _NESUnmuteOnReset = value;
        }
        public Boolean getNESNonLinearMixer() {
            return _NESNonLinearMixer;
        }
        public void setNESNonLinearMixer(Boolean value) {
            _NESNonLinearMixer = value;
        }
        public Boolean getNESPhaseRefresh() {
            return _NESPhaseRefresh;
        }
        public void setNESPhaseRefresh(Boolean value) {
            _NESPhaseRefresh = value;
        }
        public Boolean getNESDutySwap() {
            return _NESDutySwap;
        }
        public void setNESDutySwap(Boolean value) {
            _NESDutySwap = value;
        }
        public int getFDSLpf() {
            return _FDSLpf;
        }
        public void setFDSLpf(int value) {
            _FDSLpf = value;
        }
        public Boolean getFDS4085Reset() {
            return _FDS4085Reset;
        }
        public void setFDS4085Reset(Boolean value) {
            _FDS4085Reset = value;
        }
        public Boolean getFDSWriteDisable8000() {
            return _FDSWriteDisable8000;
        }
        public void setFDSWriteDisable8000(Boolean value) {
            _FDSWriteDisable8000 = value;
        }
        public Boolean getDMCUnmuteOnReset() {
            return _DMCUnmuteOnReset;
        }
        public void setDMCUnmuteOnReset(Boolean value) {
            _DMCUnmuteOnReset = value;
        }
        public Boolean getDMCNonLinearMixer() {
            return _DMCNonLinearMixer;
        }
        public void setDMCNonLinearMixer(Boolean value) {
            _DMCNonLinearMixer = value;
        }
        public Boolean getDMCEnable4011() {
            return _DMCEnable4011;
        }
        public void setDMCEnable4011(Boolean value) {
            _DMCEnable4011 = value;
        }
        public Boolean getDMCEnablePnoise() {
            return _DMCEnablePnoise;
        }
        public void setDMCEnablePnoise(Boolean value) {
            _DMCEnablePnoise = value;
        }
        public Boolean getDMCDPCMAntiClick() {
            return _DMCDPCMAntiClick;
        }
        public void setDMCDPCMAntiClick(Boolean value) {
            _DMCDPCMAntiClick = value;
        }
        public Boolean getDMCRandomizeNoise() {
            return _DMCRandomizeNoise;
        }
        public void setDMCRandomizeNoise(Boolean value) {
            _DMCRandomizeNoise = value;
        }
        public Boolean getDMCTRImute() {
            return _DMCTRImute;
        }
        public void setDMCTRImute(Boolean value) {
            _DMCTRImute = value;
        }
        public Boolean getDMCTRINull() {
            return _DMCTRINull;
        }
        void setDMCTRINull(Boolean value) {
            _DMCTRINull = value;
        }
        public Boolean getMMC5NonLinearMixer() {
            return _MMC5NonLinearMixer;
        }
        public void setMMC5NonLinearMixer(Boolean value) {
            _MMC5NonLinearMixer = value;
        }
        public Boolean getMMC5PhaseRefresh() {
            return _MMC5PhaseRefresh;
        }
        public void setMMC5PhaseRefresh(Boolean value) {
            _MMC5PhaseRefresh = value;
        }
        public Boolean getN160Serial() {
            return _N160Serial;
        }
        public void setN160Serial(Boolean value) {
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
        private Boolean _DMCRandomizeTRI = false;
        public Boolean getDMCRandomizeTRI() {
            return _DMCRandomizeTRI;
        }
        public void setDMCRandomizeTRI(Boolean value) {
            _DMCRandomizeTRI = value;
        }
        private Boolean _DMCDPCMReverse = false;
        public Boolean getDMCDPCMReverse() {
            return _DMCDPCMReverse;
        }
        public void setDMCDPCMReverse(Boolean value) {
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
        setting._outputDevice = this._outputDevice.copy();

        setting._AY8910Type = null;
        if (this._AY8910Type != null) {
            setting._AY8910Type = new ChipType2[this._AY8910Type.length];
            for (int i = 0; i < this._AY8910Type.length; i++)
                setting._AY8910Type[i] = this._AY8910Type[i].copy();
        }

        setting._K051649Type = null;
        if (this._K051649Type != null) {
            setting._K051649Type = new ChipType2[this._K051649Type.length];
            for (int i = 0; i < this._K051649Type.length; i++)
                setting._K051649Type[i] = this._K051649Type[i].copy();
        }

        setting._YM2151Type = null;
        if (this._YM2151Type != null) {
            setting._YM2151Type = new ChipType2[this._YM2151Type.length];
            for (int i = 0; i < this._YM2151Type.length; i++)
                setting._YM2151Type[i] = this._YM2151Type[i].copy();
        }

        setting._YM2203Type = null;
        if (this._YM2203Type != null) {
            setting._YM2203Type = new ChipType2[this._YM2203Type.length];
            for (int i = 0; i < this._YM2203Type.length; i++)
                setting._YM2203Type[i] = this._YM2203Type[i].copy();
        }

        setting._YM2413Type = null;
        if (this._YM2413Type != null) {
            setting._YM2413Type = new ChipType2[this._YM2413Type.length];
            for (int i = 0; i < this._YM2413Type.length; i++)
                setting._YM2413Type[i] = this._YM2413Type[i].copy();
        }

        setting._YM2608Type = null;
        if (this._YM2608Type != null) {
            setting._YM2608Type = new ChipType2[this._YM2608Type.length];
            for (int i = 0; i < this._YM2608Type.length; i++)
                setting._YM2608Type[i] = this._YM2608Type[i].copy();
        }

        setting._YM2610Type = null;
        if (this._YM2610Type != null) {
            setting._YM2610Type = new ChipType2[this._YM2610Type.length];
            for (int i = 0; i < this._YM2610Type.length; i++)
                setting._YM2610Type[i] = this._YM2610Type[i].copy();
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

        setting._YMF262Type = null;
        if (this._YMF262Type != null) {
            setting._YMF262Type = new ChipType2[this._YMF262Type.length];
            for (int i = 0; i < this._YMF262Type.length; i++)
                setting._YMF262Type[i] = this._YMF262Type[i].copy();
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

        setting._unuseRealChip = this._unuseRealChip;
        setting._FileSearchPathList = this._FileSearchPathList;

        // setting._YM2151SType = this._YM2151SType.Copy();
        // setting._YM2203SType = this._YM2203SType.Copy();
        // setting._YM2413SType = this._YM2413SType.Copy();
        // setting._AY8910SType = this._AY8910SType.Copy();
        // setting._YM2608SType = this._YM2608SType.Copy();
        // setting._YM2610SType = this._YM2610SType.Copy();
        // setting._YM2612SType = this._YM2612SType.Copy();
        // setting._YM3526SType = this._YM3526SType.Copy();
        // setting._YM3812SType = this._YM3812SType.Copy();
        // setting._YMF262SType = this._YMF262SType.Copy();
        // setting._SN76489SType = this._SN76489SType.Copy();
        // setting._C140SType = this._C140SType.Copy();
        // setting._SEGAPCMSType = this._SEGAPCMSType.Copy();

        setting._other = this._other.copy();
        setting._balance = this._balance.copy();
        setting._LatencyEmulation = this._LatencyEmulation;
        setting._LatencySCCI = this._LatencySCCI;
        setting._Debug_DispFrameCounter = this._Debug_DispFrameCounter;
        setting._HiyorimiMode = this._HiyorimiMode;
        setting._location = this._location.Copy();
        setting._midiExport = this._midiExport.copy();
        setting._midiKbd = this._midiKbd.copy();
        setting._vst = this._vst.copy();
        setting._midiOut = this._midiOut.copy();
        setting._nsf = this._nsf.copy();
        setting._sid = this._sid.copy();
        setting._NukedOPN2 = this._NukedOPN2.Copy();
        setting._autoBalance = this._autoBalance.copy();
        setting._PMDDotNET = this._PMDDotNET.copy();

        setting._keyBoardHook = this._keyBoardHook.copy();

        return setting;
    }

    public void save() {
        String fullPath = Common.settingFilePath;
        fullPath = Path.combine(fullPath, Resources.getcntSettingFileName());

        try (OutputStream sw = Files.newOutputStream(Paths.get(fullPath))) {
            Serdes.Util.serialize(sw, this);
        } catch (IOException e) {
            Log.forcedWrite(e);
        }
    }

    public static Setting load() {
        try {
            String fn = Resources.getcntSettingFileName();
            if (File.exists(Path.getDirectoryName(System.getProperty("user.dir")) + fn)) {
                // アプリケーションと同じフォルダに設定ファイルがあるならそちらを使用する
                Common.settingFilePath = Path.getDirectoryName(System.getProperty("user.dir"));
            } else {
                // 上記以外は、アプリケーション向けデータフォルダを使用する
                Common.settingFilePath = Common.getApplicationDataFolder(true);
            }

            String fullPath = Common.settingFilePath;
            fullPath = Path.combine(fullPath, Resources.getcntSettingFileName());

            if (!File.exists(fullPath)) {
                return new Setting();
            }
            try (InputStream sr = Files.newInputStream(Paths.get(fullPath))) {
                return (Setting) Serdes.Util.deserialize(sr, new Setting());
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            return new Setting();
        }
    }
}
