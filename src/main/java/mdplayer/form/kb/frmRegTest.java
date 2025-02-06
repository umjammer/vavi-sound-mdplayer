package mdplayer.form.kb;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mdplayer.Chip;
import mdplayer.DrawBuff;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.C140Chip;
import mdplayer.chips.C352Chip;
import mdplayer.chips.NesChip;
import mdplayer.chips.QSoundChip;
import mdplayer.chips.SegaPcmChip;
import mdplayer.chips.SidChip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.chips.YmF278BChip;
import mdplayer.chips.YmZ280BChip;
import mdplayer.driver.sid.Sid;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTuneInfo;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;
import mdplayer.form.sys.frmMain;
import mdplayer.properties.Resources;


public class frmRegTest extends frmChipBase {

    static class ChipData {

        public String chipName;
        public int baseIndex;
        /** GetRegisterDelegate */
        public Function<Integer, Object> register;
        public int maxRegisterSize;
        public int regWind;

        public ChipData(String chipName, int baseIndex, int maxRegisterSize, int regWindow, Function<Integer, Object> register) {
            this.chipName = chipName;
            this.baseIndex = baseIndex;
            this.register = register;
            this.maxRegisterSize = maxRegisterSize;
            regWind = regWindow;
        }
    }

    class RegisterManager {
        int select;
        public boolean needRefresh = false;
        List<ChipData> chipData = new ArrayList<>();

        public RegisterManager() {
            addChip("YMF278B", 3, 0x100, select -> { // 0
                return audio.chipRegister.chip(YmF278BChip.class).read(0)[select];
            });

            addChip("YMF262", 2, 0x100, select -> { // 3
                return audio.chipRegister.chip(YmF262Chip.class).read(0)[select];
            });

            addChip("YM2151", 1, 0x100, select -> { // 5
                return audio.chipRegister.chip(Ym2151Chip.class).read(0);
            });

            addChip("YM2610", 1, 0x200, select -> { // 6
                return audio.chipRegister.chip(Ym2610Chip.class).read(0);
            });

            addChip("YM2608", 1, 0x200, select -> { // 7
                return audio.chipRegister.chip(Ym2608Chip.class).read(0);
            });

            addChip("Ym2612Inst", 1, 0x200, select -> audio.chipRegister.chip(Ym2612Chip.class).read(0));

            addChip("C140Inst", 1, 0x200, select -> audio.chipRegister.chip(C140Chip.class).read(0));

            addChip("QSOUND", 1, 0x200, select -> audio.chipRegister.chip(QSoundChip.class).read(0));

            addChip("SEGAPCM", 1, 0x200, select -> audio.chipRegister.chip(SegaPcmChip.class).read(0));

            addChip("YMZ280B", 1, 0x100, select -> audio.chipRegister.chip(YmZ280BChip.class).read(0));

            addChip("SN76489", 1, 8, select -> audio.chipRegister.chip(Sn76489Chip.class).read(0));

            addChip("AY", 1, 16, select -> audio.chipRegister.chip(Ay8910Chip.class).read(0));

            addChip("C352Inst", 1, 0x400, select -> audio.chipRegister.chip(C352Chip.class).getChip(0));

            addChip("YM2203", 1, 0x200, select -> audio.chipRegister.chip(Ym2203Chip.class).read(0));

            addChip("YM2413", 1, 0x100, select -> audio.chipRegister.chip(Ym2413Chip.class).read(0));

            addChip("YM3812", 1, 0x100, select -> audio.chipRegister.chip(Ym3812Chip.class).read(0));

            addChip("NES", 1, 0x30, select -> audio.chipRegister.chip(NesChip.class).readApu(0));

            addChip("Sid", 3, 0x19, chipId1 -> audio.chipRegister.chip(SidChip.class).read(chipId1));
        }

        private void addChip(String ChipName, int Max, int regSize, Function<Integer, Object> p) {
            int BaseIndex = chipData.size();
            for (int i = 0; i < Max; i++) {
                chipData.add(new ChipData(ChipName, BaseIndex, regSize, Max, p));
            }
        }

        public void prev() {
            select--;
            if (select < 0) select = chipData.size() - 1;
            needRefresh = true;
        }

        public void next() {
            select++;
            if (select > chipData.size() - 1) select = 0;
            needRefresh = true;
            //if (Select < ChipList.size()-1) Select++;
        }

