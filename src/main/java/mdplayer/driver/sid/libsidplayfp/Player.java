/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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
import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.c64.C64;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;


public class Player {

    private enum State {
        STOPPED,
        PLAYING,
        STOPPING
    }

    // Commodore 64 emulator
    private C64 c64 = new C64();

    // Mixer
    private Mixer mixer = new Mixer();

    // Emulator info
    private SidTune tune;

    // User Configuration Settings
    private SidInfoImpl info = new SidInfoImpl();

    // User Configuration Settings
    private SidConfig config;

    // Error message
    private String errorString;

    private volatile State isPlaying;

    // PAL/NTSC switch value
    private byte videoSwitch;

    public SidConfig config() {
        return config;
    }

    public SidInfo info() {
        return info;
    }

    public boolean isPlaying() {
        return isPlaying != State.STOPPED;
    }

    public int time() {
        return c64.getTime();
    }

    public void debug(boolean enable, FileStream out) {
        c64.debug(enable, out);
    }

    public String error() {
        return errorString;
    }

    public short getCia1TimerA() {
        return c64.getCia1TimerA();
    }

    // Speed Strings
    static final String TXT_PAL_VBI = "50 Hz VBI (PAL)";
    static final String TXT_PAL_VBI_FIXED = "60 Hz VBI (PAL FIXED)";
    static final String TXT_PAL_CIA = "CIA (PAL)";
    //static final String TXT_PAL_UNKNOWN = "UNKNOWN (PAL)";
    static final String TXT_NTSC_VBI = "60 Hz VBI (NTSC)";
    static final String TXT_NTSC_VBI_FIXED = "50 Hz VBI (NTSC FIXED)";
    static final String TXT_NTSC_CIA = "CIA (NTSC)";
    //static final String TXT_NTSC_UNKNOWN = "UNKNOWN (NTSC)";

    // Error Strings
    static final String ERR_NA = "NA";
    static final String ERR_UNSUPPORTED_FREQ = "SIDPLAYER ERROR: Unsupported sampling frequency.";
    static final String ERR_UNSUPPORTED_SID_ADDR = "SIDPLAYER ERROR: Unsupported Sid address.";
    static final String ERR_UNSUPPORTED_SIZE = "SIDPLAYER ERROR: size of Music data exceeds C64 memory.";
    static final String ERR_INVALID_PERCENTAGE = "SIDPLAYER ERROR: Percentage value  of range.";

    /**
     * Configuration error exception.
     */
    public static class ConfigError extends RuntimeException {
        private String message;

        public ConfigError(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }

    private Setting setting;

//        public Player(MDPlayer.Setting setting) {
//            this.setting = setting;
//            m_cfg = new SidPlayFp.SidConfig(setting);
//        }

    public Player(Setting setting) {
        this.setting = setting;
        config = new SidConfig(setting);

        // Set default settings for system
        tune = null;
        errorString = ERR_NA;
        isPlaying = State.STOPPED;
//# ifdef PC64_TESTSUITE
//            m_c64.setTestEnv(this);
//#endif

        c64.setRoms(null, null, null);
        config(config, false);

        // Get component credits
        info.credits.add(c64.cpuCredits());
        info.credits.add(c64.ciaCredits());
        info.credits.add(c64.vicCredits());
    }

    /** @param desc TODO OUT ? */
    public void checkRomByKernalChecker(byte[] rom, String desc) {

        if (rom != null) {
            KernalChecker romChecker = new KernalChecker(rom);
            desc = romChecker.info();
        } else
            desc = "";
    }

    /** @param desc TODO OUT ? */
    public void checkRomByBasicChecker(byte[] rom, String desc) {
        if (rom != null) {
            BasicChecker romChecker = new BasicChecker(rom);
            desc = romChecker.info();
        } else
            desc = "";
    }

    /** @param desc TODO OUT ? */
    public void checkRomByChargenChecker(byte[] rom, String desc) {
        if (rom != null) {
            ChargenChecker romChecker = new ChargenChecker(rom);
            desc = romChecker.info();
        } else
            desc = "";
    }

