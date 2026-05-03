package mdplayer.driver.zms;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import mdplayer.emu.nise68.FileMng;
import mdplayer.emu.nise68.Nise68;
import vavi.util.compat.Tuple;


public class UnZDF {

    /** lzz.r location */
    String dir;

    public List<Tuple<String, Long>> getFileList(String arcFile, String v, Charset charset) {
        FileMng fileMng = unpack(arcFile, charset);
        if (fileMng == null) return null;

        List<Tuple<String, Long>> res = new ArrayList<>();
        for (var ele : fileMng.vDrive.entrySet()) {
            if (ele.getValue().name.equalsIgnoreCase("LZZ.R")) continue;
            if (ele.getValue().name.equalsIgnoreCase(Path.of(arcFile).getFileName().toString())) continue;

            res.add(new Tuple<>(ele.getValue().name, (long) ele.getValue().body.length));
        }

        return res;
    }

    public byte[] getFileByte(String arcFile, String dstFile, Charset charset) {
        FileMng fileMng = unpack(arcFile, charset);
        if (fileMng == null) return null;

        for (var ele : fileMng.vDrive.entrySet()) {
            if (!ele.getValue().name.equalsIgnoreCase(Path.of(dstFile).getFileName().toString())) continue;
            return ele.getValue().body;
        }
        return null;
    }

    public FileMng unpack(String arcFile, Charset charset) {
        List<Tuple<String, Long>> res = new ArrayList<>();
        Path lzz = Path.of(dir, "lzz.r");
        if (!Files.exists(lzz)) return null;

        String dn = Path.of(arcFile).getParent().toString();
        FileMng fileMng = new FileMng(dn, "C:");
        fileMng.setVFile(lzz.toString());
        lzz = lzz.getFileName();

        Nise68 nise68;
        nise68 = new Nise68();
        nise68.init(null, false, fileMng, charset);

        int rc = nise68.loadRun(lzz.toString(), "-E " + Path.of(arcFile).getFileName(), 0x0003_3c00,
                true, true, true,
                100_000_000, 0
        );
        if (rc != 0) return null;

        return fileMng;
    }
}
