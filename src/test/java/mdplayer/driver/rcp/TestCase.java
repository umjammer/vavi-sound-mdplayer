/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.rcp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import mdplayer.Audio;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.FileFormat;
import mdplayer.driver.BasePlugin;
import mdplayer.lib.rcp.RCS;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * RCP.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-05-05 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String rcp;

    @Property
    String rcs;

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 1000 * 1000 : 10 * 1000;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
Debug.println("volume: " + volume + ", player.volume: " + System.getProperty("mdplayer.volume") + ", cwd: " + System.getProperty("user.dir") + ", time: " + time);
    }

    private final Audio audio = Audio.getInstance();

    @Test
    @DisplayName("play rcp")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test() throws Exception {
Debug.println("filename: " + rcp);
        FileFormat format = FileFormat.getFileFormat(rcp);
        format.load(Files.newInputStream(Path.of(rcp)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", rcp));
        audio.init(plugin);
        audio.play();
    }

    @Test
    @DisplayName("play rcs")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
Debug.println("filename: " + rcs);
        FileFormat format = FileFormat.getFileFormat(rcs);
        format.load(Files.newInputStream(Path.of(rcs)), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", rcs));
        audio.init(plugin);
        audio.play();
    }

    /**
     * A .RCS is a PCM8 bank: the name of the .RCP that sequences it, a 127 entry table of sample
     * frequency/offset/length, and the samples themselves, all from {@code 0xc0} on. The offsets
     * are big endian and unsigned, and are relative to {@code 0xc0} -- so the last entry has to end
     * exactly at the end of the file, which is what this checks. Reading them as signed Java bytes,
     * or filling only entry 0 (the table lives behind a one-element out-parameter wrapper), both
     * pass a smoke test and produce silence.
     */
    @Test
    @DisplayName("rcs pcm table")
    void test2() throws Exception {
        byte[] buf = Files.readAllBytes(Path.of(rcs));

        RCS.PcmInfo[][] pcmInfos = new RCS.PcmInfo[1][];
        byte[][] pcmData = new byte[1][];
        String[] rcpFilename = new String[1];
        byte[][] rcpBuf = new byte[1][];
        assertTrue(RCS.getRCSInfo(rcs, null, buf, pcmInfos, pcmData, rcpFilename, rcpBuf));

        assertEquals(127, pcmInfos[0].length);
        assertEquals(buf.length - 0xc0, pcmData[0].length);
        assertFalse(rcpFilename[0].isEmpty(), "the .RCS names the .RCP that sequences it");

        int end = 0;
        int used = 0;
        for (RCS.PcmInfo info : pcmInfos[0]) {
            if (info.length == 0) continue;
            used++;
            assertTrue(info.ptr >= 0 && info.ptr + info.length <= pcmData[0].length,
                    "sample %#x..%#x is outside the %#x bytes of pcm data"
                            .formatted(info.ptr, info.ptr + info.length, pcmData[0].length));
            end = Math.max(end, info.ptr + info.length);
        }
        assertTrue(used > 0, "no samples parsed out of the bank");
        assertEquals(pcmData[0].length, end, "the last sample must end at the end of the bank");
    }
}
