/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.moonDriver;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.ChipFmDspSource;
import mdplayer.Common;
import mdplayer.LocalProperties;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdsound.chips.YmF278B;
import mdsound.instrument.YmF278BInst;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * The user PCM bank of an unpacked .MDR has to reach the OPL4's sample RAM.
 * <p>
 * An unpacked song keeps its samples in a companion file and the driver loads them into the
 * chip's RAM as it starts; the instruments over 383 are those samples, so a song whose bank never
 * arrives plays them as silence and sounds gutted rather than broken. The driver asks for the
 * bank under the song's name with a lower case ".pcm", which found nothing on a case sensitive
 * file system - TIMESUP.PCM is what is on the disk - so this pins the whole path down to the
 * bytes in the chip, and to the name the fmdsp file bar shows for it.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-01 nsano initial version <br>
 */
class MoonPcmLoadTest {

    /** an unpacked MoonDriver song and its sample bank (they live in the sibling repo) */
    static final Path MDR = Path.of("../vavi-sound-moon/tmp/TIMESUP/TIMESUP.MDR");
    static final Path PCM = Path.of("../vavi-sound-moon/tmp/TIMESUP/TIMESUP.PCM");

    @BeforeAll
    static void setup() throws Exception {
        LocalProperties.bind(); // the OPL4 wave rom location, without which the chip has no rom
    }

    @Test
    @DisplayName("the companion .PCM is loaded into the OPL4 sample RAM and named in the file bar")
    void userPcmReachesSampleRam() throws Exception {
        assumeTrue(Files.exists(MDR) && Files.exists(PCM), "samples not present: " + MDR);

        byte[] pcm = Files.readAllBytes(PCM);

        Setting.getInstance().getOutputDevice().setDeviceType(Common.DEV_Null);

        String file = MDR.toString();
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Files.newInputStream(MDR), null);
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();

        // the bank is written register by register while the driver starts, so let it play a bit
        plugin.getDriver().render(new short[8192], 0, 8192);

        byte[] ram = sampleRam(plugin);
        // what the fmdsp asks, through the source it binds to a driver of its own
        ChipFmDspSource source = new ChipFmDspSource();
        source.bind(null, plugin::getDriver);
        String pcmFilename = source.pcmFilename(0);
        String pcmType = source.pcmType(0);
        plugin.stop();
        plugin.close();

        // the bank goes to the head of the RAM, which is where the wave headers over 383 point
        assertArrayEquals(pcm, java.util.Arrays.copyOf(ram, pcm.length),
                "the sample RAM must hold the companion .PCM");

        // the fmdsp file bar labels the slot with the type and shows the name without it
        assertEquals(PCM.getFileName().toString(), pcmFilename, "the bank's name, not its extension");
        assertEquals("PCM", pcmType, "the slot label");
    }

    /** the RAM of the emulated OPL4 the plugin is playing through */
    static byte[] sampleRam(BasePlugin<? extends BaseDriver> plugin) throws Exception {
        YmF278BInst instrument = plugin.mds.inst(YmF278BInst.class);
        Field chips = YmF278BInst.class.getDeclaredField("chips");
        chips.setAccessible(true);
        YmF278B chip = ((YmF278B[]) chips.get(instrument))[0];
        Field ram = YmF278B.class.getDeclaredField("ram");
        ram.setAccessible(true);
        return (byte[]) ram.get(chip);
    }
}
