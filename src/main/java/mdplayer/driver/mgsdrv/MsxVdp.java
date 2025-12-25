package mdplayer.driver.mgsdrv;

public class MsxVdp {

    private int port1Flip = 0;
    private byte port1Data = 0;
    //private byte port1RegNo = 0;
    private final byte[] R = new byte[47];
    private boolean adrCounterSet = false;
    private int adrCounter = 0;
    private boolean adrCounterIsWrite = false;
    private final byte[] memory = new byte[0x2_0000];

    public byte m = 0;
    public byte n = 0;

    public MsxVdp() {
    }

    public byte read(int address) {

        byte value = 0;

        switch (address) {
            case 0:
                if (!adrCounterIsWrite) {
                    //logger.log(Level.TRACE, "VDP.Read:memory[%05x]=Val:%02x".formatted(adrCounter, memory[adrCounter]));
                    value = memory[adrCounter++];
                }
                break;
            case 1:
                throw new UnsupportedOperationException();
            case 2:
                throw new UnsupportedOperationException();
        }

        //logger.log(Level.TRACE, "VDP.Read:Port:%x Val:%02x".formatted(address, value));
        return value;
    }

    public void write(int address, byte value) {
        //logger.log(Level.TRACE, "VDP.Write:Port:%x Val:%02x".formatted(address, value));

        switch (address) {
            case 0:
                if (adrCounterIsWrite) {
                    //logger.log(Level.TRACE, "VDP.Write:memory[%05x]=Val:%02x".formatted(adrCounter, value));
                    memory[adrCounter++] = value;
                }
                break;
            case 1:
                if (port1Flip == 0) {
                    port1Data = value;
                } else {
                    if ((value & 0xc0) == 0x80) {
                        int regNo = value & 0x3f;
                        R[regNo] = port1Data;
                        if (regNo == 14) {
                            adrCounterSet = true;
                            adrCounter &= 0x0_3f_ff;
                            adrCounter |= (port1Data & 0x7) << 14;
                        }
                    } else if ((value & 0x80) == 0x00) {
                        if (adrCounterSet) {
                            adrCounter &= 0x1_ff_00;
                            adrCounter |= port1Data & 0xff;

                            adrCounterIsWrite = (value & 0x40) != 0;
                            adrCounter &= 0x1_c0_ff;
                            adrCounter |= (value & 0x3f) << 8;
                            adrCounterSet = false;
                        }
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
                port1Flip++;
                port1Flip &= 1;
                break;
            case 3:
                throw new UnsupportedOperationException();
        }
    }
}
