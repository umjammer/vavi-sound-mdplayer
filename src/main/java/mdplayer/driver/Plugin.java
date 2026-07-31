package mdplayer.driver;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;


public interface Plugin {

    void init();

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

    /** for SPI */
    BaseDriver getDriver();

    /**
     * prepare to play
     * <p>
     * <li>instantiate drivers</li>
     * <li>init chips</li>
     */
    void prepare();
}
