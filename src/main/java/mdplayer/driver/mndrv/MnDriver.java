package mdplayer.driver.mndrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.emu.nise68.IMemory;
import mdplayer.emu.nise68.IRegister;
import mdplayer.lib.mndrv.MnDrv;
import mdplayer.lib.mndrv.MnWork.Dw;
import mdplayer.lib.mndrv.Reg;
import mdplayer.lib.zms.Zms.MPCMSt;
import vavi.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.MPcmChip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.zms.Zms.MPcmInterface;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * MNDRV X68000
 *
 * @author kumatan
 */
public class MnDriver extends BaseDriver {

    private static final Logger logger = getLogger(MnDriver.class.getName());

    private final MnDrv mndrv;

    public MnDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.mndrv = new MnDrv();
        mndrv.ym2608Write = (c, p, a, d) -> plugin.chipRegister.chip(Ym2608Chip.class).write(c, p, a, d, model);
        mndrv.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).corrections[0], 0);
        mndrv.stop = () -> stopped = true;
        mndrv.mpcm = new MPcmInterface() {
            @Override
            public void keyOn(int ch) {
                plugin.chipRegister.chip(MPcmChip.class).keyOn(0, ch);
            }

            @Override
            public void keyOff(int ch) {
                plugin.chipRegister.chip(MPcmChip.class).keyOff(0, ch);
            }

            @Override
            public void writePcm(int ch, MPCMSt[] pcm, IMemory mem, IRegister reg, int n) {
                pcm[ch].fill(mem, reg);
                pcm[ch].fill(plugin.chipRegister.chip(MPcmChip.class).getBaseRate(0));
                plugin.chipRegister.chip(MPcmChip.class).writePcm(0, ch, mem.getMemory(), pcm[ch].type, pcm[ch].orig, pcm[ch].adrs_ptr, pcm[ch].size, pcm[ch].start, pcm[ch].end, pcm[ch].count, reg instanceof Reg, pcm[ch].frq, n);
                //nise68.dumpMemory((int) ptr.adrs_ptr, (int) (ptr.adrs_ptr + ptr.size));
            }

            @Override
            public void setFreq(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setFreq(0, ch, value);
            }

            @Override
            public void setPitch(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setPitch(0, ch, value);
            }

            @Override
            public void setVol(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setVol(0, ch, value);
            }

            @Override
            public void setPan(int ch, int value) {
                plugin.chipRegister.chip(MPcmChip.class).setPan(0, ch, value);
            }

            @Override
            public void reset() {
                plugin.chipRegister.chip(MPcmChip.class).reset(0);
            }

            @Override
            public void setVolTable(int type) {
            }

            @Override
            public void setVolTable(int type, int[] vtbl) {
                plugin.chipRegister.chip(MPcmChip.class).setVolTable(0, type, vtbl);
            }
        };
    }

    public MnDriver() {
        this(null); // gross
    }

    public void setExtendFile(List<Tuple<String,byte[]>> extendFile) {
        mndrv.extendFile = extendFile;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        metaData = retrieveMetaData(dataBuf);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;

        plugin.chipRegister.chip(Ym2151Chip.class).setCorrection(model, 4000000);

        mndrv.init(dataBuf, model == EnmModel.RealModel, Common.VGMProcSampleRate);
    }

    @Override
    public void processOneFrame() {
        // For debugging
        //if (model == enmModel.RealModel) return;

        if (mndrv.mm.mm == null) {
            return;
        }

        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                mndrv.clock();
                counter++;
                frameCounter++;
            }

            if ((mndrv.mm.readByte(mndrv.reg.a6 + Dw.DRV_STATUS) & 0x20) != 0) {
                stopped = true;
            }
            curLoop = mndrv.mm.readShort(mndrv.reg.a6 + Dw.LOOP_COUNTER) & 0xffff;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();

        // the title lives wherever the pointer at $0c says, which is only coincidentally the
        // header size at $06 -- v2 data puts it after the tone data and read the wrong way round
        // decodes voice bytes as a title
        int headerSize = (buf[6] & 0xff) * 0x100 + (buf[7] & 0xff);
        int i = headerSize > 0x0c ? ByteUtil.readBeInt(buf, 0x0c) : 0;
        if (i <= 0 || i >= buf.length) i = headerSize;
        List<Byte> lst = new ArrayList<>();
        while (i < buf.length && buf[i] != 0x0 && i + 1 < buf.length && buf[i + 1] != 0x0) {
            lst.add(buf[i]);
            i++;
        }
        String n = new String(ByteUtil.toByteArray(lst), charset);
        md.set(Tag.Title, n);
        md.set(Tag.TitleJ, n);

        return md;
    }

    @Override
    public long getDriverCounter() {
        return counter;
    }

    @Override
    public String getName() {
        return "MNDRV";
    }
}
