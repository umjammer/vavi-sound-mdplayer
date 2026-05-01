package mdplayer.emu.nise68;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class FileIni {

    public boolean isopen = false;
    public String filename = "";
    public int ptr = 0;
    //public byte[] dat = null;
    public boolean isTemp = false;
    public List<Byte> memoryStream = null;
    public int datetime = ((2024 - 1980) << 25) | (1 << 21) | (1 << 16) | (12 << 11) | (12 << 5) | (12 << 0); // 2024-1-1 12:12:12
}
