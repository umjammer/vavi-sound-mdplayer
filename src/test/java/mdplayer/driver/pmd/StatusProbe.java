/*
 * scratch: dump the fmdsp MML columns (GT / DT / M) and the keyboard keys over time
 */

package mdplayer.driver.pmd;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.fmdsp.Notes;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import pmd.driver.PW;
import vavi.sound.visualizer.fmdsp.FmDspVisualizer;
import vavi.sound.visualizer.fmdsp.TrackId;
import vavi.sound.visualizer.fmdsp.TrackStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


public class StatusProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/PMD/SC3/POP_TM.MZ");
        int seconds = Integer.getInteger("probe.seconds", 30);
        boolean verbose = Boolean.getBoolean("probe.verbose");
        String out = System.getProperty("probe.out");

        for (Path p : list(file)) {
            try {
                one(p, seconds, verbose, out);
            } catch (Exception e) {
                System.err.printf("%-40s FAILED %s%n", p.getFileName(), e);
            }
        }
    }

    static List<Path> list(String file) throws Exception {
        Path p = Path.of(file);
        if (!Files.isDirectory(p)) return List.of(p);
        try (var s = Files.walk(p)) {
            return s.filter(x -> {
                String n = x.getFileName().toString().toUpperCase();
                return n.endsWith(".M") || n.endsWith(".M2") || n.endsWith(".MZ");
            }).toList();
        }
    }

    static void one(Path file, int seconds, boolean verbose, String out) throws Exception {
        FileFormat format = FileFormat.getFileFormat(file.toString());
        format.load(Files.newInputStream(file), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file.toString()));
        plugin.prepare();

        PmdFmDspSource source = new PmdFmDspSource();
        source.setFilename(file.getFileName().toString());

        BaseDriver driver = plugin.getDriver();
        driver.addViewListener(source::update);
        // the reference has to be taken at event time, together with the source's own snapshot -
        // an LFO moves every tick and the work area is live
        int[] refs = new int[TrackId.COUNT];
        driver.addViewListener(e -> {
            if (!e.getName().equals("pmd")) return;
            PW pw = (PW) e.getArguments()[0];
            for (TrackId t : TrackId.values()) refs[t.ordinal()] = reference(t, pw);
        });

        TrackStatus status = new TrackStatus();
        short[] buffer = new short[2 * 1024];
        int bends = 0, portas = 0, maxBend = 0, mismatch = 0, checked = 0;
        for (int i = 0; i < Common.VGMProcSampleRate * seconds / 1024; i++) {
            driver.render(buffer, 0, 1024);
            if (i % 4 != 0) continue;
            StringBuilder b = new StringBuilder();
            boolean bent = false;
            for (TrackId t : TrackId.values()) {
                source.readStatus(t, status);
                if (!status.playing) continue;
                if (status.status.length() > 7 && status.status.charAt(7) == 'P') portas++;
                if (status.actualKey != status.key && status.key != 0xff && status.actualKey != 0xff) {
                    bent = true;
                    int d = Math.abs(note(status.actualKey) - note(status.key));
                    maxBend = Math.max(maxBend, d);
                }
                // the C fmdsp's own answer: the key of the block/f-number PMD is about to write
                int ref = (status.key & 0xf) == 0xf ? -1 : refs[t.ordinal()];
                if (ref >= 0) {
                    checked++;
                    if (ref != status.actualKey) {
                        mismatch++;
                        if (verbose && mismatch < 20) {
                            System.err.printf("  MISMATCH %-9s k=%02x a=%02x ref=%02x%n",
                                    t, status.key, status.actualKey, ref);
                        }
                    }
                }
                b.append("  %-9s k=%02x a=%02x gt=%3d dt=%4d m=%s%n".formatted(
                        t, status.key, status.actualKey, status.gate, status.detune, status.status));
            }
            if (bent) bends++;
            if (verbose && bent && bends < 20) {
                System.err.printf("--- %.1f s (bent)%n%s", i * 1024.0 / Common.VGMProcSampleRate, b);
            }
            if (bent && out != null && !Files.exists(Path.of(out))) {
                shot(source, out);
            }
        }
        System.err.printf("%-40s bent=%-6d maxBend=%-3d porta=%-6d checked=%-7d mismatch=%d%n",
                file.getFileName(), bends, maxBend, portas, checked, mismatch);
    }

    /** one frame of the visualizer, to look at the keyboard and the MML columns as drawn */
    static void shot(PmdFmDspSource source, String out) throws Exception {
        FmDspVisualizer visualizer = new FmDspVisualizer(60);
        visualizer.setDataSource(source);
        visualizer.setSize(visualizer.getPreferredSize());
        visualizer.doLayout();
        BufferedImage image = new BufferedImage(
                visualizer.getWidth(), visualizer.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 120; i++) visualizer.paint(image.getGraphics()); // the palette fades in
        ImageIO.write(image, "png", Path.of(out).toFile());
        System.err.printf("wrote %s%n", out);
    }

    /** {@code fmdriver_fm_freq2key(part->output_freq)} of the C fmdsp, for the FM rows; -1 if n/a */
    static int reference(TrackId t, PW pw) {
        if (t.ordinal() > TrackId.FM_6.ordinal() || pw.partWk == null) return -1;
        int p = switch (t) {
            case FM_1 -> pw.part1;
            case FM_2 -> pw.part2;
            case FM_3 -> pw.part3;
            case FM_4 -> pw.board2 != 0 ? pw.part4 : -1;
            case FM_5 -> pw.board2 != 0 ? pw.part5 : -1;
            case FM_6 -> pw.board2 != 0 ? pw.part6 : -1;
            default -> -1;
        };
        if (p < 0 || p >= pw.partWk.length) return -1;
        PW.partWork part = pw.partWk[p];
        int base = part.fnum & 0xffff;
        if (base == 0 || part.slotmask == 0) return -1;
        int lfo = 0;
        if ((part.lfoswi & 0x01) != 0) lfo += part.lfodat;
        if ((part.lfoswi & 0x10) != 0) lfo += part._lfodat;
        return Notes.fmKeyOf(normalize(base & 0x3800, (base & 0x7ff) + part.porta_num + part.detune + lfo));
    }

    /** {@code pmd_blkfnum_normalize} of the C fmdsp */
    static int normalize(int blk, int fnum) {
        fnum &= 0xffff;
        for (;;) {
            if ((fnum & 0x8000) != 0 || fnum < 0x26a) {
                if (blk - 0x800 < 0) {
                    blk = 0;
                    if ((fnum & 0x8000) != 0 || fnum < 8) fnum = 8;
                    break;
                }
                blk -= 0x800;
                fnum = (fnum + 0x26a) & 0xffff;
                continue;
            }
            if (fnum < 0x4d4) break;
            blk += 0x800;
            if (blk != 0x4000) {
                fnum = (fnum - 0x26a) & 0xffff;
                continue;
            }
            blk = 0x3800;
            if (fnum > 0x7ff) fnum = 0x7ff;
            break;
        }
        return blk | fnum;
    }

    static int note(int key) {
        return (key >> 4) * 12 + (key & 0xf);
    }
}
