package mdplayer.driver.sid.libsidplayfp.utils.md5;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TestMd5.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-06-17 nsano initial version <br>
 */
class TestMd5 {

    @Test
    void test1() {
        final String[] strTest = new String[] {
                "",
                "a",
                "abc",
                "message digest",
                "abcdefghijklmnopqrstuvwxyz",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
        };
        final String[] expected = new String[] {
                "d41d8cd98f00b204e9800998ecf8427e",
                "0cc175b9c0f1b6a831c399e269772661",
                "900150983cd24fb0d6963f7d28e17f72",
                "f96b697d7cb7938d525a2f31aaf161d0",
                "c3fcd3d76192e4007dfb496cca67e13b",
                "d174ab98d277d9f5a5611c2c9f419d9f",
                "57edf4a22be3c955ac49da2e2107b67a"
        };
        byte[][] test = new byte[7][];
        for (int i = 0; i < 7; i++) test[i] = strTest[i].getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < 7; ++i) {
            MD5 md5 = new MD5();
            md5.append(test[i], test[i].length);
            md5.finish();
            System.err.printf("MD5 (\"" + test[i] + "\") = ");
            StringBuilder sb = new StringBuilder();
            for (int di = 0; di < 16; ++di) {
                String p = String.format("%02X", (int) (md5.getDigest()[di]));
                System.err.print(p);
                sb.append(p);
            }
            System.err.println();
            assertEquals(expected[i], sb.toString());
        }
    }
}
