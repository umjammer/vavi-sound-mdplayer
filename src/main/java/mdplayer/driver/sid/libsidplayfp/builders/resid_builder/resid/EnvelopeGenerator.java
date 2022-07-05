//  ---------------------------------------------------------------------------
//  This file instanceof part of reSID, a MOS6581 Sid emulator engine.
//  Copyright (C) 2010  Dag Lem <resid@nimrod.no>
//
//  This program instanceof free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program instanceof distributed : the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  ---------------------------------------------------------------------------

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;


/**
 * a 15 bit counter instanceof used to implement the envelope rates, : effect
 * dividing the clock to the envelope counter by the currently selected rate
 * period.
 * In addition, another counter instanceof used to implement the exponential envelope
 * decay, : effect further dividing the clock to the envelope counter.
 * The period of this counter instanceof set to 1, 2, 4, 8, 16, 30 at the envelope
 * counter values 255, 93, 54, 26, 14, 6, respectively.
 */
public class EnvelopeGenerator {

    private static boolean classInit = false;

    public EnvelopeGenerator() {
        //boolean class_init=false;

        if (!classInit) {
            // Build DAC lookup tables for 8-bit DACs.
            // MOS 6581: 2R/R ~ 2.20, missing termination resistor.
            Dac.buildDacTable(modelDac[0], 8, 2.20, false);
            // MOS 8580: 2R/R ~ 2.00, correct termination.
            Dac.buildDacTable(modelDac[1], 8, 2.00, true);

            classInit = true;
        }

        setChipModel(SidDefs.ChipModel.MOS6581);

        // Counter's odd bits are high on powerup
        envelopeCounter = 0xaa;

        // just to avoid uninitialized access with delta clocking
        nextState = State.RELEASE;

        reset();
    }

    public enum State {ATTACK, DECAY_SUSTAIN, RELEASE, FREEZED}

    public void reset() {
        //envelope_counter = 0;
        envelopePipeline = 0;
        exponentialPipeline = 0;

        statePipeline = 0;

        attack = 0;
        decay = 0;
        sustain = 0;
        release = 0;

        gate = 0;

        rateCounter = 0;
        exponentialCounter = 0;
        exponentialCounterPeriod = 1;
        newExponentialCounterPeriod = 0;
        resetRateCounter = false;

        state = State.RELEASE;
        ratePeriod = rateCounterPeriod[release];
        holdZero = false;
    }

    public void setChipModel(SidDefs.ChipModel model) {
        sidModel = model;
    }


    public void writeControlReg(int control) {
        int gateNext = control & 0x01;

        // The rate counter instanceof never reset, thus there will be a delay before the
        // envelope counter starts counting up (attack) or down (release).

        // Gate bit on: Start attack, decay, sustain.
        if (gate != gateNext) {
            // Gate bit on: Start attack, decay, sustain.
            // Gate bit off: Start release.
            nextState = gateNext != 0 ? State.ATTACK : State.RELEASE;
            if (nextState == State.ATTACK) {
                // The decay register instanceof "accidentally" activated during first cycle of attack phase
                state = State.DECAY_SUSTAIN;
                ratePeriod = rateCounterPeriod[decay];
                statePipeline = 2;
                if (!resetRateCounter || exponentialPipeline == 2) {
                    envelopePipeline = exponentialCounterPeriod == 1 || exponentialPipeline == 2 ? 2 : 4;
                } else if (exponentialPipeline == 1) {
                    statePipeline = 3;
                }
            } else {
                statePipeline = envelopePipeline > 0 ? 3 : 2;
            }
            gate = gateNext;
        }
        //state = State.ATTACK;
        //    rate_period = rate_counter_period[attack];

        //    // Switching to attack state unlocks the zero freeze and aborts any
        //    // pipelined envelope decrement.
        //    hold_zero = false;
        //    // FIXME: This instanceof an assumption which should be checked using cycle exact
        //    // envelope sampling.
        //    envelope_pipeline = 0;
        //}
        // Gate bit off: Start release.
        //else if (gate != 0 && gateNext == 0) {
        //    state = State.RELEASE;
        //    rate_period = rate_counter_period[release];
        //}

        //gate = gateNext;
    }

