package mdplayer.driver.fmp.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.System.getLogger;


public class Nise286 {
    // x86 general reference sites and sources
    // https: // qiita.com/hdk_2/items/6f8cb8a7c67342e2a32a
    // mame tabel286.h

    // Sites referenced for OF determination processing
    // https: // hiroyukichishiro.com/arithmetic-overflow-in-c-language/#%E8%B6%B3%E3%81%97%E7%AE%97%E3%81%AE%E4%BA%8B%E5%BE%8C%E6%9D%A1%E4%BB%B6
    // mame

    private static final Logger logger = getLogger(Nise286.class.getName());

    private Register286 regs;
    private Memory98 mem;
    private NiseDos dos;
    private Nise98 machine;
    private boolean segPrefSw = false;
    private int segPref = 0;
    private boolean repSW = false;
    private int repType = 0; // 0:REPE, 1:REPNE
    private boolean hltSW = false;
    private final List<UserInt> lstUserInt = new ArrayList<>();
    private final Object userIntLockObject = new Object();

    public byte w_mmsk = (byte) 0xff; // (IR7 (INT0Fh)-IR0(INT08h)) All interrupts are disabled
    public byte w_smsk = (byte) 0xff; // (IR15(INT17h)-IR8(INT10h)) All interrupts are disabled
    public boolean[] interruptTrigger = new boolean[24];
    public int iLevel = 0;
    private List<Supplier<Boolean>> lstHook = new ArrayList<>();

    public Nise286(Nise98 machine) {
        this.regs = machine.GetRegisters();
        this.mem = machine.GetMem();
        this.dos = machine.GetDos();
        this.machine = machine;
    }

    public void AddUserInt(UserInt ui) {
        synchronized (userIntLockObject) {
            lstUserInt.add(ui);
        }
    }

    public void SetHook(Supplier<Boolean> hook) {
        lstHook.add(hook);
    }

    public int StepExecute() {
        if (hltSW) {
            logger.log(Level.ERROR, "CPU is HALT.");
            return -1;
        }

        Interrupt();

        for (Supplier<Boolean> func : lstHook) {
            boolean did = func.get();
            if (did) return 0;
        }

        byte op = Fetch();
        switch (op & 0xff) {
            case 0x00:
                ADD_EB_GB();
                break;
            case 0x01:
                ADD_EW_GW();
                break;
            case 0x02:
                ADD_GB_EB();
                break;
            case 0x03:
                ADD_GW_EW();
                break;
            case 0x04:
                ADD_AL_IB();
                break;
            case 0x05:
                ADD_AX_IW();
                break;
            case 0x06:
                PUSH_ES();
                break;
            case 0x07:
                POP_ES();
                break;
            case 0x08:
                OR_EB_GB();
                break;
            case 0x09:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x0a:
                OR_GB_EB();
                break;
            case 0x0b:
                OR_GW_EW();
                break;
            case 0x0c:
                OR_AL_IB();
                break;
            case 0x0d:
                OR_AX_IW();
                break;
            case 0x0e:
                PUSH_CS();
                break;
            case 0x0f:
                POP_CS();
                break; // x286

            case 0x10:
                ADC_EB_GB();
                break;
            case 0x11:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x12:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x13:
                ADC_GW_EW();
                break;
            case 0x14:
                ADC_AL_IB();
                break;
            case 0x15:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x16:
                PUSH_SS();
                break;
            case 0x17:
                POP_SS();
                break;
            case 0x18:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x19:
                SBB_EW_GW();
                break;
            case 0x1a:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x1b:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x1c:
                SBB_AL_IB();
                break;
            case 0x1d:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x1e:
                PUSH_DS();
                break;
            case 0x1f:
                POP_DS();
                break;

            case 0x20:
                AND_EB_GB();
                break;
            case 0x21:
                AND_EW_GW();
                break;
            case 0x22:
                AND_GB_EB();
                break;
            case 0x23:
                AND_GW_EW();
                break;
            case 0x24:
                AND_AL_IB();
                break;
            case 0x25:
                AND_AX_IW();
                break;
            case 0x26:
                ES();
                break;
            case 0x27:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x28:
                SUB_EB_GB();
                break;
            case 0x29:
                SUB_EW_GW();
                break;
            case 0x2a:
                SUB_GB_EB();
                break;
            case 0x2b:
                SUB_GW_EW();
                break;
            case 0x2c:
                SUB_AL_IB();
                break;
            case 0x2d:
                SUB_AX_IW();
                break;
            case 0x2e:
                CS();
                break;
            case 0x2f:
                DAS();
                break;

            case 0x30:
                XOR_EB_GB();
                break;
            case 0x31:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x32:
                XOR_GB_EB();
                break;
            case 0x33:
                XOR_GW_EW();
                break;
            case 0x34:
                XOR_AL_IB();
                break;
            case 0x35:
                XOR_AX_IW();
                break;
            case 0x36:
                SS();
                break;
            case 0x37:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x38:
                CMP_EB_GB();
                break;
            case 0x39:
                CMP_EW_GW();
                break;
            case 0x3a:
                CMP_GB_EB();
                break;
            case 0x3b:
                CMP_GW_EW();
                break;
            case 0x3c:
                CMP_AL_IB();
                break;
            case 0x3d:
                CMP_AX_IW();
                break;
            case 0x3e:
                DS();
                break;
            case 0x3f:
                throw new UnsupportedOperationException(Integer.toHexString(op));

            case 0x40:
                INC_AX();
                break;
            case 0x41:
                INC_CX();
                break;
            case 0x42:
                INC_DX();
                break;
            case 0x43:
                INC_BX();
                break;
            case 0x44:
                INC_SP();
                break;
            case 0x45:
                INC_BP();
                break;
            case 0x46:
                INC_SI();
                break;
            case 0x47:
                INC_DI();
                break;
            case 0x48:
                DEC_AX();
                break;
            case 0x49:
                DEC_CX();
                break;
            case 0x4a:
                DEC_DX();
                break;
            case 0x4b:
                DEC_BX();
                break;
            case 0x4c:
                DEC_SP();
                break;
            case 0x4d:
                DEC_BP();
                break;
            case 0x4e:
                DEC_SI();
                break;
            case 0x4f:
                DEC_DI();
                break;

            case 0x50:
                PUSH_AX();
                break;
            case 0x51:
                PUSH_CX();
                break;
            case 0x52:
                PUSH_DX();
                break;
            case 0x53:
                PUSH_BX();
                break;
            case 0x54:
                PUSH_SP();
                break;
            case 0x55:
                PUSH_BP();
                break;
            case 0x56:
                PUSH_SI();
                break;
            case 0x57:
                PUSH_DI();
                break;
            case 0x58:
                POP_AX();
                break;
            case 0x59:
                POP_CX();
                break;
            case 0x5a:
                POP_DX();
                break;
            case 0x5b:
                POP_BX();
                break;
            case 0x5c:
                POP_SP();
                break;
            case 0x5d:
                POP_BP();
                break;
            case 0x5e:
                POP_SI();
                break;
            case 0x5f:
                POP_DI();
                break;

            case 0x60:
                PUSHA();
                break; // x186
            case 0x61:
                POPA();
                break; // x186
            case 0x62:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x63:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0x64:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0x65:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0x66:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0x67:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0x68:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x69:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x6a:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x6b:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x6c:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x6d:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x6e:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0x6f:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186

            case 0x70:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x71:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x72:
                JB_short();
                break;
            case 0x73:
                JNB_short();
                break;
            case 0x74:
                JZ_short();
                break;
            case 0x75:
                JNZ_short();
                break;
            case 0x76:
                JBE_short();
                break;
            case 0x77:
                JNBE_short();
                break;
            case 0x78:
                JS_short();
                break;
            case 0x79:
                JNS_short();
                break;
            case 0x7a:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x7b:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x7c:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x7d:
                JNL_short();
                break;
            case 0x7e:
                JNG_short();
                break;
            case 0x7f:
                JNLE_short();
                break;

            case 0x80:
                GRP1B();
                break;
            case 0x81:
                GRP1W();
                break;
            case 0x82:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x83:
                GRP1WB();
                break;
            case 0x84:
                TEST_EB_GB();
                break;
            case 0x85:
                TEST_EW_GW();
                break;
            case 0x86:
                XCHG_EB_GB();
                break;
            case 0x87:
                XCHG_EW_GW();
                break;
            case 0x88:
                MOV_EB_GB();
                break;
            case 0x89:
                MOV_EW_GW();
                break;
            case 0x8a:
                MOV_GB_EB();
                break;
            case 0x8b:
                MOV_GW_EW();
                break;
            case 0x8c:
                MOV_EW_SW();
                break;
            case 0x8d:
                LEA_GW_M();
                break;
            case 0x8e:
                MOV_SW_EW();
                break;
            case 0x8f:
                POP_EW();
                break;

            case 0x90:
                NOP();
                break;
            case 0x91:
                XCHG_CX_AX();
                break;
            case 0x92:
                XCHG_DX_AX();
                break;
            case 0x93:
                XCHG_BX_AX();
                break;
            case 0x94:
                XCHG_SP_AX();
                break;
            case 0x95:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x96:
                XCHG_SI_AX();
                break;
            case 0x97:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x98:
                CBW();
                break;
            case 0x99:
                CWD();
                break;
            case 0x9a:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x9b:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x9c:
                PUSHF();
                break;
            case 0x9d:
                POPF();
                break; // x286
            case 0x9e:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0x9f:
                throw new UnsupportedOperationException(Integer.toHexString(op));

            case 0xa0:
                MOV_AL_OB();
                break;
            case 0xa1:
                MOV_AX_OW();
                break;
            case 0xa2:
                MOV_OB_AL();
                break;
            case 0xa3:
                MOV_OW_AX();
                break;
            case 0xa4:
                MOVSB();
                break;
            case 0xa5:
                MOVSW();
                break;
            case 0xa6:
                CMPSB();
                break;
            case 0xa7:
                CMPSW();
                break;
            case 0xa8:
                TEST_AL_IB();
                break;
            case 0xa9:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xaa:
                STOSB();
                break;
            case 0xab:
                STOSW();
                break;
            case 0xac:
                LODSB();
                break;
            case 0xad:
                LODSW();
                break;
            case 0xae:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xaf:
                SCASW();
                break;

            case 0xb0:
                MOV_AL_IB();
                break;
            case 0xb1:
                MOV_CL_IB();
                break;
            case 0xb2:
                MOV_DL_IB();
                break;
            case 0xb3:
                MOV_BL_IB();
                break;
            case 0xb4:
                MOV_AH_IB();
                break;
            case 0xb5:
                MOV_CH_IB();
                break;
            case 0xb6:
                MOV_DH_IB();
                break;
            case 0xb7:
                MOV_BH_IB();
                break;
            case 0xb8:
                MOV_AX_IW();
                break;
            case 0xb9:
                MOV_CX_IW();
                break;
            case 0xba:
                MOV_DX_IW();
                break;
            case 0xbb:
                MOV_BX_IW();
                break;
            case 0xbc:
                MOV_SP_IW();
                break;
            case 0xbd:
                MOV_BP_IW();
                break;
            case 0xbe:
                MOV_SI_IW();
                break;
            case 0xbf:
                MOV_DI_IW();
                break;

            case 0xc0:
                GRP2_EB_CL(op);
                break; // x186
            case 0xc1:
                GRP2_EW_CL(op);
                break; // x186
            case 0xc2:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xc3:
                RET();
                break;
            case 0xc4:
                LES_GW_EP();
                break;
            case 0xc5:
                LDS_GW_EP();
                break;
            case 0xc6:
                MOV_EB_IB();
                break;
            case 0xc7:
                MOV_EW_IW();
                break;
            case 0xc8:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0xc9:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x186
            case 0xca:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xcb:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xcc:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xcd:
                INT_IB();
                break;
            case 0xce:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xcf:
                IRET();
                break; // x286

            case 0xd0:
                GRP2_EB_1();
                break;
            case 0xd1:
                GRP2_EW_1();
                break;
            case 0xd2:
                GRP2_EB_CL(op);
                break; // x186
            case 0xd3:
                GRP2_EW_CL(op);
                break; // x186
            case 0xd4:
                AAM();
                break;
            case 0xd5:
                AAD();
                break;
            case 0xd6:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xd7:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xd8:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xd9:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xda:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xdb:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xdc:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xdd:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xde:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xdf:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286

            case 0xe0:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xe1:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xe2:
                LOOP_short();
                break;
            case 0xe3:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xe4:
                IN_AL_IB();
                break;
            case 0xe5:
                IN_AX_IB();
                break;
            case 0xe6:
                OUT_IB_AL();
                break;
            case 0xe7:
                OUT_IB_AX();
                break;
            case 0xe8:
                CALL_near();
                break;
            case 0xe9:
                JMP_near();
                break;
            case 0xea:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xeb:
                JMP_short();
                break;
            case 0xec:
                IN_AL_DX();
                break;
            case 0xed:
                IN_AX_DX();
                break;
            case 0xee:
                OUT_DX_AL();
                break;
            case 0xef:
                OUT_DX_AX();
                break;

            case 0xf0:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xf1:
                throw new UnsupportedOperationException(Integer.toHexString(op)); // x286
            case 0xf2:
                REPNE();
                break; // x186
            case 0xf3:
                REPE();
                break; // x186
            case 0xf4:
                HLT();
                break;
            case 0xf5:
                throw new UnsupportedOperationException(Integer.toHexString(op));
            case 0xf6:
                GRP3B();
                break;
            case 0xf7:
                GRP3W();
                break;
            case 0xf8:
                CLC();
                break;
            case 0xf9:
                STC();
                break;
            case 0xfa:
                CLI();
                break;
            case 0xfb:
                STI();
                break;
            case 0xfc:
                CLD();
                break;
            case 0xfd:
                STD();
                break;
            case 0xfe:
                GRP4();
                break;
            case 0xff:
                GRP5();
                break;

            default:
                throw new UnsupportedOperationException("Unkown op code %02x.".formatted(op));
        }

        return 0;
    }

