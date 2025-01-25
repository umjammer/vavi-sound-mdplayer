
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
import mdsound.Instrument;
import mdsound.MDSound;

import static java.lang.System.getLogger;


// TODO could be merged into Audio
public class ChipRegister {

    private static final Logger logger = getLogger(ChipRegister.class.getName());

    public final MDSound mds;

    // selected instruments
    public final Map<Class<? extends Instrument>, MDSound.Chip> usedInstruments = new HashMap<>();

    // view
    public final ChipLEDs chipLED = new ChipLEDs();

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

    public ChipRegister(MDSound mds) {
        this.mds = mds;

        // reused instance
        for (Chip chip : chipServiceLoader) {
            chips.put(chip.getClass(), chip);
        }
logger.log(Level.INFO, "chips: " + chips.size());

        chips.values().forEach(c -> c.init(this));

        // reused instance
        for (Plugin plugin : pluginServiceLoader) {
            plugins.put(plugin.getClass(), plugin);
        }
logger.log(Level.INFO, "plugins: " + plugins.size());

        plugins.values().forEach(c -> c.init(this));
    }

    public void initChipRegister(mdsound.MDSound.Chip[] chipInfos) {

        usedInstruments.clear();
        if (chipInfos != null) {
            for (MDSound.Chip c : chipInfos) {
                if (!usedInstruments.containsKey(c.instrument.getClass())) {
                    usedInstruments.put(c.instrument.getClass(), c);
                }
            }
        }
    }

    // ???
    public void initChipRegisterNSF(MDSound.Chip[] chipInfos) {

        usedInstruments.clear();
        if (chipInfos != null) {
            for (MDSound.Chip c : chipInfos) {
                usedInstruments.put(c.instrument.getClass(), c);
            }
        }

        plugin(MidiPlugin.class).initChipRegisterNSF();
    }

    public MDSound.Chip getChipInfo(Class<? extends Instrument> typ) {
        if (usedInstruments.containsKey(typ))
            return usedInstruments.get(typ);
        return null;
    }

    public void resetChips() {
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
