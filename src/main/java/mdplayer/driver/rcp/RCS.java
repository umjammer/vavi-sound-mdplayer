package mdplayer.driver.rcp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.driver.mxdrv.MXDRV.Pcm8St;
import mdplayer.driver.rcp.MIDIEvent.MIDIEventType;
import mdplayer.driver.rcp.MIDIEvent.MIDISpEventType;
import mdplayer.driver.zms.Zms.MPCMSt;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kYm2151Inst;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


public class RCS {

    private static final Logger logger = getLogger(RCS.class.getName());

    public RCS() {
        musicStep = Common.VGMProcSampleRate / 60.0;
    }

    private double oneSyncTime = 0.009;
    private double musicStep = 1; // setting.outputDevice.SampleRate / 60.0;
    private double musicDownCounter = 0.0;

    List<CtlSysex>[] beforeSend = null;
    int[] sendControlDelta = null;
    int[] sendControlIndex = null;
    public String filename = "";

    public static class MIDIRythm {

        String name = "";
        int key = 0;
        int gt = 1;
    }

    public static class MIDIUserExclusive {

        private String name = "";
        private String memo = "";
        private byte[] exclusive = null;
    }

    public static class Tick {

        public int millisec = 0;
        public int count = 0;
        public int before = 0;
        public int sabun = 0;
    }

    private final Tick tick = new RCS.Tick();

    public List<Tuple<String, byte[]>> extendFiles = null;

    public static void getControlFileName(
            String fn,
            String supportfile,
            byte[] buf,
            /* out */ String[] sRCP,
            /* out */ String[] CM6,
            /* out */ String[] GSD,
            /* out */String[] GSD2) {
        sRCP[0] = null;
        CM6[0] = null;
        GSD[0] = null;
        GSD2[0] = null;

        PcmInfo[][] pcmInfos = new PcmInfo[1][127];
        byte[][] pcmData = new byte[1][];
        byte[][] rcpBuf = new byte[1][];

        Boolean rets = getRCSInfo(fn, supportfile, buf, /* out */ pcmInfos, /* out */ pcmData, /* out */ sRCP, /* ref */ rcpBuf);
        if (rets == null || rets == false) return;

        Boolean ret = checkHeadString(rcpBuf[0]);
        if (ret == null) return;
        boolean IsG36 = ret;
        int ptr = 96;
        if (IsG36) {
            ptr += 568;
            //.GSD
            GSD[0] = new String(rcpBuf[0], ptr, 12, charset).replace("\0", "");
            ptr += 16;
            //.GSD
            GSD2[0] = new String(rcpBuf[0], ptr, 12, charset).replace("\0", "");
            ptr += 16;
            //.CM6
            CM6[0] = new String(rcpBuf[0], ptr, 12, charset).replace("\0", "");
        } else {
            ptr += 358;
            //.CM6
            CM6[0] = new String(rcpBuf[0], ptr, 12, charset).replace("\0", "");
            ptr += 16;
            //.GSD
            GSD[0] = new String(rcpBuf[0], ptr, 12, charset).replace("\0", "");
        }
    }

    public static class PcmInfo {

        public int freq = 4;
        public int ptr = 0;
        public int length = 0;
        public int volume = 8;
        public int pan = 3;
    }

    final PcmInfo[][] pcmInfos = new PcmInfo[1][127];
    final byte[][] pcmData = new byte[1][];
    public X68kYm2151Inst opmPCM;
    public Pcm8PPInst pcm8pp;
    public int pcm8type = 1;
    private int rcsTrackNumber = 17; // default Track18
    private int rcsControlNoteNumber = 0;
    private int rcsControlMode = 0;
    private int rcsPolyphonicMode = 3;
    public final Pcm8St[] pcm8St = new Pcm8St[] {
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St(),
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St(),
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St(),
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St()
    };
    public MPCMSt[] mpcmSt = new MPCMSt[] {
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt(),
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt(),
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt(),
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt()
    };
    public String supportFileName = null;

    static boolean getRCSInfo(String filename, String supportfile, byte[] buf, /* out */ PcmInfo[][] pcmInfos, /* out */ byte[][] pcmData, /* out */ String[] rcpFilename, /* ref */ byte[][] rcpBuf) {
        pcmInfos[0] = null;
        pcmData[0] = null;
        rcpFilename[0] = "";
        if (buf == null) return false;
        if (buf.length < 0x4c0) return false;

        // Check RCS header
        if (buf[0] != 0x52 || buf[1] != 0x43 || buf[2] != 0x53 || buf[3] != 0x66
                || buf[4] != 0x6F || buf[5] != 0x72 || buf[6] != 0x6D || buf[7] != 0x31
                || buf[8] != 0x1A) {
            return false;
        }

        // Get RCP filename
        byte[] dmy = new byte[55];
        System.arraycopy(buf, 9, dmy, 0, 55);
        rcpFilename[0] = (new String(dmy)).trim().replace("\0", "");
        if (supportfile == null) {
            if (rcpBuf[0] == null) {
                if (filename != null && !filename.isEmpty()) {
                    rcpFilename[0] = Path.combine(Path.getDirectoryName(filename), rcpFilename[0]);
                }
                if (File.exists(rcpFilename[0])) rcpBuf[0] = File.readAllBytes(rcpFilename[0]);
            }
        } else {
            if (File.exists(supportfile)) rcpBuf[0] = File.readAllBytes(supportfile);
        }

        //Get PCM Information
        pcmInfos[0] = new PcmInfo[127];
        for (int i = 0; i < pcmInfos.length; i++) {
            pcmInfos[0][i] = new PcmInfo();
            pcmInfos[0][i].freq = buf[0x40 + i];
            pcmInfos[0][i].ptr = buf[0xc0 + i * 8] * 0x1000000 + buf[0xc1 + i * 8] * 0x10000
                    + buf[0xc2 + i * 8] * 0x100 + buf[0xc3 + i * 8];
            pcmInfos[0][i].length = buf[0xc4 + i * 8] * 0x1000000 + buf[0xc5 + i * 8] * 0x10000
                    + buf[0xc6 + i * 8] * 0x100 + buf[0xc7 + i * 8];
        }
        pcmData[0] = new byte[buf.length - 0xc0];
        System.arraycopy(buf, 0xc0, pcmData[0], 0, buf.length - 0xc0);

        return true;
    }

    BiConsumer<Integer, byte[]> midiSend;
    Consumer<String> lyric;
    Runnable counter;
    IntSupplier midiCount;
    Runnable stop;

    private void rcsControl(MIDITrack trk, MIDIEvent eve) {
        if (eve == null || eve.getMIDIMessage() == null) return;

        if (eve.getMIDIMessage().length < 3) return;
        if (eve.getMIDIMessage()[0] == (byte) 0xb0) { // Control
            if (eve.getMIDIMessage()[1] == 100)
                rcsControlNoteNumber = eve.getMIDIMessage()[2] & 0xff;
            else if (eve.getMIDIMessage()[1] == 101)
                rcsControlMode = eve.getMIDIMessage()[2] & 0xff;
            else if (eve.getMIDIMessage()[1] == 6) {
                int val = eve.getMIDIMessage()[2];
                switch (rcsControlMode) {
                    case 0: // Frequency change
                        if (pcm8type == 0)
                            if (val < 5) pcmInfos[0][rcsControlNoteNumber].freq = val;
                            else
                                pcmInfos[0][rcsControlNoteNumber].freq = val;
                        break;
                    case 1: // Panpot change
                        pcmInfos[0][rcsControlNoteNumber].pan = val;
                        break;
                    case 2: // Mode Select
                        rcsPolyphonicMode = val;
                        break;
                    case 3: // Request to change the track
                        rcsTrackNumber = val - 1;
                        break;
                    case 4: // Initial track change request
                        // ignore
                        break;
                }
            }
        }
    }

    void efNoteOn(MIDITrack trk, MIDIEvent eve) {
        if (eve.getGate() == 0) return;
        int okey = eve.getMIDIMessage()[1];
        //int key = (okey + ((trk.key != null) ? (int)trk.key : 0));
        int key = okey + trk.getKey();
        if (key < 0) key = 0;
        if (key > 127) key = 127;

        if (trk.getOutChannel() != null) {
            boolean flg = false;
            // key Off
            //if (trk.NoteGateTime[okey] <= trk.NextEventTick + trk.NowPart.StartTick)
            if (trk.getNoteGateTime()[okey] <= trk.getNextEventTick()) {
                msgBuf[0] = (byte) (MIDIEventType.NoteOff.v + trk.getOutChannel());
                msgBuf[1] = (byte) key;
                msgBuf[2] = 127;
                putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
                flg = true;
            }

            // key On
            if (trk.getNoteGateTime()[okey] == Integer.MAX_VALUE || flg) {
                msgBuf[0] = (byte) ((eve.getMIDIMessage()[0] & 0xf0) + trk.getOutChannel());
                msgBuf[1] = (byte) key;
                msgBuf[2] = eve.getMIDIMessage()[2];
                putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
            }
        }
        if (trk.getNumber() == rcsTrackNumber && isVirtualModel) {
            boolean flg = false;
            // key Off
            //if (trk.getNoteGateTime()[okey] <= trk.getNextEventTick() + trk.getNowPart.StartTick())
            if (trk.getNoteGateTime()[okey] <= trk.getNextEventTick()) {
                //logger.log(Level.TRACE, "KEY OFF:" + key + ":vel:" + 127);
                flg = true;
            }

            // key On
            if (trk.getNoteGateTime()[okey] == Integer.MAX_VALUE || flg) {
                //logger.log(Level.TRACE, "KEY ON :" + key + ":gt:" + eve.Gate+ ":vel:" + eve.MIDIMessage[2]);
                boolean[] keyoff = new boolean[1];
                int ch = keyOnPCM8(key, /* out */ keyoff);
                if (rcsPolyphonicMode == 1) ch = eve.getGate() - 1;
                if (ch >= 0 && ch < 16) {
                    int mode =
                            (((eve.getMIDIMessage()[2] & 0xff) / 8) << 16) |
                                    (pcmInfos[0][key].freq << 8) |
                                    (pcmInfos[0][key].pan);
                    int length = pcmInfos[key].length;
                    if (rcsPolyphonicMode == 0) length = (int) (length * Math.min(eve.getGate(), 100) * 0.01);
                    if (pcm8type == 0) if (opmPCM != null)
                        opmPCM.chips[0].pcm8Out(ch, null, pcmInfos[0][key].ptr, mode, length); // Start of specified channel sound
                    else if (pcm8pp != null)
                        pcm8pp.keyOn(0, ch, pcmInfos[0][key].ptr, mode, length); // Start of specified channel sound
                    pcm8St[ch].tablePtr = pcmInfos[0][key].ptr;
                    pcm8St[ch].mode = mode;
                    pcm8St[ch].length = pcmInfos[key].length;
                    pcm8St[ch].Keyon = true;
                }
            }
        }

        trk.getNoteGateTime()[okey] = trk.getNextEventTick() + eve.getGate();
    }

