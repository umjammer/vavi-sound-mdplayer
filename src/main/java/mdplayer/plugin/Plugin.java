package mdplayer.plugin;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import mdplayer.format.FileFormat;


public interface Plugin {

    void init();

    boolean play(String playingFileName, FileFormat format);

    void ff();

    void stop();

    void close();

    ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);

    static Plugin getPlugin(Class<? extends Plugin> clazz) {
        for (Plugin p : plugins) {
            if (p.getClass() == clazz) {
                return p;
            }
        }
        throw new NoSuchElementException(clazz.getName());
    }
}
