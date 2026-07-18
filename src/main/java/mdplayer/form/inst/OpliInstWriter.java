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
import mdplayer.chips.Ym3812Chip;
import mdplayer.chips.YmF262Chip;
import mdplayer.chips.YmF278BChip;


/** VGM Music Maker .opli instrument file; the OPL family uses this */
public class OpliInstWriter implements InstWriter {

    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {
        if (chip != Ym3812Chip.class && chip != YmF262Chip.class && chip != YmF278BChip.class) return;

        int[][] reg;
        if (chip == YmF262Chip.class)
            reg = (int[][]) audio.plugin.chipRegister.chip(YmF262Chip.class).getInfo(chipId).get("register");
        else if (chip == YmF278BChip.class)
            reg = (int[][]) audio.plugin.chipRegister.chip(YmF278BChip.class).getInfo(chipId).get("register");
        else {
            int[] r = (int[]) audio.plugin.chipRegister.chip(Ym3812Chip.class).getInfo(chipId).get("register");
            reg = new int[1][];
            reg[0] = r;
        }

        byte[] n = new byte[76];
        Arrays.fill(n, 0, n.length, (byte) 0);
        byte[] data = "WOPL3-INST".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 0, 10);
        n[10] = 0x00;
        n[11] = 0x02; // Version 16bit-Integer LE
        n[12] = 0x00;
        n[13] = 0x00; // 0 - melodic, or 1 - percussion

        data = "MDPlayer".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, n, 14, 8);
        n[14 + 32 + 0] = 0x00; // (mstr)Big-Endian 16-bit signed integer, MIDI key offset value
        n[14 + 32 + 1] = 0x00;
        n[14 + 32 + 2] = 0x00; // (sec)Big-Endian 16-bit signed integer, MIDI key offset value
        n[14 + 32 + 3] = 0x00;
        n[14 + 32 + 4] = 0x00; // 8-bit signed integer, MIDI Velocity offset
        n[14 + 32 + 5] = 0x00; // 8-bit signed integer, Second voice detune
        n[14 + 32 + 6] = 0x00; // 8-bit unsigned integer, Percussion instrument key number

        int[] op = {0, 0, 0, 0};
        boolean isOP4 = false;
        int c = ch;
        if (ch < 6) {
            c -= ch % 2;
            op[0] = c / 2;
            op[1] = op[0] + 3;
            op[2] = op[0] + 6;
            op[3] = op[0] + 9;
            isOP4 = (chip == Ym3812Chip.class) ? false : ((reg[1][0x04] & (0x1 << c)) != 0);
            if (!isOP4 && ch % 2 != 0) {
                c = ch;
                op[0] = op[2];
                op[1] = op[3];
            }
        } else if (ch < 9) {
            op[0] = ch + 6;
            op[1] = op[0] + 3;
            isOP4 = false;
        } else if (ch < 15) {
            c -= (ch - 9) % 2;
            op[0] = (c - 9) / 2 + 18;
            op[1] = op[0] + 3;
            op[2] = op[0] + 6;
            op[3] = op[0] + 9;
            isOP4 = (reg[1][0x04] & (0x1 << ((c - 9) + 3))) != 0;
            if (!isOP4 && (ch - 9) % 2 != 0) {
                c = ch;
                op[0] = op[2];
                op[1] = op[3];
            }
        } else if (ch < 18) {
            op[0] = ch + 15;
            op[1] = op[0] + 3;
            isOP4 = false;
        }

        n[14 + 32 + 7] = (byte) (isOP4 ? 1 : 0);

        int p = c / 9;
        //int adr = c % 9;
        int[] chTbl = {0, 3, 1, 4, 2, 5, 6, 7, 8};
        //adr = chTbl[adr];

        for (int i = 0; i < 4; i++) {
            op[i] -= (op[i] / 18) * 18;
            op[i] = (op[i] % 6) + 8 * (op[i] / 6);
        }

        // OPLI has op1<->op2 and op3<->op4 reversed (?)
        for (int i = 0; i < 2; i++) {
            int s = op[i * 2];
            op[i * 2] = op[i * 2 + 1];
            op[i * 2 + 1] = s;
        }

        if (!isOP4) {
            n[14 + 32 + 8] = (byte) (reg[p][0xc0 + chTbl[c % 9]]); // 8-bit unsigned integer, Feedback / Connection
            n[14 + 32 + 9] = 0x00;
        } else {
            n[14 + 32 + 8] = (byte) (reg[p][0xc0 + chTbl[c % 9]]); // 8-bit unsigned integer, Feedback / Connection
            n[14 + 32 + 9] = (byte) (reg[p][0xc0 + chTbl[(c + 1) % 9]]); // 8-bit unsigned integer, Feedback / Connection
        }

        for (int i = 0; i < 4; i++) {
            if (isOP4 || i < 2) {
                n[14 + 32 + 10 + i * 5] = (byte) reg[p][0x20 + op[i]]; // AM/Vib/Env/Ksr/FMult characteristics
                n[14 + 32 + 11 + i * 5] = (byte) reg[p][0x40 + op[i]]; // Key Scale Level / Total level register data
                n[14 + 32 + 12 + i * 5] = (byte) reg[p][0x60 + op[i]]; // Attack / Decay
                n[14 + 32 + 13 + i * 5] = (byte) reg[p][0x80 + op[i]]; // Sustain and Release register data
                n[14 + 32 + 14 + i * 5] = (byte) reg[p][0xe0 + op[i]]; // WS
            } else {
                n[14 + 32 + 10 + i * 5] = 0x00; // AM/Vib/Env/Ksr/FMult characteristics
                n[14 + 32 + 11 + i * 5] = 0x00; // Key Scale Level / Total level register data
                n[14 + 32 + 12 + i * 5] = 0x00; // Attack / Decay
                n[14 + 32 + 13 + i * 5] = 0x00; // Sustain and Release register data
                n[14 + 32 + 14 + i * 5] = 0x00; // WS
            }
        }

        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("Tone file.opli"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".opli");
            }

            @Override
            public String getDescription() {
                return "OPLI File(*.opli)";
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
