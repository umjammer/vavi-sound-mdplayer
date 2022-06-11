
package mdplayer.driver.mgsdrv;

public interface Cartridge {
    byte get(int address);
    void set(int address, byte value);

    int PAGE_SIZE = 16 * 1024;
}
