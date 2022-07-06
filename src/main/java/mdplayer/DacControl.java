package mdplayer;

import mdplayer.Common.EnmModel;


public class DacControl {

    private static final int MAX_CHIPS = 0xFF;
    private DacControl_[] DACData = new DacControl_[MAX_CHIPS];
    private Setting setting;
    public EnmModel model = EnmModel.VirtualModel;
    public ChipRegister chipRegister = null;

    public DacControl(Setting setting) {
        this.setting = setting;
    }

    public void update(byte chipID, int samples) {
// #if DEBUG
        if (model != EnmModel.VirtualModel) return;
// #endif
        DacControl_ chip = DACData[chipID];
        chip.update(samples);
    }

    public byte device_start_daccontrol(byte chipID) {
        if (chipID >= MAX_CHIPS)
            return 0;

        DacControl_ chip = DACData[chipID];
        chip.startDacControl();

        return 1;
    }

    public void device_stop_daccontrol(byte chipID) {
        DacControl_ chip = DACData[chipID];
        chip.stopDacControl();
    }

    public void device_reset_daccontrol(byte chipID) {
        DacControl_ chip = DACData[chipID];
        chip.reset();
    }

    public void setupChip(byte chipID, byte chType, byte chNum, int command) {
        DacControl_ chip = DACData[chipID];
        chip.setup(chType, chNum, command);
    }

    public void setData(byte chipID, byte[] data, int dataLen, byte stepSize, byte stepBase) {
        DacControl_ chip = DACData[chipID];
        chip.setData(data, dataLen, stepSize, stepBase);
    }

    public void refresh_data(byte chipID, byte[] data, int dataLen) {
        // Should be called to fix the data pointer. (e.g. after a realloc)
        DacControl_ chip = DACData[chipID];
        chip.refreshData(data, dataLen);
    }

    public void set_frequency(byte chipID, int frequency) {
        //Debug.printf(("chipID%d frequency%d", chipID, frequency);
        DacControl_ chip = DACData[chipID];
        chip.setFrequency(chipID, frequency);
    }

    public void start(byte chipID, int dataPos, byte lenMode, int length) {
        DacControl_ chip = DACData[chipID];
        chip.start(dataPos, lenMode, length);
    }

    public void stop(byte ChipID) {
        DacControl_ chip = DACData[ChipID];
        chip.stop();
    }

    public void refresh() {
        for (int i = 0; i < MAX_CHIPS; i++) DACData[i] = new DacControl_();
    }

    private void writeChipReg(byte chipType2, byte chipId, byte port, byte offset, byte data) {
        switch (chipType2) {
        case 0x00: // SN76489
            chipRegister.setSN76489Register(chipId, data, model);
            break;
        case 0x01: // YM2413+
            chipRegister.setYM2413Register(chipId, offset, data, model);
            break;
        case 0x02: // Ym2612
            chipRegister.setYM2612Register(chipId, port, offset, data, model, -1);
            break;
        case 0x03: // YM2151+
            chipRegister.setYM2151Register(chipId, port, offset, data, model, 0, 0);
            break;
        case 0x06: // YM2203+
            chipRegister.setYM2203Register(chipId, offset, data, model);
            break;
        case 0x07: // YM2608+
            chipRegister.setYM2608Register(chipId, port, offset, data, model);
            break;
        case 0x08: // YM2610+
            chipRegister.setYM2610Register(chipId, port, offset, data, model);
            break;
        case 0x09: // YM3812+
            chipRegister.setYM3812Register(chipId, offset, data, model);
            break;
        case 0x0A: // YM3526+
            chipRegister.setYM3526Register(chipId, offset, data, model);
            break;
        case 0x0B: // Y8950+
            chipRegister.setY8950Register(chipId, offset, data, model);
            break;
        case 0x0C: // YMF262+
            chipRegister.setYMF262Register(chipId, port, offset, data, model);
            break;
        case 0x0D: // YMF278B+
            chipRegister.setYMF278BRegister(chipId, port, offset, data, model);
            break;
        case 0x0E: // YMF271+
            chipRegister.setYMF271Register(chipId, port, offset, data, model);
            break;
        case 0x0F: // YMZ280B+
            chipRegister.setYMZ280BRegister(chipId, offset, data, model);
            break;
        case 0x10:
            chipRegister.writeRF5C164(chipId, offset, data, model);
            break;
        case 0x11: // PWM
            chipRegister.writePWM(chipId, port, (offset << 8) | (data << 0), model);
            break;
        case 0x12: // AY8910+
            chipRegister.setAY8910Register(chipId, offset, data, model);
            break;
        case 0x13: // DMG+
            chipRegister.setDMGRegister(chipId, offset, data, model);
            break;
        case 0x14: // NES+
            chipRegister.setNESRegister(chipId, offset, data, model);
            break;
        case 0x17: // OKIM6258
            if (model == EnmModel.VirtualModel)  // Debug.printf("[DAC]");
                chipRegister.writeOKIM6258(chipId, offset, data, model);
            break;
        case 0x1b: // HuC6280
            chipRegister.setHuC6280Register(chipId, offset, data, model);
            break;
        }
    }

