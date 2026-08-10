package mdplayer.lib.hes;

import mdplayer.lib.hes.M_Hes.NezPlay;
import mdsound.np.LoopDetector;


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

    public int last_out = 0;
    public int silent_length;
    public final HESDetector ld;
    public double time_in_ms;
    public boolean playtime_detected;

    public final int[] buf = new int[2];

    public Hes() {
        this.silent_length = 0;
        this.playtime_detected = false;

        this.ld = new HESDetector();
        this.ld.reset();

        this.nezPlay = new NezPlay();
    }

    public static class HESDetector extends LoopDetector.BasicDetector {
        HESDetector() {
            super(18);
        }

        @Override
        public boolean write(int adr, int val, int id) {
            if (adr < 0x10) {
                return super.write(adr, val, id);
            }

            return false;
        }

        // isLooped was a verbatim copy of the base one, buffer index bug included; it now inherits

        @Override
        public int getLoopStart() {
            return loopStart;
        }

        public int gtLoopEnd() {
            return loopEnd;
        }
    }
}
