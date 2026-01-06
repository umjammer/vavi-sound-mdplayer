package mdplayer.driver.zms;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.FileNotFoundException;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.UnZDF;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.driver.mndrv.FMTimer;
import mdplayer.driver.mxdrv.MXDRV.Pcm8St;
import mdplayer.driver.zms.nise68.FileMng;
import mdplayer.driver.zms.nise68.MemMng;
import mdplayer.driver.zms.nise68.Nise68;
import mdplayer.plugin.BasePlugin;
import mdsound.chips.MPcm;
import mdsound.chips.MPcmPP.SETPCM;
import mdsound.instrument.MPcmPPInst;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kMPcmInst;
import mdsound.instrument.X68kYm2151Inst;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * <pre>
 *               | source | compiled
 * --------------+--------+----------
 * play data	 |  ZMS   |   ZMD
 * sampling data |  CNF   |   ZPD
 * </pre>
 * system property
 * <li>"mdplayer.zms.zpd" ...  </li>
 */
public class Zms extends BaseDriver {

    private static final Logger logger = getLogger(Zms.class.getName());

    private Nise68 nise68;
    private FileMng fileMng = new FileMng(System.getProperty("user.dir"), "C:");
    public X68kMPcmInst mpcm;
    public MPcmPPInst mpcmpp;
    public int mpcmType = 0;
    public X68kYm2151Inst opmPCM;
    public Pcm8PPInst pcm8pp;
    public int pcm8type = 0;

    private int checkCounter = 0;
    private List<String> envZPDs = new ArrayList<>();
    public int version = 0;
    private FMTimer timerOPM;
    public Pcm8St[] pcm8St = {
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St(),
            new Pcm8St(), new Pcm8St(), new Pcm8St(), new Pcm8St()
    };
    public MPCMSt[] mpcmSt = {
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt(),
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt(),
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt(),
            new MPCMSt(), new MPCMSt(), new MPCMSt(), new MPCMSt()
    };

    public static class MPCMSt {

        public boolean keyOn = false;
        public boolean keyOff = false;
        public byte type = 0;
        public byte orig = 0;
        public int adrs_ptr = 0;
        public int size = 0;
        public int start = 0;
        public int end = 0;
        public int count = 0;
        public int frq = 0;
        public int pitch = 0;
        public int volume = 0;
        public int pan = 0;
        public float rate = 0;
        public float base_ = 0;
    }

    private String playingFileName;

    public String getPlayingFileName() {
        return playingFileName;
    }

    public void setPlayingFileName(String value) {
        playingFileName = value;
    }

    private String playingArcFileName;

    public String getPlayingArcFileName() {
        return playingArcFileName;
    }

    public void setPlayingArcFileName(String value) {
        playingArcFileName = value;
    }

    public List<Tuple<byte[], String>> supportFileBinaryAndName;
    private byte[] compiledData;

    public byte[] getCompiledData() {
        return compiledData;
    }

    public void setCompiledData(byte[] value) {
        compiledData = value;
    }

    @Override
    public Vgm.Gd3 getGD3Info(byte[] buf, int[] vgmGd3) {
        if (playingFileName.toUpperCase().endsWith(".ZMS")) {
            return getGD3InfoZMS(buf);
        } else if (playingFileName.toUpperCase().endsWith(".ZMD")) {
            return getGD3InfoZMD(buf);
        } else {
            return new Gd3();
        }
    }

    private Gd3 getGD3InfoZMS(byte[] buf) {
        String text = new String(buf, charset);
        String[] texts = text.split("\r\n");
        String cmt = "";
        String comment = ".COMMENT";
        for (String s : texts) {
            if (!s.toUpperCase().trim().contains(comment)) continue;
            cmt = s.trim().substring(s.toUpperCase().trim().indexOf(comment) + comment.length()).trim();
            break;
        }
        Gd3 gd3 = new Gd3();
        if (cmt != null && !cmt.isEmpty()) {
            gd3.trackName = cmt;
            gd3.trackNameJ = cmt;
        }
        return gd3;
    }

