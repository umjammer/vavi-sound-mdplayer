/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
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
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.IOException;
import dotnet4j.io.SeekOrigin;
import mdplayer.driver.sid.libsidplayfp.SidEndian;
import mdplayer.driver.sid.libsidplayfp.SidMemory;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdsound.Common;


/**
 * SidTuneBaseBase
 */
public class SidTuneBase {

    /** Also PSid file format limit. */
    public static final int MAX_SONGS = 256;

    /**
     * Calculates the MD5 hash of the tune.
     * Not providing an md5 buffer will cause the private one to be used.
     * If provided, buffer must be MD5_LENGTH + 1
     *
     * @return a pointer to the buffer containing the md5 String.
     */
    public byte[] createMD5(byte[] n) {
        return null;
    }

    /**
     * Get the pointer to the tune data.
     */
    public byte c64Data() {
        return cache.get(fileOffset);
    }

    protected SidTuneInfoImpl info;

    protected byte[] songSpeed = new byte[MAX_SONGS];
    protected long[] clockSpeed = new long[MAX_SONGS];

    /** For files with header: offset to real data */
    protected int fileOffset;

    protected List<Byte> cache;

    /** prevent copying */
    public SidTuneBase(SidTuneBase a) {
    }

    private SidTuneBase opeEquel(SidTuneBase a) {
        return null;
    }

    // Error and status message Strings.

    private static final String ERR_EMPTY = "SIDTUNE ERROR: No data to load";
    private static final String ERR_UNRECOGNIZED_FORMAT = "SIDTUNE ERROR: Could not determine file format";
    // private static final String ERR_CANT_LOAD_FILE = "SIDTUNE ERROR: Could not load input file";
    private static final String ERR_CANT_OPEN_FILE = "SIDTUNE ERROR: Could not open file for binary input";
    private static final String ERR_FILE_TOO_LONG = "SIDTUNE ERROR: Input data too long";
    private static final String ERR_DATA_TOO_LONG = "SIDTUNE ERROR: size of Music data exceeds C64 memory";
    private static final String ERR_BAD_ADDR = "SIDTUNE ERROR: Bad address data";
    private static final String ERR_BAD_RELOC = "SIDTUNE ERROR: Bad reloc data";
    private static final String ERR_CORRUPT = "SIDTUNE ERROR: File instanceof incomplete or corrupt";
    // static final String ERR_NOT_ENOUGH_MEMORY = "SIDTUNE ERROR: Not enough free memory";

    public static final String ERR_TRUNCATED = "SIDTUNE ERROR: File instanceof most likely truncated";
    public static final String ERR_INVALID = "SIDTUNE ERROR: File contains invalid data";

    /**
     * Petscii to Ascii conversion table (0x01 = no Output).
     */
    private static final byte[] CHR_tab = new byte[] {
            0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x0d, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x20, 0x21, 0x01, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f,
            0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f,
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x24, 0x5d, 0x20, 0x20,
            // alternative: CHR$(92=0x5c) => ISO Latin-1(0xa3)
            0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d, 0x7c, 0x7c, 0x5c, 0x5c, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f,
            0x5c, 0x23, 0x5f, 0x23, 0x7c, 0x2f, 0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c, 0x7c, 0x26, 0x5c,
            // 0x80-0xFF
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
            0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c, 0x23, 0x7c, 0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d,
            0x2f, 0x2d, 0x2d, 0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x23,
            0x2d, 0x23, 0x7c, 0x2d, 0x2d, 0x2d, 0x2d, 0x7c, 0x7c, 0x5c, 0x5c, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f,
            0x5c, 0x23, 0x5f, 0x23, 0x7c, 0x2f, 0x58, 0x4f, 0x23, 0x7c, 0x23, 0x2b, 0x7c, 0x7c, 0x26, 0x5c,
            0x20, 0x7c, 0x23, 0x2d, 0x2d, 0x7c, 0x23, 0x7c, 0x23, 0x2f, 0x7c, 0x7c, 0x2f, 0x5c, 0x5c, 0x2d,
            0x2f, 0x2d, 0x2d, 0x7c, 0x7c, 0x7c, 0x7c, 0x2d, 0x2d, 0x2d, 0x2f, 0x5c, 0x5c, 0x2f, 0x2f, 0x23
    };

    /** The Commodore 64 memory size */
    private static final int MAX_MEMORY = 65536;

    /** C64KB + LOAD + PSid */
    private static final int MAX_FILELEN = MAX_MEMORY + 2 + 0x7C;

