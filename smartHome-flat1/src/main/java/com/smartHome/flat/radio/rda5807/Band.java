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
public enum Band {
    US_EUR(870, 1080), JAPAN(760, 910), WORLD_WIDE(760, 1080), EAST_EUROPE(650, 760);
    
    private final int minimumFrequency;
    private final int maximumFrequency;
    
    Band(int minimumFrequency, int maximumFrequency) {
        this.minimumFrequency = minimumFrequency;
        this.maximumFrequency = maximumFrequency;
    }
    
    public int getMinimumFrequency() {
        return minimumFrequency;
    }
    
    public int getMaximumFrequency() {
        return maximumFrequency;
    }
    
    public static Band getBandByValue(int bandValue) {
        Band[] bandValues = Band.values();
        for (Band band : bandValues) {
           if (band.ordinal() == bandValue) {
              return band; 
           }
        }
        
        return null;
    }
}
