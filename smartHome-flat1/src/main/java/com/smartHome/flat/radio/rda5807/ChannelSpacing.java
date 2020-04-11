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
public enum ChannelSpacing {
    ONE_HUNDRED_KHZ((byte)0x00, 100), TWO_HUNDRED_KHZ((byte)0x01, 200), FIFTY_KHZ((byte)0x03, 50), TWENTY_FIVE_KHZ((byte)0x04, 25);

    private final byte channelSpace;
    private final int valueInKHz;
    
    ChannelSpacing(byte channelSpace, int valueInKHz) {
        this.channelSpace = channelSpace;
        this.valueInKHz = valueInKHz;
    }
    
    public int getValueInKHz() {
        return valueInKHz;
    }
    
    public static ChannelSpacing getChannelSpacingByValueInKhz(int valueInKHz) {
        ChannelSpacing[] channelSpacingValues = ChannelSpacing.values();
        for (ChannelSpacing channelSpacing : channelSpacingValues) {
           if (channelSpacing.getValueInKHz() == valueInKHz) {
              return channelSpacing; 
           }
        }
        
        return null;
    }
}
