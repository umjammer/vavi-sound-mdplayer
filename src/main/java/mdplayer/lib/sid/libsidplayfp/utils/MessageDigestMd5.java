/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.sid.libsidplayfp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * The md5 the tune fingerprints are made of, done by the JDK.
 * <p>
 * The original picks between libgcrypt and an md5 of its own at build time; the libgcrypt one is
 * a binding to a C library and could not come over, so what was ported of it hashes nothing at
 * all. There is a {@link MessageDigest} here for exactly this.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-01 nsano initial version <br>
 */
public class MessageDigestMd5 implements IMd5 {

    private final MessageDigest md5;

    /** the digest, once {@link #finish} has been called for it */
    private byte[] digest;

    public MessageDigestMd5() {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void append(byte[] data, int nbytes) {
        md5.update(data, 0, nbytes);
    }

    @Override
    public void finish() {
        digest = md5.digest();
    }

    @Override
    public void reset() {
        md5.reset();
        digest = null;
    }

    @Override
    public byte[] getDigest() {
        return digest;
    }
}
