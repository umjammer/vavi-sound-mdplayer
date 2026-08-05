/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver;

import java.util.List;

import mdplayer.ChipFmDspSource;
import mdplayer.Common.EnmModel;
import org.junit.jupiter.api.Test;
import vavi.util.compat.Tuple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Verifies that generic drivers with extendFiles expose their PCM file info
 * to ChipFmDspSource automatically.
 */
class GenericDriverPcmLoadTest {

    static class TestPlugin extends BasePlugin<BaseDriver> {
        public TestPlugin(List<Tuple<String, byte[]>> extendFiles) {
            this.extendFiles = extendFiles;
        }

        @Override public void initChips() {}
    }

    static class DummyDriver extends BaseDriver {
        public DummyDriver(BasePlugin<? extends BaseDriver> plugin) {
            super(plugin);
        }

        @Override public void init(EnmModel model, int latency, int waitTime, Object... args) {}
        @Override public void processOneFrame() {}
        @Override public musicDriverInterface.MetaData getMetaData(byte[] buf, Object... args) { return null; }
    }

    @Test
    void testGenericDriverExtendFiles() {
        List<Tuple<String, byte[]>> files = List.of(
                new Tuple<>("SAMPLE.PDX", new byte[0]),
                new Tuple<>("VOICE.PND", new byte[0])
        );
        TestPlugin plugin = new TestPlugin(files);
        DummyDriver driver = new DummyDriver(plugin);

        ChipFmDspSource genericSource = new ChipFmDspSource();
        genericSource.bind(null, () -> driver);

        assertEquals("PDX", genericSource.pcmType(0), "Slot 0 type should be derived from extension .PDX");
        assertEquals("SAMPLE.PDX", genericSource.pcmFilename(0), "Slot 0 filename");

        assertEquals("PND", genericSource.pcmType(1), "Slot 1 type should be derived from extension .PND");
        assertEquals("VOICE.PND", genericSource.pcmFilename(1), "Slot 1 filename");

        assertNull(genericSource.pcmType(2), "Slot 2 should be null");
        assertNull(genericSource.pcmFilename(2), "Slot 2 filename should be null");
    }
}
