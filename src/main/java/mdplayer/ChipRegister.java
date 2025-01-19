
package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Plugin;
import mdsound.Instrument;

import static java.lang.System.getLogger;


public class ChipRegister {

    private static final Logger logger = getLogger(ChipRegister.class.getName());

    public final mdsound.MDSound mds;

    public final Map<Class<? extends Instrument>, mdsound.MDSound.Chip> dicChipsInfo = new HashMap<>();

    public RealChip realChip;

    public final ChipLEDs chipLED = new ChipLEDs();

    private final Map<Class<? extends Chip>, Chip> chips = new HashMap<>();

    private final Map<Class<? extends Plugin>, Plugin> plugins = new HashMap<>();

    public <T extends Chip> T chip(Class<T> clazz) {
        return clazz.cast(chips.get(clazz));
    }

    public <T extends Plugin> T plugin(Class<T> clazz) {
        return clazz.cast(plugins.get(clazz));
    }

    public ChipRegister(mdsound.MDSound mds) {
        this.mds = mds;

        dicChipsInfo.clear();

        for (Chip chip : ServiceLoader.load(Chip.class)) {
            chips.put(chip.getClass(), chip);
        }
logger.log(Level.INFO, "chips: " + chips.size());

        chips.values().forEach(c -> c.init(this));

        for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
            plugins.put(plugin.getClass(), plugin);
        }
logger.log(Level.INFO, "plugins: " + plugins.size());

        plugins.values().forEach(c -> c.init(this));
    }

    public void initChipRegister(mdsound.MDSound.Chip[] chipInfos) {

        dicChipsInfo.clear();
        if (chipInfos != null) {
            for (mdsound.MDSound.Chip c : chipInfos) {
                if (!dicChipsInfo.containsKey(c.instrument.getClass())) {
                    dicChipsInfo.put(c.instrument.getClass(), c);
                }
            }
        }
    }

    public void initChipRegisterNSF(mdsound.MDSound.Chip[] chipInfos) {

        dicChipsInfo.clear();
        if (chipInfos != null) {
            for (mdsound.MDSound.Chip c : chipInfos) {
                dicChipsInfo.put(c.instrument.getClass(), c);
            }
        }

        plugin(MidiPlugin.class).initChipRegisterNSF();

        chips.values().forEach(c -> c.init(this));
    }

    public mdsound.MDSound.Chip getChipInfo(Class<? extends Instrument> typ) {
        if (dicChipsInfo.containsKey(typ))
            return dicChipsInfo.get(typ);
        return null;
    }

    public void close() {
        plugins.values().forEach(Plugin::close);
    }

    public void resetChips() {
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
