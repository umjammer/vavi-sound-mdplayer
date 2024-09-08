package mdplayer;

import mdplayer.Common.EnmModel;


public class DacControl {

    private static final int MAX_CHIPS = 0xff;
    private DacControl_[] DACData = new DacControl_[MAX_CHIPS];
    private Setting setting;
    public EnmModel model = EnmModel.VirtualModel;
    public ChipRegister chipRegister = null;

    public DacControl(Setting setting) {
        this.setting = setting;
    }

    public void update(int chipId, int samples) {
//#if DEBUG
        if (model != EnmModel.VirtualModel) return;
//#endif
        DacControl_ chip = DACData[chipId];
        chip.update(samples);
    }

    public int device_start_daccontrol(int chipId) {
        if ((chipId & 0xff) >= MAX_CHIPS)
            return 0;

        DacControl_ chip = DACData[chipId];
        chip.startDacControl();

        return 1;
    }

    public void device_stop_daccontrol(int chipId) {
        DacControl_ chip = DACData[chipId];
        chip.stopDacControl();
    }

    public void device_reset_daccontrol(int chipId) {
        DacControl_ chip = DACData[chipId];
        chip.reset();
    }

    public void setupChip(int chipId, int chType, int chNum, int command) {
        DacControl_ chip = DACData[chipId];
        chip.setup(chType, chNum, command);
    }

    public void setData(int chipId, byte[] data, int dataLen, int stepSize, int stepBase) {
        DacControl_ chip = DACData[chipId];
        chip.setData(data, dataLen, stepSize, stepBase);
    }

    public void refresh_data(int chipId, byte[] data, int dataLen) {
        // Should be called to fix the data pointer. (e.g. after a realloc)
        DacControl_ chip = DACData[chipId];
        chip.refreshData(data, dataLen);
    }

    public void set_frequency(int chipId, int frequency) {
        //Debug.printf(("chipId%d frequency%d", chipId, frequency);
        DacControl_ chip = DACData[chipId];
        chip.setFrequency(chipId, frequency);
    }

    public void start(int chipId, int dataPos, int lenMode, int length) {
        DacControl_ chip = DACData[chipId];
        chip.start(dataPos, lenMode, length);
    }

    public void stop(int chipId) {
        DacControl_ chip = DACData[chipId];
        chip.stop();
    }

    public void refresh() {
        for (int i = 0; i < MAX_CHIPS; i++) DACData[i] = new DacControl_();
    }

    private void writeChipReg(int chipType2, int chipId, int port, int offset, int data) {
        switch (chipType2) {
        case 0x00: // SN76489
            chipRegister.setSN76489Register(chipId, data, model);
            break;
        case 0x01: // YM2413+
            chipRegister.setYM2413Register(chipId, offset, data, model);
            break;
        case 0x02: // Ym2612Inst
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
        case 0x0B: // Y8950Inst+
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
        case 0x1b: // OotakeHuC6280
            chipRegister.setHuC6280Register(chipId, offset, data, model);
            break;
        }
    }

    public class DacControl_ {

        private static final int DCTRL_LMODE_IGNORE = 0x00;
        private static final int DCTRL_LMODE_CMDS = 0x01;
        private static final int DCTRL_LMODE_MSEC = 0x02;
        private static final int DCTRL_LMODE_TOEND = 0x03;
        public static final int DCTRL_LMODE_BYTES = 0x0F;
        private static final int DAC_SMPL_RATE = 44100; // DAC control独自のサンプルレートです(Fixed)

        private static int mulDiv64Round(int multiplicand, int multiplier, int divisor) {
            // Yes, I'm correctly rounding the values.
            return (multiplicand * multiplier + divisor / 2) / divisor;
        }

        // Commands sent to dest-chips
        public int dstChipType2;
        public int dstChipID;
        public int dstCommand;
        public int cmdSize;

        // Frequency (Hz) at which the commands are sent
        public int frequency;
        // to protect from reading beyond End Of data
        public int dataLen;
        public byte[] data;
        // Position where to start
        public int dataStart;
        // usually 1, set to 2 for L/R interleaved data
        public int stepSize;
        // usually 0, set to 0/1 for L/R interleaved data
        public int stepBase;
        public int cmdsToSend;

        // Running Bits:	0 (01) - instanceof playing
        //					2 (04) - loop sample (simple loop from start to end)
        //					4 (10) - already sent this command
        //					7 (80) - disabled
        public int running;
        public int reverse;
        // Position : Player SampleRate
        public int step;
        // Position : data SampleRate
        public int pos;
        public int remainCmds;
        // true Position : data (== Pos, if Reverse instanceof off)
        public int realPos;
        // always stepSize * cmdSize
        public int dataStep;

