package mdplayer.vst;

import java.awt.Point;
import java.io.Serializable;

import mdplayer.Setting;
import tools.jackson.databind.annotation.JsonDeserialize;


/**
 * What the settings remember about a VST plug-in between runs.
 *
 * @see VstMng.VstInfo2 the same thing plus what only exists while it is loaded
 */
public class VstInfo implements Serializable {
    public String key = "";
    public String fileName = "";
    public boolean power = false;
    public boolean editor = false;
    public String effectName = "";
    public String productName = "";
    public String vendorName = "";
    public String programName = "";
    /**
     * Where the plug-in's editor window was left.
     * <p>
     * A {@link Point} is written out as {@code <x>120.0</x>} - its accessors are doubles - and
     * will not read back into the int constructor without being told how, which fails the whole
     * settings file and not just this. Every other point in the settings is annotated the same way.
     */
    @JsonDeserialize(using = Setting.PointDeserializer.class)
    public Point location = new Point(0, 0);
    public float[] param = null;
    /**
     * The state of a plug-in that keeps its own, base64'd.
     * <p>
     * {@link #param} is the whole state of a plug-in whose parameters are its state, and nothing
     * at all of one that answers {@code effFlagsProgramChunks} - most modern ones - which hands
     * over an opaque block instead. Empty for the plug-ins that do not use chunks.
     */
    public String chunk = "";
    public int midiInputChannels = 0;
    public int midiOutputChannels = 0;
}
