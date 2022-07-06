package mdplayer.vst;

import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import dotnet4j.util.compat.EventHandler;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.Log;
import mdplayer.MIDIParam;
import mdplayer.Setting;
import mdplayer.MidiOutInfo;
import org.urish.jnavst.VstConst;
import org.urish.jnavst.VstPlugin;
import org.urish.jnavst.VstTimeInfo;
import vavi.util.Debug;


public class VstMng {

    public Setting setting = null;
    //ChipRegisterからインスタンスをもらう
    public MIDIParam[] midiParams = null;

    private List<VstInfo2> vstPlugins = new ArrayList<>();
    private List<VstInfo2> vstPluginsInst = new ArrayList<>();
    public List<VstInfo2> vstMidiOuts = new ArrayList<>();
    public List<Integer> vstMidiOutsType = new ArrayList<>();


    public void vstparse() {
        while (vstPluginsInst.size() > 0) {
            if (vstPluginsInst.get(0) != null) {
                if (vstPluginsInst.get(0).vstPlugins != null)
                    vstPluginsInst.get(0).vstPlugins.editClose();
                vstPluginsInst.get(0).vstPluginsForm.timer1.stop();
                vstPluginsInst.get(0).location = vstPluginsInst.get(0).vstPluginsForm.getLocation();
                vstPluginsInst.get(0).vstPluginsForm.setVisible(false);
                if (vstPluginsInst.get(0).vstPlugins != null)
                    vstPluginsInst.get(0).vstPlugins.close();
                if (vstPluginsInst.get(0).vstPlugins != null)
                    vstPluginsInst.get(0).vstPlugins.MainsChanged(false);
                vstPluginsInst.get(0).vstPlugins.close();
            }

            vstPluginsInst.remove(0);
        }

        while (vstPlugins.size() > 0) {
            if (vstPlugins.get(0) != null) {
                if (vstPlugins.get(0).vstPlugins != null)
                    vstPlugins.get(0).vstPlugins.editClose();
                vstPlugins.get(0).vstPluginsForm.timer1.stop();
                vstPlugins.get(0).location = vstPlugins.get(0).vstPluginsForm.getLocation();
                vstPlugins.get(0).vstPluginsForm.setVisible(false);
                if (vstPlugins.get(0).vstPlugins != null)
                    vstPlugins.get(0).vstPlugins.stopProcess();
                if (vstPlugins.get(0).vstPlugins != null)
                    vstPlugins.get(0).vstPlugins.MainsChanged(false);
                vstPlugins.get(0).vstPlugins.close();
            }

            vstPlugins.remove(0);
        }
    }

    public void SetUpVstInstrument(Map.Entry<String, Integer> kv) {
        VstPlugin ctx = OpenPlugin(kv.getKey());
        if (ctx == null) return;

        VstInfo2 vi = new VstInfo2();
        vi.key = String.valueOf(System.currentTimeMillis());
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        vi.vstPlugins = ctx;
        vi.fileName = kv.getKey();
        vi.isInstrument = true;

        ctx.setBlockSize(512);
        ctx.setSampleRate(setting.getOutputDevice().getSampleRate());
        ctx.MainsChanged(true);
        ctx.startProcess();
        vi.effectName = ctx.GetEffectName();
        vi.editor = true;

        if (vi.editor) {
            frmVST dlg = new frmVST(null);
            dlg.setPluginCommandStub(ctx);
            dlg.Show(vi);
            vi.vstPluginsForm = dlg;
        }

        vstPluginsInst.add(vi);
    }

