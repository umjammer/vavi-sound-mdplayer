package mdplayer.driver.zms.nise68;


public final class Cycle {

    public static final int[] And_bDnEA = {12, 12, 14, 16, 18, 16, 20};
    public static final int[] And_wDnEA = {12, 12, 14, 16, 18, 16, 20};
    public static final int[] And_lDnEA = {20, 20, 22, 24, 26, 24, 28};
    public static final int[] And_bEADn = {4, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] And_wEADn = {4, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] And_lEADn = {8, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};
    public static final int[] Andi_b = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Andi_w = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Andi_l = {16, 28, 28, 30, 32, 24, 32, 36};
    public static final int[] Ori_b = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Ori_w = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Ori_l = {16, 30, 30, 32, 34, 36, 34, 38};
    public static final int[] Btst08 = {10, 12, 12, 14, 16, 18, 16, 20, 16, 18};
    public static final int[] Btst = {6, 8, 8, 10, 12, 14, 12, 16, 12, 14, 10};
    public static final int[] Bset_i = {12, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Bset = {8, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Bclr_i = {14, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Cmpi_b = {8, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Cmpi_w = {8, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Cmpi_l = {14, 20, 20, 22, 24, 26, 24, 28};
    public static final int[] Or_b = {12, 12, 14, 16, 18, 16, 20};
    public static final int[] Or_w = {12, 12, 14, 16, 18, 16, 20};
    public static final int[] OrEaDn_b = {4, -1, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] OrEaDn_w = {4, -1, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] OrEaDn_l = {8, -1, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};
    public static final int[] Eor_b = {4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Eor_w = {4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Eor_l = {8, 20, 20, 22, 24, 26, 24, 28};
    public static final int[] Eori_b = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Eori_w = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Eori_l = {16, 28, 28, 30, 32, 34, 32, 36};
    public static final int[] Clsrlsl_wea = {-1, -1, 12, 12, 14, 16, 18, 16, 20, -1, -1, -1};

    public static final int[] Movea_w = {
            4, 4, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8
    };
    public static final int[] Movea_l = {
            4, 4, 12, 12, 14, 16, 18, 16, 20, 16, 18, 12
    };

    public static final int[][] Move_b = {
            new int[] {4, 8, 8, 8, 12, 14, 12, 16},
            new int[] {-1, -1, -1, -1, -1, -1, -1, -1},
            new int[] {8, 12, 12, 12, 16, 18, 16, 20},
            new int[] {8, 12, 12, 12, 16, 18, 16, 20},
            new int[] {10, 14, 14, 14, 18, 20, 18, 22},
            new int[] {12, 16, 16, 16, 20, 22, 20, 24},
            new int[] {14, 18, 18, 18, 22, 24, 22, 26},
            new int[] {12, 16, 16, 16, 20, 22, 20, 24},
            new int[] {16, 20, 20, 20, 24, 26, 24, 28},
            new int[] {12, 16, 16, 16, 20, 22, 20, 24},
            new int[] {14, 18, 18, 18, 22, 24, 22, 26},
            new int[] {8, 12, 12, 12, 16, 18, 16, 20}
    };
    public static final int[][] Move_w = {
            new int[] {4, 8, 8, 8, 12, 14, 12, 16},
            new int[] {4, 8, 8, 8, 12, 14, 12, 16},
            new int[] {8, 12, 12, 12, 16, 18, 16, 20},
            new int[] {8, 12, 12, 12, 16, 18, 16, 20},
            new int[] {10, 14, 14, 14, 18, 20, 18, 22},
            new int[] {12, 16, 16, 16, 20, 22, 20, 24},
            new int[] {14, 18, 18, 18, 22, 24, 22, 26},
            new int[] {12, 16, 16, 16, 20, 22, 20, 24},
            new int[] {16, 20, 20, 20, 24, 26, 24, 28},
            new int[] {12, 16, 16, 16, 20, 22, 20, 24},
            new int[] {14, 18, 18, 18, 22, 24, 22, 26},
            new int[] {8, 12, 12, 12, 16, 18, 16, 20}
    };
    public static final int[][] Move_l = {
            new int[] {4, 12, 12, 12, 16, 18, 16, 20},
            new int[] {4, 12, 12, 12, 16, 18, 16, 20},
            new int[] {12, 20, 20, 20, 24, 26, 24, 28},
            new int[] {12, 20, 20, 20, 24, 26, 24, 28},
            new int[] {14, 22, 22, 22, 26, 28, 26, 30},
            new int[] {16, 24, 24, 24, 28, 30, 28, 32},
            new int[] {18, 26, 26, 26, 30, 32, 30, 34},
            new int[] {16, 24, 24, 24, 28, 30, 28, 32},
            new int[] {20, 28, 28, 28, 32, 34, 32, 36},
            new int[] {16, 24, 24, 24, 28, 30, 28, 32},
            new int[] {18, 26, 26, 26, 30, 32, 30, 34},
            new int[] {12, 20, 20, 20, 24, 26, 24, 28}
    };

    public static final int[] MovemFromReg_w = {4, 4, 4, 4, 4, 4};
    public static final int[] MovemFromReg_l = {8, 8, 8, 8, 8, 8};
    public static final int[] MovemToReg_w0 = {12, 12, 16, 18, 16, 20, 16, 18};
    public static final int[] MovemToReg_w1 = {4, 4, 4, 4, 4, 4, 4, 4};
    public static final int[] MovemToReg_l0 = {-1, -1, 12, 12, -1, 16, 18, 16, 20, 16, 18};
    public static final int[] MovemToReg_l1 = {-1, -1, 8, 8, -1, 8, 8, 8, 8, 8, 8};
    public static final int[] MoveToCcr_w = {12, -1, 16, 16, 18, 20, 22, 20, 24, 20, 22, 16};
    public static final int[] MoveFromSr_w = {6, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] MoveToSr_w = {12, -1, 16, 16, 18, 20, 22, 20, 24, 20, 22, 16};

    public static final int[] Addq_b = {4, -1, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Addq_w = {4, 4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Addq_l = {8, 8, 20, 20, 22, 24, 26, 24, 28};
    public static final int[] Addi_b = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Addi_w = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Addi_l = {16, 28, 28, 30, 32, 34, 32, 36};
    public static final int[] Add0_b = {4, -1, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] Add0_w = {4, 4, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] Add0_l = {8, 8, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};
    public static final int[] Add1_b = {12, 12, 14, 16, 18, 16, 20};
    public static final int[] Add1_w = {12, 12, 14, 16, 18, 16, 20};
    public static final int[] Add1_l = {20, 20, 22, 24, 26, 24, 28};
    public static final int[] Adda_w = {8, 8, 12, 12, 14, 16, 18, 16, 20, 16, 18, 12};
    public static final int[] Adda_l = {8, 8, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};

    public static final int[] Subq_b = {4, -1, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Subq_w = {4, 4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Subq_l = {8, 8, 16, 16, 22, 24, 26, 24, 28};
    public static final int[] Sub_b = {4, -1, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] Sub_w = {4, 4, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] Sub_l = {8, 8, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};
    public static final int[] Suba_w = {8, 8, 12, 12, 14, 16, 18, 16, 20, 16, 18, 12};
    public static final int[] Suba_l = {8, 8, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};
    public static final int[] Sub_bDn = {-1, -1, 12, 12, 14, 16, 18, 16, 20, -1, -1, -1};
    public static final int[] Sub_wDn = {-1, -1, 12, 12, 14, 16, 18, 16, 20, -1, -1, -1};
    public static final int[] Sub_lDn = {-1, -1, 20, 20, 22, 24, 26, 24, 28, -1, -1, -1};
    public static final int[] Subi_b = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Subi_w = {8, 16, 16, 18, 20, 22, 20, 24};
    public static final int[] Subi_l = {16, 28, 28, 30, 32, 34, 32, 36};

    public static final int[] Cmp_b = {4, -1, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] Cmp_w = {4, 4, 8, 8, 10, 12, 14, 12, 16, 12, 14, 8};
    public static final int[] Cmp_l = {6, 6, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};
    public static final int[] Cmpa_l = {6, 6, 14, 14, 16, 18, 20, 18, 22, 18, 20, 14};

    public static final int[] Mulu_w = {70, -1, 74, 74, 76, 78, 80, 78, 82, 78, 80, 74};
    public static final int[] Muls_w = {70, -1, 74, 74, 76, 78, 80, 78, 82, 78, 80, 74};
    public static final int[] Divu_w = {140, -1, 144, 144, 146, 148, 150, 148, 152, 148, 150, 144};
    public static final int[] Divs_w = {158, -1, 162, 162, 164, 166, 168, 166, 170, 166, 168, 162};

    public static final int[] Pea_l = {0, 0, 14, 0, 0, 18, 22, 18, 22, 18, 22, 0};

    public static final int[] Clr_b = {4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Clr_w = {4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Clr_l = {6, 20, 20, 22, 24, 26, 24, 28};

    public static final int[] Not_b = {4, 12, 12, 14, 16, 18, 16, 20};

    public static final int[] Tst_b = {4, 8, 8, 10, 12, 14, 12, 16};
    public static final int[] Tst_w = {4, 8, 8, 10, 12, 14, 12, 16};
    public static final int[] Tst_l = {4, 12, 12, 14, 16, 18, 16, 20};

    public static final int[] Neg_b = {4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Neg_w = {4, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Neg_l = {6, 20, 20, 22, 24, 26, 24, 28};

    public static final int[] Tas = {4, 14, 14, 16, 18, 20, 18, 22};

    public static final int[] Scc_b = {6, 12, 12, 14, 16, 18, 16, 20};
    public static final int[] Jsr_l = {16, 18, 22, 18, 20, 18, 22};
    public static final int[] Jmp = {8, 10, 14, 10, 12, 10, 14};
}
