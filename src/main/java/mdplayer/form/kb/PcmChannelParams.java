/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.form.kb;

/**
 * The channel row of a sample-playback chip's panel: {@link ChannelParams} plus the sample
 * addressing the PCM family (C140, C352, QSound, GA20, K05xxxx, SegaPCM, MultiPCM, …) displays.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-18 nsano initial version <br>
 */
public class PcmChannelParams extends ChannelParams {

    public int bank = -1;
    public int sadr = -1;
    public int eadr = -1;
    public int ladr = -1;
    public int leadr = -1;
    public int srcFreq = -1;
    public int flg16 = -1;
    public int pcmMode = -1;
}
