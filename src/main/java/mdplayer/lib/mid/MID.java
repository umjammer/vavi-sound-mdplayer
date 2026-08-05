package mdplayer.lib.mid;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import vavi.util.ByteUtil;
import vavi.util.compat.QuadConsumer;
import vavi.util.compat.TriConsumer;

import static java.lang.System.getLogger;


public class MID {

    private static final Logger logger = getLogger(MID.class.getName());

    public static final int FCC_MID = 0x6468544d;
    public static final int FCC_TRK = 0x6b72544d;
    public int format = 0;
    public int trkCount = 0;
    public int reso = 196;
    public int sampleRate;

    private double oneSyncTime = 0.0001;
    public double musicStep;
    private double musicDownCounter = 0.0;

    public interface Sysex {
        int getDelta();
        byte[] getData();
    }

    public List<Sysex>[] beforeSend = null;
    public int[] sendControlDelta = null;
    public int[] sendControlIndex = null;

    private List<Integer> musicPtr = null;
    private List<Integer> trkEndAdr = null;
    private List<Integer> trkPort = null;
    private List<Integer> midWaitCounter = null;
    private List<Boolean> isEnd = null;
    private List<Boolean> isDelta = null;

    public byte midiEvent = 0;
    List<Byte> midiEventBackup = null;
    public int midiEventCh = 0;
    public int midiEventChBackup = 0;
    private final List<Byte> eventStr = new ArrayList<>();
    private String eventText = "";
    private String eventCopyrightNotice = "";
    private String eventSequenceTrackName = "";
    private String eventInstrumentName = "";
    private String eventLyric = "";
    private String eventMarker = "";

    public QuadConsumer<Integer, Byte, Byte, Byte> send3;
    public TriConsumer<Integer, Byte, Byte> send2;
    public BiConsumer<Integer, byte[]> send0;
    public BiConsumer<Integer, String> lyric;
    public Runnable stop;
    public Runnable counter;
    public Charset charset;

