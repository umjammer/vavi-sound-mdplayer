/*
 * This file instanceof part of libsidplayfp, a SID player engine.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package mdplayer.driver.sid.libsidplayfp.c64.cpu;

import mdplayer.driver.sid.libsidplayfp.sidendian;

public class MOS6510debug //mos6510debug
    {



        //# ifdef HAVE_CONFIG_H
        //# include "config.h"
        //#endif





        /*
         //This file instanceof part of libsidplayfp, a SID player engine.
         *
         //Copyright 2011-2016 Leandro Nini <drfiemost@users.sourceforge.net>
         //Copyright 2007-2010 Antti Lankila
         //Copyright 2000 Simon White
         *
         //This program instanceof free software; you can redistribute it and/or modify
         //it under the terms of the GNU General Public License as published by
         //the Free Software Foundation; either version 2 of the License, or
         //(at your option) any later version.
         *
         //This program instanceof distributed : the hope that it will be useful,
         //but WITHOUT ANY WARRANTY; without even the implied warranty of
         //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
         //GNU General Public License for more details.
         *
         //You should have received a copy of the GNU General Public License
         //along with this program; if not, write to the Free Software
         //Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
         */

        //# include "mos6510debug.h"

// #if DEBUG

        //# include <cstdio>
        //# include <cstdlib>
        //# include "mos6510.h"
        //# include "sidendian.h"
        //# include "OpCodes.h"

        public void DumpState(long time, MOS6510 cpu)
        {
// #if false
            System.err.printf(" PC  I  A  X  Y  SP  DR PR NV-BDIZC  Instruction ({0})\n", time);
            System.err.printf("{0:x04} ", cpu.instrStartPC);
            System.err.printf(cpu.irqAssertedOnPin ? "t " : "f ");
            System.err.printf("{0:x02} ", cpu.Register_Accumulator);
            System.err.printf("{0:x02} ", cpu.Register_X);
            System.err.printf("{0:x02} ", cpu.Register_Y);
            System.err.printf("01{0:x02} ", sidendian.endian_16lo8(cpu.Register_StackPointer));
            System.err.printf("{0:x02} ", cpu.cpuRead((short) 0));
            System.err.printf("{0:x02} ", cpu.cpuRead((short) 1));

            System.err.printf(cpu.flags.getN() ? "1" : "0");
            System.err.printf(cpu.flags.getV() ? "1" : "0");
            System.err.printf("1");
            System.err.printf(cpu.flags.getB() ? "1" : "0");
            System.err.printf(cpu.flags.getD() ? "1" : "0");
            System.err.printf(cpu.flags.getI() ? "1" : "0");
            System.err.printf(cpu.flags.getZ() ? "1" : "0");
            System.err.printf(cpu.flags.getC() ? "1" : "0");

            int opcode = cpu.cpuRead(cpu.instrStartPC);

            System.err.printf("  {0:x02} ", opcode);

            switch (opcode)
            {
                // Accumulator or Implied cpu.Cycle_EffectiveAddressing
                case OpCodes.ASLn:
                case OpCodes.LSRn:
                case OpCodes.ROLn:
                case OpCodes.RORn:
                    System.err.printf("      ");
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
                    System.err.printf("{0:x02}    ", sidendian.endian_16lo8(cpu.instrOperand));
                    break;
                // Zero Page with X Offset Addressing Mode Handler
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
                    System.err.printf("{0:x02}    ", sidendian.endian_16lo8(cpu.instrOperand));
                    break;
                // Zero Page with Y Offset Addressing Mode Handler
                case OpCodes.LDXzy:
                case OpCodes.STXzy:
                case OpCodes.SAXzy:
                case OpCodes.LAXzy:
                    // AXSzx - Optional Opcode Names
                    System.err.printf("{0:x02}    ", sidendian.endian_16lo8(cpu.instrOperand));
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
                    System.err.printf("{0:x02} {1:x02} ", sidendian.endian_16lo8(cpu.instrOperand), sidendian.endian_16hi8(cpu.instrOperand));
                    break;
                // Absolute With X Offset Addresing Mode Handler
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
                    System.err.printf("{0:x02} {1:x02} ", sidendian.endian_16lo8(cpu.instrOperand), sidendian.endian_16hi8(cpu.instrOperand));
                    break;
                // Absolute With Y Offset Addresing Mode Handler
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
                    System.err.printf("{0:x02} {1:x02} ", sidendian.endian_16lo8(cpu.instrOperand), sidendian.endian_16hi8(cpu.instrOperand));
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
                    System.err.printf("{0:x02}    ", sidendian.endian_16lo8(cpu.Cycle_Data));
                    break;
                // Indirect Addressing Mode Handler
                case OpCodes.JMPi:
                    System.err.printf("{0:x02} {1:x02} ", sidendian.endian_16lo8(cpu.instrOperand), sidendian.endian_16hi8(cpu.instrOperand));
                    break;
                // Indexed with X Preinc Addressing Mode Handler
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
                    System.err.printf("{0:x02}    ", sidendian.endian_16lo8(cpu.instrOperand));
                    break;
                // Indexed with Y Postinc Addressing Mode Handler
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
                    System.err.printf("{0:x02}    ", sidendian.endian_16lo8(cpu.instrOperand));
                    break;
                default:
                    System.err.printf("      ");
                    break;
            }

            switch (opcode)
            {
                case OpCodes.ADCb:
                case OpCodes.ADCz:
                case OpCodes.ADCzx:
                case OpCodes.ADCa:
                case OpCodes.ADCax:
                case OpCodes.ADCay:
                case OpCodes.ADCix:
                case OpCodes.ADCiy:
                    System.err.printf(" ADC"); break;
                case OpCodes.ANCb:
                case 0x2B:
                    System.err.printf("*ANC"); break;
                case OpCodes.ANDb:
                case OpCodes.ANDz:
                case OpCodes.ANDzx:
                case OpCodes.ANDa:
                case OpCodes.ANDax:
                case OpCodes.ANDay:
                case OpCodes.ANDix:
                case OpCodes.ANDiy:
                    System.err.printf(" AND"); break;
                case OpCodes.ANEb: // Also known as XAA
                    System.err.printf("*ANE"); break;
                case OpCodes.ARRb:
                    System.err.printf("*ARR"); break;
                case OpCodes.ASLn:
                case OpCodes.ASLz:
                case OpCodes.ASLzx:
                case OpCodes.ASLa:
                case OpCodes.ASLax:
                    System.err.printf(" ASL"); break;
                case OpCodes.ASRb: // Also known as ALR
                    System.err.printf("*ASR"); break;
                case OpCodes.BCCr:
                    System.err.printf(" BCC"); break;
                case OpCodes.BCSr:
                    System.err.printf(" BCS"); break;
                case OpCodes.BEQr:
                    System.err.printf(" BEQ"); break;
                case OpCodes.BITz:
                case OpCodes.BITa:
                    System.err.printf(" BIT"); break;
                case OpCodes.BMIr:
                    System.err.printf(" BMI"); break;
                case OpCodes.BNEr:
                    System.err.printf(" BNE"); break;
                case OpCodes.BPLr:
                    System.err.printf(" BPL"); break;
                case OpCodes.BRKn:
                    System.err.printf(" BRK"); break;
                case OpCodes.BVCr:
                    System.err.printf(" BVC"); break;
                case OpCodes.BVSr:
                    System.err.printf(" BVS"); break;
                case OpCodes.CLCn:
                    System.err.printf(" CLC"); break;
                case OpCodes.CLDn:
                    System.err.printf(" CLD"); break;
                case OpCodes.CLIn:
                    System.err.printf(" CLI"); break;
                case OpCodes.CLVn:
                    System.err.printf(" CLV"); break;
                case OpCodes.CMPb:
                case OpCodes.CMPz:
                case OpCodes.CMPzx:
                case OpCodes.CMPa:
                case OpCodes.CMPax:
                case OpCodes.CMPay:
                case OpCodes.CMPix:
                case OpCodes.CMPiy:
                    System.err.printf(" CMP"); break;
                case OpCodes.CPXb:
                case OpCodes.CPXz:
                case OpCodes.CPXa:
                    System.err.printf(" CPX"); break;
                case OpCodes.CPYb:
                case OpCodes.CPYz:
                case OpCodes.CPYa:
                    System.err.printf(" CPY"); break;
                case OpCodes.DCPz:
                case OpCodes.DCPzx:
                case OpCodes.DCPa:
                case OpCodes.DCPax:
                case OpCodes.DCPay:
                case OpCodes.DCPix:
                case OpCodes.DCPiy: // Also known as DCM
                    System.err.printf("*DCP"); break;
                case OpCodes.DECz:
                case OpCodes.DECzx:
                case OpCodes.DECa:
                case OpCodes.DECax:
                    System.err.printf(" DEC"); break;
                case OpCodes.DEXn:
                    System.err.printf(" DEX"); break;
                case OpCodes.DEYn:
                    System.err.printf(" DEY"); break;
                case OpCodes.EORb:
                case OpCodes.EORz:
                case OpCodes.EORzx:
                case OpCodes.EORa:
                case OpCodes.EORax:
                case OpCodes.EORay:
                case OpCodes.EORix:
                case OpCodes.EORiy:
                    System.err.printf(" EOR"); break;
                case OpCodes.INCz:
                case OpCodes.INCzx:
                case OpCodes.INCa:
                case OpCodes.INCax:
                    System.err.printf(" INC"); break;
                case OpCodes.INXn:
                    System.err.printf(" INX"); break;
                case OpCodes.INYn:
                    System.err.printf(" INY"); break;
                case OpCodes.ISBz:
                case OpCodes.ISBzx:
                case OpCodes.ISBa:
                case OpCodes.ISBax:
                case OpCodes.ISBay:
                case OpCodes.ISBix:
                case OpCodes.ISBiy: // Also known as INS
                    System.err.printf("*ISB"); break;
                case OpCodes.JMPw:
                case OpCodes.JMPi:
                    System.err.printf(" JMP"); break;
                case OpCodes.JSRw:
                    System.err.printf(" JSR"); break;
                case OpCodes.LASay:
                    System.err.printf("*LAS"); break;
                case OpCodes.LAXz:
                case OpCodes.LAXzy:
                case OpCodes.LAXa:
                case OpCodes.LAXay:
                case OpCodes.LAXix:
                case OpCodes.LAXiy:
                    System.err.printf("*LAX"); break;
                case OpCodes.LDAb:
                case OpCodes.LDAz:
                case OpCodes.LDAzx:
                case OpCodes.LDAa:
                case OpCodes.LDAax:
                case OpCodes.LDAay:
                case OpCodes.LDAix:
                case OpCodes.LDAiy:
                    System.err.printf(" LDA"); break;
                case OpCodes.LDXb:
                case OpCodes.LDXz:
                case OpCodes.LDXzy:
                case OpCodes.LDXa:
                case OpCodes.LDXay:
                    System.err.printf(" LDX"); break;
                case OpCodes.LDYb:
                case OpCodes.LDYz:
                case OpCodes.LDYzx:
                case OpCodes.LDYa:
                case OpCodes.LDYax:
                    System.err.printf(" LDY"); break;
                case OpCodes.LSRz:
                case OpCodes.LSRzx:
                case OpCodes.LSRa:
                case OpCodes.LSRax:
                case OpCodes.LSRn:
                    System.err.printf(" LSR"); break;
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
                    if (opcode != OpCodes.NOPn) System.err.printf("*");
                    else System.err.printf(" ");
                    System.err.printf("NOP"); break;
                case OpCodes.LXAb: // Also known as OAL
                    System.err.printf("*LXA"); break;
                case OpCodes.ORAb:
                case OpCodes.ORAz:
                case OpCodes.ORAzx:
                case OpCodes.ORAa:
                case OpCodes.ORAax:
                case OpCodes.ORAay:
                case OpCodes.ORAix:
                case OpCodes.ORAiy:
                    System.err.printf(" ORA"); break;
                case OpCodes.PHAn:
                    System.err.printf(" PHA"); break;
                case OpCodes.PHPn:
                    System.err.printf(" PHP"); break;
                case OpCodes.PLAn:
                    System.err.printf(" PLA"); break;
                case OpCodes.PLPn:
                    System.err.printf(" PLP"); break;
                case OpCodes.RLAz:
                case OpCodes.RLAzx:
                case OpCodes.RLAix:
                case OpCodes.RLAa:
                case OpCodes.RLAax:
                case OpCodes.RLAay:
                case OpCodes.RLAiy:
                    System.err.printf("*RLA"); break;
                case OpCodes.ROLz:
                case OpCodes.ROLzx:
                case OpCodes.ROLa:
                case OpCodes.ROLax:
                case OpCodes.ROLn:
                    System.err.printf(" ROL"); break;
                case OpCodes.RORz:
                case OpCodes.RORzx:
                case OpCodes.RORa:
                case OpCodes.RORax:
                case OpCodes.RORn:
                    System.err.printf(" ROR"); break;
                case OpCodes.RRAa:
                case OpCodes.RRAax:
                case OpCodes.RRAay:
                case OpCodes.RRAz:
                case OpCodes.RRAzx:
                case OpCodes.RRAix:
                case OpCodes.RRAiy:
                    System.err.printf("*RRA"); break;
                case OpCodes.RTIn:
                    System.err.printf(" RTI"); break;
                case OpCodes.RTSn:
                    System.err.printf(" RTS"); break;
                case OpCodes.SAXz:
                case OpCodes.SAXzy:
                case OpCodes.SAXa:
                case OpCodes.SAXix: // Also known as AXS
                    System.err.printf("*SAX"); break;
                case OpCodes.SBCb:
                case 0XEB:
                    if (opcode != OpCodes.SBCb) System.err.printf("*");
                    else System.err.printf(" ");
                    System.err.printf("SBC"); break;
                case OpCodes.SBCz:
                case OpCodes.SBCzx:
                case OpCodes.SBCa:
                case OpCodes.SBCax:
                case OpCodes.SBCay:
                case OpCodes.SBCix:
                case OpCodes.SBCiy:
                    System.err.printf(" SBC"); break;
                case OpCodes.SBXb:
                    System.err.printf("*SBX"); break;
                case OpCodes.SECn:
                    System.err.printf(" SEC"); break;
                case OpCodes.SEDn:
                    System.err.printf(" SED"); break;
                case OpCodes.SEIn:
                    System.err.printf(" SEI"); break;
                case OpCodes.SHAay:
                case OpCodes.SHAiy: // Also known as AXA
                    System.err.printf("*SHA"); break;
                case OpCodes.SHSay: // Also known as TAS
                    System.err.printf("*SHS"); break;
                case OpCodes.SHXay: // Also known as XAS
                    System.err.printf("*SHX"); break;
                case OpCodes.SHYax: // Also known as SAY
                    System.err.printf("*SHY"); break;
                case OpCodes.SLOz:
                case OpCodes.SLOzx:
                case OpCodes.SLOa:
                case OpCodes.SLOax:
                case OpCodes.SLOay:
                case OpCodes.SLOix:
                case OpCodes.SLOiy: // Also known as ASO
                    System.err.printf("*SLO"); break;
                case OpCodes.SREz:
                case OpCodes.SREzx:
                case OpCodes.SREa:
                case OpCodes.SREax:
                case OpCodes.SREay:
                case OpCodes.SREix:
                case OpCodes.SREiy: // Also known as LSE
                    System.err.printf("*SRE"); break;
                case OpCodes.STAz:
                case OpCodes.STAzx:
                case OpCodes.STAa:
                case OpCodes.STAax:
                case OpCodes.STAay:
                case OpCodes.STAix:
                case OpCodes.STAiy:
                    System.err.printf(" STA"); break;
                case OpCodes.STXz:
                case OpCodes.STXzy:
                case OpCodes.STXa:
                    System.err.printf(" STX"); break;
                case OpCodes.STYz:
                case OpCodes.STYzx:
                case OpCodes.STYa:
                    System.err.printf(" STY"); break;
                case OpCodes.TAXn:
                    System.err.printf(" TAX"); break;
                case OpCodes.TAYn:
                    System.err.printf(" TAY"); break;
                case OpCodes.TSXn:
                    System.err.printf(" TSX"); break;
                case OpCodes.TXAn:
                    System.err.printf(" TXA"); break;
                case OpCodes.TXSn:
                    System.err.printf(" TXS"); break;
                case OpCodes.TYAn:
                    System.err.printf(" TYA"); break;
                default:
                    System.err.printf("*HLT"); break;
            }

            switch (opcode)
            {
                // Accumulator or Implied cpu.Cycle_EffectiveAddressing
                case OpCodes.ASLn:
                case OpCodes.LSRn:
                case OpCodes.ROLn:
                case OpCodes.RORn:
                    System.err.printf("n  A");
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
                    System.err.printf("z  {0:x02} {2}{1:x02}{3}", sidendian.endian_16lo8(cpu.instrOperand), cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.SAXz:
                case OpCodes.STAz:
                case OpCodes.STXz:
                case OpCodes.STYz:
                case OpCodes.NOPz:
                case 0x44:
                case 0x64:
                    System.err.printf("z  {0:x02}", sidendian.endian_16lo8(cpu.instrOperand));
                    break;

                // Zero Page with X Offset Addressing Mode Handler
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
                    System.err.printf("zx {0:x02},X", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]{2}{1:x02}{3}", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.STAzx:
                case OpCodes.STYzx:
                case OpCodes.NOPzx:
                case 0x34:
                case 0x54:
                case 0x74:
                case 0xD4:
                case 0xF4:
                    System.err.printf("zx {0:x02},X", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
                    break;

                // Zero Page with Y Offset Addressing Mode Handler
                case OpCodes.LAXzy:
                case OpCodes.LDXzy:
                    // AXSzx - Optional Opcode Names
                    System.err.printf("zy {0:x02},Y", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]{2}{1:x02}{3}", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.STXzy:
                case OpCodes.SAXzy:
                    System.err.printf("zy {0:x02},Y", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
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
                    System.err.printf("a  {0:x04} {2}{1:x02}{3}", cpu.instrOperand, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.SAXa:
                case OpCodes.STAa:
                case OpCodes.STXa:
                case OpCodes.STYa:
                case OpCodes.NOPa:
                    System.err.printf("a  {0:x04}", cpu.instrOperand);
                    break;
                case OpCodes.JMPw:
                case OpCodes.JSRw:
                    System.err.printf("w  {0:x04}", cpu.instrOperand);
                    break;

                // Absolute With X Offset Addresing Mode Handler
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
                    System.err.printf("ax {0:x04},X", cpu.instrOperand);
                    System.err.printf(" [{0:x04}]{2}{1:x02}{3}", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.SHYax:
                case OpCodes.STAax:
                case OpCodes.NOPax:
                case 0x3C:
                case 0x5C:
                case 0x7C:
                case 0xDC:
                case 0xFC:
                    System.err.printf("ax {0:x04},X", cpu.instrOperand);
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
                    break;

                // Absolute With Y Offset Addresing Mode Handler
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
                    System.err.printf("ay {0:x04},Y", cpu.instrOperand);
                    System.err.printf(" [{0:x04}]{2}{1:x02}{3}", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.SHAay:
                case OpCodes.SHXay:
                case OpCodes.STAay:
                    System.err.printf("ay {0:x04},Y", cpu.instrOperand);
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
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
                    System.err.printf("b  // #{0:x02}", sidendian.endian_16lo8(cpu.instrOperand));
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
                    System.err.printf("r  // #{0:x02}", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
                    break;

                // Indirect Addressing Mode Handler
                case OpCodes.JMPi:
                    System.err.printf("i  ({0:x04})", cpu.instrOperand);
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
                    break;

                // Indexed with X Preinc Addressing Mode Handler
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
                    System.err.printf("ix ({0:x02},X)", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]{2}{1:x02}{3}", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.SAXix:
                case OpCodes.STAix:
                    System.err.printf("ix ({0:x02},X)", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
                    break;

                // Indexed with Y Postinc Addressing Mode Handler
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
                    System.err.printf("iy ({0:x02}),Y", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]{2}{1:x02}{3}", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
                    break;
                case OpCodes.SHAiy:
                case OpCodes.STAiy:
                    System.err.printf("iy ({0:x02}),Y", sidendian.endian_16lo8(cpu.instrOperand));
                    System.err.printf(" [{0:x04}]", cpu.Cycle_EffectiveAddress);
                    break;

                default:
                    break;
            }

            System.err.printf("\n\n");
            //fflush(cpu.m_fdbg);
// #endif
        }

// #endif

    }


