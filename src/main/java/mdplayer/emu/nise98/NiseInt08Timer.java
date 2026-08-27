package mdplayer.emu.nise98;


/**
 * The 8253 counter #0 (I/O $71), the source of INT 08.
 *
 * <p>Only what a music driver touches is modelled: the count written through $71, the counter
 * value read back from it - FMP seeds its random number generator with that value - and the
 * periodic interrupt, whose rate stays the step count it has always been rather than the
 * written count.</p>
 */
class NiseInt08Timer {

    /**
     * PIT ticks per emulated instruction. The counter is clocked at 1.9968MHz on a PC-9801,
     * a quarter of the 7.9872MHz system clock, and an instruction of this 286 costs roughly
     * 8 of those clocks.
     */
    private static final int TICKS_PER_STEP = 2;

    private final Nise286 cpu;
    private boolean enable = false;
    private int counter = 0;
    private int wCounter = 300;
    private int intNumber = 8;

    /** free running PIT tick count */
    private int ticks = 0;
    /** the count written through $71, 0 means 65536 as on the real chip */
    private int reload = 0x10000;
    /** access mode, the RW bits of the mode word: 1 LSB only, 2 MSB only, 3 LSB then MSB */
    private int access = 3;
    /** LSB of a count being written, -1 when no LSB is pending */
    private int writeLsb = -1;
    /** read flip flop, true when the next read of $71 gives the MSB */
    private boolean readMsb = false;
    /** value held by a counter latch command, -1 when nothing is latched */
    private int latched = -1;

    public NiseInt08Timer(Nise286 cpu, int intNumber /* = 8 */) {
        this.cpu = cpu;
        this.intNumber = intNumber;
    }

    public void start() {
        enable = true;
        counter = wCounter;
    }

    /** OUT $71: the count, in the order the access mode asks for */
    public void write(byte data) {
        if (access == 1) {
            setReload((data & 0xff) | (reload & 0xff00));
        } else if (access == 2) {
            setReload(((data & 0xff) << 8) | (reload & 0x00ff));
        } else if (writeLsb < 0) {
            writeLsb = data & 0xff;
        } else {
            setReload(((data & 0xff) << 8) | writeLsb);
            writeLsb = -1;
        }
    }

    private void setReload(int value) {
        reload = value == 0 ? 0x10000 : value;
        ticks = 0;
    }

    /** OUT $77: the mode word, or a counter latch command */
    public void setMode(byte data) {
        if ((data & 0xc0) != 0x00) return; // not counter #0

        int rw = (data >> 4) & 3;
        if (rw == 0) { // counter latch command
            if (latched < 0) latched = count();
            return;
        }

        access = rw;
        writeLsb = -1;
        readMsb = false;
        latched = -1;
    }

    /** IN $71: the counter, in the order the access mode asks for */
    public byte read() {
        int value = latched >= 0 ? latched : count();
        byte ret;
        if (access == 2 || (access == 3 && readMsb)) {
            ret = (byte) (value >> 8);
            readMsb = false;
            latched = -1;
        } else {
            ret = (byte) value;
            if (access == 3) readMsb = true;
            else latched = -1;
        }
        return ret;
    }

    /** the counter counts down from the count and reloads */
    private int count() {
        return reload - (ticks % reload);
    }

    public void stepExecute() {
        ticks += TICKS_PER_STEP;

        if ((cpu.w_mmsk & 0x01) != 0) return;
        if (!enable) return;

        wCounter--;
        if (wCounter > 0) return;
        wCounter = counter;
        cpu.interruptTrigger[intNumber] = true;
    }
}

