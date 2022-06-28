
package mdplayer.vst;

import java.awt.Point;
import java.io.Serializable;


public class VstInfo implements Serializable {
    public String key = "";
    public String fileName = "";
    public boolean power = false;
    public boolean editor = false;
    public String effectName = "";
    public String productName = "";
    public String vendorName = "";
    public String programName = "";
    public Point location = new Point(0, 0);
    public float[] param = null;
    public int midiInputChannels = 0;
    public int midiOutputChannels = 0;
}
