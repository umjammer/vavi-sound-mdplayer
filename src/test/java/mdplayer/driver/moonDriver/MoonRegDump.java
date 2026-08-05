package mdplayer.driver.moonDriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import musicDriverInterface.ChipAction;
import musicDriverInterface.ChipDatum;
import musicDriverInterface.IDriver;
import musicDriverInterface.MmlDatum;
import vavi.util.compat.Tuple;


/**
 * Dumps the OPL4 register writes the MoonDriver core makes for an .MDR, one per line:
 * {@code sample<TAB>port<TAB>addr<TAB>data}, so they can be diffed against the same song's vgm.
 * <p>
 * A vgm recorded off the real machine is a register log with sample timestamps, so decoding its
 * 0xD0 commands the same way turns "does it sound right" into a text diff - both what is written
 * and when. The driver ticks once per sample here, its 60Hz interrupt every 735th, so the count
 * is in samples.
 * <p>
 * usage: MoonRegDump &lt;mdr&gt; &lt;out&gt; [samples]
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-01 nsano initial version <br>
 */
public class MoonRegDump {

    public static void main(String[] args) throws Exception {
        Path mdr = Path.of(args[0]);
        int samples = args.length > 2 ? Integer.parseInt(args[2]) : 44100 * 30;

        byte[] data = Files.readAllBytes(mdr);
        List<MmlDatum> buf = new ArrayList<>();
        for (byte b : data) buf.add(new MmlDatum(b & 0xff));

        PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(args[1])));
        int[] sample = new int[1];

        ChipAction ca = new ChipAction() {
            @Override public String getChipName() { return "YMF278B"; }
            @Override public void waitSend(long t1, int t2) {}
            @Override public void writePCMData(byte[] d, int s, int e) {
                out.printf("%d\tpcm\t%06x\t%06x%n", sample[0], s, e);
            }
            @Override public void writeRegister(ChipDatum cd) {
                out.printf("%d\t%d\t%02x\t%02x%n", sample[0], cd.port, cd.address & 0xff, cd.data & 0xff);
            }
        };

        IDriver driver = IDriver.factory("moonDriver.driver.Driver");
        driver.init(List.of(ca), buf.toArray(MmlDatum[]::new), a -> read(mdr.getParent().resolve(a)),
                mdr.toAbsolutePath().toString(), (double) 44100, 0);
        driver.startRendering(44100, new Tuple<>("YMF278B", 33868800));
        driver.startMusic(0);

        for (sample[0] = 0; sample[0] < samples; sample[0]++) {
            driver.render();
            if (driver.getStatus() < 1) {
                System.err.println("stopped at sample " + sample[0]);
                break;
            }
        }
        out.flush();
        out.close();
        System.exit(0);
    }

    static InputStream read(Path p) {
        Path path = mdplayer.emu.common.Utils.fileExistsIgnoreCase(p);
        System.err.println("extend file: " + p + " -> " + path);
        try {
            return path != null ? Files.newInputStream(path) : null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
