package mdplayer.driver.hes;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.HuC6280Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.hes.Hes;
import mdplayer.driver.BasePlugin;
import mdsound.MDSound;
import musicDriverInterface.MetaData;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * @author kumatan
 */
public class HesDriver extends BaseDriver {

    private static final Logger logger = getLogger(HesDriver.class.getName());

    private final Hes hes;

    public HesDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        hes = new Hes();
    }

    public HesDriver() {
        this(null); // gross
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        if (ByteUtil.readLeInt(buf, 0) != Hes.FCC_HES) {
            return null;
        }

        if (buf.length < 0x20) // no header?
            return null;

        hes.version = buf[0x04] & 0xff;
        hes.songs = 255;
        hes.start = (buf[0x05] & 0xff) + 1;
        hes.load_address = 0;
        hes.init_address = (buf[0x06] & 0xff) | ((buf[0x07] & 0xff) << 8);
        hes.play_address = 0;

        // There is almost no information on HES songs?
        return null;
    }

    /**
     * @param args 0: [int] song number
     */
    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {

        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        int song = (int) args[0];

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

        metaData = retrieveMetaData(dataBuf);

        if (hes.nezPlay.HESLoad(dataBuf, dataBuf.length) != 0)
            throw new IllegalArgumentException("invalid hes data");
        hes.nezPlay.heshes.freqency = Common.VGMProcSampleRate;
        hes.nezPlay.heshes.huC6280Write = (a, v) -> plugin.chipRegister.chip(HuC6280Chip.class).write(0, a & 0xf, v, EnmModel.VirtualModel);
        hes.nezPlay.heshes.ld = hes.ld;
        hes.nezPlay.song.songno = song + 1;
        hes.nezPlay.HESHESReset();
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    hes.nezPlay.executeHES();
                    counter++;
                } else {
                    frameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    public void additionalUpdate(MDSound.Chip sender, int chipId, int[][] buffer, int length) {
        if (plugin.isStopped()) {
            return;
        }
        try {
            for (int i = 0; i < length; i++) {

                int m = buffer[0][i] + buffer[1][i];
                if (m == hes.last_out && frameCounter >= 0) hes.silent_length++;
                else hes.silent_length = 0;
                hes.last_out = m;

                if (hes.nezPlay != null && hes.nezPlay.heshes != null) {
                    hes.buf[0] = 0;
                    hes.buf[1] = 0;
                    hes.nezPlay.heshes.synth(hes.buf);
                    buffer[0][i] += hes.buf[0];
                    buffer[1][i] += hes.buf[1];
                }
            }

            if (!hes.playtime_detected && hes.silent_length > setting.getOutputDevice().getSampleRate() * 3) {
                hes.playtime_detected = true;
                loopCounter = 0;
                stopped = true;
            }

            hes.time_in_ms += (1000 * length / (double) setting.getOutputDevice().getSampleRate() * speed);// ((* config)["MULT_SPEED"].GetInt()) / 256);
            if (!hes.playtime_detected && hes.ld.isLooped((int) hes.time_in_ms, 30000, 5000)) {
                hes.playtime_detected = true;
                totalCounter = (long) hes.ld.getLoopEnd() * (long) setting.getOutputDevice().getSampleRate() / 1000L;
                if (totalCounter == 0) totalCounter = counter;
                loopCounter = ((long) hes.ld.getLoopEnd() - (long) hes.ld.getLoopStart()) * (long) setting.getOutputDevice().getSampleRate() / 1000L;
            }

            if (!hes.playtime_detected) curLoop = 0;
            else {
                if (totalCounter != 0) curLoop = (int) (counter / totalCounter);
                else stopped = true;
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
