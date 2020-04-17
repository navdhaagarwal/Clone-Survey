package com.nucleus.finnone.pro.communicationgenerator.vo;

public class CommunicationEventRequestSchedulerVO  extends CommunicationSchedulerBaseVO{

	private Long[] eventCodeIds;

	public Long[] getEventCodeIds() {
		return eventCodeIds;
	}

	public void setEventCodeIds(Long[] eventCodeIds) {
		this.eventCodeIds = eventCodeIds;
	}
	
}
