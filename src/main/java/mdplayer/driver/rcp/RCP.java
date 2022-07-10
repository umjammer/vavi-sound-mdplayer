package mdplayer.driver.rcp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;

import dotnet4j.util.compat.Tuple;
import dotnet4j.io.Path;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.rcp.MIDIEvent.MIDIEventType;
import mdplayer.driver.rcp.MIDIEvent.MIDISpEventType;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.MidiOutInfo;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;


public class RCP extends BaseDriver {

    public static final Charset CHARSET = Charset.forName("Shift_JIS");

    public RCP() {
        musicStep = Common.VGMProcSampleRate / 60.0;
    }

    private double oneSyncTime = 0.009;
    private double musicStep = 1;
    private double musicDownCounter = 0.0;

    private List<CtlSysex>[] beforeSend = null;
    private int[] sendControlDelta = null;
    private int[] sendControlIndex = null;

    public static class MIDIRythm {
        private String name = "";
        private int key = 0;
        private int gt = 1;

        public void setName(String value) {
            name = value;
        }

        public String getName() {
            return name;
        }

        public void setKey(int value) {
            key = value;
        }

        public int getKey() {
            return key;
        }

        public void setGt(int value) {
            gt = value;
        }

        public int getGt() {
            return gt;
        }
    }

    public static class MIDIUserExclusive {
        private String name = "";
        private String memo = "";
        private byte[] exclusive = null;

        public void setName(String value) {
            name = value;
        }

        public String getName() {
            return name;
        }

        public void setMemo(String value) {
            memo = value;
        }

        public String getMemo() {
            return memo;
        }

        public void setExclusive(byte[] value) {
            exclusive = value;
        }

        public byte[] getExclusive() {
            return exclusive;
        }
    }

    public static class Tick {
        public int millisec = 0;
        public int count = 0;
        public int before = 0;
        public int sabun = 0;
    }

    private Tick tick = new Tick();

    public List<Tuple<String, byte[]>> ExtendFile = null;

    /**
     *
     * @param buf
     * @param cm6 OUT
     * @param gsd OUT
     * @param gsd2 OUT
     */
    public static void getControlFileName(byte[] buf, String[] cm6, String[] gsd, String[] gsd2) {
        Boolean ret = checkHeadString(buf);
        if (ret == null) return;
        boolean isG36 = ret;
        int ptr = 96;
        if (isG36) {
            ptr += 568;
            // .gsd
            gsd[0] = new String(buf, ptr, 12, CHARSET).replace("\0", "");
            ptr += 16;
            // .gsd
            gsd2[0] = new String(buf, ptr, 12, CHARSET).replace("\0", "");
            ptr += 16;
            // .cm6
            cm6[0] = new String(buf, ptr, 12, CHARSET).replace("\0", "");
        } else {
            ptr += 358;
            // .cm6
            cm6[0] = new String(buf, ptr, 12, CHARSET).replace("\0", "");
            ptr += 16;
            // .gsd
            gsd[0] = new String(buf, ptr, 12, CHARSET).replace("\0", "");
        }
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (buf == null) return null;
        Boolean ret = checkHeadString(buf);
        if (ret == null) return null;
        boolean isG36 = ret;

        Gd3 gd3 = new Gd3();
        int ptr = 32;
        StringBuilder str;

        List<Byte> title = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            if (buf[ptr + i] == 0) break;
            title.add(buf[ptr + i]);
        }
        str = new StringBuilder(new String(toByteArray(title), CHARSET).trim());
        ptr += 64;
        gd3.trackName = str.toString();
        gd3.trackNameJ = str.toString();

