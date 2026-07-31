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

public class InfoCheck {
    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void check() throws Exception {
        String file = System.getProperty("probe.file", "/Users/nsano/Public/np2/PMD/SC3/GAY_07.M");
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Files.newInputStream(Path.of(file)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        BaseDriver driver = plugin.getDriver();
        short[] buffer = new short[2048];
        for (int i = 0; i < 500; i++) driver.render(buffer, 0, 1024);
        var chip = plugin.chipRegister.chip(mdplayer.chips.Ym2608Chip.class);
        Map<String, Object> info = new TreeMap<>(chip.getInfo(0));
        info.forEach((k, v) -> {
            if (k.contains("lfo") || k.contains("sensitivity") || k.contains("amOn"))
                System.err.printf("  %-34s %s%n", k, v);
        });
    }
}
