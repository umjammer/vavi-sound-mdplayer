package mdplayer.lib.mndrv;


public interface MnWork {

    int TRACK = 64;

    // wavememory effect work structure
    interface W_We {
        int start = 0;              // .ds.l    1        * Absolute starting address
        int loop_start = 4;         // .ds.l    1        * Loop starting point absolute address
        int loop_end = 8;           // .ds.l    1        * Absolute address of loop end point
        int loop_count = 12;        // .ds.l    1        * Loop count ( == 0 for infinite loop)
        int ko_start = 16;          // .ds.l    1        * Key-off start point
        int ko_loop_start = 20;     // .ds.l    1        * Key-off loop start point
        int ko_loop_end = 24;       // .ds.l    1        * Key-off loop end point
        int ko_loop_count = 28;     // .ds.l    1        * Key-off loop count
        int adrs_work = 32;         // .ds.l    1        * Address Work
        int start_adrs_work = 36;   // .ds.l    1        * Loop start address work
        int end_adrs_work = 40;     // .ds.l    1        * Loop end point address work
        int lp_cnt_work = 44;       // .ds.l    1        * Loop Work
        int exec_adrs = 48;         // .ds.l    1        * Actual working address
        int reset = 52;             // .ds.W    1        * Redefining the value

        int exec_flag = 54;         // .ds.b    1        * Has the loop finished?
        int loop_flag = 55;         // .ds.b    1        *
        int ko_flag = 56;           // .ds.b    1        * Whether to use key-off waveform
        int exec = 57;              // .ds.b    1        * bit7  1:enable
                                    //                   * bit1  1:keyOff only
                                    //                   * bit0  1:keyOn only
        int pattern = 58;           // .ds.b    1
        int speed = 59;             // .ds.b    1
        int delay = 60;             // .ds.b    1
        int delay_work = 61;        // .ds.b    1

        int count = 62;             // .ds.b    1
        int mode = 63;              // .ds.b    1        * $80 Asynchronous
                                    //                   * $00 Sync 1
                                    //                   * $01 Sync 2
                                    // .ds.b    1
                                    // .ds.b    1
        int _work_size = 66;        // _wave_effect_work_size
    }

    //wavememory work structure
    interface W_W {
        int start = 0;                                              // .ds.l    1        * Absolute starting address
        int loop_start = start + 4;                                 // .ds.l    1        * Loop starting point absolute address
        int loop_end = loop_start + 4;                              // .ds.l    1        * Absolute address of loop end point
        int loop_count = loop_end + 4;                              // .ds.l    1        * Loop count ( == 0 for infinite loop)
        int ko_start = loop_count + 4;                              // .ds.l    1        * Key-off start point
        int ko_loop_start = ko_start + 4;                           // .ds.l    1        * Key-off loop start point
        int ko_loop_end = ko_loop_start + 4;                        // .ds.l    1        * Key-off loop end point
        int ko_loop_count = ko_loop_end + 4;                        // .ds.l    1        * Key-off loop count
        int adrs_work = ko_loop_count + 4;                          // .ds.l    1        * Address Work
        int start_adrs_work = adrs_work + 4;                        // .ds.l    1        * Loop start address work
        int end_adrs_work = start_adrs_work + 4;                    // .ds.l    1        * Loop end point address work
        int lp_cnt_work = end_adrs_work + 4;                        // .ds.l    1        * Loop Work
        int exec_flag = lp_cnt_work + 4;                            // .ds.b    1        * Has the loop finished?
        int loop_flag = exec_flag + 1;                              // .ds.b    1        *
        int ko_flag = loop_flag + 1;                                // .ds.b    1        * Whether to use key-off waveform
        int use_flag = ko_flag + 1;                                 // .ds.b    1        *
        int depth = use_flag + 1;                                   // .ds.b    1        * Waveform Magnification
        int type = depth + 1;                                       // .ds.b    1        * Waveform Type
        int slot = type + 1;                                        // .ds.b    1        * Slots Used
                                                                    // .ds.b    1
                                                                    // .ds.l    4
        int _work_size = slot + 1 + 1 + 4 * 4;                      // _wave_work_size
    }

