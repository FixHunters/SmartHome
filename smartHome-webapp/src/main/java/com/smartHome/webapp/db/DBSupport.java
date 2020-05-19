package com.smartHome.webapp.db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.smartHome.webapp.model.DataResponse;
import com.smartHome.webapp.model.SensorData;

/** database tools for working with data and database */
public class DBSupport {

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

	public void insertDataResponse(DataResponse data) {
		localDateTime = LocalDateTime.now();
		int id = (int) Math.random();
		String sql = "INSERT INTO sensor_data(id, temperature, humidity, altitude, light, date_value) VALUES(?,?,?,?,?,?)";
		jdbcTemplate.update(sql, id, data.getTemperature(), data.getPress(), data.getAlt(), data.getLight(),
				localDateTime);
		log.info("DBSupport:: sensor_data inserted: " + "id: " + id + " Temperature: " + data.getTemperature()
				+ " Humidity: " + data.getPress() + " Altitude: " + data.getAlt() + " Light: " + data.getLight()
				+ " Battery voltage: " + data.getBattery() + " Timestamp: " + localDateTime);
		// Insert Battery potential
		sql = "INSERT INTO performance(battery_potential, cpu_potential, cpu_temperature, memory_total, memory_used, date_value) VALUES(?,?,?,?,?,?)";
		jdbcTemplate.update(sql, data.getBattery(), "", "", "", "", localDateTime);
	}

	public List<SensorData> selectSensorData() {
		String sql = "SELECT * FROM sensor_data";
		List<SensorData> sensorData = (List<SensorData>) jdbcTemplate.query(sql, new SensorDataRowMapper());
		log.info("DBSupport:: All data selected: " + sensorData.toString());
		return sensorData;
	}

	public List<Map<String, Object>> selectRow(String table) {
		String sql = "SELECT * FROM " + table;
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

		log.info("DBSupport:: Select " + table + " table result: " + result.toString());

		return result;
	}

}
