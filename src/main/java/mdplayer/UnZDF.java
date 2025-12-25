package mdplayer;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.util.compat.Tuple;
import mdplayer.driver.zms.nise68.FileMng;
import mdplayer.driver.zms.nise68.Nise68;


public class UnZDF {

    public List<Tuple<String, Long>> getFileList(String arcFile, String v) {
        FileMng fileMng = unpack(arcFile);
        if (fileMng == null) return null;

        List<Tuple<String, Long>> res = new ArrayList<>();
        for (var ele : fileMng.vDrive.entrySet()) {
            if (ele.getValue().name.equalsIgnoreCase("LZZ.R")) continue;
            if (ele.getValue().name.equalsIgnoreCase(Path.getFileName(arcFile))) continue;

            res.add(new Tuple<String, Long>(ele.getValue().name, (long) ele.getValue().body.length));
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
        String crntDir = Path.getDirectoryName(System.getProperty("user.dir"));
        String lzz = Path.combine(crntDir, "lzz.r");
        if (!File.exists(lzz)) return null;

        String dn = Path.getDirectoryName(arcFile);
        FileMng fileMng = new FileMng(dn, "C:");
        fileMng.setVFile(lzz);
        lzz = Path.getFileName(lzz);

        Nise68 nise68;
        nise68 = new Nise68();
        nise68.init(null, false, fileMng);

        int rc;
        if ((rc = nise68.loadRun(lzz, "-E " + Path.getFileName(arcFile), 0x0003_3c00,
                true, true, true,
                100_000_000, 0
        )) != 0) return null;

        return fileMng;
    }
}
