/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.ay;

import java.io.BufferedInputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import konamiman.z80.Z80Processor;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdplayer.lib.ay.AY;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * What the Z80 is doing while an AY file plays: the interrupt mode it settled on, whether it is
 * waiting on a HALT, and the addresses it spends its time at. That tells a tune that never hears
 * the 50Hz interrupt from one that hears it and writes nothing. Not a regression test.
 */
class AyIntProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "diag")
    void probe() throws Exception {
        String file = System.getProperty("diag.file");
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "songNo", Integer.parseInt(System.getProperty("diag.song", "0"))));
        plugin.prepare();
        plugin.stopped = false; plugin.paused = false; plugin.fadeout = false;
        BaseDriver driver = plugin.getDriver();

        Field fAy = AyDriver.class.getDeclaredField("ay"); fAy.setAccessible(true);
        AY ay = (AY) fAy.get(driver);
        Field fZ = AY.class.getDeclaredField("z80"); fZ.setAccessible(true);
        Z80Processor z80 = (Z80Processor) fZ.get(ay);

        int rate = Setting.getInstance().getOutputDevice().getSampleRate();
        short[] buffer = new short[1024];
        Map<Integer, Integer> pcs = new HashMap<>();
        for (int s = 0; s < 8; s++) {
            for (int n = 0; n < rate; n += buffer.length / 2) {
                driver.render(buffer, 0, buffer.length);
                pcs.merge(z80.getRegisters().getPC() & 0xffff, 1, Integer::sum);
            }
            System.err.printf("t=%ds PC=%04x IM=%d IFF1=%d I=%02x halted=%s state=%s frames=%d%n",
                    s + 1, z80.getRegisters().getPC() & 0xffff, z80.getInterruptMode(),
                    z80.getRegisters().getIFF1().intValue(), z80.getRegisters().getI() & 0xff,
                    z80.isHalted(), z80.getState(), ay.frames);
        }
        pcs.entrySet().stream().sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).limit(10)
                .forEach(e -> System.err.printf("  PC %04x x%d%n", e.getKey(), e.getValue()));
    }
}
