package mdplayer.driver.hes;

import java.util.function.BiFunction;
import java.util.function.Function;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.driver.hes.KmEvent.KMEVENT;
import mdsound.Common.TriConsumer;


/*
 * km6502 HuC6280 I/F
 *
 * HES
 * system clock 21477270Hz
 * CPUH clock 21477270Hz system clock
 * CPUL clock 3579545 system clock / 6

 * FF		I/O
 * F9-FB	SGX-RAM
 * F8		RAM
 * F7		BATTERY RAM
 * 80-87	CD-ROM^2 RAM
 * 00-		ROM
 */
public class M_Hes {

    private Km6280 km6280 = new Km6280();

    public static class NEZ_PLAY {

        static class SONG_INFO {
            public int songno;
            public int maxsongno;
            public int startsongno;
            public int extdevice;
            public int initaddress;
            public int playaddress;
            public int channel;
            public int initlimit;
        }

        public HESHES heshes;
        public SONG_INFO song = new SONG_INFO();

        public static class SongInfoData {
            public String title;
            public String artist;
            public String copyright;
            public String detail;
        }

        private SongInfoData _songinfodata = new SongInfoData();

        public int ExecuteHES() {
            return this.heshes != null ? this.heshes.execute() : 0;
        }

        public void HESSoundRenderStereo(int[] d) {
            this.heshes.synth(d);
        }

        public int HESSoundRenderMono() {
            int[] d = new int[] {0, 0};
            this.heshes.synth(d);
            //#if (((-1) >> 1) == -1)
            //	return (d[0] + d[1]) >> 1;
            //#else
            return (d[0] + d[1]) / 2;
            //#endif
        }

        private void HESHESVolume(int v) {
            if (this.heshes != null) {
                this.heshes.volume(v);
            }
        }

        public void HESHESReset() {
            if (this.heshes != null) this.heshes.reset(this);
        }

        private void HESHESTerminate() {
            if (this.heshes != null) {
                this.heshes.terminate();
                this.heshes = null;
            }
        }

        public int HESLoad(byte[] pData, int uSize) {
            this.heshes = new HESHES();
            int ret = this.heshes.load(this, pData, uSize);
            if (ret != 0) {
                this.heshes.terminate();
                return ret;
            }
            return ret;
        }
    }

    private static final int SHIFT_CPS = 15;
    private static final int HES_BASECYCLES = (21477270);
    private static final int HES_TIMERCYCLES = (1024 * 3);

    public interface READPROC extends BiFunction<HESHES, Integer, Integer> {
    }

    public interface WRITEPROC extends TriConsumer<HESHES, Integer, Integer> {
    }

    public static class HESHES {
        public enum NESERR {
            NOERROR,
            SHORTOFMEMORY,
            FORMAT,
            PARAMETER
        }

        public Km6280.K6280Context ctx;
        //public KMIF_SOUND_DEVICE hessnd;
        public KMIF_SOUND_DEVICE hespcm;
        public KMEVENT kme = new KMEVENT();
        public int vsync;
        public int timer;

        public int bp;          /* break point */
        public int breaked;     /* break point flag */

        public int cps;             /* cycles per sample:fixed point */
        public int cpsrem;          /* cycle remain */
        public int cpsgap;          /* cycle gap */
        public int total_cycles;    /* total played cycles */

        public byte[] mpr = new byte[0x8];
        public byte[] firstmpr = new byte[0x8];
        public byte[][] memmap = new byte[0x100][];
        public int initaddr;

        public int playerromaddr;
        public byte[] playerrom = new byte[0x10];

        public byte hestim_RELOAD;    /* IO $C01 ($C00)*/
        public byte hestim_COUNTER;   /* IO $C00 */
        public byte hestim_START;     /* IO $C01 */
        public byte hesvdc_STATUS;
        public byte hesvdc_CR;
        public byte hesvdc_ADR;

        private KmEvent kmevent = new KmEvent();

        public ChipRegister chipRegister;
        public Hes.HESDetector ld;
        private Boolean disableSendChip = false;

        private static int GetWordLE(byte[] p, int ptr) {
            return (int) p[ptr + 0] | ((int) p[ptr + 1] << 8);
        }

        private static int GetDwordLE(byte[] p, int ptr) {
            if (p.length <= ptr + 3) return 0;
            return (int) p[ptr + 0] | ((int) p[ptr + 1] << 8) | ((int) p[ptr + 2] << 16) | ((int) p[ptr + 3] << 24);
        }

