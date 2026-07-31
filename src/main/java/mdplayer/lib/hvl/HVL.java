/*
 ______ ______________     ______  __________
(______)     ___     /____(______)_\   _____/___.
|      |      |/    /     |      |  \____       |
|______|______|\__________|______|______________|spot

                  bring you in 2008


                   HiivelyPlay 2.0


  Play HivelyTracker and AHX songs on your Wii!
           * NOW WITH 400% MORE AWESOME *


                 Code: Xeron/IRIS
             Hively logo: Spot/Up Rough


            http://www.hivelytracker.co.uk
                http://www.irishq.dk
 */

package mdplayer.lib.hvl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;


public class HVL {

    public static final int MAX_CHANNELS = 16;
    public static final int WHITENOISELEN = 0x280 * 3;

    public static final int WO_LOWPASSES = 0;
    public static final int WO_TRIANGLE_04 = WO_LOWPASSES + ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31);
    public static final int WO_TRIANGLE_08 = WO_TRIANGLE_04 + 0x04;
    public static final int WO_TRIANGLE_10 = WO_TRIANGLE_08 + 0x08;
    public static final int WO_TRIANGLE_20 = WO_TRIANGLE_10 + 0x10;
    public static final int WO_TRIANGLE_40 = WO_TRIANGLE_20 + 0x20;
    public static final int WO_TRIANGLE_80 = WO_TRIANGLE_40 + 0x40;
    public static final int WO_SAWTOOTH_04 = WO_TRIANGLE_80 + 0x80;
    public static final int WO_SAWTOOTH_08 = WO_SAWTOOTH_04 + 0x04;
    public static final int WO_SAWTOOTH_10 = WO_SAWTOOTH_08 + 0x08;
    public static final int WO_SAWTOOTH_20 = WO_SAWTOOTH_10 + 0x10;
    public static final int WO_SAWTOOTH_40 = WO_SAWTOOTH_20 + 0x20;
    public static final int WO_SAWTOOTH_80 = WO_SAWTOOTH_40 + 0x40;
    public static final int WO_SQUARES = WO_SAWTOOTH_80 + 0x80;
    public static final int WO_WHITENOISE = WO_SQUARES + (0x80 * 0x20);
    public static final int WO_HIGHPASSES = WO_WHITENOISE + WHITENOISELEN;
    public static final int WAVES_SIZE = WO_HIGHPASSES + ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31);

    public static final byte[] waves = new byte[WAVES_SIZE];

    public static final short[] VIB_TAB = {
            0, 24, 49, 74, 97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253, 255,
            253, 250, 244, 235, 224, 212, 197, 180, 161, 141, 120, 97, 74, 49, 24,
            0, -24, -49, -74, -97, -120, -141, -161, -180, -197, -212, -224, -235, -244, -250, -253, -255,
            -253, -250, -244, -235, -224, -212, -197, -180, -161, -141, -120, -97, -74, -49, -24
    };

    public static final int[] PERIOD_TAB = {
            0x0000, 0x0D60, 0x0CA0, 0x0BE8, 0x0B40, 0x0A98, 0x0A00, 0x0970,
            0x08E8, 0x0868, 0x07F0, 0x0780, 0x0714, 0x06B0, 0x0650, 0x05F4,
            0x05A0, 0x054C, 0x0500, 0x04B8, 0x0474, 0x0434, 0x03F8, 0x03C0,
            0x038A, 0x0358, 0x0328, 0x02FA, 0x02D0, 0x02A6, 0x0280, 0x025C,
            0x023A, 0x021A, 0x01FC, 0x01E0, 0x01C5, 0x01AC, 0x0194, 0x017D,
            0x0168, 0x0153, 0x0140, 0x012E, 0x011D, 0x010D, 0x00FE, 0x00F0,
            0x00E2, 0x00D6, 0x00CA, 0x00BE, 0x00B4, 0x00AA, 0x00A0, 0x0097,
            0x008F, 0x0087, 0x007F, 0x0078, 0x0071
    };

    public static final int[] stereopan_left = {128, 96, 64, 32, 0};
    public static final int[] stereopan_right = {128, 160, 193, 225, 255};

    public static final int[] panning_left = new int[256];
    public static final int[] panning_right = new int[256];

    public static final int[] OFFSETS = {0, 4, 12, 28, 60, 124};

    // --- Struct definitions ---

    public static class Envelope {

        public short aFrames, aVolume;
        public short dFrames, dVolume;
        public short sFrames;
        public short rFrames, rVolume;
        public short pad;
    }

    public static class PlsEntry {

        public byte ple_Note;
        public byte ple_Waveform;
        public short ple_Fixed;
        public byte[] ple_FX = new byte[2];
        public byte[] ple_FXParam = new byte[2];
    }

    public static class PList {

        public short pls_Speed;
        public short pls_Length;
        public PlsEntry[] pls_Entries;
    }

    public static class Instrument {

        public String ins_Name = "";
        public int ins_Volume;
        public int ins_WaveLength;
        public int ins_FilterLowerLimit;
        public int ins_FilterUpperLimit;
        public int ins_FilterSpeed;
        public int ins_SquareLowerLimit;
        public int ins_SquareUpperLimit;
        public int ins_SquareSpeed;
        public int ins_VibratoDelay;
        public int ins_VibratoSpeed;
        public int ins_VibratoDepth;
        public int ins_HardCutRelease;
        public int ins_HardCutReleaseFrames;
        public Envelope ins_Envelope = new Envelope();
        public PList ins_PList = new PList();
    }

    public static class Position {

        public int[] pos_Track = new int[MAX_CHANNELS];
        public byte[] pos_Transpose = new byte[MAX_CHANNELS];
    }

    public static class Step {

        public int stp_Note;
        public int stp_Instrument;
        public int stp_FX;
        public int stp_FXParam;
        public int stp_FXb;
        public int stp_FXbParam;
    }

    public static class Voice {

        public short vc_Track;
        public short vc_NextTrack;
        public short vc_Transpose;
        public short vc_NextTranspose;
        public short vc_OverrideTranspose;
        public int vc_ADSRVolume;
        public Envelope vc_ADSR = new Envelope();
        public Instrument vc_Instrument;
        public int vc_SamplePos;
        public int vc_Delta;
        public int vc_InstrPeriod;
        public int vc_TrackPeriod;
        public int vc_VibratoPeriod;
        public int vc_WaveLength;
        public short vc_NoteMaxVolume;
        public int vc_PerfSubVolume;
        public int vc_NewWaveform;
        public int vc_Waveform;
        public int vc_PlantPeriod;
        public int vc_VoiceVolume;
        public int vc_PlantSquare;
        public int vc_IgnoreSquare;
        public int vc_FixedNote;
        public short vc_VolumeSlideUp;
        public short vc_VolumeSlideDown;
        public short vc_HardCut;
        public int vc_HardCutRelease;
        public short vc_HardCutReleaseF;
        public int vc_PeriodSlideOn;
        public short vc_PeriodSlideSpeed;
        public short vc_PeriodSlidePeriod;
        public short vc_PeriodSlideLimit;
        public short vc_PeriodSlideWithLimit;
        public short vc_PeriodPerfSlideSpeed;
        public short vc_PeriodPerfSlidePeriod;
        public int vc_PeriodPerfSlideOn;
        public short vc_VibratoDelay;
        public short vc_VibratoSpeed;
        public short vc_VibratoCurrent;
        public short vc_VibratoDepth;
        public short vc_SquareOn;
        public short vc_SquareInit;
        public short vc_SquareWait;
        public short vc_SquareLowerLimit;
        public short vc_SquareUpperLimit;
        public short vc_SquarePos;
        public short vc_SquareSign;
        public short vc_SquareSlidingIn;
        public short vc_SquareReverse;
        public int vc_FilterOn;
        public int vc_FilterInit;
        public short vc_FilterWait;
        public short vc_FilterSpeed;
        public short vc_FilterUpperLimit;
        public short vc_FilterLowerLimit;
        public short vc_FilterPos;
        public short vc_FilterSign;
        public short vc_FilterSlidingIn;
        public short vc_IgnoreFilter;
        public short vc_PerfCurrent;
        public short vc_PerfSpeed;
        public short vc_PerfWait;
        public PList vc_PerfList;

        public byte[] vc_AudioSource_array;
        public int vc_AudioSource_offset;

        public int vc_NoteDelayOn;
        public int vc_NoteCutOn;
        public short vc_NoteDelayWait;
        public short vc_NoteCutWait;
        public short vc_AudioPeriod;
        public short vc_AudioVolume;
        public int vc_WNRandom;

        public byte[] vc_MixSource;
        public byte[] vc_SquareTempBuffer = new byte[0x80];
        public byte[] vc_VoiceBuffer = new byte[0x282 * 4];
        public int vc_VoiceNum;
        public int vc_TrackMasterVolume;
        public int vc_TrackOn;
        public short vc_VoicePeriod;
        public int vc_Pan;
        public int vc_SetPan;
        public int vc_PanMultLeft;
        public int vc_PanMultRight;
        public int vc_RingSamplePos;
        public int vc_RingDelta;

        public byte[] vc_RingMixSource;
        public int vc_RingPlantPeriod;
        public short vc_RingInstrPeriod;
        public short vc_RingBasePeriod;
        public short vc_RingAudioPeriod;

        public byte[] vc_RingAudioSource_array;
        public int vc_RingAudioSource_offset;

        public int vc_RingNewWaveform;
        public int vc_RingWaveform;
        public int vc_RingFixedPeriod;
        public byte[] vc_RingVoiceBuffer = new byte[0x282 * 4];
        public int vc_VUMeter;

        public void hvl_set_audio(double freqf) {
            if (vc_TrackOn == 0) {
                vc_VoiceVolume = 0;
                return;
            }

            vc_VoiceVolume = vc_AudioVolume;

            if (vc_PlantPeriod != 0) {
                vc_PlantPeriod = 0;
                vc_VoicePeriod = vc_AudioPeriod;

                double freq2 = Period2Freq(vc_AudioPeriod);
                int delta = (int) (freq2 / freqf);

                if (delta > (0x280 << 16)) {
                    delta -= (0x280 << 16);
                }
                if (delta == 0) {
                    delta = 1;
                }
                vc_Delta = delta;
            }

            if (vc_NewWaveform != 0) {
                byte[] src = vc_AudioSource_array;
                int srcOff = vc_AudioSource_offset;

                if (vc_Waveform == 4 - 1) {
                    System.arraycopy(src, srcOff, vc_VoiceBuffer, 0, 0x280);
                } else {
                    int waveLoops = (1 << (5 - vc_WaveLength)) * 5;
                    int len = 4 * (1 << vc_WaveLength);
                    for (int i = 0; i < waveLoops; i++) {
                        System.arraycopy(src, srcOff, vc_VoiceBuffer, i * len, len);
                    }
                }

                vc_VoiceBuffer[0x280] = vc_VoiceBuffer[0];
                vc_MixSource = vc_VoiceBuffer;
            }

            /* Ring Modulation */
            if (vc_RingPlantPeriod != 0) {
                vc_RingPlantPeriod = 0;
                double freq2 = Period2Freq(vc_RingAudioPeriod);
                int delta = (int) (freq2 / freqf);

                if (delta > (0x280 << 16)) {
                    delta -= (0x280 << 16);
                }
                if (delta == 0) {
                    delta = 1;
                }
                vc_RingDelta = delta;
            }

            if (vc_RingNewWaveform != 0) {
                byte[] src = vc_RingAudioSource_array;
                int srcOff = vc_RingAudioSource_offset;

                int waveLoops = (1 << (5 - vc_WaveLength)) * 5;
                int len = 4 * (1 << vc_WaveLength);
                for (int i = 0; i < waveLoops; i++) {
                    System.arraycopy(src, srcOff, vc_RingVoiceBuffer, i * len, len);
                }

                vc_RingVoiceBuffer[0x280] = vc_RingVoiceBuffer[0];
                vc_RingMixSource = vc_RingVoiceBuffer;
            }
        }
    }

    public static class Tune {

        public String ht_Name = "";
        public int ht_SongNum;
        public int ht_Frequency;
        public double ht_FreqF;

        public int ht_Restart;
        public int ht_PositionNr;
        public int ht_SpeedMultiplier;
        public int ht_TrackLength;
        public int ht_TrackNr;
        public int ht_InstrumentNr;
        public int ht_SubsongNr;
        public int ht_PosJump;
        public int ht_PlayingTime;
        public short ht_Tempo;
        public short ht_PosNr;
        public short ht_StepWaitFrames;
        public short ht_NoteNr;
        public int ht_PosJumpNote;
        public int ht_GetNewPosition;
        public int ht_PatternBreak;
        public int ht_SongEndReached;
        public int ht_Stereo;
        public int[] ht_Subsongs;
        public int ht_Channels;
        public Position[] ht_Positions;
        public Step[][] ht_Tracks = new Step[256][64];
        public Instrument[] ht_Instruments;
        public Voice[] ht_Voices = new Voice[MAX_CHANNELS];
        public int ht_defstereo;
        public int ht_defpanleft;
        public int ht_defpanright;
        public int ht_mixgain;
        public int ht_Version;

        public Tune() {
            for (int i = 0; i < MAX_CHANNELS; i++) {
                ht_Voices[i] = new Voice();
            }
            for (int t = 0; t < 256; t++) {
                for (int s = 0; s < 64; s++) {
                    ht_Tracks[t][s] = new Step();
                }
            }
        }

        public void hvl_DecodeFrame(short[] buf1, int offset1, short[] buf2, int offset2, int bufmod) {
            int samples = ht_Frequency / 50 / ht_SpeedMultiplier;
            int loops = ht_SpeedMultiplier;
            int shortStride = bufmod / 2;

            do {
                hvl_play_irq();
                hvl_mixchunk(samples, buf1, offset1, buf2, offset2, shortStride);
                offset1 += samples * shortStride;
                offset2 += samples * shortStride;
                loops--;
            } while (loops > 0);
        }

        public void hvl_process_stepfx_3(Voice voice, int FX, int FXParam) {
            switch (FX) {
                case 0x01: // Portamento up (period slide down)
                    voice.vc_PeriodSlideSpeed = (short) (-FXParam);
                    voice.vc_PeriodSlideOn = 1;
                    voice.vc_PeriodSlideWithLimit = 0;
                    break;
                case 0x02: // Portamento down
                    voice.vc_PeriodSlideSpeed = (short) FXParam;
                    voice.vc_PeriodSlideOn = 1;
                    voice.vc_PeriodSlideWithLimit = 0;
                    break;
                case 0x04: // Filter override
                    if (FXParam == 0 || FXParam == 0x40) {
                        break;
                    }
                    if (FXParam < 0x40) {
                        voice.vc_IgnoreFilter = (short) FXParam;
                        break;
                    }
                    if (FXParam > 0x7f) {
                        break;
                    }
                    voice.vc_FilterPos = (short) (FXParam - 0x40);
                    break;
                case 0x0c: // Volume
                    FXParam &= 0xff;
                    if (FXParam <= 0x40) {
                        voice.vc_NoteMaxVolume = (short) FXParam;
                        break;
                    }

                    FXParam -= 0x50;
                    if (FXParam < 0) {
                        break;
                    }

                    if (FXParam <= 0x40) {
                        for (int i = 0; i < ht_Channels; i++) {
                            ht_Voices[i].vc_TrackMasterVolume = FXParam;
                        }
                        break;
                    }

                    FXParam -= 0xa0 - 0x50;
                    if (FXParam < 0) {
                        break;
                    }

                    if (FXParam <= 0x40) {
                        voice.vc_TrackMasterVolume = FXParam;
                    }
                    break;

                case 0xe: // Extended commands;
                    switch (FXParam >> 4) {
                        case 0x1: // Fineslide up
                            voice.vc_PeriodSlidePeriod = (short) (-(FXParam & 0x0f));
                            voice.vc_PlantPeriod = 1;
                            break;

                        case 0x2: // Fineslide down
                            voice.vc_PeriodSlidePeriod = (short) (FXParam & 0x0f);
                            voice.vc_PlantPeriod = 1;
                            break;

                        case 0x4: // Vibrato control
                            voice.vc_VibratoDepth = (short) (FXParam & 0x0f);
                            break;

                        case 0x0a: // Fine volume up
                            voice.vc_NoteMaxVolume += FXParam & 0x0f;
                            if (voice.vc_NoteMaxVolume > 0x40) {
                                voice.vc_NoteMaxVolume = 0x40;
                            }
                            break;

                        case 0x0b: // Fine volume down
                            voice.vc_NoteMaxVolume -= FXParam & 0x0f;
                            if (voice.vc_NoteMaxVolume < 0) {
                                voice.vc_NoteMaxVolume = 0;
                            }
                            break;

                        case 0x0f: // Misc flags (1.5)
                            if (ht_Version < 1) {
                                break;
                            }
                            switch (FXParam & 0xf) {
                                case 1:
                                    voice.vc_OverrideTranspose = voice.vc_Transpose;
                                    break;
                            }
                            break;
                    }
                    break;
            }
        }

        public void hvl_process_stepfx_2(Voice voice, int FX, int FXParam, int[] Note) {
            switch (FX) {
                case 0x9: // Set squarewave offset
                    voice.vc_SquarePos = (short) (FXParam >> (5 - voice.vc_WaveLength));
                    voice.vc_PlantSquare = 1;
                    voice.vc_IgnoreSquare = 1;
                    break;

                case 0x5: // Tone portamento + volume slide
                case 0x3: // Tone portamento
                    if (FXParam != 0) {
                        voice.vc_PeriodSlideSpeed = (short) FXParam;
                    }

                    if (Note[0] != 0) {
                        int diff = PERIOD_TAB[voice.vc_TrackPeriod];
                        diff -= PERIOD_TAB[Note[0]];
                        int newVal = diff + voice.vc_PeriodSlidePeriod;

                        if (newVal != 0) {
                            voice.vc_PeriodSlideLimit = (short) (-diff);
                        }
                    }
                    voice.vc_PeriodSlideOn = 1;
                    voice.vc_PeriodSlideWithLimit = 1;
                    Note[0] = 0;
                    break;
            }
        }

        public void hvl_reset_some_stuff() {
            for (int i = 0; i < MAX_CHANNELS; i++) {
                Voice v = ht_Voices[i];
                v.vc_Delta = 1;
                v.vc_OverrideTranspose = 1000;
                v.vc_SamplePos = 0;
                v.vc_Track = 0;
                v.vc_Transpose = 0;
                v.vc_NextTrack = 0;
                v.vc_NextTranspose = 0;
                v.vc_ADSRVolume = 0;
                v.vc_InstrPeriod = 0;
                v.vc_TrackPeriod = 0;
                v.vc_VibratoPeriod = 0;
                v.vc_NoteMaxVolume = 0;
                v.vc_PerfSubVolume = 0;
                v.vc_TrackMasterVolume = 0;
                v.vc_NewWaveform = 0;
                v.vc_Waveform = 0;
                v.vc_PlantSquare = 0;
                v.vc_PlantPeriod = 0;
                v.vc_IgnoreSquare = 0;
                v.vc_TrackOn = 0;
                v.vc_FixedNote = 0;
                v.vc_VolumeSlideUp = 0;
                v.vc_VolumeSlideDown = 0;
                v.vc_HardCut = 0;
                v.vc_HardCutRelease = 0;
                v.vc_HardCutReleaseF = 0;
                v.vc_PeriodSlideSpeed = 0;
                v.vc_PeriodSlidePeriod = 0;
                v.vc_PeriodSlideLimit = 0;
                v.vc_PeriodSlideOn = 0;
                v.vc_PeriodSlideWithLimit = 0;
                v.vc_PeriodPerfSlideSpeed = 0;
                v.vc_PeriodPerfSlidePeriod = 0;
                v.vc_PeriodPerfSlideOn = 0;
                v.vc_VibratoDelay = 0;
                v.vc_VibratoCurrent = 0;
                v.vc_VibratoDepth = 0;
                v.vc_VibratoSpeed = 0;
                v.vc_SquareOn = 0;
                v.vc_SquareInit = 0;
                v.vc_SquareLowerLimit = 0;
                v.vc_SquareUpperLimit = 0;
                v.vc_SquarePos = 0;
                v.vc_SquareSign = 0;
                v.vc_SquareSlidingIn = 0;
                v.vc_SquareReverse = 0;
                v.vc_FilterOn = 0;
                v.vc_FilterInit = 0;
                v.vc_FilterLowerLimit = 0;
                v.vc_FilterUpperLimit = 0;
                v.vc_FilterPos = 0;
                v.vc_FilterSign = 0;
                v.vc_FilterSpeed = 0;
                v.vc_FilterSlidingIn = 0;
                v.vc_IgnoreFilter = 0;
                v.vc_PerfCurrent = 0;
                v.vc_PerfSpeed = 0;
                v.vc_WaveLength = 0;
                v.vc_NoteDelayOn = 0;
                v.vc_NoteCutOn = 0;
                v.vc_AudioPeriod = 0;
                v.vc_AudioVolume = 0;
                v.vc_VoiceVolume = 0;
                v.vc_VoicePeriod = 0;
                v.vc_VoiceNum = 0;
                v.vc_WNRandom = 0;
                v.vc_SquareWait = 0;
                v.vc_FilterWait = 0;
                v.vc_PerfWait = 0;
                v.vc_NoteDelayWait = 0;
                v.vc_NoteCutWait = 0;
                v.vc_PerfList = null;
                v.vc_RingSamplePos = 0;
                v.vc_RingDelta = 0;
                v.vc_RingPlantPeriod = 0;
                v.vc_RingAudioPeriod = 0;
                v.vc_RingNewWaveform = 0;
                v.vc_RingWaveform = 0;
                v.vc_RingFixedPeriod = 0;
                v.vc_RingBasePeriod = 0;

                v.vc_RingMixSource = null;
                v.vc_RingAudioSource_array = null;
                v.vc_RingAudioSource_offset = 0;

                Arrays.fill(v.vc_SquareTempBuffer, (byte) 0);

                v.vc_ADSR.aFrames = 0;
                v.vc_ADSR.aVolume = 0;
                v.vc_ADSR.dFrames = 0;
                v.vc_ADSR.dVolume = 0;
                v.vc_ADSR.sFrames = 0;
                v.vc_ADSR.rFrames = 0;
                v.vc_ADSR.rVolume = 0;
                v.vc_ADSR.pad = 0;

                Arrays.fill(v.vc_VoiceBuffer, (byte) 0);
                Arrays.fill(v.vc_RingVoiceBuffer, (byte) 0);
            }

            for (int i = 0; i < MAX_CHANNELS; i++) {
                Voice v = ht_Voices[i];
                v.vc_WNRandom = 0x280;
                v.vc_VoiceNum = i;
                v.vc_TrackMasterVolume = 0x40;
                v.vc_TrackOn = 1;
                v.vc_MixSource = v.vc_VoiceBuffer;
            }
        }

        public void hvl_plist_command_parse(Voice voice, int FX, int FXParam) {
            switch (FX) {
                case 0:
                    if (FXParam > 0 && FXParam < 0x40) {
                        if (voice.vc_IgnoreFilter != 0) {
                            voice.vc_FilterPos = voice.vc_IgnoreFilter;
                            voice.vc_IgnoreFilter = 0;
                        } else {
                            voice.vc_FilterPos = (short) FXParam;
                        }
                        voice.vc_NewWaveform = 1;
                    }
                    break;

                case 1:
                    voice.vc_PeriodPerfSlideSpeed = (short) FXParam;
                    voice.vc_PeriodPerfSlideOn = 1;
                    break;

                case 2:
                    voice.vc_PeriodPerfSlideSpeed = (short) (-FXParam);
                    voice.vc_PeriodPerfSlideOn = 1;
                    break;

                case 3:
                    if (voice.vc_IgnoreSquare == 0) {
                        voice.vc_SquarePos = (short) (FXParam >> (5 - voice.vc_WaveLength));
                    } else {
                        voice.vc_IgnoreSquare = 0;
                    }
                    break;

                case 4:
                    if (FXParam == 0) {
                        voice.vc_SquareOn ^= 1;
                        voice.vc_SquareInit = voice.vc_SquareOn;
                        voice.vc_SquareSign = 1;
                    } else {
                        if ((FXParam & 0x0f) != 0) {
                            voice.vc_SquareOn ^= 1;
                            voice.vc_SquareInit = voice.vc_SquareOn;
                            voice.vc_SquareSign = 1;
                            if ((FXParam & 0x0f) == 0x0f) {
                                voice.vc_SquareSign = -1;
                            }
                        }

                        if ((FXParam & 0xf0) != 0) {
                            voice.vc_FilterOn ^= 1;
                            voice.vc_FilterInit = (short) voice.vc_FilterOn;
                            voice.vc_FilterSign = 1;
                            if ((FXParam & 0xf0) == 0xf0) {
                                voice.vc_FilterSign = -1;
                            }
                        }
                    }
                    break;

                case 5:
                    voice.vc_PerfCurrent = (short) FXParam;
                    break;

                case 7:
                    // Ring modulate with triangle
                    if (FXParam >= 1 && FXParam <= 0x3C) {
                        voice.vc_RingBasePeriod = (short) FXParam;
                        voice.vc_RingFixedPeriod = 1;
                    } else if (FXParam >= 0x81 && FXParam <= 0xBC) {
                        voice.vc_RingBasePeriod = (short) (FXParam - 0x80);
                        voice.vc_RingFixedPeriod = 0;
                    } else {
                        voice.vc_RingBasePeriod = 0;
                        voice.vc_RingFixedPeriod = 0;
                        voice.vc_RingNewWaveform = 0;
                        voice.vc_RingAudioSource_array = null;
                        voice.vc_RingAudioSource_offset = 0;
                        voice.vc_RingMixSource = null;
                        break;
                    }
                    voice.vc_RingWaveform = 0;
                    voice.vc_RingNewWaveform = 1;
                    voice.vc_RingPlantPeriod = 1;
                    break;

                case 8:  // Ring modulate with sawtooth
                    if (FXParam >= 1 && FXParam <= 0x3C) {
                        voice.vc_RingBasePeriod = (short) FXParam;
                        voice.vc_RingFixedPeriod = 1;
                    } else if (FXParam >= 0x81 && FXParam <= 0xBC) {
                        voice.vc_RingBasePeriod = (short) (FXParam - 0x80);
                        voice.vc_RingFixedPeriod = 0;
                    } else {
                        voice.vc_RingBasePeriod = 0;
                        voice.vc_RingFixedPeriod = 0;
                        voice.vc_RingNewWaveform = 0;
                        voice.vc_RingAudioSource_array = null;
                        voice.vc_RingAudioSource_offset = 0;
                        voice.vc_RingMixSource = null;
                        break;
                    }

                    voice.vc_RingWaveform = 1;
                    voice.vc_RingNewWaveform = 1;
                    voice.vc_RingPlantPeriod = 1;
                    break;

                case 9:
                    if (FXParam > 127) {
                        FXParam -= 256;
                    }
                    voice.vc_Pan = (FXParam + 128);
                    voice.vc_PanMultLeft = panning_left[voice.vc_Pan];
                    voice.vc_PanMultRight = panning_right[voice.vc_Pan];
                    break;

                case 12:
                    if (FXParam <= 0x40) {
                        voice.vc_NoteMaxVolume = (short) FXParam;
                        break;
                    }

                    FXParam -= 0x50;
                    if (FXParam < 0) {
                        break;
                    }

                    if (FXParam <= 0x40) {
                        voice.vc_PerfSubVolume = FXParam;
                        break;
                    }

                    FXParam -= 0xa0 - 0x50;
                    if (FXParam < 0) {
                        break;
                    }

                    if (FXParam <= 0x40) {
                        voice.vc_TrackMasterVolume = FXParam;
                    }
                    break;

                case 15:
                    voice.vc_PerfSpeed = voice.vc_PerfWait = (short) FXParam;
                    break;
            }
        }

        public void hvl_play_irq() {
            if (ht_StepWaitFrames <= 0) {
                if (ht_GetNewPosition != 0) {
                    int nextpos = (ht_PosNr + 1 == ht_PositionNr) ? 0 : (ht_PosNr + 1);

                    for (int i = 0; i < ht_Channels; i++) {
                        ht_Voices[i].vc_Track = (short) ht_Positions[ht_PosNr].pos_Track[i];
                        ht_Voices[i].vc_Transpose = ht_Positions[ht_PosNr].pos_Transpose[i];
                        ht_Voices[i].vc_NextTrack = (short) ht_Positions[nextpos].pos_Track[i];
                        ht_Voices[i].vc_NextTranspose = ht_Positions[nextpos].pos_Transpose[i];
                    }
                    ht_GetNewPosition = 0;
                }

                for (int i = 0; i < ht_Channels; i++) {
                    hvl_process_step(ht_Voices[i]);
                }

                ht_StepWaitFrames = ht_Tempo;
            }

            for (int i = 0; i < ht_Channels; i++) {
                hvl_process_frame(ht_Voices[i]);
            }

            ht_PlayingTime++;
            if (ht_Tempo > 0 && --ht_StepWaitFrames <= 0) {
                if (ht_PatternBreak == 0) {
                    ht_NoteNr++;
                    if (ht_NoteNr >= ht_TrackLength) {
                        ht_PosJump = ht_PosNr + 1;
                        ht_PosJumpNote = 0;
                        ht_PatternBreak = 1;
                    }
                }

                if (ht_PatternBreak != 0) {
                    ht_PatternBreak = 0;
                    ht_PosNr = (short) ht_PosJump;
                    ht_NoteNr = (short) ht_PosJumpNote;
                    if (ht_PosNr == ht_PositionNr) {
                        ht_SongEndReached = 1;
                        ht_PosNr = (short) ht_Restart;
                    }
                    ht_PosJumpNote = 0;
                    ht_PosJump = 0;

                    ht_GetNewPosition = 1;
                }
            }

            for (int i = 0; i < ht_Channels; i++) {
                ht_Voices[i].hvl_set_audio(ht_Frequency);
            }
        }

        public void hvl_mixchunk(int samples, short[] buf1, int offset1, short[] buf2, int offset2, int shortStride) {
            byte[][] src = new byte[MAX_CHANNELS][];
            byte[][] rsrc = new byte[MAX_CHANNELS][];
            int[] delta = new int[MAX_CHANNELS];
            int[] rdelta = new int[MAX_CHANNELS];
            int[] vol = new int[MAX_CHANNELS];
            int[] pos = new int[MAX_CHANNELS];
            int[] rpos = new int[MAX_CHANNELS];
            int[] panl = new int[MAX_CHANNELS];
            int[] panr = new int[MAX_CHANNELS];
            int[] vu = new int[MAX_CHANNELS];

            int chans = ht_Channels;
            for (int i = 0; i < chans; i++) {
                delta[i] = ht_Voices[i].vc_Delta;
                vol[i] = ht_Voices[i].vc_VoiceVolume;
                pos[i] = ht_Voices[i].vc_SamplePos;
                src[i] = ht_Voices[i].vc_MixSource;
                panl[i] = ht_Voices[i].vc_PanMultLeft;
                panr[i] = ht_Voices[i].vc_PanMultRight;

                rdelta[i] = ht_Voices[i].vc_RingDelta;
                rpos[i] = ht_Voices[i].vc_RingSamplePos;
                rsrc[i] = ht_Voices[i].vc_RingMixSource;

                vu[i] = 0;
            }

            do {
                int loops = samples;
                for (int i = 0; i < chans; i++) {
                    if (pos[i] >= (0x280 << 16)) {
                        pos[i] -= 0x280 << 16;
                    }
                    int cnt = ((0x280 << 16) - pos[i] - 1) / delta[i] + 1;
                    if (cnt < loops) {
                        loops = cnt;
                    }

                    if (rsrc[i] != null) {
                        if (rpos[i] >= (0x280 << 16)) {
                            rpos[i] -= 0x280 << 16;
                        }
                        cnt = ((0x280 << 16) - rpos[i] - 1) / rdelta[i] + 1;
                        if (cnt < loops) {
                            loops = cnt;
                        }
                    }
                }

                samples -= loops;

                // Inner loop
                do {
                    int a = 0;
                    int b = 0;
                    for (int i = 0; i < chans; i++) {
                        int j;
                        if (rsrc[i] != null) {
                            /* Ring Modulation */
                            j = ((src[i][pos[i] >> 16] * rsrc[i][rpos[i] >> 16]) >> 7) * vol[i];
                            rpos[i] += rdelta[i];
                        } else {
                            j = src[i][pos[i] >> 16] * vol[i];
                        }

                        if (Math.abs(j) > vu[i]) {
                            vu[i] = Math.abs(j);
                        }

                        a += (j * panl[i]) >> 7;
                        b += (j * panr[i]) >> 7;
                        pos[i] += delta[i];
                    }

                    a = (a * ht_mixgain) >> 8;
                    b = (b * ht_mixgain) >> 8;

                    // Clamp a and b to 16-bit signed short limits
                    if (a > 32767) a = 32767;
                    else if (a < -32768) a = -32768;
                    if (b > 32767) b = 32767;
                    else if (b < -32768) b = -32768;

                    buf1[offset1] = (short) a;
                    buf2[offset2] = (short) b;

                    loops--;

                    offset1 += shortStride;
                    offset2 += shortStride;
                } while (loops > 0);
            } while (samples > 0);

            for (int i = 0; i < chans; i++) {
                ht_Voices[i].vc_SamplePos = pos[i];
                ht_Voices[i].vc_RingSamplePos = rpos[i];
                ht_Voices[i].vc_VUMeter = vu[i];
            }
        }

        public void hvl_process_step(Voice voice) {
            if (voice.vc_TrackOn == 0) {
                return;
            }

            voice.vc_VolumeSlideUp = voice.vc_VolumeSlideDown = 0;

            Step step = ht_Tracks[ht_Positions[ht_PosNr].pos_Track[voice.vc_VoiceNum]][ht_NoteNr];

            int Note = step.stp_Note;
            int Instr = step.stp_Instrument;

            int donenotedel = 0;

            // Do notedelay here
            if (((step.stp_FX & 0xf) == 0xe) && ((step.stp_FXParam & 0xf0) == 0xd0)) {
                if (voice.vc_NoteDelayOn != 0) {
                    voice.vc_NoteDelayOn = 0;
                    donenotedel = 1;
                } else {
                    if ((step.stp_FXParam & 0x0f) < ht_Tempo) {
                        voice.vc_NoteDelayWait = (short) (step.stp_FXParam & 0x0f);
                        if (voice.vc_NoteDelayWait != 0) {
                            voice.vc_NoteDelayOn = 1;
                            return;
                        }
                    }
                }
            }

            if ((donenotedel == 0) && ((step.stp_FXb & 0xf) == 0xe) && ((step.stp_FXbParam & 0xf0) == 0xd0)) {
                if (voice.vc_NoteDelayOn != 0) {
                    voice.vc_NoteDelayOn = 0;
                } else {
                    if ((step.stp_FXbParam & 0x0f) < ht_Tempo) {
                        voice.vc_NoteDelayWait = (short) (step.stp_FXbParam & 0x0f);
                        if (voice.vc_NoteDelayWait != 0) {
                            voice.vc_NoteDelayOn = 1;
                            return;
                        }
                    }
                }
            }

            if (Note != 0) {
                voice.vc_OverrideTranspose = 1000;
            }

            hvl_process_stepfx_1(voice, step.stp_FX & 0xf, step.stp_FXParam);
            hvl_process_stepfx_1(voice, step.stp_FXb & 0xf, step.stp_FXbParam);

            if (Instr != 0 && Instr <= ht_InstrumentNr) {
                Instrument Ins = ht_Instruments[Instr];

                voice.vc_Pan = voice.vc_SetPan;
                voice.vc_PanMultLeft = panning_left[voice.vc_Pan];
                voice.vc_PanMultRight = panning_right[voice.vc_Pan];

                voice.vc_PeriodSlideSpeed = voice.vc_PeriodSlidePeriod = voice.vc_PeriodSlideLimit = 0;

                voice.vc_PerfSubVolume = 0x40;
                voice.vc_ADSRVolume = 0;
                voice.vc_Instrument = Ins;
                voice.vc_SamplePos = 0;

                voice.vc_ADSR.aFrames = Ins.ins_Envelope.aFrames;
                voice.vc_ADSR.aVolume = (short) (Ins.ins_Envelope.aVolume * 256 / voice.vc_ADSR.aFrames);
                voice.vc_ADSR.dFrames = Ins.ins_Envelope.dFrames;
                voice.vc_ADSR.dVolume = (short) ((Ins.ins_Envelope.dVolume - Ins.ins_Envelope.aVolume) * 256 / voice.vc_ADSR.dFrames);
                voice.vc_ADSR.sFrames = Ins.ins_Envelope.sFrames;
                voice.vc_ADSR.rFrames = Ins.ins_Envelope.rFrames;
                voice.vc_ADSR.rVolume = (short) ((Ins.ins_Envelope.rVolume - Ins.ins_Envelope.dVolume) * 256 / voice.vc_ADSR.rFrames);

                voice.vc_WaveLength = Ins.ins_WaveLength;
                voice.vc_NoteMaxVolume = (short) Ins.ins_Volume;

                voice.vc_VibratoCurrent = 0;
                voice.vc_VibratoDelay = (short) Ins.ins_VibratoDelay;
                voice.vc_VibratoDepth = (short) Ins.ins_VibratoDepth;
                voice.vc_VibratoSpeed = (short) Ins.ins_VibratoSpeed;
                voice.vc_VibratoPeriod = 0;

                voice.vc_HardCutRelease = Ins.ins_HardCutRelease;
                voice.vc_HardCut = (short) Ins.ins_HardCutReleaseFrames;

                voice.vc_IgnoreSquare = voice.vc_SquareSlidingIn = 0;
                voice.vc_SquareWait = voice.vc_SquareOn = 0;

                int squareLower = Ins.ins_SquareLowerLimit >> (5 - voice.vc_WaveLength);
                int squareUpper = Ins.ins_SquareUpperLimit >> (5 - voice.vc_WaveLength);

                if (squareUpper < squareLower) {
                    int t = squareUpper;
                    squareUpper = squareLower;
                    squareLower = t;
                }

                voice.vc_SquareUpperLimit = (short) squareUpper;
                voice.vc_SquareLowerLimit = (short) squareLower;

                voice.vc_IgnoreFilter = 0;
                voice.vc_FilterWait = 0;
                voice.vc_FilterOn = 0;
                voice.vc_FilterSlidingIn = 0;

                int d6 = Ins.ins_FilterSpeed;
                int d3 = Ins.ins_FilterLowerLimit;
                int d4 = Ins.ins_FilterUpperLimit;

                if ((d3 & 0x80) != 0) d6 |= 0x20;
                if ((d4 & 0x80) != 0) d6 |= 0x40;

                voice.vc_FilterSpeed = (short) d6;
                d3 &= ~0x80;
                d4 &= ~0x80;

                if (d3 > d4) {
                    int t = d3;
                    d3 = d4;
                    d4 = t;
                }

                voice.vc_FilterUpperLimit = (short) d4;
                voice.vc_FilterLowerLimit = (short) d3;
                voice.vc_FilterPos = 32;

                voice.vc_PerfWait = voice.vc_PerfCurrent = 0;
                voice.vc_PerfSpeed = Ins.ins_PList.pls_Speed;
                voice.vc_PerfList = voice.vc_Instrument.ins_PList;

                voice.vc_RingMixSource = null;
                voice.vc_RingSamplePos = 0;
                voice.vc_RingPlantPeriod = 0;
                voice.vc_RingNewWaveform = 0;
            }

            voice.vc_PeriodSlideOn = 0;

            int[] noteArr = {Note};
            this.hvl_process_stepfx_2(voice, step.stp_FX & 0xf, step.stp_FXParam, noteArr);
            this.hvl_process_stepfx_2(voice, step.stp_FXb & 0xf, step.stp_FXbParam, noteArr);
            Note = noteArr[0];

            if (Note != 0) {
                voice.vc_TrackPeriod = Note;
                voice.vc_PlantPeriod = 1;
            }

            this.hvl_process_stepfx_3(voice, step.stp_FX & 0xf, step.stp_FXParam);
            this.hvl_process_stepfx_3(voice, step.stp_FXb & 0xf, step.stp_FXbParam);
        }

        public void hvl_process_stepfx_1(Voice voice, int FX, int FXParam) {
            switch (FX) {
                case 0x0:  // Position Jump HI
                    if (((FXParam & 0x0f) > 0) && ((FXParam & 0x0f) <= 9)) {
                        ht_PosJump = FXParam & 0xf;
                    }
                    break;

                case 0x5:  // Volume Slide + Tone Portamento
                case 0xa:  // Volume Slide
                    voice.vc_VolumeSlideDown = (short) (FXParam & 0x0f);
                    voice.vc_VolumeSlideUp = (short) (FXParam >> 4);
                    break;

                case 0x7:  // Panning
                    if (FXParam > 127) {
                        FXParam -= 256;
                    }
                    voice.vc_Pan = (FXParam + 128);
                    voice.vc_SetPan = (FXParam + 128);
                    voice.vc_PanMultLeft = panning_left[voice.vc_Pan];
                    voice.vc_PanMultRight = panning_right[voice.vc_Pan];
                    break;

                case 0xb: // Position jump
                    ht_PosJump = ht_PosJump * 100 + (FXParam & 0x0f) + (FXParam >> 4) * 10;
                    ht_PatternBreak = 1;
                    if (ht_PosJump <= ht_PosNr) {
                        ht_SongEndReached = 1;
                    }
                    break;

                case 0xd: // Pattern break
                    ht_PosJump = ht_PosNr + 1;
                    ht_PosJumpNote = (FXParam & 0x0f) + (FXParam >> 4) * 10;
                    ht_PatternBreak = 1;
                    if (ht_PosJumpNote > ht_TrackLength) {
                        ht_PosJumpNote = 0;
                    }
                    break;

                case 0xe: // Extended commands
                    switch (FXParam >> 4) {
                        case 0xc: // Note cut
                            if ((FXParam & 0x0f) < ht_Tempo) {
                                voice.vc_NoteCutWait = (short) (FXParam & 0x0f);
                                if (voice.vc_NoteCutWait != 0) {
                                    voice.vc_NoteCutOn = 1;
                                    voice.vc_HardCutRelease = 0;
                                }
                            }
                            break;
                    }
                    break;

                case 0xf: // Speed
                    ht_Tempo = (short) FXParam;
                    if (FXParam == 0) {
                        ht_SongEndReached = 1;
                    }
                    break;
            }
        }

        public void hvl_process_frame(Voice voice) {
            if (voice.vc_TrackOn == 0) {
                return;
            }

            if (voice.vc_NoteDelayOn != 0) {
                if (voice.vc_NoteDelayWait <= 0) {
                    this.hvl_process_step(voice);
                } else {
                    voice.vc_NoteDelayWait--;
                }
            }

            if (voice.vc_HardCut != 0) {
                int nextinst;

                if (ht_NoteNr + 1 < ht_TrackLength) {
                    nextinst = ht_Tracks[voice.vc_Track][ht_NoteNr + 1].stp_Instrument;
                } else {
                    nextinst = ht_Tracks[voice.vc_NextTrack][0].stp_Instrument;
                }

                if (nextinst != 0) {
                    int d1 = ht_Tempo - voice.vc_HardCut;
                    if (d1 < 0) d1 = 0;

                    if (voice.vc_NoteCutOn == 0) {
                        voice.vc_NoteCutOn = 1;
                        voice.vc_NoteCutWait = (short) d1;
                        voice.vc_HardCutReleaseF = (short) (-(d1 - ht_Tempo));
                    } else {
                        voice.vc_HardCut = 0;
                    }
                }
            }

            if (voice.vc_NoteCutOn != 0) {
                if (voice.vc_NoteCutWait <= 0) {
                    voice.vc_NoteCutOn = 0;
                    if (voice.vc_HardCutRelease != 0) {
                        voice.vc_ADSR.rVolume = (short) (-(voice.vc_ADSRVolume - (voice.vc_Instrument.ins_Envelope.rVolume << 8)) / voice.vc_HardCutReleaseF);
                        voice.vc_ADSR.rFrames = voice.vc_HardCutReleaseF;
                        voice.vc_ADSR.aFrames = voice.vc_ADSR.dFrames = voice.vc_ADSR.sFrames = 0;
                    } else {
                        voice.vc_NoteMaxVolume = 0;
                    }
                } else {
                    voice.vc_NoteCutWait--;
                }
            }

            // ADSR envelope
            if (voice.vc_ADSR.aFrames != 0) {
                voice.vc_ADSRVolume += voice.vc_ADSR.aVolume;
                if (--voice.vc_ADSR.aFrames <= 0) {
                    voice.vc_ADSRVolume = voice.vc_Instrument.ins_Envelope.aVolume << 8;
                }
            } else if (voice.vc_ADSR.dFrames != 0) {
                voice.vc_ADSRVolume += voice.vc_ADSR.dVolume;
                if (--voice.vc_ADSR.dFrames <= 0) {
                    voice.vc_ADSRVolume = voice.vc_Instrument.ins_Envelope.dVolume << 8;
                }
            } else if (voice.vc_ADSR.sFrames != 0) {
                voice.vc_ADSR.sFrames--;
            } else if (voice.vc_ADSR.rFrames != 0) {
                voice.vc_ADSRVolume += voice.vc_ADSR.rVolume;
                if (--voice.vc_ADSR.rFrames <= 0) {
                    voice.vc_ADSRVolume = voice.vc_Instrument.ins_Envelope.rVolume << 8;
                }
            }

            // VolumeSlide
            voice.vc_NoteMaxVolume = (short) (voice.vc_NoteMaxVolume + voice.vc_VolumeSlideUp - voice.vc_VolumeSlideDown);

            if (voice.vc_NoteMaxVolume < 0) {
                voice.vc_NoteMaxVolume = 0;
            } else if (voice.vc_NoteMaxVolume > 0x40) {
                voice.vc_NoteMaxVolume = 0x40;
            }

            // Portamento
            if (voice.vc_PeriodSlideOn != 0) {
                if (voice.vc_PeriodSlideWithLimit != 0) {
                    int d0 = voice.vc_PeriodSlidePeriod - voice.vc_PeriodSlideLimit;
                    int d2 = voice.vc_PeriodSlideSpeed;

                    if (d0 > 0) {
                        d2 = -d2;
                    }

                    if (d0 != 0) {
                        int d3 = (d0 + d2) ^ d0;
                        if (d3 >= 0) {
                            d0 = voice.vc_PeriodSlidePeriod + d2;
                        } else {
                            d0 = voice.vc_PeriodSlideLimit;
                        }
                        voice.vc_PeriodSlidePeriod = (short) d0;
                        voice.vc_PlantPeriod = 1;
                    }
                } else {
                    voice.vc_PeriodSlidePeriod += voice.vc_PeriodSlideSpeed;
                    voice.vc_PlantPeriod = 1;
                }
            }

            // Vibrato
            if (voice.vc_VibratoDepth != 0) {
                if (voice.vc_VibratoDelay <= 0) {
                    voice.vc_VibratoPeriod = (short) ((VIB_TAB[voice.vc_VibratoCurrent] * voice.vc_VibratoDepth) >> 7);
                    voice.vc_PlantPeriod = 1;
                    voice.vc_VibratoCurrent = (short) ((voice.vc_VibratoCurrent + voice.vc_VibratoSpeed) & 0x3f);
                } else {
                    voice.vc_VibratoDelay--;
                }
            }

            // PList
            if (voice.vc_PerfList != null) {
                if (voice.vc_Instrument != null && voice.vc_PerfCurrent < voice.vc_Instrument.ins_PList.pls_Length) {
                    if (--voice.vc_PerfWait <= 0) {
                        int cur = voice.vc_PerfCurrent++;
                        voice.vc_PerfWait = voice.vc_PerfSpeed;

                        if (voice.vc_PerfList.pls_Entries[cur].ple_Waveform != 0) {
                            voice.vc_Waveform = voice.vc_PerfList.pls_Entries[cur].ple_Waveform - 1;
                            voice.vc_NewWaveform = 1;
                            voice.vc_PeriodPerfSlideSpeed = voice.vc_PeriodPerfSlidePeriod = 0;
                        }

                        // Holdwave
                        voice.vc_PeriodPerfSlideOn = 0;

                        for (int i = 0; i < 2; i++) {
                            this.hvl_plist_command_parse(voice, voice.vc_PerfList.pls_Entries[cur].ple_FX[i] & 0xff, voice.vc_PerfList.pls_Entries[cur].ple_FXParam[i] & 0xff);
                        }

                        // GetNote
                        if (voice.vc_PerfList.pls_Entries[cur].ple_Note != 0) {
                            voice.vc_InstrPeriod = voice.vc_PerfList.pls_Entries[cur].ple_Note;
                            voice.vc_PlantPeriod = 1;
                            voice.vc_FixedNote = voice.vc_PerfList.pls_Entries[cur].ple_Fixed;
                        }
                    }
                } else {
                    if (voice.vc_PerfWait != 0) {
                        voice.vc_PerfWait--;
                    } else {
                        voice.vc_PeriodPerfSlideSpeed = 0;
                    }
                }
            }

            // PerfPortamento
            if (voice.vc_PeriodPerfSlideOn != 0) {
                voice.vc_PeriodPerfSlidePeriod -= voice.vc_PeriodPerfSlideSpeed;
                if (voice.vc_PeriodPerfSlidePeriod != 0) {
                    voice.vc_PlantPeriod = 1;
                }
            }

            if (voice.vc_Waveform == 3 - 1 && voice.vc_SquareOn != 0) {
                if (--voice.vc_SquareWait <= 0) {
                    int d1 = voice.vc_SquareLowerLimit;
                    int d2 = voice.vc_SquareUpperLimit;
                    int d3 = voice.vc_SquarePos;

                    if (voice.vc_SquareInit != 0) {
                        voice.vc_SquareInit = 0;
                        if (d3 <= d1) {
                            voice.vc_SquareSlidingIn = 1;
                            voice.vc_SquareSign = 1;
                        } else if (d3 >= d2) {
                            voice.vc_SquareSlidingIn = 1;
                            voice.vc_SquareSign = -1;
                        }
                    }

                    // NoSquareInit
                    if (d1 == d3 || d2 == d3) {
                        if (voice.vc_SquareSlidingIn != 0) {
                            voice.vc_SquareSlidingIn = 0;
                        } else {
                            voice.vc_SquareSign = (short) (-voice.vc_SquareSign);
                        }
                    }

                    d3 += voice.vc_SquareSign;
                    voice.vc_SquarePos = (short) d3;
                    voice.vc_PlantSquare = 1;
                    voice.vc_SquareWait = (short) voice.vc_Instrument.ins_SquareSpeed;
                }
            }

            if (voice.vc_FilterOn != 0 && --voice.vc_FilterWait <= 0) {
                int d1 = voice.vc_FilterLowerLimit;
                int d2 = voice.vc_FilterUpperLimit;
                int d3 = voice.vc_FilterPos;

                if (voice.vc_FilterInit != 0) {
                    voice.vc_FilterInit = 0;
                    if (d3 <= d1) {
                        voice.vc_FilterSlidingIn = 1;
                        voice.vc_FilterSign = 1;
                    } else if (d3 >= d2) {
                        voice.vc_FilterSlidingIn = 1;
                        voice.vc_FilterSign = -1;
                    }
                }

                // NoFilterInit
                int fMax = (voice.vc_FilterSpeed < 3) ? (5 - voice.vc_FilterSpeed) : 1;

                for (int i = 0; i < fMax; i++) {
                    if (d1 == d3 || d2 == d3) {
                        if (voice.vc_FilterSlidingIn != 0) {
                            voice.vc_FilterSlidingIn = 0;
                        } else {
                            voice.vc_FilterSign = (short) (-voice.vc_FilterSign);
                        }
                    }
                    d3 += voice.vc_FilterSign;
                }

                if (d3 < 1) d3 = 1;
                if (d3 > 63) d3 = 63;
                voice.vc_FilterPos = (short) d3;
                voice.vc_NewWaveform = 1;
                voice.vc_FilterWait = (short) (voice.vc_FilterSpeed - 3);

                if (voice.vc_FilterWait < 1) {
                    voice.vc_FilterWait = 1;
                }
            }

            if (voice.vc_Waveform == 3 - 1 || voice.vc_PlantSquare != 0) {
                // CalcSquare
                int squarePtrIdx = WO_SQUARES + (voice.vc_FilterPos - 0x20) * (0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 0x280 * 3);
                int X = voice.vc_SquarePos << (5 - voice.vc_WaveLength);

                if (X > 0x20) {
                    X = 0x40 - X;
                    voice.vc_SquareReverse = 1;
                }

                if (X > 0) {
                    squarePtrIdx += (X - 1) << 7;
                }

                int delta = 32 >> voice.vc_WaveLength;

                int len = (1 << voice.vc_WaveLength) * 4;
                for (int i = 0; i < len; i++) {
                    voice.vc_SquareTempBuffer[i] = waves[squarePtrIdx];
                    squarePtrIdx += delta;
                }

                voice.vc_NewWaveform = 1;
                voice.vc_Waveform = 3 - 1;
                voice.vc_PlantSquare = 0;
            }

            if (voice.vc_Waveform == 4 - 1) {
                voice.vc_NewWaveform = 1;
            }

            if (voice.vc_RingNewWaveform != 0) {
                int ringWf = voice.vc_RingWaveform;
                if (ringWf > 1) ringWf = 1;

                int rasrcIdx;
                if (ringWf == 0) {
                    rasrcIdx = WO_TRIANGLE_04;
                } else {
                    rasrcIdx = WO_SAWTOOTH_04;
                }
                rasrcIdx += OFFSETS[voice.vc_WaveLength];

                voice.vc_RingAudioSource_array = waves;
                voice.vc_RingAudioSource_offset = rasrcIdx;
            }

            if (voice.vc_NewWaveform != 0) {
                byte[] audioSource_array;
                int audioSource_offset;

                if (voice.vc_Waveform == 2) {
                    audioSource_array = voice.vc_SquareTempBuffer;
                    audioSource_offset = 0;
                } else {
                    audioSource_array = waves;
                    if (voice.vc_Waveform == 0) {
                        audioSource_offset = WO_TRIANGLE_04;
                    } else if (voice.vc_Waveform == 1) {
                        audioSource_offset = WO_SAWTOOTH_04;
                    } else { // 3
                        audioSource_offset = WO_WHITENOISE;
                    }
                }

                if (voice.vc_Waveform != 3 - 1) {
                    audioSource_offset += (voice.vc_FilterPos - 0x20) * (0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 0x280 * 3);
                }

                if (voice.vc_Waveform < 3 - 1) {
                    audioSource_offset += OFFSETS[voice.vc_WaveLength];
                }

                if (voice.vc_Waveform == 4 - 1) {
                    audioSource_offset += (voice.vc_WNRandom & (2 * 0x280 - 1)) & ~1;
                    // GoOnRandom
                    voice.vc_WNRandom += 2239384;
                    voice.vc_WNRandom = ((((voice.vc_WNRandom >>> 8) | (voice.vc_WNRandom << 24)) + 782323) ^ 75) - 6735;
                }

                voice.vc_AudioSource_array = audioSource_array;
                voice.vc_AudioSource_offset = audioSource_offset;
            }

            // Ring modulation period calculation
            if (voice.vc_RingAudioSource_array != null) {
                voice.vc_RingAudioPeriod = voice.vc_RingBasePeriod;

                if (voice.vc_RingFixedPeriod == 0) {
                    if (voice.vc_OverrideTranspose != 1000) {
                        voice.vc_RingAudioPeriod += voice.vc_OverrideTranspose + voice.vc_TrackPeriod - 1;
                    } else {
                        voice.vc_RingAudioPeriod += voice.vc_Transpose + voice.vc_TrackPeriod - 1;
                    }
                }

                if (voice.vc_RingAudioPeriod > 5 * 12) {
                    voice.vc_RingAudioPeriod = 5 * 12;
                }

                if (voice.vc_RingAudioPeriod < 0) {
                    voice.vc_RingAudioPeriod = 0;
                }

                voice.vc_RingAudioPeriod = (short) PERIOD_TAB[voice.vc_RingAudioPeriod];

                if (voice.vc_RingFixedPeriod == 0) {
                    voice.vc_RingAudioPeriod += voice.vc_PeriodSlidePeriod;
                }

                voice.vc_RingAudioPeriod += voice.vc_PeriodPerfSlidePeriod + voice.vc_VibratoPeriod;

                if (voice.vc_RingAudioPeriod > 0x0d60) {
                    voice.vc_RingAudioPeriod = 0x0d60;
                }

                if (voice.vc_RingAudioPeriod < 0x0071) {
                    voice.vc_RingAudioPeriod = 0x0071;
                }
            }

            // Normal period calculation
            voice.vc_AudioPeriod = (short) voice.vc_InstrPeriod;

            if (voice.vc_FixedNote == 0) {
                if (voice.vc_OverrideTranspose != 1000) {
                    voice.vc_AudioPeriod += voice.vc_OverrideTranspose + voice.vc_TrackPeriod - 1;
                } else {
                    voice.vc_AudioPeriod += voice.vc_Transpose + voice.vc_TrackPeriod - 1;
                }
            }

            if (voice.vc_AudioPeriod > 5 * 12) {
                voice.vc_AudioPeriod = 5 * 12;
            }

            if (voice.vc_AudioPeriod < 0) {
                voice.vc_AudioPeriod = 0;
            }

            voice.vc_AudioPeriod = (short) PERIOD_TAB[voice.vc_AudioPeriod];

            if (voice.vc_FixedNote == 0) {
                voice.vc_AudioPeriod += voice.vc_PeriodSlidePeriod;
            }

            voice.vc_AudioPeriod += voice.vc_PeriodPerfSlidePeriod + voice.vc_VibratoPeriod;

            if (voice.vc_AudioPeriod > 0x0d60) {
                voice.vc_AudioPeriod = 0x0d60;
            }

            if (voice.vc_AudioPeriod < 0x0071) {
                voice.vc_AudioPeriod = 0x0071;
            }

            voice.vc_AudioVolume = (short) (((((((voice.vc_ADSRVolume >> 8) * voice.vc_NoteMaxVolume) >> 6) * voice.vc_PerfSubVolume) >> 6) * voice.vc_TrackMasterVolume) >> 6);
        }

        public void hvl_FreeTune() {
            // No-op in Java (GC handles it)
        }

        public boolean hvl_InitSubsong(int nr) {
            if (nr > ht_SubsongNr) {
                return false;
            }

            ht_SongNum = nr;

            int posNr = 0;
            if (nr != 0) {
                posNr = ht_Subsongs[nr - 1];
            }

            ht_PosNr = (short) posNr;
            ht_PosJump = 0;
            ht_PatternBreak = 0;
            ht_NoteNr = 0;
            ht_PosJumpNote = 0;
            ht_Tempo = 6;
            ht_StepWaitFrames = 0;
            ht_GetNewPosition = 1;
            ht_SongEndReached = 0;
            ht_PlayingTime = 0;

            for (int i = 0; i < MAX_CHANNELS; i += 4) {
                ht_Voices[i + 0].vc_Pan = ht_defpanleft;
                ht_Voices[i + 0].vc_SetPan = ht_defpanleft;
                ht_Voices[i + 0].vc_PanMultLeft = panning_left[ht_defpanleft];
                ht_Voices[i + 0].vc_PanMultRight = panning_right[ht_defpanleft];

                ht_Voices[i + 1].vc_Pan = ht_defpanright;
                ht_Voices[i + 1].vc_SetPan = ht_defpanright;
                ht_Voices[i + 1].vc_PanMultLeft = panning_left[ht_defpanright];
                ht_Voices[i + 1].vc_PanMultRight = panning_right[ht_defpanright];

                ht_Voices[i + 2].vc_Pan = ht_defpanright;
                ht_Voices[i + 2].vc_SetPan = ht_defpanright;
                ht_Voices[i + 2].vc_PanMultLeft = panning_left[ht_defpanright];
                ht_Voices[i + 2].vc_PanMultRight = panning_right[ht_defpanright];

                ht_Voices[i + 3].vc_Pan = ht_defpanleft;
                ht_Voices[i + 3].vc_SetPan = ht_defpanleft;
                ht_Voices[i + 3].vc_PanMultLeft = panning_left[ht_defpanleft];
                ht_Voices[i + 3].vc_PanMultRight = panning_right[ht_defpanleft];
            }

            this.hvl_reset_some_stuff();

            return true;
        }
    }

    // --- Replayer static functions ---

    public static double Period2Freq(double period) {
        return (3546897.0 * 65536.0) / period;
    }

    public static void hvl_GenPanningTables() {
        double aa = (Math.PI * 2.0) / 4.0;
        double ab = 0.0;
        for (int i = 0; i < 256; i++) {
            panning_left[i] = (int) (Math.sin(aa) * 255.0);
            panning_right[i] = (int) (Math.sin(ab) * 255.0);
            aa += (Math.PI * 2.0 / 4.0) / 256.0;
            ab += (Math.PI * 2.0 / 4.0) / 256.0;
        }
        panning_left[255] = 0;
        panning_right[0] = 0;
    }

    public static void hvl_GenSawtooth(byte[] buf, int offset, int len) {
        int val = -128;
        int add = 256 / (len - 1);
        for (int i = 0; i < len; i++, val += add) {
            buf[offset + i] = (byte) val;
        }
    }

    public static void hvl_GenTriangle(byte[] buf, int offset, int len) {
        int d2 = len;
        int d5 = len >> 2;
        int d1 = 128 / d5;
        int d4 = -(d2 >> 1);
        int val = 0;
        int bufIdx = offset;

        for (int i = 0; i < d5; i++) {
            buf[bufIdx++] = (byte) val;
            val += d1;
        }
        buf[bufIdx++] = 0x7f;

        if (d5 != 1) {
            val = 128;
            for (int i = 0; i < d5 - 1; i++) {
                val -= d1;
                buf[bufIdx++] = (byte) val;
            }
        }

        int buf2Idx = bufIdx + d4;
        for (int i = 0; i < d5 * 2; i++) {
            byte c = buf[buf2Idx++];
            if (c == 0x7f) {
                c = (byte) 0x80;
            } else {
                c = (byte) -c;
            }
            buf[bufIdx++] = c;
        }
    }

    public static void hvl_GenSquare(byte[] buf, int offset) {
        int bufIdx = offset;
        for (int i = 1; i <= 0x20; i++) {
            for (int j = 0; j < (0x40 - i) * 2; j++) {
                buf[bufIdx++] = (byte) 0x80;
            }
            for (int j = 0; j < i * 2; j++) {
                buf[bufIdx++] = 0x7f;
            }
        }
    }

    private static double clip(double x) {
        if (x > 127.0) {
            x = 127.0;
        } else if (x < -128.0) {
            x = -128.0;
        }
        return x;
    }

    public static void hvl_GenFilterWaves(byte[] buf, int bufOff, byte[] lowbuf, int lowbufOff, byte[] highbuf, int highbufOff) {
        int[] lengthTable = {
                3, 7, 0xf, 0x1f, 0x3f, 0x7f, 3, 7, 0xf, 0x1f, 0x3f, 0x7f,
                0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
                0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
                (0x280 * 3) - 1
        };

        double freq;
        int temp;

        int lowIdx = lowbufOff;
        int highIdx = highbufOff;

        for (temp = 0, freq = 8.0; temp < 31; temp++, freq += 3.0) {
            int a0Idx = bufOff;

            for (int wavesVal = 0; wavesVal < 6 + 6 + 0x20 + 1; wavesVal++) {
                double fre, high, mid, low;
                int i;

                mid = 0.0;
                low = 0.0;
                fre = freq * 1.25 / 100.0;

                int lenVal = lengthTable[wavesVal];

                for (i = 0; i <= lenVal; i++) {
                    high = buf[a0Idx + i] - mid - low;
                    high = clip(high);
                    mid += high * fre;
                    mid = clip(mid);
                    low += mid * fre;
                    low = clip(low);
                }

                for (i = 0; i <= lenVal; i++) {
                    high = buf[a0Idx + i] - mid - low;
                    high = clip(high);
                    mid += high * fre;
                    mid = clip(mid);
                    low += mid * fre;
                    low = clip(low);
                    lowbuf[lowIdx++] = (byte) low;
                    highbuf[highIdx++] = (byte) high;
                }

                a0Idx += lenVal + 1;
            }
        }
    }

    public static void hvl_GenWhiteNoise(byte[] buf, int offset, int len) {
        int bufIdx = offset;
        int ays = 0x41595321;
        do {
            byte s = (byte) ays;
            if ((ays & 0x100) != 0) {
                s = (byte) 0x80;
                if ((ays & 0xffff) >= 0) {
                    s = 0x7f;
                }
            }
            buf[bufIdx++] = s;
            len--;

            ays = (ays >>> 5) | (ays << 27);
            ays = (ays & 0xffffff00) | ((ays & 0xff) ^ 0x9a);
            int bx = ays & 0xFFFF;
            ays = (ays << 2) | (ays >>> 30);
            int ax = ays & 0xFFFF;
            bx = (bx + ax) & 0xFFFF;
            ax ^= bx;
            ays = (ays & 0xffff0000) | ax;
            ays = (ays >>> 3) | (ays << 29);
        } while (len > 0);
    }

    public static void hvl_InitReplayer() {
        hvl_GenPanningTables();
        hvl_GenSawtooth(waves, WO_SAWTOOTH_04, 0x04);
        hvl_GenSawtooth(waves, WO_SAWTOOTH_08, 0x08);
        hvl_GenSawtooth(waves, WO_SAWTOOTH_10, 0x10);
        hvl_GenSawtooth(waves, WO_SAWTOOTH_20, 0x20);
        hvl_GenSawtooth(waves, WO_SAWTOOTH_40, 0x40);
        hvl_GenSawtooth(waves, WO_SAWTOOTH_80, 0x80);
        hvl_GenTriangle(waves, WO_TRIANGLE_04, 0x04);
        hvl_GenTriangle(waves, WO_TRIANGLE_08, 0x08);
        hvl_GenTriangle(waves, WO_TRIANGLE_10, 0x10);
        hvl_GenTriangle(waves, WO_TRIANGLE_20, 0x20);
        hvl_GenTriangle(waves, WO_TRIANGLE_40, 0x40);
        hvl_GenTriangle(waves, WO_TRIANGLE_80, 0x80);
        hvl_GenSquare(waves, WO_SQUARES);
        hvl_GenWhiteNoise(waves, WO_WHITENOISE, WHITENOISELEN);
        hvl_GenFilterWaves(waves, WO_TRIANGLE_04, waves, WO_LOWPASSES, waves, WO_HIGHPASSES);
    }

    private static String readString(byte[] buf, int offset, int maxLen) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLen; i++) {
            if (offset + i >= buf.length) break;
            int c = buf[offset + i] & 0xFF;
            if (c == 0) break;
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static Tune hvl_load_ahx(byte[] buf, int buflen, int defstereo, int freq) {
        int i, j, k, l, posn, insn, ssn, trkn, trkl;
        Tune ht = new Tune();

        posn = (((buf[6] & 0x0f) << 8) | (buf[7] & 0xFF));
        insn = buf[12] & 0xFF;
        ssn = buf[13] & 0xFF;
        trkl = buf[10] & 0xFF;
        trkn = buf[11] & 0xFF;

        int[] defgain = {71, 72, 76, 85, 100};

        ht.ht_Frequency = freq;
        ht.ht_FreqF = freq;
        ht.ht_Channels = 4;
        ht.ht_PositionNr = posn;
        ht.ht_Restart = (((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF));
        ht.ht_SpeedMultiplier = (((buf[6] & 0xFF) >> 5) & 3) + 1;
        ht.ht_TrackLength = trkl;
        ht.ht_TrackNr = trkn;
        ht.ht_InstrumentNr = insn;
        ht.ht_SubsongNr = ssn;
        ht.ht_defstereo = defstereo;
        ht.ht_defpanleft = stereopan_left[ht.ht_defstereo];
        ht.ht_defpanright = stereopan_right[ht.ht_defstereo];
        ht.ht_mixgain = (defgain[ht.ht_defstereo] * 256) / 100;

        if (ht.ht_Restart >= ht.ht_PositionNr) {
            ht.ht_Restart = ht.ht_PositionNr - 1;
        }

        if (ht.ht_PositionNr > 1000 || ht.ht_TrackLength > 64 || ht.ht_InstrumentNr > 64) {
            System.out.printf("%d,%d,%d\n", ht.ht_PositionNr, ht.ht_TrackLength, ht.ht_InstrumentNr);
            System.out.println("Invalid file.");
            return null;
        }

        int nameOffset = (((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF));
        ht.ht_Name = readString(buf, nameOffset, 80);
        int nptr_idx = nameOffset + ht.ht_Name.length() + 1;

        int bptr_idx = 14;

        // Subsongs
        ht.ht_Subsongs = new int[ssn];
        for (i = 0; i < ht.ht_SubsongNr; i++) {
            ht.ht_Subsongs[i] = (((buf[bptr_idx] & 0xFF) << 8) | (buf[bptr_idx + 1] & 0xFF));
            if (ht.ht_Subsongs[i] >= ht.ht_PositionNr) {
                ht.ht_Subsongs[i] = 0;
            }
            bptr_idx += 2;
        }

        // Position list
        ht.ht_Positions = new Position[posn];
        for (i = 0; i < posn; i++) {
            ht.ht_Positions[i] = new Position();
        }
        for (i = 0; i < ht.ht_PositionNr; i++) {
            for (j = 0; j < 4; j++) {
                ht.ht_Positions[i].pos_Track[j] = (buf[bptr_idx++] & 0xFF);
                ht.ht_Positions[i].pos_Transpose[j] = buf[bptr_idx++];
            }
        }

        // Tracks
        for (i = 0; i <= ht.ht_TrackNr; i++) {
            if (((buf[6] & 0x80) == 0x80) && (i == 0)) {
                for (j = 0; j < ht.ht_TrackLength; j++) {
                    ht.ht_Tracks[i][j].stp_Note = 0;
                    ht.ht_Tracks[i][j].stp_Instrument = 0;
                    ht.ht_Tracks[i][j].stp_FX = 0;
                    ht.ht_Tracks[i][j].stp_FXParam = 0;
                    ht.ht_Tracks[i][j].stp_FXb = 0;
                    ht.ht_Tracks[i][j].stp_FXbParam = 0;
                }
                continue;
            }

            for (j = 0; j < ht.ht_TrackLength; j++) {
                int b0 = buf[bptr_idx] & 0xFF;
                int b1 = buf[bptr_idx + 1] & 0xFF;
                int b2 = buf[bptr_idx + 2] & 0xFF;
                ht.ht_Tracks[i][j].stp_Note = (b0 >> 2) & 0x3f;
                ht.ht_Tracks[i][j].stp_Instrument = ((b0 & 0x3) << 4) | (b1 >> 4);
                ht.ht_Tracks[i][j].stp_FX = b1 & 0xf;
                ht.ht_Tracks[i][j].stp_FXParam = b2;
                ht.ht_Tracks[i][j].stp_FXb = 0;
                ht.ht_Tracks[i][j].stp_FXbParam = 0;
                bptr_idx += 3;
            }
        }

        // Instruments
        ht.ht_Instruments = new Instrument[insn + 1];
        for (int ins = 0; ins <= insn; ins++) {
            ht.ht_Instruments[ins] = new Instrument();
        }
        for (i = 1; i <= ht.ht_InstrumentNr; i++) {
            if (nptr_idx < buf.length) {
                ht.ht_Instruments[i].ins_Name = readString(buf, nptr_idx, 32);
                nptr_idx += ht.ht_Instruments[i].ins_Name.length() + 1;
            } else {
                ht.ht_Instruments[i].ins_Name = "";
            }

            ht.ht_Instruments[i].ins_Volume = (buf[bptr_idx] & 0xFF);
            ht.ht_Instruments[i].ins_FilterSpeed = (((buf[bptr_idx + 1] & 0xFF) >> 3) & 0x1f) | (((buf[bptr_idx + 12] & 0xFF) >> 2) & 0x20);
            ht.ht_Instruments[i].ins_WaveLength = (buf[bptr_idx + 1] & 0x07);

            ht.ht_Instruments[i].ins_Envelope.aFrames = (short) (buf[bptr_idx + 2] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.aVolume = (short) (buf[bptr_idx + 3] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.dFrames = (short) (buf[bptr_idx + 4] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.dVolume = (short) (buf[bptr_idx + 5] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.sFrames = (short) (buf[bptr_idx + 6] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.rFrames = (short) (buf[bptr_idx + 7] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.rVolume = (short) (buf[bptr_idx + 8] & 0xFF);

            ht.ht_Instruments[i].ins_FilterLowerLimit = (buf[bptr_idx + 12] & 0x7f);
            ht.ht_Instruments[i].ins_VibratoDelay = (buf[bptr_idx + 13] & 0xFF);
            ht.ht_Instruments[i].ins_HardCutReleaseFrames = ((buf[bptr_idx + 14] & 0xFF) >> 4) & 0x07;
            ht.ht_Instruments[i].ins_HardCutRelease = (buf[bptr_idx + 14] & 0x80) != 0 ? 1 : 0;
            ht.ht_Instruments[i].ins_VibratoDepth = (buf[bptr_idx + 14] & 0x0f);
            ht.ht_Instruments[i].ins_VibratoSpeed = (buf[bptr_idx + 15] & 0xFF);
            ht.ht_Instruments[i].ins_SquareLowerLimit = (buf[bptr_idx + 16] & 0xFF);
            ht.ht_Instruments[i].ins_SquareUpperLimit = (buf[bptr_idx + 17] & 0xFF);
            ht.ht_Instruments[i].ins_SquareSpeed = (buf[bptr_idx + 18] & 0xFF);
            ht.ht_Instruments[i].ins_FilterUpperLimit = (buf[bptr_idx + 19] & 0x3f);
            ht.ht_Instruments[i].ins_PList.pls_Speed = (short) (buf[bptr_idx + 20] & 0xFF);
            ht.ht_Instruments[i].ins_PList.pls_Length = (short) (buf[bptr_idx + 21] & 0xFF);

            int plsLength = ht.ht_Instruments[i].ins_PList.pls_Length;
            ht.ht_Instruments[i].ins_PList.pls_Entries = new PlsEntry[plsLength];
            for (int entryIdx = 0; entryIdx < plsLength; entryIdx++) {
                ht.ht_Instruments[i].ins_PList.pls_Entries[entryIdx] = new PlsEntry();
            }

            bptr_idx += 22;
            for (j = 0; j < plsLength; j++) {
                int pb0 = buf[bptr_idx] & 0xFF;
                int pb1 = buf[bptr_idx + 1] & 0xFF;
                int pb2 = buf[bptr_idx + 2] & 0xFF;
                int pb3 = buf[bptr_idx + 3] & 0xFF;

                k = (pb0 >> 5) & 7;
                if (k == 6) k = 12;
                if (k == 7) k = 15;
                l = (pb0 >> 2) & 7;
                if (l == 6) l = 12;
                if (l == 7) l = 15;

                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FX[1] = (byte) k;
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FX[0] = (byte) l;
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_Waveform = (byte) (((pb0 << 1) & 6) | (pb1 >> 7));
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_Fixed = (short) ((pb1 >> 6) & 1);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_Note = (byte) (pb1 & 0x3f);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FXParam[0] = (byte) pb2;
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FXParam[1] = (byte) pb3;

                if ((buf[3] == 0) && (l == 4) && ((pb2 & 0xf0) != 0)) {
                    ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FXParam[0] &= 0x0f;
                }
                if ((buf[3] == 0) && (k == 4) && ((pb3 & 0xf0) != 0)) {
                    ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FXParam[0] &= 0x0f;
                }

                bptr_idx += 4;
            }
        }

        ht.hvl_InitSubsong(0);
        return ht;
    }

    public static Tune hvl_reset(byte[] buf, int buflen, int defstereo, int freq, boolean freeit) {
        int i, j, posn, insn, ssn, chnn, trkl, trkn;

        if (buf[0] == 'T' && buf[1] == 'H' && buf[2] == 'X' && buf[3] < 3) {
            return hvl_load_ahx(buf, buflen, defstereo, freq);
        }

        if (buf[0] != 'H' || buf[1] != 'V' || buf[2] != 'L' || buf[3] > 1) {
            System.out.println("Invalid file.");
            return null;
        }

        posn = (((buf[6] & 0x0f) << 8) | (buf[7] & 0xFF));
        insn = buf[12] & 0xFF;
        ssn = buf[13] & 0xFF;
        chnn = ((buf[8] & 0xFF) >> 2) + 4;
        trkl = buf[10] & 0xFF;
        trkn = buf[11] & 0xFF;

        Tune ht = new Tune();

        ht.ht_Version = buf[3] & 0xFF;
        ht.ht_Frequency = freq;
        ht.ht_FreqF = freq;

        ht.ht_PositionNr = posn;
        ht.ht_Channels = chnn;
        ht.ht_Restart = (((buf[8] & 3) << 8) | (buf[9] & 0xFF));
        ht.ht_SpeedMultiplier = (((buf[6] & 0xFF) >> 5) & 3) + 1;
        ht.ht_TrackLength = trkl;
        ht.ht_TrackNr = trkn;
        ht.ht_InstrumentNr = insn;
        ht.ht_SubsongNr = ssn;
        ht.ht_mixgain = ((buf[14] & 0xFF) << 8) / 100;
        ht.ht_defstereo = buf[15] & 0xFF;
        ht.ht_defpanleft = stereopan_left[ht.ht_defstereo];
        ht.ht_defpanright = stereopan_right[ht.ht_defstereo];

        if (ht.ht_Restart >= ht.ht_PositionNr) {
            ht.ht_Restart = ht.ht_PositionNr - 1;
        }

        if (ht.ht_PositionNr > 1000 || ht.ht_TrackLength > 64 || ht.ht_InstrumentNr > 64) {
            System.out.printf("%d,%d,%d\n", ht.ht_PositionNr, ht.ht_TrackLength, ht.ht_InstrumentNr);
            System.out.println("Invalid file.");
            return null;
        }

        int nameOffset = (((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF));
        ht.ht_Name = readString(buf, nameOffset, 80);
        int nptr_idx = nameOffset + ht.ht_Name.length() + 1;

        int bptr_idx = 16;

        // Subsongs
        ht.ht_Subsongs = new int[ssn];
        for (i = 0; i < ht.ht_SubsongNr; i++) {
            ht.ht_Subsongs[i] = (((buf[bptr_idx] & 0xFF) << 8) | (buf[bptr_idx + 1] & 0xFF));
            bptr_idx += 2;
        }

        // Position list
        ht.ht_Positions = new Position[posn];
        for (i = 0; i < posn; i++) {
            ht.ht_Positions[i] = new Position();
        }
        for (i = 0; i < ht.ht_PositionNr; i++) {
            for (j = 0; j < ht.ht_Channels; j++) {
                ht.ht_Positions[i].pos_Track[j] = (buf[bptr_idx++] & 0xFF);
                ht.ht_Positions[i].pos_Transpose[j] = buf[bptr_idx++];
            }
        }

        // Tracks
        for (i = 0; i <= ht.ht_TrackNr; i++) {
            if (((buf[6] & 0x80) == 0x80) && (i == 0)) {
                for (j = 0; j < ht.ht_TrackLength; j++) {
                    ht.ht_Tracks[i][j].stp_Note = 0;
                    ht.ht_Tracks[i][j].stp_Instrument = 0;
                    ht.ht_Tracks[i][j].stp_FX = 0;
                    ht.ht_Tracks[i][j].stp_FXParam = 0;
                    ht.ht_Tracks[i][j].stp_FXb = 0;
                    ht.ht_Tracks[i][j].stp_FXbParam = 0;
                }
                continue;
            }

            for (j = 0; j < ht.ht_TrackLength; j++) {
                if ((buf[bptr_idx] & 0xFF) == 0x3f) {
                    ht.ht_Tracks[i][j].stp_Note = 0;
                    ht.ht_Tracks[i][j].stp_Instrument = 0;
                    ht.ht_Tracks[i][j].stp_FX = 0;
                    ht.ht_Tracks[i][j].stp_FXParam = 0;
                    ht.ht_Tracks[i][j].stp_FXb = 0;
                    ht.ht_Tracks[i][j].stp_FXbParam = 0;
                    bptr_idx++;
                    continue;
                }

                int b0 = buf[bptr_idx] & 0xFF;
                int b1 = buf[bptr_idx + 1] & 0xFF;
                int b2 = buf[bptr_idx + 2] & 0xFF;
                int b3 = buf[bptr_idx + 3] & 0xFF;
                int b4 = buf[bptr_idx + 4] & 0xFF;

                ht.ht_Tracks[i][j].stp_Note = b0;
                ht.ht_Tracks[i][j].stp_Instrument = b1;
                ht.ht_Tracks[i][j].stp_FX = b2 >> 4;
                ht.ht_Tracks[i][j].stp_FXParam = b3;
                ht.ht_Tracks[i][j].stp_FXb = b2 & 0xf;
                ht.ht_Tracks[i][j].stp_FXbParam = b4;
                bptr_idx += 5;
            }
        }

        // Instruments
        ht.ht_Instruments = new Instrument[insn + 1];
        for (int ins = 0; ins <= insn; ins++) {
            ht.ht_Instruments[ins] = new Instrument();
        }
        for (i = 1; i <= ht.ht_InstrumentNr; i++) {
            if (nptr_idx < buf.length) {
                ht.ht_Instruments[i].ins_Name = readString(buf, nptr_idx, 32);
                nptr_idx += ht.ht_Instruments[i].ins_Name.length() + 1;
            } else {
                ht.ht_Instruments[i].ins_Name = "";
            }
            ht.ht_Instruments[i].ins_Volume = buf[bptr_idx] & 0xFF;
            ht.ht_Instruments[i].ins_FilterSpeed = (((buf[bptr_idx + 1] & 0xFF) >> 3) & 0x1f) | (((buf[bptr_idx + 12] & 0xFF) >> 2) & 0x20);
            ht.ht_Instruments[i].ins_WaveLength = buf[bptr_idx + 1] & 0x07;

            ht.ht_Instruments[i].ins_Envelope.aFrames = (short) (buf[bptr_idx + 2] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.aVolume = (short) (buf[bptr_idx + 3] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.dFrames = (short) (buf[bptr_idx + 4] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.dVolume = (short) (buf[bptr_idx + 5] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.sFrames = (short) (buf[bptr_idx + 6] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.rFrames = (short) (buf[bptr_idx + 7] & 0xFF);
            ht.ht_Instruments[i].ins_Envelope.rVolume = (short) (buf[bptr_idx + 8] & 0xFF);

            ht.ht_Instruments[i].ins_FilterLowerLimit = buf[bptr_idx + 12] & 0x7f;
            ht.ht_Instruments[i].ins_VibratoDelay = buf[bptr_idx + 13] & 0xFF;
            ht.ht_Instruments[i].ins_HardCutReleaseFrames = ((buf[bptr_idx + 14] & 0xFF) >> 4) & 0x07;
            ht.ht_Instruments[i].ins_HardCutRelease = (buf[bptr_idx + 14] & 0x80) != 0 ? 1 : 0;
            ht.ht_Instruments[i].ins_VibratoDepth = buf[bptr_idx + 14] & 0x0f;
            ht.ht_Instruments[i].ins_VibratoSpeed = buf[bptr_idx + 15] & 0xFF;
            ht.ht_Instruments[i].ins_SquareLowerLimit = buf[bptr_idx + 16] & 0xFF;
            ht.ht_Instruments[i].ins_SquareUpperLimit = buf[bptr_idx + 17] & 0xFF;
            ht.ht_Instruments[i].ins_SquareSpeed = buf[bptr_idx + 18] & 0xFF;
            ht.ht_Instruments[i].ins_FilterUpperLimit = buf[bptr_idx + 19] & 0x3f;
            ht.ht_Instruments[i].ins_PList.pls_Speed = (short) (buf[bptr_idx + 20] & 0xFF);
            ht.ht_Instruments[i].ins_PList.pls_Length = (short) (buf[bptr_idx + 21] & 0xFF);

            int plsLength = ht.ht_Instruments[i].ins_PList.pls_Length;
            ht.ht_Instruments[i].ins_PList.pls_Entries = new PlsEntry[plsLength];
            for (int entryIdx = 0; entryIdx < plsLength; entryIdx++) {
                ht.ht_Instruments[i].ins_PList.pls_Entries[entryIdx] = new PlsEntry();
            }

            bptr_idx += 22;
            for (j = 0; j < plsLength; j++) {
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FX[0] = (byte) (buf[bptr_idx] & 0xf);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FX[1] = (byte) ((buf[bptr_idx + 1] >> 3) & 0xf);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_Waveform = (byte) (buf[bptr_idx + 1] & 7);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_Fixed = (short) ((buf[bptr_idx + 2] >> 6) & 1);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_Note = (byte) (buf[bptr_idx + 2] & 0x3f);
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FXParam[0] = buf[bptr_idx + 3];
                ht.ht_Instruments[i].ins_PList.pls_Entries[j].ple_FXParam[1] = buf[bptr_idx + 4];
                bptr_idx += 5;
            }
        }

        ht.hvl_InitSubsong(0);
        return ht;
    }

    public static Tune hvl_LoadTune(String name, int freq, int defstereo, int size) {
        try (FileInputStream fis = new FileInputStream(name)) {
            byte[] buf = new byte[size];
            int read = fis.read(buf);
            if (read != size) {
                System.out.println("Unable to read from file!");
                return null;
            }
            return hvl_reset(buf, size, defstereo, freq, true);
        } catch (IOException e) {
            System.out.println("Cannot open file");
            return null;
        }
    }
}
