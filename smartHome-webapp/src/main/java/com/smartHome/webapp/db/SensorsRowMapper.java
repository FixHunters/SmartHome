package com.smartHome.webapp.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.smartHome.webapp.model.Sensors;

public class SensorsRowMapper implements RowMapper<Sensors> {

	@Override
	public Sensors mapRow(ResultSet rs, int rowNum) throws SQLException {
		Sensors res = new Sensors();
		res.id(rs.getInt("ID"))
			.temperature(rs.getString("TEMPERATURE"))
			.humidity(rs.getString("HUMIDITY"))
			.altitude(rs.getString("ALTITUDE"))
			.batteryVoltage(rs.getString("BATTERY_VOLTAGE"))
			.date(rs.getTimestamp("DATE_VALUE"));
		return res;
	}
}
