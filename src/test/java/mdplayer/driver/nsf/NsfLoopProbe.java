/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.driver.nsf;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdplayer.lib.nsf.Nsf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Checks that an NSF reports its loop, and prints what the loop detector saw.
 * <p>
 * {@code render()} is driven in chunks of two samples, the way {@code Audio.play()} and
 * {@code Md2PcmAudioInputStream} really call it - that is what used to keep the song playing
 * forever, so a big test buffer would hide the regression.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 */
public class NsfLoopProbe {

    static final String file = System.getProperty("nsf",
            "../simplevgm/tmp/Thunder Force IV - Sand Hell [5-N163].nsf");

    static final int songNo = Integer.getInteger("nsf.song", 0);

    /** seconds of audio to render before giving up */
    static final int duration = Integer.getInteger("nsf.duration", 150);

    /** shorts per render() call: two samples, as the players do */
    static final int bufferSize = Integer.getInteger("nsf.buffer", 4);

    static boolean nsfExists() {
        return Files.exists(Path.of(file));
    }

    @Test
    @EnabledIf("nsfExists")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
    @SuppressWarnings("unchecked")
    public void probe() throws Exception {
        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(file);
        format.load(Files.newInputStream(Path.of(file)), null);
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", file, "songNo", songNo));
        plugin.prepare();

        NsfMdDriver driver = (NsfMdDriver) plugin.getDriver();
        Nsf nsf = driver.nsf;

        Field ldF = Nsf.class.getDeclaredField("ld");
        ldF.setAccessible(true);
        Object ld = ldF.get(nsf);
        Class<?> bd = ld.getClass().getSuperclass(); // BasicDetector
        Field bIdxF = bd.getDeclaredField("bIdx");
        Field wSpeedF = bd.getDeclaredField("wSpeed");
        Field bufSizeF = bd.getDeclaredField("bufSize");
        Field timeF = Nsf.class.getDeclaredField("time_in_ms");
        Field silentF = Nsf.class.getDeclaredField("silent_length");
        for (Field f : new Field[] {bIdxF, wSpeedF, bufSizeF, timeF, silentF}) f.setAccessible(true);

        int rate = setting.getOutputDevice().getSampleRate();
        System.err.printf("file=%s song=%d rate=%d bufSize=%d chunk=%d shorts%n",
                file, songNo, rate, bufSizeF.getInt(ld), bufferSize);

        short[] buf = new short[bufferSize];
        long samples = 0;
        long nextReport = 0;
        long total = (long) rate * duration;
        boolean ended = false;
        while (samples < total) {
            driver.render(buf, 0, buf.length);
            samples += buf.length / 2;
            if (samples >= nextReport) {
                nextReport += (long) rate * 10;
                System.err.printf("t=%6.1fs time_in_ms=%7d bIdx=%9d wSpeed=%7d silent=%6d curLoop=%d stopped=%s%n",
                        samples / (double) rate, timeF.getInt(nsf), bIdxF.getLong(ld), wSpeedF.getInt(ld),
                        silentF.getLong(nsf), driver.curLoop, driver.stopped);
            }
            if (driver.stopped || driver.curLoop > 0) {
                System.err.printf(">>> ended at %.1fs curLoop=%d stopped=%s totalCounter=%d loopCounter=%d%n",
                        samples / (double) rate, driver.curLoop, driver.stopped,
                        driver.totalCounter, driver.loopCounter);
                analyze(ld, bd, bIdxF.getLong(ld), wSpeedF.getInt(ld));
                ended = true;
                break;
            }
        }
        plugin.stop();
        plugin.close();

        assertTrue(ended, "no loop detected within " + duration + "s: the song would play forever");
    }

    /** replays the detector's own matching loop to show what it actually matched */
    static void analyze(Object ld, Class<?> bd, long bIdx, int wSpeed) throws Exception {
        Field sbF = bd.getDeclaredField("streamBuf");
        Field tbF = bd.getDeclaredField("timeBuf");
        Field bufSizeF = bd.getDeclaredField("bufSize");
        Field maskF = bd.getDeclaredField("bufMask");
        for (Field f : new Field[] {sbF, tbF, bufSizeF, maskF}) f.setAccessible(true);
        int[] sb = (int[]) sbF.get(ld);
        int[] tb = (int[]) tbF.get(ld);
        int bufSize = bufSizeF.getInt(ld);
        int mask = maskF.getInt(ld);

        int matchSize = wSpeed * 30000 / 5000;
        int matchLength = bufSize - matchSize;
        System.err.printf("bIdx=%d wSpeed=%d matchSize=%d matchLength=%d sigStart=%d%n",
                bIdx, wSpeed, matchSize, matchLength, (bIdx + matchLength) & mask);
        for (int i = 0; i < matchLength; i++) {
            int j;
            for (j = 0; j < matchSize; j++)
                if (sb[(int) ((bIdx + j + matchLength) & mask)] != sb[(int) ((bIdx + i + j) & mask)]) break;
            if (j == matchSize) {
                long pos = (bIdx + i) & mask;
                System.err.printf("MATCH pos=%d period=%d writes loopStart=%dms loopEnd=%dms%n",
                        pos, ((bIdx + matchLength) & mask) - pos,
                        tb[(int) pos], tb[(int) ((bIdx + matchLength) & mask)]);
                return;
            }
        }
        System.err.println("no match on replay (ended by silence?)");
    }
}