    private final List<Integer> freeCh = new ArrayList<>();
    private final List<Tuple<Integer, Integer>> useCh = new ArrayList<>();
    private static final int pcm8Chs = 16;

    private void initPCM8ch() {
        freeCh.clear();
        for (int i = 0; i < pcm8Chs; i++) freeCh.add(i);
        useCh.clear();
    }

    /**
     * keyon
     *
     * @param key    Key Number
     * @param keyoff out:keyoff instruction
     * @return Pronunciation channel (if there is a keyoff command, this channel must be keyed off before pronunciation)
     */
    private int keyOnPCM8(int key, /* out */ boolean[] keyoff) {
        keyoff[0] = false;

        int ch;
        if (!freeCh.isEmpty()) {
            // If the channel is free
            ch = freeCh.getFirst();
            freeCh.removeFirst();
            useCh.add(new Tuple<>(ch, key));
            return ch;
        }

        // If the channel is not available
        // (TBD For now, the pronunciations are cancelled out in order of most recent.)
        keyoff[0] = true;
        ch = useCh.getFirst().getItem1();//item1 = ch
        useCh.removeFirst();
        useCh.add(new Tuple<>(ch, key));
        return ch;
    }

    private void keyOffPCM8(int key) {
        var i = useCh.iterator();
        while (i.hasNext()) {
            var u = i.next();
            if (u.getItem2() != key) continue;
            freeCh.add(u.getItem1());
            i.remove();
        }
    }

    private boolean isG36 = false;
    private int ptr = 0;
    private int trkLen = 0;
    private int timeBase = 1;
    private double nowTempo = 120;
    private double Tempo = 0;
    private int beatDen = 0;
    private int beatMol = 0;
    private int key = 0;
    private int playBIAS = 0;
    private String controlFileGSD = "";
    private String controlFileGSD2 = "";
    private String controlFileCM6 = "";
    private int rcpVer = 0;
    private List<MIDIRythm> rtm;
    private List<MIDIUserExclusive> userExclusives;
    private MIDITrack[] trk = null;
    private MIDIPart[] prt = null;

    //private int trkTick = 0;
    //private int meaTick = 0;
    private int meaInd = 0;
    private boolean endTrack = false;
    private Map<Byte, Byte> taiDic = null;
    private int stDevNum = 0;
    private int pt = 0;
    private int skipPtr = 4;
    private byte[] msgBuf2 = new byte[2];
    private byte[] msgBuf3 = new byte[3];
    private final byte[] msgBuf = new byte[256];

    byte[] vgmBuf;
    boolean isVirtualModel;

    interface efd extends BiConsumer<MIDITrack, MIDIEvent> {

    }

    private final efd[] EventFunc = new efd[256];
    private final efd[] SpecialEventFunc = new efd[256];
    private int RelativeTempoChangeTargetTempo;
    private double RelativeTempoChangeTickSlice;
    private boolean RelativeTempoChangeSW = false;

    static Boolean checkHeadString(byte[] buf) {
        if (buf == null || buf.length < 32) return null;

        String str = new String(buf, 0, 32);
        if (!str.equals("RCM-PC98V2.0(C)COME ON MUSIC\r\n\0\0")) {
            if (!str.equals("COME ON MUSIC RECOMPOSER RCP3.0\0")) return null;
            else return true;
        }

        return false;
    }

    boolean getInformationHeader() {
        byte[] rcpBuf = null;
        if (extendFiles != null) {
            for (Tuple<String, byte[]> n : extendFiles) {
                if (n.getItem1().equals(".RCP")) {
                    rcpBuf = n.getItem2();
                    break;
                }
            }
        }
        if (supportFileName != null) {
            if (File.exists(supportFileName)) rcpBuf = File.readAllBytes(supportFileName);
        }

        if (isVirtualModel) {
            if (pcm8type == 0) if (opmPCM != null) opmPCM.chips[0].mountMemory(pcmData[0]);
            else if (pcm8pp != null) pcm8pp.writePcm(0, pcmData[0], 0,pcmData[0].length);
        }
        initPCM8ch();

        // At this point, swap the buffer
        vgmBuf = rcpBuf;
        Boolean ret = checkHeadString(vgmBuf);
        if (ret == null) return false;
        isG36 = ret;

        ptr = 32;
        ptr += 64;

        if (isG36) {
            headerG36();
        } else {
            headerRCP();
        }

        nowTempo = nowTempo == 0 ? 1 : nowTempo;
        timeBase = timeBase == 0 ? 1 : timeBase;
        oneSyncTime = 60.0 / nowTempo / timeBase;

        rhythm();

        userEx();

        trackData();

        init();

        EventFunc[0x00] = this::efMetaSeqNumber;
        EventFunc[0x01] = this::efMetaTextEvent;
        EventFunc[0x02] = this::efMetaCopyrightNotice;
        EventFunc[0x03] = this::efMetaTrackName;
        EventFunc[0x04] = this::efMetaInstrumentName;
        EventFunc[0x05] = this::efMetaLyric;
        EventFunc[0x06] = this::efMetaMarker;
        EventFunc[0x07] = this::efMetaCuePoint;
        EventFunc[0x08] = this::efMetaProgramName;
        EventFunc[0x09] = this::efMetaDeviceName;
        EventFunc[0x20] = this::efMetaChannelPrefix;
        EventFunc[0x21] = this::efMetaPortPrefix;
        EventFunc[0x2f] = this::efMetaEndOfTrack;
        EventFunc[0x51] = this::efMetaTempo;
        EventFunc[0x54] = this::efMetaSMPTEOffset;
        EventFunc[0x58] = this::efMetaTimeSignature;
        EventFunc[0x59] = this::efMetaKeySignature;
        EventFunc[0x7f] = this::efMetaSequencerSpecific;
        EventFunc[0x80] = this::efNoteOff;
        EventFunc[0x90] = this::efNoteOn;
        EventFunc[0xa0] = this::efKeyAfterTouch;
        EventFunc[0xb0] = this::efControlChange;
        EventFunc[0xc0] = this::efProgramChange;
        EventFunc[0xd0] = this::efChannelAfterTouch;
        EventFunc[0xe0] = this::efPitchBend;
        EventFunc[0xf0] = this::efSysExStart;
        EventFunc[0xf7] = this::efSysExContinue;
        for (int i = 0; i < 256; i++) {
            if (EventFunc[i] == null) EventFunc[i] = this::efn;
        }

        SpecialEventFunc[0x90] = this::sefUserExclusive1;
        SpecialEventFunc[0x91] = this::sefUserExclusive2;
        SpecialEventFunc[0x92] = this::sefUserExclusive3;
        SpecialEventFunc[0x93] = this::sefUserExclusive4;
        SpecialEventFunc[0x94] = this::sefUserExclusive5;
        SpecialEventFunc[0x95] = this::sefUserExclusive6;
        SpecialEventFunc[0x96] = this::sefUserExclusive7;
        SpecialEventFunc[0x97] = this::sefUserExclusive8;
        SpecialEventFunc[0x98] = this::sefChExclusive;
        SpecialEventFunc[0x99] = this::sefOutsideProcessExec;
        SpecialEventFunc[0xc0] = this::sefDX7Func;
        SpecialEventFunc[0xc1] = this::sefDXPara;
        SpecialEventFunc[0xc2] = this::sefDXRERF;
        SpecialEventFunc[0xc3] = this::sefTXFunc;
        SpecialEventFunc[0xc5] = this::sefFB01PPara;
        SpecialEventFunc[0xc6] = this::sefFB01SSystem;
        SpecialEventFunc[0xc7] = this::sefTX81ZVVCED;
        SpecialEventFunc[0xc8] = this::sefTX81ZAACED;
        SpecialEventFunc[0xc9] = this::sefTX81ZPPCED;
        SpecialEventFunc[0xca] = this::sefTX81ZSSystem;
        SpecialEventFunc[0xcb] = this::sefTX81ZEEffect;
        SpecialEventFunc[0xcc] = this::sefDX72RRemoteSW;
        SpecialEventFunc[0xcd] = this::sefDX72AACED;
        SpecialEventFunc[0xce] = this::sefDX72PPCED;
        SpecialEventFunc[0xcf] = this::sefTX802PPCED;
        SpecialEventFunc[0xd0] = this::sefYAMAHABase;
        SpecialEventFunc[0xd1] = this::sefYAMAHADev;
        SpecialEventFunc[0xd2] = this::sefYAMAHAAddrPara;
        SpecialEventFunc[0xd3] = this::sefYAMAHAXGAddrPara;
        SpecialEventFunc[0xdc] = this::sefMKS7;
        SpecialEventFunc[0xdd] = this::sefRolandBase;
        SpecialEventFunc[0xde] = this::sefRolandPara;
        SpecialEventFunc[0xdf] = this::sefRolandDev;
        SpecialEventFunc[0xe2] = this::sefBankProgram;
        SpecialEventFunc[0xe5] = this::sefKeyScan;
        SpecialEventFunc[0xe6] = this::sefMIDIChChange;
        SpecialEventFunc[0xe7] = this::sefTempoChange;
        SpecialEventFunc[0xf5] = this::sefKeyChange;
        SpecialEventFunc[0xf6] = this::sefCommentStart;
        SpecialEventFunc[0xf8] = this::sefLoopEnd;
        SpecialEventFunc[0xf9] = this::sefLoopStart;
        SpecialEventFunc[0xfc] = this::sefSameMeasure;
        SpecialEventFunc[0xfd] = this::sefMeasureEnd;
        SpecialEventFunc[0xfe] = this::sefEndofTrack;
        for (int i = 0; i < 256; i++) {
            if (SpecialEventFunc[i] == null) SpecialEventFunc[i] = this::efn;
        }

        return true;
    }

