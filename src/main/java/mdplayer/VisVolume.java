
package mdplayer;

import java.util.HashMap;
import java.util.Map;

import mdsound.Instrument;


public class VisVolume {

    public short master = 0;

    private Map<String, Integer> visVolumes;

    public int get(String key) {
        return visVolumes.get(key);
    }

    public void put(String key, int value) {
        visVolumes.put(key, value);
    }
}
