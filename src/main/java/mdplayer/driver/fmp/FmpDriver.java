package mdplayer.driver.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.fmp.nise98.FileTemp;
import mdplayer.plugin.BasePlugin;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * FMP
 *
 * system properties:
 * <li>{@code mdplayer.fmp.dir} ... location for fmp.com </li>
 * <li>{@code mdplayer.fmp.pvi} ... location for (.pvi) pcm files </li>
 *
 * @author kumatan
 */
public class FmpDriver extends BaseDriver {

    private static final Logger logger = getLogger(FmpDriver.class.getName());

    private final FMP fmp;

    public FmpDriver() {
        fmp = new FMP();

        fmp.blockWrite = b -> this.isDataBlock = b;
        fmp.setPPZ8PCMData = this::setPPZ8PCMData;
        fmp.setPPZ8Data = this::setPPZ8Data;
        fmp.opnaWrite = this::opnaWrite;
    }

    public void setFileTemp(FileTemp ft) {
        fmp.ft = ft;
    }

    public void setSearchPath(String searchPath) {
        fmp.setSearchPath(searchPath);
    }

    public void setPlayingFileName(String playingFileName) {
        fmp.playingFileName = playingFileName;
    }

    public void setPlayingArcFileName(String playingArcFileName) {
        fmp.playingArcFileName = playingArcFileName;
    }

    /** before using ths method, you must set playingFileName by {@link #setPlayingFileName} */
    public void compile() {
        fmp.compile();
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        Vgm.Gd3 ret = new Vgm.Gd3();

        try {
            if (buf != null && buf.length > 2) {
                int[] ptr = new int[] {(ByteUtil.readLeShort(buf, 0) & 0xffff) + 4}; // 4 'FMC'+version(1byte)
                String comment = Common.getNRDString(buf, /* ref */ ptr);
                ret.trackName = comment;
                ret.trackNameJ = ret.trackName;

            }
        } catch (Exception e) {
            ret.trackName = "";
            ret.trackNameJ = "";
        }

        return ret;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {

        Vgm.Gd3 gd3 = getGD3Info(vgmBuf, 0);
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;

        try {
            fmp.run(vgmBuf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                if (vgmFrameCounter > -1) {
                    counter++;

                    fmp.nise98.runTimer();
                    if (!fmp.nise98.intTimer()) continue;
                    vgmCurLoop = fmp.processOneFrame(() -> stopped = true);
                }
                vgmFrameCounter++;
            }

            //vgmCurLoop = mm.ReadUInt16(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void setPPZ8PCMData(int bank, int mode, byte[][] pcmData) {
        plugin.chipRegister.chip(Ppz8Chip.class).writePcm(0, bank, mode, pcmData, model);
    }

    private void setPPZ8Data(int port, int adr, int data) {
        plugin.chipRegister.chip(Ppz8Chip.class).write(0, port, adr, data, model);
    }

    private void opnaWrite(int p, int a, int d) {
        int cn = p >> 8;
        int port = (p & 0xff) == 0x8a ? 0 : 1;
        plugin.chipRegister.chip(Ym2608Chip.class).write(0, port, a, d, model);

        if (port == 1 && a == 0x8 && model == EnmModel.RealModel) {
            this.isDataBlock = true;
            fmp.pcmDataSendCount++;
            plugin.chipRegister.chip(Ym2608Chip.class).setSyncWait(0, 1);
        }
    }
}
