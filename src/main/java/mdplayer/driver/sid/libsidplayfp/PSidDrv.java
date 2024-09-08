/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2012-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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

package mdplayer.driver.sid.libsidplayfp;

import java.nio.ByteBuffer;

import mdplayer.driver.sid.libsidplayfp.c64.cpu.Mos6510;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;


/**
 * The code here instanceof use to support the PSid Version 2NG
 * (proposal B) file format for player relocation support.
 */
public class PSidDrv {

    private SidTuneInfo tuneInfo;
    private String errorString;

    private byte[] relocationDriver;
    private int relocationSize;

    private short driverAddress;
    private short driverLength;

    public PSidDrv(SidTuneInfo tuneInfo) {
        this.tuneInfo = tuneInfo;
    }

    /**
     * Get a detailed error message.
     * <p>
     * @return a pointer to the String
     */
    public String errorString() {
        return errorString;
    }

    public short driverAddr() {
        return driverAddress;
    }

    public short driverLength() {
        return driverLength;
    }


    // Error Strings
    private static final String ERR_PSIDDRV_NO_SPACE = "ERROR: No space to install psid driver : C64 ram";
    private static final String ERR_PSIDDRV_RELOC = "ERROR: Failed whilst relocating psid driver";

    private static final byte[] psidDriver = new byte[] {
            0x01, 0x00, 0x6f, 0x36, 0x35, 0x00, 0x00, 0x00,
            0x00, 0x10, (byte) 0xcf, 0x00, 0x00, 0x04, 0x00, 0x00,
            0x00, 0x40, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x1b, 0x10, (byte) 0xc5, 0x10, (byte) 0xce,
            0x10, (byte) 0xce, 0x10, (byte) 0x8c, 0x10, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x6c, 0x0e, 0x10, 0x6c, 0x0c, 0x10, 0x78, (byte) 0xa9,
            0x00, (byte) 0x8d, 0x1a, (byte) 0xd0, (byte) 0xad, 0x19, (byte) 0xd0, (byte) 0x8d,
            0x19, (byte) 0xd0, (byte) 0xa9, 0x7f, (byte) 0x8d, 0x0d, (byte) 0xdc, (byte) 0x8d,
            0x0d, (byte) 0xdd, (byte) 0xad, 0x0d, (byte) 0xdc, (byte) 0xad, 0x0d, (byte) 0xdd,
            (byte) 0xa9, 0x0f, (byte) 0x8d, 0x18, (byte) 0xd4, (byte) 0xad, 0x12, 0x10,
            (byte) 0xf0, 0x07, (byte) 0xa2, 0x25, (byte) 0xa0, 0x40, 0x4c, 0x4a,
            0x10, (byte) 0xa2, (byte) 0x95, (byte) 0xa0, 0x42, (byte) 0x8e, 0x04, (byte) 0xdc,
            (byte) 0x8c, 0x05, (byte) 0xdc, (byte) 0xa2, (byte) 0x9b, (byte) 0xa0, 0x37, 0x4d,
            0x13, 0x10, 0x0d, 0x10, 0x10, (byte) 0xf0, 0x04, (byte) 0xa2,
            0x1b, (byte) 0xa0, 0x00, (byte) 0x8e, 0x11, (byte) 0xd0, (byte) 0x8c, 0x12,
            (byte) 0xd0, (byte) 0xad, 0x10, 0x10, (byte) 0xf0, 0x0a, (byte) 0xad, 0x11,
            0x10, (byte) 0xf0, 0x05, (byte) 0xa2, (byte) 0xb2, (byte) 0x8e, 0x14, 0x03,
            (byte) 0xad, 0x0b, 0x10, (byte) 0xd0, 0x08, (byte) 0xa9, (byte) 0x81, (byte) 0x8d,
            0x1a, (byte) 0xd0, 0x4c, (byte) 0x8c, 0x10, (byte) 0xa9, (byte) 0x81, (byte) 0xa2,
            0x01, (byte) 0x8d, 0x0d, (byte) 0xdc, (byte) 0x8e, 0x0e, (byte) 0xdc, (byte) 0xad,
            0x10, 0x10, (byte) 0xd0, 0x02, (byte) 0xa9, 0x37, (byte) 0x85, 0x01,
            (byte) 0xad, 0x14, 0x10, 0x48, (byte) 0xad, 0x0a, 0x10, 0x28,
            0x20, 0x18, 0x10, (byte) 0xad, 0x10, 0x10, (byte) 0xf0, 0x0a,
            (byte) 0xad, 0x11, 0x10, (byte) 0xf0, 0x04, (byte) 0xa9, 0x37, (byte) 0x85,
            0x01, 0x58, 0x4c, (byte) 0xaf, 0x10, (byte) 0xa5, 0x01, 0x48,
            (byte) 0xad, 0x11, 0x10, (byte) 0x85, 0x01, (byte) 0xa9, 0x00, 0x20,
            0x15, 0x10, 0x68, (byte) 0x85, 0x01, (byte) 0xce, 0x19, (byte) 0xd0,
            (byte) 0xad, 0x0d, (byte) 0xdc, 0x68, (byte) 0xa8, 0x68, (byte) 0xaa, 0x68,
            0x40, 0x02, 0x00, 0x00, 0x01, (byte) 0x82, 0x02, (byte) 0x82,
            0x02, (byte) 0x82, 0x02, (byte) 0x82, 0x02, (byte) 0x82, 0x0e, (byte) 0x82,
            0x03, (byte) 0x82, 0x22, (byte) 0x82, 0x09, (byte) 0x82, 0x11, (byte) 0x82,
            0x03, (byte) 0x82, 0x0f, (byte) 0x82, 0x05, (byte) 0x82, 0x05, 0x22,
            0x05, (byte) 0x82, 0x0a, (byte) 0x82, 0x0d, (byte) 0x82, 0x09, (byte) 0x82,
            0x04, (byte) 0x82, 0x04, (byte) 0x82, 0x03, (byte) 0x82, 0x05, (byte) 0x82,
            0x0a, (byte) 0x82, 0x06, (byte) 0x82, 0x07, (byte) 0x82, 0x00, 0x00,
            0x00, 0x00,
    };

