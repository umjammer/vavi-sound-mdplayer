/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.mgsdrv;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import mdplayer.lib.mgsc.MgscCompiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * MGSC, the MGSDRV MML compiler.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-30 nsano initial version <br>
 */
class MgscCompilerTest {

    /** where the songs that have both an MML and the ".mgs" it was compiled to live, if anywhere */
    static final Path CORPUS = Path.of("/Users/nsano/Public/np2/msx_music_data-master/Mus-MGSDRV");

    @BeforeEach
    void setup() {
        System.setProperty("mdplayer.mgs.dir", "tmp/driver/mgsdrv");
    }

    @Test
    @DisplayName("an MGSDRV MML is the one with MGSC's directives in it")
    void testIsMgsMml() {
        assertTrue(MgscCompiler.isMgsMml("#opll_mode 1\r\n#tempo 120\r\n".getBytes(StandardCharsets.US_ASCII)));
        // MGSC allows the directives to be indented, and FFVMAINT.MUS does exactly that
        assertTrue(MgscCompiler.isMgsMml(";comment\r\n \t#title {\"x\"}\r\n".getBytes(StandardCharsets.US_ASCII)));
        assertTrue(MgscCompiler.isMgsMml("#TEMPO 216\r\n".getBytes(StandardCharsets.US_ASCII)));

        // MUAP98's MML is called ".mus" too and has no directive lines at all
        assertFalse(MgscCompiler.isMgsMml("_V3.0#\r\n\r\n1[ @dt20 @r ]\r\n".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(MgscCompiler.isMgsMml(";---\r\n\t_V2.4#\r\n".getBytes(StandardCharsets.US_ASCII)));
        // a "#" that is not one of MGSC's directives says nothing
        assertFalse(MgscCompiler.isMgsMml("#nonsense 1\r\n".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(MgscCompiler.isMgsMml(new byte[0]));
    }

    @Test
    @DisplayName("compiles MML to a playable .mgs")
    void testCompile() throws Exception {
        assumeTrue(MgscCompiler.isAvailable(), MgscCompiler.COMPILER + " is not there");

        String mml = """
                #opll_mode 1
                #tempo 120
                #title {"TEST TITLE"
                        "TEST COMPOSER"}
                9 o4 l4 v13 @0 cdefgab>c<
                """;
        byte[] mgs = new MgscCompiler().compile(mml.getBytes(StandardCharsets.US_ASCII));

        assertEquals("MGS", new String(mgs, 0, 3, StandardCharsets.US_ASCII));
        assertEquals("\r\n", new String(mgs, 6, 2, StandardCharsets.US_ASCII));
        String header = new String(mgs, 8, indexOf(mgs, (byte) 0x1a) - 8, StandardCharsets.US_ASCII);
        assertEquals(List.of("TEST TITLE", "TEST COMPOSER"),
                Arrays.stream(header.split("\r\n")).map(String::strip).filter(l -> !l.isEmpty()).toList());
        // MGSC writes through MSX-DOS a record at a time, so what comes out is whole records
        assertEquals(0, mgs.length % 128);
        // the binary header follows the EOF marker, and opens with the marker MGSDRV looks for
        assertEquals(0x00, mgs[indexOf(mgs, (byte) 0x1a) + 1]);
    }

    @Test
    @DisplayName("says what it did not like, rather than handing back a silent song")
    void testCompileError() {
        assumeTrue(MgscCompiler.isAvailable(), MgscCompiler.COMPILER + " is not there");

        MgscCompiler compiler = new MgscCompiler();
        Exception e = assertThrows(Exception.class,
                () -> compiler.compile("#opll_mode 1\r\n9 @@@@@\r\n".getBytes(StandardCharsets.US_ASCII)));
        assertTrue(e.getMessage().contains(MgscCompiler.COMPILER), e.getMessage());
    }

    @Test
    @DisplayName("compiles every song in the corpus to the .mgs that ships beside it")
    void testCorpus() throws Exception {
        assumeTrue(MgscCompiler.isAvailable(), MgscCompiler.COMPILER + " is not there");
        assumeTrue(Files.isDirectory(CORPUS), CORPUS + " is not there");

        try (var files = Files.list(CORPUS)) {
            List<Path> mmls = files.filter(p -> p.toString().toLowerCase().endsWith(".mus")).sorted().toList();
            assumeTrue(!mmls.isEmpty(), "no MML in " + CORPUS);
            for (Path mml : mmls) {
                Path expected = Path.of(mml.toString().replaceFirst("(?i)\\.mus$", ".MGS"));
                if (!Files.exists(expected)) continue;
                byte[] compiled = new MgscCompiler().compile(Files.readAllBytes(mml));
                if (mml.getFileName().toString().equalsIgnoreCase("YS1BEATY.MUS")) {
                    // the ".mgs" that ships with this one is older than its MML: it has the OPLL
                    // bit set where the MML now says "#opll_mode 0". The real MGSC.COM agrees
                    // with us, so compare everything except the flags byte
                    compiled[0x49] = Files.readAllBytes(expected)[0x49];
                }
                assertArrayEquals(Files.readAllBytes(expected), compiled, mml.getFileName().toString());
            }
        }
    }

    private static int indexOf(byte[] buf, byte value) {
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == value) return i;
        }
        return -1;
    }
}
