package mdplayer;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.sound.sampled.LineEvent;


public class NullOut implements Closeable {
    private boolean isNoWaitMode;

    public NullOut(boolean isNoWaitMode) {
        this.isNoWaitMode = isNoWaitMode;
    }

    public LineEvent.Type getPlaybackState() {
        return pbState;
    }

    public float getVolume() {
        return 0f;
    }

    public void setVolume(float value) {
    }

    public Consumer<LineEvent> playbackStopped;

    public void close() {
    }

    public void Init() {
         // 初期化
        try {
            os = Files.newOutputStream(Path.of(System.getProperty("dev.null")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void pause() {
    }

    public void play() {
         // レンダリング開始
        RequestPlay();
    }

    public void stop() {
         // レンダリング停止
        reqStop = true;
    }

    OutputStream os;
    private Thread trdMain;
    private byte[] buf = new byte[4000];
    private LineEvent.Type pbState = LineEvent.Type.STOP;
    private boolean reqStop = false;

    private void RequestPlay() {
        if (trdMain != null) {
            return;
        }

        reqStop = false;
        trdMain = new Thread(this::trdFunction);
        trdMain.setPriority(Thread.MAX_PRIORITY);
        trdMain.setDaemon(true);
        trdMain.setName("trdNullOutFunction");
        trdMain.start();
    }

    private void trdFunction() {
        pbState = LineEvent.Type.START;

        while (!reqStop) {
            try {
                os.write(buf, 0, 4000);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        pbState = LineEvent.Type.STOP;
    }
}
