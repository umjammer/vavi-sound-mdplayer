package mdplayer.tool;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;

/**
 * Verification counterpart of {@link VolumeBalanceCalibrator}: renders each sample the way playback
 * really does -- the preset balance auto-loaded by {@code BasePlugin#setParams}, the master volume
 * applied the way {@code Audio.render} applies it -- and prints how loud it comes out, so a
 * calibrated preset set can be checked instead of only recomputed.
 * <p>
 * A song that shows {@code rms=0} with {@code midi>0} is played entirely through
 * {@link MidiPlugin}: it renders nothing here because it is heard from the synthesizer at the other
 * end of the MIDI path, which only {@code Balance.getMidiVolume} reaches.
 *
 * <pre>{@code
 *   mvn -o test-compile
 *   mvn -o -P loudness antrun:run [-Dseconds=8] [-Dargs='tmp/zms/Clear.ZMS ...']
 * }</pre>
 * With no file arguments every sample listed in {@code local.properties} is measured.
 */
public final class LoudnessCheck {

    /** seconds of audio to render per sample */
    static int seconds = 10;

    /** |sample| below this is treated as silence and excluded from the "active" rms */
    static final double NOISE_FLOOR = 64.0;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        List<Path> samples = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "--seconds" -> seconds = Integer.parseInt(args[++i]);
            default -> samples.add(Path.of(args[i]));
            }
        }

        VolumeBalanceCalibrator.applyLocalProperties();
        if (samples.isEmpty()) samples = VolumeBalanceCalibrator.listSampleFiles();

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);
        setting.getOther().setWavSwitch(true);
        // the point of this tool is what the presets do, so they have to be loaded
        setting.getAutoBalance().setUseThis(true);
        setting.getAutoBalance().setLoadDriverBalance(true);

        System.out.printf("%n%-34s %-7s %6s %9s %8s %8s %8s %6s  %s%n",
                "sample", "driver", "master", "rms", "peak", "dBFSrms", "dBFSpk", "midi", "chips");
        for (Path sample : samples) {
            try {
                measure(sample);
            } catch (Exception e) {
                System.out.printf("%-34s failed: %s%n", sample.getFileName(), e);
            }
        }
    }

    /** play one sample through the real path and print its loudness */
    static void measure(Path sample) throws Exception {
        String driver = VolumeBalanceCalibrator.driverToken(sample);
        FileFormat format = FileFormat.getFileFormat(sample.toString());
        // see VolumeBalanceCalibrator#build for why the stream is opened this way
        try (InputStream in = vavi.util.archive.Archives.getInputStream(
                new BufferedInputStream(new java.io.FileInputStream(sample.toFile())))) {
            format.load(in, sample.toString());
        }
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", sample.toString(), "midiMode", 0, "songNo", 0));
        plugin.prepare();
        try {
            MidiPlugin midi = plugin.chipRegister.plugin(MidiPlugin.class);
            long midiBefore = midi.sentMessages();
            int rate = Setting.getInstance().getOutputDevice().getSampleRate();
            long wanted = (long) rate * seconds * 2; // stereo shorts
            // what Audio.render multiplies the mix by; recomputed there per sample, constant here
            int mul = (int) (16384.0 * Math.pow(10.0, plugin.masterVolume / 40.0));
            short[] buffer = new short[8192];
            double sumSq = 0, activeSumSq = 0, peak = 0;
            long n = 0, activeN = 0;
            while (n < wanted) {
                int ret = plugin.getDriver().render(buffer, 0, buffer.length);
                if (ret <= 0) break;
                for (short raw : buffer) {
                    short s = (short) Math.clamp((raw * mul) >> 13, -0x8000, 0x7fff);
                    double sq = (double) s * s;
                    sumSq += sq;
                    n++;
                    peak = Math.max(peak, Math.abs(s));
                    if (Math.abs(s) >= NOISE_FLOOR) {
                        activeSumSq += sq;
                        activeN++;
                    }
                }
                if (plugin.driverVirtual != null && plugin.driverVirtual.stopped) break;
            }
            double full = n == 0 ? 0 : Math.sqrt(sumSq / n);
            double active = activeN == 0 ? 0 : Math.sqrt(activeSumSq / activeN);
            Set<String> chips = new LinkedHashSet<>();
            for (var chip : plugin.getChipInstances().keySet()) chips.add(chip.getSimpleName());
            System.out.printf("%-34s %-7s %6d %9.1f %8.0f %8.1f %8.1f %6d  %s%s%n",
                    sample.getFileName(), driver, plugin.masterVolume, full, peak,
                    dbfs(full), dbfs(peak), midi.sentMessages() - midiBefore, chips,
                    full < 1 ? "  <-- no chip audio (active=" + (long) active + ")" : "");
        } finally {
            try { plugin.stop(); } catch (Exception ignore) {}
            try { plugin.close(); } catch (Exception ignore) {}
            try { plugin.chipRegister.close(); } catch (Exception ignore) {}
        }
    }

    static double dbfs(double amplitude) {
        return amplitude <= 0 ? Double.NEGATIVE_INFINITY : 20.0 * Math.log10(amplitude / 32767.0);
    }
}
