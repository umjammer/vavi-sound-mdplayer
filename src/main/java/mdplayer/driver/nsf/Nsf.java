package mdplayer.driver.nsf;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.Log;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
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


public class Nsf extends BaseDriver {

    public Nsf(Setting setting) {
        this.setting = setting;
        rate = setting.getOutputDevice().getSampleRate();
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int vgmGd3) {
        if (Common.getLE32(buf, 0) != FCC_NSF) {
            //NSFeはとりあえず未サポート
            return null;
        }

        if (buf.length < 0x80) // no header?
            return null;

        version = buf[0x05];
        songs = buf[0x06];
        start = buf[0x07];
        load_address = (short) (buf[0x08] | (buf[0x09] << 8));
        initAddress = (short) (buf[0x0a] | (buf[0x0B] << 8));
        playAddress = (short) (buf[0x0c] | (buf[0x0D] << 8));

        List<Byte> strLst = new ArrayList<>();
        int tagAdr = 0x0e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        title_nsf = new String(mdsound.Common.toByteArray(strLst), Charset.forName("MS932"));
        title = title_nsf;

        strLst.clear();
        tagAdr = 0x2e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        artist_nsf = new String(mdsound.Common.toByteArray(strLst), Charset.forName("MS932"));
        artist = artist_nsf;

        //memcpy(copyright_nsf, image + 0x4e, 32);
        //copyright_nsf[31] = '\0';
        strLst.clear();
        tagAdr = 0x4e;
        for (int i = 0; i < 32; i++) {
            if (buf[tagAdr] == 0) break;
            strLst.add(buf[tagAdr++]);
        }
        copyrightNsf = new String(mdsound.Common.toByteArray(strLst), Charset.forName("MS932"));
        copyright = copyrightNsf;

        ripper = ""; // NSFe only
        text = ""; // NSFe only
        text_len = 0; // NSFe only
        speedNtsc = (short) (buf[0x6e] | (buf[0x6f] << 8));
        System.arraycopy(buf, 112, bankSwitch, 0, 8);
        speedPal = (short) (buf[0x78] | (buf[0x79] << 8));
        palNtsc = buf[0x7a];

        if (speedPal == 0)
            speedPal = 0x4e20;
        if (speedNtsc == 0)
            speedNtsc = 0x411A;

        soundChip = buf[0x7b];

        useVrc6 = (soundChip & 1) != 0;
        useVrc7 = (soundChip & 2) != 0;
        useFds = (soundChip & 4) != 0;
        useMmc5 = (soundChip & 8) != 0;
        useN106 = (soundChip & 16) != 0;
        useFme7 = (soundChip & 32) != 0;

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
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {

        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
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

        gd3 = getGD3Info(vgmBuf, 0);

        nsfInit();

        return true;
    }

    @Override
    public void oneFrameProc() {
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
            Log.forcedWrite(ex);
        }
    }

    public static final int FCC_NSF = 0x4d53454e; // "NESM"

    public byte version;
    public byte songs;
    public byte start;
    public short load_address;
    public short initAddress;
    public short playAddress;
    public String filename;
    // margin 64 chars.
    public String printTitle;
    public String title_nsf;
    public String artist_nsf;
    public String copyrightNsf;
    public String title;
    public String artist;
    public String copyright;
    // NSFe only
    public String ripper;
    // NSFe only
    public String text;
    // NSFe only
    public int text_len;
    public short speedNtsc;
    public byte[] bankSwitch = new byte[8];
    public short speedPal;
    public byte palNtsc;
    public byte soundChip;
    public boolean useVrc7;
    public boolean useVrc6;
    public boolean useFds;
    public boolean useFme7;
    public boolean useMmc5;
    public boolean useN106;
    public byte[] extra = new byte[4];
    public byte[] body;
    public int bodySize;
    public byte[] nsfeImage;
    public byte[] nsfePlst;
    public int nsfePlstSize;
    static final int NSFE_ENTRIES = 256;

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
    private mdsound.np.DCFilter dcf;                        // 最終出力段に掛ける直流フィルタ
    private mdsound.np.Filter lpf;                          // 最終出力に掛けるローパスフィルタ

