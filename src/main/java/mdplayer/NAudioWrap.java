package mdplayer;

import java.util.UUID;
import java.util.function.BiConsumer;

import javafx.event.EventHandler;


public class NAudioWrap {

    public interface naudioCallBack extends Common.TriFunction<short[], Integer, Integer, Integer> {
    }

    public EventHandler<StoppedEventArgs> PlaybackStopped;

    private WaveOutEvent waveOut;
    private WasapiOut wasapiOut;
    private DirectSoundOut dsOut;
    private AsioOut asioOut;
    private NullOut nullOut;
    private SineWaveProvider16 waveProvider;

    private static naudioCallBack callBack = null;
    private Setting setting = null;
    private SynchronizationContext syncContext = SynchronizationContext.Current;

    static final UUID Empty = new UUID(0, 0);

    public NAudioWrap(int sampleRate, naudioCallBack nCallBack) {
        Init(sampleRate, nCallBack);
    }

    public void Init(int sampleRate, naudioCallBack nCallBack) {

        Stop();

        waveProvider = new SineWaveProvider16();
        waveProvider.SetWaveFormat(sampleRate, 2);

        callBack = nCallBack;

    }

    public void Start(Setting setting) {
        this.setting = setting;
        if (waveOut != null) waveOut.close();
        waveOut = null;
        if (wasapiOut != null) wasapiOut.Dispose();
        wasapiOut = null;
        if (dsOut != null) dsOut.Dispose();
        dsOut = null;
        if (asioOut != null) asioOut.Dispose();
        asioOut = null;
        if (nullOut != null) nullOut.Dispose();
        nullOut = null;

        try {
            switch (setting.getOutputDevice().getDeviceType()) {
            case 0:
                waveOut = new WaveOutEvent();
                waveOut.DeviceNumber = 0;
                waveOut.DesiredLatency = setting.getOutputDevice().getLatency();
                for (int i = 0; i < WaveOut.DeviceCount; i++) {
                    if (setting.getOutputDevice().WaveOutDeviceName == WaveOut.GetCapabilities(i).ProductName) {
                        waveOut.DeviceNumber = i;
                        break;
                    }
                }
                waveOut.PlaybackStopped += DeviceOut_PlaybackStopped;
                waveOut.Init(waveProvider);
                waveOut.Play();
                break;
            case 1:
                UUID g = Empty;
                for (DirectSoundDeviceInfo d : DirectSoundOut.Devices) {
                    if (setting.getOutputDevice().DirectSoundDeviceName == d.Description) {
                        g = d.Guid;
                        break;
                    }
                }
                if (g == Empty) {
                    dsOut = new DirectSoundOut(setting.getOutputDevice().getLatency());
                } else {
                    dsOut = new DirectSoundOut(g, setting.getOutputDevice().getLatency());
                }
                dsOut.PlaybackStopped(this::DeviceOut_PlaybackStopped);
                dsOut.Init(waveProvider);
                dsOut.Play();
                break;
            case 2:
                MMDevice dev = null;
                var enumerator = new MMDeviceEnumerator();
                var endPoints = enumerator.EnumerateAudioEndPoints(DataFlow.Render, DeviceState.Active);
                for (var endPoint : endPoints) {
                    if (setting.getOutputDevice().getWasapiDeviceName().equals(String.format("%s (%s)", endPoint.FriendlyName, endPoint.DeviceFriendlyName))) {
                        dev = endPoint;
                        break;
                    }
                }
                if (dev == null) {
                    wasapiOut = new WasapiOut(setting.getOutputDevice().WasapiShareMode ? AudioClientShareMode.Shared : AudioClientShareMode.Exclusive, setting.getOutputDevice().Latency);
                } else {
                    wasapiOut = new WasapiOut(dev, setting.getOutputDevice().WasapiShareMode ? AudioClientShareMode.Shared : AudioClientShareMode.Exclusive, false, setting.getOutputDevice().Latency);
                }
                wasapiOut.PlaybackStopped(this::DeviceOut_PlaybackStopped);
                wasapiOut.Init(waveProvider);
                wasapiOut.Play();
                break;
            case 3:
                if (AsioOut.isSupported()) {
                    int i = 0;
                    for (String s : AsioOut.GetDriverNames()) {
                        if (setting.getOutputDevice().getAsioDeviceName() == s) {
                            break;
                        }
                        i++;
                    }
                    asioOut = new AsioOut(i);
                    asioOut.PlaybackStopped(this::DeviceOut_PlaybackStopped);
                    asioOut.Init(waveProvider);
                    asioOut.Play();
                }
                break;

            case 5:
                nullOut = new NullOut(true);
                nullOut.PlaybackStopped(this::DeviceOut_PlaybackStopped);
                nullOut.Init(waveProvider);
                nullOut.Play();
                break;
            }
        } catch (Exception ex) {
            Log.forcedWrite(ex);
            waveOut = new WaveOutEvent();
            waveOut.PlaybackStopped(this::DeviceOut_PlaybackStopped);
            waveOut.Init(waveProvider);
            waveOut.DeviceNumber = 0;
            waveOut.Play();
        }

    }

