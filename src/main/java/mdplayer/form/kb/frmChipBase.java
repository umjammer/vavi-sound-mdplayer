package mdplayer.form.kb;

import java.awt.image.BufferedImage;

import mdsound.Instrument;
import mdsound.MDSound;
import mdplayer.FrameBuffer;
import mdplayer.ScreenPanel;
import mdplayer.form.frmBase;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


/**
 * What every chip panel is.
 * <p>
 * A chip panel is a skinned screen showing one chip's state: {@link #screenChangeParams()} pulls
 * that state out of the chip each frame, {@link #screenDrawParams()} blits what changed, and
 * {@link #update()} presents it. Each panel differs only in its skin, its parameters and what it
 * draws — everything else is the same, and lives here.
 *
 * @param <P> the chip's slice of {@link mdplayer.MDChipParams}, diffed new against old to decide
 *            what needs redrawing
 */
public class frmChipBase<P> extends frmBase {

    public boolean isClosed = false;
    public int x = -1;
    public int y = -1;
    protected int frameSizeW = 0;
    protected int frameSizeH = 0;
    protected int chipId = 0;
    protected int zoom = 1;

    protected P newParam = null;
    protected P oldParam = null;

    /** the screen this panel's skin and sprites are drawn into */
    protected final FrameBuffer frameBuffer = new FrameBuffer();

    /** the component that frame buffer is presented on */
    protected ScreenPanel pbScreen;

    public frmChipBase() {
    }

    public frmChipBase(frmMain frm, int chipId, int zoom, P newParam, P oldParam) {
        super(frm);
        parent = frm;
        this.chipId = chipId;
        this.zoom = zoom;
        this.newParam = newParam;
        this.oldParam = oldParam;
    }

    /**
     * Puts the panel on screen, once the subclass has built its components. Call at the end of the
     * constructor, with the skin this chip is drawn on.
     */
    protected final void bind(BufferedImage plane) {
        setIconImage(Resources.getFeli128());
        frameBuffer.Add(pbScreen, plane, null, zoom);
        screenInit();
        update();
    }

    /** Presents what {@link #screenDrawParams()} has drawn. */
    public void update() {
        frameBuffer.refresh(null);
    }

    /**
     * The clock of one of this panel's chips, or 0 when the song being played does not use it — a
     * panel can be open for a chip the current song has nothing to say about.
     */
    protected int clock(Class<? extends Instrument> instrument) {
        MDSound.Chip chip = audio.plugin.mds.getChipInfo(instrument);
        return chip == null ? 0 : chip.clock;
    }

    /** A chip panel never takes the focus off the main window. */
    protected boolean getShowWithoutActivation() {
        return true;
    }

    /** Reads this frame's chip state into {@link #newParam}. */
    public void screenChangeParams() {
    }

    /** Draws what changed between {@link #oldParam} and {@link #newParam}. */
    public void screenDrawParams() {
    }

    /** Draws the parts of the screen that never change. */
    public void screenInit() {
    }
}
