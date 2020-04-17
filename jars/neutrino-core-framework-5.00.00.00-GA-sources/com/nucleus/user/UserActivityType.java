package com.nucleus.user;

public enum UserActivityType {
	
	SUCCESSFUL_LOGIN(0),
	UNSUCCESSFUL_LOGIN(1),
	USER_LOCKED(2),
	USER_UNLOCKED(3),
	USER_ACTIVATED(4),
	USER_DEACTIVATED(5),
	USER_CREATED(6);
	
	private int code;
	
	UserActivityType(int code){
		this.code=code;
	}

	public int getCode() {
		return code;
	}

}
