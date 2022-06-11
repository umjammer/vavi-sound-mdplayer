
package mdplayer;

public class Tone {
    public String name = "(NO NAME)";

    public int al = 0;
    public int fb = 0;
    public int ams = 0;
    public int pms = 0;

    public Op[] ops = new Op[] {
        new Op(), new Op(), new Op(), new Op()
    };

    public static class Op {
        public int ar = 0;
        public int dr = 0;
        public int sr = 0;
        public int rr = 0;
        public int sl = 0;
        public int tl = 127;
        public int ks = 0;
        public int ml = 0;
        public int dt = 0;
        public int am = 0;
        public int sg = 0;
        public int dt2 = 0;
    }
}
