package mdplayer.driver.zgm.zgmChip;

import java.util.Map;

import mdplayer.ChipRegister;
import mdplayer.Setting;
import mdplayer.driver.zgm.ZgmDriver;
import vavi.util.ByteUtil;


public abstract class ZgmChip extends Chip {

    protected ChipRegister chipRegister;

    protected Setting setting;

    protected byte[] vgmBuf;

    public String name;

    public ZgmDriver.DefineInfo defineInfo;

    public ZgmChip(int ch) {
        super(ch);

    }

    public void setUp(int chipIndex, int dataPos, Map<Integer, ZgmDriver.RefRunnable<Byte, Integer>> cmdTable) {
        this.index = chipIndex;
        defineInfo = new ZgmDriver.DefineInfo();
        defineInfo.length = vgmBuf[dataPos + 0x03];
        defineInfo.chipIdentNo = ByteUtil.readLeInt(vgmBuf, dataPos + 0x4);
        defineInfo.commandNo = ByteUtil.readLeShort(vgmBuf, dataPos + 0x8);
        defineInfo.clock = ByteUtil.readLeInt(vgmBuf, dataPos + 0xa);
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
