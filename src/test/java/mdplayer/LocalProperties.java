/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * The sample files and driver locations {@code local.properties} carries, for tests that run
 * without the player.
 * <p>
 * The drivers that boot a real X68000/MSX/PC-98 binary (ZMC.X, MGSDRV.COM, NDP.BIN, KINROU4.COM)
 * find it relative to these system properties and fail to load at all without them, so a headless
 * test has to bind them the same way {@code TestCase} does.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-20 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class LocalProperties {

    /*@Property(name = "vavi.test.volume")*/ double volume = 0.001;
    @Property(name = "mdplayer.fmp.dir") String fmpDir;
    @Property(name = "mdplayer.fmp.pvi") String fmpPvi;
    @Property(name = "mdplayer.zms.dir") String zmsDir;
    @Property(name = "mdplayer.mgs.dir") String mgsDir;
    @Property(name = "mdplayer.ndp.dir") String ndpDir;
    @Property(name = "mdplayer.musica.dir") String musicaDir;
    @Property(name = "muap.dir.dta") String muapDirDta;
    @Property(name = "muap.dir.pcm") String muapDirPcm;

    /** where the chip roms are kept - the OPL4's wave rom, the OPNA's rhythm samples */
    @Property(name = "mdsound.pcm.path") String pcmPath;

    @Property(name = "mdplayer.variant.pcm8") int variantPcm8;
    @Property(name = "mdplayer.variant.mpcm") int variantMpcm;
    @Property(name = "mdplayer.variant.ym2151") int variantYm2151;
    @Property(name = "mdplayer.variant.ym2413") int variantYm2413;
    @Property(name = "mdplayer.variant.ymf262") int variantYmf262;
    @Property(name = "mdplayer.variant.ay8910") int variantAy8910;

    /** whether {@code local.properties} is there at all - it is not checked in */
    public static boolean exists() {
        return Files.exists(Path.of("local.properties"));
    }

    /** publishes the driver locations as system properties; does nothing without the file */
    public static void bind() throws Exception {
        if (!exists()) return;
        LocalProperties p = new LocalProperties();
        PropsEntity.Util.bind(p);
        System.setProperty("mdplayer.fmp.dir", p.fmpDir);
        System.setProperty("mdplayer.fmp.pvi", p.fmpPvi);
        System.setProperty("mdplayer.zms.dir", p.zmsDir);
        System.setProperty("mdplayer.mgs.dir", p.mgsDir);
        System.setProperty("mdplayer.ndp.dir", p.ndpDir);
        System.setProperty("mdplayer.musica.dir", p.musicaDir);
        System.setProperty("muap.dir.dta", p.muapDirDta);
        System.setProperty("muap.dir.pcm", p.muapDirPcm);
        System.setProperty("mdsound.pcm.path", p.pcmPath);
        System.setProperty("mdplayer.variant.pcm8", String.valueOf(p.variantPcm8));
        System.setProperty("mdplayer.variant.mpcm", String.valueOf(p.variantMpcm));
        System.setProperty("mdplayer.variant.ym2151", String.valueOf(p.variantYm2151));
        System.setProperty("mdplayer.variant.ym2413", String.valueOf(p.variantYm2413));
        System.setProperty("mdplayer.variant.ymf262", String.valueOf(p.variantYmf262));
        System.setProperty("mdplayer.variant.ay8910", String.valueOf(p.variantAy8910));
        System.setProperty("mdplayer.volume", "%4.2f".formatted(p.volume));
    }

    /** the sample files named in {@code local.properties}, commented out ones included */
    public static List<Path> listFiles() throws IOException {
        List<Path> paths = new ArrayList<>();
        if (!exists()) return paths;
        for (String line : Files.readAllLines(Path.of("local.properties"))) {
            if (!line.matches("^#?\\w+\\s*?=.*$")) continue;
            Path path = Path.of(line.substring(line.indexOf('=') + 1));
            if (Files.exists(path) && !Files.isDirectory(path) && !paths.contains(path)) {
                paths.add(path);
            }
        }
        return paths;
    }
}
