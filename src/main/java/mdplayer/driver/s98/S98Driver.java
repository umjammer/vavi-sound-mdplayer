package mdplayer.driver.s98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import mdplayer.Common.EnmModel;
import mdplayer.chips.Ay8910Chip;
import mdplayer.chips.Sn76489Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Ym3526Chip;
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.driver.BaseDriver;
import mdplayer.lib.s98.S98;
import mdplayer.lib.s98.S98.S98DevInfo;
import mdplayer.lib.s98.S98.S98Info;
import mdplayer.driver.BasePlugin;
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;
import vavi.util.ByteUtil;

import static java.lang.System.getLogger;
import static mdplayer.Common.charset;


/**
 * S98
 *
 * @author kumatan
 */
public class S98Driver extends BaseDriver {

    private static final Logger logger = getLogger(S98Driver.class.getName());

    private final S98 s98;

    public S98Driver(BasePlugin<? extends BaseDriver> plugin) {
        super(plugin);

        this.s98 = new S98();
        s98.isRealModel = model == EnmModel.RealModel;
        s98.musicStep = setting.getOutputDevice().getSampleRate() / 60.0;
        s98.sampleRate = setting.getOutputDevice().getSampleRate();
        s98.dataBlock = b -> isDataBlock = b;
        s98.stop = () -> stopped = true;
        s98.loop = () -> curLoop++;
        s98.writeYM2203 = (chipId, adr, data) -> plugin.chipRegister.chip(Ym2203Chip.class).write(chipId, adr, data, model);
        s98.writeYM2612 = (chipId, port, adr, data) -> plugin.chipRegister.chip(Ym2612Chip.class).write(chipId, port, adr, data, model, 0);
        s98.writeYM2608 = (chipId, port, adr, data) -> plugin.chipRegister.chip(Ym2608Chip.class).write(chipId, port, adr, data, model);
        s98.writeYM2151 = (chipId, port, adr, data) -> plugin.chipRegister.chip(Ym2151Chip.class).write(chipId, port, adr, data, model, plugin.chipRegister.chip(Ym2151Chip.class).corrections[chipId], 0);
        s98.writeYM2413 = (chipId, adr, data) -> plugin.chipRegister.chip(Ym2413Chip.class).write(chipId, adr, data, model);
        s98.writeYM3526 = (chipId, adr, data) -> plugin.chipRegister.chip(Ym3526Chip.class).write(chipId, adr, data, model);
        s98.writeYM3812 = (chipId, adr, data) -> plugin.chipRegister.chip(Ym3812Chip.class).write(chipId, adr, data, model);
        s98.writeAY8910 = (chipId, adr, data) -> plugin.chipRegister.chip(Ay8910Chip.class).write(chipId, adr, data, model);
        s98.writeSN76489 = (chipId, data) -> plugin.chipRegister.chip(Sn76489Chip.class).write(chipId, data, model);
        s98.writeYMF262 = (chipId, port, adr, data) -> plugin.chipRegister.chip(YmF262Chip.class).write(chipId, port, adr, data, model);
    }

    public S98Driver() {
        this(null); // gross
    }

    public int getSSGVolumeFromTAG() {
        return s98.SSGVolumeFromTAG;
    }

    public List<S98DevInfo> getDeviceInfos() {
        return s98.s98Info.deviceInfos;
    }

