package mdplayer.driver.fmp;

import java.nio.charset.Charset;

import mdplayer.format.FMPFormat;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
        MetaData md = driver.getMetaData(buf);

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
        MetaData md = driver.getMetaData(buf);

        assertNotNull(md);
        assertEquals("Semicolon Song Title", md.getFirst(Tag.Title));
        assertEquals("Semicolon Composer", md.getFirst(Tag.Composer));
        assertEquals("Semicolon Note", md.getFirst(Tag.Note));
    }

    @Test
    void testFmpFormatGetMetaData() throws Exception {
        String mml = "#TITLE \"Format Test Title\"\n#COMPOSER \"Format Test Composer\"\n";
        FMPFormat format = new FMPFormat();
        format.load(new java.io.ByteArrayInputStream(mml.getBytes(Charset.forName("MS932"))), "test.mpi");

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
        MetaData md = driver.getMetaData(buf);

        assertNotNull(md);
        assertEquals("Binary Title", md.getFirst(Tag.Title));
        assertEquals("Binary Composer", md.getFirst(Tag.Composer));
        assertEquals("Binary Note", md.getFirst(Tag.Note));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void testSpecificFile() throws Exception {
        java.nio.file.Path p = java.nio.file.Path.of("/Users/nsano/Public/np2/FMPPMD/_未整理_FMP-CDROM001/ARCHIVE/いりぽん/GR2-19/GR2-19.OVI");
        if (!java.nio.file.Files.exists(p)) return;

        byte[] buf = java.nio.file.Files.readAllBytes(p);
        FmpDriver driver = new FmpDriver();
        MetaData md = driver.getMetaData(buf);

        assertNotNull(md);
        String title = md.getFirst(Tag.Title);
        assertNotNull(title);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void testMakenaidFile() throws Exception {
        java.nio.file.Path p = java.nio.file.Path.of("/Users/nsano/Public/np2/FMPPMD/Pops/MAKENAID.OPI");
        if (!java.nio.file.Files.exists(p)) return;

        byte[] buf = java.nio.file.Files.readAllBytes(p);
        int memoPtr = (vavi.util.ByteUtil.readLeShort(buf, 0) & 0xffff);
        int end = Math.min(memoPtr + 250, buf.length);

        String decoded = decodePc98ShiftJis(buf, memoPtr + 4, end);
        System.out.println("Decoded PC-98 Shift_JIS memo:\n" + decoded);
    }

    private static String decodePc98ShiftJis(byte[] buf, int start, int end) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < end) {
            int b1 = buf[i] & 0xff;
            if (b1 == 0) break;

            if (b1 == 0x85 && i + 1 < end) {
                int b2 = buf[i + 1] & 0xff;
                int k = b2 + 0x40;
                if (k >= 0xa1 && k <= 0xdf) {
                    sb.append((char)(0xff61 + (k - 0xa1)));
                    i += 2;
                    continue;
                }
            }

            if (b1 >= 0xa1 && b1 <= 0xdf) {
                sb.append((char)(0xff61 + (b1 - 0xa1)));
                i++;
                continue;
            }

            if (((b1 >= 0x81 && b1 <= 0x9f) || (b1 >= 0xe0 && b1 <= 0xfc)) && i + 1 < end) {
                int b2 = buf[i + 1] & 0xff;
                if ((b2 >= 0x40 && b2 <= 0x7e) || (b2 >= 0x80 && b2 <= 0xfc)) {
                    byte[] sjis = new byte[] {(byte)b1, (byte)b2};
                    String s = new String(sjis, Charset.forName("MS932"));
                    if (!s.isEmpty() && s.charAt(0) != '\uFFFD') {
                        sb.append(s);
                    } else {
                        int j1 = (b1 < 0xe0 ? b1 - 0x81 : b1 - 0xc1) * 2 + 0x21;
                        int s2 = b2;
                        if (s2 >= 0x9e) {
                            j1++;
                            s2 -= 0x9e;
                        } else {
                            s2 -= 0x40;
                            if (s2 >= 0x3f) s2--;
                        }
                        int j2 = s2 + 0x21;
                        int jis = (j1 << 8) | j2;
                        sb.append((char)(0xE000 + jis));
                    }
                    i += 2;
                    continue;
                }
            }

            sb.append((char)b1);
            i++;
        }
        return sb.toString();
    }

    @Test
    void testProlongedSoundMark() throws Exception {
        char[] chars = new char[] {
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
            byte[] sjis = String.valueOf(c).getBytes(java.nio.charset.Charset.forName("MS932"));
            System.out.printf("Char '%c' (U+%04X): MS932=%s\n", c, (int)c, vavi.util.StringUtil.getDump(sjis));
        }
    }
}
