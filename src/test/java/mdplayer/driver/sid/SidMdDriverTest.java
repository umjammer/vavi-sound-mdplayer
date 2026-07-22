package mdplayer.driver.sid;

import java.nio.file.Files;
import java.nio.file.Paths;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.chips.SidChip;
import mdplayer.plugin.BasePlugin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@EnabledIfSystemProperty(named = "vavi.test", matches = "ai")
public class SidMdDriverTest {

    @Test
    public void testDriver() throws Exception {
        byte[] fileBuffer = Files.readAllBytes(Paths.get("../JSIDPlay2/tmp/Formula_1_Simulator.sid"));
        
        BasePlugin plugin = mock(BasePlugin.class, Mockito.RETURNS_DEEP_STUBS);
        java.lang.reflect.Field field = BasePlugin.class.getDeclaredField("chipRegister");
        field.setAccessible(true);
        field.set(plugin, mock(mdplayer.ChipRegister.class));
        
        Setting.SID sidSetting = new Setting.SID();
        sidSetting.outputBufferSize = 5000;
        sidSetting.quality = 0;
        sidSetting.c64model = 0;
        sidSetting.sidModel = 0;
        sidSetting.c64modelForce = false;
        sidSetting.sidmodelForce = false;
        
        Setting.getInstance().setSid(sidSetting);
        Setting.getInstance().getOutputDevice().setSampleRate(44100);

        // Mock SidChip for driver
        SidChip sidChip = new SidChip();
        when(plugin.chipRegister.chip(SidChip.class)).thenReturn(sidChip);

        SidMdDriver driver = new SidMdDriver(plugin) {
            {
                this.dataBuf = fileBuffer;
            }
        };
        driver.init(EnmModel.VirtualModel, 0, 0, 1);
        
        int bufferSize = 2048;
        short[] sampleBuffer = new short[bufferSize * 2]; // stereo
        
        long totalSquared = 0;
        int totalSamples = 0;
        int peak = 0;
        int validBuffers = 0;

        for (int frame = 0; frame < 100; frame++) {
            driver.render(sampleBuffer, 0, bufferSize * 2);
            
            for (int i = 0; i < bufferSize * 2; i++) {
                short val = sampleBuffer[i];
                totalSquared += (long) val * val;
                int absVal = Math.abs(val);
                if (absVal > peak) peak = absVal;
                totalSamples++;
            }
            
            if (frame % 10 == 0 && totalSamples > 0) {
                double rms = Math.sqrt((double) totalSquared / totalSamples);
                System.err.printf("Stats frame=%d: RMS=%.2f Peak=%d%n", frame, rms, peak);
                
                if (frame > 20) {
                    if (rms > 50.0 && peak > 2000) {
                        validBuffers++;
                    }
                }
                
                totalSquared = 0;
                totalSamples = 0;
                peak = 0;
            }
        }
        
        System.err.println("Valid buffers: " + validBuffers);
        // We just run this to see what output looks like first.
        // It should match SidTestProgram behavior.
    }
}
