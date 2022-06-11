package mdplayer.driver.rcp;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mdplayer.driver.rcp.MIDIEvent.MIDIRythm;


public class MIDITrack implements Serializable {
    private Integer beforeIndex = null;
    private Integer afterIndex = null;
    private int number = 0;
    private String name = "";
    private String memo = "";
    private String comment = "";
    private Integer outDeviceNumber = 0;
    private String outDeviceName = "";
    private Integer outUserDeviceNumber = 0;
    private String outUserDeviceName = "";
    private Integer outChannel = 0;
    private Integer inDeviceNumber = 0;
    private String _InDeviceName = "";
    private Integer _InUserDeviceNumber = 0;
    private String _InUserDeviceName = "";
    private Integer _InChannel = 0;
    private Boolean _Solo = false;
    private Boolean _Mute = false;
    private Color _Color = Color.black;
    private List<MIDIPart> _Part = new ArrayList<>();
    private List<MIDIRythm> _Rythm = new ArrayList<>();
    private Boolean _RythmMode = false;
    private int _Key = 0;
    private int _St = 0;
    private Integer _StartPartIndex = null;
    private Integer _EndPartIndex = null;
    private int _NumberPart = 0;
    //private int _NowPartIndex = 0;
    private MIDIPart _NowPart = null;
    private int _NowTick = 0;
    private int _NextEventTick = 0;
    private Boolean _EndMark = false;
    private int _TrackNumber = 0;
    private Stack<MIDIEvent> _LoopTargetEvent = new Stack<MIDIEvent>();
    private Integer _LoopOrSameTargetEventIndex = null;
    private Integer _SameMeasure = null;
    private byte _RolandBase_gt = 0;
    private byte _RolandBase_vel = 0;
    private byte _RolandDev_gt = 0;
    private byte _RolandDev_vel = 0;
    private byte _RolandPara_gt = 0;
    private byte _RolandPara_vel = 0;
    private byte _YAMAHABase_gt = 0;
    private byte _YAMAHABase_vel = 0;
    private byte _YAMAHA_dev = 0;
    private byte _YAMAHA_model = 0;
    private byte _YAMAHAPara_gt = 0;
    private byte _YAMAHAPara_vel = 0;
    private int _KeySIG_SF = 0;
    private int _KeySIG_MI = 0;
    private int[] _NoteGateTime = new int[128];


    public void setBeforeIndex(Integer value) {
        beforeIndex = value;
    }

    Integer getBeforeIndex() {
        return beforeIndex;
    }

    public void setAfterIndex(int value) {
        afterIndex = value;
    }

    Integer getAfterIndex() {
        return afterIndex;
    }

    public void setNumber(int value) {
        number = value;
    }

    int getNumber() {
        return number;
    }

    public void setName(String value) {
        name = value;
    }

    String getName() {
        return name;
    }

    public void setMemo(String value) {
        memo = value;
    }

    String getMemo() {
        return memo;
    }

    public void setComment(String value) {
        comment = value;
    }

    String getComment() {
        return comment;
    }

    public void setOutDeviceNumber(Integer value) {
        outDeviceNumber = value;
    }

    Integer getOutDeviceNumber() {
        return outDeviceNumber;
    }

    public void setOutDeviceName(String value) {
        outDeviceName = value;
    }

    String getOutDeviceName() {
        return outDeviceName;
    }

    public void setOutUserDeviceName(String value) {
        outUserDeviceName = value;
    }

    String getOutUserDeviceName() {
        return outUserDeviceName;
    }

    public void setOutUserDeviceNumber(Integer value) {
        outUserDeviceNumber = value;
    }

    Integer getOutUserDeviceNumber() {
        return outUserDeviceNumber;
    }

    public void setOutChannel(Integer value) {
        outChannel = value;
    }

    Integer getOutChannel() {
        return outChannel;
    }

    public void setInDeviceNumber(Integer value) {
        inDeviceNumber = value;
    }

    Integer getInDeviceNumber() {
        return inDeviceNumber;
    }

    public void setInDeviceName(String value) {
        _InDeviceName = value;
    }

    String getInDeviceName() {
        return _InDeviceName;
    }

    public void setInUserDeviceNumber(Integer value) {
        _InUserDeviceNumber = value;
    }

    Integer getInUserDeviceNumber() {
        return _InUserDeviceNumber;
    }

    public void setInUserDeviceName(String value) {
        _InUserDeviceName = value;
    }

    String getInUserDeviceName() {
        return _InUserDeviceName;
    }

    public void setInChannel(Integer value) {
        _InChannel = value;
    }

    Integer getInChannel() {
        return _InChannel;
    }

    public void setSolo(Boolean value) {
        _Solo = value;
    }

    Boolean getSolo() {
        return _Solo;
    }

