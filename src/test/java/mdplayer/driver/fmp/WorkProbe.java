/*
 * scratch: reverse engineer the FMP work area layout
 */

package mdplayer.driver.fmp;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import mdplayer.emu.nise98.FileTemp;
import mdplayer.emu.nise98.Memory98;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Runs FMP headless and correlates memory changes with the OPNA writes the driver makes,
 * to recover the layout of FMP's part work.
 */
public class WorkProbe {

    static final int SEGMENT = 0x2000 << 4;
    static final int SIZE = 0x10000;

    /** FM1 key offset, found in the previous pass */
    static final int FM1KEY = 0x013e;
    static final int STRIDE = 0x6f;

    final int[][] fnum = new int[6][2];
    final int[][] tl = new int[6][4];
    final int[] alg = new int[6];
    /** writes to the tone (patch) registers of a channel in this tick */
    final int[] patchWrites = new int[6];
    final boolean[] pendingKeyOn = new boolean[6];
    final boolean[] pendingTone = new boolean[6];

    /** ssg tone period and volume */
    final int[][] ssgPeriod = new int[3][2];
    final int[] ssgVolume = new int[3];

    int tick;

    record Snap(int tick, int ch, int pitch, int tlv, int volume, boolean tone, byte[] mem) {}

    final List<Snap> snaps = new ArrayList<>();
    final List<Snap> ssgSnaps = new ArrayList<>();
    final boolean[] pendingSsg = new boolean[3];
    final int[] lastSsgPitch = new int[3];
    boolean pendingAdpcm;
    final int[] pan = new int[6];
    int ssgMixer;

    /** ticks at which each byte of the segment changed */
    final List<List<Integer>> changes = new ArrayList<>();
    final byte[] prev = new byte[SIZE];
    /** note change ticks per ssg channel, and adpcm key on ticks */
    final List<List<Integer>> ssgNoteTicks = new ArrayList<>();
    final List<Integer> adpcmTicks = new ArrayList<>();

    final java.util.Map<String, Integer> regHist = new java.util.TreeMap<>();

    void write(int p, int a, int d) {
        int port = (p & 0xff) == 0x8a ? 0 : 1;
        regHist.merge("p%d:%02x".formatted(port, a), 1, Integer::sum);
        if (port == 0 && a == 0x28) {
            int c = d & 0x07;
            int ch = c < 3 ? c : (c >= 4 && c <= 6 ? c - 1 : -1);
            if (ch >= 0 && (d & 0xf0) != 0) pendingKeyOn[ch] = true;
        } else if (a >= 0xa0 && a <= 0xa2) {
            fnum[(a - 0xa0) + port * 3][0] = d;
        } else if (a >= 0xa4 && a <= 0xa6) {
            fnum[(a - 0xa4) + port * 3][1] = d;
        } else if (a >= 0x40 && a <= 0x4e && (a & 3) != 3) {
            tl[(a & 3) + port * 3][(a - 0x40) >> 2] = d & 0x7f;
        } else if (a >= 0xb0 && a <= 0xb2) {
            alg[(a - 0xb0) + port * 3] = d & 7;
        } else if (a >= 0xb4 && a <= 0xb6) {
            pan[(a - 0xb4) + port * 3] = d & 0xff;
        }
        if (port == 0 && a == 0x07) ssgMixer = d & 0xff;
        // a whole voice gets written to the slot registers when the tone changes
        if (a >= 0x30 && a <= 0x8e && (a & 3) != 3) {
            int ch = (a & 3) + port * 3;
            if (++patchWrites[ch] >= 8) pendingTone[ch] = true;
        }
        if (port == 1 && a == 0x00 && (d & 0x80) != 0) pendingAdpcm = true;
        if (port == 0) {
            if (a <= 0x05) {
                int ch = a >> 1;
                ssgPeriod[ch][a & 1] = d;
                pendingSsg[ch] = true;
            } else if (a >= 0x08 && a <= 0x0a) {
                ssgVolume[a - 0x08] = d & 0x1f;
            }
        }
    }

    static int pitch(int[] f) {
        int fn = ((f[1] & 7) << 8) | (f[0] & 0xff);
        int block = (f[1] >> 3) & 7;
        return block * 12 + (int) Math.round(12 * Math.log(Math.max(fn, 1) / 617.0) / Math.log(2));
    }

    int carrier(int ch) {
        int mask = switch (alg[ch]) {
            case 0, 1, 2, 3 -> 0b1000;
            case 4 -> 0b1100;
            case 5, 6 -> 0b1110;
            default -> 0b1111;
        };
        int min = 127;
        for (int i = 0; i < 4; i++) if ((mask & (1 << i)) != 0) min = Math.min(min, tl[ch][i]);
        return min;
    }

