package mdplayer.driver.mxdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.driver.mxdrv.MXDRV.MdxPcmInterface;
import mdplayer.driver.mxdrv.MXDRV.Pcm8Interface;
import mdplayer.plugin.BasePlugin;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * MxDriver
 *
 * @author kumatan
 */
public class MxDriver extends BaseDriver {

    private static final Logger logger = getLogger(MxDriver.class.getName());

    private final MXDRV mxdrv;

    private Tuple<String, byte[]> extendFile = null;

    public MxDriver() {
        this.mxdrv = new MXDRV();
        // called the same timing as mdxPcm.getPcm
        mxdrv.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).ym2151Hosei[0], vgmFrameCounter);
        mxdrv.isFromDF = Pcm8Chip::isFromDF;
        mxdrv.isFromPTM = Pcm8Chip::isFromPTM;
        mxdrv.mdxPCM = new MdxPcmInterface() {
            @Override
            public int getPcm(short[] buffer, int offset, int length) {
                return plugin.chipRegister.chip(Pcm8Chip.class).getPcm(0, buffer, offset, length);
            }

            @Override
            public int start(int sampleRate, int opmFlag, int adpcmFlag, int betw, int pcmBuf, int late, double rev) {
                return plugin.chipRegister.chip(Pcm8Chip.class).start(0, sampleRate, opmFlag, adpcmFlag, betw, pcmBuf, late, rev);
            }

            @Override
            public int startPcm(int sampleRate, int opmFlag, int adpcmFlag, int pcmBuf) {
                return 0; // done directly at mds.start
            }

            @Override
            public void initIocs() {
                plugin.chipRegister.chip(Pcm8Chip.class).initIocs(0);
            }

            @Override
            public void opmInt(Runnable func) {
                plugin.chipRegister.chip(Pcm8Chip.class).opmInt(0, func);
            }

            @Override
            public int opmWait(int wait) {
                return plugin.chipRegister.chip(Pcm8Chip.class).opmWait(0, wait);
            }

            @Override
            public int totalVolume(int vol) {
                return plugin.chipRegister.chip(Pcm8Chip.class).totalVolume(0, vol);
            }

            @Override
            public void free() {
                plugin.chipRegister.chip(Pcm8Chip.class).stop(0);
            }

            @Override
            public void abort() {
                plugin.chipRegister.chip(Pcm8Chip.class).abort(0);
            }

            @Override
            public void opmSetIocs(int addr, int data) {
                plugin.chipRegister.chip(Pcm8Chip.class).write(0, 0, addr, data, model);
            }

            @Override
            public void keyOnAdpcm(int addr, int mode, int len) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOnAdpcm(0, addr, mode, len);
            }

            @Override
            public void adpcmMod(int mode) {
                plugin.chipRegister.chip(Pcm8Chip.class).adpcmMod(0, mode);
            }
        };
        mxdrv.pcm8pp = new Pcm8Interface() {
            @Override
            public void writePcm(byte[] pcm, int offset, int length) {
                // done directly at #init
            }

            @Override
            public void keyOn(int ch, int adr, int mode, int len) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOn(0, ch, adr, mode, len);
            }

            @Override
            public void keyOff(int ch) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOff(0, ch);
            }

            @Override
            public void abort() {
                plugin.chipRegister.chip(Pcm8Chip.class).abort(0);
            }
        };
    }

    public void setExtendFile(Tuple<String,byte[]> extendFile) {
        this.extendFile = extendFile;
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
        String n = new String(ByteUtil.toByteArray(lst), Common.charset);
        gd3.trackName = n;
        gd3.trackNameJ = n;
        byte[][] mdx = new byte[1][];
        int[] mdxSize = new int[1];
        String[] pdxFileName = new String[1];
        makeMdxBuf(buf, mdx, mdxSize, pdxFileName);

        return gd3;
    }

    /**
     * @param mdx OUT
     * @param mdxSize OUT
     * @param pdxFileName OUT
     */
    private void makeMdxBuf(byte[] buf, byte[][] mdx, int[] mdxSize, String[] pdxFileName) {
        // Skip title
        int p = 8;
        int c;
        mdxSize[0] = buf.length;
        mdx[0] = new byte[buf.length + 8];
        System.arraycopy(buf, 0, mdx[0], 8, mdxSize[0]);

        while (true) {
            c = mdx[0][p++] & 0xff;
            if (c == 0x0d) break;
            if (c == 0x0a) break;
            if (c < 0x20) {
                if (c != 0x1b) throw new IllegalArgumentException("illegal char: " + Integer.toHexString(c));
            }
        }
        p--;
        mdx[0][p++] = 0x00;
        if ((p & 0x01) != 0) {
            mdx[0][p++] = 0x00;
        }
        int p2 = p;
        if (c != 0x0d) {
            while (mdx[0][p2++] != 0x0d) ;
        }

        // Load PDX
        byte havePdx = (byte) 0xff;
        List<Byte> lstPdxFileName;
        while (mdx[0][p2++] != 0x1a) ;
        if (mdx[0][p2] != 0) {
            havePdx = 0x00;
            lstPdxFileName = new ArrayList<>();
            while (mdx[0][p2] != 0x00) {
                lstPdxFileName.add(mdx[0][p2]);
                p2++;
            }
            pdxFileName[0] = new String(ByteUtil.toByteArray(lstPdxFileName), Common.charset);
        }
        p2++;

        // Process MDX so that it can be passed to MXDRV
        int mdxBodyPtr = p;
        while (p2 < mdx[0].length) {
            mdx[0][p++] = mdx[0][p2++];
        }
        mdxSize[0] = p;

        mdx[0][0] = 0x00;
        mdx[0][1] = 0x00;
        mdx[0][2] = havePdx;
        mdx[0][3] = havePdx;
        mdx[0][4] = (byte) ((mdxBodyPtr & 0xffff) >>> 8);
        mdx[0][5] = (byte) (mdxBodyPtr & 0xff);
        mdx[0][6] = 0x00;
        mdx[0][7] = 0x08;
    }

    /**
     * @param pdx OUT
     * @param pdxSize OUT
     */
    private void makePdxBuf(String pdxFileName, byte[][] pdx, int[] pdxSize) {
        if (extendFile == null) {
            logger.log(Level.DEBUG, "extendFile is null");
            return;
        }

        pdx[0] = new byte[extendFile.getItem2().length + pdxFileName.length() + 8 + 1];
        System.arraycopy(pdxFileName.getBytes(StandardCharsets.US_ASCII), 0, pdx[0], 8, pdxFileName.length());
        System.arraycopy(extendFile.getItem2(), 0, pdx[0], 8 + pdxFileName.length() + 1, extendFile.getItem2().length);
        pdx[0][0] = 0x00;
        pdx[0][1] = 0x00;
        pdx[0][2] = 0x00;
        pdx[0][3] = 0x00;
        pdx[0][4] = (byte) (((8 + pdxFileName.length() + 2) & 0xffff_fffe) >>> 8);
        pdx[0][5] = (byte) ((8 + pdxFileName.length() + 2) & 0xffff_fffe);
        pdx[0][4] = (byte) (((8 + pdxFileName.length() + 1) & 0xffff) >>> 8);
        pdx[0][5] = (byte) ((8 + pdxFileName.length() + 1) & 0xff);
        pdx[0][6] = (byte) (((pdxFileName.length() + 1) & 0xff00) >>> 8);
        pdx[0][7] = (byte) ((pdxFileName.length() + 1) & 0xff);
        pdxSize[0] = pdx[0].length;
    }

    public static void getPDXFileName(byte[] buf, String[] pdx, Charset charset) {
        int p = 0;
        int c;
        while (true) {
            c = buf[p++] & 0xff;
            if (c == 0x0d || c == 0x0a) break;
            if (c < 0x20 && c != 0x1b) throw new IllegalArgumentException("illegal char: " + Integer.toHexString(c));
        }
        if ((p & 0x01) != 0) p++;
        if (c != 0x0d) while (buf[p++] != 0x0d) ;
        while (buf[p++] != 0x1a) ;
        if (buf[p] == 0) return;
        List<Byte> lstPdxFileName = new ArrayList<>();
        while (buf[p] != 0x00) lstPdxFileName.add(buf[p++]);
        pdx[0] = new String(ByteUtil.toByteArray(lstPdxFileName), charset);
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
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

        byte[][] mdx = new byte[1][];
        int[] mdxSize = new int[1];
        byte[][] pdx = new byte[1][];
        int[] pdxSize = new int[1];
        String[] pdxFileName = new String[1];
        makeMdxBuf(vgmBuf, mdx, mdxSize, pdxFileName);
        makePdxBuf(pdxFileName[0], pdx, pdxSize);
        if ((pdxFileName[0] != null && !pdxFileName[0].isEmpty()) && pdx[0] == null) {
            logger.log(Level.WARNING, "pdxFileName: %s, pdx: %s".formatted(pdxFileName[0], pdx[0]));
            throw new IllegalStateException("Failed to load PCM file [%s].".formatted(pdxFileName[0]));
        }

        int ret;
        if (model == EnmModel.VirtualModel) {
            ret = mxdrv.MXDRV_Start(setting.getOutputDevice().getSampleRate(), 0, 0, 0, mdxSize[0], pdxSize[0], 0, -1, 1);
        } else {
            ret = mxdrv.MXDRV_Start(setting.getOutputDevice().getSampleRate(), 0, 0, 0, mdxSize[0], pdxSize[0], 0, -1, -1);
        }
logger.log(Level.TRACE, "MXDRV_Start: " + ret);
        XMemory mm = mxdrv.getMemory();
        int memind = mm.mm.length;
        int mdxPtr = memind;
        memind += mdxSize[0];
        int pdxPtr = memind;
        memind += pdxSize[0];
        mm.realloc(memind);
        for (int i = 0; i < mdxSize[0]; i++) mm.write(mdxPtr + i, mdx[0][i]);
        for (int i = 0; i < pdxSize[0]; i++) mm.write(pdxPtr + i, pdx[0][i]);

        plugin.chipRegister.chip(Pcm8Chip.class).writePcm(0, 0, 0, mm.mm, model);
        if (setting.getMxDrv().pcm8Type == 1)
            plugin.chipRegister.chip(Pcm8Chip.class).writePcm(0, 0, 0, mm.mm, model);

        int playtime = mxdrv.MXDRV_MeasurePlayTime(mdx[0], mdxSize[0], mdxPtr, pdx[0], pdxSize[0], pdxPtr, 1, Depend.TRUE);
//logger.log(Level.TRACE, "(%d:%02d) %d".formatted(playtime / 1000 / 60, playtime / 1000 % 60, ""));
        totalCounter = (long) playtime * setting.getOutputDevice().getSampleRate() / 1000;
        mxdrv.terminatePlay = false;
        mxdrv.MXDRV_Play(mdx[0], mdxSize[0], mdxPtr, pdx[0], pdxSize[0], pdxPtr);
    }

    public void clock(Runnable timer, boolean firstFlg) {
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
    public void processOneFrame() {
        //logger.log(Level.TRACE, "0:%08x".formatted(mm.readint(MXWORK_CHBUF_FM[8] + MXWORK_CH.S0012)));
        //logger.log(Level.TRACE, "1:%04x".formatted(mm.readshort(MXWORK_CHBUF_PCM[0] + MXWORK_CH.S0012) >> 6));
        //logger.log(Level.TRACE, "1a:%04x".formatted(mm.readshort(MXWORK_CHBUF_PCM[0] + MXWORK_CH.S0014) >> 6));
        //logger.log(Level.TRACE, "2:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[1] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "3:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[2] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "4:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[3] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "5:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[4] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "6:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[5] + MXWORK_CH.S0004)));
        //logger.log(Level.TRACE, "7:%d".formatted(mm.readint(MXWORK_CHBUF_PCM[6] + MXWORK_CH.S0004)));
    }
}
