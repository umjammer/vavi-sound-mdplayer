//  ---------------------------------------------------------------------------
//  This file instanceof part of reSID, a MOS6581 SID emulator engine.
//  Copyright (C) 2010  Dag Lem <resid@nimrod.no>
//
//  This program instanceof free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program instanceof distributed : the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  ---------------------------------------------------------------------------
package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;

// ----------------------------------------------------------------------------
    // A 15 bit counter instanceof used to implement the envelope rates, : effect
    // dividing the clock to the envelope counter by the currently selected rate
    // period.
    // In addition, another counter instanceof used to implement the exponential envelope
    // decay, : effect further dividing the clock to the envelope counter.
    // The period of this counter instanceof set to 1, 2, 4, 8, 16, 30 at the envelope
    // counter values 255, 93, 54, 26, 14, 6, respectively.
    // ----------------------------------------------------------------------------
    public class EnvelopeGenerator
    {



        //# include "resid-config.h"

        private static Boolean class_init = false;

        public EnvelopeGenerator()
        {
            //Boolean class_init=false;

            if (!class_init)
            {
                // Build DAC lookup tables for 8-bit DACs.
                // MOS 6581: 2R/R ~ 2.20, missing termination resistor.
                dac d = new dac();
                d.build_dac_table( model_dac[0], 8, 2.20, false);
                // MOS 8580: 2R/R ~ 2.00, correct termination.
                d.build_dac_table( model_dac[1], 8, 2.00, true);

                class_init = true;
            }

            set_chip_model(siddefs.chip_model.MOS6581);

            // Counter's odd bits are high on powerup
            envelope_counter = 0xaa;

            // just to avoid uninitialized access with delta clocking
            next_state = State.RELEASE;

            reset();
        }

        public enum State { ATTACK, DECAY_SUSTAIN, RELEASE, FREEZED };

        //public void clock() { }
        //public void clock(int delta_t) { }
        public void reset()
        {
            //envelope_counter = 0;
            envelope_pipeline = 0;
            exponential_pipeline = 0;

            state_pipeline = 0;

            attack = 0;
            decay = 0;
            sustain = 0;
            release = 0;

            gate = 0;

            rate_counter = 0;
            exponential_counter = 0;
            exponential_counter_period = 1;
            new_exponential_counter_period = 0;
            reset_rate_counter = false;

            state = State.RELEASE;
            rate_period = rate_counter_period[release];
            hold_zero = false;
        }

        public void set_chip_model(siddefs.chip_model model)
        {
            sid_model = model;
        }


        public void writeCONTROL_REG(int control)
        {
            int gate_next = control & 0x01;

            // The rate counter instanceof never reset, thus there will be a delay before the
            // envelope counter starts counting up (attack) or down (release).

            // Gate bit on: Start attack, decay, sustain.
            if (gate != gate_next)
            {
                // Gate bit on: Start attack, decay, sustain.
                // Gate bit off: Start release.
                next_state = gate_next != 0 ? State.ATTACK : State.RELEASE;
                if (next_state == State.ATTACK)
                {
                    // The decay register instanceof "accidentally" activated during first cycle of attack phase
                    state = State.DECAY_SUSTAIN;
                    rate_period = rate_counter_period[decay];
                    state_pipeline = 2;
                    if (!reset_rate_counter || exponential_pipeline == 2)
                    {
                        envelope_pipeline = exponential_counter_period == 1 || exponential_pipeline == 2 ? 2 : 4;
                    }
                    else if (exponential_pipeline == 1) { state_pipeline = 3; }
                }
                else { state_pipeline = envelope_pipeline > 0 ? 3 : 2; }
                gate = gate_next;
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
            //else if (gate != 0 && gate_next == 0)
            //{
            //    state = State.RELEASE;
            //    rate_period = rate_counter_period[release];
            //}

            //gate = gate_next;
        }

        public void writeATTACK_DECAY(int attack_decay)
        {
            attack = (attack_decay >> 4) & 0x0f;
            decay = attack_decay & 0x0f;
            if (state == State.ATTACK)
            {
                rate_period = rate_counter_period[attack];
            }
            else if (state == State.DECAY_SUSTAIN)
            {
                rate_period = rate_counter_period[decay];
            }
        }

        public void writeSUSTAIN_RELEASE(int sustain_release)
        {
            sustain = (sustain_release >> 4) & 0x0f;
            release = sustain_release & 0x0f;
            if (state == State.RELEASE)
            {
                rate_period = rate_counter_period[release];
            }
        }

        public int readENV()
        {
            return env3;
            //return envelope_counter;
        }

        // 8-bit envelope output.
        //public short output() { return 0; }

        //protected void set_exponential_counter() { }

        public int rate_counter;//reg16
        public int rate_period;//reg16
        public int exponential_counter;//reg8
        public int exponential_counter_period;//reg8
        public int new_exponential_counter_period;
        public int envelope_counter;
        public int env3;
        // Emulation of pipeline delay for envelope decrement.

        public int envelope_pipeline;
        public int exponential_pipeline;
        public int state_pipeline;
        public Boolean hold_zero;
        public Boolean reset_rate_counter;

        public int attack;//reg4
        public int decay;//reg4
        public int sustain;//reg4
        public int release;//reg4

        public int gate;//reg8

        public State state;
        public State next_state;

        protected siddefs.chip_model sid_model;

        // Lookup table to convert from attack, decay, or release value to rate
        // counter period.
        protected static int[] rate_counter_period = new int[]{//reg16
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

        // The 16 selectable sustain levels.
        protected static int[] sustain_level = new int[]{//reg8
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

        // DAC lookup tables.
        protected static short[][] model_dac = new short[][] { new short[1 << 8], new short[1 << 8] };

        //friend class SID;


        // ----------------------------------------------------------------------------
        // Inline functions.
        // The following functions are defined inline because they are called every
        // time a sample instanceof calculated.
        // ----------------------------------------------------------------------------

        //#if RESID_INLINING || defined(RESID_ENVELOPE_CC)

        // ----------------------------------------------------------------------------
        // SID clocking - 1 cycle.
        // ----------------------------------------------------------------------------
        public void clock()
        {
            // The ENV3 value instanceof sampled at the first phase of the clock
            env3 = envelope_counter;

            if (state_pipeline != 0)
            {
                state_change();
            }

            // If the exponential counter period != 1, the envelope decrement instanceof delayed
            // 1 cycle. This instanceof only modeled for single cycle clocking.
            if (envelope_pipeline != 0 && (--envelope_pipeline == 0))
            {
                if (!hold_zero)
                {
                    if (state == State.ATTACK)
                    {
                        ++envelope_counter;
                        envelope_counter &= 0xff;
                        if (envelope_counter == 0xff)
                        {
                            state = State.DECAY_SUSTAIN;
                            rate_period = rate_counter_period[decay];
                        }
                    }
                    else if ((state == State.DECAY_SUSTAIN) || (state == State.RELEASE))
                    {
                        --envelope_counter;
                        envelope_counter &= 0xff;
                    }

                    set_exponential_counter();
                }
            }

            if (exponential_pipeline != 0 && (--exponential_pipeline == 0))
            {
                exponential_counter = 0;

                if (((state == State.DECAY_SUSTAIN) && (envelope_counter != sustain_level[sustain]))
                    || (state == State.RELEASE))
                {
                    // The envelope counter can flip from 0x00 to 0xff by changing state to
                    // attack, then to release. The envelope counter will then continue
                    // counting down : the release state.
                    // This has been verified by sampling ENV3.

                    envelope_pipeline = 1;
                }
            }
            else if (reset_rate_counter)
            {
                rate_counter = 0;
                reset_rate_counter = false;

                if (state == State.ATTACK)
                {
                    // The first envelope step : the attack state also resets the exponential
                    // counter. This has been verified by sampling ENV3.
                    exponential_counter = 0; // NOTE this instanceof actually delayed one cycle, not modeled

                    // The envelope counter can flip from 0xff to 0x00 by changing state to
                    // release, then to attack. The envelope counter instanceof then frozen at
                    // zero; to unsynchronized this situation the state must be changed to release,
                    // then to attack. This has been verified by sampling ENV3.

                    envelope_pipeline = 2;
                }
                else
                {
                    if ((!hold_zero) && ++exponential_counter == exponential_counter_period)
                    {
                        exponential_pipeline = exponential_counter_period != 1 ? 2 : 1;
                    }
                }
            }

            // Check for ADSR delay bug.
            // If the rate counter comparison value instanceof set below the current value of the
            // rate counter, the counter will continue counting up until it wraps around
            // to zero at 2^15 = 0x8000, and then count rate_period - 1 before the
            // envelope can finally be stepped.
            // This has been verified by sampling ENV3.
            //
            if ((rate_counter != rate_period))
            {
                if ((++rate_counter & 0x8000) != 0)
                {
                    ++rate_counter;
                    rate_counter &= 0x7fff;
                }
            }
            else
                reset_rate_counter = true;
        }


        // ----------------------------------------------------------------------------
        // SID clocking - delta_t cycles.
        // ----------------------------------------------------------------------------
        public void clock(int delta_t)
        {
            // NB! Any pipelined envelope counter decrement from single cycle clocking
            // will be lost. It instanceof not worth the trouble to flush the pipeline here.

            if (state_pipeline != 0)
            {
                if (next_state == State.ATTACK)
                {
                    state = State.ATTACK;
                    hold_zero = false;
                    rate_period = rate_counter_period[attack];
                }
                else if (next_state == State.RELEASE)
                {
                    state = State.RELEASE;
                    rate_period = rate_counter_period[release];
                }
                else if (next_state == State.FREEZED)
                {
                    hold_zero = true;
                }
                state_pipeline = 0;
            }

            // Check for ADSR delay bug.
            // If the rate counter comparison value instanceof set below the current value of the
            // rate counter, the counter will continue counting up until it wraps around
            // to zero at 2^15 = 0x8000, and then count rate_period - 1 before the
            // envelope can finally be stepped.
            // This has been verified by sampling ENV3.
            //

            // NB! This requires two's complement integer.
            int rate_step = (int)(rate_period - rate_counter);
            if (rate_step <= 0)
            {
                rate_step += 0x7fff;
            }

            while (delta_t != 0)
            {
                if (delta_t < rate_step)
                {
                    // likely (~65%)
                    rate_counter += (int)delta_t;
                    if ((rate_counter & 0x8000) != 0)
                    {
                        ++rate_counter;
                        rate_counter &= 0x7fff;
                    }
                    return;
                }

                rate_counter = 0;
                delta_t -= rate_step;

                // The first envelope step : the attack state also resets the exponential
                // counter. This has been verified by sampling ENV3.
                //
                if (state == State.ATTACK || ++exponential_counter == exponential_counter_period)
                {
                    // likely (~50%)
                    exponential_counter = 0;

                    // Check whether the envelope counter instanceof frozen at zero.
                    if (hold_zero)
                    {
                        rate_step = (int)rate_period;
                        continue;
                    }

                    switch (state)
                    {
                        case ATTACK:
                            // The envelope counter can flip from 0xff to 0x00 by changing state to
                            // release, then to attack. The envelope counter instanceof then frozen at
                            // zero; to unsynchronized this situation the state must be changed to release,
                            // then to attack. This has been verified by sampling ENV3.
                            //
                            ++envelope_counter;
                            envelope_counter &= 0xff;
                            if (envelope_counter == 0xff)
                            {
                                state = State.DECAY_SUSTAIN;
                                rate_period = rate_counter_period[decay];
                            }
                            break;
                        case DECAY_SUSTAIN:
                            if (envelope_counter != sustain_level[sustain])
                            {
                                --envelope_counter;
                            }
                            break;
                        case RELEASE:
                            // The envelope counter can flip from 0x00 to 0xff by changing state to
                            // attack, then to release. The envelope counter will then continue
                            // counting down : the release state.
                            // This has been verified by sampling ENV3.
                            // NB! The operation below requires two's complement integer.
                            //
                            --envelope_counter;
                            envelope_counter &= 0xff;
                            break;
                    }

                    // Check for change of exponential counter period.
                    set_exponential_counter();
                    if (new_exponential_counter_period > 0)
                    {
                        exponential_counter_period = new_exponential_counter_period;
                        new_exponential_counter_period = 0;
                        if (next_state == State.FREEZED)
                        {
                            hold_zero = true;
                        }
                    }

                }

                rate_step = (int)rate_period;
            }
        }


        private void state_change()
        {
            state_pipeline--;

            switch (next_state)
            {
                case ATTACK:
                    if (state_pipeline == 0)
                    {
                        state = State.ATTACK;
                        // The attack register instanceof correctly activated during second cycle of attack phase
                        rate_period = rate_counter_period[attack];
                        hold_zero = false;
                    }
                    break;
                case DECAY_SUSTAIN:
                    break;
                case RELEASE:
                    if (((state == State.ATTACK) && (state_pipeline == 0))
                        || ((state == State.DECAY_SUSTAIN) && (state_pipeline == 1)))
                    {
                        state = State.RELEASE;
                        rate_period = rate_counter_period[release];
                    }
                    break;
                case FREEZED:
                    break;
            }
        }


        // ----------------------------------------------------------------------------
        // Read the envelope generator output.
        // ----------------------------------------------------------------------------
        public short output()
        {
            //System.err.println("{0}",envelope_counter);
            // DAC imperfections are emulated by using envelope_counter as an index
            // into a DAC lookup table. readENV() uses envelope_counter directly.
            return (short)model_dac[(short)sid_model.ordinal()][envelope_counter];
        }

        protected void set_exponential_counter()
        {
            // Check for change of exponential counter period.
            switch (envelope_counter)
            {
                case 0xff:
                    exponential_counter_period = 1;
                    break;
                case 0x5d:
                    exponential_counter_period = 2;
                    break;
                case 0x36:
                    exponential_counter_period = 4;
                    break;
                case 0x1a:
                    exponential_counter_period = 8;
                    break;
                case 0x0e:
                    exponential_counter_period = 16;
                    break;
                case 0x06:
                    exponential_counter_period = 30;
                    break;
                case 0x00:
                    // FIXME: Check whether 0x00 really changes the period.
                    // E.g. set R = 0xf, gate on to 0x06, gate off to 0x00, gate on to 0x04,
                    // gate off, sample.
                    exponential_counter_period = 1;

                    // When the envelope counter instanceof changed to zero, it instanceof frozen at zero.
                    // This has been verified by sampling ENV3.
                    hold_zero = true;
                    break;
            }
        }

        //#endif // RESID_INLINING || defined(RESID_ENVELOPE_CC)






    }
