/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 */

package mdplayer.driver.sid;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

import mdplayer.Setting;
import mdplayer.lib.sid.libsidplayfp.SidEmu;
import mdplayer.lib.sid.libsidplayfp.builders.resid_builder.ReSid;
import mdplayer.lib.sid.libsidplayfp.builders.resid_builder.ReSidBuilder;
import mdplayer.lib.sid.libsidplayfp.builders.resid_builder.resid.Sid;
import mdplayer.lib.sid.libsidplayfp.builders.resid_builder.resid.Voice;
import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidBuilder;
import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidConfig;
import mdplayer.lib.sid.libsidplayfp.sidplayfp.SidTune;
import mdplayer.lib.sid.libsidplayfp.sidplayfp.playSidFp;

import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Non-interactive diagnostic. Plays a short slice of a SID file and dumps
 * per-voice FREQ writes and accumulator snapshots for voice 0/1/2 so we can
 * see whether voice 0's pitch path is misbehaving.
 */
public class SidDiagTest {

    private static final int SamplingRate = 44100;

    static void main(String[] args) throws Exception {
        String filename = args.length > 0
                ? args[0]
                : "/Users/nsano/Public/np2/sid/Wizball.sid";
        int song = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        int buffersToPlay = args.length > 2 ? Integer.parseInt(args[2]) : 60;

        byte[] fileBuffer = Files.readAllBytes(Paths.get(filename));

        Setting setting = mock(Setting.class, Mockito.RETURNS_DEEP_STUBS);
        when(setting.getOutputDevice().getSampleRate()).thenReturn(SamplingRate);

        playSidFp engine = new playSidFp(SamplingRate);
        engine.setRoms(null, null, null);

        ReSidBuilder rs = new ReSidBuilder("ReSid", setting.getOutputDevice().getSampleRate());
        rs.create(1);

        SidTune tune = new SidTune(fileBuffer, fileBuffer.length);
        tune.selectSong(song);

        if (!engine.load(tune)) {
            System.err.println("Error loading tune: " + engine.error());
            return;
        }

        SidConfig cfg = new SidConfig(SamplingRate);
        cfg.frequency = SamplingRate;
        cfg.samplingMethod = SidConfig.SamplingMethod.RESAMPLE_INTERPOLATE;
        cfg.fastSampling = false;
        cfg.playback = SidConfig.Playback.STEREO;
        cfg.sidEmulation = rs;

        if (!engine.config(cfg)) {
            System.err.println("Error configuring: " + engine.error());
            return;
        }

        java.lang.reflect.Field sidobjsF = SidBuilder.class.getDeclaredField("sidobjs");
        sidobjsF.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SidEmu> sidobjs = (List<SidEmu>) sidobjsF.get(rs);
        ReSid reSid = (ReSid) sidobjs.getFirst();
        Sid sid = reSid.getSID();
        Voice[] voices = getVoices(sid);

        int bufferSize = 2048;
        short[] sampleBuffer = new short[bufferSize * 2];

        // mute mode: 0=all on, 1=only v0 (mute 1,2), 2=only v1, 3=only v2
        int muteMode = args.length > 3 ? Integer.parseInt(args[3]) : 0;
        if (muteMode == 1) { engine.mute(0, 1, true); engine.mute(0, 2, true); }
        if (muteMode == 2) { engine.mute(0, 0, true); engine.mute(0, 2, true); }
        if (muteMode == 3) { engine.mute(0, 0, true); engine.mute(0, 1, true); }

        java.io.DataOutputStream wav = null;
        if (args.length > 4) {
            wav = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(args[4])));
        }

        Integer[] reg = sid.GetRegister();
        for (int b = 0; b < buffersToPlay; b++) {
            int produced = engine.play(sampleBuffer, bufferSize * 2);
            long sumSq = 0;
            int peak = 0;
            for (int i = 0; i < produced; i++) {
                short s = sampleBuffer[i];
                sumSq += (long) s * s;
                if (Math.abs(s) > peak) peak = Math.abs(s);
                if (wav != null) {
                    wav.writeByte(s & 0xff);
                    wav.writeByte((s >> 8) & 0xff);
                }
            }
            double rms = produced > 0 ? Math.sqrt((double) sumSq / produced) : 0;
            System.out.printf(
                    "buf %3d | mute=%d rms=%6.0f peak=%6d | v0 f=%5d acc=%06x wf=%02x | v1 f=%5d wf=%02x | v2 f=%5d wf=%02x%n",
                    b, muteMode, rms, peak,
                    voices[0].wave.freq, voices[0].wave.accumulator, voices[0].wave.waveform,
                    voices[1].wave.freq, voices[1].wave.waveform,
                    voices[2].wave.freq, voices[2].wave.waveform);
        }
        if (wav != null) wav.close();
    }

    private static Voice[] getVoices(Sid sid) throws Exception {
        java.lang.reflect.Field f = Sid.class.getDeclaredField("voice");
        f.setAccessible(true);
        return (Voice[]) f.get(sid);
    }
}
