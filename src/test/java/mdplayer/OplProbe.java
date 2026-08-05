/*
 * scratch: what the OPL2 chip reports for a song whose rows never appear
 */

package mdplayer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class OplProbe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        String file = System.getProperty("probe.file");
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Files.newInputStream(Path.of(file)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        BaseDriver driver = plugin.getDriver();
        short[] buffer = new short[2048];
        long peak = 0;
        for (int i = 0; i < 400; i++) {
            driver.render(buffer, 0, 1024);
            for (short s : buffer) peak = Math.max(peak, Math.abs(s));
        }
        System.err.printf("pcm peak: %d%n", peak);

        var chip = plugin.chipRegister.chip(mdplayer.chips.Ym3812Chip.class);
        System.err.printf("chip: %s%n", chip);
        Map<String, Object> info = new TreeMap<>(chip.getInfo(0));
        System.err.printf("info keys: %d%n", info.size());
        info.forEach((k, v) -> System.err.printf("  %-28s %s%n",
                k, v instanceof int[] a ? "int[" + a.length + "]" : v));
    }
}
