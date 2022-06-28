package mdplayer;

import mdplayer.Common.EnmModel;


public class DacControl {

    private static final byte DCTRL_LMODE_IGNORE = 0x00;
    private static final byte DCTRL_LMODE_CMDS = 0x01;
    private static final byte DCTRL_LMODE_MSEC = 0x02;
    private static final byte DCTRL_LMODE_TOEND = 0x03;
    public static final byte DCTRL_LMODE_BYTES = 0x0F;
    private static final int DAC_SMPL_RATE = 44100;//DAC control独自のサンプルレートです(Fixed)

    private static final int MAX_CHIPS = 0xFF;
    private DacControl_[] DACData = new DacControl_[MAX_CHIPS];
    public ChipRegister chipRegister = null;
    public EnmModel model = EnmModel.VirtualModel;
    private Setting setting;

    public DacControl(Setting setting) {
        this.setting = setting;
    }

    public void sendCommand(DacControl_ chip) {
        byte Port;
        byte Command;
        byte Data;

        if ((chip.running & 0x10) != 0)   // command already sent
            return;
        if (chip.dataStart + chip.realPos >= chip.dataLen)
            return;

        //if (! chip->Reverse)
        //ChipData00 = chip.Data[(chip.DataStart + chip.RealPos)];
        //ChipData01 = chip.Data[(chip.DataStart + chip.RealPos+1)];
        //else
        //	ChipData = chip->Data + (chip->DataStart + chip->CmdsToSend - 1 - chip->Pos);
        switch (chip.DstChipType2) {
        // Support for the important chips
        case 0x02:  // Ym2612 (16-bit Register (actually 9 Bit), 8-bit Data)
            Port = (byte) ((chip.DstCommand & 0xFF00) >> 8);
            Command = (byte) ((chip.DstCommand & 0x00FF) >> 0);
            Data = chip.Data[(chip.dataStart + chip.realPos)];
            //if (model == enmModel.RealModel) Log.Write(String.format("%x %x", Data, chip.RealPos));

            chip_reg_write(chip.DstChipType2, chip.DstChipID, Port, Command, Data);
            break;
        case 0x11:  // PWM (4-bit Register, 12-bit Data)
            Port = (byte) ((chip.DstCommand & 0x000F) >> 0);
            Command = (byte) (chip.Data[chip.dataStart + chip.realPos + 1] & 0x0F);
            Data = chip.Data[chip.dataStart + chip.realPos];
            chip_reg_write(chip.DstChipType2, chip.DstChipID, Port, Command, Data);
            break;
        // Support for other chips (mainly for completeness)
        case 0x00:  // SN76496 (4-bit Register, 4-bit/10-bit Data)
            Command = (byte) ((chip.DstCommand & 0x00F0) >> 0);
            Data = (byte) (chip.Data[chip.dataStart + chip.realPos] & 0x0F);
            if ((Command & 0x10) != 0) {
                // Volume Change (4-Bit value)
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) 0x00, (byte) (Command | Data));
            } else {
                // Frequency Write (10-Bit value)
                Port = (byte) (((chip.Data[chip.dataStart + chip.realPos + 1] & 0x03) << 4) | ((chip.Data[chip.dataStart + chip.realPos] & 0xF0) >> 4));
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) 0x00, (byte) (Command | Data));
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) 0x00, Port);
            }
            break;
        case 0x18:  // OKIM6295 - TODO: verify
            Command = (byte) ((chip.DstCommand & 0x00FF) >> 0);
            Data = chip.Data[chip.dataStart + chip.realPos];

            if (Command == 0) {
                Port = (byte) ((chip.DstCommand & 0x0F00) >> 8);
                if ((Data & 0x80) > 0) {
                    // Sample Start
                    // write sample ID
                    chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, Command, Data);
                    // write channel(s) that should play the sample
                    chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, Command, (byte) (Port << 4));
                } else {
                    // Sample Stop
                    chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, Command, (byte) (Port << 3));
                }
            } else {
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, Command, Data);
            }
            break;
        // Generic support: 8-bit Register, 8-bit Data
        case 0x01:  // YM2413
        case 0x03:  // YM2151
        case 0x06:  // YM2203
        case 0x09:  // YM3812
        case 0x0A:  // YM3526
        case 0x0B:  // Y8950
        case 0x0F:  // YMZ280B
        case 0x12:  // AY8910
        case 0x13:  // GameBoy DMG
        case 0x14:  // NES APU
            //	case 0x15:	// MultiPCM
        case 0x16:  // UPD7759
        case 0x17:  // OKIM6258
        case 0x1D:  // K053260 - TODO: Verify
        case 0x1E:  // Pokey - TODO: Verify
            Command = (byte) ((chip.DstCommand & 0x00FF) >> 0);
            Data = chip.Data[chip.dataStart + chip.realPos];
            chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, Command, Data);
            break;
        // Generic support: 16-bit Register, 8-bit Data
        case 0x07:  // YM2608
        case 0x08:  // YM2610/B
        case 0x0C:  // YMF262
        case 0x0D:  // YMF278B
        case 0x0E:  // YMF271
        case 0x19:  // K051649 - TODO: Verify
        case 0x1A:  // K054539 - TODO: Verify
        case 0x1C:  // C140 - TODO: Verify
            Port = (byte) ((chip.DstCommand & 0xFF00) >> 8);
            Command = (byte) ((chip.DstCommand & 0x00FF) >> 0);
            Data = chip.Data[chip.dataStart + chip.realPos];
            chip_reg_write(chip.DstChipType2, chip.DstChipID, Port, Command, Data);
            break;
        // Generic support: 8-bit Register with Channel Select, 8-bit Data
        case 0x05:  // RF5C68
        case 0x10:  // RF5C164
        case 0x1B:  // HuC6280
            Port = (byte) ((chip.DstCommand & 0xFF00) >> 8);
            Command = (byte) ((chip.DstCommand & 0x00FF) >> 0);
            Data = chip.Data[chip.dataStart + chip.realPos];

            if (Port == 0xFF)   // Send Channel Select
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) (Command & 0x0f), Data);
            else {
                byte prevChn;

                prevChn = Port; // by default don't restore channel
                // get current channel for supported chips
                if (chip.DstChipType2 == 0x05) {
                }   // TODO
                else if (chip.DstChipType2 == 0x05) {
                }   // TODO
                else if (chip.DstChipType2 == 0x1B)
                    prevChn = chipRegister.ReadHuC6280Register(chip.DstChipID, (byte) 0x00, model);

                // Send Channel Select
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) (Command >> 4), Port);
                // Send Data
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) (Command & 0x0F), Data);
                // restore old channel
                if (prevChn != Port)
                    chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) (Command >> 4), prevChn);

                // Send Data
                chip_reg_write(chip.DstChipType2, chip.DstChipID, (byte) 0x00, (byte) (Command & 0x0F), Data);
            }
            break;
        // Generic support: 8-bit Register, 16-bit Data
        case 0x1F:  // QSound
            Command = (byte) ((chip.DstCommand & 0x00FF) >> 0);
            chip_reg_write(chip.DstChipType2, chip.DstChipID, chip.Data[chip.dataStart + chip.realPos], chip.Data[chip.dataStart + chip.realPos + 1], Command);
            break;
        }
        chip.running |= 0x10;

    }

    private int muldiv64round(int Multiplicand, int Multiplier, int Divisor) {
        // Yes, I'm correctly rounding the values.
        return (int) (((long) Multiplicand * Multiplier + Divisor / 2) / Divisor);
    }

    public void update(byte ChipID, int samples) {
// #if DEBUG
        if (model != EnmModel.VirtualModel) return;
// #endif
        DacControl_ chip = DACData[ChipID];
        int NewPos;
        int RealDataStp;

        //System.System.err.println("DAC update ChipID%d samples%d chip.Running%d ", ChipID, samples, chip.Running);
        if ((chip.running & 0x80) != 0)   // disabled
            return;
        if ((chip.running & 0x01) == 0)    // stopped
            return;

        if (chip.reverse == 0)
            RealDataStp = chip.dataStep;
        else
            RealDataStp = -chip.dataStep;

        if (samples > 0x20) {
            // very effective Speed Hack for fast seeking
            NewPos = chip.step + (samples - 0x10);
            NewPos = muldiv64round(NewPos * chip.dataStep, chip.frequency, DAC_SMPL_RATE);// (int)setting.getoutputDevice().SampleRate);
            while (chip.remainCmds != 0 && chip.pos < NewPos) {
                chip.pos += chip.dataStep;
                chip.realPos = chip.realPos + RealDataStp;
                chip.remainCmds--;
            }
        }

        chip.step += samples;
        // Formula: Step * Freq / SampleRate
        NewPos = muldiv64round(chip.step * chip.dataStep, chip.frequency, DAC_SMPL_RATE);// (int)setting.getoutputDevice().SampleRate);
        //System.System.err.printf("NewPos%d chip.Step%d chip.DataStep%d chip.Frequency%d DAC_SMPL_RATE%d \n", NewPos, chip.Step, chip.DataStep, chip.Frequency, (int)setting.getoutputDevice().SampleRate);
        sendCommand(chip);

        while (chip.remainCmds != 0 && chip.pos < NewPos) {
            sendCommand(chip);
            chip.pos += chip.dataStep;
            //if(model== enmModel.RealModel)                Log.Write(String.format("datastep:%d",chip.DataStep));
            chip.realPos = chip.realPos + RealDataStp;
            chip.running &= 0xef;// ~0x10;
            chip.remainCmds--;
        }

        if (chip.remainCmds == 0 && ((chip.running & 0x04) != 0)) {
            // loop back to start
            chip.remainCmds = chip.cmdsToSend;
            chip.step = 0x00;
            chip.pos = 0x00;
            if (chip.reverse == 0)
                chip.realPos = 0x00;
            else
                chip.realPos = (chip.cmdsToSend - 0x01) * chip.dataStep;
        }

        if (chip.remainCmds == 0)
            chip.running &= 0xfe;// ~0x01; // stop

    }

    public byte device_start_daccontrol(byte ChipID) {
        DacControl_ chip;

        if (ChipID >= MAX_CHIPS)
            return 0;

        chip = DACData[ChipID];

        chip.DstChipType2 = (byte) 0xFF;
        chip.DstChipID = 0x00;
        chip.DstCommand = 0x0000;

        chip.running = (byte) 0xFF;   // disable all actions (except setup_chip)

        return 1;
    }

    public void device_stop_daccontrol(byte ChipID) {
        DacControl_ chip = DACData[ChipID];

        chip.running = (byte) 0xFF;

    }

    public void device_reset_daccontrol(byte ChipID) {
        DacControl_ chip = DACData[ChipID];

        chip.DstChipType2 = 0x00;
        chip.DstChipID = 0x00;
        chip.DstCommand = 0x00;
        chip.CmdSize = 0x00;

        chip.frequency = 0;
        chip.dataLen = 0x00;
        chip.Data = null;
        chip.dataStart = 0x00;
        chip.StepSize = 0x00;
        chip.StepBase = 0x00;

        chip.running = 0x00;
        chip.reverse = 0x00;
        chip.step = 0x00;
        chip.pos = 0x00;
        chip.realPos = 0x00;
        chip.remainCmds = 0x00;
        chip.dataStep = 0x00;

    }

    public void setup_chip(byte ChipID, byte ChType, byte ChNum, int Command) {
        DacControl_ chip = DACData[ChipID];

        chip.DstChipType2 = ChType; // TypeID (e.g. 0x02 for Ym2612)
        chip.DstChipID = ChNum;    // chip number (to send commands to 1st or 2nd chip)
        chip.DstCommand = Command; // Port and Command (would be 0x02A for Ym2612)

        switch (chip.DstChipType2) {
        case 0x00:  // SN76496
            if ((chip.DstCommand & 0x0010) > 0)
                chip.CmdSize = 0x01;   // Volume Write
            else
                chip.CmdSize = 0x02;   // Frequency Write
            break;
        case 0x02:  // Ym2612
            chip.CmdSize = 0x01;
            break;
        case 0x11:  // PWM
        case 0x1F:  // QSound
            chip.CmdSize = 0x02;
            break;
        default:
            chip.CmdSize = 0x01;
            break;
        }
        chip.dataStep = (byte) (chip.CmdSize * chip.StepSize);

    }

    public void set_data(byte ChipID, byte[] Data, int DataLen, byte StepSize, byte StepBase) {
        DacControl_ chip = DACData[ChipID];

        if ((chip.running & 0x80) > 0)
            return;

        if (DataLen > 0 && Data != null) {
            chip.dataLen = DataLen;
            chip.Data = Data;
        } else {
            chip.dataLen = 0x00;
            chip.Data = null;
        }
        chip.StepSize = (byte) (StepSize > 0 ? StepSize : 1);
        chip.StepBase = StepBase;
        chip.dataStep = (byte) (chip.CmdSize * chip.StepSize);

    }

    public void refresh_data(byte ChipID, byte[] Data, int DataLen) {
        // Should be called to fix the data pointer. (e.g. after a realloc)
        DacControl_ chip = DACData[ChipID];

        if ((chip.running & 0x80) != 0)
            return;

        if (DataLen > 0 && Data != null) {
            chip.dataLen = DataLen;
            chip.Data = Data;
        } else {
            chip.dataLen = 0x00;
            chip.Data = null;
        }

    }

    public void set_frequency(byte ChipID, int frequency) {
        //System.System.err.println("ChipID%d frequency%d", ChipID, frequency);
        DacControl_ chip = DACData[ChipID];

        if ((chip.running & 0x80) != 0)
            return;

        if (frequency != 0)
            chip.step = chip.step * chip.frequency / frequency;
        chip.frequency = frequency;
    }

    public void start(byte ChipID, int dataPos, byte lenMode, int Length) {
        DacControl_ chip = DACData[ChipID];
        int cmdStepBase;

        if ((chip.running & 0x80) != 0)
            return;

        cmdStepBase = chip.CmdSize * chip.StepBase;
        if (dataPos != 0xFFFFFFFF) { // skip setting DataStart, if Pos == -1
            chip.dataStart = dataPos + cmdStepBase;
            if (chip.dataStart > chip.dataLen) // catch bad value and force silence
                chip.dataStart = chip.dataLen;
        }

        switch (lenMode & 0x0F) {
        case DCTRL_LMODE_IGNORE: // Length instanceof already set - ignore
            break;
        case DCTRL_LMODE_CMDS: // Length = number of commands
            chip.cmdsToSend = Length;
            break;
        case DCTRL_LMODE_MSEC: // Length = time : msec
            chip.cmdsToSend = 1000 * Length / chip.frequency;
            break;
        case DCTRL_LMODE_TOEND: // play unti stop-command instanceof received (or data-end instanceof reached)
            chip.cmdsToSend = (chip.dataLen - (chip.dataStart - cmdStepBase)) / chip.dataStep;
            break;
        case DCTRL_LMODE_BYTES: // raw byte count
            chip.cmdsToSend = Length / chip.dataStep;
            break;
        default:
            chip.cmdsToSend = 0x00;
            break;
        }
        chip.reverse = (byte) ((lenMode & 0x10) >> 4);

        chip.remainCmds = chip.cmdsToSend;
        chip.step = 0x00;
        chip.pos = 0x00;
        if (chip.reverse == 0)
            chip.realPos = 0x00;
        else
            chip.realPos = (chip.cmdsToSend - 0x01) * chip.dataStep;

        chip.running &= 0xfb;// ~0x04;
        chip.running |= (byte) ((lenMode & 0x80) != 0 ? 0x04 : 0x00);    // set loop mode

        chip.running |= 0x01;  // start
        chip.running &= 0xef;// ~0x10; // command isn't yet sent

    }

    public void stop(byte ChipID) {
        DacControl_ chip = DACData[ChipID];

        if ((chip.running & 0x80) != 0)
            return;

        chip.running &= 0xfe;// ~0x01; // stop

    }

    private void chip_reg_write(byte ChipType2, byte ChipID, byte Port, byte Offset, byte Data) {
        switch (ChipType2) {
        case 0x00:  // SN76489
            chipRegister.setSN76489Register(ChipID, Data, model);
            break;
        case 0x01:  // YM2413+
            chipRegister.setYM2413Register(ChipID, Offset, Data, model);
            break;
        case 0x02:  // Ym2612
            chipRegister.setYM2612Register(ChipID, Port, Offset, Data, model, -1);
            break;
        case 0x03:  // YM2151+
            chipRegister.setYM2151Register(ChipID, Port, Offset, Data, model, 0, 0);
            break;
        case 0x06:  // YM2203+
            chipRegister.setYM2203Register(ChipID, Offset, Data, model);
            break;
        case 0x07:  // YM2608+
            chipRegister.setYM2608Register(ChipID, Port, Offset, Data, model);
            break;
        case 0x08:  // YM2610+
            chipRegister.setYM2610Register(ChipID, Port, Offset, Data, model);
            break;
        case 0x09:  // YM3812+
            chipRegister.setYM3812Register(ChipID, Offset, Data, model);
            break;
        case 0x0A:  // YM3526+
            chipRegister.setYM3526Register(ChipID, Offset, Data, model);
            break;
        case 0x0B:  // Y8950+
            chipRegister.setY8950Register(ChipID, Offset, Data, model);
            break;
        case 0x0C:  // YMF262+
            chipRegister.setYMF262Register(ChipID, Port, Offset, Data, model);
            break;
        case 0x0D:  // YMF278B+
            chipRegister.setYMF278BRegister(ChipID, Port, Offset, Data, model);
            break;
        case 0x0E:  // YMF271+
            chipRegister.setYMF271Register(ChipID, Port, Offset, Data, model);
            break;
        case 0x0F:  // YMZ280B+
            chipRegister.setYMZ280BRegister(ChipID, Offset, Data, model);
            break;
        case 0x10:
            chipRegister.writeRF5C164(ChipID, Offset, Data, model);
            break;
        case 0x11:  // PWM
            chipRegister.writePWM(ChipID, Port, (Offset << 8) | (Data << 0), model);
            break;
        case 0x12:  // AY8910+
            chipRegister.setAY8910Register(ChipID, Offset, Data, model);
            break;
        case 0x13:  // DMG+
            chipRegister.setDMGRegister(ChipID, Offset, Data, model);
            break;
        case 0x14:  // NES+
            chipRegister.setNESRegister(ChipID, Offset, Data, model);
            break;
        case 0x17:  // OKIM6258
            if (model == EnmModel.VirtualModel) //System.System.err.printf("[DAC]");
                chipRegister.writeOKIM6258(ChipID, Offset, Data, model);
            break;
        case 0x1b:  // HuC6280
            chipRegister.setHuC6280Register(ChipID, Offset, Data, model);
            break;
        }
    }

    public void refresh() {
        for (int i = 0; i < MAX_CHIPS; i++) DACData[i] = new DacControl_();
    }

    public static class DacControl_ {
        // Commands sent to dest-chip
        public byte DstChipType2;
        public byte DstChipID;
        public int DstCommand;
        public byte CmdSize;

        public int frequency;   // Frequency (Hz) at which the commands are sent
        public int dataLen;     // to protect from reading beyond End Of Data
        public byte[] Data;
        public int dataStart;   // Position where to start
        public byte StepSize;     // usually 1, set to 2 for L/R interleaved data
        public byte StepBase;     // usually 0, set to 0/1 for L/R interleaved data
        public int cmdsToSend;

        // Running Bits:	0 (01) - instanceof playing
        //					2 (04) - loop sample (simple loop from start to end)
        //					4 (10) - already sent this command
        //					7 (80) - disabled
        public byte running;
        public byte reverse;
        public int step;        // Position : Player SampleRate
        public int pos;         // Position : Data SampleRate
        public int remainCmds;
        public int realPos;     // true Position : Data (== Pos, if Reverse instanceof off)
        public byte dataStep;     // always StepSize * CmdSize
    }
}
