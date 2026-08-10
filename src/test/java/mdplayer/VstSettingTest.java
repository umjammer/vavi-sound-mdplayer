/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import mdplayer.vst.VstInfo;
import vavi.util.serdes.Serdes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * An effect chain surviving the settings file.
 * <p>
 * The chain had never actually been written before - nothing could load a plug-in, so the array
 * was always empty - and the first thing that put a real entry in it broke reading the settings
 * back, all of them: an editor position is a {@link Point}, which is written out through its
 * double accessors as {@code <x>120.0</x>} and will not read back into the int constructor
 * without {@code Setting.PointDeserializer}, the way every other point in the settings is
 * annotated. One unreadable element fails the whole document.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-08 nsano initial version <br>
 */
class VstSettingTest {

    @Test
    @DisplayName("an effect is written to the settings and read back whole")
    void effectRoundTrip() throws Exception {
        VstInfo effect = new VstInfo();
        effect.key = "12345";
        effect.fileName = "/Library/Audio/Plug-Ins/VST/Something.vst";
        effect.effectName = "Something";
        effect.vendorName = "somebody";
        effect.power = true;
        effect.location = new Point(120, 340);
        effect.param = new float[] {0.1f, 0.25f, 0.75f};

        Setting setting = Setting.getInstance();
        setting.getVst().setVSTInfo(new VstInfo[] {effect});

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Serdes.Util.serialize(setting, out);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("<effectName>Something</effectName>"),
                "the effect was not written to the settings at all");

        Setting read = Serdes.Util.deserialize(new ByteArrayInputStream(out.toByteArray()), new Setting());

        VstInfo[] infos = read.getVst().getVSTInfo();
        assertNotNull(infos, "the effect chain did not come back");
        assertEquals(1, infos.length);
        assertEquals(effect.key, infos[0].key);
        assertEquals(effect.fileName, infos[0].fileName);
        assertEquals(effect.effectName, infos[0].effectName);
        assertEquals(effect.power, infos[0].power);
        assertEquals(effect.location, infos[0].location);
        assertArrayEquals(effect.param, infos[0].param, 0);
    }
}
