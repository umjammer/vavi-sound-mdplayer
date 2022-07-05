package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * WaveformGeneratorTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-07-04 nsano initial version <br>
 */
class WaveformGeneratorTest {

    @Test
    void test1() {
        WaveformGenerator wg = new WaveformGenerator();
        assertEquals(0x3c0, wg.modelWave[0][3][4087]);
    }
}