    interface W_Ww {
        int speed = 0;                                              // .ds.b    1        * speed
        int rate = speed + 1;                                       // .ds.b    1        * rate
        int depth = rate + 1;                                       // .ds.b    1        * depth
        int delay = depth + 1;                                      // .ds.b    1        * delay
        int sync = delay + 1;                                       // .ds.b    1        * sync
        int slot = sync + 1;                                        // .ds.b    1        * slot
        int rate_work = slot + 1;                                   // .ds.b    1
        int depth_work = rate_work + 1;                             // .ds.b    1
        int delay_work = depth_work + 1;                            // .ds.b    1
        int work = delay_work + 1;                                  // .ds.b    1
        int _work_size = work + 1;                                  // _ww_work_size
    }

    //LFO work structure
    interface W_L {
        int pattern = 0;                                            // .ds.b    1        * LFO pattern
        int count = pattern + 1;                                    // .ds.b    1        * LFO counter
        int lfo_sp = count + 1;                                     // .ds.b    1        * LFO speed
        int keydelay = lfo_sp + 1;                                  // .ds.b    1        * LFO delay
        int henka = keydelay + 1;                                   // .ds.W    1        * LFO change
        int henka_work = henka + 2;                                 // .ds.W    1        * LFO change work (pan_am_pm)
        int delay_work = henka_work + 2;                            // .ds.b    1        * LFO delay work
        int count_work = delay_work + 1;                            // .ds.b    1        * Work for LFO counter
        int flag = count_work + 1;                                  //
        int mokuhyou = flag + 0;                                    // .ds.W    1        * (Portamento Target pitch)
                                                                    //                   * The following is a normal LFO:
                                                                    //                   * bit15       0:norm 1:use slot lfo
                                                                    //                   * bit14       0:sync 1:async
                                                                    //                   * bit13       0:norm 1:stop(1shot)
                                                                    //                   *
                                                                    //                   * bit1 keyOff 0:enable 1:disable
                                                                    //                   * bit0 keyOn  0:enable 1:disable
        int bendwork = mokuhyou + 2;                                // .ds.W 1           * Pitch/Volume LFO work
        int _work_size = bendwork + 2;                              // _lfo_work_size:
    }

    // PCM work structure
    interface P {
        int NUM = 0;                                                // .ds.W    1
        int SEL = NUM + 2;                                          // .ds.b    1        * PCM Type
        int NOTE = SEL + 1;                                         // .ds.b    1        * PCM Original Note
        int MODE = NOTE + 1;                                        // .ds.b    1
        int RESERVE = MODE + 1;                                     // .ds.b    1
        int ADDRESS = RESERVE + 1;                                  // .ds.l    1        * Absolute address
        int LENGTH = ADDRESS + 4;                                   // .ds.l    1        * length
        int LOOP_START = LENGTH + 4;                                // .ds.l    1        * Loop Opening
        int LOOP_END = LOOP_START + 4;                              // .ds.l    1        * Loop End Point
        int LOOP_COUNT = LOOP_END + 4;                              // .ds.l    1        * Loop Count
        int _pcm_work_size = LOOP_COUNT + 4;                        // _pcm_work_size:

    }

    // track work structure
    interface W {
        int dataptr = 0;                                            // .ds.l    1        * $00 ! data pointer
        int voiceptr = dataptr + 4;                                 // .ds.l    1        * $04 ! Current tone pointer
        int detune = voiceptr + 4;                                  // .ds.W    1        * $08 ! detune
        int keycode2 = detune + 2;                                  // .ds.W    1        * $0A ! original
        int keycode = keycode2 + 2;                                 // .ds.W    1        * $0C ! target

        int len = keycode + 2;                                      // .ds.b    1        * $0E step
        int program = len + 1;                                      // .ds.b    1        * $0F ! voice number

        int smask = program + 1;                                    // .ds.b    1        * $10 slot mask
        int at_q = smask + 1;                                       // .ds.b    1        * $11 ! @q

