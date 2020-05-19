package com.smartHome.flat.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.smartHome.flat.radio.RadioApplication;

@Controller
@RestController
public class ApplicationController implements ApplicationApi {

	private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

	@Override
    public void getRadio() {
		log.info("Radio starting");
    	RadioApplication radioApp = new RadioApplication();
    	radioApp.main();
	}
}