/*
 * Auto-generated from 98fmplayer fmdsp_sprites.h and font data headers.
 * Do not edit by hand; regenerate with scratchpad/extract.py.
 */

package vavi.sound.visualizer.fmdsp;

import java.io.IOException;
import java.io.InputStream;

/**
 * Pixel sprite data and bitmap fonts lifted verbatim from the original
 * {@code fmdsp} C sources, so the Java renderer is dot-by-dot identical.
 * The raw bytes live in the {@code fmdsp.dat} resource; multi-dimensional C
 * arrays are stored flat (row-major) and indexed with per-element stride
 * constants in {@link FmDspVisualizer}.
 */
final class FmDspSprites {

    private FmDspSprites() {}

    static final byte[] s_num;
    static final byte[] s_num_colon;
    static final byte[] s_num_bar;
    static final byte[] s_key_bg;
    static final byte[] s_key_left;
    static final byte[] s_key_right;
    static final byte[] s_key_mask;
    static final byte[] s_bar_l;
    static final byte[] s_bar;
    static final byte[] s_playing;
    static final byte[] s_filebar;
    static final byte[] s_filebar_tri;
    static final byte[] s_dt_sign;
    static final byte[] s_logo_fm;
    static final byte[] s_logo_ds;
    static final byte[] s_logo_p;
    static final byte[] s_circle;
    static final byte[] s_text;
    static final byte[] s_ver;
    static final byte[] s_curl_left;
    static final byte[] s_curl_right;
    static final byte[] s_play;
    static final byte[] s_stop;
    static final byte[] s_pause;
    static final byte[] s_fade;
    static final byte[] s_ff;
    static final byte[] s_rew;
    static final byte[] s_floppy;
    static final byte[] s_panpot;
    static final byte[] s_comment_tri;
    static final byte[] fontdat;
    static final byte[] fmdsp_medium_dat;

    static {
        byte[] d = load();
        s_num = new byte[968];
        System.arraycopy(d, 0, s_num, 0, 968);
        s_num_colon = new byte[176];
        System.arraycopy(d, 968, s_num_colon, 0, 176);
        s_num_bar = new byte[88];
        System.arraycopy(d, 1144, s_num_bar, 0, 88);
        s_key_bg = new byte[595];
        System.arraycopy(d, 1232, s_key_bg, 0, 595);
        s_key_left = new byte[102];
        System.arraycopy(d, 1827, s_key_left, 0, 102);
        s_key_right = new byte[187];
        System.arraycopy(d, 1929, s_key_right, 0, 187);
        s_key_mask = new byte[595];
        System.arraycopy(d, 2116, s_key_mask, 0, 595);
        s_bar_l = new byte[56];
        System.arraycopy(d, 2711, s_bar_l, 0, 56);
        s_bar = new byte[8];
        System.arraycopy(d, 2767, s_bar, 0, 8);
        s_playing = new byte[648];
        System.arraycopy(d, 2775, s_playing, 0, 648);
        s_filebar = new byte[14];
        System.arraycopy(d, 3423, s_filebar, 0, 14);
        s_filebar_tri = new byte[9];
        System.arraycopy(d, 3437, s_filebar_tri, 0, 9);
        s_dt_sign = new byte[27];
        System.arraycopy(d, 3446, s_dt_sign, 0, 27);
        s_logo_fm = new byte[372];
        System.arraycopy(d, 3473, s_logo_fm, 0, 372);
        s_logo_ds = new byte[384];
        System.arraycopy(d, 3845, s_logo_ds, 0, 384);
        s_logo_p = new byte[180];
        System.arraycopy(d, 4229, s_logo_p, 0, 180);
        s_circle = new byte[961];
        System.arraycopy(d, 4409, s_circle, 0, 961);
        s_text = new byte[1155];
        System.arraycopy(d, 5370, s_text, 0, 1155);
        s_ver = new byte[65];
        System.arraycopy(d, 6525, s_ver, 0, 65);
        s_curl_left = new byte[121];
        System.arraycopy(d, 6590, s_curl_left, 0, 121);
        s_curl_right = new byte[121];
        System.arraycopy(d, 6711, s_curl_right, 0, 121);
        s_play = new byte[210];
        System.arraycopy(d, 6832, s_play, 0, 210);
        s_stop = new byte[217];
        System.arraycopy(d, 7042, s_stop, 0, 217);
        s_pause = new byte[259];
        System.arraycopy(d, 7259, s_pause, 0, 259);
        s_fade = new byte[217];
        System.arraycopy(d, 7518, s_fade, 0, 217);
        s_ff = new byte[140];
        System.arraycopy(d, 7735, s_ff, 0, 140);
        s_rew = new byte[182];
        System.arraycopy(d, 7875, s_rew, 0, 182);
        s_floppy = new byte[518];
        System.arraycopy(d, 8057, s_floppy, 0, 518);
        s_panpot = new byte[1350];
        System.arraycopy(d, 8575, s_panpot, 0, 1350);
        s_comment_tri = new byte[56];
        System.arraycopy(d, 9925, s_comment_tri, 0, 56);
        fontdat = new byte[1536];
        System.arraycopy(d, 9981, fontdat, 0, 1536);
        fmdsp_medium_dat = new byte[2048];
        System.arraycopy(d, 11517, fmdsp_medium_dat, 0, 2048);
    }

    private static byte[] load() {
        try (InputStream in = FmDspSprites.class.getResourceAsStream("fmdsp.dat")) {
            if (in == null) throw new IllegalStateException("missing fmdsp.dat");
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
