
package com.smartHome.webapp.controllers;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.smartHome.webapp.db.DBSupport;
import com.smartHome.webapp.model.DataResponse;
import com.smartHome.webapp.model.Sensors;
import com.smartHome.webapp.model.SensorsResponseEntity;

@RestController
public class ApplicationController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private DBSupport dbSupport;

	private DBSupport getDbSupport() {
		if (dbSupport == null) {
			dbSupport = new DBSupport(jdbcTemplate);
			log.info("ApplicationController:: dbSupport created " + dbSupport);
		}
		return dbSupport;
	}

	/*
	 * private final HttpServletRequest request;
	 * 
	 * @org.springframework.beans.factory.annotation.Autowired public
	 * ApplicationController(ObjectMapper objectMapper, HttpServletRequest request)
	 * { this.request = request; }
	 */

	public void insertMeasuredData(Sensors sensor) {
		getDbSupport().insertSensorsData(sensor);
	}

	public void insertMeasuredData(SensorsResponseEntity entity) {
		getDbSupport().insertSensorsResponseEntity(entity);
	}

	public void findAllSensorsData() {
		getDbSupport().findAllSensorsData();
	}

	public String findAllData() {
		return getDbSupport().findAllSensorsData().toString();
	}

	public List<Sensors> findAllDataSensor() {
		return getDbSupport().findAllSensorsData();
	}

	public Boolean setBoolean() {
		return true;
	}

	@PostMapping("/sensor")
	void saveSensor() {
		final String uri = "http://192.168.0.111:8080/data";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<DataResponse> result = restTemplate.exchange(uri, HttpMethod.GET, entity, DataResponse.class);

		getDbSupport().insertDataResponse(result.getBody());

		// function for select specific row in table
		// getDbSupport().selectRow("performance");
	}

}
