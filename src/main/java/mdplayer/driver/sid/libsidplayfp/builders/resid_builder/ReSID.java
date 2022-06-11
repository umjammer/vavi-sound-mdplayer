/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid.SID;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid.siddefs;
import mdplayer.driver.sid.libsidplayfp.sidemu;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;


public class ReSID extends sidemu {

    public SID getSID() {
        return m_sid;
    }

    private SID m_sid;
    private byte m_voiceMask;

    //public static String getCredits() { return ""; }

    //public ReSID(libsidplayfp.SidPlayFp.SidBuilder builder) : base(builder) { }
    //protected void finalize() { }

    public Boolean getStatus() {
        return m_status;
    }

    //@Override public byte read(byte addr) { return 0; }
    //@Override public void write(byte addr, byte data) { }

    // C64Sid functions
    //@Override public void reset(byte volume) { }

    // Standard SID emu functions
    //@Override public void clock() { }

    //public void sampling(float systemclock, float freq, SidPlayFp.SidConfig.sampling_method_t method, Boolean fast){ }

    //@Override public void voice(int num, Boolean mute) { }

    //public void model(SidPlayFp.SidConfig.sid_model_t model) { }

    // Specific to resid
    //public void bias(double dac_bias) { }
    //public void filter(Boolean enable) { }




    /*
     * This file instanceof part of libsidplayfp, a SID player engine.
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
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     *
     * You should have received a copy of the GNU General Public License
     * along with this program; if not, write to the Free Software
     * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
     */

    public static String getCredits() {
        String credits = "";

        if (credits == "") {
            // Setup credits
            String ss;
            ss = "ReSID V" + Const.VERSION + " Engine:\n";
            ss = "\t(C) 1999-2002 Simon White\n";
            ss = "MOS6581 (SID) Emulation (ReSID V" + siddefs.resid_version_String + "):\n";
            ss = "\t(C) 1999-2010 Dag Lem\n";
            credits = ss;
        }

        return credits;
    }

    public ReSID(SidBuilder builder, Setting setting) {
        super(builder);
        m_sid = new SID(setting);
        m_voiceMask = 0x07;
        m_buffer = new short[(int) sidemu.output.OUTPUTBUFFERSIZE];
        reset((byte) 0);
    }

    protected void finalize() {
        m_sid = null;
        m_buffer = null;
    }

    public void bias(double dac_bias) {
        m_sid.adjust_filter_bias(dac_bias);
    }

    // Standard component options
    @Override
    public void reset(byte volume) {
        m_accessClk = 0;
        m_sid.reset();
        m_sid.write(0x18, volume);
    }

    @Override
    public byte read(byte addr) {
        //System.err.println("[{0:d010}]read  addr[{1:x02}]", m_accessClk, addr);
        clock();
        return (byte) m_sid.read((int) addr);
    }

    @Override
    public void write(byte addr, byte data) {
        //System.err.println("[{0:d010}]write addr[{1:x02}] data[{2:x02}]", m_accessClk, addr,data);
        clock();
        m_sid.write(addr, data);
    }

    @Override
    public void clock() {
        int cycles = (int) eventScheduler.getTime(m_accessClk, EventScheduler.event_phase_t.EVENT_CLOCK_PHI1.EVENT_CLOCK_PHI1);
        m_accessClk += cycles;
        m_bufferpos += (int) m_sid.clock(cycles, m_buffer, m_bufferpos, (int) ((int) sidemu.output.OUTPUTBUFFERSIZE - m_bufferpos), 1);
    }

    public void filter(Boolean enable) {
        m_sid.enable_filter(enable);
    }

    @Override
    public void sampling(float systemclock, float freq, SidConfig.sampling_method_t method, Boolean fast) {
        siddefs.sampling_method sampleMethod;
        switch (method) {
        case INTERPOLATE:
            sampleMethod = fast ? siddefs.sampling_method.SAMPLE_FAST : siddefs.sampling_method.SAMPLE_INTERPOLATE;
            break;
        case RESAMPLE_INTERPOLATE:
            sampleMethod = fast ? siddefs.sampling_method.SAMPLE_RESAMPLE_FASTMEM : siddefs.sampling_method.SAMPLE_RESAMPLE;
            break;
        default:
            m_status = false;
            m_error = ERR_INVALID_SAMPLING;
            return;
        }

        if (!m_sid.set_sampling_parameters(systemclock, sampleMethod, freq, -1, 0.97)) {
            m_status = false;
            m_error = ERR_UNSUPPORTED_FREQ;
            return;
        }

        m_status = true;
    }

    @Override
    public void voice(int num, Boolean mute) {
        if (mute)
            m_voiceMask &= (byte) ~(1 << (int) num);
        else
            m_voiceMask |= (byte) (1 << (int) num);

        m_sid.set_voice_mask(m_voiceMask);
    }

    // Set the emulated SID model
    @Override
    public void model(SidConfig.sid_model_t model) {
        siddefs.chip_model chipModel;
        switch (model) {
        case MOS6581:
            chipModel = siddefs.chip_model.MOS6581;
            break;
        case MOS8580:
            chipModel = siddefs.chip_model.MOS8580;
            break;
                /* MOS8580 + digi boost
                //     chipModel = (RESID_NS::MOS8580);
                //     m_sid.set_voice_mask(0x0f);
                //     m_sid.input(-32768);
                */
        default:
            m_status = false;
            m_error = ERR_INVALID_CHIP;
            return;
        }

        m_sid.set_chip_model(chipModel);
        m_status = true;
    }

    @Override
    public Integer[] GetRegister() {
        return m_sid.GetRegister();
    }
}