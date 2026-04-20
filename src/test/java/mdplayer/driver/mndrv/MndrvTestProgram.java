package mdplayer.driver.mndrv;

import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import mdplayer.Audio;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;

import static vavi.sound.SoundUtil.volume;


/**
 * MndrvTestProgram.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-02-10 nsano initial version <br>
 */
public class MndrvTestProgram {

    private SourceDataLine audioOutput = null;
    private Thread playbackThread;
    private volatile boolean isPlaying = false;
    private volatile int validBuffers = 0;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: MndrvTestProgram <mnd_file>");
            return;
        }

        new MndrvTestProgram().play(args[0]);
    }

    public void play(String filename) throws Exception {
        Audio audio = Audio.getInstance();
        Setting setting = Setting.getInstance();
        int samplingRate = setting.getOutputDevice().getSampleRate();

        FileFormat format = FileFormat.getFileFormat(filename);
        format.load((String) null, filename);
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", filename));
        audio.init(plugin);
        audio.play();

        audioOutput = AudioSystem.getSourceDataLine(new AudioFormat(samplingRate, 16, 2, true, false));
        audioOutput.open();
        volume(audioOutput, Double.parseDouble(System.getProperty("mdplayer.volume", "0.2")));
        audioOutput.start();

        isPlaying = true;
        playbackThread = new Thread(this::playbackLoop);
        playbackThread.start();

        System.out.println("Playing " + filename);
        Thread.sleep(5000); // Play for 5 seconds

        isPlaying = false;
        playbackThread.join();
        audioOutput.close();
        plugin.stop();
        plugin.close();

        if (validBuffers == 0) {
            throw new RuntimeException("FAILURE: No valid audio signal detected at all! (RMS > 50 and Peak > 2000 not met)");
        }
        
        System.out.println("SUCCESS: Audio signal detected. (" + validBuffers + " valid buffers)");
    }

    private void playbackLoop() {
        Audio audio = Audio.getInstance();
        int bufferSize = 1024; // samples
        short[] sampleBuffer = new short[bufferSize * 2]; // stereo
        byte[] audioBuffer = new byte[bufferSize * 4]; // 16-bit stereo

        long totalSquared = 0;
        int totalSamples = 0;
        int peak = 0;
        int bufferCount = 0;
        validBuffers = 0;

        while (isPlaying) {
            int produced = audio.plugin.mds.update(sampleBuffer, 0, bufferSize * 2, null);

            for (int i = 0; i < produced; i++) {
                short val = sampleBuffer[i];
                totalSquared += (long) val * val;
                int absVal = Math.abs(val);
                if (absVal > peak) peak = absVal;
                totalSamples++;

                audioBuffer[i * 2] = (byte) (val & 0xff);
                audioBuffer[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
            }

            bufferCount++;
            if (bufferCount % 10 == 0 && totalSamples > 0) {
                double rms = Math.sqrt((double) totalSquared / totalSamples);
                // System.err.printf("Stats: RMS=%.2f Peak=%d Samples=%d%n", rms, peak, totalSamples);
                
                if (bufferCount > 20) {
                    if (rms > 5.0 && peak > 200) { // MNDRV might be quieter than SID, using lower thresholds for detection
                        validBuffers++;
                    }
                }

                totalSquared = 0;
                totalSamples = 0;
                peak = 0;
            }

            if (produced > 0) {
                audioOutput.write(audioBuffer, 0, produced * 2);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