        private static int DivFix(int p1, int p2, int fix) {
            int ret;
            ret = p1 / p2;
            p1 = p1 % p2;/* p1 = p1 - p2 * ret; */
            while (fix-- != 0) {
                p1 += p1;
                ret += ret;
                if (p1 >= p2) {
                    p1 -= p2;
                    ret++;
                }
            }
            return ret;
        }

        public void vsync_event(KmEvent.KMEVENT _event, int curid, HESHES _this) {
            _this.vsync_setup();
            if ((_this.hesvdc_CR & 8) != 0) {
                _this.ctx.iRequest |= Km6280.K6280Context.K6280_IRQ.K6280_INT1.v;
                //System.err.println("vsyncEvent");
                _this.breaked = 0;
            }
            _this.hesvdc_STATUS = 1;
        }

        private void timer_event(KmEvent.KMEVENT _event, int curid, HESHES _this) {
            if (_this.hestim_START != 0 && _this.hestim_COUNTER-- == 0) {
                _this.hestim_COUNTER = _this.hestim_RELOAD;
                _this.ctx.iRequest |= Km6280.K6280Context.K6280_IRQ.K6280_TIMER.v;
                //System.err.println("timerEvent");
                _this.breaked = 0;
            }
            _this.timer_setup();
        }

        private int execute() {
            int cycles;
            this.cpsrem += this.cps;
            cycles = this.cpsrem >> (int) SHIFT_CPS;
            if (this.cpsgap >= cycles)
                this.cpsgap -= cycles;
            else {
                int excycles = cycles - this.cpsgap;
                this.cpsgap = km6280_exec(this.ctx, excycles) - excycles;
            }
            this.cpsrem &= (1 << (int) SHIFT_CPS) - 1;
            this.total_cycles += cycles;

            return 0;
        }

        public void synth(int[] d) {
            //this.hessnd.synth(this.hessnd.ctx, d);
            this.hespcm.synth.accept(d);
        }

        private void volume(int v) {
            //this.hessnd.volume(this.hessnd.ctx, v);
            //this.hespcm.volume(this.hespcm.ctx, v);
        }

        private void vsync_setup() {
            kmevent.kmevent_settimer(this.kme, this.vsync, 4 * 342 * 262);
        }

        private void timer_setup() {
            kmevent.kmevent_settimer(this.kme, this.timer, HES_TIMERCYCLES);
        }

        private void write_6270(int a, int v) {
            switch (a) {
            case 0:
                this.hesvdc_ADR = (byte) v;
                break;
            case 2:
                switch (this.hesvdc_ADR) {
                case 5: /* CR */
                    this.hesvdc_CR = (byte) v;
                    break;
                }
                break;
            case 3:
                break;
            }
        }

        private int read_6270(int a) {
            int v = 0;
            if (a == 0) {
                if (this.hesvdc_STATUS != 0) {
                    this.hesvdc_STATUS = 0;
                    v = 0x20;
                }
                this.ctx.iRequest &= 0xFFFFFFDF;// ~Km6280.K6280_IRQ.K6280_INT1;
                //#if 0
                //v = 0x20;	/* 常にVSYNC期間 */
                //#endif
            }
            return v;
        }

        private int read_io(int a) {
            switch (a >> 10) {
            case 0: /* VDC */
                return read_6270(a & 3);
            case 2: /* PSG */
                //return this.hessnd.read(this.hessnd.ctx, a & 0xf);
                return 0;
            case 3: /* TIMER */
                if ((a & 1) != 0)
                    return this.hestim_START;
                else
                    return this.hestim_COUNTER;
            case 5: /* IRQ */
                switch (a & 15) {
                case 2: {
                    int v = 0xf8;
                    if ((this.ctx.iMask & Km6280.K6280Context.K6280_IRQ.K6280_TIMER.v) == 0) v |= 4;
                    if ((this.ctx.iMask & Km6280.K6280Context.K6280_IRQ.K6280_INT1.v) == 0) v |= 2;
                    if ((this.ctx.iMask & Km6280.K6280Context.K6280_IRQ.K6280_INT2.v) == 0) v |= 1;
                    return v;
                }
                case 3: {
                    byte v = 0;
                    if ((this.ctx.iRequest & Km6280.K6280Context.K6280_IRQ.K6280_TIMER.v) != 0) v |= 4;
                    if ((this.ctx.iRequest & Km6280.K6280Context.K6280_IRQ.K6280_INT1.v) != 0) v |= 2;
                    if ((this.ctx.iRequest & Km6280.K6280Context.K6280_IRQ.K6280_INT2.v) != 0) v |= 1;
                    //#if 0
                    //THIS_->ctx.iRequest &= ~(K6280_TIMER | K6280_INT1 | K6280_INT2);
                    //#endif
                    return v;
                }
                }
                return 0x00;
            case 7:
                a -= this.playerromaddr;
                if (a < 0x10) return this.playerrom[a];
                return 0xff;
            case 6: /* CDROM */
                switch (a & 15) {
                case 0x0a:
                case 0x0b:
                case 0x0c:
                case 0x0d:
                case 0x0e://デバッグ用
                case 0x0f://デバッグ用
                    return this.hespcm.read.apply(a & 0xf);
                }
                return 0xff;
            default:
            case 1: /* VCE */
            case 4: /* PAD */
                return 0xff;
            }
        }

