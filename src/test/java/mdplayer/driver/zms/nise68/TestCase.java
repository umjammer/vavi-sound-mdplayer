/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.zms.nise68;

import java.nio.charset.Charset;

import vavi.util.Debug;
import vavi.util.StringUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-01-01 nsano initial version <br>
 */
public class TestCase {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        Charset sjis = Charset.forName("shift_jis");
        byte[] msg = new byte[] {
            0x5A, 0x2D, 0x4D, 0x55, 0x53, 0x49, 0x43, 0x20, 0x4D, 0x4D, 0x4C, 0x20, 0x43, 0x4F, 0x4D, 0x50,
            0x49, 0x4C, 0x45, 0x52, 0x20, (byte) 0xF3, 0x49, (byte) 0xF3, 0x4E, (byte) 0xF3, 0x54, (byte) 0xF3, 0x45, (byte) 0xF3, 0x47, (byte) 0xF3,
            0x52, (byte) 0xF3, 0x41, (byte) 0xF3, 0x4C, 0x20, (byte) 0xF3, 0x56, (byte) 0xF3, 0x45, (byte) 0xF3, 0x52, (byte) 0xF3, 0x53, (byte) 0xF3, 0x49,
            (byte) 0xF3, 0x4F, (byte) 0xF3, 0x4E, 0x20, (byte) 0xF3, 0x33, (byte) 0xF3, 0x2E, (byte) 0xF3, 0x30, (byte) 0xF3, 0x32, (byte) 0xF3, 0x43, 0x20,
            0x28, 0x43, 0x29, 0x20, 0x31, 0x39, 0x39, 0x35, 0x2C, 0x39, 0x38, 0x20, 0x5A, 0x45, 0x4E, 0x4A,
            0x49, 0x20, 0x53, 0x4F, 0x46, 0x54, 0x0D, 0x0A
        };
        String text = new String(msg, sjis);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < msg.length; i++) {
            int b = msg[i] & 0xFF;
            if (b == 0xF3) { // half size height character will be displayed
                if (i + 1 < msg.length) {
                    int next = msg[++i] & 0xFF;
                    String base = new String(new byte[] {(byte) next}, sjis);
                    out.append(base);
                }
            } else {
                out.append(new String(new byte[] {(byte) b}, sjis));
            }
        }
Debug.println(out + "\n" + StringUtil.getDump(msg));
    }
}
