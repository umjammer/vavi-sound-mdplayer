package mdplayer.plugin;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.Audio;
import mdplayer.Common;
import mdplayer.Common.EnmModel;
import mdplayer.chips.MidiPlugin;
import mdplayer.chips.OkiM6258Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.driver.zms.Zms;
import mdplayer.format.FileFormat;
import mdsound.Instrument;
import mdsound.MDSound;
import mdsound.instrument.MPcmPPInst;
import mdsound.instrument.Pcm8PPInst;
import mdsound.instrument.X68kMPcmInst;
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
public class ZMSPlugin extends BasePlugin {

    private static final Logger logger = getLogger(ZMSPlugin.class.getName());

    @Override
    public boolean play(String playingFileName, FileFormat format) {
        audio.driverVirtual = new Zms();
        ((Zms) audio.driverVirtual).setPlayingFileName(playingFileName);
        ((Zms) audio.driverVirtual).setPlayingArcFileName(playingArcFileName);

        audio.driverReal = null;
        if (setting.getOutputDevice().getDeviceType() != Common.DEV_Null) {
//            audio.driverReal = new Zms();
        }
        try {
            prepare();
            boolean r = _play();
            if (!r) {
                logger.log(Level.WARNING, "cannot start: " + this);
                return false;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "cannot start: " + this, e);
            return false;
        }
        super.play();
        return true;
    }

    private String[] supportFile = null;
    private String useCompiler = null;

