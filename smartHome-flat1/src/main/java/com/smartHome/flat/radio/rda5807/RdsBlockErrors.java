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
public enum RdsBlockErrors {
    ZERO_ERRORS, ONE_TO_TWO_ERRORS, THREE_TO_FIVE_ERRORS, SIX_OR_MORE_ERRORS;
    
     public static RdsBlockErrors getRdsBlockErrorsByValue(int value) {
        RdsBlockErrors[] rdsBlockErrorsValues = RdsBlockErrors.values();
        for (RdsBlockErrors rdsBlockErrorsValue : rdsBlockErrorsValues) {
           if (rdsBlockErrorsValue.ordinal() == value) {
              return rdsBlockErrorsValue; 
           }
        }
        
        return null;
    }
}
