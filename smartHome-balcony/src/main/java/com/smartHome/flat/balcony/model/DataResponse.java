package com.smartHome.flat.balcony.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/** DataResponse */

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-04-03T15:24:02.669+02:00")
public class DataResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Float temperature = null;

	private Float press = null;

	private Double alt = null;

	private BigDecimal light = null;

	private Double battery = null;

	/**
	 * Get dataResponse
	 *
	 * @return dataResponse
	 */
	public DataResponse dataResponse() {
		this.temperature = getTemperature();
		this.press = getPress();
		this.alt = getAlt();
		this.light = getLight();
		this.battery = getBattery();
		return this;
	}
	
	public DataResponse dataResponse(DataResponse data) {
		this.temperature = data.getTemperature();
		this.press = data.getPress();
		this.alt = data.getAlt();
		this.light = data.getLight();
		this.battery = data.getBattery();
		return this;
	}
	
	public DataResponse temperature(Float temperature) {
		this.temperature = temperature;
		return this;
	}

	public Float getTemperature() {
		return temperature;
	}

	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}

	/**
	 * Get Pressue
	 *
	 * @return press
	 */
	public DataResponse press(Float press) {
		this.press = press;
		return this;
	}

	public Float getPress() {
		return press;
	}

	public void setPress(Float press) {
		this.press = press;
	}

	/**
	 * Get Altitude
	 *
	 * @return alt
	 */
	public DataResponse alt(Double alt) {
		this.alt = alt;
		return this;
	}

	public Double getAlt() {
		return alt;
	}

	public void setAlt(Double alt) {
		this.alt = alt;
	}

	/**
	 * Get Light
	 *
	 * @return BigDecimal
	 */
	public DataResponse light(BigDecimal light) {
		this.light = light;
		return this;
	}

	public BigDecimal getLight() {
		return light;
	}

	public void setLight(BigDecimal light) {
		this.light = light;
	}

	/**
	 * Get battery
	 *
	 * @return battery
	 */
	public DataResponse battery(Double battery) {
		this.battery = battery;
		return this;
	}

	public Double getBattery() {
		return battery;
	}

	public void setBattery(Double battery) {
		this.battery = battery;
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
		return Objects.equals(this.temperature, DataResponseReply.temperature)
				&& Objects.equals(this.press, DataResponseReply.press)
				&& Objects.equals(this.alt, DataResponseReply.alt)
				&& Objects.equals(this.light, DataResponseReply.light)
				&& Objects.equals(this.battery, DataResponseReply.battery);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DataResponseReply {\n");
		sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
		sb.append("    press: ").append(toIndentedString(press)).append("\n");
		sb.append("    alt: ").append(toIndentedString(alt)).append("\n");
		sb.append("    light: ").append(toIndentedString(light)).append("\n");
		sb.append("    battery: ").append(toIndentedString(battery)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
