package mdplayer;

import java.nio.charset.Charset;

import dotnet4j.io.File;
import dotnet4j.io.Path;
import dotnet4j.io.StreamWriter;
import mdplayer.properties.Resources;


public class Log {
    // #if DEBUG
    public static Boolean debug = true;
    // #else
//        public static Boolean debug = false;
// #endif
    public static Boolean consoleEchoBack = false;
    private static Charset sjisEnc = Charset.forName("Shift_JIS");
    public static String path = "";

    public static void forcedWrite(String msg) {
        try {
            if (path == "") {
                String fullPath = Common.settingFilePath;
                path = Path.combine(fullPath, Resources.getcntLogFilename());
                if (File.exists(path)) File.delete(path);
            }
            String timefmt = DateTime.Now.String.format(Resources.cntTimeFormat);

            try (StreamWriter writer = new StreamWriter(path, true, sjisEnc)) {
                writer.writeLine(timefmt + msg);
                if (consoleEchoBack) System.err.println(timefmt + msg);
            }
        } catch (Exception e) {
        }
    }

    public static void forcedWrite(Exception e) {
        try {
            if (path == "") {
                String fullPath = Common.settingFilePath;
                path = Path.combine(fullPath, Resources.cntLogFilename);
                if (File.exists(path)) File.delete(path);
            }
            String timefmt = DateTime.Now.String.format(Resources.cntTimeFormat);

            try (StreamWriter writer = new StreamWriter(path, true, sjisEnc)) {
                String msg = String.format(Resources.cntExceptionFormat, e.GetType().Name, e.getMessage(), e.Source, e.getStackTrace());
                Exception ie = e;
                while (ie.InnerException != null) {
                    ie = ie.InnerException;
                    msg += String.format(Resources.cntInnerExceptionFormat, ie.GetType().Name, ie.getMessage(), ie.Source, ie.getStackTrace());
                }

                writer.writeLine(timefmt + msg);
                if (consoleEchoBack) System.err.println(timefmt + msg);
            }
        } catch (Exception ex) {
        }
    }

    public static void write(String msg) {
        if (!debug) return;

        try {
            if (path == "") {
                String fullPath = Common.settingFilePath;
                path = Path.combine(fullPath, Resources.cntLogFilename);
                if (File.exists(path)) File.delete(path);
            }
            String timefmt = DateTime.Now.String.format(Resources.cntTimeFormat);

            if (consoleEchoBack) System.err.println(timefmt + msg);
            else {
                try (StreamWriter writer = new StreamWriter(path, true, sjisEnc)) {
                    writer.writeLine(timefmt + msg);
                }
            }
        } catch (Exception e) {
        }
    }

}
