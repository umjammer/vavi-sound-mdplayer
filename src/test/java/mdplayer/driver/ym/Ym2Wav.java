/*
 * ST-Sound ( YM files player library )
 *
 * Copyright (C) 1995-1999 Arnaud Carre ( http://leonard.oxg.free.fr )
 */

/*
 * This file is part of ST-Sound
 *
 * ST-Sound is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ST-Sound is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ST-Sound; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mdplayer.driver.ym;

import java.io.IOException;
import java.io.RandomAccessFile;

import mdplayer.lib.ym.YmMusic;
import mdplayer.lib.ym.YmMusic.YmMusicInfo;


/**
 *This is a sample program: it's an YM to WAV converter.
 */
public class Ym2Wav {

    private static final int NBSAMPLEPERBUFFER = 1024;
    private static final short[] convertBuffer = new short[NBSAMPLEPERBUFFER];

    private static final int ID_RIFF = 0x46464952;
    private static final int ID_WAVE = 0x45564157;
    private static final int ID_FMT  = 0x20746D66;
    private static final int ID_DATA = 0x61746164;

    static void main(String[] args) throws Exception {

        String platform = System.getProperty("os.arch").contains("64") ? "64bit" : "32bit";

        System.out.printf("""
                ym2wav (%s) - YM to WAV converter.
                Using ST-Sound Library, under GPL license
                Copyright (C) 1995-1999 Arnaud Carre ( http://leonard.oxg.free.fr )
                GNU/Linux (%s) port by Grzegorz Stanczyk
                """, platform, platform);

        if (args.length != 2) {
            System.out.println("Usage: ym2wav <ym music file> <wav file>");
            return;
        }

        //
        // ST-Sound library (placeholder)
        //

        YmMusic music = new YmMusic();

        music.load(args[0]);

        YmMusicInfo info = new YmMusicInfo();
        music.getMusicInfo(info);

        System.out.printf("Generating wav file from \"%s\"%n", args[0]);
        System.out.println(info.pSongName);
        System.out.println(info.pSongAuthor);
        System.out.printf("(%s)%n", info.pSongComment);
        System.out.printf("Total music time: %d seconds.%n", info.musicTimeInSec);

        int totalNbSample;

        try (RandomAccessFile out = new RandomAccessFile(args[1], "rw")) {

            // reserve 44-byte WAV header
            out.write(new byte[44]);

            music.setLoopMode(false);

            totalNbSample = 0;

            music.stop();
            music.play();

            while (music.update(convertBuffer, NBSAMPLEPERBUFFER)) {

                for (short s : convertBuffer) {
                    writeLE16(out, s);
                }

                totalNbSample += NBSAMPLEPERBUFFER;
            }

            out.seek(0);
            writeWavHeader(out, totalNbSample);
        }

        System.out.printf("%d samples written (%.02f Mb).%n", totalNbSample, (float) totalNbSample * 2 / (1024 * 1024));

        music.unLoad();
    }

    static void writeWavHeader(RandomAccessFile out, int totalSamples) throws IOException {

        int dataLength = totalSamples * 2;

        writeLE32(out, ID_RIFF);
        writeLE32(out, dataLength + 44 - 8);
        writeLE32(out, ID_WAVE);

        writeLE32(out, ID_FMT);
        writeLE32(out, 16);
        writeLE16(out, 1);          // PCM
        writeLE16(out, 1);          // mono
        writeLE32(out, 44100);
        writeLE32(out, 44100 * 2);
        writeLE16(out, 2);
        writeLE16(out, 16);

        writeLE32(out, ID_DATA);
        writeLE32(out, dataLength);
    }

    static void writeLE16(RandomAccessFile out, int v) throws IOException {
        out.writeByte(v & 0xff);
        out.writeByte((v >>> 8) & 0xff);
    }

    static void writeLE32(RandomAccessFile out, int v) throws IOException {
        out.writeByte(v & 0xff);
        out.writeByte((v >>> 8) & 0xff);
        out.writeByte((v >>> 16) & 0xff);
        out.writeByte((v >>> 24) & 0xff);
    }
}
