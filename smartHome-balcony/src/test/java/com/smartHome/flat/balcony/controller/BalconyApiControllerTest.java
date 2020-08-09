package com.smartHome.flat.balcony.controller;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class BalconyApiControllerTest {

	
	   @BeforeClass
	    public static void setup() {
	        String port = System.getProperty("server.port");
	        if (port == null) {
	            RestAssured.port = Integer.valueOf(8080);
	        }
	        else{
	            RestAssured.port = Integer.valueOf(port);
	        }


	        String basePath = System.getProperty("server.base");
	        if(basePath==null){
	            basePath = "/";
	        }
	        RestAssured.basePath = basePath;

	        String baseHost = System.getProperty("server.host");
	        if(baseHost==null){
	            baseHost = "http://192.168.0.111";
	        }
	        RestAssured.baseURI = baseHost;

	    }
	
	 @Test
	 public void getDataTest() {
		 Response response = given().when().get("/data");
		 assertEquals(response.getStatusCode(), 200);
		 System.out.println(response.getBody().prettyPrint());
	 }
	 
	 
	
}
