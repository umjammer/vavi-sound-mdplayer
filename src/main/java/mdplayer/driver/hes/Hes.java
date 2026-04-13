package mdplayer.driver.hes;

import mdplayer.driver.hes.M_Hes.NezPlay;


/** HES (PC-Engine) */
public class Hes {

    public static final int FCC_HES = 0x4d534548;  // "HESM"

    public int version;
    public int songs;
    public int start;
    public int load_address;
    public int init_address;
    public int play_address;
    public int song;

    public final NezPlay nezPlay;

    int last_out = 0;
    int silent_length;
    final HESDetector ld;
    double time_in_ms;
    public boolean playtime_detected;

    final int[] buf = new int[2];

    public Hes() {
        this.silent_length = 0;
        this.playtime_detected = false;

        this.ld = new HESDetector();
        this.ld.reset();

        this.nezPlay = new NezPlay();
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

        @Override
        public boolean isLooped(int time_in_ms, int match_second, int match_interval) {
            int i, j;
            int match_size, match_length;

            if (time_in_ms - currentTime < match_interval)
                return false;

            currentTime = time_in_ms;

            if (bIdx <= bLast)
                return false;
            if (wSpeed != 0)
                wSpeed = (wSpeed + bIdx - bLast) / 2;
            else
                wSpeed = bIdx - bLast; // First Time
            bLast = bIdx;

            match_size = wSpeed * match_second / match_interval;
            match_length = bufSize - match_size;

            if (match_length < 0)
                return false;

//            logger.log(Level.TRACE, "match_length:%d".formatted(match_length));
//            logger.log(Level.TRACE, "match_size  :%d".formatted(match_size));
            for (i = 0; i < match_length; i++) {
                for (j = 0; j < match_size; j++) {
                    if (streamBuf[(bIdx + j + match_length) & bufMask] !=
                            streamBuf[(bIdx + i + j) & bufMask]) {
                        break;
                    }
                }
                //logger.log(Level.TRACE, "j  :%d".formatted(j));
                if (j == match_size) {
                    loopStart = timeBuf[(bIdx + i) & bufMask];
                    loopEnd = timeBuf[(bIdx + match_length) & bufMask];
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getLoopStart() {
            return loopStart;
        }

        public int gtLoopEnd() {
            return loopEnd;
        }
    }
}
