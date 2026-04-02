package mdplayer.plugin;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.MPcmChip;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.OkiM6258Chip;
import mdplayer.chips.Pcm8Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.zms.ZmsDriver;
import mdsound.MDSound;
import mdsound.instrument.X68kYm2151Inst;
import mdsound.x68sound.SoundIocs;

import static java.lang.System.getLogger;
import static mdsound.MDSound.Chip.MAIN_TAG;


/**
 * ZMusic (X68000) Plugin.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-07-12 nsano initial version <br>
 */
public class ZMSPlugin extends BasePlugin<ZmsDriver> {

    private static final Logger logger = getLogger(ZMSPlugin.class.getName());

    @Override
    public void prepare() {
        driverVirtual = new ZmsDriver();
        driverVirtual.setPlayingFileName(playingFileName);
        driverVirtual.setPlayingArcFileName(playingArcFileName);

        driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            driverReal = new ZmsDriver();
        }

        super.prepare();
        initChips();
    }

    private String[] supportFile = null;
    private String useCompiler = null;

    @Override
    protected void initChips() {
        startTrdVgmReal();

        hiyorimiNecessary = setting.getHiyorimiMode();

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Ym2151Chip.class).instrument(0);
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        chip.clock = 4000000;
        chip.samplingRate = chip.clock / 64;
        chip.option = null;
        put(Ym2151Chip.class, chip);

        chipLED.put("PriOPM", 1);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(MPcmChip.class).instrument(0);
        chip.samplingRate = setting.getOutputDevice().getSampleRate();
        chip.clock = 15600;
        chip.volume = 0;
        chip.option = null;
        put(MPcmChip.class, chip);

        //audio.chipLED.put("PriMPCM", 1);

        chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = chipRegister.chip(Pcm8Chip.class).instrument(0);
        chip.volume = 0;
        chip.clock = 4_000_000;
        if (chip.instrument instanceof X68kYm2151Inst opmPCM) {
            opmPCM.soundIocs[0] = new SoundIocs(opmPCM.chips[0]);
            chip.samplingRate = 4_000_000 / 64;
            chip.option = new Object[] { 0, 1, 0 };
        } else {
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.option = new Object[] {setting.getZMusic().pcm8ppsOption};
        }
        put(Pcm8Chip.class, chip);

        mds.init(setting.getOutputDevice().getSampleRate(), BUFFER_SIZE, flatten());

        chipRegister.plugin(MidiPlugin.class).releaseAll();
        chipRegister.plugin(MidiPlugin.class).make(setting, midiMode);
//        chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(midiMode)); // , midiOuts, midiOutsType);

        if (contains(Ym2151Chip.class, 0))
            chipRegister.chip(Ym2151Chip.class).writeClock(0, 4000000, EnmModel.RealModel);

        chipRegister.chip(Ym2151Chip.class).hosei[0] = 4000000;
        chipRegister.chip(Ym2151Chip.class).hosei[1] = 4000000;

        chipLED.put("PriPCM8", 0);
        chipLED.put("PriMPCMX68k", 0);

        // Compiler usage priority
        int compilePriority = setting.getZMusic().compilePriority;
        if (useCompiler != null) {
            if (useCompiler.equals("zmusic v2")) compilePriority = 3; // 3: v2 only
            else if (useCompiler.equals("zmusic v3")) compilePriority = 2; // 2: v3 only
        }
        // Loading/compiling support files
        List<Tuple<byte[], String>> supportFileBinary = new ArrayList<>();
        if (supportFile != null) {
            for (String sf : supportFile) {
                byte[] buf;
                if (isExt(sf, ".ZMS")) {
                    buf = File.readAllBytes(sf);
                    switch (compilePriority) {
                        case 0:
                            // Version 3 is preferred
                            if (driverVirtual.compile(buf, sf)) buf = driverVirtual.getCompiledData();
                            else if (driverVirtual.compileV2(buf, sf)) buf = driverVirtual.getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                        case 1:
                            // Version 2 is preferred
                            if (driverVirtual.compileV2(buf, sf)) buf = driverVirtual.getCompiledData();
                            else if (driverVirtual.compile(vgmBuf, sf))
                                buf = driverVirtual.getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                        case 2:
                            // Version 3 only
                            if (driverVirtual.compile(buf, sf)) buf = driverVirtual.getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                        case 3:
                            // Version 2 only
                            if (driverVirtual.compileV2(buf, sf)) buf = driverVirtual.getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                    }
                    supportFileBinary.add(new Tuple<>(buf, Path.getFileName(sf)));
                    continue;
                }
                buf = File.readAllBytes(sf);
                supportFileBinary.add(new Tuple<>(buf, Path.getFileName(sf)));
            }
        }

        driverVirtual.setSupportFileBinaryAndName(supportFileBinary);
        if (driverReal != null) driverReal.setSupportFileBinaryAndName(supportFileBinary);
