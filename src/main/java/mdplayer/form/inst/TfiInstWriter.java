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
import mdplayer.Common.EnmInstFormat;
import mdplayer.chips.Ym2151Chip;
import mdplayer.chips.Ym2203Chip;
import mdplayer.chips.Ym2608Chip;
import mdplayer.chips.Ym2610Chip;
import mdplayer.chips.Ym2612Chip;


/** TFM Music Maker .tfi instrument file */
public class TfiInstWriter implements InstWriter {

    @Override
    public EnmInstFormat format() {
        return EnmInstFormat.TFI;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        byte[] n = new byte[42];

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

            n[0] = (byte) (fmRegister[p][0xb0 + c] & 0x07); // AL
            n[1] = (byte) ((fmRegister[p][0xb0 + c] & 0x38) >> 3); // FB


            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 4 : ((i == 2) ? 8 : 12));
                int ops = i * 4;

                n[i * 10 + 2] = (byte) (fmRegister[p][0x30 + ops + c] & 0x0f); // ML
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4; // DT
                // 0>3  1>4  2>5  3>6  4>3  5>2  6>1  7>0
                dt = (dt < 4) ? (dt + 3) : (7 - dt);
                n[i * 10 + 3] = (byte) dt;
                n[i * 10 + 4] = (byte) (fmRegister[p][0x40 + ops + c] & 0x7f); // TL
                n[i * 10 + 5] = (byte) ((fmRegister[p][0x50 + ops + c] & 0xc0) >> 6); // KS
                n[i * 10 + 6] = (byte) (fmRegister[p][0x50 + ops + c] & 0x1f); // AR
                n[i * 10 + 7] = (byte) (fmRegister[p][0x60 + ops + c] & 0x1f); // DR
                n[i * 10 + 8] = (byte) (fmRegister[p][0x70 + ops + c] & 0x1f); // SR
                n[i * 10 + 9] = (byte) (fmRegister[p][0x80 + ops + c] & 0x0f); // RR
                n[i * 10 + 10] = (byte) ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4); // SL
                n[i * 10 + 11] = (byte) (fmRegister[p][0x90 + ops + c] & 0x0f); // SSG
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n[0] = (byte) (ym2151Register[0x20 + ch] & 0x07); // AL
            n[1] = (byte) ((ym2151Register[0x20 + ch] & 0x38) >> 3); // FB

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;

                n[i * 10 + 2] = (byte) (ym2151Register[0x40 + ops + ch] & 0x0f); // ML
                int dt = ((ym2151Register[0x40 + ops + ch] & 0x70) >> 4); // DT
                // 0>3  1>4  2>5  3>6  4>3  5>2  6>1  7>0
                dt = (dt < 4) ? (dt + 3) : (7 - dt);
                n[i * 10 + 3] = (byte) dt;
                n[i * 10 + 4] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 10 + 5] = (byte) ((ym2151Register[0x80 + ops + ch] & 0xc0) >> 6); // KS
                n[i * 10 + 6] = (byte) (ym2151Register[0x80 + ops + ch] & 0x1f); // AR
                n[i * 10 + 7] = (byte) (ym2151Register[0xa0 + ops + ch] & 0x1f); // DR
                n[i * 10 + 8] = (byte) (ym2151Register[0xc0 + ops + ch] & 0x1f); // SR
                n[i * 10 + 9] = (byte) (ym2151Register[0xe0 + ops + ch] & 0x0f); // RR
                n[i * 10 + 10] = (byte) ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4); // SL
                n[i * 10 + 11] = 0;
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.tfi"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".tfi");
            }

            @Override
            public String getDescription() {
                return "TFI File(*.tfi)";
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
