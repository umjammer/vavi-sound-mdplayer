
package mdplayer.driver.sid.libsidplayfp.c64.banks;

public interface IBank {
    void poke(int address, byte value);

    byte peek(int address);
}

