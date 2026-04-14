package mdplayer.driver.mid;

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
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static mdplayer.Common.charset;


/**
 * @author kumatan
 */
public class MidiDriver extends BaseDriver {

    private static final Logger logger = System.getLogger(MidiDriver.class.getName());

    private final MID midi;

    public MidiDriver() {
        this.midi = new MID();
        midi.charset = Common.charset;
        midi.musicStep = Common.VGMProcSampleRate / 60.0;;
        midi.send0 = (n, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, n, d, plugin.chipRegister.plugin(VstPlugin.class).vstDelta);
        midi.send2 = (n, d1, d2) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, n, d1, d2, plugin.chipRegister.plugin(VstPlugin.class).vstDelta);
        midi.send3 = (n, d1, d2, d) -> plugin.chipRegister.plugin(MidiPlugin.class).send(model, n, d1, d2, d, plugin.chipRegister.plugin(VstPlugin.class).vstDelta);
        midi.lyric = (n, l) -> plugin.chipRegister.plugin(MidiPlugin.class).params[n].Lyric = l;
        midi.stop = () -> stopped = true;
        midi.counter = () -> frameCounter = -latency - waitTime;
    }

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        if (buf == null) return null;

        MetaData md = new MetaData();
        String T01TrackName = "";

        try {
            if (ByteUtil.readLeInt(buf, 0) != MID.FCC_MID) return null;
            int format = (buf[8] & 0xff) * 0x100 + (buf[9] & 0xff);
            int trkCount = (buf[10] & 0xff) * 0x100 + (buf[11] & 0xff);
            int adr = 14;
            byte midiEventBackup = 0;

            for (int i = 0; i < trkCount; i++) {
                if (buf.length <= adr) break;

                if (ByteUtil.readLeInt(buf, adr) != MID.FCC_TRK) return null;
                int len = (buf[adr + 4] & 0xff) * 0x1000000 + (buf[adr + 5] & 0xff) * 0x10000 + (buf[adr + 6] & 0xff) * 0x100 + (buf[adr + 7] & 0xff);
                adr += 8;
                int trkEndadr = adr + len;

                while (adr < trkEndadr && adr < buf.length) {
                    int delta = Common.getDelta(adr, buf);
                    byte cmd = buf[adr++];
                    if ((cmd & 0xff) == 0xf0 || (cmd & 0xff) == 0xf7) {
                        int bAdr = adr - 1;
                        int datalen = Common.getDelta(adr, buf);
                        adr = adr + datalen;
                    } else if ((cmd & 0xff) == 0xff) {
                        byte eventType = buf[adr++];
                        int eventLen = Common.getDelta(adr, buf);
                        List<Byte> eventData = new ArrayList<>();
                        for (int j = 0; j < eventLen; j++) {
                            if (buf[adr + j] == 0) break;
                            eventData.add(buf[adr + j]);
                        }
                        adr = adr + eventLen;
                        if (!eventData.isEmpty()) {
                            switch (eventType) {
                                case 0x01:
                                    //case 0x02:
                                    if (T01TrackName.isEmpty()) {
                                        T01TrackName = new String(ByteUtil.toByteArray(eventData), charset).trim();
                                    }
                                    break;
                                case 0x03:
                                    if (md.getFirst(Tag.Title).isEmpty()) {
                                        if (format == 0 || (format == 1 && i == 0)) {
                                            md.set(Tag.Title, new String(ByteUtil.toByteArray(eventData), charset).trim());
                                            md.set(Tag.TitleJ, new String(ByteUtil.toByteArray(eventData), charset).trim());
                                        }
                                    }
                                    break;
                                case 0x05:
                                    //case 0x04:
                                    //case 0x06:
                                    //case 0x07:
                                    break;
                            }
                        }
                    } else {
                        if ((cmd & 0x80) != 0) {
                            midiEventBackup = (byte) (cmd & 0xff);
                            midi.midiEvent = midiEventBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                adr += 2;
                            } else {
                                adr++;
                            }
                        } else {
                            // Running status activated
                            midi.midiEvent = midiEventBackup;
                            midi.midiEventCh = midi.midiEventChBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                adr++;
                            }
                        }
                    }
                }
            }

            // If no title was found
            if (md.getFirst(Tag.Title).isEmpty() && md.getFirst(Tag.TitleJ).isEmpty() && !T01TrackName.isEmpty()) {
                md.set(Tag.Title, T01TrackName);
                md.set(Tag.TitleJ, T01TrackName);
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

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

        midi.getInformationHeader(vgmBuf);

        // Create a command to send in advance for each port
        makeBeforeSendCommand();

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait(1, 1);
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
                    midi.oneFrameMain(dataBuf);
                } else {
                    frameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void makeBeforeSendCommand() {
        try {
            MidiOutInfo[] infos = plugin.chipRegister.plugin(MidiPlugin.class).get();
            if (infos == null || infos.length < 1) return;

            midi.beforeSend = new List[infos.length];
            midi.sendControlIndex = new int[infos.length];
            midi.sendControlDelta = new int[infos.length];

            for (int i = 0; i < midi.beforeSend.length; i++) {
                midi.beforeSend[i] = new ArrayList<>();

                // Generate Reset
                switch (infos[i].beforeSendType) {
                    case 0: // None
                        break;
                    case 1: // GM Reset
                        midi.getCtlSysexFromText(midi.beforeSend[i], setting.getMidiOut().getGMReset());
                        break;
                    case 2: // XG Reset
                        midi.getCtlSysexFromText(midi.beforeSend[i], setting.getMidiOut().getXGReset());
                        break;
                    case 3: // GS Reset
                        midi.getCtlSysexFromText(midi.beforeSend[i], setting.getMidiOut().getGSReset());
                        break;
                    case 4: // Custom
                        midi.getCtlSysexFromText(midi.beforeSend[i], setting.getMidiOut().getCustom());
                        break;
                }

            }

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
