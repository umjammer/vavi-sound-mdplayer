
package mdplayer;

import java.util.Map;


public class VisVolume {

    public short master = 0;

    // TODO this class should be respond realtime, map might be slow.
    private Map<String, Integer> visVolumes;

    public int get(String key) {
        return visVolumes.get(key);
    }

    public void put(String key, int value) {
        visVolumes.put(key, value);
    }
}
