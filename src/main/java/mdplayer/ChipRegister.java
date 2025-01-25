
package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import mdplayer.Common.EnmModel;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Plugin;
import mdplayer.chips.RealChipPlugin;

import static java.lang.System.getLogger;


// TODO could be merged into Audio
public class ChipRegister {

    private static final Logger logger = getLogger(ChipRegister.class.getName());

    // instruments wrapper
    private final Map<Class<? extends Chip>, Chip> chips = new HashMap<>();

    // plugins
    private final Map<Class<? extends Plugin>, Plugin> plugins = new HashMap<>();

    public <T extends Chip> T chip(Class<T> clazz) {
        return clazz.cast(chips.get(clazz));
    }

    public <T extends Plugin> T plugin(Class<T> clazz) {
        return clazz.cast(plugins.get(clazz));
    }

    private static final ServiceLoader<Chip> chipServiceLoader = ServiceLoader.load(Chip.class);
    private static final ServiceLoader<Plugin> pluginServiceLoader = ServiceLoader.load(Plugin.class);

    public ChipRegister() {
        // reused instance
        for (Chip chip : chipServiceLoader) {
            chips.put(chip.getClass(), chip);
        }
logger.log(Level.INFO, "chips: " + chips.size());

        // reused instance
        for (Plugin plugin : pluginServiceLoader) {
            plugins.put(plugin.getClass(), plugin);
        }
logger.log(Level.INFO, "plugins: " + plugins.size());
    }

    public void init(Audio context) {
        chips.values().forEach(c -> c.init(context));
        plugins.values().forEach(c -> c.init(context));
    }

    public void reset() {
        chips.values().forEach(Chip::reset);
    }

    public void softReset(EnmModel model) {
        chips.values().forEach(c -> c.softReset(model));
        plugin((MidiPlugin.class)).softReset(model);
        plugin((RealChipPlugin.class)).softReset(model);
    }

    public void clearFadeoutVolume() {
        chips.values().forEach(Chip::clearFadeout);
    }

    public void close() {
        plugins.values().forEach(Plugin::close);
    }

    //
    // Gets the volume for displaying the keyboard volume
    //

    private int volF = 1;

    /**
     * Update volume information
     */
    public void updateVol() {
        volF--;
        if (volF > 0)
            return;

        volF = 1;

        chips.values().forEach(Chip::updateVol);
    }
}
