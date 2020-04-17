package com.nucleus.entity;

/**
 * Constants for entity status
 */

public enum EntityStatus {

	ACTIVE(0),INACTIVE(1),DELETED(2);

	private int status;

	EntityStatus(int status){
		this.status=status;
	};
	
	public int getStatusValue() {
		return status;
	}

}