package mdplayer;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.UUID;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import dotnet4j.threading.SynchronizationContext;
import dotnet4j.util.compat.TriFunction;
import vavi.util.Debug;
import vavi.util.StringUtil;


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
        init(sampleRate, nCallBack);
    }

    public void init(int sampleRate, naudioCallBack nCallBack) {

        stop();

        this.sampleRate = sampleRate;
        callBack = nCallBack; // maybe no need
    }

    public void start(Setting setting) {
        this.setting = setting;
        if (dsOut != null) dsOut.close();
        dsOut = null;
        if (nullOut != null) nullOut.close();
        nullOut = null;

        try {
Debug.println("OutputDeviceType: " + setting.getOutputDevice().getDeviceType());
            switch (setting.getOutputDevice().getDeviceType()) {
            case 0: // wave out
                break;
            case 1: // direct sound
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
Debug.println(format);
                dsOut.addLineListener(this::DeviceOut_PlaybackStopped);
                dsOut.open();
                dsOut.start();
                break;
            case 2: // mmdevice???
                break;
            case 3: // asio
                break;

            case 5: // null
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
        Consumer<LineEvent> handler = this.playbackStopped;
Debug.println("line: " + e.getType());
        if (e.getType() == Type.STOP) {
            if (handler != null) {
                if (this.syncContext == null) {
                    handler.accept(e);
                } else {
                    syncContext.post(state -> handler.accept(e), null);
                }
            }
        }
    }

    /**
     * コールバックの中から呼び出さないこと(ハングします)
     */
    public void stop() {
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
//        for (int i = 0; i < 10; i++) {
//            Thread.sleep(1);
//            JApplication.DoEvents();
//        }
    }

    public int write(short[] buffer, int offset, int count) {
        ByteBuffer bb = ByteBuffer.allocate(count * Short.BYTES - offset);
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(buffer, offset, count);
        sb.rewind();
//Debug.println("write to line\n" + StringUtil.getDump(bb.array()));
        return dsOut.write(bb.array(), 0, count * Short.BYTES);
    }

    public LineEvent.Type getPlaybackState() {
        if (dsOut != null) {
            if (!dsOut.isRunning()) return LineEvent.Type.STOP;
        }
        if (nullOut != null) {
            return nullOut.getPlaybackState();
        }
        return null;
    }
}