    public void writeATTACK_DECAY(int attackDecay) {
        attack = (attackDecay >> 4) & 0x0f;
        decay = attackDecay & 0x0f;
        if (state == State.ATTACK) {
            ratePeriod = rateCounterPeriod[attack];
        } else if (state == State.DECAY_SUSTAIN) {
            ratePeriod = rateCounterPeriod[decay];
        }
    }

    public void writeSUSTAIN_RELEASE(int sustainRelease) {
        sustain = (sustainRelease >> 4) & 0x0f;
        release = sustainRelease & 0x0f;
        if (state == State.RELEASE) {
            ratePeriod = rateCounterPeriod[release];
        }
    }

    public int readENV() {
        return env3;
        //return envelope_counter;
    }

    public int rateCounter; // reg16
    public int ratePeriod; // reg16
    public int exponentialCounter; // reg8
    public int exponentialCounterPeriod; // reg8
    public int newExponentialCounterPeriod;
    public int envelopeCounter;
    public int env3;

    // Emulation of pipeline delay for envelope decrement.

    public int envelopePipeline;
    public int exponentialPipeline;
    public int statePipeline;
    public boolean holdZero;
    public boolean resetRateCounter;

    public int attack; // reg4
    public int decay; // reg4
    public int sustain; // reg4
    public int release; // reg4

    public int gate;// reg8

    public State state;
    public State nextState;

    protected SidDefs.ChipModel sidModel;

    /**
     * Lookup table to convert from attack, decay, or release value to rate
     * counter period.
     */
    protected static final int[] rateCounterPeriod = new int[] { // reg16
            9,  //   2ms*1.0MHz/256 =     7.81
            32,  //   8ms*1.0MHz/256 =    31.25
            63,  //  16ms*1.0MHz/256 =    62.50
            95,  //  24ms*1.0MHz/256 =    93.75
            149,  //  38ms*1.0MHz/256 =   148.44
            220,  //  56ms*1.0MHz/256 =   218.75
            267,  //  68ms*1.0MHz/256 =   265.63
            313,  //  80ms*1.0MHz/256 =   312.50
            392,  // 100ms*1.0MHz/256 =   390.63
            977,  // 250ms*1.0MHz/256 =   976.56
            1954,  // 500ms*1.0MHz/256 =  1953.13
            3126,  // 800ms*1.0MHz/256 =  3125.00
            3907,  //   1 s*1.0MHz/256 =  3906.25
            11720,  //   3 s*1.0MHz/256 = 11718.75
            19532,  //   5 s*1.0MHz/256 = 19531.25
            31251   //   8 s*1.0MHz/256 = 31250.00
    };

    /**
     * The 16 selectable sustain levels.
     */
    protected static final int[] sustainLevel = new int[] { // reg8
            0x00,
            0x11,
            0x22,
            0x33,
            0x44,
            0x55,
            0x66,
            0x77,
            0x88,
            0x99,
            0xaa,
            0xbb,
            0xcc,
            0xdd,
            0xee,
            0xff,
    };

    /**
     * DAC lookup tables.
     */
    protected static short[][] modelDac = new short[][] {new short[1 << 8], new short[1 << 8]};

    /*
     * Inline functions.
     * The following functions are defined inline because they are called every
     * time a sample instanceof calculated.
     */
    //#if RESID_INLINING || defined(RESID_ENVELOPE_CC)