    private static final byte[] POWER_ON = new byte[] {
            /* addr,   off,  rle, values */
            /*$0003*/ (byte) 0x83, 0x04, (byte) 0xaa, (byte) 0xb1, (byte) 0x91, (byte) 0xb3, 0x22,
            /*$000b*/ 0x03, 0x4c,
            /*$000f*/ 0x03, 0x04,
            /*$0016*/ (byte) 0x86, 0x05, 0x19, 0x16, 0x00, 0x0a, 0x76, (byte) 0xa3,
            /*$0022*/ (byte) 0x86, 0x03, 0x40, (byte) 0xa3, (byte) 0xb3, (byte) 0xbd,
            /*$002b*/ (byte) 0x85, 0x01, 0x01, 0x08,
            /*$0034*/ 0x07, (byte) 0xa0,
            /*$0038*/ 0x03, (byte) 0xa0,
            /*$003a*/ 0x01, (byte) 0xff,
            /*$0042*/ 0x07, 0x08,
            /*$0047*/ 0x04, 0x24,
            /*$0053*/ (byte) 0x8b, 0x01, 0x03, 0x4c,
            /*$0061*/ 0x0c, (byte) 0x8d,
            /*$0063*/ 0x02, 0x10,
            /*$0069*/ (byte) 0x84, 0x02, (byte) 0x8c, (byte) 0xff, (byte) 0xa0,
            /*$0071*/ (byte) 0x85, 0x1e, 0x0a, (byte) 0xa3, (byte) 0xe6, 0x7a, (byte) 0xd0, 0x02, (byte) 0xe6, 0x7b, (byte) 0xad, 0x00, 0x08, (byte) 0xc9, 0x3a, (byte) 0xb0, 0x0a, (byte) 0xc9, 0x20, (byte) 0xf0, (byte) 0xef, 0x38, (byte) 0xe9, 0x30, 0x38, (byte) 0xe9, (byte) 0xd0, 0x60, (byte) 0x80, 0x4f, (byte) 0xc7, 0x52, 0x58,
            /*$0091*/ 0x01, (byte) 0xff,
            /*$009a*/ 0x08, 0x03,
            /*$00b2*/ (byte) 0x97, 0x01, 0x3c, 0x03,
            /*$00c2*/ (byte) 0x8e, 0x03, (byte) 0xa0, 0x30, (byte) 0xfd, 0x01,
            /*$00c8*/ (byte) 0x82, (byte) 0x82, 0x03,
            /*$00cb*/ (byte) 0x80, (byte) 0x81, 0x01,
            /*$00ce*/ 0x01, 0x20,
            /*$00d1*/ (byte) 0x82, 0x01, 0x18, 0x05,
            /*$00d5*/ (byte) 0x82, 0x02, 0x27, 0x07, 0x0d,
            /*$00d9*/ (byte) 0x81, (byte) 0x86, (byte) 0x84,
            /*$00e0*/ (byte) 0x80, (byte) 0x85, (byte) 0x85,
            /*$00e6*/ (byte) 0x80, (byte) 0x86, (byte) 0x86,
            /*$00ed*/ (byte) 0x80, (byte) 0x85, (byte) 0x87,
            /*$00f3*/ (byte) 0x80, 0x03, 0x18, (byte) 0xd9, (byte) 0x81, (byte) 0xeb,
            /*$0176*/ 0x7f, 0x00,
            /*$01f6*/ 0x7f, 0x00,
            /*$0276*/ 0x7f, 0x00,
            /*$0282*/ (byte) 0x8b, 0x0a, 0x08, 0x00, (byte) 0xa0, 0x00, 0x0e, 0x00, 0x04, 0x0a, 0x00, 0x04, 0x10,
            /*$028f*/ (byte) 0x82, 0x01, 0x48, (byte) 0xeb,
            /*$0300*/ (byte) 0xef, 0x0b, (byte) 0x8b, (byte) 0xe3, (byte) 0x83, (byte) 0xa4, 0x7c, (byte) 0xa5, 0x1a, (byte) 0xa7, (byte) 0xe4, (byte) 0xa7, (byte) 0x86, (byte) 0xae,
            /*$0310*/ (byte) 0x84, 0x02, 0x4c, 0x48, (byte) 0xb2,
            /*$0314*/ (byte) 0x81, 0x1f, 0x31, (byte) 0xea, 0x66, (byte) 0xfe, 0x47, (byte) 0xfe, 0x4a, (byte) 0xf3, (byte) 0x91, (byte) 0xf2, 0x0e, (byte) 0xf2, 0x50, (byte) 0xf2, 0x33, (byte) 0xf3, 0x57, (byte) 0xf1, (byte) 0xca, (byte) 0xf1, (byte) 0xed, (byte) 0xf6, 0x3e, (byte) 0xf1, 0x2f, (byte) 0xf3, 0x66, (byte) 0xfe, (byte) 0xa5, (byte) 0xf4, (byte) 0xed, (byte) 0xf5,
            /*Total 217*/
    };

