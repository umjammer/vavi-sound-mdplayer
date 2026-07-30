/*
 * temporary verification for the YM2151 softReset NPE fix
 */

package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class Ym2151SoftResetCheck {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void softResetDoesNotThrow() throws Exception {
        // the crash needs chip index 1 to resolve to a registered instrument. The Mame variant
        // (index 1) is applied to both chip slots, so inst(1) is MameYm2151Inst, registered for
        // chip 0 but with chip 1's operators never initialised. Default 0 (fmgen) is unregistered
        // and mds.write would return early, hiding the bug.
        System.setProperty("mdplayer.variant.ym2151", "1");

        String file = System.getProperty("diag.file");
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();

        // the exact call chain from Audio.play that NPE'd on chip 1's uninitialised operators
        assertDoesNotThrow(() -> plugin.chipRegister.softReset(EnmModel.VirtualModel));
        assertDoesNotThrow(() -> plugin.chipRegister.softReset(EnmModel.RealModel));
    }
}