    @Override
    public MetaData retrieveMetaData(byte[] buf, Object... args) {
        if (buf == null) return null;

        MetaData md = new MetaData();
        s98.s98Info = new S98Info();
        s98.chips = new ArrayList<>();

        try {
            if (ByteUtil.readLe24(buf, 0) != S98.FCC_S98) return null;
            int format = buf[3] - '0';
            int tagAdr = ByteUtil.readLeInt(buf, 0x10);
            if (format < 2) {
                List<Byte> strLst = new ArrayList<>();
                String str;
                while (buf[tagAdr] != 0x0a && buf[tagAdr] != 0x00) {
                    strLst.add(buf[tagAdr++]);
                }
                str = new String(ByteUtil.toByteArray(strLst), charset);
                md.set(Tag.Title, str);
                md.set(Tag.TitleJ, str);
            } else if (format == 3) {
                if (tagAdr != 0) {
                    if (buf[tagAdr++] != 0x5b) return null;
                    if (buf[tagAdr++] != 0x53) return null;
                    if (buf[tagAdr++] != 0x39) return null;
                    if (buf[tagAdr++] != 0x38) return null;
                    if (buf[tagAdr++] != 0x5d) return null;
                    boolean isUTF8 = false;
                    if (ByteUtil.readLe24(buf, tagAdr) == S98.FCC_BOM) {
                        isUTF8 = true;
                        tagAdr += 3;
                    }

                    while (buf.length > tagAdr && buf[tagAdr] != 0x00) {
                        List<Byte> strLst = new ArrayList<>();
                        String str;
                        while (buf[tagAdr] != 0x0a && buf[tagAdr] != 0x00) {
                            strLst.add(buf[tagAdr++]);
                        }
                        if (isUTF8) {
                            str = new String(ByteUtil.toByteArray(strLst), StandardCharsets.UTF_8);
                        } else {
                            str = new String(ByteUtil.toByteArray(strLst), charset);
                        }
                        tagAdr++;

                        if (str.toLowerCase().contains("artist=")) {
                            try {
                                md.set(Tag.Composer, str.substring(str.indexOf("=") + 1));
                                md.set(Tag.ComposerJ, str.substring(str.indexOf("=") + 1));
                            } catch (Exception e) {
                                logger.log(Level.ERROR, e.getMessage(), e);

                            }
                        }
                        if (str.toLowerCase().contains("s98by=")) {
                            try {
                                md.set(Tag.Maker, str.substring(str.indexOf("=") + 1));
                            } catch (Exception e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }
                        if (str.toLowerCase().contains("game=")) {
                            try {
                                md.set(Tag.GameTitle, str.substring(str.indexOf("=") + 1));
                                md.set(Tag.GameTitleJ, str.substring(str.indexOf("=") + 1));
                            } catch (Exception e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }
                        s98.SSGVolumeFromTAG = -1;
                        if (str.toLowerCase().contains("system=")) {
                            try {
                                md.set(Tag.GameSystem, str.substring(str.indexOf("=") + 1));
                                md.set(Tag.GameSystemJ, str.substring(str.indexOf("=") + 1));

                                if (md.getFirst(Tag.GameSystem).indexOf("8801") > 0) s98.SSGVolumeFromTAG = 63;
                                else if (md.getFirst(Tag.GameSystem).indexOf("9801") > 0) s98.SSGVolumeFromTAG = 31;
                            } catch (Exception e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }
                        if (str.toLowerCase().contains("title=")) {
                            try {
                                md.set(Tag.Title, str.substring(str.indexOf("=") + 1));
                                md.set(Tag.TitleJ, str.substring(str.indexOf("=") + 1));
                            } catch (Exception e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }
                        if (str.toLowerCase().contains("year=")) {
                            try {
                                md.set(Tag.Converter, str.substring(str.indexOf("=") + 1));
                            } catch (Exception e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }
                    }
                }
            }

            this.dataBuf = buf;
            s98.getInformationHeader(dataBuf);
            if (!s98.chips.isEmpty()) {
                md.set(Tag.Chip,  String.join(",", s98.chips));
            }

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }

        return md;
    }

    @Override
    public void init(EnmModel model, int latency, int waitTime, Object... args) {
        this.model = model;
        this.latency = latency;
        this.waitTime = waitTime;

        counter = 0;
        totalCounter = 0;
        loopCounter = 0;
        curLoop = 0;
        stopped = false;
        frameCounter = -latency - waitTime;
        speed = 1;
        speedCounter = 0;

        metaData = retrieveMetaData(dataBuf);
        //if (Gd3 == null) return false;

        if (!s98.getInformationHeader(dataBuf)) throw new IllegalArgumentException("not valid header");

        if (model == EnmModel.RealModel) {
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 0, 1);
            plugin.chipRegister.chip(Ym2612Chip.class).setSyncWait((byte) 1, 1);
        }
    }

    @Override
    public void processOneFrame() {
        try {
            speedCounter += speed;
            while (speedCounter >= 1.0 && !stopped) {
                speedCounter -= 1.0;
                if (frameCounter > -1) {
                    counter++;
                    frameCounter++;

                    s98.oneFrameMain(dataBuf);
                } else {
                    frameCounter++;
                }
            }
            //stopped = !isPlaying();
        } catch (Exception ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
        }
    }
}
