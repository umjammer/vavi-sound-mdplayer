package mdplayer.emu.nise68;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.System.getLogger;


// @see "Outside X68000"
public class MidiBoard {

    private static final Logger logger = getLogger(MidiBoard.class.getName());

    private int num = 0; // Interface Number
    private final BiFunction<Integer, Byte, Integer> midi;
    private byte group = 0;
    private byte interrupt = 0;
    private byte vect = 0;
    private byte intMask = (byte) 0xff;

    private final byte[] reg = new byte[0x100];
    private Consumer<Byte>[] cmdw = null;
    private Supplier<Byte>[] cmdr = null;
    private int generalTimerValue = 0;
    private int midiClockTimerValue = 0;
    private int renderingFreq;
    // private double clkM = 4_915_200.0 / 8.0;// 1_000_000.0;
    private double clkM = 1_000_000.0;
    private double stepM;
    private double generalTimerValueWrk = 0.0;
    private double clickCounter = 1.0;

    @SuppressWarnings("unchecked")
    public MidiBoard(int num, int renderingFreq, BiFunction<Integer, Byte, Integer> midi) {
        this.num = num;
        this.midi = midi;
        cmdw = Arrays.<Consumer<Byte>>asList(
                // 0x00-
                null, null, null, null, this::setVectH, this::setIntModeControl, this::setIntEnable, null,
                null, null, null, null, null, null, null, null,
                // 0x10-
                null, null, null, null, this::setMIDIRealtimeMessageControl, null, null, null,
                null, null, null, null, null, null, null, null,
                // 0x20-
                null, null, null, null, this::setRxCommRate, this::setRxCommMode, null, null,
                null, null, null, null, null, null, null, null,
                // 0x30-
                null, null, null, null, null, this::setFIFO_RXControl, null, null,
                null, null, null, null, null, null, null, null,
                // 0x40-
                null, null, null, null, this::setTxCommRate, null, null, null,
                null, null, null, null, null, null, null, null,
                // 0x50-
                null, null, null, null, null, this::setFIFO_TXControl, this::setFIFO_TxData, null,
                null, null, null, null, null, null, null, null,
                // 0x60-
                null, null, null, null, null, this::setFSKControl, this::setClickCounterControl, this::setClickCounter,
                null, null, null, null, null, null, null, null,
                // 0x70-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x80-
                null, null, null, null,
                this::setGeneralTimerValueL, this::setGeneralTimerValueH,
                this::setMIDIClockTimerValueL, this::setMIDIClockTimerValueH,
                null, null, null, null, null, null, null, null,
                // 0x90-
                null, null, null, null, this::setExternalIODirection, null, null, null,
                null, null, null, null, null, null, null, null,
                // 0xa0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xb0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xc0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xd0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xe0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xf0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        ).toArray(Consumer[]::new);
        cmdr = new Supplier[] {
                // 0x00-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x10-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x20-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x30-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x40-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x50-
                null, null, null, null, MidiBoard::getFIFI_TxStatus, null, null, null, null, null, null, null, null, null, null, null,
                // 0x60-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x70-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x80-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0x90-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xa0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xb0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xc0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xd0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xe0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                // 0xf0-
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        };

        setClock(renderingFreq);
    }

    public void setClock(int renderingFreq) {
        this.renderingFreq = renderingFreq;
        stepM = clkM / (double) renderingFreq;
    }

    public byte read(byte ptr) {
        int n = (ptr & 0x10) != 0 ? 1 : 0;
        if (num != n) {
            throw new IndexOutOfBoundsException();
        }

        int c = ptr & 0xf;
        byte dat = 0;
        if (c == 0x3) { // rgr
            return dat;
//            throw new Exception(); // R01 is write only.
        } else if (c == 0x7) { //
        } else if (c > 0x8) {
            // R04,14,24,34,44,54,64,74,84,94 grp4
            // R05,15,25,35,45,55,65,75,85,95
            // R06,16,26,36,56,66,76,86,96 grp6
            // R17,27,67,77,87
            int r = (group << 4) + ((c - 1) >> 1);
            if (cmdr[r] != null) dat = cmdr[r].get();
            else throw new UnsupportedOperationException("not implemented yet R%02x".formatted(r));
        } else {
            // R00,10,20,30,40,50,60,70,80,90
            int r = (group << 4) + ((c >> 1));
            throw new UnsupportedOperationException("not implemented yet R%02x".formatted(r));
        }
        logger.log(Level.TRACE, "Read CZ-6BM1 %s Adr:$00ea_fa%02x Dat:$%02x".formatted(num == 0 ? "Pri" : "Sec", ptr & 0xff, dat & 0xff));
        return dat;
    }

    public boolean write(byte ptr, byte dat) {
        int n = (ptr & 0x10) != 0 ? 1 : 0;
        if (num != n) {
            throw new IndexOutOfBoundsException();
        }

        int c = ptr & 0xf;
        //logger.log(Level.TRACE, "Write CZ-6BM1 %s Adr:$%08x Dat:$%02x".formatted(n == 0 ? "Pri" : "Sec", ptr & 0xff, dat & 0xff));

        if (c == 0x1) { // R00
            throw new IllegalStateException(); // R00 is read only.
        } else if (c == 0x3) { // R01 rgr
            group = (byte) (dat & 0xf);
            if ((dat & 0x80) != 0) {
                reset();
            }
        } else if (c == 0x5) { // R02
            throw new IllegalStateException(); // R02 is read only.
        } else if (c == 0x7) { // R03
            interrupt = dat;
        } else if (c > 0x8) {
            // R04,14,24,34,44,54,64,74,84,94 grp4
            // R05,15,25,35,45,55,65,75,85,95
            // R06,16,26,36,56,66,76,86,96 grp6
            // R17,27,67,77,87
            int r = (group << 4) + ((c - 1) >> 1);
            reg[r] = dat;
            if (cmdw[r] != null) cmdw[r].accept(dat);
            else throw new UnsupportedOperationException("not implemented yet R%02x : %02x".formatted(r, dat & 0xff));
        } else {
            throw new UnsupportedOperationException();
        }
        return false;
    }

    public void timer() {

    }

    public boolean intTimer() {
        generalTimerValueWrk -= stepM; // / clickCounter;
        boolean ret = false;
        while (generalTimerValueWrk <= 0.0) {
            generalTimerValueWrk += (generalTimerValue & 0x3fff) << 3;
            ret = true;
        }
        return ret;
    }

    private void setGeneralTimerValueL(byte obj) {
        generalTimerValue &= 0b1011_1111_0000_0000;
        generalTimerValue |= obj & 0xff;
    }

    private void setGeneralTimerValueH(byte obj) {
        generalTimerValue &= 0b0000_0000_1111_1111;
        generalTimerValue |= ((obj & 0xff) << 8) & 0b1011_1111_0000_0000;
    }

    private void setMIDIClockTimerValueL(byte obj) {
        midiClockTimerValue &= 0b1011_1111_0000_0000;
        midiClockTimerValue |= obj & 0xff;
    }

    private void setMIDIClockTimerValueH(byte obj) {
        midiClockTimerValue &= 0b0000_0000_1111_1111;
        midiClockTimerValue |= ((obj & 0xff) << 8) & 0b1011_1111_0000_0000;
    }

    private void reset() {
        intMask = (byte) 0xff;
    }

    // R04
    private void setVectH(byte dat) {
        vect = (byte) ((vect & 0x1f) | (dat & 0xe0));
    }

    // R05
    private void setIntModeControl(byte dat) {
        // Ignore for now
    }

    // R06
    private void setIntEnable(byte dat) {
        intMask = (byte) ~dat;
    }

    // R14
    private void setMIDIRealtimeMessageControl(byte dat) {
        // Ignore for now
    }

    // R24
    private void setRxCommRate(byte dat) {
        // Ignore for now
    }

    // R25
    private void setRxCommMode(byte dat) {
        // Ignore for now
    }

    // R35
    private void setFIFO_RXControl(byte dat) {
        // Ignore for now
    }

    // R44
    private void setTxCommRate(byte dat) {
        // Ignore for now
    }

    // R55
    private void setFIFO_TXControl(byte dat) {
        // Ignore for now
    }

    // R56
    private void setFIFO_TxData(byte dat) {
        logger.log(Level.TRACE, "CZ-6BM1 %s Send MIDI Data:$%02x".formatted(num == 0 ? "Pri" : "Sec", dat & 0xff));
        midi.apply(num, dat);
    }

    // R65
    private void setFSKControl(byte dat) {
        // Ignore for now
    }

    // R66
    private void setClickCounterControl(byte dat) {
        if ((dat & 2) != 0) clkM = 1_000_000.0;
        else clkM = 1_000_000.0 / 2.0;
        stepM = clkM / (double) renderingFreq;
    }

    // R67
    private void setClickCounter(byte dat) {
        // Ignore for now
        clickCounter = dat & 0x7f; // It doesn't seem related...
    }

    // R94
    private void setExternalIODirection(byte dat) {
        // Ignore for now
    }

    private static byte getFIFI_TxStatus() {
        // bit 7: 1 Transmit FIFO is empty
        // bit 6: 1 Transmit FIFO is free
        return (byte) 0b1100_0000;
    }
}
