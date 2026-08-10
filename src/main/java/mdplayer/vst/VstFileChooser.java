/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.vst;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.filechooser.FileFilter;


/**
 * Where VST plug-ins are kept, and what they look like, on the machine this is running on.
 * <p>
 * The original only ever offered {@code *.dll}, which is the answer on exactly one platform: a
 * plug-in is a {@code .vst} bundle - a directory - on macOS and a {@code .so} on Linux, and none
 * of them live in the same place.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-08 nsano initial version <br>
 */
public final class VstFileChooser {

    private VstFileChooser() {
    }

    private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

    private static final boolean IS_MAC = OS.contains("mac") || OS.contains("darwin");
    private static final boolean IS_WINDOWS = OS.contains("windows");

    /** the extension a plug-in has here */
    public static String extension() {
        if (IS_MAC) return ".vst";
        if (IS_WINDOWS) return ".dll";
        return ".so";
    }

    /** accepts plug-ins, and directories so the chooser can still be navigated */
    public static FileFilter filter() {
        return new FileFilter() {
            @Override public boolean accept(File f) {
                String name = f.getName().toLowerCase(Locale.ROOT);
                if (f.isDirectory()) {
                    // a macOS bundle is a directory that is chosen, not entered
                    return !IS_MAC || !name.endsWith(".vst3");
                }
                return name.endsWith(extension());
            }

            @Override public String getDescription() {
                return "VST plugin (*" + extension() + ")";
            }
        };
    }

    /**
     * The first place plug-ins are installed on this machine that actually exists.
     *
     * @return null when none of them do, leaving the chooser wherever it would have opened
     */
    public static File defaultDirectory() {
        for (String path : searchPaths()) {
            File directory = new File(path);
            if (directory.isDirectory()) return directory;
        }
        return null;
    }

    /** every place a plug-in is conventionally installed here, most specific first */
    public static List<String> searchPaths() {
        String home = System.getProperty("user.home");
        List<String> paths = new ArrayList<>();
        if (IS_MAC) {
            paths.add(home + "/Library/Audio/Plug-Ins/VST");
            paths.add("/Library/Audio/Plug-Ins/VST");
        } else if (IS_WINDOWS) {
            String programFiles = System.getenv("ProgramFiles");
            if (programFiles != null) {
                paths.add(programFiles + "\\VstPlugins");
                paths.add(programFiles + "\\Steinberg\\VstPlugins");
                paths.add(programFiles + "\\Common Files\\VST2");
            }
        } else {
            paths.add(home + "/.vst");
            paths.add("/usr/local/lib/vst");
            paths.add("/usr/lib/vst");
        }
        return paths;
    }
}
