
package mdplayer;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mdplayer.Common.EnmRealChipType;
import vavi.util.Debug;


public class RealChip implements Closeable {
//    private NScci.NScci nScci;

//    private Nc86ctl.Nc86ctl nc86ctl;

    // // #region IDisposable Support

    private boolean disposedValue = false;

    protected void Dispose(boolean disposing) {
        if (!disposedValue) {
            if (disposing) {
                close_();
            }

            disposedValue = true;
        }
    }

    public void close() {
        Dispose(true);
    }

    // // #endregion

    public RealChip(boolean sw) {
        Debug.println(Level.SEVERE, "RealChip:Ctr:STEP 00(Start)");
        if (!sw) {
            Debug.println(Level.SEVERE, "RealChip:Not Initialize(user)");
            return;
        }

        // SCCIの存在確認
//        int n = 0;
//        try {
//            nScci = new NScci.NScci();
//            n = nScci.NSoundInterfaceManager_ == null ? 0 : nScci.NSoundInterfaceManager_.getInterfaceCount();
//            if (n == 0) {
//                if (nScci != null)
//                    nScci.Dispose();
//                nScci = null;
//                Log.forcedWrite("RealChip:Ctr:Not found SCCI.");
//            } else {
//                Log.forcedWrite(String.format("RealChip:Ctr:Found SCCI.(Interface count=%d)", n));
//                getScciInstances();
//                nScci.NSoundInterfaceManager_.setLevelDisp(false);
//            }
//        } catch (Exception e) {
//            nScci = null;
//        }

        // GIMICの存在確認
        Debug.println(Level.SEVERE, "RealChip:Ctr:STEP 01");
//        try {
//            nc86ctl = new Nc86ctl.Nc86ctl();
//            nc86ctl.initialize();
//            n = nc86ctl.getNumberOfChip();
//            if (n == 0) {
//                nc86ctl.deinitialize();
//                nc86ctl = null;
//                Log.forcedWrite("RealChip:Ctr:Not found G.I.M.I.C.");
//            } else {
//                Log.forcedWrite(String.format("RealChip:Ctr:Found G.I.M.I.C.(Interface count=%d)", n));
//                Nc86ctl.NIRealChip nirc = nc86ctl.getChipInterface(0);
//                nirc.reset();
//            }
//        } catch (Exception e) {
//            nc86ctl = null;
//        }
        Debug.println(Level.SEVERE, "RealChip:Ctr:STEP 02(Success)");
    }

    public void close_() {
//        if (nScci != null) {
//            try {
//                nScci.Dispose();
//            } catch (Exception e) {
//            }
//            nScci = null;
//        }
//        if (nc86ctl != null) {
//            try {
//                nc86ctl.deinitialize();
//            } catch (Exception e) {
//            }
//            nc86ctl = null;
//        }
    }

    public void getScciInstances() {
//        int ifc = nScci.NSoundInterfaceManager_.getInterfaceCount();
//
//        for (int i = 0; i < ifc; i++) {
//            NSoundInterface sif = nScci.NSoundInterfaceManager_.getInterface(i);
//
//            int scc = sif.getSoundChipCount();
//            for (int j = 0; j < scc; j++) {
//                NSoundChip sc = sif.getSoundChip(j);
//                NSCCI_SOUND_CHIP_INFO info = sc.getSoundChipInfo();
//            }
//        }
    }

    public void setLevelDisp(boolean v) {
//        if (nScci == null)
//            return;
//        nScci.NSoundInterfaceManager_.setLevelDisp(v);
    }

    // public void Init() {
    // if (nScci != null) {
    // nScci.NSoundInterfaceManager_.init();
    // }
    // if (nc86ctl != null) {
    // nc86ctl.initialize();
    // }
    // }

    public void reset() {
//        if (nScci != null)
//            nScci.NSoundInterfaceManager_.reset();
//        if (nc86ctl != null) {
//            // nc86ctl.initialize();
//            int n = nc86ctl.getNumberOfChip();
//            for (int i = 0; i < n; i++) {
//                NIRealChip rc = nc86ctl.getChipInterface(i);
//                rc.reset();
//            }
//        }
    }

