package com.smartHome.flat.balcony.sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

/**
 * @author JPojezdala
 *
 * Light sensor BH1750 Factory
 */
public class BH1750Factory {

    private BH1750Factory() {
    }

    public BH1750 create() throws UnsupportedBusNumberException {
        I2CBus bus;
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            BH1750 bh1750 = new BH1750(bus);

            bh1750.init();

            return bh1750;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}