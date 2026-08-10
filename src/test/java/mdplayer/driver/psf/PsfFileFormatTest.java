/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.psf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import mdplayer.driver.FileFormat;
import mdplayer.driver.psf2.Psf2FileFormat;
import mdplayer.lib.psf.PsfEngine;
import mdplayer.lib.psf.PsfFile;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vavi.util.compat.Tuple;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * The file format side: what the playlist reads out of a psf, and how a ".minipsf" finds
 * the library its "_lib" tag names.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
class PsfFileFormatTest {

    static final Path psf = Path.of("tmp/psf/pe.psf");
    static final Path psf2 = Path.of("tmp/psf/01.psf2");

    @Test
    void formatIsFoundByExtension() {
        assertInstanceOf(PsfFileFormat.class, FileFormat.getFileFormat("song.psf"));
        assertInstanceOf(PsfFileFormat.class, FileFormat.getFileFormat("song.minipsf"));
        assertInstanceOf(Psf2FileFormat.class, FileFormat.getFileFormat("song.psf2"));
        assertInstanceOf(Psf2FileFormat.class, FileFormat.getFileFormat("song.minipsf2"));
    }

    /** the samples live under tmp/, which is not in the repository - see driver/readme.md */
    private static void needSamples() {
        assumeTrue(Files.exists(psf) && Files.exists(psf2), "tmp/psf is missing, see mdplayer/driver/readme.md");
    }

    @Test
    void metaData() throws Exception {
        needSamples();
        MetaData md = new PsfDriver().getMetaData(Files.readAllBytes(psf));
        assertNotNull(md);
        assertEquals("110 Main Theme (Piano Solo)", md.getFirst(Tag.Title));
        assertEquals("Parasite Eve", md.getFirst(Tag.GameTitle));
        assertEquals("1:57", md.getFirst(Tag.Duration));
    }

    /**
     * Builds a ".minipsf" that holds only a PS-X EXE header and names the whole song as its
     * library, then checks the format hands that library over and the engine plays the same
     * audio as the original.
     */
    @Test
    void minipsfFindsItsLibrary(@TempDir Path dir) throws Exception {
        needSamples();
        byte[] original = Files.readAllBytes(psf);
        PsfFile full = PsfFile.decode(original);

        // the library is the song itself
        Path lib = dir.resolve("pe.psflib");
        Files.write(lib, original);

        // and the mini file is its header with an empty text section, plus the "_lib" tag
        byte[] header = new byte[2048];
        System.arraycopy(full.program, 0, header, 0, 2048);
        writeLeInt(header, 0x1c, 0); // text length
        Path mini = dir.resolve("song.minipsf");
        Files.write(mini, buildPsf(1, header, "_lib=pe.psflib\n"));

        PsfFileFormat format = new PsfFileFormat();
        try (InputStream is = new java.io.BufferedInputStream(Files.newInputStream(mini))) {
            format.load(is, null);
        }

        List<Tuple<String, byte[]>> extend = format.getExtendFiles();
        assertNotNull(extend);
        assertEquals(1, extend.size());
        assertEquals("pe.psflib", extend.getFirst().getItem1());
        assertArrayEquals(original, extend.getFirst().getItem2());

        // the mini file brings nothing of its own, so it has to sound like the original
        PsfFile[] files = PsfFile.load(Files.readAllBytes(mini),
                name -> name.equals("pe.psflib") ? original : null);
        PsfEngine engine = new PsfEngine();
        engine.start(files);

        short[] mine = new short[60 * 735 * 2];
        int p = 0;
        for (int f = 0; f < 60; f++) {
            for (int s = 0; s < 735; s++) {
                engine.sample();
                mine[p++] = (short) engine.getLeft();
                mine[p++] = (short) engine.getRight();
            }
            engine.frame();
        }

        short[] reference = PsfEngineTest.readWav(PsfEngineTest.reference, mine.length);
        assertArrayEquals(reference, mine, "a minipsf of the whole song should sound like it");
    }

    @Test
    void psf2Version() throws Exception {
        needSamples();
        assertEquals(2, PsfFile.decode(Files.readAllBytes(psf2)).version);
        assertEquals(1, PsfFile.decode(Files.readAllBytes(psf)).version);
    }

    /** the container as {@link PsfFile} reads it back */
    static byte[] buildPsf(int version, byte[] program, String tags) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(program);
        deflater.finish();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        byte[] buf = new byte[0x10000];
        while (!deflater.finished()) {
            int n = deflater.deflate(buf);
            compressed.write(buf, 0, n);
        }
        deflater.end();
        byte[] comp = compressed.toByteArray();

        CRC32 crc = new CRC32();
        crc.update(comp);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write('P');
        out.write('S');
        out.write('F');
        out.write(version);
        writeLeInt(out, 0);            // no reserved area
        writeLeInt(out, comp.length);
        writeLeInt(out, (int) crc.getValue());
        out.write(comp);
        out.write("[TAG]".getBytes(StandardCharsets.ISO_8859_1));
        out.write(tags.getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    static void writeLeInt(ByteArrayOutputStream out, int v) {
        out.write(v & 0xff);
        out.write((v >> 8) & 0xff);
        out.write((v >> 16) & 0xff);
        out.write((v >>> 24) & 0xff);
    }

    static void writeLeInt(byte[] b, int offset, int v) {
        b[offset] = (byte) v;
        b[offset + 1] = (byte) (v >> 8);
        b[offset + 2] = (byte) (v >> 16);
        b[offset + 3] = (byte) (v >>> 24);
    }

}
