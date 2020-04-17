package com.nucleus.broadcast.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@Synonym(grant = "ALL")
@NamedQueries({
		@NamedQuery(name = "getAllBroadcastMessages", query = "select bm from BroadcastMessage bm where bm.masterLifeCycleData.approvalStatus IN (:approvalStatus) and bm.activeFlag=:activeFlag") })
public class BroadcastMessage extends BaseMasterEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String ALL = "ALL";

	@Column(name = "MODULE_ID")
	private Long moduleId;
	@ManyToOne
	@JoinColumn(name = "MODULE_ID", insertable = false, updatable = false, referencedColumnName = "id")
	private SourceProduct module;
	private String messageCode;
	@Column(length = 500)
	private String message;
	private Long priority;
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime startDate;
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime endDate;
	private String startTime;
	private String endTime;
	private long frequency;
	private long displayDuration;
	@Column(name = "SEVERITY_ID")
	private Long severityId;
	@ManyToOne
	@JoinColumn(name = "SEVERITY_ID", insertable = false, updatable = false)
	private MessageSeverity severity;

	

	public Long getSeverityId() {
		return severityId;
	}

	public void setSeverityId(Long severityId) {
		this.severityId = severityId;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		BroadcastMessage broadCastMessage = (BroadcastMessage) baseEntity;
		super.populate(broadCastMessage, cloneOptions);
		broadCastMessage.setSeverityId(severityId);
		broadCastMessage.setModule(module);
		broadCastMessage.setModuleId(moduleId);
		broadCastMessage.setMessageCode(messageCode);
		broadCastMessage.setMessage(message);
		broadCastMessage.setPriority(priority);
		broadCastMessage.setStartDate(startDate);
		broadCastMessage.setEndDate(endDate);
		broadCastMessage.setStartTime(startTime);
		broadCastMessage.setEndTime(endTime);
		broadCastMessage.setFrequency(frequency);
		broadCastMessage.setDisplayDuration(displayDuration);
		broadCastMessage.setSeverity(severity);
		broadCastMessage.setSeverityId(severityId);
	
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		BroadcastMessage broadCastMessage = (BroadcastMessage) baseEntity;
		super.populateFrom(broadCastMessage, cloneOptions);
		this.setSeverityId(broadCastMessage.getSeverityId());
		this.setModule(broadCastMessage.getModule());
		this.setModuleId(broadCastMessage.getModuleId());
		this.setMessageCode(broadCastMessage.getMessageCode());
		this.setMessage(broadCastMessage.getMessage());
		this.setPriority(broadCastMessage.getPriority());
		this.setStartDate(broadCastMessage.getStartDate());
		this.setEndDate(broadCastMessage.getEndDate());
		this.setStartTime(broadCastMessage.getStartTime());
		this.setEndTime(broadCastMessage.getEndTime());
		this.setFrequency(broadCastMessage.getFrequency());
		this.setDisplayDuration(broadCastMessage.getDisplayDuration());
		this.setSeverity(broadCastMessage.getSeverity());
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

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate2) {
		this.startDate = startDate2;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

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

	public MessageSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(MessageSeverity severity) {
		this.severity = severity;
	}

	public Long getModuleId() {
		if (moduleId == null) {
			return -1L;
		}
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		if (null!=moduleId && moduleId == -1) {
			moduleId = null;
		}
		this.moduleId = moduleId;
	}

	public SourceProduct getModule() {
		if (module == null) {
			SourceProduct allModule = new SourceProduct();
			allModule.setId(-1L);
			allModule.setName(ALL);
			allModule.setCode(ALL);
			return allModule;
		}

		return module;
	}

	public void setModule(SourceProduct module) {
		this.module = module;
	}

}
