/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2017 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000-2001 Simon White
 *
 * This program instanceof free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program instanceof distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.FileStream;
import mdplayer.driver.sid.libsidplayfp.c64.C64;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;


public class Player {

    private enum state_t {
        STOPPED,
        PLAYING,
        STOPPING
    }

    // Commodore 64 emulator
    private C64 m_c64 = new C64();

    // Mixer
    private Mixer m_mixer = new Mixer();

    // Emulator info
    private SidTune m_tune;

    // User Configuration Settings
    private SidInfoImpl m_info = new SidInfoImpl();

    // User Configuration Settings
    private SidConfig m_cfg = null;

    // Error message
    private String m_errorString;

    private volatile state_t m_isPlaying;

    // PAL/NTSC switch value
    private byte videoSwitch;


    /**
     //Get the C64 model for the current loaded tune.
     *
     //@param defaultModel the default model
     //@param forced true if the default model shold be forced : spite of tune model
     */
    //private C64.C64.model_t c64model(SidPlayFp.SidConfig.c64_model_t defaultModel, Boolean forced) { return C64.C64.model_t.NTSC_M; }

    /**
     //Initialize the emulation.
     *
     //@throw configError
     */
    //private void initialise() { }

    /**
     //Release the SID builders.
     */
    //private void sidRelease() { }

    /**
     //Create the SID emulation(s).
     *
     //@throw configError
     */
    //private void sidCreate(SidPlayFp.SidBuilder[] builder, SidPlayFp.SidConfig.sid_model_t defaultModel,
    //Boolean forced,List<Integer> extraSidAddresses)
    //{ }

    /**
     * //Set the SID emulation parameters.
     * <p>
     * //@param cpuFreq the CPU clock frequency
     * //@param frequency the output sampling frequency
     * //@param sampling the sampling method to use
     * //@param fastSampling true to enable fast low quality resampling (only for reSID)
     */
    //private void sidParams(double cpuFreq, int frequency,
    //SidPlayFp.SidConfig.sampling_method_t sampling, Boolean fastSampling)
    //{ }

    //# ifdef PC64_TESTSUITE
    //private void load(String file) { }
    //#endif

    //private void run(int events) { }

    //public Player() { }
    protected void finalize() {
    }

    public SidConfig config() {
        return m_cfg;
    }

    public SidInfo info() {
        return m_info;
    }

    //public Boolean config( SidPlayFp.SidConfig cfg, Boolean force = false) { return false; }

    //public Boolean fastForward(int percent) { return false; }

    //public Boolean load(SidPlayFp.SidTune tune) { return false; }

    //public int play(short[] buffer, int samples) { return 0; }

    public Boolean isPlaying() {
        return m_isPlaying != state_t.STOPPED;
    }

    //public void stop() { }

    public int time() {
        return m_c64.getTime();
    }

    public void debug(Boolean enable, FileStream out_) {
        m_c64.debug(enable, out_);
    }

    //public void mute(int sidNum, int voice, Boolean enable) { }

    public String error() {
        return m_errorString;
    }

    //public void setRoms(byte[] kernal, byte[] basic, byte[] character) { }

    public short getCia1TimerA() {
        return m_c64.getCia1TimerA();
    }

    // Speed Strings
    String TXT_PAL_VBI = "50 Hz VBI (PAL)";
    String TXT_PAL_VBI_FIXED = "60 Hz VBI (PAL FIXED)";
    String TXT_PAL_CIA = "CIA (PAL)";
    //String TXT_PAL_UNKNOWN = "UNKNOWN (PAL)";
    String TXT_NTSC_VBI = "60 Hz VBI (NTSC)";
    String TXT_NTSC_VBI_FIXED = "50 Hz VBI (NTSC FIXED)";
    String TXT_NTSC_CIA = "CIA (NTSC)";
    //String TXT_NTSC_UNKNOWN = "UNKNOWN (NTSC)";

