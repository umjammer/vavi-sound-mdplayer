/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.util.List;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;
import mdplayer.MIDIExport;
import mdplayer.MIDIParam;
import mdplayer.MidiOutInfo;


/**
 * MidiPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MidiPlugin implements Plugin {

    public MIDIParam[] midiParams = {null, null};

    public MIDIExport midiExport;

    private MidiOutInfo[] midiOutInfos = null;

    private List<Receiver> midiOuts = null;
    private List<Integer> midiOutsType = null;

    @Override
    public void init(ChipRegister context) {
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

    public void setMIDIout(MidiOutInfo[] midiOutInfos, List<Receiver> midiOuts, List<Integer> midiOutsType) {
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
        this.midiOuts = midiOuts;
        this.midiOutsType = midiOutsType;
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
}
