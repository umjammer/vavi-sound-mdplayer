/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;
import mdplayer.MIDIExport;
import mdplayer.MIDIParam;
import mdplayer.MidiOutInfo;
import mdplayer.Setting;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;

import static java.lang.System.getLogger;
import static mdplayer.Audio.BUFFER_SIZE;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MidiPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MidiPlugin implements Plugin {

    private static final Logger logger = getLogger(MidiPlugin.class.getName());

    public MIDIParam[] midiParams = {null, null};

    public MIDIExport midiExport;

    private MidiOutInfo[] midiOutInfos = null;

    public final mdsound.MDSound mdsMIDI;

    private List<Receiver> midiOuts = new ArrayList<>();
    private List<Integer> midiOutsType = new ArrayList<>();

    protected short[] bufVirtualFunction_MIDIKeyboard = null;

    private ChipRegister context;

    public MidiPlugin() {
        mdsMIDI = new mdsound.MDSound(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);
    }

    @Override
    public void init(ChipRegister context) {
        this.context = context;

        midiExport = new MIDIExport();
        midiExport.fmRegisterYM2612 = context.chip(Ym2612Chip.class).fmRegisterYM2612;
        midiExport.fmRegisterYM2151 = context.chip(Ym2151Chip.class).fmRegisterYM2151;

        for (int chipId = 0; chipId < 2; chipId++) {
            midiParams[chipId] = new MIDIParam();
        }
    }

    @Override
    public void close() {
        midiExport.close();
    }

    public void initChipRegisterNSF() {
        for (int chipId = 0; chipId < 2; chipId++) {
            midiParams[chipId] = new MIDIParam();
        }
    }

    public MidiOutInfo[] getMIDIoutInfo() {
        return midiOutInfos;
    }

    public void setMIDIout(MidiOutInfo[] midiOutInfos) {
        this.midiOutInfos = null;
        if (midiOutInfos != null && midiOutInfos.length > 0) {
            this.midiOutInfos = new MidiOutInfo[midiOutInfos.length];
            for (int i = 0; i < midiOutInfos.length; i++) {
                this.midiOutInfos[i] = new MidiOutInfo();
                this.midiOutInfos[i].beforeSendType = midiOutInfos[i].beforeSendType;
                this.midiOutInfos[i].fileName = midiOutInfos[i].fileName;
                this.midiOutInfos[i].id = midiOutInfos[i].id;
                this.midiOutInfos[i].isVST = midiOutInfos[i].isVST;
                this.midiOutInfos[i].manufacturer = midiOutInfos[i].manufacturer;
                this.midiOutInfos[i].name = midiOutInfos[i].name;
                this.midiOutInfos[i].type = midiOutInfos[i].type;
                this.midiOutInfos[i].vendor = midiOutInfos[i].vendor;
            }
        }
        //VstMng.vstMidiOuts = vstMidiOuts;
        //this.vstMidiOutsType = vstMidiOutsType;

        if (midiParams == null && midiParams.length < 1) return;
//        if (midiOutsType == null && vstMng.vstMidiOutsType == null) return;
//        if (midiOuts == null && vstMng.vstMidiOuts == null) return;

        if (!midiOutsType.isEmpty()) midiParams[0].MIDIModule = Math.min(midiOutsType.get(0), 2);
        if (midiOutsType.size() > 1) midiParams[1].MIDIModule = Math.min(midiOutsType.get(1), 2);

//        if (vstMng.vstMidiOutsType.size() > 0) {
//            if (midiOutsType.size() < 1 || (midiOutsType.size() > 0 && midiOuts.get(0) == null))
//                midiParams[0].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(0), 2);
//        }
//        if (vstMng.vstMidiOutsType.size() > 1) {
//            if (midiOutsType.size() < 2 || (midiOutsType.size() > 1 && midiOuts.get(1) == null))
//                midiParams[1].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(1), 2);
//        }
    }

    public void setFileName(String fn) {
        midiExport.playingFileName = fn;
    }

    public int getMIDIoutCount() {
        if (midiOuts == null)
            return 0;
        return midiOuts.size();
    }

    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, byte prm2, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (midiOuts == null) return;
            if (num >= midiOuts.size()) return;
            if (midiOuts.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO cmd, prm1, prm2
            midiOuts.get(num).send(mm, -1);
            if (num < midiParams.length) midiParams[num].sendBuffer(new byte[] {cmd, prm1, prm2});
            return;
        }

//        vstMng.sendMIDIout(model, num, cmd, prm1, prm2, deltaFrames);
    }

    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (midiOuts == null) return;
            if (num >= midiOuts.size()) return;
            if (midiOuts.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO cmd, prm1
            midiOuts.get(num).send(mm, -1);
            if (num < midiParams.length) midiParams[num].sendBuffer(new byte[] {cmd, prm1});
            return;
        }

