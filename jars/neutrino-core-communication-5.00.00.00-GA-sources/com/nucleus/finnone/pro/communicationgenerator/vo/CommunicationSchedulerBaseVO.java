package com.nucleus.finnone.pro.communicationgenerator.vo;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.rules.model.SourceProduct;

public class CommunicationSchedulerBaseVO {

	private Long id;
	
	private String schedulerName;
	
	private String cronExpression;
	
	private String sourceProduct;
	
	private boolean activeFlag = true;
	
	private Integer runOnHoliday = 0;	

	private Integer maintainExecutionLog = 0;
	
	private Integer cronBuilderSelector = 0;
	
	
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime          endDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSchedulerName() {
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}	

    public String getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(String sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public boolean isActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Integer getRunOnHoliday() {
		return runOnHoliday;
	}

	public void setRunOnHoliday(Integer runOnHoliday) {
		this.runOnHoliday = runOnHoliday;
	}

	public Integer getMaintainExecutionLog() {
		return maintainExecutionLog;
	}

	public void setMaintainExecutionLog(Integer maintainExecutionLog) {
		this.maintainExecutionLog = maintainExecutionLog;
	}

	public Integer getCronBuilderSelector() {
		return cronBuilderSelector;
	}

	public void setCronBuilderSelector(Integer cronBuilderSelector) {
		this.cronBuilderSelector = cronBuilderSelector;
	}

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }
	
}
