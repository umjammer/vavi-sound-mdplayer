/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;

import static java.lang.System.getLogger;


/**
 * Encodings used by the MDPlayer audio decoder.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 260330 nsano initial version <br>
 */
public class MdEncoding extends AudioFormat.Encoding {

    private static final Logger logger = getLogger(MdEncoding.class.getName());

    /** Video Game Music */
    public static final MdEncoding VGM = new MdEncoding("VGM", "vgm,zgm");
    /** Mega Drive */
    public static final MdEncoding XGM = new MdEncoding("XGM", "xgm,xgm2");
    /** Mega Drive */
    public static final MdEncoding MDSDRV = new MdEncoding("MDSDRV", "mds");
    /** PC88 */
    public static final MdEncoding MUCOM88 = new MdEncoding("MUCOM88", "mub,muc");
    /** PC98 */
    public static final MdEncoding S98 = new MdEncoding("S98", "s98");
    /** PC98 */
    public static final MdEncoding PMD = new MdEncoding("PMD", "mml,m,m2,mz");
    /** PC98 */
    public static final MdEncoding FMP = new MdEncoding("FMP", "mpi,opi,mvi,ovi,mzi,ozi");
    /** MUAP98 */
    public static final MdEncoding MUAP = new MdEncoding("MUAP", "mus,o,ox,oy");
    /** X68k */
    public static final MdEncoding MXDRV = new MdEncoding("MXDRV", "mdx");
    /** X68k */
    public static final MdEncoding MNDRV = new MdEncoding("MNDRV", "mnd");
    /** X68k */
    public static final MdEncoding ZMUSIC = new MdEncoding("ZMUSIC", "zmd,zms");
    /** X1 */
    public static final MdEncoding NRTDRV = new MdEncoding("NRTDRV", "nrd");
    /** MSX */
    public static final MdEncoding MOONDRV = new MdEncoding("MOONDRV", "mdl,mdr");
    /** MSX */
    public static final MdEncoding MGSDRV = new MdEncoding("MGSDRV", "mgs");
    /** MSX */
    public static final MdEncoding NDP = new MdEncoding("NDP", "ndp");
    /** MSX */
    public static final MdEncoding MUSICA = new MdEncoding("MUSICA", "bgm,msd");
    /** ZX */
    public static final MdEncoding AY = new MdEncoding("AY", "ay");
    /** PC Engine */
    public static final MdEncoding HES = new MdEncoding("HES", "hes");
    /** Recomposer */
    public static final MdEncoding RCP = new MdEncoding("RCP", "rcp");
    /** Recomposer + PCM8 */
    public static final MdEncoding RCS = new MdEncoding("RCS", "rcs");

    private final String extensions;

    /**
     * Constructs a new encoding.
     *
     * @param name Name of the MDPlayer audio encoding.
     */
    private MdEncoding(String name, String extensions) {
        super(name);
        this.extensions = extensions;
    }

    public String getExtensions() {
        return extensions;
    }

    static final MdEncoding[] encodings = {
            VGM, XGM, MDSDRV,
            MUCOM88, S98, PMD, FMP, MUAP,
            MXDRV, MNDRV, ZMUSIC,
            NRTDRV, MOONDRV, MGSDRV, NDP, MUSICA,
            AY, HES, RCP, RCS
    };

    public static MdEncoding valueOf(String name) {
logger.log(Level.DEBUG, name);
        return Arrays.stream(encodings).filter(e -> name.equalsIgnoreCase(e.toString())).findFirst().orElseThrow();
    }
}
