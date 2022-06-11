import System;
import System.Collections.Generic;
import System.Linq;
import System.Threading.Tasks;
import System.Windows.Forms;

package WavMaker
{
    static class Program
    {
        /**
         * アプリケーションのメイン エントリ ポイントです。
         */
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new Form1());
        }
    }
}
