
package mdplayer.driver.zgm.zgmChip;

import mdplayer.Common.EnmModel;
import mdplayer.driver.zgm.EnmZGMDevice;


public class Chip {
    public Boolean use;
    public long delay;
    public EnmModel model;
    public EnmZGMDevice device;
    public int index;
    public int number;
    public int hosei;
    private final Object lockObj = new Object();
    private Boolean[] _chMasks = null;

    public void setChMasks(Boolean[] value) {
        synchronized (lockObj) {
            _chMasks = value;
        }
    }

    Boolean[] getChMasks() {
        synchronized (lockObj) {
            return _chMasks;
        }
    }

    int currentCh;

    public int getCurrentCh() {
        return currentCh;
    }

    public Chip(int ch) {
        _chMasks = new Boolean[ch];
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
