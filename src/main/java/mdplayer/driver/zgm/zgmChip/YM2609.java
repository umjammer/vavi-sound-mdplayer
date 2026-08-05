package mdplayer.driver.zgm.zgmChip;

import java.util.Map;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.driver.zgm.EnmZGMDevice;
import mdplayer.driver.zgm.ZgmDriver;


public class YM2609 extends ZgmChip {

    public YM2609(ChipRegister chipRegister, Setting setting, byte[] vgmBuf) {
        super(12 + 6 + 12 + 6 + 3);
        this.chipRegister = chipRegister;
        this.setting = setting;
        this.vgmBuf = vgmBuf;

        use = true;
        device = EnmZGMDevice.YM2609;
        name = "YM2609";
        model = EnmModel.VirtualModel;
        number = 0;
        hosei = 0;
    }

    @Override
    public void setUp(int chipIndex, int dataPos, Map<Integer, ZgmDriver.RefRunnable<Byte, Integer>> cmdTable) {
        super.setUp(chipIndex, dataPos, cmdTable);

        cmdTable.remove(defineInfo.commandNo);
        cmdTable.put(defineInfo.commandNo, YM2609::sendPort0);

        cmdTable.remove(defineInfo.commandNo + 1);
        cmdTable.put(defineInfo.commandNo + 1, YM2609::sendPort1);

        cmdTable.remove(defineInfo.commandNo + 2);
        cmdTable.put(defineInfo.commandNo + 2, YM2609::sendPort2);

        cmdTable.remove(defineInfo.commandNo + 3);
        cmdTable.put(defineInfo.commandNo + 3, YM2609::sendPort3);
    }

    private static void sendPort0(byte od, int vgmAdr) {
        // chipRegister.YM2609SetRegister(od, Audio.DriverSeqCounter, Index, 0,
        // dataBuf[vgmAdr + 1], dataBuf[vgmAdr + 2]);
        vgmAdr += 3;
    }

    private static void sendPort1(byte od, int vgmAdr) {
        // chipRegister.YM2609SetRegister(od, Audio.DriverSeqCounter, Index, 1,
        // dataBuf[vgmAdr + 1], dataBuf[vgmAdr + 2]);
        vgmAdr += 3;
    }

    private static void sendPort2(byte od, int vgmAdr) {
        // chipRegister.YM2609SetRegister(od, Audio.DriverSeqCounter, Index, 2,
        // dataBuf[vgmAdr + 1], dataBuf[vgmAdr + 2]);
        vgmAdr += 3;
    }

    private static void sendPort3(byte od, int vgmAdr) {
        // chipRegister.YM2609SetRegister(od, Audio.DriverSeqCounter, Index, 3,
        // dataBuf[vgmAdr + 1], dataBuf[vgmAdr + 2]);
        vgmAdr += 3;
    }
}
