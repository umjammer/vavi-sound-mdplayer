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


public class Mos6510Debug {


    public void DumpState(long time, Mos6510 cpu) {
// #if false
        System.err.printf(" pc  I  a  x  y  SP  DR PR NV-BDIZC  Instruction (%d)\n", time);
//        System.err.printf("%04x ", cpu.instrStartPC);
//        System.err.printf(cpu.irqAssertedOnPin ? "t " : "f ");
//        System.err.printf("%02x ", cpu.Register_Accumulator);
//        System.err.printf("%02x ", cpu.Register_X);
//        System.err.printf("%02x ", cpu.Register_Y);
//        System.err.printf("%02x ", SidEndian.to16lo8(cpu.Register_StackPointer));
        System.err.printf("%02x ", cpu.cpuRead((short) 0));
        System.err.printf("%02x ", cpu.cpuRead((short) 1));

//        System.err.printf(cpu.Flags.getN() ? "1" : "0");
//        System.err.printf(cpu.Flags.getV() ? "1" : "0");
        System.err.print("1");
//        System.err.printf(cpu.Flags.getB() ? "1" : "0");
//        System.err.printf(cpu.Flags.getD() ? "1" : "0");
//        System.err.printf(cpu.Flags.getI() ? "1" : "0");
//        System.err.printf(cpu.Flags.getZ() ? "1" : "0");
//        System.err.printf(cpu.Flags.getC() ? "1" : "0");

        int opcode = cpu.cpuRead((short) 0/*cpu.instrStartPC*/);

//        System.err.printf("  %02x ", opcode);

        switch (opcode) {
        // Accumulator or Implied cpu.Cycle_EffectiveAddressing
        case OpCodes.ASLn:
        case OpCodes.LSRn:
        case OpCodes.ROLn:
        case OpCodes.RORn:
            System.err.print("      ");
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
//            System.err.printf("%02x    ", SidEndian.to16lo8(cpu.instrOperand));
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
//            System.err.printf("%02x    ", SidEndian.to16lo8(cpu.instrOperand));
            break;
        // Zero Page with y Offset Addressing Mode Handler
        case OpCodes.LDXzy:
        case OpCodes.STXzy:
        case OpCodes.SAXzy:
        case OpCodes.LAXzy:
            // AXSzx - Optional Opcode Names
//            System.err.printf("%02x    ", SidEndian.to16lo8(cpu.instrOperand));
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
//            System.err.printf("%02x %02x ", SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand));
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
//            System.err.printf("%02x %02x ", SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand));
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
//            System.err.printf("%02x %02x ", SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand));
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
//            System.err.printf("%02x    ", SidEndian.to16lo8(cpu.Cycle_Data));
            break;
        // Indirect Addressing Mode Handler
        case OpCodes.JMPi:
//            System.err.printf("%02x %02x ", SidEndian.to16lo8(cpu.instrOperand), SidEndian.to16hi8(cpu.instrOperand));
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
//            System.err.printf("%02x    ", SidEndian.to16lo8(cpu.instrOperand));
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
//            System.err.printf("%02x    ", SidEndian.to16lo8(cpu.instrOperand));
            break;
        default:
            System.err.print("      ");
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
            System.err.print(" ADC");
            break;
        case OpCodes.ANCb:
        case 0x2B:
            System.err.print("*ANC");
            break;
        case OpCodes.ANDb:
        case OpCodes.ANDz:
        case OpCodes.ANDzx:
        case OpCodes.ANDa:
        case OpCodes.ANDax:
        case OpCodes.ANDay:
        case OpCodes.ANDix:
        case OpCodes.ANDiy:
            System.err.print(" AND");
            break;
        case OpCodes.ANEb: // Also known as XAA
            System.err.print("*ANE");
            break;
        case OpCodes.ARRb:
            System.err.print("*ARR");
            break;
        case OpCodes.ASLn:
        case OpCodes.ASLz:
        case OpCodes.ASLzx:
        case OpCodes.ASLa:
        case OpCodes.ASLax:
            System.err.print(" ASL");
            break;
        case OpCodes.ASRb: // Also known as ALR
            System.err.print("*ASR");
            break;
        case OpCodes.BCCr:
            System.err.print(" BCC");
            break;
        case OpCodes.BCSr:
            System.err.print(" BCS");
            break;
        case OpCodes.BEQr:
            System.err.
                    print(" BEQ");
            break;
        case OpCodes.BITz:
        case OpCodes.BITa:
            System.err.print(" BIT");
            break;
        case OpCodes.BMIr:
            System.err.print(" BMI");
            break;
        case OpCodes.BNEr:
            System.err.print(" BNE");
            break;
        case OpCodes.BPLr:
            System.err.print(" BPL");
            break;
        case OpCodes.BRKn:
            System.err.print(" BRK");
            break;
        case OpCodes.BVCr:
            System.err.print(" BVC");
            break;
        case OpCodes.BVSr:
            System.err.
                    print(" BVS");
            break;
        case OpCodes.CLCn:
            System.err.print(" CLC");
            break;
        case OpCodes.CLDn:
            System.err.print(" CLD");
            break;
        case OpCodes.CLIn:
            System.err.print(" CLI");
            break;
        case OpCodes.CLVn:
            System.err.print(" CLV");
            break;
        case OpCodes.CMPb:
        case OpCodes.CMPz:
        case OpCodes.CMPzx:
        case OpCodes.CMPa:
        case OpCodes.CMPax:
        case OpCodes.CMPay:
        case OpCodes.CMPix:
        case OpCodes.CMPiy:
            System.err.print(" CMP");
            break;
        case OpCodes.CPXb:
        case OpCodes.CPXz:
        case OpCodes.CPXa:
            System.err.print(" CPX");
            break;
        case OpCodes.CPYb:
        case OpCodes.CPYz:
        case OpCodes.CPYa:
            System.err.print(" CPY");
            break;
        case OpCodes.DCPz:
        case OpCodes.DCPzx:
        case OpCodes.DCPa:
        case OpCodes.DCPax:
        case OpCodes.DCPay:
        case OpCodes.DCPix:
        case OpCodes.DCPiy: // Also known as DCM
            System.err.print("*DCP");
            break;
        case OpCodes.DECz:
        case OpCodes.DECzx:
        case OpCodes.DECa:
        case OpCodes.DECax:
            System.err.print(" DEC");
            break;
        case OpCodes.DEXn:
            System.err.print(" DEX");
            break;
        case OpCodes.DEYn:
            System.err.print(" DEY");
            break;
        case OpCodes.EORb:
        case OpCodes.EORz:
        case OpCodes.EORzx:
        case OpCodes.EORa:
        case OpCodes.EORax:
        case OpCodes.EORay:
        case OpCodes.EORix:
        case OpCodes.EORiy:
            System.err.print(" EOR");
            break;
        case OpCodes.INCz:
        case OpCodes.INCzx:
        case OpCodes.INCa:
        case OpCodes.INCax:
            System.err.print(" INC");
            break;
        case OpCodes.INXn:
            System.err.print(" INX");
            break;
        case OpCodes.INYn:
            System.err.print(" INY");
            break;
        case OpCodes.ISBz:
        case OpCodes.ISBzx:
        case OpCodes.ISBa:
        case OpCodes.ISBax:
        case OpCodes.ISBay:
        case OpCodes.ISBix:
        case OpCodes.ISBiy: // Also known as INS
            System.err.print("*ISB");
            break;
        case OpCodes.JMPw:
        case OpCodes.JMPi:
            System.err.print(" JMP");
            break;
        case OpCodes.JSRw:
            System.err.print(" JSR");
            break;
        case OpCodes.LASay:
            System.err.print("*LAS");
            break;
        case OpCodes.LAXz:
        case OpCodes.LAXzy:
        case OpCodes.LAXa:
        case OpCodes.LAXay:
        case OpCodes.LAXix:
        case OpCodes.LAXiy:
            System.err.print("*LAX");
            break;
        case OpCodes.LDAb:
        case OpCodes.LDAz:
        case OpCodes.LDAzx:
        case OpCodes.LDAa:
        case OpCodes.LDAax:
        case OpCodes.LDAay:
        case OpCodes.LDAix:
        case OpCodes.LDAiy:
            System.err.print(" LDA");
            break;
        case OpCodes.LDXb:
        case OpCodes.LDXz:
        case OpCodes.LDXzy:
        case OpCodes.LDXa:
        case OpCodes.LDXay:
            System.err.print(" LDX");
            break;
        case OpCodes.LDYb:
        case OpCodes.LDYz:
        case OpCodes.LDYzx:
        case OpCodes.LDYa:
        case OpCodes.LDYax:
            System.err.print(" LDY");
            break;
        case OpCodes.LSRz:
        case OpCodes.LSRzx:
        case OpCodes.LSRa:
        case OpCodes.LSRax:
        case OpCodes.LSRn:
            System.err.print(" LSR");
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
            if (opcode != OpCodes.NOPn) System.err.print("*");
            else System.err.print(" ");
            System.err.print("NOP");
            break;
        case OpCodes.LXAb: // Also known as OAL
            System.err.print("*LXA");
            break;
        case OpCodes.ORAb:
        case OpCodes.ORAz:
        case OpCodes.ORAzx:
        case OpCodes.ORAa:
        case OpCodes.ORAax:
        case OpCodes.ORAay:
        case OpCodes.ORAix:
        case OpCodes.ORAiy:
            System.err.print(" ORA");
            break;
        case OpCodes.PHAn:
            System.err.print(" PHA");
            break;
        case OpCodes.PHPn:
            System.err.print(" PHP");
            break;
        case OpCodes.PLAn:
            System.err.print(" PLA");
            break;
        case OpCodes.PLPn:
            System.err.print(" PLP");
            break;
        case OpCodes.RLAz:
        case OpCodes.RLAzx:
        case OpCodes.RLAix:
        case OpCodes.RLAa:
        case OpCodes.RLAax:
        case OpCodes.RLAay:
        case OpCodes.RLAiy:
            System.err.print("*RLA");
            break;
        case OpCodes.ROLz:
        case OpCodes.ROLzx:
        case OpCodes.ROLa:
        case OpCodes.ROLax:
        case OpCodes.ROLn:
            System.err.print(" ROL");
            break;
        case OpCodes.RORz:
        case OpCodes.RORzx:
        case OpCodes.RORa:
        case OpCodes.RORax:
        case OpCodes.RORn:
            System.err.print(" ROR");
            break;
        case OpCodes.RRAa:
        case OpCodes.RRAax:
        case OpCodes.RRAay:
        case OpCodes.RRAz:
        case OpCodes.RRAzx:
        case OpCodes.RRAix:
        case OpCodes.RRAiy:
            System.err.print("*RRA");
            break;
        case OpCodes.RTIn:
            System.err.print(" RTI");
            break;
        case OpCodes.RTSn:
            System.err.print(" RTS");
            break;
        case OpCodes.SAXz:
        case OpCodes.SAXzy:
        case OpCodes.SAXa:
        case OpCodes.SAXix: // Also known as AXS
            System.err.print("*SAX");
            break;
        case OpCodes.SBCb:
        case 0XEB:
            if (opcode != OpCodes.SBCb) System.err.print("*");
            else System.err.print(" ");
            System.err.print("SBC");
            break;
        case OpCodes.SBCz:
        case OpCodes.SBCzx:
        case OpCodes.SBCa:
        case OpCodes.SBCax:
        case OpCodes.SBCay:
        case OpCodes.SBCix:
        case OpCodes.SBCiy:
            System.err.print(" SBC");
            break;
        case OpCodes.SBXb:
            System.err.print("*SBX");
            break;
        case OpCodes.SECn:
            System.err.print(" SEC");
            break;
        case OpCodes.SEDn:
            System.err.print(" SED");
            break;
        case OpCodes.SEIn:
            System.err.print(" SEI");
            break;
        case OpCodes.SHAay:
        case OpCodes.SHAiy: // Also known as AXA
            System.err.print("*SHA");
            break;
        case OpCodes.SHSay: // Also known as TAS
            System.err.print("*SHS");
            break;
        case OpCodes.SHXay: // Also known as XAS
            System.err.print("*SHX");
            break;
        case OpCodes.SHYax: // Also known as SAY
            System.err.print("*SHY");
            break;
        case OpCodes.SLOz:
        case OpCodes.SLOzx:
        case OpCodes.SLOa:
        case OpCodes.SLOax:
        case OpCodes.SLOay:
        case OpCodes.SLOix:
        case OpCodes.SLOiy: // Also known as ASO
            System.err.print("*SLO");
            break;
        case OpCodes.SREz:
        case OpCodes.SREzx:
        case OpCodes.SREa:
        case OpCodes.SREax:
        case OpCodes.SREay:
        case OpCodes.SREix:
        case OpCodes.SREiy: // Also known as LSE
            System.err.print("*SRE");
            break;
        case OpCodes.STAz:
        case OpCodes.STAzx:
        case OpCodes.STAa:
        case OpCodes.STAax:
        case OpCodes.STAay:
        case OpCodes.STAix:
        case OpCodes.STAiy:
            System.err.print(" STA");
            break;
        case OpCodes.STXz:
        case OpCodes.STXzy:
        case OpCodes.STXa:
            System.err.print(" STX");
            break;
        case OpCodes.STYz:
        case OpCodes.STYzx:
        case OpCodes.STYa:
            System.err.print(" STY");
            break;
        case OpCodes.TAXn:
            System.err.print(" TAX");
            break;
        case OpCodes.TAYn:
            System.err.print(" TAY");
            break;
        case OpCodes.TSXn:
            System.err.print(" TSX");
            break;
        case OpCodes.TXAn:
            System.err.print(" TXA");
            break;
        case OpCodes.TXSn:
            System.err.print(" TXS");
            break;
        case OpCodes.TYAn:
            System.err.print(" TYA");
            break;
        default:
            System.err.print("*HLT");
            break;
        }

        switch (opcode) {
        // Accumulator or Implied cpu.Cycle_EffectiveAddressing
        case OpCodes.ASLn:
        case OpCodes.LSRn:
        case OpCodes.ROLn:
        case OpCodes.RORn:
            System.err.print("n  a");
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
//            System.err.printf("z  %02x %d%02x%d", SidEndian.to16lo8(cpu.instrOperand), cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.SAXz:
        case OpCodes.STAz:
        case OpCodes.STXz:
        case OpCodes.STYz:
        case OpCodes.NOPz:
        case 0x44:
        case 0x64:
//            System.err.printf("z  %02x", SidEndian.to16lo8(cpu.instrOperand));
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
//            System.err.printf("zx %02x,x", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.STAzx:
        case OpCodes.STYzx:
        case OpCodes.NOPzx:
        case 0x34:
        case 0x54:
        case 0x74:
        case 0xD4:
        case 0xF4:
//            System.err.printf("zx %02x,x", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
            break;

        // Zero Page with y Offset Addressing Mode Handler
        case OpCodes.LAXzy:
        case OpCodes.LDXzy:
            // AXSzx - Optional Opcode Names
//            System.err.printf("zy %02x,y", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.STXzy:
        case OpCodes.SAXzy:
//            System.err.printf("zy %02x,y", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
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
//            System.err.printf("a  %04x %d%02x%d", cpu.instrOperand, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.SAXa:
        case OpCodes.STAa:
        case OpCodes.STXa:
        case OpCodes.STYa:
        case OpCodes.NOPa:
//            System.err.printf("a  %04x", cpu.instrOperand);
            break;
        case OpCodes.JMPw:
        case OpCodes.JSRw:
//            System.err.printf("w  %04x", cpu.instrOperand);
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
//            System.err.printf("ax %04x,x", cpu.instrOperand);
//            System.err.printf(" [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.SHYax:
        case OpCodes.STAax:
        case OpCodes.NOPax:
        case 0x3C:
        case 0x5C:
        case 0x7C:
        case 0xDC:
        case 0xFC:
//            System.err.printf("ax %04x,x", cpu.instrOperand);
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
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
//            System.err.printf("ay %04x,y", cpu.instrOperand);
//            System.err.printf(" [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.SHAay:
        case OpCodes.SHXay:
        case OpCodes.STAay:
//            System.err.printf("ay %04x,y", cpu.instrOperand);
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
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
//            System.err.printf("b  // #%02x", SidEndian.to16lo8(cpu.instrOperand));
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
//            System.err.printf("r  // #%02x", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
            break;

        // Indirect Addressing Mode Handler
        case OpCodes.JMPi:
//            System.err.printf("i  (%04x)", cpu.instrOperand);
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
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
//            System.err.printf("ix (%02x,x)", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.SAXix:
        case OpCodes.STAix:
//            System.err.printf("ix (%02x,x)", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
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
//            System.err.printf("iy (%02x),y", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]%d%02x%d", cpu.Cycle_EffectiveAddress, cpu.Cycle_Data, "{", "}");
            break;
        case OpCodes.SHAiy:
        case OpCodes.STAiy:
//            System.err.printf("iy (%02x),y", SidEndian.to16lo8(cpu.instrOperand));
//            System.err.printf(" [%04x]", cpu.Cycle_EffectiveAddress);
            break;

        default:
            break;
        }

        System.err.print("\n\n");
    }
}


