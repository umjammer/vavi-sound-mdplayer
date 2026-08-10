/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.emu.nise68;


/**
 * IMemory.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-07 nsano initial version <br>
 */
public interface IMemory {

    byte readByte(int address);

    int readInt(int address);

    byte[] getMemory();
}
