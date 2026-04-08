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
import javax.sound.SoundClip;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

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
    @Property
    String mgsDir;
    @Property
    String ndpDir;
    @Property
    String musicaDir;
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

            // fmp
            System.setProperty("mdplayer.fmp.dir", fmpDir);
            System.setProperty("mdplayer.fmp.pvi", fmpPvi);
            // zms
            System.setProperty("mdplayer.zms.dir", zmsDir);
            // mgsdrv
            System.setProperty("mdplayer.mgs.dir", mgsDir);
            // ndp
            System.setProperty("mdplayer.ndp.dir", ndpDir);
            // musica
            System.setProperty("mdplayer.musica.dir", musicaDir);
            // muap
            System.setProperty("muap.dir.dta", muapDirDta);
            System.setProperty("muap.dir.pcm", muapDirPcm);
//            System.setProperty("muap.dir.udp", muapDirUdp);
//            System.setProperty("muap.dir.sud", muapDirSud);
        }

        // disable other vgm conversion spi
        System.setProperty("vavi.sound.sampled.spi.emu", "false");
        System.setProperty("vavi.sound.sampled.spi.ymfm", "false");

//        System.setProperty("mdplayer.variant.ym2151", "2"); // TODO this kills pcm8
        System.setProperty("mdplayer.variant.pcm8", "0");
//        System.setProperty("mdplayer.variant.mpcm", "0");
//        System.setProperty("mdplayer.variant.ym2151", "1");
//        System.setProperty("mdplayer.variant.ay8910", "1");

Debug.println("volume: " + volume + ", player.volume: " + System.getProperty("mdplayer.volume") + ", cwd: " + System.getProperty("user.dir") + ", time: " + time);
Debug.println("mdplayer.fmp.dir: " + System.getProperty("mdplayer.fmp.dir"));
Debug.println("mdplayer.fmp.pvi: " + System.getProperty("mdplayer.fmp.pvi"));
Debug.println("mdplayer.zms.dir: " + System.getProperty("mdplayer.zms.dir"));
Debug.println("mdplayer.mgs.dir: " + System.getProperty("mdplayer.mgs.dir"));
Debug.println("mdplayer.ndp.dir: " + System.getProperty("mdplayer.ndp.dir"));
Debug.println("mdplayer.musica.dir: " + System.getProperty("mdplayer.musica.dir"));
Debug.println("muap.dir.dta: " + System.getProperty("muap.dir.dta"));
Debug.println("muap.dir.pcm: " + System.getProperty("muap.dir.pcm"));
Debug.println("mdplayer.variant.ymf262: " + System.getProperty("mdplayer.variant.ymf262"));
    }

    @Test
    @DisplayName("directly")
    @DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*") // github workflow doesn't support volume
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
    @DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*") // github workflow doesn't support volume
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
        assertInstanceOf(MdEncoding.class, ais.getFormat().getEncoding());
    }

    @Test
    @DisplayName("another input type 3")
    void test3() throws Exception {
        File file = Paths.get(inFile).toFile();
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        assertInstanceOf(MdEncoding.class, ais.getFormat().getEncoding());
    }

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

    @Test
    @Disabled("loading takes too long time")
    void test6() throws Exception {
        var clip = SoundClip.createSoundClip(Path.of(inFile).toFile());
        clip.play();
    }
}
