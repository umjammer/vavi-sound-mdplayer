/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.mxdrv;

import java.util.Arrays;

import vavi.util.ByteUtil;


/**
 * LZX compressed MDX data.
 * <p>
 * {@code LZX.X} is an X68000 packer that replaces what it compresses with a 68000 stub and its
 * own compressed stream. Run on an MDX it leaves the text header alone - the title, the
 * {@code 0x1a} and the PDX file name still read back the way every MDX reader expects - and packs
 * only the body, so a compressed song looks like an ordinary one until a player follows the part
 * offsets at the head of the body and lands in 68000 code. Over a thousand of the MDXs in
 * circulation are packed this way (Gradius III's {@code G3_ST7.MDX} among them), and nothing here
 * can play one without expanding it first.
 * <p>
 * The stub the packer prepends is what documents the format, so the layout below is named after
 * what it does with each field:
 * <pre>
 *  +0x00  bra.s to the bootstrap                (also where an MDX keeps its voice data offset)
 *  +0x04  "LZX 0.32" / "LZX 0.42"               (overwritten with the load address at run time)
 *  +0x12  the size of the expanded body
 *  +0x1e  the size of the relocation table      (zero for data, non-zero for an executable)
 *  +0x22  where the packed stream is moved to before it is expanded over itself
 *  +0x26  the bootstrap, ending in the lea that points at the stream
 * </pre>
 * The bootstrap picks its source up with {@code lea (d8,pc,a6.l),a6}, and the displacement in that
 * instruction is the only thing that moves between the two versions in the wild - 0x8c into the
 * body for 0.32, 0xa4 for 0.42 - so {@link #streamOffset} reads it out of the stub rather than
 * keeping a table of versions.
 * <p>
 * The stream itself is LZSS over a bit stream read most significant bit first, with the bytes of a
 * literal and the operands of a match taken whole from between the bits:
 * <pre>
 *  1                       one literal byte follows
 *  0 0 nn  &lt;byte&gt;          match, distance 0x100 - byte (1..256), length nn + 2 (2..5)
 *  0 1     &lt;word&gt;          match, distance -(0xffff0000 | word &gt;&gt; 3) (1..8192),
 *                          length (word &amp; 7) + 2 (3..9), or when the low three bits are 0:
 *          &lt;byte&gt;          length byte + 1 (2..256), and 0 ends the stream
 * </pre>
 * The stub zero fills whatever is left of the expanded body when the stream ends short of the size
 * at +0x12, which songs do rely on, so {@link #expand} sizes its output by that field and not by
 * what the stream produced.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-26 nsano initial version <br>
 */
final class Lzx {

    private Lzx() {
    }

    /** what the packer leaves where the first part offset of an MDX body would be */
    private static final byte[] SIGNATURE = {'L', 'Z', 'X', ' '};

    /** the signature sits one longword into the body */
    private static final int SIGNATURE_OFFSET = 4;

    /** the size of the expanded body, as a big endian longword from the head of the body */
    private static final int EXPANDED_SIZE = 0x12;

    /** {@code lea (d8,pc,a6.l),a6}, the instruction the bootstrap loads its source pointer with */
    private static final byte[] LEA_SOURCE = {0x4d, (byte) 0xfb, (byte) 0xe8, 0x38};

    /** the displacement {@link #LEA_SOURCE} carries, counted from its extension word */
    private static final int LEA_DISPLACEMENT = 0x38;

    /** as far into the body as the bootstrap that carries {@link #LEA_SOURCE} can reach */
    private static final int BOOTSTRAP_LIMIT = 0x100;

    /**
     * Where the body of an MDX starts, which is past the title, the {@code 0x1a} that ends it and
     * the null terminated name of the PDX the song wants.
     *
     * @param mdx a whole MDX file
     * @throws IndexOutOfBoundsException if the file ends before the header does
     */
    static int bodyOffset(byte[] mdx) {
        int p = 0;
        int c;
        do {
            c = mdx[p++] & 0xff;
        } while (c != 0x0d && c != 0x0a);
        if (c != 0x0d) {
            while (mdx[p++] != 0x0d) ;
        }
        while (mdx[p++] != 0x1a) ;
        while (mdx[p] != 0x00) p++;
        return p + 1;
    }

