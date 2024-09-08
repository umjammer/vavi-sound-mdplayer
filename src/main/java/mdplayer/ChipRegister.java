
package mdplayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import mdplayer.Common.EnmChip;
import mdplayer.Common.EnmModel;
import mdplayer.RealChip.RC86ctlSoundChip;
import mdplayer.RealChip.RSoundChip;
import mdplayer.driver.sid.Sid;
import mdsound.Instrument;
import mdsound.chips.C140;
import mdsound.chips.MultiPCM;
import mdsound.chips.OkiM6295;
import mdsound.chips.PPZ8Status;
import mdsound.instrument.*;
import mdsound.np.chip.DeviceInfo;
import mdsound.np.chip.NesApu;
import mdsound.np.chip.NesDmc;
import mdsound.np.chip.NesFds;
import mdsound.np.chip.NesFme7;
import mdsound.np.chip.NesMmc5;
import mdsound.np.chip.NesN106;
import mdsound.np.chip.NesVrc6;
import mdsound.np.chip.NesVrc7;
import mdsound.np.cpu.Km6502;
import mdsound.np.memory.NesBank;
import mdsound.np.memory.NesMem;


// TODO oop
public class ChipRegister {

//    private final VstMng vstMng;

    private final Setting setting = Setting.getInstance();

    private final mdsound.MDSound mds;

    private MidiOutInfo[] midiOutInfos = null;

    private List<Receiver> midiOuts = null;
    private List<Integer> midiOutsType = null;
//    private List<Integer> vstMidiOutsType = null;
//    private NX68Sound.X68Sound x68Sound = null;
//    private NX68Sound.sound_iocs sound_iocs = null;

    private Map<Class<? extends Instrument>, mdsound.MDSound.Chip> dicChipsInfo = new HashMap<>();

    private Setting.ChipType2[] ctSN76489;
    private Setting.ChipType2[] ctYM2612;
    private Setting.ChipType2[] ctYM2608;
    private Setting.ChipType2[] ctYM2151;
    private Setting.ChipType2[] ctYM2203;
    private Setting.ChipType2[] ctYM2610;
    private Setting.ChipType2[] ctYM3526;
    private Setting.ChipType2[] ctYM3812;
    private Setting.ChipType2[] ctYMF262;
    private Setting.ChipType2[] ctYMF271;
    private Setting.ChipType2[] ctYMF278B;
    private Setting.ChipType2[] ctYMZ280B;
    private Setting.ChipType2[] ctAY8910;
    private Setting.ChipType2[] ctK051649;
    private Setting.ChipType2[] ctYM2413;
    private Setting.ChipType2[] ctHuC6280;
    private Setting.ChipType2[] ctY8950;
    private Setting.ChipType2[] ctSEGAPCM;
    private Setting.ChipType2[] ctC140;
    private RealChip realChip;
    private RSoundChip[] scSN76489 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2612 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2608 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2151 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2203 = new RSoundChip[] {null, null};
    private RSoundChip[] scAY8910 = new RSoundChip[] {null, null};
    private RSoundChip[] scK051649 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2413 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2610 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2610EA = new RSoundChip[] {null, null};
    private RSoundChip[] scYM2610EB = new RSoundChip[] {null, null};
    private RSoundChip[] scYM3526 = new RSoundChip[] {null, null};
    private RSoundChip[] scYM3812 = new RSoundChip[] {null, null};
    private RSoundChip[] scYMF262 = new RSoundChip[] {null, null};
    private RSoundChip[] scYMF271 = new RSoundChip[] {null, null};
    private RSoundChip[] scYMF278B = new RSoundChip[] {null, null};
    private RSoundChip[] scYMZ280B = new RSoundChip[] {null, null};
    private RSoundChip[] scSEGAPCM = new RSoundChip[] {null, null};
    private RSoundChip[] scC140 = new RSoundChip[] {null, null};

    private static final byte[] algM = new byte[] {0x08, 0x08, 0x08, 0x08, 0x0c, 0x0e, 0x0e, 0x0f};

    private static final int[] opN = new int[] {0, 2, 1, 3};

    public Integer[] getSIDRegister(int chipId) {
        if (SID == null)
            return null;
        return SID.GetRegisterFromSid()[chipId];
    }

    private static final int[] noteTbl = new int[] {2, 4, 5, -1, 6, 8, 9, -1, 10, 12, 13, -1, 14, 0, 1, -1};

    private static final int[] noteTbl2 = new int[] {13, 14, 0, -1, 1, 2, 4, -1, 5, 6, 8, -1, 9, 10, 12, -1};

    private int nsfAPUmask = 0;
    private int nsfDMCmask = 0;
    private int nsfFDSmask = 0;
    private int nsfMMC5mask = 0;
    private int nsfVRC6mask = 0;
    private int nsfVRC7mask = 0;
    private int nsfN163mask = 0;

    public ChipLEDs chipLED = new ChipLEDs();

    public int[][] fmRegisterYM2151 = new int[][] {null, null};
    public int[][] fmKeyOnYM2151 = new int[][] {null, null};
    public int[][] fmVolYM2151 = new int[][] {
            new int[] {0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0}
    };

    private int[] nowYM2151FadeoutVol = new int[] {0, 0};
    private boolean[][] maskFMChYM2151 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false}
    };
    public int[] fmAMDYM2151 = new int[] {-1, -1};
    public int[] fmPMDYM2151 = new int[] {-1, -1};

    public int[][] fmRegisterYM2203 = new int[][] {null, null};
    public int[][] fmKeyOnYM2203 = new int[][] {null, null};
    public int[][] fmCh3SlotVolYM2203 = new int[][] {new int[4], new int[4]};
    private int[] nowYM2203FadeoutVol = new int[] {0, 0};
    public int[][] fmVolYM2203 = new int[][] {new int[9], new int[9]};
    private boolean[][] maskFMChYM2203 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false, false}
    };

    public int[][] fmRegisterYM2413 = new int[][] {null, null};
