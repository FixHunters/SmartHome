package com.smartHome.flat.application;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-04-03T15:24:02.669+02:00")
@Api(value = "radio", description = "Radio API interface")
public interface ApplicationApi {

	@ApiOperation(value = "", nickname = "getRadio", notes = "Retrieve test stations", response = String.class, tags = {})

	@ApiResponses(value = {

			@ApiResponse(code = 200, message = "The operation was successful", response = String.class),

			@ApiResponse(code = 400, message = "Wrong input data format", response = Error.class) })

	@RequestMapping(value = "/radio", produces = { "application/json" }, method = RequestMethod.GET)
	void getRadio();

}
