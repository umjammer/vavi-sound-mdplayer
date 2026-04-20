package mdplayer.driver.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
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

    public FmpDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        fmp = new FMP();
        fmp.sampleRate = Common.VGMProcSampleRate;
        fmp.charset = Common.charset;
        fmp.dir = System.getProperty("mdplayer.fmp.dir", System.getProperty("user.dir"));
        fmp.blockWrite = b -> this.isDataBlock = b;
        fmp.setPPZ8PCMData = this::setPPZ8PCMData;
        fmp.setPPZ8Data = this::setPPZ8Data;
        fmp.opnaWrite = this::opnaWrite;
    }

    public FmpDriver() {
        this(null); // gross
    }

    public void setFileTemp(FileTemp ft) {
        fmp.ft = ft;
    }

    public void setSearchPath(String searchPath) {
        fmp.setSearchPath(searchPath);
    }

    /** before using ths method, you must do {@link BaseDriver#init} */
    public void compile() {
        fmp.compile();
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();

        try {
            if (buf != null && buf.length > 2) {
                int[] ptr = new int[] {(ByteUtil.readLeShort(buf, 0) & 0xffff) + 4}; // 4 'FMC'+version(1byte)
                String comment = Common.getNRDString(buf, /* ref */ ptr);
                md.set(Tag.Title, comment);
                md.set(Tag.TitleJ, md.getFirst(Tag.Title));

            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return md;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        MetaData _ = getMetaData(dataBuf, 0);

        loopCounter = 0;
        curLoop = 0;
        this.model = model;
        frameCounter = -latency - waitTime;

        fmp.playingFileName = plugin.playingFileName;
        fmp.playingArcFileName = plugin.playingArcFileName;

        try {
            fmp.run(dataBuf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                if (frameCounter > -1) {
                    counter++;

                    fmp.nise98.runTimer();
                    if (!fmp.nise98.intTimer()) continue;
                    curLoop = fmp.processOneFrame(() -> stopped = true);
                }
                frameCounter++;
            }

            //curLoop = mm.ReadUInt16(reg.a6 + dw.LOOP_COUNTER);
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
