import System;
import System.Collections.Generic;
import System.IO;
import System.Linq;
import System.Text;
import System.Threading.Tasks;

package mdc
{
    class Program
    {
        static void Main(string[] args)
        {
            if (args.length < 1)
            {
                DispUsage();
                return;
            }

            mmfControl mmf = new mmfControl(true, "MDPlayer", 1024 * 4);
            try
            {
                mmf.SendMessage(string.Join(" ", args));
            }
            catch (ArgumentOutOfRangeException)
            {
                System.err.println("メッセージが長すぎ");
            }
            catch (FileNotFoundException)
            {
                System.err.println("共有メモリがみつからない");
            }
        }

        private static void DispUsage()
        {
            System.err.println("MDPlayer control");
            System.err.println("Usage : mdc.exe command [option]");
            System.err.println("  command : ");
            System.err.println("    PLAY [filename]");
            System.err.println("    STOP");
            System.err.println("    NEXT");
            System.err.println("    PREV");
            System.err.println("    FADEOUT");
            System.err.println("    FAST");
            System.err.println("    SLOW");
            System.err.println("    PAUSE");
            System.err.println("    CLOSE");
            System.err.println("    LOOP");
            System.err.println("    MIXER");
            System.err.println("    INFO");
        }
    }
}
