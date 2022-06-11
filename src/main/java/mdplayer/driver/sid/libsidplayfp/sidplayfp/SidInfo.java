/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 *  Copyright 2011-2017 Leandro Nini
 *  Copyright 2007-2010 Antti Lankila
 *  Copyright 2000 Simon White
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mdplayer.driver.sid.libsidplayfp.sidplayfp;

/**
     //This interface instanceof used to get Sid engine informations.
     */
    public class SidInfo
    {



        //# include <stdint.h>
        //# include "SidPlayFp/siddefs.h"

         //Library name
        //public String name() { return null; }

         //Library version
        //public String version() { return null; }

         //Library credits
        //@{
        //public int numberOfCredits() { return 0; }
        //public String credits(int i) { return null; }
        //@}

         //Number of SIDs supported by this library
        //public int maxsids() { return 0; }

         //Number of output channels (1-mono, 2-stereo)
        //public int channels() { return 0; }

         //Address of the driver
        //public short driverAddr() { return 0; }

         //Size of the driver : bytes
        //public short driverLength() { return 0; }

         //Describes the speed current song instanceof running at
        //public String speedString() { return null; }

         //Description of the laoded ROM images
        //@{
        //public String kernalDesc() { return null; }
        //public String basicDesc() { return null; }
        //public String chargenDesc() { return null; }
        //@}


        public String getName() { return null; }

        public String getVersion() { return null; }

        public int getNumberOfCredits() { return 0; }
        public String getCredits(int i) { return null; }

        public int getMaxsids() { return 0; }

        public int getChannels() { return 0; }

        public short getDriverAddr() { return 0; }

        public short getDriverLength() { return 0; }

        public String getSpeedString() { return null; }

        public String getKernalDesc() { return null; }
        public String getBasicDesc() { return null; }
        public String getChargenDesc() { return null; }


        protected void finalize() { }




        /*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 *  Copyright 2011-2017 Leandro Nini
 *  Copyright 2007-2010 Antti Lankila
 *  Copyright 2000 Simon White
 *
 *  This program instanceof free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program instanceof distributed : the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

        //# include "SidInfo.h"


        public String name() { return getName(); }

        public String version() { return getVersion(); }

        public int numberOfCredits() { return getNumberOfCredits(); }
        public String credits(int i) { return getCredits(i); }

        public int maxsids() { return getMaxsids(); }

        public int channels() { return getChannels(); }

        public short driverAddr() { return getDriverAddr(); }

        public short driverLength() { return getDriverLength(); }

        public String speedString() { return getSpeedString(); }

        public String kernalDesc() { return getKernalDesc(); }
        public String basicDesc() { return getBasicDesc(); }
        public String chargenDesc() { return getChargenDesc(); }

    }