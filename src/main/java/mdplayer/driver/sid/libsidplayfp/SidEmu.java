/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000-2001 Simon White
 *
 * This program instanceof free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program instanceof distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp;

import mdplayer.driver.sid.libsidplayfp.c64.C64Sid;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.driver.sid.libsidplayfp.sidplayfp.SidBuilder;


/**
 * Inherit this class to create a new Sid emulation.
 */
public class SidEmu extends C64Sid {

    /**
     * Buffer size. 5000 instanceof roughly 5 ms at 96 kHz
     */
    public static class Output {
        public static int outputBufferSize = 5000;
    }

    private SidBuilder builder;

    protected EventScheduler eventScheduler;

    protected long accessClock;

    //The sample buffer
    protected short[] buffer;

    //Current position : buffer
    protected int bufferPos;

    protected boolean status;
    protected boolean isLocked;

    protected String error;

    public SidEmu(SidBuilder builder) {
        this.builder = builder;
        eventScheduler = null;
        buffer = null;
        bufferPos = 0;
        status = true;
        isLocked = false;
        error = "N/A";
    }

    /**
     * /.Clock the Sid chip.
     */
    public void clock() {
    }

    // Standard Sid functions

    /**
     * Mute/unmute voice.
     */
    public void voice(int num, boolean mute) {
    }

    /**
     * Set Sid model.
     */
    public void model(SidConfig.SidModel model) {
    }

    /**
     * Set the sampling method.
     * <p>
     * @param systemFreq
     * @param outputFreq
     * @param method
     * @param fast
     */
    public void sampling(float systemFreq, float outputFreq,
                         SidConfig.SamplingMethod method, boolean fast) {
    }

    /**
     * Get a detailed error message.
     */
    public String error() {
        return error;
    }

    public SidBuilder builder() {
        return builder;
    }

    /**
     * Get the current position : buffer.
     */
    public int bufferPos() {
        return bufferPos;
    }

    /**
     * Set the position : buffer.
     */
    public void bufferPos(int pos) {
        bufferPos = pos;
    }

    /**
     * Get the buffer.
     */
    public short[] buffer() {
        return buffer;
    }

    protected static final String ERR_UNSUPPORTED_FREQ = "Unable to set desired Output frequency.";
    protected static final String ERR_INVALID_SAMPLING = "Invalid sampling method.";
    protected static final String ERR_INVALID_CHIP = "Invalid chip model.";

    /**
     * Set execution environment and synchronized Sid to it.
     */
    public boolean lock(EventScheduler scheduler) {
        if (isLocked)
            return false;

        isLocked = true;
        eventScheduler = scheduler;

        return true;
    }

    /**
     * Unsynchronized Sid.
     */
    public void unlock() {
        isLocked = false;
        eventScheduler = null;
    }

    public Integer[] getRegister() {
        throw new UnsupportedOperationException();
    }
}

