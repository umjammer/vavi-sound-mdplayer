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
    private String inDeviceName = "";
    private Integer inUserDeviceNumber = 0;
    private String inUserDeviceName = "";
    private Integer inChannel = 0;
    private boolean solo = false;
    private boolean mute = false;
    private transient Color color = Color.black;
    private List<MIDIPart> parts = new ArrayList<>();
    private List<MIDIRythm> rythms = new ArrayList<>();
    private boolean rythmMode = false;
    private int key = 0;
    private int st = 0;
    private Integer startPartIndex = null;
    private Integer endPartIndex = null;
    private int numberPart = 0;
    //private int nowPartIndex = 0;
    private transient MIDIPart nowPart = null;
    private transient int nowTick = 0;
    private transient int nextEventTick = 0;
    private transient boolean endMark = false;
    private int trackNumber = 0;
    private transient Stack<MIDIEvent> loopTargetEvents = new Stack<>();
    private transient Integer loopOrSameTargetEventIndex = null;
    private transient Integer sameMeasure = null;
    private transient byte rolandBaseGt = 0;
    private transient byte rolandBaseVel = 0;
    private transient byte rolandDevGt = 0;
    private transient byte rolandDevVel = 0;
    private transient byte rolandParaGt = 0;
    private transient byte rolandParaVel = 0;
    private byte yamahaBaseGt = 0;
    private byte yamahaBaseVel = 0;
    private byte yamahaDev = 0;
    private byte yamahaModel = 0;
    private byte yamahaParagt = 0;
    private byte yamahaParaVel = 0;
    private int keySigSf = 0;
    private int keySigMi = 0;
    private transient int[] noteGateTime = new int[128];

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

    public int getNumber() {
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

    public String getComment() {
        return comment;
    }

    public void setOutDeviceNumber(Integer value) {
        outDeviceNumber = value;
    }

    public Integer getOutDeviceNumber() {
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

    public Integer getOutChannel() {
        return outChannel;
    }

    public void setInDeviceNumber(Integer value) {
        inDeviceNumber = value;
    }

    Integer getInDeviceNumber() {
        return inDeviceNumber;
    }

    public void setInDeviceName(String value) {
        inDeviceName = value;
    }

    String getInDeviceName() {
        return inDeviceName;
    }

    public void setInUserDeviceNumber(Integer value) {
        inUserDeviceNumber = value;
    }

    Integer getInUserDeviceNumber() {
        return inUserDeviceNumber;
    }

    public void setInUserDeviceName(String value) {
        inUserDeviceName = value;
    }

    String getInUserDeviceName() {
        return inUserDeviceName;
    }

    public void setInChannel(Integer value) {
        inChannel = value;
    }

    Integer getInChannel() {
        return inChannel;
    }

    public void setSolo(boolean value) {
        solo = value;
    }

    boolean getSolo() {
        return solo;
    }

    public void setMute(boolean value) {
        mute = value;
    }

    public boolean getMute() {
        return mute;
    }

    public void setColor(Color value) {
        color = value;
    }

    Color getColor() {
        return color;
    }

    public void setPart(List<MIDIPart> value) {
        parts = value;
    }

    public List<MIDIPart> getPart() {
        return parts;
    }

    public void setRythm(List<MIDIRythm> value) {
        rythms = value;
    }

    List<MIDIRythm> getRythm() {
        return rythms;
    }

    public void setRythmMode(boolean value) {
        rythmMode = value;
    }

    boolean getRythmMode() {
        return rythmMode;
    }

    public void setKey(int value) {
        key = value;
    }

    public int getKey() {
        return key;
    }

    public void setSt(int value) {
        st = value;
    }

    public int getSt() {
        return st;
    }

    public void setStartPartIndex(Integer value) {
        startPartIndex = value;
    }

    Integer getStartPartIndex() {
        return startPartIndex;
    }

    public void setEndPartIndex(Integer value) {
        endPartIndex = value;
    }

    Integer getEndPartIndex() {
        return endPartIndex;
    }

    public void setNumberPart(int value) {
        numberPart = value;
    }

    int getNumberPart() {
        return numberPart;
    }

    public void setTrackNumber(int value) {
        trackNumber = value;
    }

    int getTrackNumber() {
        return trackNumber;
    }

    private int mCounter = 0;

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setNowPart(MIDIPart value) {
        nowPart = value;
    }

    public MIDIPart getNowPart() {
        return nowPart;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setNowTick(int value) {
        nowTick = value;
    }

    public int getNowTick() {
        return nowTick;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setNextEventTick(int value) {
        nextEventTick = value;
    }

    public int getNextEventTick() {
        return nextEventTick;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setEndMark(boolean value) {
        endMark = value;
    }

    public boolean getEndMark() {
        return endMark;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setLoopTargetEvents(Stack<MIDIEvent> value) {
        loopTargetEvents = value;
    }

    public Stack<MIDIEvent> getLoopTargetEvents() {
        return loopTargetEvents;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setLoopOrSameTargetEventIndex(Integer value) {
        loopOrSameTargetEventIndex = value;
    }

    public Integer getLoopOrSameTargetEventIndex() {
        return loopOrSameTargetEventIndex;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setSameMeasure(Integer value) {
        sameMeasure = value;
    }

    public Integer getSameMeasure() {
        return sameMeasure;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setRolandBase_gt(byte value) {
        rolandBaseGt = value;
    }

    public byte getRolandBase_gt() {
        return rolandBaseGt;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setRolandBase_vel(byte value) {
        rolandBaseVel = value;
    }

    public byte getRolandBase_vel() {
        return rolandBaseVel;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setRolandDev_gt(byte value) {
        rolandDevGt = value;
    }

    public byte getRolandDev_gt() {
        return rolandDevGt;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setRolandDev_vel(byte value) {
        rolandDevVel = value;
    }

    public byte getRolandDev_vel() {
        return rolandDevVel;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setRolandPara_gt(byte value) {
        rolandParaGt = value;
    }

    public byte getRolandPara_gt() {
        return rolandParaGt;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setRolandPara_vel(byte value) {
        rolandParaVel = value;
    }

    public byte getRolandPara_vel() {
        return rolandParaVel;
    }

    /**
     * This is for performance use only and cannot be used for any other purpose.
     */
    public void setNoteGateTime(int[] value) {
        noteGateTime = value;
    }

    public int[] getNoteGateTime() {
        return noteGateTime;
    }

    public byte getYAMAHABase_gt() {
        return yamahaBaseGt;
    }

    public void setYAMAHABase_gt(byte value) {
        yamahaBaseGt = value;
    }

    public byte getYAMAHABase_vel() {
        return yamahaBaseVel;
    }

    public void setYAMAHABase_vel(byte value) {
        yamahaBaseVel = value;
    }

    public byte getYAMAHA_dev() {
        return yamahaDev;
    }

    public void setYAMAHA_dev(byte value) {
        yamahaDev = value;
    }

    public byte getYAMAHA_model() {
        return yamahaModel;
    }

    public void setYAMAHA_model(byte value) {
        yamahaModel = value;
    }

    public byte getYAMAHAPara_gt() {
        return yamahaParagt;
    }

    public void setYAMAHAPara_gt(byte value) {
        yamahaParagt = value;
    }

    public byte getYAMAHAPara_vel() {
        return yamahaParaVel;
    }

    public void setYAMAHAPara_vel(byte value) {
        yamahaParaVel = value;
    }

    public int getKeySIG_SF() {
        return keySigSf;
    }

    public void setKeySIG_SF(int value) {
        keySigSf = value;
    }

    public int getKeySIG_MI() {
        return keySigMi;
    }

    public void setKeySIG_MI(int value) {
        keySigMi = value;
    }

    /** Get the first part */
    public MIDIPart getStartPart() {
        if (getStartPartIndex() == null) return null;

        return parts.get(getStartPartIndex());
    }

    /** Get the last part */
    public MIDIPart getEndPart() {
        if (getEndPartIndex() == null) return null;

        return parts.get(getEndPartIndex());
    }

    /** Get the next part of the specified part */
    public MIDIPart getNextPart(MIDIPart part) {
        if (part == null || part.getAfterIndex() == null) return null;

        return parts.get(part.getAfterIndex());
    }

    /** Get the measure before the specified part */
    public MIDIPart getPrevPart(MIDIPart prt) {
        if (prt == null || prt.getBeforeIndex() == null) return null;

        return parts.get(prt.getBeforeIndex());
    }

    /** Get a part from an index */
    public MIDIPart searchPart(int index) {
        int stD = Integer.MAX_VALUE;
        int edD = Integer.MAX_VALUE;
        int nowD = Integer.MAX_VALUE;
        int mode;
        if (this.getStartPartIndex() != null) stD = Math.abs(this.getStartPartIndex() - index);
        if (this.getEndPartIndex() != null) edD = Math.abs(this.getEndPartIndex() - index);
        if (this.nowPart != null) nowD = Math.abs(this.nowPart.getENumber() - index);
        MIDIPart part;
        if (stD < edD && stD < nowD) {
            part = this.getStartPart();
            mode = 0;
        } else if (edD < stD && edD < nowD) {
            part = this.getEndPart();
            mode = 1;
        } else if (nowD <= stD && nowD <= edD) {
            part = this.nowPart;
            if (part.getNumber() < index) {
                mode = 0;
            } else {
                mode = 1;
            }
        } else return null;

        switch (mode) {
        case 0:
            while (part != null) {
                if (part.getNumber() == index)
                    return part;
                part = this.getNextPart(part);
            }
            break;
        case 1:
            while (part != null) {
                if (part.getNumber() == index)
                    return part;
                part = this.getPrevPart(part);
            }
            break;
        }

        return null;
    }

    /** Clear all parts from memory*/
    public void clearAllPartMemory() {
        this.parts.clear();
        this.mCounter = 0;
        this.setStartPartIndex(null);
        this.setEndPartIndex(null);
        this.setNumberPart(0);
    }

    /** Erase all parts */
    public void clearEvent() {
        this.mCounter = 0;
        this.setStartPartIndex(null);
        this.setEndPartIndex(null);
    }

    /**
     * Insert a part
     * (Note that as the number of existing parts increases, it will take longer to identify the insertion position.)
     * @param startTick Absolute Tick Value
     * @param part part
     */
    public void insertPart(int startTick, MIDIPart part) {
        if (part == null) return;
        part.setStartTick(startTick);

        if (this.parts == null) {
            this.parts = new ArrayList<>();
        }
        if (this.parts.isEmpty() || this.getStartPartIndex() == null) { // First part
            part.setAfterIndex(null);
            part.setBeforeIndex(null);
            part.setNumber(this.getNumber());
            this.getPart().add(part);
            this.setStartPartIndex(0);
            this.setEndPartIndex(0);
            this.mCounter = 1;
            this.setNumberPart(this.getNumberPart() + 1);
            return;
        }

        // The loop that is causing the slowdown
        MIDIPart pPrt = getStartPart();
        while (true) {
            if (pPrt.getStartTick() > part.getStartTick()) {
                pPrt = getPrevPart(pPrt);
                break;
            }
            MIDIPart ppPrt = getNextPart(pPrt);
            if (ppPrt == null) break;
            pPrt = ppPrt;
        }

        part.setBeforeIndex(pPrt.getNumber());
        part.setAfterIndex(pPrt.getAfterIndex());
        part.setNumber(this.getNumberPart());
        pPrt.setAfterIndex(part.getNumber());
        this.parts.add(part);
        if (part.getAfterIndex() == null) {
            this.setEndPartIndex(this.getNumberPart());
        } else {
            pPrt = getNextPart(part);
            pPrt.setBeforeIndex(part.getNumber());
        }
        this.mCounter++;
        this.setNumberPart(this.getNumberPart() + 1);
    }
}

