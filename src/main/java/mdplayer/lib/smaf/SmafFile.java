/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.smaf;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A SMAF (Synthetic music Mobile Application Format, ".mmf") file, read far enough to say what
 * it is and what it is called. Playing it is mmftoolc.exe's job, see {@link MmfToolPlayer}.
 * <p>
 * The file is "MMMD" and a size, then chunks. What a player wants from here is in two places
 * depending on the file's age: MA-1/MA-2 files put comma separated {@code XX:value} pairs in the
 * contents info chunk, MA-3 and later put length-prefixed entries in {@code OPDA/Dch*}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-10 nsano initial version <br>
 */
public class SmafFile {

    /** what the fourth byte of the first MTR chunk id says the file needs to play */
    public enum Format {
        MA1("MA-1"), MA2("MA-2"), MA3("MA-3"), MA5("MA-5"), MA7("MA-7"), UTA2("Uta2"), UTA3("Uta3"), UNKNOWN("SMAF");

        public final String label;

        Format(String label) {
            this.label = label;
        }
    }

    private Format format = Format.UNKNOWN;

    /** the two letter tags the file carries, e.g. "ST" (song title), "CR" (copyright) */
    private final Map<String, String> options = new LinkedHashMap<>();

    public Format getFormat() {
        return format;
    }

    /** @return null when the file does not carry that tag */
    public String get(String tag) {
        return options.get(tag);
    }

    public Map<String, String> getOptions() {
        return options;
    }

    /** the song's title, or null */
    public String getTitle() {
        return get("ST");
    }

    /** who wrote it - the composer tag if there is one, otherwise the artist */
    public String getComposer() {
        String sw = get("SW");
        return sw != null ? sw : get("AT");
    }

    public String getCopyright() {
        return get("CR");
    }

    /** the file's own note about itself, e.g. "SMAF MA-5 Sample Data" */
    public String getComment() {
        return get("CA");
    }

    /** does this look like a SMAF file at all? */
    public static boolean isSmaf(byte[] b) {
        return b != null && b.length > 8 && b[0] == 'M' && b[1] == 'M' && b[2] == 'M' && b[3] == 'D';
    }

    public static SmafFile decode(byte[] b) throws IOException {
        if (!isSmaf(b)) {
            throw new IOException("not a SMAF file");
        }
        SmafFile file = new SmafFile();
        int size = readInt(b, 4);
        int end = Math.min(b.length, 8 + size);

        for (int p = 8; p + 8 <= end; ) {
            String id = new String(b, p, 4, StandardCharsets.ISO_8859_1);
            int chunkSize = readInt(b, p + 4);
            int data = p + 8;
            if (chunkSize < 0 || data + chunkSize > b.length) {
                // a truncated or mis-sized chunk: take what is readable and stop
                chunkSize = b.length - data;
                if (chunkSize < 0) break;
            }
            switch (id.substring(0, 3)) {
                case "CNT" -> file.readContentsInfo(b, data, chunkSize);
                case "OPD" -> file.readOptionalData(b, data, chunkSize);
                case "MTR" -> {
                    if (file.format == Format.UNKNOWN) {
                        file.format = trackFormat(b[p + 3] & 0xff);
                    }
                }
                case "ATR" -> {
                    if (file.format == Format.UNKNOWN) {
                        file.format = Format.UTA2;
                    }
                }
                default -> {
                }
            }
            p = data + chunkSize;
        }
        return file;
    }

    /** MTR0 is MA-1, MTR1..4 MA-2, and so on up */
    private static Format trackFormat(int version) {
        return switch (version) {
            case 0x00 -> Format.MA1;
            case 0x01, 0x02, 0x03, 0x04 -> Format.MA2;
            case 0x05 -> Format.MA3;
            case 0x06 -> Format.MA5;
            case 0x07 -> Format.MA7;
            default -> Format.UNKNOWN;
        };
    }

    /**
     * The contents info chunk: five bytes about the file, then - in MA-1 and MA-2 files - the
     * options as {@code XX:value} separated by commas.
     */
    private void readContentsInfo(byte[] b, int offset, int length) {
        if (length < 5) {
            return;
        }
        Charset charset = charsetOf(b[offset + 2] & 0xff);
        if (length <= 5) {
            return; // MA-3 and later say it all in OPDA instead
        }
        String s = new String(b, offset + 5, length - 5, charset);
        for (String entry : s.split(",")) {
            int colon = entry.indexOf(':');
            if (colon == 2) {
                options.putIfAbsent(entry.substring(0, 2), entry.substring(3));
            }
        }
    }

    /** The optional data chunk: {@code Dch<code>} sub-chunks of tag, 2 byte length, value. */
    private void readOptionalData(byte[] b, int offset, int length) {
        int end = offset + length;
        for (int p = offset; p + 8 <= end; ) {
            String id = new String(b, p, 3, StandardCharsets.ISO_8859_1);
            int code = b[p + 3] & 0xff;
            int size = readInt(b, p + 4);
            int data = p + 8;
            if (size < 0 || data + size > end) {
                break;
            }
            if (id.equals("Dch") && code != 0xff) { // 0xff is the binary one, nothing to read
                readDataEntries(b, data, size, charsetOf(code));
            }
            p = data + size;
        }
    }

    private void readDataEntries(byte[] b, int offset, int length, Charset charset) {
        int end = offset + length;
        for (int p = offset; p + 4 <= end; ) {
            String tag = new String(b, p, 2, StandardCharsets.ISO_8859_1);
            int size = ((b[p + 2] & 0xff) << 8) | (b[p + 3] & 0xff);
            int data = p + 4;
            if (data + size > end) {
                break;
            }
            options.putIfAbsent(tag, new String(b, data, size, charset));
            p = data + size;
        }
    }

    /** the file's character code; anything unfamiliar is read as Shift_JIS, which most of it is */
    private static Charset charsetOf(int code) {
        try {
            return switch (code) {
                case 0x01 -> StandardCharsets.ISO_8859_1;
                case 0x02 -> StandardCharsets.UTF_16BE;
                case 0x03 -> StandardCharsets.UTF_8;
                default -> Charset.forName("Shift_JIS");
            };
        } catch (Exception e) {
            return StandardCharsets.ISO_8859_1;
        }
    }

    private static int readInt(byte[] b, int offset) {
        return ((b[offset] & 0xff) << 24) | ((b[offset + 1] & 0xff) << 16)
                | ((b[offset + 2] & 0xff) << 8) | (b[offset + 3] & 0xff);
    }
}
