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
public enum SeekMode {
    WRAP_AT_LIMIT, STOP_AT_LIMIT;
    
    public static SeekMode getSeekModeByValue(int seekModeValue) {
        SeekMode[] seekModeValues = SeekMode.values();
        for (SeekMode seekMode : seekModeValues) {
            if (seekMode.ordinal() == seekModeValue) {
                return seekMode;
            }
        }

        return null;
    }
    
}
