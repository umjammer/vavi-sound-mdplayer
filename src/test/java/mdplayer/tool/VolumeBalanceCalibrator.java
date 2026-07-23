package mdplayer.tool;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import mdsound.MDSound;

/**
 * Measures and determines per-driver / per-chip volume balances and rewrites the
 * {@code DefaultVolumeBalance_*.xml} presets.
 * <p>
 * Method (see {@code AGENTS.md} mission): for each driver sample listed in
 * {@code local.properties}, render headless and measure each chip <em>in isolation</em>
 * (mute every other chip to {@code -192}). Because the mdsound mixer's normalization
 * ({@code volumeMul = 16384/total}) is computed from the chip set only and is independent
 * of the per-chip user volume, an isolated chip's RMS equals its true contribution to the
 * full mix, so isolation is a valid measurement. Each chip is then attenuated to the level
 * of the quietest chip (equal-loudness, attenuation-only so nothing clips or hits the
 * {@code +20} boost cap).
 * <p>
 * Cross-driver leveling is done <em>downward</em> in a second global phase: every driver's full
 * mix is measured, the quietest driver's RMS becomes the common target, and each driver's
 * {@code MasterVolume} attenuates it down to that target. This matters because the master gain
 * offers only about {@code +10 dB} of headroom ({@code 0..+20}) but a huge attenuation range
 * (down to {@code -192}); leveling <em>up</em> to an absolute target clamps every quiet driver at
 * {@code +20} (leaving the differences audible), whereas leveling down never clamps. A full run
 * (no {@code --only}) is required for a globally consistent result.
 * <p>
 * The chip/master volume unit is 2&times;dB: the mixer applies {@code 10^(v/40)}
 * (see {@code MDSound.Chip.setDefaultVolume} and {@code Audio.render}), hence
 * {@code v = 40*log10(gain)}, clamped to {@code [-192, 20]}.
 * <p>
 * Limitations (minimal first pass): balances at chip-class granularity — a chip's computed
 * gain is written to all of its mixer tags equally, sub-tag (FM/SSG/&hellip;) ratios keep
 * their defaults. NES-family chips ignore the volume field (mdsound forces it to 0 for
 * {@code NesInst}), so NSF gets {@code MasterVolume} leveling only. Only {@code inst(0)}
 * of a multi-instance chip is muted during isolation.
 *
 * <pre>{@code
 *   mvn -o test-compile
 *   java -cp <cp> mdplayer.tool.VolumeBalanceCalibrator [--dry-run] [--seconds N]
 * }</pre>
 */
public final class VolumeBalanceCalibrator {

    /** resources dir the presets are read from / written to (source tree, so edits persist) */
    static final Path RESOURCES = Path.of("src/main/resources/mdplayer/resources");

    /** seconds of audio to render per measurement */
    static int seconds = 5;

    /** at most this many samples measured per driver (default: no cap, measure everything) */
    static int maxSamplesPerDriver = Integer.MAX_VALUE;

    /** every chip class actually measured across the whole run (for the coverage report) */
    static final Set<Class<? extends mdplayer.Chip>> covered = new LinkedHashSet<>();

    /** fallback leveling target if no driver produced a usable full-mix measurement */
    static final double TARGET_MIX_RMS = 4000.0;

    /** a driver full-mix RMS below this is treated as a broken/near-silent measurement and
     *  excluded when picking the global leveling target (so one dud can't crush every driver) */
    static final double MIN_MEAS_FLOOR = 80.0;

    /** |sample| below this is treated as silence and excluded from a chip's "active RMS" */
    static final double NOISE_FLOOR = 64.0;

    /** chips whose active RMS is below this are treated as "not sounding" and left neutral */
    static final double SILENCE_RMS = 20.0;

    static boolean dryRun = false;

    /** when non-empty, only these driver tokens are calibrated */
    static Set<String> only = Set.of();

