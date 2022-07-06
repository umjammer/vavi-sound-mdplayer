
package mdplayer;

public class VRC7 extends mdsound.Ym2413 {
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
    }

    @Override
    public void reset(byte ChipID) {
    }

    @Override
    public int start(byte ChipID, int samplingRate) {
        return start(ChipID, samplingRate, 0);
    }

    @Override
    public int start(byte ChipID, int samplingRate, int ClockValue, Object... option) {
        nv.setClock(ClockValue / 2);// masterclock(NES:1789773)
        nv.setRate(samplingRate);// samplerate
        nv.reset();
        rate = samplingRate;
        return samplingRate;
    }

    @Override
    public void stop(byte ChipID) {
    }

    private int[] b = new int[2];

    @Override
    public void update(byte ChipID, int[][] outputs, int samples) {
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
    public int write(byte ChipID, int port, int adr, int data) {
        nv.write(0x9010, adr);
        nv.write(0x9030, data);
        return 0;
    }
}
