package mdplayer.driver.nsf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.NesChip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.plugin.BasePlugin;
import mdsound.np.DCFilter;
import mdsound.np.Device;
import mdsound.np.Filter;
import mdsound.np.LoopDetector;
import mdsound.np.NpNesApu;
import mdsound.np.NpNesDmc;
import mdsound.np.NpNesFds;
import mdsound.np.chip.NesApu;
import mdsound.np.chip.NesDmc;
import mdsound.np.chip.NesFds;
import mdsound.np.chip.NesFme7;
import mdsound.np.chip.NesMmc5;
import mdsound.np.chip.NesN106;
import mdsound.np.chip.NesVrc6;
import mdsound.np.chip.NesVrc7;
import mdsound.np.cpu.Km6502;
import mdsound.np.memory.NesBank;
import mdsound.np.memory.NesMem;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


public class Nsf extends BaseDriver {

    private static final Logger logger = getLogger(Nsf.class.getName());

    public Nsf() {
        rate = setting.getOutputDevice().getSampleRate();
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (ByteUtil.readLeInt(buf, 0) != FCC_NSF) {
            // NSFeはとりあえず未サポート
logger.log(Level.WARNING, "NSFe not supported.");
            return null;
        }

        if (buf.length < 0x80) { // no header?
logger.log(Level.WARNING, "no header?");
            return null;
        }

        version = buf[0x05] & 0xff;
        songs = buf[0x06] & 0xff;
        start = buf[0x07] & 0xff;
        load_address = (buf[0x08] & 0xff) | ((buf[0x09] & 0xff) << 8);
        initAddress = (buf[0x0a] & 0xff) | ((buf[0x0B] & 0xff) << 8);
        playAddress = (buf[0x0c] & 0xff) | ((buf[0x0D] & 0xff) << 8);

        List<Byte> strLst = new ArrayList<>();
        int tagAdr = 0x0e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        title_nsf = new String(ByteUtil.toByteArray(strLst), Charset.forName("MS932"));
        title = title_nsf;

        strLst.clear();
        tagAdr = 0x2e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        artist_nsf = new String(ByteUtil.toByteArray(strLst), Charset.forName("MS932"));
        artist = artist_nsf;

        //memcpy(copyright_nsf, image + 0x4e, 32);
        //copyright_nsf[31] = '\0';
        strLst.clear();
        tagAdr = 0x4e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        copyrightNsf = new String(ByteUtil.toByteArray(strLst), Charset.forName("MS932"));
        copyright = copyrightNsf;

        ripper = ""; // NSFe only
        text = ""; // NSFe only
        text_len = 0; // NSFe only
        speedNtsc = (buf[0x6e] & 0xff) | ((buf[0x6f] & 0xff) << 8);
        System.arraycopy(buf, 112, bankSwitch, 0, 8);
        speedPal = (buf[0x78] & 0xff) | ((buf[0x79] & 0xff) << 8);
        palNtsc = buf[0x7a] & 0xff;

        if (speedPal == 0)
            speedPal = 0x4e20;
        if (speedNtsc == 0)
            speedNtsc = 0x411A;

        soundChip = buf[0x7b] & 0xff;

        useVrc6 = (soundChip & 1) != 0;
        useVrc7 = (soundChip & 2) != 0;
        useFds = (soundChip & 4) != 0;
        useMmc5 = (soundChip & 8) != 0;
        useN106 = (soundChip & 16) != 0;
        useFme7 = (soundChip & 32) != 0;
logger.log(Level.INFO, "%s%s%s%s%s%s".formatted(useVrc6 ? "6" : "_", useVrc7 ? "7" : "_", useFds ? "F" : "_", useMmc5 ? "M" : "_", useN106 ? "N" : "_", useFme7 ? "F" : "_"));

        System.arraycopy(buf, 124, extra, 0, 4);

        //delete[] body;
        //body = new UINT8[size - 0x80];
        body = new byte[buf.length - 0x80];
        System.arraycopy(buf, 128, body, 0, buf.length - 0x80);

        bodySize = buf.length - 0x80;

        //song = start - 1;

        Vgm.Gd3 gd3 = new Vgm.Gd3();
        gd3.gameName = title;
        gd3.gameNameJ = title;
        gd3.composer = artist;
        gd3.composerJ = artist;
        gd3.trackName = title;
        gd3.trackNameJ = title;
        gd3.systemName = copyright;
        gd3.systemNameJ = copyright;

        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        this.vgmBuf = vgmBuf;
        this.plugin = plugin;
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

        gd3 = getGD3Info(vgmBuf);

        nsfInit();

        return true;
    }

