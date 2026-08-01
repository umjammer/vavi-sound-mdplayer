package mdplayer.lib.mndrv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mdplayer.lib.mndrv.MnWork.Dw;
import mdplayer.lib.mndrv.MnWork.W;


/**
 * Runs MND data through the real {@link MnDrv} interpreter with no chips attached and watches
 * where each track's data pointer goes.
 * <p>
 * The point is to tell a correct MML parse from a desynced one without needing reference audio.
 * A track is laid out as a contiguous run of bytes ending in {@code $FF} + a loop offset word, so
 * a correct parse visits only offsets inside its own region and comes to rest on that {@code $FF}.
 * A parse that has lost step -- because an opcode consumed the wrong number of operand bytes --
 * walks off into the next track's data or past the end of the file, and that shows up here as an
 * out-of-region data pointer. This is what makes the version 1 opcode map falsifiable: guess an
 * opcode's arity wrong and some track will stray.
 * <p>
 * Chips are stubbed out, so this runs far faster than real time and cares only about the
 * sequencer.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class MndV1Analyzer {

    /** samples per second the driver is told it is running at */
    private static final int SAMPLE_RATE = 44100;

    /** where one track's data lives, and the offset of the {@code $FF} that terminates it */
    public record Region(int track, int channel, int start, int end) {

        boolean contains(int p) {
            return p >= start && p < end;
        }
    }

    /**
     * What the analyzer saw for one track.
     *
     * @param escaped  whether the data pointer ever left the sequence data
     * @param outside  first such pointer
     * @param left     first data pointer seen outside this track's own region, or {@link #NONE};
     *                 only a hint, since tracks are free to jump into shared data, but on a
     *                 desync it is where the parse first visibly went wrong
     * @param lo       lowest offset the data pointer reached
     * @param hi       highest offset the data pointer reached
     * @param visited  how many distinct offsets it stopped on
     */
    public record TrackResult(Region region, boolean escaped, int outside, int left,
                              int lo, int hi, int visited) {

        public boolean clean() {
            return !escaped && visited > 0;
        }

        @Override
        public String toString() {
            String where = "track %2d ch $%02x [%04x-%04x)".formatted(
                    region.track, region.channel, region.start, region.end);
            if (escaped) return where + " ESCAPED to %x (left its region at %x)"
                    .formatted(outside, left);
            if (visited == 0) return where + " never advanced";
            return where + " ok, ranged %04x-%04x over %d stops%s".formatted(lo, hi, visited,
                    left != NONE ? ", left its region at %04x".formatted(left) : "");
        }
    }

    /** no such offset was seen */
    public static final int NONE = Integer.MIN_VALUE;

    /**
     * What the analyzer saw for one file.
     *
     * @param loops  the driver's loop counter, i.e. how many times a track hit its end command
     */
    public record FileResult(Path path, int version, List<TrackResult> tracks, int loops,
                             String failure) {

        public boolean clean() {
            return failure == null && loops > 0 && tracks.stream().allMatch(TrackResult::clean);
        }

        public String summary() {
            return "%s (v%d) %s, %d loops".formatted(
                    path.getFileName(), version, clean() ? "clean" : "DIRTY", loops);
        }
    }

    /**
     * Splits the sequence data into per-track regions. Tracks are stored back to back in offset
     * order, so each one runs to the start of the next and the last one runs to the end of file.
     */
    public static List<Region> regionsOf(byte[] data) {
        int seq = readInt(data, 0x10);
        int count = readShort(data, seq);
        record Entry(int track, int channel, int start) {}
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int e = seq + 2 + i * 8;
            entries.add(new Entry(i, data[e + 4] & 0xff, seq + readInt(data, e)));
        }
        List<Entry> byOffset = new ArrayList<>(entries);
        byOffset.sort((a, b) -> Integer.compare(a.start, b.start));
        List<Region> regions = new ArrayList<>();
        for (int i = 0; i < byOffset.size(); i++) {
            Entry e = byOffset.get(i);
            int end = i + 1 < byOffset.size() ? byOffset.get(i + 1).start : data.length;
            regions.add(new Region(e.track, e.channel, e.start, end));
        }
        regions.sort((a, b) -> Integer.compare(a.track, b.track));
        return regions;
    }

    /**
     * Plays {@code path} headlessly for at most {@code frames} driver ticks, sampling every
     * track's data pointer as it goes.
     */
    public static FileResult analyze(Path path, int frames) throws IOException {
        byte[] data = Files.readAllBytes(path);
        int version = readShort(data, 4);
        List<Region> regions = regionsOf(data);

        MnDrv drv = new MnDrv();
        drv.ym2151Write = (a, d) -> {};
        drv.ym2608Write = (c, p, a, d) -> {};
        drv.stop = () -> {};
        drv.mpcm = new NullMPcm();
        try {
            drv.init(data, false, SAMPLE_RATE);
        } catch (RuntimeException e) {
            return new FileResult(path, version, List.of(), 0, e.getMessage());
        }

        // the driver keeps its copy of the song at MMLBUFADR; pointers we read back out of the
        // track work areas are into that copy, so rebase them onto the file to compare offsets
        int base = drv.mm.readInt(drv.reg.a6 + Dw.MMLBUFADR);
        int trackWork = drv.reg.a6 + Dw.TRACKWORKADR;
        int seq = readInt(data, 0x10);

        List<Set<Integer>> visited = new ArrayList<>();
        boolean[] escaped = new boolean[regions.size()];
        int[] outside = new int[regions.size()];
        int[] left = new int[regions.size()];
        int[] lo = new int[regions.size()];
        int[] hi = new int[regions.size()];
        for (int i = 0; i < regions.size(); i++) {
            visited.add(new LinkedHashSet<>());
            outside[i] = NONE;
            left[i] = NONE;
            lo[i] = Integer.MAX_VALUE;
        }

        String failure = null;
        for (int f = 0; f < frames && failure == null; f++) {
            try {
                drv.clock();
            } catch (RuntimeException e) {
                // a desynced parse feeds garbage operands to real handlers, so it surfaces as an
                // arithmetic or bounds fault long before it surfaces as bad audio
                failure = "%s at frame %d".formatted(e, f);
            }
            for (int i = 0; i < regions.size(); i++) {
                int p = drv.mm.readInt(trackWork + i * W._track_work_size + W.dataptr) - base;
                if (!visited.get(i).add(p)) continue;
                lo[i] = Math.min(lo[i], p);
                hi[i] = Math.max(hi[i], p);
                if (!regions.get(i).contains(p) && left[i] == NONE) left[i] = p;
                // tracks legitimately loop and may share data with each other, so the only
                // pointer that is certainly wrong is one that has left the sequence data
                if ((p < seq || p > data.length) && !escaped[i]) {
                    escaped[i] = true;
                    outside[i] = p;
                }
            }
        }

        int loops = drv.mm.readShort(drv.reg.a6 + Dw.LOOP_COUNTER) & 0xffff;
        List<TrackResult> tracks = new ArrayList<>();
        for (int i = 0; i < regions.size(); i++) {
            int n = visited.get(i).size();
            tracks.add(new TrackResult(regions.get(i), escaped[i], outside[i], left[i],
                    n == 0 ? 0 : lo[i], hi[i], n));
        }
        return new FileResult(path, version, tracks, loops, failure);
    }

    private static int readShort(byte[] b, int o) {
        return ((b[o] & 0xff) << 8) | (b[o + 1] & 0xff);
    }

    private static int readInt(byte[] b, int o) {
        return (readShort(b, o) << 16) | readShort(b, o + 2);
    }

    /** MPCM is irrelevant to sequencing; swallow everything */
    private static class NullMPcm implements mdplayer.lib.zms.Zms.MPcmInterface {
        @Override public void keyOn(int ch) {}
        @Override public void keyOff(int ch) {}
        @Override public void writePcm(int ch, Object pcm, Object mem, Object reg, int n) {}
        @Override public void setFreq(int ch, int value) {}
        @Override public void setPitch(int ch, int value) {}
        @Override public void setVol(int ch, int value) {}
        @Override public void setPan(int ch, int value) {}
        @Override public void reset() {}
        @Override public void setVolTable(int type) {}
        @Override public void setVolTable(int type, int[] vtbl) {}
    }

    /** {@code MndV1Analyzer <mnd file|directory>...} */
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            List<Path> paths = new ArrayList<>();
            Path p = Path.of(arg);
            if (Files.isDirectory(p)) {
                try (var s = Files.list(p)) {
                    s.filter(f -> f.toString().toLowerCase().endsWith(".mnd")).sorted()
                            .forEach(paths::add);
                }
            } else {
                paths.add(p);
            }
            boolean verbose = System.getProperty("verbose") != null;
            int frames = Integer.getInteger("frames", 2_000_000);
            int clean = 0;
            for (Path f : paths) {
                FileResult r = analyze(f, frames);
                if (r.clean()) clean++;
                if (r.clean() && !verbose) continue;
                System.out.println(r.summary());
                if (r.failure() != null) {
                    System.out.printf("    %s%n", r.failure());
                }
                r.tracks().stream().filter(t -> verbose || !t.clean())
                        .forEach(t -> System.out.printf("    %s%n", t));
            }
            System.out.printf("%s: %d/%d files clean%n", arg, clean, paths.size());
        }
    }
}
