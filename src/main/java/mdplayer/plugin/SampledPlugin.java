package mdplayer.plugin;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import mdplayer.format.FileFormat;

import static java.lang.System.getLogger;


/**
 * SampledPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SampledPlugin extends BasePlugin {

    private static final Logger logger = getLogger(SampledPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        try {
            audio.naudioFileReader = AudioSystem.getAudioInputStream(new java.io.File(audio.naudioFileName));
            return true;
        } catch (UnsupportedAudioFileException | IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
    }
}
