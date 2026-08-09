package mdplayer.tool;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdsound.MDSound;

/**
 * Plays songs through the <em>production</em> path (the bundled
 * {@code DefaultVolumeBalance_*.xml} preset is loaded by {@code setParams} exactly as it is
 * during playback) and prints how loud each one actually comes out, so a "this song is too
 * quiet" report can be checked against numbers instead of ears.
 * <p>
 * Reported per song: the chips it uses with the preset volume each one got, the raw mixer RMS
 * ({@code driverVirtual.render}, pre master), and the RMS/peak after {@code MasterVolume}
 * ({@code Audio.render} multiplies by {@code 2 * 10^(master/40)}), which is what reaches the
 * output device.
 *
 * <pre>{@code
 *   java -cp <cp> mdplayer.tool.VolumeAuditProbe [--seconds N] <file> ...
 * }</pre>
 */
final class VolumeAuditProbe {

    /** print each chip's isolated full/active RMS instead of the song's mixed output */
    static boolean isolate = false;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        int i = 0;
        for (; i < args.length && args[i].startsWith("--"); i++) {
            if (args[i].equals("--seconds")) VolumeBalanceCalibrator.seconds = Integer.parseInt(args[++i]);
            if (args[i].equals("--isolate")) isolate = true;
        }
        VolumeBalanceCalibrator.applyLocalProperties();
        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);
        setting.getOther().setWavSwitch(true);
        setting.getAutoBalance().setUseThis(true); // production behaviour: load the preset

        if (isolate) {
            System.out.printf("%-46s %-13s %9s %9s %6s%n",
                    "file", "chip", "full", "active", "a/f dB");
        } else {
            System.out.printf("%-46s %5s %9s %9s %8s  %s%n",
                    "file", "mstr", "mixRms", "outRms", "outPeak", "chips (preset volume)");
        }
        for (; i < args.length; i++) {
            Path sample = Path.of(args[i]);
            try {
                if (isolate) isolate(sample); else audit(sample);
            } catch (Exception e) {
                System.out.printf("%-46s failed: %s%n", sample.getFileName(), e);
            }
        }
    }

    /** what the calibrator measures (active RMS) next to what is actually heard (full RMS):
     *  a sparse, percussive chip scores far higher on active than on full, and so is attenuated
     *  by the per-chip equalization even though its songs play quietly */
    static void isolate(Path sample) throws Exception {
        var tags = VolumeBalanceCalibrator.enumerateChips(sample);
        for (var chip : tags.keySet()) {
            var m = VolumeBalanceCalibrator.measureIsolated(sample, tags, chip);
            var main = mainOnly(sample, tags, chip);
            StringBuilder per = new StringBuilder();
            for (String tag : tags.get(chip)) {
                if (tag.equals(MDSound.Chip.MAIN_TAG)) continue;
                per.append(" ").append(tag).append('=')
                        .append("%.0f".formatted(mainOnly(sample, tags, chip, tag).full()));
            }
            System.out.printf("%-46.46s %-13s %9.1f %9.1f %6.1f %9.1f  %s%n",
                    sample.getFileName(), chip.getSimpleName(), m.full(), m.active(),
                    m.full() > 0 ? 20 * Math.log10(m.active() / m.full()) : 0, main.full(), per);
        }
    }

    /** same isolation, but only {@code MAIN} (plus {@code extra}, if given) is set to 0 -- MAIN alone
     *  is all the playback path ever applies, since sub-tag volumes are not pushed in at init */
    static VolumeBalanceCalibrator.Meas mainOnly(Path sample,
            Map<Class<? extends Chip>, Set<String>> all,
            Class<? extends mdplayer.Chip> target, String... extra) throws Exception {
        BasePlugin<? extends BaseDriver> plugin = VolumeBalanceCalibrator.build(sample);
        try {
            for (var e : all.entrySet()) {
                if (e.getKey().equals(target)) {
                    plugin.setVolume(MDSound.Chip.MAIN_TAG, e.getKey(), true, 0);
                    for (String tag : extra) plugin.setVolume(tag, e.getKey(), true, 0);
                } else {
                    for (String tag : e.getValue()) plugin.setVolume(tag, e.getKey(), true, -192);
                }
            }
            return VolumeBalanceCalibrator.renderMeas(plugin);
        } finally {
            VolumeBalanceCalibrator.close(plugin);
        }
    }

    static void audit(Path sample) throws Exception {
        BasePlugin<? extends BaseDriver> plugin = VolumeBalanceCalibrator.build(sample);
        try {
            StringBuilder chips = new StringBuilder();
            for (var e : plugin.getChipInstances().entrySet()) {
                Set<String> tags = new LinkedHashSet<>();
                for (MDSound.Chip c : e.getValue()) tags.addAll(c.setVolumes.keySet());
                if (tags.isEmpty()) tags.add(MDSound.Chip.MAIN_TAG);
                StringBuilder t = new StringBuilder();
                for (MDSound.Chip c : e.getValue()) {
                    t.append(t.isEmpty() ? "" : "/").append(c.volume).append("->tv").append(c.getTVolume());
                }
                chips.append(chips.isEmpty() ? "" : ", ")
                        .append(e.getKey().getSimpleName()).append('=').append(t);
            }
            var m = VolumeBalanceCalibrator.renderMeas(plugin);
            double g = 2.0 * Math.pow(10.0, plugin.masterVolume / 40.0);
            System.out.printf("%-46.46s %5d %9.1f %9.1f %8.0f  %s%n",
                    sample.getFileName(), plugin.masterVolume, m.full(), m.full() * g, m.peak() * g, chips);
        } finally {
            VolumeBalanceCalibrator.close(plugin);
        }
    }
}