        int vol = at_q + 1;                                         // .ds.b    1        * $12 ! volume
        int vol2 = vol + 1;                                         // .ds.b    1        * $13 ! target volume

        int pan_ampm = vol2 + 1;                                    // .ds.b    1        * $14 ! panpot(OPM pan/fl/con)
        int ch = pan_ampm + 1;                                      // .ds.b    1        * $15 ! channel

        int fbcon = ch + 1;                                         // .ds.b    1        * $16 FB/CON(OPM ams/pms)
        int volume = fbcon + 1;                                     // .ds.b    1        * $17 v For storage

        int key = volume + 1;                                       // .ds.b    1        * $18 ! Previous key
        int rct = key + 1;                                          // .ds.b    1        * $19 release cut time

        int flag = rct + 1;                                         // .ds.b    1        * $1A Truck flags
                                                                    //                   * bit7 Truck Use
                                                                    //                   * bit6 tie
                                                                    //                   * bit5 key on
                                                                    //                   * bit4 bend 2
                                                                    //                   * bit3 bend 1
                                                                    //                   * bit2 key on / off to clear
                                                                    //                   * bit1 LFO SET
                                                                    //                   * bit0 tie but the key is different from last time

        int lfo = flag + 1;                                         // .ds.b    1        * $1B LFO Control Flags
                                                                    //                   * bit7  pitch bend / portamento
                                                                    //                   * bit6  velocity 3
                                                                    //                   * bit5 velocity 2
                                                                    //                   * bit4 velocity 1
                                                                    //                   * bit3 pitch 3
                                                                    //                   * bit2 pitch 2
                                                                    //                   * bit1 pitch 1
                                                                    //                   * bit0 HARD LFO

        int p_pattern1 = lfo + 1;                                   // .ds.b _lfo_work_size      * $1C LFO 1
        int p_pattern2 = p_pattern1 + W_L._work_size;               // .ds.b _lfo_work_size      * $2A LFO 2
        int p_pattern3 = p_pattern2 + W_L._work_size;               // .ds.b _lfo_work_size      * $38 LFO 3
        int p_pattern4 = p_pattern3 + W_L._work_size;               // .ds.b _lfo_work_size      * $46 LFO 4 (for bend)

        int v_pattern1 = p_pattern4 + W_L._work_size;               // .ds.b _lfo_work_size      * $54 LFO 1
        int v_pattern2 = v_pattern1 + W_L._work_size;               // .ds.b _lfo_work_size      * $62 LFO 2
        int v_pattern3 = v_pattern2 + W_L._work_size;               // .ds.b _lfo_work_size      * $70 LFO 3
        int v_pattern4 = v_pattern3 + W_L._work_size;               // .ds.b _lfo_work_size      * $7E LFO 4 (for hardware LFO)

        int revexec = v_pattern4 + W_L._work_size;                  // .ds.b    1                * $8C run reverb?
        int pcmmode = revexec + 1;                                  // .ds.b    1                * $8D Types of PCM
        int volmode = pcmmode + 1;                                  // .ds.b    1                * $8E Relative Volume Mode
        int kom = volmode + 1;                                      // .ds.b    1                * $8F
        int envbank = kom + 1;                                      // .ds.b    1                * $90
        int envnum = envbank + 1;                                   // .ds.b    1                * $91

        //                          * $92
        int octave = envnum + 1;                                    //                           * Psg octave
        int ch3mode = octave + 0;                                   //                           * bit7 Sound Effects Mode
        int pcm_tone = ch3mode + 0;                                 // .ds.b    1                * MPCM TONE MODE
        //                      * 0 : tone mode
        //                        * 1 : timbre mode
        int bank = pcm_tone + 1;                                    // .ds.b    1                * $93 program bank

        int tone_rr = bank + 1;                                     // .ds.b    4                * $94 NOW TONE RR

