package mdplayer.driver.hes;

import mdplayer.driver.hes.S_Deltat.Kmif_LogTable;
import mdplayer.driver.hes.S_Deltat.YmDeltaTPcmSound_;


public class S_Hesad extends KmifSoundDevice {

    private static final int CPS_SHIFT = 16;
    private static final int PCE_VOLUME = 1;  // 1
    private static final int ADPCM_VOLUME = 50;

    static class HesAdpcm {

        public KmifSoundDevice kmif;
        public KmifSoundDevice deltaDev;

        public static class Common {
            public int masterVolume;
            public int cps;
            public int pt;
        }

        public final Common common = new Common();

        public byte[] pcmBuf = new byte[0x10000];
        public byte[] port = new byte[0x10];
        public final byte[] regs = new byte[0x18];
        public int outFreq;
        public int freq;
        public int addr;
        public int writePtr;
        public int readPtr;
        public int playFlag;
        public int repeatFlag;
        public int length;
        public int volume;
        public int fadeTimer;
        public int fadeCount;

        private void reset() {
            this.addr = 0;
            this.freq = 0;
            this.writePtr = 0;
            this.readPtr = 0;
            this.playFlag = 0;
            this.repeatFlag = 0;
            this.length = 0;
            this.volume = 0xff;
            this.deltaDev.write.accept(0, 1);
        }
    }

    private void sndsynth(int[] p) {
        HesAdpcm sndp = (HesAdpcm) ctx;
        int[] pbf = new int[2];
        pbf[0] = 0;
        pbf[1] = 0;

        // At this point, rendering of the built-in sound source has already been completed.
        p[0] = p[0] * PCE_VOLUME;
        p[1] = p[1] * PCE_VOLUME;

//        sndp.deltaDev.synth.accept(pbf); // TODO vavi

        sndp.common.pt += sndp.common.cps;

         // 1ms
        while (sndp.common.pt > 100000) {
            sndp.common.pt -= 100000;

            if (sndp.fadeCount > 0 && sndp.fadeTimer != 0) {
                sndp.fadeCount--;
                sndp.volume = 0xff * sndp.fadeCount / sndp.fadeTimer;
            }
            if (sndp.fadeCount < 0 && sndp.fadeTimer != 0) {
                sndp.fadeCount++;
                sndp.volume = 0xff - (0xff * sndp.fadeCount / sndp.fadeTimer);
            }

        }
        // if(sndp->common.pt > 500)p[0]+=80000;
        p[0] += (pbf[0] * ADPCM_VOLUME * sndp.volume / 0xff);
        p[1] += (pbf[1] * ADPCM_VOLUME * sndp.volume / 0xff);
    }

    private void sndreset(int clock, int freq) {
        HesAdpcm sndp = (HesAdpcm) ctx;
        //XMEMSET(&sndp.pcmBuf, 0, sizeof(sndp.pcmBuf));
        sndp.pcmBuf = new byte[0x10000];
        //XMEMSET(&sndp.port, 0, sizeof(sndp.port));
        sndp.port = new byte[0x10];
        sndp.reset();
        sndp.outFreq = freq;
        sndp.fadeTimer = 0;
        sndp.fadeCount = 0;
        sndp.common.cps = 100000000 / freq;
        sndp.common.pt = 0;
        sndp.volume = 0xff;
//        sndp.deltaDev.reset.accept(clock, freq); // TODO vavi: self recursion
        sndp.deltaDev.write.accept(1, 0);
        sndp.deltaDev.write.accept(0xb, 0xff);
        //sndp.deltaDev.setInst(sndp.deltaDev,0,sndp->pcmBuf,0x100);
    }

