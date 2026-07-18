package mdplayer.form.inst;

import java.awt.Component;
import java.util.ServiceLoader;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmInstFormat;


/**
 * Writes the tone (instrument definition) of one chip channel as text for one composition
 * environment — to the clipboard, a file, or an editor's shared memory. One writer per format,
 * found via {@link ServiceLoader}: {@link #of} answers for the user-selectable formats, and a
 * chip whose provider insists on a format of its own (OPLL and friends) calls its writer
 * directly.
 */
public interface InstWriter {

    /** the user-selectable format this writes, or null when only a chip's provider calls it */
    default EnmInstFormat format() {
        return null;
    }

    /** writes the tone of one chip channel */
    void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId);

    /** the writer registered for this format, or null when no writer claims it */
    static InstWriter of(EnmInstFormat format) {
        for (InstWriter w : Holder.writers) {
            if (w.format() == format) return w;
        }
        return null;
    }

    /** loads once */
    class Holder {

        private Holder() {
        }

        private static final ServiceLoader<InstWriter> writers = ServiceLoader.load(InstWriter.class);
    }
}