    public mdsound.MDSound.Chip cAPU = null;
    public mdsound.MDSound.Chip cDMC = null;
    public mdsound.MDSound.Chip cFDS = null;
    public mdsound.MDSound.Chip cMMC5 = null;
    public mdsound.MDSound.Chip cN160 = null;
    public mdsound.MDSound.Chip cVRC6 = null;
    public mdsound.MDSound.Chip cVRC7 = null;
    public mdsound.MDSound.Chip cFME7 = null;

    private LoopDetector.NESDetector ld = null;
    //        private NESDetectorEx ld = null;

    private double rate = 1;// setting.getoutputDevice().SampleRate;
    private double cpu_clock_rest;
    private double apu_clock_rest;
    private int time_in_ms;
    private long silent_length = 0;
    private int last_out = 0;

    private void nsfInit() {
        chipRegister.nes_bank = new NesBank();
        chipRegister.nes_mem = new NesMem();
        chipRegister.nes_cpu = new Km6502(0);
        chipRegister.nes_apu = new NesApu();
        chipRegister.nes_dmc = new NesDmc();
        chipRegister.nes_fds = new NesFds();
        chipRegister.nes_n106 = new NesN106();
        chipRegister.nes_vrc6 = new NesVrc6();
        chipRegister.nes_mmc5 = new NesMmc5();
        chipRegister.nes_fme7 = new NesFme7();
        chipRegister.nes_vrc7 = new NesVrc7();

        chipRegister.nes_apu.apu = new NpNesApu(Common.NsfClock, setting.getOutputDevice().getSampleRate());
        chipRegister.nes_apu.reset();
        chipRegister.nes_dmc.dmc = new NpNesDmc(Common.NsfClock, setting.getOutputDevice().getSampleRate());
        chipRegister.nes_dmc.reset();
        chipRegister.nes_fds.fds = new NpNesFds(Common.NsfClock, setting.getOutputDevice().getSampleRate());
        chipRegister.nes_fds.reset();
        chipRegister.nes_n106.setClock(Common.NsfClock);
        chipRegister.nes_n106.setRate(setting.getOutputDevice().getSampleRate());
        chipRegister.nes_n106.reset();
        chipRegister.nes_vrc6.setClock(Common.NsfClock);
        chipRegister.nes_vrc6.setRate(setting.getOutputDevice().getSampleRate());
        chipRegister.nes_vrc6.reset();
        chipRegister.nes_mmc5.setClock(Common.NsfClock);
        chipRegister.nes_mmc5.setRate(setting.getOutputDevice().getSampleRate());
        chipRegister.nes_mmc5.reset();
        chipRegister.nes_mmc5.setCPU(chipRegister.nes_cpu);
        chipRegister.nes_fme7.setClock(Common.NsfClock);
        chipRegister.nes_fme7.setRate(setting.getOutputDevice().getSampleRate());
        chipRegister.nes_fme7.reset();
        chipRegister.nes_vrc7.setClock(Common.NsfClock);
        chipRegister.nes_vrc7.setRate(setting.getOutputDevice().getSampleRate());
        chipRegister.nes_vrc7.reset();

        chipRegister.nes_dmc.dmc.nes_apu = chipRegister.nes_apu.apu;
        chipRegister.nes_dmc.dmc.setAPU(chipRegister.nes_apu.apu);

        stack = new Device.Bus();
        layer = new Device.Layer();
        apuBus = new Device.Bus();

        dcf = new DCFilter();
        lpf = new Filter();
        lpf.setRate(setting.getOutputDevice().getSampleRate());
        lpf.reset();
        dcf.setRate(setting.getOutputDevice().getSampleRate());
        dcf.reset();
        dcf.setParam(270, 256 - setting.getNsf().getHPF());//HPF:256-(Range0-256(Def:92))
        lpf.SetParam(4700.0, setting.getNsf().getLPF()); //LPF:(Range 0-400(Def:112))
        //System.err.println("dcf:%d", dcf.GetFactor());
        //System.err.println("lpf:%d", lpf.GetFactor());

        int i, bmax = 0;

        for (i = 0; i < 8; i++)
            if (bmax < bankSwitch[i])
                bmax = bankSwitch[i];

        chipRegister.nes_mem.setImage(body, load_address, bodySize);

        if (bmax != 0) {
            chipRegister.nes_bank.setImage(body, load_address, bodySize);
            for (i = 0; i < 8; i++)
                chipRegister.nes_bank.setBankDefault((byte) (i + 8), bankSwitch[i]);
        }

        stack.detachAll();
        layer.detachAll();
        apuBus.detachAll();

        ld = new LoopDetector.NESDetector(0);
        //            ld = new NESDetectorEx();
        ld.reset();
        stack.attach(ld);

        apuBus.attach(chipRegister.nes_apu);
        apuBus.attach(chipRegister.nes_dmc);

        chipRegister.nes_apu.setOption(NpNesApu.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getNESUnmuteOnReset() ? 1 : 0);
        chipRegister.nes_apu.setOption(NpNesApu.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getNESNonLinearMixer() ? 1 : 0);
        chipRegister.nes_apu.setOption(NpNesApu.OPT.PHASE_REFRESH.ordinal(), setting.getNsf().getNESPhaseRefresh() ? 1 : 0);
        chipRegister.nes_apu.setOption(NpNesApu.OPT.DUTY_SWAP.ordinal(), setting.getNsf().getNESDutySwap() ? 1 : 0);

        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.ENABLE_4011.ordinal(), setting.getNsf().getDMCEnable4011() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.ENABLE_PNOISE.ordinal(), setting.getNsf().getDMCEnablePnoise() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.UNMUTE_ON_RESET.ordinal(), setting.getNsf().getDMCUnmuteOnReset() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.DPCM_ANTI_CLICK.ordinal(), setting.getNsf().getDMCDPCMAntiClick() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.NONLINEAR_MIXER.ordinal(), setting.getNsf().getDMCNonLinearMixer() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.RANDOMIZE_NOISE.ordinal(), setting.getNsf().getDMCRandomizeNoise() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.TRI_MUTE.ordinal(), setting.getNsf().getDMCTRImute() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.RANDOMIZE_TRI.ordinal(), setting.getNsf().getDMCRandomizeTRI() ? 1 : 0);
        chipRegister.nes_dmc.setOption(NpNesDmc.OPT.DPCM_REVERSE.ordinal(), setting.getNsf().getDMCDPCMReverse() ? 1 : 0);

