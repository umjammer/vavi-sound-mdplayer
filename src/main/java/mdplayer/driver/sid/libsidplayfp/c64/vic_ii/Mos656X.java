/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2015 Leandro Nini <drfiemost@users.sourceforge.net>
 * Copyright 2009-2014 VICE Project
 * Copyright 2007-2010 Antti Lankila
 * Copyright 2001 Simon White
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

package mdplayer.driver.sid.libsidplayfp.c64.vic_ii;

import java.util.function.Supplier;

import mdplayer.driver.sid.libsidplayfp.Event;
import mdplayer.driver.sid.libsidplayfp.EventCallback;
import mdplayer.driver.sid.libsidplayfp.EventScheduler;
import mdplayer.driver.sid.libsidplayfp.EventScheduler.EventPhase;
import mdplayer.driver.sid.Mem;


/**
 * MOS 6567/6569/6572 emulation.
 * Not cycle exact but good enough for Sid playback.
 * <p>
 * References below are from:
 *     The MOS 6567/6569 video controller (VIC-II)
 *     and its application : the Commodore 64
 *     http://www.uni-mainz.de/~bauec002/VIC-Article.gz
 * 
 * MOS 6572 info taken from http://solidstate.com.ar/wp/?p=200
 */
public class Mos656X extends Event {

    public enum Model {
        /** OLD NTSC CHIP */
        MOS6567R56A,
        /** NTSC-M */
        MOS6567R8,
        /** PAL-B */
        MOS6569,
        /** PAL-N */
        MOS6572
    }

    private interface ClockFunc extends Supplier<Long> {
    }

    private static class data {
        public int rasterLines;
        public int cyclesPerLine;
        public ClockFunc clock;

        public data(int rasterLines, int cyclesPerLine, ClockFunc clock) {
            this.rasterLines = rasterLines;
            this.cyclesPerLine = cyclesPerLine;
            this.clock = clock;
        }
    }

    /** raster IRQ flag */
    private static final int IRQ_RASTER = 1 << 0;

    /** Light-Pen IRQ flag */
    private static final int IRQ_LIGHTPEN = 1 << 3;

    /** First line when we check for bad lines */
    private static final int FIRST_DMA_LINE = 0x30;

    /** Last line when we check for bad lines */
    private static final int LAST_DMA_LINE = 0xf7;


    /** Current model clock function. */
    private ClockFunc clock;

    /** Current raster clock. */
    private long rasterClk;

    /** System's event scheduler. */
    private EventScheduler eventScheduler;

    /** Number of cycles per line. */
    private int cyclesPerLine;

    /** Number of raster lines. */
    private int maxRasters;

    /** Current visible line */
    private int lineCycle;

    /** current raster line */
    private int rasterY;

    /** vertical scrolling value */
    private int yscroll;

    /** are bad lines enabled for this frame? */
    private boolean areBadLinesEnabled;

    /** instanceof the current line a bad line */
    private boolean isBadLine;

    /** Is rasterYIRQ condition true? */
    private boolean rasterYIRQCondition;

    /** Set when new frame starts. */
    private boolean vblanking;

    /** Is CIA asserting lightpen? */
    private boolean lpAsserted;

    /** private IRQ Flags */
    private byte irqFlags;

    /** masks for the IRQ Flags */
    private byte irqMask;

    /** Light pen */
    private Lightpen lp = new Lightpen();

    /** the 8 sprites data */
    private Sprites sprites;

    /** memory for chips registers */
    private byte[] regs = new byte[0x40];

    private EventCallback<Mos656X> badLineStateChangeEvent;

    private EventCallback<Mos656X> rasterYIRQEdgeDetectorEvent;

    /**
     * AEC state was updated.
     */
    private void badLineStateChange() {
        setBA(!isBadLine);
    }

    /**
     * RasterY IRQ edge detector.
     */
    private void rasterYIRQEdgeDetector() {
        boolean oldRasterYIRQCondition = rasterYIRQCondition;
        rasterYIRQCondition = rasterY == readRasterLineIRQ();
        if (!oldRasterYIRQCondition && rasterYIRQCondition)
            activateIRQFlag(IRQ_RASTER);
    }

