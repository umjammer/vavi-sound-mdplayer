package mdplayer;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileShare;
import dotnet4j.io.FileStream;
import dotnet4j.io.Stream;
import dotnet4j.util.compat.Tuple3;
import mdplayer.driver.Vgm;
import mdplayer.driver.Vgm.Gd3;
import mdplayer.instruments.VRC7;
import mdsound.Instrument;
import mdsound.instrument.*;
import vavi.awt.dnd.BasicDTListener;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;
import static java.lang.System.getLogger;
import static java.util.function.Predicate.not;


public class Common {

    private static final Logger logger = getLogger(Common.class.getName());

    private static String[] args;

    static void setCommandLineArgs(String[] args) {
        Common.args = args;
    }

    public static String[] getCommandLineArgs() {
        return args;
    }

    /**
     *
     * @return 0 origin
     */
    public static int getFilterIndex(JFileChooser fc) {
        FileFilter[] filters = fc.getChoosableFileFilters();
        for (int i = 0; i < filters.length; i++) {
            if (fc.getFileFilter() == filters[i]) {
                return i;
            }
        }
        return -1;
    }

    public static final int DEV_WaveOut = 0;
    public static final int DEV_DirectSound = 1;
    public static final int DEV_WasapiOut = 2;
    public static final int DEV_AsioOut = 3;
    public static final int DEV_SPPCM = 4;
    public static final int DEV_Null = 5;

    public static final int VGMProcSampleRate = 44100;
    public static final int NsfClock = 1789773;
    public static Path settingFilePath;
    public static Path playingFilePath;

    public static int getBE16(byte[] buf, int adr) {
        if (buf == null || buf.length - 1 < adr + 1) {
            throw new IndexOutOfBoundsException();
        }

        int dat;
        dat = (buf[adr] & 0xff) * 0x100 + (buf[adr + 1] & 0xff);

        return dat;
    }

    /** find an asciiz string from a byte array */
    public static byte[] getByteArray(byte[] buf, int[] adr) {
        if (adr[0] >= buf.length) throw new IndexOutOfBoundsException(adr[0] + " > " + buf.length);

        List<Byte> ary = new ArrayList<>();
        while (buf[adr[0]] != 0 || buf[adr[0] + 1] != 0) {
            ary.add(buf[adr[0]]);
            adr[0]++;
            ary.add(buf[adr[0]]);
            adr[0]++;
        }
        adr[0] += 2;

        return toByteArray(ary);
    }

    public static Vgm.Gd3 getGD3Info(byte[] buf, int adr) {
        Gd3 gd3 = new Vgm.Gd3();

        gd3.trackName = "";
        gd3.trackNameJ = "";
        gd3.gameName = "";
        gd3.gameNameJ = "";
        gd3.systemName = "";
        gd3.systemNameJ = "";
        gd3.composer = "";
        gd3.composerJ = "";
        gd3.converted = "";
        gd3.notes = "";
        gd3.vgmBy = "";
        gd3.version = "";
        gd3.usedChips = "";

        try {
            int[] adr_ = new int[] {adr};
            gd3.trackName = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.trackNameJ = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.gameName = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.gameNameJ = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.systemName = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.systemNameJ = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.composer = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.composerJ = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.converted = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.vgmBy = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            gd3.notes = new String(Common.getByteArray(buf, adr_), StandardCharsets.UTF_8);
            // Lyric(Custom extensions)
            byte[] bLyric = Common.getByteArray(buf, adr_);
            gd3.lyrics = new ArrayList<>();
            int i = 0;
            int st = 0;
            while (i < bLyric.length) {
                int h = bLyric[i] & 0xff;
                int l = bLyric[i + 1] & 0xff;
                if ((h == 0x5b && l == 0x00 && i != 0) || i >= bLyric.length - 2) {
                    if ((i >= bLyric.length - 2) || (bLyric[i + 2] != 0x5b || bLyric[i + 3] != 0x00)) {
                        String m = new String(bLyric, st, i - st + ((i >= bLyric.length - 2) ? 2 : 0), StandardCharsets.UTF_8);
                        st = i;

                        int cnt = Integer.parseInt(m.substring(1, m.indexOf("]") - 1));
                        m = m.substring(m.indexOf("]") + 1);
                        gd3.lyrics.add(new Tuple3<>(cnt, cnt, m));
                    }
                }
                i += 2;
            }

        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }

        return gd3;
    }

