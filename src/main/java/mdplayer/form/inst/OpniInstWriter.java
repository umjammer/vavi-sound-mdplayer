package mdplayer.form.inst;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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


/** VGM Music Maker .opni instrument file */
public class OpniInstWriter implements InstWriter {

    @Override
    public EnmInstFormat format() {
        return EnmInstFormat.OPNI;
    }

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        byte[] n = new byte[77];
        Arrays.fill(n, 0, n.length, (byte) 0);
        byte[] data = "WOPN2-INST".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 0, 10);
        n[10] = 0x00;
        n[11] = 0x00; // 0 - melodic, or 1 - percussion
        data = "MDPlayer".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 12, 8);
        n[12 + 32 + 0] = 0x00; // Big-Endian 16-bit signed integer, MIDI key offset value
        n[12 + 32 + 1] = 0x00;
        n[12 + 32 + 2] = 0x00; // 8-bit unsigned integer, Percussion instrument key number

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class ?
                          new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")));

            n[12 + 32 + 3] = (byte) (fmRegister[p][0xb0 + c] & 0x3f); // FB & ALG
            n[12 + 32 + 4] = 0x10; // 0x00:OPN2  0x10:OPNA

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 4 : ((i == 2) ? 8 : 12));
                int ops = i * 4;
                n[i * 7 + 12 + 32 + 5] = (byte) fmRegister[p][0x30 + ops + c]; // DT & ML
                n[i * 7 + 12 + 32 + 6] = (byte) (fmRegister[p][0x40 + ops + c] & 0x7f); // TL
                n[i * 7 + 12 + 32 + 7] = (byte) fmRegister[p][0x50 + ops + c]; // KS & AR
                n[i * 7 + 12 + 32 + 8] = (byte) fmRegister[p][0x60 + ops + c]; //AM & DR
                n[i * 7 + 12 + 32 + 9] = (byte) fmRegister[p][0x70 + ops + c]; //SR
                n[i * 7 + 12 + 32 + 10] = (byte) fmRegister[p][0x80 + ops + c]; // SL&RR
                n[i * 7 + 12 + 32 + 11] = (byte) fmRegister[p][0x90 + ops + c]; // SSG
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            n[12 + 32 + 3] = (byte) ym2151Register[0x20 + ch]; // FB & ALG
            n[12 + 32 + 4] = 0x10; // 0x00:OPN2  0x10:OPNA

            for (int i = 0; i < 4; i++) {
                //int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 16 : 24));
                int ops = i * 8;
                n[i * 7 + 12 + 32 + 5] = (byte) ym2151Register[0x40 + ops + ch]; // DT & ML
                n[i * 7 + 12 + 32 + 6] = (byte) (ym2151Register[0x60 + ops + ch] & 0x7f); // TL
                n[i * 7 + 12 + 32 + 7] = (byte) ym2151Register[0x80 + ops + ch]; // KS & AR
                n[i * 7 + 12 + 32 + 8] = (byte) ym2151Register[0xa0 + ops + ch]; // AME DR
                n[i * 7 + 12 + 32 + 9] = (byte) ym2151Register[0xc0 + ops + ch]; // SR
                n[i * 7 + 12 + 32 + 10] = (byte) ym2151Register[0xe0 + ops + ch]; // SL&RR
                n[i * 7 + 12 + 32 + 11] = 0; // SSG

                //int dt2 = (byte)((ym2151Register[0xc0 + ops + ch] & 0xc0) >> 6); //DT2
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.opni"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".opni");
            }

            @Override
            public String getDescription() {
                return "OPNI File(*.opni)";
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
