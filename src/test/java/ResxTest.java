/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import vavi.util.Debug;
import vavi.util.StringUtil;
import vavix.util.screenscrape.annotation.InputHandler;
import vavix.util.screenscrape.annotation.Target;
import vavix.util.screenscrape.annotation.WebScraper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * resx -> properties.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-04 nsano initial version <br>
 */
public class ResxTest {

    @WebScraper(
            url = "classpath:mdplayer/properties/Resources.resx",
//            input = MyInput.class,
            value="/root/data")
    public static class Resx {
        @Target("/data/@name")
        String name;
        @Target("/data/value/text()")
        String value;
        @Override public String toString() {
            return name + "=" + value.replace("\n", "\\\n");
        }
    }

    // TODO Files.walk , frm.resx -> properties
    static class MyInput implements InputHandler<Reader> {
        @Override
        public Reader getInput(String... args) throws IOException {
            return null;
        }
    }

    /**
     *
     */
    public static void main(String[] args) throws Exception {
        WebScraper.Util.foreach(Resx.class, System.err::println);
    }

    /**
     * fuckin' intellij won't support test classpass
     * @see "https://youtrack.jetbrains.com/issue/IDEA-90676"
     */
    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test() throws Exception {
        main(null);
    }

    @Test
    @Disabled
    void testF() throws Exception {
        byte a = 100;
        byte b = 100;
        a += b;
        byte c = a;
        assertEquals((byte) 200, c);

        a = (byte) 128;
        b = (byte) 128;
        a += b;
        c = a;
        assertEquals((byte) 256, c);

        a = (byte) 128;
        b = (byte) 128;
        c = (byte) (a + b);
        assertEquals(c, (byte) ((a & 0xff) + (b & 0xff)));

        int i1 = 0xf000_e000;
        short s1 = (short) i1;
        short s2 = (short) (i1 >> 16);
        assertEquals((short) 0xe000, s1);
        assertEquals((short) 0xf000, s2);

        byte b1 = 'x';
        assertTrue('x' == b1);

        char[] c1 = {'s', '\0'};
Debug.println(StringUtil.getDump(new String(c1)));

        for (int i = 0; i < 256; i++) {
            byte b0 = 127;
            byte b3 = (byte) i;
            byte b2 = (byte) ((b0 & 0xff) + i);
            b0 += b3;
            assertEquals(b2, b0);
        }

        for (byte i = 0; i < (byte) 256; i++) {
            byte b0 = 127;
            byte b3 = i;
            byte b2 = (byte) ((b0 & 0xff) + i);
            b0 += b3;
            assertEquals(b2, b0);
        }
    }
}