        int key_trans = tone_rr + 4;                                // .ds.b    1                * $98 key transpose
        int flag2 = key_trans + 1;                                  // .ds.b    1                * $99 Truck Flag 2
        //                      * bit7 mask
        //                      * bit6 set @q
        //                        * bit5 set slot mask
        //                        * bit4 set LW portament
        //                        * bit3 set LW LFO
        //                        * bit2 set HLFO delay
        //                        * bit1 set portament
        //                      * bit0 track end

        int q = flag2 + 1;                                          // .ds.b    1             * $9A q
        int ch3 = q + 1;                                            // .ds.b    1             * $9B ch3

        int loop = ch3 + 1;                                         // .ds.l    1             * $9C Permanent Loop Point

        int sdetune1 = loop + 4;                                    // .ds.W    1             * $A0 slot detune
        int sdetune2 = sdetune1 + 2;                                // .ds.W    1             * $A2 slot detune
        int sdetune3 = sdetune2 + 2;                                // .ds.W    1             * $A4 slot detune
        int sdetune4 = sdetune3 + 2;                                // .ds.W    1             * $A6 slot detune

        int freqwork = sdetune4 + 2;                                //                        * $A8 psg work(Relative value)
        int keycode_s2 = freqwork + 0;                              // .ds.W    1             *     slot keycode
        int freqbase = keycode_s2 + 2;                              //                        * $AA psg work(Original value)
        int keycode_s3 = freqbase + 0;                              // .ds.W    1             *     slot keycode
        int makotune = keycode_s3 + 2;                              //                        * $AC psg work
        int keycode_s4 = makotune + 0;                              // .ds.W    1             *     slot keycode

        int banktone = keycode_s4 + 2;                              // .ds.W    1             * $AE bank / tone work
        int tune = banktone + 2;                                    // .ds.W    1             * $B0

        int program2 = tune + 2;                                    // .ds.b    1             * $B2 Internal tone number
        int dev = program2 + 1;                                     // .ds.b    1             * $B3 Device Number

        int effect = dev + 1;                                       // .ds.b    1             * $B4
                                                                    //                        *
                                                                    //                        * bit5 wowow
                                                                    //                        * bit3 Pseudo Echo
                                                                    //                        * bit2 Pseudo reverb
                                                                    //                        * bit1 RR cut
                                                                    //                        * bit0 RR cut
                                                                    //                        *     00 = normal
                                                                    //                        *     01 = gate time(keyoff)
                                                                    //                        *     10 = step time
                                                                    //                        *     11 = with keyon

        int flag3 = effect + 1;                                     // .ds.b    1             * $B5
                                                                    //                        * flag3
                                                                    //                        * bit6 Slurs
                                                                    //                        * bit5 Negative @q mode
                                                                    //                        * bit4 0 = @v / 1 = v
                                                                    //                        * bit3 0:normal 1:rest
                                                                    //                        * bit2 0:normal 1:portament2
                                                                    //                        * bit1 0:opn    1:psg
                                                                    //                        * bit0 0:normal 1:emulation

        int weffect = flag3 + 1;                                    // .ds.b    1             * Waveform effect
                                                                    //                        * bit7  effect on
                                                                    //                        * bit3 effect 4
                                                                    //                        * bit2 effect 3
                                                                    //                        * bit1 effect 2
                                                                    //                        * bit0 effect 1

        int reverb = weffect + 1;                                   // .ds.b    1             * bit7        1=Simulation ON
                                                                    //                        * bit4      always @v
                                                                    //                        * bit3 Direct Volume
                                                                    //                        * bit2 Fine-tuning
                                                                    //                        * bit1 tone
                                                                    //                        * bit0 panpot

                                                                    //                        * default    volume
        int reverb_vol = reverb + 1;                                // .ds.b    1             * For simulated operation volume
        int reverb_tone = 185;                                      // .ds.b    1             * For simulated operation tone
        int reverb_pan = 186;                                       // .ds.b    1             * For simulated operation panpot

        int reverb_pan_work = 187;                                  // .ds.b    1             * For simulated operation panpot
        int reverb_time = 188;                                      // .ds.b    1             * For simulated operation step time
        int reverb_time_work = 189;                                 // .ds.b    1             * For simulated operation step time

