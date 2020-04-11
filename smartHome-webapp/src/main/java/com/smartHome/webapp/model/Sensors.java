package com.smartHome.webapp.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class Sensors implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String temperature;
	private String humidity;
	private String altitude;
	private Timestamp date;
	private String batteryVoltage;

	public Sensors id(Integer id) {
		this.id = id;
		return this;
	}

	public Sensors temperature(String temperature) {
		this.temperature = temperature;
		return this;
	}

	public Sensors humidity(String humidity) {
		this.humidity = humidity;
		return this;
	}

	public Sensors altitude(String altitude) {
		this.altitude = altitude;
		return this;
	}
	
	public Sensors batteryVoltage(String batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
		return this;
	}
	
	public Sensors date(Timestamp date) {
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
	
	public String getBatteryVoltage() {
		return batteryVoltage;
	}

	public void setBatteryVoltage(String batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
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
		Sensors sensors = (Sensors) o;
		return Objects.equals(this.id, sensors.id) && Objects.equals(this.temperature, sensors.temperature)
				&& Objects.equals(this.humidity, sensors.humidity)
				&& Objects.equals(this.altitude, sensors.altitude)
				&& Objects.equals(this.batteryVoltage, sensors.batteryVoltage)
				&& Objects.equals(this.date, sensors.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, temperature, humidity, altitude, batteryVoltage, date);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Sensors {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
		sb.append("    humidity: ").append(toIndentedString(humidity)).append("\n");
		sb.append("    altitude: ").append(toIndentedString(altitude)).append("\n");
		sb.append("    batteryVoltage: ").append(toIndentedString(batteryVoltage)).append("\n");
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
