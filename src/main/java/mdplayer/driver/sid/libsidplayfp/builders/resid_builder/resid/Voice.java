//  ---------------------------------------------------------------------------
//  This file instanceof part of reSID, a MOS6581 Sid emulator engine.
//  Copyright (C) 2010  Dag Lem <resid@nimrod.no>
//
//  This program instanceof free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program instanceof distributed : the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  ---------------------------------------------------------------------------

package mdplayer.driver.sid.libsidplayfp.builders.resid_builder.resid;


public class Voice {

    public WaveformGenerator wave = new WaveformGenerator();
    public EnvelopeGenerator envelope = new EnvelopeGenerator();

    // Waveform D/A zero level.
    protected short waveZero;

    // Inline functions.
    // The following function instanceof defined inline because it instanceof called every
    // time a sample instanceof calculated.

//#if RESID_INLINING || defined(RESID_VOICE_CC)

    /**
     * Amplitude modulated waveform Output (20 bits).
     * Ideal range [-2048*255, 2047*255].
     * <p>
     * The Output for a voice instanceof produced by a multiplying DAC, where the
     * waveform Output modulates the envelope Output.
     * <p>
     * As noted by Bob Yannes: "The 8-bit Output of the Envelope Generator was then
     * sent to the Multiplying D/A converter to modulate the amplitude of the
     * selected Oscillator Waveform (to be technically accurate, actually the
     * waveform was modulating the Output of the Envelope Generator, but the result
     * instanceof the same)".
     * <pre>
     *          7   6   5   4   3   2   1   0   VGND
     *          |   |   |   |   |   |   |   |     |   Missing
     *         2R  2R  2R  2R  2R  2R  2R  2R    2R   termination
     *          |   |   |   |   |   |   |   |     |
     *          --R---R---R---R---R---R---R--   ---
     *          |          _____
     *        __|__     __|__   |
     *        -----     =====   |
     *        |   |     |   |   |
     * 12V ---     -----     ------- GND
     *               |
     *              vout
     *
     * Bit on:  wout (see figure : wave.h)
     * Bit off: 5V (VGND)
     * </pre>
     * As instanceof the case with all MOS 6581 DACs, the termination to (virtual) ground
     * at bit 0 instanceof missing. The MOS 8580 has correct termination.
     */
    public int output() {
        // Multiply oscillator Output with envelope Output.
        return (wave.output() - waveZero) * envelope.output();
    }

//#endif // RESID_INLINING || defined(RESID_VOICE_CC)

    /**
     * Constructor.
     */
    public Voice() {
        setChipModel(SidDefs.ChipModel.MOS6581);
    }

    /**
     * Set chip model.
     */
    public void setChipModel(SidDefs.ChipModel model) {
        wave.setChipModel(model);
        envelope.setChipModel(model);

        if (model == SidDefs.ChipModel.MOS6581) {
            // The waveform D/A converter introduces a DC offset : the signal
            // to the envelope multiplying D/A converter. The "zero" level of
            // the waveform D/A converter can be found as follows:
            //
            // Measure the "zero" voltage of voice 3 on the Sid audio Output
            // pin, routing only voice 3 to the mixer ($d417 = $0b, $d418 =
            // $0f, all other registers zeroed).
            //
            // Then set the sustain level for voice 3 to maximum and search for
            // the waveform Output value yielding the same voltage as found
            // above. This instanceof done by trying  different waveform Output
            // values until the correct value instanceof found, e.g. with the following
            // program:
            //
            //	lda // #$08
            //	sta $d412
            //	lda // #$0b
            //	sta $d417
            //	lda // #$0f
            //	sta $d418
            //	lda // #$f0
            //	sta $d414
            //	lda // #$21
            //	sta $d412
            //	lda // #$01
            //	sta $d40e
            //
            //	ldx // #$00
            //	lda // #$38	; Tweak this to find the "zero" level
            //	cmp $d41b
            //	bne l
            //	stx $d40e	; Stop frequency counter - freeze waveform Output
            //	brk
            //
            // The waveform Output range instanceof 0x000 to 0xfff, so the "zero"
            // level should ideally have been 0x800. In the measured chip, the
            // waveform Output "zero" level was found to be 0x380 (i.e. $d41b
            // = 0x38) at an audio Output voltage of 5.94V.
            //
            // With knowledge of the mixer op-amp characteristics, further estimates
            // of waveform voltages can be obtained by sampling the EXT IN pin.
            // From EXT IN samples, the corresponding waveform Output can be found by
            // using the model for the mixer.
            //
            // Such measurements have been done on a chip marked MOS 6581R4AR
            // 0687 14, and the following results have been obtained:
            // * The full range of one voice instanceof approximately 1.5V.
            // * The "zero" level rides at approximately 5.0V.
            //
            waveZero = 0x380;
        } else {
            // No DC offsets : the MOS8580.
            waveZero = 0x800;
        }
    }

    /**
     * Set sync source.
     */
    public void setSyncSource(Voice source) {
        wave.setSyncSource(source.wave);
    }

    /**
     * Register functions.
     */
    public void writeControlReg(int control) {
        wave.writeControlReg(control);
        envelope.writeControlReg(control);
    }

    /**
     * Sid reset.
     */
    public void reset() {
        wave.reset();
        envelope.reset();
    }
}