    public void setRoms(byte[] kernal, byte[] basic, byte[] character) {
        checkRomByKernalChecker(kernal, info.kernalDesc);
        checkRomByBasicChecker(basic, info.basicDesc);
        checkRomByChargenChecker(character, info.chargenDesc);

        c64.setRoms(kernal, basic, character);
    }

    public boolean fastForward(int percent) {
        if (!mixer.setFastForward(percent / 100)) {
            errorString = ERR_INVALID_PERCENTAGE;
            return false;
        }

        return true;
    }

    /**
     * Initialize the emulation.
     *
     * @throws ConfigError
     */
    private void initialise() {
        isPlaying = State.STOPPED;

        c64.reset();

        SidTuneInfo tuneInfo = tune.getInfo();

        int size = (int) (tuneInfo.loadAddr()) + tuneInfo.c64dataLen() - 1;
        if (size > 0xffff) {
            throw new ConfigError(ERR_UNSUPPORTED_SIZE);
        }

        PSidDrv driver = new PSidDrv(tune.getInfo());
        if (!driver.relocateDriver()) {
            throw new ConfigError(driver.errorString());
        }

        info.driverAddress = driver.driverAddr();
        info.driverLength = driver.driverLength();

        SidMemory sm = c64.getMemInterface();
        driver.install(sm, videoSwitch);

        sm = c64.getMemInterface();
        if (!tune.placeSidTuneInC64mem(sm)) {
            throw new ConfigError(tune.statusString());
        }

        c64.resetCpu();
        //System.err.println("%x", sm.readMemByte(0x17e3));
    }

    public boolean load(SidTune tune) {
        this.tune = tune;

        if (tune != null) {
            // Must re-configure on fly for stereo support!
            if (!config(config, true)) {
                // Failed configuration with new tune, reject it
                this.tune = null;
                return false;
            }
        }
        return true;
    }

    public void mute(int sidNum, int voice, boolean enable) {
        SidEmu s = mixer.getSid(sidNum);
        if (s != null)
            s.voice(voice, enable);
    }

    /**
     * @throws "Mos6510#haltInstruction"
     */
    private void run(int events) {
        for (int i = 0; isPlaying != State.STOPPED && i < events; i++) {
            //System.err.println("run counter i : %d",i);
            c64.clock();
        }
    }

    public int play(short[] buffer, int count) {
        // Make sure a tune instanceof loaded
        if (tune == null)
            return 0;

        // Start the player loop
        if (isPlaying == State.STOPPED)
            isPlaying = State.PLAYING;

        if (isPlaying == State.PLAYING) {
            mixer.begin(buffer, count);
            //Log.Write(String.format("%d", count));
            try {
                if (mixer.getSid(0) != null) {
                    if (count != 0 && buffer != null) {
                        // clock chips and mix into Output buffer
                        while (isPlaying != State.STOPPED && mixer.notFinished()) {
                            run(SidEmu.Output.outputBufferSize);

                            mixer.clockChips();
                            mixer.doMix();
                        }
                        count = mixer.samplesGenerated();
                    } else {
                        // clock chips and discard buffers
                        int size = (int) (c64.getMainCpuSpeed() / config.frequency);
                        while (isPlaying != State.STOPPED && (--size) != 0) {
                            run(SidEmu.Output.outputBufferSize);

                            mixer.clockChips();
                            mixer.resetBufs();
                        }
                    }
                } else {
                    // clock the machine
                    int size = (int) (c64.getMainCpuSpeed() / config.frequency);
                    while (isPlaying != State.STOPPED && (--size) != 0) {
                        run(SidEmu.Output.outputBufferSize);
                    }
                }
            } catch (Exception e) { // Mos6510.haltInstruction
                errorString = "Illegal instruction executed";
                isPlaying = State.STOPPING;
            }
        }

        if (isPlaying == State.STOPPING) {
            try {
                initialise();
            } catch (Exception e) { // ConfigError
                isPlaying = State.STOPPED;
            }
        }

        return count;
    }

