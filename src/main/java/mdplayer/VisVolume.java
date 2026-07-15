
package mdplayer;

import java.util.HashMap;
import java.util.Map;


/**
 * The levels the mixer's meters show.
 * <p>
 * {@code "master"} is fed from the rendered wave. The per-chip levels have no source yet: a chip
 * would have to report its own output level for them, and none does.
 */
public class VisVolume {

    public short master = 0;

    // TODO this class should be respond realtime, map might be slow.
    private final Map<String, Integer> visVolumes = new HashMap<>();

    /** A meter nothing feeds reads zero, rather than throwing. */
    public int get(String key) {
        return visVolumes.getOrDefault(key, 0);
    }

    public void put(String key, int value) {
        visVolumes.put(key, value);
    }
}
