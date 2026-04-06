
package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import mdplayer.Common.EnmModel;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Plugin;
import mdplayer.chips.RealChipPlugin;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;

import static java.lang.System.getLogger;


//
public class ChipRegister {

    private static final Logger logger = getLogger(ChipRegister.class.getName());

    /** all instruments wrappers */
    private final Map<Class<? extends Chip>, Chip> chips = new HashMap<>();

    /** all plugins */
    private final Map<Class<? extends Plugin>, Plugin> plugins = new HashMap<>();

    /** @return nullable */
    public <T extends Chip> T chip(Class<T> clazz) {
        return clazz.cast(chips.getOrDefault(clazz, null));
    }

    public <T extends Chip> boolean contains(Class<T> clazz) {
        return chips.containsKey(clazz);
    }

    public Set<Class<? extends Chip>> chips() {
        return chips.keySet();
    }

    /** @return nullable */
    public <T extends Plugin> T plugin(Class<T> clazz) {
        return clazz.cast(plugins.getOrDefault(clazz, null));
    }

    /** for reuse instances */
    private static final ServiceLoader<Chip> chipServiceLoader = ServiceLoader.load(Chip.class);

    /** for reuse instances */
    private static final ServiceLoader<Plugin> pluginServiceLoader = ServiceLoader.load(Plugin.class);

    /** */
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

    /** for all chips and plugins */
    public void init(BasePlugin<? extends BaseDriver> context) {
        chips.values().forEach(c -> c.init(context));
        plugins.values().forEach(c -> c.init(context));
    }

    /** for all chips */
    public void reset() {
        chips.values().forEach(Chip::reset);
    }

    /** for all chips and plugins */
    public void softReset(EnmModel model) {
        chips.values().forEach(c -> c.softReset(model));
        plugin((MidiPlugin.class)).softReset(model);
        plugin((RealChipPlugin.class)).softReset(model);
    }

    /** for all chips */
    public void clearFadeoutVolume() {
        chips.values().forEach(Chip::clearFadeout);
    }

    /** for all chips */
    public void close() {
        plugins.values().forEach(Plugin::close);
    }

    //
    // Gets the volume for displaying the keyboard volume
    //

    private int volF = 1;

    /**
     * Update volume information
     * for all chips
     */
    public void updateVol() {
        volF--;
        if (volF > 0)
            return;

        volF = 1;

        chips.values().forEach(Chip::updateVol);
    }
}
