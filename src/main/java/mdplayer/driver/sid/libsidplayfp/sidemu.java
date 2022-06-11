/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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
import mdplayer.driver.sid.libsidplayfp.c64.C64Sid;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;


    /**
     //Inherit this class to create a new SID emulation.
     */
    public class sidemu extends C64Sid
    {



        //# include <String>
        //# include "SidPlayFp/SidConfig.h"
        //# include "SidPlayFp/siddefs.h"
        //# include "Event.h"
        //# include "EventScheduler.h"
        //# include "C64/C64Sid.h"
        //# include "sidcxx11.h"
        //class SidBuilder;

        /**
         //Buffer size. 5000 instanceof roughly 5 ms at 96 kHz
         */
        public static class output
        {
            static public int OUTPUTBUFFERSIZE = 5000;
        }

        private SidBuilder m_builder;

        //protected String ERR_UNSUPPORTED_FREQ;
        //protected String ERR_INVALID_SAMPLING;
        //protected String ERR_INVALID_CHIP;

        protected EventScheduler eventScheduler;

        protected long m_accessClk;

         //The sample buffer
        protected short[] m_buffer;

         //Current position : buffer
        protected int m_bufferpos;

        protected Boolean m_status;
        protected Boolean isLocked;

        protected String m_error;

        public sidemu(SidBuilder builder)
        {
            m_builder = (builder);
            eventScheduler = (null);
            m_buffer = (null);
            m_bufferpos = (0);
            m_status = (true);
            isLocked = (false);
            m_error = ("N/A");
        }

        protected void finalize() { }

        /**
         /.Clock the SID chip.
         */
        public void clock() { }

        /**
         //Set execution environment and synchronized Sid to it.
         */
        //public Boolean lock_(EventScheduler scheduler) { return false; }

        /**
         //Unsynchronized Sid.
         */
        //public void unlock() { }

        // Standard SID functions

        /**
         //Mute/unmute voice.
         */
        public void voice(int num, Boolean mute) { }

        /**
         //Set SID model.
         */
        public void model(SidConfig.sid_model_t model) { }

        /**
         //Set the sampling method.
         *
         //@param systemfreq
         //@param outputfreq
         //@param method
         //@param fast
         */
        //void sampling(float systemfreq SID_UNUSED, float outputfreq SID_UNUSED,
        //SidConfig::sampling_method_t method SID_UNUSED, Boolean fast SID_UNUSED)
        //{ }
        public void sampling(float systemfreq, float outputfreq,
        SidConfig.sampling_method_t method, Boolean fast)
        { }

        /**
         //Get a detailed error message.
         */
        public String error() { return m_error; }

        public SidBuilder builder() { return m_builder; }

        /**
         //Get the current position : buffer.
         */
        public int bufferpos() { return m_bufferpos; }

        /**
         //Set the position : buffer.
         */
        public void bufferpos(int pos) { m_bufferpos = pos; }

        /**
         //Get the buffer.
         */
        public short[] buffer() { return m_buffer; }



        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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

        //#include "sidemu.h"

        protected String ERR_UNSUPPORTED_FREQ = "Unable to set desired output frequency.";
        protected String ERR_INVALID_SAMPLING = "Invalid sampling method.";
        protected String ERR_INVALID_CHIP = "Invalid chip model.";

        public Boolean lock_(EventScheduler scheduler)
        {
            if (isLocked)
                return false;

            isLocked = true;
            eventScheduler = scheduler;

            return true;
        }

        public void unlock()
        {
            isLocked = false;
            eventScheduler = null;
        }

        public Integer[] GetRegister()
        {
            throw new UnsupportedOperationException();
        }
    }

