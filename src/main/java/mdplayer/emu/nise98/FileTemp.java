package mdplayer.emu.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getLogger;


public class FileTemp {

    private static final Logger logger = getLogger(FileTemp.class.getName());

    private final Map<String, byte[]> temp = new HashMap<>();
    public boolean saveCompiledFile;

    public FileTemp() {
    }

    public void writeTemp(String filename, byte[] data) {
        if (temp.containsKey(filename.toUpperCase())) {
            temp.remove(filename.toUpperCase());
        }
        temp.put(filename.toUpperCase(), data);

        if (!saveCompiledFile) return;

        try {
            Files.write(Path.of(filename), data);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public byte[] readTemp(String filename) {
        if (!temp.containsKey(filename.toUpperCase())) {
            return null;
        }
        return temp.get(filename.toUpperCase());
    }

    public boolean existTemp(String filename) {
        return temp.containsKey(filename.toUpperCase());
    }
}