    public void SendData() {
//        if (nScci != null)
//            nScci.NSoundInterfaceManager_.sendData();
//        if (nc86ctl != null) {
            // int n = nc86ctl.getNumberOfChip();
            // for (int i = 0; i < n; i++)
            // {
            // NIRealChip rc = nc86ctl.getChipInterface(i);
            // if (rc != null)
            // {
            // while ((rc.@in(0x0) & 0x00) != 0)
            // Thread.sleep(0);
            // }
            // }
//        }
    }

    public void WaitOPNADPCMData(boolean isGIMIC) {
//        if (nScci != null)
//            nScci.NSoundInterfaceManager_.sendData();
//        if (nc86ctl != null && isGIMIC) {
            // int n = nc86ctl.getNumberOfChip();
            // for (int i = 0; i < n; i++) {
            // NIRealChip rc = nc86ctl.getChipInterface(i);
            // if (rc != null) {
            // while ((rc.@in(0x0) & 0x83) != 0)
            // Thread.sleep(0);
            // while ((rc.@in(0x100) & 0xbf) != 0) {
            // Thread.sleep(0);
            // }
            // }
            // }

//        } else {
//            if (nScci == null)
//                return;
//            nScci.NSoundInterfaceManager_.sendData();
//            while (!nScci.NSoundInterfaceManager_.isBufferEmpty()) {
//                try { Thread.sleep(0); } catch (InterruptedException e) {}
//            }
//        }
    }

    public RSoundChip GetRealChip(Setting.ChipType2 ChipType2, int ind/* = 0 */) {
        if (ChipType2.getRealChipInfo().length < ind + 1)
            return null;
        if (ChipType2.getRealChipInfo()[ind] == null)
            return null;

//        if (nScci != null) {
//            int iCount = nScci.NSoundInterfaceManager_.getInterfaceCount();
//            for (int i = 0; i < iCount; i++) {
//                NSoundInterface iIntfc = nScci.NSoundInterfaceManager_.getInterface(i);
//                NSCCI_INTERFACE_INFO iInfo = nScci.NSoundInterfaceManager_.getInterfaceInfo(i);
//                int sCount = iIntfc.getSoundChipCount();
//                for (int s = 0; s < sCount; s++) {
//                    NSoundChip sc = iIntfc.getSoundChip(s);
//
//                    if (0 == ChipType2.getRealChipInfo()[ind].getSoundLocation() && i == ChipType2.getRealChipInfo()[ind].BusID &&
//                        s == ChipType2.getRealChipInfo()[ind].SoundChip) {
//                        RScciSoundChip rsc = new RScciSoundChip(0, i, s);
//                        rsc.scci = nScci;
//                        return rsc;
//                    }
//
//                }
//            }
//        }

//        if (nc86ctl != null) {
//            int iCount = nc86ctl.getNumberOfChip();
//            for (int i = 0; i < iCount; i++) {
//                NIRealChip rc = nc86ctl.getChipInterface(i);
//                NIGimic2 gm = rc.QueryInterface();
//                ChipType cct = gm.getModuleType();
//                int o = -1;
//                String seri = gm.getModuleInfo().Serial;
//                try {
//                    o = Integer.parseInt(seri);
//                } catch (NumberFormatException e) {
//                    o = -1;
//                }
//
//                if (-1 == ChipType2.getRealChipInfo()[ind].getSoundLocation() && i == ChipType2.getRealChipInfo()[ind].BusID &&
//                    o == ChipType2.getRealChipInfo()[ind].getSoundChip()) {
//                    RC86ctlSoundChip rsc = new RC86ctlSoundChip(-1, i, o);
//                    rsc.c86ctl = nc86ctl;
//                    return rsc;
//                }
//            }
//        }

        return null;
    }

