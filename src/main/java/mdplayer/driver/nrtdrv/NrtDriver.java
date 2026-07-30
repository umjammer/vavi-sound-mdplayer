package mdplayer.driver.nrtdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * NRTDRV
 *
 * @author kumatan
 */
public class NrtDriver extends BaseDriver {

    private static final Logger logger = getLogger(NrtDriver.class.getName());

    private final NRTDRV nrtdrv;

    public NrtDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.nrtdrv = new NRTDRV();
        nrtdrv.ctcStep = 4000000.0f / setting.getOutputDevice().getSampleRate();
        nrtdrv.ctc1Step = 4000000.0f / setting.getOutputDevice().getSampleRate();
        nrtdrv.ym2151WriteV = (i, a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(i, 0, a, d, EnmModel.VirtualModel, 0, 0);
        nrtdrv.ym2151WriteR = (i, a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(i, 0, a, d, EnmModel.RealModel, plugin.chipRegister.chip(Ym2151Chip.class).corrections[0], 0);
        nrtdrv.ay8910WriteV = (a, d) -> plugin.chipRegister.chip(Ay8910Chip.class).write(0, a, d, EnmModel.VirtualModel);
        nrtdrv.loop = l -> curLoop = l;
        nrtdrv.isRealModel = model == EnmModel.RealModel;
    }

    public NrtDriver() {
        this(null); // gross
    }

    public int checkUseChip(byte[] vgmBuf) {
        return nrtdrv.checkUseChip(vgmBuf);
    }

    public void call(int cmdNo) {
        nrtdrv.call(cmdNo);
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        metaData = getMetaData(dataBuf, 42);
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;

        try {
            nrtdrv.ram = new byte[65536];
            Arrays.fill(nrtdrv.ram, (byte) 0);

            System.arraycopy(this.dataBuf, 0, nrtdrv.ram, 0x4000, Math.min(this.dataBuf.length, 0xfeff - 0x4000));
        } catch (Exception ex) {
            throw new IllegalStateException("Driver initialization failed.", ex);
        }

        plugin.chipRegister.chip(Ym2151Chip.class).setCorrection(model, 4000000);

        // Initializing the Driver
        nrtdrv.call(0);

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2151Chip.class).sendData((byte) 0, model);
            plugin.chipRegister.chip(Ym2151Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2151Chip.class).sendData((byte) 1, model);
            plugin.chipRegister.chip(Ym2151Chip.class).setSyncWait((byte) 1, 1);
        }
    }

    /**
     * @param args 0: index
     */
    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        int[] index = {(int) args[0]};

        MetaData md = new MetaData();
        md.set(Tag.Title, Common.getNRDString(buf, index));
        md.set(Tag.TitleJ, Common.getNRDString(buf, index));
        md.set(Tag.Composer, Common.getNRDString(buf, index));
        md.set(Tag.ComposerJ, md.getFirst(Tag.Composer));
        md.set(Tag.Maker, Common.getNRDString(buf, index));
        md.set(Tag.Note, Common.getNRDString(buf, index));

        if ((buf[2] & 0x08) != 0) {
            int adr = index[0];
            while (buf[adr] != (byte) 0xff || buf[adr + 1] != (byte) 0xff) {
                int cnt = (buf[adr] & 0xff) + (buf[adr + 1] & 0xff) * 0x100;
                int[] sAdr = new int[] {(buf[adr + 2] & 0xff) + (buf[adr + 3] & 0xff) * 0x100};
                String msg = Common.getNRDString(buf, sAdr);
                md.set(Tag.Lyric, cnt + "," + sAdr[0] + "," + msg);
                adr += 4;
            }
        }

        if ((((buf[2] & (byte) 0x80) != 0) && buf[41] != 2) || (buf[2] & 0x80) == 0) {
            md.set(Tag.Note, "!!Warning!! This data version instanceof older/newer.");
        }

        int r = nrtdrv.checkUseChip(buf);

        switch (r) {
            case 0:
                break;
            case 1:
            case 2:
                md.set(Tag.Chip, "YM2151");
                break;
            case 3:
                md.set(Tag.Chip, "YM2151x2");
                break;
            case 4:
                md.set(Tag.Chip, "AY8910");
                break;
            case 5:
            case 6:
                md.set(Tag.Chip, "YM2151, AY8910");
                break;
            case 7:
                md.set(Tag.Chip, "YM2151x2, AY8910");
                break;
        }

        return md;
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                    frameCounter++;

                    nrtdrv.oneFrameMain();
                } else {
                    frameCounter++;
                }
            }
            stopped = !nrtdrv.isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    @Override
    public long getDriverCounter() {
        return nrtdrv.work.totalCount;
    }

    @Override
    public long whichCounter(long real, long virtual) {
        return Math.max(virtual, real);
    }

    @Override
    public String getName() {
        return "NRTDRV";
    }
}
