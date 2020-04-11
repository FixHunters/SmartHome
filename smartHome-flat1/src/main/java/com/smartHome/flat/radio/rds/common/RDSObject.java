/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartHome.flat.radio.rds.common;

/**
 *
 * @author ltorok
 */
public class RDSObject {

    private String oldValue;

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    private String newValue;

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        oldValue = this.newValue;
        this.newValue = newValue;
    }

    private RDSObjectType oldType;

    public void setOldType(RDSObjectType oldType) {
        this.oldType = oldType;
    }

    public RDSObjectType getOldType() {
        return oldType;
    }

    private RDSObjectType newType;

    public RDSObjectType getNewType() {
        return newType;
    }

    public void setNewType(RDSObjectType newType) {
        oldType = this.newType;
        this.newType = newType;
    }

    public RDSObject(RDSObjectType type) {
        this.oldType = type;
        this.newType = type;
    }

    @Override
    public String toString() {
        return ("[oldType=" + oldType + ", oldValue=" + oldValue + ", newType=" + newType + ", newValue=" + newValue + "]");
    }

}
