package mdplayer.driver.mxdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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
        mxdrv.charset = Common.charset;
        mxdrv.ym2151Write = (a, d) -> plugin.chipRegister.chip(Ym2151Chip.class).write(0, 0, a, d, model, plugin.chipRegister.chip(Ym2151Chip.class).ym2151Hosei[0], vgmFrameCounter);
        mxdrv.counter = l -> totalCounter = l;
        mxdrv.isFromDF = Pcm8Chip::isFromDF;
        mxdrv.isFromPTM = Pcm8Chip::isFromPTM;
        mxdrv.mdxPCM = new MdxPcmInterface() {

            private Runnable terminator;

            @Override
            public void writePcm(byte[] pcm, int offset, int length) {
                plugin.chipRegister.chip(Pcm8Chip.class).writePcm(0, 0, 0, pcm, model);
            }

            @Override
            public int getPcm(short[] buffer, int offset, int length, Runnable terminator) {
                this.terminator = terminator;
                return plugin.chipRegister.chip(Pcm8Chip.class).getPcm(0, buffer, offset, length, this::clock);
            }

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
                return plugin.chipRegister.chip(Pcm8Chip.class).startPcm(0, sampleRate, opmFlag, adpcmFlag, pcmBuf);
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
                plugin.chipRegister.chip(Pcm8Chip.class).free(0);
            }

            @Override
            public void abort() {
                plugin.chipRegister.chip(Pcm8Chip.class).abort(0);
            }

            @Override
            public void opmSetIocs(int addr, int data) {
                plugin.chipRegister.chip(Pcm8Chip.class).opmSetIocs(0, addr, data);
            }

            @Override
            public void keyOnAdpcm(int addr, int mode, int len) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOnAdpcm(0, addr, mode, len);
            }

            @Override
            public void adpcmMod(int mode) {
                plugin.chipRegister.chip(Pcm8Chip.class).adpcmMod(0, mode);
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

                    terminator.run();
                    vgmCurLoop = mxdrv.loopCount;
                    if (mxdrv.terminatePlay) {
                        stopped = true;
                    }
                } catch (Exception ex) {
                    logger.log(Level.ERROR, ex.getMessage(), ex);
                }
            }
        };
        mxdrv.pcm8pp = new Pcm8Interface() {
            @Override
            public void writePcm(byte[] pcm, int offset, int length) {
                plugin.chipRegister.chip(Pcm8Chip.class).writePcm(0, 0, 0, pcm, model);
            }

            @Override
            public void keyOn(int ch, int d1, int d2, int d3) {
                plugin.chipRegister.chip(Pcm8Chip.class).keyOn(0, ch, d1, d2, d3);
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
        mxdrv.extendFile = extendFile;
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

        mxdrv.init(vgmBuf, model == EnmModel.VirtualModel, setting.getOutputDevice().getSampleRate());
    }

    private final short[] dummyBuf = new short[2];

    @Override
    public void processOneFrame() {
        render(dummyBuf, 0, 2);
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
