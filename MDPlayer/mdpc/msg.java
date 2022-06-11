import System;
import System.Collections.Generic;
import System.IO;
import System.Linq;
import System.Reflection;
import System.Text;
import System.Threading.Tasks;

package mdpc
{
    public static class msg
    {

        private static HashMap<string, string> dicMsg = new HashMap<string, string>();

        static msg()
        {
            Assembly myAssembly = Assembly.GetEntryAssembly();
            String path = Path.GetDirectoryName(myAssembly.Location);
            String lang = System.Globalization.CultureInfo.CurrentCulture.Name;
            String file = Path.Combine(path, "lang", string.Format("message.{0}.txt", lang));
            file = file.Replace('\\', Path.DirectorySeparatorChar).Replace('/', Path.DirectorySeparatorChar);
            string[] lines = null;
            try
            {
                if (!File.Exists(file))
                {
                    file = Path.Combine(path, "lang", "message.txt");
                    file = file.Replace('\\', Path.DirectorySeparatorChar).Replace('/', Path.DirectorySeparatorChar);
                }
                lines = File.readAllLines(file);
            }
            catch
            {

            }

            if (lines != null)
            {
                for (String line : lines)
                {
                    try
                    {
                        if (line == null) continue;
                        if (line == "") continue;
                        String str = line.Trim();
                        if (str == "") continue;
                        if (str[0] == ';') continue;
                        String code = str.substring(0, str.IndexOf("=")).Trim();
                        String msg = str.substring(str.IndexOf("=") + 1, str.length - str.IndexOf("=") - 1);
                        if (dicMsg.containsKey(code)) continue;

                        dicMsg.add(code, msg);
                    }
                    catch { }
                }
            }
        }

        public static String get(String code)
        {
            if (dicMsg.containsKey(code))
            {
                return dicMsg[code].Replace("\\r", "\r").Replace("\\n", "\n");
            }
            return "<no message>";
        }

    }
}