    // Error Strings
    String ERR_NA = "NA";
    String ERR_UNSUPPORTED_FREQ = "SIDPLAYER ERROR: Unsupported sampling frequency.";
    String ERR_UNSUPPORTED_SID_ADDR = "SIDPLAYER ERROR: Unsupported SID address.";
    String ERR_UNSUPPORTED_SIZE = "SIDPLAYER ERROR: Size of Music data exceeds C64 memory.";
    String ERR_INVALID_PERCENTAGE = "SIDPLAYER ERROR: Percentage value  of range.";

    /**
     * Configuration error exception.
     */
    public class configError extends RuntimeException {
        private String m_msg;

        public configError(String msg) {
            m_msg = msg;
        }

        public String message() {
            return m_msg;
        }
    }

    private mdplayer.Setting setting;

//        public Player(MDPlayer.Setting setting) {
//            this.setting = setting;
//            m_cfg = new SidPlayFp.SidConfig(setting);
//        }

    public Player(mdplayer.Setting setting) {
        this.setting = setting;
        m_cfg = new SidConfig(setting);

        // Set default settings for system
        m_tune = null;
        m_errorString = ERR_NA;
        m_isPlaying = state_t.STOPPED;
//# ifdef PC64_TESTSUITE
//            m_c64.setTestEnv(this);
//#endif

        m_c64.setRoms(null, null, null);
        config(m_cfg, false);

        // Get component credits
        m_info.m_credits.add(m_c64.cpuCredits());
        m_info.m_credits.add(m_c64.ciaCredits());
        m_info.m_credits.add(m_c64.vicCredits());
    }

    //template<class T>
    public void checkRomkernalCheck(byte[] rom, String desc) {

        if (rom != null) {
            kernalCheck romCheck = new kernalCheck(rom);
            desc = romCheck.info();
        } else
            desc = "";
    }

    public void checkRombasicCheck(byte[] rom, String desc) {
        if (rom != null) {
            basicCheck romCheck = new basicCheck(rom);
            desc = romCheck.info();
        } else
            desc = "";
    }

    public void checkRomchargenCheck(byte[] rom, String desc) {
        if (rom != null) {
            chargenCheck romCheck = new chargenCheck(rom);
            desc = romCheck.info();
        } else
            desc = "";
    }

    public void setRoms(byte[] kernal, byte[] basic, byte[] character) {
        checkRomkernalCheck(kernal, m_info.m_kernalDesc);
        checkRombasicCheck(basic, m_info.m_basicDesc);
        checkRomchargenCheck(character, m_info.m_chargenDesc);

        m_c64.setRoms(kernal, basic, character);
    }

    public Boolean fastForward(int percent) {
        if (!m_mixer.setFastForward((int) (percent / 100))) {
            m_errorString = ERR_INVALID_PERCENTAGE;
            return false;
        }

        return true;
    }

    private void initialise() {
        m_isPlaying = state_t.STOPPED;

        m_c64.reset();

        SidTuneInfo tuneInfo = m_tune.getInfo();

        int size = (int) (tuneInfo.loadAddr()) + tuneInfo.c64dataLen() - 1;
        if (size > 0xffff) {
            throw new configError(ERR_UNSUPPORTED_SIZE);
        }

        psiddrv driver = new psiddrv(m_tune.getInfo());
        if (!driver.drvReloc()) {
            throw new configError(driver.errorString());
        }

        m_info.m_driverAddr = driver.driverAddr();
        m_info.m_driverLength = driver.driverLength();

        sidmemory sm = m_c64.getMemInterface();
        driver.install(sm, videoSwitch);

        sm = m_c64.getMemInterface();
        if (!m_tune.placeSidTuneInC64mem(sm)) {
            throw new configError(m_tune.statusString());
        }

        m_c64.resetCpu();
        //System.err.println("{0:x}", sm.readMemByte(0x17e3));
    }

