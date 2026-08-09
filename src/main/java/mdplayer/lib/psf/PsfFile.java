/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.psf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static java.lang.System.getLogger;


/**
 * A file in the container format Neill Corlett designed for PSF, QSF, SSF, DSF, ...
 * <p>
 * ported from aosdk corlett.c
 *
 * <pre>
 * 0x00  3 bytes  "PSF"
 * 0x03  1 byte   version (0x01: PS1, 0x02: PS2, 0x11: Saturn, 0x12: Dreamcast, 0x41: QSound)
 * 0x04  4 bytes  size of the reserved area (R)
 * 0x08  4 bytes  compressed program length (N)
 * 0x0c  4 bytes  crc32 of the compressed program
 * 0x10  R bytes  reserved area
 *       N bytes  program, zlib compress() format
 *       5 bytes  "[TAG]"
 *       ...      uncompressed ascii tag data
 * </pre>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-09 nsano initial version <br>
 */
public class PsfFile {

    private static final Logger logger = getLogger(PsfFile.class.getName());

    /** the number of "_lib" tags a file may name */
    public static final int MAX_LIBS = 9;

    /** the biggest program aosdk is willing to decompress */
    private static final int DECOMP_MAX_SIZE = (32 * 1024 * 1024) + 12;

    /** version byte, 1 for PSF1, 2 for PSF2 */
    public int version;

    /** reserved area, the PSF2 filesystem lives here */
    public byte[] reserved;

    /** decompressed program, empty for PSF2 */
    public byte[] program;

    /** case insensitive, as the tags are looked up by name */
    public final Map<String, String> tags = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /** @return null when the file has no such tag */
    public String tag(String name) {
        return tags.get(name);
    }

    /** the "length" tag in seconds, 0 when absent */
    public double length() {
        return timeToSeconds(tag("length"));
    }

    /** the "fade" tag in seconds, 0 when absent */
    public double fade() {
        return timeToSeconds(tag("fade"));
    }

    /**
     * Loads a file and the libraries it names.
     *
     * @param resolver gives the raw bytes of a library by the name a "_lib" tag holds,
     *                 null when the file cannot be found
     * @return index 0 is the file itself, 1 is "_lib", 2 is "_lib2" ... 9 is "_lib9",
     *         with a null where the file names no library.
     *         Libraries of libraries are deliberately not followed, as aosdk does not follow them.
     */
    public static PsfFile[] load(byte[] input, Function<String, byte[]> resolver) throws IOException {
        PsfFile[] result = new PsfFile[MAX_LIBS + 1];

        PsfFile main = decode(input);
        result[0] = main;

        for (int i = 0; i < MAX_LIBS; i++) {
            String tagName = i == 0 ? "_lib" : "_lib" + (i + 1);
            String libFile = main.tag(tagName);
            if (libFile == null) {
                continue;
            }
logger.log(Level.DEBUG, "loading library #%d: %s".formatted(i + 1, libFile));
            byte[] raw = resolver != null ? resolver.apply(libFile) : null;
            if (raw == null) {
                throw new IOException("unable to find auxiliary file: " + libFile);
            }
            result[i + 1] = decode(raw);
        }

        return result;
    }

    /** Decodes one file, without following its "_lib" tags. */
    public static PsfFile decode(byte[] input) throws IOException {
        if (input.length < 16 || input[0] != 'P' || input[1] != 'S' || input[2] != 'F') {
            throw new IOException("not a PSF format file");
        }

        PsfFile psf = new PsfFile();
        psf.version = input[3] & 0xff;

        int resArea = readLeInt(input, 4);
        int compLength = readLeInt(input, 8);
        int compCrc = readLeInt(input, 12);

        if (resArea < 0 || compLength < 0 || 16 + (long) resArea + compLength > input.length) {
            throw new IOException("PSF is truncated");
        }

        psf.reserved = new byte[resArea];
        System.arraycopy(input, 16, psf.reserved, 0, resArea);

        int compOffset = 16 + resArea;
        if (compLength > 0) {
            CRC32 crc32 = new CRC32();
            crc32.update(input, compOffset, compLength);
            if ((int) crc32.getValue() != compCrc) {
                throw new IOException("PSF crc mismatch");
            }
            psf.program = uncompress(input, compOffset, compLength);
        } else {
            psf.program = new byte[0];
        }

        int tagOffset = compOffset + compLength;
        decodeTags(psf, input, tagOffset, input.length - tagOffset);

        return psf;
    }

