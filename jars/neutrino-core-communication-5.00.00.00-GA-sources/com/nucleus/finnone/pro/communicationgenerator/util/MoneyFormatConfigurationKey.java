package com.nucleus.finnone.pro.communicationgenerator.util;

import java.util.Objects;

/**
 * In communication generation it will be used to put 
 * configurations related with money formatting in weak referenced cache.
 */
public class MoneyFormatConfigurationKey {
	
	private String schedulerInstanceId;
	
	public MoneyFormatConfigurationKey(String schedulerInstanceId) {
		this.schedulerInstanceId = schedulerInstanceId;
	}

	public String getSchedulerInstanceId() {
		return schedulerInstanceId;
	}

	public void setSchedulerInstanceId(String schedulerInstanceId) {
		this.schedulerInstanceId = schedulerInstanceId;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(schedulerInstanceId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MoneyFormatConfigurationKey) {
			return Objects.equals(schedulerInstanceId, ((MoneyFormatConfigurationKey) obj).getSchedulerInstanceId());
		}
		return false;
	}
}
