package mdplayer;

@Deprecated
public class MDChipParams {

    public int Cminutes = -1;
    public int Csecond = -1;
    public int Cmillisecond = -1;

    public int TCminutes = -1;
    public int TCsecond = -1;
    public int TCmillisecond = -1;

    public int LCminutes = -1;
    public int LCsecond = -1;
    public int LCmillisecond = -1;

    public int Master = -255;
    public int MasterVis = -255;
    public int MasterHover = -255;
    public int MasterDrag = -255;
    public int TimeLine = -255;
    public int TimeLineVis = -255;
    public int TimeLineHover = -255;
    public int TimeLineDrag = -255;

    // TODO this is just copy of each chip info, should be eliminated
    @Deprecated
    public static class Channel {

        public int pan = -1;
        public int panL = -1;
        public int panR = -1;
        public int pantp = -1;
        public int note = -1;
        public int volume = -1;
        public int volumeL = -1;
        public int volumeR = -1;
        public int volumeRL = -1;
        public int volumeRR = -1;
        public int flg16 = -1;
        public int srcFreq = -1;
        public int freq = -1;
        public int bank = -1;
        public int sadr = -1;
        public int eadr = -1;
        public int ladr = -1;
        public int leadr = -1;
        public int pcmMode = -1;
        public int pcmBuff = 0;
        public Boolean mask = false;
        public int slot = 0;
        public int tp = -1;
        public int kf = -1; // OPM only
        public int tn = 0; // Psg only
        public boolean ex = false; // OPN/2/a/B
        public int tntp = -1;
        public boolean dda = false; // OotakeHuC6280
        public boolean noise = false; // OotakeHuC6280
        public int nfrq = -1; // OotakeHuC6280
        public boolean loopFlg = false; // mdplayer.chips.YmZ280BChip.Params
        public int echo = -1;
        public int utp = 0;
        public int utl = 0;

        public int[] inst = new int[48];
        public final int[] typ = new int[48];
        public final boolean[] bit = new boolean[48];
        public short[] aryWave16bit;

        public Channel() {
            aryWave16bit = null;
            for (int i = 0; i < inst.length; i++) {
                inst[i] = -1;
                typ[i] = 0;
                bit[i] = false;
            }
        }
    }

    public static class VolumeInfo {

        public int Volume = -9999;
        public int VisVolume1 = -1;
        public int VisVolume2 = -1;
        public int VisVol2Cnt = 30;
    }

    public final VolumeInfo MasterVolume = new VolumeInfo();

    public final VolumeInfo GimicOPN = new VolumeInfo();
    public final VolumeInfo GimicOPNA = new VolumeInfo();
}