        public Object getData() {
            ChipData x = chipData.get(select);
            return x.register.apply(select - x.baseIndex);
        }

        public String getName() {
            ChipData x = chipData.get(select);
            return "%-10s  ".formatted(x.chipName);
        }

        public String getName2() {
            ChipData x = chipData.get(select);
            return "#%d REGISTER (%d/%d)  ".formatted(select - x.baseIndex, select + 1, chipData.size());
        }

        public int getRegisterSize() {
            return chipData.get(select).maxRegisterSize;
        }

        //public int getPageSize() {
        //    return ChipList.get(Select).regWind;
        //}

        public int getCurrentPage() {
            ChipData x = chipData.get(select);
            return select - x.baseIndex;
        }

        public int getSelect() {
            return select;
        }

        public void setSelect(int val) {
            select = val;
        }
    }

    static Preferences prefs = Preferences.userNodeForPackage(frmRegTest.class);

    private final int formWidth;
    private final int formHeight;

    //private FrameBuffer frameBuffer = new FrameBuffer();

    RegisterManager regMan = new RegisterManager();

    private final Map<Class<? extends Chip>, Integer> pageDict = new HashMap<>() {{
        put(YmF278BChip.class, 0);
        put(YmF262Chip.class, 3);
        put(Ym2151Chip.class, 5);
        put(Ym2610Chip.class, 6);
        put(Ym2608Chip.class, 7);
        put(Ym2612Chip.class, 8);
        put(C140Chip.class, 9);
        put(QSoundChip.class, 10);
        put(SegaPcmChip.class, 11);
        put(YmZ280BChip.class, 12);
        put(Sn76489Chip.class, 13);
        put(Ay8910Chip.class, 14);
        put(C352Chip.class, 15);
        put(Ym2203Chip.class, 16);
        put(Ym2413Chip.class, 17);
        put(Ym3812Chip.class, 18);
        put(NesChip.class, 19);
        put(SidChip.class, 20);
    }};

    public frmRegTest(frmMain frm, int chipId, Class<? extends Chip> enmPage, int zoom) {
        parent = frm;
        this.chipId = chipId;
        this.zoom = zoom;
        int pageSel = 0;
        pageDict.getOrDefault(enmPage, pageSel);
        formWidth = 260;
        formHeight = 280;

        initializeComponent();
        frameBuffer.Add(pbScreen, new BufferedImage(formWidth, formHeight, BufferedImage.TYPE_INT_ARGB), null, zoom);
        regMan.setSelect(pageSel);
        update();
    }

    public void changeChip(Class<? extends Chip> chip) {
        int pageSel = pageDict.get(chip);
        regMan.setSelect(pageSel);
        regMan.needRefresh = true;
        update();
    }

    @Override
    public void update() {
        if (regMan.needRefresh) {
            frameBuffer.clearScreen();
            regMan.needRefresh = false;
        }
        frameBuffer.refresh(null);
    }

    @Override
    protected boolean getShowWithoutActivation() {
        return true;
    }

    private final WindowListener windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getNewState() == WindowEvent.WINDOW_OPENED) {
                parent.setting.getLocation().getPosRegTest()[chipId] = getLocation();
            } else {
                parent.setting.getLocation().getPosRegTest()[chipId] = new Point(prefs.getInt("x", 0), prefs.getInt("y", 0));
            }
            parent.setting.getLocation().setChipSelect(regMan.getSelect());
            update();
            isClosed = true;
        }

        @Override
        public void windowOpened(WindowEvent e) {
            setLocation(new Point(x, y));
            regMan.setSelect(parent.setting.getLocation().getChipSelect());

            frameSizeW = getWidth() - getSize().width;
            frameSizeH = getHeight() - getSize().height;

            changeZoom();
        }
    };

    public void changeZoom() {
        this.setMaximumSize(new Dimension(frameSizeW + formWidth * zoom, frameSizeH + formHeight * zoom));
        //this.setMinimumSize(new Dimension(frameSizeW + FormWidth * zoom, frameSizeH + FormHeight * zoom));
        this.setPreferredSize(new Dimension(frameSizeW + formWidth * zoom, frameSizeH + formHeight * zoom));
        componentListener.componentResized(null);
    }

    private final ComponentListener componentListener = new ComponentAdapter() {
        @Override
        public void componentMoved(ComponentEvent e) {
            prefs.putInt("x", e.getComponent().getX());
            prefs.putInt("y", e.getComponent().getY());
        }
        @Override
        public void componentResized(ComponentEvent e) {
        }
    };

