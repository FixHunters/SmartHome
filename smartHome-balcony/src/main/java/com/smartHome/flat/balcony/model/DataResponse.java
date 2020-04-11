package com.smartHome.flat.balcony.model;

import java.util.Objects;

/** SensorsResponseEntity */

@javax.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2018-04-03T15:24:02.669+02:00"
)
public class DataResponse {

  private Float temperature = null;
  

  private Float press = null;
  

  private Double alt = null;
  

  private Boolean soilStatus = null;
  
 
  private Boolean waterLevel = null;
  
  
  private Boolean waterPumpStatus = null;

  
/*  private void unpackNameFromNestedObject(Map<String, Integer> dataMap) {
	  this.temperature = dataMap.get("temperature");
	  this.press = dataMap.get("press");
	  this.alt = dataMap.get("alt");
  }*/

  public DataResponse temperature(Float temperature) {
    this.temperature = temperature;
    return this;
  }

  /**
   * Get temperature
   *
   * @return temperature
   */

  public Float getTemperature() {
    return temperature;
  }

  public void setTemperature(Float temperature) {
    this.temperature = temperature;
  }
  
  public DataResponse press(Float press) {
    this.press = press;
    return this;
  }

  /**
   * Get Pressue
   *
   * @return press
   */

  public Float getPress() {
    return press;
  }

  public void setPress(Float press) {
    this.press = press;
  }

  public DataResponse alt(Double alt) {
    this.alt = alt;
    return this;
  }
  
  /**
   * Get Altitude
   *
   * @return alt
   */
 
  public Double getAlt() {
    return alt;
  }

  public void setAlt(Double alt) {
    this.alt = alt;
  }  
  
  public DataResponse soilStatus(Boolean soilStatus) {
	    this.soilStatus = soilStatus;
	    return this;
	  }

	  /**
	   * Get soilStatus
	   *
	   * @return soilStatus
	   */

	  public Boolean getSoilStatus() {
	    return soilStatus;
	  }

	  public void setSoilStatus(Boolean soilStatus) {
	    this.soilStatus = soilStatus;
	  }
	  
  public DataResponse waterLevel(Boolean waterLevel) {
	    this.waterLevel = waterLevel;
	    return this;
	  }

	  /**
	   * Get waterLevel
	   *
	   * @return waterLevel
	   */

	  public Boolean getWaterLevel() {
	    return waterLevel;
	  }

	  public void setWaterLevel(Boolean waterLevel) {
	    this.waterLevel = waterLevel;
	  }	  
	 
  public DataResponse waterPumpStatus(Boolean waterPumpStatus) {
	    this.waterPumpStatus = waterPumpStatus;
	    return this;
	  }

	  /**
	   * Get waterPumpStatus
	   *
	   * @return waterPumpStatus
	   */
	
	  public Boolean getWaterPumpStatus() {
	    return waterPumpStatus;
	  }

	  public void setWaterPumpStatus(Boolean waterPumpStatus) {
	    this.waterPumpStatus = waterPumpStatus;
	  }	  

	  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataResponse DataResponseReply = (DataResponse) o;
    return Objects.equals(this.temperature, DataResponseReply.temperature) &&
    		Objects.equals(this.press, DataResponseReply.press) &&
    		Objects.equals(this.alt, DataResponseReply.alt) &&
    		Objects.equals(this.soilStatus, DataResponseReply.soilStatus) &&
    		Objects.equals(this.waterLevel, DataResponseReply.waterLevel) &&
    		Objects.equals(this.waterPumpStatus, DataResponseReply.waterPumpStatus);
  }



  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataResponseReply {\n");
    sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
    sb.append("    press: ").append(toIndentedString(press)).append("\n");
    sb.append("    alt: ").append(toIndentedString(alt)).append("\n");
    sb.append("    soilStatus: ").append(toIndentedString(soilStatus)).append("\n");
    sb.append("    waterLevel: ").append(toIndentedString(waterLevel)).append("\n");
    sb.append("    waterPumpStatus: ").append(toIndentedString(waterPumpStatus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
