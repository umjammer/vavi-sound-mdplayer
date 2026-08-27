/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp7;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdplayer.lib.fmp7.Fmp7Compiler;
import mdplayer.lib.fmp7.Fmp7File;
import mdplayer.lib.fmp7.Fmp7Player;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Compiles FMP7 MML the way the player does, which means booting an emulated PC and running
 * FMP7's own compile core on it - so this is not a quick test.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-27 nsano initial version <br>
 */
class Fmp7CompilerTest {

    /** a song that compiles cleanly with the current FMC7, and the object it was shipped as */
    static final Path mwi = Path.of(System.getProperty("mdplayer.fmp7.test.mwi",
            "tmp/fmp7/DeltaRay_FMP7/deltaray.mwi"));

    /** the same song as its author compiled it, in 2010 */
    static final Path owi = Path.of(System.getProperty("mdplayer.fmp7.test.mwi.owi",
            "tmp/fmp7/DeltaRay_FMP7/deltaray.owi"));

    @Test
    void theMmlSaysWhatTheObjectSays() throws Exception {
        assumeTrue(Files.exists(mwi), mwi + " is missing, set -Dmdplayer.fmp7.test.mwi");
        assumeTrue(Files.exists(owi), owi + " is missing");

        Fmp7File source = Fmp7File.decodeMml(Files.readAllBytes(mwi));
        Fmp7File object = Fmp7File.decode(Files.readAllBytes(owi));
        assertEquals(object.getTitle(), source.getTitle());
        assertEquals(object.getComposer(), source.getComposer());
        assertEquals(object.getCreator(), source.getCreator());

        // and the play list gets it through the driver, without compiling anything
        MetaData md = new Fmp7Driver().retrieveMetaData(Files.readAllBytes(mwi));
        assertNotNull(md);
        assertEquals(object.getTitle(), md.getFirst(Tag.Title));
    }

    @Test
    void theMmlIsFoundByItsName() throws Exception {
        assumeTrue(Files.exists(mwi), mwi + " is missing");

        FileFormat format = FileFormat.getFileFormat(mwi.toString());
        assertInstanceOf(Fmp7FileFormat.class, format);
        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(mwi))) {
            assertInstanceOf(Fmp7FileFormat.class, FileFormat.getFileFormat(is));
        }
        format.load(new BufferedInputStream(Files.newInputStream(mwi)), null);
        assertTrue(format.isMml(), mwi + " should be mml");
        assertTrue(format.getCompiledFilename().toLowerCase().endsWith(".owi"));
    }

    /**
     * The compiler itself: the object it produces has to be the one the song was shipped as.
     * <p>
     * Not byte for byte - FMC7 stamps its own version into the header and checksums the chunk it
     * is in, and the compiler here is nine versions past the one that made the file in the repo -
     * so what is asked is that everything else matches, which for this song is all but six bytes.
     */
    @Test
    void compilesToWhatTheSongWasShippedAs() throws Exception {
        assumeTrue(Fmp7Compiler.isAvailable(),
                "no fmc.dll, set -D" + Fmp7Player.FMP7_PATH_KEY + "=<dir>");
        assumeTrue(Files.exists(mwi), mwi + " is missing");
        assumeTrue(Files.exists(owi), owi + " is missing");

        Fmp7Compiler compiler = new Fmp7Compiler();
        long start = System.currentTimeMillis();
        byte[] object = compiler.compile(Files.readAllBytes(mwi), mwi.getParent());
        byte[] shipped = Files.readAllBytes(owi);
System.err.printf("%s: %d bytes of mml -> %d of object in %.1fs%n",
        mwi, Files.size(mwi), object.length, (System.currentTimeMillis() - start) / 1000d);

        assertEquals(shipped.length, object.length, "the object is not the size it was shipped at");
        int differences = 0;
        for (int i = 0; i < shipped.length; i++) {
            if (shipped[i] != object[i]) {
                differences++;
            }
        }
System.err.printf("%d bytes differ from the shipped object (the version stamp and its checksum)%n", differences);
        assertTrue(differences <= 8, differences + " bytes differ, which is more than a version stamp");

        // and it says the same thing about itself
        Fmp7File compiled = Fmp7File.decode(object);
        assertEquals(Fmp7File.decode(shipped).getTitle(), compiled.getTitle());
    }

    /**
     * A song the current compiler rejects, which is a thing that happens: FMC7 has moved on since
     * these were written and some of what they use is an error now - an envelope on a pcm part,
     * for this one.
     * <p>
     * What is asked here is that the player says so rather than falling over or playing silence.
     * It cannot say which line: fmc.dll reports every error by throwing it, and the emulated PC
     * has no c++ exception handling, so the compiler dies at the first one and the reason goes
     * with it. The message says that much rather than pretending to know more.
     */
    @Test
    void aSongItRejectsSaysSo() throws Exception {
        assumeTrue(Fmp7Compiler.isAvailable(), "no fmc.dll");
        Path rejected = Path.of(System.getProperty("mdplayer.fmp7.test.mwi.bad",
                "tmp/fmp7/overdose/overdose.mwi"));
        assumeTrue(Files.exists(rejected), rejected + " is missing");

        Fmp7Compiler compiler = new Fmp7Compiler();
        IOException e = assertThrows(IOException.class,
                () -> compiler.compile(Files.readAllBytes(rejected), rejected.getParent()));
        List<String> problems = compiler.getProblems();
System.err.println(rejected + ": " + e.getMessage());
problems.forEach(System.err::println);
        assertNotNull(e.getMessage());
        assertTrue(e.getMessage().contains("stopped at the first thing")
                        || problems.stream().anyMatch(p -> p.startsWith("fmc7c: error")),
                "it refused the song without saying anything about it: " + e.getMessage());
    }

    /** and the whole way through: open the mml, and have the driver playing the object it made */
    @Test
    void playsMmlThroughThePlugin() throws Exception {
        assumeTrue(Fmp7Player.isAvailable(), "no FMP7.exe");
        assumeTrue(Fmp7Compiler.isAvailable(), "no fmc.dll");
        assumeTrue(Files.exists(mwi), mwi + " is missing");

        Setting.getInstance().getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(mwi.toString());
        format.load(new BufferedInputStream(Files.newInputStream(mwi)), null);
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", mwi.toString()));
        plugin.prepare();

        assertTrue(Fmp7File.isFmp7(plugin.getData()), "the plugin should be playing an object, not mml");

        BaseDriver driver = plugin.getDriver();
        short[] buffer = new short[2048];
        int peak = 0;
        long deadline = System.currentTimeMillis() + 120_000;
        while (peak <= 1000 && System.currentTimeMillis() < deadline && !driver.stopped) {
            driver.render(buffer, 0, buffer.length);
            for (short s : buffer) {
                peak = Math.max(peak, Math.abs(s));
            }
        }
System.err.printf("%s: peak %d%n", mwi, peak);
        plugin.stop();
        plugin.close();

        assertTrue(peak > 1000, mwi + " never made a sound");
    }
}
