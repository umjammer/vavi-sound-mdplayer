package mdplayer.driver.mndrv;

import mdplayer.Common;


public class FMTimer {

    /** Upper 8 bits of Timer A */
    private int timerARegH;
    /** The lower 2 bits of Timer A */
    private int timerARegL;
    /** Timer A overflow setting */
    private int timerA;
    /** Timer A counter value */
    private double timerACounter;
    /** Timer B overflow setting value */
    private int timerB;
    /** Timer B counter value */
    private double timerBCounter;
    /** Timer control register (lower 4 bits + 7 bits) */
    private int timerReg;
    /** Status register (lowest 2 bits) */
    private int statReg;
    private final boolean isOPM;
    private final Runnable csmKeyOn;
    private final double step;
    private final double masterClock;

    public FMTimer(boolean isOPM, Runnable csmKeyOn, double masterClock) {
        this.isOPM = isOPM;
        this.csmKeyOn = csmKeyOn;
        this.masterClock = masterClock;
        if (isOPM) {
            step = masterClock / 64.0 / 1.0 / (double) Common.VGMProcSampleRate;
        } else {
            step = masterClock / 72.0 / 2.0 / (double) Common.VGMProcSampleRate;
        }
    }

    public void timer() {
        int flag_set = 0;

        if ((timerReg & 0x01) != 0) { // TimerA is running
            timerACounter += step;
            if (timerACounter >= timerA) {
                flag_set |= ((timerReg >> 2) & 0x01);
                timerACounter -= timerA;
                if ((timerReg & 0x80) != 0) csmKeyOn.run();
            }
        }

        if ((timerReg & 0x02) != 0) { // TimerB is running
            timerBCounter += step;
            if (timerBCounter >= timerB) {
                flag_set |= ((timerReg >> 2) & 0x02);
                timerBCounter -= timerB;
            }
        }

        statReg |= flag_set;
    }

    public void writeReg(byte adr, byte data) {
        if (isOPM) writeRegOPM(adr, data);
        else writeRegOPN(adr, data);
    }

    private void writeRegOPM(byte adr, byte data) {
        switch (adr) {
            case 0x10:
            case 0x11:
                // timerA
                if (adr == 0x10) timerARegH = data;
                else timerARegL = data & 3;
                timerA = 1024 - ((timerARegH << 2) + timerARegL);
                break;

            case 0x12:
                // timerB
                timerB = (256 - (int) data) << (10 - 6);
                break;

            case 0x14:
                // Timer Control Register
                timerReg = data & 0x8F;
                statReg &= 0xff - ((data >> 4) & 3);
                break;
        }
    }

    private void writeRegOPN(byte adr, byte data) {
        switch (adr) {
            case 0x24:
            case 0x25:
                // timerA
                if (adr == 0x24) timerARegH = data;
                else timerARegL = data & 3;
                timerA = 1024 - ((timerARegH << 2) + timerARegL);
                break;

            case 0x26:
                // timerB
                timerB = (256 - (int) data) << (10 - 6);
                break;

            case 0x27:
                // Timer Control Register
                timerReg = data & 0x8F;
                statReg &= 0xff - ((data >> 4) & 3);
                break;
        }
    }

    public int readStatus() {
        return statReg;
    }
}