    public Boolean load(SidTune tune) {
        m_tune = tune;

        if (tune != null) {
            // Must re-configure on fly for stereo support!
            if (!config(m_cfg, true)) {
                // Failed configuration with new tune, reject it
                m_tune = null;
                return false;
            }
        }
        return true;
    }

    public void mute(int sidNum, int voice, Boolean enable) {
        sidemu s = m_mixer.getSid(sidNum);
        if (s != null)
            s.voice(voice, enable);
    }

    /**
     * //@throws MOS6510::haltInstruction
     */
    private void run(int events) {
        for (int i = 0; m_isPlaying != state_t.STOPPED && i < events; i++) {
            //System.err.println("run counter i : {0}",i);
            m_c64.clock();
        }
    }

    public int play(short[] buffer, int count) {
        // Make sure a tune instanceof loaded
        if (m_tune == null)
            return 0;

        // Start the player loop
        if (m_isPlaying == state_t.STOPPED)
            m_isPlaying = state_t.PLAYING;

        if (m_isPlaying == state_t.PLAYING) {
            m_mixer.begin(buffer, count);
            //MDPlayer.Log.Write(String.format("{0}", count));
            try {
                if (m_mixer.getSid(0) != null) {
                    if (count != 0 && buffer != null) {
                        //.Clock chips and mix into output buffer
                        while (m_isPlaying != state_t.STOPPED && m_mixer.notFinished()) {
                            run((int) sidemu.output.OUTPUTBUFFERSIZE);

                            m_mixer.clockChips();
                            m_mixer.doMix();
                        }
                        count = m_mixer.samplesGenerated();
                    } else {
                        //.Clock chips and discard buffers
                        int size = (int) (m_c64.getMainCpuSpeed() / m_cfg.frequency);
                        while (m_isPlaying != state_t.STOPPED && (--size) != 0) {
                            run((int) sidemu.output.OUTPUTBUFFERSIZE);

                            m_mixer.clockChips();
                            m_mixer.resetBufs();
                        }
                    }
                } else {
                    //.Clock the machine
                    int size = (int) (m_c64.getMainCpuSpeed() / m_cfg.frequency);
                    while (m_isPlaying != state_t.STOPPED && (--size) != 0) {
                        run((int) sidemu.output.OUTPUTBUFFERSIZE);
                    }
                }
            } catch (Exception e) //(MOS6510.haltInstruction )
            {
                m_errorString = "Illegal instruction executed";
                m_isPlaying = state_t.STOPPING;
            }
        }

        if (m_isPlaying == state_t.STOPPING) {
            try {
                initialise();
            } catch (Exception e) { //(configError final &) { }
                m_isPlaying = state_t.STOPPED;
            }
        }

        return count;
    }

    public void stop() {
        if (m_tune != null && m_isPlaying == state_t.PLAYING) {
            m_isPlaying = state_t.STOPPING;
        }
    }

