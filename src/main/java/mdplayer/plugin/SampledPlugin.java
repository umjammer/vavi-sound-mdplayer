package mdplayer.plugin;

import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Path;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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
    public void prepare() {
        try {
            audio.naudioFileReader = AudioSystem.getAudioInputStream(Path.of(audio.naudioFileName).toFile());
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void initChips() {
    }
}
