
package mdplayer;

import java.util.function.Consumer;

import mdsound.instrument.IntFNesInst;
import mdsound.instrument.Ym2413Inst;


public class VRC7 extends Ym2413Inst {
    @Override
    public String getName() {
        return "VRC7";
    }

    @Override
    public String getShortName() {
        return "VRC7";
    }

    private mdsound.np.chip.NesVrc7 nv;

    private double apu_clock_rest;

    private double rate;

    public VRC7() {
        nv = new mdsound.np.chip.NesVrc7();
        nv.setListener(listenr);
    }

    @Override
    public void reset(int chipId) {
    }

    @Override
    public int start(int chipId, int samplingRate) {
        return start(chipId, samplingRate, 0);
    }

    @Override
    public int start(int chipId, int samplingRate, int ClockValue, Object... option) {
        nv.setClock(ClockValue / 2.);// masterclock(NES:1789773)
        nv.setRate(samplingRate);// samplerate
        nv.reset();
        rate = samplingRate;
        return samplingRate;
    }

    @Override
    public void stop(int chipId) {
    }

    private int[] b = new int[2];

    @Override
    public void update(int chipId, int[][] outputs, int samples) {
        double apu_clock_per_sample = 1789773 / rate;

        for (int i = 0; i < samples; i++) {
            // tick APU / expansions
            apu_clock_rest += apu_clock_per_sample;
            int apu_clocks = (int) (apu_clock_rest);
            if (apu_clocks > 0) {
                apu_clock_rest -= apu_clocks;
            }

            nv.tick(apu_clocks);
            nv.render(b);
            // if(b[0]!=0)Debug.printf(("%d",b[0]);
            outputs[0][i] += b[0] << 2;
            outputs[1][i] += b[1] << 2;
        }
    }

    @Override
    public int write(int chipId, int port, int adr, int data) {
        nv.write(0x9010, adr);
        nv.write(0x9030, data);
        return 0;
    }

    private Consumer<int[]> listenr = ds -> {
        if (ds[7] != -1) IntFNesInst.np_nes_vrc7_volume = ds[7];
    };
}
