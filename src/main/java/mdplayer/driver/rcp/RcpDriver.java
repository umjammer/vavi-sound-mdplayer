package mdplayer.driver.rcp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.MidiOutInfo;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.VstPlugin;
import mdplayer.chips.Ym2612Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * RCP
 *
 * @author kumatan
 */
public class RcpDriver extends BaseDriver {

    private static final Logger logger = getLogger(RCP.class.getName());

    private final RCP rcp;

    public RcpDriver() {
        this.rcp = new RCP();
        int vstDelta = plugin.chipRegister.plugin(VstPlugin.class).vstDelta;
        rcp.midiSend = (l, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, l, d, vstDelta);
        rcp.lyric = l -> plugin.chipRegister.plugin(MidiPlugin.class).params[0].Lyric = l;
        rcp.counter = () -> frameCounter = -latency - waitTime;
        rcp.midiCount = () -> plugin.chipRegister.plugin(MidiPlugin.class).getCount();
        rcp.stop = () -> stopped = true;
    }

    public void setExtendFile(List<Tuple<String,byte[]>> extendFiles) {
        this.rcp.extendFile = extendFiles;
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (buf == null) return null;
        Boolean ret = RCP.checkHeadString(buf);
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
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model,
                     int latency, int waitTime, Object... args) {
        this.dataBuf = vgmBuf;
        this.plugin = plugin;
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

        metaData = getMetaData(vgmBuf);
        //if (Gd3 == null) return false;

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

        rcp.data = vgmBuf;
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