    private void sndwrite(int a, int v) {
        HesAdpcm sndp = (HesAdpcm) ctx;
        sndp.port[a & 15] = (byte) v;
        sndp.regs[a & 15] = (byte) v;
        switch (a & 15) {
        case 0x8:
            // port low
            sndp.addr &= 0xff00;
            sndp.addr |= v;
            break;
        case 0x9:
            // port high
            sndp.addr &= 0xff;
            sndp.addr |= v << 8;
            break;
        case 0xA:
            // write buffer
            sndp.pcmBuf[sndp.writePtr++] = (byte) v;
            break;
        case 0xB:
            // DMA busy?
            break;
        case 0xC:
            break;
        case 0xD:
            if ((v & 0x80) != 0) {
                // reset
                sndp.reset();
            }
            if ((v & 0x03) == 0x03) {
                // set write pointer
                sndp.writePtr = sndp.addr;
                sndp.regs[0x10] = (byte) (sndp.writePtr & 0xff);
                sndp.regs[0x11] = (byte) (sndp.writePtr >> 8);
            }
            if ((v & 0x08) != 0) {
                // set read pointer
                sndp.readPtr = sndp.addr != 0 ? sndp.addr - 1 : sndp.addr;
                sndp.regs[0x12] = (byte) (sndp.readPtr & 0xff);
                sndp.regs[0x13] = (byte) ((sndp.readPtr >> 8) & 0xff);
            }
            if ((v & 0x10) != 0) {
                sndp.length = sndp.addr;
                sndp.regs[0x14] = (byte) (sndp.length & 0xff);
                sndp.regs[0x15] = (byte) ((sndp.length >> 8) & 0xff);
            }
            sndp.repeatFlag = ((v & 0x20) == 0x20) ? 1 : 0;
            sndp.playFlag = ((v & 0x40) == 0x40) ? 1 : 0;
            if (sndp.playFlag != 0) {
                sndp.deltaDev.write.accept(2, sndp.readPtr & 0xff);
                sndp.deltaDev.write.accept(3, (sndp.readPtr >> 8) & 0xff);
                sndp.deltaDev.write.accept(4, (sndp.length + sndp.readPtr) & 0xff);
                sndp.deltaDev.write.accept(5, ((sndp.length + sndp.readPtr) >> 8) & 0xff);
                sndp.deltaDev.write.accept(0, 1);
                sndp.deltaDev.write.accept(0, (0x80 | (sndp.repeatFlag >> 1)));
            }
            break;
        case 0xE:
            // set freq
            sndp.freq = 7111 / (16 - (v & 15));
            sndp.deltaDev.write.accept(0x9, sndp.freq & 0xff);
            sndp.deltaDev.write.accept(0xa, (sndp.freq >> 8) & 0xff);
            break;
        case 0xF:
            // fade out
            switch (v & 15) {
            case 0x0:
            case 0x1:
            case 0x2:
            case 0x3:
            case 0x4:
            case 0x5:
            case 0x6:
            case 0x7:
                sndp.fadeTimer = 0;
                sndp.fadeCount = sndp.fadeTimer;
                sndp.volume = 0xff;
                break;
            case 0x8:
                sndp.fadeTimer = -100;
                sndp.fadeCount = sndp.fadeTimer;
                break;
            case 0xa:
                sndp.fadeTimer = 5000;
                sndp.fadeCount = sndp.fadeTimer;
                break;
            case 0xc:
                sndp.fadeTimer = -100;
                sndp.fadeCount = sndp.fadeTimer;
                break;
            case 0xe:
                sndp.fadeTimer = 1500;
                sndp.fadeCount = sndp.fadeTimer;
                break;
            }

            break;
        }
    }

    private int sndread(int a) {
        HesAdpcm sndp = (HesAdpcm) ctx;
        return switch (a & 15) {
            case 0xa -> sndp.pcmBuf[sndp.readPtr++];
            case 0xb -> sndp.port[0xb] & ~1;
            case 0xc -> {
                if (sndp.playFlag == 0) {
                    sndp.port[0xc] |= 1;
                    sndp.port[0xc] &= 0xf7;// ~8;
                } else {
                    sndp.port[0xc] &= 0xfe;// ~1;
                    sndp.port[0xc] |= 8;
                }
                yield sndp.port[0xc];
            }
            case 0xd -> 0;
//        case 0xe:
//            return sndp -> volume;
            default -> 0xff;
        };
    }

    private static final int LOG_BITS = 12;

    private void sndvolume(int volume) {
        HesAdpcm sndp = (HesAdpcm) ctx;
        volume = (volume << (LOG_BITS - 8)) << 1;
        sndp.common.masterVolume = volume;

        sndp.deltaDev.volume.accept(volume);
    }

    private void sndrelease() {
        HesAdpcm sndp = (HesAdpcm) ctx;

        sndp.deltaDev.release.run();

        if (sndp != null) {
            //XFREE(sndp);
            sndp = null;
        }
    }

