/*
 * This file instanceof part of libsidplayfp, a Sid player engine.
 *
 * Copyright 2011-2013 Leandro Nini <drfiemost@users.sourceforge.net>
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

public interface OpCodes {

    int OPCODE_MAX = 0x100;

    // HLT
//    case 0x02: case 0x12: case 0x22: case 0x32: case 0x42: case 0x52:
//    case 0x62: case 0x72: case 0x92: case 0xb2: case 0xd2: case 0xf2:
//    case 0x02: case 0x12: case 0x22: case 0x32: case 0x42: case 0x52:
//    case 0x62: case 0x72: case 0x92: case 0xb2: case 0xd2: case 0xf2:

    int BRKn = 0x00;
    int JSRw = 0x20;
    int RTIn = 0x40;
    int RTSn = 0x60;
    int NOPb = 0x80;
    //int  NOPb_ NOPb: case 0x82: case 0xC2: case 0xE2: case 0x89
    int LDYb = 0xA0;
    int CPYb = 0xC0;
    int CPXb = 0xE0;

    int ORAix = 0x01;
    int ANDix = 0x21;
    int EORix = 0x41;
    int ADCix = 0x61;
    int STAix = 0x81;
    int LDAix = 0xA1;
    int CMPix = 0xC1;
    int SBCix = 0xE1;

    int LDXb = 0xA2;

    int SLOix = 0x03;
    int RLAix = 0x23;
    int SREix = 0x43;
    int RRAix = 0x63;
    int SAXix = 0x83;
    int LAXix = 0xA3;
    int DCPix = 0xC3;
    int ISBix = 0xE3;

    int NOPz = 0x04;
    //int  NOPz_ NOPz: case 0x44: case 0x64
    int BITz = 0x24;
    int STYz = 0x84;
    int LDYz = 0xA4;
    int CPYz = 0xC4;
    int CPXz = 0xE4;

    int ORAz = 0x05;
    int ANDz = 0x25;
    int EORz = 0x45;
    int ADCz = 0x65;
    int STAz = 0x85;
    int LDAz = 0xA5;
    int CMPz = 0xC5;
    int SBCz = 0xE5;

    int ASLz = 0x06;
    int ROLz = 0x26;
    int LSRz = 0x46;
    int RORz = 0x66;
    int STXz = 0x86;
    int LDXz = 0xA6;
    int DECz = 0xC6;
    int INCz = 0xE6;

    int SLOz = 0x07;
    int RLAz = 0x27;
    int SREz = 0x47;
    int RRAz = 0x67;
    int SAXz = 0x87;
    int LAXz = 0xA7;
    int DCPz = 0xC7;
    int ISBz = 0xE7;

    int PHPn = 0x08;
    int PLPn = 0x28;
    int PHAn = 0x48;
    int PLAn = 0x68;
    int DEYn = 0x88;
    int TAYn = 0xA8;
    int INYn = 0xC8;
    int INXn = 0xE8;

    int ORAb = 0x09;
    int ANDb = 0x29;
    int EORb = 0x49;
    int ADCb = 0x69;
    int LDAb = 0xA9;
    int CMPb = 0xC9;
    int SBCb = 0xE9;
    //int  SBCb_ SBCb: case 0XEB

    int ASLn = 0x0A;
    int ROLn = 0x2A;
    int LSRn = 0x4A;
    int RORn = 0x6A;
    int TXAn = 0x8A;
    int TAXn = 0xAA;
    int DEXn = 0xCA;
    int NOPn = 0xEA;
    //int  NOPn_ NOPn: case 0x1A: case 0x3A: case 0x5A: case 0x7A: case 0xDA: case 0xFA

    int ANCb = 0x0B;
    //int  ANCb_ ANCb: case 0x2B
    int ASRb = 0x4B;
    int ARRb = 0x6B;
    int ANEb = 0x8B;
    int XAAb = 0x8B;
    int LXAb = 0xAB;
    int SBXb = 0xCB;

    int NOPa = 0x0C;
    int BITa = 0x2C;
    int JMPw = 0x4C;
    int JMPi = 0x6C;
    int STYa = 0x8C;
    int LDYa = 0xAC;
    int CPYa = 0xCC;
    int CPXa = 0xEC;

    int ORAa = 0x0D;
    int ANDa = 0x2D;
    int EORa = 0x4D;
    int ADCa = 0x6D;
    int STAa = 0x8D;
    int LDAa = 0xAD;
    int CMPa = 0xCD;
    int SBCa = 0xED;

    int ASLa = 0x0E;
    int ROLa = 0x2E;
    int LSRa = 0x4E;
    int RORa = 0x6E;
    int STXa = 0x8E;
    int LDXa = 0xAE;
    int DECa = 0xCE;
    int INCa = 0xEE;

    int SLOa = 0x0F;
    int RLAa = 0x2F;
    int SREa = 0x4F;
    int RRAa = 0x6F;
    int SAXa = 0x8F;
    int LAXa = 0xAF;
    int DCPa = 0xCF;
    int ISBa = 0xEF;

    int BPLr = 0x10;
    int BMIr = 0x30;
    int BVCr = 0x50;
    int BVSr = 0x70;
    int BCCr = 0x90;
    int BCSr = 0xB0;
    int BNEr = 0xD0;
    int BEQr = 0xF0;

    int ORAiy = 0x11;
    int ANDiy = 0x31;
    int EORiy = 0x51;
    int ADCiy = 0x71;
    int STAiy = 0x91;
    int LDAiy = 0xB1;
    int CMPiy = 0xD1;
    int SBCiy = 0xF1;

    int SLOiy = 0x13;
    int RLAiy = 0x33;
    int SREiy = 0x53;
    int RRAiy = 0x73;
    int SHAiy = 0x93;
    int LAXiy = 0xB3;
    int DCPiy = 0xD3;
    int ISBiy = 0xF3;

    int NOPzx = 0x14;
    //int  NOPzx_ NOPzx: case 0x34: case 0x54: case 0x74: case 0xD4: case 0xF4
    int STYzx = 0x94;
    int LDYzx = 0xB4;

    int ORAzx = 0x15;
    int ANDzx = 0x35;
    int EORzx = 0x55;
    int ADCzx = 0x75;
    int STAzx = 0x95;
    int LDAzx = 0xB5;
    int CMPzx = 0xD5;
    int SBCzx = 0xF5;

    int ASLzx = 0x16;
    int ROLzx = 0x36;
    int LSRzx = 0x56;
    int RORzx = 0x76;
    int STXzy = 0x96;
    int LDXzy = 0xB6;
    int DECzx = 0xD6;
    int INCzx = 0xF6;

    int SLOzx = 0x17;
    int RLAzx = 0x37;
    int SREzx = 0x57;
    int RRAzx = 0x77;
    int SAXzy = 0x97;
    int LAXzy = 0xB7;
    int DCPzx = 0xD7;
    int ISBzx = 0xF7;

    int CLCn = 0x18;
    int SECn = 0x38;
    int CLIn = 0x58;
    int SEIn = 0x78;
    int TYAn = 0x98;
    int CLVn = 0xB8;
    int CLDn = 0xD8;
    int SEDn = 0xF8;

    int ORAay = 0x19;
    int ANDay = 0x39;
    int EORay = 0x59;
    int ADCay = 0x79;
    int STAay = 0x99;
    int LDAay = 0xB9;
    int CMPay = 0xD9;
    int SBCay = 0xF9;

    int TXSn = 0x9A;
    int TSXn = 0xBA;

    int SLOay = 0x1B;
    int RLAay = 0x3B;
    int SREay = 0x5B;
    int RRAay = 0x7B;
    int SHSay = 0x9B;
    int TASay = 0x9B;
    int LASay = 0xBB;
    int DCPay = 0xDB;
    int ISBay = 0xFB;

    int NOPax = 0x1C;
    //int  NOPax_ NOPax: case 0x3C: case 0x5C: case 0x7C: case 0xDC: case 0xFC
    int SHYax = 0x9C;
    int LDYax = 0xBC;

    int ORAax = 0x1D;
    int ANDax = 0x3D;
    int EORax = 0x5D;
    int ADCax = 0x7D;
    int STAax = 0x9D;
    int LDAax = 0xBD;
    int CMPax = 0xDD;
    int SBCax = 0xFD;

    int ASLax = 0x1E;
    int ROLax = 0x3E;
    int LSRax = 0x5E;
    int RORax = 0x7E;
    int SHXay = 0x9E;
    int LDXay = 0xBE;
    int DECax = 0xDE;
    int INCax = 0xFE;

    int SLOax = 0x1F;
    int RLAax = 0x3F;
    int SREax = 0x5F;
    int RRAax = 0x7F;
    int SHAay = 0x9F;
    int LAXay = 0xBF;
    int DCPax = 0xDF;
    int ISBax = 0xff;

    // Instruction Aliases
    int ASOix = SLOix;
    int LSEix = SREix;
    int AXSix = SAXix;
    int DCMix = DCPix;
    int INSix = ISBix;
    int ASOz = SLOz;
    int LSEz = SREz;
    int AXSz = SAXz;
    int DCMz = DCPz;
    int INSz = ISBz;
    int ALRb = ASRb;
    int OALb = LXAb;
    int ASOa = SLOa;
    int LSEa = SREa;
    int AXSa = SAXa;
    int DCMa = DCPa;
    int INSa = ISBa;
    int ASOiy = SLOiy;
    int LSEiy = SREiy;
    int AXAiy = SHAiy;
    int DCMiy = DCPiy;
    int INSiy = ISBiy;
    int ASOzx = SLOzx;
    int LSEzx = SREzx;
    int AXSzy = SAXzy;
    int DCMzx = DCPzx;
    int INSzx = ISBzx;
    int ASOay = SLOay;
    int LSEay = SREay;
    int DCMay = DCPay;
    int INSay = ISBay;
    int SAYax = SHYax;
    int XASay = SHXay;
    int ASOax = SLOax;
    int LSEax = SREax;
    int AXAay = SHAay;
    int DCMax = DCPax;
    int INSax = ISBax;
    int SKBn = NOPb;
    int SKWn = NOPa;
}
