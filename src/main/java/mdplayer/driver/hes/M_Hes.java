package mdplayer.driver.hes;

import java.util.function.BiFunction;
import java.util.function.Function;

import dotnet4j.util.compat.TriConsumer;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.driver.hes.KmEvent.Event;
import vavi.util.ByteUtil;


/*
 * km6502 OotakeHuC6280 I/F
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

        static class SongInfo {
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
        public SongInfo song = new SongInfo();

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

    public interface ReadProc extends BiFunction<HESHES, Integer, Integer> {
    }

    public interface WriteProc extends TriConsumer<HESHES, Integer, Integer> {
    }

    public static class HESHES {
        public enum Error {
            NOERROR,
            SHORTOFMEMORY,
            FORMAT,
            PARAMETER
        }

        public Km6280.K6280Context ctx;
        //public KMIF_SOUND_DEVICE hessnd;
        public KMIF_SOUND_DEVICE hespcm;
        public Event kme = new Event();
        public int vsync;
        public int timer;

        /** break point */
        public int bp;
        /** break point flag */
        public int breaked;

        /** cycles per sample:fixed point */
        public int cps;
        /** cycle remain */
        public int cpsRem;
        /** cycle gap */
        public int cpsGap;
        /** total played cycles */
        public int totalCycles;

        public byte[] mpr = new byte[0x8];
        public byte[] firstMpr = new byte[0x8];
        public byte[][] memMap = new byte[0x100][];
        public int initAddr;

        public int playerRomAddr;
        public byte[] playerRom = new byte[0x10];

        /** IO $C01 ($C00)*/
        public int hestimReload;
        /** IO $C00 */
        public int hestimCounter;
        /** IO $C01 */
        public int hestimStart;
        public int hesvdcStatus;
        public int hesvdcCr;
        public int hesvdcAdr;

        private KmEvent kmEvent = new KmEvent();

        public ChipRegister chipRegister;
        public Hes.HESDetector ld;
        private boolean disableSendChip = false;

        private static int getWordLE(byte[] p, int ptr) {
            return ByteUtil.readLeShort(p, ptr);
        }

        private static int getDwordLE(byte[] p, int ptr) {
            if (p.length <= ptr + 3) return 0;
            return ByteUtil.readLeInt(p, ptr);
        }

        private static int fixDiv(int p1, int p2, int fix) {
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

        public void vsyncEvent(Event _event, int curid, HESHES _this) {
            _this.setUpVsync();
            if ((_this.hesvdcCr & 8) != 0) {
                _this.ctx.iRequest |= Km6280.K6280Context.IRQ.INT1.v;
                //System.err.println("vsyncEvent");
                _this.breaked = 0;
            }
            _this.hesvdcStatus = 1;
        }

        private void timerEvent(Event _event, int curid, HESHES _this) {
            if (_this.hestimStart != 0 && _this.hestimCounter-- == 0) {
                _this.hestimCounter = _this.hestimReload;
                _this.ctx.iRequest |= Km6280.K6280Context.IRQ.TIMER.v;
                //System.err.println("timerEvent");
                _this.breaked = 0;
            }
            _this.setUpTimer();
        }

        private int execute() {
            int cycles;
            this.cpsRem += this.cps;
            cycles = this.cpsRem >> SHIFT_CPS;
            if (this.cpsGap >= cycles)
                this.cpsGap -= cycles;
            else {
                int excycles = cycles - this.cpsGap;
                this.cpsGap = km6280_exec(this.ctx, excycles) - excycles;
            }
            this.cpsRem &= (1 << SHIFT_CPS) - 1;
            this.totalCycles += cycles;

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

        private void setUpVsync() {
            kmEvent.kmevent_settimer(this.kme, this.vsync, 4 * 342 * 262);
        }

        private void setUpTimer() {
            kmEvent.kmevent_settimer(this.kme, this.timer, HES_TIMERCYCLES);
        }

        private void write6270(int a, int v) {
            switch (a) {
            case 0:
                this.hesvdcAdr = v;
                break;
            case 2:
                switch (this.hesvdcAdr) {
                case 5: // CR */
                    this.hesvdcCr = v;
                    break;
                }
                break;
            case 3:
                break;
            }
        }

        private int read6270(int a) {
            int v = 0;
            if (a == 0) {
                if (this.hesvdcStatus != 0) {
                    this.hesvdcStatus = 0;
                    v = 0x20;
                }
                this.ctx.iRequest &= 0xffFF_FFDF;// ~Km6280.IRQ.INT1;
//#if 0
//                v = 0x20;	// 常にVSYNC期間
//#endif
            }
            return v;
        }

        private int readIO(int a) {
            switch (a >> 10) {
            case 0: // VDC */
                return read6270(a & 3);
            case 2: // Psg */
                //return this.hessnd.read(this.hessnd.ctx, a & 0xf);
                return 0;
            case 3: // TIMER */
                if ((a & 1) != 0)
                    return this.hestimStart;
                else
                    return this.hestimCounter;
            case 5: // IRQ */
                switch (a & 15) {
                case 2: {
                    int v = 0xf8;
                    if ((this.ctx.iMask & Km6280.K6280Context.IRQ.TIMER.v) == 0) v |= 4;
                    if ((this.ctx.iMask & Km6280.K6280Context.IRQ.INT1.v) == 0) v |= 2;
                    if ((this.ctx.iMask & Km6280.K6280Context.IRQ.INT2.v) == 0) v |= 1;
                    return v;
                }
                case 3: {
                    int v = 0;
                    if ((this.ctx.iRequest & Km6280.K6280Context.IRQ.TIMER.v) != 0) v |= 4;
                    if ((this.ctx.iRequest & Km6280.K6280Context.IRQ.INT1.v) != 0) v |= 2;
                    if ((this.ctx.iRequest & Km6280.K6280Context.IRQ.INT2.v) != 0) v |= 1;
//#if 0
//                    THIS_->ctx.iRequest &= ~(TIMER | INT1 | INT2);
//#endif
                    return v;
                }
                }
                return 0x00;
            case 7:
                a -= this.playerRomAddr;
                if (a < 0x10) return this.playerRom[a];
                return 0xff;
            case 6: // CDROM */
                switch (a & 15) {
                case 0x0a:
                case 0x0b:
                case 0x0c:
                case 0x0d:
                case 0x0e: // デバッグ用
                case 0x0f: // デバッグ用
                    return this.hespcm.read.apply(a & 0xf);
                }
                return 0xff;
            default:
            case 1: // VCE
            case 4: // PAD
                return 0xff;
            }
        }

        public void writeIO(int a, int v) {
            switch (a >> 10) {
            case 0: // VDC
                write6270(a & 3, v);
                break;
            case 2: // Psg
                //System.err.println("Adr:%2X Dat:%2X", (int) (a & 0xf), (int) v);
                if (!disableSendChip)
                    chipRegister.setHuC6280Register(0, a & 0xf, v, EnmModel.VirtualModel);
                ld.write(a & 0xf, v, 0);
                break;
            case 3: // TIMER
                switch (a & 1) {
                case 0:
                    this.hestimReload = v & 127;
                    break;
                case 1:
                    v &= 1;
                    if (v != 0 && this.hestimStart == 0)
                        this.hestimCounter = this.hestimReload;
                    this.hestimStart = v;
                    break;
                }
                break;
            case 5: // IRQ
                switch (a & 15) {
                case 2:
                    this.ctx.iMask &= 0xffffff8f;
                    if ((v & 4) == 0) this.ctx.iMask |= Km6280.K6280Context.IRQ.TIMER.v;
                    if ((v & 2) == 0) this.ctx.iMask |= Km6280.K6280Context.IRQ.INT1.v;
                    if ((v & 1) == 0) this.ctx.iMask |= Km6280.K6280Context.IRQ.INT2.v;
                    break;
                case 3:
                    this.ctx.iRequest &= 0xffffffef;
                    break;
                }
                break;
            case 6: // CDROM
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
            case 1: // VCE
            case 4: // PAD
            case 7:
                break;
            }
        }

        public int readEvent(int a) {
            int page = this.mpr[a >> 13] & 0xff;
            if (this.memMap[page] != null)
                return this.memMap[page][a & 0x1fff];
            else if (page == 0xff)
                return readIO(a & 0x1fff);
            else
                return 0xff;
        }

        public void writeEvent(int a, int v) {
            int page = this.mpr[a >> 13] & 0xff;
            if (this.memMap[page] != null)
                this.memMap[page][a & 0x1fff] = (byte) v;
            else if (page == 0xff)
                writeIO(a & 0x1fff, v);
        }

        public int readMprEvent(int a) {
            int i;
            for (i = 0; i < 8; i++) if ((a & (1 << i)) != 0) return this.mpr[i];
            return 0xff;
        }

        public void writeMprEvent(int a, int v) {
            int i;
            if (v < 0x80 && this.memMap[v] == null) return;
            for (i = 0; i < 8; i++) if ((a & (1 << i)) != 0) this.mpr[i] = (byte) v;
        }

        public void write6270_event(int a, int v) {
            write6270(a & 0x1fff, v);
        }

        private void terminate() {
            if (this.hespcm != null) this.hespcm.release.run();
            for (int i = 0; i < 0x100; i++)
                if (this.memMap[i] != null) {
                    this.memMap = null;
                    break;
                }
        }

        @Deprecated
        private int getWordLE(byte[] p) {
            return ByteUtil.readLeShort(p);
        }

        @Deprecated
        private int getDwordLE(byte[] p) {
            return ByteUtil.readLeInt(p);
        }

        public int allocPhysicalAddress(int a, int l) {
            int page = a >> 13;
            int lastPage = (a + l - 1) >> 13;
            for (; page <= lastPage; page++) {
                if (this.memMap[page] == null) {
                    this.memMap[page] = new byte[0x2000];
                    if (this.memMap[page] == null) return 0;
                }
            }
            return 1;
        }

        public void copy_physical_address(int a, int l, byte[] p, int pP) {
            int page = a >> 13;
            int w;
            if ((a & 0x1fff) != 0) {
                w = 0x2000 - (a & 0x1fff);
                if (w > l) w = l;
                if (w >= 0) System.arraycopy(p, pP, this.memMap[page], a & 0x1fff, w);
                page++;
                //p += w;
                pP += w;
                l -= w;
            }
            while (l != 0) {
                w = Math.min(l, 0x2000);
                if (w >= 0) System.arraycopy(p, pP, this.memMap[page], 0, w);
                page++;
                //p += w;
                pP += w;
                l -= w;
            }
        }

        private int km6280_exec(Km6280.K6280Context ctx, int cycles) {
            HESHES THIS_ = ctx.user;
            int kmecycle;
            kmecycle = ctx.clock = 0;
            while (ctx.clock < cycles) {
                if (THIS_.breaked == 0) {

//                    System.err.println("pc:%4x s:%2x SPDAT0x1FF:%2x%2x",
//                        THIS_.ctx.pc,THIS_.ctx.s,
//                        THIS_.memMap[0xf8][0x1ff], THIS_.memMap[0xf8][0x1fe]
//                        );
                    // Execute 1op
                    ctx.K_EXEC();

                    if (ctx.pc == THIS_.bp) {
                        if (((THIS_.ctx.iRequest) & (THIS_.ctx.iMask ^ 0x3) & (Km6280.K6280Context.IRQ.INT1.ordinal() | Km6280.K6280Context.IRQ.TIMER.ordinal())) == 0)
                            THIS_.breaked = 1;
                    }
                } else {
                    int nextcount;
                    // break時は次のイベントまで一度に進める
                    nextcount = THIS_.kme.item[THIS_.kme.item[0].next].count;
                    if (kmEvent.kmevent_gettimer(THIS_.kme, 0, nextcount) != 0) {
                        // イベント有り
                        if (ctx.clock + nextcount < cycles)
                            ctx.clock += nextcount; // 期間中にイベント有り
                        else
                            ctx.clock = cycles; // 期間中にイベント無し
                    } else {
                        // イベント無し
                        ctx.clock = cycles;
                    }
                }
                // イベント進行
                kmEvent.kmevent_process(THIS_.kme, ctx.clock - kmecycle);
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
            this.kmEvent.kmevent_init(this.kme);

            // RAM CLEAR
            for (i = 0xf8; i <= 0xfb; i++)
                if (this.memMap[i] != null) {
                    //XMEMSET(this.memMap[i], 0, 0x2000);
                    this.memMap[i] = new byte[0x2000];
                }

            this.cps = fixDiv(HES_BASECYCLES, freq, SHIFT_CPS);
            this.ctx = new Km6280.K6280Context();
            this.ctx.user = this;
            this.ctx.readByte = HESHES::readEvent;
            this.ctx.writeByte = HESHES::writeEvent;
            this.ctx.readMPR = HESHES::readMprEvent;
            this.ctx.writeMPR = HESHES::writeMprEvent;
            this.ctx.write6270 = HESHES::write6270_event;

            this.vsync = this.kmEvent.kmevent_alloc(this.kme);
            this.timer = this.kmEvent.kmevent_alloc(this.kme);
            this.kmEvent.kmevent_setevent(this.kme, this.vsync, this::vsyncEvent, this);
            this.kmEvent.kmevent_setevent(this.kme, this.timer, this::timerEvent, this);

            this.bp = this.playerRomAddr + 3;
            for (i = 0; i < 8; i++) this.mpr[i] = this.firstMpr[i];

            this.breaked = 0;
            this.cpsRem = this.cpsGap = this.totalCycles = 0;

            //this.ctx.a = (SONGINFO_GetSongNo(this.song) - 1) & 0xff;
            this.ctx.a = (nezPlay.song.songno - 1) & 0xff;
            //this.ctx.a = (int)((49 - 1) & 0xff);
            this.ctx.p = Km6280.K6280Context.Flags.Z.v + Km6280.K6280Context.Flags.I.v;
            this.ctx.x = this.ctx.y = 0;
            this.ctx.s = 0xff;
            this.ctx.pc = this.playerRomAddr;
            this.ctx.iRequest = 0;
            this.ctx.iMask = 0xffff_ffff;// ~0;
            this.ctx.lowClockMode = 0;

            this.playerRom[0x00] = 0x20; // JSR
            this.playerRom[0x01] = (byte) ((this.initAddr >> 0) & 0xff);
            this.playerRom[0x02] = (byte) ((this.initAddr >> 8) & 0xff);
            this.playerRom[0x03] = 0x4c; // JMP
            this.playerRom[0x04] = (byte) (((this.playerRomAddr + 3) >> 0) & 0xff);
            this.playerRom[0x05] = (byte) (((this.playerRomAddr + 3) >> 8) & 0xff);

            this.hesvdcStatus = 0;
            this.hesvdcCr = 0;
            this.hesvdcAdr = 0;
            this.setUpVsync();
            this.hestimReload = this.hestimCounter = this.hestimStart = 0;
            this.setUpTimer();

            // request execute(5sec)
            initbreak = 5 << 8;

            this.disableSendChip = true;

            while (this.breaked == 0 && --initbreak != 0)
                this.km6280_exec(this.ctx, HES_BASECYCLES >> 8);

            this.disableSendChip = false;
            this.chipRegister.setHuC6280Register(0, 1, 0xff, EnmModel.VirtualModel);

            if (this.breaked != 0) {
                this.breaked = 0;
                this.ctx.p &= 0xffff_fffb; // ~Km6280.Flags.I;
            } else {
                this.ctx.a = (nezPlay.song.songno - 1) & 0xff;
                this.ctx.p = Km6280.K6280Context.Flags.Z.v + Km6280.K6280Context.Flags.I.v;
                this.ctx.x = this.ctx.y = 0;
                this.ctx.s = 0xff;
                this.ctx.pc = this.playerRomAddr;
                this.ctx.iRequest = 0;
                this.ctx.iMask = 0xffff_ffff;// ~0;
                this.ctx.lowClockMode = 0;
            }

            this.cpsRem = this.cpsGap = this.totalCycles = 0;

            // ここからメモリービュアー設定
//            memview_context = this.heshes;
//            MEM_MAX = 0xffff;
//            MEM_IO = 0x0000;
//            MEM_RAM = 0x2000;
//            MEM_ROM = 0x4000;
//            memview_memread = memview_memread_hes;
            // ここまでメモリービュアー設定

            // ここからダンプ設定
//            pNezPlayDump = pNezPlay;
//            dump_MEM_PCE = dump_MEM_PCE_bf;
//            dump_DEV_HUC6230 = dump_DEV_HUC6230_bf;
//            dump_DEV_ADPCM = dump_DEV_ADPCM_bf;
            // ここまでダンプ設定
        }

        private int load(NEZ_PLAY nezPlay, byte[] pData, int uSize) {
            int i, p;
//            XMEMSET(this., 0, sizeof(HESHES));
//            this. = new HESHES();
//            this..hessnd = 0;
//            this..hespcm = 0;
            for (i = 0; i < 0x100; i++) this.memMap[i] = null;

            if (uSize < 0x20) return Error.FORMAT.ordinal();
            nezPlay.song.startsongno = pData[5] + 1;
            nezPlay.song.songno = 256;
            nezPlay.song.channel = 2;
            nezPlay.song.extdevice = 0;
            for (i = 0; i < 8; i++) this.firstMpr[i] = pData[8 + i];
            this.playerRomAddr = 0x1ff0;
            this.initAddr = getWordLE(pData, 0x06);
            nezPlay.song.initaddress = this.initAddr;
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
                    , pData[5], this.initAddr
                    , pData[0x8]
                    , pData[0x9]
                    , pData[0xa]
                    , pData[0xb]
                    , pData[0xc]
                    , pData[0xd]
                    , pData[0xe]
                    , pData[0xf]
            );

            if (this.allocPhysicalAddress(0xf8 << 13, 0x2000) == 0) // RAM */
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0xf9 << 13, 0x2000) == 0) // SGX-RAM */
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0xfa << 13, 0x2000) == 0) // SGX-RAM */
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0xfb << 13, 0x2000) == 0) // SGX-RAM */
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0x00 << 13, 0x2000) == 0) // IPL-ROM */
                return Error.SHORTOFMEMORY.ordinal();
            for (p = 0x10; p + 0x10 < uSize; p += 0x10 + getDwordLE(pData, p + 4)) {
                if (getDwordLE(pData, p) == 0x41544144) { // 'DATA'
                    int a, l;
                    l = getDwordLE(pData, p + 4);
                    a = getDwordLE(pData, p + 8);
                    if (this.allocPhysicalAddress(a, l) == 0) return Error.SHORTOFMEMORY.ordinal();
                    if (l > uSize - p - 0x10) l = uSize - p - 0x10;
                    int q = p + 0x10;
                    this.copy_physical_address(a, l, pData, q);
                    p = q;
                }
            }
            //this..hessnd = HESSoundAlloc();
            //if (this..hessnd == 0) return NESERR_SHORTOFMEMORY;
            this.hespcm = (new S_Hesad()).HESAdPcmAlloc();
            if (this.hespcm == null) return Error.SHORTOFMEMORY.ordinal();

            return Error.NOERROR.ordinal();
        }
    }

     // ここからメモリービュアー設定
    public interface memview_memread extends Function<Integer, Integer> {
    }

    private HESHES memview_context = null;

    //private int MEM_MAX, MEM_IO, MEM_RAM, MEM_ROM;
    private int memview_memread_hes(int a) {
        if (a >= 0x1800 && a < 0x1c00 && (a & 0xf) == 0xa) return 0xff;
        return memview_context.readEvent(a);
    }
     // ここまでメモリービュアー設定

     // ここからダンプ設定
    //private NEZ_PLAY pNezPlayDump;
    public interface dump_MEM_PCE extends BiFunction<Integer, byte[], Integer> {
    }

    private int dump_MEM_PCE_bf(int menu, byte[] mem) {
        int i;
        switch (menu) {
        case 1: // Memory
            for (i = 0; i < 0x10000; i++)
                mem[i] = (byte) memview_memread_hes(i);
            return i;
        }
        return 0xfffffffe;// (-2);
    }
}