    /** zlib compress() format, i.e. with the zlib header */
    private static byte[] uncompress(byte[] input, int offset, int length) throws IOException {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(input, offset, length);
            byte[] buffer = new byte[0x10000];
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            while (!inflater.finished()) {
                int n = inflater.inflate(buffer);
                if (n == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) {
                        throw new IOException("PSF decompression is truncated");
                    }
                    continue;
                }
                baos.write(buffer, 0, n);
                if (baos.size() > DECOMP_MAX_SIZE) {
                    throw new IOException("PSF program is too large");
                }
            }
            return baos.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException(e);
        } finally {
            inflater.end();
        }
    }

    /**
     * "[TAG]" then lines of "name=value".
     * <p>
     * The bytes are read as latin-1 first, which never fails, and the values are re-read as
     * utf-8 when the file says "utf8=1", the way the format documents it.
     */
    private static void decodeTags(PsfFile psf, byte[] input, int offset, int length) {
        if (length < 5) {
            return;
        }
        if (input[offset] != '[' || input[offset + 1] != 'T' || input[offset + 2] != 'A'
                || input[offset + 3] != 'G' || input[offset + 4] != ']') {
            return;
        }
        offset += 5;
        length -= 5;

        // one extra byte so a tag whose value ends at EOF terminates like the others do
        byte[] buffer = new byte[length + 1];
        System.arraycopy(input, offset, buffer, 0, length);

        Map<String, byte[]> raw = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String name = null;
        int start = -1;
        boolean atData = false;
        for (int p = 0; p < buffer.length; p++) {
            byte c = buffer[p];
            boolean terminates = atData ? (c == '\n' || c == 0) : (c == '=');
            if (terminates) {
                // a delimiter with nothing before it leaves the name or the value unset, and
                // aosdk then stores nothing - an empty tag simply does not exist
                byte[] value = start < 0 ? null : java.util.Arrays.copyOfRange(buffer, start, p);
                if (atData) {
                    if (name != null && value != null) {
                        raw.put(name, value);
                        name = null;
                    }
                } else {
                    name = value != null ? new String(value, StandardCharsets.ISO_8859_1) : null;
                }
                atData = !atData;
                start = -1;
            } else if (start < 0 && c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                start = p;
            }
        }

        byte[] utf8 = raw.get("utf8");
        boolean isUtf8 = utf8 != null && utf8.length > 0 && utf8[0] == '1';
        for (Map.Entry<String, byte[]> e : raw.entrySet()) {
            psf.tags.put(e.getKey(), new String(e.getValue(),
                    isUtf8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1));
        }
    }

    /**
     * "[[hours:]minutes:]seconds[.fraction]" as the format documents the length and fade tags.
     *
     * @return 0 when str is null
     */
    public static double timeToSeconds(String str) {
        if (str == null) {
            return 0.0;
        }

        int c = 0;
        long partVal = 0;
        long digit = 1;
        double acc = 0.0;

        for (int x = str.length(); x >= 0; x--) {
            char ch = x < str.length() ? str.charAt(x) : 0;
            if (ch >= '0' && ch <= '9') {
                partVal += (long) (ch - '0') * digit;
                digit *= 10;
            }
            if (ch == '.' || ch == ',') {
                acc = (double) partVal / (double) digit;
                partVal = 0;
                digit = 1;
            } else if (ch == ':') {
                if (c == 0) {
                    acc += partVal;
                } else if (c == 1) {
                    acc += partVal * 60;
                }
                c++;
                partVal = 0;
                digit = 1;
            } else if (x == 0) {
                if (c == 0) {
                    acc += partVal;
                } else if (c == 1) {
                    acc += partVal * 60;
                } else if (c == 2) {
                    acc += partVal * 60 * 60;
                }
                partVal = 0;
                digit = 1;
            }
        }

        return acc;
    }

    private static int readLeInt(byte[] b, int offset) {
        return (b[offset] & 0xff) | ((b[offset + 1] & 0xff) << 8)
                | ((b[offset + 2] & 0xff) << 16) | ((b[offset + 3] & 0xff) << 24);
    }
}
