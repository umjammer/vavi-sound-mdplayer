package mdplayer.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeLibraryLocator;
import com.github.kwhat.jnativehook.NativeSystem;

import static java.lang.System.getLogger;


/**
 * Extracts jnativehook's native library out of the class path.
 * <p>
 * jnativehook's {@code DefaultLibraryLocator} resolves its own code source location as a
 * {@link File} first, which is impossible when it is loaded from a nested jar ("rsrc:lib/
 * jnativehook-x.y.z.jar" is not a hierarchical uri), so read the library as a resource instead.
 *
 * @see KeyboardHook
 */
public class NativeHookLibraryLocator implements NativeLibraryLocator {

    private static final Logger logger = getLogger(NativeHookLibraryLocator.class.getName());

    @Override
    public Iterator<File> getLibraries() {
        String libName = System.getProperty("jnativehook.lib.name", "JNativeHook");
        String arch = NativeSystem.getArchitecture().toString().toLowerCase();
        // the same "hack for OS X JRE 1.6 and earlier" as the default locator
        String libNativeName = System.mapLibraryName(libName).replaceAll("\\.jnilib$", ".dylib");
        String resource = '/' + GlobalScreen.class.getPackageName().replace('.', '/') + "/lib/" +
                NativeSystem.getFamily().toString().toLowerCase() + '/' + arch + '/' + libNativeName;

        String version = GlobalScreen.class.getPackage().getImplementationVersion();
        version = version != null ? '-' + version : "";
        // the same file name as the default locator, so both may share an already extracted library
        Path lib = Path.of(System.getProperty("jnativehook.lib.path", System.getProperty("java.io.tmpdir")))
                .resolve(libNativeName.replaceAll("^(.*)\\.(.*)$", "$1" + version + '.' + arch + ".$2"));

        if (!Files.exists(lib)) {
            try (InputStream is = GlobalScreen.class.getResourceAsStream(resource)) {
                if (is == null) {
                    throw new IllegalStateException("no native library in the class path: " + resource);
                }
                Files.copy(is, lib);
                logger.log(Level.DEBUG, "extracted library: " + lib);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return List.of(lib.toFile()).iterator();
    }
}
