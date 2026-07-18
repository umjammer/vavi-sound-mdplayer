package mdplayer.form.inst;

import java.awt.Component;
import java.util.Map;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.HuC6280Chip;


/** HuSIC @ tone definition */
public class HusicInstWriter implements InstWriter {

    @Override
    public mdplayer.Common.EnmInstFormat format() {
        return mdplayer.Common.EnmInstFormat.HUSIC;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == HuC6280Chip.class) {
            Map<String, Object> huc6280Register = audio.plugin.chipRegister.chip(HuC6280Chip.class).getInfo(chipId);
            if (huc6280Register == null) return;
            if (huc6280Register.get("channels." + ch + ".wave") == null) return;
            int[] wave = (int[]) huc6280Register.get("channels." + ch + ".wave");
            if (wave.length != 32) return;

            n.append("@WTx={\n");

            for (int i = 0; i < 32; i += 8) {
                n.append("$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,$%2x,\n".formatted(
                        (17 - wave[i + 0]),
                        (17 - wave[i + 1]),
                        (17 - wave[i + 2]),
                        (17 - wave[i + 3]),
                        (17 - wave[i + 4]),
                        (17 - wave[i + 5]),
                        (17 - wave[i + 6]),
                        (17 - wave[i + 7])
                ));
            }

            n = new StringBuilder(n.substring(0, n.length() - 3) + "\n}\n");
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }
}
