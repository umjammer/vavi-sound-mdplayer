package mdplayer.driver.gbs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;


public class IO {

    private static final Logger logger = System.getLogger(IO.class.getName());

    // FF00-FF7F   I/O Ports
    private final Consumer<Integer>[] iow;
    private final Supplier<Integer>[] ior;
    private final BiConsumer<Integer, Integer> writeDmg;
    private final IntFunction<Integer> readDmg;

    private static final int NR10 = 0x00;
    private static final int NR11 = 0x01;
    private static final int NR12 = 0x02;
    private static final int NR13 = 0x03;
    private static final int NR14 = 0x04;
    // 0x05
    private static final int NR21 = 0x06;
    private static final int NR22 = 0x07;
    private static final int NR23 = 0x08;
    private static final int NR24 = 0x09;
    private static final int NR30 = 0x0A;
    private static final int NR31 = 0x0B;
    private static final int NR32 = 0x0C;
    private static final int NR33 = 0x0D;
    private static final int NR34 = 0x0E;
    // 0x0F
    private static final int NR41 = 0x10;
    private static final int NR42 = 0x11;
    private static final int NR43 = 0x12;
    private static final int NR44 = 0x13;
    private static final int NR50 = 0x14;
    private static final int NR51 = 0x15;
    private static final int NR52 = 0x16;
    // 0x17 - 0x1F
    private static final int AUD3W0 = 0x20;
    private static final int AUD3W1 = 0x21;
    private static final int AUD3W2 = 0x22;
    private static final int AUD3W3 = 0x23;
    private static final int AUD3W4 = 0x24;
    private static final int AUD3W5 = 0x25;
    private static final int AUD3W6 = 0x26;
    private static final int AUD3W7 = 0x27;
    private static final int AUD3W8 = 0x28;
    private static final int AUD3W9 = 0x29;
    private static final int AUD3WA = 0x2A;
    private static final int AUD3WB = 0x2B;
    private static final int AUD3WC = 0x2C;
    private static final int AUD3WD = 0x2D;
    private static final int AUD3WE = 0x2E;
    private static final int AUD3WF = 0x2F;

    //http://bgb.bircd.org/pandocs.htm#soundcontroller

