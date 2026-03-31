package mdplayer.driver.mxdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * MxDriver
 *
 * @author kumatan
 */
public class MxDriver extends BaseDriver {

    private static final Logger logger = getLogger(MxDriver.class.getName());

    private final MXDRV mxdrv;

    public MxDriver() {
        this.mxdrv = new MXDRV();
        mxdrv.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).ym2151Hosei[0], vgmFrameCounter);
        mxdrv.clock = this::clock;
        mxdrv.counter = l -> totalCounter = l;
    }

    public void setExtendFile(Tuple<String,byte[]> extendFile) {
        mxdrv.extendFile = extendFile;
    }

    public void setPcm8type(int type) {
        mxdrv.pcm8type = type;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Gd3 gd3 = new Gd3();

        List<Byte> lst = new ArrayList<>();
        int i = 0;
        while (i < buf.length && (buf[i] != 0xd && buf[i] != 0xa)) {
            lst.add(buf[i]);
            i++;
        }
        String n = new String(ByteUtil.toByteArray(lst), charset);
        gd3.trackName = n;
        gd3.trackNameJ = n;
        byte[][] mdx = new byte[1][];
        int[] mdxSize = new int[1];
        String[] pdxFileName = new String[1];
        mxdrv.makeMdxBuf(buf, mdx, mdxSize, pdxFileName);

        return gd3;
    }

    /**
     * @param args 0: Pcm8PPInst, 1: X68kYm2151Inst
     */
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

        mxdrv.init(
                (Pcm8PPInst) args[0],
                (X68kYm2151Inst) args[1],
                vgmBuf,
                model == EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate()
        );
    }

    private final short[] dummyBuf = new short[2];

    @Override
    public void processOneFrame() {
        render(dummyBuf, 0, 2);
    }

    private void clock(Runnable timer, boolean firstFlg) {
        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    timer.run();
                    if (firstFlg) {
                        counter++;
                        vgmFrameCounter++;
                    }
                } else {
                    if (firstFlg)
                        vgmFrameCounter++;
                }
            }

            mxdrv.MXDRV_MeasurePlayTime_OPMINT();
            vgmCurLoop = mxdrv.loopCount;
            if (mxdrv.terminatePlay) {
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        plugin.mds.setIncFlag();
//        vstDelta = 0;
        int cnt;
        for (int i = 0; i < sampleCount; i += 2) {
            cnt = mxdrv.render(buffer, offset + i, 1);
            plugin.mds.update(buffer, offset + i, 2, null);
        }
        //cnt = (int) ((MxDriver) driverVirtual).render(buffer, offset , sampleCount);
        //mds.update(buffer, offset , sampleCount, null);
        return sampleCount;
    }
}
