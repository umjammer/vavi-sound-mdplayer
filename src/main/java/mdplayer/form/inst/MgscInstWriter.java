package mdplayer.form.inst;

import java.awt.Component;
import java.util.Map;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.K051649Chip;
import mdplayer.chips.NpNesChip.Vrc7Chip;
import mdplayer.chips.Ym2413Chip;


/** MGSC (MGSDRV) @ tone definition; the OPLL family and the SCC use this */
public class MgscInstWriter implements InstWriter {

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();
        int[] register = null;

        if (chip == Ym2413Chip.class) {
            register = (int[]) audio.plugin.chipRegister.chip(Ym2413Chip.class).getInfo(chipId).get("register");
        } else if (chip == Vrc7Chip.class) {
            int[] r = (int[]) audio.plugin.chipRegister.chip(Vrc7Chip.class).getInfo(chipId).get("register");
            if (r == null) return;
            register = new int[r.length];
            System.arraycopy(r, 0, register, 0, r.length);
        } else if (chip == K051649Chip.class) {
            writeScc(parent, audio, ch, chipId);
            return;
        }

        if (register == null) return;
        n.append("@vXX = { \n");
        n.append("   ;       TL FB\n");
        n.append("           %2d,%2d,\n".formatted(register[0x02] & 0x3f, register[0x03] & 0x7));
        n.append("   ;       AR DR SL RR KL MT AM VB EG KR DT\n");

        n.append("           %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,\n".formatted(
                (register[0x04] & 0xf0) >> 4,
                (register[0x04] & 0x0f),
                (register[0x06] & 0xf0) >> 4,
                (register[0x06] & 0x0f),
                (register[0x02] & 0xc0) >> 6,
                (register[0x00] & 0x0f),
                (register[0x00] & 0x80) >> 7,
                (register[0x00] & 0x40) >> 6,
                (register[0x00] & 0x20) >> 5,
                (register[0x00] & 0x10) >> 4,
                (register[0x03] & 0x08) >> 3
        ));

        n.append("           %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d }}\n".formatted(
                (register[0x05] & 0xf0) >> 4,
                (register[0x05] & 0x0f),
                (register[0x07] & 0xf0) >> 4,
                (register[0x07] & 0x0f),
                (register[0x03] & 0xc0) >> 6,
                (register[0x01] & 0x0f),
                (register[0x01] & 0x80) >> 7,
                (register[0x01] & 0x40) >> 6,
                (register[0x01] & 0x20) >> 5,
                (register[0x01] & 0x10) >> 4,
                (register[0x03] & 0x10) >> 4
        ));

        Common.setClipboard(n.toString());
    }

    private static void writeScc(Component parent, Audio audio, int ch, int chipId) {
        Map<String, Object> chip = audio.plugin.chipRegister.chip(K051649Chip.class).getInfo(chipId);
        if (chip == null) return;
        int[] register = new int[32];
        for (int i = 0; i < 32; i++) register[i] = (int) chip.get("channels." + ch + ".inst." + i);

        StringBuilder n = new StringBuilder("@sXX = {");
        for (int i = 0; i < 8; i++) {
            n.append(" %02x%02x%02x%02x".formatted(
                    (byte) register[i * 4 + 0], (byte) register[i * 4 + 1],
                    (byte) register[i * 4 + 2], (byte) register[i * 4 + 3]
            ));
        }
        n.append(" }\n");

        Common.setClipboard(n.toString());
    }
}
