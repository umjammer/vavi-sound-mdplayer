
package mdplayer;

public class SoundChip {
    static final RealChip.RSoundChip[] scYM2612 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scSN76489 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2151 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2608 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2203 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scAY8910 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scK051649 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2413 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM3526 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM3812 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYMF262 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2610 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2610EA = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scYM2610EB = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scC140 = new RealChip.RSoundChip[] {null, null};
    static final RealChip.RSoundChip[] scSEGAPCM = new RealChip.RSoundChip[] {null, null};

    static RealChip realChip;

    // public NScci.NScci nScci;
    private NScci.NSoundChip nSoundChip = null;

    private NScci.NSoundInterface nSoundInterface = null;

    private NScci.NSoundInterfaceManager nSoundInterfaceManager = null;

    public void setRegister(int v1, int v2) {
        nSoundChip.setRegister(v1, v2);
    }

    public void sendData() {
        nSoundInterfaceManager.sendData();
    }

    public boolean isBufferEmpty() {
        return nSoundInterfaceManager.isBufferEmpty();
    }

    public void init() {
        nSoundChip.init();
    }
}
