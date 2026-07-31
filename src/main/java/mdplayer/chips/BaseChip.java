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
import java.util.Collections;
import java.util.Map;

import mdplayer.Chip;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
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

    // for ym chips TODO
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

    /** @return not null */
    public Map<String, Object> getInfo(int chipId) {
        return Collections.emptyMap();
    }

    protected void fireEventHappened(String name, Object... args) {
        if (context.getDriver() != null) context.getDriver().fireEventHappened(this, name, args);
    }

    protected void dumpData(mdplayer.Common.EnmModel model, String name, int adr, byte[] rom, int len) {
        if (model == mdplayer.Common.EnmModel.RealModel) return;
        if (!setting.getOther().getDumpSwitch()) return;

        try {
            Path fn = Path.of(setting.getOther().getDumpPath(), "%2$s_%3$s_%1$03d.bin".formatted(dumpCounter++, getClass().getSimpleName().replace("Chip", "_") + name, context.getDriver().metaData.getFirst(Tag.Title).replace("*", "").replace("?", "").replace(" ", "").replace("\"", "").replace("/", "")));
            try (OutputStream fs = Files.newOutputStream(fn)) {
                fs.write(rom, adr, len);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            // Ignore the error
        }
    }

    protected void setMask(int chipId, int ch, boolean mask, Object... args) {}

    public final void setMask(int chipId, int ch) {
        setMask(chipId, ch, true);
    }

    public final void resetMask(int chipId, int ch) {
        setMask(chipId, ch, false);
    }

    /** the panel/main-window view of whether a channel is muted; this array is the source of truth */
    protected boolean getMask(int chipId, int ch) { return false; }
}
