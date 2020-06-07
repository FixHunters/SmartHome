package com.smartHome.flat;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHome.flat.radio.RadioApplication;
import com.smartHome.flat.radio.model.Station;

@Controller
@RestController
public class ApplicationController implements ApplicationApi {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	RadioApplication radioApp = new RadioApplication();

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public ApplicationController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public List<Station> getRadio() {
		return radioApp.searchStation(request.getHeader("tuneDirection"), Boolean.valueOf(request.getHeader("stations")));
	}

	@Override
	public Map<String, String> setRadio() {
		Integer volume = null;
		Integer frequency = null;
		if (request.getHeader("volume") != null) {
			volume = Integer.valueOf(request.getHeader("volume"));
		}
		if (request.getHeader("frequency") != null) {
			frequency = Integer.valueOf(request.getHeader("frequency"));
		}
		String volumeDirection = request.getHeader("volumeDirection");
		Boolean mute = Boolean.valueOf(request.getHeader("mute"));
		Boolean bass = Boolean.valueOf(request.getHeader("bass"));
		Boolean stereo = Boolean.valueOf(request.getHeader("stereo"));
		Boolean info = Boolean.valueOf(request.getHeader("info"));
		String power = request.getHeader("power");
		return radioApp.setupRadio(volume, volumeDirection, frequency, mute, bass, stereo, info, power);
	}
}