//        if (driverPianoRoll != null) (driverPianoRoll).supportFileBinaryAndName = supportFileBinary;

        // In the case of ZMS, compilation is performed in advance
        if (isExt(playingFileName, ".ZMS")) {
            switch (compilePriority) {
                case 0:
                    // Version 3 is preferred
                    if (driverVirtual.compile(vgmBuf, playingFileName)) {
                        setVgmBufV3();
                        chipLED.put("PriMPCMX68k", 1);
                    } else if (driverVirtual.compileV2(vgmBuf, playingFileName)) {
                        setVgmBufV2();
                        chipLED.put("PriPCM8", 1);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
                case 1:
                    // Version 2 is preferred
                    if (driverVirtual.compileV2(vgmBuf, playingFileName)) {
                        setVgmBufV2();
                        chipLED.put("PriPCM8", 1);
                    } else if (driverVirtual.compile(vgmBuf, playingFileName)) {
                        setVgmBufV3();
                        chipLED.put("PriMPCMX68k", 1);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
                case 2:
                    // Version 3 only
                    if (driverVirtual.compile(vgmBuf, playingFileName)) {
                        setVgmBufV3();
                        chipLED.put("PriMPCMX68k", 1);
                        //logger.log("c:\\temp\\ge.zmd", vgmBuf);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
                case 3:
                    // Version 2 only
                    if (driverVirtual.compileV2(vgmBuf, playingFileName)) {
                        setVgmBufV2();
                        chipLED.put("PriPCM8", 1);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
            }
        } else {
            driverVirtual.getGD3Info(vgmBuf, 0);
        }

        if (driverVirtual.getVersion() != 2) {
            // Check the sound source composition used from ZMD
            boolean useFM = vgmBuf[0x48] != 0;
            boolean useMPCM = vgmBuf[0x49] != 0;
            boolean useMIDI1 = vgmBuf[0x4a] != 0;
            boolean useMIDI2 = vgmBuf[0x4b] != 0;
            boolean useMIDI3 = vgmBuf[0x4c] != 0;
            boolean useMIDI4 = vgmBuf[0x4d] != 0;
            chipLED.put("PriOPM", useFM ? 1 : 0);
            chipLED.put("PriMID", useMIDI1 ? 1 : 0);
            chipLED.put("SecMID", useMIDI2 ? 1 : 0);
            chipLED.put("TrdMID", useMIDI3 ? 1 : 0);
            chipLED.put("ForMID", useMIDI4 ? 1 : 0);
            chipLED.put("PriMPCMX68k", useMPCM ? 1 : 0);
        } else {
            chipLED.put("PriPCM8", 1);
        }

        driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                new Class[] {Ym2151Chip.class, OkiM6258Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        if (driverReal != null) {
            driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    new Class[] {Ym2151Chip.class, OkiM6258Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000);
        }
    }

    private void setVgmBufV3() {
        vgmBuf = driverVirtual.getCompiledData();
        if (driverReal != null) driverReal.setCompiledData(vgmBuf);
//        if (driverPianoRoll != null) (driverPianoRoll).compiledData = vgmBuf;
    }

    private void setVgmBufV2() {
        vgmBuf = driverVirtual.getCompiledData();
        driverVirtual.setVersion(2);
        if (driverReal != null) {
            driverReal.setCompiledData(vgmBuf);
            driverReal.setVersion(2);
        }
//        if (driverPianoRoll != null) {
//            driverPianoRoll.compiledData = vgmBuf;
//            driverPianoRoll.setVersion(2);
//        }
    }

    static boolean isExt(String filename, String ext) {
        return Path.getExtension(filename).toUpperCase().equals(ext);
    }
}
