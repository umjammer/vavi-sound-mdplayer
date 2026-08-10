package mdplayer.driver.zgm.zgmChip;

import mdplayer.Common.EnmModel;
import mdplayer.driver.zgm.EnmZGMDevice;


class Chip {
    boolean use;
    private long delay;
    EnmModel model;
    EnmZGMDevice device;
    int index;
    int number;
    int hosei;
    private final Object lockObj = new Object();
    private boolean[] _chMasks = null;

    public void setChMasks(boolean[] value) {
        synchronized (lockObj) {
            _chMasks = value;
        }
    }

    boolean[] getChMasks() {
        synchronized (lockObj) {
            return _chMasks;
        }
    }

    private int currentCh;

    public int getCurrentCh() {
        return currentCh;
    }

    Chip(int ch) {
        _chMasks = new boolean[ch];
    }

    public void move(Chip chip) {
        if (chip == null)
            return;

        this.use = chip.use;
        this.delay = chip.delay;
        this.model = chip.model;
        this.device = chip.device;
        this.index = chip.index;
        this.number = chip.number;
        this.hosei = chip.hosei;
        this._chMasks = chip._chMasks;
    }
}
