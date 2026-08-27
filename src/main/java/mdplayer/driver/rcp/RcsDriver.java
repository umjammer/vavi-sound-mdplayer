package mdplayer.driver.rcp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.lib.rcp.RCS;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import vavi.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.MidiOutInfo;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


/**
 * RCS
 *
 * @author kumatan
 */
public class RcsDriver extends BaseDriver {

    private static final Logger logger = getLogger(RcsDriver.class.getName());

    private final RCS rcs;

    public RcsDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.rcs = new RCS();
        rcs.charset = Common.charset;
        rcs.sampleRate = Common.VGMProcSampleRate;
        rcs.musicStep = Common.VGMProcSampleRate / 60.0;
        rcs.midiSend = (l, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, l, d, plugin.chipRegister.plugin(VstPlugin.class).vstDelta);
        rcs.lyric = l -> plugin.chipRegister.plugin(MidiPlugin.class).params[0].lyric = l;
        rcs.counter = () -> frameCounter = -latency - waitTime;
        rcs.midiCount = () -> plugin.chipRegister.plugin(MidiPlugin.class).getCount();
        rcs.stop = () -> stopped = true;
    }

    public RcsDriver() {
        this(null); // gross
    }

    public void setExtendFile(List<Tuple<String,byte[]>> extendFiles) {
        this.rcs.extendFiles = extendFiles;
    }

    public void setSupportFileName(String supportFileName) {
        this.rcs.supportFileName = supportFileName;
    }

    /** for the metadata-only path, where there is no plugin to take {@code playingFileName} from */
    public void setFilename(String filename) {
        this.rcs.filename = filename;
    }

    /**
     * Hands the driver the PCM8 back end the plugin registered with mdsound. Which one is live
     * follows {@code setting.pcm8Type}: {@code 0} is X68Sound's own PCM8, {@code 1} is PCM8PP.
     */
    public void setPcm8(int pcm8type, X68kYm2151Inst opmPCM, Pcm8PPInst pcm8pp) {
        this.rcs.pcm8type = pcm8type;
        this.rcs.opmPCM = opmPCM;
        this.rcs.pcm8pp = pcm8pp;
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        String[] rcpFilename = new String[1];
        byte[][] rcpBuf = new byte[1][];
        if (rcs.extendFiles != null) {
            for (Tuple<String, byte[]> n : rcs.extendFiles) {
                if (n.getItem1().equals(".RCP")) {
                    rcpBuf[0] = n.getItem2();
                    break;
                }
            }
        }
        Boolean ret = RCS.getRCSInfo(rcs.filename, rcs.supportFileName, buf, /* out */ rcs.pcmInfos, /* out */ rcs.pcmData, /* out */ rcpFilename, /* ref */ rcpBuf);
        if (!ret) return null;

        MetaData md = new MetaData();

        if (rcpBuf[0] == null) {
            String err = ".RCP File not found !";
            md.set(Tag.Title, err);
            md.set(Tag.TitleJ, err);
            return md;
        }

        // Get the song information in the RCP file from here
        ret = RCS.checkHeadString(rcpBuf[0]);
        if (ret == null) return null;
        boolean isG36 = ret;

        int ptr = 32;
        StringBuilder str;

        List<Byte> title = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (rcpBuf[0][ptr + i] == 0) break;
            title.add(rcpBuf[0][ptr + i]);
        }
        str = new StringBuilder((new String(ByteUtil.toByteArray(title))).trim());
        ptr += 64;
        md.set(Tag.Title, str.toString());
        md.set(Tag.TitleJ, str.toString());

        if (isG36) {
            ptr += 64;
            str = new StringBuilder("%s\n".formatted((new String(rcpBuf[0], ptr, 360)).replace("\0", "")));
        } else {
            str = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                str.append("%s\n".formatted((new String(rcpBuf[0], ptr + i * 28, 28)).replace("\0", "")));
            }
        }
        md.set(Tag.Note, str.toString());

        return md;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        // the compiling plugins fill dataBuf after the driver was constructed, so re-read it here
        this.dataBuf = plugin.getData();
        rcs.vgmBuf = dataBuf;
        rcs.filename = plugin.playingFileName;
        // model is only known now, and getInformationHeader() reads it to decide whether to mount the PCM
        rcs.isVirtualModel = model == EnmModel.VirtualModel;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        // Set 0 here to wait after sending control.
        //frameCounter = -latency - waitTime;
        frameCounter = 0;
        speed = 1;
        speedCounter = 0;

        metaData = retrieveMetaData(dataBuf, 0);
        //if (GD3 == null) return false;

        if (!rcs.getInformationHeader()) {
            // a .RCS carries only the PCM bank; without the .RCP it names there is no sequence to play
            throw new IllegalArgumentException(rcs.rcpFilename.isEmpty()
                    ? "Invalid header" : "no such .RCP beside the .RCS: " + rcs.rcpFilename);
        }

        // Create a command to send in advance for each port
        if (!makeBeforeSendCommand()) throw new IllegalArgumentException("Invalid command");

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(1, 1);
        }
    }

    private boolean makeBeforeSendCommand() {
        try {
            MidiOutInfo[] infos = plugin.chipRegister.plugin(MidiPlugin.class).get();
            if (infos == null || infos.length < 1) return true;

            rcs.beforeSend = new ArrayList[infos.length];
            rcs.sendControlIndex = new int[infos.length];
            rcs.sendControlDelta = new int[infos.length];
            for (int i = 0; i < rcs.beforeSend.length; i++) {
                rcs.beforeSend[i] = new ArrayList<>();

                // Generate Reset
                switch (infos[i].beforeSendType) {
                    case 0: // None
                        break;
                    case 1: // GM Reset
                        rcs.getCtlSysexFromText(rcs.beforeSend[i], setting.getMidiOut().getGMReset());
                        break;
                    case 2: // XG Reset
                        rcs.getCtlSysexFromText(rcs.beforeSend[i], setting.getMidiOut().getXGReset());
                        break;
                    case 3: // GS Reset
                        rcs.getCtlSysexFromText(rcs.beforeSend[i], setting.getMidiOut().getGSReset());
                        break;
                    case 4: // Custom
                        rcs.getCtlSysexFromText(rcs.beforeSend[i], setting.getMidiOut().getCustom());
                        break;
                }

                // If the file path is set, the control file is read.
                if (rcs.extendFiles != null) {
                    rcs.getControlFile(rcs.beforeSend[i], infos[i].type);
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void processOneFrame() {
        try {
            plugin.chipRegister.plugin(VstPlugin.class).vstDelta++;
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                    frameCounter++;

                    rcs.oneFrameMain();
                } else {
                    frameCounter++;
                }
            }
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