        int flag4 = 190;                                            // .ds.b    1             * flag
                                                                    //                        * bit7 Synchronization Wait
                                                                    //                        * bit6 Was it sent synchronously?

        int vol_work = 191;                                         // .ds.b    1             * Volume work (unused)
        int at_q_work = 192;                                        // .ds.b    1             * neg @q work
        int kov = 193;                                              // .ds.b    1             * KOV work
        int key2 = 194;                                             // .ds.b    1             * for bend

        int track_vol = 195;                                        // .ds.b    1             * track volume
        int ch3tl = 196;                                            // .ds.b    1             *
        int volcount = 197;                                         // .ds.b    1             * v Number of volumes

        int e_sw = 198;                                             // .ds.b    1             * Software Envelope
                                                                    //                        * bit7 1:on   0:off
                                                                    //                        * bit1 1:ext2 0:ext1
                                                                    //                        * bit0 1:ext  0:normal

        int e_p = 199;                                              // .ds.b    1             * ENV POINTER
                                                                    //                        * 5 : keyon
                                                                    //                        * 4 : keyoff
        int e_sub = 200;                                            //
        int e_ini = 200;                                            // .ds.b    1             * ENV INI(sub volume)
        int e_sv = 201;                                             //
        int e_dl = 201;                                             // .ds.b    1             * ENV DL
        int e_ar = 202;                                             //
        int e_sp = 202;                                             // .ds.b    1             * ENV SP
        int e_dr = 203;                                             //
        int e_al = 203;                                             //
        int e_lm = 203;                                             // .ds.b    1             * ENV LM
        int e_dd = 204;                                             //
        int e_sl = 204;                                             // .ds.b    1
        int e_sr = 205;                                             // .ds.b    1
        int e_rr = 206;                                             // .ds.b    1

        int e_alw = e_rr + 1;                                       // .ds.b    1
        int e_srw = e_alw + 1;                                      // .ds.b    1
        int e_rrw = e_srw + 1;                                      // .ds.b    1

        int voltable = e_rrw + 1;                                   // .ds.b    128            * volume table

        // The following is prohibited:
        //
        int eenv_volume = voltable + 128;                           // .ds.b    1
        int eenv_ar = eenv_volume + 1;                              // .ds.b    1
        int eenv_arc = eenv_ar + 1;                                 // .ds.b    1
        int eenv_dr = eenv_arc + 1;                                 // .ds.b    1
        int eenv_drc = eenv_dr + 1;                                 // .ds.b    1
        int eenv_sr = eenv_drc + 1;                                 // .ds.b    1
        int eenv_src = eenv_sr + 1;                                 // .ds.b    1
        int eenv_rr = eenv_src + 1;                                 // .ds.b    1
        int eenv_rrc = eenv_rr + 1;                                 // .ds.b    1
        int eenv_al = eenv_rrc + 1;                                 // .ds.b    1
        int eenv_sl = eenv_al + 1;                                  // .ds.b    1

        int eenv_limit = eenv_sl + 1;                               // .ds.b    1

                                                                    // .even
        int keycode3 = eenv_limit + 1;                              // .ds.W    1                * for LFO
        int addkeycode = keycode3 + 2;                              // .ds.W    1                * for LFO
        int addkeycode2 = addkeycode + 2;                           // .ds.W    1                * for LFO
        int addvolume = addkeycode2 + 2;                            // .ds.W    1                * for LFO
        int addvolume2 = addvolume + 2;                             // .ds.W    1                * for LFO
        int keycode_s1 = addvolume2 + 2;                            // .ds.W    1
        int keycode2_s1 = keycode_s1 + 2;                           // .ds.W    1                * slot keycode
        int keycode2_s2 = keycode2_s1 + 2;                          // .ds.W    1                * slot keycode
        int keycode2_s3 = keycode2_s2 + 2;                          // .ds.W    1                * slot keycode
        int keycode2_s4 = keycode2_s3 + 2;                          // .ds.W    1                * slot keycode
        int keycode3_s1 = keycode2_s4 + 2;                          // .ds.W    1                * slot keycode
        int keycode3_s2 = keycode3_s1 + 2;                          // .ds.W    1                * slot keycode
        int keycode3_s3 = keycode3_s2 + 2;                          // .ds.W    1                * slot keycode
        int keycode3_s4 = keycode3_s3 + 2;                          // .ds.W    1                * slot keycode