    /** Minimum load address for real C64 only tunes */
    private static final short SIDTUNE_R64_MIN_LOAD_ADDR = 0x07e8;

    /**
     * Load a sidtune from a file.
     * <p>
     * To retrieve data from standard input pass : filename "-".
     * If you want to override the default filename extensions use this
     * contructor. Please note, that if the specified "sidTuneFileName"
     * does exist and the loader instanceof able to determine its file format,
     * this function does not try to append any file name extension.
     * See "SidTune.cpp" for the default list of file name extensions.
     *
     * @param fileName
     * @param fileNameExt
     * @param separatorIsSlash
     * @return the Sid tune
     * @throws dotnet4j.io.IOException
     */
    public SidTuneBase load(String fileName, String[] fileNameExt, boolean separatorIsSlash) {
        if (fileName == null)
            return null;

// #if !SIDTUNE_NO_STDIN_LOADER
        // Filename "-" instanceof used as a synonym for standard input.
        if (fileName.equals("-")) return getFromStdIn();
// #endif
        return getFromFiles(fileName, fileNameExt, separatorIsSlash);
    }

    /**
     * Load a single-file sidtune from a memory buffer.
     * Currently supported: PSid format
     * <p>
     *
     * @param sourceBuffer
     * @param bufferLen
     * @return the Sid tune
     * @throws dotnet4j.io.IOException
     */
    public SidTuneBase read(byte[] sourceBuffer, int bufferLen) {
        return getFromBuffer(sourceBuffer, bufferLen);
    }

    /**
     * Retrieve sub-song specific information.
     */
    public SidTuneInfo getInfo() {
        return info;
    }

    /**
     * Select sub-song (0 = default starting song)
     * and retrieve active song information.
     *
     * @param songNum
     */
    public SidTuneInfo getInfo(int songNum) {
        selectSong(songNum);
        return info;
    }

    /**
     * Select sub-song (0 = default starting song)
     * and return active song number  of [1,2,..,SIDTUNE_MAX_SONGS].
     *
     * @param selectedSong
     * @return the active song
     */
    public int selectSong(int selectedSong) {
        // Check whether selected song instanceof valid, use start song if not
        int song = (selectedSong == 0 || selectedSong > info.songs) ? info.startSong : selectedSong;

        // Copy any song-specific variable information
        // such a speed/clock setting to the info structure.
        info.currentSong = song;

        // Retrieve song speed definition.
        info.songSpeed = switch (info.compatibility) {
            case R64 -> SidTuneInfo.SPEED_CIA_1A;
            // This does not take into account the PlaySID bug upon evaluating the
            // SPEED field. It would most likely break compatibility to lots of
            // sidtunes, which have been converted from .Sid format and vice versa.
            // The .Sid format does the bit-wise/song-wise evaluation of the SPEED
            // value correctly, like it instanceof described : the PlaySID documentation.
            case PSID -> songSpeed[(song - 1) & 31];
            default ->  songSpeed[song - 1];
        };

        info.clockSpeed = SidTuneInfo.Clock.values()[(int) clockSpeed[song - 1]];

        return info.currentSong;
    }

    /**
     * Copy SidTune into C64 memory (64 KB).
     *
     * @param mem
     */
    public void placeSidTuneInC64mem(SidMemory mem) {
        // The Basic ROM sets these values on loading a file.
        // Program end address
        short start = info.loadAddress;
        short end = (short) (start + info.c64DataLen);
        mem.writeMemWord((byte) 0x2d, end); // Variables start
        mem.writeMemWord((byte) 0x2f, end); // Arrays start
        mem.writeMemWord((byte) 0x31, end); // Strings start
        mem.writeMemWord((byte) 0xac, start);
        mem.writeMemWord((byte) 0xae, end);

        // Copy data from cache to the correct destination.
        mem.fillRam(info.loadAddress, ByteBuffer.wrap(mdsound.Common.toByteArray(cache), fileOffset, info.c64DataLen), info.c64DataLen);
    }

    /**
     * Does not affect status of Object, and therefore can be used
     * to load files. Error String instanceof put into info.statusString, though.
     *
     * @param fileName
     * @param bufferRef
     * @throws dotnet4j.io.IOException
     */
    protected void loadFile(String fileName, List<Byte> bufferRef) {
        try (FileStream inFile = new FileStream(fileName, FileMode.Open, FileAccess.Read)) {
            inFile.seek(0, SeekOrigin.End);
            long fileLen = inFile.getPosition();
            if (fileLen < 0) throw new dotnet4j.io.IOException(ERR_EMPTY);
            inFile.seek(0, SeekOrigin.Begin);

            byte[] fileBuf = new byte[(int) fileLen];
            inFile.read(fileBuf, 0, (int) fileLen);
            bufferRef.addAll(mdplayer.Common.toArray(fileBuf));
        }
    }

