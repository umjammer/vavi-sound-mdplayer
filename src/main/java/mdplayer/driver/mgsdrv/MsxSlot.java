package mdplayer.driver.mgsdrv;

import dotnet4j.util.compat.QuadConsumer;
import dotnet4j.util.compat.TriConsumer;
import mdplayer.Chip;
import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;


public class MsxSlot extends Slot {

    public MsxSlot(TriConsumer<Integer, Integer, Integer> chipWrite) {
        slots = new Cartridge[][] {
            // Slot0 MapperROM 64k
            new Cartridge[] {
                new MapperRamCartridge(4)
            },
            // Slot1 MapperROM 64k
            new Cartridge[] {
                new MapperRamCartridge(4)
            },
            // extSlot2-0 SCC
            // extSlot2-1 MapperROM 64k
            // extSlot2-2 MapperROM 64k
            // extSlot2-3 MapperROM 64k
            new Cartridge[] {
                new SCCCartridge(chipWrite), new MapperRamCartridge(4), new MapperRamCartridge(4),
                new MapperRamCartridge(4)
            },
            // extSlot3-0 MSX Music(Ym2413)
            // extSlot3-1 MapperROM 512k
            // extSlot3-2 MapperROM 64k
            // extSlot3-3 MapperROM 64k
            new Cartridge[] {
                new MsxMusicCartridge(), new MapperRamCartridge(32), new MapperRamCartridge(4), new MapperRamCartridge(4)
            }
        };

        setPageFromSlot(0, 3, 1);
        setPageFromSlot(1, 3, 1);
        setPageFromSlot(2, 3, 1);
        setPageFromSlot(3, 3, 1);

        // e3-1 512(32*16)kB Mapped RAM
        // MAIN ROM settings
        slots[3][1].set(0xfcc1, (byte) 0x09); // exSlot none MAIN ROM position(?) (ExtSLOT : 3-1 )
        slots[3][1].set(0xfcc2, (byte) 0x00); // exSlot none
        slots[3][1].set(0xfcc3, (byte) 0x80); // exSlot exist
        slots[3][1].set(0xfcc4, (byte) 0x80); // exSlot exist
    }
}
