package mdplayer;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;

import dotnet4j.io.File;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.StreamWriter;
import mdplayer.properties.Resources;


public class Log {
    // #if DEBUG
    public static boolean debug = true;
    // #else
//        public static boolean debug = false;
// #endif
    public static boolean consoleEchoBack = false;
    private static Charset sjisEnc = Charset.forName("Shift_JIS");
    public static String path = "";

    public static void forcedWrite(String msg) {
        try {
            if (path.isEmpty()) {
                String fullPath = Common.settingFilePath;
                path = Path.combine(fullPath, Resources.getCntLogFilename());
                if (File.exists(path)) File.delete(path);
            }
            String timefmt = String.format(Resources.getCntTimeFormat(), Instant.now());

            try (StreamWriter writer = new StreamWriter(new FileStream(path, FileMode.CreateNew), sjisEnc)) {
                writer.writeLine(timefmt + msg);
                if (consoleEchoBack) System.err.println(timefmt + msg);
            }
        } catch (Exception ignored) {
        }
    }

    public static void forcedWrite(Exception e) {
        try {
            if (path.isEmpty()) {
                String fullPath = Common.settingFilePath;
                path = Path.combine(fullPath, Resources.getCntLogFilename());
                if (File.exists(path)) File.delete(path);
            }
            String timefmt = String.format(Resources.getCntTimeFormat(), Instant.now());

            try (StreamWriter writer = new StreamWriter(new FileStream(path, FileMode.Open), sjisEnc)) {
                StringBuilder msg = new StringBuilder(String.format(Resources.getCntExceptionFormat(), e.getClass().getName(), e.getMessage(), e.getStackTrace()[0], Arrays.toString(e.getStackTrace())));
                Throwable ie = e;
                while (ie.getCause() != null) {
                    ie = ie.getCause();
                    msg.append(String.format(Resources.getcntInnerExceptionFormat(), ie.getClass().getName(), ie.getMessage(), ie.getStackTrace()[0], Arrays.toString(ie.getStackTrace())));
                }

                writer.writeLine(timefmt + msg);
                if (consoleEchoBack) System.err.println(timefmt + msg);
            }
        } catch (Exception ignored) {
        }
    }

    public static void write(String msg) {
        if (!debug) return;

        try {
            if (path.isEmpty()) {
                String fullPath = Common.settingFilePath;
                path = Path.combine(fullPath, Resources.getCntLogFilename());
                if (File.exists(path)) File.delete(path);
            }
            String timefmt = String.format(Resources.getCntTimeFormat(), Instant.now());

            if (consoleEchoBack) System.err.println(timefmt + msg);
            else {
                try (StreamWriter writer = new StreamWriter(new FileStream(path, FileMode.CreateNew), sjisEnc)) {
                    writer.writeLine(timefmt + msg);
                }
            }
        } catch (Exception ignored) {
        }
    }

}