    @SuppressWarnings("unchecked")
    public IO(BiConsumer<Integer, Integer> writeDmg, IntFunction<Integer> readDmg) {
        this.writeDmg = writeDmg;
        this.readDmg = readDmg;

        ior = new Supplier[128];
        //0x00
        ior[0x00] = null;
        ior[0x01] = null;
        ior[0x02] = null;
        ior[0x03] = null;
        ior[0x04] = null;
        ior[0x05] = null;
        ior[0x06] = null;
        ior[0x07] = null;
        //0x08
        ior[0x08] = null;
        ior[0x09] = null;
        ior[0x0A] = null;
        ior[0x0B] = null;
        ior[0x0C] = null;
        ior[0x0D] = null;
        ior[0x0E] = null;
        ior[0x0F] = null;
        //0x10
        ior[0x10] = this::r_FF10_NR10_Channel_1_Sweep_register;
        ior[0x11] = this::r_FF11_NR11_Channel_1_Sound_length_Wave_pattern_duty;
        ior[0x12] = this::r_FF12_NR12_Channel_1_Volume_Envelope;
        ior[0x13] = null;
        ior[0x14] = this::r_FF14_NR14_Channel_1_Frequency_hi;
        ior[0x15] = null;
        ior[0x16] = this::r_FF16_NR21_Channel_2_Sound_Length_Wave_Pattern_Duty;
        ior[0x17] = this::r_FF17_NR22_Channel_2_Volume_Envelope;
        //0x18
        ior[0x18] = null;
        ior[0x19] = this::r_FF19_NR24_Channel_2_Frequency_hi_data;
        ior[0x1A] = this::r_FF1A_NR30_Channel_3_Sound_on_off;
        ior[0x1B] = this::r_FF1B_NR31_Channel_3_Sound_Length;
        ior[0x1C] = this::r_FF1C_NR32_Channel_3_Select_output_level;
        ior[0x1D] = null;
        ior[0x1E] = this::r_FF1E_NR34_Channel_3_Frequencys_higher_data;
        ior[0x1F] = null;
        //0x20
        ior[0x20] = this::r_FF20_NR41_Channel_4_Sound_Length;
        ior[0x21] = this::r_FF21_NR42_Channel_4_Volume_Envelope;
        ior[0x22] = this::r_FF22_NR43_Channel_4_Polynomial_Counter;
        ior[0x23] = this::r_FF23_NR44_Channel_4_Counter_consecutive_Initial;
        ior[0x24] = this::r_FF24_NR50_Channel_control_ON_OFF_Volume;
        ior[0x25] = this::r_FF25_NR51_SelectionOfSoundOutputTerminal;
        ior[0x26] = this::r_FF26_NR52_Sound_on_off;
        ior[0x27] = null;
        //0x28
        for (int i = 0x28; i < 0x30; i++) ior[i] = null;
        //0x30
        ior[0x30] = this::r_FF30_Wave_Pattern_RAM;
        ior[0x31] = this::r_FF31_Wave_Pattern_RAM;
        ior[0x32] = this::r_FF32_Wave_Pattern_RAM;
        ior[0x33] = this::r_FF33_Wave_Pattern_RAM;
        ior[0x34] = this::r_FF34_Wave_Pattern_RAM;
        ior[0x35] = this::r_FF35_Wave_Pattern_RAM;
        ior[0x36] = this::r_FF36_Wave_Pattern_RAM;
        ior[0x37] = this::r_FF37_Wave_Pattern_RAM;
        //0x38
        ior[0x38] = this::r_FF38_Wave_Pattern_RAM;
        ior[0x39] = this::r_FF39_Wave_Pattern_RAM;
        ior[0x3A] = this::r_FF3A_Wave_Pattern_RAM;
        ior[0x3B] = this::r_FF3B_Wave_Pattern_RAM;
        ior[0x3C] = this::r_FF3C_Wave_Pattern_RAM;
        ior[0x3D] = this::r_FF3D_Wave_Pattern_RAM;
        ior[0x3E] = this::r_FF3E_Wave_Pattern_RAM;
        ior[0x3F] = this::r_FF3F_Wave_Pattern_RAM;
        //0x40-0x7F
        for (int i = 0x40; i < 0x80; i++) ior[i] = null;

        iow = new Consumer[128];
        //0x00
        iow[0x00] = null;
        iow[0x01] = null;
        iow[0x02] = null;
        iow[0x03] = null;
        iow[0x04] = null;
        iow[0x05] = null;
        iow[0x06] = null;
        iow[0x07] = this::w_FF07_TAC_Timer_Control;
        //0x08
        iow[0x08] = null;
        iow[0x09] = null;
        iow[0x0A] = null;
        iow[0x0B] = null;
        iow[0x0C] = null;
        iow[0x0D] = null;
        iow[0x0E] = null;
        iow[0x0F] = null;
        //0x10
        iow[0x10] = this::w_FF10_NR10_Channel_1_Sweep_register;
        iow[0x11] = this::w_FF11_NR11_Channel_1_Sound_length_Wave_pattern_duty;
        iow[0x12] = this::w_FF12_NR12_Channel_1_Volume_Envelope;
        iow[0x13] = this::w_FF13_NR13_Channel_1_Frequency_lo;
        iow[0x14] = this::w_FF14_NR14_Channel_1_Frequency_hi;
        iow[0x15] = null;
        iow[0x16] = this::w_FF16_NR21_Channel_2_Sound_length_Wave_pattern_duty;
        iow[0x17] = this::w_FF17_NR22_Channel_2_Volume_Envelope;
        //0x18
        iow[0x18] = this::w_FF18_NR23_Channel_2_Frequency_lo;
        iow[0x19] = this::w_FF19_NR24_Channel_2_Frequency_hi;
        iow[0x1A] = this::w_FF1A_NR30_Channel_3_Sound_on_off;
        iow[0x1B] = this::w_FF1B_NR31_Channel_3_Sound_Length;
        iow[0x1C] = this::w_FF1C_NR32_Channel_3_Select_output_level;
        iow[0x1D] = this::w_FF1D_NR33_Channel_3_Frequencys_lower_data;
        iow[0x1E] = this::w_FF1E_NR34_Channel_3_Frequencys_higher_data;
        iow[0x1F] = null;
        //0x20
        iow[0x20] = this::w_FF20_NR41_Channel_4_Sound_Length;
        iow[0x21] = this::w_FF21_NR42_Channel_4_Volume_Envelope;
        iow[0x22] = this::w_FF22_NR43_Channel_4_Polynomial_Counter;
        iow[0x23] = this::w_FF23_NR44_Channel_4_Counter_consecutive_Initial;
        iow[0x24] = this::w_FF24_NR50_ChannelControl;
        iow[0x25] = this::w_FF25_NR51_SelectionOfSoundOutputTerminal;
        iow[0x26] = this::w_FF26_NR52_SoundOnOff;
        iow[0x27] = null;
        //0x28
        for (int i = 0x28; i < 0x30; i++) iow[i] = null;
        //0x30
        iow[0x30] = this::w_FF30_Wave_Pattern_RAM;
        iow[0x31] = this::w_FF31_Wave_Pattern_RAM;
        iow[0x32] = this::w_FF32_Wave_Pattern_RAM;
        iow[0x33] = this::w_FF33_Wave_Pattern_RAM;
        iow[0x34] = this::w_FF34_Wave_Pattern_RAM;
        iow[0x35] = this::w_FF35_Wave_Pattern_RAM;
        iow[0x36] = this::w_FF36_Wave_Pattern_RAM;
        iow[0x37] = this::w_FF37_Wave_Pattern_RAM;
        //0x38
        iow[0x38] = this::w_FF38_Wave_Pattern_RAM;
        iow[0x39] = this::w_FF39_Wave_Pattern_RAM;
        iow[0x3A] = this::w_FF3A_Wave_Pattern_RAM;
        iow[0x3B] = this::w_FF3B_Wave_Pattern_RAM;
        iow[0x3C] = this::w_FF3C_Wave_Pattern_RAM;
        iow[0x3D] = this::w_FF3D_Wave_Pattern_RAM;
        iow[0x3E] = this::w_FF3E_Wave_Pattern_RAM;
        iow[0x3F] = this::w_FF3F_Wave_Pattern_RAM;
        //0x40-0x7F
        for (int i = 0x40; i < 0x80; i++) iow[i] = null;
    }

