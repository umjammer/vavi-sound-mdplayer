/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.ChipRegister;
import mdplayer.Setting;


/**
 * Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-07 nsano initial version <br>
 */
public interface Plugin {

    Setting setting = Setting.getInstance();

    void init(ChipRegister context);

    void close();
}
