package mdplayer.driver.mid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.Vgm;
import mdplayer.driver.rcp.RCP;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.MidiOutInfo;
import vavi.util.ByteUtil;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;
import static java.lang.System.getLogger;


public class MID extends BaseDriver {

    private static final Logger logger = getLogger(MID.class.getName());

    public MID() {
        musicStep = Common.VGMProcSampleRate / 60.0;
    }

    public static final int FCC_MID = 0x6468544d;
    public static final int FCC_TRK = 0x6b72544d;
    public int format = 0;
    public int trkCount = 0;
    public int reso = 196;

    private double oneSyncTime = 0.0001;
    private double musicStep;
    private double musicDownCounter = 0.0;

    private List<RCP.CtlSysex>[] beforeSend = null;
    private int[] sendControlDelta = null;
    private int[] sendControlIndex = null;

    private List<Integer> musicPtr = null;
    private List<Integer> trkEndAdr = null;
    private List<Integer> trkPort = null;
    private List<Integer> midWaitCounter = null;
    private List<Boolean> isEnd = null;
    private List<Boolean> isDelta = null;

    byte midiEvent = 0;
    List<Byte> midiEventBackup = null;
    int midiEventCh = 0;
    int midiEventChBackup = 0;
    private final List<Byte> eventStr = new ArrayList<>();
    private String eventText = "";
    private String eventCopyrightNotice = "";
    private String eventSequenceTrackName = "";
    private String eventInstrumentName = "";
    private String eventLyric = "";
    private String eventMarker = "";


    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;

        Vgm.Gd3 gd3 = new Gd3();
        String T01TrackName = "";


