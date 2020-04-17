package com.nucleus.pushnotification.vo;

public class PushNoticationsClient {

	private String notificationClientId;

	
	private String imeiNumber;
	private String deviceType;

	private String operatingSystem;

	public String getNotificationClientId() {
		return notificationClientId;
	}

	public void setNotificationClientId(String notificationClientId) {
		this.notificationClientId = notificationClientId;
	}

	public String getImeiNumber() {
		return imeiNumber;
	}

	public void setImeiNumber(String imeiNumber) {
		this.imeiNumber = imeiNumber;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	

}
