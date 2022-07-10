import mdplayer.format.FileFormat;
import mdplayer.plugin.BasePlugin;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * Test001.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-06-03 nsano initial version <br>
 */
public class Test001 {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        FileFormat format = FileFormat.getFileFormat(filename);
Debug.println(format.getClass().getName());
        var r = format.load(null, filename);
        BasePlugin plugin = (BasePlugin) format.getPlugin();
        plugin.setVGMBuffer(format, r.getItem1(), filename, null, 0, 0, r.getItem2());
Debug.println(plugin.getClass().getName());
        plugin.play(filename, format);
    }

    @Test
    void test1() throws Exception {
        String file = "/Users/nsano/src/vavi/vavi-sound-nsf/src/test/resources/smb1.nsf";
//        String file = "src/test/resources/samples/vgm/lemmings_012_tim7.vgm";
        main(new String[] {file});
    }
}