    public void SetUpVstEffect() {
        for (int i = 0; i < setting.getVst().getVSTInfo().length; i++) {
            if (setting.getVst().getVSTInfo()[i] == null) continue;
            VstPlugin ctx = OpenPlugin(setting.getVst().getVSTInfo()[i].fileName);
            if (ctx == null) continue;

            VstInfo2 vi = new VstInfo2();
            vi.vstPlugins = ctx;
            vi.fileName = setting.getVst().getVSTInfo()[i].fileName;
            vi.key = setting.getVst().getVSTInfo()[i].key;

            ctx.setBlockSize(512);
            ctx.setSampleRate(setting.getOutputDevice().getSampleRate() / 1000.0f);
            ctx.MainsChanged(true);
            ctx.StartProcess();
            vi.effectName = ctx.GetEffectName();
            vi.power = setting.getVst().getVSTInfo()[i].power;
            vi.editor = setting.getVst().getVSTInfo()[i].editor;
            vi.location = setting.getVst().getVSTInfo()[i].location;
            vi.param = setting.getVst().getVSTInfo()[i].param;

            if (vi.editor) {
                frmVST dlg = new frmVST(null);
                dlg.setPluginCommandStub(ctx);
                dlg.Show(vi);
                vi.vstPluginsForm = dlg;
            }

            if (vi.param != null) {
                for (int p = 0; p < vi.param.length; p++) {
                    ctx.setParameter(p, vi.param[p]);
                }
            }

            vstPlugins.add(vi);
        }
    }

    public void SetupVstMidiOut(MidiOutInfo mi) {
        int vn = -1;
        int vt = 0;
        VstInfo2 vmo = null;

        for (int j = 0; j < vstPluginsInst.size(); j++) {
            if (!vstPluginsInst.get(j).isInstrument || !mi.fileName.equals(vstPluginsInst.get(j).fileName)) continue;
            boolean k = false;
            for (VstInfo2 v : vstMidiOuts)
                if (v == vstPluginsInst.get(j)) {
                    k = true;
                    break;
                }
            if (k) continue;
            vn = j;
            vt = mi.type;
            break;
        }

        if (vn != -1) {
            try {
                vmo = vstPluginsInst.get(vn);
            } catch (Exception e) {
                e.printStackTrace();
                vmo = null;
            }
        }

        if (vmo != null) {
            vstMidiOuts.add(vmo);
            vstMidiOutsType.add(vt);
        }

    }

    public void ReleaseAllMIDIout() {
        if (vstMidiOuts != null && vstMidiOuts.size() > 0) {
            vstMidiOuts.clear();
            vstMidiOutsType.clear();
        }
    }

    private void ReleaseAllPlugins() {
        for (VstInfo2 ctx : vstPlugins) {
            // dispose of all (unmanaged) resources
            ctx.vstPlugins.Dispose();
        }

        vstPlugins.clear();
    }


