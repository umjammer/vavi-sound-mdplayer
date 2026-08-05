/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.common;

import java.nio.charset.Charset;

import vavi.sound.visualizer.fmdsp.Pc98Gaiji;


/**
 * EncodingUtils.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-05 nsano initial version <br>
 */
public class EncodingUtils {

    /**
     * Decodes a memo, which is Shift_JIS plus what {@link #decodeFmpHalfWidth} handles. A character
     * MS932 has no mapping for becomes a private use character carrying its JIS code, which
     * the visualizer draws straight out of the PC-98 font ROM.
     */
    public static String decodePc98ShiftJis(byte[] buf, int start, int end) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < end) {
            int b1 = buf[i] & 0xff;
            if (b1 == 0) break;

            if (b1 == 0x85 && i + 1 < end) {
                String half = decodeFmpHalfWidth(buf[i + 1] & 0xff);
                if (half != null) {
                    sb.append(half);
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
                    byte[] sjis = {(byte)b1, (byte)b2};
                    String s = new String(sjis, Charset.forName("MS932"));
                    if (!s.isEmpty() && s.charAt(0) != '\uFFFD') {
                        sb.append(s);
                    } else {
                        int j1 = (b1 < 0xe0 ? b1 - 0x81 : b1 - 0xc1) * 2 + 0x21;
                        int s2 = b2;
                        if (s2 >= 0x9f) {
                            j1++;
                            s2 -= 0x9f;
                        } else {
                            s2 -= 0x40;
                            if (s2 >= 0x3f) s2--;
                        }
                        int j2 = s2 + 0x21;
                        char pua = Pc98Gaiji.puaOf((j1 << 8) | j2);
                        // outside the maker's rows there is nothing to draw it from
                        sb.append(pua != 0 ? pua : '〓');
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

    /**
     * The half width characters FMP's editor writes with the double byte lead 0x85, the two
     * JIS X 0208 rows (9 and 10) the standard leaves unassigned and MS932 therefore cannot decode:
     * <li>row 9 is ASCII, cell 1 being {@code '!'} - so {@code 0x85 0x76} is a {@code 'W'}</li>
     * <li>row 10 is JIS X 0201, cell 1 being {@code 0xa1} - so its 63 cells are the same half
     *     width katakana a memo can also spell with single bytes</li>
     * <li>row 10 continues with 25 precomposed voiced kana in cells 70 to 94, which have no half
     *     width form of their own and so come back as a base kana plus its sound mark</li>
     * Cells 64 to 69 belong to neither set.
     *
     * @param b2 the byte after the 0x85
     * @return null when {@code b2} addresses no half width character, leaving it to the caller
     */
    private static String decodeFmpHalfWidth(int b2) {
        if (b2 < 0x40 || b2 > 0xfc || b2 == 0x7f) {
            return null;
        }
        if (b2 <= 0x9e) { // row 9
            int cell = b2 - 0x40 + (b2 < 0x7f ? 1 : 0);
            return String.valueOf((char) (0x20 + cell));
        }
        int cell = b2 - 0x9f + 1; // row 10
        if (cell <= 63) {
            return String.valueOf((char) (0xff61 + cell - 1));
        }
        return cell >= 70 ? VOICED_KANA[cell - 70] : null;
    }

    /** Cells 70 to 94 of the row {@link #decodeFmpHalfWidth} describes. */
    private static final String[] VOICED_KANA = {
            "ｶﾞ", "ｷﾞ", "ｸﾞ", "ｹﾞ", "ｺﾞ",
            "ｻﾞ", "ｼﾞ", "ｽﾞ", "ｾﾞ", "ｿﾞ",
            "ﾀﾞ", "ﾁﾞ", "ﾂﾞ", "ﾃﾞ", "ﾄﾞ",
            "ﾊﾞ", "ﾊﾟ", "ﾋﾞ", "ﾋﾟ", "ﾌﾞ", "ﾌﾟ", "ﾍﾞ", "ﾍﾟ", "ﾎﾞ", "ﾎﾟ"
    };

    public static String normalizeKanji(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '－', '−', '‐', '‑', '‒', '–', '—' -> sb.append('ー');
                case '～' -> sb.append('〜');
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
