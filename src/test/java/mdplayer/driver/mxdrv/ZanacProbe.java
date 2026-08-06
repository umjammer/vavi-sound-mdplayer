package mdplayer.driver.mxdrv;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** temporary probe */
public class ZanacProbe {

    static {
        System.setProperty("mdplayer.variant.pcm8", "0");
        System.setProperty("mdplayer.variant.ym2151", "1");
    }

    static final String file = System.getProperty("zanac",
            "/Users/nsano/Public/np2/MXDRV/_未整理/_MDX詰め合わせ11435files/Compile/ZANAC/ZANACE.MDX");

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    void probe() throws Exception {
        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "midiMode", 0, "songNo", 0));
        plugin.prepare();

        MxDriver driver = (MxDriver) plugin.getDriver();
        int sampleRate = Setting.getInstance().getOutputDevice().getSampleRate();
        System.err.printf("XXX totalCounter: %d (%.1f sec)%n", driver.totalCounter, driver.totalCounter / (double) sampleRate);

        mdplayer.ChipFmDspSource source = new mdplayer.ChipFmDspSource();
        source.bind(plugin);

        short[] buf = new short[512];
        long samples = 0;
        long next = 0;
        long start = System.nanoTime();
        while (samples < (long) sampleRate * 10) {
            // pace to real time, the way the audio line does, so the display's slew is exercised
            long behind = samples * 1_000_000_000L / sampleRate - (System.nanoTime() - start);
            if (behind > 1_000_000) Thread.sleep(behind / 1_000_000);
            int r = driver.render(buf, 0, buf.length);
            if (r < 0) { System.err.println("XXX render < 0 at " + samples / (double) sampleRate); break; }
            samples += r / 2;
            source.update(new vavi.util.event.GenericEvent(this, "master", buf, 0));
            if (samples >= next) {
                long frames = source.generatedFrames();
                int srate = Math.max(1, source.sampleRate());
                int ssec = (int) ((frames % srate) * 100 / srate);
                long sec = frames / srate;
                System.err.printf("XXX %.2f sec: display=%02d:%02d-%02d counter=%d frameCounter=%d playing=%s%n",
                        samples / (double) sampleRate, sec / 60, sec % 60, ssec, driver.counter,
                        driver.frameCounter, source.playing());
                next += sampleRate / 2L;
            }
        }
        System.err.printf("XXX end: %.2f sec counter=%d stopped=%s%n", samples / (double) sampleRate, driver.counter, driver.stopped);
    }
}
