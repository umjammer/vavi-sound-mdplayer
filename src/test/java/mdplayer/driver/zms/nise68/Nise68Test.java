/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.zms.nise68;

import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.Common;
import vavi.util.Debug;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Nise68Test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-21 nsano initial version <br>
 */
public class Nise68Test {

    final String testSuite = "Test_Suite.bin";

    @Test
    @Disabled
    void test1() throws Exception {
        Memory68 mem = new Memory68(16 * 1024 * 1024);
        Register68 reg = new Register68();
        NiseM68 cpu = new NiseM68(mem, reg);

        byte[] bytes = Files.readAllBytes(Path.of("tmp", testSuite));
        System.arraycopy(bytes, 0, mem.mem, 0x0001_2000, bytes.length);
        while (true) {
            try {
                cpu.stepExecute();
            } catch (Exception e) {
                Debug.printf("%08x: %s".formatted(reg.pc, e.toString()));
Debug.printStackTrace(e);
            }
        }
    }
}