    //private void setInst(Object ctx, int n, byte[] p, int l) { }

    // Register viewer settings from here
    //static Uint8* regdata;
    //extern Uint32 (* ioview_ioread_DEV_ADPCM) (Uint32 a);
    //static Uint32 ioview_ioread_bf(Uint32 a) {
    //    if (a >= 0x8 && a <= 0x15) return regdata[a]; else return 0x100;
    //}
    // Register viewer settings up to here

    public KmifSoundDevice HESAdPcmAlloc(S_Hesad s_hesad) {
        HesAdpcm sndp;
        //sndp = XMALLOC(sizeof(HESADPCM));
        sndp = new HesAdpcm();
        if (sndp == null) return null;
        //XMEMSET(sndp, 0, sizeof(HESADPCM));
        sndp.kmif = s_hesad; // TODO vavi
        sndp.kmif.ctx = sndp;
        sndp.kmif.release = this::sndrelease;
        sndp.kmif.reset = this::sndreset;
        sndp.kmif.synth = this::sndsynth;
        sndp.kmif.volume = this::sndvolume;
        sndp.kmif.write = this::sndwrite;
        sndp.kmif.read = this::sndread;
        sndp.kmif.setInst = setInst;

        // Register viewer settings from here
        //regdata = sndp.regs;
        //ioview_ioread_DEV_ADPCM = ioview_ioread_bf;
        // Register viewer settings up to here

        // process sound
        sndp.deltaDev = YMDELTATPCMSoundAlloc(3, sndp.pcmBuf);
        return sndp.kmif;
    }

    private KmifSoundDevice YMDELTATPCMSoundAlloc(int ymdeltatpcm_type, byte[] pcmbuf) {
        int ram_size;
        YmDeltaTPcmSound_ sndp;
        ram_size = switch (ymdeltatpcm_type) {
            case 0 -> 32 * 1024;  // YMDELTATPCM_TYPE_Y8950
            case 1 -> 256 * 1024; // YMDELTATPCM_TYPE_YM2608
            case 3 -> 256 * 256;  // MSM5205
            default -> 0;
        };
        //sndp = XMALLOC(sizeof(ymDeltaTPcmSound) + ram_size);
        sndp = new YmDeltaTPcmSound_();
        if (sndp == null) return null;
        sndp.ram_size = ram_size;
        sndp.ymDeltaTPcm_type = ymdeltatpcm_type;
        switch (ymdeltatpcm_type) {
        case 0: // YMDELTATPCM_TYPE_Y8950:
            sndp.memShift = 2;
            break;
        case 1: // YMDELTATPCM_TYPE_YM2608:
            // OPNA
            sndp.memShift = 6;
            break;
        case 2: // YMDELTATPCM_TYPE_YM2610:
            sndp.memShift = 9;
            break;
        case 3: // MSM5205:
            sndp.memShift = 0;
            break;
        }
        S_Deltat delta = new S_Deltat();
        sndp.kmif = delta;
        sndp.kmif.ctx = sndp;
        sndp.kmif.release = this::sndrelease;
        sndp.kmif.synth = this::sndsynth;
        sndp.kmif.volume = this::sndvolume;
        sndp.kmif.reset = this::sndreset;
        sndp.kmif.write = this::sndwrite;
        sndp.kmif.read = this::sndread;
        sndp.kmif.setInst = this.setInst;
        // RAM
        //ram_size != 0 ? (byte[])(sndp + 1) : 0;
        sndp.ramBuf = pcmbuf;
        sndp.ramMask = ram_size != 0 ? (ram_size - 1) : 0;
        // ROM
        sndp.romBuf = null;
        sndp.romMask = 0;
        sndp.logtbl = Kmif_LogTable.logTableAddRef();
        if (sndp.logtbl == null) {
            sndp.releaseSound();
            return null;
        }
        // Register viewer settings from here
//        sndpr = sndp;
//        if (ioview_ioread_DEV_ADPCM == NULL) ioview_ioread_DEV_ADPCM = ioview_ioread_bf;
//        if (ioview_ioread_DEV_ADPCM2 == NULL) ioview_ioread_DEV_ADPCM2 = ioview_ioread_bf2;
        // Register viewer settings up to here
        return sndp.kmif;
    }
}
