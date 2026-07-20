package mdplayer.form.inst;

import java.awt.Component;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.NpNesChip;
import mdplayer.chips.NpNesChip.N163Chip;
import mdsound.np.chip.NesN106;


/** MCK/ppmck N163 waveform definition */
public class MckInstWriter implements InstWriter {

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {
        if (chip == N163Chip.class) {
            NesN106.TrackInfo[] info = (NesN106.TrackInfo[]) audio.plugin.chipRegister.chip(NpNesChip.N163Chip.class).getInfo(0).get("tracksInfo");
            if (info == null) return;

            StringBuilder n = new StringBuilder("@Nxx = { ");
            n.append("%d ".formatted(info[ch].waveLen));
            for (int i = 0; i < info[ch].waveLen; i++) {
                n.append("%d ".formatted((byte) info[ch].wave[i]));
            }
            n.append("}\n");

            Common.setClipboard(n.toString());
        }
    }
}
