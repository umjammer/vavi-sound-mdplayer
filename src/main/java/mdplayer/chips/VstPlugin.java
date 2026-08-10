package mdplayer.chips;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.vst.VstInfo;
import mdplayer.vst.VstMng;
import mdplayer.vst.VstMng.VstInfo2;

import static java.lang.System.getLogger;


/**
 * VstPlugin.
 * <p>
 * The mixer's end of {@link VstMng}: the effect chain is rebuilt for each song here - which costs
 * nothing while the settings still name the same plug-ins - and {@link #update} is the point in
 * {@link mdplayer.Audio} where the finished mix is handed to them.
 * <p>
 * The plug-ins themselves are not unloaded between songs. {@link #close()} runs at the end of
 * every one, so it only quiets them; {@link #shutdown()} is what lets them go, on the way out of
 * the application.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public class VstPlugin implements Plugin {

    private static final Logger logger = getLogger(VstPlugin.class.getName());

    /**
     * How far into the block being rendered the driver has got, in frames.
     * <p>
     * Reset by {@link mdplayer.driver.BaseDriver#render} at the start of every block and counted
     * up once per frame by each driver's {@code processOneFrame}, so a driver that emits a MIDI
     * message knows the offset within the block it happens at - which is the one thing a VST
     * instrument needs to place a note anywhere other than on the block boundary.
     */
    public int vstDelta = 0;

    private final VstMng vstMng = new VstMng();

    public VstMng getManager() {
        return vstMng;
    }

    /** the effect chain, in the order it is applied */
    public List<VstInfo2> getEffects() {
        return vstMng.getEffects();
    }

    /** what a plug-in file says about itself, without keeping it loaded */
    public VstInfo getInfo(String fileName) {
        return vstMng.getInfo(fileName);
    }

    public boolean addEffect(String fileName) {
        return vstMng.addEffect(fileName);
    }

    public boolean removeEffect(String key) {
        return vstMng.removeEffect(key);
    }

    /** an instrument for a MIDI out to play through */
    public VstInfo2 acquireInstrument(String fileName) {
        return vstMng.acquireInstrument(fileName);
    }

    public void releaseInstruments() {
        vstMng.releaseInstruments();
    }

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        try {
            vstMng.setUpEffects();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "the VST effect chain could not be built", t);
        }
    }

    @Override
    public void close() {
        // called at the end of every song - the plug-ins stay loaded, they are only silenced
        vstMng.releaseInstruments();
    }

    /** unloads every plug-in and writes the chain back into the settings */
    public void shutdown() {
        vstMng.close();
    }

    /** mixes the instruments in and runs the result through the effect chain */
    public void update(short[] buffer, int offset, int sampleCount) {
        vstMng.update(buffer, offset, sampleCount);
    }
}