        public void sendCommand() {
            int port;
            int command;
            int data;

            if ((running & 0x10) != 0)   // command already sent
                return;
            if (dataStart + realPos >= dataLen)
                return;

//            if (!chips -> Reverse) {
//                ChipData00 = chips.data[(chips.DataStart + chips.RealPos)];
//                ChipData01 = chips.data[(chips.DataStart + chips.RealPos + 1)];
//            } else
//                ChipData = chips -> data + (chips -> dataStart + chips -> CmdsToSend - 1 - chips -> Pos);
            switch (dstChipType2) {
            // Support for the important chips
            case 0x02: // Ym2612Inst (16-bit Register (actually 9 Bit), 8-bit data)
                port = (dstCommand & 0xff00) >> 8;
                command = dstCommand & 0x00FF;
                data = this.data[(dataStart + realPos)];
                //if (model == enmModel.RealModel) Debug.printf(String.format("%x %x", data, chips.RealPos));

                writeChipReg(dstChipType2, dstChipID, port, command, data);
                break;
            case 0x11: // PWM (4-bit Register, 12-bit data)
                port = dstCommand & 0x000F;
                command = this.data[dataStart + realPos + 1] & 0x0F;
                data = this.data[dataStart + realPos];
                writeChipReg(dstChipType2, dstChipID, port, command, data);
                break;
            // Support for other chips (mainly for completeness)
            case 0x00: // SN76496 (4-bit Register, 4-bit/10-bit data)
                command = dstCommand & 0x00F0;
                data = this.data[dataStart + realPos] & 0x0F;
                if ((command & 0x10) != 0) {
                    // Volume Change (4-Bit value)
                    writeChipReg(dstChipType2, dstChipID, 0x00, 0x00, command | data);
                } else {
                    // Frequency Write (10-Bit value)
                    port = ((this.data[dataStart + realPos + 1] & 0x03) << 4) | ((this.data[dataStart + realPos] & 0xF0) >> 4);
                    writeChipReg(dstChipType2, dstChipID, 0x00, 0x00, command | data);
                    writeChipReg(dstChipType2, dstChipID, 0x00, 0x00, port);
                }
                break;
            case 0x18: // OKIM6295 - TODO: verify
                command = dstCommand & 0x00FF;
                data = this.data[dataStart + realPos];

                if (command == 0) {
                    port = (byte) ((dstCommand & 0x0F00) >> 8);
                    if ((data & 0x80) > 0) {
                        // Sample Start
                        // write sample ID
                        writeChipReg(dstChipType2, dstChipID, 0x00, command, data);
                        // write channel(s) that should play the sample
                        writeChipReg(dstChipType2, dstChipID, 0x00, command, port << 4);
                    } else {
                        // Sample Stop
                        writeChipReg(dstChipType2, dstChipID, 0x00, command, port << 3);
                    }
                } else {
                    writeChipReg(dstChipType2, dstChipID, 0x00, command, data);
                }
                break;
            // Generic support: 8-bit Register, 8-bit data
            case 0x01: // YM2413
            case 0x03: // YM2151
            case 0x06: // YM2203
            case 0x09: // YM3812
            case 0x0A: // YM3526
            case 0x0B: // Y8950Inst
            case 0x0F: // YMZ280B
            case 0x12: // AY8910
            case 0x13: // GameBoy DMG
            case 0x14: // NES APU
    //    	case 0x15: // MultiPCM
            case 0x16: // UPD7759
            case 0x17: // OKIM6258
            case 0x1D: // K053260Inst - TODO: Verify
            case 0x1E: // PokeyInst - TODO: Verify
                command = dstCommand & 0x00FF;
                data = this.data[dataStart + realPos];
                writeChipReg(dstChipType2, dstChipID, 0x00, command, data);
                break;
            // Generic support: 16-bit Register, 8-bit data
            case 0x07: // YM2608
            case 0x08: // YM2610/B
            case 0x0C: // YMF262
            case 0x0D: // YMF278B
            case 0x0E: // YMF271
            case 0x19: // K051649Inst - TODO: Verify
            case 0x1A: // K054539Inst - TODO: Verify
            case 0x1C: // C140Inst - TODO: Verify
                port = (dstCommand & 0xff00) >> 8;
                command = dstCommand & 0x00FF;
                data = this.data[dataStart + realPos];
                writeChipReg(dstChipType2, dstChipID, port, command, data);
                break;
            // Generic support: 8-bit Register with Channel Select, 8-bit data
            case 0x05: // RF5C68
            case 0x10: // RF5C164
            case 0x1B: // OotakeHuC6280
                port = (dstCommand & 0xff00) >> 8;
                command = dstCommand & 0x00FF;
                data = this.data[dataStart + realPos];

                if (port == 0xff) // Send Channel Select
                    writeChipReg(dstChipType2, dstChipID, 0x00, command & 0x0f, data);
                else {
                    int prevChn;

                    prevChn = port; // by default don't restore channel
                    // get current channel for supported chips
                    if (dstChipType2 == 0x05) {
                    } // TODO
                    else if (dstChipType2 == 0x05) {
                    } // TODO
                    else if (dstChipType2 == 0x1B)
                        prevChn = chipRegister.readHuC6280Register(dstChipID, 0x00, model);

                    // Send Channel Select
                    writeChipReg(dstChipType2, dstChipID, 0x00, command >> 4, port);
                    // Send data
                    writeChipReg(dstChipType2, dstChipID, 0x00, command & 0x0F, data);
                    // restore old channel
                    if (prevChn != port)
                        writeChipReg(dstChipType2, dstChipID, 0x00, command >> 4, prevChn);

                    // Send data
                    writeChipReg(dstChipType2, dstChipID, 0x00, command & 0x0F, data);
                }
                break;
            // Generic support: 8-bit Register, 16-bit data
            case 0x1F: // QSoundInst
                command = dstCommand & 0x00FF;
                writeChipReg(dstChipType2, dstChipID, this.data[dataStart + realPos] & 0xff, this.data[dataStart + realPos + 1] & 0xff, command);
                break;
            }
            running |= 0x10;
        }

