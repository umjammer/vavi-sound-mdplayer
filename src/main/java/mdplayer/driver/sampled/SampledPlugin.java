package mdplayer.driver.sampled;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import mdplayer.chips.RealChipPlugin;
import mdplayer.driver.BasePlugin;
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

    public AudioInputStream fileReader = null;
    public String fileName = null;

    @Override
    public void prepare() {
        try {
            fileReader = AudioSystem.getAudioInputStream(Path.of(this.fileName).toFile());
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new IllegalArgumentException(e);
        }

        super.prepare();
    }

    @Override
    protected void initChips() {
    }

    private byte[] srcbuffer = null;

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
        if (this.fileReader != null) {
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
            srcbuffer = ensure(srcbuffer, count * 2);
            convert2ByteToShort(buffer, offset, srcbuffer, count);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return count;
    }

    public void stopAudio() {
        try {
            AudioInputStream dmy = fileReader;
            fileReader = null;
            dmy.close();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
