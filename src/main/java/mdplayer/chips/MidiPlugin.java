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

import mdplayer.Common.EnmModel;
import mdplayer.MIDIExport;
import mdplayer.MIDIParam;
import mdplayer.MidiOutInfo;
import mdplayer.Setting;
import mdplayer.plugin.BasePlugin;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.Sn76489Inst;
import mdsound.instrument.Ym2612Inst;

import static java.lang.System.getLogger;
import static mdplayer.plugin.BasePlugin.BUFFER_SIZE;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * MidiPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-01-19 nsano initial version <br>
 */
public class MidiPlugin implements Plugin {

    private static final Logger logger = getLogger(MidiPlugin.class.getName());

    public MIDIParam[] params = {null, null};

    public MIDIExport export;

    private MidiOutInfo[] outInfos = null;

    public final MDSound mds;

    private final List<Receiver> outs = new ArrayList<>();
    private final List<Integer> outsType = new ArrayList<>();

    protected short[] bufVirtualFunction_MIDIKeyboard = null;

    private BasePlugin context;

    public MidiPlugin() {
        mds = new MDSound(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);
    }

    @Override
    public void init(BasePlugin context) {
        this.context = context;

        mdsInit();
        resetAll();

        export = new MIDIExport();
        export.registerYM2612 = context.chipRegister.chip(Ym2612Chip.class).register;
        export.registerYM2151 = context.chipRegister.chip(Ym2151Chip.class).register;

        for (int chipId = 0; chipId < 2; chipId++) {
            params[chipId] = new MIDIParam();
        }
    }

    @Override
    public void close() {
        export.close();
    }

    // ???
    public void initChipRegisterNSF() {
        for (int chipId = 0; chipId < 2; chipId++) {
            params[chipId] = new MIDIParam();
        }
    }

    public MidiOutInfo[] get() {
        return outInfos;
    }

    public void set(MidiOutInfo[] midiOutInfos) {
        this.outInfos = null;
        if (midiOutInfos != null && midiOutInfos.length > 0) {
            this.outInfos = new MidiOutInfo[midiOutInfos.length];
            for (int i = 0; i < midiOutInfos.length; i++) {
                this.outInfos[i] = new MidiOutInfo();
                this.outInfos[i].beforeSendType = midiOutInfos[i].beforeSendType;
                this.outInfos[i].fileName = midiOutInfos[i].fileName;
                this.outInfos[i].id = midiOutInfos[i].id;
                this.outInfos[i].isVST = midiOutInfos[i].isVST;
                this.outInfos[i].manufacturer = midiOutInfos[i].manufacturer;
                this.outInfos[i].name = midiOutInfos[i].name;
                this.outInfos[i].type = midiOutInfos[i].type;
                this.outInfos[i].vendor = midiOutInfos[i].vendor;
            }
        }
//        VstMng.vstMidiOuts = vstMidiOuts;
//        this.vstMidiOutsType = vstMidiOutsType;

        if (params == null && params.length < 1) return;
//        if (outsType == null && vstMng.vstMidiOutsType == null) return;
//        if (outs == null && vstMng.vstMidiOuts == null) return;

        if (!outsType.isEmpty()) params[0].MIDIModule = Math.min(outsType.get(0), 2);
        if (outsType.size() > 1) params[1].MIDIModule = Math.min(outsType.get(1), 2);

//        if (vstMng.vstMidiOutsType.size() > 0) {
//            if (outsType.size() < 1 || (outsType.size() > 0 && outs.get(0) == null))
//                params[0].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(0), 2);
//        }
//        if (vstMng.vstMidiOutsType.size() > 1) {
//            if (outsType.size() < 2 || (outsType.size() > 1 && outs.get(1) == null))
//                params[1].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(1), 2);
//        }
    }

    public void setFileName(String fn) {
        export.playingFileName = fn;
    }

    public int getCount() {
        if (outs == null)
            return 0;
        return outs.size();
    }

