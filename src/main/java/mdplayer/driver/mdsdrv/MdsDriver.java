/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.mdsdrv;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dotnet4j.util.compat.TriConsumer;
import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.MetaData;
import musicDriverInterface.IDriver;
import musicDriverInterface.MetaData.Tag;
import musicDriverInterface.MmlDatum;

import static java.lang.System.getLogger;


/**
 * MdsDrv (Mega Drive) Driver.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-01-08 nsano initial version <br>
 */
public class MdsDriver extends BaseDriver {

    private static final Logger logger = getLogger(MdsDriver.class.getName());

    private IDriver mdsDriver = null;

    private String PlayingFileName;

    public String getPlayingFileName() {
        return PlayingFileName;
    }

    public void setPlayingFileName(String value) {
        PlayingFileName = value;
    }

    public static final int opmBaseClock = 3579545;

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        MetaData metaData;

        mdsDriver = IDriver.factory("vavi.sound.mdsdrv.driver.MdsDriver");
        metaData = mdsDriver.getGD3TagInfo(buf);

        Vgm.Gd3 g = new Vgm.Gd3();
        g.trackName = metaData.getFirst(Tag.Title);
        g.trackNameJ = metaData.getFirst(Tag.TitleJ);
        g.composer = metaData.getFirst(Tag.Composer);
        g.composerJ = metaData.getFirst(Tag.ComposerJ);
        g.vgmBy = metaData.getFirst(Tag.Artist);
        g.converted = metaData.getFirst(Tag.ReleaseDate);

        return g;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     Class<? extends Chip>[] useChip, int latency, int waitTime, Object... args) {
        gd3 = getGD3Info(vgmBuf);

        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;

        initMds();
    }

    @Override
    public void processOneFrame() {

        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                mdsDriver.render();

                counter++;
                vgmFrameCounter++;
            }

            int lp = mdsDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            vgmCurLoop = lp;

            if (mdsDriver.getStatus() < 1) {
                if (mdsDriver.getStatus() == 0) {
                    Thread.sleep((int) (latency * 2.0)); // Wait for latency*2 until the actual voice is fully pronounced
                }
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void initMds() {
        if (mdsDriver == null) mdsDriver = new mucom88.driver.Driver();

        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : vgmBuf) buf.add(new MmlDatum(b & 0xff));

        List<ChipAction> actions = new ArrayList<>();
        ChipAction action = new MdsChipAction(this::writeOPM1, null, null);
        actions.add(action);
        action = new MdsChipAction(this::writePSG, null, null);
        actions.add(action);
        mdsDriver.init(actions, buf.toArray(MmlDatum[]::new),null,
                PlayingFileName);

        mdsDriver.startRendering(Common.VGMProcSampleRate, new Tuple<>("", opmBaseClock));
        mdsDriver.startMusic(0);
    }

    private void writeOPM1(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;

// logger.log(Level.INFO, "chipData: %02x, %02x, %02x".formatted(cd.port, cd.address, cd.data));
        plugin.chipRegister.chip(Ym2612Chip.class).write(0, cd.port, cd.address, cd.data, model, 0);
    }

    private void writePSG(ChipDatum cd) {
        if (cd == null) return;
        if (cd.address == -1) return;
        if (cd.data == -1) return;

// logger.log(Level.INFO, "chipData: %02x, %02x, %02x".formatted(cd.port, cd.address, cd.data));
        plugin.chipRegister.chip(Sn76489Chip.class).write(0, cd.data, model);
    }

    public static class MdsChipAction implements ChipAction {
        private final Consumer<ChipDatum> write;
        private final TriConsumer<byte[], Integer, Integer> writePCMData;
        private final BiConsumer<Long, Integer> sendWait;

        public MdsChipAction(Consumer<ChipDatum> write, TriConsumer<byte[], Integer, Integer> writePCMData, BiConsumer<Long, Integer> sendWait) {
            this.write = write;
            this.writePCMData = writePCMData;
            this.sendWait = sendWait;
        }

        @Override
        public String getChipName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void waitSend(long t1, int t2) {
            sendWait.accept(t1, t2);
        }

        @Override
        public void writePCMData(byte[] data, int startAddress, int endAddress) {
            writePCMData.accept(data, startAddress, endAddress);
        }

        @Override
        public void writeRegister(ChipDatum cd) {
            write.accept(cd);
        }
    }
}
