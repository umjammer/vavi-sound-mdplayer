package mdplayer.driver.rcp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mdplayer.driver.rcp.MIDIEvent.MIDIEventType;
import mdplayer.driver.rcp.MIDIEvent.MIDISpEventType;


public class MIDIPart implements Serializable {
    private Integer _BeforeIndex = null;
    private Integer _AfterIndex = null;
    private int _Number = 0;
    private String _Name = "";
    private int _StartTick = 0;
    private List<MIDIEvent> _Event = new ArrayList<MIDIEvent>();
    private int _eNumber = 0;
    private Integer _eStartIndex = null;
    private Integer _eEndIndex = null;
    private Integer _eNowIndex = 0;


    public void setBeforeIndex(Integer value) {
        _BeforeIndex = value;
    }

    Integer getBeforeIndex() {
        return _BeforeIndex;
    }

    public void setAfterIndex(Integer value) {
        _AfterIndex = value;
    }

    Integer getAfterIndex() {
        return _AfterIndex;
    }

    public void setNumber(int value) {
        _Number = value;
    }

    int getNumber() {
        return _Number;
    }

    public void setName(String value) {
        _Name = value;
    }

    String getName() {
        return _Name;
    }

    public void setStartTick(int value) {
        _StartTick = value;
    }

    int getStartTick() {
        return _StartTick;
    }

    /**
     * イベントリスト
     */
    public void setEvent(List<MIDIEvent> value) {
        _Event = value;
    }

    List<MIDIEvent> getEvent() {
        return _Event;
    }

    /**
     * 有効なイベントの個数
     */
    private int eCounter = 0;

    /**
     * イベントの通し番号
     */
    public void setENumber(int value) {
        _eNumber = value;
    }

    int getENumber() {
        return _eNumber;
    }

    /**
     * 開始イベントの番号
     */
    public void setEStartIndex(Integer value) {
        _eStartIndex = value;
    }

    Integer getEStartIndex() {
        return _eStartIndex;
    }

    /**
     * 終了イベントの番号
     */
    public void setEEndIndex(Integer value) {
        _eEndIndex = value;
    }