    public void setMute(Boolean value) {
        _Mute = value;
    }

    Boolean getMute() {
        return _Mute;
    }

    //@XmlIgnore
    public void setColor(Color value) {
        _Color = value;
    }

    Color getColor() {
        return _Color;
    }

    public void setPart(List<MIDIPart> value) {
        _Part = value;
    }

    List<MIDIPart> getPart() {
        return _Part;
    }

    public void setRythm(List<MIDIRythm> value) {
        _Rythm = value;
    }

    List<MIDIRythm> getRythm() {
        return _Rythm;
    }

    public void setRythmMode(Boolean value) {
        _RythmMode = value;
    }

    Boolean getRythmMode() {
        return _RythmMode;
    }

    public void setKey(int value) {
        _Key = value;
    }

    int getKey() {
        return _Key;
    }

    public void setSt(int value) {
        _St = value;
    }

    int getSt() {
        return _St;
    }

    public void setStartPartIndex(Integer value) {
        _StartPartIndex = value;
    }

    Integer getStartPartIndex() {
        return _StartPartIndex;
    }

    public void setEndPartIndex(Integer value) {
        _EndPartIndex = value;
    }

    Integer getEndPartIndex() {
        return _EndPartIndex;
    }

    public void setNumberPart(int value) {
        _NumberPart = value;
    }

    int getNumberPart() {
        return _NumberPart;
    }

    public void setTrackNumber(int value) {
        _TrackNumber = value;
    }

    int getTrackNumber() {
        return _TrackNumber;
    }

    private int mCounter = 0;

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setNowPart(MIDIPart value) {
        _NowPart = value;
    }