    @Override
    public void processOneFrame() {
        if (model == EnmModel.RealModel) return;

        try {
            vgmSpeedCounter += vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    //oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private static final int FCC_NSF = 0x4d53454e; // "NESM"

    private int version;
    public int songs;
    private int start;
    private int load_address;
    private int initAddress;
    private int playAddress;
    private String filename;
    // margin 64 chars.
    private String printTitle;
    private String title_nsf;
    private String artist_nsf;
    private String copyrightNsf;
    private String title;
    private String artist;
    private String copyright;
    // NSFe only
    private String ripper;
    // NSFe only
    private String text;
    // NSFe only
    private int text_len;
    private int speedNtsc;
    private byte[] bankSwitch = new byte[8];
    private int speedPal;
    private int palNtsc;
    private int soundChip;
    public boolean useVrc7;
    public boolean useVrc6;
    public boolean useFds;
    public boolean useFme7;
    public boolean useMmc5;
    public boolean useN106;
    private byte[] extra = new byte[4];
    private byte[] body;
    private int bodySize;
    private byte[] nsfeImage;
    public byte[] nsfePlst;
    public int nsfePlstSize;
    private static final int NSFE_ENTRIES = 256;

    public static class NsfeEntry {

        public byte[] tlbl;
        public int time;
        public int fade;
    }

    public NsfeEntry[] nsfeEntry = new NsfeEntry[NSFE_ENTRIES];

    /**
     * 現在選択中の曲番号
     */
    public int song;

    private Device.Bus apuBus;

    private Device.Bus stack;
    private Device.Layer layer;
    /** 最終出力段に掛ける直流フィルタ */
    private mdsound.np.DCFilter dcf;
    /** 最終出力に掛けるローパスフィルタ */
    private mdsound.np.Filter lpf;

    public mdsound.MDSound.Chip cAPU = null;
    public mdsound.MDSound.Chip cDMC = null;
    public mdsound.MDSound.Chip cFDS = null;
    public mdsound.MDSound.Chip cMMC5 = null;
    public mdsound.MDSound.Chip cN160 = null;
    public mdsound.MDSound.Chip cVRC6 = null;
    public mdsound.MDSound.Chip cVRC7 = null;
    public mdsound.MDSound.Chip cFME7 = null;

    private LoopDetector.NESDetector ld = null;
//    private NESDetectorEx ld = null;

    private final double rate; // setting.getoutputDevice().SampleRate;
    private double cpu_clock_rest;
    private double apu_clock_rest;
    private int time_in_ms;
    private long silent_length = 0;
    private int last_out = 0;

    private void nsfInit() {
        plugin.audio.chipRegister.chip(NesChip.class).bank = new NesBank();
        plugin.audio.chipRegister.chip(NesChip.class).mem = new NesMem();
        plugin.audio.chipRegister.chip(NesChip.class).cpu = new Km6502(0);
        plugin.audio.chipRegister.chip(NesChip.class).apu = new NesApu();
        plugin.audio.chipRegister.chip(NesChip.class).dmc = new NesDmc();
        plugin.audio.chipRegister.chip(NesChip.class).fds = new NesFds();
        plugin.audio.chipRegister.chip(NesChip.class).n106 = new NesN106();
        plugin.audio.chipRegister.chip(NesChip.class).vrc6 = new NesVrc6();
        plugin.audio.chipRegister.chip(NesChip.class).mmc5 = new NesMmc5();
        plugin.audio.chipRegister.chip(NesChip.class).fme7 = new NesFme7();
        plugin.audio.chipRegister.chip(NesChip.class).vrc7 = new NesVrc7();

        plugin.audio.chipRegister.chip(NesChip.class).apu.apu = new NpNesApu(Common.NsfClock, setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).apu.reset();
        plugin.audio.chipRegister.chip(NesChip.class).dmc.dmc = new NpNesDmc(Common.NsfClock, setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).dmc.reset();
        plugin.audio.chipRegister.chip(NesChip.class).fds.fds = new NpNesFds(Common.NsfClock, setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).fds.reset();
        plugin.audio.chipRegister.chip(NesChip.class).n106.setClock(Common.NsfClock);
        plugin.audio.chipRegister.chip(NesChip.class).n106.setRate(setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).n106.reset();
        plugin.audio.chipRegister.chip(NesChip.class).vrc6.setClock(Common.NsfClock);
        plugin.audio.chipRegister.chip(NesChip.class).vrc6.setRate(setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).vrc6.reset();
        plugin.audio.chipRegister.chip(NesChip.class).mmc5.setClock(Common.NsfClock);
        plugin.audio.chipRegister.chip(NesChip.class).mmc5.setRate(setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).mmc5.reset();
        plugin.audio.chipRegister.chip(NesChip.class).mmc5.setCPU(plugin.audio.chipRegister.chip(NesChip.class).cpu);
        plugin.audio.chipRegister.chip(NesChip.class).fme7.setClock(Common.NsfClock);
        plugin.audio.chipRegister.chip(NesChip.class).fme7.setRate(setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).fme7.reset();
        plugin.audio.chipRegister.chip(NesChip.class).vrc7.setClock(Common.NsfClock);
        plugin.audio.chipRegister.chip(NesChip.class).vrc7.setRate(setting.getOutputDevice().getSampleRate());
        plugin.audio.chipRegister.chip(NesChip.class).vrc7.reset();

        plugin.audio.chipRegister.chip(NesChip.class).dmc.dmc.nes_apu = plugin.audio.chipRegister.chip(NesChip.class).apu.apu;
        plugin.audio.chipRegister.chip(NesChip.class).dmc.dmc.setAPU(plugin.audio.chipRegister.chip(NesChip.class).apu.apu);

        stack = new Device.Bus();
        layer = new Device.Layer();
        apuBus = new Device.Bus();

        dcf = new DCFilter();
        lpf = new Filter();
        lpf.setRate(setting.getOutputDevice().getSampleRate());
        lpf.reset();
        dcf.setRate(setting.getOutputDevice().getSampleRate());
        dcf.reset();
        dcf.setParam(270, 256 - setting.getNsf().getHPF()); // HPF:256-(Range0-256(Def:92))
        lpf.SetParam(4700.0, setting.getNsf().getLPF()); // LPF:(Range 0-400(Def:112))
        //logger.log(Level.TRACE, "dcf:%d".formatted(dcf.GetFactor()));
        //logger.log(Level.TRACE, "lpf:%d".formatted(lpf.GetFactor()));

        int i, bmax = 0;

        for (i = 0; i < 8; i++)
            if (bmax < bankSwitch[i])
                bmax = bankSwitch[i];

        plugin.audio.chipRegister.chip(NesChip.class).mem.setImage(body, load_address & 0xffff, bodySize);

        if (bmax != 0) {
            plugin.audio.chipRegister.chip(NesChip.class).bank.setImage(body, load_address & 0xffff, bodySize);
            for (i = 0; i < 8; i++)
                plugin.audio.chipRegister.chip(NesChip.class).bank.setBankDefault(i + 8, bankSwitch[i]);
        }

        stack.detachAll();
        layer.detachAll();
        apuBus.detachAll();

        ld = new LoopDetector.NESDetector(0);
        ld.reset();
        stack.attach(ld);

        apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).apu);
        apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).dmc);

        plugin.audio.chipRegister.chip(NesChip.class).apu.setOption(NpNesApu.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getNESUnmuteOnReset() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).apu.setOption(NpNesApu.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getNESNonLinearMixer() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).apu.setOption(NpNesApu.OPT.PHASE_REFRESH.ordinal(), setting.getNsf().getNESPhaseRefresh() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).apu.setOption(NpNesApu.OPT.DUTY_SWAP.ordinal(), setting.getNsf().getNESDutySwap() ? 1 : 0);

        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.ENABLE_4011.ordinal(), setting.getNsf().getDMCEnable4011() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.ENABLE_PNOISE.ordinal(), setting.getNsf().getDMCEnablePnoise() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getDMCUnmuteOnReset() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.DPCM_ANTI_CLICK.ordinal(), setting.getNsf().getDMCDPCMAntiClick() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getDMCNonLinearMixer() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.RANDOMIZE_NOISE.ordinal(), setting.getNsf().getDMCRandomizeNoise() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.TRI_MUTE.ordinal(), setting.getNsf().getDMCTRImute() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.RANDOMIZE_TRI.ordinal(), setting.getNsf().getDMCRandomizeTRI() ? 1 : 0);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setOption(NpNesDmc.OPT.DPCM_REVERSE.ordinal(), setting.getNsf().getDMCDPCMReverse() ? 1 : 0);

        if (useFds) {
            boolean write_enable = !setting.getNsf().getFDSWriteDisable8000();
            plugin.audio.chipRegister.chip(NesChip.class).fds.setOption(0, setting.getNsf().getFDSLpf());
            plugin.audio.chipRegister.chip(NesChip.class).fds.setOption(1, setting.getNsf().getFDS4085Reset() ? 1 : 0);
            plugin.audio.chipRegister.chip(NesChip.class).mem.setFDSMode(write_enable);
            plugin.audio.chipRegister.chip(NesChip.class).bank.setFDSMode(write_enable);
            plugin.audio.chipRegister.chip(NesChip.class).bank.setBankDefault((byte) 6, bankSwitch[6]);
            plugin.audio.chipRegister.chip(NesChip.class).bank.setBankDefault((byte) 7, bankSwitch[7]);
            apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).fds);
        } else {
            plugin.audio.chipRegister.chip(NesChip.class).mem.setFDSMode(false);
            plugin.audio.chipRegister.chip(NesChip.class).bank.setFDSMode(false);
        }
        if (useN106) {
            plugin.audio.chipRegister.chip(NesChip.class).n106.setOption(0, setting.getNsf().getN160Serial() ? 1 : 0);
            apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).n106);
        }
        if (useVrc6) {
            apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).vrc6);
        }
        if (useMmc5) {
            plugin.audio.chipRegister.chip(NesChip.class).mmc5.setOption(0, setting.getNsf().getMMC5NonLinearMixer() ? 1 : 0);
            plugin.audio.chipRegister.chip(NesChip.class).mmc5.setOption(1, setting.getNsf().getMMC5PhaseRefresh() ? 1 : 0);
            apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).mmc5);
        }
        if (useFme7) {
            apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).fme7);
        }
        if (useVrc7) {
            apuBus.attach(plugin.audio.chipRegister.chip(NesChip.class).vrc7);
        }

        if (bmax > 0) layer.attach(plugin.audio.chipRegister.chip(NesChip.class).bank);
        layer.attach(plugin.audio.chipRegister.chip(NesChip.class).mem);

        stack.attach(apuBus);
        stack.attach(layer);

        plugin.audio.chipRegister.chip(NesChip.class).cpu.setMemory(stack);
        plugin.audio.chipRegister.chip(NesChip.class).dmc.setMemory(stack);

        plugin.audio.chipRegister.chip(NesChip.class).apu.apu.squareTable[0] = 0;
        for (i = 1; i < 32; i++)
            plugin.audio.chipRegister.chip(NesChip.class).apu.apu.squareTable[i] = (int) ((8192.0 * 95.88) / (8128.0 / i + 100));

        for (int c = 0; c < 2; ++c)
            for (int t = 0; t < 2; ++t)
                plugin.audio.chipRegister.chip(NesChip.class).apu.apu.sm[c][t] = 128;

        reset();
    }

    public enum Region {
        NTSC,
        PAL,
        DENDY
    }

    private void reset() {
        apu_clock_rest = 0.0;
        cpu_clock_rest = 0.0;
        silent_length = 0;

        Region region = getRegion(palNtsc);
        double speed;
        speed = 1000000.0 / ((region == Region.NTSC) ? speedNtsc : speedPal);

        layer.reset();
        plugin.audio.chipRegister.chip(NesChip.class).cpu.reset();

        plugin.audio.chipRegister.chip(NesChip.class).cpu.start(initAddress, playAddress, speed, song, (region == Region.PAL) ? 1 : 0, 0);
    }

    private static Region getRegion(int flags) {
        int pref = 0;

        // user forced region
        if (pref == 3) return Region.NTSC;
        if (pref == 4) return Region.PAL;
        if (pref == 5) return Region.DENDY;

        // single-mode NSF
        if (flags == 0) return Region.NTSC;
        if (flags == 1) return Region.PAL;

        if ((flags & 2) != 0) { // dual mode
            if (pref == 1) return Region.NTSC;
            if (pref == 2) return Region.PAL;
            // else pref == 0 or invalid, use auto setting based on Flags bit
            return ((flags & 1) != 0) ? Region.PAL : Region.NTSC;
        }

        return Region.NTSC; // fallback for invalid Flags
    }

    public int render_(short[] b, int length) {
        return render_(b, length, 0);
    }

    public int render_(short[] b, int length, int offset) {
        assert model != EnmModel.RealModel;
        assert plugin.audio.chipRegister != null;

        if (vgmFrameCounter < 0) {
            vgmFrameCounter += length;
//logger.log(Level.DEBUG, "vgmFrameCounter: " + vgmFrameCounter);
            return length;
        }

        int[] buf = new int[2];
        int[] out = new int[2];
        int outm;
        int i;
        int master_volume;

        master_volume = 0x80;

        double apu_clock_per_sample = 0;
        NesChip nesChip = plugin.audio.chipRegister.chip(NesChip.class);
        if (nesChip.cpu != null) {
            apu_clock_per_sample = nesChip.cpu.NES_BASECYCLES / rate;
        }
        double cpu_clock_per_sample = apu_clock_per_sample * vgmSpeed;

        for (i = 0; i < length; i++) {
            //total_render++;
            vgmSpeedCounter += vgmSpeed;
            counter = (int) vgmSpeedCounter;
            vgmFrameCounter++;

            // tick CPU
            cpu_clock_rest += cpu_clock_per_sample;
            int cpu_clocks = (int) cpu_clock_rest;
            if (cpu_clocks > 0) {
                int real_cpu_clocks = nesChip.cpu.exec(cpu_clocks);
                cpu_clock_rest -= real_cpu_clocks;

                // tick APU frame sequencer
                nesChip.dmc.dmc.tickFrameSequence(real_cpu_clocks);
                if (useMmc5)
                    nesChip.mmc5.tickFrameSequence(real_cpu_clocks);
            }

//            updateInfo();

            // tick APU / expansions
            apu_clock_rest += apu_clock_per_sample;
            int apu_clocks = (int) apu_clock_rest;
            if (apu_clocks > 0) {
                apu_clock_rest -= apu_clocks;
            }

            // render Output
            nesChip.apu.tick(apu_clocks);
            nesChip.apu.render(buf);

            int mul = (int) (16384.0 * Math.pow(10.0, cAPU.getTVolume() / 40.0));
            out[0] = (buf[0] * mul) >> 13;
            out[1] = (buf[1] * mul) >> 13;

            nesChip.dmc.tick(apu_clocks);
            nesChip.dmc.render(buf);
            mul = (int) (16384.0 * Math.pow(10.0, cDMC.getTVolume() / 40.0));
            out[0] += (buf[0] * mul) >> 13;
            out[1] += (buf[1] * mul) >> 13;

            if (useFds) {
                nesChip.fds.tick(apu_clocks);
                nesChip.fds.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cFDS.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 13;
                out[1] += (buf[1] * mul) >> 13;
            }

            if (useN106) {
                nesChip.n106.tick(apu_clocks);
                nesChip.n106.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cN160.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            if (useVrc6) {
                nesChip.vrc6.tick(apu_clocks);
                nesChip.vrc6.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cVRC6.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            if (useMmc5) {
                nesChip.mmc5.tick(apu_clocks);
                nesChip.mmc5.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cMMC5.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            if (useFme7) {
                nesChip.fme7.tick(apu_clocks);
                nesChip.fme7.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cFME7.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 9;
                out[1] += (buf[1] * mul) >> 9;
            }

            if (useVrc7) {
                nesChip.vrc7.tick(apu_clocks);
                nesChip.vrc7.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cVRC7.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            outm = (out[0] + out[1]); // >> 1; // mono mix
            if (outm == last_out) silent_length++;
            else silent_length = 0;
            last_out = outm;

            dcf.fastRender(out);
            lpf.fastRender(out);

            out[0] = (out[0] * master_volume) >> 9;
            out[1] = (out[1] * master_volume) >> 9;

            if (out[0] < -32767)
                out[0] = -32767;
            else if (32767 < out[0])
                out[0] = 32767;

            if (out[1] < -32767)
                out[1] = -32767;
            else if (32767 < out[1])
                out[1] = 32767;

//            if (nch == 2) {
            b[offset + i * 2] = (short) out[0];
            b[offset + i * 2 + 1] = (short) out[1];

            visWB.enq((short) out[0], (short) out[1]);
//            } else { // if not 2 channels, presume mono
//                outm = (out[0] + out[1]) >> 1;
//                for (int i = 0; i<nch; ++i)
//                    b[0] = outm;
//            }
//            b += nch;
        }

        time_in_ms += (int) (1000 * length / rate * vgmSpeed);

        //checkTerminal();
        detectLoop();
        detectSilent();
        if (!playtime_detected) vgmCurLoop = 0;
        else {
            if (totalCounter != 0) vgmCurLoop = (int) (counter / totalCounter);
            else stopped = true;
        }

if (CC++ % 300 == 0) { logger.log(Level.DEBUG, "render: " + out[0] + ", " + out[1]); }
        return length;
    }
int CC = 0;

    public void visWaveBufferCopy(short[][] dest) {
        visWB.copy(dest);
    }

    private mdsound.VisWaveBuffer visWB = new mdsound.VisWaveBuffer();

    private boolean playtime_detected = false;

    public void detectLoop() {
        if (ld.isLooped(time_in_ms, 30000, 5000) && !playtime_detected) {
            playtime_detected = true;
            totalCounter = (long) ld.getLoopEnd() * setting.getOutputDevice().getSampleRate() / 1000L;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = (long) (ld.getLoopEnd() - ld.getLoopStart()) * setting.getOutputDevice().getSampleRate() / 1000L;
        }
    }

    public void detectSilent() {
        if (silent_length > setting.getOutputDevice().getSampleRate() * 3L && !playtime_detected) {
            playtime_detected = true;
            totalCounter = (long) ld.getLoopEnd() * setting.getOutputDevice().getSampleRate() / 1000L;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = 0;
            stopped = true;
        }
    }

    // TODO separate from implementation

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }

    @Override
    public int render(short[] buffer, int offset, int sampleCount) {
//        vstDelta = 0;
        return render_(buffer, sampleCount / 2, offset) * 2;
    }

    @Override
    public void copyWaveBuffer(short[][] dest) {
        visWaveBufferCopy(dest);
    }

    @Override
    public boolean isNotRenderingOnPause() {
        return true;
    }
}