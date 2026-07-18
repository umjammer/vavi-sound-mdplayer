package mdplayer.form.inst;

import java.awt.Component;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;


/** VOPM (VST) .opm voice definition */
public class VopmInstWriter implements InstWriter {

    @Override
    public mdplayer.Common.EnmInstFormat format() {
        return mdplayer.Common.EnmInstFormat.VOPM;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class) ?
                    (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class ?
                          new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("@: n MDPlayer\n");
            n.append("LFO:  0   0   0   0   0\n");
            n.append("CH: 64  %2d  %2d   0   0 120   0\n".formatted(
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3, // FB
                    fmRegister[p][0xb0 + c] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("%s:%3d %3d %3d %3d %3d ".formatted(
                        "M1C1M2C2".substring(i * 2, 2),
                        fmRegister[p][0x50 + ops + c] & 0x1f,       // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,       // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,       // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,       // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4 // SL
                ));
                n.append("%3d %3d %3d %3d   0 %3d\n".formatted(
                        fmRegister[p][0x40 + ops + c] & 0x7f, // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7  // AM
                ));
            }
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("@: n MDPlayer\n");
            n.append("LFO:  0   0   0   0   0\n");
            n.append("CH: 64  %2d  %2d   0   0 120   0\n".formatted(
                    (ym2151Register[0x20 + ch] & 0x38) >> 3, // FB
                    ym2151Register[0x20 + ch] & 0x07 // AL
            ));

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("%s:%3d %3d %3d %3d %3d ".formatted(
                        "M1C1M2C2".substring(i * 2, 2),
                        ym2151Register[0x80 + ops + ch] & 0x1f,       // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,       // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,       // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,       // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4 // SL
                ));
                n.append("%3d %3d %3d %3d %3d %3d\n".formatted(
                        ym2151Register[0x60 + ops + ch] & 0x7f, // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }
}
