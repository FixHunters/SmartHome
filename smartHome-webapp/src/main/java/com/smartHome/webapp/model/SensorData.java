package com.smartHome.webapp.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class SensorData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String temperature;
	private String humidity;
	private String altitude;
	private String light;
	private Timestamp date;

	public SensorData id(Integer id) {
		this.id = id;
		return this;
	}

	public SensorData temperature(String temperature) {
		this.temperature = temperature;
		return this;
	}

	public SensorData humidity(String humidity) {
		this.humidity = humidity;
		return this;
	}

	public SensorData altitude(String altitude) {
		this.altitude = altitude;
		return this;
	}
	
	public SensorData light(String light) {
		this.light = light;
		return this;
	}
	
	public SensorData date(Timestamp date) {
		this.date = date;
		return this;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}
	
	public String getLight() {
		return light;
	}

	public void setLight(String light) {
		this.light = light;
	}
	
	public void setDate(Timestamp date){
		   this.date = date;
		}

	public Timestamp getDate(){
	   return this.date;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SensorData sensors = (SensorData) o;
		return Objects.equals(this.id, sensors.id) && Objects.equals(this.temperature, sensors.temperature)
				&& Objects.equals(this.humidity, sensors.humidity)
				&& Objects.equals(this.altitude, sensors.altitude)
				&& Objects.equals(this.light, sensors.light)
				&& Objects.equals(this.date, sensors.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, temperature, humidity, altitude, light, date);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Sensors {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
		sb.append("    humidity: ").append(toIndentedString(humidity)).append("\n");
		sb.append("    altitude: ").append(toIndentedString(altitude)).append("\n");
		sb.append("    light: ").append(toIndentedString(light)).append("\n");
		sb.append("    date: ").append(toIndentedString(date)).append("\n");
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
