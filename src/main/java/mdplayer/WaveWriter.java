package mdplayer;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.SeekOrigin;
import vavi.util.ByteUtil;


public class WaveWriter {

    private static final Logger logger = System.getLogger(WaveWriter.class.getName());

    private final Setting setting = Setting.getInstance();
    private FileStream dest = null;
    private int len = 0;

    private String lastFn;

    public void open(String filename) {
        if (!setting.getOther().getWavSwitch()) return;

        if (dest != null) close();
        filename = filename.replace(File.separator, "\\");
        String fn = Path.combine(setting.getOther().getWavPath(), Path.getFileNameWithoutExtension(filename) + ".wav");
        int i = 0;
        while (filename.equals(fn)) {
            fn = Path.combine(setting.getOther().getWavPath(), Path.getFileNameWithoutExtension(filename) + "_%d.wav".formatted(i));
        }

        fn = fn.replace("\\", File.separator);
        this.lastFn = fn;
logger.log(Level.TRACE, "wave writer: " + fn);
        if (new File(fn).exists()) new File(fn).delete();
        dest = new FileStream(fn, FileMode.Create, FileAccess.Write);

        List<Byte> des = new ArrayList<>();
        len = 0;

        // 'RIFF'
        des.add((byte) 'R');
        des.add((byte) 'I');
        des.add((byte) 'F');
        des.add((byte) 'F');
        // Size
        int fsize = len + 36;
        des.add((byte) ((fsize & 0xff) >> 0));
        des.add((byte) ((fsize & 0xff00) >> 8));
        des.add((byte) ((fsize & 0xff_0000) >> 16));
        des.add((byte) ((fsize & 0xff00_0000) >> 24));
        // 'WAVE'
        des.add((byte) 'W');
        des.add((byte) 'A');
        des.add((byte) 'V');
        des.add((byte) 'E');
        // 'fmt '
        des.add((byte) 'f');
        des.add((byte) 'm');
        des.add((byte) 't');
        des.add((byte) ' ');
        // Size(16)
        des.add((byte) 0x10);
        des.add((byte) 0);
        des.add((byte) 0);
        des.add((byte) 0);
        // Format(1)
        des.add((byte) 0x01);
        des.add((byte) 0x00);
        // Number of channels (stereo)
        des.add((byte) 0x02);
        des.add((byte) 0x00);
        // Sampling Frequency(44100Hz)
        des.add((byte) (setting.getOutputDevice().getSampleRate() >> 0));
        des.add((byte) (setting.getOutputDevice().getSampleRate() >> 8));
        des.add((byte) (setting.getOutputDevice().getSampleRate() >> 16));
        des.add((byte) (setting.getOutputDevice().getSampleRate() >> 24));
        // Average Data Percentage
        des.add((byte) 0x10);
        des.add((byte) 0xb1);
        des.add((byte) 0x02);
        des.add((byte) 0); // 10 B1 02 00
        // Block size(4)
        des.add((byte) 0x04);
        des.add((byte) 0x00);
        // Bit depth (16bit)
        des.add((byte) 0x10);
        des.add((byte) 0x00);

        // 'data'
        des.add((byte) 'd');
        des.add((byte) 'a');
        des.add((byte) 't');
        des.add((byte) 'a');
        // Size (data size)
        des.add((byte) ((len & 0xff) >> 0));
        des.add((byte) ((len & 0xff00) >> 8));
        des.add((byte) ((len & 0xff0000) >> 16));
        des.add((byte) ((len & 0xff000000) >> 24));

        // output
        dest.write(ByteUtil.toByteArray(des), 0, des.size());
    }

    public void close() {
        if (!setting.getOther().getWavSwitch()) return;
        if (dest == null) return;

        dest.close();
        dest = null;

        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(lastFn, "rw")) {
            int fsize = len + 36;
            raf.seek(4);
            raf.write(fsize & 0xff);
            raf.write((fsize >> 8) & 0xff);
            raf.write((fsize >> 16) & 0xff);
            raf.write((fsize >> 24) & 0xff);

            raf.seek(40);
            raf.write(len & 0xff);
            raf.write((len >> 8) & 0xff);
            raf.write((len >> 16) & 0xff);
            raf.write((len >> 24) & 0xff);
        } catch (java.io.IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    public void write(short[] buffer, int offset, int sampleCount) {
        if (!setting.getOther().getWavSwitch()) return;
        if (dest == null) return;

        for (int i = 0; i < sampleCount; i++) {
            dest.writeByte((byte) (buffer[offset + i] & 0xff));
            dest.writeByte((byte) ((buffer[offset + i] & 0xff00) >> 8));
        }
        len += sampleCount * 2;
    }
}