    MIDIPart getNowPart() {
        return _NowPart;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setNowTick(int value) {
        _NowTick = value;
    }

    int getNowTick() {
        return _NowTick;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setNextEventTick(int value) {
        _NextEventTick = value;
    }

    int getNextEventTick() {
        return _NextEventTick;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setEndMark(Boolean value) {
        _EndMark = value;
    }

    Boolean getEndMark() {
        return _EndMark;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setLoopTargetEvent(Stack<MIDIEvent> value) {
        _LoopTargetEvent = value;
    }

    Stack<MIDIEvent> getLoopTargetEvent() {
        return _LoopTargetEvent;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setLoopOrSameTargetEventIndex(Integer value) {
        _LoopOrSameTargetEventIndex = value;
    }

    Integer getLoopOrSameTargetEventIndex() {
        return _LoopOrSameTargetEventIndex;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setSameMeasure(Integer value) {
        _SameMeasure = value;
    }

    Integer getSameMeasure() {
        return _SameMeasure;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setRolandBase_gt(byte value) {
        _RolandBase_gt = value;
    }

    byte getRolandBase_gt() {
        return _RolandBase_gt;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setRolandBase_vel(byte value) {
        _RolandBase_vel = value;
    }

    byte getRolandBase_vel() {
        return _RolandBase_vel;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setRolandDev_gt(byte value) {
        _RolandDev_gt = value;
    }

    byte getRolandDev_gt() {
        return _RolandDev_gt;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setRolandDev_vel(byte value) {
        _RolandDev_vel = value;
    }

    byte getRolandDev_vel() {
        return _RolandDev_vel;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void RolandPara_gt(byte value) {
        _RolandPara_gt = value;
    }

    byte getRolandPara_gt() {
        return _RolandPara_gt;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void RolandPara_vel(byte value) {
        _RolandPara_vel = value;
    }

    byte getRolandPara_vel() {
        return _RolandPara_vel;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使用不可
     */
    //@XmlIgnore
    public void setNoteGateTime(int[] value) {
        _NoteGateTime = value;
    }

    int[] getNoteGateTime() {
        return _NoteGateTime;
    }

    public byte getYAMAHABase_gt() {
        return _YAMAHABase_gt;
    }

    void setYAMAHABase_gt(byte value) {
        _YAMAHABase_gt = value;
    }

    public byte getYAMAHABase_vel() {
        return _YAMAHABase_vel;
    }

    void setYAMAHABase_vel(byte value) {
        _YAMAHABase_vel = value;
    }

    public byte getYAMAHA_dev() {
        return _YAMAHA_dev;
    }

    void setYAMAHA_dev(byte value) {
        _YAMAHA_dev = value;
    }

    public byte getYAMAHA_model() {
        return _YAMAHA_model;
    }

    void setYAMAHA_model(byte value) {
        _YAMAHA_model = value;
    }

    public byte getYAMAHAPara_gt() {
        return _YAMAHAPara_gt;
    }

    void setYAMAHAPara_gt(byte value) {
        _YAMAHAPara_gt = value;
    }

    public byte getYAMAHAPara_vel() {
        return _YAMAHAPara_vel;
    }

    void setYAMAHAPara_vel(byte value) {
        _YAMAHAPara_vel = value;
    }

    public int getKeySIG_SF() {
        return _KeySIG_SF;
    }

    void setKeySIG_SF(int value) {
        _KeySIG_SF = value;
    }

    public int getKeySIG_MI() {
        return _KeySIG_MI;
    }

    void setKeySIG_MI(int value) {
        _KeySIG_MI = value;
    }

    //初めのpartを得る
    public MIDIPart getStartPart() {
        if (getStartPartIndex() == null) return null;

        return _Part.get(getStartPartIndex());
    }

    //最後のpartを得る
    public MIDIPart getEndPart() {
        if (getEndPartIndex() == null) return null;

        return _Part.get(getEndPartIndex());
    }

    //指定したpartの次のpartを得る
    public MIDIPart getNextPart(MIDIPart prt) {
        if (prt == null || prt.getAfterIndex() == null) return null;

        return _Part.get(prt.getAfterIndex());
    }

    //指定したpartの前の小節を得る
    public MIDIPart getPrevPart(MIDIPart prt) {
        if (prt == null || prt.getBeforeIndex() == null) return null;

        return _Part.get(prt.getBeforeIndex());
    }

    //インデックスからpartを得る
    public MIDIPart searchPart(int index) {
        int st_d = Integer.MAX_VALUE;
        int ed_d = Integer.MAX_VALUE;
        int now_d = Integer.MAX_VALUE;
        int mode = -1;
        if (this.getStartPartIndex() != null) st_d = Math.abs((int) this.getStartPartIndex() - index);
        if (this.getEndPartIndex() != null) ed_d = Math.abs((int) this.getEndPartIndex() - index);
        if (this._NowPart != null) now_d = Math.abs(this._NowPart.getENumber() - index);
        MIDIPart prt = null;
        if (st_d < ed_d && st_d < now_d) {
            prt = this.getStartPart();
            mode = 0;
        } else if (ed_d < st_d && ed_d < now_d) {
            prt = this.getEndPart();
            mode = 1;
        } else if (now_d <= st_d && now_d <= ed_d) {
            prt = this._NowPart;
            if (prt.getNumber() < index) {
                mode = 0;
            } else {
                mode = 1;
            }
        } else return null;

        switch (mode) {
        case 0:
            while (prt != null) {
                if (prt.getNumber() == index)
                    return prt;
                prt = this.getNextPart(prt);
            }
            break;
        case 1:
            while (prt != null) {
                if (prt.getNumber() == index)
                    return prt;
                prt = this.getPrevPart(prt);
            }
            break;
        }

        return null;
    }

    //全てのPartをメモリから消去する
    public void clearAllPartMemory() {
        this._Part.clear();
        this.mCounter = 0;
        this.setStartPartIndex(null);
        this.setEndPartIndex(null);
        this.setNumberPart(0);
    }

    //全てのPartを消去する
    public void clearEvent() {
        this.mCounter = 0;
        this.setStartPartIndex(null);
        this.setEndPartIndex(null);
    }

    /**
     * partを挿入する
     * (既存partが増えると挿入位置を特定するのに時間がかかるようになるので注意)
     * @param StartTick 絶対値によるTick値
     * @param prt part
     */
    public void insertPart(int StartTick, MIDIPart prt) {
        if (prt == null) return;
        prt.setStartTick(StartTick);

        if (this._Part == null) {
            this._Part = new ArrayList<>();
        }
        if (this._Part.size() == 0 || this.getStartPartIndex() == null) { // 初めの prt
            prt.setAfterIndex(null);
            prt.setBeforeIndex(null);
            prt.setNumber(this.getNumber());
            this.getPart().add(prt);
            this.setStartPartIndex(0);
            this.setEndPartIndex(0);
            this.mCounter = 1;
            this.setNumberPart(this.getNumberPart() + 1);
            return;
        }

        // 遅くなる原因になっているループ
        MIDIPart pPrt = getStartPart();
        while (true) {
            if (pPrt.getStartTick() > prt.getStartTick()) {
                pPrt = getPrevPart(pPrt);
                break;
            }
            MIDIPart ppPrt = getNextPart(pPrt);
            if (ppPrt == null) break;
            pPrt = ppPrt;
        }

        prt.setBeforeIndex(pPrt.getNumber());
        prt.setAfterIndex(pPrt.getAfterIndex());
        prt.setNumber(this.getNumberPart());
        pPrt.setAfterIndex(prt.getNumber());
        this._Part.add(prt);
        if (prt.getAfterIndex() == null) {
            this.setEndPartIndex(this.getNumberPart());
        } else {
            pPrt = getNextPart(prt);
            pPrt.setBeforeIndex(prt.getNumber());
        }
        this.mCounter++;
        this.setNumberPart(this.getNumberPart() + 1);
    }
}

