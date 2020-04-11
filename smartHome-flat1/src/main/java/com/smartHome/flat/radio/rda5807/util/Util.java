/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rda5807.util;

/**
 *
 * @author Ladislav Török
 */
public class Util {

    /**
     * Convert boolean value to integer value (according to the current need -
     * 0xFF or 0x00).
     *
     * @param booleanValue boolean value (true or false) for conversion
     * @return 0xFF when booleanValue is true and 0x00 when booleanValue is
     * false
     */
    public static int boolToInteger(boolean booleanValue) {
        if (booleanValue) {
            return 0xFF;
        } else {
            return 0x00;
        }
    }

    /**
     * Convert integer value to boolean value (it as is usually used in C-like
     * languages).
     *
     * @param integerValue integer value for conversion
     * @return true if integerValue > 0, false otherwise
     */
    public static boolean boolFromInteger(int integerValue) {
        return (integerValue > 0);
    }

    /**
     * Returns the value of regContent 'anded' with mask. This result is then
     * shifted so that the lower order non-zero bit in mask is moved to the
     * zeroth bit position. For example, if regContent = 0xFF and mask = 0x10,
     * the result would be 0x01.
     *
     * @param regContent
     * @param mask
     * @return 
     */
    public static int valueFromReg(int regContent, int mask) {
        byte shiftAmt = 0;
        int shiftedMask = mask;

        while (((shiftedMask & 0x0001) == 0) && (shiftAmt < 16)) {
            shiftedMask >>= 1;
            ++shiftAmt;
        }

        return ((regContent & mask) >> shiftAmt);
    }

}
