package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2612Chip;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;
import static java.lang.System.getLogger;


public class MIDIExport {

    private static final Logger logger = getLogger(MIDIExport.class.getName());

    private final Setting setting;

    private MidiChip midi2151 = new MidiChip();
    private MidiChip midi2612 = new MidiChip();

    private List<Byte> cData = null;

    public String playingFileName = "";
    public int[][][] registerYM2612 = null;
    public int[][] registerYM2151 = null;

    public MIDIExport() {
        this.setting = Setting.getInstance();
    }

    public void outMIDIData(Class<? extends Chip> chip, int chipId, int dPort, int dAddr, int dData, int hosei, long vgmFrameCounter) {
        if (!setting.getMidiExport().getUseMIDIExport()) return;
        if (setting.getMidiExport().getExportPath().isEmpty()) return;
        if (vgmFrameCounter < 0) return;
        if (chipId != 0) return;

        if (chip != Ym2612Chip.class && chip != Ym2151Chip.class) return;

        if (chip == Ym2151Chip.class) {
            if (setting.getMidiExport().getUseYM2151Export()) {
                outMIDIData_YM2151(chipId, dPort, dAddr, dData, hosei, vgmFrameCounter);
            }
        } else if (chip == Ym2612Chip.class) {
            if (setting.getMidiExport().getUseYM2612Export()) {
                outMIDIData_YM2612(chipId, dPort, dAddr, dData, vgmFrameCounter);
            }
        }
    }

