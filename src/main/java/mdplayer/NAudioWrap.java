package mdplayer;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import dotnet4j.threading.SynchronizationContext;
import dotnet4j.util.compat.TriFunction;
import vavi.util.Debug;


public class NAudioWrap {

    public interface naudioCallBack extends TriFunction<short[], Integer, Integer, Integer> {
    }

    public Consumer<LineEvent> playbackStopped;

    private SourceDataLine dsOut;
    private NullOut nullOut;

    int sampleRate;
    private static naudioCallBack callBack = null;
    private Setting setting = null;
    private SynchronizationContext syncContext = SynchronizationContext.getCurrent();

    static final UUID Empty = new UUID(0, 0);

    public NAudioWrap(int sampleRate, naudioCallBack nCallBack) {
        Init(sampleRate, nCallBack);
    }

    public void Init(int sampleRate, naudioCallBack nCallBack) {

        Stop();

        this.sampleRate = sampleRate;
        callBack = nCallBack;
    }

    public void Start(Setting setting) {
        this.setting = setting;
        if (dsOut != null) dsOut.close();
        dsOut = null;
        if (nullOut != null) nullOut.close();
        nullOut = null;

        try {
            switch (setting.getOutputDevice().getDeviceType()) {
            case 0:
                break;
            case 1:
                Line.Info g = null;
                Mixer.Info [] mixersInfo = AudioSystem.getMixerInfo();
                for (Mixer.Info mixerInfo : mixersInfo) {
                    Mixer mixer = AudioSystem.getMixer(mixerInfo);
                    Line.Info [] sourceLineInfo = mixer.getSourceLineInfo();
                    for (Line.Info info : sourceLineInfo) {
                        if (info instanceof DataLine.Info dataLineInfo)
                            if (setting.getOutputDevice().getDirectSoundDeviceName().equals(dataLineInfo.toString())) {
                                g = info;
                                break;
                            }
                    }
                }
                AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);
                if (g == null) {
                    dsOut = AudioSystem.getSourceDataLine(format);
                } else {
                    dsOut = AudioSystem.getSourceDataLine(format);
                }
                dsOut.addLineListener(this::DeviceOut_PlaybackStopped);
                dsOut.open();
                dsOut.start();
                break;
            case 2:
                break;
            case 3:
                break;

            case 5:
                nullOut = new NullOut(true);
                nullOut.Init();
                nullOut.play();
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void DeviceOut_PlaybackStopped(LineEvent e) {
        Consumer handler = this.playbackStopped;
        if (handler != null) {
            if (this.syncContext == null) {
                handler.accept(e);
            } else {
                syncContext.post(state -> handler.accept(e), null);
            }
        }
    }

    /**
     * コールバックの中から呼び出さないこと(ハングします)
     */
    public void Stop() {
        if (dsOut != null) {
            try {
                dsOut.drain();
                dsOut.stop();
                dsOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dsOut = null;
        }

        if (nullOut != null) {
            try {
                nullOut.stop();
                while (nullOut.getPlaybackState() != LineEvent.Type.STOP) {
                    Thread.sleep(1);
                }
                nullOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            nullOut = null;
        }

         // 一休み
        //for (int i = 0; i < 10; i++) {
        //    Thread.sleep(1);
        //    JApplication.DoEvents();
        //}
    }

    public LineEvent.Type GetPlaybackState() {
        if (dsOut != null) {
            if (!dsOut.isRunning()) return LineEvent.Type.STOP;
        }
        if (nullOut != null) {
            return nullOut.getPlaybackState();
        }
        return null;
    }
}
