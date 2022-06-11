package mdplayer;

import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Common.EnmRealModel;


public class ChipInf {
    /**
     * ID Primary:0 / Secondary:1
     */
    public int ID = 0;

    /**
     * ChipType2
     */
    public EnmChip type = EnmChip.Unuse;

    /**
     * model Virtual / Real
     */
    public EnmModel model = EnmModel.VirtualModel;

    /**
     * Real model type
     */
    public EnmRealModel mType = EnmRealModel.unknown;
}
