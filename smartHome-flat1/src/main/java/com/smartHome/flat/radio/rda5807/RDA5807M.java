/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rda5807;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.smartHome.flat.radio.common.Radio;
import com.smartHome.flat.radio.rda5807.util.Text;
import com.smartHome.flat.radio.rda5807.util.Util;
import com.smartHome.flat.radio.rds.common.RDSEvent;
import com.smartHome.flat.radio.rds.common.RDSListener;
import com.smartHome.flat.radio.rds.common.RDSObject;
import com.smartHome.flat.radio.rds.common.RDSParser;

/**
 *
 * @author Ladislav Török
 */
public final class RDA5807M implements Radio, RDSListener {

    /////////////////////////////
    // Private class Constants //
    /////////////////////////////
    // Index of the first writable register.
    private static final byte WRITE_REGISTER_BASE_IDX = 0x02;

    // Index of the last writable register.
    private static final byte WRITE_REGISTER_MAX_IDX = 0x07;

    // Index of the first readable register.
    private static final byte READ_REG_BASE_IDX = 0x0A;

    // Index of the last readable register.
    private static final byte READ_REG_MAX_IDX = 0x0F;

    // Default values for the register map. Static variable initialization.
    private static final int[] REGISTER_MAP_DEFAULT_STATE = {
        /* Reg 0x00 */0x5800,  // 0x5800 // 0x5804
        /* Reg 0x01 */ 0x0000, // Reg 0x01 is not used.
        /* Reg 0x02 */ 0xc005, // 0x8000 // 0xc005 //change 0xc001 to 0xc005 enables the RDS/RBDS
        /* Reg 0x03 */ 0x0000, // 0x0000
        /* Reg 0x04 */ 0x0400, // 0x0200 - Only SOFTMUTE_EN is set to 1. // 0x0400
        /* Reg 0x05 */ 0xc3ab, // //0xc3ab=34 stanic // 0xc2a1 // pouzit 0x888f; alebo nanajvys 0x88cf (v najhorsom pripade 0x884f); ale s 0x880f to vobec nefunguje // 0xc3ab
        /* Reg 0x06 */ 0x6000, // 0x0000 // 0x6000
        /* Reg 0x07 */ 0x4216, // 0x4202 - TH_SOFRBLEND set to 10000, 65M_50M MODE set to 1 and SOFTBLEND_EN set to 1. // 0x4216
        /* Below are read-only regs */
        /* Reg 0x08 */ 0x0000,
        /* Reg 0x09 */ 0x0000,
        /* Reg 0x0A */ 0x0000,
        /* Reg 0x0B */ 0x0000,
        /* Reg 0x0C */ 0x0000,
        /* Reg 0x0D */ 0x0000,
        /* Reg 0x0E */ 0x0000,
        /* Reg 0x0F */ 0x0000};

    // This class makes use of the random access I2C mode, which
    // isn't documented well for this chip.
    // Nonetheless, it's less of a pain to use than the sequential
    // access mode.
    // I2C registerAddress for sequential access mode to the registers (I2C-Address RDA Chip for Sequential Access) (RDA5800 style).
    private static final byte SEQUENTIAL_ACCESS_I2C_MODE_ADDR = 0x10;
    // I2C registerAddress for random access mode to the registers (I2C-Address RDA Chip for Index Access).
    private static final byte RANDOM_ACCESS_I2C_MODE_ADDR = 0x11;
    // I2C registerAddress for sequential access mode to the registers (TEA5767 compatible).
    private static final byte SEQUENTIAL_ACCESS_I2C_TEA5767_COMPATIBLE_MODE_ADDR = 0x60;
    // Note that while there are many similarities, the register map of the RDA5807
    // differs from that of the RDA5800 in several essential places.

    // Limits
    private static final int MAX_VOLUME = 0x0F;
    private static final int MIN_VOLUME = 0x00;
    private static final byte RSSI_MAX = 0x7F;

    // Expected I2C bus number for RDA5807M.
    private static final byte EXPECTED_I2C_BUS_ID_FOR_RDA5807M = 3;

    //////////////////////////////
    // Private member variables //
    //////////////////////////////
    // The I2C interface which represents I2C bus.
    private I2CBus i2cBusForRDA5807M;
    // The I2C interface wA5807Mhich represents I2C device - radio (used to talk to the radio).
    private I2CDevice i2cDeviceForRDA5807M;

    /**
     * The register map of the device. The first register is 0x00 and the last
     * is 0x0F. Each register is two bytes (16 bits).
     */
    private final int[] registers;

    // The current band (freq range).
    private Band band;

    /**
     * Returns the currently selected band (stored as an internal member, not
     * read from the radio).
     *
     * @return
     */
    public Band getBand() {
        return band;
    }

    private RDSParser rdsParser;
    private Thread t;
    
    // Constructor.
    public RDA5807M() {
        System.out.println("Constructor.");

        // Set the I2C interface.
        StatusResult statusResult = this.setI2cInterface();
        System.out.println("I2C interface set result: " + statusResult.name());

        // Initialization the local register map (16 registers).
        this.registers = new int[0x10];
        // Reset the local register map.
        System.arraycopy(REGISTER_MAP_DEFAULT_STATE, 0, registers, 0, REGISTER_MAP_DEFAULT_STATE.length);

        // Set the US/Europe intitial band.
        this.band = Band.US_EUR;
    }

    ////////////////////////////////////////////////////////////////////////////
    public void specialInit() {
  
        // init() zapise len reg. od 2 do 7 (2, 3, 4, 5, 6, 7); reg. 0 je chip id a reg. 1 sa nepouziva
        writeRegisterToDevice(Register.REG_0x08, 0x0000);
        readRegisterFromDeviceSpecial(Register.REG_0x08);
        writeRegisterToDevice(Register.REG_0x09, 0x0000);
        readRegisterFromDeviceSpecial(Register.REG_0x09);
        writeRegisterToDevice(Register.REG_0x0A, 0x0000);
        readRegisterFromDeviceSpecial(Register.REG_0x0A);
        writeRegisterToDevice(Register.REG_0x0B, 0x0000);
        readRegisterFromDeviceSpecial(Register.REG_0x0B);
        writeRegisterToDevice(Register.BLOCK_A, 0x0000); // 0x0C
        readRegisterFromDeviceSpecial(Register.BLOCK_A);
        writeRegisterToDevice(Register.BLOCK_B, 0x0000); // 0x0D
        readRegisterFromDeviceSpecial(Register.BLOCK_B);
        writeRegisterToDevice(Register.BLOCK_C, 0x0000); // 0x0E
        readRegisterFromDeviceSpecial(Register.BLOCK_C);
        writeRegisterToDevice(Register.BLOCK_D, 0x0000); // 0x0F
        readRegisterFromDeviceSpecial(Register.BLOCK_D);
        
        writeRegisterToDevice(Register.REG_0x10, 0x0000);
        readRegisterFromDeviceSpecial(Register.REG_0x10);
        writeRegisterToDevice(Register.REG_0x11, 0x0019);
        readRegisterFromDeviceSpecial(Register.REG_0x11);
        writeRegisterToDevice(Register.REG_0x12, 0x2a11);
        readRegisterFromDeviceSpecial(Register.REG_0x12);
        writeRegisterToDevice(Register.REG_0x13, 0xb042);
        readRegisterFromDeviceSpecial(Register.REG_0x13);
        writeRegisterToDevice(Register.REG_0x14, 0x2a11);
        readRegisterFromDeviceSpecial(Register.REG_0x14);
        writeRegisterToDevice(Register.REG_0x15, 0xb831);
        readRegisterFromDeviceSpecial(Register.REG_0x15);
        writeRegisterToDevice(Register.REG_0x16, 0xc000);
        readRegisterFromDeviceSpecial(Register.REG_0x16);
        writeRegisterToDevice(Register.REG_0x17, 0x2a91);
        readRegisterFromDeviceSpecial(Register.REG_0x17);
        writeRegisterToDevice(Register.REG_0x18, 0x9400);
        readRegisterFromDeviceSpecial(Register.REG_0x18);
        writeRegisterToDevice(Register.REG_0x19, 0x00a8);
        readRegisterFromDeviceSpecial(Register.REG_0x19);
        writeRegisterToDevice(Register.REG_0x1A, 0xc400);
        readRegisterFromDeviceSpecial(Register.REG_0x1A);
        writeRegisterToDevice(Register.REG_0x1B, 0xf7cf);
        readRegisterFromDeviceSpecial(Register.REG_0x1B);
        writeRegisterToDevice(Register.REG_0x1C, 0x2adc);
        readRegisterFromDeviceSpecial(Register.REG_0x1C);
        writeRegisterToDevice(Register.REG_0x1D, 0x806f);
        readRegisterFromDeviceSpecial(Register.REG_0x1D);
        writeRegisterToDevice(Register.REG_0x1E, 0x4608);
        readRegisterFromDeviceSpecial(Register.REG_0x1E);
        writeRegisterToDevice(Register.REG_0x1F, 0x0086);
        readRegisterFromDeviceSpecial(Register.REG_0x1F);
        writeRegisterToDevice(Register.REG_0x20, 0x0661);
        readRegisterFromDeviceSpecial(Register.REG_0x20);
        writeRegisterToDevice(Register.REG_0x21, 0x0000);
        readRegisterFromDeviceSpecial(Register.REG_0x21);
        writeRegisterToDevice(Register.REG_0x22, 0x109e);
        readRegisterFromDeviceSpecial(Register.REG_0x22);
        writeRegisterToDevice(Register.REG_0x23, 0x23c8);
        readRegisterFromDeviceSpecial(Register.REG_0x23);
        writeRegisterToDevice(Register.REG_0x24, 0x0406);
        readRegisterFromDeviceSpecial(Register.REG_0x24);
        // ??? reg. 25 by sa zrejme mal nastavovat iba v pripade kedy sa pouzije 12M crystal ???
        writeRegisterToDevice(Register.REG_0x25, 0x0e1c); // 0x0e1c - DOBRE!!! (reg. 0x05=0xc3ab -> cca 30 stanic) // 0x0408 - ??? (reg. 0x05=0xc3ab -> cca 20 stanic)
        readRegisterFromDeviceSpecial(Register.REG_0x25);
    }
    