        int wp_pattern1 = keycode3_s4 + 2;                          // .ds.b _wave_work_size
        int wp_pattern2 = wp_pattern1 + W_W._work_size;             // .ds.b _wave_work_size
        int wp_pattern3 = wp_pattern2 + W_W._work_size;             // .ds.b _wave_work_size
        int wp_pattern4 = wp_pattern3 + W_W._work_size;             // .ds.b _wave_work_size
        int wv_pattern1 = wp_pattern4 + W_W._work_size;             // .ds.b _wave_work_size
        int wv_pattern2 = wv_pattern1 + W_W._work_size;             // .ds.b _wave_work_size
        int wv_pattern3 = wv_pattern2 + W_W._work_size;             // .ds.b _wave_work_size
        int wv_pattern4 = wv_pattern3 + W_W._work_size;             // .ds.b _wave_work_size
        int ww_pattern1 = wv_pattern4 + W_W._work_size;             // .ds.b _ww_work_size
        int we_pattern1 = ww_pattern1 + W_Ww._work_size;            // .ds.b _wave_effect_work_size
        int we_pattern2 = we_pattern1 + W_We._work_size;            // .ds.b _wave_effect_work_size
        int we_pattern3 = we_pattern2 + W_We._work_size;            // .ds.b _wave_effect_work_size
        int we_pattern4 = we_pattern3 + W_We._work_size;            // .ds.b _wave_effect_work_size

                                                                    // .quad
        int mmljob_adrs = we_pattern4 + W_We._work_size;            // .ds.l   1                * mml analyze
        int softenv_adrs = mmljob_adrs + 4;                         // .ds.l   1                * software envelope
        int lfojob_adrs = softenv_adrs + 4;                         // .ds.l   1                * LFO JOB
        int psgenv_adrs = lfojob_adrs + 4;                          // .ds.l   1                * Psg ENV PATTERN
        int qtjob = psgenv_adrs + 4;                                // .ds.l   1                * address work
        int rrcut_adrs = qtjob + 4;                                 // .ds.l   1                * RR cut job
        int echo_adrs = rrcut_adrs + 4;                             // .ds.l   1                * reverb
        int keyoff_adrs = echo_adrs + 4;                            // .ds.l   1                * keyoff
        int keyoff_adrs2 = keyoff_adrs + 4;                         // .ds.l   1                * keyoff
        int subcmd_adrs = keyoff_adrs2 + 4;                         // .ds.l   1                * sub command
        int setnote_adrs = subcmd_adrs + 4;                         // .ds.l   1                * note
        int inithlfo_adrs = setnote_adrs + 4;                       // .ds.l   1                * for LW LFO
        int we_ycom_adrs = inithlfo_adrs + 4;                       // .ds.l   1                * effect ycommand
        int we_tone_adrs = we_ycom_adrs + 4;                        // .ds.l   1                * effect tone
        int we_pan_adrs = we_tone_adrs + 4;                         // .ds.l   1                * effect panpot
        int _track_work_size = we_pan_adrs + 4;                     // _track_work_size:
    }

    // driver work structure
    interface Dw {
                                                   // .offset    0
        int DRV_FLAG = 0;                          // .ds.b    1        * $00
                                                   //                   * bit 7 key control Disabled = 1
                                                   //                   * bit 6 MPCM Occupancy = 1
                                                   //                   * bit 5 use OPM timer =1
                                                   //                   * bit 4 use PDX = 1
                                                   //                   * bit 3 zdd Occupancy = 1
                                                   //                   * bit 2 func exec = 1
                                                   //                   * bit 1 PCM load = 1
                                                   //                   * bit 0 OPN3 None = 1

