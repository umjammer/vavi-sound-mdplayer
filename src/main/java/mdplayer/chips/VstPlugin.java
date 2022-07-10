package mdplayer.chips;

/**
 * VstPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class VstPlugin extends Plugin {

//    public static List<VstMng.VstInfo2> getVSTInfos() {
//        return vstMng.getVSTInfos();
//    }
//
//    public static VstInfo getVSTInfo(String filename) {
//        return vstMng.getVSTInfo(filename);
//    }
//
//    public static boolean addVSTeffect(String fileName) {
//        return vstMng.addVSTeffect(fileName);
//    }
//
//    public static boolean delVSTeffect(String key) {
//        return vstMng.delVSTeffect(key);
//    }

    @Override
    void init() {
//        Log.forcedWrite("Audio:Init:VST:STEP 01");
//
//        vstMng.vstparse();
//
//        Log.forcedWrite("Audio:Init:VST:STEP 02"); // Load VST instrument
//
//        // 複数のmidioutの設定から必要なVSTを絞り込む
//        Map<String, Integer> dicVst = new HashMap<>();
//        if (setting.getMidiOut().getMidiOutInfos() != null) {
//            for (MidiOutInfo[] aryMoi : setting.getMidiOut().getMidiOutInfos()) {
//                if (aryMoi == null) continue;
//                Map<String, Integer> dicVst2 = new HashMap<>();
//                for (MidiOutInfo moi : aryMoi) {
//                    if (!moi.isVST) continue;
//                    if (dicVst2.containsKey(moi.fileName)) {
//                        dicVst2.put(moi.fileName, dicVst2.get(moi.fileName + 1));
//                        continue;
//                    }
//                    dicVst2.put(moi.fileName, 1);
//                }
//
//                for (Map.Entry<String, Integer> kv : dicVst2.entrySet()) {
//                    if (dicVst.containsKey(kv.getKey())) {
//                        if (dicVst.get(kv.getKey()) < kv.getValue()) {
//                            dicVst.put(kv.getKey(), kv.getValue());
//                        }
//                        continue;
//                    }
//                    dicVst.put(kv.getKey(), kv.getValue());
//                }
//            }
//        }
//
//        for (Map.Entry<String, Integer> kv : dicVst.entrySet()) {
//            for (int i = 0; i < kv.getValue(); i++)
//                vstMng.SetUpVstInstrument(kv);
//        }
//
//        if (setting.getVst() != null && setting.getVst().getVSTInfo() != null) {
//            Log.forcedWrite("Audio:Init:VST:STEP 03"); // Load VST Effect
//            vstMng.SetUpVstEffect();
//        }
    }
}
