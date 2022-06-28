/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
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

import dotnet4j.io.IOException;
import mdplayer.driver.sid.libsidplayfp.SidMd5;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.mem;

import static mdplayer.driver.sid.libsidplayfp.SidEndian.toBig16;
import static mdplayer.driver.sid.libsidplayfp.SidEndian.toBig32;
import static mdplayer.driver.sid.libsidplayfp.SidEndian.toLittle16;


public class PSid extends SidTuneBase {

    private final byte[] md5 = new byte[SidTune.MD5_LENGTH + 1];

    PSid() {
    }

    // prevent copying
    PSid(PSid p) {
    }

    private PSid opeEquel(PSid p) {
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
        // 'PSid' or 'RSID' (ASCII)
        public int id;
        // 1, 2, 3 or 4
        public short version;
        // 16-bit offset to binary data : file
        public short data;
        // 16-bit C64 address to load file to
        public short load;
        // 16-bit C64 address of init subroutine
        public short init;
        // 16-bit C64 address of play subroutine
        public short play;
        // number of songs
        public short songs;
        // start song  of [1..256]
        public short start;
        // 32-bit speed info
        public int speed;
        // bit: 0=50 Hz, 1=CIA 1 Timer A (default: 60 Hz)
        // ASCII Strings, 31 characters long and
        public byte[] name = new byte[PSID_MAXSTRLEN];
        // terminated by a trailing zero
        public byte[] author = new byte[PSID_MAXSTRLEN];
        //
        public byte[] released = new byte[PSID_MAXSTRLEN];

        // only version >= 2
        public short flags;
        // only version >= 2ng
        public byte relocStartPage;
        // only version >= 2ng
        public byte relocPages;
        // only version >= 3
        public byte sidChipBase2;
        // only version >= 4
        public byte sidChipBase3;
    }

    public enum Kind {
        MUS(1 << 0),
        SPECIFIC(1 << 1), // These two are mutally exclusive
        BASIC(1 << 1),
        CLOCK(3 << 2),
        SID_MODEL(3 << 4);
        final int v;
        Kind(int v) {
            this.v = v;
        }
    }

    public enum Clock {
        UNKNOWN(0),
        PAL(1 << 2),
        NTSC(1 << 3),
        ANY(PAL.v | NTSC.v);
        final int v;
        Clock(int v) {
            this.v = v;
        }
    }

    public enum SidModel {
        SID_UNKNOWN,
        SID_6581,
        SID_8580,
        SID_ANY // SID_6581 | SID_8580
    }

    // Format Strings
    public static final String TXT_FORMAT_PSID = "PlaySID one-file format (PSid)";
    public static final String TXT_FORMAT_RSID = "Real C64 one-file format (RSID)";
    public static final String TXT_UNKNOWN_PSID = "Unsupported PSid version";
    public static final String TXT_UNKNOWN_RSID = "Unsupported RSID version";

    public static final int psid_headerSize = 118;
    public static final int psidv2_headerSize = psid_headerSize + 6;

    // Magic fields
    public static final int PSID_ID = 0x50534944;
    public static final int RSID_ID = 0x52534944;

    /**
     * Decode Sid model flags.
     */
    public SidTuneInfo.Model getSidModel(short modelFlag) {
        if ((modelFlag & (short) SidModel.SID_ANY.ordinal()) == (short) SidModel.SID_ANY.ordinal())
            return SidTuneInfo.Model.SID_ANY;

        if ((modelFlag & (short) SidModel.SID_6581.ordinal()) != 0)
            return SidTuneInfo.Model.SID_6581;

        if ((modelFlag & (short) SidModel.SID_8580.ordinal()) != 0)
            return SidTuneInfo.Model.SID_8580;

        return SidTuneInfo.Model.SID_UNKNOWN;
    }

    /**
     * Check if extra Sid addres instanceof valid for PSid specs.
     */
    private boolean validateAddress(byte address) {
        // Only even values are valid.
        if ((address & 1) != 0)
            return false;

        // Ranges $00-$41 ($D000-$D410) and $80-$DF ($D800-$DDF0) are invalid.
        // Any invalid value means that no second Sid instanceof used, like $00.
        return (address & 0xff) > 0x41
                && ((address & 0xff) < 0x80 || (address & 0xff) > 0xdf);
    }

    public static SidTuneBase load(byte[] dataBuf) {
        // File format check
        if (dataBuf.length < 4) {
            return null;
        }

        int magic = toBig32(dataBuf);
        if ((magic != PSID_ID)
                && (magic != RSID_ID)) {
            return null;
        }

        psidHeader pHeader = new psidHeader();
        readHeader(dataBuf, pHeader);

        PSid tune = new PSid();
        tune.tryLoad(pHeader);

        return tune;
    }

