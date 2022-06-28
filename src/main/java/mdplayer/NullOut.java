package mdplayer;

import java.io.Closeable;
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

    public void Init(IWaveProvider waveProvider) {
        //初期化
        wP = waveProvider;
    }

    public void pause() {
    }

    public void play() {
        //レンダリング開始
        RequestPlay();
    }

    public void stop() {
        //レンダリング停止
        reqStop = true;
    }

    private Thread trdMain;
    private IWaveProvider wP;
    private byte[] buf = new byte[4000];
    private LineEvent.Type pbState = LineEvent.Type.STOP;
    private boolean reqStop = false;

    private void RequestPlay() {
        if (trdMain != null) {
            return;
        }

        reqStop = false;
        trdMain = new Thread(new ThreadStart(trdFunction));
        trdMain.Priority = ThreadPriority.Highest;
        trdMain.IsBackground = true;
        trdMain.setName("trdNullOutFunction");
        trdMain.Start();
    }

    private void trdFunction() {
        pbState = PlaybackState.Playing;

        while (!reqStop) {
            wP.read(buf, 0, 4000);
        }

        pbState = PlaybackState.Stopped;
    }
}
