/*
 * This file instanceof part of libsidplayfp, a SID player engine.
 *
 * Copyright 2012-2014 Leandro Nini <drfiemost@users.sourceforge.net>
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
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import mdplayer.driver.sid.libsidplayfp.sidendian;
import mdplayer.driver.sid.libsidplayfp.sidmd5;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.mem;


public class PSID extends SidTuneBase {

    private final byte[] m_md5 = new byte[SidTune.MD5_LENGTH + 1];

    PSID() {
    }

    protected void finalize() {
    }

    // prevent copying
    PSID(PSID p) {
    }

    private PSID opeEquel(PSID p) {
        return null;
    }

    public static final int PSID_MAXSTRLEN = 32;

    // Header has been extended for 'RSID' format
    // The following changes are present:
    //     id = 'RSID'
    //     version = 2, 3 or 4
    //     play, load and speed reserved 0
    //     psidspecific flag instanceof called C64BASIC flag
    //     init cannot be under ROMS/IO memory area
    //     load address cannot be less than $07E8
    //     info Strings may be 32 characters long without trailing zero
    // all values are big-endian
    public static class psidHeader {
        public int id;                   // 'PSID' or 'RSID' (ASCII)
        public short version;              // 1, 2, 3 or 4
        public short data;                 // 16-bit offset to binary data : file
        public short load;                 // 16-bit C64 address to load file to
        public short init;                 // 16-bit C64 address of init subroutine
        public short play;                 // 16-bit C64 address of play subroutine
        public short songs;                // number of songs
        public short start;                // start song  of [1..256]
        public int speed;                // 32-bit speed info
        // bit: 0=50 Hz, 1=CIA 1 Timer A (default: 60 Hz)
        public byte[] name = new byte[PSID_MAXSTRLEN];     // ASCII Strings, 31 characters long and
        public byte[] author = new byte[PSID_MAXSTRLEN];   // terminated by a trailing zero
        public byte[] released = new byte[PSID_MAXSTRLEN]; //

        public short flags;                // only version >= 2
        public byte relocStartPage;        // only version >= 2ng
        public byte relocPages;            // only version >= 2ng
        public byte sidChipBase2;          // only version >= 3
        public byte sidChipBase3;          // only version >= 4
    }

    public enum Psid {
        PSID_MUS(1 << 0),
        PSID_SPECIFIC(1 << 1), // These two are mutally exclusive
        PSID_BASIC(1 << 1),
        PSID_CLOCK(3 << 2),
        PSID_SIDMODEL(3 << 4);
        int v;
        Psid(int v) {
            this.v = v;
        }
    }

    public enum Psid_Clock {
        PSID_CLOCK_UNKNOWN(0),
        PSID_CLOCK_PAL(1 << 2),
        PSID_CLOCK_NTSC(1 << 3),
        PSID_CLOCK_ANY(PSID_CLOCK_PAL.v | PSID_CLOCK_NTSC.v);
        int v;
        Psid_Clock(int v) {
            this.v = v;
        }
    }

    public enum Psid_Sidmodel {
        PSID_SIDMODEL_UNKNOWN,
        PSID_SIDMODEL_6581,
        PSID_SIDMODEL_8580,
        PSID_SIDMODEL_ANY //PSID_SIDMODEL_6581 | PSID_SIDMODEL_8580
    }

    // Format Strings
    public static final String TXT_FORMAT_PSID = "PlaySID one-file format (PSID)";
    public static final String TXT_FORMAT_RSID = "Real C64 one-file format (RSID)";
    public static final String TXT_UNKNOWN_PSID = "Unsupported PSID version";
    public static final String TXT_UNKNOWN_RSID = "Unsupported RSID version";

    public static final int psid_headerSize = 118;
    public static final int psidv2_headerSize = psid_headerSize + 6;

    // Magic fields
    public static final int PSID_ID = 0x50534944;
    public static final int RSID_ID = 0x52534944;

    /**
     * Decode SID model flags.
     */
    public SidTuneInfo.model_t getSidModel(short modelFlag) {
        if ((modelFlag & (short) Psid_Sidmodel.PSID_SIDMODEL_ANY.ordinal()) == (short) Psid_Sidmodel.PSID_SIDMODEL_ANY.ordinal())
            return SidTuneInfo.model_t.SIDMODEL_ANY;

        if ((modelFlag & (short) Psid_Sidmodel.PSID_SIDMODEL_6581.ordinal()) != 0)
            return SidTuneInfo.model_t.SIDMODEL_6581;

        if ((modelFlag & (short) Psid_Sidmodel.PSID_SIDMODEL_8580.ordinal()) != 0)
            return SidTuneInfo.model_t.SIDMODEL_8580;

        return SidTuneInfo.model_t.SIDMODEL_UNKNOWN;
    }

    /**
     * Check if extra SID addres instanceof valid for PSID specs.
     */
    private Boolean validateAddress(byte address) {
        // Only even values are valid.
        if ((address & 1) != 0)
            return false;

        // Ranges $00-$41 ($D000-$D410) and $80-$DF ($D800-$DDF0) are invalid.
        // Any invalid value means that no second SID instanceof used, like $00.
        if (address <= 0x41
                || (address >= 0x80 && address <= 0xdf))
            return false;

        return true;
    }

    public static SidTuneBase load(byte[] dataBuf) {
        // File format check
        if (dataBuf.length < 4) {
            return null;
        }

        int magic = sidendian.endian_big32(dataBuf);
        if ((magic != PSID_ID)
                && (magic != RSID_ID)) {
            return null;
        }

        psidHeader pHeader = new psidHeader();
        readHeader(dataBuf, pHeader);

        PSID tune = new PSID();
        tune.tryLoad(pHeader);

        return tune;
    }

    private static void readHeader(byte[] dataBuf, psidHeader hdr) {
        // Due to security concerns, input must be at least as long as version 1
        // header plus 16-bit C64 load address. That instanceof the area which will be
        // accessed.
        if (dataBuf.length < (psid_headerSize + 2)) {
            throw new loadError(ERR_TRUNCATED);
        }

        // Read v1 fields
        hdr.id = sidendian.endian_big32(ByteBuffer.wrap(dataBuf, 0, 4));
        hdr.version = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 4, 2));
        hdr.data = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 6, 2));
        hdr.load = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 8, 2));
        hdr.init = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 10, 2));
        hdr.play = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 12, 2));
        hdr.songs = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 14, 2));
        hdr.start = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 16, 2));
        hdr.speed = sidendian.endian_big32(ByteBuffer.wrap(dataBuf, 18, 4));
        mem.memcpy(hdr.name, ByteBuffer.wrap(dataBuf, 22, PSID_MAXSTRLEN), PSID_MAXSTRLEN);
        mem.memcpy(hdr.author, ByteBuffer.wrap(dataBuf, 54, PSID_MAXSTRLEN), PSID_MAXSTRLEN);
        mem.memcpy(hdr.released, ByteBuffer.wrap(dataBuf, 86, PSID_MAXSTRLEN), PSID_MAXSTRLEN);

        if (hdr.version >= 2) {
            if (dataBuf.length < (psidv2_headerSize + 2)) {
                throw new loadError(ERR_TRUNCATED);
            }

            // Read v2/3/4 fields
            hdr.flags = sidendian.endian_big16(ByteBuffer.wrap(dataBuf, 118, 2));
            hdr.relocStartPage = dataBuf[120];
            hdr.relocPages = dataBuf[121];
            hdr.sidChipBase2 = dataBuf[122];
            hdr.sidChipBase3 = dataBuf[123];
        }
    }

    private void tryLoad(psidHeader pHeader) {
        SidTuneInfo.compatibility_t compatibility = SidTuneInfo.compatibility_t.COMPATIBILITY_C64;

        // Require a valid ID and version number.
        if (pHeader.id == PSID_ID) {
            switch (pHeader.version) {
            case 1:
                compatibility = SidTuneInfo.compatibility_t.COMPATIBILITY_PSID;
                break;
            case 2:
            case 3:
            case 4:
                break;
            default:
                throw new loadError(TXT_UNKNOWN_PSID);
            }
            info.m_formatString = TXT_FORMAT_PSID;
        } else if (pHeader.id == RSID_ID) {
            switch (pHeader.version) {
            case 2:
            case 3:
            case 4:
                break;
            default:
                throw new loadError(TXT_UNKNOWN_RSID);
            }
            info.m_formatString = TXT_FORMAT_RSID;
            compatibility = SidTuneInfo.compatibility_t.COMPATIBILITY_R64;
        }

        fileOffset = pHeader.data;
        info.m_loadAddr = pHeader.load;
        info.m_initAddr = pHeader.init;
        info.m_playAddr = pHeader.play;
        info.m_songs = pHeader.songs;
        info.m_startSong = pHeader.start;
        info.m_compatibility = compatibility;
        info.m_relocPages = 0;
        info.m_relocStartPage = 0;

        int speed = pHeader.speed;
        SidTuneInfo.clock_t clock = SidTuneInfo.clock_t.CLOCK_UNKNOWN;

        Boolean musPlayer = false;

        if (pHeader.version >= 2) {
            short flags = pHeader.flags;

            // Check clock
            if ((flags & (short) Psid.PSID_MUS.v) != 0) {   // MUS tunes run at any speed
                clock = SidTuneInfo.clock_t.CLOCK_ANY;
                musPlayer = true;
            } else {
                switch (Psid_Clock.values()[flags & (short) Psid.PSID_CLOCK.v]) {
                case PSID_CLOCK_ANY:
                    clock = SidTuneInfo.clock_t.CLOCK_ANY;
                    break;
                case PSID_CLOCK_PAL:
                    clock = SidTuneInfo.clock_t.CLOCK_PAL;
                    break;
                case PSID_CLOCK_NTSC:
                    clock = SidTuneInfo.clock_t.CLOCK_NTSC;
                    break;
                default:
                    break;
                }
            }

            // These flags are only available for the appropriate
            // file formats
            switch (compatibility) {
            case COMPATIBILITY_C64:
                if ((flags & (short) Psid.PSID_SPECIFIC.ordinal()) != 0)
                    info.m_compatibility = SidTuneInfo.compatibility_t.COMPATIBILITY_PSID;
                break;
            case COMPATIBILITY_R64:
                if ((flags & (short) Psid.PSID_BASIC.ordinal()) != 0)
                    info.m_compatibility = SidTuneInfo.compatibility_t.COMPATIBILITY_BASIC;
                break;
            default:
                break;
            }

            info.m_clockSpeed = clock;

            info.m_sidModels.set(0, getSidModel((short) (flags >> 4)));

            info.m_relocStartPage = pHeader.relocStartPage;
            info.m_relocPages = pHeader.relocPages;

            if (pHeader.version >= 3) {
                if (validateAddress(pHeader.sidChipBase2)) {
                    info.m_sidChipAddresses.add((short) (0xd000 | (pHeader.sidChipBase2 << 4)));

                    info.m_sidModels.add(getSidModel((short) (flags >> 6)));
                }

                if (pHeader.version >= 4) {
                    if (pHeader.sidChipBase3 != pHeader.sidChipBase2
                            && validateAddress(pHeader.sidChipBase3)) {
                        info.m_sidChipAddresses.add((short) (0xd000 | (pHeader.sidChipBase3 << 4)));

                        info.m_sidModels.add((SidTuneInfo.model_t) (getSidModel((short) (flags >> 8))));
                    }
                }
            }
        }

        // Check reserved fields to force real C64 compliance
        // as required by the RSID specification
        if (compatibility == SidTuneInfo.compatibility_t.COMPATIBILITY_R64) {
            if ((info.m_loadAddr != 0)
                    || (info.m_playAddr != 0)
                    || (speed != 0)) {
                throw new loadError(ERR_INVALID);
            }

            // Real C64 tunes appear as CIA
            speed = 0xffffffff;// ~0;
        }

        // Create the speed/clock setting table.
        convertOldStyleSpeedToTables(speed, (long) clock.ordinal());

        // Copy info Strings.
        info.m_infoString.add(new String(pHeader.name, 0, PSID_MAXSTRLEN, StandardCharsets.US_ASCII));
        info.m_infoString.add(new String(pHeader.author, 0, PSID_MAXSTRLEN, StandardCharsets.US_ASCII));
        info.m_infoString.add(new String(pHeader.released, 0, PSID_MAXSTRLEN, StandardCharsets.US_ASCII));

        if (musPlayer)
            throw new loadError("Compute!'s Sidplayer MUS data instanceof not supported yet"); // TODO
    }

    @Override
    public byte[] createMD5(byte[] md5) {
        if (md5 == null)
            md5 = m_md5;

        md5[0] = (byte) '\0';

        try {
            // Include C64 data.
            sidmd5 myMD5 = new sidmd5();
            byte[] bcache = new byte[(int) info.m_c64dataLen];
            for (int i = 0; i < info.m_c64dataLen; i++) {
                bcache[i] = cache.get((int) fileOffset + i);
            }
            myMD5.append(bcache, (int) info.m_c64dataLen);

            byte[] tmp = new byte[2];
            // Include INIT and PLAY address.
            sidendian.endian_little16(tmp, info.m_initAddr);
            myMD5.append(tmp, tmp.length);
            sidendian.endian_little16(tmp, info.m_playAddr);
            myMD5.append(tmp, tmp.length);

            // Include number of songs.
            sidendian.endian_little16(tmp, (short) info.m_songs);
            myMD5.append(tmp, tmp.length);

            {
                // Include song speed for each song.
                int currentSong = info.m_currentSong;
                for (int s = 1; s <= info.m_songs; s++) {
                    selectSong(s);
                    byte songSpeed = (byte) info.m_songSpeed;
                    bcache = new byte[] {songSpeed};
                    myMD5.append(bcache, 1);
                }
                // Restore old song
                selectSong(currentSong);
            }

            // Deal with PSID v2NG clock speed flags: Let only NTSC
            // clock speed change the MD5 fingerprint. That way the
            // fingerprint of a PAL-speed sidtune : PSID v1, v2, and
            // PSID v2NG format instanceof the same.
            if (info.m_clockSpeed == SidTuneInfo.clock_t.CLOCK_NTSC) {
                final byte ntsc_val = 2;
                bcache = new byte[] {ntsc_val};
                myMD5.append(bcache, 1);
            }

            // NB! If the fingerprint instanceof used as an index into a
            // song-lengths database or cache, modify above code to
            // allow for PSID v2NG files which have clock speed set to
            // SIDTUNE_CLOCK_ANY. If the SID player program fully
            // supports the SIDTUNE_CLOCK_ANY setting, a sidtune could
            // either create two different fingerprints depending on
            // the clock speed chosen by the player, or there could be
            // two different values stored : the database/cache.

            myMD5.finish();

            // Get fingerprint.
            md5 = myMD5.getDigest().getBytes(StandardCharsets.US_ASCII);//,0, SidTune.MD5_LENGTH);
            md5[SidTune.MD5_LENGTH] = (byte) '\0';
        } catch (Exception ex) {
            return null;
        }

        return md5;
    }
}