    /** explicit extra sample files (pathSep-separated) added on top of the local.properties scan */
    static String[] extraFiles = new String[0];

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "--dry-run" -> dryRun = true;
            case "--seconds" -> seconds = Integer.parseInt(args[++i]);
            case "--max-samples" -> maxSamplesPerDriver = Integer.parseInt(args[++i]);
            case "--only" -> only = Set.of(args[++i].split(","));
            case "--files" -> extraFiles = args[++i].split(java.io.File.pathSeparator);
            default -> { System.err.println("unknown arg: " + args[i]); return; }
            }
        }

        applyLocalProperties();

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);
        setting.getOther().setWavSwitch(true);

        List<Path> samples = listSampleFiles();
        for (String f : extraFiles) {
            Path p = Path.of(f);
            if (Files.exists(p) && !Files.isDirectory(p) && !samples.contains(p)) samples.add(p);
        }
        System.out.printf("found %d sample file(s) (local.properties + %d explicit)%n", samples.size(), extraFiles.length);

        // group samples by driver token (VGM, NSF, ...) resolved from getPresetMixerBalance()
        Map<String, List<Path>> byDriver = new TreeMap<>();
        for (Path s : samples) {
            String driver = driverToken(s);
            if (driver == null) continue; // format has no driver preset
            if (!only.isEmpty() && !only.contains(driver)) continue;
            byDriver.computeIfAbsent(driver, k -> new ArrayList<>()).add(s);
        }

        // phase 1: measure every driver (per-chip balance + full-mix RMS), write nothing yet
        List<DriverResult> results = new ArrayList<>();
        for (var e : byDriver.entrySet()) {
            try {
                DriverResult r = measureDriver(e.getKey(), e.getValue());
                if (r != null) results.add(r);
            } catch (Exception ex) {
                System.out.printf("!! driver %s failed: %s%n", e.getKey(), ex);
                ex.printStackTrace();
            }
        }

        // phase 2: global downward leveling. target = quietest driver's full mix, so every
        // MasterVolume attenuates (<= 0) and nothing clamps at the +20 boost ceiling.
        double target = results.stream().mapToDouble(DriverResult::mixRms)
                .filter(v -> v >= MIN_MEAS_FLOOR).min().orElse(TARGET_MIX_RMS);
        if (!only.isEmpty())
            System.out.println("\n!! --only in effect: leveling within this subset only (not globally consistent)");
        System.out.printf("%n==== global leveling target (quietest driver full-mix rms) = %.1f ====%n", target);
        for (DriverResult r : results) {
            int master = r.mixRms() > 0
                    ? clampVol((int) Math.round(40.0 * Math.log10(target / r.mixRms())))
                    : 0;
            r.balance().setMasterVolume(master);
            System.out.printf("  %-8s mixRms=%9.1f  MasterVolume=%d%s%n",
                    r.driver(), r.mixRms(), master, master >= 20 ? "   <-- CLAMPED (below floor?)" : "");
            writeXml(r.driver(), r.balance(), r.xml());
        }

        reportChipCoverage();
    }

    /** one driver's measured per-chip balance plus its full-mix RMS, before master leveling */
    record DriverResult(String driver, Setting.Balance balance, double mixRms, Path xml) {}

    /** print which of the persistable chips were / were not exercised by any sample */
    static void reportChipCoverage() {
        var known = Setting.Balance.knownChipClasses();
        List<String> hit = new ArrayList<>();
        List<String> miss = new ArrayList<>();
        for (var c : known) (covered.contains(c) ? hit : miss).add(c.getSimpleName());
        System.out.printf("%n==== chip coverage: %d/%d measured ====%n", hit.size(), known.size());
        System.out.println("  measured : " + String.join(", ", hit));
        System.out.println("  NOT measured (no sample exercised them): " + String.join(", ", miss));
    }

    /** measure every chip of every sample of one driver and build its per-chip balance;
     *  the MasterVolume is left unset here and assigned globally in phase 2 (downward leveling) */
    static DriverResult measureDriver(String driver, List<Path> samples) throws Exception {
        if (samples.size() > maxSamplesPerDriver) samples = samples.subList(0, maxSamplesPerDriver);
        System.out.printf("%n==== driver %s (%d sample(s)) ====%n", driver, samples.size());

        // chipClass -> its mixer tags (union across samples); insertion-ordered for stable output
        Map<Class<? extends mdplayer.Chip>, Set<String>> chipTags = new LinkedHashMap<>();
        // chipClass -> accumulated isolated active-RMS values (one per sample it sounded in)
        Map<Class<? extends mdplayer.Chip>, List<Double>> chipRms = new LinkedHashMap<>();
        List<Path> measured = new ArrayList<>(); // samples with per-chip measurements
        List<Path> playable = new ArrayList<>(); // any sample that loaded & rendered (incl. single-chip drivers)

        for (Path sample : samples) {
            try {
                Map<Class<? extends mdplayer.Chip>, Set<String>> tags = enumerateChips(sample);
                if (tags.isEmpty()) {
                    // driver renders without exposing chips through the mixer map (e.g. SID) -> mix-only
                    System.out.printf("  %-24s no per-chip mixer tags, mix-only%n", sample.getFileName());
                    playable.add(sample);
                    continue;
                }
                for (var t : tags.entrySet()) {
                    chipTags.computeIfAbsent(t.getKey(), k -> new LinkedHashSet<>()).addAll(t.getValue());
                }
                for (Class<? extends mdplayer.Chip> chip : tags.keySet()) {
                    double rms = measureIsolated(sample, tags, chip).active();
                    System.out.printf("  %-24s %-12s active=%8.1f%n",
                            sample.getFileName(), chip.getSimpleName(), rms);
                    if (rms >= SILENCE_RMS) {
                        chipRms.computeIfAbsent(chip, k -> new ArrayList<>()).add(rms);
                        covered.add(chip);
                    }
                }
                measured.add(sample);
                playable.add(sample);
            } catch (Exception ex) {
                System.out.printf("  %-24s measurement failed (%s), skipped%n",
                        sample.getFileName(), ex.getClass().getSimpleName());
            }
        }

        if (playable.isEmpty()) {
            System.out.printf("  nothing measured for %s%n", driver);
            return null;
        }

        // average (linear) each chip's active RMS across samples
        Map<Class<? extends mdplayer.Chip>, Double> avgRms = new LinkedHashMap<>();
        for (var e : chipRms.entrySet()) {
            avgRms.put(e.getKey(), e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0));
        }

        // reference = MEDIAN chip loudness: loud outlier chips are attenuated (wide -192..0 range),
        // quiet chips are gently boosted (capped +20 ~= +10dB). Median (vs min) keeps the driver's mix
        // at its natural level instead of crushing every chip down to the faintest one -- important for
        // wide-spread drivers like VGM whose chips are sampled from different songs. Cross-driver
        // loudness is handled separately by the global downward MasterVolume leveling (phase 2).
        double tRef = median(avgRms.values());

        Setting.Balance balance = new Setting.Balance();
        System.out.printf("  --- %s balance (reference chip active rms=%.1f) ---%n", driver, tRef);
        for (var chip : chipTags.keySet()) {
            Double rms = avgRms.get(chip);
            int gain = (rms == null || rms <= 0 || tRef <= 0)
                    ? 0 // unmeasured / silent -> neutral
                    : clampVol((int) Math.round(40.0 * Math.log10(tRef / rms)));
            for (String tag : chipTags.get(chip)) {
                balance.setVolume(tag, chip, gain);
            }
            System.out.printf("      %-12s gain=%4d  tags=%s%n", chip.getSimpleName(), gain, chipTags.get(chip));
        }

        // measure the real full mix (all chips at computed gains); master leveling is global (phase 2)
        List<Double> mixes = new ArrayList<>();
        for (Path sample : playable) {
            try {
                mixes.add(measureMix(sample, balance).full());
            } catch (Exception ignore) {}
        }
        double mixRms = mixes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        System.out.printf("      full-mix rms=%.1f (master assigned in global phase)%n", mixRms);

        Path xml = RESOURCES.resolve("DefaultVolumeBalance_" + driver + ".xml");
        return new DriverResult(driver, balance, mixRms, xml);
    }

    /** honor --dry-run / missing-file and otherwise save + pretty-print the preset */
    static void writeXml(String driver, Setting.Balance balance, Path xml) throws Exception {
        if (dryRun) {
            System.out.printf("  [dry-run] would write %s%n", xml);
        } else if (!Files.exists(xml)) {
            System.out.printf("  !! no preset file to update: %s (skipped write)%n", xml);
        } else {
            balance.save(xml);
            prettyPrintInPlace(xml);
            System.out.printf("  wrote %s%n", xml);
        }
    }

    // ----- rendering / measurement -----

    /** build + prepare a fresh plugin for the sample (so each render starts from the top) */
    static BasePlugin<? extends BaseDriver> build(Path sample) throws Exception {
        // fresh, all-zero balance so no leftover mute from a previous measurement leaks in
        Setting.getInstance().setBalance(new Setting.Balance());
        FileFormat format = FileFormat.getFileFormat(sample.toString());
        // Archives.getInputStream transparently decompresses .vgz/.zip/.lzh (VGM/ZGM need it), and for
        // a plain file returns the BufferedInputStream(FileInputStream) so SoundUtil.getSource can still
        // resolve the source path via its java.io branch (the sun.nio.ch branch fails under --add-opens
        // and would leave BaseFileFormat.filename null, breaking MDX/MDR external-file lookup).
        try (InputStream in = vavi.util.archive.Archives.getInputStream(
                new BufferedInputStream(new java.io.FileInputStream(sample.toFile())))) {
            format.load(in, sample.toString());
        }
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", sample.toString(), "midiMode", 0, "songNo", 0));
        plugin.prepare();
        return plugin;
    }

    /** discover the chips a sample uses and each chip's mixer tags */
    static Map<Class<? extends mdplayer.Chip>, Set<String>> enumerateChips(Path sample) throws Exception {
        BasePlugin<? extends BaseDriver> plugin = build(sample);
        try {
            Map<Class<? extends mdplayer.Chip>, Set<String>> out = new LinkedHashMap<>();
            for (var e : plugin.getChipInstances().entrySet()) {
                Set<String> tags = new LinkedHashSet<>();
                for (MDSound.Chip c : e.getValue()) {
                    tags.addAll(c.setVolumes.keySet());
                }
                if (tags.isEmpty()) tags.add(MDSound.Chip.MAIN_TAG);
                out.put(e.getKey(), tags);
            }
            return out;
        } finally {
            close(plugin);
        }
    }

    /** {@code full} = RMS over the whole window; {@code active} = RMS over only non-silent samples */
    record Meas(double full, double active) {}

    /** render the sample with only {@code target} audible (all other chips muted) and measure it */
    static Meas measureIsolated(Path sample,
                                Map<Class<? extends mdplayer.Chip>, Set<String>> allChips,
                                Class<? extends mdplayer.Chip> target) throws Exception {
        BasePlugin<? extends BaseDriver> plugin = build(sample);
        try {
            for (var e : allChips.entrySet()) {
                int v = e.getKey().equals(target) ? 0 : -192;
                for (String tag : e.getValue()) {
                    plugin.setVolume(tag, e.getKey(), true, v);
                }
            }
            return renderMeas(plugin);
        } finally {
            close(plugin);
        }
    }

    /** render the full mix with the balance's per-chip gains applied and measure it */
    static Meas measureMix(Path sample, Setting.Balance balance) throws Exception {
        BasePlugin<? extends BaseDriver> plugin = build(sample);
        try {
            for (var e : plugin.getChipInstances().entrySet()) {
                Set<String> tags = new LinkedHashSet<>();
                for (MDSound.Chip c : e.getValue()) tags.addAll(c.setVolumes.keySet());
                if (tags.isEmpty()) tags.add(MDSound.Chip.MAIN_TAG);
                for (String tag : tags) {
                    plugin.setVolume(tag, e.getKey(), true, balance.getVolume(tag, e.getKey()));
                }
            }
            return renderMeas(plugin);
        } finally {
            close(plugin);
        }
    }

    /** pull raw mixer output (pre master volume) and compute full + active RMS over both channels */
    static Meas renderMeas(BasePlugin<? extends BaseDriver> plugin) {
        int rate = Setting.getInstance().getOutputDevice().getSampleRate();
        long wanted = (long) rate * seconds * 2; // stereo shorts
        short[] buffer = new short[8192];
        double sumSq = 0, activeSumSq = 0;
        long n = 0, activeN = 0;
        while (n < wanted) {
            int ret = plugin.getDriver().render(buffer, 0, buffer.length);
            if (ret <= 0) break;
            for (short s : buffer) {
                double sq = (double) s * s;
                sumSq += sq;
                n++;
                if (Math.abs(s) >= NOISE_FLOOR) {
                    activeSumSq += sq;
                    activeN++;
                }
            }
            if (plugin.driverVirtual != null && plugin.driverVirtual.stopped) break;
        }
        double full = n == 0 ? 0 : Math.sqrt(sumSq / n);
        double active = activeN == 0 ? 0 : Math.sqrt(activeSumSq / activeN);
        return new Meas(full, active);
    }

    static void close(BasePlugin<? extends BaseDriver> plugin) {
        try { plugin.stop(); } catch (Exception ignore) {}
        try { plugin.close(); } catch (Exception ignore) {}
        try { plugin.chipRegister.close(); } catch (Exception ignore) {}
    }

    // ----- driver resolution -----

    /** {@code DriverBalance_VGM.mbc} -> {@code "VGM"}; null when the format has no driver preset */
    static String driverToken(Path sample) {
        try {
            // getPresetMixerBalance() returns constants, so classification needs no full load
            FileFormat format = FileFormat.getFileFormat(sample.toString());
            String[] fns = format.getPresetMixerBalance();
            if (fns == null || fns.length == 0) return null;
            String mbc = fns[0]; // e.g. DriverBalance_VGM.mbc
            String t = mbc.replaceFirst("^DriverBalance_", "").replaceFirst("\\.mbc$", "");
            return t.isEmpty() ? null : t;
        } catch (Exception e) {
            return null;
        }
    }

    // ----- helpers -----

    static int clampVol(int v) {
        return Math.max(-192, Math.min(20, v));
    }

    /** median of positive values (ignores zeros/unmeasured); 0 when none */
    static double median(java.util.Collection<Double> values) {
        double[] a = values.stream().mapToDouble(Double::doubleValue).filter(v -> v > 0).sorted().toArray();
        if (a.length == 0) return 0;
        int m = a.length / 2;
        return a.length % 2 == 1 ? a[m] : (a[m - 1] + a[m]) / 2.0;
    }

    /** re-indent the single-line XML that {@code Balance.save} emits, for readable diffs */
    static void prettyPrintInPlace(Path xml) throws Exception {
        byte[] raw = Files.readAllBytes(xml);
        var dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        var doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(raw));
        doc.normalizeDocument();
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tr.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        var out = new java.io.ByteArrayOutputStream();
        tr.transform(new DOMSource(doc), new StreamResult(out));
        Files.write(xml, out.toByteArray());
    }

    // ----- local.properties -----

    /** set the driver data-dir / chip-variant system properties some plugins need to load */
    static void applyLocalProperties() throws Exception {
        Path lp = Path.of("local.properties");
        if (!Files.exists(lp)) {
            System.err.println("local.properties not found in " + Path.of(".").toAbsolutePath());
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(lp)) {
            p.load(in);
        }
        for (String key : new String[] {
                "mdplayer.fmp.dir", "mdplayer.fmp.pvi", "mdplayer.zms.dir", "mdplayer.mgs.dir",
                "mdplayer.ndp.dir", "mdplayer.musica.dir", "muap.dir.dta", "muap.dir.pcm",
                "mdplayer.variant.pcm8", "mdplayer.variant.mpcm", "mdplayer.variant.ym2151",
                "mdplayer.variant.ym2413", "mdplayer.variant.ymf262", "mdplayer.variant.ay8910",
                "fmdsp.fontRom" }) {
            String v = p.getProperty(key);
            if (v != null && !v.isEmpty()) System.setProperty(key, v);
        }
    }

    /** every existing, non-directory file referenced in local.properties (commented lines included) */
    static List<Path> listSampleFiles() throws Exception {
        List<Path> paths = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of("local.properties"))) {
            if (line.matches("^#?\\w+\\s*?=.*$")) {
                String file = unescapeUnicode(line.substring(line.indexOf("=") + 1).trim());
                if (file.isEmpty()) continue;
                Path path = Path.of(file);
                if (Files.exists(path) && !Files.isDirectory(path) && !paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    /** decode {@code \\uXXXX} escapes (local.properties stores non-ASCII paths .properties-escaped) */
    static String unescapeUnicode(String s) {
        if (!s.contains("\\u")) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            if (i + 5 < s.length() && s.charAt(i) == '\\' && s.charAt(i + 1) == 'u') {
                try {
                    sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                    i += 6;
                    continue;
                } catch (NumberFormatException ignore) {}
            }
            sb.append(s.charAt(i++));
        }
        return sb.toString();
    }
}
