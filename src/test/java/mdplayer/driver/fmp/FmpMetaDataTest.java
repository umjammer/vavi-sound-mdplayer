package mdplayer.driver.fmp;

import java.nio.charset.Charset;

import mdplayer.format.FMPFormat;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import org.junit.jupiter.api.Test;

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
}