    public void write(int pc, int dat) {
        int index = pc & 0xFF;
        if (iow[index] != null) iow[index].accept(dat);
        else {
            logger.log(Level.TRACE, "Write unknown IO Address:$%04X dat:$%02X".formatted((pc & 0xFF) + 0xff00da, dat));
            throw new UnsupportedOperationException();
        }
    }

    public int read(int pc) {
        int index = pc & 0xFF;
        if (ior[index] != null) return ior[index].get();
        else {
            logger.log(Level.TRACE, "Read unknown IO Address:$%04X".formatted((pc & 0xFF) + 0xff00));
            throw new UnsupportedOperationException();
        }
    }

    // Reference
    // http://bgb.bircd.org/pandocs.htm#soundcontroller

    private void w_FF07_TAC_Timer_Control(int dat) {
        logger.log(Level.TRACE, "Write FF07 - TAC - Timer Control (R/W) $%02X".formatted(dat));
    }

    private int r_FF10_NR10_Channel_1_Sweep_register() {
        int dat = readDmg.apply(NR10);
        logger.log(Level.TRACE, "Read FF10 - NR10 - Channel 1 Sweep register (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF10_NR10_Channel_1_Sweep_register(int dat) {
        writeDmg.accept(NR10, dat);
        logger.log(Level.TRACE, "Write FF10 - NR10 - Channel 1 Sweep register (R/W) $%02X".formatted(dat));
    }

    private int r_FF11_NR11_Channel_1_Sound_length_Wave_pattern_duty() {
        int dat = readDmg.apply(NR11);
        logger.log(Level.TRACE, "Read FF11 - NR11 - Channel 1 Sound length/Wave pattern duty (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF11_NR11_Channel_1_Sound_length_Wave_pattern_duty(int dat) {
        writeDmg.accept(NR11, dat);
        logger.log(Level.TRACE, "Write FF11 - NR11 - Channel 1 Sound length/Wave pattern duty (R/W) $%02X".formatted(dat));
    }

    private int r_FF12_NR12_Channel_1_Volume_Envelope() {
        int dat = readDmg.apply(NR12);
        logger.log(Level.TRACE, "Read FF12 - NR12 - Channel 1 Volume Envelope (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF12_NR12_Channel_1_Volume_Envelope(int dat) {
        writeDmg.accept(NR12, dat);
        logger.log(Level.TRACE, "Write FF12 - NR12 - Channel 1 Volume Envelope (R/W) $%02X".formatted(dat));
    }

    private void w_FF13_NR13_Channel_1_Frequency_lo(int dat) {
        writeDmg.accept(NR13, dat);
        logger.log(Level.TRACE, "Write FF13 - NR13 - Channel 1 Frequency lo (W) $%02X".formatted(dat));
    }

    private int r_FF14_NR14_Channel_1_Frequency_hi() {
        int dat = readDmg.apply(NR14);
        logger.log(Level.TRACE, "Read FF14 - NR14 - Channel 1 Frequency hi (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF14_NR14_Channel_1_Frequency_hi(int dat) {
        writeDmg.accept(NR14, dat);
        logger.log(Level.TRACE, "Write FF14 - NR14 - Channel 1 Frequency hi (R/W) $%02X".formatted(dat));
    }

    private int r_FF16_NR21_Channel_2_Sound_Length_Wave_Pattern_Duty() {
        int dat = readDmg.apply(NR21);
        logger.log(Level.TRACE, "Read FF16 - NR21 - Channel 2 Sound Length/Wave Pattern Duty (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF16_NR21_Channel_2_Sound_length_Wave_pattern_duty(int dat) {
        writeDmg.accept(NR21, dat);
        logger.log(Level.TRACE, "Write FF16 - NR21 - Channel 2 Sound Length/Wave Pattern Duty (R/W) $%02X".formatted(dat));
    }

    private int r_FF17_NR22_Channel_2_Volume_Envelope() {
        int dat = readDmg.apply(NR22);
        logger.log(Level.TRACE, "Read FF17 - NR22 - Channel 2 Volume Envelope (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF17_NR22_Channel_2_Volume_Envelope(int dat) {
        writeDmg.accept(NR22, dat);
        logger.log(Level.TRACE, "Write FF17 - NR22 - Channel 2 Volume Envelope (R/W) $%02X".formatted(dat));
    }

    private void w_FF18_NR23_Channel_2_Frequency_lo(int dat) {
        writeDmg.accept(NR23, dat);
        logger.log(Level.TRACE, "Write FF18 - NR23 - Channel 2 Frequency lo data (W) $%02X".formatted(dat));
    }

    private int r_FF19_NR24_Channel_2_Frequency_hi_data() {
        int dat = readDmg.apply(NR24);
        logger.log(Level.TRACE, "Read FF19 - NR24 - Channel 2 Frequency hi data (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF19_NR24_Channel_2_Frequency_hi(int dat) {
        writeDmg.accept(NR24, dat);
        logger.log(Level.TRACE, "Write FF19 - NR24 - Channel 2 Frequency hi data (R/W) $%02X".formatted(dat));
    }

    private int r_FF1A_NR30_Channel_3_Sound_on_off() {
        int dat = readDmg.apply(NR30);
        logger.log(Level.TRACE, "Read FF1A - NR30 - Channel 3 Sound on/off (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF1A_NR30_Channel_3_Sound_on_off(int dat) {
        writeDmg.accept(NR30, dat);
        logger.log(Level.TRACE, "Write FF1A - NR30 - Channel 3 Sound on/off (R/W) $%02X".formatted(dat));
    }

    private int r_FF1B_NR31_Channel_3_Sound_Length() {
        int dat = readDmg.apply(NR31);
        logger.log(Level.TRACE, "Read FF1B - NR31 - Channel 3 Sound Length $%02X".formatted(dat));
        return dat;
    }

    private void w_FF1B_NR31_Channel_3_Sound_Length(int dat) {
        writeDmg.accept(NR31, dat);
        logger.log(Level.TRACE, "Write FF1B - NR31 - Channel 3 Sound Length $%02X".formatted(dat));
    }

    private int r_FF1C_NR32_Channel_3_Select_output_level() {
        int dat = readDmg.apply(NR32);
        logger.log(Level.TRACE, "Read FF1C - NR32 - Channel 3 Select output level (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF1C_NR32_Channel_3_Select_output_level(int dat) {
        writeDmg.accept(NR32, dat);
        logger.log(Level.TRACE, "Write FF1C - NR32 - Channel 3 Select output level (R/W) $%02X".formatted(dat));
    }

    private void w_FF1D_NR33_Channel_3_Frequencys_lower_data(int dat) {
        writeDmg.accept(NR33, dat);
        logger.log(Level.TRACE, "Write FF1D - NR33 - Channel 3 Frequency's lower data (W) $%02X".formatted(dat));
    }

    private int r_FF1E_NR34_Channel_3_Frequencys_higher_data() {
        int dat = readDmg.apply(NR34);
        logger.log(Level.TRACE, "Read FF1E - NR34 - Channel 3 Frequency's higher data (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF1E_NR34_Channel_3_Frequencys_higher_data(int dat) {
        writeDmg.accept(NR34, dat);
        logger.log(Level.TRACE, "Write FF1E - NR34 - Channel 3 Frequency's higher data (R/W) $%02X".formatted(dat));
    }

    private int r_FF20_NR41_Channel_4_Sound_Length() {
        int dat = readDmg.apply(NR41);
        logger.log(Level.TRACE, "Read FF20 - NR41 - Channel 4 Sound Length (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF20_NR41_Channel_4_Sound_Length(int dat) {
        writeDmg.accept(NR41, dat);
        logger.log(Level.TRACE, "Write FF20 - NR41 - Channel 4 Sound Length (R/W) $%02X".formatted(dat));
    }

    private int r_FF21_NR42_Channel_4_Volume_Envelope() {
        int dat = readDmg.apply(NR42);
        logger.log(Level.TRACE, "Read FF21 - NR42 - Channel 4 Volume Envelope (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF21_NR42_Channel_4_Volume_Envelope(int dat) {
        writeDmg.accept(NR42, dat);
        logger.log(Level.TRACE, "Write FF21 - NR42 - Channel 4 Volume Envelope (R/W) $%02X".formatted(dat));
    }

    private int r_FF22_NR43_Channel_4_Polynomial_Counter() {
        int dat = readDmg.apply(NR43);
        logger.log(Level.TRACE, "Read FF22 - NR43 - Channel 4 Polynomial Counter (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF22_NR43_Channel_4_Polynomial_Counter(int dat) {
        writeDmg.accept(NR43, dat);
        logger.log(Level.TRACE, "Write FF22 - NR43 - Channel 4 Polynomial Counter (R/W) $%02X".formatted(dat));
    }

    private int r_FF23_NR44_Channel_4_Counter_consecutive_Initial() {
        int dat = readDmg.apply(NR44);
        logger.log(Level.TRACE, "Read FF23 - NR44 - Channel 4 Counter/consecutive; Initial (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF23_NR44_Channel_4_Counter_consecutive_Initial(int dat) {
        writeDmg.accept(NR44, dat);
        logger.log(Level.TRACE, "Write FF23 - NR44 - Channel 4 Counter/consecutive; Initial (R/W) $%02X".formatted(dat));
    }

    private int r_FF24_NR50_Channel_control_ON_OFF_Volume() {
        int dat = readDmg.apply(NR50);
        logger.log(Level.TRACE, "Read FF24 - NR50 - Channel control / ON-OFF / Volume (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF24_NR50_ChannelControl(int dat) {
        writeDmg.accept(NR50, dat);
        logger.log(Level.TRACE, "Write FF24 - NR50 - Channel control / ON-OFF / Volume (R/W) $%02X".formatted(dat));
    }

    private int r_FF25_NR51_SelectionOfSoundOutputTerminal() {
        int dat = readDmg.apply(NR51);
        logger.log(Level.TRACE, "Read FF25 - NR51 - Selection of Sound output terminal (R/W) $%02X".formatted(dat));
        return dat;
    }

    private void w_FF25_NR51_SelectionOfSoundOutputTerminal(int dat) {
        writeDmg.accept(NR51, dat);
        logger.log(Level.TRACE, "Write FF25 - NR51 - Selection of Sound output terminal (R/W) $%02X".formatted(dat));
    }

    private int r_FF26_NR52_Sound_on_off() {
        int dat = readDmg.apply(NR52);
        logger.log(Level.TRACE, "Read FF26 - NR52 - Sound on/off $%02X".formatted(dat));
        return dat;
    }

    private void w_FF26_NR52_SoundOnOff(int dat) {
        writeDmg.accept(NR52, dat);
        logger.log(Level.TRACE, "Write FF26 - NR52 - Sound on/off $%02X".formatted(dat));
    }

    private int r_FF30_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W0);
        logger.log(Level.TRACE, "Read FF30 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF30_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W0, dat);
        logger.log(Level.TRACE, "Write FF30 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF31_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W1);
        logger.log(Level.TRACE, "Read FF31 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF31_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W1, dat);
        logger.log(Level.TRACE, "Write FF31 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF32_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W2);
        logger.log(Level.TRACE, "Read FF32 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF32_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W2, dat);
        logger.log(Level.TRACE, "Write FF32 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF33_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W3);
        logger.log(Level.TRACE, "Read FF33 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF33_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W3, dat);
        logger.log(Level.TRACE, "Write FF33 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF34_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W4);
        logger.log(Level.TRACE, "Read FF34 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF34_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W4, dat);
        logger.log(Level.TRACE, "Write FF34 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF35_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W5);
        logger.log(Level.TRACE, "Read FF35 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF35_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W5, dat);
        logger.log(Level.TRACE, "Write FF35 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF36_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W6);
        logger.log(Level.TRACE, "Read FF36 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF36_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W6, dat);
        logger.log(Level.TRACE, "Write FF36 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF37_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W7);
        logger.log(Level.TRACE, "Read FF37 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF37_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W7, dat);
        logger.log(Level.TRACE, "Write FF37 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF38_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W8);
        logger.log(Level.TRACE, "Read FF38 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF38_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W8, dat);
        logger.log(Level.TRACE, "Write FF38 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF39_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3W9);
        logger.log(Level.TRACE, "Read FF39 - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF39_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3W9, dat);
        logger.log(Level.TRACE, "Write FF39 - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF3A_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3WA);
        logger.log(Level.TRACE, "Read FF3A - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF3A_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3WA, dat);
        logger.log(Level.TRACE, "Write FF3A - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF3B_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3WB);
        logger.log(Level.TRACE, "Read FF3B - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF3B_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3WB, dat);
        logger.log(Level.TRACE, "Write FF3B - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF3C_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3WC);
        logger.log(Level.TRACE, "Read FF3C - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF3C_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3WC, dat);
        logger.log(Level.TRACE, "Write FF3C - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF3D_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3WD);
        logger.log(Level.TRACE, "Read FF3D - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF3D_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3WD, dat);
        logger.log(Level.TRACE, "Write FF3D - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF3E_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3WE);
        logger.log(Level.TRACE, "Read FF3E - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF3E_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3WE, dat);
        logger.log(Level.TRACE, "Write FF3E - Wave Pattern RAM $%02X".formatted(dat));
    }

    private int r_FF3F_Wave_Pattern_RAM() {
        int dat = readDmg.apply(AUD3WF);
        logger.log(Level.TRACE, "Read FF3F - Wave Pattern RAM $%02X".formatted(dat));
        return dat;
    }

    private void w_FF3F_Wave_Pattern_RAM(int dat) {
        writeDmg.accept(AUD3WF, dat);
        logger.log(Level.TRACE, "Write FF3F - Wave Pattern RAM $%02X".formatted(dat));
    }
}
