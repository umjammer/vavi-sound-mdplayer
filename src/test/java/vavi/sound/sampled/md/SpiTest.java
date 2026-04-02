/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.FormatConversionProvider;

import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vavi.sound.SoundUtil.volume;
import static vavix.util.DelayedWorker.later;


/**
 * SpiTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-03-30 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class SpiTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    static boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static long time = onIde ? 1000 * 1000 : 10 * 1000;

    @Property(name = "vgm")
    String inFile = "src/test/resources/test.vgm";

    @Property
    String fmpDir;
    @Property
    String fmpPvi;

    @Property
    String zmsDir;

    @Property(name = "muap.dir.dta")
    String muapDirDta;
    @Property(name = "muap.dir.pcm")
    String muapDirPcm;

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        // disable other vgm conversion spi
        System.setProperty("vavi.sound.sampled.spi.emu", "false");
        System.setProperty("vavi.sound.sampled.spi.ymfm", "false");

        // fmp
        System.setProperty("mdplayer.fmp.dir", fmpDir);
        System.setProperty("mdplayer.fmp.pvi", fmpPvi);
        // zms
        System.setProperty("mdplayer.zms.dir", zmsDir);
        // muap
        System.setProperty("muap.dir.dta", muapDirDta);
        System.setProperty("muap.dir.pcm", muapDirPcm);
//        System.setProperty("muap.dir.udp", muapDirUdp);
//        System.setProperty("muap.dir.sud", muapDirSud);
Debug.println("volume: " + volume);
    }

    @Test
    @DisplayName("directly")
    public void test0() throws Exception {
Debug.println(inFile);
        Path path = Paths.get(inFile);
        AudioInputStream sourceAis = new MdAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path)));

        AudioFormat inAudioFormat = sourceAis.getFormat();
Debug.println("IN: " + inAudioFormat);
        AudioFormat outAudioFormat = new AudioFormat(
                44100,
                16,
                2,
                true,
                false);
Debug.println("OUT: " + outAudioFormat);

        assertTrue(new MdFormatConversionProvider().isConversionSupported(outAudioFormat, inAudioFormat));

        AudioInputStream secondAis = new MdFormatConversionProvider().getAudioInputStream(outAudioFormat, sourceAis);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, secondAis.getFormat());
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(secondAis.getFormat());
        line.addLineListener(ev -> Debug.println(ev.getType()));
        line.start();

        volume(line, volume);

        byte[] buf = new byte[1024];
        while (!later(time).come()) {
            int r = secondAis.read(buf, 0, 1024);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.stop();
        line.close();
    }

    @Test
    @DisplayName("by spi")
    public void test1() throws Exception {
Debug.println(inFile);
        Path path = Paths.get(inFile);
        AudioInputStream sourceAis = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(path)));

        AudioFormat inAudioFormat = sourceAis.getFormat();
Debug.println("IN: " + inAudioFormat + ", " + inAudioFormat.getEncoding().getClass().getName());

        assertInstanceOf(MdEncoding.class, inAudioFormat.getEncoding());

        AudioFormat outAudioFormat = new AudioFormat(
                44100,
                16,
                2,
                true,
                false);
Debug.println("OUT: " + outAudioFormat);

for(var codec : ServiceLoader.load(FormatConversionProvider.class)) {
 if (codec.isConversionSupported(outAudioFormat, inAudioFormat)) {
Debug.println("converter: " + codec.getClass().getName());
  break;
 }
}
        assertTrue(AudioSystem.isConversionSupported(outAudioFormat, inAudioFormat));

        AudioInputStream secondAis = AudioSystem.getAudioInputStream(outAudioFormat, sourceAis);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, secondAis.getFormat());
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(secondAis.getFormat());
        line.addLineListener(ev -> Debug.println(ev.getType()));
        line.start();

        volume(line, volume);

        byte[] buf = new byte[1024];
        while (!later(time).come()) {
            int r = secondAis.read(buf, 0, 1024);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.stop();
        line.close();
    }

    @Test
    public void test4() throws Exception {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes()) {
            System.err.println(type);
        }
        AudioInputStream originalAudioInputStream = AudioSystem.getAudioInputStream(new File(inFile).toURI().toURL());
        AudioFormat originalAudioFormat = originalAudioInputStream.getFormat();
Debug.println(originalAudioFormat);
    }

    @Test
    @DisplayName("another input type 2")
    void test2() throws Exception {
        URL url = Paths.get(inFile).toUri().toURL();
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        assertEquals(MdEncoding.VGM, ais.getFormat().getEncoding());
    }

    @Test
    @DisplayName("another input type 3")
    void test3() throws Exception {
        File file = Paths.get(inFile).toFile();
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        assertEquals(MdEncoding.VGM, ais.getFormat().getEncoding());
    }

    // com.sun.media.sound.SoftMidiAudioFileReader consumes 4byte unexpectedly.
    // so it's excluded when test. see -agent jvm option at maven-surefire-plugin
    @Test
    @DisplayName("when unsupported file coming")
    void test5() throws Exception {
        InputStream is = SpiTest.class.getResourceAsStream("/test.wma");
        int available = is.available();
        UnsupportedAudioFileException e = assertThrows(UnsupportedAudioFileException.class, () -> {
            Debug.println(is);
            AudioSystem.getAudioInputStream(is);
        });
Debug.println(e.getMessage());
        assertEquals(available, is.available()); // spi must not consume input stream even one byte
    }
}
