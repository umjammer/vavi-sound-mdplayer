package mdplayer.driver.mndrv;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;

import static mdplayer.plugin.BasePlugin.BUFFER_SIZE;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * MndrvWavTestProgram.
 * <p>
 * Copy of MndrvTestProgram with playbackLoop modified
 * to write WAV file instead of playing to speakers.
 * Uses the built-in waveWriter via settings.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-02-15 nsano initial version <br>
 */
public class MndrvWavTestProgram {

    static {
        System.setProperty("mdplayer.variant.ymf262", "0");
        System.setProperty("dev.null", "/dev/null");
        System.setProperty("javax.sound.sampled.SourceDataLine", "#WaveOut Mixer");
    }

    /** duration to render in seconds (matching reference wav) */
    private static final double RENDER_DURATION = 99.45;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: MndrvWavTestProgram <mnd_file> [reference_wav]");
            return;
        }

        new MndrvWavTestProgram().play(args[0], args.length > 2 ? args[2] : null);
    }

    public void play(String filename, String refWavFile) throws Exception {
        System.err.println("filename: " + filename);
        System.err.println("refWavFile: " + refWavFile);
        Audio audio = Audio.getInstance();
        Setting setting = Setting.getInstance();

        // disable speaker output, enable WAV writer
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);
        setting.getOther().setWavSwitch(true);

        FileFormat format = FileFormat.getFileFormat(filename);
        var r = format.load((String) null, filename);
        BasePlugin plugin = (BasePlugin) format.getPlugin();
        plugin.setBuffer(format, r.getItem1(), filename, null, 0, 0, r.getItem2());

        // Initialize driver and chips without starting the infinite loop in BasePlugin.play()
        plugin.driverVirtual = new MnDriver();
        ((MnDriver) plugin.driverVirtual).setExtendFile(r.getItem2());
        java.lang.reflect.Method _play = plugin.getClass().getDeclaredMethod("_play");
        _play.setAccessible(true);
        _play.invoke(plugin);

        System.err.println("Rendering " + filename + " to WAV...");
        // Instead of plugin.play(), we run our own loop to ensure we can stop it.
        // MNDPlugin.play() would call super.play() which has an infinite loop.

        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;
        plugin.fadeoutCounter = 1.0;
        plugin.fadeoutCounterV = 0.00001;
        audio.plugin.masterVolume = setting.getBalance().getMasterVolume();

        long start = System.currentTimeMillis();
        long timeout = (long) (RENDER_DURATION * 1000) + 10000; // duration + 10s buffer

        while (!plugin.stopped) {
            short[] buffer = new short[BUFFER_SIZE];
            int ret = audio.plugin.mds.update(buffer, 0, buffer.length, null);
            if (ret == -1) break;
            if (plugin.driverVirtual.getDriverCounter() % 1000 == 0) System.err.println("Frame: " + plugin.driverVirtual.getDriverCounter());

            if (System.currentTimeMillis() - start > timeout) {
                System.err.println("Render timeout reached, stopping...");
                break;
            }

            if (plugin.driverVirtual != null && plugin.driverVirtual.stopped) {
                System.err.println("Driver signaled stop, stopping...");
                break;
            }
        }

        // Finalize rendering
        plugin.stopped = true;
        plugin.stop();
        plugin.close();

        System.err.println("Rendering complete.");

        String actualOutWavFile = System.getProperty("vavi.sound.sampled.misc.waveout");
        if (refWavFile != null && new File(actualOutWavFile).exists()) {
            compareWavFiles(refWavFile, actualOutWavFile);
        }
    }

    /** compare two wav files and report similarity metrics */
    static void compareWavFiles(String refPath, String outPath) throws Exception {
System.out.println("out size: " + new File(outPath).length());
        AudioInputStream refAis = AudioSystem.getAudioInputStream(new File(refPath));
        AudioInputStream outAis = AudioSystem.getAudioInputStream(new File(outPath));

        System.out.println("Reference: " + refAis.getFormat());
        System.out.println("Output:    " + outAis.getFormat());

        byte[] refBytes = refAis.readAllBytes();
        byte[] outBytes = outAis.readAllBytes();
        refAis.close();
        outAis.close();

        int refSamples = refBytes.length / 2;
        int outSamples = outBytes.length / 2;
        int minSamples = Math.min(refSamples, outSamples);

        System.out.println("Reference samples: " + refSamples + ", Output samples: " + outSamples);

        long totalDiffSquared = 0;
        long maxDiff = 0;
        long refEnergy = 0;
        long outEnergy = 0;
        long crossCorr = 0;

        for (int i = 0; i < minSamples; i++) {
            int refVal = (short) ((refBytes[i * 2] & 0xff) | (refBytes[i * 2 + 1] << 8));
            int outVal = (short) ((outBytes[i * 2] & 0xff) | (outBytes[i * 2 + 1] << 8));
            int diff = refVal - outVal;
            totalDiffSquared += (long) diff * diff;
            if (Math.abs(diff) > maxDiff) maxDiff = Math.abs(diff);
            refEnergy += (long) refVal * refVal;
            outEnergy += (long) outVal * outVal;
            crossCorr += (long) refVal * outVal;
        }

        double rmsDiff = Math.sqrt((double) totalDiffSquared / minSamples);
        double refRms = Math.sqrt((double) refEnergy / minSamples);
        double outRms = Math.sqrt((double) outEnergy / minSamples);
        double snr = refEnergy > 0 ? 10.0 * Math.log10((double) refEnergy / totalDiffSquared) : Double.POSITIVE_INFINITY;
        double correlation = (refEnergy > 0 && outEnergy > 0)
                ? crossCorr / (Math.sqrt((double) refEnergy) * Math.sqrt((double) outEnergy))
                : 0;

        System.out.println("=== WAV Comparison Results ===");
        System.out.println("Reference RMS: " + String.format("%.2f", refRms));
        System.out.println("Output RMS:    " + String.format("%.2f", outRms));
        System.out.println("Diff RMS:      " + String.format("%.2f", rmsDiff));
        System.out.println("Max Diff:      " + maxDiff);
        System.out.println("SNR (dB):      " + String.format("%.2f", snr));
        System.out.println("Correlation:   " + String.format("%.6f", correlation));

        assertTrue(outRms > 200 && Math.abs(correlation) > 0.85, "quality test");
    }
}
