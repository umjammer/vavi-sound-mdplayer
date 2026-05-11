/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import mdplayer.Audio;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import vavi.util.Debug;
import vavi.util.archive.Archives;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-06-03 nsano initial version <br>
 */
@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
public class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String file;

    /** 1 origin */
    @Property
    int track;

    @Property(name = "mdplayer.fmp.dir")
    String fmpDir;
    @Property(name = "mdplayer.fmp.pvi")
    String fmpPvi;
    @Property(name = "mdplayer.zms.dir")
    String zmsDir;
    @Property(name = "mdplayer.mgs.dir")
    String mgsDir;
    @Property(name = "mdplayer.ndp.dir")
    String ndpDir;
    @Property(name = "mdplayer.musica.dir")
    String musicaDir;
    @Property(name = "muap.dir.dta")
    String muapDirDta;
    @Property(name = "muap.dir.pcm")
    String muapDirPcm;

    @Property(name = "mdplayer.variant.pcm8")
    int variantPcm8;
    @Property(name = "mdplayer.variant.mpcm")
    int variantMpcm;
    @Property(name = "mdplayer.variant.ym2151")
    int variantYm2151;
    @Property(name = "mdplayer.variant.ym2413")
    int variantYm2413;
    @Property(name = "mdplayer.variant.ymf262")
    int variantYmf262;
    @Property(name = "mdplayer.variant.ay8910")
    int variantAy8910;

    @Property
    String dir;

    @Property
    String ext;

    static final boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static final long time = onIde ? 1000 * 1000 : 10 * 1000;

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

        System.setProperty("mdplayer.variant.pcm8", String.valueOf(variantPcm8));
        System.setProperty("mdplayer.variant.mpcm", String.valueOf(variantMpcm));
        System.setProperty("mdplayer.variant.ym2151", String.valueOf(variantYm2151));
        System.setProperty("mdplayer.variant.ym2413", String.valueOf(variantYm2413));
        System.setProperty("mdplayer.variant.ay8910", String.valueOf(variantAy8910));
        System.setProperty("mdplayer.variant.ymf262", String.valueOf(variantYmf262));

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
Debug.println("volume: " + volume + ", player.volume: " + System.getProperty("mdplayer.volume") + ", cwd: " + System.getProperty("user.dir") + ", time: " + time);
Debug.println("settings\n" +
        "mdplayer.fmp.dir: " + System.getProperty("mdplayer.fmp.dir") + "\n" +
        "mdplayer.fmp.pvi: " + System.getProperty("mdplayer.fmp.pvi") + "\n" +
        "mdplayer.zms.dir: " + System.getProperty("mdplayer.zms.dir") + "\n" +
        "mdplayer.mgs.dir: " + System.getProperty("mdplayer.mgs.dir") + "\n" +
        "mdplayer.ndp.dir: " + System.getProperty("mdplayer.ndp.dir") + "\n" +
        "mdplayer.musica.dir: " + System.getProperty("mdplayer.musica.dir") + "\n" +
        "muap.dir.dta: " + System.getProperty("muap.dir.dta") + "\n" +
        "muap.dir.pcm: " + System.getProperty("muap.dir.pcm") + "\n" +
        "mdplayer.variant.pcm8: " + System.getProperty("mdplayer.variant.pcm8") + "\n" +
        "mdplayer.variant.mpcm: " + System.getProperty("mdplayer.variant.mpcm") + "\n" +
        "mdplayer.variant.ay8910: " + System.getProperty("mdplayer.variant.ay8910") + "\n" +
        "mdplayer.variant.ym2413: " + System.getProperty("mdplayer.variant.ym2413") + "\n" +
        "mdplayer.variant.ymf262: " + System.getProperty("mdplayer.variant.ymf262") + "\n" +
        "mdplayer.variant.ym2151: " + System.getProperty("mdplayer.variant.ym2151"));

        audio = Audio.getInstance(); // ⚠️ caution settings and system properties race condition
    }

    private Audio audio;

    /** */
    void play() throws Exception {
Debug.println("filename: " + file);
        FileFormat format = FileFormat.getFileFormat(file);
Debug.println("format: " + format.getClass().getSimpleName());
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of(
                "fileName", file,
                "midiMode", 0,
                "songNo", track - 1)
        );
