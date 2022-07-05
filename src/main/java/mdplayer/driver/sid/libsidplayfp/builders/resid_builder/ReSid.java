/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2001 Simon White
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

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder;

import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.Const;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.SidEmu;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid.Sid;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid.SidDefs;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;


/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2001 Simon White
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
public class ReSid extends SidEmu {

    public Sid getSID() {
        return sid;
    }

    private Sid sid;
    private byte voiceMask;

    public boolean getStatus() {
        return status;
    }

    public static String getCredits() {
        final String credits =
            "ReSid V" + Const.VERSION + " Engine:\n" +
            "\t(C) 1999-2002 Simon White\n" +
            "MOS6581 (Sid) Emulation (ReSid V" + SidDefs.residVersionString + "):\n" +
            "\t(C) 1999-2010 Dag Lem\n";
        return credits;
    }

    public ReSid(SidBuilder builder, Setting setting) {
        super(builder);
        sid = new Sid(setting);
        voiceMask = 0x07;
        buffer = new short[(int) Output.outputBufferSize];
        reset((byte) 0);
    }

    public void bias(double dac_bias) {
        sid.adjustFilterBias(dac_bias);
    }

    // Standard component options

    @Override
    public void reset(byte volume) {
        accessClock = 0;
        sid.reset();
        sid.write(0x18, volume);
    }

    @Override
    public byte read(byte addr) {
        //System.err.println("[%010d]read  accessClock[%02x]", accessClock, addr);
        clock();
        return (byte) sid.read(addr);
    }

    @Override
    public void write(byte addr, byte data) {
        //System.err.println("[%010d]write accessClock[%02x] data[%02x]", accessClock, addr,data);
        clock();
        sid.write(addr, data);
    }

    @Override
    public void clock() {
        int cycles = (int) eventScheduler.getTime(accessClock, EventScheduler.EventPhase.CLOCK_PHI1);
        accessClock += cycles;
        bufferPos += sid.clock(cycles, buffer, bufferPos, Output.outputBufferSize - bufferPos, 1);
    }

    public void filter(boolean enable) {
        sid.enableFilter(enable);
    }

    @Override
    public void sampling(float systemFreq, float outputFreq, SidConfig.SamplingMethod method, boolean fast) {
        SidDefs.SamplingMethod sampleMethod;
        switch (method) {
        case INTERPOLATE:
            sampleMethod = fast ? SidDefs.SamplingMethod.FAST : SidDefs.SamplingMethod.INTERPOLATE;
            break;
        case RESAMPLE_INTERPOLATE:
            sampleMethod = fast ? SidDefs.SamplingMethod.RESAMPLE_FASTMEM : SidDefs.SamplingMethod.RESAMPLE;
            break;
        default:
            status = false;
            error = ERR_INVALID_SAMPLING;
            return;
        }

        if (!sid.setSamplingParameters(systemFreq, sampleMethod, outputFreq, -1, 0.97)) {
            status = false;
            error = ERR_UNSUPPORTED_FREQ;
            return;
        }

        status = true;
    }

    @Override
    public void voice(int num, boolean mute) {
        if (mute)
            voiceMask &= (byte) ~(1 << num);
        else
            voiceMask |= (byte) (1 << num);

        sid.setVoiceMask(voiceMask);
    }

    /** Set the emulated Sid model */
    @Override
    public void model(SidConfig.SidModel model) {
        SidDefs.ChipModel chipModel;
        switch (model) {
        case MOS6581:
            chipModel = SidDefs.ChipModel.MOS6581;
            break;
        case MOS8580:
            chipModel = SidDefs.ChipModel.MOS8580;
            break;
                // MOS8580 + digi boost
                //chipModel = (RESID_NS::MOS8580);
                //m_sid.set_voice_mask(0x0f);
                //m_sid.input(-32768);
        default:
            status = false;
            error = ERR_INVALID_CHIP;
            return;
        }

        sid.set_chip_model(chipModel);
        status = true;
    }

    @Override
    public Integer[] getRegister() {
        return sid.GetRegister();
    }
}