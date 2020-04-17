package com.nucleus.broadcast.vo;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nucleus.broadcast.entity.BroadcastMessage;
import com.nucleus.broadcast.entity.MessageSeverity;

public class BroadcastMessageVO implements Serializable {

	private static final long serialVersionUID = 1L;


	private Long moduleId;
	private String messageCode;
	private String message;
	private Long priority;
	private DateTime startDate;
	private DateTime endDate;
	private String startTime;
	private String endTime;
	private long frequency;
	private long displayDuration;
	private Long severity;
	private long endDateUnix;
	private long startDateUnix;
	

	



	public BroadcastMessageVO(BroadcastMessage broadcastMessage) {
		super();
		this.moduleId = broadcastMessage.getModuleId();
		this.messageCode = broadcastMessage.getMessageCode();
		this.message = broadcastMessage.getMessage();
		this.priority = broadcastMessage.getPriority();
		this.startDate = broadcastMessage.getStartDate();
		this.endDate = broadcastMessage.getEndDate();
		this.startTime = broadcastMessage.getStartTime();
		this.endTime = broadcastMessage.getEndTime();
		this.frequency = broadcastMessage.getFrequency();
		this.displayDuration = broadcastMessage.getDisplayDuration();
		this.severity = getSeverityValue(broadcastMessage.getSeverity());
		this.endDateUnix = endDate.getMillis() / 1000L;
		this.startDateUnix = startDate.getMillis() / 1000L;
		if(broadcastMessage.getModuleId()==null) {
			this.moduleId = -1L;
		}
	}
	
	public BroadcastMessageVO() {
		
	}


	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}


	public String getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	@JsonIgnore
	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
		setStartDateUnix(startDate.getMillis() / 1000L);
	}

	@JsonIgnore
	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
		setEndDateUnix(endDate.getMillis() / 1000L);
	}

	@JsonIgnore
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
		
	}

	@JsonIgnore
	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public long getDisplayDuration() {
		return displayDuration;
	}

	public void setDisplayDuration(long displayDuration) {
		this.displayDuration = displayDuration;
	}

	public Long getSeverity() {
		return severity;
	}

	public void setSeverity(Long severity) {
		this.severity = severity;
	}



	public Long getEndDateUnix() {
		return endDateUnix;
	}



	public void setEndDateUnix(long endDateUnix) {
		this.endDateUnix = endDateUnix;
	}



	public Long getStartDateUnix() {
		return startDateUnix;
	}



	public void setStartDateUnix(long startDateUnix) {
		this.startDateUnix = startDateUnix;
	}
	
	
	private Long getSeverityValue(MessageSeverity severity)
	{
		if(severity.getCode().equals("CRITICAL"))
			return 3L;
		else if(severity.getCode().equals("MAJOR"))
			return 2L;
		else
			return 1L;
		
	}

}
