package mdplayer.form;

import javax.swing.JFrame;


/**
 * One openable window of the player, seen the way {@link mdplayer.form.sys.FormMain} drives it:
 * placed when opened, polled every frame for state, redrawn, and asked whether the user closed it.
 * {@link mdplayer.form.kb.ViewProvider} is how a view and its metadata are found; this is what the
 * provider's form has to be able to do.
 */
public interface View {

    /** this view as the window it is shown in */
    default JFrame frame() {
        return (JFrame) this;
    }

    /** has the user closed this window? a closed view is dropped and recreated on next open */
    boolean isClosed();

    /** the position the window will take when it is shown */
    void setDefaultLocation(int x, int y);

    /** presents what {@link #drawScreenParams()} has drawn */
    void update();

    /** reads this frame's state into the view's draw parameters */
    default void changeScreenParams() {
    }

    /** draws what changed since the last frame */
    default void drawScreenParams() {
    }

    /** draws the parts of the screen that never change */
    default void initScreen() {
    }
}
