package mdplayer.driver.mgsdrv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class MgsMetaDataTest {

    @Test
    void testMgsHeaderMetadataParsing() throws Exception {
        String headerMagic = "MGS313\r\n";
        String metaText = "Test MGS Title\r\nTest MGS Composer\r\nTest MGS Note\r\n\u001a";
        byte[] magicBytes = headerMagic.getBytes(StandardCharsets.US_ASCII);
        byte[] metaBytes = metaText.getBytes(Charset.forName("MS932"));

        byte[] buf = new byte[magicBytes.length + metaBytes.length + 10];
        System.arraycopy(magicBytes, 0, buf, 0, magicBytes.length);
        System.arraycopy(metaBytes, 0, buf, magicBytes.length, metaBytes.length);

        MgsDriver driver = new MgsDriver();
        MetaData md = driver.retrieveMetaData(buf, 8);

        assertNotNull(md);
        assertEquals("Test MGS Title", md.getFirst(Tag.Title));
        assertEquals("Test MGS Composer", md.getFirst(Tag.Composer));
        assertEquals("Test MGS Note", md.getFirst(Tag.Note));
        assertArrayEquals(new String[] {"Test MGS Title", "Test MGS Composer", "Test MGS Note"}, md.getAll(Tag.Comments).toArray(String[]::new));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void testSampleFfvMainMgsFile() throws Exception {
        Path sample = Path.of("tmp/mgs/FFVMAINT.MGS");
        if (!Files.exists(sample)) return;

        byte[] buf = Files.readAllBytes(sample);
        MgsDriver driver = new MgsDriver();
        MetaData md = driver.retrieveMetaData(buf, 8);

        assertNotNull(md);
        assertEquals("SQUARESOFT   FINAL FANTASY V   - MAIN THEME -", md.getFirst(Tag.Title));
        assertEquals("BY UNISKIE", md.getFirst(Tag.Composer));
        assertArrayEquals(new String[] {"SQUARESOFT   FINAL FANTASY V   - MAIN THEME -", "   BY UNISKIE"}, md.getAll(Tag.Comments).toArray(String[]::new));
    }
}