    @Test
    @Disabled("for ai iteration")
    void probe() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/fmp_ume3/ALPHA.OVI");
        String dir = System.getProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");

        FMP fmp = new FMP();
        fmp.ft = new FileTemp();
        fmp.setSearchPath(dir + ";" + Path.of(file).getParent() + ";/Users/nsano/Public/np2/PVI");
        fmp.sampleRate = 44100;
        fmp.charset = Charset.forName("Shift_JIS");
        fmp.dir = dir;
        fmp.blockWrite = b -> {};
        fmp.setPPZ8PCMData = (a, b, c) -> {};
        fmp.setPPZ8Data = (a, b, c) -> {};
        fmp.opnaWrite = this::write;
        fmp.playingFileName = file;
        fmp.run(Files.readAllBytes(Path.of(file)));

        Memory98 mem = fmp.nise98.getMem();
        for (int i = 0; i < SIZE; i++) changes.add(new ArrayList<>());
        for (int i = 0; i < 3; i++) ssgNoteTicks.add(new ArrayList<>());

        for (int i = 0; i < 44100 * 40; i++) {
            fmp.nise98.runTimer();
            if (!fmp.nise98.intTimer()) continue;
            java.util.Arrays.fill(patchWrites, 0);
            fmp.processOneFrame(() -> {});
            tick++;

            for (int o = 0; o < SIZE; o++) {
                byte v = mem.peekB(SEGMENT + o);
                if (v != prev[o]) {
                    changes.get(o).add(tick);
                    prev[o] = v;
                }
            }
            if (pendingAdpcm) {
                pendingAdpcm = false;
                adpcmTicks.add(tick);
            }

            byte[] snap = null;
            for (int ch = 0; ch < 6; ch++) {
                if (!pendingKeyOn[ch]) continue;
                pendingKeyOn[ch] = false;
                if (snap == null) snap = snapshot(mem);
                snaps.add(new Snap(tick, ch, pitch(fnum[ch]), carrier(ch), 0, pendingTone[ch], snap));
                pendingTone[ch] = false;
            }
            for (int ch = 0; ch < 3; ch++) {
                if (!pendingSsg[ch]) continue;
                pendingSsg[ch] = false;
                int period = (ssgPeriod[ch][1] & 0xf) << 8 | (ssgPeriod[ch][0] & 0xff);
                int p = -(int) Math.round(12 * Math.log(Math.max(period, 1)) / Math.log(2));
                if (p == lastSsgPitch[ch]) continue; // vibrato, not a new note
                lastSsgPitch[ch] = p;
                ssgNoteTicks.get(ch).add(tick);
                if (snap == null) snap = snapshot(mem);
                ssgSnaps.add(new Snap(tick, ch, p, 0, ssgVolume[ch], false, snap));
            }
        }

        System.err.printf("== ticks=%d fm snaps=%d ssg snaps=%d work=%04x%n",
                tick, snaps.size(), ssgSnaps.size(), fmp.workPtr - SEGMENT);
        System.err.println("-- register writes --");
        System.err.println("  " + regHist);

        // 1. volume: a byte that tracks the carrier total level within a channel over time
        System.err.println("-- volume: best correlating offsets per channel --");
        for (int ch = 0; ch < 6; ch++) {
            int c0 = ch;
            List<Snap> ss = snaps.stream().filter(s -> s.ch == c0).toList();
            long distinct = ss.stream().map(Snap::tlv).distinct().count();
            System.err.printf("  ch%d: %d notes, %d distinct carrier TL %s%n", ch, ss.size(), distinct,
                    ss.stream().map(Snap::tlv).distinct().limit(8).toList());
            if (ss.size() < 8 || distinct < 4) continue;
            record Hit(int r, double c) {}
            List<Hit> hits = new ArrayList<>();
            for (int r = -0x60; r < 0x60; r++) {
                double[] x = new double[ss.size()], y = new double[ss.size()];
                for (int i = 0; i < ss.size(); i++) {
                    x[i] = ss.get(i).mem[FM1KEY + STRIDE * c0 + r] & 0xff;
                    y[i] = ss.get(i).tlv;
                }
                double c = pearson(rank(x), rank(y));
                if (Math.abs(c) > 0.7) hits.add(new Hit(r, c));
            }
            hits.sort((a, b) -> Double.compare(Math.abs(b.c()), Math.abs(a.c())));
            System.err.printf("  ch%d (%d notes, %d levels):%n", ch, ss.size(), distinct);
            for (Hit h : hits.subList(0, Math.min(6, hits.size()))) {
                System.err.printf("     key%+d corr=%+.3f%n", h.r(), h.c());
            }
        }

