package mdplayer.driver.fmp.nise98;

class NiseInt08Timer {

    private final Nise286 cpu;
    private boolean enable = false;
    private int counter = 0;
    private int wCounter = 300;
    private int intNumber = 8;

    public NiseInt08Timer(Nise286 cpu, int intNumber /* = 8 */) {
        this.cpu = cpu;
        this.intNumber = intNumber;
    }

    public void start() {
        enable = true;
        counter = wCounter;
    }

    public void stepExecute() {
        if ((cpu.w_mmsk & 0x01) != 0) return;
        if (!enable) return;

        wCounter--;
        if (wCounter > 0) return;
        wCounter = counter;
        cpu.interruptTrigger[intNumber] = true;
    }
}