    public class DacControl_ {

        private static final byte DCTRL_LMODE_IGNORE = 0x00;
        private static final byte DCTRL_LMODE_CMDS = 0x01;
        private static final byte DCTRL_LMODE_MSEC = 0x02;
        private static final byte DCTRL_LMODE_TOEND = 0x03;
        public static final byte DCTRL_LMODE_BYTES = 0x0F;
        private static final int DAC_SMPL_RATE = 44100; // DAC control独自のサンプルレートです(Fixed)

        private static int mulDiv64Round(int multiplicand, int multiplier, int divisor) {
            // Yes, I'm correctly rounding the values.
            return (int) (((long) multiplicand * multiplier + divisor / 2) / divisor);
        }

        // Commands sent to dest-chip
        public byte dstChipType2;
        public byte dstChipID;
        public int dstCommand;
        public byte cmdSize;

        // Frequency (Hz) at which the commands are sent
        public int frequency;
        // to protect from reading beyond End Of Data
        public int dataLen;
        public byte[] Data;
        // Position where to start
        public int dataStart;
        // usually 1, set to 2 for L/R interleaved data
        public byte StepSize;
        // usually 0, set to 0/1 for L/R interleaved data
        public byte StepBase;
        public int cmdsToSend;

        // Running Bits:	0 (01) - instanceof playing
        //					2 (04) - loop sample (simple loop from start to end)
        //					4 (10) - already sent this command
        //					7 (80) - disabled
        public byte running;
        public byte reverse;
        // Position : Player SampleRate
        public int step;
        // Position : Data SampleRate
        public int pos;
        public int remainCmds;
        // true Position : Data (== Pos, if Reverse instanceof off)
        public int realPos;
        // always StepSize * cmdSize
        public byte dataStep;

