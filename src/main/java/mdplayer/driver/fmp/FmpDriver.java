package mdplayer.driver.fmp;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.Ppz8Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.emu.nise98.FileTemp;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;


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
    public MetaData getMetaData(byte[] buf, Object... args) {
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
                    String s = decodePc98ShiftJis(buf, start, ptr);
                    if (!s.isEmpty()) {
                        rawLines.add(s.trim());
                    }
                    if (ptr < buf.length && buf[ptr] == 0x0d) ptr++;
                    if (ptr < buf.length && buf[ptr] == 0x0a) ptr++;
                }
            } else {
                String text = decodePc98ShiftJis(buf, 0, buf.length);
                String[] lines = text.split("\\r?\\n");
                for (String line : lines) {
                    rawLines.add(line.trim());
                }
            }

            List<String> plainComments = new ArrayList<>();

            for (String line : rawLines) {
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
                    plainComments.add(normalizeKanji(line));
                } else if (line.startsWith(";")) {
                    String comment = normalizeKanji(line.substring(1).trim());
                    if (!comment.isEmpty()) {
                        plainComments.add(comment);
                    }
                }
            }

            if (md.getFirst(Tag.Title).isEmpty() && !plainComments.isEmpty()) {
                md.set(Tag.Title, plainComments.get(0));
                md.set(Tag.TitleJ, plainComments.get(0));
            }
            if (md.getFirst(Tag.Composer).isEmpty() && plainComments.size() > 1) {
                md.set(Tag.Composer, plainComments.get(1));
                md.set(Tag.ComposerJ, plainComments.get(1));
            }
            if (md.getFirst(Tag.Note).isEmpty() && md.getFirst(Tag.Arranger).isEmpty() && plainComments.size() > 2) {
                md.set(Tag.Note, plainComments.get(2));
            }

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        return md;
    }

    private static String normalizeKanji(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '－', '−', '‐', '‑', '‒', '–', '—' -> sb.append('ー');
                case '～' -> sb.append('〜');
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

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

        metaData = getMetaData(dataBuf, 0);

        loopCounter = 0;
        curLoop = 0;
        this.model = model;
        frameCounter = -latency - waitTime;

        fmp.playingFileName = plugin.playingFileName;
        fmp.playingArcFileName = plugin.playingArcFileName;

        try {
            fmp.run(dataBuf);
        } catch (Exception e) {
e.printStackTrace();
            throw new IllegalStateException(e);
        }
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

            //curLoop = mm.ReadUInt16(reg.a6 + dw.LOOP_COUNTER);
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }

    private void setPPZ8PCMData(int bank, int mode, byte[][] pcmData) {
        plugin.chipRegister.chip(Ppz8Chip.class).writePcm(0, bank, mode, pcmData, model);
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
            case 0x07 -> work.ssgMixer = d & 0xff;
            case 0x26 -> work.timerB = d & 0xff;
            default -> {
            }
            }
        } else if (port == 1) {
            switch (a) {
            case 0x10 -> {
                // bit 7 keys the instruments in the low bits off, anything else keys them on
                if ((d & 0x80) == 0) work.rhythmKeyOn |= d & 0x3f;
            }
            case 0x01 -> work.adpcmPan = d & 0xff;
            default -> {
            }
            }
        }
    }

    public static String decodePc98ShiftJis(byte[] buf, int start, int end) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < end) {
            int b1 = buf[i] & 0xff;
            if (b1 == 0) break;

            if (b1 == 0x85 && i + 1 < end) {
                int b2 = buf[i + 1] & 0xff;
                int k = b2 + 0x40;
                if (k >= 0xa1 && k <= 0xdf) {
                    sb.append((char)(0xff61 + (k - 0xa1)));
                    i += 2;
                    continue;
                }
            }

            if (b1 >= 0xa1 && b1 <= 0xdf) {
                sb.append((char)(0xff61 + (b1 - 0xa1)));
                i++;
                continue;
            }

            if (((b1 >= 0x81 && b1 <= 0x9f) || (b1 >= 0xe0 && b1 <= 0xfc)) && i + 1 < end) {
                int b2 = buf[i + 1] & 0xff;
                if ((b2 >= 0x40 && b2 <= 0x7e) || (b2 >= 0x80 && b2 <= 0xfc)) {
                    byte[] sjis = new byte[] {(byte)b1, (byte)b2};
                    String s = new String(sjis, Charset.forName("MS932"));
                    if (!s.isEmpty() && s.charAt(0) != '\uFFFD') {
                        sb.append(s);
                    } else {
                        int j1 = (b1 < 0xe0 ? b1 - 0x81 : b1 - 0xc1) * 2 + 0x21;
                        int s2 = b2;
                        if (s2 >= 0x9e) {
                            j1++;
                            s2 -= 0x9e;
                        } else {
                            s2 -= 0x40;
                            if (s2 >= 0x3f) s2--;
                        }
                        int j2 = s2 + 0x21;
                        int jis = (j1 << 8) | j2;
                        sb.append((char)(0xE000 + jis));
                    }
                    i += 2;
                    continue;
                }
            }

            sb.append((char)b1);
            i++;
        }
        return sb.toString();
    }
}
