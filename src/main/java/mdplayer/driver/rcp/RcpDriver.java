package mdplayer.driver.rcp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.MidiOutInfo;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.rcp.RCP;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;
import vavi.util.compat.Tuple;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * RCP
 *
 * @author kumatan
 */
public class RcpDriver extends BaseDriver {

    private static final Logger logger = getLogger(RcpDriver.class.getName());

    private final RCP rcp;

    public RcpDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.rcp = new RCP();
        rcp.charset = Common.charset;
        rcp.sampleRate = Common.VGMProcSampleRate;
        rcp.musicStep = Common.VGMProcSampleRate / 60.0;
        rcp.midiSend = (l, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, l, d, plugin.chipRegister.plugin(VstPlugin.class).vstDelta);
        rcp.lyric = l -> plugin.chipRegister.plugin(MidiPlugin.class).params[0].lyric = l;
        rcp.counter = () -> frameCounter = -latency - waitTime;
        rcp.midiCount = () -> plugin.chipRegister.plugin(MidiPlugin.class).getCount();
        rcp.stop = () -> stopped = true;
    }

    public RcpDriver() {
        this(null); // gross
    }

    public void setExtendFile(List<Tuple<String,byte[]>> extendFiles) {
        this.rcp.extendFile = extendFiles;
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        if (buf == null) return null;
        Boolean ret = RCP.checkHeadString(buf, Common.charset);
        if (ret == null) return null;
        boolean isG36 = ret;

        MetaData md = new MetaData();
        int ptr = 32;
        StringBuilder str;

        List<Byte> title = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (buf[ptr + i] == 0) break;
            title.add(buf[ptr + i]);
        }
        str = new StringBuilder(new String(ByteUtil.toByteArray(title), charset).trim());
        ptr += 64;
        md.set(Tag.Title, str.toString());
        md.set(Tag.TitleJ, str.toString());

        if (isG36) {
            ptr += 64;
            str = new StringBuilder("%s\n".formatted(new String(buf, ptr, 360, charset).replace("\0", "")));
        } else {
            str = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                str.append("%s\n".formatted(new String(buf, ptr + i * 28, 28, charset).replace("\0", "")));
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

        metaData = retrieveMetaData(dataBuf);
        //if (Gd3 == null) return false;

        rcp.data = dataBuf;

        if (!rcp.getInformationHeader()) {
            throw new IllegalArgumentException("invalid header");
        }

        // Create a command to send in advance for each port
        if (!makeBeforeSendCommand()) {
            throw new IllegalArgumentException("invalid command");
        }

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 1, 1);
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

                    rcp.oneFrameMain();
                } else {
                    frameCounter++;
                }
            }
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private boolean makeBeforeSendCommand() {
        try {
            MidiOutInfo[] infos = plugin.chipRegister.plugin(MidiPlugin.class).get();
            if (infos == null || infos.length < 1) return true;

            rcp.beforeSend = new List[infos.length];
            rcp.sendControlIndex = new int[infos.length];
            rcp.sendControlDelta = new int[infos.length];
            for (int i = 0; i < rcp.beforeSend.length; i++) {
                rcp.beforeSend[i] = new ArrayList<>();

                // Generate Reset
                switch (infos[i].beforeSendType) {
                    case 0: // None
                        break;
                    case 1: // GM Reset
                        rcp.getCtlSysexFromText(rcp.beforeSend[i], setting.getMidiOut().getGMReset());
                        break;
                    case 2: // XG Reset
                        rcp.getCtlSysexFromText(rcp.beforeSend[i], setting.getMidiOut().getXGReset());
                        break;
                    case 3: // GS Reset
                        rcp.getCtlSysexFromText(rcp.beforeSend[i], setting.getMidiOut().getGSReset());
                        break;
                    case 4: // Custom
                        rcp.getCtlSysexFromText(rcp.beforeSend[i], setting.getMidiOut().getCustom());
                        break;
                }

                // If the file path is set, the process to read the control file is performed.
                if (rcp.extendFile != null) {
                    rcp.getControlFile(rcp.beforeSend[i], infos[i].type);
                }
            }

            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
    }
}