        public void update(int samples) {
            int newPos;
            int realDataStp;

            //Debug.printf(("DAC update chipId%d samples%d this.Running%d ", chipId, samples, this.Running);
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
            this.dstChipType2 = 0xff;
            this.dstChipID = 0x00;
            this.dstCommand = 0x0000;

            this.running = 0xff; // disable all actions (except setup_chip)
        }

        public void stopDacControl() {
            this.running = 0xff;
        }

        public void reset() {
            this.dstChipType2 = 0x00;
            this.dstChipID = 0x00;
            this.dstCommand = 0x00;
            this.cmdSize = 0x00;

            this.frequency = 0;
            this.dataLen = 0x00;
            this.data = null;
            this.dataStart = 0x00;
            this.stepSize = 0x00;
            this.stepBase = 0x00;

            this.running = 0x00;
            this.reverse = 0x00;
            this.step = 0x00;
            this.pos = 0x00;
            this.realPos = 0x00;
            this.remainCmds = 0x00;
            this.dataStep = 0x00;
        }

        public void setup(int chType, int chNum, int command) {
            this.dstChipType2 = chType; // TypeID (e.g. 0x02 for Ym2612Inst)
            this.dstChipID = chNum; // chips number (to send commands to 1st or 2nd chips)
            this.dstCommand = command; // Port and command (would be 0x02A for Ym2612Inst)

            switch (this.dstChipType2) {
            case 0x00: // SN76496
                if ((this.dstCommand & 0x0010) > 0)
                    this.cmdSize = 0x01; // Volume Write
                else
                    this.cmdSize = 0x02; // Frequency Write
                break;
            case 0x02: // Ym2612Inst
                this.cmdSize = 0x01;
                break;
            case 0x11: // PWM
            case 0x1F: // QSoundInst
                this.cmdSize = 0x02;
                break;
            default:
                this.cmdSize = 0x01;
                break;
            }
            this.dataStep = this.cmdSize * this.stepSize;
        }

        public void setData(byte[] data, int dataLen, int stepSize, int stepBase) {
            if ((this.running & 0x80) > 0)
                return;

            if (dataLen > 0 && data != null) {
                this.dataLen = dataLen;
                this.data = data;
            } else {
                this.dataLen = 0x00;
                this.data = null;
            }
            this.stepSize = stepSize > 0 ? stepSize : 1;
            this.stepBase = stepBase;
            this.dataStep = this.cmdSize * this.stepSize;
        }

        public void refreshData(byte[] data, int dataLen) {
            if ((this.running & 0x80) != 0)
                return;

            if (dataLen > 0 && data != null) {
                this.dataLen = dataLen;
                this.data = data;
            } else {
                this.dataLen = 0x00;
                this.data = null;
            }
        }

        public void setFrequency(int chipId, int frequency) {
            if ((this.running & 0x80) != 0)
                return;

            if (frequency != 0)
                this.step = this.step * this.frequency / frequency;
            this.frequency = frequency;
        }

        public void start(int dataPos, int lenMode, int length) {
            int cmdStepBase;

            if ((this.running & 0x80) != 0)
                return;

            cmdStepBase = this.cmdSize * this.stepBase;
            if (dataPos != 0xffff_ffff) { // skip setting dataStart, if Pos == -1
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
            this.reverse = (lenMode & 0x10) >> 4;

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
