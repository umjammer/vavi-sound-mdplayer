package mdplayer.driver.sid;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.SidChip;
import mdplayer.driver.BasePlugin;
import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.lib.sid.libsidplayfp.utils.SidDatabase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * The songlength database — how long a SID tune plays for, when the collection says so.
 */
@EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
public class SongLengthTest {

    static final String tune = "../JSIDPlay2/tmp/Last_Ninja.sid";

    /** the times are "m:ss" with the milliseconds, when there are any, one to three digits */
    @Test
    public void testParseTime() {
        assertEquals(62000, SidDatabase.parseTime("1:02"));
        assertEquals(62500, SidDatabase.parseTime("1:02.5"));
        assertEquals(62500, SidDatabase.parseTime("1:02.500"));
        assertEquals(754125, SidDatabase.parseTime("12:34.125"));
        assertEquals(0, SidDatabase.parseTime("0:00"));
    }

    /** an entry lists a time per subtune, and says nothing at all about a tune it does not have */
    @Test
    public void testLookup(@TempDir Path dir) throws Exception {
        Path db = write(dir, "1:53.500 2:04 0:06");

        SidDatabase database = new SidDatabase();
        assertTrue(database.open(db.toString()));

        byte[] md5 = md5(tune);
        assertEquals(113500, database.lengthMs(md5, 1));
        assertEquals(124000, database.lengthMs(md5, 2));
        assertEquals(6000, database.lengthMs(md5, 3));
        assertEquals(-1, database.lengthMs(md5, 4), "no time for that subtune");
        assertEquals(113, database.length(md5, 1), "the same, in seconds");

        byte[] unknown = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
        assertEquals(-1, database.lengthMs(unknown, 1), "not a tune it knows");
    }

    /** the database is keyed by the md5 of the whole file, header and all */
    @Test
    public void testFingerprint() throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(tune));
        SidTune sidTune = new SidTune(data, data.length);
        sidTune.selectSong(1);

        byte[] buf = new byte[SidTune.MD5_LENGTH + 1];
        assertEquals(new String(md5(tune), StandardCharsets.US_ASCII),
                new String(sidTune.createMD5New(buf), 0, SidTune.MD5_LENGTH, StandardCharsets.US_ASCII));
    }

    /**
     * A tune the database has a time for ends at that time, rather than when the driver's watching
     * of the register writes works out that it has come around again.
     */
    @Test
    public void testDriverEndsWhereTheDatabaseSays(@TempDir Path dir) throws Exception {
        int seconds = 5;
        // the tune's own third subtune is the one that plays, so that is the time that counts
        Path db = write(dir, "9:99 9:99 0:0" + seconds);

        byte[] fileBuffer = Files.readAllBytes(Paths.get(tune));

        BasePlugin<?> plugin = mock(BasePlugin.class, Mockito.RETURNS_DEEP_STUBS);
        java.lang.reflect.Field field = BasePlugin.class.getDeclaredField("chipRegister");
        field.setAccessible(true);
        field.set(plugin, mock(mdplayer.ChipRegister.class));
        when(plugin.chipRegister.chip(SidChip.class)).thenReturn(new SidChip());

        Setting.getInstance().setSid(new Setting.SID());
        Setting.getInstance().getSid().songLengthPath = db.toString();
        Setting.getInstance().getOutputDevice().setSampleRate(44100);

        SidMdDriver driver = new SidMdDriver(plugin) {
            {
                this.dataBuf = fileBuffer;
            }
        };
        driver.init(EnmModel.VirtualModel, 0, 0, 0);

        try {
            short[] buffer = new short[2048 * 2];
            long samples = 0;
            while (!driver.stopped && samples < 44100L * 60) {
                samples += driver.render(buffer, 0, buffer.length) / 2;
            }

            double played = samples / 44100.0;
            assertTrue(driver.stopped, "ended");
            assertEquals(seconds, played, 0.1, "ended where the database says, played " + played + "s");
        } finally {
            // the setting is the one every driver reads, so it cannot be left pointing here
            Setting.getInstance().getSid().songLengthPath = "";
        }
    }

    /** a database with one entry in it, the tune's, at the times given */
    static Path write(Path dir, String times) throws Exception {
        Path db = dir.resolve("Songlengths.md5");
        Files.writeString(db, """
                [Database]
                ; %s
                %s=%s
                """.formatted(tune, new String(md5(tune), StandardCharsets.US_ASCII), times));
        return db;
    }

    static byte[] md5(String file) throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(Paths.get(file)));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append("%02x".formatted(b));
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
