package mdplayer.driver;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple3;
import mdplayer.ChipRegister;
import mdplayer.Common;
import mdplayer.DacControl;
import mdplayer.Setting;
import mdsound.chips.C140;
import vavi.util.ByteUtil;
import vavi.util.Debug;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;
import static java.lang.System.getLogger;


public class Vgm extends BaseDriver {

    private static final Logger logger = getLogger(Vgm.class.getName());

    public Vgm() {
        this.setting = Setting.getInstance();
        dacControl = new DacControl(setting);
    }

    public static final int FCC_VGM = 0x206D6756; // "Vgm "
    public static final int FCC_GD3 = 0x20336447; // "Gd3 "

    public static final int DefaultSN76489ClockValue = 3579545;
    public static final int DefaultYM2612ClockValue = 7670454;
    public static final int DefaultRF5C164ClockValue = 12500000;
    public static final int DefaultPWMClockValue = 23011361;
    public static final int DefaultC140ClockValue = 21390;
    public final C140.Type DefaultC140Type = C140.Type.ASIC219;
    public static final int DefaultOKIM6258ClockValue = 4000000;
    public static final int DefaultOKIM6295ClockValue = 4000000;
    public static final int DefaultSEGAPCMClockValue = 4000000;
    public static final int DefaultAY8910ClockValue = 1789750;

    public int sn76489ClockValue = DefaultSN76489ClockValue;
    public int ym2612ClockValue = DefaultYM2612ClockValue;
    public int rf5C68ClockValue = 12500000;
    public int rf5C164ClockValue = DefaultRF5C164ClockValue;
    public int pwmClockValue = DefaultPWMClockValue;
    public int c140ClockValue = DefaultC140ClockValue;
    public C140.Type C140Type = DefaultC140Type;
    public int okiM6258ClockValue = DefaultOKIM6258ClockValue;
    public int okiM6258Type = 0;
    public int okiM6295ClockValue = DefaultOKIM6295ClockValue;
    public int segaPCMClockValue = DefaultSEGAPCMClockValue;
    public int segaPCMInterface = 0;
    public int yn2151ClockValue;
    public int yn2608ClockValue;
    public int ym2203ClockValue;
    public int ym2610ClockValue;
    public int ym3812ClockValue;
    public int ym3526ClockValue;
    public int y8950ClockValue;
    public int ymF262ClockValue;
    public int ymF271ClockValue;
    public int ymF278BClockValue;
    public int ymZ280BClockValue;
    public int ay8910ClockValue;
    public int ym2413ClockValue;
    public int huC6280ClockValue;
    public int qSoundClockValue;
    public int saa1099ClockValue;
    public int wSwanClockValue;
    public int x1_010ClockValue;
    public int c352ClockValue;
    public int c352ClockDivider;
    public int ga20ClockValue;
    public int k053260ClockValue;
    public int k054539ClockValue;
    public int k054539Flags;
    public int k051649ClockValue;
    public int dmgClockValue;
    public int nesClockValue;
    public int multiPCMClockValue;
    public int pokeyClockValue;

    public boolean ym2612DualChipFlag;
    public boolean ym2151DualChipFlag;
    public boolean ym2203DualChipFlag;
    public boolean ym2608DualChipFlag;
    public boolean ym2610DualChipFlag;
    public boolean ym3812DualChipFlag;
    public boolean ym3526DualChipFlag;
    public boolean y8950DualChipFlag;
    public boolean ymF262DualChipFlag;
    public boolean ymF271DualChipFlag;
    public boolean ymF278BDualChipFlag;
    public boolean ymZ280BDualChipFlag;
    public boolean okiM6295DualChipFlag;
    public boolean sn76489DualChipFlag;
    public boolean sn76489NGPFlag;
    public Object[] sn76489Option;
    public boolean rf5C68DualChipFlag;
    public boolean rf5C164DualChipFlag;
    public boolean ay8910DualChipFlag;
    public boolean ym2413DualChipFlag;
    public boolean ym2413VRC7Flag;
    public boolean huC6280DualChipFlag;
    public boolean c140DualChipFlag;
    public boolean saA1099DualChipFlag;
    public boolean wSwanDualChipFlag;
    public boolean x1_010DualChipFlag;
    public boolean c352DualChipFlag;
    public boolean ga20DualChipFlag;
    public boolean k053260DualChipFlag;
    public boolean k054539DualChipFlag;
    public boolean k051649DualChipFlag;
    public boolean dmgDualChipFlag;
    public boolean nesDualChipFlag;
    public boolean multiPCMDualChipFlag;
    public boolean pokeyDualChipFlag;

    public DacControl dacControl;
    public boolean isPcmRAMWrite = false;
    public boolean useChipYM2612Ch6 = false;
//    public Setting setting = null;

    private Runnable[] vgmCmdTbl = new Runnable[0x100];

    private List<String> chips = null;

    private int vgmAdr;
    private int vgmWait;
    private int vgmLoopOffset = 0;
    private int vgmEof;
    private boolean vgmAnalyze;

    private int vgmDataOffset = 0;

    private static final int PCM_BANK_COUNT = 0x40;
    private VgmPcmBank[] pcmBank = new VgmPcmBank[PCM_BANK_COUNT];
    private PcmBankTbl pcmTbl = new PcmBankTbl();
    private int dacCtrlUsed;
    private byte[] dacCtrlUsg = new byte[0xff];
    private DacCtrlData[] dacCtrl = new DacCtrlData[0xff];

    private byte[][] ym2610AdpcmA = new byte[][] {null, null};
    private byte[][] ym2610AdpcmB = new byte[][] {null, null};

    @Override
    public boolean init(byte[] vgmBuf, ChipRegister chipRegister, Common.EnmModel model, Common.EnmChip[] useChip, int latency, int waitTime) {
        this.vgmBuf = vgmBuf;
        this.chipRegister = chipRegister;
        this.model = model;
        this.useChip = useChip;
        this.latency = latency;
        this.waitTime = waitTime;
        this.opnaRamType = 0;

        dumpCounter = 0;

        ym2610AdpcmA = new byte[][] {null, null};
        ym2610AdpcmB = new byte[][] {null, null};

        if (!getInformationHeader()) return false;

        vgmAdr = vgmDataOffset;
        vgmWait = 0;
        vgmAnalyze = true;
        counter = 0;
        vgmFrameCounter = -latency - waitTime;
        vgmCurLoop = 0;
        vgmSpeed = 1;
        vgmSpeedCounter = 0;

        for (int i = 0; i < PCM_BANK_COUNT; i++) pcmBank[i] = new VgmPcmBank();
        dacControl.refresh();
        dacCtrlUsed = 0x00;
        for (int curChip = 0x00; curChip < 0xff; curChip++) {
            dacCtrl[curChip] = new DacCtrlData();
            dacCtrl[curChip].enable = false;
        }

        setCommands();

        stopped = false;
        isDataBlock = false;
        isPcmRAMWrite = false;
        useChipYM2612Ch6 = false;
        for (mdplayer.Common.EnmChip uc : useChip) {
            if (uc == mdplayer.Common.EnmChip.YM2612Ch6) {
                useChipYM2612Ch6 = true;
                break;
            }
        }
        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, ChipRegister chipRegister, Common.EnmModel model, Common.EnmChip[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException("このdriverはこのメソッドを必要としない");
    }

    @Override
    public void processOneFrame() {
        try {
            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;
                if (vgmFrameCounter > -1) {
                    oneFrameVGMMain();
                } else {
                    vgmFrameCounter++;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void oneFrameVGMMain() {
        if (vgmWait > 0) {
            //if (model == enmModel.VirtualModel)
            oneFrameVGMStream();
            vgmWait--;
            counter++;
            vgmFrameCounter++;
//Debug.println("ret: wait: " + vgmWait + ", fc: " + vgmFrameCounter);
            return;
        }

        oneFrameVGMStream();

        if (!vgmAnalyze) {
            //if (model == enmModel.VirtualModel)
            //    oneFrameVGMStream();
            stopped = true;
Debug.println("ret: not analyze");
            return;
        }

        int countNum = 0;
        while (vgmWait <= 0) {
            if (vgmAdr >= vgmBuf.length || (vgmEof != 0 && vgmAdr >= vgmEof)) {
                if (loopCounter != 0) {
                    vgmAdr = vgmLoopOffset + 0x1c;
                    vgmCurLoop++;
                    counter = 0;
                } else {
                    vgmAnalyze = false;
Debug.println("ret: not analyze 2");
                    return;
                }
            }

            int cmd = vgmBuf[vgmAdr] & 0xff;
logger.log(Level.DEBUG, String.format("[%s]: adr: %x Dat: %x", model, vgmAdr, vgmBuf[vgmAdr])); // ok
            if (vgmCmdTbl[cmd] != null) {
                //if (model == EnmModel.VirtualModel) System.err.println("%05x : %02x ", vgmAdr, vgmBuf[vgmAdr]);
                vgmCmdTbl[cmd].run();
            } else {
                 // わからんコマンド
logger.log(Level.WARNING, String.format("[%s]:unknown command: adr: %x Dat: %x", model, vgmAdr, vgmBuf[vgmAdr]));
                vgmAdr++;
            }
            countNum++;
            if (countNum > 100) {
                if (model == mdplayer.Common.EnmModel.RealModel && countNum % 100 == 0) {
                    isDataBlock = true;
                    chipRegister.sendDataYM2608(0, model);
                    chipRegister.setYM2608SyncWait(0, 1);
                    chipRegister.sendDataYM2151(0, model);
                    chipRegister.setYM2151SyncWait(0, 1);

                    chipRegister.sendDataYM2608(1, model);
                    chipRegister.setYM2608SyncWait(1, 1);
                    chipRegister.sendDataYM2151(1, model);
                    chipRegister.setYM2151SyncWait(1, 1);
                }
            }
        }

        if (model == mdplayer.Common.EnmModel.RealModel && isDataBlock) {
            isDataBlock = false;
            //System.err.println("%s countnum:%d", model, countNum);
            countNum = 0;
        }

        // Send wait
        if (model == mdplayer.Common.EnmModel.RealModel) {
            if (vgmSpeed == 1) { // 等速の場合のみウェイトをかける
                if (useChipYM2612Ch6)
                    chipRegister.setYM2612SyncWait(0, vgmWait);
//                if ((useChip & enmUseChip.SN76489) == enmUseChip.SN76489)
//                    chipRegister.setSN76489SyncWait(vgmWait);
//                chipRegister.setYM2608SyncWait(vgmWait);
//                chipRegister.setYM2151SyncWait(vgmWait);
            }
        }

//        if (model == enmModel.VirtualModel)
//           oneFrameVGMStream();

        vgmWait--;
        counter++;
        vgmFrameCounter++;
    }

    private void oneFrameVGMStream() {
        for (int curChip = 0x00; curChip < dacCtrlUsed; curChip++) {
            dacControl.update(dacCtrlUsg[curChip] & 0xff, 1);
        }
    }

    private void setCommands() {

        Arrays.fill(vgmCmdTbl, null);

        vgmCmdTbl[0x30] = this::vcPSG;
        vgmCmdTbl[0x31] = this::vcDummy1Ope;
        vgmCmdTbl[0x32] = this::vcDummy1Ope;
        vgmCmdTbl[0x33] = this::vcDummy1Ope;
        vgmCmdTbl[0x34] = this::vcDummy1Ope;
        vgmCmdTbl[0x35] = this::vcDummy1Ope;
        vgmCmdTbl[0x36] = this::vcDummy1Ope;
        vgmCmdTbl[0x37] = this::vcDummy1Ope;

        vgmCmdTbl[0x38] = this::vcDummy1Ope;
        vgmCmdTbl[0x39] = this::vcDummy1Ope;
        vgmCmdTbl[0x3a] = this::vcDummy1Ope;
        vgmCmdTbl[0x3b] = this::vcDummy1Ope;
        vgmCmdTbl[0x3c] = this::vcDummy1Ope;
        vgmCmdTbl[0x3d] = this::vcDummy1Ope;
        vgmCmdTbl[0x3e] = this::vcDummy1Ope;
        vgmCmdTbl[0x3f] = this::vcGGPSGPort06;

        vgmCmdTbl[0x40] = this::vcDummy2Ope;
        vgmCmdTbl[0x41] = this::vcDummy2Ope;
        vgmCmdTbl[0x42] = this::vcDummy2Ope;
        vgmCmdTbl[0x43] = this::vcDummy2Ope;
        vgmCmdTbl[0x44] = this::vcDummy2Ope;
        vgmCmdTbl[0x45] = this::vcDummy2Ope;
        vgmCmdTbl[0x46] = this::vcDummy2Ope;
        vgmCmdTbl[0x47] = this::vcDummy2Ope;

        vgmCmdTbl[0x48] = this::vcDummy2Ope;
        vgmCmdTbl[0x49] = this::vcDummy2Ope;
        vgmCmdTbl[0x4a] = this::vcDummy2Ope;
        vgmCmdTbl[0x4b] = this::vcDummy2Ope;
        vgmCmdTbl[0x4c] = this::vcDummy2Ope;
        vgmCmdTbl[0x4d] = this::vcDummy2Ope;
        vgmCmdTbl[0x4e] = this::vcDummy2Ope;

        vgmCmdTbl[0x4f] = this::vcGGPSGPort06;
        vgmCmdTbl[0x50] = this::vcPSG;

        vgmCmdTbl[0x51] = this::vcYM2413;
        vgmCmdTbl[0x52] = this::vcYM2612Port0;
        vgmCmdTbl[0x53] = this::vcYM2612Port1;

        vgmCmdTbl[0x54] = this::vcYM2151;
        vgmCmdTbl[0x55] = this::vcYM2203;
        vgmCmdTbl[0x56] = this::vcYM2608Port0;
        vgmCmdTbl[0x57] = this::vcYM2608Port1;

        vgmCmdTbl[0x58] = this::vcYM2610Port0;
        vgmCmdTbl[0x59] = this::vcYM2610Port1;
        vgmCmdTbl[0x5a] = this::vcYM3812;
        vgmCmdTbl[0x5b] = this::vcYM3526;
        vgmCmdTbl[0x5c] = this::vcY8950;
        vgmCmdTbl[0x5d] = this::vcYMZ280B;
        vgmCmdTbl[0x5e] = this::vcYMF262Port0;
        vgmCmdTbl[0x5f] = this::vcYMF262Port1;

        vgmCmdTbl[0x61] = this::vcWaitNSamples;
        vgmCmdTbl[0x62] = this::vcWait735Samples;
        vgmCmdTbl[0x63] = this::vcWait882Samples;
        vgmCmdTbl[0x64] = this::vcOverrideLength;

        vgmCmdTbl[0x66] = this::vcEndOfSoundData;
        vgmCmdTbl[0x67] = this::vcDataBlock;
        vgmCmdTbl[0x68] = this::vcPCMRamWrite;

        vgmCmdTbl[0x70] = this::vcWaitN1Samples;
        vgmCmdTbl[0x71] = this::vcWaitN1Samples;
        vgmCmdTbl[0x72] = this::vcWaitN1Samples;
        vgmCmdTbl[0x73] = this::vcWaitN1Samples;
        vgmCmdTbl[0x74] = this::vcWaitN1Samples;
        vgmCmdTbl[0x75] = this::vcWaitN1Samples;
        vgmCmdTbl[0x76] = this::vcWaitN1Samples;
        vgmCmdTbl[0x77] = this::vcWaitN1Samples;

        vgmCmdTbl[0x78] = this::vcWaitN1Samples;
        vgmCmdTbl[0x79] = this::vcWaitN1Samples;
        vgmCmdTbl[0x7a] = this::vcWaitN1Samples;
        vgmCmdTbl[0x7b] = this::vcWaitN1Samples;
        vgmCmdTbl[0x7c] = this::vcWaitN1Samples;
        vgmCmdTbl[0x7d] = this::vcWaitN1Samples;
        vgmCmdTbl[0x7e] = this::vcWaitN1Samples;
        vgmCmdTbl[0x7f] = this::vcWaitN1Samples;

        vgmCmdTbl[0x80] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x81] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x82] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x83] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x84] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x85] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x86] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x87] = this::vcWaitNSamplesAndSendYM26120x2a;

        vgmCmdTbl[0x88] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x89] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x8a] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x8b] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x8c] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x8d] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x8e] = this::vcWaitNSamplesAndSendYM26120x2a;
        vgmCmdTbl[0x8f] = this::vcWaitNSamplesAndSendYM26120x2a;

        vgmCmdTbl[0x90] = this::vcSetupStreamControl;
        vgmCmdTbl[0x91] = this::vcSetStreamData;
        vgmCmdTbl[0x92] = this::vcSetStreamFrequency;
        vgmCmdTbl[0x93] = this::vcStartStream;
        vgmCmdTbl[0x94] = this::vcStopStream;
        vgmCmdTbl[0x95] = this::vcStartStreamFastCall;

        vgmCmdTbl[0xa0] = this::vcAY8910;
        vgmCmdTbl[0xa1] = this::vcYM2413;
        vgmCmdTbl[0xa2] = this::vcYM2612Port0;
        vgmCmdTbl[0xa3] = this::vcYM2612Port1;
        vgmCmdTbl[0xa4] = this::vcYM2151;
        vgmCmdTbl[0xa5] = this::vcYM2203;
        vgmCmdTbl[0xa6] = this::vcYM2608Port0;
        vgmCmdTbl[0xa7] = this::vcYM2608Port1;

        vgmCmdTbl[0xa8] = this::vcYM2610Port0;
        vgmCmdTbl[0xa9] = this::vcYM2610Port1;
        vgmCmdTbl[0xaa] = this::vcYM3812;
        vgmCmdTbl[0xab] = this::vcDummy2Ope;
        vgmCmdTbl[0xac] = this::vcY8950;
        vgmCmdTbl[0xad] = this::vcYMZ280B;
        vgmCmdTbl[0xae] = this::vcYMF262Port0;
        vgmCmdTbl[0xaf] = this::vcYMF262Port1;