    /** */
    private boolean _play() throws Exception {
        startTrdVgmReal();

        hiyorimiNecessary = setting.getHiyorimiMode();

        MDSound.Chip chip = new MDSound.Chip();
        chip.id = 0;
        chip.instrument = audio.chipRegister.chip(Ym2151Chip.class).instrument(0);
        chip.volume = setting.getBalance().getVolume(MAIN_TAG, Ym2151Chip.class);
        chip.clock = 4000000;
        chip.samplingRate = chip.clock / 64;
        chip.option = null;
        put(Ym2151Chip.class, chip);

        audio.chipLED.put("PriOPM", 1);

        if (setting.getZMusic().mpcmType == 0) {
            X68kMPcmInst mpcm = Instrument.getInstrument(X68kMPcmInst.class);
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = mpcm;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 15600;
            chip.volume = 0;
            chip.option = null;
            //audio.chipLED.put("PriMPCM", 1);
            put(OkiM6258Chip.class, chip); // not use mds, via driver direct
            ((Zms) audio.driverVirtual).mpcm = mpcm;
            ((Zms) audio.driverVirtual).mpcmType = 0;
        } else {
            MPcmPPInst mpcmpp = Instrument.getInstrument(MPcmPPInst.class);
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = mpcmpp;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.clock = 15600;
            chip.volume = 0;
            chip.option = null;
            //audio.chipLED.put("PriMPCM", 1);
            put(OkiM6258Chip.class, chip); // not use mds, via driver direct
            ((Zms) audio.driverVirtual).mpcmpp = mpcmpp;
            ((Zms) audio.driverVirtual).mpcmType = 1;
        }

        if (setting.getZMusic().pcm8Type == 0) {
            X68kYm2151Inst opmPCM = Instrument.getInstrument(X68kYm2151Inst.class);
            opmPCM.soundIocs[0] = new SoundIocs(opmPCM.chips[0]);
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = opmPCM;
            chip.volume = 0;
            chip.clock = 4_000_000;
            chip.samplingRate = 4_000_000 / 64;
            chip.option = new Object[] { 0, 1, 0 };
            put(OkiM6258Chip.class, chip); // not use mds, via driver direct
            ((Zms) audio.driverVirtual).opmPCM = opmPCM;
            ((Zms) audio.driverVirtual).pcm8type = 0;
        } else {
            Pcm8PPInst pcm8pp = Instrument.getInstrument(Pcm8PPInst.class);
            chip = new MDSound.Chip();
            chip.id = 0;
            chip.instrument = pcm8pp;
            chip.volume = 0;
            chip.clock = 4_000_000;
            chip.samplingRate = setting.getOutputDevice().getSampleRate();
            chip.option = new Object[] {setting.getZMusic().pcm8ppsOption};
            put(OkiM6258Chip.class, chip); // not use mds, via driver direct
            ((Zms) audio.driverVirtual).pcm8pp = pcm8pp;
            ((Zms) audio.driverVirtual).pcm8type = 1;
        }

        audio.mds.init(setting.getOutputDevice().getSampleRate(), Audio.BUFFER_SIZE, flatten());

        audio.chipRegister.plugin(MidiPlugin.class).releaseAll();
        audio.chipRegister.plugin(MidiPlugin.class).make(setting, midiMode);
//        audio.chipRegister.plugin(MidiPlugin.class).set(setting.getMidiOut().getMidiOutInfos().get(midiMode)); // , midiOuts, midiOutsType);

        if (contains(Ym2151Chip.class, 0))
            audio.chipRegister.chip(Ym2151Chip.class).writeClock(0, 4000000, EnmModel.RealModel);

        audio.chipRegister.chip(Ym2151Chip.class).hosei[0] = 4000000;
        audio.chipRegister.chip(Ym2151Chip.class).hosei[1] = 4000000;

        audio.chipLED.put("PriPCM8", 0);
        audio.chipLED.put("PriMPCMX68k", 0);

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
                            if (((Zms) audio.driverVirtual).compile(buf, sf)) buf = ((Zms) audio.driverVirtual).getCompiledData();
                            else if (((Zms) audio.driverVirtual).compileV2(buf, sf)) buf = ((Zms) audio.driverVirtual).getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                        case 1:
                            // Version 2 is preferred
                            if (((Zms) audio.driverVirtual).compileV2(buf, sf)) buf = ((Zms) audio.driverVirtual).getCompiledData();
                            else if (((Zms) audio.driverVirtual).compile(vgmBuf, sf))
                                buf = ((Zms) audio.driverVirtual).getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                        case 2:
                            // Version 3 only
                            if (((Zms) audio.driverVirtual).compile(buf, sf)) buf = ((Zms) audio.driverVirtual).getCompiledData();
                            else throw new IllegalArgumentException("Compile Error.Check console log.");
                            break;
                        case 3:
                            // Version 2 only
                            if (((Zms) audio.driverVirtual).compileV2(buf, sf)) buf = ((Zms) audio.driverVirtual).getCompiledData();
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

        ((Zms) audio.driverVirtual).supportFileBinaryAndName = supportFileBinary;
        if (audio.driverReal != null) ((Zms) audio.driverReal).supportFileBinaryAndName = supportFileBinary;
//        if (audio.driverPianoRoll != null) ((Zms) audio.driverPianoRoll).supportFileBinaryAndName = supportFileBinary;

        // In the case of ZMS, compilation is performed in advance
        if (isExt(playingFileName, ".ZMS")) {
            switch (compilePriority) {
                case 0:
                    // Version 3 is preferred
                    if (((Zms) audio.driverVirtual).compile(vgmBuf, playingFileName)) {
                        setVgmBufV3();
                        audio.chipLED.put("PriMPCMX68k", 1);
                    } else if (((Zms) audio.driverVirtual).compileV2(vgmBuf, playingFileName)) {
                        setVgmBufV2();
                        audio.chipLED.put("PriPCM8", 1);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
                case 1:
                    // Version 2 is preferred
                    if (((Zms) audio.driverVirtual).compileV2(vgmBuf, playingFileName)) {
                        setVgmBufV2();
                        audio.chipLED.put("PriPCM8", 1);
                    } else if (((Zms) audio.driverVirtual).compile(vgmBuf, playingFileName)) {
                        setVgmBufV3();
                        audio.chipLED.put("PriMPCMX68k", 1);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
                case 2:
                    // Version 3 only
                    if (((Zms) audio.driverVirtual).compile(vgmBuf, playingFileName)) {
                        setVgmBufV3();
                        audio.chipLED.put("PriMPCMX68k", 1);
                        //logger.log("c:\\temp\\ge.zmd", vgmBuf);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
                case 3:
                    // Version 2 only
                    if (((Zms) audio.driverVirtual).compileV2(vgmBuf, playingFileName)) {
                        setVgmBufV2();
                        audio.chipLED.put("PriPCM8", 1);
                    } else {
                        // compile error
                        throw new IllegalArgumentException("Compile Error.Check console log.");
                    }
                    break;
            }
        } else {
            audio.driverVirtual.getGD3Info(vgmBuf, 0);
        }

        if (((Zms) audio.driverVirtual).version != 2) {
            // Check the sound source composition used from ZMD
            boolean useFM = vgmBuf[0x48] != 0;
            boolean useMPCM = vgmBuf[0x49] != 0;
            boolean useMIDI1 = vgmBuf[0x4a] != 0;
            boolean useMIDI2 = vgmBuf[0x4b] != 0;
            boolean useMIDI3 = vgmBuf[0x4c] != 0;
            boolean useMIDI4 = vgmBuf[0x4d] != 0;
            audio.chipLED.put("PriOPM", useFM ? 1 : 0);
            audio.chipLED.put("PriMID", useMIDI1 ? 1 : 0);
            audio.chipLED.put("SecMID", useMIDI2 ? 1 : 0);
            audio.chipLED.put("TrdMID", useMIDI3 ? 1 : 0);
            audio.chipLED.put("ForMID", useMIDI4 ? 1 : 0);
            audio.chipLED.put("PriMPCMX68k", useMPCM ? 1 : 0);
        } else {
            audio.chipLED.put("PriPCM8", 1);
        }

        if (!audio.driverVirtual.init(vgmBuf, this, EnmModel.VirtualModel,
                new Class[] {Ym2151Chip.class, OkiM6258Chip.class},
                setting.getOutputDevice().getSampleRate() * setting.getLatencyEmulation() / 1000,
                setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000))
            return false;
        if (audio.driverReal != null) {
            if (!audio.driverReal.init(vgmBuf, this, EnmModel.RealModel,
                    new Class[] {Ym2151Chip.class, OkiM6258Chip.class},
                    setting.getOutputDevice().getSampleRate() * setting.getLatencySCCI() / 1000,
                    setting.getOutputDevice().getSampleRate() * setting.getOutputDevice().getWaitTime() / 1000)) return false;
        }

        return true;
    }

    private void setVgmBufV3() {
        vgmBuf = ((Zms) audio.driverVirtual).getCompiledData();
        if (audio.driverReal != null) ((Zms) audio.driverReal).setCompiledData(vgmBuf);
//        if (audio.driverPianoRoll != null) ((Zms) audio.driverPianoRoll).compiledData = vgmBuf;
    }

    private void setVgmBufV2() {
        vgmBuf = ((Zms) audio.driverVirtual).getCompiledData();
        ((Zms) audio.driverVirtual).version = 2;
        if (audio.driverReal != null) {
            ((Zms) audio.driverReal).setCompiledData(vgmBuf);
            ((Zms) audio.driverReal).version = 2;
        }
//        if (audio.driverPianoRoll != null) {
//            ((Zms) audio.driverPianoRoll).compiledData = vgmBuf;
//            ((Zms) audio.driverPianoRoll).version = 2;
//        }
    }

    static boolean isExt(String filename, String ext) {
        return Path.getExtension(filename).toUpperCase().equals(ext);
    }
}
