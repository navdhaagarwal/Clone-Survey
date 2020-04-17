package com.nucleus.pushnotification.vo;

public class PushNotificationResponse {

boolean success	;
String errorMsg;
public boolean isSuccess() {
	return success;
}
public void setSuccess(boolean success) {
	this.success = success;
}
public String getErrorMsg() {
	return errorMsg;
}
public void setErrorMsg(String errorMsg) {
	this.errorMsg = errorMsg;
}
      
}