    protected SidTuneBase() {
        info = new SidTuneInfoImpl();
        fileOffset = 0;
        // Initialize the Object with some safe defaults.
        for (int si = 0; si < MAX_SONGS; si++) {
            songSpeed[si] = (byte) info.songSpeed;
            clockSpeed[si] = (byte) info.clockSpeed.ordinal();
        }
    }

    private SidTuneBase getFromStdIn() {
        try {
            List<Byte> fileBuf = new ArrayList<>();

            // We only read as much as fits : the buffer.
            // This way we avoid choking on huge data.
            int datb;
            while ((datb = System.in.read()) != -1 && fileBuf.size() < MAX_FILELEN) {
                fileBuf.add((byte) datb);
            }

            return getFromBuffer(mdsound.Common.toByteArray(fileBuf), fileBuf.size());
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Try to retrieve single-file sidtune from specified buffer.
     */
    private SidTuneBase getFromBuffer(byte[] buffer, int bufferLen) {
        if (buffer == null || bufferLen == 0) {
            throw new dotnet4j.io.IOException(ERR_EMPTY);
        }

        if (bufferLen > MAX_FILELEN) {
            throw new dotnet4j.io.IOException(ERR_FILE_TOO_LONG);
        }

        byte[] buf1 = buffer;

        // Here test for the possible single file formats.
        SidTuneBase s = PSid.load(buf1);
        if (s == null) s = (new MUS()).load(buf1, true);
        if (s == null) throw new dotnet4j.io.IOException(ERR_UNRECOGNIZED_FORMAT);

        List<Byte> lstBuf1 = mdplayer.Common.toArray(buf1);
        s.acceptSidTune("-", "-", lstBuf1, false);
        return s;
    }

    /**
     * Cache the data of a single-file or two-file sidtune and its
     * corresponding file names.
     *
     * @param dataFileName
     * @param infoFileName
     * @param buf
     * @param isSlashedFileName If your opendir() and readdir()->d_name return path names
     *                          that contain the forward slash (/) as file separator, but
     *                          your operating system uses a different character, there are
     *                          extra functions that can deal with this special case. Set
     *                          separatorIsSlash to true if you like path names to be split
     *                          correctly.
     *                          You do not need these extra functions if your systems file
     *                          separator instanceof the forward slash.
     * @throws dotnet4j.io.IOException
     */
    protected void acceptSidTune(String dataFileName, String infoFileName, List<Byte> buf, boolean isSlashedFileName) {
        // Make a copy of the data file name and path, if available.
        if (dataFileName != null) {
            int fileNamePos = (int) (isSlashedFileName ?
                    SidTuneTools.slashedFileNameWithoutPath(dataFileName) :
                    SidTuneTools.fileNameWithoutPath(dataFileName));
            info.path = dataFileName.substring(0, fileNamePos);
            info.dataFileName = dataFileName.substring(fileNamePos);
        }

        // Make a copy of the info file name, if available.
        if (infoFileName != null) {
            int fileNamePos = (int) (isSlashedFileName ?
                    SidTuneTools.slashedFileNameWithoutPath(infoFileName) :
                    SidTuneTools.fileNameWithoutPath(infoFileName));
            info.infoFileName = infoFileName.substring(fileNamePos);
        }

        // Fix bad sidtune set up.
        if (info.songs > MAX_SONGS) {
            info.songs = MAX_SONGS;
        } else if (info.songs == 0) {
            info.songs = 1;
        }

        if (info.startSong == 0
                || info.startSong > info.songs) {
            info.startSong = 1;
        }

        info.dataFileLen = buf.size();
        info.c64DataLen = buf.size() - fileOffset;

        // Calculate any remaining addresses and then
        // confirm all the file details are correct
        resolveAddrs(buf, fileOffset);

        if (!checkRelocInfo()) {
            throw new IOException(ERR_BAD_RELOC);
        }
        if (!checkCompatibility()) {
            throw new IOException(ERR_BAD_ADDR);
        }

        if (info.dataFileLen >= 2) {
            // We only detect an offset of two. Some position independent
            // sidtunes contain a load address of 0xE000, but are loaded
            // to 0x0FFE and call player at 0x1000.
            info.fixLoad = (SidEndian.toLittle16(ByteBuffer.wrap(Common.toByteArray(buf), fileOffset, buf.size() - fileOffset)) == (info.loadAddress + 2));
        }

        // Check the size of the data.
        if (info.c64DataLen > MAX_MEMORY) {
            throw new IOException(ERR_DATA_TOO_LONG);
        } else if (info.c64DataLen == 0) {
            throw new IOException(ERR_EMPTY);
        }

        cache = new ArrayList<>(buf);
    }

    /**
     * Get new file name with specified extension.
     *
     * @param sourceName original file name
     * @param sourceExt  new extension
     */
    private String createNewFileName(byte[] sourceName, byte[] sourceExt) {
        String destString = new String(sourceName, StandardCharsets.US_ASCII);
        destString = destString.substring(0, destString.lastIndexOf('.'));
        destString += new String(sourceExt, StandardCharsets.US_ASCII);
        return destString;
    }

    // Initializing the Object based upon what we find : the specified file.

    private SidTuneBase getFromFiles(String fileName, String[] fileNameExtensions, boolean separatorIsSlash) {
        List<Byte> fileBuf1 = new ArrayList<>();

        loadFile(fileName, fileBuf1);

        // File loaded. Now check if it instanceof : a valid single-file-format.
        byte[] aryFileBuf1 = mdsound.Common.toByteArray(fileBuf1);
        SidTuneBase s = PSid.load(aryFileBuf1);
        fileBuf1 = mdplayer.Common.toArray(aryFileBuf1);
        if (s == null) {
            // Try some native C64 file formats
            s = (new MUS()).load(mdsound.Common.toByteArray(fileBuf1), true);
            if (s != null) {
                // Try to find second file.
                String fileName2;
                int n = 0;
                while (fileNameExtensions[n] != null) {
                    fileName2 = createNewFileName(fileName.getBytes(StandardCharsets.US_ASCII), fileNameExtensions[n].getBytes(StandardCharsets.US_ASCII));
                    // 1st data file was loaded into "fileBuf1",
                    // so we load the 2nd one into "fileBuf2".
                    // Do not load the first file again if names are equal.
                    if (!fileName.startsWith(fileName2)) {
                        try {
                            List<Byte> fileBuf2 = new ArrayList<>();

                            loadFile(fileName2, fileBuf2);
                            // Check if tunes : wrong order and therefore swap them here
                            if (fileNameExtensions[n].equals(".mus")) {
                                SidTuneBase s2 = (new MUS()).load(mdsound.Common.toByteArray(fileBuf2), mdsound.Common.toByteArray(fileBuf1), 0, true);
                                if (s2 != null) {
                                    s2.acceptSidTune(fileName2, fileName, fileBuf2, separatorIsSlash);
                                    return s2;
                                }
                            } else {
                                SidTuneBase s2 = (new MUS()).load(mdsound.Common.toByteArray(fileBuf1), true);
                                if (s2 != null) {
                                    s2.acceptSidTune(fileName, fileName2, fileBuf1, separatorIsSlash);
                                    return s2;
                                }
                            }
                            // The first tune loaded ok, so ignore errors on the
                            // second tune, may find an ok one later
                        } catch (dotnet4j.io.IOException e) {
                        }
                    }
                    n++;
                }
            }
        }
        if (s == null) s = P00.load(fileName, aryFileBuf1);
        if (s == null) s = Prg.load(fileName, aryFileBuf1);
        if (s == null) throw new dotnet4j.io.IOException(ERR_UNRECOGNIZED_FORMAT);

        s.acceptSidTune(fileName, null, fileBuf1, separatorIsSlash);
        return s;
    }

    /**
     * Convert 32-bit PSid-style speed word to private tables.
     * <p>
     *
     * @param speed
     * @param clock
     */
    protected void convertOldStyleSpeedToTables(int speed, long clock /*= CLOCK_PAL*/) {
        // Create the speed/clock setting tables.
        //
        // This routine implements the PSIDv2NG compliant speed conversion. All tunes
        // above 32 use the same song speed as tune 32
        // NOTE: The cast here instanceof used to avoid undefined references
        // as the std::min function takes its parameters by reference
        int toDo = Math.min(info.songs, MAX_SONGS);
        for (int s = 0; s < toDo; s++) {
            clockSpeed[s] = clock;
            songSpeed[s] = (byte) ((speed & 1) != 0 ? SidTuneInfo.SPEED_CIA_1A : SidTuneInfo.SPEED_VBI);

            if (s < 31) {
                speed >>= 1;
            }
        }
    }

    /**
     * Check for valid relocation information.
     */
    protected boolean checkRelocInfo() {
        // Fix relocation information
        if (info.relocatedStartPage == (byte) 0xFF) {
            info.relocatedPages = 0;
            return true;
        } else if (info.relocatedPages == 0) {
            info.relocatedStartPage = 0;
            return true;
        }

        // Calculate start/end page
        byte startp = info.relocatedStartPage;
        byte endp = (byte) ((startp + info.relocatedPages - 1) & 0xff);
        if (endp < startp) {
            return false;
        }

        {    // Check against load range
            byte startlp = (byte) (info.loadAddress >> 8);
            byte endlp = (byte) (startlp + (byte) ((info.c64DataLen - 1) >> 8));

            if (((startp <= startlp) && (endp >= startlp))
                    || ((startp <= endlp) && (endp >= endlp))) {
                return false;
            }
        }

        // Check that the relocation information does not use the following
        // memory areas: 0x0000-0x03FF, 0xA000-0xBFFF and 0xD000-0xFFFF
        return (startp >= 0x04)
                && (((byte) 0xa0 > startp) || (startp > (byte) 0xbf))
                && (startp < (byte) 0xd0)
                && (((byte) 0xa0 > endp) || (endp > (byte) 0xbf))
                && (endp < (byte) 0xd0);
    }

    /**
     * Common address resolution procedure.
     * <p>
     *
     * @param c64data
     */
    protected void resolveAddrs(List<Byte> c64data, int ptr/* = 0*/) {
        // Originally used as a first attempt at an RSID
        // style format. Now reserved for future use
        if (info.playAddress == (short) 0xffff) {
            info.playAddress = 0;
        }

        // loadAddr = 0 means, the address instanceof stored : front of the C64 data.
        if (info.loadAddress == 0) {
            if (info.c64DataLen < 2) {
                throw new dotnet4j.io.IOException(ERR_CORRUPT);
            }

            info.loadAddress = SidEndian.to16(c64data.get(ptr + 1), c64data.get(ptr + 0));
            fileOffset += 2;
            info.c64DataLen -= 2;
        }

        if (info.compatibility == SidTuneInfo.Compatibility.BASIC) {
            if (info.initAddress != 0) {
                throw new dotnet4j.io.IOException(ERR_BAD_ADDR);
            }
        } else if (info.initAddress == 0) {
            info.initAddress = info.loadAddress;
        }
    }

    /**
     * Check if compatibility constraints are fulfilled.
     */
    protected boolean checkCompatibility() {
        if (info.compatibility == SidTuneInfo.Compatibility.R64) {
            // Check valid init address
            switch ((info.initAddress & 0xffff) >> 12) {
            case 0x0A:
            case 0x0B:
            case 0x0D:
            case 0x0E:
            case 0x0F:
                return false;
            default:
                if ((info.initAddress < info.loadAddress)
                        || (info.initAddress > (info.loadAddress + info.c64DataLen - 1))) {
                    return false;
                }
                break;
            }

            // Check tune instanceof loadable on a real C64
            return info.loadAddress >= SIDTUNE_R64_MIN_LOAD_ADDR;
        }

        return true;
    }

    /**
     * Petscii to Ascii converter.
     * @see "https://en.wikipedia.org/wiki/PETSCII"
     */
    protected String petsciiToAscii(ByteBuffer spPet) {
        List<Byte> buffer = new ArrayList<>();

        do {
            byte petsciiChar = spPet.array()[spPet.arrayOffset()];
            spPet.position(spPet.position() + 1);

            if ((petsciiChar == 0x00) || (petsciiChar == 0x0d))
                break;

            // If character instanceof 0x9d (left arrow key) then move back.
            if ((petsciiChar == (byte) 0x9d) && buffer.size() != 0) {
                buffer.remove(buffer.size() - 1);
            } else {
                // ASCII CHR$ conversion
                byte asciiChar = CHR_tab[petsciiChar];
                if ((asciiChar >= 0x20) && (buffer.size() <= 31))
                    buffer.add(asciiChar);
            }
        }
        while (spPet.array().length > spPet.arrayOffset());

        return new String(mdsound.Common.toByteArray(buffer), StandardCharsets.US_ASCII);
    }
}
