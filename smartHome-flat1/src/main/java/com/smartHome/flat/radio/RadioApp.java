/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.smartHome.flat.radio.rda5807.RDA5807M;
import com.smartHome.flat.radio.rda5807.Register;
import com.smartHome.flat.radio.rda5807.RegisterMasks;
import com.smartHome.flat.radio.rda5807.StatusResult;
import com.smartHome.flat.radio.rda5807.util.Util;

/**
 *
 * @author 
 */
public class RadioApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("========== START ==========");
        System.out.println("Grove - I2C FM Receiver module (by Seeed) based on RDA5807M single-chip (by RDA Microelectronics Inc.)");

        // init
        RDA5807M radio = new RDA5807M();
        System.out.println(radio.getI2cInfo());
        StatusResult statusResult = null;

        // power up
        System.out.println("Power Up");
        System.out.println("Power Up result: " + radio.powerUp().name());
        
        System.out.println("specialInit() START");
        radio.specialInit();
        System.out.println("specialInit() END");
        
        /*
        System.out.println(String.format("01H: %s", radio.getLocalCopyOfRegister(Register.REG_0x01.getAddress())));
        System.out.println(String.format("02H: %s", radio.getLocalCopyOfRegister(Register.REG_0x02.getAddress())));
        System.out.println(String.format("03H: %s", radio.getLocalCopyOfRegister(Register.REG_0x03.getAddress())));
        System.out.println(String.format("04H: %s", radio.getLocalCopyOfRegister(Register.REG_0x04.getAddress())));
        System.out.println(String.format("05H: %s", radio.getLocalCopyOfRegister(Register.REG_0x05.getAddress())));
        System.out.println(String.format("06H: %s", radio.getLocalCopyOfRegister(Register.REG_0x06.getAddress())));
        System.out.println(String.format("07H: %s", radio.getLocalCopyOfRegister(Register.REG_0x07.getAddress())));
        
        System.out.println(String.format("020H: 0x%04x", radio.readRegisterFromDevice(Register.REG_0x20))); // 20H=32DEC
        */
        
        //System.out.println(radio.getRegisterMapString());
        
        //System.out.println(radio.generateFreqMap(1));
        
        // get chip id
        // TODO dat to potom do nejakej metody (pre RDA5807M je mozne aj nastavovat tuto hodnotu - preverit)
        System.out.println("CHIPID reading...");
        int readResult = radio.readRegisterFromDevice(Register.REG_0x00);
        if (readResult > 0) {
            System.out.println(String.format("CHIPID: 0x%04x", Util.valueFromReg(readResult, RegisterMasks.CHIP_ID)));
        } else {
            System.out.println("CHIPID result: " + readResult);
        }
        
        // set mute to false -> set unmute
        System.out.println("set unmute");
        statusResult = radio.setMute(false);
        if (statusResult != StatusResult.SUCCESS) {
            System.out.println("set unmute result: " + statusResult.name());
        } else {
            // set volume
            int volume = 1;
            System.out.println("set volume");
            statusResult = radio.setVolume(volume);
            if (statusResult != StatusResult.SUCCESS) {
                System.out.println(String.format("Set volume to %d, result: %s", volume, statusResult.name()));
            }
        }
        
            List<Integer> tunedFrequencies = new ArrayList();
            
            ////////////////////////////////////////////////////////////////////
            // AUTO TUNE (1)
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            /*
            System.out.println("Scanning all available frequencies... (1)");
            int stationId = 0;
            int f = radio.getBand().getMinimumFrequency();
            // start Simple Scan: all channels
            while (f <= radio.getBand().getMaximumFrequency()) {
                radio.setFrequency(f);
                //delay(50);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }

                if (radio.isFmTrue()) { // popripade este dat podmienku ze rssi > xxx
                    System.out.println("--------------------------------------------------------------------");
                    System.out.println("stationId: " + (++stationId));
                    //radio.formatFrequency(sFreq, sizeof(sFreq));
                    System.out.println("frequency: " + radio.getReadChannel());
                    System.out.println("rssi:" + radio.getRssi());
                    //System.out.println("snr: " + "<snr>"); // neviem snr zistit v pripade RDA5807M
                    System.out.println("stereo: " + radio.isStereoEnabled());
                    System.out.println("rds: " + radio.isRdsReady());
                    System.out.println("--------------------------------------------------------------------");
                }

                // tune up by 1 step
                f += 1;
            }
            */
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            
            ////////////////////////////////////////////////////////////////////
            // AUTO TUNE (2)
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            
            System.out.println("Seeking all frequencies... (2)");
            // set seek mode
            System.out.println("set seek mode to: 0 - STOP_AT_LIMIT");
            statusResult = radio.setSeekMode(0); // 0 - STOP_AT_LIMIT; 1 - WRAP_AT_LIMIT
            System.out.println("set seek mode result: " + statusResult.name());
            // set seek direction
            System.out.println("set seek direction to: 1 - SEEK UP");
            statusResult = radio.setSeekDirection(1); // 0 - SEEK_DOWN; 1 - SEEK UP
            System.out.println("set seek direction result: " + statusResult.name());
                        
            int fLast = 0;
            int f = radio.getBand().getMinimumFrequency();
            long startSeek; // after 300 msec must be tuned. after 500 msec must have RDS.
            while (f <= radio.getBand().getMaximumFrequency()) {
                radio.setSeek(true);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
                startSeek = System.currentTimeMillis();

                // wait for seek complete
                boolean isStcComplete;
                boolean didSeekFail;
                boolean isFmTrue;
                boolean cond;
                do {
                    //try {
                    //    Thread.sleep(35);
                    //} catch (InterruptedException iex) {
                    //    iex.printStackTrace();
                    //}

                    isStcComplete = radio.isStcComplete();
                    didSeekFail = radio.didSeekFail();
                    isFmTrue = radio.isFmTrue();
                    cond = isStcComplete && !didSeekFail && isFmTrue;
                } while (!cond && ((startSeek + 1500) > System.currentTimeMillis()));
                
                // check frequency
                f = radio.getReadChannel();
                if (f < fLast) {
                    System.out.println("/// BREAK /// " + f);
                    break;
                }
                fLast = f;
                
                System.out.println("--------------------------------------------------------------------");
                System.out.println("--- " + fLast + " ---");
                System.out.println("seek fail: " + radio.didSeekFail());
                System.out.println("RSSI: " + radio.getRssi());
                int val = radio.readRegisterFromDevice(Register.REG_0x0A);
                System.out.println(String.format("0x%04x | %s", val, Integer.toBinaryString(val)));
                System.out.println(radio.getStatusString());
                System.out.println("--------------------------------------------------------------------");
                
                if (cond) {
                    if (tunedFrequencies.isEmpty()) {
                        tunedFrequencies.add(fLast);
                    } else {
                        int fLastF = fLast;
                        Optional<Integer> tunedFrequency = tunedFrequencies.stream().filter(freq -> (freq == fLastF)).findAny();
                        if (!tunedFrequency.isPresent()) {
                            tunedFrequencies.add(fLastF);
                        }
                    }
               }
            }
            
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            
            // Vypis vsetkych naladenych stanic
            System.out.println("*******************************************************");
            System.out.println("Tuned frequencies:");
            tunedFrequencies.forEach((tunedFrequency) -> {
                System.out.println(tunedFrequency);
                /*
                System.out.println("... listen station 10 seconds ...");
                StatusResult statusResultF = radio.setFrequency(tunedFrequency);
                System.out.println("set station: " + statusResultF.name());
                try {
                    Thread.sleep(9_500);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
                System.out.println(radio.getStatusString());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
                */
            });
            System.out.println("*******************************************************");
            System.out.println("number of tuned frequencies: " + tunedFrequencies.size());
            // Vypis vsetkych naladenych stanic
            
            /*
            System.out.println("pause 1 second before set init frequency...");
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            */
            // set init fm station // pre testing RDS pouzit: 972-DEVIN & 880-Jemne
            int initFrequency = 992; // 933-LUMEN (x), 972-DEVIN, 985-EUROPA2 (x), 992-FUNRADIO, 1001-REGINAS (x), 1012-RADIO_FM (x), 1035-SRO 1, 1055-VLNA (x), 1069-JEMNE (x)
            System.out.println("set init frequency: " + initFrequency);
            radio.setVolume(50);
            statusResult = radio.setFrequency(initFrequency);
            if (statusResult != StatusResult.SUCCESS) {
                System.out.println(String.format("set init frequency to %d, result: %s", initFrequency, statusResult.name()));
            }
            System.out.println("pause 1 second before read from register...");
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            Register register = Register.REG_0x0B;
            int value = radio.readRegisterFromDevice(register);
            System.out.println(String.format("register: 0x%02x, value: 0x%04x (HEX), %s (BIN)", register.getAddress(), value, Integer.toBinaryString(value)));
            
            System.out.println("pause 3 seconds before turn on checking RDS...");
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            System.out.println("RDS is turn on.");
            //radio.turnOnRDS();
            
            int i = 1;
            while (i <= 30) {
                //radio.updateLocalRegisterMapFromDevice();
                //System.out.println("*******************************************");
                //System.out.println(i + ".");
                //System.out.println(radio.getStatusString());
                //if (radio.isRdsReady()) {
                    //System.out.println("-------------------------------------------");
                    //System.out.println(radio.getRdsInfoString());
                    //System.out.println("-------------------------------------------");
                    //RdsBlockErrors rdsBlockAErrors = radio.getRdsErrorsForBlock(Register.BLOCK_A);
                    /*
                    if (rdsBlockAErrors == RdsBlockErrors.ZERO_ERRORS) {
                        System.out.println(String.format("Programme Identification: 0x%04x (HEX)", radio.getRdsPiCode()));
                    }
                    */
                    //RdsBlockErrors rdsBlockBErrors = radio.getRdsErrorsForBlock(Register.BLOCK_B);
                    //if (rdsBlockBErrors == RdsBlockErrors.ZERO_ERRORS) {
                        //System.out.println(String.format("Group Type: %d (DEC)", radio.getRdsGroupTypeCode()));
                        //String rdsVersionCode = ((radio.getRdsVersionCode() == 0) ? "A" : "B");
                        //System.out.println(String.format("RDS Version: %s", rdsVersionCode));
                        //System.out.println(String.format("Traffic Programme: %d", radio.getRdsTrafficProgramIdCode()));
                        //System.out.println(String.format("Programme Type: 0x%02x (HEX)", radio.getRdsProgramTypeCode()));
                    //}
                    //RdsBlockErrors rdsBlockCErrors = radio.getRdsErrorsForBlock(Register.BLOCK_C);
                    //RdsBlockErrors rdsBlockDErrors = radio.getRdsErrorsForBlock(Register.BLOCK_D);
                    /*if (rdsBlockAErrors == RdsBlockErrors.ONE_TO_TWO_ERRORS && 
                        rdsBlockBErrors == RdsBlockErrors.ONE_TO_TWO_ERRORS &&
                        rdsBlockCErrors == RdsBlockErrors.ONE_TO_TWO_ERRORS &&
                        rdsBlockDErrors == RdsBlockErrors.ONE_TO_TWO_ERRORS) {*/
                                         /*radio.printStationName(//radio.getLocalRegisterContent(Register.BLOCK_A),
                                                                          radio.getLocalRegisterContent(Register.BLOCK_B),
                                                                          radio.getRdsErrorsForBlock(Register.BLOCK_B),
                                                                          //radio.getLocalRegisterContent(Register.BLOCK_C),
                                                                          radio.getLocalRegisterContent(Register.BLOCK_D),
                                                                          radio.getRdsErrorsForBlock(Register.BLOCK_D),
                                                                          radio.getRdsGroupTypeCode(),
                                                                          radio.isRdsDecoderSynchronized());*/
                    //}
                    
                    //radio.readCorrectRDS(100);
                    //radio.printStation();
                    //radio.checkRDS();
                //}
                //System.out.println("*******************************************");
                i++;
                
                
                try {
                    Thread.sleep(1000);
                    System.out.println("rssi: " + radio.getRssi());
                    //System.out.println("RDS ready: " + radio.isRdsReady());
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
                
            }

            // power down
            System.out.println("Power Down after 5 seconds");
            
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            

            System.out.println("Volume 50");
            radio.setVolume(50);
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            System.out.println("Volume 500");
            radio.setVolume(500);
            
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
            
             System.out.println("Power Down result: " + radio.powerDown().name());

            System.out.println("=========== END ===========");
  
    }

}