Debug.println("plugin: " + plugin.getClass().getSimpleName());
        audio.init(plugin);
        audio.play();
    }

    @Test
    @DisplayName("play one in local.properties")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        play();

        CountDownLatch cdl = new CountDownLatch(1);
if (!onIde) {
 Thread.sleep(time);
Debug.println("not on ide");
} else {
        cdl.await();
}
    }

    // ^N to next song
    @Test
    @DisplayName("play random one in local.properties")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {

        playMulti(listFilesInLocalProperties());
    }

    /** files in local.properties includes commented out also */
    static List<Path> listFilesInLocalProperties() throws IOException {
        List<Path> paths = new ArrayList<>();
        Files.readAllLines(Paths.get("local.properties")).forEach(line -> {
            if (line.matches("^#?\\w+\\s*?=.*$")) {
                String file = line.substring(line.indexOf("=") + 1);
//System.err.println(file);
                Path path = Path.of(file);
                if (Files.exists(path) && !Files.isDirectory(path))
                    paths.add(path);
            }
        });
        return paths;
    }

    /** play list, nexting by hitting ^n */
    void playMulti(List<Path> files) throws Exception {
        AtomicReference<CountDownLatch> cdl = new AtomicReference<>();
        Random random = new Random(System.currentTimeMillis());

        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyReleased(NativeKeyEvent event) {
                int keyCode = event.getKeyCode();
//Debug.println("keyTyped: " + keyCode + ", " + ((event.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0));
                if ((event.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && keyCode == NativeKeyEvent.VC_N) {
Debug.print("countdown");
                    cdl.get().countDown();
                }
            }
        });

        while (true) {
            this.file = files.get(random.nextInt(files.size())).toString();
            cdl.set(new CountDownLatch(1));
Debug.print("play: " + file + " ---------------------------------------------------------------------");
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(() -> { try { play(); } catch (Exception e) { Debug.printStackTrace(e); }});
Debug.print("await");
            cdl.get().await();
Debug.println("await: broke");
            es.shutdownNow();
Debug.println("stop");
            audio.stop();
            audio.close(); // TODO doesn't work well
        }
    }

    /**
     * @param dir separated by ';'
     * @param ext separated by ','
     */
    static List<Path> listFilesUnderDirFilteredByExt(String dir, String ext) {
Debug.println("dir: " + dir);
Debug.println("ext: " + ext);
        Predicate<Path> x = p -> Arrays.stream(ext.split(",")).anyMatch(e -> p.getFileName().toString().toUpperCase().endsWith(e));
        return Arrays.stream(dir.split(File.pathSeparator)).flatMap(d -> {
            try {
                return Files.walk(Paths.get(d)).filter(x);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).toList();
    }

    @Test
    @DisplayName("show meta data in the dir filtered by ext")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        listFilesUnderDirFilteredByExt(dir, ext).forEach(p -> {
            try {
                FileFormat format = FileFormat.getFileFormat(p.toString());
Debug.println(p);
                format.load(Files.newInputStream(p), null);
                MetaData music = format.getMetaData();
Debug.println(music);
            } catch (Exception _) {
            }
        });
    }

    @Test
    @DisplayName("play random one in the dir filtered by ext")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test4() throws Exception {
        playMulti(listFilesUnderDirFilteredByExt(dir, ext));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void testX() throws Exception {
        mdplayer.Program.main(new String[] {file});

        CountDownLatch cdl = new CountDownLatch(1);
        cdl.await();
    }

    /**
     * @param args 0: audio file
     */
    static void main(String[] args) throws Exception {
        TestCase app = new TestCase();
        if (args.length == 1)
            app.file = args[0];
        else
            app.setup();

        app.play();
    }
}