    /**
     * Set an IRQ flag and trigger an IRQ if the corresponding IRQ mask instanceof set.
     * The IRQ only gets activated, i.e. flag 0x80 gets set, if it was not active before.
     */
    private void activateIRQFlag(int flag) {
        irqFlags |= (byte) flag;
        handleIrqState();
    }

    /**
     * Read the value of the raster line IRQ
     *
     * @return raster line when to trigger an IRQ
     */
    private int readRasterLineIRQ() {
        return (regs[0x12] & 0xff) + ((regs[0x11] & 0x80) << 1);
    }

    /**
     * Read the DEN flag which tells whether the display instanceof enabled
     *
     * @return true if DEN instanceof set, otherwise false
     */
    private boolean readDEN() {
        return (regs[0x11] & 0x10) != 0;
    }

    private boolean evaluateIsBadLine() {
        return areBadLinesEnabled
                && rasterY >= FIRST_DMA_LINE
                && rasterY <= LAST_DMA_LINE
                && (rasterY & 7) == yscroll;
    }

    /**
     * Get previous value of y raster
     */
    private int oldRasterY() {
        return (rasterY > 0 ? rasterY : maxRasters) - 1;
    }

    private void sync() {
        eventScheduler.cancel(this);
        event();
    }

    /**
     * Check for vertical blanking.
     */
    private void checkVblank() {
        // IRQ occurred (xraster != 0)
        if (rasterY == (maxRasters - 1)) {
            vblanking = true;
        }

        // Check DEN bit on first cycle of the line following the first DMA line
        if (rasterY == FIRST_DMA_LINE
                && !areBadLinesEnabled
                && readDEN()) {
            areBadLinesEnabled = true;
        }

        // Disallow bad lines after the last possible one has passed
        if (rasterY == LAST_DMA_LINE) {
            areBadLinesEnabled = false;
        }

        isBadLine = false;

        if (!vblanking) {
            rasterY++;
            rasterYIRQEdgeDetector();
        }

        if (evaluateIsBadLine())
            isBadLine = true;
    }

    /**
     * Vertical blank (line 0).
     */
    private void vblank() {
        if (vblanking) {
            vblanking = false;
            rasterY = 0;
            rasterYIRQEdgeDetector();
            lp.untrigger();
            if (lpAsserted && lp.retrigger(lineCycle, rasterY)) {
                activateIRQFlag(IRQ_LIGHTPEN);
            }
        }
    }

    /**
     * Start DMA for sprite 0.
     */
    public void startDma(int n) {
        if (n == 0) setBA(!sprites.isDma(0x01));
        else {
            if (sprites.isDma(0x01 << n))
                setBA(false);
        }
    }

    /**
     * End DMA for sprite 7.
     */
    public void endDma(int n) {
        if (n == 7) setBA(true);
        else {
            if (!sprites.isDma(0x06 << n))
                setBA(true);
        }
    }

    /**
     * Start bad line.
     */
    private void startBadline() {
        if (isBadLine)
            setBA(false);
    }

    // Environment Interface

    protected void interrupt(boolean state) {
    }

    protected void setBA(boolean state) {
    }

    // Cycle at which the VIC takes the bus : a bad line (BA goes low).
    private static final int VICII_FETCH_CYCLE = 11;

    private static final int VICII_SCREEN_TEXTCOLS = 40;

    private data[] modelData = null;

    public String credits() {
        return
                """
                        MOS6567/6569/6572 (VIC II) Emulation:
                        \tCopyright (C) 2001 Simon White
                        \tCopyright (C) 2007-2010 Antti Lankila
                        \tCopyright (C) 2009-2014 VICE Project
                        \tCopyright (C) 2011-2016 Leandro Nini
                        """;
    }


