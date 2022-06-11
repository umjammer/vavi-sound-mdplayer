package mdplayer.driver.hes;

import java.util.Arrays;

import mdplayer.Audio;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.Log;


public class Hes extends BaseDriver {
    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        if (Common.getLE32(buf, 0) != FCC_HES) {
            return null;
        }

        if (buf.length < 0x20) // no header?
            return null;

        version = buf[0x04];
        songs = (byte) 255;
        start = (byte) (buf[0x05] + 1);
        load_address = 0;
        init_address = (short) (buf[0x06] | (buf[0x07] << 8));
        play_address = 0;

        // HESの曲情報はほぼ無い?
        return null;
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {

        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        if (model == EnmModel.RealModel) {
            stopped = true;
            vgmCurLoop = 9999;
            return true;
        }

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;
        silent_length = 0;
        playtime_detected = false;

        ld = new HESDetector();
        ld.reset();

        gd3 = getGD3Info(vgmBuf, 0);

        hes = new Hes();
        nez_play = new M_Hes.NEZ_PLAY();
        if (nez_play.HESLoad(vgmBuf, vgmBuf.length) != 0) return false;
        nez_play.heshes.chipRegister = chipRegister;
        nez_play.heshes.ld = ld;
        nez_play.song.songno = this.song + 1;
        nez_play.HESHESReset();
        return true;
    }

    @Override
    public void oneFrameProc() {
        if (hes == null) return;
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    nez_play.ExecuteHES();
                    counter++;
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            Log.forcedWrite(ex);
        }
    }

    public static final int FCC_HES = 0x4d534548;  // "HESM"

    public byte version;
    public byte songs;
    public byte start;
    public short load_address;
    public short init_address;
    public short play_address;
    public byte song;

    public Hes hes;
    public M_Hes.NEZ_PLAY nez_play;
    public mdsound.MDSound.Chip c6280;

    private int last_out = 0;
    private int silent_length = 0;
    private HESDetector ld = null;
    private double time_in_ms;
    public Boolean playtime_detected = false;

    private int[] buf = new int[2];

    public void additionalUpdate(mdsound.MDSound.Chip sender, byte chipID, int[][] Buffer, int Length) {
        if (Audio.isStopped()) {
            return;
        }
        try {
            for (int i = 0; i < Length; i++) {

                int m = Buffer[0][i] + Buffer[1][i];
                if (m == last_out && vgmFrameCounter >= 0) silent_length++;
                else silent_length = 0;
                last_out = m;

                if (nez_play != null && nez_play.heshes != null) {
                    buf[0] = 0;
                    buf[1] = 0;
                    nez_play.heshes.synth(buf);
                    Buffer[0][i] += buf[0];
                    Buffer[1][i] += buf[1];
                }
            }

            if (!playtime_detected && silent_length > setting.getOutputDevice().getSampleRate() * 3) {
                playtime_detected = true;
                loopCounter = 0;
                stopped = true;
            }

            time_in_ms += (1000 * Length / (double) setting.getOutputDevice().getSampleRate() * vgmSpeed);// ((* config)["MULT_SPEED"].GetInt()) / 256);
            if (!playtime_detected && ld.isLooped((int) time_in_ms, 30000, 5000)) {
                playtime_detected = true;
                totalCounter = (long) ld.getLoopEnd() * (long) setting.getOutputDevice().getSampleRate() / 1000L;
                if (totalCounter == 0) totalCounter = counter;
                loopCounter = ((long) ld.getLoopEnd() - (long) ld.getLoopStart()) * (long) setting.getOutputDevice().getSampleRate() / 1000L;
            }

            if (!playtime_detected) vgmCurLoop = 0;
            else {
                if (totalCounter != 0) vgmCurLoop = (int) (counter / totalCounter);
                else stopped = true;
            }
        } catch (Exception ex) {
            Log.write(String.format("Exception message:%s StackTrace:%s", ex.getMessage(), Arrays.toString(ex.getStackTrace())));
        }
    }

    public static class HESDetector extends mdsound.np.LoopDetector.BasicDetector {
        public HESDetector() {
            super(18);
        }

        @Override
        public boolean write(int adr, int val, int id) {
            if (adr < 0x10) {
                return super.write(adr, val, id);
            }

            return false;
        }

        public boolean isLooped(int time_in_ms, int match_second, int match_interval) {
            int i, j;
            int match_size, match_length;

            if (time_in_ms - currentTime < match_interval)
                return false;

            currentTime = time_in_ms;

            if (bIdx <= bLast)
                return false;
            if (wspeed != 0)
                wspeed = (wspeed + bIdx - bLast) / 2;
            else
                wspeed = bIdx - bLast; // 初回
            bLast = bIdx;

            match_size = wspeed * match_second / match_interval;
            match_length = bufSize - match_size;

            if (match_length < 0)
                return false;

//            System.err.println("match_length:%d", match_length);
//            System.err.println("match_size  :%d", match_size);
            for (i = 0; i < match_length; i++) {
                for (j = 0; j < match_size; j++) {
                    if (streamBuf[(bIdx + j + match_length) & bufMask] !=
                            streamBuf[(bIdx + i + j) & bufMask]) {
                        break;
                    }
                }
                //System.err.println("j  :{0}", j);
                if (j == match_size) {
                    loopStart = timeBuf[(bIdx + i) & bufMask];
                    loopEnd = timeBuf[(bIdx + match_length) & bufMask];
                    return true;
                }
            }
            return false;
        }

        public int getLoopStart() {
            return loopStart;
        }

        public int gtLoopEnd() {
            return loopEnd;
        }
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }
}
