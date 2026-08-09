package mdplayer.lib.rcp;

import java.io.Serializable;
import java.util.NoSuchElementException;


public class MIDIEvent implements Serializable {
    private Integer beforeIndex = null;
    private Integer afterIndex = null;
    private int number = 0;
    private int step = 0;
    private Integer sameMeasureIndex = null;
    private MIDIEventType eventType;
    private byte[] midiMessage = null;
    private byte[][] midiMessages = null;
    private int gate = 0;
    private int gs = 0;

    public void setBeforeIndex(Integer value) {
        beforeIndex = value;
    }

    Integer getBeforeIndex() {
        return beforeIndex;
    }

    public void setAfterIndex(Integer value) {
        afterIndex = value;
    }

    public Integer getAfterIndex() {
        return afterIndex;
    }

    public void setNumber(int value) {
        number = value;
    }

    public int getNumber() {
        return number;
    }

    public void setSameMeasureIndex(int value) {
        sameMeasureIndex = value;
    }

    public Integer getSameMeasureIndex() {
        return sameMeasureIndex;
    }

    public void setStep(int value) {
        step = value;
    }

    public int getStep() {
        return step;
    }

    public void setEventType(MIDIEventType value) {
        eventType = value;
    }

    public MIDIEventType getEventType() {
        return eventType;
    }

    public void setMIDIMessage(byte[] value) {
        midiMessage = value;
    }

    public byte[] getMIDIMessage() {
        return midiMessage;
    }

    public void setMIDIMessages(byte[][] value) {
        midiMessages = value;
    }

    public byte[][] getMIDIMessages() {
        return midiMessages;
    }

    public void setGate(int value) {
        gate = value;
    }

    public int getGate() {
        return gate;
    }

    public void setGosa(int value) {
        gs = value;
    }

    int getGosa() {
        return gs;
    }

    public enum MIDIEventType {
        MetaSeqNumber(0x00),
        MetaTextEvent(0x01),
        MetaCopyrightNotice(0x02),
        MetaTrackName(0x03),
        MetaInstrumentName(0x04),
        MetaLyric(0x05),
        MetaMarker(0x06),
        MetaCuePoint(0x07),
        MetaProgramName(0x08),
        MetaDeviceName(0x09),
        MetaChannelPrefix(0x20),
        MetaPortPrefix(0x21),
        MetaEndOfTrack(0x2F),
        MetaTempo(0x51),
        MetaSMPTEOffset(0x54),
        MetaTimeSignature(0x58),
        MetaKeySignature(0x59),
        MetaSequencerSpecific(0x7F),
        NoteOff(0x80),
        NoteON(0x90),
        KeyAfterTouch(0xA0),
        ControlChange(0xB0),
        ProgramChange(0xC0),
        ChannelAfterTouch(0xD0),
        PitchBend(0xE0),
        SysExF0(0xF0),
        SysExF7(0xF7);
        public final int v;

        MIDIEventType(int v) {
            this.v = v;
        }
    }

    public enum MIDISpEventType {
        UserExclusive1(0x90),
        UserExclusive2(0x91),
        UserExclusive3(0x92),
        UserExclusive4(0x93),
        UserExclusive5(0x94),
        UserExclusive6(0x95),
        UserExclusive7(0x96),
        UserExclusive8(0x97),
        ChExclusive(0x98),
        OutSideProc(0x99),
        BankProgram(0xE2),
        KeyScan(0xE5),
        MIDICh(0xE6),
        TempoChange(0xE7),
        RolandBase(0xDD),
        RolandPara(0xDE),
        RolandDevice(0xDF),
        KeyChange(0xF5),
        Comment(0xF6),
        LoopEnd(0xF8),
        LoopStart(0xF9),
        SameMeasure(0xFC),
        MeasureEnd(0xFD),
        EndOfTrack(0xFE);
        public final int v;

        MIDISpEventType(int v) {
            this.v = v;
        }

        public static MIDISpEventType valueOf(int v) {
            for (MIDISpEventType c : values()) {
                if (c.v == v) return c;
            }
            throw new NoSuchElementException(String.valueOf(v));
        }
    }

    public static class MIDIRhythm {
        private String name = "";
        private int key = 0;
        private int gt = 1;

        public void setName(String value) {
            name = value;
        }

        String getName() {
            return name;
        }

        public void setKey(int value) {
            key = value;
        }

        int getKey() {
            return key;
        }

        public void setGt(int value) {
            gt = value;
        }

        int getGt() {
            return gt;
        }
    }

    static class MIDIUserExclusive {
        private String name = "";
        private String memo = "";
        private byte[] exclusive = null;

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

        public void setExclusive(byte[] value) {
            exclusive = value;
        }

        byte[] getExclusive() {
            return exclusive;
        }
    }
}

