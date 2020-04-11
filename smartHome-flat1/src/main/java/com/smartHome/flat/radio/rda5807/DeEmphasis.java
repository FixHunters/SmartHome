/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rda5807;

/**
 *
 * @author Ladislav Török
 */
public enum DeEmphasis {
    SEVENTY_FIVE_US((byte)0x00, 75), FIFTY_US((byte)0x01, 50);
    
    private final byte deEmphasis;
    private final int value;

    private DeEmphasis(byte deEmphasis, int value) {
        this.deEmphasis = deEmphasis;
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    public static DeEmphasis getDeEmphasisByValue(int deEmphasisValue) {
        DeEmphasis[] deEmphasisValues = DeEmphasis.values();
        for (DeEmphasis deEmphasis : deEmphasisValues) {
            if (deEmphasis.ordinal() == deEmphasisValue) {
                return deEmphasis;
            }
        }

        return null;
    }
    
}
