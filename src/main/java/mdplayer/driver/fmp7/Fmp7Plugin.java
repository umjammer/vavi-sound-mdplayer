/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.fmp7;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.BasePlugin.Compilable;
import mdplayer.lib.fmp7.Fmp7Compiler;
import musicDriverInterface.MetaData;

import static java.lang.System.getLogger;


/**
 * FMP7 (".owi") Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-17 nsano initial version <br>
 */
public class Fmp7Plugin extends BasePlugin<Fmp7Driver> implements Compilable {

    private static final Logger logger = getLogger(Fmp7Plugin.class.getName());

    /**
     * What the MML said about itself, when this song was compiled from some.
     * <p>
     * The object the compiler makes says it too, but not in Japanese: the text it carries is
     * UTF-16, converted from the source by the emulated PC's ansi code page, which is a western
     * one - so a name written in kanji comes back out of the object as the bytes it was, one
     * character each. The source is the same text before any of that happened, so a song compiled
     * here is shown from its source rather than from what was made of it.
     */
    private MetaData sourceMetaData;

    /** what the MML said about itself, or null when this song was not compiled from any */
    public MetaData getSourceMetaData() {
        return sourceMetaData;
    }

    /**
     * Turns a ".mwi" into the ".owi" the driver plays, when that is what was opened.
     * <p>
     * The emulated PC is a JVM-wide singleton, so whatever was playing has to go first: the song
     * before this one is still on it until it is asked to stop, and the compiler cannot have a
     * machine while it does.
     */
    @Override
    public void compile() {
        if (!fileFormat.isMml()) {
            return;
        }
        if (driverVirtual != null) {
            driverVirtual.stopPlayer();
        }
        Fmp7Compiler compiler = new Fmp7Compiler();
        try {
            sourceMetaData = new Fmp7Driver().retrieveMetaData(dataBuf);
            dataBuf = compiler.compile(dataBuf, playingFilePath);
            playingFileName = fileFormat.getCompiledFilename();
            for (String problem : compiler.getProblems()) {
logger.log(Level.WARNING, problem);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("fmc7: " + fileFormat.getCompiledFilename(), e);
        }
    }

    @Override
    public void prepare() {
        compile();

        driverVirtual = new Fmp7Driver(this);

        driverReal = null;

        super.prepare();
        initChips();
    }

    @Override
    protected void initChips() {
        // FMP7 renders the audio itself, so no mdsound chip is registered

        driverVirtual.init(Common.EnmModel.VirtualModel,
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
    }

    /**
     * The emulated machine is a JVM-wide singleton and it holds a thread and a scratch directory,
     * so it has to go when the song does - the next song cannot start one until it has.
     */
    @Override
    public void stop() {
        if (driverVirtual != null) {
            driverVirtual.stopPlayer();
        }
        super.stop();
    }
}
