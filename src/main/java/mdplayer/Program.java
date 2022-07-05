
package mdplayer;

import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import mdplayer.form.sys.frmMain;


class Program {

    /**
     * アプリケーションのメイン エントリ ポイントです。
     */
    public static void main(String[] args) {
        Common.setCommandLineArgs(args);

        String fn = checkFiles();
        if (fn != null) {
            JOptionPane.showMessageDialog(null,
                    String.format("動作に必要なファイル(%s)がみつかりません。", fn),
                    "エラー",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        frmMain frm = null;
        try {
            frm = new frmMain();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    String.format("不明なエラーが発生しました。\nException Message:\n%s", e.getMessage()),
                    "エラー",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    static String checkFiles() {
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
