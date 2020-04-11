/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rds.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ltorok
 */
public class RDSParser {

    private final RDSObject rdsObject;
    private final List<RDSListener> rdsListeners = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////
    // Program Service Name
    private char[] _PSName1;
    private char[] _PSName2;
    private char[] programServiceName; // found station name or empty. Is max. 8 character long.    
    ////////////////////////////////////////////////////////////////////////////
    
    public RDSParser() {
        this.rdsObject = new RDSObject(RDSObjectType.UNKNOWN);
        
        _PSName1 = new char[8];
        _PSName2 = new char[8];
        programServiceName = new char[8];
    }

    private void clearProgramServiceName() {
        _PSName1 = "--------".toCharArray();
        _PSName2 = _PSName1;
        programServiceName = "        ".toCharArray();
        rdsObject.setNewValue(new String(programServiceName));
        //memset(_RDSText, 0, sizeof(_RDSText));
        //_lastTextIDX = 0;
    }
    
    public void processRDSData(int block1, int block2, int block3, int block4) {
        // process block1, block2, block3, block4 & set rdsObject
        
        // DEBUG_FUNC0("process");
        //System.out.println("processRDSData");
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
            System.out.println("--- CLEAR RDS ---");
            clearProgramServiceName();
            // Send out empty data
            //if (_sendServiceName) _sendServiceName(programServiceName
            //System.out.println("RDS: " + new String(programServiceName));
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
                //System.out.println("/");
                
                // check that the data was received successfully twice
                // before publishing the station name

                if ((_PSName1[idx] == c1) && (_PSName1[idx + 1] == c2)) {
                    // retrieved the text a second time: store to _PSName2
                    _PSName2[idx] = c1;
                    _PSName2[idx + 1] = c2;
                    //_PSName2[8] = '\0';

                    if ((idx == 6) && Arrays.equals(_PSName1, _PSName2)) {
                        //System.out.println("**************************************************************");
                        //System.out.println("_PSName2: " + new String(_PSName2));
                        //System.out.println("programServiceName: " + new String(programServiceName));
                        //System.out.println("**************************************************************");
                        if (!Arrays.equals(_PSName2, programServiceName)) {
                            // publish station name
                            //strcpy(programServiceName, _PSName2);
                            System.arraycopy(_PSName2, 0, programServiceName, 0, _PSName2.length);
                            //if (_sendServiceName)
                            //    _sendServiceName(programServiceName);
                            //System.out.println("******************************************************************** RDS: " + new String(programServiceName));
                            rdsObject.setNewType(RDSObjectType.PS);
                            rdsObject.setNewValue(new String(programServiceName));
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
        
        // call rdsObjectChanged
        rdsObjectChanged();
    }

    public synchronized void addListener(RDSListener rdsListenerToAdd) {
        rdsListeners.add(rdsListenerToAdd);
    }

    public synchronized void removeListener(RDSListener rdsListenerToRemove) {
        rdsListeners.remove(rdsListenerToRemove);
    }

    public void rdsObjectChanged() {
        RDSEvent rdsEvent = new RDSEvent(this, rdsObject);
        
        for (RDSListener rdsListener : rdsListeners) {
            rdsListener.onRDSObjectChanged(rdsEvent);
        }
    }
}
