/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2013 Leandro Nini <drfiemost@users.sourceforge.net>
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
package mdplayer.driver.sid.libsidplayfp.sidplayfp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.sidemu;


    /**
     //Base class for Sid builders.
     */
    public class SidBuilder
    {




        //# include <set>
        //# include <String>
        //# include "SidPlayFp/SidConfig.h"
        //class sidemu;
        //class EventScheduler;

        //protected typedef std::set<libsidplayfp::sidemu*> emuset_t;

        private String m_name;

        protected String m_errorBuffer;

        //protected SortedSet<libsidplayfp.sidemu> sidobjs = new SortedSet<sidemu>();
        protected List<sidemu> sidobjs = new ArrayList<>();

        protected Boolean m_status;

        /**
         //Utility class for setting emu parameters : builders.
         */
        //template<class Temu, typename Tparam>
        //protected class applyParameter
        //{
        //    protected Tparam m_param;
        //    //protected void (Temu::* m_method) (Tparam);
        //    public delegate void dlgM_method(Tparam a);
        //    public dlgM_method m_method;

        //    //public applyParameter(void (Temu::* method)(Tparam), Tparam param)
        //    public applyParameter(dlgM_method method, Tparam param)
        //    {
        //        m_param = param;
        //        m_method = method;
        //    }

        //    public void opeKakko(sidemu e)
        //    {
        //        ((Temu)e).m_method(m_param);
        //    }
        //}

        protected static class applyParameter_LibsidplayfpReSID_bool
        {
            protected Boolean m_param;
            public interface dlgM_method extends Consumer<Boolean>{}
            public dlgM_method m_method;

            public applyParameter_LibsidplayfpReSID_bool(dlgM_method method, Boolean param)
            {
                m_param = param;
                m_method = method;
            }

            public void opeKakko(sidemu e)
            {
                m_method.accept(m_param);
            }
        }

        protected static class applyParameter_LibsidplayfpReSID_double
        {
            protected double m_param;
            public interface dlgM_method extends Consumer<Double>{}
            public dlgM_method m_method;

            public applyParameter_LibsidplayfpReSID_double(dlgM_method method, double param)
            {
                m_param = param;
                m_method = method;
            }

            public void opeKakko(sidemu e)
            {
                m_method.accept(m_param);
            }
        }

        public SidBuilder(String name)
        {
            m_name = name;
            m_errorBuffer = "N/A";
            m_status = (true);
        }

        protected void finalize() { }

        /**
         //The number of used devices.
         *
         //@return number of used sids, 0 if none.
         */
        public int usedDevices() { return (int)sidobjs.size(); }

        /**
         //Available devices.
         *
         //@return the number of available sids, 0 = endless.
         */
        public int availDevices() { return 0; }

        /**
         //Create the Sid emu.
         *
         //@param sids the number of required Sid emu
         //@return the number of actually created Sid emus
         */
        public int create(int sids) { return 0; }

        /**
         //Find a free SID of the required specs
         *
         //@param env the event context
         //@param model the required Sid model
         //@return pointer to the locked Sid emu
         */
        //public sidemu lock_(EventScheduler scheduler, sid_model_t model) { return null; }

        /**
         //Release this SID.
         *
         //@param device the Sid emu to unlock
         */
        //public void unlock(sidemu device) { }

        /**
         //Remove all SID emulations.
         */
        //public void remove() { }

        /**
         //Get the builder's name.
         *
         //@return the name
         */
        public String name() { return m_name; }

        /**
         //Error message.
         *
         //@return String error message.
         */
        public String error() { return m_errorBuffer; }

        /**
         //Determine current state of Object.
         *
         //@return true = okay, false = error
         */
        public Boolean getStatus() { return m_status; }

        /**
         //Get the builder's credits.
         *
         //@return credits
         */
        public String credits() { return null; }

        /**
         //Toggle Sid filter emulation.
         *
         //@param enable true = enable, false = disable
         */
        public void filter(Boolean enable) { }




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

        //# include "SidBuilder.h"
        //# include <algorithm>
        //# include "sidemu.h"
        //# include "sidcxx11.h"

        public sidemu lock_(EventScheduler scheduler, SidConfig.sid_model_t model)
        {
            m_status = true;

            for (sidemu it : sidobjs)
            {
                sidemu sid = it;
                if (sid.lock_(scheduler))
                {
                    sid.model(model);
                    return sid;
                }
            }

            // Unable to locate free SID
            m_status = false;
            m_errorBuffer = name() + " ERROR: No available SIDs to lock";
            return null;
        }

        public void unlock(sidemu device)
        {
            sidemu oldSe = null;
            for(sidemu se : sidobjs)
            {
                if (oldSe != se)
                {
                    se.unlock();
                }
                oldSe = se;
            }
            //libsidplayfp.sidemu it = sidobjs[device];
            //if (it != sidobjs[sidobjs.size() - 1])
            //{
            //    it.unlock();
            //}
        }

        //template<class T>
        public <T> void Delete(T s) { s = T; }

        public void remove()
        {
            //for (libsidplayfp.sidemu it : sidobjs)
            //{
                //Delete<emuset_t>(it);
            //}
            //sidobjs.clear();
            sidobjs.clear();
        }

    }
