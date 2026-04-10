package mdplayer.driver.gbs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.DmgChip;
import mdplayer.driver.BaseDriver;
import mdplayer.plugin.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;


public class Gbs extends BaseDriver {

    private static final Logger logger = System.getLogger(Gbs.class.getName());

    public byte song;
    public byte songs;
    private IO io;
    private Memory memory;
    private Cpu cpu;
    private int GBClock = 4194304;
    private double GBVSync = 60.0;
    private double cycles = 0.0;
    private double vcycles = 0.0;
    private GbsInfo info;
    private int breakSp; // ushort in C# -> int in Java
    private boolean initFlg = false;

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        GbsInfo gbsInfo = getGbsInfo(buf);

        songs = gbsInfo.nums;
        MetaData metaData = new MetaData();
        metaData.set(Tag.GameTitle, gbsInfo.title);
        metaData.set(Tag.GameTitleJ, gbsInfo.title);
        metaData.set(Tag.Composer, gbsInfo.author);
        metaData.set(Tag.ComposerJ, gbsInfo.author);
        metaData.set(Tag.Title, gbsInfo.title);
        metaData.set(Tag.TitleJ, gbsInfo.title);
        metaData.set(Tag.GameSystem, gbsInfo.copyright);
        metaData.set(Tag.GameSystemJ, gbsInfo.copyright);

        this.metaData = metaData;

        return metaData;
    }

    @Override
    public void init(byte[] vgmBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model, int latency, int waitTime, Object... args) {
        getMetaData(vgmBuf, 0);
        info = getGbsInfo(vgmBuf);
        this.plugin = plugin;
        this.model = model;

        //logger.log(Level.TRACE, "Load " + fn);
        //logger.log(Level.TRACE, "title     : " + info.title);
        //logger.log(Level.TRACE, "author    : " + info.author);
        //logger.log(Level.TRACE, "copyright : " + info.copyright);
        initFlg = false;
    }

    @Override
    public void processOneFrame() {
        if (!initFlg) {
            initFlg = true;
            io = new IO(
                    (a, v) -> plugin.chipRegister.chip(DmgChip.class).write(0, a, v, model),
                    a -> plugin.chipRegister.chip(DmgChip.class).read(0, a));
            memory = new Memory(info.mem, io);
            cpu = new Cpu(GBClock, memory);
            cpu.vgmFrameCounter = frameCounter;
            cpu.Init();

            cpu.reg.pc = info.initAddress & 0xFFFF;
            cpu.reg.sp = info.sp & 0xFFFF;
            breakSp = (info.sp + 2) & 0xFFFF;
            cpu.reg.a = song;
            try {
                while (!cpu.isHalt && !cpu.isStop) {
                    cpu.ExecuteOneStep();
                    if (cpu.reg.sp == breakSp) break;
                }
            } catch (UnsupportedOperationException e) {
                logger.log(Level.WARNING, "Unimplemented instruction arrived: " + e.getMessage());
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            cpu.reg.pc = info.playAddress & 0xFFFF;
            cpu.reg.sp = info.sp & 0xFFFF;
            cycles = 0.0;
        }

        double oneVClock = GBVSync / (double) setting.getOutputDevice().getSampleRate();
        vcycles += oneVClock;
        if (vcycles >= 1.0) {
            vcycles -= 1.0;
            if (cpu.reg.sp == breakSp) {
                cpu.reg.pc = info.playAddress & 0xFFFF;
                cpu.reg.sp = info.sp & 0xFFFF;
                cycles = 0;
                //cpu.reg.a = (byte) (info.firstSong - 1);
            }
        }

        //logger.log(Level.TRACE, "exec Play.");
        //logger.log(Level.TRACE, " pc:%04X".formatted(cpu.reg.pc));
        //logger.log(Level.TRACE, " sp:%04X".formatted(cpu.reg.sp));
        //logger.log(Level.TRACE, " firstSong: " +  cpu.reg.a + 1);

        //step = 0;
        try {
            if (cpu.reg.sp != breakSp) {
                cpu.vgmFrameCounter = frameCounter;
                double oneClock = cpu.clock / (double) setting.getOutputDevice().getSampleRate();
                while (cycles < oneClock && cpu.reg.sp != breakSp) {
                    int cycle = cpu.ExecuteOneStep();
                    cycles += cycle;
                    //step++;
                }
                cycles -= oneClock;
            }
        } catch (UnsupportedOperationException e) {
            logger.log(Level.WARNING, "Unimplemented instruction arrived: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        //logger.log(Level.TRACE, "Total cycle : " + cycles);
        //logger.log(Level.TRACE, "Total step  : " + step);

        //logger.log(Level.TRACE, "Play process count : " + i + 1);
    }

    GbsInfo getGbsInfo(byte[] b) {
        // IdentifierCheck
        if (b[0] != 'G' || b[1] != 'B' || b[2] != 'S') throw new IllegalArgumentException("Unknown format");

        GbsInfo info = new GbsInfo();
        info.version = b[3];
        info.nums = b[4];
        info.firstSong = b[5];
        info.loadAddress = (b[6] & 0xFF) + ((b[7] & 0xFF) << 8);
        info.initAddress = (b[8] & 0xFF) + ((b[9] & 0xFF) << 8);
        info.playAddress = (b[10] & 0xFF) + ((b[11] & 0xFF) << 8);
        info.sp = (b[12] & 0xFF) + ((b[13] & 0xFF) << 8);
        info.timerModulo = b[14];
        info.timerControl = b[15];

        info.title = new String(b, 0x10, 32, Common.charset).replace("\0", "");
        info.author = new String(b, 0x30, 32, Common.charset).replace("\0", "");
        info.copyright = new String(b, 0x50, 32, Common.charset).replace("\0", "");

        info.mem = new byte[2][];
        info.mem[0] = new byte[0x4000];
        info.mem[1] = new byte[0x4000];

        int ptr = info.loadAddress % 0x4000;
        int cptr = 0x70;
        int bank = info.loadAddress / 0x4000;
        while ((bank < 2 && ptr < 0x4000) && cptr < b.length) {
            info.mem[bank][ptr] = b[cptr];
            ptr++;
            if (ptr == 0x4000) {
                ptr = 0;
                bank++;
            }
            cptr++;
        }

        return info;
    }
}