    public void stop() {
        if (tune != null && isPlaying == State.PLAYING) {
            isPlaying = State.STOPPING;
        }
    }

    public boolean config(SidConfig cfg, boolean force /*= false*/) {
        // Check if configuration have been changed or forced
        if (!force && !config.compare(cfg)) {
            return true;
        }

        // Check for base sampling frequency
        if (cfg.frequency < 8000) {
            errorString = ERR_UNSUPPORTED_FREQ;
            return false;
        }

        // Only do these if we have a loaded tune
        if (tune != null) {
            SidTuneInfo tuneInfo = tune.getInfo();

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

                // Sid emulation setup (must be performed before the
                // environment setup call)
                sidCreate(cfg.sidEmulation, cfg.defaultSidModel, cfg.forceSidModel, addresses);

                // Determine clock speed
                C64.Clock model = c64model(cfg.defaultC64Model, cfg.forceC64Model);

                c64.setModel(model);

                sidParams(c64.getMainCpuSpeed(), cfg.frequency, cfg.samplingMethod, cfg.fastSampling);

                // Configure, setup and install C64 environment/events
                initialise();
            } catch (ConfigError e) {
                errorString = e.message();
                config.sidEmulation = null;
                if (config != cfg) {
                    config(config, false);
                }
                return false;
            }
        }

        boolean isStereo = cfg.playback == SidConfig.Playback.STEREO;
        info.channels = isStereo ? 2 : 1;

        mixer.setStereo(isStereo);
        mixer.setVolume(cfg.leftVolume, cfg.rightVolume);

        // Update Configuration
        config = cfg;

        return true;
    }

    /**
     * Get the C64 model for the current loaded tune.
     * <p>
     * .Clock speed changes due to loading a new song
     *
     * @param defaultModel the default model
     * @param forced true if the default model should be forced : spite of tune model
     */
    private C64.Clock c64model(SidConfig.C64Model defaultModel, boolean forced) {
        SidTuneInfo tuneInfo = tune.getInfo();

        SidTuneInfo.Clock clockSpeed = tuneInfo.clockSpeed();

        C64.Clock model = C64.Clock.PAL_B;

        // Use preferred speed if forced or if song speed instanceof unknown
        if (forced || clockSpeed == SidTuneInfo.Clock.UNKNOWN || clockSpeed == SidTuneInfo.Clock.ANY) {
            switch (defaultModel) {
            case PAL:
                clockSpeed = SidTuneInfo.Clock.PAL;
                model = C64.Clock.PAL_B;
                videoSwitch = 1;
                break;
            case DREAN:
                clockSpeed = SidTuneInfo.Clock.PAL;
                model = C64.Clock.PAL_N;
                videoSwitch = 1; // TODO verify
                break;
            case NTSC:
                clockSpeed = SidTuneInfo.Clock.NTSC;
                model = C64.Clock.NTSC_M;
                videoSwitch = 0;
                break;
            case OLD_NTSC:
                clockSpeed = SidTuneInfo.Clock.NTSC;
                model = C64.Clock.OLD_NTSC_M;
                videoSwitch = 0;
                break;
            }
        } else {
            switch (clockSpeed) {
            default:
            case PAL:
                model = C64.Clock.PAL_B;
                videoSwitch = 1;
                break;
            case NTSC:
                model = C64.Clock.NTSC_M;
                videoSwitch = 0;
                break;
            }
        }

        switch (clockSpeed) {
        case PAL:
            if (tuneInfo.songSpeed() == SidTuneInfo.SPEED_CIA_1A)
                info.speedString = TXT_PAL_CIA;
            else if (tuneInfo.clockSpeed() == SidTuneInfo.Clock.NTSC)
                info.speedString = TXT_PAL_VBI_FIXED;
            else
                info.speedString = TXT_PAL_VBI;
            break;
        case NTSC:
            if (tuneInfo.songSpeed() == SidTuneInfo.SPEED_CIA_1A)
                info.speedString = TXT_NTSC_CIA;
            else if (tuneInfo.clockSpeed() == SidTuneInfo.Clock.PAL)
                info.speedString = TXT_NTSC_VBI_FIXED;
            else
                info.speedString = TXT_NTSC_VBI;
            break;
        default:
            break;
        }

        return model;
    }

