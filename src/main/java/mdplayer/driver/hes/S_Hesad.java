package mdplayer.driver.hes;

public class S_Hesad extends KMIF_SOUND_DEVICE {

    private static final int CPS_SHIFT = 16;
    private static final int PCE_VOLUME = 1; //1
    private static final int ADPCM_VOLUME = 50;

    public class HESADPCM {

        public KMIF_SOUND_DEVICE kmif;
        public KMIF_SOUND_DEVICE deltadev;

        public class common_ {
            public int mastervolume;
            public int cps;
            public int pt;
        }

        public common_ common = new common_();

        public byte[] pcmbuf = new byte[0x10000];
        public byte[] port = new byte[0x10];
        public byte[] regs = new byte[0x18];
        public int outfreq;
        public int freq;
        public short addr;
        public short writeptr;
        public short readptr;
        public byte playflag;
        public byte repeatflag;
        public int length;
        public int volume;
        public int fadetimer;
        public int fadecount;

        private void HESAdPcmReset() {
            this.addr = 0;
            this.freq = 0;
            this.writeptr = 0;
            this.readptr = 0;
            this.playflag = 0;
            this.repeatflag = 0;
            this.length = 0;
            this.volume = 0xff;
            this.deltadev.write.accept(0, 1);
        }
    }

    private void sndsynth(int[] p) {
        HESADPCM sndp = (HESADPCM) ctx;
        int[] pbf = new int[2];
        pbf[0] = 0;
        pbf[1] = 0;

        //この時既に、内蔵音源のレンダリングが終了している。
        p[0] = p[0] * PCE_VOLUME;
        p[1] = p[1] * PCE_VOLUME;

        sndp.deltadev.synth.accept(pbf);

        sndp.common.pt += sndp.common.cps;

        //1ms
        while (sndp.common.pt > 100000) {
            sndp.common.pt -= 100000;

            if (sndp.fadecount > 0 && sndp.fadetimer != 0) {
                sndp.fadecount--;
                sndp.volume = 0xff * sndp.fadecount / sndp.fadetimer;
            }
            if (sndp.fadecount < 0 && sndp.fadetimer != 0) {
                sndp.fadecount++;
                sndp.volume = 0xff - (0xff * sndp.fadecount / sndp.fadetimer);
            }

        }
        //	if(sndp->common.pt > 500)p[0]+=80000;
        p[0] += (pbf[0] * ADPCM_VOLUME * sndp.volume / 0xff);
        p[1] += (pbf[1] * ADPCM_VOLUME * sndp.volume / 0xff);
    }

    private void sndreset(int clock, int freq) {
        HESADPCM sndp = (HESADPCM) ctx;
        //XMEMSET(&sndp.pcmbuf, 0, sizeof(sndp.pcmbuf));
        sndp.pcmbuf = new byte[0x10000];
        //XMEMSET(&sndp.port, 0, sizeof(sndp.port));
        sndp.port = new byte[0x10];
        sndp.HESAdPcmReset();
        sndp.outfreq = freq;
        sndp.fadetimer = 0;
        sndp.fadecount = 0;
        sndp.common.cps = (int) (100000000 / freq);
        sndp.common.pt = 0;
        sndp.volume = 0xff;
        sndp.deltadev.reset.accept(clock, freq);
        sndp.deltadev.write.accept(1, 0);
        sndp.deltadev.write.accept(0xb, 0xff);
        //	sndp->deltadev->setinst(sndp->deltadev,0,sndp->pcmbuf,0x100);

    }

