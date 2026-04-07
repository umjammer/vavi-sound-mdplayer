package mdplayer.plugin;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import mdplayer.chips.RealChipPlugin;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * SampledPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SampledPlugin extends BasePlugin {

    private static final Logger logger = getLogger(SampledPlugin.class.getName());

    public AudioInputStream naudioFileReader = null;
    public String naudioFileName = null;

    @Override
    public void prepare() {
        try {
            naudioFileReader = AudioSystem.getAudioInputStream(Path.of(this.naudioFileName).toFile());
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new IllegalArgumentException(e);
        }

        super.prepare();
    }

    @Override
    protected void initChips() {
    }

    protected byte[] naudioSrcbuffer = null;

    private static byte[] ensure(byte[] buffer, int bytesRequired) {
        if (buffer == null || buffer.length < bytesRequired) {
            buffer = new byte[bytesRequired];
        }
        return buffer;
    }

    private static void convert2ByteToShort(short[] destBuffer, int offset, byte[] source, int shortCount) {
        int samplesRead = shortCount;
        for (int n = 0; n < samplesRead; n++) {
            destBuffer[n] = ByteUtil.readLeShort(source, offset + n * Short.BYTES); // volume;
        }
    }

    public int read(short[] buffer, int offset, int count) {
        if (this.naudioFileReader != null) {
            if (this.chipRegister.plugin(RealChipPlugin.class).isThreadClosed()) {
                this.chipRegister.plugin(RealChipPlugin.class).setThreadStopped(true);
                //this.fadeout = false;
                //this.stopped = true;
            }
            return this.readAudio(buffer, offset, count);
        } else {
            return count;
        }
    }

    private int readAudio(short[] buffer, int offset, int count) {
        try {
            naudioSrcbuffer = ensure(naudioSrcbuffer, count * 2);
            convert2ByteToShort(buffer, offset, naudioSrcbuffer, count);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return count;
    }

    public void stopAudio() {
        try {
            AudioInputStream dmy = naudioFileReader;
            naudioFileReader = null;
            dmy.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
