package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdplayer.MidiOutInfo;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import mdplayer.vst.VstInfo;
import mdplayer.vst.VstMng;
import mdplayer.vst.VstMng.VstInfo2;

import static java.lang.System.getLogger;


/**
 * VstPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class VstPlugin implements Plugin {

    private static final Logger logger = getLogger(VstPlugin.class.getName());

    public int vstDelta = 0;

    private final VstMng vstMng = new VstMng();

    public List<VstInfo2> getVSTInfos() {
        return vstMng.getVSTInfos();
    }

    public VstInfo getVSTInfo(String filename) {
        return vstMng.getVSTInfo(filename);
    }

    public boolean addVSTeffect(String fileName) {
        return vstMng.addVSTeffect(fileName);
    }

    public boolean delVSTeffect(String key) {
        return vstMng.delVSTeffect(key);
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        logger.log(Level.TRACE, "Audio:Init:VST:STEP 01");

        vstMng.vstparse();

        logger.log(Level.TRACE, "Audio:Init:VST:STEP 02"); // Load VST instrument

        // Narrow down the VST you need from multiple midiout settings
        Map<String, Integer> dicVst = new HashMap<>();
        if (setting.getMidiOut().getMidiOutInfos() != null) {
            for (MidiOutInfo[] aryMoi : setting.getMidiOut().getMidiOutInfos()) {
                if (aryMoi == null) continue;
                Map<String, Integer> dicVst2 = new HashMap<>();
                for (MidiOutInfo moi : aryMoi) {
                    if (!moi.isVST) continue;
                    if (dicVst2.containsKey(moi.fileName)) {
                        dicVst2.put(moi.fileName, dicVst2.get(moi.fileName + 1));
                        continue;
                    }
                    dicVst2.put(moi.fileName, 1);
                }

                for (Map.Entry<String, Integer> kv : dicVst2.entrySet()) {
                    if (dicVst.containsKey(kv.getKey())) {
                        if (dicVst.get(kv.getKey()) < kv.getValue()) {
                            dicVst.put(kv.getKey(), kv.getValue());
                        }
                        continue;
                    }
                    dicVst.put(kv.getKey(), kv.getValue());
                }
            }
        }

        for (Map.Entry<String, Integer> kv : dicVst.entrySet()) {
            for (int i = 0; i < kv.getValue(); i++)
                vstMng.SetUpVstInstrument(kv);
        }

        if (setting.getVst() != null && setting.getVst().getVSTInfo() != null) {
            logger.log(Level.TRACE, "Audio:Init:VST:STEP 03"); // Load VST Effect
            vstMng.SetUpVstEffect();
        }
    }

    @Override
    public void close() {

    }

    public void update(short[] buffer, int offset, int sampleCount) {
        vstMng.VST_Update(buffer, offset, sampleCount);
    }
}
