package mdplayer.driver.fmp.nise98;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.System.getLogger;


// x86 general reference sites and sources
// https://qiita.com/hdk_2/items/6f8cb8a7c67342e2a32a
// mame tabel286.h

// Sites referenced for OF determination processing
// https://hiroyukichishiro.com/arithmetic-overflow-in-c-language/#%E8%B6%B3%E3%81%97%E7%AE%97%E3%81%AE%E4%BA%8B%E5%BE%8C%E6%9D%A1%E4%BB%B6
// mame
public class Nise286 {

    private static final Logger logger = getLogger(Nise286.class.getName());

    private final Register286 regs;
    private final Memory98 mem;
    private final NiseDos dos;
    private final Nise98 machine;
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
    private final List<Supplier<Boolean>> lstHook = new ArrayList<>();

    public Nise286(Nise98 machine) {
        this.regs = machine.getRegisters();
        this.mem = machine.getMem();
        this.dos = machine.getDos();
        this.machine = machine;
    }

    public void addUserInt(UserInt ui) {
        synchronized (userIntLockObject) {
            lstUserInt.add(ui);
        }
    }

    public void setHook(Supplier<Boolean> hook) {
        lstHook.add(hook);
    }

    public int stepExecute() {
        if (hltSW) {
            logger.log(Level.ERROR, "CPU is HALT.");
            return -1;
        }

        interrupt();

        for (Supplier<Boolean> func : lstHook) {
            boolean did = func.get();
            if (did) return 0;
        }

        byte op = fetch();
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x12:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x13:
                ADC_GW_EW();
                break;
            case 0x14:
                ADC_AL_IB();
                break;
            case 0x15:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x16:
                pushSS();
                break;
            case 0x17:
                popSS();
                break;
            case 0x18:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x19:
                SBB_EW_GW();
                break;
            case 0x1a:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x1b:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x1c:
                SBB_AL_IB();
                break;
            case 0x1d:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));

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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x63:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0x64:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0x65:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0x66:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0x67:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0x68:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x69:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x6a:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x6b:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x6c:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x6d:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x6e:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0x6f:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186

            case 0x70:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x71:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x7b:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x7c:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x96:
                XCHG_SI_AX();
                break;
            case 0x97:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x98:
                CBW();
                break;
            case 0x99:
                CWD();
                break;
            case 0x9a:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x9b:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x9c:
                PUSHF();
                break;
            case 0x9d:
                POPF();
                break; // x286
            case 0x9e:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0x9f:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));

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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0xc9:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x186
            case 0xca:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xcb:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xcc:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0xcd:
                INT_IB();
                break;
            case 0xce:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xd7:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0xd8:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xd9:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xda:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xdb:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xdc:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xdd:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xde:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
            case 0xdf:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286

            case 0xe0:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0xe1:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0xe2:
                LOOP_short();
                break;
            case 0xe3:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
            case 0xf1:
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff)); // x286
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
                throw new UnsupportedOperationException(Integer.toHexString(op & 0xff));
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
                throw new UnsupportedOperationException("Unkown op code %02x.".formatted(op & 0xff));
        }

        return 0;
    }

    private void interrupt() {
        if (segPrefSw) return;
        if (repSW) return;
        if (!regs.isIF()) return;// IF is false and not allowed

        // check mask
        for (int i = 0; i < 8; i++) {
            if ((w_mmsk & (0x01 << i)) == 0) intXX(i + 8);
        }
        for (int i = 0; i < 8; i++) {
            if ((w_smsk & (0x01 << i)) == 0) intXX(i + 10);
        }

        userInt();
    }

    private void userInt() {
        if (lstUserInt.isEmpty()) return;

        UserInt ui;
        synchronized (userIntLockObject) {
            ui = lstUserInt.get(0);
            lstUserInt.remove(0);
        }

        short ofs = mem.peekW(ui.getIntNum() * 4);
        short seg = mem.peekW(ui.getIntNum() * 4 + 2);
        if (ofs == 0 && seg == 0) return;

        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.flag);
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), (short) 0);
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), (short) 0);
        regs.ip = ofs;
        regs.setCS(seg);

        logger.log(Level.DEBUG, "Interrupt:UserINT%02xh".formatted(ui.getIntNum()));
    }

    private void intXX(int i) {
        if (!interruptTrigger[i]) return;

        interruptTrigger[i] = false;
        short ofs = mem.peekW(i * 4);
        short seg = mem.peekW(i * 4 + 2);
        if (ofs == 0 && seg == 0) return;

        iLevel++;

        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.flag);
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getCS());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.ip);
        regs.ip = ofs;
        regs.setCS(seg);

        logger.log(Level.DEBUG, "Interrupt:INT%02xh".formatted(i));
    }

    private byte fetch() {
        byte op = mem.peekB(regs.getCS_IP());
        regs.ip++;
        return op;
    }

    private short fetchW() {
        short imm16 = (short) (fetch() & 0xff);
        imm16 |= (short) ((fetch() & 0xff) << 8);
        return imm16;
    }

    private int getSegment(byte rm, boolean noSeg, boolean isMod00 /* = false */) {
        if (noSeg) return 0;

        if (segPrefSw) {
            segPrefSw = false;
            return ((regs.sRegs[segPref] & 0xffff) << 4) & 0xffff;
        } else if (rm != 2 && rm != 3 && rm != 6) // When the specified register is other than BP, DS is used as the default segment.
            return ((regs.getDS() & 0xffff) << 4) & 0xffff;
        else if (rm == 6 && isMod00) // When the specified register is other than BP, DS is used as the default segment.
            return ((regs.getDS() & 0xffff) << 4) & 0xffff;
        else // When the specified register is BP, SS is used as the default segment.
            return ((regs.getSS() & 0xffff) << 4) & 0xffff;
    }

    private int getMod00RwAdr(byte rm, boolean noSeg /* = false */) {
        int seg = getSegment(rm, noSeg, true);

        switch (rm) {
            case 0:
                return seg + ((regs.getBX() + regs.getSI()) & 0xffff);
            case 1:
                return seg + ((regs.getBX() + regs.getDI()) & 0xffff);
            case 2:
                return seg + ((regs.getBP() + regs.getSI()) & 0xffff);
            case 3:
                return seg + ((regs.getBP() + regs.getDI()) & 0xffff);
            case 4:
                return seg + (regs.getSI() & 0xffff);
            case 5:
                return seg + (regs.getDI() & 0xffff);
            case 6:
                short ptr = fetchW();
                return seg + (ptr & 0xffff);
            case 7:
                return seg + (regs.getBX() & 0xffff);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int getMod01RwAdr(byte rm, boolean noSeg /* = false */) {
        int seg = getSegment(rm, noSeg, false);

        int disp8 = fetch();
        switch (rm) {
            case 0:
                return seg + ((regs.getBX() + regs.getSI() + disp8) & 0xffff);
            case 1:
                return seg + ((regs.getBX() + regs.getDI() + disp8) & 0xffff);
            case 2:
                return seg + ((regs.getBP() + regs.getSI() + disp8) & 0xffff);
            case 3:
                return seg + ((regs.getBP() + regs.getDI() + disp8) & 0xffff);
            case 4:
                return seg + ((regs.getSI() + disp8) & 0xffff);
            case 5:
                return seg + ((regs.getDI() + disp8) & 0xffff);
            case 6:
                return seg + ((regs.getBP() + disp8) & 0xffff);
            case 7:
                return seg + ((regs.getBX() + disp8) & 0xffff);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int getMod02RwAdr(byte rm, boolean noSeg /* = false */) {
        int seg = getSegment(rm, noSeg, false);

        int disp16 = fetchW();
        switch (rm) {
            case 0:
                return seg + ((regs.getBX() + regs.getSI() + disp16) & 0xffff);
            case 1:
                return seg + ((regs.getBX() + regs.getDI() + disp16) & 0xffff);
            case 2:
                return seg + ((regs.getBP() + regs.getSI() + disp16) & 0xffff);
            case 3:
                return seg + ((regs.getBP() + regs.getDI() + disp16) & 0xffff);
            case 4:
                return seg + ((regs.getSI() + disp16) & 0xffff);
            case 5:
                return seg + ((regs.getDI() + disp16) & 0xffff);
            case 6:
                return seg + ((regs.getBP() + disp16) & 0xffff);
            case 7:
                return seg + ((regs.getBX() + disp16) & 0xffff);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private int getSegment() {
        int seg;
        if (segPrefSw) {
            seg = ((regs.sRegs[segPref] & 0xffff) << 4) & 0xffff;
            segPrefSw = false;
        } else
            seg = ((regs.getDS() & 0xffff) << 4) & 0xffff;
        return seg;
    }

    // 0x00
    private void ADD_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "ADD EB,GB modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        short c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) regs.eRegs[rm];
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ic & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbAdd(a, b, ic);
        regs.setCFb(c);
        regs.setAF(a, b, ic);
    }

    // 0x01
    private void ADD_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "ADD EW,GW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short gw = regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 3:
                a = regs.eRegs[rm];
                b = gw;
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                regs.eRegs[rm] = ic;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOFwAdd(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x02
    private void ADD_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "ADD GB,EB modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        short c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
                a = gb;
                b = mem.peekB(getMod00RwAdr(rm, false));
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 1:
                a = gb;
                b = mem.peekB(getMod01RwAdr(rm, false));
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 2:
                a = gb;
                b = mem.peekB(getMod02RwAdr(rm, false));
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 3:
                a = gb;
                if (rm < 4) b = (byte) (regs.eRegs[rm] & 0x00ff);
                else b = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                c = (short) ((a & 0xff) + (b & 0xff));
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) (((byte) regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbAdd(a, b, ic);
        regs.setCFb(c);
        regs.setAF(a, b, ic);
    }

    // 0x03
    private void ADD_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "ADD gw,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // short gw = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short gw = regs.eRegs[reg];

        switch (mod) {
            case 0:
                a = gw;
                b = mem.peekW(getMod00RwAdr(rm, false));
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 1:
                a = gw;
                b = mem.peekW(getMod01RwAdr(rm, false));
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 2:
                a = gw;
                b = mem.peekW(getMod02RwAdr(rm, false));
                c = (a & 0xffff) + (b & 0xffff);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 3:
                a = gw;
                b = regs.eRegs[rm];
                c = (a & 0xffff) + (b & 0xffff);
                regs.eRegs[reg] = (short) c;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOFwAdd(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x04
    private void ADD_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "ADD AL,$%02x".formatted(imm8 & 0xff));

        byte a, b;
        short c;
        byte ic;

        a = regs.getAL();
        b = imm8;
        c = (short) ((a & 0xff) + (b & 0xff));
        ic = (byte) c;
        regs.setAL((byte) c);

        regs.setSZPFb(ic);
        regs.setOFbAdd(a, b, ic);
        regs.setCFb(c);
        regs.setAF(a, b, ic);
    }

    // 0x05
    private void ADD_AX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "ADD AX,$%04x".formatted(imm16 & 0xffff));

        // short GW = regs.eRegs[reg];
        short a;
        short b;
        int c;
        short ic;
        a = regs.getAX();
        b = imm16;
        c = (a & 0xffff) + (b & 0xffff);
        ic = (short) c;
        regs.setAX(ic);

        regs.setSZPFw(ic);
        regs.setOFwAdd(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x06
    private void PUSH_ES() {
        logger.log(Level.TRACE, "PUSH ES");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getES());
    }

    // 0x07
    private void POP_ES() {
        logger.log(Level.TRACE, "POP ES");
        regs.setES(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x08
    private void OR_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "OR EB,gb modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a;
        byte b;
        byte c;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a | b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a | b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a | b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (regs.eRegs[rm] & 0x00ff);
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (byte) (a | b);
                ic = c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ic & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0a
    private void OR_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "OR gb,EB modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a;
        byte b;
        byte c;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
                a = gb;
                b = mem.peekB(getMod00RwAdr(rm, false));
                c = (byte) (a | b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 1:
                a = gb;
                b = mem.peekB(getMod01RwAdr(rm, false));
                c = (byte) (a | b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4]) | ((ic & 0xff) << 8));
                break;
            case 2:
                a = gb;
                b = mem.peekB(getMod02RwAdr(rm, false));
                c = (byte) (a | b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 3:
                a = gb;
                if (rm < 4) b = (byte) (regs.eRegs[rm] & 0x00ff);
                else b = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                c = (byte) (a | b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0b
    private void OR_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "OR GW,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        int a;
        int b;
        int c = 0;
        short ic;
        switch (mod) {
            case 0:
                a = regs.eRegs[reg] & 0xffff;
                b = mem.peekW(getMod00RwAdr(rm, false)) & 0xffff;
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 1:
                a = regs.eRegs[reg] & 0xffff;
                b = mem.peekW(getMod01RwAdr(rm, false)) & 0xffff;
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 2:
                a = regs.eRegs[reg] & 0xffff;
                b = mem.peekW(getMod02RwAdr(rm, false)) & 0xffff;
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 3:
                a = regs.eRegs[reg] & 0xffff;
                b = regs.eRegs[rm] & 0xffff;
                c = a | b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
        }

        regs.setSZPFw((short) c);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0c
    private void OR_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "OR AL,$%02x".formatted(imm8 & 0xff));
        regs.setAL((byte) ((regs.getAL() & 0xff) | (imm8 & 0xff)));

        regs.setSZPFb(regs.getAL());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0d
    private void OR_AX_IW() {
        short imm16 = fetchW(); // signed
        logger.log(Level.TRACE, "OR AX,$%04x".formatted(imm16 & 0xffff));
        regs.setAX((short) ((regs.getAX() & 0xffff) | imm16));

        regs.setSZPFw(regs.getAX());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x0e
    private void PUSH_CS() {
        logger.log(Level.TRACE, "PUSH CS");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getCS());
    }

    // 0x0f
    private void POP_CS() {
        logger.log(Level.TRACE, "POP CS");
        regs.setCS(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x10
    private void ADC_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "ADC EB,GB modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        short c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff) + (regs.isCF() ? 1 : 0));
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff) + (regs.isCF() ? 1 : 0));
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff) + (regs.isCF() ? 1 : 0));
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (regs.eRegs[rm] & 0x00ff);
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (short) ((a & 0xff) + (b & 0xff) + (regs.isCF() ? 1 : 0));
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ic & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbAdd(a, b, ic);
        regs.setCFb(c);
        regs.setAF(a, b, ic);
    }

    // 0x13
    private void ADC_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "ADC gw,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // short gw = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short gw = regs.eRegs[reg];

        switch (mod) {
            case 0:
                a = gw;
                b = mem.peekW(getMod00RwAdr(rm, false));
                c = (a & 0xffff) + (b & 0xffff) + (regs.isCF() ? 1 : 0);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 1:
                a = gw;
                b = mem.peekW(getMod01RwAdr(rm, false));
                c = (a & 0xffff) + (b & 0xffff) + (regs.isCF() ? 1 : 0);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 2:
                a = gw;
                b = mem.peekW(getMod02RwAdr(rm, false));
                c = (a & 0xffff) + (b & 0xffff) + (regs.isCF() ? 1 : 0);
                ic = (short) c;
                regs.eRegs[reg] = (short) c;
                break;
            case 3:
                a = gw;
                b = regs.eRegs[rm];
                c = (a & 0xffff) + (b & 0xffff) + (regs.isCF() ? 1 : 0);
                regs.eRegs[reg] = (short) c;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOFwAdd(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x14
    private void ADC_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "ADC AL,$%02x".formatted(imm8 & 0xff));

        byte a, b;
        short c;
        byte ic;

        a = regs.getAL();
        b = imm8;
        c = (short) ((a & 0xff) + (b & 0xff) + (regs.isCF() ? 1 : 0));
        ic = (byte) c;
        regs.setAL((byte) c);

        regs.setSZPFb(ic);
        regs.setOFbAdd(a, b, ic);
        regs.setCFb(c);
        regs.setAF(a, b, ic);
    }

    // 0x16
    private void pushSS() {
        logger.log(Level.TRACE, "PUSH SS");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getSS());
    }

    // 0x17
    private void popSS() {
        logger.log(Level.TRACE, "POP SS");
        regs.setSS(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x19
    private void SBB_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "SBB EW,GW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short gw = regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = (short) ((gw & 0xffff) + (regs.isCF() ? 1 : 0));
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = (short) ((gw & 0xffff) + (regs.isCF() ? 1 : 0));
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = (short) ((gw & 0xffff) + (regs.isCF() ? 1 : 0));
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 3:
                a = regs.eRegs[rm];
                b = (short) ((gw & 0xffff) + (regs.isCF() ? 1 : 0));
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                regs.eRegs[rm] = ic;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOFwSub(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x1c
    private void SBB_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "SBB AL,$%02x".formatted(imm8 & 0xff));

        byte a = regs.getAL();
        byte b = (byte) ((imm8 & 0xff) + (regs.isCF() ? 1 : 0));
        int c = (a & 0xff) - (b & 0xff);
        byte ic = (byte) c;
        regs.setAL(ic);

        regs.setSZPFb(ic);
        regs.setOFbSub(a, b, ic);
        regs.setCFb((short) c);
        regs.setAF(a, b, ic);
    }

    // 0x1e
    private void PUSH_DS() {
        logger.log(Level.TRACE, "PUSH DS");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getDS());
    }

    // 0x1f
    private void POP_DS() {
        logger.log(Level.TRACE, "POP DS");
        regs.setDS(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x20
    private void AND_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "AND EB,gb modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a; // signed
        byte b; // signed
        byte c; // signed
        byte ic = 0;

        byte gb; // signed
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a & b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a & b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a & b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (regs.eRegs[rm] & 0x00ff);
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (byte) (a & b);
                ic = c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ic & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x21
    private void AND_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "AND EW,gw modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short a;
        short b;
        int c;
        short ic = 0;

        short gw;
        gw = regs.eRegs[reg];
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) & (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) & (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) & (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 3:
                a = regs.eRegs[rm];
                b = gw;
                c = (a & 0xffff) & (b & 0xffff);
                ic = (short) c;
                regs.eRegs[rm] = ic;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x22
    private void AND_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "AND GB,EB modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a; // signed
        byte b; // signed
        byte c; // signed
        byte ic = 0;

        byte gb; // signed
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
                a = gb;
                b = mem.peekB(getMod00RwAdr(rm, false));
                c = (byte) (a & b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 1:
                a = gb;
                b = mem.peekB(getMod01RwAdr(rm, false));
                c = (byte) (a & b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 2:
                a = gb;
                b = mem.peekB(getMod02RwAdr(rm, false));
                c = (byte) (a & b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 3:
                a = gb;
                if (rm < 4) b = (byte) (regs.eRegs[rm] & 0x00ff);
                else b = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                c = (byte) (a & b);
                ic = c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x23
    private void AND_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "AND GW,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        int a;
        int b;
        int c;
        short ic = 0;
        switch (mod) {
            case 0:
                a = regs.eRegs[reg] & 0xffff;
                b = mem.peekW(getMod00RwAdr(rm, false)) & 0xffff;
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 1:
                a = regs.eRegs[reg] & 0xffff;
                b = mem.peekW(getMod01RwAdr(rm, false)) & 0xffff;
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 2:
                a = regs.eRegs[reg] & 0xffff;
                b = mem.peekW(getMod02RwAdr(rm, false)) & 0xffff;
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
            case 3:
                a = regs.eRegs[reg] & 0xffff;
                b = regs.eRegs[rm] & 0xffff;
                c = a & b;
                ic = (short) c;
                regs.eRegs[reg] = ic;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x24
    private void AND_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "AND AL,$%02x".formatted(imm8 & 0xff));
        regs.setAL((byte) ((regs.getAL() & 0xff) & (imm8 & 0xff)));

        regs.setSZPFb(regs.getAL());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x25
    private void AND_AX_IW() {
        short imm16 = fetchW(); // signed
        logger.log(Level.TRACE, "AND AX,$%04x".formatted(imm16 & 0xffff));
        regs.setAX((short) ((regs.getAX() & 0xffff) & imm16));

        regs.setSZPFw(regs.getAX());
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
        byte modRw = fetch();
        logger.log(Level.TRACE, "SUB EB,gb modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                mem.pokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (regs.eRegs[rm] & 0x00ff);
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ic & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbSub(a, b, ic);
        regs.setCFb((short) c);
        regs.setAF(a, b, ic);
    }

    // 0x29
    private void SUB_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "SUB EW,gw modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short gw = regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                mem.pokeW(ptr, ic);
                break;
            case 3:
                a = regs.eRegs[rm];
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                regs.eRegs[rm] = ic;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOFwSub(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x2a
    private void SUB_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "SUB gb,EB modRw:$%02x".formatted(modRw));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
                a = gb;
                b = mem.peekB(getMod00RwAdr(rm, false));
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 1:
                a = gb;
                b = mem.peekB(getMod01RwAdr(rm, false));
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 2:
                a = gb;
                b = mem.peekB(getMod02RwAdr(rm, false));
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
            case 3:
                a = gb;
                if (rm < 4) b = (byte) (regs.eRegs[rm] & 0x00ff);
                else b = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbSub(a, b, ic);
        regs.setCFb((short) c);
        regs.setAF(a, b, ic);
    }

    // 0x2b
    private void SUB_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "SUB GW,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short a;
        short b = 0;
        int c;
        short ic;
        a = regs.eRegs[reg];
        switch (mod) {
            case 0:
                b = mem.peekW(getMod00RwAdr(rm, false));
                break;
            case 1:
                b = mem.peekW(getMod01RwAdr(rm, false));
                break;
            case 2:
                b = mem.peekW(getMod02RwAdr(rm, false));
                break;
            case 3:
                b = regs.eRegs[rm];
                break;
        }
        c = (a & 0xffff) - (b & 0xffff);
        ic = (short) c;
        regs.eRegs[reg] = ic;

        regs.setSZPFw(ic);
        regs.setOFwSub(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x2c
    private void SUB_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "SUB AL,$%02x".formatted(imm8 & 0xff));

        byte a = regs.getAL();
        byte b = imm8;
        int c = (a & 0xff) - (b & 0xff);
        byte ic = (byte) c;
        regs.setAL(ic);

        regs.setSZPFb(ic);
        regs.setOFbSub(a, b, ic);
        regs.setCFb((short) c);
        regs.setAF(a, b, ic);
    }

    // 0x2d
    private void SUB_AX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "SUB AX,$%04x".formatted(imm16 & 0xffff));

        // short GW = regs.eRegs[reg];
        short a;
        short b;
        int c;
        short ic;
        a = regs.getAX();
        b = imm16;
        c = (a & 0xffff) - (b & 0xffff);
        ic = (short) c;
        regs.setAX(ic);

        regs.setSZPFw(ic);
        regs.setOFwSub(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
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
        byte modRw = fetch();
        logger.log(Level.TRACE, "XOR EB,gb modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a; // signed
        byte b; // signed
        byte c; // signed
        byte ic = 0;

        byte gb; // signed
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a ^ b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a ^ b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekB(ptr);
                b = gb;
                c = (byte) (a ^ b);
                ic = c;
                mem.pokeB(ptr, ic);
                break;
            case 3:
                if (rm < 4) a = (byte) (regs.eRegs[rm] & 0x00ff);
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (byte) (a ^ b);
                ic = c;
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ic & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ic & 0xff) << 8));
                break;
        }

        regs.setSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x32
    private void XOR_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "XOR GB,EB modRw:$%02x".formatted(modRw));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // short GW = regs.eRegs[reg];
        byte a; // signed
        byte b = 0; // signed
        byte c; // signed
        byte ic;

        byte gb; // signed
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        a = gb;
        switch (mod) {
            case 0:
                b = mem.peekB(getMod00RwAdr(rm, false));
                break;
            case 1:
                b = mem.peekB(getMod01RwAdr(rm, false));
                break;
            case 2:
                b = mem.peekB(getMod02RwAdr(rm, false));
                break;
            case 3:
                if (rm < 4) b = (byte) (regs.eRegs[rm] & 0x00ff);
                else b = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }
        c = (byte) (a ^ b);
        ic = c;
        if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (ic & 0xff));
        else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((ic & 0xff) << 8));

        regs.setSZPFb(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x33
    private void XOR_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "XOR GW,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // short GW = regs.eRegs[reg];
        int a;
        int b = 0;
        int c;
        short ic;
        a = regs.eRegs[reg] & 0xffff;
        switch (mod) {
            case 0:
                b = mem.peekW(getMod00RwAdr(rm, false)) & 0xffff;
                break;
            case 1:
                b = mem.peekW(getMod01RwAdr(rm, false)) & 0xffff;
                break;
            case 2:
                b = mem.peekW(getMod02RwAdr(rm, false)) & 0xffff;
                break;
            case 3:
                b = regs.eRegs[rm];
                break;
        }
        c = a ^ b;
        ic = (short) c;
        regs.eRegs[reg] = ic;

        regs.setSZPFw(ic);
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x34
    private void XOR_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "XOR AL,$%02x".formatted(imm8 & 0xff));
        regs.setAL((byte) ((regs.getAL() & 0xff) ^ (imm8 & 0xff)));

        regs.setSZPFb(regs.getAL());
        regs.setOF(false);
        regs.setCF(false);
        regs.setAF(false); // TBD
    }

    // 0x35
    private void XOR_AX_IW() {
        short imm16 = fetchW(); // signed
        logger.log(Level.TRACE, "XOR AX,$%04x".formatted(imm16 & 0xffff));
        regs.setAX((short) ((regs.getAX() & 0xffff) ^ imm16));

        regs.setSZPFw(regs.getAX());
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
        byte modRw = fetch();
        logger.log(Level.TRACE, "CMP EB,gb modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
                a = mem.peekB(getMod00RwAdr(rm, false));
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
            case 1:
                a = mem.peekB(getMod01RwAdr(rm, false));
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
            case 2:
                a = mem.peekB(getMod02RwAdr(rm, false));
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
            case 3:
                if (rm < 4) a = (byte) (regs.eRegs[rm] & 0x00ff);
                else a = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                b = gb;
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbSub(a, b, (byte) c);
        regs.setCFb((short) c);
        regs.setAF(a, b, (byte) c);
    }

    // 0x39
    private void CMP_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "CMP EW,gw modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short a = 0;
        short b = 0;
        int c = 0;
        short ic = 0;

        short gw = regs.eRegs[reg];

        int ptr;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                a = mem.peekW(ptr);
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                break;
            case 3:
                a = regs.eRegs[rm];
                b = gw;
                c = (a & 0xffff) - (b & 0xffff);
                ic = (short) c;
                break;
        }

        regs.setSZPFw(ic);
        regs.setOFwSub(a, b, ic);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) ic);
    }

    // 0x3a
    private void CMP_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "CMP gb,EB modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte a = 0;
        byte b = 0;
        int c = 0;
        byte ic = 0;

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
                a = gb;
                b = mem.peekB(getMod00RwAdr(rm, false));
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
            case 1:
                a = gb;
                b = mem.peekB(getMod01RwAdr(rm, false));
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
            case 2:
                a = gb;
                b = mem.peekB(getMod02RwAdr(rm, false));
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
            case 3:
                a = gb;
                if (rm < 4) b = (byte) (regs.eRegs[rm] & 0x00ff);
                else b = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                c = (a & 0xff) - (b & 0xff);
                ic = (byte) c;
                break;
        }

        regs.setSZPFb(ic);
        regs.setOFbSub(a, b, (byte) c);
        regs.setCFb((short) c);
        regs.setAF(a, b, (byte) c);
    }

    // 0x3b
    private void CMP_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "CMP GW,EW modRw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // short GW = regs.eRegs[reg];
        short a = 0;
        short b = 0;
        int c = 0;
        switch (mod) {
            case 0:
                a = regs.eRegs[reg];
                b = mem.peekW(getMod00RwAdr(rm, false));
                c = (a & 0xffff) - (b & 0xffff);
                break;
            case 1:
                a = regs.eRegs[reg];
                b = mem.peekW(getMod01RwAdr(rm, false));
                c = (a & 0xffff) - (b & 0xffff);
                break;
            case 2:
                a = regs.eRegs[reg];
                b = mem.peekW(getMod02RwAdr(rm, false));
                c = (a & 0xffff) - (b & 0xffff);
                break;
            case 3:
                a = regs.eRegs[reg];
                b = regs.eRegs[rm];
                c = (a & 0xffff) - (b & 0xffff);
                break;
        }
        short ans = (short) c;

        regs.setSZPFw(ans);
        regs.setOFwSub(a, b, (short) c);
        regs.setCFw(c);
        regs.setAF((byte) a, (byte) b, (byte) c);
    }

    // 0x3c
    private void CMP_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "CMP AL,$%02x".formatted(imm8 & 0xff));

        int ians = (regs.getAL() & 0xff) - (imm8 & 0xff);
        byte ans = (byte) ians;

        regs.setSZPFb(ans);
        regs.setOFbSub(regs.getAL(), imm8, ans);
        regs.setCFb((short) ians);
        regs.setAF(regs.getAL(), imm8, ans);
    }

    // 0x3d
    private void CMP_AX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "CMP AX,$%04x".formatted(imm16 & 0xffff));
        int ians = (regs.getAX() & 0xffff) - (imm16 & 0xffff);
        short ans = (short) ians;

        regs.setSZPFw(ans);
        regs.setOFwSub(regs.getAX(), imm16, ans);
        regs.setCFw(ians);
        regs.setAF((byte) regs.getAX(), (byte) imm16, (byte) ans);
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

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x41
    private void INC_CX() {
        logger.log(Level.TRACE, "INC CX");

        int a = regs.getCX();
        int b = 1;
        int ans = a + b;
        regs.setCX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x42
    private void INC_DX() {
        logger.log(Level.TRACE, "INC DX");

        int a = regs.getDX();
        int b = 1;
        int ans = a + b;
        regs.setDX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x43
    private void INC_BX() {
        logger.log(Level.TRACE, "INC BX");

        int a = regs.getBX();
        int b = 1;
        int ans = a + b;
        regs.setBX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x44
    private void INC_SP() {
        logger.log(Level.TRACE, "INC SP");

        int a = regs.getSP();
        int b = 1;
        int ans = a + b;
        regs.setSP((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x45
    private void INC_BP() {
        logger.log(Level.TRACE, "INC BP");

        int a = regs.getBP();
        int b = 1;
        int ans = a + b;
        regs.setBP((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x46
    private void INC_SI() {
        logger.log(Level.TRACE, "INC SI");

        int a = regs.getSI();
        int b = 1;
        int ans = a + b;
        regs.setSI((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x47
    private void INC_DI() {
        logger.log(Level.TRACE, "INC DI");

        int a = regs.getDI();
        int b = 1;
        int ans = a + b;
        regs.setDI((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwAdd((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x48
    private void DEC_AX() {
        logger.log(Level.TRACE, "DEC AX");

        int a = regs.getAX();
        int b = 1;
        int ans = a - b;
        regs.setAX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x49
    private void DEC_CX() {
        logger.log(Level.TRACE, "DEC CX");

        int a = regs.getCX();
        int b = 1;
        int ans = a - b;
        regs.setCX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4a
    private void DEC_DX() {
        logger.log(Level.TRACE, "DEC DX");

        int a = regs.getDX();
        int b = 1;
        int ans = a - b;
        regs.setDX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4b
    private void DEC_BX() {
        logger.log(Level.TRACE, "DEC BX");

        int a = regs.getBX();
        int b = 1;
        int ans = a - b;
        regs.setBX((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4c
    private void DEC_SP() {
        logger.log(Level.TRACE, "DEC SP");

        int a = regs.getSP();
        int b = 1;
        int ans = a - b;
        regs.setSP((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4d
    private void DEC_BP() {
        logger.log(Level.TRACE, "DEC BP");

        int a = regs.getBP();
        int b = 1;
        int ans = a - b;
        regs.setBP((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4e
    private void DEC_SI() {
        logger.log(Level.TRACE, "DEC SI");

        int a = regs.getSI();
        int b = 1;
        int ans = a - b;
        regs.setSI((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x4f
    private void DEC_DI() {
        logger.log(Level.TRACE, "DEC DI");

        int a = regs.getDI();
        int b = 1;
        int ans = a - b;
        regs.setDI((short) ans);

        regs.setSZPFw((short) ans);
        regs.setOFwSub((short) a, (short) b, (short) ans);
        regs.setCFw(ans);
        regs.setAF((byte) a, (byte) b, (byte) ans);
    }

    // 0x50
    private void PUSH_AX() {
        logger.log(Level.TRACE, "PUSH AX");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getAX());
    }

    // 0x51
    private void PUSH_CX() {
        logger.log(Level.TRACE, "PUSH CX");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getCX());
    }

    // 0x52
    private void PUSH_DX() {
        logger.log(Level.TRACE, "PUSH DX");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getDX());
    }

    // 0x53
    private void PUSH_BX() {
        logger.log(Level.TRACE, "PUSH BX");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getBX());
    }

    // 0x54
    private void PUSH_SP() {
        logger.log(Level.TRACE, "PUSH SP");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getSP());
    }

    // 0x55
    private void PUSH_BP() {
        logger.log(Level.TRACE, "PUSH BP");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getBP());
    }

    // 0x56
    private void PUSH_SI() {
        logger.log(Level.TRACE, "PUSH SI");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getSI());
    }

    // 0x57
    private void PUSH_DI() {
        logger.log(Level.TRACE, "PUSH DI");
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getDI());
    }

    // 0x58
    private void POP_AX() {
        logger.log(Level.TRACE, "POP AX");
        regs.setAX(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x59
    private void POP_CX() {
        logger.log(Level.TRACE, "POP CX");
        regs.setCX(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5a
    private void POP_DX() {
        logger.log(Level.TRACE, "POP DX");
        regs.setDX(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5b
    private void POP_BX() {
        logger.log(Level.TRACE, "POP BX");
        regs.setBX(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5c
    private void POP_SP() {
        logger.log(Level.TRACE, "POP SP");
        regs.setSP(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5d
    private void POP_BP() {
        logger.log(Level.TRACE, "POP BP");
        regs.setBP(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5e
    private void POP_SI() {
        logger.log(Level.TRACE, "POP SI");
        regs.setSI(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x5f
    private void POP_DI() {
        logger.log(Level.TRACE, "POP DI");
        regs.setDI(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
    }

    // 0x60
    private void PUSHA() {
        logger.log(Level.TRACE, "PUSHA");
        short sp = regs.getSP();
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getAX());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getCX());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getDX());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getBX());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), sp);
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getBP());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getSI());
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.getDI());
    }

    // 0x61
    private void POPA() {
        logger.log(Level.TRACE, "POPA");
        short sp;

        regs.setDI( mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setSI( mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setBP(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        sp = mem.peekW(regs.getSS_SP());
        regs.addSP(2);
        regs.setBX(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setDX( mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setCX(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setAX( mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.setSP(sp);
    }

    // 0x72
    private void JB_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JB short:$%02x".formatted(imm8 & 0xff));

        if (regs.isCF()) {
            regs.ip += imm8;
        }
    }

    // 0x73
    private void JNB_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNB short:$%02x".formatted(imm8 & 0xff));

        if (!regs.isCF()) {
            regs.ip += imm8;
        }
    }

    // 0x74
    private void JZ_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JZ short:$%02x".formatted(imm8 & 0xff));

        if (regs.isZF()) {
            regs.ip += imm8;
        }
    }

    // 0x75
    private void JNZ_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNZ short:$%02x".formatted(imm8 & 0xff));

        if (!regs.isZF()) {
            regs.ip += imm8;
        }
    }

    // 0x76
    private void JBE_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JBE short:$%02x".formatted(imm8 & 0xff));

        if (regs.isCF() || regs.isZF()) {
            regs.ip += imm8;
        }
    }

    // 0x77
    private void JNBE_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNBE short:$%02x".formatted(imm8 & 0xff));

        if (!regs.isCF() && !regs.isZF()) { // cmp then op1<op2
            regs.ip += imm8;
        }
    }

    // 0x78
    private void JS_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JS short:$%02x".formatted(imm8 & 0xff));

        if (regs.isSF()) {
            regs.ip += imm8;
        }
    }

    // 0x79
    private void JNS_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNS short:$%02x".formatted(imm8 & 0xff));

        if (!regs.isSF()) {
            regs.ip += imm8;
        }
    }

    // 0x7d
    private void JNL_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNL short:$%02x".formatted(imm8 & 0xff));

        if (regs.isSF() == regs.isOF()) {
            regs.ip += imm8;
        }
    }

    // 0x7e
    private void JNG_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNG short:$%02x".formatted(imm8 & 0xff));

        if (regs.isZF() || regs.isSF() != regs.isOF()) {
            regs.ip += imm8;
        }
    }

    // 0x7f
    private void JNLE_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JNLE short:$%02x".formatted(imm8 & 0xff));

        if (!regs.isZF() && regs.isSF() == regs.isOF()) {
            regs.ip += imm8;
        }
    }

    // 0x80
    private void GRP1B() {
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        short ib;
        int ians;
        byte ans;
        byte eb = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }
        ib = (short) (fetch() & 0xff);

        switch (reg) {
            case 0: // ADD EB,IB
                logger.log(Level.TRACE, "ADD EB,$%02x".formatted(ib));
                ians = (eb & 0xff) + (ib & 0xff);
                ans = (byte) ians;
                regs.setSZPFb((byte) ians);
                regs.setOFbAdd(eb, (byte) ib, ans);
                regs.setCFb((short) ians);
                regs.setAF(eb, (byte) ib, ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 1: // OR EB,IB
                logger.log(Level.TRACE, "OR EB,$%02x".formatted(ib));
                ians = (eb & 0xff) | (ib & 0xff);
                ans = (byte) ians;
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 2: // ADC EB,IB
                logger.log(Level.TRACE, "ADC EB,$%02x".formatted(ib));
                ians = (eb & 0xff) + (ib & 0xff) + (regs.isCF() ? 1 : 0);
                ans = (byte) ians;
                regs.setSZPFb((byte) ians);
                regs.setOFbAdd(eb, (byte) ib, ans);
                regs.setCFb((short) ians);
                regs.setAF(eb, (byte) ib, ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 3: // SBB
                logger.log(Level.TRACE, "SBB EB,$%02x".formatted(ib));
                ians = (eb & 0xff) - ((ib & 0xff) + (regs.isCF() ? 1 : 0));
                ans = (byte) ians;
                regs.setSZPFb((byte) ians);
                regs.setOFbSub(eb, (byte) ib, ans);
                regs.setCFb((short) ians);
                regs.setAF(eb, (byte) ib, ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 4: // AND EB,IB
                logger.log(Level.TRACE, "AND EB,$%02x".formatted(ib));
                ians = (eb & 0xff) & (ib & 0xff);
                ans = (byte) ians;
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 5: // SUB EB,IB
                logger.log(Level.TRACE, "SUB EB,$%02x".formatted(ib));
                ians = (eb & 0xff) - (ib & 0xff);
                ans = (byte) ians;
                regs.setSZPFb((byte) ians);
                regs.setOFbSub(eb, (byte) ib, ans);
                regs.setCFb((short) ians);
                regs.setAF(eb, (byte) ib, ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 6:
                logger.log(Level.TRACE, "XOR EB,$%02x".formatted(ib));
                ians = (eb & 0xff) ^ (ib & 0xff);
                ans = (byte) ians;
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, (byte) ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 7: // CMP EB,IB
                logger.log(Level.TRACE, "CMP EB,$%02x".formatted(ib));
                ians = (eb & 0xff) - (ib & 0xff);
                ans = (byte) ians;
                regs.setSZPFb(ans);
                regs.setOFbSub(eb, (byte) ib, ans);
                regs.setCFb((short) ians);
                regs.setAF(eb, (byte) ib, ans);
                break;
        }
    }

    // 0x81
    private void GRP1W() {
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        short iw;
        int ians;
        short ans;
        short ew = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }
        iw = fetchW();

        switch (reg) {
            case 0: // ADD EW,IW
                logger.log(Level.TRACE, "ADD EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) + (iw & 0xffff);
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOFwAdd(ew, iw, ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, (byte) iw, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 1: // OR EW,IW
                logger.log(Level.TRACE, "OR EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) | iw;
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 2: // ADC EW,IW
                logger.log(Level.TRACE, "ADC EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) + (iw & 0xffff) + (regs.isCF() ? 1 : 0);
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOFwAdd(ew, iw, ans);
                regs.setCFw((int) ians);
                regs.setAF((byte) ew, (byte) iw, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 3:
                throw new UnsupportedOperationException();
            case 4: // AND EW,IW
                logger.log(Level.TRACE, "AND EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) & (iw & 0xffff);
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 5: // SUB EW,IW
                logger.log(Level.TRACE, "SUB EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) - (iw & 0xffff);
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOFwSub(ew, iw, ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, (byte) iw, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 6: // XOR EW,IW
                logger.log(Level.TRACE, "XOR EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) ^ (iw & 0xffff);
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 7: // CMP EW,IW
                logger.log(Level.TRACE, "CMP EW,$%04x".formatted(iw & 0xffff));
                ians = (ew & 0xffff) - iw;
                ans = (short) ians;
                regs.setSZPFw(ans);
                regs.setOFwSub(ew, iw, ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, (byte) iw, (byte) ans);
                break;
        }
    }

    // 0x83
    private void GRP1WB() {
        byte modRw = fetch();

        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short ew = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }
        byte ib;
        ib = fetch();
        int ians;
        short ans;

        switch (reg) {
            case 0: // ADD EW,IB
                logger.log(Level.TRACE, "ADD EW,$%02x".formatted(ib & 0xff));
                ians = (ew & 0xffff) + ib;
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOFwAdd(ew, ib, ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, ib, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 1: // OR EW,IB
                logger.log(Level.TRACE, "OR EW,$%02x".formatted(ib & 0xff));
                ians = (ew & 0xffff) | ib;
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 2: // ADC EW,IB
                logger.log(Level.TRACE, "ADC EW,$%02x".formatted(ib));
                ians = (ew & 0xffff) + (byte) (ib + (regs.isCF() ? 1 : 0));
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOFwAdd(ew, (short) (ib & 0xff), ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, ib, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 3: // SBB EW,IB
                logger.log(Level.TRACE, "SBB EW,$%02x".formatted(ib & 0xff));
                ians = (ew & 0xffff) - (byte) (ib + (regs.isCF() ? 1 : 0));
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOFwSub(ew, (short) (ib & 0xff), ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, ib, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 4: // AND EW,IB
                logger.log(Level.TRACE, "AND EW,$%02x".formatted(ib & 0xff));
                ians = (ew & 0xffff) & ib;
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 5: // SUB EW,IB
                logger.log(Level.TRACE, "SUB EW,$%02x".formatted(ib));
                ians = (ew & 0xffff) - ib;
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOFwSub(ew, (short) (ib & 0xff), ans);
                regs.setCFw((int) ians);
                regs.setAF((byte) ew, ib, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 6: // XOR EW,IB
                logger.log(Level.TRACE, "XOR EW,$%02x".formatted(ib));
                ians = (ew & 0xffff) ^ ib;
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ans);
                        break;
                    case 3:
                        regs.eRegs[rm] = ans;
                        break;
                }
                break;
            case 7: // CMP EW,IB
                logger.log(Level.TRACE, "CMP EW,$%02x".formatted(ib));
                ians = (ew & 0xffff) - ib;
                ans = (short) ians;
                regs.setSZPFw((short) ians);
                regs.setOFwSub(ew, (short) (ib & 0xff), ans);
                regs.setCFw(ians);
                regs.setAF((byte) ew, ib, (byte) ans);
                break;
        }
    }

    // 0x84
    private void TEST_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "TEST EB,GB modrw:$%02x".formatted(modRw));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte eb = 0;
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);
        switch (mod) {
            case 0:
            case 1:
            case 2:
            case 3:
                byte ans = (byte) (eb & (gb & 0xff));
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
        }
    }

    // 0x85
    private void TEST_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "TEST EW,GW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short ew = 0;
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }

        short gw;
        gw = regs.eRegs[reg];
        switch (mod) {
            case 0:
            case 1:
            case 2:
            case 3:
                short ans = (short) (ew & (gw & 0xffff));
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
        }
    }

    // 0x86
    private void XCHG_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "XCHG EB,GB modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte eb = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        // GB <-> EB
        byte p = gb;
        gb = eb;
        eb = p;

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeB(ptr, eb);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (eb & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((eb & 0xff) << 8));
                break;
        }

        if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (gb & 0xff));
        else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((gb & 0xff) << 8));
    }

    // 0x87
    private void XCHG_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "XCHG EW,GW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short ew = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }

        short gw;
        gw = regs.eRegs[reg];

        // GW <-> EW
        short p = gw;
        gw = ew;
        ew = p;

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeW(ptr, ew);
                break;
            case 3:
                regs.eRegs[rm] = ew;
                break;
        }
        regs.eRegs[reg] = gw;
    }

    // 0x88
    private void MOV_EB_GB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "MOV EB,GB modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // byte EB = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                // EB = (byte)mem.PeekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                // EB = (byte)mem.PeekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                // EB = (byte)mem.PeekB(ptr);
                break;
            case 3:
                // if (rm < 4) EB = (byte)(byte)regs.eRegs[rm];
                // else EB = (byte)(byte)(regs.eRegs[rm - 4] >> 8);
                break;
        }

        byte gb;
        if (reg < 4) gb = (byte) (regs.eRegs[reg] & 0x00ff);
        else gb = (byte) ((regs.eRegs[reg - 4] & 0xff00) >> 8);

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeB(ptr, gb);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (gb & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((gb & 0xff) << 8));
                break;
        }
    }

    // 0x89
    private void MOV_EW_GW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "MOV EW,GW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short gw = regs.eRegs[reg];
        switch (mod) {
            case 0:
                mem.pokeW(getMod00RwAdr(rm, false), gw);
                break;
            case 1:
                mem.pokeW(getMod01RwAdr(rm, false), gw);
                break;
            case 2:
                mem.pokeW(getMod02RwAdr(rm, false), gw);
                break;
            case 3:
                regs.eRegs[rm] = gw;
                break;
        }
    }

    // 0x8a
    private void MOV_GB_EB() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "MOV GB,EB modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte eb = 0;
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (eb & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((eb & 0xff) << 8));
                break;
            case 3:
                if (reg < 4) regs.eRegs[reg] = (short) ((regs.eRegs[reg] & 0xff00) | (eb & 0xff));
                else regs.eRegs[reg - 4] = (short) ((regs.eRegs[reg - 4] & 0xff) | ((eb & 0xff) << 8));
                break;
        }
    }

    // 0x8b
    private void MOV_GW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "MOV GW,EW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        switch (mod) {
            case 0:
                regs.eRegs[reg] = mem.peekW(getMod00RwAdr(rm, false));
                break;
            case 1:
                regs.eRegs[reg] = mem.peekW(getMod01RwAdr(rm, false));
                break;
            case 2:
                regs.eRegs[reg] = mem.peekW(getMod02RwAdr(rm, false));
                break;
            case 3:
                regs.eRegs[reg] = regs.eRegs[rm];
                break;
        }
    }

    // 0x8c
    private void MOV_EW_SW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "MOV EW,SW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x18) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short sw = regs.sRegs[reg];
        switch (mod) {
            case 0:
                mem.pokeW(getMod00RwAdr(rm, false), sw);
                break;
            case 1:
                mem.pokeW(getMod01RwAdr(rm, false), sw);
                break;
            case 2:
                mem.pokeW(getMod02RwAdr(rm, false), sw);
                break;
            case 3:
                regs.eRegs[rm] = sw;
                break;
        }
    }

    // 0x8d
    private void LEA_GW_M() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "LEA GW,M modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        switch (mod) {
            case 0:
                regs.eRegs[reg] = (short) getMod00RwAdr(rm, true);
                break;
            case 1:
                regs.eRegs[reg] = (short) getMod01RwAdr(rm, true);
                break;
            case 2:
                regs.eRegs[reg] = (short) getMod02RwAdr(rm, true);
                break;
            case 3:
                throw new UnsupportedOperationException();
        }
    }

    // 0x8e
    private void MOV_SW_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "MOV SW,EW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x18) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        switch (mod) {
            case 0:
                regs.sRegs[reg] = mem.peekW(getMod00RwAdr(rm, false));
                break;
            case 1:
                regs.sRegs[reg] = mem.peekW(getMod01RwAdr(rm, false));
                break;
            case 2:
                regs.sRegs[reg] = mem.peekW(getMod02RwAdr(rm, false));
                break;
            case 3:
                short r = regs.eRegs[rm];
                regs.sRegs[reg] = r;
                break;
        }
    }

    // 0x8f
    private void POP_EW() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "POP EW modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x18) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        int ptr;
        short imm16 = mem.peekW(regs.getSS_SP());
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                mem.pokeW(ptr, imm16);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                mem.pokeW(ptr, imm16);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                mem.pokeW(ptr, imm16);
                break;
            case 3:
                regs.eRegs[rm] = imm16;
                break;
        }
        regs.addSP(2);
    }

    // 0x84
    private void TEST_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "TEST AL,$%02x".formatted(imm8 & 0xff));

        byte ans = (byte) ((regs.getAL() & 0xff) & (imm8 & 0xff));
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
        mem.pokeW(regs.getSS_SP(), regs.flag);
    }

    // 0x9d
    private void POPF() {
        logger.log(Level.TRACE, "POPF");
        regs.flag = mem.peekW(regs.getSS_SP());
        regs.addSP(2);
    }

    // 0xa0
    private void MOV_AL_OB() {
        int seg = getSegment();
        short ptr = fetchW();
        logger.log(Level.TRACE, "MOV AL,[$%04x]".formatted(ptr & 0xffff));

        regs.setAL(mem.peekB(seg + (ptr & 0xffff)));
    }

    // 0xa1
    private void MOV_AX_OW() {
        int seg = getSegment();
        short ptr = fetchW();
        logger.log(Level.TRACE, "MOV AX,[$%04x]".formatted(ptr & 0xffff));

        regs.setAX(mem.peekW(seg + (ptr & 0xffff)));
    }

    // 0xa2
    private void MOV_OB_AL() {
        int seg = getSegment();
        short ptr = fetchW();
        logger.log(Level.TRACE, "MOV [$%04x],AL".formatted(ptr & 0xffff));

        mem.pokeB(seg + (ptr & 0xffff), regs.getAL());
    }

    // 0xa3
    private void MOV_OW_AX() {
        int seg = getSegment();
        short ptr = fetchW();
        logger.log(Level.TRACE, "MOV [$%04x],AX".formatted(ptr & 0xffff));

        mem.pokeW(seg + (ptr & 0xffff), regs.getAX());
    }

    // 0xa4
    private void MOVSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "MOVSB [ES:DI]:%05x [DS:SI]:%05x".formatted(regs.getES_DI(), regs.getDS_SI()));
            mem.pokeB(regs.getES_DI(), mem.peekB(regs.getDS_SI()));
            regs.addDI((short) (regs.isDF() ? -1 : 1));
            regs.addSI((short) (regs.isDF() ? -1 : 1));
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
                    regs.ip--;
            }
        }
    }

    // 0xa5
    private void MOVSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "MOVSW [ES:DI]:%05x [DS:SI]:%05x".formatted(regs.getES_DI(), regs.getDS_SI()));
            mem.pokeW(regs.getES_DI(), mem.peekW(regs.getDS_SI()));
            regs.addDI((short) (regs.isDF() ? -2 : 2));
            regs.addSI((short) (regs.isDF() ? -2 : 2));
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
                    regs.ip--;
            }
        }
    }

    // 0xa6
    private void CMPSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            byte dsv = mem.peekB(regs.getDS_SI());
            byte edv = mem.peekB(regs.getES_DI());

            logger.log(Level.TRACE, "CMPSB [DS:SI] val:%02x'%c' [ES:DI] val:%02x'%c'".formatted(dsv & 0xff, (char) dsv, edv & 0xff, (char) edv));

            regs.addSI((short) ((regs.isDF()) ? -1 : 1));
            regs.addDI((short) ((regs.isDF()) ? -1 : 1));

            int ians = (dsv & 0xff) - (edv & 0xff);
            byte ans = (byte) ians;

            regs.setSF((ans & 0x80) != 0);
            regs.setOF(regs.isSF()
                    ? ((edv > 0 && ans > dsv) || (edv < 0 && ans < dsv))
                    : ans > dsv);
            regs.setCF((short) ians != ans);
            regs.setZF(ans == 0);
            regs.setPF((ans & 0x01) != 0);
            regs.setAF(false); // TBD
        }

        if (repSW) {
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.isZF()) || (repType != 0 && regs.isZF()))) {
                    repSW = false;
                } else
                    regs.ip--;
            }
        }
    }

    // 0xa7
    private void CMPSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            short dsv = mem.peekW(regs.getDS_SI());
            short edv = mem.peekW(regs.getES_DI());

            logger.log(Level.TRACE, "CMPSW [DS:SI] val:%04x [ES:DI] val:%04x".formatted(dsv & 0xffff, edv & 0xffff));

            regs.addSI((short) ((regs.isDF()) ? -2 : 2));
            regs.addDI((short) ((regs.isDF()) ? -2 : 2));

            int ians = dsv - edv;
            short ans = (short) (ians & 0xffff);

            regs.setSF((ans & 0x8000) != 0);
            regs.setOF(regs.isSF()
                    ? ((edv > 0 && ans > dsv) || (edv < 0 && ans < dsv))
                    : ans > dsv);
            regs.setCF(ians != (ans & 0xffff));
            regs.setZF(ans == 0);
            regs.setSZPFw(ans);
            regs.setAF(false); // TBD
        }

        if (repSW) {
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.isZF()) || (repType != 0 && regs.isZF()))) {
                    repSW = false;
                } else
                    regs.ip--;
            }
        }
    }

    // 0xaa
    private void STOSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "STOSB [ES:DI]:%05x AL:%02x'%c'".formatted(regs.getES_DI(), regs.getAL() & 0xff, (char) regs.getAL()));
            mem.pokeB(regs.getES_DI(), regs.getAL());
            regs.addDI((short) ((regs.isDF()) ? -1 : 1));
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
                    regs.ip--;
            }
        }
    }

    // 0xab
    private void STOSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "STOSW [ES:DI]:%05x <- AX:%04x", regs.getES_DI(), regs.getAX() & 0xffff);
            mem.pokeW(regs.getES_DI(), regs.getAX());
            regs.addDI((short) ((regs.isDF()) ? -2 : 2));
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
                    regs.ip--;
            }
        }
    }

    // 0xac
    private void LODSB() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            regs.setAL(mem.peekB(regs.getDS_SI()));
            logger.log(Level.TRACE, "LODSB [DS:SI]:%05x AL:%02x'%c'".formatted(regs.getDS_SI(), regs.getAL() & 0xff, Character.isISOControl((char) regs.getAL()) ? '.' : (char) regs.getAL()));
            regs.addSI((short) ((regs.isDF()) ? -1 : 1));
        }

        if (repSW) {
            // Simple repeat for LODS
            // REPNE is not possible (I think so, so I have not dealt with it yet)
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.isZF()) || (repType != 0 && regs.isZF()))) {
                    repSW = false;
                } else
                    regs.ip--;
            }
        }
    }

    // 0xad
    private void LODSW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            logger.log(Level.TRACE, "LODSW [DS:SI]:%05x -> AX:%04x".formatted(regs.getDS_SI(), regs.getAX() & 0xffff));
            regs.setAX(mem.peekW(regs.getDS_SI()));
            regs.addSI((short) ((regs.isDF()) ? -2 : 2));
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
                    regs.ip--;
            }
        }
    }

    // 0xaf
    private void SCASW() {
        if (!repSW || (repSW && regs.getCX() != 0)) {
            short dsv = regs.getAX();
            short edv = mem.peekW(regs.getES_DI());

            logger.log(Level.TRACE, "SCASW AX:%04x [ES:DI] val:%04x".formatted(dsv & 0xffff, edv & 0xffff));

            regs.addSI((short) ((regs.isDF()) ? -2 : 2));
            regs.addDI((short) ((regs.isDF()) ? -2 : 2));

            int ians = dsv - edv;
            short ans = (short) (ians & 0xffff);

            regs.setSF((ans & 0x8000) != 0);
            regs.setOF(regs.isSF()
                    ? ((edv > 0 && ans > dsv) || (edv < 0 && ans < dsv))
                    : ans > dsv);
            regs.setCF(ians != (ans & 0xffff));
            regs.setZF(ans == 0);
            regs.setSZPFw(ans);
            regs.setAF(false); // TBD
        }

        if (repSW) {
            if (regs.getCX() == 0) {
                repSW = false;
            } else {
                regs.decCX();
                if (regs.getCX() == 0 || ((repType == 0 && !regs.isZF()) || (repType != 0 && regs.isZF()))) {
                    repSW = false;
                } else
                    regs.ip--;
            }
        }
    }

    // 0xb0
    private void MOV_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV AL,$%02x".formatted(imm8 & 0xff));
        regs.setAL(imm8);
    }

    // 0xb1
    private void MOV_CL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV CL,$%02x".formatted(imm8 & 0xff));
        regs.setCL(imm8);
    }

    // 0xb2
    private void MOV_DL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV DL,$%02x".formatted(imm8 & 0xff));
        regs.setDL(imm8);
    }

    // 0xb3
    private void MOV_BL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV BL,$%02x".formatted(imm8 & 0xff));
        regs.setBL(imm8);
    }

    // 0xb4
    private void MOV_AH_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV AH,$%02x".formatted(imm8 & 0xff));
        regs.setAH(imm8);
    }

    // 0xb5
    private void MOV_CH_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV CH,$%02x".formatted(imm8 & 0xff));
        regs.setCH(imm8);
    }

    // 0xb6
    private void MOV_DH_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV DH,$%02x".formatted(imm8 & 0xff));
        regs.setDH(imm8);
    }

    // 0xb7
    private void MOV_BH_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "MOV BH,$%02x".formatted(imm8 & 0xff));
        regs.setBH(imm8);
    }

    // 0xb8
    private void MOV_AX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV AX,$%04x".formatted(imm16 & 0xffff));
        regs.setAX(imm16);
    }

    // 0xb9
    private void MOV_CX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV CX,$%04x".formatted(imm16 & 0xffff));
        regs.setCX(imm16);
    }

    // 0xba
    private void MOV_DX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV DX,$%04x".formatted(imm16 & 0xffff));
        regs.setDX(imm16);
    }

    // 0xbb
    private void MOV_BX_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV BX,$%04x".formatted(imm16 & 0xffff));
        regs.setBX(imm16);
    }

    // 0xbc
    private void MOV_SP_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV SP,$%04x".formatted(imm16 & 0xffff));
        regs.setSP(imm16);
    }

    // 0xbd
    private void MOV_BP_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV BP,$%04x".formatted(imm16 & 0xffff));
        regs.setBP(imm16);
    }

    // 0xbe
    private void MOV_SI_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV SI,$%04x".formatted(imm16 & 0xffff));
        regs.setSI(imm16);
    }

    // 0xbf
    private void MOV_DI_IW() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "MOV DI,$%04x".formatted(imm16 & 0xffff));
        regs.setDI(imm16);
    }

    // 0xc0 See below
    // 0xc1 See below

    // 0xc3
    private void RET() {
        logger.log(Level.TRACE, "RET");
        regs.ip = mem.peekW(regs.getSS_SP());
        regs.addSP(2);
    }

    // 0xc4
    private void LES_GW_EP() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "LES GW,EP modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // GW eRegs@reg

        int ptr;
        short v;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                v = mem.peekW(ptr);
                regs.eRegs[reg] = v;
                regs.setES(mem.peekW(ptr + 2));
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                v = mem.peekW(ptr);
                regs.eRegs[reg] = v;
                regs.setES(mem.peekW(ptr + 2));
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                v = mem.peekW(ptr);
                regs.eRegs[reg] = v;
                regs.setES(mem.peekW(ptr + 2));
                break;
            case 3:
                short r = regs.eRegs[rm];
                regs.sRegs[reg] = r;
                break;
        }
    }

    // 0xc5
    private void LDS_GW_EP() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "LDS GW,EP modrw:$%02x".formatted(modRw & 0xff));

        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        // GW eRegs@reg

        int ptr;
        short v;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                v = mem.peekW(ptr);
                regs.eRegs[reg] = v;
                regs.setDS(mem.peekW(ptr + 2));
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                v = mem.peekW(ptr);
                regs.eRegs[reg] = v;
                regs.setDS(mem.peekW(ptr + 2));
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                v = mem.peekW(ptr);
                regs.eRegs[reg] = v;
                regs.setDS(mem.peekW(ptr + 2));
                break;
            case 3:
                short r = regs.eRegs[rm];
                regs.sRegs[reg] = r;
                break;
        }
    }

    // 0xc6
    private void MOV_EB_IB() {
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        byte imm8;
        int ptr;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                imm8 = fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                mem.pokeB(ptr, imm8);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                imm8 = fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                mem.pokeB(ptr, imm8);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                imm8 = fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                mem.pokeB(ptr, imm8);
                break;
            case 3:
                imm8 = fetch();
                logger.log(Level.TRACE, "MOV EB,$%02x".formatted(imm8));
                // regs.eRegs[reg] = (short)((regs.eRegs[reg] & 0xff00) | imm8);
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (imm8 & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((imm8 & 0xff) << 8));
                break;
        }
    }

    // 0xc7
    private void MOV_EW_IW() {
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3);
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        int ptr;
        short imm16;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                imm16 = fetchW();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                mem.pokeW(ptr, imm16);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                imm16 = fetchW();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                mem.pokeW(ptr, imm16);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                imm16 = fetchW();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                mem.pokeW(ptr, imm16);
                break;
            case 3:
                imm16 = fetchW();
                logger.log(Level.TRACE, "MOV EW,$%04x".formatted(imm16));
                regs.eRegs[reg] = imm16;
                break;
        }
    }

    // 0xcd
    private void INT_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "INT $%02x".formatted(imm8 & 0xff));
        dos.int_(imm8);
    }

    // 0xcf
    private void IRET() {
        logger.log(Level.TRACE, "IRET");
        regs.ip = mem.peekW(regs.getSS_SP());
        regs.addSP(2);
        regs.setCS(mem.peekW(regs.getSS_SP()));
        regs.addSP(2);
        regs.flag = mem.peekW(regs.getSS_SP());
        regs.addSP(2);
    }

    // 0xd0
    private void GRP2_EB_1() {
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        byte ans = 0;
        byte uans = 0;

        byte eb = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        switch (reg) {
            case 0: // ROL
                logger.log(Level.TRACE, "ROL EB,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = eb;
                uans = (byte) (((uans & 0xff) << 1) | ((uans & 0x80) == 0 ? 0 : 1));
                ans = uans;
                regs.setCF((uans & 0x01) != 0);
                // regs.OF = (ans & 0x8000) != 0;
                break;
            case 1: // ROR
                logger.log(Level.TRACE, "ROR EB,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = eb;
                uans = (byte) (((uans & 0xff) >> 1) | ((uans & 0x01) == 0 ? 0 : 0x80));
                ans = uans;
                regs.setCF((uans & 0x80) != 0);
                // regs.OF = (ans & 0x8000) != 0;
                break;
            case 2: // RCL
                throw new UnsupportedOperationException();
            case 3: // RCR
                logger.log(Level.TRACE, "RCR EB,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = eb;
                boolean newCF = (uans & 1) != 0;
                uans = (byte) (((uans & 0xff) >> 1) | (regs.isCF() ? 0x80 : 0x00));
                ans = uans;
                regs.setCF(newCF);
                break;
            case 4: // SHL
            case 6: // same SHL
                logger.log(Level.TRACE, "SHL EB,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = eb;
                regs.setCF((uans & 0x80) != 0);
                uans <<= 1;
                ans = uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.setSZPFb(uans);
                regs.setAF(true); // TBD
                break;
            case 5: // SHR
                logger.log(Level.TRACE, "SHR EB,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = eb;
                regs.setCF((uans & 0x01) != 0);
                uans >>>= 1;
                ans = uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.setSZPFb(uans);
                regs.setAF(true); // TBD
                break;
            case 7: // SAR
                logger.log(Level.TRACE, "SAR EB,1 modrw:$%02x".formatted(modRw & 0xffff));
                ans = eb;
                regs.setCF((ans & 0x01) != 0);
                ans >>>= 1;
                uans = ans;
                regs.setOF(false);
                regs.setSZPFb(uans);
                regs.setAF(true); // TBD
                break;
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeB(ptr, uans);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (uans & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((uans & 0xff) << 8));
                break;
        }
    }

    // 0xd1
    private void GRP2_EW_1() {
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        short ans;
        short uans = 0;

        int ptr = 0;
        short ew = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }

        boolean newCF;
        switch (reg) {
            case 0: // ROL
                logger.log(Level.TRACE, "ROL EW,1 modrw:$%02x".formatted(modRw & 0xffff));

                uans = ew;
                regs.setCF((uans & 0x8000) != 0);
                uans = (short) (((uans & 0xffff) << 1) | (regs.isCF() ? 1 : 0));
                ans = uans;

                regs.setOF((ans & 0x8000) != 0);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
            case 1: // ROR
                logger.log(Level.TRACE, "ROR EW,1 modrw:$%02x".formatted(modRw & 0xffff));

                uans = ew;
                regs.setCF((uans & 0x0001) != 0);
                uans = (short) (((uans & 0xffff) >> 1) | (regs.isCF() ? 1 : 0));
                ans = uans;
                regs.setOF((ans & 0x0001) != 0);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
            case 2: // RCL
                logger.log(Level.TRACE, "RCL EW,1 modrw:$%02x".formatted(modRw & 0xffff));

                uans = ew;
                newCF = (uans & 0x8000) != 0;
                uans = (short) (((ew & 0xffff) << 1) | (regs.isCF() ? 1 : 0));
                regs.setCF(newCF);
                ans = uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
            case 3: // RCR
                logger.log(Level.TRACE, "RCR EW,1 modrw:$%02x".formatted(modRw & 0xffff));

                uans = ew;
                newCF = (uans & 0x0001) != 0;
                uans = (short) (((ew & 0xffff) >> 1) | (regs.isCF() ? 0x8000 : 0x000));
                regs.setCF(newCF);
                ans = uans;
                regs.setOF((ans & 0x0001) != 0);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
            case 4: // SHL
            case 6: // same SHL
                logger.log(Level.TRACE, "SHL EW,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = ew;
                regs.setCF((uans & 0x8000) != 0);
                uans <<= 1;
                ans = uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
            case 5: // SHR
                logger.log(Level.TRACE, "SHR EW,1 modrw:$%02x".formatted(modRw & 0xffff));
                uans = ew;
                regs.setCF((uans & 0x0001) != 0);
                uans >>>= 1;
                ans = uans;
                regs.setOF((ans & 0x8000) != 0);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
            case 7: // SAR
                logger.log(Level.TRACE, "SAR EW,1 modrw:$%02x".formatted(modRw & 0xffff));
                ans = ew;
                regs.setCF((ans & 0x01) != 0);
                ans >>>= 1;
                uans = ans;
                regs.setOF(false);
                regs.setSZPFw(ans);
                regs.setAF(true); // TBD
                break;
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeW(ptr, uans);
                break;
            case 3:
                regs.eRegs[rm] = uans; // (regs.eRegs[rm] & 0xff00) | uans);
                break;
        }
    }

    // 0xc0 or 0xd2
    private void GRP2_EB_CL(byte op) {
        byte modRw = fetch();
        logger.log(Level.TRACE, "GRP2_EB_CL op:$%02x modrw:$%02x".formatted(op & 0xff, modRw & 0xff));
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte ans = 0;
        byte uans = 0;
        byte eb = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        int count = (op == (byte) 0xd2 ? regs.getCL() : fetch()) & 0x1f;
        if (count == 0) return;

        boolean newCF;
        if (count == 1) {
            switch (reg) {
                case 0: // ROL
                    uans = eb;
                    newCF = (uans & 0x80) != 0;
                    uans = (byte) (((uans & 0xff) << 1) | ((uans & 0x80) != 0 ? 1 : 0));
                    ans = uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 1: // ROR
                    uans = eb;
                    uans = (byte) (((uans & 0xff) >> 1) | ((uans & 0x01) == 0 ? 0 : 0x80));
                    ans = uans;
                    regs.setCF((uans & 0x80) != 0);
                    regs.setOF((uans & 0x80) != 0);
                    break;
                case 2: // RCL
                    uans = eb;
                    newCF = (uans & 0x80) != 0;
                    uans = (byte) (((uans & 0xff) << 1) | (regs.isCF() ? 1 : 0));
                    ans = uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL
                    uans = eb;
                    regs.setCF((uans & 0x80) != 0);
                    uans <<= 1;
                    regs.setSZPFb(uans);
                    break;
                case 5: // SHR
                    uans = eb;
                    regs.setCF((uans & 1) != 0);
                    uans >>>= 1;
                    regs.setSZPFb(uans);
                    break;
                case 6: // (SMO)
                    throw new UnsupportedOperationException();
                case 7: // SAR
                    int ians = eb;
                    regs.setCF((ians & 1) != 0);
                    ians >>>= 1;
                    uans = (byte) ians;
                    regs.setSZPFb(uans);
                    regs.setOF(false);
                    break;
            }
        } else {
            switch (reg) {
                case 0: // ROL
                    uans = eb;
                    newCF = (uans & (0x100 >> count)) != 0;
                    uans = (byte) (((uans & 0xff) << count) | ((uans & 0xff) >> (8 - count)));
                    ans = uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 1: // ROR
                    throw new UnsupportedOperationException();
                case 2: // RCL
                    uans = eb;
                    byte v = (byte) ((uans & 0xff) & (0xff << count));
                    newCF = ((byte) ((uans & 0xff) & (0x100 >> count)) != 0);
                    uans = (byte) (((uans & 0xff) << count) | ((regs.isCF() ? 1 : 0) << (count - 1)) | ((v & 0xff) >> (9 - count)));
                    ans = uans;
                    regs.setCF(newCF);
                    regs.setOF(newCF);
                    break;
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL Logical shift
                    uans = eb;
                    regs.setCF(((uans & 0xff) & (0x80 >> (count - 1))) != 0);
                    uans <<= count;
                    regs.setSZPFb(uans);
                    break;
                case 5: // SHR Logical Shift
                    uans = eb;
                    regs.setCF(((uans & 0xff) & (1 << (count - 1))) != 0);
                    uans >>>= count;
                    regs.setSZPFb(uans);
                    break;
                case 6: // (SMO)
                    throw new UnsupportedOperationException();
                case 7: // SAR Arithmetic shift
                    int ians = eb & 0xff;
                    regs.setCF((ians & (1 << (count - 1))) != 0);
                    ians >>>= count;
                    uans = (byte) ians;
                    regs.setSZPFb(uans);
                    break;
            }
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeB(ptr, uans);
                break;
            case 3:
                if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (uans & 0xff));
                else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((uans & 0xff) << 8));
                break;
        }
    }

    // 0xc1 or 0xd3
    private void GRP2_EW_CL(byte op) {
        byte modRw = fetch();
        logger.log(Level.TRACE, "GRP2_EW_CL op:$%02x modrw:$%02x".formatted(op & 0xff, modRw & 0xff));
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;
        short ans = 0;
        short uans = 0;

        int ptr = 0;
        short ew = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }

        int count = (op == (byte) 0xd3 ? regs.getCL() : fetch()) & 0x1f;
        if (count == 0) return;

        if (count == 1) {
            switch (reg) {
                case 0: // ROL
                    throw new UnsupportedOperationException();
                case 1: // ROR
                    uans = ew;
                    uans = (short) (((uans & 0xffff) >> 1) | ((uans & 0x01) == 0 ? 0 : 0x8000));
                    ans = uans;
                    regs.setCF((uans & 0x8000) != 0);
                    regs.setOF((uans & 0x8000) != 0);
                    break;
                case 2: // RCL
                    throw new UnsupportedOperationException();
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL
                case 6: // Same SHL
                    uans = ew;
                    regs.setCF((ans & 0x8000) != 0);
                    uans <<= 1;
                    ans = uans;
                    uans = ans;
                    regs.setOF((ans & 0x8000) != 0);
                    regs.setSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 5: // SHR
                    uans = ew;
                    regs.setCF((ans & 0x01) != 0);
                    uans >>>= 1;
                    ans = uans;
                    uans = ans;
                    regs.setOF((ans & 0x8000) != 0);
                    regs.setSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 7: // SAR
                    ans = ew;
                    regs.setCF((ans & 0x01) != 0);
                    ans >>>= 1;
                    uans = ans;
                    regs.setOF(false);
                    regs.setSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
            }
        } else {
            switch (reg) {
                case 0: // ROL
                    throw new UnsupportedOperationException();
                case 1: // ROR
                    uans = ew;
                    uans = (short) (((uans & 0xffff) >> count) | ((uans & 0xffff) << (16 - count)));
                    ans = uans;
                    regs.setCF((uans & 0x8000) != 0);
                    regs.setOF((uans & 0x8000) != 0);
                    break;
                case 2: // RCL
                    throw new UnsupportedOperationException();
                case 3: // RCR
                    throw new UnsupportedOperationException();
                case 4: // SHL
                case 6: // Same SHL
                    uans = ew;
                    uans <<= count - 1;
                    regs.setCF((uans & 0x8000) != 0);
                    uans <<= 1;
                    ans = uans;
                    regs.setSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 5: // SHR (logical shift right)
                    uans = ew;
                    uans >>>= count - 1;
                    regs.setCF((uans & 0x01) != 0);
                    uans >>>= 1;
                    ans = uans;
                    regs.setSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
                case 7: // SAR
                    ans = ew;
                    ans >>>= count - 1;
                    regs.setCF((ans & 0x01) != 0);
                    ans >>>= 1;
                    uans = ans;
                    regs.setSZPFw(uans);
                    regs.setAF(true); // TBD
                    break;
            }
        }

        switch (mod) {
            case 0:
            case 1:
            case 2:
                mem.pokeW(ptr, uans);
                break;
            case 3:
                regs.eRegs[rm] = uans;
                break;
        }
    }

    // 0xd4
    private void AAM() {
        byte imm8 = fetch();
        byte a = regs.getAL();
        regs.setAH((byte) ((a & 0xff) / (imm8 & 0xff)));
        regs.setAL((byte) ((a & 0xff) % (imm8 & 0xff)));
        regs.setSZPFw(regs.getAX());
        regs.setAF(true); // TBD
    }

    // 0xd5
    private void AAD() {
        byte imm8 = fetch();
        if (imm8 != 0x0a) {
            throw new UnsupportedOperationException();
        }

        regs.setAL((byte) ((regs.getAH() & 0xff) * 0x0a + (regs.getAL() & 0xff)));
        regs.setAH((short) 0);
        regs.setSZPFb(regs.getAL());
        regs.setAF(true); // TBD
    }

    // 0xe8
    private void CALL_near() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "CALL near $%04x".formatted(imm16 & 0xffff));
        regs.subSP(2);
        mem.pokeW(regs.getSS_SP(), regs.ip);
        regs.ip = (short) ((regs.ip & 0xffff) + (imm16 & 0xffff));
    }

    // 0xe2
    private void LOOP_short() {
        byte imm8 = fetch();
        if ((regs.getCX() & 0xffff) < 100) logger.log(Level.TRACE, "LOOP short $%02x CX:$%04x".formatted(imm8 & 0xff, regs.getCX() & 0xffff));

        regs.decCX();
        if (regs.getCX() != 0) {
            regs.ip = (short) ((regs.ip & 0xffff) + imm8);
        }
    }

    // 0xe4
    private void IN_AL_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "IN AL,$%02x".formatted(imm8 & 0xff));
        regs.setAL(machine.inpB((short) (imm8 & 0xff)));
    }

    // 0xe5
    private void IN_AX_IB() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "IN AX,$%02x".formatted(imm8 & 0xff));
        regs.setAX(machine.inpW((short) (imm8 & 0xff)));
    }

    // 0xe6
    private void OUT_IB_AL() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "OUT $%02x,AL".formatted(imm8 & 0xff));
        machine.outpB((short) (imm8 & 0xff), regs.getAL());
    }

    // 0xe7
    private void OUT_IB_AX() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "OUT $%02x,AX".formatted(imm8 & 0xff));
        machine.outpW((short) (imm8 & 0xff), regs.getAX());
    }

    // 0xe9
    private void JMP_near() {
        short imm16 = fetchW();
        logger.log(Level.TRACE, "JMP near $%04x".formatted(imm16 & 0xffff));

        regs.ip = (short) ((regs.ip & 0xffff) + (imm16 & 0xffff));
    }

    // 0xeb
    private void JMP_short() {
        byte imm8 = fetch();
        logger.log(Level.TRACE, "JMP short $%02x".formatted(imm8 & 0xff));

        regs.ip = (short) ((regs.ip & 0xffff) + imm8);
    }

    // 0xec
    private void IN_AL_DX() {
        logger.log(Level.TRACE, "IN AL,DX");
        regs.setAL(machine.inpB(regs.getDX()));
    }

    // 0xed
    private void IN_AX_DX() {
        logger.log(Level.TRACE, "IN AX,DX");
        regs.setAX(machine.inpW(regs.getDX()));
    }

    // 0xee
    private void OUT_DX_AL() {
        logger.log(Level.TRACE, "OUT DX,AL");
        machine.outpB(regs.getDX(), regs.getAL());
    }

    // 0xef
    private void OUT_DX_AX() {
        logger.log(Level.TRACE, "OUT DX,AX");
        machine.outpW(regs.getDX(), regs.getAX());
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
        byte modRw = fetch();
        logger.log(Level.TRACE, "GRP3B modrw:$%02x".formatted(modRw));
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte eb = 0;
        int ptr = 0;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        byte ib;
        byte ans = 0;
        switch (reg) {
            case 0: // TEST EB,IB
                ib = fetch();
                ans = (byte) (eb & (ib & 0xff));
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 1: // ?
                throw new UnsupportedOperationException();
            case 2: // NOT EB
                ans = (byte) (~eb);
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 3: // NEG EB
                ans = (byte) (-eb);
                regs.setSZPFb(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 4: // MUL EB
                int mans = (regs.getAL() & 0xff) * (eb & 0xff);
                regs.setSZPFb(eb);
                regs.setAX((short) (mans & 0xffff));
                regs.setZF(regs.getAX() == 0);
                regs.setOF(regs.getAH() != 0);
                regs.setCF(regs.isOF());
                break;
            case 5: // IMUL EB
                throw new UnsupportedOperationException();
            case 6: // DIV EB
                ans = (byte) ((regs.getAX() & 0xffff) / (eb & 0xff));
                mod = (byte) ((regs.getAX() & 0xffff) % (eb & 0xff));
                regs.setAL(ans);
                regs.setAH((short) mod);
                break;
            case 7: // IDIV EB
                throw new UnsupportedOperationException();
        }

        if (reg != 0 && reg < 4) {
            switch (mod) {
                case 0:
                case 1:
                case 2:
                    mem.pokeB(ptr, ans);
                    break;
                case 3:
                    if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                    else regs.eRegs[rm - 4] = (short) (((ans & 0xff) << 8) | (regs.eRegs[rm - 4] & 0xff));
                    break;
            }
        }
    }

    // 0xf7
    private void GRP3W() {
        byte modRw = fetch();
        logger.log(Level.TRACE, "GRP3W modrw:$%02x".formatted(modRw & 0xff));
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short ew = 0;
        int ptr = 0;
        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }

        short iw;
        short ans = 0;
        switch (reg) {
            case 0: // TEST EW,IW
                iw = fetchW();
                ans = (short) ((ew & 0xffff) & (iw & 0xffff));
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 1: // ?
                throw new UnsupportedOperationException();
            case 2: // NOT EW
                ans = (short) (~(ew & 0xffff));
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 3: // NEG EW
                ans = (short) (-(ew & 0xffff));
                regs.setSZPFw(ans);
                regs.setOF(false);
                regs.setCF(false);
                regs.setAF(false); // TBD
                break;
            case 4: // MUL EW
                int ans32 = (regs.getAX() & 0xffff) * (ew & 0xffff);
                regs.setAX((short) (ans32 & 0xffff));
                regs.setDX((short) ((ans32 & 0xffff_0000) >> 16));
                break;
            case 5: // IMUL EW
                int ians32 = regs.getAX() * ew;
                regs.setAX((short) (ians32 & 0xffff));
                regs.setDX((short) ((ians32 & 0xffff_0000) >> 16));
                break;
            case 6: // DIV EW
                int ans32d = (((regs.getDX() & 0xffff) << 16) + (regs.getAX() & 0xffff)) / (ew & 0xffff);
                int modud = (((regs.getDX() & 0xffff) << 16) + (regs.getAX() & 0xffff)) % (ew & 0xffff);
                regs.setAX((short) ans32d);
                regs.setDX((short) modud);
                break;
            case 7: // IDIV EB
                int ians32d = (((regs.getDX() & 0xffff) << 16) + (regs.getAX() & 0xffff)) / (ew & 0xffff);
                int modu = (((regs.getDX() & 0xffff) << 16) + (regs.getAX() & 0xffff)) % (ew & 0xffff);
                regs.setAX((short) ians32d);
                regs.setDX((short) modu);
                break;
        }

        if (reg != 0 && reg < 4) {
            switch (mod) {
                // case 0:
                case 1:
                case 2:
                    mem.pokeW(ptr, ans);
                    break;
                case 3:
                    regs.eRegs[rm] = ans;
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
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        byte eb = 0;
        int ptr = 0;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                eb = mem.peekB(ptr);
                break;
            case 3:
                if (rm < 4) eb = (byte) (regs.eRegs[rm] & 0x00ff);
                else eb = (byte) ((regs.eRegs[rm - 4] & 0xff00) >> 8);
                break;
        }

        byte ib = 1;
        short ians;
        byte ans;

        switch (reg) {
            case 0: // INC EB
                logger.log(Level.TRACE, "INC EB");
                ians = (short) (eb + (ib & 0xff));
                ans = (byte) ians;
                regs.setSZPFb(ans);
                regs.setOFbAdd(eb, ib, ans);
                regs.setCFb(ians);
                regs.setAF(eb, ib, ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
                        break;
                }
                break;
            case 1: // DEC EB
                logger.log(Level.TRACE, "DEC EB");
                ians = (short) (eb - (ib & 0xff));
                ans = (byte) ians;
                regs.setSZPFb(ans);
                regs.setOFbSub(eb, ib, ans);
                regs.setCFb(ians);
                regs.setAF(eb, ib, ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeB(ptr, ans);
                        break;
                    case 3:
                        if (rm < 4) regs.eRegs[rm] = (short) ((regs.eRegs[rm] & 0xff00) | (ans & 0xff));
                        else regs.eRegs[rm - 4] = (short) ((regs.eRegs[rm - 4] & 0xff) | ((ans & 0xff) << 8));
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
        byte modRw = fetch();
        byte reg = (byte) ((modRw & 0x38) >> 3); // For segment registers, ignore bit 5
        byte rm = (byte) (modRw & 7);
        int mod = (modRw & 0xff) >> 6;

        short ew = 0;
        int ptr = 0;

        boolean bSegPrefSw = segPrefSw;
        int bSegPref = segPref;

        switch (mod) {
            case 0:
                ptr = getMod00RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 1:
                ptr = getMod01RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 2:
                ptr = getMod02RwAdr(rm, false);
                ew = mem.peekW(ptr);
                break;
            case 3:
                ew = regs.eRegs[rm];
                break;
        }

        short iw = 1;
        short ians;
        short ans;

        switch (reg) {
            case 0: // INC EW
                logger.log(Level.TRACE, "INC EW");
                ians = (short) ((ew & 0xffff) + (iw & 0xffff));
                ans = ians;
                regs.setSZPFw(ans);
                regs.setOFwAdd(ew, iw, ans);
                regs.setCFw(ew + iw);
                regs.setAF((byte) ew, (byte) iw, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ians);
                        break;
                    case 3:
                        regs.eRegs[rm] = ians;
                        break;
                }
                break;
            case 1: // DEC EW
                logger.log(Level.TRACE, "DEC EW");
                ians = (short) ((ew & 0xffff) - (iw & 0xffff));
                ans = ians;
                regs.setSZPFw(ans);
                regs.setOFwSub(ew, iw, ans);
                regs.setCFw(ew - iw);
                regs.setAF((byte) ew, (byte) iw, (byte) ans);
                switch (mod) {
                    case 0:
                    case 1:
                    case 2:
                        mem.pokeW(ptr, ians);
                        break;
                    case 3:
                        regs.eRegs[rm] = ians;
                        break;
                }
                break;
            case 2: // CALL EW
                logger.log(Level.TRACE, "CALL EW");
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.ip);
                regs.ip = ew;
                break;
            case 3: // CALL EP
                logger.log(Level.TRACE, "CALL EP");

                segPrefSw = bSegPrefSw;
                segPref = bSegPref;
                int ptr2;
                short seg;
                ptr2 = ptr + 2;
                seg = mem.peekW(ptr2);

                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.getCS());
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), regs.ip);
                regs.ip = ew;
                regs.setCS(seg);
                break;
            case 4: // JMP EW
                logger.log(Level.TRACE, "JMP EW");
                regs.ip = ew;
                break;
            case 5: //
                throw new UnsupportedOperationException();
            case 6: // PUSH EW
                logger.log(Level.TRACE, "PUSH EW");
                regs.subSP(2);
                mem.pokeW(regs.getSS_SP(), ew);
                break;
            case 7: // ?
                throw new UnsupportedOperationException();
        }
    }
}