        public void sendCommand() {
            byte port;
            byte command;
            byte data;

            if ((running & 0x10) != 0)   // command already sent
                return;
            if (dataStart + realPos >= dataLen)
                return;

            //if (! chip->Reverse)
            //ChipData00 = chip.data[(chip.DataStart + chip.RealPos)];
            //ChipData01 = chip.data[(chip.DataStart + chip.RealPos+1)];
            //else
            //	ChipData = chip->data + (chip->DataStart + chip->CmdsToSend - 1 - chip->Pos);
            switch (dstChipType2) {
            // Support for the important chips
            case 0x02: // Ym2612 (16-bit Register (actually 9 Bit), 8-bit data)
                port = (byte) ((dstCommand & 0xFF00) >> 8);
                command = (byte) ((dstCommand & 0x00FF) >> 0);
                data = this.Data[(dataStart + realPos)];
                //if (model == enmModel.RealModel) Debug.printf(String.format("%x %x", data, chip.RealPos));

                writeChipReg(dstChipType2, dstChipID, port, command, data);
                break;
            case 0x11: // PWM (4-bit Register, 12-bit data)
                port = (byte) ((dstCommand & 0x000F) >> 0);
                command = (byte) (this.Data[dataStart + realPos + 1] & 0x0F);
                data = this.Data[dataStart + realPos];
                writeChipReg(dstChipType2, dstChipID, port, command, data);
                break;
            // Support for other chips (mainly for completeness)
            case 0x00: // SN76496 (4-bit Register, 4-bit/10-bit data)
                command = (byte) ((dstCommand & 0x00F0) >> 0);
                data = (byte) (this.Data[dataStart + realPos] & 0x0F);
                if ((command & 0x10) != 0) {
                    // Volume Change (4-Bit value)
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) 0x00, (byte) (command | data));
                } else {
                    // Frequency Write (10-Bit value)
                    port = (byte) (((this.Data[dataStart + realPos + 1] & 0x03) << 4) | ((this.Data[dataStart + realPos] & 0xF0) >> 4));
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) 0x00, (byte) (command | data));
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) 0x00, port);
                }
                break;
            case 0x18: // OKIM6295 - TODO: verify
                command = (byte) ((dstCommand & 0x00FF) >> 0);
                data = this.Data[dataStart + realPos];

                if (command == 0) {
                    port = (byte) ((dstCommand & 0x0F00) >> 8);
                    if ((data & 0x80) > 0) {
                        // Sample Start
                        // write sample ID
                        writeChipReg(dstChipType2, dstChipID, (byte) 0x00, command, data);
                        // write channel(s) that should play the sample
                        writeChipReg(dstChipType2, dstChipID, (byte) 0x00, command, (byte) (port << 4));
                    } else {
                        // Sample Stop
                        writeChipReg(dstChipType2, dstChipID, (byte) 0x00, command, (byte) (port << 3));
                    }
                } else {
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, command, data);
                }
                break;
            // Generic support: 8-bit Register, 8-bit data
            case 0x01: // YM2413
            case 0x03: // YM2151
            case 0x06: // YM2203
            case 0x09: // YM3812
            case 0x0A: // YM3526
            case 0x0B: // Y8950
            case 0x0F: // YMZ280B
            case 0x12: // AY8910
            case 0x13: // GameBoy DMG
            case 0x14: // NES APU
    //    	case 0x15: // MultiPCM
            case 0x16: // UPD7759
            case 0x17: // OKIM6258
            case 0x1D: // K053260 - TODO: Verify
            case 0x1E: // Pokey - TODO: Verify
                command = (byte) ((dstCommand & 0x00FF) >> 0);
                data = this.Data[dataStart + realPos];
                writeChipReg(dstChipType2, dstChipID, (byte) 0x00, command, data);
                break;
            // Generic support: 16-bit Register, 8-bit data
            case 0x07: // YM2608
            case 0x08: // YM2610/B
            case 0x0C: // YMF262
            case 0x0D: // YMF278B
            case 0x0E: // YMF271
            case 0x19: // K051649 - TODO: Verify
            case 0x1A: // K054539 - TODO: Verify
            case 0x1C: // C140 - TODO: Verify
                port = (byte) ((dstCommand & 0xFF00) >> 8);
                command = (byte) ((dstCommand & 0x00FF) >> 0);
                data = this.Data[dataStart + realPos];
                writeChipReg(dstChipType2, dstChipID, port, command, data);
                break;
            // Generic support: 8-bit Register with Channel Select, 8-bit data
            case 0x05: // RF5C68
            case 0x10: // RF5C164
            case 0x1B: // HuC6280
                port = (byte) ((dstCommand & 0xFF00) >> 8);
                command = (byte) ((dstCommand & 0x00FF) >> 0);
                data = this.Data[dataStart + realPos];

                if (port == (byte) 0xFF) // Send Channel Select
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) (command & 0x0f), data);
                else {
                    byte prevChn;

                    prevChn = port; // by default don't restore channel
                    // get current channel for supported chips
                    if (dstChipType2 == 0x05) {
                    } // TODO
                    else if (dstChipType2 == 0x05) {
                    } // TODO
                    else if (dstChipType2 == 0x1B)
                        prevChn = chipRegister.ReadHuC6280Register(dstChipID, (byte) 0x00, model);

                    // Send Channel Select
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) (command >> 4), port);
                    // Send data
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) (command & 0x0F), data);
                    // restore old channel
                    if (prevChn != port)
                        writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) (command >> 4), prevChn);

                    // Send data
                    writeChipReg(dstChipType2, dstChipID, (byte) 0x00, (byte) (command & 0x0F), data);
                }
                break;
            // Generic support: 8-bit Register, 16-bit data
            case 0x1F: // QSound
                command = (byte) ((dstCommand & 0x00FF) >> 0);
                writeChipReg(dstChipType2, dstChipID, this.Data[dataStart + realPos], this.Data[dataStart + realPos + 1], command);
                break;
            }
            running |= 0x10;
        }

        public void update(int samples) {
            int newPos;
            int realDataStp;

            //Debug.printf(("DAC update chipID%d samples%d this.Running%d ", chipID, samples, this.Running);
            if ((this.running & 0x80) != 0) // disabled
                return;
            if ((this.running & 0x01) == 0) // stopped
                return;

            if (this.reverse == 0)
                realDataStp = this.dataStep;
            else
                realDataStp = -this.dataStep;

            if (samples > 0x20) {
                // very effective Speed Hack for fast seeking
                newPos = this.step + (samples - 0x10);
                newPos = mulDiv64Round(newPos * this.dataStep, this.frequency, DAC_SMPL_RATE);
                while (this.remainCmds != 0 && this.pos < newPos) {
                    this.pos += this.dataStep;
                    this.realPos = this.realPos + realDataStp;
                    this.remainCmds--;
                }
            }

            this.step += samples;
            // Formula: Step * Freq / SampleRate
            newPos = mulDiv64Round(this.step * this.dataStep, this.frequency, DAC_SMPL_RATE);
            //Debug.printf("newPos%d this.Step%d this.DataStep%d this.Frequency%d DAC_SMPL_RATE%d \n", newPos, this.Step, this.DataStep, this.Frequency, (int)setting.getoutputDevice().SampleRate);
            this.sendCommand();

            while (this.remainCmds != 0 && this.pos < newPos) {
                this.sendCommand();
                this.pos += this.dataStep;
                //if (model== enmModel.RealModel) Debug.printf(String.format("datastep:%d",this.DataStep));
                this.realPos = this.realPos + realDataStp;
                this.running &= 0xef;// ~0x10;
                this.remainCmds--;
            }

            if (this.remainCmds == 0 && ((this.running & 0x04) != 0)) {
                // loop back to start
                this.remainCmds = this.cmdsToSend;
                this.step = 0x00;
                this.pos = 0x00;
                if (this.reverse == 0)
                    this.realPos = 0x00;
                else
                    this.realPos = (this.cmdsToSend - 0x01) * this.dataStep;
            }

            if (this.remainCmds == 0)
                this.running &= 0xfe; // stop
        }

        public void startDacControl() {
            this.dstChipType2 = (byte) 0xFF;
            this.dstChipID = 0x00;
            this.dstCommand = 0x0000;

            this.running = (byte) 0xFF; // disable all actions (except setup_chip)
        }

        public void stopDacControl() {
            this.running = (byte) 0xFF;
        }

        public void reset() {
            this.dstChipType2 = 0x00;
            this.dstChipID = 0x00;
            this.dstCommand = 0x00;
            this.cmdSize = 0x00;

            this.frequency = 0;
            this.dataLen = 0x00;
            this.Data = null;
            this.dataStart = 0x00;
            this.StepSize = 0x00;
            this.StepBase = 0x00;

            this.running = 0x00;
            this.reverse = 0x00;
            this.step = 0x00;
            this.pos = 0x00;
            this.realPos = 0x00;
            this.remainCmds = 0x00;
            this.dataStep = 0x00;
        }

        public void setup(byte chType, byte chNum, int command) {
            this.dstChipType2 = chType; // TypeID (e.g. 0x02 for Ym2612)
            this.dstChipID = chNum; // chip number (to send commands to 1st or 2nd chip)
            this.dstCommand = command; // Port and command (would be 0x02A for Ym2612)

            switch (this.dstChipType2) {
            case 0x00: // SN76496
                if ((this.dstCommand & 0x0010) > 0)
                    this.cmdSize = 0x01; // Volume Write
                else
                    this.cmdSize = 0x02; // Frequency Write
                break;
            case 0x02: // Ym2612
                this.cmdSize = 0x01;
                break;
            case 0x11: // PWM
            case 0x1F: // QSound
                this.cmdSize = 0x02;
                break;
            default:
                this.cmdSize = 0x01;
                break;
            }
            this.dataStep = (byte) (this.cmdSize * this.StepSize);
        }

        public void setData(byte[] data, int dataLen, byte stepSize, byte stepBase) {
            if ((this.running & 0x80) > 0)
                return;

            if (dataLen > 0 && data != null) {
                this.dataLen = dataLen;
                this.Data = data;
            } else {
                this.dataLen = 0x00;
                this.Data = null;
            }
            this.StepSize = (byte) (stepSize > 0 ? stepSize : 1);
            this.StepBase = stepBase;
            this.dataStep = (byte) (this.cmdSize * this.StepSize);
        }

        public void refreshData(byte[] data, int dataLen) {
            if ((this.running & 0x80) != 0)
                return;

            if (dataLen > 0 && data != null) {
                this.dataLen = dataLen;
                this.Data = data;
            } else {
                this.dataLen = 0x00;
                this.Data = null;
            }
        }

        public void setFrequency(byte chipID, int frequency) {
            if ((this.running & 0x80) != 0)
                return;

            if (frequency != 0)
                this.step = this.step * this.frequency / frequency;
            this.frequency = frequency;
        }

        public void start(int dataPos, byte lenMode, int length) {
            int cmdStepBase;

            if ((this.running & 0x80) != 0)
                return;

            cmdStepBase = this.cmdSize * this.StepBase;
            if (dataPos != 0xFFFFFFFF) { // skip setting DataStart, if Pos == -1
                this.dataStart = dataPos + cmdStepBase;
                if (this.dataStart > this.dataLen) // catch bad value and force silence
                    this.dataStart = this.dataLen;
            }

            switch (lenMode & 0x0F) {
            case DCTRL_LMODE_IGNORE: // Length instanceof already set - ignore
                break;
            case DCTRL_LMODE_CMDS: // Length = number of commands
                this.cmdsToSend = length;
                break;
            case DCTRL_LMODE_MSEC: // Length = time : msec
                this.cmdsToSend = 1000 * length / this.frequency;
                break;
            case DCTRL_LMODE_TOEND: // play unti stop-command instanceof received (or data-end instanceof reached)
                this.cmdsToSend = (this.dataLen - (this.dataStart - cmdStepBase)) / this.dataStep;
                break;
            case DCTRL_LMODE_BYTES: // raw byte count
                this.cmdsToSend = length / this.dataStep;
                break;
            default:
                this.cmdsToSend = 0x00;
                break;
            }
            this.reverse = (byte) ((lenMode & 0x10) >> 4);

            this.remainCmds = this.cmdsToSend;
            this.step = 0x00;
            this.pos = 0x00;
            if (this.reverse == 0)
                this.realPos = 0x00;
            else
                this.realPos = (this.cmdsToSend - 0x01) * this.dataStep;

            this.running &= 0xfb;
            this.running |= (byte) ((lenMode & 0x80) != 0 ? 0x04 : 0x00); // set loop mode

            this.running |= 0x01; // start
            this.running &= 0xef; // command isn't yet sent
        }

        public void stop() {
            if ((this.running & 0x80) != 0)
                return;

            this.running &= 0xfe; // stop
        }
    }
}