    private void DeviceOut_PlaybackStopped(Object sender, StoppedEventArgs e) {
        BiConsumer handler = this::PlaybackStopped;
        if (handler != null) {
            if (this.syncContext == null) {
                handler.accept(this, e);
            } else {
                syncContext.Post(state -> handler.accept(this, e), null);
            }
        }
    }

    /**
     * コールバックの中から呼び出さないこと(ハングします)
     */
    public void Stop() {
        if (waveOut != null) {
            try {
                //waveOut.Pause();
                waveOut.Stop();
                while (waveOut.PlaybackState != PlaybackState.Stopped) {
                    Thread.sleep(1);
                }
                waveOut.Dispose();
            } catch (Exception e) {
            }
            waveOut = null;
        }

        if (wasapiOut != null) {
            try {
                //wasapiOut.Pause();
                wasapiOut.Stop();
                while (wasapiOut.PlaybackState != PlaybackState.Stopped) {
                    Thread.sleep(1);
                }
                wasapiOut.Dispose();
            } catch (Exception e) {
            }
            wasapiOut = null;
        }

        if (dsOut != null) {
            try {
                //dsOut.Pause();
                dsOut.Stop();
                while (dsOut.PlaybackState != PlaybackState.Stopped) {
                    Thread.sleep(1);
                }
                dsOut.Dispose();
            } catch (Exception e) {
            }
            dsOut = null;
        }

        if (asioOut != null) {
            try {
                //asioOut.Pause();
                asioOut.Stop();
                while (asioOut.PlaybackState != PlaybackState.Stopped) {
                    Thread.sleep(1);
                }
                asioOut.Dispose();
            } catch (Exception e) {
            }
            asioOut = null;
        }

        if (nullOut != null) {
            try {
                nullOut.Stop();
                while (nullOut.PlaybackState != PlaybackState.Stopped) {
                    Thread.sleep(1);
                }
                nullOut.Dispose();
            } catch (Exception e) {
            }
            nullOut = null;
        }

        //一休み
        //for (int i = 0; i < 10; i++)
        //{
        //    Thread.sleep(1);
        //    JApplication.DoEvents();
        //}
    }

    public class SineWaveProvider16 extends WaveProvider16 {
        public SineWaveProvider16() {
        }

        @Override
        public int Read(short[] buffer, int offset, int count) {
            return callBack(buffer, offset, count);
        }
    }

    public NAudio.Wave.PlaybackState GetPlaybackState() {
        Boolean notNull = false;

        if (waveOut != null) {
            if (waveOut.PlaybackState != PlaybackState.Stopped) return waveOut.PlaybackState;
        }
        if (dsOut != null) {
            if (dsOut.PlaybackState != PlaybackState.Stopped) return dsOut.PlaybackState;
        }
        if (wasapiOut != null) {
            if (wasapiOut.PlaybackState != PlaybackState.Stopped) return wasapiOut.PlaybackState;
        }
        if (asioOut != null) {
            if (asioOut.PlaybackState != PlaybackState.Stopped) return asioOut.PlaybackState;
        }
        if (nullOut != null) {
            if (nullOut.PlaybackState != PlaybackState.Stopped) return nullOut.PlaybackState;
        }

        return notNull ? (PlaybackState) PlaybackState.Stopped : null;
    }

    public int getAsioLatency() {
        if (asioOut == null) return 0;

        return asioOut.PlaybackLatency;
    }
}