    public Boolean config(SidConfig cfg, Boolean force /*= false*/) {
        // Check if configuration have been changed or forced
        if (!force && !m_cfg.compare(cfg)) {
            return true;
        }

        // Check for base sampling frequency
        if (cfg.frequency < 8000) {
            m_errorString = ERR_UNSUPPORTED_FREQ;
            return false;
        }

        // Only do these if we have a loaded tune
        if (m_tune != null) {
            SidTuneInfo tuneInfo = m_tune.getInfo();

            try {
                sidRelease();

                List<Integer> addresses = new ArrayList<>();
                short secondSidAddress = tuneInfo.sidChipBase(1) != 0 ?
                        tuneInfo.sidChipBase(1) :
                        cfg.secondSidAddress;
                if (secondSidAddress != 0)
                    addresses.add(secondSidAddress & 0xffff);

                short thirdSidAddress = tuneInfo.sidChipBase(2) != 0 ?
                        tuneInfo.sidChipBase(2) :
                        cfg.thirdSidAddress;
                if (thirdSidAddress != 0)
                    addresses.add(thirdSidAddress & 0xffff);

                // SID emulation setup (must be performed before the
                // environment setup call)
                sidCreate(cfg.sidEmulation, cfg.defaultSidModel, cfg.forceSidModel, addresses);

                // Determine clock speed
                C64.model_t model = c64model(cfg.defaultC64Model, cfg.forceC64Model);

                m_c64.setModel(model);

                sidParams(m_c64.getMainCpuSpeed(), (int) cfg.frequency, cfg.samplingMethod, cfg.fastSampling);

                // Configure, setup and install C64 environment/events
                initialise();
            } catch (configError e) {
                m_errorString = e.message();
                m_cfg.sidEmulation = null;
                if (m_cfg != cfg) {
                    config(m_cfg, false);
                }
                return false;
            }
        }

        Boolean isStereo = cfg.playback == SidConfig.playback_t.STEREO;
        m_info.m_channels = (int) (isStereo ? 2 : 1);

        m_mixer.setStereo(isStereo);
        m_mixer.setVolume((int) cfg.leftVolume, (int) cfg.rightVolume);

        // Update Configuration
        m_cfg = cfg;

        return true;
    }

    //.Clock speed changes due to loading a new song
    private C64.model_t c64model(SidConfig.c64_model_t defaultModel, Boolean forced) {
        SidTuneInfo tuneInfo = m_tune.getInfo();

        SidTuneInfo.clock_t clockSpeed = tuneInfo.clockSpeed();

        C64.model_t model = C64.model_t.PAL_B;

        // Use preferred speed if forced or if song speed instanceof unknown
        if (forced || clockSpeed == SidTuneInfo.clock_t.CLOCK_UNKNOWN || clockSpeed == SidTuneInfo.clock_t.CLOCK_ANY) {
            switch (defaultModel) {
            case PAL:
                clockSpeed = SidTuneInfo.clock_t.CLOCK_PAL;
                model = C64.model_t.PAL_B;
                videoSwitch = 1;
                break;
            case DREAN:
                clockSpeed = SidTuneInfo.clock_t.CLOCK_PAL;
                model = C64.model_t.PAL_N;
                videoSwitch = 1; // TODO verify
                break;
            case NTSC:
                clockSpeed = SidTuneInfo.clock_t.CLOCK_NTSC;
                model = C64.model_t.NTSC_M;
                videoSwitch = 0;
                break;
            case OLD_NTSC:
                clockSpeed = SidTuneInfo.clock_t.CLOCK_NTSC;
                model = C64.model_t.OLD_NTSC_M;
                videoSwitch = 0;
                break;
            }
        } else {
            switch (clockSpeed) {
            default:
            case CLOCK_PAL:
                model = C64.model_t.PAL_B;
                videoSwitch = 1;
                break;
            case CLOCK_NTSC:
                model = C64.model_t.NTSC_M;
                videoSwitch = 0;
                break;
            }
        }

        switch (clockSpeed) {
        case CLOCK_PAL:
            if (tuneInfo.songSpeed() == SidTuneInfo.SPEED_CIA_1A)
                m_info.m_speedString = TXT_PAL_CIA;
            else if (tuneInfo.clockSpeed() == SidTuneInfo.clock_t.CLOCK_NTSC)
                m_info.m_speedString = TXT_PAL_VBI_FIXED;
            else
                m_info.m_speedString = TXT_PAL_VBI;
            break;
        case CLOCK_NTSC:
            if (tuneInfo.songSpeed() == SidTuneInfo.SPEED_CIA_1A)
                m_info.m_speedString = TXT_NTSC_CIA;
            else if (tuneInfo.clockSpeed() == SidTuneInfo.clock_t.CLOCK_PAL)
                m_info.m_speedString = TXT_NTSC_VBI_FIXED;
            else
                m_info.m_speedString = TXT_NTSC_VBI;
            break;
        default:
            break;
        }

        return model;
    }

