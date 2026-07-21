/*
 * https://github.com/yosshin4004/portable_mdx
 *
 * Copyright (C) 2018 Yosshin.
 *
 * Apache License Version 2.0
 */

package mdplayer.driver.mxdrv;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;
import javax.swing.JPanel;

import vavi.util.event.GenericEvent;


/**
 * Visualizer.
 *
 * @author <a href="mailto:umjammer@gmail.com">Yosshin</a>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-05-04 nsano initial version <br>
 */
public class Visualizer extends JPanel {

    private Map<String, Integer>[] fmChannels;
    private Map<String, Integer>[] pcmChannels;

    /** Copy of the value written to the OPM register */
    private int[] m_opmRegs;
    /** Has there been an update to the OPM register? */
    private final boolean[] m_opmRegsUpdated = new boolean[0x100];
    /** KeyOn state */
    private final boolean[] m_keyOnFlagsForFm = new boolean[8];
    private final boolean[] m_logicalSumOfKeyOnFlagsForFm = new boolean[8];
    private final boolean[] m_logicalSumOfKeyOnFlagsForPcm = new boolean[8];

    /** */
    public void update(GenericEvent event) {
        if (event.getName().equals("mxdrv")) {
            m_opmRegs = (int[]) event.getArguments()[0];
            fmChannels = (Map<String, Integer>[]) event.getArguments()[1];
            pcmChannels = (Map<String, Integer>[]) event.getArguments()[2];
            if (m_opmRegs == null || fmChannels == null || pcmChannels == null) return;
            repaint();
        }
    }

    private boolean getOpmReg(
            int regIndex,
            int[] regVal,
            boolean[] updated
    ){
        if (m_opmRegs != null) regVal[0] = m_opmRegs[regIndex];
        if (updated != null) {
		updated[0] = m_opmRegsUpdated[regIndex];
            m_opmRegsUpdated[regIndex] = false;
        }
        return true;
    }

    private  boolean getFmKeyOn(
            int channelIndex,
            boolean[] currentKeyOn,
            boolean[] logicalSumOfKeyOn
    ){
        if (channelIndex > 7) return false;
        if (currentKeyOn != null) currentKeyOn[0] = m_keyOnFlagsForFm[channelIndex];
        if (logicalSumOfKeyOn != null) {
		logicalSumOfKeyOn[0] = m_logicalSumOfKeyOnFlagsForFm[channelIndex];
            m_logicalSumOfKeyOnFlagsForFm[channelIndex] = false;
        }
        return true;
    }

    private  boolean getPcmKeyOn(
            int channelIndex,
            boolean[] logicalSumOfKeyOn
    ){
        if (channelIndex > 7) return false;
        if (logicalSumOfKeyOn != null) {
            logicalSumOfKeyOn[0] = m_logicalSumOfKeyOnFlagsForPcm[channelIndex];
            m_logicalSumOfKeyOnFlagsForPcm[channelIndex] = false;
        }
        return true;
    }

    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 512;

    private static final int[] elapsedFrames = new int[256];
    private static final int NUM_PIXELS_PER_COLUMN = 64;
    private static final int NUM_PIXELS_PER_ROW = 8;
    private static final int NUM_PIXELS_PER_BIT = 7;