    public static String getNRDString(byte[] buf,/*ref*/ int[] index) {
        if (buf == null || buf.length < 1 || index[0] < 0 || index[0] >= buf.length) return "";

        try {
            List<Byte> lst = new ArrayList<>();
            for (; buf[index[0]] != 0; index[0]++) {
                if (buf.length > index[0] + 1 && buf[index[0]] == 0x1a && buf[index[0] + 1] == 0x00)
                    break;
                lst.add(buf[index[0]]);
            }

            String n = new String(toByteArray(lst), Charset.forName("MS932"));
            index[0]++;

            return n;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return "";
    }

    public static int range(int n, int min, int max) {
        return (n > max) ? max : Math.max(n, min);
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

    public static int searchFMNote(int freq) {
        int m = Integer.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 5; i++) {
            //if (freq < Tables.FmFNum[i]) break;
            //n = i;
            int a = Math.abs(freq - Tables.FmFNum[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n - 12 * 3;
    }

    public static int searchSSGNote(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            // if (freq < Tables.freqTbl[i]) break;
            // n = i;
            float a = Math.abs(freq - Tables.freqTbl[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public static int getOPENAIRRhythmStream(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            //if (freq < Tables.freqTbl[i]) break;
            //n = i;
            float a = Math.abs(freq - Tables.freqTbl[i]);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public static int searchSegaPCMNote(double ml) {
        double m = Double.MAX_VALUE;
        int n = 0;
        for (int i = 0; i < 12 * 8; i++) {
            double a = Math.abs(ml - (Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((i / 12) - 4))));
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public static int searchPCMNote(int ml, int mul) {
        int m = Integer.MAX_VALUE;
        ml = ml % (1024 * mul);
        int n = 0;
        for (int i = 0; i < 12; i++) {
            int a = Math.abs(ml - Tables.pcmpitchTbl[i] * mul);
            if (m > a) {
                m = a;
                n = i;
            }
        }
        return n;
    }

    public static int searchYM2608Adpcm(float freq) {
        float m = Float.MAX_VALUE;
        int n = 0;

        for (int i = 0; i < 12 * 8; i++) {
            if (freq < Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((i / 12) - 3))) break;
            n = i;
            float a = Math.abs(freq - (float) (Tables.pcmMulTbl[i % 12 + 12] * Math.pow(2, ((i / 12) - 3))));
            if (m > a) {
                m = a;
                n = i;
            }
        }

        return n + 1;
    }

    public static int getYM2151Hosei(float ym2151ClockValue, float baseClock) {
        int ret = 0;

        float delta = ym2151ClockValue / baseClock;
        float d;
        float oldD = Float.MAX_VALUE;
        for (int i = 0; i < Tables.pcmMulTbl.length; i++) {
            d = Math.abs(delta - Tables.pcmMulTbl[i]);
            ret = i;
            if (d > oldD) break;
            oldD = d;
        }
        ret -= 12;

        return ret;
    }

    public static Path getApplicationFolder() {
        return Path.of(System.getProperty("user.dir"));
    }

    public static Path getApplicationDataFolder(boolean make/* = false*/) {
        try {
            String appPath = System.getProperty("user.dir");
            Path fullPath = Path.of(appPath, "./config/kuma", "mdplayer");
            if (!Files.exists(fullPath)) Files.createDirectory(fullPath);

            return fullPath;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    public static Path getOperationFolder(boolean make/* = false*/) {
        try {
            Path appDataFolder = getApplicationDataFolder(false);
            if (appDataFolder == null) return null;
            Path fullPath = appDataFolder.resolve("operation");
            if (!Files.exists(fullPath)) Files.createDirectory(fullPath);
            else
                // If it exists, clear the contents of that folder.
                deleteDataUnderDirectory(fullPath);
            return fullPath;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Empty the directory
     */
    public static void deleteDataUnderDirectory(Path directory) throws IOException {
logger.log(Level.DEBUG, "delete: " + directory);
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .filter(p -> p != directory)
                .map(Path::toFile)
                .forEach(java.io.File::delete);
    }

    /**
     * Change the attributes of a folder or file
     */
    public static void removeReadonlyAttribute(Path dir) {
logger.log(Level.DEBUG, "delete attributes: " + dir);
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .filter(p -> p != dir)
                    .map(Path::toFile)
                    .filter(not(java.io.File::canWrite))
                    .forEach(f -> f.setWritable(true));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream getOPNARyhthmStream(String fn) {
        try {
            Path ffn = Path.of(fn);

            Path chk;

            chk = playingFilePath.resolve(fn);
            if (Files.exists(chk))
                ffn = chk;
            else {
                chk = getApplicationFolder().resolve(fn);
                if (Files.exists(chk)) ffn = chk;
                else {
                    // TODO mdsound in mdplayer
                    chk = Path.of(System.getProperty("mdsound.pcm.path", "")).resolve(fn);
                    if (Files.exists(chk)) ffn = chk;
                }
            }

logger.log(Level.DEBUG, "rhythm file: " + ffn);
            if (!Files.exists(ffn)) return null;
            FileStream fs = new FileStream(ffn.toString(), FileMode.Open, FileAccess.Read, FileShare.Read);
            return fs;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    public enum EnmModel {
        VirtualModel, RealModel
    }

    public enum EnmChip {
        Unuse(),
        SN76489(Sn76489Inst.class, Sn76489Inst.class),
        YM2612(Ym2612Inst.class, Ym3438Inst.class, MameYm2612Inst.class, SimpleYm3438Inst.class),
        YM2612Ch6(),
        RF5C164(ScdPcmInst.class),
        PWM(PwmInst.class),
        C140(C140Inst.class),
        OKIM6258(OkiM6258Inst.class),
        OKIM6295(OkiM6295Inst.class),
        SEGAPCM(SegaPcmInst.class),
        YM2151(Ym2151Inst.class, MameYm2151Inst.class, X68kYm2151Inst.class, YmFmYm2151Inst.class),
        YM2608(Ym2608Inst.class, YmFmYm2608Inst.class),
        YM2203(Ym2203Inst.class, YmFmYm2203Inst.class),
        YM2610(Ym2610Inst.class, YmFmYm2610Inst.class),
        AY8910(Ay8910Inst.class, MameAy8910Inst.class),
        HuC6280(HuC6280Inst.class),
        YM2413(Ym2413Inst.class, VRC7.class),
        NES(NesInst.class), DMC(), FDS(), MMC5(),
        YMF262(YmF262Inst.class),
        YMF278B(YmF278BInst.class),
        VRC7(),
        C352(C352Inst.class),
        YM3526(Ym3526Inst.class),
        Y8950(Y8950Inst.class),
        YM3812(Ym3812Inst.class, MameYm3812Inst.class),
        K051649(K051649Inst.class),
        N163(), VRC6(), FME7(),
        RF5C68(Rf5C68Inst.class),
        MultiPCM(MultiPcmInst.class),
        YMF271(YmF271Inst.class),
        YMZ280B(YmZ280BInst.class),
        QSound(CtrQSoundInst.class, QSoundInst.class),
        GA20(Ga20Inst.class),
        K053260(K053260Inst.class),
        K054539(K054539Inst.class),
        DMG(DmgInst.class),
        SAA1099(Saa1099Inst.class),
        X1_010(X1_010Inst.class),
        PPZ8(), PPSDRV(), SID(), P86(),
        POKEY(PokeyInst.class),
        WSwan(WSwanInst.class),
        S_SN76489(), S_YM2612(), S_YM2612Ch6(), S_RF5C164(), S_PWM(),
        S_C140(), S_OKIM6258(), S_OKIM6295(), S_SEGAPCM(), S_YM2151(), S_YM2608(), S_YM2203(), S_YM2610(),
        S_AY8910(), S_HuC6280(), S_YM2413(), S_NES(), S_DMC(), S_FDS(), S_MMC5(), S_YMF262(),
        S_YMF278B(), S_VRC7(), S_C352(), S_YM3526(), S_Y8950(), S_YM3812(), S_K051649(), S_N163(),
        S_VRC6(), S_FME7(), S_RF5C68(), S_MultiPCM(), S_YMF271(), S_YMZ280B(), S_QSound(), S_GA20(),
        S_K053260(), S_K054539(), S_DMG(), S_SAA1099(), S_X1_010(), S_PPZ8(), S_PPSDRV(), S_SID(),
        S_P86(), S_POKEY(), S_WSwan();
        final Class<? extends Instrument>[] variants;
        @SafeVarargs EnmChip(Class<? extends Instrument>... variants) {
            this.variants = variants;
        }

        public Class<? extends Instrument> getInstClass(int i) {
            return variants[i];
        }
    }

    public enum EnmRealChipType {
        YM2608(1), YM2151(2), YM2610(3), YM2203(4),
        YM2612(5), AY8910(6), SN76489(7), YM3812(8),
        YMF262(9), YM2413(10), YM3526(11), K051649(13),
        SPPCM(42), C140(43), SEGAPCM(44);
        final int v;

        EnmRealChipType(int v) {
            this.v = v;
        }
    }

    public enum EnmInstFormat {
        FMP7,
        MDX,
        TFI,
        MUSICLALF,
        MUSICLALF2,
        MML2VGM,
        NRTDRV,
        HUSIC,
        VOPM,
        PMD,
        MUCOM88,
        DMP,
        OPNI,
        OPLI,
        MGSCSCC_PLAIN,
        RYM2612,
        SendMML2VGM
    }

    public enum EnmArcType {
        unknown,
        ZIP,
        LZH
    }

    public enum EnmRealModel {
        unknown,
        SCCI,
        GIMIC
    }

    public static String getClipboard() {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Clipboard clip = kit.getSystemClipboard();

        try {
            return (String) clip.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new dotnet4j.io.IOException(e);
        }
    }

    public static void setClipboard(String str) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Clipboard clip = kit.getSystemClipboard();

        StringSelection ss = new StringSelection(str);
        clip.setContents(ss, ss);
    }

    static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void sendKey(int mod, int key) {
        robot.setAutoWaitForIdle(true);
        robot.keyPress(mod);
        robot.keyPress(key);
        robot.keyRelease(mod);
    }

    /**
     * this is the DnD target sample for a file name from external applications
     * <pre>
     *   new DropTarget(component, DnDConstants.ACTION_COPY_OR_MOVE, new DTListener(), true);
     * </pre>
     */
    public static class DTListener extends BasicDTListener {

        private final Consumer<List<java.io.File>> drop;

        public DTListener(Consumer<List<java.io.File>> drop) {
            this.drop = drop;
            this.dragAction = DnDConstants.ACTION_COPY_OR_MOVE;
        }

        /**
         * Called by isDragOk
         * Checks to see if the flavor drag flavor is acceptable
         * @param ev the DropTargetDragEvent object
         * @return whether the flavor is acceptable
         */
        @Override
        protected boolean isDragFlavorSupported(DropTargetDragEvent ev) {
            return ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        /**
         * Called by drop
         * Checks the flavors and operations
         * @param ev the DropTargetDropEvent object
         * @return the chosen dataFlavor or null if none match
         */
        @Override
        protected DataFlavor chooseDropFlavor(DropTargetDropEvent ev) {
// logger.log(Level.TRACE, ev.getCurrentDataFlavorsAsList());
            if (ev.isLocalTransfer() && ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return DataFlavor.javaFileListFlavor;
            }
            DataFlavor chosen = null;
            if (ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                chosen = DataFlavor.javaFileListFlavor;
            }
            return chosen;
        }

        /**
         * You need to implement here dropping procedure.
         * data is deserialized clone
         * @param data dropped
         */
        @Override
        @SuppressWarnings("unchecked")
        protected boolean dropImpl(DropTargetDropEvent ev, Object data) {
            drop.accept((List<java.io.File>) data);
            return true;
        }
    }
}