        try {
            if (ByteUtil.readLeInt(buf, 0) != FCC_MID) return null;
            int format = (buf[8] & 0xff) * 0x100 + (buf[9] & 0xff);
            int trkCount = (buf[10] & 0xff) * 0x100 + (buf[11] & 0xff);
            int adr = 14;
            byte midiEventBackup = 0;

            for (int i = 0; i < trkCount; i++) {
                if (buf.length <= adr) break;

                if (ByteUtil.readLeInt(buf, adr) != FCC_TRK) return null;
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
                                    T01TrackName = new String(toByteArray(eventData), Charset.forName("MS932")).trim();
                                }
                                break;
                            case 0x03:
                                if (gd3.trackName.isEmpty()) {
                                    if (format == 0 || (format == 1 && i == 0)) {
                                        gd3.trackName = new String(toByteArray(eventData), Charset.forName("MS932")).trim();
                                        gd3.trackNameJ = new String(toByteArray(eventData), Charset.forName("MS932")).trim();
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
                            midiEvent = midiEventBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                adr += 2;
                            } else {
                                adr++;
                            }
                        } else {
                            // Running status activated
                            midiEvent = midiEventBackup;
                            midiEventCh = midiEventChBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                adr++;
                            }
                        }
                    }

                }
            }

            // If no title was found
            if (gd3.trackName.isEmpty() && gd3.trackNameJ.isEmpty() && !T01TrackName.isEmpty()) {
                gd3.trackName = T01TrackName;
                gd3.trackNameJ = T01TrackName;
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        vgmCurLoop = 0;
        stopped = false;
        // Set 0 here to wait after sending control.
        //vgmFrameCounter = -latency - waitTime;
        vgmFrameCounter = 0;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        gd3 = getGD3Info(vgmBuf);
        //if (Gd3 == null) return false;

        if (!getInformationHeader()) return false;

        // Create a command to send in advance for each port
        if (!makeBeforeSendCommand()) return false;

        if (model == EnmModel.RealModel) {
            chipRegister.setYM2612SyncWait((byte) 0, 1);
            chipRegister.setYM2612SyncWait((byte) 1, 1);
        }

        return true;
    }

    private boolean getInformationHeader() {
        if (vgmBuf == null) return false;
        if (ByteUtil.readLeInt(vgmBuf, 0) != FCC_MID) return false;

        format = (vgmBuf[8] & 0xff) * 0x100 + (vgmBuf[9] & 0xff);
        trkCount = (vgmBuf[10] & 0xff) * 0x100 + (vgmBuf[11] & 0xff);
        reso = (vgmBuf[12] & 0xff) * 0x100 + (vgmBuf[13] & 0xff);

        musicPtr = new ArrayList<>();
        midWaitCounter = new ArrayList<>();
        isEnd = new ArrayList<>();
        trkEndAdr = new ArrayList<>();
        trkPort = new ArrayList<>();
        isDelta = new ArrayList<>();
        midiEventBackup = new ArrayList<>();

        int adr = 14;
        for (int i = 0; i < trkCount; i++) {
            midWaitCounter.add(0);
            isEnd.add(false);
            isDelta.add(true);

            if (ByteUtil.readLeInt(vgmBuf, adr) != FCC_TRK) return false;
            int len = (vgmBuf[adr + 4] & 0xff) * 0x100_0000 + (vgmBuf[adr + 5] & 0xff) * 0x1_0000 + (vgmBuf[adr + 6] & 0xff) * 0x100 + (vgmBuf[adr + 7] & 0xff);
            adr += 8;
            musicPtr.add(adr);
            adr += len;
            trkEndAdr.add(adr);
            trkPort.add(0);
            midiEventBackup.add((byte) 0);
        }

        return true;
    }

    @Override
    public void processOneFrame() {
        try {
            vstDelta++;
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0 && !stopped) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameMain();
                } else {
                    vgmFrameCounter++;
                }
            }
            //Stopped = !IsPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameMain() {
        try {

            counter++;
            vgmFrameCounter++;

            musicStep = Common.VGMProcSampleRate * oneSyncTime;

            if (musicDownCounter <= 0.0) {
                if (beforeSend != null) {
                    sendControl();
                } else {
                    oneFrameMID();
                }
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameMID() {
//#if DEBUG
        if (model == EnmModel.VirtualModel) return;
//#endif
        boolean trksEnd = true;
        for (int trk = 0; trk < trkCount; trk++) {
            if (isEnd.get(trk)) continue;

            trksEnd = false;
            midWaitCounter.set(trk, midWaitCounter.get(trk) - 1);
            if (midWaitCounter.get(trk) > 0) continue;

            while (midWaitCounter.get(trk) < 1) {
                int ptr = musicPtr.get(trk);
                int delta;

                logger.log(Level.TRACE, "");
                logger.log(Level.TRACE, "ptr:[%08x] trk:[%2d] ".formatted(ptr, trk));

                if (isDelta.get(trk)) {
                    delta = Common.getDelta(ptr, vgmBuf);
                    midWaitCounter.set(trk, delta);

                    logger.log(Level.TRACE, "delta:%10d ".formatted(delta));
                } else {
                    byte cmd = vgmBuf[ptr++];

                    //logger.log(Level.TRACE, "cmd:%2x ".formatted(delta, cmd));

                    if ((cmd & 0xff) == 0xf0 || (cmd & 0xff) == 0xf7) {
                        int eventLen = Common.getDelta(ptr, vgmBuf);
                        //logger.log(Level.TRACE, "evntLen:%10D ".formatted(eventLen));
                        logger.log(Level.TRACE, "%2x ".formatted(cmd));
                        List<Byte> eventData = new ArrayList<>();
                        eventData.add(cmd);
                        for (int j = 0; j < eventLen; j++) {
                            eventData.add(vgmBuf[ptr + j]);
                            logger.log(Level.TRACE, "%2x ".formatted(vgmBuf[ptr + j]));
                        }

                        chipRegister.sendMIDIout(model, trkPort.get(trk), toByteArray(eventData), vstDelta);

                        ptr = ptr + eventLen;

                    } else if ((cmd & 0xff) == 0xff) {
                        byte eventType = vgmBuf[ptr++];
                        int eventLen = Common.getDelta(ptr, vgmBuf);

                        logger.log(Level.TRACE, "evntTyp:%2x evntLen:%10d ".formatted(eventType, eventLen));

                        List<Byte> eventData = new ArrayList<>();
                        for (int j = 0; j < eventLen; j++) {
                            eventData.add(vgmBuf[ptr + j]);
                            logger.log(Level.TRACE, "%2x ".formatted(vgmBuf[ptr + j]));
                        }
                        ptr = ptr + eventLen;
                        if (!eventData.isEmpty()) {
                            // In the case of a string-based event, the string data is assumed to include the termination character.
                            if (eventType >= 0x01 && eventType <= 0x07) {
                                eventStr.clear();
                                for (byte b : eventData) {
                                    if (b == 0) break;
                                    eventStr.add(b);
                                }
                            }

                            switch (eventType) {
                            case 0x01:
                                eventText = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventText:%s".formatted(eventText));
                                break;
                            case 0x02:
                                eventCopyrightNotice = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventCopyrightNotice:%s".formatted(eventCopyrightNotice));
                                break;
                            case 0x03:
                                eventSequenceTrackName = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventSequenceTrackName:%s".formatted(eventSequenceTrackName));
                                break;
                            case 0x04:
                                eventInstrumentName = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventInstrumentName:%s".formatted(eventInstrumentName));
                                break;
                            case 0x05:
                                eventLyric = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventLyric:%s".formatted(eventLyric));
                                chipRegister.midiParams[trkPort.get(trk)].Lyric = eventLyric;
                                break;
                            case 0x06:
                                eventMarker = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventMarker:%s".formatted(eventMarker));
                                break;
                            case 0x07:
                                eventText = new String(toByteArray(eventData), Charset.forName("MS932"));
                                logger.log(Level.TRACE, "eventText:%s".formatted(eventText));
                                break;
                            case 0x21:
                                trkPort.set(trk, eventData.get(0) & 0xff);
                                logger.log(Level.TRACE, "PortPrefix:%s".formatted(trkPort.get(trk)));
                                break;
                            case 0x2f:
                                ptr = trkEndAdr.get(trk);
                                logger.log(Level.TRACE, "End of Track:%s".formatted(ptr));
                                break;
                            case 0x51:
                                int Tempo = eventData.get(0) * 0x10000 + eventData.get(1) * 0x100 + eventData.get(2);
                                // reso Quarter note resolution
                                // tempo Microseconds per quarter note
                                oneSyncTime = (Tempo / (double) reso) * 0.000001;
                                logger.log(Level.TRACE, "Set Tempo:%s".formatted(Tempo));
                                break;
                            case 0x54:
                                logger.log(Level.TRACE, "SMPTE Offset ");
                                break;
                            case 0x58:
                                logger.log(Level.TRACE, "Time Signature");
                                break;
                            case 0x59:
                                logger.log(Level.TRACE, "Key Signature");
                                break;
                            case 0x7f:
                                logger.log(Level.TRACE, "Sequencer Specific Meta-Event ");
                                break;
                            default:
                                logger.log(Level.TRACE, "!! Unknown Meta Event !! eventType:%2x adr:%1x".formatted(eventType, ptr));
                                break;
                            }
                        }
                    } else {
                        if ((cmd & 0x80) != 0) {
                            midiEventBackup.set(trk, (byte) (cmd & 0xff));
                            midiEventChBackup = cmd & 0x0f;
                            midiEvent = midiEventBackup.get(trk);
                            midiEventCh = midiEventChBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), cmd, vgmBuf[ptr], vgmBuf[ptr + 1], vstDelta);
                                //logger.log(Level.TRACE, "V1:%2x V2:%2X ".formatted(vgmBuf[ptr], vgmBuf[ptr + 1]));
                                logger.log(Level.TRACE, "%2x %2x %2x".formatted(cmd, vgmBuf[ptr], vgmBuf[ptr + 1]));
                                ptr += 2;
                            } else {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), cmd, vgmBuf[ptr], vstDelta);
                                //logger.log(Level.TRACE, "V1:%2X V2:-- ".formatted(vgmBuf[ptr]));
                                logger.log(Level.TRACE, "%2x %2x".formatted(cmd, vgmBuf[ptr]));
                                ptr++;
                            }
                        } else {
                            // Running status activated
                            midiEvent = midiEventBackup.get(trk);
                            midiEventCh = midiEventChBackup;

                            if ((midiEvent & 0xf0) != 0xC0 && (midiEvent & 0xf0) != 0xD0) {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), midiEvent, cmd, vgmBuf[ptr], vstDelta);
                                //logger.log(Level.TRACE, "RunSta V1:%2X V2:%2X ".formatted(cmd, vgmBuf[ptr]));
                                logger.log(Level.TRACE, "%2x %2x %2x".formatted(midiEvent, cmd, vgmBuf[ptr]));
                                ptr++;
                            } else {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), midiEvent, cmd, vstDelta);
                                //logger.log(Level.TRACE, "RunSta V1:%2X V2:-- ".formatted(cmd));
                                logger.log(Level.TRACE, "%2x %2x ".formatted(midiEvent, cmd));
                            }
                        }
                    }
                }

                isDelta.set(trk, !isDelta.get(trk));

                musicPtr.set(trk, ptr);
                if (ptr == trkEndAdr.get(trk)) {
                    isEnd.set(trk, true);
                    break;
                }
            }
        }

        if (trksEnd) {
            stopped = true;
        }
    }

    private void sendControl() {

        int endFlg = 0;

        for (int i = 0; i < beforeSend.length; i++) {
            if (beforeSend[i] != null) {
                if (sendControlDelta[i] > 0) {
                    sendControlDelta[i]--;
                    continue;
                }
                if (beforeSend[i].size() == sendControlIndex[i]) {
                    beforeSend[i] = null;
                    endFlg++;
                    continue;
                }

                oneSyncTime = 60.0 / 29.0 / 192.0;

                RCP.CtlSysex csx = beforeSend[i].get(sendControlIndex[i]);
                sendControlDelta[i] = csx.delta;
                chipRegister.sendMIDIout(model, 0, csx.data, vstDelta);

                sendControlIndex[i]++;
            } else {
                endFlg++;
            }

        }

        if (endFlg == beforeSend.length) {
            beforeSend = null;
            vgmFrameCounter = -latency - waitTime;
        }
    }

    private boolean makeBeforeSendCommand() {
        try {
            MidiOutInfo[] infos = chipRegister.getMIDIoutInfo();
            if (infos == null || infos.length < 1) return true;

            beforeSend = new List[infos.length];
            sendControlIndex = new int[infos.length];
            sendControlDelta = new int[infos.length];

            for (int i = 0; i < beforeSend.length; i++) {
                beforeSend[i] = new ArrayList<>();

                // Generate Reset
                switch (infos[i].beforeSendType) {
                case 0: // None
                    break;
                case 1: // GM Reset
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getGMReset());
                    break;
                case 2: // XG Reset
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getXGReset());
                    break;
                case 3: // GS Reset
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getGSReset());
                    break;
                case 4: // Custom
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getCustom());
                    break;
                }

            }

            return true;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return false;
        }
    }

    private void getCtlSysexFromText(List<RCP.CtlSysex> buf, String text) {
        if (text == null || text.isEmpty()) return;

        String[] cmds = text.split(";");

        for (String cmd : cmds) {
            String[] com = cmd.split(":");
            int delay = Integer.parseInt(com[0]);
            String[] dats = com[1].split(",");
            byte[] dat = new byte[dats.length];
            for (int i = 0; i < dats.length; i++) {
                dat[i] = (byte) Short.parseShort(dats[i], 16);
            }
            buf.add(new RCP.CtlSysex(delay, dat));
        }
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("This driver does not require this method");
    }
}