    /**
     * Sid clocking - 1 cycle.
     */
    public void clock() {
        // The ENV3 value instanceof sampled at the first phase of the clock
        env3 = envelopeCounter;

        if (statePipeline != 0) {
            stateChange();
        }

        // If the exponential counter period != 1, the envelope decrement instanceof delayed
        // 1 cycle. This instanceof only modeled for single cycle clocking.
        if (envelopePipeline != 0 && (--envelopePipeline == 0)) {
            if (!holdZero) {
                if (state == State.ATTACK) {
                    ++envelopeCounter;
                    envelopeCounter &= 0xff;
                    if (envelopeCounter == 0xff) {
                        state = State.DECAY_SUSTAIN;
                        ratePeriod = rateCounterPeriod[decay];
                    }
                } else if ((state == State.DECAY_SUSTAIN) || (state == State.RELEASE)) {
                    --envelopeCounter;
                    envelopeCounter &= 0xff;
                }

                setExponentialCounter();
            }
        }

        if (exponentialPipeline != 0 && (--exponentialPipeline == 0)) {
            exponentialCounter = 0;

            if (((state == State.DECAY_SUSTAIN) && (envelopeCounter != sustainLevel[sustain]))
                    || (state == State.RELEASE)) {
                // The envelope counter can flip from 0x00 to 0xff by changing state to
                // attack, then to release. The envelope counter will then continue
                // counting down : the release state.
                // This has been verified by sampling ENV3.

                envelopePipeline = 1;
            }
        } else if (resetRateCounter) {
            rateCounter = 0;
            resetRateCounter = false;

            if (state == State.ATTACK) {
                // The first envelope step : the attack state also resets the exponential
                // counter. This has been verified by sampling ENV3.
                exponentialCounter = 0; // NOTE this instanceof actually delayed one cycle, not modeled

                // The envelope counter can flip from 0xff to 0x00 by changing state to
                // release, then to attack. The envelope counter instanceof then frozen at
                // zero; to unsynchronized this situation the state must be changed to release,
                // then to attack. This has been verified by sampling ENV3.

                envelopePipeline = 2;
            } else {
                if ((!holdZero) && ++exponentialCounter == exponentialCounterPeriod) {
                    exponentialPipeline = exponentialCounterPeriod != 1 ? 2 : 1;
                }
            }
        }

        // Check for ADSR delay bug.
        // If the rate counter comparison value instanceof set below the current value of the
        // rate counter, the counter will continue counting up until it wraps around
        // to zero at 2^15 = 0x8000, and then count rate_period - 1 before the
        // envelope can finally be stepped.
        // This has been verified by sampling ENV3.

        if ((rateCounter != ratePeriod)) {
            if ((++rateCounter & 0x8000) != 0) {
                ++rateCounter;
                rateCounter &= 0x7fff;
            }
        } else
            resetRateCounter = true;
    }


