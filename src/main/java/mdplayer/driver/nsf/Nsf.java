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
import mdsound.MDSound;
import mdsound.np.DCFilter;
import mdsound.np.Device;
import mdsound.np.Filter;
import mdsound.np.LoopDetector;
import mdsound.np.NpNesApu;
import mdsound.np.NpNesDmc;
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
import static mdplayer.Common.charset;


public class Nsf extends BaseDriver implements NsfDriver {

    private static final Logger logger = getLogger(Nsf.class.getName());

    public Nsf() {
        sampleRate = setting.getOutputDevice().getSampleRate();
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (ByteUtil.readLeInt(buf, 0) != FCC_NSF) {
            // NSFe is not supported for now
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
        title_nsf = new String(ByteUtil.toByteArray(strLst), charset);
        title = title_nsf;

        strLst.clear();
        tagAdr = 0x2e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        artist_nsf = new String(ByteUtil.toByteArray(strLst), charset);
        artist = artist_nsf;

        //memcpy(copyright_nsf, image + 0x4e, 32);
        //copyright_nsf[31] = '\0';
        strLst.clear();
        tagAdr = 0x4e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        copyrightNsf = new String(ByteUtil.toByteArray(strLst), charset);
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

        this.chip = plugin.audio.chipRegister.chip(NesChip.class);

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

        _init();

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
    public int[] nsfePlst;
    public int nsfePlstSize;
    private static final int NSFE_ENTRIES = 256;

    @Override public void setSong(int songNo) {
        song = songNo;
    }

    @Override public boolean useFds() {
        return useFds;
    }

    @Override public boolean useFme7() {
        return useFme7;
    }

    @Override public boolean useMmc5() {
        return useMmc5;
    }

    @Override public boolean useN106() {
        return useN106;
    }

    @Override public boolean useVrc6() {
        return useVrc6;
    }

    @Override public boolean useVrc7() {
        return useVrc7;
    }

    @Override public void setApu(MDSound.Chip chip) {
        cAPU = chip;
    }

    @Override public void setDmc(MDSound.Chip chip) {
        cDMC = chip;
    }

    @Override public void setFds(MDSound.Chip chip) {
        cFDS = chip;
    }

    @Override public void setMmc5(MDSound.Chip chip) {
        cMMC5 = chip;
    }

    @Override public void setN160(MDSound.Chip chip) {
        cN160 = chip;
    }

    @Override public void setVrc6(MDSound.Chip chip) {
        cVRC6 = chip;
    }

    @Override public void setVrc7(MDSound.Chip chip) {
        cVRC7 = chip;
    }

    @Override public void setFme7(MDSound.Chip chip) {
        cFME7 = chip;
    }

    public static class NsfeEntry {

        public int[] tlbl;
        public int time;
        public int fade;
    }

    public NsfeEntry[] nsfeEntry = new NsfeEntry[NSFE_ENTRIES];

    /**
     * Currently selected track number
     */
    public int song;

    private Device.Bus apuBus;

    private Device.Bus stack;
    private Device.Layer layer;
    /** DC filter applied to the final output stage */
    private mdsound.np.DCFilter dcf;
    /** Low-pass filter applied to the final output */
    private mdsound.np.Filter lpf;

    private mdsound.MDSound.Chip cAPU = null;
    private mdsound.MDSound.Chip cDMC = null;
    private mdsound.MDSound.Chip cFDS = null;
    private mdsound.MDSound.Chip cMMC5 = null;
    private mdsound.MDSound.Chip cN160 = null;
    private mdsound.MDSound.Chip cVRC6 = null;
    private mdsound.MDSound.Chip cVRC7 = null;
    private mdsound.MDSound.Chip cFME7 = null;

    private LoopDetector.NESDetector ld = null;
//    private NESDetectorEx ld = null;

    private NesChip chip;

    private final int sampleRate;
    private double cpu_clock_rest;
    private double apu_clock_rest;
    private int time_in_ms;
    private long silent_length = 0;
    private int last_out = 0;

    private void _init() {
        chip.bank = new NesBank();
        chip.mem = new NesMem();
        chip.cpu = new Km6502(true);
        chip.apu = new NesApu();
        chip.dmc = new NesDmc();
        chip.fds = new NesFds();
        chip.n106 = new NesN106();
        chip.vrc6 = new NesVrc6();
        chip.mmc5 = new NesMmc5();
        chip.fme7 = new NesFme7();
        chip.vrc7 = new NesVrc7();

        chip.apu.apu.init(Common.NsfClock, this.sampleRate);
        chip.apu.reset();
        chip.dmc.dmc.init(Common.NsfClock, this.sampleRate);
        chip.dmc.reset();
        chip.fds.fds.init(Common.NsfClock, this.sampleRate);
        chip.fds.reset();
        chip.n106.setClock(Common.NsfClock);
        chip.n106.setRate(this.sampleRate);
        chip.n106.reset();
        chip.vrc6.setClock(Common.NsfClock);
        chip.vrc6.setRate(this.sampleRate);
        chip.vrc6.reset();
        chip.mmc5.setClock(Common.NsfClock);
        chip.mmc5.setRate(this.sampleRate);
        chip.mmc5.reset();
        chip.mmc5.setCPU(chip.cpu);
        chip.fme7.setClock(Common.NsfClock);
        chip.fme7.setRate(this.sampleRate);
        chip.fme7.reset();
        chip.vrc7.setClock(Common.NsfClock);
        chip.vrc7.setRate(this.sampleRate);
        chip.vrc7.reset();

        chip.dmc.dmc.nes_apu = chip.apu.apu;
        chip.dmc.dmc.setAPU(chip.apu.apu);

        stack = new Device.Bus();
        layer = new Device.Layer();
        apuBus = new Device.Bus();

        dcf = new DCFilter();
        lpf = new Filter();
        lpf.setRate(this.sampleRate);
        lpf.reset();
        dcf.setRate(this.sampleRate);
        dcf.reset();
        dcf.setParam(270, 256 - setting.getNsf().getHPF()); // HPF:256-(Range0-256(Def:92))
        lpf.setParam(4700.0, setting.getNsf().getLPF()); // LPF:(Range 0-400(Def:112))
//logger.log(Level.TRACE, "dcf:%d".formatted(dcf.getFactor()));
//logger.log(Level.TRACE, "lpf:%d".formatted(lpf.getFactor()));

        int bmax = 0;

        for (int i = 0; i < 8; i++)
            if (bmax < (bankSwitch[i] & 0xff))
                bmax = bankSwitch[i] & 0xff;

        chip.mem.setImage(body, load_address & 0xffff, bodySize);

        if (bmax != 0) {
            chip.bank.setImage(body, load_address & 0xffff, bodySize);
            for (int i = 0; i < 8; i++)
                chip.bank.setBankDefault(i + 8, bankSwitch[i] & 0xff);
        }

        stack.detachAll();
        layer.detachAll();
        apuBus.detachAll();

        ld = new LoopDetector.NESDetector(0);
        ld.reset();
        stack.attach(ld);

        apuBus.attach(chip.apu);
        apuBus.attach(chip.dmc);

        chip.apu.setOption(NpNesApu.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getNESUnmuteOnReset() ? 1 : 0);
        chip.apu.setOption(NpNesApu.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getNESNonLinearMixer() ? 1 : 0);
        chip.apu.setOption(NpNesApu.OPT.PHASE_REFRESH.ordinal(), setting.getNsf().getNESPhaseRefresh() ? 1 : 0);
        chip.apu.setOption(NpNesApu.OPT.DUTY_SWAP.ordinal(), setting.getNsf().getNESDutySwap() ? 1 : 0);

        chip.dmc.setOption(NpNesDmc.OPT.ENABLE_4011.ordinal(), setting.getNsf().getDMCEnable4011() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.ENABLE_PNOISE.ordinal(), setting.getNsf().getDMCEnablePnoise() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getDMCUnmuteOnReset() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.DPCM_ANTI_CLICK.ordinal(), setting.getNsf().getDMCDPCMAntiClick() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getDMCNonLinearMixer() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.RANDOMIZE_NOISE.ordinal(), setting.getNsf().getDMCRandomizeNoise() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.TRI_MUTE.ordinal(), setting.getNsf().getDMCTRImute() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.RANDOMIZE_TRI.ordinal(), setting.getNsf().getDMCRandomizeTRI() ? 1 : 0);
        chip.dmc.setOption(NpNesDmc.OPT.DPCM_REVERSE.ordinal(), setting.getNsf().getDMCDPCMReverse() ? 1 : 0);

        if (useFds) {
            boolean write_enable = !setting.getNsf().getFDSWriteDisable8000();
            chip.fds.setOption(0, setting.getNsf().getFDSLpf());
            chip.fds.setOption(1, setting.getNsf().getFDS4085Reset() ? 1 : 0);
            chip.mem.setFDSMode(write_enable);
            chip.bank.setFDSMode(write_enable);
            chip.bank.setBankDefault(6, bankSwitch[6] & 0xff);
            chip.bank.setBankDefault(7, bankSwitch[7] & 0xff);
            apuBus.attach(chip.fds);
        } else {
            chip.mem.setFDSMode(false);
            chip.bank.setFDSMode(false);
        }
        if (useN106) {
            chip.n106.setOption(0, setting.getNsf().getN160Serial() ? 1 : 0);
            apuBus.attach(chip.n106);
        }
        if (useVrc6) {
            apuBus.attach(chip.vrc6);
        }
        if (useMmc5) {
            chip.mmc5.setOption(0, setting.getNsf().getMMC5NonLinearMixer() ? 1 : 0);
            chip.mmc5.setOption(1, setting.getNsf().getMMC5PhaseRefresh() ? 1 : 0);
            apuBus.attach(chip.mmc5);
        }
        if (useFme7) {
            apuBus.attach(chip.fme7);
        }
        if (useVrc7) {
            apuBus.attach(chip.vrc7);
        }

        if (bmax > 0) layer.attach(chip.bank);
        layer.attach(chip.mem);

        stack.attach(apuBus);
        stack.attach(layer);

        chip.cpu.setMemory(stack);
        chip.dmc.setMemory(stack);

        chip.apu.apu.squareTable[0] = 0;
        for (int i = 1; i < 32; i++)
            chip.apu.apu.squareTable[i] = (int) ((8192.0 * 95.88) / (8128.0 / i + 100));

        for (int c = 0; c < 2; ++c)
            for (int t = 0; t < 2; ++t)
                chip.apu.apu.sm[c][t] = 128;

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
        chip.cpu.reset();

        chip.cpu.start(initAddress, playAddress, speed, song, (region == Region.PAL) ? 1 : 0, 0);
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

int CC;
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

        int master_volume = 0x80;

        double apu_clock_per_sample = chip.cpu.NES_BASECYCLES / sampleRate;
        double cpu_clock_per_sample = apu_clock_per_sample * vgmSpeed;

        for (int i = 0; i < length; i++) {
            //total_render++;
            vgmSpeedCounter += vgmSpeed;
            counter = (int) vgmSpeedCounter;
            vgmFrameCounter++;

            // tick CPU
            cpu_clock_rest += cpu_clock_per_sample;
            int cpu_clocks = (int) cpu_clock_rest;
            if (cpu_clocks > 0) {
                int real_cpu_clocks = chip.cpu.exec(cpu_clocks);
                cpu_clock_rest -= real_cpu_clocks;

                // tick APU frame sequencer
                chip.dmc.dmc.tickFrameSequence(real_cpu_clocks);
                if (useMmc5)
                    chip.mmc5.tickFrameSequence(real_cpu_clocks);
            }

//            updateInfo();

            // tick APU / expansions
            apu_clock_rest += apu_clock_per_sample;
            int apu_clocks = (int) apu_clock_rest;
            if (apu_clocks > 0) {
                apu_clock_rest -= apu_clocks;
            }

            // render Output
            chip.apu.tick(apu_clocks);
            chip.apu.render(buf);

            int mul = (int) (16384.0 * Math.pow(10.0, cAPU.getTVolume() / 40.0));
            out[0] = (buf[0] * mul) >> 13;
            out[1] = (buf[1] * mul) >> 13;

            chip.dmc.tick(apu_clocks);
            chip.dmc.render(buf);
            mul = (int) (16384.0 * Math.pow(10.0, cDMC.getTVolume() / 40.0));
            out[0] += (buf[0] * mul) >> 13;
            out[1] += (buf[1] * mul) >> 13;

            if (useFds) {
                chip.fds.tick(apu_clocks);
                chip.fds.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cFDS.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 13;
                out[1] += (buf[1] * mul) >> 13;
            }

            if (useN106) {
                chip.n106.tick(apu_clocks);
                chip.n106.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cN160.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            if (useVrc6) {
                chip.vrc6.tick(apu_clocks);
                chip.vrc6.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cVRC6.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            if (useMmc5) {
                chip.mmc5.tick(apu_clocks);
                chip.mmc5.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cMMC5.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            if (useFme7) {
                chip.fme7.tick(apu_clocks);
                chip.fme7.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cFME7.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 9;
                out[1] += (buf[1] * mul) >> 9;
            }

            if (useVrc7) {
                chip.vrc7.tick(apu_clocks);
                chip.vrc7.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cVRC7.getTVolume() / 40.0));
                out[0] += (buf[0] * mul) >> 10;
                out[1] += (buf[1] * mul) >> 10;
            }

            int outM = (out[0] + out[1]); // >> 1; // mono mix
            if (outM == last_out) silent_length++;
            else silent_length = 0;
            last_out = outM;

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
//                outM = (out[0] + out[1]) >> 1;
//                for (int i = 0; i<nch; ++i)
//                    b[0] = outM;
//            }
//            b += nch;
        }

        time_in_ms += (int) (1000 * length / sampleRate * vgmSpeed);

        //checkTerminal();
        detectLoop();
        detectSilent();
        if (!playtime_detected) vgmCurLoop = 0;
        else {
            if (totalCounter != 0) vgmCurLoop = (int) (counter / totalCounter);
            else stopped = true;
        }

if (CC++ % INTERVAL == 0) { logger.log(Level.DEBUG, "NSF: %d, %d, pc: %04x".formatted(out[0], out[1], chip.cpu.p)); }
        return length;
    }
static final int INTERVAL = 1024;

    public void visWaveBufferCopy(short[][] dest) {
        visWB.copy(dest);
    }

    private final mdsound.VisWaveBuffer visWB = new mdsound.VisWaveBuffer();

    private boolean playtime_detected = false;

    public void detectLoop() {
        if (ld.isLooped(time_in_ms, 30000, 5000) && !playtime_detected) {
            playtime_detected = true;
            totalCounter = (long) ld.getLoopEnd() * this.sampleRate / 1000;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = (long) (ld.getLoopEnd() - ld.getLoopStart()) * this.sampleRate / 1000;
        }
    }

    public void detectSilent() {
        if (silent_length > sampleRate * 3L && !playtime_detected) {
            playtime_detected = true;
            totalCounter = (long) ld.getLoopEnd() * this.sampleRate / 1000;
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