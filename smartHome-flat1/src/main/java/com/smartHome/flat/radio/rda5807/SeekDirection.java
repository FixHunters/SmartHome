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
public enum SeekDirection {
    SEEK_UP, SEEK_DOWN;

    public static SeekDirection getSeekDirectionByValue(int seekDirectionValue) {
        SeekDirection[] seekDirectionValues = SeekDirection.values();
        for (SeekDirection seekDirection : seekDirectionValues) {
            if (seekDirection.ordinal() == seekDirectionValue) {
                return seekDirection;
            }
        }

        return null;
    }

}
