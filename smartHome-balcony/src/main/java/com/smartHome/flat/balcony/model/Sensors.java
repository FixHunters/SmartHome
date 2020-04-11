package com.smartHome.flat.balcony.model;

import java.util.Objects;

public class Sensors {

	private Integer id;
	private String temperature;
	private String humidity;
	private String soil_moisture;

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

	public Sensors soil_moisture(String soil_moisture) {
		this.soil_moisture = soil_moisture;
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

	public String getSoil_moisture() {
		return soil_moisture;
	}

	public void setSoil_moisture(String soil_moisture) {
		this.soil_moisture = soil_moisture;
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
				&& Objects.equals(this.soil_moisture, sensors.soil_moisture);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, temperature, humidity, soil_moisture);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Sensors {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
		sb.append("    humidity: ").append(toIndentedString(humidity)).append("\n");
		sb.append("    soil_moisture: ").append(toIndentedString(soil_moisture)).append("\n");
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
