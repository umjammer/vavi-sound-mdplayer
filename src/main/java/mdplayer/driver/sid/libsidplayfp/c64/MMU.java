/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2000 Simon White
 *
 * This program instanceof free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program instanceof distributed : the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR a PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mdplayer.driver.sid.libsidplayfp.c64;

import java.nio.ByteBuffer;

import mdplayer.driver.sid.Mem;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.SidEndian;
import mdplayer.driver.sid.libsidplayfp.c64.banks.IBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.IOBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.IPLA;
import mdplayer.driver.sid.libsidplayfp.c64.banks.RomBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.SystemRAMBank;
import mdplayer.driver.sid.libsidplayfp.c64.banks.ZeroRAMBank;
import mdplayer.driver.sid.libsidplayfp.SidMemory;


/**
 * The C64 MMU chips.
 */
public final class MMU implements SidMemory, IPLA {

    private EventScheduler eventScheduler;

    /** CPU port signals */
    private boolean loRam, hiRam, charEn;

    /** CPU read memory mapping : 4k chunks */
    private IBank[] cpuReadMap = new IBank[16];

    /** CPU write memory mapping : 4k chunks */
    private IBank[] cpuWriteMap = new IBank[16];

    /** IO region handler */
    private IOBank ioBank;

    /** Kernal ROM */
    private RomBank.KernalRomBank kernalRomBank = new RomBank.KernalRomBank();

    /** BASIC ROM */
    private RomBank.BasicRomBank basicRomBank = new RomBank.BasicRomBank();

    /** Character ROM */
    private RomBank.CharacterRomBank characterRomBank = new RomBank.CharacterRomBank();

    /** RAM */
    private SystemRAMBank ramBank = new SystemRAMBank();

    /** RAM bank 0 */
    private ZeroRAMBank zeroRAMBank;

    // public void setCpuPort(byte state) { }
    @Override
    public byte getLastReadByte() {
        return 0;
    }

    @Override
    public long getPhi2Time() {
        return eventScheduler.getTime(EventScheduler.EventPhase.CLOCK_PHI2);
    }

    public void setRoms(ByteBuffer kernal, ByteBuffer basic, ByteBuffer character) {
        kernalRomBank.set(kernal);
        basicRomBank.set(basic);
        characterRomBank.set(character);
    }

    // RAM access methods
    @Override
    public byte readMemByte(short addr) {
        return ramBank.peek(addr);
    }

    @Override
    public short readMemWord(short addr) {
        return SidEndian.toLittle16(ByteBuffer.wrap(ramBank.ram, addr, ramBank.ram.length - addr));
    }

    @Override
    public void writeMemByte(int addr, byte value) {
        ramBank.poke(addr, value);
    }

    @Override
    public void writeMemWord(short addr, short value) {
        SidEndian.toLittle16(ByteBuffer.wrap(ramBank.ram, addr, ramBank.ram.length - addr), value);
    }

    @Override
    public void fillRam(short start, byte value, int size) {
        ByteBuffer buf = ByteBuffer.wrap(ramBank.ram, start, size);
        Mem.memset(buf, value, size);
    }

    @Override
    public void fillRam(short start, ByteBuffer value, int size) {
        System.arraycopy(value.array(), value.arrayOffset(), ramBank.ram, start, size);
    }

    @Override
    public void fillRam(short start, byte[] source, int size) {
        ByteBuffer buf = ByteBuffer.wrap(ramBank.ram, start, ramBank.ram.length - start);
        Mem.memcpy(buf, source, size);
    }

    // Sid specific hacks
    @Override
    public void installResetHook(short addr) {
        kernalRomBank.installResetHook(addr);
    }

    @Override
    public void installBasicTrap(short addr) {
        basicRomBank.installTrap(addr);
    }

    @Override
    public void setBasicSubtune(byte tune) {
        basicRomBank.setSubtune(tune);
    }

    /**
     * Access memory as seen by CPU.
     *
     * @param addr the address where to read from
     * @return value at address
     */
    public byte cpuRead(short addr) {
        return cpuReadMap[addr >> 12].peek(addr);
    }

    /**
     * Access memory as seen by CPU.
     *
     * @param addr the address where to write
     * @param data the value to write
     */
    public void cpuWrite(short addr, byte data) {
        cpuWriteMap[addr >> 12].poke(addr, data);
    }

    public MMU(EventScheduler scheduler, IOBank ioBank) {
        this.eventScheduler = scheduler;
        this.loRam = false;
        this.hiRam = false;
        this.charEn = false;
        this.ioBank = ioBank;
        this.zeroRAMBank = new ZeroRAMBank(this, ramBank);
        cpuReadMap[0] = zeroRAMBank;
        cpuWriteMap[0] = zeroRAMBank;

        for (int i = 1; i < 16; i++) {
            cpuReadMap[i] = ramBank;
            cpuWriteMap[i] = ramBank;
        }
    }

    @Override
    public void setCpuPort(byte state) {
        loRam = (state & 1) != 0;
        hiRam = (state & 2) != 0;
        charEn = (state & 4) != 0;

        updateMappingPHI2();
    }

    private void updateMappingPHI2() {
        cpuReadMap[0xe] = cpuReadMap[0xf] = hiRam ? kernalRomBank : ramBank;
        cpuReadMap[0xa] = cpuReadMap[0xb] = (loRam && hiRam) ? basicRomBank : ramBank;

        if (charEn && (loRam || hiRam)) {
            cpuReadMap[0xd] = cpuWriteMap[0xd] = ioBank;
        } else {
            cpuReadMap[0xd] = (!charEn && (loRam || hiRam)) ? characterRomBank : ramBank;
            cpuWriteMap[0xd] = ramBank;
        }
    }

    public void reset() {
        ramBank.reset();
        zeroRAMBank.reset();

        // Reset the ROMs to undo the hacks applied
        kernalRomBank.reset();
        basicRomBank.reset();

        loRam = false;
        hiRam = false;
        charEn = false;

        updateMappingPHI2();
    }
}
