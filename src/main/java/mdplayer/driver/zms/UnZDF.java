package mdplayer.driver.zms;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.emu.nise68.FileMng;
import mdplayer.emu.nise68.Nise68;


public class UnZDF {

    /** lzz.r location */
    String dir;

    public List<Tuple<String, Long>> getFileList(String arcFile, String v) {
        FileMng fileMng = unpack(arcFile);
        if (fileMng == null) return null;

        List<Tuple<String, Long>> res = new ArrayList<>();
        for (var ele : fileMng.vDrive.entrySet()) {
            if (ele.getValue().name.equalsIgnoreCase("LZZ.R")) continue;
            if (ele.getValue().name.equalsIgnoreCase(Path.getFileName(arcFile))) continue;

            res.add(new Tuple<>(ele.getValue().name, (long) ele.getValue().body.length));
        }

        return res;
    }

    public byte[] getFileByte(String arcFile, String dstFile) {
        FileMng fileMng = unpack(arcFile);
        if (fileMng == null) return null;

        for (var ele : fileMng.vDrive.entrySet()) {
            if (!ele.getValue().name.equalsIgnoreCase(Path.getFileName(dstFile))) continue;
            return ele.getValue().body;
        }
        return null;
    }

    public FileMng unpack(String arcFile) {
        List<Tuple<String, Long>> res = new ArrayList<>();
        String lzz = Path.combine(dir, "lzz.r");
        if (!File.exists(lzz)) return null;

        String dn = Path.getDirectoryName(arcFile);
        FileMng fileMng = new FileMng(dn, "C:");
        fileMng.setVFile(lzz);
        lzz = Path.getFileName(lzz);

        Nise68 nise68;
        nise68 = new Nise68();
        nise68.init(null, false, fileMng);

        int rc = nise68.loadRun(lzz, "-E " + Path.getFileName(arcFile), 0x0003_3c00,
                true, true, true,
                100_000_000, 0
        );
        if (rc != 0) return null;

        return fileMng;
    }
}
