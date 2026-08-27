/*
 * scratch: render every FMP song under -Dprobe.file and print its rms
 */

package mdplayer.driver.fmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import mdplayer.Common;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/**
 * Written for the {@code Request port:$0071} crash: the rms of a song is what tells a swallowed
 * emulator exception apart from a song that really plays, so sweeping a directory before and
 * after a change to {@link mdplayer.emu.nise98.Nise98} shows which songs it moved.
 */
class Port71Probe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        System.setProperty("mdplayer.fmp.dir", "/Users/nsano/Public/np2/FMP/FMPDDISK");
        System.setProperty("mdplayer.fmp.pvi", "/Users/nsano/Public/np2/PVI;/Users/nsano/Public/np2/fmp_ume3");
        String file = System.getProperty("probe.file",
                "/Users/nsano/Public/np2/FMP_/Game/_Adult/ELF/Yuno/Ver2/YUNO_85B.OPI");
        int seconds = Integer.getInteger("probe.seconds", 10);
        String wav = System.getProperty("probe.wav", "");

        for (Path p : list(file)) {
            try {
                one(p, seconds, wav);
            } catch (Exception e) {
                System.out.printf("%-16s FAILED %s%n", p.getFileName(), e);
            }
        }
    }

    static List<Path> list(String file) throws Exception {
        Path p = Path.of(file);
        if (!Files.isDirectory(p)) return List.of(p);
        try (var s = Files.walk(p)) {
            return s.filter(x -> {
                String n = x.getFileName().toString().toUpperCase();
                return n.endsWith(".OPI") || n.endsWith(".OVI") || n.endsWith(".OZI");
            }).sorted().toList();
        }
    }

    static void one(Path p, int seconds, String wav) throws Exception {
        FileFormat format = FileFormat.getFileFormat(p.toString());
        format.load(Files.newInputStream(p), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", p.toString()));
        plugin.prepare();

        BaseDriver driver = plugin.getDriver();
        short[] buffer = new short[2 * 1024];
        ByteArrayOutputStream pcm = new ByteArrayOutputStream();
        double sum = 0;
        long n = 0;
        for (int i = 0; i < Common.VGMProcSampleRate * seconds / 1024; i++) {
            driver.render(buffer, 0, 1024);
            for (short s : buffer) {
                sum += (double) s * s;
                n++;
                pcm.write(s & 0xff);
                pcm.write((s >> 8) & 0xff);
            }
        }
        System.out.printf("%-16s rms=%.1f%n", p.getFileName(), Math.sqrt(sum / n));

        if (!wav.isEmpty()) {
            byte[] data = pcm.toByteArray();
            AudioFormat fmt = new AudioFormat(Common.VGMProcSampleRate, 16, 2, true, false);
            AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(data), fmt, data.length / 4),
                    AudioFileFormat.Type.WAVE, Path.of(wav).toFile());
            System.out.println("wrote " + wav);
        }
    }
}