    public List<Setting.ChipType2> GetRealChipList(EnmRealChipType realChipType2) {
        List<Setting.ChipType2> ret = new ArrayList<>();

//        if (nScci != null) {
//            int iCount = nScci.NSoundInterfaceManager_.getInterfaceCount();
//            for (int i = 0; i < iCount; i++) {
//                NSoundInterface iIntfc = nScci.NSoundInterfaceManager_.getInterface(i);
//                NSCCI_INTERFACE_INFO iInfo = nScci.NSoundInterfaceManager_.getInterfaceInfo(i);
//                int sCount = iIntfc.getSoundChipCount();
//                for (int s = 0; s < sCount; s++) {
//                    NSoundChip sc = iIntfc.getSoundChip(s);
//                    int t = sc.getSoundChipType();
//                    if (t == (int) realChipType2.v) {
//                        Setting.ChipType2 ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(0);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        ct.getRealChipInfo()[0].SoundChip = s;
//                        ct.getRealChipInfo()[0].ChipName = sc.getSoundChipInfo().cSoundChipName;
//                        ct.getRealChipInfo()[0].InterfaceName = iInfo.cInterfaceName;
//                        ret.add(ct);
//                    } else if (realChipType2 == EnmRealChipType.K051649 && (t == 12 || t == 13)) {
//                        Setting.ChipType2 ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(0);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        ct.getRealChipInfo()[0].SoundChip = s;
//                        ct.getRealChipInfo()[0].ChipName = sc.getSoundChipInfo().cSoundChipName;
//                        ct.getRealChipInfo()[0].InterfaceName = iInfo.cInterfaceName;
//                        ret.add(ct);
//                    } else {
//                        // 互換指定をチェック
//                        NSCCI_SOUND_CHIP_INFO chipInfo = sc.getSoundChipInfo();
//                        for (int n = 0; n < chipInfo.iCompatibleSoundChip.length; n++) {
//                            if ((int) realChipType2.v != chipInfo.iCompatibleSoundChip[n])
//                                continue;
//
//                            Setting.ChipType2 ct = new Setting.ChipType2();
//                            ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                                new Setting.ChipType2.RealChipInfo()
//                            };
//                            ct.getRealChipInfo()[0].Soun.setLocation(0);
//                            ct.getRealChipInfo()[0].BusID = i;
//                            ct.getRealChipInfo()[0].SoundChip = s;
//                            ct.getRealChipInfo()[0].ChipName = sc.getSoundChipInfo().cSoundChipName;
//                            ct.getRealChipInfo()[0].InterfaceName = iInfo.cInterfaceName;
//                            ret.add(ct);
//                            break;
//                        }
//                    }
//                }
//            }
//        }

//        if (nc86ctl != null) {
//            int iCount = nc86ctl.getNumberOfChip();
//            for (int i = 0; i < iCount; i++) {
//                NIRealChip rc = nc86ctl.getChipInterface(i);
//                NIGimic2 gm = rc.QueryInterface();
//                Devinfo di = gm.getModuleInfo();
//                ChipType cct = gm.getModuleType();
//                if (cct == ChipType.CHIP_UNKNOWN) {
//                    if (di.Devname == "GMC-S2149")
//                        cct = ChipType.CHIP_YM2149;
//                    else if (di.Devname == "GMC-S8910")
//                        cct = ChipType.CHIP_AY38910;
//                    else if (di.Devname == "GMC-S2413")
//                        cct = ChipType.CHIP_YM2413;
//                }
//                Setting.ChipType2 ct = null;
//                int o = -1;
//                switch (realChipType2) {
//                case YM2203:
//                case YM2608:
//                    if (cct == ChipType.CHIP_YM2608 || cct == ChipType.CHIP_YMF288 || cct == ChipType.CHIP_YM2203) {
//                        ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(-1);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        String seri = gm.getModuleInfo().Serial;
//                        try { o = Integer.parseInt(seri); } catch (NumberFormatException e) { o = -1; }
//                        ct.getRealChipInfo()[0].SoundChip = o;
//                        ct.getRealChipInfo()[0].ChipName = di.Devname;
//                        ct.getRealChipInfo()[0].InterfaceName = gm.getMBInfo().Devname;
//                        ct.getRealChipInfo()[0].ChipType = (int) cct;
//                    }
//                    break;
//                case AY8910:
//                    if (cct == ChipType.CHIP_YM2149 || cct == ChipType.CHIP_AY38910 || cct == ChipType.CHIP_YM2608 ||
//                        cct == ChipType.CHIP_YMF288 || cct == ChipType.CHIP_YM2203) {
//                        ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(-1);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        String seri = gm.getModuleInfo().Serial;
//                        try { o = Integer.parseInt(seri); } catch (NumberFormatException e) { o = -1; }
//                        ct.getRealChipInfo()[0].SoundChip = o;
//                        ct.getRealChipInfo()[0].ChipName = di.Devname;
//                        ct.getRealChipInfo()[0].InterfaceName = gm.getMBInfo().Devname;
//                        ct.getRealChipInfo()[0].ChipType = (int) cct;
//                    }
//                    break;
//                case YM2413:
//                    if (cct == ChipType.CHIP_YM2413) {
//                        ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(-1);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        String seri = gm.getModuleInfo().Serial;
//                        try { o = Integer.parseInt(seri); } catch (NumberFormatException e) { o = -1; }
//                        ct.getRealChipInfo()[0].SoundChip = o;
//                        ct.getRealChipInfo()[0].ChipName = di.Devname;
//                        ct.getRealChipInfo()[0].InterfaceName = gm.getMBInfo().Devname;
//                        ct.getRealChipInfo()[0].ChipType = (int) cct;
//                    }
//                    break;
//                case YM2610:
//                    if (cct == ChipType.CHIP_YM2608 || cct == ChipType.CHIP_YMF288) {
//                        ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(-1);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        String seri = gm.getModuleInfo().Serial;
//                        try { o = Integer.parseInt(seri); } catch (NumberFormatException e) { o = -1; }
//                        ct.getRealChipInfo()[0].SoundChip = o;
//                        ct.getRealChipInfo()[0].ChipName = di.Devname;
//                        ct.getRealChipInfo()[0].InterfaceName = gm.getMBInfo().Devname;
//                        ct.getRealChipInfo()[0].ChipType = (int) cct;
//                    }
//                    break;
//                case YM2151:
//                    if (cct == ChipType.CHIP_YM2151) {
//                        ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(-1);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        String seri = gm.getModuleInfo().Serial;
//                        try { o = Integer.parseInt(seri); } catch (NumberFormatException e) { o = -1; }
//                        ct.getRealChipInfo()[0].SoundChip = o;
//                        ct.getRealChipInfo()[0].ChipName = di.Devname;
//                        ct.getRealChipInfo()[0].InterfaceName = gm.getMBInfo().Devname;
//                        ct.getRealChipInfo()[0].ChipType = (int) cct;
//                    }
//                    break;
//                case YM3526:
//                case YM3812:
//                case YMF262:
//                    if (cct == ChipType.CHIP_OPL3) {
//                        ct = new Setting.ChipType2();
//                        ct.realChipInfo = new Setting.ChipType2.RealChipInfo[] {
//                            new Setting.ChipType2.RealChipInfo()
//                        };
//                        ct.getRealChipInfo()[0].Soun.setLocation(-1);
//                        ct.getRealChipInfo()[0].BusID = i;
//                        String seri = gm.getModuleInfo().Serial;
//                        try { o = Integer.parseInt(seri); } catch (NumberFormatException e) { o = -1; }
//                        ct.getRealChipInfo()[0].SoundChip = o;
//                        ct.getRealChipInfo()[0].ChipName = di.Devname;
//                        ct.getRealChipInfo()[0].InterfaceName = gm.getMBInfo().Devname;
//                        ct.getRealChipInfo()[0].ChipType = (int) cct;
//                    }
//                    break;
//                }
//
//                if (ct != null)
//                    ret.add(ct);
//            }
//        }

        return ret;
    }

