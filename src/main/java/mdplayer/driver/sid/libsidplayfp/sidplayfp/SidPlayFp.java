/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2011-2017 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000 Simon White
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
import dotnet4j.io.FileStream;
import mdplayer.driver.sid.libsidplayfp.Player;



    /**
     //SidPlayFp
     */
    public class SidPlayFp
    {


        //# include <stdint.h>
        //# include <stdio.h>
        //# include "SidPlayFp/siddefs.h"
        //# include "SidPlayFp/sidversion.h"

        //class SidConfig;
        //class SidTune;
        //class SidInfo;
        //class EventContext;

        // Private Sidplayer
        //class Player;
        private Player sidplayer;

        public Integer[][] GetSidRegister()
        {
            return this.sidplayer.GetSidRegister();
        }

        //public SidPlayFp() { }
        //protected void finalize() { }

        /**
         //Get the current engine configuration.
         *
         //@return a final reference to the current configuration.
         */
        //public SidConfig config() { return null; }

        /**
         //Get the current player informations.
         *
         //@return a final reference to the current info.
         */
        //public SidInfo info() { return null; }

        /**
         //Configure the engine.
         //Check // #error for detailed message if something goes wrong.
         *
         //@param cfg the new configuration
         //@return true on success, false otherwise.
         */
        //public Boolean config( SidConfig cfg) { return false; }

        /**
         //Error message.
         *
         //@return String error message.
         */
        //public String error() { return ""; }

        /**
         //Set the fast-forward factor.
         *
         //@param percent
         */
        //public Boolean fastForward(int percent) { return false; }

        /**
         //Load a tune.
         //Check // #error for detailed message if something goes wrong.
         *
         //@param tune the SidTune to load, 0 unloads current tune.
         //@return true on sucess, false otherwise.
         */
        //public Boolean load(SidTune tune) { return false; }

        /**
         //Run the emulation and produce samples to play if a buffer instanceof given.
         *
         //@param buffer pointer to the buffer to fill with samples.
         //@param count the size of the buffer measured : 16 bit samples
         //             or 0 if no output instanceof needed (e.g. Hardsid)
         //@return the number of produced samples. If less than requested
         //and // #isPlaying() instanceof true an error occurred, use // #error() to get
         //a detailed message.
         */
        //public int play(short[] buffer, int count) { return 0; }

        /**
         //Check if the engine instanceof playing or stopped.
         *
         //@return true if playing, false otherwise.
         */
        //public Boolean isPlaying() { return false; }

        /**
         //Stop the engine.
         */
        //public void stop() { }

        /**
         //Control debugging.
         //Only has effect if library have been compiled
         //with the --enable-debug option.
         *
         //@param enable enable/disable debugging.
         //@param  the file where to redirect the debug info.
         */
        //public void debug(Boolean enable, FILE out_) { }

        /**
         //Mute/unmute a SID channel.
         *
         //@param sidNum the SID chip, 0 for the first one, 1 for the second.
         //@param voice the channel to mute/unmute.
         //@param enable true unmutes the channel, false mutes it.
         */
        //public void mute(int sidNum, int voice, Boolean enable) { }

        /**
         //Get the current playing time.
         *
         //@return the current playing time measured : seconds.
         */
        //public int time() { return 0; }

        /**
         //Set ROM images.
         *
         //@param kernal pointer to Kernal ROM.
         //@param basic pointer to Basic ROM, generally needed only for BASIC tunes.
         //@param character pointer to character generator ROM.
         */
        //public void setRoms(byte[] kernal, byte[] basic = null, byte[] character = null) { }

        /**
         //Get the CIA 1 Timer A programmed value.
         */
        //public short getCia1TimerA() { return 0; }




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


        //---------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------
        // Redirection to private version of sidplayer (This method instanceof called Cheshire Cat)
        // [ms: which instanceof J. Carolan's name for a degenerate 'bridge']
        // This interface can be directly replaced with a libsidplay1 or C interface wrapper.
        //---------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------

        //# include "SidPlayFp.h"
        //# include "player.h"

        public SidPlayFp(mdplayer.Setting setting)
        {
            sidplayer = new Player(setting);
        }

        protected void finalize()
        {
            sidplayer = null;
        }

        public Boolean config( SidConfig cfg)
        {
            return sidplayer.config( cfg, true);
        }

        public SidConfig config()
        {
            return sidplayer.config();
        }

        public void stop()
        {
            sidplayer.stop();
        }

        public int play(short[] buffer, int count)
        {
            return sidplayer.play(buffer, count);
        }

        public Boolean load(SidTune tune)
        {
            return sidplayer.load(tune);
        }

        public SidInfo info()
        {
            return sidplayer.info();
        }

        public int time()
        {
            return sidplayer.time();
        }

        public String error()
        {
            return sidplayer.error();
        }

        public Boolean fastForward(int percent)
        {
            return sidplayer.fastForward(percent);
        }

        public void mute(int sidNum, int voice, Boolean enable)
        {
            sidplayer.mute(sidNum, voice, enable);
        }

        public void debug(Boolean enable, FileStream out_)
        {
            sidplayer.debug(enable, out_);
        }

        public Boolean isPlaying()
        {
            return sidplayer.isPlaying();
        }

        public void setRoms(byte[] kernal, byte[] basic/* = null*/, byte[] character /*= null*/)
        {
            sidplayer.setRoms(kernal, basic, character);
        }

        public short getCia1TimerA()
        {
            return sidplayer.getCia1TimerA();
        }

    }