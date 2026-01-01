/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package mdplayer.chips;

import mdplayer.Audio;
import mdplayer.Chip;
import mdsound.Instrument;
import mdsound.instrument.Cs4231Inst;


/**
 * Cs4231Chip.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-12-29 nsano initial version <br>
 */
public class Cs4231Chip implements Chip {

    private Audio context;

    @Override
    public Class<? extends Instrument>[] implementations() {
        return new Class[] {Cs4231Inst.class};
    }

    @Override
    public void init(Audio context) {
        this.context = context;
    }

    @Override
    public void reset() {

    }

    @Override
    public void updateVol() {

    }

    public void setFifoBuf(int chipId, byte[] buf) {
        context.mds.inst(Cs4231Inst.class).setFifoBuf(chipId, buf);
    }

    public void setFifoBuf(int chipIndex, int chipId, byte[] buf) {
        context.mds.inst(Cs4231Inst.class, chipIndex).setFifoBuf(chipId, buf);
    }

    public byte[] EMS_GetCurrentMapBuf(int chipId) {
        return context.mds.inst(Cs4231Inst.class).EMS_GetCurrentMapBuf(chipId);
    }

    public byte[] EMS_GetCurrentMapBuf(int chipIndex, int chipId) {
        return context.mds.inst(Cs4231Inst.class, chipIndex).EMS_GetCurrentMapBuf(chipId);
    }

    public void EMS_Map(int chipId, byte al, byte[] ah, int bx, int dx) {
        context.mds.inst(Cs4231Inst.class).EMS_Map(chipId, al, ah, bx, dx);
    }

    public void EMS_Map(int chipIndex, int chipId, int al, byte[] ah, int bx, int dx) {
        context.mds.inst(Cs4231Inst.class, chipIndex).EMS_Map(chipId, al, ah, bx, dx);
    }

    public int EMS_GetPageMap(int chipId) {
        return context.mds.inst(Cs4231Inst.class).EMS_GetPageMap(chipId);
    }

    public int EMS_GetPageMap(int chipIndex, int chipId) {
        return context.mds.inst(Cs4231Inst.class, chipIndex).EMS_GetPageMap(chipId);
    }

    public void EMS_GetHandleName(int chipId, byte[] ah, int dx, String[] buf) {
        context.mds.inst(Cs4231Inst.class).EMS_GetHandleName(chipId, ah, dx, buf);
    }

    public void EMS_GetHandleName(int chipIndex, int chipId, byte[] ah, int dx, String[] buf) {
        context.mds.inst(Cs4231Inst.class, chipIndex).EMS_GetHandleName(chipId, ah, dx, buf);
    }

    public void EMS_SetHandleName(int chipId, byte[] ah, int dx, String buf) {
        context.mds.inst(Cs4231Inst.class).EMS_SetHandleName(chipId, ah, dx, buf);
    }

    public void EMS_SetHandleName(int chipIndex, int chipId, byte[] ah, int dx, String buf) {
        context.mds.inst(Cs4231Inst.class, chipIndex).EMS_SetHandleName(chipId, ah, dx, buf);
    }

    public void EMS_AllocMemory(int chipId, byte[] ah, int[] dx, int bx) {
        context.mds.inst(Cs4231Inst.class).EMS_AllocMemory(chipId, ah, dx, bx);
    }

    public void EMS_AllocMemory(int chipIndex, int chipId, byte[] ah, int[] dx, int bx) {
        context.mds.inst(Cs4231Inst.class, chipIndex).EMS_AllocMemory(chipId, ah, dx, bx);
    }

//    public void Int0bEnt(int chipId, Runnable act) {
//        context.mds.inst(Cs4231Inst.class).setInt0bEnt(chipId, act);
//    }
//
//    public void Int0bEnt(int chipIndex, int chipId, Runnable act) {
//        context.mds.inst(Cs4231Inst.class, chipIndex).setInt0bEnt(chipId, act);
//    }

    public void write(int chipId, int port, int Adr, int Data) {
        context.mds.inst(Cs4231Inst.class).write(chipId, port, Adr, Data);
    }

    public void write(int chipIndex, int chipId, int port, int Adr, int Data) {
        context.mds.inst(Cs4231Inst.class).write(chipId, port, Adr, Data);
    }

    public int read(int chipId, int adr) {
        return context.mds.inst(Cs4231Inst.class).readReg(chipId, adr);
    }

    public int read(int chipIndex, int chipId, int adr) {
        return context.mds.inst(Cs4231Inst.class, chipIndex).readReg(chipId, adr);
    }

    public void mute(byte chipId, byte ch, boolean mute) {
        context.mds.inst(Cs4231Inst.class).mutePcm(chipId, ch, mute);
    }

    public void mute(int chipIndex, byte chipId, byte ch, boolean mute) {
        context.mds.inst(Cs4231Inst.class, chipIndex).mutePcm(chipId, ch, mute);
    }
}
