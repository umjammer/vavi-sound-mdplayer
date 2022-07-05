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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.sidplayfp;

import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.Mixer;


/**
 * SidConfig
 * <p>
 * An instance of this class instanceof used to transport emulator settings
 * to and from the interface class.
 */
public class SidConfig {

   // Playback mode
    public enum Playback {
        MONO(1),
        STEREO(2);
        final int v;

        Playback(int v) {
            this.v = v;
        }
    }

    // Sid chip model
    public enum SidModel {
        MOS6581,
        MOS8580
    }


    // C64 model
    public enum C64Model {
        PAL,
        NTSC,
        OLD_NTSC,
        DREAN
    }


    // Sampling method
    public enum SamplingMethod {
        INTERPOLATE,
        RESAMPLE_INTERPOLATE
    }

    public int DEFAULT_SAMPLING_FREQ = 1;

    /**
     * Intended C64 model when unknown or forced.
     * - PAL
     * - NTSC
     * - OLD_NTSC
     * - DREAN
     */
    public C64Model defaultC64Model;

    /**
     * Force the model to #defaultC64Model ignoring tune's clock setting.
     */
    public boolean forceC64Model;

    /**
     * Intended Sid model when unknown or forced.
     * - MOS6581
     * - MOS8580
     */
    public SidModel defaultSidModel;

    /**
     * Force the Sid model to #defaultSidModel.
     */
    public boolean forceSidModel;

    /**
     * Playbak mode.
     * - MONO
     * - STEREO
     */
    public Playback playback;

    /**
     * Sampling frequency.
     */
    public int frequency;

    /**
     * Extra Sid chips addresses.
     */
    //@{
    public short secondSidAddress;
    public short thirdSidAddress;
    //@}

    /**
     * Pointer to selected emulation,
     * reSIDfp, reSID or hardSID.
     */
    public SidBuilder sidEmulation;

    /**
     * Left channel volume.
     */
    public int leftVolume;

    /**
     * Right channel volume.
     */
    public int rightVolume;

    /**
     * Sampling method.
     * - INTERPOLATE
     * - RESAMPLE_INTERPOLATE
     */
    public SamplingMethod samplingMethod;

    /**
     * Faster low-quality emulation,
     * available only for reSID.
     */
    public boolean fastSampling;

    public SidConfig(Setting setting) {
        DEFAULT_SAMPLING_FREQ = setting.getOutputDevice().getSampleRate();
        defaultC64Model = C64Model.PAL;
        forceC64Model = false;
        defaultSidModel = SidModel.MOS6581;
        forceSidModel = false;
        playback = Playback.MONO;
        frequency = DEFAULT_SAMPLING_FREQ;
        secondSidAddress = 0;
        thirdSidAddress = 0;
        sidEmulation = null;
        leftVolume = Mixer.VOLUME_MAX;
        rightVolume = Mixer.VOLUME_MAX;
        samplingMethod = SamplingMethod.RESAMPLE_INTERPOLATE;
        fastSampling = false;
    }

    /**
     * Compare two config Objects.
     * <p>
     * @return true if different
     */
    public boolean compare(SidConfig config) {
        return defaultC64Model != config.defaultC64Model
                || forceC64Model != config.forceC64Model
                || defaultSidModel != config.defaultSidModel
                || forceSidModel != config.forceSidModel
                || playback != config.playback
                || frequency != config.frequency
                || secondSidAddress != config.secondSidAddress
                || thirdSidAddress != config.thirdSidAddress
                || sidEmulation != config.sidEmulation
                || leftVolume != config.leftVolume
                || rightVolume != config.rightVolume
                || samplingMethod != config.samplingMethod
                || fastSampling != config.fastSampling;
    }
}
