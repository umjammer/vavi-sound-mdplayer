package mdplayer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class MmfControl {
    private final Object lockobj = new Object();
    private MemoryMappedFile _map;
    private byte[] mmfBuf;
    public String mmfName = "dummy";
    public int mmfSize = 1024;

    public MmfControl() {
    }

    public MmfControl(Boolean isClient, String mmfName, int mmfSize) {
        this.mmfName = mmfName;
        this.mmfSize = mmfSize;
        if (!isClient) Open(mmfName, mmfSize);
    }

    public void Open(String mmfName, int mmfSize) {
        try {
            mmfBuf = new byte[mmfSize];

            synchronized (lockobj) {
                _map = MemoryMappedFile.CreateNew(mmfName, mmfSize);
                try {
                    MemoryMappedFileSecurity permission = _map.GetAccessControl();
                    permission.AddAccessRule(
                            new AccessRule<MemoryMappedFileRights>("Everyone",
                                    MemoryMappedFileRights.FullControl, AccessControlType.Allow));
                    _map.SetAccessControl(permission);
                } catch (Exception ex) {
                    Log.write(ex.getMessage() + ex.getStackTrace());
                }
            }
        } catch (Exception ex) {
            Log.write(ex.getMessage() + ex.getStackTrace());
        }
    }

    public void close() {
        synchronized (lockobj) {
            if (_map == null) return;
            try {
                _map.close();
            } catch (Exception ex) {
                Log.write(ex.getMessage() + ex.getStackTrace());
            }
        }
    }

    public String GetMessage() {
        String msg = "";

        try {
            synchronized (lockobj) {
                try (MemoryMappedViewAccessor view = _map.CreateViewAccessor()) {
                    view.readArray(0, mmfBuf, 0, mmfBuf.length);
                    msg = new String(mmfBuf, StandardCharsets.UTF_8);
                    msg = msg.substring(0, msg.indexOf('\0'));
                    Arrays.fill(mmfBuf, 0, mmfBuf.length, (byte) 0);
                    view.WriteArray(0, mmfBuf, 0, mmfBuf.length);
                }
            }
        } catch (Exception ex) {
            Log.write(ex.getMessage() + ex.getStackTrace());
        }

        return msg;
    }

    public byte[] GetBytes() {
        try {
            synchronized (lockobj) {
                try (var map = MemoryMappedFile.OpenExisting(mmfName);
                     MemoryMappedViewAccessor view = map.CreateViewAccessor()) {
                    mmfBuf = new byte[mmfSize];
                    view.readArray(0, mmfBuf, 0, mmfBuf.length);
                }
            }
        } catch (Exception ex) {
            Log.write(ex.getMessage() + ex.getStackTrace());
        }

        return mmfBuf;
    }

    public void SendMessage(String msg) {
        try {
            byte[] ary = msg.getBytes(StandardCharsets.UTF_8);
            if (ary.length > mmfSize) throw new ArgumentOutOfRangeException();

            try (var map = MemoryMappedFile.OpenExisting(mmfName);
                 var view = map.CreateViewAccessor()) {
                view.WriteArray(0, ary, 0, ary.length);
            }
        } catch (Exception ex) {
            Log.write(ex.getMessage() + ex.getStackTrace());
        }
    }

}