    private void sndwrite(int a, int v) {
        HESADPCM sndp = (HESADPCM) ctx;
        sndp.port[a & 15] = (byte) v;
        sndp.regs[a & 15] = (byte) v;
        switch (a & 15) {
        case 0x8:
            // port low
            sndp.addr &= 0xff00;
            sndp.addr |= (short) v;
            break;
        case 0x9:
            // port high
            sndp.addr &= 0xff;
            sndp.addr |= (short) (v << 8);
            break;
        case 0xA:
            // write buffer
            sndp.pcmbuf[sndp.writeptr++] = (byte) v;
            break;
        case 0xB:
            // DMA busy?
            break;
        case 0xC:
            break;
        case 0xD:
            if ((v & 0x80) != 0) {
                // reset
                sndp.HESAdPcmReset();
            }
            if ((v & 0x03) == 0x03) {
                // set write pointer
                sndp.writeptr = sndp.addr;
                sndp.regs[0x10] = (byte) (sndp.writeptr & 0xff);
                sndp.regs[0x11] = (byte) (sndp.writeptr >> 8);
            }
            if ((v & 0x08) != 0) {
                // set read pointer
                sndp.readptr = (short) (sndp.addr != 0 ? sndp.addr - 1 : sndp.addr);
                sndp.regs[0x12] = (byte) (sndp.readptr & 0xff);
                sndp.regs[0x13] = (byte) (sndp.readptr >> 8);
            }
            if ((v & 0x10) != 0) {
                sndp.length = sndp.addr;
                sndp.regs[0x14] = (byte) (sndp.length & 0xff);
                sndp.regs[0x15] = (byte) (sndp.length >> 8);
            }
            sndp.repeatflag = (byte) (((v & 0x20) == 0x20) ? 1 : 0);
            sndp.playflag = (byte) (((v & 0x40) == 0x40) ? 1 : 0);
            if (sndp.playflag != 0) {
                sndp.deltadev.write.accept(2, (int) (sndp.readptr & 0xff));
                sndp.deltadev.write.accept(3, (int) ((sndp.readptr >> 8) & 0xff));
                sndp.deltadev.write.accept(4, (int) ((sndp.length + sndp.readptr) & 0xff));
                sndp.deltadev.write.accept(5, (int) (((sndp.length + sndp.readptr) >> 8) & 0xff));
                sndp.deltadev.write.accept(0, 1);
                sndp.deltadev.write.accept(0, (int) ((0x80 | (sndp.repeatflag >> 1))));
            }
            break;
        case 0xE:
            // set freq
            sndp.freq = 7111 / (16 - (v & 15));
            sndp.deltadev.write.accept(0x9, sndp.freq & 0xff);
            sndp.deltadev.write.accept(0xa, (sndp.freq >> 8) & 0xff);
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
                sndp.fadetimer = 0;
                sndp.fadecount = sndp.fadetimer;
                sndp.volume = 0xff;
                break;
            case 0x8:
                sndp.fadetimer = -100;
                sndp.fadecount = sndp.fadetimer;
                break;
            case 0xa:
                sndp.fadetimer = 5000;
                sndp.fadecount = sndp.fadetimer;
                break;
            case 0xc:
                sndp.fadetimer = -100;
                sndp.fadecount = sndp.fadetimer;
                break;
            case 0xe:
                sndp.fadetimer = 1500;
                sndp.fadecount = sndp.fadetimer;
                break;
            }

            break;
        }
    }

    private int sndread(int a) {
        HESADPCM sndp = (HESADPCM) ctx;
        switch (a & 15) {
        case 0xa:
            return sndp.pcmbuf[sndp.readptr++];
        case 0xb:
            return (int) (sndp.port[0xb] & ~1);
        case 0xc:
            if (sndp.playflag == 0) {
                sndp.port[0xc] |= 1;
                sndp.port[0xc] &= 0xf7;// ~8;
            } else {
                sndp.port[0xc] &= 0xfe;// ~1;
                sndp.port[0xc] |= 8;
            }
            return sndp.port[0xc];
        case 0xd:
            return 0;
        //		case 0xe:
        //		  return sndp->volume;
        default:
            return 0xff;
        }
    }

    private static final int LOG_BITS = 12;

    private void sndvolume(int volume) {
        HESADPCM sndp = (HESADPCM) ctx;
        volume = (volume << (LOG_BITS - 8)) << 1;
        sndp.common.mastervolume = volume;

        sndp.deltadev.volume.accept(volume);
    }

    private void sndrelease() {
        HESADPCM sndp = (HESADPCM) ctx;

        sndp.deltadev.release.run();

        if (sndp != null) {
            //XFREE(sndp);
            sndp = null;
        }
    }

    //private void setinst(Object ctx, int n, byte[] p, int l) { }

    //ここからレジスタビュアー設定
    //static Uint8* regdata;
    //extern Uint32 (* ioview_ioread_DEV_ADPCM) (Uint32 a);
    //static Uint32 ioview_ioread_bf(Uint32 a)
    //{
    //    if (a >= 0x8 && a <= 0x15) return regdata[a]; else return 0x100;
    //}
    //ここまでレジスタビュアー設定

    public KMIF_SOUND_DEVICE HESAdPcmAlloc() {
        HESADPCM sndp;
        //sndp = XMALLOC(sizeof(HESADPCM));
        sndp = new HESADPCM();
        if (sndp == null) return null;
        //XMEMSET(sndp, 0, sizeof(HESADPCM));
        sndp.kmif = new S_Hesad();
        sndp.kmif.ctx = sndp;
        sndp.kmif.release = this::sndrelease;
        sndp.kmif.reset = this::sndreset;
        sndp.kmif.synth = this::sndsynth;
        sndp.kmif.volume = this::sndvolume;
        sndp.kmif.write = this::sndwrite;
        sndp.kmif.read = this::sndread;
        sndp.kmif.setinst = setinst;

        //ここからレジスタビュアー設定
        //regdata = sndp.regs;
        //ioview_ioread_DEV_ADPCM = ioview_ioread_bf;
        //ここまでレジスタビュアー設定

        //発声部分
        sndp.deltadev = YMDELTATPCMSoundAlloc(3, sndp.pcmbuf);
        return sndp.kmif;
    }

    private KMIF_SOUND_DEVICE YMDELTATPCMSoundAlloc(int ymdeltatpcm_type, byte[] pcmbuf) {
        int ram_size;
        S_Deltat.YMDELTATPCMSOUND_ sndp;
        switch (ymdeltatpcm_type) {
        case 0://                    YMDELTATPCM_TYPE_Y8950:
            ram_size = 32 * 1024;
            break;
        case 1://                    YMDELTATPCM_TYPE_YM2608:
            ram_size = 256 * 1024;
            break;
        case 3://                    MSM5205:
            ram_size = 256 * 256;
            break;
        default:
            ram_size = 0;
            break;
        }
        //sndp = XMALLOC(sizeof(YMDELTATPCMSOUND) + ram_size);
        sndp = new S_Deltat.YMDELTATPCMSOUND_();
        if (sndp == null) return null;
        sndp.ram_size = ram_size;
        sndp.ymdeltatpcm_type = (byte) ymdeltatpcm_type;
        switch (ymdeltatpcm_type) {
        case 0://                    YMDELTATPCM_TYPE_Y8950:
            sndp.memshift = 2;
            break;
        case 1://                    YMDELTATPCM_TYPE_YM2608:
            /* OPNA */
            sndp.memshift = 6;
            break;
        case 2://                    YMDELTATPCM_TYPE_YM2610:
            sndp.memshift = 9;
            break;
        case 3://                    MSM5205:
            sndp.memshift = 0;
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
        sndp.kmif.setinst = this.setinst;
        /* RAM */
        if (pcmbuf != null) {
            sndp.rambuf = pcmbuf;
        } else {
            sndp.rambuf = null;// ram_size != 0 ? (byte[])(sndp + 1) : 0;
        }
        sndp.rammask = ram_size != 0 ? (ram_size - 1) : 0;
        /* ROM */
        sndp.rombuf = null;
        sndp.rommask = 0;
        sndp.logtbl = S_Deltat.KMIF_LOGTABLE.LogTableAddRef();
        if (sndp.logtbl == null) {
            sndp.sndrelease();
            return null;
        }
        //ここからレジスタビュアー設定
        //sndpr = sndp;
        //if (ioview_ioread_DEV_ADPCM == NULL) ioview_ioread_DEV_ADPCM = ioview_ioread_bf;
        //if (ioview_ioread_DEV_ADPCM2 == NULL) ioview_ioread_DEV_ADPCM2 = ioview_ioread_bf2;
        //ここまでレジスタビュアー設定
        return sndp.kmif;
    }
}
