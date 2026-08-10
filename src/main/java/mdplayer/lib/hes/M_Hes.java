package mdplayer.lib.hes;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import mdplayer.lib.hes.KmEvent.Event;
import vavi.util.ByteUtil;
import vavi.util.compat.TriConsumer;


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

    public static class NezPlay {

        public static class SongInfo {
            public int songno;
            public int maxsongno;
            int startsongno;
            int extdevice;
            int initaddress;
            int playaddress;
            int channel;
            public int initlimit;
        }

        public HESHES heshes;
        public final SongInfo song = new SongInfo();

        static class SongInfoData {
            public String title;
            public String artist;
            public String copyright;
            String detail;
        }

        private final SongInfoData _songinfodata = new SongInfoData();

        public int executeHES() {
            return this.heshes != null ? this.heshes.execute() : 0;
        }

        public void HESSoundRenderStereo(int[] d) {
            this.heshes.synth(d);
        }

        public int HESSoundRenderMono() {
            int[] d = {0, 0};
            this.heshes.synth(d);
//#if (((-1) >> 1) == -1)
//	          return (d[0] + d[1]) >> 1;
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
    private static final int HES_BASECYCLES = 21477270;
    private static final int HES_TIMERCYCLES = 1024 * 3;

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

        Km6280 ctx;
        //public KMIF_SOUND_DEVICE hessnd;
        KmifSoundDevice hespcm;
        final Event kme = new Event();
        int vsync;
        int timer;

        /** break point */
        int bp;
        /** break point flag */
        int broken;

        /** cycles per sample:fixed point */
        int cps;
        /** cycle remain */
        int cpsRem;
        /** cycle gap */
        int cpsGap;
        /** total played cycles */
        int totalCycles;

        final byte[] mpr = new byte[0x8];
        final byte[] firstMpr = new byte[0x8];
        byte[][] memMap = new byte[0x100][];
        int initAddr;

        int playerRomAddr;
        final byte[] playerRom = new byte[0x10];

        /** IO $C01 ($C00)*/
        int hesTimReload;
        /** IO $C00 */
        int hesTimCounter;
        /** IO $C01 */
        int hesTimStart;
        int hesVdcStatus;
        int hesVdcCr;
        int hesVdcAdr;

        private final KmEvent kmEvent = new KmEvent();

        public int freqency;
        public BiConsumer<Integer, Integer> huC6280Write;
        public Hes.HESDetector ld;
        private boolean disableSendChip = false;

        private static int getWordLE(byte[] p, int ptr) {
            return ByteUtil.readLeShort(p, ptr);
        }

        private static int getDwordLE(byte[] p, int ptr) {
            if (p.length <= ptr + 3) return 0;
            int r = ByteUtil.readLeInt(p, ptr);
            return r;
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

        void vsyncEvent(Event _event, int curid, HESHES _this) {
            _this.setUpVsync();
            if ((_this.hesVdcCr & 8) != 0) {
                _this.ctx.iRequest |= Km6280.IRQ.INT1.v;
                //logger.log(Level.TRACE, "vsyncEvent");
                _this.broken = 0;
            }
            _this.hesVdcStatus = 1;
        }

        private static void timerEvent(Event _event, int curid, HESHES _this) {
            if (_this.hesTimStart != 0 && _this.hesTimCounter-- == 0) {
                _this.hesTimCounter = _this.hesTimReload;
                _this.ctx.iRequest |= Km6280.IRQ.TIMER.v;
                //logger.log(Level.TRACE, "timerEvent");
                _this.broken = 0;
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
                int exCycles = cycles - this.cpsGap;
                this.cpsGap = km6280_exec(this.ctx, exCycles) - exCycles;
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
            kmEvent.setTimer(this.kme, this.vsync, 4 * 342 * 262);
        }

        private void setUpTimer() {
            kmEvent.setTimer(this.kme, this.timer, HES_TIMERCYCLES);
        }

        private void write6270(int a, int v) {
            switch (a) {
            case 0:
                this.hesVdcAdr = v;
                break;
            case 2:
                switch (this.hesVdcAdr) {
                case 5: // CR */
                    this.hesVdcCr = v;
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
                if (this.hesVdcStatus != 0) {
                    this.hesVdcStatus = 0;
                    v = 0x20;
                }
                this.ctx.iRequest &= 0xffff_ffdf;// ~Km6280.IRQ.INT1;
//#if 0
//                v = 0x20;	// Always VSYNC period
//#endif
            }
            return v;
        }

        private int readIO(int a) {
            switch (a >> 10) {
            case 0: // VDC
                return read6270(a & 3);
            case 2: // Psg
                //return this.hessnd.read(this.hessnd.ctx, a & 0xf);
                return 0;
            case 3: // TIMER
                if ((a & 1) != 0)
                    return this.hesTimStart;
                else
                    return this.hesTimCounter;
            case 5: // IRQ
                switch (a & 15) {
                case 2: {
                    int v = 0xf8;
                    if ((this.ctx.iMask & Km6280.IRQ.TIMER.v) == 0) v |= 4;
                    if ((this.ctx.iMask & Km6280.IRQ.INT1.v) == 0) v |= 2;
                    if ((this.ctx.iMask & Km6280.IRQ.INT2.v) == 0) v |= 1;
                    return v;
                }
                case 3: {
                    int v = 0;
                    if ((this.ctx.iRequest & Km6280.IRQ.TIMER.v) != 0) v |= 4;
                    if ((this.ctx.iRequest & Km6280.IRQ.INT1.v) != 0) v |= 2;
                    if ((this.ctx.iRequest & Km6280.IRQ.INT2.v) != 0) v |= 1;
//#if 0
//                    THIS_->ctx.iRequest &= ~(TIMER | INT1 | INT2);
//#endif
                    return v;
                }
                }
                return 0x00;
            case 7:
                a -= this.playerRomAddr;
                if (a >= 0 && a < 0x10) return this.playerRom[a] & 0xff; // TODO vavi
                return 0xff;
            case 6: // CDROM
                return switch (a & 15) { // for debug
                    case 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f -> // for debug
                            this.hespcm.read.apply(a & 0xf);
                    default -> 0xff;
                };
                default:
            case 1: // VCE
            case 4: // PAD
                return 0xff;
            }
        }

        void writeIO(int a, int v) {
            switch (a >> 10) {
            case 0: // VDC
                write6270(a & 3, v);
                break;
            case 2: // Psg
                //logger.log(Level.TRACE, "Adr:%2X Dat:%2X".formatted((int) (a & 0xf), (int) v));
                if (!disableSendChip)
                    huC6280Write.accept(a & 0xf, v);
                ld.write(a & 0xf, v, 0);
                break;
            case 3: // TIMER
                switch (a & 1) {
                case 0:
                    this.hesTimReload = v & 127;
                    break;
                case 1:
                    v &= 1;
                    if (v != 0 && this.hesTimStart == 0)
                        this.hesTimCounter = this.hesTimReload;
                    this.hesTimStart = v;
                    break;
                }
                break;
            case 5: // IRQ
                switch (a & 15) {
                case 2:
                    this.ctx.iMask &= 0xffffff8f;
                    if ((v & 4) == 0) this.ctx.iMask |= Km6280.IRQ.TIMER.v;
                    if ((v & 2) == 0) this.ctx.iMask |= Km6280.IRQ.INT1.v;
                    if ((v & 1) == 0) this.ctx.iMask |= Km6280.IRQ.INT2.v;
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

        int readEvent(int a) {
            int page = this.mpr[a >> 13] & 0xff;
            if (this.memMap[page] != null)
                return this.memMap[page][a & 0x1fff] & 0xff;
            else if (page == 0xff)
                return readIO(a & 0x1fff);
            else
                return 0xff;
        }

        void writeEvent(int a, int v) {
            int page = this.mpr[a >> 13] & 0xff;
            if (this.memMap[page] != null)
                this.memMap[page][a & 0x1fff] = (byte) v;
            else if (page == 0xff)
                writeIO(a & 0x1fff, v);
        }

        int readMprEvent(int a) {
            int i;
            for (i = 0; i < 8; i++) if ((a & (1 << i)) != 0) return this.mpr[i] & 0xff;
            return 0xff;
        }

        void writeMprEvent(int a, int v) {
            int i;
            if (v < 0x80 && this.memMap[v] == null) return;
            for (i = 0; i < 8; i++) if ((a & (1 << i)) != 0) this.mpr[i] = (byte) v;
        }

        void write6270_event(int a, int v) {
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

        int allocPhysicalAddress(int a, int l) {
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

        void copy_physical_address(int a, int l, byte[] p, /* ref */ int[] pP) {
            int page = a >> 13;
            int w;
            if ((a & 0x1fff) != 0) {
                w = 0x2000 - (a & 0x1fff);
                if (w > l) w = l;
                if (w >= 0) System.arraycopy(p, pP[0], this.memMap[page], a & 0x1fff, w);
                page++;
                //p += w;
                pP[0] += w;
                l -= w;
            }
            while (l != 0) {
                w = Math.min(l, 0x2000);
                if (w >= 0) System.arraycopy(p, pP[0], this.memMap[page], 0, w);
                page++;
                //p += w;
                pP[0] += w;
                l -= w;
            }
        }

        private int km6280_exec(Km6280 ctx, int cycles) {
            HESHES THIS_ = ctx.user;
            int kmecycle;
            kmecycle = ctx.clock = 0;
            while (ctx.clock < cycles) {
                if (THIS_.broken == 0) {

//logger.log(Level.TRACE, "pc:%4x s:%2x SPDAT0x1FF:%2x%2x",formatted(THIS_.ctx.pc, THIS_.ctx.s, THIS_.memMap[0xf8][0x1ff], THIS_.memMap[0xf8][0x1fe]));
                    // Execute 1op
                    ctx.K_EXEC();

                    if (ctx.pc == THIS_.bp) {
                        if (((THIS_.ctx.iRequest) & (THIS_.ctx.iMask ^ 0x3) & (Km6280.IRQ.INT1.ordinal() | Km6280.IRQ.TIMER.ordinal())) == 0)
                            THIS_.broken = 1;
                    }
                } else {
                    int[] nextCount = new int[1];
                    // When you break, advance to the next event in one go.
                    nextCount[0] = THIS_.kme.item[THIS_.kme.item[0].next].count;
                    if (kmEvent.getTimer(THIS_.kme, 0, nextCount) != 0) {
                        // There is an event
                        if (ctx.clock + nextCount[0] < cycles)
                            ctx.clock += nextCount[0]; // There will be events during the period
                        else
                            ctx.clock = cycles; // No events during this period
                    } else {
                        // No event
                        ctx.clock = cycles;
                    }
                }
                // Process Event
                kmEvent.process(THIS_.kme, ctx.clock - kmecycle);
                kmecycle = ctx.clock;
            }
            ctx.clock = 0;
            return kmecycle;
        }

        private void reset(NezPlay nezPlay) {
            int i, initbreak;
            //int freq = NESAudioFrequencyGet(pNezPlay);
            int freq = freqency;

            //this.hessnd.reset(this.hessnd.ctx, HES_BASECYCLES, freq);
            this.hespcm.reset.accept(HES_BASECYCLES, freq);
            this.kmEvent.init(this.kme);

            // RAM CLEAR
            for (i = 0xf8; i <= 0xfb; i++)
                if (this.memMap[i] != null) {
                    //XMEMSET(this.memMap[i], 0, 0x2000);
                    this.memMap[i] = new byte[0x2000];
                }

            this.cps = fixDiv(HES_BASECYCLES, freq, SHIFT_CPS);
            this.ctx = new Km6280();
            this.ctx.user = this;
            this.ctx.readByte = HESHES::readEvent;
            this.ctx.writeByte = HESHES::writeEvent;
            this.ctx.readMPR = HESHES::readMprEvent;
            this.ctx.writeMPR = HESHES::writeMprEvent;
            this.ctx.write6270 = HESHES::write6270_event;

            this.vsync = this.kmEvent.alloc(this.kme);
            this.timer = this.kmEvent.alloc(this.kme);
            this.kmEvent.setEvent(this.kme, this.vsync, this::vsyncEvent, this);
            this.kmEvent.setEvent(this.kme, this.timer, HESHES::timerEvent, this);

            this.bp = this.playerRomAddr + 3;
            for (i = 0; i < 8; i++) this.mpr[i] = this.firstMpr[i];

            this.broken = 0;
            this.cpsRem = this.cpsGap = this.totalCycles = 0;

            //this.ctx.a = (SONGINFO_GetSongNo(this.song) - 1) & 0xff;
            this.ctx.a = (nezPlay.song.songno - 1) & 0xff;
            //this.ctx.a = (int)((49 - 1) & 0xff);
            this.ctx.p = Km6280.Flags.Z.v + Km6280.Flags.I.v;
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

            this.hesVdcStatus = 0;
            this.hesVdcCr = 0;
            this.hesVdcAdr = 0;
            this.setUpVsync();
            this.hesTimReload = this.hesTimCounter = this.hesTimStart = 0;
            this.setUpTimer();

            // request execute(5sec)
            initbreak = 5 << 8;

            this.disableSendChip = true;

            while (this.broken == 0 && --initbreak != 0)
                this.km6280_exec(this.ctx, HES_BASECYCLES >> 8);

            this.disableSendChip = false;
            this.huC6280Write.accept(1, 0xff);

            if (this.broken != 0) {
                this.broken = 0;
                this.ctx.p &= 0xffff_fffb; // ~Km6280.Flags.I;
            } else {
                this.ctx.a = (nezPlay.song.songno - 1) & 0xff;
                this.ctx.p = Km6280.Flags.Z.v + Km6280.Flags.I.v;
                this.ctx.x = this.ctx.y = 0;
                this.ctx.s = 0xff;
                this.ctx.pc = this.playerRomAddr;
                this.ctx.iRequest = 0;
                this.ctx.iMask = 0xffff_ffff;// ~0;
                this.ctx.lowClockMode = 0;
            }

            this.cpsRem = this.cpsGap = this.totalCycles = 0;

            // Memory viewer settings from here
//            memview_context = this.heshes;
//            MEM_MAX = 0xffff;
//            MEM_IO = 0x0000;
//            MEM_RAM = 0x2000;
//            MEM_ROM = 0x4000;
//            memview_memread = memview_memread_hes;
            // Memory viewer settings up to here

            // Dump settings from here
//            pNezPlayDump = pNezPlay;
//            dump_MEM_PCE = dump_MEM_PCE_bf;
//            dump_DEV_HUC6230 = dump_DEV_HUC6230_bf;
//            dump_DEV_ADPCM = dump_DEV_ADPCM_bf;
            // Dump settings up to here
        }

        private int load(NezPlay nezPlay, byte[] pData, int uSize) {
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

            nezPlay._songinfodata.detail = """
                    Type           : HES
                            "Start Song: %2x
                            "Init Address: %4x
                            "First Mapper 0 : %2x
                            "First Mapper 1 : %2x
                            "First Mapper 2 : %2x
                            "First Mapper 3 : %2x
                            "First Mapper 4 : %2x
                            "First Mapper 5 : %2x
                            "First Mapper 6 : %2x
                            "First Mapper 7 : %2x""".formatted(
                                    pData[5], this.initAddr,
                                    pData[0x8],
                                    pData[0x9],
                                    pData[0xa],
                                    pData[0xb],
                                    pData[0xc],
                                    pData[0xd],
                                    pData[0xe],
                                    pData[0xf]
            );

            if (this.allocPhysicalAddress(0xf8 << 13, 0x2000) == 0) // RAM
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0xf9 << 13, 0x2000) == 0) // SGX-RAM
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0xfa << 13, 0x2000) == 0) // SGX-RAM
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0xfb << 13, 0x2000) == 0) // SGX-RAM
                return Error.SHORTOFMEMORY.ordinal();
            if (this.allocPhysicalAddress(0x00 << 13, 0x2000) == 0) // IPL-ROM
                return Error.SHORTOFMEMORY.ordinal();
            for (p = 0x10; p + 0x10 < uSize; p += 0x10 + (getDwordLE(pData, p + 4) > 0 ? getDwordLE(pData, p + 4) : uSize)) {
                if (getDwordLE(pData, p) == 0x41544144) { // 'DATA'
                    int a, l;
                    l = getDwordLE(pData, p + 4);
                    a = getDwordLE(pData, p + 8);
                    if (this.allocPhysicalAddress(a, l) == 0) return Error.SHORTOFMEMORY.ordinal();
                    if (l > uSize - p - 0x10) l = uSize - p - 0x10;
                    int[] q = {p + 0x10};
                    this.copy_physical_address(a, l, pData, /* ref */ q);
                    p = q[0];
                }
            }
            //this..hessnd = HESSoundAlloc();
            //if (this..hessnd == 0) return NESERR_SHORTOFMEMORY;
            this.s_head = new S_Hesad();
            this.hespcm = s_head.HESAdPcmAlloc(s_head);
            if (this.hespcm == null) return Error.SHORTOFMEMORY.ordinal();

            return Error.NOERROR.ordinal();
        }

        S_Hesad s_head;
    }

    // Memory viewer settings from here

    public interface memview_memread extends Function<Integer, Integer> {
    }

    private final HESHES memview_context = null;

    //private int MEM_MAX, MEM_IO, MEM_RAM, MEM_ROM;
    private int memview_memread_hes(int a) {
        if (a >= 0x1800 && a < 0x1c00 && (a & 0xf) == 0xa) return 0xff;
        return memview_context.readEvent(a);
    }

    // Memory viewer settings up to here

    // Dump settings from here

//    private NEZ_PLAY pNezPlayDump;

    public interface dump_MEM_PCE extends BiFunction<Integer, byte[], Integer> {
    }

    private int dump_MEM_PCE_bf(int menu, byte[] mem) {
        int i;
        return switch (menu) {
            case 1 -> {
                for (i = 0; i < 0x10000; i++)
                    mem[i] = (byte) memview_memread_hes(i);
                yield i;
            }
            default -> 0xffff_fffe;
        };
    }
}