    private Gd3 getGD3InfoZMD(byte[] buf) {
        Gd3 gd3 = new Gd3();

        if (buf.length < 8) {
            throw new IllegalArgumentException("Unknown zmd file");
        } else {
            int chkID1 = (buf[0] & 0xFF) * 0x100_0000 + (buf[1] & 0xFF) * 0x1_0000 + (buf[2] & 0xFF) * 0x100 + (buf[3] & 0xFF);
            int chkID2 = (buf[4] & 0xFF) * 0x100_0000 + (buf[5] & 0xFF) * 0x1_0000 + (buf[6] & 0xFF) * 0x100 + (buf[7] & 0xFF);
logger.log(Level.TRACE, "Zms Version Check: chkID1=%08x, chkID2=%08x%n".formatted(chkID1, chkID2));
            if (chkID1 == 0x1a5a_6d75 && chkID2 == 0x5369_4330) version = 3;
            if (chkID1 == 0x105a_6d75 && chkID2 != 0x5369_4330) version = 2;
logger.log(Level.TRACE, "Zms Version Detected: " + version);

            if (version == 0) {
                throw new IllegalArgumentException("Version check error");
            }
        }

        String cmt = "";
        try {
            if (version == 3) {
                int ptr = (buf[9 * 4 + 0] & 0xFF) * 0x100_0000 + (buf[9 * 4 + 1] & 0xFF) * 0x1_0000 +
                        (buf[9 * 4 + 2] & 0xFF) * 0x100 + (buf[9 * 4 + 3] & 0xFF) + 40;
                int ePtr = ptr;
                while (buf[ePtr] != 0x00) {
                    if (buf[ePtr] == 0x0d && buf[ePtr + 1] == 0x0a) break;
                    ePtr++;
                }

                cmt = new String(buf, ptr, ePtr - ptr, charset);
            }
        } catch (Exception e) {
            // Do nothing
        }

        if (cmt != null && !cmt.isEmpty()) {
            gd3.trackName = cmt;
            gd3.trackNameJ = cmt;
        }
        return gd3;
    }

