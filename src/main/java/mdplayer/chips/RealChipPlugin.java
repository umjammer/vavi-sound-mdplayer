package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;


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

    public List<Setting.ChipType2> getRealChipList(Common.EnmRealChipType scciType) {
//        if (SoundChip.realChip == null) return null;
//        return SoundChip.realChip.GetRealChipList(scciType);
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
//                SoundChip.scYM2612[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2612Type()[i], 0);
//                if (SoundChip.scYM2612[i] != null) SoundChip.scYM2612[i].init();
//                SoundChip.scSN76489[i] = SoundChip.realChip.GetRealChip(Audio.setting.getSN76489Type()[i], 0);
//                if (SoundChip.scSN76489[i] != null) SoundChip.scSN76489[i].init();
//                SoundChip.scYM2608[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2608Type()[i], 0);
//                if (SoundChip.scYM2608[i] != null) SoundChip.scYM2608[i].init();
//                SoundChip.scYM2151[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2151Type()[i], 0);
//                if (SoundChip.scYM2151[i] != null) SoundChip.scYM2151[i].init();
//                SoundChip.scYM2203[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2203Type()[i], 0);
//                if (SoundChip.scYM2203[i] != null) SoundChip.scYM2203[i].init();
//                SoundChip.scAY8910[i] = SoundChip.realChip.GetRealChip(Audio.setting.getAY8910Type()[i], 0);
//                if (SoundChip.scAY8910[i] != null) SoundChip.scAY8910[i].init();
//                SoundChip.scK051649[i] = SoundChip.realChip.GetRealChip(Audio.setting.getK051649Type()[i], 0);
//                if (SoundChip.scK051649[i] != null) SoundChip.scK051649[i].init();
//                SoundChip.scYM2413[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2413Type()[i], 0);
//                if (SoundChip.scYM2413[i] != null) SoundChip.scYM2413[i].init();
//                SoundChip.scYM3526[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM3526Type()[i], 0);
//                if (SoundChip.scYM3526[i] != null) SoundChip.scYM3526[i].init();
//                SoundChip.scYM3812[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM3812Type()[i], 0);
//                if (SoundChip.scYM3812[i] != null) SoundChip.scYM3812[i].init();
//                SoundChip.scYMF262[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYMF262Type()[i], 0);
//                if (SoundChip.scYMF262[i] != null) SoundChip.scYMF262[i].init();
//                SoundChip.scYM2610[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 0);
//                if (SoundChip.scYM2610[i] != null) SoundChip.scYM2610[i].init();
//                SoundChip.scYM2610EA[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 1);
//                if (SoundChip.scYM2610EA[i] != null) SoundChip.scYM2610EA[i].init();
//                SoundChip.scYM2610EB[i] = SoundChip.realChip.GetRealChip(Audio.setting.getYM2610Type()[i], 2);
//                if (SoundChip.scYM2610EB[i] != null) SoundChip.scYM2610EB[i].init();
//                SoundChip.scSEGAPCM[i] = SoundChip.realChip.GetRealChip(Audio.setting.getSEGAPCMType()[i], 0);
//                if (SoundChip.scSEGAPCM[i] != null) SoundChip.scSEGAPCM[i].init();
//                SoundChip.scC140[i] = SoundChip.realChip.GetRealChip(Audio.setting.getC140Type()[i], 0);
//                if (SoundChip.scC140[i] != null) SoundChip.scC140[i].init();
//            }
//        }
    }

    @Override
    public void close() {
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

    public Thread trdMain = null;
    public boolean trdClosed = false;
    private boolean trdStopped = true;

    private void render() {

        if (context.driverReal == null) { // first time, driverReal must be null
            this.trdClosed = true;
            this.setThreadStopped(true);
            return;
        }

        double o = System.currentTimeMillis() / Audio.swFreq;
        double step = 1 / (double) setting.getOutputDevice().getSampleRate();
        this.setThreadStopped(false);
        try {
            while (!this.trdClosed) {
                Thread.sleep(0);

                double el1 = System.currentTimeMillis() / Audio.swFreq;
                if (el1 - o < step) continue;
                if (el1 - o >= step * setting.getOutputDevice().getSampleRate() / 100.0) { // Threshold 10ms
                    do {
                        o += step;
                    } while (el1 - o >= step);
                } else {
                    o += step;
                }

                if (context.stopped || context.paused) {
//                    if (SoundChip.realChip != null && !oneTimeReset) {
//                        softReset(EnmModel.RealModel);
//                        oneTimeReset = true;
//                        chipRegister.resetAllMIDIout();
//                    }
                    continue;
                }
                if (context.hiyorimiNecessary && context.driverVirtual.isDataBlock) {
                    continue;
                }

                if (context.fadeout) {
                    fadeOut();
                }

                if (context.hiyorimiNecessary) {
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
                            context.hiyorimiEven++;
                            if (context.hiyorimiEven > 1) {
                                context.driverReal.processOneFrame();
                                context.hiyorimiEven = 0;
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

        this.trdClosed = false;
        this.trdMain = new Thread(this::render);
        this.trdMain.setPriority(Thread.MAX_PRIORITY);
        this.trdMain.setDaemon(true);
        this.trdMain.setName("trdVgmReal");
        this.trdMain.start();
    }

    public synchronized boolean isThreadStopped() {
        return trdStopped;
    }

    public synchronized void setThreadStopped(boolean value) {
//new Exception("value: " + value).printStackTrace(System.err);
        trdStopped = value;
    }
}