    public void send(EnmModel model, int num, byte cmd, byte prm1, byte prm2, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (outs == null) return;
            if (num >= outs.size()) return;
            if (outs.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO cmd, prm1, prm2
            outs.get(num).send(mm, -1);
            if (num < params.length) params[num].sendBuffer(new byte[] {cmd, prm1, prm2});
            return;
        }

//        vstMng.sendMIDIout(model, num, cmd, prm1, prm2, deltaFrames);
    }

    public void send(EnmModel model, int num, byte cmd, byte prm1, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (outs == null) return;
            if (num >= outs.size()) return;
            if (outs.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO cmd, prm1
            outs.get(num).send(mm, -1);
            if (num < params.length) params[num].sendBuffer(new byte[] {cmd, prm1});
            return;
        }

//        vstMng.sendMIDIout(model, num, cmd, prm1, deltaFrames);
    }

    public void send(EnmModel model, int num, byte[] data, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (outs == null) return;
            if (num >= outs.size()) return;
            if (outs.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO
            outs.get(num).send(mm, -1);
            if (num < params.length) params[num].sendBuffer(data);
            return;
        }

//        vstMng.sendMIDIout(model, num, data, deltaFrames);
    }

    public void resetAll() {
        if (outs != null) {
            for (Receiver midiOut : outs) {
                if (midiOut == null)
                    continue;
                midiOut.close(); // TODO
            }
        }

//        vstMng.resetAllMIDIout(EnmModel.VirtualModel);
    }

    public void softReset(int chipId, EnmModel model) {
        resetAll();
    }

    public void mdsInit() {
        List<MDSound.Chip> infos = new ArrayList<>();
        //
        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Ym2612Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = Plugin.setting.getBalance().getVolume(MAIN_TAG, Ym2612Chip.class);
        chip.clock = 7670454;
        chip.option = null;
        context.chipLED.put("PriOPN2", 1);
        infos.add(chip);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = Instrument.getInstrument(Sn76489Inst.class);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Sn76489Chip.class);
        chip.clock = 3579545;
        chip.option = null;
        context.chipLED.put("PriDCSG", 1);
        infos.add(chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, infos);

        // Creates a midi instance.
        make(setting, 1);
    }

    public void make(Setting setting, int m) {
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
                outs.add(mo);
                outsType.add(t);
            }
        }
    }

//    public static final VstMng vstMng = new VstMng();

    public void releaseAll() {
        if (!outs.isEmpty()) {
            for (int i = 0; i < outs.size(); i++) {
                if (outs.get(i) != null) {
                    outs.get(i).close();
                    outs.set(i, null);
                }
            }
            outs.clear();
            outsType.clear();
        }

//        vstMng.ReleaseAllMIDIout();
    }

    public void midiClose() {
        // release the midi out
        if (!outs.isEmpty()) {
            for (int i = 0; i < outs.size(); i++) {
                if (outs.get(i) != null) {
                    outs.get(i).close();
                    outs.set(i, null);
                }
            }
            outs.clear();
            outsType.clear();
        }

//        vstMng.ReleaseAllMIDIout();
//        vstMng.Close();
    }

    public int[][] readYM2612() {
        return mds.inst(Ym2612Inst.class, 0).readRegister(0);
    }

    public MIDIParam get(int chipId) {
        return params[chipId];
    }

    public void softReset(EnmModel model) {
        softReset(0, model);
        softReset(1, model);
    }

    public void keyboard(short[] buffer, int offset, int sampleCount) {
        if (bufVirtualFunction_MIDIKeyboard == null || bufVirtualFunction_MIDIKeyboard.length < sampleCount) {
            bufVirtualFunction_MIDIKeyboard = new short[sampleCount];
        }
        mds.update(bufVirtualFunction_MIDIKeyboard, 0, sampleCount, null);
        for (int i = 0; i < sampleCount; i++) {
            buffer[i + offset] += bufVirtualFunction_MIDIKeyboard[i];
        }
    }
}