    /**
     * Copy : power on settings. These were created by running
     * the kernel reset routine and storing the useful values
     * from $0000-$03ff. FileFormat is:
     * - offset byte (bit 7 indicates presence rle byte)
     * - rle count byte (bit 7 indicates compression used)
     * - data (single byte) or quantity represented by uncompressed count
     * all counts and offsets are 1 less than they should be
     */
    private void copyPowerOnPattern(SidMemory mem) {
        int addr = 0;
        for (int i = 0; i < POWER_ON.length; ) {
            int off = POWER_ON[i++] & 0xff;
            int count = 0;
            boolean compressed = false;

            // Determine data count/compression
            if ((off & 0x80) != 0) {
                // fixup offset
                off &= 0x7f;
                count = POWER_ON[i++];
                if ((count & 0x80) != 0) {
                    // fixup count
                    count &= 0x7f;
                    compressed = true;
                }
            }

            // Fix count off by ones (see format details)
            count++;
            addr += off;

            if (compressed) {
                // Extract compressed data
                byte data = POWER_ON[i++];
                while (count-- > 0) {
                    mem.writeMemByte(addr++, data);
                }
            } else {
                // Extract uncompressed data
                while (count-- > 0) {
                    mem.writeMemByte(addr++, POWER_ON[i++]);
                }
            }
        }
    }

