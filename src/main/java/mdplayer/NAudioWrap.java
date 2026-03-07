package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

import static java.lang.System.getLogger;
import static vavi.sound.SoundUtil.volume;


public class NAudioWrap {

    private static final Logger logger = getLogger(NAudioWrap.class.getName());

    public Consumer<LineEvent> playbackStopped;

    private SourceDataLine dsOut;
    private NullOut nullOut;

    int sampleRate;
    private final Setting setting = Setting.getInstance();
    private final SynchronizationContext syncContext = SynchronizationContext.getCurrent();

    static final UUID Empty = new UUID(0, 0);

    public NAudioWrap(int sampleRate) {
        init(sampleRate);
    }

    public void init(int sampleRate) {
        stop();
        this.sampleRate = sampleRate;
    }

    public void start() {
        if (dsOut != null) dsOut.close();
        dsOut = null;
        if (nullOut != null) nullOut.close();
        nullOut = null;

        try {
logger.log(Level.DEBUG, "OutputDeviceType: " + setting.getOutputDevice().getDeviceType());
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
logger.log(Level.DEBUG, format);
                dsOut.addLineListener(this::DeviceOut_PlaybackStopped);
                dsOut.open();
                volume(dsOut, Double.parseDouble(System.getProperty("mdplayer.volume", "0.2")));
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void DeviceOut_PlaybackStopped(LineEvent e) {
        Consumer<LineEvent> handler = this.playbackStopped;
logger.log(Level.DEBUG, "line: " + e.getType());
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
     * Do not call it from within a callback (it will hang)
     */
    public void stop() {
logger.log(Level.INFO, "stop enter");
        if (dsOut != null) {
            try {
//                dsOut.drain(); // TODO this blocks to stop
                dsOut.stop();
                dsOut.close();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
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
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            nullOut = null;
        }

         // Take a break
//        for (int i = 0; i < 10; i++) {
//            Thread.sleep(1);
//            JApplication.DoEvents();
//        }
    }

//OutputStream os;

    public int write(short[] buffer, int offset, int count) {
//try {
// if (os == null) { os = Files.newOutputStream(Paths.get("tmp", "out.pcm")); }

        ByteBuffer bb = ByteBuffer.allocate(count * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(buffer, offset, count);
        sb.rewind();
// os.write(bb.array());
//logger.log(Level.TRACE, "write to line\n" + StringUtil.getDump(bb.array()));
        if (dsOut != null)
            return dsOut.write(bb.array(), 0, count * Short.BYTES);
        else
            return count * Short.BYTES;
//} catch (IOException e) {
//    throw new UncheckedIOException(e);
//}
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
