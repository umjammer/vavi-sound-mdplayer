
package mdplayer.driver.sid.libsidplayfp.c64.banks;

public interface IBank
    {
        void poke(short address, byte value);
        byte peek(short address);
    }

