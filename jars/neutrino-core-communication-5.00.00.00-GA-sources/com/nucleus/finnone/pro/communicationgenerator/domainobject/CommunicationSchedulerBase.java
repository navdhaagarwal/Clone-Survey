package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;


@MappedSuperclass
public class CommunicationSchedulerBase extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name="SCHEDULER_NAME")
	private String schedulerName;
	
	@Column(name="CRON_EXPRESSION")
	private String cronExpression;
	
	@ManyToOne
	@JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sourceProduct;
	
	@Column(name="RUN_ON_HOLIDAY")
	private Integer runOnHoliday = 0;	

	@Column(name="MAINTAIN_EXECUTION_LOG")
	private Integer maintainExecutionLog = 0;
	
	@Column(name="LEGACY_CRON_BUILDER_FLAG")
	private Integer cronBuilderSelector = 0;
	
	@Column
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime          endDate;

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

	public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
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
