package mdplayer.driver.gbs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

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
    private static final int GBClock = 4194304;
    private static final double GBVSync = 60.0;
    private double cycles = 0.0;
    private double vCycles = 0.0;
    private GbsInfo info;
    private int breakSp;
    private boolean initFlg = false;

    @Override
    public MetaData getMetaData(byte[] buf, Object... args) {
        GbsInfo gbsInfo = GbsInfo.factory(buf);

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

    /**
     * @param args 0: songNo
     */
    @Override
    public void init(byte[] dataBuf, BasePlugin<? extends BaseDriver> plugin, EnmModel model, int latency, int waitTime, Object... args) {
        getMetaData(dataBuf, 0);
        info = GbsInfo.factory(dataBuf);
        this.plugin = plugin;
        this.model = model;

        int s = (int) args[0] - 1;
        song = (byte) (0 < s || s >= songs ? s : 0);
logger.log(Level.DEBUG, "internal song no: " + song + " / " + songs);

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
            cpu.init();

            cpu.reg.pc = info.initAddress & 0xFFFF;
            cpu.reg.sp = info.sp & 0xFFFF;
            breakSp = (info.sp + 2) & 0xFFFF;
            cpu.reg.a = song;
            try {
                while (!cpu.isHalt && !cpu.isStop) {
                    cpu.executeOneStep();
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
        vCycles += oneVClock;
        if (vCycles >= 1.0) {
            vCycles -= 1.0;
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
                double oneClock = cpu.clock / (double) setting.getOutputDevice().getSampleRate();
                while (cycles < oneClock && cpu.reg.sp != breakSp) {
                    int cycle = cpu.executeOneStep();
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
}