        public void write_io(int a, int v) {
            switch (a >> 10) {
            case 0: /* VDC */
                write_6270(a & 3, v);
                break;
            case 2: /* PSG */
                //System.err.println("Adr:{0:X2} Dat:{1:X2}",
                //    (int)(a & 0xf),
                //    (int)v
                //    );
                if (!disableSendChip)
                    chipRegister.setHuC6280Register(0, a & 0xf, v, EnmModel.VirtualModel);
                ld.write(a & 0xf, v, 0);
                //this.hessnd.write(this.hessnd.ctx, a & 0xf, v);
                break;
            case 3: /* TIMER */
                switch (a & 1) {
                case 0:
                    this.hestim_RELOAD = (byte) (v & 127);
                    break;
                case 1:
                    v &= 1;
                    if (v != 0 && this.hestim_START == 0)
                        this.hestim_COUNTER = this.hestim_RELOAD;
                    this.hestim_START = (byte) v;
                    break;
                }
                break;
            case 5: /* IRQ */
                switch (a & 15) {
                case 2:
                    this.ctx.iMask &= 0xffffff8f;// ~((int)Km6280.K6280_IRQ.K6280_TIMER | (int)Km6280.K6280_IRQ.K6280_INT1 | (int)Km6280.K6280_IRQ.K6280_INT2);
                    if ((v & 4) == 0) this.ctx.iMask |= Km6280.K6280Context.K6280_IRQ.K6280_TIMER.v;
                    if ((v & 2) == 0) this.ctx.iMask |= Km6280.K6280Context.K6280_IRQ.K6280_INT1.v;
                    if ((v & 1) == 0) this.ctx.iMask |= Km6280.K6280Context.K6280_IRQ.K6280_INT2.v;
                    break;
                case 3:
                    this.ctx.iRequest &= 0xffffffef;// ~(int)Km6280.K6280_IRQ.K6280_TIMER;
                    break;
                }
                break;
            case 6: /* CDROM */
                switch (a & 15) {
                case 0x08:
                case 0x09:
                case 0x0a:
                case 0x0b:
                case 0x0d:
                case 0x0e:
                case 0x0f:
                    this.hespcm.write.accept(a & 0xf, v);
                    break;
                }
                break;
            default:
            case 1: /* VCE */
            case 4: /* PAD */
            case 7:
                break;
            }
        }

        public int read_event(int a) {
            byte page = this.mpr[a >> 13];
            if (this.memmap[page] != null)
                return this.memmap[page][a & 0x1fff];
            else if (page == (byte) 0xff)
                return read_io(a & 0x1fff);
            else
                return 0xff;
        }

        public void write_event(int a, int v) {
            byte page = this.mpr[a >> 13];
            if (this.memmap[page] != null)
                this.memmap[page][a & 0x1fff] = (byte) v;
            else if (page == (byte) 0xff)
                write_io(a & 0x1fff, v);
        }

        public int readmpr_event(int a) {
            int i;
            for (i = 0; i < 8; i++) if ((a & (1 << (int) i)) != 0) return this.mpr[i];
            return 0xff;
        }

        public void writempr_event(int a, int v) {
            int i;
            if (v < 0x80 && this.memmap[v] == null) return;
            for (i = 0; i < 8; i++) if ((a & (1 << (int) i)) != 0) this.mpr[i] = (byte) v;
        }

        public void write6270_event(int a, int v) {
            write_6270(a & 0x1fff, v);
        }

