package mdplayer.driver.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.lib.fmp.FMP;
import mdplayer.lib.fmp.FmpWork;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.lib.fmp.FMP.decodePc98ShiftJis;
import static mdplayer.lib.fmp.FMP.normalizeKanji;


/**
 * FMP
 *
 * system properties:
 * <li>{@code mdplayer.fmp.dir} ... location for fmp.com </li>
 * <li>{@code mdplayer.fmp.pvi} ... location for (.pvi) pcm files </li>
 *
 * @author kumatan
 */
public class FmpDriver extends BaseDriver {

    private static final Logger logger = getLogger(FmpDriver.class.getName());

    private final FMP fmp;

    public FmpDriver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        fmp = new FMP();
        fmp.setSearchPath(setting.getFileSearchPathList());
        fmp.sampleRate = Common.VGMProcSampleRate;
        fmp.charset = Common.charset;
        fmp.dir = System.getProperty("mdplayer.fmp.dir", System.getProperty("user.dir"));
        fmp.blockWrite = b -> this.isDataBlock = b;
        fmp.setPPZ8PCMData = this::setPPZ8PCMData;
        fmp.setPPZ8PCMFilename = this::setPPZ8PCMFilename;
        fmp.setPPZ8Data = this::setPPZ8Data;
        fmp.opnaWrite = this::opnaWrite;
    }

    public FmpDriver() {
        this(null); // gross
    }

    public void setFileTemp(FileTemp ft) {
        fmp.ft = ft;
    }

    public static int getBaseClock() {
        return FMP.baseClock;
    }

    /** before using ths method, you must do {@link BaseDriver#init} */
    public void compile() {
        fmp.playingFileName = plugin.playingFileName;
        fmp.playingArcFileName = plugin.playingArcFileName;

        fmp.compile();
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        MetaData md = new MetaData();

        if (buf == null || buf.length < 2) {
            return md;
        }

        try {
            List<String> rawLines = new ArrayList<>();
            int memoPtr = (ByteUtil.readLeShort(buf, 0) & 0xffff);
            boolean isBinaryFmc = (memoPtr > 0 && memoPtr + 4 <= buf.length &&
                    buf[memoPtr] == 'F' && buf[memoPtr + 1] == 'M' && buf[memoPtr + 2] == 'C');

            if (isBinaryFmc) {
                int ptr = memoPtr + 4;
                while (ptr < buf.length) {
                    if (buf[ptr] == 0) {
                        ptr++;
                        if (ptr >= buf.length || buf[ptr] == 0) break;
                    }
                    int start = ptr;
                    while (ptr < buf.length && buf[ptr] != 0 && buf[ptr] != 0x0d && buf[ptr] != 0x0a) {
                        ptr++;
                    }
                    String s = stripEscapes(decodePc98ShiftJis(buf, start, ptr)).stripTrailing();
                    if (!s.isEmpty()) {
                        rawLines.add(s);
                    }
                    if (ptr < buf.length && buf[ptr] == 0x0d) ptr++;
                    if (ptr < buf.length && buf[ptr] == 0x0a) ptr++;
                }
            } else {
                String text = decodePc98ShiftJis(buf, 0, buf.length);
                String[] lines = text.split("\\r?\\n");
                for (String line : lines) {
                    rawLines.add(stripEscapes(line).stripTrailing());
                }
            }

            // as they are to be displayed: an FMC memo is an 80 column screen image, so the leading
            // spaces are its layout - see Tag.Memo below. The tags are read from them stripped.
            List<String> plainComments = new ArrayList<>();

            for (String raw : rawLines) {
                String line = raw.strip();
                if (line.isEmpty()) continue;

                String lower = line.toLowerCase();
                if (lower.startsWith("#title")) {
                    String val = normalizeKanji(extractTagValue(line, "#title"));
                    if (!val.isEmpty()) {
                        md.set(Tag.Title, val);
                        md.set(Tag.TitleJ, val);
                    }
                } else if (lower.startsWith("#composer")) {
                    String val = normalizeKanji(extractTagValue(line, "#composer"));
                    if (!val.isEmpty()) {
                        md.set(Tag.Composer, val);
                        md.set(Tag.ComposerJ, val);
                    }
                } else if (lower.startsWith("#author")) {
                    String val = normalizeKanji(extractTagValue(line, "#author"));
                    if (!val.isEmpty()) {
                        if (md.getFirst(Tag.Composer).isEmpty()) {
                            md.set(Tag.Composer, val);
                            md.set(Tag.ComposerJ, val);
                        } else {
                            md.set(Tag.Maker, val);
                        }
                    }
                } else if (lower.startsWith("#arranger")) {
                    String val = normalizeKanji(extractTagValue(line, "#arranger"));
                    if (!val.isEmpty()) {
                        md.set(Tag.Arranger, val);
                    }
                } else if (lower.startsWith("#memo") || lower.startsWith("#comment")) {
                    String val = normalizeKanji(extractTagValue(line, lower.startsWith("#memo") ? "#memo" : "#comment"));
                    if (!val.isEmpty()) {
                        String existing = md.getFirst(Tag.Note);
                        md.set(Tag.Note, existing.isEmpty() ? val : existing + "\n" + val);
                    }
                } else if (lower.startsWith("#game")) {
                    String val = normalizeKanji(extractTagValue(line, "#game"));
                    if (!val.isEmpty()) {
                        md.set(Tag.GameTitle, val);
                        md.set(Tag.GameTitleJ, val);
                    }
                } else if (lower.startsWith("#maker")) {
                    String val = normalizeKanji(extractTagValue(line, "#maker"));
                    if (!val.isEmpty()) {
                        md.set(Tag.Maker, val);
                    }
                } else if (isBinaryFmc) {
                    plainComments.add(normalizeKanji(raw));
                } else if (line.startsWith(";")) {
                    String comment = normalizeKanji(line.substring(1).trim());
                    if (!comment.isEmpty()) {
                        plainComments.add(comment);
                    }
                }
            }

            if (md.getFirst(Tag.Title).isEmpty() && !plainComments.isEmpty()) {
                md.set(Tag.Title, plainComments.getFirst().strip());
                md.set(Tag.TitleJ, plainComments.getFirst().strip());
            }
            if (md.getFirst(Tag.Composer).isEmpty() && plainComments.size() > 1) {
                md.set(Tag.Composer, plainComments.get(1).strip());
                md.set(Tag.ComposerJ, plainComments.get(1).strip());
            }
            if (md.getFirst(Tag.Note).isEmpty() && md.getFirst(Tag.Arranger).isEmpty() && plainComments.size() > 2) {
                md.set(Tag.Note, plainComments.get(2).strip());
            }

            // the memo as the PC-98 screen had it: the credits FMC puts at the top, indented to
            // wherever the author placed them, which is what the fmdsp comment lines want
            if (isBinaryFmc && !plainComments.isEmpty())
                md.setAll(Tag.Comments, plainComments.subList(0, Math.min(3, plainComments.size())));

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return md;
    }

    /**
     * Drops the terminal control an FMC memo may carry, leaving the text: ANSI escape sequences,
     * which the author uses to colour the credits and to place the lyrics on the screen, and FMP's
     * own {@code ESC ! n} markers, which sync a lyric page to the music. The three comment lines
     * have no room to honour any of it, and it would otherwise be drawn as its own characters.
     *
     * TODO common?
     */
    private static String stripEscapes(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != 0x1b) {
                // the C0 controls left over are markers too, and no font has a glyph for them
                if (c >= 0x20 || c == '\t') sb.append(c);
                i++;
                continue;
            }
            i++;
            if (i >= s.length()) break;
            char c2 = s.charAt(i);
            if (c2 == '[') { // CSI: parameters, then intermediates, then the one byte that ends it
                i++;
                while (i < s.length() && s.charAt(i) >= 0x30 && s.charAt(i) <= 0x3f) i++;
                while (i < s.length() && s.charAt(i) >= 0x20 && s.charAt(i) <= 0x2f) i++;
                if (i < s.length()) i++;
            } else if (c2 >= 0x20 && c2 <= 0x2f) { // ESC ! n, and the character set designations
                i += 2;
            } else if (c2 >= 0x30 && c2 <= 0x7e) { // the two byte escapes
                i++;
            } // anything else was a stray ESC, and only it is dropped
        }
        return sb.toString();
    }

    /** TODO common? */
    private static String extractTagValue(String line, String tag) {
        String val = line.substring(tag.length()).trim();
        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
            val = val.substring(1, val.length() - 1).trim();
        } else if (val.startsWith("'") && val.endsWith("'") && val.length() >= 2) {
            val = val.substring(1, val.length() - 1).trim();
        }
        return val;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {

        metaData = retrieveMetaData(dataBuf, 0);

        loopCounter = 0;
        curLoop = 0;
        this.model = model;
        frameCounter = -latency - waitTime;

        fmp.playingFileName = plugin.playingFileName;
        fmp.playingArcFileName = plugin.playingArcFileName;

        try {
            fmp.run(dataBuf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Extract PVI/PPZ names from the song data header, matching the C reference
        // fmp_load(). These are the canonical PCM filenames shown on the file bar.
        // The PPZ8 callback may override them later if FMP.COM loads PCM at runtime.
        parsePcmNames(dataBuf, fmp.getWork());
    }

    /** how many {@link #processOneFrame()} calls between two "fmp" events */
    private static final int visualizeInterval = Common.VGMProcSampleRate / 120;

    private int visualizeCounter;

    @Override
    public void processOneFrame() {
        try {
            speedCounter += (double) Common.VGMProcSampleRate / setting.getOutputDevice().getSampleRate() * speed;
            while (speedCounter >= 1.0) {
                speedCounter -= 1.0;

                if (frameCounter > -1) {
                    counter++;

                    fmp.nise98.runTimer();
                    if (!fmp.nise98.intTimer()) continue;
                    curLoop = fmp.processOneFrame(() -> stopped = true);
                    // FMP's interrupt is the OPNA timer B, so one frame here is one timer B tick
                    fmp.getWork().ticks++;
                }
                frameCounter++;
            }

            if (++visualizeCounter >= visualizeInterval) {
                visualizeCounter = 0;
                FmpWork work = fmp.getWork();
                work.ppz8 = plugin.chipRegister.chip(Ppz8Chip.class).getInfo(0);
                fireEventHappened(this, "fmp", work);
            }

            //curLoop = mm.readShort(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void setPPZ8PCMFilename(int mode, String fn) {
        FmpWork work = fmp.getWork();
        if (work != null) {
            if (mode == 0) work.pviName = fn;
            else work.ppzName = fn;
        }
    }

    @Override
    public String pcmType(int index) {
        return switch (index) {
            case 0 -> "PVI";
            case 1 -> "PPZ";
            default -> null;
        };
    }

    @Override
    public String pcmFilename(int index) {
        FmpWork work = fmp.getWork(); // TODO expose fmp package
        if (work == null) return null;
        return switch (index) {
            case 0 -> work.pviName;
            case 1 -> work.ppzName;
            default -> null;
        };
    }

    @Override
    public boolean pcmError(int index) {
        FmpWork work = fmp.getWork(); // TODO expose fmp package
        if (work == null) return false;
        return switch (index) {
            case 0 -> work.pviError;
            case 1 -> work.ppzError;
            default -> false;
        };
    }

    /**
     * Extracts PVI and PPZ filenames from the FMP song data header.
     * <p>
     * Mirrors the C reference {@code fmp_load()} in {@code fmdriver_fmp.c}:
     * the SSG tone pointer at {@code read16le(data)} doubles as the end of the
     * sequence data; 0x12 bytes before it sit the 8-byte PPZ and PVI names.
     * Format versions ({@code dataver}) below 0x2a have no embedded names
     * ({@code pviname_valid = false}). The PPZ name is only valid when the PPZ
     * flag (bit 1 of the data flags byte) is set.
     */
    static void parsePcmNames(byte[] data, FmpWork work) {
        if (work == null || data == null || data.length < 4) return;

        int offset = (data[0] & 0xff) | ((data[1] & 0xff) << 8);
        if (offset + 4 > data.length) return;

        // Check for FMC header
        if (data[offset] != 'F' || data[offset + 1] != 'M' || data[offset + 2] != 'C') return;

        int dataver = data[offset + 3] & 0xff;
        boolean pvinameValid;
        int dataFlagsOffset;

        if (dataver <= 0x29) {
            // format 1: no embedded PCM names
            pvinameValid = false;
            dataFlagsOffset = 0x1b;
        } else if (dataver <= 0x49) {
            // format 2
            pvinameValid = true;
            dataFlagsOffset = 0x2f;
        } else if (dataver <= 0x69) {
            // format 3
            pvinameValid = true;
            dataFlagsOffset = 0x5f;
        } else {
            return; // unknown format
        }

        if (!pvinameValid) return;
        if (dataFlagsOffset >= data.length) return;

        boolean ppzFlag = (data[dataFlagsOffset] & 0x02) != 0;

        // pcmptr = read16le(data) - 0x12
        int pcmptr = offset - 0x12;
        if (pcmptr < 0 || pcmptr + 16 > data.length) return;

        // PVI name at pcmptr+8, PPZ name at pcmptr+0 (each 8 bytes, null-terminated)
        String pviName = extractName(data, pcmptr + 8, 8);
        if (pviName != null) {
            work.pviName = pviName;
        }

        if (ppzFlag) {
            String ppzName = extractName(data, pcmptr, 8);
            if (ppzName != null) {
                work.ppzName = ppzName;
            }
        }
    }

    /** Extracts a null-terminated ASCII name from data, returning null if empty. */
    private static String extractName(byte[] data, int offset, int maxLen) {
        int end = offset;
        while (end < offset + maxLen && end < data.length && data[end] != 0) {
            end++;
        }
        if (end == offset) return null;
        return new String(data, offset, end - offset, StandardCharsets.US_ASCII);
    }

    private void setPPZ8PCMData(int bank, int mode, byte[][] pcmData) {
        plugin.chipRegister.chip(Ppz8Chip.class).writePcm(0, bank, mode, pcmData, model);
        FmpWork work = fmp.getWork();
        if (work != null) {
            boolean err = (pcmData == null || bank < 0 || bank >= pcmData.length || pcmData[bank] == null);
            if (mode == 0) work.pviError = err;
            else work.ppzError = err;
        }
    }

    private void setPPZ8Data(int port, int adr, int data) {
        plugin.chipRegister.chip(Ppz8Chip.class).write(0, port, adr, data, model);
    }

    private void opnaWrite(int p, int a, int d) {
        int cn = p >> 8;
        int port = (p & 0xff) == 0x8a ? 0 : 1;
        plugin.chipRegister.chip(Ym2608Chip.class).write(0, port, a, d, model);

        echo(port, a, d);

        if (port == 1 && a == 0x8 && model == EnmModel.RealModel) {
            this.isDataBlock = true;
            fmp.pcmDataSendCount++;
            plugin.chipRegister.chip(Ym2608Chip.class).setSyncWait(0, 1);
        }
    }

    /**
     * The tempo, the rhythm keys and the SSG mixer never appear in FMP's work area - the driver
     * only ever writes them to the chip - so the visualizer reads them back from here.
     */
    private void echo(int port, int a, int d) {
        FmpWork work = fmp.getWork();
        if (port == 0) {
            switch (a) {
            case 0x06 -> work.ssgNoiseFreq = d & 0x1f;
            case 0x07 -> work.ssgMixer = d & 0xff;
            case 0x26 -> work.timerB = d & 0xff;
            default -> {}
            }
        } else if (port == 1) {
            switch (a) {
            case 0x10 -> {
                // bit 7 keys the instruments in the low bits off, anything else keys them on
                if ((d & 0x80) == 0) work.rhythmKeyOn |= d & 0x3f;
            }
            case 0x01 -> work.adpcmPan = d & 0xff;
            default -> {}
            }
        }
    }
}