    protected Mos656X(EventScheduler scheduler) {
        super("VIC Raster");
        modelData = new data[] {
                new data(262, 64, this::clockOldNTSC), // Old NTSC (MOS6567R56A)
                new data(263, 65, this::clockNTSC),    // NTSC-M   (MOS6567R8)
                new data(312, 63, this::clockPAL),     // PAL-B    (MOS6569R1, MOS6569R3)
                new data(312, 65, this::clockNTSC),    // PAL-N    (MOS6572)
        };

        eventScheduler = (scheduler);
        sprites = new Sprites(regs);
        badLineStateChangeEvent = new EventCallback<>("Update AEC signal", this, this::badLineStateChange);
        rasterYIRQEdgeDetectorEvent = new EventCallback<>("RasterY changed", this, this::rasterYIRQEdgeDetector);
        chip(Model.MOS6569);
    }

    /**
     * Reset VIC II.
     */
    public void reset() {
        irqFlags = 0;
        irqMask = 0;
        yscroll = 0;
        rasterY = maxRasters - 1;
        lineCycle = 0;
        areBadLinesEnabled = false;
        isBadLine = false;
        rasterYIRQCondition = false;
        rasterClk = 0;
        vblanking = false;
        lpAsserted = false;

        Mem.memset(regs, (byte) 0, regs.length);

        lp.reset();
        sprites.reset();

        eventScheduler.cancel(this);
        eventScheduler.schedule(this, 0, EventPhase.CLOCK_PHI1);
    }

    /**
     * Set chips model.
     */
    public void chip(Model model) {
        maxRasters = modelData[model.ordinal()].rasterLines;
        cyclesPerLine = modelData[model.ordinal()].cyclesPerLine;
        clock = modelData[model.ordinal()].clock;

        lp.setScreenSize(maxRasters, cyclesPerLine);

        reset();
    }

    /**
     * Read VIC register.
     *
     * @param addr Register to read.
     */
    protected byte read(byte addr) {
        addr &= 0x3f;

        // Sync up timers
        sync();

        switch (addr) {
        case 0x11:
            // Control register 1
            return (byte) ((regs[addr] & 0x7f) | (byte) ((rasterY & 0x100) >> 1));
        case 0x12:
            // Raster counter
            return (byte) (rasterY & 0xff);
        case 0x13:
            return lp.getX();
        case 0x14:
            return lp.getY();
        case 0x19:
            // Interrupt Pending Register
            return (byte) (irqFlags | 0x70);
        case 0x1a:
            // Interrupt Mask Register
            return (byte) (irqMask | 0xf0);
        default:
            // for addresses < $20 read from register directly
            if (addr < 0x20)
                return regs[addr];
            // for addresses < $2f set bits of high nibble to 1
            if (addr < 0x2f)
                return (byte) (regs[addr] | 0xf0);
            // for addresses >= $2f return $ff
            return (byte) 0xff;
        }
    }

    /**
     * Write to VIC register.
     *
     * @param addr Register to write to.
     * @param data data byte to write.
     */
    protected void write(byte addr, byte data) {
        addr &= 0x3f;

        regs[addr] = data;

        // Sync up timers
        sync();

        switch (addr) {
        case 0x11: // Control register 1
        {
            int oldYScroll = yscroll;
            yscroll = data & 0x7;

            // This instanceof the funniest part... handle bad line tricks.
            boolean wasBadLinesEnabled = areBadLinesEnabled;

            if (rasterY == FIRST_DMA_LINE && lineCycle == 0) {
                areBadLinesEnabled = readDEN();
            }

            if (oldRasterY() == FIRST_DMA_LINE && readDEN()) {
                areBadLinesEnabled = true;
            }

            if ((oldYScroll != yscroll || areBadLinesEnabled != wasBadLinesEnabled)
                    && rasterY >= FIRST_DMA_LINE
                    && rasterY <= LAST_DMA_LINE) {
                // Check whether bad line state has changed.
                boolean wasBadLine = (wasBadLinesEnabled && (oldYScroll == (rasterY & 7)));
                boolean nowBadLine = (areBadLinesEnabled && (yscroll == (rasterY & 7)));

                if (nowBadLine != wasBadLine) {
                    boolean oldBadLine = isBadLine;

                    if (wasBadLine) {
                        if (lineCycle < VICII_FETCH_CYCLE) {
                            isBadLine = false;
                        }
                    } else {
                        // Bad line may be generated during fetch interval
                        //   (VICII_FETCH_CYCLE <= lineCycle < VICII_FETCH_CYCLE + VICII_SCREEN_TEXTCOLS + 3)
                        // or outside the fetch interval but before raster ycounter instanceof incremented
                        //   (lineCycle <= VICII_FETCH_CYCLE + VICII_SCREEN_TEXTCOLS + 6)
                        if (lineCycle <= VICII_FETCH_CYCLE + VICII_SCREEN_TEXTCOLS + 6) {
                            isBadLine = true;
                        }
                    }

                    if (isBadLine != oldBadLine)
                        eventScheduler.schedule(badLineStateChangeEvent, 0, EventPhase.CLOCK_PHI1);
                }
            }
        }
        // fall-through
        eventScheduler.schedule(rasterYIRQEdgeDetectorEvent, 0, EventPhase.CLOCK_PHI1);
        break;

        case 0x12: // Raster counter
            // check raster y irq condition changes at the next PHI1
            eventScheduler.schedule(rasterYIRQEdgeDetectorEvent, 0, EventPhase.CLOCK_PHI1);
            break;

        case 0x17:
            sprites.lineCrunch(data, lineCycle);
            break;

        case 0x19:
            // VIC Interrupt Flag Register
            irqFlags &= (byte) ((~data & 0x0f) | 0x80);
            handleIrqState();
            break;

        case 0x1a:
            // IRQ Mask Register
            irqMask = (byte) (data & 0x0f);
            handleIrqState();
            break;
        }
    }

