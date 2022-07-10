package mdplayer.chips;

import java.util.List;

import mdplayer.Common;
import mdplayer.Setting;


/**
 * RealChipPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class RealChipPlugin extends Plugin {

    public static void realChipClose() {
//        if (SoundChip.realChip != null) {
//            SoundChip.realChip.close();
//        }
    }

    public static List<Setting.ChipType2> getRealChipList(Common.EnmRealChipType scciType) {
//        if (SoundChip.realChip == null) return null;
//        return SoundChip.realChip.GetRealChipList(scciType);
        return null;
    }

    @Override
    void init() {
//        if (SoundChip.realChip == null && !getemuOnly()) {
//            Log.forcedWrite("Audio:Init:STEP 04");
//            SoundChip.realChip = new RealChip(!setting.getUnuseRealChip());
//        }
//
//        if (SoundChip.realChip != null) {
//            for (int i = 0; i < 2; i++) {
//                SoundChip.scYM2612[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2612Type()[i], 0);
//                if (SoundChip.scYM2612[i] != null) SoundChip.scYM2612[i].init();
//                SoundChip.scSN76489[i] = SoundChip.realChip.GetRealChip(Audio.setting.getSN76489Type()[i], 0);
//                if (SoundChip.scSN76489[i] != null) SoundChip.scSN76489[i].init();
//                SoundChip.scYM2608[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2608Type()[i], 0);
//                if (SoundChip.scYM2608[i] != null) SoundChip.scYM2608[i].init();
//                SoundChip.scYM2151[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2151Type()[i], 0);
//                if (SoundChip.scYM2151[i] != null) SoundChip.scYM2151[i].init();
//                SoundChip.scYM2203[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2203Type()[i], 0);
//                if (SoundChip.scYM2203[i] != null) SoundChip.scYM2203[i].init();
//                SoundChip.scAY8910[i] = SoundChip.realChip.GetRealChip(Audio.setting.getAY8910Type()[i], 0);
//                if (SoundChip.scAY8910[i] != null) SoundChip.scAY8910[i].init();
//                SoundChip.scK051649[i] = SoundChip.realChip.GetRealChip(Audio.setting.getK051649Type()[i], 0);
//                if (SoundChip.scK051649[i] != null) SoundChip.scK051649[i].init();
//                SoundChip.scYM2413[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2413Type()[i], 0);
//                if (SoundChip.scYM2413[i] != null) SoundChip.scYM2413[i].init();
//                SoundChip.scYM3526[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM3526Type()[i], 0);
//                if (SoundChip.scYM3526[i] != null) SoundChip.scYM3526[i].init();
//                SoundChip.scYM3812[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM3812Type()[i], 0);
//                if (SoundChip.scYM3812[i] != null) SoundChip.scYM3812[i].init();
//                SoundChip.scYMF262[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYMF262Type()[i], 0);
//                if (SoundChip.scYMF262[i] != null) SoundChip.scYMF262[i].init();
//                SoundChip.scYM2610[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 0);
//                if (SoundChip.scYM2610[i] != null) SoundChip.scYM2610[i].init();
//                SoundChip.scYM2610EA[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 1);
//                if (SoundChip.scYM2610EA[i] != null) SoundChip.scYM2610EA[i].init();
//                SoundChip.scYM2610EB[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 2);
//                if (SoundChip.scYM2610EB[i] != null) SoundChip.scYM2610EB[i].init();
//                SoundChip.scSEGAPCM[i] = SoundChip.realChip.GetRealChip(Audio.setting.getSEGAPCMType()[i], 0);
//                if (SoundChip.scSEGAPCM[i] != null) SoundChip.scSEGAPCM[i].init();
//                SoundChip.scC140[i] = SoundChip.realChip.GetRealChip(Audio.setting.getC140Type()[i], 0);
//                if (SoundChip.scC140[i] != null) SoundChip.scC140[i].init();
//            }
//        }
    }
}
