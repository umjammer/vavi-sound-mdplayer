package mdplayer.driver.fmp.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import mdplayer.Setting;

import static java.lang.System.getLogger;


public class FileTemp {

    private static final Logger logger = getLogger(FileTemp.class.getName());

    private Map<String, byte[]> temp = new HashMap<>();
    private Setting setting = Setting.getInstance();

    public FileTemp(Setting setting) {
        this.setting = setting;
    }

    public void WriteTemp(String filename, byte[] data) {
        if (temp.containsKey(filename.toUpperCase())) {
            temp.remove(filename.toUpperCase());
        }
        temp.put(filename.toUpperCase(), data);

        if (!setting.getOther().getSaveCompiledFile()) return;

        try {
            Files.write(Path.of(filename), data);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public byte[] ReadTemp(String filename) {
        if (!temp.containsKey(filename.toUpperCase())) {
            return null;
        }
        return temp.get(filename.toUpperCase());
    }

    public boolean ExistTemp(String filename) {
        return temp.containsKey(filename.toUpperCase());
    }
}

