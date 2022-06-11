
package mdplayer.driver.mgsdrv;

public class MSXMusicCartridge implements Cartridge {

    private final byte[] memory = new byte[65536];

    public MSXMusicCartridge() {
        memory[0x4018] = (byte) 'A';
        memory[0x4019] = (byte) 'P';
        memory[0x401a] = (byte) 'R';
        memory[0x401b] = (byte) 'L';
        memory[0x401c] = (byte) 'O';
        memory[0x401d] = (byte) 'P';
        memory[0x401e] = (byte) 'L';
        memory[0x401f] = (byte) 'L';
    }

    @Override
    public byte get(int address) {
        return memory[address];
    }

    @Override
    public void set(int address, byte value) {
        Write(address, value);
    }

    private void Write(int adr, byte dat) {
    }
}