    /**
     * Get required I/O map to reach address
     * <p>
     * @param address a 16-bit effective address
     * @return a default bank-select value for $01
     */
    private byte iomap(short address) {
        // Force Real C64 Compatibility
        if (tuneInfo.compatibility() == SidTuneInfo.Compatibility.R64
                || tuneInfo.compatibility() == SidTuneInfo.Compatibility.BASIC
                || address == 0) {
            // Special case, set to 0x37 by the psid driver
            return 0;
        }

        // $34 for init/play : $d000 - $dfff
        // $35 for init/play : $e000 - $ffff
        // $36 for load end/play : $a000 - $ffff
        // $37 for the rest
        if ((address & 0xffff) < 0xa000)
            return 0x37;  // Basic-ROM, Kernal-ROM, I/O
        if ((address & 0xffff) < 0xd000)
            return 0x36;  // Kernal-ROM, I/O
        if ((address & 0xffff) >= 0xe000)
            return 0x35;  // I/O only

        return 0x34;  // RAM only
    }

    /**
     * Relocate the driver.
     * <p>
     * @return false if something's wrong, check //#errorString for error details
     */
    public boolean relocateDriver() {
        int startLp = tuneInfo.loadAddr() >> 8;
        int endLp = (tuneInfo.loadAddr() + (tuneInfo.c64dataLen() - 1)) >> 8;

        byte relocationStartPage = tuneInfo.relocStartPage();
        byte relocationPages = tuneInfo.relocPages();

        if (tuneInfo.compatibility() == SidTuneInfo.Compatibility.BASIC) {
            // The PSidDrv instanceof only used for initialisation and to
            // autorun basic tunes as running the kernel falls
            // into a manual load/run mode
            relocationStartPage = 0x04;
            relocationPages = 0x03;
        }

        // Check for free space : tune
        if (relocationStartPage == (byte) 0xff)
            relocationPages = 0;
            // Check if we need to find the reloc addr
        else if (relocationStartPage == 0) {
            relocationPages = 0;
            // find area where to dump the driver in.
            // It's only 1 block long, so any free block we can find
            // between $0400 and $d000 will do.
            for (int i = 4; i < 0xd0; i++) {
                if (i >= startLp && i <= endLp)
                    continue;

                if (i >= 0xa0 && i <= 0xbf)
                    continue;

                relocationStartPage = (byte) i;
                relocationPages = 1;
                break;
            }
        }

        if (relocationPages < 1) {
            errorString = ERR_PSIDDRV_NO_SPACE;
            return false;
        }

        // Place psid driver into ram
        short relocationAddress = (short) (relocationStartPage << 8);

        relocationDriver = psidDriver;
        relocationSize = psidDriver.length;

        O65Relocator relocator = new O65Relocator();
        relocator.setReloc(O65Relocator.Segment.TEXT, relocationAddress - 10);
        relocator.setExtract(O65Relocator.Segment.TEXT);
        if (!relocator.relocate(relocationDriver, relocationSize)) {
            errorString = ERR_PSIDDRV_RELOC;
            return false;
        }

        // Adjust size to not included initialisation data.
        relocationSize -= 10;

        driverAddress = relocationAddress;
        driverLength = (short) relocationSize;
        // Round length to end of page
        driverLength += 0xff;
        driverLength &= 0xff00;

        return true;
    }

