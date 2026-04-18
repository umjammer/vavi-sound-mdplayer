/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * Nsf Driver powered by NsfPlayer.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-04-01 nsano initial version <br>
 */
public class NsfMdDriver2 extends BaseDriver implements NsfDriver {

    private static final Logger logger = getLogger(NsfMdDriver2.class.getName());

    private final Nsf2 nsf;

    public NsfMdDriver2() {
        this.nsf = new Nsf2();
        nsf.charset = Common.charset;
        nsf.sampleRate = setting.getOutputDevice().getSampleRate();
    }

    @Override
    public int getSongs() {
        return nsf.songs;
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (ByteUtil.readLeInt(buf, 0) != Nsf2.FCC_NSF) {
            // NSFe is not supported for now
            logger.log(Level.WARNING, "NSFe not supported.");
            return null;
        }

        if (buf.length < 0x80) { // no header?
            logger.log(Level.WARNING, "no header?");
            return null;
        }

        nsf.initInfo(buf);

        MetaData md = new MetaData();
        md.set(Tag.GameTitle, nsf.title);
        md.set(Tag.GameTitleJ, nsf.title);
        md.set(Tag.Composer, nsf.artist);
        md.set(Tag.ComposerJ, nsf.artist);
        md.set(Tag.Title, nsf.title);
        md.set(Tag.TitleJ, nsf.title);
        md.set(Tag.GameSystem, nsf.copyright);
        md.set(Tag.GameSystemJ, nsf.copyright);
        md.set(Tag.NumberOfSongs, String.valueOf(nsf.songs));

        return md;
    }

    @Override
    public void init(byte[] dataBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.dataBuf = dataBuf;
        this.plugin = plugin;
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        if (model == EnmModel.RealModel) {
            stopped = true;
            curLoop = 9999;
            return;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;
        speedCounter = 0;

        metaData = getMetaData(dataBuf);

        nsf.init(dataBuf);
    }

    @Override
    public void setSong(int songNo) {
        nsf.song = songNo;
    }

    @Override
    public boolean useFds() {
        return nsf.useFds;
    }

    @Override
    public boolean useFme7() {
        return nsf.useFme7;
    }

    @Override
    public boolean useMmc5() {
        return nsf.useMmc5;
    }

    @Override
    public boolean useN106() {
        return nsf.useN106;
    }

    @Override
    public boolean useVrc6() {
        return nsf.useVrc6;
    }

    @Override
    public boolean useVrc7() {
        return nsf.useVrc7;
    }

    @Override
    public void processOneFrame() {
    }

int CC;
static final int INTERVAL = 1024;

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
        // TODO loop, silent detector
        short[] b = new short[sampleCount / 2];
        int r = nsf.renderer.render(b, offset, sampleCount / 2);
        for (int i = 0; i < r; i++) {
            buffer[i * 2 + 0] = b[i];
            buffer[i * 2 + 1] = b[i];
if (CC++ % INTERVAL == 0) { logger.log(Level.DEBUG, "NSF: %d, %d".formatted(b[i], b[i])); }
        }
        return r * 2;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        visWB.copy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }

    private final mdsound.VisWaveBuffer visWB = new mdsound.VisWaveBuffer();
}
