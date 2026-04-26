/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import mdplayer.Audio;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import mdplayer.plugin.BasePlugin.Compilable;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * FMP.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-25 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String fmp;

    @Property(name = "mdplayer.fmp.dir")
    String fmpDir;
    @Property(name = "mdplayer.fmp.pvi")
    String fmpPvi;

    @Property(name= "fmp.dir")
    String dir;

    @Property(name= "fmp.ext")
    String ext;

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 1000 * 1000 : 10 * 1000;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);

            // fmp
            System.setProperty("mdplayer.fmp.dir", fmpDir);
            System.setProperty("mdplayer.fmp.pvi", fmpPvi);
        }

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
Debug.println("volume: " + volume + ", player.volume: " + System.getProperty("mdplayer.volume") + ", cwd: " + System.getProperty("user.dir") + ", time: " + time);
Debug.println("settings\n" +
 "mdplayer.fmp.dir: " + System.getProperty("mdplayer.fmp.dir") + "\n" +
 "mdplayer.fmp.pvi: " + System.getProperty("mdplayer.fmp.pvi"));
    }

    private final Audio audio = Audio.getInstance();

    @Test
    @DisplayName("compile mml")
    void test() throws Exception {
Debug.println("filename: " + fmp);
        FileFormat format = FileFormat.getFileFormat(fmp);
        format.load(Files.newInputStream(Path.of(fmp)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", fmp));
        audio.init(plugin);
        audio.play();
    }

    /**
     * @param dir separated by ';'
     * @param ext separated by ','
     */
    static List<Path> listFilesUnderDirFilteredByExt(String dir, String ext) {
Debug.println("dir: " + dir);
Debug.println("ext: " + ext);
        Predicate<Path> x = p -> Arrays.stream(ext.split(",")).anyMatch(e -> p.getFileName().toString().toUpperCase().endsWith(e));
        return Arrays.stream(dir.split(File.pathSeparator)).flatMap(d -> {
            try {
                return Files.walk(Paths.get(d)).filter(x);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).toList();
    }

    @Test
    @Disabled("under construction")
    @DisplayName("compile dir")
    void test1() throws Exception {
        listFilesUnderDirFilteredByExt(dir, ext).forEach(p -> {
            try {
                FileFormat format = FileFormat.getFileFormat(p.toString());
                format.load(Files.newInputStream(p), null);
                var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
                plugin.setParams(format, Map.of("fileName", p.toString()));
                ((Compilable) plugin).compile();
            } catch (Exception e) {
                Debug.println(e.toString());
            }
        });
    }
}