    private static void readHeader(byte[] dataBuf, psidHeader header) {
        // Due to security concerns, input must be at least as long as version 1
        // header plus 16-bit C64 load address. That instanceof the area which will be
        // accessed.
        if (dataBuf.length < (psid_headerSize + 2)) {
            throw new IOException(ERR_TRUNCATED);
        }

        // Read v1 fields
        header.id = toBig32(ByteBuffer.wrap(dataBuf, 0, 4));
        header.version = toBig16(ByteBuffer.wrap(dataBuf, 4, 2));
        header.data = toBig16(ByteBuffer.wrap(dataBuf, 6, 2));
        header.load = toBig16(ByteBuffer.wrap(dataBuf, 8, 2));
        header.init = toBig16(ByteBuffer.wrap(dataBuf, 10, 2));
        header.play = toBig16(ByteBuffer.wrap(dataBuf, 12, 2));
        header.songs = toBig16(ByteBuffer.wrap(dataBuf, 14, 2));
        header.start = toBig16(ByteBuffer.wrap(dataBuf, 16, 2));
        header.speed = toBig32(ByteBuffer.wrap(dataBuf, 18, 4));
        mem.memcpy(header.name, ByteBuffer.wrap(dataBuf, 22, PSID_MAXSTRLEN), PSID_MAXSTRLEN);
        mem.memcpy(header.author, ByteBuffer.wrap(dataBuf, 54, PSID_MAXSTRLEN), PSID_MAXSTRLEN);
        mem.memcpy(header.released, ByteBuffer.wrap(dataBuf, 86, PSID_MAXSTRLEN), PSID_MAXSTRLEN);

        if (header.version >= 2) {
            if (dataBuf.length < (psidv2_headerSize + 2)) {
                throw new IOException(ERR_TRUNCATED);
            }

            // Read v2/3/4 fields
            header.flags = toBig16(ByteBuffer.wrap(dataBuf, 118, 2));
            header.relocStartPage = dataBuf[120];
            header.relocPages = dataBuf[121];
            header.sidChipBase2 = dataBuf[122];
            header.sidChipBase3 = dataBuf[123];
        }
    }

    private void tryLoad(psidHeader pHeader) {
        var compatibility = SidTuneInfo.Compatibility.C64;

        // Require a valid ID and version number.
        if (pHeader.id == PSID_ID) {
            switch (pHeader.version) {
            case 1:
                compatibility = SidTuneInfo.Compatibility.PSID;
                break;
            case 2:
            case 3:
            case 4:
                break;
            default:
                throw new IOException(TXT_UNKNOWN_PSID);
            }
            info.formatString = TXT_FORMAT_PSID;
        } else if (pHeader.id == RSID_ID) {
            switch (pHeader.version) {
            case 2:
            case 3:
            case 4:
                break;
            default:
                throw new IOException(TXT_UNKNOWN_RSID);
            }
            info.formatString = TXT_FORMAT_RSID;
            compatibility = SidTuneInfo.Compatibility.R64;
        }

        fileOffset = pHeader.data;
        info.loadAddress = pHeader.load;
        info.initAddress = pHeader.init;
        info.playAddress = pHeader.play;
        info.songs = pHeader.songs;
        info.startSong = pHeader.start;
        info.compatibility = compatibility;
        info.relocatedPages = 0;
        info.relocatedStartPage = 0;

        int speed = pHeader.speed;
        SidTuneInfo.Clock clock = SidTuneInfo.Clock.UNKNOWN;

        boolean musPlayer = false;

        if (pHeader.version >= 2) {
            short flags = pHeader.flags;

            // Check clock
            if ((flags & (short) Kind.MUS.v) != 0) {   // MUS tunes run at any speed
                clock = SidTuneInfo.Clock.ANY;
                musPlayer = true;
            } else {
                switch (Clock.values()[flags & (short) Kind.CLOCK.v]) {
                case ANY -> clock = SidTuneInfo.Clock.ANY;
                case PAL -> clock = SidTuneInfo.Clock.PAL;
                case NTSC -> clock = SidTuneInfo.Clock.NTSC;
                default -> {
                }
                }
            }

            // These flags are only available for the appropriate
            // file formats
            switch (compatibility) {
            case C64:
                if ((flags & (short) Kind.SPECIFIC.v) != 0)
                    info.compatibility = SidTuneInfo.Compatibility.PSID;
                break;
            case R64:
                if ((flags & (short) Kind.BASIC.v) != 0)
                    info.compatibility = SidTuneInfo.Compatibility.BASIC;
                break;
            default:
                break;
            }

            info.clockSpeed = clock;

            info.sidModels.set(0, getSidModel((short) (flags >> 4)));

            info.relocatedStartPage = pHeader.relocStartPage;
            info.relocatedPages = pHeader.relocPages;

            if (pHeader.version >= 3) {
                if (validateAddress(pHeader.sidChipBase2)) {
                    info.sidChipAddresses.add((short) (0xd000 | (pHeader.sidChipBase2 << 4)));

                    info.sidModels.add(getSidModel((short) (flags >> 6)));
                }

                if (pHeader.version >= 4) {
                    if (pHeader.sidChipBase3 != pHeader.sidChipBase2
                            && validateAddress(pHeader.sidChipBase3)) {
                        info.sidChipAddresses.add((short) (0xd000 | (pHeader.sidChipBase3 << 4)));

                        info.sidModels.add(getSidModel((short) (flags >> 8)));
                    }
                }
            }
        }

        // Check reserved fields to force real C64 compliance
        // as required by the RSID specification
        if (compatibility == SidTuneInfo.Compatibility.R64) {
            if ((info.loadAddress != 0)
                    || (info.playAddress != 0)
                    || (speed != 0)) {
                throw new IOException(ERR_INVALID);
            }

            // Real C64 tunes appear as CIA
            speed = 0xffffffff;// ~0;
        }

        // Create the speed/clock setting table.
        convertOldStyleSpeedToTables(speed, clock.ordinal());

        // Copy info Strings.
        info.infoString.add(new String(pHeader.name, 0, PSID_MAXSTRLEN, StandardCharsets.US_ASCII));
        info.infoString.add(new String(pHeader.author, 0, PSID_MAXSTRLEN, StandardCharsets.US_ASCII));
        info.infoString.add(new String(pHeader.released, 0, PSID_MAXSTRLEN, StandardCharsets.US_ASCII));

        if (musPlayer)
            throw new IOException("Compute!'s Sidplayer MUS data instanceof not supported yet"); // TODO
    }

