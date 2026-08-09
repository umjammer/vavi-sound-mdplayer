
package mdplayer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.JOptionPane;

import mdplayer.form.sys.FormMain;

import static java.lang.System.getLogger;


public class Program {

    private static final Logger logger = getLogger(Program.class.getName());

    /**
     * The main entry point for the application.
     */
    public static void main(String[] args) {
        Common.setCommandLineArgs(args);

        String fn = checkFiles();
        if (fn != null) {
            JOptionPane.showMessageDialog(null,
                    "The file (%s) required for operation cannot be found.".formatted(fn),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        FormMain frm = null;
        try {
            frm = new FormMain();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                    "An unknown error has occurred.:\n%s".formatted(e.getMessage()),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String checkFiles() {
//        List<String> chkFn = Arrays.asList("MDSound.dll", "NAudio.dll", "RealChipCtlWrap.dll", "scci.dll", "c86ctl.dll");
////        chkFn.addAll(Arrays.asList(VstMng.chkFn));
//
//        for (String fn : chkFn) {
//            if (!File.exists(Path.combine(Path.getDirectoryName(System.getProperty("user.dir")), fn)))
//                return fn;
//        }

        return null;
    }
}