    private void headerG36() {
        // dummy Skip
        ptr += 64;
        // memo
        ptr += 360;
        // track number
        trkLen = vgmBuf[ptr++] & 0xff;
        if (trkLen != 18 && trkLen != 36) trkLen = 18;
        // dummy Skip
        ptr++;
        // Timebase
        timeBase = (vgmBuf[ptr] & 0xff) + ((vgmBuf[ptr + 1] & 0xff) * 0x100);
        timeBase = timeBase == 0 ? 1 : timeBase;
        ptr += 2;
        // Tempo
        nowTempo = vgmBuf[ptr++] & 0xff;
        if (nowTempo < 8 || nowTempo > 250) nowTempo = 120;
        Tempo = nowTempo;
        // dummy Skip
        ptr++;
        // beat (numerator)
        beatDen = vgmBuf[ptr++] & 0xff;
        // beat (denominator)
        beatMol = vgmBuf[ptr++] & 0xff;
        // key
        key = vgmBuf[ptr++] & 0xff;
        // Play BIAS
        playBIAS = vgmBuf[ptr++] & 0xff;
        // dummy Skip
        ptr += 6;
        // dummy Skip
        ptr += 16;
        // dummy Skip
        ptr += 112;
        // .GSD
        controlFileGSD = new String(vgmBuf, ptr, 12).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // .GSD
        controlFileGSD2 = new String(vgmBuf, ptr, 12).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // .CM6
        controlFileCM6 = new String(vgmBuf, ptr, 12).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // dummy Skip
        ptr += 80;
        rcpVer = 0;
    }

    private void headerRCP() {
        // memo
        ptr += 336;
        // dummy Skip
        ptr += 16;
        // Timebase lower
        timeBase = vgmBuf[ptr++] & 0xff;
        // Tempo
        nowTempo = vgmBuf[ptr++] & 0xff;
        Tempo = nowTempo;
        // beat (numerator)
        beatDen = vgmBuf[ptr++] & 0xff;
        // beat (denominator)
        beatMol = vgmBuf[ptr++] & 0xff;
        // key
        key = vgmBuf[ptr++] & 0xff;
        // Play BIAS
        playBIAS = vgmBuf[ptr++] & 0xff;
        // .CM6
        controlFileCM6 = new String(vgmBuf, ptr, 12).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // .GSD
        controlFileGSD = new String(vgmBuf, ptr, 12).replace("\0", "");
        ptr += 12;
        // dummy Skip
        ptr += 4;
        // truck number
        trkLen = vgmBuf[ptr++] & 0xff;
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
        // Timebase higher
        timeBase += (vgmBuf[ptr++] & 0xff) * 0x100;
        timeBase = timeBase == 0 ? 1 : timeBase;
        // dummy Skip
        // ignored
        // TONENAME.TB?
        // for now, ignored
        ptr = 0x206; // until rhythm definition
    }

    private void rhythm() {
        rtm = new ArrayList<>();
        int n = 32;
        if (isG36) n = 128;

        for (int i = 0; i < n; i++) {
            MIDIRythm r = new MIDIRythm();
            r.name = new String(vgmBuf, ptr, 14).replace("\0", "");
            ptr += 14;
            r.key = vgmBuf[ptr++] & 0xff;
            r.gt = vgmBuf[ptr++] & 0xff;
            rtm.add(r);
        }
    }

    private void userEx() {
        userExclusives = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            MIDIUserExclusive ux = new MIDIUserExclusive();
            ux.name = (new String(vgmBuf, ptr, 24)).replace("\0", "");
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
        initTrkPrt(); // Preparing tracks and bars

        for (int i = 0; i < trkLen; i++) {
            if (ptr >= vgmBuf.length) continue;

            int vgmBufptr = ptr;
            int trkSize = (vgmBuf[ptr++] & 0xff) * 0x100 + (vgmBuf[ptr++] & 0xff);

            // Size dummy(?) skip
            if (isG36) ptr += 2;

            int trkNumber = 0;
            if (isG36) {
                trkNumber = i;
                ptr++;
            } else {
                trkNumber = i;
                ptr++; // data[ptr++] - 1;
                if (trkNumber < 0)
                    trkNumber = i;
            }
            trk[trkNumber].setRythmMode((vgmBuf[ptr++] & 0xff) == 0x80);
            int ch = vgmBuf[ptr++] & 0xff;
            if (ch != 255) {
                int mc = midiCount.getAsInt();
                if (mc == 0) mc = 1;
                int n = (stDevNum + (ch / 16)) % mc;
                trk[trkNumber].setOutDeviceName("dummy"); // config.MIDIOutDeviceList[n].DevName;
                trk[trkNumber].setOutDeviceNumber(n); // config.MIDIOutDeviceList[n].DevNumber;
                trk[trkNumber].setOutUserDeviceNumber(n); // config.MIDIOutDeviceList[n].UsrNumber;
                trk[trkNumber].setOutUserDeviceName("dummy"); // config.MIDIOutDeviceList[n].UsrName;
                trk[trkNumber].setOutChannel(ch % 16);
            } else {
                trk[trkNumber].setOutDeviceName("Null Device");
                trk[trkNumber].setOutDeviceNumber(null);
                trk[trkNumber].setOutUserDeviceNumber(null);
                trk[trkNumber].setOutUserDeviceName("Null Device");
                trk[trkNumber].setOutChannel(null);
            }

            trk[trkNumber].setInDeviceName("Null Device");
            trk[trkNumber].setInDeviceNumber(null);
            trk[trkNumber].setInUserDeviceNumber(null);
            trk[trkNumber].setInUserDeviceName("Null Device");
            trk[trkNumber].setInChannel(null);

            trk[trkNumber].setKey(vgmBuf[ptr++] & 0xff);

            if ((trk[trkNumber].getKey() & 0x80) == 0x80) {
                trk[trkNumber].setKey(0);
            } else {
                trk[trkNumber].setKey((trk[trkNumber].getKey() > 63) ? trk[trkNumber].getKey() - 128 : trk[trkNumber].getKey());
            }
            trk[trkNumber].setSt(vgmBuf[ptr++] & 0xff);
            if (rcpVer > 0) {
                trk[trkNumber].setSt(trk[trkNumber].getSt() > 127 ? trk[trkNumber].getSt() - 256 : trk[trkNumber].getSt());
            }
            trk[trkNumber].setMute(vgmBuf[ptr++] == 1);
            trk[trkNumber].setName(new String(vgmBuf, ptr, 36).replace("\0", ""));
            ptr += 36;

            //trkTick = 0;
            taiDic = new HashMap<>();
            //meaTick = 0;
            meaInd = 0;
            musData(trk[trkNumber], vgmBuf);
            extractSame(trk[trkNumber]);
        }
    }