//    private int[] fmRegisterYM2413RyhthmB = new int[] {0, 0};
//    private int[] fmRegisterYM2413Ryhthm = new int[] {0, 0};
    private ChipKeyInfo[] kiYM2413 = new ChipKeyInfo[] {new ChipKeyInfo(14), new ChipKeyInfo(14)};
    private ChipKeyInfo[] kiYM2413ret = new ChipKeyInfo[] {new ChipKeyInfo(14), new ChipKeyInfo(14)};
    private int[] nowYM2413FadeoutVol = new int[] {0, 0};
    private boolean[] rmYM2413 = new boolean[] {false, false};
    private boolean[][] maskFMChYM2413 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    public int[][][] fmRegisterYM2612 = new int[][][] {
            new int[][] {null, null},
            new int[][] {null, null}
    };
    public int[][] fmKeyOnYM2612 = new int[][] {null, null};
    public int[][] fmVolYM2612 = new int[][] {
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };
    public int[][] fmCh3SlotVolYM2612 = new int[][] {new int[4], new int[4]};
    private int[] nowYM2612FadeoutVol = new int[] {0, 0};
    private boolean[][] maskFMChYM2612 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false}
    };

    public int[][][] fmRegisterYM2608 = new int[][][] {
            new int[][] {null, null},
            new int[][] {null, null}
    };

    public int[][] fmKeyOnYM2608 = new int[][] {null, null};

    public int[][] fmVolYM2608 = new int[][] {
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public int[][] fmCh3SlotVolYM2608 = new int[][] {
            new int[4], new int[4]
    };

    public int[][][] fmVolYM2608Rhythm = new int[][][] {
            new int[][] {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]},
            new int[][] {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}
    };

    public int[][] fmVolYM2608Adpcm = new int[][] {new int[2], new int[2]};

    public int[] fmVolYM2608AdpcmPan = new int[] {0, 0};

    private int[] nowYM2608FadeoutVol = new int[] {0, 0};

    private boolean[][] maskFMChYM2608 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    public int[][][] fmRegisterYM2610 = new int[][][] {
            new int[][] {null, null},
            new int[][] {null, null}
    };

    public int[][] fmKeyOnYM2610 = new int[][] {null, null};

    public int[][] fmVolYM2610 = new int[][] {
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0},
            new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    public int[][] fmCh3SlotVolYM2610 = new int[][] {new int[4], new int[4]};

    public int[][][] fmVolYM2610Rhythm = new int[][][] {
            new int[][] {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]},
            new int[][] {new int[2], new int[2], new int[2], new int[2], new int[2], new int[2]}
    };

    public int[][] fmVolYM2610Adpcm = new int[][] {new int[2], new int[2]};

    public int[] fmVolYM2610AdpcmPan = new int[] {0, 0};

    private int[] nowYM2610FadeoutVol = new int[] {0, 0};

    private boolean[][] maskFMChYM2610 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    public int[][] fmRegisterYM3526 = new int[][] {null, null};

    private int[] nowYM3526FadeoutVol = new int[] {0, 0};

    private ChipKeyInfo[] kiYM3526 = new ChipKeyInfo[] {
            new ChipKeyInfo(14), new ChipKeyInfo(14)
    };

    private ChipKeyInfo[] kiYM3526ret = new ChipKeyInfo[] {
            new ChipKeyInfo(14), new ChipKeyInfo(14)
    };

    private boolean[][] maskFMChYM3526 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    public int[][] fmRegisterYM3812 = new int[][] {null, null};

    private int[] nowYM3812FadeoutVol = new int[] {0, 0};

    private ChipKeyInfo[] kiYM3812 = new ChipKeyInfo[] {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private ChipKeyInfo[] kiYM3812ret = new ChipKeyInfo[] {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private boolean[][] maskFMChYM3812 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    private ChipKeyInfo[] kiVRC7 = new ChipKeyInfo[] {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    private ChipKeyInfo[] kiVRC7ret = new ChipKeyInfo[] {new ChipKeyInfo(14), new ChipKeyInfo(14)};

    public int[][][] fmRegisterYMF262 = new int[][][] {
            new int[][] {null, null},
            new int[][] {null, null}
    };

    private int[] fmRegisterYMF262FM = new int[] {0, 0};

    private int[] fmRegisterYMF262RyhthmB = new int[] {0, 0};

    private int[] fmRegisterYMF262Ryhthm = new int[] {0, 0};

    private boolean[][] maskFMChYMF262 = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false
            }
    };

    private int[] nowYMF262FadeoutVol = new int[] {0, 0};

    public int[][][] fmRegisterYMF271 = new int[][][] {
            new int[][] {null, null},
            new int[][] {null, null}
    };

    public int[][][] fmRegisterYMF278B = new int[][][] {
            new int[][] {null, null},
            new int[][] {null, null}
    };

    private int[] fmRegisterYMF278BFM = new int[] {0, 0};

    private int[][] fmRegisterYMF278BPCM = new int[][] {new int[24], new int[24]};

    private int[] fmRegisterYMF278BRyhthmB = new int[] {0, 0};

    private int[] fmRegisterYMF278BRyhthm = new int[] {
            0, 0
    };

    private boolean[][] maskFMChYMF278B = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            }
    };

    private byte[] YMF278BCh = new byte[] {
            0, 3, 1, 4, 2, 5, 6, 7, 8, 9, 12, 10, 13, 11, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46
    };

    public int[][] YMZ280BRegister = new int[][] {null, null};

    public int[][] fmRegisterY8950 = new int[][] {null, null};

    private ChipKeyInfo[] kiY8950 = new ChipKeyInfo[] {new ChipKeyInfo(15), new ChipKeyInfo(15)};

    private ChipKeyInfo[] kiY8950ret = new ChipKeyInfo[] {new ChipKeyInfo(15), new ChipKeyInfo(15)};

    private boolean[][] maskFMChY8950 = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            }
    };

    public int[][] sn76489Register = new int[][] {null, null};

    public int[] sn76489RegisterGGPan = new int[] {0xff, 0xff};

    public int[][][] sn76489Vol = new int[][][] {
            new int[][] {new int[2], new int[2], new int[2], new int[2]},
            new int[][] {new int[2], new int[2], new int[2], new int[2]}
    };

    public int[] nowSN76489FadeoutVol = new int[] {0, 0};

    public boolean[][] maskChSN76489 = new boolean[][] {
            new boolean[] {false, false, false, false},
            new boolean[] {false, false, false, false}
    };

    public int[][] psgRegisterAY8910 = new int[][] {null, null};

    public int[][] psgKeyOnAY8910 = new int[][] {null, null};

    private int[] nowAY8910FadeoutVol = new int[] {0, 0};

    public int[][] psgVolAY8910 = new int[][] {new int[3], new int[3]};

    private boolean[][] maskPSGChAY8910 = new boolean[][] {
            new boolean[] {false, false, false},
            new boolean[] {false, false, false}
    };

    private boolean[] maskOKIM6258 = new boolean[] {false, false};

    public boolean[] okim6258Keyon = new boolean[] {false, false};

    private boolean[][] maskOKIM6295 = new boolean[][] {
            new boolean[] {false, false, false, false},
            new boolean[] {false, false, false, false}
    };

    public byte[][] pcmRegisterC140 = new byte[][] {null, null};

    public boolean[][] pcmKeyOnC140 = new boolean[][] {null, null};

    private boolean[][] maskChC140 = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false
            }
    };

    private boolean[][] maskChPPZ8 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false}
    };

    public int[][] pcmRegisterC352 = new int[][] {null, null};

    public int[][] pcmKeyOnC352 = new int[][] {null, null};

    private boolean[][] maskChC352 = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            }
    };

    private boolean[][] maskChRF5C164 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false}
    };

    private boolean[][] maskChRF5C68 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false, false, false}
    };

    private boolean[][] maskChHuC6280 = new boolean[][] {
            new boolean[] {false, false, false, false, false, false},
            new boolean[] {false, false, false, false, false, false}
    };

    private boolean[][] maskChSegaPCM = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            }
    };

    private boolean[][] maskChQSound = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,
            },
            new boolean[] {
                    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false,
            }
    };

    public K051649Inst scc_k051649 = new K051649Inst();

    private int sccR_port;

    private int sccR_offset;

    private int sccR_dat;

    public byte[] K051649tKeyOnOff = new byte[] {
            0, 0
    };

    public boolean[][] maskChK051649 = new boolean[][] {
            new boolean[] {
                    false, false, false, false, false
            }, new boolean[] {
            false, false, false, false, false
    }
    };

    public byte[][] pcmRegisterSEGAPCM = new byte[][] {
            null, null
    };

    public boolean[][] pcmKeyOnSEGAPCM = new boolean[][] {
            null, null
    };

    public MIDIParam[] midiParams = new MIDIParam[] {null, null};

    public boolean[][] maskChDMG = new boolean[][] {
            new boolean[] {false, false, false, false},
            new boolean[] {false, false, false, false}
    };

    public NesBank nes_bank = null;
    public NesMem nes_mem = null;
    public Km6502 nes_cpu = null;
    public NesApu nes_apu = null;
    public NesDmc nes_dmc = null;
    public NesFds nes_fds = null;
    public NesN106 nes_n106 = null;
    public NesVrc6 nes_vrc6 = null;
    public NesMmc5 nes_mmc5 = null;
    public NesFme7 nes_fme7 = null;
    public NesVrc7 nes_vrc7 = null;

    private int[] LatchedRegister = new int[] {
            0, 0
    };

    private int[] NoiseFreq = new int[] {
            0, 0
    };

    private int volF = 1;

    private MIDIExport midiExport;

    public ChipRegister(mdsound.MDSound mds
//            ,
//                        RealChip nScci,
//                        VstMng vstMng,
//                        RSoundChip[] scYM2612,
//                        RSoundChip[] scSN76489,
//                        RSoundChip[] scYM2608,
//                        RSoundChip[] scYM2151,
//                        RSoundChip[] scYM2203,
//                        RSoundChip[] scYM2413,
//                        RSoundChip[] scYM2610,
//                        RSoundChip[] scYM2610EA,
//                        RSoundChip[] scYM2610EB,
//                        RSoundChip[] scYM3526,
//                        RSoundChip[] scYM3812,
//                        RSoundChip[] scYMF262,
//                        RSoundChip[] scC140,
//                        RSoundChip[] scSEGAPCM,
//                        RSoundChip[] scAY8910,
//                        RSoundChip[] scK051649
    ) {
        this.mds = mds;
//        this.vstMng = vstMng;
//        this.vstMng.midiParams = midiParams;
//        this.realChip = nScci;
//        this.scYM2612 = scYM2612;
//        this.scYM2608 = scYM2608;
//        this.scYM2151 = scYM2151;
//        this.scSN76489 = scSN76489;
//        this.scYM2203 = scYM2203;
//        this.scYM2413 = scYM2413;
//        this.scYM3526 = scYM3526;
//        this.scYM3812 = scYM3812;
//        this.scYMF262 = scYMF262;
//        this.scYM2610 = scYM2610;
//        this.scYM2610EA = scYM2610EA;
//        this.scYM2610EB = scYM2610EB;
//        this.scC140 = scC140;
//        this.scSEGAPCM = scSEGAPCM;
//        this.scAY8910 = scAY8910;
//        this.scK051649 = scK051649;

        this.ctYM2612 = new Setting.ChipType2[] {
                setting.getYM2612Type()[0], setting.getYM2612Type()[1]
        };
        this.ctSN76489 = new Setting.ChipType2[] {
                setting.getSN76489Type()[0], setting.getSN76489Type()[1]
        };
        this.ctYM2608 = new Setting.ChipType2[] {
                setting.getYM2608Type()[0], setting.getYM2608Type()[1]
        };
        this.ctYM2151 = new Setting.ChipType2[] {
                setting.getYM2151Type()[0], setting.getYM2151Type()[1]
        };
        this.ctYM2203 = new Setting.ChipType2[] {
                setting.getYM2203Type()[0], setting.getYM2203Type()[1]
        };
        this.ctAY8910 = new Setting.ChipType2[] {
                setting.getAY8910Type()[0], setting.getAY8910Type()[1]
        };
        this.ctK051649 = new Setting.ChipType2[] {
                setting.getK051649Type()[0], setting.getK051649Type()[1]
        };
        this.ctYM2413 = new Setting.ChipType2[] {
                setting.getYM2413Type()[0], setting.getYM2413Type()[1]
        };
        this.ctYM2610 = new Setting.ChipType2[] {
                setting.getYM2610Type()[0], setting.getYM2610Type()[1]
        };
        this.ctYMF262 = new Setting.ChipType2[] {
                setting.getYMF262Type()[0], setting.getYMF262Type()[1]
        };
        this.ctYMF271 = new Setting.ChipType2[] {
                setting.getYMF271Type()[0], setting.getYMF271Type()[1]
        };
        this.ctYMF278B = new Setting.ChipType2[] {
                setting.getYMF278BType()[0], setting.getYMF278BType()[1]
        };
        this.ctYMZ280B = new Setting.ChipType2[] {
                setting.getYMZ280BType()[0], setting.getYMZ280BType()[1]
        };
        this.ctHuC6280 = new Setting.ChipType2[] {
                setting.getHuC6280Type()[0], setting.getHuC6280Type()[1]
        };
        this.ctYM3526 = new Setting.ChipType2[] {
                setting.getYM3526Type()[0], setting.getYM3526Type()[1]
        };
        this.ctYM3812 = new Setting.ChipType2[] {
                setting.getYM3812Type()[0], setting.getYM3812Type()[1]
        };
        this.ctY8950 = new Setting.ChipType2[] {
                setting.getY8950Type()[0], setting.getY8950Type()[1]
        };
        this.ctC140 = new Setting.ChipType2[] {
                setting.getC140Type()[0], setting.getC140Type()[1]
        };
        this.ctSEGAPCM = new Setting.ChipType2[] {
                setting.getSEGAPCMType()[0], setting.getSEGAPCMType()[1]
        };

        initChipRegister(null);

        // x68Sound = new NX68Sound.X68Sound();
        // sound_iocs = new NX68Sound.sound_iocs(x68Sound);

        midiExport = new MIDIExport();
        midiExport.fmRegisterYM2612 = fmRegisterYM2612;
        midiExport.fmRegisterYM2151 = fmRegisterYM2151;

        scc_k051649.start((byte) 0, 100, 200);
        scc_k051649.start((byte) 1, 100, 200);
    }

    public void initChipRegister(mdsound.MDSound.Chip[] chipInfos) {

        dicChipsInfo.clear();
        if (chipInfos != null) {
            for (mdsound.MDSound.Chip c : chipInfos) {
                if (!dicChipsInfo.containsKey(c.instrument.getClass())) {
                    dicChipsInfo.put(c.instrument.getClass(), c);
                }
            }
        }

        for (int chipId = 0; chipId < 2; chipId++) {

            fmRegisterYM2612[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2612[chipId][0][i] = 0; // -1;
                fmRegisterYM2612[chipId][1][i] = 0; // -1;
            }
            fmRegisterYM2612[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2612[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2612[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2612[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fmRegisterYM2608[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2608[chipId][0][i] = 0; // -1;
                fmRegisterYM2608[chipId][1][i] = 0; // -1;
            }
            fmRegisterYM2608[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2608[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2608[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2608[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fmRegisterYM2610[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2610[chipId][0][i] = 0; // -1;
                fmRegisterYM2610[chipId][1][i] = 0; // -1;
            }
            fmRegisterYM2610[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2610[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2610[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2610[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fmRegisterYM3526[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM3526[chipId][i] = 0;
                fmRegisterYM3526[chipId][i] = 0;
            }

            fmRegisterYM3812[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM3812[chipId][i] = 0;
                fmRegisterYM3812[chipId][i] = 0;
            }

            fmRegisterYMF262[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF262[chipId][0][i] = 0;
                fmRegisterYMF262[chipId][1][i] = 0;
            }

            fmRegisterYMF271[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF271[chipId][0][i] = 0;
                fmRegisterYMF271[chipId][1][i] = 0;
                fmRegisterYMF271[chipId][2][i] = 0;
                fmRegisterYMF271[chipId][3][i] = 0;
                fmRegisterYMF271[chipId][4][i] = 0;
                fmRegisterYMF271[chipId][5][i] = 0;
                fmRegisterYMF271[chipId][6][i] = 0;
            }

            fmRegisterYMF278B[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF278B[chipId][0][i] = 0;
                fmRegisterYMF278B[chipId][1][i] = 0;
                fmRegisterYMF278B[chipId][2][i] = 0;
            }
            fmRegisterYMF278BRyhthm[0] = 0;
            fmRegisterYMF278BRyhthm[1] = 0;
            fmRegisterYMF278BRyhthmB[0] = 0;
            fmRegisterYMF278BRyhthmB[1] = 0;

            fmRegisterY8950[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterY8950[chipId][i] = 0;
            }

            YMZ280BRegister[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                YMZ280BRegister[chipId][i] = 0;
            }

            fmRegisterYM2151[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2151[chipId][i] = 0;
            }
            fmKeyOnYM2151[chipId] = new int[] {0, 0, 0, 0, 0, 0, 0, 0};

            fmRegisterYM2203[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2203[chipId][i] = 0; // -1;
            }
            fmKeyOnYM2203[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            sn76489Register[chipId] = new int[] {0, 15, 0, 15, 0, 15, 0, 15};

            fmRegisterYM2413[chipId] = new int[0x39];
            for (int i = 0; i < 0x39; i++) {
                fmRegisterYM2413[chipId][i] = 0;
            }
            //fmRegisterYM2413Ryhthm[0] = 0;
            //fmRegisterYM2413Ryhthm[1] = 0;
            //fmRegisterYM2413RyhthmB[0] = 0;
            //fmRegisterYM2413RyhthmB[1] = 0;

            psgRegisterAY8910[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                psgRegisterAY8910[chipId][i] = 0;
            }
            psgKeyOnAY8910[chipId] = new int[] {0, 0, 0};

            pcmRegisterC140[chipId] = new byte[0x200];
            pcmKeyOnC140[chipId] = new boolean[24];

            pcmRegisterC352[chipId] = new int[0x203];
            pcmKeyOnC352[chipId] = new int[32];

            pcmRegisterSEGAPCM[chipId] = new byte[0x200];
            pcmKeyOnSEGAPCM[chipId] = new boolean[16];

            midiParams[chipId] = new MIDIParam();

            nowAY8910FadeoutVol[chipId] = 0;
            nowSN76489FadeoutVol[chipId] = 0;
            nowYM2151FadeoutVol[chipId] = 0;
            nowYM2203FadeoutVol[chipId] = 0;
            nowYM2413FadeoutVol[chipId] = 0;
            nowYM2608FadeoutVol[chipId] = 0;
            nowYM2610FadeoutVol[chipId] = 0;
            nowYM2612FadeoutVol[chipId] = 0;
            nowYM3526FadeoutVol[chipId] = 0;
            nowYM3812FadeoutVol[chipId] = 0;
            nowYMF262FadeoutVol[chipId] = 0;

        }

        nes_bank = null;
        nes_mem = null;
        nes_cpu = null;
        nes_apu = null;
        nes_dmc = null;
        nes_fds = null;
        nes_n106 = null;
        nes_vrc6 = null;
        nes_mmc5 = null;
        nes_fme7 = null;
        nes_vrc7 = null;
    }

    public void initChipRegisterNSF(mdsound.MDSound.Chip[] chipInfos) {

        dicChipsInfo.clear();
        if (chipInfos != null) {
            for (mdsound.MDSound.Chip c : chipInfos) {
                dicChipsInfo.put(c.instrument.getClass(), c);
            }
        }

        for (int chipId = 0; chipId < 2; chipId++) {

            fmRegisterYM2612[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2612[chipId][0][i] = 0;
                fmRegisterYM2612[chipId][1][i] = 0;
            }
            fmRegisterYM2612[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2612[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2612[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2612[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2612[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fmRegisterYM2608[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2608[chipId][0][i] = 0;
                fmRegisterYM2608[chipId][1][i] = 0;
            }
            fmRegisterYM2608[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2608[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2608[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2608[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2608[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fmRegisterYM2610[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2610[chipId][0][i] = 0;
                fmRegisterYM2610[chipId][1][i] = 0;
            }
            fmRegisterYM2610[chipId][0][0xb4] = 0xc0;
            fmRegisterYM2610[chipId][0][0xb5] = 0xc0;
            fmRegisterYM2610[chipId][0][0xb6] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb4] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb5] = 0xc0;
            fmRegisterYM2610[chipId][1][0xb6] = 0xc0;
            fmKeyOnYM2610[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            fmRegisterYM3526[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM3526[chipId][i] = 0;
                fmRegisterYM3526[chipId][i] = 0;
            }

            fmRegisterYM3812[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM3812[chipId][i] = 0;
                fmRegisterYM3812[chipId][i] = 0;
            }

            fmRegisterYMF262[chipId] = new int[][] {new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF262[chipId][0][i] = 0;
                fmRegisterYMF262[chipId][1][i] = 0;
            }

            fmRegisterYMF271[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF271[chipId][0][i] = 0;
                fmRegisterYMF271[chipId][1][i] = 0;
                fmRegisterYMF271[chipId][2][i] = 0;
                fmRegisterYMF271[chipId][3][i] = 0;
                fmRegisterYMF271[chipId][4][i] = 0;
                fmRegisterYMF271[chipId][5][i] = 0;
                fmRegisterYMF271[chipId][6][i] = 0;
            }

            fmRegisterYMF278B[chipId] = new int[][] {new int[0x100], new int[0x100], new int[0x100]};
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYMF278B[chipId][0][i] = 0;
                fmRegisterYMF278B[chipId][1][i] = 0;
                fmRegisterYMF278B[chipId][2][i] = 0;
            }
            fmRegisterYMF278BRyhthm[0] = 0;
            fmRegisterYMF278BRyhthm[1] = 0;
            fmRegisterYMF278BRyhthmB[0] = 0;
            fmRegisterYMF278BRyhthmB[1] = 0;

            fmRegisterY8950[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterY8950[chipId][i] = 0;
            }

            YMZ280BRegister[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                YMZ280BRegister[chipId][i] = 0;
            }

            fmRegisterYM2151[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2151[chipId][i] = 0;
            }
            fmKeyOnYM2151[chipId] = new int[] {0, 0, 0, 0, 0, 0, 0, 0};

            fmRegisterYM2203[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                fmRegisterYM2203[chipId][i] = 0;
            }
            fmKeyOnYM2203[chipId] = new int[] {0, 0, 0, 0, 0, 0};

            sn76489Register[chipId] = new int[] {0, 15, 0, 15, 0, 15, 0, 15};

            fmRegisterYM2413[chipId] = new int[0x39];
            for (int i = 0; i < 0x39; i++) {
                fmRegisterYM2413[chipId][i] = 0;
            }
            //fmRegisterYM2413Ryhthm[0] = 0;
            //fmRegisterYM2413Ryhthm[1] = 0;
            //fmRegisterYM2413RyhthmB[0] = 0;
            //fmRegisterYM2413RyhthmB[1] = 0;

            psgRegisterAY8910[chipId] = new int[0x100];
            for (int i = 0; i < 0x100; i++) {
                psgRegisterAY8910[chipId][i] = 0;
            }
            psgKeyOnAY8910[chipId] = new int[] {0, 0, 0};

            pcmRegisterC140[chipId] = new byte[0x200];
            pcmKeyOnC140[chipId] = new boolean[24];

            pcmRegisterC352[chipId] = new int[0x203];
            pcmKeyOnC352[chipId] = new int[32];

            pcmRegisterSEGAPCM[chipId] = new byte[0x200];
            pcmKeyOnSEGAPCM[chipId] = new boolean[16];

            midiParams[chipId] = new MIDIParam();

            nowAY8910FadeoutVol[chipId] = 0;
            nowSN76489FadeoutVol[chipId] = 0;
            nowYM2151FadeoutVol[chipId] = 0;
            nowYM2203FadeoutVol[chipId] = 0;
            nowYM2413FadeoutVol[chipId] = 0;
            nowYM2608FadeoutVol[chipId] = 0;
            nowYM2610FadeoutVol[chipId] = 0;
            nowYM2612FadeoutVol[chipId] = 0;
            nowYM3526FadeoutVol[chipId] = 0;
            nowYM3812FadeoutVol[chipId] = 0;
            nowYMF262FadeoutVol[chipId] = 0;
        }
    }

    public mdsound.MDSound.Chip getChipInfo(Class<? extends Instrument> typ) {
        if (dicChipsInfo.containsKey(typ))
            return dicChipsInfo.get(typ);
        return null;
    }

    public void close() {
        midiExport.close();
    }

    public void resetChips() {
        for (int chipId = 0; chipId < 2; chipId++) {
            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    setYM2612Register(chipId, p, 0x40 + c, 127, EnmModel.RealModel, -1);
                    setYM2612Register(chipId, p, 0x44 + c, 127, EnmModel.RealModel, -1);
                    setYM2612Register(chipId, p, 0x48 + c, 127, EnmModel.RealModel, -1);
                    setYM2612Register(chipId, p, 0x4c + c, 127, EnmModel.RealModel, -1);
                }
            }

            for (int c = 0; c < 4; c++) {
                setSN76489Register(chipId, 0x90 + (c << 5) + 0xf, EnmModel.RealModel);
            }

            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    setYM2608Register(chipId, p, 0x40 + c, 127, EnmModel.RealModel);
                    setYM2608Register(chipId, p, 0x44 + c, 127, EnmModel.RealModel);
                    setYM2608Register(chipId, p, 0x48 + c, 127, EnmModel.RealModel);
                    setYM2608Register(chipId, p, 0x4c + c, 127, EnmModel.RealModel);
                }
            }

            // ssg
            setYM2608Register(chipId, 0, 0x08, 0, EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x09, 0, EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x0a, 0, EnmModel.RealModel);

            // rhythm
            setYM2608Register(chipId, 0, 0x11, 0, EnmModel.RealModel);

            // adpcm
            setYM2608Register(chipId, 1, 0x0b, 0, EnmModel.RealModel);

            for (int p = 0; p < 2; p++) {
                for (int c = 0; c < 3; c++) {
                    setYM2610Register(chipId, p, 0x40 + c, 127, EnmModel.RealModel);
                    setYM2610Register(chipId, p, 0x44 + c, 127, EnmModel.RealModel);
                    setYM2610Register(chipId, p, 0x48 + c, 127, EnmModel.RealModel);
                    setYM2610Register(chipId, p, 0x4c + c, 127, EnmModel.RealModel);
                }
            }

            // ssg
            setYM2610Register(chipId, 0, 0x08, 0, EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x09, 0, EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x0a, 0, EnmModel.RealModel);

            // rhythm
            setYM2610Register(chipId, 0, 0x11, 0, EnmModel.RealModel);

            // adpcm
            setYM2610Register(chipId, 1, 0x0b, 0, EnmModel.RealModel);

            for (int c = 0; c < 8; c++) {
                setYM2151Register(chipId, 0, 0x60 + c, 127, EnmModel.RealModel, 0, -1);
                setYM2151Register(chipId, 0, 0x68 + c, 127, EnmModel.RealModel, 0, -1);
                setYM2151Register(chipId, 0, 0x70 + c, 127, EnmModel.RealModel, 0, -1);
                setYM2151Register(chipId, 0, 0x78 + c, 127, EnmModel.RealModel, 0, -1);
            }
        }
    }

//#region midi

    public MidiOutInfo[] getMIDIoutInfo() {
        return midiOutInfos;
    }

    public void setMIDIout(MidiOutInfo[] midiOutInfos, List<Receiver> midiOuts, List<Integer> midiOutsType) {
        this.midiOutInfos = null;
        if (midiOutInfos != null && midiOutInfos.length > 0) {
            this.midiOutInfos = new MidiOutInfo[midiOutInfos.length];
            for (int i = 0; i < midiOutInfos.length; i++) {
                this.midiOutInfos[i] = new MidiOutInfo();
                this.midiOutInfos[i].beforeSendType = midiOutInfos[i].beforeSendType;
                this.midiOutInfos[i].fileName = midiOutInfos[i].fileName;
                this.midiOutInfos[i].id = midiOutInfos[i].id;
                this.midiOutInfos[i].isVST = midiOutInfos[i].isVST;
                this.midiOutInfos[i].manufacturer = midiOutInfos[i].manufacturer;
                this.midiOutInfos[i].name = midiOutInfos[i].name;
                this.midiOutInfos[i].type = midiOutInfos[i].type;
                this.midiOutInfos[i].vendor = midiOutInfos[i].vendor;
            }
        }
        this.midiOuts = midiOuts;
        this.midiOutsType = midiOutsType;
        //VstMng.vstMidiOuts = vstMidiOuts;
        //this.vstMidiOutsType = vstMidiOutsType;

        if (midiParams == null && midiParams.length < 1) return;
//        if (midiOutsType == null && vstMng.vstMidiOutsType == null) return;
//        if (midiOuts == null && vstMng.vstMidiOuts == null) return;

        if (midiOutsType.size() > 0) midiParams[0].MIDIModule = Math.min(midiOutsType.get(0), 2);
        if (midiOutsType.size() > 1) midiParams[1].MIDIModule = Math.min(midiOutsType.get(1), 2);

//        if (vstMng.vstMidiOutsType.size() > 0) {
//            if (midiOutsType.size() < 1 || (midiOutsType.size() > 0 && midiOuts.get(0) == null))
//                midiParams[0].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(0), 2);
//        }
//        if (vstMng.vstMidiOutsType.size() > 1) {
//            if (midiOutsType.size() < 2 || (midiOutsType.size() > 1 && midiOuts.get(1) == null))
//                midiParams[1].MIDIModule = Math.min(vstMng.vstMidiOutsType.get(1), 2);
//        }
    }

    public void setFileName(String fn) {
        midiExport.playingFileName = fn;
    }

    public int getMIDIoutCount() {
        if (midiOuts == null)
            return 0;
        return midiOuts.size();
    }

    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, byte prm2, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (midiOuts == null) return;
            if (num >= midiOuts.size()) return;
            if (midiOuts.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO cmd, prm1, prm2
            midiOuts.get(num).send(mm, -1);
            if (num < midiParams.length) midiParams[num].sendBuffer(new byte[] {cmd, prm1, prm2});
            return;
        }

//        vstMng.sendMIDIout(model, num, cmd, prm1, prm2, deltaFrames);
    }

    public void sendMIDIout(EnmModel model, int num, byte cmd, byte prm1, int deltaFrames /* = 0 */) {
        if (model == EnmModel.RealModel) {
            if (midiOuts == null) return;
            if (num >= midiOuts.size()) return;
            if (midiOuts.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO cmd, prm1
            midiOuts.get(num).send(mm, -1);
            if (num < midiParams.length) midiParams[num].sendBuffer(new byte[] {cmd, prm1});
            return;
        }

//        vstMng.sendMIDIout(model, num, cmd, prm1, deltaFrames);
    }

    public void sendMIDIout(EnmModel model, int num, byte[] data, int deltaFrames/* = 0*/) {
        if (model == EnmModel.RealModel) {
            if (midiOuts == null) return;
            if (num >= midiOuts.size()) return;
            if (midiOuts.get(num) == null) return;

            MidiMessage mm = new ShortMessage(); // TODO
            midiOuts.get(num).send(mm, -1);
            if (num < midiParams.length) midiParams[num].sendBuffer(data);
            return;
        }

//        vstMng.sendMIDIout(model, num, data, deltaFrames);
    }

    public void resetAllMIDIout() {
        if (midiOuts != null) {
            for (Receiver midiOut : midiOuts) {
                if (midiOut == null)
                    continue;
                midiOut.close(); // TODO
            }
        }

//        vstMng.resetAllMIDIout(EnmModel.VirtualModel);
    }

    public void softResetMIDI(int chipId, EnmModel model) {
        resetAllMIDIout();
    }

//#endregion

//#region register

    public void setYM2151Register(int chipId,
                                  int dPort,
                                  int dAddr,
                                  int dData,
                                  EnmModel model,
                                  int hosei,
                                  long vgmFrameCounter) {
        if (ctYM2151 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriOPM", 2);
        else
            chipLED.put("SecOPM", 2);

        if ((model == EnmModel.VirtualModel && (ctYM2151[chipId] == null || !ctYM2151[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scYM2151 != null && scYM2151[chipId] != null))) {
            fmRegisterYM2151[chipId][dAddr] = dData;
            midiExport.outMIDIData(EnmChip.YM2151, chipId, dPort, dAddr, dData, hosei, vgmFrameCounter);
        }

        if ((model == EnmModel.RealModel && ctYM2151[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !ctYM2151[chipId].getUseReal()[0])) {
            if (dAddr == 0x08) { // Key-On/Off
                int ch = dData & 0x7;
                if (ch >= 0 && ch < 8) {
                    if ((dData & 0x78) != 0) {
                        int con = dData & 0x78;
                        fmKeyOnYM2151[chipId][ch] = con | 1;
                        fmVolYM2151[chipId][ch] = 256 * 6;
                    } else {
                        fmKeyOnYM2151[chipId][ch] &= 0xfe;
                    }
                }
            }
        }

        // AMD/PMD
        if (dAddr == 0x19) {
            if ((dData & 0x80) != 0) {
                fmPMDYM2151[chipId] = dData & 0x7f;
            } else {
                fmAMDYM2151[chipId] = dData & 0x7f;
            }
        }

        if ((dAddr & 0xf8) == 0x20) {
            int al = dData & 0x07; // AL
            int ch = (dAddr & 0x7);

            for (int i = 0; i < 4; i++) {
                int slot = (i == 0) ? 0 : ((i == 1) ? 2 : ((i == 2) ? 1 : 3));
                if ((algM[al] & (1 << slot)) > 0) {
                    if (maskFMChYM2151[chipId][ch]) {
                        if (model == EnmModel.VirtualModel) {
                            if (!ctYM2151[chipId].getUseReal()[0]) {
                                if (ctYM2151[chipId].getUseEmu()[0])
                                    mds.write(Ym2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                                if (ctYM2151[chipId].getUseEmu()[1])
                                    mds.write(MameYm2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                                if (ctYM2151[chipId].getUseEmu()[2])
                                    mds.write(X68SoundYm2151Inst.class, chipId, 0, 0x60 + i * 8 + ch, 127);
                            }
                        } else {
                            if (scYM2151 != null && scYM2151[chipId] != null)
                                scYM2151[chipId].setRegister(0x60 + i * 8 + ch, 127);
                        }
                    }
                }
            }
        }

        if ((dAddr & 0xf0) == 0x60 || (dAddr & 0xf0) == 0x70) { // TL
            int ch = (dAddr & 0x7);
            dData &= 0x7f;

            dData = Math.min(dData + nowYM2151FadeoutVol[chipId], 127);
            dData = maskFMChYM2151[chipId][ch] ? 127 : dData;
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2151[chipId].getUseReal()[0]) {
                if (ctYM2151[chipId].getUseEmu()[0])
                    mds.write(Ym2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[1])
                    mds.write(MameYm2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[2])
                    mds.write(X68SoundYm2151Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2151[chipId] == null)
                return;

            if (dAddr >= 0x28 && dAddr <= 0x2f) {
                if (hosei == 0) {
                    scYM2151[chipId].setRegister(dAddr, dData);
                } else {
                    int oct = (dData & 0x70) >> 4;
                    int note = dData & 0xf;
                    note = (note < 3) ? note : ((note < 7) ? (note - 1) : ((note < 11) ? (note - 2) : (note - 3)));
                    note += hosei - 1;
                    if (note < 0) {
                        oct += (note / 12) - 1;
                        note = (note % 12) + 12;
                    } else {
                        oct += (note / 12);
                        note %= 12;
                    }

                    note = (note < 3) ? note : ((note < 6) ? (note + 1) : ((note < 9) ? (note + 2) : (note + 3)));
                    if (scYM2151[chipId] != null)
                        scYM2151[chipId].setRegister(dAddr, (oct << 4) | note);
                }
            } else {
                scYM2151[chipId].setRegister(dAddr, dData);
            }
        }

    }

    private void writeYm2151(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM2151[chipId].getUseReal()[0]) {
                if (ctYM2151[chipId].getUseEmu()[0])
                    mds.write(Ym2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[1])
                    mds.write(MameYm2151Inst.class, chipId, 0, dAddr, dData);
                if (ctYM2151[chipId].getUseEmu()[2])
                    mds.write(X68SoundYm2151Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2151[chipId] != null)
                scYM2151[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM2151(int chipId, EnmModel model) {

        // FM全チャネルキーオフ
        for (int i = 0; i < 8; i++) {
            // note off
            writeYm2151(chipId, 0, 0x08, 0x00 + i, model);
        }

        writeYm2151(chipId, 0, 0x0f, 0x00, model); // FM NOISE ENABLE/NOISE FREQ
        writeYm2151(chipId, 0, 0x18, 0x00, model); // FM HW LFO FREQ
        writeYm2151(chipId, 0, 0x19, 0x80, model); // FM PMD/VALUE
        writeYm2151(chipId, 0, 0x19, 0x00, model); // FM AMD/VALUE
        writeYm2151(chipId, 0, 0x1b, 0x00, model); // FM HW LFO WAVEFORM

        // FM HW LFO RESET
        writeYm2151(chipId, 0, 0x01, 0x02, model);
        writeYm2151(chipId, 0, 0x01, 0x00, model);

        writeYm2151(chipId, 0, 0x10, 0x00, model); // FM Timer-a(H)
        writeYm2151(chipId, 0, 0x11, 0x00, model); // FM Timer-a(L)
        writeYm2151(chipId, 0, 0x12, 0x00, model); // FM Timer-B
        writeYm2151(chipId, 0, 0x14, 0x00, model); // FM Timer Control

        for (int i = 0; i < 8; i++) {
            // FB/ALG/PAN
            writeYm2151(chipId, 0, 0x20 + i, 0x00, model);
            // KC
            writeYm2151(chipId, 0, 0x28 + i, 0x00, model);
            // KF
            writeYm2151(chipId, 0, 0x30 + i, 0x00, model);
            // PMS/AMS
            writeYm2151(chipId, 0, 0x38 + i, 0x00, model);
        }
        for (int i = 0; i < 0x20; i++) {
            // DT1/ML
            writeYm2151(chipId, 0, 0x40 + i, 0x00, model);
            // TL=127
            writeYm2151(chipId, 0, 0x60 + i, 0x7f, model);
            // KS/AR
            writeYm2151(chipId, 0, 0x80 + i, 0x1F, model);
            // AMD/D1R
            writeYm2151(chipId, 0, 0xa0 + i, 0x00, model);
            // DT2/D2R
            writeYm2151(chipId, 0, 0xc0 + i, 0x00, model);
            // D1L/RR
            writeYm2151(chipId, 0, 0xe0 + i, 0x0F, model);
        }
    }

    public void setAY8910Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctAY8910 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriAY10", 2);
        else
            chipLED.put("SecAY10", 2);

        if (model == EnmModel.VirtualModel)
            psgRegisterAY8910[chipId][dAddr] = dData;

        // psg mixer
        if (dAddr == 0x07) {
            int maskData = 0;
            if (maskPSGChAY8910[chipId][0])
                maskData |= 0x9 << 0;
            if (maskPSGChAY8910[chipId][1])
                maskData |= 0x9 << 1;
            if (maskPSGChAY8910[chipId][2])
                maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // psg level
        if ((dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowAY8910FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskPSGChAY8910[chipId][dAddr - 0x08] ? 0 : dData;
        }

        if (model == EnmModel.VirtualModel) {
            if (ctAY8910[chipId].getUseReal()[0])
                return;
            if (ctAY8910[chipId].getUseEmu()[0])
                mds.write(Ay8910Inst.class, chipId, 0, dAddr, dData);
            else if (ctAY8910[chipId].getUseEmu()[1])
                mds.write(MameAy8910Inst.class, chipId, 0, dAddr, dData);
        } else {
            if (scAY8910[chipId] == null)
                return;
            scAY8910[chipId].setRegister(dAddr + 0x000, dData);
        }
    }

    public void setDMGRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriDMG", 2);
        else
            chipLED.put("SecDMG", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            mds.write(DmgInst.class, chipId, 0, dAddr, dData);
            // }
        } else {
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public void setNESRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriNES", 2);
        else
            chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            mds.write(IntFNesInst.class, chipId, 0, dAddr, dData);
            // }
        } else {
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public byte[] getNESRegisterAPU(int chipId, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriNES", 2);
        else
            chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            return mds.ReadNESapu(chipId);
            // }
        } else {
            return null;
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public byte[] getNESRegisterDMC(int chipId, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriNES", 2);
        else
            chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            return mds.ReadNESdmc(chipId);
            // }
        } else {
            return null;
            // if (scNES[chipId] == null) return;

            // scNES[chipId].setRegister(dAddr, dData);
        }
    }

    public mdsound.np.NpNesFds getFDSRegister(int chipId, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriFDS", 2);
        else
            chipLED.put("SecFDS", 2);

        if (model == EnmModel.VirtualModel) {
            // if (!ctNES[chipId].UseScci) {
            return mds.readFDS(chipId);
            // }
        } else {
            return null;
            // if (scFDS[chipId] == null) return;

            // scFDS[chipId].setRegister(dAddr, dData);
        }

    }

    public mdsound.np.chip.NesMmc5 getMMC5Register(int chipId, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriMMC5", 2);
        else
            chipLED.put("SecMMC5", 2);

        if (model == EnmModel.VirtualModel) {
            return null;// mds.readMMC5((byte)chipId);
        } else {
            return null;
        }

    }

    public void setMultiPCMRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriMPCM", 2);
        else
            chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(MultiPcmInst.class, chipId, 0, dAddr, dData);
        } else {
        }
    }

    public MultiPCM getMultiPCMRegister(int chipId) {
        if (chipId == 0)
            chipLED.put("PriMPCM", 2);
        else
            chipLED.put("SecMPCM", 2);

        return mds.ReadMultiPCMRegister(chipId);
    }

    public PPZ8Status.Channel[] getPPZ8Register(int chipId) {
        return mds.readPPZ8Status(chipId);
    }

    public void setMultiPCMSetBank(int chipId, int dCh, int dAddr, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriMPCM", 2);
        else
            chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel) {
            mds.WriteMultiPCMSetBank(chipId, dCh, dAddr);
        } else {
        }
    }

    public void setQSoundRegister(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriQsnd", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(CtrQSoundInst.class, chipId, 0, 0, mm);
            mds.write(CtrQSoundInst.class, chipId, 0, 1, ll);
            mds.write(CtrQSoundInst.class, chipId, 0, 2, rr);

            qSoundRegister[chipId][rr] = mm * 0x100 + ll;
        } else {
        }
    }

    private int[][] qSoundRegister = new int[][] {
            new int[256], new int[256]
    };

    public int[] getQSoundRegister(int chipId) {
        return qSoundRegister[chipId];
    }

    public void setX1_010Register(int chipId, int mm, int ll, int rr, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriX1010", 2);
        else
            chipLED.put("SecX1010", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(X1_010Inst.class, chipId, 0, 0, mm * 0x100 + ll, rr);

        } else {
        }
    }

    public void setGA20Register(int chipId, int adr, int dat, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriGA20", 2);
        else
            chipLED.put("SecGA20", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(Ga20Inst.class, chipId, 0, adr, dat);
        } else {
        }
    }

    public void setYM2413Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctYM2413 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriOPLL", 2);
        else
            chipLED.put("SecOPLL", 2);

        if (model == EnmModel.VirtualModel)
            fmRegisterYM2413[chipId][dAddr] = dData;

        if (dAddr == 0x0e) {
            rmYM2413[chipId] = (dData & 0x20) != 0;
        }

        if (dAddr >= 0x20 && dAddr <= 0x28) {
            int ch = dAddr - 0x20;
            int k = dData & 0x10;
            if (k == 0) {
                kiYM2413[chipId].Off[ch] = true;
            } else {
                if (kiYM2413[chipId].Off[ch])
                    kiYM2413[chipId].On[ch] = true;
                kiYM2413[chipId].Off[ch] = false;
            }

            // mask適用
            if (maskFMChYM2413[chipId][ch])
                dData &= 0xef;
        }

        if (dAddr >= 0x30 && dAddr < 0x39) { // TL
            int inst = dData & 0xf0;
            int tl = dData & 0x0f;
            int ch = dAddr - 0x30;

            if (dAddr < 0x36 || !rmYM2413[chipId]) {
                dData = Math.min(tl + nowYM2413FadeoutVol[chipId], 0x0f);
                dData = inst | dData;
            } else {
                dData = Math.min(tl + nowYM2413FadeoutVol[chipId], 0x0f);
                if (dAddr > 0x36)
                    dData = (dData << 4) | dData;
            }
        }

        if (dAddr == 0x0e) {
            for (int c = 0; c < 5; c++) {
                if ((dData & (0x10 >> c)) == 0) {
                    kiYM2413[chipId].Off[c + 9] = true;
                } else {
                    if (kiYM2413[chipId].Off[c + 9])
                        kiYM2413[chipId].On[c + 9] = true;
                    kiYM2413[chipId].Off[c + 9] = false;
                }
            }

            dData = (dData & 0x20) | (maskFMChYM2413[chipId][9] ? 0 : (dData & 0x10)) |
                    (maskFMChYM2413[chipId][10] ? 0 : (dData & 0x08)) | (maskFMChYM2413[chipId][11] ? 0 : (dData & 0x04)) |
                    (maskFMChYM2413[chipId][12] ? 0 : (dData & 0x02)) | (maskFMChYM2413[chipId][13] ? 0 : (dData & 0x01));
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2413[chipId].getUseReal()[0]) {
                mds.write(Ym2413Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2413[chipId] == null)
                return;
            scYM2413[chipId].setRegister(dAddr, dData);
        }

    }

    public ChipKeyInfo getYM2413KeyInfo(int chipId) {
        for (int ch = 0; ch < kiYM2413[chipId].Off.length; ch++) {
            kiYM2413ret[chipId].Off[ch] = kiYM2413[chipId].Off[ch];
            kiYM2413ret[chipId].On[ch] = kiYM2413[chipId].On[ch];
            kiYM2413[chipId].On[ch] = false;
        }
        return kiYM2413ret[chipId];
    }

    public ChipKeyInfo getY8950KeyInfo(int chipId) {
        for (int ch = 0; ch < kiY8950[chipId].Off.length; ch++) {
            kiY8950ret[chipId].Off[ch] = kiY8950[chipId].Off[ch];
            kiY8950ret[chipId].On[ch] = kiY8950[chipId].On[ch];
            kiY8950[chipId].On[ch] = false;
        }
        return kiY8950ret[chipId];
    }

    public ChipKeyInfo getVRC7KeyInfo(int chipId) {
        if (nes_vrc7 == null)
            return null;
        if (chipId != 0)
            return null;

        mdsound.np.chip.NesVrc7.ChipKeyInfo ki = nes_vrc7.getKeyInfo(chipId);

        for (int ch = 0; ch < 6; ch++) {
            kiVRC7ret[chipId].On[ch] = ki.on[ch];
            kiVRC7ret[chipId].Off[ch] = ki.off[ch];
        }
        return kiVRC7ret[chipId];
    }

    //public int getYM2413RyhthmKeyON(int chipId) {
    // int r = fmRegisterYM2413Ryhthm[chipId];
    // fmRegisterYM2413Ryhthm[chipId] = 0;
    // return r;
    //}

    public int getYMF278BRyhthmKeyON(int chipId) {
        return fmRegisterYMF278BRyhthm[chipId];
    }

    public void resetYMF278BRyhthmKeyON(int chipId) {
        fmRegisterYMF278BRyhthm[chipId] = 0;
    }

    public int[] getYMF278BPCMKeyON(int chipId) {
        return fmRegisterYMF278BPCM[chipId];
    }

    public void resetYMF278BPCMKeyON(int chipId) {
        for (int i = 0; i < 24; i++)
            fmRegisterYMF278BPCM[chipId][i] = 0;
    }

    public int getYMF278BFMKeyON(int chipId) {
        return fmRegisterYMF278BFM[chipId];
    }

    public void resetYMF278BFMKeyON(int chipId) {
        fmRegisterYMF278BFM[chipId] = 0;
    }

    public void setHuC6280Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctHuC6280 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriHuC", 2);
        else
            chipLED.put("SecHuC", 2);

        if (model == EnmModel.VirtualModel) {
            if (!ctHuC6280[chipId].getUseReal()[0]) {
                if (dAddr == 0) {
                    HuC6280CurrentCh[chipId] = dData & 7;
                }
                if (dAddr == 4) {
                    dData = maskChHuC6280[chipId][HuC6280CurrentCh[chipId]] ? 0 : dData;
                }
                // Debug.printf(("chipId:%d adr:%d Dat:%d",
                // chipId, dAddr, dData);
                mds.write(HuC6280Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            // if (scHuC6280[chipId] == null) return;
        }
    }

    public int readHuC6280Register(int chipId, int adr, EnmModel model) {
        if (ctHuC6280 == null)
            return 0;

        if (chipId == 0)
            chipLED.put("PriHuC", 2);
        else
            chipLED.put("SecHuC", 2);

        if (model == EnmModel.VirtualModel) {
            return mds.readOotakePsg(chipId, adr);
        }

        return 0;
    }

    public void setYM2203Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctYM2203 == null)
            return;
        if (dAddr < 0 || dData < 0)
            return;

        if (chipId == 0)
            chipLED.put("PriOPN", 2);
        else
            chipLED.put("SecOPN", 2);

        if (model == EnmModel.VirtualModel) {
            if (dAddr != 0x2d && dAddr != 0x2e && dAddr != 0x2f) {
                fmRegisterYM2203[chipId][dAddr] = dData;
            } else {
                fmRegisterYM2203[chipId][0x2d] = dAddr - 0x2d;
            }
        }

        if ((model == EnmModel.RealModel && ctYM2203[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !ctYM2203[chipId].getUseReal()[0])) {
            if (dAddr == 0x28) {
                int ch = dData & 0x3;
                if (ch >= 0 && ch < 3) {
                    if (ch != 2 || (fmRegisterYM2203[chipId][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            fmKeyOnYM2203[chipId][ch] = (dData & 0xf0) | 1;
                            fmVolYM2203[chipId][ch] = 256 * 6;
                        } else {
                            fmKeyOnYM2203[chipId][ch] &= 0xfe;
                        }
                    } else {
                        fmKeyOnYM2203[chipId][2] = (dData & 0xf0);
                        if ((dData & 0x10) > 0)
                            fmCh3SlotVolYM2203[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0)
                            fmCh3SlotVolYM2203[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0)
                            fmCh3SlotVolYM2203[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0)
                            fmCh3SlotVolYM2203[chipId][3] = 256 * 6;
                    }
                }
            }

        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = fmRegisterYM2203[chipId][0xb0 + ch] & 0x7;
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    dData = Math.min(dData + nowYM2203FadeoutVol[chipId], 127);
                    dData = maskFMChYM2203[chipId][ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) { // AL
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && maskFMChYM2203[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2203Register(chipId, 0x40 + ch + tslot, fmRegisterYM2203[chipId][0x40 + ch + tslot], model);
                    }
                }
            }
        }

        // ssg mixer
        if (dAddr == 0x07) {
            int maskData = 0;
            if (maskFMChYM2203[chipId][3])
                maskData |= 0x9 << 0;
            if (maskFMChYM2203[chipId][4])
                maskData |= 0x9 << 1;
            if (maskFMChYM2203[chipId][5])
                maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // ssg level
        if ((dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowYM2203FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2203[chipId][dAddr - 0x08 + 3] ? 0 : dData;
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2203[chipId].getUseReal()[0]) {
                mds.write(Ym2203Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2203[chipId] == null)
                return;

            scYM2203[chipId].setRegister(dAddr, dData);
        }
    }

    private void writeYm2203(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM2203[chipId].getUseReal()[0]) {
                mds.write(Ym2203Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM2203[chipId] == null)
                return;

            scYM2203[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM2203(int chipId, EnmModel model) {
        int i;

        // FM全チャネルキーオフ
        writeYm2203(chipId, 0, 0x28, 0x00, model);
        writeYm2203(chipId, 0, 0x28, 0x01, model);
        writeYm2203(chipId, 0, 0x28, 0x02, model);

        // FM TL=127
        for (i = 0x40; i < 0x4F + 1; i++) {
            writeYm2203(chipId, 0, i, 0x7f, model);
        }
        // FM ML/DT
        for (i = 0x30; i < 0x3F + 1; i++) {
            writeYm2203(chipId, 0, i, 0x0, model);
        }
        // FM AR,DR,SR,KS,AMON
        for (i = 0x50; i < 0x7F + 1; i++) {
            writeYm2203(chipId, 0, i, 0x0, model);
        }
        // FM SL,RR
        for (i = 0x80; i < 0x8F + 1; i++) {
            writeYm2203(chipId, 0, i, 0xff, model);
        }
        // FM F-Num, FB/CONNECT
        for (i = 0x90; i < 0xBF + 1; i++) {
            writeYm2203(chipId, 0, i, 0x0, model);
        }
        // FM PAN/AMS/PMS
        for (i = 0xB4; i < 0xB6 + 1; i++) {
            writeYm2203(chipId, 0, i, 0xc0, model);
        }
        writeYm2203(chipId, 0, 0x22, 0x00, model); // HW LFO
        writeYm2203(chipId, 0, 0x24, 0x00, model); // Timer-a(1)
        writeYm2203(chipId, 0, 0x25, 0x00, model); // Timer-a(2)
        writeYm2203(chipId, 0, 0x26, 0x00, model); // Timer-B
        writeYm2203(chipId, 0, 0x27, 0x30, model); // Timer Control

        // SSG 音程(2byte*3ch)
        for (i = 0x00; i < 0x05 + 1; i++) {
            writeYm2203(chipId, 0, i, 0x00, model);
        }
        writeYm2203(chipId, 0, 0x06, 0x00, model); // SSG ノイズ周波数
        writeYm2203(chipId, 0, 0x07, 0x38, model); // SSG ミキサ
        // SSG ボリューム(3ch)
        for (i = 0x08; i < 0x0A + 1; i++) {
            writeYm2203(chipId, 0, i, 0x00, model);
        }
        // SSG Envelope
        for (i = 0x0B; i < 0x0D + 1; i++) {
            writeYm2203(chipId, 0, i, 0x00, model);
        }

    }

    public void softResetAY8910(int chipId, EnmModel model) {

        // 全チャネルキーオフ
        setAY8910Register(chipId, 0x07, 0x00, model);

        // ボリュームオフ
        for (int ch = 0; ch < 3; ch++) {
            setAY8910Register(chipId, 0x8 + ch, 0x00, model);
        }

        // ノイズ初期化
        setAY8910Register(chipId, 0x06, 0x00, model);
        // エンベロープ初期化
        setAY8910Register(chipId, 0x0b, 0x00, model);
        setAY8910Register(chipId, 0x0c, 0x00, model);
        setAY8910Register(chipId, 0x0d, 0x00, model);
    }

    public void softResetYM2413(int chipId, EnmModel model) {

        // FM全チャネルキーオフ
        for (int ch = 0; ch < 9; ch++) {
            setYM2413Register(chipId, 0x20 + ch, 0x00, model);
        }
        setYM2413Register(chipId, 0x0e, 0x00, model);

        // FM TL=15
        for (int ch = 0; ch < 9; ch++) {
            setYM2413Register(chipId, 0x30 + ch, 0x0f, model);
        }
        setYM2413Register(chipId, 0x36, 0x0f, model);
        setYM2413Register(chipId, 0x37, 0xff, model);
        setYM2413Register(chipId, 0x38, 0xff, model);

    }

    public void setYM3526Register(int chipId, int dAddr, int dData, EnmModel model) {
        // if (ctYM3526 == null) return;

        if (chipId == 0)
            chipLED.put("PriOPL", 2);
        else
            chipLED.put("SecOPL", 2);

        fmRegisterYM3526[chipId][dAddr] = dData;

        if (dAddr >= 0x40 && dAddr <= 0x55) { // TL
            int ksl = dData & 0xc0;
            int tl = dData & 0x3f;
            int ch = dAddr - 0x40;
            boolean cr = false;
            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);

            // 2opの時のキャリア判定
            if (ch % 8 > 2)
                cr = true;
            else {
                int cnt = fmRegisterYM3526[chipId][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                if (cnt == 1)
                    cr = true;
            }

            if (ch >= 0x10 && (fmRegisterYM3526[chipId][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                dData = Math.min(tl + nowYM3526FadeoutVol[chipId], 0x3f);
                dData = ksl + (maskFMChYM3526[chipId][twoOpChannel] ? 0x3f : dData);
            }
        }

        // if (model == EnmModel.VirtualModel)
        {
            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    kiYM3526[chipId].On[ch] = false;
                    kiYM3526[chipId].Off[ch] = true;
                } else {
                    kiYM3526[chipId].On[ch] = true;
                }
                if (maskFMChYM3526[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((dData & (0x10 >> c)) == 0) {
                        kiYM3526[chipId].Off[c + 9] = true;
                    } else {
                        if (kiYM3526[chipId].Off[c + 9])
                            kiYM3526[chipId].On[c + 9] = true;
                        kiYM3526[chipId].Off[c + 9] = false;
                    }
                }

                if (maskFMChYM3526[chipId][9])
                    dData &= 0xef;
                if (maskFMChYM3526[chipId][10])
                    dData &= 0xf7;
                if (maskFMChYM3526[chipId][11])
                    dData &= 0xfb;
                if (maskFMChYM3526[chipId][12])
                    dData &= 0xfd;
                if (maskFMChYM3526[chipId][13])
                    dData &= 0xfe;
            }
        }

        writeYm3526(chipId, dAddr, dData, model);
    }

    public ChipKeyInfo getYM3526KeyInfo(int chipId) {
        for (int ch = 0; ch < kiYM3526[chipId].Off.length; ch++) {
            kiYM3526ret[chipId].Off[ch] = kiYM3526[chipId].Off[ch];
            kiYM3526ret[chipId].On[ch] = kiYM3526[chipId].On[ch];
            kiYM3526[chipId].On[ch] = false;
        }
        return kiYM3526ret[chipId];
    }

    private void writeYm3526(int chipId, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM3526[chipId].getUseReal()[0]) {
                mds.write(Ym3526Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM3526[chipId] == null)
                return;

            scYM3526[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM3526(int chipId, EnmModel model) {
        int i;

        // FM全チャネルキーオフ
        for (i = 0; i < 9; i++) {
            writeYm3526(chipId, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (i = 0; i < 22; i++) {
            writeYm3526(chipId, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (i = 0; i < 22; i++) {
            writeYm3526(chipId, 0x80 + i, 0xff, model);
        }
    }

    public void setYM3812Register(int chipId, int dAddr, int dData, EnmModel model) {
        // if (ctYM3812 == null) return;

        if (chipId == 0)
            chipLED.put("PriOPL2", 2);
        else
            chipLED.put("SecOPL2", 2);

        fmRegisterYM3812[chipId][dAddr] = dData;

        if (dAddr >= 0x40 && dAddr <= 0x55) { // TL
            int ksl = dData & 0xc0;
            int tl = dData & 0x3f;
            int ch = dAddr - 0x40;
            boolean cr = false;
            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);

            // 2opの時のキャリア判定
            if (ch % 8 > 2)
                cr = true;
            else {
                int cnt = fmRegisterYM3812[chipId][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                if (cnt == 1)
                    cr = true;
            }

            if (ch >= 0x10 && (fmRegisterYM3812[chipId][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                dData = Math.min(tl + nowYM3812FadeoutVol[chipId], 0x3f);
                dData = ksl + (maskFMChYM3812[chipId][twoOpChannel] ? 0x3f : dData);
            }
        }

        // if (model == EnmModel.VirtualModel)
        {
            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    kiYM3812[chipId].Off[ch] = true;
                } else {
                    if (kiYM3812[chipId].Off[ch])
                        kiYM3812[chipId].On[ch] = true;
                    kiYM3812[chipId].Off[ch] = false;
                }
                if (maskFMChYM3812[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((dData & (0x10 >> c)) == 0) {
                        kiYM3812[chipId].Off[c + 9] = true;
                    } else {
                        if (kiYM3812[chipId].Off[c + 9])
                            kiYM3812[chipId].On[c + 9] = true;
                        kiYM3812[chipId].Off[c + 9] = false;
                    }
                }

                if (maskFMChYM3812[chipId][9])
                    dData &= 0xef;
                if (maskFMChYM3812[chipId][10])
                    dData &= 0xf7;
                if (maskFMChYM3812[chipId][11])
                    dData &= 0xfb;
                if (maskFMChYM3812[chipId][12])
                    dData &= 0xfd;
                if (maskFMChYM3812[chipId][13])
                    dData &= 0xfe;

            }

        }

        writeYm3812(chipId, dAddr, dData, model);

    }

    public ChipKeyInfo getYM3812KeyInfo(int chipId) {
        for (int ch = 0; ch < kiYM3812[chipId].Off.length; ch++) {
            kiYM3812ret[chipId].Off[ch] = kiYM3812[chipId].Off[ch];
            kiYM3812ret[chipId].On[ch] = kiYM3812[chipId].On[ch];
            kiYM3812[chipId].On[ch] = false;
        }
        return kiYM3812ret[chipId];
    }

    private void writeYm3812(int chipId, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM3812[chipId].getUseReal()[0]) {
                mds.write(Ym3812Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYM3812[chipId] == null)
                return;

            scYM3812[chipId].setRegister(dAddr, dData);
        }
    }

    public void softResetYM3812(int chipId, EnmModel model) {
        int i;

        // FM全チャネルキーオフ
        for (i = 0; i < 9; i++) {
            writeYm3812(chipId, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (i = 0; i < 22; i++) {
            writeYm3812(chipId, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (i = 0; i < 22; i++) {
            writeYm3812(chipId, 0x80 + i, 0xff, model);
        }
    }

    public int getYMF262RyhthmKeyON(int chipId) {
        int r = fmRegisterYMF262Ryhthm[chipId];
        fmRegisterYMF262Ryhthm[chipId] = 0;
        return r;
    }

    public int getYMF262FMKeyON(int chipId) {
        return fmRegisterYMF262FM[chipId];
    }

    public void setYMF262Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (ctYMF262 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriOPL3", 2);
        else
            chipLED.put("SecOPL3", 2);

        fmRegisterYMF262[chipId][dPort][dAddr] = dData;

        if (dAddr >= 0x40 && dAddr <= 0x55)// TL
        {
            int ksl = (dData & 0xc0);
            int tl = (dData & 0x3f);
            int ch = dAddr - 0x40;
            int conSel = fmRegisterYMF262[chipId][1][4] & 0x3f;
            boolean cr = false;

            int twoOpChannel = (ch / 8) * 3 + ((ch % 8) % 3);
            int fourOpChannel = twoOpChannel > 5 ? -1 : ((twoOpChannel % 3) + dPort * 3);
            boolean fourOpMode = fourOpChannel != -1 && ((conSel & (1 << fourOpChannel)) != 0);
            int slotNumber = ((ch % 8) / 3) + (twoOpChannel > 2 ? 2 : 0);
            twoOpChannel += dPort * 9;

            if (!fourOpMode) {
                // 2opの時のキャリア判定
                if (ch % 8 > 2)
                    cr = true;
                else {
                    int cnt = fmRegisterYMF262[chipId][dPort][0xc0 + (ch / 8) * 3 + (ch % 8)] & 1;
                    if (cnt == 1)
                        cr = true;
                }
            } else {
                if (slotNumber == 3)
                    cr = true;
                else {
                    int cnt0 = fmRegisterYMF262[chipId][dPort][0xc0 + (fourOpChannel % 3)] & 1;
                    int cnt1 = fmRegisterYMF262[chipId][dPort][0xc3 + (fourOpChannel % 3)] & 1;
                    if (cnt0 == 0) {
                        if (cnt1 == 1 && slotNumber == 1)
                            cr = true;
                    } else {
                        if (cnt1 == 0) {
                            if (slotNumber == 0)
                                cr = true;
                        } else {
                            if (slotNumber != 1)
                                cr = true;
                        }
                    }
                }
            }

            if (ch >= 0x10 && dPort == 0 && (fmRegisterYMF262[chipId][dPort][0xbd] & 0x20) != 0) {
                cr = true;
            }

            if (cr) {
                dData = Math.min(tl + nowYMF262FadeoutVol[chipId], 0x3f);
                dData = ksl + (maskFMChYMF262[chipId][twoOpChannel] ? 0x3f : dData);
            }
        }

        if (dAddr >= 0xb0 && dAddr <= 0xb8) {
            int ch = dAddr - 0xb0 + dPort * 9;
            int k = (dData >> 5) & 1;
            if (k == 0) {
                fmRegisterYMF262FM[chipId] &= ~(1 << ch);
            } else {
                fmRegisterYMF262FM[chipId] |= (1 << ch);
            }
            fmRegisterYMF262FM[chipId] &= 0x3ffff;
            if (maskFMChYMF262[chipId][ch])
                dData &= 0x1f;
        }

        if (dAddr == 0xbd && dPort == 0) {
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x10) == 0 && (dData & 0x10) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x10;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x08) == 0 && (dData & 0x08) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x08;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x04) == 0 && (dData & 0x04) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x04;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x02) == 0 && (dData & 0x02) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x02;
            if ((fmRegisterYMF262RyhthmB[chipId] & 0x01) == 0 && (dData & 0x01) != 0)
                fmRegisterYMF262Ryhthm[chipId] |= 0x01;
            fmRegisterYMF262RyhthmB[chipId] = dData;

            if (maskFMChYMF262[chipId][18])
                dData &= 0xef;
            if (maskFMChYMF262[chipId][19])
                dData &= 0xf7;
            if (maskFMChYMF262[chipId][20])
                dData &= 0xfb;
            if (maskFMChYMF262[chipId][21])
                dData &= 0xfd;
            if (maskFMChYMF262[chipId][22])
                dData &= 0xfe;

        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYMF262[chipId].getUseReal()[0]) {
                mds.write(YmF262Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF262[chipId] == null)
                return;
            scYMF262[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }

    }

    private void writeYmF262(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYMF262[chipId].getUseReal()[0]) {
                mds.write(YmF262Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF262[chipId] == null)
                return;

            scYMF262[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void softResetYMF262(int chipId, EnmModel model) {
        int i;

        // FM全チャネルキーオフ
        for (i = 0; i < 9; i++) {
            writeYmF262(chipId, 0, 0xb0 + i, 0x00, model);
            writeYmF262(chipId, 1, 0xb0 + i, 0x00, model);
        }

        // FM TL=127
        for (i = 0; i < 22; i++) {
            writeYmF262(chipId, 0, 0x40 + i, 0x3f, model);
            writeYmF262(chipId, 1, 0x40 + i, 0x3f, model);
        }

        // SL=15 RR=15
        for (i = 0; i < 22; i++) {
            writeYmF262(chipId, 0, 0x80 + i, 0xff, model);
            writeYmF262(chipId, 1, 0x80 + i, 0xff, model);
        }
    }

    public void setYM2608Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        // if (chipId == 0 && dPort == 1 && dAddr == 0x01)
        // {
        // Debug.printf(String.format("FM P1 Out:Adr[%02x] val[%02x]",
        // (int)dAddr, (int)dData));
        // }

        if (ctYM2608 == null)
            return;
        if (dAddr < 0 || dData < 0)
            return;

        if (chipId == 0)
            chipLED.put("PriOPNA", 2);
        else
            chipLED.put("SecOPNA", 2);

        if ((model == EnmModel.VirtualModel && (ctYM2608[chipId] == null || !ctYM2608[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scYM2608 != null && scYM2608[chipId] != null))) {
            if (dPort == 0 && (dAddr == 0x2d || dAddr == 0x2e || dAddr == 0x2f)) {
                fmRegisterYM2608[chipId][0][0x2d] = dAddr - 0x2d;
            } else {
                fmRegisterYM2608[chipId][dPort][dAddr] = dData;
            }
        }

        if ((model == EnmModel.RealModel && ctYM2608[chipId].getUseReal()[0]) ||
                (model == EnmModel.VirtualModel && !ctYM2608[chipId].getUseReal()[0])) {
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6)// && (dData & 0xf0) > 0)
                {
                    if (ch != 2 || (fmRegisterYM2608[chipId][0][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            fmKeyOnYM2608[chipId][ch] = (dData & 0xf0) | 1;
                            fmVolYM2608[chipId][ch] = 256 * 6;
                        } else {
                            fmKeyOnYM2608[chipId][ch] = (dData & 0xf0) | 0;
                        }
                    } else {
                        fmKeyOnYM2608[chipId][2] = dData & 0xf0;
                        if ((dData & 0x10) > 0)
                            fmCh3SlotVolYM2608[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0)
                            fmCh3SlotVolYM2608[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0)
                            fmCh3SlotVolYM2608[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0)
                            fmCh3SlotVolYM2608[chipId][3] = 256 * 6;
                    }
                }
            }

            if (dPort == 1 && dAddr == 0x01) {
                fmVolYM2608AdpcmPan[chipId] = (dData & 0xc0) >> 6;
                if (fmVolYM2608AdpcmPan[chipId] > 0) {
                    fmVolYM2608Adpcm[chipId][0] = (int) ((256 * 6.0 * fmRegisterYM2608[chipId][1][0x0b] / 64.0) *
                            ((fmVolYM2608AdpcmPan[chipId] & 0x02) > 0 ? 1 : 0));
                    fmVolYM2608Adpcm[chipId][1] = (int) ((256 * 6.0 * fmRegisterYM2608[chipId][1][0x0b] / 64.0) *
                            ((fmVolYM2608AdpcmPan[chipId] & 0x01) > 0 ? 1 : 0));
                }
            }

            if (dPort == 0 && dAddr == 0x10) {
                int tl = fmRegisterYM2608[chipId][0][0x11] & 0x3f;
                for (int i = 0; i < 6; i++) {
                    if ((dData & (0x1 << i)) != 0) {
                        int il = (fmRegisterYM2608[chipId][0][0x18 + i] & 0x1f) * (((dData & 0x80) == 0) ? 1 : 0);
                        int pan = (fmRegisterYM2608[chipId][0][0x18 + i] & 0xc0) >> 6;
                        fmVolYM2608Rhythm[chipId][i][0] = (int) (256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 2) > 0 ? 1 : 0);
                        fmVolYM2608Rhythm[chipId][i][1] = (int) (256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 1) > 0 ? 1 : 0);
                    }
                }
            }

        }

        if ((dAddr & 0xf0) == 0x40)// TL
        {
            int ch = (dAddr & 0x3);
            int al = fmRegisterYM2608[chipId][dPort][0xb0 + ch] & 0x07;// AL
            int slot = (dAddr & 0xc) >> 2;
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    dData = Math.min(dData + nowYM2608FadeoutVol[chipId], 127);
                    dData = maskFMChYM2608[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0)// AL
        {
            int ch = (dAddr & 0x3);
            int al = dData & 0x07;// AL

            if (ch != 3 && maskFMChYM2608[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) > 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2608Register(chipId,
                                dPort,
                                0x40 + ch + tslot,
                                fmRegisterYM2608[chipId][dPort][0x40 + ch + tslot],
                                model);
                    }
                }
            }
        }

        // ssg mixer
        if (dPort == 0 && dAddr == 0x07) {
            int maskData = 0;
            if (maskFMChYM2608[chipId][6])
                maskData |= 0x9 << 0;
            if (maskFMChYM2608[chipId][7])
                maskData |= 0x9 << 1;
            if (maskFMChYM2608[chipId][8])
                maskData |= 0x9 << 2;
            dData |= maskData;
        }

        // ssg level
        if (dPort == 0 && (dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowYM2608FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2608[chipId][dAddr - 0x08 + 6] ? 0 : dData;
        }

        // rhythm level
        if (dPort == 0 && dAddr == 0x11) {
            int d = nowYM2608FadeoutVol[chipId] >> 1;
            dData = Math.max(dData - d, 0);
        }

        // adpcm level
        if (dPort == 1 && dAddr == 0x0b) {
            int d = nowYM2608FadeoutVol[chipId] * 2;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2608[chipId][12] ? 0 : dData;
        }

        // adpcm start
        if (dPort == 1 && dAddr == 0x00) {
            if ((dData & 0x80) != 0 && maskFMChYM2608[chipId][12]) {
                dData &= 0x7f;
            }
        }

        // Ryhthm
        if (dPort == 0 && dAddr == 0x10) {
            if (maskFMChYM2608[chipId][13]) {
                dData = 0;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYM2608[chipId].getUseReal()[0] && ctYM2608[chipId].getUseEmu()[0]) {
                // if(dAddr==0x29) System.err.printf("%2x:%2x:%2x ", dPort,
                // dAddr, dData);
                mds.write(Ym2608Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYM2608[chipId] == null)
                return;

            scYM2608[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }

    }

    public int getYM2608Register(int chipId, int dPort, int dAddr, EnmModel model) {
        if (ctYM2608 == null)
            return 0;

        if (model == EnmModel.VirtualModel) {
            return 0;
        } else {
            if (scYM2608[chipId] == null)
                return 0;

            return scYM2608[chipId].getRegister(dPort * 0x100 + dAddr);
        }

    }

    private void writeYm2608(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            if (!ctYM2608[chipId].getUseReal()[0] && ctYM2608[chipId].getUseEmu()[0]) {
                mds.write(Ym2608Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYM2608[chipId] == null)
                return;

            scYM2608[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void softResetYM2608(int chipId, EnmModel model) {
        int i;

        // FM全チャネルキーオフ
        writeYm2608(chipId, 0, 0x28, 0x00, model);
        writeYm2608(chipId, 0, 0x28, 0x01, model);
        writeYm2608(chipId, 0, 0x28, 0x02, model);
        writeYm2608(chipId, 0, 0x28, 0x04, model);
        writeYm2608(chipId, 0, 0x28, 0x05, model);
        writeYm2608(chipId, 0, 0x28, 0x06, model);

        // FM TL=127
        for (i = 0x40; i < 0x4F + 1; i++) {
            writeYm2608(chipId, 0, i, 0x7f, model);
            writeYm2608(chipId, 1, i, 0x7f, model);
        }
        // FM ML/DT
        for (i = 0x30; i < 0x3F + 1; i++) {
            writeYm2608(chipId, 0, i, 0x0, model);
            writeYm2608(chipId, 1, i, 0x0, model);
        }
        // FM AR,DR,SR,KS,AMON
        for (i = 0x50; i < 0x7F + 1; i++) {
            writeYm2608(chipId, 0, i, 0x0, model);
            writeYm2608(chipId, 1, i, 0x0, model);
        }
        // FM SL,RR
        for (i = 0x80; i < 0x8F + 1; i++) {
            writeYm2608(chipId, 0, i, 0xff, model);
            writeYm2608(chipId, 1, i, 0xff, model);
        }
        // FM F-Num, FB/CONNECT
        for (i = 0x90; i < 0xBF + 1; i++) {
            writeYm2608(chipId, 0, i, 0x0, model);
            writeYm2608(chipId, 1, i, 0x0, model);
        }
        // FM PAN/AMS/PMS
        for (i = 0xB4; i < 0xB6 + 1; i++) {
            writeYm2608(chipId, 0, i, 0xc0, model);
            writeYm2608(chipId, 1, i, 0xc0, model);
        }
        writeYm2608(chipId, 0, 0x22, 0x00, model); // HW LFO
        writeYm2608(chipId, 0, 0x24, 0x00, model); // Timer-a(1)
        writeYm2608(chipId, 0, 0x25, 0x00, model); // Timer-a(2)
        writeYm2608(chipId, 0, 0x26, 0x00, model); // Timer-B
        writeYm2608(chipId, 0, 0x27, 0x30, model); // Timer Control
        writeYm2608(chipId, 0, 0x29, 0x80, model); // FM4-6 Enable

        // SSG 音程(2byte*3ch)
        for (i = 0x00; i < 0x05 + 1; i++) {
            writeYm2608(chipId, 0, i, 0x00, model);
        }
        writeYm2608(chipId, 0, 0x06, 0x00, model); // SSG ノイズ周波数
        writeYm2608(chipId, 0, 0x07, 0x38, model); // SSG ミキサ
        // SSG ボリューム(3ch)
        for (i = 0x08; i < 0x0A + 1; i++) {
            writeYm2608(chipId, 0, i, 0x00, model);
        }
        // SSG Envelope
        for (i = 0x0B; i < 0x0D + 1; i++) {
            writeYm2608(chipId, 0, i, 0x00, model);
        }

        // RHYTHM
        writeYm2608(chipId, 0, 0x10, 0xBF, model); // 強制発音停止
        writeYm2608(chipId, 0, 0x11, 0x00, model); // Total Level
        writeYm2608(chipId, 0, 0x18, 0x00, model); // BD音量
        writeYm2608(chipId, 0, 0x19, 0x00, model); // SD音量
        writeYm2608(chipId, 0, 0x1A, 0x00, model); // CYM音量
        writeYm2608(chipId, 0, 0x1B, 0x00, model); // HH音量
        writeYm2608(chipId, 0, 0x1C, 0x00, model); // TOM音量
        writeYm2608(chipId, 0, 0x1D, 0x00, model); // RIM音量

        // ADPCM
        writeYm2608(chipId, 1, 0x00, 0x21, model); // ADPCMリセット
        writeYm2608(chipId, 1, 0x01, 0x06, model); // ADPCM消音
        writeYm2608(chipId, 1, 0x10, 0x9C, model); // FLAGリセット }
    }

    public void setYM2610Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (ctYM2610 == null) return;
        if (dAddr < 0 || dData < 0) return;

        if (chipId == 0) chipLED.put("PriOPNB", 2);
        else chipLED.put("SecOPNB", 2);


        if (
                (model == EnmModel.VirtualModel && (ctYM2610[chipId] == null || !ctYM2610[chipId].getUseReal()[0]))
                        || (model == EnmModel.RealModel && (scYM2610 != null && scYM2610[chipId] != null))
        ) {
            if (dPort == 0 && (dAddr == 0x2d || dAddr == 0x2e || dAddr == 0x2f)) {
                fmRegisterYM2610[chipId][0][0x2d] = dAddr - 0x2d;
            } else {
                fmRegisterYM2610[chipId][dPort][dAddr] = dData;
            }
        }

//#if DEBUG
        //System.err.println("OPNB p:%02X a:%02X D:%02X", dPort, dAddr, dData);
//#endif

        if ((model == EnmModel.RealModel && ctYM2610[chipId].getUseReal()[0]) || (model == EnmModel.VirtualModel && !ctYM2610[chipId].getUseReal()[0])) {
            //fmRegisterYM2610[dPort][dAddr] = dData;
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6)// && (dData & 0xf0) > 0)
                {
                    if (ch != 2 || (fmRegisterYM2610[chipId][0][0x27] & 0xc0) != 0x40) {
                        if ((dData & 0xf0) != 0) {
                            fmKeyOnYM2610[chipId][ch] = (dData & 0xf0) | 1;
                            fmVolYM2610[chipId][ch] = 256 * 6;
                        } else {
                            fmKeyOnYM2610[chipId][ch] &= 0xfe;
                        }
                    } else {
                        fmKeyOnYM2610[chipId][2] = dData & 0xf0;
                        if ((dData & 0x10) > 0) fmCh3SlotVolYM2610[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0) fmCh3SlotVolYM2610[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0) fmCh3SlotVolYM2610[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0) fmCh3SlotVolYM2610[chipId][3] = 256 * 6;
                    }
                }
            }


            // ADPCM B KEYON
            if (dPort == 0 && dAddr == 0x10) {
                if ((dData & 0x80) != 0) {
                    int p = (fmRegisterYM2610[chipId][0][0x11] & 0xc0) >> 6;
                    p = p == 0 ? 3 : p;
                    if (fmVolYM2610AdpcmPan[chipId] != p)
                        fmVolYM2610AdpcmPan[chipId] = p;

                    fmVolYM2610Adpcm[chipId][0] = ((fmVolYM2610AdpcmPan[chipId] & 0x02) != 0 ? 1 : 0);
                    fmVolYM2610Adpcm[chipId][1] = ((fmVolYM2610AdpcmPan[chipId] & 0x01) != 0 ? 1 : 0);
                } else {
                    fmVolYM2610Adpcm[chipId][0] = 0;
                    fmVolYM2610Adpcm[chipId][1] = 0;
                }
            }

            // ADPCM a KEYON
            if (dPort == 1 && dAddr == 0x00) {
                if ((dData & 0x80) == 0) {
                    int tl = fmRegisterYM2610[chipId][1][0x01] & 0x3f;
                    for (int i = 0; i < 6; i++) {
                        if ((dData & (0x1 << i)) != 0) {
                            //int il = fmRegisterYM2610[chipId][1][0x08 + i] & 0x1f;
                            int pan = ((fmRegisterYM2610[chipId][1][0x08 + i] & 0xc0) >> 6) * (((dData & 0x80) == 0) ? 1 : 0);
                            //fmVolYM2610Rhythm[chipId][i][0] = (int)(256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 2) > 0 ? 1 : 0);
                            //fmVolYM2610Rhythm[chipId][i][1] = (int)(256 * 6 * ((tl * il) >> 4) / 127.0) * ((pan & 1) > 0 ? 1 : 0);
                            fmVolYM2610Rhythm[chipId][i][0] = ((pan & 2) > 0 ? 1 : 0);
                            fmVolYM2610Rhythm[chipId][i][1] = ((pan & 1) > 0 ? 1 : 0);
                        } else {
                            fmVolYM2610Rhythm[chipId][i][0] = 0;
                            fmVolYM2610Rhythm[chipId][i][1] = 0;
                        }
                    }
                }
            }

        }


        if ((dAddr & 0xf0) == 0x40) // TL
        {
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = fmRegisterYM2610[chipId][dPort][0xb0 + ch] & 0x07; // AL
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) > 0) {
                    dData = Math.min(dData + nowYM2610FadeoutVol[chipId], 127);
                    dData = maskFMChYM2610[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) // AL
        {
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && maskFMChYM2610[chipId][ch]) {
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2610Register(
                                chipId
                                , dPort
                                , 0x40 + ch + tslot
                                , fmRegisterYM2610[chipId][dPort][0x40 + ch + tslot]
                                , model);
                    }
                }
            }
        }

         // ssg mixer
        if (dPort == 0 && dAddr == 0x07) {
            int maskData = 0;
            if (maskFMChYM2610[chipId][6]) maskData |= 0x9 << 0;
            if (maskFMChYM2610[chipId][7]) maskData |= 0x9 << 1;
            if (maskFMChYM2610[chipId][8]) maskData |= 0x9 << 2;
            dData |= maskData;
        }

         // ssg level
        if (dPort == 0 && (dAddr == 0x08 || dAddr == 0x09 || dAddr == 0x0a)) {
            int d = nowYM2610FadeoutVol[chipId] >> 3;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2610[chipId][dAddr - 0x08 + 6] ? 0 : dData;
        }

         // rhythm level
        if (dPort == 1 && dAddr == 0x01) {
            int d = nowYM2610FadeoutVol[chipId] >> 1;
            dData = Math.max(dData - d, 0);
            //dData = maskFMChYM2610[chipId][12] ? 0 : dData;
        }

         // Rhythm
        if (dPort == 1 && dAddr == 0x00) {
            if (maskFMChYM2610[chipId][12]) {
                dData = 0xbf;
            }
        }

         // adpcm level
        if (dPort == 0 && dAddr == 0x1b) {
            int d = nowYM2610FadeoutVol[chipId] * 2;
            dData = Math.max(dData - d, 0);
            dData = maskFMChYM2610[chipId][13] ? 0 : dData;
        }

         // adpcm start
        if (dPort == 0 && dAddr == 0x10) {
            if ((dData & 0x80) != 0 && maskFMChYM2610[chipId][13]) {
                dData &= 0x7f;
            }
        }


        if (model == EnmModel.VirtualModel) {
            if (
                    (ctYM2610[chipId].getUseReal().length > 0 && !ctYM2610[chipId].getUseReal()[0])
                            && (ctYM2610[chipId].getUseReal().length < 2 || (ctYM2610[chipId].getUseReal().length > 1 && !ctYM2610[chipId].getUseReal()[1]))
            ) {
                mds.write(Ym2610Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYM2610[chipId] != null) scYM2610[chipId].setRegister(dPort * 0x100 + dAddr, dData);
            if (scYM2610EA[chipId] != null) {
                int dReg = (dPort << 8) | dAddr;
                boolean bSend = true;
                // レジスタをマスクして送信する
                if (dReg >= 0x100 && dReg <= 0x12d) {
                    // ADPCM-a
                    bSend = false;
                } else if (dReg >= 0x010 && dReg <= 0x01c) {
                    // ADPCM-B
                    bSend = false;
                }
                if (bSend) {
                    scYM2610EA[chipId].setRegister((dPort << 8) | dAddr, dData);
                }
            }
            if (scYM2610EB[chipId] != null) {
                scYM2610EB[chipId].setRegister((dPort << 8) | dAddr | 0x10000, dData);
            }
        }

    }

    public void writeYm2610_SetAdpcmA(int chipId, byte[] ym2610AdpcmA, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            mds.writeYm2610SetAdpcmA(chipId, ym2610AdpcmA);
        } else {
            if (scYM2610[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x01);
                // データ転送
                for (byte b : ym2610AdpcmA) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, b & 0xff);
                }

                realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                int dPort = 2;
                int startAddr = 0;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x01);
                // データ転送
                for (byte b : ym2610AdpcmA) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, b & 0xff);
                }

                realChip.SendData();
            }
        }
    }

    public void writeYm2610_SetAdpcmA(int chipId, EnmModel model, int startAddr, int length, byte[] buf, int srcStartAddr) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2610[chipId] != null) {
                byte dPort = 2;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x01);
                // データ転送
                for (int cnt = 0; cnt < length; cnt++) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, buf[srcStartAddr + cnt] & 0xff);
                }

                realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                byte dPort = 2;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x01);
                // データ転送
                for (int cnt = 0; cnt < length; cnt++) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, buf[srcStartAddr + cnt] & 0xff);
                }

                realChip.SendData();
            }
        }
    }

    public void WriteYM2610_SetAdpcmB(int chipId, byte[] ym2610AdpcmB, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
            mds.writeYm2610SetAdpcmB(chipId, ym2610AdpcmB);
        } else {
            if (scYM2610[chipId] != null) {
                byte dPort = 2;
                int startAddr = 0;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x00);
                // データ転送
                for (byte b : ym2610AdpcmB) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, b & 0xff);
                }

                realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                byte dPort = 2;
                int startAddr = 0;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x00);
                // データ転送
                for (byte b : ym2610AdpcmB) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, b & 0xff);
                }

                realChip.SendData();
            }
        }
    }

    public void WriteYM2610_SetAdpcmB(int chipId, EnmModel model, int startAddr, int length, byte[] buf, int srcStartAddr) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2610[chipId] != null) {
                byte dPort = 2;
                scYM2610[chipId].setRegister((dPort << 8) | 0x00, 0x00);
                scYM2610[chipId].setRegister((dPort << 8) | 0x01, (startAddr >> 8) & 0xff);
                scYM2610[chipId].setRegister((dPort << 8) | 0x02, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610[chipId].setRegister((dPort << 8) | 0x03, 0x00);
                // データ転送
                for (int cnt = 0; cnt < length; cnt++) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610[chipId].setRegister((dPort << 8) | 0x04, buf[srcStartAddr + cnt] & 0xff);
                }

                realChip.SendData();
            }
            if (scYM2610EB[chipId] != null) {
                byte dPort = 2;
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10000, 0x00);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10001, (startAddr >> 8) & 0xff);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10002, (startAddr >> 16) & 0xff);

                // pushReg(CMD_YM2610|0x02,0x03,0x01);
                scYM2610EB[chipId].setRegister((dPort << 8) | 0x10003, 0x00);
                // データ転送
                for (int cnt = 0; cnt < length; cnt++) {
                    // pushReg(CMD_YM2610|0x02,0x04,*m_pDump);
                    scYM2610EB[chipId].setRegister((dPort << 8) | 0x10004, buf[srcStartAddr + cnt] & 0xff);
                }

                realChip.SendData();
            }
        }
    }

    public void setYMF271Register(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (ctYMF271 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriOPX", 2);
        else
            chipLED.put("SecOPX", 2);

        if (model == EnmModel.VirtualModel)
            fmRegisterYMF271[chipId][dPort][dAddr] = dData;

        if (model == EnmModel.VirtualModel) {
            if (!ctYMF271[chipId].getUseReal()[0]) {
                mds.write(YmF271Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF271[chipId] == null)
                return;
            scYMF271[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }
    }

    public void setYMF278BRegister(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (ctYMF278B == null)
            return;

        if (chipId == 0)
            chipLED.put("PriOPL4", 2);
        else
            chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel) {
            fmRegisterYMF278B[chipId][dPort][dAddr] = dData;

//             if (dPort == 2) {
//                 System.err.println("p=2:adr%02x dat%02x", dAddr, dData);
//             }

            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0 + dPort * 9;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    fmRegisterYMF278BFM[chipId] &= ~(1 << ch);
                } else {
                    fmRegisterYMF278BFM[chipId] |= (1 << ch);
                }
                fmRegisterYMF278BFM[chipId] &= 0x3_ffff;
                if (maskFMChYMF278B[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd && dPort == 0) {
                if ((fmRegisterYMF278BRyhthmB[chipId] & 0x10) == 0 && (dData & 0x10) != 0)
                    fmRegisterYMF278BRyhthm[chipId] |= 0x10;
                if ((fmRegisterYMF278BRyhthmB[chipId] & 0x08) == 0 && (dData & 0x08) != 0)
                    fmRegisterYMF278BRyhthm[chipId] |= 0x08;
                if ((fmRegisterYMF278BRyhthmB[chipId] & 0x04) == 0 && (dData & 0x04) != 0)
                    fmRegisterYMF278BRyhthm[chipId] |= 0x04;
                if ((fmRegisterYMF278BRyhthmB[chipId] & 0x02) == 0 && (dData & 0x02) != 0)
                    fmRegisterYMF278BRyhthm[chipId] |= 0x02;
                if ((fmRegisterYMF278BRyhthmB[chipId] & 0x01) == 0 && (dData & 0x01) != 0)
                    fmRegisterYMF278BRyhthm[chipId] |= 0x01;
                fmRegisterYMF278BRyhthmB[chipId] = dData;

                if (maskFMChYMF278B[chipId][18])
                    dData &= 0xef;
                if (maskFMChYMF278B[chipId][19])
                    dData &= 0xf7;
                if (maskFMChYMF278B[chipId][20])
                    dData &= 0xfb;
                if (maskFMChYMF278B[chipId][21])
                    dData &= 0xfd;
                if (maskFMChYMF278B[chipId][22])
                    dData &= 0xfe;
            }

            if (dPort == 2 && (dAddr >= 0x68 && dAddr <= 0x7f)) {
                int k = dData >> 7;
                if (k == 0) {
                    fmRegisterYMF278BPCM[chipId][dAddr - 0x68] = 2;
                } else {
                    fmRegisterYMF278BPCM[chipId][dAddr - 0x68] = 1;
                }
                if (maskFMChYMF278B[chipId][dAddr - 0x68 + 23])
                    dData &= 0x7f;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctYMF278B[chipId].getUseReal()[0]) {
                mds.write(YmF278bInst.class, chipId, dPort, dAddr, dData);
            }
        } else {
            if (scYMF278B[chipId] == null)
                return;
            scYMF278B[chipId].setRegister(dPort * 0x100 + dAddr, dData);
        }

    }

    public void setY8950Register(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctY8950 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriY8950", 2);
        else
            chipLED.put("SecY8950", 2);

        if (model == EnmModel.VirtualModel) {
            fmRegisterY8950[chipId][dAddr] = dData;
            if (dAddr >= 0xb0 && dAddr <= 0xb8) {
                int ch = dAddr - 0xb0;
                int k = (dData >> 5) & 1;
                if (k == 0) {
                    kiY8950[chipId].On[ch] = false;
                    kiY8950[chipId].Off[ch] = true;
                } else {
                    kiY8950[chipId].On[ch] = true;
                }
                if (maskFMChY8950[chipId][ch])
                    dData &= 0x1f;
            }

            if (dAddr == 0xbd) {

                for (int c = 0; c < 5; c++) {
                    if ((dData & (0x10 >> c)) == 0) {
                        kiY8950[chipId].Off[c + 9] = true;
                    } else {
                        if (kiY8950[chipId].Off[c + 9])
                            kiY8950[chipId].On[c + 9] = true;
                        kiY8950[chipId].Off[c + 9] = false;
                    }
                }

                if (maskFMChY8950[chipId][9])
                    dData &= 0xef;
                if (maskFMChY8950[chipId][10])
                    dData &= 0xf7;
                if (maskFMChY8950[chipId][11])
                    dData &= 0xfb;
                if (maskFMChY8950[chipId][12])
                    dData &= 0xfd;
                if (maskFMChY8950[chipId][13])
                    dData &= 0xfe;

            }

            // ADPCM
            if (dAddr == 0x07) {
                int k = (dData & 0x80);
                if (k == 0) {
                    kiY8950[chipId].On[14] = false;
                    kiY8950[chipId].Off[14] = true;
                } else {
                    kiY8950[chipId].On[14] = true;
                    kiY8950[chipId].Off[14] = false;
                }
                if (maskFMChY8950[chipId][14])
                    dData &= 0x7f;
            }

        }

        if (model == EnmModel.VirtualModel) {
            // if (!ctY8950[chipId].UseScci)
            {
                mds.write(Y8950Inst.class, chipId, 0, dAddr, dData);
            }
        } else {
        }

    }

    public void setYMZ280BRegister(int chipId, int dAddr, int dData, EnmModel model) {
        if (ctYMZ280B == null)
            return;

        if (chipId == 0)
            chipLED.put("PriYMZ", 2);
        else
            chipLED.put("SecYMZ", 2);

        if (model == EnmModel.VirtualModel)
            YMZ280BRegister[chipId][dAddr] = dData;

        if (model == EnmModel.VirtualModel) {
            if (!ctYMZ280B[chipId].getUseReal()[0]) {
                mds.write(YmZ280bInst.class, chipId, 0, dAddr, dData);
            }
        } else {
            if (scYMZ280B[chipId] == null)
                return;
            scYMZ280B[chipId].setRegister(dAddr, dData);
        }

    }

    public void setYM2612Register(int chipId, int dPort, int dAddr, int dData, EnmModel model, int vgmFrameCounter) {
        if (ctYM2612 == null) return;
        if (dAddr < 0 || dData < 0) return;

        if (chipId == 0) chipLED.put("PriOPN2", 2);
        else chipLED.put("SecOPN2", 2);

        if (model == EnmModel.VirtualModel) {
            fmRegisterYM2612[chipId][dPort][dAddr] = dData;
            midiExport.outMIDIData(EnmChip.YM2612, chipId, dPort, dAddr, dData, 0, vgmFrameCounter);
        }

        if ((model == EnmModel.RealModel && ctYM2612[chipId].getUseReal()[0]) || (model == EnmModel.VirtualModel && !ctYM2612[chipId].getUseReal()[0])) {
            //fmRegister[dPort][dAddr] = dData;
            if (dPort == 0 && dAddr == 0x28) {
                int ch = (dData & 0x3) + ((dData & 0x4) > 0 ? 3 : 0);
                if (ch >= 0 && ch < 6)// && (dData & 0xf0)>0)
                {
                    if (ch != 2 || (fmRegisterYM2612[chipId][0][0x27] & 0xc0) != 0x40) {
                        if (ch != 5 || (fmRegisterYM2612[chipId][0][0x2b] & 0x80) == 0) {
                            if ((dData & 0xf0) != 0) {
                                fmKeyOnYM2612[chipId][ch] = (dData & 0xf0) | 1;
                                fmVolYM2612[chipId][ch] = 256 * 6;
                            } else {
                                fmKeyOnYM2612[chipId][ch] = (dData & 0xf0) | 0;
                            }
                        }
                    } else {
                        fmKeyOnYM2612[chipId][2] = (dData & 0xf0);
                        if ((dData & 0x10) > 0) fmCh3SlotVolYM2612[chipId][0] = 256 * 6;
                        if ((dData & 0x20) > 0) fmCh3SlotVolYM2612[chipId][1] = 256 * 6;
                        if ((dData & 0x40) > 0) fmCh3SlotVolYM2612[chipId][2] = 256 * 6;
                        if ((dData & 0x80) > 0) fmCh3SlotVolYM2612[chipId][3] = 256 * 6;
                    }
                }
            }

             // PCM
            if ((fmRegisterYM2612[chipId][0][0x2b] & 0x80) > 0) {
                if (fmRegisterYM2612[chipId][0][0x2a] > 0) {
                    fmVolYM2612[chipId][5] = Math.abs(fmRegisterYM2612[chipId][0][0x2a] - 0x7f) * 20;
                }
            }
        }

        if ((dAddr & 0xf0) == 0x40) { // TL
            int ch = (dAddr & 0x3);
            int slot = (dAddr & 0xc) >> 2;
            int al = fmRegisterYM2612[chipId][dPort][0xb0 + ch] & 0x07;
            dData &= 0x7f;

            if (ch != 3) {
                if ((algM[al] & (1 << slot)) != 0) {
                    dData = Math.min(dData + nowYM2612FadeoutVol[chipId], 127);
                    dData = maskFMChYM2612[chipId][dPort * 3 + ch] ? 127 : dData;
                }
            }
        }

        if ((dAddr & 0xf0) == 0xb0) { // AL
            int ch = (dAddr & 0x3);
            int al = dData & 0x07; // AL

            if (ch != 3 && maskFMChYM2612[chipId][dPort * 3 + ch]) {
                 // CarrierのTLを再設定する
                for (int slot = 0; slot < 4; slot++) {
                    if ((algM[al] & (1 << slot)) != 0) {
                        int tslot = (slot == 1 ? 2 : (slot == 2 ? 1 : slot)) * 4;
                        setYM2612Register(
                                chipId
                                , dPort
                                , 0x40 + ch + tslot
                                , fmRegisterYM2612[chipId][dPort][0x40 + ch + tslot]
                                , model
                                , vgmFrameCounter);
                    }
                }
            }
        }

        if (dAddr == 0x2a) {
             // PCMデータをマスクする
            if (maskFMChYM2612[chipId][5]) dData = 0x00;
            //System.err.println("%02x",dData);
        }

        if (model == EnmModel.VirtualModel) {

             // 仮想音源の処理

            if (ctYM2612[chipId].getUseReal()[0]) {
                 // Scciを使用する場合でも
                 // PCM(6Ch)だけエミュで発音するとき
                if (ctYM2612[chipId].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                    if (dPort == 0 && dAddr == 0x2b) {
                        //if (ctYM2612[chipId].getUseEmu()[0])
                        mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, dPort, dAddr, dData);
                    } else if (dPort == 0 && dAddr == 0x2a) {
                        //if (ctYM2612[chipId].getUseEmu()[0])
                        mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, dPort, dAddr, dData);
                    } else if (dPort == 1 && dAddr == 0xb6) {
                        //if (ctYM2612[chipId].getUseEmu()[0])
                        mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[1]) mds.write(YM3438Inst.class, chipId, dPort, dAddr, dData);
                        //if (ctYM2612[chipId].getUseEmu()[2]) mds.write(YM2612mameInst.class, chipId, dPort, dAddr, dData);
                    }
                }
            } else {

//#if DEBUG
                //if (dAddr == 0x2a || dAddr==0x2b) return; // DAC
                //if (dPort == 1) return; // port1
                //if (dAddr == 0x28 && (dData & 7) == 0) return; // Ch1Keyon/off
                //if (dAddr == 0x28 && (dData & 7) == 1) return; // Ch2Keyon/off
                //if (dAddr == 0x28 && (dData & 7) == 2) return; // Ch3Keyon/off
                //if (dAddr == 0x28 && (dData & 7) == 4) return; // Ch4Keyon/off
                //if (dAddr == 0x28 && (dData & 7) == 5) return; // Ch5Keyon/off
                //if (dAddr == 0x28 && (dData & 7) == 6) return; // Ch6Keyon/off
                //if ((dAddr & 0xf0) == 0x30) return; // DTMUL cancel
                //if ((dAddr & 0xf0) == 0x40) return; // TL cancel
                //if ((dAddr & 0xf0) == 0x50) return; // TL cancel
                //if ((dAddr & 0xf0) == 0x60) return; // TL cancel
                //if ((dAddr & 0xf0) == 0x70) return; // TL cancel
                //if ((dAddr & 0xf0) == 0x80) return; // TL cancel
                //if ((dAddr & 0xf0) == 0x90) return; // TL cancel
                //if (dAddr >= 0x00 && dAddr < 0x22) return; // いろいろ cancel
                //if (dAddr >= 0xb4) return; // TL cancel
                //return;
//#endif

                if (ctYM2612[chipId].getUseEmu()[1] && dAddr == 0x21)
                    return; // TESTレジスタへのデータ送信をキャンセルする

                 // エミュを使用する場合のみMDSoundへデータを送る
                //System.err.println("%d:%02X:%02X:%02X", chipId, dPort, dAddr, dData);
                if (ctYM2612[chipId].getUseEmu()[0])
                    mds.write(Ym2612Inst.class, chipId, dPort, dAddr, dData);
                if (ctYM2612[chipId].getUseEmu()[1])
                    mds.write(Ym3438Inst.class, chipId, dPort, dAddr, dData);
                if (ctYM2612[chipId].getUseEmu()[2])
                    mds.write(MameYm2612Inst.class, chipId, dPort, dAddr, dData);
            }
        } else {

             // 実音源(Scci)

            if (scYM2612[chipId] == null) return;

             // PCM(6Ch)だけエミュで発音するとき
            if (ctYM2612[chipId].getRealChipInfo()[0].getOnlyPCMEmulation()) {
                 // アドレスを調べてPCMにはデータを送らない
                if (dPort == 0 && dAddr == 0x2b) {
                    scYM2612[chipId].setRegister(dPort * 0x100 + dAddr, dData);
                } else if (dPort == 0 && dAddr == 0x2a) {
                } else {
                    scYM2612[chipId].setRegister(dPort * 0x100 + dAddr, dData);
                }
            } else {
                 // Scciへデータを送る
                scYM2612[chipId].setRegister(dPort * 0x100 + dAddr, dData);
            }
        }
    }

    public void loadPPSDRV(int chipId, byte[] additionalData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            chipLED.put("PriPPSDRV", 2);
        else
            chipLED.put("SecPPSDRV", 2);

        mds.writePPSDRVPCMData(chipId, additionalData);
    }

    public void writePPSDRV(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            chipLED.put("PriPPSDRV", 2);
        else
            chipLED.put("SecPPSDRV", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        mds.writePPSDRV(chipId, dPort, dAddr, dData, null);
    }

    public void loadPcmPPZ8(int chipId, int bank, int mode, byte[][] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            chipLED.put("PriPPZ8", 2);
        else
            chipLED.put("SecPPZ8", 2);

        mds.WritePPZ8PCMData(chipId, bank, mode, pcmData);
    }

    public void writePPZ8(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            chipLED.put("PriPPZ8", 2);
        else
            chipLED.put("SecPPZ8", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        mds.WritePPZ8(chipId, dPort, dAddr, dData, null);
    }

    public void loadPcmP86(int chipId, int bank, int mode, byte[] pcmData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            chipLED.put("PriP86", 2);
        else
            chipLED.put("SecP86", 2);

        mds.writeP86PCMData(chipId, bank, mode, pcmData);
    }

    public void writeP86(int chipId, int dPort, int dAddr, int dData, EnmModel model) {
        if (model != EnmModel.VirtualModel)
            return;

        if (chipId == 0)
            chipLED.put("PriP86", 2);
        else
            chipLED.put("SecP86", 2);

        if (dPort == -1 && dAddr == -1 && dData == -1)
            return;
        mds.writeP86(chipId, dPort, dAddr, dData, null);
    }

//#endregion

//#region mask

    public void setMaskAY8910(int chipId, int ch, boolean mask) {
        maskPSGChAY8910[chipId][ch] = mask;

        setAY8910Register(chipId, 0x8 + ch, psgRegisterAY8910[chipId][8 + ch], EnmModel.VirtualModel);
        setAY8910Register(chipId, 0x8 + ch, psgRegisterAY8910[chipId][8 + ch], EnmModel.RealModel);
    }

    public void setMaskRF5C164(int chipId, int ch, boolean mask) {
        maskChRF5C164[chipId][ch] = mask;
        if (mask)
            mds.setRf5c164Mask(chipId, ch);
        else
            mds.resetRf5c164Mask(chipId, ch);
    }

    public void setMaskRF5C68(int chipId, int ch, boolean mask) {
        maskChRF5C68[chipId][ch] = mask;
        if (mask)
            mds.setRf5c68Mask(chipId, ch);
        else
            mds.resetRf5c68Mask(chipId, ch);
    }

    public void setMaskSN76489(int chipId, int ch, boolean mask) {
        maskChSN76489[chipId][ch] = mask;
    }

    public void setMaskYM2151(int chipId, int ch, boolean mask, boolean noSend /* = false */) {
        maskFMChYM2151[chipId][ch] = mask;

        if (noSend) return;

        setYM2151Register(chipId, 0, 0x60 + ch, fmRegisterYM2151[chipId][0x60 + ch], EnmModel.VirtualModel, 0, -1);
        setYM2151Register(chipId, 0, 0x68 + ch, fmRegisterYM2151[chipId][0x68 + ch], EnmModel.VirtualModel, 0, -1);
        setYM2151Register(chipId, 0, 0x70 + ch, fmRegisterYM2151[chipId][0x70 + ch], EnmModel.VirtualModel, 0, -1);
        setYM2151Register(chipId, 0, 0x78 + ch, fmRegisterYM2151[chipId][0x78 + ch], EnmModel.VirtualModel, 0, -1);

        setYM2151Register(chipId, 0, 0x60 + ch, fmRegisterYM2151[chipId][0x60 + ch], EnmModel.RealModel, 0, -1);
        setYM2151Register(chipId, 0, 0x68 + ch, fmRegisterYM2151[chipId][0x68 + ch], EnmModel.RealModel, 0, -1);
        setYM2151Register(chipId, 0, 0x70 + ch, fmRegisterYM2151[chipId][0x70 + ch], EnmModel.RealModel, 0, -1);
        setYM2151Register(chipId, 0, 0x78 + ch, fmRegisterYM2151[chipId][0x78 + ch], EnmModel.RealModel, 0, -1);
    }

    public void setMaskYM2203(int chipId, int ch, boolean mask, boolean noSend /*= false*/) {
        maskFMChYM2203[chipId][ch] = mask;

        if (noSend) return;

        int c = ch;
        if (ch < 3) {
            setYM2203Register(chipId, 0x40 + c, fmRegisterYM2203[chipId][0x40 + c], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x44 + c, fmRegisterYM2203[chipId][0x44 + c], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x48 + c, fmRegisterYM2203[chipId][0x48 + c], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x4c + c, fmRegisterYM2203[chipId][0x4c + c], EnmModel.VirtualModel);

            setYM2203Register(chipId, 0x40 + c, fmRegisterYM2203[chipId][0x40 + c], EnmModel.RealModel);
            setYM2203Register(chipId, 0x44 + c, fmRegisterYM2203[chipId][0x44 + c], EnmModel.RealModel);
            setYM2203Register(chipId, 0x48 + c, fmRegisterYM2203[chipId][0x48 + c], EnmModel.RealModel);
            setYM2203Register(chipId, 0x4c + c, fmRegisterYM2203[chipId][0x4c + c], EnmModel.RealModel);
        } else if (ch < 6) {
            setYM2203Register(chipId, 0x08 + c - 3, fmRegisterYM2203[chipId][0x08 + c - 3], EnmModel.VirtualModel);
            setYM2203Register(chipId, 0x08 + c - 3, fmRegisterYM2203[chipId][0x08 + c - 3], EnmModel.RealModel);
        }
    }

    public void setMaskYM2413(int chipId, int ch, boolean mask) {
        maskFMChYM2413[chipId][ch] = mask;

        if (ch < 9) {
            setYM2413Register(chipId, 0x20 + ch, fmRegisterYM2413[chipId][0x20 + ch], EnmModel.VirtualModel);
            setYM2413Register(chipId, 0x20 + ch, fmRegisterYM2413[chipId][0x20 + ch], EnmModel.RealModel);
        } else if (ch < 14) {
            setYM2413Register(chipId, 0x0e, fmRegisterYM2413[chipId][0x0e], EnmModel.VirtualModel);
            setYM2413Register(chipId, 0x0e, fmRegisterYM2413[chipId][0x0e], EnmModel.RealModel);
        }
    }

    public void setMaskYM3526(int chipId, int ch, boolean mask) {
        maskFMChYM3526[chipId][ch] = mask;
    }

    public void setMaskY8950(int chipId, int ch, boolean mask) {
        maskFMChY8950[chipId][ch] = mask;
    }

    public void setMaskYM3812(int chipId, int ch, boolean mask) {
        maskFMChYM3812[chipId][ch] = mask;
    }

    public void setMaskYMF262(int chipId, int ch, boolean mask) {
        maskFMChYMF262[chipId][YMF278BCh[ch]] = mask;
    }

    public void setMaskYMF278B(int chipId, int ch, boolean mask) {
        maskFMChYMF278B[chipId][YMF278BCh[ch]] = mask;
    }

    public void setMaskC140(int chipId, int ch, boolean mask) {
        maskChC140[chipId][ch] = mask;
    }

    public void setMaskPPZ8(int chipId, int ch, boolean mask) {
        maskChPPZ8[chipId][ch] = mask;
    }

    public void setMaskC352(int chipId, int ch, boolean mask) {
        maskChC352[chipId][ch] = mask;
    }

    public void setMaskHuC6280(int chipId, int ch, boolean mask) {
        maskChHuC6280[chipId][ch] = mask;
    }

    public void setMaskSegaPCM(int chipId, int ch, boolean mask) {
        maskChSegaPCM[chipId][ch] = mask;
    }

    public void setMaskQSound(int chipId, int ch, boolean mask) {
        maskChQSound[chipId][ch] = mask;
        if (dicChipsInfo.containsKey(QSoundInst.class)) {
            if (mask)
                mds.setQSoundMask(chipId, ch);
            else
                mds.resetQSoundMask(chipId, ch);
        }
        if (dicChipsInfo.containsKey(CtrQSoundInst.class)) {
            if (mask)
                mds.setQSoundCtrMask(chipId, ch);
            else
                mds.resetQSoundCtrMask(chipId, ch);
        }
    }

    public void setMaskYM2608(int chipId, int ch, boolean mask, boolean noSend/*=false*/) {
        maskFMChYM2608[chipId][ch] = mask;
        if (ch >= 9 && ch < 12) {
            maskFMChYM2608[chipId][2] = mask;
            maskFMChYM2608[chipId][9] = mask;
            maskFMChYM2608[chipId][10] = mask;
            maskFMChYM2608[chipId][11] = mask;
        }

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        if (noSend) return;

        if (ch < 6) {
            setYM2608Register(chipId, p, 0x40 + c, fmRegisterYM2608[chipId][p][0x40 + c], EnmModel.VirtualModel);
            setYM2608Register(chipId, p, 0x44 + c, fmRegisterYM2608[chipId][p][0x44 + c], EnmModel.VirtualModel);
            setYM2608Register(chipId, p, 0x48 + c, fmRegisterYM2608[chipId][p][0x48 + c], EnmModel.VirtualModel);
            setYM2608Register(chipId, p, 0x4c + c, fmRegisterYM2608[chipId][p][0x4c + c], EnmModel.VirtualModel);

            setYM2608Register(chipId, p, 0x40 + c, fmRegisterYM2608[chipId][p][0x40 + c], EnmModel.RealModel);
            setYM2608Register(chipId, p, 0x44 + c, fmRegisterYM2608[chipId][p][0x44 + c], EnmModel.RealModel);
            setYM2608Register(chipId, p, 0x48 + c, fmRegisterYM2608[chipId][p][0x48 + c], EnmModel.RealModel);
            setYM2608Register(chipId, p, 0x4c + c, fmRegisterYM2608[chipId][p][0x4c + c], EnmModel.RealModel);
        } else if (ch < 9) {
            setYM2608Register(chipId, 0, 0x08 + ch - 6, fmRegisterYM2608[chipId][0][0x08 + ch - 6], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x08 + ch - 6, fmRegisterYM2608[chipId][0][0x08 + ch - 6], EnmModel.RealModel);
        } else if (ch < 12) {
            setYM2608Register(chipId, 0, 0x40 + 2, fmRegisterYM2608[chipId][0][0x40 + 2], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x44 + 2, fmRegisterYM2608[chipId][0][0x44 + 2], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x48 + 2, fmRegisterYM2608[chipId][0][0x48 + 2], EnmModel.VirtualModel);
            setYM2608Register(chipId, 0, 0x4c + 2, fmRegisterYM2608[chipId][0][0x4c + 2], EnmModel.VirtualModel);

            setYM2608Register(chipId, 0, 0x40 + 2, fmRegisterYM2608[chipId][0][0x40 + 2], EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x44 + 2, fmRegisterYM2608[chipId][0][0x44 + 2], EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x48 + 2, fmRegisterYM2608[chipId][0][0x48 + 2], EnmModel.RealModel);
            setYM2608Register(chipId, 0, 0x4c + 2, fmRegisterYM2608[chipId][0][0x4c + 2], EnmModel.RealModel);
        }
    }

    public void setMaskYM2610(int chipId, int ch, boolean mask) {
        maskFMChYM2610[chipId][ch] = mask;
        if (ch >= 9 && ch < 12) {
            maskFMChYM2610[chipId][2] = mask;
            maskFMChYM2610[chipId][9] = mask;
            maskFMChYM2610[chipId][10] = mask;
            maskFMChYM2610[chipId][11] = mask;
        }

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        if (ch < 6) {
            setYM2610Register(chipId, p, 0x40 + c, fmRegisterYM2610[chipId][p][0x40 + c], EnmModel.VirtualModel);
            setYM2610Register(chipId, p, 0x44 + c, fmRegisterYM2610[chipId][p][0x44 + c], EnmModel.VirtualModel);
            setYM2610Register(chipId, p, 0x48 + c, fmRegisterYM2610[chipId][p][0x48 + c], EnmModel.VirtualModel);
            setYM2610Register(chipId, p, 0x4c + c, fmRegisterYM2610[chipId][p][0x4c + c], EnmModel.VirtualModel);

            setYM2610Register(chipId, p, 0x40 + c, fmRegisterYM2610[chipId][p][0x40 + c], EnmModel.RealModel);
            setYM2610Register(chipId, p, 0x44 + c, fmRegisterYM2610[chipId][p][0x44 + c], EnmModel.RealModel);
            setYM2610Register(chipId, p, 0x48 + c, fmRegisterYM2610[chipId][p][0x48 + c], EnmModel.RealModel);
            setYM2610Register(chipId, p, 0x4c + c, fmRegisterYM2610[chipId][p][0x4c + c], EnmModel.RealModel);
        } else if (ch < 9) {
            setYM2610Register(chipId,
                    0,
                    0x08 + ch - 6,
                    fmRegisterYM2610[chipId][0][0x08 + ch - 6],
                    EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x08 + ch - 6, fmRegisterYM2610[chipId][0][0x08 + ch - 6], EnmModel.RealModel);
        } else if (ch < 12) {
            setYM2610Register(chipId, 0, 0x40 + 2, fmRegisterYM2610[chipId][0][0x40 + 2], EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x44 + 2, fmRegisterYM2610[chipId][0][0x44 + 2], EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x48 + 2, fmRegisterYM2610[chipId][0][0x48 + 2], EnmModel.VirtualModel);
            setYM2610Register(chipId, 0, 0x4c + 2, fmRegisterYM2610[chipId][0][0x4c + 2], EnmModel.VirtualModel);

            setYM2610Register(chipId, 0, 0x40 + 2, fmRegisterYM2610[chipId][0][0x40 + 2], EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x44 + 2, fmRegisterYM2610[chipId][0][0x44 + 2], EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x48 + 2, fmRegisterYM2610[chipId][0][0x48 + 2], EnmModel.RealModel);
            setYM2610Register(chipId, 0, 0x4c + 2, fmRegisterYM2610[chipId][0][0x4c + 2], EnmModel.RealModel);
        } else if (ch == 12) {
            if (maskFMChYM2610[chipId][12]) {
                setYM2610Register(chipId, 1, 0x00, 1, EnmModel.VirtualModel);
                setYM2610Register(chipId, 1, 0x00, 1, EnmModel.RealModel);
            }
        }
    }

    public void setMaskYM2612(int chipId, int ch, boolean mask) {
        maskFMChYM2612[chipId][ch] = mask;

        int c = (ch < 3) ? ch : (ch - 3);
        int p = (ch < 3) ? 0 : 1;

        setYM2612Register(chipId, p, 0x40 + c, fmRegisterYM2612[chipId][p][0x40 + c], EnmModel.VirtualModel, -1);
        setYM2612Register(chipId, p, 0x44 + c, fmRegisterYM2612[chipId][p][0x44 + c], EnmModel.VirtualModel, -1);
        setYM2612Register(chipId, p, 0x48 + c, fmRegisterYM2612[chipId][p][0x48 + c], EnmModel.VirtualModel, -1);
        setYM2612Register(chipId, p, 0x4c + c, fmRegisterYM2612[chipId][p][0x4c + c], EnmModel.VirtualModel, -1);

        setYM2612Register(chipId, p, 0x40 + c, fmRegisterYM2612[chipId][p][0x40 + c], EnmModel.RealModel, -1);
        setYM2612Register(chipId, p, 0x44 + c, fmRegisterYM2612[chipId][p][0x44 + c], EnmModel.RealModel, -1);
        setYM2612Register(chipId, p, 0x48 + c, fmRegisterYM2612[chipId][p][0x48 + c], EnmModel.RealModel, -1);
        setYM2612Register(chipId, p, 0x4c + c, fmRegisterYM2612[chipId][p][0x4c + c], EnmModel.RealModel, -1);

        if (mask)
            mds.setYm2612Mask(chipId, ch);
        else
            mds.resetYm2612Mask(chipId, ch);
    }

    public void setMaskOKIM6258(int chipId, boolean mask) {
        maskOKIM6258[chipId] = mask;

        writeOKIM6258(chipId, 0, 1, EnmModel.VirtualModel);
        writeOKIM6258(chipId, 0, 1, EnmModel.RealModel);
    }

    public void setMaskOKIM6295(int chipId, int ch, boolean mask) {
        maskOKIM6295[chipId][ch] = mask;
        if (mask)
            mds.setOkiM6295Mask(0, chipId, 1 << ch);
        else
            mds.resetOkiM6295Mask(0, chipId, 1 << ch);
    }

    public OkiM6295.ChannelInfo getOKIM6295Info(int chipId) {
        return mds.getOkiM6295Info(0, chipId);
    }

    public void setNESMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
            case 0:
            case 1:
                nsfAPUmask |= 1 << ch;
                if (nes_apu != null)
                    nes_apu.setMask(nsfAPUmask);
                break;
            case 2:
            case 3:
            case 4:
                nsfDMCmask |= 1 << (ch - 2);
                if (nes_dmc != null)
                    nes_dmc.setMask(nsfDMCmask);
                break;
            }
        }
        mds.setNESMask(chipId, ch);
    }

    public void resetNESMask(int chipId, int ch) {
        if (chipId == 0) {
            switch (ch) {
            case 0:
            case 1:
                nsfAPUmask &= ~(1 << ch);
                if (nes_apu != null)
                    nes_apu.setMask(nsfAPUmask);
                break;
            case 2:
            case 3:
            case 4:
                nsfDMCmask &= ~(1 << (ch - 2));
                if (nes_dmc != null)
                    nes_dmc.setMask(nsfDMCmask);
                break;
            }
        }
        mds.resetNESMask(chipId, ch);
    }

    public void setFDSMask(int chipId) {
        nsfFDSmask |= 1;
        if (nes_fds != null)
            nes_fds.setMask(nsfFDSmask);
        mds.setFDSMask(chipId);
    }

    public void resetFDSMask(int chipId) {
        nsfFDSmask &= ~1;
        if (nes_fds != null)
            nes_fds.setMask(nsfFDSmask);
        mds.resetFDSMask(chipId);
    }

    public void setMMC5Mask(int chipId, int ch) {
        nsfMMC5mask |= 1 << ch;
        if (nes_mmc5 != null)
            nes_mmc5.setMask(nsfMMC5mask);
    }

    public void resetMMC5Mask(int chipId, int ch) {
        nsfMMC5mask &= ~(1 << ch);
        if (nes_mmc5 != null)
            nes_mmc5.setMask(nsfMMC5mask);
    }

    public void setVRC7Mask(int chipId, int ch) {
        nsfVRC7mask |= 1 << ch;
        if (nes_vrc7 != null)
            nes_vrc7.setMask(nsfVRC7mask);
    }

    public void resetVRC7Mask(int chipId, int ch) {
        nsfVRC7mask &= ~(1 << ch);
        if (nes_vrc7 != null)
            nes_vrc7.setMask(nsfVRC7mask);
    }

    public void setK051649Mask(int chipId, int ch) {
        maskChK051649[chipId][ch] = true;
        writeK051649(chipId, (3 << 1) | 1, K051649tKeyOnOff[chipId], EnmModel.VirtualModel);
    }

    public void resetK051649Mask(int chipId, int ch) {
        maskChK051649[chipId][ch] = false;
        writeK051649(chipId, (3 << 1) | 1, K051649tKeyOnOff[chipId], EnmModel.VirtualModel);
    }

    public void setDMGMask(int chipId, int ch) {
        maskChDMG[chipId][ch] = true;
        mds.setGbMask(chipId, ch);
    }

    public void resetDMGMask(int chipId, int ch) {
        maskChDMG[chipId][ch] = false;
        mds.resetGbMask(chipId, ch);
    }

    public void setVRC6Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfVRC6mask |= 1 << ch;
        if (nes_vrc6 != null)
            nes_vrc6.setMask(nsfVRC6mask);
    }

    public void resetVRC6Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfVRC6mask &= ~(1 << ch);
        if (nes_vrc6 != null)
            nes_vrc6.setMask(nsfVRC6mask);
    }

    public void setN163Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfN163mask |= 1 << ch;
        if (nes_n106 != null)
            nes_n106.setMask(nsfN163mask);
    }

    public void resetN163Mask(int chipId, int ch) {
        if (chipId != 0)
            return;
        nsfN163mask &= ~(1 << ch);
        if (nes_n106 != null)
            nes_n106.setMask(nsfN163mask);
    }

