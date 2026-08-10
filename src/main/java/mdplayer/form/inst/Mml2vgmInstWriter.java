package mdplayer.form.inst;

import java.awt.Component;
import java.util.Map;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.Common;
import mdplayer.Common.EnmInstFormat;
import mdplayer.chips.HuC6280Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2413Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;
import mdplayer.chips.Ym3812Chip;


/** mml2vgm @ tone definition */
public class Mml2vgmInstWriter implements InstWriter {

    private static final int[] slot1Tbl = {0, 1, 2, 6, 7, 8, 12, 13, 14};
    private static final int[] slot2Tbl = {3, 4, 5, 9, 10, 11, 15, 16, 17};

    @Override
    public EnmInstFormat format() {
        return EnmInstFormat.MML2VGM;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {
        String n = text(parent, audio, chip, ch, chipId);
        if (n != null && n.isEmpty()) Common.setClipboard(n);
    }

    static String text(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

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

            n.append("'@ N xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT  AM  SSG-EG\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                n.append("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        fmRegister[p][0x50 + ops + c] & 0x1f, // AR
                        fmRegister[p][0x60 + ops + c] & 0x1f,        // DR
                        fmRegister[p][0x70 + ops + c] & 0x1f,        // SR
                        fmRegister[p][0x80 + ops + c] & 0x0f,        // RR
                        (fmRegister[p][0x80 + ops + c] & 0xf0) >> 4, // SL
                        fmRegister[p][0x40 + ops + c] & 0x7f,        // TL
                        (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6, // KS
                        fmRegister[p][0x30 + ops + c] & 0x0f,        // ML
                        (fmRegister[p][0x30 + ops + c] & 0x70) >> 4, // DT
                        (fmRegister[p][0x60 + ops + c] & 0x80) >> 7, // AM
                        fmRegister[p][0x90 + ops + c] & 0x0f         // SG
                ));
            }
            n.append("   ALG FB\n");
            n.append("'@ %3d,%3d\n".formatted(
                    fmRegister[p][0xb0 + c] & 0x07, // AL
                    (fmRegister[p][0xb0 + c] & 0x38) >> 3  // FB
            ));
        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");
            n.append("'@ M xx\n   AR  DR  SR  RR  SL  TL  KS  ML  DT1 DT2 AME\n");

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                n.append("'@ %3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d,%3d\n".formatted(
                        ym2151Register[0x80 + ops + ch] & 0x1f, // AR
                        ym2151Register[0xa0 + ops + ch] & 0x1f,        // DR
                        ym2151Register[0xc0 + ops + ch] & 0x1f,        // SR
                        ym2151Register[0xe0 + ops + ch] & 0x0f,        // RR
                        (ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4, // SL
                        ym2151Register[0x60 + ops + ch] & 0x7f,        // TL
                        (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6, // KS
                        ym2151Register[0x40 + ops + ch] & 0x0f,        // ML
                        (ym2151Register[0x40 + ops + ch] & 0x70) >> 4, // DT1
                        (ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6, // DT2
                        (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7  // AM
                ));
            }
            n.append("   ALG FB\n");
            n.append("'@ %3d,%3d\n".formatted(
                    ym2151Register[0x20 + ch] & 0x07, // AL
                    (ym2151Register[0x20 + ch] & 0x38) >> 3  // FB
            ));
        } else if (chip == HuC6280Chip.class) {
            Map<String, Object> huc6280Register = audio.plugin.chipRegister.chip(HuC6280Chip.class).getInfo(chipId);
            if (huc6280Register == null) return null;
            if (huc6280Register.get("channels." + ch + ".wave") == null) return null;
            int[] wave = (int[]) huc6280Register.get("channels." + ch + ".wave");
            if (wave.length != 32) return null;

            n.append("'@ H xx,\n   +0 +1 +2 +3 +4 +5 +6 +7\n");

            for (int i = 0; i < 32; i += 8) {
                n.append("'@ %2d,%2d,%2d,%2d,%2d,%2d,%2d,%2d\n".formatted(
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
        } else if (chip == Ym2413Chip.class) {
            // Ym2413
            int[] regs = (int[]) audio.plugin.chipRegister.chip(Ym2413Chip.class).getInfo(chipId).get("register");
        } else if (chip == Ym3812Chip.class) {
            // OPL2
            // '@ L No "Name"
            // '@ AR DR SL RR KSL TL MT AM VIB EGT KSR WS
            // '@ AR DR SL RR KSL TL MT AM VIB EGT KSR WS
            // '@ CNT FB

            int[] regs = (int[]) audio.plugin.chipRegister.chip(Ym3812Chip.class).getInfo(chipId).get("register");
            int slot;
            if (ch < 0 || ch > 8) return null;

            n.append("'@ L No \"MDP\"\n   AR DR SL RR KSL TL MT AM VIB EGT KSR WS\n");
            for (int i = 0; i < 2; i++) {
                if (i == 0) slot = slot1Tbl[ch];
                else slot = slot2Tbl[ch];

                slot = (slot % 6) + 8 * (slot / 6);
                n.append("'@ %2d,%2d,%2d,%2d, %2d,%2d,%2d,%2d, %2d, %2d, %2d,%2d\n".formatted(
                        regs[0x60 + slot] >> 4,
                        regs[0x60 + slot] & 0xf,
                        regs[0x80 + slot] >> 4,
                        regs[0x80 + slot] & 0xf,
                        regs[0x40 + slot] >> 6,
                        regs[0x40 + slot] & 0x3f,
                        regs[0x20 + slot] & 0xf,
                        regs[0x20 + slot] >> 7,
                        (regs[0x20 + slot] >> 6) & 1,
                        (regs[0x20 + slot] >> 5) & 1,
                        (regs[0x20 + slot] >> 4) & 1,
                        (regs[0xe0 + slot] & 3)
                ));
            }
            n.append("   CNT FB\n'@  %2d,%2d\n".formatted(
                    (regs[0xc0 + ch] & 1),
                    (regs[0xc0 + ch] >> 1) & 7
            ));
        }

        return n.toString();
    }
}
