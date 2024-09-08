package mdplayer.plugin;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import mdplayer.format.FileFormat;


/**
 * SampledPlugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-08 nsano initial version <br>
 */
public class SampledPlugin extends BasePlugin {

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        try {
            audio.naudioFileReader = AudioSystem.getAudioInputStream(new java.io.File(audio.naudioFileName));
            return true;
        } catch (UnsupportedAudioFileException | java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