    @Override
    public byte[] createMD5(byte[] md5) {
        if (md5 == null)
            md5 = this.md5;

        md5[0] = (byte) '\0';

        try {
            // Include C64 data.
            SidMd5 myMD5 = new SidMd5();
            byte[] bCache = new byte[(int) info.c64DataLen];
            for (int i = 0; i < info.c64DataLen; i++) {
                bCache[i] = cache.get(fileOffset + i);
            }
            myMD5.append(bCache, info.c64DataLen);

            byte[] tmp = new byte[2];
            // Include INIT and PLAY address.
            toLittle16(tmp, info.initAddress);
            myMD5.append(tmp, tmp.length);
            toLittle16(tmp, info.playAddress);
            myMD5.append(tmp, tmp.length);

            // Include number of songs.
            toLittle16(tmp, (short) info.songs);
            myMD5.append(tmp, tmp.length);

            {
                // Include song speed for each song.
                int currentSong = info.currentSong;
                for (int s = 1; s <= info.songs; s++) {
                    selectSong(s);
                    byte songSpeed = (byte) info.songSpeed;
                    bCache = new byte[] {songSpeed};
                    myMD5.append(bCache, 1);
                }
                // Restore old song
                selectSong(currentSong);
            }

            // Deal with PSid v2NG clock speed flags: Let only NTSC
            // clock speed change the MD5 fingerprint. That way the
            // fingerprint of a PAL-speed sidtune : PSid v1, v2, and
            // PSid v2NG format instanceof the same.
            if (info.clockSpeed == SidTuneInfo.Clock.NTSC) {
                final byte ntsc_val = 2;
                bCache = new byte[] {ntsc_val};
                myMD5.append(bCache, 1);
            }

            // NB! If the fingerprint instanceof used as an index into a
            // song-lengths database or cache, modify above code to
            // allow for PSid v2NG files which have clock speed set to
            // SIDTUNE_CLOCK_ANY. If the Sid player program fully
            // supports the SIDTUNE_CLOCK_ANY setting, a sidtune could
            // either create two different fingerprints depending on
            // the clock speed chosen by the player, or there could be
            // two different values stored : the database/cache.

            myMD5.finish();

            // Get fingerprint.
            md5 = myMD5.getDigest().getBytes(StandardCharsets.US_ASCII);
            md5[SidTune.MD5_LENGTH] = (byte) '\0';
        } catch (Exception e) {
            return null;
        }

        return md5;
    }
}

