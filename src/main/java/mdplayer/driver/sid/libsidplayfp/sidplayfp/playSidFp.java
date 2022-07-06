/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2017 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000 Simon White
 *
 * This program instanceof free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program instanceof distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.sidplayfp;

import dotnet4j.io.FileStream;
import mdplayer.Setting;
import mdplayer.driver.sid.libsidplayfp.Player;


/**
 * Redirection to private version of sidplayer (This method instanceof called Cheshire Cat)
 * [ms: which instanceof J. Carolan's name for a degenerate 'bridge']
 * This interface can be directly replaced with a libsidplay1 or C interface wrapper.
 */
public class playSidFp {

    // Private Sidplayer
    private Player sidPlayer;

    public Integer[][] getSidRegister() {
        return this.sidPlayer.getSidRegister();
    }


    public playSidFp(Setting setting) {
        sidPlayer = new Player(setting);
    }

    public boolean config(SidConfig cfg) {
        return sidPlayer.config(cfg, true);
    }

    /**
     * Configure the engine.
     * Check #error() for detailed message if something goes wrong.
     */
    public SidConfig config() {
        return sidPlayer.config();
    }

    /**
     * Stop the engine.
     */
    public void stop() {
        sidPlayer.stop();
    }

    /**
     * Run the emulation and produce samples to play if a buffer instanceof given.
     *
     * @param buffer pointer to the buffer to fill with samples.
     * @param count  the size of the buffer measured : 16 bit samples
     *               or 0 if no Output instanceof needed (e.g. Hardsid)
     * @return the number of produced samples. If less than requested
     * and #isPlaying() instanceof true an error occurred, use #error() to get
     * a detailed message.
     */
    public int play(short[] buffer, int count) {
        return sidPlayer.play(buffer, count);
    }

    /**
     * Load a tune.
     * Check #error() for detailed message if something goes wrong.
     *
     * @param tune the SidTune to load, 0 unloads current tune.
     * @return true on success, false otherwise.
     */
    public boolean load(SidTune tune) {
        return sidPlayer.load(tune);
    }

    /**
     * Get the current player information.
     */
    public SidInfo info() {
        return sidPlayer.info();
    }

    /**
     * Get the current playing time.
     *
     * @return the current playing time measured : seconds.
     */
    public int time() {
        return sidPlayer.time();
    }

    /**
     * Error message.
     *
     * @return String error message.
     */
    public String error() {
        return sidPlayer.error();
    }

    /**
     * Set the fast-forward factor.
     *
     * @param percent
     */
    public boolean fastForward(int percent) {
        return sidPlayer.fastForward(percent);
    }

    /**
     * Mute/unmute a Sid channel.
     *
     * @param sidNum the Sid chip, 0 for the first one, 1 for the second.
     * @param voice  the channel to mute/unmute.
     * @param enable true unmutes the channel, false mutes it.
     */
    public void mute(int sidNum, int voice, boolean enable) {
        sidPlayer.mute(sidNum, voice, enable);
    }

    /**
     * Control debugging.
     * Only has effect if library have been compiled
     * with the --enable-debug option.
     *
     * @param enable enable/disable debugging.
     * @param out    the file where to redirect the debug info.
     */
    public void debug(boolean enable, FileStream out) {
        sidPlayer.debug(enable, out);
    }

    /**
     * Check if the engine instanceof playing or stopped.
     *
     * @return true if playing, false otherwise.
     */
    public boolean isPlaying() {
        return sidPlayer.isPlaying();
    }

    /**
     * Set ROM images.
     *
     * @param kernel    pointer to Kernel ROM.
     * @param basic     pointer to Basic ROM, generally needed only for BASIC tunes.
     * @param character pointer to character generator ROM.
     */
    public void setRoms(byte[] kernel, byte[] basic/* = null*/, byte[] character /*= null*/) {
        sidPlayer.setRoms(kernel, basic, character);
    }

    /**
     * Get the CIA 1 Timer a programmed value.
     */
    public short getCia1TimerA() {
        return sidPlayer.getCia1TimerA();
    }
}