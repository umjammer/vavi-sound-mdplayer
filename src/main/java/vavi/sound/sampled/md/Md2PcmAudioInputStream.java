/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.md;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import mdplayer.plugin.Plugin;
import vavi.io.OutputEngine;
import vavi.io.OutputEngineInputStream;

import static java.lang.System.getLogger;


/**
 * Converts a MDPlayer music BitStream into a PCM 16bits/sample audio stream.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 260330 nsano initial version <br>
 */
class Md2PcmAudioInputStream extends AudioInputStream {

    private static final Logger logger = getLogger(Md2PcmAudioInputStream.class.getName());

    /**
     * Constructor.
     *
     * @param sourceFormat the source format of this stream's audio data.
     * @param format the target format of this stream's audio data.
     * @param length the length in sample frames of the data in this stream.
     */
    public Md2PcmAudioInputStream(AudioFormat sourceFormat, AudioFormat format, long length, Map<String, Object> props) throws IOException {
        super(new OutputEngineInputStream(new MdOutputEngine((Plugin) sourceFormat.getProperty("md"), props)), format, length);
    }

    /** */
    private static class MdOutputEngine implements OutputEngine {

        /** target */
        private OutputStream out;

        final Plugin plugin;

        /** */
        public MdOutputEngine(Plugin plugin, Map<String, Object> props) throws IOException {
logger.log(Level.DEBUG,"plugin: " + plugin.getClass().getSimpleName());
            this.plugin = plugin;
            plugin.prepare();
        }

        @Override
        public void initialize(OutputStream out) throws IOException {
            if (this.out != null) {
                throw new IOException("Already initialized");
            } else {
                this.out = out;
            }
        }

        @Override
        public void execute() throws IOException {
            if (out == null) {
                throw new IOException("Not yet initialized");
            } else {
                short[] buffer = new short[4];
                int r;
                try {
                    r = plugin.getDriver().render(buffer, 0, buffer.length);
                } catch (Exception e) {
logger.log(Level.ERROR, e.getMessage(), e);
                    r = -1;
                }
                if (r != -1) {
                    ByteBuffer bb = ByteBuffer.allocate(r * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
                    ShortBuffer sb = bb.asShortBuffer();
                    sb.put(buffer, 0, r);
                    sb.rewind();
                    out.write(bb.array(), 0, r * Short.BYTES);
                } else {
                    out.close();
                }
            }
        }

        @Override
        public void finish() throws IOException {
        }
    }
}
