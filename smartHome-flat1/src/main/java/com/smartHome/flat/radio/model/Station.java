package com.smartHome.flat.radio.model;

public class Station {

	private Boolean rdsReady = null;

	private Boolean seekComplete = null;

	private Boolean seekFail = null;

	private Boolean rdsSynchronized = null;

	private Boolean eBlockFound = null;

	private String audioType = null;
	
	private Integer channel = null;
	
	private Integer rssi = null;
	
	private Boolean isStation = null;
	
	private Boolean isFMReady= null;

	public Boolean getRdsReady() {
		return rdsReady;
	}

	public void setRdsReady(Boolean rdsReady) {
		this.rdsReady = rdsReady;
	}

	public Boolean getSeekComplete() {
		return seekComplete;
	}

	public void setSeekComplete(Boolean seekComplete) {
		this.seekComplete = seekComplete;
	}

	public Boolean getSeekFail() {
		return seekFail;
	}

	public void setSeekFail(Boolean seekFail) {
		this.seekFail = seekFail;
	}

	public Boolean getRdsSynchronized() {
		return rdsSynchronized;
	}

	public void setRdsSynchronized(Boolean rdsSynchronized) {
		this.rdsSynchronized = rdsSynchronized;
	}

	public Boolean geteBlockFound() {
		return eBlockFound;
	}

	public void seteBlockFound(Boolean eBlockFound) {
		this.eBlockFound = eBlockFound;
	}

	public String getAudioType() {
		return audioType;
	}

	public void setAudioType(String audioType) {
		this.audioType = audioType;
	}

	public Integer getChannel() {
		return channel;
	}

	public void setChannel(Integer channel) {
		this.channel = channel;
	}

	public Integer getRssi() {
		return rssi;
	}

	public void setRssi(Integer rssi) {
		this.rssi = rssi;
	}

	public Boolean getIsStation() {
		return isStation;
	}

	public void setIsStation(Boolean isStation) {
		this.isStation = isStation;
	}

	public Boolean getIsFMReady() {
		return isFMReady;
	}

	public void setIsFMReady(Boolean isFMReady) {
		this.isFMReady = isFMReady;
	}
}
