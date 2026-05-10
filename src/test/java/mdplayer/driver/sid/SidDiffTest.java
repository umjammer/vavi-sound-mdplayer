package mdplayer.driver.sid;

import java.nio.file.Files;
import java.nio.file.Paths;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.SidChip;
import mdplayer.driver.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.playSidFp;
import mdplayer.plugin.BasePlugin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Disabled("ai iteration")
public class SidDiffTest {

    @Test
    public void testDriver() throws Exception {
        byte[] fileBuffer = Files.readAllBytes(Paths.get("../JSIDPlay2/tmp/Formula_1_Simulator.sid"));
        
        // 1. Setup SidMdDriver
        BasePlugin plugin = mock(BasePlugin.class, Mockito.RETURNS_DEEP_STUBS);
        java.lang.reflect.Field field = BasePlugin.class.getDeclaredField("chipRegister");
        field.setAccessible(true);
        field.set(plugin, mock(mdplayer.ChipRegister.class));
        
        Setting.SID sidSetting = new Setting.SID();
        sidSetting.outputBufferSize = 5000;
        sidSetting.quality = 0; // Means INTERPOLATE
        sidSetting.c64model = 0;
        sidSetting.sidModel = 0;
        sidSetting.c64modelForce = false;
        sidSetting.sidmodelForce = false;
        
        Setting.getInstance().setSid(sidSetting);
        Setting.getInstance().getOutputDevice().setSampleRate(44100);

        SidChip sidChip = new SidChip();
        when(plugin.chipRegister.chip(SidChip.class)).thenReturn(sidChip);

        SidMdDriver driver = new SidMdDriver(plugin) {
            {
                this.dataBuf = fileBuffer;
            }
        };
        // waitTime and latency are 0, args[0] = 1 (song)
        driver.init(EnmModel.VirtualModel, 0, 0, 1);
        
        // 2. Setup SidTestProgram engine
        playSidFp engine = new playSidFp(44100);
        engine.setRoms(null, null, null);

        ReSidBuilder rs = new ReSidBuilder("ReSid", 44100);
        rs.create(1);

        SidTune tune = new SidTune(fileBuffer, fileBuffer.length);
        tune.selectSong(1);

        engine.load(tune);

        SidConfig cfg = new SidConfig(44100);
        cfg.frequency = 44100;
        cfg.samplingMethod = SidConfig.SamplingMethod.INTERPOLATE;
        cfg.fastSampling = true;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.sidEmulation = rs;
        engine.config(cfg);

        // 3. Compare outputs
        int bufferSize = 2048;
        short[] driverSampleBuffer = new short[bufferSize * 2]; // stereo
        short[] testSampleBuffer = new short[bufferSize * 2]; // stereo

        int mismatches = 0;

        for (int frame = 0; frame < 100; frame++) {
            int producedDriver = driver.render(driverSampleBuffer, 0, bufferSize * 2);
            int producedEngine = engine.play(testSampleBuffer, bufferSize * 2);
            if (frame == 0) {
                System.err.println("Driver returned: " + producedDriver);
                System.err.println("Engine returned: " + producedEngine);
            }
            
            for (int i = 0; i < bufferSize * 2; i++) {
                if (driverSampleBuffer[i] != testSampleBuffer[i]) {
                    mismatches++;
                    if (mismatches < 10) {
                        System.err.printf("Mismatch at frame %d, pos %d: driver=%d test=%d%n", 
                            frame, i, driverSampleBuffer[i], testSampleBuffer[i]);
                    }
                }
            }
        }
        
        System.err.println("Total mismatches: " + mismatches);
    }
}