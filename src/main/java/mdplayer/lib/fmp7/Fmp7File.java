/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.fmp7;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * What an FMP7 song (".owi") says about itself.
 * <p>
 * The player itself would answer this - {@code GetMusicTitle} and friends are part of the api -
 * but only once the song is loaded and only over a window message, and mdplayer wants the title
 * of every file in a directory without starting an emulated PC for each. The file says the same
 * thing: after a 16 byte header it is a run of chunks - four characters, a length, a checksum
 * and a spare word - and the "TEXT" one holds the title, the two credits and the comment as
 * their own smaller chunks, in UTF-16, up to a "TEND".
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 */
public class Fmp7File {

    /** what an FMP7 song starts with */
    private static final byte[] MAGIC = {'O', 'W', 'I', '0'};

    /** the file header, which the chunks follow */
    private static final int HEADER_SIZE = 16;

    /** four characters, a length, a checksum and a spare word */
    private static final int CHUNK_HEADER_SIZE = 16;

    /** the text chunks inside "TEXT" have no checksum, only a name and a length */
    private static final int TEXT_HEADER_SIZE = 8;

    /**
     * A wave bank the song plays its PCM parts out of: the name it is kept under, without the
     * ".pwi", and where that name sits in the file so it can be written over - see
     * {@link Fmp7Player} on why a name sometimes has to be.
     */
    public record WaveBank(String name, int offset, int bytes) {
    }

    /** the wave banks the song names, in the order it names them */
    private final java.util.List<WaveBank> waveBanks = new java.util.ArrayList<>();

    private String title;
    private String creator;
    private String composer;
    private String comment;

    /** does this look like an FMP7 song at all? */
    public static boolean isFmp7(byte[] b) {
        if (b == null || b.length < HEADER_SIZE) {
            return false;
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (b[i] != MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    public static Fmp7File decode(byte[] b) throws IOException {
        if (!isFmp7(b)) {
            throw new IOException("not an FMP7 song");
        }
        Fmp7File file = new Fmp7File();
        int p = HEADER_SIZE;
        while (p + CHUNK_HEADER_SIZE <= b.length) {
            int size = le32(b, p + 4);
            if (size < 0 || p + CHUNK_HEADER_SIZE + size > b.length) {
                break;
            }
            if (is(b, p, "TEXT")) {
                file.readText(b, p + CHUNK_HEADER_SIZE, p + CHUNK_HEADER_SIZE + size);
            } else if (is(b, p, "TWNM")) {
                // the wave banks, one nul terminated name after another, no extension. The
                // advance is measured off the nul rather than off the name, which has been
                // trimmed by the time it is a string
                int at = p + CHUNK_HEADER_SIZE;
                int end = at + size;
                while (at + 1 < end) {
                    int nul = at;
                    while (nul + 1 < end && (b[nul] != 0 || b[nul + 1] != 0)) {
                        nul += 2;
                    }
                    String name = string(b, at, nul - at);
                    if (!name.isEmpty()) {
                        file.waveBanks.add(new WaveBank(name, at, nul - at));
                    }
                    at = nul + 2;
                }
            } else if (is(b, p, "TERM")) {
                break;
            }
            p += CHUNK_HEADER_SIZE + size;
        }
        return file;
    }

    private void readText(byte[] b, int from, int to) {
        int p = from;
        while (p + TEXT_HEADER_SIZE <= to) {
            if (is(b, p, "TEND")) {
                return;
            }
            int size = le32(b, p + 4);
            if (size < 0 || p + TEXT_HEADER_SIZE + size > to) {
                return;
            }
            String value = string(b, p + TEXT_HEADER_SIZE, size);
            if (is(b, p, "TTL0")) {
                title = value;
            } else if (is(b, p, "CRAT")) {
                creator = value;
            } else if (is(b, p, "COMP")) {
                composer = value;
            } else if (is(b, p, "CMNT")) {
                comment = value;
            }
            p += TEXT_HEADER_SIZE + size;
        }
    }

    /** UTF-16, as far as its first nul */
    private static String string(byte[] b, int offset, int length) {
        int end = offset;
        while (end + 1 < offset + length && (b[end] != 0 || b[end + 1] != 0)) {
            end += 2;
        }
        return new String(b, offset, end - offset, StandardCharsets.UTF_16LE).trim();
    }

    private static boolean is(byte[] b, int offset, String id) {
        for (int i = 0; i < 4; i++) {
            if (b[offset + i] != id.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static int le32(byte[] b, int o) {
        return (b[o] & 0xff) | (b[o + 1] & 0xff) << 8 | (b[o + 2] & 0xff) << 16 | (b[o + 3] & 0xff) << 24;
    }

    public String getTitle() {
        return title;
    }

    /** who made the data, which is not always who wrote the tune */
    public String getCreator() {
        return creator;
    }

    public String getComposer() {
        return composer;
    }

    public String getComment() {
        return comment;
    }

    /**
     * The wave banks this song plays its PCM parts out of, named without the ".pwi" they are
     * kept in. A song that names one and cannot find it does not play at all - FMP7 gives up
     * during loading - so this is what {@link Fmp7Player} goes looking for.
     */
    public java.util.List<WaveBank> getWaveBanks() {
        return waveBanks;
    }
}