    /**
     * Signal CPU Interrupt if requested by VIC.
     */
    private void handleIrqState() {
        // signal an IRQ unless we already signaled it
        if ((irqFlags & irqMask & 0x0f) != 0) {
            if ((irqFlags & 0x80) == 0) {
                interrupt(true);
                irqFlags |= 0x80;
            }
        } else {
            if ((irqFlags & 0x80) != 0) {
                interrupt(false);
                irqFlags &= 0x7f;
            }
        }
    }

    @Override
    public void event() {
        long cycles = eventScheduler.getTime(rasterClk, eventScheduler.phase());

        long delay;

        if (cycles != 0) {
            // Update x raster
            rasterClk += cycles;
            lineCycle += (int) cycles;
            lineCycle %= cyclesPerLine;

            delay = (this.clock).get();
        } else
            delay = 1;

        eventScheduler.schedule(this, (int) (delay - eventScheduler.phase().ordinal()), EventPhase.CLOCK_PHI1);
    }

    private long clockPAL() {
        long delay = 1;

        switch (lineCycle) {
        case 0:
            checkVblank();
            endDma(2);
            break;

        case 1:
            vblank();
            startDma(5);

            // No sprites before next compulsory cycle
            if (!sprites.isDma(0xf8))
                delay = 10;
            break;

        case 2:
            endDma(3);
            break;

        case 3:
            startDma(6);
            break;

        case 4:
            endDma(4);
            break;

        case 5:
            startDma(7);
            break;

        case 6:
            endDma(5);

            delay = sprites.isDma(0xc0) ? 2 : 4;
            break;

        case 7:
            break;

        case 8:
            endDma(6);

            delay = 2;
            break;

        case 9:
            break;

        case 10:
            endDma(7);
            break;

        case 11:
            startBadline();

            delay = 3;
            break;

        case 12:
            delay = 2;
            break;

        case 13:
            break;

        case 14:
            sprites.updateMc();
            break;

        case 15:
            sprites.updateMcBase();

            delay = 39;
            break;

        case 54:
            sprites.checkDma(rasterY, regs);
            startDma(0);
            break;

        case 55:
            sprites.checkDma(rasterY, regs);    // Phi1
            sprites.checkExp();                 // Phi2
            startDma(0);
            break;

        case 56:
            startDma(1);
            break;

        case 57:
            sprites.checkDisplay();

            // No sprites before next compulsory cycle
            if (!sprites.isDma(0x1f))
                delay = 6;
            break;

        case 58:
            startDma(2);
            break;

        case 59:
            endDma(0);
            break;

        case 60:
            startDma(3);
            break;

        case 61:
            endDma(1);
            break;

        case 62:
            startDma(4);
            break;

        default:
            delay = 54 - lineCycle;
            break;
        }

        return delay;
    }

