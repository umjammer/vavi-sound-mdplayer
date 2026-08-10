package mdplayer.form.inst;

import java.awt.Component;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmInstFormat;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;


/** MUSIC LALF tone definition, table layout */
public class MusicLalf2InstWriter implements InstWriter {

    @Override
    public EnmInstFormat format() {
        return EnmInstFormat.MUSICLALF2;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        StringBuilder n = new StringBuilder();

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n.append("@%xxx\n");

            for (int i = 0; i < 6; i++) {
                n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                        fmRegister[p][0x30 + 0 + c + i * 0x10] & 0xff,
                        fmRegister[p][0x30 + 8 + c + i * 0x10] & 0xff,
                        fmRegister[p][0x30 + 16 + c + i * 0x10] & 0xff,
                        fmRegister[p][0x30 + 24 + c + i * 0x10] & 0xff
                ));
            }
            n.append("$%3x\n".formatted(
                    fmRegister[p][0xb0 + c] // FB/AL
            ));
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n.append("@%xxx\n");

            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0x40 + 0 + ch] & 0x7f),  // DT/ML
                    (ym2151Register[0x40 + 8 + ch] & 0x7f),  // DT/ML
                    (ym2151Register[0x40 + 16 + ch] & 0x7f), // DT/ML
                    (ym2151Register[0x40 + 24 + ch] & 0x7f)  // DT/ML
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0x60 + 0 + ch] & 0x7f),  // TL
                    (ym2151Register[0x60 + 8 + ch] & 0x7f),  // TL
                    (ym2151Register[0x60 + 16 + ch] & 0x7f), // TL
                    (ym2151Register[0x60 + 24 + ch] & 0x7f)  // TL
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0x80 + 0 + ch] & 0xdf),  // KS/AR
                    (ym2151Register[0x80 + 8 + ch] & 0xdf),  // KS/AR
                    (ym2151Register[0x80 + 16 + ch] & 0xdf), // KS/AR
                    (ym2151Register[0x80 + 24 + ch] & 0xdf)  // KS/AR
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0xa0 + 0 + ch] & 0x9f),  // AM/DR
                    (ym2151Register[0xa0 + 8 + ch] & 0x9f),  // AM/DR
                    (ym2151Register[0xa0 + 16 + ch] & 0x9f), // AM/DR
                    (ym2151Register[0xa0 + 24 + ch] & 0x9f)  // AM/DR
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0xc0 + 0 + ch] & 0x1f),  // SR
                    (ym2151Register[0xc0 + 8 + ch] & 0x1f),  // SR
                    (ym2151Register[0xc0 + 16 + ch] & 0x1f), // SR
                    (ym2151Register[0xc0 + 24 + ch] & 0x1f)  // SR
            ));
            n.append("$%3x,$%3x,$%3x,$%3x\n".formatted(
                    (ym2151Register[0xe0 + 0 + ch] & 0xff),  // SL/RR
                    (ym2151Register[0xe0 + 8 + ch] & 0xff),  // SL/RR
                    (ym2151Register[0xe0 + 16 + ch] & 0xff), // SL/RR
                    (ym2151Register[0xe0 + 24 + ch] & 0xff)  // SL/RR
            ));

            n.append("$%3x\n".formatted(ym2151Register[0x20 + ch])); // FB/AL
        }

        if (n.isEmpty()) Common.setClipboard(n.toString());
    }
}