    @Override
    public boolean init(byte[] vgmBuf, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        gd3 = getGD3Info(vgmBuf, 0);
        this.plugin = plugin;
        loopCounter = 0;
        vgmCurLoop = 0;
        this.model = model;
        vgmFrameCounter = -latency - waitTime;
        vgmSpeed = 1;
        setZPDSearchPath();

        try {
            run(vgmBuf);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        return true;
    }

    @Override
    public boolean init(byte[] vgmBuf, int fileType, BasePlugin plugin, EnmModel model, Class<? extends Chip>[] useChip, int latency, int waitTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processOneFrame() {
        try {
            if (waitNextPlay-- > 0) return;

            vgmSpeedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * vgmSpeed;
            while (vgmSpeedCounter >= 1.0) {
                vgmSpeedCounter -= 1.0;

                if (vgmFrameCounter > -1) {
                    counter++;

                    if (version == 2) {
                        timerOPM.timer();
                        while ((timerOPM.readStatus() & 3) != 0) {
                            nise68.trapOPM(); // true, true, true);
                        }
                    } else {
                        //virtualFrameCounter++;
                        while (nise68.intTimer()) {
//#if DEBUG
                            //if (model != EnmModel.RealModel)
                            //    nise68.Trap(0x8e, true, true, true);
//                            nise68.Trap(0x8e);
//#else
                            nise68.trap(0x8e);
//#endif
                        }
                    }
                }
                vgmFrameCounter++;
            }

//            if (SkipSwitchPianoRoll) return;
            checkCounter--;
            if (checkCounter < 0) {
                checkCounter = 100;
                if (version == 2) {
                    // Check if playing
                    nise68.reg.setDl(1, 0x09); // m_stat
                    nise68.reg.setDl(2, 0); // Check mode (0: Check all channels)
                    nise68.trap(3 + 32);
                    int d0 = nise68.reg.getDl(0);
                    if (d0 == 0) {
                        if (preData.isEmpty()) stopped = true;
                        else {
                            preData.remove(0);
                            byte[] zmd = null;
                            if (preData.isEmpty()) {
                                //if (nise68.hmn.fb.containsKey(fnZMD)) zmd = nise68.hmn.fb[fnZMD];
                                if (fileMng.existsFile(fnZMD)) zmd = fileMng.vReadAllBytes(fnZMD);
                            } else {
                                //if (nise68.hmn.fb.containsKey(preData[0])) zmd = nise68.hmn.fb[preData[0]];
                                if (fileMng.existsFile(preData.get(0))) zmd = fileMng.vReadAllBytes(preData.get(0));
                            }
                            int fileSize = zmd.length;
                            int filePtr = nise68.hmn.memMng.malloc(fileSize);
                            for (int i = 0; i < zmd.length; i++) {
                                nise68.mem.pokeB(filePtr + i, zmd[i]);
                            }

                            nise68.reg.setDl(1, 0x11); // play_cnv_data
                            nise68.reg.setDl(2, zmd.length - 7);
                            nise68.reg.setAl(1, filePtr + 7);
                            nise68.trap(trp); // , true, true, true);
                            waitNextPlay = (int) (setting.getOutputDevice().getSampleRate() * (double) setting.getZMusic().waitNextPlay / 1000.0);
                        }
                    }

                    // Loop count check
                    nise68.reg.setDl(1, 0x4d); // get_loop_time
                    nise68.trap(3 + 32);
                    d0 = nise68.reg.getDl(0);
                    vgmCurLoop = d0 - 1;
                } else {
                    // Check if playing
                    nise68.reg.setDl(0, 0x0b); // ZM_PLAY_STATUS
                    nise68.reg.setDl(1, 0); // Check mode (0: Check all channels)
                    nise68.reg.setAl(1, 0); // Inspection result storage buffer address (set to 0 to return simplified inspection results)
                    nise68.trap(3 + 32);
                    int d0 = nise68.reg.getDl(0);
                    if (d0 == 0)
                        stopped = true;

                    // Loop count check
                    nise68.reg.setDl(0, 0x59); // ZM_LOOP_CONTROL
                    nise68.reg.setDl(1, 0xffff_ffff); // Control mode (-1 = get loop count)
                    nise68.trap(3 + 32);
                    d0 = nise68.reg.getDl(0);
                    vgmCurLoop = d0 - 1;
                }
            }
            //vgmCurLoop = mm.readShort(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void run(byte[] vgmBuf) throws Exception {
        //if (model == EnmModel.RealModel) { return; }

        String fn = playingFileName;
        String withoutExtFn;
        String dn = java.nio.file.Path.of(fn).getParent() != null ? java.nio.file.Path.of(fn).getParent().toString() : null;
        if (dn == null || dn.isEmpty()) dn = Path.getDirectoryName(System.getProperty("user.dir"));
        if (dn != null && !dn.isEmpty()) withoutExtFn = Path.combine(dn, Path.getFileNameWithoutExtension(fn));
        else withoutExtFn = Path.getFileNameWithoutExtension(fn);
        String fnZMD = Path.getFileName(withoutExtFn + ".ZMD");
        String fnZMS = Path.getFileName(withoutExtFn + ".ZMS");

        nise68 = new Nise68();
        nise68.setMPcm(version == 2 ? this::pcm8CallBack : this::mPcmCallBack);
        nise68.setOpm(this::opmCallBack);
        nise68.setMidi(this::midiCallBack, Common.VGMProcSampleRate);
        nise68.setSCC_A(this::sccCallBack, Common.VGMProcSampleRate);
        if (playingArcFileName != null && !playingArcFileName.isEmpty()) {
            if (playingFileName.toUpperCase().endsWith(".ZDF")) {
                UnZDF cmd = new UnZDF();
                fileMng = cmd.unpack(playingArcFileName);
            }

        } else {
            fileMng = new FileMng(dn, "C:");
        }
        nise68.init(envZPDs, version == 2, fileMng);

        fileMng.setVFile(Path.getFileName(fnZMD), vgmBuf);
        //nise68.hmn.fb.add(fnZMD, vgmBuf);
        //if (format == EnmFileFormat.ZMD) nise68.hmn.fb.add(fnZMD, vgmBuf);
        //else {
        //    // compile
        //    if (model != EnmModel.RealModel || compiledData == null) compile(vgmBuf);
        //    else {
        //        // Real receives the compilation result of virtual
        //        nise68.hmn.fb.add(fnZMS, vgmBuf);
        //        nise68.hmn.fb.add(fnZMD, compiledData);
        //    }
        //}

        play();
    }

    private final List<String> preData = new ArrayList<>();
    private String fnZMD;
    private int trp = 3 + 32;
    private int waitNextPlay = 0;
    private int rc;

    private void play() throws URISyntaxException, IOException {
        String fn = playingFileName;
        String withoutExtFn;
        String dn = Path.getDirectoryName(fn);
        if (dn != null && !dn.isEmpty()) withoutExtFn = Path.combine(dn, Path.getFileNameWithoutExtension(fn));
        else withoutExtFn = Path.getFileNameWithoutExtension(fn);
        fnZMD = Path.getFileName(withoutExtFn + ".ZMD");
        java.nio.file.Path crntDir = java.nio.file.Path.of(System.getProperty("mdplayer.zms.dir", System.getProperty("user.dir")));

        java.nio.file.Path zmsc3 = crntDir.resolve("ZMSC3.X");
        if (!Files.exists(zmsc3)) {
            logger.log(Level.INFO, "File not found : %s".formatted(zmsc3));
            throw new FileNotFoundException(zmsc3.toString());
        }
        fileMng.setVFile(zmsc3.toString());

        java.nio.file.Path zmusic = crntDir.resolve("ZMUSIC.X"); // ver2
        if (!Files.exists(zmusic)) {
            logger.log(Level.INFO, "File not found : %s".formatted(zmusic));
            throw new FileNotFoundException(zmusic.toString());
        }
        fileMng.setVFile(zmusic.toString());

        trp = 3 + 32;

        if (version == 2) {
            timerOPM = new FMTimer(true, null, 4000000); // , Common.VGMProcSampleRate);

            // If zpd is specified, specify zmusic to preload
            String optionZpd = "";
            String optionZmd = "";
            preData.clear();
            if (supportFileBinaryAndName != null) {
                for (Tuple<byte[], String> s : supportFileBinaryAndName) {
                    String ext = Path.getExtension(s.getItem2()).toUpperCase();
                    if (ext.equals(".ZPD")) {
                        optionZpd = " -B" + Path.getFileName(s.getItem2());
                        if (!fileMng.existsFile(s.getItem2())) {
                            fileMng.setVFile(s.getItem2(), s.getItem1());
                        }
                    }
                    if (ext.equals(".ZMD") || ext.equals(".ZMS")) {
                        String f = s.getItem2();
                        f = Path.changeExtension(f, ".ZMD");
                        optionZmd = " -N" + Path.getFileName(f);
                        if (!fileMng.existsFile(f)) {
                            fileMng.setVFile(f, s.getItem1());
                            preData.add(f);
                        }
                    }
                }
            }

            nise68.hmn.memMng = new MemMng(0x0001_2000 + (9212 + 2048) * 1024 + Files.readAllBytes(zmusic).length);

            //if (nise68.loadRun(zmusic, "-P9212 -T2048" + optionZpd + optionZmd, Path.GetDirectoryName(fnZMD), 0x00012000,
            // true, true, true
            //) != 0) throw new Exception("zmusic regident Error");
            if ((rc = nise68.loadRun(zmusic.toString(), "-P9212 -T2048" + optionZpd + optionZmd, 0x0001_2000,
                    true, true, true,
                    100_000_000, 0
            )) != 0) throw new IllegalStateException("zmusic resident Error: " + rc);

            if (pcm8type == 0) if (opmPCM != null) opmPCM.chips[0].mountMemory(nise68.mem.mem);
            else if (pcm8pp != null) pcm8pp.writePcm(0, nise68.mem.mem, 0, nise68.mem.mem.length);

            // play
            byte[] zmd = null;
            if (preData.isEmpty()) {
                if (File.exists(fnZMD)) {
                    zmd = File.readAllBytes(fnZMD);
                    //if (!nise68.hmn.fb.containsKey(fnZMD)) {
                    //    nise68.hmn.fb.add(fnZMD, zmd);
                    //}
                    if (!fileMng.existsFile(fnZMD)) {
                        fileMng.setVFile(fnZMD, zmd);
                    }
                } else {
                    //if (nise68.hmn.fb.containsKey(fnZMD)) {
                    //    zmd = nise68.hmn.fb[fnZMD];
                    //}
                    if (fileMng.existsFile(Path.getFileName(fnZMD))) {
                        zmd = fileMng.vReadAllBytes(Path.getFileName(fnZMD));
                    }
                }
            } else {
                //if (nise68.hmn.fb.containsKey(preData[0])) {
                //    zmd = nise68.hmn.fb[preData[0]];
                //}
                if (fileMng.existsFile(preData.get(0))) {
                    zmd = fileMng.vReadAllBytes(preData.get(0));
                }
            }
            if (zmd == null) {
                throw new IllegalStateException("Zmd[%s] file not found. ".formatted(fnZMD));
            }
            int fileSize = zmd.length;
            int filePtr = nise68.hmn.memMng.malloc(fileSize);
            for (int i = 0; i < zmd.length; i++) {
                nise68.mem.pokeB((int) (filePtr + i), zmd[i]);
            }

            nise68.reg.setDl(1, 0x11); // play_cnv_data
            nise68.reg.setDl(2, (int) (zmd.length - 7));
            nise68.reg.setAl(1, filePtr + 7);
            nise68.trap(trp); // , true, true, true);

            return;
        }

        // zmsc3 resident
        nise68.hmn.memMng = new MemMng(0x0004_0000);

        //if (nise68.loadRun(zmsc3, "-w", Path.GetDirectoryName(fnZMD), 0x00012000,
        // true, true, true
        //) != 0) throw new Exception("zmsc3 resident Error");
        if ((rc = nise68.loadRun(zmsc3.toString(), "-w", 0x0001_2000
                , true, true, true,
                100_000_000, 0
        )) != 0) throw new IllegalStateException("zmsc3 resident Error: " + rc);

        // play
        //logger.log(Level.INFO, "");
        //Log.SetLogLevel(LogLevel.Information);

        //if ((rc = nise68.LoadRun("C:\\ZP3.R", "-PC:\\SAMPLE1\\SAMPLE.ZMS", "C:\\", 0x00042000,
        //    true, true, true
        //)) != 0) Environment.Exit(rc);
        {
            // Error stock buffer release
            nise68.reg.setDl(0, 0x73); // ZM_FREE_MEM2
            nise68.reg.setDl(3, 0x82645252); // 'ＥRR'
            nise68.trap(trp);
        }
        {
            // ZMD release
            nise68.reg.setDl(0, 0x73); // ZM_FREE_MEM2
            nise68.reg.setDl(3, 0x5a826c44); // 'ZＭD'
            nise68.trap(trp);
        }
        {
            // ZM_COMPILER DETECT
            nise68.reg.setDl(0, 0x6b); // ZM_HOOK_FNC_SERVICE
            nise68.reg.setDl(1, 2); // ZM_COMPILER
            nise68.reg.setAl(1, 0xffff_ffff); // detect_mode
            nise68.trap(trp);
        }
        {
            // Version check (just call)
            nise68.reg.setDl(0, 0x7e); // ZM_ZMUSIC_MODE
            nise68.reg.setDl(1, 0xffff_ffff); //
            nise68.trap(trp);
        }
        {
            // play
            byte[] zmd = fileMng.vReadAllBytes(Path.getFileName(fnZMD)); // nise68.hmn.fb[fnZMD];
            int fileSize = zmd.length;
            int filePtr = nise68.hmn.memMng.malloc(fileSize);
            for (int i = 0; i < zmd.length; i++) {
                nise68.mem.pokeB((int) (filePtr + i), zmd[i]);
            }
            nise68.reg.setDl(0, 0x10); // ZM_PLAY_ZMD
            nise68.reg.setDl(2, fileSize);
            nise68.reg.setAl(1, filePtr + 8);
            nise68.trap(trp);
        }
        {
            // Check for errors
            nise68.reg.setDl(0, 0x6e); // ZM_STORE_ERROR
            nise68.reg.setDl(1, 0xffff_ffff);
            nise68.reg.setDl(2, 0x0000_0000);
            nise68.trap(trp);
            // The number of errors is returned in D0.l
        }
    }

    public boolean compile(byte[] vgmBuf, String fn) throws URISyntaxException {
        String withoutExtFn;
        String dn = Path.getDirectoryName(fn);
        if (dn != null && !dn.isEmpty()) withoutExtFn = Path.combine(dn, Path.getFileNameWithoutExtension(fn));
        else withoutExtFn = Path.getFileNameWithoutExtension(fn);
        String fnZMD = Path.getFileName(withoutExtFn + ".ZMD");
        String fnZMS = Path.getFileName(withoutExtFn + ".ZMS");
        String crntDir = System.getProperty("mdplayer.zms.dir", System.getProperty("user.dir"));
        java.nio.file.Path zmc = java.nio.file.Path.of(crntDir, "ZMUSIC.X");
        if (!Files.exists(zmc)) {
            logger.log(Level.INFO, "File not found : %s".formatted(zmc));
            return false; // throw new FileNotFoundException(zmc);
        }
        fileMng = new FileMng(dn, "C:"); // Set the path of the music file to the current physical drive. The current virtual drive is "C:" (default).
        fileMng.setVFile(zmc.toString());

        nise68 = new Nise68();
        nise68.setMPcm(this::mPcmCallBack);
        nise68.setOpm(this::opmCallBack);
        nise68.setMidi(this::midiCallBack, Common.VGMProcSampleRate);
        nise68.setSCC_A(this::sccCallBack, Common.VGMProcSampleRate);
        nise68.init(null, false, fileMng);

        // compile
        //nise68.hmn.fb.add(fnZMS, vgmBuf);
        fileMng.setVFile(fnZMS, vgmBuf);
        //if (nise68.LoadRun(zmc, Path.GetFileName(fnZMS), Path.GetDirectoryName(fnZMS), 0x00012000,
        // true, true, true
        // ) != 0)
        if (nise68.loadRun(zmc.toString(), Path.getFileName(fnZMS), 0x0001_2000,
                true, true, true,
                100_000_000, 0
        ) != 0) {
            logger.log(Level.INFO, "v3 Compile Error " + zmc);
            return false;
        }
        //compiledData = nise68.hmn.fb[fnZMD];
        compiledData = fileMng.vReadAllBytes(fnZMD);
        return true;
    }

    public boolean compileV2(byte[] vgmBuf, String fn) throws URISyntaxException {
        //String fn = playingFileName;
        String withoutExtFn;
        String dn = Path.getDirectoryName(fn);
        if (dn != null && !dn.isEmpty()) withoutExtFn = Path.combine(dn, Path.getFileNameWithoutExtension(fn));
        else withoutExtFn = Path.getFileNameWithoutExtension(fn);
        String fnZMD = Path.getFileName(withoutExtFn + ".ZMD");
        String fnZMS = Path.getFileName(withoutExtFn + ".ZMS");
        String crntDir = System.getProperty("mdplayer.zms.dir", System.getProperty("user.dir"));
        java.nio.file.Path zmusic = java.nio.file.Path.of(crntDir, "ZMUSIC.X");
        if (!Files.exists(zmusic)) {
            logger.log(Level.INFO, "File not found : %s".formatted(zmusic));
            return false; // throw new FileNotFoundException(zmc);
        }

        nise68 = new Nise68();
        nise68.setMPcm(this::pcm8CallBack);
        nise68.setOpm(this::opmCallBack);
        nise68.setMidi(this::midiCallBack, Common.VGMProcSampleRate);
        nise68.setSCC_A(this::sccCallBack, Common.VGMProcSampleRate);

        fileMng = new FileMng(dn, "C:"); // Set the path of the music file to the current physical drive. The current virtual drive is "C:" (default).
        fileMng.setVFile(zmusic.toString());

        nise68.init(null, false, fileMng);

        // compile
        //nise68.hmn.fb.add(fnZMS, vgmBuf);
        fileMng.setVFile(fnZMS, vgmBuf);
        //if (nise68.loadRun(zmusic, "-C " + Path.getFileName(fnZMS), Path.getDirectoryName(fnZMS), 0x00012000,
        // true, true, true
        //) != 0)
        if (nise68.loadRun(zmusic.toString(), "-C " + fnZMS, 0x00012000,
                true, true, true,
                100_000_000, 0
        ) != 0) {
            logger.log(Level.INFO, "v2 Compile Error", zmusic);
            return false;
        }
        //compiledData = nise68.hmn.fb[fnZMD];
        compiledData = fileMng.vReadAllBytes(Path.getFileName(fnZMD));

        return true;
    }

    private int mPcmCallBack(int n) {
        int ch = n & 0xf;
        switch (n & 0xfff0) {
            case 0x0000:
                //logger.log(Level.TRACE, "MPCM #M_KEY_ON($%04x)".formatted(n));
                if (mpcmType == 0) if (mpcm != null) mpcm.keyOn(0, ch);
                else if (mpcmpp != null) mpcmpp.keyOn(0, ch);
                mpcmSt[ch].keyOn = true;
                break;
            case 0x0100:
                //logger.log(Level.TRACE, "MPCM #M_KEY_OFF($%04x)".formatted(n));
                if (mpcmType == 0) if (mpcm != null) mpcm.keyOff(0, ch);
                else if (mpcmpp != null) mpcmpp.keyOff(0, ch);
                mpcmSt[ch].keyOff = true;
                break;
            case 0x0200:
                //logger.log(Level.TRACE, "MPCM #M_SET_PCM($%04x)".formatted(n));
                if (mpcmType == 0) {
                    MPcm.PCM ptr = new MPcm.PCM();
                    ptr.adrsBuf = nise68.mem.mem;
                    mpcmSt[ch].type = ptr.type = nise68.mem.peekB(0x00 + nise68.reg.getAl(1));
                    mpcmSt[ch].orig = ptr.orig = nise68.mem.peekB(0x01 + nise68.reg.getAl(1));
                    mpcmSt[ch].adrs_ptr = ptr.adrsPtr = nise68.mem.peekL(0x04 + nise68.reg.getAl(1));
                    mpcmSt[ch].size = ptr.size = nise68.mem.peekL(0x08 + nise68.reg.getAl(1));
                    mpcmSt[ch].start = ptr.start = nise68.mem.peekL(0x0c + nise68.reg.getAl(1));
                    mpcmSt[ch].end = ptr.end = nise68.mem.peekL(0x10 + nise68.reg.getAl(1));
                    mpcmSt[ch].count = ptr.count = nise68.mem.peekL(0x14 + nise68.reg.getAl(1));
                    //mpcmSt[ch].frq = mpcmSt[ch].type == 0xff ? 4 : (mpcmSt[ch].type == 1 ? 8 : (mpcmSt[ch].type == 2 ? 0x10 : 0));
                    if (mpcm != null) {
                        mpcmSt[ch].rate = mpcm.chips[0].rate;
                        mpcmSt[ch].base_ = mpcm.chips[0].base;
                    }

                    //nise68.dumpMemory((int) ptr.adrs_ptr, (int) (ptr.adrs_ptr + ptr.size));
                    if (mpcm != null) mpcm.writePcm(0, ch, ptr);
                } else {
                    SETPCM ptr = new SETPCM();
                    ptr.adrs_buf = nise68.mem.mem;
                    mpcmSt[ch].type = ptr.type = nise68.mem.peekB(0x00 + nise68.reg.getAl(1));
                    mpcmSt[ch].orig = ptr.orig = nise68.mem.peekB(0x01 + nise68.reg.getAl(1));
                    mpcmSt[ch].adrs_ptr = ptr.adrs_ptr = nise68.mem.peekL(0x04 + nise68.reg.getAl(1));
                    mpcmSt[ch].size = ptr.size = nise68.mem.peekL(0x08 + nise68.reg.getAl(1));
                    mpcmSt[ch].start = ptr.start = nise68.mem.peekL(0x0c + nise68.reg.getAl(1));
                    mpcmSt[ch].end = ptr.end = nise68.mem.peekL(0x10 + nise68.reg.getAl(1));
                    mpcmSt[ch].count = ptr.count = nise68.mem.peekL(0x14 + nise68.reg.getAl(1));
                    //mpcmSt[ch].frq = mpcmSt[ch].type == 0xff ? 4 : (mpcmSt[ch].type == 1 ? 8 : (mpcmSt[ch].type == 2 ? 0x10 : 0));
                    if (mpcmpp != null) {
                        mpcmSt[ch].rate = mpcmpp.chips[0].rate;
                        mpcmSt[ch].base_ = mpcmpp.chips[0].base;
                    }

                    //nise68.dumpMemory((int) ptr.adrs_ptr, (int) (ptr.adrs_ptr + ptr.size));
                    if (mpcmpp != null) mpcmpp.setPcm(0, ch, ptr);
                }
                break;
            case 0x0300:
                //logger.log(Level.TRACE, "MPCM #M_SET_FRQ($%04x) D1$%08x".formatted(n, nise68.reg.GetDl(1)));
                if (mpcmType == 0) if (mpcm != null) mpcm.setFreq(0, ch, nise68.reg.getDl(1) & 0xff);
                else if (mpcmpp != null) mpcmpp.setFreq(0, ch, nise68.reg.getDl(1) & 0xff);
                mpcmSt[ch].frq = (int) nise68.reg.getDl(1);
                break;
            case 0x0400:
                //logger.log(Level.TRACE, "MPCM #M_SET_PITCH($%04x) D1$%04x".formatted(n, nise68.reg.GetDl(1)));
                if (mpcmType == 0) if (mpcm != null) mpcm.setPitch(0, ch, nise68.reg.getDl(1) & 0xff);
                else if (mpcmpp != null) mpcmpp.setPitch(0, ch, nise68.reg.getDl(1) & 0xff);
                mpcmSt[ch].pitch = (int) nise68.reg.getDl(1);
                break;
            case 0x0500:
                //logger.log(Level.TRACE, "MPCM #M_SET_VOL($%04x) = $%02x".formatted(n, nise68.reg.GetDb(1)));
                if (mpcmType == 0) if (mpcm != null) mpcm.setVol(0, ch, nise68.reg.getDb(1) & 0xff);
                else if (mpcmpp != null) mpcmpp.setVol(0, ch, nise68.reg.getDb(1) & 0xff);
                mpcmSt[n & 0xf].volume = nise68.reg.getDb(1) & 0xff;
                break;
            case 0x0600:
                //logger.log(Level.TRACE, "MPCM #M_SET_PAN($%04x) = $%02x".formatted(n, nise68.reg.GetDb(1)));
                if (mpcmType == 0) if (mpcm != null) mpcm.setPan(0, ch, nise68.reg.getDb(1) & 0xff);
                else if (mpcmpp != null) mpcmpp.setPan(0, ch, nise68.reg.getDb(1) & 0xff);
                mpcmSt[n & 0xf].pan = nise68.reg.getDb(1) & 0xff;
                break;
            case 0x8000: //
                switch (n & 0x000f) {
                    case 0x0:
                        //logger.log(Level.TRACE, "MPCM #M_LOCK($%04x)".formatted(n));
                        break;
                    case 0x2: //
                        //logger.log(Level.TRACE, "MPCM #M_INIT($%04x)".formatted(n));
                        if (mpcmType == 0) if (mpcm != null) mpcm.reset(0);
                        else if (mpcmpp != null) mpcmpp.reset(0);
                        break;
                    case 0x5: //
                        //logger.log(Level.TRACE, "MPCM #M_SET_VOLTBL($%04x)".formatted(n));
                        int[] vtbl = new int[128];
                        for (int i = 0; i < 128; i++) {
                            vtbl[i] = nise68.mem.peekW(nise68.reg.getAl(1) + (i * 2)) & 0xffff;
                        }
                        if (mpcmType == 0) if (mpcm != null) mpcm.setVolTableZms(0, nise68.reg.getDl(1), vtbl);
                        else if (mpcmpp != null) mpcmpp.setVolTableZms(0, (int) nise68.reg.getDl(1), vtbl);
                        break;
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return 0;
    }

    private int pcm8CallBack(int n) {
        int ch;
        switch (n & 0xfff0) {
            case 0x0000:
                //File.WriteAllBytes("c:\\temp\\test.bin", nise68.mem.mem);
                if (pcm8type == 0)
                    if (opmPCM != null)
                        opmPCM.chips[0].pcm8Out((int) n & 0xff, null, nise68.reg.getAl(1), (int) nise68.reg.getDl(1), (int) nise68.reg.getDl(2)); // Start of specified channel sound
                    else if (pcm8pp != null)
                        pcm8pp.keyOn(0, (int) n & 0xff, nise68.reg.getAl(1), (int) nise68.reg.getDl(1), (int) nise68.reg.getDl(2)); // Start of specified channel sound // TODO vavi check args
                //logger.log(Level.TRACE, "%s adrsPtr = 0x%08x;  mode = 0x%08x; len = 0x%08x;".formatted(nise68.reg.getAl(1), (int)nise68.reg.getDl(1), (int) nise68.reg.getDl(2), (int) n & 0xff));
                ch = (int) ((n & 0xff) % 8);
                pcm8St[ch].tablePtr = nise68.reg.getAl(1);
                pcm8St[ch].mode = nise68.reg.getDl(1);
                pcm8St[ch].length = nise68.reg.getDl(2);
                pcm8St[ch].Keyon = true;
                break;
            case 0x0100:
                switch (n & 0xffff) {
                    case 0x0100:
                        ch = (int) ((n & 0xff) % 8);
                        pcm8St[ch].tablePtr = 0;
                        pcm8St[ch].mode = 0;
                        pcm8St[ch].length = 0;
                        pcm8St[ch].Keyon = false;
                        if (pcm8type == 0)
                            if (opmPCM != null)
                                opmPCM.chips[0].pcm8Out((int) n & 0xff, null, 0, 0, 0); // Stop the specified channel
                            else if (pcm8pp != null) pcm8pp.keyOff(0, (int) n & 0xff); // Stop the specified channel
                        break;
                    case 0x0101:
                        if (opmPCM != null) opmPCM.chips[0].pcm8Abort(); // Stop all channels
                        break;
                }
                break;
            case 0x01f0:
                switch (n & 0xffff) {
                    case 0x01FC:
                        nise68.reg.setDl(0, 1);
                        break;
                }
                break;
            default:
                break;
        }
        return 0;
    }

    private int opmCallBack(int adr, int dat) {
        plugin.audio.chipRegister.chip(Ym2151Chip.class).write(0, 0, adr, dat, model, plugin.audio.chipRegister.chip(Ym2151Chip.class).hosei[0], vgmFrameCounter);
        if (timerOPM != null) timerOPM.writeReg((byte) adr, (byte) dat);
        return 0;
    }

    private int midiCallBack(int n, byte dat) {
        //if (midiOutsBuff == null || midiOutsFrame == null) return 0;
        //if (n >= midiOutsBuff.count) return 0;

        //midiOutsBuff[n].add(dat);
        //midiOutsFrame[n].add(virtualFrameCounter);
        plugin.audio.chipRegister.plugin(MidiPlugin.class).send(model, n, new byte[] {dat}, 0);
        return 0;
    }

    private int sccCallBack(int n, byte dat) {
        //if (midiOutsBuff == null || midiOutsFrame == null) return 0;
        //if (2 + n >= midiOutsBuff.count) return 0;

        //midiOutsBuff[2 + n].add(dat);
        //midiOutsFrame[2 + n].add(virtualFrameCounter);
        plugin.audio.chipRegister.plugin(MidiPlugin.class).send(model, 2 + n, new byte[] {dat}, 0);
        return 0;
    }

    private void setZPDSearchPath() {
        try {
            // Get the environment variable "ZPD"
            String envZPD = "";
            try {
                envZPD = System.getProperty("mdplayer.zms.zpd");
            } catch (Exception e) {
            }
            if (envZPD != null && !envZPD.isEmpty()) {
                envZPDs = Arrays.asList(envZPD.split(";"));
            }
        } catch (Exception e) {
            envZPDs = Collections.emptyList();
        }
    }
}