        if (useFds) {
            boolean write_enable = !setting.getNsf().getFDSWriteDisable8000();
            chipRegister.nes_fds.setOption(0, setting.getNsf().getFDSLpf());
            chipRegister.nes_fds.setOption(1, setting.getNsf().getFDS4085Reset() ? 1 : 0);
            chipRegister.nes_mem.setFDSMode(write_enable);
            chipRegister.nes_bank.setFDSMode(write_enable);
            chipRegister.nes_bank.setBankDefault((byte) 6, bankSwitch[6]);
            chipRegister.nes_bank.setBankDefault((byte) 7, bankSwitch[7]);
            apuBus.attach(chipRegister.nes_fds);
        } else {
            chipRegister.nes_mem.setFDSMode(false);
            chipRegister.nes_bank.setFDSMode(false);
        }
        if (useN106) {
            chipRegister.nes_n106.setOption(0, setting.getNsf().getN160Serial() ? 1 : 0);
            apuBus.attach(chipRegister.nes_n106);
        }
        if (useVrc6) {
            apuBus.attach(chipRegister.nes_vrc6);
        }
        if (useMmc5) {
            chipRegister.nes_mmc5.setOption(0, setting.getNsf().getMMC5NonLinearMixer() ? 1 : 0);
            chipRegister.nes_mmc5.setOption(1, setting.getNsf().getMMC5PhaseRefresh() ? 1 : 0);
            apuBus.attach(chipRegister.nes_mmc5);
        }
        if (useFme7) {
            apuBus.attach(chipRegister.nes_fme7);
        }
        if (useVrc7) {
            apuBus.attach(chipRegister.nes_vrc7);
        }

