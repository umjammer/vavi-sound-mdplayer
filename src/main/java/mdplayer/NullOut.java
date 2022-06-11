package mdplayer;

import java.io.Closeable;

import javax.sound.sampled.Clip;

import javafx.event.EventHandler;


public class NullOut implements Clip, Closeable {
    private Boolean isNoWaitMode;

    public NullOut(Boolean isNoWaitMode) {
        this.isNoWaitMode = isNoWaitMode;
    }

    public PlaybackState getPlaybackState() {
        return pbState;
    }

    public float getVolume() {
        return 0f;
    }

    public void setVolume(float value) {
        ;
    }

    public EventHandler<StoppedEventArgs> PlaybackStopped;

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
    private PlaybackState pbState = PlaybackState.Stopped;
    private Boolean reqStop = false;

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
