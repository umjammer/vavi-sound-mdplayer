package mdplayer;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class MmfControl {
    private final Object lockObj = new Object();
    private MappedByteBuffer _map;
    private byte[] mmfBuf;
    public String mmfName = "dummy";
    public int mmfSize = 1024;

    public MmfControl() {
    }

    public MmfControl(boolean isClient, String mmfName, int mmfSize) {
        this.mmfName = mmfName;
        this.mmfSize = mmfSize;
        if (!isClient) open(mmfName, mmfSize);
    }

    public void open(String mmfName, int mmfSize) {
        try {
            mmfBuf = new byte[mmfSize];

            synchronized (lockObj) {
                _map = ((FileChannel) Files.newByteChannel(Paths.get(mmfName))).map(FileChannel.MapMode.READ_ONLY, 0, mmfSize);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        synchronized (lockObj) {
            if (_map == null) return;
            _map = null;
        }
    }

    public String getMessage() {
        String msg = "";

        try {
            synchronized (lockObj) {
                _map.get(mmfBuf, 0, mmfBuf.length);
                msg = new String(mmfBuf, StandardCharsets.UTF_8);
                msg = msg.substring(0, msg.indexOf('\0'));
                Arrays.fill(mmfBuf, 0, mmfBuf.length, (byte) 0);
                _map.put(mmfBuf, 0, mmfBuf.length);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return msg;
    }

    public byte[] getBytes() {
        try {
            synchronized (lockObj) {
                MappedByteBuffer map = ((FileChannel) Files.newByteChannel(Paths.get(mmfName))).map(FileChannel.MapMode.READ_ONLY, 0, mmfSize);
                    mmfBuf = new byte[mmfSize];
                    map.get(mmfBuf, 0, mmfBuf.length);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mmfBuf;
    }

    public void sendMessage(String msg) {
        try {
            byte[] ary = msg.getBytes(StandardCharsets.UTF_8);
            if (ary.length > mmfSize) throw new IndexOutOfBoundsException();

            MappedByteBuffer map = ((FileChannel) Files.newByteChannel(Paths.get(mmfName))).map(FileChannel.MapMode.READ_ONLY, 0, mmfSize);
            map.put(ary, 0, ary.length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
