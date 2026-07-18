package mdplayer.form.inst;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import mdplayer.Audio;
import mdplayer.Chip;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;


/** DefleMask .dmp instrument file */
public class DmpInstWriter implements InstWriter {

    @Override
    public mdplayer.Common.EnmInstFormat format() {
        return mdplayer.Common.EnmInstFormat.DMP;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        byte[] n = new byte[51];
        n[0] = 0x0b; // FILE_VERSION
        n[2] = 0x01; // Instrument Mode(1=FM)

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

            n[1] = 0x02; // SYSTEM_GENESIS

            n[3] = (byte) (fmRegister[p][0xb4 + c] & 0x03); // LFO (FMS on Ym2612Inst, PMS on YM2151)
            n[4] = (byte) ((fmRegister[p][0xb0 + c] & 0x38) >> 3); // FB
            n[5] = (byte) (fmRegister[p][0xb0 + c] & 0x07); // ALG
            n[6] = (byte) ((fmRegister[p][0xb4 + c] & 0x30) >> 4); // LFO2(AMS on Ym2612Inst, AMS on YM2151)

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 4 : ((i == 2) ? 8 : 12));
                int ops = i * 4;

                n[i * 11 + 7] = (byte) (fmRegister[p][0x30 + ops + c] & 0x0f); // ML
                n[i * 11 + 8] = (byte) (fmRegister[p][0x40 + ops + c] & 0x7f); // TL
                n[i * 11 + 9] = (byte) (fmRegister[p][0x50 + ops + c] & 0x1f); //AR
                n[i * 11 + 10] = (byte) (fmRegister[p][0x60 + ops + c] & 0x1f); //DR
                n[i * 11 + 11] = (byte) ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4); // SL
                n[i * 11 + 12] = (byte) (fmRegister[p][0x80 + ops + c] & 0x0f); //RR
                n[i * 11 + 13] = (byte) ((fmRegister[p][0x60 + ops + c] & 0x80) >> 7); //AM
                n[i * 11 + 14] = (byte) ((fmRegister[p][0x50 + ops + c] & 0xc0) >> 6); // KS
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4; // DT
                dt = (dt == 4) ? 0 : dt;
                // 0>5(-3)  1>6(-2)  2>7(-1)  3>0/4  4>1  5>2  6>3  7>3
                dt = (dt > 4) ? (dt - 5) : (dt + 3);
                n[i * 11 + 15] = (byte) (dt & 7);
                n[i * 11 + 16] = (byte) (fmRegister[p][0x70 + ops + c] & 0x1f); //SR
                n[i * 11 + 17] = (byte) (fmRegister[p][0x90 + ops + c] & 0x0f); // SSG
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n[1] = 0x08; // SYSTEM_YM2151

            n[3] = (byte) ((ym2151Register[0x38 + ch] & 0x70) >> 4); // LFO (FMS on Ym2612Inst, PMS on YM2151)
            n[4] = (byte) ((ym2151Register[0x20 + ch] & 0x38) >> 3); // FB
            n[5] = (byte) (ym2151Register[0x20 + ch] & 0x07); // AL
            n[6] = (byte) (ym2151Register[0x38 + ch] & 0x03); // LFO2(AMS on Ym2612Inst, AMS on YM2151)

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;

                n[i * 11 + 7] = (byte) (ym2151Register[0x40 + ops + ch] & 0x0f); // ML
                n[i * 11 + 8] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 11 + 9] = (byte) (ym2151Register[0x80 + ops + ch] & 0x1f); // AR
                n[i * 11 + 10] = (byte) (ym2151Register[0xa0 + ops + ch] & 0x1f); // DR
                n[i * 11 + 11] = (byte) ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4); // SL
                n[i * 11 + 12] = (byte) (ym2151Register[0xe0 + ops + ch] & 0x0f); // RR
                n[i * 11 + 13] = (byte) ((ym2151Register[0xa0 + ops + ch] & 0x80) >> 7); // AM
                n[i * 11 + 14] = (byte) ((ym2151Register[0x80 + ops + ch] & 0xc0) >> 6); // KS
                int dt = ((ym2151Register[0x40 + ops + ch] & 0x70) >> 4); // DT
                dt = (dt == 4) ? 0 : dt;
                // 0>5(-3)  1>6(-2)  2>7(-1)  3>0/4  4>1  5>2  6>3  7>3
                dt = (dt > 4) ? (dt - 5) : (dt + 3);
                int dt2 = (byte) ((ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6); // DT2
                n[i * 11 + 15] = (byte) ((dt & 0x7) | (dt2 << 4));
                n[i * 11 + 16] = (byte) (ym2151Register[0xc0 + ops + ch] & 0x1f); // SR
                n[i * 11 + 17] = 0;
            }

        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.dmp"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".dmp");
            }

            @Override
            public String getDescription() {
                return "DMP File(*.dmp)";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (OutputStream fs = Files.newOutputStream(Path.of(sfd.getSelectedFile().getName()))) {

            fs.write(n, 0, n.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
