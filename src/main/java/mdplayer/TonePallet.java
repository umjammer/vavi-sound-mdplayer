
package mdplayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import vavi.util.serdes.JacksonXMLBeanBinder;
import vavi.util.serdes.Serdes;

import static java.lang.System.getLogger;


@Serdes(beanBinder = JacksonXMLBeanBinder.class)
public class TonePallet implements Serializable, Cloneable {

    private static final Logger logger = getLogger(TonePallet.class.getName());

    public static final String DEFAULT_TONE_PALLET_XML = "DefaultTonePallet.xml";

    private List<Tone> _lstTone = new ArrayList<>(256);

    public List<Tone> getLstTone() {
        return _lstTone;
    }

    public void setLstTone(List<Tone> value) {
        _lstTone = value;
    }

    @Override
    public TonePallet clone() {
        TonePallet TonePallet = new TonePallet();

        return TonePallet;
    }

    public void save(String fileName) {
        Path fullPath;

        if (fileName == null || fileName.isEmpty()) {
            fullPath = Common.settingFilePath;
            fullPath = fullPath != null ? fullPath.resolve(DEFAULT_TONE_PALLET_XML) : Path.of(DEFAULT_TONE_PALLET_XML);
        } else {
            fullPath = Path.of(fileName);
        }

        try (OutputStream sw = Files.newOutputStream(fullPath)) {
            Serdes.Util.serialize(this, sw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static TonePallet load(String fileName) {
        try {
            Path fullPath;
            if (fileName == null || fileName.isEmpty()) {
                fullPath = Common.settingFilePath;
                fullPath = fullPath.resolve(DEFAULT_TONE_PALLET_XML);
            } else {
                fullPath = Path.of(fileName);
            }

            try (InputStream sr = Files.newInputStream(fullPath)) {
                TonePallet pl = Serdes.Util.deserialize(sr, new TonePallet());
                return pl;
            }
        } catch (NoSuchFileException e) {
            logger.log(Level.ERROR, e.toString());
            return new TonePallet();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return new TonePallet();
        }
    }
}