        int MND_VER = DRV_FLAG + 1;                // .ds.b    1        * $01 MND VERSION
                                                   // .ds.b    1        * $02
        int FADESPEED = MND_VER + 2;               // .ds.b    1        * $03 FADE OUT count

        int FADEFLAG = FADESPEED + 1;              // .ds.b    1        * $04
        int FADESPEED_WORK = FADEFLAG + 1;         // .ds.b    1        * $05
        int FADECOUNT = FADESPEED_WORK + 1;        // .ds.b    1        * $06
        int VOLMODE = FADECOUNT + 1;               // .ds.b    1        * $07

        int MASTER_VOL_FM = VOLMODE + 1;           // .ds.b   1         * $08
        int MASTER_VOL_PSG = MASTER_VOL_FM + 1;    // .ds.b   1         * $09
        int MASTER_VOL_RHY = MASTER_VOL_PSG + 1;   // .ds.b   1         * $0A
        int MASTER_VOL_PCM = MASTER_VOL_RHY + 1;   // .ds.b   1         * $0B

        int TEMPO = MASTER_VOL_PCM + 1;            // .ds.b   1         * $0C ! tempo
        int DRV_STATUS = TEMPO + 1;                // .ds.b   1         * $0D ! Driver Status
                                                   //                   * bit 7 play
                                                   //                   * bit 6 pause
                                                   //                   * bit 5 stop
                                                   //                   * bit 4 fadeout & stop
                                                   //                   * bit 3 During Jump
                                                   //                   * bit 0 Driver Processing
        int CH3KOM = DRV_STATUS + 1;               // .ds.b   1         * $0E Sound Effects Mode keyon flag
        int CH3KOS = CH3KOM + 1;                   // .ds.b   1         * $0F    〃              keyoff flag

        int CH3MODEM = CH3KOS + 1;                 // .ds.b   1         * $10
        int CH3MODES = CH3MODEM + 1;               // .ds.b   1         * $11
        int VOICENUM = CH3MODES + 1;               // .ds.W   1         * $12 Number of defined tones
        int ENVNUM = VOICENUM + 2;                 // .ds.W   1         * $14 Number of defined waveforms

        int DIV = ENVNUM + 2;                      // .ds.W   1         * $16 Whole Note Clock
                                                   // .ds.l   1         * $18
                                                   // .ds.l   1         * $1C

        int PSGMIX_M = DIV + 2 + 4 + 4;            // .ds.b   1         * $20 MASTER
        int PSGMIX_S = PSGMIX_M + 1;               // .ds.b   1         * $21 SLAVE

        int M_BD = PSGMIX_S + 1;                   // .ds.b   1         * $22 BD / LR vol
        int M_SD = M_BD + 1;                       // .ds.b   1         * $23 SD
        int M_TC = M_SD + 1;                       // .ds.b   1         * $24 TC
        int M_HH = M_TC + 1;                       // .ds.b   1         * $25 HH
        int M_TOM = M_HH + 1;                      // .ds.b   1         * $26 TOM
        int M_RIM = M_TOM + 1;                     // .ds.b   1         * $27 RIM

        int S_BD = 40;                 // .ds.b   1            * $28 BD / LR vol
        int S_SD = 41;                 // .ds.b   1            * $29 SD
        int S_TC = 42;                 // .ds.b   1            * $2A TC
        int S_HH = 43;                 // .ds.b   1            * $2B HH
        int S_TOM = 44;                // .ds.b   1            * $2C TOM
        int S_RIM = 45;                // .ds.b   1            * $2D RIM

        int DRV_FLAG2 = 46;            // .ds.b   1            * $2E
                                       //                      * bit 7 true tie
                                       //                      * bit 6 FMP tie
                                       //                      * bit 4 Q MODE
                                       //                      * bit 2 ENV mode
                                       //                      * bit 1 PMD lfo
                                       //                      * bit 0 mako lfo

        int DRV_FLAG3 = 47;            // .ds.b   1            * $2F
                                       //                      * bit 7 TIMER-a LFO
                                       //                      * bit 6 TIMER-a ENV

