/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Test001.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-06-03 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class Test001 {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String file;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
Debug.println("volume: " + volume);
    }

    /**
     * @param args 0: audio file
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];
Debug.println("filename: " + filename);
        FileFormat format = FileFormat.getFileFormat(filename);
Debug.println(format.getClass().getName());
        var r = format.load(null, filename);
        BasePlugin plugin = (BasePlugin) format.getPlugin();
        plugin.setVGMBuffer(format, r.getItem1(), filename, null, 0, 0, r.getItem2());
Debug.println(plugin.getClass().getName());
        plugin.play(filename, format);
    }

    @Test
    void test1() throws Exception {
        main(new String[] {file});

        CountDownLatch cdl = new CountDownLatch(1);
        cdl.await();
    }
}