        // 2. tone number: a byte that changes exactly when the driver rewrites the voice registers
        System.err.println("-- fields changing when the voice is rewritten (tone number) --");
        long tones = snaps.stream().filter(Snap::tone).count();
        System.err.printf("   (voice rewrites seen: %d)%n", tones);
        for (int r = -0x30; r < 0x40; r++) {
            int hit = 0, tot = 0, miss = 0;
            int[] last = new int[6];
            java.util.Arrays.fill(last, -1);
            for (Snap s : snaps) {
                int v = s.mem[FM1KEY + STRIDE * s.ch + r] & 0xff;
                boolean changed = last[s.ch] >= 0 && v != last[s.ch];
                last[s.ch] = v;
                if (s.tone()) { tot++; if (changed) hit++; }
                else if (changed) miss++;
            }
            if (tot > 2 && hit >= tot * 0.9 && miss <= tot) System.err.printf("  key%+d rewrites=%d hit=%d spurious=%d%n", r, tot, hit, miss);
        }

        // 3. map the whole part table: each part carries a unique bit, its chip port and channel
        System.err.println("-- part table --");
        System.err.println("  part  addr   bit    port ch  note vol gate left  dataptr");
        Snap last = snaps.getLast();
        for (int part = 0; part < 32; part++) {
            int b = FM1KEY + STRIDE * part;
            if (b + 0x48 >= SIZE) break;
            int bit = (last.mem[b + 68] & 0xff) << 8 | (last.mem[b + 67] & 0xff);
            int ptr = (last.mem[b + 62] & 0xff) << 8 | (last.mem[b + 61] & 0xff);
            System.err.printf("  %4d  %04x  %04x  %02x   %d   %02x   %02x  %02x   %02x    %04x%n",
                    part, b, bit, last.mem[b + 59] & 0xff, last.mem[b + 71] & 0xff,
                    last.mem[b] & 0xff, last.mem[b - 7] & 0xff, last.mem[b - 8] & 0xff,
                    last.mem[b - 5] & 0xff, ptr);
        }

        // 4. SSG / ADPCM parts: bytes that change exactly when that channel gets a new note
        System.err.println("-- SSG / ADPCM part fields (change timing) --");
        for (int ch = 0; ch < 4; ch++) {
            List<Integer> ks = ch < 3 ? ssgNoteTicks.get(ch) : adpcmTicks;
            String name = ch < 3 ? "ssg" + ch : "adpcm";
            if (ks.size() < 10) { System.err.printf("  %s: only %d events%n", name, ks.size()); continue; }
            List<String> hits = new ArrayList<>();
            for (int o = 0; o < SIZE; o++) {
                List<Integer> cs = changes.get(o);
                if (cs.isEmpty()) continue;
                int hit = 0;
                for (int k : ks) {
                    for (int c : cs) {
                        if (Math.abs(c - k) <= 1) { hit++; break; }
                    }
                }
                double coverage = (double) hit / ks.size();
                double precision = (double) hit / cs.size();
                if (coverage > (ch < 3 ? 0.9 : 0.8) && precision > (ch < 3 ? 0.7 : 0.4)) {
                    hits.add("%04x[fm1key%+d]".formatted(o, o - FM1KEY));
                }
            }
            System.err.printf("  %s (%d notes): %s%n", name, ks.size(), String.join(" ", hits));
        }
    }

    static byte[] snapshot(Memory98 mem) {
        byte[] snap = new byte[SIZE];
        for (int o = 0; o < SIZE; o++) snap[o] = mem.peekB(SEGMENT + o);
        return snap;
    }

    static double[] toArray(List<Double> l) {
        double[] a = new double[l.size()];
        for (int i = 0; i < a.length; i++) a[i] = l.get(i);
        return a;
    }

    static double[] rank(double[] v) {
        int n = v.length;
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (v[j] < v[i]) r[i]++;
                else if (v[j] == v[i] && j < i) r[i] += 0.5;
            }
        }
        return r;
    }

    static double pearson(double[] x, double[] y) {
        int n = x.length;
        double mx = 0, my = 0;
        for (int i = 0; i < n; i++) { mx += x[i] / n; my += y[i] / n; }
        double sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < n; i++) {
            sxy += (x[i] - mx) * (y[i] - my);
            sxx += (x[i] - mx) * (x[i] - mx);
            syy += (y[i] - my) * (y[i] - my);
        }
        return sxx == 0 || syy == 0 ? 0 : sxy / Math.sqrt(sxx * syy);
    }
}