//    @Override
//    protected void WndProc(Message m) {
//        if (parent != null) {
//            parent.windowsMessage(m);
//        }
//
//        try {
//            super.WndProc(m);
//        } catch (Exception ex) {
//            Log.forcedWrite(ex);
//        }
//    }

    @Override
    public void screenChangeParams() {
    }

    @Override
    public void screenDrawParams() {
        //if (RegMan.needRefresh) { frameBuffer.clearScreen(); RegMan.needRefresh = false; }
        String Name = regMan.getName();
        String Name2 = regMan.getName2();
        String p = String.valueOf(regMan.getCurrentPage());
        //var MaxPage = RegMan.getPageSize();
        //var regSize = RegMan.getRegisterSize(); // Max
        //var actualRegSize = RegMan.getRegisterSize();//reg.length >= regSize ? regSize : reg.length; //TODO: Change this
        DrawBuff.drawFont8(frameBuffer, 2, 1, 0, Name);
        DrawBuff.drawFont8(frameBuffer, 2, 9, 0, Name2);
        DrawBuff.drawFont8(frameBuffer, 210, 1, 0, "<>");

        int y = 17;

        if (regMan.getName().contains("Sid")) {
            //y += 8;
            Sid curSID = audio.chipRegister.chip(SidChip.class).SID;
            //Sid curSID = ChipRegister.Sid;
            Object a = regMan.getData();
            if (a == null) return;
            Integer[] r = (Integer[]) a;

            // Voice Registers

            // Regards of TEST bit... it seems to reset OSC to 0 until it clears up.

            int voice1f = r[1] << 8 | r[0]; // D401-D400 Voice 1 Freq
            int pwdc1 = ((r[3] & 0x0F) << 8) | r[2]; // D402-D403 Pulse Wave Duty Cycle
            int mode1 = r[4]; // MODE reg D404  NOISE PULSE SAWTOOTH TRIANGLE TEST RING3 SYNC3 GATE(KON)
            int attack1 = r[5] >> 4; // D405.4-7 Attack
            int decay1 = r[5] & 0x0F; // D405.0-3 Decay
            int sustain1 = r[6] >> 4; // D406.4-7 Sustain
            int release1 = r[6] & 0x0F; // D406.0-3 Release

            int voice2f = r[8] << 8 | r[7]; // D408-D407 Voice 2 Freq
            int pwdc2 = ((r[0xA] & 0x0F) << 8) | r[9]; // D40A-D409 Pulse Wave Duty Cycle
            int mode2 = r[0xB]; // D40B Control2 reg  NOISE PULSE SAWTOOTH TRIANGLE TEST RING1 SYNC1 GATE(KON)
            int attack2 = r[0xC] >> 4; // D40C.4-7 Attack
            int decay2 = r[0xC] & 0x0F; // D40C.0-3 Decay
            int sustain2 = r[0xD] >> 4; // D40D.4-7 Sustain
            int release2 = r[0xD] & 0x0F; // D40D.0-3 Release

            int voice3f = r[0xF] << 8 | r[0xE]; // D40F-D40E Voice 2 Freq
            int pwdc3 = ((r[0x11] & 0x0F) << 8) | r[0x10]; // D411-D410 Pulse Wave Duty Cycle
            int mode3 = r[0x12]; // D412 Control2 reg  NOISE PULSE SAWTOOTH TRIANGLE TEST RING1 SYNC1 GATE(KON)
            int attack3 = r[0x13] >> 4; // D413.4-7 Attack
            int decay3 = r[0x13] & 0x0F; // D413.0-3 Decay
            int sustain3 = r[0x14] >> 4; // D414.4-7 Sustain
            int release3 = r[0x14] & 0x0F; // D414.0-3 Release

            // Filter Registers
            int filtercutoff = r[0x16] << 3 | r[0x15]; // D416-D415 Filter Cutoff
            int filterreso = r[0x17] >> 4; // D417.4-7 Filter Resonanse
            int filterroute = r[0x17] & 0x0F; // D417.0-3 Filter Route (Enable bit)
            int filtermode = r[0x18] >> 4; // D418.4-7 Filter Type (Mode... Hi/Band/Lo)
            int mainvolume = r[0x18] & 0x0F; // D418.0-3 Main Volume (Sid Volume instanceof 4 bits)

            // Paddle reg
            /*
            int paddleX = r[0x19]; // D419 RO, Paddle reg x
            int paddleY = r[0x1A]; // D41A RO, Paddle reg y
            int oscv3 = r[0x1B]; // D41B RO, Oscillator3 Value
            int envv3 = r[0x1C]; // D41C RO, Oscillator3 Envelope
            */


            DrawBuff.drawFont4(frameBuffer, 2, y, 0, "VOICE1 FREQ %4x".formatted(voice1f));
            DrawBuff.drawFont4(frameBuffer, 2, y + 8, 0, "VOICE1 PWDC %4x".formatted(pwdc1));

            DrawBuff.drawFont4(frameBuffer, 2, y + 16, 0, "VOICE1 MODE ");
            DrawBuff.drawFont4(frameBuffer, 2, y + 24, 0, "" +
                    ((mode1 & 0b10000000) == 0x80 ? "NOISE" : "-----") +
                    ((mode1 & 0b01000000) == 0x40 ? "PULSE" : "-----") +
                    ((mode1 & 0b00100000) == 0x20 ? "SAWTOOTH" : "--------") +
                    ((mode1 & 0b00010000) == 0x10 ? "TRIANGLE" : "--------") +
                    ((mode1 & 0b00001000) == 0x08 ? "TEST" : "----") +
                    ((mode1 & 0b00000100) == 0x04 ? "RING 3" : "------") +
                    ((mode1 & 0b00000010) == 0x02 ? "SYNC 3" : "------") +
                    ((mode1 & 0b00000001) == 0x01 ? "GATE" : "----")); // Parse this
            DrawBuff.drawFont4(frameBuffer, 2, y + 32, 0, "VOICE1 ADSR %1x %1x %1x %1x".formatted(attack1, decay1, sustain1, release1));

            y += 48;

            DrawBuff.drawFont4(frameBuffer, 2, y, 0, "VOICE2 FREQ %4x".formatted(voice2f));
            DrawBuff.drawFont4(frameBuffer, 2, y + 8, 0, "VOICE2 PWDC %4x".formatted(pwdc2));

            DrawBuff.drawFont4(frameBuffer, 2, y + 16, 0, "VOICE2 MODE ");
            DrawBuff.drawFont4(frameBuffer, 2, y + 24, 0, "" +
                    ((mode2 & 0b10000000) == 0x80 ? "NOISE" : "-----") +
                    ((mode2 & 0b01000000) == 0x40 ? "PULSE" : "-----") +
                    ((mode2 & 0b00100000) == 0x20 ? "SAWTOOTH" : "--------") +
                    ((mode2 & 0b00010000) == 0x10 ? "TRIANGLE" : "--------") +
                    ((mode2 & 0b00001000) == 0x08 ? "TEST" : "----") +
                    ((mode2 & 0b00000100) == 0x04 ? "RING 1" : "------") +
                    ((mode2 & 0b00000010) == 0x02 ? "SYNC 1" : "------") +
                    ((mode2 & 0b00000001) == 0x01 ? "GATE" : "----")); // Parse this
            DrawBuff.drawFont4(frameBuffer, 2, y + 32, 0, "VOICE2 ADSR %1x %1x %1x %1x".formatted(attack2, decay2, sustain2, release2));

            y += 48;

            DrawBuff.drawFont4(frameBuffer, 2, y, 0, "VOICE3 FREQ %4x".formatted(voice3f));
            DrawBuff.drawFont4(frameBuffer, 2, y + 8, 0, "VOICE3 PWDC %4x".formatted(pwdc3));

            DrawBuff.drawFont4(frameBuffer, 2, y + 16, 0, "VOICE3 MODE ");
            DrawBuff.drawFont4(frameBuffer, 2, y + 24, 0, "" +
                    ((mode3 & 0b10000000) == 0x80 ? "NOISE" : "-----") +
                    ((mode3 & 0b01000000) == 0x40 ? "PULSE" : "-----") +
                    ((mode3 & 0b00100000) == 0x20 ? "SAWTOOTH" : "--------") +
                    ((mode3 & 0b00010000) == 0x10 ? "TRIANGLE" : "--------") +
                    ((mode3 & 0b00001000) == 0x08 ? "TEST" : "----") +
                    ((mode3 & 0b00000100) == 0x04 ? "RING 2" : "------") +
                    ((mode3 & 0b00000010) == 0x02 ? "SYNC 2" : "------") +
                    ((mode3 & 0b00000001) == 0x01 ? "GATE" : "----")); // Parse this
            DrawBuff.drawFont4(frameBuffer, 2, y + 32, 0, "VOICE3 ADSR %1x %1x %1x %1x".formatted(attack3, decay3, sustain3, release3));

            y += 48;

            DrawBuff.drawFont4(frameBuffer, 2, y, 0, "FILTER CUTOFF FREQ %4x".formatted(filtercutoff));
            DrawBuff.drawFont4(frameBuffer, 2, y + 8, 0, "FILTER RESONANCE %2x".formatted(filterreso));

            DrawBuff.drawFont4(frameBuffer, 2, y + 16, 0, "FILTER ROUTE");
            DrawBuff.drawFont4(frameBuffer, 2, y + 24, 0, "" +
                    ((filterroute & 0b00001000) == 0x08 ? "EXT.IN" : "------") +
                    ((filterroute & 0b00000100) == 0x04 ? "VOICE3" : "------") +
                    ((filterroute & 0b00000010) == 0x02 ? "VOICE2" : "------") +
                    ((filterroute & 0b00000001) == 0x01 ? "VOICE1" : "------"));

            DrawBuff.drawFont4(frameBuffer, 2, y + 32, 0, "FILTER MODE");
            DrawBuff.drawFont4(frameBuffer, 2, y + 40, 0, "" +
                    ((filtermode & 0b00001000) == 0x08 ? "MUTE V3" : "-------") +
                    ((filtermode & 0b00000100) == 0x04 ? "HIGHPASS" : "--------") +
                    ((filtermode & 0b00000010) == 0x02 ? "BANDPASS" : "--------") +
                    ((filtermode & 0b00000001) == 0x01 ? "LOWPASS" : "-------"));
            DrawBuff.drawFont4(frameBuffer, 2, y + 48, 0, "MAIN volume %2x".formatted(mainvolume));

            y += 56;

            SidTuneInfo sti = curSID.tuneInfo;
            SidConfig cfg = curSID.cfg;
            playSidFp curEngine = curSID.GetCurrentEngineContext();
            SidInfo si = curEngine.info();

            DrawBuff.drawFont4(frameBuffer, 2, y, 0, "LOAD ADDR %04xh".formatted(sti.getLoadAddr()));
            DrawBuff.drawFont4(frameBuffer, 2, y + 8, 0, "INIT ADDR %04xh".formatted(sti.getInitAddress()));
            DrawBuff.drawFont4(frameBuffer, 2, y + 16, 0, "PLAY ADDR %04xh".formatted(sti.getPlayAddress()));
            DrawBuff.drawFont4(frameBuffer, 2, y + 24, 0, "%s %s; CUR:%s SPD:%s".formatted(sti.sidModel(Integer.parseInt(p)), sti.getClockSpeed(), cfg.defaultSidModel, si.getSpeedString()));

            y += 32;
            //return;
        }

//        var y = 8; // Line: It changes every 0x10...
//        for (var idx = 0; idx < regSize; idx += 0x10) {
//            DrawBuff.drawFont4(frameBuffer, 2, y, 0, "{idx:X3}:");
//            var remainingRegNum = regSize >= 0x10 ? 0x10 : regSize;
//            for (var i = 0; i < remainingRegNum; i++) {
//                DrawBuff.drawFont4(frameBuffer, 34 + (i * 12), y, 0, "{reg[idx+i]:X2}");
//            }
//            y += 8;
//        }

        Object reg = regMan.getData();

        if (reg instanceof byte[] r) {
            for (int i = 0; i < r.length; i++) {
                if (i % 16 == 0) {
                    y += 8;
                    DrawBuff.drawFont4(frameBuffer, 2, y - 8, 0, "%3x:".formatted(i));
                }
                byte v = r[i];
                DrawBuff.drawFont4(frameBuffer, 34 + ((i % 16) * 12), y - 8, 0, "%2x:".formatted(v));
            }
        } else if (reg instanceof short[] r) {
            int ms = regMan.getRegisterSize();
            for (int i = 0; i < Math.min(r.length, ms); i++) {
                if (i % 16 == 0) {
                    y += 8;
                    DrawBuff.drawFont4(frameBuffer, 2, y - 8, 0, "%3x:".formatted(i));
                }
                byte v = (byte) r[i];
                DrawBuff.drawFont4(frameBuffer, 34 + ((i % 16) * 12), y - 8, 0, "%2x:".formatted(v));
            }
        } else if (reg instanceof int[] r) {
            for (int i = 0; i < r.length; i++) {
                if (i % 16 == 0) {
                    y += 8;
                    DrawBuff.drawFont4(frameBuffer, 2, y - 8, 0, "%3x:".formatted(i));
                }
                int v = r[i];
                DrawBuff.drawFont4(frameBuffer, 30 + ((i % 8) * 18), y - 8, 0, "%4x:".formatted(v));
            }
        } else if (reg instanceof int[][] r) {
            for (int j = 0; j < r.length; j++) {
                for (int i = 0; i < r[j].length; i++) {
                    if (i % 16 == 0) {
                        y += 8;
                        int n = i + j * r[j].length;
                        DrawBuff.drawFont4(frameBuffer, 2, y - 8, 0, "%3x:".formatted(n));
                    }
                    int m = r[j][i];
                    byte v = (byte) r[j][i];
                    int c = 0;
                    if (m < 0) {
                        // Unknown value
                        v = 0;
                        c = 1;
                    }
                    DrawBuff.drawFont4(frameBuffer, 34 + ((i % 16) * 12), y - 8, c, "%2x:".formatted(v));
                }
            }
        }
    }

    private final MouseListener pbScreen_MouseClick = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
