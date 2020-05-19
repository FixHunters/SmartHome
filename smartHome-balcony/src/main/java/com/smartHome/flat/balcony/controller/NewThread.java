package com.smartHome.flat.balcony.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartHome.flat.balcony.sensors.GpioBalcony;

//TODO Test creation thread for motion sensor + SingleThreadExample
public class NewThread extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(NewThread.class);
	
    public void run() {
        long startTime = System.currentTimeMillis();
        int i = 0;
        GpioBalcony gpioBalcony = new GpioBalcony();
        while (true) {
        	log.info(this.getName() + ": New Thread is running..." + i++);
            System.out.println(this.getName() + ": New Thread is running..." + i++);
            try {
                //Wait for one sec so it doesn't print too fast
                Thread.sleep(1000);
                
                System.out.println(gpioBalcony.adcFunction());
               // gpioBalcony.checkMotionSensorPIRv2();
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
          
        }
    }
    
    public double generate() {
		return Math.random();
    	
    }
}

