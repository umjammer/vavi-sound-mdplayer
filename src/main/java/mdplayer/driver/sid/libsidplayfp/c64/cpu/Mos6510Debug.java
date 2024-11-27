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

package mdplayer.driver.sid.libsidplayfp.c64.cpu;


import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


public class Mos6510Debug {

    private static final Logger logger = getLogger(Mos6510Debug.class.getName());

    public void DumpState(long time, Mos6510 cpu) {
//#if false
        logger.log(Level.TRACE, " pc  I  a  x  y  SP  DR PR NV-BDIZC  Instruction (%d)".formatted(time));
//        logger.log(Level.TRACE, "%04x ".formatted(cpu.instrStartPC));
//        logger.log(Level.TRACE, cpu.irqAssertedOnPin ? "t " : "f ");
//        logger.log(Level.TRACE, "%02x ".formatted(cpu.Register_Accumulator));
//        logger.log(Level.TRACE, "%02x ".formatted(cpu.Register_X));
//        logger.log(Level.TRACE, "%02x ".formatted(cpu.Register_Y));
//        logger.log(Level.TRACE, "%02x ".formatted(SidEndian.to16lo8(cpu.Register_StackPointer)));
        logger.log(Level.TRACE, "%02x ".formatted(cpu.cpuRead((short) 0)));
        logger.log(Level.TRACE, "%02x ".formatted(cpu.cpuRead((short) 1)));

//        logger.log(Level.TRACE, cpu.Flags.getN() ? "1" : "0");
//        logger.log(Level.TRACE, cpu.Flags.getV() ? "1" : "0");
        logger.log(Level.TRACE, "1");
//        logger.log(Level.TRACE, cpu.Flags.getB() ? "1" : "0");
//        logger.log(Level.TRACE, cpu.Flags.getD() ? "1" : "0");
//        logger.log(Level.TRACE, cpu.Flags.getI() ? "1" : "0");
//        logger.log(Level.TRACE, cpu.Flags.getZ() ? "1" : "0");
//        logger.log(Level.TRACE, cpu.Flags.getC() ? "1" : "0");

        int opcode = cpu.cpuRead((short) 0/*cpu.instrStartPC*/);

//        logger.log(Level.TRACE, "  %02x ".formatted(opcode));

        switch (opcode) {
        // Accumulator or Implied cpu.Cycle_EffectiveAddressing
        case OpCodes.ASLn:
        case OpCodes.LSRn:
        case OpCodes.ROLn:
        case OpCodes.RORn:
            logger.log(Level.TRACE, "      ");
            break;
        // Zero Page Addressing Mode Handler
        case OpCodes.ADCz:
        case OpCodes.ANDz:
        case OpCodes.ASLz:
        case OpCodes.BITz:
        case OpCodes.CMPz:
        case OpCodes.CPXz:
        case OpCodes.CPYz:
        case OpCodes.DCPz:
        case OpCodes.DECz:
        case OpCodes.EORz:
        case OpCodes.INCz:
        case OpCodes.ISBz:
        case OpCodes.LAXz:
        case OpCodes.LDAz:
        case OpCodes.LDXz:
        case OpCodes.LDYz:
        case OpCodes.LSRz:
        case OpCodes.NOPz:
        case 0x44:
        case 0x64:
        case OpCodes.ORAz:
        case OpCodes.ROLz:
        case OpCodes.RORz:
        case OpCodes.SAXz:
        case OpCodes.SBCz:
        case OpCodes.SREz:
        case OpCodes.STAz:
        case OpCodes.STXz:
        case OpCodes.STYz:
        case OpCodes.SLOz:
        case OpCodes.RLAz:
        case OpCodes.RRAz:
            // ASOz AXSz DCMz INSz LSEz - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x    ".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;
        // Zero Page with x Offset Addressing Mode Handler
        case OpCodes.ADCzx:
        case OpCodes.ANDzx:
        case OpCodes.ASLzx:
        case OpCodes.CMPzx:
        case OpCodes.DCPzx:
        case OpCodes.DECzx:
        case OpCodes.EORzx:
        case OpCodes.INCzx:
        case OpCodes.ISBzx:
        case OpCodes.LDAzx:
        case OpCodes.LDYzx:
        case OpCodes.LSRzx:
        case OpCodes.NOPzx:
        case 0x34:
        case 0x54:
        case 0x74:
        case 0xD4:
        case 0xF4:
        case OpCodes.ORAzx:
        case OpCodes.RLAzx:
        case OpCodes.ROLzx:
        case OpCodes.RORzx:
        case OpCodes.RRAzx:
        case OpCodes.SBCzx:
        case OpCodes.SLOzx:
        case OpCodes.SREzx:
        case OpCodes.STAzx:
        case OpCodes.STYzx:
            // ASOzx DCMzx INSzx LSEzx - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x    ".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;
        // Zero Page with y Offset Addressing Mode Handler
        case OpCodes.LDXzy:
        case OpCodes.STXzy:
        case OpCodes.SAXzy:
        case OpCodes.LAXzy:
            // AXSzx - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x    ".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;
        // Absolute Addressing Mode Handler
        case OpCodes.ADCa:
        case OpCodes.ANDa:
        case OpCodes.ASLa:
        case OpCodes.BITa:
        case OpCodes.CMPa:
        case OpCodes.CPXa:
        case OpCodes.CPYa:
        case OpCodes.DCPa:
        case OpCodes.DECa:
        case OpCodes.EORa:
        case OpCodes.INCa:
        case OpCodes.ISBa:
        case OpCodes.JMPw:
        case OpCodes.JSRw:
        case OpCodes.LAXa:
        case OpCodes.LDAa:
        case OpCodes.LDXa:
        case OpCodes.LDYa:
        case OpCodes.LSRa:
        case OpCodes.NOPa:
        case OpCodes.ORAa:
        case OpCodes.ROLa:
        case OpCodes.RORa:
        case OpCodes.SAXa:
        case OpCodes.SBCa:
        case OpCodes.SLOa:
        case OpCodes.SREa:
        case OpCodes.STAa:
        case OpCodes.STXa:
        case OpCodes.STYa:
        case OpCodes.RLAa:
        case OpCodes.RRAa:
            // ASOa AXSa DCMa INSa LSEa - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x %02x ".formatted(SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand)));
            break;
        // Absolute With x Offset Addresing Mode Handler
        case OpCodes.ADCax:
        case OpCodes.ANDax:
        case OpCodes.ASLax:
        case OpCodes.CMPax:
        case OpCodes.DCPax:
        case OpCodes.DECax:
        case OpCodes.EORax:
        case OpCodes.INCax:
        case OpCodes.ISBax:
        case OpCodes.LDAax:
        case OpCodes.LDYax:
        case OpCodes.LSRax:
        case OpCodes.NOPax:
        case 0x3C:
        case 0x5C:
        case 0x7C:
        case 0xDC:
        case 0xFC:
        case OpCodes.ORAax:
        case OpCodes.RLAax:
        case OpCodes.ROLax:
        case OpCodes.RORax:
        case OpCodes.RRAax:
        case OpCodes.SBCax:
        case OpCodes.SHYax:
        case OpCodes.SLOax:
        case OpCodes.SREax:
        case OpCodes.STAax:
            // ASOax DCMax INSax LSEax SAYax - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x %02x ".formatted(SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand)));
            break;
        // Absolute With y Offset Addresing Mode Handler
        case OpCodes.ADCay:
        case OpCodes.ANDay:
        case OpCodes.CMPay:
        case OpCodes.DCPay:
        case OpCodes.EORay:
        case OpCodes.ISBay:
        case OpCodes.LASay:
        case OpCodes.LAXay:
        case OpCodes.LDAay:
        case OpCodes.LDXay:
        case OpCodes.ORAay:
        case OpCodes.RLAay:
        case OpCodes.RRAay:
        case OpCodes.SBCay:
        case OpCodes.SHAay:
        case OpCodes.SHSay:
        case OpCodes.SHXay:
        case OpCodes.SLOay:
        case OpCodes.SREay:
        case OpCodes.STAay:
            // ASOay AXAay DCMay INSax LSEay TASay XASay - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x %02x ".formatted(SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand)));
            break;
        // Immediate and Relative Addressing Mode Handler
        case OpCodes.ADCb:
        case OpCodes.ANDb:
        case OpCodes.ANCb:
        case 0x2B:
        case OpCodes.ANEb:
        case OpCodes.ASRb:
        case OpCodes.ARRb:
        case OpCodes.BCCr:
        case OpCodes.BCSr:
        case OpCodes.BEQr:
        case OpCodes.BMIr:
        case OpCodes.BNEr:
        case OpCodes.BPLr:
        case OpCodes.BVCr:
        case OpCodes.BVSr:
        case OpCodes.CMPb:
        case OpCodes.CPXb:
        case OpCodes.CPYb:
        case OpCodes.EORb:
        case OpCodes.LDAb:
        case OpCodes.LDXb:
        case OpCodes.LDYb:
        case OpCodes.LXAb:
        case OpCodes.NOPb:
        case 0x82:
        case 0xC2:
        case 0xE2:
        case 0x89:
        case OpCodes.ORAb:
        case OpCodes.SBCb:
        case 0XEB:
        case OpCodes.SBXb:
            // OALb ALRb XAAb - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x    ".formatted(SidEndian.to16lo8(cpu.Cycle_Data)));
            break;
        // Indirect Addressing Mode Handler
        case OpCodes.JMPi:
//            logger.log(Level.TRACE, "%02x %02x ".formatted(SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand)));
            break;
        // Indexed with x Preinc Addressing Mode Handler
        case OpCodes.ADCix:
        case OpCodes.ANDix:
        case OpCodes.CMPix:
        case OpCodes.DCPix:
        case OpCodes.EORix:
        case OpCodes.ISBix:
        case OpCodes.LAXix:
        case OpCodes.LDAix:
        case OpCodes.ORAix:
        case OpCodes.SAXix:
        case OpCodes.SBCix:
        case OpCodes.SLOix:
        case OpCodes.SREix:
        case OpCodes.STAix:
        case OpCodes.RLAix:
        case OpCodes.RRAix:
            // ASOix AXSix DCMix INSix LSEix - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x    ".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;
        // Indexed with y Postinc Addressing Mode Handler
        case OpCodes.ADCiy:
        case OpCodes.ANDiy:
        case OpCodes.CMPiy:
        case OpCodes.DCPiy:
        case OpCodes.EORiy:
        case OpCodes.ISBiy:
        case OpCodes.LAXiy:
        case OpCodes.LDAiy:
        case OpCodes.ORAiy:
        case OpCodes.RLAiy:
        case OpCodes.RRAiy:
        case OpCodes.SBCiy:
        case OpCodes.SHAiy:
        case OpCodes.SLOiy:
        case OpCodes.SREiy:
        case OpCodes.STAiy:
            // AXAiy ASOiy LSEiy DCMiy INSiy - Optional Opcode Names
//            logger.log(Level.TRACE, "%02x    ".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;
        default:
            logger.log(Level.TRACE, "      ");
            break;
        }

        switch (opcode) {
        case OpCodes.ADCb:
        case OpCodes.ADCz:
        case OpCodes.ADCzx:
        case OpCodes.ADCa:
        case OpCodes.ADCax:
        case OpCodes.ADCay:
        case OpCodes.ADCix:
        case OpCodes.ADCiy:
            logger.log(Level.TRACE, " ADC");
            break;
        case OpCodes.ANCb:
        case 0x2B:
            logger.log(Level.TRACE, "*ANC");
            break;
        case OpCodes.ANDb:
        case OpCodes.ANDz:
        case OpCodes.ANDzx:
        case OpCodes.ANDa:
        case OpCodes.ANDax:
        case OpCodes.ANDay:
        case OpCodes.ANDix:
        case OpCodes.ANDiy:
            logger.log(Level.TRACE, " AND");
            break;
        case OpCodes.ANEb: // Also known as XAA
            logger.log(Level.TRACE, "*ANE");
            break;
        case OpCodes.ARRb:
            logger.log(Level.TRACE, "*ARR");
            break;
        case OpCodes.ASLn:
        case OpCodes.ASLz:
        case OpCodes.ASLzx:
        case OpCodes.ASLa:
        case OpCodes.ASLax:
            logger.log(Level.TRACE, " ASL");
            break;
        case OpCodes.ASRb: // Also known as ALR
            logger.log(Level.TRACE, "*ASR");
            break;
        case OpCodes.BCCr:
            logger.log(Level.TRACE, " BCC");
            break;
        case OpCodes.BCSr:
            logger.log(Level.TRACE, " BCS");
            break;
        case OpCodes.BEQr:
            System.err.
                    print(" BEQ");
            break;
        case OpCodes.BITz:
        case OpCodes.BITa:
            logger.log(Level.TRACE, " BIT");
            break;
        case OpCodes.BMIr:
            logger.log(Level.TRACE, " BMI");
            break;
        case OpCodes.BNEr:
            logger.log(Level.TRACE, " BNE");
            break;
        case OpCodes.BPLr:
            logger.log(Level.TRACE, " BPL");
            break;
        case OpCodes.BRKn:
            logger.log(Level.TRACE, " BRK");
            break;
        case OpCodes.BVCr:
            logger.log(Level.TRACE, " BVC");
            break;
        case OpCodes.BVSr:
            System.err.
                    print(" BVS");
            break;
        case OpCodes.CLCn:
            logger.log(Level.TRACE, " CLC");
            break;
        case OpCodes.CLDn:
            logger.log(Level.TRACE, " CLD");
            break;
        case OpCodes.CLIn:
            logger.log(Level.TRACE, " CLI");
            break;
        case OpCodes.CLVn:
            logger.log(Level.TRACE, " CLV");
            break;
        case OpCodes.CMPb:
        case OpCodes.CMPz:
        case OpCodes.CMPzx:
        case OpCodes.CMPa:
        case OpCodes.CMPax:
        case OpCodes.CMPay:
        case OpCodes.CMPix:
        case OpCodes.CMPiy:
            logger.log(Level.TRACE, " CMP");
            break;
        case OpCodes.CPXb:
        case OpCodes.CPXz:
        case OpCodes.CPXa:
            logger.log(Level.TRACE, " CPX");
            break;
        case OpCodes.CPYb:
        case OpCodes.CPYz:
        case OpCodes.CPYa:
            logger.log(Level.TRACE, " CPY");
            break;
        case OpCodes.DCPz:
        case OpCodes.DCPzx:
        case OpCodes.DCPa:
        case OpCodes.DCPax:
        case OpCodes.DCPay:
        case OpCodes.DCPix:
        case OpCodes.DCPiy: // Also known as DCM
            logger.log(Level.TRACE, "*DCP");
            break;
        case OpCodes.DECz:
        case OpCodes.DECzx:
        case OpCodes.DECa:
        case OpCodes.DECax:
            logger.log(Level.TRACE, " DEC");
            break;
        case OpCodes.DEXn:
            logger.log(Level.TRACE, " DEX");
            break;
        case OpCodes.DEYn:
            logger.log(Level.TRACE, " DEY");
            break;
        case OpCodes.EORb:
        case OpCodes.EORz:
        case OpCodes.EORzx:
        case OpCodes.EORa:
        case OpCodes.EORax:
        case OpCodes.EORay:
        case OpCodes.EORix:
        case OpCodes.EORiy:
            logger.log(Level.TRACE, " EOR");
            break;
        case OpCodes.INCz:
        case OpCodes.INCzx:
        case OpCodes.INCa:
        case OpCodes.INCax:
            logger.log(Level.TRACE, " INC");
            break;
        case OpCodes.INXn:
            logger.log(Level.TRACE, " INX");
            break;
        case OpCodes.INYn:
            logger.log(Level.TRACE, " INY");
            break;
        case OpCodes.ISBz:
        case OpCodes.ISBzx:
        case OpCodes.ISBa:
        case OpCodes.ISBax:
        case OpCodes.ISBay:
        case OpCodes.ISBix:
        case OpCodes.ISBiy: // Also known as INS
            logger.log(Level.TRACE, "*ISB");
            break;
        case OpCodes.JMPw:
        case OpCodes.JMPi:
            logger.log(Level.TRACE, " JMP");
            break;
        case OpCodes.JSRw:
            logger.log(Level.TRACE, " JSR");
            break;
        case OpCodes.LASay:
            logger.log(Level.TRACE, "*LAS");
            break;
        case OpCodes.LAXz:
        case OpCodes.LAXzy:
        case OpCodes.LAXa:
        case OpCodes.LAXay:
        case OpCodes.LAXix:
        case OpCodes.LAXiy:
            logger.log(Level.TRACE, "*LAX");
            break;
        case OpCodes.LDAb:
        case OpCodes.LDAz:
        case OpCodes.LDAzx:
        case OpCodes.LDAa:
        case OpCodes.LDAax:
        case OpCodes.LDAay:
        case OpCodes.LDAix:
        case OpCodes.LDAiy:
            logger.log(Level.TRACE, " LDA");
            break;
        case OpCodes.LDXb:
        case OpCodes.LDXz:
        case OpCodes.LDXzy:
        case OpCodes.LDXa:
        case OpCodes.LDXay:
            logger.log(Level.TRACE, " LDX");
            break;
        case OpCodes.LDYb:
        case OpCodes.LDYz:
        case OpCodes.LDYzx:
        case OpCodes.LDYa:
        case OpCodes.LDYax:
            logger.log(Level.TRACE, " LDY");
            break;
        case OpCodes.LSRz:
        case OpCodes.LSRzx:
        case OpCodes.LSRa:
        case OpCodes.LSRax:
        case OpCodes.LSRn:
            logger.log(Level.TRACE, " LSR");
            break;
        case OpCodes.NOPn:
        case 0x1A:
        case 0x3A:
        case 0x5A:
        case 0x7A:
        case 0xDA:
        case 0xFA:
        case OpCodes.NOPb:
        case 0x82:
        case 0xC2:
        case 0xE2:
        case 0x89:
        case OpCodes.NOPz:
        case 0x44:
        case 0x64:
        case OpCodes.NOPzx:
        case 0x34:
        case 0x54:
        case 0x74:
        case 0xD4:
        case 0xF4:
        case OpCodes.NOPa:
        case OpCodes.NOPax:
        case 0x3C:
        case 0x5C:
        case 0x7C:
        case 0xDC:
        case 0xFC:
            if (opcode != OpCodes.NOPn) logger.log(Level.TRACE, "*");
            else logger.log(Level.TRACE, " ");
            logger.log(Level.TRACE, "NOP");
            break;
        case OpCodes.LXAb: // Also known as OAL
            logger.log(Level.TRACE, "*LXA");
            break;
        case OpCodes.ORAb:
        case OpCodes.ORAz:
        case OpCodes.ORAzx:
        case OpCodes.ORAa:
        case OpCodes.ORAax:
        case OpCodes.ORAay:
        case OpCodes.ORAix:
        case OpCodes.ORAiy:
            logger.log(Level.TRACE, " ORA");
            break;
        case OpCodes.PHAn:
            logger.log(Level.TRACE, " PHA");
            break;
        case OpCodes.PHPn:
            logger.log(Level.TRACE, " PHP");
            break;
        case OpCodes.PLAn:
            logger.log(Level.TRACE, " PLA");
            break;
        case OpCodes.PLPn:
            logger.log(Level.TRACE, " PLP");
            break;
        case OpCodes.RLAz:
        case OpCodes.RLAzx:
        case OpCodes.RLAix:
        case OpCodes.RLAa:
        case OpCodes.RLAax:
        case OpCodes.RLAay:
        case OpCodes.RLAiy:
            logger.log(Level.TRACE, "*RLA");
            break;
        case OpCodes.ROLz:
        case OpCodes.ROLzx:
        case OpCodes.ROLa:
        case OpCodes.ROLax:
        case OpCodes.ROLn:
            logger.log(Level.TRACE, " ROL");
            break;
        case OpCodes.RORz:
        case OpCodes.RORzx:
        case OpCodes.RORa:
        case OpCodes.RORax:
        case OpCodes.RORn:
            logger.log(Level.TRACE, " ROR");
            break;
        case OpCodes.RRAa:
        case OpCodes.RRAax:
        case OpCodes.RRAay:
        case OpCodes.RRAz:
        case OpCodes.RRAzx:
        case OpCodes.RRAix:
        case OpCodes.RRAiy:
            logger.log(Level.TRACE, "*RRA");
            break;
        case OpCodes.RTIn:
            logger.log(Level.TRACE, " RTI");
            break;
        case OpCodes.RTSn:
            logger.log(Level.TRACE, " RTS");
            break;
        case OpCodes.SAXz:
        case OpCodes.SAXzy:
        case OpCodes.SAXa:
        case OpCodes.SAXix: // Also known as AXS
            logger.log(Level.TRACE, "*SAX");
            break;
        case OpCodes.SBCb:
        case 0XEB:
            if (opcode != OpCodes.SBCb) logger.log(Level.TRACE, "*");
            else logger.log(Level.TRACE, " ");
            logger.log(Level.TRACE, "SBC");
            break;
        case OpCodes.SBCz:
        case OpCodes.SBCzx:
        case OpCodes.SBCa:
        case OpCodes.SBCax:
        case OpCodes.SBCay:
        case OpCodes.SBCix:
        case OpCodes.SBCiy:
            logger.log(Level.TRACE, " SBC");
            break;
        case OpCodes.SBXb:
            logger.log(Level.TRACE, "*SBX");
            break;
        case OpCodes.SECn:
            logger.log(Level.TRACE, " SEC");
            break;
        case OpCodes.SEDn:
            logger.log(Level.TRACE, " SED");
            break;
        case OpCodes.SEIn:
            logger.log(Level.TRACE, " SEI");
            break;
        case OpCodes.SHAay:
        case OpCodes.SHAiy: // Also known as AXA
            logger.log(Level.TRACE, "*SHA");
            break;
        case OpCodes.SHSay: // Also known as TAS
            logger.log(Level.TRACE, "*SHS");
            break;
        case OpCodes.SHXay: // Also known as XAS
            logger.log(Level.TRACE, "*SHX");
            break;
        case OpCodes.SHYax: // Also known as SAY
            logger.log(Level.TRACE, "*SHY");
            break;
        case OpCodes.SLOz:
        case OpCodes.SLOzx:
        case OpCodes.SLOa:
        case OpCodes.SLOax:
        case OpCodes.SLOay:
        case OpCodes.SLOix:
        case OpCodes.SLOiy: // Also known as ASO
            logger.log(Level.TRACE, "*SLO");
            break;
        case OpCodes.SREz:
        case OpCodes.SREzx:
        case OpCodes.SREa:
        case OpCodes.SREax:
        case OpCodes.SREay:
        case OpCodes.SREix:
        case OpCodes.SREiy: // Also known as LSE
            logger.log(Level.TRACE, "*SRE");
            break;
        case OpCodes.STAz:
        case OpCodes.STAzx:
        case OpCodes.STAa:
        case OpCodes.STAax:
        case OpCodes.STAay:
        case OpCodes.STAix:
        case OpCodes.STAiy:
            logger.log(Level.TRACE, " STA");
            break;
        case OpCodes.STXz:
        case OpCodes.STXzy:
        case OpCodes.STXa:
            logger.log(Level.TRACE, " STX");
            break;
        case OpCodes.STYz:
        case OpCodes.STYzx:
        case OpCodes.STYa:
            logger.log(Level.TRACE, " STY");
            break;
        case OpCodes.TAXn:
            logger.log(Level.TRACE, " TAX");
            break;
        case OpCodes.TAYn:
            logger.log(Level.TRACE, " TAY");
            break;
        case OpCodes.TSXn:
            logger.log(Level.TRACE, " TSX");
            break;
        case OpCodes.TXAn:
            logger.log(Level.TRACE, " TXA");
            break;
        case OpCodes.TXSn:
            logger.log(Level.TRACE, " TXS");
            break;
        case OpCodes.TYAn:
            logger.log(Level.TRACE, " TYA");
            break;
        default:
            logger.log(Level.TRACE, "*HLT");
            break;
        }

        switch (opcode) {
        // Accumulator or Implied cpu.Cycle_EffectiveAddressing
        case OpCodes.ASLn:
        case OpCodes.LSRn:
        case OpCodes.ROLn:
        case OpCodes.RORn:
            logger.log(Level.TRACE, "n  a");
            break;

        // Zero Page Addressing Mode Handler
        case OpCodes.ADCz:
        case OpCodes.ANDz:
        case OpCodes.ASLz:
        case OpCodes.BITz:
        case OpCodes.CMPz:
        case OpCodes.CPXz:
        case OpCodes.CPYz:
        case OpCodes.DCPz:
        case OpCodes.DECz:
        case OpCodes.EORz:
        case OpCodes.INCz:
        case OpCodes.ISBz:
        case OpCodes.LAXz:
        case OpCodes.LDAz:
        case OpCodes.LDXz:
        case OpCodes.LDYz:
        case OpCodes.LSRz:
        case OpCodes.ORAz:

        case OpCodes.ROLz:
        case OpCodes.RORz:
        case OpCodes.SBCz:
        case OpCodes.SREz:
        case OpCodes.SLOz:
        case OpCodes.RLAz:
        case OpCodes.RRAz:
            // ASOz AXSz DCMz INSz LSEz - Optional Opcode Names
//            logger.log(Level.TRACE, "z  %02x %d%02x%d", SidEndian.to16lo8(cpu.instrOperand), cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.SAXz:
        case OpCodes.STAz:
        case OpCodes.STXz:
        case OpCodes.STYz:
        case OpCodes.NOPz:
        case 0x44:
        case 0x64:
//            logger.log(Level.TRACE, "z  %02x".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;

        // Zero Page with x Offset Addressing Mode Handler
        case OpCodes.ADCzx:
        case OpCodes.ANDzx:
        case OpCodes.ASLzx:
        case OpCodes.CMPzx:
        case OpCodes.DCPzx:
        case OpCodes.DECzx:
        case OpCodes.EORzx:
        case OpCodes.INCzx:
        case OpCodes.ISBzx:
        case OpCodes.LDAzx:
        case OpCodes.LDYzx:
        case OpCodes.LSRzx:
        case OpCodes.ORAzx:
        case OpCodes.RLAzx:
        case OpCodes.ROLzx:
        case OpCodes.RORzx:
        case OpCodes.RRAzx:
        case OpCodes.SBCzx:
        case OpCodes.SLOzx:
        case OpCodes.SREzx:
            // ASOzx DCMzx INSzx LSEzx - Optional Opcode Names
//            logger.log(Level.TRACE, "zx %02x,x".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.STAzx:
        case OpCodes.STYzx:
        case OpCodes.NOPzx:
        case 0x34:
        case 0x54:
        case 0x74:
        case 0xD4:
        case 0xF4:
//            logger.log(Level.TRACE, "zx %02x,x".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Zero Page with y Offset Addressing Mode Handler
        case OpCodes.LAXzy:
        case OpCodes.LDXzy:
            // AXSzx - Optional Opcode Names
//            logger.log(Level.TRACE, "zy %02x,y".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.STXzy:
        case OpCodes.SAXzy:
//            logger.log(Level.TRACE, "zy %02x,y".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Absolute Addressing Mode Handler
        case OpCodes.ADCa:
        case OpCodes.ANDa:
        case OpCodes.ASLa:
        case OpCodes.BITa:
        case OpCodes.CMPa:
        case OpCodes.CPXa:
        case OpCodes.CPYa:
        case OpCodes.DCPa:
        case OpCodes.DECa:
        case OpCodes.EORa:
        case OpCodes.INCa:
        case OpCodes.ISBa:
        case OpCodes.LAXa:
        case OpCodes.LDAa:
        case OpCodes.LDXa:
        case OpCodes.LDYa:
        case OpCodes.LSRa:
        case OpCodes.ORAa:
        case OpCodes.ROLa:
        case OpCodes.RORa:
        case OpCodes.SBCa:
        case OpCodes.SLOa:
        case OpCodes.SREa:
        case OpCodes.RLAa:
        case OpCodes.RRAa:
            // ASOa AXSa DCMa INSa LSEa - Optional Opcode Names
//            logger.log(Level.TRACE, "a  %04x %d%02x%d", cpu.instrOperand, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.SAXa:
        case OpCodes.STAa:
        case OpCodes.STXa:
        case OpCodes.STYa:
        case OpCodes.NOPa:
//            logger.log(Level.TRACE, "a  %04x".formatted(cpu.instrOperand));
            break;
        case OpCodes.JMPw:
        case OpCodes.JSRw:
//            logger.log(Level.TRACE, "w  %04x".formatted(cpu.instrOperand));
            break;

        // Absolute With x Offset Addresing Mode Handler
        case OpCodes.ADCax:
        case OpCodes.ANDax:
        case OpCodes.ASLax:
        case OpCodes.CMPax:
        case OpCodes.DCPax:
        case OpCodes.DECax:
        case OpCodes.EORax:
        case OpCodes.INCax:
        case OpCodes.ISBax:
        case OpCodes.LDAax:
        case OpCodes.LDYax:
        case OpCodes.LSRax:
        case OpCodes.ORAax:
        case OpCodes.RLAax:
        case OpCodes.ROLax:
        case OpCodes.RORax:
        case OpCodes.RRAax:
        case OpCodes.SBCax:
        case OpCodes.SLOax:
        case OpCodes.SREax:
            // ASOax DCMax INSax LSEax SAYax - Optional Opcode Names
//            logger.log(Level.TRACE, "ax %04x,x".formatted(cpu.instrOperand));
//            logger.log(Level.TRACE, " [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.SHYax:
        case OpCodes.STAax:
        case OpCodes.NOPax:
        case 0x3C:
        case 0x5C:
        case 0x7C:
        case 0xDC:
        case 0xFC:
//            logger.log(Level.TRACE, "ax %04x,x".formatted(cpu.instrOperand));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Absolute With y Offset Addresing Mode Handler
        case OpCodes.ADCay:
        case OpCodes.ANDay:
        case OpCodes.CMPay:
        case OpCodes.DCPay:
        case OpCodes.EORay:
        case OpCodes.ISBay:
        case OpCodes.LASay:
        case OpCodes.LAXay:
        case OpCodes.LDAay:
        case OpCodes.LDXay:
        case OpCodes.ORAay:
        case OpCodes.RLAay:
        case OpCodes.RRAay:
        case OpCodes.SBCay:
        case OpCodes.SHSay:
        case OpCodes.SLOay:
        case OpCodes.SREay:
            // ASOay AXAay DCMay INSax LSEay TASay XASay - Optional Opcode Names
//            logger.log(Level.TRACE, "ay %04x,y".formatted(cpu.instrOperand));
//            logger.log(Level.TRACE, " [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.SHAay:
        case OpCodes.SHXay:
        case OpCodes.STAay:
//            logger.log(Level.TRACE, "ay %04x,y".formatted(cpu.instrOperand));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Immediate Addressing Mode Handler
        case OpCodes.ADCb:
        case OpCodes.ANDb:
        case OpCodes.ANCb:
        case 0x2B:
        case OpCodes.ANEb:
        case OpCodes.ASRb:
        case OpCodes.ARRb:
        case OpCodes.CMPb:
        case OpCodes.CPXb:
        case OpCodes.CPYb:
        case OpCodes.EORb:
        case OpCodes.LDAb:
        case OpCodes.LDXb:
        case OpCodes.LDYb:
        case OpCodes.LXAb:
        case OpCodes.ORAb:
        case OpCodes.SBCb:
        case 0XEB:
        case OpCodes.SBXb:
            // OALb ALRb XAAb - Optional Opcode Names
        case OpCodes.NOPb:
        case 0x82:
        case 0xC2:
        case 0xE2:
        case 0x89:
//            logger.log(Level.TRACE, "b  //#%02x".formatted(SidEndian.to16lo8(cpu.instrOperand)));
            break;

        // Relative Addressing Mode Handler
        case OpCodes.BCCr:
        case OpCodes.BCSr:
        case OpCodes.BEQr:
        case OpCodes.BMIr:
        case OpCodes.BNEr:
        case OpCodes.BPLr:
        case OpCodes.BVCr:
        case OpCodes.BVSr:
//            logger.log(Level.TRACE, "r  //#%02x".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Indirect Addressing Mode Handler
        case OpCodes.JMPi:
//            logger.log(Level.TRACE, "i  (%04x)".formatted(cpu.instrOperand));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Indexed with x Preinc Addressing Mode Handler
        case OpCodes.ADCix:
        case OpCodes.ANDix:
        case OpCodes.CMPix:
        case OpCodes.DCPix:
        case OpCodes.EORix:
        case OpCodes.ISBix:
        case OpCodes.LAXix:
        case OpCodes.LDAix:
        case OpCodes.ORAix:
        case OpCodes.SBCix:
        case OpCodes.SLOix:
        case OpCodes.SREix:
        case OpCodes.RLAix:
        case OpCodes.RRAix:
            // ASOix AXSix DCMix INSix LSEix - Optional Opcode Names
//            logger.log(Level.TRACE, "ix (%02x,x)".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.SAXix:
        case OpCodes.STAix:
//            logger.log(Level.TRACE, "ix (%02x,x)".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        // Indexed with y Postinc Addressing Mode Handler
        case OpCodes.ADCiy:
        case OpCodes.ANDiy:
        case OpCodes.CMPiy:
        case OpCodes.DCPiy:
        case OpCodes.EORiy:
        case OpCodes.ISBiy:
        case OpCodes.LAXiy:
        case OpCodes.LDAiy:
        case OpCodes.ORAiy:
        case OpCodes.RLAiy:
        case OpCodes.RRAiy:
        case OpCodes.SBCiy:
        case OpCodes.SLOiy:
        case OpCodes.SREiy:
            // AXAiy ASOiy LSEiy DCMiy INSiy - Optional Opcode Names
//            logger.log(Level.TRACE, "iy (%02x),y".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{".formatted("}"));
            break;
        case OpCodes.SHAiy:
        case OpCodes.STAiy:
//            logger.log(Level.TRACE, "iy (%02x),y".formatted(SidEndian.to16lo8(cpu.instrOperand)));
//            logger.log(Level.TRACE, " [%04x]".formatted(cpu.Cycle_EffectiveAddress));
            break;

        default:
            break;
        }

        logger.log(Level.TRACE, "\n\n");
    }
}
