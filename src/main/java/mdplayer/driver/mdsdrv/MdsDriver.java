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

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.IDriver;
import musicDriverInterface.MetaData;
import musicDriverInterface.MmlDatum;
import vavi.util.compat.TriConsumer;
import vavi.util.compat.Tuple;

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

    public static final int opmBaseClock = 3579545;

    public MdsDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public MdsDriver() {
        this(null); // gross
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        MetaData metaData;

        mdsDriver = IDriver.factory("vavi.sound.mdsdrv.driver.MdsDriver");
        metaData = mdsDriver.getMetaData(buf);

        return metaData;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        metaData = getMetaData(dataBuf);

        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;

        initMds();
    }

    @Override
    public void processOneFrame() {

        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                mdsDriver.render();

                counter++;
                frameCounter++;
            }

            int lp = mdsDriver.getNowLoopCounter();
            lp = Math.max(lp, 0);
            curLoop = lp;

            // 0 = every track reached its finish command, < 0 = nothing loaded.
            // (vavi-sound-mdsdrv 0.4.7+; earlier versions stubbed this to a constant 0.)
            if (mdsDriver.getStatus() < 1) {
                stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void initMds() {
        if (mdsDriver == null) mdsDriver = new mucom88.driver.Driver();

        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : dataBuf) buf.add(new MmlDatum(b & 0xff));

        List<ChipAction> actions = new ArrayList<>();
        ChipAction action = new MdsChipAction(this::writeOPM1, null, null);
        actions.add(action);
        action = new MdsChipAction(this::writePSG, null, null);
        actions.add(action);
        mdsDriver.init(actions, buf.toArray(MmlDatum[]::new),null, plugin.playingFileName);

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

    @Override
    public String getName() {
        return "MDSDRV";
    }
}
