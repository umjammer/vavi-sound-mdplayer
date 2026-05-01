/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;

import mdplayer.Chip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData.Tag;


/**
 * BaseChip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-13 nsano initial version <br>
 */
public abstract class BaseChip implements Chip {

    private static final Logger logger = System.getLogger(BaseChip.class.getName());

    protected BasePlugin<? extends BaseDriver> context;

    protected int dumpCounter = 0;

    protected Setting setting = Setting.getInstance();

    // for ym chips
    protected byte[] algM = {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};

    @Override
    public void init(BasePlugin<? extends BaseDriver> context) {
        this.context = context;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateVol() {
    }

    protected void dumpData(mdplayer.Common.EnmModel model, String chipName, int adr, byte[] rom, int len) {
        if (model == mdplayer.Common.EnmModel.RealModel) return;
        if (!setting.getOther().getDumpSwitch()) return;

        try {

            Path fn = Path.of(setting.getOther().getDumpPath(), "%2$s_%3$s_%1$03d.bin".formatted(dumpCounter++, chipName, context.getDriver().metaData.getFirst(Tag.Title).replace("*", "").replace("?", "").replace(" ", "").replace("\"", "").replace("/", "")));
            try (OutputStream fs = Files.newOutputStream(fn)) {
                fs.write(rom, adr, len);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            // Ignore the error
        }
    }
}
