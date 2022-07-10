package mdplayer.driver.mgsdrv;

import mdplayer.ChipRegister;
import mdplayer.Common.EnmModel;


public class MSXSlot extends Slot {

    public MSXSlot(ChipRegister chipRegister, EnmModel model) {
        slots = new Cartridge[][] {
            // Slot0 MapperROM 64k
            new Cartridge[] {
                new MapperRAMCartridge(4)
            },
            // Slot1 MapperROM 64k
            new Cartridge[] {
                new MapperRAMCartridge(4)
            },
            // extSlot2-0 SCC
            // extSlot2-1 MapperROM 64k
            // extSlot2-2 MapperROM 64k
            // extSlot2-3 MapperROM 64k
            new Cartridge[] {
                new SCCCartridge(chipRegister, model), new MapperRAMCartridge(4), new MapperRAMCartridge(4),
                new MapperRAMCartridge(4)
            },
            // extSlot3-0 MSX Music(Ym2413)
            // extSlot3-1 MapperROM 512k
            // extSlot3-2 MapperROM 64k
            // extSlot3-3 MapperROM 64k
            new Cartridge[] {
                new MSXMusicCartridge(), new MapperRAMCartridge(32), new MapperRAMCartridge(4), new MapperRAMCartridge(4)
            }
        };

        setPageFromSlot(0, 3, 1);
        setPageFromSlot(1, 3, 1);
        setPageFromSlot(2, 3, 1);
        setPageFromSlot(3, 3, 1);

        // e3-1 512(32*16)kB Mapped RAM
        // MAIN ROM 設定
        slots[3][1].set(0xfcc1, (byte) 0x09);// exslot none MAIN ROMの位置(?) (
                                             // ExtSLOT : 3-1 )
        slots[3][1].set(0xfcc2, (byte) 0x00);// exslot none
        slots[3][1].set(0xfcc3, (byte) 0x80);// exslot exist
        slots[3][1].set(0xfcc4, (byte) 0x80);// exslot exist
    }
}
