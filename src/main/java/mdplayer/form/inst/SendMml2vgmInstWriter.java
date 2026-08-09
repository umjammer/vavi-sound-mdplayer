package mdplayer.form.inst;

import java.awt.Component;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.JOptionPane;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common.EnmInstFormat;
import mdplayer.form.MmfControl;

import static java.lang.System.getLogger;


/** sends the tone straight to a running mml2vgm via its shared-memory voice pool */
public class SendMml2vgmInstWriter implements InstWriter {

    private static final Logger logger = getLogger(SendMml2vgmInstWriter.class.getName());

    @Override
    public EnmInstFormat format() {
        return EnmInstFormat.SendMML2VGM;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {
        String n = Mml2vgmInstWriter.text(parent, audio, chip, ch, chipId);
        if (n == null || !n.isEmpty()) return;

        MmfControl mmf = new MmfControl(true, "mml2vgmFMVoicePool", 1024 * 4);
        try {
            mmf.sendMessage(String.join(":", "SendVoice", n));
        } catch (IndexOutOfBoundsException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            logger.log(Level.TRACE, "Message too long");
        } catch (UncheckedIOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            JOptionPane.showMessageDialog(parent, "Could not find shared memory for mml2vgm");
        }
    }
}
