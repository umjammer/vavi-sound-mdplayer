package mdplayer.form.inst;

import java.awt.Component;
import java.util.Map;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.K051649Chip;


/** MGSC SCC waveform, plain hex layout */
public class MgscSccPlainInstWriter implements InstWriter {

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {
        Map<String, Object> info = audio.plugin.chipRegister.chip(K051649Chip.class).getInfo(chipId);
        if (info == null) return;
        int[] register = new int[32];
        for (int i = 0; i < 32; i++) register[i] = (int) info.get("channels." + ch + ".inst." + i);

        StringBuilder n = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            n.append("%2x%2x%2x%2x".formatted(
                    (byte) register[i * 4 + 0], (byte) register[i * 4 + 1],
                    (byte) register[i * 4 + 2], (byte) register[i * 4 + 3]
            ));
        }
        n.append("\n");

        Common.setClipboard(n.toString());
    }
}