    /**
     * Get the Sid model.
     *
     * @param sidModel the tune requested model
     * @param defaultModel the default model
     * @param forced true if the default model should be forced : spite of tune model
     */
    public SidConfig.SidModel getSidModel(SidTuneInfo.Model sidModel, SidConfig.SidModel defaultModel, boolean forced) {
        SidTuneInfo.Model tuneModel = sidModel;

        // Use preferred speed if forced or if song speed instanceof unknown
        if (forced || tuneModel == SidTuneInfo.Model.SID_UNKNOWN || tuneModel == SidTuneInfo.Model.SID_ANY) {
            switch (defaultModel) {
            case MOS6581:
                tuneModel = SidTuneInfo.Model.SID_6581;
                break;
            case MOS8580:
                tuneModel = SidTuneInfo.Model.SID_8580;
                break;
            default:
                break;
            }
        }

        SidConfig.SidModel newModel;

        switch (tuneModel) {
        default:
        case SID_6581:
            newModel = SidConfig.SidModel.MOS6581;
            break;
        case SID_8580:
            newModel = SidConfig.SidModel.MOS8580;
            break;
        }

        return newModel;
    }

    /**
     * Release the Sid builders.
     */
    private void sidRelease() {
        c64.clearSids();

        for (int i = 0; ; i++) {
            SidEmu s = mixer.getSid(i);
            if (s == null)
                break;
            SidBuilder b = s.builder();
            if (b != null) {
                b.unlock(s);
            }
        }

        mixer.clearSids();
    }

    /**
     * Create the Sid emulation(s).
     *
     * @throws ConfigError
     */
    private void sidCreate(SidBuilder builder, SidConfig.SidModel defaultModel,
                           boolean forced, List<Integer> extraSidAddresses) {
        if (builder != null) {
            SidTuneInfo tuneInfo = tune.getInfo();

            // Setup base Sid
            SidConfig.SidModel userModel = getSidModel(tuneInfo.sidModel(0), defaultModel, forced);
            SidEmu s = builder.lock(c64.getEventScheduler(), userModel);
            if (!builder.getStatus()) {
                throw new ConfigError(builder.error());
            }

            c64.setBaseSid(s);
            mixer.addSid(s);

            // Setup extra SIDs if needed
            if (extraSidAddresses.size() != 0) {
                // If bits 6-7 are set to Unknown then the second Sid will be set to the same Sid
                // model as the first Sid.
                defaultModel = userModel;

                int extraSidChips = extraSidAddresses.size();

                for (int i = 0; i < extraSidChips; i++) {
                    SidConfig.SidModel userModel_1 = getSidModel(tuneInfo.sidModel(i + 1), defaultModel, forced);

                    SidEmu s1 = builder.lock(c64.getEventScheduler(), userModel_1);
                    if (!builder.getStatus()) {
                        throw new ConfigError(builder.error());
                    }

                    if (!c64.addExtraSid(s1, extraSidAddresses.get(i)))
                        throw new ConfigError(ERR_UNSUPPORTED_SID_ADDR);

                    mixer.addSid(s1);
                }
            }
        }
    }

    /**
     * Set the Sid emulation parameters.
     *
     * @param cpuFreq the CPU clock frequency
     * @param frequency the Output sampling frequency
     * @param sampling the sampling method to use
     * @param fastSampling true to enable fast low quality resampling (only for reSID)
     */
    private void sidParams(double cpuFreq, int frequency,
                           SidConfig.SamplingMethod sampling, boolean fastSampling) {
        for (int i = 0; ; i++) {
            SidEmu s = mixer.getSid(i);
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
//        name += ".Prg";
//
//        m_tune.load(name, false);
//        m_tune.selectSong(0);
//        initialise();
//    }
//
    public Integer[][] getSidRegister() {
        return mixer.getSidRegister();
    }
//#endif
}