    Integer getEEndIndex() {
        return _eEndIndex;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使っちゃだめ
     */
    public void setENowIndex(Integer value) {
        _eNowIndex = value;
    }

    Integer getENowIndex() {
        return _eNowIndex;
    }

    //初めのイベントを得る
    public MIDIEvent getStartEvent() {
        if (_eStartIndex == null) return null;

        return _Event.get((int) _eStartIndex);
    }

    /** 最後のイベントを得る */
    public MIDIEvent getEndEvent() {
        if (_eEndIndex == null) return null;

        return _Event.get((int) _eEndIndex);
    }

    /** 指定したイベントの次のイベントを得る */
    public MIDIEvent getNextEvent(MIDIEvent eve) {
        if (eve == null || eve.getAfterIndex() == null) return null;

        return _Event.get((int) eve.getAfterIndex());
    }

    /** 指定したイベントの前のイベントを得る */
    public MIDIEvent getPrevEvent(MIDIEvent eve) {
        if (eve == null || eve.getBeforeIndex() == null) return null;

        return _Event.get((int) eve.getBeforeIndex());
    }

    /** 指定したイベントを除外する (メモリには残る) */
    public Boolean removeEvent(MIDIEvent eve) {
        if (eve == null) return false;
        MIDIEvent pEvent = getPrevEvent(eve);
        MIDIEvent nEvent = getNextEvent(eve);
        if (pEvent != null) pEvent.setAfterIndex((nEvent == null) ? null : (Integer) nEvent.getNumber());
        if (nEvent != null) nEvent.setBeforeIndex((pEvent == null) ? null : (Integer) pEvent.getNumber());
        pEvent.setStep(pEvent.getStep() + eve.getStep());
        this.eCounter--;

        return true;
    }

    /** 指定したイベントをメモリから消去する(removeEventに比べ低速) */
    public Boolean clearEvent(MIDIEvent eve) {
        if (eve == null) return false;
        MIDIEvent pEvent = getPrevEvent(eve);
        MIDIEvent nEvent = getNextEvent(eve);
        if (pEvent != null) pEvent.setAfterIndex((nEvent == null) ? null : (Integer) nEvent.getNumber());
        if (nEvent != null) nEvent.setBeforeIndex((pEvent == null) ? null : (Integer) pEvent.getNumber());
        pEvent.setStep(pEvent.getStep() + eve.getStep());
        this.eCounter--;
        this._eNumber--;

        int num = eve.getNumber();
        this._Event.remove(eve);

        for (MIDIEvent evt : this._Event) {
            if (evt.getNumber() >= num) evt.setNumber(evt.getNumber() - 1);
            if (evt.getAfterIndex() >= num) evt.setAfterIndex(evt.getAfterIndex() - 1);
            if (evt.getBeforeIndex() >= num) evt.setBeforeIndex(evt.getBeforeIndex() - 1);
        }

        return true;
    }

    /** 全てのイベントをメモリから消去する */
    public void clearAllEventMemory() {
        this._Event.clear();
        this.eCounter = 0;
        this.setEStartIndex(null);
        this.setEEndIndex(null);
        this.setENumber(0);
    }

    /** 全てのイベントを消去する */
    public void clearEvent() {
        this.eCounter = 0;
        this.setEStartIndex(null);
        this.setEEndIndex(null);
    }

    /**
     * 指定されたイベントの後ろにイベントを挿入する
     *
     * @param TargetEvent このイベントの後ろに新たに入る
     * @param Step        Step値
     * @param EventType   イベントタイプ
     * @param MIDImessage MIDIメッセージ(Chは0固定であること)
     * @return 新たに挿入したイベント
     */
    public MIDIEvent insertEvent(MIDIEvent TargetEvent, int Step, MIDIEventType EventType, byte[] MIDImessage) {
        if (MIDImessage == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(EventType);
        eve.setMIDIMessage(MIDImessage);
        eve.setMIDIMessageLst(null);
        eve.setStep(Step);

        insertEve(TargetEvent, Step, eve);

        return eve;
    }

    /**
     * 指定されたイベントの後ろにイベントを挿入する
     *
     * @param TargetEvent このイベントの後ろに新たに入る
     * @param Step        Step 値
     * @param EventType   イベントタイプ
     * @param MIDImessage MIDIメッセージ(Chは0固定であること)
     * @param gt          ゲートタイム
     * @return 新たに挿入したイベント
     */
    public MIDIEvent insertEvent(MIDIEvent TargetEvent, int Step, MIDIEventType EventType, byte[] MIDImessage, int gt) {
        if (MIDImessage == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(EventType);
        eve.setMIDIMessage(MIDImessage);
        eve.setMIDIMessageLst(null);
        eve.setStep(Step);
        eve.setGate(gt);

        insertEve(TargetEvent, Step, eve);

        return eve;
    }

    /**
     * 指定されたイベントの後ろにイベントを挿入する
     *
     * @param TargetEvent    このイベントの後ろに新たに入る
     * @param Step           Step 値
     * @param EventType      イベントタイプ
     * @param MIDImessageLst MIDI メッセージ
     * @return 新たに挿入したイベント
     */
    public MIDIEvent insertSpEvent(MIDIEvent TargetEvent, int Step, MIDISpEventType EventType, byte[][] MIDImessageLst) {
        //if (MIDImessageLst == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(MIDIEventType.MetaSequencerSpecific);
        eve.setMIDIMessage(new byte[] {(byte) EventType.ordinal()});
        eve.setMIDIMessageLst(MIDImessageLst);
        eve.setStep(Step);

        insertEve(TargetEvent, Step, eve);

        return eve;
    }

    private void insertEve(MIDIEvent TargetEvent, int Step, MIDIEvent eve) {
        //イベントリストを生成
        if (this.getEvent() == null) {
            this.setEvent(new ArrayList<MIDIEvent>());
        }
        if (TargetEvent == null || this.getEvent().size() == 0 || this.getEStartIndex() == null) { //初めのイベント
            eve.setAfterIndex(null);
            eve.setBeforeIndex(null);
            eve.setNumber(this.getENumber());
            this.getEvent().add(eve);
            this.setEStartIndex(0);
            this.setEEndIndex(0);
            this.eCounter = 1;
            this.setENumber(this.getENumber() + 1);
            return;
        }

        eve.setBeforeIndex(TargetEvent.getNumber());
        eve.setAfterIndex(TargetEvent.getAfterIndex());
        eve.setNumber(this.getENumber());
        TargetEvent.setAfterIndex(eve.getNumber());
        this.getEvent().add(eve);
        if (eve.getAfterIndex() == null) {
            this.setEEndIndex(this.getENumber());
        } else {
            TargetEvent = getNextEvent(eve);
            TargetEvent.setBeforeIndex(eve.getNumber());
        }
        this.eCounter++;
        this.setENumber(this.getENumber() + 1);
    }

    /**
     * Tickを考慮せずに最後のイベントの後ろに追加する。
     *
     * @param step
     * @param eventType
     * @param midiMessage
     */
    public void addEvent(int step, MIDIEventType eventType, byte[] midiMessage) {
        if (midiMessage == null) return;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(eventType);
        eve.setMIDIMessage(midiMessage);
        eve.setMIDIMessageLst(null);
        eve.setStep(step);

        MIDIEvent lastEvent = this.getEndEvent();
        if (lastEvent == null) { //初めのイベント
            eve.setAfterIndex(null);
            eve.setBeforeIndex(null);
            eve.setNumber(this.getENumber());
            this.getEvent().add(eve);
            this.setEStartIndex(0);
            this.setEEndIndex(0);
            this.eCounter = 1;
            this.setENumber(this.getENumber() + 1);
            return;
        }

        eve.setBeforeIndex(lastEvent.getNumber());
        eve.setAfterIndex(null);
        eve.setNumber(this.getENumber());
        this.setEEndIndex(this.getENumber());
        this.getEvent().add(eve);
        this.eCounter++;
        this.setENumber(this.getENumber() + 1);
        lastEvent.setAfterIndex(eve.getNumber());
    }
}
