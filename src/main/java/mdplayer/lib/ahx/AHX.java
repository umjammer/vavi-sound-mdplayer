/*
 * Copyright:
 * --------
 * WinAHX and the AHX Replayer, although copyrighted
 * by Abyss, are Freeware! You may spread them as you
 * like - as long as you don't charge any money for it!
 */

package mdplayer.lib.ahx;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


public class AHX {

    public static class ByteSlice {
        public final byte[] array;
        public final int offset;
        public final int length;

        public ByteSlice(byte[] array) {
            this(array, 0, array.length);
        }

        public ByteSlice(byte[] array, int offset, int length) {
            this.array = array;
            this.offset = offset;
            this.length = length;
        }

        public byte get(int index) {
            return array[offset + index];
        }

        public void set(int index, byte val) {
            array[offset + index] = val;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ByteSlice other)) return false;
            return this.array == other.array && this.offset == other.offset && this.length == other.length;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(array) ^ offset ^ length;
        }
    }

    public static class AHXPlayer {
        public static class AHXPListEntry {
            public int note;
            public int fixed;
            public int waveform;
            public int[] fx = new int[2];
            public int[] fxParam = new int[2];
        }

        public static class AHXPList {
            public int speed;
            public int length;
            public AHXPListEntry[] entries;
        }

        public static class AHXEnvelope {
            public int aFrames;
            public int aVolume;
            public int dFrames;
            public int dVolume;
            public int sFrames;
            public int rFrames;
            public int rVolume;
        }

        public static class AHXInstrument {
            public String name;
            public int volume; // 0..64
            public int waveLength; // 0..5 (shifts)
            public AHXEnvelope envelope = new AHXEnvelope();
            public int filterLowerLimit;
            public int filterUpperLimit;
            public int filterSpeed;
            public int squareLowerLimit;
            public int squareUpperLimit;
            public int squareSpeed;
            public int vibratoDelay;
            public int vibratoDepth;
            public int vibratoSpeed;
            public int hardCutRelease;
            public int hardCutReleaseFrames;
            public AHXPList pList = new AHXPList();
        }

        public static class AHXPosition {
            public int[] track = new int[4];
            public int[] transpose = new int[4];
        }

        public static class AHXStep {
            public int note;
            public int instrument;
            public int fx;
            public int fxParam;
        }

        public static class AHXSong {
            public String name;
            public int restart;
            public int positionNr;
            public int trackLength;
            public int trackNr;
            public int instrumentNr;
            public int subsongNr;
            public int revision;
            public int speedMultiplier;
            public AHXPosition[] positions;
            public AHXStep[][] tracks;
            public AHXInstrument[] instruments;
            public int[] subsongs;

            public AHXSong() {
                restart = positionNr = trackLength = trackNr = instrumentNr = subsongNr = 0;
                name = null;
                positions = null;
                tracks = null;
                instruments = null;
                subsongs = null;
            }
        }

        public int playingTime;
        public AHXSong song;

        public AHXWaves waves;
        private int ourWaves;
        public AHXVoice[] voices = new AHXVoice[4];

        public int stepWaitFrames;
        public int getNewPosition;
        public int songEndReached;

        /**
         * The speed past which a song counts as parked rather than playing. Real songs run at 3 to
         * 8 frames a row, i.e. a row every 60 to 160 ms at the AHX 50 Hz; at this one a row lasts
         * more than a second.
         */
        private static final int stallTempo = 0x40;
        public int timingValue;
        public int patternBreak;
        public int mainVolume;
        public int playing;
        public int tempo;
        public int posNr;
        public int posJump;
        public int noteNr;
        public int posJumpNote;
        public ByteSlice[] waveformTab = new ByteSlice[4];
        public int wnRandom;

        private static final int[] VIBRATO_TABLE = {
            0, 24, 49, 74, 97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253, 255,
            253, 250, 244, 235, 224, 212, 197, 180, 161, 141, 120, 97, 74, 49, 24,
            0, -24, -49, -74, -97, -120, -141, -161, -180, -197, -212, -224, -235, -244, -250, -253, -255,
            -253, -250, -244, -235, -224, -212, -197, -180, -161, -141, -120, -97, -74, -49, -24
        };

        /** note index to Amiga period; public so a view can turn a voice's period back into a note */
        public static final int[] PERIOD_TABLE = {
            0x0000, 0x0D60, 0x0CA0, 0x0BE8, 0x0B40, 0x0A98, 0x0A00, 0x0970,
            0x08E8, 0x0868, 0x07F0, 0x0780, 0x0714, 0x06B0, 0x0650, 0x05F4,
            0x05A0, 0x054C, 0x0500, 0x04B8, 0x0474, 0x0434, 0x03F8, 0x03C0,
            0x038A, 0x0358, 0x0328, 0x02FA, 0x02D0, 0x02A6, 0x0280, 0x025C,
            0x023A, 0x021A, 0x01FC, 0x01E0, 0x01C5, 0x01AC, 0x0194, 0x017D,
            0x0168, 0x0153, 0x0140, 0x012E, 0x011D, 0x010D, 0x00FE, 0x00F0,
            0x00E2, 0x00D6, 0x00CA, 0x00BE, 0x00B4, 0x00AA, 0x00A0, 0x0097,
            0x008F, 0x0087, 0x007F, 0x0078, 0x0071
        };

        public void init() {
            init(null);
        }

        public void init(AHXWaves waves) {
            if (waves != null) {
                ourWaves = 0;
                this.waves = waves;
            } else {
                ourWaves = 1;
                this.waves = new AHXWaves();
            }

            waveformTab[0] = this.waves.Triangle04;
            waveformTab[1] = this.waves.Sawtooth04;
            waveformTab[3] = this.waves.WhiteNoiseBig;
        }

        public void dispose() {
        }

        public void loadSong(String filename) throws IOException {
            byte[] songBuffer = Files.readAllBytes(java.nio.file.Paths.get(filename));
            loadSong(songBuffer, songBuffer.length);
        }

        private static String strcpy(byte[] sb, int ptr) {
            StringBuilder s = new StringBuilder();
            while ((sb[ptr] & 0xff) != 0) {
                s.append((char) (sb[ptr++] & 0xff));
            }
            return s.toString();
        }

        private static int strlen(byte[] sb, int ptr) {
            int sptr = ptr;
            while ((sb[ptr] & 0xff) != 0) {
                ptr++;
            }
            return ptr - sptr;
        }

        public void loadSong(byte[] songBuffer, int len) {
            int sbPtr = 14;
            int songLength = len;
            if (songLength < 14 || songLength == 65536) throw new IllegalArgumentException("len");

            if (songBuffer[0] != 'T' || songBuffer[1] != 'H' || songBuffer[2] != 'X') throw new IllegalArgumentException("magic");
            song = new AHXSong();
            song.revision = songBuffer[3] & 0xff;
            if (song.revision > 1) throw new IllegalArgumentException("revision");

            // Header ----
            // Songname
            int namePtr = ((songBuffer[4] & 0xff) << 8) | (songBuffer[5] & 0xff);
            song.name = strcpy(songBuffer, namePtr);
            namePtr += strlen(songBuffer, namePtr) + 1;
            song.speedMultiplier = (((songBuffer[6] & 0xff) >> 5) & 3) + 1;

            song.positionNr = (((songBuffer[6] & 0xff) & 0xf) << 8) | (songBuffer[7] & 0xff);
            song.restart = ((songBuffer[8] & 0xff) << 8) | (songBuffer[9] & 0xff);
            song.trackLength = songBuffer[10] & 0xff;
            song.trackNr = songBuffer[11] & 0xff;
            song.instrumentNr = songBuffer[12] & 0xff;
            song.subsongNr = songBuffer[13] & 0xff;

            // Subsongs ----
            song.subsongs = new int[song.subsongNr];
            for (int i = 0; i < song.subsongNr; i++) {
                if (sbPtr >= songLength) throw new IllegalArgumentException("subsongs");
                song.subsongs[i] = ((songBuffer[sbPtr] & 0xff) << 8) | (songBuffer[sbPtr + 1] & 0xff);
                sbPtr += 2;
            }

            // Position List ----
            song.positions = new AHXPosition[song.positionNr];
            for (int i = 0; i < song.positionNr; i++) {
                song.positions[i] = new AHXPosition();
                for (int j = 0; j < 4; j++) {
                    if (sbPtr > songLength) throw new IllegalArgumentException("positions");
                    song.positions[i].track[j] = songBuffer[sbPtr++] & 0xff;
                    song.positions[i].transpose[j] = songBuffer[sbPtr++];
                }
            }

            // Tracks ----
            int maxTrack = song.trackNr;
            song.tracks = new AHXStep[maxTrack + 1][];

            for (int i = 0; i < maxTrack + 1; i++) {
                song.tracks[i] = new AHXStep[song.trackLength];
                if (((songBuffer[6] & 0xff) & 0x80) == 0x80 && i == 0) {
                    for (int j = 0; j < song.trackLength; j++) {
                        song.tracks[i][j] = new AHXStep();
                    }
                    continue;
                }

                for (int j = 0; j < song.trackLength; j++) {
                    song.tracks[i][j] = new AHXStep();
                    if (sbPtr >= songLength) throw new IllegalArgumentException("trackLength");
                    song.tracks[i][j].note = ((songBuffer[sbPtr] & 0xff) >> 2) & 0x3f;
                    song.tracks[i][j].instrument = (((songBuffer[sbPtr] & 0xff) & 0x3) << 4) | ((songBuffer[sbPtr + 1] & 0xff) >> 4);
                    song.tracks[i][j].fx = (songBuffer[sbPtr + 1] & 0xff) & 0xf;
                    song.tracks[i][j].fxParam = songBuffer[sbPtr + 2] & 0xff;
                    sbPtr += 3;
                }
            }

            // Instruments ----
            song.instruments = new AHXInstrument[song.instrumentNr + 1];
            for (int i = 1; i < song.instrumentNr + 1; i++) {
                song.instruments[i] = new AHXInstrument();
                song.instruments[i].name = strcpy(songBuffer, namePtr);
                namePtr += strlen(songBuffer, namePtr) + 1;

                if (sbPtr >= songLength) throw new IllegalArgumentException("instruments");
                song.instruments[i].volume = songBuffer[sbPtr + 0] & 0xff;
                song.instruments[i].filterSpeed = (((songBuffer[sbPtr + 1] & 0xff) >> 3) & 0x1f) | (((songBuffer[sbPtr + 12] & 0xff) >> 2) & 0x20);
                song.instruments[i].waveLength = (songBuffer[sbPtr + 1] & 0xff) & 0x7;
                song.instruments[i].envelope.aFrames = songBuffer[sbPtr + 2] & 0xff;
                song.instruments[i].envelope.aVolume = songBuffer[sbPtr + 3] & 0xff;
                song.instruments[i].envelope.dFrames = songBuffer[sbPtr + 4] & 0xff;
                song.instruments[i].envelope.dVolume = songBuffer[sbPtr + 5] & 0xff;
                song.instruments[i].envelope.sFrames = songBuffer[sbPtr + 6] & 0xff;
                song.instruments[i].envelope.rFrames = songBuffer[sbPtr + 7] & 0xff;
                song.instruments[i].envelope.rVolume = songBuffer[sbPtr + 8] & 0xff;
                song.instruments[i].filterLowerLimit = (songBuffer[sbPtr + 12] & 0xff) & 0x7f;
                song.instruments[i].vibratoDelay = songBuffer[sbPtr + 13] & 0xff;
                song.instruments[i].hardCutReleaseFrames = ((songBuffer[sbPtr + 14] & 0xff) >> 4) & 7;
                song.instruments[i].hardCutRelease = ((songBuffer[sbPtr + 14] & 0xff) & 0x80) != 0 ? 1 : 0;
                song.instruments[i].vibratoDepth = (songBuffer[sbPtr + 14] & 0xff) & 0xf;
                song.instruments[i].vibratoSpeed = songBuffer[sbPtr + 15] & 0xff;
                song.instruments[i].squareLowerLimit = songBuffer[sbPtr + 16] & 0xff;
                song.instruments[i].squareUpperLimit = songBuffer[sbPtr + 17] & 0xff;
                song.instruments[i].squareSpeed = songBuffer[sbPtr + 18] & 0xff;
                song.instruments[i].filterUpperLimit = (songBuffer[sbPtr + 19] & 0xff) & 0x3f;
                song.instruments[i].pList.speed = songBuffer[sbPtr + 20] & 0xff;
                song.instruments[i].pList.length = songBuffer[sbPtr + 21] & 0xff;
                sbPtr += 22;

                song.instruments[i].pList.entries = new AHXPListEntry[song.instruments[i].pList.length];
                for (int j = 0; j < song.instruments[i].pList.length; j++) {
                    if (sbPtr >= songLength) throw new IllegalArgumentException("instruments");
                    song.instruments[i].pList.entries[j] = new AHXPListEntry();
                    song.instruments[i].pList.entries[j].fx[1] = ((songBuffer[sbPtr + 0] & 0xff) >> 5) & 7;
                    song.instruments[i].pList.entries[j].fx[0] = ((songBuffer[sbPtr + 0] & 0xff) >> 2) & 7;
                    song.instruments[i].pList.entries[j].waveform = (((songBuffer[sbPtr + 0] & 0xff) << 1) & 6) | ((songBuffer[sbPtr + 1] & 0xff) >> 7);
                    song.instruments[i].pList.entries[j].fixed = ((songBuffer[sbPtr + 1] & 0xff) >> 6) & 1;
                    song.instruments[i].pList.entries[j].note = (songBuffer[sbPtr + 1] & 0xff) & 0x3f;
                    song.instruments[i].pList.entries[j].fxParam[0] = songBuffer[sbPtr + 2] & 0xff;
                    song.instruments[i].pList.entries[j].fxParam[1] = songBuffer[sbPtr + 3] & 0xff;
                    sbPtr += 4;
                }
            }
        }

        public void initSubsong(int nr) {
            if (nr > song.subsongNr) throw new IllegalArgumentException("subsong");

            if (nr == 0) posNr = 0;
            else posNr = song.subsongs[nr - 1];

            posJump = 0;
            patternBreak = 0;
            mainVolume = 0x40;
            playing = 1;
            noteNr = posJumpNote = 0;
            tempo = 6;
            stepWaitFrames = 0;
            getNewPosition = 1;
            songEndReached = 0;
            timingValue = playingTime = 0;

            for (int v = 0; v < 4; v++) {
                voices[v] = new AHXVoice();
                voices[v].init();
            }
        }

        public void playIRQ() {
            if (stepWaitFrames <= 0) {
                if (getNewPosition != 0) {
                    int nextPos = (posNr + 1 == song.positionNr) ? 0 : (posNr + 1);
                    for (int i = 0; i < 4; i++) {
                        voices[i].track = song.positions[posNr].track[i];
                        voices[i].transpose = song.positions[posNr].transpose[i];
                        voices[i].nextTrack = song.positions[nextPos].track[i];
                        voices[i].nextTranspose = song.positions[nextPos].transpose[i];
                    }

                    getNewPosition = 0;
                }

                for (int i = 0; i < 4; i++) processStep(i);
                stepWaitFrames = tempo;
            }

            // DoFrameStuff
            for (int i = 0; i < 4; i++) processFrame(i);
            playingTime++;
            if (tempo > 0 && --stepWaitFrames <= 0) {
                if (patternBreak == 0) {
                    noteNr++;
                    if (noteNr >= song.trackLength) {
                        posJump = posNr + 1;
                        posJumpNote = 0;
                        patternBreak = 1;
                    }
                }

                if (patternBreak != 0) {
                    patternBreak = 0;
                    noteNr = posJumpNote;
                    posJumpNote = 0;
                    posNr = posJump;
                    posJump = 0;
                    if (posNr == song.positionNr) {
                        songEndReached = 1;
                        posNr = song.restart;
                    }

                    getNewPosition = 1;
                }
            }

            // RemainPosition
            for (int a = 0; a < 4; a++) setAudio(a);
        }

        public void nextPosition() {
            posNr++;
            if (posNr == song.positionNr) posNr = 0;
            stepWaitFrames = 0;
            getNewPosition = 1;
        }

        public void prevPosition() {
            posNr--;
            if (posNr < 0) posNr = 0;
            stepWaitFrames = 0;
            getNewPosition = 1;
        }

        public void processStep(int v) {
            if (voices[v].trackOn == 0) return;
            voices[v].volumeSlideUp = voices[v].volumeSlideDown = 0;

            int note = song.tracks[song.positions[posNr].track[v]][noteNr].note;
            int instrument = song.tracks[song.positions[posNr].track[v]][noteNr].instrument;
            int fx = song.tracks[song.positions[posNr].track[v]][noteNr].fx;
            int fxParam = song.tracks[song.positions[posNr].track[v]][noteNr].fxParam;

            switch (fx) {
                case 0x0: // Position Jump HI
                    if ((fxParam & 0xf) > 0 && (fxParam & 0xf) <= 9) {
                        posJump = fxParam & 0xf;
                    }
                    break;
                case 0x5: // Volume Slide + Tone Portamento
                case 0xa: // Volume Slide
                    voices[v].volumeSlideDown = fxParam & 0x0f;
                    voices[v].volumeSlideUp = fxParam >> 4;
                    break;
                case 0xb: // Position Jump
                    posJump = posJump * 100 + (fxParam & 0x0f) + (fxParam >> 4) * 10;
                    patternBreak = 1;
                    // a jump that does not go forward is the song looping back on itself, which is
                    // the only end a song written this way has: it never walks off the last
                    // position, so the check in play() would wait for a wrap that never comes
                    if (posJump <= posNr) songEndReached = 1;
                    break;
                case 0xd: // Patternbreak
                    posJump = posNr + 1;
                    posJumpNote = (fxParam & 0x0f) + (fxParam >> 4) * 10;
                    if (posJumpNote > song.trackLength) posJumpNote = 0;
                    patternBreak = 1;
                    break;
                case 0xe: // Enhanced commands
                    switch (fxParam >> 4) {
                        case 0xc: // Note Cut
                            if ((fxParam & 0x0f) < tempo) {
                                voices[v].noteCutWait = fxParam & 0x0f;
                                if (voices[v].noteCutWait != 0) {
                                    voices[v].noteCutOn = 1;
                                    voices[v].hardCutRelease = 0;
                                }
                            }
                            break;
                        case 0xd: // Note Delay
                            if (voices[v].noteDelayOn != 0) {
                                voices[v].noteDelayOn = 0;
                            } else {
                                if ((fxParam & 0x0f) < tempo) {
                                    voices[v].noteDelayWait = fxParam & 0x0f;
                                    if (voices[v].noteDelayWait != 0) {
                                        voices[v].noteDelayOn = 1;
                                        return;
                                    }
                                }
                            }
                            break;
                    }
                    break;
                case 0xf: // Speed
                    tempo = fxParam;
                    // Speed zero stops the song outright, and a speed this far past anything
                    // musical - a row would last seconds - is how a song that does not loop parks
                    // itself at the end instead. Either way it is over: without this the player
                    // waits for a position wrap that never comes and the track never ends.
                    if (fxParam == 0 || fxParam >= stallTempo) songEndReached = 1;
                    break;
            }

            if (instrument != 0) {
                voices[v].perfSubVolume = 0x40;
                voices[v].periodSlideSpeed = voices[v].periodSlidePeriod = voices[v].periodSlideLimit = 0;
                voices[v].adsrVolume = 0;
                voices[v].instrument = song.instruments[instrument];
                voices[v].calcADSR();
                // InitOnInstrument
                voices[v].waveLength = voices[v].instrument.waveLength;
                voices[v].noteMaxVolume = voices[v].instrument.volume;
                // InitVibrato
                voices[v].vibratoCurrent = 0;
                voices[v].vibratoDelay = voices[v].instrument.vibratoDelay;
                voices[v].vibratoDepth = voices[v].instrument.vibratoDepth;
                voices[v].vibratoSpeed = voices[v].instrument.vibratoSpeed;
                voices[v].vibratoPeriod = 0;
                // InitHardCut
                voices[v].hardCutRelease = voices[v].instrument.hardCutRelease;
                voices[v].hardCut = voices[v].instrument.hardCutReleaseFrames;
                // InitSquare
                voices[v].ignoreSquare = voices[v].squareSlidingIn = 0;
                voices[v].squareWait = voices[v].squareOn = 0;
                int squareLower = voices[v].instrument.squareLowerLimit >> (5 - voices[v].waveLength);
                int squareUpper = voices[v].instrument.squareUpperLimit >> (5 - voices[v].waveLength);
                if (squareUpper < squareLower) {
                    int t = squareUpper;
                    squareUpper = squareLower;
                    squareLower = t;
                }

                voices[v].squareUpperLimit = squareUpper;
                voices[v].squareLowerLimit = squareLower;
                // InitFilter
                voices[v].ignoreFilter = voices[v].filterWait = voices[v].filterOn = 0;
                voices[v].filterSlidingIn = 0;
                int d6 = voices[v].instrument.filterSpeed;
                int d3 = voices[v].instrument.filterLowerLimit;
                int d4 = voices[v].instrument.filterUpperLimit;
                if ((d3 & 0x80) != 0) d6 |= 0x20;
                if ((d4 & 0x80) != 0) d6 |= 0x40;
                voices[v].filterSpeed = d6;
                d3 &= ~0x80;
                d4 &= ~0x80;
                if (d3 > d4) {
                    int t = d3;
                    d3 = d4;
                    d4 = t;
                }

                voices[v].filterUpperLimit = d4;
                voices[v].filterLowerLimit = d3;
                voices[v].filterPos = 32;
                // Init PerfList
                voices[v].perfWait = voices[v].perfCurrent = 0;
                voices[v].perfSpeed = voices[v].instrument.pList.speed;
                voices[v].perfList = voices[v].instrument.pList;
            }

            // NoInstrument
            voices[v].periodSlideOn = 0;

            boolean gotoNoNote = false;
            switch (fx) {
                case 0x4: // Override filter
                    voices[v].ignoreFilter = fxParam;
                    break;
                case 0x9: // Set Squarewave-Offset
                    voices[v].squarePos = fxParam >> (5 - voices[v].waveLength);
                    voices[v].plantSquare = 1;
                    voices[v].ignoreSquare = 1;
                    break;
                case 0x5: // Tone Portamento + Volume Slide
                case 0x3: // Tone Portamento (Period Slide Up/Down w/ Limit)
                    if (fxParam != 0) voices[v].periodSlideSpeed = fxParam;
                    if (note != 0) {
                        int Neue = PERIOD_TABLE[note];
                        int Alte = PERIOD_TABLE[voices[v].trackPeriod];
                        Alte -= Neue;
                        Neue = Alte + voices[v].periodSlidePeriod;
                        if (Neue != 0) voices[v].periodSlideLimit = -Alte;
                    }

                    voices[v].periodSlideOn = 1;
                    voices[v].periodSlideWithLimit = 1;
                    gotoNoNote = true;
                    break;
            }

            // Note anschlagen
            if (!gotoNoNote && note != 0) {
                voices[v].trackPeriod = note;
                voices[v].plantPeriod = 1;
            }

            // NoNote:
            switch (fx) {
                case 0x1: // Portamento up (Period slide down)
                    voices[v].periodSlideSpeed = -fxParam;
                    voices[v].periodSlideOn = 1;
                    voices[v].periodSlideWithLimit = 0;
                    break;
                case 0x2: // Portamento down (Period slide up)
                    voices[v].periodSlideSpeed = fxParam;
                    voices[v].periodSlideOn = 1;
                    voices[v].periodSlideWithLimit = 0;
                    break;
                case 0xc: // Volume
                    if (fxParam <= 0x40) {
                        voices[v].noteMaxVolume = fxParam;
                    } else {
                        fxParam -= 0x50;
                        if (fxParam <= 0x40) {
                            for (int i = 0; i < 4; i++) {
                                voices[i].trackMasterVolume = fxParam;
                            }
                        } else {
                            fxParam -= 0xa0 - 0x50;
                            if (fxParam <= 0x40) {
                                voices[v].trackMasterVolume = fxParam;
                            }
                        }
                    }
                    break;
                case 0xe: // Enhanced commands
                    switch (fxParam >> 4) {
                        case 0x1: // Fineslide up (Period fineslide down)
                            voices[v].periodSlidePeriod = -(fxParam & 0x0f);
                            voices[v].plantPeriod = 1;
                            break;
                        case 0x2: // Fineslide down (Period fineslide up)
                            voices[v].periodSlidePeriod = fxParam & 0x0f;
                            voices[v].plantPeriod = 1;
                            break;
                        case 0x4: // Vibrato control
                            voices[v].vibratoDepth = fxParam & 0x0f;
                            break;
                        case 0xa: // Finevolume up
                            voices[v].noteMaxVolume += fxParam & 0x0f;
                            if (voices[v].noteMaxVolume > 0x40) voices[v].noteMaxVolume = 0x40;
                            break;
                        case 0xb: // Finevolume down
                            voices[v].noteMaxVolume -= fxParam & 0x0f;
                            if (voices[v].noteMaxVolume < 0) voices[v].noteMaxVolume = 0;
                            break;
                    }
                    break;
            }
        }

        public void processFrame(int v) {
            if (voices[v].trackOn == 0) return;

            if (voices[v].noteDelayOn != 0) {
                if (voices[v].noteDelayWait <= 0) processStep(v);
                else voices[v].noteDelayWait--;
            }

            if (voices[v].hardCut != 0) {
                int nextInstrument;
                if (noteNr + 1 < song.trackLength) {
                    nextInstrument = song.tracks[voices[v].track][noteNr + 1].instrument;
                } else {
                    nextInstrument = song.tracks[voices[v].nextTrack][0].instrument;
                }
                if (nextInstrument != 0) {
                    int d1 = tempo - voices[v].hardCut;
                    if (d1 < 0) d1 = 0;
                    if (voices[v].noteCutOn == 0) {
                        voices[v].noteCutOn = 1;
                        voices[v].noteCutWait = d1;
                        voices[v].hardCutReleaseF = -(d1 - tempo);
                    } else {
                        voices[v].hardCut = 0;
                    }
                }
            }

            if (voices[v].noteCutOn != 0) {
                if (voices[v].noteCutWait <= 0) {
                    voices[v].noteCutOn = 0;
                    if (voices[v].hardCutRelease != 0) {
                        voices[v].adsr.rVolume = -(voices[v].adsrVolume - (voices[v].instrument.envelope.rVolume << 8)) / voices[v].hardCutReleaseF;
                        voices[v].adsr.rFrames = voices[v].hardCutReleaseF;
                        voices[v].adsr.aFrames = voices[v].adsr.dFrames = voices[v].adsr.sFrames = 0;
                    } else {
                        voices[v].noteMaxVolume = 0;
                    }
                } else {
                    voices[v].noteCutWait--;
                }
            }

            // adsrEnvelope
            if (voices[v].adsr.aFrames != 0) {
                voices[v].adsrVolume += voices[v].adsr.aVolume; // Delta
                if (--voices[v].adsr.aFrames <= 0) {
                    voices[v].adsrVolume = voices[v].instrument.envelope.aVolume << 8;
                }
            } else if (voices[v].adsr.dFrames != 0) {
                voices[v].adsrVolume += voices[v].adsr.dVolume; // Delta
                if (--voices[v].adsr.dFrames <= 0) {
                    voices[v].adsrVolume = voices[v].instrument.envelope.dVolume << 8;
                }
            } else if (voices[v].adsr.sFrames != 0) {
                voices[v].adsr.sFrames--;
            } else if (voices[v].adsr.rFrames != 0) {
                voices[v].adsrVolume += voices[v].adsr.rVolume; // Delta
                if (--voices[v].adsr.rFrames <= 0) {
                    voices[v].adsrVolume = voices[v].instrument.envelope.rVolume << 8;
                }
            }

            // VolumeSlide
            voices[v].noteMaxVolume = voices[v].noteMaxVolume + voices[v].volumeSlideUp - voices[v].volumeSlideDown;
            if (voices[v].noteMaxVolume < 0) voices[v].noteMaxVolume = 0;
            if (voices[v].noteMaxVolume > 0x40) voices[v].noteMaxVolume = 0x40;

            // Portamento
            if (voices[v].periodSlideOn != 0) {
                if (voices[v].periodSlideWithLimit != 0) {
                    int d0 = voices[v].periodSlidePeriod - voices[v].periodSlideLimit;
                    int d2 = voices[v].periodSlideSpeed;
                    if (d0 > 0) d2 = -d2;
                    if (d0 != 0) {
                        int d3 = (d0 + d2) ^ d0;
                        if (d3 >= 0) d0 = voices[v].periodSlidePeriod + d2;
                        else d0 = voices[v].periodSlideLimit;
                        voices[v].periodSlidePeriod = d0;
                        voices[v].plantPeriod = 1;
                    }
                } else {
                    voices[v].periodSlidePeriod += voices[v].periodSlideSpeed;
                    voices[v].plantPeriod = 1;
                }
            }

            // Vibrato
            if (voices[v].vibratoDepth != 0) {
                if (voices[v].vibratoDelay <= 0) {
                    voices[v].vibratoPeriod = (VIBRATO_TABLE[voices[v].vibratoCurrent] * voices[v].vibratoDepth) >> 7;
                    voices[v].plantPeriod = 1;
                    voices[v].vibratoCurrent = (voices[v].vibratoCurrent + voices[v].vibratoSpeed) & 0x3f;
                } else {
                    voices[v].vibratoDelay--;
                }
            }

            // PList
            if (voices[v].instrument != null && voices[v].perfCurrent < voices[v].instrument.pList.length) {
                if (--voices[v].perfWait <= 0) {
                    int cur = voices[v].perfCurrent++;
                    voices[v].perfWait = voices[v].perfSpeed;
                    if (voices[v].perfList.entries[cur].waveform != 0) {
                        voices[v].waveform = voices[v].perfList.entries[cur].waveform - 1;
                        voices[v].newWaveform = 1;
                        voices[v].periodPerfSlideSpeed = voices[v].periodPerfSlidePeriod = 0;
                    }

                    // Holdwave
                    voices[v].periodPerfSlideOn = 0;
                    for (int i = 0; i < 2; i++) {
                        pListCommandParse(v, voices[v].perfList.entries[cur].fx[i], voices[v].perfList.entries[cur].fxParam[i]);
                    }
                    // GetNote
                    if (voices[v].perfList.entries[cur].note != 0) {
                        voices[v].instrPeriod = voices[v].perfList.entries[cur].note;
                        voices[v].plantPeriod = 1;
                        voices[v].fixedNote = voices[v].perfList.entries[cur].fixed;
                    }
                }
            } else {
                if (voices[v].perfWait != 0) voices[v].perfWait--;
                else voices[v].periodPerfSlideSpeed = 0;
            }

            // PerfPortamento
            if (voices[v].periodPerfSlideOn != 0) {
                voices[v].periodPerfSlidePeriod -= voices[v].periodPerfSlideSpeed;
                if (voices[v].periodPerfSlidePeriod != 0) voices[v].plantPeriod = 1;
            }

            if (voices[v].waveform == 3 - 1 && voices[v].squareOn != 0) {
                if (--voices[v].squareWait <= 0) {
                    int d1 = voices[v].squareLowerLimit;
                    int d2 = voices[v].squareUpperLimit;
                    int d3 = voices[v].squarePos;
                    if (voices[v].squareInit != 0) {
                        voices[v].squareInit = 0;
                        if (d3 <= d1) {
                            voices[v].squareSlidingIn = 1;
                            voices[v].squareSign = 1;
                        } else if (d3 >= d2) {
                            voices[v].squareSlidingIn = 1;
                            voices[v].squareSign = -1;
                        }
                    }

                    // NoSquareInit
                    if (d1 == d3 || d2 == d3) {
                        if (voices[v].squareSlidingIn != 0) {
                            voices[v].squareSlidingIn = 0;
                        } else {
                            voices[v].squareSign = -voices[v].squareSign;
                        }
                    }

                    d3 += voices[v].squareSign;
                    voices[v].squarePos = d3;
                    voices[v].plantSquare = 1;
                    voices[v].squareWait = voices[v].instrument.squareSpeed;
                }
            }

            if (voices[v].filterOn != 0 && --voices[v].filterWait <= 0) {
                int d1 = voices[v].filterLowerLimit;
                int d2 = voices[v].filterUpperLimit;
                int d3 = voices[v].filterPos;
                if (voices[v].filterInit != 0) {
                    voices[v].filterInit = 0;
                    if (d3 <= d1) {
                        voices[v].filterSlidingIn = 1;
                        voices[v].filterSign = 1;
                    } else if (d3 >= d2) {
                        voices[v].filterSlidingIn = 1;
                        voices[v].filterSign = -1;
                    }
                }

                // NoFilterInit
                int fMax = (voices[v].filterSpeed < 3) ? (5 - voices[v].filterSpeed) : 1;
                for (int i = 0; i < fMax; i++) {
                    if (d3 <= d1 || d3 >= d2) {
                        if (voices[v].filterSlidingIn != 0) {
                            voices[v].filterSlidingIn = 0;
                        } else {
                            voices[v].filterSign = -voices[v].filterSign;
                        }
                    }

                    d3 += voices[v].filterSign;
                }

                if (d3 < 1) {
                    d3 = 1;
                    voices[v].filterSign = 1;
                } else if (d3 > 63) {
                    d3 = 63;
                    voices[v].filterSign = -1;
                }

                voices[v].filterPos = d3;
                voices[v].newWaveform = 1;
                voices[v].filterWait = voices[v].filterSpeed - 3;
                if (voices[v].filterWait < 1) voices[v].filterWait = 1;
            }

            if (voices[v].waveform == 3 - 1 || voices[v].plantSquare != 0) {
                // CalcSquare
                int squarePtr = AHXWaves.offSquares + (voices[v].filterPos - 0x20) * (0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 0x280 * 3);
                int x = voices[v].squarePos << (5 - voices[v].waveLength);
                if (x > 0x20) {
                    x = 0x40 - x;
                    voices[v].squareReverse = 1;
                }

                // OkDownSquare
                if (--x != 0) squarePtr += x << 7; // TODO int overflow
                int delta = 32 >> voices[v].waveLength;
                waveformTab[2] = new ByteSlice(voices[v].squareTempBuffer);
                for (int i = 0; i < (1 << voices[v].waveLength) * 4; i++) {
                    voices[v].squareTempBuffer[i] = waves.waveBuffer[squarePtr];
                    squarePtr += delta;
                }

                voices[v].newWaveform = 1;
                voices[v].waveform = 3 - 1;
                voices[v].plantSquare = 0;
            }

            if (voices[v].waveform == 4 - 1) voices[v].newWaveform = 1;

            if (voices[v].newWaveform != 0) {
                if (voices[v].waveform == 2) {
                    voices[v].audioSource = waveformTab[voices[v].waveform];
                } else {
                    int audioSource = waves.waveToOffset(waveformTab[voices[v].waveform]);
                    if (voices[v].waveform != 3 - 1) {
                        audioSource += (voices[v].filterPos - 0x20) * (0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 0x280 * 3);
                    }

                    if (voices[v].waveform < 3 - 1) {
                        int[] offsets = {0x00, 0x04, 0x04 + 0x08, 0x04 + 0x08 + 0x10, 0x04 + 0x08 + 0x10 + 0x20, 0x04 + 0x08 + 0x10 + 0x20 + 0x40};
                        audioSource += offsets[voices[v].waveLength];
                    }

                    if (voices[v].waveform == 4 - 1) {
                        audioSource += (wnRandom & (2 * 0x280 - 1)) & ~1;
                        wnRandom += 2239384;
                        wnRandom = ((((wnRandom >> 8) | (wnRandom << 24)) + 782323) ^ 75) - 6735;
                    }

                    voices[v].audioSource = waves.offsetToWave(audioSource);
                }
            }

            // StillHoldWaveform
            // AudioInitPeriod
            voices[v].audioPeriod = voices[v].instrPeriod;
            if (voices[v].fixedNote == 0) voices[v].audioPeriod += voices[v].transpose + voices[v].trackPeriod - 1;
            if (voices[v].audioPeriod > 5 * 12) voices[v].audioPeriod = 5 * 12;
            if (voices[v].audioPeriod < 0) voices[v].audioPeriod = 0;
            voices[v].audioPeriod = PERIOD_TABLE[voices[v].audioPeriod];
            if (voices[v].fixedNote == 0) voices[v].audioPeriod += voices[v].periodSlidePeriod;
            voices[v].audioPeriod += voices[v].periodPerfSlidePeriod + voices[v].vibratoPeriod;
            if (voices[v].audioPeriod > 0x0d60) voices[v].audioPeriod = 0x0d60;
            if (voices[v].audioPeriod < 0x0071) voices[v].audioPeriod = 0x0071;
            // AudioInitVolume
            voices[v].audioVolume = ((((((((voices[v].adsrVolume >> 8) * voices[v].noteMaxVolume) >> 6) * voices[v].perfSubVolume) >> 6) * voices[v].trackMasterVolume) >> 6) * mainVolume) >> 6;
        }

        public void setAudio(int v) {
            if (voices[v].trackOn == 0) {
                voices[v].voiceVolume = 0;
                return;
            }

            voices[v].voiceVolume = voices[v].audioVolume;
            if (voices[v].plantPeriod != 0) {
                voices[v].plantPeriod = 0;
                voices[v].voicePeriod = voices[v].audioPeriod;
            }

            if (voices[v].newWaveform != 0) {
                if (voices[v].waveform == 4 - 1) {
                    ByteSlice src = voices[v].audioSource;
                    for (int j = 0; j < 0x280; j++) {
                        voices[v].voiceBuffer[j] = src.get(j);
                    }
                } else {
                    int waveLoops = (1 << (5 - voices[v].waveLength)) * 5;
                    for (int i = 0; i < waveLoops; i++) {
                        ByteSlice src = voices[v].audioSource;
                        int size = 4 * (1 << voices[v].waveLength);
                        for (int j = 0; j < size; j++) {
                            voices[v].voiceBuffer[i * size + j] = src.get(j);
                        }
                    }
                }

                voices[v].voiceBuffer[0x280] = voices[v].voiceBuffer[0];
            }
        }

        public void pListCommandParse(int v, int fx, int fxParam) {
            switch (fx) {
                case 0:
                    if (song.revision > 0 && fxParam != 0) {
                        int pos = fxParam;
                        if (voices[v].ignoreFilter != 0) {
                            pos = voices[v].ignoreFilter;
                            voices[v].ignoreFilter = 0;
                        }
                        if (pos < 1) pos = 1;
                        if (pos > 63) pos = 63;
                        voices[v].filterPos = pos;
                        voices[v].newWaveform = 1;
                    }
                    break;
                case 1:
                    voices[v].periodPerfSlideSpeed = fxParam;
                    voices[v].periodPerfSlideOn = 1;
                    break;
                case 2:
                    voices[v].periodPerfSlideSpeed = -fxParam;
                    voices[v].periodPerfSlideOn = 1;
                    break;
                case 3: // Init Square Modulation
                    if (voices[v].ignoreSquare == 0) {
                        voices[v].squarePos = fxParam >> (5 - voices[v].waveLength);
                    } else {
                        voices[v].ignoreSquare = 0;
                    }
                    break;
                case 4: // Start/Stop Modulation
                    if (song.revision == 0 || fxParam == 0) {
                        voices[v].squareOn ^= 1;
                        voices[v].squareInit = voices[v].squareOn;
                        voices[v].squareSign = 1;
                    } else {
                        if ((fxParam & 0x0f) != 0) {
                            voices[v].squareOn ^= 1;
                            voices[v].squareInit = voices[v].squareOn;
                            voices[v].squareSign = 1;
                            if ((fxParam & 0x0f) == 0x0f) voices[v].squareSign = -1;
                        }

                        if ((fxParam & 0xf0) != 0) {
                            voices[v].filterOn ^= 1;
                            voices[v].filterInit = voices[v].filterOn;
                            voices[v].filterSign = 1;
                            if ((fxParam & 0xf0) == 0xf0) voices[v].filterSign = -1;
                        }
                    }
                    break;
                case 5: // Jump to Step [xx]
                    voices[v].perfCurrent = fxParam;
                    break;
                case 6: // Set Volume
                    if (fxParam > 0x40) {
                        fxParam -= 0x50;
                        if (fxParam >= 0) {
                            if (fxParam <= 0x40) {
                                voices[v].perfSubVolume = fxParam;
                            } else {
                                fxParam -= (0xa0 - 0x50);
                                if (fxParam >= 0 && fxParam <= 0x40) {
                                    voices[v].trackMasterVolume = fxParam;
                                }
                            }
                        }
                    } else {
                        voices[v].noteMaxVolume = fxParam;
                    }
                    break;
                case 7: // set speed
                    voices[v].perfSpeed = voices[v].perfWait = fxParam;
                    break;
            }
        }

        public void voiceOnOff(int voice, int onOff) {
            if (voice < 0 || voice > 3) return;
            voices[voice].trackOn = onOff;
        }

        public static class AHXVoice {
            public int voiceVolume;
            public int voicePeriod;
            public byte[] voiceBuffer = new byte[0x281]; // for oversampling optimization!

            public int track;
            public int transpose;
            public int nextTrack;
            public int nextTranspose;
            public int adsrVolume; // fixed point 8:8
            public AHXEnvelope adsr = new AHXEnvelope(); // frames/delta fixed 8:8
            public AHXInstrument instrument; // current instrument
            public int instrPeriod;
            public int trackPeriod;
            public int vibratoPeriod;
            public int noteMaxVolume;
            public int perfSubVolume;
            public int trackMasterVolume;
            public int newWaveform;
            public int waveform;
            public int plantSquare;
            public int plantPeriod;
            public int ignoreSquare;
            public int trackOn;
            public int fixedNote;
            public int volumeSlideUp;
            public int volumeSlideDown;
            public int hardCut;
            public int hardCutRelease;
            public int hardCutReleaseF;
            public int periodSlideSpeed;
            public int periodSlidePeriod;
            public int periodSlideLimit;
            public int periodSlideOn;
            public int periodSlideWithLimit;
            public int periodPerfSlideSpeed;
            public int periodPerfSlidePeriod;
            public int periodPerfSlideOn;
            public int vibratoDelay;
            public int vibratoCurrent;
            public int vibratoDepth;
            public int vibratoSpeed;
            public int squareOn;
            public int squareInit;
            public int squareWait;
            public int squareLowerLimit;
            public int squareUpperLimit;
            public int squarePos;
            public int squareSign;
            public int squareSlidingIn;
            public int squareReverse;
            public int filterOn;
            public int filterInit;
            public int filterWait;
            public int filterLowerLimit;
            public int filterUpperLimit;
            public int filterPos;
            public int filterSign;
            public int filterSpeed;
            public int filterSlidingIn;
            public int ignoreFilter;
            public int perfCurrent;
            public int perfSpeed;
            public int perfWait;
            public int waveLength;
            public AHXPList perfList;
            public int noteDelayWait;
            public int noteDelayOn;
            public int noteCutWait;
            public int noteCutOn;
            public byte[] audioPointer;
            public ByteSlice audioSource;
            public int audioPeriod;
            public int audioVolume;
            public byte[] squareTempBuffer = new byte[0x80];

            public AHXVoice() {
                init();
            }

            public void init() {
                trackOn = 1;
                trackMasterVolume = 0x40;
            }

            public void calcADSR() {
                adsr.aFrames = instrument.envelope.aFrames;
                adsr.aVolume = instrument.envelope.aVolume * 256 / adsr.aFrames;
                adsr.dFrames = instrument.envelope.dFrames;
                adsr.dVolume = (instrument.envelope.dVolume - instrument.envelope.aVolume) * 256 / adsr.dFrames;
                adsr.sFrames = instrument.envelope.sFrames;
                adsr.rFrames = instrument.envelope.rFrames;
                adsr.rVolume = (instrument.envelope.rVolume - instrument.envelope.dVolume) * 256 / adsr.rFrames;
            }
        }

        public static class AHXWaves {
            public final byte[] waveBuffer = new byte[TOTAL_SIZE];

            private static final int offLowPasses = 0;
            private static final int offTriangle04 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31);
            private static final int offTriangle08 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4;
            private static final int offTriangle10 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8;
            private static final int offTriangle20 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16;
            private static final int offTriangle40 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32;
            private static final int offTriangle80 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64;
            private static final int offSawtooth04 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128;
            private static final int offSawtooth08 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4;
            private static final int offSawtooth10 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8;
            private static final int offSawtooth20 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16;
            private static final int offSawtooth40 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16 + 32;
            private static final int offSawtooth80 = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16 + 32 + 64;
            public static final int offSquares = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16 + 32 + 64 + 128;
            private static final int offWhiteNoiseBig = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16 + 32 + 64 + 128 + (0x80 * 0x20);
            private static final int offHighPasses = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16 + 32 + 64 + 128 + (0x80 * 0x20) + (0x280 * 3);
            private static final int TOTAL_SIZE = ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31) + 4 + 8 + 16 + 32 + 64 + 128 + 4 + 8 + 16 + 32 + 64 + 128 + (0x80 * 0x20) + (0x280 * 3) + ((0xfc + 0xfc + 0x80 * 0x1f + 0x80 + 3 * 0x280) * 31);

            public final ByteSlice LowPasses;
            public final ByteSlice Triangle04;
            public final ByteSlice Triangle08;
            public final ByteSlice Triangle10;
            public final ByteSlice Triangle20;
            public final ByteSlice Triangle40;
            public final ByteSlice Triangle80;
            public final ByteSlice Sawtooth04;
            public final ByteSlice Sawtooth08;
            public final ByteSlice Sawtooth10;
            public final ByteSlice Sawtooth20;
            public final ByteSlice Sawtooth40;
            public final ByteSlice Sawtooth80;
            public final ByteSlice Squares;
            public final ByteSlice WhiteNoiseBig;
            public final ByteSlice HighPasses;

            private final Map<Integer, ByteSlice> waveCache = new HashMap<>();

            public AHXWaves() {
                LowPasses = new ByteSlice(waveBuffer, 0, offTriangle04);
                Triangle04 = new ByteSlice(waveBuffer, offTriangle04, offTriangle08 - offTriangle04);
                Triangle08 = new ByteSlice(waveBuffer, offTriangle08, offTriangle10 - offTriangle04);
                Triangle10 = new ByteSlice(waveBuffer, offTriangle10, offTriangle20 - offTriangle10);
                Triangle20 = new ByteSlice(waveBuffer, offTriangle20, offTriangle40 - offTriangle20);
                Triangle40 = new ByteSlice(waveBuffer, offTriangle40, offTriangle80 - offTriangle40);
                Triangle80 = new ByteSlice(waveBuffer, offTriangle80, offSawtooth04 - offTriangle80);
                Sawtooth04 = new ByteSlice(waveBuffer, offSawtooth04, offSawtooth08 - offSawtooth04);
                Sawtooth08 = new ByteSlice(waveBuffer, offSawtooth08, offSawtooth10 - offSawtooth08);
                Sawtooth10 = new ByteSlice(waveBuffer, offSawtooth10, offSawtooth20 - offSawtooth10);
                Sawtooth20 = new ByteSlice(waveBuffer, offSawtooth20, offSawtooth40 - offSawtooth20);
                Sawtooth40 = new ByteSlice(waveBuffer, offSawtooth40, offSawtooth80 - offSawtooth40);
                Sawtooth80 = new ByteSlice(waveBuffer, offSawtooth80, offSquares - offSawtooth80);
                Squares = new ByteSlice(waveBuffer, offSquares, offWhiteNoiseBig - offSquares);
                WhiteNoiseBig = new ByteSlice(waveBuffer, offWhiteNoiseBig, offHighPasses - offWhiteNoiseBig);
                HighPasses = new ByteSlice(waveBuffer, offHighPasses, waveBuffer.length - offHighPasses);

                waveCache.put(0, LowPasses);
                waveCache.put(offTriangle04, Triangle04);
                waveCache.put(offTriangle08, Triangle08);
                waveCache.put(offTriangle10, Triangle10);
                waveCache.put(offTriangle20, Triangle20);
                waveCache.put(offTriangle40, Triangle40);
                waveCache.put(offTriangle80, Triangle80);
                waveCache.put(offSawtooth04, Sawtooth04);
                waveCache.put(offSawtooth08, Sawtooth08);
                waveCache.put(offSawtooth10, Sawtooth10);
                waveCache.put(offSawtooth20, Sawtooth20);
                waveCache.put(offSawtooth40, Sawtooth40);
                waveCache.put(offSawtooth80, Sawtooth80);
                waveCache.put(offSquares, Squares);
                waveCache.put(offWhiteNoiseBig, WhiteNoiseBig);
                waveCache.put(offHighPasses, HighPasses);

                generate();
            }

            private void generate() {
                generateSawtooth(waveBuffer, offSawtooth04, 0x04);
                generateSawtooth(waveBuffer, offSawtooth08, 0x08);
                generateSawtooth(waveBuffer, offSawtooth10, 0x10);
                generateSawtooth(waveBuffer, offSawtooth20, 0x20);
                generateSawtooth(waveBuffer, offSawtooth40, 0x40);
                generateSawtooth(waveBuffer, offSawtooth80, 0x80);
                generateTriangle(waveBuffer, offTriangle04, 0x04);
                generateTriangle(waveBuffer, offTriangle08, 0x08);
                generateTriangle(waveBuffer, offTriangle10, 0x10);
                generateTriangle(waveBuffer, offTriangle20, 0x20);
                generateTriangle(waveBuffer, offTriangle40, 0x40);
                generateTriangle(waveBuffer, offTriangle80, 0x80);
                generateSquare(waveBuffer, offSquares);
                generateWhiteNoise(waveBuffer, offWhiteNoiseBig, 0x280 * 3);
                generateFilterWaveforms(waveBuffer, offTriangle04, waveBuffer, 0, waveBuffer, offHighPasses);
            }

            public int waveToOffset(ByteSlice wave) {
                if (wave.equals(LowPasses)) return offLowPasses;
                if (wave.equals(Triangle04)) return offTriangle04;
                if (wave.equals(Triangle08)) return offTriangle08;
                if (wave.equals(Triangle10)) return offTriangle10;
                if (wave.equals(Triangle20)) return offTriangle20;
                if (wave.equals(Triangle40)) return offTriangle40;
                if (wave.equals(Triangle80)) return offTriangle80;
                if (wave.equals(Sawtooth04)) return offSawtooth04;
                if (wave.equals(Sawtooth08)) return offSawtooth08;
                if (wave.equals(Sawtooth10)) return offSawtooth10;
                if (wave.equals(Sawtooth20)) return offSawtooth20;
                if (wave.equals(Sawtooth40)) return offSawtooth40;
                if (wave.equals(Sawtooth80)) return offSawtooth80;
                if (wave.equals(Squares)) return offSquares;
                if (wave.equals(WhiteNoiseBig)) return offWhiteNoiseBig;
                if (wave.equals(HighPasses)) return offHighPasses;
                throw new IllegalArgumentException();
            }

            public ByteSlice offsetToWave(int offset) {
                ByteSlice wave = waveCache.get(offset);
                if (wave != null) return wave;
                wave = new ByteSlice(waveBuffer, offset, waveBuffer.length - offset);
                waveCache.put(offset, wave);
                return wave;
            }

            private static void generateFilterWaveforms(byte[] buffer, int bufferOff, byte[] low, int lowOff, byte[] high, int highOff) {
                int[] lengthTable = {
                    3, 7, 0xf, 0x1f, 0x3f, 0x7f, 3, 7, 0xf, 0x1f, 0x3f, 0x7f,
                    0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
                    0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f,
                    (0x280 * 3) - 1
                };

                int lowHigh = 0;
                for (int temp = 0, freq = 8; temp < 31; temp++, freq += 3) {
                    int a0 = 0;
                    for (int waves = 0; waves < 6 + 6 + 0x20 + 1; waves++) {
                        float fre = (float) freq * 1.25f / 100.0f;
                        float mid = 0.0f, lowVal = 0.0f;
                        for (int i = 0; i <= lengthTable[waves]; i++) {
                            float highVal = buffer[bufferOff + a0 + i] - mid - lowVal;
                            highVal = Math.clamp(highVal, -128.0f, 127.0f);
                            mid += highVal * fre;
                            mid = Math.clamp(mid, -128.0f, 127.0f);
                            lowVal += mid * fre;
                            lowVal = Math.clamp(lowVal, -128.0f, 127.0f);
                        }

                        for (int i = 0; i <= lengthTable[waves]; i++) {
                            float highVal = buffer[bufferOff + a0 + i] - mid - lowVal;
                            highVal = Math.clamp(highVal, -128.0f, 127.0f);
                            mid += highVal * fre;
                            mid = Math.clamp(mid, -128.0f, 127.0f);
                            lowVal += mid * fre;
                            lowVal = Math.clamp(lowVal, -128.0f, 127.0f);
                            low[lowOff + lowHigh] = (byte) lowVal;
                            high[highOff + lowHigh] = (byte) highVal;
                            lowHigh++;
                        }

                        a0 += lengthTable[waves] + 1;
                    }
                }
            }

            private static void generateTriangle(byte[] buffer, int offset, int len) {
                int d2 = len;
                int d5 = d2 >> 2;
                int d1 = 128 / d5;
                int d4 = -(d2 >> 1);
                int edi = 0;
                int eax = 0;
                for (int ecx = 0; ecx < d5; ecx++) {
                    buffer[offset + edi++] = (byte) eax;
                    eax += d1;
                }

                buffer[offset + edi++] = 0x7f;
                if (d5 != 1) {
                    eax = 128;
                    for (int ecx = 0; ecx < d5 - 1; ecx++) {
                        eax -= d1;
                        buffer[offset + edi++] = (byte) eax;
                    }
                }

                int esi = edi + d4;
                for (int ecx = 0; ecx < d5 * 2; ecx++) {
                    buffer[offset + edi++] = buffer[offset + esi++];
                    if (buffer[offset + edi - 1] == 0x7f) {
                        buffer[offset + edi - 1] = -128;
                    } else {
                        buffer[offset + edi - 1] = (byte) -buffer[offset + edi - 1];
                    }
                }
            }

            private static void generateSquare(byte[] buffer, int offset) {
                int edi = 0;
                for (int ebx = 1; ebx <= 0x20; ebx++) {
                    for (int ecx = 0; ecx < (0x40 - ebx) * 2; ecx++) {
                        buffer[offset + edi++] = -128;
                    }
                    for (int ecx = 0; ecx < ebx * 2; ecx++) {
                        buffer[offset + edi++] = 0x7f;
                    }
                }
            }

            private static void generateSawtooth(byte[] buffer, int offset, int len) {
                int edi = 0;
                int ebx = 256 / (len - 1);
                int eax = -128;
                for (int ecx = 0; ecx < len; ecx++) {
                    buffer[offset + edi++] = (byte) eax;
                    eax += ebx;
                }
            }

            private static void generateWhiteNoise(byte[] buffer, int offset, int len) {
                int eax = 0x41595321;
                int bx;
                int bptr = 0;
                while (len-- != 0) {
                    if ((eax & 0x100) != 0) {
                        if ((short) eax >= 0) {
                            buffer[offset + bptr++] = 0x7f;
                        } else {
                            buffer[offset + bptr++] = -128;
                        }
                    } else {
                        buffer[offset + bptr++] = (byte) eax;
                    }

                    eax = (eax >> 5) | (eax << 27);
                    eax = eax ^ (128 + 16 + 8 + 2);
                    bx = eax & 0xffff;
                    eax = (eax << 2) | (eax >> 30);
                    bx = (bx + (eax & 0xffff)) & 0xffff;
                    eax ^= bx;
                    eax = (eax >> 3) | (eax << 29);
                }
            }
        }
    }

    public static class AHXOutput {
        public static final int AHXOF_BOOST = 0;
        public static final int AHXOI_OVERSAMPLING = 1;

        public static float period2Freq(float period) {
            return 3579545.25f / period;
        }

        public int bits, frequency, mixLen;
        public int hz;
        public int playing, paused;

        public AHXPlayer player;

        // Options
        public int oversampling;
        public float boost;

        public int[] mixingBuffer;
        public final int[][] volumeTable = new int[65][256];

        public AHXOutput() {
            player = null;
            mixingBuffer = null;
            playing = paused = 0;
        }

        public int init(int frequency, int bits, int mixLen, float boost, int hz) {
            this.mixLen = mixLen;
            this.frequency = frequency;
            this.bits = bits;
            this.hz = hz;
            this.mixingBuffer = new int[mixLen * frequency / hz];
            return setOption(AHXOF_BOOST, boost);
        }

        private int free() {
            mixingBuffer = null;
            return 1;
        }

        public int setOption(int option, int value) {
            return switch (option) {
                case AHXOI_OVERSAMPLING -> {
                    oversampling = value;
                    yield 1;
                }
                default -> throw new IllegalArgumentException("option");
            };
        }

        public int setOption(int option, float value) {
            switch (option) {
                case AHXOF_BOOST: {
                    for (int i = 0; i < 65; i++) {
                        for (int j = -128; j < 128; j++) {
                            volumeTable[i][j + 128] = (int) (i * j * value) / 64;
                        }
                    }
                    boost = value;
                    return 1;
                }
                default:
                    throw new IllegalArgumentException("option");
            }
        }

        public int getOption(int option, int[] pValue) {
            switch (option) {
                case AHXOI_OVERSAMPLING:
                    pValue[0] = oversampling;
                    return 1;
                default:
                    throw new IllegalArgumentException("option");
            }
        }

        public int getOption(int option, float[] pValue) {
            return switch (option) {
                case AHXOF_BOOST -> {
                    pValue[0] = boost;
                    yield 1;
                }
                default -> throw new IllegalArgumentException("option");
            };
        }

        private static final int[] pos = { 0, 0, 0, 0 };

        private int mixChunk(int nrSamples, int mb) {
            for (int v = 0; v < 4; v++) {
                if (player.voices[v].voiceVolume == 0) continue;
                float freq = period2Freq(player.voices[v].voicePeriod);
                int delta = (int) (freq * (1 << 16) / frequency);
                int samplesToMix = nrSamples;
                int mixpos = 0;
                while (samplesToMix != 0) {
                    if (pos[v] > (0x280 << 16)) pos[v] -= 0x280 << 16;
                    int thiscount = Math.min(samplesToMix, ((0x280 << 16) - pos[v] - 1) / delta + 1);
                    samplesToMix -= thiscount;
                    int[] volTab = volumeTable[player.voices[v].voiceVolume];
                    // INNER LOOP
                    if (oversampling != 0) {
                        for (int i = 0; i < thiscount; i++) {
                            int offset = pos[v] >> 16;
                            int sample1 = volTab[player.voices[v].voiceBuffer[offset] + 128];
                            int sample2 = volTab[player.voices[v].voiceBuffer[offset + 1] + 128];
                            int frac1 = pos[v] & ((1 << 16) - 1);
                            int frac2 = (1 << 16) - frac1;
                            mixingBuffer[mb + mixpos++] += ((sample1 * frac2) + (sample2 * frac1)) >> 16;
                            pos[v] += delta;
                        }
                    } else {
                        for (int i = 0; i < thiscount; i++) {
                            mixingBuffer[mb + mixpos++] += volTab[player.voices[v].voiceBuffer[pos[v] >> 16] + 128];
                            pos[v] += delta;
                        }
                    }
                } // while
            } // v = 0-3
            return mb + nrSamples;
        }

        public void mixBuffer() {
            int nrSamples = frequency / hz / player.song.speedMultiplier;
            int mb = 0;

            java.util.Arrays.fill(mixingBuffer, 0);
            for (int f = 0; f < mixLen * player.song.speedMultiplier; f++) {
                player.playIRQ();
                mb = mixChunk(nrSamples, mb);
            } // frames
        }
    }
}
