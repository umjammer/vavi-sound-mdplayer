package mdplayer.driver.moonDriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;


/**
 * Renders any supported file to a wav, for holding one playback path against another.
 * <p>
 * A song that exists both as a driver file and as a vgm recorded off the real machine can be
 * rendered twice this way and the two compared - band energies, envelope, correlation - which is
 * how the MoonDriver .MDR path was checked against its MoonDriver Demo vgz.
 * <p>
 * usage: RenderToWav &lt;in file&gt; &lt;out wav&gt; [seconds]
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-01 nsano initial version <br>
 */
public class RenderToWav {

    public static void main(String[] args) throws Exception {
        String in = args[0];
        String out = args[1];
        double seconds = args.length > 2 ? Double.parseDouble(args[2]) : 30;

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(in);
        format.load(Files.newInputStream(Path.of(in)), null);
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", in));
        plugin.prepare();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        short[] buffer = new short[8192];
        long frames = 0;
        while (true) {
            plugin.getDriver().render(buffer, 0, buffer.length);
            for (short value : buffer) {
                baos.write(value & 0xff);
                baos.write((value >> 8) & 0xff);
            }
            frames += buffer.length / 2;
            if (frames / 44100. > seconds) break;
            if (plugin.driverVirtual.stopped) {
                System.err.println("stopped at " + frames / 44100. + "s");
                break;
            }
        }

        plugin.stop();
        plugin.close();

        AudioFormat af = new AudioFormat(44100, 16, 2, true, false);
        byte[] audioBytes = baos.toByteArray();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(audioBytes), af, audioBytes.length / af.getFrameSize()),
                AudioFileFormat.Type.WAVE, new File(out));
        System.err.println("wrote " + out + " " + audioBytes.length / af.getFrameSize() / 44100. + "s");
        System.exit(0);
    }
}
