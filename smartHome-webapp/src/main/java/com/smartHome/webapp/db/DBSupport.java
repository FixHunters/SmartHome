package com.smartHome.webapp.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.smartHome.webapp.controllers.ApplicationController;
import com.smartHome.webapp.model.Sensors;
import com.smartHome.webapp.model.SensorsResponseEntity;

/** database tools for working with the sensors store database */
public class DBSupport implements Serializable{
	
	private static final Logger log = LoggerFactory.getLogger(DBSupport.class);

	/** shared instance for working with DB */
	private JdbcTemplate jdbcTemplate;

	/**
	 * constructor
	 *
	 * @param jdbcTemplate
	 */
	public DBSupport(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private LocalDateTime localDateTime;
	
	/**
	 * method for inserData the DB
	 */

	public void insertSensorsData(Sensors sensor) {
		String sql = "INSERT INTO sensors(id, temperature, humidity, soil_moisture) VALUES(?,?,?,?)";
		jdbcTemplate.update(sql, sensor.getId(), sensor.getTemperature(), sensor.getHumidity(),
				sensor.getAltitude());
		log.info("DBSupport:: Sensor data inserted: " 
				+ "id: " + sensor.getId() 
				+ " Temperature: " + sensor.getTemperature() 
				+ " Humidity: " + sensor.getHumidity() 
				+ " Altitude: " + sensor.getAltitude());
	}
	
	/**
	 * method for inserData to the DB from SensorsResponseEntity
	 */

	public void insertSensorsResponseEntity(SensorsResponseEntity entity) {
		localDateTime = LocalDateTime.now();
		String sql = "INSERT INTO sensors(id, temperature, humidity, altitude, battery_voltage, date_value) VALUES(?,?,?,?,?,?)";
		jdbcTemplate.update(sql, entity.getId(), entity.getDataResponse().getTemperature(), entity.getDataResponse().getPress(),
				entity.getDataResponse().getAlt(), entity.getDataResponse().getBatteryVoltage(), localDateTime);
		log.info("DBSupport:: Sensor data inserted: " 
				+ "id: " + entity.getId() 
				+ " Temperature: " + entity.getDataResponse().getTemperature()
				+ " Humidity: " + entity.getDataResponse().getPress() 
				+ " Altitude: " + entity.getDataResponse().getAlt()
				+ " Battery voltage: " + entity.getDataResponse().getBatteryVoltage()
				+ " Timestamp: " + localDateTime);
	}

	public List<Sensors> findAllSensorsData() {
		String sql = "SELECT * FROM Sensors";
		List<Sensors> sensors = (List<Sensors>) jdbcTemplate.query(sql, new SensorsRowMapper());
		log.info("DBSupport:: All sensor data selected: " + sensors.toString());
		return sensors;
	}

}
