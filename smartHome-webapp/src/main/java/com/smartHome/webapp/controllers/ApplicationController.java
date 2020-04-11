package com.smartHome.webapp.controllers;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHome.webapp.db.DBSupport;
import com.smartHome.webapp.model.Sensors;
import com.smartHome.webapp.model.SensorsResponseEntity;

@Component
//@Controller
//@RestController
public class ApplicationController implements Serializable {
	
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
	
/*	  private final HttpServletRequest request;
	
	  @org.springframework.beans.factory.annotation.Autowired
	  public ApplicationController(ObjectMapper objectMapper, HttpServletRequest request) {
	    this.request = request;
	  }*/

	
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

}
