/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.Common.EnmModel;
import mdplayer.driver.moonDriver.MoonDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.moonDriver.MDLPlugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Regression guard for the MoonDriver (MDL) init path.
 * <p>
 * An {@code .MDL} is MoonDriver MML source: the plugin compiles it into an {@code .MDR}
 * binary (into its own {@code dataBuf}) during {@code initChips()}, then calls
 * {@link MoonDriver#init}. The driver had only snapshotted the still-raw MDL in its
 * constructor, so {@code init()} saw the MDL header, routed to the dead {@code initMDL()},
 * recompiled with no compile switches and blew up with
 * {@code NullPointerException: ... "info" is null} (MoonDriver.java:209).
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-22 nsano initial version <br>
 */
class MoonDriverTest {

    /** MoonDriver MML source sample (lives in the sibling vavi-sound-moon repo). */
    static final Path MDL = Path.of("../vavi-sound-moon/tmp/O3D_REV2/O3D001.MDL");

    /** the plugin owns {@code dataBuf} as {@code protected}; the compiled binary is fed there. */
    static void setPluginData(BasePlugin<?> plugin, byte[] data) throws Exception {
        Field f = BasePlugin.class.getDeclaredField("dataBuf");
        f.setAccessible(true);
        f.set(plugin, data);
    }

    @Test
    @DisplayName("compiling an .MDL source yields an MDR binary")
    void compileProducesMdr() throws Exception {
        assumeTrue(Files.exists(MDL), "sample not present: " + MDL);

        byte[] mdl = Files.readAllBytes(MDL);
        MDLPlugin plugin = new MDLPlugin();
        plugin.playingFileName = MDL.toAbsolutePath().toString();
        MoonDriver driver = new MoonDriver(plugin);

        byte[] compiled = driver.compile(mdl);

        assertEquals("MDRV", new String(compiled, 0, 4), "compiled MoonDriver output must start with the MDRV header");
    }

    @Test
    @DisplayName("init() picks up the plugin's compiled MDR instead of the stale raw MDL")
    void initUsesCompiledData() throws Exception {
        assumeTrue(Files.exists(MDL), "sample not present: " + MDL);

        byte[] mdl = Files.readAllBytes(MDL);
        MDLPlugin plugin = new MDLPlugin();
        plugin.playingFileName = MDL.toAbsolutePath().toString();
        setPluginData(plugin, mdl);                   // plugin holds the raw MDL source ...

        MoonDriver driver = new MoonDriver(plugin);   // ... which the ctor snapshots

        // mimic MDLPlugin.initChips(): compile the MDL and store the MDR into the plugin,
        // exactly as `dataBuf = driverVirtual.compile(dataBuf)` does.
        byte[] compiled = driver.compile(mdl);
        setPluginData(plugin, compiled);

        // before the fix this recompiled a headerless buffer with no switches and threw
        // NullPointerException from initMDL(); it must now route to initMDR() cleanly.
        assertDoesNotThrow(() -> driver.init(EnmModel.VirtualModel, 0, 0));
    }
}
