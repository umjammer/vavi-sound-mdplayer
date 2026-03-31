package mdplayer.driver.mndrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import mdsound.instrument.MPcmPPInst;
import mdsound.instrument.X68kMPcmInst;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * @author kumatan
 */
public class MnDriver extends BaseDriver {

    private static final Logger logger = getLogger(MnDriver.class.getName());

    private final MnDrv mndrv;

    public MnDriver() {
        this.mndrv = new MnDrv();
        mndrv.ym2608Write = (c, p, a, d) -> plugin.chipRegister.chip(Ym2608Chip.class).write(c, p, a, d, model);
        mndrv.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).ym2151Hosei[0], 0);
        mndrv.stop = () -> stopped = true;
    }

    public void setExtendFile(List<Tuple<String,byte[]>> extendFile) {
        mndrv.extendFile = extendFile;
    }

    public void setMpcm(X68kMPcmInst mpcm) {
        mndrv.mpcm = mpcm;
        mndrv.mpcmType = 0;
    }

    public void setMpcmpp(MPcmPPInst mpcmpp) {
        mndrv.mpcmpp = mpcmpp;
        mndrv.mpcmType = 1;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        gd3 = getGD3Info(vgmBuf);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

        plugin.chipRegister.chip(Ym2151Chip.class).setYm2151Hosei(model, 4000000);

        mndrv.init(vgmBuf, model == EnmModel.RealModel);
    }

    @Override
    public void processOneFrame() {
        // For debugging
        //if (model == enmModel.RealModel) return;

        if (mndrv.mm.mm == null) {
            return;
        }

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                mndrv.clock();
                counter++;
                vgmFrameCounter++;
            }

            if ((mndrv.mm.readByte(mndrv.reg.a6 + Dw.DRV_STATUS) & 0x20) != 0) {
                stopped = true;
            }
            vgmCurLoop = mndrv.mm.readShort(mndrv.reg.a6 + Dw.LOOP_COUNTER) & 0xffff;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Gd3 gd3 = new Gd3();

        int i = (buf[6] & 0xff) * 0x100 + (buf[7] & 0xff);
        List<Byte> lst = new ArrayList<>();
        while (i < buf.length && buf[i] != 0x0 && i + 1 < buf.length && buf[i + 1] != 0x0) {
            lst.add(buf[i]);
            i++;
        }
        String n = new String(ByteUtil.toByteArray(lst), charset);
        gd3.trackName = n;
        gd3.trackNameJ = n;

        return gd3;
    }

    @Override
    public long getDriverCounter() {
        return counter;
    }
}
