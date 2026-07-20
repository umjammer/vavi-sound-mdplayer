package mdplayer.form.kb;

import java.util.Map;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.chips.BaseChip;
import mdplayer.chips.NpNesChip;


/**
 * How a provider reads its chip's output level for the mixer's meters — the common lookups behind
 * {@link ViewProvider#updateMeters}.
 */
public final class Meters {

    private Meters() {
    }

    /** one value of a chip's {@link BaseChip#getInfo} map, or null when it has none */
    public static Object chipInfo(Audio audio, Class<? extends Chip> chipClass, String key) {
        try {
            Chip chip = audio.plugin.chipRegister.chip(chipClass);
            if (!(chip instanceof BaseChip)) return null;
            Map<String, Object> info = ((BaseChip) chip).getInfo(0);
            if (info == null) return null;
            return info.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /** the loudest value in a chip's reported volume array (int[] or int[][]) */
    public static int maxVolume(Object volObj) {
        if (volObj instanceof int[] a) {
            int max = 0;
            for (int v : a) if (v > max) max = v;
            return max;
        } else if (volObj instanceof int[][] rows) {
            int max = 0;
            for (int[] row : rows) {
                if (row != null) {
                    for (int v : row) if (v > max) max = v;
                }
            }
            return max;
        }
        return 0;
    }

    /** a chip's overall output level, 0 when it reports none */
    public static int chipVolume(Audio audio, Class<? extends Chip> chipClass) {
        return maxVolume(chipInfo(audio, chipClass, "volume"));
    }

    /**
     * One NES-family level from whichever {@link NpNesChip} instance the song registered — the
     * family shares one emulator, so any of its chips answers for all of them ({@code index}:
     * 0 APU, 1 DMC, 2 FDS, 3 N163, 4 VRC6, 5 MMC5, 6 FME7, 7 VRC7). -1 when no NES chip is
     * around (the meter then keeps its last value, as the original did).
     */
    public static int npNesVolume(Audio audio, int index) {
        try {
            NpNesChip nes = npNesChip(audio);
            if (nes == null) return -1;
            return (int) nes.getInfo(index).get("volume");
        } catch (Exception e) {
            return -1;
        }
    }

    private static NpNesChip npNesChip(Audio audio) {
        Class<?>[] candidates = {
                NpNesChip.class, NpNesChip.DmcChip.class, NpNesChip.FdsChip.class,
                NpNesChip.N163Chip.class, NpNesChip.Vrc6Chip.class, NpNesChip.Mmc5Chip.class,
                NpNesChip.Fme7Chip.class, NpNesChip.Vrc7Chip.class,
        };
        for (Class<?> c : candidates) {
            try {
                @SuppressWarnings("unchecked")
                Chip chip = audio.plugin.chipRegister.chip((Class<? extends Chip>) c);
                if (chip instanceof NpNesChip nes) return nes;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