        private void terminate() {
            if (this.hespcm != null) this.hespcm.release.run();
            for (int i = 0; i < 0x100; i++) if (this.memmap[i] != null) this.memmap = null;
        }

        private int GetWordLE(byte[] p) {
            return p[0] | (p[1] << 8);
        }

        private int GetDwordLE(byte[] p) {
            return p[0] | (p[1] << 8) | (p[2] << 16) | (p[3] << 24);
        }

        public int alloc_physical_address(int a, int l) {
            byte page = (byte) (a >> 13);
            byte lastpage = (byte) ((a + l - 1) >> 13);
            for (; page <= lastpage; page++) {
                if (this.memmap[page] == null) {
                    //this.memmap[page] = (byte[])XMALLOC(0x2000);
                    this.memmap[page] = new byte[0x2000];
                    if (this.memmap[page] == null) return 0;
                    //XMEMSET(this.memmap[page], 0, 0x2000);
                }
            }
            return 1;
        }

        public void copy_physical_address(int a, int l, byte[] p, int ptrP) {
            byte page = (byte) (a >> 13);
            int w;
            if ((a & 0x1fff) != 0) {
                w = 0x2000 - (a & 0x1fff);
                if (w > l) w = l;
                if (w >= 0) System.arraycopy(p, ptrP, this.memmap[page], a & 0x1fff, w);
                page++;
                //p += w;
                ptrP += w;
                l -= w;
            }
            while (l != 0) {
                w = (l > 0x2000) ? 0x2000 : l;
                if (w >= 0) System.arraycopy(p, ptrP, this.memmap[page], 0, w);
                page++;
                //p += w;
                ptrP += w;
                l -= w;
            }
        }

        private int km6280_exec(Km6280.K6280Context ctx, int cycles) {
            HESHES THIS_ = ctx.user;
            int kmecycle;
            kmecycle = ctx.clock = 0;
            while (ctx.clock < cycles) {
                if (THIS_.breaked == 0) {

                    //System.err.println("PC:%4x S:%2x SPDAT0x1FF:%2x%2x",
                    //    THIS_.ctx.PC,THIS_.ctx.S,
                    //    THIS_.memmap[0xf8][0x1ff], THIS_.memmap[0xf8][0x1fe]
                    //    );
                    /* Execute 1op */
                    ctx.K_EXEC();

                    if (ctx.PC == THIS_.bp) {
                        if (((THIS_.ctx.iRequest) & (THIS_.ctx.iMask ^ 0x3) & ((int) Km6280.K6280Context.K6280_IRQ.K6280_INT1.ordinal() | (int) Km6280.K6280Context.K6280_IRQ.K6280_TIMER.ordinal())) == 0)
                            THIS_.breaked = 1;
                    }
                } else {
                    int nextcount;
                    /* break時は次のイベントまで一度に進める */
                    nextcount = THIS_.kme.item[THIS_.kme.item[0].next].count;
                    if (kmevent.kmevent_gettimer(THIS_.kme, 0, nextcount) != 0) {
                        /* イベント有り */
                        if (ctx.clock + nextcount < cycles)
                            ctx.clock += nextcount;    /* 期間中にイベント有り */
                        else
                            ctx.clock = cycles;        /* 期間中にイベント無し */
                    } else {
                        /* イベント無し */
                        ctx.clock = cycles;
                    }
                }
                /* イベント進行 */
                kmevent.kmevent_process(THIS_.kme, ctx.clock - kmecycle);
                kmecycle = ctx.clock;
            }
            ctx.clock = 0;
            return kmecycle;
        }

