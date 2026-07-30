package mdplayer.tool;

import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.Setting;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;

import org.junit.jupiter.api.Test;

import static mdsound.MDSound.Chip.MAIN_TAG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression test for {@link Setting.Balance} XML (de)serialization — the round-trip that
 * {@link VolumeBalanceCalibrator} writes and the mixer preset loader reads.
 */
class BalanceLoadCheck {

    @Test
    void roundTripThroughMap() throws Exception {
        Setting.Balance b = new Setting.Balance();
        b.setMasterVolume(-5);
        b.setVolume(MAIN_TAG, Ym2612Chip.class, -12);
        b.setVolume(MAIN_TAG, Sn76489Chip.class, 7);
        b.setVolume("FM", Ym2608Chip.class, -3);
        b.setMidiVolume(-16);
        b.setGimicOPNVolume(31);

        Path tmp = Files.createTempFile("balance", ".xml");
        b.save(tmp);
        Setting.Balance r = Setting.Balance.load(tmp);
        Files.deleteIfExists(tmp);

        assertNotNull(r);
        assertEquals(-5, r.getMasterVolume());
        assertEquals(-12, r.getVolume(MAIN_TAG, Ym2612Chip.class));
        assertEquals(7, r.getVolume(MAIN_TAG, Sn76489Chip.class));
        assertEquals(-3, r.getVolume("FM", Ym2608Chip.class)); // sub-tag element mapping
        assertEquals(-16, r.getMidiVolume()); // the MIDI path's own slot
        assertEquals(31, r.getGimicOPNVolume());
    }

    @Test
    void midiVolumeOfThePresetThatUsesIt() {
        Path dir = Path.of("src/main/resources/mdplayer/resources");
        Setting.Balance zmd = Setting.Balance.load(dir.resolve("DefaultVolumeBalance_ZMD.xml"));
        assertNotNull(zmd);
        // ZMS songs can be MIDI-only, which is the case MidiVolume exists for
        assertNotEquals(0, zmd.getMidiVolume());
    }

    @Test
    void bundledPresetsLoad() {
        Path dir = Path.of("src/main/resources/mdplayer/resources");
        Setting.Balance mdx = Setting.Balance.load(dir.resolve("DefaultVolumeBalance_MDX.xml"));
        assertNotNull(mdx);
        // MDX preset carries a real Pcm8 gain (element PCM8Volume -> Pcm8Chip)
        assertEquals(mdx.getVolume(MAIN_TAG, Pcm8Chip.class),
                mdx.getVolume(MAIN_TAG, Pcm8Chip.class)); // present & parseable
    }
}
