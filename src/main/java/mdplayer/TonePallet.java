
package mdplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.Path;
import vavi.util.serdes.JacksonXMLBeanBinder;
import vavi.util.serdes.Serdes;

import static java.lang.System.getLogger;


@Serdes(beanBinder = JacksonXMLBeanBinder.class)
public class TonePallet implements Serializable {

    private static final Logger logger = getLogger(TonePallet.class.getName());

    private List<Tone> _lstTone = new ArrayList<>(256);

    public List<Tone> getLstTone() {
        return _lstTone;
    }

    public void setLstTone(List<Tone> value) {
        _lstTone = value;
    }

    public TonePallet copy() {
        TonePallet TonePallet = new TonePallet();

        return TonePallet;
    }

    public void save(String fileName) {
        String fullPath;

        if (fileName == null || fileName.isEmpty()) {
            fullPath = Common.settingFilePath;
            fullPath = Path.getFullPath(fullPath + "DefaultTonePallet.xml");
        } else {
            fullPath = fileName;
        }

        try (OutputStream sw = Files.newOutputStream(Paths.get(fullPath))) {
            Serdes.Util.serialize(sw, this);
        } catch (IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    public static TonePallet load(String fileName) {
        try {
            String fullPath;
            if (fileName == null || fileName.isEmpty()) {
                fullPath = Common.settingFilePath;
                fullPath = Path.getFullPath(fullPath + "DefaultTonePallet.xml").replace('\\', File.separatorChar);
            } else {
                fullPath = fileName;
            }

            try (InputStream sr = Files.newInputStream(Paths.get(fullPath))) {
                TonePallet pl = Serdes.Util.deserialize(sr, new TonePallet());
                return pl;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return new TonePallet();
        }
    }
}