    public void close() {
        if (!setting.getMidiExport().getUseMIDIExport()) return;
        if (setting.getMidiExport().getExportPath().isEmpty()) return;
        if (cData == null) return;
        //if (midiData.size() < 23) return;

        int portNum = 0;
        int trkNum = 1;

        if (setting.getMidiExport().getUseYM2151Export()) {
            for (int ch = 0; ch < midi2151.maxTrk; ch++) {
                SetDelta(ch, midi2151, midi2151.oldFrameCounter[ch]);

                if (midi2151.oldCode[ch] >= 0) {
                    midi2151.data[ch].add((byte) (0x80 | ch));
                    midi2151.data[ch].add((byte) midi2151.oldCode[ch]);
                    midi2151.data[ch].add((byte) 0x00);

                    midi2151.oldCode[ch] = -1;
                    midi2151.data[ch].add((byte) 0x00); // Delta 0
                }

                midi2151.data[ch].add((byte) 0xff); // Meta Events
                midi2151.data[ch].add((byte) 0x2f);
                midi2151.data[ch].add((byte) 0x00);

                int mTrkLengthAdr = 0x04;
                midi2151.data[ch].set(mTrkLengthAdr + 0, (byte) (((midi2151.data[ch].size() - (mTrkLengthAdr + 4)) & 0xff000000) >> 24));
                midi2151.data[ch].set(mTrkLengthAdr + 1, (byte) (((midi2151.data[ch].size() - (mTrkLengthAdr + 4)) & 0x00ff0000) >> 16));
                midi2151.data[ch].set(mTrkLengthAdr + 2, (byte) (((midi2151.data[ch].size() - (mTrkLengthAdr + 4)) & 0x0000ff00) >> 8));
                midi2151.data[ch].set(mTrkLengthAdr + 3, (byte) (((midi2151.data[ch].size() - (mTrkLengthAdr + 4)) & 0x000000ff) >> 0));

                int PortAdr = 0x08;
                midi2151.data[ch].set(PortAdr + 4, (byte) portNum);
            }

            portNum++;
            trkNum += midi2151.maxTrk;
        }

        if (setting.getMidiExport().getUseYM2612Export()) {
            for (int ch = 0; ch < midi2612.maxTrk; ch++) {
                SetDelta(ch, midi2612, midi2612.oldFrameCounter[ch]);

                if (midi2612.oldCode[ch] >= 0) {
                    midi2612.data[ch].add((byte) (0x80 | ch));
                    midi2612.data[ch].add((byte) midi2612.oldCode[ch]);
                    midi2612.data[ch].add((byte) 0x00);

                    midi2612.oldCode[ch] = -1;
                    midi2612.data[ch].add((byte) 0x00); // Delta 0
                }

                midi2612.data[ch].add((byte) 0xff); // Meta Events
                midi2612.data[ch].add((byte) 0x2f);
                midi2612.data[ch].add((byte) 0x00);

                int MTrkLengthAdr = 0x04;
                midi2612.data[ch].set(MTrkLengthAdr + 0, (byte) (((midi2612.data[ch].size() - (MTrkLengthAdr + 4)) & 0xff000000) >> 24));
                midi2612.data[ch].set(MTrkLengthAdr + 1, (byte) (((midi2612.data[ch].size() - (MTrkLengthAdr + 4)) & 0x00ff0000) >> 16));
                midi2612.data[ch].set(MTrkLengthAdr + 2, (byte) (((midi2612.data[ch].size() - (MTrkLengthAdr + 4)) & 0x0000ff00) >> 8));
                midi2612.data[ch].set(MTrkLengthAdr + 3, (byte) (((midi2612.data[ch].size() - (MTrkLengthAdr + 4)) & 0x000000ff) >> 0));

                int PortAdr = 0x08;
                midi2612.data[ch].set(PortAdr + 4, (byte) portNum);
            }

            portNum++;
            trkNum += midi2612.maxTrk;
        }

        cData.set(0xb, (byte) trkNum); // Number of tracks

        try {
            String fn = playingFileName.isEmpty() ? "Temp.mid" : playingFileName;

            List<Byte> buf = new ArrayList<>(cData);

            if (setting.getMidiExport().getUseYM2151Export()) for (List<Byte> dat : midi2151.data) buf.addAll(dat);
            if (setting.getMidiExport().getUseYM2612Export()) for (List<Byte> dat : midi2612.data) buf.addAll(dat);

            File.writeAllBytes(Path.combine(setting.getMidiExport().getExportPath(), Path.changeExtension(Path.getFileName(fn), ".mid")), toByteArray(buf));
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        cData = null;
        midi2151 = null;
        midi2612 = null;
    }

    private void outMIDIData_YM2151(int chipId, int dPort, int dAddr, int dData, int hosei, long vgmFrameCounter) {
        if (cData == null) {
            makeHeader();
        }

        if (dAddr == 0x08) {
            int ch = dData & 0x7;
            int cmd = (dData & 0x78) != 0 ? 0x90 : 0x80;

            int freq = (registerYM2151[chipId][0x28 + ch] & 0x7f) + (registerYM2151[chipId][0x30 + ch] & 0xfc) * 0x100;
            int octNote = registerYM2151[chipId][0x28 + ch] & 0x7f;
            if (octNote == 0) return;
            int octav = (octNote & 0x70) >> 4;
            int note = searchOPMNote(octNote) + hosei;
            int code = octav * 12 + note;
            int vel = 127 - registerYM2151[chipId][0x78 + ch];

            if (midi2151.oldFreq[ch] < 0 && cmd == 0x80) return;

            SetDelta(ch, midi2151, vgmFrameCounter);

            if (midi2151.oldCode[ch] >= 0 || cmd == 0x80) {
                midi2151.data[ch].add((byte) (0x80 | ch));
                midi2151.data[ch].add((byte) midi2151.oldCode[ch]);
                midi2151.data[ch].add((byte) 0x00);

                midi2151.oldCode[ch] = -1;
                midi2151.oldFreq[ch] = -1;

                if (cmd != 0x80) midi2151.data[ch].add((byte) 0); // NextDeltaTime
            }

            if (cmd == 0x90) {
                midi2151.data[ch].add((byte) (0x90 | ch));
                midi2151.data[ch].add((byte) code);
                if (setting.getMidiExport().getUseVOPMex()) {
                    midi2151.data[ch].add((byte) 127);
                } else {
                    midi2151.data[ch].add((byte) vel);
                }

                midi2151.oldCode[ch] = code;
                midi2151.oldFreq[ch] = freq;
            }

            return;
        }

        if (!setting.getMidiExport().getKeyOnFnum()) {
            if (dAddr >= 0x28 && dAddr < 0x30) {
                int ch = dAddr & 0x7;
                int freq = midi2151.oldFreq[ch];
                if (freq == -1) return;

                freq = (freq & 0xfc00) | (dData & 0x007f);

                if (freq != midi2151.oldFreq[ch]) {
                    int freq2nd = freq & 0x007f;
                   //if (freq2nd == 0) return;
                    int octav = (freq & 0x0070) >> 4;
                    int note = searchOPMNote(freq2nd) + hosei;
                    int code = octav * 12 + note;

                    if (midi2151.oldCode[ch] != -1 && midi2151.oldCode[ch] != code) {
                        SetDelta(ch, midi2151, vgmFrameCounter);
                        midi2151.data[ch].add((byte) (0x80 | ch));
                        midi2151.data[ch].add((byte) midi2151.oldCode[ch]);
                        midi2151.data[ch].add((byte) 0x00);

                        midi2151.data[ch].add((byte) 0); // delta0
                        midi2151.data[ch].add((byte) (0x90 | ch));
                        midi2151.data[ch].add((byte) code);
                        if (setting.getMidiExport().getUseVOPMex()) {
                            midi2151.data[ch].add((byte) 127);
                        } else {
                            int vel = 127 - registerYM2151[chipId][0x78 + ch];
                            midi2151.data[ch].add((byte) vel);
                        }

                        midi2151.oldFreq[ch] = freq;
                        midi2151.oldCode[ch] = code;
                    }
                }

                return;
            }
        }

        //
        // for VOPMex
        //
        if (!setting.getMidiExport().getUseVOPMex()) return;

        if (dAddr >= 0x30 && dAddr < 0x38) {
            int ch = dAddr & 0x7;
            int bend = (dData & 0xfc) >> 2;
            bend *= 64;
            bend += 8192;

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xe0 | ch)); // pitch bend
            midi2151.data[ch].add((byte) (bend & 0x7f));
            midi2151.data[ch].add((byte) ((bend & 0x3f80) >> 7));

        } else if (dAddr >= 0x40 && dAddr < 0x60) {
            // DT/ML
            int ch = dAddr & 0x7;
            int op = (dAddr & 0x18) >> 3;
            op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // DT
            midi2151.data[ch].add((byte) (24 + op));
            midi2151.data[ch].add((byte) ((dData & 0x70) >> 4));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // ML
            midi2151.data[ch].add((byte) (20 + op));
            midi2151.data[ch].add((byte) ((dData & 0x0f) >> 0));

        } else if (dAddr >= 0x60 && dAddr < 0x80) {
            // TL
            int ch = dAddr & 0x7;
            int op = (dAddr & 0x18) >> 3;
            op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // TL
            midi2151.data[ch].add((byte) (16 + op));
            midi2151.data[ch].add((byte) ((dData & 0x7f) >> 0));

        } else if (dAddr >= 0x80 && dAddr < 0xa0) {
            // KS/AR
            int ch = dAddr & 0x7;
            int op = (dAddr & 0x18) >> 3;
            op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // KS
            midi2151.data[ch].add((byte) (39 + op));
            midi2151.data[ch].add((byte) ((dData & 0xc0) >> 6));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // AR
            midi2151.data[ch].add((byte) (43 + op));
            midi2151.data[ch].add((byte) ((dData & 0x1f) >> 0));

        } else if (dAddr >= 0xa0 && dAddr < 0xc0) {
            // AMS/DR
            int ch = dAddr & 0x7;
            int op = (dAddr & 0x18) >> 3;
            op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // AMS
            midi2151.data[ch].add((byte) (70 + op));
            midi2151.data[ch].add((byte) ((dData & 0x80) >> 7));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // DR
            midi2151.data[ch].add((byte) (47 + op));
            midi2151.data[ch].add((byte) ((dData & 0x1f) >> 0));

        } else if (dAddr >= 0xc0 && dAddr < 0xe0) {
            // DT2/SR
            int ch = dAddr & 0x7;
            int op = (dAddr & 0x18) >> 3;
            op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // DT2
            midi2151.data[ch].add((byte) (28 + op));
            midi2151.data[ch].add((byte) ((dData & 0xc0) >> 6));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // SR
            midi2151.data[ch].add((byte) (51 + op));
            midi2151.data[ch].add((byte) ((dData & 0x1f) >> 0));

        } else if (dAddr >= 0xe0 && dAddr < 0x100) {
            // DL/RR
            int ch = dAddr & 0x7;
            int op = (dAddr & 0x18) >> 3;
            op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // DL
            midi2151.data[ch].add((byte) (55 + op));
            midi2151.data[ch].add((byte) ((dData & 0xf0) >> 4));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // RR
            midi2151.data[ch].add((byte) (59 + op));
            midi2151.data[ch].add((byte) ((dData & 0x0f) >> 0));

        } else if (dAddr >= 0x20 && dAddr < 0x28) {
            // PAN/FB/ALG
            int ch = dAddr & 0x7;

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // PAN
            midi2151.data[ch].add((byte) (10));
            int pan = (dData & 0xc0) >> 6;
            midi2151.data[ch].add((byte) (pan == 0 ? 64 : ((pan == 1) ? 127 : ((pan == 2) ? 1 : 64))));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // FB
            midi2151.data[ch].add((byte) (15));
            midi2151.data[ch].add((byte) ((dData & 0x38) >> 3));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // ALG
            midi2151.data[ch].add((byte) (14));
            midi2151.data[ch].add((byte) ((dData & 0x07) >> 0));

        } else if (dAddr >= 0x38 && dAddr < 0x40) {
            // AMS/FMS
            int ch = dAddr & 0x7;

            SetDelta(ch, midi2151, vgmFrameCounter);

            midi2151.data[ch].add((byte) (0xb0 | ch)); // AMS
            midi2151.data[ch].add((byte) (76));
            midi2151.data[ch].add((byte) ((dData & (byte) 0x03) >> 0));

            midi2151.data[ch].add((byte) 0); // Delta 0

            midi2151.data[ch].add((byte) (0xb0 | ch)); // FMS
            midi2151.data[ch].add((byte) (75));
            midi2151.data[ch].add((byte) ((dData & 0x70) >> 4));
        }
    }

    private void outMIDIData_YM2612(int chipId, int dPort, int dAddr, int dData, long vgmFrameCounter) {
        if (cData == null) {
            makeHeader();
        }

        // when KeyON
        if (dPort == 0 && dAddr == 0x28) {
            byte ch = (byte) (dData & 0x7);
            ch = (byte) (ch > 2 ? ch - 1 : ch);
            byte cmd = (byte) ((dData & 0xf0) != 0 ? 0x90 : 0x80); // If any operator is on, use noteON(0x90). If all are off, use noteOFF(0x80).

            // Get the channel and port information to read the required registers
            int p = ch > 2 ? 1 : 0;
            int vch = ch > 2 ? (ch - 3) : ch;
            if (ch > 5) return;

            // Get fNum of the channel that is keyed on
            midi2612.oldFreq[ch] = registerYM2612[chipId][p][0xa0 + vch] + (registerYM2612[chipId][p][0xa4 + vch] & 0x3f) * 0x100;
            int freq = midi2612.oldFreq[ch] & 0x7ff;
            if (freq == 0) return;
            int octave = (midi2612.oldFreq[ch] & 0x3800) >> 11;
            int note = searchFMNote(freq);
            byte code = (byte) (octave * 12 + note);

            // Get only the total level of operator 4 (used as volume)
            byte vel = (byte) (127 - registerYM2612[chipId][p][0x4c + vch]);

            // If the previous code was negative and noteOFF, the process ends without doing anything.
            if (midi2612.oldCode[ch] < 0 && cmd == (byte) 0x80) return;

            // Set delta (sets the time since the last data transmission)
            SetDelta(ch, midi2612, vgmFrameCounter);

            // If the previous chord was correct (pronounced) or noteOFF, issue a noteOFF command.
            if (midi2612.oldCode[ch] >= 0 || cmd == (byte) 0x80) {
                midi2612.data[ch].add((byte) (0x80 | ch));
                midi2612.data[ch].add((byte) midi2612.oldCode[ch]);
                midi2612.data[ch].add((byte) 0x00);

                midi2612.oldCode[ch] = -1;
                if (cmd != (byte) 0x80) midi2612.data[ch].add((byte) 0); // NextDeltaTime
            }

            // If noteON, issue the noteON command
            if (cmd == (byte) 0x90) {
                midi2612.data[ch].add((byte) (0x90 | ch));
                midi2612.data[ch].add(code);
                if (setting.getMidiExport().getUseVOPMex()) {
                    midi2612.data[ch].add((byte) 127);
                } else {
                    midi2612.data[ch].add(vel);
                }

                midi2612.oldCode[ch] = code;
            }

            return;
        }

        if (!setting.getMidiExport().getKeyOnFnum()) {
            // When fNum is set
            if (dAddr >= 0xa0 && dAddr < 0xa8) {
                // Read the fNum information
                byte ch = (byte) ((dAddr & 0x3) + dPort * 3);
                int freq = midi2612.oldFreq[ch];
                int vch = ch > 2 ? (ch - 3) : ch;
                if (freq == -1) return;
                if (dAddr < 0xa4) {
                    freq = (freq & 0x3f00) | dData;
                } else {
                    freq = (freq & 0xff) | ((dData & 0x3f) << 8);
                }

                // If the value is different from the previous time, investigate further.
                if (freq != midi2612.oldFreq[ch]) {
                    // Check the current scale
                    int freq2nd = freq & 0x07ff;
                    if (freq2nd == 0) return;
                    int octav = (freq & 0x3800) >> 11;
                    int note = searchFMNote(freq2nd);
                    byte code = (byte) (octav * 12 + note);

                    // Check if the current note is being pronounced and if the note is different from the previous note
                    if (midi2612.oldCode[ch] != -1 && midi2612.oldCode[ch] != code) {
                        // Turn the key off
                        SetDelta(ch, midi2612, vgmFrameCounter);
                        midi2612.data[ch].add((byte) (0x80 | ch));
                        midi2612.data[ch].add((byte) midi2612.oldCode[ch]);
                        midi2612.data[ch].add((byte) 0x00);

                        // Re-keyOn with this scale
                        midi2612.data[ch].add((byte) 0); // delta0
                        midi2612.data[ch].add((byte) (0x90 | ch));
                        midi2612.data[ch].add(code);
                        if (setting.getMidiExport().getUseVOPMex()) {
                            midi2612.data[ch].add((byte) 127);
                        } else {
                            byte vel = (byte) (127 - registerYM2612[chipId][dPort][0x4c + vch]);
                            midi2612.data[ch].add(vel);
                        }

                        midi2612.oldFreq[ch] = freq;
                        midi2612.oldCode[ch] = code;
                    }
                }

                return;
            }
        }

        //
        // for VOPMex
        //
        if (!setting.getMidiExport().getUseVOPMex()) return;

        if ((dAddr & 0xf0) == 0x30) {
            // DT/ML
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;
                int op = (dAddr & 0xc) >> 2;
                op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // DT
                midi2612.data[ch].add((byte) (24 + op));
                midi2612.data[ch].add((byte) ((dData & 0x70) >> 4));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // ML
                midi2612.data[ch].add((byte) (20 + op));
                midi2612.data[ch].add((byte) ((dData & 0x0f) >> 0));
            }
        } else if ((dAddr & 0xf0) == 0x40) {
            // TL
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;
                int op = (dAddr & 0xc) >> 2;
                op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // TL
                midi2612.data[ch].add((byte) (16 + op));
                midi2612.data[ch].add((byte) ((dData & 0x7f) >> 0));
            }
        } else if ((dAddr & 0xf0) == 0x50) {
            // KS/AR
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;
                int op = (dAddr & 0xc) >> 2;
                op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // KS
                midi2612.data[ch].add((byte) (39 + op));
                midi2612.data[ch].add((byte) ((dData & 0xc0) >> 6));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // AR
                midi2612.data[ch].add((byte) (43 + op));
                midi2612.data[ch].add((byte) ((dData & 0x1f) >> 0));
            }
        } else if ((dAddr & 0xf0) == 0x60) {
            // AMS/DR
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;
                int op = (dAddr & 0xc) >> 2;
                op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // AMS
                midi2612.data[ch].add((byte) (70 + op));
                midi2612.data[ch].add((byte) ((dData & 0x80) >> 7));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // DR
                midi2612.data[ch].add((byte) (47 + op));
                midi2612.data[ch].add((byte) ((dData & 0x1f) >> 0));
            }
        } else if ((dAddr & 0xf0) == 0x70) {
            // SR
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;
                int op = (dAddr & 0xc) >> 2;
                op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // SR
                midi2612.data[ch].add((byte) (51 + op));
                midi2612.data[ch].add((byte) ((dData & 0x1f) >> 0));
            }
        } else if ((dAddr & 0xf0) == 0x80) {
            // DL/RR
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;
                int op = (dAddr & 0xc) >> 2;
                op = (op == 1) ? 2 : ((op == 2) ? 1 : op);

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // DL
                midi2612.data[ch].add((byte) (55 + op));
                midi2612.data[ch].add((byte) ((dData & 0xf0) >> 4));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // RR
                midi2612.data[ch].add((byte) (59 + op));
                midi2612.data[ch].add((byte) ((dData & 0x0f) >> 0));
            }
        } else if (dAddr >= 0xB0 && dAddr < 0xB4) {
            // FB/ALG
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // FB
                midi2612.data[ch].add((byte) (15));
                midi2612.data[ch].add((byte) ((dData & 0x38) >> 3));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // ALG
                midi2612.data[ch].add((byte) (14));
                midi2612.data[ch].add((byte) ((dData & 0x07) >> 0));
            }
        } else if (dAddr >= 0xB4 && dAddr < 0xB7) {
            // PAN/AMS/FMS
            int ch = (dAddr & 0x3);
            if (ch != 3) {
                ch += dPort * 3;

                SetDelta(ch, midi2612, vgmFrameCounter);

                midi2612.data[ch].add((byte) (0xb0 | ch)); // PAN
                midi2612.data[ch].add((byte) (10));
                int pan = (dData & 0xc0) >> 6;
                midi2612.data[ch].add((byte) (pan == 0 ? 64 : ((pan == 1) ? 127 : ((pan == 2) ? 1 : 64))));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // AMS
                midi2612.data[ch].add((byte) (76));
                midi2612.data[ch].add((byte) ((dData & 0x38) >> 3));

                midi2612.data[ch].add((byte) 0); // Delta 0

                midi2612.data[ch].add((byte) (0xb0 | ch)); // FMS
                midi2612.data[ch].add((byte) (75));
                midi2612.data[ch].add((byte) ((dData & 0x03) >> 0));
            }
        }
    }

    private static int searchFMNote(int freq) {
        int m = Integer.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 5; i++) {
            int a = Math.abs(freq - Tables.FmFNum[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n - 12 * 3;
    }

    private static int searchOPMNote(int freq) {
        int note = freq & 0xf;
        note = (note < 3) ? note : (note < 7 ? note - 1 : (note < 11 ? note - 2 : note - 3));

        return note;
    }

    private void SetDelta(int ch, MidiChip chip, long NewFrameCounter) {
        if (ch >= chip.oldFrameCounter.length) return;

        long sub = NewFrameCounter - chip.oldFrameCounter[ch];
        long step = (long) (sub / (double) setting.getOutputDevice().getSampleRate() * 960.0);
        chip.oldFrameCounter[ch] += (long) (step * (double) setting.getOutputDevice().getSampleRate() / 960.0);

        boolean flg = true;
        for (int i = 0; i < 4; i++) {
            byte d = (byte) ((step & (0x0fe00000 >> (7 * i))) >> (21 - 7 * i));
            if (flg && d == 0 && i < 3) continue;
            flg = false;
            d |= (byte) ((i != 3) ? 0x80 : 0x00);
            chip.data[ch].add(d);
        }
    }

    private void makeHeader() {
        cData = new ArrayList<>();

        cData.add((byte) 0x4d); // chunk type 'MThd'
        cData.add((byte) 0x54);
        cData.add((byte) 0x68);
        cData.add((byte) 0x64);

        cData.add((byte) 0x00); // data length
        cData.add((byte) 0x00);
        cData.add((byte) 0x00);
        cData.add((byte) 0x06);

        cData.add((byte) 0x00); // format
        cData.add((byte) 0x01);

        cData.add((byte) 0x00); // number of tracks
        cData.add((byte) 0x01);

        cData.add((byte) 0x01); // resolution
        cData.add((byte) 0xe0);

        cData.add((byte) 0x4d); // chunk type 'MTrk'
        cData.add((byte) 0x54);
        cData.add((byte) 0x72);
        cData.add((byte) 0x6b);

        cData.add((byte) 0x00); // data length 0x17
        cData.add((byte) 0x00);
        cData.add((byte) 0x00);
        cData.add((byte) 0x17);

        cData.add((byte) 0x00); // Delta 0
        cData.add((byte) 0xff); // meta event
        cData.add((byte) 0x03);
        cData.add((byte) 0x00);

        cData.add((byte) 0x00); // Delta 0
        cData.add((byte) 0xff); // meta event beat 4/4(fixed)
        cData.add((byte) 0x58);
        cData.add((byte) 0x04);
        cData.add((byte) 0x04);
        cData.add((byte) 0x02);
        cData.add((byte) 0x18);
        cData.add((byte) 0x08);

        cData.add((byte) 0x00); // Delta 0
        cData.add((byte) 0xff); // meta event tempo setting BPM = 120(fixed)
        cData.add((byte) 0x51);
        cData.add((byte) 0x03);
        cData.add((byte) 0x07);
        cData.add((byte) 0xa1);
        cData.add((byte) 0x20);

        cData.add((byte) 0x00); // Delta 0
        cData.add((byte) 0xff); // meta event Termination
        cData.add((byte) 0x2f);
        cData.add((byte) 0x00);

       // real Track
        if (setting.getMidiExport().getUseYM2151Export()) InitYM2151();
        if (setting.getMidiExport().getUseYM2612Export()) InitYM2612();
    }

    private void InitYM2151() {
        midi2151 = new MidiChip();
        midi2151.maxTrk = 8;
        midi2151.oldCode = new int[midi2151.maxTrk];
        midi2151.oldFreq = new int[midi2151.maxTrk];
        midi2151.data = new ArrayList[midi2151.maxTrk];
        midi2151.oldFrameCounter = new long[midi2151.maxTrk];

        for (int i = 0; i < midi2151.maxTrk; i++) {
            midi2151.oldCode[i] = -1;
            midi2151.oldFreq[i] = -1;
            midi2151.data[i] = new ArrayList<>();
            midi2151.oldFrameCounter[i] = 0L;
        }

        for (int i = 0; i < midi2151.maxTrk; i++) {
            midi2151.data[i].add((byte) 0x4d); // chunk type 'MTrk'
            midi2151.data[i].add((byte) 0x54);
            midi2151.data[i].add((byte) 0x72);
            midi2151.data[i].add((byte) 0x6b);

            midi2151.data[i].add((byte) 0x00); // data length: At this point, it is unknown, so for now it is 0.
            midi2151.data[i].add((byte) 0x00);
            midi2151.data[i].add((byte) 0x00);
            midi2151.data[i].add((byte) 0x00);

            midi2151.data[i].add((byte) 0x00); // delta0
            midi2151.data[i].add((byte) 0xff); // meta event Port Designation
            midi2151.data[i].add((byte) 0x21);
            midi2151.data[i].add((byte) 0x01);
            midi2151.data[i].add((byte) 0x00); // Port1

            midi2151.data[i].add((byte) 0x00); // delta0
            midi2151.data[i].add((byte) 0xff); // meta event Track name
            midi2151.data[i].add((byte) 0x03);
            midi2151.data[i].add((byte) 0x00);
        }

        if (!setting.getMidiExport().getUseVOPMex()) return;

       // for VOPMex

        for (int i = 0; i < midi2151.maxTrk; i++) {
            // Changed the behavior of tone control (all MIDI channels)
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 121 127
            midi2151.data[i].add((byte) 121);
            midi2151.data[i].add((byte) 127);
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 126 127
            midi2151.data[i].add((byte) 126);
            midi2151.data[i].add((byte) 127);
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 123 127
            midi2151.data[i].add((byte) 123);
            midi2151.data[i].add((byte) 127);
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 98 127
            midi2151.data[i].add((byte) 98);
            midi2151.data[i].add((byte) 127);
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 99 126
            midi2151.data[i].add((byte) 99);
            midi2151.data[i].add((byte) 126);
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 6 127
            midi2151.data[i].add((byte) 6);
            midi2151.data[i].add((byte) 127);
            midi2151.data[i].add((byte) 0x50); // Delta 0
            midi2151.data[i].add((byte) (0xb0 + i)); // CC 93 120
            midi2151.data[i].add((byte) 93);
            midi2151.data[i].add((byte) 120);
        }
    }

    private void InitYM2612() {
        midi2612 = new MidiChip();
        midi2612.maxTrk = 6;
        midi2612.oldCode = new int[midi2612.maxTrk];
        midi2612.oldFreq = new int[midi2612.maxTrk];
        midi2612.data = new ArrayList[midi2612.maxTrk];
        midi2612.oldFrameCounter = new long[midi2612.maxTrk];

        for (int i = 0; i < midi2612.maxTrk; i++) {
            midi2612.oldCode[i] = -1;
            midi2612.oldFreq[i] = -1;
            midi2612.data[i] = new ArrayList<>();
            midi2612.oldFrameCounter[i] = 0L;
        }

        for (int i = 0; i < midi2612.maxTrk; i++) {
            midi2612.data[i].add((byte) 0x4d); // chunk type 'MTrk'
            midi2612.data[i].add((byte) 0x54);
            midi2612.data[i].add((byte) 0x72);
            midi2612.data[i].add((byte) 0x6b);

            midi2612.data[i].add((byte) 0x00); // data length: At this point, it is unknown, so for now it is 0.
            midi2612.data[i].add((byte) 0x00);
            midi2612.data[i].add((byte) 0x00);
            midi2612.data[i].add((byte) 0x00);

            midi2612.data[i].add((byte) 0x00); // delta0
            midi2612.data[i].add((byte) 0xff); // meta event Port Designation
            midi2612.data[i].add((byte) 0x21);
            midi2612.data[i].add((byte) 0x01);
            midi2612.data[i].add((byte) 0x00); // Port1

            midi2612.data[i].add((byte) 0x00); // delta0
            midi2612.data[i].add((byte) 0xff); // meta event  Track name
            midi2612.data[i].add((byte) 0x03);
            midi2612.data[i].add((byte) 0x00);

        }

        if (!setting.getMidiExport().getUseVOPMex()) return;

        // for VOPMex

        for (int i = 0; i < midi2612.maxTrk; i++) {
            // Changed the behavior of tone control (all MIDI channels)
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 121 127
            midi2612.data[i].add((byte) 121);
            midi2612.data[i].add((byte) 127);
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 126 127
            midi2612.data[i].add((byte) 126);
            midi2612.data[i].add((byte) 127);
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 123 127
            midi2612.data[i].add((byte) 123);
            midi2612.data[i].add((byte) 127);
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 98 127
            midi2612.data[i].add((byte) 98);
            midi2612.data[i].add((byte) 127);
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 99 126
            midi2612.data[i].add((byte) 99);
            midi2612.data[i].add((byte) 126);
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 6 127
            midi2612.data[i].add((byte) 6);
            midi2612.data[i].add((byte) 127);
            midi2612.data[i].add((byte) 0x50); // Delta 0
            midi2612.data[i].add((byte) (0xb0 + i)); // CC 93 120
            midi2612.data[i].add((byte) 93);
            midi2612.data[i].add((byte) 120);
        }
    }

    static class MidiChip {
        public List<Byte>[] data = null;
        public long[] oldFrameCounter = null;
        public int[] oldCode = null;
        public int[] oldFreq = null;
        public int maxTrk = 0;
    }
}
