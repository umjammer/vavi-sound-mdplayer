package mdplayer.driver.zgm.zgmChip;

import java.util.Map;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.driver.zgm.EnmZGMDevice;
import mdplayer.driver.zgm.ZgmDriver;


public class Conductor extends ZgmChip {

    public Conductor(ChipRegister chipRegister, Setting setting, byte[] vgmBuf) {
        super(2);

        this.chipRegister = chipRegister;
        this.setting = setting;
        this.vgmBuf = vgmBuf;

        use = true;
        device = EnmZGMDevice.Conductor;
        name = "CONDUCTOR";
        model = EnmModel.VirtualModel;
        number = 0;
        hosei = 0;
    }

    @Override
    public void setUp(int chipIndex, int dataPos, Map<Integer, ZgmDriver.RefRunnable<Byte, Integer>> cmdTable) {
        super.setUp(chipIndex, dataPos, cmdTable);

        cmdTable.remove(defineInfo.commandNo);
        cmdTable.put(defineInfo.commandNo, Conductor::sendPort0);
    }

    private static void sendPort0(byte od, int vgmAdr) {
        vgmAdr += 3;
    }
}