    /**
     * //Get the SID model.
     * <p>
     * //@param sidModel the tune requested model
     * //@param defaultModel the default model
     * //@param forced true if the default model shold be forced : spite of tune model
     */
    public SidConfig.sid_model_t getSidModel(SidTuneInfo.model_t sidModel, SidConfig.sid_model_t defaultModel, Boolean forced) {
        SidTuneInfo.model_t tuneModel = sidModel;

        // Use preferred speed if forced or if song speed instanceof unknown
        if (forced || tuneModel == SidTuneInfo.model_t.SIDMODEL_UNKNOWN || tuneModel == SidTuneInfo.model_t.SIDMODEL_ANY) {
            switch (defaultModel) {
            case MOS6581:
                tuneModel = SidTuneInfo.model_t.SIDMODEL_6581;
                break;
            case MOS8580:
                tuneModel = SidTuneInfo.model_t.SIDMODEL_8580;
                break;
            default:
                break;
            }
        }

        SidConfig.sid_model_t newModel;

        switch (tuneModel) {
        default:
        case SIDMODEL_6581:
            newModel = SidConfig.sid_model_t.MOS6581;
            break;
        case SIDMODEL_8580:
            newModel = SidConfig.sid_model_t.MOS8580;
            break;
        }

        return newModel;
    }

    private void sidRelease() {
        m_c64.clearSids();

        for (int i = 0; ; i++) {
            sidemu s = m_mixer.getSid(i);
            if (s == null)
                break;
            SidBuilder b = s.builder();
            if (b != null) {
                b.unlock(s);
            }
        }

        m_mixer.clearSids();
    }

    private void sidCreate(SidBuilder builder, SidConfig.sid_model_t defaultModel,
                           Boolean forced, List<Integer> extraSidAddresses) {
        if (builder != null) {
            SidTuneInfo tuneInfo = m_tune.getInfo();

            // Setup base SID
            SidConfig.sid_model_t userModel = getSidModel(tuneInfo.sidModel(0), defaultModel, forced);
            sidemu s = builder.lock_(m_c64.getEventScheduler(), userModel);
            if (!builder.getStatus()) {
                throw new configError(builder.error());
            }

            m_c64.setBaseSid(s);
            m_mixer.addSid(s);

            // Setup extra SIDs if needed
            if (extraSidAddresses.size() != 0) {
                // If bits 6-7 are set to Unknown then the second SID will be set to the same SID
                // model as the first SID.
                defaultModel = userModel;

                int extraSidChips = (int) extraSidAddresses.size();

                for (int i = 0; i < extraSidChips; i++) {
                    SidConfig.sid_model_t userModel_1 = getSidModel(tuneInfo.sidModel(i + 1), defaultModel, forced);

                    sidemu s1 = builder.lock_(m_c64.getEventScheduler(), userModel_1);
                    if (!builder.getStatus()) {
                        throw new configError(builder.error());
                    }

                    if (!m_c64.addExtraSid(s1, (int) extraSidAddresses.get((int) i)))
                        throw new configError(ERR_UNSUPPORTED_SID_ADDR);

                    m_mixer.addSid(s1);
                }
            }
        }
    }

    private void sidParams(double cpuFreq, int frequency,
                           SidConfig.sampling_method_t sampling, Boolean fastSampling) {
        for (int i = 0; ; i++) {
            sidemu s = m_mixer.getSid(i);
            if (s == null)
                break;

            s.sampling((float) cpuFreq, frequency, sampling, fastSampling);
        }
    }

//# ifdef PC64_TESTSUITE
//    @Override
//    public void load(String file) {
//        String name = "$enable_testsuite";// PC64_TESTSUITE;
//        name += file;
//        name += ".prg";
//
//        m_tune.load(name, false);
//        m_tune.selectSong(0);
//        initialise();
//    }
//
//    public Integer[][] GetSidRegister() {
//        return m_mixer.GetSidRegister();
//    }
//#endif
}