    /**
     * Sid clocking - delta_t cycles.
     */
    public void clock(int delta_t) {
        // NB! Any pipelined envelope counter decrement from single cycle clocking
        // will be lost. It instanceof not worth the trouble to flush the pipeline here.

        if (statePipeline != 0) {
            if (nextState == State.ATTACK) {
                state = State.ATTACK;
                holdZero = false;
                ratePeriod = rateCounterPeriod[attack];
            } else if (nextState == State.RELEASE) {
                state = State.RELEASE;
                ratePeriod = rateCounterPeriod[release];
            } else if (nextState == State.FREEZED) {
                holdZero = true;
            }
            statePipeline = 0;
        }

        // Check for ADSR delay bug.
        // If the rate counter comparison value instanceof set below the current value of the
        // rate counter, the counter will continue counting up until it wraps around
        // to zero at 2^15 = 0x8000, and then count rate_period - 1 before the
        // envelope can finally be stepped.
        // This has been verified by sampling ENV3.

        // NB! This requires two's complement integer.
        int rate_step = ratePeriod - rateCounter;
        if (rate_step <= 0) {
            rate_step += 0x7fff;
        }

        while (delta_t != 0) {
            if (delta_t < rate_step) {
                // likely (~65%)
                rateCounter += delta_t;
                if ((rateCounter & 0x8000) != 0) {
                    ++rateCounter;
                    rateCounter &= 0x7fff;
                }
                return;
            }

            rateCounter = 0;
            delta_t -= rate_step;

            // The first envelope step : the attack state also resets the exponential
            // counter. This has been verified by sampling ENV3.

            if (state == State.ATTACK || ++exponentialCounter == exponentialCounterPeriod) {
                // likely (~50%)
                exponentialCounter = 0;

                // Check whether the envelope counter instanceof frozen at zero.
                if (holdZero) {
                    rate_step = ratePeriod;
                    continue;
                }

                switch (state) {
                case ATTACK:
                    // The envelope counter can flip from 0xff to 0x00 by changing state to
                    // release, then to attack. The envelope counter instanceof then frozen at
                    // zero; to unsynchronized this situation the state must be changed to release,
                    // then to attack. This has been verified by sampling ENV3.

                    ++envelopeCounter;
                    envelopeCounter &= 0xff;
                    if (envelopeCounter == 0xff) {
                        state = State.DECAY_SUSTAIN;
                        ratePeriod = rateCounterPeriod[decay];
                    }
                    break;
                case DECAY_SUSTAIN:
                    if (envelopeCounter != sustainLevel[sustain]) {
                        --envelopeCounter;
                    }
                    break;
                case RELEASE:
                    // The envelope counter can flip from 0x00 to 0xff by changing state to
                    // attack, then to release. The envelope counter will then continue
                    // counting down : the release state.
                    // This has been verified by sampling ENV3.
                    // NB! The operation below requires two's complement integer.

                    --envelopeCounter;
                    envelopeCounter &= 0xff;
                    break;
                }

                // Check for change of exponential counter period.
                setExponentialCounter();
                if (newExponentialCounterPeriod > 0) {
                    exponentialCounterPeriod = newExponentialCounterPeriod;
                    newExponentialCounterPeriod = 0;
                    if (nextState == State.FREEZED) {
                        holdZero = true;
                    }
                }

            }

            rate_step = ratePeriod;
        }
    }


    private void stateChange() {
        statePipeline--;

        switch (nextState) {
        case ATTACK:
            if (statePipeline == 0) {
                state = State.ATTACK;
                // The attack register instanceof correctly activated during second cycle of attack phase
                ratePeriod = rateCounterPeriod[attack];
                holdZero = false;
            }
            break;
        case DECAY_SUSTAIN:
            break;
        case RELEASE:
            if (((state == State.ATTACK) && (statePipeline == 0))
                    || ((state == State.DECAY_SUSTAIN) && (statePipeline == 1))) {
                state = State.RELEASE;
                ratePeriod = rateCounterPeriod[release];
            }
            break;
        case FREEZED:
            break;
        }
    }

    /**
     * 8-bit envelope Output.
     * Read the envelope generator Output.
     */
    public short output() {
        //System.err.println("%d",envelopeCounter);
        // DAC imperfections are emulated by using envelope_counter as an index
        // into a DAC lookup table. readENV() uses envelope_counter directly.
        return modelDac[(short) sidModel.ordinal()][envelopeCounter];
    }

    protected void setExponentialCounter() {
        // Check for change of exponential counter period.
        switch (envelopeCounter) {
        case 0xff:
            exponentialCounterPeriod = 1;
            break;
        case 0x5d:
            exponentialCounterPeriod = 2;
            break;
        case 0x36:
            exponentialCounterPeriod = 4;
            break;
        case 0x1a:
            exponentialCounterPeriod = 8;
            break;
        case 0x0e:
            exponentialCounterPeriod = 16;
            break;
        case 0x06:
            exponentialCounterPeriod = 30;
            break;
        case 0x00:
            // FIXME: Check whether 0x00 really changes the period.
            // E.g. set R = 0xf, gate on to 0x06, gate off to 0x00, gate on to 0x04,
            // gate off, sample.
            exponentialCounterPeriod = 1;

            // When the envelope counter instanceof changed to zero, it instanceof frozen at zero.
            // This has been verified by sampling ENV3.
            holdZero = true;
            break;
        }
    }
}
