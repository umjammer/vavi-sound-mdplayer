/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.mxdrv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import mdplayer.Common;
import vavi.util.ByteUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * LZX compressed MDX.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-26 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class LzxTest {

    /** a tree of MDX to sweep, when there is one to point this at */
    @Property(name = "mdx.dir")
    String mdxDir;

    @BeforeEach
    void setup() throws Exception {
        if (Files.exists(Paths.get("local.properties"))) {
            PropsEntity.Util.bind(this);
        }
    }

    /**
     * A song packed the way {@code LZX.X} packs one, small enough to spell out, holding one of
     * every token the stub knows: three literals, a short match that runs off its own output, a
     * long match with its length in the low three bits of the word, a long match with the length
     * byte that follows a word whose low three bits are zero, and the end of stream. It stops five
     * bytes short of the size it declares, which the expansion has to zero fill.
     */
    private static final byte[] PACKED = HexFormat.of().parseHex(
            // "lzx test" 0x0d 0x0a 0x1a and an empty PDX file name
            "6c7a7820746573740d0a1a00" +
            // the stub: two branches and the signature, then the expanded size at +0x12
            "602660324c5a5820302e3332000000000000" +
            "00000040" +
            "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            // lea (0x38,pc,a6.l),a6 at +0x52, which puts the stream at +0x8c
            "4dfbe838" +
            "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" +
            // the stream
            "e74d4458fd216affbf7ffff827000000");

    /** what {@link #PACKED} expands to */
    private static final byte[] EXPANDED = HexFormat.of().parseHex(
            "4d44584d44584d4421" +      // "MDX" then a short match, distance 3, length 5, then '!'
            "4d44584d44584d4421" +      // a long match, distance 9, length 9
            "7f" +
            "7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f" +
            "7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f" +   // a long match, distance 1, length 40
            "0000000000");              // and the zero fill the stream stops short of

    @Test
    @DisplayName("expands one of every token the stub knows")
    void test() {
        assertTrue(Lzx.isCompressed(PACKED));

        byte[] expanded = Lzx.expand(PACKED);
        int body = Lzx.bodyOffset(PACKED);
        assertEquals(body, Lzx.bodyOffset(expanded));
        assertArrayEquals(Arrays.copyOf(PACKED, body), Arrays.copyOf(expanded, body), "the header is kept as it is");
        assertArrayEquals(EXPANDED, Arrays.copyOfRange(expanded, body, expanded.length));
        assertFalse(Lzx.isCompressed(expanded));
    }

    @Test
    @DisplayName("a song that is not packed comes back untouched")
    void test2() {
        byte[] plain = HexFormat.of().parseHex("6d6478" + "0d0a1a" + "00" + "0014000000160018fd00fd01");
        assertFalse(Lzx.isCompressed(plain));
        assertSame(plain, Lzx.expand(plain));
    }

    @Test
    @DisplayName("a truncated stream says so instead of playing as noise")
    void test3() {
        byte[] truncated = Arrays.copyOf(PACKED, PACKED.length - 6);
        assertTrue(Lzx.isCompressed(truncated));
        assertThrows(IllegalArgumentException.class, () -> Lzx.expand(truncated));
    }

    /**
     * Sweeps a whole MDX collection: every packed song in it has to expand to exactly the size
     * its stub declares, and where the same song also sits there unpacked, byte for byte to that.
     * What the offsets at the head of an expanded body point at is deliberately not checked - a
     * few songs point a part past their own data and play perfectly well, which is the same
     * reason {@link MxDriver} does not check them either.
     */
    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    @DisplayName("expands every packed song in a collection")
    void test4() throws IOException {
        assumeTrue(mdxDir != null && Files.isDirectory(Path.of(mdxDir)), "no mdx.dir to sweep");

        List<Path> packed = new ArrayList<>();
        // a packed song and the unpacked copy of it elsewhere in the collection carry the same
        // title, which is all there is to pair them by
        Map<String, List<Path>> plain = new HashMap<>();
        try (Stream<Path> files = Files.walk(Path.of(mdxDir))) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mdx"))::iterator) {
                byte[] mdx = Files.readAllBytes(file);
                if (Lzx.isCompressed(mdx)) {
                    packed.add(file);
                } else {
                    plain.computeIfAbsent(title(mdx), t -> new ArrayList<>()).add(file);
                }
            }
        }
        assumeTrue(!packed.isEmpty(), "no packed songs in " + mdxDir);

        List<Path> failed = new ArrayList<>();
        int paired = 0, same = 0;
        for (Path file : packed) {
            byte[] mdx = Files.readAllBytes(file);
            byte[] expanded;
            try {
                expanded = Lzx.expand(mdx);
                int body = Lzx.bodyOffset(mdx);
                assertEquals(ByteUtil.readBeInt(mdx, body + 0x12), expanded.length - body, file.toString());
                assertFalse(Lzx.isCompressed(expanded), file.toString());
            } catch (IllegalArgumentException | AssertionError e) {
Debug.println(file + ": " + e.getMessage());
                failed.add(file);
                continue;
            }
            Boolean matched = null;
            for (Path copy : plain.getOrDefault(title(mdx), List.of())) {
                byte[] other = Files.readAllBytes(copy);
                if (other.length != expanded.length) continue;
                matched = Arrays.equals(other, expanded);
                if (matched) break;
            }
            if (matched != null) {
                paired++;
                if (matched) same++;
            }
        }
Debug.println("packed: %d, failed: %d, paired: %d, same: %d".formatted(packed.size(), failed.size(), paired, same));

        // the collections in the wild carry a few truncated songs, which no reading of the stream
        // can bring back; a whole percent of them would mean the reading itself is wrong
        assertTrue(failed.size() * 100 < packed.size(), failed.toString());
        // a title and a size are not proof of the same song - a handful of the pairs are different
        // arrangements that happen to agree on both - so this is a ratio and not a count
        assumeTrue(paired > 0, "no unpacked copies to check against");
        assertTrue(same * 10 >= paired * 9, "only %d of %d pairs came back byte for byte".formatted(same, paired));
    }

    /** the title an MDX opens with, which is what pairs a packed song with an unpacked copy */
    private static String title(byte[] mdx) {
        int p = 0;
        while (p < mdx.length && mdx[p] != 0x0d && mdx[p] != 0x0a) p++;
        return new String(mdx, 0, p, Common.charset);
    }
}
