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
public enum Register {
    REG_0x00((byte)0x00), // chip id
    REG_0x01((byte)0x01), // 0x01 not used
    REG_0x02((byte)0x02),
    REG_0x03((byte)0x03),
    REG_0x04((byte)0x04),
    REG_0x05((byte)0x05),
    REG_0x06((byte)0x06),
    REG_0x07((byte)0x07),
    REG_0x08((byte)0x08),
    REG_0x09((byte)0x09),
    REG_0x0A((byte)0x0A),
    REG_0x0B((byte)0x0B),
    BLOCK_A((byte)0x0C),
    BLOCK_B((byte)0x0D),
    BLOCK_C((byte)0x0E),
    BLOCK_D((byte)0x0F),
    
    REG_0x10((byte)0x10),
    REG_0x11((byte)0x11),
    REG_0x12((byte)0x12),
    REG_0x13((byte)0x13),
    REG_0x14((byte)0x14),
    REG_0x15((byte)0x15),
    REG_0x16((byte)0x16),
    REG_0x17((byte)0x17),
    REG_0x18((byte)0x18),
    REG_0x19((byte)0x19),
    REG_0x1A((byte)0x1A),
    REG_0x1B((byte)0x1B),
    REG_0x1C((byte)0x1C),
    REG_0x1D((byte)0x1D),
    REG_0x1E((byte)0x1E),
    REG_0x1F((byte)0x1F),
    REG_0x20((byte)0x20),
    REG_0x21((byte)0x21),
    REG_0x22((byte)0x22),
    REG_0x23((byte)0x23),
    REG_0x24((byte)0x24),
    REG_0x25((byte)0x25); // only if 12M crystal is used
    
    private final byte address;
    
    Register(byte address) {
        this.address = address;
    }
    
    public byte getAddress() {
        return address;
    }
    
    public static Register getRegisterByAddress(byte address) {
        Register[] registers = Register.values();
        for (Register register : registers) {
           if (register.getAddress() == address) {
              return register; 
           }
        }
        
        return null;
    }
}
