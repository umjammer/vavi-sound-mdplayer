package vavi.sound.visualizer.fmdsp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Pc98GaijiTest {

    /** every cell of the maker's rows has to come back as itself - a JIS code is too big to carry */
    @Test
    void testRoundTrip() {
        int n = 0;
        for (int row = 1; row <= 94; row++) {
            for (int cell = 1; cell <= 94; cell++) {
                int jis = (row + 0x20) << 8 | (cell + 0x20);
                char pua = Pc98Gaiji.puaOf(jis);
                boolean makers = (row >= 9 && row <= 12) || (row >= 85 && row <= 94);
                if (!makers) {
                    assertEquals(0, pua, "row " + row);
                    continue;
                }
                assertTrue(pua >= 0xe000 && pua <= 0xf8ff, "row " + row + " cell " + cell);
                assertEquals(jis, Pc98Gaiji.jisOf(pua), "row " + row + " cell " + cell);
                n++;
            }
        }
        assertEquals((4 + 10) * 94, n);
    }

    @Test
    void testHalfWidthRows() {
        assertTrue(Pc98Gaiji.isHalfWidth(Pc98Gaiji.puaOf(0x2921))); // row 9, the ASCII one
        assertTrue(Pc98Gaiji.isHalfWidth(Pc98Gaiji.puaOf(0x2c7e))); // row 12, the last of them
        assertFalse(Pc98Gaiji.isHalfWidth(Pc98Gaiji.puaOf(0x7521))); // row 85, a gaiji
        assertFalse(Pc98Gaiji.isHalfWidth('a'));
    }

    @Test
    void testNonGaijiCharactersCarryNothing() {
        assertEquals(0, Pc98Gaiji.jisOf('a'));
        assertEquals(0, Pc98Gaiji.jisOf('亜'));
        assertEquals(0, Pc98Gaiji.puaOf(0x3021)); // 亜, a row JIS X 0208 uses itself
    }
}