    private void extractSame(MIDITrack trk) {
        MIDIEvent evt = trk.getPart().getFirst().getStartEvent();
        while (evt != null) {
            if (evt.getEventType() == MIDIEventType.MetaSequencerSpecific && evt.getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.v) {

                int ofsMea = 0;
                if (isG36) {
                    ofsMea = (evt.getMIDIMessages()[0][0] & 0xff) + (evt.getMIDIMessages()[0][2] & 0xff) * 0x100;
                    //if (trkLen == 36) {
                    //    ofsMea = ofsMea * 6 - 242;
                    //}
                } else {
                    ofsMea = (evt.getMIDIMessages()[0][0] & 0xff) + (evt.getMIDIMessages()[0][1] & 3) * 0x100;
                }
                int Mea = 0;
                int MeaS = 0;
                MIDIEvent mEvt = trk.getPart().getFirst().getStartEvent();
                if (ofsMea != 0) {
                    while (mEvt != null) {
                        MeaS = 0;
                        if (mEvt.getEventType() == MIDIEventType.MetaSequencerSpecific) {
                            MIDIEvent nEvt = trk.getPart().getFirst().getNextEvent(mEvt);
                            int s = 0;
                            if (nEvt.getEventType() == MIDIEventType.MetaSequencerSpecific
                                    && nEvt.getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.v) {
                                s = 0;
                            } else {
                                s = 1;
                            }
                            if (mEvt.getMIDIMessage()[0] == (byte) MIDISpEventType.MeasureEnd.v) {
                                MeaS = s;
                            }
                            if (mEvt.getMIDIMessage()[0] == (byte) MIDISpEventType.SameMeasure.v) {
                                Mea++;
                                if (ofsMea == Mea) break;
                                MeaS = s;
                            }
                        }
                        mEvt = trk.getPart().getFirst().getNextEvent(mEvt);
                        Mea += MeaS;
                        if (ofsMea == Mea) break;
                    }
                }

                if (mEvt != null) {
                    evt.setSameMeasureIndex(mEvt.getNumber());
                }
            }

            evt = trk.getPart().getFirst().getNextEvent(evt);
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
                onpu(trkn, pk, pEvt); // Sounds like a note
            } else {
                command(trkn, pk, ebs, pEvt); // It seems to be a command
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
            case 0x98: // CH exclusive
                pt += skipPtr;
                ex = new ArrayList<>();
                ex.add((byte) 0xf0);
                while (ebs[pt] == (byte) 0xf7) {
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
                //if (isG36) ex.add((byte)(pk[2] / 0x100));
                ex.add((byte) (pk[3] & 0xff));
                //if (isG36) ex.add((byte)(pk[3] / 0x100));
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        pk[1],
                        MIDISpEventType.ChExclusive,
                        new byte[][] {
                                ByteUtil.toByteArray(ex)
                        });
                break;
            case 0x90: // User exclusive 1
            case 0x91: // User exclusive 2
            case 0x92: // User exclusive 3
            case 0x93: // User exclusive 4
            case 0x94: // User exclusive 5
            case 0x95: // User exclusive 6
            case 0x96: // User exclusive 7
            case 0x97: // User exclusive 8
            case 0xc0: // DX7 Function
            case 0xc1: // DX Parameter
            case 0xc2: // DX RERF
            case 0xc3: // TX Function
            case 0xc5: // FB-01 P Parameter
            case 0xc6: // FB-01 S System
            case 0xc7: // TX81Z V VCED
            case 0xc8: // TX81Z A ACED
            case 0xc9: // TX81Z P PCED
            case 0xca: // TX81Z S System
            case 0xcb: // TX81Z E EFFECT
            case 0xcc: // DX7-2 R Remote SW
            case 0xcd: // DX7-2 A ACED
            case 0xce: // DX7-2 P PCED
            case 0xcf: // TX802 P PCED
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
                        MIDISpEventType.valueOf(pk[0]),
                        new byte[][] {{(byte) pk[2], (byte) pk[3]}});
                pt += skipPtr;
                break;
            case 0xe2: // Bank & Program
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        pk[1],
                        MIDISpEventType.BankProgram,
                        new byte[][] {
                                {(byte) MIDIEventType.ProgramChange.v, (byte) pk[2]},
                                {(byte) MIDIEventType.ControlChange.v, 0x00, (byte) pk[3]}
                        });
                pt += skipPtr;
                break;
            case 0xe5: // KEY SCAN
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        pk[1],
                        MIDISpEventType.KeyScan,
                        new byte[][] {{(byte) pk[2]}});
                pt += skipPtr;
                break;
            case 0xe6: // MIDI CH
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        pk[1],
                        MIDISpEventType.MIDICh,
                        new byte[][] {{(byte) pk[2], (byte) pk[3]}});
                pt += skipPtr;
                break;
            case 0xe7: //TempoChange
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        pk[1],
                        MIDISpEventType.TempoChange,
                        new byte[][] {{(byte) pk[2], (byte) pk[3]}});
                pt += skipPtr;
                break;
            case 0xea: // After Touch Ch.
                trkn.getPart().get(meaInd).insertEvent(
                        pEvt,
                        pk[1],
                        MIDIEventType.ChannelAfterTouch,
                        new byte[] {(byte) MIDIEventType.ChannelAfterTouch.v, (byte) pk[2]}
                );
                pt += skipPtr;
                break;
            case 0xeb: // ControlChange
                trkn.getPart().get(meaInd).insertEvent(
                        pEvt,
                        pk[1],
                        MIDIEventType.ControlChange,
                        new byte[] {(byte) MIDIEventType.ControlChange.v, (byte) pk[2], (byte) pk[3]}
                );
                pt += skipPtr;
                break;
            case 0xec: // Program Change
                trkn.getPart().get(meaInd).insertEvent(
                        pEvt,
                        pk[1],
                        MIDIEventType.ProgramChange,
                        new byte[] {(byte) MIDIEventType.ProgramChange.v, (byte) pk[2]}
                );
                pt += skipPtr;
                break;
            case 0xed: // After Touch Pori.
                trkn.getPart().get(meaInd).insertEvent(
                        pEvt,
                        pk[1],
                        MIDIEventType.KeyAfterTouch,
                        new byte[] {(byte) MIDIEventType.KeyAfterTouch.v, (byte) pk[2], (byte) pk[3]}
                );
                pt += skipPtr;
                break;
            case 0xee: // Pitch Bend
                trkn.getPart().get(meaInd).insertEvent(
                        pEvt,
                        pk[1],
                        MIDIEventType.PitchBend,
                        new byte[] {(byte) MIDIEventType.PitchBend.v, (byte) pk[2], (byte) pk[3]}
                );
                pt += skipPtr;
                break;
            case 0xf5: // key Change
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        0,
                        MIDISpEventType.KeyChange,
                        new byte[][] {{(byte) pk[0]}}
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
                    while (ebs[pt] == (byte) 0xf7) {
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
                            new byte[][] {ByteUtil.toByteArray(ex)});
                } else {
                    ex.add((byte) pk[2]);
                    ex.add((byte) pk[3]);
                    while (ebs[pt] == (byte) 0xf7) {
                        pt += 2;
                        ex.add(ebs[pt++]);
                        ex.add(ebs[pt++]);
                    }
                    trkn.getPart().get(meaInd).insertSpEvent(
                            pEvt,
                            pk[1],
                            MIDISpEventType.Comment,
                            new byte[][] {ByteUtil.toByteArray(ex)});
                }
                break;
            case 0xf8: // Loop End
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        0,
                        MIDISpEventType.LoopEnd,
                        new byte[][] {{(byte) pk[1]}}
                );
                pt += skipPtr;
                break;
            case 0xf9: // Loop Start
                trkn.getPart().get(meaInd).insertSpEvent(
                        pEvt,
                        0,
                        MIDISpEventType.LoopStart,
                        new byte[][] {{0}}
                );
                pt += skipPtr;
                break;
            case 0xFC: // Same Measure
                if (isG36) {
                    trkn.getPart().get(meaInd).insertSpEvent(
                            pEvt,
                            0,
                            MIDISpEventType.SameMeasure,
                            new byte[][] {{(byte) pk[1], (byte) (pk[3] & 0xff), (byte) (pk[3] / 0x100)}}
                    );
                } else {
                    trkn.getPart().get(meaInd).insertSpEvent(
                            pEvt,
                            0,
                            MIDISpEventType.SameMeasure,
                            new byte[][] {{(byte) pk[1], (byte) pk[2], (byte) pk[3]}}
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
                        new byte[][] {{(byte) pk[1], (byte) pk[2], (byte) pk[3]}}
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
        trk = new MIDITrack[trkLen];
        prt = new MIDIPart[trkLen];
        for (int i = 0; i < trkLen; i++) {
            trk[i] = new MIDITrack();
            trk[i].setBeforeIndex((i - 1 < 0) ? null : i - 1);
            trk[i].setAfterIndex(i + 1);
            trk[i].setNumber(i);
            trk[i].clearAllPartMemory();
            prt[i] = new MIDIPart();
            prt[i].setName("Track %d Part".formatted(i + 1));
            trk[i].insertPart(0, prt[i]);
        }
    }

    private void init() {

        tick.millisec = 0;
        tick.count = 0;
        tick.before = 0;
        tick.sabun = 0;
        //ps.timeBase = prj.Information.timeBase; // resolution
        //ps.Tempo = prj.Information.Tempo; // Tempo: Quarter note
        //ps.BaseTempo = prj.Information.Tempo; // Tempo: Quarter note
        //ps.beatDen = prj.Information.beatDen;
        //ps.beatMol = prj.Information.beatMol;
        //ps.Lyric = "";
        //prj.RelativeTempoChangeNowTempo = prj.Information.Tempo;
        //prj.RelativeTempoChangeSW = false;

        int minSt = Integer.MAX_VALUE;
        for (MIDITrack tk : trk) {
            minSt = Math.min(tk.getSt(), minSt);
        }
        minSt = (minSt < 0 ? -minSt : 0);

        // Per-track initialization
        for (MIDITrack tk : trk) {
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

            //for (clsConfig.clsMIDIDeviceList dev : Config.MIDIOutDeviceList) {
            //    if (dev.UsrNumber == tk.OutUserDeviceNumber) {
            //        tk.OutDeviceName = dev.DevName;
            //        tk.OutDeviceNumber = dev.DevNumber;
            //    }
            //}
        }

        //// MIDI Clock Generation
        //MIDIClock = new MIDIClock();
        //MIDIClock.Create(0, ps.timeBase, 60000000 / ps.Tempo);
        //// Resetting and starting the MIDI clock
        //MIDIClock.Reset();
        //MIDIClock.Start();
    }

    void oneFrameMain() {
        try {
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
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameRCP() {
        // Ritardando processing
        if (RelativeTempoChangeSW) {
            nowTempo += RelativeTempoChangeTickSlice;
            if (RelativeTempoChangeTickSlice <= 0) {
                if (RelativeTempoChangeTargetTempo >= nowTempo) {
                    nowTempo = RelativeTempoChangeTargetTempo;
                    RelativeTempoChangeSW = false;
                }
            } else {
                if (RelativeTempoChangeTargetTempo <= nowTempo) {
                    nowTempo = RelativeTempoChangeTargetTempo;
                    RelativeTempoChangeSW = false;
                }
            }

            oneSyncTime = 60.0 / nowTempo / timeBase;
        }

        boolean endMark = true;
        for (MIDITrack tk : trk) {
            if (!tk.getEndMark()) {
                endMark = false;
                TrackProcess(tk);
            }
        }

        tick.count++;
        //if (prj.RelativeTempoChangeSW) {
        //}

        if (endMark) {
            stop.run();
        }
    }

    /**
     * Per-Track Processing
     */
    private void TrackProcess(MIDITrack trk) {
        MIDIPart prt = trk.getNowPart();

        if (prt.getENowIndex() == null) {
            if (!checkNoteOff(trk, 0)) {
                trk.setEndMark(true);
            }
        }
        // When the start of the part has not been reached
        if (prt.getStartTick() > tick.count) {
            checkNoteOff(trk, 0);
            return;
        }

        // Part processing is performed
        partProcess(trk);
        checkNoteOff(trk, 0);

        // Because it may move to the next part
        prt = trk.getNowPart();
        // Update ticks until event transmission is complete
        trk.setNowTick(tick.count - prt.getStartTick());

        if (trk.getEndMark()) return;

        if (trk.getNowPart().getENowIndex() != null) return;

        // Move to next measure
        if (prt.getAfterIndex() != null) {
            trk.setNowPart(trk.getNextPart(prt));
            trk.setNowTick(0);
            trk.setNextEventTick(0);
            trk.getNowPart().setENowIndex(trk.getNowPart().getEStartIndex());
            TrackProcess(trk);
        }
    }

    /**  */
    private boolean checkNoteOff(MIDITrack trk, int mode) {
        boolean flg = false;

        for (int n = 0; n < trk.getNoteGateTime().length; n++) {
            if (trk.getNoteGateTime()[n] != Integer.MAX_VALUE) {
                //if (trk.NoteGateTime[n] <= trk.NowTick + trk.NowPart.StartTick)
                if (trk.getNoteGateTime()[n] <= trk.getNowTick()) {
                    //int key = (n + ((trk.key != null) ? (int)trk.key : 0));
                    int key = n + trk.getKey();
                    if (key < 0) key = 0;
                    else if (key > 127) key = 127;
                    if (trk.getOutChannel() != null) {
                        if (rcsTrackNumber != trk.getNumber()) {
                            msgBuf[0] = (byte) (MIDIEventType.NoteOff.v + trk.getOutChannel());
                            msgBuf[1] = (byte) key;
                            msgBuf[2] = 127;
                            putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, 3);
                        } else {
                            keyOffPCM8(key);
                        }
                    }
                    trk.getNoteGateTime()[n] = Integer.MAX_VALUE;
                    flg = true;
                }
            }
        }

        return flg;
    }

    private final byte[] vv = new byte[1];

    private void putMIDIMessage(Integer n, byte[] pMIDIMessage, int len) {
        if (n == null) return;
        List<Byte> dat = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            dat.add(pMIDIMessage[i]);
            //plugin.audio.chipRegister.plugin(MidiPlugin.class).send(model, n, vv, vstDelta);
        }
        midiSend.accept(n, ByteUtil.toByteArray(dat));
    }

    /**
     * Part-by-part processing
     */
    private void partProcess(MIDITrack trk) {
        MIDIPart prt = trk.getNowPart();

        // Do not process if no part information is available
        if (prt == null || prt.getENowIndex() == null) return;
        // Do not process if there are no events in the part
        if (prt.getEvents() == null || prt.getEvents().isEmpty()) {
            prt.setENowIndex(null);
            return;
        }

        // Wait until the next event in the track
        if (trk.getNextEventTick() > trk.getNowTick()) return;
        // Extract one event
        MIDIEvent eve = prt.getEvents().get(prt.getENowIndex());
        // Calculating the error
        eve.setGosa(trk.getNowTick() - trk.getNextEventTick());

        // Event transmission
        sendEvent(trk, eve);

        // Set the next event trigger time
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
     * Event transmission
     */
    private void sendEvent(MIDITrack trk, MIDIEvent eve) {
        //if (trk.outDeviceNumber == null) return;
        //if (trk.outUserDeviceNumber == null) return;
        //if (!config.MIDIOutDeviceList[(int) trk.outUserDeviceNumber].DevAlive) return;
        if (trk.getNumber() == rcsTrackNumber) {
            //logger.log(Level.DEBUG, model + ":" + eve.EventType);
        }
        if (eve.getEventType() == MIDIEventType.NoteON && trk.getMute()) return;

        EventFunc[eve.getEventType().v].accept(trk, eve);
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
        if (trk.getNumber() == rcsTrackNumber) {
            rcsControl(trk, eve);
        }

        if (trk.getOutChannel() == null) return;
        msgBuf[0] = eve.getMIDIMessage()[0];
        eve.getMIDIMessage()[0] &= 0xf0;
        eve.getMIDIMessage()[0] += (byte) (int) trk.getOutChannel();
        putMIDIMessage(trk.getOutDeviceNumber(), eve.getMIDIMessage(), 3);
        eve.getMIDIMessage()[0] = msgBuf[0];
    }

    void efnbyteMsg(MIDITrack trk, MIDIEvent eve) {
        putMIDIMessage(trk.getOutDeviceNumber(), eve.getMIDIMessage(), eve.getMIDIMessage().length);
    }

    void efMetaSeqNumber(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaSeqNumber is not implemented yet!");
    }

    void efMetaTextEvent(MIDITrack trk, MIDIEvent eve) {
        trk.setComment(new String(eve.getMIDIMessage()).replace("\0", ""));
    }

    void efMetaCopyrightNotice(MIDITrack trk, MIDIEvent eve) {
        //prj.Information.Copyright = (new String(eve.MIDIMessage)).replace("\0", "");
    }

    void efMetaTrackName(MIDITrack trk, MIDIEvent eve) {
        trk.setName(new String(eve.getMIDIMessage()).replace("\0", ""));

    }

    void efMetaInstrumentName(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaInstrumentNameis not implemented yet!");
    }

    void efMetaLyric(MIDITrack trk, MIDIEvent eve) {
        //ps.Lyric = (new String(eve.MIDIMessage, 2, eve.MIDIMessage.length - 2)).replace("\0", "");
    }

    void efMetaMarker(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaMarkeris not implemented yet!");
    }

    void efMetaCuePoint(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaCuePointis not implemented yet!");
    }

    void efMetaProgramName(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaProgramNameis not implemented yet!");
    }

    void efMetaDeviceName(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaDeviceNameis not implemented yet!");
    }

    void efMetaChannelPrefix(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaChannelPrefixis not implemented yet!");
    }

    void efMetaPortPrefix(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaPortPrefixis not implemented yet!");
        logger.log(Level.DEBUG, "+Track.Number[%d] Event.Index[%d]".formatted(trk.getNumber(), eve.getNumber()));
        logger.log(Level.DEBUG, "+Message [%d,%d,%d]".formatted(eve.getMIDIMessage()[0] & 0xff, eve.getMIDIMessage()[1] & 0xff, eve.getMIDIMessage()[2] & 0xff));
    }

    void efMetaEndOfTrack(MIDITrack trk, MIDIEvent eve) {
        // No action is required at this time
    }

    void efMetaTempo(MIDITrack trk, MIDIEvent eve) {
        //int Tempo = eve.getMIDIMessage()[2] * 0x10000 + eve.getMIDIMessage()[3] * 0x100 + eve.getMIDIMessage()[4];
        //MIDIClock.Stop();
        //MIDIClock.SetTempo(Tempo);
        //MIDIClock.Start();
        //ps.Tempo = 60000000 / Tempo;
    }

    void efMetaSMPTEOffset(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaSMPTEOffsetis not implemented yet!");
    }

    void efMetaTimeSignature(MIDITrack trk, MIDIEvent eve) {
        beatDen = (int) eve.getMIDIMessage()[2] & 0xff; // numerator
        beatMol = (int) Math.pow(2.0, eve.getMIDIMessage()[3] & 0xff); // denominator
    }

    void efMetaKeySignature(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "MetaKeySignatureis not implemented yet!Track.Number[%d] Event.Index[%d]".formatted(trk.getNumber(), eve.getNumber()));
    }

    void efNoteOff(MIDITrack trk, MIDIEvent eve) {
        //ef3byteMsg(trk, eve);
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
        SpecialEventFunc[eve.getMIDIMessage()[0]].accept(trk, eve);
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
            n = switch (n & 0xff) {
                case 0x80 -> eve.getMIDIMessages()[0][eve.getMIDIMessages()[0].length - 2];
                case 0x81 -> eve.getMIDIMessages()[0][eve.getMIDIMessages()[0].length - 1];
                case 0x82 -> (byte) (int) trk.getOutChannel();
                case 0x83 -> {
                    chksum = 0;
                    yield null;
                }
                case 0x84 -> (byte) (128 - (chksum % 128));
                default -> n;
            };
            if (n != null) {
                msgBuf[i] = n;
                chksum += n & 0xff;
                i++;
            }
            j++;
            if (n == (byte) 0xf7) break;
            if (i >= msgBuf.length) {
                logger.log(Level.DEBUG, "sefChExclusive: Detects and skips exclusives that exceed the buffer.");
                return; // Do not send exclusive when buffer is over
            }
        }
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, i);
    }

    void sefOutsideProcessExec(MIDITrack trk, MIDIEvent eve) {
        logger.log(Level.DEBUG, "spEventOutsideProcessExecis not implemented yet!");
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
        logger.log(Level.DEBUG, "spEventKeyScanis not implemented yet!");
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
        double mul = (eve.getMIDIMessages()[0][0] & 0xff) / 64.0;

        if (eve.getMIDIMessages()[0][1] == 0) {
            int Tempo = (int) (this.Tempo * mul);
            if (Tempo < 10) Tempo = 10;
            else if (Tempo > 240) Tempo = 240;
            nowTempo = Tempo;
            oneSyncTime = 60.0 / nowTempo / timeBase;
        } else {
            // Ritardando
            int Tempo = (int) (this.Tempo * mul);
            double s = (Tempo - this.Tempo) * 256.0 / ((256.0 - (eve.getMIDIMessages()[0][1] & 0xff)) * timeBase);
            RelativeTempoChangeTargetTempo = Tempo;
            RelativeTempoChangeTickSlice = (nowTempo < Tempo) ? s : -s;
            RelativeTempoChangeSW = true;
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
        trk.setRolandPara_gt(eve.getMIDIMessages()[0][0]);
        trk.setRolandPara_vel(eve.getMIDIMessages()[0][1]);

        msgBuf[0] = (byte) 0xf0;
        msgBuf[1] = 0x41;
        msgBuf[2] = trk.getRolandDev_gt();
        msgBuf[3] = trk.getRolandDev_vel();
        msgBuf[4] = 0x12;
        msgBuf[5] = trk.getRolandBase_gt();
        msgBuf[6] = trk.getRolandBase_vel();
        msgBuf[7] = trk.getRolandPara_gt();
        msgBuf[8] = trk.getRolandPara_vel();
        msgBuf[9] = (byte) ((128 - ((trk.getRolandBase_gt() + trk.getRolandBase_vel() + trk.getRolandPara_gt() + trk.getRolandPara_vel()) % 128)) & 0x7f);
        msgBuf[10] = (byte) 0xf7;
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

        trk.setKeySIG_SF(sf); // Sharp Flat -7:7flats -1:1flat 0:key of C 1:1sharp 7:7sharp
        trk.setKeySIG_MI(mi); // Is minor key
    }

    void sefCommentStart(MIDITrack trk, MIDIEvent eve) {
        trk.setComment(new String(eve.getMIDIMessages()[0]).replace("\0", ""));
        lyric.accept(trk.getComment());
    }

    void sefLoopEnd(MIDITrack trk, MIDIEvent eve) {
        if (trk.getLoopTargetEvents().isEmpty()) return;
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
            return;
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

        while (j < userExclusives.get(num).exclusive.length) {
            Byte n = userExclusives.get(num).exclusive[j];
            n = switch (n & 0xff) {
                case 0x80 -> eve.getMIDIMessages()[0][0];
                case 0x81 -> eve.getMIDIMessages()[0][1];
                case 0x82 -> (byte) (int) trk.getOutChannel();
                case 0x83 -> {
                    chksum = 0;
                    yield null;
                }
                case 0x84 -> (byte) ((128 - (chksum % 128)) & 0x7f);
                default -> n;
            };
            if (n != null) {
                msgBuf[i] = n;
                chksum += n;
                i++;
            }
            j++;
            if (n == 0xf7) break;
            if (i >= msgBuf.length) {
                logger.log(Level.DEBUG, "sefUserExclusiveN: Detects and skips exclusives that exceed the buffer.");
                return; // Do not send exclusive when buffer is over
            }
        }
        putMIDIMessage(trk.getOutDeviceNumber(), msgBuf, i);
    }

    public static class CtlSysex {

        public CtlSysex(int d, byte[] dat) {
            delta = d;
            data = dat;
        }

        public int delta = 0;
        public byte[] data = null;
    }

    private byte[] getSysEx(byte... buf) {
        List<Byte> ret = new ArrayList<>();
        int chksum = 0;

        ret.add((byte) 0xf0);

        for (Byte b : buf) {
            Byte n = b;
            n = switch (n & 0xff) {
                //case 0x82:
                //  n = (byte)trk.OutChannel;
                //break;
                case 0x83 -> {
                    chksum = 0;
                    yield null;
                }
                case 0x84 -> (byte) ((128 - (chksum % 128)) & 0x7f);
                default -> n;
            };
            if (n != null) {
                ret.add(n);
                chksum += n;
            }
        }

        ret.add((byte) 0xf7);

        return ByteUtil.toByteArray(ret);
    }

    private void getGSD1Buf(/* ref */ List<CtlSysex> DBuf) {
        byte[] buf = null;
        for (Tuple<String, byte[]> trg : extendFiles) {
            if (Path.getExtension(trg.getItem1()).equalsIgnoreCase(".GSD")) {
                buf = trg.getItem2();
                break;
            }
        }
        getGSDBuf(/* ref */ DBuf, buf);
    }

    private void getGSD2Buf(/* ref */ List<CtlSysex> DBuf) {
        byte[] buf = null;
        for (Tuple<String, byte[]> trg : extendFiles) {
            if (Path.getExtension(trg.getItem1()).equalsIgnoreCase(".GSD")) {
                buf = trg.getItem2();
            }
        }
        getGSDBuf(/* ref */ DBuf, buf);
    }

    private void getGSDBuf(/* ref */ List<CtlSysex> DBuf, byte[] buf) {
        if (buf == null || buf.length < 1 || buf.length != 0xa71) return;

        int adr;

        for (int ch = 0; ch < 16; ch++) {
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x65, 0x00})); // RPN Master fine tuning
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x64, 0x01}));
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x06, (byte) ((buf[0xa6f] * 0x100 + buf[0xa6e]) >> 7)}));
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x26, (byte) ((buf[0xa6f] * 0x100 + buf[0xa6e]) & 0x7f)}));
        }

        // Master Volume
        DBuf.add(new CtlSysex(1, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83, (byte) 0x40, (byte) 0x00, (byte) 0x04, buf[0x24], (byte) 0x84)));
        DBuf.add(new CtlSysex(4, getSysEx((byte) 0x7F, (byte) 0x7F, (byte) 0x04, (byte) 0x01, (byte) ((buf[0x24] * 0x81) & 0x7F), (byte) (((buf[0x24] * 0x81) >> 7) & 0x7f))));

        for (int ch = 0; ch < 16; ch++) {
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x65, 0x00})); // RPN Master Coarse tuning
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x64, 0x02}));
            DBuf.add(new CtlSysex(1, new byte[] {(byte) (0xb0 + ch), 0x06, (byte) (buf[0xa70] & 0x7f)}));
        }

        // Master Pan
        DBuf.add(new CtlSysex(1, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83, (byte) 0x40, (byte) 0x00, (byte) 0x06, buf[0x26], (byte) 0x84)));
        // Master Balance
        DBuf.add(new CtlSysex(1, getSysEx((byte) 0x7f, (byte) 0x7f, (byte) 0x04, (byte) 0x02, (byte) ((buf[0x26] * 0x80) & 0x7F), (byte) (((buf[0x26] * 0x80) >> 7) & 0x7f))));

        // Voice Reserve Loc:Ch partdata - 1 Len:1
        DBuf.add(new CtlSysex(1, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83,
                (byte) 0x40, (byte) 0x01, (byte) 0x10,
                buf[0x4f9], buf[0x0af], buf[0x129], buf[0x1a3], // 10ch  1ch  2ch  3ch
                buf[0x21d], buf[0x297], buf[0x311], buf[0x38b], //  4ch  5ch  6ch  7ch
                buf[0x405], buf[0x47f], buf[0x573], buf[0x5ed], //  8ch  9ch 11ch 12ch
                buf[0x667], buf[0x6e1], buf[0x75b], buf[0x7d5], // 13ch 14ch 15ch 16ch
                (byte) 0x84)));

        // Reverb Loc:0x27 Len:7
        DBuf.add(new CtlSysex(1, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83,
                (byte) 0x40, (byte) 0x01, (byte) 0x30,
                buf[0x27], buf[0x28], buf[0x29], buf[0x2a],
                buf[0x2b], buf[0x2c], buf[0x2d],
                (byte) 0x84)));

        // Chorus Loc:0x2E Len:8
        DBuf.add(new CtlSysex(1, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83,
                (byte) 0x40, (byte) 0x01, (byte) 0x38,
                buf[0x2e], buf[0x2f], buf[0x30], buf[0x31],
                buf[0x32], buf[0x33], buf[0x34], buf[0x35],
                (byte) 0x84)));

        for (int i = 0; i < 16; i++) {
            adr = i * 0x7a + 0x36;
            byte ch = buf[adr + 0x2]; // MIDI CH
            byte iAdrMm;
            byte iAdrLl;

            makeGSDBufPtn_0(DBuf, buf, adr, ch);

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
            makeGSDBufPtn_1(DBuf, buf, adr, iAdrMm, iAdrLl);

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
            makeGSDBufPtn_2(DBuf, buf, adr, iAdrMm, iAdrLl);
        }

        adr = 0x7d6;
        makeGSDBufPtn_3(DBuf, buf, adr, 0x00);
        adr = 0x922;
        makeGSDBufPtn_3(DBuf, buf, adr, 0x10);
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
        DBuf.add(new CtlSysex(5, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83,
                (byte) 0x48, iAdrMm, iAdrLl,
                (byte) (buf[adr + 0x00] >> 4), (byte) (buf[adr + 0x00] & 0xf), // BANK(LSB) 0 1
                (byte) (buf[adr + 0x01] >> 4), (byte) (buf[adr + 0x01] & 0xf), // PROGRAM CHANGE 2 3
                (byte) (((buf[adr + 0x03] & 1) << 3) | ((buf[adr + 0x04] & 1) << 2) | ((buf[adr + 0x05] & 1) << 1) | ((buf[adr + 0x06] & 1) << 0)), //PITCH BEND + CH PRESSURE + PROGRAM CHANGE + CONTROL CHANGE 4
                (byte) (((buf[adr + 0x07] & 1) << 3) | ((buf[adr + 0x08] & 1) << 2) | ((buf[adr + 0x09] & 1) << 1) | ((buf[adr + 0x0a] & 1) << 0)), //POLY PRESSURE + NOTE MESSAGE + RPN + NRPN 5
                (byte) (((buf[adr + 0x0b] & 1) << 3) | ((buf[adr + 0x0c] & 1) << 2) | ((buf[adr + 0x0d] & 1) << 1) | ((buf[adr + 0x0e] & 1) << 0)), //MODURATION + VOLUME + PANPOT + EXPRESSION 6
                (byte) (((buf[adr + 0x0f] & 1) << 3) | ((buf[adr + 0x10] & 1) << 2) | ((buf[adr + 0x11] & 1) << 1) | ((buf[adr + 0x12] & 1) << 0)), //HOLD1 + PORTMENT + SOSTENUTE + SOFT 7
                (byte) (buf[adr + 0x02] >> 4), (byte) (buf[adr + 0x02] & 0xf), // MIDI CH 8 9
                (byte) (((buf[adr + 0x13] & 1) << 3) | ((buf[adr + 0x15] & 3) << 1) | ((buf[adr + 0x15] & 3) != 0 ? 1 : 0)), //MONO/PORY MODE + ASSIGN MODE  10
                (byte) (((buf[adr + 0x14] & 3))), // USE FOR RHYTHM PART 11
                (byte) (buf[adr + 0x16] >> 4), (byte) (buf[adr + 0x16] & 0xf), // PITCH KEY SHIFT 12,13
                buf[adr + 0x17], // PITCH OFFSET FINE              14
                buf[adr + 0x18], // PITCH OFFSET FINE  (NIBBLIZED) 15
                (byte) (buf[adr + 0x19] >> 4), (byte) (buf[adr + 0x19] & 0xf), // PART LEVEL 16,17
                (byte) (buf[adr + 0x1c] >> 4), (byte) (buf[adr + 0x1c] & 0xf), // PART PANPOT 18,19
                (byte) (buf[adr + 0x1a] >> 4), (byte) (buf[adr + 0x1a] & 0xf), // VELOCITY SENSE DEPTH 22,23 (20,21 ?)
                (byte) (buf[adr + 0x1b] >> 4), (byte) (buf[adr + 0x1b] & 0xf), // VELOCITY SENSE OFFSET 20,21 (22,23 ?)
                (byte) (buf[adr + 0x1d] >> 4), (byte) (buf[adr + 0x1d] & 0xf), // KEY RANGE LOW 24,25
                (byte) (buf[adr + 0x1e] >> 4), (byte) (buf[adr + 0x1e] & 0xf), // KEY RANGE HIGH 26,27
                (byte) (buf[adr + 0x21] >> 4), (byte) (buf[adr + 0x21] & 0xf), // CHOURS SEND DEPTH 28,29
                (byte) (buf[adr + 0x22] >> 4), (byte) (buf[adr + 0x22] & 0xf), // REVERB SEND DEPTH 30,31

                (byte) (buf[adr + 0x23] >> 4), (byte) (buf[adr + 0x23] & 0xf), // TONE MODEFY 1 32,33
                (byte) (buf[adr + 0x24] >> 4), (byte) (buf[adr + 0x24] & 0xf), // TONE MODEFY 2 34,35
                (byte) (buf[adr + 0x25] >> 4), (byte) (buf[adr + 0x25] & 0xf), // TONE MODEFY 3 36,37
                (byte) (buf[adr + 0x26] >> 4), (byte) (buf[adr + 0x26] & 0xf), // TONE MODEFY 4 38,39
                (byte) (buf[adr + 0x27] >> 4), (byte) (buf[adr + 0x27] & 0xf), // TONE MODEFY 5 40,41
                (byte) (buf[adr + 0x28] >> 4), (byte) (buf[adr + 0x28] & 0xf), // TONE MODEFY 6 42,43
                (byte) (buf[adr + 0x29] >> 4), (byte) (buf[adr + 0x29] & 0xf), // TONE MODEFY 7 44,45
                (byte) (buf[adr + 0x2a] >> 4), (byte) (buf[adr + 0x2a] & 0xf), // TONE MODEFY 8 46,47
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, // (The value of DATA 48,49,50,51 is 0)
                (byte) (buf[adr + 0x2b] >> 4), (byte) (buf[adr + 0x2b] & 0xf), // SCALE TUNIG C  52,53
                (byte) (buf[adr + 0x2c] >> 4), (byte) (buf[adr + 0x2c] & 0xf), // SCALE TUNIG C# 54,55
                (byte) (buf[adr + 0x2d] >> 4), (byte) (buf[adr + 0x2d] & 0xf), // SCALE TUNIG D  56,57
                (byte) (buf[adr + 0x2e] >> 4), (byte) (buf[adr + 0x2e] & 0xf), // SCALE TUNIG D# 58,59
                (byte) (buf[adr + 0x2f] >> 4), (byte) (buf[adr + 0x2f] & 0xf), // SCALE TUNIG E  60,61
                (byte) (buf[adr + 0x30] >> 4), (byte) (buf[adr + 0x30] & 0xf), // SCALE TUNIG F  62,63
                (byte) (buf[adr + 0x31] >> 4), (byte) (buf[adr + 0x31] & 0xf), // SCALE TUNIG F# 64,65
                (byte) (buf[adr + 0x32] >> 4), (byte) (buf[adr + 0x32] & 0xf), // SCALE TUNIG G  66,67
                (byte) (buf[adr + 0x33] >> 4), (byte) (buf[adr + 0x33] & 0xf), // SCALE TUNIG G# 68,69
                (byte) (buf[adr + 0x34] >> 4), (byte) (buf[adr + 0x34] & 0xf), // SCALE TUNIG A  70,71
                (byte) (buf[adr + 0x35] >> 4), (byte) (buf[adr + 0x35] & 0xf), // SCALE TUNIG A# 72,73
                (byte) (buf[adr + 0x36] >> 4), (byte) (buf[adr + 0x36] & 0xf), // SCALE TUNIG B  74,75

                (byte) (buf[adr + 0x1f] >> 4), (byte) (buf[adr + 0x1f] & 0xf), // CC1 CONTROLLER NUMBER 76,77
                (byte) (buf[adr + 0x20] >> 4), (byte) (buf[adr + 0x20] & 0xf), // CC2 CONTROLLER NUMBER 78,79

                (byte) (buf[adr + 0x37] >> 4), (byte) (buf[adr + 0x37] & 0xf), // MOD  PITCH CONTROL      80,81
                (byte) (buf[adr + 0x38] >> 4), (byte) (buf[adr + 0x38] & 0xf), // MOD  TVF CUTOFF CONTROL 82,83
                (byte) (buf[adr + 0x39] >> 4), (byte) (buf[adr + 0x39] & 0xf), // MOD  AMPLITUDE CONTROL  84,85
                (byte) 0, (byte) 0, // (The value of DATA 86,87 is 0)
                (byte) (buf[adr + 0x3a] >> 4), (byte) (buf[adr + 0x3a] & 0xf), // MOD  LFO1 RATE CONTROL  90,91
                (byte) (buf[adr + 0x3b] >> 4), (byte) (buf[adr + 0x3b] & 0xf), // MOD  LFO1 PITCH DEPTH   92,93
                (byte) (buf[adr + 0x3c] >> 4), (byte) (buf[adr + 0x3c] & 0xf), // MOD  LFO1 TVF DEPTH     94,95
                (byte) (buf[adr + 0x3d] >> 4), (byte) (buf[adr + 0x3d] & 0xf), // MOD  LFO2 TVA DEPTH     96,97
                (byte) (buf[adr + 0x3e] >> 4), (byte) (buf[adr + 0x3e] & 0xf), // MOD  LFO2 RATE CONTROL  98,99
                (byte) (buf[adr + 0x3f] >> 4), (byte) (buf[adr + 0x3f] & 0xf), // MOD  LFO2 PITCH DEPTH   100,101
                (byte) (buf[adr + 0x40] >> 4), (byte) (buf[adr + 0x40] & 0xf), // MOD  LFO2 TVF DEPTH     102,103
                (byte) (buf[adr + 0x41] >> 4), (byte) (buf[adr + 0x41] & 0xf), // MOD  LFO2 TVA DEPTH     104,105
                (byte) (buf[adr + 0x42] >> 4), (byte) (buf[adr + 0x42] & 0xf), // BEND PITCH CONTROL      106,107
                (byte) (buf[adr + 0x43] >> 4), (byte) (buf[adr + 0x43] & 0xf), // BEND TVF CUTOFF CONTROL 108,109
                (byte) (buf[adr + 0x44] >> 4), (byte) (buf[adr + 0x44] & 0xf), // BEND AMPLITUDE CONTROL  110,111
                (byte) 0, (byte) 0, // (The value of DATA 112,113 is 0)
                (byte) (buf[adr + 0x45] >> 4), (byte) (buf[adr + 0x45] & 0xf), // BEND LFO1 RATE CONTROL  114,115
                (byte) (buf[adr + 0x46] >> 4), (byte) (buf[adr + 0x46] & 0xf), // BEND LFO1 PITCH DEPTH   116,117
                (byte) (buf[adr + 0x47] >> 4), (byte) (buf[adr + 0x47] & 0xf), // BEND LFO1 TVF DEPTH     118,119
                (byte) (buf[adr + 0x48] >> 4), (byte) (buf[adr + 0x48] & 0xf), // BEND LFO1 TVA DEPTH     120,121
                (byte) (buf[adr + 0x49] >> 4), (byte) (buf[adr + 0x49] & 0xf), // BEND LFO2 RATE CONTROL  122,123
                (byte) (buf[adr + 0x4a] >> 4), (byte) (buf[adr + 0x4a] & 0xf), // BEND LFO2 PITCH DEPTH   124,125
                (byte) (buf[adr + 0x4b] >> 4), (byte) (buf[adr + 0x4b] & 0xf), // BEND LFO2 TVF DEPTH     126,127
                (byte) (buf[adr + 0x4c] >> 4), (byte) (buf[adr + 0x4c] & 0xf), // BEND LFO2 TVA DEPTH     126,127

                (byte) 0x84)));
    }

    private void makeGSDBufPtn_2(List<CtlSysex> DBuf, byte[] buf, int adr, byte iAdrMm, byte iAdrLl) {
        DBuf.add(new CtlSysex(4, getSysEx((byte) 0x41, (byte) 0x10, (byte) 0x42, (byte) 0x12, (byte) 0x83,
                (byte) 0x48, iAdrMm, iAdrLl,
                (byte) (buf[adr + 0x4d] >> 4), (byte) (buf[adr + 0x4d] & 0xf), // CAf  PITCH CONTROL      0,1
                (byte) (buf[adr + 0x4e] >> 4), (byte) (buf[adr + 0x4e] & 0xf), // CAf  TVF CUTOFF CONTROL 2,3
                (byte) (buf[adr + 0x4f] >> 4), (byte) (buf[adr + 0x4f] & 0xf), // CAf  AMPLITUDE CONTROL  4,5
                (byte) (buf[adr + 0x50] >> 4), (byte) (buf[adr + 0x50] & 0xf), // CAf  LFO1 RATE CONTROL  6,7
                (byte) 4, (byte) 0, // 8, 9
                (byte) (buf[adr + 0x51] >> 4), (byte) (buf[adr + 0x51] & 0xf), // CAf  LFO1 PITCH DEPTH   10,11
                (byte) (buf[adr + 0x52] >> 4), (byte) (buf[adr + 0x52] & 0xf), // CAf  LFO1 TVF DEPTH     12,13
                (byte) (buf[adr + 0x53] >> 4), (byte) (buf[adr + 0x53] & 0xf), // CAf  LFO1 TVA DEPTH     14,15
                (byte) (buf[adr + 0x54] >> 4), (byte) (buf[adr + 0x54] & 0xf), // CAf  LFO2 RATE CONTROL  16,17
                (byte) (buf[adr + 0x55] >> 4), (byte) (buf[adr + 0x55] & 0xf), // CAf  LFO2 PITCH DEPTH   18,19
                (byte) (buf[adr + 0x56] >> 4), (byte) (buf[adr + 0x56] & 0xf), // CAf  LFO2 TVF DEPTH     20,21
                (byte) (buf[adr + 0x57] >> 4), (byte) (buf[adr + 0x57] & 0xf), // CAf  LFO2 TVA DEPTH     22,23
                (byte) (buf[adr + 0x58] >> 4), (byte) (buf[adr + 0x58] & 0xf), // PAf  PITCH CONTROL      24,25
                (byte) (buf[adr + 0x59] >> 4), (byte) (buf[adr + 0x59] & 0xf), // PAf  TVF CUTOFF CONTROL 26,27
                (byte) (buf[adr + 0x5a] >> 4), (byte) (buf[adr + 0x5a] & 0xf), // PAf  AMPLITUDE CONTROL  28,29
                (byte) (buf[adr + 0x5b] >> 4), (byte) (buf[adr + 0x5b] & 0xf), // PAf  LFO1 RATE CONTROL  30,31
                (byte) 4, (byte) 0, // 32,33
                (byte) (buf[adr + 0x5c] >> 4), (byte) (buf[adr + 0x5c] & 0xf), // PAf  LFO1 PITCH DEPTH   34,35
                (byte) (buf[adr + 0x5d] >> 4), (byte) (buf[adr + 0x5d] & 0xf), // PAf  LFO1 TVF DEPTH     36,37
                (byte) (buf[adr + 0x5e] >> 4), (byte) (buf[adr + 0x5e] & 0xf), // PAf  LFO1 TVA DEPTH     38,39
                (byte) (buf[adr + 0x5f] >> 4), (byte) (buf[adr + 0x5f] & 0xf), // PAf  LFO2 RATE CONTROL  40,41
                (byte) (buf[adr + 0x60] >> 4), (byte) (buf[adr + 0x60] & 0xf), // PAf  LFO2 PITCH DEPTH   42,43
                (byte) (buf[adr + 0x61] >> 4), (byte) (buf[adr + 0x61] & 0xf), // PAf  LFO2 TVF DEPTH     44,45
                (byte) (buf[adr + 0x62] >> 4), (byte) (buf[adr + 0x62] & 0xf), // PAf  LFO2 TVA DEPTH     46,47
                (byte) (buf[adr + 0x63] >> 4), (byte) (buf[adr + 0x63] & 0xf), // CC1  PITCH CONTROL      48,49
                (byte) (buf[adr + 0x64] >> 4), (byte) (buf[adr + 0x64] & 0xf), // CC1  TVF CUTOFF CONTROL 50,51
                (byte) (buf[adr + 0x65] >> 4), (byte) (buf[adr + 0x65] & 0xf), // CC1  AMPLITUDE CONTROL  52,53
                (byte) 0, (byte) 0, // 54,55
                (byte) (buf[adr + 0x66] >> 4), (byte) (buf[adr + 0x66] & 0xf), // CC1  LFO1 RATE CONTROL  56,57
                (byte) (buf[adr + 0x67] >> 4), (byte) (buf[adr + 0x67] & 0xf), // CC1  LFO1 PITCH DEPTH   58,59
                (byte) (buf[adr + 0x68] >> 4), (byte) (buf[adr + 0x68] & 0xf), // CC1  LFO1 TVF DEPTH     60,61
                (byte) (buf[adr + 0x69] >> 4), (byte) (buf[adr + 0x69] & 0xf), // CC1  LFO1 TVA DEPTH     62,63
                (byte) (buf[adr + 0x6a] >> 4), (byte) (buf[adr + 0x6a] & 0xf), // CC1  LFO2 RATE CONTROL  64,65
                (byte) (buf[adr + 0x6b] >> 4), (byte) (buf[adr + 0x6b] & 0xf), // CC1  LFO2 PITCH DEPTH   66,67
                (byte) (buf[adr + 0x6c] >> 4), (byte) (buf[adr + 0x6c] & 0xf), // CC1  LFO2 TVF DEPTH     68,69
                (byte) (buf[adr + 0x6d] >> 4), (byte) (buf[adr + 0x6d] & 0xf), // CC1  LFO2 TVA DEPTH     70,71
                (byte) (buf[adr + 0x6e] >> 4), (byte) (buf[adr + 0x6e] & 0xf), // CC2  PITCH CONTROL      72,73
                (byte) (buf[adr + 0x6f] >> 4), (byte) (buf[adr + 0x6f] & 0xf), // CC2  TVF CUTOFF CONTROL 74,75
                (byte) (buf[adr + 0x70] >> 4), (byte) (buf[adr + 0x70] & 0xf), // CC2  AMPLITUDE CONTROL  76,77
                (byte) 0, (byte) 0, // 78,79
                (byte) (buf[adr + 0x71] >> 4), (byte) (buf[adr + 0x71] & 0xf), // CC2  LFO1 RATE CONTROL  80,81
                (byte) (buf[adr + 0x72] >> 4), (byte) (buf[adr + 0x72] & 0xf), // CC2  LFO1 PITCH DEPTH   82,83
                (byte) (buf[adr + 0x73] >> 4), (byte) (buf[adr + 0x73] & 0xf), // CC2  LFO1 TVF DEPTH     84,85
                (byte) (buf[adr + 0x74] >> 4), (byte) (buf[adr + 0x74] & 0xf), // CC2  LFO1 TVA DEPTH     86,87
                (byte) (buf[adr + 0x75] >> 4), (byte) (buf[adr + 0x75] & 0xf), // CC2  LFO2 RATE CONTROL  88,89
                (byte) (buf[adr + 0x76] >> 4), (byte) (buf[adr + 0x76] & 0xf), // CC2  LFO2 PITCH DEPTH   90,91
                (byte) (buf[adr + 0x77] >> 4), (byte) (buf[adr + 0x77] & 0xf), // CC2  LFO2 TVF DEPTH     92,93
                (byte) (buf[adr + 0x78] >> 4), (byte) (buf[adr + 0x78] & 0xf), // CC2  LFO2 TVA DEPTH     94,95

                (byte) 0x84)));
    }

    private void makeGSDBufPtn_3(List<CtlSysex> dBuf, byte[] buf, int adr, int adr2) {
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

        byte[][] pac0 = new byte[1][], pac1 = new byte[1][];

        makeGSDBufPtn_4(level, (byte) (0x02 + adr2), /* out */ pac0, /* out */ pac1);
        dBuf.add(new CtlSysex(5, getSysEx(pac0[0])));
        dBuf.add(new CtlSysex(5, getSysEx(pac1[0])));

        makeGSDBufPtn_4(panpot, (byte) (0x06 + adr2), /* out */ pac0, /* out */ pac1);
        dBuf.add(new CtlSysex(5, getSysEx(pac0[0])));
        dBuf.add(new CtlSysex(5, getSysEx(pac1[0])));

        makeGSDBufPtn_4(reverb, (byte) (0x08 + adr2), /* out */ pac0, /* out */ pac1);
        dBuf.add(new CtlSysex(5, getSysEx(pac0[0])));
        dBuf.add(new CtlSysex(5, getSysEx(pac1[0])));

        makeGSDBufPtn_4(chorus, (byte) (0x0a + adr2), /* out */ pac0, /* out */ pac1);
        dBuf.add(new CtlSysex(5, getSysEx(pac0[0])));
        dBuf.add(new CtlSysex(5, getSysEx(pac1[0])));
    }

    private void makeGSDBufPtn_4(List<Byte> s, byte iAdr_mm, /* out */ byte[][] pac0, /* out */ byte[][] pac1) {
        pac0[0] = new byte[128 + 9];
        pac0[0][0] = 0x41;
        pac0[0][1] = 0x10;
        pac0[0][2] = 0x42;
        pac0[0][3] = 0x12;
        pac0[0][4] = (byte) 0x83;
        pac0[0][5] = 0x49;
        pac0[0][6] = iAdr_mm;
        pac0[0][7] = 0x00;
        for (int i = 0; i < 128; i++) {
            pac0[0][i + 8] = s.get(i);
        }
        pac0[0][128 + 9 - 1] = (byte) 0x84;
        pac1[0] = new byte[128 + 9];
        pac1[0][0] = 0x41;
        pac1[0][1] = 0x10;
        pac1[0][2] = 0x42;
        pac1[0][3] = 0x12;
        pac1[0][4] = (byte) 0x83;
        pac1[0][5] = 0x49;
        pac1[0][6] = (byte) (iAdr_mm + 1);
        pac1[0][7] = 0x00;
        for (int i = 0; i < 128; i++) {
            pac1[0][i + 8] = s.get(i + 128);
        }
        pac1[0][128 + 9 - 1] = (byte) 0x84;
    }

    private void getCM6Buf(/* ref */ List<CtlSysex> DBuf) {

        byte[] buf = null;
        for (Tuple<String, byte[]> trg : extendFiles) {
            if (Path.getExtension(trg.getItem1()).equalsIgnoreCase(".CM6")) {
                buf = trg.getItem2();
            }
        }

        if (buf == null || buf.length < 1 || buf.length != 0x5849) return;

        // System Area
        DBuf.add(new CtlSysex(2, getSysEx(makeCM6Ptn_0(buf, 0x0080, 0x017, (byte) 0x10, (byte) 0x00, (byte) 0x00))));
        // Timbre Memory #1~ (User 128)
        for (int adr = 0x0e34, i = 0; adr <= 0x4d34; adr += 0x100, i += 2) {
            DBuf.add(new CtlSysex(9, getSysEx(makeCM6Ptn_0(buf, adr, 0x100, (byte) 0x08, (byte) (0x00 + i), (byte) 0x00))));
        }

        DBuf.add(new CtlSysex(9, getSysEx(makeCM6Ptn_0(buf, 0x0130, 0x100, (byte) 0x03, (byte) 0x01, (byte) 0x10))));
        DBuf.add(new CtlSysex(3, getSysEx(makeCM6Ptn_0(buf, 0x0230, 0x054, (byte) 0x03, (byte) 0x03, (byte) 0x10))));
        DBuf.add(new CtlSysex(5, getSysEx(makeCM6Ptn_0(buf, 0x00a0, 0x090, (byte) 0x03, (byte) 0x00, (byte) 0x00))));

        for (int adr = 0x0284, i = 0; adr <= 0x093e; adr += 0xf6, i += 0xf6) {
            DBuf.add(new CtlSysex(9, getSysEx(makeCM6Ptn_0(buf, adr, 0xf6, (byte) 0x04, (byte) (i >> 7), (byte) (i & 0x7f)))));
        }

        for (int adr = 0x0a34, i = 0; adr <= 0x0db4; adr += 0x80, i += 0x80) {
            DBuf.add(new CtlSysex(5, getSysEx(makeCM6Ptn_0(buf, adr, 0x80, (byte) 0x05, (byte) (i >> 7), (byte) (i & 0x7f)))));
        }

        DBuf.add(new CtlSysex(7, getSysEx(makeCM6Ptn_0(buf, 0x4e34, 0xbd, (byte) 0x50, (byte) 0x00, (byte) 0x00))));

        for (int adr = 0x4eb2, i = 0; adr <= 0x579a; adr += 0x98, i += 0x98) {
            DBuf.add(new CtlSysex(6, getSysEx(makeCM6Ptn_0(buf, adr, 0x98, (byte) 0x51, (byte) (i >> 7), (byte) (i & 0x7f)))));
        }

        DBuf.add(new CtlSysex(6, getSysEx(makeCM6Ptn_0(buf, 0x5832, 0x11, (byte) 0x52, (byte) 0x00, (byte) 0x00))));
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

        return ByteUtil.toByteArray(lst);
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
                midiSend.accept(0, csx.data);

                sendControlIndex[i]++;
            } else {
                endFlg++;
            }
        }

        if (endFlg == beforeSend.length) {
            beforeSend = null;
            oneSyncTime = 60.0 / nowTempo / timeBase;
            counter.run();
            return;
        }
    }

    void getCtlSysexFromText(List<CtlSysex> buf, String text) {
        if (text == null || text.isEmpty()) return;

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

    void getControlFile(List<CtlSysex> buf, int instType) {

        // GM / XG / GS / LA / GS(SC - 55_1) / GS(SC - 55_2)
        switch (instType) {
            case 0: // GM
            case 1: // XG
            case 2: // GS
                // No Control
                break;
            case 3: // LA
                if (!controlFileCM6.isEmpty()) {
                    getCM6Buf(/* ref */ buf);
                }
                break;
            case 4: // GS(SC - 55_1)
                if (!controlFileGSD.isEmpty()) {
                    getGSD1Buf(/* ref */ buf);
                }
                break;
            case 5: // GS(SC - 55_2)
                if (!controlFileGSD2.isEmpty()) {
                    getGSD2Buf(/* ref */ buf);
                }
                break;
        }

//#if DEBUG
        for (CtlSysex ex : buf) {
            logger.log(Level.DEBUG, "delta:%10d".formatted(ex.delta));
            for (byte b : ex.data) {
                logger.log(Level.DEBUG, "%02x ".formatted(b));
            }
            logger.log(Level.DEBUG, "");
        }
//#endif
    }
}
