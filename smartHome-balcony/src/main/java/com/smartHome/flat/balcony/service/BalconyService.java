/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartHome.flat.balcony.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.smartHome.flat.balcony.model.DataResponse;
import com.smartHome.flat.balcony.sensors.GpioBalcony;
import com.smartHome.flat.balcony.sensors.TemperatureHumidity;

/**
 * Methods for control Balcony Raspberry Pi Zero W module.
 *
 * @author Jan Pojezdala
 */

@Component
public class BalconyService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	GpioBalcony gpioBalcony = new GpioBalcony();
	TemperatureHumidity temperatureHumidity = new TemperatureHumidity();

	/**
	 * method for get BMP180 and other sensor values
	 *
	 * 
	 * @return {@link SensorsResponseEntityReply} instance
	 */
	public DataResponse getData() {
		DataResponse dataResponse = new DataResponse();
		try {			
			dataResponse.dataResponse(temperatureHumidity.main());
			dataResponse.setLight(gpioBalcony.getLight());
			dataResponse.setBattery(gpioBalcony.adcFunction());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataResponse;
	}

	/**
	 * method for patch enable/disable waterPump
	 *
	 * 
	 * @return Boolean
	 */
	public Boolean patchWaterPump(String enabled) {
		Boolean value = null;
		try {
			if (enabled.equals("true")) {
				value = true;
				gpioBalcony.waterPumpStart();
			} else {
				value = false;
				gpioBalcony.waterPumpStop();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return value;
	}

	/**
	 * method for setup waterPumpAutomat
	 *
	 * 
	 * @return
	 */
	public void setAutomateWatering(String enable, String cycleTime) {
		try {
			gpioBalcony.waterPumpAutomat(Integer.valueOf(cycleTime));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	/**
	 * method for start automated watering job
	 *
	 * 
	 * @return
	 */
	// @Scheduled(cron = "${app.run.waterPump}")
	public void runCroneJob() {
		logger.info("Automated cron watering job started");
		try {
			gpioBalcony.waterPumpAutomat(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Automated cron watering finished");
	}
	 
		/**
		 * method for logging battery status
		 *
		 * 
		 * @return
		 */
		// @Scheduled(cron = "${app.run.battery.log}")
		public void runBatteryStatusCron() {
			 try {
				 gpioBalcony.adcFunction();			
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	 
		/**
		 * method for setup ADC converter
		 *
		 * 
		 * @return Double
		 */
		public Double setConverterADC() {
			try {
				return gpioBalcony.adcFunction();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * method for check PIR sensor
		 *
		 * 
		 * @return Boolean
		 */
		//@Scheduled(fixedRate=1000)
		public Boolean getPIR() {
			try {
				return gpioBalcony.checkMotionSensorPIR();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		
		/**
		 * method for start python script
		 *
		 * 
		 */
		public void runPython(String mode) {
			gpioBalcony.executePython(mode);
	
		}
		
		
		/**
		 * method for check water
		 *
		 * 
		 * @return Boolean
		 */
		public Boolean checkWater() {
			try {
				return gpioBalcony.waterCheck();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * method for check soil
		 *
		 * 
		 * @return Boolean
		 */
		public Boolean checkSoil() {
			try {
				return gpioBalcony.soilHumidityCheck();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * method for check light BH1750
		 *
		 * 
		 * @return BigDecimal
		 * @throws UnsupportedBusNumberException
		 * @throws IOException
		 */
		public BigDecimal getLight() throws UnsupportedBusNumberException, IOException {
			return gpioBalcony.getLight();
		}
		
		/**
		 * method for patch enable/disable waterPump
		 *
		 * 
		 * @return Boolean
		 */
		public Boolean patchPowerBank(String enabled) {
			Boolean value = null;
			try {
				if (enabled.equals("true")) {
					value = true;
					gpioBalcony.enablePW();;
				} else {
					value = false;
					gpioBalcony.disablePW();
					;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return value;
		}

}