        if (bmax > 0) layer.attach(chipRegister.nes_bank);
        layer.attach(chipRegister.nes_mem);

        stack.attach(apuBus);
        stack.attach(layer);

        chipRegister.nes_cpu.setMemory(stack);
        chipRegister.nes_dmc.setMemory(stack);

        chipRegister.nes_apu.apu.squareTable[0] = 0;
        for (i = 1; i < 32; i++)
            chipRegister.nes_apu.apu.squareTable[i] = (int) ((8192.0 * 95.88) / (8128.0 / i + 100));

        for (int c = 0; c < 2; ++c)
            for (int t = 0; t < 2; ++t)
                chipRegister.nes_apu.apu.sm[c][t] = 128;

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

        Region region = GetRegion(palNtsc);
        double speed;
        speed = 1000000.0 / ((region == Region.NTSC) ? speedNtsc : speedPal);

        layer.reset();
        chipRegister.nes_cpu.reset();

        chipRegister.nes_cpu.start(initAddress, playAddress, speed, song, (region == Region.PAL) ? 1 : 0, 0);
    }

    private Region GetRegion(byte flags) {
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
            // else pref == 0 or invalid, use auto setting based on flags bit
            return ((flags & 1) != 0) ? Region.PAL : Region.NTSC;
        }

        return Region.NTSC; // fallback for invalid flags
    }

    public int Render(short[] b, int length) {
        return Render(b, length, 0);
    }

    public int Render(short[] b, int length, int offset) {
        if (model == EnmModel.RealModel) return length;
        if (chipRegister == null) return length;

        if (vgmFrameCounter < 0) {
            vgmFrameCounter += length;
            return length;
        }

        int[] buf = new int[2];
        int[] _out = new int[2];
        int outm;
        int i;
        int master_volume;

        master_volume = 0x80;


        double apu_clock_per_sample = 0;
        if (chipRegister.nes_cpu != null) {
            apu_clock_per_sample = chipRegister.nes_cpu.NES_BASECYCLES / rate;
        }
        double cpu_clock_per_sample = apu_clock_per_sample * vgmSpeed;


        for (i = 0; i < length; i++) {
            //total_render++;
            vgmSpeedCounter += vgmSpeed;
            counter = (int) vgmSpeedCounter;
            vgmFrameCounter++;

            // tick CPU
            cpu_clock_rest += cpu_clock_per_sample;
            int cpu_clocks = (int) (cpu_clock_rest);
            if (cpu_clocks > 0) {
                int real_cpu_clocks = chipRegister.nes_cpu.exec(cpu_clocks);
                cpu_clock_rest -= real_cpu_clocks;

                // tick APU frame sequencer
                chipRegister.nes_dmc.dmc.tickFrameSequence(real_cpu_clocks);
                if (useMmc5)
                    chipRegister.nes_mmc5.tickFrameSequence(real_cpu_clocks);
            }

            //updateInfo();

            // tick APU / expansions
            apu_clock_rest += apu_clock_per_sample;
            int apu_clocks = (int) (apu_clock_rest);
            if (apu_clocks > 0) {
                apu_clock_rest -= apu_clocks;
            }

            // render Output
            chipRegister.nes_apu.tick(apu_clocks);
            chipRegister.nes_apu.render(buf);

            int mul = (int) (16384.0 * Math.pow(10.0, cAPU.getTVolume() / 40.0));
            _out[0] = (buf[0] * mul) >> 13;
            _out[1] = (buf[1] * mul) >> 13;

            chipRegister.nes_dmc.tick(apu_clocks);
            chipRegister.nes_dmc.render(buf);
            mul = (int) (16384.0 * Math.pow(10.0, cDMC.getTVolume() / 40.0));
            _out[0] += (buf[0] * mul) >> 13;
            _out[1] += (buf[1] * mul) >> 13;

            if (useFds) {
                chipRegister.nes_fds.tick(apu_clocks);
                chipRegister.nes_fds.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cFDS.getTVolume() / 40.0));
                _out[0] += (buf[0] * mul) >> 13;
                _out[1] += (buf[1] * mul) >> 13;
            }

            if (useN106) {
                chipRegister.nes_n106.tick(apu_clocks);
                chipRegister.nes_n106.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cN160.getTVolume() / 40.0));
                _out[0] += (buf[0] * mul) >> 10;
                _out[1] += (buf[1] * mul) >> 10;
            }

            if (useVrc6) {
                chipRegister.nes_vrc6.tick(apu_clocks);
                chipRegister.nes_vrc6.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cVRC6.getTVolume() / 40.0));
                _out[0] += (buf[0] * mul) >> 10;
                _out[1] += (buf[1] * mul) >> 10;
            }

            if (useMmc5) {
                chipRegister.nes_mmc5.tick(apu_clocks);
                chipRegister.nes_mmc5.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cMMC5.getTVolume() / 40.0));
                _out[0] += (buf[0] * mul) >> 10;
                _out[1] += (buf[1] * mul) >> 10;
            }

            if (useFme7) {
                chipRegister.nes_fme7.tick(apu_clocks);
                chipRegister.nes_fme7.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cFME7.getTVolume() / 40.0));
                _out[0] += (buf[0] * mul) >> 9;
                _out[1] += (buf[1] * mul) >> 9;
            }

            if (useVrc7) {
                chipRegister.nes_vrc7.tick(apu_clocks);
                chipRegister.nes_vrc7.render(buf);
                mul = (int) (16384.0 * Math.pow(10.0, cVRC7.getTVolume() / 40.0));
                _out[0] += (buf[0] * mul) >> 10;
                _out[1] += (buf[1] * mul) >> 10;
            }

            outm = (_out[0] + _out[1]);// >> 1; // mono mix
            if (outm == last_out) silent_length++;
            else silent_length = 0;
            last_out = outm;

            dcf.fastRender(_out);
            lpf.fastRender(_out);

            _out[0] = (_out[0] * master_volume) >> 9;
            _out[1] = (_out[1] * master_volume) >> 9;

            if (_out[0] < -32767)
                _out[0] = -32767;
            else if (32767 < _out[0])
                _out[0] = 32767;

            if (_out[1] < -32767)
                _out[1] = -32767;
            else if (32767 < _out[1])
                _out[1] = 32767;

            //if (nch == 2) {
            b[offset + i * 2] = (short) _out[0];
            b[offset + i * 2 + 1] = (short) _out[1];

            visWB.enq((short) _out[0], (short) _out[1]);
            //} else { // if not 2 channels, presume mono
            //    outm = (_out[0] + _out[1]) >> 1;
            //    for (int i = 0; i<nch; ++i)
            //        b[0] = outm;
            //}
            //b += nch;
        }

        time_in_ms += (int) (1000 * length / rate * vgmSpeed);

        //checkTerminal();
        DetectLoop();
        DetectSilent();
        if (!playtime_detected) vgmCurLoop = 0;
        else {
            if (totalCounter != 0) vgmCurLoop = (int) (counter / totalCounter);
            else stopped = true;
        }

        return length;
    }

    public void visWaveBufferCopy(short[][] dest) {
        visWB.copy(dest);
    }

    mdsound.VisWaveBuffer visWB = new mdsound.VisWaveBuffer();

    public boolean playtime_detected = false;

    public void DetectLoop() {
        if (ld.isLooped(time_in_ms, 30000, 5000) && !playtime_detected) {
            playtime_detected = true;
            totalCounter = (long) ld.getLoopEnd() * setting.getOutputDevice().getSampleRate() / 1000L;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = (long) (ld.getLoopEnd() - ld.getLoopStart()) * setting.getOutputDevice().getSampleRate() / 1000L;
        }
    }

    public void DetectSilent() {
        if (silent_length > setting.getOutputDevice().getSampleRate() * 3L && !playtime_detected) {
            playtime_detected = true;
            totalCounter = (long) ld.getLoopEnd() * setting.getOutputDevice().getSampleRate() / 1000L;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = 0;
            stopped = true;
        }
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }
}
