package mdplayer.driver.mndrv;

import java.nio.file.Files;
import java.nio.file.Paths;

import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;


@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
public class MndrvTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "vavi.test.volume")
    double volume = 0.2;

    @Property
    String mnd;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("mdplayer.volume", "%4.2f".formatted(volume));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    public void test1() throws Exception {
        MndrvTestProgram.main(new String[]{mnd});
    }

    @Test
    @DisplayName("compare output wav quality")
    public void test2() throws Exception {
        MndrvWavTestProgram.main(new String[]{
                "tmp/mnd/MND9827/eve98@27.mnd", // source
                "tmp/mnd/orig/eve98@27.wav" // the original c# output
        });
    }
}
