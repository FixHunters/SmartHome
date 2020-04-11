/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rds.common;

import java.util.EventObject;

/**
 *
 * @author ltorok
 */
public class RDSEvent extends EventObject {

    private final RDSObject rdsObject;

    public RDSEvent(Object source, RDSObject rdsObject) {
        super(source);
        this.rdsObject = rdsObject;
    }

    public RDSObject getRDSObject() {
        return rdsObject;
    }
}