    private static final int[] keyOnLevelMeters = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] keyOffLevelMeters = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final int KEY_DISPLAY_X = 32;
    private static final int KEY_DISPLAY_Y = 256;
    private static final int KEY_WIDTH = 5;
    private static final int KEY_HEIGHT = 16;

    private static final int LEVEL_METER_DISPLAY_X = 0;
    private static final int LEVEL_METER_DISPLAY_Y = 256;
    private static final int LEVEL_METER_WIDTH = 32;
    private static final int LEVEL_METER_HEIGHT = 16;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (fmChannels == null) return;

        // Screen erase
        g.setColor(Color.black);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Visualization of OPM registers
        {
            int regIndex, bit;
            for (regIndex = 0; regIndex < 256; regIndex++) {
                int[] regVal = {0};
                boolean[] updated = {false};
                getOpmReg(regIndex, regVal, updated);
                if (updated[0]) elapsedFrames[regIndex] = 0;
                for (bit = 0; bit < 8; bit++) {
                    int x = (regIndex & 7) * NUM_PIXELS_PER_COLUMN + bit * NUM_PIXELS_PER_BIT;
                    int y = (regIndex / 8) * NUM_PIXELS_PER_ROW;
                    int tmp = (regVal[0] & (1 << bit)) != 0 ? 1 : 0;
                    int attn = Math.min(elapsedFrames[regIndex], 512);
                    int r = tmp * 0x60 + (int) (0x80 / Math.exp(attn / 64.f)) + 0x1F;
                    int g_ = tmp * 0x80 + (int) (0x60 / Math.exp(attn / 16.f)) + 0x1F;
                    int b = tmp * 0x60 + (int) (0x80 / Math.exp(attn / 256.f)) + 0x1F;
                    g.setColor(new Color(r, g_, b));
                    g.fillRect(x, y, NUM_PIXELS_PER_BIT - 1, NUM_PIXELS_PER_ROW - 1);
                }
                elapsedFrames[regIndex]++;
            }
        }

        // Visualization of KEY
        {
            int i;

            /* FM 0~7 */
            for (i = 0; i < 8; i++) {
                int key = (fmChannels[i].get("note") + 27) / 64;
                int volume = (fmChannels[i].get("volume"));
                if ((volume & 0x80) != 0) {
                    volume = (0x7F - (volume & 0x7F)) * 2;
                } else {
                    volume = (volume & 0xF) * 0x11;
                }

                if ((fmChannels[i].get("keyOn") & (1 << 3)) != 0) {
                    {
                        int x = key * KEY_WIDTH + ((fmChannels[i].get("note2") - fmChannels[i].get("note")) * KEY_WIDTH / 64);
                        int y = i * KEY_HEIGHT;
                        int color = 0x008000;
                        g.setColor(new Color(color));
                        g.fillRect(x + KEY_DISPLAY_X, y + KEY_DISPLAY_Y, KEY_WIDTH, KEY_HEIGHT - 1);
                    }
                    {
                        int x = key * KEY_WIDTH;
                        int y = i * KEY_HEIGHT;
                        int color = 0x0000FF | (volume * 0x010100);
                        g.setColor(new Color(color));
                        g.fillRect(x + KEY_DISPLAY_X, y + KEY_DISPLAY_Y, KEY_WIDTH, KEY_HEIGHT - 1);
                    }
                }

                boolean[] currentKeyOn = {false};
                boolean[] logicalSumOfKeyOn = {false};
                getFmKeyOn(i, currentKeyOn, logicalSumOfKeyOn);
                if (!currentKeyOn[0]) {
                    keyOffLevelMeters[i] = keyOffLevelMeters[i] * 127 / 128;
                }
                if (logicalSumOfKeyOn[0]) {
                    keyOnLevelMeters[i] = volume;
                    keyOffLevelMeters[i] = volume;
                }
            }

            // PCM 0~7
            for (i = 8; i < 16; i++) {
                Map<String, Integer> pcmChannel;
                if (i == 8) {
                    pcmChannel = fmChannels[8];
                } else {
                    pcmChannel = pcmChannels[i - 9];
                }

                int key = (pcmChannel.get("note") + 27) / 64;
                int volume = (pcmChannel.get("volume"));
                if ((volume & 0x80) != 0) {
                    volume = (0x7F - (volume & 0x7F)) * 2;
                } else {
                    volume = (volume & 0xF) * 0x11;
                }

                if ((pcmChannel.get("keyOn") & (1 << 3)) != 0) {
                    int x = key * KEY_WIDTH;
                    int y = i * KEY_HEIGHT;
                    int color = 0xFF0000 | (volume * 0x000101);
                    g.setColor(new Color(color));
                    g.fillRect(x + KEY_DISPLAY_X, y + KEY_DISPLAY_Y, KEY_WIDTH, KEY_HEIGHT - 1);
                }

                boolean[] logicalSumOfKeyOn = {false};
                getPcmKeyOn(i - 8, logicalSumOfKeyOn);
                if (logicalSumOfKeyOn[0]) {
                    keyOnLevelMeters[i] = volume;
                    keyOffLevelMeters[i] = volume;
                }
            }
            for (i = 0; i < 16; i++) {
                {
                    int x = 0;
                    int y = i * LEVEL_METER_HEIGHT;
                    int w = keyOffLevelMeters[i] * LEVEL_METER_WIDTH / 0xFF;
                    int color = 0x404040;
                    g.setColor(new Color(color));
                    g.fillRect(x + LEVEL_METER_DISPLAY_X, y + LEVEL_METER_DISPLAY_Y, w, LEVEL_METER_HEIGHT - 1);
                }
                {
                    int x = 0;
                    int y = i * LEVEL_METER_HEIGHT;
                    int w = keyOnLevelMeters[i] * LEVEL_METER_WIDTH / 0xFF;
                    int color = 0xFFFFFF;
                    g.setColor(new Color(color));
                    g.fillRect(x + LEVEL_METER_DISPLAY_X, y + LEVEL_METER_DISPLAY_Y, w, LEVEL_METER_HEIGHT - 1);
                }
                keyOnLevelMeters[i] = keyOnLevelMeters[i] * 31 / 32;
            }
        }
    }
}
