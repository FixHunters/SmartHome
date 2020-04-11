/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.common;

import com.smartHome.flat.radio.rda5807.StatusResult;

// TODO Upravit interface len pre najnutnejsie metody, popripade vytvorit dalsi interface pre rozsirujuce f-cie ako RDS.

/**
 *
 * @author Ladislav Török
 */
public interface Radio {
    /**
     * 
     * @return 
     */
    StatusResult powerUp();
    
    /**
     * 
     * @return 
     */
    StatusResult powerDown();
    
    /**
     * Resets the radio to a known state.
     * @return 
     */
    StatusResult reset();

    // start FUNCTIONS USED TO ENABLE/DISABLE RADIO MODES
    /**
     * Enables powerup of the radio if enable is true, turns radio power off if
     * enable is false.
     *
     * Reinitializes the radio when enable is true. Disables the radio when
     * enable is false.
     *
     * @param enable
     * @return
     */
    StatusResult setEnabled(boolean enable);

    /**
     * Enables mute if muteEnable is true, disables mute if muteEnable is false.
     *
     * @param muteEnable
     * @return
     */
    StatusResult setMute(boolean muteEnable);

    /**
     * Enables high impedance output mode if highImpedanceEnable is true,
     * enables normal operation if highImpedanceEmable is false.
     *
     * @param highImpedanceEnable
     * @return
     */
    StatusResult setHighImpedanceOutput(boolean highImpedanceEnable);

    /**
     * Enables RDS/RBDS if rdsEnable is true. Disables RDS/RBDS if rdsEnable is
     * false.
     *
     * @param rdsEnable
     * @return
     */
    StatusResult setRdsMode(boolean rdsEnable);

    /**
     *
     * @param softMuteEnable
     * @return
     */
    StatusResult setSoftMute(boolean softMuteEnable);

    /**
     * Enables stereo mode if stereoEnable is true, forces mono mode if
     * stereoEnable is false.
     *
     * @param stereoEnable
     * @return
     */
    StatusResult setStereo(boolean stereoEnable);

    /**
     * As per the manual, using the "new method" can "improve the receive
     * sensitiity by about 1dB." If newMethodEnable is true, the new method is
     * enabled, and if newMethodEnabled is false, the new method is disabled.
     *
     * KEEP ME ENABLED! Using new method offers a drastic performance reception
     * improvement.
     *
     * @param newMethodEnable
     * @return
     */
    StatusResult setNewMethod(boolean newMethodEnable);

    /**
     * Sets the radio volume. volume = 1111 is maximum, and volume = 0 is muted.
     * The volume scale is "logarithmic" as per the manual.
     *
     * @param volume
     * @return
     */
    StatusResult setVolume(int volume);

    /**
     *
     * @param channelSpacing (in KHz)
     * @return
     */
    StatusResult setChannelSpacing(int channelSpacing);

    /**
     *
     * @param band
     * @return
     */
    StatusResult setBand(int band);

    /**
     *
     * @param enable
     * @return
     */
    StatusResult setTune(boolean enable);

    /**
     * Enables bass bost if bassBostEnabe is true, disables bass boost if
     * bassBostEnable is false.
     *
     * @param bassBoostEnable
     * @return
     */
    StatusResult setBassBoost(boolean bassBoostEnable);

    // TODO Upravit javadoc !!!
    /**
     * Sets the seek direction to up if seekDirection is SEEK_UP, and sets the
     * seek direction to down if seekDirection is SEEK_DOWN.
     *
     * @param seekDirection
     * @return
     */
    StatusResult setSeekDirection(int seekDirection);

    /**
     * Enables seek mode if seekEnable is true, and disables seek mode if
     * seekEnable is false.
     *
     * @param seekEnable
     * @return
     */
    StatusResult setSeek(boolean seekEnable);

    // TODO Upravit javadoc.
    /**
     * If seekMode is WRAP_AT_LIMIT, when the seek operation reaches the upper
     * or lower band limit, the frequency will roll over to the opposite limit.
     * If seekMode is STOP_AT_LIMIT, when the seek operation reaches a band
     * limit, seeking will stop.
     *
     * @param seekMode
     * @return
     */
    StatusResult setSeekMode(int seekMode);

    /**
     * Puts the radio in soft reset mode if softResetEnable is true, removes the
     * radio from soft reset mode if softResetEnable is false.
     *
     * @param softResetEnable
     * @return
     */
    StatusResult setSoftReset(boolean softResetEnable);

    /**
     * Sets the radio frequency. The frequency is to be provided as an integer.
     * For example: 104.3 MHz is passed as 1043.
     * The minimum and maximum frequency are dependent from band.
     *
     * @param frequency
     * @return
     */
    StatusResult setFrequency(int frequency);

    /**
     *
     * @param deEmphasis
     * @return
     */
    StatusResult setDeEmphasis(int deEmphasis);

    /**
     *
     * @param afcdEnable
     * @return
     */
    StatusResult setAFCD(boolean afcdEnable);

    /**
     *
     * @param softBlendEnable
     * @return
     */
    StatusResult setSoftBlend(boolean softBlendEnable);

    // end FUNCTIONS USED TO ENABLE/DISABLE RADIO MODES
}
