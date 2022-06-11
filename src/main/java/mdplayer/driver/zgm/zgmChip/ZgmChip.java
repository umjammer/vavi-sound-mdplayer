
package mdplayer.driver.zgm.zgmChip;

import java.util.Map;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.zgm.Zgm;


public abstract class ZgmChip extends Chip {

    protected ChipRegister chipRegister;

    protected Setting setting;

    protected byte[] vgmBuf;

    public String name;

    public Zgm.DefineInfo defineInfo;

    public ZgmChip(int ch) {
        super(ch);

    }

    public void setup(int chipIndex, int dataPos, Map<Integer, Zgm.RefRunnable<Byte, Integer>> cmdTable) {
        this.index = chipIndex;
        defineInfo = new Zgm.DefineInfo();
        defineInfo.length = vgmBuf[dataPos + 0x03];
        defineInfo.chipIdentNo = Common.getLE32(vgmBuf, dataPos + 0x4);
        defineInfo.commandNo = (int) Common.getLE16(vgmBuf, dataPos + 0x8);
        defineInfo.clock = (int) Common.getLE32(vgmBuf, dataPos + 0xa);
        defineInfo.option = null;
        if (defineInfo.length > 14) {
            defineInfo.option = new byte[defineInfo.length - 14];
            for (int j = 0; j < defineInfo.length - 14; j++) {
                defineInfo.option[j] = vgmBuf[dataPos + 0x0e + j];
            }
        }

        dataPos += defineInfo.length;
    }
}
