/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.mdsdrv;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

import mdplayer.PlayList;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.sound.ctrmml.FileResolver;
import vavi.sound.ctrmml.MdsLink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * The song metadata of a {@code .mds}, which the file only carries when it was built by
 * vavi-sound-mdsdrv's compiler: the format MDSDRV itself ships has nowhere to put a title, so
 * anything its tools built plays with the file name and nothing else.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-02 nsano initial version <br>
 */
class MdsMetaDataTest {

    /** the smallest MML that compiles, with the tags a player shows */
    static final String MML = """
            #title Test Song
            #composer nsano
            #game Test Game
            #date 2026-08-02
            #platform megadrive

            A o4 c4 d4 e4 f4
            """;

    static byte[] mds(String mml) {
        InputStream in = new java.io.ByteArrayInputStream(mml.getBytes(StandardCharsets.UTF_8));
        return MdsLink.compile("test.mml", in, FileResolver.FILE_SYSTEM).toBytes().toByteArray();
    }

    /** the same file as MDSDRV's own tools would have written it, without the metadata chunk */
    static byte[] untaggedMds(String mml) {
        InputStream in = new java.io.ByteArrayInputStream(mml.getBytes(StandardCharsets.UTF_8));
        return MdsLink.compile("test.mml", in, FileResolver.FILE_SYSTEM, false).toBytes().toByteArray();
    }

    @Test
    @DisplayName("the driver reads the metadata chunk of a .mds")
    void testMetaData() {
        MetaData metaData = new MdsDriver().retrieveMetaData(mds(MML));

        assertEquals("Test Song", metaData.getFirst(Tag.Title));
        assertEquals("Test Song", metaData.getFirst(Tag.TitleJ));
        assertEquals("nsano", metaData.getFirst(Tag.Composer));
        assertEquals("Test Game", metaData.getFirst(Tag.GameTitle));
        assertEquals("2026-08-02", metaData.getFirst(Tag.ReleaseDate));
    }

    @Test
    @DisplayName("a .mds without the chunk reads as no metadata at all")
    void testNoMetaData() {
        MetaData metaData = new MdsDriver().retrieveMetaData(untaggedMds(MML));

        assertTrue(metaData.getFirst(Tag.Title).isEmpty());
        assertTrue(metaData.getFirst(Tag.Composer).isEmpty());
    }

    /** a format holding the file, the way the play list scan leaves it before {@code getMusic} */
    static MdsFileFormat format(byte[] buf) {
        return new MdsFileFormat() {
            {
                this.srcBuf = buf;
            }
        };
    }

    @Test
    @DisplayName("the play list takes its title and composer from the file, the file name being the fallback")
    void testMusic() {
        List<PlayList.Music> musics =
                format(mds(MML)).getMusic("dir/tagged.mds", null, null, null, null);
        assertEquals("Test Song", musics.getFirst().title);
        assertEquals("nsano", musics.getFirst().composer);
        assertEquals("Test Game", musics.getFirst().game);

        musics = format(untaggedMds(MML)).getMusic("dir/untagged.mds", null, null, null, null);
        assertEquals("untagged.mds", musics.getFirst().title);
        assertTrue(musics.getFirst().composer.isEmpty());
    }

    /** so a failure here reads as "the compiler stopped producing a playable file" */
    @Test
    @DisplayName("the metadata chunk leaves the sequence alone")
    void testSameSequence() {
        byte[] tagged = mds(MML);
        byte[] untagged = untaggedMds(MML);
        assertTrue(tagged.length > untagged.length);
        assertEquals(dump(untagged, "seq "), dump(tagged, "seq "));
    }

    /** the content of the named chunk, as hex */
    static String dump(byte[] mds, String chunkId) {
        int p = 12;
        while (p < mds.length - 8) {
            String id = new String(mds, p, 4, StandardCharsets.ISO_8859_1);
            int size = (mds[p + 4] & 0xff) | ((mds[p + 5] & 0xff) << 8)
                    | ((mds[p + 6] & 0xff) << 16) | ((mds[p + 7] & 0xff) << 24);
            if (id.equals(chunkId)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(mds, p + 8, size);
                return HexFormat.of().formatHex(baos.toByteArray());
            }
            p += 8 + size + (size & 1);
        }
        return "";
    }
}