    public void Close() {
        setting.getVst().VSTInfo = null;
        List<VstInfo> vstlst = new ArrayList<>();

        for (VstInfo2 vstPlugin : vstPlugins) {
            try {
                vstPlugin.vstPluginsForm.timer1.setEnabled(false);
                vstPlugin.location = vstPlugin.vstPluginsForm.Location;
                vstPlugin.vstPluginsForm.Close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (vstPlugin.vstPlugins != null) {
                    vstPlugin.vstPlugins.editClose();
                    vstPlugin.vstPlugins.PluginCommandStub.StopProcess();
                    vstPlugin.vstPlugins.PluginCommandStub.MainsChanged(false);
                    int pc = vstPlugin.vstPlugins.PluginInfo.ParameterCount;
                    List<Float> plst = new ArrayList<>();
                    for (int p = 0; p < pc; p++) {
                        float v = vstPlugin.vstPlugins.getParameter(p);
                        plst.add(v);
                    }
                    vstPlugin.param = Common.toArray(plst);
                    vstPlugin.vstPlugins.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            VstInfo vi = new VstInfo();
            vi.editor = vstPlugin.editor;
            vi.fileName = vstPlugin.fileName;
            vi.key = vstPlugin.key;
            vi.effectName = vstPlugin.effectName;
            vi.power = vstPlugin.power;
            vi.location = vstPlugin.location;
            vi.param = vstPlugin.param;

            if (!vstPlugin.isInstrument) vstlst.add(vi);
        }
        setting.getVst().setVSTInfo(vstlst.toArray(VstInfo::new));


        for (VstInfo2 vstInfo2 : vstPluginsInst) {
            try {
                vstInfo2.vstPluginsForm.timer1.setEnabled(false);
                vstInfo2.location = vstInfo2.vstPluginsForm.Location;
                vstInfo2.vstPluginsForm.Close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (vstInfo2.vstPlugins != null) {
                    vstInfo2.vstPlugins.editClose();
                    vstInfo2.vstPlugins.StopProcess();
                    vstInfo2.vstPlugins.PluginCommandStub.MainsChanged(false);
                    int pc = vstInfo2.vstPlugins.PluginInfo.ParameterCount;
                    List<Float> plst = new ArrayList<>();
                    for (int p = 0; p < pc; p++) {
                        float v = vstInfo2.vstPlugins.PluginCommandStub.GetParameter(p);
                        plst.add(v);
                    }
                    vstInfo2.param = Common.toArray(plst);
                    vstInfo2.vstPlugins.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            VstInfo vi = new VstInfo();
            vi.editor = vstInfo2.editor;
            vi.fileName = vstInfo2.fileName;
            vi.key = vstInfo2.key;
            vi.effectName = vstInfo2.effectName;
            vi.power = vstInfo2.power;
            vi.location = vstInfo2.location;
            vi.param = vstInfo2.param;
        }
    }

    public void VST_Update(short[] buffer, int offset, int sampleCount) {
        if (vstPlugins.size() < 1 && vstPluginsInst.size() < 1) return;
        if (buffer == null || buffer.length < 1 || sampleCount == 0) return;

        try {
             // if (trdStopped) return;

            int blockSize = sampleCount / 2;

            for (VstInfo2 info2 : vstPluginsInst) {
                VstPluginContext PluginContext = info2.vstPlugins;
                if (PluginContext == null) continue;
                if (PluginContext.PluginCommandStub == null) continue;


                int inputCount = info2.vstPlugins.PluginInfo.AudioInputCount;
                int outputCount = info2.vstPlugins.PluginInfo.AudioOutputCount;

                try (VstAudioBufferManager inputMgr = new VstAudioBufferManager(inputCount, blockSize)) {
                    try (VstAudioBufferManager outputMgr = new VstAudioBufferManager(outputCount, blockSize)) {
                        VstAudioBuffer[] inputBuffers = inputMgr.toArray();
                        VstAudioBuffer[] outputBuffers = outputMgr.toArray();

                        if (inputCount != 0) {
                            inputMgr.ClearBuffer(inputBuffers[0]);
                            inputMgr.ClearBuffer(inputBuffers[1]);

                            for (int j = 0; j < blockSize; j++) {
                                // generate a value between -1.0 and 1.0
                                inputBuffers[0].set(j, buffer[j * 2 + offset + 0] / (float) Short.MAX_VALUE);
                                inputBuffers[1].set(j, buffer[j * 2 + offset + 1] / (float) Short.MAX_VALUE);
                            }
                        }

                        outputMgr.ClearBuffer(outputBuffers[0]);
                        outputMgr.ClearBuffer(outputBuffers[1]);

                        PluginContext.PluginCommandStub.ProcessEvents(info2.lstEvent.toArray());
                        info2.lstEvent.clear();


                        PluginContext.PluginCommandStub.ProcessReplacing(inputBuffers, outputBuffers);

                        for (int j = 0; j < blockSize; j++) {
                            // generate a value between -1.0 and 1.0
                            if (inputCount == 0) {
                                buffer[j * 2 + offset + 0] += (short) (outputBuffers[0][j] * Short.MAX_VALUE);
                                buffer[j * 2 + offset + 1] += (short) (outputBuffers[1][j] * Short.MAX_VALUE);
                            } else {
                                buffer[j * 2 + offset + 0] = (short) (outputBuffers[0][j] * Short.MAX_VALUE);
                                buffer[j * 2 + offset + 1] = (short) (outputBuffers[1][j] * Short.MAX_VALUE);
                            }
                        }
                    }
                }
            }

            for (VstInfo2 info2 : vstPlugins) {
                VstPluginContext PluginContext = info2.vstPlugins;
                if (PluginContext == null) continue;
                if (PluginContext.PluginCommandStub == null) continue;


                int inputCount = info2.vstPlugins.PluginInfo.AudioInputCount;
                int outputCount = info2.vstPlugins.PluginInfo.AudioOutputCount;

                try (VstAudioBufferManager inputMgr = new VstAudioBufferManager(inputCount, blockSize)) {
                    try (VstAudioBufferManager outputMgr = new VstAudioBufferManager(outputCount, blockSize)) {
                        VstAudioBuffer[] inputBuffers = inputMgr.toArray();
                        VstAudioBuffer[] outputBuffers = outputMgr.toArray();

                        if (inputCount != 0) {
                            inputMgr.ClearBuffer(inputBuffers[0]);
                            inputMgr.ClearBuffer(inputBuffers[1]);

                            for (int j = 0; j < blockSize; j++) {
                                // generate a value between -1.0 and 1.0
                                inputBuffers[0][j] = buffer[j * 2 + offset + 0] / (float) Short.MAX_VALUE;
                                inputBuffers[1][j] = buffer[j * 2 + offset + 1] / (float) Short.MAX_VALUE;
                            }
                        }

                        outputMgr.ClearBuffer(outputBuffers[0]);
                        outputMgr.ClearBuffer(outputBuffers[1]);

                        PluginContext.PluginCommandStub.ProcessReplacing(inputBuffers, outputBuffers);

                        for (int j = 0; j < blockSize; j++) {
                            // generate a value between -1.0 and 1.0
                            if (inputCount == 0) {
                                buffer[j * 2 + offset + 0] += (short) (outputBuffers[0][j] * Short.MAX_VALUE);
                                buffer[j * 2 + offset + 1] += (short) (outputBuffers[1][j] * Short.MAX_VALUE);
                            } else {
                                buffer[j * 2 + offset + 0] = (short) (outputBuffers[0][j] * Short.MAX_VALUE);
                                buffer[j * 2 + offset + 1] = (short) (outputBuffers[1][j] * Short.MAX_VALUE);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, byte prm2, int deltaFrames/* = 0*/) {
        if (model == EnmModel.RealModel) return;
        if (vstMidiOuts == null) return;
        if (num >= vstMidiOuts.size()) return;
        if (vstMidiOuts.get(num) == null) return;

        VstMidiEvent evt = new VstMidiEvent(
                deltaFrames
                , 0 // noteLength
                , 0 // noteOffset
                , new byte[] {cmd, prm1, prm2}
                , 0 // detune
                , 0 // noteOffVelocity
        );
        vstMidiOuts.get(num).AddMidiEvent(evt);
        if (num < midiParams.length) midiParams[num].SendBuffer(new byte[] {cmd, prm1, prm2});
    }

    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, int deltaFrames/* = 0*/) {
        if (model == EnmModel.RealModel) return;
        if (vstMidiOuts == null) return;
        if (num >= vstMidiOuts.size()) return;
        if (vstMidiOuts.get(num) == null) return;

        Jacobi.Vst.Core.VstMidiEvent evt = new Jacobi.Vst.Core.VstMidiEvent(
                deltaFrames
                , 0 // noteLength
                , 0 // noteOffset
                , new byte[] {cmd, prm1}
                , 0 // detune
                , 0 // noteOffVelocity
        );
        vstMidiOuts.get(num).AddMidiEvent(evt);
        if (num < midiParams.length) midiParams[num].SendBuffer(new byte[] {cmd, prm1});
    }

    public void sendMIDIout(EnmModel model, int num, byte[] data, int deltaFrames/* = 0*/) {
        if (model == EnmModel.RealModel) return;
        if (vstMidiOuts == null) return;
        if (num >= vstMidiOuts.size()) return;
        if (vstMidiOuts.get(num) == null) return;

        VstMidiEvent evt = new VstMidiEvent(
                deltaFrames
                , 0 // noteLength
                , 0 // noteOffset
                , data
                , 0 // detune
                , 0 // noteOffVelocity
        );
        vstMidiOuts.get(num).AddMidiEvent(evt);
        if (num < midiParams.length) midiParams[num].SendBuffer(data);
    }

    public void resetAllMIDIout(EnmModel model) {
        if (model == EnmModel.RealModel) return;

        if (vstMidiOuts == null) return;
        for (int i = 0; i < vstMidiOuts.size(); i++) {
            if (vstMidiOuts[i] == null) continue;
            if (vstMidiOuts[i].vstPlugins == null) continue;
            if (vstMidiOuts[i].vstPlugins.PluginCommandStub == null) continue;

            try {
                List<Byte> dat = new ArrayList<>();
                for (int ch = 0; ch < 16; ch++) {
                    sendMIDIout(EnmModel.VirtualModel, i, new byte[] {(byte) (0xb0 + ch), 120, 0x00}, 0);
                    sendMIDIout(EnmModel.VirtualModel, i, new byte[] {(byte) (0xb0 + ch), 64, 0x00}, 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public VstPlugin OpenPlugin(String pluginPath) {
        try {
            HostCommandStub hostCmdStub = new HostCommandStub(setting);
            hostCmdStub.PluginCalled += new EventHandler<>(HostCmdStub_PluginCalled);

            VstPlugin ctx = new VstPlugin(new File(pluginPath)/*, hostCmdStub*/);

            // add custom data to the context
            ctx.Set("PluginPath", pluginPath);
            ctx.Set("HostCmdStub", hostCmdStub);

            // actually open the plugin itself
            ctx.open();

            return ctx;
        } catch (Exception e) {
            e.printStackTrace();
            //JOptionPane.showMessageDialog(this, e.toString(), Text, JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }

    private static void HostCmdStub_PluginCalled(Object sender, PluginCalledEventArgs e) {
        HostCommandStub hostCmdStub = (HostCommandStub) sender;

        // can be null when called from inside the plugin main entry point.
        if (hostCmdStub.PluginContext.PluginInfo != null) {
            Debug.println("Plugin " + hostCmdStub.PluginContext.PluginInfo.PluginID + " called:" + e.getMessage());
        } else {
            Debug.println("The loading Plugin called:" + e.getMessage());
        }
    }

    public List<VstInfo2> getVSTInfos() {
        return vstPlugins;
    }

    public VstInfo getVSTInfo(String filename) {
        VstPlugin ctx = OpenPlugin(filename);
        if (ctx == null) return null;

        VstInfo ret = new VstInfo();
        ret.effectName = ctx.getName();
        ret.productName = ctx.getProductString();
        ret.vendorName = ctx.getVendorString();
        ret.programName = ctx.getProgramName();
        ret.fileName = filename;
        ret.midiInputChannels = ctx.getNumInputs();
        ret.midiOutputChannels = ctx.getNumOutputs();
        ctx.close();

        return ret;
    }

    public boolean addVSTeffect(String fileName) {
        VstPlugin ctx = OpenPlugin(fileName);
        if (ctx == null) return false;

         // Stop();

        VstInfo2 vi = new VstInfo2();
        vi.vstPlugins = ctx;
        vi.fileName = fileName;
        vi.key = String.valueOf(System.currentTimeMillis());
        Thread.yield();

        ctx.setBlockSize(512);
        ctx.setSampleRate(setting.getOutputDevice().getSampleRate());
        ctx.MainsChanged(true);
        ctx.StartProcess();
        vi.effectName = ctx.getName();
        vi.power = true;
        ctx.getParameterProperties(0);


        frmVST dlg = new frmVST(null);
        dlg.setPluginCommandStub(ctx);
        dlg.Show(vi);
        vi.vstPluginsForm = dlg;
        vi.editor = true;

        vstPlugins.add(vi);

        List<VstInfo> lvi = new ArrayList<>();
        for (VstInfo2 vi2 : vstPlugins) {
            VstInfo v = new VstInfo();
            v.editor = vi.editor;
            v.effectName = vi.effectName;
            v.fileName = vi.fileName;
            v.key = vi.key;
            v.location = vi.location;
            v.midiInputChannels = vi.midiInputChannels;
            v.midiOutputChannels = vi.midiOutputChannels;
            v.param = vi.param;
            v.power = vi.power;
            v.productName = vi.productName;
            v.programName = vi.programName;
            v.vendorName = vi.vendorName;
            lvi.add(v);
        }
        setting.getVst().setVSTInfo(lvi.toArray());

        return true;
    }

    public boolean delVSTeffect(String key) {
        if (key.equals("")) {
            for (VstInfo2 vstPlugin : vstPlugins) {
                try {
                    if (vstPlugin.vstPlugins != null) {
                        vstPlugin.vstPluginsForm.timer1.stop();
                        vstPlugin.location = vstPlugin.vstPluginsForm.getLocation();
                        vstPlugin.vstPluginsForm.setVisible(false);
                        vstPlugin.vstPlugins.editClose();
                        vstPlugin.vstPlugins.StopProcess();
                        vstPlugin.vstPlugins.MainsChanged(false);
                        vstPlugin.vstPlugins.close();
                    }
                } catch (Exception e) {
                }
            }
            vstPlugins.clear();
            setting.getVst().setVSTInfo(VstInfo::new);
        } else {
            int ind = -1;
            for (int i = 0; i < vstPlugins.size(); i++) {
                 // if (vstPlugins.get(i).fileName == fileName)
                if (vstPlugins.get(i).key.equals(key)) {
                    ind = i;
                    break;
                }
            }

            if (ind != -1) {
                try {
                    if (vstPlugins.get(ind).vstPlugins != null) {
                        vstPlugins.get(ind).vstPluginsForm.timer1.stop();
                        vstPlugins.get(ind).location = vstPlugins.get(ind).vstPluginsForm.getLocation();
                        vstPlugins.get(ind).vstPluginsForm.setVisible(false);
                        vstPlugins.get(ind).vstPlugins.editClose();
                        vstPlugins.get(ind).vstPlugins.StopProcess();
                        vstPlugins.get(ind).vstPlugins.MainsChanged(false);
                        vstPlugins.get(ind).vstPlugins.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                vstPlugins.remove(ind);
            }

            List<VstInfo> nvst = new ArrayList<>();
            for (VstInfo vi : setting.getVst().getVSTInfo()) {
                if (vi.key.equals(key)) continue;
                nvst.add(vi);
            }
            setting.getVst().setVSTInfo(nvst.toArray(VstInfo::new));
        }

        return true;
    }

    /**
     * The HostCommandStub class represents the part of the host that a plugin can call.
     */
    public static class HostCommandStub implements IVstHostCommandStub {
        private Setting setting;

        public HostCommandStub(Setting setting) {
            this.setting = setting;
        }

        /**
         * Raised when one of the methods instanceof called.
         */
        public EventHandler<PluginCalledEventArgs> PluginCalled;

        private void RaisePluginCalled(String message) {
            EventHandler<PluginCalledEventArgs> handler = PluginCalled;

            if (handler != null) {
                handler(this, new PluginCalledEventArgs(message));
            }
        }

//#region IVstHostCommandsStub Members

        /* TODO */
        public IVstPluginContext getPluginContext() {
            return null;
        }

        public void setPluginContext(IVstPluginContext value) {
        }

        /* */
        public boolean BeginEdit(int index) {
            RaisePluginCalled("BeginEdit(" + index + ")");

            return false;
        }

        /* */
        public VstCanDoResult CanDo(String cando) {
            RaisePluginCalled("CanDo(" + cando + ")");
            return VstCanDoResult.Unknown;
        }

        /* */
        public boolean CloseFileSelector(VstFileSelect fileSelect) {
            RaisePluginCalled("CloseFileSelector(" + fileSelect.Command + ")");
            return false;
        }

        /* */
        public boolean EndEdit(int index) {
            RaisePluginCalled("EndEdit(" + index + ")");
            return false;
        }

        /* */
        public VstAutomationStates GetAutomationState() {
            RaisePluginCalled("GetAutomationState()");
            return VstAutomationStates.Off;
        }

        /* */
        public int GetBlockSize() {
            RaisePluginCalled("GetBlockSize()");
            return 1024;
        }

        /* */
        public String GetDirectory() {
            RaisePluginCalled("GetDirectory()");
            return null;
        }

        /* */
        public int GetInputLatency() {
            RaisePluginCalled("GetInputLatency()");
            return 0;
        }

        /* */
        public VstHostLanguage GetLanguage() {
            RaisePluginCalled("GetLanguage()");
            return VstHostLanguage.NotSupported;
        }

        /* */
        public int GetOutputLatency() {
            RaisePluginCalled("GetOutputLatency()");
            return 0;
        }

        /* */
        public VstProcessLevels GetProcessLevel() {
            RaisePluginCalled("GetProcessLevel()");
            return VstProcessLevels.Unknown;
        }

        /* */
        public String GetProductString() {
            RaisePluginCalled("GetProductString()");
            return "VST.NET";
        }

        /* */
        public float GetSampleRate() {
            RaisePluginCalled("GetSampleRate()");
            return setting.getOutputDevice().getSampleRate() / 1000.0f;
        }

        /* */
        public VstTimeInfo GetTimeInfo(VstTimeInfoFlags filterFlags) {
            //RaisePluginCalled("GetTimeInfo(" + filterFlags + ")");
            VstTimeInfo vti = new VstTimeInfo();
            vti.samplePos = 0;
            vti.sampleRate = setting.getOutputDevice().getSampleRate() / 1000.0f;
            vti.nanoSeconds = 0;
            vti.ppqPos = 0;
            vti.tempo = 120;
            vti.barStartPos = 0;
            vti.cycleStartPos = 0;
            vti.cycleEndPos = 0;
            vti.timeSigNumerator = 4;
            vti.timeSigDenominator = 4;
            vti.smpteOffset = 0;
            vti.smpteFrameRate = VstSmpteFrameRate.Smpte24fps;
            vti.samplesToNextClock = 0;
            vti.flags = VstConst.VST_NanosValid
                    | VstConst.VST_PpqPosValid
                    | VstConst.VST_TempoValid
                    | VstConst.VST_TimeSigValid;
            return vti;
        }

        /* */
        public String GetVendorString() {
            RaisePluginCalled("GetVendorString()");
            return "";
        }

        /* */
        public int GetVendorVersion() {
            RaisePluginCalled("GetVendorVersion()");
            return 1000;
        }

        /* */
        public boolean IoChanged() {
            RaisePluginCalled("IoChanged()");
            return false;
        }

        /* */
        public boolean OpenFileSelector(VstFileSelect fileSelect) {
            RaisePluginCalled("OpenFileSelector(" + fileSelect.Command + ")");
            return false;
        }

        /* */
        public boolean ProcessEvents(VstEvent[] events) {
            RaisePluginCalled("ProcessEvents(" + events.length + ")");
            return false;
        }

        /* */
        public boolean SizeWindow(int width, int height) {
            RaisePluginCalled("SizeWindow(" + width + ", " + height + ")");
            return false;
        }

        /* */
        public boolean UpdateDisplay() {
            RaisePluginCalled("UpdateDisplay()");
            return false;
        }

        public int GetCurrentPluginID() {
            RaisePluginCalled("GetCurrentPluginID()");
            return PluginContext.PluginInfo.PluginID;
        }

        public int GetVersion() {
            RaisePluginCalled("GetVersion()");
            return 1000;
        }

        public void ProcessIdle() {
            RaisePluginCalled("ProcessIdle()");
        }

        public void SetParameterAutomated(int index, float value) {
            RaisePluginCalled("SetParameterAutomated(" + index + ", " + value + ")");
        }
    }

    /**
     * Event arguments used when one of the mehtods instanceof called.
     */
    public static class PluginCalledEventArgs extends EventObject {
        /**
         * Constructs a new instance with a "message".
         * @param message
         */
        public PluginCalledEventArgs(Object source, String message) {
            super(source);
            this.message = message;
        }

        private String message;

        /**
         * Gets the message.
         */
        public String getMessage() {
            return message;
        }
    }

    public static class VstInfo2 extends VstInfo {
        public VstPlugin vstPlugins = null;
        public frmVST vstPluginsForm = null;

        // 実際にVSTiかどうかは問わない
        public boolean isInstrument = false;
        public List<VstMidiEvent> lstEvent = new ArrayList<>();

        public void AddMidiEvent(VstMidiEvent evt) {
            lstEvent.add(evt);
        }
    }
}