        private void reset(NEZ_PLAY nezPlay) {
            int i, initbreak;
            //int freq = NESAudioFrequencyGet(pNezPlay);
            int freq = Common.VGMProcSampleRate;

            //this.hessnd.reset(this.hessnd.ctx, HES_BASECYCLES, freq);
            this.hespcm.reset.accept(HES_BASECYCLES, freq);
            this.kmevent.kmevent_init(this.kme);

            /* RAM CLEAR */
            for (i = 0xf8; i <= 0xfb; i++)
                if (this.memmap[i] != null) {
                    //XMEMSET(this.memmap[i], 0, 0x2000);
                    this.memmap[i] = new byte[0x2000];
                }

            this.cps = DivFix(HES_BASECYCLES, freq, SHIFT_CPS);
            this.ctx = new Km6280.K6280Context();
            this.ctx.user = this;
            this.ctx.ReadByte = HESHES::read_event;
            this.ctx.WriteByte = HESHES::write_event;
            this.ctx.ReadMPR = HESHES::readmpr_event;
            this.ctx.WriteMPR = HESHES::writempr_event;
            this.ctx.Write6270 = HESHES::write6270_event;

            this.vsync = this.kmevent.kmevent_alloc(this.kme);
            this.timer = this.kmevent.kmevent_alloc(this.kme);
            this.kmevent.kmevent_setevent(this.kme, this.vsync, this::vsync_event, this);
            this.kmevent.kmevent_setevent(this.kme, this.timer, this::timer_event, this);

            this.bp = this.playerromaddr + 3;
            for (i = 0; i < 8; i++) this.mpr[i] = this.firstmpr[i];

            this.breaked = 0;
            this.cpsrem = this.cpsgap = this.total_cycles = 0;

            //this.ctx.A = (SONGINFO_GetSongNo(this.song) - 1) & 0xff;
            this.ctx.A = (nezPlay.song.songno - 1) & 0xff;
            //this.ctx.A = (int)((49 - 1) & 0xff);
            this.ctx.P = Km6280.K6280Context.K6280_FLAGS.K6280_Z_FLAG.v + Km6280.K6280Context.K6280_FLAGS.K6280_I_FLAG.v;
            this.ctx.X = this.ctx.Y = 0;
            this.ctx.S = 0xFF;
            this.ctx.PC = this.playerromaddr;
            this.ctx.iRequest = 0;
            this.ctx.iMask = 0xffffffff;// ~0;
            this.ctx.lowClockMode = 0;

            this.playerrom[0x00] = 0x20;  /* JSR */
            this.playerrom[0x01] = (byte) ((this.initaddr >> 0) & 0xff);
            this.playerrom[0x02] = (byte) ((this.initaddr >> 8) & 0xff);
            this.playerrom[0x03] = 0x4c;  /* JMP */
            this.playerrom[0x04] = (byte) (((this.playerromaddr + 3) >> 0) & 0xff);
            this.playerrom[0x05] = (byte) (((this.playerromaddr + 3) >> 8) & 0xff);

            this.hesvdc_STATUS = 0;
            this.hesvdc_CR = 0;
            this.hesvdc_ADR = 0;
            this.vsync_setup();
            this.hestim_RELOAD = this.hestim_COUNTER = this.hestim_START = 0;
            this.timer_setup();

            /* request execute(5sec) */
            initbreak = 5 << 8;

            this.disableSendChip = true;

            while (this.breaked == 0 && --initbreak != 0)
                this.km6280_exec(this.ctx, HES_BASECYCLES >> 8);

            this.disableSendChip = false;
            this.chipRegister.setHuC6280Register(0, 1, 0xff, EnmModel.VirtualModel);

            if (this.breaked != 0) {
                this.breaked = 0;
                this.ctx.P &= 0xfffffffb;// ~Km6280.K6280_FLAGS.K6280_I_FLAG;
            } else {
                this.ctx.A = (nezPlay.song.songno - 1) & 0xff;
                this.ctx.P = Km6280.K6280Context.K6280_FLAGS.K6280_Z_FLAG.v + Km6280.K6280Context.K6280_FLAGS.K6280_I_FLAG.v;
                this.ctx.X = this.ctx.Y = 0;
                this.ctx.S = 0xFF;
                this.ctx.PC = this.playerromaddr;
                this.ctx.iRequest = 0;
                this.ctx.iMask = 0xffffffff;// ~0;
                this.ctx.lowClockMode = 0;
            }

            this.cpsrem = this.cpsgap = this.total_cycles = 0;

            //ここからメモリービュアー設定
            //memview_context = this.heshes;
            //MEM_MAX = 0xffff;
            //MEM_IO = 0x0000;
            //MEM_RAM = 0x2000;
            //MEM_ROM = 0x4000;
            //memview_memread = memview_memread_hes;
            //ここまでメモリービュアー設定

            //ここからダンプ設定
            //pNezPlayDump = pNezPlay;
            //dump_MEM_PCE = dump_MEM_PCE_bf;
            //dump_DEV_HUC6230 = dump_DEV_HUC6230_bf;
            //dump_DEV_ADPCM = dump_DEV_ADPCM_bf;
            //ここまでダンプ設定
        }

