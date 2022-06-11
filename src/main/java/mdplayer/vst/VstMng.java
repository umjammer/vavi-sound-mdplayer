package mdplayer.vst;

import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

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
                    vstPluginsInst.get(0).vstPlugins.StopProcess();
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
        } catch (InterruptedException ignored) {
        }
        vi.vstPlugins = ctx;
        vi.fileName = kv.getKey();
        vi.isInstrument = true;

        ctx.setBlockSize(512);
        ctx.setSampleRate(setting.getOutputDevice().getSampleRate());
        ctx.MainsChanged(true);
        ctx.startProcess();
        vi.effectName = ctx.PluginCommandStub.GetEffectName();
        vi.editor = true;

        if (vi.editor) {
            frmVST dlg = new frmVST();
            dlg.PluginCommandStub = ctx.PluginCommandStub;
            dlg.Show(vi);
            vi.vstPluginsForm = dlg;
        }

        vstPluginsInst.add(vi);
    }

    public void SetUpVstEffect() {
        for (int i = 0; i < setting.getvst().getVSTInfo().length; i++) {
            if (setting.getvst().getVSTInfo()[i] == null) continue;
            VstPlugin ctx = OpenPlugin(setting.getvst().getVSTInfo()[i].fileName);
            if (ctx == null) continue;

            VstInfo2 vi = new VstInfo2();
            vi.vstPlugins = ctx;
            vi.fileName = setting.getvst().getVSTInfo()[i].fileName;
            vi.key = setting.getvst().getVSTInfo()[i].key;

            ctx.setBlockSize(512);
            ctx.setSampleRate(setting.getOutputDevice().getSampleRate() / 1000.0f);
            ctx.MainsChanged(true);
            ctx.StartProcess();
            vi.effectName = ctx.GetEffectName();
            vi.power = setting.getvst().getVSTInfo()[i].power;
            vi.editor = setting.getvst().getVSTInfo()[i].editor;
            vi.location = setting.getvst().getVSTInfo()[i].location;
            vi.param = setting.getvst().getVSTInfo()[i].param;

            if (vi.editor) {
                frmVST dlg = new frmVST();
                dlg.PluginCommandStub = ctx.PluginCommandStub;
                dlg.Show(vi);
                vi.vstPluginsForm = dlg;
            }

            if (vi.param != null) {
                for (int p = 0; p < vi.param.length; p++) {
                    ctx.PluginCommandStub.SetParameter(p, vi.param[p]);
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
        setting.getvst().VSTInfo = null;
        List<VstInfo> vstlst = new ArrayList<VstInfo>();

        for (int i = 0; i < vstPlugins.size(); i++) {
            try {
                vstPlugins.get(i).vstPluginsForm.timer1.setEnabled(false);
                vstPlugins.get(i).location = vstPlugins.get(i).vstPluginsForm.Location;
                vstPlugins.get(i).vstPluginsForm.Close();
            } catch (Exception e) {
            }

            try {
                if (vstPlugins.get(i).vstPlugins != null) {
                    vstPlugins.get(i).vstPlugins.PluginCommandStub.EditorClose();
                    vstPlugins.get(i).vstPlugins.PluginCommandStub.StopProcess();
                    vstPlugins.get(i).vstPlugins.PluginCommandStub.MainsChanged(false);
                    int pc = vstPlugins.get(i).vstPlugins.PluginInfo.ParameterCount;
                    List<Float> plst = new ArrayList<Float>();
                    for (int p = 0; p < pc; p++) {
                        float v = vstPlugins.get(i).vstPlugins.PluginCommandStub.GetParameter(p);
                        plst.add(v);
                    }
                    vstPlugins.get(i).param = plst.toArray(new Float[0]);
                    vstPlugins.get(i).vstPlugins.Dispose();
                }
            } catch (Exception e) {
            }

            VstInfo vi = new VstInfo();
            vi.editor = vstPlugins.get(i).editor;
            vi.fileName = vstPlugins.get(i).fileName;
            vi.key = vstPlugins.get(i).key;
            vi.effectName = vstPlugins.get(i).effectName;
            vi.power = vstPlugins.get(i).power;
            vi.location = vstPlugins.get(i).location;
            vi.param = vstPlugins.get(i).param;

            if (!vstPlugins.get(i).isInstrument) vstlst.add(vi);
        }
        setting.getvst().setVSTInfo(vstlst.toArray(new VstInfo[0]));


        for (int i = 0; i < vstPluginsInst.size(); i++) {
            try {
                vstPluginsInst.get(i).vstPluginsForm.timer1.setEnabled(false);
                vstPluginsInst.get(i).location = vstPluginsInst.get(i).vstPluginsForm.Location;
                vstPluginsInst.get(i).vstPluginsForm.Close();
            } catch (Exception e) {
            }

            try {
                if (vstPluginsInst.get(i).vstPlugins != null) {
                    vstPluginsInst.get(i).vstPlugins.PluginCommandStub.EditorClose();
                    vstPluginsInst.get(i).vstPlugins.PluginCommandStub.StopProcess();
                    vstPluginsInst.get(i).vstPlugins.PluginCommandStub.MainsChanged(false);
                    int pc = vstPluginsInst.get(i).vstPlugins.PluginInfo.ParameterCount;
                    List<Float> plst = new ArrayList<Float>();
                    for (int p = 0; p < pc; p++) {
                        float v = vstPluginsInst.get(i).vstPlugins.PluginCommandStub.GetParameter(p);
                        plst.add(v);
                    }
                    vstPluginsInst.get(i).param = plst.toArray();
                    vstPluginsInst.get(i).vstPlugins.Dispose();
                }
            } catch (Exception e) {
            }

            VstInfo vi = new VstInfo();
            vi.editor = vstPluginsInst.get(i).editor;
            vi.fileName = vstPluginsInst.get(i).fileName;
            vi.key = vstPluginsInst.get(i).key;
            vi.effectName = vstPluginsInst.get(i).effectName;
            vi.power = vstPluginsInst.get(i).power;
            vi.location = vstPluginsInst.get(i).location;
            vi.param = vstPluginsInst.get(i).param;

        }

    }

    public void VST_Update(short[] buffer, int offset, int sampleCount) {
        if (vstPlugins.size() < 1 && vstPluginsInst.size() < 1) return;
        if (buffer == null || buffer.length < 1 || sampleCount == 0) return;

        try {
            //if (trdStopped) return;

            int blockSize = sampleCount / 2;

            for (int i = 0; i < vstPluginsInst.size(); i++) {
                VstInfo2 info2 = vstPluginsInst.get(i);
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

            for (int i = 0; i < vstPlugins.size(); i++) {
                VstInfo2 info2 = vstPlugins.get(i);
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
        }
    }


    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, byte prm2, int deltaFrames/* = 0*/) {
        if (model == EnmModel.RealModel) return;
        if (vstMidiOuts == null) return;
        if (num >= vstMidiOuts.size()) return;
        if (vstMidiOuts.get(num) == null) return;

        VstMidiEvent evt = new VstMidiEvent(
                deltaFrames
                , 0//noteLength
                , 0//noteOffset
                , new byte[] {cmd, prm1, prm2}
                , 0//detune
                , 0//noteOffVelocity
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
                , 0//noteLength
                , 0//noteOffset
                , new byte[] {cmd, prm1}
                , 0//detune
                , 0//noteOffVelocity
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
                , 0//noteLength
                , 0//noteOffset
                , data
                , 0//detune
                , 0//noteOffVelocity
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
                List<Byte> dat = new ArrayList<Byte>();
                for (int ch = 0; ch < 16; ch++) {
                    sendMIDIout(EnmModel.VirtualModel, i, new byte[] {(byte) (0xb0 + ch), 120, 0x00}, 0);
                    sendMIDIout(EnmModel.VirtualModel, i, new byte[] {(byte) (0xb0 + ch), 64, 0x00}, 0);
                }

            } catch (Exception e) {
            }
        }
    }


    public VstPlugin OpenPlugin(String pluginPath) {
        try {
            HostCommandStub hostCmdStub = new HostCommandStub(setting);
            hostCmdStub.PluginCalled += new EventHandler<>(HostCmdStub_PluginCalled);

            VstPlugin ctx = VstPlugin.Create(pluginPath, hostCmdStub);

            // add custom data to the context
            ctx.Set("PluginPath", pluginPath);
            ctx.Set("HostCmdStub", hostCmdStub);

            // actually open the plugin itself
            ctx.open();

            return ctx;
        } catch (Exception e) {
            Log.forcedWrite(e);
            //JOptionPane.showConfirmDialog(this, e.toString(), Text, JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
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
        ret.effectName = ctx.GetEffectName();
        ret.productName = ctx.getProductString();
        ret.vendorName = ctx.getVendorString();
        ret.programName = ctx.getProgramName();
        ret.fileName = filename;
        ret.midiInputChannels = ctx.getNumInputs();
        ret.midiOutputChannels = ctx.getNumOutputs();
        ctx.close();

        return ret;
    }

    public Boolean addVSTeffect(String fileName) {
        VstPlugin ctx = OpenPlugin(fileName);
        if (ctx == null) return false;

        //Stop();

        VstInfo2 vi = new VstInfo2();
        vi.vstPlugins = ctx;
        vi.fileName = fileName;
        vi.key = String.valueOf(System.currentTimeMillis());
        try { Thread.sleep(1); } catch (InterruptedException ignored) {}

        ctx.setBlockSize(512);
        ctx.setSampleRate(setting.getOutputDevice().getSampleRate());
        ctx.PluginCommandStub.MainsChanged(true);
        ctx.StartProcess();
        vi.effectName = ctx.PluginCommandStub.GetEffectName();
        vi.power = true;
        ctx.getParameterProperties(0);


        frmVST dlg = new frmVST();
        dlg.PluginCommandStub = ctx.PluginCommandStub;
        dlg.Show(vi);
        vi.vstPluginsForm = dlg;
        vi.editor = true;

        vstPlugins.add(vi);

        List<VstInfo> lvi = new ArrayList<VstInfo>();
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
        setting.getvst().setVSTInfo(lvi.toArray());

        return true;
    }

    public Boolean delVSTeffect(String key) {
        if (key == "") {
            for (int i = 0; i < vstPlugins.size(); i++) {
                try {
                    if (vstPlugins.get(i).vstPlugins != null) {
                        vstPlugins.get(i).vstPluginsForm.timer1.stop();
                        vstPlugins.get(i).location = vstPlugins.get(i).vstPluginsForm.getLocation();
                        vstPlugins.get(i).vstPluginsForm.setVisible(false);
                        vstPlugins.get(i).vstPlugins.editClose();
                        vstPlugins.get(i).vstPlugins.StopProcess();
                        vstPlugins.get(i).vstPlugins.MainsChanged(false);
                        vstPlugins.get(i).vstPlugins.close();
                    }
                } catch (Exception e) {
                }
            }
            vstPlugins.clear();
            setting.getvst().setVSTInfo(new VstInfo[0]);
        } else {
            int ind = -1;
            for (int i = 0; i < vstPlugins.size(); i++) {
                //if (vstPlugins.get(i).fileName == fileName)
                if (vstPlugins.get(i).key == key) {
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
                }
                vstPlugins.remove(ind);
            }

            List<VstInfo> nvst = new ArrayList<>();
            for (VstInfo vi : setting.getvst().getVSTInfo()) {
                if (vi.key == key) continue;
                nvst.add(vi);
            }
            setting.getvst().setVSTInfo(nvst.toArray(new VstInfo[0]));
        }

        return true;
    }


    /**
     * //The HostCommandStub class represents the part of the host that a plugin can call.
     */
    public class HostCommandStub implements IVstHostCommandStub {
        private Setting setting;

        public HostCommandStub(Setting setting) {
            this.setting = setting;
        }

        /**
         * //Raised when one of the methods instanceof called.
         */
        public EventHandler<PluginCalledEventArgs> PluginCalled;

        private void RaisePluginCalled(String message) {
            EventHandler<PluginCalledEventArgs> handler = PluginCalled;

            if (handler != null) {
                handler(this, new PluginCalledEventArgs(message));
            }
        }

        // // #region IVstHostCommandsStub Members

        /* TODO */
        public IVstPluginContext getPluginContext() {
            return null;
        }

        public void setPluginContext(IVstPluginContext value) {
        }

        /* */
        public Boolean BeginEdit(int index) {
            RaisePluginCalled("BeginEdit(" + index + ")");

            return false;
        }

        /* */
        public VstCanDoResult CanDo(String cando) {
            RaisePluginCalled("CanDo(" + cando + ")");
            return VstCanDoResult.Unknown;
        }

        /* */
        public Boolean CloseFileSelector(VstFileSelect fileSelect) {
            RaisePluginCalled("CloseFileSelector(" + fileSelect.Command + ")");
            return false;
        }

        /* */
        public Boolean EndEdit(int index) {
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
        public Boolean IoChanged() {
            RaisePluginCalled("IoChanged()");
            return false;
        }

        /* */
        public Boolean OpenFileSelector(VstFileSelect fileSelect) {
            RaisePluginCalled("OpenFileSelector(" + fileSelect.Command + ")");
            return false;
        }

        /* */
        public Boolean ProcessEvents(VstEvent[] events) {
            RaisePluginCalled("ProcessEvents(" + events.length + ")");
            return false;
        }

        /* */
        public Boolean SizeWindow(int width, int height) {
            RaisePluginCalled("SizeWindow(" + width + ", " + height + ")");
            return false;
        }

        /* */
        public Boolean UpdateDisplay() {
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
    public class PluginCalledEventArgs extends EventObject {
        /**
         * Constructs a new instance with a "message".
         * @param message
         */
        public PluginCalledEventArgs(String message) {
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

    public class VstInfo2 extends VstInfo {
        public VstPlugin vstPlugins = null;
        public frmVST vstPluginsForm = null;

        // 実際にVSTiかどうかは問わない
        public Boolean isInstrument = false;
        public List<VstMidiEvent> lstEvent = new ArrayList<>();

        public void AddMidiEvent(VstMidiEvent evt) {
            lstEvent.add(evt);
        }
    }
}
