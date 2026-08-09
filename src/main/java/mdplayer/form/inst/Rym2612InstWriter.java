package mdplayer.form.inst;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import musicDriverInterface.MetaData;
import musicDriverInterface.MetaData.Tag;


/** RYM2612 Iconic FM Synthesizer patch file */
public class Rym2612InstWriter implements InstWriter {

    @Override
    public EnmInstFormat format() {
        return EnmInstFormat.RYM2612;
    }

    //
    //  I am using the following code for reference. Thank you!
    //
    //  Title:
    //      mucom88torym2612
    //  Author:
    //      千霧＠ぶっちぎりP(but80) 様
    //  URL:
    //      https://github.com/but80/mucom88torym2612/
    //      Github
    //        but80/mucom88torym2612
    //  License:
    //      MIT License
    //
    @Override
    public void write(Component parent, Audio audio, Class<? extends Chip> chip, int ch, int chipId) {

        List<String>[] op = new List[] {new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        int[] muls = {0, 1054, 1581, 2635, 3689, 4743, 5797, 6851, 7905, 8959, 10013, 10540, 11594, 12648, 14229, 15000};
        StringBuilder buf = new StringBuilder("<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n");
        buf.append("\n");
        int alg = 0, fb = 0, ams = 0, pms = 0;

        MetaData metaData = (audio.plugin.driverVirtual != null) ? audio.plugin.driverVirtual.metaData : null;
        String patch_Name = "MDPlayer_%d";
        if (metaData != null) {
            String pn = metaData.getFirst(Tag.Title);
            if (pn == null || !pn.isEmpty()) pn = metaData.getFirst(Tag.TitleJ);
            if (pn != null && pn.isEmpty()) {
                patch_Name = pn + "_%d";
            }
        }
        patch_Name = patch_Name.formatted(Instant.now().toEpochMilli());
        buf.append("<RYM2612Params patchName = \"{patch_Name}\" category = \"Piano\" rating = \"3\" type = \"User\" >\n");

        if (chip == Ym2612Chip.class || chip == Ym2608Chip.class || chip == Ym2203Chip.class || chip == Ym2610Chip.class) {
            int p = (ch > 2) ? 1 : 0;
            int c = (ch > 2) ? ch - 3 : ch;
            int[][] fmRegister = (chip == Ym2612Chip.class)
                    ? (int[][]) audio.plugin.chipRegister.chip(Ym2612Chip.class).getInfo(chipId).get("register")
                    : (chip == Ym2608Chip.class
                       ? (int[][]) audio.plugin.chipRegister.chip(Ym2608Chip.class).getInfo(chipId).get("register")
                       : (chip == Ym2203Chip.class
                          ? new int[][] {(int[]) audio.plugin.chipRegister.chip(Ym2203Chip.class).getInfo(chipId).get("register"), null}
                          : (int[][]) audio.plugin.chipRegister.chip(Ym2610Chip.class).getInfo(chipId).get("register")
            ));

            alg = (fmRegister[p][0xb0 + c] & 0x07) >> 0;
            fb = (fmRegister[p][0xb0 + c] & 0x38) >> 3;
            ams = (fmRegister[p][0xb4 + c] & 0x30) >> 4;
            pms = (fmRegister[p][0xb4 + c] & 0x07) >> 0;

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 8 : ((i == 2) ? 4 : 12));
                int tl = 127 - ((fmRegister[p][0x40 + ops + c] & 0x7f) >> 0);
                int vel = 0;
                int ssg = fmRegister[p][0x90 + ops + c] & 0x0f;
                ssg = ((ssg & 0x8) == 0) ? 0 : ((ssg & 0x7) + 1);
                op[i].add("  <PARAM id=\"OP%dVel\" value=\"%d.0\"/>".formatted(i + 1, vel));
                op[i].add("  <PARAM id=\"OP%dTL\" value=\"%d.0\"/>".formatted(i + 1, tl));
                op[i].add("  <PARAM id=\"OP%dSSGEG\" value=\"%d.0\"/>".formatted(i + 1, ssg));
                op[i].add("  <PARAM id=\"OP%dRS\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x50 + ops + c] & 0xc0) >> 6));
                op[i].add("  <PARAM id=\"OP%dRR\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x80 + ops + c] & 0x0f) >> 0));
                op[i].add("  <PARAM id=\"OP%dMW\" value=\"0.0\"/>".formatted(i + 1));
                op[i].add("  <PARAM id=\"OP%dMUL\" value=\"%d.0\"/>".formatted(i + 1, muls[(fmRegister[p][0x30 + ops + c] & 0x0f) >> 0]));
                op[i].add("  <PARAM id=\"OP%dFixed\" value=\"0.0\"/>".formatted(i + 1));
                int dt = (fmRegister[p][0x30 + ops + c] & 0x70) >> 4;
                dt = (dt >= 4) ? (4 - dt) : dt;
                op[i].add("  <PARAM id=\"OP%dDT\" value=\"%d.0\"/>".formatted(i + 1, dt));
                op[i].add("  <PARAM id=\"OP%dD2R\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x70 + ops + c] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dD2L\" value=\"%d.0\"/>".formatted(i + 1, 15 - ((fmRegister[p][0x80 + ops + c] & 0xf0) >> 4)));
                op[i].add("  <PARAM id=\"OP%dD1R\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x60 + ops + c] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAR\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x50 + ops + c] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAM\" value=\"%d.0\"/>".formatted(i + 1, (fmRegister[p][0x60 + ops + c] & 0x80) >> 7));
            }

        } else if (chip == Ym2151Chip.class) {
            int[] ym2151Register = (int[]) audio.plugin.chipRegister.chip(Ym2151Chip.class).getInfo(chipId).get("register");

            alg = (ym2151Register[0x20 + ch] & 0x07) >> 0;
            fb = (ym2151Register[0x20 + ch] & 0x38) >> 3;
            ams = (ym2151Register[0x38 + ch] & 0x03) >> 0;
            pms = (ym2151Register[0x38 + ch] & 0x70) >> 4;

            for (int i = 0; i < 4; i++) {
                int ops = (i == 0) ? 0 : ((i == 1) ? 16 : ((i == 2) ? 8 : 24));
                int tl = 127 - ((ym2151Register[0x60 + ops + ch] & 0x7f) >> 0);
                int vel = 0;
                op[i].add("  <PARAM id=\"OP%dVel\" value=\"%d.0\"/>".formatted(i + 1, vel));
                op[i].add("  <PARAM id=\"OP%dTL\" value=\"%d.0\"/>".formatted(i + 1, tl));
                op[i].add("  <PARAM id=\"OP%dSSGEG\" value=\"0.0\"/>".formatted(i + 1));
                op[i].add("  <PARAM id=\"OP%dRS\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0x80 + ops + ch] & 0xc0) >> 6));
                op[i].add("  <PARAM id=\"OP%dRR\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xe0 + ops + ch] & 0x0f) >> 0));
                op[i].add("  <PARAM id=\"OP%dMW\" value=\"0.0\"/>".formatted(i + 1));
                op[i].add("  <PARAM id=\"OP%dMUL\" value=\"%d.0\"/>".formatted(i + 1, muls[(ym2151Register[0x40 + ops + ch] & 0x0f) >> 0]));
                op[i].add("  <PARAM id=\"OP%dFixed\" value=\"0.0\"/>".formatted(i + 1));
                int dt = (ym2151Register[0x40 + ops + ch] & 0x70) >> 4;
                dt = (dt >= 4) ? (4 - dt) : dt;
                op[i].add("  <PARAM id=\"OP%dDT\" value=\"%d.0\"/>".formatted(i + 1, dt));
                op[i].add("  <PARAM id=\"OP%dD2R\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xc0 + ops + ch] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dD2L\" value=\"%d.0\"/>".formatted(i + 1, 15 - ((ym2151Register[0xe0 + ops + ch] & 0xf0) >> 4)));
                op[i].add("  <PARAM id=\"OP%dD1R\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xa0 + ops + ch] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAR\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0x80 + ops + ch] & 0x1f) >> 0));
                op[i].add("  <PARAM id=\"OP%dAM\" value=\"%d.0\"/>".formatted(i + 1, (ym2151Register[0xa0 + ops + ch] & 0x80) >> 7));
            }
        }

        for (int i = 0; i < op[0].size(); i++) {
            buf.append(op[3].get(i)).append("\n");
            buf.append(op[2].get(i)).append("\n");
            buf.append(op[1].get(i)).append("\n");
            buf.append(op[0].get(i)).append("\n");
        }

        buf.append("  <PARAM id=\"volume\" value=\"0.699999988079071\"/>\n"); // -0.00db
        buf.append("  <PARAM id=\"Ladder_Effect\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"Output_Filtering\" value=\"1.0\"/>\n"); // Crystal clear
        buf.append("  <PARAM id=\"Polyphony\" value=\"6.0\"/>\n");
        buf.append("  <PARAM id=\"timerA\" value=\"0.0\"/>\n"); // RETRIG RATE 1200
        buf.append("  <PARAM id=\"Spec_Mode\" value=\"2.0\"/>\n"); // 1.0:float mode  2.0:int mode
        buf.append("  <PARAM id=\"Pitchbend_Range\" value=\"2.0\"/>\n");
        buf.append("  <PARAM id=\"Legato_Retrig\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"LFO_Speed\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"LFO_Enable\" value=\"%d.0\"/>\n".formatted((pms != 0 || ams != 0) ? 1 : 0));
        buf.append("  <PARAM id=\"Feedback\" value=\"%d.0\"/>\n".formatted(fb));
        buf.append("  <PARAM id=\"FMSMW\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"FMS\" value=\"%d.0\"/>\n".formatted(pms));
        buf.append("  <PARAM id=\"DAC_Prescaler\" value=\"0.0\"/>\n");
        buf.append("  <PARAM id=\"Algorithm\" value=\"%d.0\"/>\n".formatted(alg + 1));
        buf.append("  <PARAM id=\"AMS\" value=\"%d.0\"/>\n".formatted(ams));
        buf.append("  <PARAM id=\"masterTune\"/>\n");
        buf.append("</RYM2612Params>\n");


        JFileChooser sfd = new JFileChooser();

        sfd.setSelectedFile(new File("{patch_Name}.rym2612"));
        sfd.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".rym2612");
            }

            @Override
            public String getDescription() {
                return "RYM2612 File(*.rym2612";
            }
        });
//        sfd.FilterIndex = 1;
        sfd.setDialogTitle("Save As");
//        sfd.RestoreDirectory = true;

        if (sfd.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (var sw = new PrintWriter(Files.newOutputStream(Path.of(sfd.getSelectedFile().getName())))) {
            sw.write(buf.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
