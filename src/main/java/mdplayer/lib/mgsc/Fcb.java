/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.lib.mgsc;

/**
 * An MSX-DOS file control block, as {@link BdosEmulator} sees it in the guest's memory.
 * <p>
 * Ported from the {@code FCB} class of <a href="https://github.com/digital-sound-antiques/mgsc">mgsc</a>.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-30 nsano initial version <br>
 */
class Fcb {

    /** the block the guest hands us, and the one we hand back */
    static final int SIZE = 37;

    int drive;
    /** 8 characters, space padded */
    String name;
    /** 3 characters, space padded */
    String extension;
    int currentBlock;
    int reserved1;
    int recordSize;
    int reserved2;
    int fileSize;
    int date;
    int time;
    int deviceId;
    int directoryLocation;
    int firstCluster;
    int lastCluster;
    int relativePosition;
    int currentRecord;
    int randomRecord;

    /** {@code "TEMP.MUS"} - the name the guest asked for, with the padding taken off */
    String getFilename() {
        String n = name.indexOf(' ') < 0 ? name : name.substring(0, name.indexOf(' '));
        String e = extension.indexOf(' ') < 0 ? extension : extension.substring(0, extension.indexOf(' '));
        return e.isEmpty() ? n : n + "." + e;
    }

    void set(byte[] data) {
        drive = data[0] & 0xff;
        name = new String(data, 1, 8, java.nio.charset.StandardCharsets.US_ASCII);
        extension = new String(data, 9, 3, java.nio.charset.StandardCharsets.US_ASCII);
        currentBlock = data[12] & 0xff;
        reserved1 = data[13] & 0xff;
        recordSize = data[14] & 0xff;
        reserved2 = data[15] & 0xff;
        fileSize = (data[16] & 0xff) | ((data[17] & 0xff) << 8) | ((data[18] & 0xff) << 16) | ((data[19] & 0xff) << 24);
        date = (data[20] & 0xff) | ((data[21] & 0xff) << 8);
        time = (data[22] & 0xff) | ((data[23] & 0xff) << 8);
        deviceId = data[24] & 0xff;
        directoryLocation = data[25] & 0xff;
        firstCluster = (data[26] & 0xff) | ((data[27] & 0xff) << 8);
        lastCluster = (data[28] & 0xff) | ((data[29] & 0xff) << 8);
        relativePosition = (data[30] & 0xff) | ((data[31] & 0xff) << 8);
        currentRecord = data[32] & 0xff;
        randomRecord = (data[33] & 0xff) | ((data[34] & 0xff) << 8) | ((data[35] & 0xff) << 16);
    }

    byte[] get() {
        byte[] data = new byte[SIZE];
        data[0] = (byte) drive;
        for (int i = 0; i < 8; i++) data[1 + i] = (byte) name.charAt(i);
        for (int i = 0; i < 3; i++) data[9 + i] = (byte) extension.charAt(i);
        data[12] = (byte) currentBlock;
        data[13] = (byte) reserved1;
        data[14] = (byte) recordSize;
        data[15] = (byte) reserved2;
        data[16] = (byte) fileSize;
        data[17] = (byte) (fileSize >> 8);
        data[18] = (byte) (fileSize >> 16);
        data[19] = (byte) (fileSize >> 24);
        data[20] = (byte) date;
        data[21] = (byte) (date >> 8);
        data[22] = (byte) time;
        data[23] = (byte) (time >> 8);
        data[24] = (byte) deviceId;
        data[25] = (byte) directoryLocation;
        data[26] = (byte) firstCluster;
        data[27] = (byte) (firstCluster >> 8);
        data[28] = (byte) lastCluster;
        data[29] = (byte) (lastCluster >> 8);
        data[30] = (byte) relativePosition;
        data[31] = (byte) (relativePosition >> 8);
        data[32] = (byte) currentRecord;
        data[33] = (byte) randomRecord;
        data[34] = (byte) (randomRecord >> 8);
        data[35] = (byte) (randomRecord >> 16);
        return data;
    }

    @Override
    public String toString() {
        return "FCB[%s, drive: %02x, size: %d, record: %d]".formatted(getFilename(), drive, fileSize, randomRecord);
    }
}
