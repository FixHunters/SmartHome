package com.smartHome.webapp.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.smartHome.webapp.model.SensorData;

public class SensorDataRowMapper implements RowMapper<SensorData> {

	@Override
	public SensorData mapRow(ResultSet rs, int rowNum) throws SQLException {
		SensorData res = new SensorData();
		res.id(rs.getInt("ID"))
		.temperature(rs.getString("TEMPERATURE"))
		.humidity(rs.getString("HUMIDITY"))
		.altitude(rs.getString("ALTITUDE"))
		.light(rs.getString("LIGHT"))
		.date(rs.getTimestamp("DATE_VALUE"));
	return res;
	}
}
