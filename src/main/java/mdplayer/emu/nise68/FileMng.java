package mdplayer.emu.nise68;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;

import dotnet4j.io.File;
import dotnet4j.io.Path;

import static java.lang.System.getLogger;


public class FileMng {

    private static final Logger logger = getLogger(FileMng.class.getName());

    public final String VCurrentPath;
    private final String pDir;
    private final String vDir;
    //private String crntDir;

    public final Map<String, vFileInfo> vDrive = new HashMap<>();

    /**
     * Constructor
     *
     * @param physicalPath physical Path
     * @param virtualPath  virtual Path
     */
    public FileMng(String physicalPath, String virtualPath /* = "C:" */) {
        String p = physicalPath;
        String v = virtualPath.toUpperCase();
        if (p.charAt(p.length() - 1) == java.io.File.separatorChar) p = p.substring(0, p.length() - 1);
        if (v.charAt(v.length() - 1) == java.io.File.separatorChar) v = v.substring(0, v.length() - 1);

        this.pDir = p; // .split(java.io.File.separatorChar);
        this.vDir = v; // .split(java.io.File.separatorChar);
        this.VCurrentPath = v;
        vDrive.clear();
    }

    public boolean existsFile(String vFile) {
        // Check if there are files in the virtual drive
        String vFull = Path.combine(VCurrentPath, vFile).toUpperCase();
        if (vDrive.containsKey(vFull)) return true;

        try {
            // If not present on the virtual drive, check the physical drive
            String pFull = convertPhysicalFileName(vFull);
            return File.exists(pFull);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create a file on the virtual drive
     *
     * @param vFilename Filename for Virtual Drive
     * @param body      File body
     */
    public void setVFile(String vFilename, byte[] body) {
        String vFull = Path.combine(VCurrentPath, vFilename).toUpperCase();
        if (vDrive.containsKey(vFull)) {
            vDrive.get(vFull).body = body;
        } else {
            vFileInfo fileInfo = new vFileInfo();
            fileInfo.name = Path.getFileName(vFull);
            fileInfo.body = body;
            vDrive.put(vFull, fileInfo);
        }
    }

    /**
     * Create a file on the virtual drive
     *
     * @param pFilename Read a file from the physical drive and set it
     *                 as the current file on the virtual drive
     */
    public void setVFile(String pFilename) {
        byte[] body = null;
        try {
            if (File.exists(pFilename)) body = File.readAllBytes(pFilename);
            else body = null;
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        setVFile(Path.getFileName(pFilename), body);
    }

    /**
     * Read entire files from virtual drive
     * If the file is not present on the virtual drive, it is read from the physical drive.
     * This creates a file on the virtual drive.
     *
     * @param vFilename
     * @returns
     */
    public byte[] vReadAllBytes(String vFilename) {
        // Check if there are files in the virtual drive
        String vFull = Path.combine(VCurrentPath, vFilename).toUpperCase();
        if (vDrive.containsKey(vFull)) return vDrive.get(vFull).body;

        try {
            // If not present on the virtual drive, check the physical drive
            String pFull = convertPhysicalFileName(vFull);
            byte[] body;
            try {
                body = File.readAllBytes(pFull.replace("\\", java.io.File.separator));
            } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage(), e);
                body = null;
            }
            setVFile(vFilename, body);
            return body;
        } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    public String vGetFullFilename(String vFilename) {
        String vFull = Path.combine(VCurrentPath, vFilename).toUpperCase();
        return vFull;
    }

    private String convertPhysicalFileName(String vFull) {
        String vPath = Path.getDirectoryName(vFull);
        if (vPath.indexOf(vDir) != 0) {
            if (!vPath.equals("\\")) {
                throw new IndexOutOfBoundsException("Referencing an out of range path");
            }
        }
        String pFull;
        if (!vPath.equals("\\")) {
            pFull = Path.combine(vPath.replace(vDir, pDir), Path.getFileName(vFull));
        } else {
            pFull = Path.combine(pDir, Path.getFileName(vFull));
        }

        return pFull;
    }

    public static class vFileInfo {

        public String name;
        public byte[] body;
    }
}