    private long clockNTSC() {
        long delay = 1;

        switch (lineCycle) {
        case 0:
            checkVblank();
            startDma(5);
            break;

        case 1:
            vblank();
            endDma(3);

            // No sprites before next compulsory cycle
            if (!sprites.isDma(0xf8))
                delay = 10;
            break;

        case 2:
            startDma(6);
            break;

        case 3:
            endDma(4);
            break;

        case 4:
            startDma(7);
            break;

        case 5:
            endDma(5);

            delay = sprites.isDma(0xc0) ? 2 : 4;
            break;

        case 6:
            break;

        case 7:
            endDma(6);

            delay = 2;
            break;

        case 8:
            break;

        case 9:
            endDma(7);

            delay = 2;
            break;

        case 10:
            break;

        case 11:
            startBadline();

            delay = 3;
            break;

        case 12:
            delay = 2;
            break;

        case 13:
            break;

        case 14:
            sprites.updateMc();
            break;

        case 15:
            sprites.updateMcBase();

            delay = 40;
            break;

        case 55:
            sprites.checkDma(rasterY, regs);    // Phi1
            sprites.checkExp();                 // Phi2
            startDma(0);
            break;

        case 56:
            sprites.checkDma(rasterY, regs);
            startDma(0);
            break;

        case 57:
            startDma(1);
            break;

        case 58:
            sprites.checkDisplay();

            // No sprites before next compulsory cycle
            if (!sprites.isDma(0x1f))
                delay = 7;
            break;

        case 59:
            startDma(2);
            break;

        case 60:
            endDma(0);
            break;

        case 61:
            startDma(3);
            break;

        case 62:
            endDma(1);
            break;

        case 63:
            startDma(4);
            break;

        case 64:
            endDma(2);
            break;

        default:
            delay = 55 - lineCycle;
            break;
        }

        return delay;
    }

    private long clockOldNTSC() {
        long delay = 1;

        switch (lineCycle) {
        case 0:
            checkVblank();
            endDma(2);
            break;

        case 1:
            vblank();
            startDma(5);

            // No sprites before next compulsory cycle
            if (!sprites.isDma(0xf8))
                delay = 10;
            break;

        case 2:
            endDma(3);
            break;

        case 3:
            startDma(6);
            break;

        case 4:
            endDma(4);
            break;

        case 5:
            startDma(7);
            break;

        case 6:
            endDma(5);

            delay = sprites.isDma(0xc0) ? 2 : 4;
            break;

        case 7:
            break;

        case 8:
            endDma(6);

            delay = 2;
            break;

        case 9:
            break;

        case 10:
            endDma(7);
            break;

        case 11:
            startBadline();

            delay = 3;
            break;

        case 12:
            delay = 2;
            break;

        case 13:
            break;

        case 14:
            sprites.updateMc();
            break;

        case 15:
            sprites.updateMcBase();

            delay = 40;
            break;

        case 55:
            sprites.checkDma(rasterY, regs);    // Phi1
            sprites.checkExp();                 // Phi2
            startDma(0);
            break;

        case 56:
            sprites.checkDma(rasterY, regs);
            startDma(0);
            break;

        case 57:
            sprites.checkDisplay();
            startDma(1);

            // No sprites before next compulsory cycle
            delay = (!sprites.isDma(0x1f)) ? 7 : 2;
            break;

        case 58:
            break;

        case 59:
            startDma(2);
            break;

        case 60:
            endDma(0);
            break;

        case 61:
            startDma(3);
            break;

        case 62:
            endDma(1);
            break;

        case 63:
            startDma(4);
            break;

        default:
            delay = 55 - lineCycle;
            break;
        }

        return delay;
    }

    /**
     * Trigger the lightpen. Sets the lightpen usage flag.
     */
    public void triggerLightpen() {
        // Synchronise simulation
        sync();

        lpAsserted = true;

        if (lp.trigger(lineCycle, rasterY)) {
            activateIRQFlag(IRQ_LIGHTPEN);
        }
    }

    /**
     * Clears the lightpen usage flag.
     */
    public void clearLightpen() {
        lpAsserted = false;
    }
}
