package mdplayer.form.kb.chip;

import java.awt.image.BufferedImage;

import mdplayer.Common;
import mdplayer.form.FrameBuffer;
import mdplayer.form.ScreenPanel;
import mdplayer.form.FormBase;
import mdplayer.form.View;
import mdplayer.form.sys.FormMain;
import mdsound.Instrument;
import mdsound.MDSound;


/**
 * What every chip panel is.
 * <p>
 * A chip panel is a skinned screen showing one chip's state: {@link #changeScreenParams()} pulls
 * that state out of the chip each frame, {@link #drawScreenParams()} blits what changed, and
 * {@link #update()} presents it. Each panel differs only in its skin, its parameters and what it
 * draws — everything else is the same, and lives here.
 *
 * @param <P> this panel's draw state (its own nested {@code Params} class), diffed new against
 *            old to decide what needs redrawing
 */
public abstract class FormChipBase<P> extends FormBase implements View {

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

    protected static final double LOG2_440 = 8.7813597135246596040696824762152;
    protected static final double LOG_2 = 0.69314718055994530941723212145818;
    protected static final int NOTE_440HZ = 12 * 4 + 9;

    public FormChipBase() {
    }

    public FormChipBase(FormMain frm, int chipId, int zoom, P newParam, P oldParam) {
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
        setIconImage(Common.getImage("Feli128"));
        frameBuffer.add(pbScreen, plane, null, zoom);
        initScreen();
        update();
    }

    /** Presents what {@link #drawScreenParams()} has drawn. */
    @Override
    public void update() {
        frameBuffer.refresh(null);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void setDefaultLocation(int x, int y) {
        this.x = x;
        this.y = y;
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
    @Override
    public void changeScreenParams() {
    }

    /** Draws what changed between {@link #oldParam} and {@link #newParam}. */
    @Override
    public void drawScreenParams() {
    }

    /** Draws the parts of the screen that never change. */
    @Override
    public void initScreen() {
    }
}
