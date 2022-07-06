package mdplayer.driver.mid;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.Vgm;
import mdplayer.driver.rcp.RCP;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.Log;
import mdplayer.MidiOutInfo;
import vavi.util.Debug;


public class MID extends BaseDriver {

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
    byte midiEventCh = 0;
    byte midiEventChBackup = 0;
    private List<Byte> eventStr = new ArrayList<>();
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
            if (Common.getLE32(buf, 0) != FCC_MID) return null;
            int format = buf[8] * 0x100 + buf[9];
            int trkCount = buf[10] * 0x100 + buf[11];
            int adr = 14;
            byte midiEventBackup = 0;

            for (int i = 0; i < trkCount; i++) {
                if (buf.length <= adr) break;

                if (Common.getLE32(buf, adr) != FCC_TRK) return null;
                int len = buf[adr + 4] * 0x1000000 + buf[adr + 5] * 0x10000 + buf[adr + 6] * 0x100 + buf[adr + 7];
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
                        if (eventData.size() > 0) {
                            switch (eventType) {
                            case 0x01:
                                //case 0x02:
                                if (T01TrackName.isEmpty()) {
                                    T01TrackName = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932")).trim();
                                }
                                break;
                            case 0x03:
                                if (gd3.trackName.isEmpty()) {
                                    if (format == 0 || (format == 1 && i == 0)) {
                                        gd3.trackName = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932")).trim();
                                        gd3.trackNameJ = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932")).trim();
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
                             // ランニングステータス発動
                            midiEvent = midiEventBackup;
                            midiEventCh = midiEventChBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                adr++;
                            }
                        }
                    }

                }
            }

             // タイトルが見つからなかった場合
            if (gd3.trackName.isEmpty() && gd3.trackNameJ.isEmpty() && !T01TrackName.isEmpty()) {
                gd3.trackName = T01TrackName;
                gd3.trackNameJ = T01TrackName;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
         // コントロールを送信してからウェイトするためここでは0をセットする
        //vgmFrameCounter = -latency - waitTime;
        vgmFrameCounter = 0;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        gd3 = getGD3Info(vgmBuf);
        //if (Gd3 == null) return false;

        if (!getInformationHeader()) return false;

         // ポートごとに事前に送信するコマンドを作成する
        if (!makeBeforeSendCommand()) return false;

        if (model == EnmModel.RealModel) {
            chipRegister.setYM2612SyncWait((byte) 0, 1);
            chipRegister.setYM2612SyncWait((byte) 1, 1);
        }

        return true;
    }

    private boolean getInformationHeader() {
        if (vgmBuf == null) return false;
        if (Common.getLE32(vgmBuf, 0) != FCC_MID) return false;

        format = vgmBuf[8] * 0x100 + vgmBuf[9];
        trkCount = vgmBuf[10] * 0x100 + vgmBuf[11];
        reso = vgmBuf[12] * 0x100 + vgmBuf[13];

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

            if (Common.getLE32(vgmBuf, adr) != FCC_TRK) return false;
            int len = vgmBuf[adr + 4] * 0x1000000 + vgmBuf[adr + 5] * 0x10000 + vgmBuf[adr + 6] * 0x100 + vgmBuf[adr + 7];
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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }

    private void oneFrameMID() {
// #if DEBUG
        if (model == EnmModel.VirtualModel) return;
// #endif
        boolean trksEnd = true;
        for (int trk = 0; trk < trkCount; trk++) {
            if (isEnd.get(trk)) continue;

            trksEnd = false;
            midWaitCounter.set(trk, midWaitCounter.get(trk) - 1);
            if (midWaitCounter.get(trk) > 0) continue;

            while (midWaitCounter.get(trk) < 1) {
                int ptr = musicPtr.get(trk);
                int delta;
// #if DEBUG
                System.err.println();
                System.err.printf("ptr:[%08x] trk:[%2d] ", ptr, trk);
// #endif

                if (isDelta.get(trk)) {
                    delta = Common.getDelta(ptr, vgmBuf);
                    midWaitCounter.set(trk, delta);

// #if DEBUG
                    System.err.printf("delta:%10d ", delta);
// #endif
                } else {
                    byte cmd = vgmBuf[ptr++];

// #if DEBUG
                    //System.err.printf("cmd:%2x ", delta, cmd);
// #endif

                    if ((cmd & 0xff) == 0xf0 || (cmd & 0xff) == 0xf7) {
                        int eventLen = Common.getDelta(ptr, vgmBuf);
// #if DEBUG
                        //System.err.printf("evntLen:%10D ", eventLen);
                        System.err.printf("%2x ", cmd);
// #endif
                        List<Byte> eventData = new ArrayList<>();
                        eventData.add(cmd);
                        for (int j = 0; j < eventLen; j++) {
                            eventData.add(vgmBuf[ptr + j]);
// #if DEBUG
                            System.err.printf("%2x ", vgmBuf[ptr + j]);
// #endif
                        }

                        chipRegister.sendMIDIout(model, trkPort.get(trk), mdsound.Common.toByteArray(eventData), vstDelta);

                        ptr = ptr + eventLen;

                    } else if ((cmd & 0xff) == 0xff) {
                        byte eventType = vgmBuf[ptr++];
                        int eventLen = Common.getDelta(ptr, vgmBuf);

// #if DEBUG
                        System.err.printf("evntTyp:%2x evntLen:%10d ", eventType, eventLen);
// #endif

                        List<Byte> eventData = new ArrayList<>();
                        for (int j = 0; j < eventLen; j++) {
                            eventData.add(vgmBuf[ptr + j]);
// #if DEBUG
                            System.err.printf("%2x ", vgmBuf[ptr + j]);
// #endif
                        }
                        ptr = ptr + eventLen;
                        if (eventData.size() > 0) {
                             // 文字列系のイベントの場合は終端文字までを文字列のデータとする。
                            if (eventType >= 0x01 && eventType <= 0x07) {
                                eventStr.clear();
                                for (byte b : eventData) {
                                    if (b == 0) break;
                                    eventStr.add(b);
                                }
                            }

                            switch (eventType) {
                            case 0x01:
                                eventText = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventText:%s", eventText);
// #endif
                                break;
                            case 0x02:
                                eventCopyrightNotice = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventCopyrightNotice:%s", eventCopyrightNotice);
// #endif
                                break;
                            case 0x03:
                                eventSequenceTrackName = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventSequenceTrackName:%s", eventSequenceTrackName);
// #endif
                                break;
                            case 0x04:
                                eventInstrumentName = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventInstrumentName:%s", eventInstrumentName);
// #endif
                                break;
                            case 0x05:
                                eventLyric = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventLyric:%s", eventLyric);
// #endif
                                chipRegister.midiParams[trkPort.get(trk)].Lyric = eventLyric;
                                break;
                            case 0x06:
                                eventMarker = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventMarker:%s", eventMarker);
// #endif
                                break;
                            case 0x07:
                                eventText = new String(mdsound.Common.toByteArray(eventData), Charset.forName("MS932"));
// #if DEBUG
                                System.err.printf("eventText:%s", eventText);
// #endif
                                break;
                            case 0x21:
                                trkPort.set(trk, eventData.get(0) & 0xff);
// #if DEBUG
                                System.err.printf("PortPrefix:%s", trkPort.get(trk));
// #endif
                                break;
                            case 0x2f:
                                ptr = trkEndAdr.get(trk);
// #if DEBUG
                                System.err.printf("End of Track:%s", ptr);
// #endif
                                break;
                            case 0x51:
                                int Tempo = eventData.get(0) * 0x10000 + eventData.get(1) * 0x100 + eventData.get(2);
                                // reso 4分音符当たりの分解能
                                // tempo 4分音符当たりのマイクロ秒
                                oneSyncTime = (double) (Tempo / reso) * 0.000001;
// #if DEBUG
                                System.err.printf("Set Tempo:%s", Tempo);
// #endif
                                break;
                            case 0x54:
// #if DEBUG
                                System.err.print("SMPTE Offset ");
// #endif
                                break;
                            case 0x58:
// #if DEBUG
                                System.err.print("Time Signature");
// #endif
                                break;
                            case 0x59:
// #if DEBUG
                                System.err.print("Key Signature");
// #endif
                                break;
                            case 0x7f:
// #if DEBUG
                                System.err.print("Sequencer Specific Meta-Event ");
// #endif
                                break;
                            default:
// #if DEBUG
                                System.err.printf("!! Unknown Meta Event !! eventType:%2x Adr:%1x", eventType, ptr);
// #endif
                                break;
                            }
                        }
                    } else {
                        if ((cmd & 0x80) != 0) {
                            midiEventBackup.set(trk, (byte) (cmd & 0xff));
                            midiEventChBackup = (byte) (cmd & 0x0f);
                            midiEvent = midiEventBackup.get(trk);
                            midiEventCh = midiEventChBackup;

                            if ((cmd & 0xf0) != 0xC0 && (cmd & 0xf0) != 0xD0) {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), cmd, vgmBuf[ptr], vgmBuf[ptr + 1], vstDelta);
// #if DEBUG
                                //System.err.printf("V1:%2x V2:%2X ", vgmBuf[ptr], vgmBuf[ptr + 1]);
                                System.err.printf("%2x %2x %2x", cmd, vgmBuf[ptr], vgmBuf[ptr + 1]);
// #endif
                                ptr += 2;
                            } else {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), cmd, vgmBuf[ptr], vstDelta);
// #if DEBUG
                                //System.err.printf("V1:%2X V2:-- ", vgmBuf[ptr]);
                                System.err.printf("%2x %2x", cmd, vgmBuf[ptr]);
// #endif
                                ptr++;
                            }
                        } else {
                             // ランニングステータス発動
                            midiEvent = midiEventBackup.get(trk);
                            midiEventCh = midiEventChBackup;

                            if ((midiEvent & 0xf0) != 0xC0 && (midiEvent & 0xf0) != 0xD0) {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), midiEvent, cmd, vgmBuf[ptr], vstDelta);
// #if DEBUG
                                //System.err.printf("RunSta V1:%2X V2:%2X ", cmd, vgmBuf[ptr]);
                                System.err.printf("%2x %2x %2x", midiEvent, cmd, vgmBuf[ptr]);
// #endif
                                ptr++;
                            } else {
                                chipRegister.sendMIDIout(model, trkPort.get(trk), midiEvent, cmd, vstDelta);
// #if DEBUG
                                //System.err.printf("RunSta V1:%2X V2:-- ", cmd);
                                System.err.printf("%2x %2x ", midiEvent, cmd);
// #endif
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
            MidiOutInfo[] infos = chipRegister.GetMIDIoutInfo();
            if (infos == null || infos.length < 1) return true;

            beforeSend = new List[infos.length];
            sendControlIndex = new int[infos.length];
            sendControlDelta = new int[infos.length];

            for (int i = 0; i < beforeSend.length; i++) {
                beforeSend[i] = new ArrayList<>();

                 // リセットを生成
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
            e.printStackTrace();
            return false;
        }
    }

    private void getCtlSysexFromText(List<RCP.CtlSysex> buf, String text) {
        if (text == null || text.length() < 1) return;

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
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }
}