//            int px = ev.getX() / zoom;
//            int py = ev.getY() / zoom;
//            //logger.log(Level.TRACE, "%d %d".formatted(px, py));
//            if (py > 8) return;
//            if (px < 210) return;
//            int xc = (px - 210) / 4;
//            if (xc == 0) {
//                RegMan.Prev();
//            }
//
//            if (xc == 2) {
//                RegMan.nextInt();
//            }
            if (ev.getButton() == MouseEvent.BUTTON1) regMan.prev();
            else if (ev.getButton() == MouseEvent.BUTTON2) regMan.next();
        }
    };

    private void initializeComponent() {
        this.pbScreen = new JPanel();
        this.panel1 = new JPanel();

        //
        // pbScreen
        //
//        this.pbScreen.setLocation(new Point(0, 0));
//        this.pbScreen.setName("pbScreen");
//        this.pbScreen.setPreferredSize(new Dimension(181, 73));
        // this.pbScreen.TabIndex = 0
        // this.pbScreen.TabStop = false;
        this.pbScreen.addMouseListener(this.pbScreen_MouseClick);
        //
        // panel1
        //
//        this.panel1.AutoScroll = true;
        this.panel1.add(this.pbScreen);
//        this.panel1.Dock = JDockStyle.Fill;
//        this.panel1.setLocation(new Point(0, 0));
//        this.panel1.setName("panel1");
//        this.panel1.setPreferredSize(new Dimension(320, 151));
        // this.panel1.TabIndex = 1
        //
        // frmRegTest
        //
//            this.AutoScaleDimensions = new DimensionF(6F, 12F);
//            this.AutoScaleMode = JAutoScaleMode.Font;
        //this.setBackground(Color.ControlDarkDark);
        this.setPreferredSize(new Dimension(320, 151));
        this.getContentPane().add(new JScrollPane(this.panel1));
//        this.FormBorderStyle = JFormBorderStyle.Sizable;
        this.setIconImage((Image) Resources.getResourceManager().getObject("$this.Icon"));
        this.setMaximumSize(new Dimension(1024, 1024));
        this.setName("frmRegTest");
        this.setTitle("RegDump");
        this.addWindowListener(this.windowListener);
        this.addComponentListener(this.componentListener);
        //((System.ComponentModel.ISupportInitialize)(this.pbScreen)).EndInit();
        // this.panel1.ResumeLayout(false);
//            this.ResumeLayout(false);
    }

    public JPanel pbScreen;
    private JPanel panel1;
}
