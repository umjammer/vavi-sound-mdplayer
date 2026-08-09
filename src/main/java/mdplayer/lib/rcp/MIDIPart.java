package mdplayer.lib.rcp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mdplayer.lib.rcp.MIDIEvent.MIDIEventType;
import mdplayer.lib.rcp.MIDIEvent.MIDISpEventType;


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

    void setAfterIndex(Integer value) {
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

    void setStartTick(int value) {
        startTick = value;
    }

    int getStartTick() {
        return startTick;
    }

    /**
     * Event List
     */
    public void setEvents(List<MIDIEvent> value) {
        events = value;
    }

    public List<MIDIEvent> getEvents() {
        return events;
    }

    /**
     * Number of valid events
     */
    private int eCounter = 0;

    /**
     * Event sequence number
     */
    public void setENumber(int value) {
        eNumber = value;
    }

    int getENumber() {
        return eNumber;
    }

    /**
     * Start event number
     */
    public void setEStartIndex(Integer value) {
        eStartIndex = value;
    }

    public Integer getEStartIndex() {
        return eStartIndex;
    }

    /**
     * End event number
     */
    public void setEEndIndex(Integer value) {
        eEndIndex = value;
    }

    Integer getEEndIndex() {
        return eEndIndex;
    }

    /**
     * It is for performance only and should not be used for any other purposes.
     */
    public void setENowIndex(Integer value) {
        eNowIndex = value;
    }

    public Integer getENowIndex() {
        return eNowIndex;
    }

    /** Gets the first event. */
    public MIDIEvent getStartEvent() {
        if (eStartIndex == null) return null;

        return events.get(eStartIndex);
    }

    /** Gets the last event */
    public MIDIEvent getEndEvent() {
        if (eEndIndex == null) return null;

        return events.get(eEndIndex);
    }

    /** Gets the next event after the specified event. */
    public MIDIEvent getNextEvent(MIDIEvent eve) {
        if (eve == null || eve.getAfterIndex() == null) return null;

        return events.get(eve.getAfterIndex());
    }

    /** Gets the event before the specified event. */
    public MIDIEvent getPrevEvent(MIDIEvent eve) {
        if (eve == null || eve.getBeforeIndex() == null) return null;

        return events.get(eve.getBeforeIndex());
    }

    /** Exclude the specified events (but they remain in memory) */
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

    /** Removes the specified event from memory (slower than removeEvent) */
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

    /** Clear all events from memory */
    public void clearAllEventMemory() {
        this.events.clear();
        this.eCounter = 0;
        this.setEStartIndex(null);
        this.setEEndIndex(null);
        this.setENumber(0);
    }

    /** Clear all events */
    public void clearEvent() {
        this.eCounter = 0;
        this.setEStartIndex(null);
        this.setEEndIndex(null);
    }

    /**
     * Inserts an event after the specified event.
     *
     * @param targetEvent New entry after this event
     * @param step        Step value
     * @param eventType   Event Type
     * @param midiMessage MIDI message (Ch must be fixed at 0)
     * @return Newly added events
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
     * Inserts an event after the specified event.
     *
     * @param targetEvent New entry after this event
     * @param step        step value
     * @param eventType   Event Type
     * @param midiMessage MIDI message (Ch must be fixed at 0)
     * @param gt          Gate Time
     * @return Newly added events
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
     * Inserts an event after the specified event.
     *
     * @param targetEvent    New entry after this event
     * @param step           step value
     * @param eventType      Event Type
     * @param midiMessageList MIDI message
     * @return Newly added events
     */
    public MIDIEvent insertSpEvent(MIDIEvent targetEvent, int step, MIDISpEventType eventType, byte[][] midiMessageList) {
        //if (midiMessageList == null) return null;
        MIDIEvent eve = new MIDIEvent();
        eve.setEventType(MIDIEventType.MetaSequencerSpecific);
        eve.setMIDIMessage(new byte[] {(byte) eventType.v});
        eve.setMIDIMessages(midiMessageList);
        eve.setStep(step);

        insertEve(targetEvent, step, eve);

        return eve;
    }

    private void insertEve(MIDIEvent targetEvent, int step, MIDIEvent event) {
         // Generate an event list
        if (this.getEvents() == null) {
            this.setEvents(new ArrayList<>());
        }
        if (targetEvent == null || this.getEvents().isEmpty() || this.getEStartIndex() == null) {  // First event
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
     * Add after the last event without considering the Tick.
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
        if (lastEvent == null) { // First event
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