        int LFO_FLAG = 48;             // .ds.b   1            * $30
                                       //                      * bit 7 extended LFO
                                       //                      * bit 6 Octave OK

        int EMUMODE = 49;              // .ds.b   1            * $31 OPN Emulation Mode
        int UNREMOVE = 50;             // .ds.W   1            * $32 Resident flag

        int INTEXECNUM = 52;           // .ds.W   1            * $34 _INTEXEC Registration Count
        int INTEXECBUF = 54;           // .ds.l   8            * $36 Buffer for _INTEXEC
        int SUBEVENTNUM = 86;          // .ds.W   1            * $56 How many sub-events are registered
        int SUBEVENTADR = 88;          // .ds.l   8            * $58 _SETSUBEVENT Address buffer
        int SUBEVENTID = 120;          // .ds.l   8            * $78 _SETSUBEVENT ID Buffer

        int TIMER_FLAG = 152;          // .ds.W   1            * $98
        int TEMPO2 = 154;              // .ds.b   1            * $9A
        int TEMPO3 = 155;              // .ds.b   1            * $9B

        int NOISE_M = 156;             // .ds.b   1            * $9C NOISE FREQ Psg MASTER
        int NOISE_S = 157;             // .ds.b   1            * $9D NOISE FREQ Psg SLAVE
        int NOISE_O = 158;             // .ds.b   1            * $9E NOISE FREQ OPM

                                       // .ds.b   639

        int ADPCMNAME = 798;           // .ds.b   96           * $31E
        int MASKDATA = 894;            // .ds.b   256          * $37E

        int USE_TRACK = 1150;          // .ds.W   1            * $47E ! Number of tracks used
        int RANDOMESEED = 1152;        // .ds.l   1            * $480
                                       // .ds.b   1            * $484
        int SP_KEY = 1157;             // .ds.b   1            * $485 1 if key is pressed
        int MUTE = 1158;               // .ds.b   1            * $486 Did you fast forward and MUTE?
        int RHY_DAT = 1159;            // .ds.b   1            * $487 master rhythm data
        int RHY_DAT2 = 1160;           // .ds.b   1            * $488 slave rhythm data
        int RHY_TV = 1161;             // .ds.b   1            * $489 ! rhythm total volume
        int LOOP_COUNTER = 1162;       // .ds.W   1            * $48A !
        //
        // The following cannot be referenced in ascending order:
        // Descending reference from TRKWORKADR.
        //
        int FNUM_KC_TABLE = 1164;      // .ds.b    4096
        int FREQ_KC_TABLE = 5260;      // .ds.b    8192
                                       // .even
        int TRKANA_RESTADR = 13452;    // .ds.l    1
        int PCMBUF_ENDADR = 13456;     // .ds.l    1
        int MMLBUFADR = 13460;         // .ds.l    1
        int PCMBUFADR = 13464;         // .ds.l    1
        int MPCMWORKADR = 13468;       // .ds.l    1
        int ZPDCOUNT = 13472;          // .ds.l    1
        int VOL_PTR = 13476;           // .ds.l    1            * Volume definition pointer
        int ENV_PTR = 13480;           // .ds.l    1            * Envelope Definition Pointer
        int WAVE_PTR = 13484;          // .ds.l    1            * Waveform Definition Pointer
        int TITLE_PTR = 13488;         // .ds.l    1            * Title definition pointer
        int TONE_PTR = 13492;          // .ds.l    1            * Tone definition pointer
        int SEQ_DATA_PTR = 13496;      // .ds.l    1            * sequence data address
        int SOFTENV_PATTERN = 13500;   // .ds.b    256          * 16byte x 6+1ch
        int OPMREGWORK = 13756;        // .ds.b    1024
        int REGWORKADR = 14780;        // .ds.b    1024
        int TRACKWORKADR = 15804;      // .ds.b _track_work_size*TRACK
        int _work_size = 89596;        //
        int _trackworksize = 73792;    // .equ _track_work_size*TRACK
                                       //      .text
    }

    //
    interface Cw {
        int _ch_table = 0;
    }
}
