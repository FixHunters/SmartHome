/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rda5807;

/**
 * Defines for register masks. Taken from V1.1 of the datasheet.
 *
 * @author Ladislav Török
 */
public class RegisterMasks {

    // Register 0x00
    public static final int CHIP_ID = 0xFF00;

    // Register 0x02
    public static final int DHIZ = 0x8000;
    public static final int DMUTE = 0x4000;
    public static final int DMONO = 0x2000;
    public static final int DBASS = 0x1000;
    public static final int RCLK_NCM = 0x0800;
    public static final int RCLK_DIM = 0x0400;
    public static final int SEEKUP = 0x0200;
    public static final int SEEK = 0x0100;
    public static final int SKMODE = 0x0080;
    public static final int CLK_MODE = 0x0070;
    public static final int RDS_EN = 0x0008;
    public static final int NEW_METHOD = 0x0004;
    public static final int SOFT_RESET = 0x0002;
    public static final int ENABLE = 0x0001;

    // Register 0x03
    public static final int CHAN = 0xFFC0;
    public static final int DIRECT_MODE = 0x0020;
    public static final int TUNE = 0x0010;
    public static final int BAND = 0x000C;
    public static final int SPACE = 0x0003;

    // Register 0x04
    public static final int RSVD_04_0 = 0xF000;
    public static final int DE = 0x0800;
    public static final int RSVD_04_1 = 0x0400;
    public static final int SOFTMUTE_EN = 0x0200;
    public static final int AFCD = 0x0100;

    // Register 0x05
    public static final int INT_MODE = 0x8000;
    public static final int RSVD_05_0 = 0x7000;
    public static final int SEEKTH = 0x0F00;
    public static final int RSVD_05_1 = 0x0030;
    public static final int VOLUME = 0x000F;

    // Register 0x06
    public static final int RSVD_06_0 = 0x8000;
    public static final int OPEN_MODE = 0x6000;

    // Register 0x07
    public static final int RSVD_07_0 = 0x8000;
    public static final int TH_SOFRBLEND = 0x7C00;
    public static final int R_65M_50M_MODE = 0x0200; // The leading R_ is to avoid starting the define with a number
    public static final int RSVD_07_1 = 0x0100;
    public static final int SEEK_TH_OLD = 0x00FC;
    public static final int SOFTBLEND_EN = 0x0002;
    public static final int FREQ_MODE = 0x0001;

    // Register 0x0A
    public static final int RDSR = 0x8000;
    public static final int STC = 0x4000;
    public static final int SF = 0x2000;
    public static final int RDSS = 0x1000;
    public static final int BLK_E = 0x0800;
    public static final int ST = 0x0400;
    public static final int READCHAN = 0x03FF;

    // Register 0x0B
    public static final int RSSI = 0xFE00;
    public static final int FM_TRUE = 0x0100;
    public static final int FM_READY = 0x0080;
    public static final int RSVD_0B_0 = 0x0060;
    public static final int ABCD_E = 0x0010;
    public static final int BLERA = 0x000C;
    public static final int BLERB = 0x0003;

    // Register 0x0C
    public static final int RDSA = 0xFFFF;

    // Register Ox0D
    public static final int RDSB = 0xFFFF;

    // Register 0x0E
    public static final int RDSC = 0xFFFF;

    // Register 0X0F
    public static final int RDSD = 0xFFFF;

    /**
     * RDS/RBDS Common Mappings
     */
    // Block 1/A
    public static final int PI_CODE = 0xFFFF;

    // Block 2/B
    public static final int GROUP_TYPE = 0xF000;
    public static final int VERSION_CODE = 0x0800;
    public static final int TRAFFIC_PROGRAM = 0x0400;
    public static final int PROGRAM_TYPE = 0x03E0;

    /**
     * General masks
     */
    public static final int UINT16_UPPER_BYTE = 0xFF00;
    public static final int UINT16_LOWER_BYTE = 0x00FF;

}
