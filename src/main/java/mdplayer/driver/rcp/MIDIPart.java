package mdplayer.driver.rcp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mdplayer.driver.rcp.MIDIEvent.MIDIEventType;
import mdplayer.driver.rcp.MIDIEvent.MIDISpEventType;


public class MIDIPart implements Serializable {
    private Integer beforeIndex = null;
    private Integer afterIndex = null;
    private int number = 0;
    private String name = "";
    private int startTick = 0;
    private List<MIDIEvent> events = new ArrayList<>();
    private int eNumber = 0;
    private Integer eStartIndex = null;
    private Integer eEndIndex = null;
    private Integer eNowIndex = 0;


    public void setBeforeIndex(Integer value) {
        beforeIndex = value;
    }

    Integer getBeforeIndex() {
        return beforeIndex;
    }

    public void setAfterIndex(Integer value) {
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

    public void setStartTick(int value) {
        startTick = value;
    }

    int getStartTick() {
        return startTick;
    }

    /**
     * イベントリスト
     */
    public void setEvents(List<MIDIEvent> value) {
        events = value;
    }

    List<MIDIEvent> getEvents() {
        return events;
    }

    /**
     * 有効なイベントの個数
     */
    private int eCounter = 0;

    /**
     * イベントの通し番号
     */
    public void setENumber(int value) {
        eNumber = value;
    }

    int getENumber() {
        return eNumber;
    }

    /**
     * 開始イベントの番号
     */
    public void setEStartIndex(Integer value) {
        eStartIndex = value;
    }

    Integer getEStartIndex() {
        return eStartIndex;
    }

    /**
     * 終了イベントの番号
     */
    public void setEEndIndex(Integer value) {
        eEndIndex = value;
    }

    Integer getEEndIndex() {
        return eEndIndex;
    }

    /**
     * 演奏時専用なのでそれ以外の用途で使っちゃだめ
     */
    public void setENowIndex(Integer value) {
        eNowIndex = value;
    }

    Integer getENowIndex() {
        return eNowIndex;
    }

    //初めのイベントを得る
    public MIDIEvent getStartEvent() {
        if (eStartIndex == null) return null;

        return events.get(eStartIndex);
    }

    /** 最後のイベントを得る */
    public MIDIEvent getEndEvent() {
        if (eEndIndex == null) return null;

        return events.get(eEndIndex);
    }

    /** 指定したイベントの次のイベントを得る */
    public MIDIEvent getNextEvent(MIDIEvent eve) {
        if (eve == null || eve.getAfterIndex() == null) return null;

        return events.get(eve.getAfterIndex());
    }

    /** 指定したイベントの前のイベントを得る */
    public MIDIEvent getPrevEvent(MIDIEvent eve) {
        if (eve == null || eve.getBeforeIndex() == null) return null;

        return events.get(eve.getBeforeIndex());
    }

    /** 指定したイベントを除外する (メモリには残る) */
    public boolean removeEvent(MIDIEvent eve) {
        if (eve == null) return false;
        MIDIEvent pEvent = getPrevEvent(eve);
        MIDIEvent nEvent = getNextEvent(eve);
        if (pEvent != null) pEvent.setAfterIndex((nEvent == null) ? null : nEvent.getNumber());
        if (nEvent != null) nEvent.setBeforeIndex((pEvent == null) ? null : pEvent.getNumber());
        pEvent.setStep(pEvent.getStep() + eve.getStep());
        this.eCounter--;

        return true;
    }

    /** 指定したイベントをメモリから消去する(removeEventに比べ低速) */
    public boolean clearEvent(MIDIEvent eve) {
        if (eve == null) return false;
        MIDIEvent pEvent = getPrevEvent(eve);
        MIDIEvent nEvent = getNextEvent(eve);
        if (pEvent != null) pEvent.setAfterIndex((nEvent == null) ? null : nEvent.getNumber());
        if (nEvent != null) nEvent.setBeforeIndex((pEvent == null) ? null : pEvent.getNumber());
        pEvent.setStep(pEvent.getStep() + eve.getStep());
        this.eCounter--;
        this.eNumber--;

        int num = eve.getNumber();
        this.events.remove(eve);

        for (MIDIEvent evt : this.events) {
            if (evt.getNumber() >= num) evt.setNumber(evt.getNumber() - 1);
            if (evt.getAfterIndex() >= num) evt.setAfterIndex(evt.getAfterIndex() - 1);
            if (evt.getBeforeIndex() >= num) evt.setBeforeIndex(evt.getBeforeIndex() - 1);
        }

        return true;
    }

    /** 全てのイベントをメモリから消去する */
    public void clearAllEventMemory() {
        this.events.clear();
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
     * @param targetEvent このイベントの後ろに新たに入る
     * @param step        Step値
     * @param eventType   イベントタイプ
     * @param midiMessage MIDIメッセージ(Chは0固定であること)
     * @return 新たに挿入したイベント
     */
    public MIDIEvent insertEvent(MIDIEvent targetEvent, int step, MIDIEventType eventType, byte[] midiMessage) {
        if (midiMessage == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(eventType);
        eve.setMIDIMessage(midiMessage);
        eve.setMIDIMessages(null);
        eve.setStep(step);

        insertEve(targetEvent, step, eve);

        return eve;
    }

    /**
     * 指定されたイベントの後ろにイベントを挿入する
     *
     * @param targetEvent このイベントの後ろに新たに入る
     * @param step        step 値
     * @param eventType   イベントタイプ
     * @param midiMessage MIDIメッセージ(Chは0固定であること)
     * @param gt          ゲートタイム
     * @return 新たに挿入したイベント
     */
    public MIDIEvent insertEvent(MIDIEvent targetEvent, int step, MIDIEventType eventType, byte[] midiMessage, int gt) {
        if (midiMessage == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(eventType);
        eve.setMIDIMessage(midiMessage);
        eve.setMIDIMessages(null);
        eve.setStep(step);
        eve.setGate(gt);

        insertEve(targetEvent, step, eve);

        return eve;
    }

    /**
     * 指定されたイベントの後ろにイベントを挿入する
     *
     * @param targetEvent    このイベントの後ろに新たに入る
     * @param step           step 値
     * @param eventType      イベントタイプ
     * @param midiMessageList MIDI メッセージ
     * @return 新たに挿入したイベント
     */
    public MIDIEvent insertSpEvent(MIDIEvent targetEvent, int step, MIDISpEventType eventType, byte[][] midiMessageList) {
        //if (midiMessageList == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(MIDIEventType.MetaSequencerSpecific);
        eve.setMIDIMessage(new byte[] {(byte) eventType.ordinal()});
        eve.setMIDIMessages(midiMessageList);
        eve.setStep(step);

        insertEve(targetEvent, step, eve);

        return eve;
    }

    private void insertEve(MIDIEvent targetEvent, int step, MIDIEvent event) {
        //イベントリストを生成
        if (this.getEvents() == null) {
            this.setEvents(new ArrayList<>());
        }
        if (targetEvent == null || this.getEvents().size() == 0 || this.getEStartIndex() == null) { //初めのイベント
            event.setAfterIndex(null);
            event.setBeforeIndex(null);
            event.setNumber(this.getENumber());
            this.getEvents().add(event);
            this.setEStartIndex(0);
            this.setEEndIndex(0);
            this.eCounter = 1;
            this.setENumber(this.getENumber() + 1);
            return;
        }

        event.setBeforeIndex(targetEvent.getNumber());
        event.setAfterIndex(targetEvent.getAfterIndex());
        event.setNumber(this.getENumber());
        targetEvent.setAfterIndex(event.getNumber());
        this.getEvents().add(event);
        if (event.getAfterIndex() == null) {
            this.setEEndIndex(this.getENumber());
        } else {
            targetEvent = getNextEvent(event);
            targetEvent.setBeforeIndex(event.getNumber());
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
        eve.setMIDIMessages(null);
        eve.setStep(step);

        MIDIEvent lastEvent = this.getEndEvent();
        if (lastEvent == null) { // 初めのイベント
            eve.setAfterIndex(null);
            eve.setBeforeIndex(null);
            eve.setNumber(this.getENumber());
            this.getEvents().add(eve);
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
        this.getEvents().add(eve);
        this.eCounter++;
        this.setENumber(this.getENumber() + 1);
        lastEvent.setAfterIndex(eve.getNumber());
    }
}