        if (isG36) {
            ptr += 64;
            str = new StringBuilder(String.format("%s\n", new String(buf, ptr, 360, CHARSET).replace("\0", "")));
        } else {
            str = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                str.append(String.format("%s\n", new String(buf, ptr + i * 28, 28, CHARSET).replace("\0", "")));
            }
        }
        gd3.notes = str.toString();

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
        // コントロールを送信してからウェイトするためここでは 0 をセットする
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

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, EnmModel model, EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
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
            // Stopped = !IsPlaying();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isG36 = false;
    private int ptr = 0;
    private int trkLen = 0;
    private int timeBase = 1;
    private double nowTempo = 120;
    private double tempo = 0;
    private int beatDen = 0;
    private int beatMol = 0;
    private int key = 0;
    private int playBIAS = 0;
    private String controlFileGSD = "";
    private String controlFileGSD2 = "";
    private String controlFileCM6 = "";
    private int rcpVer = 0;
    private List<MIDIRythm> rythms;
    private List<MIDIUserExclusive> userExclusives;
    private MIDITrack[] tracks = null;
    private MIDIPart[] parts = null;

    // private int trkTick = 0;
    // private int meaTick = 0;
    private int meaInd = 0;
    private boolean endTrack = false;
    private Map<Byte, Byte> taiDic = null;
    private int stDevNum = 0;
    private int pt = 0;
    private int skipPtr = 4;
    private byte[] msgBuf2 = new byte[2];
    private byte[] msgBuf3 = new byte[3];
    private byte[] msgBuf = new byte[256];

    interface EventHandler extends BiConsumer<MIDITrack, MIDIEvent> {
    }

    private EventHandler[] eventFunc = new EventHandler[256];
    private EventHandler[] specialEventFunc = new EventHandler[256];
    private int relativeTempoChangeTargetTempo;
    private double relativeTempoChangeTickSlice;
    private boolean relativeTempoChangeSW = false;

    /** @return tri-state (nullable boolean) */
    private static Boolean checkHeadString(byte[] buf) {
        if (buf == null || buf.length < 32) return null;

        String str = new String(buf, 0, 32, CHARSET);
        if (!str.equals("RCM-PC98V2.0(C)COME ON MUSIC\n\0\0")) {
            if (!str.equals("COME ON MUSIC RECOMPOSER RCP3.0\0")) return null;
            else return true;
        }

        return false;
    }

    private boolean getInformationHeader() {
        Boolean ret = checkHeadString(vgmBuf);
        if (ret == null) return false;
        isG36 = ret;

        ptr = 32;
        ptr += 64;

        if (isG36) {
            headerG36();
        } else {
            headerRcp();
        }

        nowTempo = nowTempo == 0 ? 1 : nowTempo;
        timeBase = timeBase == 0 ? 1 : timeBase;
        oneSyncTime = 60.0 / nowTempo / timeBase;

        rythm();

        userEx();

        trackData();

        init();

        eventFunc[0x00] = this::efMetaSeqNumber;
        eventFunc[0x01] = this::efMetaTextEvent;
        eventFunc[0x02] = this::efMetaCopyrightNotice;
        eventFunc[0x03] = this::efMetaTrackName;
        eventFunc[0x04] = this::efMetaInstrumentName;
        eventFunc[0x05] = this::efMetaLyric;
        eventFunc[0x06] = this::efMetaMarker;
        eventFunc[0x07] = this::efMetaCuePoint;
        eventFunc[0x08] = this::efMetaProgramName;
        eventFunc[0x09] = this::efMetaDeviceName;
        eventFunc[0x20] = this::efMetaChannelPrefix;
        eventFunc[0x21] = this::efMetaPortPrefix;
        eventFunc[0x2f] = this::efMetaEndOfTrack;
        eventFunc[0x51] = this::efMetaTempo;
        eventFunc[0x54] = this::efMetaSMPTEOffset;
        eventFunc[0x58] = this::efMetaTimeSignature;
        eventFunc[0x59] = this::efMetaKeySignature;
        eventFunc[0x7f] = this::efMetaSequencerSpecific;
        eventFunc[0x80] = this::efNoteOff;
        eventFunc[0x90] = this::efNoteOn;
        eventFunc[0xa0] = this::efKeyAfterTouch;
        eventFunc[0xb0] = this::efControlChange;
        eventFunc[0xc0] = this::efProgramChange;
        eventFunc[0xd0] = this::efChannelAfterTouch;
        eventFunc[0xe0] = this::efPitchBend;
        eventFunc[0xf0] = this::efSysExStart;
        eventFunc[0xf7] = this::efSysExContinue;
        for (int i = 0; i < 256; i++) {
            if (eventFunc[i] == null) eventFunc[i] = this::efn;
        }

        specialEventFunc[0x90] = this::sefUserExclusive1;
        specialEventFunc[0x91] = this::sefUserExclusive2;
        specialEventFunc[0x92] = this::sefUserExclusive3;
        specialEventFunc[0x93] = this::sefUserExclusive4;
        specialEventFunc[0x94] = this::sefUserExclusive5;
        specialEventFunc[0x95] = this::sefUserExclusive6;
        specialEventFunc[0x96] = this::sefUserExclusive7;
        specialEventFunc[0x97] = this::sefUserExclusive8;
        specialEventFunc[0x98] = this::sefChExclusive;
        specialEventFunc[0x99] = this::sefOutsideProcessExec;
        specialEventFunc[0xc0] = this::sefDX7Func;
        specialEventFunc[0xc1] = this::sefDXPara;
        specialEventFunc[0xc2] = this::sefDXRERF;
        specialEventFunc[0xc3] = this::sefTXFunc;
        specialEventFunc[0xc5] = this::sefFB01PPara;
        specialEventFunc[0xc6] = this::sefFB01SSystem;
        specialEventFunc[0xc7] = this::sefTX81ZVVCED;
        specialEventFunc[0xc8] = this::sefTX81ZAACED;
        specialEventFunc[0xc9] = this::sefTX81ZPPCED;
        specialEventFunc[0xca] = this::sefTX81ZSSystem;
        specialEventFunc[0xcb] = this::sefTX81ZEEffect;
        specialEventFunc[0xcc] = this::sefDX72RRemoteSW;
        specialEventFunc[0xcd] = this::sefDX72AACED;
        specialEventFunc[0xce] = this::sefDX72PPCED;
        specialEventFunc[0xcf] = this::sefTX802PPCED;
        specialEventFunc[0xd0] = this::sefYAMAHABase;
        specialEventFunc[0xd1] = this::sefYAMAHADev;
        specialEventFunc[0xd2] = this::sefYAMAHAAddrPara;
        specialEventFunc[0xd3] = this::sefYAMAHAXGAddrPara;
        specialEventFunc[0xdc] = this::sefMKS7;
        specialEventFunc[0xdd] = this::sefRolandBase;
        specialEventFunc[0xde] = this::sefRolandPara;
        specialEventFunc[0xdf] = this::sefRolandDev;
        specialEventFunc[0xe2] = this::sefBankProgram;
        specialEventFunc[0xe5] = this::sefKeyScan;
        specialEventFunc[0xe6] = this::sefMIDIChChange;
        specialEventFunc[0xe7] = this::sefTempoChange;
        specialEventFunc[0xf5] = this::sefKeyChange;
        specialEventFunc[0xf6] = this::sefCommentStart;
        specialEventFunc[0xf8] = this::sefLoopEnd;
        specialEventFunc[0xf9] = this::sefLoopStart;
        specialEventFunc[0xfc] = this::sefSameMeasure;
        specialEventFunc[0xfd] = this::sefMeasureEnd;
        specialEventFunc[0xfe] = this::sefEndofTrack;
        for (int i = 0; i < 256; i++) {
            if (specialEventFunc[i] == null) specialEventFunc[i] = this::efn;
        }

        return true;
    }

    private void headerG36() {
        // dummy Skip
        ptr += 64;
        // Memo
        ptr += 360;
        // トラック数
        trkLen = vgmBuf[ptr++];
        if (trkLen != 18 && trkLen != 36) trkLen = 18;
        // dummy Skip
        ptr++;
        // Timebase
        timeBase = vgmBuf[ptr] + (vgmBuf[ptr + 1] * 0x100);
        timeBase = timeBase == 0 ? 1 : timeBase;
        ptr += 2;
        // Tempo
        nowTempo = vgmBuf[ptr++];
        if (nowTempo < 8 || nowTempo > 250) nowTempo = 120;
        tempo = nowTempo;
        // dummy Skip
        ptr++;
        // 拍子（分子）
        beatDen = vgmBuf[ptr++];
        // 拍子（分母）
        beatMol = vgmBuf[ptr++];
        // Key
        key = vgmBuf[ptr++];
        // Play BIAS
        playBIAS = vgmBuf[ptr++];
        // dummy Skip
        ptr += 6;
        // dummy Skip
        ptr += 16;
        // dummy Skip
        ptr += 112;
        // .GSD
        controlFileGSD = new String(vgmBuf, ptr, 12, CHARSET).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // .GSD
        controlFileGSD2 = new String(vgmBuf, ptr, 12, CHARSET).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // .CM6
        controlFileCM6 = new String(vgmBuf, ptr, 12, CHARSET).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // dummy Skip
        ptr += 80;
        rcpVer = 0;
    }

    private void headerRcp() {
        // Memo
        ptr += 336;
        // dummy Skip
        ptr += 16;
        // Timebase下位
        timeBase = vgmBuf[ptr++];
        // Tempo
        nowTempo = vgmBuf[ptr++];
        tempo = nowTempo;
        // 拍子（分子）
        beatDen = vgmBuf[ptr++];
        // 拍子（分母）
        beatMol = vgmBuf[ptr++];
        // Key
        key = vgmBuf[ptr++];
        // Play BIAS
        playBIAS = vgmBuf[ptr++];
        // .CM6
        controlFileCM6 = new String(vgmBuf, ptr, 12, CHARSET).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // .GSD
        controlFileGSD = new String(vgmBuf, ptr, 12, CHARSET).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // トラック数
        trkLen = vgmBuf[ptr++];
        switch (trkLen) {
        case 0:
            trkLen = 36;
            rcpVer = 0;
            break;
        case 18:
            rcpVer = 1;
            break;
        case 36:
            rcpVer = 2;
            break;
        }
        // Timebase上位
        timeBase += vgmBuf[ptr++] * 0x100;
        timeBase = timeBase == 0 ? 1 : timeBase;
        // dummy Skip
        // 無視
        // TONENAME.TB?
        // いまのところ無視
        ptr = 0x206; // リズム定義部まで
    }

    private void rythm() {
        rythms = new ArrayList<>();
        int n = 32;
        if (isG36) n = 128;

        for (int i = 0; i < n; i++) {
            MIDIRythm r = new MIDIRythm();
            r.setName(new String(vgmBuf, ptr, 14, CHARSET).replace("\0", ""));
            ptr += 14;
            r.key = vgmBuf[ptr++];
            r.gt = vgmBuf[ptr++];
            rythms.add(r);
        }
    }

    private void userEx() {
        userExclusives = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            MIDIUserExclusive ux = new MIDIUserExclusive();
            ux.setName(new String(vgmBuf, ptr, 24, CHARSET).replace("\0", ""));
            ux.memo = ux.name;
            ptr += 24;
            ux.exclusive = new byte[25];
            ux.exclusive[0] = (byte) 0xf0;
            for (int j = 1; j < 25; j++) {
                ux.exclusive[j] = vgmBuf[ptr++];
            }
            userExclusives.add(ux);
        }
    }

    private void trackData() {
        initTrkPrt(); // トラックと小節を準備する

        for (int i = 0; i < trkLen; i++) {
            if (ptr >= vgmBuf.length) continue;

            int vgmBufptr = ptr;
            int trkSize = vgmBuf[ptr++] * 0x100 + vgmBuf[ptr++];

            // size dummy(?) skip
            if (isG36) ptr += 2;

            int trkNumber = 0;
            if (isG36) {
                trkNumber = i;
                ptr++;
            } else {
                trkNumber = i;
                ptr++;
                if (trkNumber < 0)
                    trkNumber = i;
            }
            tracks[trkNumber].setRythmMode(vgmBuf[ptr++] == 0x80);
            int ch = vgmBuf[ptr++];
            if (ch != 255) {
                int mc = chipRegister.getMIDIoutCount();
                if (mc == 0) mc = 1;
                int n = (stDevNum + (ch / 16)) % mc;
                tracks[trkNumber].setOutDeviceName("dummy");
                tracks[trkNumber].setOutDeviceNumber(n);
                tracks[trkNumber].setOutUserDeviceNumber(n);
                tracks[trkNumber].setOutUserDeviceName("dummy");
                tracks[trkNumber].setOutChannel(ch % 16);
            } else {
                tracks[trkNumber].setOutDeviceName("Null Device");
                tracks[trkNumber].setOutDeviceNumber(null);
                tracks[trkNumber].setOutUserDeviceNumber(null);
                tracks[trkNumber].setOutUserDeviceName("Null Device");
                tracks[trkNumber].setOutChannel(null);
            }

            tracks[trkNumber].setInDeviceName("Null Device");
            tracks[trkNumber].setInDeviceNumber(null);
            tracks[trkNumber].setInUserDeviceNumber(null);
            tracks[trkNumber].setInUserDeviceName("Null Device");
            tracks[trkNumber].setInChannel(null);

            tracks[trkNumber].setKey(vgmBuf[ptr++]);

            if ((tracks[trkNumber].getKey() & 0x80) == 0x80) {
                tracks[trkNumber].setKey(0);
            } else {
                tracks[trkNumber].setKey((tracks[trkNumber].getKey() > 63) ? tracks[trkNumber].getKey() - 128 : tracks[trkNumber].getKey());
            }
            tracks[trkNumber].setSt(vgmBuf[ptr++]);
            if (rcpVer > 0) {
                tracks[trkNumber].setSt((tracks[trkNumber].getSt() > 127) ? tracks[trkNumber].getSt() - 256 : tracks[trkNumber].getSt());
            }
            tracks[trkNumber].setMute(vgmBuf[ptr++] == 1);
            tracks[trkNumber].setName((new String(vgmBuf, ptr, 36, CHARSET)).replace("\0", ""));
            ptr += 36;

            // trkTick = 0;
            taiDic = new HashMap<>();
            // meaTick = 0;
            meaInd = 0;
            musData(tracks[trkNumber], vgmBuf);
            extractSame(tracks[trkNumber]);
        }
    }

    private void extractSame(MIDITrack trk) {
        MIDIEvent evt = trk.getPart().get(0).getStartEvent();
        while (evt != null) {
            if (evt.getEventType() == MIDIEventType.MetaSequencerSpecific && evt.getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.ordinal()) {

                int ofsMea = 0;
                if (isG36) {
                    ofsMea = evt.getMIDIMessages()[0][0] + evt.getMIDIMessages()[0][2] * 0x100;
                    // if (trkLen == 36)
                    // {
                    //    ofsMea = ofsMea * 6 - 242;
                    // }
                } else {
                    ofsMea = evt.getMIDIMessages()[0][0] + (evt.getMIDIMessages()[0][1] & 3) * 0x100;
                }
                int Mea = 0;
                int MeaS = 0;
                MIDIEvent mEvt = trk.getPart().get(0).getStartEvent();
                if (ofsMea != 0) {
                    while (mEvt != null) {
                        MeaS = 0;
                        if (mEvt.getEventType() == MIDIEventType.MetaSequencerSpecific) {
                            MIDIEvent nEvt = trk.getPart().get(0).getNextEvent(mEvt);
                            int s = 0;
                            if (nEvt.getEventType() == MIDIEventType.MetaSequencerSpecific
                                    && nEvt.getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.ordinal()) {
                                s = 0;
                            } else {
                                s = 1;
                            }
                            if (mEvt.getMIDIMessage()[0] == (byte) MIDISpEventType.MeasureEnd.ordinal()) {
                                MeaS = s;
                            }
                            if (mEvt.getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.ordinal()) {
                                Mea++;
                                if (ofsMea == Mea) break;
                                MeaS = s;
                            }
                        }
                        mEvt = trk.getPart().get(0).getNextEvent(mEvt);
                        Mea += MeaS;
                        if (ofsMea == Mea) break;
                    }
                }

                if (mEvt != null) {
                    evt.setSameMeasureIndex(mEvt.getNumber());
                }
            }

            evt = trk.getPart().get(0).getNextEvent(evt);
        }
    }

    private void musData(MIDITrack trkn, byte[] ebs) {
        endTrack = false;
        pt = ptr;

        while (!endTrack) {
            MIDIEvent pEvt = trkn.getPart().get(meaInd).getEndEvent();
            int[] pk = null;
            if (!isG36) {
                pk = new int[] {ebs[pt], ebs[pt + 1], ebs[pt + 2], ebs[pt + 3]};
            } else {
                // Note   Step   Gate   Vel
                pk = new int[] {ebs[pt], ebs[pt + 2] + ebs[pt + 3] * 0x100, ebs[pt + 4] + ebs[pt + 5] * 0x100, ebs[pt + 1]};
                skipPtr = 6;
            }
            if (pk[0] < 0x80) {
                onpu(trkn, pk, pEvt); // おんぷさんらしい
            } else {
                command(trkn, pk, ebs, pEvt);// コマンドらしい
            }
        }
        ptr = pt;
    }

    private void onpu(MIDITrack trkn, int[] pk, MIDIEvent pEvt) {
        trkn.getPart().get(meaInd).insertEvent(pEvt, pk[1], MIDIEventType.NoteON, new byte[] {(byte) 0x90, (byte) pk[0], (byte) pk[3]}, pk[2]);
        pt += skipPtr;
    }

    private void command(MIDITrack trkn, int[] pk, byte[] ebs, MIDIEvent pEvt) {
        List<Byte> ex = null;
        switch (pk[0]) {
        case 0x98: // CH Exclusive
            pt += skipPtr;
            ex = new ArrayList<>();
            ex.add((byte) 0xF0);
            while (ebs[pt] == 0xf7) {
                if (isG36) {
                    pt++;
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                } else {
                    pt += 2;
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                }
            }
            ex.add((byte) (pk[2] & 0xff));
            // if (IsG36) ex.add((byte)(pk[2] / 0x100));
            ex.add((byte) (pk[3] & 0xff));
            // if (IsG36) ex.add((byte)(pk[3] / 0x100));
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    pk[1],
                    MIDISpEventType.ChExclusive,
                    new byte[][] {
                            toByteArray(ex)
                    });
            break;
        case 0x90: // User Exclusive 1
        case 0x91: // User Exclusive 2
        case 0x92: // User Exclusive 3
        case 0x93: // User Exclusive 4
        case 0x94: // User Exclusive 5
        case 0x95: // User Exclusive 6
        case 0x96: // User Exclusive 7
        case 0x97: // User Exclusive 8
        case 0xc0: // DX7 Function
        case 0xc1: // DX Parameter
        case 0xc2: // DX RERF
        case 0xc3: // TX Function
        case 0xc5: // FB-01 p Parameter
        case 0xc6: // FB-01 s System
        case 0xc7: // TX81Z V VCED
        case 0xc8: // TX81Z a ACED
        case 0xc9: // TX81Z p PCED
        case 0xca: // TX81Z s System
        case 0xcb: // TX81Z E EFFECT
        case 0xcc: // DX7-2 R Remote SW
        case 0xcd: // DX7-2 a ACED
        case 0xce: // DX7-2 p PCED
        case 0xcf: // TX802 p PCED
        case 0xdc: // MKS-7
        case 0xd0: // YAMAHA Base
        case 0xd1: // YAMAHA Dev
        case 0xd2: // YAMAHA Addr/Para
        case 0xd3: // YAMAHA XG Addr/Para
        case 0xdd: // Rol Base
        case 0xde: // Rol Para
        case 0xdf: // Rol Dev
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    pk[1],
                    MIDISpEventType.values()[pk[0]],
                    new byte[][] {
                            new byte[] {(byte) pk[2], (byte) pk[3]}
                    });
            pt += skipPtr;
            break;
        case 0xe2: // Bank & Program
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    pk[1],
                    MIDISpEventType.BankProgram,
                    new byte[][] {
                            new byte[] {(byte) MIDIEventType.ProgramChange.ordinal(), (byte) pk[2]},
                            new byte[] {(byte) MIDIEventType.ControlChange.ordinal(), 0x00, (byte) pk[3]}
                    });
            pt += skipPtr;
            break;
        case 0xe5: // KEY SCAN
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    pk[1],
                    MIDISpEventType.KeyScan,
                    new byte[][] {
                            new byte[] {(byte) pk[2]}
                    });
            pt += skipPtr;
            break;
        case 0xe6: // MIDI CH
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    pk[1],
                    MIDISpEventType.MIDICh,
                    new byte[][] {
                            new byte[] {(byte) pk[2], (byte) pk[3]}
                    });
            pt += skipPtr;
            break;
        case 0xe7: // TempoChange
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    pk[1],
                    MIDISpEventType.TempoChange,
                    new byte[][] {
                            new byte[] {(byte) pk[2], (byte) pk[3]}
                    });
            pt += skipPtr;
            break;
        case 0xea: // After Touch Ch.
            trkn.getPart().get(meaInd).insertEvent(
                    pEvt,
                    pk[1],
                    MIDIEventType.ChannelAfterTouch,
                    new byte[] {(byte) MIDIEventType.ChannelAfterTouch.ordinal(), (byte) pk[2]}
            );
            pt += skipPtr;
            break;
        case 0xeb: // ControlChange
            trkn.getPart().get(meaInd).insertEvent(
                    pEvt,
                    pk[1],
                    MIDIEventType.ControlChange,
                    new byte[] {(byte) MIDIEventType.ControlChange.ordinal(), (byte) pk[2], (byte) pk[3]}
            );
            pt += skipPtr;
            break;
        case 0xec: // Program Change
            trkn.getPart().get(meaInd).insertEvent(
                    pEvt,
                    pk[1],
                    MIDIEventType.ProgramChange,
                    new byte[] {(byte) MIDIEventType.ProgramChange.ordinal(), (byte) pk[2]}
            );
            pt += skipPtr;
            break;
        case 0xed: // After Touch Pori.
            trkn.getPart().get(meaInd).insertEvent(
                    pEvt,
                    pk[1],
                    MIDIEventType.KeyAfterTouch,
                    new byte[] {(byte) MIDIEventType.KeyAfterTouch.ordinal(), (byte) pk[2], (byte) pk[3]}
            );
            pt += skipPtr;
            break;
        case 0xee: // Pitch Bend
            trkn.getPart().get(meaInd).insertEvent(
                    pEvt,
                    pk[1],
                    MIDIEventType.PitchBend,
                    new byte[] {(byte) MIDIEventType.PitchBend.ordinal(), (byte) pk[2], (byte) pk[3]}
            );
            pt += skipPtr;
            break;
        case 0xf5: // Key Change
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    0,
                    MIDISpEventType.KeyChange,
                    new byte[][] {new byte[] {(byte) pk[0]}}
            );
            pt += skipPtr;
            break;
        case 0xf6: // Comment
            pt += skipPtr;
            ex = new ArrayList<>();
            if (isG36) {
                ex.add((byte) (pk[3] & 0xff));
                ex.add((byte) (pk[1] & 0xff));
                ex.add((byte) (pk[1] / 0x100));
                ex.add((byte) (pk[2] & 0xff));
                ex.add((byte) (pk[2] / 0x100));
                while (ebs[pt] == 0xf7) {
                    pt++;
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                }
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        0,
                        MIDISpEventType.Comment,
                        new byte[][] {
                                toByteArray(ex)
                        });
            } else {
                ex.add((byte) pk[2]);
                ex.add((byte) pk[3]);
                while (ebs[pt] == 0xf7) {
                    pt += 2;
                    ex.add(ebs[pt++]);
                    ex.add(ebs[pt++]);
                }
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        pk[1],
                        MIDISpEventType.Comment,
                        new byte[][] {
                                toByteArray(ex)
                        });
            }
            break;
        case 0xf8: // Loop End
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    0,
                    MIDISpEventType.LoopEnd,
                    new byte[][] {new byte[] {(byte) pk[1]}}
            );
            pt += skipPtr;
            break;
        case 0xf9: // Loop Start
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    0,
                    MIDISpEventType.LoopStart,
                    new byte[][] {new byte[] {0}}
            );
            pt += skipPtr;
            break;
        case 0xFC: // Same Measure
            if (isG36) {
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        0,
                        MIDISpEventType.SameMeasure,
                        new byte[][] {new byte[] {(byte) pk[1], (byte) (pk[3] & 0xff), (byte) (pk[3] / 0x100)}}
                );
            } else {
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        0,
                        MIDISpEventType.SameMeasure,
                        new byte[][] {new byte[] {(byte) pk[1], (byte) pk[2], (byte) pk[3]}}
                );
            }
            pt += skipPtr;
            break;
        case 0xFD: // Measure End
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    0,
                    MIDISpEventType.MeasureEnd,
                    null
            );
            pt += skipPtr;
            break;
        case 0xFE: // End of Track
            trkn.getPart().get(meaInd).insertSpEvent(
                    pEvt,
                    0,
                    MIDISpEventType.EndOfTrack,
                    new byte[][] {
                            new byte[] {(byte) pk[1], (byte) pk[2], (byte) pk[3]}
                    }
            );
            pt += skipPtr;
            endTrack = true;
            break;
        default:
            pt += skipPtr;
            break;
        }
    }

    private void initTrkPrt() {
        tracks = new MIDITrack[trkLen];
        parts = new MIDIPart[trkLen];
        for (int i = 0; i < trkLen; i++) {
            tracks[i] = new MIDITrack();
            tracks[i].setBeforeIndex((i - 1 < 0) ? null : (i - 1));
            tracks[i].setAfterIndex(i + 1);
            tracks[i].setNumber(i);
            tracks[i].clearAllPartMemory();
            parts[i] = new MIDIPart();
            parts[i].setName(String.format("Track %d Part", i + 1));
            tracks[i].insertPart(0, parts[i]);
        }
    }

    private void init() {

        tick.millisec = 0;
        tick.count = 0;
        tick.before = 0;
        tick.sabun = 0;
        // ps.TimeBase = prj.Information.TimeBase; // 分解能
        // ps.Tempo = prj.Information.Tempo;// テンポ:4分音符
        // ps.BaseTempo = prj.Information.Tempo;// テンポ:4分音符
        // ps.BeatDen = prj.Information.BeatDen;
        // ps.BeatMol = prj.Information.BeatMol;
        // ps.Lyric = "";
        // prj.RelativeTempoChangeNowTempo = prj.Information.Tempo;
        // prj.RelativeTempoChangeSW = false;

        int minSt = Integer.MAX_VALUE;
        for (MIDITrack tk : tracks) {
            minSt = Math.min(tk.getSt(), minSt);
        }
        minSt = (minSt < 0 ? -minSt : 0);

        // トラック毎の初期化
        for (MIDITrack tk : tracks) {
            tk.setNowPart(tk.getStartPart());
            tk.setNowTick(0);
            tk.setNextEventTick(0);
            tk.setEndMark(false);
            if (tk.getLoopTargetEvents() == null) {
                tk.setLoopTargetEvents(new Stack<>());
            }
            tk.getLoopTargetEvents().clear();
            tk.setLoopOrSameTargetEventIndex(null);
            tk.setSameMeasure(null);
            Arrays.fill(tk.getNoteGateTime(), Integer.MAX_VALUE);
            MIDIPart prt = tk.getStartPart();
            while (true) {
                if (prt == null) break;
                prt.setStartTick(tk.getSt() + minSt);
                prt.setENowIndex(prt.getEStartIndex());
                prt = tk.getNextPart(prt);
            }

            // for (clsConfig.clsMIDIDeviceList dev : Config.MIDIOutDeviceList)
            // {
            //    if (dev.UsrNumber == tk.OutUserDeviceNumber)
            //    {
            //        tk.OutDeviceName = dev.DevName;
            //        tk.OutDeviceNumber = dev.DevNumber;
            //    }
            // }

        }

        // /* MIDIクロックの生成*/
        // MID.Clock = new MIDIClock();
        // MIDIClock.Create(0, ps.TimeBase, 60000000 / ps.Tempo);
        // /* MIDIクロックのリセットとスタート */
        // MIDIClock.Reset();
        // MIDIClock.Start();

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
                    oneFrameRCP();
                }
                musicDownCounter += musicStep;
            }
            musicDownCounter -= 1.0;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void oneFrameRCP() {
        // リタルダンド処理
        if (relativeTempoChangeSW) {
            nowTempo += relativeTempoChangeTickSlice;
            if (relativeTempoChangeTickSlice <= 0) {
                if (relativeTempoChangeTargetTempo >= nowTempo) {
                    nowTempo = relativeTempoChangeTargetTempo;
                    relativeTempoChangeSW = false;
                }
            } else {
                if (relativeTempoChangeTargetTempo <= nowTempo) {
                    nowTempo = relativeTempoChangeTargetTempo;
                    relativeTempoChangeSW = false;
                }
            }

            oneSyncTime = 60.0 / nowTempo / timeBase;

        }

        boolean endMark = true;
        for (MIDITrack tk : tracks) {
            if (!tk.getEndMark()) {
                endMark = false;
                trackProcess(tk);
            }
        }

        tick.count++;
        // if (prj.RelativeTempoChangeSW) {
        // }

        if (endMark) {
            stopped = true;
        }
    }

    /**
     * トラック毎の処理
     */
    private void trackProcess(MIDITrack trk) {
        MIDIPart prt = trk.getNowPart();

        if (prt.getENowIndex() == null) {
            if (!checkNoteOff(trk, 0)) {
                trk.setEndMark(true);
            }
        }
        // パートの開始位置に達していないとき
        if (prt.getStartTick() > tick.count) {
            checkNoteOff(trk, 0);
            return;
        }

        // パートの処理を実施
        partProcess(trk);
        checkNoteOff(trk, 0);

        // 次のパートに移っていることがあるので
        prt = trk.getNowPart();
        // イベント送信が済んだところまでTick更新
        trk.setNowTick(tick.count - prt.getStartTick());

        if (trk.getEndMark()) return;

        if (trk.getNowPart().getENowIndex() != null) return;

        // 次の小節へ処理を移す
        if (prt.getAfterIndex() != null) {
            trk.setNowPart(trk.getNextPart(prt));
            trk.setNowTick(0);
            trk.setNextEventTick(0);
            trk.getNowPart().setENowIndex(trk.getNowPart().getEStartIndex());
            trackProcess(trk);
        }
    }

    /**
     * 
     */
    private boolean checkNoteOff(MIDITrack trk, int mode) {
        boolean flg = false;

        for (int n = 0; n < trk.getNoteGateTime().length; n++) {
            if (trk.getNoteGateTime()[n] != Integer.MAX_VALUE) {
                // if (trk.NoteGateTime[n] <= trk.NowTick + trk.getNowPart().StartTick)
                if (trk.getNoteGateTime()[n] <= trk.getNowTick()) {
                    // int key = (n + ((trk.Key != null) ? (int)trk.Key : 0));
                    int key = n + trk.getKey();
                    if (key < 0) key = 0;
                    else if (key > 127) key = 127;
                    if (trk.getOutChannel() != null) {
                        msgBuf[0] = (byte) (MIDIEventType.NoteOff.v + trk.getOutChannel());
                        msgBuf[1] = (byte) key;
                        msgBuf[2] = 127;
                        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
                    }
                    trk.getNoteGateTime()[n] = Integer.MAX_VALUE;
                    flg = true;
                }
            }
        }

        return flg;
    }

    private byte[] vv = new byte[1];

    private void putMIDIMessage(int n, byte[] pMIDIMessage, int len) {
        List<Byte> dat = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            dat.add(pMIDIMessage[i]);
//            chipRegister.sendMIDIout(model, n, vv, vstDelta);
        }
        chipRegister.sendMIDIout(model, n, toByteArray(dat), vstDelta);
    }

    /**
     * パート毎の処理
     */
    private void partProcess(MIDITrack trk) {
        MIDIPart prt = trk.getNowPart();

        // パートの情報がない場合処理しない
        if (prt == null || prt.getENowIndex() == null) return;
        // パート内にイベントがない場合も処理しない
        if (prt.getEvents() == null || prt.getEvents().size() == 0) {
            prt.setENowIndex(null);
            return;
        }

        // トラックの次のイベントまで処理をしない
        if (trk.getNextEventTick() > trk.getNowTick()) return;
        // イベントをひとつ取り出す
        MIDIEvent eve = prt.getEvents().get(prt.getENowIndex());
        // 誤差の算出
        eve.setGosa(trk.getNowTick() - trk.getNextEventTick());

        // イベント送信
        sendEvent(trk, eve);

        // 次のイベント発動時間をセット
        trk.setNextEventTick(trk.getNextEventTick() + eve.getStep());

        if (trk.getLoopOrSameTargetEventIndex() != null) {
            prt.setENowIndex(trk.getLoopOrSameTargetEventIndex());
            trk.setLoopOrSameTargetEventIndex(null);
            partProcess(trk);
        } else if (eve != null && eve.getAfterIndex() != null) {
            prt.setENowIndex(eve.getAfterIndex());
            partProcess(trk);
        } else {
            prt.setENowIndex(null);
        }
    }

    /**
     * イベント送信
     */
    private void sendEvent(MIDITrack trk, MIDIEvent eve) {
        if (trk.getOutDeviceNumber() == null) return;
        if (trk.getOutUserDeviceNumber() == null) return;
        // if (!Config.MIDIOutDeviceList[(int)trk.OutUserDeviceNumber].DevAlive) return;
        if (eve.getEventType() == MIDIEventType.NoteON && trk.getMute()) return;

        eventFunc[eve.getEventType().v].accept(trk, eve);
    }

    void efn(MIDITrack trk, MIDIEvent eve) {
    }

    void ef2byteMsg(MIDITrack trk, MIDIEvent eve) {
        if (trk.getOutChannel() == null) return;
        msgBuf[0] = eve.getMIDIMessage()[0];
        eve.getMIDIMessage()[0] &= 0xf0;
        eve.getMIDIMessage()[0] += (byte) (int) trk.getOutChannel();
        putMIDIMessage(trk.getOutDeviceNumber(), eve.getMIDIMessage(), 2);
        eve.getMIDIMessage()[0] = msgBuf[0];
    }

    void ef3byteMsg(MIDITrack trk, MIDIEvent eve) {
        if (trk.getOutChannel() == null) return;
        msgBuf[0] = eve.getMIDIMessage()[0];
        eve.getMIDIMessage()[0] &= 0xf0;
        eve.getMIDIMessage()[0] += (byte) (int) trk.getOutChannel();
        putMIDIMessage(trk.getOutDeviceNumber(), eve.getMIDIMessage(), 3);
        eve.getMIDIMessage()[0] = msgBuf[0];
    }

    void efnbyteMsg(MIDITrack trk, MIDIEvent eve) {
        putMIDIMessage(trk.getOutUserDeviceNumber(), eve.getMIDIMessage(), eve.getMIDIMessage().length);
    }

    void efMetaSeqNumber(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaSeqNumberは未実装！");
    }

    void efMetaTextEvent(MIDITrack trk, MIDIEvent eve) {
        trk.setComment(new String(eve.getMIDIMessage(), CHARSET).replace("\0", ""));
    }

    void efMetaCopyrightNotice(MIDITrack trk, MIDIEvent eve) {
        // prj.Information.Copyright = (Charset.forName("Shift_JIS").new String(eve.getMIDIMessage())).replace("\0", "");
    }

    void efMetaTrackName(MIDITrack trk, MIDIEvent eve) {
        trk.setName(new String(eve.getMIDIMessage(), CHARSET).replace("\0", ""));
    }

    void efMetaInstrumentName(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaInstrumentNameは未実装！");
    }

    void efMetaLyric(MIDITrack trk, MIDIEvent eve) {
        // ps.Lyric = (Charset.forName("Shift_JIS").new String(eve.MIDIMessage, 2, eve.MIDIMessage.length - 2)).replace("\0", "");
    }

    void efMetaMarker(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaMarkerは未実装！");
    }

    void efMetaCuePoint(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaCuePointは未実装！");
    }

    void efMetaProgramName(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaProgramNameは未実装！");
    }

    void efMetaDeviceName(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaDeviceNameは未実装！");
    }

    void efMetaChannelPrefix(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaChannelPrefixは未実装！");
    }

    void efMetaPortPrefix(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaPortPrefixは未実装！");
        System.err.printf("+Track.Number[%d] Event.Index[%d]%n", trk.getNumber(), eve.getNumber());
        System.err.printf("+Message [%d,%d,%d]%n", eve.getMIDIMessage()[0], eve.getMIDIMessage()[1], eve.getMIDIMessage()[2]);
    }

    void efMetaEndOfTrack(MIDITrack trk, MIDIEvent eve) {
        // 現時点では特に何も処理する必要なし
    }

    void efMetaTempo(MIDITrack trk, MIDIEvent eve) {
        // int Tempo = eve.getMIDIMessage()[2] * 0x10000 + eve.getMIDIMessage()[3] * 0x100 + eve.getMIDIMessage()[4];
        // MIDIClock.Stop();
        // MIDIClock.SetTempo(Tempo);
        // MIDIClock.Start();
        // ps.Tempo = 60000000 / Tempo;
    }

    void efMetaSMPTEOffset(MIDITrack trk, MIDIEvent eve) {
        System.err.println("MetaSMPTEOffsetは未実装！");
    }

    void efMetaTimeSignature(MIDITrack trk, MIDIEvent eve) {
        beatDen = eve.getMIDIMessage()[2]; // 分子
        beatMol = (int) Math.pow(2.0, eve.getMIDIMessage()[3]); // 分母
    }

    void efMetaKeySignature(MIDITrack trk, MIDIEvent eve) {
        System.err.printf("MetaKeySignatureは未実装！Track.Number[%d] Event.Index[%d]%n", trk.getNumber(), eve.getNumber());
    }

    void efNoteOff(MIDITrack trk, MIDIEvent eve) {
        // ef3byteMsg(trk, eve);
    }

    void efNoteOn(MIDITrack trk, MIDIEvent eve) {
        if (eve.getGate() == 0) return;
        int okey = eve.getMIDIMessage()[1];
        // int key = (okey + ((trk.Key != null) ? (int)trk.Key : 0));
        int key = okey + trk.getKey();
        if (key < 0) key = 0;
        if (key > 127) key = 127;

        if (trk.getOutChannel() != null) {
            boolean flg = false;
            // Key Off
            // if (trk.NoteGateTime[okey] <= trk.NextEventTick + trk.getNowPart().StartTick)
            if (trk.getNoteGateTime()[okey] <= trk.getNextEventTick()) {
                msgBuf[0] = (byte) (MIDIEventType.NoteOff.v + trk.getOutChannel());
                msgBuf[1] = (byte) key;
                msgBuf[2] = 127;
                putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
                flg = true;
            }

            // Key On
            if (trk.getNoteGateTime()[okey] == Integer.MAX_VALUE || flg) {
                msgBuf[0] = (byte) ((eve.getMIDIMessage()[0] & 0xf0) + trk.getOutChannel());
                msgBuf[1] = (byte) key;
                msgBuf[2] = eve.getMIDIMessage()[2];
                putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
            }
        }

        trk.getNoteGateTime()[okey] = trk.getNextEventTick() + eve.getGate();
    }

    void efKeyAfterTouch(MIDITrack trk, MIDIEvent eve) {
        ef3byteMsg(trk, eve);
    }

    void efControlChange(MIDITrack trk, MIDIEvent eve) {
        ef3byteMsg(trk, eve);
    }

    void efProgramChange(MIDITrack trk, MIDIEvent eve) {
        ef2byteMsg(trk, eve);
    }

    void efChannelAfterTouch(MIDITrack trk, MIDIEvent eve) {
        ef2byteMsg(trk, eve);
    }

    void efPitchBend(MIDITrack trk, MIDIEvent eve) {
        ef3byteMsg(trk, eve);
    }

    void efSysExStart(MIDITrack trk, MIDIEvent eve) {
        efnbyteMsg(trk, eve);
    }

    void efSysExContinue(MIDITrack trk, MIDIEvent eve) {
        efnbyteMsg(trk, eve);
    }

    void efMetaSequencerSpecific(MIDITrack trk, MIDIEvent eve) {
        specialEventFunc[eve.getMIDIMessage()[0]].accept(trk, eve);
    }

    void sefUserExclusive1(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(0, trk, eve);
    }

    void sefUserExclusive2(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(1, trk, eve);
    }

    void sefUserExclusive3(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(2, trk, eve);
    }

    void sefUserExclusive4(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(3, trk, eve);
    }

    void sefUserExclusive5(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(4, trk, eve);
    }

    void sefUserExclusive6(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(5, trk, eve);
    }

    void sefUserExclusive7(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(6, trk, eve);
    }

    void sefUserExclusive8(MIDITrack trk, MIDIEvent eve) {
        sefUserExclusiveN(7, trk, eve);
    }

    void sefChExclusive(MIDITrack trk, MIDIEvent eve) {
        int i = 0;
        int j = 0;
        int chksum = 0;

        for (int b = 0; b < 30; b++) msgBuf[b] = 0;

        while (j < eve.getMIDIMessages()[0].length - 2) {
            Byte n = eve.getMIDIMessages()[0][j];
            switch ((int) n) {
            case 0x80:
                n = eve.getMIDIMessages()[0][eve.getMIDIMessages()[0].length - 2];
                break;
            case 0x81:
                n = eve.getMIDIMessages()[0][eve.getMIDIMessages()[0].length - 1];
                break;
            case 0x82:
                n = (byte) (int) trk.getOutChannel();
                break;
            case 0x83:
                chksum = 0;
                n = null;
                break;
            case 0x84:
                n = (byte) (128 - (chksum % 128));
                break;
            }
            if (n != null) {
                msgBuf[i] = n;
                chksum += n;
                i++;
            }
            j++;
            if (n == 0xf7) break;
            if (i >= msgBuf.length) {
                System.err.println("sefChExclusive:バッファをオーバーするエクスクルーシブを検知しスキップ。");
                return;// バッファをオーバーする時はエクスクルーシブを送らない
            }
        }
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, i);
    }

    void sefOutsideProcessExec(MIDITrack trk, MIDIEvent eve) {
        System.err.println("spEventOutsideProcessExecは未実装！");
    }

    void sefBankProgram(MIDITrack trk, MIDIEvent eve) {
        msgBuf[0] = (byte) (eve.getMIDIMessages()[1][0] + (trk.getOutChannel() % 16));
        msgBuf[1] = eve.getMIDIMessages()[1][1];
        msgBuf[2] = eve.getMIDIMessages()[1][2];
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
        msgBuf[0] = (byte) (eve.getMIDIMessages()[0][0] + (trk.getOutChannel() % 16));
        msgBuf[1] = eve.getMIDIMessages()[0][1];
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 2);
    }

    void sefKeyScan(MIDITrack trk, MIDIEvent eve) {
        System.err.println("spEventKeyScanは未実装！");
    }

    void sefMIDIChChange(MIDITrack trk, MIDIEvent eve) {
        int ch = eve.getMIDIMessages()[0][0];
        if (ch == 0) {
            trk.setMute(true);
            return;
        }
        trk.setMute(false);
        ch--;
        trk.setOutDeviceNumber(ch / 16);
        trk.setOutChannel(ch % 16);
    }

    void sefTempoChange(MIDITrack trk, MIDIEvent eve) {
        double mul = eve.getMIDIMessages()[0][0] / 64.0;

        if (eve.getMIDIMessages()[0][1] == 0) {
            int Tempo = (int) (this.tempo * mul);
            if (Tempo < 10) Tempo = 10;
            else if (Tempo > 240) Tempo = 240;
            nowTempo = Tempo;
            oneSyncTime = 60.0 / nowTempo / timeBase;
        } else {
            // リタルダンド
            int Tempo = (int) (this.tempo * mul);
            double s = (Tempo - this.tempo) * 256.0 / ((256.0 - eve.getMIDIMessages()[0][1]) * timeBase);
            relativeTempoChangeTargetTempo = Tempo;
            relativeTempoChangeTickSlice = (nowTempo < Tempo) ? s : -s;
            relativeTempoChangeSW = true;
        }
    }

    void sefYAMAHABase(MIDITrack trk, MIDIEvent eve) {
        trk.setYAMAHABase_gt(eve.getMIDIMessages()[0][0]);
        trk.setYAMAHABase_vel(eve.getMIDIMessages()[0][1]);
    }

    void sefYAMAHADev(MIDITrack trk, MIDIEvent eve) {
        trk.setYAMAHA_dev(eve.getMIDIMessages()[0][0]);
        trk.setYAMAHA_model(eve.getMIDIMessages()[0][1]);
    }

    void sefYAMAHAAddrPara(MIDITrack trk, MIDIEvent eve) {
        trk.setYAMAHAPara_gt(eve.getMIDIMessages()[0][0]);
        trk.setYAMAHAPara_vel(eve.getMIDIMessages()[0][1]);

        msgBuf[0] = (byte) 0xf0;
        msgBuf[1] = 0x43;
        msgBuf[2] = trk.getYAMAHA_dev();
        msgBuf[3] = trk.getYAMAHA_model();
        msgBuf[4] = trk.getYAMAHABase_gt();
        msgBuf[5] = trk.getYAMAHABase_vel();
        msgBuf[6] = trk.getYAMAHAPara_gt();
        msgBuf[7] = trk.getYAMAHAPara_vel();
        msgBuf[8] = (byte) 0xf7;
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 9);
    }

    void sefYAMAHAXGAddrPara(MIDITrack trk, MIDIEvent eve) {
        trk.setYAMAHAPara_gt(eve.getMIDIMessages()[0][0]);
        trk.setYAMAHAPara_vel(eve.getMIDIMessages()[0][1]);

        msgBuf[0] = (byte) 0xf0;
        msgBuf[1] = 0x43;
        msgBuf[2] = 0x10;
        msgBuf[3] = 0x4c;
        msgBuf[4] = trk.getYAMAHABase_gt();
        msgBuf[5] = trk.getYAMAHABase_vel();
        msgBuf[6] = trk.getYAMAHAPara_gt();
        msgBuf[7] = trk.getYAMAHAPara_vel();
        msgBuf[8] = (byte) 0xf7;
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 9);
    }

    void sefRolandBase(MIDITrack trk, MIDIEvent eve) {
        trk.setRolandBase_gt(eve.getMIDIMessages()[0][0]);
        trk.setRolandBase_vel(eve.getMIDIMessages()[0][1]);
    }

    void sefRolandPara(MIDITrack trk, MIDIEvent eve) {
        trk.RolandPara_gt(eve.getMIDIMessages()[0][0]);
        trk.RolandPara_vel(eve.getMIDIMessages()[0][1]);

        msgBuf[0] = (byte) 0xF0;
        msgBuf[1] = 0x41;
        msgBuf[2] = trk.getRolandDev_gt();
        msgBuf[3] = trk.getRolandDev_vel();
        msgBuf[4] = 0x12;
        msgBuf[5] = trk.getRolandBase_gt();
        msgBuf[6] = trk.getRolandBase_vel();
        msgBuf[7] = trk.getRolandPara_gt();
        msgBuf[8] = trk.getRolandPara_vel();
        msgBuf[9] = (byte) ((128 - ((trk.getRolandBase_gt() + trk.getRolandBase_vel() + trk.getRolandPara_gt() + trk.getRolandPara_vel()) % 128)) & 0x7f);
        msgBuf[10] = (byte) 0xF7;
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 11);
    }

    void sefRolandDev(MIDITrack trk, MIDIEvent eve) {
        trk.setRolandDev_gt(eve.getMIDIMessages()[0][0]);
        trk.setRolandDev_vel(eve.getMIDIMessages()[0][1]);
    }

    void sefKeyChange(MIDITrack trk, MIDIEvent eve) {
        int sf, mi;
        int v, vv;

        v = eve.getStep();
        vv = v % 0x10;
        sf = vv > 0x07 ? (0x100 - vv) % 0x100 : vv;
        mi = v > 0x0f ? 1 : 0;

        trk.setKeySIG_SF(sf); // Sharp Flat -7:7flats -1:1flat 0:Key of C 1:1sharp 7:7sharp
        trk.setKeySIG_MI(mi); // Is minor Key
    }

    void sefCommentStart(MIDITrack trk, MIDIEvent eve) {
        trk.setComment(new String(eve.getMIDIMessages()[0], CHARSET).replace("\0", ""));
        chipRegister.midiParams[0].Lyric = trk.getComment();
    }

    void sefLoopEnd(MIDITrack trk, MIDIEvent eve) {
        if (trk.getLoopTargetEvents().size() == 0) return;
        MIDIEvent evt = trk.getLoopTargetEvents().pop();
        if (evt.getMIDIMessages()[0][0] < eve.getMIDIMessages()[0][0] - 1) {
            evt.getMIDIMessages()[0][0]++;
            trk.getLoopTargetEvents().push(evt);
            trk.setLoopOrSameTargetEventIndex(trk.getNowPart().getNextEvent(evt).getNumber());
        } else if (eve.getMIDIMessages()[0][0] == 0) {
            trk.getLoopTargetEvents().push(evt);
            trk.setLoopOrSameTargetEventIndex(trk.getNowPart().getNextEvent(evt).getNumber());
        }
    }

    void sefLoopStart(MIDITrack trk, MIDIEvent eve) {
        MIDIEvent evt = trk.getNowPart().getEvents().get(trk.getNowPart().getENowIndex());
        evt.getMIDIMessages()[0][0] = 0;
        trk.getLoopTargetEvents().push(evt);
    }

    void sefSameMeasure(MIDITrack trk, MIDIEvent eve) {
        if (trk.getSameMeasure() != null) {
            trk.setLoopOrSameTargetEventIndex(trk.getSameMeasure());
            trk.setSameMeasure(null);
            return;
        }
        trk.setLoopOrSameTargetEventIndex(eve.getSameMeasureIndex());
        trk.setSameMeasure(trk.getNowPart().getNextEvent(eve).getNumber());
        while (trk.getNowPart().getEvents().get(trk.getLoopOrSameTargetEventIndex()).getEventType() == MIDIEventType.MetaSequencerSpecific
                && trk.getNowPart().getEvents().get(trk.getLoopOrSameTargetEventIndex()).getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.v) {
            trk.setLoopOrSameTargetEventIndex(trk.getNowPart().getEvents().get(trk.getLoopOrSameTargetEventIndex()).getSameMeasureIndex());
        }
    }

    void sefMeasureEnd(MIDITrack trk, MIDIEvent eve) {
        if (trk.getSameMeasure() != null) {
            trk.setLoopOrSameTargetEventIndex(trk.getSameMeasure());
            trk.setSameMeasure(null);
        }
    }

    void sefEndofTrack(MIDITrack trk, MIDIEvent eve) {
        trk.setEndMark(true);
    }

    void sefDX7Func(MIDITrack trk, MIDIEvent eve) {
    }

    void sefDXPara(MIDITrack trk, MIDIEvent eve) {
    }

    void sefDXRERF(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTXFunc(MIDITrack trk, MIDIEvent eve) {
    }

    void sefFB01PPara(MIDITrack trk, MIDIEvent eve) {
    }

    void sefFB01SSystem(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTX81ZVVCED(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTX81ZAACED(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTX81ZPPCED(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTX81ZSSystem(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTX81ZEEffect(MIDITrack trk, MIDIEvent eve) {
    }

    void sefDX72RRemoteSW(MIDITrack trk, MIDIEvent eve) {
    }

    void sefDX72AACED(MIDITrack trk, MIDIEvent eve) {
    }

    void sefDX72PPCED(MIDITrack trk, MIDIEvent eve) {
    }

    void sefTX802PPCED(MIDITrack trk, MIDIEvent eve) {
    }

    void sefMKS7(MIDITrack trk, MIDIEvent eve) {
    }

    void sefUserExclusiveN(int num, MIDITrack trk, MIDIEvent eve) {
        int i = 0;
        int j = 0;
        int chksum = 0;

        while (j < userExclusives.get(num).getExclusive().length) {
            Byte n = userExclusives.get(num).getExclusive()[j];
            switch (n & 0xff) {
            case 0x80:
                n = eve.getMIDIMessages()[0][0];
                break;
            case 0x81:
                n = eve.getMIDIMessages()[0][1];
                break;
            case 0x82:
                n = (byte) (int) trk.getOutChannel();
                break;
            case 0x83:
                chksum = 0;
                n = null;
                break;
            case 0x84:
                n = (byte) ((128 - (chksum % 128)) & 0x7f);
                break;
            }
            if (n != null) {
                msgBuf[i] = n;
                chksum += n;
                i++;
            }
            j++;
            if ((n & 0xff) == 0xf7) break;
            if (i >= msgBuf.length) {
                System.err.println("sefUserExclusiveN:バッファをオーバーするエクスクルーシブを検知しスキップ。");
                return; // バッファをオーバーする時はエクスクルーシブを送らない
            }
        }
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, i);
    }

    public static class CtlSysex {
        public CtlSysex(int d, byte[] dat) {
            delta = d;
            data = dat;
        }

        public int delta;
        public byte[] data;
    }

    private byte[] getSysEx(byte[] buf) {
        List<Byte> ret = new ArrayList<>();
        int chksum = 0;

        ret.add((byte) 0xf0);

        for (Byte b : buf) {
            Byte n = b;
            switch (n & 0xff) {
            // case 0x82:
            //  n = (byte)trk.OutChannel;
            // break;
            case 0x83:
                chksum = 0;
                n = null;
                break;
            case 0x84:
                n = (byte) ((128 - (chksum % 128)) & 0x7f);
                break;
            }
            if (n != null) {
                ret.add(n);
                chksum += n;
            }
        }

        ret.add((byte) 0xf7);

        return toByteArray(ret);
    }

    private void getGSD1Buf(List<CtlSysex> dBuf) {
        byte[] buf = null;
        for (Tuple<String, byte[]> trg : ExtendFile) {
            if (Path.getExtension(trg.getItem1()).equalsIgnoreCase(".GSD")) {
                buf = trg.getItem2();
                break;
            }
        }
        getGSDBuf(dBuf, buf);
    }

    private void getGSD2Buf(List<CtlSysex> DBuf) {
        byte[] buf = null;
        for (Tuple<String, byte[]> trg : ExtendFile) {
            if (Path.getExtension(trg.getItem1()).equalsIgnoreCase(".GSD")) {
                buf = trg.getItem2();
            }
        }
        getGSDBuf(DBuf, buf);
    }

    private void getGSDBuf(List<CtlSysex> dBuf, byte[] buf) {
        if (buf == null || buf.length != 0xa71) return;

        int adr;

        for (int ch = 0; ch < 16; ch++) {
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x65, 0x00})); // RPN Master fine tuning
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x64, 0x01}));
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x06, (byte) ((buf[0xa6f] * 0x100 + buf[0xa6e]) >> 7)}));
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x26, (byte) ((buf[0xa6f] * 0x100 + buf[0xa6e]) & 0x7f)}));
        }

        // Master Volume
        dBuf.add(new CtlSysex(1, getSysEx(new byte[] {0x41, 0x10, 0x42, 0x12, (byte) 0x83, 0x40, 0x00, 0x04, buf[0x24], (byte) 0x84})));
        dBuf.add(new CtlSysex(4, getSysEx(new byte[] {(byte) 0x7F, (byte) 0x7F, 0x04, 0x01, (byte) ((buf[0x24] * 0x81) & 0x7F), (byte) (((buf[0x24] * 0x81) >> 7) & 0x7f)})));

        for (int ch = 0; ch < 16; ch++) {
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x65, 0x00})); // RPN Master Coarse tuning
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x64, 0x02}));
            dBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x06, (byte) (buf[0xa70] & 0x7f)}));
        }

        // Master Pan
        dBuf.add(new CtlSysex(1, getSysEx(new byte[] {0x41, 0x10, 0x42, 0x12, (byte)0x83, 0x40, 0x00, 0x06, buf[0x26], (byte)0x84})));
        // Master Balance
        dBuf.add(new CtlSysex(1, getSysEx(new byte[] {0x7f, 0x7f, 0x04, 0x02, (byte) ((buf[0x26] * 0x80) & 0x7F), (byte) (((buf[0x26] * 0x80) >> 7) & 0x7f)})));

        // Voice Reserve Loc:Ch partdata - 1 Len:1
        dBuf.add(new CtlSysex(1, getSysEx(new byte[] {0x41, 0x10, 0x42, 0x12, (byte)0x83
                , 0x40, 0x01, 0x10
                , buf[0x4f9], buf[0x0af], buf[0x129], buf[0x1a3] // 10ch  1ch  2ch  3ch
                , buf[0x21d], buf[0x297], buf[0x311], buf[0x38b] // 4ch  5ch  6ch  7ch
                , buf[0x405], buf[0x47f], buf[0x573], buf[0x5ed] // 8ch  9ch 11ch 12ch
                , buf[0x667], buf[0x6e1], buf[0x75b], buf[0x7d5] // 13ch 14ch 15ch 16ch
                , (byte) 0x84})));

        // Reverb Loc:0x27 Len:7
        dBuf.add(new CtlSysex(1, getSysEx(new byte[]{0x41, 0x10, 0x42, 0x12, (byte)0x83
                , 0x40, 0x01, 0x30
                , buf[0x27], buf[0x28], buf[0x29], buf[0x2a]
                , buf[0x2b], buf[0x2c], buf[0x2d]
                , (byte)0x84})));

        // Chorus Loc:0x2E Len:8
        dBuf.add(new CtlSysex(1, getSysEx(new byte[]{0x41, 0x10, 0x42, 0x12, (byte)0x83
                , 0x40, 0x01, 0x38
                , buf[0x2e], buf[0x2f], buf[0x30], buf[0x31]
                , buf[0x32], buf[0x33], buf[0x34], buf[0x35]
                , (byte)0x84})));

        for (int i = 0; i < 16; i++) {
            adr = i * 0x7a + 0x36;
            byte ch = buf[adr + 0x2]; // MIDI CH
            byte iAdrMm;
            byte iAdrLl;

            makeGSDBufPtn_0(dBuf, buf, adr, ch);

            if (i < 9) {
                iAdrMm = (byte) ((i * 0xe0 + 0x170) >> 7);
                iAdrLl = (byte) ((i * 0xe0 + 0x170) & 0x7f);
            } else if (i == 9) {
                iAdrMm = 0x01;
                iAdrLl = 0x10;
            } else {
                iAdrMm = (byte) (((i - 1) * 0xe0 + 0x170) >> 7);
                iAdrLl = (byte) (((i - 1) * 0xe0 + 0x170) & 0x7f);
            }
            makeGSDBufPtn_1(dBuf, buf, adr, iAdrMm, iAdrLl);

            if (i < 9) {
                iAdrMm = (byte) ((i * 0xe0 + 0x1f0) >> 7);
                iAdrLl = (byte) ((i * 0xe0 + 0x1f0) & 0x7f);
            } else if (i == 9) {
                iAdrMm = 0x02;
                iAdrLl = 0x10;
            } else {
                iAdrMm = (byte) (((i - 1) * 0xe0 + 0x1f0) >> 7);
                iAdrLl = (byte) (((i - 1) * 0xe0 + 0x1f0) & 0x7f);
            }
            makeGSDBufPtn_2(dBuf, buf, adr, iAdrMm, iAdrLl);

        }

        adr = 0x7d6;
        makeGSDBufPtn_3(dBuf, buf, adr, 0x00);
        adr = 0x922;
        makeGSDBufPtn_3(dBuf, buf, adr, 0x10);
    }

    private void makeGSDBufPtn_0(List<CtlSysex> DBuf, byte[] buf, int adr, byte ch) {
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x00, buf[adr + 0x00]})); // Bank mm
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x20, 0})); // Bank ll
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xc0 + ch), buf[adr + 0x01]})); // Program Change
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x07, buf[adr + 0x19]})); // Volume
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x65, 0})); // RPN PITCH BEND
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x64, 0})); //
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x06, 2})); // ?
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x26, 0})); // ?
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x5b, buf[adr + 0x22]})); // Reverb Send Level
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x5d, buf[adr + 0x21]})); // Chorus Send Level
        DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x0a, buf[adr + 0x1c]})); // Panpot
    }

    private void makeGSDBufPtn_1(List<CtlSysex> DBuf, byte[] buf, int adr, byte iAdrMm, byte iAdrLl) {
        DBuf.add(new CtlSysex(5, getSysEx(new byte[] {0x41, 0x10, 0x42, 0x12, (byte)0x83
                , 0x48, iAdrMm, iAdrLl
                , (byte) (buf[adr + 0x00] >> 4), (byte) (buf[adr + 0x00] & 0xf) // BANK(LSB) 0 1
                , (byte) (buf[adr + 0x01] >> 4), (byte) (buf[adr + 0x01] & 0xf) // PROGRAM CHANGE 2 3
                , (byte) (((buf[adr + 0x03] & 1) << 3) | ((buf[adr + 0x04] & 1) << 2) | ((buf[adr + 0x05] & 1) << 1) | ((buf[adr + 0x06] & 1) << 0)) // PITCH BEND + CH PRESSURE + PROGRAM CHANGE + CONTROL CHANGE 4
                , (byte) (((buf[adr + 0x07] & 1) << 3) | ((buf[adr + 0x08] & 1) << 2) | ((buf[adr + 0x09] & 1) << 1) | ((buf[adr + 0x0a] & 1) << 0)) // POLY PRESSURE + NOTE MESSAGE + RPN + NRPN 5
                , (byte) (((buf[adr + 0x0b] & 1) << 3) | ((buf[adr + 0x0c] & 1) << 2) | ((buf[adr + 0x0d] & 1) << 1) | ((buf[adr + 0x0e] & 1) << 0)) // MODURATION + volume + PANPOT + EXPRESSION 6
                , (byte) (((buf[adr + 0x0f] & 1) << 3) | ((buf[adr + 0x10] & 1) << 2) | ((buf[adr + 0x11] & 1) << 1) | ((buf[adr + 0x12] & 1) << 0)) // HOLD1 + PORTMENT + SOSTENUTE + SOFT 7
                , (byte) (buf[adr + 0x02] >> 4), (byte) (buf[adr + 0x02] & 0xf) // MIDI CH 8 9
                , (byte) (((buf[adr + 0x13] & 1) << 3) | ((buf[adr + 0x15] & 3) << 1) | ((buf[adr + 0x15] & 3) != 0 ? 1 : 0)) // MONO/PORY MODE + ASSIGN MODE  10
                , (byte) (((buf[adr + 0x14] & 3))) // USE FOR RHYTHM PART 11
                , (byte) (buf[adr + 0x16] >> 4), (byte) (buf[adr + 0x16] & 0xf) // PITCH KEY SHIFT 12,13
                , buf[adr + 0x17] // PITCH OFFSET FINE              14
                , buf[adr + 0x18] // PITCH OFFSET FINE  (NIBBLIZED) 15
                , (byte) (buf[adr + 0x19] >> 4), (byte) (buf[adr + 0x19] & 0xf) // PART LEVEL 16,17
                , (byte) (buf[adr + 0x1c] >> 4), (byte) (buf[adr + 0x1c] & 0xf) // PART PANPOT 18,19
                , (byte) (buf[adr + 0x1a] >> 4), (byte) (buf[adr + 0x1a] & 0xf) // VELOCITY SENSE DEPTH 22,23 (20,21 ?)
                , (byte) (buf[adr + 0x1b] >> 4), (byte) (buf[adr + 0x1b] & 0xf) // VELOCITY SENSE OFFSET 20,21 (22,23 ?)
                , (byte) (buf[adr + 0x1d] >> 4), (byte) (buf[adr + 0x1d] & 0xf) // KEY RANGE LOW 24,25
                , (byte) (buf[adr + 0x1e] >> 4), (byte) (buf[adr + 0x1e] & 0xf) // KEY RANGE HIGH 26,27
                , (byte) (buf[adr + 0x21] >> 4), (byte) (buf[adr + 0x21] & 0xf) // CHOURS SEND DEPTH 28,29
                , (byte) (buf[adr + 0x22] >> 4), (byte) (buf[adr + 0x22] & 0xf) // REVERB SEND DEPTH 30,31

                , (byte) (buf[adr + 0x23] >> 4), (byte) (buf[adr + 0x23] & 0xf) // TONE MODEFY 1 32,33
                , (byte) (buf[adr + 0x24] >> 4), (byte) (buf[adr + 0x24] & 0xf) // TONE MODEFY 2 34,35
                , (byte) (buf[adr + 0x25] >> 4), (byte) (buf[adr + 0x25] & 0xf) // TONE MODEFY 3 36,37
                , (byte) (buf[adr + 0x26] >> 4), (byte) (buf[adr + 0x26] & 0xf) // TONE MODEFY 4 38,39
                , (byte) (buf[adr + 0x27] >> 4), (byte) (buf[adr + 0x27] & 0xf) // TONE MODEFY 5 40,41
                , (byte) (buf[adr + 0x28] >> 4), (byte) (buf[adr + 0x28] & 0xf) // TONE MODEFY 6 42,43
                , (byte) (buf[adr + 0x29] >> 4), (byte) (buf[adr + 0x29] & 0xf) // TONE MODEFY 7 44,45
                , (byte) (buf[adr + 0x2a] >> 4), (byte) (buf[adr + 0x2a] & 0xf) // TONE MODEFY 8 46,47
                , 0, 0, 0, 0 // (DATA 48,49,50,51の値は0)
                , (byte) (buf[adr + 0x2b] >> 4), (byte) (buf[adr + 0x2b] & 0xf) // SCALE TUNIG C  52,53
                , (byte) (buf[adr + 0x2c] >> 4), (byte) (buf[adr + 0x2c] & 0xf) // SCALE TUNIG C# 54,55
                , (byte) (buf[adr + 0x2d] >> 4), (byte) (buf[adr + 0x2d] & 0xf) // SCALE TUNIG D  56,57
                , (byte) (buf[adr + 0x2e] >> 4), (byte) (buf[adr + 0x2e] & 0xf) // SCALE TUNIG D# 58,59
                , (byte) (buf[adr + 0x2f] >> 4), (byte) (buf[adr + 0x2f] & 0xf) // SCALE TUNIG E  60,61
                , (byte) (buf[adr + 0x30] >> 4), (byte) (buf[adr + 0x30] & 0xf) // SCALE TUNIG F  62,63
                , (byte) (buf[adr + 0x31] >> 4), (byte) (buf[adr + 0x31] & 0xf) // SCALE TUNIG F# 64,65
                , (byte) (buf[adr + 0x32] >> 4), (byte) (buf[adr + 0x32] & 0xf) // SCALE TUNIG G  66,67
                , (byte) (buf[adr + 0x33] >> 4), (byte) (buf[adr + 0x33] & 0xf) // SCALE TUNIG G# 68,69
                , (byte) (buf[adr + 0x34] >> 4), (byte) (buf[adr + 0x34] & 0xf) // SCALE TUNIG a  70,71
                , (byte) (buf[adr + 0x35] >> 4), (byte) (buf[adr + 0x35] & 0xf) // SCALE TUNIG a# 72,73
                , (byte) (buf[adr + 0x36] >> 4), (byte) (buf[adr + 0x36] & 0xf) // SCALE TUNIG B  74,75

                , (byte) (buf[adr + 0x1f] >> 4), (byte) (buf[adr + 0x1f] & 0xf) // CC1 CONTROLLER NUMBER 76,77
                , (byte) (buf[adr + 0x20] >> 4), (byte) (buf[adr + 0x20] & 0xf) // CC2 CONTROLLER NUMBER 78,79

                , (byte) (buf[adr + 0x37] >> 4), (byte) (buf[adr + 0x37] & 0xf) // MOD  PITCH CONTROL      80,81
                , (byte) (buf[adr + 0x38] >> 4), (byte) (buf[adr + 0x38] & 0xf) // MOD  TVF CUTOFF CONTROL 82,83
                , (byte) (buf[adr + 0x39] >> 4), (byte) (buf[adr + 0x39] & 0xf) // MOD  AMPLITUDE CONTROL  84,85
                , 0, 0 // (DATA 86, 87の値は0)
                , (byte) (buf[adr + 0x3a] >> 4), (byte) (buf[adr + 0x3a] & 0xf) // MOD  LFO1 RATE CONTROL  90,91
                , (byte) (buf[adr + 0x3b] >> 4), (byte) (buf[adr + 0x3b] & 0xf) // MOD  LFO1 PITCH DEPTH   92,93
                , (byte) (buf[adr + 0x3c] >> 4), (byte) (buf[adr + 0x3c] & 0xf) // MOD  LFO1 TVF DEPTH     94,95
                , (byte) (buf[adr + 0x3d] >> 4), (byte) (buf[adr + 0x3d] & 0xf) // MOD  LFO2 TVA DEPTH     96,97
                , (byte) (buf[adr + 0x3e] >> 4), (byte) (buf[adr + 0x3e] & 0xf) // MOD  LFO2 RATE CONTROL  98,99
                , (byte) (buf[adr + 0x3f] >> 4), (byte) (buf[adr + 0x3f] & 0xf) // MOD  LFO2 PITCH DEPTH   100,101
                , (byte) (buf[adr + 0x40] >> 4), (byte) (buf[adr + 0x40] & 0xf) // MOD  LFO2 TVF DEPTH     102,103
                , (byte) (buf[adr + 0x41] >> 4), (byte) (buf[adr + 0x41] & 0xf) // MOD  LFO2 TVA DEPTH     104,105
                , (byte) (buf[adr + 0x42] >> 4), (byte) (buf[adr + 0x42] & 0xf) // BEND PITCH CONTROL      106,107
                , (byte) (buf[adr + 0x43] >> 4), (byte) (buf[adr + 0x43] & 0xf) // BEND TVF CUTOFF CONTROL 108,109
                , (byte) (buf[adr + 0x44] >> 4), (byte) (buf[adr + 0x44] & 0xf) // BEND AMPLITUDE CONTROL  110,111
                , 0, 0 // (DATA 112,113の値は0)
                , (byte) (buf[adr + 0x45] >> 4), (byte) (buf[adr + 0x45] & 0xf) // BEND LFO1 RATE CONTROL  114,115
                , (byte) (buf[adr + 0x46] >> 4), (byte) (buf[adr + 0x46] & 0xf) // BEND LFO1 PITCH DEPTH   116,117
                , (byte) (buf[adr + 0x47] >> 4), (byte) (buf[adr + 0x47] & 0xf) // BEND LFO1 TVF DEPTH     118,119
                , (byte) (buf[adr + 0x48] >> 4), (byte) (buf[adr + 0x48] & 0xf) // BEND LFO1 TVA DEPTH     120,121
                , (byte) (buf[adr + 0x49] >> 4), (byte) (buf[adr + 0x49] & 0xf) // BEND LFO2 RATE CONTROL  122,123
                , (byte) (buf[adr + 0x4a] >> 4), (byte) (buf[adr + 0x4a] & 0xf) // BEND LFO2 PITCH DEPTH   124,125
                , (byte) (buf[adr + 0x4b] >> 4), (byte) (buf[adr + 0x4b] & 0xf) // BEND LFO2 TVF DEPTH     126,127
                , (byte) (buf[adr + 0x4c] >> 4), (byte) (buf[adr + 0x4c] & 0xf) // BEND LFO2 TVA DEPTH     126,127

                , (byte)0x84})));
    }

    private void makeGSDBufPtn_2(List<CtlSysex> DBuf, byte[] buf, int adr, byte iAdrMm, byte iAdrLl) {
        DBuf.add(new CtlSysex(4, getSysEx(new byte[] {0x41, 0x10, 0x42, 0x12, (byte)0x83
                , 0x48, iAdrMm, iAdrLl
                , (byte) (buf[adr + 0x4d] >> 4), (byte) (buf[adr + 0x4d] & 0xf) // CAf  PITCH CONTROL      0,1
                , (byte) (buf[adr + 0x4e] >> 4), (byte) (buf[adr + 0x4e] & 0xf) // CAf  TVF CUTOFF CONTROL 2,3
                , (byte) (buf[adr + 0x4f] >> 4), (byte) (buf[adr + 0x4f] & 0xf) // CAf  AMPLITUDE CONTROL  4,5
                , (byte) (buf[adr + 0x50] >> 4), (byte) (buf[adr + 0x50] & 0xf) // CAf  LFO1 RATE CONTROL  6,7
                , 4, 0// 8, 9
                , (byte) (buf[adr + 0x51] >> 4), (byte) (buf[adr + 0x51] & 0xf) // CAf  LFO1 PITCH DEPTH   10,11
                , (byte) (buf[adr + 0x52] >> 4), (byte) (buf[adr + 0x52] & 0xf) // CAf  LFO1 TVF DEPTH     12,13
                , (byte) (buf[adr + 0x53] >> 4), (byte) (buf[adr + 0x53] & 0xf) // CAf  LFO1 TVA DEPTH     14,15
                , (byte) (buf[adr + 0x54] >> 4), (byte) (buf[adr + 0x54] & 0xf) // CAf  LFO2 RATE CONTROL  16,17
                , (byte) (buf[adr + 0x55] >> 4), (byte) (buf[adr + 0x55] & 0xf) // CAf  LFO2 PITCH DEPTH   18,19
                , (byte) (buf[adr + 0x56] >> 4), (byte) (buf[adr + 0x56] & 0xf) // CAf  LFO2 TVF DEPTH     20,21
                , (byte) (buf[adr + 0x57] >> 4), (byte) (buf[adr + 0x57] & 0xf) // CAf  LFO2 TVA DEPTH     22,23
                , (byte) (buf[adr + 0x58] >> 4), (byte) (buf[adr + 0x58] & 0xf) // PAf  PITCH CONTROL      24,25
                , (byte) (buf[adr + 0x59] >> 4), (byte) (buf[adr + 0x59] & 0xf) // PAf  TVF CUTOFF CONTROL 26,27
                , (byte) (buf[adr + 0x5a] >> 4), (byte) (buf[adr + 0x5a] & 0xf) // PAf  AMPLITUDE CONTROL  28,29
                , (byte) (buf[adr + 0x5b] >> 4), (byte) (buf[adr + 0x5b] & 0xf) // PAf  LFO1 RATE CONTROL  30,31
                , 4, 0// 32,33
                , (byte) (buf[adr + 0x5c] >> 4), (byte) (buf[adr + 0x5c] & 0xf) // PAf  LFO1 PITCH DEPTH   34,35
                , (byte) (buf[adr + 0x5d] >> 4), (byte) (buf[adr + 0x5d] & 0xf) // PAf  LFO1 TVF DEPTH     36,37
                , (byte) (buf[adr + 0x5e] >> 4), (byte) (buf[adr + 0x5e] & 0xf) // PAf  LFO1 TVA DEPTH     38,39
                , (byte) (buf[adr + 0x5f] >> 4), (byte) (buf[adr + 0x5f] & 0xf) // PAf  LFO2 RATE CONTROL  40,41
                , (byte) (buf[adr + 0x60] >> 4), (byte) (buf[adr + 0x60] & 0xf) // PAf  LFO2 PITCH DEPTH   42,43
                , (byte) (buf[adr + 0x61] >> 4), (byte) (buf[adr + 0x61] & 0xf) // PAf  LFO2 TVF DEPTH     44,45
                , (byte) (buf[adr + 0x62] >> 4), (byte) (buf[adr + 0x62] & 0xf) // PAf  LFO2 TVA DEPTH     46,47
                , (byte) (buf[adr + 0x63] >> 4), (byte) (buf[adr + 0x63] & 0xf) // CC1  PITCH CONTROL      48,49
                , (byte) (buf[adr + 0x64] >> 4), (byte) (buf[adr + 0x64] & 0xf) // CC1  TVF CUTOFF CONTROL 50,51
                , (byte) (buf[adr + 0x65] >> 4), (byte) (buf[adr + 0x65] & 0xf) // CC1  AMPLITUDE CONTROL  52,53
                , 0, 0// 54,55
                , (byte) (buf[adr + 0x66] >> 4), (byte) (buf[adr + 0x66] & 0xf) // CC1  LFO1 RATE CONTROL  56,57
                , (byte) (buf[adr + 0x67] >> 4), (byte) (buf[adr + 0x67] & 0xf) // CC1  LFO1 PITCH DEPTH   58,59
                , (byte) (buf[adr + 0x68] >> 4), (byte) (buf[adr + 0x68] & 0xf) // CC1  LFO1 TVF DEPTH     60,61
                , (byte) (buf[adr + 0x69] >> 4), (byte) (buf[adr + 0x69] & 0xf) // CC1  LFO1 TVA DEPTH     62,63
                , (byte) (buf[adr + 0x6a] >> 4), (byte) (buf[adr + 0x6a] & 0xf) // CC1  LFO2 RATE CONTROL  64,65
                , (byte) (buf[adr + 0x6b] >> 4), (byte) (buf[adr + 0x6b] & 0xf) // CC1  LFO2 PITCH DEPTH   66,67
                , (byte) (buf[adr + 0x6c] >> 4), (byte) (buf[adr + 0x6c] & 0xf) // CC1  LFO2 TVF DEPTH     68,69
                , (byte) (buf[adr + 0x6d] >> 4), (byte) (buf[adr + 0x6d] & 0xf) // CC1  LFO2 TVA DEPTH     70,71
                , (byte) (buf[adr + 0x6e] >> 4), (byte) (buf[adr + 0x6e] & 0xf) // CC2  PITCH CONTROL      72,73
                , (byte) (buf[adr + 0x6f] >> 4), (byte) (buf[adr + 0x6f] & 0xf) // CC2  TVF CUTOFF CONTROL 74,75
                , (byte) (buf[adr + 0x70] >> 4), (byte) (buf[adr + 0x70] & 0xf) // CC2  AMPLITUDE CONTROL  76,77
                , 0, 0// 78,79
                , (byte) (buf[adr + 0x71] >> 4), (byte) (buf[adr + 0x71] & 0xf) // CC2  LFO1 RATE CONTROL  80,81
                , (byte) (buf[adr + 0x72] >> 4), (byte) (buf[adr + 0x72] & 0xf) // CC2  LFO1 PITCH DEPTH   82,83
                , (byte) (buf[adr + 0x73] >> 4), (byte) (buf[adr + 0x73] & 0xf) // CC2  LFO1 TVF DEPTH     84,85
                , (byte) (buf[adr + 0x74] >> 4), (byte) (buf[adr + 0x74] & 0xf) // CC2  LFO1 TVA DEPTH     86,87
                , (byte) (buf[adr + 0x75] >> 4), (byte) (buf[adr + 0x75] & 0xf) // CC2  LFO2 RATE CONTROL  88,89
                , (byte) (buf[adr + 0x76] >> 4), (byte) (buf[adr + 0x76] & 0xf) // CC2  LFO2 PITCH DEPTH   90,91
                , (byte) (buf[adr + 0x77] >> 4), (byte) (buf[adr + 0x77] & 0xf) // CC2  LFO2 TVF DEPTH     92,93
                , (byte) (buf[adr + 0x78] >> 4), (byte) (buf[adr + 0x78] & 0xf) // CC2  LFO2 TVA DEPTH     94,95

                , (byte)0x84})));
    }

    private void makeGSDBufPtn_3(List<CtlSysex> DBuf, byte[] buf, int adr, int adr2) {
        List<Byte> level = new ArrayList<>();
        List<Byte> panpot = new ArrayList<>();
        List<Byte> reverb = new ArrayList<>();
        List<Byte> chorus = new ArrayList<>();

        for (int i = 0; i < 27; i++) {
            level.add((byte) 0);
            level.add((byte) 0);
            panpot.add((byte) 0);
            panpot.add((byte) 0);
            reverb.add((byte) 0);
            reverb.add((byte) 0);
            chorus.add((byte) 0);
            chorus.add((byte) 0);
        }

        for (int i = 0; i < 82; i++) {
            level.add((byte) (buf[adr + i * 4 + 0] >> 4));
            level.add((byte) (buf[adr + i * 4 + 0] & 0xf));
            panpot.add((byte) (buf[adr + i * 4 + 1] >> 4));
            panpot.add((byte) (buf[adr + i * 4 + 1] & 0xf));
            reverb.add((byte) (buf[adr + i * 4 + 2] >> 4));
            reverb.add((byte) (buf[adr + i * 4 + 2] & 0xf));
            chorus.add((byte) (buf[adr + i * 4 + 3] >> 4));
            chorus.add((byte) (buf[adr + i * 4 + 3] & 0xf));
        }

        for (int i = 0; i < 19; i++) {
            level.add((byte) 0);
            level.add((byte) 0);
            panpot.add((byte) 0);
            panpot.add((byte) 0);
            reverb.add((byte) 0);
            reverb.add((byte) 0);
            chorus.add((byte) 0);
            chorus.add((byte) 0);
        }

        byte[] pac0 = new byte[128 + 9], pac1 = new byte[128 + 9];

        makeGSDBufPtn_4(level, (byte) (0x02 + adr2), pac0, pac1);
        DBuf.add(new CtlSysex(5, getSysEx(pac0)));
        DBuf.add(new CtlSysex(5, getSysEx(pac1)));

        makeGSDBufPtn_4(panpot, (byte) (0x06 + adr2), pac0, pac1);
        DBuf.add(new CtlSysex(5, getSysEx(pac0)));
        DBuf.add(new CtlSysex(5, getSysEx(pac1)));

        makeGSDBufPtn_4(reverb, (byte) (0x08 + adr2), pac0, pac1);
        DBuf.add(new CtlSysex(5, getSysEx(pac0)));
        DBuf.add(new CtlSysex(5, getSysEx(pac1)));

        makeGSDBufPtn_4(chorus, (byte) (0x0a + adr2), pac0, pac1);
        DBuf.add(new CtlSysex(5, getSysEx(pac0)));
        DBuf.add(new CtlSysex(5, getSysEx(pac1)));
    }

    private void makeGSDBufPtn_4(List<Byte> s, byte iAdr_mm, byte[] pac0, byte[] pac1) {
        pac0[0] = 0x41;
        pac0[1] = 0x10;
        pac0[2] = 0x42;
        pac0[3] = 0x12;
        pac0[4] = (byte) 0x83;
        pac0[5] = 0x49;
        pac0[6] = iAdr_mm;
        pac0[7] = 0x00;
        for (int i = 0; i < 128; i++) {
            pac0[i + 8] = s.get(i);
        }
        pac0[128 + 9 - 1] = (byte) 0x84;
        pac1[0] = 0x41;
        pac1[1] = 0x10;
        pac1[2] = 0x42;
        pac1[3] = 0x12;
        pac1[4] = (byte) 0x83;
        pac1[5] = 0x49;
        pac1[6] = (byte) (iAdr_mm + 1);
        pac1[7] = 0x00;
        for (int i = 0; i < 128; i++) {
            pac1[i + 8] = s.get(i + 128);
        }
        pac1[128 + 9 - 1] = (byte) 0x84;
    }

    private void getCM6Buf(List<CtlSysex> dBuf) {

        byte[] buf = null;
        for (Tuple<String, byte[]> trg : ExtendFile) {
            if (Path.getExtension(trg.getItem1()).equalsIgnoreCase(".CM6")) {
                buf = trg.getItem2();
            }
        }

        if (buf == null || buf.length < 1 || buf.length != 0x5849) return;

        // System Area
        dBuf.add(new CtlSysex(2, getSysEx(makeCM6Ptn_0(buf, 0x0080, 0x017, (byte) 0x10, (byte) 0x00, (byte) 0x00))));
        // Timbre Memory #1～ (User 128)
        for (int adr = 0x0e34, i = 0; adr <= 0x4d34; adr += 0x100, i += 2) {
            dBuf.add(new CtlSysex(9, getSysEx(makeCM6Ptn_0(buf, adr, 0x100, (byte) 0x08, (byte) (0x00 + i), (byte) 0x00))));
        }

        dBuf.add(new CtlSysex(9, getSysEx(makeCM6Ptn_0(buf, 0x0130, 0x100, (byte) 0x03, (byte) 0x01, (byte) 0x10))));
        dBuf.add(new CtlSysex(3, getSysEx(makeCM6Ptn_0(buf, 0x0230, 0x054, (byte) 0x03, (byte) 0x03, (byte) 0x10))));
        dBuf.add(new CtlSysex(5, getSysEx(makeCM6Ptn_0(buf, 0x00a0, 0x090, (byte) 0x03, (byte) 0x00, (byte) 0x00))));

        for (int adr = 0x0284, i = 0; adr <= 0x093e; adr += 0xf6, i += 0xf6) {
            dBuf.add(new CtlSysex(9, getSysEx(makeCM6Ptn_0(buf, adr, 0xf6, (byte) 0x04, (byte) (i >> 7), (byte) (i & 0x7f)))));
        }

        for (int adr = 0x0a34, i = 0; adr <= 0x0db4; adr += 0x80, i += 0x80) {
            dBuf.add(new CtlSysex(5, getSysEx(makeCM6Ptn_0(buf, adr, 0x80, (byte) 0x05, (byte) (i >> 7), (byte) (i & 0x7f)))));
        }

        dBuf.add(new CtlSysex(7, getSysEx(makeCM6Ptn_0(buf, 0x4e34, 0xbd, (byte) 0x50, (byte)0x00, (byte)0x00))));

        for (int adr = 0x4eb2, i = 0; adr <= 0x579a; adr += 0x98, i += 0x98) {
            dBuf.add(new CtlSysex(6, getSysEx(makeCM6Ptn_0(buf, adr, 0x98, (byte) 0x51, (byte) (i >> 7), (byte) (i & 0x7f)))));
        }

        dBuf.add(new CtlSysex(6, getSysEx(makeCM6Ptn_0(buf, 0x5832, 0x11, (byte) 0x52, (byte) 0x00, (byte) 0x00))));
    }

    private byte[] makeCM6Ptn_0(byte[] buf, int adr, int len, byte hh, byte mm, byte ll) {
        List<Byte> lst;

        lst = new ArrayList<>();
        lst.add((byte) 0x41);
        lst.add((byte) 0x10);
        lst.add((byte) 0x16);
        lst.add((byte) 0x12);
        lst.add((byte) 0x83);
        lst.add(hh);
        lst.add(mm);
        lst.add(ll);
        for (int i = 0; i < len; i++) {
            lst.add(buf[adr + i]);
        }
        lst.add((byte) 0x84);

        return toByteArray(lst);
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

                CtlSysex csx = beforeSend[i].get(sendControlIndex[i]);
                sendControlDelta[i] = csx.delta;
                chipRegister.sendMIDIout(model, 0, csx.data, vstDelta);

                sendControlIndex[i]++;
            } else {
                endFlg++;
            }
        }

        if (endFlg == beforeSend.length) {
            beforeSend = null;
            oneSyncTime = 60.0 / nowTempo / timeBase;
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
                case 0:// None
                    break;
                case 1:// GM Reset
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getGMReset());
                    break;
                case 2:// XG Reset
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getXGReset());
                    break;
                case 3:// GS Reset
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getGSReset());
                    break;
                case 4:// Custom
                    getCtlSysexFromText(beforeSend[i], setting.getMidiOut().getCustom());
                    break;
                }

                // ファイルパスが設定されている場合はコントロールファイルを読み込む処理を実施
                if (ExtendFile != null) {
                    getControlFile(beforeSend[i], infos[i].type);
                }

            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getCtlSysexFromText(List<CtlSysex> buf, String text) {
        if (text == null || text.length() < 1) return;

        String[] cmds = text.split(";");

        for (String cmd : cmds) {
            String[] com = cmd.split(":");
            int delay = Integer.parseInt(com[0]);
            String[] dats = com[1].split(",");
            byte[] dat = new byte[dats.length];
            for (int i = 0; i < dats.length; i++) {
                dat[i] = (byte) Integer.parseInt(dats[i], 16);
            }
            buf.add(new CtlSysex(delay, dat));
        }
    }

    private void getControlFile(List<CtlSysex> buf, int instType) {

        // GM / XG / GS / LA / GS(SC - 55_1) / GS(SC - 55_2)
        switch (instType) {
        case 0:// GM
        case 1:// XG
        case 2:// GS
            // Control なし
            break;
        case 3:// LA
            if (!controlFileCM6.equals("")) {
                getCM6Buf(buf);
            }
            break;
        case 4:// GS(SC - 55_1)
            if (!controlFileGSD.equals("")) {
                getGSD1Buf(buf);
            }
            break;
        case 5:// GS(SC - 55_2)
            if (!controlFileGSD2.equals("")) {
                getGSD2Buf(buf);
            }
            break;
        }

// #if DEBUG
        for (CtlSysex ex : buf) {
            System.err.printf("delta:%10d", ex.delta);
            for (byte b : ex.data) {
                System.err.printf("%02x ", b);
            }
            System.err.println();
        }
// #endif
    }
}

