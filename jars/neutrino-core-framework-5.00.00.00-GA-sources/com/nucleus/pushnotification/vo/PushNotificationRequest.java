package com.nucleus.pushnotification.vo;

public class PushNotificationRequest {
	private PushNotificationData data;
	
	private  String to;

	public PushNotificationData getData() {
		return data;
	}

	public void setData(PushNotificationData data) {
		this.data = data;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}
