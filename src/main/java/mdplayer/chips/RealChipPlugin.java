package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip;
import mdplayer.RealChip.EnmRealChipType;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;


/**
 * RealChipPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class RealChipPlugin implements Plugin {

    private static final Logger logger = System.getLogger(RealChipPlugin.class.getName());

    public RealChip realChip;

    BasePlugin<? extends BaseDriver> context;

    public int realFadeoutVol = 0;
    public int realFadeoutVolWait = 4;

    public int hiyorimiEven = 0;
    private boolean hiyorimiNecessary = setting.getHiyorimiMode();

    public boolean isHiyorimiNecessary() {
        return hiyorimiNecessary;
    }

    public void setHiyorimiNecessary(boolean hiyorimiNecessary) {
        this.hiyorimiNecessary = hiyorimiNecessary;
    }

    public void initChip(int hiyorimiDeviceFlag) {
        this.hiyorimiNecessary = hiyorimiDeviceFlag == 0x3 && this.hiyorimiNecessary;
    }

    public RealChipPlugin() {
//                , SoundChip.realChip
//                , vstMng
//                , SoundChip.scYM2612
//                , SoundChip.scSN76489
//                , SoundChip.scYM2608
//                , SoundChip.scYM2151
//                , SoundChip.scYM2203
//                , SoundChip.scYM2413
//                , SoundChip.scYM2610
//                , SoundChip.scYM2610EA
//                , SoundChip.scYM2610EB
//                , SoundChip.scYM3526
//                , SoundChip.scYM3812
//                , SoundChip.scYMF262
//                , SoundChip.scC140
//                , SoundChip.scSEGAPCM
//                , SoundChip.scAY8910
//                , SoundChip.scK051649
    }

    public void realChipClose() {
//        if (SoundChip.realChip != null) {
//            SoundChip.realChip.close();
//        }
    }

    public List<Setting.ChipType2> getRealChipList(EnmRealChipType scciType) {
//        if (SoundChip.realChip == null) return null;
//        return SoundChip.realChip.getRealChipList(scciType);
        return null;
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        this.context = context;

//        if (SoundChip.realChip == null && !getemuOnly()) {
//            Log.forcedWrite("Audio:Init:STEP 04");
//            SoundChip.realChip = new RealChip(!setting.getUnuseRealChip());
//        }
//
//        if (SoundChip.realChip != null) {
//            for (int i = 0; i < 2; i++) {
//                SoundChip.scYM2612[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2612Type()[i], 0);
//                if (SoundChip.scYM2612[i] != null) SoundChip.scYM2612[i].init();
//                SoundChip.scSN76489[i] = SoundChip.realChip.getRealChip(Audio.setting.getSN76489Type()[i], 0);
//                if (SoundChip.scSN76489[i] != null) SoundChip.scSN76489[i].init();
//                SoundChip.scYM2608[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2608Type()[i], 0);
//                if (SoundChip.scYM2608[i] != null) SoundChip.scYM2608[i].init();
//                SoundChip.scYM2151[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2151Type()[i], 0);
//                if (SoundChip.scYM2151[i] != null) SoundChip.scYM2151[i].init();
//                SoundChip.scYM2203[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2203Type()[i], 0);
//                if (SoundChip.scYM2203[i] != null) SoundChip.scYM2203[i].init();
//                SoundChip.scAY8910[i] = SoundChip.realChip.getRealChip(Audio.setting.getAY8910Type()[i], 0);
//                if (SoundChip.scAY8910[i] != null) SoundChip.scAY8910[i].init();
//                SoundChip.scK051649[i] = SoundChip.realChip.getRealChip(Audio.setting.getK051649Type()[i], 0);
//                if (SoundChip.scK051649[i] != null) SoundChip.scK051649[i].init();
//                SoundChip.scYM2413[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2413Type()[i], 0);
//                if (SoundChip.scYM2413[i] != null) SoundChip.scYM2413[i].init();
//                SoundChip.scYM3526[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM3526Type()[i], 0);
//                if (SoundChip.scYM3526[i] != null) SoundChip.scYM3526[i].init();
//                SoundChip.scYM3812[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM3812Type()[i], 0);
//                if (SoundChip.scYM3812[i] != null) SoundChip.scYM3812[i].init();
//                SoundChip.scYMF262[i] = SoundChip.realChip.getRealChip(Audio.setting.getYMF262Type()[i], 0);
//                if (SoundChip.scYMF262[i] != null) SoundChip.scYMF262[i].init();
//                SoundChip.scYM2610[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2610Type()[i], 0);
//                if (SoundChip.scYM2610[i] != null) SoundChip.scYM2610[i].init();
//                SoundChip.scYM2610EA[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2610Type()[i], 1);
//                if (SoundChip.scYM2610EA[i] != null) SoundChip.scYM2610EA[i].init();
//                SoundChip.scYM2610EB[i] = SoundChip.realChip.getRealChip(Audio.setting.getYM2610Type()[i], 2);
//                if (SoundChip.scYM2610EB[i] != null) SoundChip.scYM2610EB[i].init();
//                SoundChip.scSEGAPCM[i] = SoundChip.realChip.getRealChip(Audio.setting.getSEGAPCMType()[i], 0);
//                if (SoundChip.scSEGAPCM[i] != null) SoundChip.scSEGAPCM[i].init();
//                SoundChip.scC140[i] = SoundChip.realChip.getRealChip(Audio.setting.getC140Type()[i], 0);
//                if (SoundChip.scC140[i] != null) SoundChip.scC140[i].init();
//            }
//        }
    }

    @Override
    public void close() {
        closeThread();
//        SoundChip.realChip = null;
    }

    public void fadeOut() {
        if (realFadeoutVol != 1000) realFadeoutVolWait--;
        if (realFadeoutVolWait == 0) {
            context.chipRegister.chips().forEach(c -> context.chipRegister.chip(c).setFadeout(0, realFadeoutVol));
            context.chipRegister.chips().forEach(c -> context.chipRegister.chip(c).setFadeout(1, realFadeoutVol));

            realFadeoutVol++;

            realFadeoutVol = Math.min(127, realFadeoutVol);
            if (realFadeoutVol == 127) {
//                if (SoundChip.realChip != null) {
//                    softReset(EnmModel.RealModel);
//                }
                realFadeoutVolWait = 1000;
                context.chipRegister.plugin(MidiPlugin.class).resetAll();
            } else {
                realFadeoutVolWait = 700 - realFadeoutVol * 2;
            }
        }
    }

    public void setGimicOPNVolume(boolean isAbs, int volume) {
        setting.getBalance().setGimicOPNVolume(Common.range((isAbs ? 0 : setting.getBalance().getGimicOPNVolume()) + volume, 0, 127));
    }

    public void setGimicOPNAVolume(boolean isAbs, int volume) {
        setting.getBalance().setGimicOPNAVolume(Common.range((isAbs ? 0 : setting.getBalance().getGimicOPNAVolume()) + volume, 0, 127));
    }

    public void softReset(EnmModel model) {
//        if (model == EnmModel.RealModel && SoundChip.realChip != null) {
//            SoundChip.realChip.SendData();
//        }
    }

    /** read by {@link #render()} on its own thread, written by the player thread */
    private volatile boolean threadClosed = false;
    private volatile boolean threadStopped = true;

    private void render() {

        if (context.driverReal == null) { // no real chip driver, nothing to render
            this.threadClosed = true;
            this.setThreadStopped(true);
            return;
        }

        int sampleRate = setting.getOutputDevice().getSampleRate();
        // elapsed time is counted in samples from the start of this thread. an absolute clock
        // (epoch) is far too big for a double to keep the resolution of a single sample.
        long base = System.nanoTime();
        double threshold = sampleRate / 100d; // 10ms
        // samples already processed
        double o = 0;
        this.setThreadStopped(false);
        try {
            while (!this.threadClosed) {
                Thread.sleep(0);

                double el1 = (System.nanoTime() - base) * sampleRate / 1_000_000_000d;
                if (el1 - o < 1) continue;
                if (el1 - o >= threshold) { // too late, drop the delayed samples
                    o = el1;
                } else {
                    o += 1;
                }

                if (context.stopped || context.paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (this.hiyorimiNecessary && context.driverVirtual.isDataBlock) {
                    continue;
                }

                if (context.fadeout) {
                    fadeOut();
                }

                if (this.hiyorimiNecessary) {
//                    long v = driverReal.frameCounter - audio.driverVirtual.frameCounter;
//                    long d = setting.getoutputDevice().getSampleRate() * (setting.LatencySCCI - setting.getoutputDevice().getSampleRate() * setting.LatencyEmulation) / 1000;
//                    long l = getLatency() / 4;
//                    int m = 0;
//                    if (d >= 0) {
//                        if (v >= d - l && v <= d + l) m = 0;
//                        else m = (v + d > l) ? 1 : 2;
//                    } else {
//                        d = Math.abs(setting.getoutputDevice().getSampleRate() * ((int) setting.LatencyEmulation - (int) setting.LatencySCCI) / 1000);
//                        if (v >= d - l && v <= d + l) m = 0;
//                        else m = (v - d > l) ? 1 : 2;
//                    }

                    double dEMU = setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000.0;
                    double dSCCI = setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000.0;
                    double abs = Math.abs((context.driverReal.frameCounter - dSCCI) - (context.driverVirtual.frameCounter - dEMU));
                    int m = 0;
                    long l = context.getLatency() / 10;
                    if (abs >= l) {
                        m = ((context.driverReal.frameCounter - dSCCI) > (context.driverVirtual.frameCounter - dEMU)) ? 1 : 2;
                    }

                    switch (m) {
                        case 0: // x1
                            context.driverReal.processOneFrame();
                            break;
                        case 1: // x1/2
                            this.hiyorimiEven++;
                            if (this.hiyorimiEven > 1) {
                                context.driverReal.processOneFrame();
                                this.hiyorimiEven = 0;
                            }
                            break;
                        case 2: // x2
                            context.driverReal.processOneFrame();
                            context.driverReal.processOneFrame();
                            break;
                    }
                } else {
                    context.driverReal.processOneFrame();
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        this.setThreadStopped(true);
    }

    public void startThread() {
        if (setting.getOutputDevice().getDeviceType() == Common.DEV_Null) {
            logger.log(Level.INFO, "dev null: " + getClass().getName());
            return;
        }

        // this plugin is a shared singleton, a thread of the previous song might still be alive
        closeThread();

        this.threadClosed = false;
        Thread threadMain = new Thread(this::render);
        threadMain.setPriority(Thread.MAX_PRIORITY);
        threadMain.setDaemon(true);
        threadMain.setName("trdVgmReal");
        threadMain.start();
    }

    /** requests {@link #render()} to exit and waits for it */
    public void closeThread() {
        setThreadClosed(true);
        int timeout = 1000;
        while (!isThreadStopped() && timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
                break;
            }
        }
        if (!isThreadStopped()) {
            logger.log(Level.WARNING, "real chip thread doesn't stop");
        }
    }

    public synchronized boolean isThreadStopped() {
        return threadStopped;
    }

    public synchronized boolean isThreadClosed() {
        return threadClosed;
    }

    public synchronized void setThreadStopped(boolean value) {
//new Exception("value: " + value).printStackTrace(System.err);
        threadStopped = value;
    }

    public void setThreadClosed(boolean value) {
        threadClosed = value;
    }

    public void process1(EnmModel model) {
        context.chipRegister.chip(Ym2608Chip.class).sendData(0, model);
        context.chipRegister.chip(Ym2608Chip.class).setSyncWait(0, 1);
        context.chipRegister.chip(Ym2151Chip.class).sendData(0, model);
        context.chipRegister.chip(Ym2151Chip.class).setSyncWait(0, 1);

        context.chipRegister.chip(Ym2608Chip.class).sendData(1, model);
        context.chipRegister.chip(Ym2608Chip.class).setSyncWait(1, 1);
        context.chipRegister.chip(Ym2151Chip.class).sendData(1, model);
        context.chipRegister.chip(Ym2151Chip.class).setSyncWait(1, 1);
    }

    public void process3(boolean useChipYM2612Ch6, int vgmWait) {
        if (useChipYM2612Ch6)
            context.chipRegister.chip(Ym2612Chip.class).setSyncWait(0, vgmWait);
//            if ((useChip & enmUseChip.SN76489) == enmUseChip.SN76489)
//                context.chipRegister.setSN76489SyncWait(vgmWait);
//            context.chipRegister.setYM2608SyncWait(vgmWait);
//            context.chipRegister.setYM2151SyncWait(vgmWait);
    }
}
