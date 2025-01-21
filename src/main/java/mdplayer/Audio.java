package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineEvent;

import mdplayer.Common.EnmModel;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.RealChipPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class Audio {

    private static final Logger logger = getLogger(Audio.class.getName());

    private final Setting setting = Setting.getInstance();

    private static final Audio instance = new Audio();

    // TODO driver should be one, instruments should be separated virtual and real
    public BaseDriver driverVirtual = null;

    public final mdsound.MDSound mds;

    public ChipRegister chipRegister;

    public final VisVolume visVolume = new VisVolume();

    // TODO variable?
    public static final int BUFFER_SIZE = 1024;

    public String errMsg = "";

    public FileFormat playingFileFormat;

    public String naudioFileName = null;
    public AudioInputStream naudioFileReader = null;
//    protected NWave.SampleProviders.SampleToWaveProvider16 naudioWs = null;
    protected byte[] naudioSrcbuffer = null;
    public int procTimePer1Frame = 0;
    public int stepCounter = 0;
    public double vgmFadeoutCounter;
    public double vgmFadeoutCounterV;

    private static byte[] ensure(byte[] buffer, int bytesRequired) {
        if (buffer == null || buffer.length < bytesRequired) {
            buffer = new byte[bytesRequired];
        }
        return buffer;
    }

    protected static void convert2ByteToShort(short[] destBuffer, int offset, byte[] source, int shortCount) {
        int samplesRead = shortCount;
        for (int n = 0; n < samplesRead; n++) {
            destBuffer[n] = ByteUtil.readLeShort(source, offset + n * Short.BYTES); // volume;
        }
    }

    public int nAudioRead(short[] buffer, int offset, int count) {
        try {
            naudioSrcbuffer = ensure(naudioSrcbuffer, count * 2);
//            naudioWs.read(naudioSrcbuffer, 0, count * 2);
            convert2ByteToShort(buffer, offset, naudioSrcbuffer, count);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return count;
    }

    public int trdVgmVirtualFunction(short[] buffer, int offset, int sampleCount) {
//logger.log(Level.TRACE, ": " + sampleCount);
        //return nAaudioRead(buffer, offset, sampleCount);

        if (naudioFileReader != null) {
            if (trdClosed) {
                _trdStopped = true;
                //vgmFadeout = false;
                //Stopped = true;
            }
            return nAudioRead(buffer, offset, sampleCount);
        }

        int cnt = trdVgmVirtualMainFunction(buffer, offset, sampleCount);

        if (setting.getMidiKbd().getUseMIDIKeyboard()) {
            chipRegister.plugin(MidiPlugin.class).keyboard(buffer, offset, sampleCount);
        }

        return cnt;
    }

    private static int limit(int v, int max, int min) {
        return Math.min(max, Math.max(v, min));
    }

//int CC;
    protected int trdVgmVirtualMainFunction(short[] buffer, int offset, int sampleCount) {
        if (buffer == null || buffer.length < 1 || sampleCount == 0) return 0;
        if (driverVirtual == null) return sampleCount;

        try {
            //stwh.Reset(); stwh.Start();

//if (CC++ > 100) { System.exit(1); }
//logger.log(Level.TRACE, "stop: " + stopped + ", " + hashCode());
            if (stopped || paused) {
                if (driverVirtual.isNotRenderingOnPause()) {
                    for (int d = offset; d < offset + sampleCount; d++) buffer[d] = 0;
                    return sampleCount;
                } else {
                    int ret = mds.update(buffer, offset, sampleCount, null);
                    return ret;
                }
            }

            int cnt = driverVirtual.render(buffer, offset, sampleCount);
//logger.log(Level.TRACE, "sampleCount: " + sampleCount);

            // VST
            chipRegister.plugin(VstPlugin.class).update(buffer, offset, sampleCount);

            for (int i = 0; i < sampleCount; i++) {
                int mul = (int) (16384.0 * Math.pow(10.0, masterVolume / 40.0));
                buffer[offset + i] = (short) limit((buffer[offset + i] * mul) >> 13, 0x7fff, -0x8000);

                if (!vgmFadeout) continue;

                // Fade-out Processing
                buffer[offset + i] = (short) (buffer[offset + i] * vgmFadeoutCounter);

                vgmFadeoutCounter -= vgmFadeoutCounterV;
                if (vgmFadeoutCounterV >= 0.004 && vgmFadeoutCounterV != 0.1) {
                    vgmFadeoutCounterV = 0.004;
                }

                if (vgmFadeoutCounter < 0.0) {
                    vgmFadeoutCounter = 0.0;
                }

                // After the fade out is complete, the music stops playing completely.
                if (vgmFadeoutCounter == 0.0) {
                    chipRegister.softReset(EnmModel.VirtualModel);
                    chipRegister.softReset(EnmModel.RealModel);

                    waveWriter.write(buffer, offset, i + 1);

                    waveWriter.close();

                    mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);

                    chipRegister.close();

                    //Thread.sleep(500); // Noise countermeasures

                    stopped = true;
logger.log(Level.DEBUG, "stop: " + stopped);

                    // Processing time per frame
//                    procTimePer1Frame = (int) ((double) stwh.ElapsedMilliseconds / (i + 1) * 1000000.0);
                    return i + 1;
                }
            }

            if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
                updateVisualVolume(buffer, offset);
            }

            waveWriter.write(buffer, offset, sampleCount);

            // Processing time per frame
//            procTimePer1Frame = (int) ((double) stwh.ElapsedMilliseconds / sampleCount * 1000000.0);
            return cnt;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
//            _fatalError = true;
            stopped = true;
        }

        return -1;
    }

    protected void naudioWrapPlaybackStopped(LineEvent e) {
//        if (e.getException != null) {
//            JOptionPane.showMessageDialog(null,
//                    "The device has stopped for some reason.\nMessage:\n%s\nStack trace:\n%s".formatted(
//                            e.Exception.Message, e.Exception.StackTrace)
//                    , "Error"
//                    , JOptionPane.ERROR_MESSAGE);
//            flgReinit = true;
//
//            try {
//                naudioWrap.Stop();
//            } catch (Exception ex) {
//                Log.forcedWrite(ex);
//            }
//
//        } else {
        try {
            stop();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
//        }
    }

    public void stop() {

        try {
            if (paused) pause();

            if (stopped) {
                trdClosed = true;
                while (!_trdStopped) { // TODO if realChip is not null, _trdStopped is false
                    Thread.sleep(1);
                }

                if (playingFileFormat != null && !playingFileFormat.isSampled()
                        && naudioFileReader != null) {
                    nAudioStop();
                }

                return;
            }

            if (!paused) {
                LineEvent.Type ps = naudioWrap.getPlaybackState();
                if (ps != null && ps != LineEvent.Type.STOP) {
                    vgmFadeoutCounterV = 0.1;
                    vgmFadeout = true;
                    int cnt = 0;
                    while (!stopped && cnt < 100) {
                        Thread.yield();
                        cnt++;
                    }
                }
            }
            trdClosed = true;

            if (naudioFileReader != null) {
                nAudioStop();
                return;
            }

            chipRegister.softReset(EnmModel.VirtualModel);
            chipRegister.softReset(EnmModel.RealModel);

            int timeout = 5000;
            while (!_trdStopped) {
                Thread.yield();
                timeout--;
                if (timeout < 1) break;
            }
            while (!stopped) {
                Thread.yield();
                timeout--;
                if (timeout < 1) break;
            }
            stopped = true;
new Exception().printStackTrace();
logger.log(Level.DEBUG, "stop: " + stopped + ", " + hashCode());

            chipRegister.softReset(EnmModel.VirtualModel);
            chipRegister.softReset(EnmModel.RealModel);

            //chipRegister.outMIDIData_Close();
            if (setting.getOther().getWavSwitch()) {
                Thread.sleep(500);
                waveWriter.close();
            }

            // DEBUG
            //vstparse();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public void nAudioStop() {
        try {
            AudioInputStream dmy = naudioFileReader;
            naudioFileReader = null;
            dmy.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private Audio() {
        logger.log(Level.DEBUG, "Audio:Init:STEP 01");

        naudioWrap = new NAudioWrap(setting.getOutputDevice().getSampleRate(), this::trdVgmVirtualFunction);
        naudioWrap.playbackStopped = this::naudioWrapPlaybackStopped;

        mds = new MDSound(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, null);

        chipRegister = new ChipRegister(mds);

        chipRegister.plugin(MidiPlugin.class).mdsInit();
        chipRegister.plugin(MidiPlugin.class).resetAll();
    }

    public static Audio getInstance() {
        return instance;
    }

//    public frmMain frmMain = null;

    public NAudioWrap naudioWrap;
    public WaveWriter waveWriter = null;

    public void close() {
        try {
            chipRegister.plugin(MidiPlugin.class).midiClose();
            chipRegister.plugin(RealChipPlugin.class).close();

            naudioWrap.stop();

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public Thread trdMain = null;
    public boolean trdClosed = false;
    private boolean _trdStopped = true;

    public synchronized boolean getTrdStopped() {
        return _trdStopped;
    }

    public synchronized void setTrdStopped(boolean value) {
new Exception("value: " + value).printStackTrace(System.err);
        _trdStopped = value;
    }

    private void updateVisualVolume(short[] buffer, int offset) {
        visVolume.master = buffer[offset];

        for (var i : mds.getFirstInstruments()) {
            var vs = i.getView("volume", null);
        }
    }

    public BaseDriver driverReal = null;

//    protected long sw = System.currentTimeMillis();
    public static final double swFreq = 1000d / 44100;

    public boolean stopped = false;

    public void setVolume(String tag, Class<? extends Instrument> c, boolean isAbs, int volume) {
        try {
            int v = Common.range((isAbs ? 0 : setting.getBalance().getVolume(tag, c)) + volume, -192, 20);
            mds.setVolume(tag, c, v);
            setting.getBalance().setVolume(tag, c, v);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public int masterVolume = 0;

    public void setMasterVolume(boolean isAbs, int volume) {
        masterVolume = Common.range((isAbs ? 0 : setting.getBalance().getMasterVolume()) + volume, -192, 20);
        setting.getBalance().setMasterVolume(masterVolume);
    }

    public void getPlayingFileName(String[] playingFileName, String[] playingArcFileName) {
        playingFileName[0] = null; // TODO
        playingArcFileName[0] = null;
    }

    public long getDriverCounter() {
        if (driverVirtual == null && driverReal == null) return -1;

        if (driverVirtual == null) {
            return driverReal.getDriverCounter();
        }
        if (driverReal == null) {
            return driverVirtual.getDriverCounter();
        }

        return driverVirtual.whichCounter(driverReal.getDriverCounter(), driverVirtual.getDriverCounter());
    }

    public boolean paused = false;
    public boolean vgmFadeout;

    public void fadeout() {
        if (isPaused()) {
            pause();
        }

        vgmFadeout = true;
    }

    public void pause() {
        try {
            paused = !paused;
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public boolean isPaused() {
        return paused;
    }
}