    public static abstract class RSoundChip {
        protected int soundLocation;
        protected int busID;
        protected int soundChip;

        public int dClock = 3579545;

        public RSoundChip(int soundLocation, int busID, int soundChip) {
            this.soundLocation = soundLocation;
            this.busID = busID;
            this.soundChip = soundChip;
        }

        public abstract void init();

        public abstract void setRegister(int adr, int dat);

        public abstract int getRegister(int adr);

        public abstract boolean isBufferEmpty();

        public abstract int setMasterClock(int mClock);

        public abstract void setSSGVolume(byte vol);
    }

    public static class RScciSoundChip extends RSoundChip {
//        public NScci.NScci scci = null;

//        private NSoundChip realChip = null;

        public RScciSoundChip(int soundLocation, int busID, int soundChip) {
            super(soundLocation, busID, soundChip);
        }

        @Override
        public void init() {
//            realChip = null;
//            int n = scci.NSoundInterfaceManager_.getInterfaceCount();
//            if (BusID >= n) {
//                return;
//            }

//            NSoundInterface nsif = scci.NSoundInterfaceManager_.getInterface(BusID);

//            int c = nsif.getSoundChipCount();
//            if (SoundChip >= c) {
//                return;
//            }
//            NSoundChip nsc = nsif.getSoundChip(SoundChip);
//            realChip = nsc;
//            dClock = (int) nsc.getSoundChipClock();

            // chipの種類ごとに初期化コマンドを送りたい場合
//            switch (nsc.getSoundChipType()) {
//            case (int) EnmRealChipType.YM2608.v:
                // setRegister(0x2d, 00);
                // setRegister(0x29, 82);
                // setRegister(0x07, 38);
//                break;
//            }
        }

