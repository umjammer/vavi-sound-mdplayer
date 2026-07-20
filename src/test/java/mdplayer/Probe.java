package mdplayer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.chips.MidiPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import vavi.util.archive.Archives;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


/** scratch: does stopping a MIDI song actually silence the synthesizer? */
class Probe {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "probe")
    void probe() throws Exception {
        LocalProperties.bind();
        String file = System.getProperty("probe.file");

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Archives.getInputStream(new BufferedInputStream(Files.newInputStream(Path.of(file)))), null);
        @SuppressWarnings("unchecked")
        var plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file));
        plugin.prepare();
        plugin.stopped = false;
        plugin.paused = false;
        plugin.fadeout = false;

        MidiPlugin midi = plugin.chipRegister.plugin(MidiPlugin.class);
        short[] buffer = new short[1024];
        BaseDriver d = plugin.getDriver();
        int seconds = Integer.getInteger("probe.seconds", 10);
        int lastLoop = -1;
        for (int i = 0; i < 44100 * seconds / buffer.length; i++) {
            d.render(buffer, 0, buffer.length);
            if (d instanceof mdplayer.driver.ahx.AhxDriver ahx && i % 400 == 0) {
                var p = ahx.getPlayer();
                System.err.printf("%.1fs posNr=%d/%d noteNr=%d tempo=%d end=%d%n",
                        (double) i * buffer.length / 44100, p.posNr, p.song.positionNr,
                        p.noteNr, p.tempo, p.songEndReached);
            }
            if (d.curLoop != lastLoop || d.stopped || plugin.stopped) {
                System.err.printf("%.1fs curLoop=%d stopped=%b/%b counter=%d total=%d%n",
                        (double) i * buffer.length / 44100, d.curLoop, d.stopped, plugin.stopped,
                        d.counter, d.totalCounter);
                lastLoop = d.curLoop;
                if (d.stopped || plugin.stopped) break;
            }
        }
        System.err.println("end: voices=" + midi.activeVoices() + " sent=" + midi.sentMessages());

        plugin.stop();
        midi.allSoundOff();
    }
}
