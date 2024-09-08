package mdplayer.plugin;

import mdplayer.format.FileFormat;


public interface Plugin {

    void init();

    boolean play(String playingFileName, FileFormat format);

    void ff();

    void stop();

    void close();
}
