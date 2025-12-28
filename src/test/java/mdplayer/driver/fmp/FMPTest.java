package mdplayer.driver.fmp;

import java.lang.reflect.Field;

import mdplayer.Common.EnmModel;
import mdplayer.Setting;
import mdplayer.driver.Vgm;
import mdplayer.driver.fmp.nise98.FileTemp;
import mdplayer.driver.fmp.nise98.Memory98;
import mdplayer.driver.fmp.nise98.Nise98;
import mdplayer.driver.fmp.nise98.NiseDos;
import mdplayer.driver.fmp.nise98.NisePpz8;
import mdplayer.driver.fmp.nise98.Register286;
import mdplayer.plugin.BasePlugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FMPTest {

    @Mock
    FileTemp fileTemp;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    BasePlugin plugin;

    @Mock
    Nise98 nise98;

    @Mock
    NiseDos niseDos;

    @Mock
    Register286 regs;

    @Mock
    Memory98 memory98;

    @Mock
    NisePpz8 nisePPZ8;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Setting setting;

    FMP driver;

    @BeforeEach
    void setUp() throws Exception {
        driver = new FMP(fileTemp);

        // Inject mock Nise98
        Field niseField = FMP.class.getDeclaredField("nise98");
        niseField.setAccessible(true);
        niseField.set(driver, nise98);

        // Inject mock Setting into BaseDriver
        Field settingField = mdplayer.driver.BaseDriver.class.getDeclaredField("setting");
        settingField.setAccessible(true);
        settingField.set(driver, setting);

        // Set playingFileName to avoid NPE during init
        Field fileNameField = FMP.class.getDeclaredField("playingFileName");
        fileNameField.setAccessible(true);
        fileNameField.set(driver, "test.m");
    }

    @Test
    void testGetGD3Info() {
        byte[] buf = new byte[32];
        buf[0] = 0x10; // Offset
        
        Vgm.Gd3 gd3 = driver.getGD3Info(buf, null);
        assertNotNull(gd3);
    }

    @Test
    void testInitSuccess() throws Exception {
        byte[] vgmBuf = new byte[100];

        when(nise98.getDos()).thenReturn(niseDos);
        when(nise98.getRegisters()).thenReturn(regs);
        when(nise98.getMem()).thenReturn(memory98);
        when(nise98.getPPZ8()).thenReturn(nisePPZ8);
        when(nise98.loadRun(anyString(), anyString(), anyInt())).thenReturn(0);

        doAnswer(invocation -> {
            int[] stepArr = invocation.getArgument(0);
            Register286[] regsArr = invocation.getArgument(1);
            stepArr[0] = 1;
            regsArr[0] = regs;
            return null;
        }).when(nisePPZ8).fmpRegisterPPZ8(any(), any());

        boolean result = driver.init(vgmBuf, plugin, EnmModel.VirtualModel, null, 0, 0);

        assertTrue(result);
        verify(nise98).init(any(), any(), eq(fileTemp), any());
        verify(nise98).loadRun(contains("FMP.COM"), anyString(), eq(0x2000));
        verify(nisePPZ8).setCallBack(any(), any());
    }

    @Test
    void testProcessOneFrame() throws Exception {
        Field regsField = FMP.class.getDeclaredField("regs");
        regsField.setAccessible(true);
        regsField.set(driver, regs);

        Field vgmFrameCounterField = mdplayer.driver.BaseDriver.class.getDeclaredField("vgmFrameCounter");
        vgmFrameCounterField.setAccessible(true);
        vgmFrameCounterField.set(driver, 0);

        Field vgmSpeedField = mdplayer.driver.BaseDriver.class.getDeclaredField("vgmSpeed");
        vgmSpeedField.setAccessible(true);
        vgmSpeedField.set(driver, 1.0);

        when(setting.getOutputDevice().getSampleRate()).thenReturn(44100);
        when(nise98.intTimer()).thenReturn(true);
        when(regs.getAX()).thenReturn((short) 1);
        when(nise98.getMem()).thenReturn(memory98);

        driver.processOneFrame();

        verify(nise98, atLeastOnce()).runTimer();
    }
}