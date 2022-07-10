package mdplayer.chips;

import mdplayer.Setting;


/**
 * Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public abstract class Plugin {

    protected Setting setting;

    abstract void init();
}