//        if ((useChip & enmUseChip.RF5C164) == enmUseChip.RF5C164) {
        vgmCmdTbl[0xb0] = this::vcRf5c68;
        vgmCmdTbl[0xc1] = this::vcRf5c68MemoryWrite;
        vgmCmdTbl[0xb1] = this::vcRf5c164;
        vgmCmdTbl[0xc2] = this::vcRf5c164MemoryWrite;
//        } else {
//            vgmCmdTbl[0xb1] = this::vcDummy2Ope;
//            vgmCmdTbl[0xc2] = this::vcDummy3Ope;
//        }

//        if ((useChip & enmUseChip.PWM) == enmUseChip.PWM) {
        vgmCmdTbl[0xb2] = this::vcPWM;
//        } else {
//        vgmCmdTbl[0xb2] = this::vcDummy2Ope;
//        }
        vgmCmdTbl[0xb3] = this::vcDMG;
        vgmCmdTbl[0xb4] = this::vcNES;
        vgmCmdTbl[0xb5] = this::vcMultiPCM;
        vgmCmdTbl[0xb6] = this::vcDummy2Ope;
        vgmCmdTbl[0xb7] = this::vcOKIM6258;

        vgmCmdTbl[0xb8] = this::vcOKIM6295;
        vgmCmdTbl[0xb9] = this::vcHuC6280;
        vgmCmdTbl[0xba] = this::vcK053260;
        vgmCmdTbl[0xbb] = this::vcPOKEY;
        vgmCmdTbl[0xbc] = this::vcWSwan;
        vgmCmdTbl[0xbd] = this::vcSAA1099;
        vgmCmdTbl[0xbe] = this::vcDummy2Ope;
        vgmCmdTbl[0xbf] = this::vcGA20;

        vgmCmdTbl[0xc0] = this::vcSEGAPCM;
        vgmCmdTbl[0xc3] = this::vcMultiPCMSetBank;
        vgmCmdTbl[0xc4] = this::vcQSound;
        vgmCmdTbl[0xc5] = this::vcDummy3Ope;
        vgmCmdTbl[0xc6] = this::vcWSwanMem;
        vgmCmdTbl[0xc7] = this::vcDummy3Ope;

        vgmCmdTbl[0xc8] = this::vcX1_010;
        vgmCmdTbl[0xc9] = this::vcDummy3Ope;
        vgmCmdTbl[0xca] = this::vcDummy3Ope;
        vgmCmdTbl[0xcb] = this::vcDummy3Ope;
        vgmCmdTbl[0xcc] = this::vcDummy3Ope;
        vgmCmdTbl[0xcd] = this::vcDummy3Ope;
        vgmCmdTbl[0xce] = this::vcDummy3Ope;
        vgmCmdTbl[0xcf] = this::vcDummy3Ope;

        vgmCmdTbl[0xd0] = this::vcYMF278B;
        vgmCmdTbl[0xd1] = this::vcYMF271;
        vgmCmdTbl[0xd2] = this::vcK051649;
        vgmCmdTbl[0xd3] = this::vcK054539;
        vgmCmdTbl[0xd4] = this::vcC140;
        vgmCmdTbl[0xd5] = this::vcDummy3Ope;
        vgmCmdTbl[0xd6] = this::vcDummy3Ope;
        vgmCmdTbl[0xd7] = this::vcDummy3Ope;

        vgmCmdTbl[0xd8] = this::vcDummy3Ope;
        vgmCmdTbl[0xd9] = this::vcDummy3Ope;
        vgmCmdTbl[0xda] = this::vcDummy3Ope;
        vgmCmdTbl[0xdb] = this::vcDummy3Ope;
        vgmCmdTbl[0xdc] = this::vcDummy3Ope;
        vgmCmdTbl[0xdd] = this::vcDummy3Ope;
        vgmCmdTbl[0xde] = this::vcDummy3Ope;
        vgmCmdTbl[0xdf] = this::vcDummy3Ope;

        vgmCmdTbl[0xe0] = this::vcSeekToOffsetInPCMDataBank;
        vgmCmdTbl[0xe1] = this::vcC352;
        vgmCmdTbl[0xe2] = this::vcDummy4Ope;
        vgmCmdTbl[0xe3] = this::vcDummy4Ope;
        vgmCmdTbl[0xe4] = this::vcDummy4Ope;
        vgmCmdTbl[0xe5] = this::vcDummy4Ope;
        vgmCmdTbl[0xe6] = this::vcDummy4Ope;
        vgmCmdTbl[0xe7] = this::vcDummy4Ope;

        vgmCmdTbl[0xe8] = this::vcDummy4Ope;
        vgmCmdTbl[0xe9] = this::vcDummy4Ope;
        vgmCmdTbl[0xea] = this::vcDummy4Ope;
        vgmCmdTbl[0xeb] = this::vcDummy4Ope;
        vgmCmdTbl[0xec] = this::vcDummy4Ope;
        vgmCmdTbl[0xed] = this::vcDummy4Ope;
        vgmCmdTbl[0xee] = this::vcDummy4Ope;
        vgmCmdTbl[0xef] = this::vcDummy4Ope;

        vgmCmdTbl[0xf0] = this::vcDummy4Ope;
        vgmCmdTbl[0xf1] = this::vcDummy4Ope;
        vgmCmdTbl[0xf2] = this::vcDummy4Ope;
        vgmCmdTbl[0xf3] = this::vcDummy4Ope;
        vgmCmdTbl[0xf4] = this::vcDummy4Ope;
        vgmCmdTbl[0xf5] = this::vcDummy4Ope;
        vgmCmdTbl[0xf6] = this::vcDummy4Ope;
        vgmCmdTbl[0xf7] = this::vcDummy4Ope;

        vgmCmdTbl[0xf8] = this::vcDummy4Ope;
        vgmCmdTbl[0xf9] = this::vcDummy4Ope;
        vgmCmdTbl[0xfa] = this::vcDummy4Ope;
        vgmCmdTbl[0xfb] = this::vcDummy4Ope;
        vgmCmdTbl[0xfc] = this::vcDummy4Ope;
        vgmCmdTbl[0xfd] = this::vcDummy4Ope;
        vgmCmdTbl[0xfe] = this::vcDummy4Ope;
        vgmCmdTbl[0xff] = this::vcDummy4Ope;
    }

    private void vcDummy1Ope() {
        //System.err.printf("(%02X:%02X)", vgmBuf[vgmAdr], vgmBuf[vgmAdr + 1]);
        vgmAdr += 2;
    }

    private void vcDummy2Ope() {
        //System.err.printf("(%02X:%02X:%02X)", vgmBuf[vgmAdr], vgmBuf[vgmAdr + 1], vgmBuf[vgmAdr + 2]);
        vgmAdr += 3;
    }

    private void vcDummy3Ope() {
        //System.err.printf("(%02X:%02X:%02X:%02X)", vgmBuf[vgmAdr], vgmBuf[vgmAdr + 1], vgmBuf[vgmAdr + 2], vgmBuf[vgmAdr + 3]);
        vgmAdr += 4;
    }

    private void vcDummy4Ope() {
        //System.err.println("unknown command:Adr:%x(%02X:%02X:%02X:%02X:%02X)",vgmAdr, vgmBuf[vgmAdr], vgmBuf[vgmAdr + 1], vgmBuf[vgmAdr + 2], vgmBuf[vgmAdr + 3], vgmBuf[vgmAdr + 4]);
        vgmAdr += 5;
    }

    private void vcGGPSGPort06() {
        chipRegister.setSN76489RegisterGGpanning(vgmBuf[vgmAdr] == 0x4f ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, model);
        vgmAdr += 2;
    }

    private void vcPSG() {
        chipRegister.setSN76489Register(vgmBuf[vgmAdr] == 0x50 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, model);
        vgmAdr += 2;
    }

    private void vcAY8910() {
        chipRegister.setAY8910Register((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2], model);
        //chipRegister.setAY8910Register(0, vgmBuf[vgmAdr + 1], vgmBuf[vgmAdr + 2], model);
        vgmAdr += 3;
    }

    private void vcDMG() {
        chipRegister.setDMGRegister((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2], model);
        //chipRegister.setAY8910Register(0, vgmBuf[vgmAdr + 1], vgmBuf[vgmAdr + 2], model);
        vgmAdr += 3;
    }

    private void vcNES() {
        chipRegister.setNESRegister((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2], model);
        vgmAdr += 3;
    }

    private void vcMultiPCM() {
        chipRegister.setMultiPCMRegister((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2], model);
        vgmAdr += 3;
    }

    private void vcMultiPCMSetBank() {
        chipRegister.setMultiPCMSetBank((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] + vgmBuf[vgmAdr + 3] * 0x100, model);
        vgmAdr += 4;
    }

    private void vcQSound() {
        chipRegister.setQSoundRegister(0, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, vgmBuf[vgmAdr + 3] & 0xff, model);
        vgmAdr += 4;
    }

    private void vcX1_010() {
        chipRegister.setX1_010Register((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] & 0xff, vgmBuf[vgmAdr + 3] & 0xff, model);
        vgmAdr += 4;
    }

    private void vcYM2413() {
        chipRegister.setYM2413Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYM3812() {
        chipRegister.setYM3812Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcHuC6280() {
        chipRegister.setHuC6280Register((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcGA20() {
        chipRegister.setGA20Register((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYM2612Port0() {
        chipRegister.setYM2612Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 0, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model, vgmFrameCounter);
        vgmAdr += 3;
    }

    private void vcYM2612Port1() {
        chipRegister.setYM2612Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model, vgmFrameCounter);
        vgmAdr += 3;
    }

    private void vcYM2203() {
        chipRegister.setYM2203Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYM2608Port0() {
        chipRegister.setYM2608Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 0, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYM2608Port1() {
        int adr = vgmBuf[vgmAdr + 1] & 0xff;
        int dat = vgmBuf[vgmAdr + 2] & 0xff;
//        if (adr >= 0x00 && adr <= 0x10 && model == enmModel.RealModel) {
//            System.err.println("%2X:%2X", adr, dat);
//        }
//        if (adr == 0x01) {
//            //dat &= 0xfd;
//            //dat |= 1;
//        }
//        if (adr == 0x00 && (dat & 0x20) != 0) {
//            //dat &= 0xdf;
//        }
        chipRegister.setYM2608Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 1, adr, dat, model);
        vgmAdr += 3;
    }

    private void vcYM2610Port0() {
        chipRegister.setYM2610Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 0, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYM2610Port1() {
        int adr = vgmBuf[vgmAdr + 1] & 0xff;
        int dat = vgmBuf[vgmAdr + 2] & 0xff;
        chipRegister.setYM2610Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 1, adr, dat, model);
        vgmAdr += 3;
    }

    private void vcYMF262Port0() {
        chipRegister.setYMF262Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 0, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYMF262Port1() {
        int adr = vgmBuf[vgmAdr + 1] & 0xff;
        int dat = vgmBuf[vgmAdr + 2] & 0xff;
        chipRegister.setYMF262Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 1, adr, dat, model);
        vgmAdr += 3;
    }

    private void vcYM3526() {
        chipRegister.setYM3526Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcY8950() {
        chipRegister.setY8950Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYMZ280B() {
        chipRegister.setYMZ280BRegister((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcYMF271() {
        chipRegister.setYMF271Register(
                (vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1
                , vgmBuf[vgmAdr + 1] & 0x7f
                , vgmBuf[vgmAdr + 2] & 0xff
                , vgmBuf[vgmAdr + 3] & 0xff
                , model);
        vgmAdr += 4;
    }

    private void vcYMF278B() {
        chipRegister.setYMF278BRegister(
                (vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1
                , vgmBuf[vgmAdr + 1] & 0x7f
                , vgmBuf[vgmAdr + 2] & 0xff
                , vgmBuf[vgmAdr + 3] & 0xff
                , model);
//System.err.println("fm:%02x:%02x:%02x:", vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2], vgmBuf[vgmAdr + 3]);
        vgmAdr += 4;
    }

    private void vcYM2151() {
        chipRegister.setYM2151Register((vgmBuf[vgmAdr] & 0x80) == 0 ? 0 : 1, 0, vgmBuf[vgmAdr + 1] & 0xff, vgmBuf[vgmAdr + 2] & 0xff, model, (vgmBuf[vgmAdr] & 0x80) == 0 ? ym2151Hosei[0] : ym2151Hosei[1], vgmFrameCounter);
        vgmAdr += 3;
    }

    private void vcOKIM6258() {
        chipRegister.writeOKIM6258(0, vgmBuf[vgmAdr + 0x01] & 0x7F, vgmBuf[vgmAdr + 0x02] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcOKIM6295() {
        chipRegister.writeOKIM6295((vgmBuf[vgmAdr + 0x01] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 0x01] & 0x7F, vgmBuf[vgmAdr + 0x02] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcSAA1099() {
        chipRegister.writeSAA1099((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcWSwan() {
        chipRegister.writeWSwan((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcWSwanMem() {
        chipRegister.writeWSwanMem(0, (vgmBuf[vgmAdr + 0x01] & 0xff) | ((vgmBuf[vgmAdr + 0x02] & 0xff) << 8), vgmBuf[vgmAdr + 0x03] & 0xff, model);
        vgmAdr += 4;
    }

    private void vcPOKEY() {
        chipRegister.writePOKEY((vgmBuf[vgmAdr + 1] & 0x80) == 0 ? 0 : 1, vgmBuf[vgmAdr + 1] & 0x7f, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcSEGAPCM() {
//System.err.println("%4X %4X", vgmBuf[vgmAdr + 0x01], vgmBuf[vgmAdr + 0x02]);
        chipRegister.writeSEGAPCM(0, (vgmBuf[vgmAdr + 0x01] & 0xff) | ((vgmBuf[vgmAdr + 0x02] & 0xff) << 8), vgmBuf[vgmAdr + 0x03] & 0xff, model);
        vgmAdr += 4;
    }

    private void vcWaitNSamples() {
        vgmWait += ByteUtil.readLeShort(vgmBuf, vgmAdr + 1);
        vgmAdr += 3;
    }

    private void vcWait735Samples() {
        vgmWait += 735;
        vgmAdr++;
    }

    private void vcWait882Samples() {
        vgmWait += 882;
        vgmAdr++;
    }

    private void vcOverrideLength() {
        vgmAdr += 4;
    }

    private void vcEndOfSoundData() {
        vgmAdr = vgmBuf.length;
    }

    private void vcDataBlock() {

        isDataBlock = true;

        int bAdr = vgmAdr + 7;
        int bType = vgmBuf[vgmAdr + 2] & 0xff;
        int bLen = ByteUtil.readLeInt(vgmBuf, vgmAdr + 3);
        int chipId = 0;
        if ((bLen & 0x8000_0000) != 0) {
            bLen &= 0x7fff_ffff;
            chipId = 1;
        }

        switch (bType & 0xc0) {
        case 0x00:
        case 0x40:
            addPCMData(bType, bLen, bAdr);
            vgmAdr += bLen + 7;
            break;
        case 0x80:
            int romSize = ByteUtil.readLeInt(vgmBuf, vgmAdr + 7);
            int startAddress = ByteUtil.readLeInt(vgmBuf, vgmAdr + 0x0B);
            switch (bType & 0xff) {
            case 0x80:
                 // SEGA PCM
                chipRegister.writeSEGAPCMPCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpDataForSegaPCM(model, "SEGAPCM_PCMData", vgmAdr + 15, bLen - 8);
                break;
            case 0x81:

                // YM2608

                chipRegister.setYM2608Register(chipId, 0x1, 0x00, 0x20, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x00, 0x21, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x00, 0x00, model);

                chipRegister.setYM2608Register(chipId, 0x1, 0x10, 0x00, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x10, 0x80, model);

                chipRegister.setYM2608Register(chipId, 0x1, 0x00, 0x61, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x00, 0x68, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x01, opnaRamType, model);

                if (opnaRamType != 2) {
                    chipRegister.setYM2608Register(chipId, 0x1, 0x02, (startAddress >> 2) & 0xff, model);
                    chipRegister.setYM2608Register(chipId, 0x1, 0x03, (startAddress >> 10) & 0xff, model);
                } else {
                    chipRegister.setYM2608Register(chipId, 0x1, 0x02, (startAddress >> 5) & 0xff, model);
                    chipRegister.setYM2608Register(chipId, 0x1, 0x03, (startAddress >> 13) & 0xff, model);
                }
                chipRegister.setYM2608Register(chipId, 0x1, 0x04, 0xff, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x05, 0xff, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x0c, 0xff, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x0d, 0xff, model);

                // データ転送
                for (int cnt = 0; cnt < bLen - 8; cnt++) {
                    chipRegister.setYM2608Register(chipId, 0x1, 0x08, vgmBuf[vgmAdr + 15 + cnt] & 0xff, model);
                }
                chipRegister.setYM2608Register(chipId, 0x1, 0x00, 0x00, model);
                chipRegister.setYM2608Register(chipId, 0x1, 0x10, 0x80, model);

//                chipRegister.setYM2608Register(0x1, 0x10, 0x13, model);
//                chipRegister.setYM2608Register(0x1, 0x10, 0x80, model);
//                chipRegister.setYM2608Register(0x1, 0x00, 0x60, model);
//                chipRegister.setYM2608Register(0x1, 0x01, 0x00, model);

//                chipRegister.setYM2608Register(0x1, 0x02, (int)((startAddress >> 2) & 0xff), model);
//                chipRegister.setYM2608Register(0x1, 0x03, (int)((startAddress >> 10) & 0xff), model);
//                chipRegister.setYM2608Register(0x1, 0x04, (int)(((startAddress + bLen - 8) >> 2) & 0xff), model);
//                chipRegister.setYM2608Register(0x1, 0x05, (int)(((startAddress + bLen - 8) >> 10) & 0xff), model);
//                chipRegister.setYM2608Register(0x1, 0x0c, 0xff, model);
//                chipRegister.setYM2608Register(0x1, 0x0d, 0xff, model);

//                for (int cnt = 0; cnt < bLen - 8; cnt++) {
//                    chipRegister.setYM2608Register(0x1, 0x08, vgmBuf[vgmAdr + 15 + cnt], model);
//                    chipRegister.setYM2608Register(0x1, 0x10, 0x1b, model);
//                    chipRegister.setYM2608Register(0x1, 0x10, 0x13, model);
//                }

//                chipRegister.setYM2608Register(0x1, 0x00, 0x00, model);
//                chipRegister.setYM2608Register(0x1, 0x10, 0x80, model);

                while ((chipRegister.getYM2608Register(chipId, 0x1, 0x00, model) & 0xbf) != 0) {
                    try { Thread.sleep(0); } catch (InterruptedException e) {}
                }
                if (model == mdplayer.Common.EnmModel.RealModel) {
                    if ((chipId == 0 && setting.getYM2608Type()[0].getUseReal()[0])
                            || (chipId == 1 && setting.getYM2608Type()[1].getUseReal()[0])) {
                        try { Thread.sleep(500); } catch (InterruptedException e) {}
                    }
                }

                chipRegister.sendDataYM2608(chipId, model);
                dumpData(model, "YM2608_ADPCM", vgmAdr + 15, bLen - 8);
                break;

            case 0x82:
                if (ym2610AdpcmA[chipId] == null || ym2610AdpcmA[chipId].length != romSize)
                    ym2610AdpcmA[chipId] = new byte[romSize];
                if (ym2610AdpcmA[chipId].length > 0) {
                    for (int cnt = 0; cnt < bLen - 8; cnt++) {
                        ym2610AdpcmA[chipId][startAddress + cnt] = vgmBuf[vgmAdr + 15 + cnt];
                    }
                    if (model == mdplayer.Common.EnmModel.VirtualModel)
                        chipRegister.writeYm2610_SetAdpcmA(chipId, ym2610AdpcmA[chipId], model);
                    else
                        chipRegister.writeYm2610_SetAdpcmA(chipId, model, startAddress, bLen - 8, vgmBuf, vgmAdr + 15);
                    dumpData(model, "YM2610_ADPCMA", vgmAdr + 15, bLen - 8);
                }
                break;
            case 0x83:
                if (ym2610AdpcmB[chipId] == null || ym2610AdpcmB[chipId].length != romSize)
                    ym2610AdpcmB[chipId] = new byte[romSize];
                if (ym2610AdpcmB[chipId].length > 0) {
                    for (int cnt = 0; cnt < bLen - 8; cnt++) {
                        ym2610AdpcmB[chipId][startAddress + cnt] = vgmBuf[vgmAdr + 15 + cnt];
                    }
                    if (model == mdplayer.Common.EnmModel.VirtualModel)
                        chipRegister.WriteYM2610_SetAdpcmB(chipId, ym2610AdpcmB[chipId], model);
                    else
                        chipRegister.WriteYM2610_SetAdpcmB(chipId, model, startAddress, bLen - 8, vgmBuf, vgmAdr + 15);
                    dumpData(model, "YM2610_ADPCMB", vgmAdr + 15, bLen - 8);
                }
                break;

            case 0x84:
                // YMF278B
                chipRegister.writeYmF278BPCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "YMF278B_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x85:
                // YMF271
                chipRegister.writeYmF271PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "YMF271_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x86:
                // YMZ280B
                chipRegister.writeYmZ280BPCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "YMZ280B_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x87:
                // YMF278B
                chipRegister.writeYmF278BPCMRAMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "YMF278B_PCMRAMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x88:
                // Y8950Inst
                chipRegister.writeY8950PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "Y8950_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x89:
                // MultiPCM
                chipRegister.writeMultiPCMPCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "MultiPCM_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x8b:
                // OKIM6295
                chipRegister.writeOKIM6295PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "OKIM6295_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x8c:
                // K054539Inst
                chipRegister.writeK054539PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "K054539_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x8d:
                // C140Inst
                chipRegister.writeC140PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "C140_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x8e:
                // K053260Inst
                chipRegister.writeK053260PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "K053260_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x8f:
                // QSoundInst
                chipRegister.writeQSoundPCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "QSound_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x91:
                // X1-010
                chipRegister.writeX1_010PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "X1-010_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x92:
                // C352Inst
                chipRegister.writeC352PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "C352_PCMData", vgmAdr + 15, bLen - 8);
                break;

            case 0x93:
                // GA20
                chipRegister.writeGA20PCMData(chipId, romSize, startAddress, bLen - 8, vgmBuf, vgmAdr + 15, model);
                dumpData(model, "GA20_PCMData", vgmAdr + 15, bLen - 8);
                break;
            }
            vgmAdr += bLen + 7;
            break;
        case 0xc0:
            int stAdr = ByteUtil.readLeShort(vgmBuf, vgmAdr + 7);
            int dataSize = bLen - 2;
            int romData = vgmAdr + 9;
            if ((bType & 0x20) != 0) {
                stAdr = ByteUtil.readLeInt(vgmBuf, vgmAdr + 7);
                dataSize = bLen - 4;
                romData = vgmAdr + 11;
            }

            try {
                switch (bType & 0xff) {
                case 0xc0:
                    chipRegister.writeRF5C68PCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr + 9, model);
                    dumpData(model, "RF5C68_PCMData(8BitMonoSigned)", vgmAdr + 9, dataSize);
                    break;
                case 0xc1:
                    chipRegister.writeRF5C164PCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr + 9, model);
                    dumpData(model, "RF5C164_PCMData(8BitMonoSigned)", vgmAdr + 9, dataSize);
                    break;
                case 0xc2:
                    chipRegister.writeNESPCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr + 9, model);
                    dumpData(model, "NES_PCMData", vgmAdr + 9, dataSize);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            vgmAdr += bLen + 7;
            break;
        default:
            vgmAdr += bLen + 7;
            break;
        }

        isDataBlock = false;
    }

    private int dumpCounter = 0;
    private int opnaRamType = 0;

    private void dumpData(mdplayer.Common.EnmModel model, String chipName, int adr, int len) {
        if (model == mdplayer.Common.EnmModel.RealModel) return;
        if (setting == null) return;
        if (!setting.getOther().getDumpSwitch()) return;

        try {

            String fn = Path.combine(setting.getOther().getDumpPath(), String.format("%2$s_%3$s_%1$03d.bin", dumpCounter++, chipName, gd3.trackName.replace("*", "").replace("?", "").replace(" ", "").replace("\"", "").replace("/", "")));
            try (FileStream fs = new FileStream(fn, FileMode.OpenOrCreate, FileAccess.Write)) {
                fs.write(vgmBuf, adr, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
             // エラーは無視
        }
    }

    private void dumpDataForSegaPCM(mdplayer.Common.EnmModel model, String chipName, int adr, int len) {
        if (model == mdplayer.Common.EnmModel.RealModel) return;
        if (setting == null) return;
        if (!setting.getOther().getDumpSwitch()) return;

        try {
            String dFn = Path.combine(setting.getOther().getDumpPath(), String.format("%2$s_%3$s_%1$03d.wav", dumpCounter++, chipName, gd3.trackName.replace("*", "").replace("?", "").replace(" ", "").replace("\"", "")));
            List<Byte> des = new ArrayList<>();

            // 'RIFF'
            des.add((byte) 'R');
            des.add((byte) 'I');
            des.add((byte) 'F');
            des.add((byte) 'F');
            // サイズ
            //int fsize = src.length + 36;
            int fsize = len + 36;
            des.add((byte) ((fsize & 0xff) >> 0));
            des.add((byte) ((fsize & 0xff00) >> 8));
            des.add((byte) ((fsize & 0xff_0000) >> 16));
            des.add((byte) ((fsize & 0xff00_0000) >>> 24));
            // 'WAVE'
            des.add((byte) 'W');
            des.add((byte) 'A');
            des.add((byte) 'V');
            des.add((byte) 'E');
            // 'fmt '
            des.add((byte) 'f');
            des.add((byte) 'm');
            des.add((byte) 't');
            des.add((byte) ' ');
            // サイズ(16)
            des.add((byte) 0x10);
            des.add((byte) 0);
            des.add((byte) 0);
            des.add((byte) 0);
            // フォーマット(1)
            des.add((byte) 0x01);
            des.add((byte) 0x00);
            // チャンネル数(mono)
            des.add((byte) 0x01);
            des.add((byte) 0x00);
             // サンプリング周波数(16KHz)
            des.add((byte) 0x80);
            des.add((byte) 0x3e);
            des.add((byte) 0);
            des.add((byte) 0);
             // 平均データ割合(16K)
            des.add((byte) 0x80);
            des.add((byte) 0x3e);
            des.add((byte) 0);
            des.add((byte) 0);
             // ブロックサイズ(1)
            des.add((byte) 0x01);
            des.add((byte) 0x00);
             // ビット数(8bit)
            des.add((byte) 0x08);
            des.add((byte) 0x00);

            // 'data'
            des.add((byte) 'd');
            des.add((byte) 'a');
            des.add((byte) 't');
            des.add((byte) 'a');
            // サイズ(データサイズ)
            des.add((byte) ((len & 0xff) >> 0));
            des.add((byte) ((len & 0xff00) >> 8));
            des.add((byte) ((len & 0xff_0000) >> 16));
            des.add((byte) ((len & 0xff00_0000) >>> 24));

            for (int i = 0; i < len; i++) {
                des.add(vgmBuf[adr + i]);
            }

             // 出力
            File.writeAllBytes(dFn, toByteArray(des));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vcPCMRamWrite() {

        isPcmRAMWrite = true;

        int bType = vgmBuf[vgmAdr + 2] & 0x7f;
        //CurrentChip = (vgmBuf[vgmAdr + 2] & 0x80)>>7;
        int bReadOffset = ByteUtil.readLe24(vgmBuf, vgmAdr + 3);
        int bWriteOffset = ByteUtil.readLe24(vgmBuf, vgmAdr + 6);
        int bSize = ByteUtil.readLe24(vgmBuf, vgmAdr + 9);
        if (bSize == 0) bSize = 0x100_0000;
        Integer pcmAdr = getPCMAddressFromPCMBank(bType, bReadOffset);
        if (pcmAdr != null) {
            if (bType == 0x01) {
                chipRegister.writeRF5C68PCMData(0, bWriteOffset, bSize, pcmBank[bType].data, pcmAdr, model);
            }
            if (bType == 0x02) {
                chipRegister.writeRF5C164PCMData(0, bWriteOffset, bSize, pcmBank[bType].data, pcmAdr, model);
            }
        }

        vgmAdr += 12;

        isPcmRAMWrite = false;
    }

    private void vcWaitN1Samples() {
        vgmWait += (vgmBuf[vgmAdr] & 0xff) - 0x6f;
        vgmAdr++;
    }

    private void vcWaitNSamplesAndSendYM26120x2a() {
        int dat = getDACFromPCMBank();

        vgmWait += (vgmBuf[vgmAdr] & 0xff) - 0x80;

        chipRegister.setYM2612Register(0, 0, 0x2a, dat, model, vgmFrameCounter);

        vgmAdr++;
    }

    private void vcSetupStreamControl() {
//        if (model != enmModel.VirtualModel) {
//            vgmAdr += 5;
//            return;
//        }

        int si = vgmBuf[vgmAdr + 1] & 0xff;
        if (si == 0xff) {
            vgmAdr += 5;
            return;
        }
        if (!dacCtrl[si].enable) {
            dacControl.device_start_daccontrol(si);
            dacControl.device_reset_daccontrol(si);
            dacCtrl[si].enable = true;
            dacCtrlUsg[dacCtrlUsed] = (byte) si;
            dacCtrlUsed++;
        }
        int chipId = vgmBuf[vgmAdr + 2] & 0xff;
        int port = vgmBuf[vgmAdr + 3] & 0xff;
        int cmd = vgmBuf[vgmAdr + 4] & 0xff;

        dacControl.setupChip(si, chipId & 0x7F, (chipId & 0x80) >> 7, port * 0x100 + cmd);
        vgmAdr += 5;
    }

    private void vcSetStreamData() {
//        if (model != enmModel.VirtualModel) {
//            vgmAdr += 5;
//            return;
//        }

        int si = vgmBuf[vgmAdr + 1] & 0xff;
        if (si == 0xff) {
            vgmAdr += 5;
            return;
        }
        dacCtrl[si].bank = vgmBuf[vgmAdr + 2] & 0xff;
        if (dacCtrl[si].bank >= PCM_BANK_COUNT)
            dacCtrl[si].bank = 0x00;

        VgmPcmBank tempPCM = pcmBank[dacCtrl[si].bank];
        //last95Max = tempPCM->BankCount;
        dacControl.setData(si, tempPCM.data, tempPCM.dataSize, vgmBuf[vgmAdr + 3] & 0xff, vgmBuf[vgmAdr + 4] & 0xff);

        vgmAdr += 5;
    }

    private void vcSetStreamFrequency() {
//        if (model != enmModel.VirtualModel) {
//            vgmAdr += 6;
//            return;
//        }

        int si = vgmBuf[vgmAdr + 1] & 0xff;
        if (si == 0xff || !dacCtrl[si].enable) {
            vgmAdr += 0x06;
            return;
        }
        int tempLng = ByteUtil.readLeInt(vgmBuf, vgmAdr + 2);
        //last95Freq = tempLng;
        dacControl.set_frequency(si, tempLng);
        vgmAdr += 6;
    }

    private void vcStartStream() {
//        if (model != enmModel.VirtualModel) {
//            vgmAdr += 8;
//            return;
//        }

        int si = vgmBuf[vgmAdr + 1] & 0xff;
        if (si == 0xff || !dacCtrl[si].enable || pcmBank[dacCtrl[si].bank].bankCount == 0) {
            vgmAdr += 0x08;
            return;
        }
        int dataStart = ByteUtil.readLeInt(vgmBuf, vgmAdr + 2);
        //last95Drum = 0xffFF;
        int tempByt = vgmBuf[vgmAdr + 6] & 0xff;
        int dataLen = ByteUtil.readLeInt(vgmBuf, vgmAdr + 7);
        dacControl.start(si, dataStart, tempByt, dataLen);
        vgmAdr += 0x0B;
    }

    private void vcStopStream() {
//        if (model != enmModel.VirtualModel) {
//            vgmAdr += 2;
//            return;
//        }

        int si = vgmBuf[vgmAdr + 1] & 0xff;
        if (!dacCtrl[si].enable) {
            vgmAdr += 0x02;
            return;
        }
        // last95Drum = 0xffFF;
        if (si < 0xff) {
            dacControl.stop(si);
        } else {
            for (si = 0x00; si < 0xff; si++)
                dacControl.stop(si);
        }
        vgmAdr += 0x02;
    }

    private void vcStartStreamFastCall() {
//        if (model != enmModel.VirtualModel) {
//            vgmAdr += 5;
//            return;
//        }

        int curChip = vgmBuf[vgmAdr + 1] & 0xff;
        if (curChip == 0xff || !dacCtrl[curChip].enable ||
                pcmBank[dacCtrl[curChip].bank].bankCount == 0) {
            vgmAdr += 0x05;
            return;
        }
        VgmPcmBank tempPCM = pcmBank[dacCtrl[curChip].bank];
        int TempSht = ByteUtil.readLeShort(vgmBuf, vgmAdr + 2);
        //Last95Drum = TempSht;
        //Last95Max = tempPCM->BankCount;
        if (TempSht >= tempPCM.bankCount)
            TempSht = 0x00;
        VgmPcmData tempBnk = tempPCM.bank.get(TempSht);

        int tempByt = DacControl.DacControl_.DCTRL_LMODE_BYTES |
                (vgmBuf[vgmAdr + 4] & 0x10) |         // Reverse Mode
                ((vgmBuf[vgmAdr + 4] & 0x01) << 7);   // Looping
        dacControl.start(curChip, tempBnk.dataStart, tempByt, tempBnk.dataSize);
        vgmAdr += 0x05;
    }

    private void vcSeekToOffsetInPCMDataBank() {
        pcmBank[0x00].dataPos = ByteUtil.readLeInt(vgmBuf, vgmAdr + 1);
        vgmAdr += 5;
    }

    private void vcRf5c68() {
        int id = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        int cmd = vgmBuf[vgmAdr + 1] & 0x7f;
        chipRegister.writeRF5C68(id, cmd, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcRf5c68MemoryWrite() {
        int offset = ByteUtil.readLeShort(vgmBuf, vgmAdr + 1);
        chipRegister.writeRF5C68MemW(0, offset, vgmBuf[vgmAdr + 3] & 0xff, model);
        vgmAdr += 4;
    }

    private void vcRf5c164() {
        int id = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        int cmd = vgmBuf[vgmAdr + 1] & 0x7f;
        chipRegister.writeRF5C164(id, cmd, vgmBuf[vgmAdr + 2] & 0xff, model);
        vgmAdr += 3;
    }

    private void vcRf5c164MemoryWrite() {
        int offset = ByteUtil.readLeShort(vgmBuf, vgmAdr + 1);
        chipRegister.writeRF5C164MemW(0, offset, vgmBuf[vgmAdr + 3] & 0xff, model);
        vgmAdr += 4;
    }

    private void vcPWM() {
        int cmd = (vgmBuf[vgmAdr + 1] & 0xf0) >> 4;
        int data = (vgmBuf[vgmAdr + 1] & 0xf) * 0x100 + vgmBuf[vgmAdr + 2] & 0xff;
        chipRegister.writePWM(0, cmd, data, model);
        vgmAdr += 3;
    }

    private void vcK051649() {
        int scc1_port = vgmBuf[vgmAdr + 1] & 0x7f;
        int scc1_offset = vgmBuf[vgmAdr + 2] & 0xff;
        int rDat = vgmBuf[vgmAdr + 3] & 0xff;
        int scc1_chipId = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        vgmAdr += 4;
        chipRegister.writeK051649(scc1_chipId, (scc1_port << 1) | 0x00, scc1_offset, model);
        chipRegister.writeK051649(scc1_chipId, (scc1_port << 1) | 0x01, rDat, model);

    }

    private void vcK053260() {
        int id = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        int adr = vgmBuf[vgmAdr + 1] & 0x7f;
        int data = vgmBuf[vgmAdr + 2] & 0xff;
        chipRegister.writeK053260(id, adr, data, model);
        vgmAdr += 3;
    }

    private void vcK054539() {
        int id = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        int adr = (vgmBuf[vgmAdr + 1] & 0x7f) * 0x100 + (vgmBuf[vgmAdr + 2] & 0xff);
        int data = vgmBuf[vgmAdr + 3] & 0xff;
        chipRegister.writeK054539(id, adr, data, model);
        vgmAdr += 4;
    }

    private void vcC140() {
        int id = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        int adr = (vgmBuf[vgmAdr + 1] & 0x7f) * 0x100 + (vgmBuf[vgmAdr + 2] & 0xff);
        int data = vgmBuf[vgmAdr + 3] & 0xff;
        chipRegister.writeC140(id, adr, data, model);
        vgmAdr += 4;
    }

    private void vcC352() {
        int id = (vgmBuf[vgmAdr + 1] & 0x80) != 0 ? 1 : 0;
        int adr = (vgmBuf[vgmAdr + 1] & 0x7f) * 0x100 + (vgmBuf[vgmAdr + 2] & 0xff);
        int data = (vgmBuf[vgmAdr + 3] & 0xff) * 0x100 + (vgmBuf[vgmAdr + 4] & 0xff);

        chipRegister.writeC352(id, adr, data, model);
        vgmAdr += 5;
    }

    private void addPCMData(int Type, int dataSize, int adr) {
        int curBnk;
        VgmPcmBank tempPCM;
        VgmPcmData tempBnk;
        int bankSize;
//        boolean retVal;
        int bnkType;
        int curDAC;

        bnkType = Type & 0x3F;
        if (bnkType >= PCM_BANK_COUNT || vgmCurLoop > 0)
            return;

        if (Type == 0x7F) {
            //ReadPCMTable(dataSize, data);
            readPCMTable(dataSize, adr);
            return;
        }

        tempPCM = pcmBank[bnkType]; // &PCMBank[bnkType];
        tempPCM.bnkPos++;
        if (tempPCM.bnkPos <= tempPCM.bankCount)
            return; // Speed hack for restarting playback (skip already loaded blocks)
        curBnk = tempPCM.bankCount;
        tempPCM.bankCount++;
        //if (Last95Max != 0xffFF) Last95Max = tempPCM.BankCount;
        tempPCM.bank.add(new VgmPcmData()); // = (VgmPcmData*)realloc(tempPCM->Bank, sizeof(VgmPcmData) * tempPCM->BankCount);

        if ((Type & 0x40) == 0)
            bankSize = dataSize;
        else
            bankSize = ByteUtil.readLeInt(vgmBuf, adr + 1); // ReadLE32(&Data[0x01]);

        byte[] newData = new byte[tempPCM.dataSize + bankSize];
        if (tempPCM.data != null && tempPCM.data.length > 0)
            System.arraycopy(tempPCM.data, 0, newData, 0, tempPCM.data.length);
        tempPCM.data = newData;

        //tempPCM.Data = new byte[tempPCM.dataSize + bankSize]; // realloc(tempPCM->Data, tempPCM->dataSize + bankSize);
        tempBnk = tempPCM.bank.get(curBnk);
        tempBnk.dataStart = tempPCM.dataSize;
        tempBnk.data = new byte[bankSize];
        boolean retVal = true;
        if ((Type & 0x40) == 0) {
            tempBnk.dataSize = dataSize;
            for (int i = 0; i < dataSize; i++) {
                tempPCM.data[i + tempBnk.dataStart] = vgmBuf[adr + i];
                tempBnk.data[i] = vgmBuf[adr + i];
            }
            //tempBnk.Data = tempPCM.Data + tempBnk.DataStart;
            //memcpy(tempBnk->Data, data, dataSize);
        } else {
            //tempBnk.Data = tempPCM.Data + tempBnk.DataStart;
            retVal = decompressDataBlk(tempBnk, dataSize, adr);
            if (!retVal) {
                tempBnk.data = null;
                tempBnk.dataSize = 0x00;
            } else {
                // dataSize; i++)
                System.arraycopy(tempBnk.data, 0, tempPCM.data, tempBnk.dataStart, bankSize);
            }
        }
        //if (bankSize != tempBnk.dataSize) System.err.printf("Error reading data Block! data size conflict!\n");
        if (retVal)
            tempPCM.dataSize += bankSize;

        // realloc may've moved the Bank block, so refresh all DAC Streams
        for (curDAC = 0x00; curDAC < dacCtrlUsed; curDAC++) {
            if (dacCtrl[dacCtrlUsg[curDAC]].bank == bnkType)
                dacControl.refresh_data(dacCtrlUsg[curDAC] & 0xff, tempPCM.data, tempPCM.dataSize);
        }
    }

    private boolean decompressDataBlk(VgmPcmData bank, int dataSize, int adr) {
        int comprType;
        int bitDec;
        int bitCmp;
        int cmpSubType;
        int addVal;
        int inPos;
        int inDataEnd;
        int outPos;
        int outDataEnd;
        int inVal;
        int outVal = 0;// FUINT16 outVal;
        int valSize;
        int inShift;
        int outShift;
        int ent1B = 0;// UINT8* ent1B;
        int ent2B = 0;// UINT16* ent2B;
//#if defined(_DEBUG) && defined(WIN32)
//        UINT32 Time;
//#endif

        // ReadBits Variables
        int bitsToRead;
        int bitReadVal;
        int inValB;
        int bitMask;
        int outBit;

        // Variables for DPCM
        int outMask;

//#if defined(_DEBUG) && defined(WIN32)
//        Time = GetTickCount();
//#endif
        comprType = vgmBuf[adr + 0];
        bank.dataSize = ByteUtil.readLeInt(vgmBuf, adr + 1);

        switch (comprType) {
        case 0x00:  // n-Bit compression
            bitDec = vgmBuf[adr + 5];
            bitCmp = vgmBuf[adr + 6];
            cmpSubType = vgmBuf[adr + 7];
            addVal = ByteUtil.readLeShort(vgmBuf, adr + 8);

            if (cmpSubType == 0x02) {
                //bank.dataSize = 0x00;
                //return false;

                ent1B = 0;// (UINT8*)PCMTbl.Entries; // Big Endian note: Those are stored : LE and converted when reading.
                ent2B = 0;// (UINT16*)PCMTbl.Entries;
                if (pcmTbl.entryCount == 0) {
                    bank.dataSize = 0x00;
                    //printf("Error loading table-compressed data block! No table loaded!\n");
                    return false;
                } else if (bitDec != pcmTbl.bitDec || bitCmp != pcmTbl.bitCmp) {
                    bank.dataSize = 0x00;
                    //printf("Warning! data block and loaded value table incompatible!\n");
                    return false;
                }
            }

            valSize = (byte) ((bitDec + 7) / 8);
            inPos = adr + 0x0A;
            inDataEnd = adr + dataSize;
            inShift = 0;
            outShift = bitDec - bitCmp;
//            outDataEnd = bank.Data + bank.dataSize;
            outDataEnd = bank.dataSize;

            //for (outPos = bank->Data; outPos < outDataEnd && inPos < inDataEnd; outPos += valSize)
            for (outPos = 0; outPos < outDataEnd && inPos < inDataEnd; outPos += valSize) {
                //inVal = ReadBits(Data, inPos, &inShift, bitCmp);
                // inlined - instanceof 30% faster
                outBit = 0x00;
                inVal = 0x0000;
                bitsToRead = bitCmp;
                while (bitsToRead != 0) {
                    bitReadVal = Math.min(bitsToRead, 8);
                    bitsToRead -= bitReadVal;
                    bitMask = (1 << bitReadVal) - 1;

                    inShift += bitReadVal;
                    //inValB = (byte)((vgmBuf[inPos] << inShift >> 8) & bitMask);
                    inValB = (vgmBuf[inPos] << inShift >> 8) & bitMask;
                    if (inShift >= 8) {
                        inShift -= 8;
                        inPos++;
                        if (inShift != 0)
                            inValB |= (vgmBuf[inPos] << inShift >> 8) & bitMask;
                    }

                    inVal |= inValB << outBit;
                    outBit += bitReadVal;
                }

                switch (cmpSubType) {
                case 0x00:  // Copy
                    outVal = inVal + addVal;
                    break;
                case 0x01:  // Shift Left
                    outVal = (inVal << outShift) + addVal;
                    break;
                case 0x02:  // Table
                    switch (valSize) {
                    case 0x01:
                        outVal = pcmTbl.entries[ent1B + inVal];
                        break;
                    case 0x02:
//#ifndef BIG_ENDIAN
//                        outVal = ent2B[inVal];
//#else
                        //ReadLE16((UINT8*)&ent2B[inVal]);
                        outVal = (pcmTbl.entries[ent2B + inVal * 2] & 0xff) + (pcmTbl.entries[ent2B + inVal * 2 + 1] & 0xff) * 0x100;
//#endif
                        break;
                    }
                    break;
                }

//#ifndef BIG_ENDIAN
//                //memcpy(outPos, &outVal, valSize);
//                if (valSize == 0x01)
//                    *((UINT8 *) outPos) =(UINT8) outVal;
//                else //if (valSize == 0x02)
//                    *((UINT16 *) outPos) =(UINT16) outVal;
//#else
                if (valSize == 0x01) {
                    bank.data[outPos] = (byte) outVal;
                } else { // if (valSize == 0x02)
                    bank.data[outPos + 0x00] = (byte) ((outVal & 0x00FF) >> 0);
                    bank.data[outPos + 0x01] = (byte) ((outVal & 0xff00) >> 8);
                }
//#endif
            }
            break;
        case 0x01:  // Delta-PCM
            bitDec = vgmBuf[adr + 5] & 0xff; // data[0x05];
            bitCmp = vgmBuf[adr + 6] & 0xff; // data[0x06];
            outVal = ByteUtil.readLeShort(vgmBuf, adr + 8);// ReadLE16(&Data[0x08]);

            ent1B = 0; // (UINT8*)PCMTbl.Entries;
            ent2B = 0; // (UINT16*)PCMTbl.Entries;
            if (pcmTbl.entryCount == 0) {
                bank.dataSize = 0x00;
                //printf("Error loading table-compressed data block! No table loaded!\n");
                return false;
            } else if (bitDec != pcmTbl.bitDec || bitCmp != pcmTbl.bitCmp) {
                bank.dataSize = 0x00;
                //printf("Warning! data block and loaded value table incompatible!\n");
                return false;
            }

            valSize = (bitDec + 7) / 8;
            outMask = (1 << bitDec) - 1;
            inPos = adr + 0xa;
            inDataEnd = adr + dataSize;
            inShift = 0;
            outShift = bitDec - bitCmp;
            outDataEnd = bank.dataSize;// bank.Data + bank.dataSize;
            addVal = 0x0000;

            // for (outPos = bank.Data; outPos < outDataEnd && inPos < inDataEnd; outPos += valSize)
            for (outPos = 0; outPos < outDataEnd && inPos < inDataEnd; outPos += valSize) {
                // inVal = ReadBits(Data, inPos, &inShift, bitCmp);
                // inlined - instanceof 30% faster
                outBit = 0x00;
                inVal = 0x0000;
                bitsToRead = bitCmp;
                while (bitsToRead != 0) {
                    bitReadVal = Math.min(bitsToRead, 8);
                    bitsToRead -= bitReadVal;
                    bitMask = (1 << bitReadVal) - 1;

                    inShift += bitReadVal;
                    inValB = (vgmBuf[inPos] << inShift >> 8) & bitMask;
                    if (inShift >= 8) {
                        inShift -= 8;
                        inPos++;
                        if (inShift != 0)
                            inValB |= (vgmBuf[inPos] << inShift >> 8) & bitMask;
                    }

                    inVal |= inValB << outBit;
                    outBit += bitReadVal;
                }

                switch (valSize) {
                case 0x01:
                    addVal = pcmTbl.entries[ent1B + inVal] & 0xff;
                    outVal += addVal;
                    outVal &= outMask;
                    bank.data[outPos] = (byte) outVal; // *((UINT8*)outPos) = (UINT8)outVal;
                    break;
                case 0x02:
//#ifndef BIG_ENDIAN
//                    addVal = ent2B[inVal];
//#else
                    addVal = (pcmTbl.entries[ent2B + inVal] & 0xff) + (pcmTbl.entries[ent2B + inVal + 1] & 0xff) * 0x100;
                    //addVal = ReadLE16((UINT8*)&ent2B[inVal]);
//#endif
                    outVal += addVal;
                    outVal &= outMask;
//#ifndef BIG_ENDIAN
//                    *((UINT16*)outPos) = (UINT16)outVal;
//#else
                    bank.data[outPos + 0x00] = (byte) ((outVal & 0x00FF) >> 0);
                    bank.data[outPos + 0x01] = (byte) ((outVal & 0xff00) >> 8);
//#endif
                    break;
                }
            }
            break;
        default:
            //printf("Error: Unknown data block compression!\n");
            return false;
        }

//#if defined(_DEBUG) && defined(WIN32)
//        Time = GetTickCount() - Time;
//        printf("Decompression Time: %lu\n", Time);
//#endif

        return true;
    }

    private void readPCMTable(int dataSize, int adr) {
        int valSize;
        int tblSize;

        pcmTbl.comprType = vgmBuf[adr + 0] & 0xff; // data[0x00];
        pcmTbl.cmpSubType = vgmBuf[adr + 1] & 0xff; // data[0x01];
        pcmTbl.bitDec = vgmBuf[adr + 2] & 0xff; // data[0x02];
        pcmTbl.bitCmp = vgmBuf[adr + 3] & 0xff; // data[0x03];
        pcmTbl.entryCount = ByteUtil.readLeShort(vgmBuf, adr + 4);// ReadLE16(&Data[0x04]);

        valSize = (pcmTbl.bitDec + 7) / 8;
        tblSize = pcmTbl.entryCount * valSize;

        pcmTbl.entries = new byte[tblSize];// realloc(PCMTbl.Entries, tblSize);
        for (int i = 0; i < tblSize; i++) pcmTbl.entries[i] = vgmBuf[adr + 6 + i];
        //memcpy(PCMTbl.Entries, &Data[0x06], tblSize);

        if (dataSize < 0x06 + tblSize) {
            //System.err.printf("Warning! Bad PCM Table Length!\n");
            //printf("Warning! Bad PCM Table Length!\n");
        }
    }

    private int getDACFromPCMBank() {
        // for Ym2612Inst DAC data only
//        VgmPcmBank* TempPCM;
//        UINT32 CurBnk;
        int dataPos;

//        TempPCM = &PCMBank[0x00];
//        dataPos = TempPCM -> dataPos;
//        for (CurBnk = 0x00; CurBnk < TempPCM -> BankCount; CurBnk++) {
//            if (dataPos < TempPCM -> Bank[CurBnk].DataSize) {
//                if (TempPCM -> dataPos < TempPCM -> dataSize)
//                    TempPCM -> dataPos++;
//                return TempPCM -> Bank[CurBnk].Data[dataPos];
//            }
//            dataPos -= TempPCM -> Bank[CurBnk].DataSize;
//        }
//        return 0x80;

        dataPos = pcmBank[0x00].dataPos;
        if (dataPos >= pcmBank[0x00].dataSize)
            return 0x80;

        pcmBank[0x00].dataPos++;
        return pcmBank[0x00].bank.get(0).data[dataPos] & 0xff;
    }

    /** @return nullable */
    private Integer getPCMAddressFromPCMBank(int type, int dataPos) {
        if (type >= PCM_BANK_COUNT)
            return null;

        if (dataPos >= pcmBank[type].dataSize)
            return null;

        return dataPos;
    }

    private boolean getInformationHeader() {
        chips = new ArrayList<>();
        usedChips = "";

        sn76489ClockValue = 0; // defaultSN76489ClockValue;
        ym2612ClockValue = 0; // defaultYM2612ClockValue;
        yn2151ClockValue = 0;
        segaPCMClockValue = 0;
        ym2203ClockValue = 0;
        yn2608ClockValue = 0;
        ym2610ClockValue = 0;
        ym3812ClockValue = 0;
        ymF262ClockValue = 0;
        rf5C68ClockValue = 0;
        rf5C164ClockValue = 0; // defaultRF5C164ClockValue;
        pwmClockValue = 0; // defaultPWMClockValue;
        okiM6258ClockValue = 0; // defaultOKIM6258ClockValue;
        c140ClockValue = 0; // defaultC140ClockValue;
        okiM6295ClockValue = 0; // defaultOKIM6295ClockValue;
        ay8910ClockValue = 0;
        ym2413ClockValue = 0;
        huC6280ClockValue = 0;
        k054539ClockValue = 0;
        nesClockValue = 0;
        multiPCMClockValue = 0;
        saa1099ClockValue = 0;
        x1_010ClockValue = 0;
        wSwanClockValue = 0;

         // ヘッダーを読み込めるサイズをもっているかチェック
        if (vgmBuf.length < 0x40) return false;

         // ヘッダーから情報取得

        int vgm = ByteUtil.readLeInt(vgmBuf, 0x00);
        if (vgm != FCC_VGM) return false;

        vgmEof = ByteUtil.readLeInt(vgmBuf, 0x04);

        int version = ByteUtil.readLeInt(vgmBuf, 0x08);
        this.version = String.format("%d.%d%d", (version & 0xf00) / 0x100, (version & 0xf0) / 0x10, (version & 0xf));
         // バージョンチェック
        if (version < 0x0101) {
            System.err.printf("Warning:This file instanceof older version(%s).", this.version);
            //return false;
        }

        int SN76489clock = ByteUtil.readLeInt(vgmBuf, 0x0c);
        if (SN76489clock != 0) {
            sn76489ClockValue = SN76489clock & 0x3fff_ffff;
            sn76489DualChipFlag = (SN76489clock & 0x4000_0000) != 0;
            sn76489NGPFlag = (SN76489clock & 0x8000_0000) != 0;
            if (version < 0x0150) {
                sn76489Option = new Object[] {
                        (byte) 9,
                        (byte) 0,
                        (byte) 16,
                        (byte) 0
                };
            } else {
                sn76489Option = new Object[] {
                        vgmBuf[0x28],
                        vgmBuf[0x29],
                        vgmBuf[0x2a],
                        vgmBuf[0x2b]
                };
            }
            if (sn76489DualChipFlag) chips.add("SN76489x2");
            else chips.add("SN76489");
        }

        int YM2413clock = ByteUtil.readLeInt(vgmBuf, 0x10);
        if (YM2413clock != 0) {
            ym2413ClockValue = YM2413clock & 0x3fff_ffff;
            ym2413DualChipFlag = (YM2413clock & 0x4000_0000) != 0;
            ym2413VRC7Flag = (YM2413clock & 0x8000_0000) != 0;
            if (!ym2413VRC7Flag) {
                if (ym2413DualChipFlag) chips.add("YM2413x2");
                else chips.add("YM2413");
            } else {
                if (ym2413DualChipFlag) chips.add("VRC7x2");
                else chips.add("VRC7");
            }
        }

        if (version == 0x0101) {
            int YM2612clock = ByteUtil.readLeInt(vgmBuf, 0x10);
            if (YM2612clock != 0) {
                ym2612ClockValue = YM2612clock & 0x3fff_ffff;
                ym2612DualChipFlag = (YM2612clock & 0x4000_0000) != 0;
                if (ym2612DualChipFlag) chips.add("YM2612x2");
                else chips.add("Ym2612Inst");
            }

            int YM2151clock = ByteUtil.readLeInt(vgmBuf, 0x10);
            if (YM2151clock != 0) {
                yn2151ClockValue = YM2151clock & 0x3fff_ffff;
                ym2151DualChipFlag = (YM2151clock & 0x4000_0000) != 0;
                if (ym2151DualChipFlag) chips.add("YM2151x2");
                else chips.add("YM2151");
            }
        }

        totalCounter = ByteUtil.readLeInt(vgmBuf, 0x18);
        if (totalCounter < 0) return false;

        vgmLoopOffset = ByteUtil.readLeInt(vgmBuf, 0x1c);

        loopCounter = ByteUtil.readLeInt(vgmBuf, 0x20);

        if (version > 0x0101) {

            int YM2612clock = ByteUtil.readLeInt(vgmBuf, 0x2c);
            if (YM2612clock != 0) {
                ym2612ClockValue = YM2612clock & 0x3fff_ffff;
                ym2612DualChipFlag = (YM2612clock & 0x4000_0000) != 0;
                if (ym2612DualChipFlag) chips.add("YM2612x2");
                else chips.add("Ym2612Inst");
            }

            int YM2151clock = ByteUtil.readLeInt(vgmBuf, 0x30);
            if (YM2151clock != 0) {
                yn2151ClockValue = YM2151clock & 0x3fff_ffff;
                ym2151DualChipFlag = (YM2151clock & 0x4000_0000) != 0;
                if (ym2151DualChipFlag) chips.add("YM2151x2");
                else chips.add("YM2151");
            }

            //setYM2151Hosei();

            vgmDataOffset = ByteUtil.readLeInt(vgmBuf, 0x34);
            if (vgmDataOffset == 0) {
                vgmDataOffset = 0x40;
            } else {
                vgmDataOffset += 0x34;
            }

            //if (version >= 0x0151)
            {
                if (vgmDataOffset > 0x38) {
                    int SegaPCMclock = ByteUtil.readLeInt(vgmBuf, 0x38);
                    int SPCMInterface = ByteUtil.readLeInt(vgmBuf, 0x3c);
                    if (SegaPCMclock != 0 && SPCMInterface != 0) {
                        chips.add("Sega PCM");
                        segaPCMClockValue = SegaPCMclock;
                        segaPCMInterface = SPCMInterface;
                    }
                }

                if (vgmDataOffset > 0x40) {
                    int RF5C68clock = ByteUtil.readLeInt(vgmBuf, 0x40);
                    if (RF5C68clock != 0) {
                        rf5C68ClockValue = RF5C68clock & 0x3fff_ffff;
                        rf5C68DualChipFlag = (RF5C68clock & 0x4000_0000) != 0;
                        if (rf5C68DualChipFlag) chips.add("RF5C68x2");
                        else chips.add("RF5C68");
                    }
                }

                if (vgmDataOffset > 0x44) {
                    int YM2203clock = ByteUtil.readLeInt(vgmBuf, 0x44);
                    if (YM2203clock != 0) {
                        ym2203ClockValue = YM2203clock & 0x3fff_ffff;
                        ym2203DualChipFlag = (YM2203clock & 0x4000_0000) != 0;
                        if (ym2203DualChipFlag) chips.add("YM2203x2");
                        else chips.add("YM2203");
                    }
                }

                if (vgmDataOffset > 0x48) {
                    int YM2608clock = ByteUtil.readLeInt(vgmBuf, 0x48);
                    if (YM2608clock != 0) {
                        yn2608ClockValue = YM2608clock & 0x3fff_ffff;
                        ym2608DualChipFlag = (YM2608clock & 0x4000_0000) != 0;
                        if (ym2608DualChipFlag) chips.add("YM2608x2");
                        else chips.add("YM2608");

                        opnaRamType = searchOpnaRamType() ? 0x2 : 0x0;
                    }
                }

                if (vgmDataOffset > 0x4c) {
                    int YM2610Bclock = ByteUtil.readLeInt(vgmBuf, 0x4c);
                    if (YM2610Bclock != 0) {
                        ym2610ClockValue = YM2610Bclock & 0x3fff_ffff;
                        ym2610DualChipFlag = (YM2610Bclock & 0x4000_0000) != 0;
                        if (ym2610DualChipFlag) chips.add("YM2610/Bx2");
                        else chips.add("YM2610/B");
                    }
                }

                if (vgmDataOffset > 0x50) {
                    int YM3812clock = ByteUtil.readLeInt(vgmBuf, 0x50);
                    if (YM3812clock != 0) {
                        ym3812ClockValue = YM3812clock & 0x3fff_ffff;
                        ym3812DualChipFlag = (YM3812clock & 0x4000_0000) != 0;
                        if (ym2610DualChipFlag) chips.add("YM3812x2");
                        else chips.add("YM3812");
                    }
                }

                if (vgmDataOffset > 0x54) {
                    int YM3526clock = ByteUtil.readLeInt(vgmBuf, 0x54);
                    if (YM3526clock != 0) {
                        ym3526ClockValue = YM3526clock & 0x3fff_ffff;
                        ym3526DualChipFlag = (YM3526clock & 0x4000_0000) != 0;
                        if (ym3526DualChipFlag) chips.add("YM3526x2");
                        else chips.add("YM3526");
                    }
                }

                if (vgmDataOffset > 0x58) {
                    int Y8950clock = ByteUtil.readLeInt(vgmBuf, 0x58);
                    if (Y8950clock != 0) {
                        y8950ClockValue = Y8950clock & 0x3fff_ffff;
                        y8950DualChipFlag = (Y8950clock & 0x4000_0000) != 0;
                        if (y8950DualChipFlag) chips.add("Y8950x2");
                        else chips.add("Y8950Inst");
                    }
                }

                if (vgmDataOffset > 0x5c) {
                    int YMF262clock = ByteUtil.readLeInt(vgmBuf, 0x5c);
                    if (YMF262clock != 0) {
                        ymF262ClockValue = YMF262clock & 0x3fff_ffff;
                        ymF262DualChipFlag = (YMF262clock & 0x4000_0000) != 0;
                        if (ymF262DualChipFlag) chips.add("YMF262x2");
                        else chips.add("YMF262");
                    }
                }

                if (vgmDataOffset > 0x60) {
                    int YMF278Bclock = ByteUtil.readLeInt(vgmBuf, 0x60);
                    if (YMF278Bclock != 0) {
                        ymF278BClockValue = YMF278Bclock & 0x3fff_ffff;
                        ymF278BDualChipFlag = (YMF278Bclock & 0x4000_0000) != 0;
                        if (ymF278BDualChipFlag) chips.add("YMF278Bx2");
                        else chips.add("YMF278B");
                    }
                }

                if (vgmDataOffset > 0x64) {
                    int YMF271clock = ByteUtil.readLeInt(vgmBuf, 0x64);
                    if (YMF271clock != 0) {
                        ymF271ClockValue = YMF271clock & 0x3fff_ffff;
                        ymF271DualChipFlag = (YMF271clock & 0x4000_0000) != 0;
                        if (ymF271DualChipFlag) chips.add("YMF271x2");
                        else chips.add("YMF271");
                    }
                }

                if (vgmDataOffset > 0x68) {
                    int YMZ280Bclock = ByteUtil.readLeInt(vgmBuf, 0x68);
                    if (YMZ280Bclock != 0) {
                        ymZ280BClockValue = YMZ280Bclock & 0x3fff_ffff;
                        ymZ280BDualChipFlag = (YMZ280Bclock & 0x4000_0000) != 0;
                        if (ymZ280BDualChipFlag) chips.add("YMZ280Bx2");
                        else chips.add("YMZ280B");
                    }
                }

                if (vgmDataOffset > 0x6c) {
                    int RF5C164clock = ByteUtil.readLeInt(vgmBuf, 0x6c);
                    if (RF5C164clock != 0) {
                        rf5C164ClockValue = RF5C164clock & 0x3fff_ffff;
                        rf5C164DualChipFlag = (RF5C164clock & 0x4000_0000) != 0;
                        if (rf5C164DualChipFlag) chips.add("RF5C164x2");
                        else chips.add("RF5C164");
                    }
                }


                if (vgmDataOffset > 0x70) {
                    int PWMclock = ByteUtil.readLeInt(vgmBuf, 0x70);
                    if (PWMclock != 0) {
                        chips.add("PWM");
                        pwmClockValue = PWMclock;
                    }
                }

                if (vgmDataOffset > 0x74) {
                    int AY8910clock = ByteUtil.readLeInt(vgmBuf, 0x74);
                    if (AY8910clock != 0) {
                        ay8910ClockValue = AY8910clock & 0x3fff_ffff;
                        ay8910DualChipFlag = (AY8910clock & 0x4000_0000) != 0;
                        if (ay8910DualChipFlag) chips.add("AY8910x2");
                        else chips.add("AY8910");
                    }
                }
            }

//            okiM6258ClockValue = 0;
//            huC6280ClockValue = 0;
//            okiM6295ClockValue = 0;

//            if (version >= 0x0161)
            {
                if (vgmDataOffset > 0x80) {
                    int DMGclock = ByteUtil.readLeInt(vgmBuf, 0x80);
                    if (DMGclock != 0) {
                        dmgClockValue = DMGclock & 0x3fff_ffff;
                        dmgDualChipFlag = (DMGclock & 0x4000_0000) != 0;
                        if (dmgDualChipFlag) chips.add("DMGx2");
                        else chips.add("DMG");
                    }
                }

                if (vgmDataOffset > 0x84) {
                    int NESclock = ByteUtil.readLeInt(vgmBuf, 0x84);
                    if (NESclock != 0) {
                        nesClockValue = NESclock & 0xbfff_ffff;
                        nesDualChipFlag = (NESclock & 0x4000_0000) != 0;
                        if (nesDualChipFlag) chips.add("NES_APUx2");
                        else chips.add("NES_APU");
                    }
                }

                if (vgmDataOffset > 0x88) {
                    int MultiPCMclock = ByteUtil.readLeInt(vgmBuf, 0x88);
                    if (MultiPCMclock != 0) {
                        multiPCMClockValue = MultiPCMclock & 0x3fff_ffff;
                        multiPCMDualChipFlag = (MultiPCMclock & 0x4000_0000) != 0;
                        if (multiPCMDualChipFlag) chips.add("MultiPCMx2");
                        else chips.add("MultiPCM");
                    }
                }

                if (vgmDataOffset > 0x90) {
                    int OKIM6258clock = ByteUtil.readLeInt(vgmBuf, 0x90);
                    if (OKIM6258clock != 0) {
                        chips.add("OKIM6258");
                        okiM6258ClockValue = OKIM6258clock;
                        okiM6258Type = vgmBuf[0x94] & 0xff;
                    }
                }

                if (vgmDataOffset > 0x9c) {
                    int K051649clock = ByteUtil.readLeInt(vgmBuf, 0x9c);
                    if (K051649clock != 0) {
                        k051649ClockValue = K051649clock & 0x3fff_ffff;
                        k051649DualChipFlag = (K051649clock & 0x4000_0000) != 0;
                        if (k051649DualChipFlag) chips.add("K051649x2");
                        else chips.add("K051649Inst");
                    }
                }

                if (vgmDataOffset > 0xa0) {
                    int K054539clock = ByteUtil.readLeInt(vgmBuf, 0xa0);
                    if (K054539clock != 0) {
                        k054539ClockValue = K054539clock & 0x3fff_ffff;
                        k054539DualChipFlag = (K054539clock & 0x4000_0000) != 0;
                        if (k054539DualChipFlag) chips.add("K054539x2");
                        else chips.add("K054539Inst");
                        k054539Flags = vgmBuf[0x95] & 0xff;
                    }
                }

                if (vgmDataOffset > 0xa4) {

                    int HuC6280clock = ByteUtil.readLeInt(vgmBuf, 0xa4);
                    if (HuC6280clock != 0) {
                        chips.add("OotakeHuC6280");
                        huC6280ClockValue = HuC6280clock;
                    }
                }

                if (vgmDataOffset > 0xa8) {

                    int C140clock = ByteUtil.readLeInt(vgmBuf, 0xa8);
                    if (C140clock != 0) {
                        c140ClockValue = C140clock & 0x3fff_ffff;
                        c140DualChipFlag = (C140clock & 0x4000_0000) != 0;
                        if (c140DualChipFlag) chips.add("C140x2");
                        else chips.add("C140Inst");

                        switch (vgmBuf[0x96]) {
                        case 0x00:
                            C140Type = C140.Type.SYSTEM2;
                            break;
                        case 0x01:
                            C140Type = C140.Type.SYSTEM21;
                            break;
                        case 0x02:
                        default:
                            C140Type = C140.Type.ASIC219;
                            break;
                        }
                    }
                }

                if (vgmDataOffset > 0xac) {

                    int k053260clock = ByteUtil.readLeInt(vgmBuf, 0xac);
                    if (k053260clock != 0) {
                        k053260ClockValue = k053260clock & 0x3fff_ffff;
                        k053260DualChipFlag = (k053260clock & 0x4000_0000) != 0;
                        if (k053260DualChipFlag) chips.add("K053260x2");
                        else chips.add("K053260Inst");
                    }
                }

                if (vgmDataOffset > 0xb0) {

                    int pokeyClock = ByteUtil.readLeInt(vgmBuf, 0xb0);
                    if (pokeyClock != 0) {
                        pokeyClockValue = pokeyClock & 0x3fff_ffff;
                        pokeyDualChipFlag = (pokeyClock & 0x4000_0000) != 0;
                        if (pokeyDualChipFlag) chips.add("POKEYx2");
                        else chips.add("POKEY");
                    }
                }

                if (vgmDataOffset > 0xb4) {

                    int qSoundClock = ByteUtil.readLeInt(vgmBuf, 0xb4);
                    if (qSoundClock != 0) {
                        chips.add("QSoundInst");
                        qSoundClockValue = qSoundClock;
                    }
                }

                if (vgmDataOffset > 0x98) {
                    int okiM6295clock = ByteUtil.readLeInt(vgmBuf, 0x98);
                    if (okiM6295clock != 0) {
                        okiM6295DualChipFlag = (okiM6295clock & 0x4000_0000) != 0;
                        if (okiM6295DualChipFlag) {
                            chips.add("OKIM6295x2");
                        } else {
                            chips.add("OKIM6295");
                        }
                        okiM6295ClockValue = okiM6295clock & 0xbfff_ffff;
                    }
                }

            }
            if (version >= 0x0171) {
                if (vgmDataOffset > 0xc0) {

                    int wSwanClock = ByteUtil.readLeInt(vgmBuf, 0xc0);
                    if (wSwanClock != 0) {
                        wSwanClockValue = wSwanClock & 0x3fff_ffff;
                        wSwanDualChipFlag = (wSwanClock & 0x4000_0000) != 0;
                        if (wSwanDualChipFlag) chips.add("WSwanx2");
                        else chips.add("WSwan");
                    }
                }

                if (vgmDataOffset > 0xc8) {

                    int saa1099Clock = ByteUtil.readLeInt(vgmBuf, 0xc8);
                    if (saa1099Clock != 0) {
                        saa1099ClockValue = saa1099Clock & 0x3fff_ffff;
                        saA1099DualChipFlag = (saa1099Clock & 0x4000_0000) != 0;
                        if (saA1099DualChipFlag) chips.add("SAA1099x2");
                        else chips.add("SAA1099");
                    }
                }

                if (vgmDataOffset > 0xd8) {

                    int x1_010Clock = ByteUtil.readLeInt(vgmBuf, 0xd8);
                    if (x1_010Clock != 0) {
                        x1_010ClockValue = x1_010Clock & 0x3fff_ffff;
                        x1_010DualChipFlag = (x1_010Clock & 0x4000_0000) != 0;
                        if (x1_010DualChipFlag) chips.add("X1_010x2");
                        else chips.add("X1_010Inst");
                    }
                }

                if (vgmDataOffset > 0xdc) {

                    int c352clock = ByteUtil.readLeInt(vgmBuf, 0xdc);
                    if (c352clock != 0) {
                        c352ClockValue = c352clock & 0x3fff_ffff;
                        c352DualChipFlag = (c352clock & 0x4000_0000) != 0;
                        if (c352DualChipFlag) chips.add("C352x2");
                        else chips.add("C352Inst");

                        c352ClockDivider = vgmBuf[0xd6] & 0xff;
                    }
                }

                if (vgmDataOffset > 0xe0) {

                    int ga20Clock = ByteUtil.readLeInt(vgmBuf, 0xe0);
                    if (ga20Clock != 0) {
                        ga20DualChipFlag = (ga20Clock & 0x4000_0000) != 0;
                        if (ga20DualChipFlag) {
                            ga20ClockValue = ga20Clock & 0x3fff_ffff;
                            chips.add("GA20x2");
                        } else {
                            ga20ClockValue = ga20Clock & 0xbfff_ffff;
                            chips.add("GA20");
                        }
                    }
                }
            }
        } else {
            vgmDataOffset = 0x40;
        }

        usedChips = String.join(" , ", chips);

        int vgmGd3 = ByteUtil.readLeInt(vgmBuf, 0x14);
        if (vgmGd3 != 0) {
            int vgmGd3Id = ByteUtil.readLeInt(vgmBuf, vgmGd3 + 0x14);
            if (vgmGd3Id != FCC_GD3) return false;
            gd3 = getGD3Info(vgmBuf, vgmGd3);
        }

        return true;
    }

    /**
     * OPNAのRAMTypeをデータから調べる
     * @return true: x8bit, false: x1bit
     */
    private boolean searchOpnaRamType() {
        try {
            int adr = vgmDataOffset;

            while (adr < vgmBuf.length && (vgmBuf[adr] & 0xff) != 0x66) {
                int dat = vgmBuf[adr] & 0xff;
                if (dat < 0x51) adr += 2;
                else if (dat < 0x57) adr += 3;
                else if (dat == 0x57) {
                    int reg = vgmBuf[adr + 1] & 0xff;
                    int val = vgmBuf[adr + 2] & 0xff;
                    adr += 3;
                    if (reg == 1) {
                        if ((val & 2) != 0) {
                            return true;
                        }
                    }
                } else if (dat < 0x62) adr += 3;
                else if (dat < 0x64) adr++;
                else if (dat == 0x64) adr += 4;
                else if (dat == 0x66) adr++;
                else if (dat == 0x67) {
                    int bLen = ByteUtil.readLeInt(vgmBuf, adr + 3);
                    bLen &= 0x7fff_ffff;
                    adr += bLen + 7;
                } else if (dat == 0x68) {
                    adr += 12;
                } else if ((dat & 0xff) < 0x90) adr++;
                else if (dat == 0x90) adr += 5;
                else if (dat == 0x91) adr += 5;
                else if (dat == 0x92) adr += 6;
                else if (dat == 0x93) adr += 11;
                else if (dat == 0x94) adr += 2;
                else if (dat == 0x95) adr += 5;
                else if ((dat & 0xff) < 0xc0) adr += 3;
                else if ((dat & 0xff) < 0xe0) adr += 4;
                else adr += 5;
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return false;
    }

    @Override
    public Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {

        int adr = vgmGd3[0] + 12 + 0x14;
        gd3 = Common.getGD3Info(buf, adr);
        gd3.usedChips = usedChips;

        return gd3;
    }

    static class VgmPcmData {
        public int dataSize;
        public byte[] data;
        public int dataStart;
    }

    static class VgmPcmBank {
        public int bankCount;
        public List<VgmPcmData> bank = new ArrayList<>();
        public int dataSize;
        public byte[] data;
        public int dataPos;
        public int bnkPos;
    }

    static class DacCtrlData {
        public boolean enable;
        public int bank;
    }

    static class PcmBankTbl {
        public int comprType;
        public int cmpSubType;
        public int bitDec;
        public int bitCmp;
        public int entryCount;
        public byte[] entries;
    }

    // TODO musicDriverInterface.GD3Tag -> Tag???
    public static class Gd3 {
        public String trackName = "";
        public String trackNameJ = "";
        public String gameName = "";
        public String gameNameJ = "";
        public String systemName = "";
        public String systemNameJ = "";
        public String composer = "";
        public String composerJ = "";
        public String converted = "";
        public String notes = "";
        public String vgmBy = "";
        public String version = "";
        public String usedChips = "";

        public List<Tuple3<Integer, Integer, String>> lyrics = null;
    }
}