    private void Interrupt() {
        if (segPrefSw) return;
        if (repSW) return;
        if (!regs.getIF()) return;// IF is false and not allowed

        // check mask
        for (int i = 0; i < 8; i++) {
            if ((w_mmsk & (0x01 << i)) == 0) INTxx(i + 8);
        }
        for (int i = 0; i < 8; i++) {
            if ((w_smsk & (0x01 << i)) == 0) INTxx(i + 10);
        }

        UserInt();
    }

    private void UserInt() {
        if (lstUserInt.size() < 1) return;

        UserInt ui = null;
        synchronized (userIntLockObject) {
            ui = lstUserInt.get(0);
            lstUserInt.remove(0);
        }

        short ofs = mem.PeekW(ui.getIntNum() * 4);
        short seg = mem.PeekW(ui.getIntNum() * 4 + 2);
        if (ofs == 0 && seg == 0) return;

        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.FLAG);
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), (short) 0);
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), (short) 0);
        regs.IP = ofs;
        regs.setCS(seg);

        logger.log(Level.DEBUG, "Interrupt:UserINT%02xh".formatted(ui.getIntNum()));
    }

    private void INTxx(int i) {
        if (!interruptTrigger[i]) return;

        interruptTrigger[i] = false;
        short ofs = mem.PeekW(i * 4);
        short seg = mem.PeekW(i * 4 + 2);
        if (ofs == 0 && seg == 0) return;

        iLevel++;

        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.FLAG);
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getCS());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.IP);
        regs.IP = ofs;
        regs.setCS(seg);

        logger.log(Level.DEBUG, "Interrupt:INT%02xh".formatted(i));
    }

    private byte Fetch() {
        byte op = mem.PeekB(regs.getCS_IP());
        regs.IP++;
        return op;
    }

    private short Fetchw() {
        short imm16 = Fetch();
        imm16 |= (short) (Fetch() << 8);
        return imm16;
    }

    private int GetSegment(byte rm, boolean NoSeg, boolean isMod00 /* = false */) {
        if (NoSeg) return 0;

        if (segPrefSw) {
            segPrefSw = false;
            return (int) ((short) regs.sRegs[segPref] << 4);
        } else if (rm != 2 && rm != 3 && rm != 6) // When the specified register is other than BP, DS is used as the default segment.
            return (int) ((short) regs.getDS() << 4);
        else if (rm == 6 && isMod00) // When the specified register is other than BP, DS is used as the default segment.
            return (int) ((short) regs.getDS() << 4);
        else // When the specified register is BP, SS is used as the default segment.
            return (int) ((short) regs.getSS() << 4);
    }

    private int GetMod00RWADR(byte rm, boolean NoSeg /* = false */) {
        int seg = GetSegment(rm, NoSeg, true);

        switch (rm) {
            case 0:
                return (int) (seg + (short) (regs.getBX() + regs.getSI()));
            case 1:
                return (int) (seg + (short) (regs.getBX() + regs.getDI()));
            case 2:
                return (int) (seg + (short) (regs.getBP() + regs.getSI()));
            case 3:
                return (int) (seg + (short) (regs.getBP() + regs.getDI()));
            case 4:
                return (int) (seg + (short) regs.getSI());
            case 5:
                return (int) (seg + (short) regs.getDI());
            case 6:
                short ptr = Fetchw();
                return (int) (seg + (short) ptr);
            case 7:
                return (int) (seg + (short) regs.getBX());
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int GetMod01RWADR(byte rm, boolean NoSeg /* = false */) {
        int seg = GetSegment(rm, NoSeg, false);

        byte disp8 = (byte) Fetch();
        switch (rm) {
            case 0:
                return (int) (seg + (short) (regs.getBX() + regs.getSI() + disp8));
            case 1:
                return (int) (seg + (short) (regs.getBX() + regs.getDI() + disp8));
            case 2:
                return (int) (seg + (short) (regs.getBP() + regs.getSI() + disp8));
            case 3:
                return (int) (seg + (short) (regs.getBP() + regs.getDI() + disp8));
            case 4:
                return (int) (seg + (short) (regs.getSI() + disp8));
            case 5:
                return (int) (seg + (short) (regs.getDI() + disp8));
            case 6:
                return (int) (seg + (short) (regs.getBP() + disp8));
            case 7:
                return (int) (seg + (short) (regs.getBX() + disp8));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int GetMod02RWADR(byte rm, boolean NoSeg /* = false */) {
        int seg = GetSegment(rm, NoSeg, false);

        short disp16 = (short) Fetchw();
        switch (rm) {
            case 0:
                return (int) (seg + (short) (regs.getBX() + regs.getSI() + disp16));
            case 1:
                return (int) (seg + (short) (regs.getBX() + regs.getDI() + disp16));
            case 2:
                return (int) (seg + (short) (regs.getBP() + regs.getSI() + disp16));
            case 3:
                return (int) (seg + (short) (regs.getBP() + regs.getDI() + disp16));
            case 4:
                return (int) (seg + (short) (regs.getSI() + disp16));
            case 5:
                return (int) (seg + (short) (regs.getDI() + disp16));
            case 6:
                return (int) (seg + (short) (regs.getBP() + disp16));
            case 7:
                return (int) (seg + (short) (regs.getBX() + disp16));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int GetSegment() {
        int seg;
        if (segPrefSw) {
            seg = (int) ((short) regs.sRegs[segPref] << 4);
            segPrefSw = false;
        } else
            seg = (int) ((short) regs.getDS() << 4);
        return seg;
    }

    // 0x00
    private void ADD_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "ADD EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        short c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = (short) (a + b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = (short) (a + b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = (short) (a + b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) regs.eRegs[rm];
                else a = (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = (short) (a + b);
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ic);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbAdd(a, b, ic);
        regs.SetCFb(c);
        regs.SetAF(a, b, ic);
    }

    // 0x01
    private void ADD_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "ADD EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW = (short) regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a + b);
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a + b);
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a + b);
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 3:
                a = (short) regs.eRegs[rm];
                b = GW;
                c = (int) (a + b);
                ic = (short) c;
                regs.eRegs[rm] = (short) ic;
                break;
        }

        regs.SetSZPFw(ic);
        regs.SetOFwAdd(a, b, ic);
        regs.SetCFw(c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x02
    private void ADD_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "ADD GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        short c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
                a = GB;
                b = mem.PeekB(GetMod00RWADR(rm, false));
                c = (short) (a + b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 1:
                a = GB;
                b = mem.PeekB(GetMod01RWADR(rm, false));
                c = (short) (a + b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 2:
                a = GB;
                b = mem.PeekB(GetMod02RWADR(rm, false));
                c = (short) (a + b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 3:
                a = GB;
                if (rm < 4) b = (byte) regs.eRegs[rm];
                else b = (byte) (regs.eRegs[rm - 4] >> 8);
                c = (short) (a + b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbAdd(a, b, ic);
        regs.SetCFb(c);
        regs.SetAF(a, b, ic);
    }

    // 0x03
    private void ADD_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "ADD GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // short GW = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW = (short) regs.eRegs[reg];

        switch (mod) {
            case 0:
                a = GW;
                b = (short) mem.PeekW(GetMod00RWADR(rm, false));
                c = (int) (a + b);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 1:
                a = GW;
                b = (short) mem.PeekW(GetMod01RWADR(rm, false));
                c = (int) (a + b);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 2:
                a = GW;
                b = (short) mem.PeekW(GetMod02RWADR(rm, false));
                c = (int) (a + b);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 3:
                a = GW;
                b = (short) regs.eRegs[rm];
                c = (int) (a + b);
                regs.eRegs[reg] = (short) c;
                break;
        }

        regs.SetSZPFw(ic);
        regs.SetOFwAdd(a, b, ic);
        regs.SetCFw(c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x04
    private void ADD_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "ADD AL,$%02x".formatted(imm8));

        byte a = 0, b = 0;
        short c = 0;
        byte ic;

        a = (byte) regs.getAL();
        b = (byte) imm8;
        c = (short) (a + b);
        ic = (byte) (c);
        regs.setAL((byte) c);

        regs.SetSZPFb(ic);
        regs.SetOFbAdd(a, b, ic);
        regs.SetCFb(c);
        regs.SetAF(a, b, ic);
    }

    // 0x05
    private void ADD_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "ADD AX,$%04x".formatted(imm16));

        // short GW = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;
        a = (short) regs.getAX();
        b = imm16;
        c = (int) (a + b);
        ic = (short) c;
        regs.setAX((short) ic);

        regs.SetSZPFw(ic);
        regs.SetOFwAdd(a, b, ic);
        regs.SetCFw(c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x06
    private void PUSH_ES() {
        logger.log(Level.TRACE, "PUSH ES");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getES());
    }

    // 0x07
    private void POP_ES() {
        logger.log(Level.TRACE, "POP ES");
        regs.setES(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x08
    private void OR_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "OR EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        byte c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a | b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a | b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a | b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (byte) regs.eRegs[rm];
                else a = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = (byte) (a | b);
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ic);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0a
    private void OR_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "OR GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        byte c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
                a = GB;
                b = (byte) mem.PeekB(GetMod00RWADR(rm, false));
                c = (byte) (a | b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 1:
                a = GB;
                b = (byte) mem.PeekB(GetMod01RWADR(rm, false));
                c = (byte) (a | b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 2:
                a = GB;
                b = (byte) mem.PeekB(GetMod02RWADR(rm, false));
                c = (byte) (a | b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 3:
                a = GB;
                if (rm < 4) b = (byte) (byte) regs.eRegs[rm];
                else b = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                c = (byte) (a | b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0b
    private void OR_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "OR GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        int a = 0;
        int b = 0;
        int c = 0;
        short ic = 0;
        switch (mod & 0xff) {
            case 0:
                a = regs.eRegs[reg];
                b = mem.PeekW(GetMod00RWADR(rm, false));
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 1:
                a = regs.eRegs[reg];
                b = mem.PeekW(GetMod01RWADR(rm, false));
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 2:
                a = regs.eRegs[reg];
                b = mem.PeekW(GetMod02RWADR(rm, false));
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 3:
                a = regs.eRegs[reg];
                b = regs.eRegs[rm];
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
        }

        regs.SetSZPFw((short) c);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0c
    private void OR_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "OR AL,$%02x".formatted(imm8));
        regs.setAL((byte) (regs.getAL() | imm8));

        regs.SetSZPFb(regs.getAL());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0d
    private void OR_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "OR AX,$%04x".formatted(imm16));
        regs.setAX((short) (regs.getAX() | imm16));

        regs.SetSZPFw((short) regs.getAX());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0e
    private void PUSH_CS() {
        logger.log(Level.TRACE, "PUSH CS");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getCS());
    }

    // 0x0f
    private void POP_CS() {
        logger.log(Level.TRACE, "POP CS");
        regs.setCS(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x10
    private void ADC_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "ADC EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        short c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = (short) (a + b + (regs.getCF() ? 1 : 0));
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = (short) (a + b + (regs.getCF() ? 1 : 0));
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = (short) (a + b + (regs.getCF() ? 1 : 0));
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) regs.eRegs[rm];
                else a = (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = (short) (a + b + (regs.getCF() ? 1 : 0));
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ic);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbAdd(a, b, ic);
        regs.SetCFb(c);
        regs.SetAF(a, b, ic);
    }

    // 0x13
    private void ADC_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "ADC GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // short GW = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW = (short) regs.eRegs[reg];

        switch (mod) {
            case 0:
                a = GW;
                b = (short) mem.PeekW(GetMod00RWADR(rm, false));
                c = (int) (a + b + (regs.getCF() ? 1 : 0));
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 1:
                a = GW;
                b = (short) mem.PeekW(GetMod01RWADR(rm, false));
                c = (int) (a + b + (regs.getCF() ? 1 : 0));
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 2:
                a = GW;
                b = (short) mem.PeekW(GetMod02RWADR(rm, false));
                c = (int) (a + b + (regs.getCF() ? 1 : 0));
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 3:
                a = GW;
                b = (short) regs.eRegs[rm];
                c = (int) (a + b + (regs.getCF() ? 1 : 0));
                regs.eRegs[reg] = (short) c;
                break;
        }

        regs.SetSZPFw(ic);
        regs.SetOFwAdd(a, b, ic);
        regs.SetCFw(c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x14
    private void ADC_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "ADC AL,$%02x".formatted(imm8));

        byte a = 0, b = 0;
        short c = 0;
        byte ic;

        a = (byte) regs.getAL();
        b = (byte) imm8;
        c = (short) (a + b + (regs.getCF() ? 1 : 0));
        ic = (byte) (c);
        regs.setAL((byte) c);

        regs.SetSZPFb(ic);
        regs.SetOFbAdd(a, b, ic);
        regs.SetCFb(c);
        regs.SetAF(a, b, ic);
    }

    // 0x16
    private void PUSH_SS() {
        logger.log(Level.TRACE, "PUSH SS");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getSS());
    }

    // 0x17
    private void POP_SS() {
        logger.log(Level.TRACE, "POP SS");
        regs.setSS(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x19
    private void SBB_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "SBB EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW = (short) regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = (short) (GW + (regs.getCF() ? 1 : 0));
                c = a - b;
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = (short) (GW + (regs.getCF() ? 1 : 0));
                c = a - b;
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = (short) (GW + (regs.getCF() ? 1 : 0));
                c = a - b;
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 3:
                a = (short) regs.eRegs[rm];
                b = (short) (GW + (regs.getCF() ? 1 : 0));
                c = a - b;
                ic = (short) c;
                regs.eRegs[rm] = (short) ic;
                break;
        }

        regs.SetSZPFw(ic);
        regs.SetOFwSub(a, b, ic);
        regs.SetCFw((int) c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x1c
    private void SBB_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "SBB AL,$%02x".formatted(imm8));

        byte a = (byte) regs.getAL();
        byte b = (byte) (imm8 + (regs.getCF() ? 1 : 0));
        int c = a - b;
        byte ic = (byte) c;
        regs.setAL(ic);

        regs.SetSZPFb(ic);
        regs.SetOFbSub((byte) a, (byte) b, ic);
        regs.SetCFb((short) c);
        regs.SetAF((byte) a, (byte) b, ic);
    }

    // 0x1e
    private void PUSH_DS() {
        logger.log(Level.TRACE, "PUSH DS");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getDS());
    }

    // 0x1f
    private void POP_DS() {
        logger.log(Level.TRACE, "POP DS");
        regs.setDS(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x20
    private void AND_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "AND EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        byte c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);
        int ptr;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a & b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a & b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a & b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (byte) regs.eRegs[rm];
                else a = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = (byte) (a & b);
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ic);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x21
    private void AND_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "AND EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW;
        GW = (short) regs.eRegs[reg];
        int ptr;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a & b);
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a & b);
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a & b);
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 3:
                a = (short) regs.eRegs[rm];
                b = GW;
                c = (int) (a & b);
                ic = (short) c;
                regs.eRegs[rm] = (short) ic;
                break;
        }

        regs.SetSZPFw(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x22
    private void AND_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "AND GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        byte c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
                a = GB;
                b = (byte) mem.PeekB(GetMod00RWADR(rm, false));
                c = (byte) (a & b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 1:
                a = GB;
                b = (byte) mem.PeekB(GetMod01RWADR(rm, false));
                c = (byte) (a & b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 2:
                a = GB;
                b = (byte) mem.PeekB(GetMod02RWADR(rm, false));
                c = (byte) (a & b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 3:
                a = GB;
                if (rm < 4) b = (byte) (byte) regs.eRegs[rm];
                else b = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                c = (byte) (a & b);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x23
    private void AND_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "AND GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        int a = 0;
        int b = 0;
        int c = 0;
        short ic = 0;
        switch (mod) {
            case 0:
                a = regs.eRegs[reg];
                b = mem.PeekW(GetMod00RWADR(rm, false));
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 1:
                a = regs.eRegs[reg];
                b = mem.PeekW(GetMod01RWADR(rm, false));
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 2:
                a = regs.eRegs[reg];
                b = mem.PeekW(GetMod02RWADR(rm, false));
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 3:
                a = regs.eRegs[reg];
                b = regs.eRegs[rm];
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
        }

        regs.SetSZPFw((short) ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x24
    private void AND_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "AND AL,$%02x".formatted(imm8));
        regs.setAL((byte) (regs.getAL() & imm8));

        regs.SetSZPFb(regs.getAL());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x25
    private void AND_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "AND AX,$%04x".formatted(imm16));
        regs.setAX((short) (regs.getAX() & imm16));

        regs.SetSZPFw((short) regs.getAX());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x26
    private void ES() {
        segPrefSw = true;
        segPref = 0; // 0=ES
        logger.log(Level.TRACE, "ES");
    }

    // 0x28
    private void SUB_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "SUB EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = a - b;
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = a - b;
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = mem.PeekB(ptr);
                b = GB;
                c = a - b;
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) regs.eRegs[rm];
                else a = (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = a - b;
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ic);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbSub((byte) a, (byte) b, ic);
        regs.SetCFb((short) c);
        regs.SetAF((byte) a, (byte) b, ic);
    }

    // 0x29
    private void SUB_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "SUB EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW = (short) regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = a - b;
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = a - b;
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = a - b;
                ic = (short) c;
                mem.PokeW(ptr, (short) ic);
                break;
            case 3:
                a = (short) regs.eRegs[rm];
                b = GW;
                c = a - b;
                ic = (short) c;
                regs.eRegs[rm] = (short) ic;
                break;
        }

        regs.SetSZPFw(ic);
        regs.SetOFwSub(a, b, ic);
        regs.SetCFw((int) c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x2a
    private void SUB_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "SUB GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
                a = GB;
                b = mem.PeekB(GetMod00RWADR(rm, false));
                c = a - b;
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 1:
                a = GB;
                b = mem.PeekB(GetMod01RWADR(rm, false));
                c = a - b;
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 2:
                a = GB;
                b = mem.PeekB(GetMod02RWADR(rm, false));
                c = a - b;
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
            case 3:
                a = GB;
                if (rm < 4) b = (byte) regs.eRegs[rm];
                else b = (byte) (regs.eRegs[rm - 4] >> 8);
                c = a - b;
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbSub((byte) a, (byte) b, ic);
        regs.SetCFb((short) c);
        regs.SetAF((byte) a, (byte) b, ic);
    }

    // 0x2b
    private void SUB_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "SUB GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;
        a = (short) regs.eRegs[reg];
        switch (mod) {
            case 0:
                b = (short) mem.PeekW(GetMod00RWADR(rm, false));
                break;
            case 1:
                b = (short) mem.PeekW(GetMod01RWADR(rm, false));
                break;
            case 2:
                b = (short) mem.PeekW(GetMod02RWADR(rm, false));
                break;
            case 3:
                b = (short) regs.eRegs[rm];
                break;
        }
        c = a - b;
        ic = (short) c;
        regs.eRegs[reg] = (short) ic;

        regs.SetSZPFw(ic);
        regs.SetOFwSub(a, b, ic);
        regs.SetCFw((int) c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x2c
    private void SUB_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "SUB AL,$%02x".formatted(imm8));

        byte a = (byte) regs.getAL();
        byte b = (byte) imm8;
        int c = a - b;
        byte ic = (byte) c;
        regs.setAL(ic);

        regs.SetSZPFb(ic);
        regs.SetOFbSub((byte) a, (byte) b, ic);
        regs.SetCFb((short) c);
        regs.SetAF((byte) a, (byte) b, ic);
    }

    // 0x2d
    private void SUB_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "SUB AX,$%04x".formatted(imm16));

        // short GW = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;
        a = (short) regs.getAX();
        b = imm16;
        c = a - b;
        ic = (short) c;
        regs.setAX((short) ic);

        regs.SetSZPFw(ic);
        regs.SetOFwSub(a, b, ic);
        regs.SetCFw((int) c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x2e
    private void CS() {
        segPrefSw = true;
        segPref = 1; // 1=CS
        logger.log(Level.TRACE, "CS");
    }

    private void DAS() {
        logger.log(Level.TRACE, "DAS");
        // TBD
    }

    // 0x30
    private void XOR_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "XOR EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        byte c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);
        int ptr;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a ^ b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a ^ b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (byte) mem.PeekB(ptr);
                b = GB;
                c = (byte) (a ^ b);
                ic = (byte) c;
                mem.PokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (byte) regs.eRegs[rm];
                else a = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = (byte) (a ^ b);
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ic);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ic << 8));
                break;
        }

        regs.SetSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x32
    private void XOR_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "XOR GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // short GW = regs.eRegs[reg];
        byte a = 0;
        byte b = 0;
        byte c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);

        a = GB;
        switch (mod) {
            case 0:
                b = (byte) mem.PeekB(GetMod00RWADR(rm, false));
                break;
            case 1:
                b = (byte) mem.PeekB(GetMod01RWADR(rm, false));
                break;
            case 2:
                b = (byte) mem.PeekB(GetMod02RWADR(rm, false));
                break;
            case 3:
                if (rm < 4) b = (byte) (byte) regs.eRegs[rm];
                else b = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }
        c = (byte) (a ^ b);
        ic = (byte) c;
        if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | ic);
        else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (ic << 8));

        regs.SetSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x33
    private void XOR_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "XOR GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // short GW = regs.eRegs[reg];
        int a = 0;
        int b = 0;
        int c = 0;
        short ic = 0;
        a = regs.eRegs[reg];
        switch (mod) {
            case 0:
                b = mem.PeekW(GetMod00RWADR(rm, false));
                break;
            case 1:
                b = mem.PeekW(GetMod01RWADR(rm, false));
                break;
            case 2:
                b = mem.PeekW(GetMod02RWADR(rm, false));
                break;
            case 3:
                b = regs.eRegs[rm];
                break;
        }
        c = a ^ b;
        ic = (short) c;
        regs.eRegs[reg] = ic;

        regs.SetSZPFw((short) ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x34
    private void XOR_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "XOR AL,$%02x".formatted(imm8));
        regs.setAL((byte) (regs.getAL() ^ imm8));

        regs.SetSZPFb(regs.getAL());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x35
    private void XOR_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "XOR AX,$%04x".formatted(imm16));
        regs.setAX((short) (regs.getAX() ^ imm16));

        regs.SetSZPFw((short) regs.getAX());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x36
    private void SS() {
        segPrefSw = true;
        segPref = 2; // 0=SS
        logger.log(Level.TRACE, "SS");
    }

    // 0x38
    private void CMP_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "CMP EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
                a = mem.PeekB(GetMod00RWADR(rm, false));
                b = GB;
                c = a - b;
                ic = (byte) c;
                break;
            case 1:
                a = mem.PeekB(GetMod01RWADR(rm, false));
                b = GB;
                c = a - b;
                ic = (byte) c;
                break;
            case 2:
                a = mem.PeekB(GetMod02RWADR(rm, false));
                b = GB;
                c = a - b;
                ic = (byte) c;
                break;
            case 3:
                if (rm < 4) a = (byte) regs.eRegs[rm];
                else a = (byte) (regs.eRegs[rm - 4] >> 8);
                b = GB;
                c = a - b;
                ic = (byte) c;
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbSub((byte) a, (byte) b, (byte) c);
        regs.SetCFb((short) c);
        regs.SetAF((byte) a, (byte) b, (byte) c);
    }

    // 0x39
    private void CMP_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "CMP EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short GW = (short) regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a - b);
                ic = (short) c;
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a - b);
                ic = (short) c;
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                a = (short) mem.PeekW(ptr);
                b = GW;
                c = (int) (a - b);
                ic = (short) c;
                break;
            case 3:
                a = (short) regs.eRegs[rm];
                b = GW;
                c = (int) (a - b);
                ic = (short) c;
                break;
        }

        regs.SetSZPFw(ic);
        regs.SetOFwSub(a, b, ic);
        regs.SetCFw(c);
        regs.SetAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x3a
    private void CMP_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "CMP GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
                a = GB;
                b = mem.PeekB(GetMod00RWADR(rm, false));
                c = a - b;
                ic = (byte) c;
                break;
            case 1:
                a = GB;
                b = mem.PeekB(GetMod01RWADR(rm, false));
                c = a - b;
                ic = (byte) c;
                break;
            case 2:
                a = GB;
                b = mem.PeekB(GetMod02RWADR(rm, false));
                c = a - b;
                ic = (byte) c;
                break;
            case 3:
                a = GB;
                if (rm < 4) b = (byte) regs.eRegs[rm];
                else b = (byte) (regs.eRegs[rm - 4] >> 8);
                c = a - b;
                ic = (byte) c;
                break;
        }

        regs.SetSZPFb(ic);
        regs.SetOFbSub((byte) a, (byte) b, (byte) c);
        regs.SetCFb((short) c);
        regs.SetAF((byte) a, (byte) b, (byte) c);
    }

    // 0x3b
    private void CMP_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "CMP GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // short GW = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        switch (mod) {
            case 0:
                a = (short) regs.eRegs[reg];
                b = (short) mem.PeekW(GetMod00RWADR(rm, false));
                c = a - b;
                break;
            case 1:
                a = (short) regs.eRegs[reg];
                b = (short) mem.PeekW(GetMod01RWADR(rm, false));
                c = a - b;
                break;
            case 2:
                a = (short) regs.eRegs[reg];
                b = (short) mem.PeekW(GetMod02RWADR(rm, false));
                c = a - b;
                break;
            case 3:
                a = (short) regs.eRegs[reg];
                b = (short) regs.eRegs[rm];
                c = a - b;
                break;
        }
        short ans = (short) c;

        regs.SetSZPFw(ans);
        regs.SetOFwSub((short) a, (short) b, (short) c);
        regs.SetCFw((int) c);
        regs.SetAF((byte) a, (byte) b, (byte) c);
    }

    // 0x3c
    private void CMP_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "CMP AL,$%02x".formatted(imm8));

        int ians = (byte) regs.getAL() - imm8;
        byte ans = (byte) ians;

        regs.SetSZPFb(ans);
        regs.SetOFbSub((byte) regs.getAL(), (byte) imm8, (byte) ans);
        regs.SetCFb((short) ians);
        regs.SetAF((byte) regs.getAL(), (byte) imm8, (byte) ans);
    }

    // 0x3d
    private void CMP_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "CMP AX,$%04x".formatted(imm16));
        int ians = (short) regs.getAX() - (short) imm16;
        short ans = (short) ians;

        regs.SetSZPFw(ans);
        regs.SetOFwSub((short) regs.getAX(), (short) imm16, (short) ans);
        regs.SetCFw((int) ians);
        regs.SetAF((byte) regs.getAX(), (byte) imm16, (byte) ans);
    }

    // 0x3e
    private void DS() {
        segPrefSw = true;
        segPref = 3; // 3=DS
        logger.log(Level.TRACE, "DS");
    }

    // 0x40
    private void INC_AX() {
        logger.log(Level.TRACE, "INC AX");

        int a = regs.getAX();
        int b = 1;
        int ans = a + b;
        regs.setAX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x41
    private void INC_CX() {
        logger.log(Level.TRACE, "INC CX");

        int a = regs.getCX();
        int b = 1;
        int ans = a + b;
        regs.setCX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x42
    private void INC_DX() {
        logger.log(Level.TRACE, "INC DX");

        int a = regs.getDX();
        int b = 1;
        int ans = a + b;
        regs.setDX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x43
    private void INC_BX() {
        logger.log(Level.TRACE, "INC BX");

        int a = regs.getBX();
        int b = 1;
        int ans = a + b;
        regs.setBX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x44
    private void INC_SP() {
        logger.log(Level.TRACE, "INC SP");

        int a = regs.getSP();
        int b = 1;
        int ans = a + b;
        regs.setSP((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x45
    private void INC_BP() {
        logger.log(Level.TRACE, "INC BP");

        int a = regs.getBP();
        int b = 1;
        int ans = a + b;
        regs.setBP((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x46
    private void INC_SI() {
        logger.log(Level.TRACE, "INC SI");

        int a = regs.getSI();
        int b = 1;
        int ans = a + b;
        regs.setSI((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x47
    private void INC_DI() {
        logger.log(Level.TRACE, "INC DI");

        int a = regs.getDI();
        int b = 1;
        int ans = a + b;
        regs.setDI((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwAdd((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x48
    private void DEC_AX() {
        logger.log(Level.TRACE, "DEC AX");

        int a = regs.getAX();
        int b = 1;
        int ans = a - b;
        regs.setAX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x49
    private void DEC_CX() {
        logger.log(Level.TRACE, "DEC CX");

        int a = regs.getCX();
        int b = 1;
        int ans = a - b;
        regs.setCX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4a
    private void DEC_DX() {
        logger.log(Level.TRACE, "DEC DX");

        int a = regs.getDX();
        int b = 1;
        int ans = a - b;
        regs.setDX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4b
    private void DEC_BX() {
        logger.log(Level.TRACE, "DEC BX");

        int a = regs.getBX();
        int b = 1;
        int ans = a - b;
        regs.setBX((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4c
    private void DEC_SP() {
        logger.log(Level.TRACE, "DEC SP");

        int a = regs.getSP();
        int b = 1;
        int ans = a - b;
        regs.setSP((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4d
    private void DEC_BP() {
        logger.log(Level.TRACE, "DEC BP");

        int a = regs.getBP();
        int b = 1;
        int ans = a - b;
        regs.setBP((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4e
    private void DEC_SI() {
        logger.log(Level.TRACE, "DEC SI");

        int a = regs.getSI();
        int b = 1;
        int ans = a - b;
        regs.setSI((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4f
    private void DEC_DI() {
        logger.log(Level.TRACE, "DEC DI");

        int a = regs.getDI();
        int b = 1;
        int ans = a - b;
        regs.setDI((short) ans);

        regs.SetSZPFw((short) ans);
        regs.SetOFwSub((short) a, (short) b, (short) ans);
        regs.SetCFw((int) ans);
        regs.SetAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x50
    private void PUSH_AX() {
        logger.log(Level.TRACE, "PUSH AX");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getAX());
    }

    // 0x51
    private void PUSH_CX() {
        logger.log(Level.TRACE, "PUSH CX");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getCX());
    }

    // 0x52
    private void PUSH_DX() {
        logger.log(Level.TRACE, "PUSH DX");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getDX());
    }

    // 0x53
    private void PUSH_BX() {
        logger.log(Level.TRACE, "PUSH BX");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getBX());
    }

    // 0x54
    private void PUSH_SP() {
        logger.log(Level.TRACE, "PUSH SP");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getSP());
    }

    // 0x55
    private void PUSH_BP() {
        logger.log(Level.TRACE, "PUSH BP");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getBP());
    }

    // 0x56
    private void PUSH_SI() {
        logger.log(Level.TRACE, "PUSH SI");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getSI());
    }

    // 0x57
    private void PUSH_DI() {
        logger.log(Level.TRACE, "PUSH DI");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getDI());
    }

    // 0x58
    private void POP_AX() {
        logger.log(Level.TRACE, "POP AX");
        regs.setAX(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x59
    private void POP_CX() {
        logger.log(Level.TRACE, "POP CX");
        regs.setCX(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5a
    private void POP_DX() {
        logger.log(Level.TRACE, "POP DX");
        regs.setDX(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5b
    private void POP_BX() {
        logger.log(Level.TRACE, "POP BX");
        regs.setBX(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5c
    private void POP_SP() {
        logger.log(Level.TRACE, "POP SP");
        regs.setSP(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5d
    private void POP_BP() {
        logger.log(Level.TRACE, "POP BP");
        regs.setBP(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5e
    private void POP_SI() {
        logger.log(Level.TRACE, "POP SI");
        regs.setSI(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5f
    private void POP_DI() {
        logger.log(Level.TRACE, "POP DI");
        regs.setDI(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x60
    private void PUSHA() {
        logger.log(Level.TRACE, "PUSHA");
        short SP = regs.getSP();
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getAX());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getCX());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getDX());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getBX());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), SP);
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getBP());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getSI());
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.getDI());
    }

    // 0x61
    private void POPA() {
        logger.log(Level.TRACE, "POPA");
        short SP;

        regs.setDI( mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setSI( mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setBP(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        SP = mem.PeekW(regs.getSS_SP());
        regs.addSP(2);
        regs.setBX(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setDX( mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setCX(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setAX( mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setSP(SP);
    }

    // 0x72
    private void JB_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JB short:$%02x".formatted(imm8));

        if (regs.getCF()) {
            regs.IP += imm8;
        }
    }

    // 0x73
    private void JNB_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNB short:$%02x".formatted(imm8));

        if (!regs.getCF()) {
            regs.IP += imm8;
        }
    }

    // 0x74
    private void JZ_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JZ short:$%02x".formatted(imm8));

        if (regs.getZF()) {
            regs.IP += imm8;
        }
    }

    // 0x75
    private void JNZ_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNZ short:$%02x".formatted(imm8));

        if (!regs.getZF()) {
            regs.IP += imm8;
        }
    }

    // 0x76
    private void JBE_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JBE short:$%02x".formatted(imm8));

        if (regs.getCF() || regs.getZF()) {
            regs.IP += imm8;
        }
    }

    // 0x77
    private void JNBE_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNBE short:$%02x".formatted(imm8));

        if (!regs.getCF() && !regs.getZF()) // cmp then op1<op2
        {
            regs.IP += imm8;
        }
    }

    // 0x78
    private void JS_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JS short:$%02x".formatted(imm8));

        if (regs.getSF()) {
            regs.IP += imm8;
        }
    }

    // 0x79
    private void JNS_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNS short:$%02x".formatted(imm8));

        if (!regs.getSF()) {
            regs.IP += imm8;
        }
    }

    // 0x7d
    private void JNL_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNL short:$%02x".formatted(imm8));

        if (regs.getSF() == regs.getOF()) {
            regs.IP += imm8;
        }
    }

    // 0x7e
    private void JNG_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNG short:$%02x".formatted(imm8));

        if (regs.getZF() || regs.getSF() != regs.getOF()) {
            regs.IP += imm8;
        }
    }

    // 0x7f
    private void JNLE_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JNLE short:$%02x".formatted(imm8));

        if (!regs.getZF() && regs.getSF() == regs.getOF()) {
            regs.IP += imm8;
        }
    }

    // 0x80
    private void GRP1B() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        short IB;
        int ians;
        byte ans;
        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }
        IB = Fetch();

        switch (reg) {
            case 0: // ADD EB,IB
                logger.log(Level.TRACE, "ADD EB,$%02x".formatted(IB));
                ians = (byte) EB + (byte) IB;
                ans = (byte) ians;
                regs.SetSZPFb((byte) ians);
                regs.SetOFbAdd((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 1: // OR EB,IB
                logger.log(Level.TRACE, "OR EB,$%02x".formatted(IB));
                ians = EB | (byte) IB;
                ans = (byte) ians;
                regs.SetSZPFb((byte) ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 2: // ADC EB,IB
                logger.log(Level.TRACE, "ADC EB,$%02x".formatted(IB));
                ians = EB + IB + (regs.getCF() ? 1 : 0);
                ans = (byte) ians;
                regs.SetSZPFb((byte) ians);
                regs.SetOFbAdd((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 3: // SBB
                logger.log(Level.TRACE, "SBB EB,$%02x".formatted(IB));
                ians = EB - (IB + (regs.getCF() ? 1 : 0));
                ans = (byte) ians;
                regs.SetSZPFb((byte) ians);
                regs.SetOFbSub((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 4: // AND EB,IB
                logger.log(Level.TRACE, "AND EB,$%02x".formatted(IB));
                ians = EB & IB;
                ans = (byte) ians;
                regs.SetSZPFb((byte) ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 5: // SUB EB,IB
                logger.log(Level.TRACE, "SUB EB,$%02x".formatted(IB));
                ians = (byte) EB - (byte) IB;
                ans = (byte) ians;
                regs.SetSZPFb((byte) ians);
                regs.SetOFbSub((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 6:
                logger.log(Level.TRACE, "XOR EB,$%02x".formatted(IB));
                ians = EB ^ IB;
                ans = (byte) ians;
                regs.SetSZPFb((byte) ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 7: // CMP EB,IB
                logger.log(Level.TRACE, "CMP EB,$%02x".formatted(IB));
                ians = (byte) EB - (byte) IB;
                ans = (byte) ians;
                regs.SetSZPFb((byte) ans);
                regs.SetOFbSub((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                break;
        }
    }

    // 0x81
    private void GRP1W() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        short IW;
        int ians;
        short ans;
        short EW = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 3:
                EW = regs.eRegs[rm];
                break;
        }
        IW = Fetchw();

        switch (reg) {
            case 0: // ADD EW,IW
                logger.log(Level.TRACE, "ADD EW,$%04x".formatted(IW));
                ians = (short) EW + IW;
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.SetOFwAdd((short) EW, (short) IW, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IW, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 1: // OR EW,IW
                logger.log(Level.TRACE, "OR EW,$%04x".formatted(IW));
                ians = EW | (short) IW;
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 2: // ADC EW,IW
                logger.log(Level.TRACE, "ADC EW,$%04x".formatted(IW));
                ians = EW + IW + (regs.getCF() ? 1 : 0);
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.SetOFwAdd((short) EW, (short) IW, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IW, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 3:
                throw new UnsupportedOperationException();
            case 4: // AND EW,IW
                logger.log(Level.TRACE, "AND EW,$%04x".formatted(IW));
                ians = EW & IW;
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 5: // SUB EW,IW
                logger.log(Level.TRACE, "SUB EW,$%04x".formatted(IW));
                ians = (short) EW - IW;
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.SetOFwSub((short) EW, (short) IW, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IW, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 6: // XOR EW,IW
                logger.log(Level.TRACE, "XOR EW,$%04x".formatted(IW));
                ians = EW ^ IW;
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 7: // CMP EW,IW
                logger.log(Level.TRACE, "CMP EW,$%04x", IW);
                ians = EW - (short) IW;
                ans = (short) ians;
                regs.SetSZPFw((short) ans);
                regs.SetOFwSub((short) EW, (short) IW, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IW, (byte) ans);
                break;
        }
    }

    // 0x83
    private void GRP1WB() {
        byte modrw = Fetch();

        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short EW = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 3:
                EW = regs.eRegs[rm];
                break;
        }
        byte IB;
        IB = Fetch();
        int ians;
        short ans;

        switch (reg) {
            case 0: // ADD EW,IB
                logger.log(Level.TRACE, "ADD EW,$%02x".formatted(IB));
                ians = (short) EW + (byte) IB;
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.SetOFwAdd((short) EW, (short) IB, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 1: // OR EW,IB
                logger.log(Level.TRACE, "OR EW,$%02x".formatted(IB));
                ians = (short) EW | (byte) IB;
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 2: // ADC EW,IB
                logger.log(Level.TRACE, "ADC EW,$%02x".formatted(IB));
                ians = (short) EW + (byte) (IB + (regs.getCF() ? 1 : 0));
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.SetOFwAdd((short) EW, (short) IB, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 3: // SBB EW,IB
                logger.log(Level.TRACE, "SBB EW,$%02x".formatted(IB));
                ians = (short) EW - (byte) (IB + (regs.getCF() ? 1 : 0));
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.SetOFwSub((short) EW, (short) IB, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 4: // AND EW,IB
                logger.log(Level.TRACE, "AND EW,$%02x".formatted(IB));
                ians = (short) EW & (byte) IB;
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 5: // SUB EW,IB
                logger.log(Level.TRACE, "SUB EW,$%02x".formatted(IB));
                ians = (short) EW - (byte) IB;
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.SetOFwSub((short) EW, (short) IB, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 6: // XOR EW,IB
                logger.log(Level.TRACE, "XOR EW,$%02x".formatted(IB));
                ians = (short) EW ^ (byte) IB;
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 7: // CMP EW,IB
                logger.log(Level.TRACE, "CMP EW,$%02x".formatted(IB));
                ians = (short) EW - (byte) IB;
                ans = (short) ians;
                regs.SetSZPFw((short) ians);
                regs.SetOFwSub((short) EW, (short) (byte) IB, (short) ans);
                regs.SetCFw((int) ians);
                regs.SetAF((byte) EW, (byte) IB, (byte) ans);
                break;
        }
    }

    // 0x84
    private void TEST_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "TEST EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        byte GB;
        if (reg < 4) GB = (byte) regs.eRegs[reg];
        else GB = (byte) (regs.eRegs[reg - 4] >> 8);
        switch (mod) {
            case 0:
            case 1:
            case 2:
            case 3:
                byte ans = (byte) (EB & GB);
                regs.SetSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
        }
    }

    // 0x85
    private void TEST_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "TEST EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short EW = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = mem.PeekW(ptr);
                break;
            case 3:
                EW = regs.eRegs[rm];
                break;
        }

        short GW;
        GW = (short) regs.eRegs[reg];
        switch (mod) {
            case 0:
            case 1:
            case 2:
            case 3:
                short ans = (short) (EW & GW);
                regs.SetSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
        }

    }

    // 0x86
    private void XCHG_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "XCHG EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);

        // GB <-> EB
        byte p = GB;
        GB = EB;
        EB = p;

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeB(ptr, (byte) EB);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) EB);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (EB << 8));
                break;
        }

        if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (byte) GB);
        else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (GB << 8));
    }

    // 0x87
    private void XCHG_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "XCHG EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short EW = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 3:
                EW = (short) regs.eRegs[rm];
                break;
        }

        short GW;
        GW = (short) regs.eRegs[reg];

        // GW <-> EW
        short p = GW;
        GW = EW;
        EW = p;

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeW(ptr, (short) EW);
                break;
            case 3:
                regs.eRegs[rm] = (short) EW;
                break;
        }
        regs.eRegs[reg] = (short) GW;

    }

    // 0x88
    private void MOV_EB_GB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "MOV EB,GB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                // EB = (byte)mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                // EB = (byte)mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                // EB = (byte)mem.PeekB(ptr);
                break;
            case 3:
                // if (rm < 4) EB = (byte)(byte)regs.eRegs[rm];
                // else EB = (byte)(byte)(regs.eRegs[rm - 4] >> 8);
                break;
        }

        byte GB;
        if (reg < 4) GB = (byte) (byte) regs.eRegs[reg];
        else GB = (byte) (byte) (regs.eRegs[reg - 4] >> 8);

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeB(ptr, (byte) GB);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) GB);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (GB << 8));
                break;
        }
    }

    // 0x89
    private void MOV_EW_GW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "MOV EW,GW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short GW = regs.eRegs[reg];
        switch (mod) {
            case 0:
                mem.PokeW(GetMod00RWADR(rm, false), GW);
                break;
            case 1:
                mem.PokeW(GetMod01RWADR(rm, false), GW);
                break;
            case 2:
                mem.PokeW(GetMod02RWADR(rm, false), GW);
                break;
            case 3:
                regs.eRegs[rm] = GW;
                break;
        }
    }

    // 0x8a
    private void MOV_GB_EB() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "MOV GB,EB modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (byte) EB);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (EB << 8));
                break;
            case 3:
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (byte) EB);
                else regs.eRegs[reg - 4] = (short) ((byte) regs.eRegs[reg - 4] | (EB << 8));
                break;
        }
    }

    // 0x8b
    private void MOV_GW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "MOV GW,EW modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        switch (mod) {
            case 0:
                regs.eRegs[reg] = mem.PeekW(GetMod00RWADR(rm, false));
                break;
            case 1:
                regs.eRegs[reg] = mem.PeekW(GetMod01RWADR(rm, false));
                break;
            case 2:
                regs.eRegs[reg] = mem.PeekW(GetMod02RWADR(rm, false));
                break;
            case 3:
                regs.eRegs[reg] = regs.eRegs[rm];
                break;
        }
    }

    // 0x8c
    private void MOV_EW_SW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "MOV EW,SW modrw:$%02x", modrw);

        byte reg = (byte) ((modrw & 0x18) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short SW = regs.sRegs[reg];
        switch (mod) {
            case 0:
                mem.PokeW(GetMod00RWADR(rm, false), SW);
                break;
            case 1:
                mem.PokeW(GetMod01RWADR(rm, false), SW);
                break;
            case 2:
                mem.PokeW(GetMod02RWADR(rm, false), SW);
                break;
            case 3:
                regs.eRegs[rm] = SW;
                break;
        }
    }

    // 0x8d
    private void LEA_GW_M() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "LEA GW,M modrw:$%02x", modrw);

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        switch (mod) {
            case 0:
                regs.eRegs[reg] = (short) GetMod00RWADR(rm, true);
                break;
            case 1:
                regs.eRegs[reg] = (short) GetMod01RWADR(rm, true);
                break;
            case 2:
                regs.eRegs[reg] = (short) GetMod02RWADR(rm, true);
                break;
            case 3:
                throw new UnsupportedOperationException();
        }
    }

    // 0x8e
    private void MOV_SW_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "MOV SW,EW modrw:$%02x", modrw);

        byte reg = (byte) ((modrw & 0x18) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        switch (mod) {
            case 0:
                regs.sRegs[reg] = mem.PeekW(GetMod00RWADR(rm, false));
                break;
            case 1:
                regs.sRegs[reg] = mem.PeekW(GetMod01RWADR(rm, false));
                break;
            case 2:
                regs.sRegs[reg] = mem.PeekW(GetMod02RWADR(rm, false));
                break;
            case 3:
                short r = regs.eRegs[rm];
                regs.sRegs[reg] = r;
                break;
        }
    }

    // 0x8f
    private void POP_EW() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "POP EW modrw:$%02x", modrw);

        byte reg = (byte) ((modrw & 0x18) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        int ptr;
        short imm16 = mem.PeekW(regs.getSS_SP());
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                mem.PokeW(ptr, imm16);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                mem.PokeW(ptr, imm16);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                mem.PokeW(ptr, imm16);
                break;
            case 3:
                regs.eRegs[rm] = imm16;
                break;
        }
        regs.addSP(2);
    }

    // 0x84
    private void TEST_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "TEST AL,$%02x", imm8);

        byte ans = (byte) (regs.getAL() & imm8);
        regs.setOF(false);
        regs.setCF(false);
        regs.setZF(ans == 0);
        regs.setSF((ans & 0x80) != 0);
        regs.setPF((ans & 0x01) != 0);
        regs.setAF(false); // TBD
    }

    // 0x90
    private void NOP() {
        logger.log(Level.TRACE, "NOP");

    }

    // 0x91
    private void XCHG_CX_AX() {
        logger.log(Level.TRACE, "XCHG_CX_AX");

        short v = regs.getAX();
        regs.setAX(regs.getCX());
        regs.setCX(v);
    }

    // 0x92
    private void XCHG_DX_AX() {
        logger.log(Level.TRACE, "XCHG_DX_AX");

        short v = regs.getAX();
        regs.setAX(regs.getDX());
        regs.setDX(v);
    }

    // 0x93
    private void XCHG_BX_AX() {
        logger.log(Level.TRACE, "XCHG_BX_AX");

        short v = regs.getAX();
        regs.setAX(regs.getBX());
        regs.setBX(v);
    }

    // 0x94
    private void XCHG_SP_AX() {
        logger.log(Level.TRACE, "XCHG_SP_AX");

        short v = regs.getAX();
        regs.setAX(regs.getSP());
        regs.setSP(v);
    }

    // 0x96
    private void XCHG_SI_AX() {
        logger.log(Level.TRACE, "XCHG_SI_AX");

        short v = regs.getAX();
        regs.setAX(regs.getSI());
        regs.setSI(v);
    }

    // 0x98
    private void CBW() {
        logger.log(Level.TRACE, "CBW");
        regs.setAH((byte) ((regs.getAL() & 0x80) != 0 ? 0xff : 0x00));
    }

    // 0x99
    private void CWD() {
        logger.log(Level.TRACE, "CWD");
        regs.setDX((short) ((regs.getAX() & 0x8000) != 0 ? 0xffff : 0x0000));
    }

    // 0x9c
    private void PUSHF() {
        logger.log(Level.TRACE, "PUSHF");
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.FLAG);
    }

    // 0x9d
    private void POPF() {
        logger.log(Level.TRACE, "POPF");
        regs.FLAG = mem.PeekW(regs.getSS_SP());
        regs.addSP(2);
    }

    // 0xa0
    private void MOV_AL_OB() {
        int seg = GetSegment();
        short ptr = Fetchw();
        logger.log(Level.TRACE, "MOV AL,[$%04x]".formatted(ptr));

        regs.setAL(mem.PeekB((int) (seg + ptr)));
    }

    // 0xa1
    private void MOV_AX_OW() {
        int seg = GetSegment();
        short ptr = Fetchw();
        logger.log(Level.TRACE, "MOV AX,[$%04x]".formatted(ptr));

        regs.setAX(mem.PeekW((int) (seg + ptr)));
    }

    // 0xa2
    private void MOV_OB_AL() {
        int seg = GetSegment();
        short ptr = Fetchw();
        logger.log(Level.TRACE, "MOV [$%04x],AL".formatted(ptr));

        mem.PokeB((int) (seg + ptr), regs.getAL());
    }

    // 0xa3
    private void MOV_OW_AX() {
        int seg = GetSegment();
        short ptr = Fetchw();
        logger.log(Level.TRACE, "MOV [$%04x],AX".formatted(ptr));

        mem.PokeW((int) (seg + ptr), regs.getAX());
    }

    // 0xa4
    private void MOVSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "MOVSB [ES:DI]:%05x [DS:SI]:%05x".formatted(regs.getES_DI(), regs.getDS_SI()));
            mem.PokeB(regs.getES_DI(), mem.PeekB(regs.getDS_SI()));
            regs.addDI((short) (regs.getDF() ? -1 : 1));
            regs.addSI((short) (regs.getDF() ? -1 : 1));
        }

        if (repSW) {
            // Simple repeat for MOVS
            // REPNE is not possible (I think so, so I have not dealt with it yet)

            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0) { // || ((repType == 0 && !regs.ZF) || (repType != 0 && regs.ZF)))
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xa5
    private void MOVSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "MOVSW [ES:DI]:%05x [DS:SI]:%05x".formatted(regs.getES_DI(), regs.getDS_SI()));
            mem.PokeW(regs.getES_DI(), mem.PeekW(regs.getDS_SI()));
            regs.addDI((short) (regs.getDF() ? -2 : 2));
            regs.addSI((short) (regs.getDF() ? -2 : 2));
        }

        if (repSW) {
            // Simple repeat for MOVS
            // REPNE is not possible (I think so, so I have not dealt with it yet)
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0) { // || ((repType == 0 && !regs.ZF) || (repType != 0 && regs.ZF)))
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xa6
    private void CMPSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            byte dsv = mem.PeekB(regs.getDS_SI());
            byte edv = mem.PeekB(regs.getES_DI());

            logger.log(Level.TRACE, "CMPSB [DS:SI] val:%02x'%s' [ES:DI] val:%02x'%s'".formatted(dsv, edv, dsv, (char) edv));

            regs.addSI((short) ((regs.getDF()) ? -1 : 1));
            regs.addDI((short) ((regs.getDF()) ? -1 : 1));

            int ians = dsv - edv;
            byte ans = (byte) ians;

            regs.setSF((ans & 0x80) != 0);
            regs.setOF(regs.getSF()
                    ? ((edv > 0 && ans > dsv) || (edv < 0 && ans < dsv))
                    : ans > dsv);
            regs.setCF((short) ians != (byte) ans);
            regs.setZF(ans == 0);
            regs.setPF((ans & 0x01) != 0);
            regs.setAF(false); // TBD
        }

        if (repSW) {
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.getZF()) || (repType != 0 && regs.getZF()))) {
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xa7
    private void CMPSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            short dsv = mem.PeekW(regs.getDS_SI());
            short edv = mem.PeekW(regs.getES_DI());

            logger.log(Level.TRACE, "CMPSW [DS:SI] val:%04x [ES:DI] val:%04x".formatted(dsv, edv));

            regs.addSI((short) ((regs.getDF()) ? -2 : 2));
            regs.addDI((short) ((regs.getDF()) ? -2 : 2));

            int ians = dsv - edv;
            short ans = (short) ians;

            regs.setSF((ans & 0x8000) != 0);
            regs.setOF(regs.getSF()
                    ? ((edv > 0 && ans > dsv) || (edv < 0 && ans < dsv))
                    : ans > dsv);
            regs.setCF((int) ians != (int) ans);
            regs.setZF(ans == 0);
            regs.SetSZPFw(ans);
            regs.setAF(false); // TBD
        }

        if (repSW) {
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.getZF()) || (repType != 0 && regs.getZF()))) {
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xaa
    private void STOSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "STOSB [ES:DI]:%05x AL:%02x'%s'".formatted(regs.getES_DI(), regs.getAL(), (char) (byte) regs.getAL()));
            mem.PokeB(regs.getES_DI(), regs.getAL());
            regs.addDI((short) ((regs.getDF()) ? -1 : 1));
        }

        if (repSW) {
            // Simple repeat for STOS
            // REPNE is not possible (I think so, so I have not dealt with it yet)

            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0)// || ((repType == 0 && !regs.ZF) || (repType != 0 && regs.ZF)))
                {
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xab
    private void STOSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "STOSW [ES:DI]:%05x <- AX:%04x", regs.getES_DI(), regs.getAX());
            mem.PokeW(regs.getES_DI(), regs.getAX());
            regs.addDI((short) ((regs.getDF()) ? -2 : 2));
        }

        if (repSW) {
            // Simple repeat for STOS
            // REPNE is not possible (I think so, so I have not dealt with it yet)

            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0) { // || ((repType == 0 && !regs.ZF) || (repType != 0 && regs.ZF)))
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xac
    private void LODSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            regs.setAL(mem.PeekB(regs.getDS_SI()));
            logger.log(Level.TRACE, "LODSB [DS:SI]:%05x AL:%02x'%s'".formatted(regs.getDS_SI(), regs.getAL(), (char) (byte) regs.getAL()));
            regs.addSI((short) ((regs.getDF()) ? -1 : 1));
        }

        if (repSW) {
            // Simple repeat for LODS
            // REPNE is not possible (I think so, so I have not dealt with it yet)
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.getZF()) || (repType != 0 && regs.getZF()))) {
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xad
    private void LODSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "LODSW [DS:SI]:%05x -> AX:%04x".formatted(regs.getDS_SI(), regs.getAX()));
            regs.setAX(mem.PeekW(regs.getDS_SI()));
            regs.addSI((short) ((regs.getDF()) ? -2 : 2));
        }

        if (repSW) {
            // Simple repeat for LODS
            // REPNE is not possible (I think so, so I have not dealt with it yet)

            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0)// || ((repType == 0 && !regs.ZF) || (repType != 0 && regs.ZF)))
                {
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xaf
    private void SCASW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            short dsv = regs.getAX();
            short edv = mem.PeekW(regs.getES_DI());

            logger.log(Level.TRACE, "SCASW AX:%04x [ES:DI] val:%04x".formatted(dsv, edv));

            regs.addSI((short) ((regs.getDF()) ? -2 : 2));
            regs.addDI((short) ((regs.getDF()) ? -2 : 2));

            int ians = dsv - edv;
            short ans = (short) ians;

            regs.setSF((ans & 0x8000) != 0);
            regs.setOF(regs.getSF()
                    ? ((edv > 0 && ans > dsv) || (edv < 0 && ans < dsv))
                    : ans > dsv);
            regs.setCF((int) ians != (int) ans);
            regs.setZF(ans == 0);
            regs.SetSZPFw(ans);
            regs.setAF(false); // TBD
        }

        if (repSW) {
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.getZF()) || (repType != 0 && regs.getZF()))) {
                    repSW = false;
                } else
                    regs.IP--;
            }
        }
    }

    // 0xb0
    private void MOV_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV AL,$%02x".formatted(imm8));
        regs.setAL(imm8);
    }

    // 0xb1
    private void MOV_CL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV CL,$%02x".formatted(imm8));
        regs.setCL(imm8);
    }

    // 0xb2
    private void MOV_DL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV DL,$%02x".formatted(imm8));
        regs.setDL(imm8);
    }

    // 0xb3
    private void MOV_BL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV BL,$%02x".formatted(imm8));
        regs.setBL(imm8);
    }

    // 0xb4
    private void MOV_AH_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV AH,$%02x".formatted(imm8));
        regs.setAH(imm8);
    }

    // 0xb5
    private void MOV_CH_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV CH,$%02x".formatted(imm8));
        regs.setCH(imm8);
    }

    // 0xb6
    private void MOV_DH_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV DH,$%02x".formatted(imm8));
        regs.setDH(imm8);
    }

    // 0xb7
    private void MOV_BH_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "MOV BH,$%02x".formatted(imm8));
        regs.setBH(imm8);
    }

    // 0xb8
    private void MOV_AX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV AX,$%04x".formatted(imm16));
        regs.setAX((short) imm16);
    }

    // 0xb9
    private void MOV_CX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV CX,$%04x".formatted(imm16));
        regs.setCX((short) imm16);
    }

    // 0xba
    private void MOV_DX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV DX,$%04x".formatted(imm16));
        regs.setDX((short) imm16);
    }

    // 0xbb
    private void MOV_BX_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV BX,$%04x".formatted(imm16));
        regs.setBX((short) imm16);
    }

    // 0xbc
    private void MOV_SP_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV SP,$%04x".formatted(imm16));
        regs.setSP((short) imm16);
    }

    // 0xbd
    private void MOV_BP_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV BP,$%04x".formatted(imm16));
        regs.setBP((short) imm16);
    }

    // 0xbe
    private void MOV_SI_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV SI,$%04x".formatted(imm16));
        regs.setSI((short) imm16);
    }

    // 0xbf
    private void MOV_DI_IW() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "MOV DI,$%04x".formatted(imm16));
        regs.setDI((short) imm16);
    }

    // 0xc0 See below
    // 0xc1 See below

    // 0xc3
    private void RET() {
        logger.log(Level.TRACE, "RET");
        regs.IP = mem.PeekW(regs.getSS_SP());
        regs.addSP(2);
    }

    // 0xc4
    private void LES_GW_EP() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "LES GW,EP modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // GW eRegs@reg

        int ptr;
        short v;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                v = mem.PeekW(ptr);
                regs.eRegs[reg] = v;
                regs.setES(mem.PeekW(ptr + 2));
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                v = mem.PeekW(ptr);
                regs.eRegs[reg] = v;
                regs.setES(mem.PeekW(ptr + 2));
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                v = mem.PeekW(ptr);
                regs.eRegs[reg] = v;
                regs.setES(mem.PeekW(ptr + 2));
                break;
            case 3:
                short r = regs.eRegs[rm];
                regs.sRegs[reg] = r;
                break;
        }
    }

    // 0xc5
    private void LDS_GW_EP() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "LDS GW,EP modrw:$%02x".formatted(modrw));

        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        // GW eRegs@reg

        int ptr;
        short v;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                v = mem.PeekW(ptr);
                regs.eRegs[reg] = v;
                regs.setDS(mem.PeekW(ptr + 2));
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                v = mem.PeekW(ptr);
                regs.eRegs[reg] = v;
                regs.setDS(mem.PeekW(ptr + 2));
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                v = mem.PeekW(ptr);
                regs.eRegs[reg] = v;
                regs.setDS(mem.PeekW(ptr + 2));
                break;
            case 3:
                short r = regs.eRegs[rm];
                regs.sRegs[reg] = r;
                break;
        }
    }

    // 0xc6
    private void MOV_EB_IB() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        byte imm8;
        int ptr;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                imm8 = Fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                mem.PokeB(ptr, imm8);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                imm8 = Fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                mem.PokeB(ptr, imm8);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                imm8 = Fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                mem.PokeB(ptr, imm8);
                break;
            case 3:
                imm8 = Fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                // regs.eRegs[reg] = (short)((regs.eRegs[reg] & 0xff00) | imm8);
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | imm8);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (imm8 << 8));
                break;
        }
    }

    // 0xc7
    private void MOV_EW_IW() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3);
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        int ptr;
        short imm16;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                imm16 = Fetchw();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                mem.PokeW(ptr, (short) imm16);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                imm16 = Fetchw();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                mem.PokeW(ptr, (short) imm16);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                imm16 = Fetchw();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                mem.PokeW(ptr, (short) imm16);
                break;
            case 3:
                imm16 = Fetchw();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                regs.eRegs[reg] = (short) imm16;
                break;
        }
    }

    // 0xcd
    private void INT_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "INT $%02x".formatted(imm8));
        dos.int_(imm8);
    }

    // 0xcf
    private void IRET() {
        logger.log(Level.TRACE, "IRET");
        regs.IP = mem.PeekW(regs.getSS_SP());
        regs.addSP(2);
        regs.setCS(mem.PeekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.FLAG = mem.PeekW(regs.getSS_SP());
        regs.addSP(2);
    }

    // 0xd0
    private void GRP2_EB_1() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        byte ans = 0;
        byte uans = 0;

        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        switch (reg) {
            case 0: // ROL
                logger.log(Level.TRACE, "ROL EB,1 modrw:$%02x".formatted(modrw));
                uans = (byte) EB;
                uans = (byte) ((uans << 1) | ((uans & 0x80) == 0 ? 0 : 1));
                ans = (byte) uans;
                regs.setCF((uans & 0x01) != 0);
                // regs.OF = (ans & 0x8000) != 0;
                break;
            case 1: // ROR
                logger.log(Level.TRACE, "ROR EB,1 modrw:$%02x".formatted(modrw));
                uans = (byte) EB;
                uans = (byte) ((uans >> 1) | ((uans & 0x01) == 0 ? 0 : 0x80));
                ans = (byte) uans;
                regs.setCF((uans & 0x80) != 0);
                // regs.OF = (ans & 0x8000) != 0;
                break;
            case 2: // RCL
                throw new UnsupportedOperationException();
            case 3: // RCR
                logger.log(Level.TRACE, "RCR EB,1 modrw:$%02x".formatted(modrw));
                uans = (byte) EB;
                boolean newCF = (uans & 1) != 0;
                uans = (byte) ((uans >> 1) | (regs.getCF() ? 0x80 : 0x00));
                ans = (byte) uans;
                regs.setCF(newCF);
                break;
            case 4: // SHL
            case 6: // same SHL
                logger.log(Level.TRACE, "SHL EB,1 modrw:$%02x".formatted(modrw));
                uans = (byte) EB;
                regs.setCF((uans & 0x80) != 0);
                uans <<= 1;
                ans = (byte) uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.SetSZPFb(uans);
                regs.setAF(true); // TBD
                break;
            case 5: // SHR
                logger.log(Level.TRACE, "SHR EB,1 modrw:$%02x".formatted(modrw));
                uans = (byte) EB;
                regs.setCF((uans & 0x01) != 0);
                uans >>= 1;
                ans = (byte) uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.SetSZPFb(uans);
                regs.setAF(true); // TBD
                break;
            case 7: // SAR
                logger.log(Level.TRACE, "SAR EB,1 modrw:$%02x".formatted(modrw));
                ans = EB;
                regs.setCF((ans & 0x01) != 0);
                ans >>= 1;
                uans = (byte) ans;
                regs.setOF(false);
                regs.SetSZPFb(uans);
                regs.setAF(true); // TBD
                break;
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeB(ptr, uans);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | uans);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (uans << 8));
                break;
        }
    }

    // 0xd1
    private void GRP2_EW_1() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        short ans = 0;
        short uans = 0;

        int ptr = 0;
        short EW = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 3:
                EW = (short) regs.eRegs[rm];
                break;
        }

        boolean newCF;
        switch (reg) {
            case 0: // ROL
                logger.log(Level.TRACE, "ROL EW,1 modrw:$%02x".formatted(modrw));

                uans = (short) EW;
                regs.setCF((uans & 0x8000) != 0);
                uans = (short) ((uans << 1) | (regs.getCF() ? 1 : 0));
                ans = (short) uans;

                regs.setOF((ans & 0x8000) != 0);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
            case 1: // ROR
                logger.log(Level.TRACE, "ROR EW,1 modrw:$%02x".formatted(modrw));

                uans = (short) EW;
                regs.setCF((uans & 0x0001) != 0);
                uans = (short) ((uans >> 1) | (regs.getCF() ? 1 : 0));
                ans = (short) uans;
                regs.setOF((ans & 0x0001) != 0);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
            case 2: // RCL
                logger.log(Level.TRACE, "RCL EW,1 modrw:$%02x".formatted(modrw));

                uans = (short) EW;
                newCF = (uans & 0x8000) != 0;
                uans = (short) ((EW << 1) | (regs.getCF() ? 1 : 0));
                regs.setCF(newCF);
                ans = (short) uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
            case 3: // RCR
                logger.log(Level.TRACE, "RCR EW,1 modrw:$%02x".formatted(modrw));

                uans = (short) EW;
                newCF = (uans & 0x0001) != 0;
                uans = (short) ((EW >> 1) | (regs.getCF() ? 0x8000 : 0x000));
                regs.setCF(newCF);
                ans = (short) uans;
                regs.setOF((ans & 0x0001) != 0);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
            case 4: // SHL
            case 6: // same SHL
                logger.log(Level.TRACE, "SHL EW,1 modrw:$%02x".formatted(modrw));
                uans = (short) EW;
                regs.setCF((uans & 0x8000) != 0);
                uans <<= 1;
                ans = (short) uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
            case 5: // SHR
                logger.log(Level.TRACE, "SHR EW,1 modrw:$%02x".formatted(modrw));
                uans = (short) EW;
                regs.setCF((uans & 0x0001) != 0);
                uans >>= 1;
                ans = (short) uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
            case 7: // SAR
                logger.log(Level.TRACE, "SAR EW,1 modrw:$%02x".formatted(modrw));
                ans = (short) EW;
                regs.setCF((ans & 0x01) != 0);
                ans >>= 1;
                uans = (short) ans;
                regs.setOF(false);
                regs.SetSZPFw((short) ans);
                regs.setAF(true); // TBD
                break;
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeW(ptr, (short) uans);
                break;
            case 3:
                regs.eRegs[rm] = (short) (uans); // (regs.eRegs[rm] & 0xff00) | uans);
                break;
        }

    }

    // 0xc0 or 0xd2
    private void GRP2_EB_CL(byte op) {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "GRP2_EB_CL op:$%02x modrw:$%02x".formatted(op, modrw));
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte ans = 0;
        byte uans = 0;
        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        int count = (op == 0xd2 ? regs.getCL() : Fetch()) & 0x1f;
        if (count == 0) return;

        boolean newCF;
        if (count == 1) {
            switch (reg) {
                case 0: // ROL
                    uans = (byte) EB;
                    newCF = (uans & 0x80) != 0;
                    uans = (byte) ((uans << 1) | ((uans & 0x80) != 0 ? 1 : 0));
                    ans = (byte) uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 1: // ROR
                    uans = (byte) EB;
                    uans = (byte) ((uans >> 1) | ((uans & 0x01) == 0 ? 0 : 0x80));
                    ans = (byte) uans;
                    regs.setCF((uans & 0x80) != 0);
                    regs.setOF((uans & 0x80) != 0);
                    break;
                case 2: // RCL
                    uans = (byte) EB;
                    newCF = (uans & 0x80) != 0;
                    uans = (byte) ((uans << 1) | (regs.getCF() ? 1 : 0));
                    ans = (byte) uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL
                    uans = (byte) EB;
                    regs.setCF((uans & 0x80) != 0);
                    uans <<= 1;
                    regs.SetSZPFb(uans);
                    break;
                case 5: // SHR
                    uans = (byte) EB;
                    regs.setCF((uans & 1) != 0);
                    uans >>= 1;
                    regs.SetSZPFb(uans);
                    break;
                case 6: // (SMO)
                    throw new UnsupportedOperationException();
                case 7: // SAR
                    int ians = (byte) EB;
                    regs.setCF((ians & 1) != 0);
                    ians >>= 1;
                    uans = (byte) ians;
                    regs.SetSZPFb(uans);
                    regs.setOF(false);
                    break;
            }
        } else {
            switch (reg) {
                case 0: // ROL
                    uans = (byte) EB;
                    newCF = (uans & (0x100 >> count)) != 0;
                    uans = (byte) ((uans << count) | (uans >> (8 - count)));
                    ans = (byte) uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 1: // ROR
                    throw new UnsupportedOperationException();
                case 2: // RCL
                    uans = (byte) EB;
                    byte v = (byte) (uans & (0xff << count));
                    newCF = ((byte) (uans & (0x100 >> count)) != 0);
                    uans = (byte) ((uans << count) | ((regs.getCF() ? 1 : 0) << (count - 1)) | (v >> (9 - count)));
                    ans = (byte) uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL論理シフト
                    uans = (byte) EB;
                    regs.setCF((uans & (0x80 >> (count - 1))) != 0);
                    uans <<= count;
                    regs.SetSZPFb(uans);
                    break;
                case 5: // SHR論理シフト
                    uans = (byte) EB;
                    regs.setCF((uans & (1 << (count - 1))) != 0);
                    uans >>= count;
                    regs.SetSZPFb(uans);
                    break;
                case 6: // (SMO)
                    throw new UnsupportedOperationException();
                case 7: // SAR 算出シフト
                    int ians = (byte) EB;
                    regs.setCF((ians & (1 << (count - 1))) != 0);
                    ians >>= count;
                    uans = (byte) ians;
                    regs.SetSZPFb(uans);
                    break;
            }
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeB(ptr, uans);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | uans);
                else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (uans << 8));
                break;
        }
    }

    // 0xc1 or 0xd3
    private void GRP2_EW_CL(byte op) {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "GRP2_EW_CL op:$%02x modrw:$%02x".formatted(op, modrw));
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);
        short ans = 0;
        short uans = 0;

        int ptr = 0;
        short EW = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 3:
                EW = (short) regs.eRegs[rm];
                break;
        }

        int count = (op == 0xd3 ? regs.getCL() : Fetch()) & 0x1f;
        if (count == 0) return;

        if (count == 1) {
            switch (reg) {
                case 0: // ROL
                    throw new UnsupportedOperationException();
                case 1: // ROR
                    uans = EW;
                    uans = (short) ((uans >> 1) | ((uans & 0x01) == 0 ? 0 : 0x8000));
                    ans = (short) uans;
                    regs.setCF((uans & 0x8000) != 0);
                    regs.setOF((uans & 0x8000) != 0);
                    break;
                case 2: // RCL
                    throw new UnsupportedOperationException();
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL
                case 6: // Same SHL
                    uans = EW;
                    regs.setCF((ans & 0x8000) != 0);
                    uans <<= 1;
                    ans = (short) uans;
                    uans = (short) ans;
                    regs.setOF((ans & 0x8000) != 0);
                    regs.SetSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 5: // SHR
                    uans = EW;
                    regs.setCF((ans & 0x01) != 0);
                    uans >>= 1;
                    ans = (short) uans;
                    uans = (short) ans;
                    regs.setOF((ans & 0x8000) != 0);
                    regs.SetSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 7: // SAR
                    ans = (short) EW;
                    regs.setCF((ans & 0x01) != 0);
                    ans >>= 1;
                    uans = (short) ans;
                    regs.setOF(false);
                    regs.SetSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
            }
        } else {
            switch (reg) {
                case 0: // ROL
                    throw new UnsupportedOperationException();
                case 1: // ROR
                    uans = EW;
                    uans = (short) ((uans >> count) | (uans << (16 - count)));
                    ans = (short) uans;
                    regs.setCF((uans & 0x8000) != 0);
                    regs.setOF((uans & 0x8000) != 0);
                    break;
                case 2: // RCL
                    throw new UnsupportedOperationException();
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL
                case 6: // Same SHL
                    uans = (short) EW;
                    uans <<= count - 1;
                    regs.setCF((uans & 0x8000) != 0);
                    uans <<= 1;
                    ans = (short) uans;
                    regs.SetSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 5: // SHR (logical shift right)
                    uans = (short) EW;
                    uans >>= count - 1;
                    regs.setCF((uans & 0x01) != 0);
                    uans >>= 1;
                    ans = (short) uans;
                    regs.SetSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 7: // SAR
                    ans = (short) EW;
                    ans >>= count - 1;
                    regs.setCF((ans & 0x01) != 0);
                    ans >>= 1;
                    uans = (short) ans;
                    regs.SetSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
            }
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.PokeW(ptr, (short) uans);
                break;
            case 3:
                regs.eRegs[rm] = (short) uans;
                break;
        }
    }

    // 0xd4
    private void AAM() {
        byte imm8 = Fetch();
        byte a = regs.getAL();
        regs.setAH((byte) (a / imm8));
        regs.setAL((byte) (a % imm8));
        regs.SetSZPFw((short) regs.getAX());
        regs.setAF(true); // TBD
    }

    // 0xd5
    private void AAD() {
        byte imm8 = Fetch();
        if (imm8 != 0x0a) {
            throw new UnsupportedOperationException();
        }

        regs.setAL((byte) (regs.getAH() * 0x0a + regs.getAL()));
        regs.setAH((short) 0);
        regs.SetSZPFb(regs.getAL());
        regs.setAF(true); // TBD
    }

    // 0xe8
    private void CALL_near() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "CALL near $%04x".formatted(imm16));
        regs.subSP(2);
        mem.PokeW(regs.getSS_SP(), regs.IP);
        regs.IP = (short) (regs.IP + imm16);
    }

    // 0xe2
    private void LOOP_short() {
        byte imm8 = (byte) Fetch();
        if ((short) regs.getCX() < 100) logger.log(Level.TRACE, "LOOP short $%02x CX:$%04x".formatted(imm8, regs.getCX()));

        regs.decCX();
        if (regs.getCX() != 0) {
            regs.IP = (short) (regs.IP + imm8);
        }
    }

    // 0xe4
    private void IN_AL_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "IN AL,$%02x".formatted(imm8));
        regs.setAL(machine.INPb((short) imm8));
    }

    // 0xe5
    private void IN_AX_IB() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "IN AX,$%02x".formatted(imm8));
        regs.setAX(machine.INPw((short) imm8));
    }

    // 0xe6
    private void OUT_IB_AL() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "OUT $%02x,AL".formatted(imm8));
        machine.OUTPb((short) imm8, regs.getAL());
    }

    // 0xe7
    private void OUT_IB_AX() {
        byte imm8 = Fetch();
        logger.log(Level.TRACE, "OUT $%02x,AX".formatted(imm8));
        machine.OUTPw((short) imm8, regs.getAX());
    }

    // 0xe9
    private void JMP_near() {
        short imm16 = Fetchw();
        logger.log(Level.TRACE, "JMP near $%04x".formatted(imm16));

        regs.IP = (short) (regs.IP + imm16);
    }

    // 0xeb
    private void JMP_short() {
        byte imm8 = (byte) Fetch();
        logger.log(Level.TRACE, "JMP short $%02x".formatted(imm8));

        regs.IP = (short) (regs.IP + imm8);
    }

    // 0xec
    private void IN_AL_DX() {
        logger.log(Level.TRACE, "IN AL,DX");
        regs.setAL(machine.INPb((short) regs.getDX()));
    }

    // 0xed
    private void IN_AX_DX() {
        logger.log(Level.TRACE, "IN AX,DX");
        regs.setAX(machine.INPw((short) regs.getDX()));
    }

    // 0xee
    private void OUT_DX_AL() {
        logger.log(Level.TRACE, "OUT DX,AL");
        machine.OUTPb((short) regs.getDX(), regs.getAL());
    }

    // 0xef
    private void OUT_DX_AX() {
        logger.log(Level.TRACE, "OUT DX,AX");
        machine.OUTPw((short) regs.getDX(), regs.getAX());
    }

    // 0xf2
    private void REPNE() {
        logger.log(Level.TRACE, "REPNE");
        repSW = true;
        repType = 1;
    }

    // 0xf3
    private void REPE() {
        logger.log(Level.TRACE, "REPE");
        repSW = true;
        repType = 0;
    }

    // 0xf4
    private void HLT() {
        logger.log(Level.TRACE, "HLT");
        hltSW = true;
    }

    // 0xf6
    private void GRP3B() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "GRP3B modrw:$%02x".formatted(modrw));
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte EB = 0;
        int ptr = 0;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        byte IB;
        byte ans = 0;
        switch (reg) {
            case 0: // TEST EB,IB
                IB = (byte) Fetch();
                ans = (byte) (EB & IB);
                regs.SetSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 1: // ?
                throw new UnsupportedOperationException();
            case 2: // NOT EB
                ans = (byte) (~EB);
                regs.SetSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 3: // NEG EB
                ans = (byte) (-EB);
                regs.SetSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 4: // MUL EB
                int mans = regs.getAL() * (byte) EB;
                regs.SetSZPFb((byte) EB);
                regs.setAX((short) (short) mans);
                regs.setZF(regs.getAX() == 0);
                regs.setOF(regs.getAH() != 0);
                regs.setCF(regs.getOF());
                break;
            case 5: // IMUL EB
                throw new UnsupportedOperationException();
            case 6: // DIV EB
                ans = (byte) ((short) regs.getAX() / (byte) EB);
                mod = (byte) ((short) regs.getAX() % (byte) EB);
                regs.setAL(ans);
                regs.setAH(mod);
                break;
            case 7: // IDIV EB
                throw new UnsupportedOperationException();
        }

        if (reg != 0 && reg < 4) {
            switch (mod) {
                case 0:
                case 1:
                case 2:
                    mem.PokeB(ptr, (byte) ans);
                    break;
                case 3:
                    if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (byte) ans);
                    else regs.eRegs[rm - 4] = (short) ((ans << 8) | (byte) regs.eRegs[rm - 4]);
                    break;
            }
        }
    }

    // 0xf7
    private void GRP3W() {
        byte modrw = Fetch();
        logger.log(Level.TRACE, "GRP3W modrw:$%02x".formatted(modrw));
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short EW = 0;
        int ptr = 0;
        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 3:
                EW = regs.eRegs[rm];
                break;
        }

        short IW;
        short ans = 0;
        switch (reg) {
            case 0: // TEST EW,IW
                IW = (short) Fetchw();
                ans = (short) (EW & IW);
                regs.SetSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 1: // ?
                throw new UnsupportedOperationException();
            case 2: // NOT EW
                ans = (short) (~EW);
                regs.SetSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 3: // NEG EW
                ans = (short) (-EW);
                regs.SetSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 4: // MUL EW
                int ans32 = (int) ((short) regs.getAX() * (short) EW);
                regs.setAX((short) ans32);
                regs.setDX((short) (ans32 >> 16));
                break;
            case 5: // IMUL EW
                int ians32 = (int) ((short) regs.getAX() * (short) EW);
                regs.setAX((short) ians32);
                regs.setDX((short) (ians32 >> 16));
                break;
            case 6: // DIV EW
                int ans32d = (int) ((int) (((short) regs.getDX() << 16) + (short) regs.getAX()) / (int) EW);
                int modud = (int) ((int) (((short) regs.getDX() << 16) + (short) regs.getAX()) % (int) EW);
                regs.setAX((short) ans32d);
                regs.setDX((short) modud);
                break;
            case 7: // IDIV EB
                int ians32d = (int) (((short) regs.getDX() << 16) + (short) regs.getAX()) / (int) EW;
                int modu = (int) (((short) regs.getDX() << 16) + (short) regs.getAX()) % (int) EW;
                regs.setAX((short) ians32d);
                regs.setDX((short) modu);
                break;
        }

        if (reg != 0 && reg < 4) {
            switch (mod) {
                // case 0:
                case 1:
                case 2:
                    mem.PokeW(ptr, (short) ans);
                    break;
                case 3:
                    regs.eRegs[rm] = (short) ans;
                    break;
            }
        }
    }

    // 0xf8
    private void CLC() {
        logger.log(Level.TRACE, "CLC");
        regs.setCF(false);
    }

    // 0xf9
    private void STC() {
        logger.log(Level.TRACE, "STC");
        regs.setCF(true);
    }

    // 0xfa
    private void CLI() {
        logger.log(Level.TRACE, "CLI");
        regs.setIF(false);
    }

    // 0xfb
    private void STI() {
        logger.log(Level.TRACE, "STI");
        regs.setIF(true);
    }

    // 0xfc
    private void CLD() {
        logger.log(Level.TRACE, "CLD");
        regs.setDF(false);
    }

    // 0xfd
    private void STD() {
        logger.log(Level.TRACE, "STD");
        regs.setDF(true);
    }

    // 0xfe
    private void GRP4() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EB = (byte) mem.PeekB(ptr);
                break;
            case 3:
                if (rm < 4) EB = (byte) (byte) regs.eRegs[rm];
                else EB = (byte) (byte) (regs.eRegs[rm - 4] >> 8);
                break;
        }

        byte IB = 1;
        short ians;
        byte ans;

        switch (reg) {
            case 0: // INC EB
                logger.log(Level.TRACE, "INC EB");
                ians = (short) (EB + IB);
                ans = (byte) ians;
                regs.SetSZPFb(ans);
                regs.SetOFbAdd((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 1: // DEC EB
                logger.log(Level.TRACE, "DEC EB");
                ians = (short) (EB - IB);
                ans = (byte) ians;
                regs.SetSZPFb(ans);
                regs.SetOFbSub((byte) EB, (byte) IB, (byte) ans);
                regs.SetCFb((short) ians);
                regs.SetAF((byte) EB, (byte) IB, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | ans);
                        else regs.eRegs[rm - 4] = (short) ((byte) regs.eRegs[rm - 4] | (ans << 8));
                        break;
                }
                break;
            case 2: // ?
                throw new UnsupportedOperationException();
            case 3: // ?
                throw new UnsupportedOperationException();
            case 4: // ?
                throw new UnsupportedOperationException();
            case 5: // ?
                throw new UnsupportedOperationException();
            case 6: // ?
                throw new UnsupportedOperationException();
            case 7: // ?
                throw new UnsupportedOperationException();
        }
    }

    // 0xff
    private void GRP5() {
        byte modrw = Fetch();
        byte reg = (byte) ((modrw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modrw & 7);
        byte mod = (byte) (modrw >> 6);

        short EW = 0;
        int ptr = 0;

        boolean bSegPrefSw = segPrefSw;
        int bSegPref = segPref;

        switch (mod) {
            case 0:
                ptr = GetMod00RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 1:
                ptr = GetMod01RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 2:
                ptr = GetMod02RWADR(rm, false);
                EW = (short) mem.PeekW(ptr);
                break;
            case 3:
                EW = regs.eRegs[rm];
                break;
        }

        short IW = 1;
        short ians;
        short ans;

        switch (reg) {
            case 0: // INC EW
                logger.log(Level.TRACE, "INC EW");
                ians = (short) (EW + IW);
                ans = (short) ians;
                regs.SetSZPFw(ans);
                regs.SetOFwAdd((short) EW, (short) IW, (short) ans);
                regs.SetCFw((int) (EW + IW));
                regs.SetAF((byte) EW, (byte) IW, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ians);
                        break;
                    case 3:
                        regs.eRegs[rm] = ians;
                        break;
                }
                break;
            case 1: // DEC EW
                logger.log(Level.TRACE, "DEC EW");
                ians = (short) (EW - IW);
                ans = (short) ians;
                regs.SetSZPFw(ans);
                regs.SetOFwSub((short) EW, (short) IW, (short) ans);
                regs.SetCFw((int) (EW - IW));
                regs.SetAF((byte) EW, (byte) IW, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.PokeW(ptr, ians);
                        break;
                    case 3:
                        regs.eRegs[rm] = ians;
                        break;
                }
                break;
            case 2: // CALL EW
                logger.log(Level.TRACE, "CALL EW");
                regs.subSP(2);
                mem.PokeW(regs.getSS_SP(), regs.IP);
                regs.IP = EW;
                break;
            case 3: // CALL EP
                logger.log(Level.TRACE, "CALL EP");

                segPrefSw = bSegPrefSw;
                segPref = bSegPref;
                int ptr2;
                short seg = 0;
                ptr2 = ptr + 2;
                seg = (short) mem.PeekW(ptr2);

                regs.subSP(2);
                mem.PokeW(regs.getSS_SP(), regs.getCS());
                regs.subSP(2);
                mem.PokeW(regs.getSS_SP(), regs.IP);
                regs.IP = EW;
                regs.setCS(seg);
                break;
            case 4: // JMP EW
                logger.log(Level.TRACE, "JMP EW");
                regs.IP = EW;
                break;
            case 5: //
                throw new UnsupportedOperationException();
            case 6: // PUSH EW
                logger.log(Level.TRACE, "PUSH EW");
                regs.subSP(2);
                mem.PokeW(regs.getSS_SP(), EW);
                break;
            case 7: // ?
                throw new UnsupportedOperationException();
        }
    }
}
