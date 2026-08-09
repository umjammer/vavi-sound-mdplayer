package mdplayer.driver.ay;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.ZxBeepChip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.ay.AY;
import mdplayer.driver.BasePlugin;
import mdsound.np.LoopDetector;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;

import static java.lang.System.getLogger;


/**
 * AY
 *
 * @author kumatan
 */
public class AyDriver extends BaseDriver {

    private static final Logger logger = getLogger(AyDriver.class.getName());

    /** the rate the AY player's interrupt runs at, and so the unit a song's length is stated in */
    private static final double PAL = 50.0;

    /**
     * How long a song whose header states no length is played before it is given up on. Most of
     * those loop and the loop detector below ends them; this is for the ones that never repeat
     * themselves exactly.
     */
    private static final int maxPlayTimeMs = 5 * 60 * 1000;

    private AY ay;

    /** what the header says this song lasts, fade included; 0 when it says nothing */
    private int songLengthMs;
    private final LoopDetector.BasicDetector ld = new LoopDetector.BasicDetector(20);
    private boolean playtimeDetected;

    public AyDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);
    }

    public AyDriver() {
        this(null); // gross
    }

    /** @param args 0: songNo */
    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        playtimeDetected = false;
        songLengthMs = 0;
        ld.reset();
        this.model = model;
        frameCounter = -latency - waitTime;

        int songNo = (int) args[0];

        ay = new AY();
        ay.setZxClock();
        // one oneFrame() is one tick of this rate, and processOneFrame() runs VGMProcSampleRate of
        // them per second whatever the output device asks for - telling the AY anything else here
        // plays the song at the wrong speed (8% slow at 48kHz), and counter would not be samples
        ay.setSampleRate(Common.VGMProcSampleRate);

        try {
            ay.run(dataBuf);
            ay.setup(songNo,
                    (r, d) -> {
                        plugin.chipRegister.chip(Ay8910Chip.class).write(0, r, d, model);
                        ld.write(r, d, 0);
                    },
                    () -> plugin.chipRegister.chip(ZxBeepChip.class).write(0, -1, -1, -1, model)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Driver initialization failed.", e);
        }

        metaData = getMetaData(dataBuf, songNo);

        // where the header states how long the song plays for, that settles it and the watching
        // in processOneFrame() has nothing left to work out
        AY.SongData songData = ay.songData(songNo);
        songLengthMs = (int) ((songData.songLength + songData.fadeLength) / PAL * 1000);
        if (songLengthMs > 0) {
logger.log(Level.DEBUG, "songlength: %.1fs".formatted(songLengthMs / 1000d));
            playtimeDetected = true;
            totalCounter = (long) songLengthMs * Common.VGMProcSampleRate / 1000L;
        }
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    ay.oneFrame();
                    counter++;
                    detectEnd();
                } else {
                    frameCounter++;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    /**
     * Works out how far through the song the playback is. Without this an AY file plays for ever:
     * the player it emulates has no end of its own, it just keeps taking interrupts.
     */
    private void detectEnd() {
        int timeInMs = (int) (ay.frames / PAL * 1000);

        if (playtimeDetected) {
            if (songLengthMs > 0) {
                // the header's time is the whole of the song, not one time round it, so it is
                // played once and not loopTimes times
                if (timeInMs >= songLengthMs) over();
            } else if (totalCounter != 0) {
                curLoop = (int) (counter / totalCounter);
            }
            return;
        }
        curLoop = 0;

        if (ld.isLooped(timeInMs, 30000, 5000)) {
            int start = ld.getLoopStart(), end = ld.getLoopEnd();
logger.log(Level.DEBUG, "loop: %d - %d ms".formatted(start, end));
            playtimeDetected = true;
            totalCounter = (long) end * Common.VGMProcSampleRate / 1000L;
            if (totalCounter == 0) totalCounter = counter;
            loopCounter = ((long) end - (long) start) * Common.VGMProcSampleRate / 1000L;
            return;
        }

        if (timeInMs > maxPlayTimeMs) {
logger.log(Level.DEBUG, "end: gave up after %ds".formatted(maxPlayTimeMs / 1000));
            over();
        }
    }

    /** Ends the song: the player fades it out from here and moves on to the next one. */
    private void over() {
        playtimeDetected = true;
        loopCounter = 0;
        stopped = true;
    }

    /** @param args 0: songNo, defaults to the file's first song */
    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        // the metadata is read straight out of the file, so a driver that is playing nothing (the
        // one the play list reads a file with) answers this as well as one that is
        AY read = new AY();
        read.getInformation(buf);
        int songNo = read.songIndex(args.length > 0 ? (int) args[0] : 0);
        AY.SongsStructure song = read.information.songsStructures.get(songNo);

        // the song name is what an AY file is usually known by, but plenty of them leave it
        // empty and say everything in the file's own comment instead
        String title = song.pSongName.isBlank() ? read.information.pMisc : song.pSongName;

        MetaData metaData = new MetaData();
        metaData.set(Tag.Title, title);
        metaData.set(Tag.TitleJ, title);
        metaData.set(Tag.Composer, read.information.pAuthor);
        metaData.set(Tag.ComposerJ, read.information.pAuthor);
        metaData.set(Tag.Note, read.information.pMisc);
        metaData.set(Tag.NumberOfSongs, String.valueOf(read.information.numOfSongs));

        int lengthMs = (int) ((song.songData.songLength + song.songData.fadeLength) / PAL * 1000);
        if (lengthMs > 0) {
            metaData.set(Tag.Duration, "%d:%02d".formatted(lengthMs / 60000, lengthMs / 1000 % 60));
        }

        return metaData;
    }
}