//#endregion

//#region fadeout volume

    public void setFadeoutVolYM2151(int chipId, int v) {
        nowYM2151FadeoutVol[chipId] = v;
        for (int c = 0; c < 8; c++) {
            setYM2151Register(chipId, 0, 0x60 + c, fmRegisterYM2151[chipId][0x60 + c], EnmModel.RealModel, 0, -1);
            setYM2151Register(chipId, 0, 0x68 + c, fmRegisterYM2151[chipId][0x68 + c], EnmModel.RealModel, 0, -1);
            setYM2151Register(chipId, 0, 0x70 + c, fmRegisterYM2151[chipId][0x70 + c], EnmModel.RealModel, 0, -1);
            setYM2151Register(chipId, 0, 0x78 + c, fmRegisterYM2151[chipId][0x78 + c], EnmModel.RealModel, 0, -1);
        }
    }

    int[] algVolTbl = new int[] {
            8, 8, 8, 8, 0xa, 0xe, 0xe, 0xf
    };

    private int[] HuC6280CurrentCh = new int[] {
            0, 0
    };

    public Sid SID;

    public void setFadeoutVolYM2203(int chipId, int v) {
        nowYM2203FadeoutVol[chipId] = v;
        for (int c = 0; c < 3; c++) {
            int alg = fmRegisterYM2203[chipId][0xb0 + c] & 0x7;
            if ((algVolTbl[alg] & 1) != 0)
                setYM2203Register(chipId, 0x40 + c, fmRegisterYM2203[chipId][0x40 + c], EnmModel.RealModel);
            if ((algVolTbl[alg] & 4) != 0)
                setYM2203Register(chipId, 0x44 + c, fmRegisterYM2203[chipId][0x44 + c], EnmModel.RealModel);
            if ((algVolTbl[alg] & 2) != 0)
                setYM2203Register(chipId, 0x48 + c, fmRegisterYM2203[chipId][0x48 + c], EnmModel.RealModel);
            if ((algVolTbl[alg] & 8) != 0)
                setYM2203Register(chipId, 0x4c + c, fmRegisterYM2203[chipId][0x4c + c], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolAY8910(int chipId, int v) {
        nowAY8910FadeoutVol[chipId] = v;
        for (int c = 0; c < 3; c++) {
            setAY8910Register(chipId, 0x8 + c, psgRegisterAY8910[chipId][0x8 + c], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolYM2413(int chipId, int v) {
        nowYM2413FadeoutVol[chipId] = v / (128 / 16);
        for (int c = 0; c < 9; c++) {
            setYM2413Register(chipId, 0x30 + c, fmRegisterYM2413[chipId][0x30 + c], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolYM3526(int chipId, int v) {
        nowYM3526FadeoutVol[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setYM3526Register(chipId, 0x40 + c, fmRegisterYM3526[chipId][0x40 + c], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolYM3812(int chipId, int v) {
        nowYM3812FadeoutVol[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setYM3812Register(chipId, 0x40 + c, fmRegisterYM3812[chipId][0x40 + c], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolYMF262(int chipId, int v) {
        nowYMF262FadeoutVol[chipId] = v >> 1;// 0-63 (v range: 0-127)
        for (int c = 0; c < 22; c++) {
            setYMF262Register(chipId, 0, 0x40 + c, fmRegisterYMF262[chipId][0][0x40 + c], EnmModel.RealModel);
            setYMF262Register(chipId, 1, 0x40 + c, fmRegisterYMF262[chipId][1][0x40 + c], EnmModel.RealModel);
        }
    }

    public void setFadeoutVolYM2608(int chipId, int v) {

        nowYM2608FadeoutVol[chipId] = v;

        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                setYM2608Register(chipId, p, 0x40 + c, fmRegisterYM2608[chipId][p][0x40 + c], EnmModel.RealModel);
                setYM2608Register(chipId, p, 0x44 + c, fmRegisterYM2608[chipId][p][0x44 + c], EnmModel.RealModel);
                setYM2608Register(chipId, p, 0x48 + c, fmRegisterYM2608[chipId][p][0x48 + c], EnmModel.RealModel);
                setYM2608Register(chipId, p, 0x4c + c, fmRegisterYM2608[chipId][p][0x4c + c], EnmModel.RealModel);
            }
        }

        // ssg
        setYM2608Register(chipId, 0, 0x08, fmRegisterYM2608[chipId][0][0x08], EnmModel.RealModel);
        setYM2608Register(chipId, 0, 0x09, fmRegisterYM2608[chipId][0][0x09], EnmModel.RealModel);
        setYM2608Register(chipId, 0, 0x0a, fmRegisterYM2608[chipId][0][0x0a], EnmModel.RealModel);

        // rhythm
        setYM2608Register(chipId, 0, 0x11, fmRegisterYM2608[chipId][0][0x11], EnmModel.RealModel);

        // adpcm
        setYM2608Register(chipId, 1, 0x0b, fmRegisterYM2608[chipId][1][0x0b], EnmModel.RealModel);
    }

    public void setFadeoutVolYM2610(int chipId, int v) {
        nowYM2610FadeoutVol[chipId] = v;
        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                setYM2610Register(chipId, p, 0x40 + c, fmRegisterYM2610[chipId][p][0x40 + c], EnmModel.RealModel);
                setYM2610Register(chipId, p, 0x44 + c, fmRegisterYM2610[chipId][p][0x44 + c], EnmModel.RealModel);
                setYM2610Register(chipId, p, 0x48 + c, fmRegisterYM2610[chipId][p][0x48 + c], EnmModel.RealModel);
                setYM2610Register(chipId, p, 0x4c + c, fmRegisterYM2610[chipId][p][0x4c + c], EnmModel.RealModel);
            }
        }

        // ssg
        setYM2610Register(chipId, 0, 0x08, fmRegisterYM2610[chipId][0][0x08], EnmModel.RealModel);
        setYM2610Register(chipId, 0, 0x09, fmRegisterYM2610[chipId][0][0x09], EnmModel.RealModel);
        setYM2610Register(chipId, 0, 0x0a, fmRegisterYM2610[chipId][0][0x0a], EnmModel.RealModel);

        // rhythm
        setYM2610Register(chipId, 0, 0x11, fmRegisterYM2610[chipId][0][0x11], EnmModel.RealModel);

        // adpcm
        setYM2610Register(chipId, 1, 0x0b, fmRegisterYM2610[chipId][1][0x0b], EnmModel.RealModel);
    }

    public void setFadeoutVolYM2612(int chipId, int v) {
        nowYM2612FadeoutVol[chipId] = v;
        for (int p = 0; p < 2; p++) {
            for (int c = 0; c < 3; c++) {
                setYM2612Register(chipId, p, 0x40 + c, fmRegisterYM2612[chipId][p][0x40 + c], EnmModel.RealModel, -1);
                setYM2612Register(chipId, p, 0x44 + c, fmRegisterYM2612[chipId][p][0x44 + c], EnmModel.RealModel, -1);
                setYM2612Register(chipId, p, 0x48 + c, fmRegisterYM2612[chipId][p][0x48 + c], EnmModel.RealModel, -1);
                setYM2612Register(chipId, p, 0x4c + c, fmRegisterYM2612[chipId][p][0x4c + c], EnmModel.RealModel, -1);
            }
        }
    }

//#endregion

//#region sync wait

    public void setYM2151SyncWait(int chipId, int wait) {
        if (scYM2151[chipId] != null && ctYM2151[chipId].getRealChipInfo()[0].getUseWait()) {
            scYM2151[chipId].setRegister(-1, (int) (wait * (ctYM2151[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void setYM2608SyncWait(int chipId, int wait) {
        if (scYM2608[chipId] != null && ctYM2608[chipId].getRealChipInfo()[0].getUseWait()) {
            scYM2608[chipId].setRegister(-1, (int) (wait * (ctYM2608[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void setYM2612SyncWait(int chipId, int wait) {
        if (scYM2612[chipId] != null && ctYM2612[chipId].getRealChipInfo()[0].getUseWait()) {
            scYM2612[chipId].setRegister(-1, (int) (wait * (ctYM2612[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void sendDataYM2151(int chipId, EnmModel model) {
        if (model == EnmModel.VirtualModel)
            return;

        if (scYM2151[chipId] != null && ctYM2151[chipId].getRealChipInfo()[0].getUseWait()) {
            realChip.SendData();
            while (!scYM2151[chipId].isBufferEmpty()) {
            }
        }
    }

    public void sendDataYM2608(int chipId, EnmModel model) {
        if (model == EnmModel.VirtualModel)
            return;

        if (scYM2608[chipId] != null && ctYM2608[chipId].getRealChipInfo()[0].getUseWait()) {
            realChip.SendData();
            while (!scYM2608[chipId].isBufferEmpty()) {
            }
        }
    }

    public int getYM2151Clock(int chipId) {
        if (scYM2151[chipId] == null)
            return -1;

        return scYM2151[chipId].dClock;
    }

//#endregion

//#region register

    public void setSN76489RegisterGGpanning(int chipId, int dData, EnmModel model) {
        if (ctSN76489 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriDCSG", 2);
        else
            chipLED.put("SecDCSG", 2);

        if (model == EnmModel.RealModel) {
            if (ctSN76489[chipId].getUseReal()[0]) {
                if (scSN76489[chipId] == null) {
                }
            }
        } else {
            if (!ctSN76489[chipId].getUseReal()[0]) {
                if (ctSN76489[chipId].getUseEmu()[0])
                    mds.writeSn76489GGPanning(chipId, dData);
                else if (ctSN76489[chipId].getUseEmu()[1])
                    mds.writeSn76496GGPanning(chipId, dData);
                sn76489RegisterGGPan[chipId] = dData;
            }
        }
    }

    public void setSN76489Register(int chipId, int dData, EnmModel model) {
        if (ctSN76489 == null)
            return;

        if (chipId == 0)
            chipLED.put("PriDCSG", 2);
        else
            chipLED.put("SecDCSG", 2);

        writeSN76489(chipId, dData);

        if ((dData & 0x10) != 0) {
            if (LatchedRegister[chipId] != 0 && LatchedRegister[chipId] != 2 && LatchedRegister[chipId] != 4 &&
                    LatchedRegister[chipId] != 6) {
                sn76489Vol[chipId][(dData & 0x60) >> 5][0] = (15 - (dData & 0xf)) *
                        ((sn76489RegisterGGPan[chipId] >> (((dData & 0x60) >> 5) + 4)) &
                                0x1);
                sn76489Vol[chipId][(dData & 0x60) >> 5][1] = (15 - (dData & 0xf)) *
                        ((sn76489RegisterGGPan[chipId] >> ((dData & 0x60) >> 5)) & 0x1);

                int v = dData & 0xf;
                v = v + nowSN76489FadeoutVol[chipId];
                v = maskChSN76489[chipId][(dData & 0x60) >> 5] ? 15 : v;
                v = Math.min(v, 15);
                dData = (dData & 0xf0) | v;
            }
        }

        if (model == EnmModel.RealModel) {
            if (ctSN76489[chipId].getUseReal()[0]) {
                if (scSN76489[chipId] == null)
                    return;
                scSN76489[chipId].setRegister(0, dData);
            }
        } else {
            if (!ctSN76489[chipId].getUseReal()[0]) {
                if (ctSN76489[chipId].getUseEmu()[0])
                    mds.write(Sn76489Inst.class, chipId, 0, 0, dData);
                else if (ctSN76489[chipId].getUseEmu()[1])
                    mds.write(Sn76496Inst.class, chipId, 0, 0, dData);
            }
        }
    }

    public void setSN76489SyncWait(int chipId, int wait) {
        if (scSN76489 != null && ctSN76489[chipId].getRealChipInfo()[0].getUseWait()) {
            scSN76489[chipId].setRegister(-1, (int) (wait * (ctSN76489[chipId].getRealChipInfo()[0].getUseWaitBoost() ? 2.0 : 1.0)));
        }
    }

    public void setFadeoutVolSN76489(int chipId, int v) {
        nowSN76489FadeoutVol[chipId] = (v & 0x78) >> 3;
        for (int c = 0; c < 4; c++) {

            setSN76489Register(chipId, 0x90 + (c << 5) + sn76489Register[chipId][1 + (c << 1)], EnmModel.RealModel);
        }
    }

//#endregion

//#region write

    private void writeSN76489(int chipId, int data) {
        if ((data & 0x80) != 0) {
            // Latch/data byte %1 cc t dddd
            LatchedRegister[chipId] = (data >> 4) & 0x07;
            sn76489Register[chipId][LatchedRegister[chipId]] = (sn76489Register[chipId][LatchedRegister[chipId]] &
                    0x3f0) // zero low 4 bits
                    | (data & 0xf); // and replace with data
        } else {
            // data byte %0 - dddddd
            if ((LatchedRegister[chipId] % 2) == 0 && (LatchedRegister[chipId] < 5))
                // Tone register
                sn76489Register[chipId][LatchedRegister[chipId]] = (sn76489Register[chipId][LatchedRegister[chipId]] &
                        0x00f) // zero high 6 bits
                        | ((data & 0x3f) << 4); // and replace with data
            else
                // Other register
                sn76489Register[chipId][LatchedRegister[chipId]] = data & 0x0f; // Replace with data
        }
        switch (LatchedRegister[chipId]) {
        case 0:
        case 2:
        case 4: // Tone channels
            //if (sn76489Register[chipId][LatchedRegister[chipId]] == 0)
            // sn76489Register[chipId][LatchedRegister[chipId]] = 1; // Zero frequency changed to 1 to avoid div/0
            break;
        case 6: // Noise
            NoiseFreq[chipId] = 0x10 << (sn76489Register[chipId][6] & 0x3); // set noise signal generator frequency
            break;
        }
    }

    public void writeRF5C68PCMData(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriRF5C68", 2);
        else
            chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteRf5c68PCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public void writeRF5C68(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriRF5C68", 2);
        else
            chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(Rf5c68Inst.class, chipId, 0, adr, data);
        }
    }

    public void writeRF5C68MemW(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriRF5C68", 2);
        else
            chipLED.put("SecRF5C68", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteRf5c68MemW(chipId, offset, data);
    }

    public void writeRF5C164PCMData(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriRF5C", 2);
        else
            chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel)
            mds.writeScdPcmPCMData(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public void writeNESPCMData(int chipId, int stAdr, int dataSize, byte[] vgmBuf, int vgmAdr, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriNES", 2);
        else
            chipLED.put("SecNES", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteNESRam(chipId, stAdr, dataSize, vgmBuf, vgmAdr);
    }

    public void writeRF5C164(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriRF5C", 2);
        else
            chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(ScdPcmInst.class, chipId, 0, adr, data);
        }
    }

    public void writeRF5C164MemW(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriRF5C", 2);
        else
            chipLED.put("SecRF5C", 2);

        if (model == EnmModel.VirtualModel)
            mds.writeScdPcmMemW(chipId, offset, data);
    }

    public void writePWM(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriPWM", 2);
        else
            chipLED.put("SecPWM", 2);

        if (model == EnmModel.VirtualModel)
            mds.write(PwmInst.class, chipId, 0, adr, data);
    }

    public void writeK051649(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriK051649", 2);
        else
            chipLED.put("SecK051649", 2);

        if ((adr & 1) != 0) {
            if ((adr >> 1) == 3) { // keyonoff
                K051649tKeyOnOff[chipId] = (byte) data;
                data &= maskChK051649[chipId][0] ? 0xfe : 0xff;
                data &= maskChK051649[chipId][1] ? 0xfd : 0xff;
                data &= maskChK051649[chipId][2] ? 0xfb : 0xff;
                data &= maskChK051649[chipId][3] ? 0xf7 : 0xff;
                data &= maskChK051649[chipId][4] ? 0xef : 0xff;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctK051649[chipId].getUseReal()[0]) {
                mds.write(K051649Inst.class, chipId, 0, adr, data);

                // レジスタのデータを退避
                scc_k051649.write(chipId, 0, adr, data);
            }
        } else {
            if (scK051649[chipId] == null)
                return;

            // レジスタのデータを退避
            scc_k051649.write(chipId, 0, adr, data);

            if ((adr & 1) == 0) {
                sccR_port = (adr >> 1);
                sccR_offset = data;
            } else {
                sccR_dat = data;

                switch (sccR_port) {
                case 0x00:
                    sccR_offset += 0x00;
                    break;
                case 0x01:
                    sccR_offset += 0x80;
                    break;
                case 0x02:
                    sccR_offset += 0x8a;
                    break;
                case 0x03:
                    sccR_offset += 0x8f;
                    break;
                }

                scK051649[chipId].setRegister(setting.getDebug_SCCbaseAddress() | sccR_offset, sccR_dat);
            }
        }
    }

    public void softResetK051649(int chipId, EnmModel model) {
        // 全チャネルボリュームzero
        for (int i = 0; i < 5; i++) {
            writeK051649(chipId, (0x00 << 1) + 0, i, model);
            writeK051649(chipId, (0x02 << 1) + 1, 0x00, model);
            writeK051649(chipId, (0x00 << 1) + 0, i, model);
            writeK051649(chipId, (0x03 << 1) + 1, 0x00, model);
        }
    }

    public void writeK053260(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriK053260", 2);
        else
            chipLED.put("SecK053260", 2);

        if (model == EnmModel.VirtualModel)
            mds.write(K053260Inst.class, chipId, 0, adr, data);
    }

    public void writeK054539(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriK054539", 2);
        else
            chipLED.put("SecK054539", 2);

        if (model == EnmModel.VirtualModel)
            mds.write(K054539Inst.class, chipId, 0, adr, data);
    }

    public void writeK053260PCMData(int chipId,
                                    int romSize,
                                    int dataStart,
                                    int dataLength,
                                    byte[] romData,
                                    int srcStartAdr,
                                    EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriK053260", 2);
        else
            chipLED.put("SecK053260", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteK053260PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeK054539PCMData(int chipId,
                                    int romSize,
                                    int dataStart,
                                    int dataLength,
                                    byte[] romData,
                                    int srcStartAdr,
                                    EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriK054539", 2);
        else
            chipLED.put("SecK054539", 2);

        if (model == EnmModel.VirtualModel)
            mds.writeK054539PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeQSoundPCMData(int chipId,
                                   int romSize,
                                   int dataStart,
                                   int dataLength,
                                   byte[] romData,
                                   int srcStartAdr,
                                   EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriQsnd", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteQSoundCtrPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeX1_010PCMData(int chipId,
                                   int romSize,
                                   int dataStart,
                                   int dataLength,
                                   byte[] romData,
                                   int srcStartAdr,
                                   EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriX1010", 2);
        else
            chipLED.put("SecX1010", 2);

        if (model == EnmModel.VirtualModel)
            mds.writeX1_010PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeC352(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriC352", 2);
        else
            chipLED.put("SecC352", 2);

        if (adr < pcmRegisterC352[chipId].length)
            pcmRegisterC352[chipId][adr] = data;
        int c = adr / 8;
        if (adr < 0x100 && (adr % 8) == 3 && maskChC352[chipId][adr / 8]) {
            data &= 0xbfff;
        }
        if (model == EnmModel.VirtualModel)
            mds.write(C352Inst.class, chipId, 0, adr, data);
    }

    public int[] readC352(int chipId) {
        return mds.ReadC352Flag(chipId);
    }

    public void writeC352PCMData(int chipId,
                                 int romSize,
                                 int dataStart,
                                 int dataLength,
                                 byte[] romData,
                                 int srcStartAdr,
                                 EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriC352", 2);
        else
            chipLED.put("SecC352", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteC352PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeGA20PCMData(int chipId,
                                 int romSize,
                                 int dataStart,
                                 int dataLength,
                                 byte[] romData,
                                 int srcStartAdr,
                                 EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriGA20", 2);
        else
            chipLED.put("SecGA20", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteIremga20PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeOKIM6258(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriOKI5", 2);
        else
            chipLED.put("SecOKI5", 2);

        if (port == 0x00) {
            if ((data & 0x2) != 0)
                okim6258Keyon[chipId] = true;

            if (maskOKIM6258[chipId]) {
                if ((data & 0x2) != 0)
                    return;
            }
        }
        if (port == 0x1) {
            if (maskOKIM6258[chipId])
                return;
        }

        if (model == EnmModel.VirtualModel) {
            mds.write(OkiM6258Inst.class, chipId, 0, port, data);
        }
    }

    public void writeOKIM6295(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriOKI9", 2);
        else
            chipLED.put("SecOKI9", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(OkiM6295Inst.class, chipId, 0, port, data);
//Debug.printf("chipId=%d Port=%x data=%x", chipId, port, data);
        }
    }

    public void writeSAA1099(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriSAA", 2);
        else
            chipLED.put("SecSAA", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(Saa1099Inst.class, chipId, 0, port, data);
        }
    }

    public void writeWSwan(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriWSW", 2);
        else
            chipLED.put("SecWSW", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(WsAudioInst.class, chipId, 0, port, data);
        }
    }

    public void writeWSwanMem(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriWSW", 2);
        else
            chipLED.put("SecWSW", 2);

        if (model == EnmModel.VirtualModel) {
            mds.writeWsAudioMem(chipId, port, data);
        }
    }

    public void writePOKEY(int chipId, int port, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriPOK", 2);
        else
            chipLED.put("SecPOK", 2);

        if (model == EnmModel.VirtualModel) {
            mds.write(PokeyInst.class, chipId, 0, port, data);
        }
    }

    public void writeOKIM6295PCMData(int chipId,
                                     int romSize,
                                     int dataStart,
                                     int dataLength,
                                     byte[] romData,
                                     int srcStartAdr,
                                     EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriOKI9", 2);
        else
            chipLED.put("SecOKI9", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteOkiM6295PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeMultiPCMPCMData(int chipId,
                                     int romSize,
                                     int dataStart,
                                     int dataLength,
                                     byte[] romData,
                                     int srcStartAdr,
                                     EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriMPCM", 2);
        else
            chipLED.put("SecMPCM", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteMultiPCMPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeYmF271PCMData(int chipId,
                                   int romSize,
                                   int dataStart,
                                   int dataLength,
                                   byte[] romData,
                                   int srcStartAdr,
                                   EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriOPX", 2);
        else
            chipLED.put("SecOPX", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteYmf271PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeYmF278BPCMData(int chipId,
                                    int romSize,
                                    int dataStart,
                                    int dataLength,
                                    byte[] romData,
                                    int srcStartAdr,
                                    EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriOPL4", 2);
        else
            chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteYmF278bPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeYmF278BPCMRAMData(int chipId,
                                       int romSize,
                                       int dataStart,
                                       int dataLength,
                                       byte[] romData,
                                       int srcStartAdr,
                                       EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriOPL4", 2);
        else
            chipLED.put("SecOPL4", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteYmF278bPCMramData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeYmZ280BPCMData(int chipId,
                                    int romSize,
                                    int dataStart,
                                    int dataLength,
                                    byte[] romData,
                                    int srcStartAdr,
                                    EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriYMZ", 2);
        else
            chipLED.put("SecYMZ", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteYmZ280bPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeY8950PCMData(int chipId,
                                  int romSize,
                                  int dataStart,
                                  int dataLength,
                                  byte[] romData,
                                  int srcStartAdr,
                                  EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriY8950", 2);
        else
            chipLED.put("SecY8950", 2);

        if (model == EnmModel.VirtualModel)
            mds.WriteY8950PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
    }

    public void writeSEGAPCM(int chipId, int offset, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriSPCM", 2);
        else
            chipLED.put("SecSPCM", 2);

        if ((model == EnmModel.VirtualModel && (ctSEGAPCM[chipId] == null || !ctSEGAPCM[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scSEGAPCM != null && scSEGAPCM[chipId] != null))) {
            pcmRegisterSEGAPCM[chipId][offset & 0x1ff] = (byte) data;

            if ((offset & 0x87) == 0x86) {
                int ch = (offset >> 3) & 0xf;
                if ((data & 0x01) == 0)
                    pcmKeyOnSEGAPCM[chipId][ch] = true;
                data = maskChSegaPCM[chipId][ch] ? data | 0x01 : data;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (!ctSEGAPCM[chipId].getUseReal()[0])
                mds.write(SegaPcmInst.class, chipId, 0, offset, data);
//Debug.printf("chipId=%d offset=%x data=%x ", chipId, offset, data);
        } else {
            if (scSEGAPCM != null && scSEGAPCM[chipId] != null)
                scSEGAPCM[chipId].setRegister(offset, data);
        }
    }

    public void writeSEGAPCMPCMData(int chipId,
                                    int romSize,
                                    int dataStart,
                                    int dataLength,
                                    byte[] romData,
                                    int srcStartAdr,
                                    EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriSPCM", 2);
        else
            chipLED.put("SecSPCM", 2);

        if (model == EnmModel.VirtualModel) {
            mds.WriteSegaPcmPCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
        } else {
            if (scSEGAPCM != null && scSEGAPCM[chipId] != null) {
                // スタートアドレス設定
                scSEGAPCM[chipId].setRegister(0x10000, dataStart);
                scSEGAPCM[chipId].setRegister(0x10001, dataStart >> 8);
                scSEGAPCM[chipId].setRegister(0x10002, dataStart >> 16);
                // データ転送
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    scSEGAPCM[chipId].setRegister(0x10004, romData[srcStartAdr + cnt]);
                }
                scSEGAPCM[chipId].setRegister(0x10006, romSize);

                realChip.SendData();
            }
        }
    }

    public void writeYm2151Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2151 != null && scYM2151[chipId] != null) {
                scYM2151[chipId].dClock = scYM2151[chipId].setMasterClock(clock);
            }
        }
    }

    public void writeYm2203Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2203 != null && scYM2203[chipId] != null) {
                if (scYM2203[chipId] instanceof RC86ctlSoundChip) {
//                    Nc86ctl.ChipType ct = ((RC86ctlSoundChip) scYM2203[chipId]).ChipType;
//                    // OPNA/OPN3Lが選ばれている場合は周波数を2倍にする
//                    if (ct == Nc86ctl.ChipType.CHIP_OPN3L || ct == Nc86ctl.ChipType.CHIP_OPNA) {
//                        clock *= 2;
//                    }
                }
                scYM2203[chipId].dClock = scYM2203[chipId].setMasterClock(clock);
            }
        }
    }

    public void writeAY8910Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scAY8910 != null && scAY8910[chipId] != null) {
                if (scAY8910[chipId] instanceof RC86ctlSoundChip) {
//                    Nc86ctl.ChipType ct = ((RC86ctlSoundChip) scAY8910[chipId]).ChipType;
//                    // YM2149が選ばれている場合は周波数を2倍にする
//                    if (ct == Nc86ctl.ChipType.CHIP_YM2149) {
//                        clock *= 2;
//                    }
                }
                scAY8910[chipId].dClock = scAY8910[chipId].setMasterClock(clock);
            }
        }
    }

    public void writeYm2608Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2608 != null && scYM2608[chipId] != null) {
                scYM2608[chipId].dClock = scYM2608[chipId].setMasterClock(clock);
            }
        }
    }

    public void writeYm3526Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM3526 != null && scYM3526[chipId] != null) {
//                if (scYM3526[chipId] instanceof RC86ctlSoundChip
//                        && ((RC86ctlSoundChip) scYM3526[chipId]).ChipType == Nc86ctl.ChipType.CHIP_OPL3) clock *= 4;
//                scYM3526[chipId].dClock = scYM3526[chipId].SetMasterClock((int) clock);
            }
        }
    }

    public void writeYm3812Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM3812 != null && scYM3812[chipId] != null) {
                scYM3812[chipId].dClock = scYM3812[chipId].setMasterClock(clock);
            }
        }
    }

    public void writeYmF262Clock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYMF262 != null && scYMF262[chipId] != null) {
                scYMF262[chipId].dClock = scYMF262[chipId].setMasterClock(clock);
            }
        }
    }

    public void setYM2203SSGVolume(int chipId, int vol, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2203 != null && scYM2203[chipId] != null) {
                scYM2203[chipId].setSSGVolume((byte) vol);
            }
        }
    }

    public void setYM2608SSGVolume(int chipId, int vol, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scYM2608 != null && scYM2608[chipId] != null) {
                scYM2608[chipId].setSSGVolume((byte) vol);
            }
        }
    }

    public void writeSEGAPCMClock(int chipId, int clock, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scSEGAPCM != null && scSEGAPCM[chipId] != null) {
                scSEGAPCM[chipId].setRegister(0x10005, clock);
            }
        }
    }

    public void writeC140(int chipId, int adr, int data, EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriC140", 2);
        else
            chipLED.put("SecC140", 2);

        if ((model == EnmModel.VirtualModel && (ctC140[chipId] == null || !ctC140[chipId].getUseReal()[0])) ||
                (model == EnmModel.RealModel && (scC140 != null && scC140[chipId] != null))) {
            pcmRegisterC140[chipId][adr] = (byte) data;
            int ch = adr >> 4;
            switch (adr & 0xf) {
            case 0x05:
                if ((data & 0x80) != 0) {
                    pcmKeyOnC140[chipId][ch] = true;
                    data = maskChC140[chipId][ch] ? data & 0x7f : data;
                }
                break;
            }
        }

        if (model == EnmModel.VirtualModel) {
            if (ctC140[chipId] == null || !ctC140[chipId].getUseReal()[0])
                mds.write(C140Inst.class, chipId, 0, adr, data);
        } else {
            if (scC140 != null && scC140[chipId] != null)
                scC140[chipId].setRegister(adr, data);
        }
    }

    public void writeC140PCMData(int chipId,
                                 int romSize,
                                 int dataStart,
                                 int dataLength,
                                 byte[] romData,
                                 int srcStartAdr,
                                 EnmModel model) {
        if (chipId == 0)
            chipLED.put("PriC140", 2);
        else
            chipLED.put("SecC140", 2);

        if (model == EnmModel.VirtualModel)
            mds.writeC140PCMData(chipId, romSize, dataStart, dataLength, romData, srcStartAdr);
        else {
            if (scC140 != null && scC140[chipId] != null) {
                // スタートアドレス設定
                scC140[chipId].setRegister(0x10000, dataStart);
                scC140[chipId].setRegister(0x10001, dataStart >> 8);
                scC140[chipId].setRegister(0x10002, dataStart >> 16);
                // データ転送
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    scC140[chipId].setRegister(0x10004, romData[srcStartAdr + cnt]);
                }
                // scC140[chipId].setRegister(0x10006, (int)ROMSize);

                realChip.SendData();
            }
        }
    }

    public void writeC140Type(int chipId, C140.Type type, EnmModel model) {
        if (model == EnmModel.VirtualModel) {
        } else {
            if (scC140 != null && scC140[chipId] != null) {
                switch (type) {
                case SYSTEM2:
                    scC140[chipId].setRegister(0x10008, 0);
                    break;
                case SYSTEM21:
                    scC140[chipId].setRegister(0x10008, 1);
                    break;
                case ASIC219:
                    scC140[chipId].setRegister(0x10008, 2);
                    break;
                }
            }
        }
    }

//#endregion

//#region volume

    //
    // 鍵盤のボリューム表示のため音量を取得する
    //

    /**
     * ボリューム情報の更新
     */
    public void updateVol() {
        volF--;
        if (volF > 0)
            return;

        volF = 1;

        for (int chipId = 0; chipId < 2; chipId++) {
            for (int i = 0; i < 9; i++) {
                if (fmVolYM2612[chipId][i] > 0) {
                    fmVolYM2612[chipId][i] -= 50;
                    if (fmVolYM2612[chipId][i] < 0)
                        fmVolYM2612[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2612[chipId][i] > 0) {
                    fmCh3SlotVolYM2612[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2612[chipId][i] < 0)
                        fmCh3SlotVolYM2612[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 8; i++) {
                if (fmVolYM2151[chipId][i] > 0) {
                    fmVolYM2151[chipId][i] -= 50;
                    if (fmVolYM2151[chipId][i] < 0)
                        fmVolYM2151[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 9; i++) {
                if (fmVolYM2608[chipId][i] > 0) {
                    fmVolYM2608[chipId][i] -= 50;
                    if (fmVolYM2608[chipId][i] < 0)
                        fmVolYM2608[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2608[chipId][i] > 0) {
                    fmCh3SlotVolYM2608[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2608[chipId][i] < 0)
                        fmCh3SlotVolYM2608[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 6; i++) {
                if (fmVolYM2608Rhythm[chipId][i][0] > 0) {
                    fmVolYM2608Rhythm[chipId][i][0] -= 50;
                    if (fmVolYM2608Rhythm[chipId][i][0] < 0)
                        fmVolYM2608Rhythm[chipId][i][0] = 0;
                }
                if (fmVolYM2608Rhythm[chipId][i][1] > 0) {
                    fmVolYM2608Rhythm[chipId][i][1] -= 50;
                    if (fmVolYM2608Rhythm[chipId][i][1] < 0)
                        fmVolYM2608Rhythm[chipId][i][1] = 0;
                }
            }

            if (fmVolYM2608Adpcm[chipId][0] > 0) {
                fmVolYM2608Adpcm[chipId][0] -= 50;
                if (fmVolYM2608Adpcm[chipId][0] < 0)
                    fmVolYM2608Adpcm[chipId][0] = 0;
            }
            if (fmVolYM2608Adpcm[chipId][1] > 0) {
                fmVolYM2608Adpcm[chipId][1] -= 50;
                if (fmVolYM2608Adpcm[chipId][1] < 0)
                    fmVolYM2608Adpcm[chipId][1] = 0;
            }

            for (int i = 0; i < 9; i++) {
                if (fmVolYM2610[chipId][i] > 0) {
                    fmVolYM2610[chipId][i] -= 50;
                    if (fmVolYM2610[chipId][i] < 0)
                        fmVolYM2610[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2610[chipId][i] > 0) {
                    fmCh3SlotVolYM2610[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2610[chipId][i] < 0)
                        fmCh3SlotVolYM2610[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 6; i++) {
                if (fmVolYM2610Rhythm[chipId][i][0] > 0) {
                    fmVolYM2610Rhythm[chipId][i][0] -= 50;
                    if (fmVolYM2610Rhythm[chipId][i][0] < 0)
                        fmVolYM2610Rhythm[chipId][i][0] = 0;
                }
                if (fmVolYM2610Rhythm[chipId][i][1] > 0) {
                    fmVolYM2610Rhythm[chipId][i][1] -= 50;
                    if (fmVolYM2610Rhythm[chipId][i][1] < 0)
                        fmVolYM2610Rhythm[chipId][i][1] = 0;
                }
            }

            if (fmVolYM2610Adpcm[chipId][0] > 0) {
                fmVolYM2610Adpcm[chipId][0] -= 50;
                if (fmVolYM2610Adpcm[chipId][0] < 0)
                    fmVolYM2610Adpcm[chipId][0] = 0;
            }
            if (fmVolYM2610Adpcm[chipId][1] > 0) {
                fmVolYM2610Adpcm[chipId][1] -= 50;
                if (fmVolYM2610Adpcm[chipId][1] < 0)
                    fmVolYM2610Adpcm[chipId][1] = 0;
            }

            for (int i = 0; i < 6; i++) {
                if (fmVolYM2203[chipId][i] > 0) {
                    fmVolYM2203[chipId][i] -= 50;
                    if (fmVolYM2203[chipId][i] < 0)
                        fmVolYM2203[chipId][i] = 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (fmCh3SlotVolYM2203[chipId][i] > 0) {
                    fmCh3SlotVolYM2203[chipId][i] -= 50;
                    if (fmCh3SlotVolYM2203[chipId][i] < 0)
                        fmCh3SlotVolYM2203[chipId][i] = 0;
                }
            }
        }

    }

    public int[] getYM2151Volume(int chipId) {
        return fmVolYM2151[chipId];
    }

    public int[] getYM2203Volume(int chipId) {
        return fmVolYM2203[chipId];
    }

    public int[] getYM2608Volume(int chipId) {
        return fmVolYM2608[chipId];
    }

    public int[] getYM2610Volume(int chipId) {
        return fmVolYM2610[chipId];
    }

    public int[] getYM2612Volume(int chipId) {
        return fmVolYM2612[chipId];
    }

    public int[] getYM2203Ch3SlotVolume(int chipId) {
        // if (ctYM2612.UseScci)
        // {
        return fmCh3SlotVolYM2203[chipId];
        // }
        // return mds.readFMCh3SlotVolume();
    }

    public int[] getYM2608Ch3SlotVolume(int chipId) {
        // if (ctYM2612.UseScci)
        // {
        return fmCh3SlotVolYM2608[chipId];
        // }
        // return mds.readFMCh3SlotVolume();
    }

    public int[] getYM2610Ch3SlotVolume(int chipId) {
        // if (ctYM2612.UseScci)
        // {
        return fmCh3SlotVolYM2610[chipId];
        // }
        // return mds.readFMCh3SlotVolume();
    }

    public int[] getYM2612Ch3SlotVolume(int chipId) {
        return fmCh3SlotVolYM2612[chipId];
    }

    public int[][] getYM2608RhythmVolume(int chipId) {
        return fmVolYM2608Rhythm[chipId];
    }

    public int[][] getYM2610RhythmVolume(int chipId) {
        return fmVolYM2610Rhythm[chipId];
    }

    public int[] getYM2608AdpcmVolume(int chipId) {
        return fmVolYM2608Adpcm[chipId];
    }

    public int[] getYM2610AdpcmVolume(int chipId) {
        return fmVolYM2610Adpcm[chipId];
    }

    public int[][] getPSGVolume(int chipId) {

        return sn76489Vol[chipId];

    }

    public DeviceInfo.TrackInfo[] getVRC6Register(int chipId) {
        if (nes_vrc6 == null)
            return null;
        if (chipId != 0)
            return null;

        return nes_vrc6.getTracksInfo();
    }

    public byte[] getVRC7Register(int chipId) {
        if (nes_vrc7 == null) return null;
        if (chipId != 0) return null;

        return nes_vrc7.getRegs();
    }

    public DeviceInfo.TrackInfo[] getN106Register(int chipId) {
        if (nes_n106 == null)
            return null;
        if (chipId != 0)
            return null;

        return nes_n106.getTracksInfo();
    }

//    public int x68Sound_TotalVolume(int vol, enmModel model) {
//        if (model == enmModel.RealModel) return 0;
//
//        return x68Sound.X68Sound_TotalVolume(vol);
//    }
//
//    public int x68Sound_GetPcm(short[] buf, int len, enmModel model,
//                               Runnable<Runnable, boolean oneFrameProc =null) {
//        if (model == enmModel.RealModel) return 0;
//        return x68Sound.X68Sound_GetPcm(buf, len, oneFrameProc);
//    }
//
//    public void x68Sound_Free(enmModel model) {
//        if (model == enmModel.RealModel) return;
//        x68Sound.X68Sound_Free();
//    }
//
//    public void x68Sound_OpmInt(Runnable p, enmModel model) {
//        if (model == enmModel.RealModel) return;
//        x68Sound.X68Sound_OpmInt(p);
//    }
//
//    public int x68Sound_StartPcm(int sampleRate, int v1, int v2, int pcmbuf,
//                                 enmModel model) {
//        if (model == enmModel.RealModel) return 0;
//        return x68Sound.X68Sound_StartPcm(sampleRate, v1, v2, pcmbuf);
//    }
//
//    public int x68Sound_Start(int sampleRate, int v1, int v2, int betw, int
//            pcmbuf, int late, double v3, enmModel model) {
//        if (model == enmModel.RealModel) return 0;
//        return x68Sound.X68Sound_Start(sampleRate, v1, v2, betw, pcmbuf, late, v3);
//    }
//
//    public int x68Sound_OpmWait(int v, enmModel model) {
//        if (model == enmModel.RealModel) return 0;
//        return x68Sound.X68Sound_OpmWait(v);
//    }
//
//    public void x68Sound_Pcm8_Out(int v, byte[] p, int a1, int d1, int d2,
//                                  enmModel model) {
//        if (model == enmModel.RealModel) return;
//        x68Sound.X68Sound_Pcm8_Out(v, p, a1, d1, d2);
//    }
//
//    public void x68Sound_Pcm8_Abort(enmModel model) {
//        if (model == enmModel.RealModel) return;
//        x68Sound.X68Sound_Pcm8_Abort();
//    }
//
//    private void x68Sound_MountMemory(byte[] mm, enmModel model) {
//        if (model == enmModel.RealModel) return;
//        x68Sound.MountMemory(mm);
//    }
//
//    public void sound_iocs_init(enmModel model) {
//        if (model == enmModel.RealModel) return;
//        sound_iocs.init();
//    }
//
//    public void sound_iocs_iocs_adpcmmod(int v, enmModel model) {
//        if (model == enmModel.RealModel) return;
//        sound_iocs._iocs_adpcmmod(v);
//    }
//
//    public void sound_iocs_iocs_adpcmout(int a1, int d1, int d2, enmModel
//            model) {
//        if (model == enmModel.RealModel) return;
//        sound_iocs._iocs_adpcmout(a1, d1, d2);
//    }
//
//    public void sound_iocs_iocs_opmset(byte d1, byte d2, enmModel model) {
//        //if (model == enmModel.RealModel) return;
//        setYM2151Register(0, 0, d1, d2, model, 0, 0);
//        sound_iocs._iocs_opmset(d1, d2);
//    }

//#endregion

    public static class ChipKeyInfo {
        public boolean[] On;

        public boolean[] Off;

        public ChipKeyInfo(int n) {
            On = new boolean[n];
            Off = new boolean[n];
            for (int i = 0; i < n; i++)
                Off[i] = true;
        }
    }
}