    private StatusResult writeRegisterToDevice(Register register, int wordToWrite) {
        byte registerAddress = register.getAddress();
        byte[] writeBytes;
        try {
            writeBytes = this.prepareWriteData(wordToWrite);
            System.out.println(String.format("Writing: {register: 0x%02x, upper: 0x%02x, lower: 0x%02x, word: 0x%04x}", registerAddress, writeBytes[0], writeBytes[1], wordToWrite));
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            String exceptionMessage = ioex.getMessage();
            System.out.println("Write failed: " + ioex.getClass().getName() + ": " + exceptionMessage);
            if (exceptionMessage.startsWith("Input/output error")) {
                System.out.println(String.format("Reading of register 0x%02x to verify the written data...", registerAddress));
                int readWord = this.readRegisterFromDevice(register);
                if (readWord == wordToWrite) {
                    System.out.println("Written and read data are matched.");
                    return StatusResult.SUCCESS;
                } else {
                    System.out.println("Written and read data are mismatched.");
                }
            } else {
                System.out.println("Unexpected exception message: " + exceptionMessage + " for " + ioex.getClass().getName());
            }

            return StatusResult.I2C_FAILURE;
        }
        //System.out.println("Write successful");

        return StatusResult.SUCCESS;
    }
    
    private void readRegisterFromDeviceSpecial(Register register) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException iex) {
            System.out.println(iex.getMessage());
        }
        int readData = readRegisterFromDevice(register);
        System.out.println(String.format("Read: {register: 0x%02x, word: 0x%04x}", register.getAddress(), readData));
    }
    ////////////////////////////////////////////////////////////////////////////
    
    /////////////////////////////////
    // Private interface functions //
    /////////////////////////////////
    /**
     * Initializes the radio to a known state. This is done implicitly when an
     * RDA5807M instance is constructed.
     */
    private StatusResult init() {
        // reg. 02H
        this.setHighImpedanceOutput(false, false);  // set DHIZ to 1
        this.setMute(true, false);                 // set DMUTE to 0
        this.setStereo(true, false);                // set MONO to 0
        //this.setBassBoost(true, false);             // <<< nie je nutne nastav. pri init???
        
        //this.setSeekDirection(SeekDirection.SEEK_UP, false); // <<< nie je nutne nastav. pri init???
        //this.setSeekMode(SeekMode.STOP_AT_LIMIT, false);     // <<< nie je nutne nastav. pri init???
        
        this.setRdsMode(true, false);               // set RDS_EN to 1
        this.setNewMethod(true, false);             // set NEW_METHOD to 1 - KEEP ME ENABLED! Using new method offers a drastic performance reception improvement.       
        this.setEnabled(true, false);               // set ENABLE to 1
        // po tychto nastav. musi mat reg. 02H hodnotu 1000000000001101b=0x800d
        
        // reg. 03H
        this.setTune(true, false);                                      // set TUNE to 1
        this.setBand(band, false);                                      // set BAND to 00 (US/Europe)
        this.setChannelSpacing(ChannelSpacing.ONE_HUNDRED_KHZ, false);  // set SPACE to 00 (100KHz channel spacing) 
        // po tychto nastav. musi mat reg. 03H hodnotu 0000000000010000b=0x0010
        
        // reg. 04H
        this.setSoftMute(false, false); // set SOFTMUTE_EN to 0
        // po tychto nastav. musi mat reg. 04H hodnotu 0000000000000000b=0x0000
        
        // reg. 05H
        this.setVolume(0x00, false); // set VOLUME to 0000
        // po tychto nastav. musi mat reg. 05H hodnotu 1000100001000000b=0x8840
        
        /*
        try {
            // special
            System.out.println("*** special write to reg. 0x20 ***");
            i2cDeviceForRDA5807M.write(0x20, (byte)0x0f); // orig. hodnota 0x0661 alebo podla RPI tam bola 0x6106
        } catch (IOException ioex) {
            System.out.println(ioex.getMessage());
        }
        */
        ////////////////////////////////////////////////////////////////////////
        /*
        byte registerAddress = Register.REG_0x20.getAddress();
        int writeWord = 0;
        byte[] writeBytes;
        try {
            writeBytes = this.prepareWriteData(writeWord);
            //System.out.println(String.format("Writing: {register: 0x%02x, upper: 0x%02x, lower: 0x%02x, word: 0x%04x}", registerAddress, writeBytes[0], writeBytes[1], writeWord));
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            String exceptionMessage = ioex.getMessage();
            System.out.println("Write failed: " + ioex.getClass().getName() + ": " + exceptionMessage);
            if (exceptionMessage.startsWith("Input/output error")) {
                System.out.println(String.format("Reading of register 0x%02x to verify the written data...", registerAddress));
                int readWord = this.readRegisterFromDevice(Register.REG_0x20);
                if (readWord == writeWord) {
                    System.out.println("Written and read data are matched.");
                    return StatusResult.SUCCESS;
                } else {
                    System.out.println("Written and read data are mismatched.");
                }
            } else {
                System.out.println("Unexpected exception message: " + exceptionMessage + " for " + ioex.getClass().getName());
            }

            return StatusResult.I2C_FAILURE;
        }
        */
        ////////////////////////////////////////////////////////////////////////
        
        
        return this.writeAllRegistersToDevice();
    }

    public void turnOnRDS() {
        // mozno by bolo lepsie dat nasledujuce tri riadky na ine miesto ???
        rdsParser = new RDSParser();
        rdsParser.addListener(this);
        this.checkRDS();
    }
    
    /**
     *
     * @return
     */
    private StatusResult setI2cInterface() {
        List<Integer> availableI2CBusIds = new ArrayList<>();
        try {
            int[] foundI2CBusNumbers = I2CFactory.getBusIds();
            if (foundI2CBusNumbers != null) {
                availableI2CBusIds.addAll(Arrays.stream(foundI2CBusNumbers).boxed().collect(Collectors.toList()));
            }
        } catch (IOException ioex) {
            System.out.println("I/O error during fetch of I2C buses occurred.");
            //ioex.printStackTrace();
            return StatusResult.I2C_FAILURE;
        }

        if (!availableI2CBusIds.isEmpty()) {
            Optional<Integer> foundI2CBusNumberForRDA5807M = availableI2CBusIds.stream()
                    .filter(i2cBusId -> i2cBusId == EXPECTED_I2C_BUS_ID_FOR_RDA5807M)
                    .findAny();
            if (!foundI2CBusNumberForRDA5807M.isPresent()) {
                System.out.println("Expected I2C bus number '" + EXPECTED_I2C_BUS_ID_FOR_RDA5807M + "' for RDA5807M was not found!");
                return StatusResult.I2C_FAILURE;
            } else {
                int i2cBusNumberForRDA5807M = I2CBus.BUS_3;  // foundI2CBusIdForRDA5807M.get() must be 3 - use I2CBus.BUS_3 (I2CBus.BUS_3 is equal 3)
                try {
                    i2cBusForRDA5807M = I2CFactory.getInstance(i2cBusNumberForRDA5807M);
                } catch (I2CFactory.UnsupportedBusNumberException ubnex) { // TODO ma vobec zmysel tato vynimka v tomto pripade ???
                    System.out.println(String.format("I2C bus numer %d is not supported by the underlying system.", i2cBusNumberForRDA5807M));
                    //ubnex.printStackTrace();
                    return StatusResult.I2C_FAILURE;
                } catch (IOException ioex) {
                    System.out.println("I/O error during creating of new I2CBus instance occurred.");
                    //ioex.printStackTrace();
                    return StatusResult.I2C_FAILURE;
                }

                if (i2cBusForRDA5807M != null) {
                    try {
                        i2cDeviceForRDA5807M = i2cBusForRDA5807M.getDevice(RANDOM_ACCESS_I2C_MODE_ADDR);
                    } catch (IOException ioex) {
                        System.out.println(String.format("I2C bus (number %d) cannot return I2C device.", i2cBusForRDA5807M.getBusNumber()));
                        //ioex.printStackTrace();
                        return StatusResult.I2C_FAILURE;
                    }
                } else {
                    System.out.println("Creating of new I2CBus instance: " + i2cBusForRDA5807M);
                    return StatusResult.I2C_FAILURE;
                }
            }
        } else {
            System.out.println("None I2C buses was found.");
            return StatusResult.I2C_FAILURE;
        }

        return StatusResult.SUCCESS;
    }

    private StatusResult closeI2cInterface() {
        try {
            i2cBusForRDA5807M.close();
        } catch (IOException ioex) {
            System.out.println("Exception at closing I2C bus occured.");
            //ioex.printStackTrace();
            return StatusResult.I2C_FAILURE;
        }

        return StatusResult.SUCCESS;
    }

    /**
     * A wrapper for writeRegisterToDevice(). Since many functions have the same
     * if(writeResultToDevice) do write; else return SUCCESS; statement, this
     * function is intended to help slim code duplication. Returns the result of
     * writeRegisterToDevice(regToWrite) if shouldWrite is true, and returns
     * SUCCESS if shouldWrite is false.
     */
    private StatusResult conditionallyWriteRegisterToDevice(Register regToWrite, boolean shouldWrite) {
        if (shouldWrite) {
            return writeRegisterToDevice(regToWrite);
        } else {
            return StatusResult.SUCCESS;
        }
    }

    ////////////////////////////////
    // Public interface functions //
    ////////////////////////////////
    @Override
    public StatusResult powerUp() {
        return this.init();
    }

    @Override
    public StatusResult powerDown() {
        StatusResult statusResult = setEnabled(false, true);
        if (statusResult == StatusResult.SUCCESS) {
            statusResult = this.closeI2cInterface();
        }

        return statusResult;
    }

    @Override
    public StatusResult reset() {
        this.init(); // TODO dorobit this.init() bude vraciat StatusResult ???

        return StatusResult.SUCCESS;
    }

    // start FUNCTIONS USED TO ENABLE/DISABLE RADIO MODES
    @Override
    public StatusResult setMute(boolean muteEnable) {
        return this.setMute(muteEnable, true);
    }

    @Override
    public StatusResult setHighImpedanceOutput(boolean highImpedanceEnable) {
        return this.setHighImpedanceOutput(highImpedanceEnable, true);
    }

    @Override
    public StatusResult setRdsMode(boolean rdsEnable) {
        return this.setRdsMode(rdsEnable, true);
    }

    @Override
    public StatusResult setSoftMute(boolean softMuteEnable) {
        return this.setSoftMute(softMuteEnable, true);
    }

    @Override
    public StatusResult setStereo(boolean stereoEnable) {
        return this.setStereo(stereoEnable, true);
    }

    @Override
    public StatusResult setNewMethod(boolean newMethodEnable) {
        return this.setNewMethod(newMethodEnable, true);
    }

    @Override
    public StatusResult setVolume(int volume) {
        if (volume < MIN_VOLUME) {
            return StatusResult.BELOW_MIN;
        } else if (volume > MAX_VOLUME) {
            return StatusResult.ABOVE_MAX;
        }

        return this.setVolume(volume, true);
    }

    @Override
    public StatusResult setChannelSpacing(int channelSpacingValue) {
        if (channelSpacingValue < ChannelSpacing.TWENTY_FIVE_KHZ.getValueInKHz()) {
            return StatusResult.BELOW_MIN;
        } else if (channelSpacingValue > ChannelSpacing.TWO_HUNDRED_KHZ.getValueInKHz()) {
            return StatusResult.ABOVE_MAX;
        }

        ChannelSpacing channelSpacing = ChannelSpacing.getChannelSpacingByValueInKhz(channelSpacingValue);
        if (channelSpacing == null) {
            return StatusResult.GENERAL_FAILURE; // TODO Zaviest novy StatusResult (mimo zoznamu povolenych hodnot) ???
        }

        return this.setChannelSpacing(channelSpacing, true);
    }

    @Override
    public StatusResult setBand(int bandValue) {
        if (bandValue < Band.US_EUR.ordinal()) {
            return StatusResult.BELOW_MIN;
        } else if (bandValue > Band.EAST_EUROPE.ordinal()) {
            return StatusResult.ABOVE_MAX;
        }

        Band bandByValue = Band.getBandByValue(bandValue);
        if (bandByValue == null) {
            return StatusResult.GENERAL_FAILURE; // TODO Zaviest novy StatusResult (mimo zoznamu povolenych hodnot) ???
        }

        return this.setBand(bandByValue, true);
    }

    @Override
    public StatusResult setTune(boolean enable) {
        return this.setTune(enable, true);
    }

    @Override
    public StatusResult setEnabled(boolean enable) {
        if (enable) {
            this.reset();
        }

        return this.setEnabled(enable, true);
    }

    @Override
    public StatusResult setBassBoost(boolean bassBoostEnable) {
        return this.setBassBoost(bassBoostEnable, true);
    }

    @Override
    public StatusResult setSeekDirection(int seekDirectionValue) {
        // 0 - SEEK_DOWN - SeekDirection.SEEK_DOWN (really 1)
        // 1 - SEEK_UP - SeekDirection.SEEK_UP (really 0)
        if (seekDirectionValue == 0) {
            seekDirectionValue += 1;
        } else if (seekDirectionValue == 1) {
            seekDirectionValue -= 1;
        }

        if (seekDirectionValue < SeekDirection.SEEK_UP.ordinal()) {
            return StatusResult.BELOW_MIN;
        } else if (seekDirectionValue > SeekDirection.SEEK_DOWN.ordinal()) {
            return StatusResult.ABOVE_MAX;
        }

        SeekDirection seekDirection = SeekDirection.getSeekDirectionByValue(seekDirectionValue);
        if (seekDirection == null) {
            return StatusResult.GENERAL_FAILURE; // TODO Zaviest novy StatusResult (mimo zoznamu povolenych hodnot) ???
        }

        return this.setSeekDirection(seekDirection, true);
    }

    @Override
    public StatusResult setSeek(boolean seekEnable) {
        return this.setSeek(seekEnable, true);
    }

    @Override
    public StatusResult setSeekMode(int seekModeValue) {
        // 0 - STOP_AT_LIMIT - SeekMode.STOP_AT_LIMIT (really 1)
        // 1 - WRAP_AT_LIMIT - SeekMode.WRAP_AT_LIMIT (really 0)
        if (seekModeValue == 0) {
            seekModeValue += 1;
        } else if (seekModeValue == 1) {
            seekModeValue -= 1;
        }

        if (seekModeValue < SeekMode.WRAP_AT_LIMIT.ordinal()) {
            return StatusResult.BELOW_MIN;
        } else if (seekModeValue < SeekMode.STOP_AT_LIMIT.ordinal()) {
            return StatusResult.ABOVE_MAX;
        }

        SeekMode seekMode = SeekMode.getSeekModeByValue(seekModeValue);
        if (seekMode == null) {
            return StatusResult.GENERAL_FAILURE; // TODO Zaviest novy StatusResult (mimo zoznamu povolenych hodnot) ???
        }

        return this.setSeekMode(seekMode, true);
    }

    @Override
    public StatusResult setSoftReset(boolean softResetEnable) {
        return this.setSoftReset(softResetEnable, true);
    }

    @Override
    public StatusResult setFrequency(int frequency) {
        if (frequency < this.band.getMinimumFrequency()) {
            return StatusResult.BELOW_MIN;
        } else if (frequency > this.band.getMaximumFrequency()) {
            return StatusResult.ABOVE_MAX;
        }

        this.setChannel(frequency, false);

        return this.setTune(true);
    }

    @Override
    public StatusResult setDeEmphasis(int deEmphasisValue) {
        if (deEmphasisValue < DeEmphasis.FIFTY_US.getValue()) {
            return StatusResult.BELOW_MIN;
        } else if (deEmphasisValue > DeEmphasis.SEVENTY_FIVE_US.getValue()) {
            return StatusResult.ABOVE_MAX;
        }

        DeEmphasis deEmphasis = DeEmphasis.getDeEmphasisByValue(deEmphasisValue);
        if (deEmphasis == null) {
            return StatusResult.GENERAL_FAILURE; // TODO Zaviest novy StatusResult (mimo zoznamu povolenych hodnot) ???
        }

        return this.setDeEmphasis(deEmphasis, true);
    }

    @Override
    public StatusResult setAFCD(boolean afcdEnable) {
        return this.setAFCD(afcdEnable, true);
    }

    @Override
    public StatusResult setSoftBlend(boolean softBlendEnable) {
        return this.setSoftBlend(softBlendEnable, true);
    }

    // end FUNCTIONS USED TO ENABLE/DISABLE RADIO MODES
    ////////////////////////////////////////////////////////////////////////////
    /*
    * Applies value to the masked region in register register. The changes are not
     * pushed to the device by this call.
     */
    public void setRegister(Register register, int value, int mask) {
        int address = register.getAddress();

        //System.out.println(String.format("setRegister(): register: 0x%02x, value: 0x%04x, mask: 0x%04x", register.getAddress(), value, mask));
        // Align the mask's LSB to bit zero
        int shiftAmt = 0;
        int shiftedMask = mask;

        while (((shiftedMask & 0x0001) == 0) && (shiftAmt < 16)) {
            shiftedMask >>= 1;
            ++shiftAmt;
        }

        // AND the mask with the value in the mask position
        int maskAndValue = shiftedMask & value;

        // Shift the mask ANDed with value back to the original mask position
        maskAndValue <<= shiftAmt;

        // Create a working copy of the register we intend to modify
        int regTemp = registers[address];

        // ANDing with the negation of mask will set zeroes to the mask region
        // but won't modify any other bits
        regTemp &= ~mask;

        // ORing maskAndValue with regTemp will apply the changes to the masked
        // region in regTemp, but won't impact any other bits in regTemp
        regTemp |= maskAndValue;

        // Voila!
        registers[address] = regTemp;
    }

    public StatusResult writeRegisterToDevice(Register register) {
        byte registerAddress = register.getAddress();
        int writeWord = registers[registerAddress];
        byte[] writeBytes;
        try {
            writeBytes = this.prepareWriteData(writeWord);
            //System.out.println(String.format("Writing: {register: 0x%02x, upper: 0x%02x, lower: 0x%02x, word: 0x%04x}", registerAddress, writeBytes[0], writeBytes[1], writeWord));
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            String exceptionMessage = ioex.getMessage();
            System.out.println("Write failed: " + ioex.getClass().getName() + ": " + exceptionMessage);
            if (exceptionMessage.startsWith("Input/output error")) {
                System.out.println(String.format("Reading of register 0x%02x to verify the written data...", registerAddress));
                int readWord = this.readRegisterFromDevice(register);
                if (readWord == writeWord) {
                    System.out.println("Written and read data are matched.");
                    return StatusResult.SUCCESS;
                } else {
                    System.out.println("Written and read data are mismatched.");
                }
            } else {
                System.out.println("Unexpected exception message: " + exceptionMessage + " for " + ioex.getClass().getName());
            }

            return StatusResult.I2C_FAILURE;
        }
        //System.out.println("Write successful");

        return StatusResult.SUCCESS;
    }

    public StatusResult writeAllRegistersToDevice() {
        StatusResult statusResult = StatusResult.SUCCESS;
        for (byte regIdx = WRITE_REGISTER_BASE_IDX; regIdx <= WRITE_REGISTER_MAX_IDX; ++regIdx) {
            if (writeRegisterToDevice(Register.getRegisterByAddress(regIdx)) == StatusResult.I2C_FAILURE) {
                statusResult = StatusResult.I2C_FAILURE;
                break;
            }
        }

        return statusResult;
    }

    /**
     *
     */
    public void readDeviceRegistersAndStoreLocally() {
        registers[Register.REG_0x00.getAddress()] = readRegisterFromDevice(Register.REG_0x00);

        for (byte regIdx = READ_REG_BASE_IDX; regIdx <= READ_REG_MAX_IDX; ++regIdx) {
            registers[regIdx] = readRegisterFromDevice(Register.getRegisterByAddress(regIdx));
        }
    }

    public StatusResult updateLocalRegisterMapFromDevice() {
        readDeviceRegistersAndStoreLocally();

        return StatusResult.SUCCESS;
    }

    /**
     * Reads a single register from the device and updates its value in the
     * local register map
     *
     * @param reg
     */
    public void readAndStoreSingleRegisterFromDevice(Register reg) {
        registers[reg.getAddress()] = readRegisterFromDevice(reg);
    }

    /**
     * Returns the content of the specified register from the device.
     *
     * @param register
     * @return
     */
    public int readRegisterFromDevice(Register register) {
        byte registerAddress = register.getAddress();
        byte[] readBytes = new byte[2];
        int readWord = 0;
        int numberOfBytesRead;
        try {
            numberOfBytesRead = i2cDeviceForRDA5807M.read(registerAddress, readBytes, 0, readBytes.length);
            readWord = prepareReadData(readBytes);
            if (numberOfBytesRead == readBytes.length) {
                //System.out.println("Read successful");
                //System.out.println(String.format("Read: {register: 0x%02x; value: 0x%04x}", registerAddress, readWord));
            } else {
                if (numberOfBytesRead < 0) {
                    System.out.println("Read failed: " + numberOfBytesRead);
                } else {
                    System.out.println(String.format("Read failed: %d bytes read from register 0x%02x", numberOfBytesRead, registerAddress));
                }
            }
        } catch (IOException ioex) {
            System.out.println("Read failed: " + ioex.getClass().getName() + ": " + ioex.getMessage());
        }

        return readWord;
    }

    /**
     * Returns the content of the *LOCALLY STORED* register specified by reg.
     *
     * @param register
     * @return
     */
    public int getLocalRegisterContent(Register register) {
        return registers[register.getAddress()];
    }

    public String getLocalCopyOfRegister(byte registerAddress) {
        return String.format("0x%04x", getLocalRegisterContent(Register.getRegisterByAddress(registerAddress)));
    }

    /**
     *
     * @param reg
     * @param mask
     * @return
     */
    public boolean readAndStoreRegFromDeviceAndReturnFlag(Register reg, int mask) {
        readAndStoreSingleRegisterFromDevice(reg);
        int maskedValue = Util.valueFromReg(registers[reg.getAddress()], mask);

        return (maskedValue != 0);
    }

    public boolean isRdsReady() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0A, RegisterMasks.RDSR);
    }

    public boolean isStcComplete() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0A, RegisterMasks.STC);
    }

    public boolean didSeekFail() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0A, RegisterMasks.SF);
    }

    public boolean isRdsDecoderSynchronized() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0A, RegisterMasks.RDSS);
    }

    public boolean hasBlkEBeenFound() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0A, RegisterMasks.BLK_E);
    }

    public boolean isStereoEnabled() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0A, RegisterMasks.ST);
    }

    //TODO this depends on channel spacing!
    // We assume 100KHz spacing and US band currently, and the channel
    // must be in terms of 10f - that is, 985 for 98.5MHz, for example
    public int getReadChannel() {
        int rv = readRegisterFromDevice(Register.REG_0x0A);
        registers[Register.REG_0x0A.getAddress()] = rv;
        int readChannel = Util.valueFromReg(registers[Register.REG_0x0A.getAddress()], RegisterMasks.READCHAN);

        return (readChannel + this.band.getMinimumFrequency());
    }

    public int getRssi() {
        registers[Register.REG_0x0B.getAddress()] = readRegisterFromDevice(Register.REG_0x0B);

        return Util.valueFromReg(registers[Register.REG_0x0B.getAddress()], RegisterMasks.RSSI);
    }

    public boolean isFmTrue() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0B, RegisterMasks.FM_TRUE);
    }

    public boolean isFmReady() {
        return readAndStoreRegFromDeviceAndReturnFlag(Register.REG_0x0B, RegisterMasks.FM_READY);
    }

    // TODO Dat f-cie s RDS do interface-u Radio ???
    // start RDS functions
    /*
     * Use the UPDATELOCALREGS command before calling to pull in new
     * RDS data. Note that if UPDATELOCALREGS is called between queries
     * for RDS data, it'll probably be the case that the RDS data will
     * change.
     */
    public int getRdsPiCode() {
        return this.getRdsPiCode(false);
    }

    private int getRdsPiCode(boolean readRegisterFromDevice) {
        if (readRegisterFromDevice) {
            readAndStoreSingleRegisterFromDevice(Register.BLOCK_A);
        }

        return registers[Register.BLOCK_A.getAddress()];
    }

    public int getRdsGroupTypeCode() {
        return this.getRdsGroupTypeCode(false);
    }

    private int getRdsGroupTypeCode(boolean readRegisterFromDevice) {
        if (readRegisterFromDevice) {
            readAndStoreSingleRegisterFromDevice(Register.BLOCK_B);
        }

        return Util.valueFromReg(registers[Register.BLOCK_B.getAddress()], RegisterMasks.GROUP_TYPE);
    }

    public int getRdsVersionCode() {
        return this.getRdsVersionCode(false);
    }

    private int getRdsVersionCode(boolean readRegisterFromDevice) {
        if (readRegisterFromDevice) {
            readAndStoreSingleRegisterFromDevice(Register.BLOCK_B);
        }

        return Util.valueFromReg(registers[Register.BLOCK_B.getAddress()], RegisterMasks.VERSION_CODE);
    }

    public int getRdsTrafficProgramIdCode() {
        return this.getRdsTrafficProgramIdCode(false);
    }

    private int getRdsTrafficProgramIdCode(boolean readRegisterFromDevice) {
        if (readRegisterFromDevice) {
            readAndStoreSingleRegisterFromDevice(Register.BLOCK_B);
        }

        return Util.valueFromReg(registers[Register.BLOCK_B.getAddress()], RegisterMasks.TRAFFIC_PROGRAM);
    }

    public int getRdsProgramTypeCode() {
        return this.getRdsProgramTypeCode(false);
    }

    private int getRdsProgramTypeCode(boolean readRegisterFromDevice) {
        if (readRegisterFromDevice) {
            readAndStoreSingleRegisterFromDevice(Register.BLOCK_B);
        }

        return Util.valueFromReg(registers[Register.BLOCK_B.getAddress()], RegisterMasks.PROGRAM_TYPE);
    }

    public RdsBlockErrors getRdsErrorsForBlock(Register block) {
        switch (block) {
            case BLOCK_A:
                return RdsBlockErrors.getRdsBlockErrorsByValue(Util.valueFromReg(registers[block.getAddress()], RegisterMasks.BLERA));
            case BLOCK_B:
                return RdsBlockErrors.getRdsBlockErrorsByValue(Util.valueFromReg(registers[block.getAddress()], RegisterMasks.BLERB));
            default:
                // Return SIX_OR_MORE_ERRORS in the event that an invalid registers is passed as a param
                return RdsBlockErrors.SIX_OR_MORE_ERRORS;
        }
    }

    // end RDS functions
    /**
     * Generates a fancy-formatted version of the register map.
     *
     * @return
     */
    public String getRegisterMapString() {
        StringBuilder regMap = new StringBuilder("|===============|\n");
        regMap.append("| REG  | VALUE  |\n");
        for (int regIdx = 0; regIdx < REGISTER_MAP_DEFAULT_STATE.length; ++regIdx) {
            regMap.append("|---------------|\n");
            regMap.append(String.format("| 0x%02x | 0x%04x |\n", regIdx, registers[regIdx]));
        }
        regMap.append("|===============|");

        return regMap.toString();
    }

    public String getStatusString() {
        readDeviceRegistersAndStoreLocally();

        StringBuilder status = new StringBuilder();

        status.append(String.format("New RDS/RBDS group ready: %s\n", isRdsReady() ? "Yes" : "No"));

        status.append(String.format("Seek/Tune complete: %s\n", isStcComplete() ? "Complete" : "Not complete"));

        status.append(String.format("Seek fail: %s\n", didSeekFail() ? "Failure" : "Successful"));

        status.append(String.format("RDS decoder synchronized: %s\n", isRdsDecoderSynchronized() ? "Synchronized" : "Not synchronized"));

        status.append(String.format("Block E has been found: %s\n", hasBlkEBeenFound() ? "Yes" : "No"));

        status.append(String.format("Audio type: %s\n", isStereoEnabled() ? "Stereo" : "Mono"));

        status.append(String.format("Read channel: %d\n", getReadChannel()));

        status.append(String.format("RSSI: %d\n", getRssi()));

        status.append(String.format("Is this frequency a station: %s\n", isFmTrue() ? "Yes" : "No"));

        status.append(String.format("FM ready: %s\n", isFmReady() ? "Yes" : "No"));

        return status.toString();
    }

    /**
     * If the length parameter is 1, then the function will search for RDS (long
     * mode). Otherwise, no RDS info is shown.
     *
     * @param length
     * @return
     */
    public String generateFreqMap(int length) {
        setMute(true);

        StringBuilder results = new StringBuilder();

        for (int freq = 870; freq <= 1080; freq += 1) { // TODO freq zavisi od band - toto je zrejme pre US_EUR band - prerobit podla aktualneho band
            setFrequency(freq);
            if (length == 1) {
                setRdsMode(false);
                setRdsMode(true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(120);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
            }

            // Generate freq bars
            int rssi = getRssi();
            boolean isFMStation = isFmTrue();
            char[] barBuff = new char[129];
            //std::memset(barBuff, '|', rssi);
            for (int i = 0; i < rssi; i++) {
                barBuff[i] = '|';
            }
            //barBuff[rssi] = '\0';

            //char[] fullBuff = new char[160];
            if (length == 1) {
                //std::sprintf(fullBuff, "Freq: %04u  RDS: %s  RSSI(%03u): %s\n", freq, isRdsDecoderSynchronized() ? "Y" : "N", rssi, barBuff);
                results.append(String.format("Freq: %04d  RDS: %s  RSSI(%03d): %s\n", freq, isRdsDecoderSynchronized() ? "Y" : "N", rssi, new String(barBuff)));
            } else {
                //std::sprintf(fullBuff, "Freq: %04u RSSI(%03u): %s\n", freq, rssi, barBuff);
                results.append(String.format("Freq: %04d, Is FM station: %s, RSSI(%03d): %s\n", freq, isFMStation, rssi, new String(barBuff)));
            }
        }

        return results.toString();
    }

    public String getRdsInfoString() {
        readDeviceRegistersAndStoreLocally();

        StringBuilder status = new StringBuilder();

        status.append(String.format("New RDS/RBDS group ready?: %s\n", isRdsReady() ? "Yes" : "No"));

        status.append(String.format("RDS decoder synchronized?: %s\n", isRdsDecoderSynchronized() ? "Yes" : "No"));

        status.append(String.format("Block E has been found: %s\n", hasBlkEBeenFound() ? "Yes" : "No"));

        status.append(String.format("Program ID Code: 0x%04x\n", getRdsTrafficProgramIdCode()));

        status.append(String.format("Group Type: %02d %s\n", getRdsGroupTypeCode(), Util.boolFromInteger(getRdsVersionCode()) ? "B" : "A"));

        status.append(String.format("Traffic Program?: %s\n", Util.boolFromInteger(getRdsTrafficProgramIdCode()) ? "Yes" : "No"));

        status.append(String.format("Program Type?: %02d\n", getRdsProgramTypeCode()));

        status.append(String.format("Block A Register: 0x%04x\n", getLocalRegisterContent(Register.BLOCK_A)));

        status.append(String.format("Errors on Block A: %s\n", getRdsErrorsForBlock(Register.BLOCK_A).name()));

        status.append(String.format("Block B Register: 0x%04x\n", getLocalRegisterContent(Register.BLOCK_B)));

        status.append(String.format("Errors on Block B: %s\n", getRdsErrorsForBlock(Register.BLOCK_B).name()));

        status.append(String.format("Block C Register: 0x%04x (%c%c)\n", getLocalRegisterContent(Register.BLOCK_C),
                (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_C), RegisterMasks.UINT16_UPPER_BYTE),
                (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_C), RegisterMasks.UINT16_LOWER_BYTE)));

        status.append(String.format("Block D Register: 0x%04x (%c%c)\n", getLocalRegisterContent(Register.BLOCK_D),
                (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_D), RegisterMasks.UINT16_UPPER_BYTE),
                (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_D), RegisterMasks.UINT16_LOWER_BYTE)));

        return status.toString();
    }

    /**
     * Prints the contents block C and D registers when they contain group 2
     * data. The process runs for ms milliseconds. The RDS registers are queried
     * approximately every 10 ms.
     *
     * @param ms
     * @return
     */
    public String snoopRdsGroupTwo(int ms) {
        int msToWait = 10;
        int numRetries = ms / msToWait;

        StringBuilder stringBuilder = new StringBuilder();
        for (int retryIdx = 0; retryIdx < numRetries; ++retryIdx) {
            readDeviceRegistersAndStoreLocally();
            if (isRdsDecoderSynchronized() && getRdsGroupTypeCode() == 2) {
                stringBuilder.append(String.format("%c%c%c%c",
                        (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_C), RegisterMasks.UINT16_UPPER_BYTE),
                        (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_C), RegisterMasks.UINT16_LOWER_BYTE),
                        (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_D), RegisterMasks.UINT16_UPPER_BYTE),
                        (char) Util.valueFromReg(getLocalRegisterContent(Register.BLOCK_D), RegisterMasks.UINT16_LOWER_BYTE)));

                // Toggle RDS. This clears the synchronized flag.
                setRdsMode(false);
                setRdsMode(true);
            }
            try {
                Thread.sleep(msToWait);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
        }

        return stringBuilder.toString();
    }

    public String getI2cInfo() {
        StringBuilder i2cInfo = new StringBuilder();
        i2cInfo.append(String.format("RDA5807M module is on I2C bus: %d\n", i2cBusForRDA5807M.getBusNumber()));
        i2cInfo.append(String.format("RDA5807M module address for access mode to the registers: 0x%02x", i2cDeviceForRDA5807M.getAddress()));

        return i2cInfo.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    private StatusResult setEnabled(boolean enable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(enable), RegisterMasks.ENABLE);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setMute(boolean muteEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(!muteEnable), RegisterMasks.DMUTE);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setHighImpedanceOutput(boolean highImpedanceEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(!highImpedanceEnable), RegisterMasks.DHIZ);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setRdsMode(boolean rdsEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(rdsEnable), RegisterMasks.RDS_EN);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setSoftMute(boolean softMuteEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x04, Util.boolToInteger(softMuteEnable), RegisterMasks.SOFTMUTE_EN);

        return conditionallyWriteRegisterToDevice(Register.REG_0x04, writeResultToDevice);
    }

    private StatusResult setStereo(boolean stereoEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(!stereoEnable), RegisterMasks.DMONO);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setNewMethod(boolean newMethodEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(newMethodEnable), RegisterMasks.NEW_METHOD);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    /**
     * Sets the radio volume. The minimum volume is 0, and the maximum volume is
     * 15.
     */
    private StatusResult setVolume(int volume, boolean writeResultToDevice) {
        setRegister(Register.REG_0x05, volume, RegisterMasks.VOLUME);

        return conditionallyWriteRegisterToDevice(Register.REG_0x05, writeResultToDevice);
    }

    private StatusResult setChannelSpacing(ChannelSpacing channelSpacing, boolean writeResultToDevice) {
        setRegister(Register.REG_0x03, channelSpacing.ordinal(), RegisterMasks.SPACE);

        return conditionallyWriteRegisterToDevice(Register.REG_0x03, writeResultToDevice);
    }

    private StatusResult setBand(Band band, boolean writeResultToDevice) {
        setRegister(Register.REG_0x03, band.ordinal(), RegisterMasks.BAND);

        this.band = band;

        return conditionallyWriteRegisterToDevice(Register.REG_0x03, writeResultToDevice);
    }

    private StatusResult setTune(boolean enable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x03, Util.boolToInteger(enable), RegisterMasks.TUNE);

        return conditionallyWriteRegisterToDevice(Register.REG_0x03, writeResultToDevice);
    }

    private StatusResult setBassBoost(boolean bassBoostEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(bassBoostEnable), RegisterMasks.DBASS);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setSeekDirection(SeekDirection seekDirection, boolean writeResultToDevice) {
        if (seekDirection == SeekDirection.SEEK_DOWN) {
            setRegister(Register.REG_0x02, 0x00, RegisterMasks.SEEKUP);
        } else {
            setRegister(Register.REG_0x02, 0xFF, RegisterMasks.SEEKUP);
        }

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setSeek(boolean seekEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(seekEnable), RegisterMasks.SEEK);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setSeekMode(SeekMode seekMode, boolean writeResultToDevice) {
        if (seekMode == SeekMode.STOP_AT_LIMIT) {
            setRegister(Register.REG_0x02, 0xFF, RegisterMasks.SKMODE);
        } else {
            setRegister(Register.REG_0x02, 0x00, RegisterMasks.SKMODE);
        }

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    private StatusResult setSoftReset(boolean softResetEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x02, Util.boolToInteger(softResetEnable), RegisterMasks.SOFT_RESET);

        return conditionallyWriteRegisterToDevice(Register.REG_0x02, writeResultToDevice);
    }

    // TODO this depends on channel spacing!
    // We assume 100KHz spacing (and US band currently - TOTO UZ NEPLATI), and the channel
    // must be in terms of 10f - that is, 985 for 98.5MHz, for example
    private StatusResult setChannel(int channelValue, boolean writeResultToDevice) {
        int channel = channelValue - this.band.getMinimumFrequency();
        setRegister(Register.REG_0x03, channel, RegisterMasks.CHAN);

        return conditionallyWriteRegisterToDevice(Register.REG_0x03, writeResultToDevice);
    }

    private StatusResult setDeEmphasis(DeEmphasis deEmphasis, boolean writeResultToDevice) {
        setRegister(Register.REG_0x04, deEmphasis.ordinal(), RegisterMasks.DE);

        return conditionallyWriteRegisterToDevice(Register.REG_0x04, writeResultToDevice);
    }

    private StatusResult setAFCD(boolean afcdEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x04, Util.boolToInteger(!afcdEnable), RegisterMasks.AFCD);

        return conditionallyWriteRegisterToDevice(Register.REG_0x04, writeResultToDevice);
    }

    private StatusResult setSoftBlend(boolean softBlendEnable, boolean writeResultToDevice) {
        setRegister(Register.REG_0x07, Util.boolToInteger(softBlendEnable), RegisterMasks.SOFTBLEND_EN);

        return conditionallyWriteRegisterToDevice(Register.REG_0x07, writeResultToDevice);
    }

    ////////////////////////////////////////////////////////////////////////////
    private byte[] prepareWriteData(int data) {
        byte[] preparedData = new byte[2];
        byte lower = (byte) (data >> 8);
        byte upper = (byte) (data & 0xff);
        preparedData[0] = lower;
        preparedData[1] = upper;

        return preparedData;
    }

    private int prepareReadData(byte[] data) {
        return ((data[0] & 0xff) << 8) | (data[1] & 0xff);
    }

    /*
    Note:
        Exception IOException (Input/output error) sa vyskytuje sem tam len pri zapise,
        ale ak su zapisane a precitane data zhodne, tak je mozne tuto vynimku bez problemov ignorovat.
        Preco sa vyskytuje (niekedy), to netusim, ale nieco podobne sa uz riesilo na;
        https://github.com/Pi4J/pi4j/issues/311
     */
    public StatusResult test() {
        byte registerAddress;

        int numberOfBytes;

        int writeWord;
        byte[] writeBytes;

        byte[] readBytes = new byte[2];
        int readWord;

        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        // init
        registerAddress = Register.REG_0x02.getAddress();
        writeWord = 0xc00d;
        System.out.println(String.format("init: register address: 0x%02x", registerAddress));
        /*
        writeBytes = this.prepareWriteData(writeWord);
        numberOfBytes = writeBytes.length;
        System.out.println(String.format("init write: number of bytes: %d, bytes: 0x%02x, 0x%02x, word: 0x%04x", numberOfBytes, writeBytes[0], writeBytes[1], writeWord));
        try {
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            System.out.println("init: Exception at writing occured: " + ioex.getClass().getName() + ": " + ioex.getMessage());
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        try {
            numberOfBytes = i2cDeviceForRDA5807M.read(registerAddress, readBytes, 0, 2);
            readWord = prepareReadData(readBytes);
            System.out.println(String.format("init read: number of bytes: %d, bytes: 0x%02x 0x%02x, word: 0x%04x", numberOfBytes, readBytes[0], readBytes[1], readWord));
        } catch (IOException ioex) {
            System.out.println("init: Exception at reading occured: " + ioex.getMessage());
        }
         */
        registers[registerAddress] = writeWord;
        if (this.writeRegisterToDevice(Register.getRegisterByAddress(registerAddress)) == StatusResult.SUCCESS) {
            readWord = this.readRegisterFromDevice(Register.getRegisterByAddress(registerAddress));
            System.out.println(String.format("init read: 0x%04x", readWord));
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        // volume
        registerAddress = Register.REG_0x05.getAddress();
        writeWord = 0x9080;
        System.out.println(String.format("volume: register address: 0x%02x", registerAddress));
        /*
        writeBytes = this.prepareWriteData(writeWord);
        numberOfBytes = writeBytes.length;
        System.out.println(String.format("volume write: number of bytes: %d, data: 0x%02x, 0x%02x, word: 0x%04x", numberOfBytes, writeBytes[0], writeBytes[1], writeWord));
        try {
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            System.out.println("volume: Exception at writing occured: " + ioex.getMessage());
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        try {
            numberOfBytes = i2cDeviceForRDA5807M.read(registerAddress, readBytes, 0, 2);
            readWord = prepareReadData(readBytes);
            System.out.println(String.format("volume read: number of bytes: %d, bytes: 0x%02x 0x%02x, word: 0x%04x", numberOfBytes, readBytes[0], readBytes[1], readWord));
        } catch (IOException ioex) {
            System.out.println("volume: Exception at reading occured: " + ioex.getMessage());
        }*/
        registers[registerAddress] = writeWord;
        if (this.writeRegisterToDevice(Register.getRegisterByAddress(registerAddress)) == StatusResult.SUCCESS) {
            readWord = this.readRegisterFromDevice(Register.getRegisterByAddress(registerAddress));
            System.out.println(String.format("volume read: 0x%04x", readWord));
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        // band + frequency
        registerAddress = Register.REG_0x03.getAddress();
        writeWord = 0x0fd0;
        System.out.println(String.format("band + frequency: register address: 0x%02x", registerAddress));
        /*
        writeBytes = this.prepareWriteData(writeWord);
        numberOfBytes = writeBytes.length;
        System.out.println(String.format("band + frequency write: number of bytes: %d, data: 0x%02x, 0x%02x, word: 0x%04x", numberOfBytes, writeBytes[0], writeBytes[1], writeWord));
        try {
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            System.out.println("band + frequency: Exception at writing occured: " + ioex.getMessage());
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        try {
            numberOfBytes = i2cDeviceForRDA5807M.read(registerAddress, readBytes, 0, 2);
            readWord = prepareReadData(readBytes);
            System.out.println(String.format("band + frequency read: number of bytes: %d, bytes: 0x%02x 0x%02x, word: 0x%04x", numberOfBytes, readBytes[0], readBytes[1], readWord));
            System.out.println("WARNING: TUNE bit after compelete is set to low/0 -> difference between write and reed data (example: write/read - 0x0fd0/0x0fc0) !!!");
        } catch (IOException ioex) {
            System.out.println("band + frequency: Exception at reading occured: " + ioex.getMessage());
        }
         */
        registers[registerAddress] = writeWord;
        if (this.writeRegisterToDevice(Register.getRegisterByAddress(registerAddress)) == StatusResult.SUCCESS) {
            readWord = this.readRegisterFromDevice(Register.getRegisterByAddress(registerAddress));
            System.out.println(String.format("band + frequency read: 0x%04x", readWord));
        }

        System.out.println("deinit after 30 seconds...");
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        // deinit
        registerAddress = Register.REG_0x02.getAddress();
        writeWord = 0xc00c;
        System.out.println(String.format("deinit: register address: 0x%02x", registerAddress));
        /*
        writeBytes = this.prepareWriteData(writeWord);
        numberOfBytes = writeBytes.length;
        System.out.println(String.format("deinit write: number of bytes: %d, data: 0x%02x, 0x%02x, word: 0x%04x", numberOfBytes, writeBytes[0], writeBytes[1], writeWord));
        try {
            i2cDeviceForRDA5807M.write(registerAddress, writeBytes);
        } catch (IOException ioex) {
            System.out.println("deinit: Exception at writing occured: " + ioex.getMessage());
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }
        try {
            numberOfBytes = i2cDeviceForRDA5807M.read(registerAddress, readBytes, 0, 2);
            readWord = prepareReadData(readBytes);
            System.out.println(String.format("deinit read: number of bytes: %d, bytes: 0x%02x 0x%02x, word: 0x%04x", numberOfBytes, readBytes[0], readBytes[1], readWord));
        } catch (IOException ioex) {
            System.out.println("deinit: Exception at reading occured: " + ioex.getMessage());
        }
         */
        registers[registerAddress] = writeWord;
        if (this.writeRegisterToDevice(Register.getRegisterByAddress(registerAddress)) == StatusResult.SUCCESS) {
            readWord = this.readRegisterFromDevice(Register.getRegisterByAddress(registerAddress));
            System.out.println(String.format("deinit read: 0x%04x", readWord));
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException iex) {
            iex.printStackTrace();
        }

        System.out.println("Closing I2C interface...");
        this.closeI2cInterface();

        return StatusResult.SUCCESS;
    }

    private void setRegisterTest(Register regNum, int value, int mask) {
        int address = regNum.getAddress();

        System.out.println(String.format("setRegister(): register: 0x%02x, value: 0x%04x, mask: 0x%04x", regNum.getAddress(), value, mask));

        // Align the mask's LSB to bit zero
        int shiftAmt = 0; // uint8
        int shiftedMask = mask; // uint16

        while (((shiftedMask & 0x0001) == 0) && (shiftAmt < 16)) {
            shiftedMask >>= 1;
            ++shiftAmt;
        }

        System.out.println("shiftedMask: " + Integer.toBinaryString(shiftedMask));

        // AND the mask with the value in the mask position
        int maskAndValue = shiftedMask & value;

        System.out.println("maskAndValue: " + Integer.toBinaryString(maskAndValue));

        // Shift the mask ANDed with value back to the original mask position
        maskAndValue <<= shiftAmt;
        System.out.println("shiftAmt: " + Integer.toBinaryString(shiftAmt));
        System.out.println("maskAndValue after maskAndValue <<= shiftAmt: " + Integer.toBinaryString(maskAndValue));

        // Create a working copy of the register we intend to modify
        int regTemp = registers[address];

        // ANDing with the negation of mask will set zeroes to the mask region
        // but won't modify any other bits
        regTemp &= ~mask;

        // ORing maskAndValue with regTemp will apply the changes to the masked
        // region in regTemp, but won't impact any other bits in regTemp
        regTemp |= maskAndValue;

        // Voila!
        registers[address] = regTemp;
    }

    public StatusResult test1() {
        Register register = Register.REG_0x02;
        int address = register.getAddress();

        // set init value for register 0x02
        registers[address] = 0x0000;

        System.out.println(String.format("Set register device 0x%02x to 0x%04x", address, registers[address]));
        System.out.println("Writing to device...");
        StatusResult writeStatusResult = writeRegisterToDevice(register);
        System.out.println("writeStatusResult: " + writeStatusResult.name());

        // set High Impedance Output to false (1 = Normal operation) // bit 15
        int highImpedanceOutputValue = 0x0001;
        int highImpedanceOutputMask = RegisterMasks.DHIZ;
        // start setRegister()
        this.setRegisterTest(register, highImpedanceOutputValue, highImpedanceOutputMask);
        // end setRegister()

        // set mute to false (1 = Normal operation) // bit 14
        int muteValue = 0x0001;
        int muteMask = RegisterMasks.DMUTE;
        // start setRegister()
        this.setRegisterTest(register, muteValue, muteMask);
        // end setRegister()

        // set stereo to true (0 = stereo) // bit 13
        int stereoValue = 0x0000;
        int stereoMask = RegisterMasks.DMONO;
        // start setRegister()
        this.setRegisterTest(register, stereoValue, stereoMask);
        // end setRegister()

        // Bass Boost. 0 = Disabled; 1 = Bass boost enabled. Default is disabled (0). // bit 12
        // 0 // bit 11
        // 0 // bit 10
        // 0 // bit 9
        // 0 // bit 8
        // 0 // bit 7
        // 000 // bit 6, 5, 4
        // set RDS mode to true (If 1, rds/rbds enable) // bit 3
        int rdsModeValue = 0x0001;
        int rdsModeMask = RegisterMasks.RDS_EN;
        // start setRegister()
        this.setRegisterTest(register, rdsModeValue, rdsModeMask);
        // end setRegister()

        // set new method to true (New Demodulate Method Enable, can improve the receive sensitivity about 1dB. Default is disabled (0).) // bit 2
        int newMethodValue = 0x0001;
        int newMethodMask = RegisterMasks.NEW_METHOD;
        // start setRegister()
        this.setRegisterTest(register, newMethodValue, newMethodMask);
        // end setRegister()

        // 0 (Soft reset. If 0, not reset; If 1, reset. Default is not reset (0).) // bit 2
        // set enable to true (Power Up Enable. 0 = Disabled; 1 = Enabled. Deafult is disabled (0). // bit 1
        int enabledValue = 0x0001;
        int enabledMask = RegisterMasks.ENABLE;
        // start setRegister()
        this.setRegisterTest(register, enabledValue, enabledMask);
        // end setRegister()

        System.out.println("Writing to device...");
        writeStatusResult = writeRegisterToDevice(register);
        System.out.println("writeStatusResult: " + writeStatusResult.name());

        System.out.println("Reading from device...");
        int readData = readRegisterFromDevice(register);
        System.out.println(String.format("readData: 0x%04x", readData));

        System.out.println("----------------------------------------------------------------------------------");

        System.out.println("Set volume");
        StatusResult statusResultVolume = setVolume(7, true);
        System.out.println("Set volume result: " + statusResultVolume.name());
        System.out.println("Reading from device...");
        readData = readRegisterFromDevice(Register.REG_0x05);
        System.out.println(String.format("readData: 0x%04x", readData));

        System.out.println("----------------------------------------------------------------------------------");

        register = Register.REG_0x03;
        address = register.getAddress();

        // set init value for register 0x03
        registers[address] = 0x0000;

        System.out.println(String.format("Set register device 0x%02x to 0x%04x", address, registers[address]));
        System.out.println("Writing to device...");
        writeStatusResult = writeRegisterToDevice(register);
        System.out.println("writeStatusResult: " + writeStatusResult.name());

        // channel spacing + band + set frequency
        System.out.println("Set Channel Spacing");
        setChannelSpacing(ChannelSpacing.ONE_HUNDRED_KHZ.getValueInKHz());
        System.out.println("Set Band");
        setBand(Band.US_EUR.ordinal());
        System.out.println("Set frequency");
        setFrequency(933);

        System.out.println("Reading from device...");
        readData = readRegisterFromDevice(register);
        System.out.println(String.format("readData: 0x%04x", readData));

        return StatusResult.SUCCESS;
    }

    public void setSeekMode(SeekMode seekMode) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    ////////////////////////////////////////////////////////////////////////////
    // Program Service Name
    char[] psName1 = new char[8];
    char[] psName2 = new char[8];
    //char[] programServiceName = new char[8]; // found station name or empty. Is max. 8 character long. 

    public void printStationName(int block2, RdsBlockErrors rdsBlock2Errors, int block4, RdsBlockErrors rdsBlock4Errors, int rdsGroupTypeCode, boolean isRdsDecoderSynchronized) {
        int idx; // index of rdsText
        int c1, c2;

        //System.out.println(String.format("       0x%02x", rdsGroupTypeCode));
        //if (block1 == 0 /*isRdsDecoderSynchronized*/) {
        //    programServiceName = new char[8];
        //    return;
        //}
        // analyzing Block 2
        //int rdsGroupType = this.getRdsGroupTypeCode();
        //System.out.println(String.format("0x%02x", rdsGroupType));
        if (rdsGroupTypeCode == 0) {
            // The data received is part of the Service Station Name 
            idx = 2 * (block2 & 0x03);
            //System.out.println("--------------------------------------------------- idx: " + idx);

            // new data is 2 chars from block 4
            c1 = block4 >> 8;
            c2 = block4 & 0x00ff;

            //System.out.println(c1 + "-" + c2);
            // check that the data was received successfully twice
            // before publishing the station name
            if ((psName1[idx] == ((char) c1)) && (psName1[idx + 1] == ((char) c2))) {
                // retrieved the text a second time: store to psName2
                psName2[idx] = (char) c1;
                psName2[idx + 1] = (char) c2;
                if (Arrays.equals(psName1, psName2)) {
                    int n = 0;
                    for (int i = 0; i < 8; i++) { // remove non-printable error ASCCi characters
                        if (psName2[i] > 31 && psName2[i] < 127) {
                            programServiceName[n] += psName2[i];
                            n++;
                        }
                    }
                    //System.out.println(new String(programServiceName));
                    //return;
                    /*
                    if (isRdsDecoderSynchronized) {
                        programServiceName = new char[8];
                        return;
                    }
                     */
                }
            }

            if ((psName1[idx] != (char) c1) || (psName1[idx + 1] != (char) c2)) {
                psName1[idx] = (char) c1;
                psName1[idx + 1] = (char) c2;
            }
        }

        System.out.println(new String(programServiceName));
        //return new String(programServiceName);
    }
    ////////////////////////////////////////////////////////////////////////////

    String stationName;

    public void readCorrectRDS(long timeout) {
        readDeviceRegistersAndStoreLocally();

        //int block1 = readRegisterFromDevice(Register.BLOCK_A);
        int block1 = getLocalRegisterContent(Register.BLOCK_A);
        //int block2 = readRegisterFromDevice(Register.BLOCK_B);
        int block2 = getLocalRegisterContent(Register.BLOCK_B);
        //int block4 = readRegisterFromDevice(Register.BLOCK_D);
        int block4 = getLocalRegisterContent(Register.BLOCK_D);

        boolean isRdsReady = isRdsReady();
        boolean isRdsDecoderSynchronized = isRdsDecoderSynchronized();

        if (block1 == 0) {
            // reset all the RDS info.
            //RDSinit();
            psName1 = new char[8];
            psName2 = new char[8];
            stationName = "";
            // Send out empty data
            //if (sendServiceName) sendServiceName(StationName);
            System.out.println("Station: " + stationName);
            //if (sendText)        sendText("");
            return;
        }

        int rdsGroupTypeCode = getRdsGroupTypeCode();

        //System.out.println(String.format("Group Type: 0x%02x", rdsGroupType));
        if (rdsGroupTypeCode == 0x0a || rdsGroupTypeCode == 0x0b) {
            // The data received is part of the Service Station Name 

            int idx = 2 * (block2 & 0x0003);
            //// new data is 2 chars from block 4
            char c1 = toChar((block4 >> 8) & 0xff);
            char c2 = toChar(block4 & 0xff);
            // check that the data was received successfully twice
            // before publishing the station name
            if ((psName1[idx] == c1) && (psName1[idx + 1] == c2)) {
                // retrieved the text a second time: store to _PSName2
                psName2[idx] = c1;
                psName2[idx + 1] = c2;
                if ((idx == 6) && Arrays.equals(psName1, psName2)) {
                    if (stationName == null || !Arrays.equals(psName2, stationName.toCharArray())) {
                        // publish station name
                        if (psName2.length < 9) {
                            stationName = new String(psName2);
                            System.out.println("Station: " + stationName);
                        }
                    }
                }
            }
            if ((psName1[idx] != c1) || (psName1[idx + 1] != c2)) {
                psName1[idx] = c1;
                psName1[idx + 1] = c2;
            }
        }

        /*
            maskedValue = Util.valueFromReg(registers[Register.REG_0x0A.getAddress()], RegisterMasks.RDSS);
            boolean isRdsDecoderSynchronized = (maskedValue != 0);
            
            if (isRdsReady) {
                if (groupTypeCode == 0) {
                
                }   
            }
            
            System.out.println(new String(buffer));
         */
    }

    Text text = new Text(8);

    public void printStation() {
        readDeviceRegistersAndStoreLocally();

        //int block1 = readRegisterFromDevice(Register.BLOCK_A);
        int block1 = getLocalRegisterContent(Register.BLOCK_A);
        //int block2 = readRegisterFromDevice(Register.BLOCK_B);
        int block2 = getLocalRegisterContent(Register.BLOCK_B);
        //int block4 = readRegisterFromDevice(Register.BLOCK_D);
        int block4 = getLocalRegisterContent(Register.BLOCK_D);

        boolean isRdsReady = isRdsReady();
        boolean isRdsDecoderSynchronized = isRdsDecoderSynchronized();

        int addr = block2 & 3;

        if (block1 == 0) {
            System.out.println("RESET******************************************");
            text.reset();
        }
        
        //RdsBlockErrors errorsA = getRdsErrorsForBlock(Register.BLOCK_A);
        //RdsBlockErrors errorsB = getRdsErrorsForBlock(Register.BLOCK_B);
        //System.out.println(String.format("errorsA: 0x%02x", errorsA.ordinal()));
        //System.out.println(String.format("errorsB: 0x%02x", errorsB.ordinal()));
        //int rdsGroupType = 0x0A | ((block2 & 0xF000) >> 8) | ((block2 & 0x0800) >> 11);
        int rdsGroupTypeCode = getRdsGroupTypeCode();
        //System.out.println(String.format("RDS Group Type Code: 0x%02x", rdsGroupTypeCode));
        int rdsVersionCode = getRdsVersionCode();
        //System.out.println(String.format("RDS Version Code: 0x%02x", rdsVersionCode));
        // Groups 0A & 0B: to extract PS segment we need blocks 1 and 3
        if ((rdsGroupTypeCode == 0x0a || rdsGroupTypeCode == 0x0b) && (rdsVersionCode == 0x00 || rdsVersionCode == 0x01)) {
            if (/*errorsA == RdsBlockErrors.ZERO_ERRORS &&*/ /*errorsB == RdsBlockErrors.ZERO_ERRORS*/ true) {
                System.out.println(String.format("0x%04x", block4));
                
                char ch1 = toChar((block4 >> 8) & 0xff); // (block4 >> 8) & 0xff
                char ch2 = toChar(block4 & 0xff); // (data[0] & 0xff) << 8) | (data[1] & 0xff)
                //if (ch1 <= 127 && ch2 <= 127) {
                    System.out.println("PS pos=" + addr + ": \"" + ch1 + ch2 + "\" ");
                    text.setChars(addr, ch1, ch2);
                //}
                //System.out.println(text.getMostFrequentText());
                
            }
        }
    }

    private final static char CTRLCHAR = '\u2423';

    private final static char[] charmap = new char[]{
        CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR,
        CTRLCHAR, CTRLCHAR, '\u240A', '\u240B', CTRLCHAR, '\u21B5', CTRLCHAR, CTRLCHAR,
        CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR,
        CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, CTRLCHAR, '\u241F',
        '\u0020', '\u0021', '\u0022', '\u0023', '\u00A4', '\u0025', '\u0026', '\'',
        '\u0028', '\u0029', '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
        '\u0030', '\u0031', '\u0032', '\u0033', '\u0034', '\u0035', '\u0036', '\u0037',
        '\u0038', '\u0039', '\u003A', '\u003B', '\u003C', '\u003D', '\u003E', '\u003F',
        '\u0040', '\u0041', '\u0042', '\u0043', '\u0044', '\u0045', '\u0046', '\u0047',
        '\u0048', '\u0049', '\u004A', '\u004B', '\u004C', '\u004D', '\u004E', '\u004F',
        '\u0050', '\u0051', '\u0052', '\u0053', '\u0054', '\u0055', '\u0056', '\u0057',
        '\u0058', '\u0059', '\u005A', '\u005B', '\\',     '\u005D', '\u2015', '\u005F',
        '\u2551', '\u0061', '\u0062', '\u0063', '\u0064', '\u0065', '\u0066', '\u0067',
        '\u0068', '\u0069', '\u006A', '\u006B', '\u006C', '\u006D', '\u006E', '\u006F',
        '\u0070', '\u0071', '\u0072', '\u0073', '\u0074', '\u0075', '\u0076', '\u0077',
        '\u0078', '\u0079', '\u007A', '\u007B', '\u007C', '\u007D', '\u00AF', '\u007F',
        '\u00E1', '\u00E0', '\u00E9', '\u00E8', '\u00ED', '\u00EC', '\u00F3', '\u00F2',
        '\u00FA', '\u00F9', '\u00D1', '\u00C7', '\u015E', '\u00DF', '\u00A1', '\u0132',
        '\u00E2', '\u00E4', '\u00EA', '\u00EB', '\u00EE', '\u00EF', '\u00F4', '\u00F6',
        '\u00FB', '\u00FC', '\u00F1', '\u00E7', '\u015F', '\u011F', '\u0131', '\u0133',
        '\u00AA', '\u03B1', '\u00A9', '\u2030', '\u011E', '\u011B', '\u0148', '\u0151',
        '\u03C0', '\u20AC', '\u00A3', '\u0024', '\u2190', '\u2191', '\u2192', '\u2193',
        '\u00BA', '\u00B9', '\u00B2', '\u00B3', '\u00B1', '\u0130', '\u0144', '\u0171',
        '\u00B5', '\u00BF', '\u00F7', '\u00B0', '\u00BC', '\u00BD', '\u00BE', '\u00A7',
        '\u00C1', '\u00C0', '\u00C9', '\u00C8', '\u00CD', '\u00CC', '\u00D3', '\u00D2',
        '\u00DA', '\u00D9', '\u0158', '\u010C', '\u0160', '\u017D', '\u0110', '\u013F',
        '\u00C2', '\u00C4', '\u00CA', '\u00CB', '\u00CE', '\u00CF', '\u00D4', '\u00D6',
        '\u00DB', '\u00DC', '\u0159', '\u010D', '\u0161', '\u017E', '\u0111', '\u0140',
        '\u00C3', '\u00C5', '\u00C6', '\u0152', '\u0177', '\u00DD', '\u00D5', '\u00D8',
        '\u00DE', '\u014A', '\u0154', '\u0106', '\u015A', '\u0179', '\u0166', '\u00F0',
        '\u00E3', '\u00E5', '\u00E6', '\u0153', '\u0175', '\u00FD', '\u00F5', '\u00F8',
        '\u00FE', '\u014B', '\u0155', '\u0107', '\u015B', '\u017A', '\u0167', CTRLCHAR,};

    private static char toChar(int code) {
        return charmap[code];
    }

    public void checkRDS() {
        
        t = new Thread(() -> {
            while (true) {
                
                ////////////////////////////////////////////////////////////////
                // DEBUG_FUNC0("checkRDS");
                //System.out.println("checkRDS");
        
                // check RDS data if there is a listener !
                //if (_sendRDS) {

                    // check register A
                    //Wire.requestFrom(I2C_SEQ, 2);
                    //registers[RADIO_REG_RA] = _read16();
                    //Wire.endTransmission();
            
                    readAndStoreSingleRegisterFromDevice(Register.REG_0x0A);
                    // nahrada
                    /*
                    int numberOfBytesRead;
                    byte rcv[] = new byte[12];
                    try {
                        numberOfBytesRead = i2cDeviceForRDA5807M.read(Register.REG_0x0A.getAddress(), rcv, 0, 12);
                        System.out.println("number of bytes read: " + numberOfBytesRead);
                    } catch (IOException ioex) {
                        System.out.println(ioex.getMessage());
                    }
                    
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    */
                    // nahrada
            
                    //if (registers[RADIO_REG_RA] & RADIO_REG_RA_RDSBLOCK) {
                    //    DEBUG_STR("BLOCK_E found.");
                    //} // if
            
                    if ((registers[Register.REG_0x0A.getAddress()] & RegisterMasks.BLK_E) != 0) {
                        System.out.println("BLOCK_E found.");
                    } // if

                    /*
                    if (registers[RADIO_REG_RA] & RADIO_REG_RA_RDS) {
                        // check for new RDS data available
                        uint16_t newData;
                        bool result = false;

                        Wire.beginTransmission(I2C_INDX);                // Device 0x11 for random access
                        Wire.write(RADIO_REG_RDSA);                      // Start at Register 0x0C
                        Wire.endTransmission(0);                         // restart condition

                        Wire.requestFrom(I2C_INDX, 8,; 1)                // Retransmit device address with READ, followed by 8 bytes
                        newData = _read16();
                        if (newData != registers[RADIO_REG_RDSA]) { registers[RADIO_REG_RDSA] = newData; result = true; }

                        newData = _read16();
                        if (newData != registers[RADIO_REG_RDSB]) { registers[RADIO_REG_RDSB] = newData; result = true; }

                        newData = _read16();
                        if (newData != registers[RADIO_REG_RDSC]) { registers[RADIO_REG_RDSC] = newData; result = true; }

                        newData = _read16();
                        if (newData != registers[RADIO_REG_RDSD]) { registers[RADIO_REG_RDSD] = newData; result = true; }

                        Wire.endTransmission();
                        // _printHex(registers[RADIO_REG_RDSA]); _printHex(registers[RADIO_REG_RDSB]);
                        // _printHex(registers[RADIO_REG_RDSC]); _printHex(registers[RADIO_REG_RDSD]);
                        // Serial.println();

                        if (result) {
                            // new data in the registers
                            // send to RDS decoder
                            _sendRDS(registers[RADIO_REG_RDSA], registers[RADIO_REG_RDSB], registers[RADIO_REG_RDSC], registers[RADIO_REG_RDSD]);
                        } // if
                    } // if
                    */

                    if ((registers[Register.REG_0x0A.getAddress()] & RegisterMasks.RDSR) != 0) {
                        //System.out.println("in block for processing newData");
                        // check for new RDS data available
                        int newData;
                        boolean result = false;

                        //writeRegisterToDevice(Register.BLOCK_A); // ???

                        readDeviceRegistersAndStoreLocally(); // ???
                        // nahrada
                        /*
                        int numberOfBytesRead;
                        byte rcv[] = new byte[12];
                        try {
                            numberOfBytesRead = i2cDeviceForRDA5807M.read(Register.REG_0x0A.getAddress(), rcv, 0, 12);
                            System.out.println("number of bytes read: " + numberOfBytesRead);
                        } catch (IOException ioex) {
                            System.out.println(ioex.getMessage());
                        }
                        */
                        // nahrada
                        
                        try {                
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        newData = readRegisterFromDevice(Register.BLOCK_A);
                        if (newData != registers[Register.BLOCK_A.getAddress()]) {
                            registers[Register.BLOCK_A.getAddress()] = newData;
                            result = true;
                        }

                        newData = readRegisterFromDevice(Register.BLOCK_B);
                        if (newData != registers[Register.BLOCK_B.getAddress()]) {
                            registers[Register.BLOCK_B.getAddress()] = newData;
                            result = true;
                        }

                        newData = readRegisterFromDevice(Register.BLOCK_C);
                        if (newData != registers[Register.BLOCK_C.getAddress()]) {
                            registers[Register.BLOCK_C.getAddress()] = newData;
                            result = true;
                        }

                        newData = readRegisterFromDevice(Register.BLOCK_D);
                        if (newData != registers[Register.BLOCK_D.getAddress()]) {
                            registers[Register.BLOCK_D.getAddress()] = newData;
                            result = true;
                        }

                        if (result) {
                            // new data in the registers
                            // send to RDS decoder
                            //_sendRDS(registers[RADIO_REG_RDSA], registers[RADIO_REG_RDSB], registers[RADIO_REG_RDSC], registers[RADIO_REG_RDSD]);
                            rdsParser.processRDSData(registers[Register.BLOCK_A.getAddress()], registers[Register.BLOCK_B.getAddress()], registers[Register.BLOCK_C.getAddress()], registers[Register.BLOCK_D.getAddress()]);
                        } // if
                    } // if
                //}
                ////////////////////////////////////////////////////////////////
            }
        });

        t.setDaemon(true);
        t.start();
    }
    
    // Program Service Name
    char[] _PSName1 = new char[8];
    char[] _PSName2 = new char[8];
    char[] programServiceName = new char[8]; // found station name or empty. Is max. 8 character long.
    
    private void init1() {
        _PSName1 = "--------".toCharArray();
        _PSName2 = _PSName1;
        programServiceName = "        ".toCharArray();
        //memset(_RDSText, 0, sizeof(_RDSText));
        //_lastTextIDX = 0;
    } // init()
    
    private void processData(int block1, int block2, int block3, int block4) {
        // DEBUG_FUNC0("process");
        //System.out.println("process");
        //uint8_t  idx; // index of rdsText
        int idx; // index of rdsText
        //char c1, c2;
        char c1, c2;
        //char *p;
        // ???
        
        //uint16_t mins; ///< RDS time in minutes
        //int mins; ///< RDS time in minutes
        //uint8_t off;   ///< RDS time offset and sign
        //int off;  ///< RDS time offset and sign

        // Serial.print('('); Serial.print(block1, HEX); Serial.print(' '); Serial.print(block2, HEX); Serial.print(' '); Serial.print(block3, HEX); Serial.print(' '); Serial.println(block4, HEX);

        if (block1 == 0) {
            // reset all the RDS info.
            init1();
            // Send out empty data
            //if (_sendServiceName) _sendServiceName(programServiceName
            System.out.println("RDS: " + new String(programServiceName));
            //if (_sendText)        _sendText("");
            return;
        } // if

        // analyzing Block 2
        int rdsGroupType = 0x0A | ((block2 & 0xF000) >> 8) | ((block2 & 0x0800) >> 11);
        //rdsTP = (block2 & 0x0400);
        //rdsPTY = (block2 & 0x0400);

        switch (rdsGroupType) {
            case 0x0A:
            case 0x0B:
                //System.out.println(String.format("0x%02x", rdsGroupType));
                // The data received is part of the Service Station Name 
                idx = 2 * (block2 & 0x0003);
                //System.out.println("idx: " + idx);
                
                // new data is 2 chars from block 4
                c1 = (char)(block4 >> 8);
                //System.out.println("c1: " + c1);
                c2 = (char) (block4 & 0x00FF);
                //System.out.println("c2: " + c2);

                System.out.println("_PSName1: " + new String(_PSName1));
                System.out.println("_PSName2: " + new String(_PSName2));
                
                // check that the data was received successfully twice
                // before publishing the station name

                if ((_PSName1[idx] == c1) && (_PSName1[idx + 1] == c2)) {
                    // retrieved the text a second time: store to _PSName2
                    _PSName2[idx] = c1;
                    _PSName2[idx + 1] = c2;
                    //_PSName2[8] = '\0';

                    if ((idx == 6) && Arrays.equals(_PSName1, _PSName2)) {
                        System.out.println("**************************************************************");
                        System.out.println("_PSName2: " + new String(_PSName2));
                        System.out.println("programServiceName: " + new String(programServiceName));
                        System.out.println("**************************************************************");
                        if (!Arrays.equals(_PSName2, programServiceName)) {
                            // publish station name
                            //strcpy(programServiceName, _PSName2);
                            System.arraycopy(_PSName2, 0, programServiceName, 0, _PSName2.length);
                            //if (_sendServiceName)
                            //    _sendServiceName(programServiceName);
                            System.out.println("******************************************************************** RDS: " + new String(programServiceName));
                        } // if
                    } // if
                } // if

                if ((_PSName1[idx] != c1) || (_PSName1[idx + 1] != c2)) {
                    _PSName1[idx] = c1;
                    _PSName1[idx + 1] = c2;
                    //_PSName1[8] = '\0';
                    // Serial.println(_PSName1);
                } // if
                break;
            
            /*
            case 0x2A:
                // The data received is part of the RDS Text.
                _textAB = (block2 & 0x0010);
                idx = 4 * (block2 & 0x000F);

                if (idx < _lastTextIDX) {
                    // the existing text might be complete because the index is starting at the beginning again.
                    // now send it to the possible listener.
                    if (_sendText)
                        _sendText(_RDSText);
                }
                _lastTextIDX = idx;

                if (_textAB != _last_textAB) {
                    // when this bit is toggled the whole buffer should be cleared.
                    _last_textAB = _textAB;
                    memset(_RDSText, 0, sizeof(_RDSText));
                    // Serial.println("T>CLEAR");
                } // if


                // new data is 2 chars from block 3
                _RDSText[idx] = (block3 >> 8);     idx++;
                _RDSText[idx] = (block3 & 0x00FF); idx++;

                // new data is 2 chars from block 4
                _RDSText[idx] = (block4 >> 8);     idx++;
                _RDSText[idx] = (block4 & 0x00FF); idx++;

                // Serial.print(' '); Serial.println(_RDSText);
                // Serial.print("T>"); Serial.println(_RDSText);
                break;
            */

            /*
            case 0x4A:
                // Clock time and date
                off = (block4)& 0x3F; // 6 bits
                mins = (block4 >> 6) & 0x3F; // 6 bits
                mins += 60 * (((block3 & 0x0001) << 4) | ((block4 >> 12) & 0x0F));

                // adjust offset
                if (off & 0x20) {
                    mins -= 30 * (off & 0x1F);
                } else {
                    mins += 30 * (off & 0x1F);
                }

                if ((_sendTime) && (mins != _lastRDSMinutes)) {
                    _lastRDSMinutes = mins;
                    _sendTime(mins / 60, mins % 60);
                } // if
                break;
            */

            case 0x6A:
                // IH
                break;

            case 0x8A:
                // TMC
                break;

            case 0xAA:
                // TMC
                break;

            case 0xCA:
                // TMC
                break;

            case 0xEA:
                // IH
                break;

            default:
                // Serial.print("RDS_GRP:"); Serial.println(rdsGroupType, HEX);
                break;
        }
    } // processData()

    private String station;
    
    @Override
    public void onRDSObjectChanged(RDSEvent rdsEvent) {
        RDSObject rdsObject = rdsEvent.getRDSObject();
        //System.out.println(rdsObject.toString());
        if (/*(rdsObject.getNewType() != rdsObject.getOldType()) &&*/ rdsObject.getNewValue() != null && (!rdsObject.getNewValue().equals(rdsObject.getOldValue()))) {
            //System.out.println("*** OLD -> NEW ***");
            if (!rdsObject.getNewValue().equals(station)) {
                station = rdsObject.getNewValue();
                System.out.println("**************************** " + station + " ****************************");
                this.setFrequency(972);
            }
        } else {
            //System.out.println("no chng");
        }
    }
}
