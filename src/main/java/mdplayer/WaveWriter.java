package mdplayer;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.io.FileAccess;
import dotnet4j.io.FileMode;
import dotnet4j.io.FileStream;
import dotnet4j.io.Path;
import dotnet4j.io.SeekOrigin;

import static dotnet4j.util.compat.CollectionUtilities.toByteArray;


public class WaveWriter {
    private final Setting setting = Setting.getInstance();
    private FileStream dest = null;
    private int len = 0;

    public void open(String filename) {
        if (!setting.getOther().getWavSwitch()) return;

        if (dest != null) close();
        String fn = Path.combine(setting.getOther().getWavPath(), Path.getFileNameWithoutExtension(filename) + ".wav");
        int i = 0;
        while (filename.equals(fn)) {
            fn = Path.combine(setting.getOther().getWavPath(), Path.getFileNameWithoutExtension(filename) + "_%d.wav".formatted(i));
        }

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
        dest.write(toByteArray(des), 0, des.size());
    }

    public void close() {
        if (!setting.getOther().getWavSwitch()) return;
        if (dest == null) return;

        dest.seek(4, SeekOrigin.Begin);
        int fsize = len + 36;
        dest.writeByte((byte) ((fsize & 0xff) >> 0));
        dest.writeByte((byte) ((fsize & 0xff00) >> 8));
        dest.writeByte((byte) ((fsize & 0xff0000) >> 16));
        dest.writeByte((byte) ((fsize & 0xff000000) >> 24));

        dest.seek(40, SeekOrigin.Begin);
        dest.writeByte((byte) ((len & 0xff) >> 0));
        dest.writeByte((byte) ((len & 0xff00) >> 8));
        dest.writeByte((byte) ((len & 0xff0000) >> 16));
        dest.writeByte((byte) ((len & 0xff000000) >> 24));

        dest.close();
        dest = null;
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