//        vstMng.sendMIDIout(model, num, cmd, prm1, deltaFrames);
    }

    public void sendMIDIout(EnmModel model, int num, byte[] data, int deltaFrames/* = 0*/) {
        if (model == EnmModel.RealModel) {
            if (midiOuts == null) return;
            if (num >= midiOuts.size()) return;
            if (midiOuts.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO
            midiOuts.get(num).send(mm, -1);
            if (num < midiParams.length) midiParams[num].sendBuffer(data);
            return;
        }

//        vstMng.sendMIDIout(model, num, data, deltaFrames);
    }

    public void resetAllMIDIout() {
        if (midiOuts != null) {
            for (Receiver midiOut : midiOuts) {
                if (midiOut == null)
                    continue;
                midiOut.close(); // TODO
            }
        }

//        vstMng.resetAllMIDIout(EnmModel.VirtualModel);
    }

    public void softResetMIDI(int chipId, EnmModel model) {
        resetAllMIDIout();
    }

    public void mdsInit() {
        //
        List<MDSound.Chip> lstChips = new ArrayList<>();
        MDSound.Chip chip;

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Ym2612Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2612Inst.class);
        chip.clock = 7670454;
        chip.option = null;
        context.chipLED.put("PriOPN2", 1);
        lstChips.add(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Sn76489Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Inst.class);
        chip.clock = 3579545;
        chip.option = null;
        context.chipLED.put("PriDCSG", 1);
        lstChips.add(chip);

        mdsMIDI.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, lstChips.toArray(MDSound.Chip[]::new));

        // Creates a midi instance.
        makeMIDIout(setting, 1);
    }

    public void makeMIDIout(Setting setting, int m) {
        if (setting.getMidiOut().getMidiOutInfos() == null || setting.getMidiOut().getMidiOutInfos().isEmpty())
            return;
        if (setting.getMidiOut().getMidiOutInfos().get(m) == null || setting.getMidiOut().getMidiOutInfos().get(m).length < 1)
            return;

        for (int i = 0; i < setting.getMidiOut().getMidiOutInfos().get(m).length; i++) {
            int n = -1;
            int t = 0;
            Receiver mo = null;

            MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
            int j = 0;
            for (var info : midiDeviceInfos) {
                MidiDevice device;
                try {
                    device = MidiSystem.getMidiDevice(info);
                } catch (MidiUnavailableException e) {
                    throw new RuntimeException(e);
                }
                if (device.getMaxReceivers() == 0) {
                    continue;
                }
                if (!setting.getMidiOut().getMidiOutInfos().get(m)[i].name.equals(info.getName()))
                    continue;

                n = j++;
                t = setting.getMidiOut().getMidiOutInfos().get(m)[i].type;
                break;
            }

            if (n != -1) {
                try {
                    mo = MidiSystem.getReceiver();
                } catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    mo = null;
                }
            }

//            if (n == -1) {
//                vstMng.SetupVstMidiOut(setting.getMidiOut().getMidiOutInfos().get(m)[i]);
//            }

            if (mo != null) {
                midiOuts.add(mo);
                midiOutsType.add(t);
            }
        }
    }

//    public static final VstMng vstMng = new VstMng();

    public void releaseAllMIDIout() {
        if (!midiOuts.isEmpty()) {
            for (int i = 0; i < midiOuts.size(); i++) {
                if (midiOuts.get(i) != null) {
                    midiOuts.get(i).close();
                    midiOuts.set(i, null);
                }
            }
            midiOuts.clear();
            midiOutsType.clear();
        }

//        vstMng.ReleaseAllMIDIout();
    }

    public void midiClose() {
        // release the midi out
        if (!midiOuts.isEmpty()) {
            for (int i = 0; i < midiOuts.size(); i++) {
                if (midiOuts.get(i) != null) {
                    midiOuts.get(i).close();
                    midiOuts.set(i, null);
                }
            }
            midiOuts.clear();
            midiOutsType.clear();
        }

//        vstMng.ReleaseAllMIDIout();
//        vstMng.Close();
    }

    public int[][] getYM2612MIDIRegister() {
        return mdsMIDI.ReadYm2612Register(0, 0);
    }

    public MIDIParam getMIDIInfos(int chipId) {
        return midiParams[chipId];
    }

    public void softReset(EnmModel model) {
        softResetMIDI(0, model);
        softResetMIDI(1, model);
    }

    public void midiKeyboard(short[] buffer, int offset, int sampleCount) {
        if (bufVirtualFunction_MIDIKeyboard == null || bufVirtualFunction_MIDIKeyboard.length < sampleCount) {
            bufVirtualFunction_MIDIKeyboard = new short[sampleCount];
        }
        mdsMIDI.update(bufVirtualFunction_MIDIKeyboard, 0, sampleCount, null);
        for (int i = 0; i < sampleCount; i++) {
            buffer[i + offset] += bufVirtualFunction_MIDIKeyboard[i];
        }
    }
}