        private int load(NEZ_PLAY nezPlay, byte[] pData, int uSize) {
            int i, p;
            //XMEMSET(this., 0, sizeof(HESHES));
            //this. = new HESHES();
            //this..hessnd = 0;
            //this..hespcm = 0;
            for (i = 0; i < 0x100; i++) this.memmap[i] = null;

            if (uSize < 0x20) return NESERR.FORMAT.ordinal();
            nezPlay.song.startsongno = pData[5] + 1;
            nezPlay.song.songno = 256;
            nezPlay.song.channel = 2;
            nezPlay.song.extdevice = 0;
            for (i = 0; i < 8; i++) this.firstmpr[i] = pData[8 + i];
            this.playerromaddr = 0x1ff0;
            this.initaddr = GetWordLE(pData, 0x06);
            nezPlay.song.initaddress = this.initaddr;
            nezPlay.song.playaddress = 0;

            nezPlay._songinfodata.detail = String.format(
                    "Type           : HES" +
                            "Start Song: %2x" +
                            "Init Address: %4x" +
                            "First Mapper 0 : %2x" +
                            "First Mapper 1 : %2x" +
                            "First Mapper 2 : %2x" +
                            "First Mapper 3 : %2x" +
                            "First Mapper 4 : %2x" +
                            "First Mapper 5 : %2x" +
                            "First Mapper 6 : %2x" +
                            "First Mapper 7 : %2x"
                    , pData[5], this.initaddr
                    , pData[0x8]
                    , pData[0x9]
                    , pData[0xa]
                    , pData[0xb]
                    , pData[0xc]
                    , pData[0xd]
                    , pData[0xe]
                    , pData[0xf]
            );

            if (this.alloc_physical_address(0xf8 << 13, 0x2000) == 0) /* RAM */
                return NESERR.SHORTOFMEMORY.ordinal();
            if (this.alloc_physical_address(0xf9 << 13, 0x2000) == 0) /* SGX-RAM */
                return NESERR.SHORTOFMEMORY.ordinal();
            if (this.alloc_physical_address(0xfa << 13, 0x2000) == 0) /* SGX-RAM */
                return NESERR.SHORTOFMEMORY.ordinal();
            if (this.alloc_physical_address(0xfb << 13, 0x2000) == 0) /* SGX-RAM */
                return NESERR.SHORTOFMEMORY.ordinal();
            if (this.alloc_physical_address(0x00 << 13, 0x2000) == 0) /* IPL-ROM */
                return NESERR.SHORTOFMEMORY.ordinal();
            for (p = 0x10; p + 0x10 < uSize; p += 0x10 + GetDwordLE(pData, p + 4)) {
                if (GetDwordLE(pData, p) == 0x41544144)    /* 'DATA' */ {
                    int a, l;
                    l = GetDwordLE(pData, p + 4);
                    a = GetDwordLE(pData, p + 8);
                    if (this.alloc_physical_address(a, l) == 0) return NESERR.SHORTOFMEMORY.ordinal();
                    if (l > uSize - p - 0x10) l = uSize - p - 0x10;
                    int q = p + 0x10;
                    this.copy_physical_address(a, l, pData, q);
                    p = q;
                }
            }
            //this..hessnd = HESSoundAlloc();
            //if (this..hessnd == 0) return NESERR_SHORTOFMEMORY;
            this.hespcm = (new S_Hesad()).HESAdPcmAlloc();
            if (this.hespcm == null) return NESERR.SHORTOFMEMORY.ordinal();

            return NESERR.NOERROR.ordinal();
        }
    }

    //ここからメモリービュアー設定
    public interface memview_memread extends Function<Integer, Integer> {
    }

    private HESHES memview_context = null;

    //private int MEM_MAX, MEM_IO, MEM_RAM, MEM_ROM;
    private int memview_memread_hes(int a) {
        if (a >= 0x1800 && a < 0x1c00 && (a & 0xf) == 0xa) return 0xff;
        return memview_context.read_event(a);
    }
    //ここまでメモリービュアー設定

    //ここからダンプ設定
    //private NEZ_PLAY pNezPlayDump;
    public interface dump_MEM_PCE extends BiFunction<Integer, byte[], Integer> {
    }

    private int dump_MEM_PCE_bf(int menu, byte[] mem) {
        int i;
        switch (menu) {
        case 1://Memory
            for (i = 0; i < 0x10000; i++)
                mem[i] = (byte) memview_memread_hes(i);
            return (int) i;
        }
        return 0xfffffffe;// (-2);
    }
}