    /**
     * Install the driver.
     * Must be called after the tune has been placed : memory.
     * <p>
     * @param mem the C64 memory interface
     * @param video the PAL/NTSC switch value, 0: NTSC, 1: PAL
     */
    public void install(SidMemory mem, byte video) {
        mem.fillRam((byte) 0, (byte) (0), 0x3ff);

        if (tuneInfo.compatibility().ordinal() >= SidTuneInfo.Compatibility.R64.ordinal()) {
            copyPowerOnPattern(mem);
        }

        // Set PAL/NTSC switch
        mem.writeMemByte((short) 0x02a6, video);

        mem.installResetHook(SidEndian.toLittle16(relocationDriver));

        // If not a basic tune then the PSidDrv must install
        // Interrupt hooks and trap programs trying to restart basic
        if (tuneInfo.compatibility() == SidTuneInfo.Compatibility.BASIC) {
            // Install hook to set subtune number for basic
            mem.setBasicSubtune((byte) (tuneInfo.currentSong() - 1));
            mem.installBasicTrap((short) 0xbf53);
        } else {
            // Only install irq handle for RSID tunes
            mem.fillRam((short) 0x0314, ByteBuffer.wrap(relocationDriver, 2, tuneInfo.compatibility() == SidTuneInfo.Compatibility.R64 ? 2 : 6), tuneInfo.compatibility() == SidTuneInfo.Compatibility.R64 ? 2 : 6);

            // Experimental restart basic trap
            short address = SidEndian.toLittle16(ByteBuffer.wrap(relocationDriver, 8, 2));
            mem.installBasicTrap((short) 0xffe1);
            mem.writeMemWord((short) 0x0328, address);
        }

        int pos = driverAddress;

        // Install driver to ram
        mem.fillRam((short) pos, ByteBuffer.wrap(relocationDriver, 10, relocationSize), relocationSize);

        // Set song number
        mem.writeMemByte((short) pos, (byte) (tuneInfo.currentSong() - 1));
        pos++;

        // Set tunes speed (VIC/CIA)
        mem.writeMemByte((short) pos, (byte) (tuneInfo.songSpeed() == SidTuneInfo.SPEED_VBI ? 0 : 1));
        pos++;

        // Set init address
        mem.writeMemWord((short) pos, (short) (tuneInfo.compatibility() == SidTuneInfo.Compatibility.BASIC ?
                0xbf55 : tuneInfo.initAddr()));
        pos += 2;

        // Set play address
        mem.writeMemWord((short) pos, tuneInfo.playAddr());
        pos += 2;

        // Set init address io bank value
        mem.writeMemByte((short) pos, iomap(tuneInfo.initAddr()));
        pos++;

        // Set play address io bank value
        mem.writeMemByte((short) pos, iomap(tuneInfo.playAddr()));
        pos++;

        // Set PAL/NTSC flag
        mem.writeMemByte((short) pos, video);
        pos++;

        // Set the required tune clock speed
        byte clockSpeed = switch (tuneInfo.clockSpeed()) {
            case PAL -> 1;
            case NTSC -> 0;
            default -> video; // UNKNOWN or ANY
        };
        mem.writeMemByte((short) pos, clockSpeed);
        pos++;

        // Set default processor register Flags on calling init
        mem.writeMemByte((short) pos, (byte) (tuneInfo.compatibility().ordinal() >= SidTuneInfo.Compatibility.R64.ordinal() ? 0 : 1 << Mos6510.SR_INTERRUPT));

        //System.err.println("%d",Mem.readMemByte(0x17e3));
    }
}