    /**
     * Whether the body of this MDX is a packed stream behind {@code LZX.X}'s stub rather than
     * sequence data.
     *
     * @param mdx a whole MDX file
     */
    static boolean isCompressed(byte[] mdx) {
        try {
            int body = bodyOffset(mdx);
            int p = body + SIGNATURE_OFFSET;
            return p + SIGNATURE.length <= mdx.length
                    && Arrays.equals(mdx, p, p + SIGNATURE.length, SIGNATURE, 0, SIGNATURE.length);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Expands an MDX packed by {@code LZX.X} into the MDX it was made from, header and all.
     *
     * @param mdx a whole MDX file
     * @return the same array when the body is not packed, a new file otherwise
     * @throws IllegalArgumentException if the packed stream is damaged
     */
    static byte[] expand(byte[] mdx) {
        if (!isCompressed(mdx)) return mdx;

        int body = bodyOffset(mdx);
        byte[] expanded = expandBody(mdx, body);
        byte[] ret = new byte[body + expanded.length];
        System.arraycopy(mdx, 0, ret, 0, body);
        System.arraycopy(expanded, 0, ret, body, expanded.length);
        return ret;
    }

    /**
     * Where the packed stream starts, read out of the {@code lea} the bootstrap points its source
     * pointer with so that both versions of the packer are covered by the one rule.
     *
     * @param mdx a whole MDX file
     * @param body where its body starts
     * @throws IllegalArgumentException if the bootstrap is not the one this knows
     */
    private static int streamOffset(byte[] mdx, int body) {
        int limit = Math.min(mdx.length - LEA_SOURCE.length, body + BOOTSTRAP_LIMIT);
        for (int p = body; p <= limit; p++) {
            if (Arrays.equals(mdx, p, p + LEA_SOURCE.length, LEA_SOURCE, 0, LEA_SOURCE.length)) {
                return p + 2 + LEA_DISPLACEMENT - body;
            }
        }
        throw new IllegalArgumentException("Not an LZX stub: it does not point at a packed stream.");
    }

    /**
     * @param mdx a whole MDX file
     * @param body where its body starts
     * @throws IllegalArgumentException if the packed stream is damaged
     */
    private static byte[] expandBody(byte[] mdx, int body) {
        int size = ByteUtil.readBeInt(mdx, body + EXPANDED_SIZE);
        if (size <= 0) {
            throw new IllegalArgumentException("Not an LZX stub: the expanded size is %d.".formatted(size));
        }

        byte[] out = new byte[size];
        Stream in = new Stream(mdx, body + streamOffset(mdx, body));
        int o = 0;
        try {
            while (true) {
                if (in.getBit() != 0) {
                    out[o++] = (byte) in.getByte();
                    continue;
                }

                int distance, length;
                if (in.getBit() == 0) {
                    int n = in.getBit() << 1;
                    n |= in.getBit();
                    length = n + 2;
                    distance = 0x100 - in.getByte();
                } else {
                    int word = in.getByte() << 8 | in.getByte();
                    distance = -((0xffff_0000 | word) >> 3);
                    int n = word & 7;
                    if (n != 0) {
                        length = n + 2;
                    } else {
                        int extra = in.getByte();
                        if (extra == 0) break;
                        length = extra + 1;
                    }
                }

                if (o - distance < 0) {
                    throw new IllegalArgumentException("Damaged LZX stream: a match at %d reaches back %d.".formatted(o, distance));
                }
                for (int i = 0; i < length; i++) {
                    out[o] = out[o - distance];
                    o++;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Damaged LZX stream: it does not expand to the %d bytes it declares.".formatted(size), e);
        }
        // what the stream leaves short of the declared size the stub zero fills, and so does the
        // array this wrote into
        return out;
    }

    /** The bit stream the stub reads its literals and matches out of, most significant bit first. */
    private static final class Stream {

        private final byte[] buf;
        private int p;
        /** the byte the bits are being shifted out of the top of */
        private int bits;
        /** how many of them are left in it */
        private int count;

        Stream(byte[] buf, int p) {
            this.buf = buf;
            this.p = p;
            this.bits = getByte();
            this.count = 8;
        }

        int getByte() {
            return buf[p++] & 0xff;
        }

        int getBit() {
            if (--count < 0) {
                bits = getByte();
                count = 7;
            }
            int bit = (bits >> 7) & 1;
            bits = (bits << 1) & 0xff;
            return bit;
        }
    }
}