    public void getInformationHeader(byte[] data) {
        if (data == null) throw new IllegalArgumentException("null buffer");
        if (ByteUtil.readLeInt(data, 0) != FCC_MID) throw new IllegalArgumentException("invalid midi data");

        format = (data[8] & 0xff) * 0x100 + (data[9] & 0xff);
        trkCount = (data[10] & 0xff) * 0x100 + (data[11] & 0xff);
        reso = (data[12] & 0xff) * 0x100 + (data[13] & 0xff);

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

            if (ByteUtil.readLeInt(data, adr) != FCC_TRK) throw new IllegalArgumentException("invalid track data");
            int len = (data[adr + 4] & 0xff) * 0x100_0000 + (data[adr + 5] & 0xff) * 0x1_0000 + (data[adr + 6] & 0xff) * 0x100 + (data[adr + 7] & 0xff);
            adr += 8;
            musicPtr.add(adr);
            adr += len;
            trkEndAdr.add(adr);
            trkPort.add(0);
            midiEventBackup.add((byte) 0);
        }
    }

    public void oneFrameMain(byte[] data) {
        try {
            musicStep = sampleRate * oneSyncTime;

            if (musicDownCounter <= 0.0) {
                if (beforeSend != null) {
                    sendControl();
                } else {
                    oneFrameMID(data);
                }
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameMID(byte[] data) {
//#if DEBUG
//        if (model == EnmModel.VirtualModel) return;
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
                    delta = getDelta(ptr, data);
                    midWaitCounter.set(trk, delta);

                    logger.log(Level.TRACE, "delta:%10d ".formatted(delta));
                } else {
                    byte cmd = data[ptr++];

                    //logger.log(Level.TRACE, "cmd:%2x ".formatted(delta, cmd));

                    if ((cmd & 0xff) == 0xf0 || (cmd & 0xff) == 0xf7) {
                        int eventLen = getDelta(ptr, data);
                        //logger.log(Level.TRACE, "evntLen:%10D ".formatted(eventLen));
                        logger.log(Level.TRACE, "%2x ".formatted(cmd));
                        List<Byte> eventData = new ArrayList<>();
                        eventData.add(cmd);
                        for (int j = 0; j < eventLen; j++) {
                            eventData.add(data[ptr + j]);
                            logger.log(Level.TRACE, "%2x ".formatted(data[ptr + j]));
                        }

                        send0.accept(trkPort.get(trk), ByteUtil.toByteArray(eventData));

                        ptr = ptr + eventLen;

                    } else if ((cmd & 0xff) == 0xff) {
                        byte eventType = data[ptr++];
                        int eventLen = getDelta(ptr, data);

                        logger.log(Level.TRACE, "evntTyp:%2x evntLen:%10d ".formatted(eventType, eventLen));

                        List<Byte> eventData = new ArrayList<>();
                        for (int j = 0; j < eventLen; j++) {
                            eventData.add(data[ptr + j]);
                            logger.log(Level.TRACE, "%2x ".formatted(data[ptr + j]));
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
                                eventText = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventText:%s".formatted(eventText));
                                break;
                            case 0x02:
                                eventCopyrightNotice = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventCopyrightNotice:%s".formatted(eventCopyrightNotice));
                                break;
                            case 0x03:
                                eventSequenceTrackName = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventSequenceTrackName:%s".formatted(eventSequenceTrackName));
                                break;
                            case 0x04:
                                eventInstrumentName = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventInstrumentName:%s".formatted(eventInstrumentName));
                                break;
                            case 0x05:
                                eventLyric = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventLyric:%s".formatted(eventLyric));
                                lyric.accept(trkPort.get(trk), eventLyric);
                                break;
                            case 0x06:
                                eventMarker = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventMarker:%s".formatted(eventMarker));
                                break;
                            case 0x07:
                                eventText = new String(ByteUtil.toByteArray(eventData), charset);
                                logger.log(Level.TRACE, "eventText:%s".formatted(eventText));
                                break;
                            case 0x21:
                                trkPort.set(trk, eventData.getFirst() & 0xff);
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
                                send3.accept(trkPort.get(trk), cmd, data[ptr], data[ptr + 1]);
                                //logger.log(Level.TRACE, "V1:%2x V2:%2X ".formatted(data[ptr], data[ptr + 1]));
                                logger.log(Level.TRACE, "%2x %2x %2x".formatted(cmd, data[ptr], data[ptr + 1]));
                                ptr += 2;
                            } else {
                                send2.accept(trkPort.get(trk), cmd, data[ptr]);
                                //logger.log(Level.TRACE, "V1:%2X V2:-- ".formatted(data[ptr]));
                                logger.log(Level.TRACE, "%2x %2x".formatted(cmd, data[ptr]));
                                ptr++;
                            }
                        } else {
                            // Running status activated
                            midiEvent = midiEventBackup.get(trk);
                            midiEventCh = midiEventChBackup;

                            if ((midiEvent & 0xf0) != 0xC0 && (midiEvent & 0xf0) != 0xD0) {
                                send3.accept(trkPort.get(trk), midiEvent, cmd, data[ptr]);
                                //logger.log(Level.TRACE, "RunSta V1:%2X V2:%2X ".formatted(cmd, data[ptr]));
                                logger.log(Level.TRACE, "%2x %2x %2x".formatted(midiEvent, cmd, data[ptr]));
                                ptr++;
                            } else {
                                send2.accept(trkPort.get(trk), midiEvent, cmd);
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
            stop.run();
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

                Sysex csx = beforeSend[i].get(sendControlIndex[i]);
                sendControlDelta[i] = csx.getDelta();
                send0.accept(0, csx.getData());

                sendControlIndex[i]++;
            } else {
                endFlg++;
            }

        }

        if (endFlg == beforeSend.length) {
            beforeSend = null;
            counter.run();
        }
    }

    public void getCtlSysexFromText(List<Sysex> buf, String text, BiFunction<Integer, byte[], Sysex> factory) {
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
            buf.add(factory.apply(delay, dat));
        }
    }

    public static int getDelta(int trkPtr, byte[] bs) {
        int delta = 0;
        while (true) {
            delta = (delta << 7) + (bs[trkPtr] & 0x7f);
            if ((bs[trkPtr] & 0x80) == 0) {
                trkPtr++;
                break;
            }
            trkPtr++;
        }

        return delta;
    }
}
