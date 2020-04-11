package com.smartHome.flat.balcony.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.smartHome.flat.balcony.BalconyApplication;
import com.smartHome.flat.balcony.model.SensorsResponseEntity;
import com.smartHome.flat.balcony.service.BalconyService;

@Controller
@RestController
public class BalconyApiController implements BalconyApi {

	private static final Logger log = LoggerFactory.getLogger(BalconyApiController.class);

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public BalconyApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.request = request;
	}

	@Autowired
	private BalconyService balconyService;
	
	@Autowired
	private DynamicScheduler dynamicScheduler;
	
    @PostMapping("/restart")
    public void restart() {
        BalconyApplication.restart();
    } 

	@Override
	public ResponseEntity<SensorsResponseEntity> getData() {
		return new ResponseEntity<SensorsResponseEntity>(balconyService.getData(), HttpStatus.OK);
	}

	@Override
	public Boolean patchWaterPump() {
		return balconyService.patchWaterPump(request.getHeader("enabled"));

	}

	@Override
	public void setAutomateWatering() {
		String cycleTime = request.getHeader("cycleTime");
		log.debug("Cycle was set on time" + cycleTime);
		balconyService.setAutomateWatering("true", cycleTime);
		return;
	}

	@Override
	public Double getAdc() {
		log.debug("Call ADC converter");
		return balconyService.setConverterADC();
	}

	@Override
	public Boolean getPir() {
		log.debug("Call PIR sensor");
		return balconyService.getPIR();
	}
	
    @PostMapping("/python")
    public void executePython() {
    	String mode = request.getHeader("mode");
		log.debug("Mode : " + mode);
    	balconyService.runPython(mode);
    } 
    
    @PostMapping("/crone")
    public void setCroneJob() {
    	String croneEx = request.getHeader("croneEx");
		log.debug("croneEx : " + croneEx);
    	dynamicScheduler.setActivate(croneEx);
    }   
    
    @GetMapping("/water")
    public void getWaterStatus() {
    	balconyService.checkWater();
    }
    
    @GetMapping("/soil")
    public void getSoilStatus() {
    	balconyService.checkSoil();
    }
    
	@GetMapping("/light")
	public BigDecimal getLight() {
		BigDecimal value = null;
		try {
			value = balconyService.getLight();
		} catch (UnsupportedBusNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
    
/*    @Override
    public List<Integer> getRadio() {
    	RadioApp radioApp = new RadioApp();
    	return radioApp.main();
    }*/
    
    @PatchMapping("/enablePower")
    public Boolean enablePowerBank() {
    	return balconyService.patchPowerBank(request.getHeader("enabled"));
    }  
    
	@RequestMapping(value="/upload", method=RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
		File convertFile = new File("C:\\talk2amareswaran-downloads\\fileuploaddemo\\fileuploaddemo\\"+file.getOriginalFilename());
		convertFile.createNewFile();
		FileOutputStream fout = new FileOutputStream(convertFile);
		fout.write(file.getBytes());
		fout.close();
		return new ResponseEntity<>("File is uploaded successfully", HttpStatus.OK);
	}

}
