package mdplayer.driver.fmp;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;
import vavi.util.StringUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static mdplayer.lib.fmp.FMP.decodePc98ShiftJis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FmpMetaDataTest {

    @Test
    void testMmlMetadataWithTags() throws Exception {
        String mml = """
                #TITLE "Test Song Title"
                #COMPOSER "Test Composer"
                #ARRANGER "Test Arranger"
                #MEMO "Test Memo"
                A t120 l4 o5 cdefg
                """;
        byte[] buf = mml.getBytes(Charset.forName("MS932"));

        FmpDriver driver = new FmpDriver();
        MetaData md = driver.retrieveMetaData(buf);

        assertNotNull(md);
        assertEquals("Test Song Title", md.getFirst(Tag.Title));
        assertEquals("Test Composer", md.getFirst(Tag.Composer));
        assertEquals("Test Arranger", md.getFirst(Tag.Arranger));
        assertEquals("Test Memo", md.getFirst(Tag.Note));
    }

    @Test
    void testMmlMetadataWithSemicolonComments() throws Exception {
        String mml = """
                ; Semicolon Song Title
                ; Semicolon Composer
                ; Semicolon Note
                A t120 l4 o5 cdefg
                """;
        byte[] buf = mml.getBytes(Charset.forName("MS932"));

        FmpDriver driver = new FmpDriver();
        MetaData md = driver.retrieveMetaData(buf);

        assertNotNull(md);
        assertEquals("Semicolon Song Title", md.getFirst(Tag.Title));
        assertEquals("Semicolon Composer", md.getFirst(Tag.Composer));
        assertEquals("Semicolon Note", md.getFirst(Tag.Note));
    }

    @Test
    void testFmpFormatGetMetaData() throws Exception {
        String mml = "#TITLE \"Format Test Title\"\n#COMPOSER \"Format Test Composer\"\n";
        FMPFormat format = new FMPFormat();
        format.load(new ByteArrayInputStream(mml.getBytes(Charset.forName("MS932"))), "test.mpi");

        MetaData md = format.getMetaData();
        assertNotNull(md);
        assertEquals("Format Test Title", md.getFirst(Tag.Title));
        assertEquals("Format Test Composer", md.getFirst(Tag.Composer));
    }

    @Test
    void testFmpBinaryHeaderMetadata() throws Exception {
        byte[] buf = new byte[64];
        buf[0] = 0x10;
        buf[1] = 0x00;

        int memoPtr = 0x10;
        buf[memoPtr] = 'F';
        buf[memoPtr + 1] = 'M';
        buf[memoPtr + 2] = 'C';
        buf[memoPtr + 3] = 0x10;

        String memoStr = "Binary Title\0Binary Composer\0Binary Note\0";
        byte[] memoBytes = memoStr.getBytes(Charset.forName("MS932"));
        System.arraycopy(memoBytes, 0, buf, memoPtr + 4, memoBytes.length);

        FmpDriver driver = new FmpDriver();
        MetaData md = driver.retrieveMetaData(buf);

        assertNotNull(md);
        assertEquals("Binary Title", md.getFirst(Tag.Title));
        assertEquals("Binary Composer", md.getFirst(Tag.Composer));
        assertEquals("Binary Note", md.getFirst(Tag.Note));
    }

    /**
     * An FMC memo is an 80 column screen image: a line is placed with spaces, so the comment lines
     * have to keep the indent the tags are stripped of.
     */
    @Test
    void testFmpBinaryMemoKeepsIndent() throws Exception {
        byte[] buf = new byte[128];
        buf[0] = 0x10;
        buf[1] = 0x00;

        int memoPtr = 0x10;
        buf[memoPtr] = 'F';
        buf[memoPtr + 1] = 'M';
        buf[memoPtr + 2] = 'C';
        buf[memoPtr + 3] = 0x10;

        String memoStr = "  Title\r\n        Words by  Someone\r\n   Music by  Another\r\n\0";
        byte[] memoBytes = memoStr.getBytes(Charset.forName("MS932"));
        System.arraycopy(memoBytes, 0, buf, memoPtr + 4, memoBytes.length);

        FmpDriver driver = new FmpDriver();
        MetaData md = driver.retrieveMetaData(buf);

        assertEquals("Title", md.getFirst(Tag.Title));
        assertEquals("Words by  Someone", md.getFirst(Tag.Composer));
        assertEquals("Music by  Another", md.getFirst(Tag.Note));
        assertArrayEquals(new String[] {"  Title", "        Words by  Someone", "   Music by  Another"},
                driver.comments());
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void testSpecificFile() throws Exception {
        Path p = Path.of("/Users/nsano/Public/np2/FMPPMD/_未整理_FMP-CDROM001/ARCHIVE/いりぽん/GR2-19/GR2-19.OVI");
        if (!Files.exists(p)) return;

        byte[] buf = Files.readAllBytes(p);
        FmpDriver driver = new FmpDriver();
        MetaData md = driver.retrieveMetaData(buf);

        assertNotNull(md);
        String title = md.getFirst(Tag.Title);
        assertNotNull(title);
    }

    /**
     * A memo may be coloured and laid out with ANSI escapes, and FMP's {@code ESC ! n} marks a
     * lyric page - none of which the comment lines can honour, so none of it is text.
     */
    @Test
    void testFmpBinaryMemoDropsEscapes() throws Exception {
        byte[] buf = new byte[128];
        buf[0] = 0x10;
        buf[1] = 0x00;

        int memoPtr = 0x10;
        buf[memoPtr] = 'F';
        buf[memoPtr + 1] = 'M';
        buf[memoPtr + 2] = 'C';
        buf[memoPtr + 3] = 0x10;

        String memoStr = "\u001b[31m  Title\u001b[m\r\n"
                + "\u001b!\u0001\u001b[11;28HWords by\u001b!!  Someone\r\n\0";
        byte[] memoBytes = memoStr.getBytes(Charset.forName("MS932"));
        System.arraycopy(memoBytes, 0, buf, memoPtr + 4, memoBytes.length);

        FmpDriver driver = new FmpDriver();
        MetaData md = driver.retrieveMetaData(buf);

        assertEquals("Title", md.getFirst(Tag.Title));
        assertEquals("Words by  Someone", md.getFirst(Tag.Composer));
        assertArrayEquals(new String[] {"  Title", "Words by  Someone"}, driver.comments());
    }

    /** FMP writes half width ASCII with the double byte lead 0x85, JIS X 0208 row 9. */
    @Test
    void testHalfWidthAsciiRow() throws Exception {
        byte[] buf = {
                (byte) 0x85, 0x76, (byte) 0x85, (byte) 0x8f, (byte) 0x85, (byte) 0x92,
                (byte) 0x85, (byte) 0x84, (byte) 0x85, (byte) 0x93, 0x20,
                (byte) 0x85, (byte) 0x82, (byte) 0x85, (byte) 0x99
        };
        assertEquals("Words by", decodePc98ShiftJis(buf, 0, buf.length));
    }

    /** Row 10 of the same escape: half width katakana, then the precomposed voiced ones. */
    @Test
    void testHalfWidthKatakanaRow() throws Exception {
        byte[] buf = {
                (byte) 0x85, (byte) 0xbb, (byte) 0x85, (byte) 0xc1, (byte) 0x85, (byte) 0xae,
                (byte) 0x85, (byte) 0xea, 0x20,
                (byte) 0x85, (byte) 0xf4, (byte) 0x85, (byte) 0xda, (byte) 0x85, (byte) 0xae
        };
        assertEquals("\uFF7D\uFF83\uFF70\uFF7C\uFF9E \uFF8A\uFF9F\uFF9C\uFF70", decodePc98ShiftJis(buf, 0, buf.length));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void testMakenaidFile() throws Exception {
        Path p = Path.of("/Users/nsano/Public/np2/FMPPMD/Pops/MAKENAID.OPI");
        if (!Files.exists(p)) return;

        byte[] buf = Files.readAllBytes(p);
        int memoPtr = (ByteUtil.readLeShort(buf, 0) & 0xffff);
        int end = Math.min(memoPtr + 400, buf.length);

        String decoded = decodePc98ShiftJis(buf, memoPtr + 4, end);
        System.out.println("Decoded PC-98 Shift_JIS memo:\n" + decoded);

        assertTrue(decoded.contains("Words by"), decoded);
        assertTrue(decoded.contains("Music by"), decoded);
        assertTrue(decoded.contains("FMP Arranged by"), decoded);
        assertTrue(decoded.contains("\u300E\u8CA0\u3051\u306A\u3044\u3067\u300F"), decoded);
    }

    /** half width katakana written as single bytes, which the 0x85 escape must not steal */
    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void testNrthncrsFile() throws Exception {
        Path p = Path.of("/Users/nsano/Public/np2/FMPData/MusicData/NRTHNCRS.OZI");
        if (!Files.exists(p)) return;

        byte[] buf = Files.readAllBytes(p);
        int memoPtr = (ByteUtil.readLeShort(buf, 0) & 0xffff);
        int end = Math.min(memoPtr + 300, buf.length);

        String decoded = decodePc98ShiftJis(buf, memoPtr + 4, end);
        System.out.println("Decoded PC-98 Shift_JIS memo:\n" + decoded);

        assertTrue(decoded.contains("\uFF71\uFF86\uFF92\u300C\uFF8F\uFF78\uFF9B\uFF7DF\u300D\uFF74\uFF9D\uFF83\uFF9E\uFF68\uFF9D\uFF78\uFF9E\uFF83\uFF70\uFF8F"), decoded);
        assertTrue(decoded.contains("starring May'n"), decoded);
    }

    @Test
    void testProlongedSoundMark() throws Exception {
        char[] chars = {
            '\u30FC', // ー KATAKANA-HIRAGANA PROLONGED SOUND MARK
            '\uFF70', // ｰ HALFWIDTH KATAKANA HIRAGANA PROLONGED SOUND MARK
            '\u2015', // ― EM DASH (JIS 0x213C)
            '\u2014', // — EM DASH (Unicode)
            '\u2013', // – EN DASH
            '\u2212', // − MINUS SIGN
            '\uFF0D', // － FULLWIDTH HYPHEN-MINUS
            '\u2010', // ‐ HYPHEN
        };

        FmpDriver driver = new FmpDriver();
        for (char c : chars) {
            byte[] sjis = String.valueOf(c).getBytes(Charset.forName("MS932"));
            System.out.printf("Char '%c' (U+%04X): MS932=%s\n", c, (int)c, StringUtil.getDump(sjis));
        }
    }
}
