package mdplayer.driver.moonDriver;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import mdplayer.Common;
import mdplayer.Setting;
import mdplayer.driver.BaseDriver;
import mdplayer.driver.BasePlugin;
import mdplayer.driver.FileFormat;
import mdsound.chips.YmF278B;
import mdsound.instrument.YmF278BInst;


/**
 * Reads the OPL4's wave rom and sample RAM back out after a song has started and compares them
 * with a reference file, to tell whether the samples a song plays actually reached the chip.
 * <p>
 * It takes any format, so the same song as a .MDR and as a .VGZ (the vgm carries the sample RAM
 * as a data block) can be held against each other - which is how the missing user PCM bank of
 * TIMESUP.MDR was found. Note the vgm writes its block a little way into playback, so give it
 * some buffers before looking.
 * <p>
 * usage: MoonRamCheck &lt;song&gt; &lt;reference pcm&gt; [buffers to render first]
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-01 nsano initial version <br>
 */
public class MoonRamCheck {

    public static void main(String[] args) throws Exception {
        String in = args[0];
        byte[] ref = Files.readAllBytes(Path.of(args[1]));

        Setting setting = Setting.getInstance();
        setting.getOutputDevice().setDeviceType(Common.DEV_Null);

        FileFormat format = FileFormat.getFileFormat(in);
        format.load(Files.newInputStream(Path.of(in)), null);
        @SuppressWarnings("unchecked")
        BasePlugin<? extends BaseDriver> plugin = (BasePlugin<? extends BaseDriver>) format.getPlugin();
        plugin.setParams(format, Map.of("fileName", in));
        plugin.prepare();

        short[] buffer = new short[8192];
        int renders = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        for (int i = 0; i < renders; i++) plugin.getDriver().render(buffer, 0, buffer.length);

        YmF278BInst inst = plugin.mds.inst(YmF278BInst.class);
        Field chipsField = YmF278BInst.class.getDeclaredField("chips");
        chipsField.setAccessible(true);
        YmF278B chip = ((YmF278B[]) chipsField.get(inst))[0];
        Field ramField = YmF278B.class.getDeclaredField("ram");
        ramField.setAccessible(true);
        byte[] ram = (byte[]) ramField.get(chip);

        int same = 0;
        for (int i = 0; i < ref.length; i++) if (i < ram.length && ram[i] == ref[i]) same++;
        System.err.printf("ram %d bytes, reference %d bytes, matching %d (%.4f)%n",
                ram.length, ref.length, same, same / (double) ref.length);

        Field romField = YmF278B.class.getDeclaredField("rom");
        romField.setAccessible(true);
        byte[] rom = (byte[]) romField.get(chip);
        Field romSizeField = YmF278B.class.getDeclaredField("romSize");
        romSizeField.setAccessible(true);
        System.err.printf("rom %s bytes (romSize %d)%n", rom == null ? "null" : rom.length, romSizeField.get(chip));
        if (rom != null) {
            int romSame = 0;
            for (int i = 0; i < ref.length && i < rom.length; i++) if (rom[i] == ref[i]) romSame++;
            System.err.printf("rom vs reference matching %d (%.4f), rom[0:16]=%s%n",
                    romSame, romSame / (double) ref.length, java.util.HexFormat.of().formatHex(rom, 0, 16));
        }
        System.err.printf("ram[0:16]=%s%n", java.util.HexFormat.of().formatHex(ram, 0, 16));
        System.exit(0);
    }
}