        @Override
        public void setRegister(int adr, int dat) {
//            if (realChip == null)
//                return;
//            realChip.setRegister(adr, dat);
        }

        @Override
        public int getRegister(int adr) {
//            if (realChip == null)
//                return -1;
            return -1; // realChip.getRegister(adr);
        }

        @Override
        public boolean isBufferEmpty() {
//            if (realChip == null)
//                return false;
            return false; // realChip.isBufferEmpty();
        }

        /**
         * マスタークロックの設定
         * @param mClock 設定したい値
         * @return 実際設定された値
         */
        @Override
        public int setMasterClock(int mClock) {
            // SCCIはクロックの変更不可
//            if (realChip == null)
//                return 0;

            return -1; // (int) realChip.getSoundChipClock();
        }

        @Override
        public void setSSGVolume(byte vol) {
            // SCCIはSSG音量の変更不可
//            if (realChip == null)
//                return;
        }
    }

    static class RC86ctlSoundChip extends RSoundChip {
//        public Nc86ctl.Nc86ctl c86ctl = null;

//        public Nc86ctl.NIRealChip realChip = null;

//        public Nc86ctl.ChipType ChipType = ChipType.CHIP_UNKNOWN;

        public RC86ctlSoundChip(int soundLocation, int busID, int soundChip) {
            super(soundLocation, busID, soundChip);
        }

        @Override
        public void init() {
//            NIRealChip rc = c86ctl.getChipInterface(BusID);
//            rc.reset();
//            realChip = rc;
//            NIGimic2 gm = rc.QueryInterface();
//            dClock = gm.getPLLClock();
//            Devinfo di = gm.getModuleInfo();
//            ChipType = gm.getModuleType();
//            if (ChipType == ChipType.CHIP_UNKNOWN) {
//                if (di.Devname == "GMC-S2149")
//                    ChipType = ChipType.CHIP_YM2149;
//                else if (di.Devname == "GMC-S8910")
//                    ChipType = ChipType.CHIP_AY38910;
//                else if (di.Devname == "GMC-S2413")
//                    ChipType = ChipType.CHIP_YM2413;
//            }
//            if (ChipType == ChipType.CHIP_YM2608) {
//                // setRegister(0x2d, 00);
//                // setRegister(0x29, 82);
//                // setRegister(0x07, 38);
//            }
        }

        @Override public void setRegister(int adr, int dat) {
//            realChip((int)adr, (byte)dat);
        }

        @Override public int getRegister(int adr) {
            return -1; // realChip((int)adr);
        }

        @Override
        public boolean isBufferEmpty() {
            return true;
        }

        /**
         * マスタークロックの設定
         * @param mClock 設定したい値
         * @return 実際設定された値
         */
        @Override
        public int setMasterClock(int mClock) {
//            NIGimic2 gm = realChip.QueryInterface();
//            int nowClock = gm.getPLLClock();
//            if (no.Clock != mClock) {
//                gm.setPLLClock(mClock);
//            }

            return -1; // gm.getPLLClock();
        }

        @Override
        public void setSSGVolume(byte vol) {
//            NIGimic2 gm = realChip.QueryInterface();
//            gm.setSSGVolume(vol);
        }
    }
}
